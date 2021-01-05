# 运维 & 统计 & 技术支持

## 常规监控

- [《腾讯业务系统监控的修炼之路》](https://blog.csdn.net/enweitech/article/details/77849205)
  _ 监控的方式：主动、被动、旁路(比如舆情监控)
  _ 监控类型： 基础监控、服务端监控、客户端监控、
  监控、用户端监控
  _ 监控的目标：全、块、准
  _ 核心指标：请求量、成功率、耗时

- [《开源还是商用？十大云运维监控工具横评》](https://www.oschina.net/news/67525/monitoring-tools) \* Zabbix、Nagios、Ganglia、Zenoss、Open-falcon、监控宝、 360 网站服务监控、阿里云监控、百度云观测、小蜜蜂网站监测等。

- [《监控报警系统搭建及二次开发经验》](http://developer.51cto.com/art/201612/525373.htm)

**命令行监控工具**

- [《常用命令行监控工具》](https://coderxing.gitbooks.io/architecture-evolution/di-er-pian-ff1a-feng-kuang-yuan-shi-ren/44-an-quan-yu-yun-wei/445-fu-wu-qi-zhuang-tai-jian-ce/4451-ming-ling-xing-gong-ju.html) \* top、sar、tsar、nload

- [《20 个命令行工具监控 Linux 系统性能》](http://blog.jobbole.com/96846/)

- [《JVM 性能调优监控工具 jps、jstack、jmap、jhat、jstat、hprof 使用详解》](https://my.oschina.net/feichexia/blog/196575)

## APM

APM — Application Performance Management

- [《Dapper，大规模分布式系统的跟踪系统》](http://bigbully.github.io/Dapper-translation/)

- [CNCF OpenTracing](http://opentracing.io)，[中文版](https://github.com/opentracing-contrib/opentracing-specification-zh)

- 主要开源软件，按字母排序

  - [Apache SkyWalking](https://github.com/apache/incubator-skywalking)
  - [CAT](https://github.com/dianping/cat)
  - [CNCF jaeger](https://github.com/jaegertracing/jaeger)
  - [Pinpoint](https://github.com/naver/pinpoint)
  - [Zipkin](https://github.com/openzipkin/zipkin)

- [《开源 APM 技术选型与实战》](http://www.infoq.com/cn/articles/apm-Pinpoint-practice) \* 主要基于 Google 的 Dapper（大规模分布式系统的跟踪系统） 思想。

## 统计分析

- [《流量统计的基础：埋点》](https://zhuanlan.zhihu.com/p/25195217) \* 常用指标：访问与访客、停留时长、跳出率、退出率、转化率、参与度

- [《APP 埋点常用的统计工具、埋点目标和埋点内容》](http://www.25xt.com/company/17066.html) \* 第三方统计：友盟、百度移动、魔方、App Annie、talking data、神策数据等。

- [《美团点评前端无痕埋点实践》](https://tech.meituan.com/mt_mobile_analytics_practice.html) \* 所谓无痕、即通过可视化工具配置采集节点，在前端自动解析配置并上报埋点数据，而非硬编码。

## 持续集成(CI/CD)

- [《持续集成是什么？》](http://www.ruanyifeng.com/blog/2015/09/continuous-integration.html)
- [《8 个流行的持续集成工具》](https://www.testwo.com/article/1170)

### Jenkins

- [《使用 Jenkins 进行持续集成》](https://www.liaoxuefeng.com/article/001463233913442cdb2d1bd1b1b42e3b0b29eb1ba736c5e000)

### 环境分离

开发、测试、生成环境分离。

- [《开发环境、生产环境、测试环境的基本理解和区》](https://my.oschina.net/sancuo/blog/214904)

## 自动化运维

### Ansible

- [《Ansible 中文权威指南》](http://www.ansible.com.cn/)
- [《Ansible 基础配置和企业级项目实用案例》](https://www.cnblogs.com/heiye123/articles/7855890.html)

### puppet

- [《自动化运维工具——puppet 详解》](https://www.cnblogs.com/keerya/p/8040071.html)

### chef

- [《Chef 的安装与使用》](https://www.ibm.com/developerworks/cn/cloud/library/1407_caomd_chef/)

## 测试

### TDD 理论

- [《深度解读 - TDD（测试驱动开发）》](https://www.jianshu.com/p/62f16cd4fef3)
  _ 基于测试用例编码功能代码，XP（Extreme Programming）的核心实践.
  _ 好处：一次关注一个点，降低思维负担；迎接需求变化或改善代码的设计；提前澄清需求；快速反馈；

### 单元测试

- [《Java 单元测试之 JUnit 篇》](https://www.cnblogs.com/happyzm/p/6482886.html)
- [《JUnit 4 与 TestNG 对比》](https://blog.csdn.net/hotdust/article/details/53406086) \* TestNG 覆盖 JUnit 功能，适用于更复杂的场景。
- [《单元测试主要的测试功能点》](https://blog.csdn.net/wqetfg/article/details/50900512) \* 模块接口测试、局部数据结构测试、路径测试 、错误处理测试、边界条件测试 。

### 压力测试

- [《Apache ab 测试使用指南》](https://blog.csdn.net/blueheart20/article/details/52170790)
- [《大型网站压力测试及优化方案》](https://www.cnblogs.com/binyue/p/6141088.html)
- [《10 大主流压力/负载/性能测试工具推荐》](http://news.chinabyte.com/466/14126966.shtml)
- [《真实流量压测工具 tcpcopy 应用浅析》](http://quentinxxz.iteye.com/blog/2249799)
- [《nGrinder 简易使用教程》](https://www.cnblogs.com/jwentest/p/7136727.html)

### 全链路压测

- [《京东 618：升级全链路压测方案，打造军演机器人 ForceBot》](http://www.infoq.com/cn/articles/jd-618-upgrade-full-link-voltage-test-program-forcebot)
- [《饿了么全链路压测的探索与实践》](https://zhuanlan.zhihu.com/p/30306892)
- [《四大语言，八大框架｜滴滴全链路压测解决之道》](https://zhuanlan.zhihu.com/p/28355759)
- [《全链路压测经验》](https://www.jianshu.com/p/27060fd61f72)

### A/B 、灰度、蓝绿测试

- [《技术干货 | AB 测试和灰度发布探索及实践》](https://testerhome.com/topics/11165)
- [《nginx 根据 IP 进行灰度发布》](http://blog.51cto.com/purplegrape/1403123)

- [《蓝绿部署、A/B 测试以及灰度发布》](https://www.v2ex.com/t/344341)

## 虚拟化

- [《VPS 的三种虚拟技术 OpenVZ、Xen、KVM 优缺点比较》](https://blog.csdn.net/enweitech/article/details/52910082)

### KVM

- [《KVM 详解，太详细太深入了，经典》](http://blog.chinaunix.net/uid-20201831-id-5775661.html)
- [《【图文】KVM 虚拟机安装详解》](https://www.coderxing.com/kvm-install.html)

### Xen

- [《Xen 虚拟化基本原理详解》](https://www.cnblogs.com/sddai/p/5931201.html)

### OpenVZ

- [《开源 Linux 容器 OpenVZ 快速上手指南》](https://blog.csdn.net/longerzone/article/details/44829255)

## 容器技术

### Docker

- [《几张图帮你理解 docker 基本原理及快速入门》](https://www.cnblogs.com/SzeCheng/p/6822905.html)
- [《Docker 核心技术与实现原理》](https://draveness.me/docker)
- [《Docker 教程》](http://www.runoob.com/docker/docker-tutorial.html)

## 云技术

### OpenStack

- [《OpenStack 构架知识梳理》](https://www.cnblogs.com/klb561/p/8660264.html)

## DevOps

- [《一分钟告诉你究竟 DevOps 是什么鬼？》](https://www.cnblogs.com/jetzhang/p/6068773.html)
- [《DevOps 详解》](http://www.infoq.com/cn/articles/detail-analysis-of-devops)

## 文档管理

- [Confluence-收费文档管理系统](http://www.confluence.cn/)
- GitLab?
- Wiki
