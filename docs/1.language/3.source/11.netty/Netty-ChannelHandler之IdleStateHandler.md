# 精尽 Netty 源码解析 —— ChannelHandler（五）之 IdleStateHandler

# []( "1. 概述")1. 概述

在

netty-handler
模块的

timeout
包，实现 Channel 的读写操作的**空闲**检测。可能有胖友不太了解空闲检测的具体用途。请先研读理解下 [《简易 RPC 框架-心跳与重连机制》](https://www.cnblogs.com/ASPNET2008/p/7615973.html) 。

# []( "2. 类")2. 类

timeout
包，包含的类，如下图所示：[![`timeout` 包](http://static2.iocoder.cn/images/Netty/2018_10_13/01.png)](http://static2.iocoder.cn/images/Netty/2018_10_13/01.png '`timeout` 包')`timeout` 包

一共有 3 个 ChannelHandler 实现类：

- IdleStateHandler ，当 Channel 的**读或者写**空闲时间太长时，将会触发一个 IdleStateEvent 事件。然后，你可以自定义一个 ChannelInboundHandler ，重写

/#userEventTriggered(ChannelHandlerContext ctx, Object evt)
方法，处理该事件。

- ReadTimeoutHandler ，继承 IdleStateHandler 类，当 Channel 的**读**空闲时间( 读或者写 )太长时，抛出 ReadTimeoutException 异常，并自动关闭该 Channel 。然后，你可以自定一个 ChannelInboundHandler ，重写

/#exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
方法，处理该异常。

- WriteTimeoutHandler ，当一个**写**操作不能在指定时间内完成时，抛出 WriteTimeoutException 异常，并自动关闭对应 Channel 。然后，你可以自定一个 ChannelInboundHandler ，重写

/#exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
方法，处理该异常。

😈 从 WriteTimeoutHandler 可以看出，本文实际不仅仅分享 IdleStateHandler ，更准确的是分享 Timeout 相关的 ChannelHandler 。考虑到大多数胖友对 IdleStateHandler 比较熟悉，也相对常用，所以标题才取了 [《精尽 Netty 源码解析 —— ChannelHandler（五）之 IdleStateHandler》]() 。

# []( "3. IdleState")3. IdleState

io.netty.handler.timeout.IdleState
，空闲状态**枚举**。代码如下：

```
//*/*
/* 空闲状态枚举
/*
/* An {@link Enum} that represents the idle state of a {@link Channel}.
/*/
public enum IdleState {
//*/*
/* No data was received for a while.
/*
/* 读空闲
/*/
READER_IDLE,
//*/*
/* No data was sent for a while.
/*
/* 写空闲
/*/
WRITER_IDLE,
//*/*
/* No data was either received or sent for a while.
/*
/* 读或写任一空闲
/*/
ALL_IDLE
}
```

- 一共有 3 种状态。其中，

ALL_IDLE
表示的是，读**或**写任一空闲，注意是“或”。

## []( "3.1 IdleStateEvent")3.1 IdleStateEvent

io.netty.handler.timeout.IdleStateEvent
，空闲事件。代码如下：

```
public class IdleStateEvent{
// READ
public static final IdleStateEvent FIRST_READER_IDLE_STATE_EVENT = new IdleStateEvent(IdleState.READER_IDLE, true); // 首次
public static final IdleStateEvent READER_IDLE_STATE_EVENT = new IdleStateEvent(IdleState.READER_IDLE, false);
// WRITE
public static final IdleStateEvent FIRST_WRITER_IDLE_STATE_EVENT = new IdleStateEvent(IdleState.WRITER_IDLE, true); // 首次
public static final IdleStateEvent WRITER_IDLE_STATE_EVENT = new IdleStateEvent(IdleState.WRITER_IDLE, false);
// ALL
public static final IdleStateEvent FIRST_ALL_IDLE_STATE_EVENT = new IdleStateEvent(IdleState.ALL_IDLE, true); // 首次
public static final IdleStateEvent ALL_IDLE_STATE_EVENT = new IdleStateEvent(IdleState.ALL_IDLE, false);
//*/*
/* 空闲状态类型
/*/
private final IdleState state;
//*/*
/* 是否首次
/*/
private final boolean first;
//*/*
/* Constructor for sub-classes.
/*
/* @param state the {@link IdleStateEvent} which triggered the event.
/* @param first {@code true} if its the first idle event for the {@link IdleStateEvent}.
/*/
protected IdleStateEvent(IdleState state, boolean first){
this.state = ObjectUtil.checkNotNull(state, "state");
this.first = first;
}
//*/*
/* Returns the idle state.
/*/
public IdleState state(){
return state;
}
//*/*
/* Returns {@code true} if this was the first event for the {@link IdleState}
/*/
public boolean isFirst(){
return first;
}
}
```

- 3 **类**(

state
)空闲事件，再组合上是否首次(

first
)，一共有 6 种空闲事件。

# []( "4. TimeoutException")4. TimeoutException

io.netty.handler.timeout.TimeoutException
，继承 ChannelException 类，超时异常。代码如下：

```
public class TimeoutException extends ChannelException{
TimeoutException() { }
@Override
public Throwable fillInStackTrace(){
return this;
}
}
```

## []( "4.1 ReadTimeoutException")4.1 ReadTimeoutException

io.netty.handler.timeout.ReadTimeoutException
，继承 TimeoutException 类，读超时( 空闲 )异常。代码如下：

```
public final class ReadTimeoutException extends TimeoutException{
//*/*
/* 单例
/*/
public static final ReadTimeoutException INSTANCE = new ReadTimeoutException();
private ReadTimeoutException(){ }
}
```

## []( "4.2 WriteTimeoutException")4.2 WriteTimeoutException

io.netty.handler.timeout.WriteTimeoutException
，继承 TimeoutException 类，写超时( 空闲 )异常。代码如下：

```
public final class WriteTimeoutException extends TimeoutException{
//*/*
/* 单例
/*/
public static final WriteTimeoutException INSTANCE = new WriteTimeoutException();
private WriteTimeoutException(){ }
}
```

# []( "5. IdleStateHandler")5. IdleStateHandler

io.netty.handler.timeout.IdleStateHandler
，继承 ChannelDuplexHandler 类，当 Channel 的**读或者写**空闲时间太长时，将会触发一个 IdleStateEvent 事件。

## []( "5.1 构造方法")5.1 构造方法

老艿艿：高能预警，IdleStateHandler 的属性有点点多。

```
//*/*
/* 最小的超时时间，单位：纳秒
/*/
private static final long MIN_TIMEOUT_NANOS = TimeUnit.MILLISECONDS.toNanos(1);
//*/*
/* 写入任务监听器
/*/
// Not create a new ChannelFutureListener per write operation to reduce GC pressure.
private final ChannelFutureListener writeListener = new ChannelFutureListener() {
@Override
public void operationComplete(ChannelFuture future) throws Exception{
// 记录最后写时间
lastWriteTime = ticksInNanos();
// 重置 firstWriterIdleEvent 和 firstAllIdleEvent 为 true
firstWriterIdleEvent = firstAllIdleEvent = true;
}
};
//*/*
/* 是否观察 {@link ChannelOutboundBuffer} 写入队列
/*/
private final boolean observeOutput;
//*/*
/* 配置的读空闲时间，单位：纳秒
/*/
private final long readerIdleTimeNanos;
//*/*
/* 配置的写空闲时间，单位：纳秒
/*/
private final long writerIdleTimeNanos;
//*/*
/* 配置的All( 读或写任一 )，单位：纳秒
/*/
private final long allIdleTimeNanos;
//*/*
/* 读空闲的定时检测任务
/*/
private ScheduledFuture<?> readerIdleTimeout;
//*/*
/* 最后读时间
/*/
private long lastReadTime;
//*/*
/* 是否首次读空闲
/*/
private boolean firstReaderIdleEvent = true;
//*/*
/* 写空闲的定时检测任务
/*/
private ScheduledFuture<?> writerIdleTimeout;
//*/*
/* 最后写时间
/*/
private long lastWriteTime;
//*/*
/* 是否首次写空闲
/*/
private boolean firstWriterIdleEvent = true;
//*/*
/* All 空闲时间，单位：纳秒
/*/
private ScheduledFuture<?> allIdleTimeout;
//*/*
/* 是否首次 All 空闲
/*/
private boolean firstAllIdleEvent = true;
//*/*
/* 状态
/*
/* 0 - none ，未初始化
/* 1 - initialized ，已经初始化
/* 2 - destroyed ，已经销毁
/*/
private byte state; // 0 - none, 1 - initialized, 2 - destroyed
//*/*
/* 是否正在读取
/*/
private boolean reading;
//*/*
/* 最后检测到 {@link ChannelOutboundBuffer} 发生变化的时间
/*/
private long lastChangeCheckTimeStamp;
//*/*
/* 第一条准备 flash 到对端的消息( {@link ChannelOutboundBuffer/#current()} )的 HashCode
/*/
private int lastMessageHashCode;
//*/*
/* 总共等待 flush 到对端的内存大小( {@link ChannelOutboundBuffer/#totalPendingWriteBytes()} )
/*/
private long lastPendingWriteBytes;
public IdleStateHandler(int readerIdleTimeSeconds, int writerIdleTimeSeconds, int allIdleTimeSeconds){
this(readerIdleTimeSeconds, writerIdleTimeSeconds, allIdleTimeSeconds, TimeUnit.SECONDS);
}
public IdleStateHandler(long readerIdleTime, long writerIdleTime, long allIdleTime, TimeUnit unit){
this(false, readerIdleTime, writerIdleTime, allIdleTime, unit);
}
//*/*
/* Creates a new instance firing {@link IdleStateEvent}s.
/*
/* @param observeOutput
/* whether or not the consumption of {@code bytes} should be taken into
/* consideration when assessing write idleness. The default is {@code false}.
/* @param readerIdleTime
/* an {@link IdleStateEvent} whose state is {@link IdleState/#READER_IDLE}
/* will be triggered when no read was performed for the specified
/* period of time. Specify {@code 0} to disable.
/* @param writerIdleTime
/* an {@link IdleStateEvent} whose state is {@link IdleState/#WRITER_IDLE}
/* will be triggered when no write was performed for the specified
/* period of time. Specify {@code 0} to disable.
/* @param allIdleTime
/* an {@link IdleStateEvent} whose state is {@link IdleState/#ALL_IDLE}
/* will be triggered when neither read nor write was performed for
/* the specified period of time. Specify {@code 0} to disable.
/* @param unit
/* the {@link TimeUnit} of {@code readerIdleTime},
/* {@code writeIdleTime}, and {@code allIdleTime}
/*/
public IdleStateHandler(boolean observeOutput, long readerIdleTime, long writerIdleTime, long allIdleTime, TimeUnit unit){
if (unit == null) {
throw new NullPointerException("unit");
}
this.observeOutput = observeOutput;
if (readerIdleTime <= 0) {
readerIdleTimeNanos = 0;
} else {
readerIdleTimeNanos = Math.max(unit.toNanos(readerIdleTime), MIN_TIMEOUT_NANOS); // 保证大于等于 MIN_TIMEOUT_NANOS
}
if (writerIdleTime <= 0) {
writerIdleTimeNanos = 0;
} else {
writerIdleTimeNanos = Math.max(unit.toNanos(writerIdleTime), MIN_TIMEOUT_NANOS); // 保证大于等于 MIN_TIMEOUT_NANOS
}
if (allIdleTime <= 0) {
allIdleTimeNanos = 0;
} else {
allIdleTimeNanos = Math.max(unit.toNanos(allIdleTime), MIN_TIMEOUT_NANOS); // 保证大于等于 MIN_TIMEOUT_NANOS
}
}
```

- 属性比较多，保持耐心和淡定，我们继续来整理一波。
- MIN_TIMEOUT_NANOS
  静态属性，最小的超时时间为 **1** ，单位：纳秒。因为 IdleStateHandler 创建的，检测定时任务的时间，以纳秒为单位。
- state
  属性，IdleStateHandler 的状态。一共有三种，见注释。
- Read 空闲相关属性

- readerIdleTimeNanos
  属性，配置的读空闲时间，单位：纳秒。
- readerIdleTimeout
  属性，读空闲的定时检测任务。
- lastReadTime
  属性，读空闲的定时检测任务。
- firstReaderIdleEvent
  属性，是否首次读空闲。
- 【**独有**】

reading
属性，是否正在读取。

- Write 空闲相关属性

- writerIdleTimeNanos
  属性，配置的写空闲时间，单位：纳秒。
- writerIdleTimeout
  属性，写空闲的定时检测任务。
- lastWriteTime
  属性，最后写时间。
- writeListener
  属性，写入操作，完成 flush 到对端的回调监听器。初始时，创建好，避免重复创建，从而减轻 GC 压力。
- 【**独有**】ChannelOutboundBuffer 相关属性

- observeOutput
  属性， 是否观察 ChannelOutboundBuffer 写入队列。
- lastChangeCheckTimeStamp
  属性，最后检测到 ChannelOutboundBuffer 发生变化的时间。
- lastMessageHashCode
  属性，第一条准备 flash 到对端的消息的 HashCode 。
- lastPendingWriteBytes
  属性，总共等待 flush 到对端的内存大小。
- 关于这几个属性，跟着 [「5.7 hasOutputChanged」]() 一起理解。
- ALL 空闲相关属性

- 因为 ALL 是 Write 和 Read 任一，所以共用它们的一些属性。
- allIdleTimeNanos
  属性，配置的 All( 读或写任一 )，单位：纳秒。

## []( "5.2 initialize")5.2 initialize

/#initialize(ChannelHandlerContext ctx)
方法，初始化 IdleStateHandler 。代码如下：

```
1: private void initialize(ChannelHandlerContext ctx){
2: // 校验状态，避免因为 `/#destroy()` 方法在 `/#initialize(ChannelHandlerContext ctx)` 方法，执行之前
3: // Avoid the case where destroy() is called before scheduling timeouts.
4: // See: https://github.com/netty/netty/issues/143
5: switch (state) {
6: case 1:
7: case 2:
8: return;
9: }
10:
11: // 标记为已初始化
12: state = 1;
13: // 初始化 ChannelOutboundBuffer 相关属性
14: initOutputChanged(ctx);
15:
16: // 初始相应的定时任务
17: lastReadTime = lastWriteTime = ticksInNanos();
18: if (readerIdleTimeNanos > 0) {
19: readerIdleTimeout = schedule(ctx, new ReaderIdleTimeoutTask(ctx), readerIdleTimeNanos, TimeUnit.NANOSECONDS);
20: }
21: if (writerIdleTimeNanos > 0) {
22: writerIdleTimeout = schedule(ctx, new WriterIdleTimeoutTask(ctx), writerIdleTimeNanos, TimeUnit.NANOSECONDS);
23: }
24: if (allIdleTimeNanos > 0) {
25: allIdleTimeout = schedule(ctx, new AllIdleTimeoutTask(ctx), allIdleTimeNanos, TimeUnit.NANOSECONDS);
26: }
27: }
```

- 第 2 至 9 行：校验状态，避免因为

/#destroy()
方法在

/#initialize(ChannelHandlerContext ctx)
方法，执行之前。

- 第 12 行：标记

state
为已初始化。

- 第 14 行：调用

/#initOutputChanged(ChannelHandlerContext ctx)
方法，初始化 ChannelOutboundBuffer 相关属性。代码如下：

```
private void initOutputChanged(ChannelHandlerContext ctx){
if (observeOutput) {
Channel channel = ctx.channel();
Unsafe unsafe = channel.unsafe();
ChannelOutboundBuffer buf = unsafe.outboundBuffer();
if (buf != null) {
// 记录第一条准备 flash 到对端的消息的 HashCode
lastMessageHashCode = System.identityHashCode(buf.current());
// 记录总共等待 flush 到对端的内存大小
lastPendingWriteBytes = buf.totalPendingWriteBytes();
}
}
}
```

- 初始化

lastMessageHashCode
和

lastPendingWriteBytes
属性。

- 第 17 至 26 行：根据配置，分别调用

/#schedule(hannelHandlerContext ctx, Runnable task, long delay, TimeUnit unit)
方法，初始相应的定时任务。代码如下：

```
ScheduledFuture<?> schedule(ChannelHandlerContext ctx, Runnable task, long delay, TimeUnit unit) {
return ctx.executor().schedule(task, delay, unit);
}
```

- 一共有 ReaderIdleTimeoutTask、WriterIdleTimeoutTask、AllIdleTimeoutTask 三种任务，下文我们详细解析。

该方法，会在多个 Channel **事件**中被调用。代码如下：

```
// <2>
@Override
public void handlerAdded(ChannelHandlerContext ctx) throws Exception{
if (ctx.channel().isActive() && ctx.channel().isRegistered()) {
// 初始化
// channelActive() event has been fired already, which means this.channelActive() will
// not be invoked. We have to initialize here instead.
initialize(ctx);
} else {
// channelActive() event has not been fired yet. this.channelActive() will be invoked
// and initialization will occur there.
}
}
// <3>
@Override
public void channelRegistered(ChannelHandlerContext ctx) throws Exception{
// 初始化
// Initialize early if channel is active already.
if (ctx.channel().isActive()) {
initialize(ctx);
}
// 继续传播 Channel Registered 事件到下一个节点
super.channelRegistered(ctx);
}
// <1>
@Override
public void channelActive(ChannelHandlerContext ctx) throws Exception{
// 初始化
// This method will be invoked only if this handler was added
// before channelActive() event is fired. If a user adds this handler
// after the channelActive() event, initialize() will be called by beforeAdd().
initialize(ctx);
// 继续传播 Channel Registered 事件到下一个节点
super.channelActive(ctx);
}
```

- <1>
  ：当客户端与服务端成功建立连接后，Channel 被激活，此时 channelActive 方法，的初始化被调用。
- <2>
  ：当 Channel 被激活后，动态添加此 Handler ，则 handlerAdded 方法的初始化被调用。
- <3>
  ：当 Channel 被激活后，用户主动切换 Channel 的所在的 EventLoop ，则 channelRegistered 方法的初始化被调用。

## []( "5.3 destroy")5.3 destroy

/#destroy()
方法，销毁 IdleStateHandler 。代码如下：

```
private void destroy(){
// 标记为销毁
state = 2;
// 销毁相应的定时任务
if (readerIdleTimeout != null) {
readerIdleTimeout.cancel(false);
readerIdleTimeout = null;
}
if (writerIdleTimeout != null) {
writerIdleTimeout.cancel(false);
writerIdleTimeout = null;
}
if (allIdleTimeout != null) {
allIdleTimeout.cancel(false);
allIdleTimeout = null;
}
}
```

- 标记

state
为已销毁。

- 销毁响应的定时任务。

该方法，会在多个 Channel **事件**中被调用。代码如下：

```
@Override
public void handlerRemoved(ChannelHandlerContext ctx) throws Exception{
// 销毁
destroy();
}
@Override
public void channelInactive(ChannelHandlerContext ctx) throws Exception{
// 销毁
destroy();
// 继续传播 Channel Incative 事件到下一个节点
super.channelInactive(ctx);
}
```

## []( "5.4 channelIdle")5.4 channelIdle

在定时任务中，如果检测到**空闲**：

① 首先，调用

/#newIdleStateEvent(IdleState state, boolean first)
方法，创建对应的空闲事件。代码如下：

```
protected IdleStateEvent newIdleStateEvent(IdleState state, boolean first){
switch (state) {
case ALL_IDLE:
return first ? IdleStateEvent.FIRST_ALL_IDLE_STATE_EVENT : IdleStateEvent.ALL_IDLE_STATE_EVENT;
case READER_IDLE:
return first ? IdleStateEvent.FIRST_READER_IDLE_STATE_EVENT : IdleStateEvent.READER_IDLE_STATE_EVENT;
case WRITER_IDLE:
return first ? IdleStateEvent.FIRST_WRITER_IDLE_STATE_EVENT : IdleStateEvent.WRITER_IDLE_STATE_EVENT;
default:
throw new IllegalArgumentException("Unhandled: state=" + state + ", first=" + first);
}
}
```

② 然后，调用

/#channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt)
方法，在 pipeline 中，触发 UserEvent 事件。代码如下：

```
//*/*
/* Is called when an {@link IdleStateEvent} should be fired. This implementation calls
/* {@link ChannelHandlerContext/#fireUserEventTriggered(Object)}.
/*/
protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception{
ctx.fireUserEventTriggered(evt);
}
```

## []( "5.5 channelRead")5.5 channelRead

/#channelRead(ChannelHandlerContext ctx, Object msg)
方法，代码如下：

```
@Override
public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception{
// 开启了 read 或 all 的空闲检测
if (readerIdleTimeNanos > 0 || allIdleTimeNanos > 0) {
// 标记正在读取
reading = true;
// 重置 firstWriterIdleEvent 和 firstAllIdleEvent 为 true
firstReaderIdleEvent = firstAllIdleEvent = true;
}
// 继续传播 Channel Read 事件到下一个节点
ctx.fireChannelRead(msg);
}
```

在开启 read 或 all 的空闲检测的情况下，在【继续传播 Channel Read 事件到下一个节点】**之前**，会：

- 标记

reading
为正在读取。

- 重置

firstWriterIdleEvent
和

firstAllIdleEvent
为

true
，即又变成**首次**。

那么什么时候记录

lastReadTime
最后读取时间呢？答案在

/#channelReadComplete(ChannelHandlerContext ctx)
方法中。代码如下：

```
@Override
public void channelReadComplete(ChannelHandlerContext ctx) throws Exception{
// 开启了 read 或 all 的空闲检测
if ((readerIdleTimeNanos > 0 || allIdleTimeNanos > 0) && reading) {
// 记录最后读时间
lastReadTime = ticksInNanos();
// 标记不在读取
reading = false;
}
// 继续传播 Channel ReadComplete 事件到下一个节点
ctx.fireChannelReadComplete();
}
```

在开启 read 或 all 的空闲检测的情况下，在【继续传播 Channel ReadComplete 事件到下一个节点】**之前**，会：

- 记录

lastReadTime
最后读取时间为

/#ticksInNanos()
方法，代码如下：

```
long ticksInNanos(){
return System.nanoTime();
}
```

- 当前时间，单位：纳秒。
- 标记

reading
为不在读取。

## []( "5.6 write")5.6 write

/#write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
方法，代码如下：

```
@Override
public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception{
// 开启了 write 或 all 的空闲检测
// Allow writing with void promise if handler is only configured for read timeout events.
if (writerIdleTimeNanos > 0 || allIdleTimeNanos > 0) {
// 写入，并添加写入监听器
ctx.write(msg, promise.unvoid()).addListener(writeListener);
} else {
// 写入，不添加监听器
ctx.write(msg, promise);
}
}
```

在开启 write 或 all 的空闲检测的情况下，写入的时候，会添加写入监听器

writeListener
。该监听器会在消息( 数据 ) flush 到对端后，**回调**，修改最后写入时间

lastWriteTime
为

/#ticksInNanos()
。代码如下：

```
// Not create a new ChannelFutureListener per write operation to reduce GC pressure.
private final ChannelFutureListener writeListener = new ChannelFutureListener() {
@Override
public void operationComplete(ChannelFuture future) throws Exception{
// 记录最后写时间
lastWriteTime = ticksInNanos();
// 重置 firstWriterIdleEvent 和 firstAllIdleEvent 为 true
firstWriterIdleEvent = firstAllIdleEvent = true;
}
}
```

## []( "5.7 hasOutputChanged")5.7 hasOutputChanged

老艿艿：关于这个方法，看完 [「5.8.2 WriterIdelTimeoutTask」]() 后，再回过头理解。

/#hasOutputChanged(ChannelHandlerContext ctx, boolean first)
方法，判断 ChannelOutboundBuffer 是否发生变化。代码如下：

```
//*/*
/* Returns {@code true} if and only if the {@link IdleStateHandler} was constructed
/* with {@link /#observeOutput} enabled and there has been an observed change in the
/* {@link ChannelOutboundBuffer} between two consecutive calls of this method.
/*
/* https://github.com/netty/netty/issues/6150
/*/
1: private boolean hasOutputChanged(ChannelHandlerContext ctx, boolean first){
2: // 开启观察 ChannelOutboundBuffer 队列
3: if (observeOutput) {
4:
5: // We can take this shortcut if the ChannelPromises that got passed into write()
6: // appear to complete. It indicates "change" on message level and we simply assume
7: // that there's change happening on byte level. If the user doesn't observe channel
8: // writability events then they'll eventually OOME and there's clearly a different
9: // problem and idleness is least of their concerns.
10: // 如果 lastChangeCheckTimeStamp 和 lastWriteTime 不一样，说明写操作进行过了，则更新此值
11: if (lastChangeCheckTimeStamp != lastWriteTime) {
12: lastChangeCheckTimeStamp = lastWriteTime;
13:
14: // But this applies only if it's the non-first call.
15: if (!first) { // 非首次
16: return true;
17: }
18: }
19:
20: Channel channel = ctx.channel();
21: Unsafe unsafe = channel.unsafe();
22: ChannelOutboundBuffer buf = unsafe.outboundBuffer();
23:
24: if (buf != null) {
25: // 获得新的 messageHashCode 和 pendingWriteBytes
26: int messageHashCode = System.identityHashCode(buf.current());
27: long pendingWriteBytes = buf.totalPendingWriteBytes();
28:
29: // 发生了变化
30: if (messageHashCode != lastMessageHashCode || pendingWriteBytes != lastPendingWriteBytes) {
31: // 修改最后一次的 lastMessageHashCode 和 lastPendingWriteBytes
32: lastMessageHashCode = messageHashCode;
33: lastPendingWriteBytes = pendingWriteBytes;
34:
35: if (!first) { // 非首次
36: return true;
37: }
38: }
39: }
40: }
41:
42: return false;
43: }
```

- 第 3 行：判断开启观察 ChannelOutboundBuffer 队列。

- 如果

lastChangeCheckTimeStamp
和

lastWriteTime
不一样，说明写操作进行过了，则更新此值。

- 第 14 至 17 行：这段逻辑，理论来说不会发生。因为

lastWriteTime
属性，只会在

writeListener
回调中修改，那么如果发生

lastChangeCheckTimeStamp
和

lastWriteTime
不相等，

first
必然为

true
。因为，Channel 相关的事件逻辑，都在它所在的 EventLoop 中，不会出现并发的情况。关于这一块，基友【莫那一鲁道】在 [https://github.com/netty/netty/issues/8251](https://github.com/netty/netty/issues/8251) 已经进行提问，坐等结果。

- 第 25 至 27 行：获得新的

messageHashCode
和

pendingWriteBytes
的。

- 第 29 至 33 行：若发生了变化，则修改最后一次的

lastMessageHashCode
和

lastPendingWriteBytes
。

- messageHashCode != lastMessageHashCode
  成立，① 有可能对端接收数据比较慢，导致一个消息发送了一部分；② 又或者，发送的消息**非常非常非常大**，导致一个消息发送了一部分，就将发送缓存区写满。如果是这种情况下，可以使用 ChunkedWriteHandler ，一条大消息，拆成多条小消息。
- pendingWriteBytes != lastPendingWriteBytes
  成立，① 有新的消息，写到 ChannelOutboundBuffer 内存队列中；② 有几条消息成功写到对端。这种情况，此处不会发生。
- 第 35 至 37 行：当且仅当

first
为

true
时，即非首次，才返回

true
，表示 ChannelOutboundBuffer 发生变化。

- 这是一个有点“神奇”的设定，笔者表示不太理解。理论来说，ChannelOutboundBuffer 是否发生变化，只需要考虑【第 30 行】代码的判断。如果加了

!first
的判断，导致的结果是在 WriterIdleTimeoutTask 和 AllIdleTimeoutTask 任务中，ChannelOutboundBuffer 即使发生了变化，在**首次**还是会触发 write 和 all 空闲事件，在**非首次**不会触发 write 和 all 空闲事件。

- 关于上述的困惑，[《Netty 那些事儿 ——— 关于 “Netty 发送大数据包时 触发写空闲超时” 的一些思考》](https://www.jianshu.com/p/8fe70d313d78) 一文的作者，也表达了相同的困惑。后续，找闪电侠面基沟通下。
- 关于上述的困惑，[《Netty 心跳服务之 IdleStateHandler 源码分析》](https://www.jianshu.com/p/f2ed73cf4df8) 一文的作者，表达了自己的理解。感兴趣的胖友，可以看看。
- 当然，这块如果不理解的胖友，也不要方。从笔者目前了解下来，

observeOutput
都是设置为

false
。也就说，不会触发这个方法的执行。

- 第 42 行：返回

false
，表示 ChannelOutboundBuffer 未发生变化。

## []( "5.8 AbstractIdleTask")5.8 AbstractIdleTask

AbstractIdleTask ，实现 Runnable 接口，空闲任务抽象类。代码如下：
AbstractIdleTask 是 IdleStateHandler 的内部静态类。

```
private abstract static class AbstractIdleTask implements Runnable{
private final ChannelHandlerContext ctx;
AbstractIdleTask(ChannelHandlerContext ctx) {
this.ctx = ctx;
}
@Override
public void run(){
// <1> 忽略未打开的 Channel
if (!ctx.channel().isOpen()) {
return;
}
// <2> 执行任务
run(ctx);
}
protected abstract void run(ChannelHandlerContext ctx);
}
```

- <1>
  处，忽略未打开的 Channel 。
- <2>
  处，子类实现

/#run()
**抽象**方法，实现自定义的空闲检测逻辑。

### []( "5.8.1 ReaderIdleTimeoutTask")5.8.1 ReaderIdleTimeoutTask

ReaderIdleTimeoutTask ，继承 AbstractIdleTask 抽象类，检测 Read 空闲超时**定时**任务。代码如下：
ReaderIdleTimeoutTask 是 IdleStateHandler 的内部静态类。

```
1: private final class ReaderIdleTimeoutTask extends AbstractIdleTask{
2:
3: ReaderIdleTimeoutTask(ChannelHandlerContext ctx) {
4: super(ctx);
5: }
6:
7: @Override
8: protected void run(ChannelHandlerContext ctx){
9: // 计算下一次检测的定时任务的延迟
10: long nextDelay = readerIdleTimeNanos;
11: if (!reading) {
12: nextDelay -= ticksInNanos() - lastReadTime;
13: }
14:
15: // 如果小于等于 0 ，说明检测到读空闲
16: if (nextDelay <= 0) {
17: // 延迟时间为 readerIdleTimeNanos ，即再次检测
18: // Reader is idle - set a new timeout and notify the callback.
19: readerIdleTimeout = schedule(ctx, this, readerIdleTimeNanos, TimeUnit.NANOSECONDS);
20:
21: // 获得当前是否首次检测到读空闲
22: boolean first = firstReaderIdleEvent;
23: // 标记 firstReaderIdleEvent 为 false 。也就说，下次检测到空闲，就非首次了。
24: firstReaderIdleEvent = false;
25:
26: try {
27: // 创建读空闲事件
28: IdleStateEvent event = newIdleStateEvent(IdleState.READER_IDLE, first);
29: // 通知通道空闲事件
30: channelIdle(ctx, event);
31: } catch (Throwable t) {
32: // 触发 Exception Caught 到下一个节点
33: ctx.fireExceptionCaught(t);
34: }
35: // 如果大于 0 ，说明未检测到读空闲
36: } else {
37: // 延迟时间为 nextDelay ，即按照最后一次读的时间作为开始计数
38: // Read occurred before the timeout - set a new timeout with shorter delay.
39: readerIdleTimeout = schedule(ctx, this, nextDelay, TimeUnit.NANOSECONDS);
40: }
41: }
42: }
```

- 第 9 至 13 行：计算下一次检测的定时任务的**延迟**。

- reading
  为

true
时，意味着正在读取，**不会**被检测为读空闲。

- reading
  为

false
时，实际

nextDelay
的计算为

readerIdleTimeNanos - (ticksInNanos() - lastReadTime)
。如果小于等于 0 ，意味着

ticksInNanos() - lastReadTime >= readerIdleTimeNanos
，超时。

- ① 第 35 至 40 行：如果**大于** 0 ，说明未检测到读空闲。

- 第 39 行：调用

/#schedule(ChannelHandlerContext ctx, Runnable task, long delay, TimeUnit unit)
方法，初始**下一次**的 ReaderIdleTimeoutTask 定时任务。其中，延迟时间为

nextDelay
，即按照最后一次读的时间作为开始计数。

- ② 第 15 至 34 行：如果**小于等于** 0 ，说明检测到读空闲。

- 第 19 行：调用

/#schedule(ChannelHandlerContext ctx, Runnable task, long delay, TimeUnit unit)
方法，初始**下一次**的 ReaderIdleTimeoutTask 定时任务。其中，延迟时间为

readerIdleTimeNanos
，即重新计数。

- 第 21 行：获得当前是否首次检测到读空闲。

- 第 24 行：标记

firstReaderIdleEvent
为

false
。也就说，下次检测到空闲，就**非首次**了。

- 第 28 行：调用

/#newIdleStateEvent(IdleState state, boolean first)
方法，创建创建**读**空闲事件。

- 第 30 行： 调用

/#channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt)
方法，在 pipeline 中，触发 UserEvent 事件。

- 第 31 至 34 行：如果**发生异常**，触发 Exception Caught 事件到下一个节点，处理异常。

### []( "5.8.2 WriterIdleTimeoutTask")5.8.2 WriterIdleTimeoutTask

WriterIdleTimeoutTask ，继承 AbstractIdleTask 抽象类，检测 Write 空闲超时**定时**任务。代码如下：
WriterIdleTimeoutTask 是 IdleStateHandler 的内部静态类。

```
1: private final class WriterIdleTimeoutTask extends AbstractIdleTask{
2:
3: WriterIdleTimeoutTask(ChannelHandlerContext ctx) {
4: super(ctx);
5: }
6:
7: @Override
8: protected void run(ChannelHandlerContext ctx){
9: // 计算下一次检测的定时任务的延迟
10: long lastWriteTime = IdleStateHandler.this.lastWriteTime;
11: long nextDelay = writerIdleTimeNanos - (ticksInNanos() - lastWriteTime);
12:
13: // 如果小于等于 0 ，说明检测到写空闲
14: if (nextDelay <= 0) {
15: // 延迟时间为 writerIdleTimeout ，即再次检测
16: // Writer is idle - set a new timeout and notify the callback.
17: writerIdleTimeout = schedule(ctx, this, writerIdleTimeNanos, TimeUnit.NANOSECONDS);
18:
19: // 获得当前是否首次检测到写空闲
20: boolean first = firstWriterIdleEvent;
21: // 标记 firstWriterIdleEvent 为 false 。也就说，下次检测到空闲，就非首次了。
22: firstWriterIdleEvent = false;
23:
24: try {
25: // 判断 ChannelOutboundBuffer 是否发生变化
26: if (hasOutputChanged(ctx, first)) {
27: return;
28: }
29:
30: // 创建写空闲事件
31: IdleStateEvent event = newIdleStateEvent(IdleState.WRITER_IDLE, first);
32: // 通知通道空闲事件
33: channelIdle(ctx, event);
34: } catch (Throwable t) {
35: // 触发 Exception Caught 到下一个节点
36: ctx.fireExceptionCaught(t);
37: }
38: // 如果大于 0 ，说明未检测到读空闲
39: } else {
40: // Write occurred before the timeout - set a new timeout with shorter delay.
41: writerIdleTimeout = schedule(ctx, this, nextDelay, TimeUnit.NANOSECONDS);
42: }
43: }
44: }
```

- 第 9 至 11 行：计算下一次检测的定时任务的**延迟**。
- ① 第 38 至 42 行：如果**大于** 0 ，说明未检测到写空闲。

- 第 39 行：调用

/#schedule(ChannelHandlerContext ctx, Runnable task, long delay, TimeUnit unit)
方法，初始**下一次**的 WriterIdleTimeoutTask 定时任务。其中，延迟时间为

nextDelay
，即按照最后一次写的时间作为开始计数。

- ② 第 13 至 37 行：如果**小于等于** 0 ，说明检测到写空闲。

- 第 17 行：调用

/#schedule(ChannelHandlerContext ctx, Runnable task, long delay, TimeUnit unit)
方法，初始**下一次**的 WriterIdleTimeoutTask 定时任务。其中，延迟时间为

readerIdleTimeNanos
，即重新计数。

- 第 20 行：获得当前是否首次检测到写空闲。

- 第 22 行：标记

firstWriterIdleEvent
为

false
。也就说，下次检测到空闲，就**非首次**了。

- 第 25 至 28 行：判断 ChannelOutboundBuffer 是否发生变化。如果有变化，不触发写空闲时间。
- 第 31 行：调用

/#newIdleStateEvent(IdleState state, boolean first)
方法，创建创建**写**空闲事件。

- 第 33 行： 调用

/#channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt)
方法，在 pipeline 中，触发 UserEvent 事件。

- 第 34 至 37 行：如果**发生异常**，触发 Exception Caught 事件到下一个节点，处理异常。

### []( "5.8.3 AllIdleTimeoutTask")5.8.3 AllIdleTimeoutTask

AllIdleTimeoutTask ，继承 AbstractIdleTask 抽象类，检测 All 空闲超时**定时**任务。代码如下：
AllIdleTimeoutTask 是 IdleStateHandler 的内部静态类。

```
private final class AllIdleTimeoutTask extends AbstractIdleTask{
AllIdleTimeoutTask(ChannelHandlerContext ctx) {
super(ctx);
}
@Override
protected void run(ChannelHandlerContext ctx){
// 计算下一次检测的定时任务的延迟
long nextDelay = allIdleTimeNanos;
if (!reading) {
nextDelay -= ticksInNanos() - Math.max(lastReadTime, lastWriteTime); // <1> 取大值
}
// 如果小于等于 0 ，说明检测到 all 空闲
if (nextDelay <= 0) {
// 延迟时间为 allIdleTimeNanos ，即再次检测
// Both reader and writer are idle - set a new timeout and
// notify the callback.
allIdleTimeout = schedule(ctx, this, allIdleTimeNanos, TimeUnit.NANOSECONDS);
// 获得当前是否首次检测到 all 空闲
boolean first = firstAllIdleEvent;
// 标记 firstAllIdleEvent 为 false 。也就说，下次检测到空闲，就非首次了。
firstAllIdleEvent = false;
try {
// 判断 ChannelOutboundBuffer 是否发生变化
if (hasOutputChanged(ctx, first)) {
return;
}
// 创建 all 空闲事件
IdleStateEvent event = newIdleStateEvent(IdleState.ALL_IDLE, first);
// 通知通道空闲事件
channelIdle(ctx, event);
} catch (Throwable t) {
ctx.fireExceptionCaught(t);
}
// 如果大于 0 ，说明未检测到 all 空闲
} else {
// Either read or write occurred before the timeout - set a new
// timeout with shorter delay.
allIdleTimeout = schedule(ctx, this, nextDelay, TimeUnit.NANOSECONDS);
}
}
}
```

- 因为 All 是 Write 和 Read **任一**一种空闲即可，所以 AllIdleTimeoutTask 是 ReaderIdleTimeoutTask 和 WriterIdleTimeoutTask 的**综合**。
- <1>
  处，取

lastReadTime
和

lastWriteTime
中的**大**值，从而来判断，是否有 Write 和 Read **任一**一种空闲。

- WriterIdleTimeoutTask 就不详细解析，胖友自己读读代码即可。

# []( "6. ReadTimeoutHandler")6. ReadTimeoutHandler

io.netty.handler.timeout.ReadTimeoutHandler
，继承 IdleStateHandler 类，当 Channel 的**读**空闲时间( 读或者写 )太长时，抛出 ReadTimeoutException 异常，并自动关闭该 Channel 。

## []( "6.1 构造方法")6.1 构造方法

```
//*/*
/* Channel 是否关闭
/*/
private boolean closed;
public ReadTimeoutHandler(int timeoutSeconds){
this(timeoutSeconds, TimeUnit.SECONDS);
}
public ReadTimeoutHandler(long timeout, TimeUnit unit){
// 禁用 Write / All 的空闲检测
super(timeout, 0, 0, unit); // <1>
}
```

- closed
  属性，Channel 是否关闭。
- <1>
  处，禁用 Write / All 的空闲检测，只根据

timeout
方法参数，开启 Read 的空闲检测。

## []( "6.2 channelIdle")6.2 channelIdle

/#channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt)
方法，覆写父类方法，代码如下：

```
@Override
protected final void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception{
assert evt.state() == IdleState.READER_IDLE;
readTimedOut(ctx);
}
//*/*
/* Is called when a read timeout was detected.
/*/
protected void readTimedOut(ChannelHandlerContext ctx) throws Exception{
if (!closed) {
// <1> 触发 Exception Caught 事件到 pipeline 中，异常为 ReadTimeoutException
ctx.fireExceptionCaught(ReadTimeoutException.INSTANCE);
// <2> 关闭 Channel 通道
ctx.close();
// <3> 标记 Channel 为已关闭
closed = true;
}
}
```

- <1>
  处，触发 Exception Caught 事件到 pipeline 中，异常为 ReadTimeoutException 。
- <2>
  处，关闭 Channel 通道。
- <3>
  处，标记 Channel 为已关闭。

# []( "7. WriteTimeoutHandler")7. WriteTimeoutHandler

io.netty.handler.timeout.WriteTimeoutHandler
，继承 ChannelOutboundHandlerAdapter 类，当一个**写**操作不能在指定时间内完成时，抛出 WriteTimeoutException 异常，并自动关闭对应 Channel 。

😈 **注意，这里写入，指的是 flush 到对端 Channel ，而不仅仅是写到 ChannelOutboundBuffer 队列**。

## []( "7.1 构造方法")7.1 构造方法

```
//*/*
/* 最小的超时时间，单位：纳秒
/*/
private static final long MIN_TIMEOUT_NANOS = TimeUnit.MILLISECONDS.toNanos(1);
//*/*
/* 超时时间，单位：纳秒
/*/
private final long timeoutNanos;
//*/*
/* WriteTimeoutTask 双向链表。
/*
/* lastTask 为链表的尾节点
/*
/* A doubly-linked list to track all WriteTimeoutTasks
/*/
private WriteTimeoutTask lastTask;
//*/*
/* Channel 是否关闭
/*/
private boolean closed;
public WriteTimeoutHandler(int timeoutSeconds){
this(timeoutSeconds, TimeUnit.SECONDS);
}
public WriteTimeoutHandler(long timeout, TimeUnit unit){
if (unit == null) {
throw new NullPointerException("unit");
}
if (timeout <= 0) {
timeoutNanos = 0;
} else {
timeoutNanos = Math.max(unit.toNanos(timeout), MIN_TIMEOUT_NANOS); // 保证大于等于 MIN_TIMEOUT_NANOS
}
}
```

- timeoutNanos
  属性，写入超时时间，单位：纳秒。

- MIN_TIMEOUT_NANOS
  属性，最小的超时时间，单位：纳秒。
- lastTask
  属性，WriteTimeoutTask 双向链表。其中，

lastTask
为链表的**尾节点**。

- closed
  属性，Channel 是否关闭。

## []( "7.2 handlerRemoved")7.2 handlerRemoved

/#handlerRemoved(ChannelHandlerContext ctx)
方法，移除所有 WriteTimeoutTask 任务，并取消。代码如下：

```
@Override
public void handlerRemoved(ChannelHandlerContext ctx) throws Exception{
WriteTimeoutTask task = lastTask;
// 置空 lastTask
lastTask = null;
// 循环移除，知道为空
while (task != null) {
// 取消当前任务的定时任务
task.scheduledFuture.cancel(false);
// 记录前一个任务
WriteTimeoutTask prev = task.prev;
// 置空当前任务的前后节点
task.prev = null;
task.next = null;
// 跳到前一个任务
task = prev;
}
}
```

- 代码比较简单，胖友自己看注释。

## []( "7.3 write")7.3 write

/#write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
方法，代码如下：

```
@Override
public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception{
if (timeoutNanos > 0) {
// 如果 promise 是 VoidPromise ，则包装成非 VoidPromise ，为了后续的回调。
promise = promise.unvoid(); <1》
// 创建定时任务
scheduleTimeout(ctx, promise);
}
// 写入
ctx.write(msg, promise);
}
```

- <1>
  处，如果

promise
类型是 VoidPromise ，则包装成非 VoidPromise ，为了后续的回调。因为 VoidPromise 无法接收到回调。

- <2>
  处，调用

/#scheduleTimeout(final ChannelHandlerContext ctx, final ChannelPromise promise)
方法，创建定时任务。详细解析，见 [「7.4 scheduleTimeout」]() 。

## []( "7.4 scheduleTimeout")7.4 scheduleTimeout

/#scheduleTimeout(final ChannelHandlerContext ctx, final ChannelPromise promise)
方法，创建定时任务。代码如下：

```
1: private void scheduleTimeout(final ChannelHandlerContext ctx, final ChannelPromise promise){
2: // Schedule a timeout.
3: // 创建 WriteTimeoutTask 任务
4: final WriteTimeoutTask task = new WriteTimeoutTask(ctx, promise);
5: // 定时任务
6: task.scheduledFuture = ctx.executor().schedule(task, timeoutNanos, TimeUnit.NANOSECONDS);
7:
8: if (!task.scheduledFuture.isDone()) {
9: // 添加到链表
10: addWriteTimeoutTask(task);
11:
12: // Cancel the scheduled timeout if the flush promise is complete.
13: // 将 task 作为监听器，添加到 promise 中。在写入完成后，可以移除该定时任务
14: promise.addListener(task);
15: }
16: }
```

- 第 2 至 6 行：创建 WriteTimeoutTask 任务，并发起**定时任务**。
- 第 8 行：如果定时任务**已经执行完成**，则不需要进行监听。否则，需要执行【第 10 至 14 行】的代码逻辑。
- 第 10 行：调用

/#addWriteTimeoutTask(WriteTimeoutTask task)
方法，添加到链表。详细解析，见 [「7.5 addWriteTimeoutTask」]() 。

- 第 14 行：将

task
作为监听器，添加到

promise
中。在写入完成后，可以移除该定时任务。也就说，调用链是

flush => 回调 => promise => 回调 => task
。

## []( "7.5 addWriteTimeoutTask")7.5 addWriteTimeoutTask

/#addWriteTimeoutTask(WriteTimeoutTask task)
方法，添加到链表。代码如下：

```
private void addWriteTimeoutTask(WriteTimeoutTask task){
// 添加到链表的尾节点
if (lastTask != null) {
lastTask.next = task;
task.prev = lastTask;
}
// 修改 lastTask 为当前任务
lastTask = task;
}
```

添加到链表的尾节点，并修改

lastTask
为**当前**任务。

## []( "7.6 removeWriteTimeoutTask")7.6 removeWriteTimeoutTask

/#removeWriteTimeoutTask(WriteTimeoutTask task)
方法，移除出链表。代码如下：

```
private void removeWriteTimeoutTask(WriteTimeoutTask task){
// 从双向链表中，移除自己
if (task == lastTask) { // 尾节点
// task is the tail of list
assert task.next == null;
lastTask = lastTask.prev;
if (lastTask != null) {
lastTask.next = null;
}
} else if (task.prev == null && task.next == null) { // 已经被移除
// Since task is not lastTask, then it has been removed or not been added.
return;
} else if (task.prev == null) { // 头节点
// task is the head of list and the list has at least 2 nodes
task.next.prev = null;
} else { // 中间的节点
task.prev.next = task.next;
task.next.prev = task.prev;
}
// 重置 task 前后节点为空
task.prev = null;
task.next = null;
}
```

该方法的调用，在 [「7.8 WriteTimeoutTask」]() 会看到。

## []( "7.7 writeTimedOut")7.7 writeTimedOut

/#writeTimedOut(ChannelHandlerContext ctx)
方法，写入超时，关闭 Channel 通道。代码如下：

```
//*/*
/* Is called when a write timeout was detected
/*/
protected void writeTimedOut(ChannelHandlerContext ctx) throws Exception{
if (!closed) {
// 触发 Exception Caught 事件到 pipeline 中，异常为 WriteTimeoutException
ctx.fireExceptionCaught(WriteTimeoutException.INSTANCE);
// 关闭 Channel 通道
ctx.close();
// 标记 Channel 为已关闭
closed = true;
}
}
```

- 和

ReadTimeoutHandler/#readTimeout(ChannelHandlerContext ctx)
方法，基本类似。

该方法的调用，在 [「7.8 WriteTimeoutTask」]() 会看到。

## []( "7.8 WriteTimeoutTask")7.8 WriteTimeoutTask

WriteTimeoutTask ，实现 Runnable 和 ChannelFutureListener 接口，写入超时任务。
WriteTimeoutTask 是 WriteTimeoutHandler 的内部类。

### []( "7.8.1 构造方法")7.8.1 构造方法

```
private final ChannelHandlerContext ctx;
//*/*
/* 写入任务的 Promise 对象
/*/
private final ChannelPromise promise;
// WriteTimeoutTask is also a node of a doubly-linked list
//*/*
/* 前一个 task
/*/
WriteTimeoutTask prev;
//*/*
/* 后一个 task
/*/
WriteTimeoutTask next;
//*/*
/* 定时任务
/*/
ScheduledFuture<?> scheduledFuture;
WriteTimeoutTask(ChannelHandlerContext ctx, ChannelPromise promise) {
this.ctx = ctx;
this.promise = promise;
}
```

### []( "7.8.2 run")7.8.2 run

当定时任务执行，说明写入任务执行超时。代码如下：

```
@Override
public void run(){
// Was not written yet so issue a write timeout
// The promise itself will be failed with a ClosedChannelException once the close() was issued
// See https://github.com/netty/netty/issues/2159
if (!promise.isDone()) { // 未完成，说明写入超时
try {
// <1> 写入超时，关闭 Channel 通道
writeTimedOut(ctx);
} catch (Throwable t) {
// 触发 Exception Caught 事件到 pipeline 中
ctx.fireExceptionCaught(t);
}
}
// <2> 移除出链表
removeWriteTimeoutTask(this);
}
```

- <1>
  处，调用

/#writeTimedOut(ChannelHandlerContext ctx)
方法，写入超时，关闭 Channel 通道。

- <2>
  处，调用

/#removeWriteTimeoutTask(WriteTimeoutTask task)
方法，移除出链表。

### []( "7.8.3 operationComplete")7.8.3 operationComplete

当回调方法执行，说明写入任务执行完成。代码如下：

```
@Override
public void operationComplete(ChannelFuture future) throws Exception{
// scheduledFuture has already be set when reaching here
// <1> 取消定时任务
scheduledFuture.cancel(false);
// <2> 移除出链表
removeWriteTimeoutTask(this);
}
```

- <1>
  处，取消定时任务。
- <2>
  处，调用

/#removeWriteTimeoutTask(WriteTimeoutTask task)
方法，移除出链表。

# []( "666. 彩蛋")666. 彩蛋

和 「5.7 hasOutputChanged」(/#) 小节，这个方法较真了好久。感谢中间，基友【莫那一鲁道】的沟通。

推荐阅读文章：

- 莫那一鲁道 [《Netty 心跳服务之 IdleStateHandler 源码分析》](https://www.jianshu.com/p/f2ed73cf4df8)
- Hypercube [自顶向下深入分析 Netty（八）–ChannelHandler](https://www.jianshu.com/p/a9bcd89553f5)
