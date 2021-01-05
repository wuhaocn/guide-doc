<div id="post_detail">
<div class="block">
	<h1 class="block_title">
<a id="cb_post_title_url" class="postTitle2" href="https://www.cnblogs.com/zhaoxinshanwei/p/9139827.html">Greys--JVM异常诊断工具</a>
</h1>
	<div class="post">
		<div class="postcontent">
			
<div id="cnblogs_post_body" class="blogpost-body ">
    <h4>https://github.com/oldmanpushcart/greys-anatomy/wiki/greys-pdf</h4>
<h4 id="一简介">一、简介</h4>
<blockquote>
<p>我们平时在线上或者开发中会遇到各种性能、功能等问题，在运行过程中需要查看方法入参、返回值，或者方法执行的堆栈时间，或者jar冲突时类加载来自那个jar包等问题。我们在开发过程中，可能会打印log日志、手动去打印入参、返回值等，或者自己实现简单的profile方法，代码侵入性大且效率较低；另外我们可以使用类似btrace工具去跟踪，这需要自己去实现btrace脚本，服务端需要启动agent，也有点小麻烦。后来淘宝聚石用scala写了个houseMD，但只支持到jdk1.6，后续也没有更新了；后面又有淘宝同学借鉴了btrace和houseMD，写了Greys，方便定位常见的java问题，下面简单介绍其使用方法。</p>
</blockquote>
<p>下载地址：<a href="http://ompc.oss.aliyuncs.com/greys/release/greys-1.7.6.4-bin.zip" target="_blank">http://ompc.oss.aliyuncs.com/greys/release/greys-1.7.6.4-bin.zip</a></p>
<h4 id="二安装">二、安装</h4>
<p>a. 解压greys-1.7.6.4-bin.zip，目录结构如下</p>
<pre class="prettyprint"><code class="hljs lasso css"><span class="hljs-attribute"><span class="hljs-selector-tag">-rwxr</span><span class="hljs-attribute"><span class="hljs-selector-tag">-xr</span><span class="hljs-attribute"><span class="hljs-selector-tag">-x</span><span class="hljs-built_in">. <span class="hljs-number">1 <span class="hljs-selector-tag">admin</span> <span class="hljs-selector-tag">admin</span>    <span class="hljs-number">1047 <span class="hljs-number">11月  <span class="hljs-number">7 <span class="hljs-number">11<span class="hljs-selector-pseudo">:</span><span class="hljs-number"><span class="hljs-selector-pseudo">54</span> <span class="hljs-selector-tag">ga</span><span class="hljs-built_in"><span class="hljs-selector-class">.sh</span>
<span class="hljs-attribute"><span class="hljs-selector-tag">-rw</span><span class="hljs-attribute"><span class="hljs-selector-tag">-r</span><span class="hljs-subst"><span class="hljs-selector-tag">--r</span><span class="hljs-subst"><span class="hljs-selector-tag">--</span><span class="hljs-built_in">. <span class="hljs-number">1 <span class="hljs-selector-tag">admin</span> <span class="hljs-selector-tag">admin</span>   <span class="hljs-number">10595 <span class="hljs-number">11月  <span class="hljs-number">7 <span class="hljs-number">11<span class="hljs-selector-pseudo">:</span><span class="hljs-number"><span class="hljs-selector-pseudo">54</span> <span class="hljs-selector-tag">greys</span><span class="hljs-attribute"><span class="hljs-selector-tag">-agent</span><span class="hljs-built_in"><span class="hljs-selector-class">.jar</span>
<span class="hljs-attribute"><span class="hljs-selector-tag">-rw</span><span class="hljs-attribute"><span class="hljs-selector-tag">-r</span><span class="hljs-subst"><span class="hljs-selector-tag">--r</span><span class="hljs-subst"><span class="hljs-selector-tag">--</span><span class="hljs-built_in">. <span class="hljs-number">1 <span class="hljs-selector-tag">admin</span> <span class="hljs-selector-tag">admin</span> <span class="hljs-number">3472230 <span class="hljs-number">11月  <span class="hljs-number">7 <span class="hljs-number">11<span class="hljs-selector-pseudo">:</span><span class="hljs-number"><span class="hljs-selector-pseudo">54</span> <span class="hljs-selector-tag">greys</span><span class="hljs-attribute"><span class="hljs-selector-tag">-core</span><span class="hljs-built_in"><span class="hljs-selector-class">.jar</span>
<span class="hljs-attribute"><span class="hljs-selector-tag">-rwxr</span><span class="hljs-attribute"><span class="hljs-selector-tag">-xr</span><span class="hljs-attribute"><span class="hljs-selector-tag">-x</span><span class="hljs-built_in">. <span class="hljs-number">1 <span class="hljs-selector-tag">admin</span> <span class="hljs-selector-tag">admin</span>    <span class="hljs-number">7972 <span class="hljs-number">11月  <span class="hljs-number">7 <span class="hljs-number">11<span class="hljs-selector-pseudo">:</span><span class="hljs-number"><span class="hljs-selector-pseudo">54</span> <span class="hljs-selector-tag">greys</span><span class="hljs-built_in"><span class="hljs-selector-class">.sh</span>
<span class="hljs-attribute"><span class="hljs-selector-tag">-rwxr</span><span class="hljs-attribute"><span class="hljs-selector-tag">-xr</span><span class="hljs-attribute"><span class="hljs-selector-tag">-x</span><span class="hljs-built_in">. <span class="hljs-number">1 <span class="hljs-selector-tag">admin</span> <span class="hljs-selector-tag">admin</span>    <span class="hljs-number">2927 <span class="hljs-number">11月  <span class="hljs-number">7 <span class="hljs-number">11<span class="hljs-selector-pseudo">:</span><span class="hljs-number"><span class="hljs-selector-pseudo">54</span> <span class="hljs-selector-tag">gs</span><span class="hljs-built_in"><span class="hljs-selector-class">.sh</span>
<span class="hljs-attribute"><span class="hljs-selector-tag">-rwxr</span><span class="hljs-attribute"><span class="hljs-selector-tag">-xr</span><span class="hljs-attribute"><span class="hljs-selector-tag">-x</span><span class="hljs-built_in">. <span class="hljs-number">1 <span class="hljs-selector-tag">admin</span> <span class="hljs-selector-tag">admin</span>     <span class="hljs-number">683 <span class="hljs-number">11月  <span class="hljs-number">7 <span class="hljs-number">11<span class="hljs-selector-pseudo">:</span><span class="hljs-number"><span class="hljs-selector-pseudo">54</span> <span class="hljs-selector-tag">install</span><span class="hljs-attribute"><span class="hljs-selector-tag">-local</span><span class="hljs-built_in"><span class="hljs-selector-class">.sh</span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></code></pre>
<p>b. 安装</p>
<pre class="prettyprint"><code class="hljs bash"><span class="hljs-built_in"><span class="hljs-built_in">cd</span> greys
sh ./install-local.sh</span></code></pre>
<h4 id="三待监控应用启动agent">三、待监控应用启动agent</h4>
<p>pid为应用进程号</p>
<pre class="prettyprint"><code class="hljs avrasm">./ga<span class="hljs-preprocessor">.sh pid
</span></code></pre>
<h4 id="三应用监控控制客户端">三、应用监控控制客户端</h4>
<p>pid 为应用进程号， ip为应用所在机器， 3658为agent默认端口号</p>
<pre class="prettyprint"><code class="hljs ruby">./greys.sh pid<span class="hljs-variable">@ip<span class="hljs-symbol"><span class="hljs-symbol">:</span><span class="hljs-number"><span class="hljs-number">3658</span>
</span></span></span></code></pre>
<p>或者远程访问</p>
<pre class="prettyprint"><code class="hljs avrasm">./gs<span class="hljs-preprocessor">.sh ip
</span></code></pre>
<h4 id="四greys命令详解">四、Greys命令详解</h4>
<table>
<thead>
<tr><th>命令</th><th>说明</th></tr>
</thead>
<tbody>
<tr>
<td>help</td>
<td>查看命令的帮助文档，每个命令和参数都有很详细的说明</td>
</tr>
<tr>
<td>sc</td>
<td>查看JVM已加载的类信息</td>
</tr>
<tr>
<td>sm</td>
<td>查看已加载的方法信息</td>
</tr>
<tr>
<td>monitor</td>
<td>方法执行监控</td>
</tr>
<tr>
<td>trace</td>
<td>渲染方法内部调用路径，并输出方法路径上的每个节点上耗时</td>
</tr>
<tr>
<td>ptrace</td>
<td>强化版的trace命令。通过指定渲染路径，并可记录下路径中所有方法的入参、返值；与tt命令联动</td>
</tr>
<tr>
<td>watch</td>
<td>方法执行数据观测</td>
</tr>
<tr>
<td>tt</td>
<td>方法执行数据的时空隧道，记录下指定方法每次调用的入参和返回信息，并能对这些不同的时间下调用进行观测</td>
</tr>
<tr>
<td>stack</td>
<td>输出当前方法被调用的调用路径</td>
</tr>
<tr>
<td>js</td>
<td>支持使用JavaScript脚本；支持CommonJS部分规范模块化（BMD规范）</td>
</tr>
<tr>
<td>version</td>
<td>输出当前目标Java进程所加载的Greys版本号</td>
</tr>
<tr>
<td>quit</td>
<td>退出greys客户端</td>
</tr>
<tr>
<td>shutdown</td>
<td>关闭greys服务端</td>
</tr>
<tr>
<td>rest</td>
<td>重置增强类，将被greys增强过的类全部还原</td>
</tr>
<tr>
<td>session</td>
<td>查看当前会话</td>
</tr>
<tr>
<td>jvm</td>
<td>查看当前JVM的信息</td>
</tr>
</tbody>
</table>
<p>示列：</p>
<pre class="prettyprint"><code class="hljs asciidoc ruby"><span class="hljs-header">tt -t -n <span class="hljs-number">100</span> *UserServiceImpl queryById
+----------+------------+----------------------+------------+----------+----------+-----------------+--------------------------------+--------------------------------+
<span class="hljs-header"><span class="hljs-params">|    INDEX |</span> PROCESS-ID <span class="hljs-params">|            TIMESTAMP |</span>   COST(ms) <span class="hljs-params">|   IS-RET |</span>   IS-EXP <span class="hljs-params">|          OBJECT |</span>                          CLASS <span class="hljs-params">|                         METHOD |</span>
+----------+------------+----------------------+------------+----------+----------+-----------------+--------------------------------+--------------------------------+
<span class="hljs-header"><span class="hljs-params">|     1001 |</span>       <span class="hljs-number">1001</span> <span class="hljs-params">|  2017-03-08 15:39:11 |</span>         <span class="hljs-number">10</span> <span class="hljs-params">|     <span class="hljs-literal">true</span> |</span>    <span class="hljs-literal">false</span> <span class="hljs-params">|      0x7204ebf1 |</span>    UserServiceImpl <span class="hljs-params">|          queryById |</span>
+----------+------------+----------------------+------------+----------+----------+-----------------+--------------------------------+--------------------------------+
<span class="hljs-header"><span class="hljs-params">|     1002 |</span>       <span class="hljs-number">1002</span> <span class="hljs-params">|  2017-03-08 15:39:12 |</span>          <span class="hljs-number">6</span> <span class="hljs-params">|     <span class="hljs-literal">true</span> |</span>    <span class="hljs-literal">false</span> <span class="hljs-params">|      0x7204ebf1 |</span>    UserServiceImpl <span class="hljs-params">|          queryById |</span>
+----------+------------+----------------------+------------+----------+----------+-----------------+--------------------------------+--------------------------------+
<span class="hljs-header"><span class="hljs-params">|     1003 |</span>       <span class="hljs-number">1003</span> <span class="hljs-params">|  2017-03-08 15:39:12 |</span>          <span class="hljs-number">5</span> <span class="hljs-params">|     <span class="hljs-literal">true</span> |</span>    <span class="hljs-literal">false</span> <span class="hljs-params">|      0x7204ebf1 |</span>    UserServiceImpl <span class="hljs-params">|          queryById |</span>
+----------+------------+----------------------+------------+----------+----------+-----------------+--------------------------------+--------------------------------+
<span class="hljs-header"><span class="hljs-params">|     1004 |</span>       <span class="hljs-number">1004</span> <span class="hljs-params">|  2017-03-08 15:39:13 |</span>          <span class="hljs-number">6</span> <span class="hljs-params">|     <span class="hljs-literal">true</span> |</span>    <span class="hljs-literal">false</span> <span class="hljs-params">|      0x7204ebf1 |</span>    UserServiceImpl <span class="hljs-params">|          queryById |</span>
+----------+------------+----------------------+------------+----------+----------+-----------------+--------------------------------+--------------------------------+
<span class="hljs-header"><span class="hljs-params">|     1005 |</span>       <span class="hljs-number">1005</span> <span class="hljs-params">|  2017-03-08 15:39:48 |</span>          <span class="hljs-number">6</span> <span class="hljs-params">|     <span class="hljs-literal">true</span> |</span>    <span class="hljs-literal">false</span> <span class="hljs-params">|      0x7204ebf1 |</span>    UserServiceImpl <span class="hljs-params">|          queryById |</span>
+----------+------------+----------------------+------------+----------+----------+-----------------+--------------------------------+--------------------------------+
<span class="hljs-header"><span class="hljs-params">|     1006 |</span>       <span class="hljs-number">1006</span> <span class="hljs-params">|  2017-03-08 15:39:48 |</span>          <span class="hljs-number">5</span> <span class="hljs-params">|     <span class="hljs-literal">true</span> |</span>    <span class="hljs-literal">false</span> <span class="hljs-params">|      0x7204ebf1 |</span>    UserServiceImpl <span class="hljs-params">|          queryById |</span>
+----------+------------+----------------------+------------+----------+----------+-----------------+--------------------------------+--------------------------------+
<span class="hljs-header"><span class="hljs-params">|     1007 |</span>       <span class="hljs-number">1007</span> <span class="hljs-params">|  2017-03-08 15:39:49 |</span>          <span class="hljs-number">4</span> <span class="hljs-params">|     <span class="hljs-literal">true</span> |</span>    <span class="hljs-literal">false</span> <span class="hljs-params">|      0x7204ebf1 |</span>    UserServiceImpl <span class="hljs-params">|          queryById |</span>
+----------+------------+----------------------+------------+----------+----------+-----------------+--------------------------------+--------------------------------+
<span class="hljs-params">|     1008 |</span>       <span class="hljs-number">1008</span> <span class="hljs-params">|  2017-03-08 15:39:49 |</span>          <span class="hljs-number">5</span> <span class="hljs-params">|     <span class="hljs-literal">true</span> |</span>    <span class="hljs-literal">false</span> <span class="hljs-params">|      0x7204ebf1 |</span>    UserServiceImpl <span class="hljs-params">|          queryById |</span></span></span></span></span></span></span></span></span></span></code></pre>
<p>具体使用详见：<a href="https://github.com/oldmanpushcart/greys-anatomy/wiki/greys-pdf" target="_blank">https://github.com/oldmanpushcart/greys-anatomy/wiki/greys-pdf</a></p>
</div>
<div id="MySignature"></div>
<div class="clear"></div>
<div id="blog_post_info_block"><div id="BlogPostCategory">
    分类: 
            <a href="https://www.cnblogs.com/zhaoxinshanwei/category/1082801.html" target="_blank">JVM</a></div>

<div id="blog_post_info">
<div id="green_channel">
        <a href="javascript:void(0);" id="green_channel_digg" onclick="DiggIt(9139827,cb_blogId,1);green_channel_success(this,'谢谢推荐！');">好文要顶</a>
        <a id="green_channel_follow" onclick="follow('4db17c66-aa43-e311-8d02-90b11c0b17d6');" href="javascript:void(0);">关注我</a>
    <a id="green_channel_favorite" onclick="AddToWz(cb_entryId);return false;" href="javascript:void(0);">收藏该文</a>
    <a id="green_channel_weibo" href="javascript:void(0);" title="分享至新浪微博" onclick="ShareToTsina()"><img src="https://common.cnblogs.com/images/icon_weibo_24.png" alt=""></a>
    <a id="green_channel_wechat" href="javascript:void(0);" title="分享至微信" onclick="shareOnWechat()"><img src="https://common.cnblogs.com/images/wechat.png" alt=""></a>
</div>
<div id="author_profile">
    <div id="author_profile_info" class="author_profile_info">
            <a href="https://home.cnblogs.com/u/zhaoxinshanwei/" target="_blank"><img src="https://pic.cnblogs.com/face/578896/20131102183538.png" class="author_avatar" alt=""></a>
        <div id="author_profile_detail" class="author_profile_info">
            <a href="https://home.cnblogs.com/u/zhaoxinshanwei/">冰花ぃ雪魄</a><br>
            <a href="https://home.cnblogs.com/u/zhaoxinshanwei/followees/">关注 - 83</a><br>
            <a href="https://home.cnblogs.com/u/zhaoxinshanwei/followers/">粉丝 - 30</a>
        </div>
    </div>
    <div class="clear"></div>
    <div id="author_profile_honor"></div>
    <div id="author_profile_follow">
                <a href="javascript:void(0);" onclick="follow('4db17c66-aa43-e311-8d02-90b11c0b17d6');return false;">+加关注</a>
    </div>
</div>
<div id="div_digg">
    <div class="diggit" onclick="votePost(9139827,'Digg')">
        <span class="diggnum" id="digg_count">0</span>
    </div>
    <div class="buryit" onclick="votePost(9139827,'Bury')">
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

    <a href="https://www.cnblogs.com/zhaoxinshanwei/p/9139134.html" class="p_n_p_prefix">« </a> 上一篇：    <a href="https://www.cnblogs.com/zhaoxinshanwei/p/9139134.html" title="发布于 2018-06-05 12:22">restfull和传统http的区别</a>
    <br>
    <a href="https://www.cnblogs.com/zhaoxinshanwei/p/9150007.html" class="p_n_p_prefix">» </a> 下一篇：    <a href="https://www.cnblogs.com/zhaoxinshanwei/p/9150007.html" title="发布于 2018-06-07 12:31">Spring AOP 中pointcut expression表达式</a>

</div>
</div>
		</div>
		<div class="itemdesc">
			posted on 
<span id="post-date">2018-06-05 14:59</span>&nbsp;
<a href="https://www.cnblogs.com/zhaoxinshanwei/">冰花ぃ雪魄</a>&nbsp;
阅读(<span id="post_view_count">3709</span>)&nbsp;
评论(<span id="post_comment_count">0</span>)&nbsp;
<a href="https://i.cnblogs.com/EditPosts.aspx?postid=9139827" rel="nofollow">编辑</a>&nbsp;
<a href="javascript:void(0)" onclick="AddToWz(9139827);return false;">收藏</a>
		</div>
	</div>
	<div class="seperator">&nbsp;</div>
	
	
<script src="https://common.cnblogs.com/highlight/9.12.0/highlight.min.js"></script>
<script>markdown_highlight();</script>
<script>
    var allowComments = true, cb_blogId = 168281, cb_blogApp = 'zhaoxinshanwei', cb_blogUserGuid = '4db17c66-aa43-e311-8d02-90b11c0b17d6';
    var cb_entryId = 9139827, cb_entryCreatedDate = '2018-06-05 14:59', cb_postType = 1; 
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
    <div id="ad_t2"><a href="http://www.ucancode.com/index.htm" target="_blank" onclick="ga('send', 'event', 'Link', 'click', 'T2-ucancode')">【推荐】超50万行VC++源码: 大型组态工控、电力仿真CAD与GIS源码库</a><br><a href="https://cloud.tencent.com/act/season?fromSource=gwzcw.3422970.3422970.3422970&amp;utm_medium=cpc&amp;utm_id=gwzcw.3422970.3422970.3422970" target="_blank" onclick="ga('send', 'event', 'Link', 'click', 'T2-腾讯云')">【活动】腾讯云服务器推出云产品采购季 1核2G首年仅需99元</a><br><a href="https://developer.aliyun.com/ask/257760?utm_content=g_1000088949" target="_blank" onclick="ga('send', 'event', 'Link', 'click', 'T2-阿里云开发者社区')">【推荐】精品问答：大数据计算技术 1000 问</a><br><a href="https://developer.aliyun.com/article/721809?utm_content=g_1000088933" target="_blank" onclick="ga('send', 'event', 'Link', 'click', 'T2-阿里云开发者社区')">【推荐】技术人必备的17组成长笔记+1500道面试题</a><br></div>
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
        <div id="div-gpt-ad-1546353474406-0" style="height:250px; width:300px;" data-google-query-id="CKb13ZTM4ecCFQOMvQod-uoLRg"><div id="google_ads_iframe_/1090369/C1_0__container__" style="border: 0pt none;"><iframe id="google_ads_iframe_/1090369/C1_0" title="3rd party ad content" name="google_ads_iframe_/1090369/C1_0" width="300" height="250" scrolling="no" marginwidth="0" marginheight="0" frameborder="0" srcdoc="" style="border: 0px; vertical-align: bottom;" data-google-container-id="1" data-load-complete="true"></iframe></div></div>
    </div>
    <div id="under_post_news"><div class="recomm-block"><b>相关博文：</b><br>·  <a title="Greys学习笔记(未完待续)" href="https://www.cnblogs.com/oldtrafford/p/6773579.html" target="_blank" onclick="clickRecomItmem(6773579)">Greys学习笔记(未完待续)</a><br>·  <a title="greys java在线诊断工具" href="https://www.cnblogs.com/netsa/p/7490914.html" target="_blank" onclick="clickRecomItmem(7490914)">greys java在线诊断工具</a><br>·  <a title="Greys Java在线问题诊断工具" href="https://www.cnblogs.com/sidesky/p/6757563.html" target="_blank" onclick="clickRecomItmem(6757563)">Greys Java在线问题诊断工具</a><br>·  <a title="GreysJava在线问题诊断工具" href="https://www.cnblogs.com/aoyihuashao/p/8795902.html" target="_blank" onclick="clickRecomItmem(8795902)">GreysJava在线问题诊断工具</a><br>·  <a title="java诊断工具——Arthas" href="https://www.cnblogs.com/blogabc/p/10030464.html" target="_blank" onclick="clickRecomItmem(10030464)">java诊断工具——Arthas</a><br>»  <a target="_blank" href="https://recomm.cnblogs.com/blogpost/9139827">更多推荐...</a><div id="cnblogs_t5"><a href="https://developer.aliyun.com/article/726591?utm_content=g_1000088942" target="_blank" onclick="ga('send', 'event', 'Link', 'click', 'T5-阿里云开发者社区')">96秒100亿！哪些“黑科技”支撑全球最大流量洪峰？</a></div></div></div>
    <div id="cnblogs_c2" class="c_ad_block">
        <div id="div-gpt-ad-1539008685004-0" style="height:60px; width:468px;" data-google-query-id="CKf13ZTM4ecCFQOMvQod-uoLRg">
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
 ·              <a href="//news.cnblogs.com/n/656083/" target="_blank">黑客在思杰内部潜伏了五个月之久！</a>
            <br>
 ·              <a href="//news.cnblogs.com/n/656093/" target="_blank">瑞典试验全球第一种央行数字货币“e克朗”</a>
            <br>
 ·              <a href="//news.cnblogs.com/n/656090/" target="_blank">微软高管：为什么我们的基础岗位不要求大学学历</a>
            <br>
 ·              <a href="//news.cnblogs.com/n/656089/" target="_blank">特斯拉又有高层离职：HR和工厂建设一把手走人</a>
            <br>
 ·              <a href="//news.cnblogs.com/n/656092/" target="_blank">惠普董事会宣布“毒丸计划” 欲阻止施乐340亿美元收购公司</a>
            <br>
    » <a href="https://news.cnblogs.com/" title="IT 新闻" target="_blank">更多新闻...</a>
</div></div>
    <div id="HistoryToday" class="c_ad_block">
<b>历史上的今天：</b>
<br>

2018-06-05 <a href="https://www.cnblogs.com/zhaoxinshanwei/p/9139134.html">restfull 和传统 http 的区别</a>
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
</div></div>
</div>
