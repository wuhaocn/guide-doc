## ubuntu 安装 git 及 git 命令

### 1、检查 git 是否已经安装

```
输入
git version
命令即可，如果没有显示版本号表示没有安装git
```

### 2、安装 git

```
sudo apt-get install git
```

### 3、配置 git 全局环境

```
git config --global user.name "用户名"
git config --global user.email "邮箱地址"
```

### 4、生成 ssh 密钥

```
ssh-keygen -C 'wuhaocn@126.com' -t rsa
会在用户目录~/.ssh/下建立相应的密钥文件。
```

### 5、创建完公钥后，需要上传。

```
使用命令cd ~/.ssh进入~/.ssh文件夹，输入
cat id_rsa.pub
打开id_rsa.pub文件，复制其中所有内容。
接着访问git网页，点击SSH公钥，标题栏可以随意输入，公钥栏把刚才复制的内容粘贴进去。

创建一个空的目录，初始化git仓库，添加远程仓库做测试
```

### 6、测试连接

```
命令：
ssh -T git@github.com
成功提示：
Warning: Permanently added the RSA host key for IP address '52.74.223.119'
to the list of known hosts.
Hi dingdingingitHub! You've successfully authenticated,
but GitHub does not provide shell access.
```

### 7、git 使用命令

```
git clone 项目地址  拉项目
git pull    拉代码
git push  提交到仓库
git init指令初始化一个git仓库
git add .添加文件
git commit -m "注释"提交至仓库。
git remote add origin https://git.oschina.net/你的用户名/项目名.
git，git push origin master即可完成推送
git checkout master   切换到master分支
```
