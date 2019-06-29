<h2 id="docker-compose-的安装及使用">docker-compose 的安装及使用</h2>
<h3 id="简单介绍-1">简单介绍</h3>
<blockquote>
<p>Docker Compose 是一个用来定义和运行复杂应用的 Docker 工具。<br />使用 Docker Compose 不再需要使用 shell 脚本来启动容器。(通过 docker-compose.yml 配置)</p>
</blockquote>
<h3 id="安装-1">安装</h3>
<p>可以通过修改 URL 中的版本，自定义您需要的版本。</p>
<ul>
<li>Github源</li>
</ul>
<pre><code class="hljs awk">sudo curl -L https:<span class="hljs-regexp">//gi</span>thub.com<span class="hljs-regexp">/docker/</span>compose<span class="hljs-regexp">/releases/</span>download<span class="hljs-regexp">/1.22.0/</span>docker-compose-`uname -s`-`uname -m` -o <span class="hljs-regexp">/usr/</span>local<span class="hljs-regexp">/bin/</span>docker-compose
sudo chmod +x <span class="hljs-regexp">/usr/</span>local<span class="hljs-regexp">/bin/</span>docker-compose</code></pre>
<ul>
<li>Daocloud镜像</li>
</ul>
<pre class="sh"><code class="hljs bash">curl -L https://get.daocloud.io/docker/compose/releases/download/1.22.0/docker-compose-`uname -s`-`uname -m` &gt; /usr/<span class="hljs-built_in">local</span>/bin/docker-compose
chmod +x /usr/<span class="hljs-built_in">local</span>/bin/docker-compose</code><br /><br /><strong>测试<br />&nbsp;&nbsp;&nbsp;</strong></pre>
<p class="p1"><span class="s1"> docker-compose -v </span></p>
<h3 id="卸载-1">卸载</h3>
<pre><code class="hljs groovy">sudo rm <span class="hljs-regexp">/usr/</span>local<span class="hljs-regexp">/bin/</span>docker-compose</code></pre>
<h3 id="基础命令-1">基础命令</h3>
<p>需要在 docker-compose.yml 所在文件夹中执行命令</p>
<p>使用 docker-compose 部署项目的简单步骤</p>
<ul>
<li>停止现有 docker-compose 中的容器：<code>docker-compose down</code></li>
<li>重新拉取镜像：<code>docker-compose pull</code></li>
<li>后台启动 docker-compose 中的容器：<code>docker-compose up -d</code></li>
</ul>
<h3 id="通过-docker-compose.yml-部署应用">通过 docker-compose.yml 部署应用</h3>
<p>我将上面所创建的镜像推送到了阿里云，在此使用它</p>
<h4 id="新建-docker-compose.yml-文件">1.新建 docker-compose.yml 文件</h4>
<p>通过以下配置，在运行后可以创建两个站点(只为演示)</p>
<pre><code class="hljs less"><span class="hljs-attribute">version</span>: <span class="hljs-string">"3"</span>
<span class="hljs-attribute">services</span>:
  <span class="hljs-attribute">web1</span>:
    <span class="hljs-attribute">image</span>: registry.cn-hangzhou.aliyuncs.com/yimo_public/<span class="hljs-attribute">docker-nginx-test</span>:latest
    <span class="hljs-attribute">ports</span>:
      - <span class="hljs-string">"4466:80"</span>
  <span class="hljs-attribute">web2</span>:
    <span class="hljs-attribute">image</span>: registry.cn-hangzhou.aliyuncs.com/yimo_public/<span class="hljs-attribute">docker-nginx-test</span>:latest
    <span class="hljs-attribute">ports</span>:
      - <span class="hljs-string">"4477:80"</span></code></pre>
<p>此处只是简单演示写法，说明 docker-compose 的方便</p>
<h4 id="构建完成后台运行镜像">2.构建完成，后台运行镜像</h4>
<pre><code class="hljs">docker-compose up -d</code></pre>
<p>运行后就可以使用 ip+port 访问这两个站点了</p>
<h4 id="镜像更新重新部署">3.镜像更新重新部署</h4>
<pre><code class="hljs">docker-compose down
docker-compose pull
docker-compose up -d</code><br /><br /><br />参考：<br /><a href="https://www.cnblogs.com/morang/p/9501223.html">https://www.cnblogs.com/morang/p/9501223.html</a></pre>