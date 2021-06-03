# [mongodb 副本集高可用架构](https://www.cnblogs.com/littleatp/p/8562842.html)

## 一、简介

Mongodb 复制集由一组 Mongod 实例（进程）组成，包含一个 Primary 节点和多个 Secondary 节点。
Mongodb Driver（客户端）的所有数据都写入 Primary，Secondary 从 Primary 同步写入的数据，以保持复制集内所有成员存储相同的数据集，实现数据的高可用。

**使用场景**

- 数据冗余，用做故障恢复使用，当发生硬件故障或者其它原因造成的宕机时，可以使用副本进行恢复。
- 读写分离，读的请求分流到副本上，减轻主节点的读压力。

一个典型的副本集架构如下图所示：

![](https://images2018.cnblogs.com/blog/242916/201803/242916-20180313214746630-2040647850.png)

## 二、副本集角色

1.

主节点（Primary）
接收所有的写请求，然后把修改同步到所有 Secondary。一个 Replica Set 只能有一个 Primary 节点，当 Primary 挂掉后，其他 Secondary 或者 Arbiter 节点会重新选举出来一个主节点。
默认读请求也是发到 Primary 节点处理的，可以通过修改客户端连接配置以支持读取 Secondary 节点。

1.

副本节点（Secondary）
与主节点保持同样的数据集。当主节点挂掉的时候，参与选主。

1.

仲裁者（Arbiter）
不保有数据，不参与选主，只进行选主投票。使用 Arbiter 可以减轻数据存储的硬件需求，Arbiter 几乎没什么大的硬件资源需求，但重要的一点是，在生产环境下它和其他数据节点不要部署在同一台机器上。

## 三、两种架构模式

1. PSS
   Primary + Secondary + Secondary 模式，通过 Primary 和 Secondary 搭建的 Replica Set
   Diagram of a 3 member replica set that consists of a primary and two secondaries.

![](https://images2018.cnblogs.com/blog/242916/201803/242916-20180313214807697-1361550290.png)

该模式下 Replica Set 节点数必须为奇数，目的是选主投票的时候要出现**大多数**才能进行选主决策。

1. PSA
   Primary + Secondary + Arbiter 模式，使用 Arbiter 搭建 Replica Set

![](https://images2018.cnblogs.com/blog/242916/201803/242916-20180313214821963-1134366100.png)

偶数个数据节点，加一个 Arbiter 构成的 Replica Set

## 四、选举机制

复制集通过 replSetInitiate 命令或 rs.initiate() 命令进行初始化。
初始化后各个成员间开始发送心跳消息，并发起 Primary 选举操作，获得大多数成员投票支持的节点，会成为 Primary，其余节点成为 Secondary。

```
config = { _id : "my_replica_set", members : [ {_id : 0, host : "rs1.example.net:27017"}, {_id : 1, host : "rs2.example.net:27017"}, {_id : 2, host : "rs3.example.net:27017"}, ] } rs.initiate(config)
```

**大多数**
假设复制集内投票成员（后续介绍）数量为 N，则大多数为 N/2 + 1，当复制集内存活成员数量不足大多数时，整个复制集将无法选举出 Primary，复制集将无法提供写服务，处于只读状态。
关于大多数的计算如下表所示
投票成员数 大多数 容忍失效数 1 1 0 2 2 0 3 2 1 4 3 1 5 3 2

Mongodb 副本集的选举基于 Bully 算法，这是一种协调者竞选算法，详细解析可以[参考这里](http://blog.nosqlfan.com/html/4139.html)
Primary 的选举受节点间心跳、优先级、最新的 oplog 时间等多种因素影响。官方文档对于[选举机制的说明](https://docs.mongodb.com/manual/core/replica-set-elections/)

**特殊角色**

- Arbiter
  Arbiter 节点只参与投票，不能被选为 Primary，并且不从 Primary 同步数据。
  当节点宕机导致复制集无法选出 Primary 时，可以给复制集添加一个 Arbiter 节点，即使有节点宕机，仍能选出 Primary。
  Arbiter 本身不存储数据，是非常轻量级的服务，当复制集成员为偶数时，最好加入一个 Arbiter 节点，以提升复制集可用性。
- Priority0
  Priority0 节点的选举优先级为 0，不会被选举为 Primary。
  比如你跨机房 A、B 部署了一个复制集，并且想指定 Primary 必须在 A 机房，这时可以将 B 机房的复制集成员 Priority 设置为 0，这样 Primary 就一定会是 A 机房的成员。
  （注意：如果这样部署，最好将大多数节点部署在 A 机房，否则网络分区时可能无法选出 Primary。）
- Vote0
  Mongodb 3.0 里，复制集成员最多 50 个，参与 Primary 选举投票的成员最多 7 个，其他成员（Vote0）的 vote 属性必须设置为 0，即不参与投票。
- Hidden
  Hidden 节点不能被选为主（Priority 为 0），并且对 Driver 不可见。
  因 Hidden 节点不会接受 Driver 的请求，可使用 Hidden 节点做一些数据备份、离线计算的任务，不会影响复制集的服务。
- Delayed
  Delayed 节点必须是 Hidden 节点，并且其数据落后与 Primary 一段时间（可配置，比如 1 个小时）。
  因 Delayed 节点的数据比 Primary 落后一段时间，当错误或者无效的数据写入 Primary 时，可通过 Delayed 节点的数据来恢复到之前的时间点。

**触发选举条件**

- 初始化一个副本集时。
- 从库不能连接到主库(默认超过 10s，可通过 heartbeatTimeoutSecs 参数控制)，由从库发起选举
- 主库放弃 primary 角色，比如执行 rs.stepdown 命令

Mongodb 副本集通过心跳检测实现自动 failover 机制，进而实现高可用
![](https://images2018.cnblogs.com/blog/242916/201803/242916-20180313214956548-1867816349.png)

## 五、数据同步

Primary 与 Secondary 之间通过 oplog 来同步数据，Primary 上的写操作完成后，会向特殊的 local.oplog.rs 特殊集合写入一条 oplog，Secondary 不断的从 Primary 取新的 oplog 并应用。
因 oplog 的数据会不断增加，local.oplog.rs 被设置成为一个 capped 集合，当容量达到配置上限时，会将最旧的数据删除掉。
另外考虑到 oplog 在 Secondary 上可能重复应用，oplog 必须具有幂等性，即重复应用也会得到相同的结果。
如下 oplog 的格式，包含 ts、h、op、ns、o 等字段。

```
{ "ts" : Timestamp(1446011584, 2), "h" : NumberLong("1687359108795812092"), "v" : 2, "op" : "i", "ns" : "test.nosql", "o" : { "_id" : ObjectId("563062c0b085733f34ab4129"), "name" : "mongodb", "score" : "100" } }
```

属性 说明 ts 操作时间，当前 timestamp + 计数器，计数器每秒都被重置 h 操作的全局唯一标识 v oplog 版本信息 op 操作类型 op.i 插入操作 op.u 更新操作 op.d 删除操作 op.c 执行命令（如 createDatabase，dropDatabase） op.n 空操作，特殊用途 ns 操作针对的集合 o 操作内容 o2 操作查询条件，仅 update 操作包含该字段。

Secondary 初次同步数据时，会先执行 init sync，从 Primary（或其他数据更新的 Secondary）同步全量数据，
然后不断通过执行 tailable cursor 从 Primary 的 local.oplog.rs 集合里查询最新的 oplog 并应用到自身。

**异常回滚**
当 Primary 宕机时，如果有数据未同步到 Secondary，当 Primary 重新加入时，如果新的 Primary 上已经发生了写操作，则旧 Primary 需要回滚部分操作，以保证数据集与新的 Primary 一致。
旧 Primary 将回滚的数据写到单独的 rollback 目录下，数据库管理员可根据需要使用 mongorestore 进行恢复

## 六、读写配置

**Read Preference**
默认情况下，复制集的所有读请求都发到 Primary，Driver 可通过设置 Read Preference 来将读请求路由到其他的节点。

- primary：默认规则，所有读请求发到 Primary；
- primaryPreferred：Primary 优先，如果 Primary 不可达，请求 Secondary；
- secondary：所有的读请求都发到 secondary；
- secondaryPreferred：Secondary 优先，当所有 Secondary 不可达时，请求 Primary；
- nearest：读请求发送到最近的可达节点上（通过 ping 探测得出最近的节点）。
  [关于 read-preference](https://docs.mongodb.com/manual/core/read-preference/)

**Write Concern**
默认情况下，Primary 完成写操作即返回，Driver 可通过设置 Write Concern (参见这里)来设置写成功的规则。

如下的 write concern 规则设置写必须在大多数节点上成功，超时时间为 5s。

```
db.products.insert( { item: "envelopes", qty : 100, type: "Clasp" }, { writeConcern: { w: majority, wtimeout: 5000 } } )
```

[关于 write-concern](https://docs.mongodb.com/manual/core/replica-set-write-concern/)

## 参考文档

**搭建高可用 mongodb 集群**
[http://www.lanceyan.com/tech/mongodb_repset2.html](http://www.lanceyan.com/tech/mongodb_repset2.html)

**深入浅出 Mongodb 复制**
[http://www.mongoing.com/archives/5200](http://www.mongoing.com/archives/5200)

mongodb 副本集原理
[https://yq.aliyun.com/articles/64?spm=0.0.0.0.9jrPm8](https://yq.aliyun.com/articles/64?spm=0.0.0.0.9jrPm8)

副本集主备切换
[https://docs.mongodb.com/manual/tutorial/force-member-to-be-primary/index.html](https://docs.mongodb.com/manual/tutorial/force-member-to-be-primary/index.html)

![](https://images.cnblogs.com/cnblogs_com/littleatp/1241412/o_qrcode_for_gh_b2cf486409a0_258.jpg)

作者： [美码师(zale)](http://www.cnblogs.com/littleatp/)

出处： [http://www.cnblogs.com/littleatp/](http://www.cnblogs.com/littleatp/), 如果喜欢我的文章，请**关注我的公众号**

本文版权归作者和博客园共有，欢迎转载，但未经作者同意必须保留此段声明，且在文章页面明显位置给出 [原文链接]() 如有问题， 可留言咨询.
分类: [5.数据库中间件](https://www.cnblogs.com/littleatp/category/1209630.html)

标签: [mongodb](https://www.cnblogs.com/littleatp/tag/mongodb/)
[好文要顶]() [关注我]() [收藏该文]() [![](https://common.cnblogs.com/images/icon_weibo_24.png)]("分享至新浪微博") [![](https://common.cnblogs.com/images/wechat.png)]("分享至微信")

[![](https://pic.cnblogs.com/face/242916/20180527184904.png)](https://home.cnblogs.com/u/littleatp/)

[美码师](https://home.cnblogs.com/u/littleatp/)
[关注 - 9](https://home.cnblogs.com/u/littleatp/followees/)
[粉丝 - 116](https://home.cnblogs.com/u/littleatp/followers/)

[+加关注]()
0

0

[«](https://www.cnblogs.com/littleatp/p/8419796.html) 上一篇： [redis 通过 pipeline 提升吞吐量](https://www.cnblogs.com/littleatp/p/8419796.html '发布于 2018-02-05 23:03')
[»](https://www.cnblogs.com/littleatp/p/8562931.html) 下一篇： [mongodb 分片扩展架构](https://www.cnblogs.com/littleatp/p/8562931.html '发布于 2018-03-13 21:58')

posted @ 2018-03-13 21:51 [美码师](https://www.cnblogs.com/littleatp/) 阅读(5101) 评论(0) [编辑](https://i.cnblogs.com/EditPosts.aspx?postid=8562842) [收藏]()
[]()

[]()

[刷新评论]()[刷新页面]()[返回顶部]()
注册用户登录后才能发表评论，请 [登录]() 或 [注册]()， [访问](https://www.cnblogs.com/) 网站首页。

[【推荐】超 50 万行 VC++源码: 大型组态工控、电力仿真 CAD 与 GIS 源码库](http://www.ucancode.com/index.htm)
[【活动】腾讯云服务器推出云产品采购季 1 核 2G 首年仅需 99 元](https://cloud.tencent.com/act/season?fromSource=gwzcw.3422970.3422970.3422970&utm_medium=cpc&utm_id=gwzcw.3422970.3422970.3422970)
[【推荐】阿里毕玄 16 篇文章，深度讲解 Java 开发、系统设计、职业发展](https://developer.aliyun.com/article/714279?utm_content=g_1000088939)
[【推荐】12 知识点+20 干货案例+110 面试题，助你拿 offer | Python 面试宝典](https://developer.aliyun.com/article/715141?utm_content=g_1000088938)
**相关博文：**
· [MongoDB 副本集学习(一)：概述和环境搭建](https://www.cnblogs.com/zhanjindong/p/3251330.html 'MongoDB副本集学习(一)：概述和环境搭建')
· [Mongodb 主从复制/ 副本集/分片集群介绍](https://www.cnblogs.com/kevingrace/p/5685486.html 'Mongodb主从复制/ 副本集/分片集群介绍')
· [mongodb 副本集架构搭建](https://www.cnblogs.com/dennisit/archive/2013/01/28/2880166.html 'mongodb副本集架构搭建')
· [搭建高可用 mongodb 集群（二）—— 副本集](https://www.cnblogs.com/lanceyan/p/3497124.html '搭建高可用mongodb集群（二）—— 副本集')
· [MongoDB（五）-- 副本集（replica Set）](https://www.cnblogs.com/xbq8080/p/7231548.html 'MongoDB（五）-- 副本集（replica Set）')
» [更多推荐...](https://recomm.cnblogs.com/blogpost/8562842)

[这 6 种编码方法，你掌握了几个？](https://developer.aliyun.com/article/718649?utm_content=g_1000088936)

**最新 IT 新闻**:
· [特斯拉超级工厂二期列入上海市重大预备项目]()
· [苹果研究配环绕触摸屏全玻璃 iPhone 可以在任意表面显示信息]()
· [猎豹移动回应被谷歌下架 APP：未通知的情况下被谷歌单方面下架]()
· [Project xCloud 落地移动终端，微软这一次要走群众路线]()
· [华为发布新一代 5G 网络解决方案，加速 5G 生态发展]()
» [更多新闻...](https://news.cnblogs.com/ 'IT 新闻') 参考：https://www.cnblogs.com/littleatp/p/8562842.html
