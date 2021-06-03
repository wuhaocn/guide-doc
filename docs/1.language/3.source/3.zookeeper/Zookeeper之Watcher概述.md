# [一文理解 Zookeeper 的 Watcher 机制]()

- 时间：2019-08-22 00:19 作者：Java 花园 来源：[Java 花园]() 阅读：24
- 扫一扫，手机访问
  ![](https://www.songma.com/tem/getqr.php?u=https://www.songma.com/m/news/txtlist_i59421v.html&size=4)

**摘要：**Zookeeper 系列详情（持续升级）Zookeeper 基础初探一文理解 Zookeeper 数据节点 znode 一文理解 WatcherZookeeper 分布式锁实现 Zookeeper 部署模式??Zookeeper 提供了数据的发布/订阅功能，多个订阅者可同时监听某一特定主题对象，当该主题对象的自身状态发
Zookeeper 系列详情（**持续升级**）

- Zookeeper 基础初探
- 一文理解 Zookeeper 数据节点-znode
- 一文理解 Watcher
- Zookeeper 分布式锁实现
- Zookeeper 部署模式

??Zookeeper 提供了数据的发布/订阅功能，多个订阅者可同时监听某一特定主题对象，当该主题对象的自身状态发生变化时(例如节点内容改变、节点下的子节点列表改变等)，会实时、主动通知所有订阅者。

??Zookeeper 采用了 Watcher 机制实现数据的发布/订阅功能。该机制在被订阅对象发生变化时会异步通知用户端，因而用户端不必在 Watcher 注册后轮询阻塞，从而减轻了用户端压力。
Watcher 机制实际上**与观察者模式相似**，也可看作是一种观察者模式在分布式场景下的实现方式。

### watcher 架构

Watcher 实现由三个部分组成：

- Zookeeper 服务端；
- Zookeeper 用户端；
- 用户端的 ZKWatchManager 对象；

??用户端首先将 Watcher 注册到服务端，同时将 Watcher 对象保存到用户端的 Watch 管理器中。当 ZooKeeper 服务端监听的数据状态发生变化时，服务端会主动通知用户端，接着用户端的 Watch 管理器会触发相关 Watcher 来回调相应解决逻辑，从而完成整体的数据发布/订阅流程。
![](https://img.songma.com/wenzhang/20190628/gm3bfn2zskc134.png)Watcher 架构

### Watcher 特性

特性说明一次性 Watcher 是一次性的，一旦被触发就会移除，再次使用时需要重新注册用户端顺序回调 Watcher 回调是顺序串行化执行的，只有回调后用户端才能看到最新的数据状态。一个 Watcher 回调逻辑不应该太多，以免影响别的 watcher 执行轻量级 WatchEvent 是最小的通信单元，结构上只包含通知状态、事件类型和节点路径，并不会告诉数据节点变化前后的具体内容；时效性 Watcher 只有在当前 session 彻底失效时才会无效，若在 session 有效期内快速重连成功，则 watcher 仍然存在，仍可接收到通知；

### Watcher 接口设计

??Watcher 是一个接口，**任何实现了 Watcher 接口的类就是一个新的 Watcher**。Watcher 内部包含了两个枚举类：KeeperState、EventType。
![](https://img.songma.com/wenzhang/20190628/yceapkfl5dh135.png)watcher 类图

### Watcher 通知状态(KeeperState)

??KeeperState 是用户端与服务端连接状态发生变化时对应的通知类型。路径为 org.apache.zookeeper.Watcher.Event.KeeperState，是一个枚举类，其枚举属性如下；
枚举属性说明 Unknown(-1)属性过期 Disconnected(0)用户端与服务器断开连接时 NoSyncConnected(1)属性过期 SyncConnected(3)用户端与服务器正常连接时 AuthFailed(4)身份认证失败时 ConnectedReadOnly(5)3.3.0 版本后支持只读模式，一般情况下 ZK 集群中半数以上服务器正常，zk 集群才能正常对外提供服务。该属性的意义在于：若用户端设置了允许只读模式，则当 zk 集群中只有少于半数的服务器正常时，会返回这个状态给用户端，此时用户端只能解决读请求 SaslAuthenticated(6)服务器采用 SASL 做校验时 Expired(-112)会话 session 失效时

### Watcher 事件类型(EventType)

??EventType 是数据节点(znode)发生变化时对应的通知类型。**EventType 变化时 KeeperState 永远处于 SyncConnected 通知状态下；当 KeeperState 发生变化时，EventType 永远为 None**。其路径为 org.apache.zookeeper.Watcher.Event.EventType，是一个枚举类，枚举属性如下；
枚举属性说明 None (-1)无 NodeCreated (1)Watcher 监听的数据节点被创立时 NodeDeleted (2)Watcher 监听的数据节点被删除时 NodeDataChanged (3)Watcher 监听的数据节点内容发生变更时(无论内容数据能否变化)NodeChildrenChanged (4)Watcher 监听的数据节点的子节点列表发生变更时

**注**：用户端接收到的相关事件通知中只包含状态及类型等信息，不包括节点变化前后的具体内容，变化前的数据需业务自身存储，变化后的数据需调用 get 等方法重新获取；

### Watcher 注册及通知流程

- 用户端 Watcher 管理器：ZKWatchManager 数据结构
  1<

code

> //ZKWatchManager 维护了三个 map，key 代表数据节点的绝对路径，value 代表注册在当前节点上的 watcher 集合//代表节点上内容数据、状态信息变更相关监听 private final Map<String, Set<Watcher>> dataWatches = new HashMap<String, Set<Watcher>>();//代表节点变更相关监听 private final Map<String, Set<Watcher>> existWatches = new HashMap<String, Set<Watcher>>();//代表节点子列表变更相关监听 private final Map<String, Set<Watcher>> childWatches = new HashMap<String, Set<Watcher>>();</

code

>

- 服务端 Watcher 管理器：WatchManager 数据结构
  1<

code

> //WatchManager 维护了两个 map//说明：WatchManager 中的 Watcher 对象不是用户端客户定义的 Watcher，// 而是服务端中实现了 Watcher 接口的 ServerCnxn 笼统类，// 该笼统类代表了一个用户端与服务端的连接//key 代表数据节点路径，value 代表用户端连接的集合，该 map 作用为：//通过一个指定 znode 路径可找到其映射的所有用户端，当 znode 发生变更时//可快速通知所有注册了当前 Watcher 的用户端 private final HashMap<String, HashSet<Watcher>> watchTable = new HashMap<String, HashSet<Watcher>>();//key 代表一个用户端与服务端的连接，value 代表当前用户端监听的所有数据节点路径//该 map 作用为：当一个连接彻底断开时，可快速找到当前连接对应的所有//注册了监听的节点，以便移除当前用户端对节点的 Watcherprivate final HashMap<Watcher, HashSet<String>> watch

2

Paths = new HashMap<Watcher, HashSet<String>>();</

code

>

- Watcher 注册流程
  1<

code

> //Packet 对象构造函数//参数含义：请求头、响应头、请求体、响应体、Watcher 封装的注册体、能否允许只读 Packet(RequestHeader requestHeader, ReplyHeader replyHeader, Record request, Record response, WatchRegistration watchRegistration, boolean readOnly) { this.requestHeader = requestHeader; this.replyHeader = replyHeader; this.request = request; this.response = response; this.readOnly = readOnly; this.watchRegistration = watchRegistration; }</

code

> ![](https://img.songma.com/wenzhang/20190628/srg3mxfkwyd136.png)Watcher 注册流程.png1<

code

>

1

. 用户端发送的请求中只包含能否需要注册 Watcher，不会将 Watcher 实体发送；

2

. Packet 构造函数中的参数 WatchRegistration 是 Watcher 的封装体，用于服务响应成功后将 Watcher 保存到 ZKWatchManager 中；</

code

>

- Watcher 通知流程
  ![](https://img.songma.com/wenzhang/20190628/rp3lrmejbmc137.png)Watcher 通知流程
  []()

- **全部评论**(0)
-

\*

- [[展开所有评论]()]
-

上一篇：[Java 程序员进阶架构师必经之路]()
下一篇：[iOS 的异步解决神器——Promises]()

**最新发布的资讯信息**
【系统环境|】[淘码库，据消息称已被调查。淘码库源码网，已经无法访问！]()(2020-01-14 04:13)
【系统环境|服务器应用】[Discuz 隐藏后台 admin.php 网址修改路径]()(2019-12-16 16:48)
【系统环境|服务器应用】[2020 新网站如何让百度快速收录网站首页最新方法，亲测有用！免费]()(2019-12-16 16:46)
【系统环境|服务器应用】[Discuz 发布帖子时默认显示第一个主题分类的修改方法]()(2019-12-09 00:13)
【系统环境|软件环境】[Android | App 内存优化 之 内存泄漏 要点概述 以及 处理实战]()(2019-12-04 14:27)
【系统环境|软件环境】[MySQL InnoDB 事务]()(2019-12-04 14:26)
【系统环境|软件环境】[vue-router（单页面应用控制中心）常见用法]()(2019-12-04 14:26)
【系统环境|软件环境】[Linux 中的 Kill 命令]()(2019-12-04 14:26)
【系统环境|软件环境】[Linux 入门时必学 60 个文件解决命令]()(2019-12-04 14:26)
【系统环境|软件环境】[更新版 ThreeJS 3D 粒子波浪动画]()(2019-12-04 14:26)

[![云上智慧](https://www.songma.com/upload/365/shop.jpg)](https://www.songma.com/my/view365.html)

[云上智慧](https://www.songma.com/my/view365.html)

店铺

- [2018 漂亮的杰奇小说 2.3 源码（有教程...](https://www.songma.com/product/view41984.html)
- [易语言挂机界面秒余额源码 只提供学...](https://www.songma.com/product/view41469.html)
- [贷款超市借贷超市|小额贷款|thinkp...](https://www.songma.com/product/view40799.html)
- [微信，支付宝，QQ 钱包，二维码收款...](https://www.songma.com/product/view43453.html)
- [微信群二维码导航带整站数据+手机版...](https://www.songma.com/product/view42059.html)

- 商品推荐
- [![SEO在线伪原创工具源码 在线同义词替换工具源码 伪原创代写网站源码](https://www.songma.com/upload/1/1519837008-1/0398494001519837154tp1-2.jpg)]()[SEO 在线伪原创工具源码 在线同义词替换工具源码 伪原创代写网站源码]( "SEO 在线伪原创工具源码 在线同义词替换工具源码 伪原创代写网站源码")
  **￥ 100.00**
- [![微信小说分销系统源码,相似掌中云系统源码](https://www.songma.com/upload/367/1533965197-367/0555566001575212669tp367-2.jpg)]()[微信小说分销系统源码,相似掌中云系统源码]("微信小说分销系统源码,相似掌中云系统源码")
  **￥ 60.00**
- [![app store 红包 苹果应用商城红包 限量100个红包优惠券代金券](https://www.songma.com/upload/3/1537600051-3/0437482001537600595tp3-2.jpg)]()[app store 红包 苹果应用商城红包 限量 100 个红包优惠券代金券]( "app store 红包 苹果应用商城红包 限量 100 个红包优惠券代金券")
  **￥ 0.2**
- [![360快照logo站点logo网站排名小图标LOGO展示关键词显示出图](https://www.songma.com/upload/1/1528798727-1/0085899001528798744tp1-2.jpg)]()[360 快照 logo 站点 logo 网站排名小图标 LOGO 展示关键词显示出图]("360快照logo站点logo网站排名小图标LOGO展示关键词显示出图")
  **￥ 200.00**
- [![千图网图库网站解析源码 千库 昵图 摄图 图片下载vip解析源码 9网通用 淘宝卖家版](https://www.songma.com/upload/7/1532858685-7/0044542001533664953tp7-2.jpg)]()[千图网图库网站解析源码 千库 昵图 摄图 图片下载 vip 解析源码 9 网通...]( "千图网图库网站解析源码 千库 昵图 摄图 图片下载 vip 解析源码 9 网通用 淘宝卖家版")
  **￥ 1888.00**
- 资讯排行榜
- [更多>>]()

- 1
- [程序媛眼中的程序猿原来是这样子的！]("程序媛眼中的程序猿原来是这样子的！")
- 2
- [紧急整理了 20 道 Spring Boot 面试题，我经]( "紧急整理了 20 道 Spring Boot 面试题，我经常拿来面试别人！")
- 3
- [深入 JVM 内核 5 GC 参数]( "深入 JVM 内核 5 GC 参数")
- 4
- [HTML5(H5)常使用的十大前台框架（一）](<"HTML5(H5)常使用的十大前台框架（一）">)
- 5
- [Android 中 okhttp 原理详解-极度针对面试篇]("Android中okhttp原理详解-极度针对面试篇")
- 6
- [斗鱼主播“祖祖小姨妈”下海]("斗鱼主播“祖祖小姨妈”下海")
- 7
- [动漫聚选最新宅新闻 2018.09.26「周三」]( "动漫聚选最新宅新闻 2018.09.26「周三」")
- 8
- [网站建设技术革命 202：零技术为网站增加留言]("网站建设技术革命202：零技术为网站增加留言板栏目")
- 9
- [在线 VIP 视频免费观看, 嘘这个方法我只告诉你]( "在线 VIP 视频免费观看, 嘘这个方法我只告诉你!")
- 10
- [传奇单机架设，IIS 安装网站架设，登录器简单]("传奇单机架设，IIS安装网站架设，登录器简单配置教程图文教程")
