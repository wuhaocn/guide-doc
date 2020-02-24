<div id="mainContent">
<div class="forFlow">
<div id="post_detail">
<div id="topics">
<div class="post">
<h1 class="postTitle">

<a id="cb_post_title_url" class="postTitle2" href="https://www.cnblogs.com/littleatp/p/8562842.html">mongodb副本集高可用架构</a>

</h1>
<div class="clear"></div>
<div class="postBody">

<div id="cnblogs_post_body" class="blogpost-body cnblogs-markdown">
<h2 id="一简介">一、简介</h2>
<p>Mongodb复制集由一组Mongod实例（进程）组成，包含一个Primary节点和多个Secondary节点。<br>
Mongodb Driver（客户端）的所有数据都写入Primary，Secondary从Primary同步写入的数据，以保持复制集内所有成员存储相同的数据集，实现数据的高可用。</p>
<p><strong>使用场景</strong></p>
<ul>
<li>数据冗余，用做故障恢复使用，当发生硬件故障或者其它原因造成的宕机时，可以使用副本进行恢复。</li>
<li>读写分离，读的请求分流到副本上，减轻主节点的读压力。</li>
</ul>
<p>一个典型的副本集架构如下图所示：</p>
<p><img src="https://images2018.cnblogs.com/blog/242916/201803/242916-20180313214746630-2040647850.png"></p>
<h2 id="二副本集角色">二、副本集角色</h2>
<ol>
<li><p>主节点（Primary）<br>
接收所有的写请求，然后把修改同步到所有Secondary。一个Replica Set只能有一个Primary节点，当Primary挂掉后，其他Secondary或者Arbiter节点会重新选举出来一个主节点。<br>
默认读请求也是发到Primary节点处理的，可以通过修改客户端连接配置以支持读取Secondary节点。</p></li>
<li><p>副本节点（Secondary）<br>
与主节点保持同样的数据集。当主节点挂掉的时候，参与选主。</p></li>
<li><p>仲裁者（Arbiter）<br>
不保有数据，不参与选主，只进行选主投票。使用Arbiter可以减轻数据存储的硬件需求，Arbiter几乎没什么大的硬件资源需求，但重要的一点是，在生产环境下它和其他数据节点不要部署在同一台机器上。</p></li>
</ol>
<h2 id="三两种架构模式">三、两种架构模式</h2>
<ol>
<li>PSS<br>
Primary + Secondary + Secondary模式，通过Primary和Secondary搭建的Replica Set<br>
Diagram of a 3 member replica set that consists of a primary and two secondaries.</li>
</ol>
<p><img src="https://images2018.cnblogs.com/blog/242916/201803/242916-20180313214807697-1361550290.png"></p>
<p>该模式下 Replica Set节点数必须为奇数，目的是选主投票的时候要出现<strong>大多数</strong>才能进行选主决策。</p>
<ol>
<li>PSA<br>
Primary + Secondary + Arbiter模式，使用Arbiter搭建Replica Set</li>
</ol>
<p><img src="https://images2018.cnblogs.com/blog/242916/201803/242916-20180313214821963-1134366100.png"></p>
<p>偶数个数据节点，加一个Arbiter构成的Replica Set</p>
<h2 id="四选举机制">四、选举机制</h2>
<p>复制集通过 replSetInitiate 命令或 rs.initiate() 命令进行初始化。<br>
初始化后各个成员间开始发送心跳消息，并发起 Primary 选举操作，获得大多数成员投票支持的节点，会成为 Primary，其余节点成为 Secondary。</p>
<pre><code class="hljs groovy">config = {
<span class="hljs-string">_id :</span> <span class="hljs-string">"my_replica_set"</span>,
<span class="hljs-string">members :</span> [
{<span class="hljs-string">_id :</span> <span class="hljs-number">0</span>, <span class="hljs-string">host :</span> <span class="hljs-string">"rs1.example.net:27017"</span>},
{<span class="hljs-string">_id :</span> <span class="hljs-number">1</span>, <span class="hljs-string">host :</span> <span class="hljs-string">"rs2.example.net:27017"</span>},
{<span class="hljs-string">_id :</span> <span class="hljs-number">2</span>, <span class="hljs-string">host :</span> <span class="hljs-string">"rs3.example.net:27017"</span>},
]
}
rs.initiate(config)</code></pre>
<p><strong>大多数</strong><br>
假设复制集内投票成员（后续介绍）数量为 N，则大多数为 N/2 + 1，当复制集内存活成员数量不足大多数时，整个复制集将无法选举出 Primary，复制集将无法提供写服务，处于只读状态。<br>
关于大多数的计算如下表所示</p>
<table>
<thead>
<tr class="header">
<th>投票成员数</th>
<th>大多数</th>
<th>容忍失效数</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td>1</td>
<td>1</td>
<td>0</td>
</tr>
<tr class="even">
<td>2</td>
<td>2</td>
<td>0</td>
</tr>
<tr class="odd">
<td>3</td>
<td>2</td>
<td>1</td>
</tr>
<tr class="even">
<td>4</td>
<td>3</td>
<td>1</td>
</tr>
<tr class="odd">
<td>5</td>
<td>3</td>
<td>2</td>
</tr>
</tbody>
</table>
<p>Mongodb副本集的选举基于Bully算法，这是一种协调者竞选算法，详细解析可以<a href="http://blog.nosqlfan.com/html/4139.html">参考这里</a><br>
Primary 的选举受节点间心跳、优先级、最新的 oplog 时间等多种因素影响。官方文档对于<a href="https://docs.mongodb.com/manual/core/replica-set-elections/">选举机制的说明</a></p>
<p><strong>特殊角色</strong></p>
<ul>
<li><p>Arbiter<br>
Arbiter 节点只参与投票，不能被选为 Primary，并且不从 Primary 同步数据。<br>
当节点宕机导致复制集无法选出 Primary时，可以给复制集添加一个 Arbiter 节点，即使有节点宕机，仍能选出 Primary。<br>
Arbiter 本身不存储数据，是非常轻量级的服务，当复制集成员为偶数时，最好加入一个 Arbiter 节点，以提升复制集可用性。</p></li>
<li><p>Priority0<br>
Priority0节点的选举优先级为0，不会被选举为 Primary。<br>
比如你跨机房 A、B 部署了一个复制集，并且想指定 Primary 必须在 A 机房，这时可以将 B 机房的复制集成员 Priority 设置为0，这样 Primary 就一定会是 A 机房的成员。<br>
（注意：如果这样部署，最好将大多数节点部署在 A 机房，否则网络分区时可能无法选出 Primary。）</p></li>
<li><p>Vote0<br>
Mongodb 3.0里，复制集成员最多50个，参与 Primary 选举投票的成员最多7个，其他成员（Vote0）的 vote 属性必须设置为0，即不参与投票。</p></li>
<li><p>Hidden<br>
Hidden 节点不能被选为主（Priority 为0），并且对 Driver 不可见。<br>
因 Hidden 节点不会接受 Driver 的请求，可使用 Hidden 节点做一些数据备份、离线计算的任务，不会影响复制集的服务。</p></li>
<li><p>Delayed<br>
Delayed 节点必须是 Hidden 节点，并且其数据落后与 Primary 一段时间（可配置，比如1个小时）。<br>
因 Delayed 节点的数据比 Primary 落后一段时间，当错误或者无效的数据写入 Primary 时，可通过 Delayed 节点的数据来恢复到之前的时间点。</p></li>
</ul>
<p><strong>触发选举条件</strong></p>
<ul>
<li>初始化一个副本集时。</li>
<li>从库不能连接到主库(默认超过10s，可通过heartbeatTimeoutSecs参数控制)，由从库发起选举</li>
<li>主库放弃primary 角色，比如执行rs.stepdown 命令</li>
</ul>
<p>Mongodb副本集通过心跳检测实现自动failover机制，进而实现高可用<br>
<img src="https://images2018.cnblogs.com/blog/242916/201803/242916-20180313214956548-1867816349.png"></p>
<h2 id="五数据同步">五、数据同步</h2>
<p>Primary 与 Secondary 之间通过 oplog 来同步数据，Primary 上的写操作完成后，会向特殊的 local.oplog.rs 特殊集合写入一条 oplog，Secondary 不断的从 Primary 取新的 oplog 并应用。<br>
因 oplog 的数据会不断增加，local.oplog.rs 被设置成为一个 capped 集合，当容量达到配置上限时，会将最旧的数据删除掉。<br>
另外考虑到 oplog 在 Secondary 上可能重复应用，oplog 必须具有幂等性，即重复应用也会得到相同的结果。<br>
如下 oplog 的格式，包含 ts、h、op、ns、o 等字段。</p>
<pre><code class="hljs clojure">{
<span class="hljs-string">"ts"</span> : Timestamp(<span class="hljs-number">1446011584</span>, <span class="hljs-number">2</span>),
<span class="hljs-string">"h"</span> : NumberLong(<span class="hljs-string">"1687359108795812092"</span>),
<span class="hljs-string">"v"</span> : <span class="hljs-number">2</span>,
<span class="hljs-string">"op"</span> : <span class="hljs-string">"i"</span>,
<span class="hljs-string">"ns"</span> : <span class="hljs-string">"test.nosql"</span>,
<span class="hljs-string">"o"</span> : { <span class="hljs-string">"_id"</span> : ObjectId(<span class="hljs-string">"563062c0b085733f34ab4129"</span>), <span class="hljs-string">"name"</span> : <span class="hljs-string">"mongodb"</span>, <span class="hljs-string">"score"</span> : <span class="hljs-string">"100"</span> }
}</code></pre>
<table>
<thead>
<tr class="header">
<th>属性</th>
<th>说明</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td>ts</td>
<td>操作时间，当前 timestamp + 计数器，计数器每秒都被重置</td>
</tr>
<tr class="even">
<td>h</td>
<td>操作的全局唯一标识</td>
</tr>
<tr class="odd">
<td>v</td>
<td>oplog 版本信息</td>
</tr>
<tr class="even">
<td>op</td>
<td>操作类型</td>
</tr>
<tr class="odd">
<td>op.i</td>
<td>插入操作</td>
</tr>
<tr class="even">
<td>op.u</td>
<td>更新操作</td>
</tr>
<tr class="odd">
<td>op.d</td>
<td>删除操作</td>
</tr>
<tr class="even">
<td>op.c</td>
<td>执行命令（如 createDatabase，dropDatabase）</td>
</tr>
<tr class="odd">
<td>op.n</td>
<td>空操作，特殊用途</td>
</tr>
<tr class="even">
<td>ns</td>
<td>操作针对的集合</td>
</tr>
<tr class="odd">
<td>o</td>
<td>操作内容</td>
</tr>
<tr class="even">
<td>o2</td>
<td>操作查询条件，仅 update 操作包含该字段。</td>
</tr>
</tbody>
</table>
<p>Secondary 初次同步数据时，会先执行 init sync，从 Primary（或其他数据更新的 Secondary）同步全量数据，<br>
然后不断通过执行tailable cursor从 Primary 的 local.oplog.rs 集合里查询最新的 oplog 并应用到自身。</p>
<p><strong>异常回滚</strong><br>
当 Primary 宕机时，如果有数据未同步到 Secondary，当 Primary 重新加入时，如果新的 Primary 上已经发生了写操作，则旧 Primary 需要回滚部分操作，以保证数据集与新的 Primary 一致。<br>
旧 Primary 将回滚的数据写到单独的 rollback 目录下，数据库管理员可根据需要使用 mongorestore 进行恢复</p>
<h2 id="六读写配置">六、读写配置</h2>
<p><strong>Read Preference</strong><br>
默认情况下，复制集的所有读请求都发到 Primary，Driver 可通过设置 Read Preference 来将读请求路由到其他的节点。</p>
<ul>
<li>primary：默认规则，所有读请求发到 Primary；</li>
<li>primaryPreferred：Primary 优先，如果 Primary 不可达，请求 Secondary；</li>
<li>secondary：所有的读请求都发到 secondary；</li>
<li>secondaryPreferred：Secondary 优先，当所有 Secondary 不可达时，请求 Primary；</li>
<li>nearest：读请求发送到最近的可达节点上（通过 ping 探测得出最近的节点）。<br>
<a href="https://docs.mongodb.com/manual/core/read-preference/">关于read-preference</a></li>
</ul>
<p><strong>Write Concern</strong><br>
默认情况下，Primary 完成写操作即返回，Driver 可通过设置 Write Concern (参见这里)来设置写成功的规则。</p>
<p>如下的 write concern 规则设置写必须在大多数节点上成功，超时时间为5s。</p>
<pre><code class="hljs css"><span class="hljs-selector-tag">db</span><span class="hljs-selector-class">.products</span><span class="hljs-selector-class">.insert</span>(
{ <span class="hljs-attribute">item</span>: <span class="hljs-string">"envelopes"</span>, qty : <span class="hljs-number">100</span>, type: <span class="hljs-string">"Clasp"</span> },
{ <span class="hljs-attribute">writeConcern</span>: { w: majority, wtimeout: <span class="hljs-number">5000</span> } }
)</code></pre>
<p><a href="https://docs.mongodb.com/manual/core/replica-set-write-concern/">关于write-concern</a></p>
<h2 id="参考文档">参考文档</h2>
<p><strong>搭建高可用mongodb集群</strong><br>
<a href="http://www.lanceyan.com/tech/mongodb_repset2.html" class="uri">http://www.lanceyan.com/tech/mongodb_repset2.html</a></p>
<p><strong>深入浅出Mongodb复制</strong><br>
<a href="http://www.mongoing.com/archives/5200" class="uri">http://www.mongoing.com/archives/5200</a></p>
<p>mongodb副本集原理<br>
<a href="https://yq.aliyun.com/articles/64?spm=0.0.0.0.9jrPm8" class="uri">https://yq.aliyun.com/articles/64?spm=0.0.0.0.9jrPm8</a></p>
<p>副本集主备切换<br>
<a href="https://docs.mongodb.com/manual/tutorial/force-member-to-be-primary/index.html" class="uri">https://docs.mongodb.com/manual/tutorial/force-member-to-be-primary/index.html</a></p>

</div>
<div id="MySignature" style="display: block;"><div style="background-color: #7c7f7c1c; font-size: small; padding: 5px">

<div style="float: left; padding-right: 15px"> 
<img src="https://images.cnblogs.com/cnblogs_com/littleatp/1241412/o_qrcode_for_gh_b2cf486409a0_258.jpg" style="width: 120px; height: 120px">
</div>

<div style="padding-top: 15px">
<p>
作者： 
<a href="http://www.cnblogs.com/littleatp/">美码师(zale)</a>
</p>
<p>
出处：
<a href="http://www.cnblogs.com/littleatp/">http://www.cnblogs.com/littleatp/</a>, 如果喜欢我的文章，请<b style="font-size: medium">关注我的公众号</b>
</p>

<p>
本文版权归作者和博客园共有，欢迎转载，但未经作者同意必须保留此段声明，且在文章页面明显位置给出
<a href="#" style="color: #0; font-size: medium">原文链接</a>
&nbsp;如有问题， 可留言咨询.
</p>

</div>
<div style="clear: both"></div>

</div></div>
<div class="clear"></div>
<div id="blog_post_info_block"><div id="BlogPostCategory">
分类: 
<a href="https://www.cnblogs.com/littleatp/category/1209630.html" target="_blank">5.数据库中间件</a></div>
<div id="EntryTag">
标签: 
<a href="https://www.cnblogs.com/littleatp/tag/mongodb/">mongodb</a></div>

<div id="blog_post_info">
<div id="green_channel">
<a href="javascript:void(0);" id="green_channel_digg" onclick="DiggIt(8562842,cb_blogId,1);green_channel_success(this,'谢谢推荐！');">好文要顶</a>
<a id="green_channel_follow" onclick="follow('14b7cf8b-2d08-e011-ac81-842b2b196315');" href="javascript:void(0);">关注我</a>
<a id="green_channel_favorite" onclick="AddToWz(cb_entryId);return false;" href="javascript:void(0);">收藏该文</a>
<a id="green_channel_weibo" href="javascript:void(0);" title="分享至新浪微博" onclick="ShareToTsina()"><img src="https://common.cnblogs.com/images/icon_weibo_24.png" alt=""></a>
<a id="green_channel_wechat" href="javascript:void(0);" title="分享至微信" onclick="shareOnWechat()"><img src="https://common.cnblogs.com/images/wechat.png" alt=""></a>
</div>
<div id="author_profile">
<div id="author_profile_info" class="author_profile_info">
<a href="https://home.cnblogs.com/u/littleatp/" target="_blank"><img src="https://pic.cnblogs.com/face/242916/20180527184904.png" class="author_avatar" alt=""></a>
<div id="author_profile_detail" class="author_profile_info">
<a href="https://home.cnblogs.com/u/littleatp/">美码师</a><br>
<a href="https://home.cnblogs.com/u/littleatp/followees/">关注 - 9</a><br>
<a href="https://home.cnblogs.com/u/littleatp/followers/">粉丝 - 116</a>
</div>
</div>
<div class="clear"></div>
<div id="author_profile_honor"></div>
<div id="author_profile_follow">
<a href="javascript:void(0);" onclick="follow('14b7cf8b-2d08-e011-ac81-842b2b196315');return false;">+加关注</a>
</div>
</div>
<div id="div_digg">
<div class="diggit" onclick="votePost(8562842,'Digg')">
<span class="diggnum" id="digg_count">0</span>
</div>
<div class="buryit" onclick="votePost(8562842,'Bury')">
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

<a href="https://www.cnblogs.com/littleatp/p/8419796.html" class="p_n_p_prefix">« </a> 上一篇：    <a href="https://www.cnblogs.com/littleatp/p/8419796.html" title="发布于 2018-02-05 23:03">redis通过pipeline提升吞吐量</a>
<br>
<a href="https://www.cnblogs.com/littleatp/p/8562931.html" class="p_n_p_prefix">» </a> 下一篇：    <a href="https://www.cnblogs.com/littleatp/p/8562931.html" title="发布于 2018-03-13 21:58">mongodb分片扩展架构</a>

</div>
</div>
</div>
<div class="postDesc">posted @ 
<span id="post-date">2018-03-13 21:51</span>&nbsp;
<a href="https://www.cnblogs.com/littleatp/">美码师</a>&nbsp;
阅读(<span id="post_view_count">5101</span>)&nbsp;
评论(<span id="post_comment_count">0</span>)&nbsp;
<a href="https://i.cnblogs.com/EditPosts.aspx?postid=8562842" rel="nofollow">编辑</a>&nbsp;
<a href="javascript:void(0)" onclick="AddToWz(8562842);return false;">收藏</a></div>
</div>
<script src="https://common.cnblogs.com/highlight/9.12.0/highlight.min.js"></script>
<script>markdown_highlight();</script>
<script>
var allowComments = true, cb_blogId = 129222, cb_blogApp = 'littleatp', cb_blogUserGuid = '14b7cf8b-2d08-e011-ac81-842b2b196315';
var cb_entryId = 8562842, cb_entryCreatedDate = '2018-03-13 21:51', cb_postType = 1; 
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
<div id="ad_t2"><a href="http://www.ucancode.com/index.htm" target="_blank" onclick="ga('send', 'event', 'Link', 'click', 'T2-ucancode')">【推荐】超50万行VC++源码: 大型组态工控、电力仿真CAD与GIS源码库</a><br><a href="https://cloud.tencent.com/act/season?fromSource=gwzcw.3422970.3422970.3422970&amp;utm_medium=cpc&amp;utm_id=gwzcw.3422970.3422970.3422970" target="_blank" onclick="ga('send', 'event', 'Link', 'click', 'T2-腾讯云')">【活动】腾讯云服务器推出云产品采购季 1核2G首年仅需99元</a><br><a href="https://developer.aliyun.com/article/714279?utm_content=g_1000088939" target="_blank" onclick="ga('send', 'event', 'Link', 'click', 'T2-阿里云开发者社区')">【推荐】阿里毕玄16篇文章，深度讲解Java开发、系统设计、职业发展</a><br><a href="https://developer.aliyun.com/article/715141?utm_content=g_1000088938" target="_blank" onclick="ga('send', 'event', 'Link', 'click', 'T2-阿里云开发者社区')">【推荐】12知识点+20干货案例+110面试题，助你拿offer | Python面试宝典</a><br></div>
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
<div id="cnblogs_c1" class="c_ad_block">
<div id="div-gpt-ad-1546353474406-0" style="height:250px; width:300px;" data-google-query-id="CPnc5YOy5OcCFc0OXAodLhkEdg"><div id="google_ads_iframe_/1090369/C1_0__container__" style="border: 0pt none;"><iframe id="google_ads_iframe_/1090369/C1_0" title="3rd party ad content" name="google_ads_iframe_/1090369/C1_0" width="300" height="250" scrolling="no" marginwidth="0" marginheight="0" frameborder="0" srcdoc="" style="border: 0px; vertical-align: bottom;" data-google-container-id="1" data-load-complete="true"></iframe></div></div>
</div>
<div id="under_post_news"><div class="recomm-block"><b>相关博文：</b><br>·  <a title="MongoDB副本集学习(一)：概述和环境搭建" href="https://www.cnblogs.com/zhanjindong/p/3251330.html" target="_blank" onclick="clickRecomItmem(3251330)">MongoDB副本集学习(一)：概述和环境搭建</a><br>·  <a title="Mongodb主从复制/ 副本集/分片集群介绍" href="https://www.cnblogs.com/kevingrace/p/5685486.html" target="_blank" onclick="clickRecomItmem(5685486)">Mongodb主从复制/ 副本集/分片集群介绍</a><br>·  <a title="mongodb副本集架构搭建" href="https://www.cnblogs.com/dennisit/archive/2013/01/28/2880166.html" target="_blank" onclick="clickRecomItmem(2880166)">mongodb副本集架构搭建</a><br>·  <a title="搭建高可用mongodb集群（二）—— 副本集" href="https://www.cnblogs.com/lanceyan/p/3497124.html" target="_blank" onclick="clickRecomItmem(3497124)">搭建高可用mongodb集群（二）—— 副本集</a><br>·  <a title="MongoDB（五）-- 副本集（replica Set）" href="https://www.cnblogs.com/xbq8080/p/7231548.html" target="_blank" onclick="clickRecomItmem(7231548)">MongoDB（五）-- 副本集（replica Set）</a><br>»  <a target="_blank" href="https://recomm.cnblogs.com/blogpost/8562842">更多推荐...</a><div id="cnblogs_t5"><a href="https://developer.aliyun.com/article/718649?utm_content=g_1000088936" target="_blank" onclick="ga('send', 'event', 'Link', 'click', 'T5-阿里云开发者社区')">这6种编码方法，你掌握了几个？</a></div></div></div>
<div id="cnblogs_c2" class="c_ad_block">
<div id="div-gpt-ad-1539008685004-0" style="height:60px; width:468px;" data-google-query-id="CPrc5YOy5OcCFc0OXAodLhkEdg">
<script>
if (new Date() >= new Date(2018, 9, 13)) {
googletag.cmd.push(function () { googletag.display("div-gpt-ad-1539008685004-0"); });
}
</script>
<div id="google_ads_iframe_/1090369/C2_0__container__" style="border: 0pt none; width: 468px; height: 60px;"></div></div>
</div>
<div id="under_post_kb">
<div class="itnews c_ad_block">
<b>最新 IT 新闻</b>:
<br>
·              <a href="//news.cnblogs.com/n/656143/" target="_blank">特斯拉超级工厂二期列入上海市重大预备项目</a>
<br>
·              <a href="//news.cnblogs.com/n/656157/" target="_blank">苹果研究配环绕触摸屏全玻璃iPhone 可以在任意表面显示信息</a>
<br>
·              <a href="//news.cnblogs.com/n/656156/" target="_blank">猎豹移动回应被谷歌下架APP：未通知的情况下被谷歌单方面下架</a>
<br>
·              <a href="//news.cnblogs.com/n/656148/" target="_blank">Project xCloud 落地移动终端，微软这一次要走群众路线</a>
<br>
·              <a href="//news.cnblogs.com/n/656151/" target="_blank">华为发布新一代5G网络解决方案，加速5G生态发展</a>
<br>
» <a href="https://news.cnblogs.com/" title="IT 新闻" target="_blank">更多新闻...</a>
</div></div>
<div id="HistoryToday" class="c_ad_block"></div>
<script type="text/javascript">
fixPostBody();
setTimeout(function() { incrementViewCount(cb_entryId); }, 50);        deliverAdT2();
deliverAdC1();
deliverAdC2();
loadNewsAndKb();
loadBlogSignature();
LoadPostCategoriesTags(cb_blogId, cb_entryId);        LoadPostInfoBlock(cb_blogId, cb_entryId, cb_blogApp, cb_blogUserGuid);
GetPrevNextPost(cb_entryId, cb_blogId, cb_entryCreatedDate, cb_postType);
loadOptUnderPost();
GetHistoryToday(cb_blogId, cb_blogApp, cb_entryCreatedDate);
</script>
</div>    </div>
</div>
</div>
</div>

参考：https://www.cnblogs.com/littleatp/p/8562842.html