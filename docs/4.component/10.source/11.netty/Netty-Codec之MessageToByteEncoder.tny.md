<div class="article-inner">

<header class="article-header">

<h1 class="article-title" itemprop="name">
精尽 Netty 源码解析 —— Codec 之 MessageToByteEncoder
</h1>

</header>

<div class="article-entry" itemprop="articleBody">

<!-- Table of Contents -->

<h1 id="1-概述"><a href="#1-概述" class="headerlink" title="1. 概述"></a>1. 概述</h1><p>本文，我们来分享 MessageToByteEncoder 部分的内容。</p>
<p>MessageToByteEncoder 负责将消息<strong>编码</strong>成字节。核心类图如下：</p>
<p><a href="http://static2.iocoder.cn/images/Netty/2018_12_18/01.png" title="核心类图" class="fancybox" rel="article0"><img src="http://static2.iocoder.cn/images/Netty/2018_12_18/01.png" alt="核心类图"></a><span class="caption">核心类图</span></p>
<p>ByteToMessageDecoder 本身是个<strong>抽象</strong>类，其下有多个子类，笔者简单整理成两类，可能不全哈：</p>
<ul>
<li><strong>蓝框</strong>部分，将消息<strong>压缩</strong>，主要涉及相关压缩算法，例如：GZip、BZip 等等。<ul>
<li>它要求消息类型是 ByteBuf ，将已经转化好的字节流，进一步压缩。</li>
</ul>
</li>
<li><strong>黄框</strong>部分，将消息使用<strong>指定序列化方式</strong>序列化成字节。例如：JSON、XML 等等。<ul>
<li>因为 Netty 没有内置的 JSON、XML 等相关的类库，所以不好提供类似 JSONEncoder 或 XMLEncoder ，所以图中笔者就使用 <code>netty-example</code> 提供的 NumberEncoder 。</li>
</ul>
</li>
</ul>
<p>在 <a href="http://svip.iocoder.cn/Netty/Codec-1-1-ByteToMessageDecoder-core-impl">《精尽 Netty 源码解析 —— Codec 之 ByteToMessageDecoder（一）Cumulator》</a> 中，我们提到<strong>粘包拆包</strong>的现象，所以在实际使用 Netty 编码消息时，还需要有为了解决<strong>粘包拆包</strong>的 Encoder 实现类，例如：换行、定长等等方式。关于这块内容，胖友可以看看 <a href="https://www.codetd.com/article/1539061" rel="external nofollow noopener noreferrer" target="_blank">《netty使用MessageToByteEncoder 自定义协议》</a> 。</p>
<h1 id="2-MessageToByteEncoder"><a href="#2-MessageToByteEncoder" class="headerlink" title="2. MessageToByteEncoder"></a>2. MessageToByteEncoder</h1><p><code>io.netty.handler.codec.MessageToByteEncoder</code> ，继承 ChannelOutboundHandlerAdapter 类，负责将消息<strong>编码</strong>成字节，支持<strong>匹配指定类型</strong>的消息。</p>
<h2 id="2-1-构造方法"><a href="#2-1-构造方法" class="headerlink" title="2.1 构造方法"></a>2.1 构造方法</h2><figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="keyword">public</span> <span class="keyword">abstract</span> <span class="class"><span class="keyword">class</span> <span class="title">MessageToByteEncoder</span>&lt;<span class="title">I</span>&gt; <span class="keyword">extends</span> <span class="title">ChannelOutboundHandlerAdapter</span> </span>{</span><br><span class="line"></span><br><span class="line">    <span class="comment">/**</span></span><br><span class="line"><span class="comment">     * 类型匹配器</span></span><br><span class="line"><span class="comment">     */</span></span><br><span class="line">    <span class="keyword">private</span> <span class="keyword">final</span> TypeParameterMatcher matcher;</span><br><span class="line">    <span class="comment">/**</span></span><br><span class="line"><span class="comment">     * 是否偏向使用 Direct 内存</span></span><br><span class="line"><span class="comment">     */</span></span><br><span class="line">    <span class="keyword">private</span> <span class="keyword">final</span> <span class="keyword">boolean</span> preferDirect;</span><br><span class="line"></span><br><span class="line">    <span class="function"><span class="keyword">protected</span> <span class="title">MessageToByteEncoder</span><span class="params">()</span> </span>{</span><br><span class="line">        <span class="keyword">this</span>(<span class="keyword">true</span>);</span><br><span class="line">    }</span><br><span class="line"></span><br><span class="line">    <span class="function"><span class="keyword">protected</span> <span class="title">MessageToByteEncoder</span><span class="params">(Class&lt;? extends I&gt; outboundMessageType)</span> </span>{</span><br><span class="line">        <span class="keyword">this</span>(outboundMessageType, <span class="keyword">true</span>);</span><br><span class="line">    }</span><br><span class="line"></span><br><span class="line">    <span class="function"><span class="keyword">protected</span> <span class="title">MessageToByteEncoder</span><span class="params">(<span class="keyword">boolean</span> preferDirect)</span> </span>{</span><br><span class="line">        <span class="comment">// &lt;1&gt; 获得 matcher</span></span><br><span class="line">        matcher = TypeParameterMatcher.find(<span class="keyword">this</span>, MessageToByteEncoder.class, <span class="string">"I"</span>);</span><br><span class="line">        <span class="keyword">this</span>.preferDirect = preferDirect;</span><br><span class="line">    }</span><br><span class="line"></span><br><span class="line">    <span class="function"><span class="keyword">protected</span> <span class="title">MessageToByteEncoder</span><span class="params">(Class&lt;? extends I&gt; outboundMessageType, <span class="keyword">boolean</span> preferDirect)</span> </span>{</span><br><span class="line">        <span class="comment">// &lt;2&gt; 获得 matcher</span></span><br><span class="line">        matcher = TypeParameterMatcher.get(outboundMessageType);</span><br><span class="line">        <span class="keyword">this</span>.preferDirect = preferDirect;</span><br><span class="line">    }</span><br><span class="line">    </span><br><span class="line">    <span class="comment">// ... 省略其他无关代码</span></span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li><code>matcher</code> 属性，有<strong>两种</strong>方式赋值。<ul>
<li>【常用】<code>&lt;1&gt;</code> 处，使用类的 <code>I</code> 泛型对应的 TypeParameterMatcher 类型匹配器。</li>
<li><code>&lt;2&gt;</code> 处，使用 <code>inboundMessageType</code> 参数对应的 TypeParameterMatcher 类型匹配器。</li>
<li>在大多数情况下，我们不太需要特别详细的了解 <code>io.netty.util.internal.TypeParameterMatcher</code> 的代码实现，感兴趣的胖友可以自己看看 <a href="http://donald-draper.iteye.com/blog/2387772" rel="external nofollow noopener noreferrer" target="_blank">《netty 简单Inbound通道处理器（SimpleChannelInboundHandler）》</a> 的 <a href="#">「TypeParameterMatcher」</a> 部分。</li>
</ul>
</li>
<li><code>preferDirect</code> 属性，是否偏向使用 Direct 内存。默认为 <code>true</code> 。</li>
</ul>
<h2 id="2-2-acceptInboundMessage"><a href="#2-2-acceptInboundMessage" class="headerlink" title="2.2 acceptInboundMessage"></a>2.2 acceptInboundMessage</h2><p><code>#acceptInboundMessage(Object msg)</code> 方法，判断消息是否匹配。代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="comment">/**</span></span><br><span class="line"><span class="comment"> * Returns {<span class="doctag">@code</span> true} if the given message should be handled. If {<span class="doctag">@code</span> false} it will be passed to the next</span></span><br><span class="line"><span class="comment"> * {<span class="doctag">@link</span> ChannelInboundHandler} in the {<span class="doctag">@link</span> ChannelPipeline}.</span></span><br><span class="line"><span class="comment"> */</span></span><br><span class="line"><span class="function"><span class="keyword">public</span> <span class="keyword">boolean</span> <span class="title">acceptInboundMessage</span><span class="params">(Object msg)</span> </span>{</span><br><span class="line">    <span class="keyword">return</span> matcher.match(msg);</span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<p>一般情况下，<code>matcher</code> 的类型是 ReflectiveMatcher( 它是 TypeParameterMatcher 的内部类 )。代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="keyword">private</span> <span class="keyword">static</span> <span class="keyword">final</span> <span class="class"><span class="keyword">class</span> <span class="title">ReflectiveMatcher</span> <span class="keyword">extends</span> <span class="title">TypeParameterMatcher</span> </span>{</span><br><span class="line">    </span><br><span class="line">    <span class="comment">/**</span></span><br><span class="line"><span class="comment">     * 类型</span></span><br><span class="line"><span class="comment">     */</span></span><br><span class="line">    <span class="keyword">private</span> <span class="keyword">final</span> Class&lt;?&gt; type;</span><br><span class="line">    </span><br><span class="line">    ReflectiveMatcher(Class&lt;?&gt; type) {</span><br><span class="line">        <span class="keyword">this</span>.type = type;</span><br><span class="line">    }</span><br><span class="line">    </span><br><span class="line">    <span class="meta">@Override</span></span><br><span class="line">    <span class="function"><span class="keyword">public</span> <span class="keyword">boolean</span> <span class="title">match</span><span class="params">(Object msg)</span> </span>{</span><br><span class="line">        <span class="keyword">return</span> type.isInstance(msg); <span class="comment">// &lt;1&gt;</span></span><br><span class="line">    }</span><br><span class="line">    </span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li>匹配逻辑，看 <code>&lt;1&gt;</code> 处，使用 <code>Class#isInstance(Object obj)</code> 方法。对于这个方法，如果我们定义的 <code>I</code> 泛型是个父类，那可以匹配所有的子类。例如 <code>I</code> 设置为 Object 类，那么所有消息，都可以被匹配列。</li>
</ul>
<h2 id="2-3-write"><a href="#2-3-write" class="headerlink" title="2.3 write"></a>2.3 write</h2><p><code>#write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)</code> 方法，匹配指定的消息类型，编码消息成 ByteBuf 对象，继续写到下一个节点。代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"> <span class="number">1</span>: <span class="meta">@Override</span></span><br><span class="line"> <span class="number">2</span>: <span class="function"><span class="keyword">public</span> <span class="keyword">void</span> <span class="title">write</span><span class="params">(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)</span> <span class="keyword">throws</span> Exception </span>{</span><br><span class="line"> <span class="number">3</span>:     ByteBuf buf = <span class="keyword">null</span>;</span><br><span class="line"> <span class="number">4</span>:     <span class="keyword">try</span> {</span><br><span class="line"> <span class="number">5</span>:         <span class="comment">// 判断是否为匹配的消息</span></span><br><span class="line"> <span class="number">6</span>:         <span class="keyword">if</span> (acceptOutboundMessage(msg)) {</span><br><span class="line"> <span class="number">7</span>:             <span class="meta">@SuppressWarnings</span>(<span class="string">"unchecked"</span>)</span><br><span class="line"> <span class="number">8</span>:             I cast = (I) msg;</span><br><span class="line"> <span class="number">9</span>:             <span class="comment">// 申请 buf</span></span><br><span class="line"><span class="number">10</span>:             buf = allocateBuffer(ctx, cast, preferDirect);</span><br><span class="line"><span class="number">11</span>:             <span class="comment">// 编码</span></span><br><span class="line"><span class="number">12</span>:             <span class="keyword">try</span> {</span><br><span class="line"><span class="number">13</span>:                 encode(ctx, cast, buf);</span><br><span class="line"><span class="number">14</span>:             } <span class="keyword">finally</span> {</span><br><span class="line"><span class="number">15</span>:                 <span class="comment">// 释放 msg</span></span><br><span class="line"><span class="number">16</span>:                 ReferenceCountUtil.release(cast);</span><br><span class="line"><span class="number">17</span>:             }</span><br><span class="line"><span class="number">18</span>: </span><br><span class="line"><span class="number">19</span>:             <span class="comment">// buf 可读，说明有编码到数据</span></span><br><span class="line"><span class="number">20</span>:             <span class="keyword">if</span> (buf.isReadable()) {</span><br><span class="line"><span class="number">21</span>:                 <span class="comment">// 写入 buf 到下一个节点</span></span><br><span class="line"><span class="number">22</span>:                 ctx.write(buf, promise);</span><br><span class="line"><span class="number">23</span>:             } <span class="keyword">else</span> {</span><br><span class="line"><span class="number">24</span>:                 <span class="comment">// 释放 buf</span></span><br><span class="line"><span class="number">25</span>:                 buf.release();</span><br><span class="line"><span class="number">26</span>:                 <span class="comment">// 写入 EMPTY_BUFFER 到下一个节点，为了 promise 的回调</span></span><br><span class="line"><span class="number">27</span>:                 ctx.write(Unpooled.EMPTY_BUFFER, promise);</span><br><span class="line"><span class="number">28</span>:             }</span><br><span class="line"><span class="number">29</span>: </span><br><span class="line"><span class="number">30</span>:             <span class="comment">// 置空 buf</span></span><br><span class="line"><span class="number">31</span>:             buf = <span class="keyword">null</span>;</span><br><span class="line"><span class="number">32</span>:         } <span class="keyword">else</span> {</span><br><span class="line"><span class="number">33</span>:             <span class="comment">// 提交 write 事件给下一个节点</span></span><br><span class="line"><span class="number">34</span>:             ctx.write(msg, promise);</span><br><span class="line"><span class="number">35</span>:         }</span><br><span class="line"><span class="number">36</span>:     } <span class="keyword">catch</span> (EncoderException e) {</span><br><span class="line"><span class="number">37</span>:         <span class="keyword">throw</span> e;</span><br><span class="line"><span class="number">38</span>:     } <span class="keyword">catch</span> (Throwable e) {</span><br><span class="line"><span class="number">39</span>:         <span class="keyword">throw</span> <span class="keyword">new</span> EncoderException(e);</span><br><span class="line"><span class="number">40</span>:     } <span class="keyword">finally</span> {</span><br><span class="line"><span class="number">41</span>:         <span class="comment">// 释放 buf</span></span><br><span class="line"><span class="number">42</span>:         <span class="keyword">if</span> (buf != <span class="keyword">null</span>) {</span><br><span class="line"><span class="number">43</span>:             buf.release();</span><br><span class="line"><span class="number">44</span>:         }</span><br><span class="line"><span class="number">45</span>:     }</span><br><span class="line"><span class="number">46</span>: }</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li>第 6 行：调用 <code>#acceptInboundMessage(Object msg)</code> 方法，判断是否为匹配的消息。</li>
<li><p>① 第 6 行：<strong>匹配</strong>。</p>
<ul>
<li>第 8 行：对象类型转化为 <code>I</code> 类型的消息。</li>
<li><p>第 10 行：调用 <code>#allocateBuffer(ChannelHandlerContext ctx, I msg, boolean preferDirect)</code> 方法，申请 <code>buf</code> 。代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="comment">/**</span></span><br><span class="line"><span class="comment"> * Allocate a {<span class="doctag">@link</span> ByteBuf} which will be used as argument of {<span class="doctag">@link</span> #encode(ChannelHandlerContext, I, ByteBuf)}.</span></span><br><span class="line"><span class="comment"> * Sub-classes may override this method to return {<span class="doctag">@link</span> ByteBuf} with a perfect matching {<span class="doctag">@code</span> initialCapacity}.</span></span><br><span class="line"><span class="comment"> */</span></span><br><span class="line"><span class="function"><span class="keyword">protected</span> ByteBuf <span class="title">allocateBuffer</span><span class="params">(ChannelHandlerContext ctx, @SuppressWarnings(<span class="string">"unused"</span>)</span> I msg, <span class="keyword">boolean</span> preferDirect) <span class="keyword">throws</span> Exception </span>{</span><br><span class="line">    <span class="keyword">if</span> (preferDirect) {</span><br><span class="line">        <span class="keyword">return</span> ctx.alloc().ioBuffer();</span><br><span class="line">    } <span class="keyword">else</span> {</span><br><span class="line">        <span class="keyword">return</span> ctx.alloc().heapBuffer();</span><br><span class="line">    }</span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li>x</li>
</ul>
</li>
<li><p>第 13 行：调用 <code>#encode(ChannelHandlerContext ctx, I msg, ByteBuf out)</code> 方法，编码。代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="comment">/**</span></span><br><span class="line"><span class="comment"> * Encode a message into a {<span class="doctag">@link</span> ByteBuf}. This method will be called for each written message that can be handled</span></span><br><span class="line"><span class="comment"> * by this encoder.</span></span><br><span class="line"><span class="comment"> *</span></span><br><span class="line"><span class="comment"> * <span class="doctag">@param</span> ctx           the {<span class="doctag">@link</span> ChannelHandlerContext} which this {<span class="doctag">@link</span> MessageToByteEncoder} belongs to</span></span><br><span class="line"><span class="comment"> * <span class="doctag">@param</span> msg           the message to encode</span></span><br><span class="line"><span class="comment"> * <span class="doctag">@param</span> out           the {<span class="doctag">@link</span> ByteBuf} into which the encoded message will be written</span></span><br><span class="line"><span class="comment"> * <span class="doctag">@throws</span> Exception    is thrown if an error occurs</span></span><br><span class="line"><span class="comment"> */</span></span><br><span class="line"><span class="function"><span class="keyword">protected</span> <span class="keyword">abstract</span> <span class="keyword">void</span> <span class="title">encode</span><span class="params">(ChannelHandlerContext ctx, I msg, ByteBuf out)</span> <span class="keyword">throws</span> Exception</span>;</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li>子类可以实现该方法，实现自定义的编码功能。</li>
</ul>
</li>
<li><p>第 16 行：调用 <code>ReferenceCountUtil#release(Object msg)</code> 方法，释放 <code>msg</code> 。</p>
</li>
<li>第 19 至 22 行：<code>buf</code> 可读，说明编码消息到 <code>buf</code> 中了，所以写入 <code>buf</code> 到下一个节点。😈 因为 <code>buf</code> 需要继续被下一个节点使用，所以不进行释放。</li>
<li>第 23 至 28 行：<code>buf</code> 不可读，说明无法编码，所以释放 <code>buf</code> ，并写入 <code>EMPTY_BUFFER</code> 到下一个节点，为了 promise 的回调。</li>
<li>第 31 行：置空 <code>buf</code> 为空。这里是为了防止【第 41 至 44 行】的代码，释放 <code>buf</code> 。</li>
</ul>
</li>
<li>② 第 32 行：<strong>不匹配</strong>。<ul>
<li>提交 write 事件给下一个节点。</li>
</ul>
</li>
<li>第 36 至 39 行：发生异常，抛出 EncoderException 异常。</li>
<li>第 40 至 45 行：如果中间发生异常，导致 <code>buf</code> 不为空，所以此处释放 <code>buf</code> 。</li>
</ul>
<h1 id="3-NumberEncoder"><a href="#3-NumberEncoder" class="headerlink" title="3. NumberEncoder"></a>3. NumberEncoder</h1><p><code>io.netty.example.factorial.NumberEncoder</code> ，继承 MessageToByteEncoder 抽象类，Number 类型的消息的 Encoder 实现类。代码如下：</p>
<blockquote>
<p>NumberEncoder 是 <code>netty-example</code> 模块提供的示例类，实际使用时，需要做调整。</p>
</blockquote>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="keyword">public</span> <span class="class"><span class="keyword">class</span> <span class="title">NumberEncoder</span> <span class="keyword">extends</span> <span class="title">MessageToByteEncoder</span>&lt;<span class="title">Number</span>&gt; </span>{</span><br><span class="line"></span><br><span class="line">    <span class="meta">@Override</span></span><br><span class="line">    <span class="function"><span class="keyword">protected</span> <span class="keyword">void</span> <span class="title">encode</span><span class="params">(ChannelHandlerContext ctx, Number msg, ByteBuf out)</span> </span>{</span><br><span class="line">        <span class="comment">// &lt;1&gt; 转化成 BigInteger 对象</span></span><br><span class="line">        <span class="comment">// Convert to a BigInteger first for easier implementation.</span></span><br><span class="line">        BigInteger v;</span><br><span class="line">        <span class="keyword">if</span> (msg <span class="keyword">instanceof</span> BigInteger) {</span><br><span class="line">            v = (BigInteger) msg;</span><br><span class="line">        } <span class="keyword">else</span> {</span><br><span class="line">            v = <span class="keyword">new</span> BigInteger(String.valueOf(msg));</span><br><span class="line">        }</span><br><span class="line"></span><br><span class="line">        <span class="comment">// &lt;2&gt; 转换为字节数组</span></span><br><span class="line">        <span class="comment">// Convert the number into a byte array.</span></span><br><span class="line">        <span class="keyword">byte</span>[] data = v.toByteArray();</span><br><span class="line">        <span class="keyword">int</span> dataLength = data.length;</span><br><span class="line"></span><br><span class="line">        <span class="comment">// &lt;3&gt; Write a message.</span></span><br><span class="line">        out.writeByte((<span class="keyword">byte</span>) <span class="string">'F'</span>); <span class="comment">// magic number</span></span><br><span class="line">        out.writeInt(dataLength);  <span class="comment">// data length</span></span><br><span class="line">        out.writeBytes(data);      <span class="comment">// data</span></span><br><span class="line">    }</span><br><span class="line"></span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li><code>&lt;1&gt;</code> 处，转化消息类型为 BigInteger 对象，方便统一处理。</li>
<li><code>&lt;2&gt;</code> 处，转化为字节数组。</li>
<li><code>&lt;3&gt;</code> 处<ul>
<li>首位，写入 magic number ，方便区分<strong>不同类型</strong>的消息。例如说，后面如果有 Double 类型，可以使用 <code>D</code> ；String 类型，可以使用 <code>S</code> 。</li>
<li>后两位，写入 data length + data 。如果没有 data length ，那么数组内容，是无法读取的。</li>
</ul>
</li>
</ul>
<p>实际一般不采用 NumberEncoder 的方式，因为 POJO 类型不好支持。关于这一块，可以参看下：</p>
<ul>
<li>Dubbo</li>
<li>Motan</li>
<li>Sofa-RPC</li>
</ul>
<p>对 Encoder 和 Codec 真正实战。hoho</p>
<h1 id="666-彩蛋"><a href="#666-彩蛋" class="headerlink" title="666. 彩蛋"></a>666. 彩蛋</h1><p>MessageToByteEncoder 相比 ByteToMessageDecoder 来说，简单好多。</p>
<p>推荐阅读文章：</p>
<ul>
<li>Hypercube <a href="https://www.jianshu.com/p/7c439cc7b01c" rel="external nofollow noopener noreferrer" target="_blank">《自顶向下深入分析Netty（八）–CodecHandler》</a></li>
</ul>
<p>另外，可能很多胖友，看完 Encoder 和 Decoder ，还是一脸懵逼，不知道实际如何使用。可以在网络上，再 Google 一些资料，不要方，不要怕。</p>

</div>
<!--
<footer class="article-footer">
<a data-url="http://svip.iocoder.cn/Netty/Codec-2-1-MessageToByteEncoder-core-impl/" data-id="ck4pl3fpb00ejfgcflqmv8t0y" class="article-share-link">分享</a>

</footer>
-->
</div>
