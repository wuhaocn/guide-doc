linux 中的 ip addr 和 ip link 命令详解

#### 一、ip addr 命令

我是使用的 linux 系统是 redhat7.3，其它 linux 的相关操作大同小异(在这里不做赘述)

1.查看

(1). ip addr 的缩写是 ip a ，可以查看网卡的 ip、mac 等，

即使网卡处于 down 状态，也能显示出网卡状态，但是 ifconfig 查看就看不到。

(2).ip addr show device   查看指定网卡的信息

比如查看网卡接口的信息，就是 ip addr show eth0

2.增加 ip

ip addr add ip/netmask dev 接口

比如给 eth0 增加一个 172.25.21.1/24 地址

ip addr add 172.25.21.1/24 dev eth0

3.删除 ip

ip addr del ip/netmask dev 接口

4.清空指定网卡的所有 ip

ip addr flush dev 接口

5. 给网卡起别名，起别名相当于给网卡多绑定了一个 ip

用法： 比如给网卡 eth0 增加别名

ip addr add 172.25.21.1/32 dev eth0 label eth0:1

6.删除别名

ip addr del ip/netmask dev eth0

注意:

使用命令的方式设置别名，重启服务就没了，若要永久生效，

需要写配置文件，步骤如下：

1、确保 NetworkManager 服务是停止的

systemctl stop NetworkManager    关闭该服务

systemctl disable NetworkManager  开机不自启动

注意：

使用命令的方式增加或者删除 ip，都是临时的，

如果重启 network 服务，那么操作就失效了。

想要永久生效可以修改配置文件 ifcfg-eth0

#### 二、ip link  命令

1.查看

ip link 只能看链路层的状态，看不到 ip 地址

2.启用、禁用接口

ip link set device down    禁用指定接口

ip link set device up     启用指定接口

比如禁用 eth0 就是 ip link set eth0 down

说明：

ip link 不支持 tab 键补齐

#### 参考

————————————————
版权声明：本文为 CSDN 博主「huige 永生」的原创文章，遵循 CC 4.0 BY-SA 版权协议，转载请附上原文出处链接及本声明。
原文链接：https://blog.csdn.net/qq_43309149/java/article/details/104481743
