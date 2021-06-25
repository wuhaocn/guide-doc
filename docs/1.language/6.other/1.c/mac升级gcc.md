# 升级 Mac 中的 GCC 版本

Mac 中自带的 GCC 版本是 4.2.1，由于版本太低，在很多操作的时候会报错。
因此需要对其进行升级，这里使用 Homebrew 来下载最新的 GCC。

## 安装 Homebrew

macOS 的终端中输入如下的命令来安装 Homebrew

```
$ /usr/bin/ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"
```

安装完之后查看安装的版本

```
$ brew --version
```

正确显示版本号便是安装成功

使用 Homebrew 来安装 GCC

## 查找下最新的 GCC 版本

```
$ brew search gcc
```

显示如下的信息：

```
==> Formulae
gcc                                      homebrew/linuxbrew-core/gcc@4.9
gcc@4.9                                  homebrew/linuxbrew-core/gcc@5
gcc@5                                    homebrew/linuxbrew-core/gcc@6
gcc@6                                    homebrew/linuxbrew-core/gcc@7
gcc@7                                    homebrew/linuxbrew-core/gcc@8
gcc@8                                    homebrew/linuxbrew-core/gcc@9
i386-elf-gcc                             homebrew/linuxbrew-core/i386-elf-gcc
homebrew/linuxbrew-core/gcc
```

## 安装 gcc@8

使用如下的命令进行安装：

```
$ brew install gcc@8
```

安装完之后查看下版本：

```
$ gcc-8 -v
```

正确显示版本号就是安装正确。

## 配置 GCC

虽然已经成功安装了 GCC8，但是在终端中输入 GCC 其实还是使用之前的 GCC 4.2.1。因此得进行设置让 gcc 命令指向 gcc-8

这里通过设置用户的环境变量对 gcc 命令设置别名来实现上述效果。

```
$ sudo vi ~/.bash_profile
```

在文件下面添加如下的内容：

```
alias gcc='gcc-8'
alias cc='gcc-8'
alias g++='g++-8'
alias c++='c++-8'
```

## 刷新环境变量：

```
$ source ~/.bash_profile
```

之后在终端中查看 gcc 命令的版本：

```
$ gcc -v
```

显示如下：

```
gcc version 8.3.0 (Homebrew GCC 8.3.0)
```

设置成功。
