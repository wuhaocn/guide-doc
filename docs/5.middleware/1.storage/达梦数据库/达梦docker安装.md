### 安装前准备
软硬件	版本
终端	X86-64 架构
Docker	19.0 及以上版本
### 下载 Docker 安装包
在根目录下创建 /dm8 文件夹，用来放置下载的 Docker 安装包。命令如下：

```
mkdir /dm8
```
切换到 /dm8 目录，下载 DM Docker 安装包。命令如下：
```
wget -O dm8_docker.tar -c http://download.dameng.com/eco/dm8/dm8_docker.tar
```
### 导入镜像
下载完成后，导入安装包，使用如下命令：
```
docker import dm8_docker.tar dm8:v01
```
导入完成后，可以使用 docker images 来查看导入的镜像，命令如下：
```
docker images
```
查看结果如下：
```
REPOSITORY   TAG       IMAGE ID       CREATED         SIZE
dm8          v01       7e69fae2492c   4 seconds ago   1.72GB
```
### 启动容器
镜像导入后，使用 docker run 来启动容器，默认的端口 5236 默认的账号密码 ，启动命令如下：
```
docker run -itd -p 5236:5236 --name dm8_01 dm8:v01 /bin/bash /startDm.sh
```
容器启动完成后，使用 docker ps 来查看镜像的启动情况，命令如下：
```
docker ps
```
结果显示如下：
```
CONTAINER ID   IMAGE     COMMAND                  CREATED         STATUS         PORTS                    NAMES
7efdc6db308b   dm8:v01   "/bin/bash /startDm.…"   5 seconds ago   Up 4 seconds   0.0.0.0:5236->5236/tcp   dm8_01
```
启动完成后，可以查看日志来查看启动情况，命令如下：
```
docker logs -f  dm8_01
```
显示内容如下，则表示启动成功。

Starting DmServiceDMSERVER: Last login: Wed Dec 23 09:02:59 UTC 2020
[ OK ]

### 启动停止数据库
停止命令如下：
```
docker stop  dm8_01
```
启动命令如下：
```
docker start  dm8_01
```
重启命令如下：
```
docker restart  dm8_01
```
### 注意
如果使用docker容器里面的 disql ,进入容器后，先执行 source /etc/profile 防止中文乱码。