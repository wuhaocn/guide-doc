## FastDFS 集群 docker 构建

### 1. docker 源

    1.1.使用season/fastdfs作为FastDFS源!

[season/fastdfs](https://hub.docker.com/r/season/fastdfs)

    1.2.Run as shell启动client做相关测试和镜像文件查看
        docker run -ti --name fdfs_sh --net=host season/fastdfs sh

    1.3.源镜像中会执行entrypoint.sh脚本进行FastDFS初始化工作


### dockerfile 解析

```
# FastDFS基础镜像
FROM season/fastdfs

# MAINTAINER
MAINTAINER wuhaocn@126.com
VOLUME ["/tmp"]

RUN mv /etc/apt/sources.list /etc/apt/sources.list.bak && \
    echo "deb http://mirrors.163.com/debian/ jessie main non-free contrib" >>/etc/apt/sources.list && \
    echo "deb http://mirrors.163.com/debian/ jessie-proposed-updates main non-free contrib" >>/etc/apt/sources.list && \
    echo "deb http://mirrors.163.com/debian/ jessie-backports main non-free contrib" >>/etc/apt/sources.list && \
    echo "deb-src http://mirrors.163.com/debian/ jessie main non-free contrib" >>/etc/apt/sources.list && \
    echo "deb-src http://mirrors.163.com/debian/ jessie-proposed-updates main non-free contrib" >>/etc/apt/sources.list && \
    echo "deb-src http://mirrors.163.com/debian/ jessie-backports main non-free contrib" >>/etc/apt/sources.list

RUN mkdir -p /data/fastdfs/

RUN mkdir -p /data/fastdfs/permfile

//将自定义storage.conf拷贝到镜像中替换源镜像中/fdfs_conf/storage.conf
COPY conf/storage.conf /fdfs_conf/storage.conf

//更新源
RUN apt-get update

//安装vim
RUN apt-get -y install vim

//安装netstat等
RUN apt-get -y install net-tools
```

### 3.FastDFS 补充

    3.1.通过storage.conf指定不同store_path
    store_path0=/data/fastdfs
    store_path1=/data/fastdfs/permfile

    3.2.通过storage.conf设置tracker_server地址

    3.3.fastdfs的storage server的状态查询
        # FDFS_STORAGE_STATUS：INIT      :初始化，尚未得到同步已有数据的源服务器
        # FDFS_STORAGE_STATUS：WAIT_SYNC :等待同步，已得到同步已有数据的源服务器
        # FDFS_STORAGE_STATUS：SYNCING   :同步中
        # FDFS_STORAGE_STATUS：DELETED   :已删除，该服务器从本组中摘除
        # FDFS_STORAGE_STATUS：OFFLINE   :离线
        # FDFS_STORAGE_STATUS：ONLINE    :在线，尚不能提供服务
        # FDFS_STORAGE_STATUS：ACTIVE    :在线，可以提供服务
        使用命令：[root@localhost bin]# fdfs_monitor /etc/fdfs/client.conf

### 4.注意事项

    4.1.生产环境FastDFS部署必须要进行数据挂载至硬盘
    4.2.先部署tracker,再进行storage部署
    4.3.检查状态
    可以使用 fdfs_monitor 来查看一下storage的状态，看是否已经成功注册到了tracker
    [......]#  fdfs_monitor /etc/fdfs/storage.conf
    #也可以以下命令来监控服务器的状态：
    [......]# fdfs_monitor /etc/fdfs/client.conf
