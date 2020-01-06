<div class="article-inner">


<header class="article-header">


<h1 class="article-title" itemprop="name">
精尽 Netty 源码解析 —— ChannelPipeline（六）之异常事件的传播
</h1>


</header>

<div class="article-entry" itemprop="articleBody">

<!-- Table of Contents -->

<h1 id="1-概述"><a href="#1-概述" class="headerlink" title="1. 概述"></a>1. 概述</h1><p>在 <a href="http://svip.iocoder.cn/Netty/Pipeline-4-outbound/">《精尽 Netty 源码解析 —— ChannelPipeline（四）之 Outbound 事件的传播》</a> 和 <a href="http://svip.iocoder.cn/Netty/Pipeline-5-inbound/">《精尽 Netty 源码解析 —— ChannelPipeline（五）之 Inbound 事件的传播》</a> 中，我们看到 Outbound 和 Inbound 事件在 pipeline 中的传播逻辑。但是，无可避免，传播的过程中，可能会发生异常，那是怎么处理的呢？</p>
<p>本文，我们就来分享分享这块。</p>
<h1 id="2-notifyOutboundHandlerException"><a href="#2-notifyOutboundHandlerException" class="headerlink" title="2. notifyOutboundHandlerException"></a>2. notifyOutboundHandlerException</h1><p>我们以 Outbound 事件中的 <strong>bind</strong> 举例子，代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="comment">// AbstractChannelHandlerContext.java</span></span><br><span class="line"></span><br><span class="line"><span class="function"><span class="keyword">private</span> <span class="keyword">void</span> <span class="title">invokeBind</span><span class="params">(SocketAddress localAddress, ChannelPromise promise)</span> </span>{</span><br><span class="line">    <span class="keyword">if</span> (invokeHandler()) { <span class="comment">// 判断是否符合的 ChannelHandler</span></span><br><span class="line">        <span class="keyword">try</span> {</span><br><span class="line">            <span class="comment">// 调用该 ChannelHandler 的 bind 方法 &lt;1&gt;</span></span><br><span class="line">            ((ChannelOutboundHandler) handler()).bind(<span class="keyword">this</span>, localAddress, promise);</span><br><span class="line">        } <span class="keyword">catch</span> (Throwable t) {</span><br><span class="line">            notifyOutboundHandlerException(t, promise); <span class="comment">// 通知 Outbound 事件的传播，发生异常 &lt;2&gt;</span></span><br><span class="line">        }</span><br><span class="line">    } <span class="keyword">else</span> {</span><br><span class="line">        <span class="comment">// 跳过，传播 Outbound 事件给下一个节点</span></span><br><span class="line">        bind(localAddress, promise);</span><br><span class="line">    }</span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li>在 <code>&lt;1&gt;</code> 处，调用 <code>ChannelOutboundHandler#bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise)</code> 方法<strong>发生异常</strong>时，会在 <code>&lt;2&gt;</code> 处调用 <code>AbstractChannelHandlerContext#notifyOutboundHandlerException(Throwable cause, ChannelPromise promise)</code> 方法，通知 Outbound 事件的传播，发生异常。</li>
<li>其他 Outbound 事件，大体的代码也是和 <code>#invokeBind(SocketAddress localAddress, ChannelPromise promise)</code> 是一致的。如下图所示：<a href="http://static2.iocoder.cn/images/Netty/2018_06_16/01.png" title="类图" class="fancybox" rel="article0"><img src="http://static2.iocoder.cn/images/Netty/2018_06_16/01.png" alt="类图"></a><span class="caption">类图</span></li>
</ul>
<hr>
<p><code>AbstractChannelHandlerContext#notifyOutboundHandlerException(Throwable cause, ChannelPromise promise)</code> 方法，通知 Outbound 事件的传播，发生异常。代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="function"><span class="keyword">private</span> <span class="keyword">static</span> <span class="keyword">void</span> <span class="title">notifyOutboundHandlerException</span><span class="params">(Throwable cause, ChannelPromise promise)</span> </span>{</span><br><span class="line">    <span class="comment">// Only log if the given promise is not of type VoidChannelPromise as tryFailure(...) is expected to return</span></span><br><span class="line">    <span class="comment">// false.</span></span><br><span class="line">    PromiseNotificationUtil.tryFailure(promise, cause, promise <span class="keyword">instanceof</span> VoidChannelPromise ? <span class="keyword">null</span> : logger);</span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li><p>在方法内部，会调用 <code>PromiseNotificationUtil#tryFailure(Promise&lt;?&gt; p, Throwable cause, InternalLogger logger)</code> 方法，通知 bind 事件对应的 Promise 对应的监听者们。代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="function"><span class="keyword">public</span> <span class="keyword">static</span> <span class="keyword">void</span> <span class="title">tryFailure</span><span class="params">(Promise&lt;?&gt; p, Throwable cause, InternalLogger logger)</span> </span>{</span><br><span class="line">    <span class="keyword">if</span> (!p.tryFailure(cause) &amp;&amp; logger != <span class="keyword">null</span>) {</span><br><span class="line">        Throwable err = p.cause();</span><br><span class="line">        <span class="keyword">if</span> (err == <span class="keyword">null</span>) {</span><br><span class="line">            logger.warn(<span class="string">"Failed to mark a promise as failure because it has succeeded already: {}"</span>, p, cause);</span><br><span class="line">        } <span class="keyword">else</span> {</span><br><span class="line">            logger.warn(</span><br><span class="line">                    <span class="string">"Failed to mark a promise as failure because it has failed already: {}, unnotified cause: {}"</span>,</span><br><span class="line">                    p, ThrowableUtil.stackTraceToString(err), cause);</span><br><span class="line">        }</span><br><span class="line">    }</span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li><p>以 bind 事件来举一个监听器的例子。代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line">ChannelFuture f = b.bind(PORT).addListener(<span class="keyword">new</span> ChannelFutureListener() { <span class="comment">// &lt;1&gt; 监听器就是我！</span></span><br><span class="line">    <span class="meta">@Override</span></span><br><span class="line">    <span class="function"><span class="keyword">public</span> <span class="keyword">void</span> <span class="title">operationComplete</span><span class="params">(ChannelFuture future)</span> <span class="keyword">throws</span> Exception </span>{</span><br><span class="line">        System.out.println(<span class="string">"异常："</span> + future.casue());</span><br><span class="line">    }</span><br><span class="line">}).sync();</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li><code>&lt;1&gt;</code> 处的监听器，就是示例。当发生异常时，就会通知该监听器，对该异常做进一步<strong>自定义</strong>的处理。<strong>也就是说，该异常不会在 pipeline 中传播</strong>。</li>
</ul>
</li>
<li><p>我们再来看看怎么通知监听器的源码实现。调用 <code>DefaultPromise#tryFailure(Throwable cause)</code> 方法，通知 Promise 的监听器们，发生了异常。代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="meta">@Override</span></span><br><span class="line"><span class="function"><span class="keyword">public</span> <span class="keyword">boolean</span> <span class="title">tryFailure</span><span class="params">(Throwable cause)</span> </span>{</span><br><span class="line">    <span class="keyword">if</span> (setFailure0(cause)) { <span class="comment">// 设置 Promise 的结果</span></span><br><span class="line">        <span class="comment">// 通知监听器</span></span><br><span class="line">        notifyListeners();</span><br><span class="line">        <span class="comment">// 返回成功</span></span><br><span class="line">        <span class="keyword">return</span> <span class="keyword">true</span>;</span><br><span class="line">    }</span><br><span class="line">    <span class="comment">// 返回失败</span></span><br><span class="line">    <span class="keyword">return</span> <span class="keyword">false</span>;</span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li>若 <code>DefaultPromise#setFailure0(Throwable cause)</code> 方法，设置 Promise 的结果为方法传入的异常。但是有可能会传递失败，例如说，Promise 已经被设置了结果。</li>
<li>如果该方法返回 <code>false</code> 通知 Promise 失败，那么 <code>PromiseNotificationUtil#tryFailure(Promise&lt;?&gt; p, Throwable cause, InternalLogger logger)</code> 方法的后续，就会使用 <code>logger</code> 打印错误日志。</li>
</ul>
</li>
</ul>
</li>
</ul>
<h1 id="3-notifyHandlerException"><a href="#3-notifyHandlerException" class="headerlink" title="3. notifyHandlerException"></a>3. notifyHandlerException</h1><p>我们以 Inbound 事件中的 <strong>fireChannelActive</strong> 举例子，代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="function"><span class="keyword">private</span> <span class="keyword">void</span> <span class="title">invokeChannelActive</span><span class="params">()</span> </span>{</span><br><span class="line">    <span class="keyword">if</span> (invokeHandler()) { <span class="comment">// 判断是否符合的 ChannelHandler</span></span><br><span class="line">        <span class="keyword">try</span> {</span><br><span class="line">            <span class="comment">// 调用该 ChannelHandler 的 Channel active 方法 &lt;1&gt;</span></span><br><span class="line">            ((ChannelInboundHandler) handler()).channelActive(<span class="keyword">this</span>);</span><br><span class="line">        } <span class="keyword">catch</span> (Throwable t) {</span><br><span class="line">            notifyHandlerException(t);  <span class="comment">// 通知 Inbound 事件的传播，发生异常 &lt;2&gt;</span></span><br><span class="line">        }</span><br><span class="line">    } <span class="keyword">else</span> {</span><br><span class="line">        <span class="comment">// 跳过，传播 Inbound 事件给下一个节点</span></span><br><span class="line">        fireChannelActive();</span><br><span class="line">    }</span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li>在 <code>&lt;1&gt;</code> 处，调用 <code>ChannelInboundHandler#channelActive(ChannelHandlerContext ctx)</code> 方法<strong>发生异常</strong>时，会在 <code>&lt;2&gt;</code> 处调用 <code>AbstractChannelHandlerContext#notifyHandlerException(Throwable cause)</code> 方法，通知 Inbound 事件的传播，发生异常。</li>
<li>其他 Inbound 事件，大体的代码也是和 <code>#invokeChannelActive()</code> 是一致的。如下图所示：<a href="http://static2.iocoder.cn/images/Netty/2018_06_16/02.png" title="类图" class="fancybox" rel="article0"><img src="http://static2.iocoder.cn/images/Netty/2018_06_16/02.png" alt="类图"></a><span class="caption">类图</span><ul>
<li>😈 <strong>注意，笔者在写的时候，突然发现 Outbound 事件中的 read 和 flush 的异常处理方式和 Inbound 事件是一样的</strong>。</li>
<li>😈 <strong>注意，笔者在写的时候，突然发现 Outbound 事件中的 read 和 flush 的异常处理方式和 Inbound 事件是一样的</strong>。</li>
<li>😈 <strong>注意，笔者在写的时候，突然发现 Outbound 事件中的 read 和 flush 的异常处理方式和 Inbound 事件是一样的</strong>。</li>
</ul>
</li>
</ul>
<hr>
<p><code>AbstractChannelHandlerContext#notifyHandlerException(Throwable cause)</code> 方法，通知 Inbound 事件的传播，发生异常。代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="function"><span class="keyword">private</span> <span class="keyword">void</span> <span class="title">notifyHandlerException</span><span class="params">(Throwable cause)</span> </span>{</span><br><span class="line">    <span class="comment">// &lt;1&gt; 如果是在 `ChannelHandler#exceptionCaught(ChannelHandlerContext ctx, Throwable cause)` 方法中，仅打印错误日志。否则会形成死循环。</span></span><br><span class="line">    <span class="keyword">if</span> (inExceptionCaught(cause)) {</span><br><span class="line">        <span class="keyword">if</span> (logger.isWarnEnabled()) {</span><br><span class="line">            logger.warn(</span><br><span class="line">                    <span class="string">"An exception was thrown by a user handler "</span> +</span><br><span class="line">                            <span class="string">"while handling an exceptionCaught event"</span>, cause);</span><br><span class="line">        }</span><br><span class="line">        <span class="keyword">return</span>;</span><br><span class="line">    }</span><br><span class="line"></span><br><span class="line">    <span class="comment">// &lt;2&gt; 在 pipeline 中，传播 Exception Caught 事件</span></span><br><span class="line">    invokeExceptionCaught(cause);</span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li><p><code>&lt;1&gt;</code> 处，调用 <code>AbstractChannelHandlerContext#inExceptionCaught(Throwable cause)</code> 方法，如果是在 <code>ChannelHandler#exceptionCaught(ChannelHandlerContext ctx, Throwable cause)</code> 方法中，<strong>发生异常</strong>，仅打印错误日志，<strong>并 <code>return</code> 返回</strong> 。否则会形成死循环。代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="function"><span class="keyword">private</span> <span class="keyword">static</span> <span class="keyword">boolean</span> <span class="title">inExceptionCaught</span><span class="params">(Throwable cause)</span> </span>{</span><br><span class="line">    <span class="keyword">do</span> {</span><br><span class="line">        StackTraceElement[] trace = cause.getStackTrace();</span><br><span class="line">        <span class="keyword">if</span> (trace != <span class="keyword">null</span>) {</span><br><span class="line">            <span class="keyword">for</span> (StackTraceElement t : trace) { <span class="comment">// 循环 StackTraceElement</span></span><br><span class="line">                <span class="keyword">if</span> (t == <span class="keyword">null</span>) {</span><br><span class="line">                    <span class="keyword">break</span>;</span><br><span class="line">                }</span><br><span class="line">                <span class="keyword">if</span> (<span class="string">"exceptionCaught"</span>.equals(t.getMethodName())) { <span class="comment">// 通过方法名判断</span></span><br><span class="line">                    <span class="keyword">return</span> <span class="keyword">true</span>;</span><br><span class="line">                }</span><br><span class="line">            }</span><br><span class="line">        }</span><br><span class="line">        cause = cause.getCause();</span><br><span class="line">    } <span class="keyword">while</span> (cause != <span class="keyword">null</span>); <span class="comment">// 循环异常的 cause() ，直到到没有</span></span><br><span class="line">    </span><br><span class="line">    <span class="keyword">return</span> <span class="keyword">false</span>;</span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li>通过 StackTraceElement 的方法名来判断，是不是 <code>ChannelHandler#exceptionCaught(ChannelHandlerContext ctx, Throwable cause)</code> 方法。</li>
</ul>
</li>
<li><p><code>&lt;2&gt;</code> 处，调用 <code>AbstractChannelHandlerContext#invokeExceptionCaught(Throwable cause)</code> 方法，在 pipeline 中，传递 Exception Caught 事件。在下文中，我们会看到，和 <a href="http://svip.iocoder.cn/Netty/Pipeline-5-inbound/">《精尽 Netty 源码解析 —— ChannelPipeline（五）之 Inbound 事件的传播》</a> 的逻辑( <code>AbstractChannelHandlerContext#invokeChannelActive()</code> )是<strong>一致</strong>的。</p>
<ul>
<li>比较特殊的是，Exception Caught 事件在 pipeline 的起始节点，不是 <code>head</code> 头节点，而是<strong>发生异常的当前节点开始</strong>。怎么理解好呢？对于在 pipeline 上传播的 Inbound <strong>xxx</strong> 事件，在发生异常后，转化成 <strong>Exception Caught</strong> 事件，继续从当前节点，继续向下传播。</li>
<li><p>如果 <strong>Exception Caught</strong> 事件在 pipeline 中的传播过程中，一直没有处理掉该异常的节点，最终会到达尾节点 <code>tail</code> ，它对 <code>#exceptionCaught(ChannelHandlerContext ctx, Throwable cause)</code> 方法的实现，代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="meta">@Override</span></span><br><span class="line"><span class="function"><span class="keyword">public</span> <span class="keyword">void</span> <span class="title">exceptionCaught</span><span class="params">(ChannelHandlerContext ctx, Throwable cause)</span> <span class="keyword">throws</span> Exception </span>{</span><br><span class="line">    onUnhandledInboundException(cause);</span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li><p>在方法内部，会调用 <code>DefaultChannelPipeline#onUnhandledInboundException(Throwable cause)</code> 方法，代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="comment">/**</span></span><br><span class="line"><span class="comment"> * Called once a {<span class="doctag">@link</span> Throwable} hit the end of the {<span class="doctag">@link</span> ChannelPipeline} without been handled by the user</span></span><br><span class="line"><span class="comment"> * in {<span class="doctag">@link</span> ChannelHandler#exceptionCaught(ChannelHandlerContext, Throwable)}.</span></span><br><span class="line"><span class="comment"> */</span></span><br><span class="line"><span class="function"><span class="keyword">protected</span> <span class="keyword">void</span> <span class="title">onUnhandledInboundException</span><span class="params">(Throwable cause)</span> </span>{</span><br><span class="line">    <span class="keyword">try</span> {</span><br><span class="line">        logger.warn(</span><br><span class="line">                <span class="string">"An exceptionCaught() event was fired, and it reached at the tail of the pipeline. "</span> +</span><br><span class="line">                        <span class="string">"It usually means the last handler in the pipeline did not handle the exception."</span>,</span><br><span class="line">                cause);</span><br><span class="line">    } <span class="keyword">finally</span> {</span><br><span class="line">        ReferenceCountUtil.release(cause);</span><br><span class="line">    }</span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li>打印<strong>告警</strong>日志，并调用 <code>ReferenceCountUtil#release(Throwable)</code> 方法，释放需要释放的资源。</li>
<li>从英文注释中，我们也可以看到，这种情况出现在<strong>使用者</strong>未定义合适的 ChannelHandler 处理这种异常，所以对于这种情况下，<code>tail</code> 节点只好打印<strong>告警</strong>日志。</li>
<li>实际使用时，笔者建议胖友一定要定义 ExceptionHandler ，能够处理掉所有的异常，而不要使用到 <code>tail</code> 节点的异常处理。😈</li>
<li><p>好基友【闪电侠】对尾节点 <code>tail</code> 做了很赞的总结</p>
<blockquote>
<p>总结一下，tail 节点的作用就是结束事件传播，并且对一些重要的事件做一些善意提醒</p>
</blockquote>
</li>
</ul>
</li>
</ul>
</li>
</ul>
</li>
</ul>
<h1 id="666-彩蛋"><a href="#666-彩蛋" class="headerlink" title="666. 彩蛋"></a>666. 彩蛋</h1><p>推荐阅读文章：</p>
<ul>
<li>闪电侠 <a href="https://www.jianshu.com/p/087b7e9a27a2" rel="external nofollow noopener noreferrer" target="_blank">《netty 源码分析之 pipeline(二)》</a></li>
</ul>


</div>
<!--
<footer class="article-footer">
<a data-url="http://svip.iocoder.cn/Netty/ChannelPipeline-6-exception/" data-id="ck4pl3foz00dmfgcf1k34myrj" class="article-share-link">分享</a>



</footer>
-->
</div>