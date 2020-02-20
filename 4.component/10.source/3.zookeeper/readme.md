## 1.概览

## 2.源码

### 2.1 官网

* https://github.com/apache/zookeeper.git

### 2.2 gradle版本
* 地址: git@github.com:coral-learning/zookeeper.git
* 分支: master-gradle

    
### 2.3 源码分析目录

* [Zookeeper介绍](Zookeeper介绍.md)
* [Zookeeper之工作原理](Zookeeper之工作原理.md)
* [Zookeeper源码环境搭建](Zookeeper源码环境搭建.tny.md)
* 客户端
    * 
* 服务端
    * [Zookeeper之节点详解](Zookeeper之节点详解.md)
    * [Zookeeper之服务端启动流程](Zookeeper之服务端启动流程.md)
    * [Zookeeper之服务端处理客户端事件流程-单机](Zookeeper之服务端处理客户端事件流程-单机.md)
    * [Zookeeper之服务端处理客户端事件流程-集群](Zookeeper之服务端处理客户端事件流程-集群.md)
    * [Zookeeper之服务端ZookeeperServer](Zookeeper之服务端ZookeeperServer.md)
    * [Zookeeper之服务端LeaderZooKeeperServer](Zookeeper之服务端LeaderZooKeeperServer.md)
    * [Zookeeper之服务端FollowerZooKeeperServer](Zookeeper之服务端FollowerZooKeeperServer.md)
    * [Zookeeper之服务端ObserverZooKeeperServer](Zookeeper之服务端ObserverZooKeeperServer.md)
* 选举概述
    * [Zookeeper之Leader选举概述](Zookeeper之Leader选举概述.md)
    * [Zookeeper之Leader选举之FastLeaderElection](Zookeeper之Leader选举之FastLeaderElection.md)
* 序列化
    * [Zookeeper之序列化](Zookeeper之序列化.md)
    * [Zookeeper之持久化(一)之FileTxnLog](Zookeeper之持久化(一)之FileTxnLog.md)
    * [Zookeeper之持久化(二)之FileSnap](Zookeeper之持久化(二)之FileSnap.md)
* Watcher机制
    * [Zookeeper之Watcher概述](Zookeeper之Watcher概述.tny.md)
    * [Zookeeper之Watcher机制概述](Zookeeper之Watcher机制概述.tny.md)
    * [Zookeeper之Watcher机制之ZooKeeper](Zookeeper之Watcher机制之ZooKeeper.md)
    * [Zookeeper之Watcher机制之WatchManager](Zookeeper之Watcher机制之WatchManager.tny.md)



   

<div id="cnblogs_post_body" class="blogpost-body ">
    <p>Zookeeper源码分析目录如下</p>
<p>　　1.&nbsp;<a id="cb_post_title_url" class="postTitle2" href="http://www.cnblogs.com/leesf456/p/6278853.html">【Zookeeper】源码分析之序列化</a></p>
<p>　　2.&nbsp;<a id="cb_post_title_url" class="postTitle2" href="http://www.cnblogs.com/leesf456/p/6279956.html">【Zookeeper】源码分析之持久化（一）之FileTxnLog</a></p>
<p>　　3.&nbsp;<a id="cb_post_title_url" class="postTitle2" href="http://www.cnblogs.com/leesf456/p/6285014.html">【Zookeeper】源码分析之持久化（二）之FileSnap</a></p>
<p>　　4.&nbsp;<a id="cb_post_title_url" class="postTitle2" href="http://www.cnblogs.com/leesf456/p/6285703.html">【Zookeeper】源码分析之持久化（三）之FileTxnSnapLog</a></p>
<p>　　5.&nbsp;<a id="cb_post_title_url" class="postTitle2" href="http://www.cnblogs.com/leesf456/p/6286827.html">【Zookeeper】源码分析之Watcher机制（一）</a></p>
<p>　　6.&nbsp;<a id="ArchiveMonth1_Days_ctl00_Entries_TitleUrl_4" class="entrylistItemTitle" href="http://www.cnblogs.com/leesf456/p/6288709.html">【Zookeeper】源码分析之Watcher机制（二）之WatchManager</a></p>
<p>　　7.&nbsp;<a id="ArchiveMonth1_Days_ctl00_Entries_TitleUrl_1" class="entrylistItemTitle" href="http://www.cnblogs.com/leesf456/p/6291004.html">【Zookeeper】源码分析之Watcher机制（三）之ZooKeeper</a></p>
<p>　　8.&nbsp;<a id="ArchiveMonth1_Days_ctl00_Entries_TitleUrl_5" class="entrylistItemTitle" href="http://www.cnblogs.com/leesf456/p/6410793.html">【Zookeeper】源码分析之请求处理链（一）</a></p>
<p>　　9.&nbsp;<a id="ArchiveMonth1_Days_ctl00_Entries_TitleUrl_4" class="entrylistItemTitle" href="http://www.cnblogs.com/leesf456/p/6412843.html">【Zookeeper】源码分析之请求处理链（二）之PrepRequestProcessor</a></p>
<p>　　10.&nbsp;<a id="ArchiveMonth1_Days_ctl00_Entries_TitleUrl_2" class="entrylistItemTitle" href="http://www.cnblogs.com/leesf456/p/6438411.html">【Zookeeper】源码分析之请求处理链（三）之SyncRequestProcessor</a></p>
<p>　　11.&nbsp;<a id="ArchiveMonth1_Days_ctl00_Entries_TitleUrl_1" class="entrylistItemTitle" href="http://www.cnblogs.com/leesf456/p/6472496.html">【Zookeeper】源码分析之请求处理链（四）之FinalRequestProcessor</a></p>
<p>　　12.&nbsp;<a id="ArchiveMonth1_Days_ctl00_Entries_TitleUrl_0" class="entrylistItemTitle" href="http://www.cnblogs.com/leesf456/p/6477815.html">【Zookeeper】源码分析之网络通信（一）</a></p>
<p>　　13.&nbsp;<a id="ArchiveMonth1_Days_ctl00_Entries_TitleUrl_10" class="entrylistItemTitle" href="http://www.cnblogs.com/leesf456/p/6484780.html">【Zookeeper】源码分析之网络通信（二）之NIOServerCnxn</a></p>
<p>　　14.&nbsp;<a id="ArchiveMonth1_Days_ctl00_Entries_TitleUrl_8" class="entrylistItemTitle" href="http://www.cnblogs.com/leesf456/p/6486454.html">【Zookeeper】源码分析之网络通信（三）之NettyServerCnxn</a></p>
<p>　　15.&nbsp;<a id="ArchiveMonth1_Days_ctl00_Entries_TitleUrl_7" class="entrylistItemTitle" href="http://www.cnblogs.com/leesf456/p/6494290.html">【Zookeeper】源码分析之Leader选举（一）</a></p>
<p>　　16.&nbsp;<a id="ArchiveMonth1_Days_ctl00_Entries_TitleUrl_5" class="entrylistItemTitle" href="http://www.cnblogs.com/leesf456/p/6508185.html">【Zookeeper】源码分析之Leader选举（二）之FastLeaderElection</a></p>
<p>　　17.&nbsp;<a id="ArchiveMonth1_Days_ctl00_Entries_TitleUrl_4" class="entrylistItemTitle" href="http://www.cnblogs.com/leesf456/p/6514897.html">【Zookeeper】源码分析之服务器（一）</a></p>
<p>　　18.&nbsp;<a id="ArchiveMonth1_Days_ctl00_Entries_TitleUrl_3" class="entrylistItemTitle" href="http://www.cnblogs.com/leesf456/p/6515105.html">【Zookeeper】源码分析之服务器（二）之ZooKeeperServer</a></p>
<p>　　19.&nbsp;<a id="ArchiveMonth1_Days_ctl00_Entries_TitleUrl_2" class="entrylistItemTitle" href="http://www.cnblogs.com/leesf456/p/6516805.html">【Zookeeper】源码分析之服务器（三）之LeaderZooKeeperServer</a></p>
<p>　　20.&nbsp;<a id="cb_post_title_url" class="postTitle2" href="http://www.cnblogs.com/leesf456/p/6517058.html">【Zookeeper】源码分析之服务器（四）之FollowerZooKeeperServer</a></p>
<p>　　21.&nbsp;<a id="cb_post_title_url" class="postTitle2" href="http://www.cnblogs.com/leesf456/p/6517945.html">【Zookeeper】源码分析之服务器（五）之ObserverZooKeeperServer</a></p>

</div>

### 2.4 参考

https://www.cnblogs.com/leesf456/p/6518040.html