# 精尽 Netty 源码解析 —— Channel（五）之 flush 操作

# []( "1. 概述")1. 概述

本文接 [《精尽 Netty 源码解析 —— Channel（四）之 write 操作》](http://svip.iocoder.cn/Netty/Channel-4-write/) ，分享 Netty Channel 的

/#flush()
方法，刷新**内存队列**，将其中的数据写入到对端。

在本文中，我们会发现，

/#flush()
方法和

/#write(Object msg, ...)
**正常**情况下，经历的流程是**差不多**的，例如在 pipeline 中对事件的传播，从

tail
节点传播到

head
节点，最终交由 Unsafe 处理，而差异点就是 Unsafe 的处理方式**不同**：

- write 方法：将数据写到**内存队列**中。
- flush 方法：刷新**内存队列**，将其中的数据写入到对端。

当然，上述描述仅仅指的是**正常**情况下，在**异常**情况下会有所不同。我们知道，Channel 大多数情况下是**可写**的，所以不需要专门去注册

SelectionKey.OP_WRITE
事件。所以在 Netty 的实现中，默认 Channel 是**可写**的，当写入失败的时候，再去注册

SelectionKey.OP_WRITE
事件。这意味着什么呢？在

/#flush()
方法中，如果写入数据到 Channel 失败，会通过注册

SelectionKey.OP_WRITE
事件，然后在轮询到 Channel **可写** 时，再“回调”

/#forceFlush()
方法。

是不是非常巧妙？！让我直奔代码，大口吃肉，潇洒撸码。
下文的 [「2.」]()、[「3.」]()、[「4.」]()、[「5.」]() 和 [《精尽 Netty 源码解析 —— Channel（四）之 write 操作》](http://svip.iocoder.cn/Netty/Channel-4-write) 非常**类似**，所以胖友可以快速浏览。真正的**差异**，从 [「6.」]() 开始。

# []( "2. AbstractChannel")2. AbstractChannel

AbstractChannel 对

/#flush()
方法的实现，代码如下：

```
@Override
public Channel flush(){
pipeline.flush();
return this;
}
```

- 在方法内部，会调用对应的

ChannelPipeline/#flush()
方法，将 flush 事件在 pipeline 上传播。详细解析，见 [「3. DefaultChannelPipeline」]() 。

- 最终会传播 flush 事件到

head
节点，刷新**内存队列**，将其中的数据写入到对端。详细解析，见 [「5. HeadContext」]() 。

# []( "3. DefaultChannelPipeline")3. DefaultChannelPipeline

DefaultChannelPipeline/#flush()
方法，代码如下：

```
@Override
public final ChannelPipeline flush(){
tail.flush();
return this;
}
```

- 在方法内部，会调用

TailContext/#flush()
方法，将 flush 事件在 pipeline 中，从尾节点向头节点传播。详细解析，见 [「4. TailContext」]() 。

# []( "4. TailContext")4. TailContext

TailContext 对

TailContext/#flush()
方法的实现，是从 AbstractChannelHandlerContext 抽象类继承，代码如下：

```
1: @Override
2: public ChannelHandlerContext flush(){
3: // 获得下一个 Outbound 节点
4: final AbstractChannelHandlerContext next = findContextOutbound();
5: EventExecutor executor = next.executor();
6: // 在 EventLoop 的线程中
7: if (executor.inEventLoop()) {
8: // 执行 flush 事件到下一个节点
9: next.invokeFlush();
10: // 不在 EventLoop 的线程中
11: } else {
12: // 创建 flush 任务
13: Runnable task = next.invokeFlushTask;
14: if (task == null) {
15: next.invokeFlushTask = task = new Runnable() {
16: @Override
17: public void run(){
18: next.invokeFlush();
19: }
20: };
21: }
22: // 提交到 EventLoop 的线程中，执行该任务
23: safeExecute(executor, task, channel().voidPromise(), null);
24: }
25:
26: return this;
27: }
```

- 第 4 行：调用

/#findContextOutbound()
方法，获得**下一个** Outbound 节点。

- 第 7 行：**在** EventLoop 的线程中。

- 第 12 至 15 行：调用

AbstractChannelHandlerContext/#invokeFlush()()
方法，执行 flush 事件到下一个节点。

- 后续的逻辑，和 [《精尽 Netty 源码解析 —— ChannelPipeline（四）之 Outbound 事件的传播》](http://svip.iocoder.cn/Netty/Pipeline-4-outbound/) 分享的 **bind** 事件在 pipeline 中的传播是**基本一致**的。
- 随着 flush **事件**不断的向下一个节点传播，最终会到达 HeadContext 节点。详细解析，见 [「5. HeadContext」]() 。
- 第 16 行：**不在** EventLoop 的线程中。

- 第 12 至 21 行：创建 flush 任务。该任务的内部的调用【第 18 行】的代码，和【第 9 行】的代码是**一致**的。
- 第 23 行：调用

/#safeExecute(executor, task, promise, m)
方法，提交到 EventLoop 的线程中，执行该任务。从而实现，**在** EventLoop 的线程中，执行 flush 事件到下一个节点。

# []( "5. HeadContext")5. HeadContext

在 pipeline 中，flush 事件最终会到达 HeadContext 节点。而 HeadContext 的

/#flush()
方法，会处理该事件，代码如下：

```
@Override
public void flush(ChannelHandlerContext ctx) throws Exception{
unsafe.flush();
}
```

- 在方法内部，会调用

AbstractUnsafe/#flush()
方法，刷新**内存队列**，将其中的数据写入到对端。详细解析，见 [「6. AbstractUnsafe」]() 。

# []( "6. AbstractUnsafe")6. AbstractUnsafe

AbstractUnsafe/#flush()
方法，刷新**内存队列**，将其中的数据写入到对端。代码如下：

```
1: @Override
2: public final void flush(){
3: assertEventLoop();
4:
5: // 内存队列为 null ，一般是 Channel 已经关闭，所以直接返回。
6: ChannelOutboundBuffer outboundBuffer = this.outboundBuffer;
7: if (outboundBuffer == null) {
8: return;
9: }
10:
11: // 标记内存队列开始 flush
12: outboundBuffer.addFlush();
13: // 执行 flush
14: flush0();
15: }
```

- 第 5 至 9 行：内存队列为

null
，一般是 Channel **已经关闭**，所以直接返回。

- 第 12 行：调用

ChannelOutboundBuffer/#addFlush()
方法，标记内存队列开始 **flush** 。详细解析，见 [「8.4 addFlush」]() 。

- 第 14 行：调用

/#flush0()
方法，执行 flush 操作。代码如下：

```
//*/*
/* 是否正在 flush 中，即正在调用 {@link /#flush0()} 中
/*/
private boolean inFlush0;
1: @SuppressWarnings("deprecation")
2: protected void flush0(){
3: // 正在 flush 中，所以直接返回。
4: if (inFlush0) {
5: // Avoid re-entrance
6: return;
7: }
8:
9: // 内存队列为 null ，一般是 Channel 已经关闭，所以直接返回。
10: // 内存队列为空，无需 flush ，所以直接返回
11: final ChannelOutboundBuffer outboundBuffer = this.outboundBuffer;
12: if (outboundBuffer == null || outboundBuffer.isEmpty()) {
13: return;
14: }
15:
16: // 标记正在 flush 中。
17: inFlush0 = true;
18:
19: // 若未激活，通知 flush 失败
20: // Mark all pending write requests as failure if the channel is inactive.
21: if (!isActive()) {
22: try {
23: if (isOpen()) {
24: outboundBuffer.failFlushed(FLUSH0_NOT_YET_CONNECTED_EXCEPTION, true);
25: } else {
26: // Do not trigger channelWritabilityChanged because the channel is closed already.
27: outboundBuffer.failFlushed(FLUSH0_CLOSED_CHANNEL_EXCEPTION, false);
28: }
29: } finally {
30: // 标记不在 flush 中。
31: inFlush0 = false;
32: }
33: return;
34: }
35:
36: // 执行真正的写入到对端
37: try {
38: doWrite(outboundBuffer);
39: } catch (Throwable t) {
40: // TODO 芋艿 细节
41: if (t instanceof IOException && config().isAutoClose()) {
42: //*/*
43: /* Just call {@link /#close(ChannelPromise, Throwable, boolean)} here which will take care of
44: /* failing all flushed messages and also ensure the actual close of the underlying transport
45: /* will happen before the promises are notified.
46: /*
47: /* This is needed as otherwise {@link /#isActive()} , {@link /#isOpen()} and {@link /#isWritable()}
48: /* may still return {@code true} even if the channel should be closed as result of the exception.
49: /*/
50: close(voidPromise(), t, FLUSH0_CLOSED_CHANNEL_EXCEPTION, false);
51: } else {
52: try {
53: shutdownOutput(voidPromise(), t);
54: } catch (Throwable t2) {
55: close(voidPromise(), t2, FLUSH0_CLOSED_CHANNEL_EXCEPTION, false);
56: }
57: }
58: } finally {
59: // 标记不在 flush 中。
60: inFlush0 = false;
61: }
62: }
```

- inFlush0
  字段，是否正在 flush 中，即正在调用

/#flush0()
中。

- 第 3 至 7 行：正在 flush 中，所以直接返回。
- 第 9 至 14 行：

- outboundBuffer == null
  ，内存队列为

null
，一般是 Channel 已经**关闭**，所以直接返回。

- outboundBuffer.isEmpty()
  ，内存队列为空，无需 flush ，所以直接返回。
- 第 17 行：设置

inFlush0
为

true
，表示正在 flush 中。

- 第 19 至 34 行：调用

/#isActive()
方法，发现 Channel **未激活**，在根据 Channel **是否打开**，调用

ChannelOutboundBuffer/#failFlushed(Throwable cause, boolean notify)
方法，通知 flush 失败**异常**。详细解析，见 [「8.6 failFlushed」]() 。

- 第 29 至 33 行：最终，设置

inFlush0
为

false
，表示结束 flush 操作，最后

return
返回。

- 第 38 行：调用

AbstractChannel/#doWrite(outboundBuffer)
方法，**执行真正的写入到对端**。详细解析，见 [「7. NioSocketChannel」]() 。

- 第 39 至 57 行：TODO 芋艿 细节
- 第 58 至 61 行：同【第 29 至 33】的代码和目的。
- 实际上，AbstractNioUnsafe **重写**了

/#flush0()
方法，代码如下：

```
@Override
protected final void flush0(){
// Flush immediately only when there's no pending flush.
// If there's a pending flush operation, event loop will call forceFlush() later,
// and thus there's no need to call it now.
if (!isFlushPending()) {
super.flush0();
}
}
```

- 在执行父类 AbstractUnsafe 的

/#flush0()
方法时，先调用

AbstractNioUnsafe/#isFlushPending()
判断，是否已经处于 flush **准备**中。代码如下：

```
private boolean isFlushPending(){
SelectionKey selectionKey = selectionKey();
return selectionKey.isValid() // 合法
&& (selectionKey.interestOps() & SelectionKey.OP_WRITE) != 0; // 对 SelectionKey.OP_WRITE 事件不感兴趣。
}
```

- 是不是有点懵 x ？在文初，我们提到：“所以在 Netty 的实现中，默认 Channel 是**可写**的，当写入失败的时候，再去注册

SelectionKey.OP_WRITE
事件。这意味着什么呢？在

/#flush()
方法中，如果写入数据到 Channel 失败，会通过注册

SelectionKey.OP_WRITE
事件，然后在轮询到 Channel **可写** 时，再“回调”

/#forceFlush()
方法”。

- 这就是这段代码的目的，如果处于对

SelectionKey.OP_WRITE
事件感兴趣，说明 Channel 此时是**不可写**的，那么调用父类 AbstractUnsafe 的

/#flush0()
方法，**也没有意义**，所以就不调用。

- 😈 逻辑上，略微有点复杂，胖友好好理解下。

# []( "7. NioSocketChannel")7. NioSocketChannel

AbstractChannel/#doWrite(ChannelOutboundBuffer in)
**抽象**方法，**执行真正的写入到对端**。定义在 AbstractChannel **抽象**类中，代码如下：

```
//*/*
/* Flush the content of the given buffer to the remote peer.
/*/
protected abstract void doWrite(ChannelOutboundBuffer in) throws Exception;
```

NioSocketChannel 对该**抽象**方法，实现代码如下：

```
1: @Override
2: protected void doWrite(ChannelOutboundBuffer in) throws Exception{
3: SocketChannel ch = javaChannel();
4: // 获得自旋写入次数
5: int writeSpinCount = config().getWriteSpinCount();
6: do {
7: // 内存队列为空，结束循环，直接返回
8: if (in.isEmpty()) {
9: // 取消对 SelectionKey.OP_WRITE 的感兴趣
10: // All written so clear OP_WRITE
11: clearOpWrite();
12: // Directly return here so incompleteWrite(...) is not called.
13: return;
14: }
15:
16: // 获得每次写入的最大字节数
17: // Ensure the pending writes are made of ByteBufs only.
18: int maxBytesPerGatheringWrite = ((NioSocketChannelConfig) config).getMaxBytesPerGatheringWrite();
19: // 从内存队列中，获得要写入的 ByteBuffer 数组
20: ByteBuffer[] nioBuffers = in.nioBuffers(1024, maxBytesPerGatheringWrite);
21: // 写入的 ByteBuffer 数组的个数
22: int nioBufferCnt = in.nioBufferCount();
23:
24: // 写入 ByteBuffer 数组，到对端
25: // Always us nioBuffers() to workaround data-corruption.
26: // See https://github.com/netty/netty/issues/2761
27: switch (nioBufferCnt) {
28: case 0:
29: // 芋艿 TODO 1014 扣 doWrite0 的细节
30: // We have something else beside ByteBuffers to write so fallback to normal writes.
31: writeSpinCount -= doWrite0(in);
32: break;
33: case 1: {
34: // Only one ByteBuf so use non-gathering write
35: // Zero length buffers are not added to nioBuffers by ChannelOutboundBuffer, so there is no need
36: // to check if the total size of all the buffers is non-zero.
37: ByteBuffer buffer = nioBuffers[0];
38: int attemptedBytes = buffer.remaining();
39: // 执行 NIO write 调用，写入单个 ByteBuffer 对象到对端
40: final int localWrittenBytes = ch.write(buffer);
41: // 写入字节小于等于 0 ，说明 NIO Channel 不可写，所以注册 SelectionKey.OP_WRITE ，等待 NIO Channel 可写，并返回以结束循环
42: if (localWrittenBytes <= 0) {
43: incompleteWrite(true);
44: return;
45: }
46: // TODO 芋艿 调整每次写入的最大字节数
47: adjustMaxBytesPerGatheringWrite(attemptedBytes, localWrittenBytes, maxBytesPerGatheringWrite);
48: // 从内存队列中，移除已经写入的数据( 消息 )
49: in.removeBytes(localWrittenBytes);
50: // 写入次数减一
51: --writeSpinCount;
52: break;
53: }
54: default: {
55: // Zero length buffers are not added to nioBuffers by ChannelOutboundBuffer, so there is no need
56: // to check if the total size of all the buffers is non-zero.
57: // We limit the max amount to int above so cast is safe
58: long attemptedBytes = in.nioBufferSize();
59: // 执行 NIO write 调用，写入多个 ByteBuffer 到对端
60: final long localWrittenBytes = ch.write(nioBuffers, 0, nioBufferCnt);
61: // 写入字节小于等于 0 ，说明 NIO Channel 不可写，所以注册 SelectionKey.OP_WRITE ，等待 NIO Channel 可写，并返回以结束循环
62: if (localWrittenBytes <= 0) {
63: incompleteWrite(true);
64: return;
65: }
66: // TODO 芋艿 调整每次写入的最大字节数
67: // Casting to int is safe because we limit the total amount of data in the nioBuffers to int above.
68: adjustMaxBytesPerGatheringWrite((int) attemptedBytes, (int) localWrittenBytes, maxBytesPerGatheringWrite);
69: // 从内存队列中，移除已经写入的数据( 消息 )
70: in.removeBytes(localWrittenBytes);
71: // 写入次数减一
72: --writeSpinCount;
73: break;
74: }
75: }
76: } while (writeSpinCount > 0); // 循环自旋写入
77:
78: // 内存队列中的数据未完全写入，说明 NIO Channel 不可写，所以注册 SelectionKey.OP_WRITE ，等待 NIO Channel 可写
79: incompleteWrite(writeSpinCount < 0);
80: }
```

- 第 3 行：调用

/#javaChannel()
方法，获得 Java NIO **原生** SocketChannel 。

- 第 5 行：调用

ChannelConfig/#getWriteSpinCount()
方法，获得**自旋**写入次数 N 。在【第 6 至 76 行】的代码，我们可以看到，不断**自旋**写入 N 次，直到完成写入结束。关于该配置项，官方注释如下：

```
//*/*
/* Returns the maximum loop count for a write operation until {@link WritableByteChannel/#write(ByteBuffer)} returns a non-zero value.
/* It is similar to what a spin lock is used for in concurrency programming.
/* It improves memory utilization and write throughput depending on the platform that JVM runs on. The default value is {@code 16}.
/*/
int getWriteSpinCount();
```

- 默认值为

DefaultChannelConfig.writeSpinCount = 16
，可配置修改，一般不需要。

- 第 6 至 76 行：不断**自旋**写入 N 次，直到完成写入结束。
- 第 8 行：调用

ChannelOutboundBuffer/#isEmpty()
方法，内存队列为空，结束循环，直接返回。

- 第 10 行：因为在 Channel **不可写**的时候，会注册

SelectionKey.OP_WRITE
，等待 NIO Channel 可写。而后会”回调”

/#forceFlush()
方法，该方法内部也会调用

/#doWrite(ChannelOutboundBuffer in)
方法。所以在完成内部队列的数据向对端写入时候，需要调用

/#clearOpWrite()
方法，代码如下：

```
protected final void clearOpWrite(){
final SelectionKey key = selectionKey();
// Check first if the key is still valid as it may be canceled as part of the deregistration
// from the EventLoop
// See https://github.com/netty/netty/issues/2104
if (!key.isValid()) { // 合法
return;
}
final int interestOps = key.interestOps();
// 若注册了 SelectionKey.OP_WRITE ，则进行取消
if ((interestOps & SelectionKey.OP_WRITE) != 0) {
key.interestOps(interestOps & ~SelectionKey.OP_WRITE);
}
}
```

- 😈 胖友看下代码注释。
- 第 18 行：调用

NioSocketChannelConfig/#getMaxBytesPerGatheringWrite()
方法，获得每次写入的最大字节数。// TODO 芋艿 调整每次写入的最大字节数

- 第 20 行：调用

ChannelOutboundBuffer/#nioBuffers(int maxCount, long maxBytes)
方法，从内存队列中，获得要写入的 ByteBuffer 数组。**注意**，如果内存队列中数据量很大，可能获得的仅仅是一部分数据。详细解析，见 [「8.5 nioBuffers」]() 。

- 第 22 行：获得写入的 ByteBuffer 数组的个数。为什么不直接调用数组的

/#length()
方法呢？因为返回的 ByteBuffer 数组是**预先生成的数组缓存**，存在不断重用的情况，所以不能直接使用

/#length()
方法，而是要调用

ChannelOutboundBuffer/#nioBufferCount()
方法，获得写入的 ByteBuffer 数组的个数。详细解析，见 [「8.5 nioBuffers」]() 。

- 后续根据

nioBufferCnt
的数值，分成**三种**情况。

- **(づ￣ 3 ￣)づ ╭❤ ～ 第一种**，

nioBufferCnt = 0
。

- 芋艿 TODO 1014 扣 doWrite0 的细节，应该是内部的数据为 FileRegion ，可以暂时无视，不影响本文理解。
- **(づ￣ 3 ￣)づ ╭❤ ～ 第二种**，

nioBufferCnt = 1
。

- 第 40 行：调用 Java **原生**

SocketChannel/#write(ByteBuffer buffer)
方法，执行 NIO write 调用，写入**单个** ByteBuffer 对象到对端。

- 第 42 行：写入字节小于等于 0 ，说明 NIO Channel **不可写**，所以注册

SelectionKey.OP_WRITE
，等待 NIO Channel **可写**，并返回以结束循环。

- 第 43 行：调用

AbstractNioByteChannel/#incompleteWrite(true)
方法，代码如下：

```
protected final void incompleteWrite(boolean setOpWrite){
// Did not write completely.
// true ，注册对 SelectionKey.OP_WRITE 事件感兴趣
if (setOpWrite) {
setOpWrite();
// false ，取消对 SelectionKey.OP_WRITE 事件感兴趣
} else {
// It is possible that we have set the write OP, woken up by NIO because the socket is writable, and then
// use our write quantum. In this case we no longer want to set the write OP because the socket is still
// writable (as far as we know). We will find out next time we attempt to write if the socket is writable
// and set the write OP if necessary.
clearOpWrite();
// Schedule flush again later so other tasks can be picked up in the meantime
// 立即发起下一次 flush 任务
eventLoop().execute(flushTask); // <1>
}
}
```

- setOpWrite
  为

true
，调用

/#setOpWrite()
方法，注册对

SelectionKey.OP_WRITE
事件感兴趣。代码如下：

```
protected final void setOpWrite(){
final SelectionKey key = selectionKey();
// Check first if the key is still valid as it may be canceled as part of the deregistration
// from the EventLoop
// See https://github.com/netty/netty/issues/2104
if (!key.isValid()) { // 合法
return;
}
final int interestOps = key.interestOps();
// 注册 SelectionKey.OP_WRITE 事件的感兴趣
if ((interestOps & SelectionKey.OP_WRITE) == 0) {
key.interestOps(interestOps | SelectionKey.OP_WRITE);
}
}
```

- 【第 43 行】的代码，就是这种情况。
- setOpWrite
  为

false
，调用

/#clearOpWrite()
方法，取消对 SelectionKey.OP_WRITE 事件感兴趣。而后，在

<1>
处，立即发起下一次 flush 任务。

- 第 47 行：TODO 芋艿 调整每次写入的最大字节数
- 第 49 行：调用

ChannelOutboundBuffer/#removeBytes(long writtenBytes)
方法啊，从内存队列中，移除已经写入的数据( 消息 )。详细解析，见 [「8.7 removeBytes」]() 。

- 第 51 行：写入次数减一。
- **(づ￣ 3 ￣)づ ╭❤ ～ 第三种**，

nioBufferCnt > 1
。和【第二种】基本相同，差别是在于【第 60 行】的代码，调用 Java **原生**

SocketChannel/#write(ByteBuffer[] srcs, int offset, int length)
方法，执行 NIO write 调用，写入**多个** ByteBuffer 对象到对端。😈 批量一次性写入，提升性能。

- =========== 结束 ===========
- 第 79 行：通过

writeSpinCount < 0
来判断，内存队列中的数据**是否**未完全写入。从目前逻辑看下来，笔者认为只会返回

true
，即内存队列中的数据未完全写入，说明 NIO Channel 不可写，所以注册

SelectionKey.OP_WRITE
，等待 NIO Channel 可写。因此，调用

/#incompleteWrite(true)
方法。

- 举个例子，最后一次写入，Channel 的缓冲区还剩下 10 字节可写，内存队列中剩余 90 字节，那么可以成功写入 10 字节，剩余 80 字节。😈 也就说，此时 Channel 不可写落。

## []( "7.1 乱入")7.1 乱入

老艿艿：临时插入下 AbstractNioByteChannel 和 AbstractNioMessageChannel 实现类对

/#doWrite(ChannelOutboundBuffer in)
方法的实现。不感兴趣的胖友，可以直接跳过。

**AbstractNioByteChannel**

虽然，AbstractNioByteChannel 实现了

/#doWrite(ChannelOutboundBuffer in)
方法，但是子类 NioSocketChannel 又覆盖实现了该方法，所以可以忽略 AbstractNioByteChannel 的实现方法了。

那么为什么 AbstractNioByteChannel 会实现了

/#doWrite(ChannelOutboundBuffer in)
方法呢？因为 NioUdtByteConnectorChannel 和 NioUdtByteRendezvousChannel 会使用到该方法。但是，这两个类已经被**标记废弃**，因为：

```
transport udt is deprecated and so the user knows it will be removed in the future.
```

- 来自 Netty 官方提交的注释说明。

**AbstractNioMessageChannel**

虽然，AbstractNioMessageChannel 实现了

/#doWrite(ChannelOutboundBuffer in)
方法，但是对于 NioServerSocketChannel 来说，暂时没有意义，因为：

```
@Override
protected boolean doWriteMessage(Object msg, ChannelOutboundBuffer in) throws Exception{
throw new UnsupportedOperationException();
}
@Override
protected final Object filterOutboundMessage(Object msg) throws Exception{
throw new UnsupportedOperationException();
}
```

- 两个方法，都是直接抛出 UnsupportedOperationException 异常。

那么为什么 AbstractNioMessageChannel 会实现了

/#doWrite(ChannelOutboundBuffer in)
方法呢？因为 NioDatagramChannel 和 NioSctpChannel **等等**会使用到该方法。感兴趣的胖友，可以自己研究下。

# []( "8. ChannelOutboundBuffer")8. ChannelOutboundBuffer

io.netty.channel.ChannelOutboundBuffer
，**内存队列**。

- 在 write 操作时，将数据写到 ChannelOutboundBuffer 中。
- 在 flush 操作时，将 ChannelOutboundBuffer 的数据写入到对端。

## []( "8.1 Entry")8.1 Entry

在 write 操作时，将数据写到 ChannelOutboundBuffer 中，都会产生一个 Entry 对象。代码如下：

```
//*/*
/* Recycler 对象，用于重用 Entry 对象
/*/
private static final Recycler<Entry> RECYCLER = new Recycler<Entry>() {
@Override
protected Entry newObject(Handle<Entry> handle){
return new Entry(handle);
}
};
//*/*
/* Recycler 处理器
/*/
private final Handle<Entry> handle;
//*/*
/* 下一条 Entry
/*/
Entry next;
//*/*
/* 消息（数据）
/*/
Object msg;
//*/*
/* {@link /#msg} 转化的 NIO ByteBuffer 数组
/*/
ByteBuffer[] bufs;
//*/*
/* {@link /#msg} 转化的 NIO ByteBuffer 对象
/*/
ByteBuffer buf;
//*/*
/* Promise 对象
/*/
ChannelPromise promise;
//*/*
/* 已写入的字节数
/*/
long progress;
//*/*
/* 长度，可读字节数数。
/*/
long total;
//*/*
/* 每个 Entry 预计占用的内存大小，计算方式为消息( {@link /#msg} )的字节数 + Entry 对象自身占用内存的大小。
/*/
int pendingSize;
//*/*
/* {@link /#msg} 转化的 NIO ByteBuffer 的数量。
/*
/* 当 = 1 时，使用 {@link /#buf}
/* 当 > 1 时，使用 {@link /#bufs}
/*/
int count = -1;
//*/*
/* 是否取消写入对端
/*/
boolean cancelled;
private Entry(Handle<Entry> handle){
this.handle = handle;
}
```

- RECYCLER
  **静态**属性，用于**重用** Entry 对象。

- handle
  属性，Recycler 处理器，用于**回收** Entry 对象。
- next
  属性，指向**下一条** Entry 。通过它，形成 ChannelOutboundBuffer 内部的链式存储**每条写入数据**的数据结构。
- msg
  属性，写入的消息( 数据 )。

- promise
  属性，Promise 对象。当数据写入成功后，可以通过它回调通知结果。
- total
  属性，长度，可读字节数。通过

/#total(Object msg)
方法来计算。代码如下：

```
private static long total(Object msg){
if (msg instanceof ByteBuf) {
return ((ByteBuf) msg).readableBytes();
}
if (msg instanceof FileRegion) {
return ((FileRegion) msg).count();
}
if (msg instanceof ByteBufHolder) {
return ((ByteBufHolder) msg).content().readableBytes();
}
return -1;
}
```

- 从这个方法，我们看到，

msg
的类型，有 ByteBuf、FileRegion、ByteBufHolder 。

- process
  属性，已写入的字节数。详细解析，见 [「8.7.1 process」]() 。
- count
  属性，

msg
属性转化的 NIO ByteBuffer 的数量。

- bufs
  属性，当

count > 0
时使用，表示

msg
属性转化的 NIO ByteBuffer 数组。

- buf
  属性，当

count = 0
时使用，表示

msg
属性转化的 NIO ByteBuffer 对象。

- cancelled
  属性，是否取消写入对端。
- pendingSize
  属性，每个 Entry 预计占用的内存大小，计算方式为消息(

msg
)的字节数 + Entry 对象自身占用内存的大小。

### []( "8.1.1 newInstance")8.1.1 newInstance

/#newInstance(Object msg, int size, long total, ChannelPromise promise)
**静态**方法，创建 Entry 对象。代码如下：

```
static Entry newInstance(Object msg, int size, long total, ChannelPromise promise){
// 通过 Recycler 重用对象
Entry entry = RECYCLER.get();
// 初始化属性
entry.msg = msg;
entry.pendingSize = size + CHANNEL_OUTBOUND_BUFFER_ENTRY_OVERHEAD;
entry.total = total;
entry.promise = promise;
return entry;
}
```

- 通过 Recycler 来**重用** Entry 对象。

### []( "8.1.2 recycle")8.1.2 recycle

/#recycle()
方法，**回收** Entry 对象，以为下次**重用**该对象。代码如下：

```
void recycle(){
// 重置属性
next = null;
bufs = null;
buf = null;
msg = null;
promise = null;
progress = 0;
total = 0;
pendingSize = 0;
count = -1;
cancelled = false;
// 回收 Entry 对象
handle.recycle(this);
}
```

### []( "8.1.3 recycleAndGetNext")8.1.3 recycleAndGetNext

/#recycleAndGetNext()
方法，获得下一个 Entry 对象，并**回收**当前 Entry 对象。代码如下：

```
Entry recycleAndGetNext(){
// 获得下一个 Entry 对象
Entry next = this.next;
// 回收当前 Entry 对象
recycle();
// 返回下一个 Entry 对象
return next;
}
```

### []( "8.1.4 cancel")8.1.4 cancel

/#cancel()
方法，标记 Entry 对象，取消写入到对端。在 ChannelOutboundBuffer 里，Entry 数组是通过**链式**的方式进行组织，而当某个 Entry 对象( **节点** )如果需要取消写入到对端，是通过设置

canceled = true
来**标记删除**。代码如下：

```
int cancel(){
if (!cancelled) {
// 标记取消
cancelled = true;
int pSize = pendingSize;
// 释放消息( 数据 )相关的资源
// release message and replace with an empty buffer
ReferenceCountUtil.safeRelease(msg);
// 设置为空 ByteBuf
msg = Unpooled.EMPTY_BUFFER;
// 置空属性
pendingSize = 0;
total = 0;
progress = 0;
bufs = null;
buf = null;
// 返回 pSize
return pSize;
}
return 0;
}
```

## []( "8.2 构造方法")8.2 构造方法

```
//*/*
/* Entry 对象自身占用内存的大小
/*/
// Assuming a 64-bit JVM:
// - 16 bytes object header
// - 8 reference fields
// - 2 long fields
// - 2 int fields
// - 1 boolean field
// - padding
static final int CHANNEL_OUTBOUND_BUFFER_ENTRY_OVERHEAD = SystemPropertyUtil.getInt("io.netty.transport.outboundBufferEntrySizeOverhead", 96);
private static final InternalLogger logger = InternalLoggerFactory.getInstance(ChannelOutboundBuffer.class);
//*/*
/* 线程对应的 ByteBuffer 数组缓存
/*
/* 每次调用 {@link /#nioBuffers(int, long)} 会重新生成
/*/
private static final FastThreadLocal<ByteBuffer[]> NIO_BUFFERS = new FastThreadLocal<ByteBuffer[]>() {
@Override
protected ByteBuffer[] initialValue() throws Exception {
return new ByteBuffer[1024];
}
};
//*/*
/* Channel 对象
/*/
private final Channel channel;
// Entry(flushedEntry) --> ... Entry(unflushedEntry) --> ... Entry(tailEntry)
//
//*/*
/* 第一个( 开始 ) flush Entry
/*/
// The Entry that is the first in the linked-list structure that was flushed
private Entry flushedEntry;
//*/*
/* 第一个未 flush Entry
/*/
// The Entry which is the first unflushed in the linked-list structure
private Entry unflushedEntry;
//*/*
/* 尾 Entry
/*/
// The Entry which represents the tail of the buffer
private Entry tailEntry;
//*/*
/* 已 flush 但未写入对端的 Entry 数量
/*
/* {@link /#addFlush()}
/*
/* The number of flushed entries that are not written yet
/*/
private int flushed;
//*/*
/* {@link /#NIO_BUFFERS} 数组大小
/*/
private int nioBufferCount;
//*/*
/* {@link /#NIO_BUFFERS} 字节数
/*/
private long nioBufferSize;
//*/*
/* 正在通知 flush 失败中
/*/
private boolean inFail;
//*/*
/* {@link /#totalPendingSize} 的原子更新器
/*/
private static final AtomicLongFieldUpdater<ChannelOutboundBuffer> TOTAL_PENDING_SIZE_UPDATER = AtomicLongFieldUpdater.newUpdater(ChannelOutboundBuffer.class, "totalPendingSize");
//*/*
/* 总共等待 flush 到对端的内存大小，通过 {@link Entry/#pendingSize} 来合计。
/*/
@SuppressWarnings("UnusedDeclaration")
private volatile long totalPendingSize;
//*/*
/* {@link /#unwritable} 的原子更新器
/*/
private static final AtomicIntegerFieldUpdater<ChannelOutboundBuffer> UNWRITABLE_UPDATER = AtomicIntegerFieldUpdater.newUpdater(ChannelOutboundBuffer.class, "unwritable");
//*/*
/* 是否不可写
/*/
@SuppressWarnings("UnusedDeclaration")
private volatile int unwritable;
//*/*
/* 触发 Channel 可写的改变的任务
/*/
private volatile Runnable fireChannelWritabilityChangedTask;
ChannelOutboundBuffer(AbstractChannel channel) {
this.channel = channel;
}
```

- channel
  属性，所属的 Channel 对象。
- 链式结构

- flushedEntry
  属性，第一个( 开始 ) flush Entry 。
- unflushedEntry
  属性，第一个**未** flush Entry 。
- tailEntry
  属性，尾 Entry 。
- flushed
  属性， 已 flush 但未写入对端的 Entry 数量。
- 指向关系是

Entry(flushedEntry) --> ... Entry(unflushedEntry) --> ... Entry(tailEntry)
。这样看，可能有点抽象，下文源码解析详细理解。

- NIO_BUFFERS
  **静态**属性，线程对应的 NIO ByteBuffer 数组缓存。在

AbstractChannel/#doWrite(ChannelOutboundBuffer)
方法中，会调用

ChannelOutbound/#nioBuffers(int maxCount, long maxBytes)
方法，初始化数组缓存。 详细解析，见 [「8.6 nioBuffers」]() 中。

- nioBufferCount
  属性：NIO ByteBuffer 数组的**数组**大小。
- nioBufferSize
  属性：NIO ByteBuffer 数组的字**节**大小。
- inFail
  属性，正在通知 flush 失败中。详细解析，见 [「8.8 failFlushed」]() 中。
- ChannelOutboundBuffer 写入控制相关。😈 详细解析，见 [「10. ChannelOutboundBuffer」]() 。

- unwritable
  属性，是否不可写。

- UNWRITABLE_UPDATER
  静态属性，

unwritable
属性的原子更新器。

- totalPendingSize
  属性，所有 Entry 预计占用的内存大小，通过

Entry.pendingSize
来合计。

- TOTAL_PENDING_SIZE_UPDATER
  静态属性，

totalPendingSize
属性的原子更新器。

- fireChannelWritabilityChangedTask
  属性，触发 Channel 可写的改变的**任务**。
- CHANNEL_OUTBOUND_BUFFER_ENTRY_OVERHEAD
  **静态**属性，每个 Entry 对象自身占用内存的大小。为什么占用的 96 字节呢？

- - 16 bytes object header
    ，对象头，16 字节。
- - 8 reference fields
    ，实际是 6 个**对象引用**字段，6 /\* 8 = 48 字节。
- - 2 long fields
    ，2 个

long
字段，2 /\* 8 = 16 字节。

- - 2 int fields
    ，1 个

int
字段，2 /\* 4 = 8 字节。

- - 1 boolean field
    ，1 个

boolean
字段，1 字节。

- padding
  ，补齐 8 字节的整数倍，因此 7 字节。
- 因此，合计 96 字节( 64 位的 JVM 虚拟机，并且不考虑压缩 )。
- 如果不理解的胖友，可以看看 [《JVM 中 对象的内存布局 以及 实例分析》](https://www.jianshu.com/p/12a3c97dc2b7) 。

## []( "8.3 addMessage")8.3 addMessage

/#addMessage(Object msg, int size, ChannelPromise promise)
方法，写入消息( 数据 )到内存队列。**注意**，

promise
只有在真正完成写入到对端操作，才会进行通知。代码如下：

```
1: //*/*
2: /* Add given message to this {@link ChannelOutboundBuffer}. The given {@link ChannelPromise} will be notified once
3: /* the message was written.
4: /*/
5: public void addMessage(Object msg, int size, ChannelPromise promise){
6: // 创建新 Entry 对象
7: Entry entry = Entry.newInstance(msg, size, total(msg), promise);
8: // 若 tailEntry 为空，将 flushedEntry 也设置为空。防御型编程，实际不会出现
9: if (tailEntry == null) {
10: flushedEntry = null;
11: // 若 tailEntry 非空，将原 tailEntry 指向新 Entry
12: } else {
13: Entry tail = tailEntry;
14: tail.next = entry;
15: }
16: // 更新 tailEntry 为新 Entry
17: tailEntry = entry;
18: // 若 unflushedEntry 为空，更新为新 Entry
19: if (unflushedEntry == null) {
20: unflushedEntry = entry;
21: }
22:
23: // 增加 totalPendingSize 计数
24: // increment pending bytes after adding message to the unflushed arrays.
25: // See https://github.com/netty/netty/issues/1619
26: incrementPendingOutboundBytes(entry.pendingSize, false);
27: }
```

- 第 7 行：调用

/#newInstance(Object msg, int size, long total, ChannelPromise promise)
**静态**方法，创建 Entry 对象。

- 第 11 至 17 行：修改**尾**节点

tailEntry
为新的 Entry 节点。

- 第 8 至 10 行：若

tailEntry
为空，将

flushedEntry
也设置为空。防御型编程，实际不会出现，胖友可以忽略。😈 当然，原因在

/#removeEntry(Entry e)
方法。

- 第 11 至 15 行：若

tailEntry
非空，将原

tailEntry.next
指向**新** Entry 。

- 第 17 行：更新原

tailEntry
为新 Entry 。

- 第 18 至 21 行：若

unflushedEntry
为空，则更新为新 Entry ，此时相当于**首**节点。

- 第 23 至 26 行：

/#incrementPendingOutboundBytes(long size, ...)
方法，增加

totalPendingSize
计数。详细解析，见 [「10.1 incrementPendingOutboundBytes」]() 。

可能有点抽象，我们来看看基友【闪电侠】对这块的解析：
FROM 闪电侠 [《netty 源码分析之 writeAndFlush 全解析》](https://www.jianshu.com/p/feaeaab2ce56)

初次调用

addMessage
之后，各个指针的情况为

[![](http://static2.iocoder.cn/1ff7a5d2b08b9e6160dd92e74e68145f)](http://static2.iocoder.cn/1ff7a5d2b08b9e6160dd92e74e68145f)

fushedEntry
指向空，

unFushedEntry
和

tailEntry
都指向新加入的节点

第二次调用

addMessage
之后，各个指针的情况为

[![](http://static2.iocoder.cn/1f939423f079ff491b90c8300e7ef3ea)](http://static2.iocoder.cn/1f939423f079ff491b90c8300e7ef3ea)

第 n 次调用

addMessage
之后，各个指针的情况为

[![](http://static2.iocoder.cn/c0077b0dc86ecf1b791a99eeb9664fc3)](http://static2.iocoder.cn/c0077b0dc86ecf1b791a99eeb9664fc3)

可以看到，调用 n 次

addMessage
，

flushedEntry
指针一直指向 NULL ，表示现在还未有节点需要写出到 Socket 缓冲区，而

unFushedEntry
之后有 n 个节点，表示当前还有 n 个节点尚未写出到 Socket 缓冲区中去

## []( "8.4 addFlush")8.4 addFlush

/#addFlush()
方法，标记内存队列每个 Entry 对象，开始 **flush** 。代码如下：
老艿艿：总觉得这个方法名取的有点奇怪，胖友可以直接看英文注释。😈 我“翻译”不好，哈哈哈。

```
1: public void addFlush(){
2: // There is no need to process all entries if there was already a flush before and no new messages
3: // where added in the meantime.
4: //
5: // See https://github.com/netty/netty/issues/2577
6: Entry entry = unflushedEntry;
7: if (entry != null) {
8: // 若 flushedEntry 为空，赋值为 unflushedEntry ，用于记录第一个( 开始 ) flush 的 Entry 。
9: if (flushedEntry == null) {
10: // there is no flushedEntry yet, so start with the entry
11: flushedEntry = entry;
12: }
13: // 计算 flush 的数量，并设置每个 Entry 对应的 Promise 不可取消
14: do {
15: // 增加 flushed
16: flushed ++;
17: // 设置 Promise 不可取消
18: if (!entry.promise.setUncancellable()) { // 设置失败
19: // 减少 totalPending 计数
20: // Was cancelled so make sure we free up memory and notify about the freed bytes
21: int pending = entry.cancel();
22: decrementPendingOutboundBytes(pending, false, true);
23: }
24: // 获得下一个 Entry
25: entry = entry.next;
26: } while (entry != null);
27:
28: // 设置 unflushedEntry 为空，表示所有都 flush
29: // All flushed so reset unflushedEntry
30: unflushedEntry = null;
31: }
32: }
```

- 第 6 至 7 行：若

unflushedEntry
为空，说明每个 Entry 对象已经“标记” flush 。**注意**，“标记”的方式，不是通过 Entry 对象有一个

flushed
字段，而是

flushedEntry
属性，指向第一个( 开始 ) flush 的 Entry ，而

unflushedEntry
置空。

- 第 8 至 12 行：若

flushedEntry
为空，赋值为

unflushedEntry
，用于记录第一个( 开始 ) flush 的 Entry 。

- 第 13 至 26 行：计算需要 flush 的 Entry 数量，并设置每个 Entry 对应的 Promise **不可取消**。

- 第 18 至 23 行：

/#decrementPendingOutboundBytes(long size, ...)
方法，减少

totalPendingSize
计数。

- 第 30 行：设置

unflushedEntry
为空。

可能有点抽象，我们来看看基友【闪电侠】对这块的解析：
FROM 闪电侠 [《netty 源码分析之 writeAndFlush 全解析》](https://www.jianshu.com/p/feaeaab2ce56)

可以结合前面的图来看，首先拿到

unflushedEntry
指针，然后将

flushedEntry
指向

unflushedEntry
所指向的节点，调用完毕之后，三个指针的情况如下所示

[![](http://static2.iocoder.cn/ecb3df153a3df70464b524838b559232)](http://static2.iocoder.cn/ecb3df153a3df70464b524838b559232)

老艿艿：再次切回到老艿艿的频道，呼呼。

当一次需要从内存队列写到对端的数据量非常大，那么可能写着写着 Channel 的缓存区不够，导致 Channel 此时不可写。但是，这一轮

/#addFlush(...)
标记的 Entry 对象并没有都写到对端。例如，准备写到对端的 Entry 的数量是

flush = 10
个，结果只写了 6 个，那么就剩下

flush = 4
。

但是的但是，

/#addMessage(...)
可能又不断写入新的消息( 数据 )到 ChannelOutboundBuffer 中。那会出现什么情况呢？会“分”成两段：

- <1>
  段：自节点

flushedEntry
开始的

flush
个 Entry 节点，需要写入到对端。

- <2>
  段：自节点

unFlushedEntry
开始的 Entry 节点，需要调用

/#addFlush()
方法，添加到

<1>
段中。

这就很好的解释两个事情：

1. 为什么

/#addFlush()
方法，命名是以

"add"
开头。

1. ChannelOutboundBuffer 的链式结构，为什么不是

head
和

tail
**两个**节点，而是

flushedEntry
、

unFlushedEntry
、

flushedEntry
**三个**节点。在此处，请允许老艿艿爆个粗口：真他 x 的巧妙啊。

### []( "8.4.1 size")8.4.1 size

/#size()
方法，获得

flushed
属性。代码如下：

```
//*/*
/* Returns the number of flushed messages in this {@link ChannelOutboundBuffer}.
/*/
public int size(){
return flushed;
}
```

### []( "8.4.2 isEmpty")8.4.2 isEmpty

/#isEmpty()
方法，是否为空。代码如下：

```
//*/*
/* Returns {@code true} if there are flushed messages in this {@link ChannelOutboundBuffer} or {@code false}
/* otherwise.
/*/
public boolean isEmpty(){
return flushed == 0;
}
```

## []( "8.5 current")8.5 current

/#current()
方法，获得**当前**要写入对端的消息( 数据 )。代码如下：

```
//*/*
/* Return the current message to write or {@code null} if nothing was flushed before and so is ready to be written.
/*/
public Object current(){
Entry entry = flushedEntry;
if (entry == null) {
return null;
}
return entry.msg;
}
```

- 即，返回的是

flushedEntry
的消息( 数据 )。

## []( "8.6 nioBuffers")8.6 nioBuffers

/#nioBuffers(int maxCount, long maxBytes)
方法，获得当前要写入到对端的 NIO ByteBuffer 数组，并且获得的数组大小不得超过

maxCount
，字节数不得超过

maxBytes
。我们知道，在写入数据到 ChannelOutboundBuffer 时，一般使用的是 Netty ByteBuf 对象，但是写到 NIO SocketChannel 时，则必须使用 NIO ByteBuffer 对象，因此才有了这个方法。考虑到性能，这个方法里会使用到“**缓存**”，所以看起来会比较绕一丢丢。OK，开始看代码落：

```
//*/*
/* Returns an array of direct NIO buffers if the currently pending messages are made of {@link ByteBuf} only.
/* {@link /#nioBufferCount()} and {@link /#nioBufferSize()} will return the number of NIO buffers in the returned
/* array and the total number of readable bytes of the NIO buffers respectively.
/* <p>
/* Note that the returned array is reused and thus should not escape
/* {@link AbstractChannel/#doWrite(ChannelOutboundBuffer)}.
/* Refer to {@link NioSocketChannel/#doWrite(ChannelOutboundBuffer)} for an example.
/* </p>
/* @param maxCount The maximum amount of buffers that will be added to the return value.
/* @param maxBytes A hint toward the maximum number of bytes to include as part of the return value. Note that this
/* value maybe exceeded because we make a best effort to include at least 1 {@link ByteBuffer}
/* in the return value to ensure write progress is made.
/*/
1: public ByteBuffer[] nioBuffers(int maxCount, long maxBytes) {
2: assert maxCount > 0;
3: assert maxBytes > 0;
4: long nioBufferSize = 0;
5: int nioBufferCount = 0;
6: // 获得当前线程的 NIO ByteBuffer 数组缓存。
7: final InternalThreadLocalMap threadLocalMap = InternalThreadLocalMap.get();
8: ByteBuffer[] nioBuffers = NIO_BUFFERS.get(threadLocalMap);
9: // 从 flushedEntry 节点，开始向下遍历
10: Entry entry = flushedEntry;
11: while (isFlushedEntry(entry) && entry.msg instanceof ByteBuf) {
12: // 若 Entry 节点已经取消，忽略。
13: if (!entry.cancelled) {
14: ByteBuf buf = (ByteBuf) entry.msg;
15: // 获得消息( 数据 )开始读取位置
16: final int readerIndex = buf.readerIndex();
17: // 获得消息( 数据 )可读取的字节数
18: final int readableBytes = buf.writerIndex() - readerIndex;
19:
20: // 若无可读取的数据，忽略。
21: if (readableBytes > 0) {
22: // 前半段，可读取的字节数，不能超过 maxBytes
23: // 后半段，如果第一条数据，就已经超过 maxBytes ，那么只能“强行”读取，否则会出现一直无法读取的情况。
24: if (maxBytes - readableBytes < nioBufferSize && nioBufferCount != 0) {
25: // If the nioBufferSize + readableBytes will overflow maxBytes, and there is at least one entry
26: // we stop populate the ByteBuffer array. This is done for 2 reasons:
27: // 1. bsd/osx don't allow to write more bytes then Integer.MAX_VALUE with one writev(...) call
28: // and so will return 'EINVAL', which will raise an IOException. On Linux it may work depending
29: // on the architecture and kernel but to be safe we also enforce the limit here.
30: // 2. There is no sense in putting more data in the array than is likely to be accepted by the
31: // OS.
32: //
33: // See also:
34: // - https://www.freebsd.org/cgi/man.cgi?query=write&sektion=2
35: // - http://linux.die.net/man/2/writev
36: break;
37: }
38: // 增加 nioBufferSize
39: nioBufferSize += readableBytes;
40: // 初始 Entry 节点的 NIO ByteBuffer 数量
41: int count = entry.count;
42: if (count == -1) {
43: //noinspection ConstantValueVariableUse
44: entry.count = count = buf.nioBufferCount();
45: }
46: // 如果超过 NIO ByteBuffer 数组的大小，进行扩容。
47: int neededSpace = min(maxCount, nioBufferCount + count);
48: if (neededSpace > nioBuffers.length) {
49: nioBuffers = expandNioBufferArray(nioBuffers, neededSpace, nioBufferCount);
50: NIO_BUFFERS.set(threadLocalMap, nioBuffers);
51: }
52: // 初始化 Entry 节点的 buf / bufs 属性
53: if (count == 1) {
54: ByteBuffer nioBuf = entry.buf;
55: if (nioBuf == null) {
56: // cache ByteBuffer as it may need to create a new ByteBuffer instance if its a
57: // derived buffer
58: entry.buf = nioBuf = buf.internalNioBuffer(readerIndex, readableBytes);
59: }
60: nioBuffers[nioBufferCount++] = nioBuf;
61: } else {
62: ByteBuffer[] nioBufs = entry.bufs;
63: if (nioBufs == null) {
64: // cached ByteBuffers as they may be expensive to create in terms
65: // of Object allocation
66: entry.bufs = nioBufs = buf.nioBuffers();
67: }
68: for (int i = 0; i < nioBufs.length && nioBufferCount < maxCount; ++i) {
69: ByteBuffer nioBuf = nioBufs[i];
70: if (nioBuf == null) {
71: break;
72: } else if (!nioBuf.hasRemaining()) {
73: continue;
74: }
75: nioBuffers[nioBufferCount++] = nioBuf;
76: }
77: }
78:
79: // 到达 maxCount 上限，结束循环。老艿艿的想法，这里最好改成 nioBufferCount >= maxCount ，是有可能会超过的
80: if (nioBufferCount == maxCount) {
81: break;
82: }
83: }
84: }
85:
86: // 下一个 Entry节点
87: entry = entry.next;
88: }
89:
90: // 设置 nioBufferCount 和 nioBufferSize 属性
91: this.nioBufferCount = nioBufferCount;
92: this.nioBufferSize = nioBufferSize;
93:
94: return nioBuffers;
95: }
```

- 第 4 至 5 行：初始

nioBufferSize
、

nioBufferCount
计数。

- 第 6 至 8 行：获得当前线程的 NIO ByteBuffer 数组缓存。

- 关于 InternalThreadLocalMap 和 FastThreadLocal ，胖友可以暂时忽略，后续的文章，详细解析。
- 第 10 至 11 行：从

flushedEntry
节点，开始向下遍历。

- 调用

/#isFlushedEntry(Entry entry)
方法，判断是否为已经“标记”为 flush 的 Entry 节点。代码如下：

```
private boolean isFlushedEntry(Entry e){
return e != null && e != unflushedEntry;
}
```

- e != unflushedEntry
  ，就是我们在 [「8.4 addFlush」]() 最后部分讲的，思考下。
- entry.msg instanceof ByteBuf
  ，消息( 数据 )类型为 ByteBuf 。实际上，

msg
的类型也可能是 FileRegion 。如果 ChannelOutboundBuffer 里的消息都是 FileRegion 类型，那就会导致这个方法返回为**空** NIO ByteBuffer 数组。

- 第 13 行：若 Entry 节点已经取消，忽略。
- 第 14 至 18 行：获得消息( 数据 )开始读取位置和可读取的字节数。

- 第 21 行：若无可读取的数据，忽略。
- 第 22 至 37 行：

- 前半段

maxBytes - readableBytes < nioBufferSize
，当前 ByteBuf 可读取的字节数，不能超过

maxBytes
。这个比较好理解。

- 后半段

nioBufferCount != 0
，如果**第一条**数据，就已经超过

maxBytes
，那么只能“强行”读取，否则会出现一直无法读取的情况( 因为不能跳过这条 😈 )。

- 第 39 行：增加

nioBufferSize
。

- 第 40 至 45 行：调用

ByteBuf/#nioBufferCount()
方法，初始 Entry 节点的

count
属性( NIO ByteBuffer 数量)。

- 使用

count == -1
的原因是，

Entry.count
未初始化时，为

-1
。

- 第 47 至 51 行：如果超过 NIO ByteBuffer 数组的大小，调用

/#expandNioBufferArray(ByteBuffer[] array, int neededSpace, int size)
方法，进行扩容。详细解析，见 [「8.6.1 expandNioBufferArray」]() 。

- 第 52 至 77 行：初始 Entry 节点的

buf
或

bufs
属性。

- 当

count = 1
时，调用

ByteBuf/#internalNioBuffer(readerIndex, readableBytes)
方法，获得 NIO ByteBuffer 对象。

- 当

count > 1
时，调用

ByteBuf/#nioBuffers()
方法，获得 NIO ByteBuffer 数组。

- 通过

nioBuffers[nioBufferCount++] = nioBuf
，将 NIO ByteBuffer 赋值到结果数组

nioBuffers
中，并增加

nioBufferCount
。

- 第 79 至 82 行：到达

maxCount
上限，结束循环。老艿艿的想法，这里最好改成

nioBufferCount >= maxCount
，是有可能会超过的。

- 第 87 行：**下一个 Entry 节点**。
- 第 90 至 92 行：设置 ChannelOutboundBuffer 的

nioBufferCount
和

nioBufferSize
属性。

### []( "8.6.1 expandNioBufferArray")8.6.1 expandNioBufferArray

/#expandNioBufferArray(ByteBuffer[] array, int neededSpace, int size)
方法，进行 NIO ByteBuff 数组的**扩容**。代码如下：

```
private static ByteBuffer[] expandNioBufferArray(ByteBuffer[] array, int neededSpace, int size) {
// 计算扩容后的数组的大小，按照 2 倍计算
int newCapacity = array.length;
do {
// double capacity until it is big enough
// See https://github.com/netty/netty/issues/1890
newCapacity <<= 1;
if (newCapacity < 0) {
throw new IllegalStateException();
}
} while (neededSpace > newCapacity);
// 创建新的 ByteBuffer 数组
ByteBuffer[] newArray = new ByteBuffer[newCapacity];
// 复制老的 ByteBuffer 数组到新的 ByteBuffer 数组中
System.arraycopy(array, 0, newArray, 0, size);
return newArray;
}
```

- 代码比较简单，胖友自己看下注释。

### []( "8.6.2 nioBufferCount")8.6.2 nioBufferCount

/#nioBufferCount()
方法，返回

nioBufferCount
属性。代码如下：

```
//*/*
/* Returns the number of {@link ByteBuffer} that can be written out of the {@link ByteBuffer} array that was
/* obtained via {@link /#nioBuffers()}. This method <strong>MUST</strong> be called after {@link /#nioBuffers()}
/* was called.
/*/
public int nioBufferCount(){
return nioBufferCount;
}
```

### []( "8.6.3 nioBufferSize")8.6.3 nioBufferSize

/#nioBufferSize()
方法，返回

nioBufferSize
属性。代码如下：

```
//*/*
/* Returns the number of bytes that can be written out of the {@link ByteBuffer} array that was
/* obtained via {@link /#nioBuffers()}. This method <strong>MUST</strong> be called after {@link /#nioBuffers()}
/* was called.
/*/
public long nioBufferSize(){
return nioBufferSize;
}
```

## []( "8.7 removeBytes")8.7 removeBytes

/#removeBytes(long writtenBytes)
方法，移除已经写入

writtenBytes
字节对应的 Entry 对象 / 对象们。代码如下：

```
1: public void removeBytes(long writtenBytes){
2: // 循环移除
3: for (;;) {
4: // 获得当前消息( 数据 )
5: Object msg = current();
6: if (!(msg instanceof ByteBuf)) {
7: assert writtenBytes == 0;
8: break;
9: }
10:
11: final ByteBuf buf = (ByteBuf) msg;
12: // 获得消息( 数据 )开始读取位置
13: final int readerIndex = buf.readerIndex();
14: // 获得消息( 数据 )可读取的字节数
15: final int readableBytes = buf.writerIndex() - readerIndex;
16:
17: // 当前消息( 数据 )已被写完到对端
18: if (readableBytes <= writtenBytes) {
19: if (writtenBytes != 0) {
20: // 处理当前消息的 Entry 的写入进度
21: progress(readableBytes);
22: // 减小 writtenBytes
23: writtenBytes -= readableBytes;
24: }
25: // 移除当前消息对应的 Entry
26: remove();
27: // 当前消息( 数据 )未被写完到对端
28: } else { // readableBytes > writtenBytes
29: if (writtenBytes != 0) {
30: // 标记当前消息的 ByteBuf 的读取位置
31: buf.readerIndex(readerIndex + (int) writtenBytes);
32: // 处理当前消息的 Entry 的写入进度
33: progress(writtenBytes);
34: }
35: break;
36: }
37: }
38:
39: // 清除 NIO ByteBuff 数组的缓存
40: clearNioBuffers();
41: }
```

- 第 3 行：**循环**，移除已经写入

writtenBytes
字节对应的 Entry 对象。

- 第 5 行：调用

/#current()
方法，获得当前消息( 数据 )。

- 第 12 至 15 行：获得消息( 数据 )开始读取位置和可读取的字节数。
- <1>
  当前消息( 数据 )**已**被写完到对端。
- 第 21 行：调用

/#progress(long amount)
方法，处理当前消息的 Entry 的写入进度。详细解析，见 [「8.7.1 progress」]() 。

- 第 23 行：减小

writtenBytes
。

- 第 26 行：调用

/#remove()
方法，移除当前消息对应的 Entry 对象。详细解析，见 [「8.7.2 remove」]() 。

- <2》
  当前消息( 数据 )**未**被写完到对端。
- 第 31 行：调用

ByteBuf/#readerIndex(readerIndex)
方法，标记当前消息的 ByteBuf 的**读取位置**。

- 第 33 行：调用

/#progress(long amount)
方法，处理当前消息的 Entry 的写入进度。

- 第 35 行：

break
，结束循环。

- 第 40 行：调用

/#clearNioBuffers()
方法，**清除** NIO ByteBuff 数组的缓存。详细解析，见 [「8.7.4 clearNioBuffers」]() 。

### []( "8.7.1 progress")8.7.1 progress

/#progress(long amount)
方法，处理当前消息的 Entry 的写入进度，主要是**通知** Promise 消息写入的进度。代码如下：

```
//*/*
/* Notify the {@link ChannelPromise} of the current message about writing progress.
/*/
1: public void progress(long amount){
2: Entry e = flushedEntry;
3: assert e != null;
4: ChannelPromise p = e.promise;
5: if (p instanceof ChannelProgressivePromise) {
6: // 设置 Entry 对象的 progress 属性
7: long progress = e.progress + amount;
8: e.progress = progress;
9: // 通知 ChannelProgressivePromise 进度
10: ((ChannelProgressivePromise) p).tryProgress(progress, e.total);
11: }
12: }
```

- 第 5 行：若

promise
的类型是 ChannelProgressivePromise 类型。

- 第 6 至 8 行：设置 Entry 对象的

progress
属性。

- 第 10 行：调用

ChannelProgressivePromise/#tryProgress(progress, total)
方法，通知 ChannelProgressivePromise 进度。

### []( "8.7.2 remove")8.7.2 remove

/#remove()
方法，移除当前消息对应的 Entry 对象，并 Promise 通知成功。代码如下：

```
1: public boolean remove(){
2: Entry e = flushedEntry;
3: if (e == null) {
4: // 清除 NIO ByteBuff 数组的缓存
5: clearNioBuffers();
6: return false;
7: }
8: Object msg = e.msg;
9:
10: ChannelPromise promise = e.promise;
11: int size = e.pendingSize;
12:
13: // 移除指定 Entry 对象
14: removeEntry(e);
15:
16: if (!e.cancelled) {
17: // 释放消息( 数据 )相关的资源
18: // only release message, notify and decrement if it was not canceled before.
19: ReferenceCountUtil.safeRelease(msg);
20: // 通知 Promise 执行成功
21: safeSuccess(promise);
22: // 减少 totalPending 计数
23: decrementPendingOutboundBytes(size, false, true);
24: }
25:
26: // 回收 Entry 对象
27: // recycle the entry
28: e.recycle();
29:
30: return true;
31: }
```

- 第 14 行：调用

/#removeEntry(Entry e)
方法，移除**指定** Entry 对象。详细解析，见 [「8.7.3 removeEntry」]() 。

- 第 16 行：若 Entry 已取消，则忽略。
- 第 19 行：

ReferenceCountUtil/#safeRelease(msg)
方法，释放消息( 数据 )相关的资源。

- 第 21 行：【**重要**】调用

/#safeSuccess(promise)
方法，通知 Promise 执行成功。此处才是，真正触发

Channel/#write(...)
或

Channel/#writeAndFlush(...)
方法，返回的 Promise 的通知。

/#safeSuccess(promise)
方法的代码如下：

```
private static void safeSuccess(ChannelPromise promise){
// Only log if the given promise is not of type VoidChannelPromise as trySuccess(...) is expected to return
// false.
PromiseNotificationUtil.trySuccess(promise, null, promise instanceof VoidChannelPromise ? null : logger);
}
```

- 第 23 行：

/#decrementPendingOutboundBytes(long size, ...)
方法，减少

totalPendingSize
计数。

- 第 28 行：调用

Entry/#recycle()
方法，**回收** Entry 对象。

### []( "8.7.3 removeEntry")8.7.3 removeEntry

/#removeEntry(Entry e)
方法，移除**指定** Entry 对象。代码如下：

```
1: private void removeEntry(Entry e){
2: // 已移除完已 flush 的 Entry 节点，置空 flushedEntry、tailEntry、unflushedEntry 。
3: if (-- flushed == 0) {
4: // processed everything
5: flushedEntry = null;
6: if (e == tailEntry) {
7: tailEntry = null;
8: unflushedEntry = null;
9: }
10: // 未移除完已 flush 的 Entry 节点，flushedEntry 指向下一个 Entry 对象
11: } else {
12: flushedEntry = e.next;
13: }
14: }
```

- 第 3 至 9 行：**已**移除完已 flush 的**所有** Entry 节点，置空

flushedEntry
、

tailEntry
、

unflushedEntry
。

- 第 10 至 13 行：**未**移除完已 flush 的**所有** Entry 节点，

flushedEntry
指向**下一个** Entry 对象。

### []( "8.7.4 clearNioBuffers")8.7.4 clearNioBuffers

/#clearNioBuffers()
方法，**清除** NIO ByteBuff 数组的缓存。代码如下：

```
// Clear all ByteBuffer from the array so these can be GC'ed.
// See https://github.com/netty/netty/issues/3837
private void clearNioBuffers(){
int count = nioBufferCount;
if (count > 0) {
// 归零 nioBufferCount 。老艿艿觉得，应该把 nioBufferSize 也归零
nioBufferCount = 0;
// 置空 NIO ByteBuf 数组
Arrays.fill(NIO_BUFFERS.get(), 0, count, null);
}
}
```

- 代码比较简单，胖友自己看注释。主要目的是 help gc 。

## []( "8.8 failFlushed")8.8 failFlushed

/#failFlushed(Throwable cause, boolean notify)
方法，写入数据到对端**失败**，进行后续的处理，详细看代码。代码如下：

```
1: void failFlushed(Throwable cause, boolean notify){
2: // 正在通知 flush 失败中，直接返回
3: // Make sure that this method does not reenter. A listener added to the current promise can be notified by the
4: // current thread in the tryFailure() call of the loop below, and the listener can trigger another fail() call
5: // indirectly (usually by closing the channel.)
6: //
7: // See https://github.com/netty/netty/issues/1501
8: if (inFail) {
9: return;
10: }
11:
12: try {
13: // 标记正在通知 flush 失败中
14: inFail = true;
15: // 循环，移除所有已 flush 的 Entry 节点们
16: for (;;) {
17: if (!remove0(cause, notify)) {
18: break;
19: }
20: }
21: } finally {
22: // 标记不在通知 flush 失败中
23: inFail = false;
24: }
25: }
```

- 第 2 至 10 行：正在通知 flush 失败中，直接返回。
- 第 14 行：标记正在通知 flush 失败中，即

inFail = true
。

- 第 15 至 20 行：循环，调用

/#remove0(Throwable cause, boolean notifyWritability)
方法，移除**所有**已 flush 的 Entry 节点们。详细解析，见 [「8. remove0」]() 中。

- 第 21 至 24 行：标记不在通知 flush 失败中，即

inFail = false
。

### []( "8.8.1 remove0")8.8.1 remove0

/#remove0(Throwable cause, boolean notifyWritability)
方法，移除当前消息对应的 Entry 对象，并 Promise 通知异常。代码如下：

```
1: private boolean remove0(Throwable cause, boolean notifyWritability){
2: Entry e = flushedEntry;
3: // 所有 flush 的 Entry 节点，都已经写到对端
4: if (e == null) {
5: // // 清除 NIO ByteBuff 数组的缓存
6: clearNioBuffers();
7: return false; // 没有后续的 flush 的 Entry 节点
8: }
9: Object msg = e.msg;
10:
11: ChannelPromise promise = e.promise;
12: int size = e.pendingSize;
13:
14: removeEntry(e);
15:
16: if (!e.cancelled) {
17: // 释放消息( 数据 )相关的资源
18: // only release message, fail and decrement if it was not canceled before.
19: ReferenceCountUtil.safeRelease(msg);
20: // 通知 Promise 执行失败
21: safeFail(promise, cause);
22: // 减少 totalPendingSize 计数
23: decrementPendingOutboundBytes(size, false, notifyWritability);
24: }
25:
26: // 回收 Entry 对象
27: // recycle the entry
28: e.recycle();
29:
30: return true; // 还有后续的 flush 的 Entry 节点
31: }
```

- 第 3 至 8 行：若**所有** flush 的 Entry 节点，都已经写到对端，则调用

/#clearNioBuffers()
方法，清除 NIO ByteBuff 数组的缓存。

- 第 14 行：调用

/#removeEntry(Entry e)
方法，移除**指定** Entry 对象。详细解析，见 [「8.7.3 removeEntry」]() 。

- 第 16 行：若 Entry 已取消，则忽略。
- 第 19 行：

ReferenceCountUtil/#safeRelease(msg)
方法，释放消息( 数据 )相关的资源。

- 第 21 行：【**重要**】调用

/#safeFail(promise)
方法，通知 Promise 执行失败。此处才是，真正触发

Channel/#write(...)
或

Channel/#writeAndFlush(...)
方法，返回的 Promise 的通知。

/#safeFail(promise)
方法的代码如下：

```
private static void safeFail(ChannelPromise promise, Throwable cause){
// Only log if the given promise is not of type VoidChannelPromise as tryFailure(...) is expected to return
// false.
PromiseNotificationUtil.tryFailure(promise, cause, promise instanceof VoidChannelPromise ? null : logger);
}
```

- 第 23 行：调用

/#decrementPendingOutboundBytes(long size, ...)
方法，减少

totalPendingSize
计数。

- 第 28 行：调用

Entry/#recycle()
方法，**回收** Entry 对象。

## []( "8.9 forEachFlushedMessage")8.9 forEachFlushedMessage

TODO 1015 forEachFlushedMessage 在

netty-transport-native-poll
和

netty-transport-native-kqueue
中使用，在后续的文章解析。

## []( "8.10 close")8.10 close

/#close(...)
方法，关闭 ChannelOutboundBuffer ，进行后续的处理，详细看代码。代码如下：

```
void close(ClosedChannelException cause){
close(cause, false);
}
1: void close(final Throwable cause, final boolean allowChannelOpen){
2: // 正在通知 flush 失败中
3: if (inFail) {
4: // 提交 EventLoop 的线程中，执行关闭
5: channel.eventLoop().execute(new Runnable() {
6: @Override
7: public void run(){
8: close(cause, allowChannelOpen);
9: }
10: });
11: // 返回
12: return;
13: }
14:
15: // 标记正在通知 flush 失败中
16: inFail = true;
17:
18: if (!allowChannelOpen && channel.isOpen()) {
19: throw new IllegalStateException("close() must be invoked after the channel is closed.");
20: }
21:
22: if (!isEmpty()) {
23: throw new IllegalStateException("close() must be invoked after all flushed writes are handled.");
24: }
25:
26: // Release all unflushed messages.
27: try {
28: // 从 unflushedEntry 节点，开始向下遍历
29: Entry e = unflushedEntry;
30: while (e != null) {
31: // 减少 totalPendingSize
32: // Just decrease; do not trigger any events via decrementPendingOutboundBytes()
33: int size = e.pendingSize;
34: TOTAL_PENDING_SIZE_UPDATER.addAndGet(this, -size);
35:
36: if (!e.cancelled) {
37: // 释放消息( 数据 )相关的资源
38: ReferenceCountUtil.safeRelease(e.msg);
39: // 通知 Promise 执行失败
40: safeFail(e.promise, cause);
41: }
42: // 回收当前节点，并获得下一个 Entry 节点
43: e = e.recycleAndGetNext();
44: }
45: } finally {
46: // 标记在在通知 flush 失败中
47: inFail = false;
48: }
49:
50: // 清除 NIO ByteBuff 数组的缓存。
51: clearNioBuffers();
52: }
```

- 第 3 行：正在通知 flush 失败中：

- 第 5 至 10 行: 提交 EventLoop 的线程中，执行关闭。
- 第 12 行：

return
返回。

- 第 16 行：标记正在通知 flush 失败中，即

inFail = true
。

- 第 28 至 30 行：从

unflushedEntry
节点，开始向下遍历。

- 第 31 至 34 行：减少

totalPendingSize
计数。

- 第 36 行：若 Entry 已取消，则忽略。
- 第 38 行：调用

ReferenceCountUtil/#safeRelease(msg)
方法，释放消息( 数据 )相关的资源。

- 第 40 行：【**重要**】调用

/#safeFail(promise)
方法，通知 Promise 执行失败。此处才是，真正触发

Channel/#write(...)
或

Channel/#writeAndFlush(...)
方法，返回的 Promise 的通知。

- 第 43 行：调用

Entry/#recycleAndGetNext()
方法，回收当前节点，并获得下一个 Entry 节点。

- 第 45 至 48 行：标记不在通知 flush 失败中，即

inFail = false
。

- 第 51 行：调用

/#clearNioBuffers()
方法，**清除** NIO ByteBuff 数组的缓存。

# []( "9. NioEventLoop")9. NioEventLoop

在上文 [「7. NioSocketChannel」]() 中，在写入到 Channel 到对端，若 TCP 数据发送缓冲区**已满**，这将导致 Channel **不写可**，此时会注册对该 Channel 的

SelectionKey.OP_WRITE
事件感兴趣。从而实现，再在 Channel 可写后，进行**强制** flush 。这块的逻辑，在

NioEventLoop/#processSelectedKey(SelectionKey k, AbstractNioChannel ch)
中实现，代码如下：

```
// OP_WRITE 事件就绪
// Process OP_WRITE first as we may be able to write some queued buffers and so free memory.
if ((readyOps & SelectionKey.OP_WRITE) != 0) {
// Call forceFlush which will also take care of clear the OP_WRITE once there is nothing left to write
// 向 Channel 写入数据
ch.unsafe().forceFlush();
}
```

- 通过 Selector 轮询到 Channel 的

OP_WRITE
就绪时，调用

AbstractNioUnsafe/#forceFlush()
方法，强制 flush 。代码如下：

```
// AbstractNioUnsafe.java
@Override
public final void forceFlush(){
// directly call super.flush0() to force a flush now
super.flush0();
}
```

- 后续的逻辑，又回到 [「6. AbstractUnsafe」]() 小节的

/#flush0()
流程。

- 在完成强制 flush 之后，会取消对

SelectionKey.OP_WRITE
事件的感兴趣。

## []( "9.1 如何模拟")9.1 如何模拟

1.

配置服务端 ServerBootstrap 的启动参数如下：

```
.childOption(ChannelOption.SO_SNDBUF, 5) // Socket 参数，TCP 数据发送缓冲区大小。
```

1.

telnet
到启动的服务端，发送相对长的命令，例如

"abcdefghijklmnopqrstuvw11321321321nhdkslk"
。

# []( "10. ChannelOutboundBuffer 写入控制")10. ChannelOutboundBuffer 写入控制

当我们不断调用

/#addMessage(Object msg, int size, ChannelPromise promise)
方法，添加消息到 ChannelOutboundBuffer 内存队列中，如果**不及时** flush 写到对端( 例如程序一直未调用

Channel/#flush()
方法，或者对端接收数据比较慢导致 Channel 不可写 )，可能会导致 **OOM 内存溢出**。所以，在 ChannelOutboundBuffer 使用

totalPendingSize
属性，存储所有 Entry 预计占用的内存大小(

pendingSize
)。

- 在

totalPendingSize
大于高水位阀值时(

ChannelConfig.writeBufferHighWaterMark
，默认值为 64 KB )，**关闭**写开关(

unwritable
)。详细解析，见 [「10.1 incrementPendingOutboundBytes」]() 。

- 在

totalPendingSize
小于低水位阀值时(

ChannelConfig.writeBufferLowWaterMark
，默认值为 32 KB )，**打开**写开关(

unwritable
)。详细解析，见 [「10.2 decrementPendingOutboundBytes」]() 。

该功能，对应 Github 提交为 [《Take memory overhead of ChannelOutboundBuffer / PendingWriteQueue into account》](https://github.com/netty/netty/commit/e3cb9935c0b63357e3d51867cffe624129e7e1dd) 。

## []( "10.1 incrementPendingOutboundBytes")10.1 incrementPendingOutboundBytes

/#incrementPendingOutboundBytes(long size, ...)
方法，增加

totalPendingSize
计数。代码如下：

```
1: //*/*
2: /* Increment the pending bytes which will be written at some point.
3: /* This method is thread-safe!
4: /*/
5: void incrementPendingOutboundBytes(long size){
6: incrementPendingOutboundBytes(size, true);
7: }
8:
9: private void incrementPendingOutboundBytes(long size, boolean invokeLater){
10: if (size == 0) {
11: return;
12: }
13:
14: // 增加 totalPendingSize 计数
15: long newWriteBufferSize = TOTAL_PENDING_SIZE_UPDATER.addAndGet(this, size);
16: // totalPendingSize 大于高水位阀值时，设置为不可写
17: if (newWriteBufferSize > channel.config().getWriteBufferHighWaterMark()) {
18: setUnwritable(invokeLater);
19: }
20: }
```

- 第 15 行：增加

totalPendingSize
计数。

- 第 16 至 19 行：

totalPendingSize
大于高水位阀值时，调用

/#setUnwritable(boolean invokeLater)
方法，设置为不可写。代码如下：

```
1: private void setUnwritable(boolean invokeLater){
2: for (;;) {
3: final int oldValue = unwritable;
4: // 或位操作，修改第 0 位 bits 为 1
5: final int newValue = oldValue | 1;
6: // CAS 设置 unwritable 为新值
7: if (UNWRITABLE_UPDATER.compareAndSet(this, oldValue, newValue)) {
8: // 若之前可写，现在不可写，触发 Channel WritabilityChanged 事件到 pipeline 中。
9: if (oldValue == 0 && newValue != 0) {
10: fireChannelWritabilityChanged(invokeLater);
11: }
12: break;
13: }
14: }
15: }
```

- 第 2 行：

for
循环，直到 CAS 修改成功

- 第 5 行：或位操作，修改第 0 位 bits 为 1 。😈 比较神奇的是，

unwritable
的类型不是

boolean
，而是

int
类型。通过每个 bits ，来表示**哪种**类型不可写。感兴趣的胖友，可以看看

io.netty.handler.traffic.AbstractTrafficShapingHandler
，使用了第 1、2、3 bits 。

- 第 7 行：CAS 设置

unwritable
为新值。

- 第 8 至 11 行：若之前可写，现在不可写，调用

/#fireChannelWritabilityChanged(boolean invokeLater)
方法，触发 Channel WritabilityChanged 事件到 pipeline 中。详细解析，见 [「10.3 fireChannelWritabilityChanged」]() 。

### []( "10.1.1 bytesBeforeUnwritable")10.1.1 bytesBeforeUnwritable

/#bytesBeforeUnwritable()
方法，获得距离**不可写**还有多少字节数。代码如下：

```
public long bytesBeforeUnwritable(){
long bytes = channel.config().getWriteBufferHighWaterMark() - totalPendingSize;
// If bytes is negative we know we are not writable, but if bytes is non-negative we have to check writability.
// Note that totalPendingSize and isWritable() use different volatile variables that are not synchronized
// together. totalPendingSize will be updated before isWritable().
if (bytes > 0) {
return isWritable() ? bytes : 0; // 判断 /#isWritable() 的原因是，可能已经被设置不可写
}
return 0;
}
```

- 基于**高水位**阀值来判断。

## []( "10.2 decrementPendingOutboundBytes")10.2 decrementPendingOutboundBytes

/#decrementPendingOutboundBytes(long size, ...)
方法，减少

totalPendingSize
计数。代码如下：

```
1: //*/*
2: /* Decrement the pending bytes which will be written at some point.
3: /* This method is thread-safe!
4: /*/
5: void decrementPendingOutboundBytes(long size){
6: decrementPendingOutboundBytes(size, true, true);
7: }
8:
9: private void decrementPendingOutboundBytes(long size, boolean invokeLater, boolean notifyWritability){
10: if (size == 0) {
11: return;
12: }
13:
14: // 减少 totalPendingSize 计数
15: long newWriteBufferSize = TOTAL_PENDING_SIZE_UPDATER.addAndGet(this, -size);
16: // totalPendingSize 小于低水位阀值时，设置为可写
17: if (notifyWritability && newWriteBufferSize < channel.config().getWriteBufferLowWaterMark()) {
18: setWritable(invokeLater);
19: }
20: }
```

- 第 15 行：减少

totalPendingSize
计数。

- 第 16 至 19 行：

totalPendingSize
小于低水位阀值时，调用

/#setWritable(boolean invokeLater)
方法，设置为可写。代码如下：

```
1: private void setWritable(boolean invokeLater){
2: for (;;) {
3: final int oldValue = unwritable;
4: // 并位操作，修改第 0 位 bits 为 0
5: final int newValue = oldValue & ~1;
6: // CAS 设置 unwritable 为新值
7: if (UNWRITABLE_UPDATER.compareAndSet(this, oldValue, newValue)) {
8: // 若之前不可写，现在可写，触发 Channel WritabilityChanged 事件到 pipeline 中。
9: if (oldValue != 0 && newValue == 0) {
10: fireChannelWritabilityChanged(invokeLater);
11: }
12: break;
13: }
14: }
15: }
```

- 第 2 行：

for
循环，直到 CAS 修改成功

- 第 5 行：并位操作，修改第 0 位 bits 为 0 。
- 第 7 行：CAS 设置

unwritable
为新值。

- 第 8 至 11 行：若之前可写，现在不可写，调用

/#fireChannelWritabilityChanged(boolean invokeLater)
方法，触发 Channel WritabilityChanged 事件到 pipeline 中。详细解析，见 [「10.3 fireChannelWritabilityChanged」]() 。

### []( "10.2.1 bytesBeforeWritable")10.2.1 bytesBeforeWritable

/#bytesBeforeWritable()
方法，获得距离**可写**还要多少字节数。代码如下：

```
//*/*
/* Get how many bytes must be drained from the underlying buffer until {@link /#isWritable()} returns {@code true}.
/* This quantity will always be non-negative. If {@link /#isWritable()} is {@code true} then 0.
/*/
public long bytesBeforeWritable(){
long bytes = totalPendingSize - channel.config().getWriteBufferLowWaterMark();
// If bytes is negative we know we are writable, but if bytes is non-negative we have to check writability.
// Note that totalPendingSize and isWritable() use different volatile variables that are not synchronized
// together. totalPendingSize will be updated before isWritable().
if (bytes > 0) {
return isWritable() ? 0 : bytes; // 判断 /#isWritable() 的原因是，可能已经被设置不可写
}
return 0;
}
```

- 基于**低水位**阀值来判断。

## []( "10.3 fireChannelWritabilityChanged")10.3 fireChannelWritabilityChanged

/#fireChannelWritabilityChanged(boolean invokeLater)
方法，触发 Channel WritabilityChanged 事件到 pipeline 中。代码如下：

```
private void fireChannelWritabilityChanged(boolean invokeLater){
final ChannelPipeline pipeline = channel.pipeline();
// 延迟执行，即提交 EventLoop 中触发 Channel WritabilityChanged 事件到 pipeline 中
if (invokeLater) {
Runnable task = fireChannelWritabilityChangedTask;
if (task == null) {
fireChannelWritabilityChangedTask = task = new Runnable() {
@Override
public void run(){
pipeline.fireChannelWritabilityChanged();
}
};
}
channel.eventLoop().execute(task);
// 直接触发 Channel WritabilityChanged 事件到 pipeline 中
} else {
pipeline.fireChannelWritabilityChanged();
}
}
```

- 根据

invokeLater
的值，分成两种方式，调用

ChannelPipeline/#fireChannelWritabilityChanged()
方法，触发 Channel WritabilityChanged 事件到 pipeline 中。具体，胖友看下代码注释。

- 后续的流程，就是 [《精尽 Netty 源码解析 —— ChannelPipeline（五）之 Inbound 事件的传播》](http://svip.iocoder.cn/Netty/Pipeline-5-inbound/) 。
- 通过 Channel WritabilityChanged 事件，配合

io.netty.handler.stream.ChunkedWriteHandler
处理器，实现 ChannelOutboundBuffer 写入的控制，避免 OOM 。ChunkedWriteHandler 的具体代码实现，我们在后续的文章，详细解析。

- 所以，有一点要注意，ChannelOutboundBuffer 的

unwritable
属性，仅仅作为一个是否不可写的**开关**，具体需要配合响应的 ChannelHandler 处理器，才能实现“不可写”的功能。

## []( "10.4 isWritable")10.4 isWritable

/#isWritable()
方法，是否可写。代码如下：

```
//*/*
/* Returns {@code true} if and only if {@linkplain /#totalPendingWriteBytes() the total number of pending bytes} did
/* not exceed the write watermark of the {@link Channel} and
/* no {@linkplain /#setUserDefinedWritability(int, boolean) user-defined writability flag} has been set to
/* {@code false}.
/*/
public boolean isWritable(){
return unwritable == 0;
}
```

- 如果

unwritable
大于 0 ，则表示不可写。😈 一定要注意！！！

### []( "10.4.1 getUserDefinedWritability")10.4.1 getUserDefinedWritability

/#getUserDefinedWritability(int index)
方法，获得指定 bits 是否可写。代码如下：

```
//*/*
/* Returns {@code true} if and only if the user-defined writability flag at the specified index is set to
/* {@code true}.
/*/
public boolean getUserDefinedWritability(int index){
return (unwritable & writabilityMask(index)) == 0;
}
private static int writabilityMask(int index){
// 不能 < 1 ，因为第 0 bits 为 ChannelOutboundBuffer 自己使用
// 不能 > 31 ，因为超过 int 的 bits 范围
if (index < 1 || index > 31) {
throw new IllegalArgumentException("index: " + index + " (expected: 1~31)");
}
return 1 << index;
}
```

- 为什么方法名字上会带有

"UserDefined"
呢？因为

index
不能使用 0 ，表示只允许使用用户定义(

"UserDefined"
) bits 位，即

[1, 31]
。

### []( "10.4.2 setUserDefinedWritability")10.4.2 setUserDefinedWritability

/#setUserDefinedWritability(int index, boolean writable)
方法，设置指定 bits 是否可写。代码如下：

```
//*/*
/* Sets a user-defined writability flag at the specified index.
/*/
public void setUserDefinedWritability(int index, boolean writable){
// 设置可写
if (writable) {
setUserDefinedWritability(index);
// 设置不可写
} else {
clearUserDefinedWritability(index);
}
}
private void setUserDefinedWritability(int index){
final int mask = ~writabilityMask(index);
for (;;) {
final int oldValue = unwritable;
final int newValue = oldValue & mask;
// CAS 设置 unwritable 为新值
if (UNWRITABLE_UPDATER.compareAndSet(this, oldValue, newValue)) {
// 若之前不可写，现在可写，触发 Channel WritabilityChanged 事件到 pipeline 中。
if (oldValue != 0 && newValue == 0) {
fireChannelWritabilityChanged(true);
}
break;
}
}
}
private void clearUserDefinedWritability(int index){
final int mask = writabilityMask(index);
for (;;) {
final int oldValue = unwritable;
final int newValue = oldValue | mask;
if (UNWRITABLE_UPDATER.compareAndSet(this, oldValue, newValue)) {
// 若之前可写，现在不可写，触发 Channel WritabilityChanged 事件到 pipeline 中。
if (oldValue == 0 && newValue != 0) {
fireChannelWritabilityChanged(true);
}
break;
}
}
}
```

- 代码比较简单，胖友自己看噢。

# []( "666. 彩蛋")666. 彩蛋

比想象中，长的多的多的一篇文章。总的来说，绝大部分细节，都已经扣到，美滋滋。如果有解释不够清晰或错误的细节，一起多多沟通呀。

写完这篇，我简直疯了。。。。

推荐阅读文章：

- 莫那一鲁道 [《Netty 出站缓冲区 ChannelOutboundBuffer 源码解析（isWritable 属性的重要性）》](https://www.jianshu.com/p/311425d1c72f)
- tomas 家的小拨浪鼓 [《Netty 源码解析 ——— writeAndFlush 流程分析》](https://www.jianshu.com/p/a3443cacd081)
- 闪电侠 [《netty 源码分析之 writeAndFlush 全解析》](https://www.jianshu.com/p/feaeaab2ce56)
- 占小狼 [《深入浅出 Netty write》](https://www.jianshu.com/p/1ad424c53e80)
- Hypercube [《自顶向下深入分析 Netty（六）–Channel 源码实现》](https://www.jianshu.com/p/9258af254e1d)
