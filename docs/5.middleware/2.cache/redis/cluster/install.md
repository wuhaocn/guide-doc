## 基础环境准备

- mac
- docker version 20.10.5
- redis version redis:5.0

## redis 集群

### 拉取 redis 官方镜像

```
docker pull redis:5.0
```

### 创建配置文件和数据目录

```
mkdir ~/rcluster
```

### 新建一个模板文件【redis_cluster.tmpl】

vim ~/rcluster/redis_cluster.tmpl

```
# redis端口
port ${PORT}
# 关闭保护模式
protected-mode no
# 开启集群
cluster-enabled yes
# 集群节点配置
cluster-config-file nodes.conf
# 超时
cluster-node-timeout 5000
# 集群节点IP host模式为宿主机IP
cluster-announce-ip 127.0.0.1
# 集群节点端口 7000 - 7005
cluster-announce-port ${PORT}
cluster-announce-bus-port 1${PORT}
# 开启 appendonly 备份模式
appendonly yes
# 每秒钟备份
appendfsync everysec
# 对aof文件进行压缩时，是否执行同步操作
no-appendfsync-on-rewrite no
# 当目前aof文件大小超过上一次重写时的aof文件大小的100%时会再次进行重写
auto-aof-rewrite-percentage 100
# 重写前AOF文件的大小最小值 默认 64mb
auto-aof-rewrite-min-size 5120mb
# 关闭快照备份
save ""
```

### 批量生成配置

vim ~/rcluster/rcbuild.sh

```
for port in `seq 7000 7005`; do \
  mkdir -p ~/rcluster/${port}/conf \
  && PORT=${port} envsubst < ~/rcluster/redis_cluster.tmpl > ~/rcluster/${port}/conf/redis.conf \
  && mkdir -p ~/rcluster/${port}/data; \
done
```

chmod 777 ~/rcluster/rcbuild.sh

~/rcluster/rcbuild.sh

### 批量启动容器

vim ~/rcluster/rcrun.sh

```
for port in `seq 7000 7005`; do \
  docker stop redis-${port}
  docker rm redis-${port}
  docker run -d -it --memory=1G \
  -v ~/rcluster/${port}/conf/redis.conf:/usr/local/etc/redis/redis.conf \
  -v ~/rcluster/${port}/data:/data \
  --restart always --name redis-${port} --net host \
  redis:5.0 redis-server /usr/local/etc/redis/redis.conf; \
done
```

chmod 777 ~/rcluster/rcrun.sh

~/rcluster/rcrun.sh

- 备注

```
这里的--memeory=1G是限制单个 docker 容器占用内存大小为 1G，超过会被进程杀死。
运行时可能会出现...Memory limited without swap...这个警告，可以忽略。
如不需要限制内存，可以去掉--memeory参数。
```

### 握手

- 集群握手

```
./redis-cli -h 10.3.4.111 -p 7000

10.3.4.111:51001> cluster meet 10.3.4.111 7001
OK
10.3.4.111:51001> cluster meet 10.3.4.111 7002
OK
10.3.4.111:51001> cluster meet 10.3.4.111 7003
OK
10.3.4.111:51001> cluster meet 10.3.4.111 7004
OK
10.3.4.111:51001> cluster meet 10.3.4.111 7005
OK
```

### 划分切片

```
./redis-cli -h 10.3.4.111 -p 7001 cluster addslots {0..5461}

./redis-cli -h 10.3.4.111 -p 7003 cluster addslots {5462..10922}

./redis-cli -h 10.3.4.111 -p 7005 cluster addslots {10923..16383}
```

### 集群查看

- 查看集群整体信息

```
10.3.4.111:7000> cluster info
cluster_state:ok
cluster_slots_assigend:16384
cluster_slots_ok:16384
cluster_slots_pfail:0
cluster_known_nodes:6
cluster_size:3
cluster_current_epoch:5
cluster_my_epoch:0
cluster_stats_messages_ping_sent:1028
cluster_stats_messages_pong_sent:1051
cluster_stats_messages_sent:2079
cluster_stats_messages_ping_received:1046
cluster_stats_messages_pong_received:1028
cluster_stats_messages_meet_received:5
cluster_stats_messages_received:2079
```

- 查看集群节点 ID

```
10.3.4.111:7000> cluster nodes
09d338ba582ca508590318cb64b58051766ade46 10.3.4.111:7003@17003 master - 0 1624941178687 3 connected 5462-10922
9e20653a47f948c1320c71f541f93ba705755d9d 10.3.4.111:7005@17005 master - 0 1624941178696 5 connected 10923-16383
ff91ecdb55139ada0e4e8e0cb36087054488ed53 10.3.4.111:7001@17001 master - 0 1624941179397 0 connected 0-5461
06b07e38210375c4cb66b1de39f0525cf01eade4 10.3.4.111:7004@17004 master - 0 1624941180549 4 connected
d4169ecb4187c609fa1bd82aec5d160fe3e5cb29 10.3.4.111:7000@17000 myself,master - 0 1624941177000 1 connected
e86b49d7fd4509eee0a049d773c3032501f939a5 10.3.4.111:7002@17002 master - 0 1624941179577 2 connected
```

### 主从复制

- 上述步骤已获节点 id

```
./redis-cli -h 10.3.4.111 -p 7000 cluster  replicate ff91ecdb55139ada0e4e8e0cb36087054488ed53
OK
./redis-cli -h 10.3.4.111 -p 7002 cluster replicate 09d338ba582ca508590318cb64b58051766ade46
OK
./redis-cli -h 10.3.4.111 -p 7004 cluster replicate 9e20653a47f948c1320c71f541f93ba705755d9d
OK
```

- 完成后查看集群信息

```
10.3.4.111:7001> cluster nodes
1503e79d88b7953b1d13461756b47ddc6e98a6d6 10.3.4.111:51003@61003 master - 0 1624935112000 2 connected 5462-10922
8c46938569d322cb1f1fa9b17e4ac454c33a69b5 10.3.4.111:51006@61006 slave e4e78464d23ef1e97bae90aa6792c18f593f45a3 0 1624935113395 5 connected
a9007924fb2d018f1173cff0283a3404ef78a6af 10.3.4.111:51004@61004 myself,slave 1503e79d88b7953b1d13461756b47ddc6e98a6d6 0 1624935112000 3 connected
e4e78464d23ef1e97bae90aa6792c18f593f45a3 10.3.4.111:51005@61005 master - 0 1624935112393 4 connected 10923-16383
6c9c067f264404db2dd487ab450ed27dcbe71e09 10.3.4.111:51001@61001 master - 0 1624935114398 1 connected 0-5461
f57e9de79123c4ab9581aecc5fdf96665a304cc1 10.3.4.111:51002@61002 slave 6c9c067f264404db2dd487ab450ed27dcbe71e09 0 1624935114000 1 connected
```
