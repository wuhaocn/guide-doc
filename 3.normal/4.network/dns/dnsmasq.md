<div id="content" class="mw-body" role="main">
<a id="top"></a>

<div class="mw-indicators mw-body-content">
</div>

<h1 id="firstHeading" class="firstHeading" lang="en">dnsmasq (简体中文)</h1>

<div id="bodyContent" class="mw-body-content">
<div id="siteSub" class="noprint">From ArchWiki</div>
<div id="contentSub"></div>



<div id="jump-to-nav"></div>
<a class="mw-jump-link" href="#mw-head">Jump to navigation</a>
<a class="mw-jump-link" href="#p-search">Jump to search</a>
<div id="mw-content-text" lang="en" dir="ltr" class="mw-content-ltr"><div class="mw-parser-output"><p><span></span>
</p>
<div style="padding: 5px; margin: 0.50em 0; background-color: #DDDDFF; border: thin solid #BBBBDD; overflow: hidden;"><strong> 翻译状态： </strong>本文是英文页面 <a href="/index.php/Dnsmasq" title="Dnsmasq">Dnsmasq</a> 的<a href="/index.php/ArchWiki_Translation_Team_(%E7%AE%80%E4%BD%93%E4%B8%AD%E6%96%87)" class="mw-redirect" title="ArchWiki Translation Team (简体中文)">翻译</a>，最后翻译时间：2016-07-24，点击<a rel="nofollow" class="external text" href="https://wiki.archlinux.org/index.php?title=Dnsmasq&amp;diff=0&amp;oldid=442246">这里</a>可以查看翻译后英文页面的改动。</div>
<p><a rel="nofollow" class="external text" href="http://www.thekelleys.org.uk/dnsmasq/doc.html">Dnsmasq</a> 提供 DNS 缓存和 DHCP 服务功能。作为域名解析服务器(DNS)，dnsmasq可以通过缓存 DNS 请求来提高对访问过的网址的连接速度。作为DHCP 服务器，<span class="plainlinks archwiki-template-pkg"><a rel="nofollow" class="external text" href="https://www.archlinux.org/packages/?name=dnsmasq">dnsmasq</a></span> 可以用于为局域网电脑分配内网ip地址和提供路由。DNS和DHCP两个功能可以同时或分别单独实现。dnsmasq轻量且易配置，适用于个人用户或少于50台主机的网络。此外它还自带了一个 <a href="/index.php/PXE_(%E7%AE%80%E4%BD%93%E4%B8%AD%E6%96%87)" title="PXE (简体中文)">PXE</a> 服务器。
</p>
<div id="toc" class="toc"><input type="checkbox" role="button" id="toctogglecheckbox" class="toctogglecheckbox" style="display:none"><div class="toctitle" lang="en" dir="ltr"><h2>Contents</h2><span class="toctogglespan"><label class="toctogglelabel" for="toctogglecheckbox"></label></span></div>
<ul>
<li class="toclevel-1 tocsection-1"><a href="#安装"><span class="tocnumber">1</span> <span class="toctext">安装</span></a></li>
<li class="toclevel-1 tocsection-2"><a href="#配置"><span class="tocnumber">2</span> <span class="toctext">配置</span></a></li>
<li class="toclevel-1 tocsection-3"><a href="#DNS_缓存设置"><span class="tocnumber">3</span> <span class="toctext">DNS 缓存设置</span></a>
<ul>
<li class="toclevel-2 tocsection-4"><a href="#DNS_地址文件"><span class="tocnumber">3.1</span> <span class="toctext">DNS 地址文件</span></a>
<ul>
<li class="toclevel-3 tocsection-5"><a href="#resolv.conf"><span class="tocnumber">3.1.1</span> <span class="toctext">resolv.conf</span></a>
<ul>
<li class="toclevel-4 tocsection-6"><a href="#三个以上域名服务器"><span class="tocnumber">3.1.1.1</span> <span class="toctext">三个以上域名服务器</span></a></li>
</ul>
</li>
<li class="toclevel-3 tocsection-7"><a href="#使用dhcpcd"><span class="tocnumber">3.1.2</span> <span class="toctext">使用dhcpcd</span></a></li>
<li class="toclevel-3 tocsection-8"><a href="#使用dhclient"><span class="tocnumber">3.1.3</span> <span class="toctext">使用dhclient</span></a></li>
</ul>
</li>
<li class="toclevel-2 tocsection-9"><a href="#使用NetworkManager"><span class="tocnumber">3.2</span> <span class="toctext">使用NetworkManager</span></a>
<ul>
<li class="toclevel-3 tocsection-10"><a href="#IPv6"><span class="tocnumber">3.2.1</span> <span class="toctext">IPv6</span></a></li>
<li class="toclevel-3 tocsection-11"><a href="#其他方式"><span class="tocnumber">3.2.2</span> <span class="toctext">其他方式</span></a></li>
</ul>
</li>
</ul>
</li>
<li class="toclevel-1 tocsection-12"><a href="#DHCP_服务器设置"><span class="tocnumber">4</span> <span class="toctext">DHCP 服务器设置</span></a></li>
<li class="toclevel-1 tocsection-13"><a href="#启动守护进程"><span class="tocnumber">5</span> <span class="toctext">启动守护进程</span></a></li>
<li class="toclevel-1 tocsection-14"><a href="#测试"><span class="tocnumber">6</span> <span class="toctext">测试</span></a>
<ul>
<li class="toclevel-2 tocsection-15"><a href="#DNS_缓存"><span class="tocnumber">6.1</span> <span class="toctext">DNS 缓存</span></a></li>
<li class="toclevel-2 tocsection-16"><a href="#DHCP_服务器"><span class="tocnumber">6.2</span> <span class="toctext">DHCP 服务器</span></a></li>
</ul>
</li>
<li class="toclevel-1 tocsection-17"><a href="#小技巧"><span class="tocnumber">7</span> <span class="toctext">小技巧</span></a>
<ul>
<li class="toclevel-2 tocsection-18"><a href="#阻止_OpenDNS_重定向_Google_请求"><span class="tocnumber">7.1</span> <span class="toctext">阻止 OpenDNS 重定向 Google 请求</span></a></li>
<li class="toclevel-2 tocsection-19"><a href="#查看租约"><span class="tocnumber">7.2</span> <span class="toctext">查看租约</span></a></li>
<li class="toclevel-2 tocsection-20"><a href="#添加自定义域"><span class="tocnumber">7.3</span> <span class="toctext">添加自定义域</span></a></li>
<li class="toclevel-2 tocsection-21"><a href="#Override_addresses"><span class="tocnumber">7.4</span> <span class="toctext">Override addresses</span></a></li>
<li class="toclevel-2 tocsection-22"><a href="#多个_Dnsmasq"><span class="tocnumber">7.5</span> <span class="toctext">多个 Dnsmasq</span></a>
<ul>
<li class="toclevel-3 tocsection-23"><a href="#静态"><span class="tocnumber">7.5.1</span> <span class="toctext">静态</span></a></li>
<li class="toclevel-3 tocsection-24"><a href="#动态"><span class="tocnumber">7.5.2</span> <span class="toctext">动态</span></a></li>
</ul>
</li>
</ul>
</li>
</ul>
</div>

<h2><span id=".E5.AE.89.E8.A3.85"></span><span class="mw-headline" id="安装">安装</span></h2>
<p>从<a href="/index.php/%E5%AE%98%E6%96%B9%E4%BB%93%E5%BA%93" class="mw-redirect" title="官方仓库">官方仓库</a>中<a href="/index.php/%E5%AE%89%E8%A3%85" class="mw-redirect" title="安装">安装</a> <span class="plainlinks archwiki-template-pkg"><a rel="nofollow" class="external text" href="https://www.archlinux.org/packages/?name=dnsmasq">dnsmasq</a></span>。
</p>
<h2><span id=".E9.85.8D.E7.BD.AE"></span><span class="mw-headline" id="配置">配置</span></h2>
<p>编辑  dnsmasq 的配置文件 <code>/etc/dnsmasq.conf</code> 。这个文件包含大量的选项注释。 
</p>
<div style="padding: 5px; margin: 0.50em 0; background-color: #FFDDDD; border: thin solid #DDBBBB; overflow: hidden;"><strong> 警告: </strong>dnsmasq 默认启用其 DNS 服务器。如果不需要，必须明确地将其 DNS 端口设置为 <code>0</code> 禁用它：
<pre style="margin-bottom: 0; border-bottom:none; padding-bottom:0.8em;">/etc/dnsmasq.conf</pre>
<pre style="margin-top: 0; border-top-style:dashed; padding-top: 0.8em;">port=0</pre>
</div>
<div style="padding: 5px; margin: 0.50em 0; background-color: #DDFFDD; border: thin solid #BBDDBB; overflow: hidden;"><strong> 提示： </strong>查看配置文件语法是否正确，可执行下列命令：
<pre>$ dnsmasq --test
</pre>
</div>
<p><br>
</p>
<h2><span id="DNS_.E7.BC.93.E5.AD.98.E8.AE.BE.E7.BD.AE"></span><span class="mw-headline" id="DNS_缓存设置">DNS 缓存设置</span></h2>
<p>要在单台电脑上以守护进程方式启动dnsmasq做DNS缓存服务器，编辑<code>/etc/dnsmasq.conf</code>，添加监听地址：
</p>
<pre>listen-address=127.0.0.1
</pre>
<p>如果用此主机为局域网提供默认 DNS，请用为该主机绑定固定 IP 地址，设置：
</p>
<pre>listen-address=192.168.x.x
</pre>
<p>这种情况建议配置静态IP
</p><p>多个ip地址设置:
</p>
<pre>listen-address=127.0.0.1,192.168.x.x 
</pre>
<h3><span id="DNS_.E5.9C.B0.E5.9D.80.E6.96.87.E4.BB.B6"></span><span class="mw-headline" id="DNS_地址文件">DNS 地址文件</span></h3>
<div class="noprint archwiki-template-message">
<p><a href="/index.php/File:Merge-arrows-2.png" class="image"><img alt="Merge-arrows-2.png" src="/images/c/c9/Merge-arrows-2.png" decoding="async" width="48" height="48"></a><b>This article or section is a candidate for merging with <a href="/index.php/Resolv.conf" class="mw-redirect" title="Resolv.conf">resolv.conf</a>.</b><a href="/index.php/File:Merge-arrows-2.png" class="image"><img alt="Merge-arrows-2.png" src="/images/c/c9/Merge-arrows-2.png" decoding="async" width="48" height="48"></a></p>
<div><b>Notes:</b> 主题相同。本文或本节候选与<a href="/index.php/Resolv.conf" class="mw-redirect" title="Resolv.conf">resolv.conf</a>合并。此处绝大部分功能可以在原生的<code>/etc/resolvconf.conf</code>文件中通过配置<code>name_servers</code> 和 <code>name_servers_append</code>选项实现。 (Discuss in <a rel="nofollow" class="external text" href="https://wiki.archlinux.org/index.php/Talk:Dnsmasq_(%E7%AE%80%E4%BD%93%E4%B8%AD%E6%96%87)">Talk:Dnsmasq (简体中文)#</a>)</div>
</div>
<p>在配置好dnsmasq后，你需要编辑<code>/etc/resolv.conf</code>让DHCP客户端首先将本地地址(localhost)加入 DNS 文件(<code>/etc/resolv.conf</code>)，然后再通过其他DNS服务器解析地址。配置好DHCP客户端后需要重新启动网络来使设置生效。
</p>
<h4><span class="mw-headline" id="resolv.conf">resolv.conf</span></h4>
<p>一种选择是一个纯粹的 <code>resolv.conf</code> 配置。要做到这一点，才使第一个域名服务器在<code>/etc/resolv.conf</code> 中指向localhost：
</p>
<pre style="margin-bottom: 0; border-bottom:none; padding-bottom:0.8em;">/etc/resolv.conf</pre>
<pre style="margin-top: 0; border-top-style:dashed; padding-top: 0.8em;">nameserver 127.0.0.1
# External nameservers
...
</pre>
<p>现在，DNS查询将首先解析dnsmasq，只检查外部的服务器如果DNSMasq无法解析查询. <span class="plainlinks archwiki-template-pkg"><a rel="nofollow" class="external text" href="https://www.archlinux.org/packages/?name=dhcpcd">dhcpcd</a></span>, 不幸的是，往往默认覆盖 <code>/etc/resolv.conf</code>, 所以如果你使用DHCP，这里有一个好主意来保护 <code>/etc/resolv.conf</code>,要做到这一点，追加 <code>nohook resolv.conf</code>到dhcpcd的配置文件：
</p>
<pre style="margin-bottom: 0; border-bottom:none; padding-bottom:0.8em;">/etc/dhcpcd.conf</pre>
<pre style="margin-top: 0; border-top-style:dashed; padding-top: 0.8em;">...
nohook resolv.conf</pre>
<p>也可以保护您的resolv.conf不被修改：
</p>
<pre># chattr +i /etc/resolv.conf
</pre>
<h5><span id=".E4.B8.89.E4.B8.AA.E4.BB.A5.E4.B8.8A.E5.9F.9F.E5.90.8D.E6.9C.8D.E5.8A.A1.E5.99.A8"></span><span class="mw-headline" id="三个以上域名服务器">三个以上域名服务器</span></h5>
<p>Linux 处理 DNS 请求时有个限制，在 <code>resolv.conf</code> 中最多只能配置三个域名服务器（nameserver）。作为一种变通方法,可以在 <code>resolv.conf</code> 文件中只保留 localhost 作为域名服务器，然后为外部域名服务器另外创建 <code>resolv-file</code> 文件。首先，为 dnsmasq 新建一个域名解析文件：
</p>
<pre style="margin-bottom: 0; border-bottom:none; padding-bottom:0.8em;">/etc/resolv.dnsmasq.conf</pre>
<pre style="margin-top: 0; border-top-style:dashed; padding-top: 0.8em;"># Google's nameservers, for example
nameserver 8.8.8.8
nameserver 8.8.4.4
</pre>
<p>然后编辑 <code>/etc/dnsmasq.conf</code> 让 dnsmasq 使用新创建的域名解析文件：
</p>
<pre style="margin-bottom: 0; border-bottom:none; padding-bottom:0.8em;">/etc/dnsmasq.conf</pre>
<pre style="margin-top: 0; border-top-style:dashed; padding-top: 0.8em;">...
resolv-file=/etc/resolv.dnsmasq.conf
...
</pre>
<h4><span id=".E4.BD.BF.E7.94.A8dhcpcd"></span><span class="mw-headline" id="使用dhcpcd">使用dhcpcd</span></h4>
<p><span class="plainlinks archwiki-template-pkg"><a rel="nofollow" class="external text" href="https://www.archlinux.org/packages/?name=dhcpcd">dhcpcd</a></span> 可以是通过创建（或编辑）<code>/etc/resolv.conf.head</code>文件或 <code>/etc/resolv.conf.tail</code>文件来指定dns服务器，使<code>/etc/resolv.conf</code>不会被每次都被dhcpcd重写
</p>
<pre>echo "nameserver 127.0.0.1" &gt; /etc/resolv.conf.head #设置dns服务器为127.0.0.1
</pre>
<h4><span id=".E4.BD.BF.E7.94.A8dhclient"></span><span class="mw-headline" id="使用dhclient">使用dhclient</span></h4>
<p>要使用 dhclient， 取消 <code>/etc/dhclient.conf</code> 文件中如下行的注释：
</p>
<pre>prepend domain-name-servers 127.0.0.1;
</pre>
<h3><span id=".E4.BD.BF.E7.94.A8NetworkManager"></span><span class="mw-headline" id="使用NetworkManager">使用NetworkManager</span></h3>
<p><a href="/index.php/NetworkManager" title="NetworkManager">NetworkManager</a> 可以靠自身配置文件的设置项启动 <i>dnsmasq</i> 。在 <code>NetworkManager.conf</code> 文件的 <code>[main]</code> 节段添加 <code>dns=dnsmasq</code> 配置语句，然后禁用由 <a href="/index.php/Systemd" title="Systemd">systemd</a> 启动的 <code>dnsmasq.service</code>:
</p>
<pre style="margin-bottom: 0; border-bottom:none; padding-bottom:0.8em;">/etc/NetworkManager/NetworkManager.conf</pre>
<pre style="margin-top: 0; border-top-style:dashed; padding-top: 0.8em;">[main]
plugins=keyfile
dns=dnsmasq
</pre>
<p>可以在 <code>/etc/NetworkManager/dnsmasq.d/</code> 目录下为 <i>dnsmasq</i> 创建自定义配置文件。例如，调整 DNS 缓存大小（保存在内存中）：
</p>
<pre style="margin-bottom: 0; border-bottom:none; padding-bottom:0.8em;">/etc/NetworkManager/dnsmasq.d/cache</pre>
<pre style="margin-top: 0; border-top-style:dashed; padding-top: 0.8em;">cache-size=1000</pre>
<p><i>dnsmasq</i> 被 <code>NetworkManager</code> 启动后，此目录下配置文件中的配置将取代默认配置。
</p>
<div style="padding: 5px; margin: 0.50em 0; background-color: #DDFFDD; border: thin solid #BBDDBB; overflow: hidden;"><strong> 提示： </strong>这种方法可以让你启用特定域名的自定义DNS设置。例如: <code>server=/example1.com/exemple2.com/xx.xxx.xxx.x</code> 改变第一个DNS地址，浏览以下网站<code>example1.com, example2.com</code>使用<code>xx.xxx.xxx.xx</code>。This method is preferred to a global DNS configuration when using particular DNS nameservers which lack of speed, stability, privacy and security.</div>
<p><br>
</p>
<h4><span class="mw-headline" id="IPv6">IPv6</span></h4>
<p>启用 <code>dnsmasq</code> 在 NetworkManager 可能会中断仅持IPv6的DNS查询 (例如 <code>dig -6 [hostname]</code>) 否则将工作。 为了解决这个问题，创建以下文件将配置 <i>dnsmasq</i> 总是监听IPv6的loopback：
</p>
<pre style="margin-bottom: 0; border-bottom:none; padding-bottom:0.8em;">/etc/NetworkManager/dnsmasq.d/ipv6_listen.conf</pre>
<pre style="margin-top: 0; border-top-style:dashed; padding-top: 0.8em;">listen-address=::1</pre>
<p>此外， <code>dnsmasq</code>不优先考虑上游IPv6的DNS。不幸的是NetworkManager已不这样做 (<a rel="nofollow" class="external text" href="https://bugs.launchpad.net/ubuntu/+source/network-manager/+bug/936712">Ubuntu Bug</a>)。 
一种解决方法是将禁用IPv4 DNS的NetworkManager的配置，假设存在。
</p>
<h4><span id=".E5.85.B6.E4.BB.96.E6.96.B9.E5.BC.8F"></span><span class="mw-headline" id="其他方式">其他方式</span></h4>
<p>另一种选择是在NetworkManagers“设置（通常通过右键单击小程序）和手动输入设置。设置将取决于前端中使用的类型;这个过程通常涉及右击小程序，编辑（或创建）一个配置文件，然后选择DHCP类型为“自动（指定地址）。”DNS地址将需要输入，通常以这种形式：<code>127.0.0.1, DNS-server-one, ...</code>.
</p>
<h2><span id="DHCP_.E6.9C.8D.E5.8A.A1.E5.99.A8.E8.AE.BE.E7.BD.AE"></span><span class="mw-headline" id="DHCP_服务器设置">DHCP 服务器设置</span></h2>
<p>dnsmasq默认关闭DHCP功能，如果该主机需要为局域网中的其他设备提供IP和路由，应该对dnsmasq 配置文件(<code>/etc/dnsmasq.conf</code>)必要的配置如下：
</p>
<pre># Only listen to routers' LAN NIC.  Doing so opens up tcp/udp port 53 to
# localhost and udp port 67 to world:
interface=&lt;LAN-NIC&gt;

# dnsmasq will open tcp/udp port 53 and udp port 67 to world to help with
# dynamic interfaces (assigning dynamic ips). Dnsmasq will discard world
# requests to them, but the paranoid might like to close them and let the 
# kernel handle them:
bind-interfaces

# Dynamic range of IPs to make available to LAN pc
dhcp-range=192.168.111.50,192.168.111.100,12h

# If you’d like to have dnsmasq assign static IPs, bind the LAN computer's
# NIC MAC address:
dhcp-host=aa:bb:cc:dd:ee:ff,192.168.111.50
</pre>
<h2><span id=".E5.90.AF.E5.8A.A8.E5.AE.88.E6.8A.A4.E8.BF.9B.E7.A8.8B"></span><span class="mw-headline" id="启动守护进程">启动守护进程</span></h2>
<p>设置为开机启动：
</p>
<pre># systemctl enable dnsmasq.service
</pre>
<p>立即启动 dnsmasq：
</p>
<pre># systemctl start dnsmasq.service
</pre>
<p>查看dnsmasq是否启动正常，查看系统日志：
</p>
<pre># journalctl -u dnsmasq.service
</pre>
<p>需要重启网络服务以使 DHCP 客户端重建一个新的 <code>/etc/resolv.conf</code>。
</p>
<h2><span id=".E6.B5.8B.E8.AF.95"></span><span class="mw-headline" id="测试">测试</span></h2>
<h3><span id="DNS_.E7.BC.93.E5.AD.98"></span><span class="mw-headline" id="DNS_缓存">DNS 缓存</span></h3>
<p>要测试查询速度，请访问一个 dnsmasq 启动后没有访问过的网站，执行 (<code>dig</code> (位于 <span class="plainlinks archwiki-template-pkg"><a rel="nofollow" class="external text" href="https://www.archlinux.org/packages/?name=bind-tools">bind-tools</a></span> 软件包):
</p>
<pre>$ dig archlinux.org | grep "Query time"
</pre>
<p>再次运行命令，因为使用了缓存，查询时间应该大大缩短。
</p>
<h3><span id="DHCP_.E6.9C.8D.E5.8A.A1.E5.99.A8"></span><span class="mw-headline" id="DHCP_服务器">DHCP 服务器</span></h3>
<p>从一个连接到使用了 dnsmasq 的计算机的计算机，配置它使用 DHCP 自动获取 IP 地址，然后尝试连接到你平时使用的网络。
</p>
<h2><span id=".E5.B0.8F.E6.8A.80.E5.B7.A7"></span><span class="mw-headline" id="小技巧">小技巧</span></h2>
<h3><span id=".E9.98.BB.E6.AD.A2_OpenDNS_.E9.87.8D.E5.AE.9A.E5.90.91_Google_.E8.AF.B7.E6.B1.82"></span><span class="mw-headline" id="阻止_OpenDNS_重定向_Google_请求">阻止 OpenDNS 重定向 Google 请求</span></h3>
<p>要避免 OpenDNS 重定向所有 Google 请求到他们自己的搜索服务器，添加以下内容到 <code>/etc/dnsmasq.conf</code>：
</p>
<pre>server=/www.google.com/&lt;ISP DNS IP&gt;
</pre>
<p>用你的互联网服务供应商（ISP）的 DNS 服务器/路由器的 IP 替换 &lt;ISP DNS IP&gt; 。
</p>
<div class="archwiki-template-box archwiki-template-box-note"><strong>Note:</strong> 因为众所周知的原因，ISP的DNS服务器可能会污染或劫持Google的DNS <a rel="nofollow" class="external autonumber" href="https://github.com/lifetyper/FreeRouter/wiki/3-DNS%E6%B1%A1%E6%9F%93%E4%B8%8E%E5%BA%94%E5%AF%B9">[1]</a>，请自行搜索解决办法。</div>
<h3><span id=".E6.9F.A5.E7.9C.8B.E7.A7.9F.E7.BA.A6"></span><span class="mw-headline" id="查看租约">查看租约</span></h3>
<pre>cat /var/lib/misc/dnsmasq.leases
</pre>
<h3><span id=".E6.B7.BB.E5.8A.A0.E8.87.AA.E5.AE.9A.E4.B9.89.E5.9F.9F"></span><span class="mw-headline" id="添加自定义域">添加自定义域</span></h3>
<p>它可以将一个自定义域添加到主机中的（本地）网络：
</p>
<pre>local=/home.lan/
domain=home.lan
</pre>
<p>在这个例子中可以ping主机/设备 (例如:您的主机文件中的定义) <code>hostname.home.lan</code>.
</p><p>取消扩展主机添加自定义域的主机条目：存在
</p>
<pre>expand-hosts
</pre>
<p>如果没有这个设置，你必须将域添加到 <code>/etc/hosts</code> 中。
</p>
<h3><span class="mw-headline" id="Override_addresses">Override addresses</span></h3>
<p>In some cases, such as when operating a captive portal, it can be useful to resolve specific domains names to a hard-coded set of addresses. This is done with the <code>address</code> config:
</p>
<pre>address=/example.com/1.2.3.4
</pre>
<p>Furthermore, it's possible to return a specific address for all domain names that are not answered from <code>/etc/hosts</code> or DHCP by using a special wildcard:
</p>
<pre>address=/#/1.2.3.4
</pre>
<h3><span id=".E5.A4.9A.E4.B8.AA_Dnsmasq"></span><span class="mw-headline" id="多个_Dnsmasq">多个 Dnsmasq</span></h3>
<h4><span id=".E9.9D.99.E6.80.81"></span><span class="mw-headline" id="静态">静态</span></h4>
<p>要让每个 interface 有独立的 dnsmasq，用 <code>interface</code> 和 <code>bind-interface</code> 选项来实现。
</p>
<h4><span id=".E5.8A.A8.E6.80.81"></span><span class="mw-headline" id="动态">动态</span></h4>
<p>像下面这样可以指定排除某个 interface，而其他的 interface 将会拥有各自的 dnsmasq。
</p>
<pre>except-interface=lo
bind-dynamic
</pre>
<div class="archwiki-template-box archwiki-template-box-note"><strong>Note:</strong> <a href="/index.php/Libvirt" title="Libvirt">libvirt</a> 默认就是这样做的。</div>
<!-- 
NewPP limit report
Cached time: 20200320045058
Cache expiry: 86400
Dynamic content: false
Complications: []
CPU time usage: 0.052 seconds
Real time usage: 0.056 seconds
Preprocessor visited node count: 685/1000000
Preprocessor generated node count: 0/1000000
Post‐expand include size: 10558/2097152 bytes
Template argument size: 5371/2097152 bytes
Highest expansion depth: 9/40
Expensive parser function count: 0/100
Unstrip recursion depth: 0/20
Unstrip post‐expand size: 673/5000000 bytes
-->
<!--
Transclusion expansion time report (%,ms,calls,template)
100.00%   25.202      1 -total
18.10%    4.561      1 Template:警告
15.89%    4.004      1 Template:TranslationStatus_(简体中文)
13.96%    3.519      2 Template:提示
13.31%    3.355      4 Template:META_Box
11.42%    2.877      1 Template:Merge
10.83%    2.729     44 Template:Ic
10.48%    2.640      1 Template:META_Box_Red
9.78%    2.464      1 Template:META_Box_Blue
7.70%    1.941      8 Template:Hc
-->

<!-- Saved in parser cache with key archwiki:pcache:idhash:10385-0!canonical and timestamp 20200320045058 and revision id 572803
-->
</div></div>

<div class="printfooter">Retrieved from "<a dir="ltr" href="https://wiki.archlinux.org/index.php?title=Dnsmasq_(简体中文)&amp;oldid=572803">https://wiki.archlinux.org/index.php?title=Dnsmasq_(简体中文)&amp;oldid=572803</a>"</div>

<div id="catlinks" class="catlinks" data-mw="interface"><div id="mw-normal-catlinks" class="mw-normal-catlinks"><a href="/index.php/Special:Categories" title="Special:Categories">Categories</a>: <ul><li><a href="/index.php/Category:%E7%AE%80%E4%BD%93%E4%B8%AD%E6%96%87" title="Category:简体中文">简体中文</a></li><li><a href="/index.php/Category:Domain_Name_System_(%E7%AE%80%E4%BD%93%E4%B8%AD%E6%96%87)" title="Category:Domain Name System (简体中文)">Domain Name System (简体中文)</a></li></ul></div><div id="mw-hidden-catlinks" class="mw-hidden-catlinks mw-hidden-cats-hidden">Hidden category: <ul><li><a href="/index.php/Category:Pages_or_sections_flagged_with_Template:Merge" title="Category:Pages or sections flagged with Template:Merge">Pages or sections flagged with Template:Merge</a></li></ul></div></div>
<div class="visualClear"></div>

</div>
</div>