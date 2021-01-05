### 问题

（1）java8 中为什么要新增 LongAdder？

（2）LongAdder 的实现方式？

（3）LongAdder 与 AtomicLong 的对比？

### 简介

LongAdder 是 java8 中新增的原子类，在多线程环境中，它比 AtomicLong 性能要高出不少，特别是写多的场景。

它是怎么实现的呢？让我们一起来学习吧。

### 原理

LongAdder 的原理是，在最初无竞争时，只更新 base 的值，当有多线程竞争时通过分段的思想，让不同的线程更新不同的段，最后把这些段相加就得到了完整的 LongAdder 存储的值。

LongAdder

### 源码分析

LongAdder 继承自 Striped64 抽象类，Striped64 中定义了 Cell 内部类和各重要属性。

#### 主要内部类

// Striped64 中的内部类，使用@sun.misc.Contended 注解，说明里面的值消除伪共享

```
@sun.misc.Contended static final class Cell {
    // 存储元素的值，使用volatile修饰保证可见性
    volatile long value;
    Cell(long x) { value = x; }
    // CAS更新value的值
    final boolean cas(long cmp, long val) {
        return UNSAFE.compareAndSwapLong(this, valueOffset, cmp, val);
    }

    // Unsafe实例
    private static final sun.misc.Unsafe UNSAFE;
    // value字段的偏移量
    private static final long valueOffset;
    static {
        try {
            UNSAFE = sun.misc.Unsafe.getUnsafe();
            Class<?> ak = Cell.class;
            valueOffset = UNSAFE.objectFieldOffset
                (ak.getDeclaredField("value"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
```

Cell 类使用[@sun.misc.Contended 注解](Java8的@sun.misc.Contended注解.md)，说明是要避免伪共享的。

使用 Unsafe 的 CAS 更新 value 的值，其中 value 的值使用 volatile 修饰，保证可见性。

关于 Unsafe 的介绍请查看【死磕 java 魔法类之 Unsafe 解析】。

关于伪共享的介绍请查看【杂谈 什么是伪共享（false sharing）？】。
[一篇对伪共享、缓存行填充和 CPU 缓存讲的很透彻的文章](https://blog.csdn.net/qq_27680317/article/details/78486220)

主要属性

```
// 这三个属性都在Striped64中
// cells数组，存储各个段的值
transient volatile Cell[] cells;
// 最初无竞争时使用的，也算一个特殊的段
transient volatile long base;
// 标记当前是否有线程在创建或扩容cells，或者在创建Cell
// 通过CAS更新该值，相当于是一个锁
transient volatile int cellsBusy;
```

最初无竞争或有其它线程在创建 cells 数组时使用 base 更新值，有过竞争时使用 cells 更新值。
最初无竞争是指一开始没有线程之间的竞争，但也有可能是多线程在操作，只是这些线程没有同时去更新 base 的值。
有过竞争是指只要出现过竞争不管后面有没有竞争都使用 cells 更新值，规则是不同的线程 hash 到不同的 cell 上去更新，减少竞争。

#### add(x)方法

add(x)方法是 LongAdder 的主要方法，使用它可以使 LongAdder 中存储的值增加 x，x 可为正可为负。

```
public void add(long x) {
    // as是Striped64中的cells属性
    // b是Striped64中的base属性
    // v是当前线程hash到的Cell中存储的值
    // m是cells的长度减1，hash时作为掩码使用
    // a是当前线程hash到的Cell
    Cell[] as; long b, v; int m; Cell a;
    // 条件1：cells不为空，说明出现过竞争，cells已经创建
    // 条件2：cas操作base失败，说明其它线程先一步修改了base，正在出现竞争
    if ((as = cells) != null || !casBase(b = base, b + x)) {
        // true表示当前竞争还不激烈
        // false表示竞争激烈，多个线程hash到同一个Cell，可能要扩容
        boolean uncontended = true;
        // 条件1：cells为空，说明正在出现竞争，上面是从条件2过来的
        // 条件2：应该不会出现
        // 条件3：当前线程所在的Cell为空，说明当前线程还没有更新过Cell，应初始化一个Cell
        // 条件4：更新当前线程所在的Cell失败，说明现在竞争很激烈，多个线程hash到了同一个Cell，应扩容
        if (as == null || (m = as.length - 1) < 0 ||
            // getProbe()方法返回的是线程中的threadLocalRandomProbe字段
            // 它是通过随机数生成的一个值，对于一个确定的线程这个值是固定的
            // 除非刻意修改它
            (a = as[getProbe() & m]) == null ||
            !(uncontended = a.cas(v = a.value, v + x)))
            // 调用Striped64中的方法处理
            longAccumulate(x, null, uncontended);
    }
}
```

\*（1）最初无竞争时只更新 base；

\*（2）直到更新 base 失败时，创建 cells 数组；

\*（3）当多个线程竞争同一个 Cell 比较激烈时，可能要扩容；

#### longAccumulate()方法

```
final void longAccumulate(long x, LongBinaryOperator fn,
                              boolean wasUncontended) {
    // 存储线程的probe值
    int h;
    // 如果getProbe()方法返回0，说明随机数未初始化
    if ((h = getProbe()) == 0) {
        // 强制初始化
        ThreadLocalRandom.current(); // force initialization
        // 重新获取probe值
        h = getProbe();
        // 都未初始化，肯定还不存在竞争激烈
        wasUncontended = true;
    }
    // 是否发生碰撞
    boolean collide = false;                // True if last slot nonempty
    for (;;) {
        Cell[] as; Cell a; int n; long v;
        // cells已经初始化过
        if ((as = cells) != null && (n = as.length) > 0) {
            // 当前线程所在的Cell未初始化
            if ((a = as[(n - 1) & h]) == null) {
                // 当前无其它线程在创建或扩容cells，也没有线程在创建Cell
                if (cellsBusy == 0) {       // Try to attach new Cell
                    // 新建一个Cell，值为当前需要增加的值
                    Cell r = new Cell(x);   // Optimistically create
                    // 再次检测cellsBusy，并尝试更新它为1
                    // 相当于当前线程加锁
                    if (cellsBusy == 0 && casCellsBusy()) {
                        // 是否创建成功
                        boolean created = false;
                        try {               // Recheck under lock
                            Cell[] rs; int m, j;
                            // 重新获取cells，并找到当前线程hash到cells数组中的位置
                            // 这里一定要重新获取cells，因为as并不在锁定范围内
                            // 有可能已经扩容了，这里要重新获取
                            if ((rs = cells) != null &&
                                (m = rs.length) > 0 &&
                                rs[j = (m - 1) & h] == null) {
                                // 把上面新建的Cell放在cells的j位置处
                                rs[j] = r;
                                // 创建成功
                                created = true;
                            }
                        } finally {
                            // 相当于释放锁
                            cellsBusy = 0;
                        }
                        // 创建成功了就返回
                        // 值已经放在新建的Cell里面了
                        if (created)
                            break;
                        continue;           // Slot is now non-empty
                    }
                }
                // 标记当前未出现冲突
                collide = false;
            }
            // 当前线程所在的Cell不为空，且更新失败了
            // 这里简单地设为true，相当于简单地自旋一次
            // 通过下面的语句修改线程的probe再重新尝试
            else if (!wasUncontended)       // CAS already known to fail
                wasUncontended = true;      // Continue after rehash
            // 再次尝试CAS更新当前线程所在Cell的值，如果成功了就返回
            else if (a.cas(v = a.value, ((fn == null) ? v + x :
                                         fn.applyAsLong(v, x))))
                break;
            // 如果cells数组的长度达到了CPU核心数，或者cells扩容了
            // 设置collide为false并通过下面的语句修改线程的probe再重新尝试
            else if (n >= NCPU || cells != as)
                collide = false;            // At max size or stale
            // 上上个elseif都更新失败了，且上个条件不成立，说明出现冲突了
            else if (!collide)
                collide = true;
            // 明确出现冲突了，尝试占有锁，并扩容
            else if (cellsBusy == 0 && casCellsBusy()) {
                try {
                    // 检查是否有其它线程已经扩容过了
                    if (cells == as) {      // Expand table unless stale
                        // 新数组为原数组的两倍
                        Cell[] rs = new Cell[n << 1];
                        // 把旧数组元素拷贝到新数组中
                        for (int i = 0; i < n; ++i)
                            rs[i] = as[i];
                        // 重新赋值cells为新数组
                        cells = rs;
                    }
                } finally {
                    // 释放锁
                    cellsBusy = 0;
                }
                // 已解决冲突
                collide = false;
                // 使用扩容后的新数组重新尝试
                continue;                   // Retry with expanded table
            }
            // 更新失败或者达到了CPU核心数，重新生成probe，并重试
            h = advanceProbe(h);
        }
        // 未初始化过cells数组，尝试占有锁并初始化cells数组
        else if (cellsBusy == 0 && cells == as && casCellsBusy()) {
            // 是否初始化成功
            boolean init = false;
            try {                           // Initialize table
                // 检测是否有其它线程初始化过
                if (cells == as) {
                    // 新建一个大小为2的Cell数组
                    Cell[] rs = new Cell[2];
                    // 找到当前线程hash到数组中的位置并创建其对应的Cell
                    rs[h & 1] = new Cell(x);
                    // 赋值给cells数组
                    cells = rs;
                    // 初始化成功
                    init = true;
                }
            } finally {
                // 释放锁
                cellsBusy = 0;
            }
            // 初始化成功直接返回
            // 因为增加的值已经同时创建到Cell中了
            if (init)
                break;
        }
        // 如果有其它线程在初始化cells数组中，就尝试更新base
        // 如果成功了就返回
        else if (casBase(v = base, ((fn == null) ? v + x :
                                    fn.applyAsLong(v, x))))
            break;                          // Fall back on using base
    }
}
```

\*（1）如果 cells 数组未初始化，当前线程会尝试占有 cellsBusy 锁并创建 cells 数组；

\*（2）如果当前线程尝试创建 cells 数组时，发现有其它线程已经在创建了，就尝试更新 base，如果成功就返回；

\*（3）通过线程的 probe 值找到当前线程应该更新 cells 数组中的哪个 Cell；

\*（4）如果当前线程所在的 Cell 未初始化，就占有占有 cellsBusy 锁并在相应的位置创建一个 Cell；

\*（5）尝试 CAS 更新当前线程所在的 Cell，如果成功就返回，如果失败说明出现冲突；

\*（5）当前线程更新 Cell 失败后并不是立即扩容，而是尝试更新 probe 值后再重试一次；

\*（6）如果在重试的时候还是更新失败，就扩容；

\*（7）扩容时当前线程占有 cellsBusy 锁，并把数组容量扩大到两倍，再迁移原 cells 数组中元素到新数组中；

\*（8）cellsBusy 在创建 cells 数组、创建 Cell、扩容 cells 数组三个地方用到；

#### sum()方法

sum()方法是获取 LongAdder 中真正存储的值的大小，通过把 base 和所有段相加得到。

```
public long sum() {
    Cell[] as = cells; Cell a;
    // sum初始等于base
    long sum = base;
    // 如果cells不为空
    if (as != null) {
        // 遍历所有的Cell
        for (int i = 0; i < as.length; ++i) {
            // 如果所在的Cell不为空，就把它的value累加到sum中
            if ((a = as[i]) != null)
                sum += a.value;
        }
    }
    // 返回sum
    return sum;
}
```

可以看到 sum()方法是把 base 和所有段的值相加得到，那么，这里有一个问题，如果前面已经累加到 sum 上的 Cell 的 value 有修改，不是就没法计算到了么？

答案确实如此，所以 LongAdder 可以说不是强一致性的，它是最终一致性的。

#### LongAdder VS AtomicLong

直接上代码：

```
public class LongAdderVSAtomicLongTest {
    public static void main(String[] args){
        testAtomicLongVSLongAdder(1, 10000000);
        testAtomicLongVSLongAdder(10, 10000000);
        testAtomicLongVSLongAdder(20, 10000000);
        testAtomicLongVSLongAdder(40, 10000000);
        testAtomicLongVSLongAdder(80, 10000000);
    }

    static void testAtomicLongVSLongAdder(final int threadCount, final int times){
        try {
            System.out.println("threadCount：" + threadCount + ", times：" + times);
            long start = System.currentTimeMillis();
            testLongAdder(threadCount, times);
            System.out.println("LongAdder elapse：" + (System.currentTimeMillis() - start) + "ms");

            long start2 = System.currentTimeMillis();
            testAtomicLong(threadCount, times);
            System.out.println("AtomicLong elapse：" + (System.currentTimeMillis() - start2) + "ms");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static void testAtomicLong(final int threadCount, final int times) throws InterruptedException {
        AtomicLong atomicLong = new AtomicLong();
        List<Thread> list = new ArrayList<>();
        for (int i=0;i<threadCount;i++){
            list.add(new Thread(() -> {
                for (int j = 0; j<times; j++){
                    atomicLong.incrementAndGet();
                }
            }));
        }

        for (Thread thread : list){
            thread.start();
        }

        for (Thread thread : list){
            thread.join();
        }
    }

    static void testLongAdder(final int threadCount, final int times) throws InterruptedException {
        LongAdder longAdder = new LongAdder();
        List<Thread> list = new ArrayList<>();
        for (int i=0;i<threadCount;i++){
            list.add(new Thread(() -> {
                for (int j = 0; j<times; j++){
                    longAdder.add(1);
                }
            }));
        }

        for (Thread thread : list){
            thread.start();
        }

        for (Thread thread : list){
            thread.join();
        }
    }
}
```

运行结果如下：

```
threadCount：1, times：10000000
LongAdder elapse：158ms
AtomicLong elapse：64ms
threadCount：10, times：10000000
LongAdder elapse：206ms
AtomicLong elapse：2449ms
threadCount：20, times：10000000
LongAdder elapse：429ms
AtomicLong elapse：5142ms
threadCount：40, times：10000000
LongAdder elapse：840ms
AtomicLong elapse：10506ms
threadCount：80, times：10000000
LongAdder elapse：1369ms
AtomicLong elapse：20482ms
```

可以看到当只有一个线程的时候，AtomicLong 反而性能更高，随着线程越来越多，AtomicLong 的性能急剧下降，而 LongAdder 的性能影响很小。

### 总结

\*（1）LongAdder 通过 base 和 cells 数组来存储值；

\*（2）不同的线程会 hash 到不同的 cell 上去更新，减少了竞争；

\*（3）LongAdder 的性能非常高，最终会达到一种无竞争的状态；

在 longAccumulate()方法中有个条件是 n >= NCPU 就不会走到扩容逻辑了，而 n 是 2 的倍数，那是不是代表 cells 数组最大只能达到大于等于 NCPU 的最小 2 次方？
答案是明确的。因为同一个 CPU 核心同时只会运行一个线程，而更新失败了说明有两个不同的核心更新了同一个 Cell，
这时会重新设置更新失败的那个线程的 probe 值，这样下一次它所在的 Cell 很大概率会发生改变，如果运行的时间足够长，
最终会出现同一个核心的所有线程都会 hash 到同一个 Cell（大概率，但不一定全在一个 Cell 上）上去更新，
所以，这里 cells 数组中长度并不需要太长，达到 CPU 核心数足够了。
比如，笔者的电脑是 8 核的，所以这里 cells 的数组最大只会到 8，达到 8 就不会扩容了。
