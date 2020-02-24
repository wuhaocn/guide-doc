## JDK下载过慢的问题解决方案

下载JDK的时候，10k每秒的速度简直难以忍受，下面列出了一些解决方法

### 可用的加速方法
将下载链接去掉https
由于下载时默认是https，所以会慢一些，使用http之后虽然还是慢，但是也能稳定在400k左右，不急的话这速度十分钟之内就可以下完，还可以接受
使用下载工具下载
使用诸如百度网盘、迅雷等下载工具下载，流氓加速，快的一批
### 国内各大公司的镜像站
比如华为的：https://repo.huaweicloud.com/java/jdk/
### 你懂的方法
坐飞机去国外，下完再回来，perfect.
### 文件校验
歪门邪道下载来的东西肯定要检查一下，可以去oracle官网查查checksum，然后和本地的对比一下，如果一致的话就没问题了：
https://www.oracle.com/webfolder/s/digest/8u211checksum.html
（将地址中的 8u211替换为你需要检查的jdk版本）

2020年2月21日更新：
oracle的官网发生了变化，界面更好看了，也支持中文了，我试了一下下载jdk直接就是满速，大概jdk8停止免费支持然后准备收割中国市场？

下面是官网链接，直接下吧，不需要折腾了(需要使用甲骨文账号登录)：
https://www.oracle.com/java/technologies/javase-downloads.html

openjdk的下载页：http://jdk.java.net/
oracle license页：www.oracle.com/oracle-javase-license.html

原文链接：https://blog.csdn.net/qq_29753285/article/details/93992594