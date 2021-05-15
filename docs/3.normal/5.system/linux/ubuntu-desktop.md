## ubuntu 远程桌面连接

### server 端安装

- 桌面系统

```
sudo apt update
sudo apt install ubuntu-desktop
```

or

```
sudo apt update
sudo apt install xubuntu-desktop
```

- xrdp 安装

```
sudo apt install xrdp

sudo systemctl status xrdp


sudo adduser xrdp ssl-cert

sudo systemctl restart xrdp
```

- 配置防火墙

```
单个ip
sudo ufw allow from 192.168.33.0/24 to any port 3389

所有ip
sudo ufw allow 3389
```

### 客户端连接

- mac 下载

https://mac.softpedia.com/get/Utilities/Microsoft-Remote-Desktop-Connection.shtml#download
