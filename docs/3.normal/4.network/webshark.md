<h1>信令追踪部署说明</h1>

<h3>1、部署webssh服务端（示例172.16.106.82），启动webssh服务端。</h3>
		
<h4>1) 解压代码,安装python3环境</h4>
	https://github.com/huashengdun/webssh
<h4>2) 执行代码安装webssh所需组件 </h4>
    sudo python3 /home/pcscf/webssh-master/setup.py install
<h4>3) 后台启动webssh服务  </h4>
    sudo nohup python3 /home/pcscf/webssh-master/run.py --xsrf=False &
<h4>4) 访问http://172.16.106.82:8888 测试,成功后会出现登陆页面 </h4>

<h3>2、部署termshark抓包程序安装于目标服务器B中(示例172.16.160.197)</h3>

<h4>1) 安装termshark </h4>
    sudo apt install termshark
<h4>2）配置sudo不需要密码，在/etc/sudoers中增加一行 </h4>
    #user 'pcscf' do not need password when executing command
    pcscf ALL = NOPASSWD: ALL
<h3>3、构造自动登录、执行参数</h3>

<p>参数说明,示例(抓取sip信令):</p>
    http://172.16.106.82:8888/?hostname=172.16.106.82&amp;username=pcscf&amp;password=cGNzY2Y=&amp;port=22&amp;command=sudo%20termshark%20-i%20any%20-Y%20sip
  <p>   172.16.106.82:8888: webssh路径</p>
  <p>   hostname/username/password/port: 目标主机的登陆信息,注意需要对密码进行base64加密处理</p>
  <p>   command: termshark执行命令</p>

<h4>另附webssh服务脚本</h4>

    	#!/bin/bash
    	case "$1" in
    	start)
    		echo "start webssh-service"
    		sudo nohup python3 /home/pcscf/webssh-master/run.py --xsrf=False > /home/pcscf/webssh-master/log.txt 2>&1 &
    	;;
    	restart)
    		echo "kill webssh-service"
    		sudo ps -aux|grep run.py| grep -v grep | awk '{print $2}'|sudo xargs kill -9
    		echo "start webssh-service"
    		sudo nohup python3 /home/pcscf/webssh-master/run.py --xsrf=False > /home/pcscf/webssh-master/log.txt 2>&1 &

    	;;
    	stop)
    		echo "kill webssh-service"
    		sudo ps -aux|grep run.py| grep -v grep | awk '{print $2}'|sudo xargs kill -9
    	;;
    	esac
