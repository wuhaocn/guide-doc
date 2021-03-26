## docker-compose 的安装及使用

### 简单介绍

Docker Compose 是一个用来定义和运行复杂应用的 Docker 工具。
使用 Docker Compose 不再需要使用 shell 脚本来启动容器。(通过 docker-compose.yml 配置)

### 安装

可以通过修改 URL 中的版本，自定义您需要的版本。

- Github 源

```
sudo curl -L https://github.com/docker/compose/releases/download/1.22.0/docker-compose-`uname -s`-`uname -m` -o /usr/local/bin/docker-compose sudo chmod +x /usr/local/bin/docker-compose
```

- Daocloud 镜像

```
curl -L https://get.daocloud.io/docker/compose/releases/download/1.22.0/docker-compose-`uname -s`-`uname -m` > /usr/local/bin/docker-compose chmod +x /usr/local/bin/docker-compose
**测试
**
```

docker-compose -v

### 卸载

```
sudo rm /usr/local/bin/docker-compose
```

### 基础命令

需要在 docker-compose.yml 所在文件夹中执行命令

使用 docker-compose 部署项目的简单步骤

- 停止现有 docker-compose 中的容器：

docker-compose down

- 重新拉取镜像：

docker-compose pull

- 后台启动 docker-compose 中的容器：

docker-compose up -d

### 通过 docker-compose.yml 部署应用

我将上面所创建的镜像推送到了阿里云，在此使用它

### 1.新建 docker-compose.yml 文件

通过以下配置，在运行后可以创建两个站点(只为演示)

```
version: "3" services: web1: image: registry.cn-hangzhou.aliyuncs.com/yimo_public/docker-nginx-test:latest ports: - "4466:80" web2: image: registry.cn-hangzhou.aliyuncs.com/yimo_public/docker-nginx-test:latest ports: - "4477:80"
```

此处只是简单演示写法，说明 docker-compose 的方便

### 2.构建完成，后台运行镜像

```
docker-compose up -d
```

运行后就可以使用 ip+port 访问这两个站点了

### 3.镜像更新重新部署

```
docker-compose down docker-compose pull docker-compose up -d
参考：
[https://www.cnblogs.com/morang/p/9501223.html](https://www.cnblogs.com/morang/p/9501223.html)
```
