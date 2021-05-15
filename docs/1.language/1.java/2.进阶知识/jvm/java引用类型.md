## 软引用、弱引用、虚引用-他们的特点及应用场景

- 参考: https://www.jianshu.com/p/825cca41d962

### 为什么会有这 4 种引用

Java 中的引用的定义很传统：如果 reference 类型的数据中存储的数值代表的是另外一块内存的起始地址，
就称这块内存代表着一个引用。 这种定义很纯粹，但是太过狭隘，一个对象在这种定义下只有被引用或者没有被引用两种状态，
对于如何描述一些“食之无味，弃之可惜”的对象就显得无能为力。 我们希望能描述这样一类对象：
当内存空间还足够时，则能保留在内存之中；如果内存空间在进行垃圾收集后还是非常紧张，则可以抛弃这些对象。
很多系统的缓存功能都符合这样的应用场景。

说白了传统的两种应用没法描述对象生命周期中的多种状态，对象有哪些状态呢。

在 Java 中，对象的生命周期包括以下几个阶段：

- 创建阶段(Created)
  应用阶段(In Use)
  不可见阶段(Invisible)
  不可达阶段(Unreachable)
  收集阶段(Collected)
  终结阶段(Finalized)
  对象空间重分配阶段(De-allocated)
  创建阶段(Created)
  在创建阶段系统通过下面的几个步骤来完成对象的创建过程

为对象分配存储空间
开始构造对象
从超类到子类对 static 成员进行初始化
超类成员变量按顺序初始化，递归调用超类的构造方法
子类成员变量按顺序初始化，子类构造方法调用
一旦对象被创建，并被分派给某些变量赋值，这个对象的状态就切换到了应用阶段
应用阶段(In Use)
对象至少被一个强引用持有着。

不可见阶段(Invisible)
当一个对象处于不可见阶段时，说明程序本身不再持有该对象的任何强引用，虽然该这些引用仍然是存在着的。
简单说就是程序的执行已经超出了该对象的作用域了。

不可达阶段(Unreachable)
对象处于不可达阶段是指该对象不再被任何强引用所持有。
与“不可见阶段”相比，“不可见阶段”是指程序不再持有该对象的任何强引用，这种情况下，该对象仍可能被 JVM 等系统下的某些已装载的静态变量或线程或 JNI 等强引用持有着，这些特殊的强引用被称为”GC root”。存在着这些 GC root 会导致对象的内存泄露情况，无法被回收。

收集阶段(Collected)
当垃圾回收器发现该对象已经处于“不可达阶段”并且垃圾回收器已经对该对象的内存空间重新分配做好准备时，则对象进入了“收集阶段”。如果该对象已经重写了 finalize()方法，则会去执行该方法的终端操作。
这里要特别说明一下：不要重载 finazlie()方法！原因有两点：

会影响 JVM 的对象分配与回收速度
在分配该对象时，JVM 需要在垃圾回收器上注册该对象，以便在回收时能够执行该重载方法；在该方法的执行时需要消耗 CPU 时间且在执行完该方法后才会重新执行回收操作，即至少需要垃圾回收器对该对象执行两次 GC。

可能造成该对象的再次“复活”
在 finalize()方法中，如果有其它的强引用再次持有该对象，则会导致对象的状态由“收集阶段”又重新变为“应用阶段”。这个已经破坏了 Java 对象的生命周期进程，且“复活”的对象不利用后续的代码管理。

终结阶段
当对象执行完 finalize()方法后仍然处于不可达状态时，则该对象进入终结阶段。在该阶段是等待垃圾回收器对该对象空间进行回收。

对象空间重新分配阶段
垃圾回收器对该对象的所占用的内存空间进行回收或者再分配了，则该对象彻底消失了，称之为“对象空间重新分配阶段”。

哪 4 种，各有什么特点
强引用
强引用是使用最普遍的引用。如果一个对象具有强引用，那垃圾收器绝不会回收它。当内存空间不足，Java 虚拟机宁愿抛出 OutOfM moryError 错误，使程序异常终止，也不会靠随意回收具有强引用 对象来解决内存不足的问题。

软引用
软引用是用来描述一些还有用但并非必须的对象。对于软引用关联着的对象，在系统将要发生内存溢出异常之前，将会把这些对象列进回收范围进行第二次回收。如果这次回收还没有足够的内存，才会抛出内存溢出异常。

/\*\*

- 软引用何时被收集
- 运行参数 -Xmx200m -XX:+PrintGC
- Created by ccr at 2018/7/14.
  \*/
  public class SoftReferenceDemo {

  public static void main(String[] args) throws InterruptedException {
  //100M 的缓存数据
  byte[] cacheData = new byte[100 * 1024 * 1024];
  //将缓存数据用软引用持有
  SoftReference<byte[]> cacheRef = new SoftReference<>(cacheData);
  //将缓存数据的强引用去除
  cacheData = null;
  System.out.println("第一次 GC 前" + cacheData);
  System.out.println("第一次 GC 前" + cacheRef.get());
  //进行一次 GC 后查看对象的回收情况
  System.gc();
  //等待 GC
  Thread.sleep(500);
  System.out.println("第一次 GC 后" + cacheData);
  System.out.println("第一次 GC 后" + cacheRef.get());

       //在分配一个120M的对象，看看缓存对象的回收情况
       byte[] newData = new byte[120 * 1024 * 1024];
       System.out.println("分配后" + cacheData);
       System.out.println("分配后" + cacheRef.get());

  }

}

第一次 GC 前 null
第一次 GC 前[B@7d4991ad
[GC (System.gc()) 105728K->103248K(175104K), 0.0009623 secs][full gc (system.gc()) 103248k->103139k(175104k), 0.0049909 secs]
第一次 GC 后 null
第一次 GC 后[B@7d4991ad
[GC (Allocation Failure) 103805K->103171K(175104K), 0.0027889 secs][gc (allocation failure) 103171k->103171k(175104k), 0.0016018 secs]
[Full GC (Allocation Failure) 103171K->103136K(175104K), 0.0089988 secs][gc (allocation failure) 103136k->103136k(199680k), 0.0009408 secs]
[Full GC (Allocation Failure) 103136K->719K(128512K), 0.0082685 secs]
分配后 null
分配后 null
从上面的示例中就能看出，软引用关联的对象不会被 GC 回收。JVM 在分配空间时，若果 Heap 空间不足，就会进行相应的 GC，但是这次 GC 并不会收集软引用关联的对象，但是在 JVM 发现就算进行了一次回收后还是不足（Allocation Failure），JVM 会尝试第二次 GC，回收软引用关联的对象。

像这种如果内存充足，GC 时就保留，内存不够，GC 再来收集的功能很适合用在缓存的引用场景中。在使用缓存时有一个原则，如果缓存中有就从缓存获取，如果没有就从数据库中获取，缓存的存在是为了加快计算速度，如果因为缓存导致了内存不足进而整个程序崩溃，那就得不偿失了。

弱引用
弱引用也是用来描述非必须对象的，他的强度比软引用更弱一些，被弱引用关联的对象，在垃圾回收时，如果这个对象只被弱引用关联（没有任何强引用关联他），那么这个对象就会被回收。

/\*\*

- 弱引用关联对象何时被回收
- Created by ccr at 2018/7/14.
  _/
  public class WeakReferenceDemo {
  public static void main(String[] args) throws InterruptedException {
  //100M 的缓存数据
  byte[] cacheData = new byte[100 _ 1024 \* 1024];
  //将缓存数据用软引用持有
  WeakReference<byte[]> cacheRef = new WeakReference<>(cacheData);
  System.out.println("第一次 GC 前" + cacheData);
  System.out.println("第一次 GC 前" + cacheRef.get());
  //进行一次 GC 后查看对象的回收情况
  System.gc();
  //等待 GC
  Thread.sleep(500);
  System.out.println("第一次 GC 后" + cacheData);
  System.out.println("第一次 GC 后" + cacheRef.get());

       //将缓存数据的强引用去除
       cacheData = null;
       System.gc();
       //等待GC
       Thread.sleep(500);
       System.out.println("第二次GC后" + cacheData);
       System.out.println("第二次GC后" + cacheRef.get());

  }
  }
  第一次 GC 前[B@7d4991ad
  第一次 GC 前[B@7d4991ad
  第一次 GC 后[B@7d4991ad
  第一次 GC 后[B@7d4991ad
  第二次 GC 后 null
  第二次 GC 后 null
  从上面的代码中可以看出，弱引用关联的对象是否回收取决于这个对象有没有其他强引用指向它。这个确实很难理解，既然弱引用关联对象的存活周期和强引用差不多，那直接用强引用好了，干嘛费用弄出个弱引用呢？其实弱引用存在必然有他的应用场景。

static Map<Object,Object> container = new HashMap<>();
public static void putToContainer(Object key,Object value){
container.put(key,value);
}

public static void main(String[] args) {
//某个类中有这样一段代码
Object key = new Object();
Object value = new Object();
putToContainer(key,value);

    //..........
    /**
     * 若干调用层次后程序员发现这个key指向的对象没有用了，
     * 为了节省内存打算把这个对象抛弃，然而下面这个方式真的能把对象回收掉吗？
     * 由于container对象中包含了这个对象的引用,所以这个对象不能按照程序员的意向进行回收.
     * 并且由于在程序中的任何部分没有再出现这个键，所以，这个键 / 值 对无法从映射中删除。
     * 很可能会造成内存泄漏。
     */
    key = null;

}
下面一段话摘自《Java 核心技术卷 1》：

设计 WeakHashMap 类是为了解决一个有趣的问题。如果有一个值，对应的键已经不再 使用了， 将会出现什么情况呢？ 假定对某个键的最后一次引用已经消亡，不再有任何途径引 用这个值的对象了。但是，由于在程序中的任何部分没有再出现这个键，所以，这个键 / 值 对无法从映射中删除。为什么垃圾回收器不能够删除它呢？ 难道删除无用的对象不是垃圾回 收器的工作吗？

遗憾的是，事情没有这样简单。垃圾回收器跟踪活动的对象。只要映射对象是活动的， 其中的所有桶也是活动的， 它们不能被回收。因此，需要由程序负责从长期存活的映射表中 删除那些无用的值。 或者使用 WeakHashMap 完成这件事情。当对键的唯一引用来自散列条
目时， 这一数据结构将与垃圾回收器协同工作一起删除键 / 值对。

下面是这种机制的内部运行情况。WeakHashMap 使用弱引用（weak references) 保存键。 WeakReference 对象将引用保存到另外一个对象中，在这里，就是散列键。对于这种类型的 对象，垃圾回收器用一种特有的方式进行处理。通常，如果垃圾回收器发现某个特定的对象 已经没有他人引用了，就将其回收。然而， 如果某个对象只能由 WeakReference 引用， 垃圾 回收器仍然回收它，但要将引用这个对象的弱引用放人队列中。WeakHashMap 将周期性地检 查队列， 以便找出新添加的弱引用。一个弱引用进人队列意味着这个键不再被他人使用， 并 且已经被收集起来。于是， WeakHashMap 将删除对应的条目。

除了 WeakHashMap 使用了弱引用，ThreadLocal 类中也是用了弱引用。

虚引用
一个对象是否有虚引用的存在，完全不会对其生存时间构成影响，也无法通过虚引用来获取一个对象的实例。为一个对象设置虚引用关联的唯一目的就是能在这个对象被收集器回收时收到一个系统通知。虚引用和弱引用对关联对象的回收都不会产生影响，如果只有虚引用活着弱引用关联着对象，那么这个对象就会被回收。它们的不同之处在于弱引用的 get 方法，虚引用的 get 方法始终返回 null,弱引用可以使用 ReferenceQueue,虚引用必须配合 ReferenceQueue 使用。

jdk 中直接内存的回收就用到虚引用，由于 jvm 自动内存管理的范围是堆内存，而直接内存是在堆内存之外（其实是内存映射文件，自行去理解虚拟内存空间的相关概念），所以直接内存的分配和回收都是有 Unsafe 类去操作，java 在申请一块直接内存之后，会在堆内存分配一个对象保存这个堆外内存的引用，这个对象被垃圾收集器管理，一旦这个对象被回收，相应的用户线程会收到通知并对直接内存进行清理工作。
