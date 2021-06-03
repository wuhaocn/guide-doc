<div id="content" class="content">

<div id="posts" class="posts-expand">

<article class="post post-type-normal" itemscope="" itemtype="http://schema.org/Article">

<div class="post-block" style="opacity: 1; display: block;">
<link itemprop="mainEntityOfPage" href="http://craze-lee.github.io/2019/02/25/每日一面/Zookeeper面试/">

<span hidden="" itemprop="author" itemscope="" itemtype="http://schema.org/Person">
<meta itemprop="name" content="Craze lee">
<meta itemprop="description" content="做个有温度的人">
<meta itemprop="image" content="/images/avatar.jpeg">
</span>

<span hidden="" itemprop="publisher" itemscope="" itemtype="http://schema.org/Organization">
<meta itemprop="name" content="念念不忘 必有回响">
</span>

<header class="post-header" style="opacity: 1; display: block; transform: translateY(0px);">

<h1 class="post-title" itemprop="name headline">Zookeeper 面试题

</h1>

<div class="post-meta">
<span class="post-time">

<span class="post-meta-item-icon">
<i class="fa fa-calendar-o"></i>
</span>

<span class="post-meta-item-text">发表于</span>

<time title="创建时间：2019-02-25 20:00:08" itemprop="dateCreated datePublished" datetime="2019-02-25T20:00:08+08:00">2019-02-25</time>

<span class="post-meta-divider">|</span>

<span class="post-meta-item-icon">
<i class="fa fa-calendar-check-o"></i>
</span>

<span class="post-meta-item-text">更新于</span>

<time title="修改时间：2019-02-26 11:56:09" itemprop="dateModified" datetime="2019-02-26T11:56:09+08:00">2019-02-26</time>

</span>

<span class="post-category">

<span class="post-meta-divider">|</span>

<span class="post-meta-item-icon">
<i class="fa fa-folder-o"></i>
</span>

<span class="post-meta-item-text">分类于</span>

<span itemprop="about" itemscope="" itemtype="http://schema.org/Thing"><a href="/categories/Zookeeper/" itemprop="url" rel="index"><span itemprop="name">Zookeeper</span></a></span>

</span>

<span class="post-comments-count">
<span class="post-meta-divider">|</span>
<span class="post-meta-item-icon">
<i class="fa fa-comment-o"></i>
</span>

<span class="post-meta-item-text">评论数：</span>
<a href="/2019/02/25/每日一面/Zookeeper面试/#comments" itemprop="discussionUrl">
<span class="post-comments-count valine-comment-count" data-xid="/2019/02/25/每日一面/Zookeeper面试/" itemprop="commentCount">0</span>
</a>
</span>

<span class="post-meta-divider">|</span>
<span class="post-meta-item-icon">
<i class="fa fa-eye"></i>
阅读次数：
<span class="busuanzi-value" id="busuanzi_value_page_pv">720</span>
</span>

</div>
</header>

<div class="post-body" itemprop="articleBody" style="opacity: 1; display: block; transform: translateY(0px);">

<h2 id="1-Zookeeper的用途，选举的原理是什么？"><a href="#1-Zookeeper的用途，选举的原理是什么？" class="headerlink" title="1. Zookeeper的用途，选举的原理是什么？"></a>1. Zookeeper的用途，选举的原理是什么？</h2><p><strong>用途</strong></p>
<ol>
<li>分布式锁</li>
<li>服务注册和发现<ul>
<li>利用Znode和Watcher，可以实现分布式服务的注册和发现。最著名的应用就是阿里的分布式RPC框架Dubbo。</li>
</ul>
</li>
<li>共享配置和状态信息<ul>
<li>Redis的分布式解决方案Codis（豌豆荚），就利用了Zookeeper来存放数据路由表和 codis-proxy 节点的元信息。同时 codis-config 发起的命令都会通过 ZooKeeper 同步到各个存活的 codis-proxy。 </li>
</ul>
</li>
<li>软负载均衡 </li>
</ol>
<a id="more"></a>
<p><strong>选举原理</strong></p>
<ol>
<li>每个 server 发出一个投票： 投票的最基本元素是（SID-服务器id,ZXID-事物id）</li>
<li>接受来自各个服务器的投票</li>
<li>处理投票：优先检查 ZXID(数据越新ZXID越大),ZXID比较大的作为leader，ZXID一样的情况下比较SID</li>
<li>统计投票：这里有个过半的概念，大于集群机器数量的一半，即大于或等于（n/2+1）,我们这里的由三台，大于等于2即为达到“过半”的要求。这里也有引申到为什么 Zookeeper 集群推荐是单数。</li>
</ol>
<h2 id="2-zookeeper-watch机制"><a href="#2-zookeeper-watch机制" class="headerlink" title="2. zookeeper watch机制"></a>2. zookeeper watch机制</h2><div class="table-container"><table>
<thead>
<tr>
<th>客户端</th>
<th>服务端</th>
</tr>
</thead>
<tbody>
<tr>
<td>Main进程</td>
<td></td>
</tr>
<tr>
<td>创建ZK客户端，会创建connet网络连接通信线程，listener监听线程</td>
<td></td>
</tr>
<tr>
<td>通过connect线程将注册的监听事件发送给Zookeeper服务端</td>
<td></td>
</tr>
<tr>
<td></td>
<td>将监听事件添加到注册监听器列表</td>
</tr>
<tr>
<td></td>
<td>监听到有数据或路径变化，将消息发送给listener</td>
</tr>
<tr>
<td>listener线程内部调用process方法</td>
</tr>
</tbody>
</table></div>
<p><img src="/images/15510827433474.jpg" alt=""></p>
<h2 id="3-Zookeeper-分布式锁"><a href="#3-Zookeeper-分布式锁" class="headerlink" title="3. Zookeeper 分布式锁"></a>3. Zookeeper 分布式锁</h2><p><img src="/images/15511531924836.jpg" alt=""></p>

</div>

<div>

</div>

<div>
<div style="padding: 10px 0; margin: 20px auto; width: 90%; text-align: center;">
<div>您的支持将鼓励我继续创作！</div>
<button id="rewardButton" disable="enable" onclick="var qr = document.getElementById(&quot;QR&quot;); if (qr.style.display === 'none') {qr.style.display='block';} else {qr.style.display='none'}">
<span>打赏</span>
</button>
<div id="QR" style="display: none;">

<div id="wechat" style="display: inline-block">
<img id="wechat_qr" src="/images/wechatpay.jpg" alt="Craze lee 微信支付">
<p>微信支付</p>
</div>

<div id="alipay" style="display: inline-block">
<img id="alipay_qr" src="/images/alipay.jpg" alt="Craze lee 支付宝">
<p>支付宝</p>
</div>

</div>
</div>

</div>

<!-- -- 添加微信图标 ---->
<div>

<img src="/images/public.png" title="微信公众号" height="120px" width="60%">

</div>

<footer class="post-footer">

<div class="post-tags">

<a href="/tags/分布式/" rel="tag"><i class="fa fa-tag"></i> 分布式</a>

<a href="/tags/Zookeeper/" rel="tag"><i class="fa fa-tag"></i> Zookeeper</a>

</div>

<div class="post-nav">
<div class="post-nav-next post-nav-item">

<a href="/2019/02/25/Zookeeper/Zookeeper入门/" rel="next" title="Zookeeper 入门介绍">
<i class="fa fa-chevron-left"></i> Zookeeper 入门介绍
</a>

</div>

<span class="post-nav-divider"></span>

<div class="post-nav-prev post-nav-item">

<a href="/2019/02/26/每日一算/下一个更大元素 I/" rel="prev" title="下一个最大元素 I">
下一个最大元素 I <i class="fa fa-chevron-right"></i>
</a>

</div>
</div>

</footer>
</div>

</article>

</div>

</div>
