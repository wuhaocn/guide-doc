<div class="newsmain">
<!--左B-->
<div class="left">
<h1 class="titcap fontyh"><a name="tit">一文理解Zookeeper的Watcher机制</a></h1>
<ul class="u1">
<li class="l1"><i class="icon icon-p-shijian"></i>时间：2019-08-22 00:19 <i class="icon icon-rengezhongxin"></i>作者：Java花园 <i class="icon icon-servicehaiwaizhiyou1"></i>来源：<a href="#" class="blue" target="_blank">Java花园</a> <i class="icon icon-fire"></i>阅读：24</li>
<li class="l2" onmouseover="objdis(1,'newm')" onmouseout="objdis(0,'newm')">扫一扫，手机访问</li>
</ul>
<div id="newm" style="display:none;"><img src="https://www.songma.com/tem/getqr.php?u=https://www.songma.com/m/news/txtlist_i59421v.html&amp;size=4"></div>
<div class="zytad"></div>
<div class="zhaiy "><strong>摘要：</strong>Zookeeper系列详情（持续升级）Zookeeper基础初探一文理解Zookeeper数据节点 znode一文理解WatcherZookeeper分布式锁实现Zookeeper部署模式??Zookeeper提供了数据的发布/订阅功能，多个订阅者可同时监听某一特定主题对象，当该主题对象的自身状态发</div>
<div class="ntxt"><p>Zookeeper系列详情（<strong>持续升级</strong>）</p><ul><li>Zookeeper基础初探</li><li>一文理解Zookeeper数据节点-znode</li><li>一文理解Watcher</li><li>Zookeeper分布式锁实现</li><li>Zookeeper部署模式</li></ul><p>??Zookeeper提供了数据的发布/订阅功能，多个订阅者可同时监听某一特定主题对象，当该主题对象的自身状态发生变化时(例如节点内容改变、节点下的子节点列表改变等)，会实时、主动通知所有订阅者。</p><p>??Zookeeper采用了Watcher机制实现数据的发布/订阅功能。该机制在被订阅对象发生变化时会异步通知用户端，因而用户端不必在Watcher注册后轮询阻塞，从而减轻了用户端压力。</p><blockquote><p>Watcher机制实际上<strong>与观察者模式相似</strong>，也可看作是一种观察者模式在分布式场景下的实现方式。</p></blockquote><h3>watcher架构</h3><p>Watcher实现由三个部分组成：</p><ul><li>Zookeeper服务端；</li><li>Zookeeper用户端；</li><li>用户端的ZKWatchManager对象；</li></ul><p>??用户端首先将Watcher注册到服务端，同时将Watcher对象保存到用户端的Watch管理器中。当ZooKeeper服务端监听的数据状态发生变化时，服务端会主动通知用户端，接着用户端的Watch管理器会触发相关Watcher来回调相应解决逻辑，从而完成整体的数据发布/订阅流程。</p><br><img src="https://img.songma.com/wenzhang/20190628/gm3bfn2zskc134.png">Watcher架构<h3>Watcher特性</h3><table><thead><tr><th>特性</th><th>说明</th></tr></thead><tbody><tr><td>一次性</td><td>Watcher是一次性的，一旦被触发就会移除，再次使用时需要重新注册</td></tr><tr><td>用户端顺序回调</td><td>Watcher回调是顺序串行化执行的，只有回调后用户端才能看到最新的数据状态。一个Watcher回调逻辑不应该太多，以免影响别的watcher执行</td></tr><tr><td>轻量级</td><td>WatchEvent是最小的通信单元，结构上只包含通知状态、事件类型和节点路径，并不会告诉数据节点变化前后的具体内容；</td></tr><tr><td>时效性</td><td>Watcher只有在当前session彻底失效时才会无效，若在session有效期内快速重连成功，则watcher仍然存在，仍可接收到通知；</td></tr></tbody></table><h3>Watcher接口设计</h3><p>??Watcher是一个接口，<strong>任何实现了Watcher接口的类就是一个新的Watcher</strong>。Watcher内部包含了两个枚举类：KeeperState、EventType。<br></p><img src="https://img.songma.com/wenzhang/20190628/yceapkfl5dh135.png">watcher类图<p></p><h3>Watcher通知状态(KeeperState)</h3><p>??KeeperState是用户端与服务端连接状态发生变化时对应的通知类型。路径为org.apache.zookeeper.Watcher.Event.KeeperState，是一个枚举类，其枚举属性如下；</p><table><thead><tr><th>枚举属性</th><th>说明</th></tr></thead><tbody><tr><td>Unknown(-1)</td><td>属性过期</td></tr><tr><td>Disconnected(0)</td><td>用户端与服务器断开连接时</td></tr><tr><td>NoSyncConnected(1)</td><td>属性过期</td></tr><tr><td>SyncConnected(3)</td><td>用户端与服务器正常连接时</td></tr><tr><td>AuthFailed(4)</td><td>身份认证失败时</td></tr><tr><td>ConnectedReadOnly(5)</td><td>3.3.0版本后支持只读模式，一般情况下ZK集群中半数以上服务器正常，zk集群才能正常对外提供服务。该属性的意义在于：若用户端设置了允许只读模式，则当zk集群中只有少于半数的服务器正常时，会返回这个状态给用户端，此时用户端只能解决读请求</td></tr><tr><td>SaslAuthenticated(6)</td><td>服务器采用SASL做校验时</td></tr><tr><td>Expired(-112)</td><td>会话session失效时</td></tr></tbody></table><h3>Watcher事件类型(EventType)</h3><p>??EventType是数据节点(znode)发生变化时对应的通知类型。<strong>EventType变化时KeeperState永远处于SyncConnected通知状态下；当KeeperState发生变化时，EventType永远为None</strong>。其路径为org.apache.zookeeper.Watcher.Event.EventType，是一个枚举类，枚举属性如下；</p><table><thead><tr><th>枚举属性</th><th>说明</th></tr></thead><tbody><tr><td>None (-1)</td><td>无</td></tr><tr><td>NodeCreated (1)</td><td>Watcher监听的数据节点被创立时</td></tr><tr><td>NodeDeleted (2)</td><td>Watcher监听的数据节点被删除时</td></tr><tr><td>NodeDataChanged (3)</td><td>Watcher监听的数据节点内容发生变更时(无论内容数据能否变化)</td></tr><tr><td>NodeChildrenChanged (4)</td><td>Watcher监听的数据节点的子节点列表发生变更时</td></tr></tbody></table><p><strong>注</strong>：用户端接收到的相关事件通知中只包含状态及类型等信息，不包括节点变化前后的具体内容，变化前的数据需业务自身存储，变化后的数据需调用get等方法重新获取；</p><h3>Watcher注册及通知流程</h3><ul><li>用户端Watcher管理器：ZKWatchManager数据结构</li></ul><table border="0" cellpadding="0" cellspacing="0" class="syntaxhighlighter  css"><tbody><tr><td class="gutter"><div class="line number1 index0 alt2">1</div></td><td class="code"><div class="container"><div class="line number1 index0 alt2"><code class="css plain">&lt;</code><code class="css value">code</code><code class="css plain">&gt;//ZKWatchManager维护了三个map，key代表数据节点的绝对路径，value代表注册在当前节点上的watcher集合//代表节点上内容数据、状态信息变更相关监听private final Map&lt;String, Set&lt;Watcher&gt;&gt; dataWatches =&nbsp;&nbsp;&nbsp; new HashMap&lt;String, Set&lt;Watcher&gt;&gt;();//代表节点变更相关监听private final Map&lt;String, Set&lt;Watcher&gt;&gt; existWatches =&nbsp;&nbsp;&nbsp; new HashMap&lt;String, Set&lt;Watcher&gt;&gt;();//代表节点子列表变更相关监听private final Map&lt;String, Set&lt;Watcher&gt;&gt; childWatches =&nbsp;&nbsp;&nbsp; new HashMap&lt;String, Set&lt;Watcher&gt;&gt;();&lt;/</code><code class="css value">code</code><code class="css plain">&gt;</code></div></div></td></tr></tbody></table><ul><li>服务端Watcher管理器：WatchManager数据结构</li></ul><table border="0" cellpadding="0" cellspacing="0" class="syntaxhighlighter  css"><tbody><tr><td class="gutter"><div class="line number1 index0 alt2">1</div></td><td class="code"><div class="container"><div class="line number1 index0 alt2"><code class="css plain">&lt;</code><code class="css value">code</code><code class="css plain">&gt;//WatchManager维护了两个map//说明：WatchManager中的Watcher对象不是用户端客户定义的Watcher，//&nbsp;&nbsp;&nbsp;&nbsp; 而是服务端中实现了Watcher接口的ServerCnxn笼统类，//&nbsp;&nbsp;&nbsp;&nbsp; 该笼统类代表了一个用户端与服务端的连接//key代表数据节点路径，value代表用户端连接的集合，该map作用为：//通过一个指定znode路径可找到其映射的所有用户端，当znode发生变更时//可快速通知所有注册了当前Watcher的用户端private final HashMap&lt;String, HashSet&lt;Watcher&gt;&gt; watchTable =&nbsp;&nbsp;&nbsp; new HashMap&lt;String, HashSet&lt;Watcher&gt;&gt;();//key代表一个用户端与服务端的连接，value代表当前用户端监听的所有数据节点路径//该map作用为：当一个连接彻底断开时，可快速找到当前连接对应的所有//注册了监听的节点，以便移除当前用户端对节点的Watcherprivate final HashMap&lt;Watcher, HashSet&lt;String&gt;&gt; watch</code><code class="css value">2</code><code class="css plain">Paths =&nbsp;&nbsp;&nbsp; new HashMap&lt;Watcher, HashSet&lt;String&gt;&gt;();&lt;/</code><code class="css value">code</code><code class="css plain">&gt;</code></div></div></td></tr></tbody></table><ul><li>Watcher注册流程</li></ul><table border="0" cellpadding="0" cellspacing="0" class="syntaxhighlighter  css"><tbody><tr><td class="gutter"><div class="line number1 index0 alt2">1</div></td><td class="code"><div class="container"><div class="line number1 index0 alt2"><code class="css plain">&lt;</code><code class="css value">code</code><code class="css plain">&gt;//Packet对象构造函数//参数含义：请求头、响应头、请求体、响应体、Watcher封装的注册体、能否允许只读Packet(RequestHeader requestHeader, ReplyHeader replyHeader,&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; Record request, Record response,&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; WatchRegistration watchRegistration, boolean readOnly) {&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; this.requestHeader = requestHeader;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; this.replyHeader = replyHeader;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; this.request = request;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; this.response = response;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; this.readOnly = readOnly;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; this.watchRegistration = watchRegistration;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; }&lt;/</code><code class="css value">code</code><code class="css plain">&gt;</code></div></div></td></tr></tbody></table><img src="https://img.songma.com/wenzhang/20190628/srg3mxfkwyd136.png">Watcher注册流程.png<table border="0" cellpadding="0" cellspacing="0" class="syntaxhighlighter  css"><tbody><tr><td class="gutter"><div class="line number1 index0 alt2">1</div></td><td class="code"><div class="container"><div class="line number1 index0 alt2"><code class="css plain">&lt;</code><code class="css value">code</code><code class="css plain">&gt;</code><code class="css value">1</code><code class="css plain">. 用户端发送的请求中只包含能否需要注册Watcher，不会将Watcher实体发送；</code><code class="css value">2</code><code class="css plain">. Packet构造函数中的参数WatchRegistration是Watcher的封装体，用于服务响应成功后将Watcher保存到ZKWatchManager中；&lt;/</code><code class="css value">code</code><code class="css plain">&gt;</code></div></div></td></tr></tbody></table><ul><li><p>Watcher通知流程</p><br><img src="https://img.songma.com/wenzhang/20190628/rp3lrmejbmc137.png">Watcher通知流程</li></ul>                          <!-- 如果是付费文章，未购买，则显示购买按钮 -->    <!-- 连载目录项 -->    <!-- 如果是付费文章 -->      <!-- 如果是付费连载，已购买，且作者允许赞赏，则显示付费信息和赞赏 --></div>

<!--评论B-->
<a name="pj"></a>
<div class="pinlun fontyh">
<ul class="plu1">
<li class="l1"><strong>全部评论</strong>(0)</li><li class="l2"></li>
</ul>
<form name="f1" method="post" target="_blank" onsubmit="return newspj()">
<input type="hidden" value="1566404371n365" name="bh">
<ul class="plu0">
<li class="l1"><textarea id="pjt" name="pjt"></textarea></li>
<li class="l3">[<a href="pjlist_i59421v.html" target="_blank">展开所有评论</a>]</li>
<li class="l2"><input type="submit" value="发表评论"></li>
</ul>
</form>

</div>
<!--评论E-->

<div class="nxg">
上一篇：<a href="txtlist_i59422v.html">Java程序员进阶架构师必经之路</a><br>
下一篇：<a href="txtlist_i59420v.html">iOS的异步解决神器——Promises</a> </div>
<div class="lad"><div class="ad1"><script type="text/javascript" src="//t.syouhui.com/bwostcofx.js"></script></div></div>
<div class="otherxg">
<strong>最新发布的资讯信息</strong><br>
【系统环境|】<a href="txtlist_i61271v.html" class="g_ac0">淘码库，据消息称已被调查。淘码库源码网，已经无法访问！</a><span class="hui">(2020-01-14 04:13)</span><br>
【系统环境|服务器应用】<a href="txtlist_i61263v.html" class="g_ac0">Discuz隐藏后台admin.php网址修改路径</a><span class="hui">(2019-12-16 16:48)</span><br>
【系统环境|服务器应用】<a href="txtlist_i61262v.html" class="g_ac0">2020新网站如何让百度快速收录网站首页最新方法，亲测有用！免费</a><span class="hui">(2019-12-16 16:46)</span><br>
【系统环境|服务器应用】<a href="txtlist_i61256v.html" class="g_ac0">Discuz发布帖子时默认显示第一个主题分类的修改方法</a><span class="hui">(2019-12-09 00:13)</span><br>
【系统环境|软件环境】<a href="txtlist_i61218v.html" class="g_ac0">Android | App内存优化 之 内存泄漏 要点概述 以及 处理实战</a><span class="hui">(2019-12-04 14:27)</span><br>
【系统环境|软件环境】<a href="txtlist_i61217v.html" class="g_ac0">MySQL InnoDB 事务</a><span class="hui">(2019-12-04 14:26)</span><br>
【系统环境|软件环境】<a href="txtlist_i61216v.html" class="g_ac0">vue-router（单页面应用控制中心）常见用法</a><span class="hui">(2019-12-04 14:26)</span><br>
【系统环境|软件环境】<a href="txtlist_i61215v.html" class="g_ac0">Linux中的Kill命令</a><span class="hui">(2019-12-04 14:26)</span><br>
【系统环境|软件环境】<a href="txtlist_i61214v.html" class="g_ac0">Linux 入门时必学60个文件解决命令</a><span class="hui">(2019-12-04 14:26)</span><br>
【系统环境|软件环境】<a href="txtlist_i61213v.html" class="g_ac0">更新版ThreeJS 3D粒子波浪动画</a><span class="hui">(2019-12-04 14:26)</span><br>
</div>
</div>
<!--左E-->

<!--右B-->
<div class="right">

<div class="wzuser">
<div class="wzuserh">
<div class="user-wz">
<div class="user-wz-head">
<a target="_blank" href="https://www.songma.com/my/view365.html" class="user-wz-avatar"><img src="https://www.songma.com/upload/365/shop.jpg" alt="云上智慧" onerror="this.src='https://www.songma.com/user/img/nopic.gif'"></a> 
<div class="user-wz-name">
<a href="https://www.songma.com/my/view365.html" target="_blank">云上智慧</a> 
<div class="userwzdp">
<div class="left-arrow userwzdp-btn" style="cursor: pointer;" onclick="window.location='https://www.songma.com/shop/view365.html';">

<i class="icon icon-jiahao" style="font-size: 10px; color: rgb(255, 255, 255);"></i>
<span>店铺</span>
</div>
</div>
</div>
</div> 
<ul class="user-wz-shop-list">
<li class="user-wz-shop-item"><a href="https://www.songma.com/product/view41984.html" target="_blank">2018漂亮的杰奇小说2.3源码（有教程...</a></li>
<li class="user-wz-shop-item"><a href="https://www.songma.com/product/view41469.html" target="_blank">易语言挂机界面秒余额源码 只提供学...</a></li>
<li class="user-wz-shop-item"><a href="https://www.songma.com/product/view40799.html" target="_blank">贷款超市借贷超市|小额贷款|thinkp...</a></li>
<li class="user-wz-shop-item"><a href="https://www.songma.com/product/view43453.html" target="_blank">微信，支付宝，QQ钱包，二维码收款...</a></li>
<li class="user-wz-shop-item"><a href="https://www.songma.com/product/view42059.html" target="_blank">微信群二维码导航带整站数据+手机版...</a></li>
</ul>
</div>
</div></div>

<div class="adf"><div class="ad1"><script type="text/javascript" src="//t.syouhui.com/faswzswzj.js"></script></div></div>
<ul class="hotpro">
<li class="l1">商品推荐</li>
<li class="l2"><a href="../product/view7.html"><img alt="SEO在线伪原创工具源码 在线同义词替换工具源码 伪原创代写网站源码" src="https://www.songma.com/upload/1/1519837008-1/0398494001519837154tp1-2.jpg" width="50" height="50" align="left"></a><a href="../product/view7.html" title="SEO在线伪原创工具源码 在线同义词替换工具源码 伪原创代写网站源码">SEO在线伪原创工具源码 在线同义词替换工具源码 伪原创代写网站源码</a><br><strong class="feng">￥100.00</strong></li>
<li class="l2"><a href="../product/view53572.html"><img alt="微信小说分销系统源码,相似掌中云系统源码" src="https://www.songma.com/upload/367/1533965197-367/0555566001575212669tp367-2.jpg" width="50" height="50" align="left"></a><a href="../product/view53572.html" title="微信小说分销系统源码,相似掌中云系统源码">微信小说分销系统源码,相似掌中云系统源码</a><br><strong class="feng">￥60.00</strong></li>
<li class="l2"><a href="../product/view79605.html"><img alt="app store 红包 苹果应用商城红包 限量100个红包优惠券代金券" src="https://www.songma.com/upload/3/1537600051-3/0437482001537600595tp3-2.jpg" width="50" height="50" align="left"></a><a href="../product/view79605.html" title="app store 红包 苹果应用商城红包 限量100个红包优惠券代金券">app store 红包 苹果应用商城红包 限量100个红包优惠券代金券</a><br><strong class="feng">￥0.2</strong></li>
<li class="l2"><a href="../product/view31994.html"><img alt="360快照logo站点logo网站排名小图标LOGO展示关键词显示出图" src="https://www.songma.com/upload/1/1528798727-1/0085899001528798744tp1-2.jpg" width="50" height="50" align="left"></a><a href="../product/view31994.html" title="360快照logo站点logo网站排名小图标LOGO展示关键词显示出图">360快照logo站点logo网站排名小图标LOGO展示关键词显示出图</a><br><strong class="feng">￥200.00</strong></li>
<li class="l2"><a href="../product/view48088.html"><img alt="千图网图库网站解析源码 千库 昵图 摄图 图片下载vip解析源码 9网通用 淘宝卖家版" src="https://www.songma.com/upload/7/1532858685-7/0044542001533664953tp7-2.jpg" width="50" height="50" align="left"></a><a href="../product/view48088.html" title="千图网图库网站解析源码 千库 昵图 摄图 图片下载vip解析源码 9网通用 淘宝卖家版">千图网图库网站解析源码 千库 昵图 摄图 图片下载vip解析源码 9网通...</a><br><strong class="feng">￥1888.00</strong></li>
</ul> 


<div class="hotnew">
<ul class="u1 fontyh">
<li class="l1">资讯排行榜</li>
<li class="l2"><a href="./">更多&gt;&gt;</a></li>
</ul>
<ul class="u2">
<li class="l1"><span class="s1">1</span></li>
<li class="l2"><a href="txtlist_i21633v.html" class="g_ac0" title="程序媛眼中的程序猿原来是这样子的！">程序媛眼中的程序猿原来是这样子的！</a></li>
<li class="l1"><span class="s2">2</span></li>
<li class="l2"><a href="txtlist_i24822v.html" class="g_ac0" title="紧急整理了 20 道 Spring Boot 面试题，我经常拿来面试别人！">紧急整理了 20 道 Spring Boot 面试题，我经</a></li>
<li class="l1"><span class="s3">3</span></li>
<li class="l2"><a href="txtlist_i28505v.html" class="g_ac0" title="深入JVM内核5 GC参数">深入JVM内核5 GC参数</a></li>
<li class="l1"><span class="s4">4</span></li>
<li class="l2"><a href="txtlist_i2498v.html" class="g_ac0" title="HTML5(H5)常使用的十大前台框架（一）">HTML5(H5)常使用的十大前台框架（一）</a></li>
<li class="l1"><span class="s5">5</span></li>
<li class="l2"><a href="txtlist_i28215v.html" class="g_ac0" title="Android中okhttp原理详解-极度针对面试篇">Android中okhttp原理详解-极度针对面试篇</a></li>
<li class="l1"><span class="s6">6</span></li>
<li class="l2"><a href="txtlist_i1260v.html" class="g_ac0" title="斗鱼主播“祖祖小姨妈”下海">斗鱼主播“祖祖小姨妈”下海</a></li>
<li class="l1"><span class="s7">7</span></li>
<li class="l2"><a href="txtlist_i23765v.html" class="g_ac0" title="动漫聚选最新宅新闻 2018.09.26「周三」">动漫聚选最新宅新闻 2018.09.26「周三」</a></li>
<li class="l1"><span class="s8">8</span></li>
<li class="l2"><a href="txtlist_i1577v.html" class="g_ac0" title="网站建设技术革命202：零技术为网站增加留言板栏目">网站建设技术革命202：零技术为网站增加留言</a></li>
<li class="l1"><span class="s9">9</span></li>
<li class="l2"><a href="txtlist_i1767v.html" class="g_ac0" title="在线VIP视频免费观看, 嘘这个方法我只告诉你!">在线VIP视频免费观看, 嘘这个方法我只告诉你</a></li>
<li class="l1"><span class="s10">10</span></li>
<li class="l2"><a href="txtlist_i24704v.html" class="g_ac0" title="传奇单机架设，IIS安装网站架设，登录器简单配置教程图文教程">传奇单机架设，IIS安装网站架设，登录器简单</a></li>
</ul>
</div> </div>
<!--右E-->

</div>