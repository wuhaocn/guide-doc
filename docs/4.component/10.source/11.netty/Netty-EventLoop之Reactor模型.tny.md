<div class="article-inner">

<header class="article-header">

<h1 class="article-title" itemprop="name">
精尽 Netty 源码解析 —— EventLoop（一）之 Reactor 模型
</h1>

</header>

<div class="article-entry" itemprop="articleBody">

<!-- Table of Contents -->

<h1 id="1-概述"><a href="#1-概述" class="headerlink" title="1. 概述"></a>1. 概述</h1><p>从本文开始，我们来分享 Netty 非常重要的一个组件 EventLoop 。在看 EventLoop 的具体实现之前，我们先来对 Reactor 模型做个简单的了解。</p>
<p>为什么要了解 Reactor 模型呢？因为 EventLoop 是 Netty 基于 Reactor 模型的思想进行实现。所以理解 Reactor 模型，对于我们理解 EventLoop 会有很大帮助。</p>
<p>我们来看看 Reactor 模型的<strong>核心思想</strong>：</p>
<blockquote>
<p>将关注的 I/O 事件注册到多路复用器上，一旦有 I/O 事件触发，将事件分发到事件处理器中，执行就绪 I/O 事件对应的处理函数中。模型中有三个重要的组件：</p>
<ul>
<li>多路复用器：由操作系统提供接口，Linux 提供的 I/O 复用接口有select、poll、epoll 。</li>
<li>事件分离器：将多路复用器返回的就绪事件分发到事件处理器中。</li>
<li>事件处理器：处理就绪事件处理函数。</li>
</ul>
</blockquote>
<p>初步一看，Java NIO 符合 Reactor 模型啊？因为 Reactor 有 3 种模型实现：</p>
<ol>
<li>单 Reactor 单线程模型</li>
<li>单 Reactor 多线程模型</li>
<li>多 Reactor 多线程模型</li>
</ol>
<blockquote>
<p>😈 由于老艿艿不擅长相对理论文章的内容编写，所以 <a href="#">「2.」</a>、<a href="#">「3.」</a>、<a href="#">「4.」</a> 小节的内容，我决定一本正经的引用基友 wier 的 <a href="https://my.oschina.net/u/1859679/blog/1844109" rel="external nofollow noopener noreferrer" target="_blank">《【NIO 系列】—— 之Reactor 模型》</a> 。</p>
</blockquote>
<h1 id="2-单-Reactor-单线程模型"><a href="#2-单-Reactor-单线程模型" class="headerlink" title="2. 单 Reactor 单线程模型"></a>2. 单 Reactor 单线程模型</h1><p>示例图如下：</p>
<p><a href="http://static2.iocoder.cn/images/Netty/2018_05_01/01.png" title="单 Reactor 单线程模型" class="fancybox" rel="article0"><img src="http://static2.iocoder.cn/images/Netty/2018_05_01/01.png" alt="单 Reactor 单线程模型"></a><span class="caption">单 Reactor 单线程模型</span></p>
<blockquote>
<p>老艿艿：示例代码主要表达大体逻辑，比较奔放。所以，胖友理解大体意思就好。</p>
</blockquote>
<p>Reactor 示例代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="comment">/**</span></span><br><span class="line"><span class="comment">* 等待事件到来，分发事件处理</span></span><br><span class="line"><span class="comment">*/</span></span><br><span class="line"><span class="class"><span class="keyword">class</span> <span class="title">Reactor</span> <span class="keyword">implements</span> <span class="title">Runnable</span> </span>{</span><br><span class="line">​</span><br><span class="line">  <span class="function"><span class="keyword">private</span> <span class="title">Reactor</span><span class="params">()</span> <span class="keyword">throws</span> Exception </span>{</span><br><span class="line">      SelectionKey sk = serverSocket.register(selector, SelectionKey.OP_ACCEPT);</span><br><span class="line">      <span class="comment">// attach Acceptor 处理新连接</span></span><br><span class="line">      sk.attach(<span class="keyword">new</span> Acceptor());</span><br><span class="line">  }</span><br><span class="line">​</span><br><span class="line">​  <span class="meta">@Override</span></span><br><span class="line">  <span class="function"><span class="keyword">public</span> <span class="keyword">void</span> <span class="title">run</span><span class="params">()</span> </span>{</span><br><span class="line">      <span class="keyword">try</span> {</span><br><span class="line">          <span class="keyword">while</span> (!Thread.interrupted()) {</span><br><span class="line">              selector.select();</span><br><span class="line">              Set selected = selector.selectedKeys();</span><br><span class="line">              Iterator it = selected.iterator();</span><br><span class="line">              <span class="keyword">while</span> (it.hasNext()) {</span><br><span class="line">                  it.remove();</span><br><span class="line">                  <span class="comment">//分发事件处理</span></span><br><span class="line">                  dispatch((SelectionKey) (it.next()));</span><br><span class="line">              }</span><br><span class="line">          }</span><br><span class="line">      } <span class="keyword">catch</span> (IOException ex) {</span><br><span class="line">          <span class="comment">//do something</span></span><br><span class="line">      }</span><br><span class="line">  }</span><br><span class="line">​</span><br><span class="line">  <span class="function"><span class="keyword">void</span> <span class="title">dispatch</span><span class="params">(SelectionKey k)</span> </span>{</span><br><span class="line">      <span class="comment">// 若是连接事件获取是acceptor</span></span><br><span class="line">      <span class="comment">// 若是IO读写事件获取是handler</span></span><br><span class="line">      Runnable runnable = (Runnable) (k.attachment());</span><br><span class="line">      <span class="keyword">if</span> (runnable != <span class="keyword">null</span>) {</span><br><span class="line">          runnable.run();</span><br><span class="line">      }</span><br><span class="line">  }</span><br><span class="line">​</span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<blockquote>
<p>老艿艿：示例的 Handler 的代码实现应该是漏了。胖友脑补一个实现 Runnable 接口的 Handler 类。😈</p>
</blockquote>
<p>这是最基础的单 Reactor 单线程模型。</p>
<p>Reactor 线程，负责多路分离套接字。</p>
<ul>
<li>有新连接到来触发 <code>OP_ACCEPT</code> 事件之后， 交由 Acceptor 进行处理。</li>
<li>有 IO 读写事件之后，交给 Handler 处理。</li>
</ul>
<p>Acceptor 主要任务是构造 Handler 。</p>
<ul>
<li>在获取到 Client 相关的 SocketChannel 之后，绑定到相应的 Handler 上。</li>
<li>对应的 SocketChannel 有读写事件之后，基于 Reactor 分发，Handler 就可以处理了。</li>
</ul>
<p><strong>注意，所有的 IO 事件都绑定到 Selector 上，由 Reactor 统一分发</strong>。</p>
<hr>
<p>该模型适用于处理器链中业务处理组件能快速完成的场景。不过，这种单线程模型不能充分利用多核资源，<strong>所以实际使用的不多</strong>。</p>
<h1 id="3-单-Reactor-多线程模型"><a href="#3-单-Reactor-多线程模型" class="headerlink" title="3. 单 Reactor 多线程模型"></a>3. 单 Reactor 多线程模型</h1><p>示例图如下：</p>
<p><a href="http://static2.iocoder.cn/images/Netty/2018_05_01/02.png" title="单 Reactor 多线程模型" class="fancybox" rel="article0"><img src="http://static2.iocoder.cn/images/Netty/2018_05_01/02.png" alt="单 Reactor 多线程模型"></a><span class="caption">单 Reactor 多线程模型</span></p>
<p>相对于第一种单线程的模式来说，在处理业务逻辑，也就是获取到 IO 的读写事件之后，交由线程池来处理，这样可以减小主 Reactor 的性能开销，从而更专注的做事件分发工作了，从而提升整个应用的吞吐。</p>
<p>MultiThreadHandler 示例代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="comment">/**</span></span><br><span class="line"><span class="comment">* 多线程处理读写业务逻辑</span></span><br><span class="line"><span class="comment">*/</span></span><br><span class="line"><span class="class"><span class="keyword">class</span> <span class="title">MultiThreadHandler</span> <span class="keyword">implements</span> <span class="title">Runnable</span> </span>{</span><br><span class="line">  <span class="keyword">public</span> <span class="keyword">static</span> <span class="keyword">final</span> <span class="keyword">int</span> READING = <span class="number">0</span>, WRITING = <span class="number">1</span>;</span><br><span class="line">  <span class="keyword">int</span> state;</span><br><span class="line">  <span class="keyword">final</span> SocketChannel socket;</span><br><span class="line">  <span class="keyword">final</span> SelectionKey sk;</span><br><span class="line">​</span><br><span class="line">  <span class="comment">//多线程处理业务逻辑</span></span><br><span class="line">  ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());</span><br><span class="line">​</span><br><span class="line">​</span><br><span class="line">  <span class="function"><span class="keyword">public</span> <span class="title">MultiThreadHandler</span><span class="params">(SocketChannel socket, Selector sl)</span> <span class="keyword">throws</span> Exception </span>{</span><br><span class="line">      <span class="keyword">this</span>.state = READING;</span><br><span class="line">      <span class="keyword">this</span>.socket = socket;</span><br><span class="line">      sk = socket.register(selector, SelectionKey.OP_READ);</span><br><span class="line">      sk.attach(<span class="keyword">this</span>);</span><br><span class="line">      socket.configureBlocking(<span class="keyword">false</span>);</span><br><span class="line">  }</span><br><span class="line">​</span><br><span class="line">  <span class="meta">@Override</span></span><br><span class="line">  <span class="function"><span class="keyword">public</span> <span class="keyword">void</span> <span class="title">run</span><span class="params">()</span> </span>{</span><br><span class="line">      <span class="keyword">if</span> (state == READING) {</span><br><span class="line">          read();</span><br><span class="line">      } <span class="keyword">else</span> <span class="keyword">if</span> (state == WRITING) {</span><br><span class="line">          write();</span><br><span class="line">      }</span><br><span class="line">  }</span><br><span class="line">​</span><br><span class="line">  <span class="function"><span class="keyword">private</span> <span class="keyword">void</span> <span class="title">read</span><span class="params">()</span> </span>{</span><br><span class="line">      <span class="comment">//任务异步处理</span></span><br><span class="line">      executorService.submit(() -&gt; process());</span><br><span class="line">​</span><br><span class="line">      <span class="comment">//下一步处理写事件</span></span><br><span class="line">      sk.interestOps(SelectionKey.OP_WRITE);</span><br><span class="line">      <span class="keyword">this</span>.state = WRITING;</span><br><span class="line">  }</span><br><span class="line">​</span><br><span class="line">  <span class="function"><span class="keyword">private</span> <span class="keyword">void</span> <span class="title">write</span><span class="params">()</span> </span>{</span><br><span class="line">      <span class="comment">//任务异步处理</span></span><br><span class="line">      executorService.submit(() -&gt; process());</span><br><span class="line">​</span><br><span class="line">      <span class="comment">//下一步处理读事件</span></span><br><span class="line">      sk.interestOps(SelectionKey.OP_READ);</span><br><span class="line">      <span class="keyword">this</span>.state = READING;</span><br><span class="line">  }</span><br><span class="line">​</span><br><span class="line">  <span class="comment">/**</span></span><br><span class="line"><span class="comment">    * task 业务处理</span></span><br><span class="line"><span class="comment">    */</span></span><br><span class="line">  <span class="function"><span class="keyword">public</span> <span class="keyword">void</span> <span class="title">process</span><span class="params">()</span> </span>{</span><br><span class="line">      <span class="comment">//do IO ,task,queue something</span></span><br><span class="line">  }</span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li>在 <code>#read()</code> 和 <code>#write()</code> 方法中，提交 <code>executorService</code> 线程池，进行处理。</li>
</ul>
<h1 id="4-多-Reactor-多线程模型"><a href="#4-多-Reactor-多线程模型" class="headerlink" title="4. 多 Reactor 多线程模型"></a>4. 多 Reactor 多线程模型</h1><p>示例图如下：</p>
<p><a href="http://static2.iocoder.cn/images/Netty/2018_05_01/03.png" title="多 Reactor 多线程模型" class="fancybox" rel="article0"><img src="http://static2.iocoder.cn/images/Netty/2018_05_01/03.png" alt="多 Reactor 多线程模型"></a><span class="caption">多 Reactor 多线程模型</span></p>
<p>第三种模型比起第二种模型，是将 Reactor 分成两部分：</p>
<ol>
<li>mainReactor 负责监听 ServerSocketChannel ，用来处理客户端新连接的建立，并将建立的客户端的 SocketChannel 指定注册给 subReactor 。</li>
<li>subReactor 维护自己的 Selector ，基于 mainReactor 建立的客户端的 SocketChannel 多路分离 IO 读写事件，读写网络数据。对于业务处理的功能，另外扔给 worker 线程池来完成。</li>
</ol>
<p>MultiWorkThreadAcceptor 示例代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="comment">/**</span></span><br><span class="line"><span class="comment">* 多work 连接事件Acceptor,处理连接事件</span></span><br><span class="line"><span class="comment">*/</span></span><br><span class="line"><span class="class"><span class="keyword">class</span> <span class="title">MultiWorkThreadAcceptor</span> <span class="keyword">implements</span> <span class="title">Runnable</span> </span>{</span><br><span class="line">​</span><br><span class="line">  <span class="comment">// cpu线程数相同多work线程</span></span><br><span class="line">  <span class="keyword">int</span> workCount = Runtime.getRuntime().availableProcessors();</span><br><span class="line">  SubReactor[] workThreadHandlers = <span class="keyword">new</span> SubReactor[workCount];</span><br><span class="line">  <span class="keyword">volatile</span> <span class="keyword">int</span> nextHandler = <span class="number">0</span>;</span><br><span class="line">​</span><br><span class="line">  <span class="function"><span class="keyword">public</span> <span class="title">MultiWorkThreadAcceptor</span><span class="params">()</span> </span>{</span><br><span class="line">      <span class="keyword">this</span>.init();</span><br><span class="line">  }</span><br><span class="line">​</span><br><span class="line">  <span class="function"><span class="keyword">public</span> <span class="keyword">void</span> <span class="title">init</span><span class="params">()</span> </span>{</span><br><span class="line">      nextHandler = <span class="number">0</span>;</span><br><span class="line">      <span class="keyword">for</span> (<span class="keyword">int</span> i = <span class="number">0</span>; i &lt; workThreadHandlers.length; i++) {</span><br><span class="line">          <span class="keyword">try</span> {</span><br><span class="line">              workThreadHandlers[i] = <span class="keyword">new</span> SubReactor();</span><br><span class="line">          } <span class="keyword">catch</span> (Exception e) {</span><br><span class="line">          }</span><br><span class="line">      }</span><br><span class="line">  }</span><br><span class="line">​</span><br><span class="line">  <span class="meta">@Override</span></span><br><span class="line">  <span class="function"><span class="keyword">public</span> <span class="keyword">void</span> <span class="title">run</span><span class="params">()</span> </span>{</span><br><span class="line">      <span class="keyword">try</span> {</span><br><span class="line">          SocketChannel c = serverSocket.accept();</span><br><span class="line">          <span class="keyword">if</span> (c != <span class="keyword">null</span>) {<span class="comment">// 注册读写</span></span><br><span class="line">              <span class="keyword">synchronized</span> (c) {</span><br><span class="line">                  <span class="comment">// 顺序获取SubReactor，然后注册channel </span></span><br><span class="line">                  SubReactor work = workThreadHandlers[nextHandler];</span><br><span class="line">                  work.registerChannel(c);</span><br><span class="line">                  nextHandler++;</span><br><span class="line">                  <span class="keyword">if</span> (nextHandler &gt;= workThreadHandlers.length) {</span><br><span class="line">                      nextHandler = <span class="number">0</span>;</span><br><span class="line">                  }</span><br><span class="line">              }</span><br><span class="line">          }</span><br><span class="line">      } <span class="keyword">catch</span> (Exception e) {</span><br><span class="line">      }</span><br><span class="line">  }</span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<p>SubReactor 示例代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="comment">/**</span></span><br><span class="line"><span class="comment">* 多work线程处理读写业务逻辑</span></span><br><span class="line"><span class="comment">*/</span></span><br><span class="line"><span class="class"><span class="keyword">class</span> <span class="title">SubReactor</span> <span class="keyword">implements</span> <span class="title">Runnable</span> </span>{</span><br><span class="line">  <span class="keyword">final</span> Selector mySelector;</span><br><span class="line">​</span><br><span class="line">  <span class="comment">//多线程处理业务逻辑</span></span><br><span class="line">  <span class="keyword">int</span> workCount =Runtime.getRuntime().availableProcessors();</span><br><span class="line">  ExecutorService executorService = Executors.newFixedThreadPool(workCount);</span><br><span class="line">​</span><br><span class="line">​</span><br><span class="line">  <span class="function"><span class="keyword">public</span> <span class="title">SubReactor</span><span class="params">()</span> <span class="keyword">throws</span> Exception </span>{</span><br><span class="line">      <span class="comment">// 每个SubReactor 一个selector </span></span><br><span class="line">      <span class="keyword">this</span>.mySelector = SelectorProvider.provider().openSelector();</span><br><span class="line">  }</span><br><span class="line">​</span><br><span class="line">  <span class="comment">/**</span></span><br><span class="line"><span class="comment">    * 注册chanel</span></span><br><span class="line"><span class="comment">    *</span></span><br><span class="line"><span class="comment">    * <span class="doctag">@param</span> sc</span></span><br><span class="line"><span class="comment">    * <span class="doctag">@throws</span> Exception</span></span><br><span class="line"><span class="comment">    */</span></span><br><span class="line">  <span class="function"><span class="keyword">public</span> <span class="keyword">void</span> <span class="title">registerChannel</span><span class="params">(SocketChannel sc)</span> <span class="keyword">throws</span> Exception </span>{</span><br><span class="line">      sc.register(mySelector, SelectionKey.OP_READ | SelectionKey.OP_CONNECT);</span><br><span class="line">  }</span><br><span class="line">​</span><br><span class="line">  <span class="meta">@Override</span></span><br><span class="line">  <span class="function"><span class="keyword">public</span> <span class="keyword">void</span> <span class="title">run</span><span class="params">()</span> </span>{</span><br><span class="line">      <span class="keyword">while</span> (<span class="keyword">true</span>) {</span><br><span class="line">          <span class="keyword">try</span> {</span><br><span class="line">          <span class="comment">//每个SubReactor 自己做事件分派处理读写事件</span></span><br><span class="line">              selector.select();</span><br><span class="line">              Set&lt;SelectionKey&gt; keys = selector.selectedKeys();</span><br><span class="line">              Iterator&lt;SelectionKey&gt; iterator = keys.iterator();</span><br><span class="line">              <span class="keyword">while</span> (iterator.hasNext()) {</span><br><span class="line">                  SelectionKey key = iterator.next();</span><br><span class="line">                  iterator.remove();</span><br><span class="line">                  <span class="keyword">if</span> (key.isReadable()) {</span><br><span class="line">                      read();</span><br><span class="line">                  } <span class="keyword">else</span> <span class="keyword">if</span> (key.isWritable()) {</span><br><span class="line">                      write();</span><br><span class="line">                  }</span><br><span class="line">              }</span><br><span class="line">​</span><br><span class="line">          } <span class="keyword">catch</span> (Exception e) {</span><br><span class="line">​</span><br><span class="line">          }</span><br><span class="line">      }</span><br><span class="line">  }</span><br><span class="line">​</span><br><span class="line">  <span class="function"><span class="keyword">private</span> <span class="keyword">void</span> <span class="title">read</span><span class="params">()</span> </span>{</span><br><span class="line">      <span class="comment">//任务异步处理</span></span><br><span class="line">      executorService.submit(() -&gt; process());</span><br><span class="line">  }</span><br><span class="line">​</span><br><span class="line">  <span class="function"><span class="keyword">private</span> <span class="keyword">void</span> <span class="title">write</span><span class="params">()</span> </span>{</span><br><span class="line">      <span class="comment">//任务异步处理</span></span><br><span class="line">      executorService.submit(() -&gt; process());</span><br><span class="line">  }</span><br><span class="line">​</span><br><span class="line">  <span class="comment">/**</span></span><br><span class="line"><span class="comment">    * task 业务处理</span></span><br><span class="line"><span class="comment">    */</span></span><br><span class="line">  <span class="function"><span class="keyword">public</span> <span class="keyword">void</span> <span class="title">process</span><span class="params">()</span> </span>{</span><br><span class="line">      <span class="comment">//do IO ,task,queue something</span></span><br><span class="line">  }</span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<p>从代码中，我们可以看到：</p>
<ol>
<li>mainReactor 主要用来处理网络 IO 连接建立操作，通常，mainReactor 只需要一个，因为它一个线程就可以处理。</li>
<li>subReactor 主要和建立起来的客户端的 SocketChannel 做数据交互和事件业务处理操作。通常，subReactor 的个数和 CPU 个数<strong>相等</strong>，每个 subReactor <strong>独占</strong>一个线程来处理。</li>
</ol>
<hr>
<p>此种模式中，每个模块的工作更加专一，耦合度更低，性能和稳定性也大大的提升，支持的可并发客户端数量可达到上百万级别。</p>
<blockquote>
<p>老艿艿：一般来说，是达到数十万级别。</p>
</blockquote>
<p>关于此种模式的应用，目前有很多优秀的框架已经在应用，比如 Mina 和 Netty 等等。<strong>上述中去掉线程池的第三种形式的变种，也是 Netty NIO 的默认模式</strong>。</p>
<h1 id="5-Netty-NIO-客户端"><a href="#5-Netty-NIO-客户端" class="headerlink" title="5. Netty NIO 客户端"></a>5. Netty NIO 客户端</h1><p>我们来看看 Netty NIO 客户端的示例代码中，和 EventLoop 相关的代码：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="comment">// 创建一个 EventLoopGroup 对象</span></span><br><span class="line">EventLoopGroup group = <span class="keyword">new</span> NioEventLoopGroup();</span><br><span class="line"><span class="comment">// 创建 Bootstrap 对象</span></span><br><span class="line">Bootstrap b = <span class="keyword">new</span> Bootstrap();</span><br><span class="line"><span class="comment">// 设置使用的 EventLoopGroup</span></span><br><span class="line">b.group(group);</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li>对于 Netty NIO 客户端来说，仅创建一个 EventLoopGroup 。 </li>
<li>一个 EventLoop 可以对应一个 Reactor 。因为 EventLoopGroup 是 EventLoop 的分组，所以对等理解，EventLoopGroup 是<strong>一种</strong> Reactor 的分组。</li>
<li>一个 Bootstrap 的启动，只能发起对一个远程的地址。所以只会使用一个 NIO Selector ，也就是说仅使用<strong>一个</strong> Reactor 。即使，我们在声明使用一个 EventLoopGroup ，该 EventLoopGroup 也只会分配一个 EventLoop 对 IO 事件进行处理。</li>
<li>因为 Reactor 模型主要使用服务端的开发中，如果套用在 Netty NIO 客户端中，到底使用了哪一种模式呢？<ul>
<li>如果只有一个业务线程使用 Netty NIO 客户端，那么可以认为是【单 Reactor <strong>单</strong>线程模型】。</li>
<li>如果有<strong>多个</strong>业务线程使用 Netty NIO 客户端，那么可以认为是【单 Reactor <strong>多</strong>线程模型】。</li>
</ul>
</li>
<li>那么 Netty NIO 客户端是否能够使用【多 Reactor 多线程模型】呢？😈 创建多个 Netty NIO 客户端，连接同一个服务端。那么多个 Netty 客户端就可以认为符合多 Reactor 多线程模型了。<ul>
<li>一般情况下，我们不会这么干。</li>
<li>当然，实际也有这样的示例。例如 Dubbo 或 Motan 这两个 RPC 框架，支持通过配置，同一个 Consumer 对同一个 Provider 实例同时建立多个客户端连接。 </li>
</ul>
</li>
</ul>
<h1 id="6-Netty-NIO-服务端"><a href="#6-Netty-NIO-服务端" class="headerlink" title="6. Netty NIO 服务端"></a>6. Netty NIO 服务端</h1><p>我们来看看 Netty NIO 服务端的示例代码中，和 EventLoop 相关的代码：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="comment">// 创建两个 EventLoopGroup 对象</span></span><br><span class="line">EventLoopGroup bossGroup = <span class="keyword">new</span> NioEventLoopGroup(<span class="number">1</span>); <span class="comment">// 创建 boss 线程组 用于服务端接受客户端的连接</span></span><br><span class="line">EventLoopGroup workerGroup = <span class="keyword">new</span> NioEventLoopGroup(); <span class="comment">// 创建 worker 线程组 用于进行 SocketChannel 的数据读写</span></span><br><span class="line"><span class="comment">// 创建 ServerBootstrap 对象</span></span><br><span class="line">ServerBootstrap b = <span class="keyword">new</span> ServerBootstrap();</span><br><span class="line"><span class="comment">// 设置使用的 EventLoopGroup</span></span><br><span class="line">b.group(bossGroup, workerGroup);</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li>对于 Netty NIO 服务端来说，创建两个 EventLoopGroup 。<ul>
<li><code>bossGroup</code> 对应 Reactor 模式的 mainReactor ，用于服务端接受客户端的连接。比较特殊的是，传入了方法参数 <code>nThreads = 1</code> ，表示只使用一个 EventLoop ，即只使用一个 Reactor 。这个也符合我们上面提到的，“<em>通常，mainReactor 只需要一个，因为它一个线程就可以处理</em>”。</li>
<li><code>workerGroup</code> 对应 Reactor 模式的 subReactor ，用于进行 SocketChannel 的数据读写。对于 EventLoopGroup ，如果未传递方法参数 <code>nThreads</code> ，表示使用 CPU 个数 Reactor 。这个也符合我们上面提到的，“<em>通常，subReactor 的个数和 CPU 个数相等，每个 subReactor 独占一个线程来处理</em>”。</li>
</ul>
</li>
<li>因为使用两个 EventLoopGroup ，所以符合【多 Reactor 多线程模型】的多 Reactor 的要求。实际在使用时，<code>workerGroup</code> 在读完数据时，具体的业务逻辑处理，我们会提交到<strong>专门的业务逻辑线程池</strong>，例如在 Dubbo 或 Motan 这两个 RPC 框架中。这样一来，就完全符合【多 Reactor 多线程模型】。</li>
<li>那么可能有胖友可能和我有一样的疑问，<code>bossGroup</code> 如果配置多个线程，是否可以使用<strong>多个 mainReactor</strong> 呢？我们来分析一波，一个 Netty NIO 服务端<strong>同一时间</strong>，只能 bind 一个端口，那么只能使用一个 Selector 处理客户端连接事件。又因为，Selector 操作是非线程安全的，所以无法在多个 EventLoop ( 多个线程 )中，同时操作。所以这样就导致，即使 <code>bossGroup</code> 配置多个线程，实际能够使用的也就是一个线程。</li>
<li>那么如果一定一定一定要多个 mainReactor 呢？创建多个 Netty NIO 服务端，并绑定多个端口。</li>
</ul>
<h1 id="666-彩蛋"><a href="#666-彩蛋" class="headerlink" title="666. 彩蛋"></a>666. 彩蛋</h1><p>如果 Reactor 模式讲解的不够清晰，或者想要更加深入的理解，推荐阅读如下文章：</p>
<ul>
<li>wier <a href="https://my.oschina.net/u/1859679/blog/1844109" rel="external nofollow noopener noreferrer" target="_blank">《【NIO 系列】—— 之 Reactor 模型》</a></li>
<li>永顺 <a href="https://segmentfault.com/a/1190000007403873" rel="external nofollow noopener noreferrer" target="_blank">《Netty 源码分析之 三 我就是大名鼎鼎的 EventLoop(一)》</a> 里面有几个图不错。</li>
<li>Essviv <a href="https://essviv.github.io/2017/01/25/IO/netty/reactor%E6%A8%A1%E5%9E%8B/" rel="external nofollow noopener noreferrer" target="_blank">《Reactor 模型》</a> 里面的代码示例不错。</li>
<li>xieshuang <a href="https://tech.youzan.com/yi-bu-wang-luo-mo-xing/" rel="external nofollow noopener noreferrer" target="_blank">《异步网络模型》</a> 内容很高端，一看就是高玩。</li>
</ul>
<p>另外，还有一个经典的 Proactor 模型，因为 Netty 并未实现，所以笔者就省略了。如果感兴趣的胖友，可以自行 Google 理解下。</p>

</div>
<!--
<footer class="article-footer">
<a data-url="http://svip.iocoder.cn/Netty/EventLoop-1-Reactor-Model/" data-id="ck4pl3fou00dafgcf448tkf5f" class="article-share-link">分享</a>

</footer>
-->
</div>A
