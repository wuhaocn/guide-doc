# 精尽 Netty 源码分析 —— 启动（二）之客户端

# []( "1. 概述")1. 概述

本文，我们来分享 Bootstrap 分享 Netty 客户端。因为我们日常使用 Netty 主要使用 NIO 部分，所以本文也只分享 Netty NIO 客户端。

# []( "2. Bootstrap 示例")2. Bootstrap 示例

下面，我们先来看一个 ServerBootstrap 的使用示例，就是我们在 [《精尽 Netty 源码分析 —— 调试环境搭建》](http://svip.iocoder.cn/Netty/build-debugging-environment/#5-2-EchoClient) 搭建的 EchoClient 示例。代码如下：

```
public final class EchoClient{
static final boolean SSL = System.getProperty("ssl") != null;
static final String HOST = System.getProperty("host", "127.0.0.1");
static final int PORT = Integer.parseInt(System.getProperty("port", "8007"));
static final int SIZE = Integer.parseInt(System.getProperty("size", "256"));
public static void main(String[] args) throws Exception{
// Configure SSL.git
// 配置 SSL
final SslContext sslCtx;
if (SSL) {
sslCtx = SslContextBuilder.forClient()
.trustManager(InsecureTrustManagerFactory.INSTANCE).build();
} else {
sslCtx = null;
}
// Configure the client.
// 创建一个 EventLoopGroup 对象
EventLoopGroup group = new NioEventLoopGroup();
try {
// 创建 Bootstrap 对象
Bootstrap b = new Bootstrap();
b.group(group) // 设置使用的 EventLoopGroup
.channel(NioSocketChannel.class) // 设置要被实例化的为 NioSocketChannel 类
.option(ChannelOption.TCP_NODELAY, true) // 设置 NioSocketChannel 的可选项
.handler(new ChannelInitializer<SocketChannel>() { // 设置 NioSocketChannel 的处理器
@Override
public void initChannel(SocketChannel ch) throws Exception{
ChannelPipeline p = ch.pipeline();
if (sslCtx != null) {
p.addLast(sslCtx.newHandler(ch.alloc(), HOST, PORT));
}
//p.addLast(new LoggingHandler(LogLevel.INFO));
p.addLast(new EchoClientHandler());
}
});
// Start the client.
// 连接服务器，并同步等待成功，即启动客户端
ChannelFuture f = b.connect(HOST, PORT).sync();
// Wait until the connection is closed.
// 监听客户端关闭，并阻塞等待
f.channel().closeFuture().sync();
} finally {
// Shut down the event loop to terminate all threads.
// 优雅关闭一个 EventLoopGroup 对象
group.shutdownGracefully();
}
}
}
```

- 示例比较简单，已经添加中文注释，胖友自己查看。

# []( "3. Bootstrap")3. Bootstrap

io.netty.bootstrap.Bootstrap
，实现 AbstractBootstrap 抽象类，用于 Client 的启动器实现类。

## []( "3.1 构造方法")3.1 构造方法

```
//*/*
/* 默认地址解析器对象
/*/
private static final AddressResolverGroup<?> DEFAULT_RESOLVER = DefaultAddressResolverGroup.INSTANCE;
//*/*
/* 启动类配置对象
/*/
private final BootstrapConfig config = new BootstrapConfig(this);
//*/*
/* 地址解析器对象
/*/
@SuppressWarnings("unchecked")
private volatile AddressResolverGroup<SocketAddress> resolver = (AddressResolverGroup<SocketAddress>) DEFAULT_RESOLVER;
//*/*
/* 连接地址
/*/
private volatile SocketAddress remoteAddress;
public Bootstrap(){ }
private Bootstrap(Bootstrap bootstrap){
super(bootstrap);
resolver = bootstrap.resolver;
remoteAddress = bootstrap.remoteAddress;
}
```

- config
  属性，BootstrapConfig 对象，启动类配置对象。
- resolver
  属性，地址解析器对象。绝大多数情况下，使用

DEFAULT_RESOLVER
即可。

- remoteAddress
  属性，连接地址。

## []( "3.2 resolver")3.2 resolver

/#resolver(AddressResolverGroup<?> resolver)
方法，设置

resolver
属性。代码如下：

```
public Bootstrap resolver(AddressResolverGroup<?> resolver){
this.resolver = (AddressResolverGroup<SocketAddress>) (resolver == null ? DEFAULT_RESOLVER : resolver);
return this;
}
```

## []( "3.3 remoteAddress")3.3 remoteAddress

/#remoteAddress(...)
方法，设置

remoteAddress
属性。代码如下：

```
public Bootstrap resolver(AddressResolverGroup<?> resolver){
this.resolver = (AddressResolverGroup<SocketAddress>) (resolver == null ? DEFAULT_RESOLVER : resolver);
return this;
}
public Bootstrap remoteAddress(SocketAddress remoteAddress){
this.remoteAddress = remoteAddress;
return this;
}
public Bootstrap remoteAddress(String inetHost, int inetPort){
remoteAddress = InetSocketAddress.createUnresolved(inetHost, inetPort);
return this;
}
public Bootstrap remoteAddress(InetAddress inetHost, int inetPort){
remoteAddress = new InetSocketAddress(inetHost, inetPort);
return this;
}
```

## []( "3.4 validate")3.4 validate

/#validate()
方法，校验配置是否正确。代码如下：

```
@Override
public Bootstrap validate(){
// 父类校验
super.validate();
// handler 非空
if (config.handler() == null) {
throw new IllegalStateException("handler not set");
}
return this;
}
```

- 在

/#connect(...)
方法中，连接服务端时，会调用该方法进行校验。

## []( "3.5 clone")3.5 clone

/#clone(...)
方法，克隆 Bootstrap 对象。代码如下：

```
@Override
public Bootstrap clone(){
return new Bootstrap(this);
}
public Bootstrap clone(EventLoopGroup group){
Bootstrap bs = new Bootstrap(this);
bs.group = group;
return bs;
}
```

- 两个克隆方法，都是调用参数为

bootstrap
为 Bootstrap 构造方法，克隆一个 Bootstrap 对象。差别在于，下面的方法，多了对

group
属性的赋值。

## []( "3.6 connect")3.6 connect

/#connect(...)
方法，连接服务端，即启动客户端。代码如下：

```
public ChannelFuture connect(){
// 校验必要参数
validate();
SocketAddress remoteAddress = this.remoteAddress;
if (remoteAddress == null) {
throw new IllegalStateException("remoteAddress not set");
}
// 解析远程地址，并进行连接
return doResolveAndConnect(remoteAddress, config.localAddress());
}
public ChannelFuture connect(String inetHost, int inetPort){
return connect(InetSocketAddress.createUnresolved(inetHost, inetPort));
}
public ChannelFuture connect(InetAddress inetHost, int inetPort){
return connect(new InetSocketAddress(inetHost, inetPort));
}
public ChannelFuture connect(SocketAddress remoteAddress){
// 校验必要参数
validate();
if (remoteAddress == null) {
throw new NullPointerException("remoteAddress");
}
// 解析远程地址，并进行连接
return doResolveAndConnect(remoteAddress, config.localAddress());
}
public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress){
// 校验必要参数
validate();
if (remoteAddress == null) {
throw new NullPointerException("remoteAddress");
}
// 解析远程地址，并进行连接
return doResolveAndConnect(remoteAddress, localAddress);
}
```

- 该方法返回的是 ChannelFuture 对象，也就是**异步**的连接服务端，启动客户端。如果需要**同步**，则需要调用

ChannelFuture/#sync()
方法。

/#connect(...)
方法，核心流程如下图：

/#bind(...)
方法，核心流程如下图：

[![核心流程](http://static2.iocoder.cn/images/Netty/2018_04_05/01.png)](http://static2.iocoder.cn/images/Netty/2018_04_05/01.png '核心流程')核心流程

- 主要有 5 个步骤，下面我们来拆解代码，看看和我们在 [《精尽 Netty 源码分析 —— NIO 基础（五）之示例》](http://svip.iocoder.cn/Netty/nio-5-demo/?self) 的 NioClient 的代码，是**怎么对应**的。
- 相比

/#bind(...)
方法的流程，主要是**绿色**的 2 个步骤。

### []( "3.6.1 doResolveAndConnect")3.6.1 doResolveAndConnect

/#doResolveAndConnect(final SocketAddress remoteAddress, final SocketAddress localAddress)
方法，代码如下：

```
1: private ChannelFuture doResolveAndConnect(final SocketAddress remoteAddress, final SocketAddress localAddress){
2: // 初始化并注册一个 Channel 对象，因为注册是异步的过程，所以返回一个 ChannelFuture 对象。
3: final ChannelFuture regFuture = initAndRegister();
4: final Channel channel = regFuture.channel();
5:
6: if (regFuture.isDone()) {
7: // 若执行失败，直接进行返回。
8: if (!regFuture.isSuccess()) {
9: return regFuture;
10: }
11: // 解析远程地址，并进行连接
12: return doResolveAndConnect0(channel, remoteAddress, localAddress, channel.newPromise());
13: } else {
14: // Registration future is almost always fulfilled already, but just in case it's not.
15: final PendingRegistrationPromise promise = new PendingRegistrationPromise(channel);
16: regFuture.addListener(new ChannelFutureListener() {
17:
18: @Override
19: public void operationComplete(ChannelFuture future) throws Exception{
20: // Directly obtain the cause and do a null check so we only need one volatile read in case of a
21: // failure.
22: Throwable cause = future.cause();
23: if (cause != null) {
24: // Registration on the EventLoop failed so fail the ChannelPromise directly to not cause an
25: // IllegalStateException once we try to access the EventLoop of the Channel.
26: promise.setFailure(cause);
27: } else {
28: // Registration was successful, so set the correct executor to use.
29: // See https://github.com/netty/netty/issues/2586
30: promise.registered();
31:
32: // 解析远程地址，并进行连接
33: doResolveAndConnect0(channel, remoteAddress, localAddress, promise);
34: }
35: }
36:
37: });
38: return promise;
39: }
40: }
```

- 第 3 行：调用

/#initAndRegister()
方法，初始化并注册一个 Channel 对象。因为注册是**异步**的过程，所以返回一个 ChannelFuture 对象。详细解析，见 [「3.7 initAndRegister」]() 。

- 第 6 至 10 行：若执行失败，直接进行返回

regFuture
对象。

- 第 9 至 37 行：因为注册是**异步**的过程，有可能已完成，有可能未完成。所以实现代码分成了【第 12 行】和【第 13 至 37 行】分别处理已完成和未完成的情况。

- **核心**在【第 12 行】或者【第 33 行】的代码，调用

/#doResolveAndConnect0(final Channel channel, SocketAddress remoteAddress, final SocketAddress localAddress, final ChannelPromise promise)
方法，解析远程地址，并进行连接。

- 如果**异步**注册对应的 ChanelFuture 未完成，则调用

ChannelFuture/#addListener(ChannelFutureListener)
方法，添加监听器，在**注册**完成后，进行回调执行

/#doResolveAndConnect0(...)
方法的逻辑。详细解析，见 [「3.6.2 doResolveAndConnect0」]() 。

- 所以总结来说，**resolve 和 connect 的逻辑，执行在 register 的逻辑之后**。

### []( "3.6.2 doResolveAndConnect0")3.6.2 doResolveAndConnect0

老艿艿：此小节的内容，胖友先看完 [「3.7 initAndRegister」]() 的内容在回过头来看。因为

/#doResolveAndConnect0(...)
方法的执行，在

/#initAndRegister()
方法之后。

/#doResolveAndConnect0(...)
方法，解析远程地址，并进行连接。代码如下：

```
1: private ChannelFuture doResolveAndConnect0(final Channel channel, SocketAddress remoteAddress,
2: final SocketAddress localAddress, final ChannelPromise promise){
3: try {
4: final EventLoop eventLoop = channel.eventLoop();
5: final AddressResolver<SocketAddress> resolver = this.resolver.getResolver(eventLoop);
6:
7: if (!resolver.isSupported(remoteAddress) || resolver.isResolved(remoteAddress)) {
8: // Resolver has no idea about what to do with the specified remote address or it's resolved already.
9: doConnect(remoteAddress, localAddress, promise);
10: return promise;
11: }
12:
13: // 解析远程地址
14: final Future<SocketAddress> resolveFuture = resolver.resolve(remoteAddress);
15:
16: if (resolveFuture.isDone()) {
17: // 解析远程地址失败，关闭 Channel ，并回调通知 promise 异常
18: final Throwable resolveFailureCause = resolveFuture.cause();
19: if (resolveFailureCause != null) {
20: // Failed to resolve immediately
21: channel.close();
22: promise.setFailure(resolveFailureCause);
23: } else {
24: // Succeeded to resolve immediately; cached? (or did a blocking lookup)
25: // 连接远程地址
26: doConnect(resolveFuture.getNow(), localAddress, promise);
27: }
28: return promise;
29: }
30:
31: // Wait until the name resolution is finished.
32: resolveFuture.addListener(new FutureListener<SocketAddress>() {
33: @Override
34: public void operationComplete(Future<SocketAddress> future) throws Exception{
35: // 解析远程地址失败，关闭 Channel ，并回调通知 promise 异常
36: if (future.cause() != null) {
37: channel.close();
38: promise.setFailure(future.cause());
39: // 解析远程地址成功，连接远程地址
40: } else {
41: doConnect(future.getNow(), localAddress, promise);
42: }
43: }
44: });
45: } catch (Throwable cause) {
46: // 发生异常，并回调通知 promise 异常
47: promise.tryFailure(cause);
48: }
49: return promise;
50: }
```

- 第 3 至 14 行：使用

resolver
解析远程地址。因为解析是**异步**的过程，所以返回一个 Future 对象。

- 详细的解析远程地址的代码，考虑到暂时不是本文的重点，所以暂时省略。😈 老艿艿猜测胖友应该也暂时不感兴趣，哈哈哈。
- 第 16 至 44 行：因为注册是**异步**的过程，有可能已完成，有可能未完成。所以实现代码分成了【第 16 至 29 行】和【第 31 至 44 行】分别处理已完成和未完成的情况。

- **核心**在【第 26 行】或者【第 41 行】的代码，调用

/#doConnect(...)
方法，连接远程地址。

- 如果**异步**解析对应的 Future 未完成，则调用

Future/#addListener(FutureListener)
方法，添加监听器，在**解析**完成后，进行回调执行

/#doConnect(...)
方法的逻辑。详细解析，见 见 [「3.13.3 doConnect」]() 。

- 所以总结来说，**connect 的逻辑，执行在 resolve 的逻辑之后**。
- 老艿艿目前使用 [「2. Bootstrap 示例」]() 测试下来，符合【第 16 至 30 行】的条件，即无需走**异步**的流程。

### []( "3.6.3 doConnect")3.6.3 doConnect

/#doConnect(...)
方法，执行 Channel 连接远程地址的逻辑。代码如下：

```
1: private static void doConnect(final SocketAddress remoteAddress, final SocketAddress localAddress, final ChannelPromise connectPromise){
2:
3: // This method is invoked before channelRegistered() is triggered. Give user handlers a chance to set up
4: // the pipeline in its channelRegistered() implementation.
5: final Channel channel = connectPromise.channel();
6: channel.eventLoop().execute(new Runnable() {
7:
8: @Override
9: public void run(){
10: if (localAddress == null) {
11: channel.connect(remoteAddress, connectPromise);
12: } else {
13: channel.connect(remoteAddress, localAddress, connectPromise);
14: }
15: connectPromise.addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
16: }
17:
18: });
19: }
```

- 第 6 行：调用 EventLoop 执行 Channel 连接远程地址的逻辑。但是，实际上当前线程已经是 EventLoop 所在的线程了，为何还要这样操作呢？答案在【第 3 至 4 行】的英语注释。感叹句，Netty 虽然代码量非常庞大且复杂，但是英文注释真的是非常齐全，包括 Github 的 issue 对代码提交的描述，也非常健全。
- 第 10 至 14 行：调用

Channel/#connect(...)
方法，执行 Channel 连接远程地址的逻辑。后续的方法栈调用如下图：[![Channel connect 流程](http://static2.iocoder.cn/images/Netty/2018_04_05/02.png)](http://static2.iocoder.cn/images/Netty/2018_04_05/02.png 'Channel connect 流程')Channel connect 流程

- 还是老样子，我们先省略掉 pipeline 的内部实现代码，从

AbstractNioUnsafe/#connect(final SocketAddress remoteAddress, final SocketAddress localAddress, final ChannelPromise promise)
方法，继续向下分享。

AbstractNioUnsafe/#connect(final SocketAddress remoteAddress, final SocketAddress localAddress, final ChannelPromise promise)
方法，执行 Channel 连接远程地址的逻辑。代码如下：

```
1: @Override
2: public final void connect(final SocketAddress remoteAddress, final SocketAddress localAddress, final ChannelPromise promise){
3: if (!promise.setUncancellable() || !ensureOpen(promise)) {
4: return;
5: }
6:
7: try {
8: // 目前有正在连接远程地址的 ChannelPromise ，则直接抛出异常，禁止同时发起多个连接。
9: if (connectPromise != null) {
10: // Already a connect in process.
11: throw new ConnectionPendingException();
12: }
13:
14: // 记录 Channel 是否激活
15: boolean wasActive = isActive();
16:
17: // 执行连接远程地址
18: if (doConnect(remoteAddress, localAddress)) {
19: fulfillConnectPromise(promise, wasActive);
20: } else {
21: // 记录 connectPromise
22: connectPromise = promise;
23: // 记录 requestedRemoteAddress
24: requestedRemoteAddress = remoteAddress;
25:
26: // 使用 EventLoop 发起定时任务，监听连接远程地址超时。若连接超时，则回调通知 connectPromise 超时异常。
27: // Schedule connect timeout.
28: int connectTimeoutMillis = config().getConnectTimeoutMillis(); // 默认 30 /* 1000 毫秒
29: if (connectTimeoutMillis > 0) {
30: connectTimeoutFuture = eventLoop().schedule(new Runnable() {
31: @Override
32: public void run(){
33: ChannelPromise connectPromise = AbstractNioChannel.this.connectPromise;
34: ConnectTimeoutException cause = new ConnectTimeoutException("connection timed out: " + remoteAddress);
35: if (connectPromise != null && connectPromise.tryFailure(cause)) {
36: close(voidPromise());
37: }
38: }
39: }, connectTimeoutMillis, TimeUnit.MILLISECONDS);
40: }
41:
42: // 添加监听器，监听连接远程地址取消。
43: promise.addListener(new ChannelFutureListener() {
44: @Override
45: public void operationComplete(ChannelFuture future) throws Exception{
46: if (future.isCancelled()) {
47: // 取消定时任务
48: if (connectTimeoutFuture != null) {
49: connectTimeoutFuture.cancel(false);
50: }
51: // 置空 connectPromise
52: connectPromise = null;
53: close(voidPromise());
54: }
55: }
56: });
57: }
58: } catch (Throwable t) {
59: // 回调通知 promise 发生异常
60: promise.tryFailure(annotateConnectException(t, remoteAddress));
61: closeIfClosed();
62: }
63: }
```

- 第 8 至 12 行：目前有正在连接远程地址的 ChannelPromise ，则直接抛出异常，禁止同时发起多个连接。

connectPromise
变量，定义在 AbstractNioChannel 类中，代码如下：

```
//*/*
/* 目前正在连接远程地址的 ChannelPromise 对象。
/*
/* The future of the current connection attempt. If not null, subsequent
/* connection attempts will fail.
/*/
private ChannelPromise connectPromise;
```

- 第 15 行：调用

/#isActive()
方法，获得 Channel 是否激活。NioSocketChannel 对该方法的实现代码如下：

```
@Override
public boolean isActive(){
SocketChannel ch = javaChannel();
return ch.isOpen() && ch.isConnected();
}
```

- 判断 SocketChannel 是否处于打开，并且连接的状态。此时，一般返回的是

false
。

- 第 18 行：调用

/#doConnect(SocketAddress remoteAddress, SocketAddress localAddress)
方法，执行连接远程地址。代码如下：

```
// NioSocketChannel.java
1: @Override
2: protected boolean doConnect(SocketAddress remoteAddress, SocketAddress localAddress) throws Exception{
3: // 绑定本地地址
4: if (localAddress != null) {
5: doBind0(localAddress);
6: }
7:
8: boolean success = false; // 执行是否成功
9: try {
10: // 连接远程地址
11: boolean connected = SocketUtils.connect(javaChannel(), remoteAddress);
12: // 若未连接完成，则关注连接( OP_CONNECT )事件。
13: if (!connected) {
14: selectionKey().interestOps(SelectionKey.OP_CONNECT);
15: }
16: // 标记执行是否成功
17: success = true;
18: // 返回是否连接完成
19: return connected;
20: } finally {
21: // 执行失败，则关闭 Channel
22: if (!success) {
23: doClose();
24: }
25: }
26: }
```

- 第 3 至 6 行：若

localAddress
非空，则调用

/#doBind0(SocketAddress)
方法，绑定本地地址。一般情况下，NIO Client 是不需要绑定本地地址的。默认情况下，系统会随机分配一个可用的本地地址，进行绑定。

- 第 11 行：调用

SocketUtils/#connect(SocketChannel socketChannel, SocketAddress remoteAddress)
方法，Java 原生 NIO SocketChannel 连接 远程地址，并返回是否连接完成( 成功 )。代码如下：

```
public static boolean connect(final SocketChannel socketChannel, final SocketAddress remoteAddress) throws IOException{
try {
return AccessController.doPrivileged(new PrivilegedExceptionAction<Boolean>() {
@Override
public Boolean run() throws IOException{
return socketChannel.connect(remoteAddress);
}
});
} catch (PrivilegedActionException e) {
throw (IOException) e.getCause();
}
}
```

- 可能有胖友有和我一样的疑问，为什么将 connect 操作包在 AccessController 中呢？我们来看下 SocketUtils 类的注释：

```
//*/*
/* Provides socket operations with privileges enabled. This is necessary for applications that use the
/* {@link SecurityManager} to restrict {@link SocketPermission} to their application. By asserting that these
/* operations are privileged, the operations can proceed even if some code in the calling chain lacks the appropriate
/* {@link SocketPermission}.
/*/
```

- 一般情况下，我们用不到，所以也可以暂时不用理解。
- 感兴趣的胖友，可以 Google “AccessController” 关键字，或者阅读 [《Java 安全模型介绍》](https://www.ibm.com/developerworks/cn/java/j-lo-javasecurity/index.html) 。
- 【重要】第 12 至 15 行：若连接未完成(

connected == false
)时，我们可以看到，调用

SelectionKey/#interestOps(ops)
方法，添加连接事件(

SelectionKey.OP_CONNECT
)为感兴趣的事件。也就说，也就是说，当连接远程地址成功时，Channel 对应的 Selector 将会轮询到该事件，可以进一步处理。

- 第 20 至 25 行：若执行失败(

success == false
)时，调用

/#doClose()
方法，关闭 Channel 。

- 第 18 至 19 行：笔者测试下来，

/#doConnect(SocketChannel socketChannel, SocketAddress remoteAddress)
方法的结果为

false
，所以不会执行【第 19 行】代码的

/#fulfillConnectPromise(ChannelPromise promise, boolean wasActive)
方法，而是执行【第 20 至 57 行】的代码逻辑。

- 第 22 行：记录

connectPromise
。

- 第 24 行：记录

requestedRemoteAddress
。

requestedRemoteAddress
变量，在 AbstractNioChannel 类中定义，代码如下：

```
//*/*
/* 正在连接的远程地址
/*/
private SocketAddress requestedRemoteAddress;
```

- 第 26 至 40 行：调用

EventLoop/#schedule(Runnable command, long delay, TimeUnit unit)
方法，发起定时任务

connectTimeoutFuture
，监听连接远程地址**是否超时**。若连接超时，则回调通知

connectPromise
超时异常。

connectPromise
变量，在 AbstractNioChannel 类中定义，代码如下：

```
//*/*
/* 连接超时监听 ScheduledFuture 对象。
/*/
private ScheduledFuture<?> connectTimeoutFuture;
```

- 第 42 至 57 行：调用

ChannelPromise/#addListener(ChannelFutureListener)
方法，添加监听器，监听连接远程地址**是否取消**。若取消，则取消

connectTimeoutFuture
任务，并置空

connectPromise
。这样，客户端 Channel 可以发起下一次连接。

### []( "3.6.4 finishConnect")3.6.4 finishConnect

看到此处，可能胖友会有疑问，客户端的连接在哪里完成呢？答案在

AbstractNioUnsafe/#finishConnect()
方法中。而该方法通过 Selector 轮询到

SelectionKey.OP_CONNECT
事件时，进行触发。调用栈如下图：[![finishConnect 调用栈](http://static2.iocoder.cn/images/Netty/2018_04_05/03.png)](http://static2.iocoder.cn/images/Netty/2018_04_05/03.png 'finishConnect 调用栈')finishConnect 调用栈

```
/* 哈哈哈，还是老样子，我们先省略掉 EventLoop 的内部实现代码，从 `AbstractNioUnsafe/#finishConnect()` 方法，继续向下分享。
```

AbstractNioUnsafe/#finishConnect()
方法，完成客户端的连接。代码如下：

```
1: @Override
2: public final void finishConnect(){
3: // Note this method is invoked by the event loop only if the connection attempt was
4: // neither cancelled nor timed out.
5: // 判断是否在 EventLoop 的线程中。
6: assert eventLoop().inEventLoop();
7:
8: try {
9: // 获得 Channel 是否激活
10: boolean wasActive = isActive();
11: // 执行完成连接
12: doFinishConnect();
13: // 通知 connectPromise 连接完成
14: fulfillConnectPromise(connectPromise, wasActive);
15: } catch (Throwable t) {
16: // 通知 connectPromise 连接异常
17: fulfillConnectPromise(connectPromise, annotateConnectException(t, requestedRemoteAddress));
18: } finally {
19: // 取消 connectTimeoutFuture 任务
20: // Check for null as the connectTimeoutFuture is only created if a connectTimeoutMillis > 0 is used
21: // See https://github.com/netty/netty/issues/1770
22: if (connectTimeoutFuture != null) {
23: connectTimeoutFuture.cancel(false);
24: }
25: // 置空 connectPromise
26: connectPromise = null;
27: }
28: }
```

- 第 6 行：判断是否在 EventLoop 的线程中。
- 第 10 行：调用

/#isActive()
方法，获得 Channel 是否激活。笔者调试时，此时返回

false
，因为连接还没完成。

- 第 12 行：调用

/#doFinishConnect()
方法，执行完成连接的逻辑。详细解析，见 [「3.6.4.1 doFinishConnect」]() 。

- 第 14 行：执行完成连接**成功**，调用

/#fulfillConnectPromise(ChannelPromise promise, boolean wasActive)
方法，通知

connectPromise
连接完成。详细解析，见 [「3.6.4.2 fulfillConnectPromise 成功」]() 。

- 第 15 至 17 行：执行完成连接**异常**，调用

/#fulfillConnectPromise(ChannelPromise promise, Throwable cause)
方法，通知

connectPromise
连接异常。详细解析，见 [「3.6.4.3 fulfillConnectPromise 异常」]() 。

- 第 18 至 27 行：执行完成连接**结束**，取消

connectTimeoutFuture
任务，并置空

connectPromise
。

### []( "3.6.4.1 doFinishConnect")3.6.4.1 doFinishConnect

NioSocketChannel/#doFinishConnect()
方法，执行完成连接的逻辑。代码如下：

```
@Override
protected void doFinishConnect() throws Exception{
if (!javaChannel().finishConnect()) {
throw new Error();
}
}
```

- 【重要】是不是非常熟悉的，调用

SocketChannel/#finishConnect()
方法，完成连接。😈 美滋滋。

### []( "3.6.4.2 fulfillConnectPromise 成功")3.6.4.2 fulfillConnectPromise 成功

AbstractNioUnsafe/#fulfillConnectPromise(ChannelPromise promise, Throwable cause)
方法，通知

connectPromise
连接完成。代码如下：

```
1: private void fulfillConnectPromise(ChannelPromise promise, boolean wasActive){
2: if (promise == null) {
3: // Closed via cancellation and the promise has been notified already.
4: return;
5: }
6:
7: // 获得 Channel 是否激活
8: // Get the state as trySuccess() may trigger an ChannelFutureListener that will close the Channel.
9: // We still need to ensure we call fireChannelActive() in this case.
10: boolean active = isActive();
11:
12: // 回调通知 promise 执行成功
13: // trySuccess() will return false if a user cancelled the connection attempt.
14: boolean promiseSet = promise.trySuccess();
15:
16: // 若 Channel 是新激活的，触发通知 Channel 已激活的事件。
17: // Regardless if the connection attempt was cancelled, channelActive() event should be triggered,
18: // because what happened is what happened.
19: if (!wasActive && active) {
20: pipeline().fireChannelActive();
21: }
22:
23: // If a user cancelled the connection attempt, close the channel, which is followed by channelInactive().
24: // TODO 芋艿
25: if (!promiseSet) {
26: close(voidPromise());
27: }
28: }
```

- 第 10 行：调用

/#isActive()
方法，获得 Channel 是否激活。笔者调试时，此时返回

true
，因为连接已经完成。

- 第 14 行：回调通知

promise
执行成功。此处的通知，对应回调的是我们添加到

/#connect(...)
方法返回的 ChannelFuture 的 ChannelFutureListener 的监听器。示例代码如下：

```
ChannelFuture f = b.connect(HOST, PORT).addListener(new ChannelFutureListener() { // 回调的就是我！！！
@Override
public void operationComplete(ChannelFuture future) throws Exception{
System.out.println("连接完成");
}
}).sync();
```

- 第 19 行：因为

wasActive == false
并且

active == true
，因此，Channel 可以认为是**新激活**的，满足【第 20 行】代码的执行条件。

- 第 40 行：调用

DefaultChannelPipeline/#fireChannelActive()
方法，触发 Channel 激活的事件。【重要】后续的流程，和 NioServerSocketChannel 一样，也就说，会调用到

AbstractUnsafe/#beginRead()
方法。这意味着什么呢？将我们创建 NioSocketChannel 时，设置的

readInterestOp = SelectionKey.OP_READ
添加为感兴趣的事件。也就说，客户端可以读取服务端发送来的数据。

- 关于

AbstractUnsafe/#beginRead()
方法的解析，见 [《精尽 Netty 源码分析 —— 启动（一）之服务端》的 「3.13.3 beginRead」](http://svip.iocoder.cn/Netty/bootstrap-1-server/?self) 部分。

- 第 23 至 27 行：TODO 芋艿 1004 fulfillConnectPromise promiseSet

### []( "3.6.4.3 fulfillConnectPromise 异常")3.6.4.3 fulfillConnectPromise 异常

/#fulfillConnectPromise(ChannelPromise promise, Throwable cause)
方法，通知

connectPromise
连接异常。代码如下：

```
private void fulfillConnectPromise(ChannelPromise promise, Throwable cause){
if (promise == null) {
// Closed via cancellation and the promise has been notified already.
return;
}
// 回调通知 promise 发生异常
// Use tryFailure() instead of setFailure() to avoid the race against cancel().
promise.tryFailure(cause);
// 关闭
closeIfClosed();
}
```

- 比较简单，已经添加中文注释，胖友自己查看。

## []( "3.7 initAndRegister")3.7 initAndRegister

Bootstrap 继承 AbstractBootstrap 抽象类，所以

/#initAndRegister()
方法的流程上是一致的。所以和 ServerBootstrap 的差别在于：

1. 创建的 Channel 对象不同。
1. 初始化 Channel 配置的代码实现不同。

### []( "3.7.1 创建 Channel 对象")3.7.1 创建 Channel 对象

考虑到本文的内容，我们以 NioSocketChannel 的创建过程作为示例。创建 NioSocketChannel 对象的流程，和 NioServerSocketChannel 基本是一致的，所以流程图我们就不提供了，直接开始撸源码。

### []( "3.7.1.1 NioSocketChannel")3.7.1.1 NioSocketChannel

```
private static final SelectorProvider DEFAULT_SELECTOR_PROVIDER = SelectorProvider.provider();
private final SocketChannelConfig config;
public NioSocketChannel(){
this(DEFAULT_SELECTOR_PROVIDER);
}
public NioSocketChannel(SelectorProvider provider){
this(newSocket(provider));
}
```

- DEFAULT_SELECTOR_PROVIDER
  **静态**属性，默认的 SelectorProvider 实现类。
- config
  属性，Channel 对应的配置对象。每种 Channel 实现类，也会对应一个 ChannelConfig 实现类。例如，NioSocketChannel 类，对应 SocketChannelConfig 配置类。
- 在构造方法中，调用

/#newSocket(SelectorProvider provider)
方法，创建 NIO 的 ServerSocketChannel 对象。代码如下：

```
private static SocketChannel newSocket(SelectorProvider provider){
try {
//*/*
/* Use the {@link SelectorProvider} to open {@link SocketChannel} and so remove condition in
/* {@link SelectorProvider/#provider()} which is called by each SocketChannel.open() otherwise.
/*
/* See <a href="https://github.com/netty/netty/issues/2308">/#2308</a>.
/*/
return provider.openSocketChannel();
} catch (IOException e) {
throw new ChannelException("Failed to open a socket.", e);
}
}
```

- 😈 是不是很熟悉这样的代码，效果和

SocketChannel/#open()
方法创建 SocketChannel 对象是一致。

- /#NioSocketChannel(SocketChannel channel)
  构造方法，代码如下：

```
public NioSocketChannel(SocketChannel socket){
this(null, socket);
}
public NioSocketChannel(Channel parent, SocketChannel socket){
super(parent, socket);
config = new NioSocketChannelConfig(this, socket.socket());
}
```

- 调用父 AbstractNioByteChannel 的构造方法。详细解析，见 [「3.7.1.2 AbstractNioByteChannel」]() 。
- 初始化

config
属性，创建 NioSocketChannelConfig 对象。

### []( "3.7.1.2 AbstractNioByteChannel")3.7.1.2 AbstractNioByteChannel

```
protected AbstractNioByteChannel(Channel parent, SelectableChannel ch){
super(parent, ch, SelectionKey.OP_READ);
}
```

- 调用父 AbstractNioChannel 的构造方法。后续的构造方法，和 NioServerSocketChannel 是一致的。

- 注意传入的 SelectionKey 的值为

OP_READ
。

### []( "3.7.2 初始化 Channel 配置")3.7.2 初始化 Channel 配置

/#init(Channel channel)
方法，初始化 Channel 配置。代码如下：

```
@Override
void init(Channel channel) throws Exception{
ChannelPipeline p = channel.pipeline();
// 添加处理器到 pipeline 中
p.addLast(config.handler());
// 初始化 Channel 的可选项集合
final Map<ChannelOption<?>, Object> options = options0();
synchronized (options) {
setChannelOptions(channel, options, logger);
}
// 初始化 Channel 的属性集合
final Map<AttributeKey<?>, Object> attrs = attrs0();
synchronized (attrs) {
for (Entry<AttributeKey<?>, Object> e: attrs.entrySet()) {
channel.attr((AttributeKey<Object>) e.getKey()).set(e.getValue());
}
}
}
```

- 比较简单，已经添加中文注释，胖友自己查看。

# []( "666. 彩蛋")666. 彩蛋

撸完 Netty 服务端启动之后，再撸 Netty 客户端启动之后，出奇的顺手。美滋滋。

另外，也推荐如下和 Netty 客户端启动相关的文章，以加深理解：

- 杨武兵 [《Netty 源码分析系列 —— Bootstrap》](https://my.oschina.net/ywbrj042/blog/868798)
- 永顺 [《Netty 源码分析之 一 揭开 Bootstrap 神秘的红盖头 (客户端)》](https://segmentfault.com/a/1190000007282789)
