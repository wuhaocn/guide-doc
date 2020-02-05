<div class="post">
<h1 class="postTitle">
<a id="cb_post_title_url" class="postTitle2" href="https://www.cnblogs.com/heyonggang/p/12123991.html">【ZooKeeper系列】3.ZooKeeper源码环境搭建</a>
</h1>

<div id="cnblogs_post_body" class="blogpost-body cnblogs-markdown">
<p><strong>前文阅读</strong>：<br>
<a href="https://www.cnblogs.com/heyonggang/p/12048060.html">【ZooKeeper系列】1.ZooKeeper单机版、伪集群和集群环境搭建</a><br>
<a href="https://www.cnblogs.com/heyonggang/p/12058313.html">【ZooKeeper系列】2.用Java实现ZooKeeper API的调用</a></p>
<p>在系列的前两篇文章中，介绍了ZooKeeper环境的搭建（包括单机版、伪集群和集群），对创建、删除、修改节点等场景用命令行的方式进行了测试，让大家对ZooKeeper环境搭建及常用命令行有初步的认识，也为搭建ZooKeeper的开发环境、生产环境起到了抛砖引玉的作用。也介绍了用Java来实现API的调用，包括节点的增、删、改、查。通过对这两篇的学习，让大家对ZooKeeper的使用有了初步认识，也可用于实现系列后面篇章要介绍的命名服务、集群管理、分布式锁、负载均衡、分布式队列等。</p>
<p>在前两篇中，强调了阅读英文文档的重要性，也带领大家解读了部分官方文档，想传达出的理念是ZooKeeper没有想象中的那么难，阅读官方文档也没那么难。后面的篇章中，结合官方文档，在实战演练和解读源码的基础上加深理解。</p>
<blockquote>
<p>上联：说你行你就行不行也行<br>
下联：说不行就不行行也不行<br>
横批：不服不行<br>
<code>阅读源码就跟这个对联一模一样，就看你选上联，还是下联了！</code></p>
</blockquote>
<p>这一篇开始源码环境的搭建，<code>here we go</code>！</p>
<p>很多老铁留言说很想研读些github上的开源项目，但代码clone下来后总出现这样或那样奇奇怪怪的问题，很影响学习的积极性。学习ZooKeeper的源码尤其如此，很多人clone代码后，报各种错，提示少各种包。问了下度娘ZooKeeper源码环境，搜出来的文章真的差强人意，有些文章错的竟然非常离谱。这里我重新搭建了一遍，也会介绍遇到的一些坑。</p>
<p>很多老铁上来一堆猛操作，从github上下载了ZooKeeper源码后，按常规方式导入IDEA，最后发现少各种包。起初我也是这样弄的，以为ZooKeeper是用Maven来构建的，仔细去了解了下ZooKeeper的版本历史，其实是用的Ant。如今一般用的Maven或Gradle，很少见到Ant的项目了，这里不对Ant多做介绍。</p>
<h2 id="ant环境搭建">1 Ant环境搭建</h2>
<p>Ant官网地址：<a href="https://ant.apache.org/bindownload.cgi" class="uri">https://ant.apache.org/bindownload.cgi</a></p>
<p>下载解压后，跟配置jdk一样配置几个环境变量：</p>
<pre class="xml"><code class="hljs">//修改为自己本地安装的目录
ANT_HOMT=D:\apache-ant-1.10.7
PATH=%ANT_HOME%/bin
CLASSPATH=%ANT_HOME%/lib</code></pre>
<p>配置好后，测试下Ant是否安装成功。<strong>ant -version</strong>,得到如下信息则代表安装成功：</p>
<pre class="xml"><code class="hljs">Apache Ant(TM) version 1.10.7 compiled on September 1 2019</code></pre>
<p>Ant的安装跟JDK的安装和配置非常相似，这里不做过多介绍。</p>
<h2 id="下载zookeeper源码">2 下载ZooKeeper源码</h2>
<p>源码地址：<a href="https://github.com/apache/zookeeper" class="uri">https://github.com/apache/zookeeper</a></p>
<p>猿人谷在写本篇文章时，releases列表里的最新版本为<code>release-3.5.6</code>，我们以此版本来进行源码环境的搭建。</p>
<h2 id="编译zookeeper源码">3 编译ZooKeeper源码</h2>
<p>切换到源码所在目录，运行<code>ant eclipse</code>将项目编译并转成eclipse的项目结构。<br>
<img src="https://img-blog.csdnimg.cn/20191230152211788.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly95dWFucmVuZ3UuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70" alt="在这里插入图片描述"><br>
这个编译过程会比较长，差不多等了7分钟。如果编译成功，会出现如下结果：<br>
<img src="https://img-blog.csdnimg.cn/20191230152345614.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly95dWFucmVuZ3UuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70" alt="在这里插入图片描述"></p>
<h2 id="导入idea">4 导入IDEA</h2>
<p>上面已经将项目编译并转成eclipse的项目结构，按eclipse的形式导入项目。<br>
<img src="https://img-blog.csdnimg.cn/20191230152519906.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly95dWFucmVuZ3UuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70" alt="在这里插入图片描述"></p>
<p><img src="https://img-blog.csdnimg.cn/20191230152653707.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly95dWFucmVuZ3UuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70" alt="在这里插入图片描述"></p>
<h2 id="特别说明">5 特别说明</h2>
<p>将源码导入IDEA后在<code>org.apache.zookeeper.Version</code>中发现很多红色警告，很明显少了<code>org.apache.zookeeper.version.Info</code>类。<br>
<img src="https://img-blog.csdnimg.cn/20191230155040951.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly95dWFucmVuZ3UuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70" alt="在这里插入图片描述"><br>
查询源码得知是用来发布的时候生成版本用的，我们只是研读源码，又不发布版本所以直接写死就ok了。<br>
<img src="https://img-blog.csdnimg.cn/20191230155439579.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly95dWFucmVuZ3UuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70" alt="在这里插入图片描述"><br>
即新增Info类：</p>
<pre class="java"><code class="hljs"><span class="hljs-keyword">package</span> org.apache.zookeeper.version;

<span class="hljs-keyword">public</span> <span class="hljs-class"><span class="hljs-keyword">interface</span> <span class="hljs-title">Info</span> </span>{
<span class="hljs-keyword">int</span> MAJOR = <span class="hljs-number">3</span>;
<span class="hljs-keyword">int</span> MINOR = <span class="hljs-number">5</span>;
<span class="hljs-keyword">int</span> MICRO = <span class="hljs-number">6</span>;
String QUALIFIER = <span class="hljs-keyword">null</span>;
String REVISION_HASH = <span class="hljs-string">"c11b7e26bc554b8523dc929761dd28808913f091"</span>;
String BUILD_DATE = <span class="hljs-string">"10/08/2019 20:18 GMT"</span>;
}</code></pre>
<h2 id="启动zookeeper">6 启动zookeeper</h2>
<p>针对单机版本和集群版本，分别对应两个启动类：</p>
<ul>
<li>单机：ZooKeeperServerMain</li>
<li>集群：QuorumPeerMain</li>
</ul>
<p>这里我们只做单机版的测试。</p>
<p><strong>在conf目录里有个zoo_sample.cfg，复制一份重命名为zoo.cfg</strong>。</p>
<p>zoo.cfg里的内容做点修改（也可以不做修改），方便日志查询。dataDir和dataLogDir根据自己的情况设定。</p>
<pre class="xml"><code class="hljs">dataDir=E:\\02private\\1opensource\\zk\\zookeeper\\dataDir
dataLogDir=E:\\02private\\1opensource\\zk\\zookeeper\\dataLogDir</code></pre>
<p>运行主类 <code>org.apache.zookeeper.server.ZooKeeperServerMain</code>，将zoo.cfg的完整路径配置在Program arguments。<br>
<img src="https://img-blog.csdnimg.cn/20191230153718683.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly95dWFucmVuZ3UuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70" alt="在这里插入图片描述"><br>
运行<code>ZooKeeperServerMain</code>，得到的结果如下：</p>
<pre class="xml"><code class="hljs">Connected to the target VM, address: '127.0.0.1:0', transport: 'socket'
log4j:WARN No appenders could be found for logger (org.apache.zookeeper.jmx.ManagedUtil).
log4j:WARN Please initialize the log4j system properly.
log4j:WARN See http://logging.apache.org/log4j/1.2/faq.html#noconfig for more info.</code></pre>
<p>告知日志无法输出，日志文件配置有误。这里需要指定日志文件log4j.properties。<br>
<img src="https://img-blog.csdnimg.cn/2019123015440042.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly95dWFucmVuZ3UuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70" alt="在这里插入图片描述"><br>
在VM options配置，即指定到conf目录下的log4j.properties：</p>
<pre class="xml"><code class="hljs">-Dlog4j.configuration=file:E:/02private/1opensource/zk/zookeeper/conf/log4j.properties</code></pre>
<p>配置后重新运行<code>ZooKeeperServerMain</code>，输出日志如下，<br>
<img src="https://img-blog.csdnimg.cn/20191230154623937.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly95dWFucmVuZ3UuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70" alt="在这里插入图片描述"><br>
可以得知单机版启动成功，单机版服务端地址为127.0.0.1:2181。</p>
<h2 id="启动客户端">7 启动客户端</h2>
<p>通过运行<code>ZooKeeperServerMain</code>得到的日志，可以得知ZooKeeper服务端已经启动，服务的地址为<code>127.0.0.1:2181</code>。启动客户端来进行连接测试。</p>
<p>客户端的启动类为<code>org.apache.zookeeper.ZooKeeperMain</code>，进行如下配置：<br>
<img src="https://img-blog.csdnimg.cn/20191230175008781.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly95dWFucmVuZ3UuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70" alt="在这里插入图片描述"><br>
即客户端连接127.0.0.1:2181，获取节点<code>/yuanrengu</code>的信息。</p>
<p>下面带领大家一起看看客户端启动的源码（<code>org.apache.zookeeper.ZooKeeperMain</code>）。<strong>这里要给大家说下我阅读源码的习惯，很多老铁以为阅读源码就是顺着代码看，这样也没啥不对，只是很多开源项目代码量惊人，这么个干看法，容易注意力分散也容易看花眼。我一般是基于某个功能点，从入口开始debug跑一遍，弄清这个功能的“代码线”，就像跑马圈块地儿一样，弄清楚功能有关的代码，了解参数传递的过程，这样看代码时就更有针对性，也能排除很多干扰代码。</strong></p>
<h3 id="main">7.1 main</h3>
<p>main里就两行代码，通过debug得知args里包含的信息就是上面我们配置在<strong>Program arguments</strong>里的信息：<br>
<img src="https://img-blog.csdnimg.cn/20191231095122793.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly95dWFucmVuZ3UuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70" alt="在这里插入图片描述"></p>
<h4 id="zookeepermain">7.1.1 ZooKeeperMain</h4>
<pre class="java"><code class="hljs">    <span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-title">ZooKeeperMain</span><span class="hljs-params">(String args[])</span> <span class="hljs-keyword">throws</span> IOException, InterruptedException </span>{
<span class="hljs-comment">// 用于解析参数里的命令行的</span>
cl.parseOptions(args);
System.out.println(<span class="hljs-string">"Connecting to "</span> + cl.getOption(<span class="hljs-string">"server"</span>));
<span class="hljs-comment">// 用于连接ZooKeeper服务端</span>
connectToZK(cl.getOption(<span class="hljs-string">"server"</span>));
}</code></pre>
<p>通过下图可以看出，解析参数后，就尝试连接127.0.0.1:2181，即ZooKeeper服务端。cl.getOption("server")得到的就是127.0.0.1:2181。<br>
<img src="https://img-blog.csdnimg.cn/20191231100610526.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly95dWFucmVuZ3UuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70" alt="在这里插入图片描述"></p>
<h4 id="parseoptions">7.1.2 parseOptions</h4>
<p><img src="https://img-blog.csdnimg.cn/20191231114421581.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly95dWFucmVuZ3UuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70" alt="在这里插入图片描述"><br>
<strong>可以很清楚的得知解析args的过程，主要从"-server"，"-timeout"，"-r"，"-"这几个维度来进行解析。</strong></p>
<h4 id="connecttozk">7.1.3 connectToZK</h4>
<pre class="java"><code class="hljs">    <span class="hljs-function"><span class="hljs-keyword">protected</span> <span class="hljs-keyword">void</span> <span class="hljs-title">connectToZK</span><span class="hljs-params">(String newHost)</span> <span class="hljs-keyword">throws</span> InterruptedException, IOException </span>{
<span class="hljs-comment">// 用于判断现在ZooKeeper连接是否还有效</span>
<span class="hljs-comment">// zk.getState().isAlive() 注意这个会话是否有效的判断，客户端与 Zookeeper连接断开不一定会话失效</span>
<span class="hljs-keyword">if</span> (zk != <span class="hljs-keyword">null</span> &amp;&amp; zk.getState().isAlive()) {
zk.close();
}

<span class="hljs-comment">// 此时newHost为127.0.0.1:2181</span>
host = newHost;
<span class="hljs-comment">// 判断是否为只读模式，关于只读模式的概念在前一篇文章中有介绍</span>
<span class="hljs-keyword">boolean</span> readOnly = cl.getOption(<span class="hljs-string">"readonly"</span>) != <span class="hljs-keyword">null</span>;
<span class="hljs-comment">// 用于判断是否建立安全连接</span>
<span class="hljs-keyword">if</span> (cl.getOption(<span class="hljs-string">"secure"</span>) != <span class="hljs-keyword">null</span>) {
System.setProperty(ZKClientConfig.SECURE_CLIENT, <span class="hljs-string">"true"</span>);
System.out.println(<span class="hljs-string">"Secure connection is enabled"</span>);
}
zk = <span class="hljs-keyword">new</span> ZooKeeperAdmin(host, Integer.parseInt(cl.getOption(<span class="hljs-string">"timeout"</span>)), <span class="hljs-keyword">new</span> MyWatcher(), readOnly);
}</code></pre>
<p><code>ZKClientConfig.SECURE_CLIENT</code>已经被标注为deprecation了：</p>
<pre class="java"><code class="hljs">    <span class="hljs-comment">/**
* Setting this to "true" will enable encrypted client-server communication.
*/</span>
<span class="hljs-meta">@SuppressWarnings</span>(<span class="hljs-string">"deprecation"</span>)
<span class="hljs-keyword">public</span> <span class="hljs-keyword">static</span> <span class="hljs-keyword">final</span> String SECURE_CLIENT = ZooKeeper.SECURE_CLIENT;</code></pre>
<p>debug查看关键点处的信息，可以得知这是建立一个ZooKeeper连接的过程（<a href="https://www.cnblogs.com/heyonggang/p/12058313.html">【ZooKeeper系列】2.用Java实现ZooKeeper API的调用</a>,这篇文章里详细介绍过ZooKeeper建立连接的过程）</p>
<p>下图看看几处关键信息：<br>
<img src="https://img-blog.csdnimg.cn/20191231101754442.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly95dWFucmVuZ3UuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70" alt="在这里插入图片描述"><br>
Integer.parseInt(cl.getOption("timeout"))为30000。</p>
<p>至此完成了ZooKeeperMain main = new ZooKeeperMain(args);的整个过程。简短点说就是：</p>
<ol>
<li>解析Program arguments里的参数</li>
<li>连接ZooKeeper服务端</li>
</ol>
<h3 id="main.run">7.2 main.run()</h3>
<p><strong>敲黑板，重头戏来了哦！</strong></p>
<p>一起来看下run()的代码：</p>
<pre class="java"><code class="hljs">    <span class="hljs-function"><span class="hljs-keyword">void</span> <span class="hljs-title">run</span><span class="hljs-params">()</span> <span class="hljs-keyword">throws</span> CliException, IOException, InterruptedException </span>{
<span class="hljs-comment">// cl.getCommand()得到的是 “get”，就是上文传进来的</span>
<span class="hljs-keyword">if</span> (cl.getCommand() == <span class="hljs-keyword">null</span>) {
System.out.println(<span class="hljs-string">"Welcome to ZooKeeper!"</span>);

<span class="hljs-keyword">boolean</span> jlinemissing = <span class="hljs-keyword">false</span>;
<span class="hljs-comment">// only use jline if it's in the classpath</span>
<span class="hljs-keyword">try</span> {
Class&lt;?&gt; consoleC = Class.forName(<span class="hljs-string">"jline.console.ConsoleReader"</span>);
Class&lt;?&gt; completorC =
Class.forName(<span class="hljs-string">"org.apache.zookeeper.JLineZNodeCompleter"</span>);

System.out.println(<span class="hljs-string">"JLine support is enabled"</span>);

Object console =
consoleC.getConstructor().newInstance();

Object completor =
completorC.getConstructor(ZooKeeper.class).newInstance(zk);
Method addCompletor = consoleC.getMethod(<span class="hljs-string">"addCompleter"</span>,
Class.forName(<span class="hljs-string">"jline.console.completer.Completer"</span>));
addCompletor.invoke(console, completor);

String line;
Method readLine = consoleC.getMethod(<span class="hljs-string">"readLine"</span>, String.class);
<span class="hljs-keyword">while</span> ((line = (String)readLine.invoke(console, getPrompt())) != <span class="hljs-keyword">null</span>) {
executeLine(line);
}
} <span class="hljs-keyword">catch</span> (ClassNotFoundException e) {
LOG.debug(<span class="hljs-string">"Unable to start jline"</span>, e);
jlinemissing = <span class="hljs-keyword">true</span>;
} <span class="hljs-keyword">catch</span> (NoSuchMethodException e) {
LOG.debug(<span class="hljs-string">"Unable to start jline"</span>, e);
jlinemissing = <span class="hljs-keyword">true</span>;
} <span class="hljs-keyword">catch</span> (InvocationTargetException e) {
LOG.debug(<span class="hljs-string">"Unable to start jline"</span>, e);
jlinemissing = <span class="hljs-keyword">true</span>;
} <span class="hljs-keyword">catch</span> (IllegalAccessException e) {
LOG.debug(<span class="hljs-string">"Unable to start jline"</span>, e);
jlinemissing = <span class="hljs-keyword">true</span>;
} <span class="hljs-keyword">catch</span> (InstantiationException e) {
LOG.debug(<span class="hljs-string">"Unable to start jline"</span>, e);
jlinemissing = <span class="hljs-keyword">true</span>;
}

<span class="hljs-keyword">if</span> (jlinemissing) {
System.out.println(<span class="hljs-string">"JLine support is disabled"</span>);
BufferedReader br =
<span class="hljs-keyword">new</span> BufferedReader(<span class="hljs-keyword">new</span> InputStreamReader(System.in));

String line;
<span class="hljs-keyword">while</span> ((line = br.readLine()) != <span class="hljs-keyword">null</span>) {
executeLine(line);
}
}
} <span class="hljs-keyword">else</span> {
<span class="hljs-comment">// 处理传进来的参数</span>
processCmd(cl);
}
System.exit(exitCode);
}</code></pre>
<p>通过下图可以看出<code>processCmd(cl);</code>里<code>cl</code>包含的信息：<br>
<img src="https://img-blog.csdnimg.cn/20191231103551902.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly95dWFucmVuZ3UuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70" alt="在这里插入图片描述"><br>
debug到<code>processCmd(MyCommandOptions co)</code> 就到了决战时刻。里面的<code>processZKCmd(MyCommandOptions co)</code>就是核心了，代码太长，只说下processZKCmd里的重点代码，获取节点/yuanrengu的信息：<br>
<img src="https://img-blog.csdnimg.cn/20191231105710706.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly95dWFucmVuZ3UuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70" alt="在这里插入图片描述"><br>
因为我之前没有创建过/yuanrengu节点，会抛异常<code>org.apache.zookeeper.KeeperException$NoNodeException: KeeperErrorCode = NoNode for /yuanrengu</code> ， 如下图所示：<br>
<img src="https://img-blog.csdnimg.cn/2019123111251754.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly95dWFucmVuZ3UuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70" alt="在这里插入图片描述"><br>
经过上面的步骤后exitCode为1，执行System.exit(exitCode);退出。</p>
<p>至此带领大家dubug了一遍org.apache.zookeeper.ZooKeeperMain，上面我说过，阅读源码干看效果很小，只有debug才能有助于梳理流程和思路，也能清楚参数传递的过程发生了什么变化。</p>
<h2 id="温馨提示">温馨提示</h2>
<p>上面我们介绍了源码环境的搭建过程，运行运行主类 <code>org.apache.zookeeper.server.ZooKeeperServerMain</code> 启动ZooKeeper服务端，运行<code>org.apache.zookeeper.ZooKeeperMain</code>连接服务端。</p>
<p><strong>阅读源码最好能动起来(debug)读，这样代码才是活的，干看的话代码如死水一样，容易让人索然无味！</strong></p>
<p>每个人操作的方式不一样，有可能遇到的问题也不一样，搭建过程中遇到什么问题，大家可以在评论区留言。</p>

</div>
<div id="MySignature" style="display: block;"><div style="display: block" id="MySignature">
<p style="border: 2px solid #DEEBF7; padding-top: 10px; padding-right: 10px; padding-bottom: 10px; padding-left: 10px; background: #fff7dc no-repeat 1% 50%; height: 100px; font-family: 微软雅黑; font-size: 12px;" id="PSignature">
<img style="margin-right: 20px; width: 100px; height: 100px; float: left;" src="https://images.cnblogs.com/cnblogs_com/heyonggang/957279/o_qrcode_for_gh_96c5e991bd62_430.jpg">
<br>
微信公众号：
<a target="_blank" href="https://images.cnblogs.com/cnblogs_com/heyonggang/957279/o_qrcode_for_gh_96c5e991bd62_430.jpg">猿人谷 </a>
<br>
如果您认为阅读这篇博客让您有些收获，不妨点击一下右下角的【推荐】
<br>
如果您希望与我交流互动，欢迎关注微信公众号
<br>
本文版权归作者和博客园共有，欢迎转载，但未经作者同意必须保留此段声明，且在文章页面明显位置给出原文连接。
</p><p></p>
</div></div>
<div class="clear"></div>
<div id="blog_post_info_block"><div id="BlogPostCategory">
分类:
<a href="https://www.cnblogs.com/heyonggang/category/1614282.html" target="_blank">ZooKeeper</a></div>


