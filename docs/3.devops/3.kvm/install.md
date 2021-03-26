## kvm 环境安装及依赖

### 1.安装 KVM

- 1.安装 KVM 及依赖项

```
sudo apt update
sudo apt install qemu qemu-kvm bridge-utils  virt-manager
```

- 2.启动 libvirtd 服务，并设置开机自动启动

```
执行service libvirtd status查看libvirtd服务状态
sudo systemctl start libvirtd.service
sudo systemctl enable libvirtd.service
```

### 2.镜像导入

- 1.查看运行镜像

```
virsh list
 Id   Name   State
--------------------
```

-

### 2. 网络配置

一般虚拟机网络配置有 Bridge、NAT 等几种模式。NAT 模式下，虚拟机不需要配置自己的 IP，通过宿主机来访问外部网络；Bridge 模式下，
虚拟机需要配置自己的 IP，然后虚拟出一个网卡， 与宿主机的网卡一起挂到一个虚拟网桥上（类似于交换机）来访问外部网络，这种模式下，虚拟机拥有独立的 IP，局域网其它主机能直接通过 IP 与其通信。简单理解，就是 NAT 模式下，虚机隐藏在宿主机后面了，虚机能通过宿主机访问外网，但局域网其它主机访问不到它，Bridge 模式下，虚机跟宿主机一样平等地存在，局域网其它主机可直接通过 IP 与其通信。一般我们创建虚机是用来部署服务供使用的， 所以都是用 Bridge 模式。

ubuntu 18 中，网络配置通过 netplan 来实现了，如下，更改配置文件 /etc/netplan/50-cloud-init.yaml

```
devuser@cserver_01:~$ sudo vim /etc/netplan/50-cloud-init.yaml
# This file is generated from information provided by
# the datasource.  Changes to it will not persist across an instance.
# To disable cloud-init's network configuration capabilities, write a file
# /etc/cloud/cloud.cfg.d/99-disable-network-config.cfg with the following:
# network: {config: disabled}
network:
    ethernets:
        enp6s0:
            dhcp4: true
        enp7s0:
            dhcp4: no
            dhcp6: no
    version: 2

    bridges:
         br0:
             interfaces: [enp7s0]
             dhcp4: no
             addresses: [192.168.40.241/24]
             gateway4: 192.168.40.1
             nameservers:
                 addresses: [114.114.114.114,8.8.8.8]
```

### 参考

[KVM 安装](https://www.cnblogs.com/spec-dog/p/11178181.html)
