<div class="article-inner">


<header class="article-header">


<h1 class="article-title" itemprop="name">
精尽 Netty 源码解析 —— Channel（六）之 writeAndFlush 操作
</h1>


</header>

<div class="article-entry" itemprop="articleBody">

<!-- Table of Contents -->

<h1 id="1-概述"><a href="#1-概述" class="headerlink" title="1. 概述"></a>1. 概述</h1><p>本文接 <a href="http://svip.iocoder.cn/Netty/Channel-5-flush/">《精尽 Netty 源码解析 —— Channel（五）之 flush 操作》</a> ，分享 Netty Channel 的 <code>#writeAndFlush(Object msg, ...)</code> 方法，write + flush 的组合，将数据写到内存队列后，立即刷新<strong>内存队列</strong>，又将其中的数据写入到对端。</p>
<p>😈 本来是不准备写这篇的，因为内容主要是 <a href="http://svip.iocoder.cn/Netty/Channel-4-write/">《精尽 Netty 源码解析 —— Channel（四）之 write 操作》</a> 和 <a href="http://svip.iocoder.cn/Netty/Channel-5-flush/">《精尽 Netty 源码解析 —— Channel（五）之 flush 操作》</a> 的组合。但是，考虑到内容的完整性，于是乎就稍微水更下下。</p>
<h1 id="2-AbstractChannel"><a href="#2-AbstractChannel" class="headerlink" title="2. AbstractChannel"></a>2. AbstractChannel</h1><p>AbstractChannel 对 <code>#writeAndFlush(Object msg, ...)</code> 方法的实现，代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="meta">@Override</span></span><br><span class="line"><span class="function"><span class="keyword">public</span> ChannelFuture <span class="title">writeAndFlush</span><span class="params">(Object msg)</span> </span>{</span><br><span class="line">    <span class="keyword">return</span> pipeline.writeAndFlush(msg);</span><br><span class="line">}</span><br><span class="line"></span><br><span class="line"><span class="meta">@Override</span></span><br><span class="line"><span class="function"><span class="keyword">public</span> ChannelFuture <span class="title">writeAndFlush</span><span class="params">(Object msg, ChannelPromise promise)</span> </span>{</span><br><span class="line">    <span class="keyword">return</span> pipeline.writeAndFlush(msg, promise);</span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li>在方法内部，会调用对应的 <code>ChannelPipeline#write(Object msg, ...)</code> 方法，将 write 和 flush <strong>两个</strong>事件在 pipeline 上传播。详细解析，见 <a href="#">「3. DefaultChannelPipeline」</a> 。<ul>
<li>最终会传播 write 事件到 <code>head</code> 节点，将数据写入到内存队列中。详细解析，见 <a href="#">「5. HeadContext」</a> 。</li>
<li>最终会传播 flush 事件到 <code>head</code> 节点，刷新<strong>内存队列</strong>，将其中的数据写入到对端。详细解析，见 <a href="#">「5. HeadContext」</a> 。</li>
</ul>
</li>
</ul>
<h1 id="3-DefaultChannelPipeline"><a href="#3-DefaultChannelPipeline" class="headerlink" title="3. DefaultChannelPipeline"></a>3. DefaultChannelPipeline</h1><p><code>DefaultChannelPipeline#writeAndFlush(Object msg, ...)</code> 方法，代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="meta">@Override</span></span><br><span class="line"><span class="function"><span class="keyword">public</span> <span class="keyword">final</span> ChannelFuture <span class="title">write</span><span class="params">(Object msg)</span> </span>{</span><br><span class="line">    <span class="keyword">return</span> tail.writeAndFlush(msg);</span><br><span class="line">}</span><br><span class="line"></span><br><span class="line"><span class="meta">@Override</span></span><br><span class="line"><span class="function"><span class="keyword">public</span> <span class="keyword">final</span> ChannelFuture <span class="title">write</span><span class="params">(Object msg, ChannelPromise promise)</span> </span>{</span><br><span class="line">    <span class="keyword">return</span> tail.writeAndFlush(msg, promise);</span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li>在方法内部，会调用 <code>TailContext#writeAndFlush(Object msg, ...)</code> 方法，将 write 和 flush <strong>两个</strong>事件在 pipeline 中，从尾节点向头节点传播。详细解析，见 <a href="#">「4. TailContext」</a> 。</li>
</ul>
<h1 id="4-TailContext"><a href="#4-TailContext" class="headerlink" title="4. TailContext"></a>4. TailContext</h1><p>TailContext 对 <code>TailContext#writeAndFlush(Object msg, ...)</code> 方法的实现，是从 AbstractChannelHandlerContext 抽象类继承，代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="meta">@Override</span></span><br><span class="line"><span class="function"><span class="keyword">public</span> ChannelFuture <span class="title">writeAndFlush</span><span class="params">(Object msg, ChannelPromise promise)</span> </span>{</span><br><span class="line">    <span class="keyword">if</span> (msg == <span class="keyword">null</span>) {</span><br><span class="line">        <span class="keyword">throw</span> <span class="keyword">new</span> NullPointerException(<span class="string">"msg"</span>);</span><br><span class="line">    }</span><br><span class="line"></span><br><span class="line">    <span class="comment">// 判断是否为合法的 Promise 对象</span></span><br><span class="line">    <span class="keyword">if</span> (isNotValidPromise(promise, <span class="keyword">true</span>)) {</span><br><span class="line">        <span class="comment">// 释放消息( 数据 )相关的资源</span></span><br><span class="line">        ReferenceCountUtil.release(msg);</span><br><span class="line">        <span class="comment">// cancelled</span></span><br><span class="line">        <span class="keyword">return</span> promise;</span><br><span class="line">    }</span><br><span class="line"></span><br><span class="line">    <span class="comment">// 写入消息( 数据 )到内存队列</span></span><br><span class="line">    write(msg, <span class="keyword">true</span>, promise); <span class="comment">// &lt;1&gt;</span></span><br><span class="line"></span><br><span class="line">    <span class="keyword">return</span> promise;</span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li><p>这个方法，和我们在 <a href="http://svip.iocoder.cn/Netty/Channel-4-write/">《精尽 Netty 源码解析 —— Channel（四）之 write 操作》</a> 的 <a href="#">「4. TailContext」</a> 的小节，<code>TailContext#write(Object msg, ...)</code> 方法，基本类似，差异在于 <code>&lt;1&gt;</code> 处，调用 <code>#write(Object msg, boolean flush, ChannelPromise promise)</code> 方法，传入的 <code>flush = true</code> 方法参数，表示 write 操作的同时，<strong>后续</strong>需要执行 flush 操作。代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="function"><span class="keyword">private</span> <span class="keyword">void</span> <span class="title">write</span><span class="params">(Object msg, <span class="keyword">boolean</span> flush, ChannelPromise promise)</span> </span>{</span><br><span class="line">    <span class="comment">// 获得下一个 Outbound 节点</span></span><br><span class="line">    AbstractChannelHandlerContext next = findContextOutbound();</span><br><span class="line">    <span class="comment">// 简化代码 😈</span></span><br><span class="line">    <span class="comment">// 执行 write + flush 操作</span></span><br><span class="line">    next.invokeWriteAndFlush(m, promise);</span><br><span class="line">}</span><br><span class="line"></span><br><span class="line"><span class="function"><span class="keyword">private</span> <span class="keyword">void</span> <span class="title">invokeWriteAndFlush</span><span class="params">(Object msg, ChannelPromise promise)</span> </span>{</span><br><span class="line">    <span class="keyword">if</span> (invokeHandler()) {</span><br><span class="line">        <span class="comment">// 执行 write 事件到下一个节点</span></span><br><span class="line">        invokeWrite0(msg, promise);</span><br><span class="line">        <span class="comment">// 执行 flush 事件到下一个节点</span></span><br><span class="line">        invokeFlush0();</span><br><span class="line">    } <span class="keyword">else</span> {</span><br><span class="line">        writeAndFlush(msg, promise);</span><br><span class="line">    }</span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li>在后面，就是 <a href="http://svip.iocoder.cn/Netty/Channel-4-write/">《精尽 Netty 源码解析 —— Channel（四）之 write 操作》</a> 的 <a href="#">「5. HeadContext」</a> 的小节及其后续的小节。</li>
<li>再在后面，就是 <a href="http://svip.iocoder.cn/Netty/Channel-5-flush/">《精尽 Netty 源码解析 —— Channel（五）之 flush 操作》</a> 。</li>
</ul>
</li>
</ul>
<h1 id="666-彩蛋"><a href="#666-彩蛋" class="headerlink" title="666. 彩蛋"></a>666. 彩蛋</h1><p>😈 真的是水更，哈哈哈哈。</p>
<p>推荐阅读文章：</p>
<ul>
<li>闪电侠 <a href="https://www.jianshu.com/p/feaeaab2ce56" rel="external nofollow noopener noreferrer" target="_blank">《netty 源码分析之 writeAndFlush 全解析》</a> 的 <a href="#">「writeAndFlush: 写队列并刷新」</a> 小节。</li>
</ul>


</div>
<!--
<footer class="article-footer">
<a data-url="http://svip.iocoder.cn/Netty/Channel-6-writeAndFlush/" data-id="ck4pl3fp200dsfgcfved64hok" class="article-share-link">分享</a>



</footer>
-->
</div>