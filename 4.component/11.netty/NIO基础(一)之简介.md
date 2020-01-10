# 精尽 Netty 源码分析 —— NIO 基础（一）之简介

# 1. 概述

Java NIO( New IO 或者 Non Blocking IO ) ，从 Java 1.4 版本开始引入的**非阻塞** IO ，用于替换**标准**( 有些文章也称为**传统**，或者 Blocking IO 。下文统称为 BIO ) Java IO API 的 IO API 。
老艿艿：在一些文章中，会将 Java NIO 描述成**异步** IO ，实际是不太正确的： Java NIO 是**同步** IO ，Java AIO ( 也称为 NIO 2 )是**异步** IO。具体原因，推荐阅读文章：

* [《异步和非阻塞一样吗? (内容涉及 BIO, NIO, AIO, Netty)》](https://blog.csdn.net/matthew_zhang/article/details/71328697) 。
* [《BIO与NIO、AIO的区别(这个容易理解)》](https://blog.csdn.net/skiof007/article/details/52873421)

总结来说，在 **Unix IO 模型**的语境下：

* 同步和异步的区别：数据拷贝阶段是否需要完全由操作系统处理。
* 阻塞和非阻塞操作：是针对发起 IO 请求操作后，是否有立刻返回一个标志信息而不让请求线程等待。

因此，Java NIO 是**同步**且非阻塞的 IO 。

# 2. 核心组件

Java NIO 由如下**三个**核心组件组成：

* Channel
* Buffer
* Selector

后续的每篇文章，我们会分享对应的一个组件。

# 3. NIO 和 BIO 的对比

NIO 和 BIO 的区别主要体现在三个方面：
NIO BIO 基于缓冲区( Buffer ) 基于流( Stream ) **非**阻塞 IO 阻塞 IO 选择器( Selector ) 无

* 其中，选择器( Selector )是 NIO 能实现**非**阻塞的基础。

## 3.1 基于 Buffer 与基于 Stream

BIO 是面向字节流或者字符流的，而在 NIO 中，它摒弃了传统的 IO 流，而是引入 Channel 和 Buffer 的概念：从 Channel 中读取数据到 Buffer 中，或者将数据从 Buffer 中写到 Channel 中。

① 那么什么是**基于 Stream**呢？

在一般的 Java IO 操作中，我们以**流式**的方式，**顺序**的从一个 Stream 中读取一个或者多个字节，直至读取所有字节。因为它没有缓存区，所以我们就不能随意改变读取指针的位置。

② 那么什么是**基于 Buffer** 呢？

基于 Buffer 就显得有点不同了。我们在从 Channel 中读取数据到 Buffer 中，这样 Buffer 中就有了数据后，我们就可以对这些数据进行操作了。并且不同于一般的 Java IO 操作那样是**顺序**操作，NIO 中我们可以随意的读取任意位置的数据，这样大大增加了处理过程中的灵活性。
老艿艿：**写入**操作，也符合上述**读取**操作的情况。

## 3.2 阻塞与非阻塞 IO

Java IO 的各种流是**阻塞**的 IO 操作。这就意味着，当一个线程执行读或写 IO 操作时，该线程会被**阻塞**，直到有一些数据被读取，或者数据完全写入。

Java NIO 可以让我们**非阻塞**的使用 IO 操作。例如：

* 当一个线程执行从 Channel 执行读取 IO 操作时，当此时有数据，则读取数据并返回；当此时无数据，则直接返回**而不会阻塞当前线程**。
* 当一个线程执行向 Channel 执行写入 IO 操作时，**不需要阻塞等待它完全写入**，这个线程同时可以做别的事情。

也就是说，线程可以将非阻塞 IO 的空闲时间用于在其他 Channel 上执行 IO 操作。所以，一个单独的线程，可以管理多个 Channel 的读取和写入 IO 操作。

## 3.3 Selector

Java NIO 引入 Selector ( 选择器 )的概念，它是 Java NIO 得以实现非阻塞 IO 操作的**最最最关键**。

我们可以注册**多个** Channel 到**一个** Selector 中。而 Selector 内部的机制，就可以自动的为我们不断的执行查询( select )操作，判断这些注册的 Channel 是否有**已就绪的 IO 事件( 例如可读，可写，网络连接已完成 )**。
通过这样的机制，**一个**线程通过使用**一个** Selector ，就可以非常简单且高效的来管理**多个** Channel 了。

# 4. NIO 和 AIO 的对比

考虑到 Netty 4.1.X 版本实际并未基于 Java AIO 实现，所以我们就省略掉这块内容。那么，感兴趣的同学，可以自己 Google 下 Java NIO 和 Java AIO 的对比。

具体为什么 Netty 4.1.X 版本不支持 Java AIO 的原因，可参见 [《Netty（二）：Netty 为啥去掉支持 AIO ?》](https://juejin.im/entry/5a8ed33b6fb9a0634c26801c) 文章。

也因此，Netty 4.1.X 一般情况下，使用的是**同步非阻塞的 NIO 模型**。当然，如果真的有必要，也可以使用**同步阻塞的 BIO 模型**。

# 参考文章如下：
* [精尽 Netty 源码分析 —— NIO 基础（一）之简介](http://svip.iocoder.cn/Netty/nio-1-intro/)
* [《高并发 Java（8）：NIO 和 AIO》](http://www.importnew.com/21341.html)
* [《Java NIO 的前生今世 之一 简介》](https://segmentfault.com/a/1190000006824091)
* [《Java NIO 系列教程（十二） Java NIO 与 IO》](http://ifeve.com/java-nio-vs-io/)