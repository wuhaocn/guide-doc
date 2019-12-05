# 网络


## 协议

### OSI 七层协议

* [《OSI七层协议模型、TCP/IP四层模型学习笔记》](https://www.cnblogs.com/Robin-YB/p/6668762.html)

### TCP/IP
* [《深入浅出 TCP/IP 协议》](https://www.cnblogs.com/onepixel/p/7092302.html)
* [《TCP协议中的三次握手和四次挥手》](https://blog.csdn.net/whuslei/article/details/6667471/)

### HTTP
* [《http协议详解(超详细)》](https://www.cnblogs.com/wangning528/p/6388464.html)

### HTTP2.0
* [《HTTP 2.0 原理详细分析》](https://blog.csdn.net/zhuyiquan/article/details/69257126)
* [《HTTP2.0的基本单位为二进制帧》](https://blog.csdn.net/u012657197/article/details/77877840)
	* 利用二进制帧负责传输。
	* 多路复用。

### HTTPS
* [《https原理通俗了解》](https://www.cnblogs.com/zhangshitong/p/6478721.html)
	* 使用非对称加密协商加密算法
	* 使用对称加密方式传输数据
	* 使用第三方机构签发的证书，来加密公钥，用于公钥的安全传输、防止被中间人串改。

* [《八大免费SSL证书-给你的网站免费添加Https安全加密》](https://blog.csdn.net/enweitech/article/details/53213862)

## 网络模型

* [《web优化必须了解的原理之I/o的五种模型和web的三种工作模式》](http://blog.51cto.com/litaotao/1289790)
	* 五种I/O模型：阻塞I/O，非阻塞I/O，I/O复用、事件(信号)驱动I/O、异步I/O，前四种I/O属于同步操作，I/O的第一阶段不同、第二阶段相同，最后的一种则属于异步操作。
	* 三种 Web Server 工作方式：Prefork(多进程)、Worker方式(线程方式)、Event方式。

* [《select、poll、epoll之间的区别总结》](http://www.cnblogs.com/Anker/p/3265058.html)
	* select，poll，epoll本质上都是同步I/O，因为他们都需要在读写事件就绪后自己负责进行读写，也就是说这个读写过程是阻塞的。
	* select 有打开文件描述符数量限制，默认1024（2048 for x64），100万并发，就要用1000个进程、切换开销大；poll采用链表结构，没有数量限制。
	* select，poll “醒着”的时候要遍历整个fd集合，而epoll在“醒着”的时候只要判断一下就绪链表是否为空就行了，通过回调机制节省大量CPU时间；select，poll每次调用都要把fd集合从用户态往内核态拷贝一次，而epoll只要一次拷贝。
	* poll会随着并发增加，性能逐渐下降，epoll采用红黑树结构，性能稳定，不会随着连接数增加而降低。
	
* [《select，poll，epoll比较  》](http://xingyunbaijunwei.blog.163.com/blog/static/76538067201241685556302/)
	* 在连接数少并且连接都十分活跃的情况下，select和poll的性能可能比epoll好，毕竟epoll的通知机制需要很多函数回调。

* [《深入理解Java NIO》](https://www.cnblogs.com/geason/p/5774096.html)
	* NIO 是一种同步非阻塞的 IO 模型。同步是指线程不断轮询 IO 事件是否就绪，非阻塞是指线程在等待 IO 的时候，可以同时做其他任务

* [《BIO与NIO、AIO的区别》](https://blog.csdn.net/skiof007/article/details/52873421)

* [《两种高效的服务器设计模型：Reactor和Proactor模型》](https://blog.csdn.net/u013074465/article/details/46276967)

### Epoll

* [《epoll使用详解（精髓）》](https://www.cnblogs.com/fnlingnzb-learner/p/5835573.html)

### Java NIO
* [《深入理解Java NIO》](https://www.cnblogs.com/geason/p/5774096.html)
* [《Java NIO编写Socket服务器的一个例子》](https://blog.csdn.net/xidianliuy/article/details/51612676)

### kqueue
* [《kqueue用法简介》](http://www.cnblogs.com/luminocean/p/5631336.html)

## 连接和短连接

* [《TCP/IP系列——长连接与短连接的区别》](https://www.cnblogs.com/pangguoping/p/5571422.html)

## 框架

* [《Netty原理剖析》](https://blog.csdn.net/excellentyuxiao/article/details/53390408)
	* Reactor 模式介绍。
	* Netty 是 Reactor 模式的一种实现。

## 零拷贝（Zero-copy）
* [《对于 Netty ByteBuf 的零拷贝(Zero Copy) 的理解》](https://www.cnblogs.com/xys1228/p/6088805.html)
	* 多个物理分离的buffer，通过逻辑上合并成为一个，从而避免了数据在内存之间的拷贝。

## 序列化(二进制协议)

### Hessian
* [《Hessian原理分析》](https://www.cnblogs.com/happyday56/p/4268249.html)
Binary-RPC;不仅仅是序列化

### Protobuf
* [《Protobuf协议的Java应用例子》](https://blog.csdn.net/antgan/article/details/52103966)
Goolge出品、占用空间和效率完胜其他序列化类库，如Hessian；需要编写  .proto 文件。
* [《Protocol Buffers序列化协议及应用》](https://worktile.com/tech/share/prototol-buffers)
  	* 关于协议的解释；缺点：可读性差;

* [《简单的使用 protobuf 和 protostuff》](https://blog.csdn.net/eric520zenobia/article/details/53766571)
	* protostuff 的好处是不用写 .proto 文件，Java 对象直接就可以序列化。