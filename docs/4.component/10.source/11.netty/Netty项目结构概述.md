# Netty 简介（一）之项目结构

# 1. 概述

本文主要分享 **Netty 的项目结构**。
希望通过本文能让胖友对 Netty 的整体项目有个简单的了解。

在拉取 Netty 项目后，我们会发现拆分了**好多** Maven 项目。是不是内心一紧，产生了恐惧感？不要方，我们就是继续怼。

![项目结构](http://static2.iocoder.cn/images/Netty/2018_03_01/01.png) 项目结构

# 2. 代码统计

这里先分享一个小技巧。笔者在开始源码学习时，会首先了解项目的代码量。

**第一种方式**，使用 [IDEA Statistic](https://plugins.jetbrains.com/plugin/4509-statistic) 插件，统计整体代码量。

![Statistic 统计代码量](http://static2.iocoder.cn/images/Netty/2018_03_01/02.png) Statistic 统计代码量

我们可以粗略的看到，总的代码量在 251365 行。这其中还包括单元测试，示例等等代码。
所以，不慌。

**第二种方式**，使用 [Shell 脚本命令逐个 Maven 模块统计](http://blog.csdn.net/yhhwatl/article/details/52623879) 。

一般情况下，笔者使用 find . -name "/_.java"|xargs cat|grep -v -e ^$ -e ^\s/_\/\/./\*$|wc -l。
这个命令只过滤了**部分注释**，所以相比 [IDEA Statistic](https://plugins.jetbrains.com/plugin/4509-statistic) 会**偏多**。

当然，考虑到准确性，胖友需要手动 cd 到每个 Maven 项目的 src/main/java 目录下，以达到排除单元测试的代码量。

![Shell 脚本统计代码量](http://static2.iocoder.cn/images/Netty/2018_03_01/03.png) Shell 脚本统计代码量

- 😈 偷懒了下，暂时只统计**核心**模块，未统计**拓展**模块。

# 3. 架构图

在看具体每个 Netty 的 Maven 项目之前，我们还是先来看看 Netty 的整体架构图。

![架构图](http://static2.iocoder.cn/images/Netty/2018_03_01/04.png) 架构图

- **Core** ：核心部分，是底层的网络通用抽象和部分实现。

- Extensible Event Model ：可拓展的事件模型。Netty 是基于事件模型的网络应用框架。
- Universal Communication API ：通用的通信 API 层。Netty 定义了一套抽象的通用通信层的 API 。
- Zero-Copy-Capable Rich Byte Buffer ：支持零拷贝特性的 Byte Buffer 实现。
- **Transport Services** ：传输( 通信 )服务，具体的网络传输的定义与实现。

- Socket & Datagram ：TCP 和 UDP 的传输实现。
- HTTP Tunnel ：HTTP 通道的传输实现。
- In-VM Piple ：JVM 内部的传输实现。😈 理解起来有点怪，后续看具体代码，会易懂。
- **Protocol Support** ：协议支持。Netty 对于一些通用协议的编解码实现。例如：HTTP、Redis、DNS 等等。

# 4. 项目依赖图

Netty 的 Maven 项目之间**主要依赖**如下图：

![依赖图](http://static2.iocoder.cn/images/Netty/2018_03_01/07.png) 依赖图

- 本图省略**非主要依赖**。例如，handler-proxy 对 codec 有依赖，但是并未画出。
- 本图省略**非主要的项目**。例如，resolver、testsuite、example 等等。下面，我们来详细介绍每个项目。

# 5. common

common 项目，该项目是一个通用的工具类项目，几乎被所有的其它项目依赖使用，它提供了一些数据类型处理工具类，并发编程以及多线程的扩展，计数器等等通用的工具类。

![common 项目](http://static2.iocoder.cn/images/Netty/2018_03_01/05.png) common 项目

# 6. buffer

该项目实现了 Netty 架构图中的 Zero-Copy-Capable Rich Byte Buffer 。

buffer 项目，该项目下是 Netty 自行实现的一个 Byte Buffer 字节缓冲区。该包的实现相对于 JDK 自带的 ByteBuffer 有很多**优点**：无论是 API 的功能，使用体验，性能都要更加优秀。它提供了**一系列( 多种 )**的抽象定义以及实现，以满足不同场景下的需要。

![buffer 项目](http://static2.iocoder.cn/images/Netty/2018_03_01/06.png) buffer 项目

# 7. transport

该项是核心项目，实现了 Netty 架构图中 Transport Services、Universal Communication API 和 Extensible Event Model 等多部分内容。

transport 项目，该项目是网络传输通道的抽象和实现。它定义通信的统一通信 API ，统一了 JDK 的 OIO、NIO ( 不包括 AIO )等多种编程接口。

![transport 项目](http://static2.iocoder.cn/images/Netty/2018_03_01/08.png) transport 项目

另外，它提供了多个子项目，实现不同的传输类型。
例如：
transport-native-epoll、transport-native-kqueue、transport-rxtx、transport-udt、transport-sctp 等等。

# 8. codec

该项目实现了 Netty 架构图中的 Protocol Support 。

codec 项目，该项目是协议编解码的抽象与**部分**实现：JSON、Google Protocol、Base64、XML 等等。

![codec 项目](http://static2.iocoder.cn/images/Netty/2018_03_01/09.png) codec 项目

另外，它提供了多个子项目，实现不同协议的编解码。例如：
codec-dns、codec-haproxy、codec-http、codec-http2、codec-mqtt、
codec-redis、codec-memcached、codec-smtp、codec-socks、
codec-stomp、codec-xml 等等。

# 9. handler

handler 项目，该项目是提供**内置的**连接通道处理器( ChannelHandler )实现类。例如：SSL 处理器、日志处理器等等。

![handler 项目](http://static2.iocoder.cn/images/Netty/2018_03_01/10.png) handler 项目

另外，它提供了一个子项目 handler-proxy，实现对 HTTP、Socks 4、Socks 5 的代理转发。

# 10. example

example 项目，该项目是提供各种 Netty 使用示例，良心开源项目。

![example 项目](http://static2.iocoder.cn/images/Netty/2018_03_01/11.png) example 项目

# 11. 其它项目

Netty 中还有其它项目，考虑到不是本系列的重点，就暂时进行省略。

- all
  ：All In One 的 pom 声明。
- bom：Netty Bill Of Materials 的缩写，不了解的胖友，可以看看 [《Maven 与 Spring BOM( Bill Of Materials )简化 Spring 版本控制》](https://blog.csdn.net/fanxiaobin577328725/article/details/66974896) 。
- microbench：微基准测试。
- resolver：终端( Endpoint ) 的地址解析器。
- resolver-dns
- tarball：All In One 打包工具。
- testsuite：测试集。测试集( TestSuite ) ：测试集是把多个相关测试归入一个组的表达方式。在 Junit 中，如果我们没有明确的定义一个测试集，那么 Juint 会自动的提供一个测试集。一个测试集一般将同一个包的测试类归入一组。
- testsuite-autobahhn
- testsuite-http2
- testsuite-osgi

# 参考

本文基于杨武兵大佬的 [《Netty 源码分析系列 —— 概述》](https://my.oschina.net/ywbrj042/blog/856596) 进行修改。
