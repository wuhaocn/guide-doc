### linux 文件修复

```
linux文件系统修复：The root filesystem on /dev/sda1 requires a manual fsck

1. 问题描述
dev/sda1: Inodes that were part of a corrupted orphan linked list found.
/dev/sda1: UNEXPECTED INCONSISTENCY:; RUN fsck MANUALLY
        (i.e., without -a or -p options)
fsck exited with status code 4
The root filesystem on /dev/sda1 requires a manual fsck
modprobe: module ehci-orion not found in modules.dep

BusyBox v1.22.1 (Debian 1:1.22.0-9+deb8u1) built-in shell (ash)
Enter 'help' for a list of built-in commands.

/bin/sh: can't access tty; job control turned off
(initramfs) _
出错原因：磁盘检测不能通过，可能是因为系统突然断电或其它未正常关闭系统导致。

2. 解决方法
(initramfs) fsck /dev/sda1   // 修复对象取决于出错的对象，可以是一块磁盘或者一个VG
```
