
摘录：https://www.jianshu.com/p/0f58475c6918


<article class="_2rhmJa"><h1>前言</h1>
<p><code>Redis</code> 的 <strong>主从复制</strong> 模式下，一旦 <strong>主节点</strong> 由于故障不能提供服务，需要手动将 <strong>从节点</strong> 晋升为 <strong>主节点</strong>，同时还要通知 <strong>客户端</strong> 更新 <strong>主节点地址</strong>，这种故障处理方式从一定程度上是无法接受的。<code>Redis 2.8</code> 以后提供了 <code>Redis Sentinel</code> <strong>哨兵机制</strong> 来解决这个问题。</p>
<div class="image-package">
<div class="image-container" style="max-width: 700px; max-height: 591px; background-color: transparent;">
<div class="image-container-fill" style="padding-bottom: 33.35%;"></div>
<div class="image-view" data-width="1772" data-height="591"><img data-original-src="//upload-images.jianshu.io/upload_images/12738336-5c648c89a1f51ade" data-original-width="1772" data-original-height="591" data-original-format="image/jpeg" data-original-filesize="46258" data-image-index="0" class="" style="cursor: zoom-in;" src="//upload-images.jianshu.io/upload_images/12738336-5c648c89a1f51ade?imageMogr2/auto-orient/strip|imageView2/2/w/1200/format/webp"></div>
</div>
<div class="image-caption">image</div>
</div>
<h1>正文</h1>
<h2>1. Redis高可用概述</h2>
<p>在 <code>Web</code> 服务器中，<strong>高可用</strong> 是指服务器可以 <strong>正常访问</strong> 的时间，衡量的标准是在 <strong>多长时间</strong> 内可以提供正常服务（<code>99.9%</code>、<code>99.99%</code>、<code>99.999%</code> 等等）。在 <code>Redis</code> 层面，<strong>高可用</strong> 的含义要宽泛一些，除了保证提供 <strong>正常服务</strong>（如 <strong>主从分离</strong>、<strong>快速容灾技术</strong> 等），还需要考虑 <strong>数据容量扩展</strong>、<strong>数据安全</strong> 等等。</p>
<p>在 <code>Redis</code> 中，实现 <strong>高可用</strong> 的技术主要包括 <strong>持久化</strong>、<strong>复制</strong>、<strong>哨兵</strong> 和 <strong>集群</strong>，下面简单说明它们的作用，以及解决了什么样的问题：</p>
<ul>
<li><p><strong>持久化</strong>：持久化是 <strong>最简单的</strong> 高可用方法。它的主要作用是 <strong>数据备份</strong>，即将数据存储在 <strong>硬盘</strong>，保证数据不会因进程退出而丢失。</p></li>
<li><p><strong>复制</strong>：复制是高可用 <code>Redis</code> 的基础，<strong>哨兵</strong> 和 <strong>集群</strong> 都是在 <strong>复制基础</strong> 上实现高可用的。复制主要实现了数据的多机备份以及对于读操作的负载均衡和简单的故障恢复。缺陷是故障恢复无法自动化、写操作无法负载均衡、存储能力受到单机的限制。</p></li>
<li><p><strong>哨兵</strong>：在复制的基础上，哨兵实现了 <strong>自动化</strong> 的 <strong>故障恢复</strong>。缺陷是 <strong>写操作</strong> 无法 <strong>负载均衡</strong>，<strong>存储能力</strong> 受到 <strong>单机</strong> 的限制。</p></li>
<li><p><strong>集群</strong>：通过集群，<code>Redis</code> 解决了 <strong>写操作</strong> 无法 <strong>负载均衡</strong> 以及 <strong>存储能力</strong> 受到 <strong>单机限制</strong> 的问题，实现了较为 <strong>完善</strong> 的 <strong>高可用方案</strong>。</p></li>
</ul>
<h2>2. Redis Sentinel的基本概念</h2>
<p><code>Redis Sentinel</code> 是 <code>Redis</code> <strong>高可用</strong> 的实现方案。<code>Sentinel</code> 是一个管理多个 <code>Redis</code> 实例的工具，它可以实现对 <code>Redis</code> 的 <strong>监控</strong>、<strong>通知</strong>、<strong>自动故障转移</strong>。下面先对 <code>Redis Sentinel</code> 的 <strong>基本概念</strong> 进行简单的介绍。</p>
<p>基本名词说明：</p>
<table>
<thead>
<tr>
<th style="text-align:left">基本名词</th>
<th style="text-align:left">逻辑结构</th>
<th style="text-align:left">物理结构</th>
</tr>
</thead>
<tbody>
<tr>
<td style="text-align:left">Redis数据节点</td>
<td style="text-align:left">主节点和从节点</td>
<td style="text-align:left">主节点和从节点的进程</td>
</tr>
<tr>
<td style="text-align:left">主节点(master)</td>
<td style="text-align:left">Redis主数据库</td>
<td style="text-align:left">一个独立的Redis进程</td>
</tr>
<tr>
<td style="text-align:left">从节点(slave)</td>
<td style="text-align:left">Redis从数据库</td>
<td style="text-align:left">一个独立的Redis进程</td>
</tr>
<tr>
<td style="text-align:left">Sentinel节点</td>
<td style="text-align:left">监控Redis数据节点</td>
<td style="text-align:left">一个独立的Sentinel进程</td>
</tr>
<tr>
<td style="text-align:left">Sentinel节点集合</td>
<td style="text-align:left">若干Sentinel节点的抽象组合</td>
<td style="text-align:left">若干Sentinel节点进程</td>
</tr>
<tr>
<td style="text-align:left">Redis Sentinel</td>
<td style="text-align:left">Redis高可用实现方案</td>
<td style="text-align:left">Sentinel节点集合和Redis数据节点进程</td>
</tr>
<tr>
<td style="text-align:left">应用客户端</td>
<td style="text-align:left">泛指一个或多个客户端</td>
<td style="text-align:left">一个或者多个客户端进程或者线程</td>
</tr>
</tbody>
</table>
<p>如图所示，<code>Redis</code> 的 <strong>主从复制模式</strong> 和 <code>Sentinel</code> <strong>高可用架构</strong> 的示意图：</p>
<div class="image-package">
<div class="image-container" style="max-width: 700px; max-height: 601px; background-color: transparent;">
<div class="image-container-fill" style="padding-bottom: 58.18%;"></div>
<div class="image-view" data-width="1033" data-height="601"><img data-original-src="//upload-images.jianshu.io/upload_images/12738336-e196b8719a670121" data-original-width="1033" data-original-height="601" data-original-format="image/png" data-original-filesize="37825" data-image-index="1" class="" style="cursor: zoom-in;" src="//upload-images.jianshu.io/upload_images/12738336-e196b8719a670121?imageMogr2/auto-orient/strip|imageView2/2/w/1033/format/webp"></div>
</div>
<div class="image-caption">image</div>
</div>
<h2>3. Redis主从复制的问题</h2>
<p><code>Redis</code> <strong>主从复制</strong> 可将 <strong>主节点</strong> 数据同步给 <strong>从节点</strong>，从节点此时有两个作用：</p>
<ol>
<li>一旦 <strong>主节点宕机</strong>，<strong>从节点</strong> 作为 <strong>主节点</strong> 的 <strong>备份</strong> 可以随时顶上来。</li>
<li>扩展 <strong>主节点</strong> 的 <strong>读能力</strong>，分担主节点读压力。</li>
</ol>
<div class="image-package">
<div class="image-container" style="max-width: 394px; max-height: 338px; background-color: transparent;">
<div class="image-container-fill" style="padding-bottom: 85.79%;"></div>
<div class="image-view" data-width="394" data-height="338"><img data-original-src="//upload-images.jianshu.io/upload_images/12738336-ef07111b9825517c" data-original-width="394" data-original-height="338" data-original-format="image/png" data-original-filesize="13268" data-image-index="2" class="" style="cursor: zoom-in;" src="//upload-images.jianshu.io/upload_images/12738336-ef07111b9825517c?imageMogr2/auto-orient/strip|imageView2/2/w/394/format/webp"></div>
</div>
<div class="image-caption">image</div>
</div>
<p><strong>主从复制</strong> 同时存在以下几个问题：</p>
<ol>
<li><p>一旦 <strong>主节点宕机</strong>，<strong>从节点</strong> 晋升成 <strong>主节点</strong>，同时需要修改 <strong>应用方</strong> 的 <strong>主节点地址</strong>，还需要命令所有 <strong>从节点</strong> 去 <strong>复制</strong> 新的主节点，整个过程需要 <strong>人工干预</strong>。</p></li>
<li><p><strong>主节点</strong> 的 <strong>写能力</strong> 受到 <strong>单机的限制</strong>。</p></li>
<li><p><strong>主节点</strong> 的 <strong>存储能力</strong> 受到 <strong>单机的限制</strong>。</p></li>
<li><p><strong>原生复制</strong> 的弊端在早期的版本中也会比较突出，比如：<code>Redis</code> <strong>复制中断</strong> 后，<strong>从节点</strong> 会发起 <code>psync</code>。此时如果 <strong>同步不成功</strong>，则会进行 <strong>全量同步</strong>，<strong>主库</strong> 执行 <strong>全量备份</strong> 的同时，可能会造成毫秒或秒级的 <strong>卡顿</strong>。</p></li>
</ol>
<h2>4. Redis Sentinel深入探究</h2>
<h3>4.1. Redis Sentinel的架构</h3>
<div class="image-package">
<div class="image-container" style="max-width: 478px; max-height: 550px; background-color: transparent;">
<div class="image-container-fill" style="padding-bottom: 115.06%;"></div>
<div class="image-view" data-width="478" data-height="550"><img data-original-src="//upload-images.jianshu.io/upload_images/12738336-e6b956f930b1daa2" data-original-width="478" data-original-height="550" data-original-format="image/png" data-original-filesize="28698" data-image-index="3" class="" style="cursor: zoom-in;" src="//upload-images.jianshu.io/upload_images/12738336-e6b956f930b1daa2?imageMogr2/auto-orient/strip|imageView2/2/w/478/format/webp"></div>
</div>
<div class="image-caption">image</div>
</div>
<h3>4.2. Redis Sentinel的主要功能</h3>
<p><code>Sentinel</code> 的主要功能包括 <strong>主节点存活检测</strong>、<strong>主从运行情况检测</strong>、<strong>自动故障转移</strong> （<code>failover</code>）、<strong>主从切换</strong>。<code>Redis</code> 的 <code>Sentinel</code> 最小配置是 <strong>一主一从</strong>。</p>
<p><code>Redis</code> 的 <code>Sentinel</code> 系统可以用来管理多个 <code>Redis</code> 服务器，该系统可以执行以下四个任务：</p>
<ul>
<li><strong>监控</strong></li>
</ul>
<p><code>Sentinel</code> 会不断的检查 <strong>主服务器</strong> 和 <strong>从服务器</strong> 是否正常运行。</p>
<ul>
<li><strong>通知</strong></li>
</ul>
<p>当被监控的某个 <code>Redis</code> 服务器出现问题，<code>Sentinel</code> 通过 <code>API</code> <strong>脚本</strong> 向 <strong>管理员</strong> 或者其他的 <strong>应用程序</strong> 发送通知。</p>
<ul>
<li><strong>自动故障转移</strong></li>
</ul>
<p>当 <strong>主节点</strong> 不能正常工作时，<code>Sentinel</code> 会开始一次 <strong>自动的</strong> 故障转移操作，它会将与 <strong>失效主节点</strong> 是 <strong>主从关系</strong> 的其中一个 <strong>从节点</strong> 升级为新的 <strong>主节点</strong>，并且将其他的 <strong>从节点</strong> 指向 <strong>新的主节点</strong>。</p>
<ul>
<li><strong>配置提供者</strong></li>
</ul>
<p>在 <code>Redis Sentinel</code> 模式下，<strong>客户端应用</strong> 在初始化时连接的是 <code>Sentinel</code> <strong>节点集合</strong>，从中获取 <strong>主节点</strong> 的信息。</p>
<h3>4.3. 主观下线和客观下线</h3>
<p>默认情况下，<strong>每个</strong> <code>Sentinel</code> 节点会以 <strong>每秒一次</strong> 的频率对 <code>Redis</code> 节点和 <strong>其它</strong> 的 <code>Sentinel</code> 节点发送 <code>PING</code> 命令，并通过节点的 <strong>回复</strong> 来判断节点是否在线。</p>
<ul>
<li><strong>主观下线</strong></li>
</ul>
<p><strong>主观下线</strong> 适用于所有 <strong>主节点</strong> 和 <strong>从节点</strong>。如果在 <code>down-after-milliseconds</code> 毫秒内，<code>Sentinel</code> 没有收到 <strong>目标节点</strong> 的有效回复，则会判定 <strong>该节点</strong> 为 <strong>主观下线</strong>。</p>
<ul>
<li><strong>客观下线</strong></li>
</ul>
<p><strong>客观下线</strong> 只适用于 <strong>主节点</strong>。如果 <strong>主节点</strong> 出现故障，<code>Sentinel</code> 节点会通过 <code>sentinel is-master-down-by-addr</code> 命令，向其它 <code>Sentinel</code> 节点询问对该节点的 <strong>状态判断</strong>。如果超过 <code>&lt;quorum&gt;</code> 个数的节点判定 <strong>主节点</strong> 不可达，则该 <code>Sentinel</code> 节点会判断 <strong>主节点</strong> 为 <strong>客观下线</strong>。</p>
<h3>4.4. Sentinel的通信命令</h3>
<p><code>Sentinel</code> 节点连接一个 <code>Redis</code> 实例的时候，会创建 <code>cmd</code> 和 <code>pub/sub</code> 两个 <strong>连接</strong>。<code>Sentinel</code> 通过 <code>cmd</code> 连接给 <code>Redis</code> 发送命令，通过 <code>pub/sub</code> 连接到 <code>Redis</code> 实例上的其他 <code>Sentinel</code> 实例。</p>
<p><code>Sentinel</code> 与 <code>Redis</code> <strong>主节点</strong> 和 <strong>从节点</strong> 交互的命令，主要包括：</p>
<table>
<thead>
<tr>
<th>命令</th>
<th>作 用</th>
</tr>
</thead>
<tbody>
<tr>
<td>PING</td>
<td>
<code>Sentinel</code> 向 <code>Redis</code> 节点发送 <code>PING</code> 命令，检查节点的状态</td>
</tr>
<tr>
<td>INFO</td>
<td>
<code>Sentinel</code> 向 <code>Redis</code> 节点发送 <code>INFO</code> 命令，获取它的 <strong>从节点信息</strong>
</td>
</tr>
<tr>
<td>PUBLISH</td>
<td>
<code>Sentinel</code> 向其监控的 <code>Redis</code> 节点 <code>__sentinel__:hello</code> 这个 <code>channel</code> 发布 <strong>自己的信息</strong> 及 <strong>主节点</strong> 相关的配置</td>
</tr>
<tr>
<td>SUBSCRIBE</td>
<td>
<code>Sentinel</code> 通过订阅 <code>Redis</code> <strong>主节点</strong> 和 <strong>从节点</strong> 的 <code>__sentinel__:hello</code> 这个 <code>channnel</code>，获取正在监控相同服务的其他 <code>Sentinel</code> 节点</td>
</tr>
</tbody>
</table>
<p><code>Sentinel</code> 与 <code>Sentinel</code> 交互的命令，主要包括：</p>
<table>
<thead>
<tr>
<th>命令</th>
<th>作 用</th>
</tr>
</thead>
<tbody>
<tr>
<td>PING</td>
<td>
<code>Sentinel</code> 向其他 <code>Sentinel</code> 节点发送 <code>PING</code> 命令，检查节点的状态</td>
</tr>
<tr>
<td>SENTINEL:is-master-down-by-addr</td>
<td>和其他 <code>Sentinel</code> 协商 <strong>主节点</strong> 的状态，如果 <strong>主节点</strong> 处于 <code>SDOWN</code> 状态，则投票自动选出新的 <strong>主节点</strong>
</td>
</tr>
</tbody>
</table>
<h3>4.5. Redis Sentinel的工作原理</h3>
<p>每个 <code>Sentinel</code> 节点都需要 <strong>定期执行</strong> 以下任务：</p>
<ul>
<li>每个 <code>Sentinel</code> 以 <strong>每秒钟</strong> 一次的频率，向它所知的 <strong>主服务器</strong>、<strong>从服务器</strong> 以及其他 <code>Sentinel</code> <strong>实例</strong> 发送一个 <code>PING</code> 命令。</li>
</ul>
<div class="image-package">
<div class="image-container" style="max-width: 692px; max-height: 493px; background-color: transparent;">
<div class="image-container-fill" style="padding-bottom: 71.24000000000001%;"></div>
<div class="image-view" data-width="692" data-height="493"><img data-original-src="//upload-images.jianshu.io/upload_images/12738336-ed2457621e27ac69" data-original-width="692" data-original-height="493" data-original-format="image/png" data-original-filesize="24384" data-image-index="4" class="" style="cursor: zoom-in;" src="//upload-images.jianshu.io/upload_images/12738336-ed2457621e27ac69?imageMogr2/auto-orient/strip|imageView2/2/w/692/format/webp"></div>
</div>
<div class="image-caption">image</div>
</div>
<ol start="2">
<li>如果一个 <strong>实例</strong>（<code>instance</code>）距离 <strong>最后一次</strong> 有效回复 <code>PING</code> 命令的时间超过 <code>down-after-milliseconds</code> 所指定的值，那么这个实例会被 <code>Sentinel</code> 标记为 <strong>主观下线</strong>。</li>
</ol>
<div class="image-package">
<div class="image-container" style="max-width: 692px; max-height: 521px; background-color: transparent;">
<div class="image-container-fill" style="padding-bottom: 75.29%;"></div>
<div class="image-view" data-width="692" data-height="521"><img data-original-src="//upload-images.jianshu.io/upload_images/12738336-dc7e3c116b2ac488" data-original-width="692" data-original-height="521" data-original-format="image/png" data-original-filesize="30028" data-image-index="5" class="" style="cursor: zoom-in;" src="//upload-images.jianshu.io/upload_images/12738336-dc7e3c116b2ac488?imageMogr2/auto-orient/strip|imageView2/2/w/692/format/webp"></div>
</div>
<div class="image-caption">image</div>
</div>
<ol start="3">
<li>如果一个 <strong>主服务器</strong> 被标记为 <strong>主观下线</strong>，那么正在 <strong>监视</strong> 这个 <strong>主服务器</strong> 的所有 <code>Sentinel</code> 节点，要以 <strong>每秒一次</strong> 的频率确认 <strong>主服务器</strong> 的确进入了 <strong>主观下线</strong> 状态。</li>
</ol>
<div class="image-package">
<div class="image-container" style="max-width: 692px; max-height: 521px; background-color: transparent;">
<div class="image-container-fill" style="padding-bottom: 75.29%;"></div>
<div class="image-view" data-width="692" data-height="521"><img data-original-src="//upload-images.jianshu.io/upload_images/12738336-128fec9a2e78f79f" data-original-width="692" data-original-height="521" data-original-format="image/png" data-original-filesize="33399" data-image-index="6" class="" style="cursor: zoom-in;" src="//upload-images.jianshu.io/upload_images/12738336-128fec9a2e78f79f?imageMogr2/auto-orient/strip|imageView2/2/w/692/format/webp"></div>
</div>
<div class="image-caption">image</div>
</div>
<ol start="4">
<li>如果一个 <strong>主服务器</strong> 被标记为 <strong>主观下线</strong>，并且有 <strong>足够数量</strong> 的 <code>Sentinel</code>（至少要达到 <strong>配置文件</strong> 指定的数量）在指定的 <strong>时间范围</strong> 内同意这一判断，那么这个 <strong>主服务器</strong> 被标记为 <strong>客观下线</strong>。</li>
</ol>
<div class="image-package">
<div class="image-container" style="max-width: 692px; max-height: 521px; background-color: transparent;">
<div class="image-container-fill" style="padding-bottom: 75.29%;"></div>
<div class="image-view" data-width="692" data-height="521"><img data-original-src="//upload-images.jianshu.io/upload_images/12738336-581e758ba23cf4cf" data-original-width="692" data-original-height="521" data-original-format="image/png" data-original-filesize="36443" data-image-index="7" class="" style="cursor: zoom-in;" src="//upload-images.jianshu.io/upload_images/12738336-581e758ba23cf4cf?imageMogr2/auto-orient/strip|imageView2/2/w/692/format/webp"></div>
</div>
<div class="image-caption">image</div>
</div>
<ol start="5">
<li>在一般情况下， 每个 <code>Sentinel</code> 会以每 <code>10</code> 秒一次的频率，向它已知的所有 <strong>主服务器</strong> 和 <strong>从服务器</strong> 发送 <code>INFO</code> 命令。当一个 <strong>主服务器</strong> 被 <code>Sentinel</code> 标记为 <strong>客观下线</strong> 时，<code>Sentinel</code> 向 <strong>下线主服务器</strong> 的所有 <strong>从服务器</strong> 发送 <code>INFO</code> 命令的频率，会从 <code>10</code> 秒一次改为 <strong>每秒一次</strong>。</li>
</ol>
<div class="image-package">
<div class="image-container" style="max-width: 700px; max-height: 511px; background-color: transparent;">
<div class="image-container-fill" style="padding-bottom: 67.95%;"></div>
<div class="image-view" data-width="752" data-height="511"><img data-original-src="//upload-images.jianshu.io/upload_images/12738336-14e4979097d45bd6" data-original-width="752" data-original-height="511" data-original-format="image/png" data-original-filesize="38521" data-image-index="8" class="" style="cursor: zoom-in;" src="//upload-images.jianshu.io/upload_images/12738336-14e4979097d45bd6?imageMogr2/auto-orient/strip|imageView2/2/w/752/format/webp"></div>
</div>
<div class="image-caption">image</div>
</div>
<ol start="6">
<li>
<code>Sentinel</code> 和其他 <code>Sentinel</code> 协商 <strong>主节点</strong> 的状态，如果 <strong>主节点</strong> 处于 <code>SDOWN</code> 状态，则投票自动选出新的 <strong>主节点</strong>。将剩余的 <strong>从节点</strong> 指向 <strong>新的主节点</strong> 进行 <strong>数据复制</strong>。</li>
</ol>
<div class="image-package">
<div class="image-container" style="max-width: 700px; max-height: 569px; background-color: transparent;">
<div class="image-container-fill" style="padding-bottom: 75.66000000000001%;"></div>
<div class="image-view" data-width="752" data-height="569"><img data-original-src="//upload-images.jianshu.io/upload_images/12738336-480cab3fffcbfc6e" data-original-width="752" data-original-height="569" data-original-format="image/png" data-original-filesize="39887" data-image-index="9" class="" style="cursor: zoom-in;" src="//upload-images.jianshu.io/upload_images/12738336-480cab3fffcbfc6e?imageMogr2/auto-orient/strip|imageView2/2/w/752/format/webp"></div>
</div>
<div class="image-caption">image</div>
</div>
<ol start="7">
<li>当没有足够数量的 <code>Sentinel</code> 同意 <strong>主服务器</strong> 下线时， <strong>主服务器</strong> 的 <strong>客观下线状态</strong> 就会被移除。当 <strong>主服务器</strong> 重新向 <code>Sentinel</code> 的 <code>PING</code> 命令返回 <strong>有效回复</strong> 时，<strong>主服务器</strong> 的 <strong>主观下线状态</strong> 就会被移除。</li>
</ol>
<div class="image-package">
<div class="image-container" style="max-width: 700px; max-height: 569px; background-color: transparent;">
<div class="image-container-fill" style="padding-bottom: 38.21%;"></div>
<div class="image-view" data-width="1489" data-height="569"><img data-original-src="//upload-images.jianshu.io/upload_images/12738336-7da7e5abf85a44b0" data-original-width="1489" data-original-height="569" data-original-format="image/png" data-original-filesize="44771" data-image-index="10" class="" style="cursor: zoom-in;" src="//upload-images.jianshu.io/upload_images/12738336-7da7e5abf85a44b0?imageMogr2/auto-orient/strip|imageView2/2/w/1200/format/webp"></div>
</div>
<div class="image-caption">image</div>
</div>
<blockquote>
<p>注意：一个有效的 <code>PING</code> 回复可以是：<code>+PONG</code>、<code>-LOADING</code> 或者 <code>-MASTERDOWN</code>。如果 <strong>服务器</strong> 返回除以上三种回复之外的其他回复，又或者在 <strong>指定时间</strong> 内没有回复 <code>PING</code> 命令， 那么 <code>Sentinel</code> 认为服务器返回的回复 <strong>无效</strong>（<code>non-valid</code>）。</p>
</blockquote>
<h2>5. Redis Sentinel搭建</h2>
<h3>5.1. Redis Sentinel的部署须知</h3>
<ol>
<li><p>一个稳健的 <code>Redis Sentinel</code> 集群，应该使用至少 <strong>三个</strong> <code>Sentinel</code> 实例，并且保证讲这些实例放到 <strong>不同的机器</strong> 上，甚至不同的 <strong>物理区域</strong>。</p></li>
<li><p><code>Sentinel</code> 无法保证 <strong>强一致性</strong>。</p></li>
<li><p>常见的 <strong>客户端应用库</strong> 都支持 <code>Sentinel</code>。</p></li>
<li><p><code>Sentinel</code> 需要通过不断的 <strong>测试</strong> 和 <strong>观察</strong>，才能保证高可用。</p></li>
</ol>
<h3>5.2. Redis Sentinel的配置文件</h3>
<pre class="line-numbers  language-bash"><code class="bash  language-bash"># 哨兵sentinel实例运行的端口，默认26379  
port 26379
# 哨兵sentinel的工作目录
dir ./

# 哨兵sentinel监控的redis主节点的 
## ip：主机ip地址
## port：哨兵端口号
## master-name：可以自己命名的主节点名字（只能由字母A-z、数字0-9 、这三个字符".-_"组成。）
## quorum：当这些quorum个数sentinel哨兵认为master主节点失联 那么这时 客观上认为主节点失联了  
# sentinel monitor &lt;master-name&gt; &lt;ip&gt; &lt;redis-port&gt; &lt;quorum&gt;  
sentinel monitor mymaster 127.0.0.1 6379 2

# 当在Redis实例中开启了requirepass &lt;foobared&gt;，所有连接Redis实例的客户端都要提供密码。
# sentinel auth-pass &lt;master-name&gt; &lt;password&gt;  
sentinel auth-pass mymaster 123456  

# 指定主节点应答哨兵sentinel的最大时间间隔，超过这个时间，哨兵主观上认为主节点下线，默认30秒  
# sentinel down-after-milliseconds &lt;master-name&gt; &lt;milliseconds&gt;
sentinel down-after-milliseconds mymaster 30000  

# 指定了在发生failover主备切换时，最多可以有多少个slave同时对新的master进行同步。这个数字越小，完成failover所需的时间就越长；反之，但是如果这个数字越大，就意味着越多的slave因为replication而不可用。可以通过将这个值设为1，来保证每次只有一个slave，处于不能处理命令请求的状态。
# sentinel parallel-syncs &lt;master-name&gt; &lt;numslaves&gt;
sentinel parallel-syncs mymaster 1  

# 故障转移的超时时间failover-timeout，默认三分钟，可以用在以下这些方面：
## 1. 同一个sentinel对同一个master两次failover之间的间隔时间。  
## 2. 当一个slave从一个错误的master那里同步数据时开始，直到slave被纠正为从正确的master那里同步数据时结束。  
## 3. 当想要取消一个正在进行的failover时所需要的时间。
## 4.当进行failover时，配置所有slaves指向新的master所需的最大时间。不过，即使过了这个超时，slaves依然会被正确配置为指向master，但是就不按parallel-syncs所配置的规则来同步数据了
# sentinel failover-timeout &lt;master-name&gt; &lt;milliseconds&gt;  
sentinel failover-timeout mymaster 180000

# 当sentinel有任何警告级别的事件发生时（比如说redis实例的主观失效和客观失效等等），将会去调用这个脚本。一个脚本的最大执行时间为60s，如果超过这个时间，脚本将会被一个SIGKILL信号终止，之后重新执行。
# 对于脚本的运行结果有以下规则：  
## 1. 若脚本执行后返回1，那么该脚本稍后将会被再次执行，重复次数目前默认为10。
## 2. 若脚本执行后返回2，或者比2更高的一个返回值，脚本将不会重复执行。  
## 3. 如果脚本在执行过程中由于收到系统中断信号被终止了，则同返回值为1时的行为相同。
# sentinel notification-script &lt;master-name&gt; &lt;script-path&gt;  
sentinel notification-script mymaster /var/redis/notify.sh

# 这个脚本应该是通用的，能被多次调用，不是针对性的。
# sentinel client-reconfig-script &lt;master-name&gt; &lt;script-path&gt;
sentinel client-reconfig-script mymaster /var/redis/reconfig.sh
<span aria-hidden="true" class="line-numbers-rows"><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span></span></code></pre>
<h3>5.3. Redis Sentinel的节点规划</h3>
<table>
<thead>
<tr>
<th style="text-align:left">角色</th>
<th style="text-align:left">IP地址</th>
<th style="text-align:left">端口号</th>
</tr>
</thead>
<tbody>
<tr>
<td style="text-align:left">Redis Master</td>
<td style="text-align:left">10.206.20.231</td>
<td style="text-align:left">16379</td>
</tr>
<tr>
<td style="text-align:left">Redis Slave1</td>
<td style="text-align:left">10.206.20.231</td>
<td style="text-align:left">26379</td>
</tr>
<tr>
<td style="text-align:left">Redis Slave2</td>
<td style="text-align:left">10.206.20.231</td>
<td style="text-align:left">36379</td>
</tr>
<tr>
<td style="text-align:left">Redis Sentinel1</td>
<td style="text-align:left">10.206.20.231</td>
<td style="text-align:left">16380</td>
</tr>
<tr>
<td style="text-align:left">Redis Sentinel2</td>
<td style="text-align:left">10.206.20.231</td>
<td style="text-align:left">26380</td>
</tr>
<tr>
<td style="text-align:left">Redis Sentinel3</td>
<td style="text-align:left">10.206.20.231</td>
<td style="text-align:left">36380</td>
</tr>
</tbody>
</table>
<h3>5.4. Redis Sentinel的配置搭建</h3>
<h4>5.4.1. Redis-Server的配置管理</h4>
<p>分别拷贝三份 <code>redis.conf</code> 文件到 <code>/usr/local/redis-sentinel</code> 目录下面。三个配置文件分别对应 <code>master</code>、<code>slave1</code> 和 <code>slave2</code> 三个 <code>Redis</code> 节点的 <strong>启动配置</strong>。</p>
<pre class="line-numbers  language-bash"><code class="bash  language-bash">$ sudo cp /usr/local/redis-4.0.11/redis.conf /usr/local/redis-sentinel/redis-16379.conf
$ sudo cp /usr/local/redis-4.0.11/redis.conf /usr/local/redis-sentinel/redis-26379.conf
$ sudo cp /usr/local/redis-4.0.11/redis.conf /usr/local/redis-sentinel/redis-36379.conf
<span aria-hidden="true" class="line-numbers-rows"><span></span><span></span><span></span></span></code></pre>
<p>分别修改三份配置文件如下：</p>
<ul>
<li>主节点：redis-16379.conf</li>
</ul>
<pre class="line-numbers  language-properties"><code class="properties  language-properties">daemonize yes
pidfile /var/run/redis-16379.pid
logfile /var/log/redis/redis-16379.log
port 16379
bind 0.0.0.0
timeout 300
databases 16
dbfilename dump-16379.db
dir ./redis-workdir
masterauth 123456
requirepass 123456
<span aria-hidden="true" class="line-numbers-rows"><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span></span></code></pre>
<ul>
<li>从节点1：redis-26379.conf</li>
</ul>
<pre class="line-numbers  language-properties"><code class="properties  language-properties">daemonize yes
pidfile /var/run/redis-26379.pid
logfile /var/log/redis/redis-26379.log
port 26379
bind 0.0.0.0
timeout 300
databases 16
dbfilename dump-26379.db
dir ./redis-workdir
masterauth 123456
requirepass 123456
slaveof 127.0.0.1 16379
<span aria-hidden="true" class="line-numbers-rows"><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span></span></code></pre>
<ul>
<li>从节点2：redis-36379.conf</li>
</ul>
<pre class="line-numbers  language-properties"><code class="properties  language-properties">daemonize yes
pidfile /var/run/redis-36379.pid
logfile /var/log/redis/redis-36379.log
port 36379
bind 0.0.0.0
timeout 300
databases 16
dbfilename dump-36379.db
dir ./redis-workdir
masterauth 123456
requirepass 123456
slaveof 127.0.0.1 16379
<span aria-hidden="true" class="line-numbers-rows"><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span></span></code></pre>
<blockquote>
<p>如果要做 <strong>自动故障转移</strong>，建议所有的 <code>redis.conf</code> 都设置 <code>masterauth</code>。因为 <strong>自动故障</strong> 只会重写 <strong>主从关系</strong>，即 <code>slaveof</code>，不会自动写入 <code>masterauth</code>。如果 <code>Redis</code> 原本没有设置密码，则可以忽略。</p>
</blockquote>
<h4>5.4.2. Redis-Server启动验证</h4>
<p>按顺序分别启动 <code>16379</code>，<code>26379</code> 和 <code>36379</code> 三个 <code>Redis</code> 节点，启动命令和启动日志如下：</p>
<p><code>Redis</code> 的启动命令：</p>
<pre class="line-numbers  language-bash"><code class="bash  language-bash">$ sudo redis-server /usr/local/redis-sentinel/redis-16379.conf
$ sudo redis-server /usr/local/redis-sentinel/redis-26379.conf
$ sudo redis-server /usr/local/redis-sentinel/redis-36379.conf
<span aria-hidden="true" class="line-numbers-rows"><span></span><span></span><span></span></span></code></pre>
<p>查看 <code>Redis</code> 的启动进程：</p>
<pre class="line-numbers  language-bash"><code class="bash  language-bash">$ ps -ef | grep redis-server
0  7127     1   0  2:16下午 ??         0:01.84 redis-server 0.0.0.0:16379 
0  7133     1   0  2:16下午 ??         0:01.73 redis-server 0.0.0.0:26379 
0  7137     1   0  2:16下午 ??         0:01.70 redis-server 0.0.0.0:36379 
<span aria-hidden="true" class="line-numbers-rows"><span></span><span></span><span></span><span></span></span></code></pre>
<p>查看 <code>Redis</code> 的启动日志：</p>
<ul>
<li>节点 <code>redis-16379</code>
</li>
</ul>
<pre class="line-numbers  language-bash"><code class="bash  language-bash">$ cat /var/log/redis/redis-16379.log 
7126:C 22 Aug 14:16:38.907 # oO0OoO0OoO0Oo Redis is starting oO0OoO0OoO0Oo
7126:C 22 Aug 14:16:38.908 # Redis version=4.0.11, bits=64, commit=00000000, modified=0, pid=7126, just started
7126:C 22 Aug 14:16:38.908 # Configuration loaded
7127:M 22 Aug 14:16:38.910 * Increased maximum number of open files to 10032 (it was originally set to 256).
7127:M 22 Aug 14:16:38.912 * Running mode=standalone, port=16379.
7127:M 22 Aug 14:16:38.913 # Server initialized
7127:M 22 Aug 14:16:38.913 * Ready to accept connections
7127:M 22 Aug 14:16:48.416 * Slave 127.0.0.1:26379 asks for synchronization
7127:M 22 Aug 14:16:48.416 * Full resync requested by slave 127.0.0.1:26379
7127:M 22 Aug 14:16:48.416 * Starting BGSAVE for SYNC with target: disk
7127:M 22 Aug 14:16:48.416 * Background saving started by pid 7134
7134:C 22 Aug 14:16:48.433 * DB saved on disk
7127:M 22 Aug 14:16:48.487 * Background saving terminated with success
7127:M 22 Aug 14:16:48.494 * Synchronization with slave 127.0.0.1:26379 succeeded
7127:M 22 Aug 14:16:51.848 * Slave 127.0.0.1:36379 asks for synchronization
7127:M 22 Aug 14:16:51.849 * Full resync requested by slave 127.0.0.1:36379
7127:M 22 Aug 14:16:51.849 * Starting BGSAVE for SYNC with target: disk
7127:M 22 Aug 14:16:51.850 * Background saving started by pid 7138
7138:C 22 Aug 14:16:51.862 * DB saved on disk
7127:M 22 Aug 14:16:51.919 * Background saving terminated with success
7127:M 22 Aug 14:16:51.923 * Synchronization with slave 127.0.0.1:36379 succeeded
<span aria-hidden="true" class="line-numbers-rows"><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span></span></code></pre>
<p>以下两行日志日志表明，<code>redis-16379</code> 作为 <code>Redis</code> 的 <strong>主节点</strong>，<code>redis-26379</code> 和 <code>redis-36379</code> 作为 <strong>从节点</strong>，从 <strong>主节点</strong> 同步数据。</p>
<pre class="line-numbers  language-bash"><code class="bash  language-bash">7127:M 22 Aug 14:16:48.416 * Slave 127.0.0.1:26379 asks for synchronization
7127:M 22 Aug 14:16:51.848 * Slave 127.0.0.1:36379 asks for synchronization
<span aria-hidden="true" class="line-numbers-rows"><span></span><span></span></span></code></pre>
<ul>
<li>节点 <code>redis-26379</code>
</li>
</ul>
<pre class="line-numbers  language-bash"><code class="bash  language-bash">$ cat /var/log/redis/redis-26379.log 
7132:C 22 Aug 14:16:48.407 # oO0OoO0OoO0Oo Redis is starting oO0OoO0OoO0Oo
7132:C 22 Aug 14:16:48.408 # Redis version=4.0.11, bits=64, commit=00000000, modified=0, pid=7132, just started
7132:C 22 Aug 14:16:48.408 # Configuration loaded
7133:S 22 Aug 14:16:48.410 * Increased maximum number of open files to 10032 (it was originally set to 256).
7133:S 22 Aug 14:16:48.412 * Running mode=standalone, port=26379.
7133:S 22 Aug 14:16:48.413 # Server initialized
7133:S 22 Aug 14:16:48.413 * Ready to accept connections
7133:S 22 Aug 14:16:48.413 * Connecting to MASTER 127.0.0.1:16379
7133:S 22 Aug 14:16:48.413 * MASTER &lt;-&gt; SLAVE sync started
7133:S 22 Aug 14:16:48.414 * Non blocking connect for SYNC fired the event.
7133:S 22 Aug 14:16:48.414 * Master replied to PING, replication can continue...
7133:S 22 Aug 14:16:48.415 * Partial resynchronization not possible (no cached master)
7133:S 22 Aug 14:16:48.417 * Full resync from master: 211d3b4eceaa3af4fe5c77d22adf06e1218e0e7b:0
7133:S 22 Aug 14:16:48.494 * MASTER &lt;-&gt; SLAVE sync: receiving 176 bytes from master
7133:S 22 Aug 14:16:48.495 * MASTER &lt;-&gt; SLAVE sync: Flushing old data
7133:S 22 Aug 14:16:48.496 * MASTER &lt;-&gt; SLAVE sync: Loading DB in memory
7133:S 22 Aug 14:16:48.498 * MASTER &lt;-&gt; SLAVE sync: Finished with success
<span aria-hidden="true" class="line-numbers-rows"><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span></span></code></pre>
<ul>
<li>节点 <code>redis-36379</code>
</li>
</ul>
<pre class="line-numbers  language-bash"><code class="bash  language-bash">$ cat /var/log/redis/redis-36379.log 
7136:C 22 Aug 14:16:51.839 # oO0OoO0OoO0Oo Redis is starting oO0OoO0OoO0Oo
7136:C 22 Aug 14:16:51.840 # Redis version=4.0.11, bits=64, commit=00000000, modified=0, pid=7136, just started
7136:C 22 Aug 14:16:51.841 # Configuration loaded
7137:S 22 Aug 14:16:51.843 * Increased maximum number of open files to 10032 (it was originally set to 256).
7137:S 22 Aug 14:16:51.845 * Running mode=standalone, port=36379.
7137:S 22 Aug 14:16:51.845 # Server initialized
7137:S 22 Aug 14:16:51.846 * Ready to accept connections
7137:S 22 Aug 14:16:51.846 * Connecting to MASTER 127.0.0.1:16379
7137:S 22 Aug 14:16:51.847 * MASTER &lt;-&gt; SLAVE sync started
7137:S 22 Aug 14:16:51.847 * Non blocking connect for SYNC fired the event.
7137:S 22 Aug 14:16:51.847 * Master replied to PING, replication can continue...
7137:S 22 Aug 14:16:51.848 * Partial resynchronization not possible (no cached master)
7137:S 22 Aug 14:16:51.850 * Full resync from master: 211d3b4eceaa3af4fe5c77d22adf06e1218e0e7b:14
7137:S 22 Aug 14:16:51.923 * MASTER &lt;-&gt; SLAVE sync: receiving 176 bytes from master
7137:S 22 Aug 14:16:51.923 * MASTER &lt;-&gt; SLAVE sync: Flushing old data
7137:S 22 Aug 14:16:51.924 * MASTER &lt;-&gt; SLAVE sync: Loading DB in memory
7137:S 22 Aug 14:16:51.927 * MASTER &lt;-&gt; SLAVE sync: Finished with success
<span aria-hidden="true" class="line-numbers-rows"><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span></span></code></pre>
<h4>5.4.3. Sentinel的配置管理</h4>
<p>分别拷贝三份 <code>redis-sentinel.conf</code> 文件到 <code>/usr/local/redis-sentinel</code> 目录下面。三个配置文件分别对应 <code>master</code>、<code>slave1</code> 和 <code>slave2</code> 三个 <code>Redis</code> 节点的 <strong>哨兵配置</strong>。</p>
<pre class="line-numbers  language-bash"><code class="bash  language-bash">$ sudo cp /usr/local/redis-4.0.11/sentinel.conf /usr/local/redis-sentinel/sentinel-16380.conf
$ sudo cp /usr/local/redis-4.0.11/sentinel.conf /usr/local/redis-sentinel/sentinel-26380.conf
$ sudo cp /usr/local/redis-4.0.11/sentinel.conf /usr/local/redis-sentinel/sentinel-36380.conf
<span aria-hidden="true" class="line-numbers-rows"><span></span><span></span><span></span></span></code></pre>
<ul>
<li>节点1：sentinel-16380.conf</li>
</ul>
<pre class="line-numbers  language-properties"><code class="properties  language-properties">protected-mode no
bind 0.0.0.0
port 16380
daemonize yes
sentinel monitor master 127.0.0.1 16379 2
sentinel down-after-milliseconds master 5000
sentinel failover-timeout master 180000
sentinel parallel-syncs master 1
sentinel auth-pass master 123456
logfile /var/log/redis/sentinel-16380.log
<span aria-hidden="true" class="line-numbers-rows"><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span></span></code></pre>
<ul>
<li>节点2：sentinel-26380.conf</li>
</ul>
<pre class="line-numbers  language-properties"><code class="properties  language-properties">protected-mode no
bind 0.0.0.0
port 26380
daemonize yes
sentinel monitor master 127.0.0.1 16379 2
sentinel down-after-milliseconds master 5000
sentinel failover-timeout master 180000
sentinel parallel-syncs master 1
sentinel auth-pass master 123456
logfile /var/log/redis/sentinel-26380.log
<span aria-hidden="true" class="line-numbers-rows"><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span></span></code></pre>
<ul>
<li>节点3：sentinel-36380.conf</li>
</ul>
<pre class="line-numbers  language-properties"><code class="properties  language-properties">protected-mode no
bind 0.0.0.0
port 36380
daemonize yes
sentinel monitor master 127.0.0.1 16379 2
sentinel down-after-milliseconds master 5000
sentinel failover-timeout master 180000
sentinel parallel-syncs master 1
sentinel auth-pass master 123456
logfile /var/log/redis/sentinel-36380.log
<span aria-hidden="true" class="line-numbers-rows"><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span></span></code></pre>
<h4>5.4.4. Sentinel启动验证</h4>
<p>按顺序分别启动 <code>16380</code>，<code>26380</code> 和 <code>36380</code> 三个 <code>Sentinel</code> 节点，启动命令和启动日志如下：</p>
<pre class="line-numbers  language-bash"><code class="bash  language-bash">$ sudo redis-sentinel /usr/local/redis-sentinel/sentinel-16380.conf
$ sudo redis-sentinel /usr/local/redis-sentinel/sentinel-26380.conf
$ sudo redis-sentinel /usr/local/redis-sentinel/sentinel-36380.conf
<span aria-hidden="true" class="line-numbers-rows"><span></span><span></span><span></span></span></code></pre>
<p>查看 <code>Sentinel</code> 的启动进程：</p>
<pre class="line-numbers  language-bash"><code class="bash  language-bash">$ ps -ef | grep redis-sentinel
0  7954     1   0  3:30下午 ??         0:00.05 redis-sentinel 0.0.0.0:16380 [sentinel] 
0  7957     1   0  3:30下午 ??         0:00.05 redis-sentinel 0.0.0.0:26380 [sentinel] 
0  7960     1   0  3:30下午 ??         0:00.04 redis-sentinel 0.0.0.0:36380 [sentinel] 
<span aria-hidden="true" class="line-numbers-rows"><span></span><span></span><span></span><span></span></span></code></pre>
<p>查看 <code>Sentinel</code> 的启动日志：</p>
<ul>
<li>节点 <code>sentinel-16380</code>
</li>
</ul>
<pre class="line-numbers  language-bash"><code class="bash  language-bash">$ cat /var/log/redis/sentinel-16380.log 
7953:X 22 Aug 15:30:27.245 # oO0OoO0OoO0Oo Redis is starting oO0OoO0OoO0Oo
7953:X 22 Aug 15:30:27.245 # Redis version=4.0.11, bits=64, commit=00000000, modified=0, pid=7953, just started
7953:X 22 Aug 15:30:27.245 # Configuration loaded
7954:X 22 Aug 15:30:27.247 * Increased maximum number of open files to 10032 (it was originally set to 256).
7954:X 22 Aug 15:30:27.249 * Running mode=sentinel, port=16380.
7954:X 22 Aug 15:30:27.250 # Sentinel ID is 69d05b86a82102a8919231fd3c2d1f21ce86e000
7954:X 22 Aug 15:30:27.250 # +monitor master master 127.0.0.1 16379 quorum 2
7954:X 22 Aug 15:30:32.286 # +sdown sentinel fd166dc66425dc1d9e2670e1f17cb94fe05f5fc7 127.0.0.1 36380 @ master 127.0.0.1 16379
7954:X 22 Aug 15:30:34.588 # -sdown sentinel fd166dc66425dc1d9e2670e1f17cb94fe05f5fc7 127.0.0.1 36380 @ master 127.0.0.1 16379
<span aria-hidden="true" class="line-numbers-rows"><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span></span></code></pre>
<p><code>sentinel-16380</code> 节点的 <code>Sentinel ID</code> 为 <code>69d05b86a82102a8919231fd3c2d1f21ce86e000</code>，并通过 <code>Sentinel ID</code> 把自身加入 <code>sentinel</code> 集群中。</p>
<ul>
<li>节点 <code>sentinel-26380</code>
</li>
</ul>
<pre class="line-numbers  language-bash"><code class="bash  language-bash">$ cat /var/log/redis/sentinel-26380.log 
7956:X 22 Aug 15:30:30.900 # oO0OoO0OoO0Oo Redis is starting oO0OoO0OoO0Oo
7956:X 22 Aug 15:30:30.901 # Redis version=4.0.11, bits=64, commit=00000000, modified=0, pid=7956, just started
7956:X 22 Aug 15:30:30.901 # Configuration loaded
7957:X 22 Aug 15:30:30.904 * Increased maximum number of open files to 10032 (it was originally set to 256).
7957:X 22 Aug 15:30:30.905 * Running mode=sentinel, port=26380.
7957:X 22 Aug 15:30:30.906 # Sentinel ID is 21e30244cda6a3d3f55200bcd904d0877574e506
7957:X 22 Aug 15:30:30.906 # +monitor master master 127.0.0.1 16379 quorum 2
7957:X 22 Aug 15:30:30.907 * +slave slave 127.0.0.1:26379 127.0.0.1 26379 @ master 127.0.0.1 16379
7957:X 22 Aug 15:30:30.911 * +slave slave 127.0.0.1:36379 127.0.0.1 36379 @ master 127.0.0.1 16379
7957:X 22 Aug 15:30:36.311 * +sentinel sentinel fd166dc66425dc1d9e2670e1f17cb94fe05f5fc7 127.0.0.1 36380 @ master 127.0.0.1 16379
<span aria-hidden="true" class="line-numbers-rows"><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span></span></code></pre>
<p><code>sentinel-26380</code> 节点的 <code>Sentinel ID</code> 为 <code>21e30244cda6a3d3f55200bcd904d0877574e506</code>，并通过 <code>Sentinel ID</code> 把自身加入 <code>sentinel</code> 集群中。此时 <code>sentinel</code> 集群中已有 <code>sentinel-16380</code> 和 <code>sentinel-26380</code> 两个节点。</p>
<ul>
<li>节点 <code>sentinel-36380</code>
</li>
</ul>
<pre class="line-numbers  language-bash"><code class="bash  language-bash">$ cat /var/log/redis/sentinel-36380.log 
7959:X 22 Aug 15:30:34.273 # oO0OoO0OoO0Oo Redis is starting oO0OoO0OoO0Oo
7959:X 22 Aug 15:30:34.274 # Redis version=4.0.11, bits=64, commit=00000000, modified=0, pid=7959, just started
7959:X 22 Aug 15:30:34.274 # Configuration loaded
7960:X 22 Aug 15:30:34.276 * Increased maximum number of open files to 10032 (it was originally set to 256).
7960:X 22 Aug 15:30:34.277 * Running mode=sentinel, port=36380.
7960:X 22 Aug 15:30:34.278 # Sentinel ID is fd166dc66425dc1d9e2670e1f17cb94fe05f5fc7
7960:X 22 Aug 15:30:34.278 # +monitor master master 127.0.0.1 16379 quorum 2
7960:X 22 Aug 15:30:34.279 * +slave slave 127.0.0.1:26379 127.0.0.1 26379 @ master 127.0.0.1 16379
7960:X 22 Aug 15:30:34.283 * +slave slave 127.0.0.1:36379 127.0.0.1 36379 @ master 127.0.0.1 16379
7960:X 22 Aug 15:30:34.993 * +sentinel sentinel 21e30244cda6a3d3f55200bcd904d0877574e506 127.0.0.1 26380 @ master 127.0.0.1 16379
<span aria-hidden="true" class="line-numbers-rows"><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span></span></code></pre>
<p><code>sentinel-36380</code> 节点的 <code>Sentinel ID</code> 为 <code>fd166dc66425dc1d9e2670e1f17cb94fe05f5fc7</code>，并通过 <code>Sentinel ID</code> 把自身加入 <code>sentinel</code> 集群中。此时 <code>sentinel</code> 集群中已有 <code>sentinel-16380</code>，<code>sentinel-26380</code> 和 <code>sentinel-36380</code> 三个节点。</p>
<h4>5.4.5. Sentinel配置刷新</h4>
<ul>
<li>节点1：sentinel-16380.conf</li>
</ul>
<p><code>sentinel-16380.conf</code> 文件新生成如下的配置项：</p>
<pre class="line-numbers  language-bash"><code class="bash  language-bash"># Generated by CONFIG REWRITE
dir "/usr/local/redis-sentinel"
sentinel config-epoch master 0
sentinel leader-epoch master 0
sentinel known-slave master 127.0.0.1 36379
sentinel known-slave master 127.0.0.1 26379
sentinel known-sentinel master 127.0.0.1 26380 21e30244cda6a3d3f55200bcd904d0877574e506
sentinel known-sentinel master 127.0.0.1 36380 fd166dc66425dc1d9e2670e1f17cb94fe05f5fc7
sentinel current-epoch 0
<span aria-hidden="true" class="line-numbers-rows"><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span></span></code></pre>
<p>可以注意到，<code>sentinel-16380.conf</code> 刷新写入了 <code>Redis</code> 主节点关联的所有 <strong>从节点</strong> <code>redis-26379</code> 和 <code>redis-36379</code>，同时写入了其余两个 <code>Sentinel</code> 节点 <code>sentinel-26380</code> 和 <code>sentinel-36380</code> 的 <code>IP</code> 地址，<strong>端口号</strong> 和 <code>Sentinel ID</code>。</p>
<pre class="line-numbers  language-bash"><code class="bash  language-bash"># Generated by CONFIG REWRITE
dir "/usr/local/redis-sentinel"
sentinel config-epoch master 0
sentinel leader-epoch master 0
sentinel known-slave master 127.0.0.1 26379
sentinel known-slave master 127.0.0.1 36379
sentinel known-sentinel master 127.0.0.1 36380 fd166dc66425dc1d9e2670e1f17cb94fe05f5fc7
sentinel known-sentinel master 127.0.0.1 16380 69d05b86a82102a8919231fd3c2d1f21ce86e000
sentinel current-epoch 0
<span aria-hidden="true" class="line-numbers-rows"><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span></span></code></pre>
<p>可以注意到，<code>sentinel-26380.conf</code> 刷新写入了 <code>Redis</code> 主节点关联的所有 <strong>从节点</strong> <code>redis-26379</code> 和 <code>redis-36379</code>，同时写入了其余两个 <code>Sentinel</code> 节点 <code>sentinel-36380</code> 和 <code>sentinel-16380</code> 的 <code>IP</code> 地址，<strong>端口号</strong> 和 <code>Sentinel ID</code>。</p>
<pre class="line-numbers  language-bash"><code class="bash  language-bash"># Generated by CONFIG REWRITE
dir "/usr/local/redis-sentinel"
sentinel config-epoch master 0
sentinel leader-epoch master 0
sentinel known-slave master 127.0.0.1 36379
sentinel known-slave master 127.0.0.1 26379
sentinel known-sentinel master 127.0.0.1 16380 69d05b86a82102a8919231fd3c2d1f21ce86e000
sentinel known-sentinel master 127.0.0.1 26380 21e30244cda6a3d3f55200bcd904d0877574e506
sentinel current-epoch 0
<span aria-hidden="true" class="line-numbers-rows"><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span></span></code></pre>
<p>可以注意到，<code>sentinel-36380.conf</code> 刷新写入了 <code>Redis</code> 主节点关联的所有 <strong>从节点</strong> <code>redis-26379</code> 和 <code>redis-36379</code>，同时写入了其余两个 <code>Sentinel</code> 节点 <code>sentinel-16380</code> 和 <code>sentinel-26380</code> 的 <code>IP</code> 地址，<strong>端口号</strong> 和 <code>Sentinel ID</code>。</p>
<h4>5.5. Sentinel时客户端命令</h4>
<ul>
<li>检查其他 <code>Sentinel</code> 节点的状态，返回 <code>PONG</code> 为正常。</li>
</ul>
<pre class="line-numbers  language-bash"><code class="bash  language-bash">&gt; PING sentinel
<span aria-hidden="true" class="line-numbers-rows"><span></span></span></code></pre>
<ul>
<li>显示被监控的所有 <strong>主节点</strong> 以及它们的状态。</li>
</ul>
<pre class="line-numbers  language-bash"><code class="bash  language-bash">&gt; SENTINEL masters
<span aria-hidden="true" class="line-numbers-rows"><span></span></span></code></pre>
<ul>
<li>显示指定 <strong>主节点</strong> 的信息和状态。</li>
</ul>
<pre class="line-numbers  language-bash"><code class="bash  language-bash">&gt; SENTINEL master &lt;master_name&gt;
<span aria-hidden="true" class="line-numbers-rows"><span></span></span></code></pre>
<ul>
<li>显示指定 <strong>主节点</strong> 的所有 <strong>从节点</strong> 以及它们的状态。</li>
</ul>
<pre class="line-numbers  language-bash"><code class="bash  language-bash">&gt; SENTINEL slaves &lt;master_name&gt;
<span aria-hidden="true" class="line-numbers-rows"><span></span></span></code></pre>
<p>返回指定 <strong>主节点</strong> 的 <code>IP</code> 地址和 <strong>端口</strong>。如果正在进行 <code>failover</code> 或者 <code>failover</code> 已经完成，将会显示被提升为 <strong>主节点</strong> 的 <strong>从节点</strong> 的 <code>IP</code> 地址和 <strong>端口</strong>。</p>
<pre class="line-numbers  language-bash"><code class="bash  language-bash">&gt; SENTINEL get-master-addr-by-name &lt;master_name&gt;
<span aria-hidden="true" class="line-numbers-rows"><span></span></span></code></pre>
<ul>
<li>重置名字匹配该 <strong>正则表达式</strong> 的所有的 <strong>主节点</strong> 的状态信息，清除它之前的 <strong>状态信息</strong>，以及 <strong>从节点</strong> 的信息。</li>
</ul>
<pre class="line-numbers  language-bash"><code class="bash  language-bash">&gt; SENTINEL reset &lt;pattern&gt;
<span aria-hidden="true" class="line-numbers-rows"><span></span></span></code></pre>
<ul>
<li>强制当前 <code>Sentinel</code> 节点执行 <code>failover</code>，并且不需要得到其他 <code>Sentinel</code> 节点的同意。但是 <code>failover</code> 后会将 <strong>最新的配置</strong> 发送给其他 <code>Sentinel</code> 节点。</li>
</ul>
<pre class="line-numbers  language-bash"><code class="bash  language-bash">SENTINEL failover &lt;master_name&gt;
<span aria-hidden="true" class="line-numbers-rows"><span></span></span></code></pre>
<h2>6. Redis Sentinel故障切换与恢复</h2>
<h3>6.1. Redis CLI客户端跟踪</h3>
<p>上面的日志显示，<code>redis-16379</code> 节点为 <strong>主节点</strong>，它的进程 <code>ID</code> 为 <code>7127</code>。为了模拟 <code>Redis</code> 主节点故障，强制杀掉这个进程。</p>
<pre class="line-numbers  language-bash"><code class="bash  language-bash">$ kill -9 7127
<span aria-hidden="true" class="line-numbers-rows"><span></span></span></code></pre>
<p>使用 <code>redis-cli</code> 客户端命令进入 <code>sentinel-16380</code> 节点，查看 <code>Redis</code> <strong>节点</strong> 的状态信息。</p>
<pre class="line-numbers  language-bash"><code class="bash  language-bash">$ redis-cli -p 16380
<span aria-hidden="true" class="line-numbers-rows"><span></span></span></code></pre>
<ul>
<li>查看 <code>Redis</code> 主从集群的 <strong>主节点</strong> 信息。可以发现 <code>redis-26379</code> 晋升为 <strong>新的主节点</strong>。</li>
</ul>
<pre class="line-numbers  language-bash"><code class="bash  language-bash">
127.0.0.1:16380&gt; SENTINEL master master
1) "name"
2) "master"
3) "ip"
4) "127.0.0.1"
5) "port"
6) "26379"
7) "runid"
8) "b8ca3b468a95d1be5efe1f50c50636cafe48c59f"
9) "flags"
10) "master"
11) "link-pending-commands"
12) "0"
13) "link-refcount"
14) "1"
15) "last-ping-sent"
16) "0"
17) "last-ok-ping-reply"
18) "588"
19) "last-ping-reply"
20) "588"
21) "down-after-milliseconds"
22) "5000"
23) "info-refresh"
24) "9913"
25) "role-reported"
26) "master"
27) "role-reported-time"
28) "663171"
29) "config-epoch"
30) "1"
31) "num-slaves"
32) "2"
33) "num-other-sentinels"
34) "2"
35) "quorum"
36) "2"
37) "failover-timeout"
38) "180000"
39) "parallel-syncs"
40) "1"
<span aria-hidden="true" class="line-numbers-rows"><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span></span></code></pre>
<h3>6.2. Redis Sentinel日志跟踪</h3>
<p>查看任意 <code>Sentinel</code> 节点的日志如下：</p>
<pre class="line-numbers  language-bash"><code class="bash  language-bash">7954:X 22 Aug 18:40:22.504 # +tilt #tilt mode entered
7954:X 22 Aug 18:40:32.197 # +tilt #tilt mode entered
7954:X 22 Aug 18:41:02.241 # -tilt #tilt mode exited
7954:X 22 Aug 18:48:24.550 # +sdown master master 127.0.0.1 16379
7954:X 22 Aug 18:48:24.647 # +new-epoch 1
7954:X 22 Aug 18:48:24.651 # +vote-for-leader fd166dc66425dc1d9e2670e1f17cb94fe05f5fc7 1
7954:X 22 Aug 18:48:25.678 # +odown master master 127.0.0.1 16379 #quorum 3/2
7954:X 22 Aug 18:48:25.678 # Next failover delay: I will not start a failover before Wed Aug 22 18:54:24 2018
7954:X 22 Aug 18:48:25.709 # +config-update-from sentinel fd166dc66425dc1d9e2670e1f17cb94fe05f5fc7 127.0.0.1 36380 @ master 127.0.0.1 16379
7954:X 22 Aug 18:48:25.710 # +switch-master master 127.0.0.1 16379 127.0.0.1 26379
7954:X 22 Aug 18:48:25.710 * +slave slave 127.0.0.1:36379 127.0.0.1 36379 @ master 127.0.0.1 26379
7954:X 22 Aug 18:48:25.711 * +slave slave 127.0.0.1:16379 127.0.0.1 16379 @ master 127.0.0.1 26379
7954:X 22 Aug 18:48:30.738 # +sdown slave 127.0.0.1:16379 127.0.0.1 16379 @ master 127.0.0.1 26379
7954:X 22 Aug 19:38:23.479 # -sdown slave 127.0.0.1:16379 127.0.0.1 16379 @ master 127.0.0.1 26379
<span aria-hidden="true" class="line-numbers-rows"><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span></span></code></pre>
<ul>
<li>分析日志，可以发现 <code>redis-16329</code> 节点先进入 <code>sdown</code> <strong>主观下线</strong> 状态。</li>
</ul>
<pre class="line-numbers  language-bash"><code class="bash  language-bash">+sdown master master 127.0.0.1 16379
<span aria-hidden="true" class="line-numbers-rows"><span></span></span></code></pre>
<ul>
<li>哨兵检测到 <code>redis-16329</code> 出现故障，<code>Sentinel</code> 进入一个 <strong>新纪元</strong>，从 <code>0</code> 变为 <code>1</code>。</li>
</ul>
<pre class="line-numbers  language-bash"><code class="bash  language-bash">+new-epoch 1
<span aria-hidden="true" class="line-numbers-rows"><span></span></span></code></pre>
<ul>
<li>三个 <code>Sentinel</code> 节点开始协商 <strong>主节点</strong> 的状态，判断其是否需要 <strong>客观下线</strong>。</li>
</ul>
<pre class="line-numbers  language-bash"><code class="bash  language-bash">+vote-for-leader fd166dc66425dc1d9e2670e1f17cb94fe05f5fc7 1
<span aria-hidden="true" class="line-numbers-rows"><span></span></span></code></pre>
<ul>
<li>超过 <code>quorum</code> 个数的 <code>Sentinel</code> 节点认为 <strong>主节点</strong> 出现故障，<code>redis-16329</code> 节点进入 <strong>客观下线</strong> 状态。</li>
</ul>
<pre class="line-numbers  language-bash"><code class="bash  language-bash">+odown master master 127.0.0.1 16379 #quorum 3/2
<span aria-hidden="true" class="line-numbers-rows"><span></span></span></code></pre>
<ul>
<li>
<code>Sentinal</code> 进行 <strong>自动故障切换</strong>，协商选定 <code>redis-26329</code> 节点作为新的 <strong>主节点</strong>。</li>
</ul>
<pre class="line-numbers  language-bash"><code class="bash  language-bash">+switch-master master 127.0.0.1 16379 127.0.0.1 26379
<span aria-hidden="true" class="line-numbers-rows"><span></span></span></code></pre>
<ul>
<li>
<code>redis-36329</code> 节点和已经 <strong>客观下线</strong> 的 <code>redis-16329</code> 节点成为 <code>redis-26479</code> 的 <strong>从节点</strong>。</li>
</ul>
<pre class="line-numbers  language-bash"><code class="bash  language-bash">7954:X 22 Aug 18:48:25.710 * +slave slave 127.0.0.1:36379 127.0.0.1 36379 @ master 127.0.0.1 26379
7954:X 22 Aug 18:48:25.711 * +slave slave 127.0.0.1:16379 127.0.0.1 16379 @ master 127.0.0.1 26379
<span aria-hidden="true" class="line-numbers-rows"><span></span><span></span></span></code></pre>
<h3>6.3. Redis的配置文件</h3>
<p>分别查看三个 <code>redis</code> 节点的配置文件，发生 <strong>主从切换</strong> 时 <code>redis.conf</code> 的配置会自动发生刷新。</p>
<ul>
<li>节点 redis-16379</li>
</ul>
<pre class="line-numbers  language-bash"><code class="bash  language-bash">daemonize yes
pidfile "/var/run/redis-16379.pid"
logfile "/var/log/redis/redis-16379.log"
port 16379
bind 0.0.0.0
timeout 300
databases 16
dbfilename "dump-16379.db"
dir "/usr/local/redis-sentinel/redis-workdir"
masterauth "123456"
requirepass "123456"
<span aria-hidden="true" class="line-numbers-rows"><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span></span></code></pre>
<ul>
<li>节点 redis-26379</li>
</ul>
<pre class="line-numbers  language-bash"><code class="bash  language-bash">daemonize yes
pidfile "/var/run/redis-26379.pid"
logfile "/var/log/redis/redis-26379.log"
port 26379
bind 0.0.0.0
timeout 300
databases 16
dbfilename "dump-26379.db"
dir "/usr/local/redis-sentinel/redis-workdir"
masterauth "123456"
requirepass "123456"
<span aria-hidden="true" class="line-numbers-rows"><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span></span></code></pre>
<ul>
<li>节点 redis-36379</li>
</ul>
<pre class="line-numbers  language-bash"><code class="bash  language-bash">daemonize yes
pidfile "/var/run/redis-36379.pid"
logfile "/var/log/redis/redis-36379.log"
port 36379
bind 0.0.0.0
timeout 300
databases 16
dbfilename "dump-36379.db"
dir "/usr/local/redis-sentinel/redis-workdir"
masterauth "123456"
requirepass "123456"
slaveof 127.0.0.1 26379
<span aria-hidden="true" class="line-numbers-rows"><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span></span></code></pre>
<blockquote>
<p><strong>分析</strong>：<code>redis-26379</code> 节点 <code>slaveof</code> 配置被移除，晋升为 <strong>主节点</strong>。<code>redis-16379</code> 节点处于 <strong>宕机状态</strong>。<code>redis-36379</code> 的 <code>slaveof</code> 配置更新为 <code>127.0.0.1 redis-26379</code>，成为 <code>redis-26379</code> 的 <strong>从节点</strong>。</p>
</blockquote>
<p>重启节点 <code>redis-16379</code>。待正常启动后，再次查看它的 <code>redis.conf</code> 文件，配置如下：</p>
<pre class="line-numbers  language-bash"><code class="bash  language-bash">daemonize yes
pidfile "/var/run/redis-16379.pid"
logfile "/var/log/redis/redis-16379.log"
port 16379
bind 0.0.0.0
timeout 300
databases 16
dbfilename "dump-16379.db"
dir "/usr/local/redis-sentinel/redis-workdir"
masterauth "123456"
requirepass "123456"
# Generated by CONFIG REWRITE
slaveof 127.0.0.1 26379
<span aria-hidden="true" class="line-numbers-rows"><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span></span></code></pre>
<p>节点 <code>redis-16379</code> 的配置文件新增一行 <code>slaveof</code> 配置属性，指向 <code>redis-26379</code>，即成为 <strong>新的主节点</strong> 的 <strong>从节点</strong>。</p>
<h1>小结</h1>
<p>本文首先对 <code>Redis</code> 实现高可用的几种模式做出了阐述，指出了 <code>Redis</code> <strong>主从复制</strong> 的不足之处，进一步引入了 <code>Redis Sentinel</code> <strong>哨兵模式</strong> 的相关概念，深入说明了 <code>Redis Sentinel</code> 的 <strong>具体功能</strong>，<strong>基本原理</strong>，<strong>高可用搭建</strong> 和 <strong>自动故障切换</strong> 验证等。</p>
<p>当然，<code>Redis Sentinel</code> 仅仅解决了 <strong>高可用</strong> 的问题，对于 <strong>主节点</strong> 单点写入和单节点无法扩容等问题，还需要引入 <code>Redis Cluster</code> <strong>集群模式</strong> 予以解决。</p>
<h1>参考</h1>
<p>《Redis 开发与运维》</p>
<hr>
<p>欢迎关注技术公众号： 零壹技术栈</p>
<div class="image-package">
<div class="image-container" style="max-width: 258px; max-height: 258px; background-color: transparent;">
<div class="image-container-fill" style="padding-bottom: 100.0%;"></div>
<div class="image-view" data-width="258" data-height="258"><img data-original-src="//upload-images.jianshu.io/upload_images/12738336-27757550a1084738" data-original-width="258" data-original-height="258" data-original-format="image/jpeg" data-original-filesize="16109" data-image-index="11" class="" style="cursor: zoom-in;" src="//upload-images.jianshu.io/upload_images/12738336-27757550a1084738?imageMogr2/auto-orient/strip|imageView2/2/w/258/format/webp"></div>
</div>
<div class="image-caption">零壹技术栈</div>
</div>
<p>本帐号将持续分享后端技术干货，包括虚拟机基础，多线程编程，高性能框架，异步、缓存和消息中间件，分布式和微服务，架构学习和进阶等学习资料和文章。</p>
</article>