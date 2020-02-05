<div id="mainContent">
<div class="forFlow">
<div id="post_detail">
<!--done-->
<div id="topics">
<div class="post">
<h1 class="postTitle">
    
<a id="cb_post_title_url" class="postTitle2" href="https://www.cnblogs.com/leesf456/p/6285703.html">【Zookeeper】源码分析之持久化（三）之FileTxnSnapLog</a>

</h1>
<div class="clear"></div>
<div class="postBody">
    
<div id="cnblogs_post_body" class="blogpost-body ">
<p><strong>一、前言</strong></p>
<p>　　前面分析了FileSnap，接着继续分析FileTxnSnapLog源码，其封装了TxnLog和SnapShot，其在持久化过程中是一个帮助类。</p>
<p><strong>二、FileTxnSnapLog源码分析</strong></p>
<p>　　<span style="color: #ff0000;">2.1 类的属性　</span>　</p>
<div class="cnblogs_code"><div class="cnblogs_code_toolbar"><span class="cnblogs_code_copy"><a href="javascript:void(0);" onclick="copyCnblogsCode(this)" title="复制代码"><img src="//common.cnblogs.com/images/copycode.gif" alt="复制代码"></a></span></div>
<pre><span style="color: #0000ff;">public</span> <span style="color: #0000ff;">class</span><span style="color: #000000;"> FileTxnSnapLog {
</span><span style="color: #008000;">//</span><span style="color: #008000;">the direcotry containing the 
</span><span style="color: #008000;">//</span><span style="color: #008000;">the transaction logs
</span><span style="color: #008000;">//</span><span style="color: #008000;"> 日志文件目录</span>
<span style="color: #0000ff;">private</span> <span style="color: #0000ff;">final</span><span style="color: #000000;"> File dataDir;
</span><span style="color: #008000;">//</span><span style="color: #008000;">the directory containing the
</span><span style="color: #008000;">//</span><span style="color: #008000;">the snapshot directory
</span><span style="color: #008000;">//</span><span style="color: #008000;"> 快照文件目录</span>
<span style="color: #0000ff;">private</span> <span style="color: #0000ff;">final</span><span style="color: #000000;"> File snapDir;
</span><span style="color: #008000;">//</span><span style="color: #008000;"> 事务日志</span>
<span style="color: #0000ff;">private</span><span style="color: #000000;"> TxnLog txnLog;
</span><span style="color: #008000;">//</span><span style="color: #008000;"> 快照</span>
<span style="color: #0000ff;">private</span><span style="color: #000000;"> SnapShot snapLog;
</span><span style="color: #008000;">//</span><span style="color: #008000;"> 版本号</span>
<span style="color: #0000ff;">public</span> <span style="color: #0000ff;">final</span> <span style="color: #0000ff;">static</span> <span style="color: #0000ff;">int</span> VERSION = 2<span style="color: #000000;">;
</span><span style="color: #008000;">//</span><span style="color: #008000;"> 版本</span>
<span style="color: #0000ff;">public</span> <span style="color: #0000ff;">final</span> <span style="color: #0000ff;">static</span> String version = "version-"<span style="color: #000000;">;

</span><span style="color: #008000;">//</span><span style="color: #008000;"> Logger</span>
<span style="color: #0000ff;">private</span> <span style="color: #0000ff;">static</span> <span style="color: #0000ff;">final</span> Logger LOG = LoggerFactory.getLogger(FileTxnSnapLog.<span style="color: #0000ff;">class</span><span style="color: #000000;">);
}</span></pre>
<div class="cnblogs_code_toolbar"><span class="cnblogs_code_copy"><a href="javascript:void(0);" onclick="copyCnblogsCode(this)" title="复制代码"><img src="//common.cnblogs.com/images/copycode.gif" alt="复制代码"></a></span></div></div>
<p>　　说明：类的属性中包含了TxnLog和SnapShot接口，即对FileTxnSnapLog的很多操作都会转发给TxnLog和SnapLog进行操作，这是一种典型的组合方法。</p>
<p>　　<span style="color: #ff0000;">2.2 内部类</span></p>
<p>　　FileTxnSnapLog包含了PlayBackListener内部类，用来接收事务应用过程中的回调，在Zookeeper数据恢复后期，会有事务修正过程，此过程会回调PlayBackListener来进行对应的数据修正。其源码如下　</p>
<div class="cnblogs_code">
<pre><span style="color: #0000ff;">public</span> <span style="color: #0000ff;">interface</span><span style="color: #000000;"> PlayBackListener {
</span><span style="color: #0000ff;">void</span><span style="color: #000000;"> onTxnLoaded(TxnHeader hdr, Record rec);
}</span></pre>
</div>
<p>　　说明：在完成事务操作后，会调用到onTxnLoaded方法进行相应的处理。</p>
<p>　<span style="color: #ff0000;">　2.3 构造函数</span></p>
<p>　　FileTxnSnapLog的构造函数如下　</p>
<div class="cnblogs_code"><div class="cnblogs_code_toolbar"><span class="cnblogs_code_copy"><a href="javascript:void(0);" onclick="copyCnblogsCode(this)" title="复制代码"><img src="//common.cnblogs.com/images/copycode.gif" alt="复制代码"></a></span></div>
<pre>    <span style="color: #0000ff;">public</span> FileTxnSnapLog(File dataDir, File snapDir) <span style="color: #0000ff;">throws</span><span style="color: #000000;"> IOException {
LOG.debug(</span>"Opening datadir:{} snapDir:{}"<span style="color: #000000;">, dataDir, snapDir);
</span><span style="color: #008000;">//</span><span style="color: #008000;"> 在datadir和snapdir下生成version-2目录</span>
<span style="color: #0000ff;">this</span>.dataDir = <span style="color: #0000ff;">new</span> File(dataDir, version +<span style="color: #000000;"> VERSION);
</span><span style="color: #0000ff;">this</span>.snapDir = <span style="color: #0000ff;">new</span> File(snapDir, version +<span style="color: #000000;"> VERSION);
</span><span style="color: #0000ff;">if</span> (!<span style="color: #0000ff;">this</span>.dataDir.exists()) { <span style="color: #008000;">//</span><span style="color: #008000;"> datadir存在但无法创建目录，则抛出异常</span>
<span style="color: #0000ff;">if</span> (!<span style="color: #0000ff;">this</span><span style="color: #000000;">.dataDir.mkdirs()) {
    </span><span style="color: #0000ff;">throw</span> <span style="color: #0000ff;">new</span> IOException("Unable to create data directory "
            + <span style="color: #0000ff;">this</span><span style="color: #000000;">.dataDir);
}
}
</span><span style="color: #0000ff;">if</span> (!<span style="color: #0000ff;">this</span>.snapDir.exists()) { <span style="color: #008000;">//</span><span style="color: #008000;"> snapdir存在但无法创建目录，则抛出异常</span>
<span style="color: #0000ff;">if</span> (!<span style="color: #0000ff;">this</span><span style="color: #000000;">.snapDir.mkdirs()) {
    </span><span style="color: #0000ff;">throw</span> <span style="color: #0000ff;">new</span> IOException("Unable to create snap directory "
            + <span style="color: #0000ff;">this</span><span style="color: #000000;">.snapDir);
}
}
</span><span style="color: #008000;">//</span><span style="color: #008000;"> 给属性赋值</span>
txnLog = <span style="color: #0000ff;">new</span> FileTxnLog(<span style="color: #0000ff;">this</span><span style="color: #000000;">.dataDir);
snapLog </span>= <span style="color: #0000ff;">new</span> FileSnap(<span style="color: #0000ff;">this</span><span style="color: #000000;">.snapDir);
}</span></pre>
<div class="cnblogs_code_toolbar"><span class="cnblogs_code_copy"><a href="javascript:void(0);" onclick="copyCnblogsCode(this)" title="复制代码"><img src="//common.cnblogs.com/images/copycode.gif" alt="复制代码"></a></span></div></div>
<p>　　说明：对于构造函数而言，其会在传入的datadir和snapdir目录下新生成version-2的目录，并且会判断目录是否创建成功，之后会创建txnLog和snapLog。</p>
<p>　　<span style="color: #ff0000;">2.4 核心函数分析</span></p>
<p>　　<span style="color: #0000ff;">1.&nbsp;restore函数</span>　</p>
<div class="cnblogs_code"><div class="cnblogs_code_toolbar"><span class="cnblogs_code_copy"><a href="javascript:void(0);" onclick="copyCnblogsCode(this)" title="复制代码"><img src="//common.cnblogs.com/images/copycode.gif" alt="复制代码"></a></span></div>
<pre>    <span style="color: #0000ff;">public</span> <span style="color: #0000ff;">long</span> restore(DataTree dt, Map&lt;Long, Integer&gt;<span style="color: #000000;"> sessions, 
PlayBackListener listener) </span><span style="color: #0000ff;">throws</span><span style="color: #000000;"> IOException {
</span><span style="color: #008000;">//</span><span style="color: #008000;"> 根据snap文件反序列化dt和sessions</span>
<span style="color: #000000;">        snapLog.deserialize(dt, sessions);
</span><span style="color: #008000;">//</span> 
FileTxnLog txnLog = <span style="color: #0000ff;">new</span><span style="color: #000000;"> FileTxnLog(dataDir);
</span><span style="color: #008000;">//</span><span style="color: #008000;"> 获取比最后处理的zxid+1大的log文件的迭代器</span>
TxnIterator itr = txnLog.read(dt.lastProcessedZxid+1<span style="color: #000000;">);
</span><span style="color: #008000;">//</span><span style="color: #008000;"> 最大的zxid</span>
<span style="color: #0000ff;">long</span> highestZxid =<span style="color: #000000;"> dt.lastProcessedZxid;

TxnHeader hdr;
</span><span style="color: #0000ff;">try</span><span style="color: #000000;"> {
</span><span style="color: #0000ff;">while</span> (<span style="color: #0000ff;">true</span><span style="color: #000000;">) {
    </span><span style="color: #008000;">//</span><span style="color: #008000;"> iterator points to 
    </span><span style="color: #008000;">//</span><span style="color: #008000;"> the first valid txn when initialized
    </span><span style="color: #008000;">//</span><span style="color: #008000;"> itr在read函数调用后就已经指向第一个合法的事务
    </span><span style="color: #008000;">//</span><span style="color: #008000;"> 获取事务头</span>
    hdr =<span style="color: #000000;"> itr.getHeader();
    </span><span style="color: #0000ff;">if</span> (hdr == <span style="color: #0000ff;">null</span>) { <span style="color: #008000;">//</span><span style="color: #008000;"> 事务头为空
        </span><span style="color: #008000;">//</span><span style="color: #008000;">empty logs 
        </span><span style="color: #008000;">//</span><span style="color: #008000;"> 表示日志文件为空</span>
        <span style="color: #0000ff;">return</span><span style="color: #000000;"> dt.lastProcessedZxid;
    }
    </span><span style="color: #0000ff;">if</span> (hdr.getZxid() &lt; highestZxid &amp;&amp; highestZxid != 0) { <span style="color: #008000;">//</span><span style="color: #008000;"> 事务头的zxid小于snapshot中的最大zxid并且其不为0，则会报错</span>
        LOG.error("{}(higestZxid) &gt; {}(next log) for type {}"<span style="color: #000000;">,
                </span><span style="color: #0000ff;">new</span><span style="color: #000000;"> Object[] { highestZxid, hdr.getZxid(),
                        hdr.getType() });
    } </span><span style="color: #0000ff;">else</span> { <span style="color: #008000;">//</span><span style="color: #008000;"> 重新赋值highestZxid</span>
        highestZxid =<span style="color: #000000;"> hdr.getZxid();
    }
    </span><span style="color: #0000ff;">try</span><span style="color: #000000;"> {
        </span><span style="color: #008000;">//</span><span style="color: #008000;"> 在datatree上处理事务</span>
<span style="color: #000000;">                    processTransaction(hdr,dt,sessions, itr.getTxn());
    } </span><span style="color: #0000ff;">catch</span><span style="color: #000000;">(KeeperException.NoNodeException e) {
       </span><span style="color: #0000ff;">throw</span> <span style="color: #0000ff;">new</span> IOException("Failed to process transaction type: " +<span style="color: #000000;">
             hdr.getType() </span>+ " error: " +<span style="color: #000000;"> e.getMessage(), e);
    }
    </span><span style="color: #008000;">//</span><span style="color: #008000;"> 每处理完一个事务都会进行回调</span>
<span style="color: #000000;">                listener.onTxnLoaded(hdr, itr.getTxn());
    </span><span style="color: #0000ff;">if</span> (!itr.next()) <span style="color: #008000;">//</span><span style="color: #008000;"> 已无事务，跳出循环</span>
        <span style="color: #0000ff;">break</span><span style="color: #000000;">;
}
} </span><span style="color: #0000ff;">finally</span><span style="color: #000000;"> {
</span><span style="color: #0000ff;">if</span> (itr != <span style="color: #0000ff;">null</span>) { <span style="color: #008000;">//</span><span style="color: #008000;"> 迭代器不为空，则关闭</span>
<span style="color: #000000;">                itr.close();
}
}
</span><span style="color: #008000;">//</span><span style="color: #008000;"> 返回最高的zxid</span>
<span style="color: #0000ff;">return</span><span style="color: #000000;"> highestZxid;
}</span></pre>
<div class="cnblogs_code_toolbar"><span class="cnblogs_code_copy"><a href="javascript:void(0);" onclick="copyCnblogsCode(this)" title="复制代码"><img src="//common.cnblogs.com/images/copycode.gif" alt="复制代码"></a></span></div></div>
<p>　　说明：restore用于恢复datatree和sessions，其步骤大致如下</p>
<p>　　① 根据snapshot文件反序列化datatree和sessions，进入②</p>
<p>　　② 获取比snapshot文件中的zxid+1大的log文件的迭代器，以对log文件中的事务进行迭代，进入③</p>
<p>　　③ 迭代log文件的每个事务，并且将该事务应用在datatree中，同时会调用onTxnLoaded函数进行后续处理，进入④</p>
<p>　　④ 关闭迭代器，返回log文件中最后一个事务的zxid（作为最高的zxid）</p>
<p>　　其中会调用到FileTxnLog的read函数，read函数在FileTxnLog中已经进行过分析，会调用processTransaction函数，其源码如下　</p>
<div class="cnblogs_code"><div class="cnblogs_code_toolbar"><span class="cnblogs_code_copy"><a href="javascript:void(0);" onclick="copyCnblogsCode(this)" title="复制代码"><img src="//common.cnblogs.com/images/copycode.gif" alt="复制代码"></a></span></div>
<pre>    <span style="color: #0000ff;">public</span> <span style="color: #0000ff;">void</span><span style="color: #000000;"> processTransaction(TxnHeader hdr,DataTree dt,
Map</span>&lt;Long, Integer&gt;<span style="color: #000000;"> sessions, Record txn)
</span><span style="color: #0000ff;">throws</span><span style="color: #000000;"> KeeperException.NoNodeException {
</span><span style="color: #008000;">//</span><span style="color: #008000;"> 事务处理结果</span>
<span style="color: #000000;">        ProcessTxnResult rc;
</span><span style="color: #0000ff;">switch</span> (hdr.getType()) { <span style="color: #008000;">//</span><span style="color: #008000;"> 确定事务类型</span>
<span style="color: #0000ff;">case</span> OpCode.createSession: <span style="color: #008000;">//</span><span style="color: #008000;"> 创建会话
</span><span style="color: #008000;">//</span><span style="color: #008000;"> 添加进会话</span>
<span style="color: #000000;">            sessions.put(hdr.getClientId(),
        ((CreateSessionTxn) txn).getTimeOut());
</span><span style="color: #0000ff;">if</span><span style="color: #000000;"> (LOG.isTraceEnabled()) {
    ZooTrace.logTraceMessage(LOG,ZooTrace.SESSION_TRACE_MASK,
            </span>"playLog --- create session in log: 0x"
                    +<span style="color: #000000;"> Long.toHexString(hdr.getClientId())
                    </span>+ " with timeout: "
                    +<span style="color: #000000;"> ((CreateSessionTxn) txn).getTimeOut());
}
</span><span style="color: #008000;">//</span><span style="color: #008000;"> give dataTree a chance to sync its lastProcessedZxid
</span><span style="color: #008000;">//</span><span style="color: #008000;"> 处理事务</span>
rc =<span style="color: #000000;"> dt.processTxn(hdr, txn);
</span><span style="color: #0000ff;">break</span><span style="color: #000000;">;
</span><span style="color: #0000ff;">case</span> OpCode.closeSession: <span style="color: #008000;">//</span><span style="color: #008000;"> 关闭会话
</span><span style="color: #008000;">//</span><span style="color: #008000;"> 会话中移除</span>
<span style="color: #000000;">            sessions.remove(hdr.getClientId());
</span><span style="color: #0000ff;">if</span><span style="color: #000000;"> (LOG.isTraceEnabled()) {
    ZooTrace.logTraceMessage(LOG,ZooTrace.SESSION_TRACE_MASK,
            </span>"playLog --- close session in log: 0x"
                    +<span style="color: #000000;"> Long.toHexString(hdr.getClientId()));
}
</span><span style="color: #008000;">//</span><span style="color: #008000;"> 处理事务</span>
rc =<span style="color: #000000;"> dt.processTxn(hdr, txn);
</span><span style="color: #0000ff;">break</span><span style="color: #000000;">;
</span><span style="color: #0000ff;">default</span><span style="color: #000000;">:
</span><span style="color: #008000;">//</span><span style="color: #008000;"> 处理事务</span>
rc =<span style="color: #000000;"> dt.processTxn(hdr, txn);
}

</span><span style="color: #008000;">/**</span><span style="color: #008000;">
* Snapshots are lazily created. So when a snapshot is in progress,
* there is a chance for later transactions to make into the
* snapshot. Then when the snapshot is restored, NONODE/NODEEXISTS
* errors could occur. It should be safe to ignore these.
</span><span style="color: #008000;">*/</span>
<span style="color: #0000ff;">if</span> (rc.err != Code.OK.intValue()) { <span style="color: #008000;">//</span><span style="color: #008000;"> 忽略处理结果中可能出现的错误</span>
LOG.debug("Ignoring processTxn failure hdr:" +<span style="color: #000000;"> hdr.getType()
        </span>+ ", error: " + rc.err + ", path: " +<span style="color: #000000;"> rc.path);
}
}</span></pre>
<div class="cnblogs_code_toolbar"><span class="cnblogs_code_copy"><a href="javascript:void(0);" onclick="copyCnblogsCode(this)" title="复制代码"><img src="//common.cnblogs.com/images/copycode.gif" alt="复制代码"></a></span></div></div>
<p>　　说明：processTransaction会根据事务头中记录的事务类型（createSession、closeSession、其他类型）来进行相应的操作，对于createSession类型而言，其会将会话和超时时间添加至会话map中，对于closeSession而言，会话map会根据客户端的id号删除其会话，同时，所有的操作都会调用到dt.processTxn函数，其源码如下　　</p>
<div class="cnblogs_code" onclick="cnblogs_code_show('1f45b702-0582-41a8-b591-cc2c2383bfec')"><img id="code_img_closed_1f45b702-0582-41a8-b591-cc2c2383bfec" class="code_img_closed" src="https://images.cnblogs.com/OutliningIndicators/ContractedBlock.gif" alt="" style="display: none;"><img id="code_img_opened_1f45b702-0582-41a8-b591-cc2c2383bfec" class="code_img_opened" style="" onclick="cnblogs_code_hide('1f45b702-0582-41a8-b591-cc2c2383bfec',event)" src="https://images.cnblogs.com/OutliningIndicators/ExpandedBlockStart.gif" alt="">
<div id="cnblogs_code_open_1f45b702-0582-41a8-b591-cc2c2383bfec" class="cnblogs_code_hide" style="display: block;"><div class="cnblogs_code_toolbar"><span class="cnblogs_code_copy"><a href="javascript:void(0);" onclick="copyCnblogsCode(this)" title="复制代码"><img src="//common.cnblogs.com/images/copycode.gif" alt="复制代码"></a></span></div>
<pre>    <span style="color: #0000ff;">public</span><span style="color: #000000;"> ProcessTxnResult processTxn(TxnHeader header, Record txn)
{
</span><span style="color: #008000;">//</span><span style="color: #008000;"> 事务处理结果</span>
ProcessTxnResult rc = <span style="color: #0000ff;">new</span><span style="color: #000000;"> ProcessTxnResult();

</span><span style="color: #0000ff;">try</span><span style="color: #000000;"> {
</span><span style="color: #008000;">//</span><span style="color: #008000;"> 从事务头中解析出相应属性并保存至rc中</span>
rc.clientId =<span style="color: #000000;"> header.getClientId();
rc.cxid </span>=<span style="color: #000000;"> header.getCxid();
rc.zxid </span>=<span style="color: #000000;"> header.getZxid();
rc.type </span>=<span style="color: #000000;"> header.getType();
rc.err </span>= 0<span style="color: #000000;">;
rc.multiResult </span>= <span style="color: #0000ff;">null</span><span style="color: #000000;">;
</span><span style="color: #0000ff;">switch</span> (header.getType()) { <span style="color: #008000;">//</span><span style="color: #008000;"> 确定事务类型</span>
    <span style="color: #0000ff;">case</span> OpCode.create: <span style="color: #008000;">//</span><span style="color: #008000;"> 创建结点
        </span><span style="color: #008000;">//</span><span style="color: #008000;"> 显示转化</span>
        CreateTxn createTxn =<span style="color: #000000;"> (CreateTxn) txn;
        </span><span style="color: #008000;">//</span><span style="color: #008000;"> 获取创建结点路径</span>
        rc.path =<span style="color: #000000;"> createTxn.getPath();
        </span><span style="color: #008000;">//</span><span style="color: #008000;"> 创建结点</span>
<span style="color: #000000;">                    createNode(
                createTxn.getPath(),
                createTxn.getData(),
                createTxn.getAcl(),
                createTxn.getEphemeral() </span>? header.getClientId() : 0<span style="color: #000000;">,
                createTxn.getParentCVersion(),
                header.getZxid(), header.getTime());
        </span><span style="color: #0000ff;">break</span><span style="color: #000000;">;
    </span><span style="color: #0000ff;">case</span> OpCode.delete: <span style="color: #008000;">//</span><span style="color: #008000;"> 删除结点
        </span><span style="color: #008000;">//</span><span style="color: #008000;"> 显示转化</span>
        DeleteTxn deleteTxn =<span style="color: #000000;"> (DeleteTxn) txn;
        </span><span style="color: #008000;">//</span><span style="color: #008000;"> 获取删除结点路径</span>
        rc.path =<span style="color: #000000;"> deleteTxn.getPath();
        </span><span style="color: #008000;">//</span><span style="color: #008000;"> 删除结点</span>
<span style="color: #000000;">                    deleteNode(deleteTxn.getPath(), header.getZxid());
        </span><span style="color: #0000ff;">break</span><span style="color: #000000;">;
    </span><span style="color: #0000ff;">case</span> OpCode.setData: <span style="color: #008000;">//</span><span style="color: #008000;"> 写入数据
        </span><span style="color: #008000;">//</span><span style="color: #008000;"> 显示转化</span>
        SetDataTxn setDataTxn =<span style="color: #000000;"> (SetDataTxn) txn;
        </span><span style="color: #008000;">//</span><span style="color: #008000;"> 获取写入数据结点路径</span>
        rc.path =<span style="color: #000000;"> setDataTxn.getPath();
        </span><span style="color: #008000;">//</span><span style="color: #008000;"> 写入数据</span>
        rc.stat =<span style="color: #000000;"> setData(setDataTxn.getPath(), setDataTxn
                .getData(), setDataTxn.getVersion(), header
                .getZxid(), header.getTime());
        </span><span style="color: #0000ff;">break</span><span style="color: #000000;">;
    </span><span style="color: #0000ff;">case</span> OpCode.setACL: <span style="color: #008000;">//</span><span style="color: #008000;"> 设置ACL
        </span><span style="color: #008000;">//</span><span style="color: #008000;"> 显示转化</span>
        SetACLTxn setACLTxn =<span style="color: #000000;"> (SetACLTxn) txn;
        </span><span style="color: #008000;">//</span><span style="color: #008000;"> 获取路径</span>
        rc.path =<span style="color: #000000;"> setACLTxn.getPath();
        </span><span style="color: #008000;">//</span><span style="color: #008000;"> 设置ACL</span>
        rc.stat =<span style="color: #000000;"> setACL(setACLTxn.getPath(), setACLTxn.getAcl(),
                setACLTxn.getVersion());
        </span><span style="color: #0000ff;">break</span><span style="color: #000000;">;
    </span><span style="color: #0000ff;">case</span> OpCode.closeSession: <span style="color: #008000;">//</span><span style="color: #008000;"> 关闭会话
        </span><span style="color: #008000;">//</span><span style="color: #008000;"> 关闭会话</span>
<span style="color: #000000;">                    killSession(header.getClientId(), header.getZxid());
        </span><span style="color: #0000ff;">break</span><span style="color: #000000;">;
    </span><span style="color: #0000ff;">case</span> OpCode.error: <span style="color: #008000;">//</span><span style="color: #008000;"> 错误
        </span><span style="color: #008000;">//</span><span style="color: #008000;"> 显示转化</span>
        ErrorTxn errTxn =<span style="color: #000000;"> (ErrorTxn) txn;
        </span><span style="color: #008000;">//</span><span style="color: #008000;"> 记录错误</span>
        rc.err =<span style="color: #000000;"> errTxn.getErr();
        </span><span style="color: #0000ff;">break</span><span style="color: #000000;">;
    </span><span style="color: #0000ff;">case</span> OpCode.check: <span style="color: #008000;">//</span><span style="color: #008000;"> 检查
        </span><span style="color: #008000;">//</span><span style="color: #008000;"> 显示转化</span>
        CheckVersionTxn checkTxn =<span style="color: #000000;"> (CheckVersionTxn) txn;
        </span><span style="color: #008000;">//</span><span style="color: #008000;"> 获取路径</span>
        rc.path =<span style="color: #000000;"> checkTxn.getPath();
        </span><span style="color: #0000ff;">break</span><span style="color: #000000;">;
    </span><span style="color: #0000ff;">case</span> OpCode.multi: <span style="color: #008000;">//</span><span style="color: #008000;"> 多个事务
        </span><span style="color: #008000;">//</span><span style="color: #008000;"> 显示转化</span>
        MultiTxn multiTxn =<span style="color: #000000;"> (MultiTxn) txn ;
        </span><span style="color: #008000;">//</span><span style="color: #008000;"> 获取事务列表</span>
        List&lt;Txn&gt; txns =<span style="color: #000000;"> multiTxn.getTxns();
        rc.multiResult </span>= <span style="color: #0000ff;">new</span> ArrayList&lt;ProcessTxnResult&gt;<span style="color: #000000;">();
        </span><span style="color: #0000ff;">boolean</span> failed = <span style="color: #0000ff;">false</span><span style="color: #000000;">;
        </span><span style="color: #0000ff;">for</span> (Txn subtxn : txns) { <span style="color: #008000;">//</span><span style="color: #008000;"> 遍历事务列表</span>
            <span style="color: #0000ff;">if</span> (subtxn.getType() ==<span style="color: #000000;"> OpCode.error) {
                failed </span>= <span style="color: #0000ff;">true</span><span style="color: #000000;">;
                </span><span style="color: #0000ff;">break</span><span style="color: #000000;">;
            }
        }

        </span><span style="color: #0000ff;">boolean</span> post_failed = <span style="color: #0000ff;">false</span><span style="color: #000000;">;
        </span><span style="color: #0000ff;">for</span> (Txn subtxn : txns) { <span style="color: #008000;">//</span><span style="color: #008000;"> 遍历事务列表，确定每个事务类型并进行相应操作
            </span><span style="color: #008000;">//</span><span style="color: #008000;"> 处理事务的数据</span>
            ByteBuffer bb =<span style="color: #000000;"> ByteBuffer.wrap(subtxn.getData());
            Record record </span>= <span style="color: #0000ff;">null</span><span style="color: #000000;">;
            </span><span style="color: #0000ff;">switch</span><span style="color: #000000;"> (subtxn.getType()) {
                </span><span style="color: #0000ff;">case</span><span style="color: #000000;"> OpCode.create:
                    record </span>= <span style="color: #0000ff;">new</span><span style="color: #000000;"> CreateTxn();
                    </span><span style="color: #0000ff;">break</span><span style="color: #000000;">;
                </span><span style="color: #0000ff;">case</span><span style="color: #000000;"> OpCode.delete:
                    record </span>= <span style="color: #0000ff;">new</span><span style="color: #000000;"> DeleteTxn();
                    </span><span style="color: #0000ff;">break</span><span style="color: #000000;">;
                </span><span style="color: #0000ff;">case</span><span style="color: #000000;"> OpCode.setData:
                    record </span>= <span style="color: #0000ff;">new</span><span style="color: #000000;"> SetDataTxn();
                    </span><span style="color: #0000ff;">break</span><span style="color: #000000;">;
                </span><span style="color: #0000ff;">case</span><span style="color: #000000;"> OpCode.error:
                    record </span>= <span style="color: #0000ff;">new</span><span style="color: #000000;"> ErrorTxn();
                    post_failed </span>= <span style="color: #0000ff;">true</span><span style="color: #000000;">;
                    </span><span style="color: #0000ff;">break</span><span style="color: #000000;">;
                </span><span style="color: #0000ff;">case</span><span style="color: #000000;"> OpCode.check:
                    record </span>= <span style="color: #0000ff;">new</span><span style="color: #000000;"> CheckVersionTxn();
                    </span><span style="color: #0000ff;">break</span><span style="color: #000000;">;
                </span><span style="color: #0000ff;">default</span><span style="color: #000000;">:
                    </span><span style="color: #0000ff;">throw</span> <span style="color: #0000ff;">new</span> IOException("Invalid type of op: " +<span style="color: #000000;"> subtxn.getType());
            }
            </span><span style="color: #0000ff;">assert</span>(record != <span style="color: #0000ff;">null</span><span style="color: #000000;">);
            </span><span style="color: #008000;">//</span><span style="color: #008000;"> 将bytebuffer转化为record(初始化record的相关属性)</span>
<span style="color: #000000;">                        ByteBufferInputStream.byteBuffer2Record(bb, record);
           
            </span><span style="color: #0000ff;">if</span> (failed &amp;&amp; subtxn.getType() != OpCode.error){ <span style="color: #008000;">//</span><span style="color: #008000;"> 失败并且不为error类型</span>
                <span style="color: #0000ff;">int</span> ec = post_failed ?<span style="color: #000000;"> Code.RUNTIMEINCONSISTENCY.intValue() 
                                     : Code.OK.intValue();

                subtxn.setType(OpCode.error);
                record </span>= <span style="color: #0000ff;">new</span><span style="color: #000000;"> ErrorTxn(ec);
            }

            </span><span style="color: #0000ff;">if</span> (failed) { <span style="color: #008000;">//</span><span style="color: #008000;"> 失败</span>
                <span style="color: #0000ff;">assert</span>(subtxn.getType() ==<span style="color: #000000;"> OpCode.error) ;
            }
            
            </span><span style="color: #008000;">//</span><span style="color: #008000;"> 生成事务头</span>
            TxnHeader subHdr = <span style="color: #0000ff;">new</span><span style="color: #000000;"> TxnHeader(header.getClientId(), header.getCxid(),
                                             header.getZxid(), header.getTime(), 
                                             subtxn.getType());
            </span><span style="color: #008000;">//</span><span style="color: #008000;"> 递归调用处理事务</span>
            ProcessTxnResult subRc =<span style="color: #000000;"> processTxn(subHdr, record);
            </span><span style="color: #008000;">//</span><span style="color: #008000;"> 保存处理结果</span>
<span style="color: #000000;">                        rc.multiResult.add(subRc);
            </span><span style="color: #0000ff;">if</span> (subRc.err != 0 &amp;&amp; rc.err == 0<span style="color: #000000;">) {
                rc.err </span>=<span style="color: #000000;"> subRc.err ;
            }
        }
        </span><span style="color: #0000ff;">break</span><span style="color: #000000;">;
}
} </span><span style="color: #0000ff;">catch</span><span style="color: #000000;"> (KeeperException e) {
</span><span style="color: #0000ff;">if</span><span style="color: #000000;"> (LOG.isDebugEnabled()) {
    LOG.debug(</span>"Failed: " + header + ":" +<span style="color: #000000;"> txn, e);
}
rc.err </span>=<span style="color: #000000;"> e.code().intValue();
} </span><span style="color: #0000ff;">catch</span><span style="color: #000000;"> (IOException e) {
</span><span style="color: #0000ff;">if</span><span style="color: #000000;"> (LOG.isDebugEnabled()) {
    LOG.debug(</span>"Failed: " + header + ":" +<span style="color: #000000;"> txn, e);
}
}
</span><span style="color: #008000;">/*</span><span style="color: #008000;">
* A snapshot might be in progress while we are modifying the data
* tree. If we set lastProcessedZxid prior to making corresponding
* change to the tree, then the zxid associated with the snapshot
* file will be ahead of its contents. Thus, while restoring from
* the snapshot, the restore method will not apply the transaction
* for zxid associated with the snapshot file, since the restore
* method assumes that transaction to be present in the snapshot.
*
* To avoid this, we first apply the transaction and then modify
* lastProcessedZxid.  During restore, we correctly handle the
* case where the snapshot contains data ahead of the zxid associated
* with the file.
</span><span style="color: #008000;">*/</span>
<span style="color: #008000;">//</span><span style="color: #008000;"> 事务处理结果中保存的zxid大于已经被处理的最大的zxid，则重新赋值</span>
<span style="color: #0000ff;">if</span> (rc.zxid &gt;<span style="color: #000000;"> lastProcessedZxid) {
lastProcessedZxid </span>=<span style="color: #000000;"> rc.zxid;
}

</span><span style="color: #008000;">/*</span><span style="color: #008000;">
* Snapshots are taken lazily. It can happen that the child
* znodes of a parent are created after the parent
* is serialized. Therefore, while replaying logs during restore, a
* create might fail because the node was already
* created.
*
* After seeing this failure, we should increment
* the cversion of the parent znode since the parent was serialized
* before its children.
*
* Note, such failures on DT should be seen only during
* restore.
</span><span style="color: #008000;">*/</span>
<span style="color: #0000ff;">if</span> (header.getType() == OpCode.create &amp;&amp;<span style="color: #000000;">
    rc.err </span>== Code.NODEEXISTS.intValue()) { <span style="color: #008000;">//</span><span style="color: #008000;"> 处理在恢复数据过程中的结点创建操作</span>
LOG.debug("Adjusting parent cversion for Txn: " + header.getType() +
        " path:" + rc.path + " err: " +<span style="color: #000000;"> rc.err);
</span><span style="color: #0000ff;">int</span> lastSlash = rc.path.lastIndexOf('/'<span style="color: #000000;">);
String parentName </span>= rc.path.substring(0<span style="color: #000000;">, lastSlash);
CreateTxn cTxn </span>=<span style="color: #000000;"> (CreateTxn)txn;
</span><span style="color: #0000ff;">try</span><span style="color: #000000;"> {
    setCversionPzxid(parentName, cTxn.getParentCVersion(),
            header.getZxid());
} </span><span style="color: #0000ff;">catch</span><span style="color: #000000;"> (KeeperException.NoNodeException e) {
    LOG.error(</span>"Failed to set parent cversion for: " +<span style="color: #000000;">
          parentName, e);
    rc.err </span>=<span style="color: #000000;"> e.code().intValue();
}
} </span><span style="color: #0000ff;">else</span> <span style="color: #0000ff;">if</span> (rc.err !=<span style="color: #000000;"> Code.OK.intValue()) {
LOG.debug(</span>"Ignoring processTxn failure hdr: " + header.getType() +
      " : error: " +<span style="color: #000000;"> rc.err);
}
</span><span style="color: #0000ff;">return</span><span style="color: #000000;"> rc;
}</span></pre>
<div class="cnblogs_code_toolbar"><span class="cnblogs_code_copy"><a href="javascript:void(0);" onclick="copyCnblogsCode(this)" title="复制代码"><img src="//common.cnblogs.com/images/copycode.gif" alt="复制代码"></a></span></div></div>
<span class="cnblogs_code_collapse" style="display: none;">View Code</span></div>
<p>　　说明：processTxn用于处理事务，即将事务操作应用到DataTree内存数据库中，以恢复成最新的数据。</p>
<p>　　<span style="color: #0000ff;">2.&nbsp;save函数</span>　　</p>
<div class="cnblogs_code"><div class="cnblogs_code_toolbar"><span class="cnblogs_code_copy"><a href="javascript:void(0);" onclick="copyCnblogsCode(this)" title="复制代码"><img src="//common.cnblogs.com/images/copycode.gif" alt="复制代码"></a></span></div>
<pre>    <span style="color: #0000ff;">public</span> <span style="color: #0000ff;">void</span><span style="color: #000000;"> save(DataTree dataTree,
ConcurrentHashMap</span>&lt;Long, Integer&gt;<span style="color: #000000;"> sessionsWithTimeouts)
</span><span style="color: #0000ff;">throws</span><span style="color: #000000;"> IOException {
</span><span style="color: #008000;">//</span><span style="color: #008000;"> 获取最后处理的zxid</span>
<span style="color: #0000ff;">long</span> lastZxid =<span style="color: #000000;"> dataTree.lastProcessedZxid;
</span><span style="color: #008000;">//</span><span style="color: #008000;"> 生成snapshot文件</span>
File snapshotFile = <span style="color: #0000ff;">new</span><span style="color: #000000;"> File(snapDir, Util.makeSnapshotName(lastZxid));
LOG.info(</span>"Snapshotting: 0x{} to {}"<span style="color: #000000;">, Long.toHexString(lastZxid),
    snapshotFile);
</span><span style="color: #008000;">//</span><span style="color: #008000;"> 序列化datatree、sessionsWithTimeouts至snapshot文件</span>
<span style="color: #000000;">        snapLog.serialize(dataTree, sessionsWithTimeouts, snapshotFile);

}</span></pre>
<div class="cnblogs_code_toolbar"><span class="cnblogs_code_copy"><a href="javascript:void(0);" onclick="copyCnblogsCode(this)" title="复制代码"><img src="//common.cnblogs.com/images/copycode.gif" alt="复制代码"></a></span></div></div>
<p>　　说明：save函数用于将sessions和datatree保存至snapshot文件中，其大致步骤如下</p>
<p>　　① 获取内存数据库中已经处理的最新的zxid，进入②</p>
<p>　　② 根据zxid和快照目录生成snapshot文件，进入③</p>
<p>　　③ 将datatree（内存数据库）、sessionsWithTimeouts序列化至快照文件。</p>
<p>　　其他的函数或多或少都是调用TxnLog和SnapLog中的相应函数，之前已经进行过分析，这里不再累赘。</p>
<p><strong>三、总结</strong></p>
<p>　　本篇博文分析了FileTxnSnapLog的源码，其主要封装了TxnLog和SnapLog来进行相应的处理，其提供了从snapshot文件和log文件中恢复内存数据库的接口，源码相对而言较为简单，也谢谢各位园友的观看~</p>
</div>
<div id="MySignature" style="display: block;"><div style="margin-top: 100px">         
<p style="border-width: 2px; border-style: dashed; border-color: #000; font-family: 微软雅黑; color: black; font-size: 11px; padding: 0px 10px 10px 40px">             
PS:如果您觉得阅读本文对您有帮助，请点一下<b><font color="red">“推荐”</font></b>按钮，您的<b><font color="red">“推荐”</font></b>，将会是我不竭的动力！
<br>
作者：<a href="http://www.cnblogs.com/leesf456/" target="_blank"><b>leesf</b></a>&nbsp;&nbsp;&nbsp;&nbsp;<b>掌控之中，才会成功；掌控之外，注定失败。</b>       
<br>  
出处：<a href="http://www.cnblogs.com/leesf456/" target="_blank">http://www.cnblogs.com/leesf456/</a> 
<br>本文版权归作者和博客园共有，欢迎转载，但未经作者同意必须保留此段声明，且在文章页面明显位置给出原文连接，否则保留追究法律责任的权利。 
<br> 
<font color="red">如果觉得本文对您有帮助，您可以请我喝杯咖啡!</font>
<br>
<img src="https://files.cnblogs.com/files/leesf456/weixin.gif" height="350" width="350"> 
<img src="https://files.cnblogs.com/files/leesf456/alipay.gif" height="350" width="350">  
</p></div></div>
<div class="clear"></div>
<div id="blog_post_info_block">
<div id="EntryTag">
标签: 
<a href="https://www.cnblogs.com/leesf456/tag/Zookeeper/">Zookeeper</a>,             <a href="https://www.cnblogs.com/leesf456/tag/%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90/">源码分析</a></div>

<div id="blog_post_info">
<div id="green_channel">
<a href="javascript:void(0);" id="green_channel_digg" onclick="DiggIt(6285703,cb_blogId,1);green_channel_success(this,'谢谢推荐！');">好文要顶</a>
<a id="green_channel_follow" onclick="follow('75e00040-f3b3-e311-8d02-90b11c0b17d6');" href="javascript:void(0);">关注我</a>
<a id="green_channel_favorite" onclick="AddToWz(cb_entryId);return false;" href="javascript:void(0);">收藏该文</a>
<a id="green_channel_weibo" href="javascript:void(0);" title="分享至新浪微博" onclick="ShareToTsina()"><img src="https://common.cnblogs.com/images/icon_weibo_24.png" alt=""></a>
<a id="green_channel_wechat" href="javascript:void(0);" title="分享至微信" onclick="shareOnWechat()"><img src="https://common.cnblogs.com/images/wechat.png" alt=""></a>
</div>
<div id="author_profile">
<div id="author_profile_info" class="author_profile_info">
<a href="https://home.cnblogs.com/u/leesf456/" target="_blank"><img src="https://pic.cnblogs.com/face/616953/20160324090549.png" class="author_avatar" alt=""></a>
<div id="author_profile_detail" class="author_profile_info">
<a href="https://home.cnblogs.com/u/leesf456/">leesf</a><br>
<a href="https://home.cnblogs.com/u/leesf456/followees/">关注 - 0</a><br>
<a href="https://home.cnblogs.com/u/leesf456/followers/">粉丝 - 744</a>
</div>
</div>
<div class="clear"></div>
<div id="author_profile_honor"></div>
<div id="author_profile_follow">
    <a href="javascript:void(0);" onclick="follow('75e00040-f3b3-e311-8d02-90b11c0b17d6');return false;">+加关注</a>
</div>
</div>
<div id="div_digg">
<div class="diggit" onclick="votePost(6285703,'Digg')">
<span class="diggnum" id="digg_count">1</span>
</div>
<div class="buryit" onclick="votePost(6285703,'Bury')">
<span class="burynum" id="bury_count">0</span>
</div>
<div class="clear"></div>
<div class="diggword" id="digg_tips">
</div>
</div>

<script type="text/javascript">
currentDiggType = 0;
</script></div>
<div class="clear"></div>
<div id="post_next_prev">

<a href="https://www.cnblogs.com/leesf456/p/6285014.html" class="p_n_p_prefix">« </a> 上一篇：    <a href="https://www.cnblogs.com/leesf456/p/6285014.html" title="发布于 2017-01-14 11:51">【Zookeeper】源码分析之持久化（二）之FileSnap</a>
<br>
<a href="https://www.cnblogs.com/leesf456/p/6286827.html" class="p_n_p_prefix">» </a> 下一篇：    <a href="https://www.cnblogs.com/leesf456/p/6286827.html" title="发布于 2017-01-15 17:02">【Zookeeper】源码分析之Watcher机制（一）</a>

</div>
</div>
</div>
<div class="postDesc">posted @ 
<span id="post-date">2017-01-14 18:31</span>&nbsp;
<a href="https://www.cnblogs.com/leesf456/">leesf</a>&nbsp;
阅读(<span id="post_view_count">1676</span>)&nbsp;
评论(<span id="post_comment_count">0</span>)&nbsp;
<a href="https://i.cnblogs.com/EditPosts.aspx?postid=6285703" rel="nofollow">编辑</a>&nbsp;
<a href="javascript:void(0)" onclick="AddToWz(6285703);return false;">收藏</a></div>
</div>


</div><!--end: topics 文章、评论容器-->
</div>
<script src="https://common.cnblogs.com/highlight/9.12.0/highlight.min.js"></script>
<script>markdown_highlight();</script>
<script>
var allowComments = true, cb_blogId = 236150, cb_blogApp = 'leesf456', cb_blogUserGuid = '75e00040-f3b3-e311-8d02-90b11c0b17d6';
var cb_entryId = 6285703, cb_entryCreatedDate = '2017-01-14 18:31', cb_postType = 1; 
loadViewCount(cb_entryId);
</script><a name="!comments"></a>
<div id="blog-comments-placeholder"></div>
<script>
var commentManager = new blogCommentManager();
commentManager.renderComments(0);
</script>

<div id="comment_form" class="commentform">
<a name="commentform"></a>
<div id="divCommentShow"></div>
<div id="comment_nav"><span id="span_refresh_tips"></span><a href="javascript:void(0);" onclick="return RefreshCommentList();" id="lnk_RefreshComments" runat="server" clientidmode="Static">刷新评论</a><a href="#" onclick="return RefreshPage();">刷新页面</a><a href="#top">返回顶部</a></div>
<div id="comment_form_container"><div class="login_tips">
注册用户登录后才能发表评论，请 
<a rel="nofollow" href="javascript:void(0);" class="underline" onclick="return login('commentform');">登录</a>
或 
<a rel="nofollow" href="javascript:void(0);" class="underline" onclick="return register();">注册</a>，
<a href="https://www.cnblogs.com/">访问</a> 网站首页。
</div></div>
<div class="ad_text_commentbox" id="ad_text_under_commentbox"></div>
<div id="ad_t2" style="display: none;"><a href="http://www.ucancode.com/index.htm" target="_blank" onclick="ga('send', 'event', 'Link', 'click', 'T2-ucancode')">【推荐】超50万行VC++源码: 大型组态工控、电力仿真CAD与GIS源码库</a><br><a href="http://click.aliyun.com/m/1000081987/" target="_blank" onclick="ga('send', 'event', 'Link', 'click', 'T2-阿里云')">【推荐】阿里云双11返场来袭，热门产品低至一折等你来抢！</a><br><a href="https://cloud.tencent.com/act/developer?fromSource=gwzcw.3196335.3196335.3196335&amp;utm_medium=cpc&amp;utm_id=gwzcw.3196335.3196335.3196335" target="_blank" onclick="ga('send', 'event', 'Link', 'click', 'T2-腾讯云')">【活动】开发者上云必备，腾讯云1核4G 2M云服务器11元/月起</a><br><a href="https://cloud.baidu.com/campaign/Promotion-20191111/index.html?track=cp:dsp|pf:pc|pp:chui-bokeyuan-huodong-19shuangshiyiganenji-BCC-cpaxingshi-191210|pu:cpa-xingshi|ci:2019syj|kw:2172212" target="_blank" onclick="ga('send', 'event', 'Link', 'click', 'T2-百度云')">【推荐】百度智能云岁末感恩季，明星产品低至1元新老用户畅享</a><br><a href="https://www.jdcloud.com/cn/activity/newUser?utm_source=DMT_cnblogs&amp;utm_medium=CH&amp;utm_campaign=09vm&amp;utm_term=Virtual-Machines" target="_blank" onclick="ga('send', 'event', 'Link', 'click', 'T2-京东云')">【活动】京东云限时优惠1.5折购云主机，最高返价值1000元礼品！</a><br></div>
<div id="opt_under_post"></div>
<script async="async" src="https://www.googletagservices.com/tag/js/gpt.js"></script>
<script>
var googletag = googletag || {};
googletag.cmd = googletag.cmd || [];
</script>
<script>
googletag.cmd.push(function () {
googletag.defineSlot("/1090369/C1", [300, 250], "div-gpt-ad-1546353474406-0").addService(googletag.pubads());
googletag.defineSlot("/1090369/C2", [468, 60], "div-gpt-ad-1539008685004-0").addService(googletag.pubads());
googletag.pubads().enableSingleRequest();
googletag.enableServices();
});
</script>
<div id="cnblogs_c1" class="c_ad_block" style="display: none;">
<div id="div-gpt-ad-1546353474406-0" style="height:250px; width:300px;" data-google-query-id="CP--y6iejecCFQGYlgodMFoGdw"><div id="google_ads_iframe_/1090369/C1_0__container__" style="border: 0pt none;"><iframe id="google_ads_iframe_/1090369/C1_0" title="3rd party ad content" name="google_ads_iframe_/1090369/C1_0" width="300" height="250" scrolling="no" marginwidth="0" marginheight="0" frameborder="0" srcdoc="" data-google-container-id="1" style="border: 0px; vertical-align: bottom;" data-load-complete="true"></iframe></div></div>
</div>
<div id="under_post_news" style="display: none;"><div class="recomm-block"><b>相关博文：</b><br>·  <a title="【Zookeeper】源码分析之序列化" href="https://www.cnblogs.com/leesf456/p/6278853.html" target="_blank" onclick="clickRecomItmem(6278853)">【Zookeeper】源码分析之序列化</a><br>·  <a title="Zookeeper数据与存储" href="https://www.cnblogs.com/hehheai/p/6506835.html" target="_blank" onclick="clickRecomItmem(6506835)">Zookeeper数据与存储</a><br>·  <a title="【分布式】Zookeeper数据与存储" href="https://www.cnblogs.com/leesf456/p/6179118.html" target="_blank" onclick="clickRecomItmem(6179118)">【分布式】Zookeeper数据与存储</a><br>·  <a title="【Zookeeper】源码分析之Leader选举（二）之FastLeaderElection" href="https://www.cnblogs.com/leesf456/p/6508185.html" target="_blank" onclick="clickRecomItmem(6508185)">【Zookeeper】源码分析之Leader选举（二）之FastLeaderElection</a><br>·  <a title="【JUC】JDK1.8源码分析之ReentrantLock（三）" href="https://www.cnblogs.com/leesf456/p/5383609.html" target="_blank" onclick="clickRecomItmem(5383609)">【JUC】JDK1.8源码分析之ReentrantLock（三）</a><br>»  <a target="_blank" href="https://recomm.cnblogs.com/blogpost/6285703">更多推荐...</a><div id="cnblogs_t5"><a href="https://developer.aliyun.com/article/717197?utm_content=g_1000088944" target="_blank" onclick="ga('send', 'event', 'Link', 'click', 'T5-阿里云开发者社区')">开放下载！《长安十二时辰》爆款背后的优酷技术秘籍首次公开</a></div></div></div>
<div id="cnblogs_c2" class="c_ad_block" style="display: none;">
<div id="div-gpt-ad-1539008685004-0" style="height:60px; width:468px;" data-google-query-id="CIC_y6iejecCFQGYlgodMFoGdw">

<div id="google_ads_iframe_/1090369/C2_0__container__" style="border: 0pt none;"><iframe id="google_ads_iframe_/1090369/C2_0" title="3rd party ad content" name="google_ads_iframe_/1090369/C2_0" width="468" height="60" scrolling="no" marginwidth="0" marginheight="0" frameborder="0" srcdoc="" data-google-container-id="2" style="border: 0px; vertical-align: bottom;" data-load-complete="true"></iframe></div></div>
</div>
<div id="under_post_kb" style="display: none;">
<div class="itnews c_ad_block">
<b>最新 IT 新闻</b>:
<br>
·              <a href="//news.cnblogs.com/n/653882/" target="_blank">“鬼压床”压你的，到底是什么鬼？</a>
<br>
·              <a href="//news.cnblogs.com/n/653881/" target="_blank">开源技术社区创业者陈智宏郁结自杀：享年42岁</a>
<br>
·              <a href="//news.cnblogs.com/n/653880/" target="_blank">开源在不断发展，但似乎有些跑偏？</a>
<br>
·              <a href="//news.cnblogs.com/n/653879/" target="_blank">你敢坐吗？空客首次实现飞机全自动起飞：基于图像识别</a>
<br>
·              <a href="//news.cnblogs.com/n/653878/" target="_blank">寻找下个SpaceX！太空领域创业投资创历史新高</a>
<br>
» <a href="https://news.cnblogs.com/" title="IT 新闻" target="_blank">更多新闻...</a>
</div></div>
<div id="HistoryToday" class="c_ad_block"></div>
<script type="text/javascript">
fixPostBody();
setTimeout(function () { incrementViewCount(cb_entryId); }, 50);
deliverAdT2();
deliverAdC1();
deliverAdC2();
loadNewsAndKb();
loadBlogSignature();
LoadPostCategoriesTags(cb_blogId, cb_entryId);        LoadPostInfoBlock(cb_blogId, cb_entryId, cb_blogApp, cb_blogUserGuid);
GetPrevNextPost(cb_entryId, cb_blogId, cb_entryCreatedDate, cb_postType);
loadOptUnderPost();
GetHistoryToday(cb_blogId, cb_blogApp, cb_entryCreatedDate);
</script>
</div>
</div><!--end: forFlow -->
</div>