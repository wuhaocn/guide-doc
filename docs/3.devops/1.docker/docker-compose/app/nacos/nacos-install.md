### 1.docker 安装

#### 1.1 拉取配置

    git clone https://github.com/nacos-group/nacos-docker.git

#### 1.2 启动

    cd nacos-docker
    docker-compose -f example/standalone-mysql.yaml up

#### 1.3 验证

    http://yourip:8848/nacos
    登录用户名和密码都为:nacos

#### 1.4 演示地址：

    http://10.10.220.121:8848/nacos/#/configurationManagement?dataId=&group=&appName=&namespace=
