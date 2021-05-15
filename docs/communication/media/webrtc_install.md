### 一.安装 depot_tools

- git
- python3

#### 1.1 创建 webrtc 目录，安装 depot_tools

```
mkdir webrtc
cd  webrtc
git clone https://chromium.googlesource.com/chromium/tools/depot_tools.git
```

```
如果超时
git config --global http.proxy "localhost:51972"
git clone https://chromium.googlesource.com/chromium/tools/depot_tools.git
```

注：(这里需要不可描述的上网手段，不然会报错误)

#### 1.2 配置个人环境变量中：

```
export WORKSPACE=$(pwd)
export PATH=$WORKSPACE/depot_tools:$PATH
```

### 二.下载源码

```
export http_proxy=http://127.0.0.1:51972/
export https_proxy=$http_proxy

fetch --nohooks webrtc
```

然后就是等待下载完成
下载完成以后执行

```
gclient sync
```

注:中途下载失败可以反复执行 gclient sync 命令

### 三.创建自己的开发分支

#### 3.1 指定代码的追踪方式

```
git config branch.autosetupmerge always
git config branch.autosetuprebase always
```

注：建议使用第一种，合并代码比较安全，不容易丢失

#### 3.2 设定自己的开发分支

```
git branch tyh_myStudy
git checkout tyh_myStudy
```

### 四.生成 Xcode 项目

```
gn gen out/ios --args='target_os="ios" target_cpu="arm64"' --ide=Xcode
```

这个编译会报证书相关的错误，修改为以下命令

```
gn gen out/ios --args='target_os="ios" target_cpu="arm64" ios_enable_code_signing=true ios_code_signing_identity="证书名"' --ide=Xcode
```

或者使用以下命令，不带证书生成项目

```
gn gen out/ios --args='target_os="ios" target_cpu="arm64" is_component_build=false ios_enable_code_signing=false' --ide=xcode
```

使用如下命令获取设备上的证书名

```
security find-identity -v -p codesigning
```

### 五. 编译项目

```
ninja -C out/ios_64 AppRTCMobile
```

作者：搬砖的作家
链接：https://www.jianshu.com/p/a2e1bd391547
来源：简书
著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。
