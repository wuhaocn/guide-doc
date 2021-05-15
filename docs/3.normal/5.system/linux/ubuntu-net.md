## ubuntu 网络配置

### static

```
vim /etc/netplan/00-installer-config.yaml
```

```
# This is the network config written by 'subiquity'
network:
 ethernets:
   enp1s0:
     dhcp4: no
     dhcp6: no
     addresses:
       - 172.16.160.103/24
     gateway4: 172.16.160.100
     nameservers:
       addresses: [8.8.8.8, 114.114.114.114]
     routes:
       - to: 192.168.200.0/24
         via: 172.16.160.100
       - to: 192.168.210.0/24
         via: 172.16.160.100
       - to: 192.168.220.0/24
         via: 172.16.160.100
       - to: 222.222.2.0/24
         via: 172.16.160.102
         table: 101
     routing-policy:
       - from: 0.0.0.0/24
         table: 101
 version: 2
```

```
sudo netplan apply
```

### dynamic

- 命令配置:机器重启后配置生效

```
sudo route add -net 192.168.200.0/24 gw 172.16.160.100
sudo route add -net 192.168.210.0/24 gw 172.16.160.100
sudo route add -net 192.168.220.0/24 gw 172.16.160.100
```

### nat

- iproute

```
sudo route add -net 222.1.0.0/16 gw 172.16.106.129


sudo route add -net 222.1.0.0/16 gw 172.16.106.77


sudo route del -net 222.1.0.0/16 gw 172.16.106.129

sudo route add -net 222.1.0.0/16 gw 172.16.106.72
```

- nat

```
sudo echo 1 > /proc/sys/net/ipv4/ip_forward
sudo iptables -t nat -A POSTROUTING -s 222.100.0.0/16 -o ens160 -j MASQUERADE
sudo iptables -t nat -D POSTROUTING -s 222.100.0.0/16 -o ens160 -j MASQUERADE
```
