# 精尽 Netty 源码解析 —— EventLoop（二）之 EventLoopGroup

# []( "1. 概述")1. 概述

在 [《精尽 Netty 源码分析 —— Netty 简介（二）之核心组件》](http://svip.iocoder.cn/Netty/intro-2/?self) 中，对 EventLoopGroup 和 EventLoop 做了定义，我们再来回顾下：

- Channel 为 Netty 网络操作抽象类，EventLoop 负责处理注册到其上的 Channel 处理 I/O 操作，两者配合参与 I/O 操作。
- EventLoopGroup 是一个 EventLoop 的分组，它可以获取到一个或者多个 EventLoop 对象，因此它提供了迭代出 EventLoop 对象的方法。

在 [《精尽 Netty 源码分析 —— 启动》]() 中，我们特别熟悉的一段代码就是：

- new NioEventLoopGroup()
  ，创建一个 EventLoopGroup 对象。
- EventLoopGroup/#register(Channel channel)
  ，将 Channel 注册到 EventLoopGroup 上。

那么，本文我们分享 EventLoopGroup 的具体代码实现，来一探究竟。

# []( "2. 类结构图")2. 类结构图

EventLoopGroup 的整体类结构如下图：

[![EventLoopGroup 类图](http://static2.iocoder.cn/images/Netty/2018_05_04/01.png)](http://static2.iocoder.cn/images/Netty/2018_05_04/01.png 'EventLoopGroup 类图')EventLoopGroup 类图

- 红框部分，为 EventLoopGroup 相关的类关系。其他部分，为 EventLoop 相关的类关系。
- 因为我们实际上使用的是 **NioEventLoopGroup** 和 NioEventLoop ，所以笔者省略了其它相关的类，例如 OioEventLoopGroup、EmbeddedEventLoop 等等。

下面，我们逐层看看每个接口和类的实现代码。

# []( "3. EventExecutorGroup")3. EventExecutorGroup

io.netty.util.concurrent.EventExecutorGroup
，实现 Iterable、ScheduledExecutorService 接口，EventExecutor ( 事件执行器 )的分组接口。代码如下：

```
// ========== 自定义接口 ==========
boolean isShuttingDown();
// 优雅关闭
Future<?> shutdownGracefully();
Future<?> shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit);
Future<?> terminationFuture();
// 选择一个 EventExecutor 对象
EventExecutor next();
// ========== 实现自 Iterable 接口 ==========
@Override
Iterator<EventExecutor> iterator();
// ========== 实现自 ExecutorService 接口 ==========
@Override
Future<?> submit(Runnable task);
@Override
<T> Future<T> submit(Runnable task, T result);
@Override
<T> Future<T> submit(Callable<T> task);
@Override
@Deprecated
void shutdown();
@Override
@Deprecated
List<Runnable> shutdownNow();
// ========== 实现自 ScheduledExecutorService 接口 ==========
@Override
ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit);
@Override
<V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit);
@Override
ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit);
@Override
ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit);
```

- 每个接口的方法的意思比较好理解，笔者就不一一赘述了。
- 比较特殊的是，接口方法返回类型为 Future 不是 Java 原生的

java.util.concurrent.Future
，而是 Netty 自己实现的 Future 接口。详细解析，见后续文章。

- EventExecutorGroup 自身不执行任务，而是将任务

/#submit(...)
或

/#schedule(...)
给自己管理的 EventExecutor 的分组。至于提交给哪一个 EventExecutor ，一般是通过

/#next()
方法，选择一个 EventExecutor 。

# []( "4. AbstractEventExecutorGroup")4. AbstractEventExecutorGroup

io.netty.util.concurrent.AbstractEventExecutorGroup
，实现 EventExecutorGroup 接口，EventExecutor ( 事件执行器 )的分组抽象类。

## []( "4.1 submit")4.1 submit

/#submit(...)
方法，提交**一个**普通任务到 EventExecutor 中。代码如下：

```
@Override
public Future<?> submit(Runnable task) {
return next().submit(task);
}
@Override
public <T> Future<T> submit(Runnable task, T result){
return next().submit(task, result);
}
@Override
public <T> Future<T> submit(Callable<T> task){
return next().submit(task);
}
```

- 提交的 EventExecutor ，通过

/#next()
方法选择。

## []( "4.2 schedule")4.2 schedule

/#schedule(...)
方法，提交**一个**定时任务到 EventExecutor 中。代码如下：

```
@Override
public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
return next().schedule(command, delay, unit);
}
@Override
public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit){
return next().schedule(callable, delay, unit);
}
@Override
public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
return next().scheduleAtFixedRate(command, initialDelay, period, unit);
}
@Override
public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
return next().scheduleWithFixedDelay(command, initialDelay, delay, unit);
}
```

- 提交的 EventExecutor ，通过

/#next()
方法选择。

## []( "4.3 execute")4.3 execute

/#execute(...)
方法，在 EventExecutor 中执行**一个**普通任务。代码如下：

```
@Override
public void execute(Runnable command){
next().execute(command);
}
```

- 执行的 EventExecutor ，通过

/#next()
方法选择。

- 看起来

/#execute(...)
和

/#submit(...)
方法有几分相似，具体的差异，由 EventExecutor 的实现决定。

## []( "4.4 invokeAll")4.4 invokeAll

/#invokeAll(...)
方法，在 EventExecutor 中执行**多个**普通任务。代码如下：

```
@Override
public <T> List<java.util.concurrent.Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
return next().invokeAll(tasks);
}
@Override
public <T> List<java.util.concurrent.Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
return next().invokeAll(tasks, timeout, unit);
}
```

- 执行的 EventExecutor ，通过

/#next()
方法选择。并且，多个任务使用同一个 EventExecutor 。

## []( "4.5 invokeAny")4.5 invokeAny

/#invokeAll(...)
方法，在 EventExecutor 中执行**多个**普通任务，有**一个**执行完成即可。代码如下：

```
@Override
public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException{
return next().invokeAny(tasks);
}
@Override
public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException{
return next().invokeAny(tasks, timeout, unit);
}
```

- 执行的 EventExecutor ，通过

/#next()
方法选择。并且，多个任务使用同一个 EventExecutor 。

## []( "4.6 shutdown")4.6 shutdown

/#shutdown(...)
方法，关闭 EventExecutorGroup 。代码如下：

```
@Override
public Future<?> shutdownGracefully() {
return shutdownGracefully(DEFAULT_SHUTDOWN_QUIET_PERIOD //* 2 /*/, DEFAULT_SHUTDOWN_TIMEOUT //* 15 /*/, TimeUnit.SECONDS);
}
@Override
@Deprecated
public List<Runnable> shutdownNow(){
shutdown();
return Collections.emptyList();
}
@Override
@Deprecated
public abstract void shutdown();
```

- 具体的

/#shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit)
和

/#shutdown()
方法，由子类实现。

# []( "5. MultithreadEventExecutorGroup")5. MultithreadEventExecutorGroup

io.netty.util.concurrent.MultithreadEventExecutorGroup
，继承 AbstractEventExecutorGroup 抽象类，**基于多线程**的 EventExecutor ( 事件执行器 )的分组抽象类。

## []( "5.1 构造方法")5.1 构造方法

```
//*/*
/* EventExecutor 数组
/*/
private final EventExecutor[] children;
//*/*
/* 不可变( 只读 )的 EventExecutor 数组
/*
/* @see /#MultithreadEventExecutorGroup(int, Executor, EventExecutorChooserFactory, Object...)
/*/
private final Set<EventExecutor> readonlyChildren;
//*/*
/* 已终止的 EventExecutor 数量
/*/
private final AtomicInteger terminatedChildren = new AtomicInteger();
//*/*
/* 用于终止 EventExecutor 的异步 Future
/*/
private final Promise<?> terminationFuture = new DefaultPromise(GlobalEventExecutor.INSTANCE);
//*/*
/* EventExecutor 选择器
/*/
private final EventExecutorChooserFactory.EventExecutorChooser chooser;
protected MultithreadEventExecutorGroup(int nThreads, ThreadFactory threadFactory, Object... args){
this(nThreads, threadFactory == null ? null : new ThreadPerTaskExecutor(threadFactory), args);
}
protected MultithreadEventExecutorGroup(int nThreads, Executor executor, Object... args){
this(nThreads, executor, DefaultEventExecutorChooserFactory.INSTANCE, args);
}
1: protected MultithreadEventExecutorGroup(int nThreads, Executor executor, EventExecutorChooserFactory chooserFactory, Object... args){
2: if (nThreads <= 0) {
3: throw new IllegalArgumentException(String.format("nThreads: %d (expected: > 0)", nThreads));
4: }
5:
6: // 创建执行器
7: if (executor == null) {
8: executor = new ThreadPerTaskExecutor(newDefaultThreadFactory());
9: }
10:
11: // 创建 EventExecutor 数组
12: children = new EventExecutor[nThreads];
13:
14: for (int i = 0; i < nThreads; i ++) {
15: boolean success = false; // 是否创建成功
16: try {
17: // 创建 EventExecutor 对象
18: children[i] = newChild(executor, args);
19: // 标记创建成功
20: success = true;
21: } catch (Exception e) {
22: // 创建失败，抛出 IllegalStateException 异常
23: // TODO: Think about if this is a good exception type
24: throw new IllegalStateException("failed to create a child event loop", e);
25: } finally {
26: // 创建失败，关闭所有已创建的 EventExecutor
27: if (!success) {
28: // 关闭所有已创建的 EventExecutor
29: for (int j = 0; j < i; j ++) {
30: children[j].shutdownGracefully();
31: }
32: // 确保所有已创建的 EventExecutor 已关闭
33: for (int j = 0; j < i; j ++) {
34: EventExecutor e = children[j];
35: try {
36: while (!e.isTerminated()) {
37: e.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
38: }
39: } catch (InterruptedException interrupted) {
40: // Let the caller handle the interruption.
41: Thread.currentThread().interrupt();
42: break;
43: }
44: }
45: }
46: }
47: }
48:
49: // 创建 EventExecutor 选择器
50: chooser = chooserFactory.newChooser(children);
51:
52: // 创建监听器，用于 EventExecutor 终止时的监听
53: final FutureListener<Object> terminationListener = new FutureListener<Object>() {
54:
55: @Override
56: public void operationComplete(Future<Object> future) throws Exception{
57: if (terminatedChildren.incrementAndGet() == children.length) { // 全部关闭
58: terminationFuture.setSuccess(null); // 设置结果，并通知监听器们。
59: }
60: }
61:
62: };
63: // 设置监听器到每个 EventExecutor 上
64: for (EventExecutor e: children) {
65: e.terminationFuture().addListener(terminationListener);
66: }
67:
68: // 创建不可变( 只读 )的 EventExecutor 数组
69: Set<EventExecutor> childrenSet = new LinkedHashSet<EventExecutor>(children.length);
70: Collections.addAll(childrenSet, children);
71: readonlyChildren = Collections.unmodifiableSet(childrenSet);
72: }
```

- 每个属性的定义，胖友直接看代码注释。
- 方法参数

executor
，执行器。详细解析，见 [「5.2 ThreadPerTaskExecutor」]() 。

- 第 6 至 9 行：若

executor
为空，则创建执行器。

- 第 12 行：创建 EventExecutor 数组。

- 第 18 行：调用

/#newChild(Executor executor, Object... args)
方法，创建 EventExecutor 对象，然后设置到数组中。

- 第 21 至 24 行：创建失败，抛出 IllegalStateException 异常。
- 第 25 至 45 行：创建失败，关闭所有已创建的 EventExecutor 。
- 第 50 行：调用

EventExecutorChooserFactory/#newChooser(EventExecutor[] executors)
方法，创建 EventExecutor 选择器。详细解析，见 [「5.3 EventExecutorChooserFactory」]() 。

- 第 52 至 62 行：创建监听器，用于 EventExecutor 终止时的监听。

- 第 55 至 60 行：回调的具体逻辑是，当所有 EventExecutor 都终止完成时，通过调用

Future/#setSuccess(V result)
方法，通知监听器们。至于为什么设置的值是

null
，因为监听器们不关注具体的结果。

- 第 63 至 66 行：设置监听器到每个 EventExecutor 上。
- 第 68 至 71 行：创建不可变( 只读 )的 EventExecutor 数组。

## []( "5.2 ThreadPerTaskExecutor")5.2 ThreadPerTaskExecutor

io.netty.util.concurrent.ThreadPerTaskExecutor
，实现 Executor 接口，每个任务一个线程的执行器实现类。代码如下：

```
public final class ThreadPerTaskExecutor implements Executor{
//*/*
/* 线程工厂对象
/*/
private final ThreadFactory threadFactory;
public ThreadPerTaskExecutor(ThreadFactory threadFactory){
if (threadFactory == null) {
throw new NullPointerException("threadFactory");
}
this.threadFactory = threadFactory;
}
//*/*
/* 执行任务
/*
/* @param command 任务
/*/
@Override
public void execute(Runnable command){
threadFactory.newThread(command).start();
}
}
```

- threadFactory
  属性，线程工厂对象。Netty 实现自定义的 ThreadFactory 类，为

io.netty.util.concurrent.DefaultThreadFactory
。关于 DefaultThreadFactory 比较简单，胖友可以自己看看。

- /#execute(Runnable command)
  方法，通过

ThreadFactory/#newThread(Runnable)
方法，创建一个 Thread ，然后调用

Thread/#start()
方法，**启动线程执行任务**。

## []( "5.3 EventExecutorChooserFactory")5.3 EventExecutorChooserFactory

io.netty.util.concurrent.EventExecutorChooserFactory
，EventExecutorChooser 工厂接口。代码如下：

```
public interface EventExecutorChooserFactory{
//*/*
/* 创建一个 EventExecutorChooser 对象
/*
/* Returns a new {@link EventExecutorChooser}.
/*/
EventExecutorChooser newChooser(EventExecutor[] executors);
//*/*
/* EventExecutor 选择器接口
/*
/* Chooses the next {@link EventExecutor} to use.
/*/
@UnstableApi
interface EventExecutorChooser{
//*/*
/* 选择下一个 EventExecutor 对象
/*
/* Returns the new {@link EventExecutor} to use.
/*/
EventExecutor next();
}
}
```

- /#newChooser(EventExecutor[] executors)
  方法，创建一个 EventExecutorChooser 对象。
- EventExecutorChooser 接口，EventExecutor 选择器接口。

- /#next()
  方法，选择下一个 EventExecutor 对象。

### []( "5.3.1 DefaultEventExecutorChooserFactory")5.3.1 DefaultEventExecutorChooserFactory

io.netty.util.concurrent.DefaultEventExecutorChooserFactory
，实现 EventExecutorChooserFactory 接口，默认 EventExecutorChooser 工厂实现类。代码如下

```
//*/*
/* 单例
/*/
public static final DefaultEventExecutorChooserFactory INSTANCE = new DefaultEventExecutorChooserFactory();
private DefaultEventExecutorChooserFactory(){ }
@SuppressWarnings("unchecked")
@Override
public EventExecutorChooser newChooser(EventExecutor[] executors){
if (isPowerOfTwo(executors.length)) { // 是否为 2 的幂次方
return new PowerOfTwoEventExecutorChooser(executors);
} else {
return new GenericEventExecutorChooser(executors);
}
}
private static boolean isPowerOfTwo(int val){
return (val & -val) == val;
}
```

- INSTANCE
  **静态**属性，单例。
- /#newChooser(EventExecutor[] executors)
  方法，调用

/#isPowerOfTwo(int val)
方法，判断 EventExecutor 数组的大小是否为 2 的幂次方。

- 若是，创建 PowerOfTwoEventExecutorChooser 对象。详细解析，见 [「5.3.3 PowerOfTwoEventExecutorChooser」]() 。
- 若否，创建 GenericEventExecutorChooser 对象。详细解析，见 [「5.3.2 GenericEventExecutorChooser」]() 。
- /#isPowerOfTwo(int val)
  方法，为什么

(val & -val) == val
可以判断数字是否为 2 的幂次方呢？

- 我们以 8 来举个例子。

- 8 的二进制为

1000
。

- -8 的二进制使用补码表示。所以，先求反生成反码为

0111
，然后加一生成补码为

1000
。

- 8 和 -8 并操作后，还是 8 。
- 实际上，以 2 为幂次方的数字，都是最高位为 1 ，剩余位为 0 ，所以对应的负数，求完补码还是自己。
- 胖友也可以自己试试非 2 的幂次方数字的效果。

### []( "5.3.2 GenericEventExecutorChooser")5.3.2 GenericEventExecutorChooser

GenericEventExecutorChooser 实现 EventExecutorChooser 接口，通用的 EventExecutor 选择器实现类。代码如下：
GenericEventExecutorChooser 内嵌在 DefaultEventExecutorChooserFactory 类中。

```
private static final class GenericEventExecutorChooser implements EventExecutorChooser{
//*/*
/* 自增序列
/*/
private final AtomicInteger idx = new AtomicInteger();
//*/*
/* EventExecutor 数组
/*/
private final EventExecutor[] executors;
GenericEventExecutorChooser(EventExecutor[] executors) {
this.executors = executors;
}
@Override
public EventExecutor next(){
return executors[Math.abs(idx.getAndIncrement() % executors.length)];
}
}
```

- 实现比较**简单**，使用

idx
自增，并使用 EventExecutor 数组的大小来取余。

### []( "5.3.3 PowerOfTwoEventExecutorChooser")5.3.3 PowerOfTwoEventExecutorChooser

PowerOfTwoEventExecutorChooser 实现 EventExecutorChooser 接口，基于 EventExecutor 数组的大小为 2 的幂次方的 EventExecutor 选择器实现类。这是一个优化的实现，代码如下：
PowerOfTwoEventExecutorChooser 内嵌在 DefaultEventExecutorChooserFactory 类中。

```
private static final class PowerOfTwoEventExecutorChooser implements EventExecutorChooser{
//*/*
/* 自增序列
/*/
private final AtomicInteger idx = new AtomicInteger();
//*/*
/* EventExecutor 数组
/*/
private final EventExecutor[] executors;
PowerOfTwoEventExecutorChooser(EventExecutor[] executors) {
this.executors = executors;
}
@Override
public EventExecutor next(){
return executors[idx.getAndIncrement() & executors.length - 1];
}
}
```

- 实现比较**巧妙**，通过

idx
自增，并使用【EventExecutor 数组的大小 - 1】进行进行

&
并操作。

- 因为

-
( 二元操作符 ) 的计算优先级高于

&
( 一元操作符 ) 。

- 因为 EventExecutor 数组的大小是以 2 为幂次方的数字，那么减一后，除了最高位是 0 ，剩余位都为 1 ( 例如 8 减一后等于 7 ，而 7 的二进制为 0111 。)，那么无论

idx
无论如何递增，再进行

&
并操作，都不会超过 EventExecutor 数组的大小。并且，还能保证顺序递增。

## []( "5.4 newDefaultThreadFactory")5.4 newDefaultThreadFactory

/#newDefaultThreadFactory()
方法，创建线程工厂对象。代码如下：

```
protected ThreadFactory newDefaultThreadFactory(){
return new DefaultThreadFactory(getClass());
}
```

- 创建的对象为 DefaultThreadFactory ，并且使用类名作为

poolType
。

## []( "5.5 next")5.5 next

/#next()
方法，选择下一个 EventExecutor 对象。代码如下：

```
@Override
public EventExecutor next(){
return chooser.next();
}
```

## []( "5.6 iterator")5.6 iterator

/#iterator()
方法，获得 EventExecutor 数组的迭代器。代码如下：

```
@Override
public Iterator<EventExecutor> iterator(){
return readonlyChildren.iterator();
}
```

- 为了避免调用方，获得迭代器后，对 EventExecutor 数组进行修改，所以返回是**不可变**的 EventExecutor 数组

readonlyChildren
的迭代器。

## []( "5.7 executorCount")5.7 executorCount

/#executorCount()
方法，获得 EventExecutor 数组的大小。代码如下：

```
public final int executorCount(){
return children.length;
}
```

## []( "5.8 newChild")5.8 newChild

/#newChild(Executor executor, Object... args)
**抽象**方法，创建 EventExecutor 对象。代码如下：

```
protected abstract EventExecutor newChild(Executor executor, Object... args) throws Exception;
```

- 子类实现该方法，创建其对应的 EventExecutor 实现类的对象。

## []( "5.9 关闭相关方法")5.9 关闭相关方法

如下是关闭相关的方法，比较简单，胖友自己研究：

- /#terminationFuture()
- /#shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit)
- /#shutdown()
- /#awaitTermination(long timeout, TimeUnit unit)
- /#isShuttingDown()
- /#isShutdown()
- /#isTerminated()

# []( "6. EventLoopGroup")6. EventLoopGroup

io.netty.channel.EventExecutorGroup
，继承 EventExecutorGroup 接口，EventLoop 的分组接口。代码如下：

```
// ========== 自定义接口 ==========
//*/*
/* Register a {@link Channel} with this {@link EventLoop}. The returned {@link ChannelFuture}
/* will get notified once the registration was complete.
/*/
ChannelFuture register(Channel channel);
ChannelFuture register(ChannelPromise promise);
@Deprecated
ChannelFuture register(Channel channel, ChannelPromise promise);
// ========== 实现自 EventExecutorGroup 接口 ==========
@Override
EventLoop next();
```

- /#next()
  方法，选择下一个 EventLoop 对象。
- /#register(...)
  方法，注册 Channel 到 EventLoopGroup 中。实际上，EventLoopGroup 会分配一个 EventLoop 给该 Channel 注册。

# []( "7. MultithreadEventLoopGroup")7. MultithreadEventLoopGroup

io.netty.channel.MultithreadEventLoopGroup
，实现 EventLoopGroup 接口，继承 MultithreadEventExecutorGroup 抽象类，**基于多线程**的 EventLoop 的分组抽象类。

## []( "7.1 构造方法")7.1 构造方法

```
//*/*
/* 默认 EventLoop 线程数
/*/
private static final int DEFAULT_EVENT_LOOP_THREADS;
static {
DEFAULT_EVENT_LOOP_THREADS = Math.max(1, SystemPropertyUtil.getInt("io.netty.eventLoopThreads", NettyRuntime.availableProcessors() /* 2));
if (logger.isDebugEnabled()) {
logger.debug("-Dio.netty.eventLoopThreads: {}", DEFAULT_EVENT_LOOP_THREADS);
}
}
protected MultithreadEventLoopGroup(int nThreads, Executor executor, Object... args){
super(nThreads == 0 ? DEFAULT_EVENT_LOOP_THREADS : nThreads, executor, args);
}
protected MultithreadEventLoopGroup(int nThreads, ThreadFactory threadFactory, Object... args){
super(nThreads == 0 ? DEFAULT_EVENT_LOOP_THREADS : nThreads, threadFactory, args);
}
protected MultithreadEventLoopGroup(int nThreads, Executor executor, EventExecutorChooserFactory chooserFactory, Object... args){
super(nThreads == 0 ? DEFAULT_EVENT_LOOP_THREADS : nThreads, executor, chooserFactory, args);
}
```

- DEFAULT_EVENT_LOOP_THREADS
  属性，EventLoopGroup 默认拥有的 EventLoop 数量。因为一个 EventLoop 对应一个线程，所以为 CPU 数量 /\* 2 。

- 为什么会 /\* 2 呢？因为目前 CPU 基本都是超线程，**一个 CPU 可对应 2 个线程**。
- 在构造方法未传入

nThreads
方法参数时，使用

DEFAULT_EVENT_LOOP_THREADS
。

## []( "7.2 newDefaultThreadFactory")7.2 newDefaultThreadFactory

newDefaultThreadFactory

/#newDefaultThreadFactory()
方法，创建线程工厂对象。代码如下：

```
@Override
protected ThreadFactory newDefaultThreadFactory(){
return new DefaultThreadFactory(getClass(), Thread.MAX_PRIORITY);
}
```

- 覆盖父类方法，增加了线程优先级为

Thread.MAX_PRIORITY
。

## []( "7.3 next")7.3 next

/#next()
方法，选择下一个 EventLoop 对象。代码如下：

```
@Override
public EventLoop next(){
return (EventLoop) super.next();
}
```

- 覆盖父类方法，将返回值转换成 EventLoop 类。

## []( "7.4 newChild")7.4 newChild

/#newChild(Executor executor, Object... args)
**抽象**方法，创建 EventExecutor 对象。代码如下：

```
@Override
protected abstract EventLoop newChild(Executor executor, Object... args) throws Exception;
```

- 覆盖父类方法，返回值改为 EventLoop 类。

## []( "7.5 register")7.5 register

/#register()
方法，注册 Channel 到 EventLoopGroup 中。实际上，EventLoopGroup 会分配一个 EventLoop 给该 Channel 注册。代码如下：

```
@Override
public ChannelFuture register(Channel channel){
return next().register(channel);
}
@Override
public ChannelFuture register(ChannelPromise promise){
return next().register(promise);
}
@Deprecated
@Override
public ChannelFuture register(Channel channel, ChannelPromise promise){
return next().register(channel, promise);
}
```

- Channel 注册的 EventLoop ，通过

/#next()
方法来选择。

# []( "8. NioEventLoopGroup")8. NioEventLoopGroup

io.netty.channel.nio.NioEventLoopGroup
，继承 MultithreadEventLoopGroup 抽象类，NioEventLoop 的分组实现类。

## []( "8.1 构造方法")8.1 构造方法

```
public NioEventLoopGroup(){
this(0);
}
public NioEventLoopGroup(int nThreads){
this(nThreads, (Executor) null);
}
public NioEventLoopGroup(int nThreads, ThreadFactory threadFactory){
this(nThreads, threadFactory, SelectorProvider.provider());
}
public NioEventLoopGroup(int nThreads, Executor executor){
this(nThreads, executor, SelectorProvider.provider());
}
public NioEventLoopGroup(
int nThreads, ThreadFactory threadFactory, final SelectorProvider selectorProvider){
this(nThreads, threadFactory, selectorProvider, DefaultSelectStrategyFactory.INSTANCE);
}
public NioEventLoopGroup(int nThreads, ThreadFactory threadFactory,
final SelectorProvider selectorProvider, final SelectStrategyFactory selectStrategyFactory){
super(nThreads, threadFactory, selectorProvider, selectStrategyFactory, RejectedExecutionHandlers.reject());
}
public NioEventLoopGroup(int nThreads, Executor executor, final SelectorProvider selectorProvider){
this(nThreads, executor, selectorProvider, DefaultSelectStrategyFactory.INSTANCE);
}
public NioEventLoopGroup(int nThreads, Executor executor, final SelectorProvider selectorProvider,
final SelectStrategyFactory selectStrategyFactory){
super(nThreads, executor, selectorProvider, selectStrategyFactory, RejectedExecutionHandlers.reject());
}
public NioEventLoopGroup(int nThreads, Executor executor, EventExecutorChooserFactory chooserFactory,
final SelectorProvider selectorProvider,
final SelectStrategyFactory selectStrategyFactory){
super(nThreads, executor, chooserFactory, selectorProvider, selectStrategyFactory,
RejectedExecutionHandlers.reject());
}
public NioEventLoopGroup(int nThreads, Executor executor, EventExecutorChooserFactory chooserFactory,
final SelectorProvider selectorProvider,
final SelectStrategyFactory selectStrategyFactory,
final RejectedExecutionHandler rejectedExecutionHandler){
super(nThreads, executor, chooserFactory, selectorProvider, selectStrategyFactory, rejectedExecutionHandler);
}
```

- 构造方法比较多，主要是明确了父构造方法的

Object ... args
方法参数：

- 第一个参数，

selectorProvider
，

java.nio.channels.spi.SelectorProvider
，用于创建 Java NIO Selector 对象。

- 第二个参数，

selectStrategyFactory
，

io.netty.channel.SelectStrategyFactory
，选择策略工厂。详细解析，见后续文章。

- 第三个参数，

rejectedExecutionHandler
，

io.netty.channel.SelectStrategyFactory
，拒绝执行处理器。详细解析，见后续文章。

## []( "8.2 newChild")8.2 newChild

/#newChild(Executor executor, Object... args)
方法，创建 NioEventLoop 对象。代码如下：

```
@Override
protected EventLoop newChild(Executor executor, Object... args) throws Exception{
return new NioEventLoop(this, executor,
(SelectorProvider) args[0], ((SelectStrategyFactory) args[1]).newSelectStrategy(), (RejectedExecutionHandler) args[2]);
}
```

- 通过

Object... args
方法参数，传入给 NioEventLoop 创建需要的参数。

## []( "8.3 setIoRatio")8.3 setIoRatio

/#setIoRatio(int ioRatio)
方法，设置所有 EventLoop 的 IO 任务占用执行时间的比例。代码如下：

```
//*/*
/* Sets the percentage of the desired amount of time spent for I/O in the child event loops. The default value is
/* {@code 50}, which means the event loop will try to spend the same amount of time for I/O as for non-I/O tasks.
/*/
public void setIoRatio(int ioRatio){
for (EventExecutor e: this) {
((NioEventLoop) e).setIoRatio(ioRatio);
}
}
```

## []( "8.4 rebuildSelectors")8.4 rebuildSelectors

/#rebuildSelectors()
方法，重建所有 EventLoop 的 Selector 对象。代码如下：

```
//*/*
/* Replaces the current {@link Selector}s of the child event loops with newly created {@link Selector}s to work
/* around the infamous epoll 100% CPU bug.
/*/
public void rebuildSelectors(){
for (EventExecutor e: this) {
((NioEventLoop) e).rebuildSelector();
}
}
```

- 因为 JDK 有 [epoll 100% CPU Bug](https://www.jianshu.com/p/da4398743b5a) 。实际上，NioEventLoop 当触发该 Bug 时，也会**自动**调用

NioEventLoop/#rebuildSelector()
方法，进行重建 Selector 对象，以修复该问题。

# []( "666. 彩蛋")666. 彩蛋

还是比较简单的文章。如果有不清晰的地方，也可以阅读如下文章：

- 永顺 [《Netty 源码分析之 三 我就是大名鼎鼎的 EventLoop(一)》](https://segmentfault.com/a/1190000007403873#articleHeader2) 的 [「NioEventLoopGroup 实例化过程」]() 小节。
- Hypercube [《自顶向下深入分析 Netty（四）—— EventLoop-1》](https://www.jianshu.com/p/da4398743b5a)
