# 1.Condition 简介

任何一个 java 对象都天然继承于 Object 类，在线程间实现通信的往往会应用到 Object 的几个方法，比如 wait(),wait(long timeout),wait(long timeout, int nanos)与 notify(),notifyAll()几个方法实现等待/通知机制，同样的， 在 java Lock 体系下依然会有同样的方法实现等待/通知机制。从整体上来看**Object 的 wait 和 notify/notify 是与对象监视器配合完成线程间的等待/通知机制，而 Condition 与 Lock 配合完成等待通知机制，前者是 java 底层级别的，后者是语言级别的，具有更高的可控制性和扩展性**。两者除了在使用方式上不同外，在**功能特性**上还是有很多的不同：

1. Condition 能够支持不响应中断，而通过使用 Object 方式不支持；
2. Condition 能够支持多个等待队列（new 多个 Condition 对象），而 Object 方式只能支持一个；
3. Condition 能够支持超时时间的设置，而 Object 不支持

参照 Object 的 wait 和 notify/notifyAll 方法，Condition 也提供了同样的方法：

> **针对 Object 的 wait 方法**

1. void await() throws InterruptedException:当前线程进入等待状态，如果其他线程调用 condition 的 signal 或者 signalAll 方法并且当前线程获取 Lock 从 await 方法返回，如果在等待状态中被中断会抛出被中断异常；
2. long awaitNanos(long nanosTimeout)：当前线程进入等待状态直到被通知，中断或者**超时**；
3. boolean await(long time, TimeUnit unit)throws InterruptedException：同第二种，支持自定义时间单位
4. boolean awaitUntil(Date deadline) throws InterruptedException：当前线程进入等待状态直到被通知，中断或者**到了某个时间**

> **针对 Object 的 notify/notifyAll 方法**

1. void signal()：唤醒一个等待在 condition 上的线程，将该线程从**等待队列**中转移到**同步队列**中，如果在同步队列中能够竞争到 Lock 则可以从等待方法中返回。
2. void signalAll()：与 1 的区别在于能够唤醒所有等待在 condition 上的线程

# 2.Condition 实现原理分析

## 2.1 等待队列

要想能够深入的掌握 condition 还是应该知道它的实现原理，现在我们一起来看看 condiiton 的源码。创建一个 condition 对象是通过`lock.newCondition()`,而这个方法实际上是会 new 出一个**ConditionObject**对象，该类是 AQS（[AQS 的实现原理的文章](https://juejin.im/post/5aeb07ab6fb9a07ac36350c8)）的一个内部类，有兴趣可以去看看。前面我们说过，condition 是要和 lock 配合使用的也就是 condition 和 Lock 是绑定在一起的，而 lock 的实现原理又依赖于 AQS，自然而然 ConditionObject 作为 AQS 的一个内部类无可厚非。我们知道在锁机制的实现上，AQS 内部维护了一个同步队列，如果是独占式锁的话，所有获取锁失败的线程的尾插入到**同步队列**，同样的，condition 内部也是使用同样的方式，内部维护了一个 **等待队列**，所有调用 condition.await 方法的线程会加入到等待队列中，并且线程状态转换为等待状态。另外注意到 ConditionObject 中有两个成员变量：

    /** First node of condition queue. */
    private transient Node firstWaiter;
    /** Last node of condition queue. */
    private transient Node lastWaiter;

这样我们就可以看出来 ConditionObject 通过持有等待队列的头尾指针来管理等待队列。主要注意的是 Node 类复用了在 AQS 中的 Node 类，其节点状态和相关属性可以去看[AQS 的实现原理的文章](https://juejin.im/post/5aeb07ab6fb9a07ac36350c8)，如果您仔细看完这篇文章对 condition 的理解易如反掌，对 lock 体系的实现也会有一个质的提升。Node 类有这样一个属性：

    //后继节点
    Node nextWaiter;

进一步说明，**等待队列是一个单向队列**，而在之前说 AQS 时知道同步队列是一个双向队列。接下来我们用一个 demo，通过 debug 进去看是不是符合我们的猜想：

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            Thread thread = new Thread(() -> {
                lock.lock();
                try {
                    condition.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }finally {
                    lock.unlock();
                }
            });
            thread.start();
        }
    }

这段代码没有任何实际意义，甚至很臭，只是想说明下我们刚才所想的。新建了 10 个线程，没有线程先获取锁，然后调用 condition.await 方法释放锁将当前线程加入到等待队列中，通过 debug 控制当走到第 10 个线程的时候查看`firstWaiter`即等待队列中的头结点，debug 模式下情景图如下：

![debug模式下情景图](http://upload-images.jianshu.io/upload_images/2615789-67a211209835e36d.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

从这个图我们可以很清楚的看到这样几点：1. 调用 condition.await 方法后线程依次尾插入到等待队列中，如图队列中的线程引用依次为 Thread-0,Thread-1,Thread-2....Thread-8；2. 等待队列是一个单向队列。通过我们的猜想然后进行实验验证，我们可以得出等待队列的示意图如下图所示：

![等待队列的示意图](http://upload-images.jianshu.io/upload_images/2615789-5aa1ee1ae8cb7f5a.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

同时还有一点需要注意的是：我们可以多次调用 lock.newCondition()方法创建多个 condition 对象，也就是一个 lock 可以持有多个等待队列。而在之前利用 Object 的方式实际上是指在**对象 Object 对象监视器上只能拥有一个同步队列和一个等待队列，而并发包中的 Lock 拥有一个同步队列和多个等待队列**。示意图如下：

![AQS持有多个Condition.png](http://upload-images.jianshu.io/upload_images/2615789-6621181fc19603c2.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

如图所示，ConditionObject 是 AQS 的内部类，因此每个 ConditionObject 能够访问到 AQS 提供的方法，相当于每个 Condition 都拥有所属同步器的引用。

## 2.2 await 实现原理

**当调用 condition.await()方法后会使得当前获取 lock 的线程进入到等待队列，如果该线程能够从 await()方法返回的话一定是该线程获取了与 condition 相关联的 lock**。接下来，我们还是从源码的角度去看，只有熟悉了源码的逻辑我们的理解才是最深的。await()方法源码为：

    public final void await() throws InterruptedException {
        if (Thread.interrupted())
            throw new InterruptedException();
    	// 1. 将当前线程包装成Node，尾插入到等待队列中
        Node node = addConditionWaiter();
    	// 2. 释放当前线程所占用的lock，在释放的过程中会唤醒同步队列中的下一个节点
        int savedState = fullyRelease(node);
        int interruptMode = 0;
        while (!isOnSyncQueue(node)) {
    		// 3. 当前线程进入到等待状态
            LockSupport.park(this);
            if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
                break;
        }
    	// 4. 自旋等待获取到同步状态（即获取到lock）
        if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
            interruptMode = REINTERRUPT;
        if (node.nextWaiter != null) // clean up if cancelled
            unlinkCancelledWaiters();
    	// 5. 处理被中断的情况
        if (interruptMode != 0)
            reportInterruptAfterWait(interruptMode);
    }

代码的主要逻辑**请看注释**，我们都知道**当当前线程调用 condition.await()方法后，会使得当前线程释放 lock 然后加入到等待队列中，直至被 signal/signalAll 后会使得当前线程从等待队列中移至到同步队列中去，直到获得了 lock 后才会从 await 方法返回，或者在等待时被中断会做中断处理**。那么关于这个实现过程我们会有这样几个问题：1. 是怎样将当前线程添加到等待队列中去的？2.释放锁的过程？3.怎样才能从 await 方法退出？而这段代码的逻辑就是告诉我们这三个问题的答案。具体**请看注释**，在第 1 步中调用 addConditionWaiter 将当前线程添加到等待队列中，该方法源码为：

    private Node addConditionWaiter() {
        Node t = lastWaiter;
        // If lastWaiter is cancelled, clean out.
        if (t != null && t.waitStatus != Node.CONDITION) {
            unlinkCancelledWaiters();
            t = lastWaiter;
        }
    	//将当前线程包装成Node
        Node node = new Node(Thread.currentThread(), Node.CONDITION);
        if (t == null)
            firstWaiter = node;
        else
    		//尾插入
            t.nextWaiter = node;
    	//更新lastWaiter
        lastWaiter = node;
        return node;
    }

这段代码就很容易理解了，将当前节点包装成 Node，如果等待队列的 firstWaiter 为 null 的话（等待队列为空队列），则将 firstWaiter 指向当前的 Node,否则，更新 lastWaiter(尾节点)即可。就是**通过尾插入的方式将当前线程封装的 Node 插入到等待队列中即可**，同时可以看出等待队列是一个**不带头结点的链式队列**，之前我们学习 AQS 时知道同步队列**是一个带头结点的链式队列**，这是两者的一个区别。将当前节点插入到等待对列之后，会使当前线程释放 lock，由 fullyRelease 方法实现，fullyRelease 源码为：

    final int fullyRelease(Node node) {
        boolean failed = true;
        try {
            int savedState = getState();
            if (release(savedState)) {
    			//成功释放同步状态
                failed = false;
                return savedState;
            } else {
    			//不成功释放同步状态抛出异常
                throw new IllegalMonitorStateException();
            }
        } finally {
            if (failed)
                node.waitStatus = Node.CANCELLED;
        }
    }

这段代码就很容易理解了，**调用 AQS 的模板方法 release 方法释放 AQS 的同步状态并且唤醒在同步队列中头结点的后继节点引用的线程**，如果释放成功则正常返回，若失败的话就抛出异常。到目前为止，这两段代码已经解决了前面的两个问题的答案了，还剩下第三个问题，怎样从 await 方法退出？现在回过头再来看 await 方法有这样一段逻辑：

    while (!isOnSyncQueue(node)) {
    	// 3. 当前线程进入到等待状态
        LockSupport.park(this);
        if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
            break;
    }

很显然，当线程第一次调用 condition.await()方法时，会进入到这个 while()循环中，然后通过 LockSupport.park(this)方法使得当前线程进入等待状态，那么要想退出这个 await 方法第一个前提条件自然而然的是要先退出这个 while 循环，出口就只剩下两个地方：**1. 逻辑走到 break 退出 while 循环；2. while 循环中的逻辑判断为 false**。再看代码出现第 1 种情况的条件是当前等待的线程被中断后代码会走到 break 退出，第二种情况是当前节点被移动到了同步队列中（即另外线程调用的 condition 的 signal 或者 signalAll 方法），while 中逻辑判断为 false 后结束 while 循环。总结下，就是**当前线程被中断或者调用 condition.signal/condition.signalAll 方法当前节点移动到了同步队列后** ，这是当前线程退出 await 方法的前提条件。当退出 while 循环后就会调用`acquireQueued(node, savedState)`，这个方法在介绍 AQS 的底层实现时说过了，若感兴趣的话可以去[看这篇文章](https://juejin.im/post/5aeb07ab6fb9a07ac36350c8)，该方法的作用是在**自旋过程中线程不断尝试获取同步状态，直至成功（线程获取到 lock）**。这样也说明了**退出 await 方法必须是已经获得了 condition 引用（关联）的 lock**。到目前为止，开头的三个问题我们通过阅读源码的方式已经完全找到了答案，也对 await 方法的理解加深。await 方法示意图如下图：

![await方法示意图](http://upload-images.jianshu.io/upload_images/2615789-1cb1c2fe3c1ddf38.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

如图，调用 condition.await 方法的线程必须是已经获得了 lock，也就是当前线程是同步队列中的头结点。调用该方法后会使得当前线程所封装的 Node 尾插入到等待队列中。

> 超时机制的支持

condition 还额外支持了超时机制，使用者可调用方法 awaitNanos,awaitUtil。这两个方法的实现原理，基本上与 AQS 中的 tryAcquire 方法如出一辙，关于 tryAcquire 可以仔细阅读[这篇文章的第 3.4 部分](https://juejin.im/post/5aeb07ab6fb9a07ac36350c8#heading-6)。

> 不响应中断的支持

要想不响应中断可以调用 condition.awaitUninterruptibly()方法，该方法的源码为：

    public final void awaitUninterruptibly() {
        Node node = addConditionWaiter();
        int savedState = fullyRelease(node);
        boolean interrupted = false;
        while (!isOnSyncQueue(node)) {
            LockSupport.park(this);
            if (Thread.interrupted())
                interrupted = true;
        }
        if (acquireQueued(node, savedState) || interrupted)
            selfInterrupt();
    }

这段方法与上面的 await 方法基本一致，只不过减少了对中断的处理，并省略了 reportInterruptAfterWait 方法抛被中断的异常。

## 2.3 signal/signalAll 实现原理

**调用 condition 的 signal 或者 signalAll 方法可以将等待队列中等待时间最长的节点移动到同步队列中**，使得该节点能够有机会获得 lock。按照等待队列是先进先出（FIFO）的，所以等待队列的头节点必然会是等待时间最长的节点，也就是每次调用 condition 的 signal 方法是将头节点移动到同步队列中。我们来通过看源码的方式来看这样的猜想是不是对的，signal 方法源码为：

    public final void signal() {
        //1. 先检测当前线程是否已经获取lock
        if (!isHeldExclusively())
            throw new IllegalMonitorStateException();
        //2. 获取等待队列中第一个节点，之后的操作都是针对这个节点
    	Node first = firstWaiter;
        if (first != null)
            doSignal(first);
    }

signal 方法首先会检测当前线程是否已经获取 lock，如果没有获取 lock 会直接抛出异常，如果获取的话再得到等待队列的头指针引用的节点，之后的操作的 doSignal 方法也是基于该节点。下面我们来看看 doSignal 方法做了些什么事情，doSignal 方法源码为：

    private void doSignal(Node first) {
        do {
            if ( (firstWaiter = first.nextWaiter) == null)
                lastWaiter = null;
    		//1. 将头结点从等待队列中移除
            first.nextWaiter = null;
    		//2. while中transferForSignal方法对头结点做真正的处理
        } while (!transferForSignal(first) &&
                 (first = firstWaiter) != null);
    }

具体逻辑请看注释，真正对头节点做处理的逻辑在**transferForSignal**放，该方法源码为：

    final boolean transferForSignal(Node node) {
        /*
         * If cannot change waitStatus, the node has been cancelled.
         */
    	//1. 更新状态为0
        if (!compareAndSetWaitStatus(node, Node.CONDITION, 0))
            return false;

        /*
         * Splice onto queue and try to set waitStatus of predecessor to
         * indicate that thread is (probably) waiting. If cancelled or
         * attempt to set waitStatus fails, wake up to resync (in which
         * case the waitStatus can be transiently and harmlessly wrong).
         */
    	//2.将该节点移入到同步队列中去
        Node p = enq(node);
        int ws = p.waitStatus;
        if (ws > 0 || !compareAndSetWaitStatus(p, ws, Node.SIGNAL))
            LockSupport.unpark(node.thread);
        return true;
    }

关键逻辑请看注释，这段代码主要做了两件事情 1.将头结点的状态更改为 CONDITION；2.调用 enq 方法，将该节点尾插入到同步队列中，关于 enq 方法请看 AQS 的底层实现这篇文章。现在我们可以得出结论：**调用 condition 的 signal 的前提条件是当前线程已经获取了 lock，该方法会使得等待队列中的头节点即等待时间最长的那个节点移入到同步队列，而移入到同步队列后才有机会使得等待线程被唤醒，即从 await 方法中的 LockSupport.park(this)方法中返回，从而才有机会使得调用 await 方法的线程成功退出**。signal 执行示意图如下图：

![signal执行示意图](http://upload-images.jianshu.io/upload_images/2615789-3750f5baf7995623.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

> signalAll

sigllAll 与 sigal 方法的区别体现在 doSignalAll 方法上，前面我们已经知道 d**oSignal 方法只会对等待队列的头节点进行操作，**，而 doSignalAll 的源码为：

    private void doSignalAll(Node first) {
        lastWaiter = firstWaiter = null;
        do {
            Node next = first.nextWaiter;
            first.nextWaiter = null;
            transferForSignal(first);
            first = next;
        } while (first != null);
    }

该方法只不过时间等待队列中的每一个节点都移入到同步队列中，即“通知”当前调用 condition.await()方法的每一个线程。

# 3. await 与 signal/signalAll 的结合思考

文章开篇提到等待/通知机制，通过使用 condition 提供的 await 和 signal/signalAll 方法就可以实现这种机制，而这种机制能够解决最经典的问题就是“生产者与消费者问题”，关于“生产者消费者问题”之后会用单独的一篇文章进行讲解，这也是面试的高频考点。await 和 signal 和 signalAll 方法就像一个开关控制着线程 A（等待方）和线程 B（通知方）。它们之间的关系可以用下面一个图来表现得更加贴切：

![condition下的等待通知机制.png](http://upload-images.jianshu.io/upload_images/2615789-02449dc316fe1de6.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

如图，**线程 awaitThread 先通过 lock.lock()方法获取锁成功后调用了 condition.await 方法进入等待队列，而另一个线程 signalThread 通过 lock.lock()方法获取锁成功后调用了 condition.signal 或者 signalAll 方法，使得线程 awaitThread 能够有机会移入到同步队列中，当其他线程释放 lock 后使得线程 awaitThread 能够有机会获取 lock，从而使得线程 awaitThread 能够从 await 方法中退出执行后续操作。如果 awaitThread 获取 lock 失败会直接进入到同步队列**。

# 3. 一个例子

我们用一个很简单的例子说说 condition 的用法：

    public class AwaitSignal {
        private static ReentrantLock lock = new ReentrantLock();
        private static Condition condition = lock.newCondition();
        private static volatile boolean flag = false;

        public static void main(String[] args) {
            Thread waiter = new Thread(new waiter());
            waiter.start();
            Thread signaler = new Thread(new signaler());
            signaler.start();
        }

        static class waiter implements Runnable {

            @Override
            public void run() {
                lock.lock();
                try {
                    while (!flag) {
                        System.out.println(Thread.currentThread().getName() + "当前条件不满足等待");
                        try {
                            condition.await();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    System.out.println(Thread.currentThread().getName() + "接收到通知条件满足");
                } finally {
                    lock.unlock();
                }
            }
        }

        static class signaler implements Runnable {

            @Override
            public void run() {
                lock.lock();
                try {
                    flag = true;
                    condition.signalAll();
                } finally {
                    lock.unlock();
                }
            }
        }
    }

输出结果为：

    Thread-0当前条件不满足等待
    Thread-0接收到通知，条件满足

开启了两个线程 waiter 和 signaler，waiter 线程开始执行的时候由于条件不满足，执行 condition.await 方法使该线程进入等待状态同时释放锁，signaler 线程获取到锁之后更改条件，并通知所有的等待线程后释放锁。这时，waiter 线程获取到锁，并由于 signaler 线程更改了条件此时相对于 waiter 来说条件满足，继续执行。

> 参考文献

《java 并发编程的艺术》
