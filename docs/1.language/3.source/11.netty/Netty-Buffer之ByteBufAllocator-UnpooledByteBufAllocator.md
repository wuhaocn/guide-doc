# 精尽 Netty 源码解析 —— Buffer 之 ByteBufAllocator（二）UnpooledByteBufAllocator

# []( "1. 概述")1. 概述

本文，我们来分享 UnpooledByteBufAllocator ，**普通**的 ByteBuf 的分配器，**不基于内存池**。

# []( "2. ByteBufAllocatorMetricProvider")2. ByteBufAllocatorMetricProvider

io.netty.buffer.ByteBufAllocatorMetricProvider
，ByteBufAllocator Metric 提供者接口，**用于监控 ByteBuf 的 Heap 和 Direct 占用内存的情况**。代码如下：

```
public interface ByteBufAllocatorMetricProvider{
//*/*
/* Returns a {@link ByteBufAllocatorMetric} for a {@link ByteBufAllocator}.
/*/
ByteBufAllocatorMetric metric();
}
```

ByteBufAllocatorMetricProvider 有两个子类：UnpooledByteBufAllocator 和 PooledByteBufAllocator 。

# []( "3. ByteBufAllocatorMetric")3. ByteBufAllocatorMetric

io.netty.buffer.ByteBufAllocatorMetric
，ByteBufAllocator Metric 接口。代码如下：

```
public interface ByteBufAllocatorMetric{
//*/*
/* Returns the number of bytes of heap memory used by a {@link ByteBufAllocator} or {@code -1} if unknown.
/*
/* 已使用 Heap 占用内存大小
/*/
long usedHeapMemory();
//*/*
/* Returns the number of bytes of direct memory used by a {@link ByteBufAllocator} or {@code -1} if unknown.
/*
/* 已使用 Direct 占用内存大小
/*/
long usedDirectMemory();
}
```

ByteBufAllocatorMetric 有两个子类：UnpooledByteBufAllocatorMetric 和 PooledByteBufAllocatorMetric 。

## []( "3.1 UnpooledByteBufAllocatorMetric")3.1 UnpooledByteBufAllocatorMetric

UnpooledByteBufAllocatorMetric ，在 UnpooledByteBufAllocator 的**内部静态类**，实现 ByteBufAllocatorMetric 接口，UnpooledByteBufAllocator Metric 实现类。代码如下：

```
//*/*
/* Direct ByteBuf 占用内存大小
/*/
final LongCounter directCounter = PlatformDependent.newLongCounter();
//*/*
/* Heap ByteBuf 占用内存大小
/*/
final LongCounter heapCounter = PlatformDependent.newLongCounter();
@Override
public long usedHeapMemory(){
return heapCounter.value();
}
@Override
public long usedDirectMemory(){
return directCounter.value();
}
```

- 比较简单，两个计数器。
- PlatformDependent/#newLongCounter()
  方法，获得 LongCounter 对象。代码如下：

```
//*/*
/* Creates a new fastest {@link LongCounter} implementation for the current platform.
/*/
public static LongCounter newLongCounter(){
if (javaVersion() >= 8) {
return new LongAdderCounter();
} else {
return new AtomicLongCounter();
}
}
```

- 也就是说，JDK

> =8
> 使用

java.util.concurrent.atomic.LongAdder
，JDK

<7
使用

java.util.concurrent.atomic.AtomicLong
。相比来说，Metric 写多读少，所以 LongAdder 比 AtomicLong 更合适。对比的解析，可以看看 [《Java 并发计数器探秘》](https://www.cnkirito.moe/java-concurrent-counter/) 。

# []( "4. UnpooledByteBufAllocator")4. UnpooledByteBufAllocator

io.netty.buffer.UnpooledByteBufAllocator
，实现 ByteBufAllocatorMetricProvider 接口，继承 AbstractByteBufAllocator 抽象类，**普通**的 ByteBuf 的分配器，**不基于内存池**。

## []( "4.1 构造方法")4.1 构造方法

```
//*/*
/* Metric
/*/
private final UnpooledByteBufAllocatorMetric metric = new UnpooledByteBufAllocatorMetric();
//*/*
/* 是否禁用内存泄露检测功能
/*/
private final boolean disableLeakDetector;
//*/*
/* 不使用 `io.netty.util.internal.Cleaner` 释放 Direct ByteBuf
/*
/* @see UnpooledUnsafeNoCleanerDirectByteBuf
/* @see InstrumentedUnpooledUnsafeNoCleanerDirectByteBuf
/*/
private final boolean noCleaner;
public UnpooledByteBufAllocator(boolean preferDirect){
this(preferDirect, false);
}
public UnpooledByteBufAllocator(boolean preferDirect, boolean disableLeakDetector){
this(preferDirect, disableLeakDetector, PlatformDependent.useDirectBufferNoCleaner() //*/* 返回 true /*/*/ );
}
//*/*
/* Create a new instance
/*
/* @param preferDirect {@code true} if {@link /#buffer(int)} should try to allocate a direct buffer rather than
/* a heap buffer
/* @param disableLeakDetector {@code true} if the leak-detection should be disabled completely for this
/* allocator. This can be useful if the user just want to depend on the GC to handle
/* direct buffers when not explicit released.
/* @param tryNoCleaner {@code true} if we should try to use {@link PlatformDependent/#allocateDirectNoCleaner(int)}
/* to allocate direct memory.
/*/
public UnpooledByteBufAllocator(boolean preferDirect, boolean disableLeakDetector, boolean tryNoCleaner){
super(preferDirect);
this.disableLeakDetector = disableLeakDetector;
noCleaner = tryNoCleaner && PlatformDependent.hasUnsafe() //*/* 返回 true /*/*/
&& PlatformDependent.hasDirectBufferNoCleanerConstructor() //*/* 返回 true /*/*/ ;
}
```

- metric
  属性，UnpooledByteBufAllocatorMetric 对象。
- disableLeakDetector
  属性，是否禁用内存泄露检测功能。

- 默认为

false
。

- noCleaner
  属性，是否不使用

io.netty.util.internal.Cleaner
来释放 Direct ByteBuf 。

- 默认为

true
。

- 详细解析，见 [「5.5 InstrumentedUnpooledUnsafeNoCleanerDirectByteBuf」]() 。

## []( "4.2 newHeapBuffer")4.2 newHeapBuffer

```
@Override
protected ByteBuf newHeapBuffer(int initialCapacity, int maxCapacity){
return PlatformDependent.hasUnsafe() ?
new InstrumentedUnpooledUnsafeHeapByteBuf(this, initialCapacity, maxCapacity) :
new InstrumentedUnpooledHeapByteBuf(this, initialCapacity, maxCapacity);
}
```

- 创建的是以

"Instrumented"
的 Heap ByteBuf 对象，因为要结合 Metric 。详细解析，见 [「5. Instrumented ByteBuf」]() 。

## []( "4.3 newDirectBuffer")4.3 newDirectBuffer

```
@Override
protected ByteBuf newDirectBuffer(int initialCapacity, int maxCapacity){
final ByteBuf buf;
if (PlatformDependent.hasUnsafe()) {
buf = noCleaner ? new InstrumentedUnpooledUnsafeNoCleanerDirectByteBuf(this, initialCapacity, maxCapacity) :
new InstrumentedUnpooledUnsafeDirectByteBuf(this, initialCapacity, maxCapacity);
} else {
buf = new InstrumentedUnpooledDirectByteBuf(this, initialCapacity, maxCapacity);
}
return disableLeakDetector ? buf : toLeakAwareBuffer(buf);
}
```

- 创建的是以

"Instrumented"
的 Heap ByteBuf 对象，因为要结合 Metric 。详细解析，见 [「5. Instrumented ByteBuf」]() 。

- 结合了

disableLeakDetector
属性。

## []( "4.4 compositeHeapBuffer")4.4 compositeHeapBuffer

```
@Override
public CompositeByteBuf compositeHeapBuffer(int maxNumComponents){
CompositeByteBuf buf = new CompositeByteBuf(this, false, maxNumComponents);
return disableLeakDetector ? buf : toLeakAwareBuffer(buf);
}
```

- 结合了

disableLeakDetector
属性。

## []( "4.5 compositeDirectBuffer")4.5 compositeDirectBuffer

```
@Override
public CompositeByteBuf compositeDirectBuffer(int maxNumComponents){
CompositeByteBuf buf = new CompositeByteBuf(this, true, maxNumComponents);
return disableLeakDetector ? buf : toLeakAwareBuffer(buf);
}
```

- 结合了

disableLeakDetector
属性。

## []( "4.6 isDirectBufferPooled")4.6 isDirectBufferPooled

```
@Override
public boolean isDirectBufferPooled(){
return false;
}
```

## []( "4.7 Metric 相关操作方法")4.7 Metric 相关操作方法

```
@Override
public ByteBufAllocatorMetric metric(){
return metric;
}
void incrementDirect(int amount){ // 增加 Direct
metric.directCounter.add(amount);
}
void decrementDirect(int amount){ // 减少 Direct
metric.directCounter.add(-amount);
}
void incrementHeap(int amount){ // 增加 Heap
metric.heapCounter.add(amount);
}
void decrementHeap(int amount){ // 减少 Heap
metric.heapCounter.add(-amount);
}
```

# []( "5. Instrumented ByteBuf")5. Instrumented ByteBuf

因为要和 Metric 结合，所以通过**继承**的方式，进行增强。

## []( "5.1 InstrumentedUnpooledUnsafeHeapByteBuf")5.1 InstrumentedUnpooledUnsafeHeapByteBuf

**Instrumented**UnpooledUnsafeHeapByteBuf ，在 UnpooledByteBufAllocator 的**内部静态类**，继承 UnpooledUnsafeHeapByteBuf 类。代码如下：

```
private static final class InstrumentedUnpooledUnsafeHeapByteBuf extends UnpooledUnsafeHeapByteBuf{
InstrumentedUnpooledUnsafeHeapByteBuf(UnpooledByteBufAllocator alloc, int initialCapacity, int maxCapacity) {
super(alloc, initialCapacity, maxCapacity);
}
@Override
protected byte[] allocateArray(int initialCapacity) {
byte[] bytes = super.allocateArray(initialCapacity);
// Metric ++
((UnpooledByteBufAllocator) alloc()).incrementHeap(bytes.length);
return bytes;
}
@Override
protected void freeArray(byte[] array){
int length = array.length;
super.freeArray(array);
// Metric --
((UnpooledByteBufAllocator) alloc()).decrementHeap(length);
}
}
```

- 在原先的基础上，调用 Metric 相应的增减操作方法，得以记录 Heap 占用内存的大小。

## []( "5.2 InstrumentedUnpooledHeapByteBuf")5.2 InstrumentedUnpooledHeapByteBuf

**Instrumented**UnpooledHeapByteBuf ，在 UnpooledByteBufAllocator 的**内部静态类**，继承 UnpooledHeapByteBuf 类。代码如下：

```
private static final class InstrumentedUnpooledHeapByteBuf extends UnpooledHeapByteBuf{
InstrumentedUnpooledHeapByteBuf(UnpooledByteBufAllocator alloc, int initialCapacity, int maxCapacity) {
super(alloc, initialCapacity, maxCapacity);
}
@Override
protected byte[] allocateArray(int initialCapacity) {
byte[] bytes = super.allocateArray(initialCapacity);
// Metric ++
((UnpooledByteBufAllocator) alloc()).incrementHeap(bytes.length);
return bytes;
}
@Override
protected void freeArray(byte[] array){
int length = array.length;
super.freeArray(array);
// Metric --
((UnpooledByteBufAllocator) alloc()).decrementHeap(length);
}
}
```

- 在原先的基础上，调用 Metric 相应的增减操作方法，得以记录 Heap 占用内存的大小。

## []( "5.3 InstrumentedUnpooledUnsafeDirectByteBuf")5.3 InstrumentedUnpooledUnsafeDirectByteBuf

**Instrumented**UnpooledUnsafeDirectByteBuf ，在 UnpooledByteBufAllocator 的**内部静态类**，继承 UnpooledUnsafeDirectByteBuf 类。代码如下：

```
private static final class InstrumentedUnpooledUnsafeDirectByteBuf extends UnpooledUnsafeDirectByteBuf{
InstrumentedUnpooledUnsafeDirectByteBuf(
UnpooledByteBufAllocator alloc, int initialCapacity, int maxCapacity) {
super(alloc, initialCapacity, maxCapacity);
}
@Override
protected ByteBuffer allocateDirect(int initialCapacity){
ByteBuffer buffer = super.allocateDirect(initialCapacity);
// Metric ++
((UnpooledByteBufAllocator) alloc()).incrementDirect(buffer.capacity());
return buffer;
}
@Override
protected void freeDirect(ByteBuffer buffer){
int capacity = buffer.capacity();
super.freeDirect(buffer);
// Metric --
((UnpooledByteBufAllocator) alloc()).decrementDirect(capacity);
}
}
```

- 在原先的基础上，调用 Metric 相应的增减操作方法，得以记录 Direct 占用内存的大小。

## []( "5.4 InstrumentedUnpooledDirectByteBuf")5.4 InstrumentedUnpooledDirectByteBuf

**Instrumented**UnpooledDirectByteBuf 的**内部静态类**，继承 UnpooledDirectByteBuf 类。代码如下：

```
private static final class InstrumentedUnpooledDirectByteBuf extends UnpooledDirectByteBuf{
InstrumentedUnpooledDirectByteBuf(
UnpooledByteBufAllocator alloc, int initialCapacity, int maxCapacity) {
super(alloc, initialCapacity, maxCapacity);
}
@Override
protected ByteBuffer allocateDirect(int initialCapacity){
ByteBuffer buffer = super.allocateDirect(initialCapacity);
// Metric ++
((UnpooledByteBufAllocator) alloc()).incrementDirect(buffer.capacity());
return buffer;
}
@Override
protected void freeDirect(ByteBuffer buffer){
int capacity = buffer.capacity();
super.freeDirect(buffer);
// Metric --
((UnpooledByteBufAllocator) alloc()).decrementDirect(capacity);
}
}
```

- 在原先的基础上，调用 Metric 相应的增减操作方法，得以记录 Direct 占用内存的大小。

## []( "5.5 InstrumentedUnpooledUnsafeNoCleanerDirectByteBuf")5.5 InstrumentedUnpooledUnsafeNoCleanerDirectByteBuf

**Instrumented**UnpooledDirectByteBuf 的**内部静态类**，继承 UnpooledUnsafeNoCleanerDirectByteBuf 类。代码如下：

```
private static final class InstrumentedUnpooledUnsafeNoCleanerDirectByteBuf
extends UnpooledUnsafeNoCleanerDirectByteBuf{
InstrumentedUnpooledUnsafeNoCleanerDirectByteBuf(
UnpooledByteBufAllocator alloc, int initialCapacity, int maxCapacity) {
super(alloc, initialCapacity, maxCapacity);
}
@Override
protected ByteBuffer allocateDirect(int initialCapacity){
ByteBuffer buffer = super.allocateDirect(initialCapacity);
// Metric ++
((UnpooledByteBufAllocator) alloc()).incrementDirect(buffer.capacity());
return buffer;
}
@Override
ByteBuffer reallocateDirect(ByteBuffer oldBuffer, int initialCapacity){
int capacity = oldBuffer.capacity();
ByteBuffer buffer = super.reallocateDirect(oldBuffer, initialCapacity);
// Metric ++
((UnpooledByteBufAllocator) alloc()).incrementDirect(buffer.capacity() - capacity);
return buffer;
}
@Override
protected void freeDirect(ByteBuffer buffer){
int capacity = buffer.capacity();
super.freeDirect(buffer);
// Metric --
((UnpooledByteBufAllocator) alloc()).decrementDirect(capacity);
}
}
```

- 在原先的基础上，调用 Metric 相应的增减操作方法，得以记录 Heap 占用内存的大小。

### []( "5.5.1 UnpooledUnsafeNoCleanerDirectByteBuf")5.5.1 UnpooledUnsafeNoCleanerDirectByteBuf

io.netty.buffer.UnpooledUnsafeNoCleanerDirectByteBuf
，继承 UnpooledUnsafeDirectByteBuf 类。代码如下：

```
class UnpooledUnsafeNoCleanerDirectByteBuf extends UnpooledUnsafeDirectByteBuf{
UnpooledUnsafeNoCleanerDirectByteBuf(ByteBufAllocator alloc, int initialCapacity, int maxCapacity) {
super(alloc, initialCapacity, maxCapacity);
}
@Override
protected ByteBuffer allocateDirect(int initialCapacity){
// 反射，直接创建 ByteBuffer 对象。并且该对象不带 Cleaner 对象
return PlatformDependent.allocateDirectNoCleaner(initialCapacity);
}
ByteBuffer reallocateDirect(ByteBuffer oldBuffer, int initialCapacity){
return PlatformDependent.reallocateDirectNoCleaner(oldBuffer, initialCapacity);
}
@Override
protected void freeDirect(ByteBuffer buffer){
// 直接释放 ByteBuffer 对象
PlatformDependent.freeDirectNoCleaner(buffer);
}
@Override
public ByteBuf capacity(int newCapacity){
checkNewCapacity(newCapacity);
int oldCapacity = capacity();
if (newCapacity == oldCapacity) {
return this;
}
// 重新分配 ByteBuf 对象
ByteBuffer newBuffer = reallocateDirect(buffer, newCapacity);
if (newCapacity < oldCapacity) {
if (readerIndex() < newCapacity) {
// 重置 writerIndex 为 newCapacity ，避免越界
if (writerIndex() > newCapacity) {
writerIndex(newCapacity);
}
} else {
// 重置 writerIndex 和 readerIndex 为 newCapacity ，避免越界
setIndex(newCapacity, newCapacity);
}
}
// 设置 ByteBuf 对象
setByteBuffer(newBuffer, false);
return this;
}
}
```

FROM [《Netty 源码分析（一） ByteBuf》](https://www.jianshu.com/p/b833254908f7)

和 UnpooledUnsafeDirectByteBuf 最大区别在于 UnpooledUnsafeNoCleanerDirectByteBuf 在 allocate 的时候通过反射构造函数的方式创建 DirectByteBuffer，这样在 DirectByteBuffer 中没有对应的 Cleaner 函数(通过 ByteBuffer.allocateDirect 的方式会自动生成 Cleaner 函数，Cleaner 用于内存回收，具体可以看源码)，内存回收时，UnpooledUnsafeDirectByteBuf 通过调用 DirectByteBuffer 中的 Cleaner 函数回收，而 UnpooledUnsafeNoCleanerDirectByteBuf 直接使用 UNSAFE.freeMemory(address)释放内存地址。

# []( "666. 彩蛋")666. 彩蛋

😈 小水文一篇。铺垫铺垫，你懂的。
