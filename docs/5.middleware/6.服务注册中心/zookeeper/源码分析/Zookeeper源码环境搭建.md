# [【ZooKeeper 系列】3.ZooKeeper 源码环境搭建](https://www.cnblogs.com/heyonggang/p/12123991.html)

**前文阅读**：
[【ZooKeeper 系列】1.ZooKeeper 单机版、伪集群和集群环境搭建](https://www.cnblogs.com/heyonggang/p/12048060.html)
[【ZooKeeper 系列】2.用 Java 实现 ZooKeeper API 的调用](https://www.cnblogs.com/heyonggang/p/12058313.html)

在系列的前两篇文章中，介绍了 ZooKeeper 环境的搭建（包括单机版、伪集群和集群），对创建、删除、修改节点等场景用命令行的方式进行了测试，让大家对 ZooKeeper 环境搭建及常用命令行有初步的认识，也为搭建 ZooKeeper 的开发环境、生产环境起到了抛砖引玉的作用。也介绍了用 Java 来实现 API 的调用，包括节点的增、删、改、查。通过对这两篇的学习，让大家对 ZooKeeper 的使用有了初步认识，也可用于实现系列后面篇章要介绍的命名服务、集群管理、分布式锁、负载均衡、分布式队列等。

在前两篇中，强调了阅读英文文档的重要性，也带领大家解读了部分官方文档，想传达出的理念是 ZooKeeper 没有想象中的那么难，阅读官方文档也没那么难。后面的篇章中，结合官方文档，在实战演练和解读源码的基础上加深理解。
上联：说你行你就行不行也行
下联：说不行就不行行也不行
横批：不服不行

阅读源码就跟这个对联一模一样，就看你选上联，还是下联了！

这一篇开始源码环境的搭建，

here we go
！

很多老铁留言说很想研读些 github 上的开源项目，但代码 clone 下来后总出现这样或那样奇奇怪怪的问题，很影响学习的积极性。学习 ZooKeeper 的源码尤其如此，很多人 clone 代码后，报各种错，提示少各种包。问了下度娘 ZooKeeper 源码环境，搜出来的文章真的差强人意，有些文章错的竟然非常离谱。这里我重新搭建了一遍，也会介绍遇到的一些坑。

很多老铁上来一堆猛操作，从 github 上下载了 ZooKeeper 源码后，按常规方式导入 IDEA，最后发现少各种包。起初我也是这样弄的，以为 ZooKeeper 是用 Maven 来构建的，仔细去了解了下 ZooKeeper 的版本历史，其实是用的 Ant。如今一般用的 Maven 或 Gradle，很少见到 Ant 的项目了，这里不对 Ant 多做介绍。

## 1 Ant 环境搭建

Ant 官网地址：[https://ant.apache.org/bindownload.cgi](https://ant.apache.org/bindownload.cgi)

下载解压后，跟配置 jdk 一样配置几个环境变量：

```
//修改为自己本地安装的目录 ANT_HOMT=D:\apache-ant-1.10.7 PATH=%ANT_HOME%/bin CLASSPATH=%ANT_HOME%/lib
```

配置好后，测试下 Ant 是否安装成功。**ant -version**,得到如下信息则代表安装成功：

```
Apache Ant(TM) version 1.10.7 compiled on September 1 2019
```

Ant 的安装跟 JDK 的安装和配置非常相似，这里不做过多介绍。

## 2 下载 ZooKeeper 源码

源码地址：[https://github.com/apache/zookeeper](https://github.com/apache/zookeeper)

猿人谷在写本篇文章时，releases 列表里的最新版本为

release-3.5.6
，我们以此版本来进行源码环境的搭建。

## 3 编译 ZooKeeper 源码

切换到源码所在目录，运行

ant eclipse
将项目编译并转成 eclipse 的项目结构。
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191230152211788.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly95dWFucmVuZ3UuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70)
这个编译过程会比较长，差不多等了 7 分钟。如果编译成功，会出现如下结果：
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191230152345614.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly95dWFucmVuZ3UuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70)

## 4 导入 IDEA

上面已经将项目编译并转成 eclipse 的项目结构，按 eclipse 的形式导入项目。
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191230152519906.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly95dWFucmVuZ3UuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70)

![在这里插入图片描述](https://img-blog.csdnimg.cn/20191230152653707.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly95dWFucmVuZ3UuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70)

## 5 特别说明

将源码导入 IDEA 后在

org.apache.zookeeper.Version
中发现很多红色警告，很明显少了

org.apache.zookeeper.version.Info
类。
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191230155040951.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly95dWFucmVuZ3UuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70)
查询源码得知是用来发布的时候生成版本用的，我们只是研读源码，又不发布版本所以直接写死就 ok 了。
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191230155439579.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly95dWFucmVuZ3UuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70)
即新增 Info 类：

```
package org.apache.zookeeper.version; public interface Info{ int MAJOR = 3; int MINOR = 5; int MICRO = 6; String QUALIFIER = null; String REVISION_HASH = "c11b7e26bc554b8523dc929761dd28808913f091"; String BUILD_DATE = "10/08/2019 20:18 GMT"; }
```

## 6 启动 zookeeper

针对单机版本和集群版本，分别对应两个启动类：

- 单机：ZooKeeperServerMain
- 集群：QuorumPeerMain

这里我们只做单机版的测试。

**在 conf 目录里有个 zoo_sample.cfg，复制一份重命名为 zoo.cfg**。

zoo.cfg 里的内容做点修改（也可以不做修改），方便日志查询。dataDir 和 dataLogDir 根据自己的情况设定。

```
dataDir=E:\\02private\\1opensource\\zk\\zookeeper\\dataDir dataLogDir=E:\\02private\\1opensource\\zk\\zookeeper\\dataLogDir
```

运行主类

org.apache.zookeeper.server.ZooKeeperServerMain
，将 zoo.cfg 的完整路径配置在 Program arguments。
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191230153718683.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly95dWFucmVuZ3UuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70)
运行

ZooKeeperServerMain
，得到的结果如下：

```
Connected to the target VM, address: '127.0.0.1:0', transport: 'socket' log4j:WARN No appenders could be found for logger (org.apache.zookeeper.jmx.ManagedUtil). log4j:WARN Please initialize the log4j system properly. log4j:WARN See http://logging.apache.org/log4j/1.2/faq.html/#noconfig for more info.
```

告知日志无法输出，日志文件配置有误。这里需要指定日志文件 log4j.properties。
![在这里插入图片描述](https://img-blog.csdnimg.cn/2019123015440042.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly95dWFucmVuZ3UuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70)
在 VM options 配置，即指定到 conf 目录下的 log4j.properties：

```
-Dlog4j.configuration=file:E:/02private/1opensource/zk/zookeeper/conf/log4j.properties
```

配置后重新运行

ZooKeeperServerMain
，输出日志如下，
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191230154623937.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly95dWFucmVuZ3UuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70)
可以得知单机版启动成功，单机版服务端地址为 127.0.0.1:2181。

## 7 启动客户端

通过运行

ZooKeeperServerMain
得到的日志，可以得知 ZooKeeper 服务端已经启动，服务的地址为

127.0.0.1:2181
。启动客户端来进行连接测试。

客户端的启动类为

org.apache.zookeeper.ZooKeeperMain
，进行如下配置：
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191230175008781.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly95dWFucmVuZ3UuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70)
即客户端连接 127.0.0.1:2181，获取节点

/yuanrengu
的信息。

下面带领大家一起看看客户端启动的源码（

org.apache.zookeeper.ZooKeeperMain
）。**这里要给大家说下我阅读源码的习惯，很多老铁以为阅读源码就是顺着代码看，这样也没啥不对，只是很多开源项目代码量惊人，这么个干看法，容易注意力分散也容易看花眼。我一般是基于某个功能点，从入口开始 debug 跑一遍，弄清这个功能的“代码线”，就像跑马圈块地儿一样，弄清楚功能有关的代码，了解参数传递的过程，这样看代码时就更有针对性，也能排除很多干扰代码。**

### 7.1 main

main 里就两行代码，通过 debug 得知 args 里包含的信息就是上面我们配置在**Program arguments**里的信息：
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191231095122793.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly95dWFucmVuZ3UuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70)

### 7.1.1 ZooKeeperMain

```
public ZooKeeperMain(String args[]) throws IOException, InterruptedException{ // 用于解析参数里的命令行的 cl.parseOptions(args); System.out.println("Connecting to " + cl.getOption("server")); // 用于连接ZooKeeper服务端 connectToZK(cl.getOption("server")); }
```

通过下图可以看出，解析参数后，就尝试连接 127.0.0.1:2181，即 ZooKeeper 服务端。cl.getOption("server")得到的就是 127.0.0.1:2181。
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191231100610526.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly95dWFucmVuZ3UuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70)

### 7.1.2 parseOptions

![在这里插入图片描述](https://img-blog.csdnimg.cn/20191231114421581.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly95dWFucmVuZ3UuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70)
**可以很清楚的得知解析 args 的过程，主要从"-server"，"-timeout"，"-r"，"-"这几个维度来进行解析。**

### 7.1.3 connectToZK

```
protected void connectToZK(String newHost) throws InterruptedException, IOException{ // 用于判断现在ZooKeeper连接是否还有效 // zk.getState().isAlive() 注意这个会话是否有效的判断，客户端与 Zookeeper连接断开不一定会话失效 if (zk != null && zk.getState().isAlive()) { zk.close(); } // 此时 newHost 为 127.0.0.1:2181 host = newHost; // 判断是否为只读模式，关于只读模式的概念在前一篇文章中有介绍 boolean readOnly = cl.getOption("readonly") != null; // 用于判断是否建立安全连接 if (cl.getOption("secure") != null) { System.setProperty(ZKClientConfig.SECURE_CLIENT, "true"); System.out.println("Secure connection is enabled"); } zk = new ZooKeeperAdmin(host, Integer.parseInt(cl.getOption("timeout")), new MyWatcher(), readOnly); }
```

ZKClientConfig.SECURE_CLIENT
已经被标注为 deprecation 了：

```
//*/* /* Setting this to "true" will enable encrypted client-server communication. /*/ @SuppressWarnings("deprecation") public static final String SECURE_CLIENT = ZooKeeper.SECURE_CLIENT;
```

debug 查看关键点处的信息，可以得知这是建立一个 ZooKeeper 连接的过程（[【ZooKeeper 系列】2.用 Java 实现 ZooKeeper API 的调用](https://www.cnblogs.com/heyonggang/p/12058313.html),这篇文章里详细介绍过 ZooKeeper 建立连接的过程）

下图看看几处关键信息：
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191231101754442.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly95dWFucmVuZ3UuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70)
Integer.parseInt(cl.getOption("timeout"))为 30000。

至此完成了 ZooKeeperMain main = new ZooKeeperMain(args);的整个过程。简短点说就是：

1. 解析 Program arguments 里的参数
1. 连接 ZooKeeper 服务端

### 7.2 main.run()

**敲黑板，重头戏来了哦！**

一起来看下 run()的代码：

```
void run() throws CliException, IOException, InterruptedException{ // cl.getCommand()得到的是 “get”，就是上文传进来的 if (cl.getCommand() == null) { System.out.println("Welcome to ZooKeeper!"); boolean jlinemissing = false; // only use jline if it's in the classpath try { Class<?> consoleC = Class.forName("jline.console.ConsoleReader"); Class<?> completorC = Class.forName("org.apache.zookeeper.JLineZNodeCompleter"); System.out.println("JLine support is enabled"); Object console = consoleC.getConstructor().newInstance(); Object completor = completorC.getConstructor(ZooKeeper.class).newInstance(zk); Method addCompletor = consoleC.getMethod("addCompleter", Class.forName("jline.console.completer.Completer")); addCompletor.invoke(console, completor); String line; Method readLine = consoleC.getMethod("readLine", String.class); while ((line = (String)readLine.invoke(console, getPrompt())) != null) { executeLine(line); } } catch (ClassNotFoundException e) { LOG.debug("Unable to start jline", e); jlinemissing = true; } catch (NoSuchMethodException e) { LOG.debug("Unable to start jline", e); jlinemissing = true; } catch (InvocationTargetException e) { LOG.debug("Unable to start jline", e); jlinemissing = true; } catch (IllegalAccessException e) { LOG.debug("Unable to start jline", e); jlinemissing = true; } catch (InstantiationException e) { LOG.debug("Unable to start jline", e); jlinemissing = true; } if (jlinemissing) { System.out.println("JLine support is disabled"); BufferedReader br = new BufferedReader(new InputStreamReader(System.in)); String line; while ((line = br.readLine()) != null) { executeLine(line); } } } else { // 处理传进来的参数 processCmd(cl); } System.exit(exitCode); }
```

通过下图可以看出

processCmd(cl);
里

cl
包含的信息：
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191231103551902.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly95dWFucmVuZ3UuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70)
debug 到

processCmd(MyCommandOptions co)
就到了决战时刻。里面的

processZKCmd(MyCommandOptions co)
就是核心了，代码太长，只说下 processZKCmd 里的重点代码，获取节点/yuanrengu 的信息：
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191231105710706.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly95dWFucmVuZ3UuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70)
因为我之前没有创建过/yuanrengu 节点，会抛异常

org.apache.zookeeper.KeeperException$NoNodeException: KeeperErrorCode = NoNode for /yuanrengu
， 如下图所示：
![在这里插入图片描述](https://img-blog.csdnimg.cn/2019123111251754.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly95dWFucmVuZ3UuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70)
经过上面的步骤后 exitCode 为 1，执行 System.exit(exitCode);退出。

至此带领大家 dubug 了一遍 org.apache.zookeeper.ZooKeeperMain，上面我说过，阅读源码干看效果很小，只有 debug 才能有助于梳理流程和思路，也能清楚参数传递的过程发生了什么变化。

## 温馨提示

上面我们介绍了源码环境的搭建过程，运行运行主类

org.apache.zookeeper.server.ZooKeeperServerMain
启动 ZooKeeper 服务端，运行

org.apache.zookeeper.ZooKeeperMain
连接服务端。

**阅读源码最好能动起来(debug)读，这样代码才是活的，干看的话代码如死水一样，容易让人索然无味！**

每个人操作的方式不一样，有可能遇到的问题也不一样，搭建过程中遇到什么问题，大家可以在评论区留言。

![](https://images.cnblogs.com/cnblogs_com/heyonggang/957279/o_qrcode_for_gh_96c5e991bd62_430.jpg)
微信公众号： [猿人谷](https://images.cnblogs.com/cnblogs_com/heyonggang/957279/o_qrcode_for_gh_96c5e991bd62_430.jpg)
如果您认为阅读这篇博客让您有些收获，不妨点击一下右下角的【推荐】
如果您希望与我交流互动，欢迎关注微信公众号
本文版权归作者和博客园共有，欢迎转载，但未经作者同意必须保留此段声明，且在文章页面明显位置给出原文连接。
分类: [ZooKeeper](https://www.cnblogs.com/heyonggang/category/1614282.html)

[好文要顶]() [关注我]() [收藏该文]() [![](https://common.cnblogs.com/images/icon_weibo_24.png)]("分享至新浪微博") [![](https://common.cnblogs.com/images/wechat.png)]("分享至微信")

[![](https://pic.cnblogs.com/face/478153/20181105135741.png)](https://home.cnblogs.com/u/heyonggang/)

[夏雪冬日](https://home.cnblogs.com/u/heyonggang/)
[关注 - 3](https://home.cnblogs.com/u/heyonggang/followees/)
[粉丝 - 805](https://home.cnblogs.com/u/heyonggang/followers/)

[+加关注]()
1

0
[«](https://www.cnblogs.com/heyonggang/p/12058313.html) 上一篇： [【ZooKeeper 系列】2.用 Java 实现 ZooKeeper API 的调用](https://www.cnblogs.com/heyonggang/p/12058313.html '发布于 2019-12-18 10:00')

posted on 2019-12-31 13:36 [夏雪冬日](https://www.cnblogs.com/heyonggang/) 阅读(253) 评论(4) [编辑](https://i.cnblogs.com/EditPosts.aspx?postid=12123991) [收藏]()
