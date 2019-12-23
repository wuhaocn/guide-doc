### 日志组件常见jar
    由于java日志框架众多（common-logging，log4j，slf4j，logback等），引入jar包的时候，就要为其添加对应的日志实现.

    不同的jar包，可能用了不同的日志框架，那引用了之后就得给不同的日志框架添加配置，这个是比较麻烦的。

### slf4j分析
    slf4j就是为了解决这个麻烦事的。

    slf4j全称为Simple Logging Facade for JAVA，java简单日志门面。类似于Apache Common-Logging，是对不同日志框架提供的一个门面封装，
    可以在部署的时候不修改任何配置即可接入一种日志实现方案。但是，他在编译时静态绑定真正的Log库。使用SLF4J时，如果你需要使用某一种日志实现
    ，那么你必须选择正确的SLF4J的jar包的集合（各种桥接包）。

    结构图如下：
![](https://upload-images.jianshu.io/upload_images/7462071-5bf21d00b9ad530c.png?imageMogr2/auto-orient/strip|imageView2/2/w/1152)


    slf4j的核心包：slf4j-1.7.25.zip
    需要配合日志实现，才能将日志绑定另一种输出。我们先看看slf4j里的jar包的作用.
    
    jcl-over-slf4j.jar  -->    (jcl    -> slf4j)    将Jakarta Commons Logging日志框架到 slf4j 的桥接  
    jul-to-slf4j.jar    -->    (juc    -> slf4j)    将java.util.logging的日志桥接到 slf4j  
    log4j-over-slf4j.jar-->    (log4j  -> slf4j)    将log4j 的日志，桥接到slf4j  
    osgi-over-slf4j.jar -->    (osgi   -> slf4j)    将osgi环境下的日志，桥接到slf4j  
    slf4j-android.jar   -->    (android-> slf4j)    将android环境下的日志，桥接到slf4j  
    slf4j-api.jar       -->                         slf4j 的api接口jar包  
    slf4j-ext.jar       -->                         扩展功能  
    slf4j-jcl.jar       -->    (lf4j -> jcl )          slf4j 转接到 Jakarta Commons Logging日志输出框架  
    slf4j-jdk14.jar     -->    (slf4j -> jul )          slf4j 转接到 java.util.logging，所以这个包不能和jul-to-slf4j.jar同时用，否则会死循环！！  
    slf4j-log4j12.jar   -->    (slf4j -> log4j)         slf4j 转接到 log4j,所以这个包不能和log4j-over-slf4j.jar同时用，否则会死循环！！  
    slf4j-migrator.jar  -->                                       一个GUI工具，支持将项目代码中 JCL,log4j,java.util.logging的日志API转换为slf4j的写法  
    slf4j-nop.jar       -->    (slf4j -> null)          slf4j的空接口输出绑定，丢弃所有日志输出  
    slf4j-simple.jar    -->    (slf4j -> slf4j-simple ) slf4j的自带的简单日志输出接口  

    这样整理之后，slf4j的jar包用途就比较清晰了。
    目前个人遇到的项目里，一般还会有log4j和log4j2混用的问题。
    因为log4j项目已经停止更新了，官方建议用log4j2。
### log4j2分析
    然后，log4j2里也提供了对各类log的桥接支持，这里就只列举相关的几个jar包说明。
    
    log4j-1.2-api.jar   -->    (log4j  -> log4j2)               将log4j 的日志转接到log4j2日志框架  
    log4j-api.jar       -->                                     log4j2的api接口jar包  
    log4j-core.jar      -->    (          log4j2 -> log4j-core) log4j2的日志输出核心jar包  
    log4j-slf4j-impl.jar-->    (slf4j  -> log4j2)               slf4j 转接到 log4j2 的日志输出框架  (不能和 log4j-to-slf4j同时用)  
    log4j-to-slf4j.jar  -->    (          log4j2 -> slf4j)      将 log4j2的日志桥接到 slf4j  (不能和 log4j-slf4j-impl 同时用)  
    从这里就已经可以看到，日志框架之间的关系有点乱。
    因为log4j2和slf4j都能对接多种日志框架，所以这些包的依赖，作用，还有命名，都容易让人混淆。
    
    这里针对 log4j, log4j2, slf4j做一个简单总结。

### log4j

    这个最简单，单独使用jar包就一个，已经停止更新，当前最新版是 log4j-1.2.17.jar
    只有输出功能，没有转接功能。
    可以用
    
    //    PropertyConfigurator.configure("conf/log4j.properties");  
    //    DOMConfigurator.configure("conf/log4j.xml");  
    来指定配置文件位置。

### log4j2

    同时有日志输出和转接功能。
    单独使用时，jar包是 log4j-api-2.x.x.jar 和 log4j-core-2.x.x.jar
    如果配置文件不在src目录，或者项目build Path 的source目录下，可以用以下代码指定配置文件位置：
    
    String config = System.getProperty("user.dir");// 获取程序的当前路径
    ConfigurationSource source = new ConfigurationSource(
        new FileInputStream(config + File.separator + "conf" + File.separator + "log4j2.xml"));
    Configurator.initialize(null, source);
    
      // 以下代码是log4j已经加载配置的情况下，指定加载其他的配置
    LoggerContext logContext = (LoggerContext) LogManager.getContext(false);
    File conFile = new File("conf/logs/log4j2.xml");
    logContext.setConfigLocation(conFile.toURI());
    logContext.reconfigure();
    log4j -> log4j2 桥接
    
    去掉 log4j 1.x jar，添加log4j-1.2-api.jar，配合 log4j-api-2.x.x.jar 和 log4j-core-2.x.x.jar 即可，依赖如下
    
    log4j-1.2-api.jar
        log4j-api-2.x.x.jar
            log4j-core-2.x.x.jar
    log4j2 -> log4j 桥接

    不建议。
    本来log4j在2015年停止更新后，就建议转向log4j2，并提供了到log4j2的桥接接口。
    所以反过来log4j2到log4j是不建议这么做的，log4j2也没有提供直接支持。
    但理清了上面的jar包作用，就会发现，可以通过 log4j2 -> slf4j -> log4j 的方式来实现。
    
    需要的jar包，根据依赖关系分别是：
    
    log4j-api-2.x.x.jar
        log4j-to-slf4j.jar
            slf4j-api-x.x.x.jar
                slf4j-log4j12-x.x.x.jar
                    log4j-1.2.17.jar
### slf4j

    同时有日志输出和转接功能。
    核心jar包是 slf4j-api-x.x.x.jar
    因为一般slf4j 只作为桥接用，如果要搭配 slf4j 自带的简单日志输出，那么就加上 slf4j-simple.jar
    
    log4j -> slf4j
    
    将代码中的log4j日志桥接到 slf4j，需要如下jar包
    
    log4j-over-slf4j-x.x.x.jar
        slf4j-api-x.x.x.jar
    log4j2 -> slf4j
    
    将代码中的log4j2日志桥接到 slf4j，需要如下jar包
    
    log4j-api-2.x.x.jar
        log4j-to-slf4j-2.x.x.jar
            slf4j-api-x.x.x.jar
    slf4j -> log4j
    
    将slf4j日志，采用log4j实现进行输出，需要如下jar包
    
    slf4j-api-x.x.x.jar
        slf4j-log4j12.jar
            log4j-1.2.17.jar
    slf4j -> log4j2
    
    将slf4j日志，采用log4j2实现进行输出，需要如下jar包
    
    slf4j-api-x.x.x.jar
        log4j-slf4j-impl.jar
            log4j-api.jar
                log4j-core.jar
    slf4j的代理绑定和输出组合起来，就实现了从一种日志框架，转到另一种日志实现框架的效果。
    建议在这三种日志混用的情况下，采用如下方案
    
    log4j -> log4j2
    slf4j -> log4j2

参考：
https://www.jianshu.com/p/d7b0e981868d