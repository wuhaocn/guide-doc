# 精尽 Netty 源码解析 —— Buffer 之 Jemalloc（五）PoolArena

# []( "1. 概述")1. 概述

在应用程序里，我们可以使用 PooledByteBufAllocator 来创建 ByteBuf 对象。代码如下：

```
PooledByteBufAllocator.DEFAULT.buffer(1024);
```

- 在方法的内部实现，通过 PoolArena 来进行内存分配。

下面，就让我们来看看 PoolArena 具体的代码实现。
FROM [《自顶向下深入分析 Netty（十）–JEMalloc 分配算法》](https://www.jianshu.com/p/15304cd63175)

为了提高内存分配效率并减少内部碎片，Jemalloc 算法将 Arena 切分为小块 Chunk，根据每块的内存使用率又将小块组合为以下几种状态：QINIT、Q00、Q25、Q50、Q75、Q100 。Chunk 块可以在这几种状态间随着内存使用率的变化进行转移，从而提高分配效率。

# []( "2. PoolArena")2. PoolArena

io.netty.buffer.PoolArena
，实现 PoolArenaMetric 接口，Netty 对 Jemalloc Arena 的抽象实现类。

PoolArena 有两个子类实现：

- HeapArena ，对 Heap 类型的内存分配。
- DirectArena ，对 Direct 类型的内存分配。

## []( "2.1 构造方法")2.1 构造方法

```
//*/*
/* 是否支持 Unsafe 操作
/*/
static final boolean HAS_UNSAFE = PlatformDependent.hasUnsafe();
//*/*
/* 内存分类
/*/
enum SizeClass {
Tiny,
Small,
Normal
// 还有一个隐藏的，Huge
}
//*/*
/* {@link /#tinySubpagePools} 数组的大小
/*
/* 默认为 32
/*/
static final int numTinySubpagePools = 512 >>> 4;
//*/*
/* 所属 PooledByteBufAllocator 对象
/*/
final PooledByteBufAllocator parent;
//*/*
/* 满二叉树的高度。默认为 11 。
/*/
private final int maxOrder;
//*/*
/* Page 大小，默认 8KB = 8192B
/*/
final int pageSize;
//*/*
/* 从 1 开始左移到 {@link /#pageSize} 的位数。默认 13 ，1 << 13 = 8192 。
/*/
final int pageShifts;
//*/*
/* Chunk 内存块占用大小。默认为 16M = 16 /* 1024 。
/*/
final int chunkSize;
//*/*
/* 判断分配请求内存是否为 Tiny/Small ，即分配 Subpage 内存块。
/*
/* Used to determine if the requested capacity is equal to or greater than pageSize.
/*/
final int subpageOverflowMask;
//*/*
/* {@link /#smallSubpagePools} 数组的大小
/*
/* 默认为 23
/*/
final int numSmallSubpagePools;
//*/*
/* 对齐基准
/*/
final int directMemoryCacheAlignment;
//*/*
/* {@link /#directMemoryCacheAlignment} 掩码
/*/
final int directMemoryCacheAlignmentMask;
//*/*
/* tiny 类型的 PoolSubpage 数组
/*
/* 数组的每个元素，都是双向链表
/*/
private final PoolSubpage<T>[] tinySubpagePools;
//*/*
/* small 类型的 SubpagePools 数组
/*
/* 数组的每个元素，都是双向链表
/*/
private final PoolSubpage<T>[] smallSubpagePools;
// PoolChunkList 之间的双向链表
private final PoolChunkList<T> q050;
private final PoolChunkList<T> q025;
private final PoolChunkList<T> q000;
private final PoolChunkList<T> qInit;
private final PoolChunkList<T> q075;
private final PoolChunkList<T> q100;
//*/*
/* PoolChunkListMetric 数组
/*/
private final List<PoolChunkListMetric> chunkListMetrics;
// Metrics for allocations and deallocations
//*/*
/* 分配 Normal 内存块的次数
/*/
private long allocationsNormal;
// We need to use the LongCounter here as this is not guarded via synchronized block.
//*/*
/* 分配 Tiny 内存块的次数
/*/
private final LongCounter allocationsTiny = PlatformDependent.newLongCounter();
//*/*
/* 分配 Small 内存块的次数
/*/
private final LongCounter allocationsSmall = PlatformDependent.newLongCounter();
//*/*
/* 分配 Huge 内存块的次数
/*/
private final LongCounter allocationsHuge = PlatformDependent.newLongCounter();
//*/*
/* 正在使用中的 Huge 内存块的总共占用字节数
/*/
private final LongCounter activeBytesHuge = PlatformDependent.newLongCounter();
//*/*
/* 释放 Tiny 内存块的次数
/*/
private long deallocationsTiny;
//*/*
/* 释放 Small 内存块的次数
/*/
private long deallocationsSmall;
//*/*
/* 释放 Normal 内存块的次数
/*/
private long deallocationsNormal;
//*/*
/* 释放 Huge 内存块的次数
/*/
// We need to use the LongCounter here as this is not guarded via synchronized block.
private final LongCounter deallocationsHuge = PlatformDependent.newLongCounter();
//*/*
/* 该 PoolArena 被多少线程引用的计数器
/*/
// Number of thread caches backed by this arena.
final AtomicInteger numThreadCaches = new AtomicInteger();
1: protected PoolArena(PooledByteBufAllocator parent, int pageSize,
2: int maxOrder, int pageShifts, int chunkSize, int cacheAlignment){
3: this.parent = parent;
4: this.pageSize = pageSize;
5: this.maxOrder = maxOrder;
6: this.pageShifts = pageShifts;
7: this.chunkSize = chunkSize;
8: directMemoryCacheAlignment = cacheAlignment;
9: directMemoryCacheAlignmentMask = cacheAlignment - 1;
10: subpageOverflowMask = ~(pageSize - 1);
11:
12: // 初始化 tinySubpagePools 数组
13: tinySubpagePools = newSubpagePoolArray(numTinySubpagePools);
14: for (int i = 0; i < tinySubpagePools.length; i ++) {
15: tinySubpagePools[i] = newSubpagePoolHead(pageSize);
16: }
17:
18: // 初始化 smallSubpagePools 数组
19: numSmallSubpagePools = pageShifts - 9;
20: smallSubpagePools = newSubpagePoolArray(numSmallSubpagePools);
21: for (int i = 0; i < smallSubpagePools.length; i ++) {
22: smallSubpagePools[i] = newSubpagePoolHead(pageSize);
23: }
24:
25: // PoolChunkList 之间的双向链表，初始化
26:
27: q100 = new PoolChunkList<T>(this, null, 100, Integer.MAX_VALUE, chunkSize);
28: q075 = new PoolChunkList<T>(this, q100, 75, 100, chunkSize);
29: q050 = new PoolChunkList<T>(this, q075, 50, 100, chunkSize);
30: q025 = new PoolChunkList<T>(this, q050, 25, 75, chunkSize);
31: q000 = new PoolChunkList<T>(this, q025, 1, 50, chunkSize);
32: qInit = new PoolChunkList<T>(this, q000, Integer.MIN_VALUE, 25, chunkSize);
33:
34: q100.prevList(q075);
35: q075.prevList(q050);
36: q050.prevList(q025);
37: q025.prevList(q000);
38: q000.prevList(null); // 无前置节点
39: qInit.prevList(qInit); // 前置节点为自己
40:
41: // 创建 PoolChunkListMetric 数组
42: List<PoolChunkListMetric> metrics = new ArrayList<PoolChunkListMetric>(6);
43: metrics.add(qInit);
44: metrics.add(q000);
45: metrics.add(q025);
46: metrics.add(q050);
47: metrics.add(q075);
48: metrics.add(q100);
49: chunkListMetrics = Collections.unmodifiableList(metrics);
50: }
```

- 虽然属性比较多，但是内部主要还是以 PoolSubpage 和 PoolChunkList 对象为主。如下图所示：
  FROM [《深入浅出 Netty 内存管理 PoolArena》](https://www.jianshu.com/p/4856bd30dd56)

[![大体结构](http://static2.iocoder.cn/images/Netty/2018_09_13/01.png)](http://static2.iocoder.cn/images/Netty/2018_09_13/01.png '大体结构')大体结构

- SizeClass **枚举类**，内存分类，一共有四种：Tiny、Small、Normal、Huge 。如下图所示：[![内存分配](http://static2.iocoder.cn/images/Netty/2018_09_13/02.png)](http://static2.iocoder.cn/images/Netty/2018_09_13/02.png '内存分配')内存分配
- parent
  属性，所属 PooledByteBufAllocator 对象。
- 内存属性相关

- HAS_UNSAFE
  **静态**属性，是否支持 Unsafe 操作。
- directMemoryCacheAlignment
  属性，对齐基准，默认为 0 。😈 实际可以忽略哈。
- directMemoryCacheAlignmentMask
  属性，

directMemoryCacheAlignment
的掩码，默认为 -1( 【第 9 行】的代码

directMemoryCacheAlignment - 1
)。😈 实际可以忽略哈。

- PoolChunk 属性相关，

maxOrder
、

pageSize
、

pageShifts
、

chunkSize
、

subpageOverflowMask
。已经在 [《精尽 Netty 源码解析 —— Buffer 之 Jemalloc（二）PoolChunk》](http://svip.iocoder.cn/Netty/ByteBuf-3-2-Jemalloc-chunk) 。

- PoolSubpage 属性相关

- tinySubpagePools
  属性，tiny 类型的 PoolSubpage 数组。数组的每个元素，都是**双向链表**。

- 在【第 12 至 16 行】的代码，进行初始化。
- numTinySubpagePools
  **静态**属性，数组大小。默认为 32 。
- smallSubpagePools
  属性，small 类型的 SubpagePools 数组。数组的每个元素，都是**双向链表**。

- 在【第 18 至 23 行】的代码，进行初始化。
- numSmallSubpagePools
  属性，数组大小。默认为 23(

numTinySubpagePools - 9
)。

- PoolChunkList 属性相关，

qInit
、

q025
、

q050
、

q075
、

q100
、

chunkListMetrics
。已经在 [《精尽 Netty 源码解析 —— Buffer 之 Jemalloc（四）PoolChunkList》](http://svip.iocoder.cn/Netty/Netty/ByteBuf-3-4-Jemalloc-chunkList) 。

- PoolArenaMetric 属性相关

- 分配 _XXX_ 内存块的次数的属性：

allocationsNormal
、

allocationsTiny
、

allocationsSmall
、

allocationsHuge
。

- 释放 _XXX_ 内存块的次数的属性：

deallocationsTiny
、

deallocationsSmall
、

deallocationsNormal
、

deallocationsHuge
。

- ↑↑↑ 上述属性使用 LongCounter 还是

long
类型，主要是变量访问时，是否在

synchronized {}
代码块中访问，从而保证**内存的可见性**。

- activeBytesHuge
  属性，**正在使用中**的 Huge 内存块的总共占用字节数。
- numThreadCaches
  属性，该 PoolArena 被多少线程引用的计数器。
- 😈 构造方法，简单看看就好，基本上面都已经提到了。

## []( "2.2 容量相关方法")2.2 容量相关方法

### []( "2.2.1 normalizeCapacity")2.2.1 normalizeCapacity

/#normalizeCapacity(int reqCapacity)
方法，标准化请求分配的内存大小。通过这样的方式，**保证分配的内存块统一**。代码如下：

```
1: int normalizeCapacity(int reqCapacity){
2: if (reqCapacity < 0) {
3: throw new IllegalArgumentException("capacity: " + reqCapacity + " (expected: 0+)");
4: }
5:
6: // Huge 内存类型，直接使用 reqCapacity ，无需进行标准化。
7: if (reqCapacity >= chunkSize) {
8: return directMemoryCacheAlignment == 0 ? reqCapacity : alignCapacity(reqCapacity);
9: }
10:
11: // 非 tiny 内存类型
12: if (!isTiny(reqCapacity)) { // >= 512
13: // Doubled
14: // 转换成接近于两倍的容量
15: int normalizedCapacity = reqCapacity;
16: normalizedCapacity --;
17: normalizedCapacity |= normalizedCapacity >>> 1;
18: normalizedCapacity |= normalizedCapacity >>> 2;
19: normalizedCapacity |= normalizedCapacity >>> 4;
20: normalizedCapacity |= normalizedCapacity >>> 8;
21: normalizedCapacity |= normalizedCapacity >>> 16;
22: normalizedCapacity ++;
23: if (normalizedCapacity < 0) {
24: normalizedCapacity >>>= 1;
25: }
26: assert directMemoryCacheAlignment == 0 || (normalizedCapacity & directMemoryCacheAlignmentMask) == 0;
27:
28: return normalizedCapacity;
29: }
30:
31: if (directMemoryCacheAlignment > 0) {
32: return alignCapacity(reqCapacity);
33: }
34:
35: // 补齐成 16 的倍数
36: // Quantum-spaced
37: if ((reqCapacity & 15) == 0) {
38: return reqCapacity;
39: }
40: return (reqCapacity & ~15) + 16;
41: }
```

- 第 6 至 9 行：**Huge** 内存类型，直接使用 reqCapacity ，无需进行标准化。
- 第 11 至 29 行：**Small**、**Normal** 内存类型，转换成接近于**两倍**的容量。
- 第 35 至 40 行：**Tiny** 内存类型，补齐成 **16** 的倍数。

总结来说，还是下图：[![内存容量](http://static2.iocoder.cn/images/Netty/2018_09_13/03.png)](http://static2.iocoder.cn/images/Netty/2018_09_13/03.png '内存容量')内存容量

### []( "2.2.2 alignCapacity")2.2.2 alignCapacity

/#alignCapacity(int reqCapacity)
方法，对齐请求分配的内存大小。代码如下：

```
int alignCapacity(int reqCapacity){
// 获得 delta
int delta = reqCapacity & directMemoryCacheAlignmentMask;
// 补齐 directMemoryCacheAlignment ，并减去 delta
return delta == 0 ? reqCapacity : reqCapacity + directMemoryCacheAlignment - delta;
}
```

### []( "2.2.3 isTinyOrSmall")2.2.3 isTinyOrSmall

/#isTinyOrSmall(int normCapacity)
方法，判断请求分配的内存类型是否为 tiny 或 small 类型。代码如下：

```
// capacity < pageSize
boolean isTinyOrSmall(int normCapacity){
return (normCapacity & subpageOverflowMask) == 0;
}
```

### []( "2.2.4 isTiny")2.2.4 isTiny

/#isTiny(int normCapacity)
方法，判断请求分配的内存类型是否为 tiny 类型。代码如下：

```
// normCapacity < 512
static boolean isTiny(int normCapacity){
return (normCapacity & 0xFFFFFE00) == 0;
}
```

### []( "2.2.5 tinyIdx")2.2.5 tinyIdx

/#tinyIdx(int normCapacity)
**静态**方法，计算请求分配的内存大小在

tinySubpagePools
数组的下标。代码如下：

```
static int tinyIdx(int normCapacity){
return normCapacity >>> 4;
}
```

### []( "2.2.6 smallIdx")2.2.6 smallIdx

/#smallIdx(int normCapacity)
**静态**方法，计算请求分配的内存大小在

smallSubpagePools
数组的下标。代码如下：

```
static int smallIdx(int normCapacity){
int tableIdx = 0;
int i = normCapacity >>> 10;
while (i != 0) {
i >>>= 1;
tableIdx ++;
}
return tableIdx;
}
```

### []( "2.2.7 sizeClass")2.2.7 sizeClass

/#sizeClass(int normCapacity)
方法，计算请求分配的内存的内存类型。代码如下：

```
private SizeClass sizeClass(int normCapacity){
if (!isTinyOrSmall(normCapacity)) {
return SizeClass.Normal;
}
return isTiny(normCapacity) ? SizeClass.Tiny : SizeClass.Small;
}
```

## []( "2.3 findSubpagePoolHead")2.3 findSubpagePoolHead

/#findSubpagePoolHead(int elemSize)
方法，获得请求分配的 Subpage 类型的内存的链表的**头节点**。代码如下：

```
PoolSubpage<T> findSubpagePoolHead(int elemSize){
int tableIdx;
PoolSubpage<T>[] table;
if (isTiny(elemSize)) { // < 512
// 实际上，就是 `/#tinyIdx(int normCapacity)` 方法
tableIdx = elemSize >>> 4;
// 获得 table
table = tinySubpagePools;
} else {
// 实际上，就是 `/#smallIdx(int normCapacity)` 方法
tableIdx = 0;
elemSize >>>= 10;
while (elemSize != 0) {
elemSize >>>= 1;
tableIdx ++;
}
// 获得 table
table = smallSubpagePools;
}
// 获得 Subpage 链表的头节点
return table[tableIdx];
}
```

## []( "2.4 allocate")2.4 allocate

PoolArena 根据申请分配的内存大小不同，提供了**两种**方式分配内存：

- 1、PoolSubpage ，用于分配**小于**

8KB
的内存块

- 1.1

tinySubpagePools
属性，用于分配小于

512B
的 tiny **Subpage** 内存块。

- 1.2

smallSubpagePools
属性，用于分配小于

8KB
的 small **Subpage** 内存块。

- 2、PoolChunkList ，用于分配**大于等于**

8KB
的内存块

- 2.1 小于

32MB
，分配 normal 内存块，即一个 Chunk 中的 **Page** 内存块。

- 2.2 大于等于

32MB
，分配 huge 内存块，即一整个 **Chunk** 内存块。

/#allocate(PoolThreadCache cache, int reqCapacity, int maxCapacity)
方法，创建 PooledByteBuf 对象，并分配内存块给 PooledByteBuf 对象。代码如下：

```
1: private void allocate(PoolThreadCache cache, PooledByteBuf<T> buf, final int reqCapacity){
2: // 标准化请求分配的容量
3: final int normCapacity = normalizeCapacity(reqCapacity);
4: // PoolSubpage 的情况
5: if (isTinyOrSmall(normCapacity)) { // capacity < pageSize
6: int tableIdx;
7: PoolSubpage<T>[] table;
8: // 判断是否为 tiny 类型的内存块申请
9: boolean tiny = isTiny(normCapacity);
10: if (tiny) { // < 512 tiny 类型的内存块申请
11: // 从 PoolThreadCache 缓存中，分配 tiny 内存块，并初始化到 PooledByteBuf 中。
12: if (cache.allocateTiny(this, buf, reqCapacity, normCapacity)) {
13: // was able to allocate out of the cache so move on
14: return;
15: }
16: // 获得 tableIdx 和 table 属性
17: tableIdx = tinyIdx(normCapacity);
18: table = tinySubpagePools;
19: } else {
20: // 从 PoolThreadCache 缓存中，分配 small 内存块，并初始化到 PooledByteBuf 中。
21: if (cache.allocateSmall(this, buf, reqCapacity, normCapacity)) {
22: // was able to allocate out of the cache so move on
23: return;
24: }
25: // 获得 tableIdx 和 table 属性
26: tableIdx = smallIdx(normCapacity);
27: table = smallSubpagePools;
28: }
29:
30: // 获得 PoolSubpage 链表的头节点
31: final PoolSubpage<T> head = table[tableIdx];
32:
33: // 从 PoolSubpage 链表中，分配 Subpage 内存块
34: //*/*
35: /* Synchronize on the head. This is needed as {@link PoolChunk/#allocateSubpage(int)} and
36: /* {@link PoolChunk/#free(long)} may modify the doubly linked list as well.
37: /*/
38: synchronized (head) { // 同步 head ，避免并发问题
39: final PoolSubpage<T> s = head.next;
40: if (s != head) {
41: assert s.doNotDestroy && s.elemSize == normCapacity;
42: // 分配 Subpage 内存块
43: long handle = s.allocate();
44: assert handle >= 0;
45: // 初始化 Subpage 内存块到 PooledByteBuf 对象中
46: s.chunk.initBufWithSubpage(buf, handle, reqCapacity);
47: // 增加 allocationsTiny 或 allocationsSmall 计数
48: incTinySmallAllocation(tiny);
49: // 返回，因为已经分配成功
50: return;
51: }
52: }
53: // 申请 Normal Page 内存块。实际上，只占用其中一块 Subpage 内存块。
54: synchronized (this) { // 同步 arena ，避免并发问题
55: allocateNormal(buf, reqCapacity, normCapacity);
56: }
57: // 增加 allocationsTiny 或 allocationsSmall 计数
58: incTinySmallAllocation(tiny);
59: // 返回，因为已经分配成功
60: return;
61: }
62: if (normCapacity <= chunkSize) {
63: // 从 PoolThreadCache 缓存中，分配 normal 内存块，并初始化到 PooledByteBuf 中。
64: if (cache.allocateNormal(this, buf, reqCapacity, normCapacity)) {
65: // was able to allocate out of the cache so move on
66: return;
67: }
68: // 申请 Normal Page 内存块
69: synchronized (this) { // 同步 arena ，避免并发问题
70: allocateNormal(buf, reqCapacity, normCapacity);
71: // 增加 allocationsNormal
72: ++allocationsNormal;
73: }
74: } else {
75: // 申请 Huge Page 内存块
76: // Huge allocations are never served via the cache so just call allocateHuge
77: allocateHuge(buf, reqCapacity);
78: }
79: }
```

- 第 3 行：调用

/#normalizeCapacity(int reqCapacity)
方法，标准化请求分配的内存大小。

- 第 5 至 61 行：上述“1、PoolSubpage ，用于分配小于 8KB 的内存块”。

- 第 5 行：调用

/#isTinyOrSmall(int normCapacity)
方法，判断请求分配的内存类型是否为 tiny 或 small 类型。

- 第 9 行：调用

/#isTiny(int normCapacity)
方法，判断请求分配的内存类型是否为 tiny 类型。

- 第 10 至 31 行：获得 PoolSubpage 链表的头节点。从实现上，和 [「2.3 findSubpagePoolHead」]() 功能上是一致的。

- 第 11 至 15 行：调用

PoolThreadCache/#allocateTiny(this, buf, reqCapacity, normCapacity)
方法，从 PoolThreadCache 缓存中，分配 tiny 内存块，并初始化到 PooledByteBuf 中。详细解析，见 [《精尽 Netty 源码解析 —— Buffer 之 Jemalloc（六）PoolThreadCache》](http://svip.iocoder.cn/Netty/ByteBuf-3-6-Jemalloc-ThreadCache) 中。

- 第 20 至 24 行：调用

PoolThreadCache/#allocateSmall(this, buf, reqCapacity, normCapacity)
方法，从 PoolThreadCache 缓存中，分配 small 内存块，并初始化到 PooledByteBuf 中。详细解析，见 [《精尽 Netty 源码解析 —— Buffer 之 Jemalloc（六）PoolThreadCache》](http://svip.iocoder.cn/Netty/ByteBuf-3-6-Jemalloc-ThreadCache) 中。

- 第 33 至 52 行：从 PoolSubpage 链表中，分配 Subpage 内存块。

- 第 43 行：调用

head.next
节点的

PoolSubpage/#allocate()
方法，分配一个 Subpage 内存块，并返回该内存块的位置

handle
。如果遗忘这个过程的胖友，可以看看 [《精尽 Netty 源码解析 —— Buffer 之 Jemalloc（三）PoolSubpage》](http://svip.iocoder.cn/Netty/ByteBuf-3-3-Jemalloc-subpage) 的 [「2.4 allocate」]() 。

- 第 46 行：调用

PoolChunk/#initBufWithSubpage(buf, handle, reqCapacity)
方法，初始化 Subpage 内存块到 PooledByteBuf 对象中。如果遗忘这个过程的胖友，可以看看 [《精尽 Netty 源码解析 —— Buffer 之 Jemalloc（二）PoolChunk》](http://svip.iocoder.cn/Netty/ByteBuf-3-2-Jemalloc-chunk) 的 [「2.5.1 initBufWithSubpage」]() 。

- 第 48 行：调用

/#incTinySmallAllocation(boolean tiny)
方法，增加

allocationsTiny
或

allocationsSmall
计数。详细解析，见 [「2.8.1 incTinySmallAllocation」]() 。

- 第 50 行：

return
返回，因为已经分配成功。

- 第 53 至 60 行：在 PoolSubpage 链表中，分配不到 Subpage 内存块，所以申请 Normal Page 内存块。实际上，只占用其中一块 Subpage 内存块。

- 第 53 至 56 行：调用

/#allocateNormal(PooledByteBuf<T> buf, int reqCapacity, int normCapacity)
方法，申请 Normal Page 内存块。详细解析，见 [「2.4.1 allocateNormal」]() 。

- 第 62 至 73 行：上述“2.1 小于

32MB
，分配 normal 内存块，即一个 Chunk 中的 **Page** 内存块”。

- 第 62 行：通过

normCapacity <= chunkSize
判断，判断请求分配的内存类型是否为 normal 类型。

- 第 63 至 67 行：调用

PoolThreadCache/#allocateNormal(this, buf, reqCapacity, normCapacity)
方法，从 PoolThreadCache 缓存中，分配 normal 内存块，并初始化到 PooledByteBuf 中。详细解析，见 [《精尽 Netty 源码解析 —— Buffer 之 Jemalloc（六）PoolThreadCache》](http://svip.iocoder.cn/Netty/ByteBuf-3-6-Jemalloc-ThreadCache) 中。

- 第 68 至 70 行：同【第 53 至 56 行】的代码。
- 第 72 行：增加

allocationsNormal
。

- 第 74 至 78 行：上述“2.2 大于等于

32MB
，分配 huge 内存块，即一整个 **Chunk** 内存块。”

- 第 77 行：调用

/#allocateHuge(PooledByteBuf<T> buf, int reqCapacity)
方法，申请 Huge 内存块。

### []( "2.4.1 allocateNormal")2.4.1 allocateNormal

/#allocateNormal(PooledByteBuf<T> buf, int reqCapacity, int normCapacity)
方法，申请 Normal Page 内存块。代码如下：

```
// Method must be called inside synchronized(this) { ... } block // 必须在 synchronized(this) { ... } 中执行
1: private void allocateNormal(PooledByteBuf<T> buf, int reqCapacity, int normCapacity){
2: // 按照优先级，从多个 ChunkList 中，分配 Normal Page 内存块。如果有一分配成功，返回
3: if (q050.allocate(buf, reqCapacity, normCapacity) || q025.allocate(buf, reqCapacity, normCapacity) ||
4: q000.allocate(buf, reqCapacity, normCapacity) || qInit.allocate(buf, reqCapacity, normCapacity) ||
5: q075.allocate(buf, reqCapacity, normCapacity)) {
6: return;
7: }
8:
9: // Add a new chunk.
10: // 新建 Chunk 内存块
11: PoolChunk<T> c = newChunk(pageSize, maxOrder, pageShifts, chunkSize);
12: // 申请对应的 Normal Page 内存块。实际上，如果申请分配的内存类型为 tiny 或 small 类型，实际申请的是 Subpage 内存块。
13: long handle = c.allocate(normCapacity);
14: assert handle > 0;
15: // 初始化 Normal Page / Subpage 内存块到 PooledByteBuf 对象中
16: c.initBuf(buf, handle, reqCapacity);
17: // 添加到 ChunkList 双向链中。
18: qInit.add(c);
19: }
```

- 按照优先级，从多个 ChunkList 中，调用

PoolChunkList/#allocate(normCapacity)
方法，分配 Normal Page 内存块。如果有一分配成功，

return
返回。如果遗忘这个过程的胖友，可以看看 [《精尽 Netty 源码解析 —— Buffer 之 Jemalloc（四）PoolChunkList》](http://svip.iocoder.cn/Netty/ByteBuf-3-4-Jemalloc-chunkList) 的 [「2.2 allocate」]() 。
FROM [《深入浅出 Netty 内存管理 PoolArena》](https://www.jianshu.com/p/4856bd30dd56)

分配内存时，为什么不从内存使用率较低的

q000
开始？在 ChunkList 中，我们知道一个 chunk 随着内存的释放，会往当前 ChunkList 的前一个节点移动。

**
q000
存在的目的是什么？**

q000
是用来保存内存利用率在

1%-50%
的 chunk ，那么这里为什么不包括

0%
的 chunk？
直接弄清楚这些，才好理解为什么不从

q000
开始分配。`q000 中的 chunk，当内存利用率为 0 时，就从链表中删除，直接释放物理内存，避免越来越多的 chunk 导致内存被占满。

想象一个场景，当应用在实际运行过程中，碰到访问高峰，这时需要分配的内存是平时的好几倍，当然也需要创建好几倍的 chunk ，如果先从

q0000
开始，这些在高峰期创建的 chunk 被回收的概率会大大降低，延缓了内存的回收进度，造成内存使用的浪费。

\*\*那么为什么选择从

q050
开始？\*\*

- 1、

q050
保存的是内存利用率

50%~100%
的 chunk ，这应该是个折中的选择！这样大部分情况下，chunk 的利用率都会保持在一个较高水平，提高整个应用的内存利用率；

- 2、

qinit
的 chunk 利用率低，但不会被回收；

- 3、

q075
和

q100
由于内存利用率太高，导致内存分配的成功率大大降低，因此放到最后；

- 第 11 行：调用

/#newChunk(pageSize, maxOrder, pageShifts, chunkSize)
**抽象**方法，新建 Chunk 内存块。需要 PoolArea 子类实现该方法，代码如下：

```
protected abstract PoolChunk<T> newChunk(int pageSize, int maxOrder, int pageShifts, int chunkSize);
```

- 第 13 行：调用新申请的 Chunk 内存块的

PoolChunk/#allocate(normCapacity)
方法，申请对应的 Normal Page 内存块。实际上，如果申请分配的内存类型为 tiny 或 small 类型，实际申请的是 Subpage 内存块。如果遗忘这个过程的胖友，可以看看 [《精尽 Netty 源码解析 —— Buffer 之 Jemalloc（二）PoolChunk》](http://svip.iocoder.cn/Netty/ByteBuf-3-2-Jemalloc-chunk) 的 [「2.2 allocate」]() 。

- 第 16 行：调用

PoolChunk/#initBuf(buf, handle, reqCapacity)
方法，初始化 Normal Page / Subpage 内存块到 PooledByteBuf 对象中。

- 第 18 行：调用

PoolChunkList/#add(PoolChunk)
方法，添加到 ChunkList 双向链中。

### []( "2.4.2 allocateHuge")2.4.2 allocateHuge

/#allocateHuge(PooledByteBuf<T> buf, int reqCapacity)
方法，申请 Huge 内存块。

```
1: private void allocateHuge(PooledByteBuf<T> buf, int reqCapacity){
2: // 新建 Chunk 内存块，它是 unpooled 的
3: PoolChunk<T> chunk = newUnpooledChunk(reqCapacity);
4: // 增加 activeBytesHuge
5: activeBytesHuge.add(chunk.chunkSize());
6: // 初始化 Huge 内存块到 PooledByteBuf 对象中
7: buf.initUnpooled(chunk, reqCapacity);
8: // 增加 allocationsHuge
9: allocationsHuge.increment();
10: }
```

- 第 3 行：调用

/#newUnpooledChunk(int capacity)
**抽象**方法，新建 **unpooled** Chunk 内存块。需要 PoolArea 子类实现该方法，代码如下：

```
protected abstract PoolChunk<T> newUnpooledChunk(int capacity); //
```

- 第 7 行：调用

PoolChunk/#initUnpooled(chunk, reqCapacity)
方法，初始化 Huge 内存块到 PooledByteBuf 对象中。

- 第 5 行：增加

activeBytesHuge
计数。

- 第 9 行：增加

allocationsHuge
计数。

## []( "2.5 reallocate")2.5 reallocate

/#reallocate(PooledByteBuf<T> buf, int newCapacity, boolean freeOldMemor)
方法，因为要扩容或缩容，所以重新分配合适的内存块给 PooledByteBuf 对象。代码如下：

```
1: void reallocate(PooledByteBuf<T> buf, int newCapacity, boolean freeOldMemory){
2: if (newCapacity < 0 || newCapacity > buf.maxCapacity()) {
3: throw new IllegalArgumentException("newCapacity: " + newCapacity);
4: }
5:
6: // 容量大小没有变化，直接返回
7: int oldCapacity = buf.length;
8: if (oldCapacity == newCapacity) {
9: return;
10: }
11:
12: // 记录老的内存块的信息
13: PoolChunk<T> oldChunk = buf.chunk;
14: long oldHandle = buf.handle;
15: T oldMemory = buf.memory;
16: int oldOffset = buf.offset;
17: int oldMaxLength = buf.maxLength;
18:
19: // 记录读写索引
20: int readerIndex = buf.readerIndex();
21: int writerIndex = buf.writerIndex();
22:
23: // 分配新的内存块给 PooledByteBuf 对象
24: allocate(parent.threadCache(), buf, newCapacity);
25:
26: // 扩容
27: if (newCapacity > oldCapacity) {
28: // 将老的内存块的数据，复制到新的内存块中
29: memoryCopy(
30: oldMemory, oldOffset,
31: buf.memory, buf.offset, oldCapacity);
32: // 缩容
33: } else {
34: // 有部分数据未读取完
35: if (readerIndex < newCapacity) {
36: // 如果 writerIndex 大于 newCapacity ，重置为 newCapacity ，避免越界
37: if (writerIndex > newCapacity) {
38: writerIndex = newCapacity;
39: }
40: // 将老的内存块的数据，复制到新的内存块中
41: memoryCopy(
42: oldMemory, oldOffset + readerIndex,
43: buf.memory, buf.offset + readerIndex, writerIndex - readerIndex);
44: // 全部读完，重置 readerIndex 和 writerIndex 为 newCapacity ，避免越界
45: } else {
46: readerIndex = writerIndex = newCapacity;
47: }
48: }
49:
50: // 设置读写索引
51: buf.setIndex(readerIndex, writerIndex);
52:
53: // 释放老的内存块
54: if (freeOldMemory) {
55: free(oldChunk, oldHandle, oldMaxLength, buf.cache);
56: }
57: }
```

- 第 2 至 4 行：新的容量(

newCapacity
) ，不能**超过** PooledByteBuf 对象的可分配的最大容量(

maxCapacity
) 。

- 第 6 至 10 行：容量大小没有变化，直接返回。
- 第 12 至 17 行：记录**老的**内存块的信息。

- 第 20 至 21 行：记录读写索引。
- 第 26 至 31 行：容量变大，说明是扩容，调用

/#memoryCopy(T src, int srcOffset, T dst, int dstOffset, int length)
**抽象**方法，将老的内存块的数据( 此处的复制，是全部数据 )，**复制到新的内存块中**。需要 PoolArea 子类实现该方法，代码如下：

```
protected abstract void memoryCopy(T src, int srcOffset, T dst, int dstOffset, int length);
```

- 第 33 至 48 行：容量变小，说明是缩容。

- 第 34 至 44 行：有**部分**数据**未读取完**，调用

/#memoryCopy(T src, int srcOffset, T dst, int dstOffset, int length)
**抽象**方法，将老的内存块的数据( 此处的复制，是全部数据 )，复制到新的内存块中。😈 注意，**此处复制的只有未读取完的部分数据**。

- 第 36 至 39 行：如果

writerIndex
大于

newCapacity
，重置为

newCapacity
，避免越界。

- 第 44 至 47 行：全部读完，**无需复制**。

- 第 46 行: 全部读完，重置

readerIndex
和

writerIndex
为

newCapacity
，避免越界。

- 第 51 行：设置读写索引。
- 第 53 至 56 行：如果需要释放老的内存块(

freeOldMemory
为

true
) 时，调用

/#free(PoolChunk<T> chunk, long handle, int normCapacity, PoolThreadCache cache)
方法，进行释放。详细解析，见 [「2.6 free」]() 。

## []( "2.6 free")2.6 free

/#free(PoolChunk<T> chunk, long handle, int normCapacity, PoolThreadCache cache)
方法，释放内存块。代码如下：

```
1: void free(PoolChunk<T> chunk, long handle, int normCapacity, PoolThreadCache cache){
2: if (chunk.unpooled) {
3: int size = chunk.chunkSize();
4: // 直接销毁 Chunk 内存块，因为占用空间较大
5: destroyChunk(chunk);
6: // 减少 activeBytesHuge 计数
7: activeBytesHuge.add(-size);
8: // 减少 deallocationsHuge 计数
9: deallocationsHuge.increment();
10: } else {
11: // 计算内存的 SizeClass
12: SizeClass sizeClass = sizeClass(normCapacity);
13: // 添加内存块到 PoolThreadCache 缓存
14: if (cache != null && cache.add(this, chunk, handle, normCapacity, sizeClass)) {
15: // cached so not free it.
16: return;
17: }
18: // 释放 Page / Subpage 内存块回 Chunk 中
19: freeChunk(chunk, handle, sizeClass);
20: }
21: }
```

- 第 2 至 9 行：**unpooled** 类型的 Chunk 对象，目前是 Huge 内存块。

- 第 5 行：调用

/#destroyChunk(PoolChunk<T> chunk)
方法，直接销毁 Chunk 内存块，因为占用空间较大。详细解析，见 [「2.7 finalize」]() 。

- 第 6 至 9 行：减少

activeBytesHuge
、

deallocationsHuge
计数。

- 第 10 至 20 行：**pooled** 类型的 Chunk 对象，目前是 Page / Subpage 内存块。

- 第 12 行：调用

/#size(normCapacity)
方法，计算内存的 SizeClass 内存类型。

- 第 12 行：计算内存的 SizeClass 。
- 第 13 至 17 行：调用

PoolThreadCache/#add(PoolArena<?> area, PoolChunk chunk, long handle, int normCapacity, SizeClass sizeClass)
方法，添加内存块到 PoolThreadCache 的指定 MemoryRegionCache 的队列中，进行缓存。并且，返回是否添加成功。详细解析，见 [《精尽 Netty 源码解析 —— Buffer 之 Jemalloc（六）PoolThreadCache》](http://svip.iocoder.cn/Netty/ByteBuf-3-6-Jemalloc-ThreadCache/) 。

- 第 19 行：调用

/#freeChunk(PoolChunk<T> chunk, long handle, SizeClass sizeClass)
方法，释放指定位置的 Page / Subpage 内存块回 Chunk 中。

### []( "2.6.1 freeChunk")2.6.1 freeChunk

/#freeChunk(PoolChunk<T> chunk, long handle, SizeClass sizeClass)
方法，释放指定位置的 Page / Subpage 内存块回 Chunk 中。代码如下：

```
1: void freeChunk(PoolChunk<T> chunk, long handle, SizeClass sizeClass){
2: final boolean destroyChunk;
3: synchronized (this) { // 锁，避免并发
4: // 减小相应的计数
5: switch (sizeClass) {
6: case Normal:
7: ++deallocationsNormal;
8: break;
9: case Small:
10: ++deallocationsSmall;
11: break;
12: case Tiny:
13: ++deallocationsTiny;
14: break;
15: default:
16: throw new Error();
17: }
18: // 释放指定位置的内存块
19: destroyChunk = !chunk.parent.free(chunk, handle);
20: }
21: // 当 destroyChunk 为 true 时，意味着 Chunk 中不存在在使用的 Page / Subpage 内存块。也就是说，内存使用率为 0 ，所以销毁 Chunk
22: if (destroyChunk) {
23: // destroyChunk not need to be called while holding the synchronized lock.
24: destroyChunk(chunk);
25: }
26: }
```

- 第 4 至 17 行：减小**相应**的计数。
- 第 19 行：调用 Chunk 对象所在的 ChunList 的

ChunkList/#free(chunk, handle)
方法，释放指定位置的内存块。如果遗忘这个过程的胖友，可以看看 [《精尽 Netty 源码解析 —— Buffer 之 Jemalloc（四）PoolChunkList》](http://svip.iocoder.cn/Netty/ByteBuf-3-4-Jemalloc-chunkList) 的 [「2.3 free」]() 。

- 第 21 至 25 行：当

destroyChunk
为

true
时，意味着 Chunk 中不存在在使用的 Page / Subpage 内存块。也就是说，内存使用率为 0 ，所以调用

/#destroyChunk(PoolChunk<T> chunk)
方法，直接销毁 Chunk 内存块，回收对应的空间。详细解析，见 [「2.7 finalize」]() 。

## []( "2.7 finalize")2.7 finalize

在 PoolArena 对象被 GC 回收时，清理其管理的内存。😈 实际上，主要是为了清理对外内存。代码如下：

```
@Override
protected final void finalize() throws Throwable{
try {
// 调用父方法
super.finalize();
} finally {
// 清理 tiny Subpage 们
destroyPoolSubPages(smallSubpagePools);
// 清理 small Subpage 们
destroyPoolSubPages(tinySubpagePools);
// 清理 ChunkList 们
destroyPoolChunkLists(qInit, q000, q025, q050, q075, q100);
}
}
private static void destroyPoolSubPages(PoolSubpage<?>[] pages){
for (PoolSubpage<?> page : pages) {
page.destroy();
}
}
private void destroyPoolChunkLists(PoolChunkList<T>... chunkLists){
for (PoolChunkList<T> chunkList: chunkLists) {
chunkList.destroy(this);
}
}
```

## []( "2.8 PoolArenaMetric")2.8 PoolArenaMetric

老艿艿：这个小节，主要是读取 Metric 数据的方法，快速浏览或跳过都可以。

io.netty.buffer.PoolArenaMetric
，PoolArena Metric 接口。代码如下：

```
public interface PoolArenaMetric{
//*/*
/* Returns the number of thread caches backed by this arena.
/*/
int numThreadCaches();
//*/*
/* Returns the number of tiny sub-pages for the arena.
/*/
int numTinySubpages();
//*/*
/* Returns the number of small sub-pages for the arena.
/*/
int numSmallSubpages();
//*/*
/* Returns the number of chunk lists for the arena.
/*/
int numChunkLists();
//*/*
/* Returns an unmodifiable {@link List} which holds {@link PoolSubpageMetric}s for tiny sub-pages.
/*/
List<PoolSubpageMetric> tinySubpages();
//*/*
/* Returns an unmodifiable {@link List} which holds {@link PoolSubpageMetric}s for small sub-pages.
/*/
List<PoolSubpageMetric> smallSubpages();
//*/*
/* Returns an unmodifiable {@link List} which holds {@link PoolChunkListMetric}s.
/*/
List<PoolChunkListMetric> chunkLists();
//*/*
/* Return the number of allocations done via the arena. This includes all sizes.
/*/
long numAllocations();
//*/*
/* Return the number of tiny allocations done via the arena.
/*/
long numTinyAllocations();
//*/*
/* Return the number of small allocations done via the arena.
/*/
long numSmallAllocations();
//*/*
/* Return the number of normal allocations done via the arena.
/*/
long numNormalAllocations();
//*/*
/* Return the number of huge allocations done via the arena.
/*/
long numHugeAllocations();
//*/*
/* Return the number of deallocations done via the arena. This includes all sizes.
/*/
long numDeallocations();
//*/*
/* Return the number of tiny deallocations done via the arena.
/*/
long numTinyDeallocations();
//*/*
/* Return the number of small deallocations done via the arena.
/*/
long numSmallDeallocations();
//*/*
/* Return the number of normal deallocations done via the arena.
/*/
long numNormalDeallocations();
//*/*
/* Return the number of huge deallocations done via the arena.
/*/
long numHugeDeallocations();
//*/*
/* Return the number of currently active allocations.
/*/
long numActiveAllocations();
//*/*
/* Return the number of currently active tiny allocations.
/*/
long numActiveTinyAllocations();
//*/*
/* Return the number of currently active small allocations.
/*/
long numActiveSmallAllocations();
//*/*
/* Return the number of currently active normal allocations.
/*/
long numActiveNormalAllocations();
//*/*
/* Return the number of currently active huge allocations.
/*/
long numActiveHugeAllocations();
//*/*
/* Return the number of active bytes that are currently allocated by the arena.
/*/
long numActiveBytes();
}
```

PoolArena 对 PoolArenaMetric 接口的实现，代码如下：

```
@Override
public int numThreadCaches(){
return numThreadCaches.get();
}
@Override
public int numTinySubpages(){
return tinySubpagePools.length;
}
@Override
public int numSmallSubpages(){
return smallSubpagePools.length;
}
@Override
public int numChunkLists(){
return chunkListMetrics.size();
}
@Override
public List<PoolSubpageMetric> tinySubpages(){
return subPageMetricList(tinySubpagePools);
}
@Override
public List<PoolSubpageMetric> smallSubpages(){
return subPageMetricList(smallSubpagePools);
}
@Override
public List<PoolChunkListMetric> chunkLists(){
return chunkListMetrics;
}
private static List<PoolSubpageMetric> subPageMetricList(PoolSubpage<?>[] pages){
List<PoolSubpageMetric> metrics = new ArrayList<PoolSubpageMetric>();
for (PoolSubpage<?> head : pages) {
if (head.next == head) {
continue;
}
PoolSubpage<?> s = head.next;
for (;;) {
metrics.add(s);
s = s.next;
if (s == head) {
break;
}
}
}
return metrics;
}
@Override
public long numAllocations(){
final long allocsNormal;
synchronized (this) {
allocsNormal = allocationsNormal;
}
return allocationsTiny.value() + allocationsSmall.value() + allocsNormal + allocationsHuge.value();
}
@Override
public long numTinyAllocations(){
return allocationsTiny.value();
}
@Override
public long numSmallAllocations(){
return allocationsSmall.value();
}
@Override
public synchronized long numNormalAllocations(){
return allocationsNormal;
}
@Override
public long numDeallocations(){
final long deallocs;
synchronized (this) {
deallocs = deallocationsTiny + deallocationsSmall + deallocationsNormal;
}
return deallocs + deallocationsHuge.value();
}
@Override
public synchronized long numTinyDeallocations(){
return deallocationsTiny;
}
@Override
public synchronized long numSmallDeallocations(){
return deallocationsSmall;
}
@Override
public synchronized long numNormalDeallocations(){
return deallocationsNormal;
}
@Override
public long numHugeAllocations(){
return allocationsHuge.value();
}
@Override
public long numHugeDeallocations(){
return deallocationsHuge.value();
}
@Override
public long numActiveAllocations(){
long val = allocationsTiny.value() + allocationsSmall.value() + allocationsHuge.value()
- deallocationsHuge.value();
synchronized (this) {
val += allocationsNormal - (deallocationsTiny + deallocationsSmall + deallocationsNormal);
}
return max(val, 0);
}
@Override
public long numActiveTinyAllocations(){
return max(numTinyAllocations() - numTinyDeallocations(), 0);
}
@Override
public long numActiveSmallAllocations(){
return max(numSmallAllocations() - numSmallDeallocations(), 0);
}
@Override
public long numActiveNormalAllocations(){
final long val;
synchronized (this) {
val = allocationsNormal - deallocationsNormal;
}
return max(val, 0);
}
@Override
public long numActiveHugeAllocations(){
return max(numHugeAllocations() - numHugeDeallocations(), 0);
}
@Override
public long numActiveBytes(){
long val = activeBytesHuge.value();
synchronized (this) {
for (int i = 0; i < chunkListMetrics.size(); i++) {
for (PoolChunkMetric m: chunkListMetrics.get(i)) {
val += m.chunkSize();
}
}
}
return max(0, val);
}
```

### []( "2.8.1 incTinySmallAllocation")2.8.1 incTinySmallAllocation

/#incTinySmallAllocation(boolean tiny)
方法，增加

allocationsTiny
或

allocationsSmall
计数。代码如下：

```
private void incTinySmallAllocation(boolean tiny){
if (tiny) {
allocationsTiny.increment();
} else {
allocationsSmall.increment();
}
}
```

## []( "2.9 抽象方法")2.9 抽象方法

虽然上文中，已经提到了几个抽象方法，这里还是同一整理如下：

```
abstract boolean isDirect();
protected abstract PoolChunk<T> newChunk(int pageSize, int maxOrder, int pageShifts, int chunkSize); //
protected abstract PoolChunk<T> newUnpooledChunk(int capacity); //
protected abstract PooledByteBuf<T> newByteBuf(int maxCapacity);
protected abstract void memoryCopy(T src, int srcOffset, T dst, int dstOffset, int length); //
protected abstract void destroyChunk(PoolChunk<T> chunk);
```

# []( "3. HeapArena")3. HeapArena

HeapArena ，继承 PoolArena 抽象类，对 Heap 类型的内存分配。
HeapArena 是 PoolArena 的内部静态类。代码比较简单，胖友自己看看就成。

```
static final class HeapArena extends PoolArena<byte[]> { // 管理 byte[] 数组
HeapArena(PooledByteBufAllocator parent, int pageSize, int maxOrder, int pageShifts, int chunkSize, int directMemoryCacheAlignment) {
super(parent, pageSize, maxOrder, pageShifts, chunkSize, directMemoryCacheAlignment);
}
private static byte[] newByteArray(int size) {
return PlatformDependent.allocateUninitializedArray(size); // 创建 byte[] 数组
}
@Override
boolean isDirect(){
return false;
}
@Override
protected PoolChunk<byte[]> newChunk(int pageSize, int maxOrder, int pageShifts, int chunkSize) {
return new PoolChunk<byte[]>(this, newByteArray(chunkSize), pageSize, maxOrder, pageShifts, chunkSize, 0);
}
@Override
protected PoolChunk<byte[]> newUnpooledChunk(int capacity) {
return new PoolChunk<byte[]>(this, newByteArray(capacity), capacity, 0);
}
@Override
protected void destroyChunk(PoolChunk<byte[]> chunk){
// Rely on GC. 依赖 GC
}
@Override
protected PooledByteBuf<byte[]> newByteBuf(int maxCapacity) {
return HAS_UNSAFE ? PooledUnsafeHeapByteBuf.newUnsafeInstance(maxCapacity)
: PooledHeapByteBuf.newInstance(maxCapacity);
}
@Override
protected void memoryCopy(byte[] src, int srcOffset, byte[] dst, int dstOffset, int length){
if (length == 0) {
return;
}
System.arraycopy(src, srcOffset, dst, dstOffset, length);
}
}
```

# []( "4. DirectArena")4. DirectArena

DirectArena ，继承 PoolArena 抽象类，对 Direct 类型的内存分配。
DirectArena 是 PoolArena 的内部静态类。代码比较简单，胖友自己看看就成。

```
static final class DirectArena extends PoolArena<ByteBuffer>{ // 管理 Direct ByteBuffer 对象
DirectArena(PooledByteBufAllocator parent, int pageSize, int maxOrder,
int pageShifts, int chunkSize, int directMemoryCacheAlignment) {
super(parent, pageSize, maxOrder, pageShifts, chunkSize, directMemoryCacheAlignment);
}
@Override
boolean isDirect(){
return true;
}
private int offsetCacheLine(ByteBuffer memory){
// We can only calculate the offset if Unsafe is present as otherwise directBufferAddress(...) will
// throw an NPE.
return HAS_UNSAFE ?
(int) (PlatformDependent.directBufferAddress(memory) & directMemoryCacheAlignmentMask) : 0;
}
@Override
protected PoolChunk<ByteBuffer> newChunk(int pageSize, int maxOrder,
int pageShifts, int chunkSize){
if (directMemoryCacheAlignment == 0) {
return new PoolChunk<ByteBuffer>(this, allocateDirect(chunkSize), pageSize, maxOrder, pageShifts, chunkSize, 0);
}
final ByteBuffer memory = allocateDirect(chunkSize + directMemoryCacheAlignment);
return new PoolChunk<ByteBuffer>(this, memory, pageSize, maxOrder, pageShifts, chunkSize, offsetCacheLine(memory));
}
@Override
protected PoolChunk<ByteBuffer> newUnpooledChunk(int capacity){
if (directMemoryCacheAlignment == 0) {
return new PoolChunk<ByteBuffer>(this,
allocateDirect(capacity), capacity, 0);
}
final ByteBuffer memory = allocateDirect(capacity + directMemoryCacheAlignment);
return new PoolChunk<ByteBuffer>(this, memory, capacity, offsetCacheLine(memory));
}
private static ByteBuffer allocateDirect(int capacity){ // 创建 Direct ByteBuffer 对象
return PlatformDependent.useDirectBufferNoCleaner() ?
PlatformDependent.allocateDirectNoCleaner(capacity) : ByteBuffer.allocateDirect(capacity);
}
@Override
protected void destroyChunk(PoolChunk<ByteBuffer> chunk){
if (PlatformDependent.useDirectBufferNoCleaner()) {
PlatformDependent.freeDirectNoCleaner(chunk.memory);
} else {
PlatformDependent.freeDirectBuffer(chunk.memory);
}
}
@Override
protected PooledByteBuf<ByteBuffer> newByteBuf(int maxCapacity){
if (HAS_UNSAFE) {
return PooledUnsafeDirectByteBuf.newInstance(maxCapacity);
} else {
return PooledDirectByteBuf.newInstance(maxCapacity);
}
}
@Override
protected void memoryCopy(ByteBuffer src, int srcOffset, ByteBuffer dst, int dstOffset, int length){
if (length == 0) {
return;
}
if (HAS_UNSAFE) {
PlatformDependent.copyMemory(
PlatformDependent.directBufferAddress(src) + srcOffset,
PlatformDependent.directBufferAddress(dst) + dstOffset, length);
} else {
// We must duplicate the NIO buffers because they may be accessed by other Netty buffers.
src = src.duplicate();
dst = dst.duplicate();
src.position(srcOffset).limit(srcOffset + length);
dst.position(dstOffset);
dst.put(src);
}
}
}
```

# []( "666. 彩蛋")666. 彩蛋

终于看懂 Jemalloc 算法的大体的实现。一开始看，一脸懵逼！！！其实耐下性子，慢慢看，总能看懂的。

当然，如果这个时候让自己手写 Jemalloc 算法，估计还是会泪崩。哈哈哈，相比写代码来说，读懂代码还是容易很多的。

嘿嘿，找了一张厉害的图，胖友在结合这个图，理解理解。
FROM [《Netty Buffer - 内存管理 PoolArena》](http://www.woowen.com/%E6%BA%90%E7%A0%81/2016/08/01/Netty%20buffer%20-%20%E5%86%85%E5%AD%98%E7%AE%A1%E7%90%86%20PoolArena/)

[![内存分配](http://static2.iocoder.cn/images/Netty/2018_09_13/04.png)](http://static2.iocoder.cn/images/Netty/2018_09_13/04.png '内存分配')内存分配

参考如下文章：

- 占小狼 [《深入浅出 Netty 内存管理 PoolArena》](https://www.jianshu.com/p/4856bd30dd56)
- Hypercube [《自顶向下深入分析 Netty（十）–PoolArena》](https://www.jianshu.com/p/86fbacdb68bd)
