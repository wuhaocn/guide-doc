<div class="article-inner">


<header class="article-header">


<h1 class="article-title" itemprop="name">
精尽 Netty 源码解析 —— ChannelPipeline（五）之 Inbound 事件的传播
</h1>


</header>

<div class="article-entry" itemprop="articleBody">

<!-- Table of Contents -->

<h1 id="1-概述"><a href="#1-概述" class="headerlink" title="1. 概述"></a>1. 概述</h1><p>本文我们来分享，在 pipeline 中的 <strong>Inbound 事件的传播</strong>。我们先来回顾下 Inbound 事件的定义：</p>
<blockquote>
<p>老艿艿：B01、B02 等等，是我们每条定义的编号。</p>
</blockquote>
<ul>
<li><p>[x] B01：Inbound 事件是【通知】事件, 当某件事情已经就绪后, 通知上层.</p>
<blockquote>
<p>老艿艿：B01 = B02 + B03</p>
</blockquote>
</li>
<li><p>[x] B02：Inbound 事件发起者是 Unsafe</p>
</li>
<li>[x] B03：Inbound 事件的处理者是 TailContext, 如果用户没有实现自定义的处理方法, 那么Inbound 事件默认的处理者是 TailContext, 并且其处理方法是空实现.</li>
<li>[x] B04：Inbound 事件在 Pipeline 中传输方向是 <code>head</code>( 头 ) -&gt; <code>tail</code>( 尾 )</li>
<li>[x] B05：在 ChannelHandler 中处理事件时, 如果这个 Handler 不是最后一个 Handler, 则需要调用 <code>ctx.fireIN_EVT</code> (例如 <code>ctx.fireChannelActive</code> ) 将此事件继续传播下去. 如果不这样做, 那么此事件的传播会提前终止.</li>
<li>[x] B06：Inbound 事件流: <code>Context.fireIN_EVT</code> -&gt; <code>Connect.findContextInbound</code> -&gt; <code>nextContext.invokeIN_EVT</code> -&gt; <code>nextHandler.IN_EVT</code> -&gt; <code>nextContext.fireIN_EVT</code></li>
</ul>
<p>Outbound 和 Inbound 事件十分的镜像，所以，接下来我们来跟着的代码，和 <a href="http://svip.iocoder.cn/Netty/Pipeline-4-inbound">《精尽 Netty 源码解析 —— ChannelPipeline（四）之 Outbound 事件的传播》</a> 会非常相似。</p>
<h1 id="2-ChannelInboundInvoker"><a href="#2-ChannelInboundInvoker" class="headerlink" title="2. ChannelInboundInvoker"></a>2. ChannelInboundInvoker</h1><p>在 <code>io.netty.channel.ChannelInboundInvoker</code> 接口中，定义了所有 Inbound 事件对应的方法：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="function">ChannelInboundInvoker <span class="title">fireChannelRegistered</span><span class="params">()</span></span>;</span><br><span class="line"><span class="function">ChannelInboundInvoker <span class="title">fireChannelUnregistered</span><span class="params">()</span></span>;</span><br><span class="line"></span><br><span class="line"><span class="function">ChannelInboundInvoker <span class="title">fireChannelActive</span><span class="params">()</span></span>;</span><br><span class="line"><span class="function">ChannelInboundInvoker <span class="title">fireChannelInactive</span><span class="params">()</span></span>;</span><br><span class="line"></span><br><span class="line"><span class="function">ChannelInboundInvoker <span class="title">fireExceptionCaught</span><span class="params">(Throwable cause)</span></span>;</span><br><span class="line"></span><br><span class="line"><span class="function">ChannelInboundInvoker <span class="title">fireUserEventTriggered</span><span class="params">(Object event)</span></span>;</span><br><span class="line"></span><br><span class="line"><span class="function">ChannelInboundInvoker <span class="title">fireChannelRead</span><span class="params">(Object msg)</span></span>;</span><br><span class="line"><span class="function">ChannelInboundInvoker <span class="title">fireChannelReadComplete</span><span class="params">()</span></span>;</span><br><span class="line"></span><br><span class="line"><span class="function">ChannelInboundInvoker <span class="title">fireChannelWritabilityChanged</span><span class="params">()</span></span>;</span><br></pre></td></tr></tbody></table></figure>
<p>而 ChannelInboundInvoker 的<strong>部分</strong>子类/接口如下图：</p>
<p><a href="http://static2.iocoder.cn/images/Netty/2018_06_13/01.png" title="类图" class="fancybox" rel="article0"><img src="http://static2.iocoder.cn/images/Netty/2018_06_13/01.png" alt="类图"></a><span class="caption">类图</span></p>
<ul>
<li>我们可以看到类图，有 ChannelPipeline、AbstractChannelHandlerContext 都继承/实现了该接口。那这意味着什么呢？我们继续往下看。</li>
<li>相比来说，Channel 实现了 ChannelOutboundInvoker 接口，但是<strong>不实现</strong> ChannelInboundInvoker 接口。</li>
</ul>
<p>在 <a href="http://svip.iocoder.cn/Netty/bootstrap-1-server/">《精尽 Netty 源码解析 —— 启动（一）之服务端》</a> 中，我们可以看到 Inbound 事件的其中之一 <strong>fireChannelActive</strong> ，本文就以 <strong>fireChannelActive</strong> 的过程，作为示例。调用栈如下：</p>
<p><a href="http://static2.iocoder.cn/images/Netty/2018_06_13/02.png" title="调用栈" class="fancybox" rel="article0"><img src="http://static2.iocoder.cn/images/Netty/2018_06_13/02.png" alt="调用栈"></a><span class="caption">调用栈</span></p>
<ul>
<li><p><code>AbstractUnsafe#bind(final SocketAddress localAddress, final ChannelPromise promise)</code> 方法，代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="meta">@Override</span></span><br><span class="line"><span class="function"><span class="keyword">public</span> <span class="keyword">final</span> <span class="keyword">void</span> <span class="title">bind</span><span class="params">(<span class="keyword">final</span> SocketAddress localAddress, <span class="keyword">final</span> ChannelPromise promise)</span> </span>{</span><br><span class="line">    <span class="comment">// 判断是否在 EventLoop 的线程中。</span></span><br><span class="line">    assertEventLoop();</span><br><span class="line"></span><br><span class="line">    <span class="comment">// ... 省略部分代码</span></span><br><span class="line">   </span><br><span class="line">    <span class="comment">// 记录 Channel 是否激活</span></span><br><span class="line">    <span class="keyword">boolean</span> wasActive = isActive();</span><br><span class="line"></span><br><span class="line">    <span class="comment">// 绑定 Channel 的端口</span></span><br><span class="line">    doBind(localAddress);</span><br><span class="line"></span><br><span class="line">    <span class="comment">// 若 Channel 是新激活的，触发通知 Channel 已激活的事件。 </span></span><br><span class="line">    <span class="keyword">if</span> (!wasActive &amp;&amp; isActive()) {</span><br><span class="line">        invokeLater(<span class="keyword">new</span> Runnable() {</span><br><span class="line">            <span class="meta">@Override</span></span><br><span class="line">            <span class="function"><span class="keyword">public</span> <span class="keyword">void</span> <span class="title">run</span><span class="params">()</span> </span>{</span><br><span class="line">                pipeline.fireChannelActive(); <span class="comment">// &lt;1&gt;</span></span><br><span class="line">            }</span><br><span class="line">        });</span><br><span class="line">    }</span><br><span class="line"></span><br><span class="line">    <span class="comment">// 回调通知 promise 执行成功</span></span><br><span class="line">    safeSetSuccess(promise);</span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li>在 <code>&lt;1&gt;</code> 处，调用 <code>ChannelPipeline#fireChannelActive()</code> 方法。<ul>
<li>Unsafe 是 <strong>fireChannelActive</strong> 的发起者，<strong>这符合 Inbound 事件的定义 B02</strong> 。</li>
<li>那么接口下，让我们看看 <code>ChannelPipeline#fireChannelActive()</code> 方法的具体实现。</li>
</ul>
</li>
</ul>
</li>
</ul>
<h1 id="3-DefaultChannelPipeline"><a href="#3-DefaultChannelPipeline" class="headerlink" title="3. DefaultChannelPipeline"></a>3. DefaultChannelPipeline</h1><p><code>DefaultChannelPipeline#fireChannelActive()</code> 方法的实现，代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="meta">@Override</span></span><br><span class="line"><span class="function"><span class="keyword">public</span> <span class="keyword">final</span> ChannelPipeline <span class="title">fireChannelActive</span><span class="params">()</span> </span>{</span><br><span class="line">    AbstractChannelHandlerContext.invokeChannelActive(head);</span><br><span class="line">    <span class="keyword">return</span> <span class="keyword">this</span>;</span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li>在方法内部，会调用 <code>AbstractChannelHandlerContext#invokeChannelActive(final AbstractChannelHandlerContext next)</code> 方法，而方法参数是 <code>head</code> ，<strong>这符合 Inbound 事件的定义 B04</strong> 。<ul>
<li>实际上，HeadContext 的该方法，继承自 AbstractChannelHandlerContext 抽象类，而 AbstractChannelHandlerContext 实现了 ChannelInboundInvoker 接口。<em>从这里可以看出，对于 ChannelInboundInvoker 接口方法的实现，ChannelPipeline 对它的实现，会调用 AbstractChannelHandlerContext 的对应方法</em>( 有一点绕，胖友理解下 )。</li>
</ul>
</li>
</ul>
<h1 id="4-AbstractChannelHandlerContext-invokeChannelActive"><a href="#4-AbstractChannelHandlerContext-invokeChannelActive" class="headerlink" title="4. AbstractChannelHandlerContext#invokeChannelActive"></a>4. AbstractChannelHandlerContext#invokeChannelActive</h1><p><code>AbstractChannelHandlerContext#invokeChannelActive(final AbstractChannelHandlerContext next)</code> <strong>静态</strong>方法，代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"> <span class="number">1</span>: <span class="function"><span class="keyword">static</span> <span class="keyword">void</span> <span class="title">invokeChannelActive</span><span class="params">(<span class="keyword">final</span> AbstractChannelHandlerContext next)</span> </span>{</span><br><span class="line"> <span class="number">2</span>:     <span class="comment">// 获得下一个 Inbound 节点的执行器</span></span><br><span class="line"> <span class="number">3</span>:     EventExecutor executor = next.executor();</span><br><span class="line"> <span class="number">4</span>:     <span class="comment">// 调用下一个 Inbound 节点的 Channel active 方法</span></span><br><span class="line"> <span class="number">5</span>:     <span class="keyword">if</span> (executor.inEventLoop()) {</span><br><span class="line"> <span class="number">6</span>:         next.invokeChannelActive();</span><br><span class="line"> <span class="number">7</span>:     } <span class="keyword">else</span> {</span><br><span class="line"> <span class="number">8</span>:         executor.execute(<span class="keyword">new</span> Runnable() {</span><br><span class="line"> <span class="number">9</span>:             <span class="meta">@Override</span></span><br><span class="line"><span class="number">10</span>:             <span class="function"><span class="keyword">public</span> <span class="keyword">void</span> <span class="title">run</span><span class="params">()</span> </span>{</span><br><span class="line"><span class="number">11</span>:                 next.invokeChannelActive();</span><br><span class="line"><span class="number">12</span>:             }</span><br><span class="line"><span class="number">13</span>:         });</span><br><span class="line"><span class="number">14</span>:     }</span><br><span class="line"><span class="number">15</span>: }</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li>方法参数 <code>next</code> ，下一个 Inbound 节点。</li>
<li>第 3 行：调用 <code>AbstractChannelHandlerContext#executor()</code> 方法，获得下一个 Inbound 节点的执行器。</li>
<li><p>第 4 至 14 行：调用 <code>AbstractChannelHandlerContext#invokeChannelActive()</code> 方法，调用下一个 Inbound 节点的 Channel active 方法。</p>
<ul>
<li>在 <a href="#">「3. DefaultChannelPipeline」</a> 中，我们可以看到传递的<strong>第一个</strong> <code>next</code> 方法参数为 <code>head</code>( HeadContext ) 节点。</li>
<li><p>代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"> <span class="number">1</span>: <span class="function"><span class="keyword">private</span> <span class="keyword">void</span> <span class="title">invokeChannelActive</span><span class="params">()</span> </span>{</span><br><span class="line"> <span class="number">2</span>:     <span class="keyword">if</span> (invokeHandler()) { <span class="comment">// 判断是否符合的 ChannelHandler</span></span><br><span class="line"> <span class="number">3</span>:         <span class="keyword">try</span> {</span><br><span class="line"> <span class="number">4</span>:             <span class="comment">// 调用该 ChannelHandler 的 Channel active 方法</span></span><br><span class="line"> <span class="number">5</span>:             ((ChannelInboundHandler) handler()).channelActive(<span class="keyword">this</span>);</span><br><span class="line"> <span class="number">6</span>:         } <span class="keyword">catch</span> (Throwable t) {</span><br><span class="line"> <span class="number">7</span>:             notifyHandlerException(t);  <span class="comment">// 通知 Inbound 事件的传播，发生异常</span></span><br><span class="line"> <span class="number">8</span>:         }</span><br><span class="line"> <span class="number">9</span>:     } <span class="keyword">else</span> {</span><br><span class="line"><span class="number">10</span>:         <span class="comment">// 跳过，传播 Inbound 事件给下一个节点</span></span><br><span class="line"><span class="number">11</span>:         fireChannelActive();</span><br><span class="line"><span class="number">12</span>:     }</span><br><span class="line"><span class="number">13</span>: }</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li>第 2 行：调用 <code>#invokeHandler()</code> 方法，判断是否符合的 ChannelHandler 。</li>
<li>第 9 至 12 行：若是<strong>不符合</strong>的 ChannelHandler ，则<strong>跳过</strong>该节点，调用 <code>AbstractChannelHandlerContext#fireChannelActive(</code> 方法，传播 Inbound 事件给下一个节点。详细解析，见 <a href="#">「6. AbstractChannelHandlerContext#fireChannelActive」</a> 。</li>
<li><p>第 2 至 8 行：若是<strong>符合</strong>的 ChannelHandler ：</p>
<ul>
<li><p>第 5 行：调用 ChannelHandler 的 <code>#channelActive(ChannelHandlerContext ctx)</code> 方法，处理 Channel active 事件。</p>
<ul>
<li><p>😈 实际上，此时节点的数据类型为 DefaultChannelHandlerContext 类。若它被认为是 Inbound 节点，那么他的处理器的类型会是 <strong>ChannelInboundHandler</strong> 。而 <code>io.netty.channel.ChannelInboundHandler</code> 类似 ChannelInboundInvoker ，定义了对每个 Inbound 事件的处理。代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="function"><span class="keyword">void</span> <span class="title">channelRegistered</span><span class="params">(ChannelHandlerContext ctx)</span> <span class="keyword">throws</span> Exception</span>;</span><br><span class="line"><span class="function"><span class="keyword">void</span> <span class="title">channelUnregistered</span><span class="params">(ChannelHandlerContext ctx)</span> <span class="keyword">throws</span> Exception</span>;</span><br><span class="line"></span><br><span class="line"><span class="function"><span class="keyword">void</span> <span class="title">channelActive</span><span class="params">(ChannelHandlerContext ctx)</span> <span class="keyword">throws</span> Exception</span>;</span><br><span class="line"><span class="function"><span class="keyword">void</span> <span class="title">channelInactive</span><span class="params">(ChannelHandlerContext ctx)</span> <span class="keyword">throws</span> Exception</span>;</span><br><span class="line"></span><br><span class="line"><span class="function"><span class="keyword">void</span> <span class="title">userEventTriggered</span><span class="params">(ChannelHandlerContext ctx, Object evt)</span> <span class="keyword">throws</span> Exception</span>;</span><br><span class="line"></span><br><span class="line"><span class="function"><span class="keyword">void</span> <span class="title">channelRead</span><span class="params">(ChannelHandlerContext ctx, Object msg)</span> <span class="keyword">throws</span> Exception</span>;</span><br><span class="line"><span class="function"><span class="keyword">void</span> <span class="title">channelReadComplete</span><span class="params">(ChannelHandlerContext ctx)</span> <span class="keyword">throws</span> Exception</span>;</span><br><span class="line"></span><br><span class="line"><span class="function"><span class="keyword">void</span> <span class="title">channelWritabilityChanged</span><span class="params">(ChannelHandlerContext ctx)</span> <span class="keyword">throws</span> Exception</span>;</span><br><span class="line"> </span><br><span class="line"><span class="meta">@Override</span></span><br><span class="line"><span class="meta">@SuppressWarnings</span>(<span class="string">"deprecation"</span>)</span><br><span class="line"><span class="function"><span class="keyword">void</span> <span class="title">exceptionCaught</span><span class="params">(ChannelHandlerContext ctx, Throwable cause)</span> <span class="keyword">throws</span> Exception</span>; <span class="comment">// 不属于 Inbound 事件</span></span><br></pre></td></tr></tbody></table></figure>
<ul>
<li>胖友自己对比下噢。</li>
</ul>
</li>
<li><p>如果节点的 <code>ChannelInboundHandler#channelActive(ChannelHandlerContext ctx</code> 方法的实现，不调用 <code>AbstractChannelHandlerContext#fireChannelActive()</code> 方法，就不会传播 Inbound 事件给下一个节点。<strong>这就是 Inbound 事件的定义 B05</strong> 。可能有点绕，我们来看下 Netty LoggingHandler 对该方法的实现代码：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="keyword">final</span> <span class="class"><span class="keyword">class</span> <span class="title">LoggingHandler</span> <span class="keyword">implements</span> <span class="title">ChannelInboundHandler</span>, <span class="title">ChannelOutboundHandler</span> </span>{</span><br><span class="line"></span><br><span class="line">    <span class="comment">// ... 省略无关方法</span></span><br><span class="line">    </span><br><span class="line">    <span class="meta">@Override</span></span><br><span class="line">    <span class="function"><span class="keyword">public</span> <span class="keyword">void</span> <span class="title">channelActive</span><span class="params">(ChannelHandlerContext ctx)</span> <span class="keyword">throws</span> Exception </span>{</span><br><span class="line">        <span class="comment">// 打印日志</span></span><br><span class="line">        <span class="keyword">if</span> (logger.isEnabled(internalLevel)) {</span><br><span class="line">            logger.log(internalLevel, format(ctx, <span class="string">"ACTIVE"</span>));</span><br><span class="line">        }</span><br><span class="line">        <span class="comment">// 传递 Channel active 事件，给下一个节点</span></span><br><span class="line">        ctx.fireChannelActive(); <span class="comment">// &lt;1&gt;</span></span><br><span class="line">    }</span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li>如果把 <code>&lt;1&gt;</code> 处的代码去掉，Channel active 事件 事件将不会传播给下一个节点！！！<strong>一定要注意</strong>。</li>
</ul>
</li>
<li>这块的逻辑非常重要，如果胖友觉得很绕，一定要自己多调试 + 调试 + 调试。</li>
</ul>
</li>
<li>第 7 行：如果发生异常，调用 <code>#notifyHandlerException(Throwable)</code> 方法，通知 Inbound 事件的传播，发生异常。详细解析，见 <a href="http://svip.iocoder.cn/Netty/ChannelPipeline-6-exception">《精尽 Netty 源码解析 —— ChannelPipeline（六）之异常事件的传播》</a> 。 </li>
</ul>
</li>
</ul>
</li>
</ul>
</li>
</ul>
<h1 id="5-HeadContext"><a href="#5-HeadContext" class="headerlink" title="5. HeadContext"></a>5. HeadContext</h1><p><code>HeadContext#invokeChannelActive()</code> 方法，代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="meta">@Override</span></span><br><span class="line"><span class="function"><span class="keyword">public</span> <span class="keyword">void</span> <span class="title">channelActive</span><span class="params">(ChannelHandlerContext ctx)</span> <span class="keyword">throws</span> Exception </span>{</span><br><span class="line">    <span class="comment">// 传播 Channel active 事件给下一个 Inbound 节点 &lt;1&gt;</span></span><br><span class="line">    ctx.fireChannelActive();</span><br><span class="line"></span><br><span class="line">    <span class="comment">// 执行 read 逻辑 &lt;2&gt;</span></span><br><span class="line">    readIfIsAutoRead();</span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li><code>&lt;1&gt;</code> 处，调用 <code>AbstractChannelHandlerContext#fireChannelActive()</code> 方法，传播 Channel active 事件给下一个 Inbound 节点。详细解析，见 <a href="#">「6. AbstractChannelHandlerContext」</a> 中。</li>
<li><p><code>&lt;2&gt;</code> 处，调用 <code>HeadContext#readIfIsAutoRead()</code> 方法，执行 read 逻辑。代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="comment">// HeadContext.java</span></span><br><span class="line"><span class="function"><span class="keyword">private</span> <span class="keyword">void</span> <span class="title">readIfIsAutoRead</span><span class="params">()</span> </span>{</span><br><span class="line">    <span class="keyword">if</span> (channel.config().isAutoRead()) { </span><br><span class="line">        channel.read();</span><br><span class="line">    }</span><br><span class="line">}</span><br><span class="line"></span><br><span class="line"><span class="comment">// AbstractChannel.java</span></span><br><span class="line"><span class="meta">@Override</span></span><br><span class="line"><span class="function"><span class="keyword">public</span> Channel <span class="title">read</span><span class="params">()</span> </span>{</span><br><span class="line">    pipeline.read();</span><br><span class="line">    <span class="keyword">return</span> <span class="keyword">this</span>;</span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li><p>该方法内部，会调用 <code>Channel#read()</code> 方法，而后通过 pipeline 传递该 <strong>read</strong> OutBound 事件，最终调用 <code>HeadContext#read()</code> 方法，代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="meta">@Override</span></span><br><span class="line"><span class="function"><span class="keyword">public</span> <span class="keyword">void</span> <span class="title">read</span><span class="params">(ChannelHandlerContext ctx)</span> </span>{</span><br><span class="line">    unsafe.beginRead();</span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li>后续的逻辑，便是 <a href="http://svip.iocoder.cn/Netty/bootstrap-1-server/">《精尽 Netty 源码分析 —— 启动（一）之服务端》</a> 的 <a href="#">3.13.3 beginRead</a> 小节，胖友可以自己再去回顾下。</li>
</ul>
</li>
<li>这里说的是 <strong>OutBound</strong> 事件，不是本文的 InBound 事件。所以，胖友不要搞混哈。只能说是对 <a href="http://svip.iocoder.cn/Netty/bootstrap-1-server/">《精尽 Netty 源码分析 —— 启动（一）之服务端》</a> 的 <a href="#">3.13.3 beginRead</a> 小节的补充。</li>
</ul>
</li>
</ul>
<h1 id="6-AbstractChannelHandlerContext-fireChannelActive"><a href="#6-AbstractChannelHandlerContext-fireChannelActive" class="headerlink" title="6. AbstractChannelHandlerContext#fireChannelActive"></a>6. AbstractChannelHandlerContext#fireChannelActive</h1><p><code>AbstractChannelHandlerContext#fireChannelActive()</code> 方法，代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="meta">@Override</span></span><br><span class="line"><span class="function"><span class="keyword">public</span> ChannelHandlerContext <span class="title">fireChannelActive</span><span class="params">()</span> </span>{</span><br><span class="line">    <span class="comment">// 获得下一个 Inbound 节点的执行器</span></span><br><span class="line">    <span class="comment">// 调用下一个 Inbound 节点的 Channel active 方法</span></span><br><span class="line">    invokeChannelActive(findContextInbound());</span><br><span class="line">    <span class="keyword">return</span> <span class="keyword">this</span>;</span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li><p>【重要】调用 <code>AbstractChannelHandlerContext#findContextInbound()</code> 方法，获得下一个 Inbound 节点的执行器。代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="function"><span class="keyword">private</span> AbstractChannelHandlerContext <span class="title">findContextInbound</span><span class="params">()</span> </span>{</span><br><span class="line">    <span class="comment">// 循环，向后获得一个 Inbound 节点</span></span><br><span class="line">    AbstractChannelHandlerContext ctx = <span class="keyword">this</span>;</span><br><span class="line">    <span class="keyword">do</span> {</span><br><span class="line">        ctx = ctx.next;</span><br><span class="line">    } <span class="keyword">while</span> (!ctx.inbound);</span><br><span class="line">    <span class="keyword">return</span> ctx;</span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li>循环，<strong>向后</strong>获得一个 Inbound 节点。</li>
<li>循环，<strong>向后</strong>获得一个 Inbound 节点。</li>
<li>循环，<strong>向后</strong>获得一个 Inbound 节点。</li>
<li>😈 重要的事情说三遍，对于 Inbound 事件的传播，是从 pipeline 的头部到尾部，<strong>这符合 Inbound 事件的定义 B04</strong> 。</li>
</ul>
</li>
<li>调用 <code>AbstractChannelHandlerContext#invokeChannelActive(AbstractChannelHandlerContext)</code> <strong>静态</strong>方法，调用下一个 Inbound 节点的 Channel active 方法。即，又回到 <a href="#">「4. AbstractChannelHandlerContext#invokeChannelActive」</a> 的开头。</li>
</ul>
<hr>
<p>本小节的整个代码实现，<strong>就是 Inbound 事件的定义 B06</strong> 的体现。而随着 Inbound 事件在节点不断从 pipeline 的头部到尾部的传播，最终会到达 TailContext 节点。</p>
<h1 id="7-TailContext"><a href="#7-TailContext" class="headerlink" title="7. TailContext"></a>7. TailContext</h1><p><code>TailContext#channelActive(ChannelHandlerContext ctx)</code> 方法，代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="meta">@Override</span></span><br><span class="line"><span class="function"><span class="keyword">public</span> <span class="keyword">void</span> <span class="title">channelActive</span><span class="params">(ChannelHandlerContext ctx)</span> <span class="keyword">throws</span> Exception </span>{</span><br><span class="line">    onUnhandledInboundChannelActive();</span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li><p>在方法内部，会调用 <code>DefaultChannelPipeline#onUnhandledInboundChannelActive()</code> 方法，代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="comment">/**</span></span><br><span class="line"><span class="comment"> * Called once the {<span class="doctag">@link</span> ChannelInboundHandler#channelActive(ChannelHandlerContext)}event hit</span></span><br><span class="line"><span class="comment"> * the end of the {<span class="doctag">@link</span> ChannelPipeline}.</span></span><br><span class="line"><span class="comment"> */</span></span><br><span class="line"><span class="function"><span class="keyword">protected</span> <span class="keyword">void</span> <span class="title">onUnhandledInboundChannelActive</span><span class="params">()</span> </span>{</span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li>该方法是个<strong>空</strong>方法，<strong>这符合 Inbound 事件的定义 B03</strong> 。</li>
<li>至此，整个 pipeline 的 Inbound 事件的传播结束。</li>
</ul>
</li>
</ul>
<h1 id="8-关于其他-Inbound-事件"><a href="#8-关于其他-Inbound-事件" class="headerlink" title="8. 关于其他 Inbound 事件"></a>8. 关于其他 Inbound 事件</h1><p>本文暂时只分享了 <strong>firecChannelActive</strong> 这个 Inbound 事件。剩余的其他事件，胖友可以自己进行调试和理解。例如：<strong>fireChannelRegistered</strong> 事件，并且结合 <a href="http://svip.iocoder.cn/Netty/bootstrap-1-server/">《精尽 Netty 源码分析 —— 启动（一）之服务端》</a> 一文。</p>
<h1 id="666-彩蛋"><a href="#666-彩蛋" class="headerlink" title="666. 彩蛋"></a>666. 彩蛋</h1><p>推荐阅读文章：</p>
<ul>
<li>闪电侠 <a href="https://www.jianshu.com/p/087b7e9a27a2" rel="external nofollow noopener noreferrer" target="_blank">《netty 源码分析之 pipeline(二)》</a></li>
</ul>
<p>感觉上来说，Inbound 事件的传播，比起 Outbound 事件的传播，会相对“绕”一点点。简化来说，实际大概是如下：</p>
<figure class="highlight"><table><tbody><tr><td class="code"><pre><span class="line">Unsafe 开始 =&gt; DefaultChannelPipeline#fireChannelActive</span><br><span class="line"></span><br><span class="line">=&gt; HeadContext#invokeChannelActive =&gt; DefaultChannelHandlerContext01#fireChannelActive</span><br><span class="line"></span><br><span class="line">=&gt; DefaultChannelHandlerContext01#invokeChannelActive =&gt; DefaultChannelHandlerContext02#fireChannelActive</span><br><span class="line">...</span><br><span class="line">=&gt; DefaultChannelHandlerContext99#fireChannelActive =&gt; TailContext#fireChannelActive</span><br><span class="line"></span><br><span class="line">=&gt; TailContext#invokeChannelActive =&gt; 结束</span><br></pre></td></tr></tbody></table></figure>
<p>笔者觉得可能解释的也有点“绕”，如果不理解或者有地方写的有误解，欢迎来叨叨，以便我们能一起优化这篇文章。</p>


</div>
<!--
<footer class="article-footer">
<a data-url="http://svip.iocoder.cn/Netty/Pipeline-5-inbound/" data-id="ck4pl3fp000dofgcfnr81c0i4" class="article-share-link">分享</a>



</footer>
-->
</div>