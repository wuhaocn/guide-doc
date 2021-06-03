# 精尽 Netty 源码解析 —— Buffer 之 ByteBuf（一）简介

# []( "1. 概述")1. 概述

从本文开始，我们来分享 Netty ByteBuf 相关的内容。它在

buffer
模块中实现，在功能定位上，它和 NIO ByteBuffer 是一致的，但是强大非常多。如下是 [《Netty 实战》]() 对它的**优点总**结：

- A01. 它可以被用户自定义的**缓冲区类型**扩展
- A02. 通过内置的符合缓冲区类型实现了透明的**零拷贝**
- A03. 容量可以**按需增长**
- A04. 在读和写这两种模式之间切换不需要调用

/#flip()
方法

- A05. 读和写使用了**不同的索引**
- A06. 支持方法的**链式**调用
- A07. 支持引用计数
- A08. 支持**池化**

- 特别是第 A04 这点，相信很多胖友都被 NIO ByteBuffer 反人类的读模式和写模式给坑哭了。在 [《精尽 Netty 源码分析 —— NIO 基础（三）之 Buffer》](http://svip.iocoder.cn/Netty/nio-3-buffer/) 中，我们也吐槽过了。😈
- 当然，可能胖友看着这些优点，会一脸懵逼，不要紧，边读源码边理解落。
  老艿艿，从下文开始，Netty ByteBuf ，我们只打 ByteBuf 。相比 NIO ByteBuffer ，它少

"fer"
三个字母。

ByteBuf 的代码实现挺有趣的，但是会略有一点点深度，所以笔者会分成三大块来分享：

- ByteBuf 相关，主要是它的核心 API 和核心子类实现。
- ByteBufAllocator 相关，用于创建 ByteBuf 对象。
- Jemalloc 相关，内存管理算法，Netty 基于该算法，实现对内存高效和有效的管理。😈 这块是最最最有趣的。

每一块，我们会分成几篇小的文章。而本文，我们就来对 ByteBuf 有个整体的认识，特别是核心 API 部分。

# []( "2. ByteBuf")2. ByteBuf

io.netty.buffer.ByteBuf
，实现 ReferenceCounted 、Comparable 接口，ByteBuf **抽象类**。注意，ByteBuf 是一个抽象类，而不是一个接口。当然，实际上，它主要定义了**抽象**方法，**很少**实现对应的方法。

关于

io.netty.util.ReferenceCounted
接口，对象引用计数器接口。

- 对象的初始引用计数为 1 。
- 当引用计数器值为 0 时，表示该对象不能再被继续引用，只能被释放。
- 本文暂时不解析，我们会在 TODO 1011

## []( "2.1 抽象方法")2.1 抽象方法

因为 ByteBuf 的方法非常多，所以笔者对它的方法做了简单的归类。Let’s Go 。

### []( "2.1.1 基础信息")2.1.1 基础信息

```
public abstract int capacity(); // 容量
public abstract ByteBuf capacity(int newCapacity);
public abstract int maxCapacity(); // 最大容量
public abstract ByteBufAllocator alloc(); // 分配器，用于创建 ByteBuf 对象。
@Deprecated
public abstract ByteOrder order(); // 字节序，即大小端。推荐阅读 http://www.ruanyifeng.com/blog/2016/11/byte-order.html
@Deprecated
public abstract ByteBuf order(ByteOrder endianness);
public abstract ByteBuf unwrap(); // 获得被包装( wrap )的 ByteBuf 对象。
public abstract boolean isDirect(); // 是否 NIO Direct Buffer
public abstract boolean isReadOnly(); // 是否为只读 Buffer
public abstract ByteBuf asReadOnly();
public abstract int readerIndex(); // 读取位置
public abstract ByteBuf readerIndex(int readerIndex);
public abstract int writerIndex(); // 写入位置
public abstract ByteBuf writerIndex(int writerIndex);
public abstract ByteBuf setIndex(int readerIndex, int writerIndex); // 设置读取和写入位置
public abstract int readableBytes(); // 剩余可读字节数
public abstract int writableBytes(); // 剩余可写字节数
public abstract int maxWritableBytes();
public abstract boolean isReadable();
public abstract boolean isReadable(int size);
public abstract boolean isWritable();
public abstract boolean isWritable(int size);
public abstract ByteBuf ensureWritable(int minWritableBytes);
public abstract int ensureWritable(int minWritableBytes, boolean force);
public abstract ByteBuf markReaderIndex(); // 标记读取位置
public abstract ByteBuf resetReaderIndex();
public abstract ByteBuf markWriterIndex(); // 标记写入位置
public abstract ByteBuf resetWriterIndex();
```

主要是如下四个属性：

- readerIndex
  ，读索引。
- writerIndex
  ，写索引。
- capacity
  ，当前容量。
- maxCapacity
  ，最大容量。当

writerIndex
写入超过

capacity
时，可自动扩容。**每次**扩容的大小，为

capacity
的 2 倍。当然，前提是不能超过

maxCapacity
大小。

所以，ByteBuf 通过

readerIndex
和

writerIndex
两个索引，解决 ByteBuffer 的读写模式的问题。

四个大小关系很简单：

readerIndex
<=

writerIndex
<=

capacity
<=

maxCapacity
。如下图所示：[![分段](http://static2.iocoder.cn/images/Netty/2018_08_01/01.png)](http://static2.iocoder.cn/images/Netty/2018_08_01/01.png '分段')分段

- 图中一共有三段，实际是四段，省略了

capacity
到

maxCapacity
之间的一段。

- discardable bytes ，废弃段。一般情况下，可以理解成已读的部分。
- readable bytes ，可读段。可通过

/#readXXX()
方法，顺序向下读取。

- writable bytes ，可写段。可通过

/#writeXXX()
方法，顺序向下写入。

另外，ByteBuf 还有

markReaderIndex
和

markWriterIndex
两个属性：

- 通过对应的

/#markReaderIndex()
和

/#markWriterIndex()
方法，分别标记读取和写入位置。

- 通过对应的

/#resetReaderIndex()
和

/#resetWriterIndex()
方法，分别读取和写入位置到标记处。

### []( "3.1.2 读取 / 写入操作")3.1.2 读取 / 写入操作

```
// Boolean 1 字节
public abstract boolean getBoolean(int index);
public abstract ByteBuf setBoolean(int index, boolean value);
public abstract boolean readBoolean();
public abstract ByteBuf writeBoolean(boolean value);
// Byte 1 字节
public abstract byte getByte(int index);
public abstract short getUnsignedByte(int index);
public abstract ByteBuf setByte(int index, int value);
public abstract byte readByte();
public abstract short readUnsignedByte();
public abstract ByteBuf writeByte(int value);
// Short 2 字节
public abstract short getShort(int index);
public abstract short getShortLE(int index);
public abstract int getUnsignedShort(int index);
public abstract int getUnsignedShortLE(int index);
public abstract ByteBuf setShort(int index, int value);
public abstract ByteBuf setShortLE(int index, int value);
public abstract short readShort();
public abstract short readShortLE();
public abstract int readUnsignedShort();
public abstract int readUnsignedShortLE();
public abstract ByteBuf writeShort(int value);
public abstract ByteBuf writeShortLE(int value);
// 【特殊】Medium 3 字节
public abstract int getMedium(int index);
public abstract int getMediumLE(int index);
public abstract int getUnsignedMedium(int index);
public abstract int getUnsignedMediumLE(int index);
public abstract ByteBuf setMedium(int index, int value);
public abstract ByteBuf setMediumLE(int index, int value);
public abstract int readMedium();
public abstract int readMediumLE();
public abstract int readUnsignedMedium();
public abstract int readUnsignedMediumLE();
public abstract ByteBuf writeMedium(int value);
public abstract ByteBuf writeMediumLE(int value);
// Int 4 字节
public abstract int getInt(int index);
public abstract int getIntLE(int index);
public abstract long getUnsignedInt(int index);
public abstract long getUnsignedIntLE(int index);
public abstract ByteBuf setInt(int index, int value);
public abstract ByteBuf setIntLE(int index, int value);
public abstract int readInt();
public abstract int readIntLE();
public abstract long readUnsignedInt();
public abstract long readUnsignedIntLE();
public abstract ByteBuf writeInt(int value);
public abstract ByteBuf writeIntLE(int value);
// Long 8 字节
public abstract long getLong(int index);
public abstract long getLongLE(int index);
public abstract ByteBuf setLong(int index, long value);
public abstract ByteBuf setLongLE(int index, long value);
public abstract long readLong();
public abstract long readLongLE();
public abstract ByteBuf writeLong(long value);
public abstract ByteBuf writeLongLE(long value);
// Char 2 字节
public abstract char getChar(int index);
public abstract ByteBuf setChar(int index, int value);
public abstract char readChar();
public abstract ByteBuf writeChar(int value);
// Float 4 字节
public abstract float getFloat(int index);
public float getFloatLE(int index){
return Float.intBitsToFloat(getIntLE(index));
}
public abstract ByteBuf setFloat(int index, float value);
public ByteBuf setFloatLE(int index, float value){
return setIntLE(index, Float.floatToRawIntBits(value));
}
public abstract float readFloat();
public float readFloatLE(){
return Float.intBitsToFloat(readIntLE());
}
public abstract ByteBuf writeFloat(float value);
public ByteBuf writeFloatLE(float value){
return writeIntLE(Float.floatToRawIntBits(value));
}
// Double 8 字节
public abstract double getDouble(int index);
public double getDoubleLE(int index){
return Double.longBitsToDouble(getLongLE(index));
}
public abstract ByteBuf setDouble(int index, double value);
public ByteBuf setDoubleLE(int index, double value){
return setLongLE(index, Double.doubleToRawLongBits(value));
}
public abstract double readDouble();
public double readDoubleLE(){
return Double.longBitsToDouble(readLongLE());
}
public abstract ByteBuf writeDouble(double value);
public ByteBuf writeDoubleLE(double value){
return writeLongLE(Double.doubleToRawLongBits(value));
}
// Byte 数组
public abstract ByteBuf getBytes(int index, ByteBuf dst);
public abstract ByteBuf getBytes(int index, ByteBuf dst, int length);
public abstract ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length);
public abstract ByteBuf getBytes(int index, byte[] dst);
public abstract ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length);
public abstract ByteBuf getBytes(int index, ByteBuffer dst);
public abstract ByteBuf getBytes(int index, OutputStream out, int length) throws IOException;
public abstract int getBytes(int index, GatheringByteChannel out, int length) throws IOException;
public abstract int getBytes(int index, FileChannel out, long position, int length) throws IOException;
public abstract ByteBuf setBytes(int index, ByteBuf src);
public abstract ByteBuf setBytes(int index, ByteBuf src, int length);
public abstract ByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length);
public abstract ByteBuf setBytes(int index, byte[] src);
public abstract ByteBuf setBytes(int index, byte[] src, int srcIndex, int length);
public abstract ByteBuf setBytes(int index, ByteBuffer src);
public abstract int setBytes(int index, InputStream in, int length) throws IOException;
public abstract int setBytes(int index, ScatteringByteChannel in, int length) throws IOException;
public abstract int setBytes(int index, FileChannel in, long position, int length) throws IOException;
public abstract ByteBuf setZero(int index, int length);
public abstract ByteBuf readBytes(int length);
public abstract ByteBuf readSlice(int length);
public abstract ByteBuf readRetainedSlice(int length);
public abstract ByteBuf readBytes(ByteBuf dst);
public abstract ByteBuf readBytes(ByteBuf dst, int length);
public abstract ByteBuf readBytes(ByteBuf dst, int dstIndex, int length);
public abstract ByteBuf readBytes(byte[] dst);
public abstract ByteBuf readBytes(byte[] dst, int dstIndex, int length);
public abstract ByteBuf readBytes(ByteBuffer dst);
public abstract ByteBuf readBytes(OutputStream out, int length) throws IOException;
public abstract int readBytes(GatheringByteChannel out, int length) throws IOException;
public abstract int readBytes(FileChannel out, long position, int length) throws IOException;
public abstract ByteBuf skipBytes(int length); // 忽略指定长度的字节数
public abstract ByteBuf writeBytes(ByteBuf src);
public abstract ByteBuf writeBytes(ByteBuf src, int length);
public abstract ByteBuf writeBytes(ByteBuf src, int srcIndex, int length);
public abstract ByteBuf writeBytes(byte[] src);
public abstract ByteBuf writeBytes(byte[] src, int srcIndex, int length);
public abstract ByteBuf writeBytes(ByteBuffer src);
public abstract int writeBytes(InputStream in, int length) throws IOException;
public abstract int writeBytes(ScatteringByteChannel in, int length) throws IOException;
public abstract int writeBytes(FileChannel in, long position, int length) throws IOException;
public abstract ByteBuf writeZero(int length); // 填充指定长度的 0
// String
public abstract CharSequence getCharSequence(int index, int length, Charset charset);
public abstract int setCharSequence(int index, CharSequence sequence, Charset charset);
public abstract CharSequence readCharSequence(int length, Charset charset);
public abstract int writeCharSequence(CharSequence sequence, Charset charset);
```

虽然方法比较多，总结下来是不同数据类型的**四种**读写方法：

- /#getXXX(index)
  方法，读取**指定**位置的数据，不改变

readerIndex
索引。

- /#readXXX()
  方法，读取

readerIndex
位置的数据，会改成

readerIndex
索引。

- /#setXXX(index, value)
  方法，写入数据到**指定**位置，不改变

writeIndex
索引。

- /#writeXXX(value)
  方法，写入数据到**指定**位置，会改变

writeIndex
索引。

### []( "2.1.3 查找 / 遍历操作")2.1.3 查找 / 遍历操作

```
public abstract int indexOf(int fromIndex, int toIndex, byte value); // 指定值( value ) 在 ByteBuf 中的位置
public abstract int bytesBefore(byte value);
public abstract int bytesBefore(int length, byte value);
public abstract int bytesBefore(int index, int length, byte value);
public abstract int forEachByte(ByteProcessor processor); // 遍历 ByteBuf ，进行自定义处理
public abstract int forEachByte(int index, int length, ByteProcessor processor);
public abstract int forEachByteDesc(ByteProcessor processor);
public abstract int forEachByteDesc(int index, int length, ByteProcessor processor);
```

### []( "3.1.4 释放操作")3.1.4 释放操作

```
public abstract ByteBuf discardReadBytes(); // 释放已读的字节空间
public abstract ByteBuf discardSomeReadBytes(); // 释放部分已读的字节空间
public abstract ByteBuf clear(); // 清空字节空间。实际是修改 readerIndex=writerIndex=0，标记清空。
```

**discardReadBytes**

/#discardReadBytes()
方法，释放【所有的】**废弃段**的空间内存。

- 优点：达到重用废弃段的空间内存。
- 缺点：释放的方式，是通过复制**可读段**到 ByteBuf 的头部。所以，频繁释放会导致性能下降。
- 总结：这是典型的问题：选择空间还是时间。具体的选择，需要看对应的场景。😈 后续的文章，我们会看到对该方法的调用。

整个过程如下图：[![discardReadBytes](http://static2.iocoder.cn/images/Netty/2018_08_01/02.png)](http://static2.iocoder.cn/images/Netty/2018_08_01/02.png 'discardReadBytes')discardReadBytes

**discardSomeReadBytes**

/#discardSomeReadBytes()
方法，释放【部分的】**废弃段**的空间内存。

这是对

/#discardSomeReadBytes()
方法的这种方案，具体的实现，见 [「4. AbstractByteBuf」]() 中。

**clear**

/#clear()
方法，清空字节空间。实际是修改

readerIndex = writerIndex = 0
，标记清空。

- 优点：通过标记来实现清空，避免置空 ByteBuf ，提升性能。
- 缺点：数据实际还存在，如果错误修改

writerIndex
时，会导致读到“脏”数据。

整个过程如下图：[![discardReadBytes](http://static2.iocoder.cn/images/Netty/2018_08_01/03.png)](http://static2.iocoder.cn/images/Netty/2018_08_01/03.png 'discardReadBytes')discardReadBytes

### []( "3.1.5 拷贝操作")3.1.5 拷贝操作

```
public abstract ByteBuf copy(); // 拷贝可读部分的字节数组。独立，互相不影响。
public abstract ByteBuf copy(int index, int length);
public abstract ByteBuf slice(); // 拷贝可读部分的字节数组。共享，相互影响。
public abstract ByteBuf slice(int index, int length);
public abstract ByteBuf retainedSlice();
public abstract ByteBuf duplicate(); // 拷贝整个的字节数组。共享，相互影响。
public abstract ByteBuf retainedDuplicate();
```

### []( "3.1.6 转换 NIO ByteBuffer 操作")3.1.6 转换 NIO ByteBuffer 操作

```
// ByteBuf 包含 ByteBuffer 数量。
// 如果返回 = 1 ，则调用 `/#nioBuffer()` 方法，获得 ByteBuf 包含的 ByteBuffer 对象。
// 如果返回 > 1 ，则调用 `/#nioBuffers()` 方法，获得 ByteBuf 包含的 ByteBuffer 数组。
public abstract int nioBufferCount();
public abstract ByteBuffer nioBuffer();
public abstract ByteBuffer nioBuffer(int index, int length);
public abstract ByteBuffer internalNioBuffer(int index, int length);
public abstract ByteBuffer[] nioBuffers();
public abstract ByteBuffer[] nioBuffers(int index, int length);
```

### []( "3.1.7 Heap 相关方法")3.1.7 Heap 相关方法

```
// 适用于 Heap 类型的 ByteBuf 对象的 byte[] 字节数组
public abstract boolean hasArray(); // 是否有 byte[] 字节数组
public abstract byte[] array();
public abstract int arrayOffset();
```

- 详细解析，见 [《精尽 Netty 源码解析 —— Buffer 之 ByteBuf（二）核心子类》](http://svip.iocoder.cn/Netty/ByteBuf-1-2-ByteBuf-core-impl)

### []( "3.1.8 Unsafe 相关方法")3.1.8 Unsafe 相关方法

```
// 适用于 Unsafe 类型的 ByteBuf 对象
public abstract boolean hasMemoryAddress(); // 是否有内存地址
public abstract long memoryAddress();
```

- 详细解析，见 [《精尽 Netty 源码解析 —— Buffer 之 ByteBuf（二）核心子类》](http://svip.iocoder.cn/Netty/ByteBuf-1-2-ByteBuf-core-impl)

### []( "3.1.9 Object 相关")3.1.9 Object 相关

```
@Override
public abstract String toString();
public abstract String toString(Charset charset);
public abstract String toString(int index, int length, Charset charset);
@Override
public abstract int hashCode();
@Override
public abstract boolean equals(Object obj);
@Override
public abstract int compareTo(ByteBuf buffer);
```

### []( "3.1.10 引用计数相关")3.1.10 引用计数相关

本文暂时不解析，我们会在 TODO 1011 。

来自 ReferenceCounted

[https://skyao.gitbooks.io/learning-netty/content/buffer/interface_ReferenceCounted.html](https://skyao.gitbooks.io/learning-netty/content/buffer/interface_ReferenceCounted.html) 可参考

```
@Override
public abstract ByteBuf retain(int increment);
@Override
public abstract ByteBuf retain();
@Override
public abstract ByteBuf touch();
@Override
public abstract ByteBuf touch(Object hint);
```

## []( "3.2 子类类图")3.2 子类类图

ByteBuf 的子类灰常灰常灰常多，胖友点击 [传送门](http://static2.iocoder.cn/images/Netty/2018_08_01/04.png) 可以进行查看。

本文仅分享 ByteBuf 的**五个**直接子类实现，如下图所示：[![传送门](http://static2.iocoder.cn/images/Netty/2018_08_01/05.png)](http://static2.iocoder.cn/images/Netty/2018_08_01/05.png '传送门')传送门

- 【重点】AbstractByteBuf ，ByteBuf 抽象实现类，提供 ByteBuf 的默认实现类。可以说，是 ByteBuf 最最最重要的子类。详细解析，见 [「4. AbstractByteBuf」]() 。
- EmptyByteBuf ，用于构建空 ByteBuf 对象，

capacity
和

maxCapacity
均为 0 。详细解析，见 [「5. EmptyByteBuf」]() 。

- WrappedByteBuf ，用于装饰 ByteBuf 对象。详细解析，见 [「6. WrappedByteBuf」]() 。
- SwappedByteBuf ，用于构建具有切换**字节序**功能的 ByteBuf 对象。详细解析，见 [「7. SwappedByteBuf」]() 。
- ReplayingDecoderByteBuf ，用于构建在 IO 阻塞条件下实现无阻塞解码的特殊 ByteBuf 对象，当要读取的数据还未接收完全时，抛出异常，交由 ReplayingDecoder 处理。详细解析，见 [「8. ReplayingDecoderByteBuf」]() 。

# []( "4. AbstractByteBuf")4. AbstractByteBuf

io.netty.buffer.AbstractByteBuf
，实现 ByteBuf 抽象类，ByteBuf 抽象实现类。官方注释如下：

```
//*/*
/* A skeletal implementation of a buffer.
/*/
```

因为 AbstractByteBuf 实现类 ByteBuf 超级多的方法，所以我们还是按照 ByteBuf 的归类，逐个分析过去。

## []( "4.1 基础信息")4.1 基础信息

### []( "4.1.1 构造方法")4.1.1 构造方法

```
//*/*
/* 读取位置
/*/
int readerIndex;
//*/*
/* 写入位置
/*/
int writerIndex;
//*/*
/* {@link /#readerIndex} 的标记
/*/
private int markedReaderIndex;
//*/*
/* {@link /#writerIndex} 的标记
/*/
private int markedWriterIndex;
//*/*
/* 最大容量
/*/
private int maxCapacity;
protected AbstractByteBuf(int maxCapacity){
if (maxCapacity < 0) {
throw new IllegalArgumentException("maxCapacity: " + maxCapacity + " (expected: >= 0)");
}
this.maxCapacity = maxCapacity;
}
```

- capacity
  属性，在 AbstractByteBuf 未定义，而是由子类来实现。为什么呢？在后面的文章，我们会看到，ByteBuf 根据**内存类型**分成 Heap 和 Direct ，它们获取

capacity
的值的方式不同。

- maxCapacity
  属性，相关的方法：

```
@Override
public int maxCapacity(){
return maxCapacity;
}
protected final void maxCapacity(int maxCapacity){
this.maxCapacity = maxCapacity;
}
```

### []( "4.1.2 读索引相关的方法")4.1.2 读索引相关的方法

**获取和设置读位置**

```
@Override
public int readerIndex(){
return readerIndex;
}
@Override
public ByteBuf readerIndex(int readerIndex){
if (readerIndex < 0 || readerIndex > writerIndex) {
throw new IndexOutOfBoundsException(String.format(
"readerIndex: %d (expected: 0 <= readerIndex <= writerIndex(%d))", readerIndex, writerIndex));
}
this.readerIndex = readerIndex;
return this;
}
```

**是否可读**

```
@Override
public boolean isReadable(){
return writerIndex > readerIndex;
}
@Override
public boolean isReadable(int numBytes){
return writerIndex - readerIndex >= numBytes;
}
@Override
public int readableBytes(){
return writerIndex - readerIndex;
}
```

**标记和重置读位置**

```
@Override
public ByteBuf markReaderIndex(){
markedReaderIndex = readerIndex;
return this;
}
@Override
public ByteBuf resetReaderIndex(){
readerIndex(markedReaderIndex);
return this;
}
```

### []( "4.1.3 写索引相关的方法")4.1.3 写索引相关的方法

**获取和设置写位置**

```
@Override
public int writerIndex(){
return writerIndex;
}
@Override
public ByteBuf writerIndex(int writerIndex){
if (writerIndex < readerIndex || writerIndex > capacity()) {
throw new IndexOutOfBoundsException(String.format(
"writerIndex: %d (expected: readerIndex(%d) <= writerIndex <= capacity(%d))",
writerIndex, readerIndex, capacity()));
}
this.writerIndex = writerIndex;
return this;
}
```

**是否可写**

```
@Override
public boolean isWritable(){
return capacity() > writerIndex;
}
@Override
public boolean isWritable(int numBytes){
return capacity() - writerIndex >= numBytes;
}
@Override
public int writableBytes(){
return capacity() - writerIndex;
}
@Override
public int maxWritableBytes(){
return maxCapacity() - writerIndex;
}
```

**标记和重置写位置**

```
@Override
public ByteBuf markWriterIndex(){
markedWriterIndex = writerIndex;
return this;
}
@Override
public ByteBuf resetWriterIndex(){
writerIndex(markedWriterIndex);
return this;
}
```

**保证可写**

/#ensureWritable(int minWritableBytes)
方法，保证有足够的可写空间。若不够，则进行扩容。代码如下：

```
1: @Override
2: public ByteBuf ensureWritable(int minWritableBytes){
3: if (minWritableBytes < 0) {
4: throw new IllegalArgumentException(String.format(
5: "minWritableBytes: %d (expected: >= 0)", minWritableBytes));
6: }
7: ensureWritable0(minWritableBytes);
8: return this;
9: }
10:
11: final void ensureWritable0(int minWritableBytes){
12: // 检查是否可访问
13: ensureAccessible();
14: // 目前容量可写，直接返回
15: if (minWritableBytes <= writableBytes()) {
16: return;
17: }
18:
19: // 超过最大上限，抛出 IndexOutOfBoundsException 异常
20: if (minWritableBytes > maxCapacity - writerIndex) {
21: throw new IndexOutOfBoundsException(String.format(
22: "writerIndex(%d) + minWritableBytes(%d) exceeds maxCapacity(%d): %s",
23: writerIndex, minWritableBytes, maxCapacity, this));
24: }
25:
26: // 计算新的容量。默认情况下，2 倍扩容，并且不超过最大容量上限。
27: // Normalize the current capacity to the power of 2.
28: int newCapacity = alloc().calculateNewCapacity(writerIndex + minWritableBytes, maxCapacity);
29:
30: // 设置新的容量大小
31: // Adjust to the new capacity.
32: capacity(newCapacity);
33: }
```

- 第 13 行：调用

/#ensureAccessible()
方法，检查是否可访问。代码如下：

```
//*/*
/* Should be called by every method that tries to access the buffers content to check
/* if the buffer was released before.
/*/
protected final void ensureAccessible(){
if (checkAccessible && refCnt() == 0) { // 若指向为 0 ，说明已经释放，不可继续写入。
throw new IllegalReferenceCountException(0);
}
}
private static final String PROP_MODE = "io.netty.buffer.bytebuf.checkAccessible";
//*/*
/* 是否检查可访问
/*
/* @see /#ensureAccessible()
/*/
private static final boolean checkAccessible;
static {
checkAccessible = SystemPropertyUtil.getBoolean(PROP_MODE, true);
if (logger.isDebugEnabled()) {
logger.debug("-D{}: {}", PROP_MODE, checkAccessible);
}
}
```

- 第 14 至 17 行：目前容量可写，直接返回。
- 第 19 至 24 行：超过最大上限，抛出 IndexOutOfBoundsException 异常。
- 第 28 行：调用

ByteBufAllocator/#calculateNewCapacity(int minNewCapacity, int maxCapacity)
方法，计算新的容量。默认情况下，2 倍扩容，并且不超过最大容量上限。**注意**，此处仅仅是计算，并没有扩容内存复制等等操作。

- 第 32 行：调用

/#capacity(newCapacity)
方法，设置新的容量大小。

/#ensureWritable(int minWritableBytes, boolean force)
方法，保证有足够的可写空间。若不够，则进行扩容。代码如下：

```
@Override
public int ensureWritable(int minWritableBytes, boolean force){
// 检查是否可访问
ensureAccessible();
if (minWritableBytes < 0) {
throw new IllegalArgumentException(String.format(
"minWritableBytes: %d (expected: >= 0)", minWritableBytes));
}
// 目前容量可写，直接返回 0
if (minWritableBytes <= writableBytes()) {
return 0;
}
final int maxCapacity = maxCapacity();
final int writerIndex = writerIndex();
// 超过最大上限
if (minWritableBytes > maxCapacity - writerIndex) {
// 不强制设置，或者已经到达最大容量
if (!force || capacity() == maxCapacity) {
// 返回 1
return 1;
}
// 设置为最大容量
capacity(maxCapacity);
// 返回 3
return 3;
}
// 计算新的容量。默认情况下，2 倍扩容，并且不超过最大容量上限。
// Normalize the current capacity to the power of 2.
int newCapacity = alloc().calculateNewCapacity(writerIndex + minWritableBytes, maxCapacity);
// 设置新的容量大小
// Adjust to the new capacity.
capacity(newCapacity);
// 返回 2
return 2;
}
```

和

/#ensureWritable(int minWritableBytes)
方法，有两点不同：

- 超过最大容量的上限时，不会抛出 IndexOutOfBoundsException 异常。
- 根据执行的过程不同，返回不同的返回值。

比较简单，胖友自己看下代码。

### []( "4.1.4 setIndex")4.1.4 setIndex

```
@Override
public ByteBuf setIndex(int readerIndex, int writerIndex){
if (readerIndex < 0 || readerIndex > writerIndex || writerIndex > capacity()) {
throw new IndexOutOfBoundsException(String.format(
"readerIndex: %d, writerIndex: %d (expected: 0 <= readerIndex <= writerIndex <= capacity(%d))",
readerIndex, writerIndex, capacity()));
}
setIndex0(readerIndex, writerIndex);
return this;
}
final void setIndex0(int readerIndex, int writerIndex){
this.readerIndex = readerIndex;
this.writerIndex = writerIndex;
}
```

### []( "4.1.5 读索引标记位相关的方法")4.1.5 读索引标记位相关的方法

```
@Override
public ByteBuf markReaderIndex(){
markedReaderIndex = readerIndex;
return this;
}
@Override
public ByteBuf resetReaderIndex(){
readerIndex(markedReaderIndex);
return this;
}
```

### []( "4.1.6 写索引标记位相关的方法")4.1.6 写索引标记位相关的方法

```
@Override
public ByteBuf markWriterIndex(){
markedWriterIndex = writerIndex;
return this;
}
@Override
public ByteBuf resetWriterIndex(){
writerIndex(markedWriterIndex);
return this;
}
```

### []( "4.1.7 是否只读相关")4.1.7 是否只读相关

/#isReadOnly()
方法，返回是否只读。代码如下：

```
@Override
public boolean isReadOnly(){
return false;
}
```

- 默认返回

false
。子类可覆写该方法，根据情况返回结果。

/#asReadOnly()
方法，转换成只读 ByteBuf 对象。代码如下：

```
@SuppressWarnings("deprecation")
@Override
public ByteBuf asReadOnly(){
// 如果是只读，直接返回
if (isReadOnly()) {
return this;
}
// 转化成只读 Buffer 对象
return Unpooled.unmodifiableBuffer(this);
}
```

- 如果已是只读，直接返回该 ByteBuf 对象。
- 如果不是只读，调用

Unpooled/#unmodifiableBuffer(Bytebuf)
方法，转化成只读 Buffer 对象。代码如下：

```
//*/*
/* Creates a read-only buffer which disallows any modification operations
/* on the specified {@code buffer}. The new buffer has the same
/* {@code readerIndex} and {@code writerIndex} with the specified
/* {@code buffer}.
/*
/* @deprecated Use {@link ByteBuf/#asReadOnly()}.
/*/
@Deprecated
public static ByteBuf unmodifiableBuffer(ByteBuf buffer){
ByteOrder endianness = buffer.order();
// 大端
if (endianness == BIG_ENDIAN) {
return new ReadOnlyByteBuf(buffer);
}
// 小端
return new ReadOnlyByteBuf(buffer.order(BIG_ENDIAN)).order(LITTLE_ENDIAN);
}
```

- 注意，返回的是**新的**

io.netty.buffer.ReadOnlyByteBuf
对象。并且，和原 ByteBuf 对象，共享

readerIndex
和

writerIndex
索引，以及相关的数据。仅仅是说，只读，不能写入。

### []( "4.1.8 ByteOrder 相关的方法")4.1.8 ByteOrder 相关的方法

/#order()
方法，获得字节序。由子类实现，因为 AbstractByteBuf 的内存类型，不确定是 Heap 还是 Direct 。

/#order(ByteOrder endianness)
方法，设置字节序。代码如下：

```
@Override
public ByteBuf order(ByteOrder endianness){
if (endianness == null) {
throw new NullPointerException("endianness");
}
// 未改变，直接返回
if (endianness == order()) {
return this;
}
// 创建 SwappedByteBuf 对象
return newSwappedByteBuf();
}
//*/*
/* Creates a new {@link SwappedByteBuf} for this {@link ByteBuf} instance.
/*/
protected SwappedByteBuf newSwappedByteBuf(){
return new SwappedByteBuf(this);
}
```

- 如果字节序未修改，直接返回该 ByteBuf 对象。
- 如果字节序有修改，调用

/#newSwappedByteBuf()
方法，TODO SwappedByteBuf

### []( "4.1.9 未实现方法")4.1.9 未实现方法

和 [「2.1.1 基础信息」]() 相关的方法，有三个未实现，如下：

```
public abstract ByteBufAllocator alloc(); // 分配器，用于创建 ByteBuf 对象。
public abstract ByteBuf unwrap(); // 获得被包装( wrap )的 ByteBuf 对象。
public abstract boolean isDirect(); // 是否 NIO Direct Buffer
```

## []( "4.2 读取 / 写入操作")4.2 读取 / 写入操作

我们以 Int 类型为例子，来看看它的读取和写入操作的实现代码。

### []( "4.2.1 getInt")4.2.1 getInt

```
@Override
public int getInt(int index){
// 校验读取是否会超过容量
checkIndex(index, 4);
// 读取 Int 数据
return _getInt(index);
}
```

- 调用

/#checkIndex(index, fieldLength)
方法，校验读取是否会超过**容量**。注意，不是超过

writerIndex
位置。因为，只是读取指定位置开始的 Int 数据，不会改变

readerIndex
。代码如下：

```
protected final void checkIndex(int index, int fieldLength){
// 校验是否可访问
ensureAccessible();
// 校验是否会超过容量
checkIndex0(index, fieldLength);
}
final void checkIndex0(int index, int fieldLength){
if (isOutOfBounds(index, fieldLength, capacity())) {
throw new IndexOutOfBoundsException(String.format(
"index: %d, length: %d (expected: range(0, %d))", index, fieldLength, capacity()));
}
}
// MathUtil.java
//*/*
/* Determine if the requested {@code index} and {@code length} will fit within {@code capacity}.
/* @param index The starting index.
/* @param length The length which will be utilized (starting from {@code index}).
/* @param capacity The capacity that {@code index + length} is allowed to be within.
/* @return {@code true} if the requested {@code index} and {@code length} will fit within {@code capacity}.
/* {@code false} if this would result in an index out of bounds exception.
/*/
public static boolean isOutOfBounds(int index, int length, int capacity){
// 只有有负数，或运算，就会有负数。
// 另外，此处的越界，不仅仅有 capacity - (index + length < 0 ，例如 index < 0 ，也是越界
return (index | length | (index + length) | (capacity - (index + length))) < 0;
}
```

- 调用

/#\_getInt(index)
方法，读取 Int 数据。这是一个**抽象**方法，由子类实现。代码如下：

```
protected abstract int _getInt(int index);
```

关于

/#getIntLE(int index)
/

getUnsignedInt(int index)
/

getUnsignedIntLE(int index)
方法的实现，胖友自己去看。

### []( "4.2.2 readInt")4.2.2 readInt

```
@Override
public int readInt(){
// 校验读取是否会超过可读段
checkReadableBytes0(4);
// 读取 Int 数据
int v = _getInt(readerIndex);
// 修改 readerIndex ，加上已读取字节数
readerIndex += 4;
return v;
}
```

- 调用

/#checkReadableBytes0(fieldLength)
方法，校验读取是否会超过**可读段**。代码如下：

```
private void checkReadableBytes0(int minimumReadableBytes){
// 是否可访问
ensureAccessible();
// 是否超过写索引，即超过可读段
if (readerIndex > writerIndex - minimumReadableBytes) {
throw new IndexOutOfBoundsException(String.format(
"readerIndex(%d) + length(%d) exceeds writerIndex(%d): %s",
readerIndex, minimumReadableBytes, writerIndex, this));
}
}
```

- 调用

/#\_getInt(index)
方法，读取 Int 数据。

- 读取完成，修改

readerIndex
【**重要** 😈】，加上已读取字节数 4 。

关于

/#readIntLE()
/

readUnsignedInt()
/

readUnsignedIntLE()
方法的实现，胖友自己去看。

### []( "4.2.3 setInt")4.2.3 setInt

```
@Override
public ByteBuf setInt(int index, int value){
// 校验写入是否会超过容量
checkIndex(index, 4);
// 设置 Int 数据
_setInt(index, value);
return this;
}
```

- 调用

/#checkIndex(index, fieldLength)
方法，校验写入是否会超过**容量**。

- 调用

/#\_setInt(index,value )
方法，写入 Int 数据。这是一个**抽象**方法，由子类实现。代码如下：

```
protected abstract int _setInt(int index, int value);
```

关于

/#setIntLE(int index, int value)
方法的实现，胖友自己去看。

public abstract ByteBuf writeInt(int value);
public abstract ByteBuf writeIntLE(int value);

### []( "4.2.4 writeInt")4.2.4 writeInt

```
@Override
public ByteBuf writeInt(int value){
// 保证可写入
ensureWritable0(4);
// 写入 Int 数据
_setInt(writerIndex, value);
// 修改 writerIndex ，加上已写入字节数
writerIndex += 4;
return this;
}
```

- 调用

/#ensureWritable0(int minWritableBytes)
方法，保证可写入。

- 调用

/#\_setInt(index, int value)
方法，写入 Int 数据。

- 写入完成，修改

writerIndex
【**重要** 😈】，加上已写入字节数 4 。

### []( "4.2.5 其它方法")4.2.5 其它方法

其它类型的读取和写入操作的实现代码，胖友自己研究落。还是有一些有意思的方法，例如：

- /#writeZero(int length)
  方法。原本以为是循环

length
次写入 0 字节，结果发现会基于

long
=>

int
=>

byte
的顺序，尽可能合并写入。

- /#skipBytes((int length)
  方法

## []( "4.3 查找 / 遍历操作")4.3 查找 / 遍历操作

查找 / 遍历操作相关的方法，实现比较简单。所以，感兴趣的胖友，可以自己去看。

## []( "4.4 释放操作")4.4 释放操作

### []( "4.4.1 discardReadBytes")4.4.1 discardReadBytes

/#discardReadBytes()
方法，代码如下：

```
1: @Override
2: public ByteBuf discardReadBytes(){
3: // 校验可访问
4: ensureAccessible();
5: // 无废弃段，直接返回
6: if (readerIndex == 0) {
7: return this;
8: }
9:
10: // 未读取完
11: if (readerIndex != writerIndex) {
12: // 将可读段复制到 ByteBuf 头
13: setBytes(0, this, readerIndex, writerIndex - readerIndex);
14: // 写索引减小
15: writerIndex -= readerIndex;
16: // 调整标记位
17: adjustMarkers(readerIndex);
18: // 读索引重置为 0
19: readerIndex = 0;
20: // 全部读取完
21: } else {
22: // 调整标记位
23: adjustMarkers(readerIndex);
24: // 读写索引都重置为 0
25: writerIndex = readerIndex = 0;
26: }
27: return this;
28: }
```

- 第 4 行：调用

/#ensureAccessible()
方法，检查是否可访问。

- 第 5 至 8 行：无**废弃段**，直接返回。
- 第 10 至 19 行：未读取完，即还有**可读段**。

- 第 13 行：调用

/#setBytes(int index, ByteBuf src, int srcIndex, int length)
方法，将可读段复制到 ByteBuf 头开始。如下图所示：[![discardReadBytes](http://static2.iocoder.cn/images/Netty/2018_08_01/02.png)](http://static2.iocoder.cn/images/Netty/2018_08_01/02.png 'discardReadBytes')discardReadBytes

- 第 15 行：写索引

writerIndex
减小。

- 第 19 行：调用

/#adjustMarkers(int decrement)
方法，调整标记位。代码如下：

```
protected final void adjustMarkers(int decrement){
int markedReaderIndex = this.markedReaderIndex;
// 读标记位小于减少值(decrement)
if (markedReaderIndex <= decrement) {
// 重置读标记位为 0
this.markedReaderIndex = 0;
// 写标记位小于减少值(decrement)
int markedWriterIndex = this.markedWriterIndex;
if (markedWriterIndex <= decrement) {
// 重置写标记位为 0
this.markedWriterIndex = 0;
// 减小写标记位
} else {
this.markedWriterIndex = markedWriterIndex - decrement;
}
// 减小读写标记位
} else {
this.markedReaderIndex = markedReaderIndex - decrement;
this.markedWriterIndex -= decrement;
}
}
```

- 代码虽然比较多，但是目的很明确，**减小**读写标记位。并且，通过判断，**最多减小至 0** 。
- 第 19 行：**仅**读索引重置为 0 。
- 第 20 至 26 行：全部读取完，即无**可读段**。

- 第 23 行：调用

/#adjustMarkers(int decrement)
方法，调整标记位。

- 第 25 行：读写索引**都**重置为 0 。

### []( "4.4.2 discardSomeReadBytes")4.4.2 discardSomeReadBytes

/#discardSomeReadBytes()
方法，代码如下：

```
@Override
public ByteBuf discardSomeReadBytes(){
// 校验可访问
ensureAccessible();
// 无废弃段，直接返回
if (readerIndex == 0) {
return this;
}
// 全部读取完
if (readerIndex == writerIndex) {
// 调整标记位
adjustMarkers(readerIndex);
// 读写索引都重置为 0
writerIndex = readerIndex = 0;
return this;
}
// 读取超过容量的一半，进行释放
if (readerIndex >= capacity() >>> 1) {
// 将可读段复制到 ByteBuf 头
setBytes(0, this, readerIndex, writerIndex - readerIndex);
// 写索引减小
writerIndex -= readerIndex;
// 调整标记位
adjustMarkers(readerIndex);
// 读索引重置为 0
readerIndex = 0;
}
return this;
}
```

整体代码和

/#discardReadBytes()
方法是**一致的**。差别在于，

readerIndex >= capacity() >>> 1
，读取超过容量的**一半**时，进行释放。也就是说，在空间和时间之间，做了一个平衡。

😈 后续，我们来看看，Netty 具体在什么时候，调用

/#discardSomeReadBytes()
和

/#discardReadBytes()
方法。

### []( "4.4.3 clear")4.4.3 clear

/#clear()
方法，代码如下：

```
@Override
public ByteBuf clear(){
readerIndex = writerIndex = 0;
return this;
}
```

- 读写索引**都**重置为 0 。
- 读写标记位**不会**重置。

## []( "4.5 拷贝操作")4.5 拷贝操作

### []( "4.5.1 copy")4.5.1 copy

/#copy()
方法，拷贝可读部分的字节数组。代码如下：

```
@Override
public ByteBuf copy(){
return copy(readerIndex, readableBytes());
}
```

- 调用

/#readableBytes()
方法，获得可读的字节数。

- 调用

/#copy(int index, int length)
方法，拷贝**指定部分**的字节数组。独立，互相不影响。具体的实现，需要子类中实现，原因是做**深**拷贝，需要根据内存类型是 Heap 和 Direct 会有不同。

### []( "4.5.2 slice")4.5.2 slice

/#slice()
方法，拷贝可读部分的字节数组。代码如下：

```
@Override
public ByteBuf slice(){
return slice(readerIndex, readableBytes());
}
```

- 调用

/#readableBytes()
方法，获得可读的字节数。

- 调用

/#slice(int index, int length)
方法，拷贝**指定部分**的字节数组。共享，互相影响。代码如下：

```
@Override
public ByteBuf slice(int index, int length){
// 校验可访问
ensureAccessible();
// 创建 UnpooledSlicedByteBuf 对象
return new UnpooledSlicedByteBuf(this, index, length);
}
```

- 返回的是创建的 UnpooledSlicedByteBuf 对象。在它内部，会调用当前 ByteBuf 对象，所以这也是为什么说是**共享**的。或者说，我们可以认为这是一个**浅**拷贝。

/#retainedSlice()
方法，在

/#slice()
方法的基础上，引用计数加 1 。代码如下：

```
@Override
public ByteBuf retainedSlice(int index, int length){
return slice(index, length).retain();
}
```

- 调用

/#slice(int index, int length)
方法，拷贝**指定部分**的字节数组。也就说，返回 UnpooledSlicedByteBuf 对象。

- 调用

UnpooledSlicedByteBuf/#retain()
方法，，引用计数加 1 。本文暂时不解析，我们会在 TODO 1011 。

### []( "4.5.3 duplicate")4.5.3 duplicate

/#duplicate()
方法，拷贝**整个**的字节数组。代码如下：

```
@Override
public ByteBuf duplicate(){
// 校验是否可访问
ensureAccessible();
return new UnpooledDuplicatedByteBuf(this);
}
```

- 创建的 UnpooledDuplicatedByteBuf 对象。在它内部，会调用当前 ByteBuf 对象，所以这也是为什么说是**共享**的。或者说，我们可以认为这是一个**浅**拷贝。
- 它和

/#slice()
方法的差别在于，前者是**整个**，后者是**可写段**。

/#retainedDuplicate()
方法，在

/#duplicate()
方法的基础上，引用计数加 1 。代码如下：

```
@Override
public ByteBuf retainedDuplicate(){
return duplicate().retain();
}
```

- 调用

/#duplicate()
方法，拷贝**整个**的字节数组。也就说，返回 UnpooledDuplicatedByteBuf 对象。

- 调用

UnpooledDuplicatedByteBuf/#retain()
方法，，引用计数加 1 。本文暂时不解析，我们会在 TODO 1011 。

## []( "4.6 转换 NIO ByteBuffer 操作")4.6 转换 NIO ByteBuffer 操作

### []( "4.6.1 nioBuffer")4.6.1 nioBuffer

/#nioBuffer()
方法，代码如下：

```
@Override
public ByteBuffer nioBuffer(){
return nioBuffer(readerIndex, readableBytes());
}
```

- 在方法内部，会调用

/#nioBuffer(int index, int length)
方法。而该方法，由具体的子类实现。
FROM [《深入研究 Netty 框架之 ByteBuf 功能原理及源码分析》](https://my.oschina.net/7001/blog/742236)

将当前 ByteBuf 的可读缓冲区(

readerIndex
到

writerIndex
之间的内容) 转换为 ByteBuffer 对象，两者共享共享缓冲区的内容。对 ByteBuffer 的读写操作不会影响 ByteBuf 的读写索引。

注意：ByteBuffer 无法感知 ByteBuf 的动态扩展操作。ByteBuffer 的长度为

readableBytes()
。

### []( "4.6.2 nioBuffers")4.6.2 nioBuffers

/#nioBuffers()
方法，代码如下：

```
@Override
public ByteBuffer[] nioBuffers() {
return nioBuffers(readerIndex, readableBytes());
}
```

- 在方法内部，会调用

/#nioBuffers(int index, int length)
方法。而该方法，由具体的子类实现。

- 😈 为什么会产生数组的情况呢？例如 CompositeByteBuf 。当然，后续文章，我们也会具体分享。

## []( "4.7 Heap 相关方法")4.7 Heap 相关方法

Heap 相关方法，在子类中实现。详细解析，见 [《精尽 Netty 源码解析 —— Buffer 之 ByteBuf（二）核心子类》](http://svip.iocoder.cn/Netty/ByteBuf-1-2-ByteBuf-core-impl)

## []( "4.8 Unsafe 相关方法")4.8 Unsafe 相关方法

Unsafe，在子类中实现。详细解析，见 [《精尽 Netty 源码解析 —— Buffer 之 ByteBuf（二）核心子类》](http://svip.iocoder.cn/Netty/ByteBuf-1-2-ByteBuf-core-impl)

## []( "4.9 Object 相关")4.9 Object 相关

Object 相关的方法，主要调用

io.netty.buffer.ByteBufUtil
进行实现。而 ByteUtil 是一个非常有用的工具类，它提供了一系列静态方法，用于操作 ByteBuf 对象：[![ByteUtil](http://static2.iocoder.cn/images/Netty/2018_08_01/06.png)](http://static2.iocoder.cn/images/Netty/2018_08_01/06.png 'ByteUtil')ByteUtil

😈 因为 Object 相关的方法，实现比较简单。所以，感兴趣的胖友，可以自己去看。

## []( "4.10 引用计数相关")4.10 引用计数相关

本文暂时不解析，我们会在 TODO 1011 。

# []( "5. EmptyByteBuf")5. EmptyByteBuf

io.netty.buffer.EmptyByteBuf
，继承 ByteBuf 抽象类，用于构建空 ByteBuf 对象，

capacity
和

maxCapacity
均为 0 。

😈 代码实现超级简单，感兴趣的胖友，可以自己去看。

# []( "6. WrappedByteBuf")6. WrappedByteBuf

io.netty.buffer.WrappedByteBuf
，继承 ByteBuf 抽象类，用于装饰 ByteBuf 对象。构造方法如下：

```
//*/*
/* 被装饰的 ByteBuf 对象
/*/
protected final ByteBuf buf;
protected WrappedByteBuf(ByteBuf buf){
if (buf == null) {
throw new NullPointerException("buf");
}
this.buf = buf;
}
```

- buf
  属性，被装饰的 ByteBuf 对象。
- 每个实现方法，是对

buf
的对应方法的调用。例如：

```
@Override
public final int capacity(){
return buf.capacity();
}
@Override
public ByteBuf capacity(int newCapacity){
buf.capacity(newCapacity);
return this;
}
```

# []( "7. SwappedByteBuf")7. SwappedByteBuf

io.netty.buffer.SwappedByteBuf
，继承 ByteBuf 抽象类，用于构建具有切换**字节序**功能的 ByteBuf 对象。构造方法如下：

```
//*/*
/* 原 ByteBuf 对象
/*/
private final ByteBuf buf;
//*/*
/* 字节序
/*/
private final ByteOrder order;
public SwappedByteBuf(ByteBuf buf){
if (buf == null) {
throw new NullPointerException("buf");
}
this.buf = buf;
// 初始化 order 属性
if (buf.order() == ByteOrder.BIG_ENDIAN) {
order = ByteOrder.LITTLE_ENDIAN;
} else {
order = ByteOrder.BIG_ENDIAN;
}
}
```

- buf
  属性，原 ByteBuf 对象。
- order
  属性，字节数。
- 实际上，SwappedByteBuf 可以看成一个特殊的 WrappedByteBuf 实现，所以它除了读写操作外的方法，都是对

buf
的对应方法的调用。

- /#capacity()
  方法，代码如下：

```
@Override
public int capacity(){
return buf.capacity();
}
```

- 直接调用

buf
的对应方法。

- /#setInt(int index, int value)
  方法，代码如下：

```
@Override
public ByteBuf setInt(int index, int value){
buf.setInt(index, ByteBufUtil.swapInt(value));
return this;
}
// ByteBufUtil.java
//*/*
/* Toggles the endianness of the specified 32-bit integer.
/*/
public static int swapInt(int value){
return Integer.reverseBytes(value);
}
```

- 先调用

ByteBufUtil/#swapInt(int value)
方法，将

value
的值，转换成相反字节序的 Int 值。

- 后调用

buf
的对应方法。

通过 SwappedByteBuf 类，我们可以很方便的修改原 ByteBuf 对象的字节序，并且无需进行内存复制。但是反过来，一定要注意，这两者是**共享**的。

# []( "8. ReplayingDecoderByteBuf")8. ReplayingDecoderByteBuf

io.netty.handler.codec.ReplayingDecoderByteBuf
，继承 ByteBuf 抽象类，用于构建在 IO 阻塞条件下实现无阻塞解码的特殊 ByteBuf 对 象。当要读取的数据还未接收完全时，抛出异常，交由 ReplayingDecoder 处理。

细心的胖友，会看到 ReplayingDecoderByteBuf 是在

codec
模块，配合 ReplayingDecoder 使用。所以，本文暂时不会分享它，而是在 [《TODO 2000 ReplayingDecoderByteBuf》]() 中，详细解析。

# []( "666. 彩蛋")666. 彩蛋

每逢开篇，内容就特别啰嗦，哈哈哈哈。

推荐阅读如下文章：

- AbeJeffrey [《深入研究 Netty 框架之 ByteBuf 功能原理及源码分析》](https://my.oschina.net/7001/blog/742236)
- [《Netty 学习笔记 —— ByteBuf 继承结构》](https://skyao.gitbooks.io/learning-netty/content/buffer/inheritance.html)
