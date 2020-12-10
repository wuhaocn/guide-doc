### docker 安装
安装：

```
wget http://ftp.loongnix.org/os/loongnix-server/1.7/os/mips64el/Packages/docker-ce-18.06.3.ce-0.lns7.mips64el.rpm
rpm -ivh docker-ce-18.06.3.ce-0.lns7.mips64el.rpm
```



#### 依赖安装

* 安装时会提示有依赖包需要安装，直接http://ftp.loongnix.org/os/loongnix-server/1.7/os/mips64el/Packages/里找到2个对应的安装包先安装，然后安装docker-ce即可。

```

错误：依赖检测失败：
	container-selinux >= 2.9 被 docker-ce-18.06.3.ce-0.lns7.mips64el 需要

```

```
yum install container-selinux
```

#### 重启

### docker-compose安装
docker-compose 则直接通过pip安装：

```
1. 安装python2-pip （如上安装包里面找到对应rpm包）

2. pip install --upgrade pip

3. pip install docker-compose --ignore-installed requests
```