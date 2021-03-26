DevOps 工程师或 SRE 工程师，可能都知道 Prometheus 普罗米修斯。Prometheus 于 2012 年由 SoundCloud 创建，目前已经已发展为最热门的分布式监控系统。Prometheus 完全开源的，被很多云厂商（架构）内置，在这些厂商（架构）中，可以简单部署 Prometheus，用来监控整个云基础架构设施。比如 DigitalOcean 或 Docker 都是普罗米修斯作为基础监控。

希腊神话中，普罗米修斯是最具智慧的神明之一，是泰坦巨神后代，其名字意思为"先见之明"，那么以该名字命名的监控系统究竟怎么样呢？今天虫虫给大家讲讲这个以神之名命名的监控系统。
![](https://pics0.baidu.com/feed/cc11728b4710b912921c3a5f51ce1f079345226e.jpeg?token=72a2ff98e119cb77696696b5ec1024d0&s=80107D32939B41C802E160D30000C0B0)

普罗米修斯（Prometheus）介绍

Prometheus 是一个时间序列数据库。但是，它不仅仅是一个时间序列数据库。

它涵盖了可以绑定的整个生态系统工具集及其功能。

Prometheus 主要用于对基础设施的监控。包括服务器，数据库，VPS，几乎所有东西都可以通过 Prometheus 进行监控。Prometheus 希望通过对 Prometheus 配置中定义的某些端点执行的 HTTP 调用来检索度量标准。
![](https://pics6.baidu.com/feed/aec379310a55b31910153cccd19a6122cefc1757.jpeg?token=9e50d6fdb859363cbe6c27ca816f330d&s=5BAC3C62599B69C808CD80CB00006070)

例如，如果我们以 localhost:3000 的 Web 应用程序为例，你的应用程序将在特定 URL（例如 localhost:3000/metrics）中将指标公开为纯文本。以该 URL 为起点，，在给定的有效期间隔期间，Prometheus 将从该目标中提取数据。

工作原理？

如前所述，Prometheus 由各种不同的组件组成。其监控指标可以从系统中提取到，可以通过不同的方式做到：

通过应用程序给定监控项，对给定的公开 URL 上 Prometheus 兼容的指标。Prometheus 将其定义为目标并加入监控系统。

通过使用云厂商内置 Prometheus 程序，会定义好整个监控项和监控工具集拥。例如，可以 Linux 机器监控模版（节点导出器），数据库的模版（SQL 导出器或 MongoDB 导出器），以及 HTTP 代理或者负载程序的模版（例如 HAProxy 导出器）等这些模版直接就可以加入监控并使用。

通过使用 Pushgateway：应用程序或作业不会直接公开指标。某些应用程序要么没有合适的监控模版（例如批处理作业），对他们选择不能直接通过应用程序公开这些指标。如果我们忽略您可能使用 Pushgateway 的极少数情况，Prometheus 是一个基于主动请求 pull 的监控系统。
![](https://pics7.baidu.com/feed/42166d224f4a20a41d7d639102617b26720ed039.jpeg?token=c9ade9883f762b7ceeac77a051d9e32c&s=03B47C2281346C2346E1C17B0000E072)

推方式和拉方式

Prometheus 与其他时间序列数据库之间存在明显差异：Prometheus 主动筛选目标，以便从中检索指标。这与 InfluxDB 非常不同，InfluxDB 是需要直接推送数据给它。
![](https://pics5.baidu.com/feed/dcc451da81cb39dbb634375f4225ea20aa1830fe.jpeg?token=389003f2d2f466f60e988eeea4172a97&s=0594EF361B40584158D184CA00004033)

基于推和基于拉方式各有其优劣之处。Prometheus 使用主动拉方式主要的基于以下考虑：

实现集中控制：如果 Prometheus 向其目标发起查询，则整个配置在 Prometheus 服务器端完成，而不是在各个目标上完成。Prometheus 决定取值，以及取值的的频率。

使用基于推的系统，可能会导致向服务器发送过多数据的风险，这时会使其服务器崩溃。基于拉的系统能够实现速率控制，具有多级过期配置的灵活性，因此可以针对不同目标实现多种速率。

存储汇总的指标

Prometheus 不是基于事件的系统，这与其他时间序列数据库不同。Prometheus 并非旨在及时捕获单个和时间事件（例如服务中断），但它旨在收集有关的服务的预先汇总的指标。具体而言，它不会从 Web 服务发送 404 错误消息以及错误的消息的具体内容，而是对这些消息做处理、聚合过的指标。这与其他在收集"原始消息"的时间序列数据库之间的基本差异

生态系统

Prometheus 的主要功能仍然是时间序列数据库。但是，在使用时间序列数据库时，对它们实现了可视化、数据分析并通过自定义方式进行告警。

Prometheus 生态系统有功能丰富工具集：

Alertmanager：Prometheus 通过配置文件中定义的自定义规则将告警信息推送到 Alertmanager。Alertmanager 可以将其导出到多个端点，例如 Pagerduty 或 Slack 等。

数据可视化：与 Grafana、Kibana 等类似，可以直接在 Web UI 中可视化时间序列数据。轻松过滤查看了不通监控目标的信息。

服务发现：Prometheus 可以动态发现监控目标，并根据需要自动废弃目标。这在云架构中使用动态变更地址的容器时，尤为方便。
![](https://pics6.baidu.com/feed/b21c8701a18b87d6943f7372953bcb3c1f30fd13.jpeg?token=e12eb8f278ec7b4315b8cd06a6d552e4&s=B182FD138AD570CA146CC8D7030090E3)

普罗米修斯技术原理和构成

关键值数据模型

在开始使用 Prometheus 工具之前，了解数据模型非常重要。Prometheus 使用键值对。键描述了测量值时将实际测量值存储为数字的值。
![](https://pics5.baidu.com/feed/55e736d12f2eb9389dec47a647516631e4dd6f9c.jpeg?token=9477589ef575369c6c85f2262e3ea29d&s=3496743215CEC4EA10F5ADDF000040B3)

注意：Prometheus 并不会存储原始信息，如日志文本，它存储的是随时间汇总的指标。

一般来说键也就监控度量。比如 CPU 使用百分比或内存使用量等。但是，如果想要获得有关指标的更多详细信息，该怎么办？比如服务器 CPU 有四个核心，我们要想给它们分别设置指标怎么做？对此，Prometheus 有一个标签的概念。标签旨在通过向其添加其他字段来为指标提供更详细信息。你不需要简单地描述 CPU 速率，你可以指定位于某个 IP 的核心 CPU 的 CPU 速率。也能通过标签过滤指标并准确检索查找的内容。

度量类型

Prometheus 的监控指标有四种基本的类型来描述：

计数器 Counter

计数器可能是我们可以使用的最简单的度量标准形式。就想它字面意思一样，计数器是随着时间的增长的计算元素。

比如，要计算服务器上的 HTTP 错误数或网站上的访问次数，这时候就使用计数器。

计数器的值只能增加或重置为 0。计数器特别适合计算某个时段上某个事件的发生次数，即指标随时间演变的速率。

Gauges

Gauges 用于处理可能随时间减少的值。比如温度本华，内存变化等。Gauge 类型的值可以上升和下降，可以是正值或负值。

如果系统每 5 秒发送一次指标，Prometheus 每 15 秒抓取一次目标，那么这期间可能会丢失一些指标。如果对这些指分析计算，则结果的准确性会越来越低。

而使用计数器，每个值都会被汇总计算。

直方图 Histogram

直方图是一种更复杂的度量标准类型。它为我们的指标提供了额外信息，例如观察值的总和及其数量，常用于跟踪事件发生的规模。其值在具有可配置上限的存储对象中聚合。使用直方图可以用来：

计算平均值：因为它们表示值的总和除以记录的值的数量。

计算值的小数测量：这是一个非常强大的工具，可以让我们知道给定的集合中有多少值遵循给定的标准。在用于监控比例或建立质量指标时，这非常有用。

比如，为了监控性能指标，我们希望得到在有 20%的服务器请求响应时间超过 300 毫秒发送警告。对于涉及比例的指标就可以考虑使用直方图。

摘要 Summary

摘要是对直方图的扩展。除了提供观察的总和和计数之外，它们还提供滑动窗口上的分位数度量。分位数是将概率密度划分为相等概率范围的方法。

对比直方图：

直方图随时间汇总值，给出总和和计数函数，使得易于查看给定度量的变化趋势。

而摘要则给出了滑动窗口上的分位数（即随时间不断变化）。

这对于获得代表随时间记录的值的 95％的值尤其方便。

实例计算

随着分布式架构的不会发展完善和云解决方案的普及，现在的架构不再是孤零零几台配置很高的 IBM 小机就可以搞定一起的时代了。

分布式的服务器复制和分发成了日常架构的必备组件。我们举一个经典的 Web 架构，该架构由 2 个 HAProxy 代理服务器，在 3 个后端 Web 服务器。在该实例中，我们要监视 Web 服务器返回的 HTTP 错误的数量。

使用 Prometheus 语言，单个 Web 服务器单元称为实例。该任务是计算所有实例的 HTTP 错误数量。
![](https://pics3.baidu.com/feed/6a600c338744ebf884c469eb4bca342e6159a761.jpeg?token=2075595607eab48930208dc73d46f1dd&s=03946D22491F44C80AE5E07B0200D073)

PromQL

如果使用过基于 InfluxDB 的数据库，你可能会熟悉 InfluxQL。或者使用 TimescaleDB 过的 SQL 语句。Prometheus 也内置了自己的 SQL 查询语言，用于便捷和熟悉的方式从 Prometheus 查询和检索数据，这个内置的语言就是 PromQL。

我们前面说过，Prometheus 数据是用键值对表示的。PromQL 也用相同的语法查询和返回结果集。

使用 Prometheus 和 PromQL，会处理两种向量：

即时向量：表示在最近时间戳中跟踪的指标。

时间范围向量：用于查看度量随时间的演变，可以使用自定义时间范围查询 Prometheus。结果是一个向量聚合所选期间记录的值。
![](https://pics3.baidu.com/feed/9f510fb30f2442a774ce0e5943704e4fd31302c6.jpeg?token=367bd34a881354cf9a64e6cc2fe7e436&s=88A47D3213CA6D4B4E7581CA000070B2)

PromQL API 公开了一组方便查询数据操作的函数。用它可以实现排序，数学函计算（如导数或指数函数），统计预测计算（如 Holt Winters 函数）等。

Instrumentation 仪表化

仪表化是 Prometheus 的一个重要组成部分。在从应用程序检索数据之前，必须要仪表化它们。Prometheus 术语中的仪表化表示将客户端类库添加到应用程序，以便它们向 Prometheus 吐出指标。可以对大多数主流的编程语言（比如 Python，Java，Ruby，Go 甚至 Node 或 C/#应用程序）进行仪表化。

在仪表化操作时，需要创建内存对象（如仪表或计数器），可以在运行中增加或减少。然后选择指标公开的位置。Prometheus 将从该位置获取并存储到时间序列数据库。
![](https://pics3.baidu.com/feed/0823dd54564e92583d54f0520eb1325ccdbf4ee6.jpeg?token=a75e8fc62e466f6b926edee241fba40e&s=44C6DD1A17606D01157DBDD0000030B1)

Exporters 模版

对于自定义应用程序，仪表化非常方便，它允许自定义公开的指标以及其随时间的变化方式。

对于一些广泛使用的应用程序，服务器或数据库，Prometheus 提供专门的应用模版，可以使用它们来监控目标。

这些模版很多都用 Docker 镜像，可以轻松配置以监控目标。他们会预设常用的的指标和面板，可以几分钟内就完成监控配置。

常见的 Exporters 模版有：

数据库模版：用于 MongoDB 数据库，SQL 服务器和 MySQL 服务器的配置。

HTTP 模版：用于 HAProxy，Apache 或 NGINX 等 web 服务器和代理的配置。

Unix 模版：用来使用构建的节点导出程序监视系统性能，可以实现完整的系统指标的监控。
![](https://pics5.baidu.com/feed/80cb39dbb6fd5266bd11f0d1382b742fd50736f1.jpeg?token=5970d7489dc1769ad8acf7fccc9cd87a&s=4F84EC020B1E44CE5AC9E4CA02004073)

告警

在处理时间序列数据库时，我们希望对数据进行处理，并对结果给出反馈，而这部分工作有告警来实现。

告警在 Grafana 中非常常见，Prometheus 也通过 Alertmanager 实现完成的告警系统。Alertmanager 是一个独立的工具，可以绑定到 Prometheus 并运行自定义 Alertmanager。告警通过配置文件定义，定义有一组指标定义规则组成，如果数据命中这些规则，则会触发告警并将其发送到预定义的目标。与 Grafana 类似，Prometheus 的告警，可以通过 email，Slack webhooks，PagerDuty 和自定义 HTTP 目标等。
![](https://pics4.baidu.com/feed/c9fcc3cec3fdfc03d0c6c29b470c6490a4c2260a.jpeg?token=410ad5e5035bc8410f0a5731f56884f6&s=0492EC32270E56EA7AF1D4CA02000031)

普罗米修斯用例

我们最后再说下 Prometheus 的用例，可以使用该系统的各行各业。

DevOps

随着为系统，数据库和服务器构建，Prometheus 的主要目标显然是针对 DevOps 行业。在该领域有很多的供应商和众多的解决方案。Prometheus 无疑是一个理想的方案。在云基础架构下 Prometheus 的实例启动并运行的非常简便，还可以根据需要灵活的装配所需的部件。这它成为器和分布式体系结构的严重依赖容的工具栈的理想解决方案。在实例的创建速度与销毁速度一样快的容器的世界中，服务发现是每个 DevOps 栈必须要具备的。

医疗保健

时下监控解决方案不仅适用于 IT 专业人员。它们还用于支持大型行业，为医疗保健行业提供弹性和可扩展的架构。随着需求的增长越来越多，部署的 IT 架构必须满足这种需求。如果没有可靠的方法来监控整个基础架构，可能会面临服务大量中断的风险，而 Prometheus 可以为其保驾护航。opensource 网站上提供了很这样的实例。

金融服务业

InfoQ 会议中有了讨论了将 Prometheus 用于金融机构方案。Jamie Christian 和 Alan Strader 介绍了他们如何使用 Prometheus 监控 Northern Trust 的基础设施。

总结

本文我们讲了分布式监控系统 Prometheus 的介绍，原理和用例，希望我们能熟悉这个工具，并在以后的架构和实践中使用它解决系统和应用监控的问题。

## 作者最新文章

- ### [大型仓库 git clone 性能优化之部分克隆](https://mbd.baidu.com/newspage/data/landingsuper?context=%7B%22nid%22%3A%22news_9735602596942413063%22%7D&n_type=1&p_from=3)

10-2516:54

- ### [GitLab 新版本 12.4 发布，支持发布通知、部分克隆，Pages 访问控制](https://mbd.baidu.com/newspage/data/landingsuper?context=%7B%22nid%22%3A%22news_9039287066927810136%22%7D&n_type=1&p_from=3)

10-2316:26

- ### [开源和赚钱的抉择](https://mbd.baidu.com/newspage/data/landingsuper?context=%7B%22nid%22%3A%22news_9788232567851534800%22%7D&n_type=1&p_from=3)

10-2117:17

## 相关文章

- ### [数据分析师成长指南：IT 人士必看！](https://mbd.baidu.com/newspage/data/landingsuper?context=%7B%22nid%22%3A%22news_8796082031207606531%22%7D&n_type=1&p_from=4)

[![](https://ss0.baidu.com/6ONWsjip0QIZ8tyhnq/it/u=3086761275,3190831198&fm=173&app=49&f=JPEG?w=218&h=146&s=8810719342816CE81DC9C9F003009033)](https://mbd.baidu.com/newspage/data/landingsuper?context=%7B%22nid%22%3A%22news_8796082031207606531%22%7D&n_type=1&p_from=4)

- ### [Python 慢，为啥还有大公司用？](https://mbd.baidu.com/newspage/data/landingsuper?context=%7B%22nid%22%3A%22news_9916868028229925841%22%7D&n_type=1&p_from=4)

[![](https://ss2.baidu.com/6ONYsjip0QIZ8tyhnq/it/u=632538392,3452892419&fm=173&app=49&f=JPEG?w=218&h=146&s=9AA1E900533909989CD6DC0D030020C9)](https://mbd.baidu.com/newspage/data/landingsuper?context=%7B%22nid%22%3A%22news_9916868028229925841%22%7D&n_type=1&p_from=4)

- ### [Java 程序员如何步入大数据开发领域](https://mbd.baidu.com/newspage/data/landingsuper?context=%7B%22nid%22%3A%22news_9464277310679737982%22%7D&n_type=1&p_from=4)

[![](https://ss2.baidu.com/6ONYsjip0QIZ8tyhnq/it/u=452498687,698049090&fm=173&app=49&f=JPEG?w=218&h=146&s=B21530C41B40364116CB03830300808A)](https://mbd.baidu.com/newspage/data/landingsuper?context=%7B%22nid%22%3A%22news_9464277310679737982%22%7D&n_type=1&p_from=4)

- ### [机械专业本科毕业生是否可以转向软件开发领域](https://mbd.baidu.com/newspage/data/landingsuper?context=%7B%22nid%22%3A%22news_10343204018407621147%22%7D&n_type=1&p_from=4)

[![](https://ss0.baidu.com/6ONWsjip0QIZ8tyhnq/it/u=2319940587,1478030836&fm=173&app=49&f=JPEG?w=218&h=146&s=969239C70E848C1FB381A8F00300C032)](https://mbd.baidu.com/newspage/data/landingsuper?context=%7B%22nid%22%3A%22news_10343204018407621147%22%7D&n_type=1&p_from=4)

- ### [JSON 中的树状结构数据简介](https://mbd.baidu.com/newspage/data/landingsuper?context=%7B%22nid%22%3A%22news_9925419080433174575%22%7D&n_type=1&p_from=4)

[![](https://ss1.baidu.com/6ONXsjip0QIZ8tyhnq/it/u=1450034353,404384239&fm=173&app=49&f=JPEG?w=218&h=146&s=0C2674338B29640B5E58D5CE0000A0B0)](https://mbd.baidu.com/newspage/data/landingsuper?context=%7B%22nid%22%3A%22news_9925419080433174575%22%7D&n_type=1&p_from=4)
