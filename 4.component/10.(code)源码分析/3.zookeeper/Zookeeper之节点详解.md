## Zookeeper节点操作详解

### 1.概述
基于Quorum模式，Zookeeper版本为3.5.4。

DataNode是Zookeeper数据节点树DataTree中的最小单元，每个DataNode上都可以保存数据等信息，同时还可以挂载子节点，DataNode之间的层级关系就像文件系统的目录结构一样，Zookeeper将全部的数据存储在内存中以此来提高服务器吞吐量。

#### 1.1.DataNode类型目前已扩展到7种：
* PERSISTENT：永久节点，不会随着会话的结束而自动删除。
* PERSISTENT_SEQUENTIAL：带单调递增序号的永久节点，不会随着会话的结束而自动删除。
* EPHEMERAL：临时节点，会随着会话的结束而自动删除。
* EPHEMERAL_SEQUENTIAL：带单调递增序号的临时节点，会随着会话的结束而自动删除。
* CONTAINER：容器节点，用于Leader、Lock等特殊用途，当容器节点不存在任何子节点时，容器将成为服务器在将来某个时候删除的候选节点。
* PERSISTENT_WITH_TTL：带TTL（time-to-live，存活时间）的永久节点，节点在TTL时间之内没有得到更新并且没有孩子节点，就会被自动删除。
* PERSISTENT_SEQUENTIAL_WITH_TTL：带TTL（time-to-live，存活时间）和单调递增序号的永久节点，节点在TTL时间之内没有得到更新并且没有孩子节点，就会被自动删除。

#### 1.2.核心类：
* org.apache.zookeeper.server.DataNode：数据节点
* org.apache.zookeeper.server.DataTree：数据节点树，管理着DataNode，负责触发watch通知。
* org.apache.zookeeper.server.ZKDatabase：管理sessions，DataTree，Committed logs，在Zookeeper启动时从磁盘读取快照和提交日志以后创建。
* org.apache.zookeeper.server.RequestProcessor：用于处理所有的客户端请求，Zookeeper采用调用链的设计，最后一个请求处理者为org.apache.zookeeper.server.FinalRequestProcessor，FinalRequestProcessor管理着ZKDatabase。
* org.apache.zookeeper.server.ZooKeeperServer：ZK服务核心类，控制所有的节点操作流程，跟踪会话等。每种角色创建的该实例不同：单机模式为ZooKeeperServer实例。Quorum模式中Leader角色为LearnerZooKeeperServer，Follower角色为FollowerZooKeeperServer，Observer角色为ObserverZooKeeperServer。
* org.apache.zookeeper.server.ServerCnxnFactory：管理着所有的客户端连接。主要有两种实现：org.apache.zookeeper.server.NIOServerCnxnFactory和org.apache.zookeeper.server.NettyServerCnxnFactory。

#### 1.3.引用关系：
ServerCnxnFactory 持有ZookeeperServer，ZookeeperServer持有ZKDatabase和RequestProcessor链表，ZKDatabase持有DataTree，DataTree持有DataNode集合。

### 2.数据节点DataNode
![](https://upload-images.jianshu.io/upload_images/14781314-94b7917fc1bc5f03.png)

* byte data[]：节点数据的字节数组。
* Long acl：Datatree的ReferenceCountedACLCache中使用Map<Long, List<ACL>>缓存着所有DataNode的权限列表，这里的acl就是Map<Long, List<ACL>>的Key。
* StatPersisted stat：节点状态信息。
* Set<String> children：数据节点的子节点列表，这是只是节点Path的字符串路径，并且是相对路径。
#### 2.1.节点树DataTree
```
public class DataTree {
    private static final Logger LOG = LoggerFactory.getLogger(DataTree.class);

    /**
     * This hashtable provides a fast lookup to the datanodes. The tree is the
     * source of truth and is where all the locking occurs
     */
    private final ConcurrentHashMap<String, DataNode> nodes =
        new ConcurrentHashMap<String, DataNode>();

    private IWatchManager dataWatches;

    private IWatchManager childWatches;

    /** cached total size of paths and data for all DataNodes */
    private final AtomicLong nodeDataSize = new AtomicLong(0);

    /** the root of zookeeper tree */
    private static final String rootZookeeper = "/";

    /** the zookeeper nodes that acts as the management and status node **/
    private static final String procZookeeper = Quotas.procZookeeper;

    /** this will be the string thats stored as a child of root */
    private static final String procChildZookeeper = procZookeeper.substring(1);

    /**
     * the zookeeper quota node that acts as the quota management node for
     * zookeeper
     */
    private static final String quotaZookeeper = Quotas.quotaZookeeper;

    /** this will be the string thats stored as a child of /zookeeper */
    private static final String quotaChildZookeeper = quotaZookeeper
            .substring(procZookeeper.length() + 1);

    /**
     * the zookeeper config node that acts as the config management node for
     * zookeeper
     */
    private static final String configZookeeper = ZooDefs.CONFIG_NODE;

    /** this will be the string thats stored as a child of /zookeeper */
    private static final String configChildZookeeper = configZookeeper
            .substring(procZookeeper.length() + 1);

    /**
     * the path trie that keeps track of the quota nodes in this datatree
     */
    private final PathTrie pTrie = new PathTrie();

    /**
     * This hashtable lists the paths of the ephemeral nodes of a session.
     */
    private final Map<Long, HashSet<String>> ephemerals =
        new ConcurrentHashMap<Long, HashSet<String>>();

    /**
     * This set contains the paths of all container nodes
     */
    private final Set<String> containers =
            Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    /**
     * This set contains the paths of all ttl nodes
     */
    private final Set<String> ttls =
            Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    private final ReferenceCountedACLCache aclCache = new ReferenceCountedACLCache();
}
```

DataNode的存储结构：
* ConcurrentHashMap<String, DataNode> nodes = new ConcurrentHashMap<String, DataNode>()：存储着所有DataNode，Key为DataNode的绝对路径Path，Value为DataNode。
* Map<Long, HashSet<String>> ephemerals = new ConcurrentHashMap<Long, HashSet<String>>()：存储着所有的临时节点的Path，Key为会话的ID，Value为当前会话的所有临时节点的Path。
* Set<String> containers = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>())：存储着所有容器节点的Path。
* Set<String> ttls = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>())：存储着所有TTL节点的Path。
* ReferenceCountedACLCache aclCache = new ReferenceCountedACLCache()：缓存着所有的节点的ACL列表。创建DataNode会将DataNode的ACL缓存到里面，DataNode中的acl为Key。

#### 2.2.DataNode的操作：
这里只分析DataTree层面的DataNode操作。

核心方法：org.apache.zookeeper.server.DataTree#processTxn(TxnHeader header, Record txn)
```
public ProcessTxnResult processTxn(TxnHeader header, Record txn, boolean isSubTxn)
    {
        ProcessTxnResult rc = new ProcessTxnResult();

        try {
            rc.clientId = header.getClientId();
            rc.cxid = header.getCxid();
            rc.zxid = header.getZxid();
            rc.type = header.getType();
            rc.err = 0;
            rc.multiResult = null;
            switch (header.getType()) {
                case OpCode.create:
                    CreateTxn createTxn = (CreateTxn) txn;
                    rc.path = createTxn.getPath();
                    createNode(
                            createTxn.getPath(),
                            createTxn.getData(),
                            createTxn.getAcl(),
                            createTxn.getEphemeral() ? header.getClientId() : 0,
                            createTxn.getParentCVersion(),
                            header.getZxid(), header.getTime(), null);
                    break;
                case OpCode.create2:
                    CreateTxn create2Txn = (CreateTxn) txn;
                    rc.path = create2Txn.getPath();
                    Stat stat = new Stat();
                    createNode(
                            create2Txn.getPath(),
                            create2Txn.getData(),
                            create2Txn.getAcl(),
                            create2Txn.getEphemeral() ? header.getClientId() : 0,
                            create2Txn.getParentCVersion(),
                            header.getZxid(), header.getTime(), stat);
                    rc.stat = stat;
                    break;
                case OpCode.createTTL:
                    CreateTTLTxn createTtlTxn = (CreateTTLTxn) txn;
                    rc.path = createTtlTxn.getPath();
                    stat = new Stat();
                    createNode(
                            createTtlTxn.getPath(),
                            createTtlTxn.getData(),
                            createTtlTxn.getAcl(),
                            EphemeralType.TTL.toEphemeralOwner(createTtlTxn.getTtl()),
                            createTtlTxn.getParentCVersion(),
                            header.getZxid(), header.getTime(), stat);
                    rc.stat = stat;
                    break;
                case OpCode.createContainer:
                    CreateContainerTxn createContainerTxn = (CreateContainerTxn) txn;
                    rc.path = createContainerTxn.getPath();
                    stat = new Stat();
                    createNode(
                            createContainerTxn.getPath(),
                            createContainerTxn.getData(),
                            createContainerTxn.getAcl(),
                            EphemeralType.CONTAINER_EPHEMERAL_OWNER,
                            createContainerTxn.getParentCVersion(),
                            header.getZxid(), header.getTime(), stat);
                    rc.stat = stat;
                    break;
                case OpCode.delete:
                case OpCode.deleteContainer:
                    DeleteTxn deleteTxn = (DeleteTxn) txn;
                    rc.path = deleteTxn.getPath();
                    deleteNode(deleteTxn.getPath(), header.getZxid());
                    break;
                case OpCode.reconfig:
                case OpCode.setData:
                    SetDataTxn setDataTxn = (SetDataTxn) txn;
                    rc.path = setDataTxn.getPath();
                    rc.stat = setData(setDataTxn.getPath(), setDataTxn
                            .getData(), setDataTxn.getVersion(), header
                            .getZxid(), header.getTime());
                    break;
                case OpCode.setACL:
                    SetACLTxn setACLTxn = (SetACLTxn) txn;
                    rc.path = setACLTxn.getPath();
                    rc.stat = setACL(setACLTxn.getPath(), setACLTxn.getAcl(),
                            setACLTxn.getVersion());
                    break;
                case OpCode.closeSession:
                    killSession(header.getClientId(), header.getZxid());
                    break;
                case OpCode.error:
                    ErrorTxn errTxn = (ErrorTxn) txn;
                    rc.err = errTxn.getErr();
                    break;
                case OpCode.check:
                    CheckVersionTxn checkTxn = (CheckVersionTxn) txn;
                    rc.path = checkTxn.getPath();
                    break;
                case OpCode.multi:
                    MultiTxn multiTxn = (MultiTxn) txn ;
                    List<Txn> txns = multiTxn.getTxns();
                    rc.multiResult = new ArrayList<ProcessTxnResult>();
                    boolean failed = false;
                    for (Txn subtxn : txns) {
                        if (subtxn.getType() == OpCode.error) {
                            failed = true;
                            break;
                        }
                    }

                    boolean post_failed = false;
                    for (Txn subtxn : txns) {
                        ByteBuffer bb = ByteBuffer.wrap(subtxn.getData());
                        Record record = null;
                        switch (subtxn.getType()) {
                            case OpCode.create:
                                record = new CreateTxn();
                                break;
                            case OpCode.createTTL:
                                record = new CreateTTLTxn();
                                break;
                            case OpCode.createContainer:
                                record = new CreateContainerTxn();
                                break;
                            case OpCode.delete:
                            case OpCode.deleteContainer:
                                record = new DeleteTxn();
                                break;
                            case OpCode.setData:
                                record = new SetDataTxn();
                                break;
                            case OpCode.error:
                                record = new ErrorTxn();
                                post_failed = true;
                                break;
                            case OpCode.check:
                                record = new CheckVersionTxn();
                                break;
                            default:
                                throw new IOException("Invalid type of op: " + subtxn.getType());
                        }
                        assert(record != null);

                        ByteBufferInputStream.byteBuffer2Record(bb, record);

                        if (failed && subtxn.getType() != OpCode.error){
                            int ec = post_failed ? Code.RUNTIMEINCONSISTENCY.intValue()
                                                 : Code.OK.intValue();

                            subtxn.setType(OpCode.error);
                            record = new ErrorTxn(ec);
                        }

                        if (failed) {
                            assert(subtxn.getType() == OpCode.error) ;
                        }

                        TxnHeader subHdr = new TxnHeader(header.getClientId(), header.getCxid(),
                                                         header.getZxid(), header.getTime(),
                                                         subtxn.getType());
                        ProcessTxnResult subRc = processTxn(subHdr, record, true);
                        rc.multiResult.add(subRc);
                        if (subRc.err != 0 && rc.err == 0) {
                            rc.err = subRc.err ;
                        }
                    }
                    break;
            }
        } catch (KeeperException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Failed: " + header + ":" + txn, e);
            }
            rc.err = e.code().intValue();
        } catch (IOException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Failed: " + header + ":" + txn, e);
       }
 }
 ```
#### 2.3.TxnHeader类
定义了节点操作类型，事务ID，客户端信息等。
![](https://upload-images.jianshu.io/upload_images/14781314-5056e46e644166b2.png)

* long clientId：发起节点操作的客户端的ID，用来关联一个Socket连接。
* int cxid：与客户端交互的事务ID。
* long zxid：服务器端生成的事务ID。
* long time：时间戳。
* int type：操作的类型，对应OpCode类中的常量值，OpCode类定义了所有的操作类型。
* Record类： 底层通信数据序列化与反序列化统一接口。org.apache.zookeeper.txn包下所有类都继承此类。这里以org.apache.zookeeper.txn.CreateTxn类为例，CreateTxn用于创建非TTL节点和非CONTAINER节点。
#### 2.4.CreateTxn类
![](https://upload-images.jianshu.io/upload_images/14781314-2ddcc0c6e40492ad.png)
* String path：节点的绝对路径Path。
* byte[] data：节点数据。
* java.util.List<org.apache.zookeeper.data.ACL> acl：节点的权限控制列表。
* boolean ephemeral：是否为临时节点。
* int parentCVersion：父节点的版本号。
##### 2.4.1.创建节点操作
org.apache.zookeeper.server.DataTree#createNode

创建节点时父节点必须存在，如何确定父节点是否存在？
根据所创建节点的绝对路径Path，来解析出父节点的绝对路径Path。以父节点的绝对路径Path从ConcurrentHashMap<String, DataNode> nodes = new ConcurrentHashMap<String, DataNode>()获取，若为null，则代表父节点不存在，抛出KeeperException.NoNodeException()异常。
```
 int lastSlash = path.lastIndexOf('/');
 String parentName = path.substring(0, lastSlash);
```
随后就是创建一个DataNode对象，填充DataNode属性，修改父DataNode中的子节点信息和版本信息。最后将DataNode放入节点树中。

##### 2.4.2.更新节点数据操作
org.apache.zookeeper.server.DataTree#setData
设置数据和更新数据的操作都归于此方法，采用替换的方式。先检查对应的节点是否存在，加锁保证数据的更新有序进行，替换数据后更新节点的stat信息，最后触发watch通知。

```
public Stat setData(String path, byte data[], int version, long zxid,
            long time) throws KeeperException.NoNodeException {
        Stat s = new Stat();
        DataNode n = nodes.get(path);
        if (n == null) {
            throw new KeeperException.NoNodeException();
        }
        byte lastdata[] = null;
        synchronized (n) {
            lastdata = n.data;
            n.data = data;
            n.stat.setMtime(time);
            n.stat.setMzxid(zxid);
            n.stat.setVersion(version);
            n.copyStat(s);
        }
        // now update if the path is in a quota subtree.
        String lastPrefix = getMaxPrefixWithQuota(path);
        if(lastPrefix != null) {
          this.updateBytes(lastPrefix, (data == null ? 0 : data.length)
              - (lastdata == null ? 0 : lastdata.length));
        }
        nodeDataSize.addAndGet(getNodeSize(path, data) - getNodeSize(path, lastdata));
        dataWatches.triggerWatch(path, EventType.NodeDataChanged);
        return s;
    }
```  
##### 2.4.3.删除节点操作
org.apache.zookeeper.server.DataTree#deleteNode
首先remove掉父节点Set<String> children中的子节点名称字符串。
```
int lastSlash = path.lastIndexOf('/');
//解析出父节点的绝对路径Path
String parentName = path.substring(0, lastSlash);
//当前节点的名称
String childName = path.substring(lastSlash + 1);
DataNode parent = nodes.get(parentName);
if (parent == null) {
    throw new KeeperException.NoNodeException();
}
//在父节点直接remove掉，然后更新版本
synchronized (parent) {
    parent.removeChild(childName);
    parent.stat.setPzxid(zxid);
 }
```
然后在节点树中删除此节点，删除此节点的ACL缓存，更新节点数量。
```
 DataNode node = nodes.get(path);
if (node == null) {
    throw new KeeperException.NoNodeException();
}
//删除此节点
nodes.remove(path);
synchronized (node) {
    //删除此节点的ACL缓存
    aclCache.removeUsage(node.acl);
    //更新节点数量
    nodeDataSize.addAndGet(-getNodeSize(path, node.data));
}
```
再然后判断该节点若为临时节点、容器节点或TTL节点，需要进一步执行清理工作。
```
synchronized (parent) {
    long eowner = node.stat.getEphemeralOwner();
    EphemeralType ephemeralType = EphemeralType.get(eowner);
    if (ephemeralType == EphemeralType.CONTAINER) {
           containers.remove(path);
     } else if (ephemeralType == EphemeralType.TTL) {
           ttls.remove(path);
     } else if (eowner != 0) {
           Set<String> nodes = ephemerals.get(eowner);
           if (nodes != null) {
               synchronized (nodes) {
                   nodes.remove(path);
               }
          }
    }
}
```
最后触发watch通知。
```
WatcherOrBitSet processed = dataWatches.triggerWatch(path,
                EventType.NodeDeleted);
childWatches.triggerWatch(path, EventType.NodeDeleted, processed);
childWatches.triggerWatch("".equals(parentName) ? "/" : parentName,
                EventType.NodeChildrenChanged);
```

#### 2.4.2.数据库ZKDatabase
ZKDatabase会定时将DataTree保存为快照保存在磁盘里，启动的时候，ZKDatabase负责从磁盘加载快照和操作日志来构建Database：org.apache.zookeeper.server.ZKDatabase#loadDataBase
首先在磁盘dataDir目录寻找最新的那个快照，并将其反序列化填充至DataTree。这个快照的zxid很有可能不是最新的，但事务提交日志里记录着最新的zxid，因此想要完全恢复完整的DataTree，需要将快照的最新zxid和事务提交日志最新zxid之间的事务操作全部执行一遍：org.apache.zookeeper.server.persistence.FileTxnSnapLog#fastForwardFromEdits。
完全恢复Database以后返回当前Database的最新事务zxid，这个zxid是后面选举的重要凭证。
```
long zxid = snapLog.restore(dataTree, sessionsWithTimeouts, commitProposalPlaybackListener);
```
启动完成后ZKDatabase负责执行所有的节点操作。

### ZK服务的核心ZookeeperServer
* ZookeeperServer维护着ZK服务的状态，负责会话管理、定义处理客户端请求的流程、生成快照等功能。从ZookeeperServer的成员变量可以看出：
* SessionTracker sessionTracker：负责会话的创建、追踪、销毁。
* RequestProcessor firstProcessor：采用调用链的方式处理客户端的请求，ZookeeperServer的不同子类会重新定义这个流程。
* FileTxnSnapLog txnLogFactory：生成快照到磁盘，恢复快照到内存。
* ZKDatabase zkDb：数据库。
* State state：ZK服务的状态的状态，初始为INITIAL。Quorum模式下Leader等到选举完成，Follow和Observe等到同步完成以后才会变成RUNNING状态，这时候才能处理客户端请求。
ZookeeperServer家族
![](https://upload-images.jianshu.io/upload_images/14781314-2ba624435230898c.png)

不同模式，不同角色使用的ZookeeperServer实例不同：
* 单机模式：ZookeeperServer实例。
* Quorum模式：
  * Leader > LeaderZooKeeperServer
  * Follower > FollowerZooKeeperServer
  * Observer > ObserverZooKeeperServer
这些ZookeeperServer实例最大的不同是处理客户端请求的流程不同，具体是由RequestProcessor调用链来控制。
* ZookeeperServer：PrepRequestProcessor -> SyncRequestProcessor -> FinalRequestProcessor
* LeaderZooKeeperServer：CommitProcessor -> Leader.ToBeAppliedRequestProcessor -> FinalRequestProcessor
* FollowerZooKeeperServer：FollowerRequestProcessor -> CommitProcessor -> FinalRequestProcessor
* ObserverZooKeeperServer：ObserverRequestProcessor -> CommitProcessor -> FinalRequestProcessor
#### 客户端连接管理ServerCnxnFactory
不同角色的ServerCnxnFactory持有的是不同的ZookeeperServer。Leader的ServerCnxnFactory持有的是LeaderZooKeeperServer，Follower的ServerCnxnFactory持有的是FollowerZooKeeperServer，Observer的ServerCnxnFactory持有的是ObserverZooKeeperServer。
每个ZookeeperServer在选举完成以后被设置进ServerCnxnFactory里。
org.apache.zookeeper.server.quorum.Observer#observeLeader
org.apache.zookeeper.server.quorum.Follower#followLeader
org.apache.zookeeper.server.quorum.Leader#lead


## 参考
https://www.jianshu.com/p/d27d7f3e42ce
