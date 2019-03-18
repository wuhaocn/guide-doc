##### docker安装


##### 拉取镜像

    docker pull mysql:5.6.40

##### 运行镜像

    docker run --name mysql.5.6.40 -p 3306:3306 -e MYSQL_ROOT_PASSWORD=urcs@2018 -d mysql:5.6.40 

##### 进入容器

    docker exec -it xxxx bash