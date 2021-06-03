# [【Zookeeper】源码分析之持久化（三）之 FileTxnSnapLog](https://www.cnblogs.com/leesf456/p/6285703.html)

**一、前言**

前面分析了 FileSnap，接着继续分析 FileTxnSnapLog 源码，其封装了 TxnLog 和 SnapShot，其在持久化过程中是一个帮助类。

**二、FileTxnSnapLog 源码分析**

2.1 类的属性
[![复制代码]()]("复制代码")

```
public classFileTxnSnapLog {//the direcotry containing the//the transaction logs//日志文件目录 private finalFile dataDir;//the directory containing the//the snapshot directory//快照文件目录 private finalFile snapDir;//事务日志 privateTxnLog txnLog;//快照 privateSnapShot snapLog;//版本号 public final static int VERSION = 2;//版本 public final static String version = "version-";//Logger private static final Logger LOG = LoggerFactory.getLogger(FileTxnSnapLog.class); }
```

[![复制代码]()]("复制代码")

说明：类的属性中包含了 TxnLog 和 SnapShot 接口，即对 FileTxnSnapLog 的很多操作都会转发给 TxnLog 和 SnapLog 进行操作，这是一种典型的组合方法。

2.2 内部类

FileTxnSnapLog 包含了 PlayBackListener 内部类，用来接收事务应用过程中的回调，在 Zookeeper 数据恢复后期，会有事务修正过程，此过程会回调 PlayBackListener 来进行对应的数据修正。其源码如下

```
public interfacePlayBackListener {voidonTxnLoaded(TxnHeader hdr, Record rec); }
```

说明：在完成事务操作后，会调用到 onTxnLoaded 方法进行相应的处理。

2.3 构造函数

FileTxnSnapLog 的构造函数如下
[![复制代码]()]("复制代码")

```
public FileTxnSnapLog(File dataDir, File snapDir) throwsIOException { LOG.debug("Opening datadir:{} snapDir:{}", dataDir, snapDir);//在datadir和snapdir下生成version-2目录 this.dataDir = new File(dataDir, version +VERSION);this.snapDir = new File(snapDir, version +VERSION);if (!this.dataDir.exists()) { //datadir存在但无法创建目录，则抛出异常 if (!this.dataDir.mkdirs()) {throw new IOException("Unable to create data directory " + this.dataDir); } }if (!this.snapDir.exists()) { //snapdir存在但无法创建目录，则抛出异常 if (!this.snapDir.mkdirs()) {throw new IOException("Unable to create snap directory " + this.snapDir); } }//给属性赋值 txnLog = new FileTxnLog(this.dataDir); snapLog= new FileSnap(this.snapDir); }
```

[![复制代码]()]("复制代码")

说明：对于构造函数而言，其会在传入的 datadir 和 snapdir 目录下新生成 version-2 的目录，并且会判断目录是否创建成功，之后会创建 txnLog 和 snapLog。

2.4 核心函数分析

1. restore 函数
   [![复制代码]()]("复制代码")

```
public long restore(DataTree dt, Map<Long, Integer>sessions, PlayBackListener listener)throwsIOException {//根据snap文件反序列化dt和sessions snapLog.deserialize(dt, sessions);// FileTxnLog txnLog = newFileTxnLog(dataDir);//获取比最后处理的zxid+1大的log文件的迭代器 TxnIterator itr = txnLog.read(dt.lastProcessedZxid+1);//最大的zxid long highestZxid =dt.lastProcessedZxid; TxnHeader hdr;try{while (true) {//iterator points to//the first valid txn when initialized//itr 在 read 函数调用后就已经指向第一个合法的事务//获取事务头 hdr =itr.getHeader();if (hdr == null) { //事务头为空//empty logs//表示日志文件为空 returndt.lastProcessedZxid; }if (hdr.getZxid() < highestZxid && highestZxid != 0) { //事务头的 zxid 小于 snapshot 中的最大 zxid 并且其不为 0，则会报错 LOG.error("{}(higestZxid) > {}(next log) for type {}",newObject[] { highestZxid, hdr.getZxid(), hdr.getType() }); }else { //重新赋值 highestZxid highestZxid =hdr.getZxid(); }try{//在 datatree 上处理事务 processTransaction(hdr,dt,sessions, itr.getTxn()); }catch(KeeperException.NoNodeException e) {throw new IOException("Failed to process transaction type: " +hdr.getType()+ " error: " +e.getMessage(), e); }//每处理完一个事务都会进行回调 listener.onTxnLoaded(hdr, itr.getTxn());if (!itr.next()) //已无事务，跳出循环 break; } }finally{if (itr != null) { //迭代器不为空，则关闭 itr.close(); } }//返回最高的 zxid returnhighestZxid; }
```

[![复制代码]()]("复制代码")

说明：restore 用于恢复 datatree 和 sessions，其步骤大致如下

① 根据 snapshot 文件反序列化 datatree 和 sessions，进入 ②

② 获取比 snapshot 文件中的 zxid+1 大的 log 文件的迭代器，以对 log 文件中的事务进行迭代，进入 ③

③ 迭代 log 文件的每个事务，并且将该事务应用在 datatree 中，同时会调用 onTxnLoaded 函数进行后续处理，进入 ④

④ 关闭迭代器，返回 log 文件中最后一个事务的 zxid（作为最高的 zxid）

其中会调用到 FileTxnLog 的 read 函数，read 函数在 FileTxnLog 中已经进行过分析，会调用 processTransaction 函数，其源码如下
[![复制代码]()]("复制代码")

```
public voidprocessTransaction(TxnHeader hdr,DataTree dt, Map<Long, Integer>sessions, Record txn)throwsKeeperException.NoNodeException {//事务处理结果 ProcessTxnResult rc;switch (hdr.getType()) { //确定事务类型 case OpCode.createSession: //创建会话//添加进会话 sessions.put(hdr.getClientId(), ((CreateSessionTxn) txn).getTimeOut());if(LOG.isTraceEnabled()) { ZooTrace.logTraceMessage(LOG,ZooTrace.SESSION_TRACE_MASK,"playLog --- create session in log: 0x" +Long.toHexString(hdr.getClientId())+ " with timeout: " +((CreateSessionTxn) txn).getTimeOut()); }//give dataTree a chance to sync its lastProcessedZxid//处理事务 rc =dt.processTxn(hdr, txn);break;case OpCode.closeSession: //关闭会话//会话中移除 sessions.remove(hdr.getClientId());if(LOG.isTraceEnabled()) { ZooTrace.logTraceMessage(LOG,ZooTrace.SESSION_TRACE_MASK,"playLog --- close session in log: 0x" +Long.toHexString(hdr.getClientId())); }//处理事务 rc =dt.processTxn(hdr, txn);break;default://处理事务 rc =dt.processTxn(hdr, txn); }/\/*\/*- Snapshots are lazily created. So when a snapshot is in progress, - there is a chance for later transactions to make into the - snapshot. Then when the snapshot is restored, NONODE/NODEEXISTS - errors could occur. It should be safe to ignore these.\/*/ if (rc.err != Code.OK.intValue()) { //忽略处理结果中可能出现的错误 LOG.debug("Ignoring processTxn failure hdr:" +hdr.getType()+ ", error: " + rc.err + ", path: " +rc.path); } }
```

[![复制代码]()]("复制代码")

说明：processTransaction 会根据事务头中记录的事务类型（createSession、closeSession、其他类型）来进行相应的操作，对于 createSession 类型而言，其会将会话和超时时间添加至会话 map 中，对于 closeSession 而言，会话 map 会根据客户端的 id 号删除其会话，同时，所有的操作都会调用到 dt.processTxn 函数，其源码如下

![](https://images.cnblogs.com/OutliningIndicators/ContractedBlock.gif)![](https://images.cnblogs.com/OutliningIndicators/ExpandedBlockStart.gif)

[![复制代码]()]("复制代码")

```
publicProcessTxnResult processTxn(TxnHeader header, Record txn) {//事务处理结果 ProcessTxnResult rc = newProcessTxnResult();try{//从事务头中解析出相应属性并保存至 rc 中 rc.clientId =header.getClientId(); rc.cxid=header.getCxid(); rc.zxid=header.getZxid(); rc.type=header.getType(); rc.err= 0; rc.multiResult= null;switch (header.getType()) { //确定事务类型 case OpCode.create: //创建结点//显示转化 CreateTxn createTxn =(CreateTxn) txn;//获取创建结点路径 rc.path =createTxn.getPath();//创建结点 createNode( createTxn.getPath(), createTxn.getData(), createTxn.getAcl(), createTxn.getEphemeral()? header.getClientId() : 0, createTxn.getParentCVersion(), header.getZxid(), header.getTime());break;case OpCode.delete: //删除结点//显示转化 DeleteTxn deleteTxn =(DeleteTxn) txn;//获取删除结点路径 rc.path =deleteTxn.getPath();//删除结点 deleteNode(deleteTxn.getPath(), header.getZxid());break;case OpCode.setData: //写入数据//显示转化 SetDataTxn setDataTxn =(SetDataTxn) txn;//获取写入数据结点路径 rc.path =setDataTxn.getPath();//写入数据 rc.stat =setData(setDataTxn.getPath(), setDataTxn .getData(), setDataTxn.getVersion(), header .getZxid(), header.getTime());break;case OpCode.setACL: //设置 ACL//显示转化 SetACLTxn setACLTxn =(SetACLTxn) txn;//获取路径 rc.path =setACLTxn.getPath();//设置 ACL rc.stat =setACL(setACLTxn.getPath(), setACLTxn.getAcl(), setACLTxn.getVersion());break;case OpCode.closeSession: //关闭会话//关闭会话 killSession(header.getClientId(), header.getZxid());break;case OpCode.error: //错误//显示转化 ErrorTxn errTxn =(ErrorTxn) txn;//记录错误 rc.err =errTxn.getErr();break;case OpCode.check: //检查//显示转化 CheckVersionTxn checkTxn =(CheckVersionTxn) txn;//获取路径 rc.path =checkTxn.getPath();break;case OpCode.multi: //多个事务//显示转化 MultiTxn multiTxn =(MultiTxn) txn ;//获取事务列表 List<Txn> txns =multiTxn.getTxns(); rc.multiResult= new ArrayList<ProcessTxnResult>();boolean failed = false;for (Txn subtxn : txns) { //遍历事务列表 if (subtxn.getType() ==OpCode.error) { failed= true;break; } }boolean post_failed = false;for (Txn subtxn : txns) { //遍历事务列表，确定每个事务类型并进行相应操作//处理事务的数据 ByteBuffer bb =ByteBuffer.wrap(subtxn.getData()); Record record= null;switch(subtxn.getType()) {caseOpCode.create: record= newCreateTxn();break;caseOpCode.delete: record= newDeleteTxn();break;caseOpCode.setData: record= newSetDataTxn();break;caseOpCode.error: record= newErrorTxn(); post_failed= true;break;caseOpCode.check: record= newCheckVersionTxn();break;default:throw new IOException("Invalid type of op: " +subtxn.getType()); }assert(record != null);//将bytebuffer转化为record(初始化record的相关属性) ByteBufferInputStream.byteBuffer2Record(bb, record);if (failed && subtxn.getType() != OpCode.error){ //失败并且不为 error 类型 int ec = post_failed ?Code.RUNTIMEINCONSISTENCY.intValue() : Code.OK.intValue(); subtxn.setType(OpCode.error); record= newErrorTxn(ec); }if (failed) { //失败 assert(subtxn.getType() ==OpCode.error) ; }//生成事务头 TxnHeader subHdr = newTxnHeader(header.getClientId(), header.getCxid(), header.getZxid(), header.getTime(), subtxn.getType());//递归调用处理事务 ProcessTxnResult subRc =processTxn(subHdr, record);//保存处理结果 rc.multiResult.add(subRc);if (subRc.err != 0 && rc.err == 0) { rc.err=subRc.err ; } }break; } }catch(KeeperException e) {if(LOG.isDebugEnabled()) { LOG.debug("Failed: " + header + ":" +txn, e); } rc.err=e.code().intValue(); }catch(IOException e) {if(LOG.isDebugEnabled()) { LOG.debug("Failed: " + header + ":" +txn, e); } }/\/*- A snapshot might be in progress while we are modifying the data - tree. If we set lastProcessedZxid prior to making corresponding - change to the tree, then the zxid associated with the snapshot - file will be ahead of its contents. Thus, while restoring from - the snapshot, the restore method will not apply the transaction - for zxid associated with the snapshot file, since the restore - method assumes that transaction to be present in the snapshot. - - To avoid this, we first apply the transaction and then modify - lastProcessedZxid. During restore, we correctly handle the - case where the snapshot contains data ahead of the zxid associated - with the file.\/*/ //事务处理结果中保存的 zxid 大于已经被处理的最大的 zxid，则重新赋值 if (rc.zxid >lastProcessedZxid) { lastProcessedZxid=rc.zxid; }/\/*- Snapshots are taken lazily. It can happen that the child - znodes of a parent are created after the parent - is serialized. Therefore, while replaying logs during restore, a - create might fail because the node was already - created. - - After seeing this failure, we should increment - the cversion of the parent znode since the parent was serialized - before its children. - - Note, such failures on DT should be seen only during - restore.\/*/ if (header.getType() == OpCode.create &&rc.err== Code.NODEEXISTS.intValue()) { //处理在恢复数据过程中的结点创建操作 LOG.debug("Adjusting parent cversion for Txn: " + header.getType() + " path:" + rc.path + " err: " +rc.err);int lastSlash = rc.path.lastIndexOf('/'); String parentName= rc.path.substring(0, lastSlash); CreateTxn cTxn=(CreateTxn)txn;try{ setCversionPzxid(parentName, cTxn.getParentCVersion(), header.getZxid()); }catch(KeeperException.NoNodeException e) { LOG.error("Failed to set parent cversion for: " +parentName, e); rc.err=e.code().intValue(); } }else if (rc.err !=Code.OK.intValue()) { LOG.debug("Ignoring processTxn failure hdr: " + header.getType() + " : error: " +rc.err); }returnrc; }
```

[![复制代码]()]("复制代码")
View Code

说明：processTxn 用于处理事务，即将事务操作应用到 DataTree 内存数据库中，以恢复成最新的数据。

2. save 函数
   [![复制代码]()]("复制代码")

```
public voidsave(DataTree dataTree, ConcurrentHashMap<Long, Integer>sessionsWithTimeouts)throwsIOException {//获取最后处理的zxid long lastZxid =dataTree.lastProcessedZxid;//生成snapshot文件 File snapshotFile = newFile(snapDir, Util.makeSnapshotName(lastZxid)); LOG.info("Snapshotting: 0x{} to {}", Long.toHexString(lastZxid), snapshotFile);//序列化datatree、sessionsWithTimeouts至snapshot文件 snapLog.serialize(dataTree, sessionsWithTimeouts, snapshotFile); }
```

[![复制代码]()]("复制代码")

说明：save 函数用于将 sessions 和 datatree 保存至 snapshot 文件中，其大致步骤如下

① 获取内存数据库中已经处理的最新的 zxid，进入 ②

② 根据 zxid 和快照目录生成 snapshot 文件，进入 ③

③ 将 datatree（内存数据库）、sessionsWithTimeouts 序列化至快照文件。

其他的函数或多或少都是调用 TxnLog 和 SnapLog 中的相应函数，之前已经进行过分析，这里不再累赘。

**三、总结**

本篇博文分析了 FileTxnSnapLog 的源码，其主要封装了 TxnLog 和 SnapLog 来进行相应的处理，其提供了从 snapshot 文件和 log 文件中恢复内存数据库的接口，源码相对而言较为简单，也谢谢各位园友的观看~

PS:如果您觉得阅读本文对您有帮助，请点一下**“推荐”**按钮，您的**“推荐”**，将会是我不竭的动力！
作者：[**leesf**](http://www.cnblogs.com/leesf456/) **掌控之中，才会成功；掌控之外，注定失败。**
出处：[http://www.cnblogs.com/leesf456/](http://www.cnblogs.com/leesf456/)
本文版权归作者和博客园共有，欢迎转载，但未经作者同意必须保留此段声明，且在文章页面明显位置给出原文连接，否则保留追究法律责任的权利。
如果觉得本文对您有帮助，您可以请我喝杯咖啡!
![](https://files.cnblogs.com/files/leesf456/weixin.gif) ![](https://files.cnblogs.com/files/leesf456/alipay.gif)
标签: [Zookeeper](https://www.cnblogs.com/leesf456/tag/Zookeeper/), [源码分析](https://www.cnblogs.com/leesf456/tag/%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90/)

[好文要顶]() [关注我]() [收藏该文]() [![](https://common.cnblogs.com/images/icon_weibo_24.png)]("分享至新浪微博") [![](https://common.cnblogs.com/images/wechat.png)]("分享至微信")

[![](https://pic.cnblogs.com/face/616953/20160324090549.png)](https://home.cnblogs.com/u/leesf456/)

[leesf](https://home.cnblogs.com/u/leesf456/)
[关注 - 0](https://home.cnblogs.com/u/leesf456/followees/)
[粉丝 - 744](https://home.cnblogs.com/u/leesf456/followers/)

[+加关注]()
1

0
[«](https://www.cnblogs.com/leesf456/p/6285014.html) 上一篇： [【Zookeeper】源码分析之持久化（二）之 FileSnap](https://www.cnblogs.com/leesf456/p/6285014.html '发布于 2017-01-14 11:51')
[»](https://www.cnblogs.com/leesf456/p/6286827.html) 下一篇： [【Zookeeper】源码分析之 Watcher 机制（一）](https://www.cnblogs.com/leesf456/p/6286827.html '发布于 2017-01-15 17:02')

posted @ 2017-01-14 18:31 [leesf](https://www.cnblogs.com/leesf456/) 阅读(1676) 评论(0) [编辑](https://i.cnblogs.com/EditPosts.aspx?postid=6285703) [收藏]() []()

[]()

[刷新评论]()[刷新页面]()[返回顶部]()
注册用户登录后才能发表评论，请 [登录]() 或 [注册]()， [访问](https://www.cnblogs.com/) 网站首页。

[【推荐】超 50 万行 VC++源码: 大型组态工控、电力仿真 CAD 与 GIS 源码库](http://www.ucancode.com/index.htm)
[【推荐】阿里云双 11 返场来袭，热门产品低至一折等你来抢！](http://click.aliyun.com/m/1000081987/)
[【活动】开发者上云必备，腾讯云 1 核 4G 2M 云服务器 11 元/月起](https://cloud.tencent.com/act/developer?fromSource=gwzcw.3196335.3196335.3196335&utm_medium=cpc&utm_id=gwzcw.3196335.3196335.3196335)
[【推荐】百度智能云岁末感恩季，明星产品低至 1 元新老用户畅享](https://cloud.baidu.com/campaign/Promotion-20191111/index.html?track=cp:dsp|pf:pc|pp:chui-bokeyuan-huodong-19shuangshiyiganenji-BCC-cpaxingshi-191210|pu:cpa-xingshi|ci:2019syj|kw:2172212)
[【活动】京东云限时优惠 1.5 折购云主机，最高返价值 1000 元礼品！](https://www.jdcloud.com/cn/activity/newUser?utm_source=DMT_cnblogs&utm_medium=CH&utm_campaign=09vm&utm_term=Virtual-Machines)
**相关博文：**
· [【Zookeeper】源码分析之序列化](https://www.cnblogs.com/leesf456/p/6278853.html '【Zookeeper】源码分析之序列化')
· [Zookeeper 数据与存储](https://www.cnblogs.com/hehheai/p/6506835.html 'Zookeeper数据与存储')
· [【分布式】Zookeeper 数据与存储](https://www.cnblogs.com/leesf456/p/6179118.html '【分布式】Zookeeper数据与存储')
· [【Zookeeper】源码分析之 Leader 选举（二）之 FastLeaderElection](https://www.cnblogs.com/leesf456/p/6508185.html '【Zookeeper】源码分析之Leader选举（二）之FastLeaderElection')
· [【JUC】JDK1.8 源码分析之 ReentrantLock（三）](https://www.cnblogs.com/leesf456/p/5383609.html '【JUC】JDK1.8源码分析之ReentrantLock（三）')
» [更多推荐...](https://recomm.cnblogs.com/blogpost/6285703)

[开放下载！《长安十二时辰》爆款背后的优酷技术秘籍首次公开](https://developer.aliyun.com/article/717197?utm_content=g_1000088944)

**最新 IT 新闻**:
· [“鬼压床”压你的，到底是什么鬼？]()
· [开源技术社区创业者陈智宏郁结自杀：享年 42 岁]()
· [开源在不断发展，但似乎有些跑偏？]()
· [你敢坐吗？空客首次实现飞机全自动起飞：基于图像识别]()
· [寻找下个 SpaceX！太空领域创业投资创历史新高]()
» [更多新闻...](https://news.cnblogs.com/ 'IT 新闻')
