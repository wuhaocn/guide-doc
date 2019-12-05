### 1.拉取 镜像

    docker pull fauria/vsftpd


### 2.启动容器

    docker pull fauria/vsftpd
    
    mkdir /data/ftp
    
    docker stop vsftpd
    
    docker rm vsftpd
    
    docker run -d -v /data/ftp:/home/vsftpd -p 2120:20 -p 2121:21 -p 21100-21110:21100-21110 -e FTP_USER=urcs -e FTP_PASS=urcs@2018 -e PASV_ADDRESS=10.10.208.194 -e PASV_MIN_PORT=21100 -e PASV_MAX_PORT=21110 --name vsftpd --restart=always fauria/vsftpd 

docker ps



### 3. 进入容器

    docker exec -i -t vsftpd bash 进去docker
    
    vi /etc/vsftpd/virtual_users.txt 编辑配置文件写入用户跟密码
    
    mkdir /home/vsftpd/user 建立新用户文件夹
    
    /usr/bin/db_load -T -t hash -f /etc/vsftpd/virtual_users.txt /etc/vsftpd/virtual_users.db 写入数据库
    
    docker restart +(虚拟机运行的 imageId) 重启服务 