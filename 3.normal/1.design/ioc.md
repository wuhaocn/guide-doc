<div id="mainContent">
<div class="forFlow">
<div id="post_detail">
<!--done-->
<div id="topics">
<div class="post">
<h1 class="postTitle">

<a id="cb_post_title_url" class="postTitle2" href="https://www.cnblogs.com/superjt/p/4311577.html">Spring的IOC原理[通俗解释一下]</a>

</h1>
<div class="clear"></div>
<div class="postBody">

<div id="cnblogs_post_body" class="blogpost-body ">
<p><strong>1. IoC理论的背景</strong><br>我们都知道，在采用面向对象方法设计的软件系统中，它的底层实现都是由N个对象组成的，所有的对象通过彼此的合作，最终实现系统的业务逻辑。</p>
<p><img src="https://pic002.cnblogs.com/images/2011/230454/2011052709382686.jpg" alt="">图1：软件系统中耦合的对象</p>
<p>如果我们打开机械式手表的后盖，就会看到与上面类似的情形，各个齿轮分别带动时针、分针和秒针顺时针旋转，从而在表盘上产生正确的时间。图1中描述的就是这样的一个齿轮组，它拥有多个独立的齿轮，这些齿轮相互啮合在一起，协同工作，共同完成某项任务。我们可以看到，在这样的齿轮组中，如果有一个齿轮出了问题，就可能会影响到整个齿轮组的正常运转。<br>齿轮组中齿轮之间的啮合关系,与软件系统中对象之间的耦合关系非常相似。对象之间的耦合关系是无法避免的，也是必要的，这是协同工作的基础。现在，伴随着工业级应用的规模越来越庞大，对象之间的依赖关系也越来越复杂，经常会出现对象之间的多重依赖性关系，因此，架构师和设计师对于系统的分析和设计，将面临更大的挑战。对象之间耦合度过高的系统，必然会出现牵一发而动全身的情形。</p>
<p><img src="https://pic002.cnblogs.com/images/2011/230454/2011052709390013.jpg" alt="">图2：对象之间复杂的依赖关系</p>
<p>耦合关系不仅会出现在对象与对象之间，也会出现在软件系统的各模块之间，以及软件系统和硬件系统之间。如何降低系统之间、模块之间和对象之间的耦合度，是软件工程永远追求的目标之一。<strong>为了解决对象之间的耦合度过高的问题</strong>，软件专家Michael Mattson提出了IOC理论，用来实现对象之间的“解耦”，目前这个理论已经被成功地应用到实践当中，很多的J2EE项目均采用了IOC框架产品Spring。<span id="more-122"></span></p>
<p><strong>2. 什么是控制反转(IoC)</strong><br>IOC是Inversion of Control的缩写，多数书籍翻译成“控制反转”，还有些书籍翻译成为“控制反向”或者“控制倒置”。<br>1996年，Michael Mattson在一篇有关探讨面向对象框架的文章中，首先提出了IOC 这个概念。对于面向对象设计及编程的基本思想，前面我们已经讲了很多了，不再赘述，简单来说就是把复杂系统分解成相互合作的对象，这些对象类通过封装以后，内部实现对外部是透明的，从而降低了解决问题的复杂度，而且可以灵活地被重用和扩展。IOC理论提出的观点大体是这样的：借助于“第三方”实现具有依赖关系的对象之间的解耦，如下图：</p>
<p><img src="https://pic002.cnblogs.com/images/2011/230454/2011052709391014.jpg" alt=""></p>
<p align="center">图3：IOC解耦过程</p>
<p>大家看到了吧，由于引进了中间位置的“第三方”，也就是IOC容器，使得A、B、C、D这4个对象没有了耦合关系，齿轮之间的传动全部依靠“第三方”了，全部对象的控制权全部上缴给“第三方”IOC容器，所以，IOC容器成了整个系统的关键核心，它起到了一种类似“粘合剂”的作用，把系统中的所有对象粘合在一起发挥作用，如果没有这个“粘合剂”，对象与对象之间会彼此失去联系，这就是有人把IOC容器比喻成“粘合剂”的由来。<br>我们再来做个试验：把上图中间的IOC容器拿掉，然后再来看看这套系统：</p>
<p><img src="https://pic002.cnblogs.com/images/2011/230454/2011052709392670.jpg" alt=""></p>
<p>图4：拿掉IoC容器后的系统</p>
<p>我们现在看到的画面，就是我们要实现整个系统所需要完成的全部内容。这时候，A、B、C、D这4个对象之间已经没有了耦合关系，彼此毫无联系，这样的话，当你在实现A的时候，根本无须再去考虑B、C和D了，对象之间的依赖关系已经降低到了最低程度。所以，如果真能实现IOC容器，对于系统开发而言，这将是一件多么美好的事情，参与开发的每一成员只要实现自己的类就可以了，跟别人没有任何关系！<br>我们再来看看，控制反转(IOC)到底为什么要起这么个名字？我们来对比一下：<br>软件系统在没有引入IOC容器之前，如图1所示，对象A依赖于对象B，那么对象A在初始化或者运行到某一点的时候，自己必须主动去创建对象B或者使用已经创建的对象B。无论是创建还是使用对象B，控制权都在自己手上。<br>软件系统在引入IOC容器之后，这种情形就完全改变了，如图3所示，由于IOC容器的加入，对象A与对象B之间失去了直接联系，所以，当对象A运行到需要对象B的时候，IOC容器会主动创建一个对象B注入到对象A需要的地方。<br>通过前后的对比，我们不难看出来：对象A获得依赖对象B的过程,由主动行为变为了被动行为，控制权颠倒过来了，这就是“控制反转”这个名称的由来。</p>
<p><strong>3.&nbsp; IOC的别名：依赖注入(DI)</strong><br>2004年，Martin Fowler探讨了同一个问题，既然IOC是控制反转，那么到底是“哪些方面的控制被反转了呢？”，经过详细地分析和论证后，他得出了答案：“获得依赖对象的过程被反转了”。控制被反转之后，获得依赖对象的过程由自身管理变为了由IOC容器主动注入。于是，他给“控制反转”取了一个更合适的名字叫做“依赖注入（Dependency Injection）”。他的这个答案，实际上给出了实现IOC的方法：注入。所谓依赖注入，就是由IOC容器在运行期间，动态地将某种依赖关系注入到对象之中。</p>
<p>所以，依赖注入(DI)和控制反转(IOC)是从不同的角度的描述的同一件事情，就是指<strong>通过引入IOC容器，利用依赖关系注入的方式，实现对象之间的解耦</strong>。<br>我们举一个生活中的例子，来帮助理解依赖注入的过程。大家对USB接口和USB设备应该都很熟悉吧，USB为我们使用电脑提供了很大的方便，现在有很多的外部设备都支持USB接口。</p>
<p><img src="https://pic002.cnblogs.com/images/2011/230454/2011052709393897.jpg" alt=""></p>
<p>图5：USB接口和USB设备</p>
<p>现在，我们利用电脑主机和USB接口来实现一个任务：从外部USB设备读取一个文件。<br>电脑主机读取文件的时候，它一点也不会关心USB接口上连接的是什么外部设备，而且它确实也无须知道。它的任务就是读取USB接口，挂接的外部设备只要符合USB接口标准即可。所以，如果我给电脑主机连接上一个U盘，那么主机就从U盘上读取文件；如果我给电脑主机连接上一个外置硬盘，那么电脑主机就从外置硬盘上读取文件。挂接外部设备的权力由我作主，即控制权归我，至于USB接口挂接的是什么设备，电脑主机是决定不了，它只能被动的接受。电脑主机需要外部设备的时候，根本不用它告诉我，我就会主动帮它挂上它想要的外部设备，你看我的服务是多么的到位。这就是我们生活中常见的一个依赖注入的例子。在这个过程中，<strong>我就起到了IOC容器的作用</strong>。<br>通过这个例子,依赖注入的思路已经非常清楚：当电脑主机读取文件的时候，我就把它所要依赖的外部设备，帮他挂接上。整个外部设备注入的过程和一个被依赖的对象在系统运行时被注入另外一个对象内部的过程完全一样。<br>我们把依赖注入应用到软件系统中，再来描述一下这个过程：<br>对象A依赖于对象B,当对象 A需要用到对象B的时候，IOC容器就会立即创建一个对象B送给对象A。IOC容器就是一个对象制造工厂，你需要什么，它会给你送去，你直接使用就行了，而再也不用去关心你所用的东西是如何制成的，也不用关心最后是怎么被销毁的，这一切全部由IOC容器包办。<br>在传统的实现中，由程序内部代码来控制组件之间的关系。我们经常使用new关键字来实现两个组件之间关系的组合，这种实现方式会造成组件之间耦合。IOC很好地解决了该问题，它将实现组件间关系从程序内部提到外部容器，也就是说由容器在运行期将组件间的某种依赖关系动态注入组件中。</p>
<p><strong>4.&nbsp; IOC为我们带来了什么好处</strong></p>
<p>我们还是从USB的例子说起，使用USB外部设备比使用内置硬盘，到底带来什么好处？<br>第一、USB设备作为电脑主机的外部设备，在插入主机之前，与电脑主机没有任何的关系，只有被我们连接在一起之后，两者才发生联系，具有相关性。所以，无论两者中的任何一方出现什么的问题，都不会影响另一方的运行。这种特性体现在软件工程中，就是可维护性比较好，非常便于进行单元测试，便于调试程序和诊断故障。代码中的每一个Class都可以单独测试，彼此之间互不影响，只要保证自身的功能无误即可，这就是组件之间低耦合或者无耦合带来的好处。<br>第二、USB设备和电脑主机的之间无关性，还带来了另外一个好处，生产USB设备的厂商和生产电脑主机的厂商完全可以是互不相干的人，各干各事，他们之间唯一需要遵守的就是USB接口标准。这种特性体现在软件开发过程中，好处可是太大了。每个开发团队的成员都只需要关心实现自身的业务逻辑，完全不用去关心其它的人工作进展，因为你的任务跟别人没有任何关系，你的任务可以单独测试，你的任务也不用依赖于别人的组件，再也不用扯不清责任了。所以，在一个大中型项目中，团队成员分工明确、责任明晰，很容易将一个大的任务划分为细小的任务，开发效率和产品质量必将得到大幅度的提高。<br>第三、同一个USB外部设备可以插接到任何支持USB的设备，可以插接到电脑主机，也可以插接到DV机，USB外部设备可以被反复利用。在软件工程中，这种特性就是可复用性好，我们可以把具有普遍性的常用组件独立出来，反复利用到项目中的其它部分，或者是其它项目，当然这也是面向对象的基本特征。显然，IOC不仅更好地贯彻了这个原则，提高了模块的可复用性。符合接口标准的实现，都可以插接到支持此标准的模块中。<br>第四、同USB外部设备一样，模块具有热插拔特性。IOC生成对象的方式转为外置方式，也就是把对象生成放在配置文件里进行定义，这样，当我们更换一个实现子类将会变得很简单，只要修改配置文件就可以了，完全具有热插拨的特性。<br>以上几点好处，难道还不足以打动我们，让我们在项目开发过程中使用IOC框架吗？</p>
<p><strong>5.&nbsp; IOC容器的技术剖析</strong><br>IOC中最基本的技术就是“反射(Reflection)”编程，目前.Net C#、Java和PHP5等语言均支持，其中PHP5的技术书籍中，有时候也被翻译成“映射”。<strong>有关反射的概念和用法，大家应该都很清楚，通俗来讲就是根据给出的类名（字符串方式）来动态地生成对象</strong>。这种编程方式可以让对象在生成时才决定到底是哪一种对象。反射的应用是很广泛的，很多的成熟的框架，比如象Java中的Hibernate、Spring框架，.Net中 NHibernate、Spring.Net框架都是把“反射”做为最基本的技术手段。<br>反射技术其实很早就出现了，但一直被忽略，没有被进一步的利用。当时的反射编程方式相对于正常的对象生成方式要慢至少得10倍。现在的反射技术经过改良优化，已经非常成熟，反射方式生成对象和通常对象生成方式，速度已经相差不大了，大约为1-2倍的差距。<br><strong>我们可以把IOC容器的工作模式看做是工厂模式的升华，可以把IOC容器看作是一个工厂，这个工厂里要生产的对象都在配置文件中给出定义，然后利用编程语言的的反射编程，根据配置文件中给出的类名生成相应的对象。从实现来看，IOC是把以前在工厂方法里写死的对象生成代码，改变为由配置文件来定义，也就是把工厂和对象生成这两者独立分隔开来，目的就是提高灵活性和可维护性。</strong><br><strong>6.&nbsp; IOC容器的一些产品</strong><br>Sun ONE技术体系下的IOC容器有：轻量级的有Spring、Guice、Pico Container、Avalon、HiveMind；重量级的有EJB；不轻不重的有JBoss，Jdon等等。Spring框架作为Java开发中SSH(Struts、Spring、Hibernate)三剑客之一，大中小项目中都有使用，非常成熟，应用广泛，EJB在关键性的工业级项目中也被使用，比如某些电信业务。<br>.Net技术体系下的IOC容器有：Spring.Net、Castle等等。Spring.Net是从Java的Spring移植过来的IOC容器，Castle的IOC容器就是Windsor部分。它们均是轻量级的框架，比较成熟，其中Spring.Net已经被逐渐应用于各种项目中。<br><strong>7. 使用IOC框架应该注意什么</strong><br>使用IOC框架产品能够给我们的开发过程带来很大的好处，但是也要充分认识引入IOC框架的缺点，做到心中有数，杜绝滥用框架。<br>第一、软件系统中由于引入了第三方IOC容器，生成对象的步骤变得有些复杂，本来是两者之间的事情，又凭空多出一道手续，所以，我们在刚开始使用IOC框架的时候，会感觉系统变得不太直观。所以，引入了一个全新的框架，就会增加团队成员学习和认识的培训成本，并且在以后的运行维护中，还得让新加入者具备同样的知识体系。<br>第二、由于IOC容器生成对象是通过反射方式，在运行效率上有一定的损耗。如果你要追求运行效率的话，就必须对此进行权衡。<br>第三、具体到IOC框架产品(比如：Spring)来讲，需要进行大量的配制工作，比较繁琐，对于一些小的项目而言，客观上也可能加大一些工作成本。<br>第四、IOC框架产品本身的成熟度需要进行评估，如果引入一个不成熟的IOC框架产品，那么会影响到整个项目，所以这也是一个隐性的风险。<br>我们大体可以得出这样的结论：一些工作量不大的项目或者产品，不太适合使用IOC框架产品。另外，如果团队成员的知识能力欠缺，对于IOC框架产品缺乏深入的理解，也不要贸然引入。最后，特别强调运行效率的项目或者产品，也不太适合引入IOC框架产品，象WEB2.0网站就是这种情况。</p>
<p>参考资料：</p>
<p><a href="http://jiwenke.iteye.com/blog/493965">http://jiwenke.iteye.com/blog/493965</a></p>
</div>
<div id="MySignature"></div>
<div class="clear"></div>
<div id="blog_post_info_block"><div id="BlogPostCategory">
分类: 
<a href="https://www.cnblogs.com/superjt/category/412680.html" target="_blank">Spring</a></div>
<div id="EntryTag">
标签: 
<a href="https://www.cnblogs.com/superjt/tag/spring/">spring</a>,             <a href="https://www.cnblogs.com/superjt/tag/ioc/">ioc</a></div>

<div id="blog_post_info">
<div id="green_channel">
<a href="javascript:void(0);" id="green_channel_digg" onclick="DiggIt(4311577,cb_blogId,1);green_channel_success(this,'谢谢推荐！');">好文要顶</a>
<a id="green_channel_follow" onclick="follow('59f5dc50-5cb6-e011-8673-842b2b196315');" href="javascript:void(0);">关注我</a>
<a id="green_channel_favorite" onclick="AddToWz(cb_entryId);return false;" href="javascript:void(0);">收藏该文</a>
<a id="green_channel_weibo" href="javascript:void(0);" title="分享至新浪微博" onclick="ShareToTsina()"><img src="https://common.cnblogs.com/images/icon_weibo_24.png" alt=""></a>
<a id="green_channel_wechat" href="javascript:void(0);" title="分享至微信" onclick="shareOnWechat()"><img src="https://common.cnblogs.com/images/wechat.png" alt=""></a>
</div>
<div id="author_profile">
<div id="author_profile_info" class="author_profile_info">
<a href="https://home.cnblogs.com/u/superjt/" target="_blank"><img src="https://pic.cnblogs.com/face/318255/20131223155154.png" class="author_avatar" alt=""></a>
<div id="author_profile_detail" class="author_profile_info">
<a href="https://home.cnblogs.com/u/superjt/">牧涛</a><br>
<a href="https://home.cnblogs.com/u/superjt/followees/">关注 - 6</a><br>
<a href="https://home.cnblogs.com/u/superjt/followers/">粉丝 - 290</a>
</div>
</div>
<div class="clear"></div>
<div id="author_profile_honor"></div>
<div id="author_profile_follow">
<a href="javascript:void(0);" onclick="follow('59f5dc50-5cb6-e011-8673-842b2b196315');return false;">+加关注</a>
</div>
</div>
<div id="div_digg">
<div class="diggit" onclick="votePost(4311577,'Digg')">
<span class="diggnum" id="digg_count">16</span>
</div>
<div class="buryit" onclick="votePost(4311577,'Bury')">
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

<a href="https://www.cnblogs.com/superjt/p/4276500.html" class="p_n_p_prefix">« </a> 上一篇：    <a href="https://www.cnblogs.com/superjt/p/4276500.html" title="发布于 2015-02-06 10:18">Oracle权限授予</a>
<br>
<a href="https://www.cnblogs.com/superjt/p/4318875.html" class="p_n_p_prefix">» </a> 下一篇：    <a href="https://www.cnblogs.com/superjt/p/4318875.html" title="发布于 2015-03-06 17:41">Spring AOP 详解[转]</a>

</div>
</div>
</div>
<div class="postDesc">posted @ 
<span id="post-date">2015-03-03 17:38</span>&nbsp;
<a href="https://www.cnblogs.com/superjt/">牧涛</a>&nbsp;
阅读(<span id="post_view_count">66125</span>)&nbsp;
评论(<span id="post_comment_count">9</span>)&nbsp;
<a href="https://i.cnblogs.com/EditPosts.aspx?postid=4311577" rel="nofollow">编辑</a>&nbsp;
<a href="javascript:void(0)" onclick="AddToWz(4311577);return false;">收藏</a></div>
</div>


</div>
<script src="https://common.cnblogs.com/highlight/9.12.0/highlight.min.js"></script>
<script>markdown_highlight();</script>
<script>
var allowComments = true, cb_blogId = 93525, cb_blogApp = 'superjt', cb_blogUserGuid = '59f5dc50-5cb6-e011-8673-842b2b196315';
var cb_entryId = 4311577, cb_entryCreatedDate = '2015-03-03 17:38', cb_postType = 1; 
loadViewCount(cb_entryId);
</script><a name="!comments"></a>
<div id="blog-comments-placeholder">

<div id="comment_pager_top">

</div>

<!--done-->
<br>
<div class="feedback_area_title">
Post Comment</div>
<div class="feedbackNoItems">
<div class="feedbackNoItems"></div></div>
<div class="feedbackItem">
<div class="feedbackListSubtitle">
<div class="feedbackManage">


<span class="comment_actions">




</span>


</div>

<a href="#3734522" class="layer">#1楼</a>
<a name="3734522" id="comment_anchor_3734522"></a>


<span class="comment_date">2017-07-13 15:41</span>


|


<a id="a_comment_author_3734522" href="https://home.cnblogs.com/u/1183401/" target="_blank">Jason_Maron</a>

</div>
<div class="feedbackCon">

<div id="comment_body_3734522" class="blog_comment_body">
大神用一个很通常的例子就简单明了的解释清楚IOC的作用，牛
</div>
<div class="comment_vote">
<span class="comment_error" style="color: red"></span>
<a href="javascript:void(0);" class="comment_digg" onclick="return voteComment(3734522, 'Digg', this.parentElement, false);">
支持(0)
</a>
<a href="javascript:void(0);" class="comment_burry" onclick="return voteComment(3734522, 'Bury', this.parentElement, false);">
反对(0)
</a>
</div>

<br>
</div>
</div>
<div class="feedbackItem">
<div class="feedbackListSubtitle">
<div class="feedbackManage">


<span class="comment_actions">




</span>


</div>

<a href="#3750704" class="layer">#2楼</a>
<a name="3750704" id="comment_anchor_3750704"></a>


<span class="comment_date">2017-08-04 15:55</span>


|


<a id="a_comment_author_3750704" href="https://www.cnblogs.com/zd1009/" target="_blank">DamKeeper</a>

</div>
<div class="feedbackCon">

<div id="comment_body_3750704" class="blog_comment_body">
小白受教！
</div>
<div class="comment_vote">
<span class="comment_error" style="color: red"></span>
<a href="javascript:void(0);" class="comment_digg" onclick="return voteComment(3750704, 'Digg', this.parentElement, false);">
支持(0)
</a>
<a href="javascript:void(0);" class="comment_burry" onclick="return voteComment(3750704, 'Bury', this.parentElement, false);">
反对(0)
</a>
</div>
<span id="comment_3750704_avatar" style="display:none">
https://pic.cnblogs.com/face/953720/20161009160214.png
</span>
<br>
</div>
</div>
<div class="feedbackItem">
<div class="feedbackListSubtitle">
<div class="feedbackManage">


<span class="comment_actions">




</span>


</div>

<a href="#3759665" class="layer">#3楼</a>
<a name="3759665" id="comment_anchor_3759665"></a>


<span class="comment_date">2017-08-16 17:44</span>


|


<a id="a_comment_author_3759665" href="https://home.cnblogs.com/u/1220751/" target="_blank">一根指</a>

</div>
<div class="feedbackCon">

<div id="comment_body_3759665" class="blog_comment_body">
受教了！
</div>
<div class="comment_vote">
<span class="comment_error" style="color: red"></span>
<a href="javascript:void(0);" class="comment_digg" onclick="return voteComment(3759665, 'Digg', this.parentElement, false);">
支持(0)
</a>
<a href="javascript:void(0);" class="comment_burry" onclick="return voteComment(3759665, 'Bury', this.parentElement, false);">
反对(0)
</a>
</div>

<br>
</div>
</div>
<div class="feedbackItem">
<div class="feedbackListSubtitle">
<div class="feedbackManage">


<span class="comment_actions">




</span>


</div>

<a href="#3844514" class="layer">#4楼</a>
<a name="3844514" id="comment_anchor_3844514"></a>


<span class="comment_date">2017-11-19 11:59</span>


|


<a id="a_comment_author_3844514" href="https://www.cnblogs.com/aminqiao/" target="_blank">南有乔木**</a>

</div>
<div class="feedbackCon">

<div id="comment_body_3844514" class="blog_comment_body">
厉害,讲解的很清楚
</div>
<div class="comment_vote">
<span class="comment_error" style="color: red"></span>
<a href="javascript:void(0);" class="comment_digg" onclick="return voteComment(3844514, 'Digg', this.parentElement, false);">
支持(0)
</a>
<a href="javascript:void(0);" class="comment_burry" onclick="return voteComment(3844514, 'Bury', this.parentElement, false);">
反对(0)
</a>
</div>

<br>
</div>
</div>
<div class="feedbackItem">
<div class="feedbackListSubtitle">
<div class="feedbackManage">


<span class="comment_actions">




</span>


</div>

<a href="#3887205" class="layer">#5楼</a>
<a name="3887205" id="comment_anchor_3887205"></a>


<span class="comment_date">2018-01-11 20:28</span>


|


<a id="a_comment_author_3887205" href="https://home.cnblogs.com/u/1313582/" target="_blank">Princiman</a>

</div>
<div class="feedbackCon">

<div id="comment_body_3887205" class="blog_comment_body">
学习了，谢谢
</div>
<div class="comment_vote">
<span class="comment_error" style="color: red"></span>
<a href="javascript:void(0);" class="comment_digg" onclick="return voteComment(3887205, 'Digg', this.parentElement, false);">
支持(0)
</a>
<a href="javascript:void(0);" class="comment_burry" onclick="return voteComment(3887205, 'Bury', this.parentElement, false);">
反对(0)
</a>
</div>

<br>
</div>
</div>
<div class="feedbackItem">
<div class="feedbackListSubtitle">
<div class="feedbackManage">


<span class="comment_actions">




</span>


</div>

<a href="#4174287" class="layer">#6楼</a>
<a name="4174287" id="comment_anchor_4174287"></a>


<span class="comment_date">2019-01-30 15:16</span>


|


<a id="a_comment_author_4174287" href="https://www.cnblogs.com/ch-10/" target="_blank">Automation</a>

</div>
<div class="feedbackCon">

<div id="comment_body_4174287" class="blog_comment_body">
膜拜大神
</div>
<div class="comment_vote">
<span class="comment_error" style="color: red"></span>
<a href="javascript:void(0);" class="comment_digg" onclick="return voteComment(4174287, 'Digg', this.parentElement, false);">
支持(0)
</a>
<a href="javascript:void(0);" class="comment_burry" onclick="return voteComment(4174287, 'Bury', this.parentElement, false);">
反对(0)
</a>
</div>
<span id="comment_4174287_avatar" style="display:none">
https://pic.cnblogs.com/face/504947/20130315220832.png
</span>
<br>
</div>
</div>
<div class="feedbackItem">
<div class="feedbackListSubtitle">
<div class="feedbackManage">


<span class="comment_actions">




</span>


</div>

<a href="#4218642" class="layer">#7楼</a>
<a name="4218642" id="comment_anchor_4218642"></a>


<span class="comment_date">2019-03-31 14:59</span>


|


<a id="a_comment_author_4218642" href="https://www.cnblogs.com/YangPeng/" target="_blank">我还会怀念过去</a>

</div>
<div class="feedbackCon">

<div id="comment_body_4218642" class="blog_comment_body">
学习了，谢谢！
</div>
<div class="comment_vote">
<span class="comment_error" style="color: red"></span>
<a href="javascript:void(0);" class="comment_digg" onclick="return voteComment(4218642, 'Digg', this.parentElement, false);">
支持(0)
</a>
<a href="javascript:void(0);" class="comment_burry" onclick="return voteComment(4218642, 'Bury', this.parentElement, false);">
反对(0)
</a>
</div>
<span id="comment_4218642_avatar" style="display:none">
https://pic.cnblogs.com/face/610261/20190524221533.png
</span>
<br>
</div>
</div>
<div class="feedbackItem">
<div class="feedbackListSubtitle">
<div class="feedbackManage">


<span class="comment_actions">




</span>


</div>

<a href="#4231918" class="layer">#8楼</a>
<a name="4231918" id="comment_anchor_4231918"></a>


<span class="comment_date">2019-04-15 22:14</span>


|


<a id="a_comment_author_4231918" href="https://home.cnblogs.com/u/1615226/" target="_blank">IT_搬运工</a>

</div>
<div class="feedbackCon">

<div id="comment_body_4231918" class="blog_comment_body">
膜拜大神
</div>
<div class="comment_vote">
<span class="comment_error" style="color: red"></span>
<a href="javascript:void(0);" class="comment_digg" onclick="return voteComment(4231918, 'Digg', this.parentElement, false);">
支持(0)
</a>
<a href="javascript:void(0);" class="comment_burry" onclick="return voteComment(4231918, 'Bury', this.parentElement, false);">
反对(0)
</a>
</div>

<br>
</div>
</div>
<div class="feedbackItem">
<div class="feedbackListSubtitle">
<div class="feedbackManage">


<span class="comment_actions">




</span>


</div>

<a href="#4383208" class="layer">#9楼</a>
<a name="4383208" id="comment_anchor_4383208"></a>

<span id="comment-maxId" style="display:none">4383208</span>
<span id="comment-maxDate" style="display:none">2019/10/9 上午9:46:17</span>

<span class="comment_date">2019-10-09 09:46</span>


|


<a id="a_comment_author_4383208" href="https://home.cnblogs.com/u/1826739/" target="_blank">雪梨蛋花汤</a>

</div>
<div class="feedbackCon">

<div id="comment_body_4383208" class="blog_comment_body">
谢谢
</div>
<div class="comment_vote">
<span class="comment_error" style="color: red"></span>
<a href="javascript:void(0);" class="comment_digg" onclick="return voteComment(4383208, 'Digg', this.parentElement, false);">
支持(0)
</a>
<a href="javascript:void(0);" class="comment_burry" onclick="return voteComment(4383208, 'Bury', this.parentElement, false);">
反对(0)
</a>
</div>

<br>
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
<div id="ad_t2"><a href="http://www.ucancode.com/index.htm" target="_blank" onclick="ga('send', 'event', 'Link', 'click', 'T2-ucancode')">【推荐】超50万行VC++源码: 大型组态工控、电力仿真CAD与GIS源码库</a><br><a href="https://cloud.tencent.com/act/season?fromSource=gwzcw.3422970.3422970.3422970&amp;utm_medium=cpc&amp;utm_id=gwzcw.3422970.3422970.3422970" target="_blank" onclick="ga('send', 'event', 'Link', 'click', 'T2-腾讯云')">【活动】腾讯云服务器推出云产品采购季 1核2G首年仅需99元</a><br><a href="https://developer.aliyun.com/article/718649?utm_content=g_1000088936" target="_blank" onclick="ga('send', 'event', 'Link', 'click', 'T2-阿里云开发者社区')">【推荐】这6种编码方法，你掌握了几个？</a><br><a href="https://developer.aliyun.com/article/721829?utm_content=g_1000088935" target="_blank" onclick="ga('send', 'event', 'Link', 'click', 'T2-阿里云开发者社区')">【推荐】2019热门技术盛会400则演讲资料全收录</a><br></div>
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
<div id="div-gpt-ad-1546353474406-0" style="height:250px; width:300px;" data-google-query-id="CLC-7PuP3ecCFQcuKgodu28KUg"><div id="google_ads_iframe_/1090369/C1_0__container__" style="border: 0pt none;"><iframe id="google_ads_iframe_/1090369/C1_0" title="3rd party ad content" name="google_ads_iframe_/1090369/C1_0" width="300" height="250" scrolling="no" marginwidth="0" marginheight="0" frameborder="0" srcdoc="" style="border: 0px; vertical-align: bottom;" data-google-container-id="1" data-load-complete="true"></iframe></div></div>
</div>
<div id="under_post_news"><div class="recomm-block"><b>相关博文：</b><br>·  <a title="Spring的IOC原理[通俗解释一下]" href="https://www.cnblogs.com/Vae1990Silence/p/4622735.html" target="_blank" onclick="clickRecomItmem(4622735)">Spring的IOC原理[通俗解释一下]</a><br>·  <a title="Spring的IOC原理[通俗解释一下]" href="https://www.cnblogs.com/whoisaline/p/6403183.html" target="_blank" onclick="clickRecomItmem(6403183)">Spring的IOC原理[通俗解释一下]</a><br>·  <a title="Spring的IOC原理[通俗解释一下" href="https://www.cnblogs.com/zhaofei/p/3332785.html" target="_blank" onclick="clickRecomItmem(3332785)">Spring的IOC原理[通俗解释一下</a><br>·  <a title="Spring的IOC原理[通俗解释一下]" href="https://www.cnblogs.com/visionit/p/4183966.html" target="_blank" onclick="clickRecomItmem(4183966)">Spring的IOC原理[通俗解释一下]</a><br>·  <a title="Spring的IOC原理[通俗解释一下]" href="https://www.cnblogs.com/wuyida/p/6300371.html" target="_blank" onclick="clickRecomItmem(6300371)">Spring的IOC原理[通俗解释一下]</a><br>»  <a target="_blank" href="https://recomm.cnblogs.com/blogpost/4311577">更多推荐...</a><div id="cnblogs_t5"><a href="https://developer.aliyun.com/article/721818?utm_content=g_1000088934" target="_blank" onclick="ga('send', 'event', 'Link', 'click', 'T5-阿里云开发者社区')">20本必看的阿里精品免费电子书</a></div></div></div>
<div id="cnblogs_c2" class="c_ad_block">
<div id="div-gpt-ad-1539008685004-0" style="height:60px; width:468px;" data-google-query-id="CLG-7PuP3ecCFQcuKgodu28KUg">
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
·              <a href="//news.cnblogs.com/n/655975/" target="_blank">我国首颗5G卫星通信试验成功：通信能力达10Gbps</a>
<br>
·              <a href="//news.cnblogs.com/n/655974/" target="_blank">刘强东卸任京东云计算全资子公司经理一职</a>
<br>
·              <a href="//news.cnblogs.com/n/655973/" target="_blank">恒星参宿四停止变暗 近期或不会发生超新星爆炸</a>
<br>
·              <a href="//news.cnblogs.com/n/655972/" target="_blank">服务器DRAM合约价暴涨，但PC和手机快涨不动了</a>
<br>
·              <a href="//news.cnblogs.com/n/655971/" target="_blank">回应质疑：统一操作系统UOS的五大杀手锏</a>
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
</div></div>


</div><!--end: forFlow -->
</div>