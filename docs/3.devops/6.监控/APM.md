原创

# 开源 APM 性能检测系统技术选型与架构实战

2018-09-28 17:58:30 [天府云创](https://me.csdn.net/English0523) 阅读数 3090

[](http://creativecommons.org/licenses/by-sa/4.0/) 版权声明：本文为博主原创文章，遵循[CC 4.0 BY-SA](http://creativecommons.org/licenses/by-sa/4.0/)版权协议，转载请附上原文出处链接和本声明。

本文链接：[https://blog.csdn.net/English0523/article/details/82887459](https://blog.csdn.net/English0523/article/details/82887459)

随着分布式应用、云计算的不断深入发展，业务系统的逻辑结构变得越来越复杂，目前很多应用都采用了分布式架构，即从一个大程序演变成一系列服务的形式，运行在不同的平台不同的机器上，这种架构的复杂性和灵活性为发现和定位性能问题、系统安全运维带来了更高的挑战。此时需要一种新的技术手段，用来关注应用哪些问题影响了企业服务的性能和可用性，关注如何识别这些问题以及如何解决这些问题。本文将介绍目前业界主流的 APM 技术工具，解决分布式架构带来的监控和运维上的挑战。

## []()一、APM 基础篇

### []()1、什么是 APM?

APM，全称：Application Performance Management ，目前市面的系统基本都是参考 Google 的 Dapper（大规模分布式系统的跟踪系统）来做的，翻译传送门[《google 的 Dapper 中文翻译》](http://bigbully.github.io/Dapper-translation/)

APM（ApplicationPerformance Management）是一种应用性能监控工具，通过汇聚业务系统各处理环节的实时数据，分析业务系统各事务处理的交易路径和处理时间，实现对应用的全链路性能监测。目前主流的 APM 工具，基本都是参考了 Google 的 Dapper（大规模分布式系统的跟踪系统）体系，通过跟踪业务请求的处理过程，完成对应用系统在前后端处理、服务端调用的性能消耗跟踪，提供可视化的界面来展示对跟踪数据的分析。

思考下：不遵守该理论的是伪 APM，耍流氓吗？

APM 的核心思想是什么？ 在应用服务各节点相互调用的时候，从中记录并传递一个应用级别的标记，这个标记可以用来关联各个服务节点之间的关系。比如两个应用服务节点之间使用 HTTP 作为传输协议的话，那么这些标记就会被加入到 HTTP 头中。可见如何传递这些标记是与应用服务节点之间使用的通讯协议有关的，常用的协议就相对容易加入这些内容，一些按需定制的可能就相对困难些，这一点也直接决定了实现分布式追踪系统的难度。

### []()2、为什么要用 APM?

有业务痛点，才要寻求解决方案，个人认为，APM 需要优先解决测试环境下两个场景问题，基于测试先行的原则考虑：

[![](https://res.infoq.com/articles/apm-Pinpoint-practice/zh/resources/2231-1513792218601.png)](https://s3.amazonaws.com/infoq.content.live.0/articles/apm-Pinpoint-practice/zh/resources/2231-1513792218601.png)
优先关注宏观数据，并不是说测试人员无须关注微观层面的问题，在测试角度来看，先解决性能测试环境的数据采样、收集问题，再去评估生产环境，而线上的链路监控需要研发跟运维去配合，【研发角度场景】相对于测试人员来说是弱关注了。

APM 工具与传统的性能监控工具的区别在于，不仅仅提供一些零散的资源监控点和指标，其主要关注在系统内部执行、系统间调用的性能瓶颈分析，这样更有利于定位到问题的具体原因。

### []()3、市面上有哪些 APM 工具？

近年来 APM 技术和市场得到了快速发展，随着移动互联网的爆发，APM 的产业扩张的比较明显，重心也从最初的 IT 和技术运维转到了商业核心业务，相关产品也如雨后春笋般大量涌现出来，商业产品有国外的 Dynatrace、Appdynamics、New Relic、卓豪，国内的 RichAPM、听云、OneAPM、阿里的业务实时监控服务 ARMS 和百度 MTC 等等，开源产品也得到了迅速发展。

随后笔者带大家一起了解几种业界主流的开源 APM 工具。。。

- Pinpoint
  Pinpoint is an open source APM (Application Performance Management) tool for large-scale distributed systems written in Java.
  [https://github.com/naver/pinpoint](https://github.com/naver/pinpoint)
- SkyWalking
  A distributed tracing system, and APM ( Application Performance Monitoring ) .
  [http://skywalking.org](http://skywalking.org/)
- Zipkin
  Zipkin is a distributed tracing system. It helps gather timing data needed to troubleshoot latency problems in microservice architectures. It manages both the collection and lookup of this data. Zipkin’s design is based on the Google Dapper paper.
  [http://zipkin.io/](http://zipkin.io/)
- CAT （大众点评）
  CAT 基于 Java 开发的实时应用监控平台，包括实时应用监控，业务监控。
  [https://github.com/dianping/cat](https://github.com/dianping/cat)

我所知道相对有名的 APM 系统主要有以下几个（翻译上面的英文原文）：

- **1、Pinpoint**

github 地址：[GitHub - naver/pinpoint: Pinpoint is an open source APM (Application Performance Management) tool for large-scale distributed systems written in Java.](https://link.zhihu.com/?target=https%3A//github.com/naver/pinpoint)
对 java 领域的性能分析有兴趣的朋友都应该看看这个开源项目，这个是一个韩国团队开源出来的，通过 JavaAgent 的机制来做字节码代码植入，实现加入 traceid 和抓取性能数据的目的。
NewRelic、Oneapm 之类的工具在 java 平台上的性能分析也是类似的机制。

- **2、SkyWalking**

github 地址：[wu-sheng/sky-walking](https://link.zhihu.com/?target=https%3A//github.com/wu-sheng/sky-walking)
这是国内一位叫吴晟的兄弟开源的，也是一个对 JAVA 分布式应用程序集群的业务运行情况进行追踪、告警和分析的系统，在 github 上也有 400 多颗星了。
功能相对 pinpoint 还是稍弱一些，插件还没那么丰富，不过也很难得了。

- **3、Zipkin**

官网：[OpenZipkin · A distributed tracing system](https://link.zhihu.com/?target=http%3A//zipkin.io/)
github 地址：[GitHub - openzipkin/zipkin: Zipkin is a distributed tracing system](https://link.zhihu.com/?target=https%3A//github.com/openzipkin/zipkin)
这个是 twitter 开源出来的，也是参考 Dapper 的体系来做的。
Zipkin 的 java 应用端是通过一个叫 Brave 的组件来实现对应用内部的性能分析数据采集。
Brave 的 github 地址：[https://github.com/openzipkin/brave](https://link.zhihu.com/?target=https%3A//github.com/openzipkin/brave)
这个组件通过实现一系列的 java 拦截器，来做到对 http/servlet 请求、数据库访问的调用过程跟踪。
然后通过在 spring 之类的配置文件里加入这些拦截器，完成对 java 应用的性能数据采集。

- **4、CAT**

github 地址：[GitHub - dianping/cat: Central Application Tracking](https://link.zhihu.com/?target=https%3A//github.com/dianping/cat)
这个是大众点评开源出来的，实现的功能也还是蛮丰富的，国内也有一些公司在用了。
不过他实现跟踪的手段，是要在代码里硬编码写一些“埋点”，也就是侵入式的。
这样做有利有弊，好处是可以在自己需要的地方加埋点，比较有针对性；坏处是必须改动现有系统，很多开发团队不愿意。

- **5、Xhprof/Xhgui**

这两个工具的组合，是针对 PHP 应用提供 APM 能力的工具，也是非侵入式的。
Xhprof github 地址：[GitHub - preinheimer/xhprof: XHGUI is a GUI for the XHProf PHP extension, using a database backend, and pretty graphs to make it easy to use and interpret.](https://link.zhihu.com/?target=https%3A//github.com/preinheimer/xhprof)
Xhgui github 地址：[GitHub - perftools/xhgui: A graphical interface for XHProf data built on MongoDB](https://link.zhihu.com/?target=https%3A//github.com/perftools/xhgui)
**前面三个工具里面，我推荐的顺序依次是 Pinpoint—》Zipkin—》CAT。**
原因很简单，就是这三个工具对于程序源代码和配置文件的侵入性，是依次递增的：
Pinpoint：基本不用修改源码和配置文件，只要在启动命令里指定 javaagent 参数即可，对于运维人员来讲最为方便；
Zipkin：需要对 Spring、web.xml 之类的配置文件做修改，相对麻烦一些；
CAT：因为需要修改源码设置埋点，因此基本不太可能由运维人员单独完成，而必须由开发人员的深度参与了，而很多开发人员是比较抗拒在代码中加入这些东西滴；
相对于传统的监控软件（Zabbix 之流）的区别，APM 跟关注在对于系统内部执行、系统间调用的性能瓶颈分析，这样更有利于定位到问题的具体原因，而不仅仅像传统监控软件一样只提供一些零散的监控点和指标，就算告警了也不知道问题是出在哪里。

### []()4、先说结论

目前比较贴合 Google Dapper 原理设计的，Pinpoint 优于 Zipkin。
Pinpoint 对代码的零侵入，运用 JavaAgent 字节码增强技术，添加启动参数即可。
并且符合【测试角度场景】性能测试调优监控的宏观；
当然，结论太早了，会有疑惑：

“ Spring Cloud Slueth 和 zipkin 之间的关系是什么？ “

需要看详细对比的，详见下图：

[![](https://res.infoq.com/articles/apm-Pinpoint-practice/zh/resources/1682-1513877501187.png)](https://s3.amazonaws.com/infoq.content.live.0/articles/apm-Pinpoint-practice/zh/resources/1682-1513877501187.png)

### []()5、再说对比

本质上 Spring Cloud Slueth 与 Pinpoint 没有可比性，真正对比的是 Zipkin，Spring Cloud Slueth 聚焦在链路追踪和分析，将信息发送到 Zipkin，利用 Zipkin 的存储来存储信息，当然，Zipkin 也可以使用 ELK 来记录日志和展示，再通过收集服务器性能的脚本把数据存储到 ELK，则可以展示服务器状况信息了。Zipkin 的总体展示，也是基于链路分析为主。

[![](https://res.infoq.com/articles/apm-Pinpoint-practice/zh/resources/1483-1513877500307.png)](https://s3.amazonaws.com/infoq.content.live.0/articles/apm-Pinpoint-practice/zh/resources/1483-1513877500307.png)

## []()二、Pinpoint 实战篇

### []()1、Pinpoint 架构图

Pinpoint is an open source APM (Application Performance Management) tool for large-scale distributed systems written in Java.
[![](https://res.infoq.com/articles/apm-Pinpoint-practice/zh/resources/1134-1513877500669.png)](https://s3.amazonaws.com/infoq.content.live.0/articles/apm-Pinpoint-practice/zh/resources/1134-1513877500669.png)

架构图对应说明：

- Pinpoint-Collector：收集各种性能数据
- Pinpoint-Agent：探针与应用服务器（例如 tomcat)关联，部署到同一台服务器上
- Pinpoint-Web：将收集到的数据层现在 web 展示
- HBase Storage：收集到数据存到 HBase 中

### []()2、Pinpoint 的数据结构

Pinpoint 消息的数据结构主要包含三种类型 Span，Trace 和 TraceId。

- Span 是最基本的调用追踪单元
  当远程调用到达的时候，Span 指代处理该调用的作业，并且携带追踪数据。为了实现代码级别的可见性，Span 下面还包含一层 SpanEvent 的数据结构。每个 Span 都包含一个 SpanId。
- Trace 是一组相互关联的 Span 集合
  同一个 Trace 下的 Span 共享一个 TransactionId，而且会按照 SpanId 和 ParentSpanId 排列成一棵有层级关系的树形结构。
- TraceId 是 TransactionId、SpanId 和 ParentSpanId 的组合
  TransactionId（TxId）是一个交易下的横跨整个分布式系统收发消息的 ID，其必须在整个服务器组中是全局唯一的。也就是说 TransactionId 识别了整个调用链；SpanId（SpanId）是处理远程调用作业的 ID，当一个调用到达一个节点的时候随即产生；ParentSpanId（pSpanId）顾名思义，就是产生当前 Span 的调用方 Span 的 ID。如果一个节点是交易的最初发起方，其 ParentSpanId 是 -1，以标志其是整个交易的根 Span。下图能够比较直观的说明这些 ID 结构之间的关系。

[![](https://res.infoq.com/articles/apm-Pinpoint-practice/zh/resources/1tra1-1513877500804.png)](https://s3.amazonaws.com/infoq.content.live.0/articles/apm-Pinpoint-practice/zh/resources/1tra1-1513877500804.png)

### []()3、Pinpoint 部署

网上太多部署文档，这里不详细说明，简要说明下：
[![](https://res.infoq.com/articles/apm-Pinpoint-practice/zh/resources/2t2-1513877500995.png)](https://s3.amazonaws.com/infoq.content.live.0/articles/apm-Pinpoint-practice/zh/resources/2t2-1513877500995.png)

注意版本要求：

- Java version required to run Pinpoint:
  [![](https://res.infoq.com/articles/apm-Pinpoint-practice/zh/resources/2t3-1513877501511.png)](https://s3.amazonaws.com/infoq.content.live.0/articles/apm-Pinpoint-practice/zh/resources/2t3-1513877501511.png)
- HBase compatibility table:
  [![](https://res.infoq.com/articles/apm-Pinpoint-practice/zh/resources/1t4-1513877501641.png)](https://s3.amazonaws.com/infoq.content.live.0/articles/apm-Pinpoint-practice/zh/resources/1t4-1513877501641.png)
- Agent compatibility table:
  [![](https://res.infoq.com/articles/apm-Pinpoint-practice/zh/resources/1t5-1513877501772.png)](https://s3.amazonaws.com/infoq.content.live.0/articles/apm-Pinpoint-practice/zh/resources/1t5-1513877501772.png)

有两种方式启动：

方式一：修改 tomat 目录下 bin/catalina.sh，在 Control Script for the CATALINA Server 加入以下三行代码：

```
1. CATALINA_OPTS="$CATALINA_OPTS -javaagent:/home/webapps/service/pp-agent/pinpoint-bootstrap-1.6.2.jar"    1. CATALINA_OPTS="$CATALINA_OPTS -Dpinpoint.agentId=pp32tomcattest"    1. CATALINA_OPTS="$CATALINA_OPTS -Dpinpoint.applicationName=32tomcat"
```

第一行：pinpoint-bootstrap-1.6.2.jar 的位置
第二行：agentId 必须唯一,标志一个 jvm
第三行：applicationName 表示同一种应用：同一个应用的不同实例应该使用不同的 agentId,相同的 applicationName

方式二：SpringBoot 启动

```
java -javaagent:/home/webapps/pp-agent/pinpoint-bootstrap-1.6.2.jar -Dpinpoint.agentId=pp32tomcattest -Dpinpoint.applicationName=32tomcat -jar 32tomcat-0.0.1-SNAPSHOT.jar
```

### []()4、代码注入是如何工作的

[![](https://res.infoq.com/articles/apm-Pinpoint-practice/zh/resources/687-1513877502168.png)](https://s3.amazonaws.com/infoq.content.live.0/articles/apm-Pinpoint-practice/zh/resources/687-1513877502168.png)
Pinpoint 对代码注入的封装非常类似 AOP，当一个类被加载的时候会通过 Interceptor 向指定的方法前后注入 before 和 after 逻辑，在这些逻辑中可以获取系统运行的状态，并通过 TraceContext 创建 Trace 消息，并发送给 Pinpoint 服务器。但与 AOP 不同的是，Pinpoint 在封装的时候考虑到了更多与目标代码的交互能力，因此用 Pinpoint 提供的 API 来编写代码会比 AOP 更加容易和专业。

### []()5、Pinpoint 实战效果演示

搭建一个 java 开源项目[jforum](http://jforum.net/)，跑在 tomcat 下，使用 jmeter 进行压测，用户 100 个：

- 服务器图（ServerMap）

通过可视化其组件的互连方式来了解任何分布式系统的拓扑。单击节点将显示有关组件的详细信息，例如其当前状态和事务计数。
[![](https://res.infoq.com/articles/apm-Pinpoint-practice/zh/resources/578-1513877501361.png)](https://s3.amazonaws.com/infoq.content.live.0/articles/apm-Pinpoint-practice/zh/resources/578-1513877501361.png)

- 实时活动线程图（Realtime Active Thread Chart）

实时监视应用程序内的活动线程。（用了官方图，当时没截图）
[![](https://res.infoq.com/articles/apm-Pinpoint-practice/zh/resources/1shish-1513877501983.png)](https://s3.amazonaws.com/infoq.content.live.0/articles/apm-Pinpoint-practice/zh/resources/1shish-1513877501983.png)

- 请求/响应散布图（Request/Response Scatter Chart）

可视化请求计数和响应模式，以确定潜在问题。可以通过在图表上拖动来选择事务以获取更多详细信息。
[![](https://res.infoq.com/articles/apm-Pinpoint-practice/zh/resources/1qps-1513877502428.png)](https://s3.amazonaws.com/infoq.content.live.0/articles/apm-Pinpoint-practice/zh/resources/1qps-1513877502428.png)

- 调用栈信息（CallStack）

增强分布式环境中每个事务的代码级可见性，识别单个视图中的瓶颈和故障点。
[![](https://res.infoq.com/articles/apm-Pinpoint-practice/zh/resources/479-1513878052510.png)](https://s3.amazonaws.com/infoq.content.live.0/articles/apm-Pinpoint-practice/zh/resources/479-1513878052510.png)

- 检查器（Inspector）

查看应用程序的其他详细信息，如 CPU 使用率，内存/垃圾收集，TPS 和 JVM 参数。
[![](https://res.infoq.com/articles/apm-Pinpoint-practice/zh/resources/3410-1513878051392.png)](https://s3.amazonaws.com/infoq.content.live.0/articles/apm-Pinpoint-practice/zh/resources/3410-1513878051392.png)

### []()6、总结

第一：PinPoint 从宏观上看：总体链路、服务总体状态（cpu、内存等等信息），符合【测试角度场景】性能测试调优监控的宏观；
第二：Spring Cloud Slueth 需要结合 Zipkin 从微观来看：自身无法单独提供展示，要结合 Zipkin 展示链路问题（并没有服务器总体状态的展示），更多服务器性能状况等信息展示需要定制脚本通过 ELK 收集展示，符合【研发角度场景】性能测试调优监控的微观；

总的来说两者是结合体，要单独使用的话，从测试业务上来看：PinPoint 满足，性能测试调优监控的宏观【测试角度场景】

### []()7、项目场景

访问某个 API，后端应用服务产生的一系列链路，为何请求一次有 23 次数据库访问呢？这里就是需要排查的的地方，详细看看 CallTree，找出可优化的 SQL 查询语句。
[![](https://res.infoq.com/articles/apm-Pinpoint-practice/zh/resources/1shizhan-1513878051674.png)](https://s3.amazonaws.com/infoq.content.live.0/articles/apm-Pinpoint-practice/zh/resources/1shizhan-1513878051674.png)
[![](https://res.infoq.com/articles/apm-Pinpoint-practice/zh/resources/1shizhan2-1513878051908.png)](https://s3.amazonaws.com/infoq.content.live.0/articles/apm-Pinpoint-practice/zh/resources/1shizhan2-1513878051908.png)
另外，在做性能测试的时候，服务器并发的 IO，PP 不断写入也会产生瓶颈，需要后续解决。

### []()8、标签库项目简单压测

通过 jmeter 对标签库进行简单压测，脚本如下：
[![](https://res.infoq.com/articles/apm-Pinpoint-practice/zh/resources/1jmeter-1513878052220.png)](https://s3.amazonaws.com/infoq.content.live.0/articles/apm-Pinpoint-practice/zh/resources/1jmeter-1513878052220.png)

通过 APM 发现问题如下：
[![](https://res.infoq.com/articles/apm-Pinpoint-practice/zh/resources/1b1-1513878052680.png)](https://s3.amazonaws.com/infoq.content.live.0/articles/apm-Pinpoint-practice/zh/resources/1b1-1513878052680.png)

pquery.do 的 res 高达 6782ms，需要安排开发进一步排查定位代码问题
[![](https://res.infoq.com/articles/apm-Pinpoint-practice/zh/resources/3611-1513878459739.png)](https://s3.amazonaws.com/infoq.content.live.0/articles/apm-Pinpoint-practice/zh/resources/3611-1513878459739.png)

[![](https://res.infoq.com/articles/apm-Pinpoint-practice/zh/resources/1b3-1513878459276.png)](https://s3.amazonaws.com/infoq.content.live.0/articles/apm-Pinpoint-practice/zh/resources/1b3-1513878459276.png)

另外一种场景，测试人员无法在页面获取到的信息（有些情况下，测试人员是没有服务器权限），这些是服务底层的异常信息，可以通过 CallTree 来查看。

### []()9、应用服务接入 APM 后的链路全景蜘蛛网图

[![](https://res.infoq.com/articles/apm-Pinpoint-practice/zh/resources/1812-1513878459509.jpg)](https://s3.amazonaws.com/infoq.content.live.0/articles/apm-Pinpoint-practice/zh/resources/1812-1513878459509.jpg)

## []()**总结**

介绍了 4 种主流的开源 APM 工具，最后我从代码侵入方式、数据落地方式、性能分析报表等三个方面对上述工具进行对比分析：

![](http://5b0988e595225.cdn.sohucs.com/images/20180327/02bbe5d6d2144481834761abc00a884f.jpeg)

通过对比可知，主流 APM 工具为了更好地进行推广，主要采用了侵入程度低的方式完成对应用代码的改造。并且为了应对云计算、微服务、容器化的迅速发展与应用带来的 APM 监控的数据的海量增长的趋势，数据落地方式也主要以海量存储数据库为主。 未来在数据分析和性能分析方面，大数据和机器学习将在 APM 领域发挥重要的作用，APM 的功能也将从单一的资源监控和应用监控，向异常检测、性能诊断、未来预测等自动化、智能化等方向发展，让我们共同期待开源社区或组织带来功能更强大的免费产品！

**【参考资料】**

1、回到网易后开源 APM 技术选型与实战 http://www.infoq.com/cn/articles/apm-Pinpoint-practice

2、常见开源 APM 监控工具介绍\_搜狐科技 https://www.sohu.com/a/226488076_505794

3、OneAPM Servers | 服务器监控软件 | 服务器监控系统 – OneAPM https://www.oneapm.com/others/servers.html

4、业务实时监控服务 ARMS\_秒级业务监控 https://www.aliyun.com/product/arms

5、开源 APM 工具 pinpoint 安装与使用 - CSDN 博客 https://blog.csdn.net/wh211212/article/details/80437696
文章最后发布于: 2018-09-28 17:58:30
