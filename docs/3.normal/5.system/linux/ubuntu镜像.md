## 官方工具

http://rufus.ie/zh/

## 安装

使用 U 盘作为启动盘安装双系统中的 Ubuntu16.04 时遇到了一些问题折腾了好久，特此记录下来！

### 一、unable to find a medium containing a live file system 错误

问题： 再安装 ubuntu 的刚开始，显示看到了 ubuntu 的加载图标，但是过一会布拉布拉。。。的弹出黑色命令框，打出一堆错误信息，最后一行显示：(initramfs)unable to find a medium containing a
live file system 错误。

解决方案： 出现的原因是因为 U 盘 USB 版本和电脑 USB 版本的兼容性问题，导致无法识别 USB。可以采用如下两种方法： （1）推荐这种方法很管用：
就是在打开 ubuntu 安装界面前，走进度条时，快速把 U 盘拔出来再插进去，这样电脑就可以正确找到 U 盘并安装了。 具体拔出来再插进去的时机，就是在下面这个界面显示的时候 ↓ 在这里插入图片描述
这样就能开到后续的安装界面了，没成功的或者卡住了，重启多来几次就好了。

（2）第二种方法必须得自己的电脑有 USB2.0 的接口，把启动 U 盘插在这个 USB 接口上即可。我的电脑没有，所以只能采用第一种方法。

### 二、ACPI Error 错误，无法启动安装界面

问题： 当 UEFI 模式下 U 盘启动，进入选择安装 Ubuntu 的界面后，选择第二项“Install Ubuntu”回车会出现报 ACPI Error 错，部分信息如下所示： 在这里插入图片描述 解决方案： 在刚才选择“Install
Ubuntu”时不按回车键，而是按“E”键可以进入一个黑色文本编辑框，在“linux”这一行的末尾加上：acpi=off

### 三、双系统启动如何弹出选择启动系统类别

首 先 进 入 电 脑 B I O S 设 置 后 把 b o o t 的 l i n u x 启 动 项 调 到 w i n d o w s 上 面 即 可 ， 之 后保 存 重 启 即 可 进 入 每 次 就 有 选 系 统 的 界 面
了 。 如 下 图 ：

在这里插入图片描述上 面 为 l i n u x 启 动 项 下 面 为 w i n d o w s 启 动 项 。
