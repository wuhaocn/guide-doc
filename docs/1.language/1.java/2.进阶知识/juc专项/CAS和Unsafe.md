# JUC 源码分析—CAS 和 Unsafe

[目录](readme.md)
[参考](https://www.jianshu.com/p/a897c4b8929f)

在对 J.U.C 包的源码分析之前，首先介绍下一个比较重要的概念-CAS（Compare-and-Swap）。
在 J.U.C 包中大量使用了 CAS，涉及并发或资源争用的地方都使用了 sun.misc.Unsafe 类的方法进行 CAS 操作。

## CAS

在针对多处理器操作而设计的处理器中提供了一些特殊指令，用于管理共享数据的并发访问。
现在，几乎所有的现代处理器中都包含了某种形式的原子读-改-写指令，
例如:比较并交换（Compare-and-Swap）或者关联加载/条件存储（Load-Linked/Store-Conditional）。
操作系统和 JVM 可以使用这些指令来实现锁和并发的数据结构。

在 JAVA 中,CAS 通过调用 C++库实现，由 C++库再去调用 CPU 指令集。不同体系结构中，cpu 指令还存在着明显不同。
x86 CPU 提供 cmpxchg 指令；而在精简指令集的体系架构中，（如“load and reserve”和“store conditional”）实现的，
在大多数处理器上 CAS 都是个非常轻量级的操作，这也是其优势所在。

**CAS,compare and swap 比较并替换。 CAS 有三个参数：需要读写的内存位值（V）、进行比较的预期原值（A）和拟写入的新值(B)。
当且仅当 V 的值等于 A 时，CAS 才会通过原子方式用新值 B 来更新 V 的值，否则不会执行任何操作。**

简单来说，CAS 的含义是：“我认为 V 的值应该是 A，如果是，那么将 V 的值更新为 B，否则不修改并告诉 V 的值实际为多少”。
CAS 是一项乐观的技术，它希望能成功地执行更新操作，并且如果有另一个线程在最近一次检查后更新了该变量，那么 CAS 能检测到这个错误。
当多个线程尝试使用 CAS 同时更新同一个变量时，只有其中一个线程能更新变量的值，而其他线程都将失败。
但是，失败的线程并不会被挂起（这就是与获取锁的机制不同之处），而是被告知在这次竞争中失败，并可以多次尝试。
这种灵活性就大大减少了与锁相关的活跃性风险。

以 AtomicInteger 为例，内部的 CAS 实现如下：

```java
public class AtomicInteger extends Number implements java.io.Serializable {
    private static final long serialVersionUID = 6214790243416807050L;
    // setup to use Unsafe.compareAndSwapInt for updates
    private static final Unsafe unsafe = Unsafe.getUnsafe(); //value的偏移地址
    private static final long valueOffset;
    static {
        try {
            valueOffset = unsafe.objectFieldOffset (AtomicInteger.class.getDeclaredField("value"));
        } catch (Exception ex) {
            throw new Error(ex);
        }
    }
    private volatile int value;
    public AtomicInteger(int initialValue) {
        value = initialValue;
    }
    public final int getAndUpdate(IntUnaryOperator updateFunction) {
        int prev, next;
        do {
                prev = get();
                next = updateFunction.applyAsInt(prev);
            } while (!compareAndSet(prev, next));
            return prev;
        }
    public final boolean compareAndSet(int expect, int update) {
        return unsafe.compareAndSwapInt(this, valueOffset, expect, update);
    }
}
```

**说明**：
可以看到 AtomicInteger 内部都是使用了 Unsafe 类来进行 CAS 操作，

- valueOffset :表示的是 value 值的偏移地址，因为 Unsafe 就是根据内存偏移地址获取数据的原值的, 偏移量可以简单理解为指针指向该变量的内存地址。

- value :使用 volatile 修饰，直接从共享内存中操作变量，保证多线程之间看到的 value 值是同一份。

以方法 getAndUpdate()为例，执行步骤如下：

- 1. 从内存中读取修改前的值 prev，并执行给定函数式计算修改后的值 next；
- 2. 调用 compareAndSet 修改 value 值（内部是调用了 unsafe 的 compareAndSwapInt 方法）。
     如果此时有其他线程也在修改这个 value 值，那么 CAS 操作就会失败，继续进入 do 循环重新获取新值，再次执行 CAS 直到修改成功。

### ABA 问题

ABA 问题是一种异常现象：如果算法中的节点可以被循环利用，那么在使用“比较并替换”指令时就可能出现这种问题（主要在没有垃圾回收机制的环境中）。

如果有两个线程 x 和 y，如果 x 初次从内存中读取变量值为 A；
线程 y 对它进行了一些操作使其变成 B，然后再改回 A，那么线程 x 进行 CAS 的时候就会误认为这个值没有被修改过。
尽管 CAS 操作会成功执行，但是不代表它是没有问题的，
如果有一个单向链表 A B 组成的栈，栈顶为 A，线程 T1 准备执行 CAS 操作 head.compareAndSet(A,B)，
在执行之前线程 T2 介入，T2 将 A、B 出栈，然后又把 C、A 放入栈，T2 执行完毕；
切回线程 T1，T1 发现栈顶元素依然为 A，也会成功执行 CAS 将栈顶元素修改为 B，但因为 B.next 为 null，所以栈结构就会丢弃 C 元素。

针对这种情况，有一种简单的解决方案：不是更新某个引用的值，而是更新两个值，包括一个引用和一个和版本号，即这个值由 A 变为 B，然后又变成 A，版本号也将是不同的。
Java 中提供了 AtomicStampedReference 和 AtomicMarkableReference 来解决 ABA 问题。他们支持在两个变量上执行原子的条件更新。

- AtomicStampedReference
  将更新一个“对象-引用”二元组，通过在引用上加上“版本号”，从而避免 ABA 问题。 类似地，

- AtomicMarkableReference
  将更新一个“对象引用-布尔值”二元组，在某些算法中将通过这种二元组使节点保存在链表中同时又将其标记为“已删除的节点”。

** 不过目前来说，这两个类比较鸡肋，大部分情况下的 ABA 问题不会影响程序并发的正确性，如果需要解决 ABA 问题，改用传统的互斥同步可能会比原子类更高效。**

### CAS 的缺点有以下几个方面：

    1.ABA问题
        如果某个线程在CAS操作时发现，内存值和预期值都是A，就能确定期间没有线程对值进行修改吗？
        答案未必，如果期间发生了 A -> B -> A 的更新，仅仅判断数值是 A，可能导致不合理的修改操作。针对这种情况，
        Java 提供了 AtomicStampedReference 工具类，通过为引用建立类似版本号（stamp）的方式，来保证 CAS 的正确性。
    2.循环时间长开销大
        CAS中使用的失败重试机制，隐藏着一个假设，即竞争情况是短暂的。大多数应用场景中，
        确实大部分重试只会发生一次就获得了成功。但是总有意外情况，所以在有需要的时候，还是要考虑限制自旋的次数，
        以免过度消耗 CPU。
    3.只能保证一个共享变量的原子操作

## Unsafe

Unsafe 是实现 CAS 的核心类，Java 无法直接访问底层操作系统，而是通过本地（native）方法来访问。Unsafe 类提供了硬件级别的原子操作。

### Unsafe 函数列表

```java
///--------------------- peek and poke 指令--------------
//获取对象o中给定偏移地址(offset)的值。以下相关get方法作用相同
public native int getInt(Object o, long offset);
//在对象o的给定偏移地址存储数值x。以下set方法作用相同
public native void putInt(Object o, long offset, int x);
public native Object getObject(Object o, long offset);
public native void putObject(Object o, long offset, Object x);
/**篇幅原因，省略其他类型方法 */
//从给定内存地址获取一个byte。下同
public native byte    getByte(long address);
//在给定内存地址放置一个x。下同
public native void    putByte(long address, byte x);
/**篇幅原因，省略其他类型方法*/
//获取给定内存地址的一个本地指针
public native long getAddress(long address);
//在给定的内存地址处存放一个本地指针x
public native void putAddress(long address, long x);

///------------------内存操作----------------------
//在本地内存分配一块指定大小的新内存，内存的内容未初始化;它们通常被当做垃圾回收。
public native long allocateMemory(long bytes);
//重新分配给定内存地址的本地内存
public native long reallocateMemory(long address, long bytes);
//将给定内存块中的所有字节设置为固定值（通常是0）
public native void setMemory(Object o, long offset, long bytes, byte value);
//复制一块内存，double-register模型
public native void copyMemory(Object srcBase, long srcOffset,
                              Object destBase, long destOffset,
                              long bytes);
//复制一块内存，single-register模型
public void copyMemory(long srcAddress, long destAddress, long bytes) {
    copyMemory(null, srcAddress, null, destAddress, bytes);
}
//释放给定地址的内存
public native void freeMemory(long address);
//获取给定对象的偏移地址
public native long staticFieldOffset(Field f);
public native long objectFieldOffset(Field f);

//------------------数组操作---------------------------------
//获取给定数组的第一个元素的偏移地址
public native int arrayBaseOffset(Class<?> arrayClass);
//获取给定数组的元素增量地址，也就是说每个元素的占位数
public native int arrayIndexScale(Class<?> arrayClass);

//------------------------------------------------------------
//告诉虚拟机去定义一个类。默认情况下，类加载器和保护域都来自这个方法
public native Class<?> defineClass(String name, byte[] b, int off, int len,
                                   ClassLoader loader,
                                   ProtectionDomain protectionDomain);
//定义匿名内部类
public native Class<?> defineAnonymousClass(Class<?> hostClass, byte[] data, Object[] cpPatches);
//定位一个实例，但不运行构造函数
public native Object allocateInstance(Class<?> cls) throws InstantiationException;

///--------------------锁指令（synchronized）-------------------------------
//对象加锁
public native void monitorEnter(Object o);
//对象解锁
public native void monitorExit(Object o);
public native boolean tryMonitorEnter(Object o);
//解除给定线程的阻塞
public native void unpark(Object thread);
//阻塞当前线程
public native void park(boolean isAbsolute, long time);

// CAS
public final native boolean compareAndSwapObject(Object o, long offset,
                                                 Object expected,
                                                 Object x);
//获取对象o的给定偏移地址的引用值（volatile方式）
public native Object getObjectVolatile(Object o, long offset);
public native void    putObjectVolatile(Object o, long offset, Object x);
/** 省略其他类型方法  */


//用于lazySet，适用于低延迟代码。
public native void    putOrderedObject(Object o, long offset, Object x);
/** 省略其他类型方法  */
//获取并加上给定delta，返回加之前的值
public final int getAndAddInt(Object o, long offset, int delta)
/** 省略其他类型方法  */
//为给定偏移地址设置一个新的值，返回设置之前的值
public final int getAndSetInt(Object o, long offset, int newValue)
/** 省略其他类型方法  */

///--------------------1.8新增指令-----------------------
// loadFence() 表示该方法之前的所有load操作在内存屏障之前完成
public native void loadFence();
//表示该方法之前的所有store操作在内存屏障之前完成
public native void storeFence();
//表示该方法之前的所有load、store操作在内存屏障之前完成，这个相当于上面两个的合体功能
public native void fullFence();
```

Unsafe 的方法比较简单，直接看方法字面意思就大概知道方法的作用。
在 Unsafe 里有两个方法模型：
**double-register 模型**：给定对象，给定偏移地址 offset。从给定对象的偏移地址取值。如 getInt(Object o, long offset)；
**single-register 模型**：给定内存地址，直接从给定内存地址取值，如 getInt(long)。

这里介绍一下几个比较重要的方法，在之后的源码阅读里会用到。

- 1. arrayBaseOffset：操作数组，用于获取数组的第一个元素的偏移地址
- 2. arrayIndexScale：操作数组，用于获取数组元素的增量地址，也就是说每个元素的占位数。
     打个栗子：如果有一个数组{1,2,3,4,5,6}，它第一个元素的偏移地址为 16，每个元素的占位是 4，如果我们要获取数组中“5”这个数字，那么它的偏移地址就是 16+4/\*4。
- 3. putOrderedObject：putOrderedObject 是 lazySet 的实现，适用于低延迟代码。它能够实现非堵塞写入，避免指令重排序，
     这样它使用快速的存储-存储(store-store) barrier,而不是较慢的存储-加载(store-load) barrier, 后者多是用在 volatile 的写操作上。
     但这种性能提升也是有代价的，也就是写后结果并不会被其他线程（甚至是自己的线程）看到，通常是几纳秒后被其他线程看到。
     类似的方法还有 putOrderedInt、putOrderedLong。loadFence、storeFence、fullFence：这三个方法是 1.8 新增，主要针对内存屏障定义，也是为了避免重排序：

- loadFence() 表示该方法之前的所有 load 操作在内存屏障之前完成。
- storeFence()表示该方法之前的所有 store 操作在内存屏障之前完成。
- fullFence()表示该方法之前的所有 load、store 操作在内存屏障之前完成。

[目录](readme.md)
