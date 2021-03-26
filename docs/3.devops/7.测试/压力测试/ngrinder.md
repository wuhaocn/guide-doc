### 1.介绍

#### 1.1.参考资源链接

    github:
    官网：
    博客参考：https://segmentfault.com/a/1190000017022784
    docker hub: https://hub.docker.com/r/ngrinder/controller/

### 2.安装

1.安装完成后：
用户名：admin
密码： admin

2. 地址：
   http://127.0.0.1/perftest/

#### 2.1. docker 安装

参考链接：https://hub.docker.com/r/ngrinder/controller/

##### 2.1.1 control 安装：

    docker pull ngrinder/controller:3.4

    docker run -d -v ~/ngrinder-controller:/opt/ngrinder-controller -p 80:80 -p 16001:16001 -p 12000-12009:12000-12009 ngrinder/controller:3.4



    80: Default controller web UI port.

    9010-9019: agents connect to the controller cluster thorugh these ports.

    12000-12029: controllers allocate stress tests through these ports.

##### 2.1.2 agent 安装：

    docker pull ngrinder/agent:3.4

    docker run -v ~/ngrinder-agent:/opt/ngrinder-agent -d ngrinder/agent:3.4 controller_ip:controller_web_port
