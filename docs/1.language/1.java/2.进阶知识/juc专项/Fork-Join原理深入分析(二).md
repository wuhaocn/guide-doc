# [Fork-Join 原理深入分析（二）](https://www.cnblogs.com/jinggod/p/8490573.html)

本文是将 Fork-Join 复杂且较为庞大的框架分成 5 个小点来分析 Fork-Join 框架的实现原理，一个个点地理解透 Fork-Join 的核心原理。

[目录](readme.md)

### **1. Frok-Join 框架的核心类的结构分析**

Fork-Join 框架有三个核心类：ForkJoinPool，ForkJoinWorkerThread，ForkJoinTask。下面将分析这三个类的数据结构，初步了解三个类的核心成员。

**ForkJoinPool**

```java
//继承了 AbstractExecutorService 类
public class ForkJoinPool extends AbstractExecutorService{

    //任务队列数组，存储了所有任务队列，包括 内部队列 和 外部队列
    volatile WorkQueue[] workQueues;     // main registry

    //一个静态常量，ForkJoinPool 提供的内部公用的线程池
    static final ForkJoinPool common;

    //默认的线程工厂类
  public static final ForkJoinWorkerThreadFactory defaultForkJoinWorkerThreadFactory;

}
```

**ForkJoinWorkerThread**

```java
//继承了 Thread 类
public class ForkJoinWorkerThread extends Thread {

  //线程工作的线程池，即此线程所属的线程池
  final ForkJoinPool pool;

  // 线程的内部队列
  final ForkJoinPool.WorkQueue workQueue;

//.....
}
```

### **2. ForkJoinPool 中线程的创建**

**2.1 默认的线程工厂类**

ForkJoinPool 中的线程是由默认的线程工厂类 defaultForkJoinWorkerThreadFactory 创建的

```java
//默认的工厂类
public static final ForkJoinWorkerThreadFactory defaultForkJoinWorkerThreadFactory;

defaultForkJoinWorkerThreadFactory = new DefaultForkJoinWorkerThreadFactory();
```

defaultForkJoinWorkerThreadFactory 创建线程的方法 newThread()，其实就是传入当前的线程池，直接创建。

```java
/**
 * Default ForkJoinWorkerThreadFactory implementation; creates a
 * new ForkJoinWorkerThread.
 */
static final class DefaultForkJoinWorkerThreadFactory
    implements ForkJoinWorkerThreadFactory {
    public final ForkJoinWorkerThread newThread(ForkJoinPool pool) {
        return new ForkJoinWorkerThread(pool);
    }
}
```

**2.2 ForkJoinWorkerThread 的构造方法**

```java
protected ForkJoinWorkerThread(ForkJoinPool pool) {
    // Use a placeholder until a useful name can be set in registerWorker
    super("aForkJoinWorkerThread");
    //线程工作的线程池，即创建这个线程的线程池
    this.pool = pool;
    //注册线程到线程池中，并返回此线程的内部任务队列
    this.workQueue = pool.registerWorker(this);
}
```

创建一个工作线程，最后一步还要注册到其所属的线程池中，看下面源码，注册的过程可以分为两步：

1. 创建一个新的任务队列
2. 为此任务队列分配一个线程池的索引，将任务队列存储在线程数组 workQueues 的此索引位置，并返回这个任务队列，作为线程的内部任务队列。线程注册成功。

```java
final WorkQueue registerWorker(ForkJoinWorkerThread wt) {
    UncaughtExceptionHandler handler;
    wt.setDaemon(true);               // configure thread
    if ((handler = ueh) != null)
        wt.setUncaughtExceptionHandler(handler);
    //创建一个任务队列
    WorkQueue w = new WorkQueue(this, wt);
    int i = 0;                             //分配一个线程池的索引
    int mode = config & MODE_MASK;
    int rs = lockRunState();
    try {
        WorkQueue[] ws; int n;                    // skip if no array
        if ((ws = workQueues) != null && (n = ws.length) > 0) {
            int s = indexSeed += SEED_INCREMENT; // unlikely to collide
            int m = n - 1;
            //计算 索引
            i = ((s << 1) | 1) & m;               // odd-numbered indices

           if (ws[i] != null) {                   //如果索引冲突
                int probes = 0;                   // step by approx half n
                int step = (n <= 4) ? 2 : ((n >>> 1) & EVENMASK) + 2;
                while (ws[i = (i + step) & m] != null) {
                    if (++probes >= n) {
                        //扩容：以原来的数组的长度的两倍来创建一个新的数组，再复制旧数组的内衣
                        workQueues = ws = Arrays.copyOf(ws, n <<= 1);
                        m = n - 1;
                        probes = 0;
                    }
                }
            }
            w.hint = s;                           // use as random seed
            w.config = i | mode;
            w.scanState = i;                      // publication fence
            //刚创建的任务队列加入到线程池的 任务队列数组中
            ws[i] = w;
        }
    } finally {
        unlockRunState(rs, rs & ~RSLOCK);
    }
    wt.setName(workerNamePrefix.concat(Integer.toString(i >>> 1)));
    return w;
}
```

对应注册线程，ForkJoinPool 也提供了一个取消线程注册的方法 deregisterWorker()，在线程被销毁的时候调用，此处就不说了。

### **3. ForkJoinTask 的 fork()、join()方法**

在上一篇文章中，我们在实现 分治编程时，主要就是调用

ForkJoinTask 的 fork()和 join()方法。

fork()方法用于提交子任务，而

join()方法则用于等待子任务的完成。而这个过程中，将涉及到 “工作窃取算法”。

**3.1 fork( ) 方法提交任务**

先来看一下 fork()方法的源码

```
public final ForkJoinTask<V> fork() {
    Thread t;
    //判断是否是一个 工作线程
    if ((t = Thread.currentThread()) instanceof ForkJoinWorkerThread)
        //加入到内部队列中
        ((ForkJoinWorkerThread)t).workQueue.push(this);
    else//由 common 线程池来执行任务
        ForkJoinPool.common.externalPush(this);
    return this;
}
```

源码中，fork()方法先判断当前线程（调用 fork()来提交任务的线程）是不是一个 ForkJoinWorkerThread 的工作线程，如果是，则将任务加入到内部队列中，否则，由 ForkJoinPool 提供的内部公用的线程池

common 线程池来执行这个任务。

```java
//ForkJoinPool 提供的内部公用的线程池
static final ForkJoinPool common;
```

顺便说一下，根据上面的说法，意味着我们可以在普通线程池中直接调用 fork()方法来提交任务到一个默认提供的线程池中。这将非常方便。假如，你要在程序中处理大任务，需要分治编程，但你仅仅只处理一次，以后就不会用到，而且任务不算太大，不需要设置特定的参数，那么你肯定不想为此创建一个线程池，这时默认的提供的线程池将会很有用。

下面是我基于上一篇文章例子改造的，CountTask 类在我上一篇文章中找到

```java
public class Test_34 {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        // 创建一个计算任务，计算 由1加到12
        CountTask countTask2 = new CountTask(1, 12);
        //直接在main线程中调用 fork 来提交任务,
        countTask2.fork();
        //没有创建线程池，使用commonPool线程池
        System.out.println(countTask2.get());
    }
}
```

**运行结果：**

```
任务过大，切割的任务： 1加到 12的和 执行此任务的线程：ForkJoinPool.commonPool-worker-1
任务过大，切割的任务： 1加到 6的和 执行此任务的线程：ForkJoinPool.commonPool-worker-2
任务过大，切割的任务： 7加到 12的和 执行此任务的线程：ForkJoinPool.commonPool-worker-3
执行计算任务，计算 1到 3的和 ，结果是：6 执行此任务的线程：ForkJoinPool.commonPool-worker-2
执行计算任务，计算 4到 6的和 ，结果是：15 执行此任务的线程：ForkJoinPool.commonPool-worker-1
执行计算任务，计算 7到 9的和 ，结果是：24 执行此任务的线程：ForkJoinPool.commonPool-worker-3
执行计算任务，计算 10到 12的和 ，结果是：33 执行此任务的线程：ForkJoinPool.commonPool-worker-1
78
```

**注意执行任务的线程名称：commonPool 表示执行任务的线程是公用的 ForkJoinPooL 线程池中的线程，上面的例子中，并没有创建一个新的 ForKJoinPool 线程池**

**3.2 join( ) 等待任务的完成**

```java
public final V join() {
    int s;
    if ((s = doJoin() & DONE_MASK) != NORMAL)
        reportException(s);
    return getRawResult();//直接返回结果
}
```

重点在 dojoin()方法，下面追踪下去

```java
private int doJoin() {
    int s; Thread t; ForkJoinWorkerThread wt; ForkJoinPool.WorkQueue w;
    return
        //如果完成，直接返回s
        (s = status) < 0 ? s :
        //没有完成，判断是不是池中的 ForkJoinWorkerThread 工作线程
        ((t = Thread.currentThread()) instanceof ForkJoinWorkerThread) ?
        //如果是池中线程，执行这里
        (w = (wt = (ForkJoinWorkerThread)t).workQueue).
        tryUnpush(this) && (s = doExec()) < 0 ? s :
        wt.pool.awaitJoin(w, this, 0L) :
        //如果不是池中的线程池，则执行这里
        externalAwaitDone();
}
```

仔细看上面的注释。当 dojoin( )方法发现任务没有完成且当前线程是池中线程时，执行了 tryUnpush( )方法。
tryUnpush()方法尝试去执行此任务：如果要 join 的任务正好在当前任务队列的顶端，那么 pop 出这个任务，然后调用 doExec() 让当前线程去执行这个任务。

```java
final boolean tryUnpush(ForkJoinTask<?> t) {
        ForkJoinTask<?>[] a; int s;
        if ((a = array) != null && (s = top) != base &&
            U.compareAndSwapObject
            (a, (((a.length - 1) & --s) << ASHIFT) + ABASE, t, null)) {
            U.putOrderedInt(this, QTOP, s);
            return true;
        }
        return false;
    }
```

```
final int doExec() {
    int s; boolean completed;
    if ((s = status) >= 0) {
        try {
            completed = exec();
        } catch (Throwable rex) {
            return setExceptionalCompletion(rex);
        }
        if (completed)
            s = setCompletion(NORMAL);
    }
    return s;
}
```

如果任务不是处于队列的顶端，那么就会执行 awaitJoin( )方法。

```java
/**
 * Helps and/or blocks until the given task is done or timeout.
 *
 * @param w caller
 * @param task the task
 * @param deadline for timed waits, if nonzero
 * @return task status on exit
 */
final int awaitJoin(WorkQueue w, ForkJoinTask<?> task, long deadline) {
    int s = 0;
    if (task != null && w != null) {
        ForkJoinTask<?> prevJoin = w.currentJoin;
        U.putOrderedObject(w, QCURRENTJOIN, task);
        CountedCompleter<?> cc = (task instanceof CountedCompleter) ?
            (CountedCompleter<?>)task : null;
        for (;;) {
            if ((s = task.status) < 0)//如果任务完成了，跳出死循环
                break;
            if (cc != null)//当前任务是CountedCompleter类型，则尝试从任务队列中获取当前任务的派生子任务来执行；
                helpComplete(w, cc, 0);
            else if (w.base == w.top || w.tryRemoveAndExec(task))//如果当前线程的内部队列为空，或者成功完成了任务，帮助某个线程完成任务。
                helpStealer(w, task);
            if ((s = task.status) < 0)//任务完成，跳出死循环
                break;
            long ms, ns;
            if (deadline == 0L)
                ms = 0L;
            else if ((ns = deadline - System.nanoTime()) <= 0L)
                break;
            else if ((ms = TimeUnit.NANOSECONDS.toMillis(ns)) <= 0L)
                ms = 1L;
            if (tryCompensate(w)) {
                task.internalWait(ms);
                U.getAndAddLong(this, CTL, AC_UNIT);
            }
        }
        U.putOrderedObject(w, QCURRENTJOIN, prevJoin);
    }
    return s;
}
```

重点说一下 helpStealer。helpStealer 的原则是你帮助我执行任务,我也帮你执行任务。

- 遍历奇数下标,如果发现队列对象 currentSteal 放置的刚好是自己要找的任务,则说明自己的任务被该队列 A 的 owner 线程偷来执行
- 如果队列 A 队列中有任务，则从队尾(base)取出执行；
- 如果发现队列 A 队列为空，则根据它正在 join 的任务,在拓扑找到相关的队列 B 去偷取任务执行。
  在执行的过程中要注意，我们应该完整的把任务完成

fork/join 框架的核心来自于它的轻量级调度机制。FJTask 借鉴在 Cilk 中采用的工作窃取调度策略：

- （1）每个 worker 线程利用它自己的调度队列维护可执行任务。
- （2）队列是双端的，支持 LIFO（last-in-first-out）的 push 和 pop 操作，通知也支持 FIFO（first-in-first-out）的 take 操作。
- （3）一个任务 fork 的子任务，只会 push 到它所在线程的队列。
- （4）工作线程使用 LIFO 通过 pop 处理它自己队列中的线程。
- （5）当线程自己本地队列中没有待处理任务时，它尝试去随机读取（窃取）一个 worker 线程的工作队列任务（使用 FIFO）。
- （6）当线程进入 join 操作，它开始处理其它线程的任务（自己的已经处理完了），直到目标任务完成（通过 isDone 方法）。因此，所有任务都无阻塞的完成了。
- （7）当一个工作线程没有任务了，并且尝试从其它线程处窃取也失败了，它让出资源（通过使用 yields, sleeps 或者其它优先级调整）并且随后会再次激活，直到所有工作线程都空闲了

* [jdk1.8-ForkJoin 框架剖析](https://www.jianshu.com/p/f777abb7b251)
* [Jdk1.7 JUC 源码增量解析(3)-ForkJoin-非 ForkJoin 任务的执行过程](http://brokendreams.iteye.com/blog/2258068)

最后，有兴趣的还可以看一下 Doug Lea 的写的 Fork-Join 框架的文章
原文：[A Java Fork/Join Framework](http://gee.cs.oswego.edu/dl/papers/fj.pdf)
中文译文：[Fork/Join 框架-设计与实现](https://www.cnblogs.com/suxuan/p/4970498.html)

参考文献：

- PunyGod https://www.jianshu.com/p/f777abb7b251

分类: [Java 并发知识整理](https://www.cnblogs.com/jinggod/category/1168984.html)
