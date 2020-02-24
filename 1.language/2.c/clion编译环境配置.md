## Ubuntu下Clion中的编译环境的配置

本文简要介绍一下Ubuntu下Clion中的编译环境的配置。

### 一.问题出现
首次安装使用clion出现报错：
```
CMake Error: CMake was unable to find a build program corresponding to "Unix Makefiles".  CMAKE_MAKE_PROGRAM is not set.  You probably need to select a different build tool.CMake Error: CMAKE_C_COMPILER not set, after EnableLanguageCMake Error: CMAKE_CXX_COMPILER not set, after EnableLanguage-- Configuring incomplete, errors occurred!See also "/home/lina/CLionProjects/HelloWorld/cmake-build-debug/CMakeFiles/CMakeOutput.log".[Failed to reload]
```
显然是C++ 的编译环境没有配置好。

### 二.解决问题
Ubuntu中用到的编译工具是gcc(C)，g++（C++），make(连接)。因此只需安装对应的工具包即可。Ubuntu下使用命令安装这些包：

* 1.安装gcc
```
sudo apt install gcc
```
* 2.安装g++

```
sudo apt install g++
```
* 3.安装make
```
sudo apt install make
```
* 4.然后将它们的执行路径填入
```
File--Settings--Build,Execution,Deployment--Toolchains相应的位置
```
配置

* 没有红色报错表明配置成功！

### 三.测试效果
运行一个程序，查看结果：


成功运行程序表明配置成功！