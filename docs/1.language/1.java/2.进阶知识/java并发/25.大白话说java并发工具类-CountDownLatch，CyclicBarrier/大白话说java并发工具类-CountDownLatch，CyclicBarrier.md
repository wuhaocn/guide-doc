# 1. 倒计时器 CountDownLatch

在多线程协作完成业务功能时，有时候需要等待其他多个线程完成任务之后，主线程才能继续往下执行业务功能，在这种的业务场景下，通常可以使用 Thread 类的 join 方法，让主线程等待被 join 的线程执行完之后，主线程才能继续往下执行。当然，使用线程间消息通信机制也可以完成。其实，java 并发工具类中为我们提供了类似“倒计时”这样的工具类，可以十分方便的完成所说的这种业务场景。

为了能够理解 CountDownLatch，举一个很通俗的例子，运动员进行跑步比赛时，假设有 6 个运动员参与比赛，裁判员在终点会为这 6 个运动员分别计时，可以想象没当一个运动员到达终点的时候，对于裁判员来说就少了一个计时任务。直到所有运动员都到达终点了，裁判员的任务也才完成。这 6 个运动员可以类比成 6 个线程，当线程调用 CountDownLatch.countDown 方法时就会对计数器的值减一，直到计数器的值为 0 的时候，裁判员（调用 await 方法的线程）才能继续往下执行。

下面来看些 CountDownLatch 的一些重要方法。

先从 CountDownLatch 的构造方法看起：

    public CountDownLatch(int count)

构造方法会传入一个整型数 N，之后调用 CountDownLatch 的`countDown`方法会对 N 减一，知道 N 减到 0 的时候，当前调用`await`方法的线程继续执行。

CountDownLatch 的方法不是很多，将它们一个个列举出来：

1. await() throws InterruptedException：调用该方法的线程等到构造方法传入的 N 减到 0 的时候，才能继续往下执行；
2. await(long timeout, TimeUnit unit)：与上面的 await 方法功能一致，只不过这里有了时间限制，调用该方法的线程等到指定的 timeout 时间后，不管 N 是否减至为 0，都会继续往下执行；
3. countDown()：使 CountDownLatch 初始值 N 减 1；
4. long getCount()：获取当前 CountDownLatch 维护的值；

下面用一个具体的例子来说明 CountDownLatch 的具体用法:

    public class CountDownLatchDemo {
    private static CountDownLatch startSignal = new CountDownLatch(1);
    //用来表示裁判员需要维护的是6个运动员
    private static CountDownLatch endSignal = new CountDownLatch(6);

    public static void main(String[] args) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(6);
        for (int i = 0; i < 6; i++) {
            executorService.execute(() -> {
                try {
                    System.out.println(Thread.currentThread().getName() + " 运动员等待裁判员响哨！！！");
                    startSignal.await();
                    System.out.println(Thread.currentThread().getName() + "正在全力冲刺");
                    endSignal.countDown();
                    System.out.println(Thread.currentThread().getName() + "  到达终点");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        System.out.println("裁判员发号施令啦！！！");
        startSignal.countDown();
        endSignal.await();
        System.out.println("所有运动员到达终点，比赛结束！");
        executorService.shutdown();
    }
    }
    输出结果：

    pool-1-thread-2 运动员等待裁判员响哨！！！
    pool-1-thread-3 运动员等待裁判员响哨！！！
    pool-1-thread-1 运动员等待裁判员响哨！！！
    pool-1-thread-4 运动员等待裁判员响哨！！！
    pool-1-thread-5 运动员等待裁判员响哨！！！
    pool-1-thread-6 运动员等待裁判员响哨！！！
    裁判员发号施令啦！！！
    pool-1-thread-2正在全力冲刺
    pool-1-thread-2  到达终点
    pool-1-thread-3正在全力冲刺
    pool-1-thread-3  到达终点
    pool-1-thread-1正在全力冲刺
    pool-1-thread-1  到达终点
    pool-1-thread-4正在全力冲刺
    pool-1-thread-4  到达终点
    pool-1-thread-5正在全力冲刺
    pool-1-thread-5  到达终点
    pool-1-thread-6正在全力冲刺
    pool-1-thread-6  到达终点
    所有运动员到达终点，比赛结束！

该示例代码中设置了两个 CountDownLatch，第一个`endSignal`用于控制让 main 线程（裁判员）必须等到其他线程（运动员）让 CountDownLatch 维护的数值 N 减到 0 为止。另一个`startSignal`用于让 main 线程对其他线程进行“发号施令”，startSignal 引用的 CountDownLatch 初始值为 1，而其他线程执行的 run 方法中都会先通过 `startSignal.await()`让这些线程都被阻塞，直到 main 线程通过调用`startSignal.countDown();`，将值 N 减 1，CountDownLatch 维护的数值 N 为 0 后，其他线程才能往下执行，并且，每个线程执行的 run 方法中都会通过`endSignal.countDown();`对`endSignal`维护的数值进行减一，由于往线程池提交了 6 个任务，会被减 6 次，所以`endSignal`维护的值最终会变为 0，因此 main 线程在`latch.await();`阻塞结束，才能继续往下执行。

另外，需要注意的是，当调用 CountDownLatch 的 countDown 方法时，当前线程是不会被阻塞，会继续往下执行，比如在该例中会继续输出`pool-1-thread-4 到达终点`。

# 2. 循环栅栏：CyclicBarrier

CyclicBarrier 也是一种多线程并发控制的实用工具，和 CountDownLatch 一样具有等待计数的功能，但是相比于 CountDownLatch 功能更加强大。

为了理解 CyclicBarrier，这里举一个通俗的例子。开运动会时，会有跑步这一项运动，我们来模拟下运动员入场时的情况，假设有 6 条跑道，在比赛开始时，就需要 6 个运动员在比赛开始的时候都站在起点了，裁判员吹哨后才能开始跑步。跑道起点就相当于“barrier”，是临界点，而这 6 个运动员就类比成线程的话，就是这 6 个线程都必须到达指定点了，意味着凑齐了一波，然后才能继续执行，否则每个线程都得阻塞等待，直至凑齐一波即可。cyclic 是循环的意思，也就是说 CyclicBarrier 当多个线程凑齐了一波之后，仍然有效，可以继续凑齐下一波。CyclicBarrier 的执行示意图如下：

![CyclicBarrier执行示意图.jpg](https://upload-images.jianshu.io/upload_images/2615789-5bacb4f757882e56.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/500)

当多个线程都达到了指定点后，才能继续往下继续执行。这就有点像报数的感觉，假设 6 个线程就相当于 6 个运动员，到赛道起点时会报数进行统计，如果刚好是 6 的话，这一波就凑齐了，才能往下执行。**CyclicBarrier 在使用一次后，下面依然有效，可以继续当做计数器使用，这是与 CountDownLatch 的区别之一。**这里的 6 个线程，也就是计数器的初始值 6，是通过 CyclicBarrier 的构造方法传入的。

下面来看下 CyclicBarrier 的主要方法：

    //等到所有的线程都到达指定的临界点
    await() throws InterruptedException, BrokenBarrierException

    //与上面的await方法功能基本一致，只不过这里有超时限制，阻塞等待直至到达超时时间为止
    await(long timeout, TimeUnit unit) throws InterruptedException,
    BrokenBarrierException, TimeoutException

    //获取当前有多少个线程阻塞等待在临界点上
    int getNumberWaiting()

    //用于查询阻塞等待的线程是否被中断
    boolean isBroken()


    //将屏障重置为初始状态。如果当前有线程正在临界点等待的话，将抛出BrokenBarrierException。
    void reset()

另外需要注意的是，CyclicBarrier 提供了这样的构造方法：

    public CyclicBarrier(int parties, Runnable barrierAction)

可以用来，当指定的线程都到达了指定的临界点的时，接下来执行的操作可以由 barrierAction 传入即可。

> 一个例子

下面用一个简单的例子，来看下 CyclicBarrier 的用法，我们来模拟下上面的运动员的例子。

    public class CyclicBarrierDemo {
        //指定必须有6个运动员到达才行
        private static CyclicBarrier barrier = new CyclicBarrier(6, () -> {
            System.out.println("所有运动员入场，裁判员一声令下！！！！！");
        });
        public static void main(String[] args) {
            System.out.println("运动员准备进场，全场欢呼............");

            ExecutorService service = Executors.newFixedThreadPool(6);
            for (int i = 0; i < 6; i++) {
                service.execute(() -> {
                    try {
                        System.out.println(Thread.currentThread().getName() + " 运动员，进场");
                        barrier.await();
                        System.out.println(Thread.currentThread().getName() + "  运动员出发");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (BrokenBarrierException e) {
                        e.printStackTrace();
                    }
                });
            }
        }

    }

    输出结果：
    运动员准备进场，全场欢呼............
    pool-1-thread-2 运动员，进场
    pool-1-thread-1 运动员，进场
    pool-1-thread-3 运动员，进场
    pool-1-thread-4 运动员，进场
    pool-1-thread-5 运动员，进场
    pool-1-thread-6 运动员，进场
    所有运动员入场，裁判员一声令下！！！！！
    pool-1-thread-6  运动员出发
    pool-1-thread-1  运动员出发
    pool-1-thread-5  运动员出发
    pool-1-thread-4  运动员出发
    pool-1-thread-3  运动员出发
    pool-1-thread-2  运动员出发

从输出结果可以看出，当 6 个运动员（线程）都到达了指定的临界点（barrier）时候，才能继续往下执行，否则，则会阻塞等待在调用`await()`处

# 3. CountDownLatch 与 CyclicBarrier 的比较

CountDownLatch 与 CyclicBarrier 都是用于控制并发的工具类，都可以理解成维护的就是一个计数器，但是这两者还是各有不同侧重点的：

1. CountDownLatch 一般用于某个线程 A 等待若干个其他线程执行完任务之后，它才执行；而 CyclicBarrier 一般用于一组线程互相等待至某个状态，然后这一组线程再同时执行；CountDownLatch 强调一个线程等多个线程完成某件事情。CyclicBarrier 是多个线程互等，等大家都完成，再携手共进。
2. 调用 CountDownLatch 的 countDown 方法后，当前线程并不会阻塞，会继续往下执行；而调用 CyclicBarrier 的 await 方法，会阻塞当前线程，直到 CyclicBarrier 指定的线程全部都到达了指定点的时候，才能继续往下执行；
3. CountDownLatch 方法比较少，操作比较简单，而 CyclicBarrier 提供的方法更多，比如能够通过 getNumberWaiting()，isBroken()这些方法获取当前多个线程的状态，**并且 CyclicBarrier 的构造方法可以传入 barrierAction**，指定当所有线程都到达时执行的业务功能；
4. CountDownLatch 是不能复用的，而 CyclicLatch 是可以复用的。
