# 信令追踪部署说明

### 1、部署 webssh 服务端（示例 172.16.106.82），启动 webssh 服务端。

### 1) 解压代码,安装 python3 环境

https://github.com/huashengdun/webssh

### 2) 执行代码安装 webssh 所需组件

sudo python3 /home/pcscf/webssh-master/setup.py install

### 3) 后台启动 webssh 服务

sudo nohup python3 /home/pcscf/webssh-master/run.py --xsrf=False &

### 4) 访问http://172.16.106.82:8888 测试,成功后会出现登陆页面

### 2、部署 termshark 抓包程序安装于目标服务器 B 中(示例 172.16.160.197)

### 1) 安装 termshark

sudo apt install termshark

### 2）配置 sudo 不需要密码，在/etc/sudoers 中增加一行

/#user 'pcscf' do not need password when executing command pcscf ALL = NOPASSWD: ALL

### 3、构造自动登录、执行参数

参数说明,示例(抓取 sip 信令):
http://172.16.106.82:8888/?hostname=172.16.106.82&username=pcscf&password=cGNzY2Y=&port=22&command=sudo%20termshark%20-i%20any%20-Y%20sip

172.16.106.82:8888: webssh 路径

hostname/username/password/port: 目标主机的登陆信息,注意需要对密码进行 base64 加密处理

command: termshark 执行命令

### 另附 webssh 服务脚本

/#!/bin/bash case "$1" in start) echo "start webssh-service" sudo nohup python3 /home/pcscf/webssh-master/run.py --xsrf=False > /home/pcscf/webssh-master/log.txt 2>&1 & ;; restart) echo "kill webssh-service" sudo ps -aux|grep run.py| grep -v grep | awk '{print $2}'|sudo xargs kill -9 echo "start webssh-service" sudo nohup python3 /home/pcscf/webssh-master/run.py --xsrf=False > /home/pcscf/webssh-master/log.txt 2>&1 & ;; stop) echo "kill webssh-service" sudo ps -aux|grep run.py| grep -v grep | awk '{print $2}'|sudo xargs kill -9 ;; esac
