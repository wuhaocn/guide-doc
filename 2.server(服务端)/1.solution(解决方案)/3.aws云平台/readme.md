### aws概览

#### EC2：
    通用型（M1\M3\M4）
    计算优化型(C1-CC2\C3\C4)
    存储/IO优化型(HI1-HS1\I2\I2-D2)
    GPU已启用(CG1\CG2)
    内存优化型(M2-CR1\R3
    小型实例(T1\T2)

#### 存储：
    Amazon EBS：
        数据块存储：操作系统和网站文件
        标准卷：io一般，性能不高
        预配置iops：一致性，低延迟、io密集型
    
    Amazon S3: 
        对象存储：检索数据、图像、视频
    
    Amazon Glancier;
        归档永久性存储及备份
        
    Amazon Storage Gateway;     
        数据备份服务 与AWS云（S3 数据、 EC2镜像）
    
    AWS Snowball
        PB级数据传输方案：高速、扩展性高、防篡改安全，成本低、数据检索
    Amazon CloudFront（CDN）
        边缘缓存:静态内容

#### 数据库：
    Amazon RDS
           Aurora
        关系型数据库
    Amazon Dynamo DB
        Nosql数据库:ssd,无数据大小限制
    Amazon ElasticCache
        缓存数据库
    AWS Database Migration Service
    
    Amazon Redshit

#### 物联网：
    Amazon VPC：
        Amazon Virtual Private Cloud
        专用、隔离的aws云区间
        
    AWS Direct Connect
        数据中心私有链接
    Amazon Route 53 【dns】
        //是一种具有高度可用性和扩展性的DNS服务，能够方便管理域名和IP地址；
        //选择特定于您的优先级和配置的路由策略。


#### 管理工具：通过管理工具，实现如何帮助其监控或自动化您使用的AWS服务。
    Amazon CloudWatch（资源和应用程序监控，包括：CPU、内存、磁盘及网络等，图形及报表）；
    AWS CloudFormation（AWs资源创建模板）；
    AWS Trusted Adisor（可以提供实时指导，以帮助您根据AWS最佳实践预置资源，提供最佳时间建议，包括：成本优化、性能、安全性和容错能力）

#### 安全和身份管理：保护云资源的2种工具。

    AWS IAM（AWS Identity and Access Management）：
        用于安全控制用户访问AWS服务和资源。
    Amazon CloudWatch
        资源监控 
    Amazon CloudTrail
        用户活动变更追踪
    AWS Config
        追踪配置
    Multi-Factor Authentication（MFA、多重身份证验证）；
    
    AWS WAF（ AWS Web Application Firewall）：
        是一种Web应用程序防火墙，帮助保护Web应用程序免受可能会影响应用程序可用性、影响安全性或占用过多资源等常见Web滥用。

#### 分析：
    AmazonRdeshift：
        pb数据仓库
    Amazon ElasticMapReduce(EMR)
        托管式hadoop框架
        搭配EC2和S3
    Amazon Kinesis Data Firehose(Amazon Kinesis)：
        AWS流数据平台，提供强大的服务以便轻松加载和分析流数据。
        （子工具：Kinesis Streams 、Kinesis Firehose、Kinesis analytics）
    Amazon athena
        即时查询
    Amazon Cognito
        
    Amazon QuickSight:
    
#### 管理工具
    AWS cloud formation
    AWS auto scaling 
    AWS service catalog
    AWS systems manager
    AWS cloudtrail
    AWS config
    AWS 托管服务
    AWS trusted advisor
    
#### AWS Cloud   
#### 应用程序服务
    Amazon API Gateway：
    AWS SQS （AmazonSimple Queue Service）：是一个可扩展的消息队列系统，用于当消息在应用程序架构的不同组件之间传输时消息存储。
    
    企业应用程序
    Amazon WorkSpaces：提供虚拟桌面服务，包括：Windows 7、Windows 10或Amazon Linux2桌面体验，也可以使自己的Windows 7或Windows 10桌面并在Amazon WorkSpaces上运行它们。


#### 移动服务
    Amazon Api Gateway
        移动接入网关
    AWS Device Farm:
        移动设备，自动化测试
    Amazon Cognito
        移动认证安全
#### IOT
        
    AWS Greengrass:
        一组定义的Greengrass Core与其他设备进行互通， Greengrass组是移动接入设备【家庭、汽车】
    AWS FreeRTOS
        微控制操作系统
        
    AWS IoT-Click：
        


#### 人工智能
#### 机器学习
    AWS SageMaker：构建 训练 调整 部署
    AWS Comprehend： 输入 理解 输出
    AWS Lex lex机器人
    备注：
    Amazon Machine Learning：
        机器学习分为三层：人工智能（AI)、机器学习（ML）和深度学习（DL）。
        机器学习是A.I子集，使机器能够根据经验改进任务；
        深度学习是由自我训练算法组成的ML的子集。
        MWs提供了许多ML服务，如：Amazon Sage Maker、AWS DeepLens、Amazon Lex、Amazon Polly和Amazon Rekognition。
    
    
#### 数据迁移
    AWS DataBase Migration Service
        
    AWS Server Migration Service
        易于入门 控制 敏捷性 经济高效
    AWS Snowball
        AWS管理控制台
    AWS Migration Hub
        迁移灵活性、集中跟踪
    
        
