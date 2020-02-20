##【Zookeeper】源码分析之服务器（五）之ObserverZooKeeperServer
### 一、前言

前面分析了FollowerZooKeeperServer，接着分析ObserverZooKeeperServer。

### 二、ObserverZooKeeperServer源码分析

#### 2.1 类的继承关系　
```
public class ObserverZooKeeperServer extends LearnerZooKeeperServer {}
```
说明：ObserverZooKeeperServer也继承了LearnerZooKeeperServer抽象类，角色为Observer，
其请求处理链为ObserverRequestProcessor -> CommitProcessor -> FinalRequestProcessor。
可能会存在SyncRequestProcessor。

#### 2.2 类的属性　　

```
public class ObserverZooKeeperServer extends LearnerZooKeeperServer {
    // 日志
    private static final Logger LOG =
        LoggerFactory.getLogger(ObserverZooKeeperServer.class);        
    
    /**
     * Enable since request processor for writing txnlog to disk and
     * take periodic snapshot. Default is ON.
     */
    // 同步处理器是否可用
    private boolean syncRequestProcessorEnabled = this.self.getSyncEnabled();
    
    /*
     * Request processors
     */
    // 提交请求处理器
    private CommitProcessor commitProcessor;
    // 同步请求处理器
    private SyncRequestProcessor syncProcessor;
    
    /*
     * Pending sync requests
     */
    // 待同步请求队列
    ConcurrentLinkedQueue<Request> pendingSyncs = 
        new ConcurrentLinkedQueue<Request>();
}
```
说明：该类维护了提交请求处理器和同步请求处理器，同时维护一个待同步请求的队列，是否使用同步请求处理器要根据其标志而定。

#### 2.3 类的构造函数　　

```
ObserverZooKeeperServer(FileTxnSnapLog logFactory, QuorumPeer self,
        DataTreeBuilder treeBuilder, ZKDatabase zkDb) throws IOException {
    // 父类构造函数
    super(logFactory, self.tickTime, self.minSessionTimeout,
            self.maxSessionTimeout, treeBuilder, zkDb, self);
    LOG.info("syncEnabled =" + syncRequestProcessorEnabled);
}
```
说明：其会调用父类构造函数进行初始化操作，同时可确定此时同步请求处理器是否可用。

#### 2.4 核心函数分析

##### 1. commitRequest函数　　

```
public void commitRequest(Request request) {     
    if (syncRequestProcessorEnabled) { // 同步处理器可用
        // Write to txnlog and take periodic snapshot
        // 使用同步处理器处理请求
        syncProcessor.processRequest(request);
    }
    // 提交请求
    commitProcessor.commit(request);        
}
```
说明：若同步处理器可用，则使用同步处理器进行处理（放入同步处理器的queuedRequests队列中），然后提交请求（放入提交请求处理器的committedRequests队列中）。

##### 2. sync函数　

```
synchronized public void sync(){
    if(pendingSyncs.size() ==0){ // 没有未完成的同步请求
        LOG.warn("Not expecting a sync.");
        return;
    }
    // 移除队首元素        
    Request r = pendingSyncs.remove();
    // 提交请求
    commitProcessor.commit(r);
}
```
说明：若还有未完成的同步请求，则移除该请求，并且进行提交。

### 三、总结

本篇博文分析了ObserverZooKeeperServer的源码，其核心也是请求处理链对于请求的处理。至此，ZooKeeper的源码分析就告一段落了，
其中之分析了部分源码，还有很多的没有分析到，之后在使用过程中遇到则再进行分析，也谢谢各位园友的观看~

### 参考
https://www.cnblogs.com/leesf456/p/6517945.html