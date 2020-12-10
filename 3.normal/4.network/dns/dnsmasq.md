## DNSmasq 的安装、配置与使用

[参考](https://www.jianshu.com/p/4a23c7b605c4)

### 1. DNSmasq 简介

DNSmasq 是一个小巧且方便地用于配置DNS和DHCP的工具。我们在做开发时，经常会把一些域名映射到本地，但我们知道 /etc/hosts 不支持各种稍微高级一些的配置，
比如泛域名解析，你想把 *.hello.me 映射到本地，必须在 hosts 文件里一个一个地写，这个就很坑了。

所以就有了 DNSmasq ！

### 2. 安装
Mac 下直接使用 brew 安装即可:

```
brew install dnsmasq
```


ubuntu

```
sudo apt-get install dnsmasq

sudo systemctl restart dnsmasq
```

安装成功后提示:

```
cp /usr/local/opt/dnsmasq/dnsmasq.conf.example /usr/local/etc/dnsmasq.conf 
sudo brew services start dnsmasq
```

### 3. 配置
#### 3.1 配置文件 /usr/local/etc/dnsmasq.conf
根据上述成功安装提示照做，生成 /usr/local/etc/dnsmasq.conf 文件，编辑内容如下：

```
# 配置上行DNS，对应no-resolv
resolv-file=/etc/resolv.conf

# resolv.conf内的DNS寻址严格按照从上到下顺序执行，直到成功为止
strict-order

# DNS解析hosts时对应的hosts文件，对应no-hosts
addn-hosts=/etc/hosts
cache-size=1024 

# 多个IP用逗号分隔，192.168.x.x表示本机的ip地址，只有127.0.0.1的时候表示只有本机可以访问。
# 通过这个设置就可以实现同一局域网内的设备，通过把网络DNS设置为本机IP从而实现局域网范围内的DNS泛解析(注：无效IP有可能导至服务无法启动）
listen-address=192.168.x.x,127.0.0.1  

# 重要！！这一行就是你想要泛解析的域名配置.
address=/hello.me/127.0.0.1 
以上几乎是最简配置.

reolve-file=/etc/resolv.conf 配置上行DNS，假设 /etc/resolv.conf 内容如下：

nameserver 183.44.22.19
那就是说如果你访问域名abc.com没有被dnsmasq解析，它会尝试访问 183.44.22.19 去解析。
```

#### 3.2 客户端指定域名服务器/etc/resolv.conf
你的 Mac 可能同时就是你的 DNS 使用者，所有，需要：系统偏好配置->网络->(你的连接)->DNS增加了一个条目：

```
# 局域网其它机器则换成实际dnsmasq的IP地址。
127.0.0.1 
一般这一行放到最上面，会优先DNS解析

此时，你 ping 一下 hello.me、dev.hello.me，就会发现全指向了本地。
```


### 4. 启动
注意需要使用 sudo 来启动，因为权限要求较高。

```

# 启动
sudo brew services start dnsmasq

# 重启
sudo brew services restart dnsmasq

# 停止
sudo brew services stop dnsmasq
如果改动了泛解析规则，重启 dnsmasq 不会立即看到效果，因为有缓存，可以稍等便可或清除一下缓存再试

sudo killall -HUP mDNSResponder
```