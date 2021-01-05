### 自动化

#### 简介

    背景：
        1.易出错，不确定性
        2.成本高
        3.无版本控制
        4.缺少审计
        5.数据不一致
        6.无法扩展
    收益：

#### 基础设置自动化

    AWS Cloud Formation：
        1.模板：
            json/yml配置
        2.基础设施即代码（IaC）
            • 将其视为代码并使用版本控制系统进行管理。
            • 在 JSON 或 YAML 模板文件中定义完整的应用程序堆栈(应用程序需要的所有 资源)。
            • 定义模板的运行时参数(Amazon EC2 实例大小、Amazon EC2 密钥对等)。
        3.流程
            架构模板--->AWS Cloud Formation--->架构堆栈
    AWS System Manager:
        1.自动执行、日常管理
        2.run command/ maintenance window / patch manager /stat manager
    AWS OpsWorks
        一种配置管理服务
        适用于 Puppet Enterprise 的AWS OpsWorks 提供了一个托管的 Puppet Enterprise 服务器和一套自动化工具,
        让您可以针对编排、自动预置和可追踪性可视化实 现工作流程自动化。
    AWS Elastic Beanstalk
        目标是帮助开发人员在云中部署和维护可扩展的Web 应 用程序和服务,同时让他们无需担心底层基础设施。

#### 缓存

    解决问题：
        快速访问
        热数据
        提高访问速度
    场景：
        查询高昂的数据
        静态数据
        动态数据：如股票价格
    cloud front：
        场景：
            静态:具有高存活时间 (TTL) 的图像、js、html 等
            视频:rtmp 和 http 流支持
            动态:自定义和不可缓存的内容
            用户输入:包括 Put/Post 的 http 操作支持
            安全:通过 SSL (https) 安全地提供内容

        过期：
            ttl：固定有效期
            更改对象名称：会出现脏数据
            强制清除：不得已手段
        通过route 53访问cloud front

        来源：
            ec2:收费，下行
            s3:s3不收费

        会话管理：
            用户在线状态
     Amazon ElastiCache
         是一种Web 服务,可用于在云中部署、操作和扩展内存缓存。
         ElastiCache 允许您从快速的托管型内存数据存储中检索信息,而不是依赖速度 较慢的基于磁盘型数据库,从而提高Web 应用程序的性能。
         只要有可能,应用 程序都将从 ElastiCache 中检索数据,如果缓存中找不到数据,再转向数据库。

#### 解耦

    elastic load balancing 【负载解耦】
        负载均衡器
    Amazon SQS【松散耦合，不能推送】
       特性
        异步处理
        标准队列：
            至少一次
            业务需要考虑幂等性
        FIFO：
            消息只处理一次
            每秒钟3000条
       配置：
         超时
       死信队列:
          未处理消息
    Amazon SNS 【具备推送能力】
        订阅：
            订阅类型
            http/https
            短信服务
            sqs
            lamaba
        特性
            单个发布消息
            没有重新调用选项
            http/https选项
            无法保证订单传递
    sns:
        持久性
        推送
        发布、订阅
        一对多
    sqs
        无持久性
        轮询
        发送、接受
        一对一

#### 微服务

    容器
        跨平台
        环境差异
    Amazon Ecs
        容器管理
    AWS Fargate
        完全托管的容器服务
    AWS lambda
     特性：
        完全托管的计算服务
        运行无状态代码
        最长时间15分钟
     计费模式：
        内存 * 时间 = GB*S
        1G * 10S
        2G * 3S
        根据应用程序确定
        运行时收费
     拓展：
        无服务模式

     功能：
        服务器
        容量需求
        部署
        扩展和容错能力
        操作系统活语言更新
        指标和日志
     运行方式：
        并行
        串行
        重试
        条件判断
    AWS Step Functions
        可视化工作流访问微服务
    Api-Gateway：
        高并发
        防DDos

#### RTO/RPO 和备份设置

    RTO
        恢复点目标
    RPO
        灰度时间目标

#### aws 最佳实践

    启用可扩展性
    环境自动化
    启用一次性资源
    组件实现松耦合
    设计服务而不是服务器
    数据库解决方案
    避免单点故障
    优化成本
    使用缓存
    每一层的保护基础设施：子网、安全组
