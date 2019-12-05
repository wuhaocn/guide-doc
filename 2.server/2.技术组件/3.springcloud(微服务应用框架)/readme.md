#### 1.基础组件参考

##### 1.1 spring

[详细说明](0-language(编程语言)/java(server端)/6.framework(框架)/1.spring(应用框架))

##### 1.2 spring-boot


#### 2.Spring Cloud组件说明

##### 2.1.Spring Cloud Config
Spring
配置管理工具包，让你可以把配置放到远程服务器，集中化管理集群配置，目前支持本地存储、Git以及Subversion。


##### 2.2.Spring Cloud Bus
Spring
事件、消息总线，用于在集群（例如，配置变化事件）中传播状态变化，可与Spring Cloud Config联合实现热部署。

##### 2.3.Eureka
Netflix
云端服务发现，一个基于 REST 的服务，用于定位服务，以实现云端中间层服务发现和故障转移。

##### 2.4.Hystrix
Netflix
熔断器，容错管理工具，旨在通过熔断机制控制服务和第三方库的节点,从而对延迟和故障提供更强大的容错能力。


##### 2.5.Zuul
Netflix
Zuul 是在云平台上提供动态路由,监控,弹性,安全等边缘服务的框架。Zuul 相当于是设备和 Netflix 流应用的 Web 网站后端所有请求的前门。


##### 2.6.Archaius
Netflix
配置管理API，包含一系列配置管理API，提供动态类型化属性、线程安全配置操作、轮询框架、回调机制等功能。

##### 2.7.Consul
HashiCorp
封装了Consul操作，consul是一个服务发现与配置工具，与Docker容器可以无缝集成。


##### 2.8.Spring Cloud for Cloud Foundry
Pivotal
通过Oauth2协议绑定服务到CloudFoundry，CloudFoundry是VMware推出的开源PaaS云平台。


##### 2.9.Spring Cloud Sleuth
Spring
日志收集工具包，封装了Dapper和log-based追踪以及Zipkin和HTrace操作，为SpringCloud应用实现了一种分布式追踪解决方案。


##### 2.10.Spring Cloud Data Flow
Pivotal
大数据操作工具，作为Spring XD的替代产品，它是一个混合计算模型，结合了流数据与批量数据的处理方式。

##### 2.11.Spring Cloud Security
Spring
基于spring security的安全工具包，为你的应用程序添加安全控制。

##### 2.12.Spring Cloud Zookeeper
Spring
操作Zookeeper的工具包，用于使用zookeeper方式的服务发现和配置管理。

##### 2.13.Spring Cloud Stream
Spring
数据流操作开发包，封装了与Redis,Rabbit、Kafka等发送接收消息。

##### 2.14.Spring Cloud CLI
Spring
基于 Spring Boot CLI，可以让你以命令行方式快速建立云组件。

##### 2.15.Ribbon
Netflix
提供云端负载均衡，有多种负载均衡策略可供选择，可配合服务发现和断路器使用。

##### 2.16.Turbine
Netflix
Turbine是聚合服务器发送事件流数据的一个工具，用来监控集群下hystrix的metrics情况。

##### 2.17.Feign
OpenFeign
Feign是一种声明式、模板化的HTTP客户端。

##### 2.18.Spring Cloud Task
Spring
提供云端计划任务管理、任务调度。

##### 2.19.Spring Cloud Connectors
Spring
便于云端应用程序在各种PaaS平台连接到后端，如：数据库和消息代理服务。


##### 2.20.Spring Cloud Cluster
Spring
提供Leadership选举，如：Zookeeper, Redis, Hazelcast, Consul等常见状态模式的抽象和实现。


##### 2.21.Spring Cloud Starters
Pivotal
Spring Boot式的启动项目，为Spring Cloud提供开箱即用的依赖管理。

