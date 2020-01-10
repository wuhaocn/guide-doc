### 一致性
    S3
       先写后读一致性：事件通知需求
       最终一致性
    DynamoDB
       最终一致性
       强一致性

### 网络
    总结：
        网络调用流程：
            internet网关--路由表--acl---子网---安全组
        公网IP：
            eip：固定【不工作就收费、一台ec2多一个eip就收费】
            pubicip：关机释放
        负载均衡器：
            alb：7层【http】
            nlb：4层【tcp-单节点百万】
            clb：上一代
        Route 53：
            dns解析
        IAM：
            用户、组、角色
            权限：
                拒绝
                允许
            附加权限：
                用户
                资源
        弹性：
            elb：负载
            cloudwatch：监听
            auto scaling：管理，扩展
            保存时间：13月

#### Amazon Virtual Private Cloud(VPC)
    部署在区域
    作用：
        开发、测试、生产环境隔离
    特性：
        aws账户虚拟网络【自定义ip】
        支持IPv4、IPv6
        一个1vpc只能在一个区域
    场景：
        单VPC：
            小团队、个人、身份管理、高性能计算
            复杂度简单
        多VPC：
            单团队、单组织，例托管服务商
            管理控制粒度中
            复杂度中
        多账户：
            大型组织
            管理控制粒度细
            复杂度高
            难度大
    限制：
        单账户每个区域最多创建5个vpc
        多个区域不能互通【vpc之间】
        
    CIDR：
        ip地址占用的范围，区块
        0.0.0.0/0 = 所有ip
        vpc:ip数量
            最小:/28数量16
            最大:/16数量65536
    vpc网络ip不足：
        现有子网无法扩展
        新增子网（新的ip端）
        
    vpc对等链接：
        特性：
            解决vpc与vpc之间的互联
            采用私有ip通信
            区域内、区域间都支持
            ip空间不能重叠
            两个vpc之间只能建立一个对等链接
            支持不同账户之间互通
            只能点对点，不支持传递
        
            无带宽瓶颈
            流量始终保留在aws主干网
            通过路由表进行互联
            高可用
    规划：
        vpc建设ip地址段要进行规划
        网络应易于扩展
        
    vpc共享：
        设置代理 
           
#### 子网
    关键特性
        子网是VPC CIDR块的子集
        子网CIDR块不能重叠
        每个子网完全位于一个可用区内
        一个可用区可以包含多个子网
        
        每个子网预留5个地址作为子网公用：前四个ip和最后一个ip
                1.网关
                2.dns
                3.广播
                ...
        eIp：
            固定ip
        publicIp：
            启动时动态分配，无法修改
        ec2：
            只免费一个eip，新增收费
            eip空余收费  
            
        公有子网：
            将路由表添加到Internet网关
            可支持针对公共internet的入站、出站访问
            适用场景：
                web应用
                nginx负载应用
        私有子网：
            不支持internet访问
            不允许将路由条目添加到internet网关
            nat实现公共internet访问
            
            适用场景：
                数据存储
                批处理
                后端
                web应用
        
     弹性网络接口：
        虚拟网络接口：
            在实例之间移动可以保留
            私有IP地址
            公有IP地址
            mac地址   
        弹性IP地址
            每个账户的每个AWS区域最多创建5个    
      
#### 网关
    互联网网关-挂载vpc
#### 安全性
    安全组：
        虚拟防火墙
            控制aws资源入站出站流量
        重定向
            
        规则有状态
        
        默认安全组：
            允许所有出站规则
    

    防火墙：
        默认所有出站入站规则
        作用：
            控制流量
    应用网络打通：        
        互联网网关
            挂在vpc
        路由表
            ip路由
        公网ip
            提供自身地址，具备访问地址
        网络acl（防火墙）
            白名单
            黑名单
        sg（安全组）
            只有白名单


#### VGW虚拟私有网关
    作用：
        供vpc与其他网络间建设vpn私有链接
#### AWS Direct Connect（DX）
    作用：
        提供专用私有网络链接
        降低数据传输成本
        安全性
        提高性能
    案例：
        混合云架构
        大型数据集
        预测网络性能
        安全性与合规性
#### Transit Gateway
    多vpc维护难
    解决vpc互联维护的问题
    配置路由
#### vpc endpoint 
    水平扩展
    同一区域
    无需离开aws内部网络
    不需要互联网遍历
#### interface endpoint
    api
    cloudwatch logs
    cloud build
    ...
#### gateway endpoint
    s3
    dynamodb


#### elb【负载均衡器】
    Elastic Load Balancing (ELB) ELB 
        Web 层的基础包括在架构中使用 ELB。
        这些负载均衡器不仅可以向 EC2 实例发送 流量,还可以将指标发送到Amazon CloudWatch(一种托管的监控服务)。
        来自 Amazon EC2 和 ELB 的指标可以充当触发器,因此,如果您发现延迟特别高或我 们的服务器使用过度,
        可以利用Auto Scaling 为您的Web 服务器队列添加更多容 量
    功能：
        使用http、https tcp ssl
        应用内部、外部
        每个负载均衡器都有一个dns
        发现并响应应用状况不佳的实例

    ELB 支持三种类型的负载均衡器:

    Application Load Balancer 
        在应用程序层中运行,即开放式系统互连 (OSI) 模型的 第七层。
        Application Load Balancer 支持基于内容的路由,并支持在容器中运行 的应用程序。
        它支持基于HTTP 或HTTPS 的本机Web 套接字,以及具有HTTPS 侦听器的HTTP/2。
        它还可以检查目标的运行状况,无论目标是 EC2 实例还是容 器。
        此外,在容器或 EC2 实例上运行的网站和移动应用程序也可以从 Application Load Balancer d的使用中受益。
    Network Load Balancer 
        设计用于每秒处理数千万个请求,同时以超低延迟保持 高吞吐量,无需您的干预。
        它会接受来自客户端的传入流量,然后在同一可用 区内的多个目标之间分配这些流量。
        Network Load Balancer 在连接级别(第 4 层)运行,根据 IP 协议数据将连接路由到目标,
        例如Amazon EC2 实例、容器和 IP 地址。
        Network Load Balancer 与Application Load Balancer API 兼容,包括对目 标组和目标的完全编程控制。
        Network Load Balancer 是对 TCP 流量进行负载均衡的理想选择。
        Network Load Balancer 经过优化,可以处理突发和不稳定的流量模式,
        同时为每个可用区使用 单个静态 IP 地址。
     Classic Load Balancer 
        在请求级别和 OSI 的连接级别上运行,可在多个可用区中 的 EC2 实例之间提供基本负载均衡。

####  Connection Draining 
    在负载均衡器上启用Connection Draining 时,取消注册的任何后端实例将在注 销之前完成正在进行的请求
    
    
    
#### 高可用
    Amazon Route 53 
        概念：
            提供域名系统 (DNS)、域名注册和运行状况检查Web 服务。
            该 服务旨在为开发人员和企业提供一种可靠且经济高效的方式
        特性:
            简单轮询
            加权
            基于延迟路由
            状态检测和dns故障转移
            地理位置路由
            临近度路由
            多值应答
            

### 账户
#### root用户
    对所有aws产品拥有完全访问权限
        账单信息
        个人信息
        整体架构及组件
    root用户拥有最大权利
    
    最好不要使用root用户
#### AWS Identity And Access Management
    与aws产品集成
    联合身份管理
    应用程序安全访问
    精细化的权限设定
#### IAM Principal
    IAM用户：
        aws账户下的用户，使用用户
        
        iam权限：
            拒绝高于允许
        授予权限：
            附加到aws资源
            附加到iam委托人
         基于用户身份策略：
            附加到用户
         Deny：
            优先级最高
         Allow：
            优先级较低 
    IAM用户组：
        以组的方式授权
        
    IAM解决
        联合用户：
            解决临时性访问用户
            
            定制化扩展身份认证：
                比如微信身份认证
        
        AWS Security Token Service (AWS STS) 是一项Web 服务,
            可以为IAM 用户或使用联合身份验证的 用户提供临时性有限权限凭证。
        凭证访问过期：
            最大12个小时
            
    Amazon Cognito 的两个主要组件是用户池和身份池。 
    • 用户池是为应用程序用户提供注册和登录选项的用户目录。 
    • 身份池让您能够授权您的用户访问其他AWS 产品。
    身份池和用户池可以单独 使用,也可以一起使用。
        
#### 高可用性

    有三个因素决定了应用程序的整体可用性:容错、可恢复性和可扩展性。
    容错通常与高可用性混淆,但容错指的是应用程序组件的内置冗余。它是否能 避免单点故障?该模块稍后将对容错进行介绍。
    可恢复性经常被忽视,人们往往认为它属于可用性的一部分。如果自然灾难导 致您的一个或多个组件不可用或破坏了主数据源,您是否可以快速恢复服务而 不会丢失数据?本模块稍后将讨论具体的灾难恢复策略。
    可扩展性用于衡量应用程序的基础设施响应容量需求增加的速度,以便您的应 用程序可用,并在您所需的标准内运行。它并不保证可用性,但它是应用程序 可用性的一部分。
    
    弹性：
        1.基于时间
        2.基于容量
    监控：
        运行状况
        资源利用率
        安全审计
        应用程序性能
      成本优化监控:可以生成报告,提供对服务使用情况和成本的深入见解。同时 提供可按时段、账户、资源或标签细分的预估成本
      成本管理器:可以查看过去最多 13 个月的数据,让您可以了解您在某段时间内 使用AWS 资源的模式。
      
      Amazon CloudWatch
        通过CloudWatch 全面地了解资源使用率、应用程序性能和运行状况。
      响应方式：
          指标
          日志
          警报
          事件
          规则
          目标
      
#### 实验1
    
    1.创建vpc
    2.创建子网
    3.创建互联网网关
    4.配置路由表
    5.为应用服务器创建一个安全组
    6.在公有子网中启动应用服务器
    额外：配置vpc对等链接

#### 实验2
    1.检查您的VPC
    2.创建application loadbalancer
    3.创建auto scaling
    4.更新安全组
    5.测试应用程序
    6.测试高可用
    7.数据库高可用
    8.高可用nat网关
            
            
    