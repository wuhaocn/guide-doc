### 1.简介
      早期的JDK版本中，如果要并发的对Integer、Long、Double之类的Java原始类型或引用类型进行操作，一般都需要通过锁来控制并发，
    以防止数据不一致。JUC-Atomic原子类位于java.util.concurrent.atomic包下。该包提供了许多Java原始/引用类型的映射类。
    如AtomicInteger、AtomicLong、AtomicBoolean，这些类可以通过一种“无锁算法”，线程安全的操作Integer、Long、Boolean等原始类型。
    
    包中类分为五种：
    
    基本类型：
        AtomicBoolean：布尔型原子类
        AtomicInteger：整型原子类
        AtomicLong：长整型原子类
    
    数组：
        AtomicIntegerArray：整形数组原子类
        AtomicLongArray：长整形数组原子类
        AtomicReferenceArray：引用类型数组原子类
    
    引用类型：
        AtomicReference：引用类型原子类
        AtomicStampedRerence：原子更新引用类型里的字段原子类
        AtomicMarkableReference：原子更新带有标记位的引用类型
    
    对象的属性：
        AtomicIntegerFieldUpdater：原子更新整形字段的更新器
        AtomicLongFieldUpdater：原子更新长整形字段的更新器
        AtomicReferenceFieldUpdater：原子更新带有版本号的引用类型。
            该类将整数值与引用关联起来，可用于解决原子的更新数据和数据的版本号，可以解决使用 CAS 进行原子更新时可能出现的 ABA 问题
        　　本文不会详细介绍这几种类型的api及使用，只是列出Atomic的实现原理，及比较重点的类
    
    基本类型原子类：　
  
        AtomicBoolean：布尔型原子类
        AtomicInteger：整型原子类
        AtomicLong：长整型原子类
        这几个类的共同特点是都提供单个变量的原子方式访问和更新功能。以AtomicLong为代表，进行介绍。
 
 ### 2.实例解析
 
 - 例子：我们使用AtomicLong来演示之前的线程不安全的
 
```java
 /**
  * 并发测试代码
  */
 @ThreadSafe
 public class AtomicExample2 {
     //请求总数
     public static int clientTotal = 5000;
     //同时并发执行的线程数
     public static int threadTotal = 200;
     //变成了AtomicLong类型
     public static AtomicLong count = new AtomicLong(0);
     public static void main(String[] args) throws InterruptedException {
         //创建线程池
         ExecutorService executorService = Executors.newCachedThreadPool();
         //定义信号量
         final Semaphore semaphore = new Semaphore(threadTotal);
         //定义计数器 闭锁
         final CountDownLatch countDownLatch = new CountDownLatch(clientTotal);
         for (int i = 0; i < clientTotal; i++) {
             executorService.execute(() ->{
                 try {
                     semaphore.acquire();
                     add();
                     //释放
                     semaphore.release();
                 } catch (Exception e) {
                     System.out.println("exception:"+e.getMessage());
                 }
                 countDownLatch.countDown();
             });
         }
         countDownLatch.await();
         executorService.shutdown();
         System.out.println("count:{}"+count.get());
     }
     private static void add(){
         count.incrementAndGet();//先做增加再获取当前值
         //count.getAndIncrement();先获取当前值再做增加
     }
 }
```
 - 解析
 
         当使用AtomicLong去执行自增操作时，得出的最终结果count就是5000。数次运行情况下结果一致。不会带来线程不安全的情况。
         那我们来看看AtomicLong是如何保证线程安全的呢。
         
         我们看看incrementAndGet方法，看看AtomicLong如何实现单个变量的原子方式更新。Unsafe是CAS的核心类，AtomicLong是基于CAS实现的。
         此处就介绍AtomicLong，AtomicBoolean、AtomicInteger、AtomicReference与之相似，就不一一介绍
 
 ```java
 private static final Unsafe unsafe = Unsafe.getUnsafe();
 
 public final long incrementAndGet() {
     return unsafe.getAndAddLong(this, valueOffset, 1L) + 1L;
 }
 ```
 incrementAndGet方法实际上是调用Unsafe类的方法来执行操作，我们进入Unsafe里看看具体的getAndAddLong是如何实现原子方式更新的。
 
 ```java
 public final long getAndAddLong(Object var1, long var2, long var4) {
     long var6;
     do {
         var6 = this.getLongVolatile(var1, var2);
     } while(!this.compareAndSwapLong(var1, var2, var6, var6 + var4));
     return var6;
 }
```
     我们来解析一下这个方法，var1为当前调用这个方法的对象，var2是当前值，假如执行的2+1=3的操作，那么var4就是1。
     var6是调用底层方法获得底层当前值。假设没有其他线程来处理count，那么var6就是var2。此处使用了一个do while循环。
     compareAndSwapLong方法是native的，代表是java底层的方法。也是遵循CAS算法的api。compareAndSwap，比较并交换。
     在getAndAddLong的while判断中，该方法实现的是：对于var1这个对象，如果当前值var2和底层值var6相同的话，就更新为后面的操作结果值。
     当我们执行更新结果时，可能被其他线程修改，因此此处判断当前值与期望值相同时才允许更新。否则重新取出当前的底层值，和当前count的值再做比较。
     保证当前值与底层值完全一致时才进行结果更新，以此保证线程安全。这也是Atomic使用CAS原理实现的机制。底层值是主内存中的值，当前值是源自于工作内存。
     由于该方法的逻辑是采用自旋的方式不断更新目标值，直到更新成功，在并发量较低的环境下，线程冲突较少，自旋次数不会很多。
     但是在高并发情况下，N个线程同时进行自旋操作，会出现大量失败并不断自旋的情况，此时的AtomicLong的自旋会成为瓶颈，
     因此为了解决高并发环境下的AtomicLong的自旋瓶颈问题，引入了LongAdder。
 
**LongAdder：**
 
 　　AtomicLong中有个内部变量value保存着实际的long值，所有的操作都是针对该变量进行。也就是说，高并发环境下，value变量其实是一个热点，也就是N个线程竞争一个热点。LongAdder的基本思路就是分散热点，将value值分散到一个数组中，不同线程会命中到数组的不同槽中，各个线程只对自己槽中的那个值进行CAS操作，这样热点就被分散了，冲突的概率就小很多。如果要获取真正的long值，只要将各个槽中的变量值累加返回。
 
 　　低并发、一般的业务场景下AtomicLong是足够了。如果并发量很多，存在大量写多读少的情况，那LongAdder可能更合适。
 
**AtomicBoolean：**
 
 针对该类我们主要研究compareAndSet函数
 
```java
public final boolean compareAndSet(boolean expect, boolean update) {
 int e = expect ? 1 : 0;
 int u = update ? 1 : 0;
 return unsafe.compareAndSwapInt(this, valueOffset, e, u);
}
```
该函数实现的功能是高并发情况下只有一个线程能访问这个属性值，常用于初始化一次的功能中。
 
```java
private static AtomicBoolean initialized = new AtomicBoolean(false);
 public void init()
 {
    if( initialized.compareAndSet(false, true) )//如果为false，更新为true
    {
        // 初始化操作代码....
    }
 }
```

    各原子类api及使用demo，可以参考：https://github.com/Snailclimb/JavaGuide/blob/master/Java%E7%9B%B8%E5%85%B3/Multithread/Atomic.md
    
    主要是掌握CAS算法的设计思想，了解原子类如何保证原子操作。
     
  

 ### 参考
 
 https://www.cnblogs.com/zhangbLearn/p/9922790.html