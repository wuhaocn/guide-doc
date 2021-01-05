### 简介

- 优点
  - 成熟的编解码框架，提供简单易用的编解码框架
  - 基于 reactor 模型，提高应用的并发性
  - 优化 java-nio 实用性
- 核心组件
  - Bootstrap & ServerBootstrap
  - Channel
  - ChannelFuture
  - EventLoop & EventLoopGroup
  - ChannelHandler
  - ChannelPipeline
- 编解码
  - codec-dns
  - codec-haproxy
  - codec-http
  - codec-http2
  - codec-memcache
  - codec-mqtt
  - codec-redis
  - codec-smtp
  - codec-socks
  - codec-stomp
  - codec-xml
  - 解码器
    - LineBasedFrameDecoder
      - 换行符，将回车换行符作为消息结束符，例如：FTP 协议，这种方式在文本协议中应用比较广泛。
    - DelimiterBaseFrameDecoder
      - 分隔符，将特殊的分隔符作为消息的结束标志，回车换行符就是一种特殊的结束分隔符。
    - FixdLengthFrameDecoder
      - 定长，消息长度固定，累计读取的长度总和为约定的定长长度后，就认为读到了一个完整的消息；将计数器置位，重新开始读取下一条报文。
    - LengthFieldBasedFrameDecoder
      - 定义了一个长度的字段来表示消息的长度，因此能够处理可变长度的消息。将消息分为消息头和消息体，消息头固定位置增加一个表示长度的字段，通过长度字段来获取整包的信息

### 导航

- NIO
  - [NIO 基础(一)之简介](NIO基础%28一%29之简介.md)
  - [NIO 基础(二)之 Channel](NIO基础%28二%29之Channel.md)
  - [NIO 基础(三)之 Buffer](NIO基础%28三%29之Buffer.md)
  - [NIO 基础(四)之 Selector](NIO基础%28四%29之Selector.md)
  - [NIO 基础(五)之示例](NIO基础%28五%29之示例.md)
- Netty 橄榄
  - [Netty 概览](Netty概览.md)
  - [学习导读](学习导读.md)
  - [调试环境搭建](Netty源码环境搭建.md)
  - [Netty 项目结构概述](Netty项目结构概述.md)
  - [Netty 核心组件](Netty核心组件.md)
- Netty-EventLoop
  - [Netty-EventLoop 之 Reactor 模型.tny](Netty-EventLoop之Reactor模型.tny.md)
  - [Netty-EventLoop 之 EventLoopGroup.tny](Netty-EventLoop之EventLoopGroup.tny.md)
  - [Netty-EventLoop 之初始化.tny](Netty-EventLoop之初始化.tny.md)
  - [Netty-EventLoop 之 EventLoop 运行.tny](Netty-EventLoop之EventLoop运行.tny.md)
  - [Netty-EventLoop 之 EventLoop 处理 IO 事件.tny](Netty-EventLoop之EventLoop处理IO事件.tny.md)
  - [Netty-EventLoop 之 EventLoop 处理普通任务.tny](Netty-EventLoop之EventLoop处理普通任务.tny.md)
  - [Netty-EventLoop 之 EventLoop 处理定时任务.tny](Netty-EventLoop之EventLoop处理定时任务.tny.md)
  - [Netty-EventLoop 之 EventLoop 优雅关闭.tny](Netty-EventLoop之EventLoop优雅关闭.tny.md)
- Netty-ChannelPipeline

  - [Netty-ChannelPipeline 之初始化.tny](Netty-ChannelPipeline之初始化.tny.md)
  - [Netty-ChannelPipeline 之添加 ChannelHandler.tny](Netty-ChannelPipeline之添加ChannelHandler.tny.md)
  - [Netty-ChannelPipeline 之移除 ChannelHandler.tny](Netty-ChannelPipeline之移除ChannelHandler.tny.md)
  - [Netty-ChannelPipeline 之 Inbound 事件的传播.tny](Netty-ChannelPipeline之Inbound事件的传播.tny.md)
  - [Netty-ChannelPipeline 之 Outbound 事件的传播.tny](Netty-ChannelPipeline之Outbound事件的传播.tny.md)
  - [Netty-ChannelPipeline 之异常事件的传播.tny](Netty-ChannelPipeline之异常事件的传播.tny.md)

- Netty-Channel
  - [Netty-Channel 之简介.tny](Netty-Channel之简介.tny.md)
  - [Netty-Channel 之 accept 操作.tny](Netty-Channel之accept操作.tny.md)
  - [Netty-Channel 之 read 操作.tny](Netty-Channel之read操作.tny.md)
  - [Netty-Channel 之 write 操作.tny](Netty-Channel之write操作.tny.md)
  - [Netty-Channel 之 flush 操作.tny](Netty-Channel之flush操作.tny.md)
  - [Netty-Channel 之 writeAndFlush 操作.tny](Netty-Channel之writeAndFlush操作.tny.md)
  - [Netty-Channel 之 close 操作.tny](Netty-Channel之close操作.tny.md)
  - [Netty-Channel 之 disconnect 操作.tny](Netty-Channel之disconnect操作.tny.md)
- Netty-Buffer

  - [Netty-Buffer 之 ByteBuf 简介.tny](Netty-Buffer之ByteBuf简介.tny.md)
  - [Netty-Buffer 之 ByteBuf 核心子类.tny](Netty-Buffer之ByteBuf核心子类.tny.md)
  - [Netty-Buffer 之 ByteBuf 内存泄露检测.tny](Netty-Buffer之ByteBuf内存泄露检测.tny.md)
  - [Netty-Buffer 之 ByteBuf 其它子类.tny](Netty-Buffer之ByteBuf其它子类.tny.md)
  - [Netty-Buffer 之 ByteBufAllocator 简介.tny](Netty-Buffer之ByteBufAllocator简介.tny.md)
  - [Netty-Buffer 之 ByteBufAllocator-UnpooledByteBufAllocator.tny](Netty-Buffer之ByteBufAllocator-UnpooledByteBufAllocator.tny.md)
  - [Netty-Buffer 之 ByteBufAllocator-PooledByteBufAllocator.tny](Netty-Buffer之ByteBufAllocator-PooledByteBufAllocator.tny.md)
  - [Netty-Buffer 之 Jemalloc 简介.tny](Netty-Buffer之Jemalloc简介.tny.md)
  - [Netty-Buffer 之 Jemalloc-PoolChunk.tny](Netty-Buffer之Jemalloc-PoolChunk.tny.md)
  - [Netty-Buffer 之 Jemalloc-PoolChunkList.tny](Netty-Buffer之Jemalloc-PoolChunkList.tny.md)
  - [Netty-Buffer 之 Jemalloc-PoolArena.tny](Netty-Buffer之Jemalloc-PoolArena.tny.md)
  - [Netty-Buffer 之 Jemalloc-PoolSubpage.tny](Netty-Buffer之Jemalloc-PoolSubpage.tny.md)
  - [Netty-Buffer 之 Jemalloc-PoolThreadCache.tny](Netty-Buffer之Jemalloc-PoolThreadCache.tny.md)

- Netty-ChannelHandler

  - [Netty-ChannelHandler 之简介.tny](Netty-ChannelHandler之简介.tny.md)
  - [Netty-ChannelHandler 之 ChannelInitializer.tny](Netty-ChannelHandler之ChannelInitializer.tny.md)
  - [Netty-ChannelHandler 之 SimpleChannelInboundHandler.tny](Netty-ChannelHandler之SimpleChannelInboundHandler.tny.md)
  - [Netty-ChannelHandler 之 LoggingHandler.tny](Netty-ChannelHandler之LoggingHandler.tny.md)
  - [Netty-ChannelHandler 之 IdleStateHandler.tny](Netty-ChannelHandler之IdleStateHandler.tny.md)
  - [Netty-ChannelHandler 之 AbstractTrafficShapingHandler.tny](Netty-ChannelHandler之AbstractTrafficShapingHandler.tny.md)

- Netty-Codec

  - [Netty-Codec 之 ByteToMessageDecoder-Cumulator.tny](Netty-Codec之ByteToMessageDecoder-Cumulator.tny.md)
  - [Netty-Codec 之 ByteToMessageDecoder-FrameDecoder.tny](Netty-Codec之ByteToMessageDecoder-FrameDecoder.tny.md)
  - [Netty-Codec 之 ByteToMessageCodec.tny](Netty-Codec之ByteToMessageCodec.tny.md)
  - [Netty-Codec 之 MessageToByteEncoder.tny](Netty-Codec之MessageToByteEncoder.tny.md)
  - [Netty-Codec 之 MessageToMessageCodec.tny](Netty-Codec之MessageToMessageCodec.tny.md)

- Netty-Util
  - [Netty-Util 之 Future.tny](Netty-Util之Future.tny.md)
  - [Netty-Util 之 FastThreadLocal.tny](Netty-Util之FastThreadLocal.tny.md)
  - [Netty-Util 之 Recycler.tny](Netty-Util之Recycler.tny.md)
  - [Netty-Util 之 HashedWheelTimer.tny](Netty-Util之HashedWheelTimer.tny.md)
  - [Netty-Util 之 MpscUnboundedArrayQueue.tny](Netty-Util之MpscUnboundedArrayQueue.tny.md)
