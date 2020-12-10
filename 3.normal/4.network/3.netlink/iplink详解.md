linux中的 ip addr 和 ip link命令详解
#### 一、ip addr命令
我是使用的linux系统是redhat7.3，其它linux的相关操作大同小异(在这里不做赘述)

1.查看

(1). ip addr 的缩写是ip a ，可以查看网卡的ip、mac等， 

即使网卡处于down状态，也能显示出网卡状态，但是ifconfig查看就看不到。

(2).ip addr show device  查看指定网卡的信息

比如查看网卡接口的信息，就是ip addr show eth0

 
2.增加ip

ip addr add ip/netmask dev 接口

比如给eth0增加一个172.25.21.1/24 地址

ip addr add 172.25.21.1/24 dev eth0

3.删除ip

ip addr del ip/netmask dev 接口   

4.清空指定网卡的所有ip

ip addr flush dev 接口        

 5. 给网卡起别名，起别名相当于给网卡多绑定了一个ip  

 用法： 比如给网卡eth0增加别名                

 ip addr add 172.25.21.1/32 dev eth0 label eth0:1

 6.删除别名              

 ip addr del ip/netmask dev eth0 

注意:

使用命令的方式设置别名，重启服务就没了，若要永久生效，

需要写配置文件，步骤如下：

1、确保NetworkManager服务是停止的

    systemctl stop NetworkManager    关闭该服务 

    systemctl disable NetworkManager  开机不自启动

   注意：

使用命令的方式增加或者删除ip，都是临时的，

如果重启network服务，那么操作就失效了。

想要永久生效可以修改配置文件ifcfg-eth0

#### 二、ip link  命令
 1.查看

  ip link只能看链路层的状态，看不到ip地址

 2.启用、禁用接口

  ip link set device down   禁用指定接口

  ip link set device up    启用指定接口

  比如禁用eth0就是ip link set eth0 down

说明：

    ip link不支持tab键补齐

#### 参考
————————————————
版权声明：本文为CSDN博主「huige永生」的原创文章，遵循 CC 4.0 BY-SA 版权协议，转载请附上原文出处链接及本声明。
原文链接：https://blog.csdn.net/qq_43309149/java/article/details/104481743