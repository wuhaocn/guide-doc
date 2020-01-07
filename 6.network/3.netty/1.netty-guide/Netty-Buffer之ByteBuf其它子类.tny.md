<div class="article-inner">


<header class="article-header">


<h1 class="article-title" itemprop="name">
精尽 Netty 源码解析 —— Buffer 之 ByteBuf（四）其它子类
</h1>


</header>

<div class="article-entry" itemprop="articleBody">

<!-- Table of Contents -->

<h1 id="1-概述"><a href="#1-概述" class="headerlink" title="1. 概述"></a>1. 概述</h1><p>在前面三篇文章，我们已经看了 ByteBuf 最最最核心的几个实现类。而剩余的，主要是如下几个类，如<a href="http://static2.iocoder.cn/images/Netty/2018_08_10/01.png" rel="external nofollow noopener noreferrer" target="_blank">下图</a>所示：</p>
<p><a href="http://static2.iocoder.cn/images/Netty/2018_08_10/01.png" title="类图" class="fancybox" rel="article0"><img src="http://static2.iocoder.cn/images/Netty/2018_08_10/01.png" alt="类图"></a><span class="caption">类图</span></p>
<p>整理起来，用途主要是：</p>
<ul>
<li>Swap</li>
<li>Slice</li>
<li>Duplicate</li>
<li>ReadOnly</li>
<li>Composite</li>
</ul>
<p>因为老艿艿想要尽快去写 Jemalloc 内存管理相关的内容，所以本文先暂时“省略”。感兴趣的胖友，可以自己去研究下落。</p>
<p>TODO 1016 派生类</p>
<h1 id="2-Composite-ByteBuf"><a href="#2-Composite-ByteBuf" class="headerlink" title="2. Composite ByteBuf"></a>2. Composite ByteBuf</h1><p>因为 Composite ByteBuf 是比较重要的内容，胖友一定要自己去研究下。推荐阅读文章：</p>
<ul>
<li>键走偏锋 <a href="https://my.oschina.net/LucasZhu/blog/1617222" rel="external nofollow noopener noreferrer" target="_blank">《对于 Netty ByteBuf 的零拷贝（Zero Copy）的理解》</a></li>
<li><a href="https://skyao.gitbooks.io/learning-netty/content/buffer/class_CompositeByteBuf.html" rel="external nofollow noopener noreferrer" target="_blank">《Netty 学习笔记 —— 类CompositeByteBuf》</a></li>
</ul>
<h1 id="666-彩蛋"><a href="#666-彩蛋" class="headerlink" title="666. 彩蛋"></a>666. 彩蛋</h1><p>没有彩蛋。</p>


</div>
<!--
<footer class="article-footer">
<a data-url="http://svip.iocoder.cn/Netty/ByteBuf-1-4-ByteBuf-other-impl/" data-id="ck4pl3fp300dwfgcf2xh9o0nv" class="article-share-link">分享</a>



</footer>
-->
</div>