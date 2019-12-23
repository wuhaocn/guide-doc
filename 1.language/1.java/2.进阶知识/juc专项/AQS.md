# JUC源码分析—AQS
[目录](readme.md)
[参考](https://www.jianshu.com/p/a8d27ba5db49)


## 1. 概述

AbstractQueuedSynchronizer，简称AQS。是一个用于构建锁和同步器的框架，许多同步器都可以通过AQS很容易并且高效地构造出来，
如常用的ReentrantLock、Semaphore、CountDownLatch等。基于AQS来构建同步器能带来许多好处。
它不仅能极大地减少实现工作，而且也不必处理在多个位置上发生的竞争问题。
在基于AQS构建的同步器中，只可能在一个时刻发生阻塞，从而降低上下文切换的开销，并提高吞吐量。
Doug Lea 大神在设计AQS时也充分考虑了可伸缩性，因此java.util.concurrent中所有基于AQS构建的同步器都能获得这个优势。
大多数开发者都不会直接使用AQS，JUC中标准同步器类都能够满足绝大多数情况的需求。但如果能了解标准同步器类的实现方式，
那么对理解它们的工作原理是非常有帮助的。

AQS支持**独占锁（Exclusive）和共享锁（Share）**两种模式：

* 独占锁：只能被一个线程获取到(ReentrantLock)；
* 共享锁：可以被多个线程同时获取(CountDownLatch、ReadWriteLock的读锁)。

不管是独占锁还是共享锁，本质上都是对AQS内部的一个变量state的获取，state是一个原子性的int变量，可用来表示锁状态、资源数等，如下图。

![](https://upload-images.jianshu.io/upload_images/6050820-72616d7003d7e162.png?imageMogr2/auto-orient/strip|imageView2/2/w/698/format/webp)

state获取

## 2. 数据结构和核心参数

![](https://upload-images.jianshu.io/upload_images/6050820-ba86152ff8eca037.png?imageMogr2/auto-orient/strip|imageView2/2/w/698/format/webp)

队列结构

AQS的内部实现了两个队列，**一个同步队列和一个条件队列**。

* **条件队列** 是为Lock实现的一个基础同步器，并且一个线程可能会有多个条件队列，只有在使用了Condition才会存在条件队列。
* **同步队列** 是在线程获取资源失败后，进入同步队列队尾保持自旋等待状态， 在同步队列中的线程在自旋时会判断其前节点是否为head节点，
             如果为head节点则不断尝试获取资源/锁，获取成功则退出同步队列。当线程执行完逻辑后，会释放资源/锁，释放后唤醒其后继节点。

不管是同步队列还是条件队列，其内部都是由节点Node组成，首先介绍下AQS的内部类Node，源码如下：

```java
static final class Node {
    /**
     * Marker to indicate a node is waiting in shared mode
     */
    static final Node SHARED = new Node();
    /**
     * Marker to indicate a node is waiting in exclusive mode
     */
    static final Node EXCLUSIVE = null;
    //取消
    static final int CANCELLED = 1;
    //等待触发
    static final int SIGNAL = -1;
    //等待条件
    static final int CONDITION = -2;
    //状态需要向后传播
    static final int PROPAGATE = -3;

    volatile int waitStatus;
    volatile Node prev;
    volatile Node next;
    volatile Thread thread;
    Node nextWaiter;
}
```

说明：Node的实现很简单，就是一个普通双向链表的实现，这里主要说明一下内部的几个等待状态：

* CANCELLED
：值为1，当前节点由于超时或中断被取消。
* SIGNAL
：值为-1，表示当前节点的前节点被阻塞，当前节点在release或cancel时需要执行unpark来唤醒后继节点。
* CONDITION
：值为-2，当前节点正在等待Condition，这个状态在同步队列里不会被用到。
* PROPAGATE
：值为-3，(针对共享锁)releaseShared()操作需要被传递到其他节点，这个状态在doReleaseShared中被设置，用来保证后续节点可以获取共享资源。
* **0**：初始状态，当前节点在sync queue中，等待获取锁。

AQS已经为我们提供了同步器的基础操作，如果要自定义同步器，必须实现以下几个方法：

* tryAcquire(int)
：独占方式。尝试获取资源，成功则返回true，失败则返回false。
* tryRelease(int)
：独占方式。尝试释放资源，成功则返回true，失败则返回false。
* tryAcquireShared(int)
：共享方式。尝试获取资源。负数表示失败；0表示成功，但没有剩余可用资源；正数表示成功，且有剩余资源。
* tryReleaseShared(int)
：共享方式。尝试释放资源，成功则返回true，失败则返回false。
* isHeldExclusively()
：该线程是否正在独占资源。只有用到Condition才需要去实现它。

## 3. 源码解析

### 3.1 acquire(int)

```java
//独占模式获取资源
public final void acquire(int arg) {
    if (!tryAcquire(arg) &&
        acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
        selfInterrupt();
}
```

**说明：**独占模式下获取资源/锁，忽略中断的影响。
内部主要调用了三个方法，其中tryAcquire需要自定义实现。后面会对各个方法进行详细分析。acquire方法流程如下：

* 1. tryAcquire()尝试直接获取资源，如果成功则直接返回，失败进入第二步；
* 2. addWaiter()获取资源失败后，将当前线程加入等待队列的尾部，并标记为独占模式；
* 3. acquireQueued()使线程在等待队列中自旋等待获取资源，一直获取到资源后才返回。如果在等待过程中被中断过，则返回true，否则返回false。
* 4. 如果线程在等待过程中被中断(interrupt)是不响应的，在获取资源成功之后根据返回的中断状态调用selfInterrupt()方法再把中断状态补上。

### 3.1.1 tryAcquire(int)

```java
protected boolean tryAcquire(int arg) {
    throw new UnsupportedOperationException();
}
```

**说明：**尝试获取资源，成功返回true。具体资源获取/释放方式交由自定义同步器实现。ReentrantLock中公平锁和非公平锁的实现如下:

```java
//公平锁
protected final boolean tryAcquire(int acquires) {
    final Thread current = Thread.currentThread();
    int c = getState();
    if (c == 0) {
        if (!hasQueuedPredecessors() &&
                compareAndSetState(0, acquires)) {
            setExclusiveOwnerThread(current);
            return true;
        }
    }
    else if (current == getExclusiveOwnerThread()) {
        int nextc = c + acquires;
        if (nextc < 0)
            throw new Error("Maximum lock count exceeded");
        setState(nextc);
        return true;
    }
    return false;
}
//非公平锁
final boolean nonfairTryAcquire(int acquires) {
    final Thread current = Thread.currentThread();
    int c = getState();
    if (c == 0) {
        if (compareAndSetState(0, acquires)) {
            setExclusiveOwnerThread(current);
            return true;
        }
    }
    else if (current == getExclusiveOwnerThread()) {
        int nextc = c + acquires;
        if (nextc < 0) // overflow
            throw new Error("Maximum lock count exceeded");
        setState(nextc);
        return true;
    }
    return false;
}

```

### 3.1.2 addWaiter(Node)

```java
//添加等待节点到尾部
private Node addWaiter(Node mode) {
    Node node = new Node(Thread.currentThread(), mode);
    // Try the fast path of enq; backup to full enq on failure
    //尝试快速入队
    Node pred = tail;
    if (pred != null) {
        node.prev = pred;
        if (compareAndSetTail(pred, node)) {
            pred.next = node;
            return node;
        }
    }
    enq(node);
    return node;
}
//插入给定节点到队尾
private Node enq(final Node node) {
    for (;;) {
        Node t = tail;
        if (t == null) { // Must initialize
            if (compareAndSetHead(new Node()))
                tail = head;
        } else {
            node.prev = t;
            if (compareAndSetTail(t, node)) {
                t.next = node;
                return t;
            }
        }
    }
}
```

**说明：**获取独占锁失败后，将当前线程加入等待队列的尾部，并标记为独占模式。返回插入的等待节点。

### 3.1.3 acquireQueued(Node,int)

```java
//自旋等待获取资源
final boolean acquireQueued(final Node node, int arg) {
    boolean failed = true;
    try {
        boolean interrupted = false;
        for (;;) {
            final Node p = node.predecessor();//获取前继节点
            //前继节点为head，说明可以尝试获取资源
            if (p == head && tryAcquire(arg)) {
                setHead(node);//获取成功，更新head节点
                p.next = null; // help GC
                failed = false;
                return interrupted;
            }
            if (shouldParkAfterFailedAcquire(p, node) && //检查是否可以park
                parkAndCheckInterrupt())
                interrupted = true;
        }
    } finally {
        if (failed)
            cancelAcquire(node);
    }
}

//获取资源失败后，检查并更新等待状态
private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
    int ws = pred.waitStatus;
    if (ws == Node.SIGNAL)
        /*
         * This node has already set status asking a release
         * to signal it, so it can safely park.
         */
        return true;
    if (ws > 0) {
        /*
         * Predecessor was cancelled. Skip over predecessors and
         * indicate retry.
         */
        //如果前节点取消了，那就一直往前找到一个等待状态的节点，并排在它的后边
        do {
            node.prev = pred = pred.prev;
        } while (pred.waitStatus > 0);
        pred.next = node;
    } else {
        /*
         * waitStatus must be 0 or PROPAGATE.  Indicate that we
         * need a signal, but don't park yet.  Caller will need to
         * retry to make sure it cannot acquire before parking.
         */
        //此时前节点状态为0或PROPAGATE，表示我们需要一个唤醒信号，但是不立即park,在park前调用者需要重试来确认它不能获取资源。
        compareAndSetWaitStatus(pred, ws, Node.SIGNAL);
    }
    return false;
}

```

**说明：**线程进入等待队列后，在等待队列中自旋等待获取资源。如果在整个等待过程中被中断过，则返回true，否则返回false。具体流程如下：

1. 获取当前等待节点的前继节点，如果前继节点为head，说明可以尝试获取锁；
2. 调用tryAcquire获取锁，成功后更新head为当前节点；
3. 获取资源失败，调用shouldParkAfterFailedAcquire方法检查并更新等待状态。如果前继节点状态为

SIGNAL，说明当前节点可以进入waiting状态等待唤醒；被唤醒后，继续自旋重复上述步骤。
1. 获取资源成功后返回中断状态。当前线程通过parkAndCheckInterrupt()阻塞之后进入waiting状态，此状态下可以通过下面两种途径唤醒线程：
2. 前继节点释放资源后，通过unparkSuccessor()方法unpark当前线程；
3. 当前线程被中断。

### 3.2 release(int)

```java
/**独占模式释放资源*/
public final boolean release(int arg) {
    if (tryRelease(arg)) {//尝试释放资源
        Node h = head;//头结点
        if (h != null && h.waitStatus != 0)
            unparkSuccessor(h);//唤醒head的下一个节点
        return true;
    }
    return false;
}
```

**说明：**独占模式下释放指定量的资源，成功释放后调用unparkSuccessor唤醒head的下一个节点。

### 3.2.1 tryRelease(int)

```java
protected boolean tryRelease(int arg) {
    throw new UnsupportedOperationException();
}

```

**说明** 和tryAcquire()一样，这个方法也需要自定义同步器去实现。一般来说，释放资源直接拿state减去给定的参数arg，释放后state==0说明释放成功。在

ReentrantLock中实现如下：

```java
protected final boolean tryRelease(int releases) {
    int c = getState() - releases;
    if (Thread.currentThread() != getExclusiveOwnerThread())
        throw new IllegalMonitorStateException();
    boolean free = false;
    if (c == 0) {
        free = true;
        setExclusiveOwnerThread(null);//设置独占锁持有线程为null
    }
    setState(c);
    return free;
}
```

### 3.2.2 unparkSuccessor(Node)

```java
private void unparkSuccessor(Node node) {
    int ws = node.waitStatus;
    if (ws < 0)//当前节点没有被取消,更新waitStatus为0。
        compareAndSetWaitStatus(node, ws, 0);

    Node s = node.next;//找到下一个需要唤醒的结点
    if (s == null || s.waitStatus > 0) {
        s = null;
        //next节点为空，从tail节点开始向前查找有效节点
        for (Node t = tail; t != null && t != node; t = t.prev)
            if (t.waitStatus <= 0)
                s = t;
    }
    if (s != null)
        LockSupport.unpark(s.thread);
}
```

**说明：**成功获取到资源后，调用此方法唤醒head的下一个节点。因为当前节点已经释放掉资源，下一个等待的线程可以被唤醒继续获取资源。

## 3.3 acquireShared(int)

```java
public final void acquireShared(int arg) {
    if (tryAcquireShared(arg) < 0)
        doAcquireShared(arg);
}
```

**说明：**共享模式下获取资源/锁，忽略中断的影响。内部主要调用了两个个方法，其中

tryAcquireShared需要自定义同步器实现。后面会对各个方法进行详细分析。

acquireShared方法流程如下：

1. tryAcquireShared(arg)
尝试获取共享资源。**成功获取并且还有可用资源返回正数；成功获取但是没有可用资源时返回0；获取资源失败返回一个负数。**
2. 获取资源失败后调用doAcquireShared方法进入等待队列，获取资源后返回。

### 3.3.1 tryAcquireShared(int arg)

```java
/**共享模式下获取资源*/
protected int tryAcquireShared(int arg) {
    throw new UnsupportedOperationException();
}
```

**说明：**尝试获取共享资源，需同步器自定义实现。有三个类型的返回值：

* 正数：成功获取资源，并且还有剩余可用资源，可以唤醒下一个等待线程；
* 负数：获取资源失败，准备进入等待队列；
* 0：获取资源成功，但没有剩余可用资源。

### 3.3.2 doAcquireShared(int)

```java
//获取共享锁
private void doAcquireShared(int arg) {
    final Node node = addWaiter(Node.SHARED);//添加一个共享模式Node到队列尾
    boolean failed = true;
    try {
        boolean interrupted = false;
        for (;;) {
            final Node p = node.predecessor();//获取前节点
            if (p == head) {
                int r = tryAcquireShared(arg);//前节点为head，尝试获取资源
                if (r >= 0) {
                    //获取资源成功，设置head为自己，如果有剩余资源可以在唤醒之后的线程
                    setHeadAndPropagate(node, r);
                    p.next = null; // help GC
                    if (interrupted)
                        selfInterrupt();
                    failed = false;
                    return;
                }
            }
            if (shouldParkAfterFailedAcquire(p, node) &&  //检查获取失败后是否可以阻塞
                parkAndCheckInterrupt())
                interrupted = true;
        }
    } finally {
        if (failed)
            cancelAcquire(node);
    }
}
```

**说明：**在tryAcquireShared中获取资源失败后，将当前线程加入等待队列尾部等待唤醒，成功获取资源后返回。在阻塞结束后成功获取到资源时，如果还有剩余资源，就调用setHeadAndPropagate方法继续唤醒之后的线程，源码如下：

```java

//设置head，如果有剩余资源可以再唤醒之后的线程
private void setHeadAndPropagate(Node node, int propagate) {
    Node h = head; // Record old head for check below
    setHead(node);
    /*
     * 如果满足下列条件可以尝试唤醒下一个节点：
     *  调用者指定参数(propagate>0)，并且后继节点正在等待或后继节点为空
     */
    if (propagate > 0 || h == null || h.waitStatus < 0 ||
        (h = head) == null || h.waitStatus < 0) {
        Node s = node.next;
        if (s == null || s.isShared())
            doReleaseShared();
    }
}

```

## 3.4 releaseShared(int)

```java
//设置head，如果有剩余资源可以再唤醒之后的线程
private void setHeadAndPropagate(Node node, int propagate) {
    Node h = head; // Record old head for check below
    setHead(node);
    /*
     * 如果满足下列条件可以尝试唤醒下一个节点：
     *  调用者指定参数(propagate>0)，并且后继节点正在等待或后继节点为空
     */
    if (propagate > 0 || h == null || h.waitStatus < 0 ||
        (h = head) == null || h.waitStatus < 0) {
        Node s = node.next;
        if (s == null || s.isShared())
            doReleaseShared();
    }
}
```

说明：共享模式下释放给定量的资源，如果成功释放，唤醒等待队列的后继节点。tryReleaseShared需要自定义同步器去实现。
方法执行流程：tryReleaseShared(int)尝试释放给定量的资源，成功释放后调用doReleaseShared()唤醒后继线程。

### 3.4.1 tryReleaseShared(int)

```java
/**共享模式释放资源*/
protected boolean tryReleaseShared(int arg) {
    throw new UnsupportedOperationException();
}
```

**说明：**释放给定量的资源，需自定义同步器实现。释放后如果允许后继等待线程获取资源返回true。

### 3.4.2 doReleaseShared(int)

```java

//释放共享资源-唤醒后继线程并保证后继节点的资源传播
private void doReleaseShared() {
    //自旋，确保释放后唤醒后继节点
    for (;;) {
        Node h = head;
        if (h != null && h != tail) {
            int ws = h.waitStatus;
            if (ws == Node.SIGNAL) {
                if (!compareAndSetWaitStatus(h, Node.SIGNAL, 0))
                    continue;            // loop to recheck cases
                unparkSuccessor(h);//唤醒后继节点
            }
            else if (ws == 0 &&
                     !compareAndSetWaitStatus(h, 0, Node.PROPAGATE))  //waitStatus为0，CAS修改为PROPAGATE
                continue;                // loop on failed CAS
        }
        if (h == head)                   // loop if head changed
            break;
    }
}
```

说明：在tryReleaseShared成功释放资源后，调用此方法唤醒后继线程并保证后继节点的release传播（通过设置head节点的waitStatus为PROPAGATE）。


## 小结


小结
自此，AQS的主要方法就讲完了，有几个没有讲到的方法如tryAcquireNanos、tryAcquireSharedNanos，都是带等待时间的资源获取方法，还有acquireInterruptibly acquireSharedInterruptibly,响应中断式资源获取方法。都比较简单，同学们可以参考本篇源码阅读。


[目录](readme.md)
