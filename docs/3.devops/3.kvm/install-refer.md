### kvm 安装

前一阵使用在最新的 Ubuntu 18.04 上安装了 KVM 来虚拟一个小的 VM 集群，将主要过程和其中遇到的一些问题记录下来。

#### 1.准备工作

首先需要检查一下 CPU 是否支持虚拟化，执行一下命令来检查/proc/cpuinfo 文件中是否又虚拟化相关的字眼，如果有的话表明 CPU 支持虚拟化技术。

egrep -c '(svm|vmx)' /proc/cpuinfo
上面命令执行结果如果返回 0，表示 CPU 不支持虚拟化技术。当然主板 BIOS 中的虚拟化技术也可能不是默认开启的，如果没有开启需要手动开启一下。

#### 2.安装 KVM

执行以下命令安装 KVM

```
sudo apt update
sudo apt install qemu qemu-kvm libvirt-bin bridge-utils virt-manager
```

将 libvirtd 添加自启动

```
sudo systemctl start libvirtd.service
sudo systemctl enable libvirtd.service
```

#### 3.网络模式

```
KVM安装完成后，首先需要进行网络设定，KVM支持四种网络模式：

桥接模式
NAT模式
用户网络模式
直接分配设备模式
主要讲一下前两种
```

- 桥接（Bridge）模式

```
在桥接模式下，宿主机和虚拟机共享同一个物理网络设备，虚拟机中的网卡和物理机中的网卡是平行关系，所以虚拟机可以直接接入外部网络，虚拟机和宿主机有平级的IP。

原本宿主机是通过网卡eth0连接外部网络的，网桥模式会新创建一个网桥br0，接管eth0来连接外部网络，然后将宿主机和虚拟机的网卡eth0都绑定到网桥上。

使用桥接模式需要进行以下操作：

编辑/etc/network/interfaces，增加如下内容

auto br0
iface br0 inet dhcp # 网桥使用DHCP模式，从DHCP服务器获取IP
bridge_ports enp3s0 # 网卡名称，网桥创建前连接外部的网卡，可通过ifconfig命令查看，有IP地址的就是
bridge_stp on # 避免数据链路出现死循环
bridge_fd 0 # 将转发延迟设置为0
接下来需要重启networking服务（如果是通过SSH连接到宿主机上的，这一步会导致网络中断，如果出现问题可能导致连不上宿主机，最好在宿主机上直接操作）


systemctl restart networking.service
使用ifconfig命令查看IP是否从enp3s0（网桥创建前的网卡）变到了br0上，如果没有变化则需要重启。如果宿主机ip已经成功变到网桥上，并且宿主机能正常上网而虚拟机获取不到ip，可能是ufw没有允许ip转发导致的，编辑/etc/default/ufw允许ip转发。

DEFAULT_FORWARD_POLICY="ACCEPT"
重启ufw服务让设置生效

systemctl restart ufw.service
NAT（Network Address Translation）模式

NAT模式是KVM默认的网络模式，KVM会创建一个名为virbr0的虚拟网桥，但是宿主机和虚拟机对于网桥来说不是平等的了，网桥会把虚拟机藏在背后，虚拟机会被分配一个内网IP，而从外网访问不到虚拟机。
```

#### 3. 安装 Linux 虚拟机

使用如下命令安装安装 Linux 虚拟机

```
sudo virt-install -n ubuntu_3
 --description "ubuntu_3"
 --os-type=linux --os-variant=ubuntu17.10 --ram=1024 --vcpus=1
 --disk path=/var/lib/libvirt/images/ubuntu_3.img,bus=virtio,size=50 # 磁盘位置，大小50G
 --network bridge:br0 # 这里网络选择了桥接模式
 --accelerate
 --graphics vnc,listen=0.0.0.0,keymap=en-us # VNC监听端口，注意要选择en-us作为key-map，否则键位布局可能会乱
 --cdrom /home/zzy/Downloads/ubuntu-18.04-live-server-amd64.iso # 安装ISO路径
```

#### 4. 安装 Windows 10 虚拟机

```
安装Windows 10虚拟机会出现没有virtio驱动的问题，导致安装程序找不到硬盘，需要先下载virtio驱动。

https://fedoraproject.org/wiki/Windows_Virtio_Drivers

创建虚拟机时，将其加入到CD-ROM中


sudo virt-install -n win10
 --description "win10"
 --os-type=win --os-variant=win10
 --ram=4096 --vcpus=2
 --disk path=/var/lib/libvirt/images/win_10.img,bus=virtio,size=100
 --network bridge:br0
 --accelerate
 --graphics vnc,listen=0.0.0.0,keymap=en-us
 --cdrom =/home/zzy/Downloads/cn_windows_10_consumer_editions_version_1803_updated_march_2018_x64_dvd_12063766.iso
--cdrom=/home/zzy/Downloads/virtio-win.iso
使用VNC客户端连接虚拟机


执行以下命令查看虚拟机的列表


sudo virus list
通过上一步查处的虚拟机列表，查看单台机器的VNC端口


sudo virsh vncdisplay ubuntu_3 # ubuntu_3是虚拟机名称
知道了VNC端口号，就可以使用VNC客户端连接到虚拟机完成安装了。

以上就是本文的全部内容，希望对大家的学习有所帮助，也希望大家多多支持脚本之家。
```
