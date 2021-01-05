## ZooKeeper 服务端处理请求 集群

集群处理请求分两种：事务和非事务，对于非事务，请求处理和单机类似，节点本地就可以完成数据的请求；事务请求需要提交给 Leader 处理，Leader 以投票的形式，等待半数的 Follower 的投票，完成同步后才将操作结果返回。

这里，无论什么模式、节点类型，处理客户端请求的都是 ServerCnxnFactory 的子类，默认为 NIOServerCnxnFactory，只是其内部处理调用链的 zkServer 实例不同，单机模式为 ZooKeeperServer 的实例，其他类型的节点使用 ZooKeeperServer 类的子类，其继承结构如下

```
                                    ZooKeeperServer
                                             ↑
                                    QuorumZooKeeperServer
                           ↗                 ↑                      ↖
            LearnerZooKeeperServer ReadOnlyZooKeeperServer LeaderZooKeeperServer
                  ↗                 ↖
ObserverZooKeeperServer FollowerZooKeeperServer
```

顾名思义：LeaderZooKeeperServer、FollowerZooKeeperServer、ObserverZooKeeperServer 分别处理 Leader、Follower、Observer 的请求。对于处理的详细流程不做展开，可参考单机版。

### 1.非事务请求

非事务请求以 Follower 举例。接收数据包、解析、调用请求处理链处理，过程都一样，只是不同类型的节点处理链不同，在 Follower 中。

```
// FollowerZooKeeperServer#setupRequestProcessors
@Override
protected void setupRequestProcessors() {
  RequestProcessor finalProcessor = new FinalRequestProcessor(this);
  commitProcessor = new CommitProcessor(finalProcessor,
                                        Long.toString(getServerId()), true,
                                        getZooKeeperServerListener());
  commitProcessor.start();
  firstProcessor = new FollowerRequestProcessor(this, commitProcessor);
  ((FollowerRequestProcessor) firstProcessor).start();
  syncProcessor = new SyncRequestProcessor(this,
                                           new SendAckRequestProcessor((Learner)getFollower()));
  syncProcessor.start();
}
```

在 FollowerZooKeeperServer 中调用链如下：

```
FollowerRequestProcessor -> CommitProcessor -> FinalRequestProcessor
```

还生成了一个 SyncRequestProcessor 来记录来自 leader 的 proposals。

#### 1.1.FollowerRequestProcessor

FollowerRequestProcessor 以后台线程的方式启动，当调用其 processRequest 方法时只是将请求添加到队列

```
queuedRequests.add(request);
```

FollowerRequestProcessor#run 方法核心逻辑：

```
Request request = queuedRequests.take();
if (request == Request.requestOfDeath) {
  break;
}

nextProcessor.processRequest(request);
//事务请求交给leader。
switch (request.type) {
  case OpCode.sync:
    // sync请求同步Leader数据
    zks.pendingSyncs.add(request);
    zks.getFollower().request(request);
    break;
  case OpCode.create:
  case OpCode.delete:
  case OpCode.setData:
  case OpCode.setACL:
  case OpCode.createSession:
  case OpCode.closeSession:
  case OpCode.multi:
    zks.getFollower().request(request);
    break;
}
```

- 1.所有请求交由下一处理器处理
- 对于事务请求，直接通过与 leader 的连接，将请求转发过去

#### 1.2.CommitProcessor

CommitProcessor 同样以后台线程方式启动，其 processRequest 方法也是将强求添加到队列

```
queuedRequests.add(request);
```

线程取出请求，然后处理。因为 CommitProcessor#run 内部将循环边界放在前面，所以为了便于理解代码，展示代码顺序做调整。

对于事务请求，需要等待 Leader 的 COMMIT 消息，才能进入下一个处理器，所以将事务请求赋值给 nextPending 变量，表明正在等待 Leader 的 commit 消息。

```
synchronized (this) {
  // 没有要阻塞等待的请求且请求队列不为空
  while (nextPending == null && queuedRequests.size() > 0) {
    Request request = queuedRequests.remove();
    switch (request.type) {
      case OpCode.create:
      case OpCode.delete:
      case OpCode.setData:
      case OpCode.multi:
      case OpCode.setACL:
      case OpCode.createSession:
      case OpCode.closeSession:
        // 这些都是需要阻塞的
        nextPending = request;
        break;
      case OpCode.sync:
        // Leaner的sync操作需要等待Leader的消息
        if (matchSyncs) {
          nextPending = request;
        } else {
          toProcess.add(request);
        }
        break;
      default:
        toProcess.add(request);
    }
  }
}
```

nextPending 表明当前阻塞的，正在等待 Leader 处理的 Request。

```
synchronized (this) {
  if ((queuedRequests.size() == 0 || nextPending != null)
      && committedRequests.size() == 0) {
    //当前有一个nextPending，但是没有收到Leader的commit消息，继续阻塞，等待Leader的commit消息
    wait();
    continue;
  }

  if ((queuedRequests.size() == 0 || nextPending != null)
      && committedRequests.size() > 0) {
        // 当前有一个nextPending，也有commit消息了
    Request r = committedRequests.remove();
    if (nextPending != null
        && nextPending.sessionId == r.sessionId
        && nextPending.cxid == r.cxid) {
      // nextPending与commit对应
      nextPending.hdr = r.hdr;
      nextPending.txn = r.txn;
      nextPending.zxid = r.zxid;
      toProcess.add(nextPending);
      nextPending = null;
    } else {
      toProcess.add(r);
    }
  }
}
```

toProcess 表示可以扔到下一个处理器的请求。

```
int len = toProcess.size();
for (int i = 0; i < len; i++) {
    nextProcessor.processRequest(toProcess.get(i));
}
```

#### 1.3.FinalRequestProcessor

这样 CommitProcessor 处理完成。进入下一个处理器：FinalRequestProcessor，这个处理器比较简单，无论什么模式、什么角色，请求链的最后都将由 FinalRequestProcessor 处理，该 Processor 就是根据请求，从数据库中获取数据，然后返回，这个在单机模式讲过，不再说明。

### 2.事务请求

事务请求是大头，比较复杂。

重新回到 FollowerRequestProcessor.run 方法，对于事务请求，会调用 Follower 的 request 方法，请求 Leader 处理

```
zks.getFollower().request(request);
```

request 方法来自基类 Learner，发送请求包给 Leader，请求类型为 REQUEST，到这里需要进入 Leader 节点的 LearnerHandler 处理器，在集群启动的部分说了，该类为 Leader 处理和 Leander 之间的通信。在 LearnerHandler#run 方法内部

```
case Leader.REQUEST:
    bb = ByteBuffer.wrap(qp.getData());
    sessionId = bb.getLong();
    cxid = bb.getInt();
    type = bb.getInt();
    bb = bb.slice();
    Request si;
    if(type == OpCode.sync){
        // 转为 LearnerSyncRequest request
        si = new LearnerSyncRequest(this, sessionId, cxid, type, bb, qp.getAuthinfo());
    } else {
        si = new Request(null, sessionId, cxid, type, bb, qp.getAuthinfo());
    }
    si.setOwner(this);
    leader.zk.submitRequest(si);
    break;
```

解析出 Request 后提交给 ZooKeeperServer，交由请求处理链处理。

```
firstProcessor.processRequest(si);
```

Leader 节点的处理链

```CommitProcessor → ToBeAppliedRequestProcessor → FinalRequestProcessor
PrepRequestProcessor → ProposalRequestProcessor <
```

                                                                                                SyncRequestProcessor → AckRequestProcessor

SyncRequestProcessor 在 ProposalRequestProcessor 内部

#### 2.1.PrepRequestProcessor

PrepRequestProcessor 处理器在单机模式中是第一个处理器，对于事务请求，PrepRequestProcessor 处理器会对其进行一系列预处理，诸如创建请求事务头、事务体，会话检查、ACL 检查和版本检查等。该类 PrepRequestProcessor#processRequest 方法将请求添加到 submittedRequests

```
public void processRequest(Request request) {
  submittedRequests.add(request);
}
```

线程调用从中取出 request 处理。

```
Request request = submittedRequests.take();
pRequest(request);
```

##### 2.1.1.在 PrepRequestProcessor#pRequest 方法内部，就是真正的处理流程

```
protected void pRequest(Request request) throws RequestProcessorException {
  request.hdr = null;
  request.txn = null;

  try {
    // 1 根据request.type分别处理
    switch (request.type) {
      // 事务操作
      case OpCode.create:
        CreateRequest createRequest = new CreateRequest();
        pRequest2Txn(request.type, zks.getNextZxid(), request, createRequest, true);
        break;
      case OpCode.delete:
        DeleteRequest deleteRequest = new DeleteRequest();
        pRequest2Txn(request.type, zks.getNextZxid(), request, deleteRequest, true);
        break;
      case OpCode.setData:
        SetDataRequest setDataRequest = new SetDataRequest();
        pRequest2Txn(request.type, zks.getNextZxid(), request, setDataRequest, true);
        break;
      ...
      case OpCode.sync:
      case OpCode.exists:
      case OpCode.getData:
      case OpCode.getACL:
      case OpCode.getChildren:
      case OpCode.getChildren2:
      case OpCode.ping:
      case OpCode.setWatches:
        // 非事务操作
        zks.sessionTracker.checkSession(request.sessionId, request.getOwner());
        break;
      default:
        break;
    }
  } catch (KeeperException e) {
    // 2 处理异常
    if (request.hdr != null) {
      request.hdr.setType(OpCode.error);
      request.txn = new ErrorTxn(e.code().intValue());
    }
    LOG.info("...");
    request.setException(e);
  } catch (Exception e) {
     // 处理异常。。。
  }
  request.zxid = zks.getZxid();
  // 3 调用nextProcessor处理
  nextProcessor.processRequest(request);
}
```

- 1.根据请求类型分别处理，其中事务操作和非事务操作分别处理，非事务操作仅检查 session
- 2.需要将异常信息设置到 Request 中
- 3.调用下一个 Processor 处理

##### 2.1.2.处理事务请求

事务请求会调用 PrepRequestProcessor#pRequest2Txn 方法，以 setData 类型为例

```
// 设置事务头
request.hdr = new TxnHeader(request.sessionId, request.cxid, zxid, Time.currentWallTime(), type);

case OpCode.setData:
 // 检查session
  zks.sessionTracker.checkSession(request.sessionId, request.getOwner());
  SetDataRequest setDataRequest = (SetDataRequest)record;
  if(deserialize)
    // 反序列化SetDataRequest
    ByteBufferInputStream.byteBuffer2Record(request.request, setDataRequest);
  path = setDataRequest.getPath();
 // 检测路径是否有效
  validatePath(path, request.sessionId);
 // 针对当前路径节点的未完成变更
  nodeRecord = getRecordForPath(path);
  checkACL(zks, nodeRecord.acl, ZooDefs.Perms.WRITE,
           request.authInfo);
 version = setDataRequest.getVersion();
  int currentVersion = nodeRecord.stat.getVersion();
 // 比较版本是否正确
  if (version != -1 && version != currentVersion) {
    throw new KeeperException.BadVersionException(path);
  }
  // version+1
 version = currentVersion + 1;
  request.txn = new SetDataTxn(path, setDataRequest.getData(), version);
  nodeRecord = nodeRecord.duplicate(request.hdr.getZxid());
  nodeRecord.stat.setVersion(version);
 // 添加到队列中，为保存操作的顺序性
  addChangeRecord(nodeRecord);
  break;
在PrepRequestProcessor中主要还是补充事务请求头和一些请求检测，包括session、节点路径、版本等。注意最后的addChangeRecord方法的调用

void addChangeRecord(ChangeRecord c) {
  synchronized (zks.outstandingChanges) {
    zks.outstandingChanges.add(c);
    zks.outstandingChangesForPath.put(c.path, c);
  }
}
```

将节点变更记录添加到 outstandingChanges 队列中，主要是为了保证响应的顺序性，在后续处理器中会使用到。

#### 2.3.ProposalRequestProcessor

Leader 的事务投票处理器。对于非事务请求，直接将请求流转到 CommitProcessor 处理器；而对于事务请求，除了将请求交给 CommitProcessor 处理器外，还会根据请求类型创建对应的 Proposal 提议，并发送给所有的 Follower 服务器来发起一次集群内的事务投票。同时，ProposalRequestProcessor 还会将事务请求交付给 SyncRequestProcessor 进行事务日志的记录。

```
ProposalRequestProcessor proposalProcessor = new ProposalRequestProcessor(this, commitProcessor);
proposalProcessor.initialize();
```

构造函数除了赋值外还声明了两个处理器

```
public ProposalRequestProcessor(LeaderZooKeeperServer zks,
                                RequestProcessor nextProcessor) {
  this.zks = zks;
  this.nextProcessor = nextProcessor;
  AckRequestProcessor ackProcessor = new AckRequestProcessor(zks.getLeader());
  syncProcessor = new SyncRequestProcessor(zks, ackProcessor);
}
```

同时调用 initialize 方法初始化

```
public void initialize() {
  syncProcessor.start();
}
```

这里启用了调用链的另一个分支，即 ProposalRequestProcessor→SyncRequestProcessor→AckRequestProcessor

```
public void processRequest(Request request) throws RequestProcessorException {
  // 在LearnerHandler内部，当请求类型为OpCode.sync时，请求会被转为LearnerSyncRequest
  if(request instanceof LearnerSyncRequest){
    zks.getLeader().processSync((LearnerSyncRequest)request);
  } else {
    nextProcessor.processRequest(request);
    if (request.hdr != null) {
      //事务请求需要同步并投票通过
      try {
        zks.getLeader().propose(request);
      } catch (XidRolloverException e) {
        throw new RequestProcessorException(e.getMessage(), e);
      }
      syncProcessor.processRequest(request);
    }
  }
}
```

#### 2.4.事务投票

如果当前请求是事务请求，除了将请求交给下个处理器处理，另外进入分支流程，即提议投票，也就是 Leader#propose 方法。

```
public Proposal propose(Request request) throws XidRolloverException {
  // zxid低32位满了，再进位会进到Epoch区域，所以这里要重新选举，开启新的epoch
  if ((request.zxid & 0xffffffffL) == 0xffffffffL) {
    String msg =
      "zxid lower 32 bits have rolled over, forcing re-election, and therefore new epoch start";
    shutdown(msg);
    throw new XidRolloverException(msg);
  }
  byte[] data = SerializeUtils.serializeRequest(request);
  proposalStats.setLastProposalSize(data.length);
  QuorumPacket pp = new QuorumPacket(Leader.PROPOSAL, request.zxid, data, null);

  Proposal p = new Proposal();
  p.packet = pp;
  p.request = request;
  synchronized (this) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Proposing:: " + request);
    }

    lastProposed = p.packet.getZxid();
    // Leader发出的提议，等待投票结果
    outstandingProposals.put(lastProposed, p);
    sendPacket(pp);
  }
  return p;
}
// 给每个注册的Followers发送提议
void sendPacket(QuorumPacket qp) {
  synchronized (forwardingFollowers) {
    for (LearnerHandler f : forwardingFollowers) {
      f.queuePacket(qp);
    }
  }
}
```

- 1.检查事务 id 是否已经超出限制，否则重新开启选举
- 2.外发提议，同时等待 Follower 的投票结果

在集群启动部分说过，集群选举完毕后，Follower 会与 Leader 建立连接，完成数据同步，之后便保持连接，处理之后的提议投票。在 Follower#followLeader 方法内部

```
while (this.isRunning()) {
  readPacket(qp);
  processPacket(qp);
}
```

所以这个时候 readPacket，读取到数据，Follower 收到提议

```
// Follower#processPacket
case Leader.PROPOSAL:
    TxnHeader hdr = new TxnHeader();
    Record txn = SerializeUtils.deserializeTxn(qp.getData(), hdr);

    lastQueued = hdr.getZxid();
    fzk.logRequest(hdr, txn);
    break;
FollowerZooKeeperServer#logRequest方法记录日志，并回复ACK，然后等待Leader的COMMIT消息

public void logRequest(TxnHeader hdr, Record txn) {
    Request request = new Request(null, hdr.getClientId(), hdr.getCxid(),
            hdr.getType(), null, null);
    request.hdr = hdr;
    request.txn = txn;
    request.zxid = hdr.getZxid();
    if ((request.zxid & 0xffffffffL) != 0) {
        pendingTxns.add(request);
    }
    syncProcessor.processRequest(request);
}
```

调用 syncProcessor 处理请求，这个处理器在单机模式有详细讲解过，其将请求记录到磁盘。它批量处理请求以有效地执行 IO。在将请求的日志同步到磁盘之前，该请求不会传递到下一个 RequestProcessor。对于 Follower，将请求同步到磁盘，并将请求转发给 SendAckRequestProcessor，后者将数据包发送给 Leader。SendAckRequestProcessor 是实现了 Flushable 接口，我们可以调用 flush 方法强制将数据包推送到 Leader。其他说明可以看类头注释部分。在 FollowerZooKeeperServer#setupRequestProcessors 方法也可以看到其处理链

```
// FollowerZooKeeperServer#setupRequestProcessors
@Override
protected void setupRequestProcessors() {
  ...
  syncProcessor = new SyncRequestProcessor(this,
        new SendAckRequestProcessor((Learner)getFollower()));
  syncProcessor.start();
}
```

因为在单机模式讲过，不再占用篇幅，进入到 SendAckRequestProcessor.processRequest 方法

```
// SendAckRequestProcessor#processRequest
QuorumPacket qp = new QuorumPacket(Leader.ACK, si.hdr.getZxid(), null, null);
learner.writePacket(qp, false);
```

将对应某个事务 id 的响应发回给 Leader。

再次回到 Leader，这时候 Leader 中对应的 LearnerHandler 收到了 Learner 的 ACK

```
// LearnerHandler#run
case Leader.ACK:
    syncLimitCheck.updateAck(qp.getZxid());
    leader.processAck(this.sid, qp.getZxid(), sock.getLocalSocketAddress());
    break;
Leader#processAck方法处理收到的ACK

synchronized public void processAck(long sid, long zxid, SocketAddress followerAddr) {
  if ((zxid & 0xffffffffL) == 0) {
    // NEWLEADER响应
    return;
  }

  // outstandingProposals就是等待投票的额提议，前面propose方法加入的
  if (outstandingProposals.size() == 0) {
    return;
  }
  if (lastCommitted >= zxid) {
    return;
  }
  Proposal p = outstandingProposals.get(zxid);
  if (p == null) {
    return;
  }

  p.ackSet.add(sid);
  // 过半判断
  if (self.getQuorumVerifier().containsQuorum(p.ackSet)){
    outstandingProposals.remove(zxid);
    // 添加到toBeApplied队列
    if (p.request != null) {
      toBeApplied.add(p);
    }
    // 要求Learner提交，发送COMMIT消息
    commit(zxid);
    inform(p);
    // 本地提交
    zk.commitProcessor.commit(p.request);
    if(pendingSyncs.containsKey(zxid)){
      for(LearnerSyncRequest r: pendingSyncs.remove(zxid)) {
        // 要求Learner同步，发送SYNC消息
        sendSync(r);
      }
    }
  }
}
```

根据收到的对某个提议的投票（ACK），过半数则通知 Followers 提交，同时本地提交。Follower 收到 COMMIT 消息后执行提交处理。

```
// Follower#processPacket
case Leader.COMMIT:
    fzk.commit(qp.getZxid());
    break;
```

这样，对于一个事务的投票就处理完成了。

#### 2.5.CommitProcessor

上面有讲

。。。

#### 2.6.ToBeAppliedRequestProcessor

ToBeCommitProcessor 处理器中有一个 toBeApplied 队列，存储已经被 CommitProcessor 处理过的可被提交的 Proposal，在 Leader#processAck 方法中，处理过半时添加。然后将这些请求逐个交付给 FinalRequestProcessor 处理器进行处理，完成之后，再将其从 toBeApplied 队列中除。

在 LeaderZooKeeperServer#setupRequestProcessors 方法中可以看到，这个队列是 leader 的 toBeApplied 队列

```
RequestProcessor toBeAppliedProcessor = new Leader.ToBeAppliedRequestProcessor(
        finalProcessor, getLeader().toBeApplied);
```

同时在 Leader#startForwarding 方法中可以看到

```
for (Proposal p : toBeApplied) {
  if (p.packet.getZxid() <= lastSeenZxid) {
    continue;
  }
  handler.queuePacket(p.packet);
  QuorumPacket qp = new QuorumPacket(Leader.COMMIT, p.packet.getZxid(), null, null);
  handler.queuePacket(qp);
}
```

在 leader 中，这个队列的作用其实是让新追随的 Follower 提交这些 Proposal（这个 Follower 刚刚完成数据同步）。

#### 2.7.FinalRequestProcessor

这个同上，处理最后的响应。

## 3.参考

https://www.jianshu.com/p/ab20bfc47938
