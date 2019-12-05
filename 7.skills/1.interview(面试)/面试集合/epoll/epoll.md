<div id="main">

<div id="post_detail">
<div class="post">
<div class="posthead">
<h2>
<a id="cb_post_title_url" class="singleposttitle" href="https://www.cnblogs.com/my_life/articles/3968782.html">epoll 水平触发 边沿触发</a>
</h2>
Posted on <span id="post-date">2014-09-12 17:17</span> <a href="https://www.cnblogs.com/my_life/">bw_0927</a> 阅读(<span id="post_view_count">8760</span>) 评论(<span id="post_comment_count">0</span>) &nbsp;<a href="https://i.cnblogs.com/EditArticles.aspx?postid=3968782" rel="nofollow">编辑</a> <a href="#" onclick="AddToWz(3968782);return false;">收藏</a>
<script type="text/javascript">var allowComments=true,cb_blogId=80083,cb_entryId=3968782,cb_blogApp=currentBlogApp,cb_blogUserGuid='711c089b-17ca-df11-ac81-842b2b196315',cb_entryCreatedDate='2014/9/12 17:17:00';loadViewCount(cb_entryId);var cb_postType=2;var isMarkdown=false;</script>

</div>
<div class="postbody"><div id="cnblogs_post_body" class="blogpost-body"><h1 class="article-title"><span style="font-size: 14px; line-height: 1.5;">http://www.cppfans.org/1417.html</span></h1>
<p><span style="font-size: 14px; line-height: 1.5;">http://blog.lucode.net/linux/epoll-tutorial.html</span></p>
<p>&nbsp;</p>
<p>现如今，网络通讯中用<span class="wp_keywordlink_affiliate"><a title="" href="http://www.cppfans.org/tag/epoll" target="_blank" data-original-title="View all posts in epoll">epoll</a>(linux)和IOCP(windows)几乎是大家津津乐道的东西，不为别的，就因为高效，所以大家喜欢用。IOCP的基础东西已经讲过了，可翻阅<a title="" href="http://www.cppfans.org/1054.html" target="_blank" data-original-title="">《IOCP浅析》</a>&nbsp;<a title="" href="http://www.cppfans.org/1089.html" target="_blank" data-original-title="">《IOCP浅析[二]——IOCP出现的意义和函数接口》</a>.</span></p>
<h5>什么是<span class="wp_keywordlink_affiliate"><a title="" href="http://www.cppfans.org/tag/epoll" target="_blank" data-original-title="View all posts in epoll">epoll</a>？</span></h5>
<p>epoll是<span class="wp_keywordlink_affiliate"><a title="" href="http://www.cppfans.org/tag/linux" target="_blank" data-original-title="View all posts in Linux">Linux</a>下多路复用IO接口select/poll的增强版本，它能显著提高程序在<span style="color: #ff0000;">大量并发连接中<span style="text-decoration: underline;">只有少量活跃</span>的情况下</span>的系统CPU利用率，因为它会复用文件描述符集 合来传递结果而不用迫使开发者每次等待事件之前都必须重新准备要被侦听的文件描述符集合，另一点原因就是获取事件的时候，它<span style="color: #ff0000;">无须遍历整个</span>被侦听的描述符 集，<span style="color: #ff0000;">只要遍历那些被内核IO事件异步唤醒而加入Ready队列的描述符集合就行了</span>。epoll除了提供<span style="color: #ff0000;">select/poll那种IO事件的电平触发 （Level Triggered）外，还提供了边沿触发（Edge Triggered），</span>这就使得用户空间程序有可能缓存IO状态，减少epoll_wait/epoll_pwait的调用，提高应用程序效率。<span class="wp_keywordlink_affiliate"><a title="" href="http://www.cppfans.org/tag/linux" target="_blank" data-original-title="View all posts in Linux">Linux</a>2.6内核中对/dev/epoll设备的访问的封装（system epoll）。</span></span></p>
<p>这个使我们开发网络应用程序更加简单，并且更加高效。</p>
<h5>为什么要使用epoll？</h5>
<p>同样，我们在linux系统下，影响效率的依然是I/O操作，linux提供给我们select/poll/epoll等多路复用I/O方式<em>(kqueue暂时没研究过)</em>，为什么我们对epoll情有独钟呢？原因如下：</p>
<p>1.文件描述符数量的对比。</p>
<p>epoll并没有fd(文件描述符)的上限，它只跟系统内存有关，我的2G的ubuntu下查看是20480个，轻松支持20W个fd。可使用如下命令查看：</p>
<pre>cat /proc/sys/fs/file-max</pre>
<p>再来看select/poll，有一个限定的fd的数量，linux/posix_types.h头文件中</p>
<pre>#define __FD_SETSIZE&nbsp;&nbsp;&nbsp; 1024</pre>
<p>2.效率对比。</p>
<p>当然了，你可以修改上述值，然后重新编译内核，然后再次写代码，这也是没问题的，不过我先说说select/poll的机制，估计你马上会作废上面修改枚举值的想法。</p>
<p>select/poll会因为监听fd的数量而导致效率低下，因为它是<span style="color: #ff0000;">轮询</span>所有fd，有数据就处理，没数据就跳过，所以fd的数量会降低效率；<span style="color: #ff0000;">而epoll只处理</span>就绪的fd，它<span style="color: #ff0000;">有一个就绪设备的队列</span>，每次只轮询该队列的数据，然后进行处理。<em>(先简单讲一下，第二篇还会详细讲解)</em></p>
<p>3.内存处理方式对比。</p>
<p>不管是哪种I/O机制，都无法避免fd在操作过程中拷贝的问题，而<span style="color: #ff0000;">epoll使用了mmap</span>(是指文件/对象的内存映射，被映射到多个内存页上)，所以同一块内存就可以避免这个问题。</p>
<p>btw:TCP/IP协议栈使用内存池管理sk_buff结构，你还可以通过修改内存池pool的大小，毕竟linux支持各种微调内核。</p>
<h5>epoll的工作方式</h5>
<p>epoll分为两种工作方式LT和ET。</p>
<p>LT(level triggered) 是默认/缺省的工作方式，<span style="color: #ff0000;">同时支持</span> block和no_block socket。这种工作方式下，内核会通知你一个fd是否就绪，然后才可以对这个就绪的fd进行I/O操作。就算你没有任何操作，系统<span style="color: #ff0000;">还是会继续提示</span>fd已经就绪，不过这种工作方式出错会比较小，传统的select/poll就是这种工作方式的代表。</p>
<p>ET(edge-triggered) 是高速工作方式，<span style="color: #ff0000;">仅支持</span>no_block socket，这种工作方式下，当fd从未就绪变为就绪时，内核会通知fd已经就绪，并且内核认为你知道该fd已经就绪，<span style="color: #ff0000;">不会再次通知</span>了，<span style="color: #ff0000;">除非</span>因为某些操作导致fd就绪状态发生变化。如果一直不对这个fd进行I/O操作，导致fd变为未就绪时，内核同样不会发送更多的通知，因为only once。所以这种方式下，<span style="color: #ff0000;">出错率比较高</span>，需要增加一些检测程序。</p>
<p><strong><span style="color: #ff0000;">LT可以理解为水平触发，只要有数据可以读，不管怎样都会通知。而ET为边缘触发，只有状态发生变化时才会通知，可以理解为电平变化</span></strong>。</p>
<h5>如何使用epoll？</h5>
<p>使用epoll很简单，只需要</p>
<pre>#include &lt;sys/epoll.h&gt;</pre>
<p>有三个关键函数：</p>
<p>int epoll_create(int size);</p>
<p>int epoll_ctl(int epfd, int op, int fd, struct epoll_events* event);</p>
<p>int epoll_wait(int epfd, struct epoll_event* events, int maxevents, int timeout);</p>
<p>当然了，不要忘记关闭函数.</p>
<p>&nbsp;</p>
<p>============分割线==============</p>
<p>这篇就讲到这里了，下面两篇主要是函数介绍，效率分析，例子。</p>
<p>转载请注明：<a title="" href="http://www.cppfans.org/" data-original-title="">C++爱好者博客</a>&nbsp;»&nbsp;<a title="" href="http://www.cppfans.org/1417.html" data-original-title="">浅析epoll-为何多路复用I/O要使用epoll</a></p>
<p>前一篇大致讲了一下<span class="wp_keywordlink_affiliate"><a title="" href="http://www.cppfans.org/tag/epoll" target="_blank" data-original-title="View all posts in epoll">epoll</a>是个什么东西，优点等内容，这篇延续上一篇的内容，主要是分析<span class="wp_keywordlink_affiliate"><a title="" href="http://www.cppfans.org/tag/epoll" target="_blank" data-original-title="View all posts in epoll">epoll</a>的函数，epoll<span class="wp_keywordlink_affiliate"><a title="" href="http://www.cppfans.org/tag/%e9%ab%98%e6%80%a7%e8%83%bd" target="_blank" data-original-title="View all posts in 高性能">高性能</a>的深入分析。</span></span></span></p>
<h5>epoll的三大函数</h5>
<p>1.创建epoll fd函数</p>
<pre>int epoll_create(int size);</pre>
<p>epoll_create()创建一个epoll的事例，通知内核需要监听size个fd。size指的并不是最大的后备存储设备，而是衡量内核内部结构大小的一个提示。当创建成功后，会占用一个fd，所以记得在使用完之后调用close()，否则fd可能会被耗尽。</p>
<p>Note:自从<span class="wp_keywordlink_affiliate"><a title="" href="http://www.cppfans.org/tag/linux" target="_blank" data-original-title="View all posts in Linux">Linux</a>2.6.8版本以后，size值其实是没什么用的，不过要大于0，因为内核可以动态的分配大小，所以不需要size这个提示了。</span></p>
<p>创建还有另外一个函数</p>
<pre>int epoll_create1(int flag);</pre>
<p>这个函数是在linux 2.6.27中加入的，当你在看陈硕的muduo时可以看到这个函数，其实它和epoll_create差不多，不同的是epoll_create1函数的参数是flag，当flag是0时，表示和epoll_create函数完全一样，不需要size的提示了。</p>
<p>当flag = EPOLL_CLOEXEC，创建的epfd会设置FD_CLOEXEC</p>
<p>当flag = EPOLL_NONBLOCK，创建的epfd会设置为非阻塞</p>
<p>一般用法都是使用EPOLL_CLOEXEC.</p>
<p>Note:关于FD_CLOEXEC，现在网上好多都说的有点问题，我翻阅了一些资料，请教了一些人，大约明白它的意思了。</p>
<p>它是fd的一个标识说明，用来设置文件close-on-exec状态的。当close-on-exec状态为0时，调用exec时，fd<strong>不会</strong>被关闭；状态非零时则<strong>会</strong>被关闭，这样做可以防止fd泄露给执行exec后的进程。关于exec的用法，大家可以去自己查阅下，或者直接man exec。</p>
<p>2.epoll事件的注册函数</p>
<pre>int epoll_ctl(int epfd, int op, int fd, struct epoll_event* event);</pre>
<p><span style="color: #ff0000;">select是在监听时告诉内核要监听的事件，而epoll_ctl是先注册需要监听的事件。</span></p>
<p>第一个参数epfd，为epoll_create返回的的epoll fd。</p>
<p>第二个参数op表示操作值。有三个操作类型，</p>
<pre>EPOLL_CTL_ADD&nbsp; // 注册目标fd到epfd中，同时关联内部event到fd上

EPOLL_CTL_MOD // 修改已经注册到fd的监听事件

EPOLL_CTL_DEL // 从epfd中删除/移除已注册的fd，event可以被忽略，也可以为NULL</pre>
<p>第三个参数fd表示需要监听的fd。</p>
<p>第四个参数event表示需要监听的事件。</p>
<pre>typedef union epoll_data {
void&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; *ptr;
int&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; fd;
uint32_t&nbsp;&nbsp;&nbsp;&nbsp; u32;
uint64_t&nbsp;&nbsp;&nbsp;&nbsp; u64;
} epoll_data_t;

struct epoll_event {
uint32_t&nbsp;&nbsp;&nbsp;&nbsp; events;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; /* Epoll events */
epoll_data_t data;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; /* User data variable */
};</pre>
<p>event参数是一个枚举的集合，可以用” | “来增加事件类型，枚举如下：</p>
<p>EPOLLIN:表示关联的fd可以进行读操作了。<br>EPOLLOUT:表示关联的fd可以进行写操作了。<br>EPOLLRDHUP(since&nbsp;<span class="wp_keywordlink_affiliate"><a title="" href="http://www.cppfans.org/tag/linux" target="_blank" data-original-title="View all posts in Linux">Linux</a>&nbsp;2.6.17):表示套接字关闭了连接，或者关闭了正写一半的连接。<br>EPOLLPRI:表示关联的fd有紧急优先事件可以进行读操作了。<br>EPOLLERR:表示关联的fd发生了错误，epoll_wait会一直等待这个事件，所以一般没必要设置这个属性。<br>EPOLLHUP:表示关联的fd挂起了，epoll_wait会一直等待这个事件，所以一般没必要设置这个属性。<br>EPOLLET:设置关联的fd为ET的工作方式，epoll的默认工作方式是LT。<br>EPOLLONESHOT (since Linux 2.6.2):设置关联的fd为one-shot的工作方式。表示只监听一次事件，如果要再次监听，需要把socket放入到epoll队列中。</span></p>
<p>3.epoll等待事件函数</p>
<p>int epoll_wait(int epfd, struct epoll_event *events, int maxevents, int timeout);<br>int epoll_pwait(int epfd, struct epoll_event *events, int maxevents, int timeout,&nbsp; const sigset_t *sigmask);</p>
<p>上面两个函数的参数含义：</p>
<p>第一个参数:表示epoll_wait等待epfd上的事件</p>
<p>第二个参数:events指针携带有epoll_data_t数据</p>
<p>第三个参数:maxevents告诉内核events有多大，该值必须大于0</p>
<p>第四个参数:timeout表示超时时间(单位：毫秒)</p>
<p>epoll_pwait(since linux 2.6.19)允许一个应用程序安全的等待，直到fd设备准备就绪，或者捕获到一个信号量。其中sigmask表示要捕获的信号量。</p>
<p>函数如果等待成功，则返回fd的数字；0表示等待fd超时，其他错误号请查看errno</p>
<p>函数到这里就讲完了，下一篇会写一个例子给大家看下这些函数是如何使用的。</p>
<p>&nbsp;</p>
<p>============</p>
<p>epoll支持水平触发和边缘触发，理论上来说边缘触发<span style="color: #ff0000;">性能更高，但是使用更加复杂，因为任何意外的丢失事件都会造成请求处理错误</span>。Nginx就使用了epoll的边缘触发模型。</p>
<p>这里提一下水平触发和边缘触发就绪通知的区别，这两个词来源于计算机硬件设计。它们的区别是只要句柄满足某种状态，水平触发就会发出通知；而只有当句柄状态改变时，边缘触发才会发出通知。例如一个socket经过长时间等待后接收到一段100k的数据，两种触发方式都会向程序发出就绪通知。假设程序从这个socket中读取了50k数据，并再次调用监听函数，水平触发依然会发出就绪通知，而边缘触发会因为socket“有数据可读”这个状态没有发生变化而不发出通知且陷入长时间的等待。</p>
<p>因此在使用边缘触发的 api 时，<span style="color: #ff0000;">要注意每次都要读到 socket返回 EWOULDBLOCK为止</span>。 <span style="color: #ff0000;">否则netstat 的recv-q会持续增加</span></p>
<p>===============</p>
<p>通常来说，et方式是比较危险的方式，如果要使用et方式，那么，应用程序应该 1、将socket设置为non-blocking方式 2、epoll_wait收到event后，read或write需要读到没有数据为止，write需要写到没有数据为止（对于non-blocking socket来说，EAGAIN通常是无数据可读，无数据可写的返回状态）；</p>
<p>我们最近遇到一个问题，就是由于在使用epoll的过程中，缓冲区的数据没有读完，造成后续的通信失败。</p>
<p>表现现象就是，使用netstat -an观察时，这个socket的recv-q值不为0.</p></div><div id="MySignature"></div>
<div class="clear"></div>
<div id="blog_post_info_block">
<div id="BlogPostCategory"></div>
<div id="EntryTag"></div>
<div id="blog_post_info"><div id="green_channel">
<a href="javascript:void(0);" id="green_channel_digg" onclick="DiggIt(3968782,cb_blogId,1);green_channel_success(this,'谢谢推荐！');">好文要顶</a>
<a id="green_channel_follow" onclick="follow('711c089b-17ca-df11-ac81-842b2b196315');" href="javascript:void(0);">关注我</a>
<a id="green_channel_favorite" onclick="AddToWz(cb_entryId);return false;" href="javascript:void(0);">收藏该文</a>
<a id="green_channel_weibo" href="javascript:void(0);" title="分享至新浪微博" onclick="ShareToTsina()"><img src="//common.cnblogs.com/images/icon_weibo_24.png" alt=""></a>
<a id="green_channel_wechat" href="javascript:void(0);" title="分享至微信" onclick="shareOnWechat()"><img src="//common.cnblogs.com/images/wechat.png" alt=""></a>
</div>
<div id="author_profile">
<div id="author_profile_info" class="author_profile_info">
<a href="https://home.cnblogs.com/u/my_life/" target="_blank"><img src="//pic.cnblogs.com/face/sample_face.gif" class="author_avatar" alt=""></a>
<div id="author_profile_detail" class="author_profile_info">
<a href="https://home.cnblogs.com/u/my_life/">bw_0927</a><br>
<a href="https://home.cnblogs.com/u/my_life/followees">关注 - 2</a><br>
<a href="https://home.cnblogs.com/u/my_life/followers">粉丝 - 96</a>
</div>
</div>
<div class="clear"></div>
<div id="author_profile_honor"></div>
<div id="author_profile_follow">
<a href="javascript:void(0);" onclick="follow('711c089b-17ca-df11-ac81-842b2b196315');return false;">+加关注</a>
</div>
</div>
<div id="div_digg">
<div class="diggit" onclick="votePost(3968782,'Digg')">
<span class="diggnum" id="digg_count">2</span>
</div>
<div class="buryit" onclick="votePost(3968782,'Bury')">
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
<div id="post_next_prev"><a href="https://www.cnblogs.com/my_life/articles/3968386.html" class="p_n_p_prefix">« </a> 上一篇：<a href="https://www.cnblogs.com/my_life/articles/3968386.html" title="发布于2014-09-12 14:51">3&gt;I/O事件处理模型之Reactor和Proactor</a><br><a href="https://www.cnblogs.com/my_life/articles/3972604.html" class="p_n_p_prefix">» </a> 下一篇：<a href="https://www.cnblogs.com/my_life/articles/3972604.html" title="发布于2014-09-15 12:49">virtualbox／vmware 文件夹共享 fstab mount</a><br></div>
</div>

</div>
</div></div><a name="!comments"></a><div id="blog-comments-placeholder"></div><script type="text/javascript">var commentManager = new blogCommentManager();commentManager.renderComments(0);</script>
<div id="comment_form" class="commentform">
<a name="commentform"></a>
<div id="divCommentShow"></div>
<div id="comment_nav"><span id="span_refresh_tips"></span><a href="javascript:void(0);" onclick="return RefreshCommentList();" id="lnk_RefreshComments" runat="server" clientidmode="Static">刷新评论</a><a href="#" onclick="return RefreshPage();">刷新页面</a><a href="#top">返回顶部</a></div>
<div id="comment_form_container"><div class="login_tips">注册用户登录后才能发表评论，请 <a rel="nofollow" href="javascript:void(0);" class="underline" onclick="return login('commentform');">登录</a> 或 <a rel="nofollow" href="javascript:void(0);" class="underline" onclick="return register();">注册</a>，<a href="http://www.cnblogs.com">访问</a>网站首页。</div></div>
<div class="ad_text_commentbox" id="ad_text_under_commentbox"></div>
<div id="ad_t2"><a href="http://www.ucancode.com/index.htm" target="_blank" onclick="ga('send', 'event', 'Link', 'click', 'T2-工控')">【推荐】超50万C++/C#源码: 大型实时仿真组态图形源码</a><br><a href="https://www.grapecity.com.cn/developer/spreadjs?utm_source=cnblogs&amp;utm_medium=blogpage&amp;utm_term=bottom&amp;utm_content=SpreadJS&amp;utm_campaign=community" target="_blank" onclick="ga('send', 'event', 'Link', 'click', 'T2-SpreadJS')">【前端】SpreadJS表格控件，可嵌入系统开发的在线Excel</a><br><a href="https://q.cnblogs.com/" target="_blank" onclick="ga('send', 'event', 'Link', 'click', 'T2-博问')">【推荐】程序员问答平台，解决您开发中遇到的技术难题</a><br></div>
<div id="opt_under_post"></div>
<script async="async" src="https://www.googletagservices.com/tag/js/gpt.js"></script>
<script>
var googletag = googletag || {};
googletag.cmd = googletag.cmd || [];
</script>
<script>
googletag.cmd.push(function() {
googletag.defineSlot('/1090369/C1', [300, 250], 'div-gpt-ad-1546353474406-0').addService(googletag.pubads());
googletag.defineSlot('/1090369/C2', [468, 60], 'div-gpt-ad-1539008685004-0').addService(googletag.pubads());
googletag.pubads().enableSingleRequest();
googletag.enableServices();
});
</script>
<div id="cnblogs_c1" class="c_ad_block">
<div id="div-gpt-ad-1546353474406-0" style="height:250px; width:300px;" data-google-query-id="CMLwxLu43-MCFQsZKgodF7cJ1g"><div id="google_ads_iframe_/1090369/C1_0__container__" style="border: 0pt none;"><iframe id="google_ads_iframe_/1090369/C1_0" title="3rd party ad content" name="google_ads_iframe_/1090369/C1_0" width="300" height="250" scrolling="no" marginwidth="0" marginheight="0" frameborder="0" srcdoc="" style="border: 0px; vertical-align: bottom;" data-google-container-id="1" data-load-complete="true"></iframe></div></div>
</div>
<div id="under_post_news"><div class="recomm-block"><b>相关博文：</b><br>·  <a href="https://www.cnblogs.com/meihao1203/p/8655397.html" target="_blank" onclick="clickRecomItmem(8655397)">epoll模型边沿触发</a><br>·  <a href="https://www.cnblogs.com/libing029/p/10460945.html" target="_blank" onclick="clickRecomItmem(10460945)">linuxepollET边沿触发</a><br>·  <a href="https://www.cnblogs.com/lemontea-t/p/4919091.html" target="_blank" onclick="clickRecomItmem(4919091)">epoll  ET(边缘触发) LT（水平触发）</a><br>·  <a href="https://www.cnblogs.com/zhongxinWang/p/3696411.html" target="_blank" onclick="clickRecomItmem(3696411)">边沿触发</a><br>·  <a href="https://www.cnblogs.com/guxuanqing/p/10570625.html" target="_blank" onclick="clickRecomItmem(10570625)">epollET(边缘触发)LT（水平触发）</a><br></div></div>
<div id="cnblogs_c2" class="c_ad_block">
<div id="div-gpt-ad-1539008685004-0" style="height:60px; width:468px;" data-google-query-id="CMPwxLu43-MCFQsZKgodF7cJ1g"><div id="google_ads_iframe_/1090369/C2_0__container__" style="border: 0pt none;"><iframe id="google_ads_iframe_/1090369/C2_0" title="3rd party ad content" name="google_ads_iframe_/1090369/C2_0" width="468" height="60" scrolling="no" marginwidth="0" marginheight="0" frameborder="0" srcdoc="" style="border: 0px; vertical-align: bottom;" data-google-container-id="2" data-load-complete="true"></iframe></div></div>
</div>
<div id="under_post_kb"><div class="itnews c_ad_block"><b>最新新闻</b>：<br> ·  <a href="https://news.cnblogs.com/n/629032/" target="_blank">Siri 也被曝出「窃听」用户，三大智能语音助手全军覆没</a><br> ·  <a href="https://news.cnblogs.com/n/629031/" target="_blank">2019Q2全球智能手机销量出炉：华为逆势增长成全球第二！</a><br> ·  <a href="https://news.cnblogs.com/n/629030/" target="_blank">机械硬盘避坑大法：一文搞懂PMR和SMR有什么区别</a><br> ·  <a href="https://news.cnblogs.com/n/629028/" target="_blank">人类也能做到睁一只眼闭一只眼的单半球睡眠吗？</a><br> ·  <a href="https://news.cnblogs.com/n/629027/" target="_blank">中国半导体产能并没有过剩 晶圆代工国产率不足40%</a><br>» <a href="http://news.cnblogs.com/" title="IT新闻" target="_blank">更多新闻...</a></div></div>
<div id="HistoryToday" class="c_ad_block"></div>
<script type="text/javascript">
if(enablePostBottom()) {
codeHighlight();
fixPostBody();
setTimeout(function () { incrementViewCount(cb_entryId); }, 50);
deliverT2();
deliverC1();
deliverC2();    
loadNewsAndKb();
loadBlogSignature();
LoadPostInfoBlock(cb_blogId, cb_entryId, cb_blogApp, cb_blogUserGuid);
GetPrevNextPost(cb_entryId, cb_blogId, cb_entryCreatedDate, cb_postType);
loadOptUnderPost();
GetHistoryToday(cb_blogId, cb_blogApp, cb_entryCreatedDate);  
}
</script>
</div>


</div>