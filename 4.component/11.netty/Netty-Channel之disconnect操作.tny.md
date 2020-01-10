<div class="article-inner">


<header class="article-header">


<h1 class="article-title" itemprop="name">
精尽 Netty 源码解析 —— Channel（八）之 disconnect 操作
</h1>


</header>

<div class="article-entry" itemprop="articleBody">

<!-- Table of Contents -->

<h1 id="1-概述"><a href="#1-概述" class="headerlink" title="1. 概述"></a>1. 概述</h1><p>本文分享 Netty NIO Channel <strong>客户端</strong>断开连接( <strong>disconnect</strong> )操作的过程。</p>
<p>在看 Netty NIO Channel 对 <code>#disconnect(ChannelPromise promise)</code> 方法的实现代码之前，我们先来看看 Java <strong>原生</strong> NIO SocketChannel 的 <code>#disconnect()</code> 方法。</p>
<ul>
<li>结果，结果，结果，翻了半天，只看到 NIO SocketChannel 的父类 AbstractInterruptibleChannel 中，有 <code>#close()</code> 方法，而找不到 <code>#disconnect()</code> 方法。这个是啥情况？</li>
<li>我们又去翻了 Java <strong>原生</strong> UDP DatagramSocket 类，结果找到了 <code>#connect()</code> 方法。这个又是啥情况？</li>
</ul>
<p>不卖关子了，直接说结论啦：</p>
<ul>
<li>Java <strong>原生</strong> NIO SocketChannel <strong>不存在</strong>，当调用 Netty <code>NioSocketChannel#disconnect(ChannelPromise promise)</code> 时，会自动转换成 <strong>close</strong> 操作，即 <a href="http://svip.iocoder.cn/Netty/Channel-7-close/">《精尽 Netty 源码解析 —— Channel（七）之 close 操作》</a> 。</li>
<li>实际上， <code>Channel#disconnect(ChannelPromise promise)</code> 方法，是 Netty 为 UDP 设计的。</li>
</ul>
<h1 id="2-NioSocketChannel"><a href="#2-NioSocketChannel" class="headerlink" title="2. NioSocketChannel"></a>2. NioSocketChannel</h1><p>通过 <code>NioSocketChannel#disconnect()</code> 方法，应用程序里可以主动关闭 NioSocketChannel 通道。代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="meta">@Override</span></span><br><span class="line"><span class="function"><span class="keyword">public</span> ChannelFuture <span class="title">disconnect</span><span class="params">()</span> </span>{</span><br><span class="line">    <span class="keyword">return</span> pipeline.disconnect();</span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li>NioSocketChannel 继承 AbstractChannel 抽象类，所以 <code>#disconnect()</code> 方法实际是 AbstractChannel 实现的。</li>
<li>在方法内部，会调用对应的 <code>ChannelPipeline#disconnect()</code> 方法，将 disconnect 事件在 pipeline 上传播。</li>
</ul>
<h1 id="3-DefaultChannelPipeline"><a href="#3-DefaultChannelPipeline" class="headerlink" title="3. DefaultChannelPipeline"></a>3. DefaultChannelPipeline</h1><p><code>DefaultChannelPipeline#disconnect()</code> 方法，代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="meta">@Override</span></span><br><span class="line"><span class="function"><span class="keyword">public</span> <span class="keyword">final</span> ChannelPipeline <span class="title">disconnect</span><span class="params">()</span> </span>{</span><br><span class="line">    tail.disconnect();</span><br><span class="line">    <span class="keyword">return</span> <span class="keyword">this</span>;</span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li>在方法内部，会调用 <code>TailContext#disconnect()</code> 方法，将 flush 事件在 pipeline 中，从尾节点向头节点传播。详细解析，见 <a href="#">「4. TailContext」</a> 。</li>
</ul>
<h1 id="4-TailContext"><a href="#4-TailContext" class="headerlink" title="4. TailContext"></a>4. TailContext</h1><p>TailContext 对 <code>#flush()</code> 方法的实现，是从 AbstractChannelHandlerContext 抽象类继承，代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="meta">@Override</span></span><br><span class="line"><span class="function"><span class="keyword">public</span> ChannelFuture <span class="title">disconnect</span><span class="params">()</span> </span>{</span><br><span class="line">    <span class="keyword">return</span> disconnect(newPromise());</span><br><span class="line">}</span><br><span class="line"></span><br><span class="line"><span class="meta">@Override</span></span><br><span class="line"><span class="function"><span class="keyword">public</span> ChannelFuture <span class="title">disconnect</span><span class="params">(<span class="keyword">final</span> ChannelPromise promise)</span> </span>{</span><br><span class="line">    <span class="comment">// 判断是否为合法的 Promise 对象</span></span><br><span class="line">    <span class="keyword">if</span> (isNotValidPromise(promise, <span class="keyword">false</span>)) {</span><br><span class="line">        <span class="comment">// cancelled</span></span><br><span class="line">        <span class="keyword">return</span> promise;</span><br><span class="line">    }</span><br><span class="line"></span><br><span class="line">    <span class="keyword">final</span> AbstractChannelHandlerContext next = findContextOutbound();</span><br><span class="line">    EventExecutor executor = next.executor();</span><br><span class="line">    <span class="keyword">if</span> (executor.inEventLoop()) {</span><br><span class="line">        <span class="comment">// &lt;1&gt; 如果没有 disconnect 操作，则执行 close 事件在 pipeline 上</span></span><br><span class="line">        <span class="comment">// Translate disconnect to close if the channel has no notion of disconnect-reconnect.</span></span><br><span class="line">        <span class="comment">// So far, UDP/IP is the only transport that has such behavior.</span></span><br><span class="line">        <span class="keyword">if</span> (!channel().metadata().hasDisconnect()) {</span><br><span class="line">            next.invokeClose(promise);</span><br><span class="line">        <span class="comment">// 如果有 disconnect 操作，则执行 disconnect 事件在 pipeline 上</span></span><br><span class="line">        } <span class="keyword">else</span> {</span><br><span class="line">            next.invokeDisconnect(promise);</span><br><span class="line">        }</span><br><span class="line">    } <span class="keyword">else</span> {</span><br><span class="line">        safeExecute(executor, <span class="keyword">new</span> Runnable() {</span><br><span class="line">            <span class="meta">@Override</span></span><br><span class="line">            <span class="function"><span class="keyword">public</span> <span class="keyword">void</span> <span class="title">run</span><span class="params">()</span> </span>{</span><br><span class="line">                <span class="comment">// &lt;1&gt; 如果没有 disconnect 操作，则执行 close 事件在 pipeline 上</span></span><br><span class="line">                <span class="keyword">if</span> (!channel().metadata().hasDisconnect()) {</span><br><span class="line">                    next.invokeClose(promise);</span><br><span class="line">                    <span class="comment">// 如果有 disconnect 操作，则执行 disconnect 事件在 pipeline 上</span></span><br><span class="line">                } <span class="keyword">else</span> {</span><br><span class="line">                    next.invokeDisconnect(promise);</span><br><span class="line">                }</span><br><span class="line">            }</span><br><span class="line">        }, promise, <span class="keyword">null</span>);</span><br><span class="line">    }</span><br><span class="line">    <span class="keyword">return</span> promise;</span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li>在 <code>&lt;1&gt;</code> 处，调用 <code>ChannelMetadata#hasDisconnect()</code> 方法，判断 Channel <strong>是否支持</strong> disconnect 操作。<ul>
<li>如果支持，则<strong>转换</strong>执行 close 事件在 pipeline 上。后续的逻辑，就是 <a href="http://svip.iocoder.cn/Netty/Channel-7-close/">《精尽 Netty 源码解析 —— Channel（七）之 close 操作》</a> 。</li>
<li>如果不支持，则<strong>保持</strong>执行 disconnect 事件在 pipeline 上。 </li>
</ul>
</li>
<li>支持 disconnect 操作的 Netty Channel 实现类有：<a href="http://static2.iocoder.cn/images/Netty/2018_07_22/01.png" title="支持" class="fancybox" rel="article0"><img src="http://static2.iocoder.cn/images/Netty/2018_07_22/01.png" alt="支持"></a><span class="caption">支持</span><ul>
<li>和文头，我们提到的，只有 Java <strong>原生</strong> UDP DatagramSocket 支持是一致的。从 <code>So far, UDP/IP is the only transport that has such behavior.</code> 的英文注释，也能证实这一点。</li>
</ul>
</li>
<li>不支持 disconnect 操作的 Netty Channel 实现类有：<a href="http://static2.iocoder.cn/images/Netty/2018_07_22/02.png" title="不支持" class="fancybox" rel="article0"><img src="http://static2.iocoder.cn/images/Netty/2018_07_22/02.png" alt="不支持"></a><span class="caption">不支持</span><ul>
<li>和文头，我们提到的，只有 Java <strong>原生</strong> NIO SocketChannel 不支持是一致的。</li>
</ul>
</li>
</ul>
<p>因为本系列，暂时不分享 UDP 相关的内容，所以对“执行 disconnect 事件在 pipeline 上”就不解析了。</p>
<h1 id="666-彩蛋"><a href="#666-彩蛋" class="headerlink" title="666. 彩蛋"></a>666. 彩蛋</h1><p>水更一篇，本来以为 Netty NIO Channel 的 disconnect 操作是个<strong>骚</strong>操作。</p>


</div>
<!--
<footer class="article-footer">
<a data-url="http://svip.iocoder.cn/Netty/Channel-8-disconnect/" data-id="ck4pl3fp200dtfgcff5rpwfat" class="article-share-link">分享</a>



</footer>
-->
</div>