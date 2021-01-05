##

新版 wireshark 的升级#
普通方法安装 wireshark 最便捷的方式是

sudo apt-get install wireshark
虽然一个命令就能搞定，但是，wireshark 的版本过于老旧，和 win 平台的版本对应不上，最关键的是不支持 MPTCP 的直接解析，使用起来十分蛋疼。但是，实际上，我们已经可以在 Ubuntu 平台升级较新版本的 wireshark 了，具体步骤如下。直接下载即可，会自动覆盖老版的 wireshark。

添加 wireshark 官方密钥并升级

sudo apt-add-repository ppa:wireshark-dev/stable
sudo apt-get update
安装 wireshark

sudo apt-get install wireshark
配置 wireshark

sudo dpkg-reconfigure wireshark-common
中间会出现设置界面，选择<是>即可。

这样，新版 wireshark 就安装完成了，界面和 windows 版本一样。

旧版 wireshark 的回退#
新版 wireshark 功能如此强大，为什么要回退呢，因为配置跟不上啊。。。电脑配置不高，发现使用新版 wireshark 抓包时时常会发生卡顿现象，删除重装之后居然发现装上的还是新版的 wireshark。。

然后想起来在升级的时候添加了官方 ppa 源，所以在以后的安装中都会默认安装更新的版本。

首先删除 ppa 源

cd /ect/apt/sources.list.d
找到新版 wireshark 的 ppa 源的保存文件删除，具体的名字忘记了，是 wireshark 开头的一个文件

sudo rm wiresharkxxxx
更新 apt-get

sudo apt-get update
删除 wireshark 和相关的 wireshark-common，否则安装老版本时会提示失败

sudo apt-get remove wireshark
sudo apt-get remove wireshark-common
然后就可以安装老版本的 wiershark 了

sudo apt-get install wireshark

##

ssh xgc@172.16.106.65 "echo xgc| sudo -S tcpdump -i any -l -w - " | wireshark -k -i -
