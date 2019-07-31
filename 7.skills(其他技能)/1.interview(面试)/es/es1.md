<div class="post">
<h1 class="postTitle"><a id="cb_post_title_url" class="postTitle2" href="https://www.cnblogs.com/daiwei1981/p/9403970.html">分布式搜索的面试题1</a></h1>
<div id="cnblogs_post_body" class="blogpost-body"><p>&nbsp;</p>
<p>1<span style="font-family: 宋体;">、面试题</span></p>
<p>&nbsp;</p>
<p>es<span style="font-family: 宋体;">的</span><span style="font-family: 宋体;">分布式架构原理能说一下么（</span>es<span style="font-family: 宋体;">是</span>如何实现分布式的啊）？</p>
<p>&nbsp;</p>
<p>2<span style="font-family: 宋体;">、面试官心里分析</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">在搜索这块，</span>lucene<span style="font-family: 宋体;">是最流行的搜索库。几年前业内一般都问，你了解</span><span style="font-family: Calibri;">lucene</span><span style="font-family: 宋体;">吗？你知道倒排索引的原理吗？现在早已经</span><span style="font-family: Calibri;">out</span><span style="font-family: 宋体;">了，因为现在很多项目都是直接用基于</span><span style="font-family: Calibri;">lucene</span><span style="font-family: 宋体;">的分布式搜索引擎——</span><span style="font-family: Calibri;">elasticsearch</span><span style="font-family: 宋体;">，简称为</span><span style="font-family: Calibri;">es</span><span style="font-family: 宋体;">。</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">而现在分布式搜索基本已经成为大部分互联网行业的</span>java<span style="font-family: 宋体;">系统的标配，其中尤为流行的就是</span><span style="font-family: Calibri;">es</span><span style="font-family: 宋体;">，前几年</span><span style="font-family: Calibri;">es</span><span style="font-family: 宋体;">没火的时候，大家一般用</span><span style="font-family: Calibri;">solr</span><span style="font-family: 宋体;">。但是这两年基本大部分企业和项目都开始转向</span><span style="font-family: Calibri;">es</span><span style="font-family: 宋体;">了。</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">所以互联网面试，肯定会跟你聊聊分布式搜索引擎，也就一定会聊聊</span>es<span style="font-family: 宋体;">，如果你确实不知道，那你真的就</span><span style="font-family: Calibri;">out</span><span style="font-family: 宋体;">了。</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">如果面试官问你第一个问题，确实一般都会问你</span>es<span style="font-family: 宋体;">的分布式架构设计能介绍一下么？就看看你对分布式搜索引擎架构的一个基本理解。</span></p>
<p>&nbsp;</p>
<p>3<span style="font-family: 宋体;">、额外的友情提示</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">同学啊，如果你看到这里发现自己对</span>es<span style="font-family: 宋体;">一无所知，没事儿，保持淡定，暂停一下课程。然后上百度搜一下</span><span style="font-family: Calibri;">es</span><span style="font-family: 宋体;">是啥？本机启动个</span><span style="font-family: Calibri;">es</span><span style="font-family: 宋体;">？然后写个</span><span style="font-family: Calibri;">es</span><span style="font-family: 宋体;">的</span><span style="font-family: Calibri;">hello world</span><span style="font-family: 宋体;">感受一下？然后搜个帖子把</span><span style="font-family: Calibri;">es</span><span style="font-family: 宋体;">常见的几个操作都执行一遍（聚合、常见搜索语法之类的）？</span><span style="font-family: Calibri;">ok</span><span style="font-family: 宋体;">了，</span><span style="font-family: Calibri;">1~2</span><span style="font-family: 宋体;">小时熟悉足够了，回来吧，继续看我们的课程。</span></p>
<p>&nbsp;</p>
<p>4<span style="font-family: 宋体;">、面试题剖析</span></p>
<p>&nbsp;</p>
<p>elasticsearch<span style="font-family: 宋体;">设计的理念就是分布式搜索引擎，底层其实还是基于</span><span style="font-family: Calibri;">lucene</span><span style="font-family: 宋体;">的。</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">核心思想就是在多台机器上启动多个</span>es<span style="font-family: 宋体;">进程实例，组成了一个</span><span style="font-family: Calibri;">es</span><span style="font-family: 宋体;">集群。</span></p>
<p>&nbsp;</p>
<p>es<span style="font-family: 宋体;">中存储数据的基本单位是索引，比如说你现在要在</span><span style="font-family: Calibri;">es</span><span style="font-family: 宋体;">中存储一些订单数据，你就应该在</span><span style="font-family: Calibri;">es</span><span style="font-family: 宋体;">中创建一个索引，</span><span style="font-family: Calibri;">order_idx</span><span style="font-family: 宋体;">，所有的订单数据就都写到这个索引里面去，一个索引差不多就是相当于是</span><span style="font-family: Calibri;">mysql</span><span style="font-family: 宋体;">里的一张表。</span><span style="font-family: Calibri;">index -&gt; type -&gt; mapping -&gt; document -&gt; field</span><span style="font-family: 宋体;">。</span></p>
<p>&nbsp;</p>
<p>index<span style="font-family: 宋体;">：</span><span style="font-family: Calibri;">mysql</span><span style="font-family: 宋体;">里的一张表</span></p>
<p>&nbsp;</p>
<p>type<span style="font-family: 宋体;">：没法跟</span><span style="font-family: Calibri;">mysql</span><span style="font-family: 宋体;">里去对比，一个</span><span style="font-family: Calibri;">index</span><span style="font-family: 宋体;">里可以有多个</span><span style="font-family: Calibri;">type</span><span style="font-family: 宋体;">，每个</span><span style="font-family: Calibri;">type</span><span style="font-family: 宋体;">的字段都是差不多的，但是有一些略微的差别。</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">好比说，有一个</span>index<span style="font-family: 宋体;">，是订单</span><span style="font-family: Calibri;">index</span><span style="font-family: 宋体;">，里面专门是放订单数据的。就好比说你在</span><span style="font-family: Calibri;">mysql</span><span style="font-family: 宋体;">中建表，有些订单是实物商品的订单，就好比说一件衣服，一双鞋子；有些订单是虚拟商品的订单，就好比说游戏点卡，话费充值。就两种订单大部分字段是一样的，但是少部分字段可能有略微的一些差别。</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">所以就会在订单</span>index<span style="font-family: 宋体;">里，建两个</span><span style="font-family: Calibri;">type</span><span style="font-family: 宋体;">，一个是实物商品订单</span><span style="font-family: Calibri;">type</span><span style="font-family: 宋体;">，一个是虚拟商品订单</span><span style="font-family: Calibri;">type</span><span style="font-family: 宋体;">，这两个</span><span style="font-family: Calibri;">type</span><span style="font-family: 宋体;">大部分字段是一样的，少部分字段是不一样的。</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">很多情况下，一个</span>index<span style="font-family: 宋体;">里可能就一个</span><span style="font-family: Calibri;">type</span><span style="font-family: 宋体;">，但是确实如果说是一个</span><span style="font-family: Calibri;">index</span><span style="font-family: 宋体;">里有多个</span><span style="font-family: Calibri;">type</span><span style="font-family: 宋体;">的情况，你可以认为</span><span style="font-family: Calibri;">index</span><span style="font-family: 宋体;">是一个类别的表，具体的每个</span><span style="font-family: Calibri;">type</span><span style="font-family: 宋体;">代表了具体的一个</span><span style="font-family: Calibri;">mysql</span><span style="font-family: 宋体;">中的表</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">每个</span>type<span style="font-family: 宋体;">有一个</span><span style="font-family: Calibri;">mapping</span><span style="font-family: 宋体;">，如果你认为一个</span><span style="font-family: Calibri;">type</span><span style="font-family: 宋体;">是一个具体的一个表，</span><span style="font-family: Calibri;">index</span><span style="font-family: 宋体;">代表了多个</span><span style="font-family: Calibri;">type</span><span style="font-family: 宋体;">的同属于的一个类型，</span><span style="font-family: Calibri;">mapping</span><span style="font-family: 宋体;">就是这个</span><span style="font-family: Calibri;">type</span><span style="font-family: 宋体;">的表结构定义，你在</span><span style="font-family: Calibri;">mysql</span><span style="font-family: 宋体;">中创建一个表，肯定是要定义表结构的，里面有哪些字段，每个字段是什么类型。。。</span></p>
<p>&nbsp;</p>
<p>mapping<span style="font-family: 宋体;">就代表了这个</span><span style="font-family: Calibri;">type</span><span style="font-family: 宋体;">的表结构的定义，定义了这个</span><span style="font-family: Calibri;">type</span><span style="font-family: 宋体;">中每个字段名称，字段是什么类型的，然后还有这个字段的各种配置</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">实际上你往</span>index<span style="font-family: 宋体;">里的一个</span><span style="font-family: Calibri;">type</span><span style="font-family: 宋体;">里面写的一条数据，叫做一条</span><span style="font-family: Calibri;">document</span><span style="font-family: 宋体;">，一条</span><span style="font-family: Calibri;">document</span><span style="font-family: 宋体;">就代表了</span><span style="font-family: Calibri;">mysql</span><span style="font-family: 宋体;">中某个表里的一行给，每个</span><span style="font-family: Calibri;">document</span><span style="font-family: 宋体;">有多个</span><span style="font-family: Calibri;">field</span><span style="font-family: 宋体;">，每个</span><span style="font-family: Calibri;">field</span><span style="font-family: 宋体;">就代表了这个</span><span style="font-family: Calibri;">document</span><span style="font-family: 宋体;">中的一个字段的值</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">接着你搞一个索引，这个索引可以拆分成多个</span>shard<span style="font-family: 宋体;">，每个</span><span style="font-family: Calibri;">shard</span><span style="font-family: 宋体;">存储部分数据。</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">接着就是这个</span>shard<span style="font-family: 宋体;">的数据实际是有多个备份，就是说每个</span><span style="font-family: Calibri;">shard</span><span style="font-family: 宋体;">都有一个</span><span style="font-family: Calibri;">primary shard</span><span style="font-family: 宋体;">，负责写入数据，但是还有几个</span><span style="font-family: Calibri;">replica shard</span><span style="font-family: 宋体;">。</span><span style="font-family: Calibri;">primary shard</span><span style="font-family: 宋体;">写入数据之后，会将数据同步到其他几个</span><span style="font-family: Calibri;">replica shard</span><span style="font-family: 宋体;">上去。</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">通过这个</span>replica<span style="font-family: 宋体;">的方案，每个</span><span style="font-family: Calibri;">shard</span><span style="font-family: 宋体;">的数据都有多个备份，如果某个机器宕机了，没关系啊，还有别的数据副本在别的机器上呢。高可用了吧。</span></p>
<p>&nbsp;</p>
<p>es<span style="font-family: 宋体;">集群多个节点，会自动选举一个节点为</span><span style="font-family: Calibri;">master</span><span style="font-family: 宋体;">节点，这个</span><span style="font-family: Calibri;">master</span><span style="font-family: 宋体;">节点其实就是干一些管理的工作的，比如维护索引元数据拉，负责切换</span><span style="font-family: Calibri;">primary shard</span><span style="font-family: 宋体;">和</span><span style="font-family: Calibri;">replica shard</span><span style="font-family: 宋体;">身份拉，之类的。</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">要是</span>master<span style="font-family: 宋体;">节点宕机了，那么会重新选举一个节点为</span><span style="font-family: Calibri;">master</span><span style="font-family: 宋体;">节点。</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">如果是非</span>master<span style="font-family: 宋体;">节点宕机了，那么会由</span><span style="font-family: Calibri;">master</span><span style="font-family: 宋体;">节点，让那个宕机节点上的</span><span style="font-family: Calibri;">primary shard</span><span style="font-family: 宋体;">的身份转移到其他机器上的</span><span style="font-family: Calibri;">replica shard</span><span style="font-family: 宋体;">。急着你要是修复了那个宕机机器，重启了之后，</span><span style="font-family: Calibri;">master</span><span style="font-family: 宋体;">节点会控制将缺失的</span><span style="font-family: Calibri;">replica shard</span><span style="font-family: 宋体;">分配过去，同步后续修改的数据之类的，让集群恢复正常。</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">其实上述就是</span>elasticsearch<span style="font-family: 宋体;">作为一个分布式搜索引擎最基本的一个架构设计</span></p>
<p>&nbsp;</p>
<p>&nbsp;<img src="https://images2018.cnblogs.com/blog/918692/201808/918692-20180801211633549-1183843351.png" alt=""></p>
<p>&nbsp;</p></div><div id="MySignature"></div>
<div class="clear"></div>
<div id="blog_post_info_block">
<div id="BlogPostCategory"></div>
<div id="EntryTag"></div>
<div id="blog_post_info"><div id="green_channel">
<a href="javascript:void(0);" id="green_channel_digg" onclick="DiggIt(9403970,cb_blogId,1);green_channel_success(this,'谢谢推荐！');">好文要顶</a>
<a id="green_channel_follow" onclick="follow('fb39f011-47ef-e511-9fc1-ac853d9f53cc');" href="javascript:void(0);">关注我</a>
<a id="green_channel_favorite" onclick="AddToWz(cb_entryId);return false;" href="javascript:void(0);">收藏该文</a>
<a id="green_channel_weibo" href="javascript:void(0);" title="分享至新浪微博" onclick="ShareToTsina()"><img src="//common.cnblogs.com/images/icon_weibo_24.png" alt=""></a>
<a id="green_channel_wechat" href="javascript:void(0);" title="分享至微信" onclick="shareOnWechat()"><img src="//common.cnblogs.com/images/wechat.png" alt=""></a>
</div>
<div id="author_profile">
<div id="author_profile_info" class="author_profile_info">
<a href="https://home.cnblogs.com/u/daiwei1981/" target="_blank"><img src="//pic.cnblogs.com/face/918692/20160622171901.png" class="author_avatar" alt=""></a>
<div id="author_profile_detail" class="author_profile_info">
<a href="https://home.cnblogs.com/u/daiwei1981/">伪全栈的java工程师</a><br>
<a href="https://home.cnblogs.com/u/daiwei1981/followees">关注 - 25</a><br>
<a href="https://home.cnblogs.com/u/daiwei1981/followers">粉丝 - 67</a>
</div>
</div>
<div class="clear"></div>
<div id="author_profile_honor"></div>
<div id="author_profile_follow">
<a href="javascript:void(0);" onclick="follow('fb39f011-47ef-e511-9fc1-ac853d9f53cc');return false;">+加关注</a>
</div>
</div>
<div id="div_digg">
<div class="diggit" onclick="votePost(9403970,'Digg')">
<span class="diggnum" id="digg_count">0</span>
</div>
<div class="buryit" onclick="votePost(9403970,'Bury')">
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
<div id="post_next_prev"><a href="https://www.cnblogs.com/daiwei1981/p/9403925.html" class="p_n_p_prefix">« </a> 上一篇：<a href="https://www.cnblogs.com/daiwei1981/p/9403925.html" title="发布于2018-08-01 21:06">消息队列的面试题7</a><br><a href="https://www.cnblogs.com/daiwei1981/p/9411482.html" class="p_n_p_prefix">» </a> 下一篇：<a href="https://www.cnblogs.com/daiwei1981/p/9411482.html" title="发布于2018-08-03 09:03">分布式搜索的面试题2</a><br></div>
</div>


<div class="postDesc">posted on <span id="post-date">2018-08-01 21:17</span> <a href="https://www.cnblogs.com/daiwei1981/">伪全栈的java工程师</a> 阅读(<span id="post_view_count">579</span>) 评论(<span id="post_comment_count">0</span>)  <a href="https://i.cnblogs.com/EditPosts.aspx?postid=9403970" rel="nofollow">编辑</a> <a href="#" onclick="AddToWz(9403970);return false;">收藏</a></div>
</div>