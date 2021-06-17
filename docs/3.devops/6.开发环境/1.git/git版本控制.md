# git 分支维护建议

## 1.分支维护

- master: 稳定运行
- dev: 日常开发小改动
- feature: 大功能 or 影响较大采用
- hotfixes: bug 修复采用，非紧急可以与 dev 合并

![](http://wuhaocn.top:8082/file/get/img/57208cee-a93b-497f-817b-005b3132ce52)

## 2.分支说明

### 2.1.master 分支

存放的应该是随时可供在生产环境中部署的代码
当开发活动告一段落，产生了一份新的可供部署的代码时，master 分支上的代码会被更新。同时，每一次更新，都有对应的版本号标签（TAG）。
分支命名：master
该分支，由管理员负责维护，其它人只有拉取权限。来自于 release 分支的合并，供发版使用
生命周期：伴随着整个项目的生命周期，项目结束时结束。

### 2.2.develop 分支

develop 分支是每次迭代版本的共有开发分支，从最新的 master 分支派生（管理员操作）
当 develop 分支上的代码已实现了软件需求说明书中所有的功能，派生出 release 分支（管理员操作）
分支命名：dev-版本号
该分支，由开发人员在各自的 feature 分支开发完成后，合并至该分支。
生命周期：一个阶段功能开发开始到本阶段结束

### 2.3.release 分支

从 develop 分支派生（管理员操作）
测试环境中出现的 bug，统一在该分支下进行修改，并推送至远程分支。修改内容必须合并回 develop 分支和 master 分支。
分支命名惯例：release-版本号
生命周期：一个阶段功能开发结束开始，完成阶段功能测试并修复所有发现 bug，合并会 develop 分支结束。

### 2.4.feature 分支

在开发一项新的软件功能的时候使用，这个分支上的代码变更最终合并回 develop 分支
分支命名惯例：feature-姓名全拼-分支说明-日期 / feature-分支说明-日期
例：接到一个开发关于 cc 视频点播替换的任务，你需要从 develop 分支拉出一个分支，并命名为：release-yuruixin-ccVideo-20171117。然后在该分支下进行开发，开发结束，将该分支合并至 develop 分支（此时的代码必须为可运行的，不能影响到他人），合并完成删掉该特性分支。
开发人员的每一个新功能开发都应该在该类分支下进行。
生命周期：开发一个新功能开始，完成新功能开发并合并回 develop 分支结束。

### 2.5.hotfixes 分支

在 master 分支发现 bug 时，在 master 的分支上派生出一个 hotfixes 分支，修改完成后，合并至 master 分支以及 develop 分支，合并完成，删除该 hotfixes 分支。
分支命名惯例：hotfixes-姓名全拼-分支说明-日期
示例：hotfixes-yuruixin-cclivebug-20171117
生命周期：发现 master 分支 bug 开始，完成 master 分支 bug 结束。

## 参考

- [使用 git 进行项目版本管理](https://blog.csdn.net/yuruixin_china/article/details/79061999)
