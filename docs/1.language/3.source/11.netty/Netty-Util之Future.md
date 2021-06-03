# 精尽 Netty 源码解析 —— Util 之 Future

笔者先把 Netty 主要的内容写完，所以关于 Future 的分享，先放在后续的计划里。
老艿艿：其实是因为，自己想去研究下 Service Mesh ，所以先简单收个小尾。

当然，良心如我，还是为对这块感兴趣的胖友，先准备好了一篇不错的文章：

- Hypercube [《自顶向下深入分析 Netty（五）–Future》](https://www.jianshu.com/p/a06da3256f0c)

为避免可能 [《自顶向下深入分析 Netty（五）–Future》](https://www.jianshu.com/p/a06da3256f0c) 被作者删除，笔者这里先复制一份作为备份。

# []( "666. 备份")666. 备份

再次回顾这幅图，在上一章中，我们分析了 Reactor 的完整实现。由于 Java NIO 事件驱动的模型，要求 Netty 的事件处理采用异步的方式，异步处理则需要表示异步操作的结果。Future 正是用来表示异步操作结果的对象，Future 的类签名为：

```
public interface Future<V>;
```

其中的泛型参数 V 即表示异步结果的类型。

### []( "5.1 总述")5.1 总述

也许你已经使用过 JDK 的 Future 对象，该接口的方法如下：

```
// 取消异步操作
boolean cancel(boolean mayInterruptIfRunning);
// 异步操作是否取消
boolean isCancelled();
// 异步操作是否完成，正常终止、异常、取消都是完成
boolean isDone();
// 阻塞直到取得异步操作结果
V get() throws InterruptedException, ExecutionException;
// 同上，但最长阻塞时间为timeout
V get(long timeout, TimeUnit unit)
throws InterruptedException, ExecutionException, TimeoutException;
```

我们的第一印象会觉得这样的设计并不坏，但仔细思考，便会发现问题：
(1).接口中只有 isDone()方法判断一个异步操作是否完成，但是对于完成的定义过于模糊，JDK 文档指出正常终止、抛出异常、用户取消都会使 isDone()方法返回真。在我们的使用中，我们极有可能是对这三种情况分别处理，而 JDK 这样的设计不能满足我们的需求。
(2).对于一个异步操作，我们更关心的是这个异步操作触发或者结束后能否再执行一系列动作。比如说，我们浏览网页时点击一个按钮后实现用户登录。在 javascript 中，处理代码如下：

```
$("/#login").click(function(){
login();
});
```

可见在这样的情况下，JDK 中的 Future 便不能处理，所以，Netty 扩展了 JDK 的 Future 接口，使其能解决上面的两个问题。扩展的方法如下（类似方法只列出一个）：

```
// 异步操作完成且正常终止
boolean isSuccess();
// 异步操作是否可以取消
boolean isCancellable();
// 异步操作失败的原因
Throwable cause();
// 添加一个监听者，异步操作完成时回调，类比javascript的回调函数
Future<V> addListener(GenericFutureListener<? extends Future<? super V>> listener);
Future<V> removeListener(GenericFutureListener<? extends Future<? super V>> listener);
// 阻塞直到异步操作完成
Future<V> await() throws InterruptedException;
// 同上，但异步操作失败时抛出异常
Future<V> sync() throws InterruptedException;
// 非阻塞地返回异步结果，如果尚未完成返回null
V getNow();
```

如果你对 Future 的状态还有疑问，放上代码注释中的 ascii 图打消你的疑虑：

```
/* +---------------------------+
/* | Completed successfully |
/* +---------------------------+
/* +----> isDone() = true |
/* +--------------------------+ | | isSuccess() = true |
/* | Uncompleted | | +===========================+
/* +--------------------------+ | | Completed with failure |
/* | isDone() = false | | +---------------------------+
/* | isSuccess() = false |----+----> isDone() = true |
/* | isCancelled() = false | | | cause() = non-null |
/* | cause() = null | | +===========================+
/* +--------------------------+ | | Completed by cancellation |
/* | +---------------------------+
/* +----> isDone() = true |
/* | isCancelled() = true |
/* +---------------------------+
```

可知，Future 对象有两种状态尚未完成和已完成，其中已完成又有三种状态：成功、失败、用户取消。各状态的状态断言请在此图中查找。
仔细看完上面的图并联系 Future 接口中的方法，你是不是也会和我有相同的疑问：Future 接口中的方法都是 getter 方法而没有 setter 方法，也就是说这样实现的 Future 子类的状态是不可变的，如果我们想要变化，那该怎么办呢？Netty 提供的解决方法是：使用可写的 Future 即 Promise。Promise 接口扩展的方法如下：

```
// 标记异步操作结果为成功，如果已被设置（不管成功还是失败）则抛出异常IllegalStateException
Promise<V> setSuccess(V result);
// 同上，只是结果已被设置时返回False
boolean trySuccess(V result);
Promise<V> setFailure(Throwable cause);
boolean tryFailure(Throwable cause);
// 设置结果为不可取消，结果已被取消返回False
boolean setUncancellable();
```

需要注意的是：Promise 接口继承自 Future 接口，它提供的 setter 方法与常见的 setter 方法大为不同。Promise 从 Uncompleted–>Completed 的状态转变**有且只能有一次**，也就是说 setSuccess 和 setFailure 方法最多只会成功一个，此外，在 setSuccess 和 setFailure 方法中会通知注册到其上的监听者。为了加深对 Future 和 Promise 的理解，我们可以将 Future 类比于定额发票，Promise 类比于机打发票。当商户拿到税务局的发票时，如果是定额发票，则已经确定好金额是 100 还是 50 或其他，商户再也不能更改；如果是机打发票，商户相当于拿到了一个发票模板，需要多少金额按实际情况填到模板指定处。显然，不能两次使用同一张机打发票打印，这会使发票失效，而 Promise 做的更好，它使第二次调用 setter 方法失败。
至此，我们从总体上了解了 Future 和 Promise 的原理。我们再看一下类图：

[![Future类图](http://static2.iocoder.cn/images/Netty/2019_02_01/01.png)](http://static2.iocoder.cn/images/Netty/2019_02_01/01.png 'Future类图')Future 类图

类图给我们的第一印象是：繁杂。我们抓住关键点：Future 和 Promise 两条分支，分而治之。我们使用自顶向下的方法分析其实现细节，使用两条线索：

```
AbstractFuture<--CompleteFuture<--CompleteChannelFuture<--Succeeded/FailedChannelFuture
DefaultPromise<--DefaultChannelPromise
```

### []( "5.2 Future")5.2 Future

### []( "5.2.1 AbstractFuture")5.2.1 AbstractFuture

AbstractFuture 主要实现 Future 的 get()方法，取得 Future 关联的异步操作结果：

```
@Override
public V get() throws InterruptedException, ExecutionException{
await(); // 阻塞直到异步操作完成
Throwable cause = cause();
if (cause == null) {
return getNow(); // 成功则返回关联结果
}
if (cause instanceof CancellationException) {
throw (CancellationException) cause; // 由用户取消
}
throw new ExecutionException(cause); // 失败抛出异常
}
```

其中的实现简单明了，但关键调用方法的具体实现并没有，我们将在子类实现中分析。对应的加入超时时间的 get(long timeout, TimeUnit unit)实现也类似，不再列出。

### []( "5.2.2 CompleteFuture")5.2.2 CompleteFuture

Complete 表示操作已完成，所以 CompleteFuture 表示一个异步操作已完成的结果，由此可推知：该类的实例在异步操作完成时创建，返回给用户，用户则使用 addListener()方法定义一个异步操作。如果你熟悉 javascript，将 Listener 类比于回调函数 callback()可方便理解。
我们首先看其中的字段和构造方法：

```
// 执行器，执行Listener中定义的操作
private final EventExecutor executor;
// 这有一个构造方法，可知executor是必须的
protected CompleteFuture(EventExecutor executor){
this.executor = executor;
}
```

CompleteFuture 类定义了一个 EventExecutor，可视为一个线程，用于执行 Listener 中的操作。我们再看 addListener()和 removeListener()方法：

```
public Future<V> addListener(GenericFutureListener<? extends Future<? super V>> listener){
// 由于这是一个已完成的Future，所以立即通知Listener执行
DefaultPromise.notifyListener(executor(), this, listener);
return this;
}
public Future<V> removeListener(GenericFutureListener<? extends Future<? super V>> listener){
// 由于已完成，Listener中的操作已完成，没有需要删除的Listener
return this;
}
```

其中的实现也很简单，我们看一下 GenericFutureListener 接口，其中只定义了一个方法：

```
// 异步操作完成是调用
void operationComplete(F future) throws Exception;
```

关于 Listener 我们再关注一下 ChannelFutureListener，它并没有扩展 GenericFutureListener 接口，所以类似于一个标记接口。我们看其中实现的三个通用 ChannelFutureListener：

```
ChannelFutureListener CLOSE = (future) --> {
future.channel().close(); //操作完成时关闭Channel
};
ChannelFutureListener CLOSE_ON_FAILURE = (future) --> {
if (!future.isSuccess()) {
future.channel().close(); // 操作失败时关闭Channel
}
};
ChannelFutureListener FIRE_EXCEPTION_ON_FAILURE = (future) --> {
if (!future.isSuccess()) {
// 操作失败时触发一个ExceptionCaught事件
future.channel().pipeline().fireExceptionCaught(future.cause());
}
};
```

这三个 Listener 对象定义了对 Channel 处理时常用的操作，如果符合需求，可以直接使用。
由于 CompleteFuture 表示一个已完成的异步操作，所以可推知 sync()和 await()方法都将立即返回。此外，可推知线程的状态如下，不再列出代码：

```
isDone() = true; isCancelled() = false;
```

### []( "5.2.3 CompleteChannelFuture")5.2.3 CompleteChannelFuture

CompleteChannelFuture 的类签名如下：

```
abstract class CompleteChannelFuture extends CompleteFuture<Void> implements ChannelFuture
```

ChannelFuture 是不是觉得很亲切？你肯定已经使用过 ChannelFuture。ChannelFuture 接口相比于 Future 只扩展了一个方法 channel()用于取得关联的 Channel 对象。CompleteChannelFuture 还继承了 CompleteFuture，尖括号中的泛型表示 Future 关联的结果，此结果为 Void，意味着 CompleteChannelFuture 不关心这个特定结果即 get()相关方法返回 null。也就是说，我们可以将 CompleteChannelFuture 纯粹的视为一种回调函数机制。
CompleteChannelFuture 的字段只有一个：

```
private final Channel channel; // 关联的Channel对象
```

CompleteChannelFuture 的大部分方法实现中，只是将方法返回的 Future 覆盖为 ChannelFuture 对象（ChannelFuture 接口的要求），代码不在列出。我们看一下 executor()方法：

```
@Override
protected EventExecutor executor(){
EventExecutor e = super.executor(); // 构造方法指定
if (e == null) {
return channel().eventLoop(); // 构造方法未指定使用channel注册到的eventLoop
} else {
return e;
}
}
```

### []( "5.2.4 Succeeded/FailedChannelFuture")5.2.4 Succeeded/FailedChannelFuture

Succeeded/FailedChannelFuture 为特定的两个异步操作结果，回忆总述中关于 Future 状态的讲解，成功意味着

```
Succeeded: isSuccess() == true, cause() == null;
Failed: isSuccess() == false, cause() == non-null
```

代码中的实现也很简单，不再列出。需要注意的是，其中的构造方法不建议用户调用，一般使用 Channel 对象的方法 newSucceededFuture()和 newFailedFuture(Throwable)代替。

### []( "5.3 Promise")5.3 Promise

### []( "5.3.1 DefaultPromise")5.3.1 DefaultPromise

我们首先看其中的 static 字段：

```
// 可以嵌套的Listener的最大层数，可见最大值为8
private static final int MAX_LISTENER_STACK_DEPTH = Math.min(8,
SystemPropertyUtil.getInt("io.netty.defaultPromise.maxListenerStackDepth", 8));
// result字段由使用RESULT_UPDATER更新
private static final AtomicReferenceFieldUpdater<DefaultPromise, Object> RESULT_UPDATER;
// 此处的Signal是Netty定义的类，继承自Error，异步操作成功且结果为null时设置为改值
private static final Signal SUCCESS = Signal.valueOf(DefaultPromise.class.getName() + ".SUCCESS");
// 异步操作不可取消
private static final Signal UNCANCELLABLE = Signal.valueOf(...);
// 异步操作失败时保存异常原因
private static final CauseHolder CANCELLATION_CAUSE_HOLDER = new CauseHolder(...);
```

嵌套的 Listener，是指在 listener 的 operationComplete 方法中，可以再次使用 future.addListener()继续添加 listener，Netty 限制的最大层数是 8，用户可使用系统变量 io.netty.defaultPromise.maxListenerStackDepth 设置。
再看其中的私有字段：

```
// 异步操作结果
private volatile Object result;
// 执行listener操作的执行器
private final EventExecutor executor;
// 监听者
private Object listeners;
// 阻塞等待该结果的线程数
private short waiters;
// 通知正在进行标识
private boolean notifyingListeners;
```

也许你已经注意到，listeners 是一个 Object 类型。这似乎不合常理，一般情况下我们会使用一个集合或者一个数组。Netty 之所以这样设计，是因为大多数情况下 listener 只有一个，用集合和数组都会造成浪费。当只有一个 listener 时，该字段为一个 GenericFutureListener 对象；当多余一个 listener 时，该字段为 DefaultFutureListeners，可以储存多个 listener。明白了这些，我们分析关键方法 addListener()：

```
@Override
public Promise<V> addListener(GenericFutureListener<? extends Future<? super V>> listener){
synchronized (this) {
addListener0(listener); // 保证多线程情况下只有一个线程执行添加操作
}
if (isDone()) {
notifyListeners(); // 异步操作已经完成通知监听者
}
return this;
}
private void addListener0(GenericFutureListener<? extends Future<? super V>> listener){
if (listeners == null) {
listeners = listener; // 只有一个
} else if (listeners instanceof DefaultFutureListeners) {
((DefaultFutureListeners) listeners).add(listener); // 大于两个
} else {
// 从一个扩展为两个
listeners = new DefaultFutureListeners((GenericFutureListener<? extends Future<V>>) listeners, listener);
}
}
```

从代码中可以看出，在添加 Listener 时，如果异步操作已经完成，则会 notifyListeners()：

```
private void notifyListeners(){
EventExecutor executor = executor();
if (executor.inEventLoop()) { //执行线程为指定线程
final InternalThreadLocalMap threadLocals = InternalThreadLocalMap.get();
final int stackDepth = threadLocals.futureListenerStackDepth(); // 嵌套层数
if (stackDepth < MAX_LISTENER_STACK_DEPTH) {
// 执行前增加嵌套层数
threadLocals.setFutureListenerStackDepth(stackDepth + 1);
try {
notifyListenersNow();
} finally {
// 执行完毕，无论如何都要回滚嵌套层数
threadLocals.setFutureListenerStackDepth(stackDepth);
}
return;
}
}
// 外部线程则提交任务给执行线程
safeExecute(executor, () -> { notifyListenersNow(); });
}
private static void safeExecute(EventExecutor executor, Runnable task){
try {
executor.execute(task);
} catch (Throwable t) {
rejectedExecutionLogger.error("Failed to submit a listener notification task. Event loop shut down?", t);
}
}
```

所以，外部线程不能执行监听者 Listener 中定义的操作，只能提交任务到指定 Executor，其中的操作最终由指定 Executor 执行。我们再看 notifyListenersNow()方法：

```
private void notifyListenersNow(){
Object listeners;
// 此时外部线程可能会执行添加Listener操作，所以需要同步
synchronized (this) {
if (notifyingListeners || this.listeners == null) {
// 正在通知或已没有监听者（外部线程删除）直接返回
return;
}
notifyingListeners = true;
listeners = this.listeners;
this.listeners = null;
}
for (;;) {
if (listeners instanceof DefaultFutureListeners) { // 通知单个
notifyListeners0((DefaultFutureListeners) listeners);
} else { // 通知多个（遍历集合调用单个）
notifyListener0(this, (GenericFutureListener<? extends Future<V>>) listeners);
}
synchronized (this) {
// 执行完毕且外部线程没有再添加监听者
if (this.listeners == null) {
notifyingListeners = false;
return;
}
// 外部线程添加了监听者继续执行
listeners = this.listeners;
this.listeners = null;
}
}
}
private static void notifyListener0(Future future, GenericFutureListener l){
try {
l.operationComplete(future);
} catch (Throwable t) {
logger.warn("An exception was thrown by " + l.getClass().getName() + ".operationComplete()", t);
}
}
```

到此为止，我们分析完了 Promise 最重要的 addListener()和 notifyListener()方法。在源码中还有 static 的 notifyListener()方法，这些方法是 CompleteFuture 使用的，对于 CompleteFuture，添加监听者的操作不需要缓存，直接执行 Listener 中的方法即可，执行线程为调用线程，相关代码可回顾 CompleteFuture。addListener()相对的 removeListener()方法实现简单，我们不再分析。
回忆 result 字段，修饰符有 volatile，所以使用 RESULT_UPDATER 更新，保证更新操作为原子操作。Promise 不携带特定的结果（即携带 Void）时，成功时设置为静态字段的 Signal 对象 SUCCESS；如果携带泛型参数结果，则设置为泛型一致的结果。对于 Promise，设置成功、设置失败、取消操作，**三个操作至多只能调用一个且同一个方法至多生效一次**，再次调用会抛出异常（set）或返回失败（try）。这些设置方法原理相同，我们以 setSuccess()为例分析:

```
public Promise<V> setSuccess(V result){
if (setSuccess0(result)) {
notifyListeners(); // 可以设置结果说明异步操作已完成，故通知监听者
return this;
}
throw new IllegalStateException("complete already: " + this);
}
private boolean setSuccess0(V result){
// 为空设置为Signal对象Success
return setValue0(result == null ? SUCCESS : result);
}
private boolean setValue0(Object objResult){
// 只有结果为null或者UNCANCELLABLE时才可设置且只可以设置一次
if (RESULT_UPDATER.compareAndSet(this, null, objResult) ||
RESULT_UPDATER.compareAndSet(this, UNCANCELLABLE, objResult)) {
checkNotifyWaiters(); // 通知等待的线程
return true;
}
return false;
}
```

checkNotifyWaiters()方法唤醒调用 await()和 sync()方法等待该异步操作结果的线程，代码如下：

```
private synchronized void checkNotifyWaiters(){
// 确实有等待的线程才notifyAll
if (waiters > 0) {
notifyAll(); // JDK方法
}
}
```

有了唤醒操作，那么 sync()和 await()的实现是怎么样的呢？我们首先看 sync()的代码：

```
public Promise<V> sync() throws InterruptedException{
await();
rethrowIfFailed(); // 异步操作失败抛出异常
return this;
}
```

可见，sync()和 await()很类似，区别只是 sync()调用，如果异步操作失败，则会抛出异常。我们接着看 await()的实现：

```
public Promise<V> await() throws InterruptedException{
// 异步操作已经完成，直接返回
if (isDone()) {
return this;
}
if (Thread.interrupted()) {
throw new InterruptedException(toString());
}
// 死锁检测
checkDeadLock();
// 同步使修改waiters的线程只有一个
synchronized (this) {
while (!isDone()) { // 等待直到异步操作完成
incWaiters(); // ++waiters;
try {
wait(); // JDK方法
} finally {
decWaiters(); // --waiters
}
}
}
return this;
}
```

其中的实现简单明了，其他 await()方法也类似，不再分析。我们注意其中的 checkDeadLock()方法用来进行死锁检测：

```
protected void checkDeadLock() {
EventExecutor e = executor();
if (e != null && e.inEventLoop()) {
throw new BlockingOperationException(toString());
}
}
```

也就是说，**不能在同一个线程中调用 await()相关的方法**。为了更好的理解这句话，我们使用代码注释中的例子来解释。Handler 中的 channelRead()方法是由 Channel 注册到的 eventLoop 执行的，其中的 Future 的 Executor 也是这个 eventLoop，所以不能在 channelRead()方法中调用 await 这一类（包括 sync）方法。

```
// 错误的例子
public void channelRead(ChannelHandlerContext ctx, Object msg){
ChannelFuture future = ctx.channel().close();
future.awaitUninterruptibly();
// ...
}
// 正确的做法
public void channelRead(ChannelHandlerContext ctx, Object msg){
ChannelFuture future = ctx.channel().close();
future.addListener(new ChannelFutureListener() {
public void operationComplete(ChannelFuture future){
// ... 使用异步操作
}
});
}
```

到了这里，我们已经分析完 Future 和 Promise 的主要实现。剩下的 DefaultChannelPromise、VoidChannelPromise 实现都很简单，我们不再分析。ProgressivePromise 表示异步的进度结果，也不再进行分析。

# []( "666. 彩蛋")666. 彩蛋

一条有趣的评论：
其实 Netty 在实现 Future 接口的 cancel 和 isDone 方法时违反了 Java 的约定规则，请参见文章：[https://www.jianshu.com/p/6a87ceb7f70a](https://www.jianshu.com/p/6a87ceb7f70a)
