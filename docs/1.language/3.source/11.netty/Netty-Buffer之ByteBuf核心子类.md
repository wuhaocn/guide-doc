# 精尽 Netty 源码解析 —— Buffer 之 ByteBuf（二）核心子类

# []( "1. 概述")1. 概述

在 [《精尽 Netty 源码解析 —— ByteBuf（一）之简介》](http://svip.iocoder.cn/Netty/ByteBuf-1-1-ByteBuf-intro/) 中，我们对 ByteBuf 有了整体的认识，特别是核心 API 部分。同时，我们也看到，ByteBuf 有非常非常非常多的子类，那么怎么办呢？实际上，**ByteBuf 有 8 个最最最核心的子类实现**。如下图所示：[![核心子类](http://static2.iocoder.cn/images/Netty/2018_08_04/01.png)](http://static2.iocoder.cn/images/Netty/2018_08_04/01.png '核心子类')核心子类

一共可以按照三个维度来看这 8 个核心子类，刚好是 2 x 2 x 2 = 8 ：

- 按照**内存类型**分类：

- ① 堆内存字节缓冲区( **Heap**ByteBuf )：底层为 JVM 堆内的字节数组，其特点是申请和释放效率较高。但是如果要进行 Socket 的 I/O 读写，需要额外多做一次内存复制，需要将堆内存对应的缓冲区复制到内核 Channel 中，性能可能会有一定程度的损耗。
- ② 直接内存字节缓冲区( **Direct**ByteBuf )：堆外内存，为操作系统内核空间的字节数组，它由操作系统直接管理和操作，其申请和释放的效率会慢于堆缓冲区。但是将它写入或者从 SocketChannel 中读取时，会少一次内存复制，这样可以大大提高 I/O 效率，实现零拷贝。
- 关于这两者的对比，感兴趣的胖友，可以再看看 [《Java NIO direct buffer 的优势在哪儿？》](https://www.zhihu.com/question/60892134) 和 [《JAVA NIO 之 Direct Buffer 与 Heap Buffer 的区别？》](http://eyesmore.iteye.com/blog/1133335)
- 按照 **对象池** 分类：

- ① 基于对象池( **Pooled**ByteBuf )：基于对象池的 ByteBuf 可以重用 ByteBuf ，也就是说它自己内部维护着一个对象池，当对象释放后会归还给对象池，这样就可以循环地利用创建的 ByteBuf，提升内存的使用率，降低由于高负载导致的频繁 GC。当需要大量且频繁创建缓冲区时，推荐使用该类缓冲区。
- ② 不使用对象池( **Unpooled**ByteBuf )：对象池的管理和维护会比较困难，所以在不需要创建大量缓冲区对象时，推荐使用此类缓冲区。
- 按照 **Unsafe** 分类：

- ① 使用 Unsafe ：基于 Java

sun.misc.Unsafe.Unsafe
的 API ，直接访问内存中的数据。

- ② 不使用 Unsafe ： 基于 **Heap**ByteBuf 和 **Direct**ByteBuf 的标准 API ，进行访问对应的数据。
- 关于 Unsafe ，JVM 大佬 R 大在知乎上有个回答：[《为什么 JUC 中大量使用了 sun.misc.Unsafe 这个类，但官方却不建议开发者使用？》](https://www.zhihu.com/question/29266773) 。关于为什么 Unsafe 的性能会更好：”其中一种是嫌 Java 性能不够好，例如说数组访问的边界检查语义，嫌这个开销太大，觉得用 Unsafe 会更快；”。

默认情况下，使用 PooledUnsafeDirectByteBuf 类型。所以，重点重点重点，看 [「2.4 PooledUnsafeDirectByteBuf」]() 。

# []( "2. PooledByteBuf")2. PooledByteBuf

io.netty.buffer.PooledByteBuf
，继承 AbstractReferenceCountedByteBuf 抽象类，**对象池化**的 ByteBuf 抽象基类，为基于**对象池**的 ByteBuf 实现类，提供公用的方法。

关于

io.netty.util.AbstractReferenceCountedByteBuf
抽象类，对象引用计数器抽象类。本文暂时不解析，我们会在 [《精尽 Netty 源码解析 —— Buffer 之 ByteBuf（三）内存泄露检测》](http://svip.iocoder.cn/Netty/ByteBuf-1-3-ByteBuf-resource-leak-detector/) 详细解析。

## []( "2.1 内部方法")2.1 内部方法

### []( "2.1.1 构造方法")2.1.1 构造方法

```
//*/*
/* Recycler 处理器，用于回收对象
/*/
private final Recycler.Handle<PooledByteBuf<T>> recyclerHandle;
//*/*
/* Chunk 对象
/*/
protected PoolChunk<T> chunk;
//*/*
/* 从 Chunk 对象中分配的内存块所处的位置
/*/
protected long handle;
//*/*
/* 内存空间。具体什么样的数据，通过子类设置泛型。
/*/
protected T memory;
//*/*
/* {@link /#memory} 开始位置
/*
/* @see /#idx(int)
/*/
protected int offset;
//*/*
/* 容量
/*
/* @see /#capacity()
/*/
protected int length;
//*/*
/* 占用 {@link /#memory} 的大小
/*/
int maxLength;
//*/*
/* TODO 1013 Chunk
/*/
PoolThreadCache cache;
//*/*
/* 临时 ByteBuff 对象
/*
/* @see /#internalNioBuffer()
/*/
private ByteBuffer tmpNioBuf;
//*/*
/* ByteBuf 分配器对象
/*/
private ByteBufAllocator allocator;
@SuppressWarnings("unchecked")
protected PooledByteBuf(Recycler.Handle<? extends PooledByteBuf<T>> recyclerHandle, int maxCapacity){
super(maxCapacity);
this.recyclerHandle = (Handle<PooledByteBuf<T>>) recyclerHandle;
}
```

- recyclerHandle
  属性，Recycler 处理器，用于回收**当前**对象。
- chunk
  属性，PoolChunk 对象。在 Netty 中，使用 Jemalloc 算法管理内存，而 Chunk 是里面的一种**内存块**。在这里，我们可以理解

memory
所属的 PoolChunk 对象。

- handle
  属性，从 Chunk 对象中分配的内存块所处的位置。具体的，胖友后面仔细看看 [《精尽 Netty 源码解析 —— Buffer 之 Jemalloc（二）PoolChunk》](http://svip.iocoder.cn/Netty/ByteBuf-3-2-Jemalloc-chunk/) 和 [《精尽 Netty 源码解析 —— Buffer 之 Jemalloc（三）PoolSubpage》](http://svip.iocoder.cn/Netty/ByteBuf-3-3-Jemalloc-subpage/) 。
- memory
  属性，内存空间。具体什么样的数据，通过子类设置泛型(

T
)。例如：1) PooledDirectByteBuf 和 PooledUnsafeDirectByteBuf 为 **ByteBuffer** ；2) PooledHeapByteBuf 和 PooledUnsafeHeapByteBuf 为

byte[]
。

- offset
  属性，使用

memory
的开始位置。

- maxLength
  属性，**最大**使用

memory
的长度( 大小 )。

- length
  属性，**目前**使用

memory
的长度( 大小 )。

- 😈 因为

memory
属性，可以被**多个** ByteBuf 使用。**每个** ByteBuf 使用范围为

[offset, maxLength)
。

- cache
  属性，TODO 1013 Chunk
- tmpNioBuf
  属性，临时 ByteBuff 对象，通过

/#tmpNioBuf()
方法生成。详细解析，见 [「2.1.9 internalNioBuffer」]() 。

- allocator
  属性，ByteBuf 分配器。

### []( "2.1.2 init0")2.1.2 init0

/#init0(PoolChunk<T> chunk, long handle, int offset, int length, int maxLength, PoolThreadCache cache)
方法，初始化 PooledByteBuf 对象。代码如下：

```
private void init0(PoolChunk<T> chunk, long handle, int offset, int length, int maxLength, PoolThreadCache cache){
assert handle >= 0;
assert chunk != null;
// From PoolChunk 对象
this.chunk = chunk;
memory = chunk.memory;
allocator = chunk.arena.parent;
// 其他
this.cache = cache;
this.handle = handle;
this.offset = offset;
this.length = length;
this.maxLength = maxLength;
tmpNioBuf = null;
}
```

仔细的胖友，可能会发现，这是一个

private
私有方法。目前它被两个方法调用：

- ①

/#init(PoolChunk<T> chunk, long handle, int offset, int length, int maxLength, PoolThreadCache cache)
方法，一般是基于 **pooled** 的 PoolChunk 对象，初始化 PooledByteBuf 对象。代码如下：

```
void init(PoolChunk<T> chunk, long handle, int offset, int length, int maxLength, PoolThreadCache cache){
init0(chunk, handle, offset, length, maxLength, cache);
}
```

- ②

/#initUnpooled(PoolChunk<T> chunk, int length)
方法，基于 **unPoolooled** 的 PoolChunk 对象，初始化 PooledByteBuf 对象。代码如下：

```
void initUnpooled(PoolChunk<T> chunk, int length){
init0(chunk, 0, chunk.offset, length, length, null);
}
```

- 例如说 **Huge** 大小的 PoolChunk 对象。
- 注意，传入的给

/#init0(...)
方法的

length
和

maxLength
方法参数，**都是**

length
。

可能胖友读到此处会一脸懵逼。其实，这是很正常的。可以在看完 [《精尽 Netty 源码解析 —— Buffer 之 Jemalloc（二）PoolChunk》](http://svip.iocoder.cn/Netty/ByteBuf-3-2-Jemalloc-chunk/) 后，在回过头来，理解理解。

### []( "2.1.3 reuse")2.1.3 reuse

/#reuse(int maxCapacity)
方法，每次在重用 PooledByteBuf 对象时，需要调用该方法，重置属性。代码如下：

```
//*/*
/* Method must be called before reuse this {@link PooledByteBufAllocator}
/*/
final void reuse(int maxCapacity){
// 设置最大容量
maxCapacity(maxCapacity);
// 设置引用数量为 0
setRefCnt(1);
// 重置读写索引为 0
setIndex0(0, 0);
// 重置读写标记位为 0
discardMarks();
}
```

也就是说，该方法在 [「2.1.2 init9」]() **之前**就调用了。在下文中，我们会看到，该方法的调用。

### []( "2.1.4 capacity")2.1.4 capacity

/#capacity()
方法，获得容量。代码如下：

```
@Override
public final int capacity(){
return length;
}
```

**当前**容量的值为

length
属性。
但是，要注意的是，

maxLength
属性，**不是表示最大容量**。

maxCapacity
属性，才是真正表示最大容量。
那么，

maxLength
属性有什么用？表示**占用**

memory
的最大容量( 而不是 PooledByteBuf 对象的最大容量 )。在写入数据超过

maxLength
容量时，会进行扩容，但是容量的上限，为

maxCapacity
。

/#capacity(int newCapacity)
方法，调整容量大小。在这个过程中，根据情况，可能对

memory
扩容或缩容。代码如下：

```
1: @Override
2: public final ByteBuf capacity(int newCapacity){
3: // 校验新的容量，不能超过最大容量
4: checkNewCapacity(newCapacity);
5:
6: // Chunk 内存，非池化
7: // If the request capacity does not require reallocation, just update the length of the memory.
8: if (chunk.unpooled) {
9: if (newCapacity == length) { // 相等，无需扩容 / 缩容
10: return this;
11: }
12: // Chunk 内存，是池化
13: } else {
14: // 扩容
15: if (newCapacity > length) {
16: if (newCapacity <= maxLength) {
17: length = newCapacity;
18: return this;
19: }
20: // 缩容
21: } else if (newCapacity < length) {
22: // 大于 maxLength 的一半
23: if (newCapacity > maxLength >>> 1) {
24: if (maxLength <= 512) {
25: // 因为 Netty SubPage 最小是 16 ，如果小于等 16 ，无法缩容。
26: if (newCapacity > maxLength - 16) {
27: length = newCapacity;
28: // 设置读写索引，避免超过最大容量
29: setIndex(Math.min(readerIndex(), newCapacity), Math.min(writerIndex(), newCapacity));
30: return this;
31: }
32: } else { // > 512 (i.e. >= 1024)
33: length = newCapacity;
34: // 设置读写索引，避免超过最大容量
35: setIndex(Math.min(readerIndex(), newCapacity), Math.min(writerIndex(), newCapacity));
36: return this;
37: }
38: }
39: // 相等，无需扩容 / 缩容
40: } else {
41: return this;
42: }
43: }
44:
45: // 重新分配新的内存空间，并将数据复制到其中。并且，释放老的内存空间。
46: // Reallocation required.
47: chunk.arena.reallocate(this, newCapacity, true);
48: return this;
49: }
```

- 第 4 行：调用

AbstractByteBuf/#checkNewCapacity(int newCapacity)
方法，校验新的容量，不能超过最大容量。代码如下：

```
protected final void checkNewCapacity(int newCapacity){
ensureAccessible();
if (newCapacity < 0 || newCapacity > maxCapacity()) {
throw new IllegalArgumentException("newCapacity: " + newCapacity + " (expected: 0-" + maxCapacity() + ')');
}
}
```

- 第 6 至 11 行：对于基于 **unPoolooled** 的 PoolChunk 对象，除非容量不变，否则会扩容或缩容，即【第 47 行】的代码。为什么呢？在

/#initUnpooled(PoolChunk<T> chunk, int length)
方法中，我们可以看到，

maxLength
和

length
是相等的，所以大于或小于时，需要进行扩容或缩容。

- 第 13 行：对于基于 **poolooled** 的 PoolChunk 对象，需要根据情况：

- 第 39 至 42 行：容量未变，不进行扩容。类似【第 9 至 11 行】的代码。
- 第 14 至 19 行：新容量**大于**当前容量，但是小于

memory
最大容量，仅仅修改当前容量，无需进行扩容。否则，第【第 47 行】的代码，进行**扩容**。

- 第 20 至 38 行：新容量**小于**当前容量，但是不到

memory
最大容量的**一半**，因为缩容**相对**释放不多，无需进行缩容。否则，第【第 47 行】的代码，进行**缩容**。

- 比较神奇的是【第 26 行】的

newCapacity > maxLength - 16
代码块。 笔者的理解是，Netty SubPage **最小**是 16 B ，如果小于等 16 ，无法缩容。

- 第 47 行：调用

PoolArena/#reallocate(PooledByteBuf<T> buf, int newCapacity, boolean freeOldMemory)
方法，**重新分配**新的内存空间，并将数据**复制**到其中。并且，**释放**老的内存空间。详细解析，见 [《TODO 1013 Chunk》]() 中。

### []( "2.1.5 order")2.1.5 order

/#order()
方法，返回字节序为

ByteOrder.BIG_ENDIAN
大端。代码如下：

```
@Override
public final ByteOrder order(){
return ByteOrder.BIG_ENDIAN;
}
```

统一**大端**模式。

FROM [《深入浅出： 大小端模式》](https://www.bysocket.com/?p=615)

在网络上传输数据时，由于数据传输的两端对应不同的硬件平台，采用的存储字节顺序可能不一致。所以在 TCP/IP 协议规定了在网络上必须采用网络字节顺序，也就是大端模式。

### []( "2.1.6 unwrap")2.1.6 unwrap

/#unwrap()
方法，返回空，因为没有被装饰的 ByteBuffer 对象。代码如下：

```
@Override
public final ByteBuf unwrap(){
return null;
}
```

### []( "2.1.7 retainedSlice")2.1.7 retainedSlice

/#retainedSlice()
方法，代码如下：

```
@Override
public final ByteBuf retainedSlice(){
final int index = readerIndex();
return retainedSlice(index, writerIndex() - index);
}
@Override
public final ByteBuf retainedSlice(int index, int length){
return PooledSlicedByteBuf.newInstance(this, this, index, length);
}
```

- 调用

PooledSlicedByteBuf/#newInstance(AbstractByteBuf unwrapped, ByteBuf wrapped, int index, int length)
方法，创建**池化的** PooledSlicedByteBuf 对象。

- TODO 1016 派生类

### []( "2.1.8 retainedDuplicate")2.1.8 retainedDuplicate

/#retainedDuplicate()
方法，代码如下：

```
@Override
public final ByteBuf retainedDuplicate(){
return PooledDuplicatedByteBuf.newInstance(this, this, readerIndex(), writerIndex());
}
```

- 调用

PooledSlicedByteBuf/#newInstance(AbstractByteBuf unwrapped, ByteBuf wrapped, int readerIndex, int writerIndex)
方法，创建**池化的** PooledDuplicatedByteBuf.newInstance 对象。

- TODO 1016 派生类

### []( "2.1.9 internalNioBuffer")2.1.9 internalNioBuffer

/#internalNioBuffer()
方法，获得临时 ByteBuf 对象(

tmpNioBuf
) 。代码如下：

```
protected final ByteBuffer internalNioBuffer(){
ByteBuffer tmpNioBuf = this.tmpNioBuf;
// 为空，创建临时 ByteBuf 对象
if (tmpNioBuf == null) {
this.tmpNioBuf = tmpNioBuf = newInternalNioBuffer(memory);
}
return tmpNioBuf;
}
```

- 当

tmpNioBuf
属性为空时，调用

/#newInternalNioBuffer(T memory)
方法，创建 ByteBuffer 对象。因为

memory
的类型不确定，所以该方法定义成**抽象方法**，由子类实现。代码如下：

```
protected abstract ByteBuffer newInternalNioBuffer(T memory);
```

为什么要有

tmpNioBuf
这个属性呢？以 PooledDirectByteBuf 举例子，代码如下：

```
@Override
public int setBytes(int index, FileChannel in, long position, int length) throws IOException{
checkIndex(index, length);
// 获得临时 ByteBuf 对象
ByteBuffer tmpBuf = internalNioBuffer();
index = idx(index);
tmpBuf.clear().position(index).limit(index + length);
try {
// 写入临时 ByteBuf 对象
return in.read(tmpBuf, position);
} catch (ClosedChannelException ignored) {
return -1;
}
}
private int getBytes(int index, FileChannel out, long position, int length, boolean internal) throws IOException{
checkIndex(index, length);
if (length == 0) {
return 0;
}
// 获得临时 ByteBuf 对象
ByteBuffer tmpBuf = internal ? internalNioBuffer() : memory.duplicate();
index = idx(index);
tmpBuf.clear().position(index).limit(index + length);
// 写入到 FileChannel 中
return out.write(tmpBuf, position);
}
```

### []( "2.1.10 deallocate")2.1.10 deallocate

/#deallocate()
方法，当引用计数为 0 时，调用该方法，进行内存回收。代码如下：

```
@Override
protected final void deallocate(){
if (handle >= 0) {
// 重置属性
final long handle = this.handle;
this.handle = -1;
memory = null;
tmpNioBuf = null;
// 释放内存回 Arena 中
chunk.arena.free(chunk, handle, maxLength, cache);
chunk = null;
// 回收对象
recycle();
}
}
private void recycle(){
recyclerHandle.recycle(this); // 回收对象
}
```

### []( "2.1.11 idx")2.1.11 idx

/#idx(int index)
方法，获得指定位置在

memory
变量中的位置。代码如下：

```
protected final int idx(int index){
return offset + index;
}
```

## []( "2.2 PooledDirectByteBuf")2.2 PooledDirectByteBuf

io.netty.buffer.PooledDirectByteBuf
，实现 PooledByteBuf 抽象类，基于 **ByteBuffer** 的**可重用** ByteBuf 实现类。所以，泛型

T
为 ByteBuffer ，即：

```
final class PooledDirectByteBuf extends PooledByteBuf<ByteBuffer>
```

### []( "2.2.1 构造方法")2.2.1 构造方法

```
private PooledDirectByteBuf(Recycler.Handle<PooledDirectByteBuf> recyclerHandle, int maxCapacity){
super(recyclerHandle, maxCapacity);
}
```

### []( "2.2.2 newInstance")2.2.2 newInstance

/#newInstance(int maxCapacity)
**静态**方法，“创建” PooledDirectByteBuf 对象。代码如下：

```
//*/*
/* Recycler 对象
/*/
private static final Recycler<PooledDirectByteBuf> RECYCLER = new Recycler<PooledDirectByteBuf>() {
@Override
protected PooledDirectByteBuf newObject(Handle<PooledDirectByteBuf> handle){
return new PooledDirectByteBuf(handle, 0); // 真正创建 PooledDirectByteBuf 对象
}
};
static PooledDirectByteBuf newInstance(int maxCapacity){
// 从 Recycler 的对象池中获得 PooledDirectByteBuf 对象
PooledDirectByteBuf buf = RECYCLER.get();
// 重置 PooledDirectByteBuf 的属性
buf.reuse(maxCapacity);
return buf;
}
```

### []( "2.2.3 newInternalNioBuffer")2.2.3 newInternalNioBuffer

/#newInternalNioBuffer(ByteBuffer memory)
方法，获得临时 ByteBuf 对象(

tmpNioBuf
) 。代码如下：

```
@Override
protected ByteBuffer newInternalNioBuffer(ByteBuffer memory){
return memory.duplicate();
}
```

- 调用

ByteBuffer/#duplicate()
方法，复制一个 ByteBuffer 对象，**共享**里面的数据。

### []( "2.2.4 isDirect")2.2.4 isDirect

/#isDirect()
方法，获得内部类型是否为 Direct ，返回

true
。代码如下：

```
@Override
public boolean isDirect(){
return true;
}
```

### []( "2.2.5 读取 / 写入操作")2.2.5 读取 / 写入操作

老样子，我们以 Int 类型为例子，来看看它的读取和写入操作的实现代码。代码如下：

```
@Override
protected int _getInt(int index){
return memory.getInt(idx(index));
}
@Override
protected void _setInt(int index, int value){
memory.putInt(idx(index), value);
}
```

### []( "2.2.6 copy")2.2.6 copy

/#copy(int index, int length)
方法，复制指定范围的数据到新创建的 Direct ByteBuf 对象。代码如下：

```
@Override
public ByteBuf copy(int index, int length){
// 校验索引
checkIndex(index, length);
// 创建一个 Direct ByteBuf 对象
ByteBuf copy = alloc().directBuffer(length, maxCapacity());
// 写入数据
copy.writeBytes(this, index, length);
return copy;
}
```

### []( "2.2.7 转换 NIO ByteBuffer 操作")2.2.7 转换 NIO ByteBuffer 操作

### []( "2.2.7.1 nioBufferCount")2.2.7.1 nioBufferCount

/#nioBufferCount()
方法，返回 ByteBuf 包含 ByteBuffer 数量为 **1** 。代码如下：

```
@Override
public int nioBufferCount(){
return 1;
}
```

### []( "2.2.7.2 nioBuffer")2.2.7.2 nioBuffer

/#nioBuffer(int index, int length)
方法，返回 ByteBuf **指定范围**包含的 ByteBuffer 对象( **共享** )。代码如下：

```
@Override
public ByteBuffer nioBuffer(int index, int length){
checkIndex(index, length);
// memory 中的开始位置
index = idx(index);
// duplicate 复制一个 ByteBuffer 对象，共享数据
// position + limit 设置位置和大小限制
// slice 创建 [position, limit] 子缓冲区，共享数据
return ((ByteBuffer) memory.duplicate().position(index).limit(index + length)).slice();
}
```

- 代码比较简单，看具体注释。

### []( "2.2.7.3 nioBuffers")2.2.7.3 nioBuffers

/#nioBuffers(int index, int length)
方法，返回 ByteBuf **指定范围**内包含的 ByteBuffer 数组( **共享** )。代码如下：

```
@Override
public ByteBuffer[] nioBuffers(int index, int length) {
return new ByteBuffer[] { nioBuffer(index, length) };
}
```

- 在

/#nioBuffer(int index, int length)
方法的基础上，创建大小为 1 的 ByteBuffer 数组。

### []( "2.2.7.4 internalNioBuffer")2.2.7.4 internalNioBuffer

/#internalNioBuffer(int index, int length)
方法，返回 ByteBuf **指定范围**内的 ByteBuffer 对象( **共享** )。代码如下：

```
@Override
public ByteBuffer internalNioBuffer(int index, int length){
checkIndex(index, length);
// memory 中的开始位置
index = idx(index);
// clear 标记清空（不会清理数据）
// position + limit 设置位置和大小限制
return (ByteBuffer) internalNioBuffer().clear().position(index).limit(index + length);
}
```

- 代码比较简单，看具体注释。
- 因为是基于

tmpNioBuf
属性实现，所以方法在命名上，以

"internal"
打头。

### []( "2.2.8 Heap 相关方法")2.2.8 Heap 相关方法

不支持 Heap 相关方法。代码如下：

```
@Override
public boolean hasArray(){
return false;
}
@Override
public byte[] array() {
throw new UnsupportedOperationException("direct buffer");
}
@Override
public int arrayOffset(){
throw new UnsupportedOperationException("direct buffer");
}
```

### []( "2.2.9 Unsafe 相关方法")2.2.9 Unsafe 相关方法

不支持 Unsafe 相关方法。代码如下：

```
@Override
public boolean hasMemoryAddress(){
return false;
}
@Override
public long memoryAddress(){
throw new UnsupportedOperationException();
}
```

## []( "2.3 PooledHeapByteBuf")2.3 PooledHeapByteBuf

io.netty.buffer.PooledHeapByteBuf
，实现 PooledByteBuf 抽象类，基于 **ByteBuffer** 的**可重用** ByteBuf 实现类。所以，泛型

T
为

byte[]
，即：

```
class PooledHeapByteBuf extends PooledByteBuf<byte[]> {
```

### []( "2.3.1 构造方法")2.3.1 构造方法

和 [「2.2.1 构造方法」]() 相同。

### []( "2.3.2 newInstance")2.3.2 newInstance

和 [「2.2.2 newInstance」]() 相同。

### []( "2.3.3 newInternalNioBuffer")2.3.3 newInternalNioBuffer

/#newInternalNioBuffer(byte[] memory)
方法，获得临时 ByteBuf 对象(

tmpNioBuf
) 。代码如下：

```
@Override
protected final ByteBuffer newInternalNioBuffer(byte[] memory){
return ByteBuffer.wrap(memory);
}
```

- 调用

ByteBuffer/#wrap(byte[] array)
方法，创建 ByteBuffer 对象。注意，返回的是 HeapByteBuffer 对象。

### []( "2.3.4 isDirect")2.3.4 isDirect

/#isDirect()
方法，获得内部类型是否为 Direct ，返回

false
。代码如下：

```
@Override
public boolean isDirect(){
return false;
}
```

### []( "2.3.5 读取 / 写入操作")2.3.5 读取 / 写入操作

老样子，我们以 Int 类型为例子，来看看它的读取和写入操作的实现代码。

① **读取**操作：

```
@Override
protected int _getInt(int index){
return HeapByteBufUtil.getInt(memory, idx(index));
}
// HeapByteBufUtil.java
static int getInt(byte[] memory, int index){
return (memory[index] & 0xff) << 24 |
(memory[index + 1] & 0xff) << 16 |
(memory[index + 2] & 0xff) << 8 |
memory[index + 3] & 0xff;
}
```

② **写入**操作：

```
@Override
protected void _setInt(int index, int value){
HeapByteBufUtil.setInt(memory, idx(index), value);
}
// HeapByteBufUtil.java
static void setInt(byte[] memory, int index, int value){
memory[index] = (byte) (value >>> 24);
memory[index + 1] = (byte) (value >>> 16);
memory[index + 2] = (byte) (value >>> 8);
memory[index + 3] = (byte) value;
}
```

### []( "2.3.6 copy")2.3.6 copy

/#copy(int index, int length)
方法，复制指定范围的数据到新创建的 Heap ByteBuf 对象。代码如下：

```
@Override
public ByteBuf copy(int index, int length){
// 校验索引
checkIndex(index, length);
// 创建一个 Heap ByteBuf 对象
ByteBuf copy = alloc().heapBuffer(length, maxCapacity());
// 写入数据
copy.writeBytes(this, index, length);
return copy;
}
```

和 PooledDirectByteBuf [「2.2.6 copy」]() 的差异在于，创建的是 **Heap** ByteBuf 对象。

### []( "2.3.7 转换 NIO ByteBuffer 操作")2.3.7 转换 NIO ByteBuffer 操作

### []( "2.3.7.1 nioBufferCount")2.3.7.1 nioBufferCount

和 [「2.2.7.1 nioBufferCount」]() 一致。

### []( "2.3.7.2 nioBuffer")2.3.7.2 nioBuffer

/#nioBuffer(int index, int length)
方法，返回 ByteBuf **指定范围**包含的 ByteBuffer 对象( **共享** )。代码如下：

```
@Override
public final ByteBuffer nioBuffer(int index, int length){
checkIndex(index, length);
// memory 中的开始位置
index = idx(index);
// 创建 ByteBuffer 对象
ByteBuffer buf = ByteBuffer.wrap(memory, index, length);
// slice 创建 [position, limit] 子缓冲区
return buf.slice();
}
```

- 代码比较简单，看具体注释。

### []( "2.3.7.3 nioBuffers")2.3.7.3 nioBuffers

和 [「2.2.7.3 nioBuffers」]() 一致。

### []( "2.3.7.4 internalNioBuffer")2.3.7.4 internalNioBuffer

和 [「2.2.7.4 nioBuffers」]() 一致。

### []( "2.3.8 Heap 相关方法")2.3.8 Heap 相关方法

```
@Override
public final boolean hasArray(){
return true;
}
@Override
public final byte[] array() {
ensureAccessible();
return memory;
}
@Override
public final int arrayOffset(){
return offset;
}
```

### []( "2.3.8 Unsafe 相关方法")2.3.8 Unsafe 相关方法

和 [「2.2.9 Unsafe 相关方法」]() 一致。

## []( "2.4 PooledUnsafeDirectByteBuf")2.4 PooledUnsafeDirectByteBuf

老艿艿：它是 [「2.2 PooledDirectByteBuf」]() 对应的基于 Unsafe 版本的实现类。

io.netty.buffer.PooledUnsafeDirectByteBuf
，实现 PooledByteBuf 抽象类，基于 **ByteBuffer** + **Unsafe** 的**可重用** ByteBuf 实现类。所以，泛型

T
为

ByteBuffer
，即：

```
final class PooledUnsafeDirectByteBuf extends PooledByteBuf<ByteBuffer>
```

### []( "2.4.1 构造方法")2.4.1 构造方法

和 [「2.2.1 构造方法」]() 相同。

### []( "2.4.2 newInstance")2.4.2 newInstance

和 [「2.2.2 newInstance」]() 相同。

### []( "2.4.3 初始化")2.4.3 初始化

PooledUnsafeDirectByteBuf 重写了初始化相关的方法，代码如下：

```
@Override
void init(PoolChunk<ByteBuffer> chunk, long handle, int offset, int length, int maxLength,
PoolThreadCache cache){
// 调用父初始化方法
super.init(chunk, handle, offset, length, maxLength, cache);
// 初始化内存地址
initMemoryAddress(); // <1>
}
@Override
void initUnpooled(PoolChunk<ByteBuffer> chunk, int length){
// 调用父初始化方法
super.initUnpooled(chunk, length);
// 初始化内存地址
initMemoryAddress(); // <2>
}
```

- 在

<1>
处，增加调用

/#initMemoryAddress()
方法，初始化内存地址。代码如下：

```
//*/*
/* 内存地址
/*/
private long memoryAddress;
private void initMemoryAddress(){
memoryAddress = PlatformDependent.directBufferAddress(memory) + offset; // <2>
}
```

- 调用

PlatformDependent/#directBufferAddress(ByteBuffer buffer)
方法，获得 ByteBuffer 对象的起始内存地址。代码如下：

```
// PlatformDependent.java
public static long directBufferAddress(ByteBuffer buffer){
return PlatformDependent0.directBufferAddress(buffer);
}
// PlatformDependent0.java
static final Unsafe UNSAFE;
static long directBufferAddress(ByteBuffer buffer){
return getLong(buffer, ADDRESS_FIELD_OFFSET);
}
private static long getLong(Object object, long fieldOffset){
return UNSAFE.getLong(object, fieldOffset);
}
```

- 对于 Unsafe 类不熟悉的胖友，可以看看 [《Java Unsafe 类》](https://blog.csdn.net/zhxdick/article/details/52003123)
- 注意，

<2>
处的代码，已经将

offset
添加到

memoryAddress
中。所以在

/#addr(int index)
方法中，求指定位置(

index
) 在内存地址的顺序，不用再添加。代码如下：

```
private long addr(int index){
return memoryAddress + index;
}
```

- x

### []( "2.4.4 newInternalNioBuffer")2.4.4 newInternalNioBuffer

和 [「2.2.3 newInternalNioBuffer」]() 相同。

### []( "2.4.5 isDirect")2.4.5 isDirect

和 [「2.2.4 isDirect」]() 相同。

### []( "2.4.6 读取 / 写入操作")2.4.6 读取 / 写入操作

老样子，我们以 Int 类型为例子，来看看它的读取和写入操作的实现代码。

① **读取**操作：

```
@Override
protected int _getInt(int index){
return UnsafeByteBufUtil.getInt(addr(index));
}
// UnsafeByteBufUtil.java
static int getInt(long address){
if (UNALIGNED) {
int v = PlatformDependent.getInt(address);
return BIG_ENDIAN_NATIVE_ORDER ? v : Integer.reverseBytes(v);
}
return PlatformDependent.getByte(address) << 24 |
(PlatformDependent.getByte(address + 1) & 0xff) << 16 |
(PlatformDependent.getByte(address + 2) & 0xff) << 8 |
PlatformDependent.getByte(address + 3) & 0xff;
}
// PlatformDependent.java
public static int getInt(long address){
return PlatformDependent0.getInt(address);
}
// PlatformDependent0.java
static int getInt(long address){
return UNSAFE.getInt(address);
}
```

② **写入**操作：

```
@Override
protected void _setInt(int index, int value){
UnsafeByteBufUtil.setInt(addr(index), value);
}
// UnsafeByteBufUtil.java
static void setInt(long address, int value){
if (UNALIGNED) {
PlatformDependent.putInt(address, BIG_ENDIAN_NATIVE_ORDER ? value : Integer.reverseBytes(value));
} else {
PlatformDependent.putByte(address, (byte) (value >>> 24));
PlatformDependent.putByte(address + 1, (byte) (value >>> 16));
PlatformDependent.putByte(address + 2, (byte) (value >>> 8));
PlatformDependent.putByte(address + 3, (byte) value);
}
}
// PlatformDependent.java
public static void putInt(long address, int value){
PlatformDependent0.putInt(address, value);
}
// PlatformDependent0.java
static void putInt(long address, int value){
UNSAFE.putInt(address, value);
}
```

### []( "2.4.7 copy")2.4.7 copy

/#copy(int index, int length)
方法，复制指定范围的数据到新创建的 Direct ByteBuf 对象。代码如下：

```
@Override
public ByteBuf copy(int index, int length){
return UnsafeByteBufUtil.copy(this, addr(index), index, length);
}
// UnsafeByteBufUtil.java
static ByteBuf copy(AbstractByteBuf buf, long addr, int index, int length){
buf.checkIndex(index, length);
// 创建 Direct ByteBuffer 对象
ByteBuf copy = buf.alloc().directBuffer(length, buf.maxCapacity());
if (length != 0) {
if (copy.hasMemoryAddress()) {
// 使用 Unsafe 操作来复制
PlatformDependent.copyMemory(addr, copy.memoryAddress(), length);
copy.setIndex(0, length);
} else {
copy.writeBytes(buf, index, length);
}
}
return copy;
}
// PlatformDependent.java
public static void copyMemory(long srcAddr, long dstAddr, long length){
PlatformDependent0.copyMemory(srcAddr, dstAddr, length);
}
// PlatformDependent0.java
static void copyMemory(long srcAddr, long dstAddr, long length){
//UNSAFE.copyMemory(srcAddr, dstAddr, length);
while (length > 0) {
long size = Math.min(length, UNSAFE_COPY_THRESHOLD);
UNSAFE.copyMemory(srcAddr, dstAddr, size);
length -= size;
srcAddr += size;
dstAddr += size;
}
}
```

### []( "2.4.8 转换 NIO ByteBuffer 操作")2.4.8 转换 NIO ByteBuffer 操作

### []( "2.4.8.1 nioBufferCount")2.4.8.1 nioBufferCount

和 [「2.2.7.1 nioBufferCount」]() 一致。

### []( "2.4.8.2 nioBuffer")2.4.8.2 nioBuffer

和 [「2.2.7.2 nioBuffer」]() 一致。

### []( "2.4.8.3 nioBuffers")2.4.8.3 nioBuffers

和 [「2.2.7.3 nioBuffers」]() 一致。

### []( "2.4.8.4 internalNioBuffer")2.4.8.4 internalNioBuffer

和 [「2.2.7.4 internalNioBuffer」]() 一致。

### []( "2.4.9 Heap 相关方法")2.4.9 Heap 相关方法

不支持 Heap 相关方法。

### []( "2.4.10 Unsafe 相关方法。")2.4.10 Unsafe 相关方法。

```
@Override
public boolean hasMemoryAddress(){
return true;
}
@Override
public long memoryAddress(){
ensureAccessible();
return memoryAddress;
}
```

### []( "2.4.11 newSwappedByteBuf")2.4.11 newSwappedByteBuf

/#newSwappedByteBuf()
方法的**重写**，是 Unsafe 类型独有的。

/#newSwappedByteBuf()
方法，创建 SwappedByteBuf 对象。代码如下：

```
@Override
protected SwappedByteBuf newSwappedByteBuf(){
if (PlatformDependent.isUnaligned()) { // 支持
// Only use if unaligned access is supported otherwise there is no gain.
return new UnsafeDirectSwappedByteBuf(this);
}
return super.newSwappedByteBuf();
}
```

- 对于 Linux 环境下，一般是支持 unaligned access( 对齐访问 )，所以返回的是 UnsafeDirectSwappedByteBuf 对象。详细解析，见 [《TODO 1016 派生类》]() 。
- 为什么要对齐访问呢？可看 [《什么是字节对齐，为什么要对齐?》](https://www.zhihu.com/question/23791224) 。有趣。

## []( "2.5 PooledUnsafeHeapByteBuf")2.5 PooledUnsafeHeapByteBuf

io.netty.buffer.PooledUnsafeHeapByteBuf
，实现 PooledHeapByteBuf 类，在 [「2.3 PooledHeapByteBuf」]() 的基础上，基于 **Unsafe** 的**可重用** ByteBuf 实现类。所以，泛型

T
为

byte[]
，即：

```
final class PooledUnsafeHeapByteBuf extends PooledHeapByteBuf
```

也因此，PooledUnsafeHeapByteBuf 需要实现的方法，灰常少。

### []( "2.5.1 构造方法")2.5.1 构造方法

和 [「2.2.1 构造方法」]() 相同。

### []( "2.5.2 newInstance")2.5.2 newInstance

和 [「2.2.2 newInstance」]() 相同。

### []( "2.5.3 读取 / 写入操作")2.5.3 读取 / 写入操作

老样子，我们以 Int 类型为例子，来看看它的读取和写入操作的实现代码。

① **读取**操作：

```
@Override
protected int _getInt(int index){
return UnsafeByteBufUtil.getInt(memory, idx(index));
}
// UnsafeByteBufUtil.java
static int getInt(byte[] array, int index){
if (UNALIGNED) {
int v = PlatformDependent.getInt(array, index);
return BIG_ENDIAN_NATIVE_ORDER ? v : Integer.reverseBytes(v);
}
return PlatformDependent.getByte(array, index) << 24 |
(PlatformDependent.getByte(array, index + 1) & 0xff) << 16 |
(PlatformDependent.getByte(array, index + 2) & 0xff) << 8 |
PlatformDependent.getByte(array, index + 3) & 0xff;
}
// PlatformDependent.java
public static int getInt(byte[] data, int index){
return PlatformDependent0.getInt(data, index);
}
// PlatformDependent0.java
static int getInt(byte[] data, int index){
return UNSAFE.getInt(data, BYTE_ARRAY_BASE_OFFSET + index);
}
```

- 基于 Unsafe 操作

byte[]
数组。

② **写入**操作：

```
@Override
protected void _setInt(int index, int value){
UnsafeByteBufUtil.setInt(memory, idx(index), value);
}
// UnsafeByteBufUtil.java
static void setInt(byte[] array, int index, int value){
if (UNALIGNED) {
PlatformDependent.putInt(array, index, BIG_ENDIAN_NATIVE_ORDER ? value : Integer.reverseBytes(value));
} else {
PlatformDependent.putByte(array, index, (byte) (value >>> 24));
PlatformDependent.putByte(array, index + 1, (byte) (value >>> 16));
PlatformDependent.putByte(array, index + 2, (byte) (value >>> 8));
PlatformDependent.putByte(array, index + 3, (byte) value);
}
}
// PlatformDependent.java
public static void putInt(byte[] data, int index, int value){
PlatformDependent0.putInt(data, index, value);
}
// PlatformDependent0.java
static void putInt(byte[] data, int index, int value){
UNSAFE.putInt(data, BYTE_ARRAY_BASE_OFFSET + index, value);
}
```

### []( "2.5.4 newSwappedByteBuf")2.5.4 newSwappedByteBuf

/#newSwappedByteBuf()
方法的**重写**，是 Unsafe 类型独有的。

/#newSwappedByteBuf()
方法，创建 SwappedByteBuf 对象。代码如下：

```
@Override
@Deprecated
protected SwappedByteBuf newSwappedByteBuf(){
if (PlatformDependent.isUnaligned()) {
// Only use if unaligned access is supported otherwise there is no gain.
return new UnsafeHeapSwappedByteBuf(this);
}
return super.newSwappedByteBuf();
}
```

- 对于 Linux 环境下，一般是支持 unaligned access( 对齐访问 )，所以返回的是 UnsafeHeapSwappedByteBuf 对象。详细解析，见 [《TODO 1016 派生类》]() 。

# []( "3. UnpooledByteBuf")3. UnpooledByteBuf

😈 不存在 UnpooledByteBuf 这样一个类，主要是为了**聚合**所有 Unpooled 类型的 ByteBuf 实现类。

## []( "3.1 UnpooledDirectByteBuf")3.1 UnpooledDirectByteBuf

io.netty.buffer.UnpooledDirectByteBuf
，实现 AbstractReferenceCountedByteBuf 抽象类，对应 [「2.2 PooledDirectByteBuf」]() 的**非池化** ByteBuf 实现类。

### []( "3.1.1 构造方法")3.1.1 构造方法

```
//*/*
/* ByteBuf 分配器对象
/*/
private final ByteBufAllocator alloc;
//*/*
/* 数据 ByteBuffer 对象
/*/
private ByteBuffer buffer;
//*/*
/* 临时 ByteBuffer 对象
/*/
private ByteBuffer tmpNioBuf;
//*/*
/* 容量
/*/
private int capacity;
//*/*
/* 是否需要释放 <1>
/*
/* 如果 {@link /#buffer} 从外部传入，则需要进行释放，即 {@link /#UnpooledDirectByteBuf(ByteBufAllocator, ByteBuffer, int)} 构造方法。
/*/
private boolean doNotFree;
public UnpooledDirectByteBuf(ByteBufAllocator alloc, int initialCapacity, int maxCapacity){
// 设置最大容量
super(maxCapacity);
if (alloc == null) {
throw new NullPointerException("alloc");
}
if (initialCapacity < 0) {
throw new IllegalArgumentException("initialCapacity: " + initialCapacity);
}
if (maxCapacity < 0) {
throw new IllegalArgumentException("maxCapacity: " + maxCapacity);
}
if (initialCapacity > maxCapacity) {
throw new IllegalArgumentException(String.format("initialCapacity(%d) > maxCapacity(%d)", initialCapacity, maxCapacity));
}
this.alloc = alloc;
// 创建 Direct ByteBuffer 对象
// 设置数据 ByteBuffer 对象
setByteBuffer(ByteBuffer.allocateDirect(initialCapacity));
}
protected UnpooledDirectByteBuf(ByteBufAllocator alloc, ByteBuffer initialBuffer, int maxCapacity){
// 设置最大容量
super(maxCapacity);
if (alloc == null) {
throw new NullPointerException("alloc");
}
if (initialBuffer == null) {
throw new NullPointerException("initialBuffer");
}
if (!initialBuffer.isDirect()) { // 必须是 Direct
throw new IllegalArgumentException("initialBuffer is not a direct buffer.");
}
if (initialBuffer.isReadOnly()) { // 必须可写
throw new IllegalArgumentException("initialBuffer is a read-only buffer.");
}
// 获得剩余可读字节数，作为初始容量大小 <2>
int initialCapacity = initialBuffer.remaining();
if (initialCapacity > maxCapacity) {
throw new IllegalArgumentException(String.format(
"initialCapacity(%d) > maxCapacity(%d)", initialCapacity, maxCapacity));
}
this.alloc = alloc;
// 标记为 true 。因为 initialBuffer 是从外部传递进来，释放的工作，不交给当前 UnpooledDirectByteBuf 对象。
doNotFree = true; <1>
// slice 切片
// 设置数据 ByteBuffer 对象
setByteBuffer(initialBuffer.slice().order(ByteOrder.BIG_ENDIAN));
// 设置写索引 <2>
writerIndex(initialCapacity);
}
```

- 代码比较简单，主要要理解下

<1>
和

<2>
两处。

- 调用

/#allocateDirect(int initialCapacity)
方法，创建 Direct ByteBuffer 对象。代码如下：

```
protected ByteBuffer allocateDirect(int initialCapacity){
return ByteBuffer.allocateDirect(initialCapacity);
}
```

- 调用

/#setByteBuffer(ByteBuffer buffer)
方法，设置数据 ByteBuffer 对象。如果有老的**自己的**( 指的是自己创建的 )

buffer
对象，需要进行释放。代码如下：

```
private void setByteBuffer(ByteBuffer buffer){
ByteBuffer oldBuffer = this.buffer;
if (oldBuffer != null) {
// 标记为 false 。因为设置的 ByteBuffer 对象，是 UnpooledDirectByteBuf 自己创建的
if (doNotFree) {
doNotFree = false;
} else {
// 释放老的 buffer 对象
freeDirect(oldBuffer); // <3>
}
}
// 设置 buffer
this.buffer = buffer;
// 重置 tmpNioBuf 为 null
tmpNioBuf = null;
// 设置容量
capacity = buffer.remaining();
}
```

- <3>
  处，调用

/#freeDirect(ByteBuffer buffer)
方法，释放**老的**

buffer
对象。详细解析，见 [「3.1.3 deallocate」]() 。

### []( "3.1.2 capacity")3.1.2 capacity

/#capacity()
方法，获得容量。代码如下：

```
@Override
public int capacity(){
return capacity;
}
```

/#capacity(int newCapacity)
方法，调整容量大小。在这个过程中，根据情况，可能对

buffer
扩容或缩容。代码如下：

```
@SuppressWarnings("Duplicates")
@Override
public ByteBuf capacity(int newCapacity){
// 校验新的容量，不能超过最大容量
checkNewCapacity(newCapacity);
int readerIndex = readerIndex();
int writerIndex = writerIndex();
int oldCapacity = capacity;
// 扩容
if (newCapacity > oldCapacity) {
ByteBuffer oldBuffer = buffer;
// 创建新的 Direct ByteBuffer 对象
ByteBuffer newBuffer = allocateDirect(newCapacity);
// 复制数据到新的 buffer 对象
oldBuffer.position(0).limit(oldBuffer.capacity());
newBuffer.position(0).limit(oldBuffer.capacity());
newBuffer.put(oldBuffer);
newBuffer.clear(); // 因为读取和写入，使用 readerIndex 和 writerIndex ，所以没关系。
// 设置新的 buffer 对象，并根据条件释放老的 buffer 对象
setByteBuffer(newBuffer);
// 缩容
} else if (newCapacity < oldCapacity) {
ByteBuffer oldBuffer = buffer;
// 创建新的 Direct ByteBuffer 对象
ByteBuffer newBuffer = allocateDirect(newCapacity);
if (readerIndex < newCapacity) {
// 如果写索引超过新容量，需要重置下，设置为最大容量。否则就越界了。
if (writerIndex > newCapacity) {
writerIndex(writerIndex = newCapacity);
}
// 复制数据到新的 buffer 对象
oldBuffer.position(readerIndex).limit(writerIndex);
newBuffer.position(readerIndex).limit(writerIndex);
newBuffer.put(oldBuffer);
newBuffer.clear(); // 因为读取和写入，使用 readerIndex 和 writerIndex ，所以没关系。
} else {
// 因为读索引超过新容量，所以写索引超过新容量
// 如果读写索引都超过新容量，需要重置下，都设置为最大容量。否则就越界了。
setIndex(newCapacity, newCapacity);
// 这里要注意下，老的数据，相当于不进行复制，因为已经读取完了。
}
// 设置新的 buffer 对象，并根据条件释放老的 buffer 对象
setByteBuffer(newBuffer);
}
return this;
}
```

- 虽然代码比较长，实际很简单。胖友自己耐心看下注释进行理解下噢。

### []( "3.1.3 deallocate")3.1.3 deallocate

/#deallocate()
方法，当引用计数为 0 时，调用该方法，进行内存回收。代码如下：

```
@Override
protected void deallocate(){
ByteBuffer buffer = this.buffer;
if (buffer == null) {
return;
}
// 置空 buffer 属性
this.buffer = null;
// 释放 buffer 对象
if (!doNotFree) {
freeDirect(buffer);
}
}
```

- /#freeDirect(ByteBuffer buffer)
  方法，释放

buffer
对象。代码如下：

```
protected void freeArray(byte[] array){
PlatformDependent.freeDirectBuffer(buffer);
}
// PlatformDependent.java
private static final Cleaner NOOP = new Cleaner() { ... }
public static void freeDirectBuffer(ByteBuffer buffer){
CLEANER.freeDirectBuffer(buffer);
}
```

- 通过调用

io.netty.util.internal.Cleaner/#freeDirectBuffer(ByteBuffer buffer)
方法，释放 Direct ByteBuffer 对象。因为 Java 的版本不同，调用的方法，所以 Cleaner 有两个 实现类：

- io.netty.util.internal.CleanerJava9
  ，适用于 Java9+ 的版本，通过反射调用 DirectByteBuffer 对象的

/#invokeCleaner()
方法，进行释放。

- io.netty.util.internal.CleanerJava6
  ，适用于 Java6+ 的版本，通过反射获得 DirectByteBuffer 对象的

/#cleaner()
方法，从而调用

sun.misc.Cleaner/#clean()
方法，进行释放。

- 虽然实现略有不同，但是原理是一致的。感兴趣的胖友，自己看下 CleanerJava9 和 CleanerJava6 的实现代码。

### []( "3.1.4 其它方法")3.1.4 其它方法

其他方法，和 [「2.2 PooledDirectByteBuf」]() 基本一致。

## []( "3.2 UnpooledHeapByteBuf")3.2 UnpooledHeapByteBuf

io.netty.buffer.UnpooledHeapByteBuf
，实现 AbstractReferenceCountedByteBuf 抽象类，对应 [「2.3 PooledHeapByteBuf」]() 的**非池化** ByteBuf 实现类。

### []( "3.2.1 构造方法")3.2.1 构造方法

```
//*/*
/* ByteBuf 分配器对象
/*/
private final ByteBufAllocator alloc;
//*/*
/* 字节数组
/*/
byte[] array;
//*/*
/* 临时 ByteBuff 对象
/*/
private ByteBuffer tmpNioBuf;
public UnpooledHeapByteBuf(ByteBufAllocator alloc, int initialCapacity, int maxCapacity){
// 设置最大容量
super(maxCapacity);
checkNotNull(alloc, "alloc");
if (initialCapacity > maxCapacity) {
throw new IllegalArgumentException(String.format(
"initialCapacity(%d) > maxCapacity(%d)", initialCapacity, maxCapacity));
}
this.alloc = alloc;
// 创建并设置字节数组
setArray(allocateArray(initialCapacity));
// 设置读写索引
setIndex(0, 0);
}
protected UnpooledHeapByteBuf(ByteBufAllocator alloc, byte[] initialArray, int maxCapacity){
// 设置最大容量
super(maxCapacity);
checkNotNull(alloc, "alloc");
checkNotNull(initialArray, "initialArray");
if (initialArray.length > maxCapacity) {
throw new IllegalArgumentException(String.format(
"initialCapacity(%d) > maxCapacity(%d)", initialArray.length, maxCapacity));
}
this.alloc = alloc;
// 设置字节数组
setArray(initialArray);
// 设置读写索引
setIndex(0, initialArray.length);
}
```

- 第一、二个构造方法的区别，后者字节数组是否从方法参数(

initialArray
)传递进来。

- 调用

/#allocateArray(int initialCapacity)
方法，创建字节数组。

```
protected byte[] allocateArray(int initialCapacity) {
return new byte[initialCapacity];
}
```

- 调用

/#setArray(byte[] initialArray)
方法，设置

array
属性。代码如下：

```
private void setArray(byte[] initialArray){
array = initialArray;
tmpNioBuf = null;
}
```

/#/#/# 3.2.2 capacity
`/#capacity()` 方法，获得容量。代码如下：

```Java
@Override
public int capacity(){
return array.length;
}
```

- 使用字节数组的大小，作为当前容量上限。

/#capacity(int newCapacity)
方法，调整容量大小。在这个过程中，根据情况，可能对

array
扩容或缩容。代码如下：

```
@Override
public ByteBuf capacity(int newCapacity){
// // 校验新的容量，不能超过最大容量
checkNewCapacity(newCapacity);
int oldCapacity = array.length;
byte[] oldArray = array;
// 扩容
if (newCapacity > oldCapacity) {
// 创建新数组
byte[] newArray = allocateArray(newCapacity);
// 复制【全部】数据到新数组
System.arraycopy(oldArray, 0, newArray, 0, oldArray.length);
// 设置数组
setArray(newArray);
// 释放老数组
freeArray(oldArray);
// 缩容
} else if (newCapacity < oldCapacity) {
// 创建新数组
byte[] newArray = allocateArray(newCapacity);
int readerIndex = readerIndex();
if (readerIndex < newCapacity) {
// 如果写索引超过新容量，需要重置下，设置为最大容量。否则就越界了。
int writerIndex = writerIndex();
if (writerIndex > newCapacity) {
writerIndex(writerIndex = newCapacity);
}
// 只复制【读取段】数据到新数组
System.arraycopy(oldArray, readerIndex, newArray, readerIndex, writerIndex - readerIndex);
} else {
// 因为读索引超过新容量，所以写索引超过新容量
// 如果读写索引都超过新容量，需要重置下，都设置为最大容量。否则就越界了。
setIndex(newCapacity, newCapacity);
// 这里要注意下，老的数据，相当于不进行复制，因为已经读取完了。
}
// 设置数组
setArray(newArray);
// 释放老数组
freeArray(oldArray);
}
return this;
}
```

- 虽然代码比较长，实际很简单。胖友自己耐心看下注释进行理解下噢。😈 和 [「3.1.2 capacity」]() 基本一直的。

### []( "3.2.3 deallocate")3.2.3 deallocate

/#deallocate()
方法，当引用计数为 0 时，调用该方法，进行内存回收。代码如下：

```
@Override
protected void deallocate(){
// 释放老数组
freeArray(array);
// 设置为空字节数组
array = EmptyArrays.EMPTY_BYTES;
}
```

- /#freeArray(byte[] array)
  方法，释放数组。代码如下：

```
protected void freeArray(byte[] array){
// NOOP
}
```

- 字节数组，无引用后，自然就会被 GC 回收。

### []( "3.2.4 其它方法")3.2.4 其它方法

其它方法，和 [「2.3 PooledHeapByteBuf」]() 基本一致。

## []( "3.3 UnpooledUnsafeDirectByteBuf")3.3 UnpooledUnsafeDirectByteBuf

io.netty.buffer.UnpooledUnsafeDirectByteBuf
，实现 AbstractReferenceCountedByteBuf 抽象类，对应

「2.4 PooledUnsafeDirectByteBuf」
的**非池化** ByteBuf 实现类。

- 构造方法、

/#capacity(...)
方法、

/#deallocate()
方法，和 [「3.1 PooledDirectByteBuf」]() 基本一致。

- 其它方法，和 [「2.4 PooledUnsafeDirectByteBuf」]() 基本一致。

另外，UnpooledUnsafeDirectByteBuf 有一个子类 UnpooledUnsafeNoCleanerDirectByteBuf ，用于

netty-microbench
模块，进行基准测试。感兴趣的胖友，可以自己看看。

## []( "3.4 UnpooledUnsafeHeapByteBuf")3.4 UnpooledUnsafeHeapByteBuf

io.netty.buffer.UnpooledUnsafeHeapByteBuf
，实现 AbstractReferenceCountedByteBuf 抽象类，对应

「2.5 PooledUnsafeHeapByteBuf」
的**非池化** ByteBuf 实现类。

- 构造方法、

/#capacity(...)
方法、

/#deallocate()
方法，和 [「3.2 PooledHeapByteBuf」]() 基本一致。

- 其它方法，和 [「2.5 PooledUnsafeHeapByteBuf」]() 基本一致。

## []( "3.5 ThreadLocal ByteBuf")3.5 ThreadLocal ByteBuf

老艿艿：这是本文的拓展内容。

虽然 UnpooledByteBuf 不基于**对象池**实现，但是考虑到 NIO Direct ByteBuffer 申请的成本是比较高的，所以基于 ThreadLocal + Recycler 实现重用。

ByteBufUtil/#threadLocalDirectBuffer()
方法，创建 ThreadLocal ByteBuf 对象。代码如下：

```
private static final int THREAD_LOCAL_BUFFER_SIZE;
static {
THREAD_LOCAL_BUFFER_SIZE = SystemPropertyUtil.getInt("io.netty.threadLocalDirectBufferSize", 0);
}
//*/*
/* Returns a cached thread-local direct buffer, if available.
/*
/* @return a cached thread-local direct buffer, if available. {@code null} otherwise.
/*/
public static ByteBuf threadLocalDirectBuffer(){
if (THREAD_LOCAL_BUFFER_SIZE <= 0) {
return null;
}
if (PlatformDependent.hasUnsafe()) {
return ThreadLocalUnsafeDirectByteBuf.newInstance();
} else {
return ThreadLocalDirectByteBuf.newInstance();
}
}
```

- THREAD_LOCAL_BUFFER_SIZE
  **静态**属性，通过

"io.netty.threadLocalDirectBufferSize"
配置，默认为 0 。也就是说，实际

/#threadLocalDirectBuffer()
方法，返回

null
，不开启 ThreadLocal ByteBuf 的功能。

- 根据是否支持 Unsafe 操作，创建 ThreadLocalUnsafeDirectByteBuf 或 ThreadLocalDirectByteBuf 对象。

### []( "3.5.1 ThreadLocalUnsafeDirectByteBuf")3.5.1 ThreadLocalUnsafeDirectByteBuf

ThreadLocalUnsafeDirectByteBuf ，在 ByteBufUtil 的**内部静态类**，继承 UnpooledUnsafeDirectByteBuf 类。代码如下：

```
static final class ThreadLocalUnsafeDirectByteBuf extends UnpooledUnsafeDirectByteBuf{
//*/*
/* Recycler 对象
/*/
private static final Recycler<ThreadLocalUnsafeDirectByteBuf> RECYCLER =
new Recycler<ThreadLocalUnsafeDirectByteBuf>() {
@Override
protected ThreadLocalUnsafeDirectByteBuf newObject(Handle<ThreadLocalUnsafeDirectByteBuf> handle){
return new ThreadLocalUnsafeDirectByteBuf(handle);
}
};
static ThreadLocalUnsafeDirectByteBuf newInstance(){
// 从 RECYCLER 中，获得 ThreadLocalUnsafeDirectByteBuf 对象
ThreadLocalUnsafeDirectByteBuf buf = RECYCLER.get();
// 初始化 ref 为 1
buf.setRefCnt(1);
return buf;
}
//*/*
/* Recycler 处理器
/*/
private final Handle<ThreadLocalUnsafeDirectByteBuf> handle;
private ThreadLocalUnsafeDirectByteBuf(Handle<ThreadLocalUnsafeDirectByteBuf> handle){
super(UnpooledByteBufAllocator.DEFAULT, 256, Integer.MAX_VALUE);
this.handle = handle;
}
@Override
protected void deallocate(){
if (capacity() > THREAD_LOCAL_BUFFER_SIZE) { // <1>
// 释放
super.deallocate();
} else {
// 清空
clear();
// 回收对象
handle.recycle(this);
}
}
}
```

- 在

<1>
处，我们可以看到，只有 ByteBuffer 容量小于

THREAD_LOCAL_BUFFER_SIZE
时，才会重用 ByteBuffer 对象。

### []( "3.5.2 ThreadLocalDirectByteBuf")3.5.2 ThreadLocalDirectByteBuf

ThreadLocalUnsafeDirectByteBuf ，在 ByteBufUtil 的**内部静态类**，继承 UnpooledDirectByteBuf 类。代码如下：

```
static final class ThreadLocalDirectByteBuf extends UnpooledDirectByteBuf{
//*/*
/* Recycler 对象
/*/
private static final Recycler<ThreadLocalDirectByteBuf> RECYCLER = new Recycler<ThreadLocalDirectByteBuf>() {
@Override
protected ThreadLocalDirectByteBuf newObject(Handle<ThreadLocalDirectByteBuf> handle){
return new ThreadLocalDirectByteBuf(handle);
}
};
static ThreadLocalDirectByteBuf newInstance(){
// 从 RECYCLER 中，获得 ThreadLocalUnsafeDirectByteBuf 对象
ThreadLocalDirectByteBuf buf = RECYCLER.get();
// 初始化 ref 为 1
buf.setRefCnt(1);
return buf;
}
//*/*
/* Recycler 处理器
/*/
private final Handle<ThreadLocalDirectByteBuf> handle;
private ThreadLocalDirectByteBuf(Handle<ThreadLocalDirectByteBuf> handle){
super(UnpooledByteBufAllocator.DEFAULT, 256, Integer.MAX_VALUE);
this.handle = handle;
}
@Override
protected void deallocate(){
if (capacity() > THREAD_LOCAL_BUFFER_SIZE) {
// 释放
super.deallocate();
} else {
// 清理
clear();
// 回收
handle.recycle(this);
}
}
}
```

## []( "3.6 WrappedUnpooledUnsafeDirectByteBuf")3.6 WrappedUnpooledUnsafeDirectByteBuf

老艿艿：这是本文的拓展内容。

io.netty.buffer.WrappedUnpooledUnsafeDirectByteBuf
，继承 UnpooledUnsafeDirectByteBuf 类，基于

memoryAddress
内存地址，创建 Direct ByteBuf 对象。代码如下：

```
final class WrappedUnpooledUnsafeDirectByteBuf extends UnpooledUnsafeDirectByteBuf{
// 基于 memoryAddress 内存地址，创建 Direct ByteBuf 对象
WrappedUnpooledUnsafeDirectByteBuf(ByteBufAllocator alloc, long memoryAddress, int size, boolean doFree) {
super(alloc, PlatformDependent.directBuffer(memoryAddress, size) //*/* 创建 Direct ByteBuf 对象 /*/*/, size, doFree);
}
@Override
protected void freeDirect(ByteBuffer buffer){
PlatformDependent.freeMemory(memoryAddress);
}
}
```

FROM [《Netty 源码分析（一） ByteBuf》](https://www.jianshu.com/p/b833254908f7)

创建一个指定内存地址的 UnpooledUnsafeDirectByteBuf，该内存块可能已被写入数据以减少一步拷贝操作。

# []( "666. 彩蛋")666. 彩蛋

每次这种 N 多实现类的源码解析，写到 60% 的时候，就特别头疼。不是因为难写，是因为基本是组合排列，不断在啰嗦啰嗦啰嗦的感觉。

嗯嗯，如果有地方写的错乱，烦请指出。默默再 review 几遍。

推荐阅读文章：

- HryReal [《PooledByteBuf 源码分析》](https://blog.csdn.net/qq_33394088/article/details/72763305)
- 江南白衣 [《Netty 之 Java 堆外内存扫盲贴》](http://calvin1978.blogcn.com/articles/directbytebuffer.html)
