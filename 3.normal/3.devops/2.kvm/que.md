```
问题产生原因：
在对虚拟机扩容时候，由于linux系统没有对其磁盘信息进行更新，导致了磁盘实际容量和linux系统容量不一致

报错的地方：
1、使用sudo fdisk -l查看磁盘信息时报错：GPT PMBR size mismatch will be corrected by w(rite)错误
2、使用sudo fdisk /dev/sda 进行虚拟机磁盘分区扩容时报错：明明有多余的空间，却显示value out of range

解决办法：
# 执行命令：
sudo parted -l

# 然后输入：
Fix


再次执行sudo fdisk -l，可以看到问题解决。

```