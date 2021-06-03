# 精尽 Netty 源码解析 —— EventLoop（三）之 EventLoop 初始化

# []( "1. 概述")1. 概述

本文我们分享 EventLoop 的具体代码实现。因为 EventLoop 涉及的代码量较大，所以笔者会分成好几篇文章分别分享。而本文，我们来分享 EventLoop 的初始化。

但是要将 EventLoop 拆出“初始化”部分的内容，笔者又觉得是件非常困难的事情。所以本文希望能达到如下的效果：

1. 理解 EventLoop 有哪些属性
1. 创建 EventLoop 的过程
1. Channel 注册到 EventLoop 的过程
1. EventLoop 的任务提交。

- 虽然任务的提交，比较接近任务的执行，但是考虑到胖友可以更容易的理解 EventLoop ，所以放在本文。

# []( "2. 类结构图")2. 类结构图

EventLoopGroup 的整体类结构如下图：

[![EventLoopGroup 类图](http://static2.iocoder.cn/images/Netty/2018_05_04/01.png)](http://static2.iocoder.cn/images/Netty/2018_05_04/01.png 'EventLoopGroup 类图')EventLoopGroup 类图

- 红框部分，为 EventLoopGroup 相关的类关系。**其他部分，为 EventLoop 相关的类关系**。
- 因为我们实际上使用的是 NioEventLoopGroup 和 **NioEventLoop** ，所以笔者省略了其它相关的类，例如 OioEventLoopGroup、EmbeddedEventLoop 等等。

下面，我们逐层看看每个接口和类的实现代码。

# []( "3. EventExecutor")3. EventExecutor

io.netty.util.concurrent.EventExecutor
，继承 EventExecutorGroup 接口，事件执行器接口。代码如下：

```
// ========== 实现自 EventExecutorGroup 接口 ==========
//*/*
/* 返回自己
/*
/* Returns a reference to itself.
/*/
@Override
EventExecutor next();
// ========== 自定义接口 ==========
//*/*
/* 所属 EventExecutorGroup
/*
/* Return the {@link EventExecutorGroup} which is the parent of this {@link EventExecutor},
/*/
EventExecutorGroup parent();
//*/*
/* 当前线程是否在 EventLoop 线程中
/*
/* Calls {@link /#inEventLoop(Thread)} with {@link Thread/#currentThread()} as argument
/*/
boolean inEventLoop();
//*/*
/* 指定线程是否是 EventLoop 线程
/*
/* Return {@code true} if the given {@link Thread} is executed in the event loop,
/* {@code false} otherwise.
/*/
boolean inEventLoop(Thread thread);
//*/*
/* 创建一个 Promise 对象
/*
/* Return a new {@link Promise}.
/*/
<V> Promise<V> newPromise();
//*/*
/* 创建一个 ProgressivePromise 对象
/*
/* Create a new {@link ProgressivePromise}.
/*/
<V> ProgressivePromise<V> newProgressivePromise();
//*/*
/* 创建成功结果的 Future 对象
/*
/* Create a new {@link Future} which is marked as succeeded already. So {@link Future/#isSuccess()}
/* will return {@code true}. All {@link FutureListener} added to it will be notified directly. Also
/* every call of blocking methods will just return without blocking.
/*/
<V> Future<V> newSucceededFuture(V result);
//*/*
/* 创建异常的 Future 对象
/*
/* Create a new {@link Future} which is marked as failed already. So {@link Future/#isSuccess()}
/* will return {@code false}. All {@link FutureListener} added to it will be notified directly. Also
/* every call of blocking methods will just return without blocking.
/*/
<V> Future<V> newFailedFuture(Throwable cause);
```

- 接口定义的方法比较简单，已经添加中文注释，胖友自己看下。

# []( "4. OrderedEventExecutor")4. OrderedEventExecutor

io.netty.util.concurrent.OrderedEventExecutor
，继承 EventExecutor 接口，有序的事件执行器接口。代码如下：

```
//*/*
/* Marker interface for {@link EventExecutor}s that will process all submitted tasks in an ordered / serial fashion.
/*/
public interface OrderedEventExecutor extends EventExecutor{
}
```

- 没有定义任何方法，仅仅是一个标记接口，表示该执行器会有序 / 串行的方式执行。

# []( "5. EventLoop")5. EventLoop

io.netty.channel.EventLoop
，继承 OrderedEventExecutor 和 EventLoopGroup 接口，EventLoop 接口。代码如下：

```
//*/*
/* Will handle all the I/O operations for a {@link Channel} once registered.
/*
/* One {@link EventLoop} instance will usually handle more than one {@link Channel} but this may depend on
/* implementation details and internals.
/*
/*/
public interface EventLoop extends OrderedEventExecutor, EventLoopGroup{
@Override
EventLoopGroup parent();
}
```

- /#parent()
  接口方法，覆写方法的返回类型为 EventLoopGroup 。
- 接口上的英文注释，意思如下：

- EventLoop 将会处理注册在其上的 Channel 的所有 IO 操作。
- 通常，一个 EventLoop 上可以注册不只一个 Channel 。当然，这个也取决于具体的实现。

# []( "6. AbstractEventExecutor")6. AbstractEventExecutor

io.netty.util.concurrent.AbstractEventExecutor
，实现 EventExecutor 接口，继承 AbstractExecutorService 抽象类，EventExecutor 抽象类。

## []( "6.1 构造方法")6.1 构造方法

```
//*/*
/* 所属 EventExecutorGroup
/*/
private final EventExecutorGroup parent;
//*/*
/* EventExecutor 数组。只包含自己，用于 {@link /#iterator()}
/*/
private final Collection<EventExecutor> selfCollection = Collections.<EventExecutor>singleton(this);
protected AbstractEventExecutor(){
this(null);
}
protected AbstractEventExecutor(EventExecutorGroup parent){
this.parent = parent;
}
```

## []( "6.2 parent")6.2 parent

/#parent()
方法，获得所属 EventExecutorGroup 。代码如下：

```
@Override
public EventExecutorGroup parent(){
return parent;
}
```

## []( "6.3 next")6.3 next

/#next()
方法，获得自己。代码如下：

```
@Override
public EventExecutor next(){
return this;
}
```

## []( "6.4 inEventLoop()")6.4 inEventLoop()

/#inEventLoop()
方法，判断当前线程是否在 EventLoop 线程中。代码如下：

```
@Override
public boolean inEventLoop(){
return inEventLoop(Thread.currentThread());
}
```

- 具体的

/#inEventLoop(Thread thread)
方法，需要在子类实现。因为 AbstractEventExecutor 类还体现不出它所拥有的线程。

## []( "6.5 iterator")6.5 iterator

/#iterator()
方法，代码如下：

```
@Override
public Iterator<EventExecutor> iterator(){
return selfCollection.iterator();
}
```

## []( "6.6 newPromise 和 newProgressivePromise")6.6 newPromise 和 newProgressivePromise

/#newPromise()
和

/#newProgressivePromise()
方法，分别创建 DefaultPromise 和 DefaultProgressivePromise 对象。代码如下：

```
@Override
public <V> Promise<V> newPromise(){
return new DefaultPromise<V>(this);
}
@Override
public <V> ProgressivePromise<V> newProgressivePromise(){
return new DefaultProgressivePromise<V>(this);
}
```

- 我们可以看到，创建的 Promise 对象，都会传入自身作为 EventExecutor 。关于 Promise 相关的，我们在后续文章详细解析。实在想了解，也可以看看 [《Netty 源码笔记 —— 第四章 Future 和 Promise》](https://www.kancloud.cn/ssj234/netty-source/433215) 。

## []( "6.7 newSucceededFuture 和 newFailedFuture")6.7 newSucceededFuture 和 newFailedFuture

/#newSucceededFuture(V result)
和

/#newFailedFuture(Throwable cause)
方法，分别创建成功结果和异常的 Future 对象。代码如下：

```
@Override
public <V> Future<V> newSucceededFuture(V result){
return new SucceededFuture<V>(this, result);
}
@Override
public <V> Future<V> newFailedFuture(Throwable cause){
return new FailedFuture<V>(this, cause);
}
```

- 创建的 Future 对象，会传入自身作为 EventExecutor ，并传入

result
或

cause
分别作为成功结果和异常。

## []( "6.8 newTaskFor")6.8 newTaskFor

/#newTaskFor(...)
方法，创建 PromiseTask 对象。代码如下：

```
@Override
protected final <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value){
return new PromiseTask<T>(this, runnable, value);
}
@Override
protected final <T> RunnableFuture<T> newTaskFor(Callable<T> callable){
return new PromiseTask<T>(this, callable);
}
```

- 创建的 PromiseTask 对象，会传入自身作为 EventExecutor ，并传入 Runnable + Value 或 Callable 作为任务( Task )。

## []( "6.9 submit")6.9 submit

/#submit(...)
方法，提交任务。代码如下：

```
@Override
public Future<?> submit(Runnable task) {
return (Future<?>) super.submit(task);
}
@Override
public <T> Future<T> submit(Runnable task, T result){
return (Future<T>) super.submit(task, result);
}
@Override
public <T> Future<T> submit(Callable<T> task){
return (Future<T>) super.submit(task);
}
```

- 每个方法的实现上，是调用父类 AbstractExecutorService 的实现。

## []( "6.10 schedule")6.10 schedule

/#schedule(...)
方法，都不支持，交给子类 AbstractScheduledEventExecutor 实现。代码如下：

```
@Override
public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
throw new UnsupportedOperationException();
}
@Override
public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit){
throw new UnsupportedOperationException();
}
@Override
public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
throw new UnsupportedOperationException();
}
@Override
public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
throw new UnsupportedOperationException();
}
```

## []( "6.11 safeExecute")6.11 safeExecute

/#safeExecute(Runnable task)
**静态**方法，安全的执行任务。代码如下：

```
protected static void safeExecute(Runnable task){
try {
task.run();
} catch (Throwable t) {
logger.warn("A task raised an exception. Task: {}", task, t);
}
}
```

- 所谓“安全”指的是，当任务执行发生异常时，仅仅打印**告警**日志。

## []( "6.12 shutdown")6.12 shutdown

/#shutdown()
方法，关闭执行器。代码如下：

```
@Override
public Future<?> shutdownGracefully() {
return shutdownGracefully(DEFAULT_SHUTDOWN_QUIET_PERIOD, DEFAULT_SHUTDOWN_TIMEOUT, TimeUnit.SECONDS);
}
@Override
@Deprecated
public List<Runnable> shutdownNow(){
shutdown();
return Collections.emptyList();
}
```

- 具体的

/#shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit)
和

/#shutdown()
方法的实现，在子类中。

# []( "7. AbstractScheduledEventExecutor")7. AbstractScheduledEventExecutor

io.netty.util.concurrent.AbstractScheduledEventExecutor
，继承 AbstractEventExecutor 抽象类，**支持定时任务**的 EventExecutor 的抽象类。

详细解析，见 [《精尽 Netty 源码解析 —— EventLoop（七）之 EventLoop 处理定时任务》](http://svip.iocoder.cn/Netty/EventLoop-3-EventLoop-init) 。

# []( "8. SingleThreadEventExecutor")8. SingleThreadEventExecutor

io.netty.util.concurrent.SingleThreadEventExecutor
，实现 OrderedEventExecutor 接口，继承 AbstractScheduledEventExecutor 抽象类，基于单线程的 EventExecutor 抽象类，**即一个 EventExecutor 对应一个线程**。

## []( "8.1 构造方法")8.1 构造方法

```
//*/*
/* {@link /#state} 字段的原子更新器
/*/
private static final AtomicIntegerFieldUpdater<SingleThreadEventExecutor> STATE_UPDATER =AtomicIntegerFieldUpdater.newUpdater(SingleThreadEventExecutor.class, "state");
//*/*
/* {@link /#thread} 字段的原子更新器
/*/
private static final AtomicReferenceFieldUpdater<SingleThreadEventExecutor, ThreadProperties> PROPERTIES_UPDATER = AtomicReferenceFieldUpdater.newUpdater(SingleThreadEventExecutor.class, ThreadProperties.class, "threadProperties");
//*/*
/* 任务队列
/*
/* @see /#newTaskQueue(int)
/*/
private final Queue<Runnable> taskQueue;
//*/*
/* 线程
/*/
private volatile Thread thread;
//*/*
/* 线程属性
/*/
@SuppressWarnings("unused")
private volatile ThreadProperties threadProperties;
//*/*
/* 执行器
/*/
private final Executor executor;
//*/*
/* 线程是否已经打断
/*
/* @see /#interruptThread()
/*/
private volatile boolean interrupted;
//*/*
/* TODO 1006 EventLoop 优雅关闭
/*/
private final Semaphore threadLock = new Semaphore(0);
//*/*
/* TODO 1006 EventLoop 优雅关闭
/*/
private final Set<Runnable> shutdownHooks = new LinkedHashSet<Runnable>();
//*/*
/* 添加任务时，是否唤醒线程{@link /#thread}
/*/
private final boolean addTaskWakesUp;
//*/*
/* 最大等待执行任务数量，即 {@link /#taskQueue} 的队列大小
/*/
private final int maxPendingTasks;
//*/*
/* 拒绝执行处理器
/*
/* @see /#reject()
/* @see /#reject(Runnable)
/*/
private final RejectedExecutionHandler rejectedExecutionHandler;
//*/*
/* 最后执行时间
/*/
private long lastExecutionTime;
//*/*
/* 状态
/*/
@SuppressWarnings({ "FieldMayBeFinal", "unused" })
private volatile int state = ST_NOT_STARTED;
//*/*
/* TODO 优雅关闭
/*/
private volatile long gracefulShutdownQuietPeriod;
//*/*
/* 优雅关闭超时时间，单位：毫秒 TODO 1006 EventLoop 优雅关闭
/*/
private volatile long gracefulShutdownTimeout;
//*/*
/* 优雅关闭开始时间，单位：毫秒 TODO 1006 EventLoop 优雅关闭
/*/
private long gracefulShutdownStartTime;
//*/*
/* TODO 1006 EventLoop 优雅关闭
/*/
private final Promise<?> terminationFuture = new DefaultPromise<Void>(GlobalEventExecutor.INSTANCE);
protected SingleThreadEventExecutor(
EventExecutorGroup parent, ThreadFactory threadFactory, boolean addTaskWakesUp){
this(parent, new ThreadPerTaskExecutor(threadFactory), addTaskWakesUp);
}
protected SingleThreadEventExecutor(
EventExecutorGroup parent, ThreadFactory threadFactory,
boolean addTaskWakesUp, int maxPendingTasks, RejectedExecutionHandler rejectedHandler){
this(parent, new ThreadPerTaskExecutor(threadFactory), addTaskWakesUp, maxPendingTasks, rejectedHandler);
}
protected SingleThreadEventExecutor(EventExecutorGroup parent, Executor executor, boolean addTaskWakesUp){
this(parent, executor, addTaskWakesUp, DEFAULT_MAX_PENDING_EXECUTOR_TASKS, RejectedExecutionHandlers.reject());
}
protected SingleThreadEventExecutor(EventExecutorGroup parent, Executor executor,
boolean addTaskWakesUp, int maxPendingTasks,
RejectedExecutionHandler rejectedHandler){
super(parent);
this.addTaskWakesUp = addTaskWakesUp;
this.maxPendingTasks = Math.max(16, maxPendingTasks);
this.executor = ObjectUtil.checkNotNull(executor, "executor");
taskQueue = newTaskQueue(this.maxPendingTasks);
rejectedExecutionHandler = ObjectUtil.checkNotNull(rejectedHandler, "rejectedHandler");
}
```

- 属性比较多，我们耐心往下看。
- taskQueue
  属性，任务队列。

- addTaskWakesUp
  属性，添加任务到

taskQueue
队列时，是否唤醒

thread
线程。详细解析，见 [「8.11 execute」]() 。

- maxPendingTasks
  属性，最大等待执行任务数量，即

taskQueue
队列大小。

- rejectedExecutionHandler
  属性，拒绝执行处理器。在

taskQueue
队列超过最大任务数量时，怎么拒绝处理新提交的任务。

- thread
  属性，线程。在 SingleThreadEventExecutor 中，任务是提交到

taskQueue
队列中，而执行在

thread
线程中。

- threadProperties
  属性，线程属性。详细解析，见 [「8.15 threadProperties」]() 。
- executor
  属性，执行器。通过它创建

thread
线程。详细解析，见 [「8.11 execute」]() 。

- interrupted
  属性，线程是否打断。详细解析，详细解析，见 [「8.14 interruptThread」]() 。
- lastExecutionTime
  属性，最后执行时间。
- state
  属性，线程状态。SingleThreadEventExecutor 在实现上，

thread
的初始化采用延迟启动的方式，只有在第一个任务时，

executor
才会执行并创建该线程，从而节省资源。目前

thread
线程有 5 种状态，代码如下：

```
private static final int ST_NOT_STARTED = 1; // 未开始
private static final int ST_STARTED = 2; // 已开始
private static final int ST_SHUTTING_DOWN = 3; // 正在关闭中
private static final int ST_SHUTDOWN = 4; // 已关闭
private static final int ST_TERMINATED = 5; // 已经终止
```

- 状态变更流程如下图：[![状态变更流程](http://static2.iocoder.cn/images/Netty/2018_05_07/01.png)](http://static2.iocoder.cn/images/Netty/2018_05_07/01.png '状态变更流程')状态变更流程
- 构造方法，虽然比较多，但是很简单，胖友自己看下。

## []( "8.2 newTaskQueue")8.2 newTaskQueue

/#newTaskQueue(int maxPendingTasks)
方法，创建任务队列。代码如下：

```
//*/*
/* Create a new {@link Queue} which will holds the tasks to execute. This default implementation will return a
/* {@link LinkedBlockingQueue} but if your sub-class of {@link SingleThreadEventExecutor} will not do any blocking
/* calls on the this {@link Queue} it may make sense to {@code @Override} this and return some more performant
/* implementation that does not support blocking operations at all.
/*/
protected Queue<Runnable> newTaskQueue(int maxPendingTasks){
return new LinkedBlockingQueue<Runnable>(maxPendingTasks);
}
```

- 方法上有一大段注释，简单的说，这个方法默认返回的是 LinkedBlockingQueue 阻塞队列。如果子类有更好的队列选择( 例如非阻塞队列 )，可以重写该方法。在下文，我们会看到它的子类 NioEventLoop ，就重写了这个方法。

## []( "8.3 inEventLoop")8.3 inEventLoop

/#inEventLoop(Thread thread)
方法，判断指定线程是否是 EventLoop 线程。代码如下：

```
@Override
public boolean inEventLoop(Thread thread){
return thread == this.thread;
}
```

## []( "8.4 offerTask")8.4 offerTask

/#offerTask(Runnable task)
方法，添加任务到队列中。若添加失败，则返回

false
。代码如下：

```
final boolean offerTask(Runnable task){
// 关闭时，拒绝任务
if (isShutdown()) {
reject();
}
// 添加任务到队列
return taskQueue.offer(task);
}
```

- 注意，即使对于 BlockingQueue 的

/#offer(E e)
方法，也**不是阻塞的**！

## []( "8.5 addTask")8.5 addTask

/#offerTask(Runnable task)
方法，在

/#offerTask(Runnable task)
的方法的基础上，若添加任务到队列中失败，则进行拒绝任务。代码如下：

```
protected void addTask(Runnable task){
if (task == null) {
throw new NullPointerException("task");
}
// 添加任务到队列
if (!offerTask(task)) {
// 添加失败，则拒绝任务
reject(task);
}
}
```

- 调用

/#reject(task)
方法，拒绝任务。详细解析，见 [「8.6 reject」]() 。

- 该方法是

void
，无返回值。

## []( "8.6 removeTask")8.6 removeTask

/#removeTask(Runnable task)
方法，移除指定任务。代码如下：

```
protected boolean removeTask(Runnable task){
if (task == null) {
throw new NullPointerException("task");
}
return taskQueue.remove(task);
}
```

## []( "8.7 peekTask")8.7 peekTask

/#peekTask()
方法，返回队头的任务，但是**不移除**。代码如下：

```
protected Runnable peekTask(){
assert inEventLoop(); // 仅允许在 EventLoop 线程中执行
return taskQueue.peek();
}
```

## []( "8.8 hasTasks")8.8 hasTasks

/#hasTasks()
方法，队列中是否有任务。代码如下：

```
protected boolean hasTasks(){
assert inEventLoop(); // 仅允许在 EventLoop 线程中执行
return !taskQueue.isEmpty();
}
```

## []( "8.9 pendingTasks")8.9 pendingTasks

/#pendingTasks()
方法，获得队列中的任务数。代码如下：

```
public int pendingTasks(){
return taskQueue.size();
}
```

## []( "8.10 reject")8.10 reject

/#reject(Runnable task)
方法，拒绝任务。代码如下：

```
protected final void reject(Runnable task){
rejectedExecutionHandler.rejected(task, this);
}
```

- 调用

RejectedExecutionHandler/#rejected(Runnable task, SingleThreadEventExecutor executor)
方法，拒绝该任务。

/#reject()
方法，拒绝任何任务，用于 SingleThreadEventExecutor 已关闭(

/#isShutdown()
方法返回的结果为

true
)的情况。代码如下：

```
protected static void reject(){
throw new RejectedExecutionException("event executor terminated");
}
```

### []( "8.10.1 RejectedExecutionHandler")8.10.1 RejectedExecutionHandler

io.netty.util.concurrent.RejectedExecutionHandler
，拒绝执行处理器接口。代码如下：

```
//*/*
/* Called when someone tried to add a task to {@link SingleThreadEventExecutor} but this failed due capacity
/* restrictions.
/*/
void rejected(Runnable task, SingleThreadEventExecutor executor);
```

### []( "8.10.2 RejectedExecutionHandlers")8.10.2 RejectedExecutionHandlers

io.netty.util.concurrent.RejectedExecutionHandlers
，RejectedExecutionHandler 实现类枚举，目前有 2 种实现类。

**第一种**

```
private static final RejectedExecutionHandler REJECT = new RejectedExecutionHandler() {
@Override
public void rejected(Runnable task, SingleThreadEventExecutor executor){
throw new RejectedExecutionException();
}
};
public static RejectedExecutionHandler reject(){
return REJECT;
}
```

- 通过

/#reject()
方法，返回

REJECT
实现类的对象。该实现在拒绝时，直接抛出 RejectedExecutionException 异常。

- 默认情况下，使用这种实现。

**第二种**

```
public static RejectedExecutionHandler backoff(final int retries, long backoffAmount, TimeUnit unit){
ObjectUtil.checkPositive(retries, "retries");
final long backOffNanos = unit.toNanos(backoffAmount);
return new RejectedExecutionHandler() {
@Override
public void rejected(Runnable task, SingleThreadEventExecutor executor){
if (!executor.inEventLoop()) { // 非 EventLoop 线程中。如果在 EventLoop 线程中，就无法执行任务，这就导致完全无法重试了。
// 循环多次尝试添加到队列中
for (int i = 0; i < retries; i++) {
// 唤醒执行器，进行任务执行。这样，就可能执行掉部分任务。
// Try to wake up the executor so it will empty its task queue.
executor.wakeup(false);
// 阻塞等待
LockSupport.parkNanos(backOffNanos);
// 添加任务
if (executor.offerTask(task)) {
return;
}
}
}
// Either we tried to add the task from within the EventLoop or we was not able to add it even with
// backoff.
// 多次尝试添加失败，抛出 RejectedExecutionException 异常
throw new RejectedExecutionException();
}
};
}
```

- 通过

/#backoff(final int retries, long backoffAmount, TimeUnit unit)
方法，创建带多次尝试添加到任务队列的 RejectedExecutionHandler 实现类。

- 代码已经添加中文注释，胖友自己理解下，比较简单的。

## []( "8.11 execute")8.11 execute

/#execute(Runnable task)
方法，执行一个任务。但是方法名无法很完整的体现出具体的方法实现，甚至有一些出入，所以我们直接看源码，代码如下：

```
1: @Override
2: public void execute(Runnable task){
3: if (task == null) {
4: throw new NullPointerException("task");
5: }
6:
7: // 获得当前是否在 EventLoop 的线程中
8: boolean inEventLoop = inEventLoop();
9: // 添加到任务队列
10: addTask(task);
11: if (!inEventLoop) {
12: // 创建线程
13: startThread();
14: // 若已经关闭，移除任务，并进行拒绝
15: if (isShutdown() && removeTask(task)) {
16: reject();
17: }
18: }
19:
20: // 唤醒线程
21: if (!addTaskWakesUp && wakesUpForTask(task)) {
22: wakeup(inEventLoop);
23: }
24: }
```

- 第 8 行：调用

/#inEventLoop()
方法，获得当前是否在 EventLoop 的线程中。

- 第 10 行：调用

/#addTask(Runnable task)
方法，添加任务到队列中。

- 第 11 行：非 EventLoop 的线程

- 第 13 行：调用

/#startThread()
方法，启动 EventLoop **独占**的线程，即

thread
属性。详细解析，见 [「8.12 startThread」]() 。

- 第 14 至 17 行：若已经关闭，则移除任务，并拒绝执行。
- 第 20 至 23 行：调用

/#wakeup(boolean inEventLoop)
方法，唤醒线程。详细解析，见 [「8.13 wakeup」]() 。

- 等等，第 21 行的

!addTaskWakesUp
有点奇怪，不是说好的

addTaskWakesUp
表示“添加任务时，是否唤醒线程”？！但是，怎么使用

!
取反了。这样反倒变成了，“添加任务时，是否【**不**】唤醒线程”。具体的原因是为什么呢？笔者 Google、Github Netty Issue、和基佬讨论，都未找到解答。目前笔者的理解是：

addTaskWakesUp
真正的意思是，“添加任务后，任务是否会自动导致线程唤醒”。为什么呢？

- 对于 Nio 使用的 NioEventLoop ，它的线程执行任务是基于 Selector 监听感兴趣的事件，所以当任务添加到

taskQueue
队列中时，线程是无感知的，所以需要调用

/#wakeup(boolean inEventLoop)
方法，进行**主动**的唤醒。

- 对于 Oio 使用的 ThreadPerChannelEventLoop ，它的线程执行是基于

taskQueue
队列监听( **阻塞拉取** )事件和任务，所以当任务添加到

taskQueue
队列中时，线程是可感知的，相当于说，进行**被动**的唤醒。

- 感谢闪电侠，证实我的理解是正确的。参见：

- [https://github.com/netty/netty/commit/23d017849429c18e1890b0a5799e5262df4f269f](https://github.com/netty/netty/commit/23d017849429c18e1890b0a5799e5262df4f269f)

- [![提交图](http://static2.iocoder.cn/images/Netty/2018_05_07/05.png)](http://static2.iocoder.cn/images/Netty/2018_05_07/05.png '提交图')提交图
- 调用

/#wakesUpForTask(task)
方法，判断该任务是否需要唤醒线程。代码如下：

```
protected boolean wakesUpForTask(Runnable task){
return true;
}
```

- 默认返回

true
。在 [「9. SingleThreadEventLoop」]() 中，我们会看到对该方法的重写。

## []( "8.12 startThread")8.12 startThread

/#startThread()
方法，启动 EventLoop **独占**的线程，即

thread
属性。代码如下：

```
1: private void doStartThread(){
2: assert thread == null;
3: executor.execute(new Runnable() {
4:
5: @Override
6: public void run(){
7: // 记录当前线程
8: thread = Thread.currentThread();
9:
10: // 如果当前线程已经被标记打断，则进行打断操作。
11: if (interrupted) {
12: thread.interrupt();
13: }
14:
15: boolean success = false; // 是否执行成功
16:
17: // 更新最后执行时间
18: updateLastExecutionTime();
19: try {
20: // 执行任务
21: SingleThreadEventExecutor.this.run();
22: success = true; // 标记执行成功
23: } catch (Throwable t) {
24: logger.warn("Unexpected exception from an event executor: ", t);
25: } finally {
26: // TODO 1006 EventLoop 优雅关闭
27: for (;;) {
28: int oldState = state;
29: if (oldState >= ST_SHUTTING_DOWN || STATE_UPDATER.compareAndSet(
30: SingleThreadEventExecutor.this, oldState, ST_SHUTTING_DOWN)) {
31: break;
32: }
33: }
34:
35: // TODO 1006 EventLoop 优雅关闭
36: // Check if confirmShutdown() was called at the end of the loop.
37: if (success && gracefulShutdownStartTime == 0) {
38: if (logger.isErrorEnabled()) {
39: logger.error("Buggy " + EventExecutor.class.getSimpleName() + " implementation; " +
40: SingleThreadEventExecutor.class.getSimpleName() + ".confirmShutdown() must " +
41: "be called before run() implementation terminates.");
42: }
43: }
44:
45: // TODO 1006 EventLoop 优雅关闭
46: try {
47: // Run all remaining tasks and shutdown hooks.
48: for (;;) {
49: if (confirmShutdown()) {
50: break;
51: }
52: }
53: } finally {
54: try {
55: cleanup(); // 清理，释放资源
56: } finally {
57: STATE_UPDATER.set(SingleThreadEventExecutor.this, ST_TERMINATED);
58: threadLock.release();
59: if (!taskQueue.isEmpty()) {
60: if (logger.isWarnEnabled()) {
61: logger.warn("An event executor terminated with " +
62: "non-empty task queue (" + taskQueue.size() + ')');
63: }
64: }
65:
66: terminationFuture.setSuccess(null);
67: }
68: }
69: }
70:
71: }
72: });
73: }
```

- 第 2 行：断言，保证

thread
为空。

- 第 3 行 至 72 行：调用

Executor/#execute(Runnable runnable)
方法，执行任务。下面，我们来详细解析。

- 第 8 行：赋值当前的线程给

thread
属性。这就是，每个 SingleThreadEventExecutor 独占的线程的创建方式。

- 第 10 至 13 行：如果当前线程已经被标记打断，则进行打断操作。为什么会有这样的逻辑呢？详细解析，见 [「8.14 interruptThread」]() 。
- 第 18 行：调用

/#updateLastExecutionTime()
方法，更新最后执行时间。代码如下：

```
//*/*
/* Updates the internal timestamp that tells when a submitted task was executed most recently.
/* {@link /#runAllTasks()} and {@link /#runAllTasks(long)} updates this timestamp automatically, and thus there's
/* usually no need to call this method. However, if you take the tasks manually using {@link /#takeTask()} or
/* {@link /#pollTask()}, you have to call this method at the end of task execution loop for accurate quiet period
/* checks.
/*/
protected void updateLastExecutionTime(){
lastExecutionTime = ScheduledFutureTask.nanoTime();
}
```

- 英文注释，自己看。😈
- 第 21 行：调用

SingleThreadEventExecutor/#run()
方法，执行任务。详细解析，见 [8.X run]() 。

- 第 25 至 69 行：TODO 1006 EventLoop 优雅关闭
- 第 55 行：调用

/#cleanup()
方法，清理释放资源。详细解析，见 [8.X cleanup]() 。

## []( "8.13 wakeup")8.13 wakeup

/#wakeup(boolean inEventLoop)
方法，唤醒线程。代码如下：

```
protected void wakeup(boolean inEventLoop){
if (!inEventLoop // <1>
|| state == ST_SHUTTING_DOWN) { // TODO 1006 EventLoop 优雅关闭
// Use offer as we actually only need this to unblock the thread and if offer fails we do not care as there
// is already something in the queue.
taskQueue.offer(WAKEUP_TASK); // <2>
}
}
```

- <1>
  处的

!inEventLoop
代码段，判断不在 EventLoop 的线程中。因为，如果在 EventLoop 线程中，意味着线程就在执行中，不必要唤醒。

- <2>
  处，调用

Queue/#offer(E e)
方法，添加任务到队列中。而添加的任务是

WAKEUP_TASK
，代码如下：

```
private static final Runnable WAKEUP_TASK = new Runnable() {
@Override
public void run(){
// Do nothing.
}
};
```

- 这是一个空的 Runnable 实现类。仅仅用于唤醒基于

taskQueue
阻塞拉取的 EventLoop 实现类。

- 对于 NioEventLoop 会重写该方法，代码如下：

```
@Override
protected void wakeup(boolean inEventLoop){
if (!inEventLoop && wakenUp.compareAndSet(false, true)) {
selector.wakeup();
}
}
```

- 通过 NIO Selector 唤醒。

## []( "8.14 interruptThread")8.14 interruptThread

/#interruptThread()
方法，打断 EventLoop 的线程。代码如下：

```
protected void interruptThread(){
Thread currentThread = thread;
// 线程不存在，则标记线程被打断
if (currentThread == null) {
interrupted = true;
// 打断线程
} else {
currentThread.interrupt();
}
}
```

- 因为 EventLoop 的线程是延迟启动，所以可能

thread
并未创建，此时通过

interrupted
标记打断。之后在

/#startThread()
方法中，创建完线程后，再进行打断，也就是说，“延迟打断”。

## []( "8.15 threadProperties")8.15 threadProperties

/#threadProperties()
方法，获得 EventLoop 的线程属性。代码如下：

```
1: public final ThreadProperties threadProperties(){
2: ThreadProperties threadProperties = this.threadProperties;
3: if (threadProperties == null) {
4: Thread thread = this.thread;
5: if (thread == null) {
6: assert !inEventLoop();
7: // 提交空任务，促使 execute 方法执行
8: submit(NOOP_TASK).syncUninterruptibly();
9: // 获得线程
10: thread = this.thread;
11: assert thread != null;
12: }
13:
14: // 创建 DefaultThreadProperties 对象
15: threadProperties = new DefaultThreadProperties(thread);
16: // CAS 修改 threadProperties 属性
17: if (!PROPERTIES_UPDATER.compareAndSet(this, null, threadProperties)) {
18: threadProperties = this.threadProperties;
19: }
20: }
21:
22: return threadProperties;
23: }
```

- 第 2 至 3 行：获得 ThreadProperties 对象。若不存在，则进行创建 ThreadProperties 对象。

- 第 4 至 5 行：获得 EventLoop 的线程。因为线程是延迟启动的，所以会出现线程为空的情况。若线程为空，则需要进行创建。

- 第 8 行：调用

/#submit(Runnable)
方法，提交任务，就能促使

/#execute(Runnable)
方法执行。如下图所示：[![submit => execute 的流程](http://static2.iocoder.cn/images/Netty/2018_05_07/02.png)](http://static2.iocoder.cn/images/Netty/2018_05_07/02.png 'submit => execute 的流程')submit => execute 的流程

- 第 8 行：调用

Future/#syncUninterruptibly()
方法，保证

execute()
方法中**异步**创建

thread
完成。

- 第 10 至 11 行：获得线程，并断言保证线程存在。
- 第 15 行：调用 DefaultThreadProperties 对象。
- 第 16 至 19 行：CAS 修改

threadProperties
属性。

- 第 22 行：返回

threadProperties
。

### []( "8.15.1 ThreadProperties")8.15.1 ThreadProperties

io.netty.util.concurrent.ThreadProperties
，线程属性接口。代码如下：

```
Thread.State state();
int priority();
boolean isInterrupted();
boolean isDaemon();
String name();
long id();
StackTraceElement[] stackTrace();
boolean isAlive();
```

### []( "8.15.2 DefaultThreadProperties")8.15.2 DefaultThreadProperties

DefaultThreadProperties 实现 ThreadProperties 接口，默认线程属性实现类。代码如下：
DefaultThreadProperties 内嵌在 SingleThreadEventExecutor 中。

```
private static final class DefaultThreadProperties implements ThreadProperties{
private final Thread t;
DefaultThreadProperties(Thread t) {
this.t = t;
}
@Override
public State state(){
return t.getState();
}
@Override
public int priority(){
return t.getPriority();
}
@Override
public boolean isInterrupted(){
return t.isInterrupted();
}
@Override
public boolean isDaemon(){
return t.isDaemon();
}
@Override
public String name(){
return t.getName();
}
@Override
public long id(){
return t.getId();
}
@Override
public StackTraceElement[] stackTrace() {
return t.getStackTrace();
}
@Override
public boolean isAlive(){
return t.isAlive();
}
}
```

- 我们可以看到，每个实现方法，实际上就是对被包装的线程

t
的方法的封装。

- 那为什么

/#threadProperties()
方法不直接返回

thread
呢？因为如果直接返回

thread
，调用方可以调用到该变量的其他方法，这个是我们不希望看到的。

## []( "8.16 run")8.16 run

/#run()
方法，它是一个**抽象方法**，由子类实现，如何执行

taskQueue
队列中的任务。代码如下：

```
protected abstract void run();
```

SingleThreadEventExecutor 提供了很多执行任务的方法，方便子类在实现自定义运行任务的逻辑时：

- [x]

/#runAllTasks()

- [x]

/#runAllTasks(long timeoutNanos)

- [x]

/#runAllTasksFrom(Queue<Runnable> taskQueue)

- [x]

/#afterRunningAllTasks()

- [x]

/#pollTask()

- [x]

/#pollTaskFrom(Queue<Runnable> taskQueue)

- /#takeTask()
- /#fetchFromScheduledTaskQueue()
- /#delayNanos(long currentTimeNanos)

详细解析，见 [《精尽 Netty 源码解析 —— EventLoop（四）之 EventLoop 运行》](http://svip.iocoder.cn/Netty/EventLoop-4-EventLoop-run) 。

## []( "8.17 cleanup")8.17 cleanup

/#cleanup()
方法，清理释放资源。代码如下：

```
//*/*
/* Do nothing, sub-classes may override
/*/
protected void cleanup(){
// NOOP
}
```

- 目前该方法为空的。在子类 NioEventLoop 中，我们会看到它覆写该方法，关闭 NIO Selector 对象。

## []( "8.18 invokeAll")8.18 invokeAll

/#invokeAll(...)
方法，在 EventExecutor 中执行**多个**普通任务。代码如下：

```
@Override
public <T> List<java.util.concurrent.Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
throws InterruptedException {
throwIfInEventLoop("invokeAll");
return super.invokeAll(tasks);
}
@Override
public <T> List<java.util.concurrent.Future<T>> invokeAll(
Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
throwIfInEventLoop("invokeAll");
return super.invokeAll(tasks, timeout, unit);
}
```

- 调用

/#throwIfInEventLoop(String method)
方法，判断若在 EventLoop 的线程中调用该方法，抛出 RejectedExecutionException 异常。代码如下：

```
private void throwIfInEventLoop(String method){
if (inEventLoop()) {
throw new RejectedExecutionException("Calling " + method + " from within the EventLoop is not allowed");
}
}
```

- 调用父类 AbstractScheduledEventExecutor 的

/#invokeAll(tasks, ...)
方法，执行**多个**普通任务。在该方法内部，会调用

/#execute(Runnable task)
方法，执行任务。调用栈如下图：[![invokeAll => execute 的流程](http://static2.iocoder.cn/images/Netty/2018_05_07/03.png)](http://static2.iocoder.cn/images/Netty/2018_05_07/03.png 'invokeAll => execute 的流程')invokeAll => execute 的流程

## []( "8.19 invokeAny")8.19 invokeAny

和

/#invokeAll(...)
方法，**类似**。

/#invokeAll(...)
方法，在 EventExecutor 中执行**多个**普通任务，有**一个**执行完成即可。代码如下：

```
@Override
public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException{
throwIfInEventLoop("invokeAny");
return super.invokeAny(tasks);
}
@Override
public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
throws InterruptedException, ExecutionException, TimeoutException{
throwIfInEventLoop("invokeAny");
return super.invokeAny(tasks, timeout, unit);
}
```

- 调用

/#throwIfInEventLoop(String method)
方法，判断若在 EventLoop 的线程中调用该方法，抛出 RejectedExecutionException 异常。

- 调用父类 AbstractScheduledEventExecutor 的

/#invokeAny(tasks, ...)
方法，执行**多个**普通任务，有**一个**执行完成即可。在该方法内部，会调用

/#execute(Runnable task)
方法，执行任务。调用栈如下图：[![invokeAny => execute 的流程](http://static2.iocoder.cn/images/Netty/2018_05_07/04.png)](http://static2.iocoder.cn/images/Netty/2018_05_07/04.png 'invokeAny => execute 的流程')invokeAny => execute 的流程

## []( "8.20 shutdown")8.20 shutdown

如下是优雅关闭，我们在 TODO 1006 EventLoop 优雅关闭

- /#addShutdownHook(final Runnable task)
- /#removeShutdownHook(final Runnable task)
- /#runShutdownHooks()
- /#shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit)
- /#shutdown()
- /#terminationFuture()
- /#isShuttingDown()
- /#isShutdown()
- /#isTerminated()
- /#confirmShutdown()
- /#awaitTermination(long timeout, TimeUnit unit)

# []( "9. SingleThreadEventLoop")9. SingleThreadEventLoop

io.netty.channel.SingleThreadEventLoop
，实现 EventLoop 接口，继承 SingleThreadEventExecutor 抽象类，基于单线程的 EventLoop 抽象类，主要增加了 Channel 注册到 EventLoop 上。

## []( "9.1 构造方法")9.1 构造方法

```
//*/*
/* 默认任务队列最大数量
/*/
protected static final int DEFAULT_MAX_PENDING_TASKS = Math.max(16, SystemPropertyUtil.getInt("io.netty.eventLoop.maxPendingTasks", Integer.MAX_VALUE));
//*/*
/* 尾部任务队列，执行在 {@link /#taskQueue} 之后
/*
/* Commits
/* /* [Ability to run a task at the end of an eventloop iteration.](https://github.com/netty/netty/pull/5513)
/*
/* Issues
/* /* [Auto-flush for channels. (`ChannelHandler` implementation)](https://github.com/netty/netty/pull/5716)
/* /* [Consider removing executeAfterEventLoopIteration](https://github.com/netty/netty/issues/7833)
/*
/* 老艿艿：未来会移除该队列，前提是实现了 Channel 的 auto flush 功能。按照最后一个 issue 的讨论
/*/
private final Queue<Runnable> tailTasks;
protected SingleThreadEventLoop(EventLoopGroup parent, ThreadFactory threadFactory, boolean addTaskWakesUp){
this(parent, threadFactory, addTaskWakesUp, DEFAULT_MAX_PENDING_TASKS, RejectedExecutionHandlers.reject());
}
protected SingleThreadEventLoop(EventLoopGroup parent, Executor executor, boolean addTaskWakesUp){
this(parent, executor, addTaskWakesUp, DEFAULT_MAX_PENDING_TASKS, RejectedExecutionHandlers.reject());
}
protected SingleThreadEventLoop(EventLoopGroup parent, ThreadFactory threadFactory,
boolean addTaskWakesUp, int maxPendingTasks,
RejectedExecutionHandler rejectedExecutionHandler){
super(parent, threadFactory, addTaskWakesUp, maxPendingTasks, rejectedExecutionHandler);
tailTasks = newTaskQueue(maxPendingTasks);
}
protected SingleThreadEventLoop(EventLoopGroup parent, Executor executor,
boolean addTaskWakesUp, int maxPendingTasks,
RejectedExecutionHandler rejectedExecutionHandler){
super(parent, executor, addTaskWakesUp, maxPendingTasks, rejectedExecutionHandler);
tailTasks = newTaskQueue(maxPendingTasks);
}
```

- 新增了一条

tailTasks
队列，执行的顺序在

taskQueue
之后。详细解析，见 [《精尽 Netty 源码解析 —— EventLoop（六）之 EventLoop 处理普通任务》](http://svip.iocoder.cn/Netty/EventLoop-6-EventLoop-handle-normal-task) 。

- 构造方法比较简单，胖友自己看下就可以了。

## []( "9.2 parent")9.2 parent

/#parent()
方法，获得所属 EventLoopGroup 。代码如下：

```
@Override
public EventLoopGroup parent(){
return (EventLoopGroup) super.parent();
}
```

- 覆盖父类方法，将返回值转换成 EventLoopGroup 类。

## []( "9.3 next")9.3 next

/#next()
方法，获得自己。代码如下：

```
@Override
public EventLoop next(){
return (EventLoop) super.next();
}
```

- 覆盖父类方法，将返回值转换成 EventLoop 类。

## []( "9.4 register")9.4 register

/#register(Channel channel)
方法，注册 Channel 到 EventLoop 上。代码如下：

```
@Override
public ChannelFuture register(Channel channel){
return register(new DefaultChannelPromise(channel, this));
}
```

- 将 Channel 和 EventLoop 创建一个 DefaultChannelPromise 对象。通过这个 DefaultChannelPromise 对象，我们就能实现对**异步**注册过程的监听。
- 调用

/#register(final ChannelPromise promise)
方法，注册 Channel 到 EventLoop 上。代码如下：

```
@Override
public ChannelFuture register(final ChannelPromise promise){
ObjectUtil.checkNotNull(promise, "promise");
// 注册 Channel 到 EventLoop 上
promise.channel().unsafe().register(this, promise);
// 返回 ChannelPromise 对象
return promise;
}
```

- 在方法内部，我们就看到在 [《精尽 Netty 源码分析 —— 启动（一）之服务端》](http://svip.iocoder.cn/Netty/bootstrap-1-server?self) 的 [「3.14.3 注册 Channel 到 EventLoopGroup」]() 章节，熟悉的内容，调用

AbstractUnsafe/#register(EventLoop eventLoop, final ChannelPromise promise)
方法，**注册 Channel 到 EventLoop 上**。

## []( "9.5 hasTasks")9.5 hasTasks

/#hasTasks()
方法，队列中是否有任务。代码如下：

```
@Override
protected boolean hasTasks(){
return super.hasTasks() || !tailTasks.isEmpty();
}
```

- 基于两个队列来判断是否还有任务。

## []( "9.6 pendingTasks")9.6 pendingTasks

/#pendingTasks()
方法，获得队列中的任务数。代码如下：

```
@Override
public int pendingTasks(){
return super.pendingTasks() + tailTasks.size();
}
```

- 计算两个队列的任务之和。

## []( "9.7 executeAfterEventLoopIteration")9.7 executeAfterEventLoopIteration

/#executeAfterEventLoopIteration(Runnable task)
方法，执行一个任务。但是方法名无法很完整的体现出具体的方法实现，甚至有一些出入，所以我们直接看源码，代码如下：

```
1: @UnstableApi
2: public final void executeAfterEventLoopIteration(Runnable task){
3: ObjectUtil.checkNotNull(task, "task");
4: // 关闭时，拒绝任务
5: if (isShutdown()) {
6: reject();
7: }
8:
9: // 添加到任务队列
10: if (!tailTasks.offer(task)) {
11: // 添加失败，则拒绝任务
12: reject(task);
13: }
14:
15: // 唤醒线程
16: if (wakesUpForTask(task)) {
17: wakeup(inEventLoop());
18: }
19: }
```

- 第 4 至 7 行：SingleThreadEventLoop 关闭时，拒绝任务。
- 第 10 行：调用

Queue/#offer(E e)
方法，添加任务到队列中。

- 第 12 行：若添加失败，调用

/#reject(Runnable task)
方法，拒绝任务。

- 第 15 至 18 行：唤醒线程。

- 第 16 行：SingleThreadEventLoop 重写了

/#wakesUpForTask(Runnable task)
方法。详细解析，见 [「9.9 wakesUpForTask」]() 。

## []( "9.8 removeAfterEventLoopIterationTask")9.8 removeAfterEventLoopIterationTask

/#removeAfterEventLoopIterationTask(Runnable task)
方法，移除指定任务。代码如下：

```
@UnstableApi
final boolean removeAfterEventLoopIterationTask(Runnable task){
return tailTasks.remove(ObjectUtil.checkNotNull(task, "task"));
}
```

## []( "9.9 wakesUpForTask")9.9 wakesUpForTask

/#wakesUpForTask(task)
方法，判断该任务是否需要唤醒线程。代码如下：

```
@Override
protected boolean wakesUpForTask(Runnable task){
return !(task instanceof NonWakeupRunnable);
}
```

- 当任务类型为 NonWakeupRunnable ，则不进行唤醒线程。

### []( "9.9.1 NonWakeupRunnable")9.9.1 NonWakeupRunnable

NonWakeupRunnable 实现 Runnable 接口，用于标记不唤醒线程的任务。代码如下：

```
//*/*
/* Marker interface for {@link Runnable} that will not trigger an {@link /#wakeup(boolean)} in all cases.
/*/
interface NonWakeupRunnable extends Runnable{ }
```

## []( "9.10 afterRunningAllTasks")9.10 afterRunningAllTasks

/#afterRunningAllTasks()
方法，在运行完所有任务后，执行

tailTasks
队列中的任务。代码如下：

```
protected void afterRunningAllTasks(){
runAllTasksFrom(tailTasks);
}
```

- 调用

/#runAllTasksFrom(queue)
方法，执行

tailTasks
队列中的所有任务。

# []( "10. NioEventLoop")10. NioEventLoop

io.netty.channel.nio.NioEventLoop
，继承 SingleThreadEventLoop 抽象类，NIO EventLoop 实现类，实现对注册到其中的 Channel 的就绪的 IO 事件，和对用户提交的任务进行处理。

详细解析，见 [《精尽 Netty 源码解析 —— EventLoop（四）之 EventLoop 运行》](http://svip.iocoder.cn/Netty/EventLoop-4-EventLoop-run) 。

# []( "666. 彩蛋")666. 彩蛋

自顶向下的过了下 EventLoop 相关的类和方法。因为仅涉及 EventLoop 初始化相关的内容，所以对于 EventLoop 运行相关的内容，就不得不省略了。

那么，饥渴难耐的我们，[《精尽 Netty 源码解析 —— EventLoop（四）之 EventLoop 运行》](http://svip.iocoder.cn/Netty/EventLoop-4-EventLoop-run) ，走起！

推荐阅读如下文章：

- 永顺 [《Netty 源码分析之 三 我就是大名鼎鼎的 EventLoop(一)》](https://segmentfault.com/a/1190000007403873#articleHeader7) 的 [「NioEventLoop」]() 小节。
- Hypercube [《自顶向下深入分析 Netty（四）—— EventLoop-2》](https://www.jianshu.com/p/d0f06b13e2fb)
