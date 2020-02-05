## Zookeeper之服务端ZookeeperServer
### 1.概览
```
                                    ZooKeeperServer
                                             ↑
                                    QuorumZooKeeperServer
                           ↗                 ↑                      ↖
            LearnerZooKeeperServer ReadOnlyZooKeeperServer LeaderZooKeeperServer
                  ↗             ↖
ObserverZooKeeperServer FollowerZooKeeperServer
```
![](https://images2015.cnblogs.com/blog/616953/201703/616953-20170307150147609-364239383.png)

ZooKeeperServer，为所有服务器的父类，其请求处理链为PrepRequestProcessor -> SyncRequestProcessor -> FinalRequestProcessor。

QuorumZooKeeperServer，其是所有参与选举的服务器的父类，是抽象类，其继承了ZooKeeperServer类。

LeaderZooKeeperServer，Leader服务器，继承了QuorumZooKeeperServer类，其请求处理链为PrepRequestProcessor -> ProposalRequestProcessor -> CommitProcessor -> Leader.ToBeAppliedRequestProcessor -> FinalRequestProcessor。

LearnerZooKeeper，其是Learner服务器的父类，为抽象类，也继承了QuorumZooKeeperServer类。

FollowerZooKeeperServer，Follower服务器，继承了LearnerZooKeeper，其请求处理链为FollowerRequestProcessor -> CommitProcessor -> FinalRequestProcessor。

ObserverZooKeeperServer，Observer服务器，继承了LearnerZooKeeper。

ReadOnlyZooKeeperServer，只读服务器，不提供写服务，继承QuorumZooKeeperServer，其处理链的第一个处理器为ReadOnlyRequestProcessor。
### 2.ZooKeeperServer


ZooKeeperServer分别为：INITIAL/RUNNING/SHUTDOWN/ERROR
```
protected enum State {
    INITIAL,
    RUNNING,
    SHUTDOWN,
    ERROR
}
```
#### 2.1 类的继承关系　
```
public class ZooKeeperServer implements SessionExpirer, ServerStats.Provider {}
```
说明：
ZooKeeperServer是ZooKeeper中所有服务器的父类，其实现了Session.Expirer和ServerStats.Provider接口，
SessionExpirer中定义了expire方法（表示会话过期）和getServerId方法（表示获取服务器ID），
而Provider则主要定义了获取服务器某些数据的方法。

#### 2.2 类的内部类

##### 1. DataTreeBuilder类
```
public interface DataTreeBuilder {
    // 构建DataTree
    public DataTree build();
}
```
说明：其定义了构建树DataTree的接口。

##### 2. BasicDataTreeBuilder类　　
```
static public class BasicDataTreeBuilder implements DataTreeBuilder {
    public DataTree build() {
        return new DataTree();
    }
}
```
说明：实现DataTreeBuilder接口，返回新创建的树DataTree。

##### 3. MissingSessionException类　　

```
public static class MissingSessionException extends IOException {
    private static final long serialVersionUID = 7467414635467261007L;

    public MissingSessionException(String msg) {
        super(msg);
    }
}
```
说明：表示会话缺失异常。

##### 4. ChangeRecord类　

```
static class ChangeRecord {
    ChangeRecord(long zxid, String path, StatPersisted stat, int childCount,
            List<ACL> acl) {
        // 属性赋值
        this.zxid = zxid;
        this.path = path;
        this.stat = stat;
        this.childCount = childCount;
        this.acl = acl;
    }
    
    // zxid
    long zxid;

    // 路径
    String path;

    // 统计数据
    StatPersisted stat; /* Make sure to create a new object when changing */

    // 子节点个数
    int childCount;

    // ACL列表
    List<ACL> acl; /* Make sure to create a new object when changing */

    @SuppressWarnings("unchecked")
    // 拷贝
    ChangeRecord duplicate(long zxid) {
        StatPersisted stat = new StatPersisted();
        if (this.stat != null) {
            DataTree.copyStatPersisted(this.stat, stat);
        }
        return new ChangeRecord(zxid, path, stat, childCount,
                acl == null ? new ArrayList<ACL>() : new ArrayList(acl));
    }
}
```
说明：ChangeRecord数据结构是用于方便PrepRequestProcessor和FinalRequestProcessor之间进行信息共享，其包含了一个拷贝方法duplicate，用于返回属性相同的ChangeRecord实例。

#### 2.3 类的属性　　


```
public class ZooKeeperServer implements SessionExpirer, ServerStats.Provider {
// 日志器
protected static final Logger LOG;

static {
    // 初始化日志器
    LOG = LoggerFactory.getLogger(ZooKeeperServer.class);
    
    Environment.logEnv("Server environment:", LOG);
}
// JMX服务
protected ZooKeeperServerBean jmxServerBean;
protected DataTreeBean jmxDataTreeBean;


// 默认心跳频率
public static final int DEFAULT_TICK_TIME = 3000;
protected int tickTime = DEFAULT_TICK_TIME;
/** value of -1 indicates unset, use default */
// 最小会话过期时间
protected int minSessionTimeout = -1;
/** value of -1 indicates unset, use default */
// 最大会话过期时间
protected int maxSessionTimeout = -1;
// 会话跟踪器
protected SessionTracker sessionTracker;
// 事务日志快照
private FileTxnSnapLog txnLogFactory = null;
// Zookeeper内存数据库
private ZKDatabase zkDb;
// 
protected long hzxid = 0;
// 异常
public final static Exception ok = new Exception("No prob");
// 请求处理器
protected RequestProcessor firstProcessor;
// 运行标志
protected volatile boolean running;

/**
 * This is the secret that we use to generate passwords, for the moment it
 * is more of a sanity check.
 */
// 生成密码的密钥
static final private long superSecret = 0XB3415C00L;

// 
int requestsInProcess;

// 未处理的ChangeRecord
final List<ChangeRecord> outstandingChanges = new ArrayList<ChangeRecord>();

// this data structure must be accessed under the outstandingChanges lock
// 记录path对应的ChangeRecord
final HashMap<String, ChangeRecord> outstandingChangesForPath =
    new HashMap<String, ChangeRecord>();
    
// 连接工厂
private ServerCnxnFactory serverCnxnFactory;

// 服务器统计数据
private final ServerStats serverStats;
}
```
说明：类中包含了心跳频率，会话跟踪器（处理会话）、事务日志快照、内存数据库、请求处理器、未处理的ChangeRecord、服务器统计信息等。

#### 2.4 类的构造函数

##### 1. ZooKeeperServer()型构造函数　　
```
public ZooKeeperServer() {
    serverStats = new ServerStats(this);
}
```
说明：其只初始化了服务器的统计信息。

##### 2. ZooKeeperServer(FileTxnSnapLog, int, int, int, DataTreeBuilder, ZKDatabase)型构造函数　　

```
public ZooKeeperServer(FileTxnSnapLog txnLogFactory, int tickTime,
        int minSessionTimeout, int maxSessionTimeout,
        DataTreeBuilder treeBuilder, ZKDatabase zkDb) {
    // 给属性赋值
    serverStats = new ServerStats(this);
    this.txnLogFactory = txnLogFactory;
    this.zkDb = zkDb;
    this.tickTime = tickTime;
    this.minSessionTimeout = minSessionTimeout;
    this.maxSessionTimeout = maxSessionTimeout;
    
    LOG.info("Created server with tickTime " + tickTime
            + " minSessionTimeout " + getMinSessionTimeout()
            + " maxSessionTimeout " + getMaxSessionTimeout()
            + " datadir " + txnLogFactory.getDataDir()
            + " snapdir " + txnLogFactory.getSnapDir());
}
```
说明：该构造函数会初始化服务器统计数据、事务日志工厂、心跳时间、会话时间（最短超时时间和最长超时时间）。

##### 3. ZooKeeperServer(FileTxnSnapLog, int, DataTreeBuilder)型构造函数　　
```
public ZooKeeperServer(FileTxnSnapLog txnLogFactory, int tickTime,
        DataTreeBuilder treeBuilder) throws IOException {
    this(txnLogFactory, tickTime, -1, -1, treeBuilder,
            new ZKDatabase(txnLogFactory));
}
```
说明：其首先会生成ZooKeeper内存数据库后，然后调用第二个构造函数进行初始化操作。

##### 4. ZooKeeperServer(File, File, int)型构造函数　
```
public ZooKeeperServer(File snapDir, File logDir, int tickTime)
        throws IOException {
    this( new FileTxnSnapLog(snapDir, logDir),
            tickTime, new BasicDataTreeBuilder());
}
```
说明：其会调用同名构造函数进行初始化操作。

##### 5. ZooKeeperServer(FileTxnSnapLog, DataTreeBuilder)型构造函数　　

```
public ZooKeeperServer(FileTxnSnapLog txnLogFactory,
        DataTreeBuilder treeBuilder)
    throws IOException
{
    this(txnLogFactory, DEFAULT_TICK_TIME, -1, -1, treeBuilder,
            new ZKDatabase(txnLogFactory));
}
```
说明：其生成内存数据库之后再调用同名构造函数进行初始化操作。

#### 2.5 核心函数分析

##### 1. loadData函数　

```
public void loadData() throws IOException, InterruptedException {
    /*
     * When a new leader starts executing Leader#lead, it 
     * invokes this method. The database, however, has been
     * initialized before running leader election so that
     * the server could pick its zxid for its initial vote.
     * It does it by invoking QuorumPeer#getLastLoggedZxid.
     * Consequently, we don't need to initialize it once more
     * and avoid the penalty of loading it a second time. Not 
     * reloading it is particularly important for applications
     * that host a large database.
     * 
     * The following if block checks whether the database has
     * been initialized or not. Note that this method is
     * invoked by at least one other method: 
     * ZooKeeperServer#startdata.
     *  
     * See ZOOKEEPER-1642 for more detail.
     */
    if(zkDb.isInitialized()){ // 内存数据库已被初始化
        // 设置为最后处理的Zxid
        setZxid(zkDb.getDataTreeLastProcessedZxid());
    }
    else { // 未被初始化，则加载数据库
        setZxid(zkDb.loadDataBase());
    }
    
    // Clean up dead sessions
    LinkedList<Long> deadSessions = new LinkedList<Long>();
    for (Long session : zkDb.getSessions()) { // 遍历所有的会话
        if (zkDb.getSessionWithTimeOuts().get(session) == null) { // 删除过期的会话
            deadSessions.add(session);
        }
    }
    // 完成DataTree的初始化
    zkDb.setDataTreeInit(true);
    for (long session : deadSessions) { // 遍历过期会话
        // XXX: Is lastProcessedZxid really the best thing to use?
        // 删除会话
        killSession(session, zkDb.getDataTreeLastProcessedZxid());
    }
}
```
说明：该函数用于加载数据，其首先会判断内存库是否已经加载设置zxid，之后会调用killSession函数删除过期的会话，killSession会从sessionTracker中删除session，并且killSession最后会调用DataTree的killSession函数，其源码如下　

```
void killSession(long session, long zxid) {
    // the list is already removed from the ephemerals
    // so we do not have to worry about synchronizing on
    // the list. This is only called from FinalRequestProcessor
    // so there is no need for synchronization. The list is not
    // changed here. Only create and delete change the list which
    // are again called from FinalRequestProcessor in sequence.
    // 移除session，并获取该session对应的所有临时节点
    HashSet<String> list = ephemerals.remove(session);
    if (list != null) {
        for (String path : list) { // 遍历所有临时节点
            try {
                // 删除路径对应的节点
                deleteNode(path, zxid);
                if (LOG.isDebugEnabled()) {
                    LOG
                            .debug("Deleting ephemeral node " + path
                                    + " for session 0x"
                                    + Long.toHexString(session));
                }
            } catch (NoNodeException e) {
                LOG.warn("Ignoring NoNodeException for path " + path
                        + " while removing ephemeral for dead session 0x"
                        + Long.toHexString(session));
            }
        }
    }
}
```
说明：DataTree的killSession函数的逻辑首先移除session，然后取得该session下的所有临时节点，然后逐一删除临时节点。

##### 2. submit函数　

```
public void submitRequest(Request si) {
    if (firstProcessor == null) { // 第一个处理器为空
        synchronized (this) {
            try {
                while (!running) { // 直到running为true，否则继续等待
                    wait(1000);
                }
            } catch (InterruptedException e) {
                LOG.warn("Unexpected interruption", e);
            }
            if (firstProcessor == null) {
                throw new RuntimeException("Not started");
            }
        }
    }
    try {
        touch(si.cnxn);
        // 是否为合法的packet
        boolean validpacket = Request.isValid(si.type);
        if (validpacket) { 
            // 处理请求
            firstProcessor.processRequest(si);
            if (si.cnxn != null) {
                incInProcess();
            }
        } else {
            LOG.warn("Received packet at server of unknown type " + si.type);
            new UnimplementedRequestProcessor().processRequest(si);
        }
    } catch (MissingSessionException e) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Dropping request: " + e.getMessage());
        }
    } catch (RequestProcessorException e) {
        LOG.error("Unable to process request:" + e.getMessage(), e);
    }
}
```
说明：当firstProcessor为空时，并且running标志为false时，其会一直等待，直到running标志为true，之后调用touch函数判断session是否存在或者已经超时，之后判断请求的类型是否合法，合法则使用请求处理器进行处理。

##### 3. processConnectRequest函数　　


```
public void processConnectRequest(ServerCnxn cnxn, ByteBuffer incomingBuffer) throws IOException {
    BinaryInputArchive bia = BinaryInputArchive.getArchive(new ByteBufferInputStream(incomingBuffer));
    ConnectRequest connReq = new ConnectRequest();
    // 反序列化
    connReq.deserialize(bia, "connect");
    if (LOG.isDebugEnabled()) {
        LOG.debug("Session establishment request from client "
                + cnxn.getRemoteSocketAddress()
                + " client's lastZxid is 0x"
                + Long.toHexString(connReq.getLastZxidSeen()));
    }
    boolean readOnly = false;
    try {
        // 是否为只读
        readOnly = bia.readBool("readOnly");
        cnxn.isOldClient = false;
    } catch (IOException e) {
        // this is ok -- just a packet from an old client which
        // doesn't contain readOnly field
        LOG.warn("Connection request from old client "
                + cnxn.getRemoteSocketAddress()
                + "; will be dropped if server is in r-o mode");
    }
    if (readOnly == false && this instanceof ReadOnlyZooKeeperServer) { // 为只读模式但是该服务器是只读服务器，抛出异常
        String msg = "Refusing session request for not-read-only client "
            + cnxn.getRemoteSocketAddress();
        LOG.info(msg);
        throw new CloseRequestException(msg);
    }
    if (connReq.getLastZxidSeen() > zkDb.dataTree.lastProcessedZxid) { // 请求连接的zxid大于DataTree处理的最大的zxid，抛出异常
        String msg = "Refusing session request for client "
            + cnxn.getRemoteSocketAddress()
            + " as it has seen zxid 0x"
            + Long.toHexString(connReq.getLastZxidSeen())
            + " our last zxid is 0x"
            + Long.toHexString(getZKDatabase().getDataTreeLastProcessedZxid())
            + " client must try another server";

        LOG.info(msg);
        throw new CloseRequestException(msg);
    }
    // 获取超时时间
    int sessionTimeout = connReq.getTimeOut();
    // 获取密码
    byte passwd[] = connReq.getPasswd();
    // 获取最短超时时间
    int minSessionTimeout = getMinSessionTimeout();
    if (sessionTimeout < minSessionTimeout) { 
        sessionTimeout = minSessionTimeout;
    }
    // 获取最长超时时间
    int maxSessionTimeout = getMaxSessionTimeout();
    if (sessionTimeout > maxSessionTimeout) {
        sessionTimeout = maxSessionTimeout;
    }
    // 设置超时时间
    cnxn.setSessionTimeout(sessionTimeout);
    // We don't want to receive any packets until we are sure that the
    // session is setup
    // 不接收任何packet，直到会话创建成功
    cnxn.disableRecv();
    // 获取会话id
    long sessionId = connReq.getSessionId();
    if (sessionId != 0) { // 表示重新创建会话
        long clientSessionId = connReq.getSessionId();
        LOG.info("Client attempting to renew session 0x"
                + Long.toHexString(clientSessionId)
                + " at " + cnxn.getRemoteSocketAddress());
        // 关闭会话
        serverCnxnFactory.closeSession(sessionId);
        // 设置会话id
        cnxn.setSessionId(sessionId);
        // 重新打开会话
        reopenSession(cnxn, sessionId, passwd, sessionTimeout);
    } else {
        LOG.info("Client attempting to establish new session at "
                + cnxn.getRemoteSocketAddress());
        // 创建会话
        createSession(cnxn, passwd, sessionTimeout);
    }
}
```
说明：其首先将传递的ByteBuffer进行反序列化，转化为相应的ConnectRequest，之后进行一系列判断（可能抛出异常），然后获取并判断该ConnectRequest中会话id是否为0，若为0，则表示可以创建会话，否则，重新打开会话。

##### 4. processPacket函数　

```
public void processPacket(ServerCnxn cnxn, ByteBuffer incomingBuffer) throws IOException {
    // We have the request, now process and setup for next
    InputStream bais = new ByteBufferInputStream(incomingBuffer);
    BinaryInputArchive bia = BinaryInputArchive.getArchive(bais);
    // 创建请求头
    RequestHeader h = new RequestHeader();
    // 将头反序列化为RequestHeader
    h.deserialize(bia, "header");
    // Through the magic of byte buffers, txn will not be
    // pointing
    // to the start of the txn
    incomingBuffer = incomingBuffer.slice();
    if (h.getType() == OpCode.auth) { // 需要进行认证（有密码）
        LOG.info("got auth packet " + cnxn.getRemoteSocketAddress());
        AuthPacket authPacket = new AuthPacket();
        // 将ByteBuffer转化为AuthPacket
        ByteBufferInputStream.byteBuffer2Record(incomingBuffer, authPacket);
        // 获取AuthPacket的模式
        String scheme = authPacket.getScheme();
        AuthenticationProvider ap = ProviderRegistry.getProvider(scheme);
        Code authReturn = KeeperException.Code.AUTHFAILED;
        if(ap != null) {
            try {
                // 进行认证
                authReturn = ap.handleAuthentication(cnxn, authPacket.getAuth());
            } catch(RuntimeException e) {
                LOG.warn("Caught runtime exception from AuthenticationProvider: " + scheme + " due to " + e);
                authReturn = KeeperException.Code.AUTHFAILED;                   
            }
        }
        if (authReturn!= KeeperException.Code.OK) { // 认证失败
            if (ap == null) {
                LOG.warn("No authentication provider for scheme: "
                        + scheme + " has "
                        + ProviderRegistry.listProviders());
            } else {
                LOG.warn("Authentication failed for scheme: " + scheme);
            }
            // send a response...
            // 构造响应头
            ReplyHeader rh = new ReplyHeader(h.getXid(), 0,
                    KeeperException.Code.AUTHFAILED.intValue());
            // 发送响应
            cnxn.sendResponse(rh, null, null);
            // ... and close connection
            // 关闭连接的信息
            cnxn.sendBuffer(ServerCnxnFactory.closeConn);
            // 不接收任何packet
            cnxn.disableRecv();
        } else { // 认证成功
            if (LOG.isDebugEnabled()) {
                LOG.debug("Authentication succeeded for scheme: "
                          + scheme);
            }
            LOG.info("auth success " + cnxn.getRemoteSocketAddress());
            // 构造响应头
            ReplyHeader rh = new ReplyHeader(h.getXid(), 0,
                    KeeperException.Code.OK.intValue());
            // 发送响应
            cnxn.sendResponse(rh, null, null);
        }
        return;
    } else {
        if (h.getType() == OpCode.sasl) { // 为SASL类型
            // 处理SASL
            Record rsp = processSasl(incomingBuffer,cnxn);
            // 构造响应头
            ReplyHeader rh = new ReplyHeader(h.getXid(), 0, KeeperException.Code.OK.intValue());
            // 发送响应
            cnxn.sendResponse(rh,rsp, "response"); // not sure about 3rd arg..what is it?
        }
        else { // 不为SASL类型
            // 创建请求
            Request si = new Request(cnxn, cnxn.getSessionId(), h.getXid(),
              h.getType(), incomingBuffer, cnxn.getAuthInfo());
            // 设置请求所有者
            si.setOwner(ServerCnxn.me);
            // 提交请求
            submitRequest(si);
        }
    }
    // 
    cnxn.incrOutstandingRequests(h);
}
```

说明：该函数首先将传递的ByteBuffer进行反序列，转化为相应的RequestHeader，然后根据该RequestHeader判断是否需要认证，
若认证失败，则构造认证失败的响应并发送给客户端，然后关闭连接，并且再补接收任何packet。
若认证成功，则构造认证成功的响应并发送给客户端。若不需要认证，则再判断其是否为SASL类型，
若是，则进行处理，然后构造响应并发送给客户端，否则，构造请求并且提交请求。

### 参考
https://www.cnblogs.com/leesf456/p/6514897.html
https://www.cnblogs.com/leesf456/p/6515105.html