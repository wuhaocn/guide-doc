## ubuntu 开启 root 用户

- 备注 系统版本

```
Ubuntu 18.04.3 LTS
```

### 1.设置 root 用户密码

```
wuhao@ubuntu-linux:~$ sudo passwd root
[sudo] password for wuhao:
Enter new UNIX password:
Retype new UNIX password:
passwd: password updated successfully
```

### 2.测试 root 用户

```
wuhao@ubuntu-linux:~$ su root
Password:
root@ubuntu-linux:/home/wuhao#
```

### 3.配置 root 用户登录

```
gedit /usr/share/lightdm/lightdm.conf.d/50-ubuntu.conf

user-session=ubuntu
greeter-show-manual-login=true
all-guest=false
```

---

```
16.04

sudo gedit  /etc/lightdm/lightdm.conf

[Seat:*]
autologin-guest=false
autologin-user=root
autologin-user-timeout=0
greeter-session=lightdm-gtk-greeter
```

```
gedit /etc/gdm3/custom.conf

# Enabling automatic login
AutomaticLoginEnable = true
AutomaticLogin = root
```

```
gedit /etc/pam.d/gdm-autologin
gedit /etc/pam.d/gdm-password
注释或者删除，两个文件都需要改
# auth	required	pam_succeed_if.so user != root quiet_success
```

### 4.登录环境设置

```
gedit /root/.profile
```

```
mesg n || true
改为
tty -s && mesg n || true
```

### 5.重启

```
sudo reboot
```
