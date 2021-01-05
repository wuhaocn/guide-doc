## 1.安装依赖环境

    yum -y install pcre-devel
    yum -y install openssl openssl-devel

### 2.安装 nginx

wget http://nginx.org/download/nginx-1.15.7.tar.gz
tar -xzvf nginx-1.15.7.tar.gz

    cd nginx-1.15.7

    ./configure --prefix=/home/app/nginx-1.15.7 --with-stream

    make

    make install

### 3.启动

    cd /home/app/nginx-1.15.7

    cd sbin

    ./nginx

### 4.参考文档

    下载地址：
    http://nginx.org/download/

    文档：
    http://nginx.org/en/docs/

### 5.常用维护命令

    从容停止服务
    这种方法较stop相比就比较温和一些了，需要进程完成当前工作后再停止。

    nginx -s quit

    立即停止服务
    这种方法比较强硬，无论进程是否在工作，都直接停止进程。

    nginx -s stop

    systemctl 停止
    systemctl属于Linux命令

    systemctl stop nginx.service

    killall 方法杀死进程
    直接杀死进程，在上面无效的情况下使用，态度强硬，简单粗暴！

    killall nginx

    2.启动Nginx

    nginx直接启动

    nginx

    systemctl命令启动

    systemctl start nginx.service

    3.查看启动后记录

    ps aux | grep nginx

    4.重启Nginx服务

    systemctl restart nginx.service

    5.重新载入配置文件
    当有系统配置文件有修改，用此命令，建议不要停止再重启，以防报错！

    nginx -s reload

    6.查看端口号

    netstat -tlnp

原文链接：https://blog.csdn.net/londa/article/details/91372918
