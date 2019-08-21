<h2 id="activity-name" class="rich_media_title">Docker 容器日志管理最佳实践</h2>
<div id="meta_content" class="rich_media_meta_list"><span id="profileBt" class="rich_media_meta rich_media_meta_nickname"><a id="js_name"></a>Linux爱好者</span>&nbsp;<em id="publish_time" class="rich_media_meta rich_media_meta_text">1周前</em></div>
<div id="js_content" class="rich_media_content ">
<p><span class="">（给</span>Linux爱好者<span class="">加星标，提升Linux技能</span><span class="">）</span></p>
<p><span class="">&nbsp;</span></p>
<blockquote>
<p>来源：自由早晚乱余生</p>
<p>www.cnblogs.com/operationhome/p/10907591.html</p>
</blockquote>
<p>&nbsp;</p>
<pre><code class=""><span class="">Docker-CE</span><br /><span class="">Server</span>&nbsp;<span class="">Version</span>:&nbsp;18<span class="">.09.6</span><br /><span class="">Storage</span>&nbsp;<span class="">Driver</span>:&nbsp;<span class="">overlay2</span><br /><span class="">Kernel</span>&nbsp;<span class="">Version</span>:&nbsp;3<span class="">.10.0-862.el7.x86_64</span><br /><span class="">Operating</span>&nbsp;<span class="">System</span>:&nbsp;<span class="">CentOS</span>&nbsp;<span class="">Linux</span>&nbsp;7&nbsp;(<span class="">Core</span>)</code></pre>
<p>Docker &nbsp;日志分为两类：</p>
<ul class="list-paddingleft-2">
<li>
<p>Docker 引擎日志(也就是 dockerd 运行时的日志)，</p>
</li>
<li>
<p>容器的日志，容器内的服务产生的日志。</p>
</li>
</ul>
<h3><strong>&nbsp;</strong></h3>
<h3><strong>一 、Docker 引擎日志</strong></h3>
<p>&nbsp;</p>
<p>Docker 引擎日志一般是交给了 Upstart(Ubuntu 14.04) 或者 systemd (CentOS 7, Ubuntu 16.04)。前者一般位于 /var/log/upstart/docker.log 下，后者我们一般 通过 &nbsp;<code>journalctl -u docker</code>&nbsp;&nbsp;来进行查看。</p>
<p>&nbsp;</p>
<table width="676">
<thead>
<tr>
<th width="225">系统</th>
<th width="337">日志位置</th>
</tr>
</thead>
<tbody>
<tr>
<td width="226">Ubuntu(14.04)</td>
<td width="337"><code>/var/log/upstart/docker.log</code></td>
</tr>
<tr>
<td width="227">Ubuntu(16.04)</td>
<td width="337"><code>journalctl -u docker.service</code></td>
</tr>
<tr>
<td width="227">CentOS 7/RHEL 7/Fedora</td>
<td width="337"><code>journalctl -u docker.service</code></td>
</tr>
<tr>
<td width="227">CoreOS</td>
<td width="337"><code>journalctl -u docker.service</code></td>
</tr>
<tr>
<td width="236">OpenSuSE</td>
<td width="337"><code>journalctl -u docker.service</code></td>
</tr>
<tr>
<td width="227">OSX</td>
<td width="337"><code>~/Library/Containers/com.docker.docker/Data/com.docker.driver.amd64-linux/log/d?ocker.log</code></td>
</tr>
<tr>
<td width="232">Debian GNU/Linux 7</td>
<td width="337"><code>/var/log/daemon.log</code></td>
</tr>
<tr>
<td width="227">Debian GNU/Linux 8</td>
<td width="337"><code>journalctl -u docker.service</code></td>
</tr>
<tr>
<td width="227">Boot2Docker</td>
<td width="337"><code>/var/log/docker.log</code></td>
</tr>
</tbody>
</table>
<p>以上内容来自：https://blog.lab99.org/post/docker-2016-07-14-faq.html</p>
<h3><strong>二、容器日志</strong></h3>
<h4>&nbsp;</h4>
<h4><strong>2.1、常用查看日志命令&mdash;&mdash;docker logs</strong></h4>
<p><code>docker logs CONTAINER</code>&nbsp;显示当前运行的容器的日志信息， UNIX 和 Linux 的命令有三种 输入输出，分别是 STDIN(标准输入)、STDOUT(标准输出)、STDERR(标准错误输出)，docker logs &nbsp;显示的内容包含 STOUT 和 STDERR。在生产环境，如果我们的应用输出到我们的日志文件里，所以我们在使用 &nbsp;docker &nbsp;logs 一般收集不到太多重要的日志信息。</p>
<blockquote>
<ul class="list-paddingleft-2">
<li>
<p>nginx 官方镜像，使用了一种方式，让日志输出到 STDOUT，也就是 创建一个符号链接<code>/var/log/nginx/access.log</code>&nbsp;到&nbsp;<code>/dev/stdout</code>。</p>
</li>
<li>
<p>httpd 使用的是 让其输出到指定文件 ，正常日志输出到&nbsp;<code>/proc/self/fd/1</code>&nbsp;(STDOUT) ，错误日志输出到&nbsp;<code>/proc/self/fd/2</code>&nbsp;(STDERR)。</p>
</li>
<li>
<p>当日志量比较大的时候，我们使用 docker logs &nbsp; 来查看日志，会对 docker daemon 造成比较大的压力，容器导致容器创建慢等一系列问题。</p>
</li>
<li>
<p><strong>只有使用了 `local 、json-file、journald` &nbsp;的日志驱动的容器才可以使用 docker logs 捕获日志，使用其他日志驱动无法使用 `docker logs`</strong></p>
</li>
</ul>
</blockquote>
<h4>2.2 、Docker 日志 驱动</h4>
<p>Docker 提供了两种模式用于将消息从容器到日志驱动。</p>
<ul class="list-paddingleft-2">
<li>
<p>(默认)拒绝，阻塞从容器到容器驱动</p>
</li>
<li>
<p>非阻塞传递,日志将储存在容器的缓冲区。</p>
</li>
</ul>
<blockquote>
<p>当缓冲区满，旧的日志将被丢弃。</p>
</blockquote>
<p>在 mode 日志选项控制使用&nbsp;<code>blocking(默认)</code>&nbsp;或者&nbsp;<code>non-blocking</code>, 当设置为&nbsp;<code>non-blocking</code>需要设置&nbsp;<code>max-buffer-size</code>&nbsp;参数(默认为 1MB)。</p>
<p>支持的驱动</p>
<table width="676">
<thead>
<tr>
<th width="67">&nbsp;</th>
<th width="404">描述</th>
</tr>
</thead>
<tbody>
<tr>
<td width="67"><code>none</code></td>
<td width="404">运行的容器没有日志，<code>docker logs</code>也不返回任何输出。</td>
</tr>
<tr>
<td width="67"><code>local</code></td>
<td width="404">日志以自定义格式存储，旨在实现最小开销。</td>
</tr>
<tr>
<td width="67"><code>json-file</code></td>
<td width="404">日志格式为JSON。Docker的默认日志记录驱动程序。</td>
</tr>
<tr>
<td width="168"><code>syslog</code></td>
<td width="404">将日志消息写入<code>syslog</code>。该<code>syslog</code>守护程序必须在主机上运行。</td>
</tr>
<tr>
<td width="67"><code>journald</code></td>
<td width="404">将日志消息写入<code>journald</code>。该<code>journald</code>守护程序必须在主机上运行。</td>
</tr>
<tr>
<td width="67"><code>gelf</code></td>
<td width="404">将日志消息写入Graylog扩展日志格式（GELF）端点，例如Graylog或Logstash。</td>
</tr>
<tr>
<td width="67"><code>fluentd</code></td>
<td width="404">将日志消息写入<code>fluentd</code>（转发输入）。该<code>fluentd</code>守护程序必须在主机上运行。</td>
</tr>
<tr>
<td width="67"><code>awslogs</code></td>
<td width="404">将日志消息写入Amazon CloudWatch Logs。</td>
</tr>
<tr>
<td width="164"><code>splunk</code></td>
<td width="404">使用HTTP事件收集器将日志消息写入<code>splunk</code>。</td>
</tr>
<tr>
<td width="67"><code>etwlogs</code></td>
<td width="404">将日志消息写为Windows事件跟踪（ETW）事件。仅适用于Windows平台。</td>
</tr>
<tr>
<td width="67"><code>gcplogs</code></td>
<td width="404">将日志消息写入Google Cloud Platform（GCP）Logging。</td>
</tr>
<tr>
<td width="67"><code>logentries</code></td>
<td width="404">将日志消息写入Rapid7 Logentries。</td>
</tr>
</tbody>
</table>
<p>使用 Docker-CE 版本，<code>docker logs</code>命令 仅仅适用于以下驱动程序(前面 docker logs 详解也提及到了)</p>
<ul class="list-paddingleft-2">
<li>
<p>local</p>
</li>
<li>
<p>json-file</p>
</li>
<li>
<p>journald</p>
</li>
</ul>
<figure><img src="https://mmbiz.qpic.cn/mmbiz_png/NW4iaKVI4GNOvqkw3xribUiaMYKsJRkuIxagkWX61Xu0feRDQicjDySAIPnk1mLhCDFAIOMj5pJ3hnEicI4Ria4J3Jzg/640?wx_fmt=png&amp;tp=webp&amp;wxfrom=5&amp;wx_lazy=1&amp;wx_co=1" alt="" width="140" height="140" /></figure>
<h6>Docker 日志驱动常用命令</h6>
<p>查看系统当前设置的日志驱动</p>
<pre><code class="">docker&nbsp;&nbsp;info&nbsp;|grep&nbsp;&nbsp;<span class="">"Logging&nbsp;Driver"</span>&nbsp;&nbsp;/&nbsp;docker&nbsp;info&nbsp;--format&nbsp;<span class="">'{{.LoggingDriver}}'</span><br /></code></pre>
<p>查看单个容器的设置的日志驱动</p>
<pre><code class="">docker&nbsp;inspect&nbsp;&nbsp;-f&nbsp;<span class="">'{{.HostConfig.LogConfig.Type}}'</span>&nbsp;&nbsp;&nbsp;容器id<br /></code></pre>
<h6>Docker 日志驱动全局配置更改</h6>
<p>修改日志驱动，在配置文件&nbsp;<code>/etc/docker/daemon.json</code>（注意该文件内容是 JSON 格式的）进行配置即可。</p>
<p>示例：</p>
<pre><code class="">{<br />&nbsp;&nbsp;<span class="">"log-driver"</span>:&nbsp;<span class="">"syslog"</span><br />}<br /></code></pre>
<p>以上更改是针对所有的容器的日志驱动的。我们也可以单独为单一容器设置日志驱动。</p>
<h6>Docker 单一容器日志驱动配置</h6>
<p>在 运行容器的时候指定 日志驱动&nbsp;<code>--log-driver</code>。</p>
<pre><code class="">docker&nbsp;&nbsp;run&nbsp;&nbsp;-itd&nbsp;--<span class="">log</span>-driver&nbsp;none&nbsp;alpine&nbsp;ash&nbsp;<span class="">#&nbsp;这里指定的日志驱动为&nbsp;none&nbsp;</span><br /></code></pre>
<h6>日志驱动 一 、local</h6>
<p><code>local</code>&nbsp;&nbsp;日志驱动 记录从容器的&nbsp;<code>STOUT/STDERR</code>&nbsp;的输出，并写到宿主机的磁盘。</p>
<p>默认情况下，local &nbsp;日志驱动为每个容器保留 100MB 的日志信息，并启用自动压缩来保存。(经过测试，保留100MB 的日志是指没有经过压缩的日志)</p>
<p>local 日志驱动的储存位置&nbsp;<code>/var/lib/docker/containers/容器id/local-logs/</code>&nbsp;以<code>container.log</code>&nbsp;命名。</p>
<p><strong>local 驱动支持的选项</strong></p>
<table width="676">
<thead>
<tr>
<th width="120">选项</th>
<th width="209">描述</th>
<th width="188">示例值</th>
</tr>
</thead>
<tbody>
<tr>
<td width="153"><code>max-size</code></td>
<td width="209">切割之前日志的最大大小。可取值为(k,m,g)， 默认为20m。</td>
<td width="188"><code>--log-opt max-size=10m</code></td>
</tr>
<tr>
<td width="153"><code>max-file</code></td>
<td width="209">可以存在的最大日志文件数。如果超过最大值，则会删除最旧的文件。**仅在max-size设置时有效。默认为5。</td>
<td width="188"><code>--log-opt max-file=3</code></td>
</tr>
<tr>
<td width="120"><code>compress</code></td>
<td width="209">对应切割日志文件是否启用压缩。默认情况下启用。</td>
<td width="188"><code>--log-opt compress=false</code></td>
</tr>
</tbody>
</table>
<p><strong>全局日志驱动设置为&mdash;local</strong></p>
<p>在配置文件&nbsp;<code>/etc/docker/daemon.json</code>（注意该文件内容是 JSON 格式的）进行配置即可。</p>
<pre><code class="">{<br />&nbsp;&nbsp;<span class="">"log-driver"</span>:&nbsp;<span class="">"local"</span>,<br />&nbsp;&nbsp;<span class="">"log-opts"</span>:&nbsp;{<br />&nbsp;&nbsp;&nbsp;&nbsp;<span class="">"max-size"</span>:&nbsp;<span class="">"10m"</span><br />&nbsp;&nbsp;}<br />}<br /></code></pre>
<p>重启 docker &nbsp;即可生效。</p>
<p><strong>单个容器日志驱动设置为&mdash;local</strong></p>
<p>运行容器并设定为&nbsp;<code>local</code>&nbsp;驱动。</p>
<pre><code class=""><span class="">#&nbsp;&nbsp;运行一个容器&nbsp;，并设定日志驱动为&nbsp;local&nbsp;，并运行命令&nbsp;ping&nbsp;www.baidu.com</span><br />[root@localhost&nbsp;docker]<span class="">#&nbsp;docker&nbsp;run&nbsp;&nbsp;-itd&nbsp;&nbsp;--log-driver&nbsp;&nbsp;local&nbsp;&nbsp;alpine&nbsp;&nbsp;ping&nbsp;www.baidu.com&nbsp;</span><br />3795b6483534961c1d5223359ad1106433ce2bf25e18b981a47a2d79ad7a3156<br /><span class="">#&nbsp;&nbsp;查看运行的容器的&nbsp;日志驱动是否是&nbsp;local</span><br />[root@localhost&nbsp;docker]<span class="">#&nbsp;docker&nbsp;inspect&nbsp;&nbsp;-f&nbsp;'{{.HostConfig.LogConfig.Type}}'&nbsp;&nbsp;&nbsp;3795b6483534961c</span><br /><span class="">local</span><br /><span class="">#&nbsp;查看日志</span><br />[root@localhost&nbsp;<span class="">local</span>-logs]<span class="">#&nbsp;tail&nbsp;-f&nbsp;&nbsp;/var/lib/docker/containers/3795b6483534961c1d5223359ad1106433ce2bf25e18b981a47a2d79ad7a3156/local-logs/container.log&nbsp;</span><br />NNdout????:64&nbsp;bytes&nbsp;from&nbsp;14.215.177.38:&nbsp;seq=816&nbsp;ttl=55&nbsp;time=5.320&nbsp;ms<br />NNdout?&mu;???:64&nbsp;bytes&nbsp;from&nbsp;14.215.177.38:&nbsp;seq=817&nbsp;ttl=55&nbsp;time=4.950&nbsp;ms<br /></code></pre>
<blockquote>
<p>注意事项： 经过测试，当我们产生了100 MB 大小的日志时 会有 四个压缩文件和一个<code>container.log</code>：</p>
<pre><code class="">[root@localhost&nbsp;<span class="">local</span>-logs]<span class="">#&nbsp;ls&nbsp;-l</span><br />total&nbsp;32544<br />-rw-r-----.&nbsp;1&nbsp;root&nbsp;root&nbsp;18339944&nbsp;May&nbsp;16&nbsp;09:41&nbsp;container.log<br />-rw-r-----.&nbsp;1&nbsp;root&nbsp;root&nbsp;&nbsp;3698660&nbsp;May&nbsp;16&nbsp;09:41&nbsp;container.log.1.gz<br />-rw-r-----.&nbsp;1&nbsp;root&nbsp;root&nbsp;&nbsp;3726315&nbsp;May&nbsp;16&nbsp;09:41&nbsp;container.log.2.gz<br />-rw-r-----.&nbsp;1&nbsp;root&nbsp;root&nbsp;&nbsp;3805668&nbsp;May&nbsp;16&nbsp;09:41&nbsp;container.log.3.gz<br />-rw-r-----.&nbsp;1&nbsp;root&nbsp;root&nbsp;&nbsp;3744104&nbsp;May&nbsp;16&nbsp;09:41&nbsp;container.log.4.gz<br /></code></pre>
&nbsp;
<p>那么当超过了 100MB 的日志文件，日志文件会继续写入到 &nbsp;<code>container.log</code>，但是会将 &nbsp;<code>container.log</code>&nbsp;日志中老的日志删除，追加新的，也就是 当写满 100MB 日志后 ，再产生一条新日志，会删除 &nbsp;<code>container.log</code>&nbsp;中的一条老日志，保存 100MB 的大小。<strong>这个 对我们是会有一些影响的，</strong></p>
<pre><code class="">当我运行系统时&nbsp;第一天由于bug产生了&nbsp;100MB&nbsp;日志，那么之前的日志就已经有&nbsp;80MB&nbsp;日志变成的压缩包，所以我在后续的运行中，只能获取最近的&nbsp;20MB日志。<br /></code></pre>
</blockquote>
<h6>日志驱动 二、 默认的日志驱动&mdash;JSON</h6>
<p><strong>所有容器默认的日志驱动&nbsp;</strong><strong><code>json-file</code></strong>。</p>
<p><code>json-file</code>&nbsp;日志驱动 记录从容器的&nbsp;<code>STOUT/STDERR</code>&nbsp;的输出 ，用 JSON 的格式写到文件中，日志中不仅包含着 输出日志，还有时间戳和 输出格式。下面是一个&nbsp;<code>ping www.baidu.com</code>&nbsp;&nbsp;对应的 JSON 日志</p>
<pre><code class="">{<span class="">"log"</span>:<span class="">"64&nbsp;bytes&nbsp;from&nbsp;14.215.177.39:&nbsp;seq=34&nbsp;ttl=55&nbsp;time=7.067&nbsp;ms\r\n"</span>,<span class="">"stream"</span>:<span class="">"stdout"</span>,<span class="">"time"</span>:<span class="">"2019-05-16T14:14:15.030612567Z"</span>}<br /></code></pre>
<p>json-file &nbsp;日志的路径位于&nbsp;<code>/var/lib/docker/containers/container_id/container_id-json.log</code>。</p>
<p><code>json-file</code>&nbsp;的 日志驱动支持以下选项：</p>
<table width="676">
<thead>
<tr>
<th width="91">选项</th>
<th width="209">描述</th>
<th width="226">示例值</th>
</tr>
</thead>
<tbody>
<tr>
<td width="121"><code>max-size</code></td>
<td width="209">切割之前日志的最大大小。可取值单位为(k,m,g)， 默认为-1（表示无限制）。</td>
<td width="226"><code>--log-opt max-size=10m</code></td>
</tr>
<tr>
<td width="123"><code>max-file</code></td>
<td width="205">可以存在的最大日志文件数。如果切割日志会创建超过阈值的文件数，则会删除最旧的文件。<strong>仅在max-size设置时有效。</strong>正整数。默认为1。</td>
<td width="226"><code>--log-opt max-file=3</code></td>
</tr>
<tr>
<td width="91"><code>labels</code></td>
<td width="207">适用于启动Docker守护程序时。此守护程序接受的以逗号分隔的与日志记录相关的标签列表。</td>
<td width="226"><code>--log-opt labels=production_status,geo</code></td>
</tr>
<tr>
<td width="91"><code>env</code></td>
<td width="207">适用于启动Docker守护程序时。此守护程序接受的以逗号分隔的与日志记录相关的环境变量列表。</td>
<td width="226"><code>--log-opt env=os,customer</code></td>
</tr>
<tr>
<td width="91"><code>env-regex</code></td>
<td width="207">类似于并兼容<code>env</code>。用于匹配与日志记录相关的环境变量的正则表达式。</td>
<td width="226"><code>--log-opt env-regex=^(os|customer).</code></td>
</tr>
<tr>
<td width="91"><code>compress</code></td>
<td width="207">切割的日志是否进行压缩。默认是<code>disabled</code>。</td>
<td width="225"><code>--log-opt compress=true</code></td>
</tr>
</tbody>
</table>
<p><strong><code>json-file</code>&nbsp;的日志驱动示例</strong></p>
<pre><code class=""><span class="">#&nbsp;设置&nbsp;日志驱动为&nbsp;json-file&nbsp;，我们也可以不设置，因为默认就是&nbsp;json-file</span><br />docker&nbsp;run&nbsp;&nbsp;-itd&nbsp;&nbsp;--name&nbsp;&nbsp;<span class="">test</span>-log-json&nbsp;&nbsp;--<span class="">log</span>-driver&nbsp;json-file&nbsp;&nbsp;&nbsp;alpine&nbsp;&nbsp;ping&nbsp;www.baidu.com<br />199608b2e2c52136d2a17e539e9ef7fbacf97f1293678aded421dadbdb006a5e<br /><br /><span class="">#&nbsp;查看日志,日志名称就是&nbsp;容器名称-json.log</span><br />tail&nbsp;-f&nbsp;/var/lib/docker/containers/199608b2e2c52136d2a17e539e9ef7fbacf97f1293678aded421dadbdb006a5e/199608b2e2c52136d2a17e539e9ef7fbacf97f1293678aded421dadbdb006a5e-json.log<br /><br />{<span class="">"log"</span>:<span class="">"64&nbsp;bytes&nbsp;from&nbsp;14.215.177.39:&nbsp;seq=13&nbsp;ttl=55&nbsp;time=15.023&nbsp;ms\r\n"</span>,<span class="">"stream"</span>:<span class="">"stdout"</span>,<span class="">"time"</span>:<span class="">"2019-05-16T14:13:54.003118877Z"</span>}<br />{<span class="">"log"</span>:<span class="">"64&nbsp;bytes&nbsp;from&nbsp;14.215.177.39:&nbsp;seq=14&nbsp;ttl=55&nbsp;time=9.640&nbsp;ms\r\n"</span>,<span class="">"stream"</span>:<span class="">"stdout"</span>,<span class="">"time"</span>:<span class="">"2019-05-16T14:13:54.999011017Z"</span>}<br />{<span class="">"log"</span>:<span class="">"64&nbsp;bytes&nbsp;from&nbsp;14.215.177.39:&nbsp;seq=15&nbsp;ttl=55&nbsp;time=8.938&nbsp;ms\r\n"</span>,<span class="">"stream"</span>:<span class="">"stdout"</span>,<span class="">"time"</span>:<span class="">"2019-05-16T14:13:55.998612636Z"</span>}<br />{<span class="">"log"</span>:<span class="">"64&nbsp;bytes&nbsp;from&nbsp;14.215.177.39:&nbsp;seq=16&nbsp;ttl=55&nbsp;time=18.086&nbsp;ms\r\n"</span>,<span class="">"stream"</span>:<span class="">"stdout"</span>,<span class="">"time"</span>:<span class="">"2019-05-16T14:13:57.011235913Z"</span>}<br />{<span class="">"log"</span>:<span class="">"64&nbsp;bytes&nbsp;from&nbsp;14.215.177.39:&nbsp;seq=17&nbsp;ttl=55&nbsp;time=12.615&nbsp;ms\r\n"</span>,<span class="">"stream"</span>:<span class="">"stdout"</span>,<span class="">"time"</span>:<span class="">"2019-05-16T14:13:58.007104112Z"</span>}<br />{<span class="">"log"</span>:<span class="">"64&nbsp;bytes&nbsp;from&nbsp;14.215.177.39:&nbsp;seq=18&nbsp;ttl=55&nbsp;time=11.001&nbsp;ms\r\n"</span>,<span class="">"stream"</span>:<span class="">"stdout"</span>,<span class="">"time"</span>:<span class="">"2019-05-16T14:13:59.007559413Z"</span>}<br /></code></pre>
<h6>日志驱动 三、syslog</h6>
<p>syslog 日志驱动将日志路由到 syslog 服务器，syslog 以原始的字符串作为 日志消息元数据，接收方可以提取以下的消息：</p>
<ul class="list-paddingleft-2">
<li>
<p>level &nbsp;日志等级 ，如<code>debug</code>，<code>warning</code>，<code>error</code>，<code>info</code>。</p>
</li>
<li>
<p>timestamp &nbsp;时间戳</p>
</li>
<li>
<p>hostname &nbsp;事件发生的主机</p>
</li>
<li>
<p>facillty &nbsp;系统模块</p>
</li>
<li>
<p>进程名称和进程 ID &nbsp;</p>
</li>
</ul>
<p><strong><code>syslog</code>&nbsp;日志驱动全局配置</strong></p>
<p>编辑&nbsp;<code>/etc/docker/daemon.json</code>&nbsp;&nbsp;文件</p>
<pre><code class="">{<br />&nbsp;&nbsp;<span class="">"log-driver"</span>:&nbsp;<span class="">"syslog"</span>,<br />&nbsp;&nbsp;<span class="">"log-opts"</span>:&nbsp;{<br />&nbsp;&nbsp;&nbsp;&nbsp;<span class="">"syslog-address"</span>:&nbsp;<span class="">"udp://1.2.3.4:1111"</span><br />&nbsp;&nbsp;}<br />}<br /></code></pre>
<p>重启 docker &nbsp;即可生效。</p>
<table width="676">
<thead>
<tr>
<th width="70">Option</th>
<th width="167">Description</th>
<th width="275">Example value</th>
</tr>
</thead>
<tbody>
<tr>
<td width="109"><code>syslog-address</code></td>
<td width="168">指定syslog 服务所在的服务器和使用的协议和端口。 格式：<code>[tcp|udp|tcp+tls]://host:port,unix://path, orunixgram://path</code>. 默认端口是 514.</td>
<td width="275"><code>--log-opt syslog-address=tcp+tls://192.168.1.3:514</code>,&nbsp;<code>--log-opt syslog-address=unix:///tmp/syslog.sock</code></td>
</tr>
<tr>
<td width="70"><code>syslog-facility</code></td>
<td width="165">使用的&nbsp;<code>syslog</code>&nbsp;的设备， &nbsp;具体设备名称见 syslog documentation.</td>
<td width="275"><code>--log-opt syslog-facility=daemon</code></td>
</tr>
<tr>
<td width="70"><code>syslog-tls-ca-cert</code></td>
<td width="165">如果使用的是&nbsp;<code>tcp+tls</code>的地址，指定CA 证书的地址，如果没有使用，则不设置该选项。</td>
<td width="275"><code>--log-opt syslog-tls-ca-cert=/etc/ca-certificates/custom/ca.pem</code></td>
</tr>
<tr>
<td width="70"><code>syslog-tls-cert</code></td>
<td width="165">如果使用的是&nbsp;<code>tcp+tls</code>的地址，指定 TLS 证书的地址，如果没有使用，则不设置该选项。</td>
<td width="275"><code>--log-opt syslog-tls-cert=/etc/ca-certificates/custom/cert.pem</code></td>
</tr>
<tr>
<td width="70"><code>syslog-tls-key</code></td>
<td width="165">如果使用的是&nbsp;<code>tcp+tls</code>的地址，指定 TLS 证书 key的地址，如果没有使用，则不设置该选项。**</td>
<td width="275"><code>--log-opt syslog-tls-key=/etc/ca-certificates/custom/key.pem</code></td>
</tr>
<tr>
<td width="70"><code>syslog-tls-skip-verify</code></td>
<td width="165">如果设置为 true ，会跳过 TLS 验证，默认为 false</td>
<td width="275"><code>--log-opt syslog-tls-skip-verify=true</code></td>
</tr>
<tr>
<td width="70"><code>tag</code></td>
<td width="165">将应用程序的名称附加到&nbsp;<code>syslog</code>&nbsp;消息中，默认情况下使用容器ID的前12位去 标记这个日志信息。</td>
<td width="275"><code>--log-opt tag=mailer</code></td>
</tr>
<tr>
<td width="70"><code>syslog-format</code></td>
<td width="165"><code>syslog</code>&nbsp;使用的消息格式 如果未指定则使用本地 UNIX syslog 格式，rfc5424micro 格式具有微妙时间戳。</td>
<td width="275"><code>--log-opt syslog-format=rfc5424micro</code></td>
</tr>
<tr>
<td width="100"><code>labels</code></td>
<td width="163">启动 docker 时，配置与日志相关的标签，以逗号分割</td>
<td width="275"><code>--log-opt labels=production_status,geo</code></td>
</tr>
<tr>
<td width="70"><code>env</code></td>
<td width="163">启动 docker 时，指定环境变量用于日志中，以逗号分隔</td>
<td width="275"><code>--log-opt env=os,customer</code></td>
</tr>
<tr>
<td width="70"><code>env-regex</code></td>
<td width="163">类似并兼容&nbsp;<code>env</code>，</td>
<td width="275"><code>--log-opt env-regex=^(os\|customer)</code></td>
</tr>
</tbody>
</table>
<p>**单个容器日志驱动设置为&mdash;syslog **</p>
<p><code>Linux</code>&nbsp;系统中 我们用的系统日志模块时 &nbsp;<code>rsyslog</code>&nbsp;，它是基于<code>syslog</code>&nbsp;的标准实现。我们要使用 syslog 驱动需要使用 系统自带的&nbsp;<code>rsyslog</code>&nbsp;服务。</p>
<pre><code class=""><span class="">#&nbsp;查看当前&nbsp;rsyslog&nbsp;版本和基本信息</span><br />[root@localhost&nbsp;harbor]<span class="">#&nbsp;rsyslogd&nbsp;&nbsp;-v</span><br />rsyslogd&nbsp;8.24.0,&nbsp;compiled&nbsp;with:<br />&nbsp;&nbsp;&nbsp;&nbsp;PLATFORM:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;x86_64-redhat-linux-gnu<br />&nbsp;&nbsp;&nbsp;&nbsp;PLATFORM&nbsp;(lsb_release&nbsp;-d):&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<br />&nbsp;&nbsp;&nbsp;&nbsp;FEATURE_REGEXP:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Yes<br />&nbsp;&nbsp;&nbsp;&nbsp;GSSAPI&nbsp;Kerberos&nbsp;5&nbsp;support:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Yes<br />&nbsp;&nbsp;&nbsp;&nbsp;FEATURE_DEBUG&nbsp;(debug&nbsp;build,&nbsp;slow&nbsp;code):&nbsp;No<br />&nbsp;&nbsp;&nbsp;&nbsp;32bit&nbsp;Atomic&nbsp;operations&nbsp;supported:&nbsp;&nbsp;Yes<br />&nbsp;&nbsp;&nbsp;&nbsp;64bit&nbsp;Atomic&nbsp;operations&nbsp;supported:&nbsp;&nbsp;Yes<br />&nbsp;&nbsp;&nbsp;&nbsp;memory&nbsp;allocator:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;system&nbsp;default<br />&nbsp;&nbsp;&nbsp;&nbsp;Runtime&nbsp;Instrumentation&nbsp;(slow&nbsp;code):&nbsp;&nbsp;&nbsp;&nbsp;No<br />&nbsp;&nbsp;&nbsp;&nbsp;uuid&nbsp;support:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Yes<br />&nbsp;&nbsp;&nbsp;&nbsp;Number&nbsp;of&nbsp;Bits&nbsp;<span class="">in</span>&nbsp;RainerScript&nbsp;integers:&nbsp;64<br /><br />See&nbsp;http://www.rsyslog.com&nbsp;<span class="">for</span>&nbsp;more&nbsp;information.<br /></code></pre>
<p>配置 syslog , 在配置文件&nbsp;<code>/etc/rsyslog.conf</code>&nbsp;&nbsp;大约14-20行，我们可以看到两个配置，一个udp，一个tcp ，都是监听 514 端口，提供 syslog 的接收。选择 tcp 就将 tcp 的两个配置的前面 # 号注释即可。</p>
<pre><code class=""><span class="">#</span><span class="">&nbsp;Provides&nbsp;UDP&nbsp;syslog&nbsp;reception</span><br /><span class="">#</span><span class="">$ModLoad&nbsp;imudp</span><br /><span class="">#</span><span class="">$UDPServerRun&nbsp;514</span><br /><span class=""><br />#</span><span class="">&nbsp;Provides&nbsp;TCP&nbsp;syslog&nbsp;reception</span><br /><span class="">#</span><span class="">$ModLoad&nbsp;imtcp&nbsp;&nbsp;</span><br /><span class="">#</span><span class="">$InputTCPServerRun&nbsp;514</span><br /></code></pre>
<p>然后重启 rsyslog，我们可以看到514端口在监听。</p>
<pre><code class="">systemctl&nbsp;restart&nbsp;&nbsp;rsyslog<br />[root@localhost&nbsp;harbor]<span class="">#&nbsp;netstat&nbsp;-ntul&nbsp;|grep&nbsp;514</span><br />tcp&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;0&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;0&nbsp;0.0.0.0:514&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;0.0.0.0:*&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;LISTEN&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<br />tcp6&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;0&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;0&nbsp;:::514&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;:::*&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;LISTEN&nbsp;&nbsp;<br /></code></pre>
<p>启动一个以 &nbsp;<code>syslog</code>&nbsp;为驱动的容器。</p>
<pre><code class="">docker&nbsp;&nbsp;run&nbsp;-d&nbsp;-it&nbsp;&nbsp;-p&nbsp;87:80&nbsp;--<span class="">log</span>-driver&nbsp;syslog&nbsp;--<span class="">log</span>-opt&nbsp;syslog-address=tcp://127.0.0.1:514&nbsp;&nbsp;--name&nbsp;nginx-syslog&nbsp;&nbsp;&nbsp;nginx<br /></code></pre>
<p>访问并查看日志</p>
<pre><code class=""><span class="">#&nbsp;访问nginx</span><br />curl&nbsp;127.0.0.1:87<br /><span class="">#&nbsp;查看访问日志</span><br />tail&nbsp;-f&nbsp;&nbsp;/var/<span class="">log</span>/messages<br />May&nbsp;17&nbsp;15:56:48&nbsp;localhost&nbsp;fe18924aefde[6141]:&nbsp;172.17.0.1&nbsp;-&nbsp;-&nbsp;[17/May/2019:07:56:48&nbsp;+0000]&nbsp;<span class="">"GET&nbsp;/&nbsp;HTTP/1.1"</span>&nbsp;200&nbsp;612&nbsp;<span class="">"-"</span>&nbsp;<span class="">"curl/7.29.0"</span>&nbsp;<span class="">"-"</span><span class="">#015</span><br />May&nbsp;17&nbsp;15:58:16&nbsp;localhost&nbsp;fe18924aefde[6141]:&nbsp;172.17.0.1&nbsp;-&nbsp;-&nbsp;[17/May/2019:07:58:16&nbsp;+0000]&nbsp;<span class="">"GET&nbsp;/&nbsp;HTTP/1.1"</span>&nbsp;200&nbsp;612&nbsp;<span class="">"-"</span>&nbsp;<span class="">"curl/7.29.0"</span>&nbsp;<span class="">"-"</span><span class="">#015</span><br /></code></pre>
<h6>日志驱动 四、Journald</h6>
<p><code>journald</code>&nbsp;日志驱动程序将容器的日志发送到&nbsp;<code>systemd journal</code>, 可以使用&nbsp;<code>journal API</code>&nbsp;或者使用&nbsp;<code>docker logs</code>&nbsp;来查日志。</p>
<p>除了日志本身以外，&nbsp;<code>journald</code>&nbsp;&nbsp;日志驱动还会在日志加上下面的数据与消息一起储存。</p>
<table width="676">
<thead>
<tr>
<th>Field</th>
<th width="371">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td><code>CONTAINER_ID</code></td>
<td width="381">容器ID,为 12个字符</td>
</tr>
<tr>
<td><code>CONTAINER_ID_FULL</code></td>
<td width="391">完整的容器ID，为64个字符</td>
</tr>
<tr>
<td><code>CONTAINER_NAME</code></td>
<td width="401">启动时容器的名称，如果容器后面更改了名称，日志中的名称不会更改。</td>
</tr>
<tr>
<td><code>CONTAINER_TAG</code>,&nbsp;<code>SYSLOG_IDENTIFIER</code></td>
<td width="411">容器的tag.</td>
</tr>
<tr>
<td><code>CONTAINER_PARTIAL_MESSAGE</code></td>
<td width="421">当日志比较长的时候使用标记来表示(显示日志的大小)</td>
</tr>
</tbody>
</table>
<p>选项</p>
<table width="676">
<thead>
<tr>
<th>选项</th>
<th>是否必须</th>
<th width="291">描述</th>
</tr>
</thead>
<tbody>
<tr>
<td><code>tag</code></td>
<td>可选的</td>
<td width="341">指定要在日志中设置<code>CONTAINER_TAG</code>和<code>SYSLOG_IDENTIFIER</code>值的模板。</td>
</tr>
<tr>
<td><code>labels</code></td>
<td>可选的</td>
<td width="311">以逗号分隔的标签列表，如果为容器指定了这些标签，则应包含在消息中。</td>
</tr>
<tr>
<td><code>env</code></td>
<td>可选的</td>
<td width="321">如果为容器指定了这些变量，则以逗号分隔的环境变量键列表（应包含在消息中）。</td>
</tr>
<tr>
<td><code>env-regex</code></td>
<td>可选的</td>
<td width="331">与env类似并兼容。用于匹配与日志记录相关的环境变量的正则表达式 。</td>
</tr>
</tbody>
</table>
<p><strong><code>journald</code>&nbsp;日志驱动全局配置</strong></p>
<p>编辑&nbsp;<code>/etc/docker/daemon.json</code>&nbsp;&nbsp;文件</p>
<pre><code class="">{<br />&nbsp;&nbsp;<span class="">"log-driver"</span>:&nbsp;<span class="">"journald"</span><br />}<br /></code></pre>
<p><strong>单个容器日志驱动设置为&mdash;</strong><strong><code>journald</code></strong></p>
<pre><code class="">docker&nbsp;&nbsp;run&nbsp;&nbsp;-d&nbsp;-it&nbsp;<span class="">--log-driver=journald&nbsp;\</span><br />&nbsp;&nbsp;&nbsp;&nbsp;<span class="">--log-opt&nbsp;labels=location&nbsp;\</span><br />&nbsp;&nbsp;&nbsp;&nbsp;<span class="">--log-opt&nbsp;env=TEST&nbsp;\</span><br />&nbsp;&nbsp;&nbsp;&nbsp;<span class="">--env&nbsp;"TEST=false"&nbsp;\</span><br />&nbsp;&nbsp;&nbsp;&nbsp;<span class="">--label&nbsp;location=china&nbsp;\</span><br />&nbsp;&nbsp;&nbsp;&nbsp;<span class="">--name&nbsp;&nbsp;nginx-journald\</span><br />&nbsp;&nbsp;&nbsp;&nbsp;-p&nbsp;80:80\<br />&nbsp;&nbsp;&nbsp;&nbsp;nginx<br /></code></pre>
<p>查看日志&nbsp;<code>journalctl</code></p>
<pre><code class=""><span class="">#&nbsp;只查询指定容器的相关消息</span><br />&nbsp;journalctl&nbsp;CONTAINER_NAME=webserver<br /><span class="">#&nbsp;-b&nbsp;指定从上次启动以来的所有消息</span><br />&nbsp;journalctl&nbsp;-b&nbsp;CONTAINER_NAME=webserver<br /><span class="">#&nbsp;-o&nbsp;指定日志消息格式，-o&nbsp;json&nbsp;表示以json&nbsp;格式返回日志消息</span><br />&nbsp;journalctl&nbsp;-o&nbsp;json&nbsp;CONTAINER_NAME=webserver<br /><span class="">#&nbsp;-f&nbsp;一直捕获日志输出</span><br />&nbsp;journalctl&nbsp;-f&nbsp;CONTAINER_NAME=webserver<br /></code></pre>
<blockquote>
<p>如果我们的容器在启动的时候加了 -t 参数，启用了 TTY 的话，那么我查看日志是会像下面一样</p>
<pre><code class="">May&nbsp;17&nbsp;17:19:26&nbsp;localhost.localdomain&nbsp;2a338e4631fe[6141]:&nbsp;[104B&nbsp;blob&nbsp;data]<br />May&nbsp;17&nbsp;17:19:32&nbsp;localhost.localdomain&nbsp;2a338e4631fe[6141]:&nbsp;[104B&nbsp;blob&nbsp;data]<br /></code></pre>
&nbsp;
<p>显示<code>[104B blob data]</code>&nbsp;而不是完整日志原因是因为有&nbsp;<code>\r</code>&nbsp;的存在，如果我们要完整显示，需要加上参数&nbsp;<code>--all</code>&nbsp;。</p>
</blockquote>
<h3><strong>&nbsp;</strong></h3>
<h3><strong>三、 生产环境中该如何储存容器中的日志</strong></h3>
<p><strong>&nbsp;</strong></p>
<p>我们在上面看到了 Docker 官方提供了 很多日志驱动，但是上面的这些驱动都是针对的 标准输出的日志驱动。</p>
<h6>容器日志分类</h6>
<p>容器的日志实际是有两大类的：</p>
<ul class="list-paddingleft-2">
<li>
<p><strong>标准输出的</strong>&nbsp;，也就是 STDOUT 、STDERR ,<strong>这类日志我们可以通过 Docker 官方的日志驱动进行收集。</strong></p>
<p>示例：Nginx 日志，Nginx 日志有&nbsp;<code>access.log</code>&nbsp;和&nbsp;<code>error.log</code>&nbsp;，我们在 Docker Hub 上可以看到 &nbsp;Nginx 的 dockerfile &nbsp;对于这两个日志的处理是：</p>
<pre><code class="">RUN&nbsp;ln&nbsp;-sf&nbsp;/dev/stdout&nbsp;/var/log/nginx/access.log&nbsp;\<br />&nbsp;&nbsp;&amp;&amp;&nbsp;ln&nbsp;-sf&nbsp;/dev/stderr&nbsp;/var/log/nginx/error.log<br /></code></pre>
<p>都软连接到&nbsp;<code>/dev/stdout</code>&nbsp;和&nbsp;<code>/dev/stderr</code>&nbsp;&nbsp;，也就是标准输出，所以这类 容器是可以使用 Docker 官方的日志驱动。</p>
</li>
<li>
<p><strong>文本日志</strong>，存在在于容器内部，并没有重定向到 容器的标准输出的日志。</p>
<p>示例： Tomcat 日志，Tomcat 有 catalina、localhost、manager、admin、host-manager，我们可以在 Docker Hub 看到 Tomcat 的 dockerfile 只有对于 catalina 进行处理，其它日志将储存在容器里。</p>
<pre><code class="">CMD&nbsp;["catalina.sh",&nbsp;"run"]<br /></code></pre>
<p>我们运行了一个 Tomcat 容器 ，然后进行访问后，并登陆到容器内部，我们可以看到产生了文本日志：</p>
<pre><code class="">root@25ba00fdab97:/usr/<span class="">local</span>/tomcat/logs<span class="">#&nbsp;ls&nbsp;-l</span><br />total&nbsp;16<br />-rw-r-----.&nbsp;1&nbsp;root&nbsp;root&nbsp;6822&nbsp;May&nbsp;17&nbsp;14:36&nbsp;catalina.2019-05-17.log<br />-rw-r-----.&nbsp;1&nbsp;root&nbsp;root&nbsp;&nbsp;&nbsp;&nbsp;0&nbsp;May&nbsp;17&nbsp;14:36&nbsp;host-manager.2019-05-17.log<br />-rw-r-----.&nbsp;1&nbsp;root&nbsp;root&nbsp;&nbsp;459&nbsp;May&nbsp;17&nbsp;14:36&nbsp;localhost.2019-05-17.log<br />-rw-r-----.&nbsp;1&nbsp;root&nbsp;root&nbsp;1017&nbsp;May&nbsp;17&nbsp;14:37&nbsp;localhost_access_log.2019-05-17.txt<br />-rw-r-----.&nbsp;1&nbsp;root&nbsp;root&nbsp;&nbsp;&nbsp;&nbsp;0&nbsp;May&nbsp;17&nbsp;14:36&nbsp;manager.2019-05-17.log<br /></code></pre>
<p>这类容器我们下面有专门的方案来应对。</p>
</li>
</ul>
<h4>一、当是完全是标准输出的类型的容器</h4>
<p>我们可以选择 &nbsp;json-file 、syslog、local 等 Docker 支持的日志驱动。</p>
<h4>二、当有文件文本日志的类型容器</h4>
<h6>方案一 挂载目录 &nbsp;bind</h6>
<p>创建一个目录，将目录挂载到 容器中产生日志的目录。</p>
<pre><code class="">--mount&nbsp;&nbsp;<span class="">type</span>=<span class="">bind</span>,src=/opt/logs/,dst=/usr/<span class="">local</span>/tomcat/logs/&nbsp;<br /></code></pre>
<p>示例：</p>
<pre><code class=""><span class="">#&nbsp;创建挂载目录/opt/logs</span><br />[root@fy-local-2&nbsp;/]<span class="">#&nbsp;mkdir&nbsp;&nbsp;/opt/logs</span><br /><span class="">#&nbsp;创建容器tomcat-bind&nbsp;并将&nbsp;/opt/logs&nbsp;挂载至&nbsp;/usr/local/tomcat/logs/</span><br />[root@fy-local-2&nbsp;/]<span class="">#&nbsp;docker&nbsp;&nbsp;run&nbsp;-d&nbsp;&nbsp;--name&nbsp;&nbsp;tomcat-bind&nbsp;&nbsp;-P&nbsp;&nbsp;--mount&nbsp;&nbsp;type=bind,src=/opt/logs/,dst=/usr/local/tomcat/logs/&nbsp;&nbsp;&nbsp;tomcat&nbsp;</span><br />[root@fy-local-2&nbsp;/]<span class="">#&nbsp;ls&nbsp;-l&nbsp;/opt/logs/</span><br />total&nbsp;12<br />-rw-r-----&nbsp;1&nbsp;root&nbsp;root&nbsp;6820&nbsp;May&nbsp;22&nbsp;17:31&nbsp;catalina.2019-05-22.log<br />-rw-r-----&nbsp;1&nbsp;root&nbsp;root&nbsp;&nbsp;&nbsp;&nbsp;0&nbsp;May&nbsp;22&nbsp;17:31&nbsp;host-manager.2019-05-22.log<br />-rw-r-----&nbsp;1&nbsp;root&nbsp;root&nbsp;&nbsp;459&nbsp;May&nbsp;22&nbsp;17:31&nbsp;localhost.2019-05-22.log<br />-rw-r-----&nbsp;1&nbsp;root&nbsp;root&nbsp;&nbsp;&nbsp;&nbsp;0&nbsp;May&nbsp;22&nbsp;17:31&nbsp;localhost_access_log.2019-05-22.txt<br />-rw-r-----&nbsp;1&nbsp;root&nbsp;root&nbsp;&nbsp;&nbsp;&nbsp;0&nbsp;May&nbsp;22&nbsp;17:31&nbsp;manager.2019-05-22.log<br /></code></pre>
<h6>方案二 使用数据卷 volume</h6>
<p>创建数据卷，创建容器时绑定数据卷，</p>
<pre><code class="">--mount&nbsp;&nbsp;<span class="">type</span>=volume&nbsp;&nbsp;src=volume_name&nbsp;&nbsp;dst=/usr/<span class="">local</span>/tomcat/logs/&nbsp;<br /></code></pre>
<p>示例：</p>
<pre><code class=""><span class="">#&nbsp;创建tomcat应用数据卷名称为&nbsp;tomcat</span><br />[root@fy-local-2&nbsp;/]<span class="">#&nbsp;docker&nbsp;volume&nbsp;&nbsp;create&nbsp;&nbsp;tomcat</span><br /><span class="">#&nbsp;创建容器tomcat-volume&nbsp;并指定数据卷为&nbsp;tomcat，绑定至&nbsp;/usr/local/tomcat/logs/</span><br />[root@fy-local-2&nbsp;/]<span class="">#&nbsp;docker&nbsp;&nbsp;run&nbsp;-d&nbsp;&nbsp;--name&nbsp;&nbsp;tomcat-volume&nbsp;&nbsp;&nbsp;-P&nbsp;&nbsp;--mount&nbsp;&nbsp;type=volume,src=tomcat,dst=/usr/local/tomcat/logs/&nbsp;&nbsp;&nbsp;tomcat</span><br /><span class="">#&nbsp;查看数据卷里面的内容</span><br />[root@fy-local-2&nbsp;/]<span class="">#&nbsp;ls&nbsp;-l&nbsp;/var/lib/docker/volumes/tomcat/_data/</span><br />total&nbsp;12<br />-rw-r-----&nbsp;1&nbsp;root&nbsp;root&nbsp;6820&nbsp;May&nbsp;22&nbsp;17:33&nbsp;catalina.2019-05-22.log<br />-rw-r-----&nbsp;1&nbsp;root&nbsp;root&nbsp;&nbsp;&nbsp;&nbsp;0&nbsp;May&nbsp;22&nbsp;17:33&nbsp;host-manager.2019-05-22.log<br />-rw-r-----&nbsp;1&nbsp;root&nbsp;root&nbsp;&nbsp;459&nbsp;May&nbsp;22&nbsp;17:33&nbsp;localhost.2019-05-22.log<br />-rw-r-----&nbsp;1&nbsp;root&nbsp;root&nbsp;&nbsp;&nbsp;&nbsp;0&nbsp;May&nbsp;22&nbsp;17:33&nbsp;localhost_access_log.2019-05-22.txt<br />-rw-r-----&nbsp;1&nbsp;root&nbsp;root&nbsp;&nbsp;&nbsp;&nbsp;0&nbsp;May&nbsp;22&nbsp;17:33&nbsp;manager.2019-05-22.log<br /></code></pre>
<h6>方案三 计算容器 rootfs 挂载点</h6>
<p>此方案的文字内容摘抄于 https://yq.aliyun.com/articles/672054</p>
<p>使用挂载宿主机目录的方式采集日志对应用会有一定的侵入性，因为它要求容器启动的时候包含挂载命令。如果采集过程能对用户透明那就太棒了。事实上，可以通过计算容器 rootfs 挂载点来达到这种目的。</p>
<p>和容器 rootfs 挂载点密不可分的一个概念是 storage driver。实际使用过程中，用户往往会根据 linux 版本、文件系统类型、容器读写情况等因素选择合适的 storage driver。不同 storage driver 下，容器的 rootfs 挂载点遵循一定规律，因此我们可以根据 storage driver 的类型推断出容器的 rootfs 挂载点，进而采集容器内部日志。下表展示了部分 storage dirver 的 rootfs 挂载点及其计算方法。</p>
<table width="676">
<thead>
<tr>
<th width="51">Storage driver</th>
<th width="310">rootfs 挂载点</th>
<th width="98">计算方法</th>
</tr>
</thead>
<tbody>
<tr>
<td width="51">aufs</td>
<td width="311">/var/lib/docker/aufs/mnt/</td>
<td width="108">id 可以从如下文件读到。&nbsp;<code>/var/lib/docker/image/aufs/layerdb/mounts/&lt;container-id&gt;/mount-id</code></td>
</tr>
<tr>
<td width="51">overlay</td>
<td width="311">/var/lib/docker/overlay//merged</td>
<td width="118">完整路径可以通过如下命令得到。&nbsp;<code>docker inspect -f '{{.GraphDriver.Data.MergedDir}}' &lt;container-id&gt;</code></td>
</tr>
<tr>
<td width="51">overlay2</td>
<td width="311">/var/lib/docker/overlay2//merged</td>
<td width="128">完整路径可以通过如下命令得到。&nbsp;<code>docker inspect -f '{{.GraphDriver.Data.MergedDir}}' &lt;container-id&gt;</code></td>
</tr>
<tr>
<td width="82">devicemapper</td>
<td width="311">/var/lib/docker/devicemapper/mnt//rootfs</td>
<td width="138">id 可以通过如下命令得到。&nbsp;<code>docker inspect -f '{{.GraphDriver.Data.DeviceName}}' &lt;container-id&gt;</code></td>
</tr>
</tbody>
</table>
<p>示例：</p>
<pre><code class=""><span class="">#&nbsp;创建容器&nbsp;tomcat-test</span><br />[root@fy-local-2&nbsp;/]<span class="">#&nbsp;docker&nbsp;&nbsp;run&nbsp;-d&nbsp;&nbsp;--name&nbsp;&nbsp;tomcat-test&nbsp;&nbsp;-P&nbsp;&nbsp;tomcat</span><br />36510dd653ae7dcac1d017174b1c38b3f9a226f9c4e329d0ff656cfe041939ff&nbsp;&nbsp;<br /><span class="">#&nbsp;查看tomcat-test&nbsp;容器的&nbsp;挂载点位置</span><br />[root@fy-local-2&nbsp;/]<span class="">#&nbsp;docker&nbsp;inspect&nbsp;-f&nbsp;'{{.GraphDriver.Data.MergedDir}}'&nbsp;36510dd653ae7dcac1d017174b1c38b3f9a226f9c4e329d0ff656cfe041939ff&nbsp;&nbsp;</span><br />/var/lib/docker/overlay2/c10ec54bab8f3fccd2c5f1a305df6f3b1e53068776363ab0c104d253216b799d/merged<br /><span class="">#&nbsp;查看挂载点的目录结构</span><br />[root@fy-local-2&nbsp;/]<span class="">#&nbsp;ls&nbsp;-l&nbsp;/var/lib/docker/overlay2/c10ec54bab8f3fccd2c5f1a305df6f3b1e53068776363ab0c104d253216b799d/merged</span><br />total&nbsp;4<br />drwxr-xr-x&nbsp;1&nbsp;root&nbsp;root&nbsp;&nbsp;179&nbsp;May&nbsp;&nbsp;8&nbsp;13:05&nbsp;bin<br />drwxr-xr-x&nbsp;2&nbsp;root&nbsp;root&nbsp;&nbsp;&nbsp;&nbsp;6&nbsp;Mar&nbsp;28&nbsp;17:12&nbsp;boot<br />drwxr-xr-x&nbsp;1&nbsp;root&nbsp;root&nbsp;&nbsp;&nbsp;43&nbsp;May&nbsp;22&nbsp;17:27&nbsp;dev<br />lrwxrwxrwx&nbsp;1&nbsp;root&nbsp;root&nbsp;&nbsp;&nbsp;33&nbsp;May&nbsp;&nbsp;8&nbsp;13:08&nbsp;docker-java-home&nbsp;-&gt;&nbsp;/usr/lib/jvm/java-8-openjdk-amd64<br />drwxr-xr-x&nbsp;1&nbsp;root&nbsp;root&nbsp;&nbsp;&nbsp;66&nbsp;May&nbsp;22&nbsp;17:27&nbsp;etc<br />drwxr-xr-x&nbsp;2&nbsp;root&nbsp;root&nbsp;&nbsp;&nbsp;&nbsp;6&nbsp;Mar&nbsp;28&nbsp;17:12&nbsp;home<br />drwxr-xr-x&nbsp;1&nbsp;root&nbsp;root&nbsp;&nbsp;&nbsp;&nbsp;6&nbsp;May&nbsp;16&nbsp;08:50&nbsp;lib<br />drwxr-xr-x&nbsp;2&nbsp;root&nbsp;root&nbsp;&nbsp;&nbsp;34&nbsp;May&nbsp;&nbsp;6&nbsp;08:00&nbsp;lib64<br />drwxr-xr-x&nbsp;2&nbsp;root&nbsp;root&nbsp;&nbsp;&nbsp;&nbsp;6&nbsp;May&nbsp;&nbsp;6&nbsp;08:00&nbsp;media<br />drwxr-xr-x&nbsp;2&nbsp;root&nbsp;root&nbsp;&nbsp;&nbsp;&nbsp;6&nbsp;May&nbsp;&nbsp;6&nbsp;08:00&nbsp;mnt<br />drwxr-xr-x&nbsp;2&nbsp;root&nbsp;root&nbsp;&nbsp;&nbsp;&nbsp;6&nbsp;May&nbsp;&nbsp;6&nbsp;08:00&nbsp;opt<br />drwxr-xr-x&nbsp;2&nbsp;root&nbsp;root&nbsp;&nbsp;&nbsp;&nbsp;6&nbsp;Mar&nbsp;28&nbsp;17:12&nbsp;proc<br />drwx------&nbsp;1&nbsp;root&nbsp;root&nbsp;&nbsp;&nbsp;27&nbsp;May&nbsp;22&nbsp;17:29&nbsp;root<br />drwxr-xr-x&nbsp;3&nbsp;root&nbsp;root&nbsp;&nbsp;&nbsp;30&nbsp;May&nbsp;&nbsp;6&nbsp;08:00&nbsp;run<br />drwxr-xr-x&nbsp;2&nbsp;root&nbsp;root&nbsp;4096&nbsp;May&nbsp;&nbsp;6&nbsp;08:00&nbsp;sbin<br />drwxr-xr-x&nbsp;2&nbsp;root&nbsp;root&nbsp;&nbsp;&nbsp;&nbsp;6&nbsp;May&nbsp;&nbsp;6&nbsp;08:00&nbsp;srv<br />drwxr-xr-x&nbsp;2&nbsp;root&nbsp;root&nbsp;&nbsp;&nbsp;&nbsp;6&nbsp;Mar&nbsp;28&nbsp;17:12&nbsp;sys<br />drwxrwxrwt&nbsp;1&nbsp;root&nbsp;root&nbsp;&nbsp;&nbsp;29&nbsp;May&nbsp;16&nbsp;08:50&nbsp;tmp<br />drwxr-xr-x&nbsp;1&nbsp;root&nbsp;root&nbsp;&nbsp;&nbsp;19&nbsp;May&nbsp;&nbsp;6&nbsp;08:00&nbsp;usr<br />drwxr-xr-x&nbsp;1&nbsp;root&nbsp;root&nbsp;&nbsp;&nbsp;41&nbsp;May&nbsp;&nbsp;6&nbsp;08:00&nbsp;var<br /><span class="">#&nbsp;查看日志</span><br />[root@fy-local-2&nbsp;/]<span class="">#&nbsp;ls&nbsp;-l&nbsp;/var/lib/docker/overlay2/c10ec54bab8f3fccd2c5f1a305df6f3b1e53068776363ab0c104d253216b799d/merged/usr/local/tomcat/logs/</span><br />total&nbsp;20<br />-rw-r-----&nbsp;1&nbsp;root&nbsp;root&nbsp;14514&nbsp;May&nbsp;22&nbsp;17:40&nbsp;catalina.2019-05-22.log<br />-rw-r-----&nbsp;1&nbsp;root&nbsp;root&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;0&nbsp;May&nbsp;22&nbsp;17:27&nbsp;host-manager.2019-05-22.log<br />-rw-r-----&nbsp;1&nbsp;root&nbsp;root&nbsp;&nbsp;1194&nbsp;May&nbsp;22&nbsp;17:40&nbsp;localhost.2019-05-22.log<br />-rw-r-----&nbsp;1&nbsp;root&nbsp;root&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;0&nbsp;May&nbsp;22&nbsp;17:27&nbsp;localhost_access_log.2019-05-22.txt<br />-rw-r-----&nbsp;1&nbsp;root&nbsp;root&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;0&nbsp;May&nbsp;22&nbsp;17:27&nbsp;manager.2019-05-22.log<br /></code></pre>
<h6>方案四 &nbsp;在代码层中实现直接将日志写入redis</h6>
<p>docker &nbsp;&mdash;&mdash;》redis &mdash;&mdash;》Logstash&mdash;&mdash;》Elasticsearch</p>
<p>通过代码层面，直接将日志写入<code>redis</code>,最后写入&nbsp;<code>Elasticsearch</code>。</p>
<p>以上就是对 Docker 日志的所有的概念解释和方提供，具体采用什么方案，根据公司的具体的业务来选择。合适的才是最好的。</p>
</div>