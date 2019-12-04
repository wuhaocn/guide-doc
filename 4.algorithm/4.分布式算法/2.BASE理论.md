<div id="cnblogs_post_body" class="blogpost-body cnblogs-markdown"><p><img src="https://upload-images.jianshu.io/upload_images/4236553-aa5d16bbf0bb70e0.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240"></p>
<h2 id="前言">前言</h2>
<p>在前文 <a href="https://www.jianshu.com/p/4118718658ac">分布式理论(一) —— CAP 定理</a> 中，我们说，CAP 不可能同时满足，而分区容错是对于分布式系统而言，是必须的。最后，我们说，如果系统能够同时实现 CAP 是再好不过的了，所以出现了 BASE 理论，今天就来讲讲 Base 理论。</p>
<h2 id="什么是-base-理论">1. 什么是 Base 理论</h2>
<blockquote>
<p>BASE：全称：Basically Available(基本可用)，Soft state（软状态）,和 Eventually consistent（最终一致性）三个短语的缩写，来自 ebay 的架构师提出。</p>
</blockquote>
<p>Base 理论是对 CAP 中一致性和可用性权衡的结果，其来源于对大型互联网分布式实践的总结，是基于 CAP 定理逐步演化而来的。其核心思想是：</p>
<blockquote>
<p>既是无法做到强一致性（Strong consistency），但每个应用都可以根据自身的业务特点，采用适当的方式来使系统达到最终一致性（Eventual consistency）。</p>
</blockquote>
<h2 id="basically-available基本可用">2. Basically Available(基本可用)</h2>
<p>什么是基本可用呢？假设系统，出现了不可预知的故障，但还是能用，相比较正常的系统而言：</p>
<ol>
<li><p>响应时间上的损失：正常情况下的搜索引擎 0.5 秒即返回给用户结果，而<strong>基本可用</strong>的搜索引擎可以在 1 秒作用返回结果。</p></li>
<li><p>功能上的损失：在一个电商网站上，正常情况下，用户可以顺利完成每一笔订单，但是到了大促期间，为了保护购物系统的稳定性，部分消费者可能会被引导到一个降级页面。</p></li>
</ol>
<h2 id="soft-state软状态">3. Soft state（软状态）</h2>
<p>什么是软状态呢？相对于原子性而言，要求多个节点的数据副本都是一致的，这是一种 “硬状态”。</p>
<p>软状态指的是：允许系统中的数据存在中间状态，并认为该状态不影响系统的整体可用性，即允许系统在多个不同节点的数据副本存在数据延时。</p>
<h2 id="eventually-consistent最终一致性">4. Eventually consistent（最终一致性）</h2>
<p>这个比较好理解了哈。</p>
<p>上面说软状态，然后不可能一直是软状态，必须有个时间期限。在期限过后，应当保证所有副本保持数据一致性。从而达到数据的最终一致性。这个时间期限取决于网络延时，系统负载，数据复制方案设计等等因素。</p>
<p>稍微官方一点的说法就是：</p>
<blockquote>
<p>系统能够保证在没有其他新的更新操作的情况下，数据最终一定能够达到一致的状态，因此所有客户端对系统的数据访问最终都能够获取到最新的值。</p>
</blockquote>
<p>而在实际工程实践中，<strong>最终一致性分为 5 种：</strong></p>
<p><strong>1. 因果一致性（Causal consistency）</strong></p>
<p>指的是：如果节点 A 在更新完某个数据后通知了节点 B，那么节点 B 之后对该数据的访问和修改都是基于 A 更新后的值。于此同时，和节点 A 无因果关系的节点 C 的数据访问则没有这样的限制。</p>
<p><strong>2. 读己之所写（Read your writes）</strong></p>
<p>这种就很简单了，节点 A 更新一个数据后，它自身总是能访问到自身更新过的最新值，而不会看到旧值。其实也算一种因果一致性。</p>
<p><strong>3. 会话一致性（Session consistency）</strong></p>
<p>会话一致性将对系统数据的访问过程框定在了一个会话当中：系统能保证在同一个有效的会话中实现 “读己之所写” 的一致性，也就是说，执行更新操作之后，客户端能够在同一个会话中始终读取到该数据项的最新值。</p>
<p><strong>4. 单调读一致性（Monotonic read consistency）</strong></p>
<p>单调读一致性是指如果一个节点从系统中读取出一个数据项的某个值后，那么系统对于该节点后续的任何数据访问都不应该返回更旧的值。</p>
<p><strong>5. 单调写一致性（Monotonic write consistency）</strong></p>
<p>指一个系统要能够保证来自同一个节点的写操作被顺序的执行。</p>
<p>然而，在实际的实践中，这 5 种系统往往会结合使用，以构建一个具有最终一致性的分布式系统。实际上，不只是分布式系统使用最终一致性，关系型数据库在某个功能上，也是使用最终一致性的，比如备份，数据库的复制过程是需要时间的，这个复制过程中，业务读取到的值就是旧的。当然，最终还是达成了数据一致性。这也算是一个最终一致性的经典案例。</p>
<h2 id="总结">5. 总结</h2>
<p>总的来说，BASE 理论面向的是大型高可用可扩展的分布式系统，和传统事务的 ACID 是<strong>相反的</strong>，它完全不同于 ACID 的强一致性模型，而是<strong>通过牺牲强一致性</strong>来获得可用性，并允许数据在一段时间是不一致的。</p>
</div>