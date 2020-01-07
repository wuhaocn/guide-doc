<div class="article-inner">


<header class="article-header">


<h1 class="article-title" itemprop="name">
精尽 Netty 源码解析 —— ChannelHandler（二）之 ChannelInitializer
</h1>


</header>

<div class="article-entry" itemprop="articleBody">

<!-- Table of Contents -->

<h1 id="1-概述"><a href="#1-概述" class="headerlink" title="1. 概述"></a>1. 概述</h1><p>本文，我们来分享 <strong>ChannelInitializer</strong> 。它是一个<strong>特殊</strong>的ChannelInboundHandler 实现类，用于 Channel 注册到 EventLoop 后，<strong>执行自定义的初始化操作</strong>。一般情况下，初始化自定义的 ChannelHandler 到 Channel 中。例如：</p>
<ul>
<li>在 <a href="http://svip.iocoder.cn/Netty/bootstrap-1-server">《精尽 Netty 源码分析 —— 启动（一）之服务端》</a> 一文中，ServerBootstrap 初始化时，通过 ChannelInitializer 初始化了用于接受( accept )新连接的 ServerBootstrapAcceptor 。</li>
<li>在有新连接接入时，服务端通过 ChannelInitializer 初始化，为客户端的 Channel 添加自定义的 ChannelHandler ，用于处理该 Channel 的读写( read/write ) 事件。</li>
</ul>
<p>OK，让我们看看具体的代码实现落。</p>
<h1 id="2-ChannelInitializer"><a href="#2-ChannelInitializer" class="headerlink" title="2. ChannelInitializer"></a>2. ChannelInitializer</h1><p><code>io.netty.channel.ChannelInitializer</code> ，继承 ChannelInboundHandlerAdapter 类，Channel Initializer <strong>抽象类</strong>。代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="meta">@Sharable</span></span><br><span class="line"><span class="keyword">public</span> <span class="keyword">abstract</span> <span class="class"><span class="keyword">class</span> <span class="title">ChannelInitializer</span>&lt;<span class="title">C</span> <span class="keyword">extends</span> <span class="title">Channel</span>&gt; <span class="keyword">extends</span> <span class="title">ChannelInboundHandlerAdapter</span> </span>{</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li>通过 <code>@Sharable</code> 注解，支持共享。</li>
</ul>
<h2 id="2-1-initChannel"><a href="#2-1-initChannel" class="headerlink" title="2.1 initChannel"></a>2.1 initChannel</h2><p><code>#initChannel(ChannelHandlerContext ctx)</code> 方法，执行行自定义的初始化操作。代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="comment">// We use a ConcurrentMap as a ChannelInitializer is usually shared between all Channels in a Bootstrap /</span></span><br><span class="line"><span class="comment">// ServerBootstrap. This way we can reduce the memory usage compared to use Attributes.</span></span><br><span class="line"><span class="comment">/**</span></span><br><span class="line"><span class="comment"> * 由于 ChannelInitializer 可以在 Bootstrap/ServerBootstrap 的所有通道中共享，所以我们用一个 ConcurrentMap 作为初始化器。</span></span><br><span class="line"><span class="comment"> * 这种方式，相对于使用 {<span class="doctag">@link</span> io.netty.util.Attribute} 方式，减少了内存的使用。</span></span><br><span class="line"><span class="comment"> */</span></span><br><span class="line"><span class="keyword">private</span> <span class="keyword">final</span> ConcurrentMap&lt;ChannelHandlerContext, Boolean&gt; initMap = PlatformDependent.newConcurrentHashMap();</span><br><span class="line"></span><br><span class="line">  <span class="number">1</span>: <span class="function"><span class="keyword">private</span> <span class="keyword">boolean</span> <span class="title">initChannel</span><span class="params">(ChannelHandlerContext ctx)</span> <span class="keyword">throws</span> Exception </span>{</span><br><span class="line">  <span class="number">2</span>:     <span class="keyword">if</span> (initMap.putIfAbsent(ctx, Boolean.TRUE) == <span class="keyword">null</span>) { <span class="comment">// Guard against re-entrance. 解决并发问题</span></span><br><span class="line">  <span class="number">3</span>:         <span class="keyword">try</span> {</span><br><span class="line">  <span class="number">4</span>:             <span class="comment">// 初始化通道</span></span><br><span class="line">  <span class="number">5</span>:             initChannel((C) ctx.channel());</span><br><span class="line">  <span class="number">6</span>:         } <span class="keyword">catch</span> (Throwable cause) {</span><br><span class="line">  <span class="number">7</span>:             <span class="comment">// 发生异常时，执行异常处理</span></span><br><span class="line">  <span class="number">8</span>:             <span class="comment">// Explicitly call exceptionCaught(...) as we removed the handler before calling initChannel(...).</span></span><br><span class="line">  <span class="number">9</span>:             <span class="comment">// We do so to prevent multiple calls to initChannel(...).</span></span><br><span class="line"> <span class="number">10</span>:             exceptionCaught(ctx, cause);</span><br><span class="line"> <span class="number">11</span>:         } <span class="keyword">finally</span> {</span><br><span class="line"> <span class="number">12</span>:             <span class="comment">// 从 pipeline 移除 ChannelInitializer</span></span><br><span class="line"> <span class="number">13</span>:             remove(ctx);</span><br><span class="line"> <span class="number">14</span>:         }</span><br><span class="line"> <span class="number">15</span>:         <span class="keyword">return</span> <span class="keyword">true</span>; <span class="comment">// 初始化成功</span></span><br><span class="line"> <span class="number">16</span>:     }</span><br><span class="line"> <span class="number">17</span>:     <span class="keyword">return</span> <span class="keyword">false</span>; <span class="comment">// 初始化失败</span></span><br><span class="line"> <span class="number">18</span>: }</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li>第 2 行：通过 <code>initMap</code> 属性，解决并发问题。对应 Netty Git 提交是 <a href="https://github.com/netty/netty/commit/26aa34853a8974d212e12b98e708790606bea5fa" rel="external nofollow noopener noreferrer" target="_blank">https://github.com/netty/netty/commit/26aa34853a8974d212e12b98e708790606bea5fa</a> 。</li>
<li><p>第 5 行：调用 <code>#initChannel(C ch)</code> <strong>抽象</strong>方法，执行行自定义的初始化操作。代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="comment">/**</span></span><br><span class="line"><span class="comment"> * This method will be called once the {<span class="doctag">@link</span> Channel} was registered. After the method returns this instance</span></span><br><span class="line"><span class="comment"> * will be removed from the {<span class="doctag">@link</span> ChannelPipeline} of the {<span class="doctag">@link</span> Channel}.</span></span><br><span class="line"><span class="comment"> *</span></span><br><span class="line"><span class="comment"> * <span class="doctag">@param</span> ch            the {<span class="doctag">@link</span> Channel} which was registered.</span></span><br><span class="line"><span class="comment"> * <span class="doctag">@throws</span> Exception    is thrown if an error occurs. In that case it will be handled by</span></span><br><span class="line"><span class="comment"> *                      {<span class="doctag">@link</span> #exceptionCaught(ChannelHandlerContext, Throwable)} which will by default close</span></span><br><span class="line"><span class="comment"> *                      the {<span class="doctag">@link</span> Channel}.</span></span><br><span class="line"><span class="comment"> */</span></span><br><span class="line"><span class="function"><span class="keyword">protected</span> <span class="keyword">abstract</span> <span class="keyword">void</span> <span class="title">initChannel</span><span class="params">(C ch)</span> <span class="keyword">throws</span> Exception</span>;</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li>子类继承 ChannelInitializer 抽象类后，实现该方法，自定义 Channel 的初始化逻辑。</li>
</ul>
</li>
<li><p>第 6 至 10 行：调用 <code>#exceptionCaught(ChannelHandlerContext ctx, Throwable cause)</code> 方法，发生异常时，执行异常处理。代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="comment">/**</span></span><br><span class="line"><span class="comment"> * Handle the {<span class="doctag">@link</span> Throwable} by logging and closing the {<span class="doctag">@link</span> Channel}. Sub-classes may override this.</span></span><br><span class="line"><span class="comment"> */</span></span><br><span class="line"><span class="meta">@Override</span></span><br><span class="line"><span class="function"><span class="keyword">public</span> <span class="keyword">void</span> <span class="title">exceptionCaught</span><span class="params">(ChannelHandlerContext ctx, Throwable cause)</span> <span class="keyword">throws</span> Exception </span>{</span><br><span class="line">    <span class="keyword">if</span> (logger.isWarnEnabled()) {</span><br><span class="line">        logger.warn(<span class="string">"Failed to initialize a channel. Closing: "</span> + ctx.channel(), cause);</span><br><span class="line">    }</span><br><span class="line">    ctx.close();</span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li>打印<strong>告警</strong>日志。</li>
<li><strong>关闭</strong> Channel 通道。因为，初始化 Channel 通道发生异常，意味着很大可能，无法正常处理该 Channel 后续的读写事件。</li>
<li>😈 当然，<code>#exceptionCaught(...)</code> 方法，并非使用 <code>final</code> 修饰。所以也可以在子类覆写该方法。当然，笔者在实际使用并未这么做过。</li>
</ul>
</li>
<li><p>第 11 至 14 行：最终，调用 <code>#remove(ChannelHandlerContext ctx)</code> 方法，从 pipeline 移除 ChannelInitializer。代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="function"><span class="keyword">private</span> <span class="keyword">void</span> <span class="title">remove</span><span class="params">(ChannelHandlerContext ctx)</span> </span>{</span><br><span class="line">    <span class="keyword">try</span> {</span><br><span class="line">        <span class="comment">// 从 pipeline 移除 ChannelInitializer</span></span><br><span class="line">        ChannelPipeline pipeline = ctx.pipeline();</span><br><span class="line">        <span class="keyword">if</span> (pipeline.context(<span class="keyword">this</span>) != <span class="keyword">null</span>) {</span><br><span class="line">            pipeline.remove(<span class="keyword">this</span>);</span><br><span class="line">        }</span><br><span class="line">    } <span class="keyword">finally</span> {</span><br><span class="line">        initMap.remove(ctx); <span class="comment">// 从 initMap 移除</span></span><br><span class="line">    }</span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li>从 pipeline 移除 ChannelInitializer 后，避免重新初始化的问题。</li>
</ul>
</li>
<li>第 15 行：返回 <code>true</code> ，表示<strong>有</strong>执行初始化。</li>
<li>第 17 行：返回 <code>false</code> ，表示<strong>未</strong>执行初始化。</li>
</ul>
<h2 id="2-2-channelRegistered"><a href="#2-2-channelRegistered" class="headerlink" title="2.2 channelRegistered"></a>2.2 channelRegistered</h2><p>在 Channel 注册到 EventLoop 上后，会触发 Channel Registered 事件。那么 <code>ChannelInitializer</code> 的 <code>#channelRegistered(ChannelHandlerContext ctx)</code> 方法，就会处理该事件。而 ChannelInitializer 对该事件的处理逻辑是，初始化 Channel 。代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="meta">@Override</span></span><br><span class="line"><span class="meta">@SuppressWarnings</span>(<span class="string">"unchecked"</span>)</span><br><span class="line"><span class="function"><span class="keyword">public</span> <span class="keyword">final</span> <span class="keyword">void</span> <span class="title">channelRegistered</span><span class="params">(ChannelHandlerContext ctx)</span> <span class="keyword">throws</span> Exception </span>{</span><br><span class="line">    <span class="comment">// Normally this method will never be called as handlerAdded(...) should call initChannel(...) and remove</span></span><br><span class="line">    <span class="comment">// the handler.</span></span><br><span class="line">    <span class="comment">// &lt;1&gt; 初始化 Channel</span></span><br><span class="line">    <span class="keyword">if</span> (initChannel(ctx)) {</span><br><span class="line">        <span class="comment">// we called initChannel(...) so we need to call now pipeline.fireChannelRegistered() to ensure we not</span></span><br><span class="line">        <span class="comment">// miss an event.</span></span><br><span class="line">        <span class="comment">// &lt;2.1&gt; 重新触发 Channel Registered 事件</span></span><br><span class="line">        ctx.pipeline().fireChannelRegistered();</span><br><span class="line">    } <span class="keyword">else</span> {</span><br><span class="line">        <span class="comment">// &lt;2.2&gt; 继续向下一个节点的 Channel Registered 事件</span></span><br><span class="line">        <span class="comment">// Called initChannel(...) before which is the expected behavior, so just forward the event.</span></span><br><span class="line">        ctx.fireChannelRegistered();</span><br><span class="line">    }</span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li><code>&lt;1&gt;</code> 处，调用 <code>#initChannel(ChannelHandlerContext ctx)</code> 方法，初始化 Channel 。</li>
<li><code>&lt;2.1&gt;</code> 处，若有初始化，<strong>重新触发</strong> Channel Registered 事件。因为，很有可能添加了新的 ChannelHandler 到 pipeline 中。</li>
<li><code>&lt;2.2&gt;</code> 处，若无初始化，<strong>继续向下一个节点</strong>的 Channel Registered 事件。</li>
</ul>
<h2 id="2-3-handlerAdded"><a href="#2-3-handlerAdded" class="headerlink" title="2.3 handlerAdded"></a>2.3 handlerAdded</h2><p><code>ChannelInitializer#handlerAdded(ChannelHandlerContext ctx)</code> 方法，代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="meta">@Override</span></span><br><span class="line"><span class="function"><span class="keyword">public</span> <span class="keyword">void</span> <span class="title">handlerAdded</span><span class="params">(ChannelHandlerContext ctx)</span> <span class="keyword">throws</span> Exception </span>{</span><br><span class="line">    <span class="keyword">if</span> (ctx.channel().isRegistered()) { <span class="comment">// 已注册</span></span><br><span class="line">        <span class="comment">// This should always be true with our current DefaultChannelPipeline implementation.</span></span><br><span class="line">        <span class="comment">// The good thing about calling initChannel(...) in handlerAdded(...) is that there will be no ordering</span></span><br><span class="line">        <span class="comment">// surprises if a ChannelInitializer will add another ChannelInitializer. This is as all handlers</span></span><br><span class="line">        <span class="comment">// will be added in the expected order.</span></span><br><span class="line">        initChannel(ctx);</span><br><span class="line">    }</span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li>诶？怎么这里又调用了 <code>#initChannel(ChannelHandlerContext ctx)</code> 方法，初始化 Channel 呢？实际上，绝绝绝大多数情况下，因为 Channel Registered 事件触发在 Added <strong>之后</strong>，如果说在 <code>#handlerAdded(ChannelHandlerContext ctx)</code> 方法中，初始化 Channel 完成，那么 ChannelInitializer 便会从 pipeline 中移除。也就说，不会执行 <code>#channelRegistered(ChannelHandlerContext ctx)</code> 方法。</li>
<li>↑↑↑ 上面这段话听起来非常绕噢。简单来说，ChannelInitializer 调用 <code>#initChannel(ChannelHandlerContext ctx)</code> 方法，初始化 Channel 的调用来源，是来自 <code>#handlerAdded(...)</code> 方法，而不是 <code>#channelRegistered(...)</code> 方法。</li>
<li>还是不理解？胖友在 <code>#handlerAdded(ChannelHandlerContext ctx)</code> 方法上打上“<strong>断点</strong>”，并调试启动 <code>io.netty.example.echo.EchoServer</code> ，就能触发这种情况。原因是什么呢？如下图所示：<a href="http://static2.iocoder.cn/images/Netty/2018_10_04/02.png" title="register0" class="fancybox" rel="article0"><img src="http://static2.iocoder.cn/images/Netty/2018_10_04/02.png" alt="register0"></a><span class="caption">register0</span><ul>
<li>😈 红框部分，看到否？明白了哇。 </li>
</ul>
</li>
</ul>
<p>至于说，什么时候使用 ChannelInitializer 调用 <code>#initChannel(ChannelHandlerContext ctx)</code> 方法，初始化 Channel 的调用来源，是来自 <code>#channelRegistered(...)</code> 方法，笔者暂未发现。如果有知道的胖友，麻烦深刻教育我下。</p>
<p>TODO 1020 ChannelInitializer 对 channelRegistered 的触发</p>
<h1 id="666-彩蛋"><a href="#666-彩蛋" class="headerlink" title="666. 彩蛋"></a>666. 彩蛋</h1><p>小水文一篇。同时也推荐阅读：</p>
<ul>
<li>Donald_Draper <a href="http://donald-draper.iteye.com/blog/2389352" rel="external nofollow noopener noreferrer" target="_blank">《netty 通道初始化器ChannelInitializer》</a></li>
</ul>


</div>
<!--
<footer class="article-footer">
<a data-url="http://svip.iocoder.cn/Netty/ChannelHandler-2-ChannelInitializer/" data-id="ck4pl3fp700e9fgcfj7u8a2p4" class="article-share-link">分享</a>



</footer>
-->
</div>