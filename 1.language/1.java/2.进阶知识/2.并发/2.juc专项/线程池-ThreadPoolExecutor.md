# JUC源码分析-线程池篇（一）：ThreadPoolExecutor
[目录](readme.md)
[JUC源码分析-线程池篇（一）：ThreadPoolExecutor](https://www.jianshu.com/p/7be43712ef21)

在多线程编程中，任务都是一些抽象且离散的工作单元，而线程是使任务异步执行的基本机制。随着应用的扩张，线程和任务管理也变得非常复杂，为了简化这些复杂的线程管理模式，我们需要一个“管理者”来统一管理线程及任务分配，这就是线程池。本章开始，我们将逐个分析 JUC 框架中几种不同的线程池。首先来认识一下我们的老朋友—ThreadPoolExecutor

# 概述

ThreadPoolExecutor 是线程池的核心实现。线程的创建和终止需要很大的开销，线程池中预先提供了指定数量的可重用线程，所以使用线程池会节省系统资源，并且每个线程池都维护了一些基础的数据统计，方便线程的管理和监控。

线程池的使用想必大家都很熟悉了，这里笔者也大概讲一下。线程池的创建一般由工具类 Executors 来完成，当然我们也可以根据业务需求来定义自己需要的线程池。Executors 为线程池提供了三种不同的构造，每种构造也都可以自定义线程工厂(ThreadFactory)：

* **newFixedThreadPool**：指定大小的线程池，使用 LinkedBlockingQueue 作为等待队列。
* **newSingleThreadExecutor**：只有一个工作线程的线程池，使用 LinkedBlockingQueue 作为等待队列。如果内部工作线程由于异常而被终止，则会新建一个线程替代它的位置。
* **newCachedThreadPool**：无容量线程池，核心线程数为0，工作线程空闲60秒后会被自动回收。使用非公平模式的 SynchronousQueue 作为等待队列（详见[JUC源码分析-集合篇（八）：SynchronousQueue](https://www.jianshu.com/p/c4855acb57ec)）。只有在需要时（新任务到来时）才创建新的线程，如果有空闲线程则会重用。适合执行周期较小的异步任务。

**注意**：newFixedThreadPool(1, threadFactory)**不等价于**newSingleThreadExecutor

newSingleThreadExecutor创建的线程池保证内部只有一个线程执行任务，并且**线程数不可扩展**；而通过

newFixedThreadPool(1, threadFactory)创建的线程池可以通过setCorePoolSize方法来修改核心线程数。

### corePoolSize & maximumPoolSize

核心线程数（corePoolSize）和最大线程数（maximumPoolSize）是线程池中非常重要的两个概念，希望同学们能够掌握。
当一个新任务被提交到池中，如果当前运行线程小于核心线程数（corePoolSize），即使当前有空闲线程，也会新建一个线程来处理新提交的任务；如果当前运行线程数大于核心线程数（corePoolSize）并小于最大线程数（maximumPoolSize），只有当等待队列已满的情况下才会新建线程。

### 等待队列

任何阻塞队列（BlockingQueue）都可以用来转移或保存提交的任务，线程池大小和阻塞队列相互约束线程池：

1. 如果运行线程数小于corePoolSize，提交新任务时就会新建一个线程来运行；
2. 如果运行线程数大于或等于corePoolSize，新提交的任务就会入列等待；如果队列已满，并且运行线程数小于maximumPoolSize，也将会新建一个线程来运行；
3. 如果线程数大于maximumPoolSize，新提交的任务将会根据**拒绝策略**来处理。

下面来看一下三种通用的入队策略：

1. **直接传递**：通过 SynchronousQueue 直接把任务传递给线程。如果当前没可用线程，尝试入队操作会失败，然后再创建一个新的线程。当处理可能具有内部依赖性的请求时，该策略会避免请求被锁定。直接传递通常需要无界的最大线程数（maximumPoolSize），避免拒绝新提交的任务。当任务持续到达的平均速度超过可处理的速度时，可能导致线程的无限增长。
2. **无界队列**：使用无界队列（如 LinkedBlockingQueue）作为等待队列，当所有的核心线程都在处理任务时， 新提交的任务都会进入队列等待。因此，不会有大于 corePoolSize 的线程会被创建（maximumPoolSize 也将失去作用）。这种策略适合每个任务都完全独立于其他任务的情况；例如网站服务器。这种类型的等待队列可以使瞬间爆发的高频请求变得平滑。当任务持续到达的平均速度超过可处理速度时，可能导致等待队列无限增长。
3. **有界队列**：当使用有限的最大线程数时，有界队列（如 ArrayBlockingQueue）可以防止资源耗尽，但是难以调整和控制。队列大小和线程池大小可以相互作用：使用大的队列和小的线程数可以减少CPU使用率、系统资源和上下文切换的开销，但是会导致吞吐量变低，如果任务频繁地阻塞（例如被I/O限制），系统就能为更多的线程调度执行时间。使用小的队列通常需要更多的线程数，这样可以最大化CPU使用率，但可能会需要更大的调度开销，从而降低吞吐量。

### 拒绝策略

当线程池已经关闭或达到饱和（最大线程和队列都已满）状态时，新提交的任务将会被拒绝。 ThreadPoolExecutor 定义了四种拒绝策略：

1. **AbortPolicy**：默认策略，在需要拒绝任务时抛出RejectedExecutionException；
2. **CallerRunsPolicy**：直接在 execute 方法的调用线程中运行被拒绝的任务，如果线程池已经关闭，任务将被丢弃；
3. **DiscardPolicy**：直接丢弃任务；
4. **DiscardOldestPolicy**：丢弃队列中等待时间最长的任务，并执行当前提交的任务，如果线程池已经关闭，任务将被丢弃。

我们也可以自定义拒绝策略，只需要实现 RejectedExecutionHandler； 需要注意的是，拒绝策略的运行需要指定线程池和队列的容量。

### 线程池状态

ThreadPoolExecutor 通过一个 int 型参数 ctl 来控制池状态，并且封装了两个概念字段：
**workerCount**：表示工作线程数，最大为(2^29)-1
**runState**：提供了对池生命周期的控制，包括以下几种状态：

* **RUNNING**：可以接收新的任务和队列任务
* **SHUTDOWN**：不接收新的任务，但是会运行队列任务
* **STOP**：不接收新任务，也不会运行队列任务，并且中断正在运行的任务
* **TIDYING**：所有任务都已经终止，workerCount为0，当池状态为TIDYING时将会运行terminated()方法
* **TERMINATED**：terminated函数完成执行。

线程池的状态转化如下：
![](https://upload-images.jianshu.io/upload_images/6050820-1d912bf6dadc8e21.png)

线程池状态转化

### 钩子方法

ThreadPoolExecutor 提供了可覆盖的钩子方法：

beforeExecute、afterExecute 和 terminated，分别在每个任务调用之前/之后和池关闭之后执行。这些可以用来操作执行环境，例如，重新初始化 ThreadLocal、数据收集统计、日志添加等。如果钩子方法或回调方法抛出异常，内部工作线程也会失败并销毁。此外 ThreadPoolExecutor 也为 ScheduledThreadPoolExecutor 提供了一个专门的钩子方法

onShutdown，用来处理关闭线程池时的逻辑，后面我们介绍 ScheduledThreadPoolExecutor 的时候再详细讲解。

# 数据结构和核心参数

![](https://upload-images.jianshu.io/upload_images/6050820-153aa5cf8548741c.png?imageMogr2/auto-orient/strip|imageView2/2/w/659/format/webp)

ThreadPoolExecutor 继承关系

**Worker**： ThreadPoolExecutor 的内部类，继承自 AQS，实现了不可重入的互斥锁。在线程池中持有一个 Worker 集合，一个 Worker 对应一个工作线程。当线程池启动时，对应的worker会执行池中的任务，执行完毕后从阻塞队列里获取一个新的任务继续执行。它本身实现了Runnable接口，也就是说 Worker 本身也作为一个线程任务执行。
Worker内部维护了三个变量，用来记录每个工作线程的状态：

```java
//工作线程
final Thread thread;
//初始运行任务
Runnable firstTask;
//任务完成计数
volatile long completedTasks;
```

### 核心参数

```java
/**当核心线程数已满，新增任务的存储队列*/
private final BlockingQueue<Runnable> workQueue;
/**线程运行期间的锁，在调用shutdown和shutdownNow之后依然持有*/
private final ReentrantLock mainLock = new ReentrantLock();
/**工作线程池，只有在持有mainLock才存储*/
private final HashSet<Worker> workers = new HashSet<Worker>();
/**awaitTermination的等待条件*/
private final Condition termination = mainLock.newCondition();
/**最大池容量*/
private int largestPoolSize;
/**已完成任务数量*/
private long completedTaskCount;
/**线程工厂，所有线程都是用它来创建(通过addWorker方法)*/
private volatile ThreadFactory threadFactory;
/**在执行期间调用饱和或关闭时的处理*/
private volatile RejectedExecutionHandler handler;
/**空闲线程保活时长*/
private volatile long keepAliveTime;
/**默认false，表示core线程空闲依然保活；
 * 如果为true，使用keepAliveTime确定等待超时时间*/
private volatile boolean allowCoreThreadTimeOut;
/**核心线程池大小
 * 超过核心线程数之后提交的任务将被放到等待队列中
 * */
private volatile int corePoolSize;
/**最大线程池大小
 * 如果当前等待队列任务已满，继续提交的任务将继续创建新的线程执行，这个线程数最大为maximumPoolSize
 * */
private volatile int maximumPoolSize;
/**默认拒绝策略*/
private static final RejectedExecutionHandler defaultHandler = new AbortPolicy();
/**针对shutdown和shutdownNow的运行权限许可*/
private static final RuntimePermission shutdownPerm = new RuntimePermission("modifyThread");

```

# 源码解析

本章我们主要针对execute方法进行讲解，submit方法在之后对 FutureTask 进行解析的时候再详细分析。

首先来看一下 ThreadPoolExecutor 的构造函数：

```java
public ThreadPoolExecutor(int corePoolSize,
                              int maximumPoolSize,
                              long keepAliveTime,
                              TimeUnit unit,
                              BlockingQueue<Runnable> workQueue,
                              ThreadFactory threadFactory,
                              RejectedExecutionHandler handler) {
        if (corePoolSize < 0 ||
            maximumPoolSize <= 0 ||
            maximumPoolSize < corePoolSize ||
            keepAliveTime < 0)
            throw new IllegalArgumentException();
        if (workQueue == null || threadFactory == null || handler == null)
            throw new NullPointerException();
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.workQueue = workQueue;
        this.keepAliveTime = unit.toNanos(keepAliveTime);
        this.threadFactory = threadFactory;
        this.handler = handler;
}

```

ThreadPoolExecutor 为用户提供了更广阔的控制权限，所以理解 ThreadPoolExecutor 中每个参数的涵义可以使我们更加得心应手的根据业务需求制定我们自己的线程池。

下面我们将从方法execute开始，逐步深入解析 ThreadPoolExecutor。

## execute(Runnable command)

```java
public void execute(Runnable command) {
    if (command == null)
        throw new NullPointerException();
  
    int c = ctl.get();
    if (workerCountOf(c) < corePoolSize) {
        if (addWorker(command, true))//添加到工作线程
            return;
        c = ctl.get();
    }
    if (isRunning(c) && workQueue.offer(command)) {//池正在运行，添加到等待队列
        int recheck = ctl.get();
        if (! isRunning(recheck) && remove(command))//池状态>=SHUTDOWN，移除任务，执行拒绝策略
            reject(command);
        else if (workerCountOf(recheck) == 0)//工作线程为空，添加新的工作线程
            addWorker(null, false);
    }
    else if (!addWorker(command, false))
        reject(command);
}
```

**说明**：
提交一个任务到线程池，任务不一定会立即执行。提交的任务可能在一个新的线程中执行，也可能在已经存在的空闲线程中执行。
如果由于池关闭或者池容量已经饱和导致任务无法提交，那么就根据拒绝策略RejectedExecutionHandler处理提交过来的任务。

execute的运行分三种情况：

1. 如果正在运行线程少于corePoolSize，通过addWorker方法尝试开启一个新的线程并把提交的任务作为它的firstTask运行。
addWorker会检查ctl状态的状态（runState和workerCount）来判断是否可以添加新的线程。
2. 如果addWorker执行失败（返回false），就把任务添加到等待队列。这里需要对ctl进行双重检查，因为从任务入队到入队完成可能有线程死掉，或者在进入此方法后线程池被关闭。
所以我们要在入队后重新检查池状态，如果有必要，就回滚入队操作。
3. 如果任务不能入队，我们再次尝试增加一个新的线程。如果添加失败，就意味着池被关闭或已经饱和，这种情况就需要根据拒绝策略来处理任务。

### addWorker(Runnable firstTask, boolean core)

```java
private boolean addWorker(Runnable firstTask, boolean core) {
    //自旋，判断可以添加线程的前提条件
    retry:
    for (;;) {
        int c = ctl.get();
        int rs = runStateOf(c);//获取runState

        // Check if queue empty only if necessary.
        //检查池状态
        if (rs >= SHUTDOWN &&
            ! (rs == SHUTDOWN &&
               firstTask == null &&
               ! workQueue.isEmpty()))
            return false;

        for (;;) {//检查工作线程数是否饱和
            int wc = workerCountOf(c);//获取工作线程数
            if (wc >= CAPACITY ||
                wc >= (core ? corePoolSize : maximumPoolSize))
                return false;
            if (compareAndIncrementWorkerCount(c))//可以添加新的线程，递增ctl，跳出retry自旋
                break retry;
            //更新ctl失败，重读ctl继续循环检查
            c = ctl.get();  // Re-read ctl
            if (runStateOf(c) != rs)
                continue retry;
            // else CAS failed due to workerCount change; retry inner loop
        }
    }

    boolean workerStarted = false;
    boolean workerAdded = false;
    Worker w = null;
    try {
        //创建新的工作线程
        w = new Worker(firstTask);
        final Thread t = w.thread;
        if (t != null) {
            final ReentrantLock mainLock = this.mainLock;
            mainLock.lock();//加锁，准备添加新的工作线程
            try {
                // Recheck while holding lock.
                // Back out on ThreadFactory failure or if
                // shut down before lock acquired.
                int rs = runStateOf(ctl.get());
                //重新检查runState
                if (rs < SHUTDOWN ||
                    (rs == SHUTDOWN && firstTask == null)) {
                    if (t.isAlive()) // precheck that t is startable
                        throw new IllegalThreadStateException();//线程已经被启动，抛出异常
                    workers.add(w);//添加工作线程
                    int s = workers.size();
                    if (s > largestPoolSize)
                        largestPoolSize = s;//更新最大池容量
                    workerAdded = true;
                }
            } finally {
                mainLock.unlock();
            }
            if (workerAdded) {//添加成功，启动线程
                t.start();
                workerStarted = true;
            }
        }
    } finally {
        if (! workerStarted)
            addWorkerFailed(w);//添加失败，回滚操作
    }
    return workerStarted;
}
```

**说明**：

说明：addWorker用来尝试添加一个新的工作线程。在任务提交（execute方法）、更新核心线程数（setCorePoolSize方法）、预启动线程（prestartCoreThread方法）中都有用到。
函数执行逻辑如下：首先检查当前池状态和给定界限中（核心线程数或最大线程数）是否可以添加新工作线程。如果可以，需要对workercount做出相应调整。添加完毕后，启动给定任务firstTask。
如果线程池已停止或正在关闭或Threadfactory创建线程失败返回false。
最后，如果由于Threadfactory返回null或创建线程过程中抛出异常导致工作线程创建失败，则调用addWorkerFailed回滚添加工作线程操作。
addWorkerFailed源码如下：

```java
private void addWorkerFailed(Worker w) {
    final ReentrantLock mainLock = this.mainLock;
    mainLock.lock();
    try {
        if (w != null)
            workers.remove(w);
        decrementWorkerCount();//workerCount-1
        tryTerminate();//尝试终止线程池
    } finally {
        mainLock.unlock();
    }
}

```

## Worker.runWorker(Worker w)

```java
final void runWorker(Worker w) {
    Thread wt = Thread.currentThread();
    Runnable task = w.firstTask;
    w.firstTask = null;
    //任务线程的锁状态默认为-1，此时解锁+1，变为0，即锁打开状态，允许中断，在任务未执行之前不允许中断。
    w.unlock(); // allow interrupts
    boolean completedAbruptly = true;//工作线程是否因异常而退出
    try {
        while (task != null || (task = getTask()) != null) {
            w.lock();//加锁
            
            if ((runStateAtLeast(ctl.get(), STOP) ||
                 (Thread.interrupted() &&
                  runStateAtLeast(ctl.get(), STOP))) &&
                !wt.isInterrupted())
                wt.interrupt();//中断线程
            try {
                beforeExecute(wt, task);//执行前逻辑，自定义实现
                Throwable thrown = null;
                try {
                    task.run();//执行任务
                } catch (RuntimeException x) {
                    thrown = x; throw x;
                } catch (Error x) {
                    thrown = x; throw x;
                } catch (Throwable x) {
                    thrown = x; throw new Error(x);
                } finally {
                    afterExecute(task, thrown);//执行后逻辑，自定义实现
                }
            } finally {
                task = null;
                w.completedTasks++;
                w.unlock();
            }
        }
        completedAbruptly = false;
    } finally {
        //处理工作线程退出逻辑
        processWorkerExit(w, completedAbruptly);
    }
}

```

说明：runWorker是工作线程运行的核心方法，循环从队列中获取任务并执行。
工作线程启动后，会首先运行内部持有的任务firstTask，如果firstTask为null，则循环调用getTask方法从队列中获取任务执行。在任务执行前后可调用beforeExecute和afterExecute处理执行前后的逻辑，这两个方法在线程池中都是空方法，可根据业务需求自定义实现。
如果线程池正在停止(stopping)，需要确保线程被中断；否则的话需要确保线程没有被中断。这里针对两种情况需要进行复查，以处理在清除中断时的shutdownNow事件。


任务获取方法getTask源码如下：

```java
private Runnable getTask() {
    boolean timedOut = false; // Did the last poll() time out?

    for (;;) {
        int c = ctl.get();
        int rs = runStateOf(c);//获取runState

        // Check if queue empty only if necessary.
        if (rs >= SHUTDOWN && (rs >= STOP || workQueue.isEmpty())) {//线程池已经关闭或等待队列为null
            decrementWorkerCount();
            return null;
        }

        int wc = workerCountOf(c);//获取工作线程数workerCount

        // Are workers subject to culling?
        boolean timed = allowCoreThreadTimeOut || wc > corePoolSize;//是否允许超时

        if ((wc > maximumPoolSize || (timed && timedOut))
            && (wc > 1 || workQueue.isEmpty())) {
            if (compareAndDecrementWorkerCount(c))
                return null;
            continue;//修改ctl失败，继续循环
        }

        try {
            Runnable r = timed ?
                workQueue.poll(keepAliveTime, TimeUnit.NANOSECONDS) : //等待指定超时时间
                workQueue.take();//出队，等待直到元素可用
            if (r != null)
                return r;
            timedOut = true;
        } catch (InterruptedException retry) {

            timedOut = false;
        }
    }
}

```

**说明**：获取等待队列中的任务，基于当前线程池的配置来决定执行任务阻塞、等待或返回null。
在以下四个情况下会引起worker退出，并返回null：
1. 工作线程数大于maximumPoolSize
2. 线程池已停止（STOP）
3. 线程池已关闭（SHUTDOWN）并且等待队列为空
4. 工作线程等待任务超时

由于上述条件返回null后，需要递减workerCount回到runWorker
方法，当工作线程处理完所有的任务之后，会调用processWorkerExit
处理工作线程退出的逻辑，源码如下：

```java
private void processWorkerExit(Worker w, boolean completedAbruptly) {
    if (completedAbruptly) // If abrupt, then workerCount wasn't adjusted
        //如果任务线程被中断，则工作线程数量减1
        decrementWorkerCount();

    final ReentrantLock mainLock = this.mainLock;
    mainLock.lock();
    try {
        completedTaskCount += w.completedTasks;//更新完成任务数
        workers.remove(w);//移除工作线程
    } finally {
        mainLock.unlock();
    }

    tryTerminate();//尝试终止线程池

    int c = ctl.get();
    if (runStateLessThan(c, STOP)) {//线程池尚未完全停止
        if (!completedAbruptly) {//工作线程非异常退出
            //获取当前核心线程数
            int min = allowCoreThreadTimeOut ? 0 : corePoolSize;
            if (min == 0 && ! workQueue.isEmpty())
                //如果允许空闲工作线程等待任务，且任务队列不为空，则min为1
                min = 1;
            if (workerCountOf(c) >= min)//工作线程数大于核心线程数，直接返回
                return; // replacement not needed
        }
        addWorker(null, false);//继续尝试添加新的工作线程
    }
}
```

**说明**：工作线程处理完所有的任务之后，调用此方法处理工作线程退出逻辑。为已经死亡的工作线程执行相关的清除操作。此方法会从线程池内的工作线程集合（workers）中移除当前工作线程，并会尝试终止线程池。
在下面几种情况下，可能会替换当前工作线程：

1. 用户任务执行异常导致线程退出
2. 工作线程数少于corePoolSize
3. 等待队列不为空但没有工作线程

## tryTerminate()

```java
final void tryTerminate() {
    for (;;) {
        int c = ctl.get();
        if (isRunning(c) || //正在运行
            runStateAtLeast(c, TIDYING) || //状态大于TIDYING
            (runStateOf(c) == SHUTDOWN && ! workQueue.isEmpty())) //状态为shutdown并且等待队列不为空
            return;
        if (workerCountOf(c) != 0) { // Eligible to terminate
            interruptIdleWorkers(ONLY_ONE);//中断空闲线程
            return;
        }

        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            //线程池已经关闭，等待队列为空，并且工作线程等于0，更新池状态为TIDYING
            if (ctl.compareAndSet(c, ctlOf(TIDYING, 0))) {
                try {
                    terminated();//线程池终止后操作，需自定义实现
                } finally {
                    ctl.set(ctlOf(TERMINATED, 0));
                    termination.signalAll();//唤醒等待池结束的线程
                }
                return;
            }
        } finally {
            mainLock.unlock();
        }
        // else retry on failed CAS
    }
}
```

**说明**：

说明：tryTerminate用于尝试终止线程池，在shutdow()、shutdownNow()、remove()中均是通过此方法来终止线程池。此方法必须在任何可能导致终止的行为之后被调用，例如减少工作线程数，移除队列中的任务，或者是在工作线程运行完毕后处理工作线程退出逻辑的方法processWorkerExit。
如果线程池可被终止（状态为SHUTDOWN并且等待队列和池任务都为空，或池状态为STOP且池任务为空），调用此方法转换线程池状态为TERMINATED。
如果线程池可以被终止，但是当前工作线程数大于0，则调用interruptIdleWorkers方法先中断一个空闲的工作线程，用来保证池关闭操作继续向下传递。interruptIdleWorkers源码如下：


```java
private void interruptIdleWorkers(boolean onlyOne) {
    final ReentrantLock mainLock = this.mainLock;
    mainLock.lock();
    try {
        for (Worker w : workers) {
            Thread t = w.thread;
            if (!t.isInterrupted() && w.tryLock()) {
                try {
                    t.interrupt();
                } catch (SecurityException ignore) {
                } finally {
                    w.unlock();
                }
            }
            if (onlyOne)
                break;
        }
    } finally {
        mainLock.unlock();
    }
}
```

## shutdown()

```java
public void shutdown() {
    final ReentrantLock mainLock = this.mainLock;
    mainLock.lock();
    try {
        checkShutdownAccess();//检查关闭权限
        advanceRunState(SHUTDOWN);//修改运行状态runState
        interruptIdleWorkers();//中断空闲工作线程
        //为ScheduledThreadPoolExecutor提供的关闭钩子程序
        onShutdown(); // hook for ScheduledThreadPoolExecutor
    } finally {
        mainLock.unlock();
    }
    tryTerminate();//销毁线程池
}
```

**说明**：启动一个有序的关闭方式，在关闭之前已提交的任务会被执行，但不会接收新任务。此方法不会等待已提交任务执行完毕（通过awaitTermination方法可以等待任务完成之后再关闭）。方法内部的调用在上面都已经介绍过，不多赘述。

## shutdownNow()

```java
public List<Runnable> shutdownNow() {
    List<Runnable> tasks;
    final ReentrantLock mainLock = this.mainLock;
    mainLock.lock();
    try {
        checkShutdownAccess();//检查关闭权限
        advanceRunState(STOP);//修改运行状态runState
        interruptWorkers();//中断所有线程
        tasks = drainQueue();//移除所有等待队列的任务
    } finally {
        mainLock.unlock();
    }
    tryTerminate();
    return tasks;
}
```

说明：停止线程池内所有的任务（包括正在执行和正在等待的任务），并返回正在等待执行的任务列表。此任务不会等待活跃任务（正在执行的任务）执行完毕之后再关闭（通过awaitTermination方法可以等待任务完成之后再关闭）。

注意：此方法并不能保证一定会停止每个任务，因为我们是通过Thread.interrupt来中断线程，如果中断失败，就可能无法终止线程池。

shutdcown 和 shutdownNow的区别：

shutdown 会把当前池状态改为SHUTDOWN，表示还会继续运行池内已经提交的任务，然后中断所有的空闲工作线程 ；但 shutdownNow 直接把池状态改为STOP，也就是说不会再运行已存在的任务，然后会中断所有工作线程。


## awaitTermination(long timeout, TimeUnit unit)

```java
public boolean awaitTermination(long timeout, TimeUnit unit)
    throws InterruptedException {
    long nanos = unit.toNanos(timeout);
    final ReentrantLock mainLock = this.mainLock;
    mainLock.lock();
    try {
        for (;;) {//自旋等待任务完成或超时
            if (runStateAtLeast(ctl.get(), TERMINATED))
                return true;
            if (nanos <= 0)
                return false;
            nanos = termination.awaitNanos(nanos);//等待给定的超时时间
        }
    } finally {
        mainLock.unlock();
    }
}
```

**说明**：

awaitTermination
一般是用来配合

shutdown
来使用。在对线程池发送一个

shutdown
请求后开始阻塞，直到所有任务都完成执行/超时/线程被中断才返回。如果在等待时间内线程池终止（

TERMINATED
）就返回

true
，如果等待超时后线程池还未终止就返回

false
。

到此，ThreadPoolExecutor 中几个比较重要的方法就讲完了，如果同学们对此源码解析有任何疑问，欢迎大家在评论中提出。

# 小结

说明：awaitTermination一般是用来配合shutdown来使用。在对线程池发送一个shutdown请求后开始阻塞，直到所有任务都完成执行/超时/线程被中断才返回。如果在等待时间内线程池终止（TERMINATED）就返回true，如果等待超时后线程池还未终止就返回false。

到此，ThreadPoolExecutor 中几个比较重要的方法就讲完了，如果同学们对此源码解析有任何疑问，欢迎大家在评论中提出。

[目录](readme.md)