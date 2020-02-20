##【Zookeeper】源码分析之服务器（四）之FollowerZooKeeperServer
### 一、前言

前面分析了LeaderZooKeeperServer，接着分析FollowerZooKeeperServer。

### 二、FollowerZooKeeperServer源码分析

#### 2.1 类的继承关系　　
```
public class FollowerZooKeeperServer extends LearnerZooKeeperServer {}
```
说明：其继承LearnerZooKeeperServer抽象类，角色为Follower。其请求处理链为FollowerRequestProcessor -> CommitProcessor -> FinalRequestProcessor。

#### 2.2 类的属性　　

```
public class FollowerZooKeeperServer extends LearnerZooKeeperServer {
private static final Logger LOG =
    LoggerFactory.getLogger(FollowerZooKeeperServer.class);
// 提交请求处理器
CommitProcessor commitProcessor;

// 同步请求处理器
SyncRequestProcessor syncProcessor;

/*
 * Pending sync requests
 */
// 待同步请求
ConcurrentLinkedQueue<Request> pendingSyncs;

// 待处理的事务请求
LinkedBlockingQueue<Request> pendingTxns = new LinkedBlockingQueue<Request>();
}
```
说明：FollowerZooKeeperServer中维护着提交请求处理器和同步请求处理器，并且维护了所有待同步请求队列和待处理的事务请求队列。

#### 2.3 类的构造函数　　

```
FollowerZooKeeperServer(FileTxnSnapLog logFactory,QuorumPeer self,
        DataTreeBuilder treeBuilder, ZKDatabase zkDb) throws IOException {
    super(logFactory, self.tickTime, self.minSessionTimeout,
            self.maxSessionTimeout, treeBuilder, zkDb, self);
    // 初始化pendingSyncs
    this.pendingSyncs = new ConcurrentLinkedQueue<Request>();
}
```
说明：其首先调用父类的构造函数，然后初始化pendingSyncs为空队列。

#### 2.4 核心函数分析

##### 1. logRequest函数　　

```
public void logRequest(TxnHeader hdr, Record txn) {
    // 创建请求
    Request request = new Request(null, hdr.getClientId(), hdr.getCxid(),
            hdr.getType(), null, null);
    // 赋值请求头、事务体、zxid
    request.hdr = hdr;
    request.txn = txn;
    request.zxid = hdr.getZxid();
    if ((request.zxid & 0xffffffffL) != 0) { // zxid不为0，表示本服务器已经处理过请求
        // 则需要将该请求放入pendingTxns中
        pendingTxns.add(request);
    }
    // 使用SyncRequestProcessor处理请求(其会将请求放在队列中，异步进行处理)
    syncProcessor.processRequest(request);
}
```
说明：该函数将请求进行记录（放入到对应的队列中），等待处理。

##### 2. commit函数　

```
public void commit(long zxid) {
    if (pendingTxns.size() == 0) { // 没有还在等待处理的事务
        LOG.warn("Committing " + Long.toHexString(zxid)
                + " without seeing txn");
        return;
    }
    // 队首元素的zxid
    long firstElementZxid = pendingTxns.element().zxid;
    if (firstElementZxid != zxid) { // 如果队首元素的zxid不等于需要提交的zxid，则退出程序
        LOG.error("Committing zxid 0x" + Long.toHexString(zxid)
                + " but next pending txn 0x"
                + Long.toHexString(firstElementZxid));
        System.exit(12);
    }
    // 从待处理事务请求队列中移除队首请求
    Request request = pendingTxns.remove();
    // 提交该请求
    commitProcessor.commit(request);
}
```
说明：该函数会提交zxid对应的请求（pendingTxns的队首元素），其首先会判断队首请求对应的zxid是否为传入的zxid，然后再进行移除和提交（放在committedRequests队列中）。

##### 3. sync函数　　

```
synchronized public void sync(){
    if(pendingSyncs.size() ==0){ // 没有需要同步的请求
        LOG.warn("Not expecting a sync.");
        return;
    }
    // 从待同步队列中移除队首请求
    Request r = pendingSyncs.remove();
    // 提交该请求
    commitProcessor.commit(r);
}
```
说明：该函数会将待同步请求队列中的元素进行提交，也是将该请求放入committedRequests队列中。

### 三、总结

本篇学习了FollowerZooKeeperServer的源码，其核心是对待同步请求和待处理事务请求交由不同的请求处理器进行处理。也谢谢各位园友的观看~

### 参考
https://www.cnblogs.com/leesf456/p/6517058.html

