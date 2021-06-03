> ## RPC

**RPC（Remote Procedure Call）—远程过程调用** ，它是一种通过网络从远程计算机程序上请求服务，而不需要了解底层网络技术的协议。RPC 协议假定某些传输协议的存在，如 TCP 或 UDP，为通信程序之间携带信息数据。在 OSI 网络通信模型中，RPC 跨越了传输层和应用层。RPC 使得开发分布式程序就像开发本地程序一样简单。

**RPC 采用客户端（服务调用方）/服务器端（服务提供方）模式，** 都运行在自己的 JVM 中。客户端只需要引入要使用的接口，接口的实现和运行都在服务器端。RPC 主要依赖的技术包括序列化、反序列化和数据传输协议，这是一种定义与实现相分离的设计。

**目前 Java 使用比较多的 RPC 方案主要有 RMI（JDK 自带）、Hessian、Dubbo 以及 Thrift 等。**

**注意： RPC 主要指内部服务之间的调用，RESTful 也可以用于内部服务之间的调用，但其主要用途还在于外部系统提供服务，因此没有将其包含在本知识点内。**

### 常见 RPC 框架：

- **RMI（JDK 自带）：** JDK 自带的 RPC

  详细内容可以参考：[从懵逼到恍然大悟之 Java 中 RMI 的使用](https://blog.csdn.net/lmy86263/article/details/72594760)

- **Dubbo:** Dubbo 是 阿里巴巴公司开源的一个高性能优秀的服务框架，使得应用可通过高性能的 RPC 实现服务的输出和输入功能，可以和 Spring 框架无缝集成。

  详细内容可以参考：

  - [ 高性能优秀的服务框架-dubbo 介绍](https://blog.csdn.net/qq_34337272/article/details/79862899)

  - [Dubbo 是什么？能做什么？](https://blog.csdn.net/houshaolin/article/details/76408399)

* **Hessian：** Hessian 是一个轻量级的 remotingonhttp 工具，使用简单的方法提供了 RMI 的功能。 相比 WebService，Hessian 更简单、快捷。采用的是二进制 RPC 协议，因为采用的是二进制协议，所以它很适合于发送二进制数据。

  详细内容可以参考： [Hessian 的使用以及理解](https://blog.csdn.net/sunwei_pyw/article/details/74002351)

* **Thrift：** Apache Thrift 是 Facebook 开源的跨语言的 RPC 通信框架，目前已经捐献给 Apache 基金会管理，由于其跨语言特性和出色的性能，在很多互联网公司得到应用，有能力的公司甚至会基于 thrift 研发一套分布式服务框架，增加诸如服务注册、服务发现等功能。


    详细内容可以参考： [【Java】分布式RPC通信框架Apache Thrift 使用总结](https://www.cnblogs.com/zeze/p/8628585.html)

### 如何进行选择：

- **是否允许代码侵入：** 即需要依赖相应的代码生成器生成代码，比如 Thrift。
- **是否需要长连接获取高性能：** 如果对于性能需求较高的 haul，那么可以果断选择基于 TCP 的 Thrift、Dubbo。
- **是否需要跨越网段、跨越防火墙：** 这种情况一般选择基于 HTTP 协议的 Hessian 和 Thrift 的 HTTP Transport。

此外，Google 推出的基于 HTTP2.0 的 gRPC 框架也开始得到应用，其序列化协议基于 Protobuf，网络框架使用的是 Netty4,但是其需要生成代码，可扩展性也比较差。

> ## 消息中间件

**消息中间件，也可以叫做中央消息队列或者是消息队列（区别于本地消息队列，本地消息队列指的是 JVM 内的队列实现）**，是一种独立的队列系统，消息中间件经常用来解决内部服务之间的 **异步调用问题** 。请求服务方把请求队列放到队列中即可返回，然后等待服务提供方去队列中获取请求进行处理，之后通过回调等机制把结果返回给请求服务方。

异步调用只是消息中间件一个非常常见的应用场景。此外，常用的消息队列应用场景还偷如下几个：

- **解耦 ：** 一个业务的非核心流程需要依赖其他系统，但结果并不重要，有通知即可。
- **最终一致性 ：** 指的是两个系统的状态保持一致，可以有一定的延迟，只要最终达到一致性即可。经常用在解决分布式事务上。
- **广播 ：** 消息队列最基本的功能。生产者只负责生产消息，订阅者接收消息。
- **错峰和流控**

具体可以参考：

[《消息队列深入解析》](https://blog.csdn.net/qq_34337272/article/details/80029918)

当前使用较多的消息队列有 ActiveMQ（性能差，不推荐使用）、RabbitMQ、RocketMQ、Kafka 等等，我们之前提到的 redis 数据库也可以实现消息队列，不过不推荐，redis 本身设计就不是用来做消息队列的。

- **ActiveMQ：** ActiveMQ 是 Apache 出品，最流行的，能力强劲的开源消息总线。ActiveMQ 是一个完全支持 JMS1.1 和 J2EE 1.4 规范的 JMSProvider 实现,尽管 JMS 规范出台已经是很久的事情了,但是 JMS 在当今的 J2EE 应用中间仍然扮演着特殊的地位。

  具体可以参考：

  [《消息队列 ActiveMQ 的使用详解》](https://blog.csdn.net/qq_34337272/article/details/80031702)

- **RabbitMQ:** RabbitMQ 是一个由 Erlang 语言开发的 AMQP 的开源实现。RabbitMQ 最初起源于金融系统，用于在分布式系统中存储转发消息，在易用性、扩展性、高可用性等方面表现不俗
  > AMQP ：Advanced Message Queue，高级消息队列协议。它是应用层协议的一个开放标准，为面向消息的中间件设计，基于此协议的客户端与消息中间件可传递消息，并不受产品、开发语言等条件的限制。

具体可以参考：

[《消息队列之 RabbitMQ》](https://www.jianshu.com/p/79ca08116d57)

- **RocketMQ：**

  具体可以参考：

  [《RocketMQ 实战之快速入门》](https://www.jianshu.com/p/824066d70da8)

  [《十分钟入门 RocketMQ》](http://jm.taobao.org/2017/01/12/rocketmq-quick-start-in-10-minutes/) （阿里中间件团队博客）

* **Kafka**：Kafka 是一个分布式的、可分区的、可复制的、基于发布/订阅的消息系统（现在官方的描述是“一个分布式流平台”）,Kafka 主要用于大数据领域,当然在分布式系统中也有应用。目前市面上流行的消息队列 RocketMQ 就是阿里借鉴 Kafka 的原理、用 Java 开发而得。

  具体可以参考：

  [《Kafka 应用场景》](http://book.51cto.com/art/201801/565244.htm)

  [《初谈 Kafka》](https://mp.weixin.qq.com/s?__biz=MzU4NDQ4MzU5OA==&mid=2247484106&idx=1&sn=aa1999895d009d91eb3692a3e6429d18&chksm=fd9854abcaefddbd1101ca5dc2c7c783d7171320d6300d9b2d8e68b7ef8abd2b02ea03e03600#rd)

**推荐阅读：**

[《Kafka、RabbitMQ、RocketMQ 等消息中间件的对比 —— 消息发送性能和区别》](https://mp.weixin.qq.com/s?__biz=MzU5OTMyODAyNg==&mid=2247484721&idx=1&sn=11e4e29886e581dd328311d308ccc068&chksm=feb7d144c9c058529465b02a4e26a25ef76b60be8984ace9e4a0f5d3d98ca52e014ecb73b061&scene=21#wechat_redirect)
