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
