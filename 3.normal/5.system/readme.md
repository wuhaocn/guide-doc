# 操作系统

## 计算机原理

* [《操作系统基础知识——操作系统的原理，类型和结构》](https://segmentfault.com/a/1190000003692840)

## CPU

### 多级缓存
典型的 CPU 有三级缓存，距离核心越近，速度越快，空间越小。L1 一般 32k，L2 一般 256k，L3 一般12M。内存速度需要200个 CPU 周期，CPU 缓存需要1个CPU周期。

* [《从Java视角理解CPU缓存和伪共享》](https://blog.csdn.net/zero__007/article/details/54089730)

## 进程

TODO

## 线程

* [《线程的生命周期及状态转换详解》](https://blog.csdn.net/asdf_1024/article/details/78978437)

## 协程

* [《终结python协程----从yield到actor模型的实现》](https://www.thinksaas.cn/group/topic/839375/)
	* 线程的调度是由操作系统负责，协程调度是程序自行负责
	* 与线程相比，协程减少了无谓的操作系统切换.
	* 实际上当遇到IO操作时做切换才更有意义，（因为IO操作不用占用CPU），如果没遇到IO操作，按照时间片切换.
	
## Linux

* [《Linux 命令大全》](http://www.runoob.com/linux/linux-command-manual.html)