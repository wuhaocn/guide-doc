# 精尽 Netty 源码解析 —— Codec 之 MessageToMessageCodec

# []( "1. 概述")1. 概述

本文，我们来分享 MessageToMessageCodec 部分的内容。

MessageToMessageCodec 负责消息和消息**之间**的编解码，主要由两部分构造：

- MessageToMessageEncoder ，将消息**编码**成另一种消息。
- MessageToMessageDecoder ，将消息**解码**成另一种消息。

所以，我们先来看 MessageToMessageDecoder 的代码实现，再看 MessageToMessageEncoder 的代码实现，最后看 MessageToMessageCodec 的代码实现。
老艿艿：因为笔者平时比较少用这三个类，所以本文会分享的比较简单噢。

# []( "2. MessageToMessageEncoder")2. MessageToMessageEncoder

io.netty.handler.codec.MessageToMessageEncoder
，继承 ChannelOutboundHandlerAdapter 抽象类，将消息**编码**成另一种消息。

## []( "2.1 构造方法")2.1 构造方法

```
//*/*
/* 类型匹配器
/*/
private final TypeParameterMatcher matcher;
protected MessageToMessageEncoder(){
matcher = TypeParameterMatcher.find(this, MessageToMessageEncoder.class, "I");
}
protected MessageToMessageEncoder(Class<? extends I> outboundMessageType){
matcher = TypeParameterMatcher.get(outboundMessageType);
}
```

## []( "2.2 acceptOutboundMessage")2.2 acceptOutboundMessage

```
//*/*
/* Returns {@code true} if the given message should be handled. If {@code false} it will be passed to the next
/* {@link ChannelOutboundHandler} in the {@link ChannelPipeline}.
/*/
public boolean acceptOutboundMessage(Object msg) throws Exception{
return matcher.match(msg);
}
```

## []( "2.3 write")2.3 write

```
@Override
public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception{
CodecOutputList out = null;
try {
// 判断是否为匹配的消息
if (acceptOutboundMessage(msg)) {
// 创建 CodecOutputList 对象
out = CodecOutputList.newInstance();
// 转化消息类型
@SuppressWarnings("unchecked")
I cast = (I) msg;
try {
// 将消息编码成另外一个消息
encode(ctx, cast, out);
} finally {
// 释放 cast 原消息
ReferenceCountUtil.release(cast);
}
// 如果未编码出消息，抛出异常
if (out.isEmpty()) {
// 回收 CodecOutputList 对象
out.recycle();
out = null;
// 抛出异常
throw new EncoderException(StringUtil.simpleClassName(this) + " must produce at least one message.");
}
} else {
// 直接下一个节点
ctx.write(msg, promise);
}
} catch (EncoderException e) {
throw e;
} catch (Throwable t) {
throw new EncoderException(t);
} finally {
if (out != null) {
final int sizeMinusOne = out.size() - 1;
// 只编码出一条消息
if (sizeMinusOne == 0) {
// 直接写入新消息到下一个节点
ctx.write(out.get(0), promise);
// 编码出多条消息
} else if (sizeMinusOne > 0) {
// 第 [0, n-1) 条消息，写入下一个节点，使用 voidPromise ，即不需要回调
// Check if we can use a voidPromise for our extra writes to reduce GC-Pressure
// See https://github.com/netty/netty/issues/2525
ChannelPromise voidPromise = ctx.voidPromise();
boolean isVoidPromise = promise == voidPromise;
for (int i = 0; i < sizeMinusOne; i ++) {
ChannelPromise p;
if (isVoidPromise) {
p = voidPromise;
} else {
p = ctx.newPromise();
}
ctx.write(out.getUnsafe(i), p);
}
// 第 n-1 条消息，写入下一个节点，使用 promise ，即需要回调
ctx.write(out.getUnsafe(sizeMinusOne), promise);
}
// 回收 CodecOutputList 对象
out.recycle();
}
}
}
```

- 代码比较简单，胖友自己看注释。

## []( "2.4 encode")2.4 encode

```
//*/*
/* Encode from one message to an other. This method will be called for each written message that can be handled
/* by this encoder.
/*
/* @param ctx the {@link ChannelHandlerContext} which this {@link MessageToMessageEncoder} belongs to
/* @param msg the message to encode to an other one
/* @param out the {@link List} into which the encoded msg should be added
/* needs to do some kind of aggregation
/* @throws Exception is thrown if an error occurs
/*/
protected abstract void encode(ChannelHandlerContext ctx, I msg, List<Object> out) throws Exception;
```

## []( "2.5 子类")2.5 子类

MessageToMessageEncoder 子类比较多，感兴趣的胖友，可以看看负责实现 FrameEncoder 的两个类：

- io.netty.handler.codec.string.LineEncoder
  ，基于**指定行分隔符**的 FrameEncoder 实现类。

- 代码比较简单，胖友可以直接撸。
- io.netty.handler.codec.LengthFieldPrepender
  ，基于基于**消息头指定消息长度**的 FrameEncoder 实现类。

- 相对复杂一些，胖友可以结合 [《Netty 源码分析-7-MessageToByteEncoder》](https://liuzhengyang.github.io/2018/08/03/netty-7-messagetobyteencoder/) 一起撸。

# []( "3. MessageToMessageDecoder")3. MessageToMessageDecoder

io.netty.handler.codec.MessageToMessageDecoder
，继承 ChannelInboundHandlerAdapter 类，将消息**解码**成另一种消息。

## []( "3.1 构造方法")3.1 构造方法

```
//*/*
/* 类型匹配器
/*/
private final TypeParameterMatcher matcher;
protected MessageToMessageDecoder(){
matcher = TypeParameterMatcher.find(this, MessageToMessageDecoder.class, "I");
}
protected MessageToMessageDecoder(Class<? extends I> inboundMessageType){
matcher = TypeParameterMatcher.get(inboundMessageType);
}
```

## []( "3.2 acceptInboundMessage")3.2 acceptInboundMessage

```
public boolean acceptInboundMessage(Object msg) throws Exception{
return matcher.match(msg);
}
```

## []( "3.3 channelRead")3.3 channelRead

```
@Override
public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception{
// 创建 CodecOutputList 对象
CodecOutputList out = CodecOutputList.newInstance();
try {
// 判断是否为匹配的消息
if (acceptInboundMessage(msg)) {
// 转化消息类型
@SuppressWarnings("unchecked")
I cast = (I) msg;
try {
// 将消息解码成另外一个消息
decode(ctx, cast, out);
} finally {
// 释放 cast 原消息
ReferenceCountUtil.release(cast);
}
} else {
// 不匹配，添加到 out
out.add(msg);
}
} catch (DecoderException e) {
throw e;
} catch (Exception e) {
throw new DecoderException(e);
} finally {
// 遍历 out ，触发 Channel Read 事件到 pipeline 中
int size = out.size();
for (int i = 0; i < size; i ++) {
ctx.fireChannelRead(out.getUnsafe(i));
}
// 回收 CodecOutputList 对象
out.recycle();
}
}
```

## []( "3.4 decode")3.4 decode

```
//*/*
/* Decode from one message to an other. This method will be called for each written message that can be handled
/* by this encoder.
/*
/* @param ctx the {@link ChannelHandlerContext} which this {@link MessageToMessageDecoder} belongs to
/* @param msg the message to decode to an other one
/* @param out the {@link List} to which decoded messages should be added
/* @throws Exception is thrown if an error occurs
/*/
protected abstract void decode(ChannelHandlerContext ctx, I msg, List<Object> out) throws Exception;
```

# []( "4. MessageToMessageCodec")4. MessageToMessageCodec

io.netty.handler.codec.MessageToMessageCodec
，继承 ChannelDuplexHandler 类，通过**组合** MessageToMessageEncoder 和 MessageToMessageDecoder 的功能，从而实现编解码的 Codec **抽象类**。
老艿艿：从实现的方式上，和 ByteToMessageCodec 非常相似。

## []( "4.1 构造方法")4.1 构造方法

```
public abstract class MessageToMessageCodec<INBOUND_IN, OUTBOUND_IN> extends ChannelDuplexHandler{
//*/*
/* Encoder 对象
/*/
private final MessageToMessageEncoder<Object> encoder = new MessageToMessageEncoder<Object>() {
@Override
public boolean acceptOutboundMessage(Object msg) throws Exception{
return MessageToMessageCodec.this.acceptOutboundMessage(msg);
}
@Override
@SuppressWarnings("unchecked")
protected void encode(ChannelHandlerContext ctx, Object msg, List<Object> out) throws Exception{
MessageToMessageCodec.this.encode(ctx, (OUTBOUND_IN) msg, out);
}
};
//*/*
/* Decoder 对象
/*/
private final MessageToMessageDecoder<Object> decoder = new MessageToMessageDecoder<Object>() {
@Override
public boolean acceptInboundMessage(Object msg) throws Exception{
return MessageToMessageCodec.this.acceptInboundMessage(msg);
}
@Override
@SuppressWarnings("unchecked")
protected void decode(ChannelHandlerContext ctx, Object msg, List<Object> out) throws Exception{
MessageToMessageCodec.this.decode(ctx, (INBOUND_IN) msg, out);
}
};
//*/*
/* Decoder 的类型匹配器
/*/
private final TypeParameterMatcher inboundMsgMatcher;
//*/*
/* Encoder 的类型匹配器
/*/
private final TypeParameterMatcher outboundMsgMatcher;
protected MessageToMessageCodec(){
inboundMsgMatcher = TypeParameterMatcher.find(this, MessageToMessageCodec.class, "INBOUND_IN");
outboundMsgMatcher = TypeParameterMatcher.find(this, MessageToMessageCodec.class, "OUTBOUND_IN");
}
protected MessageToMessageCodec(Class<? extends INBOUND_IN> inboundMessageType, Class<? extends OUTBOUND_IN> outboundMessageType){
inboundMsgMatcher = TypeParameterMatcher.get(inboundMessageType);
outboundMsgMatcher = TypeParameterMatcher.get(outboundMessageType);
}
// ... 省略非构造方法相关
}
```

## []( "4.2 MessageToMessageEncoder 相关方法")4.2 MessageToMessageEncoder 相关方法

```
@Override
public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception{
encoder.write(ctx, msg, promise);
}
//*/*
/* Returns {@code true} if and only if the specified message can be decoded by this codec.
/*
/* @param msg the message
/*/
public boolean acceptInboundMessage(Object msg) throws Exception{
return inboundMsgMatcher.match(msg);
}
//*/*
/* @see MessageToMessageEncoder/#encode(ChannelHandlerContext, Object, List)
/*/
protected abstract void encode(ChannelHandlerContext ctx, OUTBOUND_IN msg, List<Object> out) throws Exception;
```

## []( "4.3 MessageToMessageDecoder 相关方法")4.3 MessageToMessageDecoder 相关方法

```
@Override
public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception{
decoder.channelRead(ctx, msg);
}
//*/*
/* Returns {@code true} if and only if the specified message can be encoded by this codec.
/*
/* @param msg the message
/*/
public boolean acceptOutboundMessage(Object msg) throws Exception{
return outboundMsgMatcher.match(msg);
}
//*/*
/* @see MessageToMessageDecoder/#decode(ChannelHandlerContext, Object, List)
/*/
protected abstract void decode(ChannelHandlerContext ctx, INBOUND_IN msg, List<Object> out) throws Exception;
```

# []( "666. 彩蛋")666. 彩蛋

还是一篇小水文。嘿嘿。

推荐阅读文章：

- wade&luffy [《ChannelHandler》](https://www.cnblogs.com/wade-luffy/p/6222960.html)
- 堆码时刻 [《Netty In Action 中文版 - 第八章：附带的 ChannelHandler 和 Codec》](https://blog.csdn.net/abc_key/article/details/38061079)
