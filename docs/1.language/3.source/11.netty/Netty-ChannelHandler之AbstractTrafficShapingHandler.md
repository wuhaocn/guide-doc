# 精尽 Netty 源码解析 —— ChannelHandler（六）之 AbstractTrafficShapingHandler

笔者先把 Netty 主要的内容写完，所以关于 AbstractTrafficShapingHandler 的分享，先放在后续的计划里。

当然，良心如我，还是为对这块感兴趣的胖友，先准备好了一篇不错的文章：

- tomas 家的小拨浪鼓 [《Netty 那些事儿 ——— Netty 实现“流量整形”原理分析及实战》](https://www.jianshu.com/p/bea1b4ea8402)

为避免可能 [《Netty 那些事儿 ——— Netty 实现“流量整形”原理分析及实战》](https://www.jianshu.com/p/bea1b4ea8402) 被作者删除，笔者这里先复制一份作为备份。

# []( "666. 备份")666. 备份

本文是 Netty 文集中“Netty 那些事儿”系列的文章。主要结合在开发实战中，我们遇到的一些“奇奇怪怪”的问题，以及如何正确且更好的使用 Netty 框架，并会对 Netty 中涉及的重要设计理念进行介绍。

### []("Netty实现“流量整形”原理分析")Netty 实现“流量整形”原理分析

### []("流量整形")流量整形

流量整形（Traffic Shaping）是一种主动调整流量输出速率的措施。流量整形与流量监管的主要区别在于，流量整形对流量监管中需要丢弃的报文进行缓存——通常是将它们放入缓冲区或队列内，也称流量整形（Traffic Shaping，简称 TS）。当报文的发送速度过快时，首先在缓冲区进行缓存；再通过流量计量算法的控制下“均匀”地发送这些被缓冲的报文。流量整形与流量监管的另一区别是，整形可能会增加延迟，而监管几乎不引入额外的延迟。

Netty 提供了 GlobalTrafficShapingHandler、ChannelTrafficShapingHandler、GlobalChannelTrafficShapingHandler 三个类来实现流量整形，他们都是 AbstractTrafficShapingHandler 抽象类的实现类，下面我们就对其进行介绍，让我们来了解 Netty 是如何实现流量整形的。

### []("核心类分析")核心类分析

### []("AbstractTrafficShapingHandler")AbstractTrafficShapingHandler

AbstractTrafficShapingHandler 允许限制全局的带宽（见 GlobalTrafficShapingHandler）或者每个 session 的带宽（见 ChannelTrafficShapingHandler）作为流量整形。
它允许你使用 TrafficCounter 来实现几乎实时的带宽监控，TrafficCounter 会在每个检测间期（checkInterval）调用这个处理器的 doAccounting 方法。

如果你有任何特别的原因想要停止监控（计数）或者改变读写的限制或者改变检测间期（checkInterval），可以使用如下方法：
① configure：允许你改变读或写的限制，或者检测间期（checkInterval）；
② getTrafficCounter：允许你获得 TrafficCounter，并可以停止或启动监控，直接改变检测间期（checkInterval），或去访问它的值。

**TrafficCounter**：对读和写的字节进行计数以用于限制流量。
它会根据给定的检测间期周期性的计算统计入站和出站的流量，并会回调 AbstractTrafficShapingHandler 的 doAccounting 方法。
如果检测间期（checkInterval）是 0，将不会进行计数并且统计只会在每次读或写操作时进行计算。

- configure

```
public void configure(long newWriteLimit, long newReadLimit,
long newCheckInterval){
configure(newWriteLimit, newReadLimit);
configure(newCheckInterval);
}
```

配置新的写限制、读限制、检测间期。该方法会尽最大努力进行此更改，这意味着已经被延迟进行的流量将不会使用新的配置，它仅用于新的流量中。

- ReopenReadTimerTask

```
static final class ReopenReadTimerTask implements Runnable{
final ChannelHandlerContext ctx;
ReopenReadTimerTask(ChannelHandlerContext ctx) {
this.ctx = ctx;
}
@Override
public void run(){
ChannelConfig config = ctx.channel().config();
if (!config.isAutoRead() && isHandlerActive(ctx)) {
// If AutoRead is False and Active is True, user make a direct setAutoRead(false)
// Then Just reset the status
if (logger.isDebugEnabled()) {
logger.debug("Not unsuspend: " + config.isAutoRead() + ':' +
isHandlerActive(ctx));
}
ctx.attr(READ_SUSPENDED).set(false);
} else {
// Anything else allows the handler to reset the AutoRead
if (logger.isDebugEnabled()) {
if (config.isAutoRead() && !isHandlerActive(ctx)) {
logger.debug("Unsuspend: " + config.isAutoRead() + ':' +
isHandlerActive(ctx));
} else {
logger.debug("Normal unsuspend: " + config.isAutoRead() + ':'
+ isHandlerActive(ctx));
}
}
ctx.attr(READ_SUSPENDED).set(false);
config.setAutoRead(true);
ctx.channel().read();
}
if (logger.isDebugEnabled()) {
logger.debug("Unsuspend final status => " + config.isAutoRead() + ':'
+ isHandlerActive(ctx));
}
}
}
```

重启读操作的定时任务。该定时任务总会实现：
a) 如果 Channel 的 autoRead 为 false，并且 AbstractTrafficShapingHandler 的 READ_SUSPENDED 属性设置为 null 或 false（说明读暂停未启用或开启），则直接将 READ_SUSPENDED 属性设置为 false。
b) 否则，如果 Channel 的 autoRead 为 true，或者 READ_SUSPENDED 属性的值为 true（说明读暂停开启了），则将 READ_SUSPENDED 属性设置为 false，并将 Channel 的 autoRead 标识为 true（该操作底层会将该 Channel 的 OP_READ 事件重新注册为感兴趣的事件，这样 Selector 就会监听该 Channel 的读就绪事件了），最后触发一次 Channel 的 read 操作。
也就说，若“读操作”为“开启”状态（READ_SUSPENDED 为 null 或 false）的情况下，Channel 的 autoRead 是保持 Channel 原有的配置，此时并不会做什么操作。但当“读操作”从“暂停”状态（READ_SUSPENDED 为 true）转为“开启”状态（READ_SUSPENDED 为 false）时，则会将 Channel 的 autoRead 标志为 true，并将“读操作”设置为“开启”状态（READ_SUSPENDED 为 false）。

- channelRead

```
public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception{
long size = calculateSize(msg);
long now = TrafficCounter.milliSecondFromNano();
if (size > 0) {
// compute the number of ms to wait before reopening the channel
long wait = trafficCounter.readTimeToWait(size, readLimit, maxTime, now);
wait = checkWaitReadTime(ctx, wait, now);
if (wait >= MINIMAL_WAIT) { // At least 10ms seems a minimal
// time in order to try to limit the traffic
// Only AutoRead AND HandlerActive True means Context Active
ChannelConfig config = ctx.channel().config();
if (logger.isDebugEnabled()) {
logger.debug("Read suspend: " + wait + ':' + config.isAutoRead() + ':'
+ isHandlerActive(ctx));
}
if (config.isAutoRead() && isHandlerActive(ctx)) {
config.setAutoRead(false);
ctx.attr(READ_SUSPENDED).set(true);
// Create a Runnable to reactive the read if needed. If one was create before it will just be
// reused to limit object creation
Attribute<Runnable> attr = ctx.attr(REOPEN_TASK);
Runnable reopenTask = attr.get();
if (reopenTask == null) {
reopenTask = new ReopenReadTimerTask(ctx);
attr.set(reopenTask);
}
ctx.executor().schedule(reopenTask, wait, TimeUnit.MILLISECONDS);
if (logger.isDebugEnabled()) {
logger.debug("Suspend final status => " + config.isAutoRead() + ':'
+ isHandlerActive(ctx) + " will reopened at: " + wait);
}
}
}
}
informReadOperation(ctx, now);
ctx.fireChannelRead(msg);
}
```

① 『long size = calculateSize(msg);』计算本次读取到的消息的字节数。
② 如果读取到的字节数大于 0，则根据数据的大小、设定的 readLimit、最大延迟时间等计算（『long wait = trafficCounter.readTimeToWait(size, readLimit, maxTime, now);』）得到下一次开启读操作需要的延迟时间（距当前时间而言）wait(毫秒)。
③ 如果 a）wait >= MINIMAL_WAIT(10 毫秒)。并且 b）当前 Channel 为自动读取（即，autoRead 为 true）以及 c）当前的 READ_SUSPENDED 标识为 null 或 false（即，读操作未被暂停），那么将 Channel 的 autoRead 设置为 false（该操作底层会将该 Channel 的 OP_READ 事件从感兴趣的事件中移除，这样 Selector 就不会监听该 Channel 的读就绪事件了），并且将 READ_SUSPENDED 标识为 true（说明，接下来的读操作会被暂停），并将“重新开启读操作“封装为一个任务，让入 Channel 所注册 NioEventLoop 的定时任务队列中（延迟 wait 时间后执行）。
也就说，只有当计算出的下一次读操作的时间大于了 MINIMAL_WAIT(10 毫秒)，并且当前 Channel 是自动读取的，且“读操作”处于“开启”状态时，才会去暂停读操作，而暂停读操作主要需要完成三件事：[1]将 Channel 的 autoRead 标识设置为 false，这使得 OP_READ 会从感兴趣的事件中移除，这样 Selector 就会不会监听这个 Channel 的读就绪事件了；[2]将“读操作”状态设置为“暂停”（READ_SUSPENDED 为 true）；[3]将重启开启“读操作”的操作封装为一个 task，在延迟 wait 时间后执行。
当你将得 Channel 的 autoRead 都会被设置为 false 时，Netty 底层就不会再去执行读操作了，也就是说，这时如果有数据过来，会先放入到内核的接收缓冲区，只有我们执行读操作的时候数据才会从内核缓冲区读取到用户缓冲区中。而对于 TCP 协议来说，你不要担心一次内核缓冲区会溢出。因为如果应用进程一直没有读取，接收缓冲区满了之后，发生的动作是：通知对端 TCP 协议中的窗口关闭。这个便是滑动窗口的实现。保证 TCP 套接口接收缓冲区不会溢出，从而保证了 TCP 是可靠传输。因为对方不允许发出超过所通告窗口大小的数据。 这就是 TCP 的流量控制，如果对方无视窗口大小而发出了超过窗口大小的数据，则接收方 TCP 将丢弃它。
④ 将当前的消息发送给 ChannelPipeline 中的下一个 ChannelInboundHandler。

- write

```
public void write(final ChannelHandlerContext ctx, final Object msg, final ChannelPromise promise)
throws Exception{
long size = calculateSize(msg);
long now = TrafficCounter.milliSecondFromNano();
if (size > 0) {
// compute the number of ms to wait before continue with the channel
long wait = trafficCounter.writeTimeToWait(size, writeLimit, maxTime, now);
if (wait >= MINIMAL_WAIT) {
if (logger.isDebugEnabled()) {
logger.debug("Write suspend: " + wait + ':' + ctx.channel().config().isAutoRead() + ':'
+ isHandlerActive(ctx));
}
submitWrite(ctx, msg, size, wait, now, promise);
return;
}
}
// to maintain order of write
submitWrite(ctx, msg, size, 0, now, promise);
}
```

① 『long size = calculateSize(msg);』计算待写出的数据大小
② 如果待写出数据的字节数大于 0，则根据数据大小、设置的 writeLimit、最大延迟时间等计算（『long wait = trafficCounter.writeTimeToWait(size, writeLimit, maxTime, now);』）得到本次写操作需要的延迟时间（距当前时间而言）wait(毫秒)。
③ 如果 wait >= MINIMAL_WAIT（10 毫秒），则调用『submitWrite(ctx, msg, size, wait, now, promise);』wait 即为延迟时间，该方法的具体实现由子类完成；否则，若 wait < MINIMAL_WAIT（10 毫秒），则调用『submitWrite(ctx, msg, size, 0, now, promise);』注意这里传递的延迟时间为 0 了。

### []("GlobalTrafficShapingHandler")GlobalTrafficShapingHandler

这实现了 AbstractTrafficShapingHandler 的全局流量整形，也就是说它限制了全局的带宽，无论开启了几个 channel。
注意『 OutboundBuffer.setUserDefinedWritability(index, boolean)』中索引使用’2’。

一般用途如下：
创建一个唯一的 GlobalTrafficShapingHandler

```
GlobalTrafficShapingHandler myHandler = new GlobalTrafficShapingHandler(executor);
pipeline.addLast(myHandler);
```

executor 可以是底层的 IO 工作池

注意，这个处理器是覆盖所有管道的，这意味着只有一个处理器对象会被创建并且作为所有 channel 间共享的计数器，它必须于所有的 channel 共享。
所有你可以见到，该类的定义上面有个

@Sharable
注解。

在你的处理器中，你需要考虑使用『channel.isWritable()』和『channelWritabilityChanged(ctx)』来处理可写性，或通过在 ctx.write()返回的 future 上注册 listener 来实现。

你还需要考虑读或写操作对象的大小需要和你要求的带宽相对应：比如，你将一个 10M 大小的对象用于 10KB/s 的带宽将会导致爆发效果，若你将 100KB 大小的对象用于在 1M/s 带宽那么将会被流量整形处理器平滑处理。

一旦不在需要这个处理器时请确保调用『release()』以释放所有内部的资源。这不会关闭 EventExecutor，因为它可能是共享的，所以这需要你自己做。

GlobalTrafficShapingHandler 中持有一个 Channel 的哈希表，用于存储当前应用所有的 Channel：

```
private final ConcurrentMap<Integer, PerChannel> channelQueues = PlatformDependent.newConcurrentHashMap();
```

key 为 Channel 的 hashCode；value 是一个 PerChannel 对象。
PerChannel 对象中维护有该 Channel 的待发送数据的消息队列（ArrayDeque messagesQueue）。

- submitWrite

```
void submitWrite(final ChannelHandlerContext ctx, final Object msg,
final long size, final long writedelay, final long now,
final ChannelPromise promise){
Channel channel = ctx.channel();
Integer key = channel.hashCode();
PerChannel perChannel = channelQueues.get(key);
if (perChannel == null) {
// in case write occurs before handlerAdded is raised for this handler
// imply a synchronized only if needed
perChannel = getOrSetPerChannel(ctx);
}
final ToSend newToSend;
long delay = writedelay;
boolean globalSizeExceeded = false;
// write operations need synchronization
synchronized (perChannel) {
if (writedelay == 0 && perChannel.messagesQueue.isEmpty()) {
trafficCounter.bytesRealWriteFlowControl(size);
ctx.write(msg, promise);
perChannel.lastWriteTimestamp = now;
return;
}
if (delay > maxTime && now + delay - perChannel.lastWriteTimestamp > maxTime) {
delay = maxTime;
}
newToSend = new ToSend(delay + now, msg, size, promise);
perChannel.messagesQueue.addLast(newToSend);
perChannel.queueSize += size;
queuesSize.addAndGet(size);
checkWriteSuspend(ctx, delay, perChannel.queueSize);
if (queuesSize.get() > maxGlobalWriteSize) {
globalSizeExceeded = true;
}
}
if (globalSizeExceeded) {
setUserDefinedWritability(ctx, false);
}
final long futureNow = newToSend.relativeTimeAction;
final PerChannel forSchedule = perChannel;
ctx.executor().schedule(new Runnable() {
@Override
public void run(){
sendAllValid(ctx, forSchedule, futureNow);
}
}, delay, TimeUnit.MILLISECONDS);
}
```

写操作提交上来的数据。
① 如果写延迟为 0，且当前该 Channel 的 messagesQueue 为空（说明，在此消息前没有待发送的消息了），那么直接发送该消息包。并返回，否则到下一步。
② 『newToSend = new ToSend(delay + now, msg, size, promise);
perChannel.messagesQueue.addLast(newToSend);』
将待发送的数据封装成 ToSend 对象放入 PerChannel 的消息队列中（messagesQueue）。注意，这里的 messagesQueue 是一个 ArrayDeque 队列，我们总是从队列尾部插入。然后从队列的头获取消息来依次发送，这就保证了消息的有序性。但是，如果一个大数据包前于一个小数据包发送的话，小数据包也会因为大数据包的延迟发送而被延迟到大数据包发送后才会发送。
ToSend 对象中持有带发送的数据对象、发送的相对延迟时间（即，根据数据包大小以及设置的写流量限制值（writeLimit）等计算出来的延迟操作的时间）、消息数据的大小、异步写操作的 promise。
③ 『checkWriteSuspend(ctx, delay, perChannel.queueSize);』
检查单个 Channel 待发送的数据包是否超过了 maxWriteSize（默认 4M），或者延迟时间是否超过了 maxWriteDelay（默认 4s）。如果是的话，则调用『setUserDefinedWritability(ctx, false);』该方法会将 ChannelOutboundBuffer 中的 unwritable 属性值的相应标志位置位（unwritable 关系到 isWritable 方法是否会返回 true。以及会在 unwritable 从 0 到非 0 间变化时触发 ChannelWritabilityChanged 事件）。
④ 如果所有待发送的数据大小（这里指所有 Channel 累积的待发送的数据大小）大于了 maxGlobalWriteSize（默认 400M），则标识 globalSizeExceeded 为 true，并且调用『setUserDefinedWritability(ctx, false)』将 ChannelOutboundBuffer 中的 unwritable 属性值相应的标志位置位。
⑤ 根据指定的延迟时间（一个 >= 0 且 <= maxTime 的值，maxTime 默认 15s）delay，将『sendAllValid(ctx, forSchedule, futureNow);』操作封装成一个任务提交至 executor 的定时周期任务队列中。
sendAllValid 操作会遍历该 Channel 中待发送的消息队列 messagesQueue，依次取出 perChannel.messagesQueue 中的消息包，将满足发送条件（即，延迟发送的时间已经到了）的消息发送给到 ChannelPipeline 中的下一个 ChannelOutboundHandler（ctx.write(newToSend.toSend, newToSend.promise);），并且将 perChannel.queueSize（当前 Channel 待发送的总数据大小）和 queuesSize（所有 Channel 待发送的总数据大小）减小相应的值（即，被发送出去的这个数据包的大小）。循环遍历前面的操作直到当前的消息不满足发送条件则退出遍历。并且如果该 Channel 的消息队列中的消息全部都发送出去的话（即，messagesQueue.isEmpty()为 true），则会通过调用『releaseWriteSuspended(ctx);』来释放写暂停。而该方法底层会将 ChannelOutboundBuffer 中的 unwritable 属性值相应的标志位重置。

### []("ChannelTrafficShapingHandler")ChannelTrafficShapingHandler

ChannelTrafficShapingHandler 是针对单个 Channel 的流量整形，和 GlobalTrafficShapingHandler 的思想是一样的。只是实现中没有对全局概念的检测，仅检测了当前这个 Channel 的数据。
这里就不再赘述了。

### []("GlobalChannelTrafficShapingHandler")GlobalChannelTrafficShapingHandler

相比于 GlobalTrafficShapingHandler 增加了一个误差概念，以平衡各个 Channel 间的读/写操作。也就是说，使得各个 Channel 间的读/写操作尽量均衡。比如，尽量避免不同 Channel 的大数据包都延迟近乎一样的是时间再操作，以及如果小数据包在一个大数据包后才发送，则减少该小数据包的延迟发送时间等。。

### []("“流量整形”实战")“流量整形”实战

这里仅展示服务端和客户端中使用“流量整形”功能涉及的关键代码，完整 demo 可见[github](https://link.jianshu.com?t=https%3A%2F%2Fgithub.com%2Flinling1%2Fnetty_module_function%2Ftree%2Fmaster%2Fsrc%2Fmain%2Fjava%2Fcom%2Flinling%2Fnetty%2Ftrafficshaping)
**服务端**
使用 GlobalTrafficShapingHandler 来实现服务端的“流量整形”，每当有客户端连接至服务端时服务端就会开始往这个客户端发送 26M 的数据包。我们将 GlobalTrafficShapingHandler 的 writeLimit 设置为 10M/S。并使用了 ChunkedWriteHandler 来实现大数据包拆分成小数据包发送的功能。

MyServerInitializer 实现：在 ChannelPipeline 中注册了 GlobalTrafficShapingHandler

```
public class MyServerInitializer extends ChannelInitializer<SocketChannel>{
Charset utf8 = Charset.forName("utf-8");
final int M = 1024 /* 1024;
@Override
protected void initChannel(SocketChannel ch) throws Exception{
GlobalTrafficShapingHandler globalTrafficShapingHandler = new GlobalTrafficShapingHandler(ch.eventLoop().parent(), 10 /* M, 50 /* M);
// globalTrafficShapingHandler.setMaxGlobalWriteSize(50 /* M);
// globalTrafficShapingHandler.setMaxWriteSize(5 /* M);
ch.pipeline()
.addLast("LengthFieldBasedFrameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4, true))
.addLast("LengthFieldPrepender", new LengthFieldPrepender(4, 0))
.addLast("GlobalTrafficShapingHandler", globalTrafficShapingHandler)
.addLast("chunkedWriteHandler", new ChunkedWriteHandler())
.addLast("myServerChunkHandler", new MyServerChunkHandler())
.addLast("StringDecoder", new StringDecoder(utf8))
.addLast("StringEncoder", new StringEncoder(utf8))
.addLast("myServerHandler", new MyServerHandlerForPlain());
}
}
```

ServerHandler：当有客户端连接上了后就开始给客户端发送消息。并且通过『Channel/#isWritable』方法以及『channelWritabilityChanged』事件来监控可写性，以判断啥时需要停止数据的写出，啥时可以开始继续写出数据。同时写了一个简易的 task 来计算每秒数据的发送速率（并非精确的计算）。

```
public class MyServerHandlerForPlain extends MyServerCommonHandler{
@Override
protected void sentData(ChannelHandlerContext ctx){
sentFlag = true;
ctx.writeAndFlush(tempStr, getChannelProgressivePromise(ctx, future -> {
if(ctx.channel().isWritable() && !sentFlag) {
sentData(ctx);
}
}));
}
@Override
public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception{
if(ctx.channel().isWritable() && !sentFlag) {
// System.out.println(" /#/#/#/#/#/# 重新开始写数据 /#/#/#/#/#/#");
sentData(ctx);
} else {
// System.out.println(" ===== 写暂停 =====");
}
}
}
public abstract class MyServerCommonHandler extends SimpleChannelInboundHandler<String>{
protected final int M = 1024 /* 1024;
protected String tempStr;
protected AtomicLong consumeMsgLength;
protected Runnable counterTask;
private long priorProgress;
protected boolean sentFlag;
@Override
public void handlerAdded(ChannelHandlerContext ctx) throws Exception{
consumeMsgLength = new AtomicLong();
counterTask = () -> {
while (true) {
try {
Thread.sleep(1000);
} catch (InterruptedException e) {
}
long length = consumeMsgLength.getAndSet(0);
System.out.println("/*/*/* " + ctx.channel().remoteAddress() + " rate（M/S）：" + (length / M));
}
};
StringBuilder builder = new StringBuilder();
for (int i = 0; i < M; i++) {
builder.append("abcdefghijklmnopqrstuvwxyz");
}
tempStr = builder.toString();
super.handlerAdded(ctx);
}
@Override
public void channelActive(ChannelHandlerContext ctx) throws Exception{
sentData(ctx);
new Thread(counterTask).start();
}
protected ChannelProgressivePromise getChannelProgressivePromise(ChannelHandlerContext ctx, Consumer<ChannelProgressiveFuture> completedAction){
ChannelProgressivePromise channelProgressivePromise = ctx.newProgressivePromise();
channelProgressivePromise.addListener(new ChannelProgressiveFutureListener(){
@Override
public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) throws Exception{
consumeMsgLength.addAndGet(progress - priorProgress);
priorProgress = progress;
}
@Override
public void operationComplete(ChannelProgressiveFuture future) throws Exception{
sentFlag = false;
if(future.isSuccess()){
System.out.println("成功发送完成！");
priorProgress -= 26 /* M;
Optional.ofNullable(completedAction).ifPresent(action -> action.accept(future));
} else {
System.out.println("发送失败！！！！！");
future.cause().printStackTrace();
}
}
});
return channelProgressivePromise;
}
protected abstract void sentData(ChannelHandlerContext ctx);
@Override
protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception{
System.out.println("===== receive client msg : " + msg);
}
@Override
public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception{
cause.printStackTrace();
ctx.channel().close();
}
}
```

**客户端**
客户端比较简单了，使用 ChannelTrafficShapingHandler 来实现“流量整形”，并将 readLimit 设置为 1M/S。

```
public class MyClientInitializer extends ChannelInitializer<SocketChannel>{
Charset utf8 = Charset.forName("utf-8");
final int M = 1024 /* 1024;
@Override
protected void initChannel(SocketChannel ch) throws Exception{
ChannelTrafficShapingHandler channelTrafficShapingHandler = new ChannelTrafficShapingHandler(10 /* M, 1 /* M);
ch.pipeline()
.addLast("channelTrafficShapingHandler",channelTrafficShapingHandler)
.addLast("lengthFieldBasedFrameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4, true))
.addLast("lengthFieldPrepender", new LengthFieldPrepender(4, 0))
.addLast("stringDecoder", new StringDecoder(utf8))
.addLast("stringEncoder", new StringEncoder(utf8))
.addLast("myClientHandler", new MyClientHandler());
}
}
```

### []("注意事项")注意事项

① 注意，trafficShaping 是通过程序来达到控制流量的作用，并不是网络层真实的传输流量大小的控制。TrafficShapingHandler 仅仅是根据消息大小（待发送出去的数据包大小）和设定的流量限制来得出延迟发送该包的时间，即同一时刻不会发送过大的数据导致带宽负荷不了。但是并没有对大数据包进行拆分的作用，这会使在发送这个大数据包时同样可能会导致带宽爆掉的情况。所以你需要注意一次发送数据包的大小，不要大于你设置限定的写带宽大小(writeLimit)。你可以通过在业务 handler 中自己控制的方式，或者考虑使用 ChunkedWriteHandler，如果它能满足你的要求的话。同时注意，不要将 writeLimit 和 readLimit 设置的过小，这是没有意义的，只会导致读/写操作的不断停顿。。
② 注意，不要在非 NioEventLoop 线程中不停歇的发送非 ByteBuf、ByteBufHolder 或者 FileRegion 对象的大数据包，如：

```
new Thread(() -> {
while (true) {
if(ctx.channel().isWritable()) {
ctx.writeAndFlush(tempStr, getChannelProgressivePromise(ctx, null));
}
}
}).start();
```

因为写操作是一个 I/O 操作，当你在非 NioEventLoop 线程上执行了 Channel 的 I/O 操作的话，该操作会封装为一个 task 被提交至 NioEventLoop 的任务队列中，以使得 I/O 操作最终是 NioEventLoop 线程上得到执行。
而提交这个任务的流程，仅会对 ByteBuf、ByteBufHolder 或者 FileRegion 对象进行真实数据大小的估计（其他情况默认估计大小为 8 bytes），并将估计后的数据大小值对该 ChannelOutboundBuffer 的 totalPendingSize 属性值进行累加。而 totalPendingSize 同 WriteBufferWaterMark 一起来控制着 Channel 的 unwritable。所以，如果你在一个非 NioEventLoop 线程中不断地发送一个非 ByteBuf、ByteBufHolder 或者 FileRegion 对象的大数据包时，最终就会导致提交大量的任务到 NioEventLoop 线程的任务队列中，而当 NioEventLoop 线程在真实执行这些 task 时可能发生 OOM。

### []("扩展")扩展

### []( "关于 “OP_WRITE” 与 “Channel#isWritable()”")关于 “OP_WRITE” 与 “Channel/#isWritable()”

首先，我们需要明确的一点是，“OP_WRITE” 与 “Channel/#isWritable()” 虽然都是的对数据的可写性进行检测，但是它们是分别针对不同层面的可写性的。

- “OP_WRITE”是当内核的发送缓冲区满的时候，我们程序执行 write 操作（这里是真实写操作了，将数据通过 TCP 协议进行网络传输）无法将数据写出，这时我们需要注册 OP_WRITE 事件。这样当发送缓冲区空闲时就 OP_WRITE 事件就会触发，我们就可以继续 write 未写完的数据了。这可以看做是对系统层面的可写性的一种检测。
- 而“Channel/#isWritable()”则是检测程序中的缓存的待写出的数据大小超过了我们设定的相关最大写数据大小，如果超过了 isWritable()方法将返回 false，说明这时我们不应该再继续进行 write 操作了（这里写操作一般为通过 ChannelHandlerContext 或 Channel 进行的写操作）。
  关于“OP_WRITE”前面的[NIO 文章](https://www.jianshu.com/p/1af407c043cb)及前面 Netty 系列文章已经进行过不少介绍了，这里不再赘述。下面我们来看看“Channel/#isWritable()”是如果检测可写性的。

```
public boolean isWritable(){
return unwritable == 0;
}
```

ChannelOutboundBuffer 的 unwritable 属性为 0 时，Channel 的 isWritable()方法将返回 true；否之，返回 false；
unwritable 可以看做是一个二进制的开关属性值。它的二进制的不同位表示的不同状态的开关。如：

[![img](https://upload-images.jianshu.io/upload_images/4235178-5291c64aba1bbaac.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/972/format/jpeg)](https://upload-images.jianshu.io/upload_images/4235178-5291c64aba1bbaac.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/972/format/jpeg 'img')img

ChannelOutboundBuffer 有四个方法会对 unwritable 属性值进行修改：clearUserDefinedWritability、setUnwritable、setUserDefinedWritability、setWritable。并且，当 unwritable 从 0 到非 0 间改变时还会触发 ChannelWritabilityChanged 事件，以通知 ChannelPipeline 中的各个 ChannelHandler 当前 Channel 可写性发生了改变。

其中 setUnwritable、setWritable 这对方法是由于待写数据大小高于或低于了 WriteBufferWaterMark 的水位线而导致的 unwritable 属性值的改变。
我们所执行的『ChannelHandlerContext/#write』和『Channel/#write』操作会先将待发送的数据包放到 Channel 的输出缓冲区（ChannelOutboundBuffer）中，然后在执行 flush 操作的时候，会从 ChannelOutboundBuffer 中依次出去数据包进行真实的网络数据传输。而 WriteBufferWaterMark 控制的就是 ChannelOutboundBuffer 中待发送的数据总大小（即，totalPendingSize：包含一个个 ByteBuf 中待发送的数据大小，以及数据包对象占用的大小）。如果 totalPendingSize 的大小超过了 WriteBufferWaterMark 高水位（默认为 64KB），则会 unwritable 属性的’WriteBufferWaterMark 状态位’置位 1；随着数据不断写出（每写完一个 ByteBuf 后，就会将 totalPendingSize 减少相应的值），当 totalPendingSize 的大小小于 WriteBufferWaterMark 低水位（默认为 32KB）时，则会将 unwritable 属性的’WriteBufferWaterMark 状态位’置位 0。

而本文的主题“流量整形”则是使用了 clearUserDefinedWritability、setUserDefinedWritability 这对方法来控制 unwritable 相应的状态位。
当数据 write 到 GlobalTrafficShapingHandler 的时候，估计的数据大小大于 0，且通过 trafficCounter 计算出的延迟时间大于最小延迟时间（MINIMAL_WAIT，默认为 10ms）时，满足如下任意条件会使得 unwritable 的’GlobalTrafficShaping 状态位’置为 1：

- 当 perChannel.queueSize（单个 Channel 中待写出的总数据大小）设定的最大写数据大小时（默认为 4M）
- 当 queuesSize（所有 Channel 的待写出的总数据大小）超过设定的最大写数据大小时（默认为 400M）
- 对于 Channel 发送的单个数据包如果太大，以至于计算出的延迟发送时间大于了最大延迟发送时间（maxWriteDelay，默认为 4s）时

随着写延迟时间的到达 GlobalTrafficShaping 中积压的数据不断被写出，当某个 Channel 中所有待写出的数据都写出后（注意，这里指将数据写到 ChannelPipeline 中的下一个 ChannelOutboundBuffer 中）会将 unwritable 的’GlobalTrafficShaping 状态位’置为 0。
