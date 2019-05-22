# 分布式设计

## 扩展性设计

* [《架构师不可不知的十大可扩展架构》](https://blog.csdn.net/hemin1003/article/details/53633926)
	* 总结下来，通用的套路就是分布、缓存及异步处理。 

* [《可扩展性设计之数据切分》](https://yq.aliyun.com/articles/38119)
	* 水平切分+垂直切分
	* 利用中间件进行分片如，MySQL Proxy。
	* 利用分片策略进行切分，如按照ID取模。 
* [《说说如何实现可扩展性的大型网站架构》](https://blog.csdn.net/deniro_li/article/details/78458306)
	* 分布式服务+消息队列。

* [《大型网站技术架构（七）--网站的可扩展性架构》](https://blog.csdn.net/chaofanwei/article/details/29191073)

## 稳定性 & 高可用

* [《系统设计：关于高可用系统的一些技术方案》](https://blog.csdn.net/hustspy1990/article/details/78008324)
	* 可扩展：水平扩展、垂直扩展。 通过冗余部署，避免单点故障。
	* 隔离：避免单一业务占用全部资源。避免业务之间的相互影响 2. 机房隔离避免单点故障。
	* 解耦：降低维护成本，降低耦合风险。减少依赖，减少相互间的影响。
	* 限流：滑动窗口计数法、漏桶算法、令牌桶算法等算法。遇到突发流量时，保证系统稳定。
	* 降级：紧急情况下释放非核心功能的资源。牺牲非核心业务，保证核心业务的高可用。
	* 熔断：异常情况超出阈值进入熔断状态，快速失败。减少不稳定的外部依赖对核心服务的影响。
	* 自动化测试：通过完善的测试，减少发布引起的故障。
	* 灰度发布：灰度发布是速度与安全性作为妥协，能够有效减少发布故障。


* [《关于高可用的系统》](https://coolshell.cn/articles/17459.html)
	* 设计原则：数据不丢(持久化)；服务高可用(服务副本)；绝对的100%高可用很难，目标是做到尽可能多的9，如99.999%（全年累计只有5分钟）。	 

### 硬件负载均衡

* [《转！！负载均衡器技术Nginx和F5的优缺点对比》](https://www.cnblogs.com/wuyun-blog/p/6186198.html)
	* 主要是和F5对比。

* [《软/硬件负载均衡产品 你知多少？》](https://www.cnblogs.com/lcword/p/5773296.html)

### 软件负载均衡

* [《几种负载均衡算法》](https://www.cnblogs.com/tianzhiliang/articles/2317808.html)
	轮寻、权重、负载、最少连接、QoS
* [《DNS负载均衡》](https://coderxing.gitbooks.io/architecture-evolution/di-san-pian-ff1a-bu-luo/611-dns-fang-shi.html)
	* 配置简单，更新速度慢。 
* [《Nginx负载均衡》](https://coderxing.gitbooks.io/architecture-evolution/di-san-pian-ff1a-bu-luo/613-nginx-fu-zai-jun-heng.html)
	* 简单轻量、学习成本低；主要适用于web应用。

*  [《借助LVS+Keepalived实现负载均衡 》](https://www.cnblogs.com/edisonchou/p/4281978.html)
	* 配置比较负载、只支持到4层，性能较高。

* [《HAProxy用法详解 全网最详细中文文档》](http://www.ttlsa.com/linux/haproxy-study-tutorial/)
	* 支持到七层（比如HTTP）、功能比较全面，性能也不错。

* [《Haproxy+Keepalived+MySQL实现读均衡负载》](http://blog.itpub.net/25704976/viewspace-1319781/)
	* 主要是用户读请求的负载均衡。

* [《rabbitmq+haproxy+keepalived实现高可用集群搭建》](https://www.cnblogs.com/lylife/p/5584019.html)

### 限流

* [《谈谈高并发系统的限流》](https://www.cnblogs.com/haoxinyue/p/6792309.html)
	* 计数器：通过滑动窗口计数器，控制单位时间内的请求次数，简单粗暴。
	* 漏桶算法：固定容量的漏桶，漏桶满了就丢弃请求，比较常用。
	* 令牌桶算法：固定容量的令牌桶，按照一定速率添加令牌，处理请求前需要拿到令牌，拿不到令牌则丢弃请求，或进入丢队列，可以通过控制添加令牌的速率，来控制整体速度。Guava 中的 RateLimiter 是令牌桶的实现。
	* Nginx 限流：通过 `limit_req` 等模块限制并发连接数。

### 应用层容灾

* [《防雪崩利器：熔断器 Hystrix 的原理与使用》](https://segmentfault.com/a/1190000005988895)
	* 雪崩效应原因：硬件故障、硬件故障、程序Bug、重试加大流量、用户大量请求。 
	* 雪崩的对策：限流、改进缓存模式(缓存预加载、同步调用改异步)、自动扩容、降级。
	* Hystrix设计原则：
		* 资源隔离：Hystrix通过将每个依赖服务分配独立的线程池进行资源隔离, 从而避免服务雪崩。
		* 熔断开关：服务的健康状况 = 请求失败数 / 请求总数，通过阈值设定和滑动窗口控制开关。
		* 命令模式：通过继承 HystrixCommand 来包装服务调用逻辑。 

* [《缓存穿透，缓存击穿，缓存雪崩解决方案分析》](https://blog.csdn.net/zeb_perfect/article/details/54135506)
* [《缓存击穿、失效以及热点key问题》](https://blog.csdn.net/zeb_perfect/article/details/54135506) 
	* 主要策略：失效瞬间：单机使用锁；使用分布式锁；不过期；
	* 热点数据：热点数据单独存储；使用本地缓存；分成多个子key；

### 跨机房容灾

* [《“异地多活”多机房部署经验谈》](http://dc.idcquan.com/ywgl/71559.shtml)
	* 通过自研中间件进行数据同步。 

* [《异地多活（异地双活）实践经验》](https://blog.csdn.net/jeffreynicole/article/details/48135093)
	* 注意延迟问题，多次跨机房调用会将延时放大数倍。
	* 建房间专线很大概率会出现问题，做好运维和程序层面的容错。
	* 不能依赖于程序端数据双写，要有自动同步方案。 
	* 数据永不在高延迟和较差网络质量下，考虑同步质量问题。
	* 核心业务和次要业务分而治之，甚至只考虑核心业务。
	* 异地多活监控部署、测试也要跟上。
	* 业务允许的情况下考虑用户分区，尤其是游戏、邮箱业务。
	* 控制跨机房消息体大小，越小越好。
	* 考虑使用docker容器虚拟化技术，提高动态调度能力。

* [容灾技术及建设经验介绍](https://blog.csdn.net/yoara/article/details/38013751)


### 容灾演练流程

* [《依赖治理、灰度发布、故障演练，阿里电商故障演练系统的设计与实战经验》](https://mp.weixin.qq.com/s?__biz=MjM5MDE0Mjc4MA==&mid=2650996320&idx=1&sn=0ed3be190bbee4a9277886ef88cbb2e5)
	* 常见故障画像
	* 案例：预案有效性、预案有效性、故障复现、架构容灾测试、参数调优、参数调优、故障突袭、联合演练。

### 平滑启动

* 平滑重启应用思路
1.端流量（如vip层）、2. flush 数据(如果有)、3, 重启应用

* [《JVM安全退出（如何优雅的关闭java服务）》](https://blog.csdn.net/u011001084/article/details/73480432)
推荐推出方式：System.exit，Kill SIGTERM；不推荐 kill-9；用 Runtime.addShutdownHook 注册钩子。
* [《常见Java应用如何优雅关闭》](http://ju.outofmemory.cn/entry/337235)
Java、Spring、Dubbo 优雅关闭方式。

## 数据库扩展

### 读写分离模式

* [《Mysql主从方案的实现》](https://www.cnblogs.com/houdj/p/6563771.html)
* [《搭建MySQL主从复制经典架构》](https://www.cnblogs.com/edisonchou/p/4133148.html)
* [《Haproxy+多台MySQL从服务器(Slave) 实现负载均衡》](https://blog.csdn.net/nimasike/article/details/48048341)

* [《DRBD+Heartbeat+Mysql高可用读写分离架构》](https://www.cnblogs.com/zhangsubai/p/6801764.html)
	* DRDB 进行磁盘复制，避免单点问题。

* [《MySQL Cluster 方式》](https://coderxing.gitbooks.io/architecture-evolution/di-san-pian-ff1a-bu-luo/62-ke-kuo-zhan-de-shu-ju-ku-jia-gou/621-gao-ke-yong-mysql-de-ji-zhong-fang-an/6214-mysql-cluster-fang-an.html)

### 分片模式
* [《分库分表需要考虑的问题及方案》](https://www.jianshu.com/p/32b3e91aa22c)
	* 中间件： 轻量级：sharding-jdbc、TSharding；重量级：Atlas、MyCAT、Vitess等。
	* 问题：事务、Join、迁移、扩容、ID、分页等。
	* 事务补偿：对数据进行对帐检查;基于日志进行比对;定期同标准数据来源进行同步等。
	* 分库策略：数值范围；取模；日期等。
	* 分库数量：通常 MySQL 单库 5千万条、Oracle 单库一亿条需要分库。 

* [《MySql分表和表分区详解》](https://www.2cto.com/database/201503/380348.html)
	* 分区：是MySQL内部机制，对客户端透明，数据存储在不同文件中，表面上看是同一个表。
	* 分表：物理上创建不同的表、客户端需要管理分表路由。

## 服务治理
###  服务注册与发现

* [《永不失联！如何实现微服务架构中的服务发现？》](https://blog.csdn.net/jiaolongdy/article/details/51188798)
  * 客户端服务发现模式：客户端直接查询注册表，同时自己负责负载均衡。Eureka 采用这种方式。
  * 服务器端服务发现模式：客户端通过负载均衡查询服务实例。
* [《SpringCloud服务注册中心比较:Consul vs Zookeeper vs Etcd vs Eureka》](https://blog.csdn.net/u010963948/article/details/71730165)
  * CAP支持：Consul（CA）、zookeeper（cp）、etcd（cp） 、euerka（ap）
  * 作者认为目前 Consul 对 Spring cloud 的支持比较好。

* [《基于Zookeeper的服务注册与发现》](http://mobile.51cto.com/news-502394.htm)
	* 优点：API简单、Pinterest，Airbnb 在用、多语言、通过watcher机制来实现配置PUSH，能快速响应配置变化。 

### 服务路由控制
* [《分布式服务框架学习笔记4 服务路由》](https://blog.csdn.net/xundh/article/details/59492750)
	* 原则：透明化路由
	* 负载均衡策略：随机、轮询、服务调用延迟、一致性哈希、粘滞连接
	* 本地路由优先策略：injvm(优先调用jvm内部的服务)，innative(优先使用相同物理机的服务),原则上找距离最近的服务。
	* 配置方式：统一注册表；本地配置；动态下发。

## 分布式一致

### CAP 与 BASE 理论

* [《从分布式一致性谈到CAP理论、BASE理论》](http://www.cnblogs.com/szlbm/p/5588543.html)
	* 一致性分类：强一致(立即一致)；弱一致(可在单位时间内实现一致，比如秒级)；最终一致(弱一致的一种，一定时间内最终一致)
	* CAP：一致性、可用性、分区容错性(网络故障引起)
	* BASE：Basically Available（基本可用）、Soft state（软状态）和Eventually consistent（最终一致性）
	* BASE理论的核心思想是：即使无法做到强一致性，但每个应用都可以根据自身业务特点，采用适当的方式来使系统达到最终一致性。

### 分布式锁

* [《分布式锁的几种实现方式》](http://www.hollischuang.com/archives/1716)
	* 基于数据库的分布式锁：优点：操作简单、容易理解。缺点：存在单点问题、数据库性能够开销较大、不可重入；
	* 基于缓存的分布式锁：优点：非阻塞、性能好。缺点：操作不好容易造成锁无法释放的情况。
	* Zookeeper 分布式锁：通过有序临时节点实现锁机制，自己对应的节点需要最小，则被认为是获得了锁。优点：集群可以透明解决单点问题，避免锁不被释放问题，同时锁可以重入。缺点：性能不如缓存方式，吞吐量会随着zk集群规模变大而下降。
* [《基于Zookeeper的分布式锁》](https://www.tuicool.com/articles/VZJr6fY)
	* 清楚的原理描述 + Java 代码示例。 

* [《jedisLock—redis分布式锁实现》](https://www.cnblogs.com/0201zcr/p/5942748.html)
	* 基于 setnx(set if ont exists)，有则返回false，否则返回true。并支持过期时间。

* [《Memcached 和 Redis 分布式锁方案》](https://blog.csdn.net/albertfly/article/details/77412333)
	* 利用 memcached 的 add（有别于set）操作，当key存在时，返回false。

### 分布式一致性算法

#### PAXOS
* [《分布式系列文章——Paxos算法原理与推导》](https://www.cnblogs.com/linbingdong/p/6253479.html)
* [《Paxos-->Fast Paxos-->Zookeeper分析》](https://blog.csdn.net/u010039929/article/details/70171672)
* [《【分布式】Zookeeper与Paxos》](https://www.cnblogs.com/leesf456/p/6012777.html)

#### Zab
* [《Zab：Zookeeper 中的分布式一致性协议介绍》](https://www.jianshu.com/p/fb527a64deee)

#### Raft
* [《Raft 为什么是更易理解的分布式一致性算法》](http://www.cnblogs.com/mindwind/p/5231986.html)
	* 三种角色：Leader（领袖）、Follower（群众）、Candidate（候选人）
	* 通过随机等待的方式发出投票，得票多的获胜。

#### Gossip
* [《Gossip算法》](http://blog.51cto.com/tianya23/530743)

#### 两阶段提交、多阶段提交

* [《关于分布式事务、两阶段提交协议、三阶提交协议》](http://blog.jobbole.com/95632/)

### 幂等

* [《分布式系统---幂等性设计》](https://www.cnblogs.com/wxgblogs/p/6639272.html)
	* 幂等特性的作用：该资源具备幂等性，请求方无需担心重复调用会产生错误。
	* 常见保证幂等的手段：MVCC（类似于乐观锁）、去重表(唯一索引)、悲观锁、一次性token、序列号方式。 

### 分布式一致方案
* [《分布式系统事务一致性解决方案》](http://www.infoq.com/cn/articles/solution-of-distributed-system-transaction-consistency)
* [《保证分布式系统数据一致性的6种方案》](https://weibo.com/ttarticle/p/show?id=2309403965965003062676)

### 分布式 Leader 节点选举
* [《利用zookeeper实现分布式leader节点选举》](https://blog.csdn.net/johnson_moon/article/details/78809995)

### TCC(Try/Confirm/Cancel) 柔性事务
* [《传统事务与柔性事务》](https://www.jianshu.com/p/ab1a1c6b08a1)
	* 基于BASE理论：基本可用、柔性状态、最终一致。
	* 解决方案：记录日志+补偿（正向补充或者回滚）、消息重试(要求程序要幂等)；“无锁设计”、采用乐观锁机制。

## 分布式文件系统

* [说说分布式文件存储系统-基本架构](https://zhuanlan.zhihu.com/p/27666295) ？
* [《各种分布式文件系统的比较》](https://blog.csdn.net/gatieme/article/details/44982961) ？
  * HDFS：大批量数据读写，用于高吞吐量的场景，不适合小文件。
  * FastDFS：轻量级、适合小文件。

## 唯一ID 生成

### 全局唯一ID
* [《高并发分布式系统中生成全局唯一Id汇总》](https://www.cnblogs.com/baiwa/p/5318432.html)
	* Twitter 方案（Snowflake 算法）：41位时间戳+10位机器标识（比如IP，服务器名称等）+12位序列号(本地计数器)
	* Flicker 方案：MySQL自增ID + "REPLACE INTO XXX:SELECT LAST_INSERT_ID();" 
	* UUID：缺点，无序，字符串过长，占用空间，影响检索性能。
	* MongoDB 方案：利用 ObjectId。缺点：不能自增。

* [《TDDL 在分布式下的SEQUENCE原理》](https://blog.csdn.net/hdu09075340/article/details/79103851)
	* 在数据库中创建 sequence 表，用于记录，当前已被占用的id最大值。
	* 每台客户端主机取一个id区间（比如 1000~2000）缓存在本地，并更新 sequence 表中的id最大值记录。
	* 客户端主机之间取不同的id区间，用完再取，使用乐观锁机制控制并发。

## 一致性Hash算法

* [《一致性哈希算法》](https://coderxing.gitbooks.io/architecture-evolution/di-san-pian-ff1a-bu-luo/631-yi-zhi-xing-ha-xi.html)
