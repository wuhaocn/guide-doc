### [Paxos共识算法详解](https://segmentfault.com/a/1190000018844326)
 
在一个分布式系统中，由于节点故障、网络延迟等各种原因，根据CAP理论，我们只能保证**一致性（Consistency）、可用性（Availability）、分区容错性（Partition Tolerance）**中的两个。

对于一致性要求高的系统，比如银行取款机，就会选择牺牲可用性，故障时拒绝服务。MongoDB、Redis、MapReduce使用这种方案。

对于静态网站、实时性较弱的查询类数据库，会牺牲一致性，允许一段时间内不一致。简单分布式协议Gossip，数据库CouchDB、Cassandra使用这种方案。

![clipboard.png]( https://segmentfault.com/img/bVbrepW?w=828&h=212)
图1

如图1所示，一致性问题，可以根据是否存在恶意节点分类两类。无恶意节点，是指节点会丢失、重发、不响应消息，但不会篡改消息。而恶意节点可能会篡改消息。
有恶意节点的问题称为拜占庭将军问题，不在今天的讨论范围。Paxos很好地解决了无恶意节点的分布式一致性问题。

#### 目录

* [背景]()
* [Paxos类型]()
* [Basic Paxos]()
    * [角色]()
    * [算法]()
    * [三种情况]()
    * [活锁 (livelock)]()
* [Multi-Paxos]()
* [总结]()
* [Reference]()
* [版权]()
* 参考： https://segmentfault.com/a/1190000018844326
 
#### 背景

1990年，Leslie Lamport在论文《The Part-Time Parliament》中提出Paxos算法。由于论文使用故事的方式，没有使用数学证明，起初并没有得到重视。直到1998年该论文才被正式接受。后来2001年Lamport又重新组织了论文，发表了[《Paxos Made Simple》](https://lamport.azurewebsites.net/pubs/paxos-simple.pdf)。作为分布式系统领域的早期贡献者，Lamport获得了2013年图灵奖。

Paxos算法广泛应用在分布式系统中，Google Chubby的作者Mike Burrows说：“这个世界上只有一种一致性算法，那就是 Paxos（There is only one consensus protocol, and that's Paxos）”。

后来的Raft算法、是对Paxos的简化和改进，变得更加容易理解和实现。

#### Paxos类型

Paxos本来是虚构故事中的一个小岛，议会通过表决来达成共识。但是议员可能离开，信使可能走丢，或者重复传递消息。对应到分布式系统的节点故障和网络故障。

![clipboard.png]( https://segmentfault.com/img/bVbrepZ?w=585&h=222)
图2

如图2所示，假设议员要提议中午吃什么。如果有一个或者多个人同时提议，但一次只能通过一个提议，这就是Basic Paxos，是Paxos中最基础的协议。

显然Basic Paxos是不够高效的，如果将Basic Paxos并行起来，同时提出多个提议，比如中午吃什么、吃完去哪里嗨皮、谁请客等提议，议员也可以同时通过多个提议。这就是Multi-Paxos协议。

## Basic Paxos

### 角色

Paxos算法存在3种角色：Proposer、Acceptor、Learner，在实现中一个节点可以担任多个角色。

![clipboard.png](https://segmentfault.com/img/bVbreqe?w=353&h=94)
图3

* Proposer负责提出提案
* Acceptor负责对提案进行投票
* Learner获取投票结果，并帮忙传播

Learner不参与投票过程，为了简化描述，我们直接忽略掉这个角色。

### 算法

运行过程分为两个阶段，Prepare阶段和Accept阶段。

Proposer需要发出两次请求，Prepare请求和Accept请求。Acceptor根据其收集的信息，接受或者拒绝提案。

**Prepare阶段**

* Proposer选择一个提案编号n，发送Prepare(n)请求给超过半数（或更多）的Acceptor。
* Acceptor收到消息后，如果n比它之前见过的编号大，就回复这个消息，而且以后不会接受小于n的提案。另外，如果之前已经接受了小于n的提案，回复那个提案编号和内容给Proposer。

**Accept阶段**

* 当Proposer收到超过半数的回复时，就可以发送Accept(n, value)请求了。 n就是自己的提案编号，value是Acceptor回复的最大提案编号对应的value，如果Acceptor没有回复任何提案，value就是Proposer自己的提案内容。
* Acceptor收到消息后，如果n大于等于之前见过的最大编号，就记录这个提案编号和内容，回复请求表示接受。
* 当Proposer收到超过半数的回复时，说明自己的提案已经被接受。否则回到第一步重新发起提案。

完整算法如图4所示：

![clipboard.png](https://segmentfault.com/img/bVbreqm?w=679&h=401)
图4

Acceptor需要持久化存储minProposal、acceptedProposal、acceptedValue这3个值。

### 三种情况

Basic Paxos共识过程一共有三种可能的情况。下面分别进行介绍。

### 情况1：提案已接受

如图5所示。X、Y代表客户端，S1到S5是服务端，既代表Proposer又代表Acceptor。为了防止重复，Proposer提出的编号由两部分组成：

序列号.Server ID

例如S1提出的提案编号，就是1.1、2.1、3.1……

![clipboard.png]( https://segmentfault.com/img/bVbreqs?w=880&h=385)
图5 以上图片来自[Paxos lecture (Raft user study)](https://ramcloud.stanford.edu/~ongaro/userstudy/paxos.pdf)第13页

这个过程表示，S1收到客户端的提案X，于是S1作为Proposer，给S1-S3发送Prepare(3.1)请求，由于Acceptor S1-S3没有接受过任何提案，所以接受该提案。然后Proposer S1-S3发送Accept(3.1, X)请求，提案X成功被接受。

在提案X被接受后，S5收到客户端的提案Y，S5给S3-S5发送Prepare(4.5)请求。对S3来说，4.5比3.1大，且已经接受了X，它会回复这个提案 (3.1, X)。S5收到S3-S5的回复后，使用X替换自己的Y，于是发送Accept(4.5, X)请求。S3-S5接受提案。最终所有Acceptor达成一致，都拥有相同的值X。

这种情况的结果是：**新Proposer会使用已接受的提案**

### 情况2：提案未接受，新Proposer可见

![clipboard.png]( https://segmentfault.com/img/bVbreqt?w=808&h=278)
图6 以上图片来自[Paxos lecture (Raft user study)](https://ramcloud.stanford.edu/~ongaro/userstudy/paxos.pdf)第14页

如图6所示，S3接受了提案(3.1, X)，但S1-S2还没有收到请求。此时S3-S5收到Prepare(4.5)，S3会回复已经接受的提案(3.1, X)，S5将提案值Y替换成X，发送Accept(4.5, X)给S3-S5，对S3来说，编号4.5大于3.1，所以会接受这个提案。

然后S1-S2接受Accept(3.1, X)，最终所有Acceptor达成一致。

这种情况的结果是：**新Proposer会使用已提交的值，两个提案都能成功**

### 情况3：提案未接受，新Proposer不可见

![clipboard.png](https://segmentfault.com/img/bVbreqx?w=789&h=266)
图7 以上图片来自[Paxos lecture (Raft user study)](https://ramcloud.stanford.edu/~ongaro/userstudy/paxos.pdf)第15页

如图7所示，S1接受了提案(3.1, X)，S3先收到Prepare(4.5)，后收到Accept(3.1, X)，由于3.1小于4.5，会直接拒绝这个提案。所以提案X无法收到超过半数的回复，这个提案就被阻止了。提案Y可以顺利通过。

这种情况的结果是：**新Proposer使用自己的提案，旧提案被阻止**

### 活锁 (livelock)

活锁发生的几率很小，但是会严重影响性能。就是两个或者多个Proposer在Prepare阶段发生互相抢占的情形。

![clipboard.png]( https://segmentfault.com/img/bVbreqy?w=887&h=272)
图8 以上图片来自[Paxos lecture (Raft user study)](https://ramcloud.stanford.edu/~ongaro/userstudy/paxos.pdf)第16页

解决方案是Proposer失败之后给一个随机的等待时间，这样就减少同时请求的可能。

## Multi-Paxos

上一小节提到的活锁，也可以使用Multi-Paxos来解决。它会从Proposer中选出一个Leader，只由Leader提交Proposal，还可以省去Prepare阶段，减少了性能损失。当然，直接把Basic Paxos的多个Proposer的机制搬过来也是可以的，只是性能不够高。

将Basic Paxos并行之后，就可以同时处理多个提案了，因此要能存储不同的提案，也要保证提案的顺序。

Acceptor的结构如图9所示，每个方块代表一个Entry，用于存储提案值。用递增的Index来区分Entry。

![clipboard.png]( https://segmentfault.com/img/bVbreqz?w=480&h=81)
图9

Multi-Paxos需要解决几个问题，我们逐个来看。

### 1. Leader选举

一个最简单的选举方法，就是Server ID最大的当Leader。

每个Server间隔T时间向其他Server发送心跳包，如果一个Server在2T时间内没有收到来自更高ID的心跳，那么它就成为Leader。

其他Proposer，必须拒绝客户端的请求，或将请求转发给Leader。

当然，还可以使用其他更复杂的选举方法，这里不再详述。

### 2. 省略Prepare阶段

Prepare的作用是阻止旧的提案，以及检查是否有已接受的提案值。

当只有一个Leader发送提案的时候，Prepare是不会产生冲突的，可以省略Prepare阶段，这样就可以减少一半RPC请求。

Prepare请求的逻辑修改为：

* Acceptor记录一个全局的最大提案编号
* 回复最大提案编号，如果当前entry以及之后的所有entry都没有接受任何提案，回复noMoreAccepted

当Leader收到超过半数的noMoreAccepted回复，之后就不需要Prepare阶段了，只需要发送Accept请求。直到Accept被拒绝，就重新需要Prepare阶段。

### 3. 完整信息流

目前为止信息是不完整的。

* Basic Paxos只需超过半数的节点达成一致。但是在Multi-Paxos中，这种方式可能会使一些节点无法得到完整的entry信息。我们希望每个节点都拥有全部的信息。
* 只有Proposer知道一个提案是否被接受了（根据收到的回复），而Acceptor无法得知此信息。

第1个问题的解决方案很简单，就是Proposer给全部节点发送Accept请求。

第2个问题稍微复杂一些。首先，我们可以增加一个Success RPC，让Proposer显式地告诉Acceptor，哪个提案已经被接受了，这个是完全可行的，只不过还可以优化一下，减少请求次数。

我们在Accept请求中，增加一个firstUnchosenIndex参数，表示Proposer的第一个未接受的Index，这个参数隐含的意思是，对该Proposer来说，小于Index的提案都已经被接受了。因此Acceptor可以利用这个信息，把小于Index的提案标记为已接受。另外要注意的是，只能标记该Proposer的提案，因为如果发生Leader切换，不同的Proposer拥有的信息可能不同，不区分Proposer直接标记的话可能会不一致。

![clipboard.png]( https://segmentfault.com/img/bVbreqA?w=493&h=359)
图10

如图10所示，Proposer正在准备提交Index=2的Accept请求，0和1是已接受的提案，因此firstUnchosenIndex=2。当Acceptor收到请求后，比较Index，就可以将Dumplings提案标记为已接受。

由于之前提到的Leader切换的情况，仍然需要显式请求才能获得完整信息。在Acceptor回复Accept消息时，带上自己的firstUnchosenIndex。如果比Proposer的小，那么就需要发送Success(index, value)，Acceptor将收到的index标记为已接受，再回复新的firstUnchosenIndex，如此往复直到两者的index相等。

## 总结

Paxos是分布式一致性问题中的重要共识算法。这篇文章分别介绍了最基础的Basic Paxos，和能够并行的Multi-Paxos。

在Basic Paxos中，介绍了3种基本角色Proposer、Acceptor、Learner，以及提案时可能发生的3种基本情况。在Multi-Paxos中，介绍了3个需要解决的问题：Leader选举、Prepare省略、完整信息流。

在下一篇文章中，我们将实现一个简单的demo来验证这个算法，实现过程将会涉及到更多的细节。

## Reference

[分布式一致性与共识算法](https://draveness.me/consensus)

[Paxos 算法与 Raft 算法](https://yeasy.gitbooks.io/blockchain_guide/content/distribute_system/paxos.html)

[Paxos](https://en.wikipedia.org/wiki/Paxos_%28computer_science%29)

[Paxos Made Simple](https://lamport.azurewebsites.net/pubs/paxos-simple.pdf)

[Paxos lecture (Raft user study)](https://ramcloud.stanford.edu/~ongaro/userstudy/paxos.pdf)

[YouTube | Paxos lecture (Raft user study)](https://www.youtube.com/watch?v=JEpsBg0AO6o)


