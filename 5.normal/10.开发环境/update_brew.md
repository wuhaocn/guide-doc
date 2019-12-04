### 替换brew默认源
```properties
第一步，替换brew.git

cd "$(brew --repo)"
git remote set-url origin https://mirrors.ustc.edu.cn/brew.git
第二步：替换homebrew-core.git

cd "$(brew --repo)/Library/Taps/homebrew/homebrew-core"
git remote set-url origin https://mirrors.ustc.edu.cn/homebrew-core.git
最后使用

  brew update
进行更新，发现速度变的很快。替换镜像完成。

```


### Home Brew 安装和卸载
```properties


1.安装Home Brew

1·进入官网获取下载命令

官网：http://brew.sh/

执行命令：/usr/bin/ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"

2.卸载Home Brew

执行命令：/usr/bin/ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/uninstall)"  

3.brew 命令

  brew search [TEXT|/REGEX/]

  brew info [FORMULA...]

  brew install FORMULA...

  brew update

  brew upgrade [FORMULA...]

  brew uninstall FORMULA...

  brew list [FORMULA...]

Troubleshooting:

  brew config

  brew doctor

  brew install --verbose --debug FORMULA

Contributing:

  brew create [URL [--no-fetch]]

  brew edit [FORMULA...]

Further help:

  brew commands

  brew help [COMMAND]

  man brew

  https://docs.brew.sh

```
