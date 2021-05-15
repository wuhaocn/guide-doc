在 macOS 和 Linux 上
打开一个终端窗口。

下载 jenkinsci/blueocean 镜像并使用以下 docker run 命令将其作为 Docker 中的容器运行 ：

- docker 安装

```
docker stop jenkins
docker rm jenkins
docker run \
-u root \
--name jenkins \
-d \
-p 8080:8080 \
-p 50000:50000 \
-v /home/rcloud/data/jenkins_home:/var/jenkins_home \
-v /var/run/docker.sock:/var/run/docker.sock \
jenkinsci/blueocean

docker update jenkins --restart=always
```

- 查询密码

访问 jenkins 地址
/var/jenkins_home/secrets/initialAdminPassword

\*
