#### 1.ElasticSearch安装
    特别是单节点的安装非常简单。按照官方文档操作如下
    
    最新的Elastic Search 6.1.1 要求linux 内核 3.5 以上，本文在 CentOS 7 版本下进行安装。
    最新的Elastic Search 6.1.1 要求linux 内核 3.5 以上对JDK 的要求是 1.8 以上。首先确保系统中的 JDK 存在并且是 JDK1.8 以上版本
    java -version
    java version "1.8.0_144"
    Java(TM) SE Runtime Environment (build 1.8.0_144-b01)
    Java HotSpot(TM) 64-Bit Server VM (build 25.144-b01, mixed mode)
    
    这里显示 JDK 是1.8.0_144版本的。满足 Elastic Search 6 的要求
#### 2.下载安装的压缩包
    wget https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-6.1.1.tar.gz

#### 3.解压安装包
    tar -xvf elasticsearch-6.1.1.tar.gz

#### 4.配置 Elastic Search 集群
    ES 的配置文件在安装目录下的 conf 目录下，一个是 log4j2.properties 文件，用来配置日志信息的，一个是 jvm.options 文件，用来配置 JVM 运行参数的。
    一个是 elasticsearch.yml 是用来配置 ES 自身的。前面两个我们先不管。我们主要配置 elasticsearch.yml 文件
    注意下面几行
    #cluster.name: my-application
    #node.name: node-1
    #path.data: /path/to/data
    #path.logs: /path/to/logs
    #network.host: 192.168.0.1
    #http.port: 9200


    cluster.name 用来指定集群的名称。如果不指定，则默认是 elasticsearch。
    node.name 用来指定当前节点的名称，如果不指定，则会启动的时候自动生成一个随机唯一标识符作为节点的名称。
    path.data 指定 ES 数据存储路径。
    path.logs 指定 ES 日志文件存储路径。
    network.host 用来指定服务端口绑定的 IP 地址，默认绑定 127.0.0.1 ，也就是只能本机访问。
    http.port 用来指定提供 http 服务的端口。

    根据上述描述。我们去掉这几行的注释标识 # 并且把内容更改为下面的内容。这里加锁 /usr/local/elasticsearch-6.1.1 是 ES 安装路径。
    cluster.name: study_cluster
    node.name: master
    path.data: /usr/local/elasticsearch-6.1.1/data
    path.logs: /usr/local/elasticsearch-6.1.1/logs
    network.host: 0.0.0.0
    http.port: 9200

    启动 Elastic Search
    /usr/local/elasticsearch-6.1.1/elasticsearch 
#### 5.可能出现问题
    如果启动的时候出现如下提示
    [1]: max file descriptors [4096] for elasticsearch process is too low, increase to at least [65536]
    [2]: max virtual memory areas vm.max_map_count [65530] is too low, increase to at least [262144]
    
    解决第一个问题需要用有 root 权限用户 在文件 /etc/security/limits.conf 的最后加上两行。
    * hard nofile 65536
    * soft nofile 65536
    
    解决第二个问题需要用有 root 权限的用户执行如下的命令
    sysctl -w vm.max_map_count=262144
    
    再重新执行如下命令
    /usr/local/elasticsearch-6.1.1/elasticsearch 

    将能正常启动 ES 集群
    查看状态
    查看基本信息
    在命令行下执行命令
    curl http://localhost:9200
    
    得到如下的信息
    {
      "name" : "master",
      "cluster_name" : "study_cluster",
      "cluster_uuid" : "YEpCazm4RlK5iPUKKZpmEQ",
      "version" : {
        "number" : "6.1.1",
        "build_hash" : "bd92e7f",
        "build_date" : "2017-12-17T20:23:25.338Z",
        "build_snapshot" : false,
        "lucene_version" : "7.1.0",
        "minimum_wire_compatibility_version" : "5.6.0",
        "minimum_index_compatibility_version" : "5.0.0"
      },
      "tagline" : "You Know, for Search"
    }

#### 6.Elastic Search 集群健康状态查看


    timestamp：代表状态时间
    cluster：表示集群名称
    status：表示集群状态。green 代表健康；yellow 代表数据完整，但是副本不完整；red 代表数据不完整
    node.total：表示集群节点总数
    node.data：表示集群数据节点总数
    shards：表示集群分片的总数
    active_shards_percent：表示集群活动的分片百分比

    查看健康状态信息
    在命令行下执行命令
    curl http://localhost:9200/_cat/health?v

    查看状态列表
    在命令行下执行命令
    curl http://localhost:9200/_cat
