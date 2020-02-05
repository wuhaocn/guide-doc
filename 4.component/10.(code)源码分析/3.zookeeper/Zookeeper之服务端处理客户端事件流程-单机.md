## ZooKeeper服务端处理请求 单机

在Zookeeper服务端启动源码中，在启动ServerCnxnFactory工厂时调用ServerCnxnFactory#startup方法，启动服务端接收客户端连接工厂，默认为NIOServerCnxnFactory，所以处理请求也是以NIOServerCnxnFactory分析。

### 1.处理客户端连接
接收线程调用的是NIOServerCnxnFactory#run方法，处理客户端channel
```
while (!ss.socket().isClosed()) {
  try {
    selector.select(1000);
    Set<SelectionKey> selected;
    synchronized (this) {
      selected = selector.selectedKeys();
    }
    ArrayList<SelectionKey> selectedList = new ArrayList<SelectionKey>(selected);
    Collections.shuffle(selectedList);
    for (SelectionKey k : selectedList) {
      if ((k.readyOps() & SelectionKey.OP_ACCEPT) != 0) {
        SocketChannel sc = ((ServerSocketChannel) k.channel()).accept();
        InetAddress ia = sc.socket().getInetAddress();
        int cnxncount = getClientCnxnCount(ia);
        // 1
        if (maxClientCnxns > 0 && cnxncount >= maxClientCnxns){
          sc.close();
        } else {
          //2
          sc.configureBlocking(false);
          SelectionKey sk = sc.register(selector, SelectionKey.OP_READ);
          NIOServerCnxn cnxn = createConnection(sc, sk);
          sk.attach(cnxn);
          addCnxn(cnxn);
        }
      } else if ((k.readyOps() & (SelectionKey.OP_READ | SelectionKey.OP_WRITE)) != 0) {
        //3
        NIOServerCnxn c = (NIOServerCnxn) k.attachment();
        c.doIO(k);
      } else {
        //log
      }
    }
    selected.clear();
  }
}
```
* 1.判断同一个ip连接数是否超限
* 2.新连接：创建NIOServerCnxn实例cnxn并调用sk.attach(cnxn)
* 3.读写：取出attachment并处理IO

看到这里我有两个问题:
* selector.select(1000)，这个NIO epoll bug修复了吗？
* c.doIO(k);IO处理单线程的，性能有问题，虽然后面会将请求交由其他线程处理，但也是单线程的

我这里看的版本是3.4.13，在3.5.3-beta版本至少改成多线程版本了

* 3.4.13 NIOServerCnxnFactory#start
```
public void start() {
  // ensure thread is started once and only once
  if (thread.getState() == Thread.State.NEW) {
    thread.start();
  }
}
```
* 3.5.3-beta NIOServerCnxnFactory#start
```
public void start() {
    stopped = false;
    // IO线程池
    if (workerPool == null) {
        workerPool = new WorkerService("NIOWorker", numWorkerThreads, false);
    }
    // selectorThreads作用类似于Netty中的childGroup
    for(SelectorThread thread : selectorThreads) {
        if (thread.getState() == Thread.State.NEW) {
            thread.start();
        }
    }
    // 接收连接线程
    if (acceptThread.getState() == Thread.State.NEW) {
        acceptThread.start();
    }
    // 释放过期连接线程
    if (expirerThread.getState() == Thread.State.NEW) {
        expirerThread.start();
    }
}
```
* 在3.5.3-beta中做了如下改进:
    * 由原来的单线程改为IO线程池处理（workerPool）
    * 客户端连接的读写事件也改为多线程（selectorThreads），类似于Netty中的childGroup
    * acceptThread作用类似Netty中的parentGroup
### 2.处理客户端IO
继续分析NIOServerCnxn#doIO方法
```
void doIO(SelectionKey k) throws InterruptedException {
  try {
    if (isSocketOpen() == false) {
      return;
    }
    if (k.isReadable()) {
      // 1. incomingBuffer赋值：ByteBuffer incomingBuffer = lenBuffer;
      // 和客户端一样先读4个字节的长度，解决拆包粘包
      int rc = sock.read(incomingBuffer);
      if (rc < 0) {
        throw new EndOfStreamException("");
      }
      if (incomingBuffer.remaining() == 0) {
        boolean isPayload;
        // 当前读的是长度
        if (incomingBuffer == lenBuffer) { // start of next request
          incomingBuffer.flip();
          // 2. 根据读取的长度，重新分配incomingBuffer
          isPayload = readLength(k);
          incomingBuffer.clear();
        } else {
          // continuation
          isPayload = true;
        }
        if (isPayload) { // not the case for 4letterword
          // 3 读取数据并处理
          readPayload();
        } else {
          return;
        }
      }
    }
    if (k.isWritable()) {
      // 1 从outgoingBuffers发送数据
      if (outgoingBuffers.size() > 0) {
        ByteBuffer directBuffer = factory.directBuffer;
        directBuffer.clear();

        // 2. 根据发送队列数据，将数据放到directBuffer一并发送
        for (ByteBuffer b : outgoingBuffers) {
          if (directBuffer.remaining() < b.remaining()) {
            // directBuffer放不下整个buffer，尝试放一部分
            b = (ByteBuffer) b.slice().limit(directBuffer.remaining());
          }
          int p = b.position();
          directBuffer.put(b);
          b.position(p);
          if (directBuffer.remaining() == 0) {
            break;
          }
        }
        directBuffer.flip();

        // 3.发送数据
        int sent = sock.write(directBuffer);
        ByteBuffer bb;

        //4 根据send大小移除buffer队列
        while (outgoingBuffers.size() > 0) {
          bb = outgoingBuffers.peek();
          if (bb == ServerCnxnFactory.closeConn) {
            throw new CloseRequestException("close requested");
          }
          int left = bb.remaining() - sent;
          if (left > 0) {
            // 这个buff是上面slice之后还有剩余的，剩下的buffer都没发，跳出
            bb.position(bb.position() + sent);
            break;
          }
          packetSent();
          /* 移除已发送的buffer */
          sent -= bb.remaining();
          outgoingBuffers.remove();
        }
      }

      synchronized(this.factory) {
        if (outgoingBuffers.size() == 0) {
          if (!initialized && (sk.interestOps() & SelectionKey.OP_READ) == 0) {
            throw new CloseRequestException("responded to info probe");
          }
          sk.interestOps(sk.interestOps() & (~SelectionKey.OP_WRITE));
        } else {
          sk.interestOps(sk.interestOps() | SelectionKey.OP_WRITE);
        }
      }
    }
  } catch (CancelledKeyException e) {
    close();
  } catch (CloseRequestException e) {
    // expecting close to log session closure
    close();
  } catch (EndOfStreamException e) {
    close();
  } catch (IOException e) {
    close();
  }
}
```
#### 2.1.读操作

* 读数据，和客户端一样先读4个字节的长度，然后根据读得的长度分配buffer，解决拆包粘包问题
* 根据读取的长度，重新分配incomingBuffer
* 读数据并处理，后面分析

#### 2.2.写操作

* 从outgoingBuffers发送数据，说明其他操作的结果数据将保存到此发送队列，节点的操作触发的watche事件也会保存到此队列
* 根据发送队列数据，将数据放到directBuffer一并发送
* 发送directBuffer中的数据
* 根据send大小移除buffer队列
### 3.读取客户端请求并处理
比如说对于客户端的getData请求，服务端先读取客户端通道的请求内容，然后根据请求的节点信息，从内存数据库中获取数据，然后再将数据返回给客户端。

#### 3.1.读取客户端数据
NIOServerCnxn#readPayload方法读取客户端通道数据
```
private void readPayload() throws IOException, InterruptedException {
  // 1 正常逻辑是不应该进入if块的，因为上一步应该把len个字节的数据全读进去了
  if (incomingBuffer.remaining() != 0) { // have we read length bytes?
    int rc = sock.read(incomingBuffer); // sock is non-blocking, so ok
    if (rc < 0) {
      // 数据不完整
      throw new EndOfStreamException("");
    }
  }

  //2 incomingBuffer读满才说明数据是完整的
  if (incomingBuffer.remaining() == 0) { // have we read length bytes?
    packetReceived();
    incomingBuffer.flip();
    // 3 处理请求
    if (!initialized) {
      readConnectRequest();
    } else {
      readRequest();
    }
    // 4 重置两个buffer
    lenBuffer.clear();
    incomingBuffer = lenBuffer;
  }
}
```
* 1.正常逻辑是不应该进入if块的，因为上一步应该把len个字节的数据全读进去了，如果进入了，还没读到数据，则说明数据包不完整
* 2.incomingBuffer读满才说明数据是完整的
* 3.处理请求
* 4.重置两个buffer

zk字节数据流的格式是如下格式，前4个字节的长度length，后面length个长度的数据体，这种数据格式解决TCP数据包的拆包粘包问题。
```
|----|---------...----------|
 len           data
```
#### 3.2.处理客户端请求
根据客户端连接是否初始化将请求分为连接请求和数据请求，连接请求需要为客户端生成session并将sessionId返回回去，而数据请求则需要处理数据。

#### 3.3.创建连接
NIOServerCnxn#readConnectRequest方法读取连接请求，处理并响应，内部调用ZooKeeperServer#processConnectRequest方法处理，processConnectRequest方法流程

##### 1. 反序列化连接请求
```
BinaryInputArchive bia = BinaryInputArchive.getArchive(new ByteBufferInputStream(incomingBuffer));
ConnectRequest connReq = new ConnectRequest();
connReq.deserialize(bia, "connect");
```
##### 2.根据连接是否只读以及服务器是否只读判断匹配，否则报错
```
readOnly = bia.readBool("readOnly");
if (readOnly == false && this instanceof ReadOnlyZooKeeperServer) {
  String msg = "。。。"
  throw new CloseRequestException(msg);
}
```
##### 3. 根据Zxid判断该连接连接是否可连
```
if (connReq.getLastZxidSeen() > zkDb.dataTree.lastProcessedZxid) {
  String msg = "。。。"
  throw new CloseRequestException(msg);
}
```
##### 4. 处理sessionTimeout
```
byte passwd[] = connReq.getPasswd();
。。。
cnxn.setSessionTimeout(sessionTimeout);
```
##### 5.根据sessionId有无进行重连或创建session操作

* 创建session
```
createSession(cnxn, passwd, sessionTimeout);
```
创建session有两步
    
1.由sessionTracker创建session，将session按过期时间分桶存放，返回创建的sessionId
    
```
long sessionId = sessionTracker.createSession(timeout);
```
    
2.提交Request，由处理链处理，创建连接是个事务操作，需要保存到内存数据库中，具体后面处理请求部分分析
    
* 重连
```
cnxn.setSessionId(sessionId);
reopenSession(cnxn, sessionId, passwd, sessionTimeout);
```
1.判断密码是否正确

2.根据sessionId构建session，并保存

3.返回响应。重连session已经保存到内存数据库，不需再处理。

#### 3.4.数据请求
NIOServerCnxn#readRequest方法内部调用ZooKeeperServer#processPacket方法处理请求，流程：

##### 1.反序列化请求头
```
InputStream bais = new ByteBufferInputStream(incomingBuffer);
BinaryInputArchive bia = BinaryInputArchive.getArchive(bais);
RequestHeader h = new RequestHeader();
h.deserialize(bia, "header");
```
##### 2.根据请求头类型分别处理
```
if (h.getType() == OpCode.auth) {
 ...
} else {
  if (h.getType() == OpCode.sasl) {
    ...
  }
  else {
    // 创建请求
    Request si = new Request(cnxn, cnxn.getSessionId(), h.getXid(),
                             h.getType(), incomingBuffer, cnxn.getAuthInfo());
    si.setOwner(ServerCnxn.me);
    // 提交请求
    submitRequest(si);
  }
}
```
权限认证类型比较简单，具体分析数据请求Request

##### 3.创建请求，提交请求，将请求交由请求链处理
```
// ZooKeeperServer#submitRequest(Request)
firstProcessor.processRequest(si);
```
在服务端启动源码已经分析了请求链
```
// ZooKeeperServer#setupRequestProcessors
protected void setupRequestProcessors() {
  RequestProcessor finalProcessor = new FinalRequestProcessor(this);
  RequestProcessor syncProcessor = new SyncRequestProcessor(this, finalProcessor);
  ((SyncRequestProcessor)syncProcessor).start();
  firstProcessor = new PrepRequestProcessor(this, syncProcessor);
  ((PrepRequestProcessor)firstProcessor).start();
}
```
```
firstProcessor(PrepRequestProcessor) -> syncProcessor(SyncRequestProcessor) -> finalProcessor(FinalRequestProcessor)
```
且firstProcessor、syncProcessor是以线程的方式启动的。

对于PrepRequestProcessor#processRequest方法，仅仅将请求添加到submittedRequests队列
```
public void processRequest(Request request) {
  submittedRequests.add(request);
}
```
##### 4. 请求链PrepRequestProcessor firstProcessor处理请求
```
// PrepRequestProcessor#run
Request request = submittedRequests.take();
pRequest(request);
```
在PrepRequestProcessor#pRequest方法内部，就是真正的处理流程
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
1.根据请求类型分别处理，其中事务操作和非事务操作分别处理，非事务操作仅检查session

2.需要将异常信息设置到Request中

3.调用下一个Processor处理

##### 5.处理事务请求
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
将节点变更记录添加到outstandingChanges队列中，主要是为了保证响应的顺序性，在后面FinalRequestProcessor会用到

##### 6.请求链SyncRequestProcessor syncProcessor处理请求
PrepRequestProcessor#pRequest方法最后调用nextProcessor.processRequest(request);继续处理请求，根据链式结构，进入SyncRequestProcessor#processRequest方法，该方法也是将request添加到队列中，等待线程消费处理。这个处理器主要工作是同步，将内存数据及日志写入snapshot文件和flush log文件，不过时间是半随机的，为了确保不是集群中的所有服务器都同时保存快照，在run方法中
```
public void run() {
  try {
    int logCount = 0;

    // 1 我们这样做是为了确保不是集成中的所有服务器都同时保存快照
    setRandRoll(r.nextInt(snapCount/2));
    while (true) {
      Request si = null;
      if (toFlush.isEmpty()) {
        si = queuedRequests.take();
      } else {
        si = queuedRequests.poll();
        if (si == null) {
          // 请求队列没了，则flush队列
          flush(toFlush);
          continue;
        }
      }
      if (si == requestOfDeath) {
        break;
      }
      if (si != null) {
        // 2 将请求写入日志文件
        if (zks.getZKDatabase().append(si)) {
          logCount++;
          // 3 保存的日志数量过多
          if (logCount > (snapCount / 2 + randRoll)) {
            setRandRoll(r.nextInt(snapCount/2));
            // 滚动日志文件
            zks.getZKDatabase().rollLog();
            // take a snapshot
            if (snapInProcess != null && snapInProcess.isAlive()) {
              // 当前保存快照线程还在运行，说明此时系统有些繁忙
              LOG.warn("Too busy to snap, skipping");
            } else {
              snapInProcess = new ZooKeeperThread("Snapshot Thread") {
                public void run() {
                  try {
                    zks.takeSnapshot();
                  } catch(Exception e) {
                    LOG.warn("Unexpected exception", e);
                  }
                }
              };
              // 启动新线程保存快照
              snapInProcess.start();
            }
            logCount = 0;
          }
        } else if (toFlush.isEmpty()) {
          // 到这里其实请求只是一个读请求，而且前面没有堆积请求，直接传递给下一个处理器
          if (nextProcessor != null) {
            nextProcessor.processRequest(si);
            if (nextProcessor instanceof Flushable) {
              ((Flushable)nextProcessor).flush();
            }
          }
          continue;
        }
        // 4 保存到队列
        toFlush.add(si);
        // 批量
        if (toFlush.size() > 1000) {
          // 5 这个操作会将日志强制刷写到磁盘，然后将toFlush队列传递给下个处理器
          flush(toFlush);
        }
      }
    }
  } catch (Throwable t) {
    handleException(this.getName(), t);
    running = false;
  }
  LOG.info("SyncRequestProcessor exited!");
}
```
1.写快照的时机半随机，为了防止集群中所有的机器一起写快照（消耗性能），造成整个集群响应变慢
2.将日志写入磁盘，实际上只调用了flush方法，将数据写入流中
3.保存的日志数量过多，则滚动日志（重新创建一个日志文件，写入）并开启一条线程保存快照
4.将request加入到toFlush队列
5.toFlush队列数量达到限制，日志强制刷写到磁盘，然后将toFlush队列传递给下个处理器。因为写磁盘的性能消耗，所以会在请求队列处理完或者堆积的toFlush队列数量过多才批量刷写磁盘，SyncRequestProcessor#flush方法不能频繁调用。

##### 7.请求链FinalRequestProcessor finalProcessor处理请求
在ZooKeeperServer#setupRequestProcessors方法中可以看到，finalProcessor并不是以线程启动，其processRequest方法处理事务并将响应发回给客户端。
```
synchronized (zks.outstandingChanges) {
  //1 进入while块实际上说明已经出错了
  while (!zks.outstandingChanges.isEmpty()
         && zks.outstandingChanges.get(0).zxid <= request.zxid) {
    //remove
    ...
  }
  // 2 说明是事务操作，处理事务
  if (request.hdr != null) {
    TxnHeader hdr = request.hdr;
    Record txn = request.txn;
    // 处理结果以 ProcessTxnResult rc返回
    rc = zks.processTxn(hdr, txn);
  }
  // 3 需要投票的包，实际上就是事务操作，添加Proposal
  if (Request.isQuorum(request.type)) {
    zks.getZKDatabase().addCommittedProposal(request);
  }
}
...
// 4 创建响应
switch (request.type) {
 ...
  case OpCode.setData: {
    lastOp = "SETD";
    rsp = new SetDataResponse(rc.stat);
    err = Code.get(rc.err);
    break;
  }
}
...
// 5 发送响应
long lastZxid = zks.getZKDatabase().getDataTreeLastProcessedZxid();
ReplyHeader hdr = new ReplyHeader(request.cxid, lastZxid, err.intValue());
cnxn.sendResponse(hdr, rsp, "response");
```
1.进入while块实际上说明已经出错了，因为outstandingChanges是顺序添加的，前面的zxid肯定要比后面的小，如果第0个比当前的request.zxid小，则这个是出错了
2.事务操作，处理事务，zks.processTxn处理事务并将结果返回
3.需要投票的请求，实际上就是事务操作，添加Proposal
4.创建响应，事务请求根据事务操作的结果创建，非事务请求直接获取节点数据
5.发送响应

##### 8.数据节点操作
在第7步中，对于数据节点的操作，这里详细分析
```
rc = zks.processTxn(hdr, txn);

// ZooKeeperServer#processTxn
public ProcessTxnResult processTxn(TxnHeader hdr, Record txn) {
  ...
  rc = getZKDatabase().processTxn(hdr, txn);
  ...
  return rc;
}
```
通过调用DataTree#processTxn方法保存数据到DataTree中

设置数据：
```
case OpCode.setData:
  SetDataTxn setDataTxn = (SetDataTxn) txn;
  rc.path = setDataTxn.getPath();
  rc.stat = setData(setDataTxn.getPath(), setDataTxn.getData(),
                    setDataTxn.getVersion(), header.getZxid(), 
                    header.getTime());
break;
```
调用DataTree#setData方法设置数据
```
public Stat setData(String path, byte data[], int version, long zxid, long time)
  throws KeeperException.NoNodeException {
    Stat s = new Stat();
    DataNode n = nodes.get(path);
    if (n == null) {
        throw new KeeperException.NoNodeException();
    }
    byte lastdata[] = null;
     // 设置数据
    synchronized (n) {
        lastdata = n.data;
        n.data = data;
        n.stat.setMtime(time);
        n.stat.setMzxid(zxid);
        n.stat.setVersion(version);
        n.copyStat(s);
    }
    // now update if the path is in a quota subtree.
    String lastPrefix;
    if((lastPrefix = getMaxPrefixWithQuota(path)) != null) {
      this.updateBytes(lastPrefix, (data == null ? 0 : data.length)
          - (lastdata == null ? 0 : lastdata.length));
    }
     // 触发watch
    dataWatches.triggerWatch(path, EventType.NodeDataChanged);
    return s;
}
```
设置数据

触发watch，就是取出对应path的watcher，然后调用
```
watchers = watchTable.remove(path);
for (Watcher w : watchers) {
  if (supress != null && supress.contains(w)) {
    continue;
  }
  w.process(e);
}
```
需要注意的依然是：
1. remove方法，也就是一次性的；2. watcher是什么？watcher实际上是cnxn也就客户端连接，在设置watcher的时候可以看到，可以看看NIOServerCnxn#process方法
```
synchronized public void process(WatchedEvent event) {
    WatcherEvent e = event.getWrapper();
    sendResponse(h, e, "notification");
}
```
服务端发送的是WatchedEvent，是没有数据的，也就是说客户端需要根据event中的信息具体处理。

获取数据：
```
case OpCode.getData: {
  ...
 //  watcher传入的是cnxn
  byte b[] = zks.getZKDatabase().getData(getDataRequest.getPath(), stat,
                                         getDataRequest.getWatch() ? cnxn : null);
  rsp = new GetDataResponse(b, stat);
  break;
}
```
如果getWatch方法返回true，则将cnxn以watcher传参，DataTree#getData方法中
```
public byte[] getData(String path, Stat stat, Watcher watcher)
  throws KeeperException.NoNodeException {
  // 1 获取节点数据
  DataNode n = nodes.get(path);
  if (n == null) {
    throw new KeeperException.NoNodeException();
  }
  synchronized (n) {
    n.copyStat(stat);
    // 添加watcher
    if (watcher != null) {
      // 添加watcher
      dataWatches.addWatch(path, watcher);
    }
    return n.data;
  }
}
```
* 1.获取节点数据
* 2.添加watcher，这里watcher是cnxn

至此，单机版服务端处理请求流程结束。

### 参考
https://www.jianshu.com/p/75fb98042cca