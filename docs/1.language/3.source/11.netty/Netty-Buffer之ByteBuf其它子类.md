# 精尽 Netty 源码解析 —— Buffer 之 ByteBuf（四）其它子类

# []( "1. 概述")1. 概述

在前面三篇文章，我们已经看了 ByteBuf 最最最核心的几个实现类。而剩余的，主要是如下几个类，如[下图](http://static2.iocoder.cn/images/Netty/2018_08_10/01.png)所示：

[![类图](http://static2.iocoder.cn/images/Netty/2018_08_10/01.png)](http://static2.iocoder.cn/images/Netty/2018_08_10/01.png '类图')类图

整理起来，用途主要是：

- Swap
- Slice
- Duplicate
- ReadOnly
- Composite

因为老艿艿想要尽快去写 Jemalloc 内存管理相关的内容，所以本文先暂时“省略”。感兴趣的胖友，可以自己去研究下落。

TODO 1016 派生类

# []( "2. Composite ByteBuf")2. Composite ByteBuf

因为 Composite ByteBuf 是比较重要的内容，胖友一定要自己去研究下。推荐阅读文章：

- 键走偏锋 [《对于 Netty ByteBuf 的零拷贝（Zero Copy）的理解》](https://my.oschina.net/LucasZhu/blog/1617222)
- [《Netty 学习笔记 —— 类 CompositeByteBuf》](https://skyao.gitbooks.io/learning-netty/content/buffer/class_CompositeByteBuf.html)

# []( "666. 彩蛋")666. 彩蛋

没有彩蛋。
