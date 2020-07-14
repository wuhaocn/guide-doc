## fastdfs安装
    
### 创建目录
    mkdir -p /root/base/fastdfs
### 解压文件
    tar -zxf fastdfs.tar.gz 
### 配置环境变量
    vim ~/.bash_profile
    export PATH=/root/base/fastdfs/fastdfs/bin:/root/base/fastdfs/app/bin:/root/base/fastdfs/nginx/sbin/:$PATH
    export C_INCLUDE_PATH=/root/base/fastdfs/app/include:/root/base/fastdfs/fastdfs/include
    export LD_LIBRARY_PATH=/root/base/fastdfs/app/lib:/root/base/fastdfs/fastdfs/lib
    export LIBRARY_PATH=/root/base/fastdfs/app/lib:/root/base/fastdfs/fastdfs/lib

### 修改启动脚本:
    位置：/root/base/fastdfs/scripts
    
    vi modify_ip_userdir.sh
    localip="10.10.208.125"
    userdir="/root/base/fastdfs:q"
    
    vi start_service.sh
    userdir="/root/base/fastdfs"

### 启动脚本
    
    
    sh modify_ip_userdir.sh
    
    sh start_service.sh




## redis安装
### 创建目录
    mkdir redis
### 解压
    tar zxf rediscluster.tar.gz -C /home/user1/redis/
### 配置环境变量
    vi /home/user1/.bash_profile
    export PATH=/home/user1/redis/app/bin:/home/user1/redis/redis/bin:$PATH
    export C_INCLUDE_PATH=/home/user1/redis/app/include
    export LD_LIBRARY_PATH=/home/user1/redis/app/lib
    export LIBRARY_PATH=/home/user1/redis/app/lib
    source .bash_profile 

### 编辑配置文件（都需要改）
    vi redis_cluster/7001/redis_7001.conf
    第41行pidfile /home/user1/redis/redis_cluster/pid/redis_7001.pid
    第64行修改bind 10.10.208.125
    添加：protected-mode no（备注：关闭protected-mode模式，此时外部网络可以直接访问）
    第103行logfile /home/user1/redis/redis_cluster/7001/redis_7001.log
    第187行dir /home/user1/redis/redis_cluster/7001/
    第632行 cluster-enabled yes改为no，如果是搭建集群的话，不需要修改
### 修改启动脚本
    vi modify_userdir.sh 
    userdir='/home/user1/redis'
    sh modify_userdir.sh
    
    vi start_redis_cluster.sh
    localip="10.10.208.125"
    userdir="/home/user1/redis"
    sh start_redis_cluster.sh
### 测试是否可以连接redis
    redis-cli -h 10.10.208.125 -p 7001
    info
