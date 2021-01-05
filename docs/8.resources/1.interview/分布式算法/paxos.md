[登录免广告](https://segmentfault.com/user/login)

![](https://sponsor.segmentfault.com/lg.php?bannerid=43&campaignid=1&zoneid=2&loc=https%3A%2F%2Fsegmentfault.com%2Fa%2F1190000018844326&referer=https%3A%2F%2Fwww.baidu.com%2Flink%3Furl%3D1uoIyXLmFSARTn7kMme2lFJvYfxkahWa1AZvg0gjVK2ISge4y8DofXyRdzp3z4yVwvYtBJOkORRWQZqDxkLXkK%26wd%3D%26eqid%3Dfb32b017001397d0000000035d41acbc&cb=b17e9b7226)

1. [专栏]()
1. 文章详情
   [![哈希表演艺术家](https://cdn.segmentfault.com/v-5d2ffc9a/global/img/user-64.png)]()

[**哈希表演艺术家**]() ![](https://cdn.segmentfault.com/v-5d2ffc9a/global/img/rp.svg)3 关注作者 2019-04-12 发布

# [Paxos 共识算法详解]()

[]()

- [分布式系统]()
- [区块链]()
- [后端]()
  402 次阅读 · 读完需要 21 分钟

1
在一个分布式系统中，由于节点故障、网络延迟等各种原因，根据 CAP 理论，我们只能保证**一致性（Consistency）、可用性（Availability）、分区容错性（Partition Tolerance）**中的两个。

对于一致性要求高的系统，比如银行取款机，就会选择牺牲可用性，故障时拒绝服务。MongoDB、Redis、MapReduce 使用这种方案。

对于静态网站、实时性较弱的查询类数据库，会牺牲一致性，允许一段时间内不一致。简单分布式协议 Gossip，数据库 CouchDB、Cassandra 使用这种方案。

![clipboard.png]("clipboard.png")
图 1

如图 1 所示，一致性问题，可以根据是否存在恶意节点分类两类。无恶意节点，是指节点会丢失、重发、不响应消息，但不会篡改消息。而恶意节点可能会篡改消息。有恶意节点的问题称为拜占庭将军问题，不在今天的讨论范围。Paxos 很好地解决了无恶意节点的分布式一致性问题。

## 背景

1990 年，Leslie Lamport 在论文《The Part-Time Parliament》中提出 Paxos 算法。由于论文使用故事的方式，没有使用数学证明，起初并没有得到重视。直到 1998 年该论文才被正式接受。后来 2001 年 Lamport 又重新组织了论文，发表了[《Paxos Made Simple》](https://lamport.azurewebsites.net/pubs/paxos-simple.pdf)。作为分布式系统领域的早期贡献者，Lamport 获得了 2013 年图灵奖。

Paxos 算法广泛应用在分布式系统中，Google Chubby 的作者 Mike Burrows 说：“这个世界上只有一种一致性算法，那就是 Paxos（There is only one consensus protocol, and that's Paxos）”。

后来的 Raft 算法、是对 Paxos 的简化和改进，变得更加容易理解和实现。

## Paxos 类型

Paxos 本来是虚构故事中的一个小岛，议会通过表决来达成共识。但是议员可能离开，信使可能走丢，或者重复传递消息。对应到分布式系统的节点故障和网络故障。

![clipboard.png]("clipboard.png")
图 2

如图 2 所示，假设议员要提议中午吃什么。如果有一个或者多个人同时提议，但一次只能通过一个提议，这就是 Basic Paxos，是 Paxos 中最基础的协议。

显然 Basic Paxos 是不够高效的，如果将 Basic Paxos 并行起来，同时提出多个提议，比如中午吃什么、吃完去哪里嗨皮、谁请客等提议，议员也可以同时通过多个提议。这就是 Multi-Paxos 协议。

## Basic Paxos

### 角色

Paxos 算法存在 3 种角色：Proposer、Acceptor、Learner，在实现中一个节点可以担任多个角色。

![clipboard.png]("clipboard.png")
图 3

- Proposer 负责提出提案
- Acceptor 负责对提案进行投票
- Learner 获取投票结果，并帮忙传播

Learner 不参与投票过程，为了简化描述，我们直接忽略掉这个角色。

### 算法

运行过程分为两个阶段，Prepare 阶段和 Accept 阶段。

Proposer 需要发出两次请求，Prepare 请求和 Accept 请求。Acceptor 根据其收集的信息，接受或者拒绝提案。

**Prepare 阶段**

- Proposer 选择一个提案编号 n，发送 Prepare(n)请求给超过半数（或更多）的 Acceptor。
- Acceptor 收到消息后，如果 n 比它之前见过的编号大，就回复这个消息，而且以后不会接受小于 n 的提案。另外，如果之前已经接受了小于 n 的提案，回复那个提案编号和内容给 Proposer。

**Accept 阶段**

- 当 Proposer 收到超过半数的回复时，就可以发送 Accept(n, value)请求了。 n 就是自己的提案编号，value 是 Acceptor 回复的最大提案编号对应的 value，如果 Acceptor 没有回复任何提案，value 就是 Proposer 自己的提案内容。
- Acceptor 收到消息后，如果 n 大于等于之前见过的最大编号，就记录这个提案编号和内容，回复请求表示接受。
- 当 Proposer 收到超过半数的回复时，说明自己的提案已经被接受。否则回到第一步重新发起提案。

完整算法如图 4 所示：

![clipboard.png]("clipboard.png")
图 4

Acceptor 需要持久化存储 minProposal、acceptedProposal、acceptedValue 这 3 个值。

### 三种情况

Basic Paxos 共识过程一共有三种可能的情况。下面分别进行介绍。

### 情况 1：提案已接受

如图 5 所示。X、Y 代表客户端，S1 到 S5 是服务端，既代表 Proposer 又代表 Acceptor。为了防止重复，Proposer 提出的编号由两部分组成：

序列号.Server ID

例如 S1 提出的提案编号，就是 1.1、2.1、3.1……

![clipboard.png]("clipboard.png")
图 5 以上图片来自[Paxos lecture (Raft user study)](https://ramcloud.stanford.edu/~ongaro/userstudy/paxos.pdf)第 13 页

这个过程表示，S1 收到客户端的提案 X，于是 S1 作为 Proposer，给 S1-S3 发送 Prepare(3.1)请求，由于 Acceptor S1-S3 没有接受过任何提案，所以接受该提案。然后 Proposer S1-S3 发送 Accept(3.1, X)请求，提案 X 成功被接受。

在提案 X 被接受后，S5 收到客户端的提案 Y，S5 给 S3-S5 发送 Prepare(4.5)请求。对 S3 来说，4.5 比 3.1 大，且已经接受了 X，它会回复这个提案 (3.1, X)。S5 收到 S3-S5 的回复后，使用 X 替换自己的 Y，于是发送 Accept(4.5, X)请求。S3-S5 接受提案。最终所有 Acceptor 达成一致，都拥有相同的值 X。

这种情况的结果是：**新 Proposer 会使用已接受的提案**

### 情况 2：提案未接受，新 Proposer 可见

![clipboard.png]("clipboard.png")
图 6 以上图片来自[Paxos lecture (Raft user study)](https://ramcloud.stanford.edu/~ongaro/userstudy/paxos.pdf)第 14 页

如图 6 所示，S3 接受了提案(3.1, X)，但 S1-S2 还没有收到请求。此时 S3-S5 收到 Prepare(4.5)，S3 会回复已经接受的提案(3.1, X)，S5 将提案值 Y 替换成 X，发送 Accept(4.5, X)给 S3-S5，对 S3 来说，编号 4.5 大于 3.1，所以会接受这个提案。

然后 S1-S2 接受 Accept(3.1, X)，最终所有 Acceptor 达成一致。

这种情况的结果是：**新 Proposer 会使用已提交的值，两个提案都能成功**

### 情况 3：提案未接受，新 Proposer 不可见

![clipboard.png]("clipboard.png")
图 7 以上图片来自[Paxos lecture (Raft user study)](https://ramcloud.stanford.edu/~ongaro/userstudy/paxos.pdf)第 15 页

如图 7 所示，S1 接受了提案(3.1, X)，S3 先收到 Prepare(4.5)，后收到 Accept(3.1, X)，由于 3.1 小于 4.5，会直接拒绝这个提案。所以提案 X 无法收到超过半数的回复，这个提案就被阻止了。提案 Y 可以顺利通过。

这种情况的结果是：**新 Proposer 使用自己的提案，旧提案被阻止**

### 活锁 (livelock)

活锁发生的几率很小，但是会严重影响性能。就是两个或者多个 Proposer 在 Prepare 阶段发生互相抢占的情形。

![clipboard.png]("clipboard.png")
图 8 以上图片来自[Paxos lecture (Raft user study)](https://ramcloud.stanford.edu/~ongaro/userstudy/paxos.pdf)第 16 页

解决方案是 Proposer 失败之后给一个随机的等待时间，这样就减少同时请求的可能。

## Multi-Paxos

上一小节提到的活锁，也可以使用 Multi-Paxos 来解决。它会从 Proposer 中选出一个 Leader，只由 Leader 提交 Proposal，还可以省去 Prepare 阶段，减少了性能损失。当然，直接把 Basic Paxos 的多个 Proposer 的机制搬过来也是可以的，只是性能不够高。

将 Basic Paxos 并行之后，就可以同时处理多个提案了，因此要能存储不同的提案，也要保证提案的顺序。

Acceptor 的结构如图 9 所示，每个方块代表一个 Entry，用于存储提案值。用递增的 Index 来区分 Entry。

![clipboard.png]("clipboard.png")
图 9

Multi-Paxos 需要解决几个问题，我们逐个来看。

### 1. Leader 选举

一个最简单的选举方法，就是 Server ID 最大的当 Leader。

每个 Server 间隔 T 时间向其他 Server 发送心跳包，如果一个 Server 在 2T 时间内没有收到来自更高 ID 的心跳，那么它就成为 Leader。

其他 Proposer，必须拒绝客户端的请求，或将请求转发给 Leader。

当然，还可以使用其他更复杂的选举方法，这里不再详述。

### 2. 省略 Prepare 阶段

Prepare 的作用是阻止旧的提案，以及检查是否有已接受的提案值。

当只有一个 Leader 发送提案的时候，Prepare 是不会产生冲突的，可以省略 Prepare 阶段，这样就可以减少一半 RPC 请求。

Prepare 请求的逻辑修改为：

- Acceptor 记录一个全局的最大提案编号
- 回复最大提案编号，如果当前 entry 以及之后的所有 entry 都没有接受任何提案，回复 noMoreAccepted

当 Leader 收到超过半数的 noMoreAccepted 回复，之后就不需要 Prepare 阶段了，只需要发送 Accept 请求。直到 Accept 被拒绝，就重新需要 Prepare 阶段。

### 3. 完整信息流

目前为止信息是不完整的。

- Basic Paxos 只需超过半数的节点达成一致。但是在 Multi-Paxos 中，这种方式可能会使一些节点无法得到完整的 entry 信息。我们希望每个节点都拥有全部的信息。
- 只有 Proposer 知道一个提案是否被接受了（根据收到的回复），而 Acceptor 无法得知此信息。

第 1 个问题的解决方案很简单，就是 Proposer 给全部节点发送 Accept 请求。

第 2 个问题稍微复杂一些。首先，我们可以增加一个 Success RPC，让 Proposer 显式地告诉 Acceptor，哪个提案已经被接受了，这个是完全可行的，只不过还可以优化一下，减少请求次数。

我们在 Accept 请求中，增加一个 firstUnchosenIndex 参数，表示 Proposer 的第一个未接受的 Index，这个参数隐含的意思是，对该 Proposer 来说，小于 Index 的提案都已经被接受了。因此 Acceptor 可以利用这个信息，把小于 Index 的提案标记为已接受。另外要注意的是，只能标记该 Proposer 的提案，因为如果发生 Leader 切换，不同的 Proposer 拥有的信息可能不同，不区分 Proposer 直接标记的话可能会不一致。

![clipboard.png]("clipboard.png")
图 10

如图 10 所示，Proposer 正在准备提交 Index=2 的 Accept 请求，0 和 1 是已接受的提案，因此 firstUnchosenIndex=2。当 Acceptor 收到请求后，比较 Index，就可以将 Dumplings 提案标记为已接受。

由于之前提到的 Leader 切换的情况，仍然需要显式请求才能获得完整信息。在 Acceptor 回复 Accept 消息时，带上自己的 firstUnchosenIndex。如果比 Proposer 的小，那么就需要发送 Success(index, value)，Acceptor 将收到的 index 标记为已接受，再回复新的 firstUnchosenIndex，如此往复直到两者的 index 相等。

## 总结

Paxos 是分布式一致性问题中的重要共识算法。这篇文章分别介绍了最基础的 Basic Paxos，和能够并行的 Multi-Paxos。

在 Basic Paxos 中，介绍了 3 种基本角色 Proposer、Acceptor、Learner，以及提案时可能发生的 3 种基本情况。在 Multi-Paxos 中，介绍了 3 个需要解决的问题：Leader 选举、Prepare 省略、完整信息流。

在下一篇文章中，我们将实现一个简单的 demo 来验证这个算法，实现过程将会涉及到更多的细节。

## Reference

[分布式一致性与共识算法](https://draveness.me/consensus)

[Paxos 算法与 Raft 算法](https://yeasy.gitbooks.io/blockchain_guide/content/distribute_system/paxos.html)

[Paxos](https://en.wikipedia.org/wiki/Paxos_%28computer_science%29)

[Paxos Made Simple](https://lamport.azurewebsites.net/pubs/paxos-simple.pdf)

[Paxos lecture (Raft user study)](https://ramcloud.stanford.edu/~ongaro/userstudy/paxos.pdf)

[YouTube | Paxos lecture (Raft user study)](https://www.youtube.com/watch?v=JEpsBg0AO6o)

## 版权

本作品采用[CC BY 4.0 许可协议](https://creativecommons.org/licenses/by/4.0/deed.zh)，转载时请注明链接。

- [![](https://cdn.segmentfault.com/v-5d2ffc9a/global/img/creativecommons-cc.svg)](https://creativecommons.org/licenses/by-nc-nd/4.0/)
- []()

- [举报]()
- [新浪微博]()
- [微信]()
- [Twitter]()
- [Facebook]()
  [**•••**]()
  赞 | 1 收藏 | 1

![](https://sponsor.segmentfault.com/lg.php?bannerid=2&campaignid=1&zoneid=3&loc=https%3A%2F%2Fsegmentfault.com%2Fa%2F1190000018844326&referer=https%3A%2F%2Fwww.baidu.com%2Flink%3Furl%3D1uoIyXLmFSARTn7kMme2lFJvYfxkahWa1AZvg0gjVK2ISge4y8DofXyRdzp3z4yVwvYtBJOkORRWQZqDxkLXkK%26wd%3D%26eqid%3Dfb32b017001397d0000000035d41acbc&cb=9fa8b6b5e6)

### 你可能感兴趣的

- [【go 共识算法】-DPOS](http://segmentfault.com/a/1190000016537965 '【go共识算法】-DPOS')jincheng828[golang]()
- [【go 共识算法】-PBFT](http://segmentfault.com/a/1190000016639075 '【go共识算法】-PBFT')jincheng828[golang]()
- [RSA 算法详解](http://segmentfault.com/a/1190000013128367 'RSA算法详解')damonare[前端]()[https]()
- [【python】魔法算法详解](http://segmentfault.com/a/1190000009653403 '【python】魔法算法详解')ChristmasBoy[python]()
- [从 Paxos 到 NOPaxos 重新理解分布式共识算法（consensus）](http://segmentfault.com/a/1190000013463908 '从Paxos到NOPaxos 重新理解分布式共识算法（consensus）')chasel[一致性]()[区块链]()
- [个人文章分类整理](http://segmentfault.com/a/1190000008219057 '个人文章分类整理')samsara0511[apache]()[vue.js]()[html]()[javascript]()[css]()
- [【go 共识算法】-BFT](http://segmentfault.com/a/1190000016540665 '【go共识算法】-BFT')jincheng828[golang]()
- [【go 共识算法】-POS](http://segmentfault.com/a/1190000016535794 '【go共识算法】-POS')jincheng828[golang]()

**评论**

[默认排序]() [时间排序]()

载入中...

[显示更多评论]()
![](https://cdn.segmentfault.com/v-5d2ffc9a/global/img/user-128.png)

发布评论

![Planets](https://static.segmentfault.com/sponsor/20190731.png)

[想在上方展示你的广告？]()

### 推广链接

[**Java 微服务实践课**
上千人学习过的微服务实栈课](https://sponsor.segmentfault.com/ck.php?oaparams=2__bannerid=57__zoneid=7__cb=d6c065ed52__oadest=https%3A%2F%2Fsegmentfault.com%2Fls%2F1650000011387052)

![](https://sponsor.segmentfault.com/lg.php?bannerid=57&campaignid=14&zoneid=7&loc=https%3A%2F%2Fsegmentfault.com%2Fa%2F1190000018844326&referer=https%3A%2F%2Fwww.baidu.com%2Flink%3Furl%3D1uoIyXLmFSARTn7kMme2lFJvYfxkahWa1AZvg0gjVK2ISge4y8DofXyRdzp3z4yVwvYtBJOkORRWQZqDxkLXkK%26wd%3D%26eqid%3Dfb32b017001397d0000000035d41acbc&cb=d6c065ed52)
[**大神的 PHP 进阶之路**
亿级 PV 项目的架构梳理，性能提升实战](https://sponsor.segmentfault.com/ck.php?oaparams=2__bannerid=56__zoneid=9__cb=6a8f882b46__oadest=https%3A%2F%2Fsegmentfault.com%2Fls%2F1650000011318558)

![](https://sponsor.segmentfault.com/lg.php?bannerid=56&campaignid=14&zoneid=9&loc=https%3A%2F%2Fsegmentfault.com%2Fa%2F1190000018844326&referer=https%3A%2F%2Fwww.baidu.com%2Flink%3Furl%3D1uoIyXLmFSARTn7kMme2lFJvYfxkahWa1AZvg0gjVK2ISge4y8DofXyRdzp3z4yVwvYtBJOkORRWQZqDxkLXkK%26wd%3D%26eqid%3Dfb32b017001397d0000000035d41acbc&cb=6a8f882b46)
![](https://sponsor.segmentfault.com/lg.php?bannerid=0&campaignid=0&zoneid=10&loc=https%3A%2F%2Fsegmentfault.com%2Fa%2F1190000018844326&referer=https%3A%2F%2Fwww.baidu.com%2Flink%3Furl%3D1uoIyXLmFSARTn7kMme2lFJvYfxkahWa1AZvg0gjVK2ISge4y8DofXyRdzp3z4yVwvYtBJOkORRWQZqDxkLXkK%26wd%3D%26eqid%3Dfb32b017001397d0000000035d41acbc&cb=6b8c6af095)

![](https://sponsor.segmentfault.com/lg.php?bannerid=0&campaignid=0&zoneid=15&loc=https%3A%2F%2Fsegmentfault.com%2Fa%2F1190000018844326&referer=https%3A%2F%2Fwww.baidu.com%2Flink%3Furl%3D1uoIyXLmFSARTn7kMme2lFJvYfxkahWa1AZvg0gjVK2ISge4y8DofXyRdzp3z4yVwvYtBJOkORRWQZqDxkLXkK%26wd%3D%26eqid%3Dfb32b017001397d0000000035d41acbc&cb=cd65b6b37f)
![](https://sponsor.segmentfault.com/lg.php?bannerid=0&campaignid=0&zoneid=16&loc=https%3A%2F%2Fsegmentfault.com%2Fa%2F1190000018844326&referer=https%3A%2F%2Fwww.baidu.com%2Flink%3Furl%3D1uoIyXLmFSARTn7kMme2lFJvYfxkahWa1AZvg0gjVK2ISge4y8DofXyRdzp3z4yVwvYtBJOkORRWQZqDxkLXkK%26wd%3D%26eqid%3Dfb32b017001397d0000000035d41acbc&cb=b7753af5ce)
![](https://sponsor.segmentfault.com/lg.php?bannerid=1&campaignid=1&zoneid=1&loc=https%3A%2F%2Fsegmentfault.com%2Fa%2F1190000018844326&referer=https%3A%2F%2Fwww.baidu.com%2Flink%3Furl%3D1uoIyXLmFSARTn7kMme2lFJvYfxkahWa1AZvg0gjVK2ISge4y8DofXyRdzp3z4yVwvYtBJOkORRWQZqDxkLXkK%26wd%3D%26eqid%3Dfb32b017001397d0000000035d41acbc&cb=f22fc5ee4f)

目录

- [背景]()
- [Paxos 类型]()
- [Basic Paxos]()
- - [角色]()
- [算法]()
- [三种情况]()
- [活锁 (livelock)]()
- [Multi-Paxos]()
- [总结]()
- [Reference]()
- [版权]()
  参考： https://segmentfault.com/a/1190000018844326
