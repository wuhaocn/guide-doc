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

#### Amazon ECS

    Amazon Elastic Container Service (Amazon ECS) 是一项高度可扩展的快速容器管理服务，它可轻松运行、停止和管理集群上的 Docker 容器。您可以通过使用 Fargate 启动类型启动服务或任务，将集群托管在由 Amazon ECS 管理的无服务器基础设施上。若要进行更多控制，您可以在使用 EC2 启动类型进行管理的 Amazon Elastic Compute Cloud (Amazon EC2) 实例集群上托管您的任务。有关启动类型的更多信息，请参阅 Amazon ECS 启动类型。

    利用 Amazon ECS，您可以通过简单的 API 调用来启动和停止基于容器的应用程序，可以从集中式服务获取集群状态，并且可以访问许多熟悉的 Amazon EC2 功能。

    您可以根据资源需求、隔离策略和可用性要求使用 Amazon ECS 计划容器在集群中的放置。借助 Amazon ECS，无需操作自己的集群管理和配置管理系统，也无需担心扩展管理基础设施。

    Amazon ECS 可用于创建一致的部署和构建体验、管理和扩展批处理和提取-转换-加载 (ETL) 工作负载以及在微服务模型上构建先进的应用程序架构。有关 Amazon ECS 使用案例和方案的更多信息，请参阅容器使用案例。

    AWS Elastic Beanstalk 还可用于快速开发、测试和部署 Docker 容器以及应用程序基础设施的其他组件；而直接使用 Amazon ECS 将提供对一系列更广泛的使用案例的更精细的控制和访问。有关更多信息，请参阅 AWS Elastic Beanstalk 开发人员指南。

#### AWS Elastic Beanstalk

    AWS Elastic Beanstalk 可让您迅速地在 AWS 云中部署和管理应用程序，而无需为运行这些应用程序的基础设施操心。AWS Elastic Beanstalk 可降低管理的复杂性，但不会影响选择或控制。您只需上传应用程序，AWS Elastic Beanstalk 将自动处理有关容量预配置、负载均衡、扩展和应用程序运行状况监控的部署细节。
    AWS Elastic Beanstalk 概念
    AWS Elastic Beanstalk 允许您管理将应用程序 作为环境 运行的所有资源。以下是一些关键的 Elastic Beanstalk 概念。

    应用程序
    Elastic Beanstalk 应用程序 是 Elastic Beanstalk 组件的逻辑集合，包括环境、版本 和环境配置。在 Elastic Beanstalk 中，应用程序在概念上类似文件夹。

    应用程序版本
    在 Elastic Beanstalk 中，应用程序版本 指的是 Web 应用程序的可部署代码的特定标记迭代。一个应用程序版本指向一个包含可部署代码（例如，Java WAR 文件）的 Amazon Simple Storage Service (Amazon S3) 对象。应用程序版本是应用程序的组成部分。应用程序可以有多个版本，每个应用程序版本都是唯一的。在运行环境中，您可以部署已上传到应用程序的任意应用程序版本，也可以上传并立即部署新的应用程序版本。您可以上传多个应用程序版本，以测试 Web 应用程序不同版本之间的差异。

    环境
    环境 是运行应用程序版本的 AWS 资源的集合。每个环境一次只运行一个应用程序版本，但您可以同时在多个环境中运行相同或不同的应用程序版本。当您创建环境时，Elastic Beanstalk 会预配置运行您指定的应用程序版本所需的资源。

    环境层
    在启动 Elastic Beanstalk 环境时，您需首先选择环境层。环境层指定环境运行的应用程序类型，并确定 Elastic Beanstalk 预置哪些资源来为其提供支持。为 HTTP 请求提供服务的应用程序在 Web 服务器环境层中运行。从 Amazon Simple Queue Service (Amazon SQS) 队列中拉取任务的环境在工作线程环境层中运行。

    环境配置
    环境配置 标识一组参数和配置，这些参数和配置用于定义环境及其相关资源的行为方式。当您更新环境的配置设置时，Elastic Beanstalk 会自动将更改应用到现有资源或者删除并部署新资源 (取决于更改的类型)。

    已保存的配置
    保存的配置 是一种模板，您可以将其用作创建独特环境配置的起点。您可以使用 Elastic Beanstalk 控制台、EB CLI、AWS CLI 或 API 创建和修改保存的配置并将其应用到环境。API 和 AWS CLI 将保存的配置称为配置模板。

    平台
    平台 是操作系统、编程语言运行时、Web 服务器、应用程序服务器和 Elastic Beanstalk 组件的组合。您设计 Web 应用程序并将其目标指向某个平台。Elastic Beanstalk 提供多种平台让您构建您的应用程序。

    有关详细信息，请参阅 AWS Elastic Beanstalk 平台。

#### AWS Lambda

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
Amazon Cognito
AWS Directory Service
AWS Key Management Service (AWS KMS)

机器学习
AWS Deep Learning AMI
Amazon Polly

管理与监管
AWS Auto Scaling【重点】
AWS CloudFormation
AWS CloudTrail
Amazon CloudWatch
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
