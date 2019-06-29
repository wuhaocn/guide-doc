<div class="postTitle">
<div class="postTitle"><a id="cb_post_title_url" class="postTitle2" href="https://www.cnblogs.com/xinghen1216/p/9914007.html">f5源站获取http/https访问的真实源IP解决方案</a></div>
<div id="cnblogs_post_body" class="blogpost-body">
<p>1.背景</p>
<p align="left">　　F5负载均衡设备，很多场景下需要采用旁挂的方式部署。为了保证访问到源站的数据流的request和response的TCP路径一致，f5采用了snat机制。但是这样导致源站上看到的来源IP都是snat地址，而看不到真实的访问源地址。</p>
<p align="left">2.http访问</p>
<p align="left">　　对于HTTP应用可以直接在VS中开启X-Forwarded规则</p>
<p align="left">　　<img src="https://img2018.cnblogs.com/blog/1274431/201811/1274431-20181106104548688-925072878.png" alt="" />&nbsp; &nbsp;&nbsp;<img src="https://img2018.cnblogs.com/blog/1274431/201811/1274431-20181106104613886-164823228.png" alt="" /></p>
<p>3.https访问　　</p>
<p align="left">　　由于HTTPS应用到达F5的数据都是密文，F5只能看到网络层的地址，4&mdash;7层的内容无法看到，所以F5也无法像http应用一样将客户端地址插入到X_forward_for字段；</p>
<p align="left">　　目前HTTPS应用的加解密工作都是由服务器自身完成的，为了保证F5能够看到4&mdash;7层的数据，需要将加解密工作交给F5来做：</p>
<p align="left">　　1）根证书和KEY文件导入F5设备，F5代替服务器同用户端建立SSL通道;</p>
<p align="left">　　2）F5将加密数据解密后通过X_forward_for功能插入用户端源IP</p>
<p align="left">　　3）业务部门将服务器上的443端口更改为80端口即取消证书加解密工作</p>
<p align="left">　　此过程在原有服务器上进行证书撤销操作，会影响到应用中断，建议重新搭建2台提供相同业务的80端口服务器；</p>
<p align="left">　　4）F5设备上配置一个测试VS关联新搭建的80服务器及SSL策略，验证F5是否可以成功发布HTTPS业务、HTTPS业务插入源地址等功能。即新建测试的vip,访问端口为443，关联SSL加解密策略，后端关联80的POOL。</p>
<p align="left">&nbsp;　　VS中</p>
<p align="left">　　<img src="https://img2018.cnblogs.com/blog/1274431/201811/1274431-20181106105024676-798990548.png" alt="" width="458" height="274" /></p>
<p>4.非http/https　　</p>
<p align="left">&nbsp;　　对于TCP协议则需要通过TCP OPTION来实现客户需求</p>
<p align="left">　　TCP&nbsp; Options需要配合IRULSE+TCP Profile来实现。</p>
<p align="left">　　Irulse（当服务建立起连接时，转换客户端的ip地址并以点&rdquo;.&rdquo;划分为四个部分，在TCP报头中插入kind为29类型的字段。设置变量，输出log信息。</p>
<p align="left">　　当服务建立起连接时，转换客户端的ip地址并以点&rdquo;.&rdquo;划分为四个部分，在TCP报头中插入kind为29类型的字段。设置变量，输出log信息）：</p>
<p align="left">　　1）irule　　</p>
<div class="cnblogs_code">
<div class="cnblogs_code_toolbar"><span class="cnblogs_code_copy"><a title="复制代码"><img src="https://common.cnblogs.com/images/copycode.gif" alt="复制代码" /></a></span></div>
<pre>when SERVER_CONNECTED {

scan [IP::client_addr] {%d.%d.%d.%d} a b c d

TCP::option set 29 [binary format cccc $a $b $c $d] all

set a [binary format cccc $a $b $c $d]

log "insert ip to tcp option $a"

}</pre>
<div class="cnblogs_code_toolbar"><span class="cnblogs_code_copy"><a title="复制代码"><img src="https://common.cnblogs.com/images/copycode.gif" alt="复制代码" /></a></span></div>
</div>
<p>　　2）Tcp Profile案例（需要在tmsh下运行，其中tcplh3为手动创建的tcp profile名称）：</p>
<p align="left">　　create ltm profile tcp tcplh3 tcp-options &ldquo;{8 first} {28 last}&rdquo;</p>
<p align="left">　　步骤：</p>
<p align="left">　　1）模拟业务环境，在F5中建立Pool，建立VS，VS关联特定的pool，关联上述指定的iRule脚本和profile；</p>
<p align="left">&nbsp;　　<img src="https://img2018.cnblogs.com/blog/1274431/201811/1274431-20181106105332826-1137946516.png" alt="" width="559" height="401" /><img src="https://img2018.cnblogs.com/blog/1274431/201811/1274431-20181106105401336-347911660.png" alt="" /></p>
<p>　　3）用SshClient登录F5的命令行，运行tcpdump抓取数据包，同时用客户端多次访问VS地址。</p>
<p align="left">　　抓包命令：tcpdump -s0 -ni 0.0:nnnp host 10.160.100.49 and port 25 -w /var/tmp/test_0907.pcap</p>
<p align="left">　　即，需要抓10.160.100.49的25端口，保存目录为/var/tmp/，保存文件为test.pcap。</p>
<p align="left">　　抓包结果如下：</p>
<p>&nbsp;　　<img src="https://img2018.cnblogs.com/blog/1274431/201811/1274431-20181106105451993-2132504016.png" alt="" width="419" height="95" /></p>
<p>　　<img src="https://img2018.cnblogs.com/blog/1274431/201811/1274431-20181106105510221-1754720988.png" alt="" width="575" height="383" /></p>
<p>　　其中1d=29，表示tcp类型为29；06表示字节数</p>
<p align="left">　　后边的十六进制转换为十进制后即为真实源地址</p>
<p align="left">　　此时即表明f5已经成功将真实源地址插入到option字段，具体如何读取需服务器端配置。</p>
<p align="left">&nbsp;　　<img src="https://img2018.cnblogs.com/blog/1274431/201811/1274431-20181106105539728-34017466.png" alt="" /></p>
<p>　　TCP三次握手</p>
<p align="left">　　注：option是在传输层，业务服务器默认只读网络层</p>
</div>
<div id="MySignature">看看天上，于是我去了满是风雪的地方</div>
</div>
<div id="cnblogs_post_body" class="blogpost-body">
<p>1.背景</p>
<p align="left">　　F5负载均衡设备，很多场景下需要采用旁挂的方式部署。为了保证访问到源站的数据流的request和response的TCP路径一致，f5采用了snat机制。但是这样导致源站上看到的来源IP都是snat地址，而看不到真实的访问源地址。</p>
<p align="left">2.http访问</p>
<p align="left">　　对于HTTP应用可以直接在VS中开启X-Forwarded规则</p>
<p align="left">　　<img src="https://img2018.cnblogs.com/blog/1274431/201811/1274431-20181106104548688-925072878.png" alt="" />&nbsp; &nbsp;&nbsp;<img src="https://img2018.cnblogs.com/blog/1274431/201811/1274431-20181106104613886-164823228.png" alt="" /></p>
<p>3.https访问　　</p>
<p align="left">　　由于HTTPS应用到达F5的数据都是密文，F5只能看到网络层的地址，4&mdash;7层的内容无法看到，所以F5也无法像http应用一样将客户端地址插入到X_forward_for字段；</p>
<p align="left">　　目前HTTPS应用的加解密工作都是由服务器自身完成的，为了保证F5能够看到4&mdash;7层的数据，需要将加解密工作交给F5来做：</p>
<p align="left">　　1）根证书和KEY文件导入F5设备，F5代替服务器同用户端建立SSL通道;</p>
<p align="left">　　2）F5将加密数据解密后通过X_forward_for功能插入用户端源IP</p>
<p align="left">　　3）业务部门将服务器上的443端口更改为80端口即取消证书加解密工作</p>
<p align="left">　　此过程在原有服务器上进行证书撤销操作，会影响到应用中断，建议重新搭建2台提供相同业务的80端口服务器；</p>
<p align="left">　　4）F5设备上配置一个测试VS关联新搭建的80服务器及SSL策略，验证F5是否可以成功发布HTTPS业务、HTTPS业务插入源地址等功能。即新建测试的vip,访问端口为443，关联SSL加解密策略，后端关联80的POOL。</p>
<p align="left">&nbsp;　　VS中</p>
<p align="left">　　<img src="https://img2018.cnblogs.com/blog/1274431/201811/1274431-20181106105024676-798990548.png" alt="" width="458" height="274" /></p>
<p>4.非http/https　　</p>
<p align="left">&nbsp;　　对于TCP协议则需要通过TCP OPTION来实现客户需求</p>
<p align="left">　　TCP&nbsp; Options需要配合IRULSE+TCP Profile来实现。</p>
<p align="left">　　Irulse（当服务建立起连接时，转换客户端的ip地址并以点&rdquo;.&rdquo;划分为四个部分，在TCP报头中插入kind为29类型的字段。设置变量，输出log信息。</p>
<p align="left">　　当服务建立起连接时，转换客户端的ip地址并以点&rdquo;.&rdquo;划分为四个部分，在TCP报头中插入kind为29类型的字段。设置变量，输出log信息）：</p>
<p align="left">　　1）irule　　</p>
<div class="cnblogs_code">
<div class="cnblogs_code_toolbar"><span class="cnblogs_code_copy"><a title="复制代码"><img src="https://common.cnblogs.com/images/copycode.gif" alt="复制代码" /></a></span></div>
<pre>when SERVER_CONNECTED {

scan [IP::client_addr] {%d.%d.%d.%d} a b c d

TCP::option set 29 [binary format cccc $a $b $c $d] all

set a [binary format cccc $a $b $c $d]

log "insert ip to tcp option $a"

}</pre>
<div class="cnblogs_code_toolbar"><span class="cnblogs_code_copy"><a title="复制代码"><img src="https://common.cnblogs.com/images/copycode.gif" alt="复制代码" /></a></span></div>
</div>
<p>　　2）Tcp Profile案例（需要在tmsh下运行，其中tcplh3为手动创建的tcp profile名称）：</p>
<p align="left">　　create ltm profile tcp tcplh3 tcp-options &ldquo;{8 first} {28 last}&rdquo;</p>
<p align="left">　　步骤：</p>
<p align="left">　　1）模拟业务环境，在F5中建立Pool，建立VS，VS关联特定的pool，关联上述指定的iRule脚本和profile；</p>
<p align="left">&nbsp;　　<img src="https://img2018.cnblogs.com/blog/1274431/201811/1274431-20181106105332826-1137946516.png" alt="" width="559" height="401" /><img src="https://img2018.cnblogs.com/blog/1274431/201811/1274431-20181106105401336-347911660.png" alt="" /></p>
<p>　　3）用SshClient登录F5的命令行，运行tcpdump抓取数据包，同时用客户端多次访问VS地址。</p>
<p align="left">　　抓包命令：tcpdump -s0 -ni 0.0:nnnp host 10.160.100.49 and port 25 -w /var/tmp/test_0907.pcap</p>
<p align="left">　　即，需要抓10.160.100.49的25端口，保存目录为/var/tmp/，保存文件为test.pcap。</p>
<p align="left">　　抓包结果如下：</p>
<p>&nbsp;　　<img src="https://img2018.cnblogs.com/blog/1274431/201811/1274431-20181106105451993-2132504016.png" alt="" width="419" height="95" /></p>
<p>　　<img src="https://img2018.cnblogs.com/blog/1274431/201811/1274431-20181106105510221-1754720988.png" alt="" width="575" height="383" /></p>
<p>　　其中1d=29，表示tcp类型为29；06表示字节数</p>
<p align="left">　　后边的十六进制转换为十进制后即为真实源地址</p>
<p align="left">　　此时即表明f5已经成功将真实源地址插入到option字段，具体如何读取需服务器端配置。</p>
<p align="left">&nbsp;　　<img src="https://img2018.cnblogs.com/blog/1274431/201811/1274431-20181106105539728-34017466.png" alt="" /></p>
<p>　　TCP三次握手</p>
<p align="left">　　注：option是在传输层，业务服务器默认只读网络层</p>
</div>
<div id="MySignature">看看天上，于是我去了满是风雪的地方</div>