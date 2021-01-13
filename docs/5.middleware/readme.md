# 中间件

## Web Server

### Nginx

- [《Ngnix 的基本学习-多进程和 Apache 的比较》](https://blog.csdn.net/qq_25797077/article/details/52200722)
  _ Nginx 通过异步非阻塞的事件处理机制实现高并发。Apache 每个请求独占一个线程，非常消耗系统资源。
  _ 事件驱动适合于 IO 密集型服务(Nginx)，多进程或线程适合于 CPU 密集型服务(Apache)，所以 Nginx 适合做反向代理，而非 web 服务器使用。

- [《nginx 与 Apache 的对比以及优缺点》](https://www.cnblogs.com/cunkouzh/p/5410154.html) \* nginx 只适合静态和反向代理，不适合处理动态请求。

### OpenResty

- [官方网站](http://openresty.org/cn/)
- [《浅谈 OpenResty》](http://www.linkedkeeper.com/detail/blog.action?bid=1034) \* 通过 Lua 模块可以在 Nginx 上进行开发。
- [agentzh 的 Nginx 教程](https://openresty.org/download/agentzh-nginx-tutorials-zhcn.html)

### Tengine

- [官方网站](http://tengine.taobao.org/)

### Apache Httpd

- [官方网站](http://httpd.apache.org/)

### Tomcat

#### 架构原理

- [《TOMCAT 原理详解及请求过程》](https://www.cnblogs.com/hggen/p/6264475.html)
- [《Tomcat 服务器原理详解》](https://www.cnblogs.com/crazylqy/p/4706223.html)
- [《Tomcat 系统架构与设计模式,第 1 部分: 工作原理》](https://www.ibm.com/developerworks/cn/java/j-lo-tomcat1/)

- [《四张图带你了解 Tomcat 系统架构》](https://blog.csdn.net/xlgen157387/article/details/79006434)

- [《JBoss vs. Tomcat: Choosing A Java Application Server》](https://www.futurehosting.com/blog/jboss-vs-tomcat-choosing-a-java-application-server/)
  _ Tomcat 是轻量级的 Serverlet 容器，没有实现全部 JEE 特性（比如持久化和事务处理），但可以通过其他组件代替，比如 Spring。
  _ Jboss 实现全部了 JEE 特性，软件开源免费、文档收费。

#### 调优方案

- [《Tomcat 调优方案》](https://www.cnblogs.com/sunfenqing/p/7339058.html) \* 启动 NIO 模式（或者 APR）；调整线程池；禁用 AJP 连接器（Nginx+tomcat 的架构，不需要 AJP）；

- [《tomcat http 协议与 ajp 协议》](http://blog.chinaunix.net/uid-20662363-id-3012760.html)
- [《AJP 与 HTTP 比较和分析》](http://dmouse.iteye.com/blog/1354527)
  _ AJP 协议（8009 端口）用于降低和前端 Server（如 Apache，而且需要支持 AJP 协议）的连接数(前端)，通过长连接提高性能。
  _ 并发高时，AJP 协议优于 HTTP 协议。

### Jetty

- [《Jetty 的工作原理以及与 Tomcat 的比较》](https://www.ibm.com/developerworks/cn/java/j-lo-jetty/)
- [《jetty 和 tomcat 优势比较》](https://blog.csdn.net/doutao6677/article/details/51957288)
  _ 架构比较:Jetty 的架构比 Tomcat 的更为简单。
  _ 性能比较：Jetty 和 Tomcat 性能方面差异不大，Jetty 默认采用 NIO 结束在处理 I/O 请求上更占优势，Tomcat 默认采用 BIO 处理 I/O 请求，Tomcat 适合处理少数非常繁忙的链接，处理静态资源时性能较差。 \* 其他方面：Jetty 的应用更加快速，修改简单，对新的 Servlet 规范的支持较好;Tomcat 对 JEE 和 Servlet 支持更加全面。

## 缓存

- [《缓存失效策略（FIFO 、LRU、LFU 三种算法的区别）》](https://blog.csdn.net/clementad/article/details/48229243)

### 本地缓存

- [《HashMap 本地缓存》](https://coderxing.gitbooks.io/architecture-evolution/di-er-pian-ff1a-feng-kuang-yuan-shi-ren/42-xing-neng-zhi-ben-di-huan-cun/421-ying-yong-ceng-ben-di-huan-cun/4211.html)

- [《EhCache 本地缓存》](https://coderxing.gitbooks.io/architecture-evolution/di-er-pian-ff1a-feng-kuang-yuan-shi-ren/42-xing-neng-zhi-ben-di-huan-cun/421-ying-yong-ceng-ben-di-huan-cun/4212-ehcache.html)
  _ 堆内、堆外、磁盘三级缓存。
  _ 可按照缓存空间容量进行设置。 \* 按照时间、次数等过期策略。

- [《Guava Cache》](https://coderxing.gitbooks.io/architecture-evolution/di-er-pian-ff1a-feng-kuang-yuan-shi-ren/42-xing-neng-zhi-ben-di-huan-cun/421-ying-yong-ceng-ben-di-huan-cun/4213-guava-cache.html) \* 简单轻量、无堆外、磁盘缓存。

* [《Nginx 本地缓存》](https://coderxing.gitbooks.io/architecture-evolution/di-er-pian-ff1a-feng-kuang-yuan-shi-ren/42-xing-neng-zhi-ben-di-huan-cun/422-fu-wu-duan-ben-di-huan-cun/nginx-ben-di-huan-cun.html)

* [《Pagespeed—懒人工具，服务器端加速》](https://coderxing.gitbooks.io/architecture-evolution/di-er-pian-ff1a-feng-kuang-yuan-shi-ren/42-xing-neng-zhi-ben-di-huan-cun/422-fu-wu-duan-ben-di-huan-cun/4222-pagespeed.html)

## 客户端缓存

- [《浏览器端缓存》](https://coderxing.gitbooks.io/architecture-evolution/di-er-pian-ff1a-feng-kuang-yuan-shi-ren/42-xing-neng-zhi-ben-di-huan-cun/423-ke-hu-duan-huan-cun.html) \* 主要是利用 Cache-Control 参数。

- [《H5 和移动端 WebView 缓存机制解析与实战》](https://mp.weixin.qq.com/s/qHm_dJBhVbv0pJs8Crp77w)

## 服务端缓存

### Web 缓存

- [nuster](https://github.com/jiangwenyuan/nuster) - nuster cache
- [varnish](https://github.com/varnishcache/varnish-cache) - varnish cache
- [squid](https://github.com/squid-cache/squid) - squid cache

### Memcached

- [《Memcached 教程》](http://www.runoob.com/Memcached/Memcached-tutorial.html)
- [《深入理解 Memcached 原理》](https://blog.csdn.net/chenleixing/article/details/47035453)
  _ 采用多路复用技术提高并发性。
  _ slab 分配算法： memcached 给 Slab 分配内存空间，默认是 1MB。分配给 Slab 之后 把 slab 的切分成大小相同的 chunk，Chunk 是用于缓存记录的内存空间，Chunk 的大小默认按照 1.25 倍的速度递增。好处是不会频繁申请内存，提高 IO 效率，坏处是会有一定的内存浪费。
- [《Memcached 软件工作原理》](https://www.jianshu.com/p/36e5cd400580)
- [《Memcache 技术分享：介绍、使用、存储、算法、优化、命中率》](http://zhihuzeye.com/archives/2361)

- [《memcache 中 add 、 set 、replace 的区别》](https://blog.csdn.net/liu251890347/article/details/37690045) \* 区别在于当 key 存在还是不存在时，返回值是 true 和 false 的。

- [**《memcached 全面剖析》**](https://pan.baidu.com/s/1qX00Lti?errno=0&errmsg=Auth%20Login%20Sucess&&bduss=&ssnerror=0&traceid=)

### Redis

- [《Redis 教程》](http://www.runoob.com/redis/redis-tutorial.html)
- [《redis 底层原理》](https://blog.csdn.net/wcf373722432/article/details/78678504)
  _ 使用 ziplist 存储链表，ziplist 是一种压缩链表，它的好处是更能节省内存空间，因为它所存储的内容都是在连续的内存区域当中的。
  _ 使用 skiplist(跳跃表)来存储有序集合对象、查找上先从高 Level 查起、时间复杂度和红黑树相当，实现容易，无锁、并发性好。
- [《Redis 持久化方式》](http://doc.redisfans.com/topic/persistence.html)
  _ RDB 方式：定期备份快照，常用于灾难恢复。优点：通过 fork 出的进程进行备份，不影响主进程、RDB 在恢复大数据集时的速度比 AOF 的恢复速度要快。缺点：会丢数据。
  _ AOF 方式：保存操作日志方式。优点：恢复时数据丢失少，缺点：文件大，回复慢。 \* 也可以两者结合使用。

- [《分布式缓存--序列 3--原子操作与 CAS 乐观锁》](https://blog.csdn.net/chunlongyu/article/details/53346436)

#### 架构

- [《Redis 单线程架构》](https://blog.csdn.net/sunhuiliang85/article/details/73656830)

#### 回收策略

- [《redis 的回收策略》](https://blog.csdn.net/qq_29108585/article/details/63251491)

### Tair

- [官方网站](https://github.com/alibaba/tair)
- [《Tair 和 Redis 的对比》](http://blog.csdn.net/farphone/article/details/53522383)
- 特点：可以配置备份节点数目，通过异步同步到备份节点
- 一致性 Hash 算法。
- 架构：和 Hadoop 的设计思想类似，有 Configserver，DataServer，Configserver 通过心跳来检测，Configserver 也有主备关系。

几种存储引擎:

- MDB，完全内存性，可以用来存储 Session 等数据。
- Rdb（类似于 Redis），轻量化，去除了 aof 之类的操作，支持 Restfull 操作
- LDB（LevelDB 存储引擎），持久化存储，LDB 作为 rdb 的持久化，google 实现，比较高效，理论基础是 LSM(Log-Structured-Merge Tree)算法，现在内存中修改数据，达到一定量时（和内存汇总的旧数据一同写入磁盘）再写入磁盘，存储更加高效，县比喻 Hash 算法。
- Tair 采用共享内存来存储数据，如果服务挂掉（非服务器），重启服务之后，数据亦然还在。

## 消息队列

- [《消息队列-推/拉模式学习 & ActiveMQ 及 JMS 学习》](https://www.cnblogs.com/charlesblc/p/6045238.html)
  _ RabbitMQ 消费者默认是推模式（也支持拉模式）。
  _ Kafka 默认是拉模式。
  _ Push 方式：优点是可以尽可能快地将消息发送给消费者，缺点是如果消费者处理能力跟不上，消费者的缓冲区可能会溢出。
  _ Pull 方式：优点是消费端可以按处理能力进行拉去，缺点是会增加消息延迟。

- [《Kafka、RabbitMQ、RocketMQ 等消息中间件的对比 —— 消息发送性能和区别》](https://blog.csdn.net/yunfeng482/article/details/72856762)

### 消息总线

消息总线相当于在消息队列之上做了一层封装，统一入口，统一管控、简化接入成本。

- [《消息总线 VS 消息队列》](https://blog.csdn.net/yanghua_kobe/article/details/43877281)

### 消息的顺序

- [《如何保证消费者接收消息的顺序》](https://www.cnblogs.com/cjsblog/p/8267892.html)

### RabbitMQ

支持事务，推拉模式都是支持、适合需要可靠性消息传输的场景。

- [《RabbitMQ 的应用场景以及基本原理介绍》](https://blog.csdn.net/whoamiyang/article/details/54954780)
- [《消息队列之 RabbitMQ》](https://www.jianshu.com/p/79ca08116d57)
- [《RabbitMQ 之消息确认机制（事务+Confirm）》](https://blog.csdn.net/u013256816/article/details/55515234)

### RocketMQ

Java 实现，推拉模式都是支持，吞吐量逊于 Kafka。可以保证消息顺序。

- [《RocketMQ 实战之快速入门》](https://www.jianshu.com/p/824066d70da8)
- [《RocketMQ 源码解析》](http://www.iocoder.cn/categories/RocketMQ/?vip&architect-awesome)

### ActiveMQ

纯 Java 实现，兼容 JMS，可以内嵌于 Java 应用中。

- [《ActiveMQ 消息队列介绍》](https://www.cnblogs.com/wintersun/p/3962302.html)

### Kafka

高吞吐量、采用拉模式。适合高 IO 场景，比如日志同步。

- [官方网站](http://kafka.apache.org/)
- [《各消息队列对比，Kafka 深度解析，众人推荐，精彩好文！》](https://blog.csdn.net/allthesametome/article/details/47362451)
- [《Kafka 分区机制介绍与示例》](http://lxw1234.com/archives/2015/10/538.htm)

### Redis 消息推送

生产者、消费者模式完全是客户端行为，list 和 拉模式实现，阻塞等待采用 blpop 指令。

- [《Redis 学习笔记之十：Redis 用作消息队列》](https://blog.csdn.net/qq_34212276/article/details/78455004)

### ZeroMQ

TODO

## 定时调度

### 单机定时调度

- [《linux 定时任务 cron 配置》](https://www.cnblogs.com/shuaiqing/p/7742382.html)

- [《Linux cron 运行原理》](https://my.oschina.net/daquan/blog/483305) \* fork 进程 + sleep 轮询

- [《Quartz 使用总结》](https://www.cnblogs.com/drift-ice/p/3817269.html)
- [《Quartz 源码解析 ---- 触发器按时启动原理》](https://blog.csdn.net/wenniuwuren/article/details/42082981/)
- [《quartz 原理揭秘和源码解读》](https://www.jianshu.com/p/bab8e4e32952) \* 定时调度在 QuartzSchedulerThread 代码中，while()无限循环，每次循环取出时间将到的 trigger，触发对应的 job，直到调度器线程被关闭。

### 分布式定时调度

- [《这些优秀的国产分布式任务调度系统，你用过几个？》](https://blog.csdn.net/qq_16216221/article/details/70314337) \* opencron、LTS、XXL-JOB、Elastic-Job、Uncode-Schedule、Antares

- [《Quartz 任务调度的基本实现原理》](https://www.cnblogs.com/zhenyuyaodidiao/p/4755649.html) \* Quartz 集群中，独立的 Quartz 节点并不与另一其的节点或是管理节点通信，而是通过相同的数据库表来感知到另一 Quartz 应用的
- [《Elastic-Job-Lite 源码解析》](http://www.iocoder.cn/categories/Elastic-Job-Lite/?vip&architect-awesome)
- [《Elastic-Job-Cloud 源码解析》](http://www.iocoder.cn/categories/Elastic-Job-Cloud/?vip&architect-awesome)

## RPC

- [《从零开始实现 RPC 框架 - RPC 原理及实现》](https://blog.csdn.net/top_code/article/details/54615853) \* 核心角色：Server: 暴露服务的服务提供方、Client: 调用远程服务的服务消费方、Registry: 服务注册与发现的注册中心。

- [《分布式 RPC 框架性能大比拼 dubbo、motan、rpcx、gRPC、thrift 的性能比较》](https://blog.csdn.net/testcs_dn/article/details/78050590)

### Dubbo

- [官方网站](http://dubbo.apache.org/)
- [dubbo 实现原理简单介绍](https://www.cnblogs.com/steven520213/p/7606598.html)

** SPI **
TODO

### Thrift

- [官方网站](http://thrift.apache.org/)
- [《Thrift RPC 详解》](https://blog.csdn.net/kesonyk/article/details/50924489) \* 支持多语言，通过中间语言定义接口。

### gRPC

服务端可以认证加密，在外网环境下，可以保证数据安全。

- [官方网站](https://grpc.io/)
- [《你应该知道的 RPC 原理》](https://www.cnblogs.com/LBSer/p/4853234.html)

## 数据库中间件

### Sharding Jdbc

- [官网](http://shardingjdbc.io/)
- [源码解析](http://www.iocoder.cn/categories/Sharding-JDBC/?vip&architect-awesome)

## 日志系统

### 日志搜集

- [《从零开始搭建一个 ELKB 日志收集系统》](http://cjting.me/misc/build-log-system-with-elkb/)
- [《用 ELK 搭建简单的日志收集分析系统》](https://blog.csdn.net/lzw_2006/article/details/51280058)
- [《日志收集系统-探究》](https://www.cnblogs.com/beginmind/p/6058194.html)

## 配置中心

- [Apollo - 携程开源的配置中心应用](https://github.com/ctripcorp/apollo)
  _ Spring Boot 和 Spring Cloud
  _ 支持推、拉模式更新配置 \* 支持多种语言

- [《基于 zookeeper 实现统一配置管理》](https://blog.csdn.net/u011320740/article/details/78742625)

- [《 Spring Cloud Config 分布式配置中心使用教程》](https://www.cnblogs.com/shamo89/p/8016908.html)

servlet 3.0 异步特性可用于配置中心的客户端

- [《servlet3.0 新特性——异步处理》](https://www.cnblogs.com/dogdogwang/p/7151866.html)

## API 网关

主要职责：请求转发、安全认证、协议转换、容灾。

- [《API 网关那些儿》](http://yunlzheng.github.io/2017/03/14/the-things-about-api-gateway/)
- [《谈 API 网关的背景、架构以及落地方案》](http://www.infoq.com/cn/news/2016/07/API-background-architecture-floo)

- [《使用 Zuul 构建 API Gateway》](https://blog.csdn.net/zhanglh046/article/details/78651993)
- [《Spring Cloud Gateway 源码解析》](http://www.iocoder.cn/categories/Spring-Cloud-Gateway/?vip&architect-awesome)
- [《HTTP API 网关选择之一 Kong 介绍》](https://mp.weixin.qq.com/s/LIq2CiXJQmmjBC0yvYLY5A)