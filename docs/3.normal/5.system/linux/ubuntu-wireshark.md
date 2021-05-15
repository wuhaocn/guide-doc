## 新版 wireshark 的升级

- 普通方法安装 wireshark 最便捷的方式是

```
sudo apt-get install wireshark
```

虽然一个命令就能搞定，但是，wireshark 的版本过于老旧，和 win 平台的版本对应不上，
最关键的是不支持 MPTCP 的直接解析，使用起来十分蛋疼。
但是，实际上，我们已经可以在 Ubuntu 平台升级较新版本的 wireshark 了，具体步骤如下。
直接下载即可，会自动覆盖老版的 wireshark。

- 添加 wireshark 官方密钥并升级

```
sudo apt-add-repository ppa:wireshark-dev/stable
sudo apt-get update
```

- 安装 wireshark

```
sudo apt-get install wireshark
```
