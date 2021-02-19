###

echo 1 > /proc/sys/net/ipv4/ip_forward
#iptables -t nat -A POSTROUTING -s 222.222.1.0/24 -o enp1s0 -j SNAT --to-source 172.16.160.102
#iptables -t nat -A POSTROUTING -s 222.222.2.0/24 -o enp1s0 -j SNAT --to-source 172.16.160.103
iptables -t nat -A POSTROUTING -s 200.200.100.0/24 -o ens160 -j MASQUERADE
iptables -t nat -A POSTROUTING -s 200.200.2.0/24 -o enp0s5 -j MASQUERADE
#iptables -t nat -A POSTROUTING -o enp1s0 -j MASQUERADE

sudo iptables -P INPUT ACCEPT
sudo iptables -P FORWARD ACCEPT
sudo iptables -P OUTPUT ACCEPT
sudo iptables -F

sudo iptables -t nat -A POSTROUTING -s 222.100.0.0/16 -o ens160 -j MASQUERADE
sudo iptables -t nat -D POSTROUTING -s 222.100.0.0/16 -o ens160 -j MASQUERADE

sudo iptables -t nat -A POSTROUTING -s 222.100.0.0/16 -o ens160 -j SNAT --to-source 172.16.106.77

1.sudo ufw status 查看当前防火墙状态

2.sudo ufw enable

sudo ufw disable

sudo /etc/init.d/ufw restart

3.sudo vi /etc/default/ufw

DEFAULT_FORWARD_POLICY = "ACCEPT" 允许转发

4.sudo vim /etc/ufw/sysctl.conf

net/ipv4/ip_forward=1

net/ipv6/conf/default/forwarding=1

5.sudo vim /etc/ufw/before.rules

#nat 规则

\*nat

:POSTROUTING ACCEPT [0:0]

#将来自 eth1 的数据包转发给 eth0

-A POSTROUTING -s 192.168.0.0/24 -o eth0 -j MASQUERADE

COMMIT

6.sudo ufw logging on/off
