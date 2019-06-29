### 计算
#### Amazon EC2【重点】
    Amazon Elastic Compute Cloud (Amazon EC2) 是一种提供可调节计算容量的 Web 服务,
    简单来说，就是 Amazon 数据中心里的服务器 – 您可以使用它来构建和托管您的软件系统。
[详细介绍](http://docs.amazonaws.cn/AWSEC2/latest/UserGuide/ec2-instances-and-amis.html)
#### Amazon ECR
    Amazon Elastic Container Registry (Amazon ECR) 是一项托管 AWS Docker 镜像仓库服务，安全、可扩展且可靠。通过使用 AWS IAM，Amazon ECR 支持具有基于资源的权限的私有 Docker 存储库，以便特定用户或 Amazon EC2 实例可以访问存储库和镜像。开发人员可以使用 Docker CLI 推送、拉取和管理镜像。

    Amazon ECR 包含以下组件：
    
    镜像仓库
    我们为每个 AWS 账户均提供了一个 Amazon ECR 镜像仓库；您可以在镜像仓库中创建镜像存储库，并在其中存储镜像。有关更多信息，请参阅Amazon ECR 注册表。
    
    授权令牌
    Docker 客户端必须作为 AWS 用户向 Amazon ECR 注册表进行身份验证，然后才能推送和拉取镜像。AWS CLI get-login 命令可为您提供传递给 Docker 的身份验证凭证。有关更多信息，请参阅镜像仓库身份验证。
    
    存储库
    Amazon ECR 镜像存储库包含您的 Docker 镜像。有关更多信息，请参阅Amazon ECR 存储库。
    
    存储库策略
    您可以通过存储库策略来控制对存储库及其中的镜像的访问。有关更多信息，请参阅 Amazon ECR 存储库策略。
    
    镜像
    您可以对存储库推送和拉取 Docker 镜像。这些镜像可以在开发系统中本地使用，也可以在 Amazon ECS 任务定义中使用。有关更多信息，请参阅在 Amazon ECS 中使用 Amazon ECR 镜像。
    
    Amazon ECS
    AWS Elastic Beanstalk
    AWS Lambda
存储
    Amazon S3【重点】
    Amazon S3 Glacier
    AWS Snowball
    AWS Storage Gateway
    
Database
    Amazon Aurora【重点】
    Amazon DynamoDB
    Amazon ElastiCache
    Amazon RDS
    Amazon Redshift 【重点】
开发人员工具
    AWS CodeBuild
    AWS CodeDeploy
    
安全性、身份与合规性
    AWS Identity & Access Management (AWS IAM)
    Amazon  Cognito
    AWS Directory Service
    AWS Key Management Service (AWS KMS)

机器学习
    AWS Deep Learning AMI
    Amazon  Polly

管理与监管
    AWS Auto Scaling【重点】
    AWS CloudFormation
    AWS CloudTrail
    Amazon  CloudWatch
    AWS 命令行界面 (AWS CLI)
    AWS Config
    AWS 管理控制台
    AWS Systems Manager
    AWS Tools for Powershell

迁移与传输
    AWS Database Migration Service
    AWS Server Migration Service
    AWS Snowball
    
网络和内容传输
	Amazon API Gateway
	Amazon CloudFront
	AWS Direct Connect
	AWS Elastic Load Balancing
	Amazon VPC

媒体服务
	AWS Elemental
	AWS MediaConvert
	
Analytics
	Amazon Elasticsearch Service
	Amazon EMR
	Amazon Kinesis
	Amazon Redshift
应用程序集成
	Amazon SNS
	Amazon SQS
	AWS Step Functions
	Amazon SWF

游戏开发
	Amazon GameLift

物联网 (IoT)
	AWS IoT Core

开发工具包和工具箱
	AWS SDK for Java
	AWS SDK for JavaScript
	AWS SDK for .NET
	AWS SDK for PHP
	AWS SDK for Python (Boto 3) 
	AWS SDK for Ruby
	AWS Toolkit for Eclipse
	AWS Toolkit for Visual Studio

一般引用
	ARN 和服务命名空间
	AWS 术语表
	AWS 区域和终端节点
	AWS 安全证书
	AWS 服务限制
	AWS 命令行工具
	AWS 签署 API 请求
	AWS 错误重试和指数退避