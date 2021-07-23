## Redis 集群之 Redis+Twemproxy 方案

2020-06-03 阅读 3710
【转载请注明出处】：https://cloud.tencent.com/developer/article/1637716

## 1、下载 Redis 安装包

Redis 的官网下载地址是https://redis.io/download，上面有各个发行版本，我选择的是当前的最新稳定版5.0.4，下载到安装目录并编译出安装包。

wget http://download.redis.io/releases/redis-5.0.4.tar.gz
tar -zxvf redis-5.0.4.tar.gz
cd redis-5.0.4
make
make test
测试执行没有问题之后显示

image.png

## 2、验证 Redis

启动服务

./src/redis-server
客户端连接验证

./src/redis-cli
127.0.0.1:6379> set foo bar
OK
127.0.0.1:6379> get foo
"bar"
127.0.0.1:6379> exit

## 3、配置 Redis 集群环境

创建 redis-cluster 目录，然后在 redis-cluster 下分别创建 7001 7002 7003 7004 7005 7006 目录

mkdir redis-cluster
cd redis-cluster
mkdir 7001 7002 7003 7004 7005 7006
cd ..
编辑 7001 的配置文件

./redis-cluster/7001/redis.conf
配置文件内容如下

bind 127.0.0.1
port 7001
daemonize no
pidfile /var/run/redis_7001.pid
logfile "./redis-cluster/7001/redis.log"
save 900 1
save 300 10
save 60 10000
dir ./redis-cluster/7001/
cluster-enabled yes
cluster-config-file nodes-7001.conf
cluster-node-timeout 5000
appendonly yes
appendfsync always
这些配置的含义及例子都可以在根目录 redis.conf 文件中找到，具体含义可以阅读此文件。将编辑好的配置文件复制到其他节点，修改相应节点的配置

cp ./redis-cluster/7001/redis.conf ./redis-cluster/7002/
cp ./redis-cluster/7001/redis.conf ./redis-cluster/7003/
cp ./redis-cluster/7001/redis.conf ./redis-cluster/7004/
cp ./redis-cluster/7001/redis.conf ./redis-cluster/7005/
cp ./redis-cluster/7001/redis.conf ./redis-cluster/7006/
grep 7001 -rl ./redis-cluster/7002 | xargs -t sed -i ".bak" "s/7001/7002/g"

- 将 7002 目录下的文件备份为 xxx.bak，并将文件中的 7001 替换成 7002
- -t 是回显命令 ，-i 是直接修改文件内容，“.bak”是备份文件的后缀
- 产生的备份文件如果不想要可以用下面任何一个命令删除
- find ./redis-cluster -name "\*.bak" | xargs rm -f ;
- find ./redis-cluster -name "\*.bak" -exec rm {} \;
  grep 7001 -rl ./redis-cluster/7003 | xargs -t sed -i ".bak" "s/7001/7003/g"
  grep 7001 -rl ./redis-cluster/7004 | xargs -t sed -i ".bak" "s/7001/7004/g"
  grep 7001 -rl ./redis-cluster/7005 | xargs -t sed -i ".bak" "s/7001/7005/g"
  grep 7001 -rl ./redis-cluster/7006 | xargs -t sed -i ".bak" "s/7001/7006/g"
  分别启动五个实例

./src/redis-server ./redis-cluster/7001/redis.conf &
./src/redis-server ./redis-cluster/7002/redis.conf &
./src/redis-server ./redis-cluster/7003/redis.conf &
./src/redis-server ./redis-cluster/7004/redis.conf &
./src/redis-server ./redis-cluster/7005/redis.conf &
./src/redis-server ./redis-cluster/7006/redis.conf &
创建 redis 集群

./src/redis-cli --cluster create 127.0.0.1:7001 127.0.0.1:7002 127.0.0.1:7003 127.0.0.1:7004 127.0.0.1:7005 127.0.0.1:7006 --cluster-replicas 1
为了后续操作方便，可以编写一个启动与停止的脚本：

启动脚本 start.sh

#!/bin/sh
./src/redis-server ./redis-cluster/7001/redis.conf &
./src/redis-server ./redis-cluster/7002/redis.conf &
./src/redis-server ./redis-cluster/7003/redis.conf &
./src/redis-server ./redis-cluster/7004/redis.conf &
./src/redis-server ./redis-cluster/7005/redis.conf &
./src/redis-server ./redis-cluster/7006/redis.conf &
停止脚本 stop.sh

#!/bin/sh
./src/redis-cli -p 7001 shutdown
./src/redis-cli -p 7002 shutdown
./src/redis-cli -p 7003 shutdown
./src/redis-cli -p 7004 shutdown
./src/redis-cli -p 7005 shutdown
./src/redis-cli -p 7006 shutdown
给脚本添加执行权限

chmod +x start.sh
chmod +x stop.sh
通过随便一个客户端查看节点信息

./src/redis-cli -p 7001 cluster nodes

image.png

## 4、Twemproxy 介绍

Twemproxy 也叫 nutcraker。是 Twtter 开源的一个 Redis 和 Memcache 代理服务器，主要用于管理 Redis 和 Memcached 集群，减少与 Cache 服务器直接连接的数量。

Twemproxy 特性：

支持失败节点自动删除
– 可以设置重新连接该节点的时间
– 可以设置连接多少次之后删除该节点
支持设置 HashTag
– 通过 HashTag 可以自己设定将两个 key 哈希到同一个实例上去减少与 redis 的直接连接数
– 保持与 redis 的长连接
– 减少了客户端直接与服务器连接的连接数量
自动分片到后端多个 redis 实例上
– 多种 hash 算法：md5、crc16、crc32 、crc32a、fnv1_64、fnv1a_64、fnv1_32、fnv1a_32、hsieh、murmur、jenkins
– 多种分片算法：ketama(一致性 hash 算法的一种实现)、modula、random
– 可以设置后端实例的权重
避免单点问题
– 可以平行部署多个代理层,通过 HAProxy 做负载均衡，将 redis 的读写分散到多个 twemproxy 上。
支持状态监控
– 可设置状态监控 ip 和端口，访问 ip 和端口可以得到一个 json 格式的状态信息串
– 可设置监控信息刷新间隔时间
使用 pipelining 处理请求和响应
– 连接复用，内存复用
– 将多个连接请求，组成 reids pipelining 统一向 redis 请求
并不是支持所有 redis 命令
– 不支持 redis 的事务操作
– 使用 SIDFF, SDIFFSTORE, SINTER, SINTERSTORE, SMOVE, SUNION and SUNIONSTORE 命令需要保证 key 都在同一个分片上。

## 5、Twemproxy 安装

Twemproxy 官网地址是https://github.com/twitter/twemproxy，在安装twemproxy 前，需要安装 autoconf，automake，libtool 软件包，此处不再说明。

Twemproxy 的发布版本在 https://github.com/twitter/twemproxy/releases

下载最新版本 0.4.1

wget https://github.com/twitter/twemproxy/archive/v0.4.1.tar.gz
tar -zxvf v0.4.1.tar.gz
cd twemproxy-0.4.1/
autoreconf -fvi
./configure --prefix=/usr/local/twemproxy
make
sudo make install
编辑配置文件 nutcracker.yml

cd /usr/local/twemproxy/
mkdir conf run
vim ./conf/nutcracker.yml
这里只配置了三个 master 节点作为测试，具体根据业务需要配置，具体的参数含义在官网有说明https://github.com/twitter/twemproxy

beta:
listen: 127.0.0.1:22122
hash: fnv1a_64
hash_tag: "{}"
distribution: ketama
auto_eject_hosts: false
timeout: 400
redis: true
servers:

- 127.0.0.1:7004:1 7004
- 127.0.0.1:7005:1 7005
- 127.0.0.1:7006:1 7006
  启动 twemproxy

./sbin/nutcracker -t

- 测试配置文件是否正确
  ./sbin/nutcracker -d -c /usr/local/twemproxy/conf/nutcracker.yml -p /usr/local/twemproxy/run/redisproxy.pid -o /usr/local/twemproxy/run/redisproxy.log
- 指定配置文件路径、pid 路径、日志路径
  连接 twemproxy 进行测试

./src/redis-cli -h 127.0.0.1 -p 22122 -c
127.0.0.1:22122> set foo bar
-> Redirected to slot [1044] located at 127.0.0.1:7005
OK
127.0.0.1:7005> get foo
"bar"
表示成功了，可以进行读取，应用代码中就可以直接连接 22122 端口使用 redis 了。

配置启动/重启/停止脚本方便操作
启动脚本 start.sh

```
#!/bin/sh
./sbin/nutcracker -d -c /usr/local/twemproxy/conf/nutcracker.yml -p /usr/local/twemproxy/run/redisproxy.pid -o /usr/local/twemproxy/run/redisproxy.log
```

停止脚本 stop.sh

```
#!/bin/sh
sudo killall nutcracker
```

或者复制 twemproxy-0.4.1/scripts/nutcracker.init 文件到/usr/local/twemproxy/sbin 下，并修改脚本即可。

## 6、Twemproxy 的缺点

虽然可以动态移除节点，但该移除节点的数据就丢失了。
redis 集群动态增加节点的时候，twemproxy 不会对已有数据做重分布
性能上损耗
【转载请注明出处】：https://cloud.tencent.com/developer/article/1637716
