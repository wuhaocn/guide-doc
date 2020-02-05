## ZooKeeper服务端处理请求 集群


集群处理请求分两种：事务和非事务，对于非事务，请求处理和单机类似，节点本地就可以完成数据的请求；事务请求需要提交给Leader处理，Leader以投票的形式，等待半数的Follower的投票，完成同步后才将操作结果返回。

这里，无论什么模式、节点类型，处理客户端请求的都是ServerCnxnFactory的子类，默认为NIOServerCnxnFactory，只是其内部处理调用链的zkServer实例不同，单机模式为ZooKeeperServer的实例，其他类型的节点使用ZooKeeperServer类的子类，其继承结构如下

```
                                    ZooKeeperServer
                                             ↑
                                    QuorumZooKeeperServer
                           ↗                 ↑                      ↖
            LearnerZooKeeperServer ReadOnlyZooKeeperServer LeaderZooKeeperServer
                  ↗                 ↖
ObserverZooKeeperServer FollowerZooKeeperServer
```

顾名思义：LeaderZooKeeperServer、FollowerZooKeeperServer、ObserverZooKeeperServer分别处理Leader、Follower、Observer的请求。对于处理的详细流程不做展开，可参考单机版。

### 1.非事务请求

非事务请求以Follower举例。接收数据包、解析、调用请求处理链处理，过程都一样，只是不同类型的节点处理链不同，在Follower中。
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

在FollowerZooKeeperServer中调用链如下：
```
FollowerRequestProcessor -> CommitProcessor -> FinalRequestProcessor
```
还生成了一个SyncRequestProcessor来记录来自leader的proposals。

#### 1.1.FollowerRequestProcessor
FollowerRequestProcessor以后台线程的方式启动，当调用其processRequest方法时只是将请求添加到队列
```
queuedRequests.add(request);
```

FollowerRequestProcessor#run方法核心逻辑：

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
* 1.所有请求交由下一处理器处理
* 对于事务请求，直接通过与leader的连接，将请求转发过去
#### 1.2.CommitProcessor
CommitProcessor同样以后台线程方式启动，其processRequest方法也是将强求添加到队列
```
queuedRequests.add(request);
```

线程取出请求，然后处理。因为CommitProcessor#run内部将循环边界放在前面，所以为了便于理解代码，展示代码顺序做调整。

对于事务请求，需要等待Leader的COMMIT消息，才能进入下一个处理器，所以将事务请求赋值给nextPending变量，表明正在等待Leader的commit消息。
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
nextPending表明当前阻塞的，正在等待Leader处理的Request。
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
toProcess表示可以扔到下一个处理器的请求。
```
int len = toProcess.size();
for (int i = 0; i < len; i++) {
    nextProcessor.processRequest(toProcess.get(i));
}
```
#### 1.3.FinalRequestProcessor
这样CommitProcessor处理完成。进入下一个处理器：FinalRequestProcessor，这个处理器比较简单，无论什么模式、什么角色，请求链的最后都将由FinalRequestProcessor处理，该Processor就是根据请求，从数据库中获取数据，然后返回，这个在单机模式讲过，不再说明。

### 2.事务请求
事务请求是大头，比较复杂。

重新回到FollowerRequestProcessor.run方法，对于事务请求，会调用Follower的request方法，请求Leader处理
```
zks.getFollower().request(request);
```
request方法来自基类Learner，发送请求包给Leader，请求类型为REQUEST，到这里需要进入Leader节点的LearnerHandler处理器，在集群启动的部分说了，该类为Leader处理和Leander之间的通信。在LearnerHandler#run方法内部
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
解析出Request后提交给ZooKeeperServer，交由请求处理链处理。
```
firstProcessor.processRequest(si);
```
Leader节点的处理链

```                                                                                                CommitProcessor → ToBeAppliedRequestProcessor → FinalRequestProcessor
PrepRequestProcessor → ProposalRequestProcessor <
```
                                                                                                SyncRequestProcessor → AckRequestProcessor
SyncRequestProcessor在ProposalRequestProcessor内部

#### 2.1.PrepRequestProcessor
PrepRequestProcessor处理器在单机模式中是第一个处理器，对于事务请求，PrepRequestProcessor处理器会对其进行一系列预处理，诸如创建请求事务头、事务体，会话检查、ACL检查和版本检查等。该类PrepRequestProcessor#processRequest方法将请求添加到submittedRequests
```
public void processRequest(Request request) {
  submittedRequests.add(request);
}
```
线程调用从中取出request处理。
```
Request request = submittedRequests.take();
pRequest(request);
```
##### 2.1.1.在PrepRequestProcessor#pRequest方法内部，就是真正的处理流程
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
* 1.根据请求类型分别处理，其中事务操作和非事务操作分别处理，非事务操作仅检查session
* 2.需要将异常信息设置到Request中
* 3.调用下一个Processor处理
##### 2.1.2.处理事务请求
事务请求会调用PrepRequestProcessor#pRequest2Txn方法，以setData类型为例
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
将节点变更记录添加到outstandingChanges队列中，主要是为了保证响应的顺序性，在后续处理器中会使用到。

#### 2.3.ProposalRequestProcessor
Leader的事务投票处理器。对于非事务请求，直接将请求流转到CommitProcessor处理器；而对于事务请求，除了将请求交给CommitProcessor处理器外，还会根据请求类型创建对应的Proposal提议，并发送给所有的Follower服务器来发起一次集群内的事务投票。同时，ProposalRequestProcessor 还会将事务请求交付给SyncRequestProcessor进行事务日志的记录。
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
同时调用initialize方法初始化
```
public void initialize() {
  syncProcessor.start();
}
```
这里启用了调用链的另一个分支，即ProposalRequestProcessor→SyncRequestProcessor→AckRequestProcessor

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
如果当前请求是事务请求，除了将请求交给下个处理器处理，另外进入分支流程，即提议投票，也就是Leader#propose方法。
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
* 1.检查事务id是否已经超出限制，否则重新开启选举
* 2.外发提议，同时等待Follower的投票结果

在集群启动部分说过，集群选举完毕后，Follower会与Leader建立连接，完成数据同步，之后便保持连接，处理之后的提议投票。在Follower#followLeader方法内部
```
while (this.isRunning()) {
  readPacket(qp);
  processPacket(qp);
}
```
所以这个时候readPacket，读取到数据，Follower收到提议
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
调用syncProcessor处理请求，这个处理器在单机模式有详细讲解过，其将请求记录到磁盘。它批量处理请求以有效地执行IO。在将请求的日志同步到磁盘之前，该请求不会传递到下一个RequestProcessor。对于Follower，将请求同步到磁盘，并将请求转发给SendAckRequestProcessor，后者将数据包发送给Leader。SendAckRequestProcessor是实现了Flushable接口，我们可以调用flush方法强制将数据包推送到Leader。其他说明可以看类头注释部分。在FollowerZooKeeperServer#setupRequestProcessors方法也可以看到其处理链
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
因为在单机模式讲过，不再占用篇幅，进入到SendAckRequestProcessor.processRequest方法

```
// SendAckRequestProcessor#processRequest
QuorumPacket qp = new QuorumPacket(Leader.ACK, si.hdr.getZxid(), null, null);
learner.writePacket(qp, false);
```
将对应某个事务id的响应发回给Leader。

再次回到Leader，这时候Leader中对应的LearnerHandler收到了Learner的ACK
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
根据收到的对某个提议的投票（ACK），过半数则通知Followers提交，同时本地提交。Follower收到COMMIT消息后执行提交处理。

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
ToBeCommitProcessor处理器中有一个toBeApplied队列，存储已经被CommitProcessor处理过的可被提交的Proposal，在Leader#processAck方法中，处理过半时添加。然后将这些请求逐个交付给FinalRequestProcessor处理器进行处理，完成之后，再将其从toBeApplied队列中除。

在LeaderZooKeeperServer#setupRequestProcessors方法中可以看到，这个队列是leader的toBeApplied队列
```
RequestProcessor toBeAppliedProcessor = new Leader.ToBeAppliedRequestProcessor(
        finalProcessor, getLeader().toBeApplied);
```
同时在Leader#startForwarding方法中可以看到
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
在leader中，这个队列的作用其实是让新追随的Follower提交这些Proposal（这个Follower刚刚完成数据同步）。

#### 2.7.FinalRequestProcessor
这个同上，处理最后的响应。

## 3.参考
https://www.jianshu.com/p/ab20bfc47938

