# 精尽 Netty 源码解析 —— Codec 之 ByteToMessageDecoder（一）Cumulator

# []( "1. 概述")1. 概述

在 [《精尽 Netty 源码解析 —— ChannelHandler（一）之简介》](http://svip.iocoder.cn/Netty/ChannelHandler-1-intro) 中，我们看了 ChannelHandler 的核心类图，如下：[![核心类图](http://static2.iocoder.cn/images/Netty/2018_10_01/01.png)](http://static2.iocoder.cn/images/Netty/2018_10_01/01.png '核心类图')核心类图

- **绿框**部分，我们可以看到，Netty 基于 ChannelHandler 实现了读写的数据( 消息 )的编解码。
  Codec( 编解码 ) = Encode( 编码 ) + Decode( 解码 )。
- 图中有五个和 Codec 相关的类，整理如下：

- 😈 ，实际应该是六个，漏画了 MessageToMessageDecoder 类。
- ByteToMessageCodec ，ByteToMessageDecoder + MessageByteEncoder 的**组合**。

- ByteToMessageDecoder ，将字节**解码**成消息。
- MessageByteEncoder ，将消息**编码**成字节。
- MessageToMessageCodec ，MessageToMessageDecoder + MessageToMessageEncoder 的**组合**。

- MessageToMessageDecoder ，将消息**解码**成另一种消息。
- MessageToMessageEncoder ，将消息**编码**成另一种消息。

而本文，我们来分享 ByteToMessageDecoder 部分的内容。

# []( "2. ByteToMessageDecoder 核心类图")2. ByteToMessageDecoder 核心类图

[![核心类图](http://static2.iocoder.cn/images/Netty/2018_12_01/01.png)](http://static2.iocoder.cn/images/Netty/2018_12_01/01.png '核心类图')核心类图

ByteToMessageDecoder 本身是个**抽象**类，其下有多个子类，笔者简单整理成三类，可能不全哈：

- **绿框**部分 FrameDecoder ：消息帧( Frame )解码器。也就是说该类解码器，用于处理 TCP 的**粘包**现象，将网络发送的字节流解码为具有确定含义的消息帧。之后的解码器再将消息帧解码为实际的 POJO 对象。 如下图所示：[decode](http://static2.iocoder.cn/images/Netty/2018_12_01/02.png)
- **黄框**部分，将字节流使用**指定序列化方式**反序列化成**消息**，例如：XML、JSON 等等。

- 对于该类解码器，不处理 TCP 的**粘包**现象，所以需要搭配 FrameDecoder 一起使用。
- **蓝框**部分，将字节流**解压**，主要涉及相关压缩算法，例如：GZip、BZip 等等。

- 对于该类解码器，不处理 TCP 的**粘包**现象，所以需要搭配 FrameDecoder 一起使用。

# []( "3. 为什么要粘包拆包")3. 为什么要粘包拆包

😈 因为有些朋友不了解粘包和拆包的概念和原理，这里引用笔者的基友【闪电侠】在 [《netty 源码分析之拆包器的奥秘》](https://www.jianshu.com/p/dc26e944da95) 对这块的描述。

## []( "3.1 为什么要粘包")3.1 为什么要粘包

首先你得了解一下 TCP/IP 协议，在用户数据量非常小的情况下，极端情况下，一个字节，该 TCP 数据包的有效载荷非常低，传递 100 字节的数据，需要 100 次 TCP 传送， 100 次 ACK，在应用及时性要求不高的情况下，将这 100 个有效数据拼接成一个数据包，那会缩短到一个 TCP 数据包，以及一个 ack ，有效载荷提高了，带宽也节省了。

非极端情况，有可能**两个**数据包拼接成一个数据包，也有可能**一个半**的数据包拼接成一个数据包，也有可能**两个半**的数据包拼接成一个数据包。

## []( "3.2 为什么要拆包")3.2 为什么要拆包

拆包和粘包是相对的，一端粘了包，另外一端就需要将粘过的包拆开。举个栗子，发送端将三个数据包粘成两个 TCP 数据包发送到接收端，接收端就需要根据应用协议将两个数据包重新组装成三个数据包。

还有一种情况就是用户数据包超过了 mss(最大报文长度)，那么这个数据包在发送的时候必须拆分成几个数据包，接收端收到之后需要将这些数据包粘合起来之后，再拆开。

## []( "3.3 拆包的原理")3.3 拆包的原理

数据，每次读取完都需要判断是否是一个完整的数据包：

1. 如果当前读取的数据不足以拼接成一个完整的业务数据包，那就保留该数据，继续从 tcp 缓冲区中读取，直到得到一个完整的数据包。
1. 如果当前读到的数据加上已经读取的数据足够拼接成一个数据包，那就将已经读取的数据拼接上本次读取的数据，够成一个完整的业务数据包传递到业务逻辑，多余的数据仍然保留，以便和下次读到的数据尝试拼接。

# []( "4. Cumulator")4. Cumulator

Cumulator ，是 ByteToMessageDecoder 的**内部**接口。中文翻译为“累加器”，用于将读取到的数据进行累加到一起，然后再尝试**解码**，从而实现**拆包**。

也是因为 Cumulator 的累加，所以能将不完整的包累加到一起，从而完整。当然，累加的过程，没准又进入了一个不完整的包。所以，这是一个不断累加，不断解码拆包的过程。

Cumulator 接口，代码如下：

```
//*/*
/* ByteBuf 累积器接口
/*
/* Cumulate {@link ByteBuf}s.
/*/
public interface Cumulator{
//*/*
/* Cumulate the given {@link ByteBuf}s and return the {@link ByteBuf} that holds the cumulated bytes.
/* The implementation is responsible to correctly handle the life-cycle of the given {@link ByteBuf}s and so
/* call {@link ByteBuf/#release()} if a {@link ByteBuf} is fully consumed.
/*
/* @param alloc ByteBuf 分配器
/* @param cumulation ByteBuf 当前累积结果
/* @param in 当前读取( 输入 ) ByteBuf
/* @return ByteBuf 新的累积结果
/*/
ByteBuf cumulate(ByteBufAllocator alloc, ByteBuf cumulation, ByteBuf in);
}
```

- 对于

Cumulator/#cumulate(ByteBufAllocator alloc, ByteBuf cumulation, ByteBuf in)
方法，将**原有**

cumulation
累加上**新的**

in
，返回“新”的 ByteBuf 对象。

- 如果

in
过大，超过

cumulation
的空间上限，使用

alloc
进行扩容后再累加。

Cumulator 有两个实现类，代码如下：

```
public static final Cumulator MERGE_CUMULATOR = new Cumulator() {
// ... 省略代码
}
public static final Cumulator COMPOSITE_CUMULATOR = new Cumulator() {
// ... 省略代码
}
```

两者的累加方式不同，我们来详细解析。

## []( "4.1 MERGE_CUMULATOR")4.1 MERGE_CUMULATOR

MERGE_CUMULATOR
思路是，不断使用**老的** ByteBuf 累积。如果空间不够，扩容出**新的** ByteBuf ，再继续进行累积。代码如下：

```
// ByteToMessageDecoder.java
//*/*
/* Cumulate {@link ByteBuf}s by merge them into one {@link ByteBuf}'s, using memory copies.
/*/
1: public static final Cumulator MERGE_CUMULATOR = new Cumulator() {
2:
3: @Override
4: public ByteBuf cumulate(ByteBufAllocator alloc, ByteBuf cumulation, ByteBuf in){
5: final ByteBuf buffer;
6: if (cumulation.writerIndex() > cumulation.maxCapacity() - in.readableBytes() // 超过空间大小，需要扩容
7: || cumulation.refCnt() > 1 // 引用大于 1 ，说明用户使用了 slice().retain() 或 duplicate().retain() 使refCnt增加并且大于 1 ，
8: // 此时扩容返回一个新的累积区ByteBuf，方便用户对老的累积区ByteBuf进行后续处理。
9: || cumulation.isReadOnly()) { // 只读，不可累加，所以需要改成可写
10: // Expand cumulation (by replace it) when either there is not more room in the buffer
11: // or if the refCnt is greater then 1 which may happen when the user use slice().retain() or
12: // duplicate().retain() or if its read-only.
13: //
14: // See:
15: // - https://github.com/netty/netty/issues/2327
16: // - https://github.com/netty/netty/issues/1764
17: // 扩容，返回新的 buffer
18: buffer = expandCumulation(alloc, cumulation, in.readableBytes());
19: } else {
20: // 使用老的 buffer
21: buffer = cumulation;
22: }
23: // 写入 in 到 buffer 中
24: buffer.writeBytes(in);
25: // 释放输入 in
26: in.release();
27: // 返回 buffer
28: return buffer;
29: }
30:
31: };
```

- 获取

buffer
对象。

- 第 6 至 9 行：如下三个条件，满足任意，需要进行扩容。

- ① 第 6 行：

cumulation.writerIndex() > cumulation.maxCapacity() - in.readableBytes()
，超过空间大小，需要扩容。

- 这个比较好理解。
- ② 第 7 行：

cumulation.refCnt() > 1
，引用大于 1 ，说明用户使用了

ByteBuf/#slice()/#retain()
或

ByteBuf/#duplicate()/#retain()
方法，使

refCnt
增加并且大于 1 。

- 关于这块，在【第 11 行】的英文注释，也相应的提到。
- ③ 第 9 行：只读，不可累加，所以需要改成可写。

- 这个比较好理解。
- 【需要扩容】第 18 行：调用

ByteToMessageDecoder/#expandCumulation(ByteBufAllocator alloc, ByteBuf cumulation, int readable)
**静态**方法，扩容，并返回新的，并赋值给

buffer
。代码如下：

```
static ByteBuf expandCumulation(ByteBufAllocator alloc, ByteBuf cumulation, int readable){
// 记录老的 ByteBuf 对象
ByteBuf oldCumulation = cumulation;
// 分配新的 ByteBuf 对象
cumulation = alloc.buffer(oldCumulation.readableBytes() + readable);
// 将老的数据，写入到新的 ByteBuf 对象
cumulation.writeBytes(oldCumulation);
// 释放老的 ByteBuf 对象
oldCumulation.release();
// 返回新的 ByteBuf 对象
return cumulation;
}
```

- 标准的扩容，并复制老数据的过程。胖友自己看下注释噢。
- 【无需扩容】第 21 行：

buffer
直接使用的

cumulation
对象。

- 第 24 行：写入

in
到

buffer
中，进行累积。

- 第 26 行：释放

in
。

- 第 28 行：返回

buffer
。

## []( "4.2 COMPOSITE_CUMULATOR")4.2 COMPOSITE_CUMULATOR

COMPOSITE_CUMULATOR
思路是，使用 CompositeByteBuf ，组合新输入的 ByteBuf 对象，从而避免内存拷贝。代码如下：

```
// ByteToMessageDecoder.java
//*/*
/* Cumulate {@link ByteBuf}s by add them to a {@link CompositeByteBuf} and so do no memory copy whenever possible.
/* Be aware that {@link CompositeByteBuf} use a more complex indexing implementation so depending on your use-case
/* and the decoder implementation this may be slower then just use the {@link /#MERGE_CUMULATOR}.
/*
/* 相比 MERGE_CUMULATOR 来说：
/*
/* 好处是，内存零拷贝
/* 坏处是，因为维护复杂索引，所以某些使用场景下，慢于 MERGE_CUMULATOR
/*/
1: public static final Cumulator COMPOSITE_CUMULATOR = new Cumulator() {
2:
3: @Override
4: public ByteBuf cumulate(ByteBufAllocator alloc, ByteBuf cumulation, ByteBuf in){
5: ByteBuf buffer;
6: // 和 MERGE_CUMULATOR 的情况类似
7: if (cumulation.refCnt() > 1) {
8: // Expand cumulation (by replace it) when the refCnt is greater then 1 which may happen when the user
9: // use slice().retain() or duplicate().retain().
10: //
11: // See:
12: // - https://github.com/netty/netty/issues/2327
13: // - https://github.com/netty/netty/issues/1764
14: buffer = expandCumulation(alloc, cumulation, in.readableBytes());
15: buffer.writeBytes(in);
16: in.release();
17: } else {
18: CompositeByteBuf composite;
19: // 原来是 CompositeByteBuf 类型，直接使用
20: if (cumulation instanceof CompositeByteBuf) {
21: composite = (CompositeByteBuf) cumulation;
22: // 原来不是 CompositeByteBuf 类型，创建，并添加到其中
23: } else {
24: composite = alloc.compositeBuffer(Integer.MAX_VALUE);
25: composite.addComponent(true, cumulation);
26: }
27: // 添加 in 到 composite 中
28: composite.addComponent(true, in);
29: // 赋值给 buffer
30: buffer = composite;
31: }
32: // 返回 buffer
33: return buffer;
34: }
35:
36: };
```

- 第 7 至 16 行：

cumulation.refCnt() > 1
成立，和

MERGE_CUMULATOR
的情况一致，创建一个新的 ByteBuf 对象。这样，再下一次

/#cumulate(...)
时，就会走【第 22 至 26 行】的情况。

- 获得

composite
对象

- 第 19 至 21 行：如果原来**就是** CompositeByteBuf 类型，直接使用。
- 第 22 至 26 行：如果原来**不是** CompositeByteBuf 类型，创建 CompositeByteBuf 对象，并添加

cumulation
到其中。

- 第 28 行：添加

in
到

composite
中，避免内存拷贝。

## []( "4.3 对比")4.3 对比

关于

MERGE_CUMULATOR
和

COMPOSITE_CUMULATOR
的对比，已经写在

COMPOSITE_CUMULATOR
的**头上**的注释。

默认情况下，ByteToMessageDecoder 使用

MERGE_CUMULATOR
作为累加器。

# []( "5. ByteToMessageDecoder")5. ByteToMessageDecoder

io.netty.handler.codec.ByteToMessageDecoder
，继承 ChannelInboundHandlerAdapter 类，**抽象基类**，负责将 Byte 解码成 Message 。
老艿艿：ByteToMessageDecoder 的细节比较多，建议胖友理解如下小节即可：

- [5.1 构造方法]()
- [5.2 channelRead]()
- [5.3 callDecode]()
- [5.4 channelReadComplete]()

## []( "5.1 构造方法")5.1 构造方法

```
private static final byte STATE_INIT = 0;
private static final byte STATE_CALLING_CHILD_DECODE = 1;
private static final byte STATE_HANDLER_REMOVED_PENDING = 2;
//*/*
/* 已累积的 ByteBuf 对象
/*/
ByteBuf cumulation;
//*/*
/* 累计器
/*/
private Cumulator cumulator = MERGE_CUMULATOR;
//*/*
/* 是否每次只解码一条消息，默认为 false 。
/*
/* 部分解码器为 true ，例如：Socks4ClientDecoder
/*
/* @see /#callDecode(ChannelHandlerContext, ByteBuf, List)
/*/
private boolean singleDecode;
//*/*
/* 是否解码到消息。
/*
/* WasNull ，说明就是没解码到消息
/*
/* @see /#channelReadComplete(ChannelHandlerContext)
/*/
private boolean decodeWasNull;
//*/*
/* 是否首次读取，即 {@link /#cumulation} 为空
/*/
private boolean first;
//*/*
/* A bitmask where the bits are defined as
/* <ul>
/* <li>{@link /#STATE_INIT}</li>
/* <li>{@link /#STATE_CALLING_CHILD_DECODE}</li>
/* <li>{@link /#STATE_HANDLER_REMOVED_PENDING}</li>
/* </ul>
/*
/* 解码状态
/*
/* 0 - 初始化
/* 1 - 调用 {@link /#decode(ChannelHandlerContext, ByteBuf, List)} 方法中，正在进行解码
/* 2 - 准备移除
/*/
private byte decodeState = STATE_INIT;
//*/*
/* 读取释放阀值
/*/
private int discardAfterReads = 16;
//*/*
/* 已读取次数。
/*
/* 再读取 {@link /#discardAfterReads} 次数据后，如果无法全部解码完，则进行释放，避免 OOM
/*/
private int numReads;
protected ByteToMessageDecoder(){
// 校验，不可共享
ensureNotSharable();
}
```

属性比较简单，胖友自己看注释。

## []( "5.2 channelRead")5.2 channelRead

/#channelRead(ChannelHandlerContext ctx, Object msg)
方法，读取到新的数据，进行解码。代码如下：

```
1: @Override
2: public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception{
3: if (msg instanceof ByteBuf) {
4: // 创建 CodecOutputList 对象
5: CodecOutputList out = CodecOutputList.newInstance();
6: try {
7: ByteBuf data = (ByteBuf) msg;
8: // 判断是否首次
9: first = cumulation == null;
10: // 若首次，直接使用读取的 data
11: if (first) {
12: cumulation = data;
13: // 若非首次，将读取的 data ，累积到 cumulation 中
14: } else {
15: cumulation = cumulator.cumulate(ctx.alloc(), cumulation, data);
16: }
17: // 执行解码
18: callDecode(ctx, cumulation, out);
19: } catch (DecoderException e) {
20: throw e; // 抛出异常
21: } catch (Exception e) {
22: throw new DecoderException(e); // 封装成 DecoderException 异常，抛出
23: } finally {
24: // cumulation 中所有数据被读取完，直接释放全部
25: if (cumulation != null && !cumulation.isReadable()) {
26: numReads = 0; // 重置 numReads 次数
27: cumulation.release(); // 释放 cumulation
28: cumulation = null; // 置空 cumulation
29: // 读取次数到达 discardAfterReads 上限，释放部分的已读
30: } else if (++ numReads >= discardAfterReads) {
31: // We did enough reads already try to discard some bytes so we not risk to see a OOME.
32: // See https://github.com/netty/netty/issues/4275
33: numReads = 0; // 重置 numReads 次数
34: discardSomeReadBytes(); // 释放部分的已读
35: }
36:
37: // 解码消息的数量
38: int size = out.size();
39: // 是否解码到消息
40: decodeWasNull = !out.insertSinceRecycled();
41:
42: // 触发 Channel Read 事件。可能是多条消息
43: fireChannelRead(ctx, out, size);
44:
45: // 回收 CodecOutputList 对象
46: out.recycle();
47: }
48: } else {
49: // 触发 Channel Read 事件到下一个节点
50: ctx.fireChannelRead(msg);
51: }
52: }
```

- 第 48 至 51 行：消息的类型**不是** ByteBuf 类，直接触发 Channel Read 事件到下一个节点。也就说，不进行解码。
- 第 3 行：消息的类型**是** ByteBuf 类，开始解码。
- 第 5 行：创建 CodecOutputList 对象。CodecOutputList 的简化代码如下：

```
//*/*
/* Special {@link AbstractList} implementation which is used within our codec base classes.
/*/
final class CodecOutputList extends AbstractList<Object> implements RandomAccess{
// ... 省略代码
}
```

- 如下内容，引用自 [《自顶向下深入分析 Netty（八）–CodecHandler》](https://www.jianshu.com/p/7c439cc7b01c)
  解码结果列表 CodecOutputList 是 Netty 定制的一个特殊列表，该列表在线程中被缓存，可循环使用来存储解码结果，减少不必要的列表实例创建，从而提升性能。由于解码结果需要频繁存储，普通的 ArrayList 难以满足该需求，故定制化了一个特殊列表，由此可见 Netty 对优化的极致追求。

- 第 7 至 9 行：通过

cumulation
是否为

null
来判断，是否为首次

first
。

- 若**是**首次，直接使用读取的

data
(

ByteBuf data = (ByteBuf) msg
)。

- 若**非**首次，将读取的

data
，累积到

cumulation
中。在 [「4. Cumulator」]() 中，我们已经详细解析。

- 第 18 行：调用

/#callDecode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
方法，执行解码。而解码的结果，会添加到

out
数组中。详细解析，见 [「5.3 callDecode」]() 。

- 第 19 至 22 行：若发生异常，抛出 DecoderException 异常。
- 第 24 至 35 行：根据

cumulation
的情况，释放

cumulation
。

- 第 24 至 28 行：

cumulation
中所有数据被读取完，直接**释放全部**。

- 第 29 至 35 行：读取次数(

numReads
)到达

discardAfterReads
上限，重置计数，并调用

/#discardSomeReadBytes()
方法，释放部分的已读。😈 如果一直不去释放，等到满足【第 24 至 28 行】的条件，很有可能会出现 OOM 的情况。代码如下：

```
protected final void discardSomeReadBytes(){
if (cumulation != null && !first
&& cumulation.refCnt() == 1) { // <1> 如果用户使用了 slice().retain() 和 duplicate().retain() 使 refCnt > 1 ，表明该累积区还在被用户使用，丢弃数据可能导致用户的困惑，所以须确定用户不再使用该累积区的已读数据，此时才丢弃。
// discard some bytes if possible to make more room in the
// buffer but only if the refCnt == 1 as otherwise the user may have
// used slice().retain() or duplicate().retain().
//
// See:
// - https://github.com/netty/netty/issues/2327
// - https://github.com/netty/netty/issues/1764
// <2> 释放部分
cumulation.discardSomeReadBytes();
}
}
```

- <1>
  处，原因见中文注释。
- <2>
  处，释放**部分**已读字节区。注意，是“部分”，而不是“全部”，避免一次性释放全部，时间过长。并且，能够读取到这么“大”，往往字节数容量不小。如果直接释放掉“全部”，那么后续还需要再重复扩容，反倒不好。
- 第 38 行：获得解码消息的数量。

- 第 40 行：是否解码到消息。为什么不直接使用

size
来判断呢？因为如果添加了消息，然后又移除该消息，此时

size
为 0 ，但是

!out.insertSinceRecycled()
为

true
。

- 另外，我们在 [「5.3 callDecode」]() 中，将会看到一个

out
的清理操作，到时会更加明白。

- 第 43 行：调用

/#fireChannelRead(ChannelHandlerContext ctx, List<Object> msgs, int numElements)
**静态**方法，触发 Channel Read 事件。可能是多条消息。代码如下：

```
//*/*
/* Get {@code numElements} out of the {@link List} and forward these through the pipeline.
/*/
static void fireChannelRead(ChannelHandlerContext ctx, List<Object> msgs, int numElements){
if (msgs instanceof CodecOutputList) { // 如果是 CodecOutputList 类型，特殊优化
fireChannelRead(ctx, (CodecOutputList) msgs, numElements);
} else {
for (int i = 0; i < numElements; i++) {
ctx.fireChannelRead(msgs.get(i));
}
}
}
//*/*
/* Get {@code numElements} out of the {@link CodecOutputList} and forward these through the pipeline.
/*/
static void fireChannelRead(ChannelHandlerContext ctx, CodecOutputList msgs, int numElements){
for (int i = 0; i < numElements; i ++) {
ctx.fireChannelRead(msgs.getUnsafe(i)); // getUnsafe 是自定义的方法，减少越界判断，效率更高
}
}
```

- 遍历

msgs
数组，每条消息触发一次 Channel Read 事件。

- 第 46 行：回收 CodecOutputList 对象。

## []( "5.3 callDecode")5.3 callDecode

/#callDecode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
方法，执行解码。而解码的结果，会添加到

out
数组中。代码如下：

```
1: protected void callDecode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out){
2: try {
3: // 循环读取，直到不可读
4: while (in.isReadable()) {
5: // 记录
6: int outSize = out.size();
7: // out 非空，说明上一次解码有解码到消息
8: if (outSize > 0) {
9: // 触发 Channel Read 事件。可能是多条消息
10: fireChannelRead(ctx, out, outSize);
11: // 清空
12: out.clear();
13:
14: // 用户主动删除该 Handler ，继续操作 in 是不安全的
15: // Check if this handler was removed before continuing with decoding.
16: // If it was removed, it is not safe to continue to operate on the buffer.
17: //
18: // See:
19: // - https://github.com/netty/netty/issues/4635
20: if (ctx.isRemoved()) {
21: break;
22: }
23: outSize = 0;
24: }
25:
26: // 记录当前可读字节数
27: int oldInputLength = in.readableBytes();
28:
29: // 执行解码。如果 Handler 准备移除，在解码完成后，进行移除。
30: decodeRemovalReentryProtection(ctx, in, out);
31:
32: // 用户主动删除该 Handler ，继续操作 in 是不安全的
33: // Check if this handler was removed before continuing the loop.
34: // If it was removed, it is not safe to continue to operate on the buffer.
35: //
36: // See https://github.com/netty/netty/issues/1664
37: if (ctx.isRemoved()) {
38: break;
39: }
40:
41: // 整列判断 `out.size() == 0` 比较合适。因为，如果 `outSize > 0` 那段，已经清理了 out 。
42: if (outSize == out.size()) {
43: // 如果未读取任何字节，结束循环
44: if (oldInputLength == in.readableBytes()) {
45: break;
46: // 如果可读字节发生变化，继续读取
47: } else {
48: continue;
49: }
50: }
51:
52: // 如果解码了消息，但是可读字节数未变，抛出 DecoderException 异常。说明，有问题。
53: if (oldInputLength == in.readableBytes()) {
54: throw new DecoderException(StringUtil.simpleClassName(getClass()) + ".decode() did not read anything but decoded a message.");
55: }
56:
57: // 如果开启 singleDecode ，表示只解析一次，结束循环
58: if (isSingleDecode()) {
59: break;
60: }
61: }
62: } catch (DecoderException e) {
63: throw e;
64: } catch (Exception cause) {
65: throw new DecoderException(cause);
66: }
67: }
```

- 第 4 行：循环读取

in
，直到不可读。

- 第 5 行：记录

out
的大小。

- 第 8 行：如果

out
非空，说明上一次解码有解码到消息。

- 第 10 行：调用

/#fireChannelRead(ChannelHandlerContext ctx, List<Object> msgs, int numElements)
**静态**方法，触发 Channel Read 事件。可能是多条消息。😈 关于该方法，上文已经详细解析。

- 第 12 行：清空

out
。所以，有可能会出现

/#channelRead(ChannelHandlerContext ctx, Object msg)
方法的【第 40 行】的情况。

- 第 14 至 22 行：用户主动删除该 Handler ，继续操作

in
是不安全的，所以结束循环。

- 第 23 行：记录

out
的大小为**零**。所以，实际上，

outSize
没有必要记录。因为，一定是为**零**。

- 第 27 行：记录当前可读字节数。
- 第 30 行：调用

/#decodeRemovalReentryProtection(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
方法，执行解码。如果 Handler 准备移除，在解码完成后，进行移除。详细解析，见 [「5.3.1 decodeRemovalReentryProtection」]() 中。

- 第 32 至 39 行：用户主动删除该 Handler ，继续操作

in
是不安全的，所以结束循环。

- 第 42 行：直接判断

out.size() == 0
比较合适。因为【第 8 至 24 行】的代码，能够保证

outSize
等于**零**。

- 第 43 至 45 行：如果**未读取**任何字节，

break
结束循环。

- 第 46 至 49 行：如果可读字节**发生变化**，

continue
重新开始循环，即继续读取。

- 第 52 至 55 行：如果解码了消息，但是可读字节数未变，抛出 DecoderException 异常。说明，有问题。
- 第 57 至 60 行：如果开启

singleDecode
，表示只解析一次，

break
结束循环。

- 第 62 至 66 行：如果发生异常，抛出 DecoderException 异常。

😈 代码有一些长，胖友保持耐心看完哈。其实，蛮简单的。

### []( "5.3.1 decodeRemovalReentryProtection")5.3.1 decodeRemovalReentryProtection

/#decodeRemovalReentryProtection(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
方法，执行解码。如果 Handler 准备移除，在解码完成后，进行移除。代码如下：

```
1: final void decodeRemovalReentryProtection(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception{
2: // 设置状态为 STATE_CALLING_CHILD_DECODE
3: decodeState = STATE_CALLING_CHILD_DECODE;
4: try {
5: // 执行解码
6: decode(ctx, in, out);
7: } finally {
8: // 判断是否准备移除
9: boolean removePending = decodeState == STATE_HANDLER_REMOVED_PENDING;
10: // 设置状态为 STATE_INIT
11: decodeState = STATE_INIT;
12: // 移除当前 Handler
13: if (removePending) {
14: handlerRemoved(ctx);
15: }
16: }
17: }
```

- 第 3 行：设置状态(

decodeState
) 为

STATE_CALLING_CHILD_DECODE
。

- 第 6 行：调用

/#decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
方法，执行解码。代码如下：

```
//*/*
/* Decode the from one {@link ByteBuf} to an other. This method will be called till either the input
/* {@link ByteBuf} has nothing to read when return from this method or till nothing was read from the input
/* {@link ByteBuf}.
/*
/* @param ctx the {@link ChannelHandlerContext} which this {@link ByteToMessageDecoder} belongs to
/* @param in the {@link ByteBuf} from which to read data
/* @param out the {@link List} to which decoded messages should be added
/* @throws Exception is thrown if an error occurs
/*/
protected abstract void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception;
```

- 子类实现该方法，就可以愉快的解码消息了，**并且，也只需要实现该方法**。其它的逻辑，ByteToMessageDecoder 已经全部帮忙实现了。
- 第 9 行：判断是否准备移除。那么什么情况下，会出现

decodeState == STATE_HANDLER_REMOVED_PENDING
成立呢？详细解析，见 [「5.7 handlerRemoved」]() 。

- 第 11 行：设置状态(

decodeState
) 为

STATE_HANDLER_REMOVED_PENDING
。

- 第 12 至 15 行：如果准备移除，则调用

/#handlerRemoved(ChannelHandlerContext ctx)
方法，移除当前 Handler 。详细解析，见 [「5.7 handlerRemoved」]() 。

## []( "5.4 channelReadComplete")5.4 channelReadComplete

/#channelReadComplete(ChannelHandlerContext ctx)
方法，代码如下：

```
1: @Override
2: public void channelReadComplete(ChannelHandlerContext ctx) throws Exception{
3: // 重置 numReads
4: numReads = 0;
5: // 释放部分的已读
6: discardSomeReadBytes();
7: // 未解码到消息，并且未开启自动读取，则再次发起读取，期望读取到更多数据，以便解码到消息
8: if (decodeWasNull) {
9: decodeWasNull = false; // 重置 decodeWasNull
10: if (!ctx.channel().config().isAutoRead()) {
11: ctx.read();
12: }
13: }
14: // 触发 Channel ReadComplete 事件到下一个节点
15: ctx.fireChannelReadComplete();
16: }
```

- 第 4 行：重置

numReads
。

- 第 6 行：调用

/#discardSomeReadBytes()
方法，释放部分的已读。

- 第 7 至 13 行：未解码到消息(

decodeWasNull == true
)，并且未开启自动读取(

ctx.channel().config().isAutoRead() == false
)，则再次发起读取，期望读取到更多数据，以便解码到消息。

- 第 15 行：触发 Channel ReadComplete 事件到下一个节点。

## []( "5.5 channelInactive")5.5 channelInactive

/#channelInactive(ChannelHandlerContext ctx)
方法，通道处于未激活( Inactive )，解码完剩余的消息，并释放相关资源。代码如下：

```
@Override
public void channelInactive(ChannelHandlerContext ctx) throws Exception{
channelInputClosed(ctx, true);
}
```

- 调用

/#channelInputClosed(ChannelHandlerContext ctx, boolean callChannelInactive)
方法，执行 Channel 读取关闭的逻辑。代码如下：

```
1: private void channelInputClosed(ChannelHandlerContext ctx, boolean callChannelInactive) throws Exception{
2: // 创建 CodecOutputList 对象
3: CodecOutputList out = CodecOutputList.newInstance();
4: try {
5: // 当 Channel 读取关闭时，执行解码剩余消息的逻辑
6: channelInputClosed(ctx, out);
7: } catch (DecoderException e) {
8: throw e;
9: } catch (Exception e) {
10: throw new DecoderException(e);
11: } finally {
12: try {
13: // 释放 cumulation
14: if (cumulation != null) {
15: cumulation.release();
16: cumulation = null;
17: }
18: int size = out.size();
19: // 触发 Channel Read 事件到下一个节点。可能是多条消息
20: fireChannelRead(ctx, out, size);
21: // 如果有解码到消息，则触发 Channel ReadComplete 事件到下一个节点。
22: if (size > 0) {
23: // Something was read, call fireChannelReadComplete()
24: ctx.fireChannelReadComplete();
25: }
26: // 如果方法调用来源是 `/#channelInactive(...)` ，则触发 Channel Inactive 事件到下一个节点
27: if (callChannelInactive) {
28: ctx.fireChannelInactive();
29: }
30: } finally {
31: // 回收 CodecOutputList 对象
32: // Recycle in all cases
33: out.recycle();
34: }
35: }
36: }
```

- 第 3 行：创建 CodecOutputList 对象。

- 第 6 行：调用

/#channelInputClosed(ChannelHandlerContext ctx, List<Object> out)
方法，当 Channel 读取关闭时，执行解码剩余消息的逻辑。代码如下：

```
//*/*
/* Called when the input of the channel was closed which may be because it changed to inactive or because of
/* {@link ChannelInputShutdownEvent}.
/*/
void channelInputClosed(ChannelHandlerContext ctx, List<Object> out) throws Exception{
if (cumulation != null) {
// 执行解码
callDecode(ctx, cumulation, out);
// 最后一次，执行解码
decodeLast(ctx, cumulation, out);
} else {
// 最后一次，执行解码
decodeLast(ctx, Unpooled.EMPTY_BUFFER, out);
}
}
//*/*
/* Is called one last time when the {@link ChannelHandlerContext} goes in-active. Which means the
/* {@link /#channelInactive(ChannelHandlerContext)} was triggered.
/*
/* By default this will just call {@link /#decode(ChannelHandlerContext, ByteBuf, List)} but sub-classes may
/* override this for some special cleanup operation.
/*/
protected void decodeLast(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception{
if (in.isReadable()) {
// Only call decode() if there is something left in the buffer to decode.
// See https://github.com/netty/netty/issues/4386
decodeRemovalReentryProtection(ctx, in, out);
}
}
```

- 其中，

/#decodeLast(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
方法，是可以被重写的。例如，HttpObjectDecoder 就重写了该方法。

- 第 7 至 10 行：如果发生异常，就抛出 DecoderException 异常。
- 第 13 至 17 行：释放

cumulation
。

- 第 20 行：调用

/#fireChannelRead(ChannelHandlerContext ctx, List<Object> msgs, int numElements)
**静态**方法，触发 Channel Read 事件。可能是多条消息。

- 第 21 至 25 行：如果有解码到消息(

size > 0
)，则触发 Channel ReadComplete 事件到下一个节点。

- 第 26 至 29 行：如果方法调用来源是

/#channelInactive(...)
，则触发 Channel Inactive 事件到下一个节点。

- 第 30 至 35 行：回收 CodecOutputList 对象。

😈 对于该方法的目的，笔者的理解是，尽可能在解码一次剩余的

cumulation
，在 Channel 变成未激活时。细节好多呀！！！

## []( "5.6 userEventTriggered")5.6 userEventTriggered

/#userEventTriggered(ChannelHandlerContext ctx, Object evt)
方法，处理 ChannelInputShutdownEvent 事件，即 Channel 关闭读取。代码如下：

```
@Override
public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception{
if (evt instanceof ChannelInputShutdownEvent) {
// The decodeLast method is invoked when a channelInactive event is encountered.
// This method is responsible for ending requests in some situations and must be called
// when the input has been shutdown.
channelInputClosed(ctx, false);
}
// 继续传播 evt 到下一个节点
super.userEventTriggered(ctx, evt);
}
```

- 调用

/#channelInputClosed(ChannelHandlerContext ctx, boolean callChannelInactive)
方法，执行 Channel 读取关闭的逻辑。

- 继续传播

evt
到下一个节点。

😈 对于该方法的目的，笔者的理解是，尽可能在解码一次剩余的

cumulation
，在 Channel 关闭读取。细节好多呀！！！

## []( "5.7 handlerRemoved")5.7 handlerRemoved

/#handlerRemoved(ChannelHandlerContext ctx)
方法，代码如下：

```
1: @Override
2: public final void handlerRemoved(ChannelHandlerContext ctx) throws Exception{
3: // 状态处于 STATE_CALLING_CHILD_DECODE 时，标记状态为 STATE_HANDLER_REMOVED_PENDING
4: if (decodeState == STATE_CALLING_CHILD_DECODE) {
5: decodeState = STATE_HANDLER_REMOVED_PENDING;
6: return; // 返回！！！！结合 `/#decodeRemovalReentryProtection(...)` 方法，一起看。
7: }
8: ByteBuf buf = cumulation;
9: if (buf != null) {
10: // 置空 cumulation
11: // Directly set this to null so we are sure we not access it in any other method here anymore.
12: cumulation = null;
13:
14: int readable = buf.readableBytes();
15: // 有可读字节
16: if (readable > 0) {
17: // 读取剩余字节，并释放 buf
18: ByteBuf bytes = buf.readBytes(readable);
19: buf.release();
20: // 触发 Channel Read 到下一个节点
21: ctx.fireChannelRead(bytes);
22: // 无可读字节
23: } else {
24: // 释放 buf
25: buf.release();
26: }
27:
28: // 置空 numReads
29: numReads = 0;
30: // 触发 Channel ReadComplete 到下一个节点
31: ctx.fireChannelReadComplete();
32: }
33: // 执行移除逻辑
34: handlerRemoved0(ctx);
35: }
```

- 第 3 至 7 行：如果状态(

decodeState
)处于

STATE_CALLING_CHILD_DECODE
时，说明正在执行

/#decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
方法中。如果此时，直接往下执行，

cumulation
将被直接释放，而

/#decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
方法可能正在解码中，很大可能性造成影响，导致错误。所以，此处仅仅标记状态(

decodeState
)为

STATE_HANDLER_REMOVED_PENDING
。等到

/#decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
方法执行完成后，在进行移除。胖友，此时可以再跳回 [「5.3.1 decodeRemovalReentryProtection」]() ，进行再次理解。

- 【有可读字节】第 15 至 21 行：读取剩余字节，并释放

buf
。然后，触发 Channel Read 到下一个节点。通过这样的方式，避免

cumulation
中，有字节被“丢失”，即使当前可能无法解码成一个数据包。

- 【无可读字节】第 22 至 26 行：直接释放

buf
。

- 第 29 行：置空

numReads
。

- 第 34 行：调用

/#handlerRemoved0(ChannelHandlerContext ctx)
方法，执行移除逻辑。代码如下：

```
//*/*
/* Gets called after the {@link ByteToMessageDecoder} was removed from the actual context and it doesn't handle
/* events anymore.
/*/
protected void handlerRemoved0(ChannelHandlerContext ctx) throws Exception{ }
```

- 默认情况下，该方法实现为空。目前可重写该方法，实现自定义的资源释放。目前重写该方法的类，例如：Http2ConnectionHandler、SslHandler 等等。

## []( "5.8 internalBuffer")5.8 internalBuffer

/#internalBuffer()
方法，获得 ByteBuf 对象。代码如下：

```
//*/*
/* Returns the internal cumulative buffer of this decoder. You usually
/* do not need to access the internal buffer directly to write a decoder.
/* Use it only when you must use it at your own risk.
/*/
protected ByteBuf internalBuffer(){
if (cumulation != null) {
return cumulation;
} else {
return Unpooled.EMPTY_BUFFER;
}
}
```

## []( "5.9 actualReadableBytes")5.9 actualReadableBytes

/#actualReadableBytes()
方法，获得可读字节数。代码如下：

```
//*/*
/* Returns the actual number of readable bytes in the internal cumulative
/* buffer of this decoder. You usually do not need to rely on this value
/* to write a decoder. Use it only when you must use it at your own risk.
/* This method is a shortcut to {@link /#internalBuffer() internalBuffer().readableBytes()}.
/*/
protected int actualReadableBytes(){
return internalBuffer().readableBytes();
}
```

# []( "666. 彩蛋")666. 彩蛋

细节有点多，可能对如下小节理解不够到位。如有错误，烦请胖友教育。

- [「5.5 channelInactive」]()
- [「5.6 userEventTriggered」]()
- [「5.7 handlerRemoved」]()

本文参考如下文章：

- 简书闪电侠 [《netty 源码分析之拆包器的奥秘》](https://www.jianshu.com/p/dc26e944da95)
- Hypercube [《自顶向下深入分析 Netty（八）–CodecHandler》](https://www.jianshu.com/p/7c439cc7b01c)
