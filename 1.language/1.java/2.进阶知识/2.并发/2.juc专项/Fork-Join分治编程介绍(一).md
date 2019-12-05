# [Fork-Join分治编程介绍（一）](https://www.cnblogs.com/jinggod/p/8490511.html)

[目录](readme.md)

### **一、Fork-Join 框架介绍**

### **1. 什么是 Fork-Join 分治编程框架**

Fork/Join框架是Java7提供了的一个用于并行执行任务的框架，是一个把大任务分割成若干个小任务，最终汇总每个小任务结果后得到大任务结果的框架，这种开发方法也叫 **分治编程**。分治编程可以极大地利用CPU资源，提高任务执行的效率，也是目前与多线程有关的前沿技术。

### **2. 分治编程会遇到什么问题**

分治的原理上面说了，就是切割大任务成小任务来完成。咦，看起来好像也不难实现啊！为什么专门弄一个新的框架呢？
我们先看一下，在不使用 Fork-Join 框架时，使用普通的线程池是怎么实现的。

* 我们往一个线程池提交了一个大任务，规定好任务切割的阀值。
* 由池中线程（假设是线程A）执行大任务，发现大任务的大小大于阀值，于是切割成两个子任务，并调用 submit() 提交到线程池，得到返回的子任务的 Future。
* 线程A就调用 返回的 Future 的 get() 方法阻塞等待子任务的执行结果。
* 池中的其他线程（除线程A外，线程A被阻塞）执行两个子任务，然后判断子任务的大小有没有超过阀值，如果超过，则按照步骤2继续切割，否则，才计算并返回结果。

嘿，好像一切都很美好。真的吗？别忘了， **每一个切割任务的线程（如线程A）都被阻塞了，直到其子任务完成，才能继续往下运行** 。如果任务太大了，需要切割多次，那么就会有多个线程被阻塞，性能将会急速下降。更糟糕的是，如果你的线程池的线程数量是有上限的，极可能会造成池中所有线程被阻塞，线程池无法执行任务。

***@ Example1* 普通线程池实现分治时阻塞的问题**

来看一个例子，体会一下吧！下面的例子是将 1+2+...+10 的任务 分割成相加的个数不能超过3（即两端的差不能大于2）的多个子任务。

```java

//普通线程池下实现的分治效果测试
public class CommonThreadPoolTest {
    //固定大小的线程池，池中线程数量为3
    static ExecutorService fixPoolExcutor = Executors.newFixedThreadPool(3);

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        //计算 1+2+...+10  的结果
        CountTaskCallable task = new CountTaskCallable(1,10);
        //提交主人翁
        Future<Integer> future = fixPoolExcutor.submit(task);
        System.out.println("计算的结果："+future.get());
    }
}

```

```java
class CountTaskCallable implements Callable<Integer> {

    //设置阀值为2
    private static final int THRESHOLD = 2;
    private int start;
    private int end;

    public CountTaskCallable(int start, int end) {
        super();
        this.start = start;
        this.end = end;
    }

    @Override
    public Integer call() throws Exception {
        int sum = 0;
        //判断任务的大小是否超过阀值
        boolean canCompute = (end - start) <= THRESHOLD;
        if (canCompute) {
            for (int i = start; i <= end; i++) {
                sum += i;
            }
        } else {
            System.out.println("切割的任务："+start+"加到"+end+"   执行此任务的线程是 "+Thread.currentThread().getName());
            int middle = (start + end) / 2;
   
            CountTaskCallable leftTaskCallable = new CountTaskCallable(start, middle);
            CountTaskCallable rightTaskCallable = new CountTaskCallable(middle + 1, end);
            // 将子任务提交到线程池中
            Future<Integer> leftFuture = CommonThreadPoolTest.fixPoolExcutor.submit(leftTaskCallable);
            Future<Integer> rightFuture = CommonThreadPoolTest.fixPoolExcutor.submit(rightTaskCallable);
            //阻塞等待子任务的执行结果
            int leftResult = leftFuture.get();
            int rightResult = rightFuture.get();
            // 合并子任务的执行结果
            sum = leftResult + rightResult;
            
        }
        return sum;
    }

}

```

**运行结果**

```
切割的任务：1加到10 执行此任务的线程是 pool-1-thread-1
切割的任务：1加到5 执行此任务的线程是 pool-1-thread-2
切割的任务：6加到10 执行此任务的线程是 pool-1-thread-3
```

池的线程只有三个，当任务分割了三次后，池中的线程也就都被阻塞了，无法再执行任何任务，一直卡着动不了。

### **3. 工作窃取算法**

针对上面的问题，Fork-Join 框架使用了 “工作窃取（work-stealing）”算法。工作窃取（work-stealing）算法是指某个线程从其他队列里窃取任务来执行。看一下《Java 并发编程的艺术》对工作窃取算法的解释：
使用工作窃取算法有什么优势呢？假如我们需要做一个比较大的任务，我们可以把这个任务分割为若干互不依赖的子任务，为了减少线程间的竞争，于是把这些子任务分别放到不同的队列里，并为每个队列创建一个单独的线程来执行队列里的任务，线程和队列一一对应，比如A线程负责处理A队列里的任务。但是有的线程会先把自己队列里的任务干完，而其他线程对应的队列里还有任务等待处理。干完活的线程与其等着，不如去帮其他线程干活，于是它就去其他线程的队列里窃取一个任务来执行。而在这时它们会访问同一个队列，所以为了减少窃取任务线程和被窃取任务线程之间的竞争，通常会使用双端队列，被窃取任务线程永远从双端队列的头部拿任务执行，而窃取任务的线程永远从双端队列的尾部拿任务执行。

![](https://images2018.cnblogs.com/blog/1243403/201803/1243403-20180301204836868-655269115.png)

**Fork-Join 框架使用工作窃取算法对分治编程实现的描述：**

下面是 ForkJoin 框架对分治编程实现的过程的描述，增加对工作窃取算法的理解。在下面的内容提供了一个分治的例子，可结合这部分描述一起看。这里仅是简单的描述，如果想深入了解，可参考我下一篇的源码分析的文章，让你知其然，也知其所以然。

* Fork-Join 框架的线程池ForkJoinPool 的任务分为“外部任务” 和 “内部任务”。
* “外部任务”是放在 ForkJoinPool 的全局队列里；
* ForkJoinPool 池中的每个线程都维护着一个内部队列，用于存放“内部任务”。
* 线程切割任务得到的子任务就会作为“内部任务”放到内部队列中。
* 当此线程要想要拿到子任务的计算结果时，先判断子任务没有完成，如果没有完成，则再判断子任务有没有被其他线程“窃取”，一旦子任务被窃取了则去执行本线程“内部队列”的其他任务，或者扫描其他的任务队列，窃取任务，如果子任务没有被窃取，则由本线程来完成。
* 最后，当线程完成了其“内部任务”，处于空闲的状态时，就会去扫描其他的任务队列，窃取任务，尽可能地

**总之，ForkJoin线程在等待一个任务的完成时，要么自己来完成这个任务，或者在其他线程窃取了这个任务的情况下，去执行其他任务，是不会阻塞等待，从而避免浪费资源，除非是所有任务队列都为空。**

**工作窃取算法的优点：**

Fork-Join 框架中的工作窃取算法的优点可以总结为以下两点：

* 线程是不会因为等待某个子任务的完成或者没有内部任务要执行而被阻塞等待、挂起，而是会扫描所有的队列，窃取任务，直到所有队列都为空时，才会被挂起。 就如上面所说的。
* Fork-Join 框架在多CPU的环境下，能提供很好的并行性能。在使用普通线程池的情况下，当CPU不再是性能瓶颈时，能并行地运行多个线程，然而却因为要互斥访问一个任务队列而导致性能提高不上去。而 Fork-Join 框架为每个线程为维护着一个内部任务队列，以及一个全局的任务队列，而且任务队列都是双向队列，可从首尾两端来获取任务，极大地减少了竞争的可能性，提高并行的性能。

### **二、 Fork-Join 框架的使用介绍**

JDK7引入的Fork/Join有三个核心类：

**ForkJoinPool：** 执行任务的线程池，继承了 AbstractExecutorService 类。
**ForkJoinWorkerThread：** 执行任务的工作线程（即 ForkJoinPool 线程池里的线程）。每个线程都维护着一个内部队列，用于存放“内部任务”。继承了 Thread 类。
**ForkJoinTask：** 一个用于ForkJoinPool的任务抽象类。实现了 Future 接口

因为ForkJoinTask比较复杂，抽象方法比较多，日常使用时一般不会继承ForkJoinTask来实现自定义的任务，而是继承ForkJoinTask的两个子类，实现 compute() 方法：

**RecursiveTask：** 子任务带返回结果时使用
**RecursiveAction：** 子任务不带返回结果时使用

**compute 方法的实现模式一般是：**

```
if 任务足够小
    直接返回结果
else
    分割成N个子任务
    依次调用每个子任务的fork方法执行子任务
    依次调用每个子任务的join方法合并执行结果

```

对于Fork/Join框架的原理，Doug Lea的文章：[A Java Fork/Join Framework](http://gee.cs.oswego.edu/dl/papers/fj.pdf);

***@ Example2* 分治例子**

下面的例子与 @Exampel1 是一样的，计算 1+2+....+12 的结果。
使用Fork／Join框架首先要考虑到的是如何分割任务，如果我们希望每个子任务最多执行两个数的相加，那么我们设置分割的阈值是2，由于是12个数字相加。同时，观察执行任务的线程名称，理解工作窃取算法的实现。

```java
public class CountTest {
    public static void main(String[] args) throws InterruptedException, ExecutionException {

        ForkJoinPool forkJoinPool = new ForkJoinPool();
        //创建一个计算任务，计算 由1加到12
        CountTask countTask = new CountTask(1, 12);
        Future<Integer> future = forkJoinPool.submit(countTask);
        System.out.println("最终的计算结果："+future.get());
    }
}

```

```java
class CountTask extends RecursiveTask<Integer>{

    private static final int THRESHOLD = 2;
    private int start;
    private int end;
    
    
    public CountTask(int start, int end) {
        this.start = start;
        this.end = end;
    }

    @Override
    protected Integer compute() {
        int sum = 0;
        boolean canCompute = (end - start) <= THRESHOLD;
        
        if(canCompute){//任务已经足够小，可以直接计算，并返回结果
            for(int i = start;i<=end;i++){
                sum += i;
            }
            System.out.println("执行计算任务，计算    "+start+"到 "+end+"的和  ，结果是："+sum+"   执行此任务的线程："+Thread.currentThread().getName());

        }else{ //任务过大，需要切割
            System.out.println("任务过大，切割的任务：  "+start+"加到 "+end+"的和       执行此任务的线程："+Thread.currentThread().getName());
            int middle = (start+end)/2;
            //切割成两个子任务
            CountTask leftTask = new CountTask(start, middle);
            CountTask rightTask = new CountTask(middle+1, end);
            //执行子任务
            leftTask.fork();
            rightTask.fork();
            //等待子任务的完成，并获取执行结果
            int leftResult = leftTask.join();
            int rightResult = rightTask.join();
            //合并子任务
            sum = leftResult+rightResult;
        }
        return sum;
    }
}

```

**运行结果：**
```java
任务过大，切割的任务： 1加到 12的和 执行此任务的线程：ForkJoinPool-1-worker-1
任务过大，切割的任务： 7加到 12的和 执行此任务的线程：ForkJoinPool-1-worker-3
任务过大，切割的任务： 1加到 6的和 执行此任务的线程：ForkJoinPool-1-worker-2
执行计算任务，计算 7到 9的和 ，结果是：24 执行此任务的线程：ForkJoinPool-1-worker-3
执行计算任务，计算 1到 3的和 ，结果是：6 执行此任务的线程：ForkJoinPool-1-worker-1
执行计算任务，计算 4到 6的和 ，结果是：15 执行此任务的线程：ForkJoinPool-1-worker-1
执行计算任务，计算 10到 12的和 ，结果是：33 执行此任务的线程：ForkJoinPool-1-worker-3
最终的计算结果：78
```

从结果可以看出，提交的计算任务是由线程1执行，线程1进行了第一次切割，切割成两个子任务 “7加到12“ 和 ”1加到6“，并提交这两个子任务。然后这两个任务便被 线程2、线程3 给窃取了。线程1 的内部队列中已经没有任务了，这时候，线程2、线程3 也分别进行了一次任务切割并各自提交了两个子任务，于是线程1也去窃取任务（这里窃取的都是线程2的子任务）。

如果想深入了解 Fork-Join 框架，可参考 Fork-Join 原理深入分析（二）

**参考文献**

* https://www.cnblogs.com/wanly3643/p/3951659.html
* 《java并发编程的艺术》

分类: [Java并发知识整理](https://www.cnblogs.com/jinggod/category/1168984.html)
