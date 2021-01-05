### Raft 算法

#### 一、更加直观的 Raft 算法

Raft 适用于一个管理日志一致性的协议，相比于 Paxos 协议 Raft 更易于理解和去实现它。
为了提高理解性，Raft 将一致性算法分为了几个部分，包括领导选取（leader selection）、日志复制（log replication）、安全（safety），并且使用了更强的一致性来减少了必须需要考虑的状态。

#### 1.解决什么问题

分布式存储系统通常通过维护多个副本来提高系统的 availability，带来的代价就是分布式存储系统的核心问题之一：维护多个副本的一致性。

Raft 协议基于复制状态机（replicated state machine），即一组 server 从相同的初始状态起，按相同的顺序执行相同的命令，最终会达到一直的状态，一组 server 记录相同的操作日志，并以相同的顺序应用到状态机。

![replicated state machine](https://img.alicdn.com/tfs/TB1fmKeiWmWBuNjy1XaXXXCbXXa-879-306.jpg 'replicated state machine')

Raft 有一个明确的场景，就是管理复制日志的一致性。

如图，每台机器保存一份日志，日志来自于客户端的请求，包含一系列的命令，状态机会按顺序执行这些命令。
一致性算法管理来自客户端状态命令的复制日志，保证状态机处理的日志中的命令的顺序都是一致的，因此会得到相同的执行结果。

![state machine](https://img.alicdn.com/tfs/TB1PTN7i25TBuNjSspmXXaDRVXa-605-319.jpg 'state machine')

#### 2.Raft 概览

先看一段动画演示，[Understandable Distributed Consensus](http://thesecretlivesofdata.com/raft/) 。

相比 Paxos，Raft 算法理解起来直观的很。

Raft 算法将 Server 划分为 3 种状态，或者也可以称作角色：

- Leader

负责 Client 交互和 log 复制，同一时刻系统中最多存在 1 个。

- Follower

被动响应请求 RPC，从不主动发起请求 RPC。

- Candidate

一种临时的角色，只存在于 leader 的选举阶段，某个节点想要变成 leader，那么就发起投票请求，同时自己变成 candidate。如果选举成功，则变为 candidate，否则退回为 follower

状态或者说角色的流转如下：

![state](https://img.alicdn.com/tfs/TB1UEuni.R1BeNjy0FmXXb0wVXa-1152-480.png 'state')

在 Raft 中，问题分解为：领导选取、日志复制、安全和成员变化。

复制状态机通过复制日志来实现：

- 日志：每台机器保存一份日志，日志来自于客户端的请求，包含一系列的命令
- 状态机：状态机会按顺序执行这些命令
- 一致性模型：分布式环境下，保证多机的日志是一致的，这样回放到状态机中的状态是一致的

###

## 二、Raft 算法流程

Raft 中使用心跳机制来出发 leader 选举。当服务器启动的时候，服务器成为 follower。只要 follower 从 leader 或者 candidate 收到有效的 RPCs 就会保持 follower 状态。如果 follower 在一段时间内（该段时间被称为 election timeout）没有收到消息，则它会假设当前没有可用的 leader，然后开启选举新 leader 的流程。

### 1.Term

Term 的概念类比中国历史上的朝代更替，Raft 算法将时间划分成为任意不同长度的任期（term）。

任期用连续的数字进行表示。每一个任期的开始都是一次选举（election），一个或多个候选人会试图成为领导人。如果一个候选人赢得了选举，它就会在该任期的剩余时间担任领导人。在某些情况下，选票会被瓜分，有可能没有选出领导人，那么，将会开始另一个任期，并且立刻开始下一次选举。Raft 算法保证在给定的一个任期最多只有一个领导人。

### 2.RPC

Raft 算法中服务器节点之间通信使用远程过程调用（RPCs），并且基本的一致性算法只需要两种类型的 RPCs，为了在服务器之间传输快照增加了第三种 RPC。

RPC 有三种：

- RequestVote RPC：候选人在选举期间发起
- AppendEntries RPC：领导人发起的一种心跳机制，复制日志也在该命令中完成
- InstallSnapshot RPC: 领导者使用该 RPC 来发送快照给太落后的追随者

### 3.选举流程

（1）follower 增加当前的 term，转变为 candidate。
（2）candidate 投票给自己，并发送 RequestVote RPC 给集群中的其他服务器。
（3）收到 RequestVote 的服务器，在同一 term 中只会按照先到先得投票给至多一个 candidate。且只会投票给 log 至少和自身一样新的 candidate。

![](https://images2018.cnblogs.com/blog/524341/201804/524341-20180416200723272-896605592.png)

![](https://images2018.cnblogs.com/blog/524341/201804/524341-20180416200738632-44217418.png)

candidate 节点保持（2）的状态，直到下面三种情况中的一种发生。

- 该节点赢得选举。即收到大多数的节点的投票。则其转变为 leader 状态。
- 另一个服务器成为了 leader。即收到了 leader 的合法心跳包（term 值等于或大于当前自身 term 值）。则其转变为 follower 状态。
- 一段时间后依然没有胜者。该种情况下会开启新一轮的选举。

Raft 中使用随机选举超时时间来解决当票数相同无法确定 leader 的问题。

### 4.日志复制

日志复制（Log Replication）主要作用是用于保证节点的一致性，这阶段所做的操作也是为了保证一致性与高可用性。

当 Leader 选举出来后便开始负责客户端的请求，所有事务（更新操作）请求都必须先经过 Leader 处理，日志复制（Log Replication）就是为了保证执行相同的操作序列所做的工作。

在 Raft 中当接收到客户端的日志（事务请求）后先把该日志追加到本地的 Log 中，然后通过 heartbeat 把该 Entry 同步给其他 Follower，Follower 接收到日志后记录日志然后向 Leader 发送 ACK，当 Leader 收到大多数（n/2+1）Follower 的 ACK 信息后将该日志设置为已提交并追加到本地磁盘中，通知客户端并在下个 heartbeat 中 Leader 将通知所有的 Follower 将该日志存储在自己的本地磁盘中。

## 三、Raft 和 Paxos 的工程应用

Raft 算法的论文相比 Paxos 直观很多，更容易在工程上实现。

可以看到 Raft 算法的实现已经非常多了，[https://raft.github.io//#implementations](https://raft.github.io/#implementations)

### 1.Raft 的应用

这里用 ETCD 来关注 Raft 的应用，ETCD 目标是构建一个高可用的分布式键值（key-value）数据库，基于 Go 语言实现。
Etcd 主要用途是共享配置和服务发现，实现一致性使用了 Raft 算法。
更多 Etcd 的应用可以查看文档：[https://coreos.com/etcd/docs/latest/](https://coreos.com/etcd/docs/latest/)

### 2.Zookeeper 中的 Paxos

Zookeeper 使用了一种修改后的 Paxos 协议。

在 Zookeeper 中，始终分为两种场景:

- Leader activation

在这个场景里，系统中缺乏 Leader(primary)，通过一个类似 paxos 协议的过程完成 Leader 选举。

- Active messaging
  在 这个场景里，Leader 接收客户端发送的更新操作，以一种类似两阶段提交的过程在各个 follower (secondary)节点上进行更新操作。

在 Leader activation 场景中完成 leader 选举及数据同步后，系统转入 Active messaging 场景，在 active messaging 中 leader 异常后，系统转入 Leader activation 场景。

无论在那种场景，Zookeeper 依赖于一个全局版本号:zxid。zxid 由(epoch, count)两部分组成， 高位的 epoch 部分是选举编号，每次提议进行新的 leader 选举时 epoch 都会增加，低位的 count 部分 是 leader 为每个更新操作决定的序号。可以认为，一个 leader 对应一个唯一的 epoch，每个 leader 任期内产生的更新操作对应一个唯一的有序的 count，从而从全局的视野，一个 zxid 代表了一个更新操作的全局序号(版本号)。

Zookeeper 通过 zxid 将两个场景阶段较好的结合起来，且能保证全局的强一致性。由于同一时刻只有一个 zookeeper 节点能获得超过半数的 follower，所以同一时刻最多只存在唯一的 leader;每个 leader 利用 FIFO 以 zxid 顺序更新各个 follower，只有成功完成前一个更新操作的才会进行下一个更新操作，在同一个 leader 任期内，数据在全局满足 quorum 约束的强一致，即读超过半数的节点 一定可以读到最新已提交的数据;每个成功的更新操作都至少被超过半数的节点确认，使得新选举 的 leader 一定可以包括最新的已成功提交的数据。

### 3.如何解决 split brain 问题

分布式协议一个著名问题就是 split brain 问题。

简单说，就是比如当你的 cluster 里面有两个结点，它们都知道在这个 cluster 里需要选举出一个 master。那么当它们两之间的通信完全没有问题的时候，就会达成共识，选出其中一个作为 master。但是如果它们之间的通信出了问题，那么两个结点都会觉得现在没有 master，所以每个都把自己选举成 master。于是 cluster 里面就会有两个 master。

区块链的分叉其实类似分布式系统的 split brain。

一般来说，Zookeeper 会默认设置：

- zookeeper cluster 的节点数目必须是奇数。
- zookeeper 集群中必须超过半数节点(Majority)可用，整个集群才能对外可用。

Majority 就是一种 Qunroms 的方式来支持 Leader 选举，可以防止 split brain 出现。奇数个节点可以在相同容错能力的情况下节省资源。

## 四、从 CAP 的角度理解几种不同的算法

### 1.两阶段提交协议

两阶段提交系统具有完全的 C，很糟糕的 A，很糟糕的 P。
首先，两阶段提交协议保证了副本间是完全一致的，这也是协议的设计目的。再者，协议在一个节点出现异常时，就无法更新数据，其服务可用性较低。最后，一旦协调者与参与者之间网络分化，无法提供服务。

### 2.Paxos 和 Raft 算法

Paxos 协议和 Raft 算法都是强一致性协议。Paxos 只有两种情况下服务不可用:一是超过半数的 Proposer 异常，二是出现活锁。前者可以通过增加 Proposer 的个数来 降低由于 Proposer 异常影响服务的概率，后者本身发生的概率就极低。最后，只要能与超过半数的 Proposer 通信就可以完成协议流程，协议本身具有较好的容忍网络分区的能力。

参考
[Raft 一致性算法](https://blog.csdn.net/cszhouwei/article/details/38374603)
[Raft 一致性算法论文译文](http://www.infoq.com/cn/articles/raft-paper)
