# 精尽 Netty 源码解析 —— Buffer 之 ByteBuf（三）内存泄露检测

# []( "1. 概述")1. 概述

在本文，我们来分享 Netty 的**内存泄露检测**的实现机制。考虑到胖友更好的理解本文，请先阅读江南白衣大大的 [《Netty 之有效规避内存泄漏》](http://calvin1978.blogcn.com/articles/netty-leak.html) 。

因为江南白衣大大在文章中，已经很清晰的讲解了概念与原理，笔者就不班门弄斧，直接上手，撸源码。

# []( "2. ReferenceCounted")2. ReferenceCounted

FROM [《【Netty 官方文档翻译】引用计数对象（reference counted objects）》](http://damacheng009.iteye.com/blog/2013657)

自从 Netty 4 开始，对象的生命周期由它们的引用计数( reference counts )管理，而不是由垃圾收集器( garbage collector )管理了。**ByteBuf 是最值得注意的，它使用了引用计数来改进分配内存和释放内存的性能**。

在 Netty 中，通过

io.netty.util.ReferenceCounted
**接口**，定义了引用计数相关的一系列操作。代码如下：

```
public interface ReferenceCounted{
//*/*
/* 获得引用计数
/*
/* Returns the reference count of this object. If {@code 0}, it means this object has been deallocated.
/*/
int refCnt();
//*/*
/* 增加引用计数 1
/*
/* Increases the reference count by {@code 1}.
/*/
ReferenceCounted retain();
//*/*
/* 增加引用计数 n
/*
/* Increases the reference count by the specified {@code increment}.
/*/
ReferenceCounted retain(int increment);
//*/*
/* 等价于调用 `/#touch(null)` 方法，即 hint 方法参数传递为 null 。
/*
/* Records the current access location of this object for debugging purposes.
/* If this object is determined to be leaked, the information recorded by this operation will be provided to you
/* via {@link ResourceLeakDetector}. This method is a shortcut to {@link /#touch(Object) touch(null)}.
/*/
ReferenceCounted touch();
//*/*
/* 出于调试目的,用一个额外的任意的(arbitrary)信息记录这个对象的当前访问地址. 如果这个对象被检测到泄露了, 这个操作记录的信息将通过ResourceLeakDetector 提供.
/*
/* Records the current access location of this object with an additional arbitrary information for debugging
/* purposes. If this object is determined to be leaked, the information recorded by this operation will be
/* provided to you via {@link ResourceLeakDetector}.
/*/
ReferenceCounted touch(Object hint);
//*/*
/* 减少引用计数 1 。
/* 当引用计数为 0 时，释放
/*
/* Decreases the reference count by {@code 1} and deallocates this object if the reference count reaches at
/* {@code 0}.
/*
/* @return {@code true} if and only if the reference count became {@code 0} and this object has been deallocated
/*/
boolean release();
//*/*
/* 减少引用计数 n 。
/* 当引用计数为 0 时，释放
/*
/* Decreases the reference count by the specified {@code decrement} and deallocates this object if the reference
/* count reaches at {@code 0}.
/*
/* @return {@code true} if and only if the reference count became {@code 0} and this object has been deallocated
/*/
boolean release(int decrement);
}
```

- /#refCnt()
  、

/#retain(...)
、

/#release(...)
三种方法比较好理解，对引用指数的获取与增减。

- /#touch(...)
  方法，主动记录一个

hint
给 ResourceLeakDetector ，方便我们在发现内存泄露有更多的信息进行排查。详细的，在下文 ResourceLeakDetector 相关的内容，具体来看。

ReferenceCounted 的直接子类 / 子接口有两个 ：

- io.netty.buffer.ByteBuf
  。所以，所有 ByteBuf 实现类，都支持引用计数的操作。
- io.netty.util.AbstractReferenceCounted
  ，ReferenceCounted 的抽象实现类。它的子类实现类，主要是除了 ByteBuf 之外，需要引用计数的操作的类。例如：AbstractHttpData、DefaultFileRegion 等等。

- AbstractReferenceCounted 不是本文的重点，就不多做介绍。
- AbstractReferenceCounted 的具体代码实现，在下文中，我们会看到和

io.netty.buffer.AbstractReferenceCountedByteBuf
基本差不多。

# []( "3. ByteBuf")3. ByteBuf

ByteBuf 虽然继承了 ReferenceCounted 接口，但是并未实现相应的方法。那么真正实现与相关的类，如下图所示：[![类图](http://static2.iocoder.cn/images/Netty/2018_08_07/01.png)](http://static2.iocoder.cn/images/Netty/2018_08_07/01.png '类图')类图

- 黄框

- AbstractReferenceCountedByteBuf ，实现引用计数的获取与增减的操作。
- 红框

- WrappedByteBuf ，实现对 ByteBuf 的装饰器实现类。
- WrappedCompositeByteBuf ，实现对 CompositeByteBuf 的装饰器实现类。
- 绿框

- SimpleLeakAwareByteBuf、SimpleLeakAwareCompositeByteBuf ，实现了

SIMPLE
级别的内存泄露检测。

- AdvancedLeakAwareByteBuf、AdvancedLeakAwareCompositeByteBuf ，实现了

ADVANCED
和

PARANOID
级别的内存泄露检测。

- 蓝筐

- UnreleasableByteBuf ，用于阻止他人对装饰的 ByteBuf 的销毁，避免被错误销毁掉。

因为带

"Composite"
类的代码实现，和不带的类( 例如 WrappedCompositeByteBuf 和 WrappedByteBuf )，实现代码基本一致，\*\*所以本文只分享不带

"Composite"
的类\*\*。

## []( "3.1 创建 LeakAware ByteBuf 对象")3.1 创建 LeakAware ByteBuf 对象

在前面的文章中，我们已经提到，ByteBufAllocator 可用于创建 ByteBuf 对象。创建的过程中，它会调用

/#toLeakAwareBuffer(...)
方法，将 ByteBuf **装饰**成 LeakAware ( 可检测内存泄露 )的 ByteBuf 对象，代码如下：

```
// AbstractByteBufAllocator.java
protected static ByteBuf toLeakAwareBuffer(ByteBuf buf){
ResourceLeakTracker<ByteBuf> leak;
switch (ResourceLeakDetector.getLevel()) {
case SIMPLE:
leak = AbstractByteBuf.leakDetector.track(buf);
if (leak != null) {
buf = new SimpleLeakAwareByteBuf(buf, leak);
}
break;
case ADVANCED:
case PARANOID:
leak = AbstractByteBuf.leakDetector.track(buf);
if (leak != null) {
buf = new AdvancedLeakAwareByteBuf(buf, leak);
}
break;
default:
break;
}
return buf;
}
protected static CompositeByteBuf toLeakAwareBuffer(CompositeByteBuf buf){
ResourceLeakTracker<ByteBuf> leak;
switch (ResourceLeakDetector.getLevel()) {
case SIMPLE:
leak = AbstractByteBuf.leakDetector.track(buf);
if (leak != null) {
buf = new SimpleLeakAwareCompositeByteBuf(buf, leak);
}
break;
case ADVANCED:
case PARANOID:
leak = AbstractByteBuf.leakDetector.track(buf);
if (leak != null) {
buf = new AdvancedLeakAwareCompositeByteBuf(buf, leak);
}
break;
default:
break;
}
return buf;
}
```

- 有两个

/#toLeakAwareBuffer(...)
方法，分别对应带

"Composite"
的 组合 ByteBuf 类，和不带

Composite
普通 ByteBuf 类。因为这个不同，所以前者创建的是 SimpleLeakAwareCompositeByteBuf / AdvancedLeakAwareCompositeByteBuf 对象，后者创建的是 SimpleLeakAwareByteBuf / AdvancedLeakAwareByteBuf 对象。

- 当然，从总的逻辑来看，是**一致**的：

- SIMPLE
  级别，创建 SimpleLeakAwareByteBuf 或 SimpleLeakAwareCompositeByteBuf 对象。
- ADVANCED
  和

PARANOID
级别，创建 AdvancedLeakAwareByteBuf 或者 AdvancedLeakAwareCompositeByteBuf 对象。

- 是否需要创建 LeakAware ByteBuf 对象，有一个前提，调用

ResourceLeakDetector/#track(ByteBuf)
方法，返回了 ResourceLeakTracker 对象。

- 虽然说，

ADVANCED
和

PARANOID
级别，都使用了 AdvancedLeakAwareByteBuf 或 AdvancedLeakAwareCompositeByteBuf 对象，但是它们的差异是：1)

PARANOID
级别，一定返回 ResourceLeakTracker 对象；2)

ADVANCED
级别，随机概率( 默认为

1%
左右 )返回 ResourceLeakTracker 对象。

- 关于

ResourceLeakDetector/#track(ByteBuf)
方法的实现，下文也会详细解析。

## []( "3.2 AbstractReferenceCountedByteBuf")3.2 AbstractReferenceCountedByteBuf

io.netty.buffer.AbstractReferenceCountedByteBuf
，实现引用计数的获取与增减的操作。

### []( "3.2.1 构造方法")3.2.1 构造方法

```
//*/*
/* {@link /#refCnt} 的更新器
/*/
private static final AtomicIntegerFieldUpdater<AbstractReferenceCountedByteBuf> refCntUpdater = AtomicIntegerFieldUpdater.newUpdater(AbstractReferenceCountedByteBuf.class, "refCnt");
//*/*
/* 引用计数
/*/
private volatile int refCnt;
protected AbstractReferenceCountedByteBuf(int maxCapacity){
// 设置最大容量
super(maxCapacity);
// 初始 refCnt 为 1
refCntUpdater.set(this, 1);
}
```

- 为什么

refCnt
不使用 AtomicInteger 呢？
计数器基于 AtomicIntegerFieldUpdater ，为什么不直接用 AtomicInteger ？因为 ByteBuf 对象很多，如果都把

int
包一层 AtomicInteger 花销较大，而 AtomicIntegerFieldUpdater 只需要一个全局的静态变量。

### []( "3.2.2 refCnt")3.2.2 refCnt

```
@Override
public int refCnt(){
return refCnt;
}
```

### []( "3.2.3 setRefCnt")3.2.3 setRefCnt

/#setRefCnt(int refCnt)
方法，直接修改

refCnt
。代码如下：

```
//*/*
/* An unsafe operation intended for use by a subclass that sets the reference count of the buffer directly
/*/
protected final void setRefCnt(int refCnt){
refCntUpdater.set(this, refCnt);
}
```

### []( "3.2.4 retain")3.2.4 retain

```
@Override
public ByteBuf retain(int increment){
return retain0(checkPositive(increment, "increment"));
}
private ByteBuf retain0(final int increment){
// 增加
int oldRef = refCntUpdater.getAndAdd(this, increment);
// 原有 refCnt 就是 <= 0 ；或者，increment 为负数
if (oldRef <= 0 || oldRef + increment < oldRef) {
// Ensure we don't resurrect (which means the refCnt was 0) and also that we encountered an overflow.
// 加回去，负负得正。
refCntUpdater.getAndAdd(this, -increment);
// 抛出 IllegalReferenceCountException 异常
throw new IllegalReferenceCountException(oldRef, increment);
}
return this;
}
```

### []( "3.2.5 release")3.2.5 release

```
@Override
public boolean release(){
return release0(1);
}
@Override
public boolean release(int decrement){
return release0(checkPositive(decrement, "decrement"));
}
@SuppressWarnings("Duplicates")
private boolean release0(int decrement){
// 减少
int oldRef = refCntUpdater.getAndAdd(this, -decrement);
// 原有 oldRef 等于减少的值
if (oldRef == decrement) {
// 释放
deallocate();
return true;
// 减少的值得大于 原有 oldRef ，说明“越界”；或者，increment 为负数
} else if (oldRef < decrement || oldRef - decrement > oldRef) {
// Ensure we don't over-release, and avoid underflow.
// 加回去，负负得正。
refCntUpdater.getAndAdd(this, decrement);
// 抛出 IllegalReferenceCountException 异常
throw new IllegalReferenceCountException(oldRef, -decrement);
}
return false;
}
```

- 当释放完成，即

refCnt
等于 0 时，调用

/#deallocate()
方法，进行**真正的释放**。这是个**抽象方法**，需要子类去实现。代码如下：

```
//*/*
/* Called once {@link /#refCnt()} is equals 0.
/*/
protected abstract void deallocate();
```

- 在 [《精尽 Netty 源码解析 —— Buffer 之 ByteBuf（二）核心子类》](http://svip.iocoder.cn/Netty/ByteBuf-1-2-ByteBuf-core-impl/) 中，可以看到各种 ByteBuf 对

/#deallocate()
方法的实现。

### []( "3.2.6 touch")3.2.6 touch

```
@Override
public ByteBuf touch(){
return this;
}
@Override
public ByteBuf touch(Object hint){
return this;
}
```

一脸懵逼？！实际 AbstractReferenceCountedByteBuf **并未**实现

/#touch(...)
方法。而是在 AdvancedLeakAwareByteBuf 中才实现。

## []( "3.3 SimpleLeakAwareByteBuf")3.3 SimpleLeakAwareByteBuf

io.netty.buffer.SimpleLeakAwareByteBuf
，继承 WrappedByteBuf 类，

Simple
级别的 LeakAware ByteBuf 实现类。

### []( "3.3.1 构造方法")3.3.1 构造方法

```
//*/*
/* 关联的 ByteBuf 对象
/*
/* This object's is associated with the {@link ResourceLeakTracker}. When {@link ResourceLeakTracker/#close(Object)}
/* is called this object will be used as the argument. It is also assumed that this object is used when
/* {@link ResourceLeakDetector/#track(Object)} is called to create {@link /#leak}.
/*/
private final ByteBuf trackedByteBuf;
//*/*
/* ResourceLeakTracker 对象
/*/
final ResourceLeakTracker<ByteBuf> leak;
SimpleLeakAwareByteBuf(ByteBuf wrapped, ByteBuf trackedByteBuf, ResourceLeakTracker<ByteBuf> leak) { // <2>
super(wrapped);
this.trackedByteBuf = ObjectUtil.checkNotNull(trackedByteBuf, "trackedByteBuf");
this.leak = ObjectUtil.checkNotNull(leak, "leak");
}
SimpleLeakAwareByteBuf(ByteBuf wrapped, ResourceLeakTracker<ByteBuf> leak) { // <1>
this(wrapped, wrapped, leak);
}
```

- leak
  属性，ResourceLeakTracker 对象。
- trackedByteBuf
  属性，**真正**关联

leak
的 ByteBuf 对象。

- 对于构造方法

<1>
，

wrapped
和

trackedByteBuf
**相同**。

- 对于构造方法

<2>
，

wrapped
和

trackedByteBuf
**一般不同**。

- 有点难理解？继续往下看。

### []( "3.3.2 slice")3.3.2 slice

```
@Override
public ByteBuf slice(){
return newSharedLeakAwareByteBuf(super.slice());
}
@Override
public ByteBuf slice(int index, int length){
return newSharedLeakAwareByteBuf(super.slice(index, length));
}
```

- 首先，调用**父**

/#slice(...)
方法，获得 **slice** ByteBuf 对象。

- 之后，因为 **slice** ByteBuf 对象，并不是一个 LeakAware 的 ByteBuf 对象。所以调用

/#newSharedLeakAwareByteBuf(ByteBuf wrapped)
方法，装饰成 LeakAware 的 ByteBuf 对象。代码如下：

```
private SimpleLeakAwareByteBuf newSharedLeakAwareByteBuf(ByteBuf wrapped){
return newLeakAwareByteBuf(wrapped, trackedByteBuf //*/* <1> /*/*/, leak);
}
protected SimpleLeakAwareByteBuf newLeakAwareByteBuf(ByteBuf buf, ByteBuf trackedByteBuf, ResourceLeakTracker<ByteBuf> leakTracker){
return new SimpleLeakAwareByteBuf(buf, trackedByteBuf //*/* <1> /*/*/, leakTracker);
}
```

- 从

<1>
处，我们可以看到，

trackedByteBuf
代表的是**原始的** ByteBuf 对象，它是跟

leak
真正进行关联的。而

wrapped
则不是。

在 SimpleLeakAwareByteBuf 中，还有如下方法，和

/#slice(...)
方法是**类似**的，在调用完**父**对应的方法后，再调用

/#newSharedLeakAwareByteBuf(ByteBuf wrapped)
方法，装饰成 LeakAware 的 ByteBuf 对象。整理如下：

```
@Override
public ByteBuf duplicate(){
return newSharedLeakAwareByteBuf(super.duplicate());
}
@Override
public ByteBuf readSlice(int length){
return newSharedLeakAwareByteBuf(super.readSlice(length));
}
@Override
public ByteBuf asReadOnly(){
return newSharedLeakAwareByteBuf(super.asReadOnly());
}
@Override
public ByteBuf order(ByteOrder endianness){
if (order() == endianness) {
return this;
} else {
return newSharedLeakAwareByteBuf(super.order(endianness));
}
}
```

### []( "3.3.3 retainedSlice")3.3.3 retainedSlice

```
@Override
public ByteBuf retainedSlice(){
return unwrappedDerived(super.retainedSlice());
}
@Override
public ByteBuf retainedSlice(int index, int length){
return unwrappedDerived(super.retainedSlice(index, length));
}
```

- 首先，调用**父**

/#retainedSlice(...)
方法，获得 **slice** ByteBuf 对象，引用计数加 1。

- 之后，因为 **slice** ByteBuf 对象，并不是一个 LeakAware 的 ByteBuf 对象。所以调用

/#unwrappedDerived(ByteBuf wrapped)
方法，装饰成 LeakAware 的 ByteBuf 对象。代码如下：

```
// TODO 芋艿，看不懂 1017
private ByteBuf unwrappedDerived(ByteBuf derived){
// We only need to unwrap SwappedByteBuf implementations as these will be the only ones that may end up in
// the AbstractLeakAwareByteBuf implementations beside slices / duplicates and "real" buffers.
ByteBuf unwrappedDerived = unwrapSwapped(derived);
if (unwrappedDerived instanceof AbstractPooledDerivedByteBuf) {
// Update the parent to point to this buffer so we correctly close the ResourceLeakTracker.
((AbstractPooledDerivedByteBuf) unwrappedDerived).parent(this);
ResourceLeakTracker<ByteBuf> newLeak = AbstractByteBuf.leakDetector.track(derived);
if (newLeak == null) {
// No leak detection, just return the derived buffer.
return derived;
}
return newLeakAwareByteBuf(derived, newLeak);
}
return newSharedLeakAwareByteBuf(derived);
}
@SuppressWarnings("deprecation")
private static ByteBuf unwrapSwapped(ByteBuf buf){
if (buf instanceof SwappedByteBuf) {
do {
buf = buf.unwrap();
} while (buf instanceof SwappedByteBuf);
return buf;
}
return buf;
}
private SimpleLeakAwareByteBuf newLeakAwareByteBuf(ByteBuf wrapped, ResourceLeakTracker<ByteBuf> leakTracker){
return newLeakAwareByteBuf(wrapped, wrapped, leakTracker);
}
```

- TODO 1017

在 SimpleLeakAwareByteBuf 中，还有如下方法，和

/#retainedSlice(...)
方法是**类似**的，在调用完**父**对应的方法后，再调用

/#unwrappedDerived(ByteBuf derived)
方法，装饰成 LeakAware 的 ByteBuf 对象。整理如下：

```
@Override
public ByteBuf retainedDuplicate(){
return unwrappedDerived(super.retainedDuplicate());
}
@Override
public ByteBuf readRetainedSlice(int length){
return unwrappedDerived(super.readRetainedSlice(length));
}
```

### []( "3.3.4 release")3.3.4 release

```
@Override
public boolean release(){
if (super.release()) { // 释放完成
closeLeak();
return true;
}
return false;
}
@Override
public boolean release(int decrement){
if (super.release(decrement)) { // 释放完成
closeLeak();
return true;
}
return false;
}
```

- 在调用**父**

/#release(...)
方法，释放完成后，会调用

/#closeLeak()
方法，关闭 ResourceLeakTracker 。代码如下：

```
private void closeLeak(){
// Close the ResourceLeakTracker with the tracked ByteBuf as argument. This must be the same that was used when
// calling DefaultResourceLeak.track(...).
boolean closed = leak.close(trackedByteBuf);
assert closed;
}
```

```
/* 进一步的详细解析，可以看看 [「5.1.5 close」](/#) 。
```

### []( "3.3.5 touch")3.3.5 touch

```
@Override
public ByteBuf touch(){
return this;
}
@Override
public ByteBuf touch(Object hint){
return this;
}
```

又一脸懵逼？！实际 SimpleLeakAwareByteBuf **也并未**实现

/#touch(...)
方法。而是在 AdvancedLeakAwareByteBuf 中才实现。

## []( "3.4 AdvancedLeakAwareByteBuf")3.4 AdvancedLeakAwareByteBuf

io.netty.buffer.AdvancedLeakAwareByteBuf
，继承 SimpleLeakAwareByteBuf 类，

ADVANCED
和

PARANOID
级别的 LeakAware ByteBuf 实现类。

### []( "3.4.1 构造方法")3.4.1 构造方法

```
AdvancedLeakAwareByteBuf(ByteBuf buf, ResourceLeakTracker<ByteBuf> leak) {
super(buf, leak);
}
AdvancedLeakAwareByteBuf(ByteBuf wrapped, ByteBuf trackedByteBuf, ResourceLeakTracker<ByteBuf> leak) {
super(wrapped, trackedByteBuf, leak);
}
```

就是调用父构造方法，没啥特点。

### []( "3.4.2 retain")3.4.2 retain

```
@Override
public ByteBuf retain(){
leak.record();
return super.retain();
}
@Override
public ByteBuf retain(int increment){
leak.record();
return super.retain(increment);
}
```

- 会调用

ResourceLeakTracer/#record()
方法，记录信息。

### []( "3.4.3 release")3.4.3 release

```
@Override
public boolean release(){
leak.record();
return super.release();
}
@Override
public boolean release(int decrement){
leak.record();
return super.release(decrement);
}
```

- 会调用

ResourceLeakTracer/#record()
方法，记录信息。

### []( "3.4.4 touch")3.4.4 touch

```
@Override
public ByteBuf touch(){
leak.record();
return this;
}
@Override
public ByteBuf touch(Object hint){
leak.record(hint);
return this;
}
```

- 会调用

ResourceLeakTracer/#record(...)
方法，记录信息。

- 😈

/#touch(...)
方法，终于实现了，哈哈哈。

### []( "3.4.5 recordLeakNonRefCountingOperation")3.4.5 recordLeakNonRefCountingOperation

/#recordLeakNonRefCountingOperation(ResourceLeakTracker<ByteBuf> leak)
**静态**方法，除了引用计数操作相关( 即

/#retain(...)
/

/#release(...)
/

/#touch(...)
方法 )方法外，是否要调用记录信息。代码如下：

```
private static final String PROP_ACQUIRE_AND_RELEASE_ONLY = "io.netty.leakDetection.acquireAndReleaseOnly";
//*/*
/* 默认为
/*/
private static final boolean ACQUIRE_AND_RELEASE_ONLY;
static {
ACQUIRE_AND_RELEASE_ONLY = SystemPropertyUtil.getBoolean(PROP_ACQUIRE_AND_RELEASE_ONLY, false);
}
static void recordLeakNonRefCountingOperation(ResourceLeakTracker<ByteBuf> leak){
if (!ACQUIRE_AND_RELEASE_ONLY) {
leak.record();
}
}
```

- 负负得正，所以会调用

ResourceLeakTracer/#record(...)
方法，记录信息。

- 也就是说，ByteBuf 的所有方法，都会记录信息。例如：

```
@Override
public ByteBuf order(ByteOrder endianness){
recordLeakNonRefCountingOperation(leak);
return super.order(endianness);
}
@Override
public int readIntLE(){
recordLeakNonRefCountingOperation(leak);
return super.readIntLE();
}
```

- 方法比较多，就不一一列举了。

### []( "3.4.6 newLeakAwareByteBuf")3.4.6 newLeakAwareByteBuf

/#newLeakAwareByteBuf(ByteBuf buf, ByteBuf trackedByteBuf, ResourceLeakTracker<ByteBuf> leakTracker)
方法，覆写父类方法，将原先装饰成 SimpleLeakAwareByteBuf 改成 AdvancedLeakAwareByteBuf 对象。代码如下:

```
@Override
protected AdvancedLeakAwareByteBuf newLeakAwareByteBuf(
ByteBuf buf, ByteBuf trackedByteBuf, ResourceLeakTracker<ByteBuf> leakTracker){
return new AdvancedLeakAwareByteBuf(buf, trackedByteBuf, leakTracker);
}
```

## []( "3.5 UnreleasableByteBuf")3.5 UnreleasableByteBuf

io.netty.buffer.UnreleasableByteBuf
，继承 WrappedByteBuf 类，用于阻止他人对装饰的 ByteBuf 的销毁，避免被错误销毁掉。

它的实现方法比较简单，主要是两大点：

- 引用计数操作相关( 即

/#retain(...)
/

/#release(...)
/

/#touch(...)
方法 )方法，不进行调用。代码如下：

```
@Override
public ByteBuf retain(int increment){
return this;
}
@Override
public ByteBuf retain(){
return this;
}
@Override
public ByteBuf touch(){
return this;
}
@Override
public ByteBuf touch(Object hint){
return this;
}
@Override
public boolean release(){
return false;
}
@Override
public boolean release(int decrement){
return false;
}
```

- 拷贝操作相关方法，都会在包一层 UnreleasableByteBuf 对象。例如：

```
@Override
public ByteBuf slice(){
return new UnreleasableByteBuf(buf.slice());
}
```

# []( "4. ResourceLeakDetector")4. ResourceLeakDetector

io.netty.util.ResourceLeakDetector
，内存泄露检测器。
老艿艿：Resource 翻译成“资源”更合理。考虑到标题叫做《内存泄露检测》，包括互联网其他作者在关于这块内容的命名，也是叫做“内存泄露检测”。所以，在下文，Resource 笔者还是继续翻译成“资源”。

ResourceLeakDetector 为了检测内存是否泄漏，使用了 WeakReference( 弱引用 )和 ReferenceQueue( 引用队列 )，过程如下：

1. 根据检测级别和采样率的设置，在需要时为需要检测的 ByteBuf 创建 WeakReference 引用。
1. 当 JVM 回收掉 ByteBuf 对象时，JVM 会将 WeakReference 放入 ReferenceQueue 队列中。
1. 通过对 ReferenceQueue 中 WeakReference 的检查，判断在 GC 前是否有释放 ByteBuf 的资源，就可以知道是否有资源释放。

😈 看不太懂？继续往下看代码，在回过头来理解理解。

## []( "4.1 静态属性")4.1 静态属性

```
private static final String PROP_LEVEL_OLD = "io.netty.leakDetectionLevel";
private static final String PROP_LEVEL = "io.netty.leakDetection.level";
//*/*
/* 默认内存检测级别
/*/
private static final Level DEFAULT_LEVEL = Level.SIMPLE;
private static final String PROP_TARGET_RECORDS = "io.netty.leakDetection.targetRecords";
private static final int DEFAULT_TARGET_RECORDS = 4;
//*/*
/* 每个 DefaultResourceLeak 记录的 Record 数量
/*/
private static final int TARGET_RECORDS;
//*/*
/* 内存检测级别枚举
/*
/* Represents the level of resource leak detection.
/*/
public enum Level {
//*/*
/* Disables resource leak detection.
/*/
DISABLED,
//*/*
/* Enables simplistic sampling resource leak detection which reports there is a leak or not,
/* at the cost of small overhead (default).
/*/
SIMPLE,
//*/*
/* Enables advanced sampling resource leak detection which reports where the leaked object was accessed
/* recently at the cost of high overhead.
/*/
ADVANCED,
//*/*
/* Enables paranoid resource leak detection which reports where the leaked object was accessed recently,
/* at the cost of the highest possible overhead (for testing purposes only).
/*/
PARANOID;
//*/*
/* Returns level based on string value. Accepts also string that represents ordinal number of enum.
/*
/* @param levelStr - level string : DISABLED, SIMPLE, ADVANCED, PARANOID. Ignores case.
/* @return corresponding level or SIMPLE level in case of no match.
/*/
static Level parseLevel(String levelStr){
String trimmedLevelStr = levelStr.trim();
for (Level l : values()) {
if (trimmedLevelStr.equalsIgnoreCase(l.name()) || trimmedLevelStr.equals(String.valueOf(l.ordinal()))) {
return l;
}
}
return DEFAULT_LEVEL;
}
}
//*/*
/* 内存泄露检测等级
/*/
private static Level level;
//*/*
/* 默认采集频率
/*/
// There is a minor performance benefit in TLR if this is a power of 2.
static final int DEFAULT_SAMPLING_INTERVAL = 128;
1: static {
2: // 获得是否禁用泄露检测
3: final boolean disabled;
4: if (SystemPropertyUtil.get("io.netty.noResourceLeakDetection") != null) {
5: disabled = SystemPropertyUtil.getBoolean("io.netty.noResourceLeakDetection", false);
6: logger.debug("-Dio.netty.noResourceLeakDetection: {}", disabled);
7: logger.warn("-Dio.netty.noResourceLeakDetection is deprecated. Use '-D{}={}' instead.", PROP_LEVEL, DEFAULT_LEVEL.name().toLowerCase());
8: } else {
9: disabled = false;
10: }
11:
12: // 获得默认级别
13: Level defaultLevel = disabled? Level.DISABLED : DEFAULT_LEVEL;
14: // 获得配置的级别字符串，从老版本的配置
15: // First read old property name (兼容老版本）
16: String levelStr = SystemPropertyUtil.get(PROP_LEVEL_OLD, defaultLevel.name());
17: // 获得配置的级别字符串，从新版本的配置
18: // If new property name is present, use it
19: levelStr = SystemPropertyUtil.get(PROP_LEVEL, levelStr);
20: // 获得最终的级别
21: Level level = Level.parseLevel(levelStr);
22: // 设置最终的级别
23: ResourceLeakDetector.level = level;
24:
25: // 初始化 TARGET_RECORDS
26: TARGET_RECORDS = SystemPropertyUtil.getInt(PROP_TARGET_RECORDS, DEFAULT_TARGET_RECORDS);
27:
28: if (logger.isDebugEnabled()) {
29: logger.debug("-D{}: {}", PROP_LEVEL, level.name().toLowerCase());
30: logger.debug("-D{}: {}", PROP_TARGET_RECORDS, TARGET_RECORDS);
31: }
32: }
```

- level
  **静态**属性，内存泄露等级。😈 不是说好了，静态变量要统一大写么。

- 默认级别为

DEFAULT_LEVEL = Level.SIMPLE
。

- 在 Level 中，枚举了四个级别。
- 禁用（DISABLED） - 完全禁止泄露检测，省点消耗。
- 简单（SIMPLE） - 默认等级，告诉我们取样的 1%的 ByteBuf 是否发生了泄露，但总共一次只打印一次，看不到就没有了。
- 高级（ADVANCED） - 告诉我们取样的 1%的 ByteBuf 发生泄露的地方。每种类型的泄漏（创建的地方与访问路径一致）只打印一次。对性能有影响。
- 偏执（PARANOID） - 跟高级选项类似，但此选项检测所有 ByteBuf，而不仅仅是取样的那 1%。对性能有绝大的影响。

- 看着有点懵逼？下面继续看代码。
- 在【第 2 至 23 行】的代码进行初始化。
- TARGET_RECORDS
  静态属性，每个 DefaultResourceLeak 记录的 Record 数量。

- 默认大小为

DEFAULT_TARGET_RECORDS = 4
。

- 在【第 26 行】的代码进行初始化。
- DEFAULT_SAMPLING_INTERVAL
  静态属性，默认采集频率，128 。

## []( "4.2 构造方法")4.2 构造方法

```
//*/*
/* DefaultResourceLeak 集合
/*
/* the collection of active resources
/*/
private final ConcurrentMap<DefaultResourceLeak<?>, LeakEntry> allLeaks = PlatformDependent.newConcurrentHashMap();
//*/*
/* 引用队列
/*/
private final ReferenceQueue<Object> refQueue = new ReferenceQueue<Object>();
//*/*
/* 已汇报的内存泄露的资源类型的集合
/*/
private final ConcurrentMap<String, Boolean> reportedLeaks = PlatformDependent.newConcurrentHashMap();
//*/*
/* 资源类型
/*/
private final String resourceType;
//*/*
/* 采集评率
/*/
private final int samplingInterval;
public ResourceLeakDetector(Class<?> resourceType, int samplingInterval){
this(simpleClassName(resourceType) //*/* <1> /*/*/, samplingInterval, Long.MAX_VALUE);
}
```

- allLeaks
  属性，DefaultResourceLeak 集合。因为 Java 没有自带的 ConcurrentSet ，所以只好使用使用 ConcurrentMap 。也就是说，value 属性实际没有任何用途。

- 关于 LeakEntry ，可以看下 [「6. LeakEntry」]() 。
- refQueue
  属性，就是我们提到的**引用队列**( ReferenceQueue 队列 )。
- reportedLeaks
  属性，已汇报的内存泄露的资源类型的集合。
- resourceType
  属性，资源类型，使用资源类的类名简写，见

<1>
处。

- samplingInterval
  属性，采集频率。

在 AbstractByteBuf 类中，我们可以看到创建了所有 ByteBuf 对象统一使用的 ResourceLeakDetector 对象。代码如下：

```
static final ResourceLeakDetector<ByteBuf> leakDetector = ResourceLeakDetectorFactory.instance().newResourceLeakDetector(ByteBuf.class);
```

- ResourceLeakDetector 的创建，通过

io.netty.util.ResourceLeakDetectorFactory
，基于工厂模式的方式来创建。

- 关于 ResourceLeakDetectorFactory 的代码比较简单，笔者就不赘述了。
- 有一点要注意的是，可以通过

"io.netty.customResourceLeakDetector"
来**自定义** ResourceLeakDetector 的实现类。当然，绝大多数场景是完全不需要的。

## []( "4.3 track")4.3 track

/#track(...)
方法，给指定资源( 例如 ByteBuf 对象 )创建一个检测它是否泄漏的 ResourceLeakTracker 对象。代码如下：

```
1: public final ResourceLeakTracker<T> track(T obj){
2: return track0(obj);
3: }
4:
5: @SuppressWarnings("unchecked")
6: private DefaultResourceLeak track0(T obj){
7: Level level = ResourceLeakDetector.level;
8: // DISABLED 级别，不创建
9: if (level == Level.DISABLED) {
10: return null;
11: }
12:
13: // SIMPLE 和 ADVANCED
14: if (level.ordinal() < Level.PARANOID.ordinal()) {
15: // 随机
16: if ((PlatformDependent.threadLocalRandom().nextInt(samplingInterval)) == 0) {
17: // 汇报内存是否泄漏
18: reportLeak();
19: // 创建 DefaultResourceLeak 对象
20: return new DefaultResourceLeak(obj, refQueue, allLeaks);
21: }
22: return null;
23: }
24:
25: // PARANOID 级别
26: // 汇报内存是否泄漏
27: reportLeak();
28: // 创建 DefaultResourceLeak 对象
29: return new DefaultResourceLeak(obj, refQueue, allLeaks);
30: }
```

- 第 8 至 11 行：

DISABLED
级别时，不创建，直接返回

null
。

- 第 13 至 23 行：

SIMPLE
和

ADVANCED
级别时，随机，概率为

1 / samplingInterval
，创建 DefaultResourceLeak 对象。默认情况下

samplingInterval = 128
，约等于

1%
，这也是就为什么说“告诉我们取样的 1% 的 ByteBuf 发生泄露的地方”。

- 第 27 至 29 行：

PARANOID
级别时，一定创建 DefaultResourceLeak 对象。这也是为什么说“对性能有绝大的影响”。

- 第 18 至 27 行：笔者原本以为，ResourceLeakDetector 会有一个定时任务，不断检测是否有内存泄露。从这里的代码来看，它是在每次一次创建 DefaultResourceLeak 对象时，调用

/#reportLeak()
方法，汇报内存是否泄漏。详细解析，见 [「4.4 reportLeak」]() 。

## []( "4.4 reportLeak")4.4 reportLeak

/#reportLeak()
方法，检测是否有内存泄露。若有，则进行汇报。代码如下：

```
1: private void reportLeak(){
2: // 如果不允许打印错误日志，则无法汇报，清理队列，并直接结束。
3: if (!logger.isErrorEnabled()) {
4: // 清理队列
5: clearRefQueue();
6: return;
7: }
8:
9: // 循环引用队列，直到为空
10: // Detect and report previous leaks.
11: for (;;) {
12: @SuppressWarnings("unchecked")
13: DefaultResourceLeak ref = (DefaultResourceLeak) refQueue.poll();
14: if (ref == null) {
15: break;
16: }
17:
18: // 清理，并返回是否内存泄露
19: if (!ref.dispose()) {
20: continue;
21: }
22:
23: // 获得 Record 日志
24: String records = ref.toString();
25: // 相同 Record 日志，只汇报一次
26: if (reportedLeaks.putIfAbsent(records, Boolean.TRUE) == null) {
27: if (records.isEmpty()) {
28: reportUntracedLeak(resourceType);
29: } else {
30: reportTracedLeak(resourceType, records);
31: }
32: }
33: }
34: }
```

- 第 2 至 7 行：如果不允许打印错误日志，则无法汇报，因此调用

/#clearRefQueue()
方法，清理队列，并直接结束。详细解析，见 [「4.5 clearRefQueue」]() 。

- 第 9 至 16 行：循环引用队列

refQueue
，直到为空。

- 第 18 至 21 行：调用

DefaultResourceLeak/#dispose()
方法，清理，并返回是否内存泄露。如果未泄露，就直接

continue
。详细解析，见 [「5.1.3 dispose」]() 。

- 第 24 行：调用

DefaultResourceLeak/#toString()
方法，获得 Record 日志。详细解析，见 [「5.1 DefaultResourceLeak」]() 。

- 第 25 至 32 行：相同 Record 日志内容( 即“创建的地方与访问路径一致” )，**只汇报一次**。 代码如下：

```
//*/*
/* This method is called when a traced leak is detected. It can be overridden for tracking how many times leaks
/* have been detected.
/*/
protected void reportTracedLeak(String resourceType, String records){
logger.error(
"LEAK: {}.release() was not called before it's garbage-collected. " +
"See http://netty.io/wiki/reference-counted-objects.html for more information.{}",
resourceType, records);
}
//*/*
/* This method is called when an untraced leak is detected. It can be overridden for tracking how many times leaks
/* have been detected.
/*/
protected void reportUntracedLeak(String resourceType){
logger.error("LEAK: {}.release() was not called before it's garbage-collected. " +
"Enable advanced leak reporting to find out where the leak occurred. " +
"To enable advanced leak reporting, " +
"specify the JVM option '-D{}={}' or call {}.setLevel() " +
"See http://netty.io/wiki/reference-counted-objects.html for more information.",
resourceType, PROP_LEVEL, Level.ADVANCED.name().toLowerCase(), simpleClassName(this));
}
```

😈 这块逻辑的信息量，可能有点大，胖友可以看完 [「5. ResourceLeakTracker」]() ，再回过头理解下。

## []( "4.5 clearRefQueue")4.5 clearRefQueue

/#clearRefQueue()
方法，清理队列。代码如下：

```
private void clearRefQueue(){
for (;;) {
@SuppressWarnings("unchecked")
DefaultResourceLeak ref = (DefaultResourceLeak) refQueue.poll();
if (ref == null) {
break;
}
// 清理，并返回是否内存泄露
ref.dispose();
}
}
```

- 实际上，就是

/#reportLeak()
方法的**不汇报内存泄露**的版本。

# []( "5. ResourceLeakTracker")5. ResourceLeakTracker

io.netty.util.ResourceLeakTracker
，内存泄露追踪器接口。从 [「4.3 track」]() 中，我们已经看到，每个资源( 例如：ByteBuf 对象 )，会创建一个追踪它是否内存泄露的 ResourceLeakTracker 对象。

接口方法定义如下：

```
public interface ResourceLeakTracker<T>{
//*/*
/* 记录
/*
/* Records the caller's current stack trace so that the {@link ResourceLeakDetector} can tell where the leaked
/* resource was accessed lastly. This method is a shortcut to {@link /#record(Object) record(null)}.
/*/
void record();
//*/*
/* 记录
/*
/* Records the caller's current stack trace and the specified additional arbitrary information
/* so that the {@link ResourceLeakDetector} can tell where the leaked resource was accessed lastly.
/*/
void record(Object hint);
//*/*
/* 关闭
/*
/* Close the leak so that {@link ResourceLeakTracker} does not warn about leaked resources.
/* After this method is called a leak associated with this ResourceLeakTracker should not be reported.
/*
/* @return {@code true} if called first time, {@code false} if called already
/*/
boolean close(T trackedObject);
}
```

- /#record(...)
  方法，出于调试目的，用一个额外的任意的( arbitrary )信息记录这个对象的当前访问地址。如果这个对象被检测到泄露了, 这个操作记录的信息将通过 ResourceLeakDetector 提供。实际上，就是

ReferenceCounted/#touch(...)
方法，会调用

/#record(...)
方法。

- /#close(T trackedObject)
  方法，关闭 ResourceLeakTracker 。如果资源( 例如：ByteBuf 对象 )被正确释放，则会调用

/#close(T trackedObject)
方法，关闭 ResourceLeakTracker ，从而结束追踪。这样，在

ResourceLeakDetector/#reportLeak()
方法，就不会提示该资源泄露。

## []( "4.6 addExclusions")4.6 addExclusions

/#addExclusions(Class clz, String ... methodNames)
方法，添加忽略方法的集合。代码如下：

```
//*/*
/* 忽略的方法集合
/*/
private static final AtomicReference<String[]> excludedMethods = new AtomicReference<String[]>(EmptyArrays.EMPTY_STRINGS);
public static void addExclusions(Class clz, String ... methodNames){
Set<String> nameSet = new HashSet<String>(Arrays.asList(methodNames));
// Use loop rather than lookup. This avoids knowing the parameters, and doesn't have to handle
// NoSuchMethodException.
for (Method method : clz.getDeclaredMethods()) {
if (nameSet.remove(method.getName()) && nameSet.isEmpty()) {
break;
}
}
if (!nameSet.isEmpty()) {
throw new IllegalArgumentException("Can't find '" + nameSet + "' in " + clz.getName());
}
String[] oldMethods;
String[] newMethods;
do {
oldMethods = excludedMethods.get();
newMethods = Arrays.copyOf(oldMethods, oldMethods.length + 2 /* methodNames.length);
for (int i = 0; i < methodNames.length; i++) {
newMethods[oldMethods.length + i /* 2] = clz.getName();
newMethods[oldMethods.length + i /* 2 + 1] = methodNames[i];
}
} while (!excludedMethods.compareAndSet(oldMethods, newMethods));
}
```

- 代码比较简单，胖友自己理解。
- 具体的用途，可参见 [「7. Record」]() 的

/#toString()
方法。

- 目前调用该静态方法的有如下几处：

```
// AbstractByteBufAllocator.java
static {
ResourceLeakDetector.addExclusions(AbstractByteBufAllocator.class, "toLeakAwareBuffer");
}
// AdvancedLeakAwareByteBuf.java
static {
ResourceLeakDetector.addExclusions(AdvancedLeakAwareByteBuf.class, "touch", "recordLeakNonRefCountingOperation");
}
// ReferenceCountUtil.java
static {
ResourceLeakDetector.addExclusions(ReferenceCountUtil.class, "touch");
}
```

## []( "5.1 DefaultResourceLeak")5.1 DefaultResourceLeak

DefaultResourceLeak ，继承

java.lang.ref.WeakReference
类，实现 ResourceLeakTracker 接口，默认 ResourceLeakTracker 实现类。同时，它是 ResourceLeakDetector 内部静态类。即：

```
// ... 简化无关代码
public class ResourceLeakDetector<T>{
private static final class DefaultResourceLeak<T> extends WeakReference<Object> implements ResourceLeakTracker<T>, ResourceLeak{
}
}
```

那么为什么要继承

java.lang.ref.WeakReference
类呢？在 [「5.1.1 构造方法」]() 见分晓。

### []( "5.1.1 构造方法")5.1.1 构造方法

```
//*/*
/* {@link /#head} 的更新器
/*/
@SuppressWarnings("unchecked") // generics and updaters do not mix.
private static final AtomicReferenceFieldUpdater<DefaultResourceLeak<?>, Record> headUpdater =
(AtomicReferenceFieldUpdater)
AtomicReferenceFieldUpdater.newUpdater(DefaultResourceLeak.class, Record.class, "head");
//*/*
/* {@link /#droppedRecords} 的更新器
/*/
@SuppressWarnings("unchecked") // generics and updaters do not mix.
private static final AtomicIntegerFieldUpdater<DefaultResourceLeak<?>> droppedRecordsUpdater =
(AtomicIntegerFieldUpdater)
AtomicIntegerFieldUpdater.newUpdater(DefaultResourceLeak.class, "droppedRecords");
//*/*
/* Record 链的头节点
/*
/* 看完 {@link /#record()} 方法后，实际上，head 是尾节点，即最后( 新 )的一条 Record 。
/*/
@SuppressWarnings("unused")
private volatile Record head;
//*/*
/* 丢弃的 Record 计数
/*/
@SuppressWarnings("unused")
private volatile int droppedRecords;
//*/*
/* DefaultResourceLeak 集合。来自 {@link ResourceLeakDetector/#allLeaks}
/*/
private final ConcurrentMap<DefaultResourceLeak<?>, LeakEntry> allLeaks;
//*/*
/* hash 值
/*
/* 保证 {@link /#close(Object)} 传入的对象，就是 {@link /#referent} 对象
/*/
private final int trackedHash;
1: DefaultResourceLeak(
2: Object referent,
3: ReferenceQueue<Object> refQueue,
4: ConcurrentMap<DefaultResourceLeak<?>, LeakEntry> allLeaks) {
5: // 父构造方法 <1>
6: super(referent, refQueue);
7:
8: assert referent != null;
9:
10: // Store the hash of the tracked object to later assert it in the close(...) method.
11: // It's important that we not store a reference to the referent as this would disallow it from
12: // be collected via the WeakReference.
13: trackedHash = System.identityHashCode(referent);
14: allLeaks.put(this, LeakEntry.INSTANCE);
15: // Create a new Record so we always have the creation stacktrace included.
16: headUpdater.set(this, new Record(Record.BOTTOM));
17: this.allLeaks = allLeaks;
18: }
```

- head
  属性，Record 链的头节点。

- 为什么说它是链呢？详细解析，胖友可以先跳到 [「7. Record」]() 。
- 实际上，

head
是尾节点，即最后( 新 )的一条 Record 记录。详细解析，见 [「5.1.2 record」]() 。

- 在【第 16 行】代码，会默认创建尾节点

Record.BOTTOM
。

- droppedRecords
  属性，丢弃的 Record 计数。详细解析，见 [「5.1.2 record」]() 。
- allLeaks
  属性，DefaultResourceLeak 集合。来自

ResourceLeakDetector.allLeaks
属性。

- 在【第 14 行】代码，会将自己添加到

allLeaks
中。

- trackedHash
  属性，hash 值。保证在

/#close(T trackedObject)
方法，传入的对象，就是

referent
属性，即就是 DefaultResourceLeak 指向的资源( 例如：ByteBuf 对象 )。详细解析，见 [「5.1.4 close」]() 。

- 在【第 10 至 13 行】代码，计算并初始化

trackedHash
属性。

- 【重要】在

<1>
处，会将

referent
( 资源，例如：ByteBuf 对象 )和

refQueue
( 引用队列 )传入父 WeakReference 构造方法。
FROM [《译文：理解 Java 中的弱引用》](https://droidyue.com/blog/2014/10/12/understanding-weakreference-in-java/index.html)

**引用队列(Reference Queue)**

一旦弱引用对象开始返回 null，该弱引用指向的对象就被标记成了垃圾。而这个弱引用对象（非其指向的对象）就没有什么用了。通常这时候需要进行一些清理工作。比如 WeakHashMap 会在这时候移除没用的条目来避免保存无限制增长的没有意义的弱引用。

引用队列可以很容易地实现跟踪不需要的引用。当你在构造 WeakReference 时传入一个 ReferenceQueue 对象，当该引用指向的对象被标记为垃圾的时候，这个引用对象会自动地加入到引用队列里面。接下来，你就可以在固定的周期，处理传入的引用队列，比如做一些清理工作来处理这些没有用的引用对象。

- 也就是说，

referent
被标记为垃圾的时候，它对应的 WeakReference 对象会被添加到

refQueue
队列中。\*\*在此处，即将 DefaultResourceLeak 添加到

referent
队列中\*\*。

- 那又咋样呢？假设

referent
为 ByteBuf 对象。如果它被正确的释放，即调用了 [「3.3.4 release」]() 方法，从而调用了

AbstractReferenceCountedByteBuf/#closeLeak()
方法，最终调用到

ResourceLeakTracker/#close(trackedByteBuf)
方法，那么该 ByteBuf 对象对应的 ResourceLeakTracker 对象，将从

ResourceLeakDetector.allLeaks
中移除。

- 那这又意味着什么呢？ 在

ResourceLeakDetector/#reportLeak()
方法中，即使从

refQueue
队列中，获取到该 ByteBuf 对象对应 ResourceLeakTracker 对象，因为在

ResourceLeakDetector.allLeaks
中移除了，所以在

ResourceLeakDetector/#reportLeak()
方法的【第 19 行】代码

!ref.dispose() = true
，直接

continue
。

- 😈 比较绕，胖友再好好理解下。胖友可以在思考下，如果 ByteBuf 对象，没有被正确的释放，是怎么样一个流程。

### []( "5.1.2 record")5.1.2 record

/#record(...)
方法，创建 Record 对象，添加到

head
链中。代码如下：

```
@Override
public void record(){
record0(null);
}
@Override
public void record(Object hint){
record0(hint);
}
//*/*
/* This method works by exponentially backing off as more records are present in the stack. Each record has a
/* 1 / 2^n chance of dropping the top most record and replacing it with itself. This has a number of convenient
/* properties:
/*
/* <ol>
/* <li> The current record is always recorded. This is due to the compare and swap dropping the top most
/* record, rather than the to-be-pushed record.
/* <li> The very last access will always be recorded. This comes as a property of 1.
/* <li> It is possible to retain more records than the target, based upon the probability distribution.
/* <li> It is easy to keep a precise record of the number of elements in the stack, since each element has to
/* know how tall the stack is.
/* </ol>
/*
/* In this particular implementation, there are also some advantages. A thread local random is used to decide
/* if something should be recorded. This means that if there is a deterministic access pattern, it is now
/* possible to see what other accesses occur, rather than always dropping them. Second, after
/* {@link /#TARGET_RECORDS} accesses, backoff occurs. This matches typical access patterns,
/* where there are either a high number of accesses (i.e. a cached buffer), or low (an ephemeral buffer), but
/* not many in between.
/*
/* The use of atomics avoids serializing a high number of accesses, when most of the records will be thrown
/* away. High contention only happens when there are very few existing records, which is only likely when the
/* object isn't shared! If this is a problem, the loop can be aborted and the record dropped, because another
/* thread won the race.
/*/
1: private void record0(Object hint){
2: // Check TARGET_RECORDS > 0 here to avoid similar check before remove from and add to lastRecords
3: if (TARGET_RECORDS > 0) {
4: Record oldHead;
5: Record prevHead;
6: Record newHead;
7: boolean dropped;
8: do {
9: // 已经关闭，则返回
10: if ((prevHead = oldHead = headUpdater.get(this)) == null) {
11: // already closed.
12: return;
13: }
14: // 当超过 TARGET_RECORDS 数量时，随机丢到头节点。
15: final int numElements = oldHead.pos + 1;
16: if (numElements >= TARGET_RECORDS) {
17: final int backOffFactor = Math.min(numElements - TARGET_RECORDS, 30);
18: if (dropped = PlatformDependent.threadLocalRandom().nextInt(1 << backOffFactor) != 0) {
19: prevHead = oldHead.next;
20: }
21: } else {
22: dropped = false;
23: }
24: // 创建新的头节点
25: newHead = hint != null ? new Record(prevHead, hint) : new Record(prevHead);
26: } while (!headUpdater.compareAndSet(this, oldHead, newHead)); // cas 修改头节点
27: // 若丢弃，增加 droppedRecordsUpdater 计数
28: if (dropped) {
29: droppedRecordsUpdater.incrementAndGet(this);
30: }
31: }
32: }
```

- 第 9 至 13 行：通过

headUpdater
获得

head
属性，若为

null
时，说明 DefaultResourceLeak 已经关闭。为什么呢？详细可见 [「5.1.4 close」]() 和 [5.1.5 toString]() 。

- 第 14 至 23 行：当当前 DefaultResourceLeak 对象所拥有的 Record 数量超过

TARGET_RECORDS
时，随机丢弃当前

head
节点的数据。也就是说，尽量保留**老**的 Record 节点。这是为什么呢?越是**老**( 开始 )的 Record 节点，越有利于排查问题。另外，随机丢弃的的概率，按照

1 - (1 / 2^n）
几率，越来越**大**。

- 第 25 行：创建新 Record 对象，作为头节点，指向**原头节点**。这也是为什么说，“实际上，head 是尾节点，即最后( 新 )的一条 Record”。
- 第 26 行：通过 CAS 的方式，修改新创建的 Record 对象为头节点。
- 第 27 至 30 行：若丢弃，增加

droppedRecordsUpdater
计数。

### []( "5.1.3 dispose")5.1.3 dispose

/#dispose()
方法， 清理，并返回是否内存泄露。代码如下：

```
// 清理，并返回是否内存泄露
boolean dispose(){
// 清理 referent 的引用
clear();
// 移除出 allLeaks 。移除成功，意味着内存泄露。
return allLeaks.remove(this, LeakEntry.INSTANCE);
}
```

### []( "5.1.4 close")5.1.4 close

/#close(T trackedObject)
方法，关闭 DefaultResourceLeak 对象。代码如下：

```
1: @Override
2: public boolean close(T trackedObject){
3: // 校验一致
4: // Ensure that the object that was tracked is the same as the one that was passed to close(...).
5: assert trackedHash == System.identityHashCode(trackedObject);
6:
7: // 关闭
8: // We need to actually do the null check of the trackedObject after we close the leak because otherwise
9: // we may get false-positives reported by the ResourceLeakDetector. This can happen as the JIT / GC may
10: // be able to figure out that we do not need the trackedObject anymore and so already enqueue it for
11: // collection before we actually get a chance to close the enclosing ResourceLeak.
12: return close() && trackedObject != null;
13: }
```

- 第 5 行：校验一致性。
- 第 12 行：调用

/#close()
方法，关闭 DefaultResourceLeak 对象。代码如下：

```
@Override
public boolean close(){
// 移除出 allLeaks
// Use the ConcurrentMap remove method, which avoids allocating an iterator.
if (allLeaks.remove(this, LeakEntry.INSTANCE)) {
// 清理 referent 的引用
// Call clear so the reference is not even enqueued.
clear();
// 置空 head
headUpdater.set(this, null);
return true; // 返回成功
}
return false; // 返回失败
}
```

- 关闭时，会将 DefaultResourceLeak 对象，从

allLeaks
中移除。

### []( "5.1.5 toString")5.1.5 toString

当 DefaultResourceLeak 追踪到内存泄露，会在

ResourceLeakDetector/#reportLeak()
方法中，调用

DefaultResourceLeak/#toString()
方法，拼接提示信息。代码如下：

```
@Override
public String toString(){
// 获得 head 属性，并置空 <1>
Record oldHead = headUpdater.getAndSet(this, null);
// 若为空，说明已经关闭。
if (oldHead == null) {
// Already closed
return EMPTY_STRING;
}
final int dropped = droppedRecordsUpdater.get(this);
int duped = 0;
int present = oldHead.pos + 1;
// Guess about 2 kilobytes per stack trace
StringBuilder buf = new StringBuilder(present /* 2048).append(NEWLINE);
buf.append("Recent access records: ").append(NEWLINE);
// 拼接 Record 练
int i = 1;
Set<String> seen = new HashSet<String>(present);
for (; oldHead != Record.BOTTOM; oldHead = oldHead.next) {
String s = oldHead.toString();
if (seen.add(s)) { // 是否重复
if (oldHead.next == Record.BOTTOM) {
buf.append("Created at:").append(NEWLINE).append(s);
} else {
buf.append('/#').append(i++).append(':').append(NEWLINE).append(s);
}
} else {
duped++;
}
}
// 拼接 duped ( 重复 ) 次数
if (duped > 0) {
buf.append(": ")
.append(dropped)
.append(" leak records were discarded because they were duplicates")
.append(NEWLINE);
}
// 拼接 dropped (丢弃) 次数
if (dropped > 0) {
buf.append(": ")
.append(dropped)
.append(" leak records were discarded because the leak record count is targeted to ")
.append(TARGET_RECORDS)
.append(". Use system property ")
.append(PROP_TARGET_RECORDS)
.append(" to increase the limit.")
.append(NEWLINE);
}
buf.setLength(buf.length() - NEWLINE.length());
return buf.toString();
}
```

- 代码比较简单，胖友自己看注释。
- <1>
  处，真的是个神坑。如果胖友在 IDEA 调试时，因为默认会调用对应的

/#toString()
方法，会导致

head
属性被错误的重置为

null
值。wtf！！！笔者在这里卡了好久好久。

# []( "6. LeakEntry")6. LeakEntry

LeakEntry ，用于

ResourceLeakDetector.allLeaks
属性的 value 值。代码如下：

```
private static final class LeakEntry{
//*/*
/* 单例
/*/
static final LeakEntry INSTANCE = new LeakEntry();
//*/*
/* hash 值，避免重复计算
/*/
private static final int HASH = System.identityHashCode(INSTANCE);
private LeakEntry(){ // 禁止创建，仅使用 INSTANCE 单例
}
@Override
public int hashCode(){
return HASH;
}
@Override
public boolean equals(Object obj){
return obj == this;
}
}
```

😈 没有什么功能逻辑。

# []( "7. Record")7. Record

Record ，记录。每次调用

ResourceLeakTracker/#touch(...)
方法后，会产生响应的 Record 对象。代码如下：

```
private static final class Record extends Throwable{
private static final long serialVersionUID = 6065153674892850720L;
//*/*
/* 尾节点的单例
/*/
private static final Record BOTTOM = new Record();
//*/*
/* hint 字符串
/*/
private final String hintString;
//*/*
/* 下一个节点
/*/
private final Record next;
//*/*
/* 位置
/*/
private final int pos;
// =========== 构造方法 ===========
Record(Record next, Object hint) {
// This needs to be generated even if toString() is never called as it may change later on.
hintString = hint instanceof ResourceLeakHint ? ((ResourceLeakHint) hint).toHintString() : hint.toString(); // <1>
this.next = next;
this.pos = next.pos + 1;
}
Record(Record next) {
hintString = null;
this.next = next;
this.pos = next.pos + 1;
}
// Used to terminate the stack
private Record(){
hintString = null;
next = null;
pos = -1;
}
// =========== toString ===========
@Override
public String toString(){
StringBuilder buf = new StringBuilder(2048);
if (hintString != null) {
buf.append("\tHint: ").append(hintString).append(NEWLINE);
}
// Append the stack trace.
StackTraceElement[] array = getStackTrace();
// Skip the first three elements.
out: for (int i = 3; i < array.length; i++) {
StackTraceElement element = array[i];
// 跳过忽略的方法 <2>
// Strip the noisy stack trace elements.
String[] exclusions = excludedMethods.get();
for (int k = 0; k < exclusions.length; k += 2) {
if (exclusions[k].equals(element.getClassName())
&& exclusions[k + 1].equals(element.getMethodName())) {
continue out;
}
}
buf.append('\t');
buf.append(element.toString());
buf.append(NEWLINE);
}
return buf.toString();
}
}
```

- 通过

next
属性，我们可以得知，Record 是链式结构。

- <1>
  处，如果传入的

hint
类型为 ResourceLeakHint 类型，会调用对应的

/#toHintString()
方法，拼接更友好的字符串提示信息。

- <2>
  处，如果调用栈的方法在

ResourceLeakDetector.exclusions
属性中，进行忽略。

# []( "8. ResourceLeakHint")8. ResourceLeakHint

io.netty.util.ResourceLeakHint
，接口，提供人类可读( 易懂 )的提示信息，使用在 ResourceLeakDetector 中。代码如下：

```
//*/*
/* A hint object that provides human-readable message for easier resource leak tracking.
/*/
public interface ResourceLeakHint{
//*/*
/* Returns a human-readable message that potentially enables easier resource leak tracking.
/*/
String toHintString();
}
```

目前它的实现类是 AbstractChannelHandlerContext 。对应的实现方法如下：

```
//*/*
/* 名字
/*/
private final String name;
@Override
public String toHintString(){
return '\'' + name + "' will handle the message from this point.";
}
```

# []( "666. 彩蛋")666. 彩蛋

比想象中长很多的文章，也比想象中花费了更多时间的文章。主要是 xxx 的 [「5.1.5 toString」]() 中卡了好久啊！！！！

推荐阅读文章：

- [《Netty 学习笔记 —— Reference Count》](https://skyao.gitbooks.io/learning-netty/content/buffer/reference_count.html)
- 唯有坚持不懈 [《Netty 学习之旅—-源码分析 Netty 内存泄漏检测》](https://blog.csdn.net/prestigeding/article/details/54233327)

上述两篇文章，因为分析的 Netty 不是最新版本，所以代码会有一些差异，例如

maxActive
已经被去除。
