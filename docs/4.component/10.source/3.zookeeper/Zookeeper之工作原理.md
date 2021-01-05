## Zookeeper 工作原理概念

### 1、Zookeeper 的角色

- 领导者（leader），负责进行投票的发起和决议，更新系统状态
- 学习者（learner），包括跟随者（follower）和观察者（observer），follower 用于接受客户端请求并想客户端返回结果，在选主过程中参与投票
- Observer 可以接受客户端连接，将写请求转发给 leader，但 observer 不参加投票过程，只同步 leader 的状态，observer 的目的是为了扩展系统，提高读取速度
- 客户端（client），请求发起方

![](https://images2015.cnblogs.com/blog/183233/201603/183233-20160316222520584-1877673765.jpg)
![](https://images2015.cnblogs.com/blog/183233/201603/183233-20160316222444771-1363762533.png)

- Zookeeper 的核心是原子广播，这个机制保证了各个 Server 之间的同步。实现这个机制的协议叫做 Zab 协议。
  Zab 协议有两种模式，它们分别是恢复模式（选主）和广播模式（同步）。当服务启动或者在领导者崩溃后，Zab 就进入了恢复模式，当领导者被选举出来，
  且大多数 Server 完成了和 leader 的状态同步以后，恢复模式就结束了。状态同步保证了 leader 和 Server 具有相同的系统状态。

- 为了保证事务的顺序一致性，zookeeper 采用了递增的事务 id 号（zxid）来标识事务。
  所有的提议（proposal）都在被提出的时候加上了 zxid。实现中 zxid 是一个 64 位的数字，它高 32 位是 epoch 用来标识 leader 关系是否改变，每次一个 leader 被选出来，
  它都会有一个新的 epoch，标识当前属于那个 leader 的统治时期。低 32 位用于递增计数。
- 每个 Server 在工作过程中有三种状态：
  - LOOKING：当前 Server 不知道 leader 是谁，正在搜寻
  - LEADING：当前 Server 即为选举出来的 leader
  - FOLLOWING：leader 已经选举出来，当前 Server 与之同步

其他文档：http://www.cnblogs.com/lpshou/archive/2013/06/14/3136738.html

### 2、Zookeeper 的读写机制

- Zookeeper 是一个由多个 server 组成的集群
- 一个 leader，多个 follower
- 每个 server 保存一份数据副本
- 全局数据一致
- 分布式读写
- 更新请求转发，由 leader 实施

### 3、Zookeeper 的保证

- 更新请求顺序进行，来自同一个 client 的更新请求按其发送顺序依次执行
- 数据更新原子性，一次数据更新要么成功，要么失败
- 全局唯一数据视图，client 无论连接到哪个 server，数据视图都是一致的
- 实时性，在一定事件范围内，client 能读到最新数据

### 4、Zookeeper 节点数据操作流程

![](https://images2015.cnblogs.com/blog/183233/201603/183233-20160316223234865-1124736424.png)

注：

- 1.在 Client 向 Follwer 发出一个写的请求

- 2.Follwer 把请求发送给 Leader

- 3.Leader 接收到以后开始发起投票并通知 Follwer 进行投票

- 4.Follwer 把投票结果发送给 Leader

- 5.Leader 将结果汇总后如果需要写入，则开始写入同时把写入操作通知给 Leader，然后 commit;

- 6.Follwer 把请求结果返回给 Client

#### 4.1.Follower 主要有四个功能：

- 1. 向 Leader 发送请求（PING 消息、REQUEST 消息、ACK 消息、REVALIDATE 消息）；
- 2 .接收 Leader 消息并进行处理；
- 3 .接收 Client 的请求，如果为写请求，发送给 Leader 进行投票；
- 4 .返回 Client 结果。

#### 4.2.Follower 的消息循环处理如下几种来自 Leader 的消息：

- 1 .PING 消息： 心跳消息；
- 2 .PROPOSAL 消息：Leader 发起的提案，要求 Follower 投票；
- 3 .COMMIT 消息：服务器端最新一次提案的信息；
- 4 .UPTODATE 消息：表明同步完成；
- 5 .REVALIDATE 消息：根据 Leader 的 REVALIDATE 结果，关闭待 revalidate 的 session 还是允许其接受消息；
- 6 .SYNC 消息：返回 SYNC 结果到客户端，这个消息最初由客户端发起，用来强制得到最新的更新。

### 5、Zookeeper leader 选举

- 半数通过
  – 3 台机器 挂一台 2>3/2
  – 4 台机器 挂 2 台 2！>4/2
- A 提案说，我要选自己，B 你同意吗？C 你同意吗？B 说，我同意选 A；C 说，我同意选 A。(注意，这里超过半数了，其实在现实世界选举已经成功了。但是计算机世界是很严格，另外要理解算法，要继续模拟下去。)
- 接着 B 提案说，我要选自己，A 你同意吗；A 说，我已经超半数同意当选，你的提案无效；C 说，A 已经超半数同意当选，B 提案无效。
- 接着 C 提案说，我要选自己，A 你同意吗；A 说，我已经超半数同意当选，你的提案无效；B 说，A 已经超半数同意当选，C 的提案无效。
- 选举已经产生了 Leader，后面的都是 follower，只能服从 Leader 的命令。而且这里还有个小细节，就是其实谁先启动谁当头。

![](https://images2015.cnblogs.com/blog/183233/201603/183233-20160316224650521-63353773.png)

![](https://images2015.cnblogs.com/blog/183233/201603/183233-20160316224702381-344312695.png)

### 6、zxid

- znode 节点的状态信息中包含 czxid, 那么什么是 zxid 呢?
- ZooKeeper 状态的每一次改变, 都对应着一个递增的 Transaction id, 该 id 称为 zxid. 由于 zxid 的递增性质, 如果 zxid1 小于 zxid2, 那么 zxid1 肯定先于 zxid2 发生.

创建任意节点, 或者更新任意节点的数据, 或者删除任意节点, 都会导致 Zookeeper 状态发生改变, 从而导致 zxid 的值增加.

### 7、Zookeeper 工作原理

- Zookeeper 的核心是原子广播，这个机制保证了各个 server 之间的同步。实现这个机制的协议叫做 Zab 协议。Zab 协议有两种模式，它们分别是恢复模式和广播模式。

当服务启动或者在领导者崩溃后，Zab 就进入了恢复模式，当领导者被选举出来，且大多数 server 的完成了和 leader 的状态同步以后，恢复模式就结束了。

状态同步保证了 leader 和 server 具有相同的系统状态

- 一旦 leader 已经和多数的 follower 进行了状态同步后，他就可以开始广播消息了，即进入广播状态。这时候当一个 server 加入 zookeeper 服务中，它会在恢复模式下启动，

发现 leader，并和 leader 进行状态同步。待到同步结束，它也参与消息广播。Zookeeper 服务一直维持在 Broadcast 状态，直到 leader 崩溃了或者 leader 失去了大部分

的 followers 支持。

- 广播模式需要保证 proposal 被按顺序处理，因此 zk 采用了递增的事务 id 号(zxid)来保证。所有的提议(proposal)都在被提出的时候加上了 zxid。

实现中 zxid 是一个 64 为的数字，它高 32 位是 epoch 用来标识 leader 关系是否改变，每次一个 leader 被选出来，它都会有一个新的 epoch。低 32 位是个递增计数。

- 当 leader 崩溃或者 leader 失去大多数的 follower，这时候 zk 进入恢复模式，恢复模式需要重新选举出一个新的 leader，让所有的 server 都恢复到一个正确的状态。

- 每个 Server 启动以后都询问其它的 Server 它要投票给谁。
- 对于其他 server 的询问，server 每次根据自己的状态都回复自己推荐的 leader 的 id 和上一次处理事务的 zxid（系统启动时每个 server 都会推荐自己）
- 收到所有 Server 回复以后，就计算出 zxid 最大的哪个 Server，并将这个 Server 相关信息设置成下一次要投票的 Server。
- 计算这过程中获得票数最多的的 sever 为获胜者，如果获胜者的票数超过半数，则改 server 被选为 leader。否则，继续这个过程，直到 leader 被选举出来

- leader 就会开始等待 server 连接
- Follower 连接 leader，将最大的 zxid 发送给 leader
- Leader 根据 follower 的 zxid 确定同步点
- 完成同步后通知 follower 已经成为 uptodate 状态
- Follower 收到 uptodate 消息后，又可以重新接受 client 的请求进行服务了

### 8、数据一致性与 paxos 算法

- 据说 Paxos 算法的难理解与算法的知名度一样令人敬仰，所以我们先看如何保持数据的一致性，这里有个原则就是：
- 在一个分布式数据库系统中，如果各节点的初始状态一致，每个节点都执行相同的操作序列，那么他们最后能得到一个一致的状态。
- Paxos 算法解决的什么问题呢，解决的就是保证每个节点执行相同的操作序列。好吧，这还不简单，master 维护一个
  全局写队列，所有写操作都必须 放入这个队列编号，那么无论我们写多少个节点，只要写操作是按编号来的，就能保证一
  　致性。没错，就是这样，可是如果 master 挂了呢。
- Paxos 算法通过投票来对写操作进行全局编号，同一时刻，只有一个写操作被批准，同时并发的写操作要去争取选票，
  　只有获得过半数选票的写操作才会被 批准（所以永远只会有一个写操作得到批准），其他的写操作竞争失败只好再发起一
  　轮投票，就这样，在日复一日年复一年的投票中，所有写操作都被严格编号排 序。编号严格递增，当一个节点接受了一个
  　编号为 100 的写操作，之后又接受到编号为 99 的写操作（因为网络延迟等很多不可预见原因），它马上能意识到自己 数据
  　不一致了，自动停止对外服务并重启同步过程。任何一个节点挂掉都不会影响整个集群的数据一致性（总 2n+1 台，除非挂掉大于 n 台）。
  　 总结
- Zookeeper 作为 Hadoop 项目中的一个子项目，是 Hadoop 集群管理的一个必不可少的模块，它主要用来控制集群中的数据，

如它管理 Hadoop 集群中的 NameNode，还有 Hbase 中 Master Election、Server 之间状态同步等。\

关于 Paxos 算法可以查看文章 Zookeeper 全解析——Paxos 作为灵魂

推荐书籍：《从 Paxos 到 Zookeeper 分布式一致性原理与实践》

### 9、Observer

- Zookeeper 需保证高可用和强一致性；
- 为了支持更多的客户端，需要增加更多 Server；
- Server 增多，投票阶段延迟增大，影响性能；
- 权衡伸缩性和高吞吐率，引入 Observer
- Observer 不参与投票；
- Observers 接受客户端的连接，并将写请求转发给 leader 节点；
- 加入更多 Observer 节点，提高伸缩性，同时不影响吞吐率

### 10、 为什么 zookeeper 集群的数目，一般为奇数个？

•Leader 选举算法采用了 Paxos 协议；
•Paxos 核心思想：当多数 Server 写成功，则任务数据写成功如果有 3 个 Server，则两个写成功即可；如果有 4 或 5 个 Server，则三个写成功即可。
•Server 数目一般为奇数（3、5、7）如果有 3 个 Server，则最多允许 1 个 Server 挂掉；如果有 4 个 Server，则同样最多允许 1 个 Server 挂掉由此，

我们看出 3 台服务器和 4 台服务器的的容灾能力是一样的，所以为了节省服务器资源，一般我们采用奇数个数，作为服务器部署个数。

### 11、Zookeeper 的数据模型

- 层次化的目录结构，命名符合常规文件系统规范
- 每个节点在 zookeeper 中叫做 znode,并且其有一个唯一的路径标识
- 节点 Znode 可以包含数据和子节点，但是 EPHEMERAL 类型的节点不能有子节点
- Znode 中的数据可以有多个版本，比如某一个路径下存有多个数据版本，那么查询这个路径下的数据就需要带上版本
- 客户端应用可以在节点上设置监视器
- 节点不支持部分读写，而是一次性完整读写

### 12、Zookeeper 的节点

- Znode 有两种类型，短暂的（ephemeral）和持久的（persistent）
- Znode 的类型在创建时确定并且之后不能再修改
- 短暂 znode 的客户端会话结束时，zookeeper 会将该短暂 znode 删除，短暂 znode 不可以有子节点
- 持久 znode 不依赖于客户端会话，只有当客户端明确要删除该持久 znode 时才会被删除
- Znode 有四种形式的目录节点
- PERSISTENT（持久的）
- EPHEMERAL(暂时的)
- PERSISTENT_SEQUENTIAL（持久化顺序编号目录节点）
- EPHEMERAL_SEQUENTIAL（暂时化顺序编号目录节点）
