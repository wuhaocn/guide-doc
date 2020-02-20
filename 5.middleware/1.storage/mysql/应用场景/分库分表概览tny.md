<div id="mainContent">
<div class="forFlow">
<div id="post_detail">
<!--done-->
<div id="topics">
<div class="post">
<h1 class="postTitle">

<a id="cb_post_title_url" class="postTitle2" href="https://www.cnblogs.com/aksir/p/9085694.html">MySQL 分库分表方案，总结的非常好！</a>

</h1>
<div class="clear"></div>
<div class="postBody">

<div id="cnblogs_post_body" class="blogpost-body ">
<p><span style="font-size: 1.5em;">前言</span></p>
<p>公司最近在搞服务分离，数据切分方面的东西，因为单张包裹表的数据量实在是太大，并且还在以每天60W的量增长。</p>
<p>之前了解过数据库的分库分表，读过几篇博文，但就只知道个模糊概念， 而且现在回想起来什么都是模模糊糊的。</p>
<p>今天看了一下午的数据库分库分表，看了很多文章，现在做个总结，“摘抄”下来。（但更期待后期的实操） 会从以下几个方面说起：&nbsp;</p>
<p>第一部分：实际网站发展过程中面临的问题。&nbsp;</p>
<p>第二部分：有哪几种切分方式，垂直和水平的区别和适用面。</p>
<p>第三部分：目前市面有的一些开源产品，技术，它们的优缺点是什么。</p>
<p>第四部分：可能是最重要的，为什么不建议水平分库分表！？这能让你能在规划前期谨慎的对待，规避掉切分造成的问题。</p>
<h3>名词解释</h3>
<p>库：<strong>database</strong>；表：<strong>table</strong>；分库分表：<strong>sharding</strong></p>
<h2>数据库架构演变</h2>
<p>刚开始我们只用单机数据库就够了，随后面对越来越多的请求，我们将数据库的<strong>写操作</strong>和<strong>读操作</strong>进行分离， 使用多个从库副本（<strong>Slaver Replication</strong>）负责读，使用主库（<strong>Master</strong>）负责写， 从库从主库同步更新数据，保持数据一致。架构上就是数据库主从同步。 从库可以水平扩展，所以更多的读请求不成问题。</p>
<p>但是当用户量级上来后，写请求越来越多，该怎么办？加一个Master是不能解决问题的， 因为数据要保存一致性，写操作需要2个master之间同步，相当于是重复了，而且更加复杂。</p>
<p>这时就需要用到分库分表（<strong>sharding</strong>），对写操作进行切分。</p>
<h2>分库分表前的问题</h2>
<p>任何问题都是太大或者太小的问题，我们这里面对的数据量太大的问题。</p>
<h3>用户请求量太大</h3>
<p>因为单服务器TPS，内存，IO都是有限的。</p>
<p>解决方法：分散请求到多个服务器上； 其实用户请求和执行一个sql查询是本质是一样的，都是请求一个资源，只是用户请求还会经过网关，路由，http服务器等。</p>
<h3>单库太大</h3>
<p>单个数据库处理能力有限；单库所在服务器上磁盘空间不足；</p>
<p>单库上操作的IO瓶颈 解决方法：切分成更多更小的库</p>
<h3>单表太大</h3>
<p>CRUD都成问题；索引膨胀，查询超时</p>
<p>解决方法：切分成多个数据集更小的表。</p>
<h2>分库分表的方式方法</h2>
<p>一般就是<strong>垂直切分</strong>和<strong>水平切分</strong>，这是一种结果集描述的切分方式，是物理空间上的切分。</p>
<p>我们从面临的问题，开始解决，阐述： 首先是用户请求量太大，我们就堆机器搞定（这不是本文重点）。</p>
<p>然后是单个库太大，这时我们要看是因为<strong>表多而导致数据多</strong>，还是因为<strong>单张表里面的数据多</strong>。</p>
<p>如果是因为表多而数据多，使用垂直切分，根据业务切分成不同的库。</p>
<p>如果是因为单张表的数据量太大，这时要用水平切分，即把表的数据按<strong>某种规则</strong>切分成多张表，甚至多个库上的多张表。&nbsp;</p>
<p><strong>分库分表的顺序应该是先垂直分，后水平分。</strong>&nbsp;因为垂直分更简单，更符合我们处理现实世界问题的方式。</p>
<h3>垂直拆分</h3>
<ol class="list-paddingleft-2">
<li>
<p>垂直分表</p>
<p>也就是“大表拆小表”，基于列字段进行的。一般是表中的字段较多，将不常用的， 数据较大，长度较长（比如text类型字段）的拆分到“扩展表“。一般是针对那种几百列的大表，也避免查询时，数据量太大造成的“跨页”问题。</p>
</li>
<li>
<p>垂直分库</p>
<p>垂直分库针对的是一个系统中的不同业务进行拆分，比如用户User一个库，商品Producet一个库，订单Order一个库。 切分后，要放在多个服务器上，而不是一个服务器上。为什么？ 我们想象一下，一个购物网站对外提供服务，会有用户，商品，订单等的CRUD。没拆分之前， 全部都是落到单一的库上的，这会让数据库的<strong>单库处理能力成为瓶颈</strong>。按垂直分库后，如果还是放在一个数据库服务器上， 随着用户量增大，这会让<strong>单个数据库的处理能力成为瓶颈</strong>，还有<strong>单个服务器的磁盘空间，内存，tps等非常吃紧</strong>。 所以我们要拆分到多个服务器上，这样上面的问题都解决了，以后也不会面对单机资源问题。</p>
<p>数据库业务层面的拆分，和服务的“治理”，“降级”机制类似，也能对不同业务的数据分别的进行管理，维护，监控，扩展等。 数据库往往最容易成为应用系统的瓶颈，而数据库本身属于“有状态”的，相对于Web和应用服务器来讲，是比较难实现“横向扩展”的。 数据库的连接资源比较宝贵且单机处理能力也有限，在高并发场景下，垂直分库一定程度上能够突破IO、连接数及单机硬件资源的瓶颈。</p>
</li>
</ol>
<h3>水平拆分</h3>
<ol class="list-paddingleft-2">
<li>
<p>水平分表</p>
<p>针对数据量巨大的单张表（比如订单表），按照某种规则（<strong>RANGE</strong>,<strong>HASH取模</strong>等），切分到多张表里面去。 但是这些表还是在同一个库中，所以<strong>库级别的数据库操作还是有IO瓶颈</strong>。不建议采用。</p>
</li>
<li>
<p>水平分库分表</p>
<p>将单张表的数据切分到多个服务器上去，每个服务器具有相应的库与表，只是表中数据集合不同。 水平分库分表能够有效的缓解单机和单库的性能瓶颈和压力，突破IO、连接数、硬件资源等的瓶颈。</p>
</li>
<li>
<p>水平分库分表切分规则</p>
</li>
<ol class="list-paddingleft-2">
<li>
<p><strong>RANGE</strong></p>
<p>从0到10000一个表，10001到20000一个表；</p>
</li>
<li>
<p><strong>HASH取模</strong></p>
<p>一个商场系统，一般都是将用户，订单作为主表，然后将和它们相关的作为附表，这样不会造成跨库事务之类的问题。 取<strong>用户id</strong>，然后<strong>hash取模</strong>，分配到不同的数据库上。</p>
</li>
<li>
<p><strong>地理区域</strong></p>
<p>比如按照华东，华南，华北这样来区分业务，七牛云应该就是如此。</p>
</li>
<li>
<p><strong>时间</strong></p>
<p>按照时间切分，就是将6个月前，甚至一年前的数据切出去放到另外的一张表，因为随着时间流逝，这些表的数据 被查询的概率变小，所以没必要和“热数据”放在一起，这个也是“冷热数据分离”。</p>
</li>
</ol></ol>
<h2>分库分表后面临的问题</h2>
<h3>事务支持</h3>
<p>分库分表后，就成了<span style="color: #ff0000;"><strong>分布式事务</strong></span>了。</p>
<p>如果依赖数据库本身的分布式事务管理功能去执行事务，将付出高昂的性能代价； 如果由应用程序去协助控制，形成程序逻辑上的事务，又会造成编程方面的负担。</p>
<h3>多库结果集合并（group by，order by）</h3>
<p>TODO</p>
<h3>跨库join</h3>
<p>TODO 分库分表后表之间的关联操作将受到限制，我们无法join位于不同分库的表，也无法join分表粒度不同的表， 结果原本一次查询能够完成的业务，可能需要多次查询才能完成。粗略的解决方法： 全局表：基础数据，所有库都拷贝一份。 字段冗余：这样有些字段就不用join去查询了。 系统层组装：分别查询出所有，然后组装起来，较复杂。</p>
<h2>分库分表方案产品</h2>
<p>目前市面上的分库分表中间件相对较多，其中基于代理方式的有MySQL Proxy和Amoeba， 基于Hibernate框架的是Hibernate Shards，基于jdbc的有当当sharding-jdbc， 基于mybatis的类似maven插件式的有蘑菇街的蘑菇街TSharding， 通过重写spring的ibatis template类的Cobar Client。</p>
<p>还有一些大公司的开源产品：</p>
<p><img src="https://mmbiz.qpic.cn/mmbiz_jpg/tuSaKc6SfPoRLAFtmI4PGFzYzudYsr3FkFx3yAVxhI0sUJgWDSy6W0PYWvCsF2SYFtMLUoJ62NiaewdmSR8w0sw/640" alt="" data-copyright="0" data-ratio="0.6312785388127854" data-s="300,640" data-src="https://mmbiz.qpic.cn/mmbiz_jpg/tuSaKc6SfPoRLAFtmI4PGFzYzudYsr3FkFx3yAVxhI0sUJgWDSy6W0PYWvCsF2SYFtMLUoJ62NiaewdmSR8w0sw/640" data-type="jpeg" data-w="876" data-fail="0"></p>
</div>
<div id="MySignature"></div>
<div class="clear"></div>
<div id="blog_post_info_block"><div id="BlogPostCategory">
分类: 
<a href="https://www.cnblogs.com/aksir/category/992373.html" target="_blank">数据库</a></div>
<div id="EntryTag">
标签: 
<a href="https://www.cnblogs.com/aksir/tag/MySQL%E4%BC%98%E5%8C%96/">MySQL优化</a>,             <a href="https://www.cnblogs.com/aksir/tag/web%E9%AB%98%E5%B9%B6%E5%8F%91/">web高并发</a></div>

<div id="blog_post_info">
<div id="green_channel">
<a href="javascript:void(0);" id="green_channel_digg" onclick="DiggIt(9085694,cb_blogId,1);green_channel_success(this,'谢谢推荐！');">好文要顶</a>
<a id="green_channel_follow" onclick="follow('edea76d4-291f-e711-9fc1-ac853d9f53cc');" href="javascript:void(0);">关注我</a>
<a id="green_channel_favorite" onclick="AddToWz(cb_entryId);return false;" href="javascript:void(0);">收藏该文</a>
<a id="green_channel_weibo" href="javascript:void(0);" title="分享至新浪微博" onclick="ShareToTsina()"><img src="https://common.cnblogs.com/images/icon_weibo_24.png" alt=""></a>
<a id="green_channel_wechat" href="javascript:void(0);" title="分享至微信" onclick="shareOnWechat()"><img src="https://common.cnblogs.com/images/wechat.png" alt=""></a>
</div>
<div id="author_profile">
<div id="author_profile_info" class="author_profile_info">
<a href="https://home.cnblogs.com/u/aksir/" target="_blank"><img src="https://pic.cnblogs.com/face/1145093/20180513194706.png" class="author_avatar" alt=""></a>
<div id="author_profile_detail" class="author_profile_info">
<a href="https://home.cnblogs.com/u/aksir/">妖星杉木</a><br>
<a href="https://home.cnblogs.com/u/aksir/followees/">关注 - 0</a><br>
<a href="https://home.cnblogs.com/u/aksir/followers/">粉丝 - 28</a>
</div>
</div>
<div class="clear"></div>
<div id="author_profile_honor"></div>
<div id="author_profile_follow">
<a href="javascript:void(0);" onclick="follow('edea76d4-291f-e711-9fc1-ac853d9f53cc');return false;">+加关注</a>
</div>
</div>
<div id="div_digg">
<div class="diggit" onclick="votePost(9085694,'Digg')">
<span class="diggnum" id="digg_count">3</span>
</div>
<div class="buryit" onclick="votePost(9085694,'Bury')">
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

<a href="https://www.cnblogs.com/aksir/p/9085564.html" class="p_n_p_prefix">« </a> 上一篇：    <a href="https://www.cnblogs.com/aksir/p/9085564.html" title="发布于 2018-05-24 22:40">HTTPS科普扫盲</a>
<br>
<a href="https://www.cnblogs.com/aksir/p/9085777.html" class="p_n_p_prefix">» </a> 下一篇：    <a href="https://www.cnblogs.com/aksir/p/9085777.html" title="发布于 2018-05-24 23:09">优化 MySQL： 3 个简单的小调整</a>

</div>
</div>
</div>
<div class="postDesc">posted @ 
<span id="post-date">2018-05-24 22:59</span>&nbsp;
<a href="https://www.cnblogs.com/aksir/">妖星杉木</a>&nbsp;
阅读(<span id="post_view_count">9313</span>)&nbsp;
评论(<span id="post_comment_count">1</span>)&nbsp;
<a href="https://i.cnblogs.com/EditPosts.aspx?postid=9085694" rel="nofollow">编辑</a>&nbsp;
<a href="javascript:void(0)" onclick="AddToWz(9085694);return false;">收藏</a></div>
</div>


</div><!--end: topics 文章、评论容器-->
</div>
<script src="https://common.cnblogs.com/highlight/9.12.0/highlight.min.js"></script>
<script>markdown_highlight();</script>
<script>
var allowComments = true, cb_blogId = 347962, cb_blogApp = 'aksir', cb_blogUserGuid = 'edea76d4-291f-e711-9fc1-ac853d9f53cc';
var cb_entryId = 9085694, cb_entryCreatedDate = '2018-05-24 22:59', cb_postType = 1; 
loadViewCount(cb_entryId);
</script><a name="!comments"></a>
<div id="blog-comments-placeholder">

<div id="comment_pager_top">

</div>

<br>
<div class="feedback_area_title">评论列表</div>
<div class="feedbackNoItems"><div class="feedbackNoItems"></div></div>	
<div class="feedbackItem">
<div class="feedbackListSubtitle">
<div class="feedbackManage">
&nbsp;&nbsp;

<span class="comment_actions">




</span>


</div>

<a href="#4225664" class="layer">#1楼</a>
<a name="4225664" id="comment_anchor_4225664"></a>

<span id="comment-maxId" style="display:none">4225664</span>
<span id="comment-maxDate" style="display:none">2019/4/8 下午9:51:08</span>

<span class="comment_date">2019-04-08 21:51</span>



<a id="a_comment_author_4225664" href="https://home.cnblogs.com/u/1549731/" target="_blank">星岚工作室</a>

</div>
<div class="feedbackCon">

<div id="comment_body_4225664" class="blog_comment_body">
深度好文，感谢分享！另外，分库分表的技术产品，还有一个mycat，还不错用的。
</div>
<div class="comment_vote">
<span class="comment_error" style="color: red"></span>
<a href="javascript:void(0);" class="comment_digg" onclick="return voteComment(4225664, 'Digg', this.parentElement, false);">
支持(0)
</a>
<a href="javascript:void(0);" class="comment_burry" onclick="return voteComment(4225664, 'Bury', this.parentElement, false);">
反对(0)
</a>
</div>


</div>
</div>

<div id="comment_pager_bottom">

</div>


</div>
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
<div id="ad_t2"><a href="http://www.ucancode.com/index.htm" target="_blank" onclick="ga('send', 'event', 'Link', 'click', 'T2-ucancode')">【推荐】超50万行VC++源码: 大型组态工控、电力仿真CAD与GIS源码库</a><br><a href="https://cloud.tencent.com/act/season?fromSource=gwzcw.3422970.3422970.3422970&amp;utm_medium=cpc&amp;utm_id=gwzcw.3422970.3422970.3422970" target="_blank" onclick="ga('send', 'event', 'Link', 'click', 'T2-腾讯云')">【活动】腾讯云服务器推出云产品采购季 1核2G首年仅需99元</a><br><a href="https://cloud.baidu.com/campaign/Annualceremony-2020/index.html?track=cp:dsp|pf:pc|pp:chui-bokeyuan-huodong-20kainiancaigouji-yunchanpin1zhe-cpaxingshi-20200214|pu:cpa-xingshi|ci:2020ndsd|kw:2191279" target="_blank" onclick="ga('send', 'event', 'Link', 'click', 'T2-百度云')">【推荐】开年采购季，百度智能云全场云服务器低至1折起</a><br><a href="https://developer.aliyun.com/article/743591?utm_content=g_1000104138" target="_blank" onclick="ga('send', 'event', 'Link', 'click', 'T2-阿里云开发者社区')">【推荐】Java经典面试题整理及答案详解（一）</a><br><a href="https://developer.aliyun.com/ask/272880?utm_content=g_1000104136" target="_blank" onclick="ga('send', 'event', 'Link', 'click', 'T2-阿里云开发者社区')">【推荐】前端精品集合之JavaScript实战100例</a><br></div>
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
<div id="div-gpt-ad-1546353474406-0" style="height:250px; width:300px;" data-google-query-id="COuMzJz82ecCFSTUTAIdMNsISQ"><div id="google_ads_iframe_/1090369/C1_0__container__" style="border: 0pt none;"><iframe id="google_ads_iframe_/1090369/C1_0" title="3rd party ad content" name="google_ads_iframe_/1090369/C1_0" width="300" height="250" scrolling="no" marginwidth="0" marginheight="0" frameborder="0" srcdoc="" style="border: 0px; vertical-align: bottom;" data-google-container-id="1" data-load-complete="true"></iframe></div></div>
</div>
<div id="under_post_news"><div class="recomm-block"><b>相关博文：</b><br>·  <a title="分库分表的几种常见玩法及如何解决跨库查询等问题" href="https://www.cnblogs.com/dinglang/p/6076319.html" target="_blank" onclick="clickRecomItmem(6076319)">分库分表的几种常见玩法及如何解决跨库查询等问题</a><br>·  <a title="分库分表的几种常见玩法及如何解决跨库查询等问题" href="https://www.cnblogs.com/cxxjohnson/p/9048518.html" target="_blank" onclick="clickRecomItmem(9048518)">分库分表的几种常见玩法及如何解决跨库查询等问题</a><br>·  <a title="水平分库分表的关键问题及解决思路" href="https://www.cnblogs.com/dinglang/p/6084306.html" target="_blank" onclick="clickRecomItmem(6084306)">水平分库分表的关键问题及解决思路</a><br>·  <a title="分库分表带来的问题" href="https://www.cnblogs.com/wade-luffy/p/6096578.html" target="_blank" onclick="clickRecomItmem(6096578)">分库分表带来的问题</a><br>·  <a title="Mysql分库分表方案" href="https://www.cnblogs.com/try-better-tomorrow/p/4987620.html" target="_blank" onclick="clickRecomItmem(4987620)">Mysql分库分表方案</a><br>»  <a target="_blank" href="https://recomm.cnblogs.com/blogpost/9085694">更多推荐...</a><div id="cnblogs_t5"><a href="https://developer.aliyun.com/article/744670?utm_content=g_1000104140" target="_blank" onclick="ga('send', 'event', 'Link', 'click', 'T5-阿里云开发者社区')">如何在面试中成长？来看阿里前端终面官的面试心得</a></div></div></div>
<div id="cnblogs_c2" class="c_ad_block">
<div id="div-gpt-ad-1539008685004-0" style="height:60px; width:468px;" data-google-query-id="CPiczZz82ecCFSTUTAIdMNsISQ">

<div id="google_ads_iframe_/1090369/C2_0__container__" style="border: 0pt none;"><iframe id="google_ads_iframe_/1090369/C2_0" title="3rd party ad content" name="google_ads_iframe_/1090369/C2_0" width="468" height="60" scrolling="no" marginwidth="0" marginheight="0" frameborder="0" srcdoc="" style="border: 0px; vertical-align: bottom;" data-google-container-id="2" data-load-complete="true"></iframe></div></div>
</div>
<div id="under_post_kb">
<div class="itnews c_ad_block">
<b>最新 IT 新闻</b>:
<br>
·              <a href="//news.cnblogs.com/n/655916/" target="_blank">微软推出新版 Edge 路线图，Linux 支持在计划中</a>
<br>
·              <a href="//news.cnblogs.com/n/655915/" target="_blank">是谁吹响了几千亿只蝗虫的“冲锋号”</a>
<br>
·              <a href="//news.cnblogs.com/n/655914/" target="_blank">微软全新Android版Office APP正式发布：Word、Excel、PPT三合一</a>
<br>
·              <a href="//news.cnblogs.com/n/655913/" target="_blank">Ubuntu 20.04 LTS 有望提供&nbsp;PHP 7.4</a>
<br>
·              <a href="//news.cnblogs.com/n/655912/" target="_blank">“星链”五发：改发射剖面，一级落败</a>
<br>
» <a href="https://news.cnblogs.com/" title="IT 新闻" target="_blank">更多新闻...</a>
</div></div>
<div id="HistoryToday" class="c_ad_block">
<b>历史上的今天：</b>
<br>

2018-05-24    <a href="https://www.cnblogs.com/aksir/p/9085564.html">HTTPS科普扫盲</a>
<br>
2018-05-24    <a href="https://www.cnblogs.com/aksir/p/9081687.html">业务id转密文短链的一种实现思路</a>
<br>
</div>
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
</div>
</div><!--end: forFlow -->
</div>