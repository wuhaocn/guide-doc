- 1.安装 KVM 及依赖项

```
sudo apt update
sudo apt install qemu qemu-kvm bridge-utils  virt-manager
```

- 2.启动 libvirtd 服务，并设置开机自动启动

```
docker 安装

sudo apt-get update

sudo apt install docker.io

sudo groupadd docker     #添加docker用户组
sudo gpasswd -a $USER docker     #将登陆用户加入到docker用户组中
sudo usermod -aG docker $USER
sudo newgrp docker     #更新用户组

sudo vim /etc/docker/daemon.json
	root@user1-virtual-machine:~# cat /etc/docker/daemon.json

	{
	 "storage-driver":"overlay",
	 "insecure-registries": ["10.10.208.193:5000"]
	}
sudo service docker restart
```
