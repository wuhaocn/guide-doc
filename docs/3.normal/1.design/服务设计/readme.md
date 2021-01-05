# 服务演进

## 单体服务

## 集群

## 消息总线

## 微服务

## 中台

[阿里中台架构](阿里中台架构.md)

# 设计思想 & 开发模式

## DDD(Domain-driven Design - 领域驱动设计)

- [《浅谈我对 DDD 领域驱动设计的理解》](https://www.cnblogs.com/netfocus/p/5548025.html)

  - 概念：DDD 主要对传统软件开发流程(分析-设计-编码)中各阶段的割裂问题而提出，避免由于一开始分析不明或在软件开发过程中的信息流转不一致而造成软件无法交付（和需求方设想不一致）的问题。DDD 强调一切以领域（Domain）为中心，强调领域专家（Domain Expert）的作用，强调先定义好领域模型之后在进行开发，并且领域模型可以指导开发（所谓的驱动）。
  - 过程：理解领域、拆分领域、细化领域，模型的准确性取决于模型的理解深度。
  - 设计：DDD 中提出了建模工具，比如聚合、实体、值对象、工厂、仓储、领域服务、领域事件来帮助领域建模。

- [《领域驱动设计的基础知识总结》](https://www.cnblogs.com/butterfly100/p/7827870.html)

  - 领域（Doamin）本质上就是问题域，比如一个电商系统，一个论坛系统等。
  - 界限上下文（Bounded Context）：阐述子域之间的关系，可以简单理解成一个子系统或组件模块。
  - 领域模型（Domain Model）：DDD 的核心是建立（用通用描述语言、工具—领域通用语言）正确的领域模型；反应业务需求的本质，包括实体和过程；其贯穿软件分析、设计、开发 的整个过程；常用表达领域模型的方式：图、代码或文字；
  - 领域通用语言：领域专家、开发设计人员都能立即的语言或工具。
  - 经典分层架构：用户界面/展示层、应用层、领域层、基础设施层，是四层架构模式。
  - 使用的模式：
    - 关联尽量少，尽量单项，尽量降低整体复杂度。
    - 实体（Entity）：领域中的唯一标示，一个实体的属性尽量少，少则清晰。
    - 值对象（Value Object）：没有唯一标识，且属性值不可变，小二简单的对象，比如 Date。
    - 领域服务（Domain Service）： 协调多个领域对象，只有方法没有状态(不存数据)；可以分为应用层服务，领域层服务、基础层服务。
    - 聚合及聚合根（Aggregate，Aggregate Root）：聚合定义了一组具有内聚关系的相关对象的集合；聚合根是对聚合引用的唯一元素；当修改一个聚合时，必须在事务级别；大部分领域模型中，有 70%的聚合通常只有一个实体，30%只有 2~3 个实体；如果一个聚合只有一个实体，那么这个实体就是聚合根；如果有多个实体，那么我们可以思考聚合内哪个对象有独立存在的意义并且可以和外部直接进行交互；
    - 工厂（Factory）：类似于设计模式中的工厂模式。
    - 仓储（Repository）：持久化到 DB，管理对象，且只对聚合设计仓储。

- [《领域驱动设计(DDD)实现之路》](http://www.cnblogs.com/Leo_wl/p/3866629.html) \* 聚合：比如一辆汽车（Car）包含了引擎（Engine）、车轮（Wheel）和油箱（Tank）等组件，缺一不可。

- [《领域驱动设计系列（2）浅析 VO、DTO、DO、PO 的概念、区别和用处》](http://www.hollischuang.com/archives/553)

### 命令查询职责分离(CQRS)

CQRS — Command Query Responsibility Seperation

- [《领域驱动设计系列 (六)：CQRS》](https://www.cnblogs.com/cnblogsfans/p/4551990.html) \* 核心思想：读写分离（查询和更新在不同的方法中），不同的流程只是不同的设计方式，CQ 代码分离，分布式环境中会有明显体现（有冗余数据的情况下），目的是为了高性能。

- [《DDD CQRS 架构和传统架构的优缺点比较》](http://www.techweb.com.cn/network/system/2017-07-07/2553563.shtml) \* 最终一致的设计理念；依赖于高可用消息中间件。
- [《CQRS 架构简介》](http://www.cnblogs.com/netfocus/p/4055346.html) \* 一个实现 CQRS 的抽象案例。

- [《深度长文：我对 CQRS/EventSourcing 架构的思考》](http://www.uml.org.cn/zjjs/201609221.asp) \* CQRS 模式分析 + 12306 抢票案例

### 贫血，充血模型

- [《贫血，充血模型的解释以及一些经验》](https://kb.cnblogs.com/page/520743/)
  _ 失血模型：老子和儿子分别定义，相互不知道，二者实体定义中完全没有业务逻辑，通过外部 Service 进行关联。
  _ 贫血模型：老子知道儿子，儿子也知道老子；部分业务逻辑放到实体中；优点：各层单项依赖，结构清楚，易于维护；缺点：不符合 OO 思想，相比于充血模式，Service 层较为厚重；
  _ 充血模型：和贫血模型类似，区别在于如何划分业务逻辑。优点：Service 层比较薄，只充当 Facade 的角色，不和 DAO 打交道、复合 OO 思想；缺点：非单项依赖，DO 和 DAO 之间双向依赖、和 Service 层的逻辑划分容易造成混乱。
  _ 肿胀模式：是一种极端情况，取消 Service 层、全部业务逻辑放在 DO 中；优点：符合 OO 思想、简化了分层；缺点：暴露信息过多、很多非 DO 逻辑也会强行并入 DO。这种模式应该避免。 \* 作者主张使用贫血模式。

## Actor 模式

TODO

## 响应式编程

### Reactor

TODO

### RxJava

TODO

### Vert.x

TODO

## DODAF2.0

- [《DODAF2.0 方法论》](http://www.360doc.com/content/16/0627/19/33945750_571201779.shtml)
- [《DODAF2.0 之能力视角如何落地》](http://blog.51cto.com/xiaoyong/1553164)

## Serverless

无需过多关系服务器的服务架构理念。

- [《什么是 Serverless 无服务器架构？》](http://www.jdon.com/soa/serverless.html)
  _ Serverless 不代表出去服务器，而是去除对服务器运行状态的关心。
  _ Serverless 代表一思维方式的转变，从“构建一套服务在一台服务器上，对对个事件进行响应转变为构建一个为服务器，来响应一个事件”。 \* Serverless 不代表某个具体的框架。

- [《如何理解 Serverless？》](http://www.infoq.com/cn/news/2017/10/how-to-understand-serverless) \* 依赖于 Baas （(Mobile) Backend as a Service） 和 Faas （Functions as a service）

## Service Mesh

- [《什么是 Service Mesh？》](https://time.geekbang.org/article/2355)
- [《初识 Service Mesh》](https://www.jianshu.com/p/e23e3e74538e)
