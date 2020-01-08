# Netty服务端启动

# 1. 概述

对于所有 Netty 的新手玩家，我们**最先**使用的就是 Netty 的 Bootstrap 和
ServerBootstrap 组这两个“**启动器**”组件。
它们在transport模块的bootstrap包下实现，如下图所示：

![`bootstrap` 包](http://static2.iocoder.cn/images/Netty/2018_04_01/01.png)
`bootstrap` 包在图中，我们可以看到三个以 Bootstrap 结尾的类，类图如下：

![Bootstrap 类图](http://static2.iocoder.cn/images/Netty/2018_04_01/02.png)
Bootstrap 类图

* 为什么是这样的类关系呢？因为 ServerBootstrap 和 Bootstrap
  **大部分**的方法和职责都是相同的。

本文仅分享 ServerBootstrap 启动 Netty 服务端的过程。下一篇文章，我们再分享
Bootstrap 分享 Netty 客户端。

# 2. ServerBootstrap 示例

下面，我们先来看一个 ServerBootstrap 的使用示例，就是我们在
[《精尽 Netty 源码分析 —— 调试环境搭建》](http://svip.iocoder.cn/Netty/build-debugging-environment/#5-1-EchoServer)
搭建的 EchoServer 示例。代码如下：


```java
 1: public final class EchoServer {
 2: 
 3:     static final boolean SSL = System.getProperty("ssl") != null;
 4:     static final int PORT = Integer.parseInt(System.getProperty("port", "8007"));
 5: 
 6:     public static void main(String[] args) throws Exception {
 7:         // Configure SSL.
 8:         // 配置 SSL
 9:         final SslContext sslCtx;
10:         if (SSL) {
11:             SelfSignedCertificate ssc = new SelfSignedCertificate();
12:             sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
13:         } else {
14:             sslCtx = null;
15:         }
16: 
17:         // Configure the server.
18:         // 创建两个 EventLoopGroup 对象
19:         EventLoopGroup bossGroup = new NioEventLoopGroup(1); // 创建 boss 线程组 用于服务端接受客户端的连接
20:         EventLoopGroup workerGroup = new NioEventLoopGroup(); // 创建 worker 线程组 用于进行 SocketChannel 的数据读写
21:         // 创建 EchoServerHandler 对象
22:         final EchoServerHandler serverHandler = new EchoServerHandler();
23:         try {
24:             // 创建 ServerBootstrap 对象
25:             ServerBootstrap b = new ServerBootstrap();
26:             b.group(bossGroup, workerGroup) // 设置使用的 EventLoopGroup
27:              .channel(NioServerSocketChannel.class) // 设置要被实例化的为 NioServerSocketChannel 类
28:              .option(ChannelOption.SO_BACKLOG, 100) // 设置 NioServerSocketChannel 的可选项
29:              .handler(new LoggingHandler(LogLevel.INFO)) // 设置 NioServerSocketChannel 的处理器
30:              .childHandler(new ChannelInitializer<SocketChannel>() {
31:                  @Override
32:                  public void initChannel(SocketChannel ch) throws Exception { // 设置连入服务端的 Client 的 SocketChannel 的处理器
33:                      ChannelPipeline p = ch.pipeline();
34:                      if (sslCtx != null) {
35:                          p.addLast(sslCtx.newHandler(ch.alloc()));
36:                      }
37:                      //p.addLast(new LoggingHandler(LogLevel.INFO));
38:                      p.addLast(serverHandler);
39:                  }
40:              });
41: 
42:             // Start the server.
43:             // 绑定端口，并同步等待成功，即启动服务端
44:             ChannelFuture f = b.bind(PORT).sync();
45: 
46:             // Wait until the server socket is closed.
47:             // 监听服务端关闭，并阻塞等待
48:             f.channel().closeFuture().sync();
49:         } finally {
50:             // Shut down all event loops to terminate all threads.
51:             // 优雅关闭两个 EventLoopGroup 对象
52:             bossGroup.shutdownGracefully();
53:             workerGroup.shutdownGracefully();
54:         }
55:     }
56: }

```

* 第 7 至 15 行：配置 SSL ，暂时可以忽略。
* 第 17 至 20 行：创建两个 EventLoopGroup 对象。
  * **boss** 线程组：用于服务端接受客户端的**连接**。
  * **worker** 线程组：用于进行客户端的 SocketChannel 的**数据读写**。
  * 关于为什么是**两个** EventLoopGroup 对象，我们在后续的文章，进行分享。
* 第 22 行：创建 [io.netty.example.echo.EchoServerHandler](https://github.com/YunaiV/netty/blob/f7016330f1483021ef1c38e0923e1c8b7cef0d10/example/src/main/java/io/netty/example/echo/EchoServerHandler.java)
  对象。
* 第 24 行：创建 ServerBootstrap 对象，用于设置服务端的启动配置。

* 第 26 行：调用#group(EventLoopGroup parentGroup, EventLoopGroup childGroup)
方法，设置使用的 EventLoopGroup 。

* 第 27 行：调用#channel(Class<? extends C> channelClass) 方法，设置要被实例化的 Channel
为 NioServerSocketChannel 类。在下文中，我们会看到该 Channel 内嵌了java.nio.channels.ServerSocketChannel 对象。

* 第 28 行：调用#option(ChannelOption<T> option, T value) 方法，设置
NioServerSocketChannel 的可选项。在 [io.netty.channel.ChannelOption](https://github.com/YunaiV/netty/blob/f7016330f1483021ef1c38e0923e1c8b7cef0d10/transport/src/main/java/io/netty/channel/ChannelOption.java)
类中，枚举了相关的可选项。

* 第 29 行：调用#handler(ChannelHandler handler) 方法，设置 NioServerSocketChannel
的处理器。在本示例中，使用了io.netty.handler.logging.LoggingHandler类，用于打印服务端的每个事件。详细解析，见后续文章。

* 第 30 至 40 行：调用#childHandler(ChannelHandler handler) 方法，设置连入服务端的 Client 的SocketChannel 的处理器。
在本实例中，使用 ChannelInitializer来初始化连入服务端的 Client 的 SocketChannel 的处理器。

* 第 44 行：**先**调用#bind(int port) 方法，绑定端口，**后**调用ChannelFuture/#sync() 方法，阻塞等待成功。这个过程，就是“**启动服务端**”。

* 第 48 行：**先**调用# closeFuture() 方法，**监听**服务器关闭，**后**调用ChannelFuture/#sync() 方法，阻塞等待成功。😈
注意，此处不是关闭服务器，而是“**监听**”关闭。

* 第 49 至 54 行：执行到此处，说明服务端已经关闭，所以调用EventLoopGroup/#shutdownGracefully() 方法，分别关闭两个 EventLoopGroup
对象。

# 3. AbstractBootstrap

我们再一起来看看 AbstractBootstrap 的代码实现。因为 ServerBootstrap 和Bootstrap 都实现这个类，
本文仅分享 ServerBootstrap 启动 Netty 服务端的过程。下一篇文章，我们再分享Bootstrap 分享 Netty 客户端。

# 2. ServerBootstrap 示例

下面，我们先来看一个 ServerBootstrap 的使用示例，就是我们在
[《精尽 Netty 源码分析 —— 调试环境搭建》](http://svip.iocoder.cn/Netty/build-debugging-environment/#5-1-EchoServer)
搭建的 EchoServer 示例。代码如下：


```java
 1: public final class EchoServer {
 2: 
 3:     static final boolean SSL = System.getProperty("ssl") != null;
 4:     static final int PORT = Integer.parseInt(System.getProperty("port", "8007"));
 5: 
 6:     public static void main(String[] args) throws Exception {
 7:         // Configure SSL.
 8:         // 配置 SSL
 9:         final SslContext sslCtx;
10:         if (SSL) {
11:             SelfSignedCertificate ssc = new SelfSignedCertificate();
12:             sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
13:         } else {
14:             sslCtx = null;
15:         }
16: 
17:         // Configure the server.
18:         // 创建两个 EventLoopGroup 对象
19:         EventLoopGroup bossGroup = new NioEventLoopGroup(1); // 创建 boss 线程组 用于服务端接受客户端的连接
20:         EventLoopGroup workerGroup = new NioEventLoopGroup(); // 创建 worker 线程组 用于进行 SocketChannel 的数据读写
21:         // 创建 EchoServerHandler 对象
22:         final EchoServerHandler serverHandler = new EchoServerHandler();
23:         try {
24:             // 创建 ServerBootstrap 对象
25:             ServerBootstrap b = new ServerBootstrap();
26:             b.group(bossGroup, workerGroup) // 设置使用的 EventLoopGroup
27:              .channel(NioServerSocketChannel.class) // 设置要被实例化的为 NioServerSocketChannel 类
28:              .option(ChannelOption.SO_BACKLOG, 100) // 设置 NioServerSocketChannel 的可选项
29:              .handler(new LoggingHandler(LogLevel.INFO)) // 设置 NioServerSocketChannel 的处理器
30:              .childHandler(new ChannelInitializer<SocketChannel>() {
31:                  @Override
32:                  public void initChannel(SocketChannel ch) throws Exception { // 设置连入服务端的 Client 的 SocketChannel 的处理器
33:                      ChannelPipeline p = ch.pipeline();
34:                      if (sslCtx != null) {
35:                          p.addLast(sslCtx.newHandler(ch.alloc()));
36:                      }
37:                      //p.addLast(new LoggingHandler(LogLevel.INFO));
38:                      p.addLast(serverHandler);
39:                  }
40:              });
41: 
42:             // Start the server.
43:             // 绑定端口，并同步等待成功，即启动服务端
44:             ChannelFuture f = b.bind(PORT).sync();
45: 
46:             // Wait until the server socket is closed.
47:             // 监听服务端关闭，并阻塞等待
48:             f.channel().closeFuture().sync();
49:         } finally {
50:             // Shut down all event loops to terminate all threads.
51:             // 优雅关闭两个 EventLoopGroup 对象
52:             bossGroup.shutdownGracefully();
53:             workerGroup.shutdownGracefully();
54:         }
55:     }
56: }

```

* 第 7 至 15 行：配置 SSL ，暂时可以忽略。
* 第 17 至 20 行：创建两个 EventLoopGroup 对象。
  * **boss** 线程组：用于服务端接受客户端的**连接**。
  * **worker** 线程组：用于进行客户端的 SocketChannel 的**数据读写**。
  * 关于为什么是**两个** EventLoopGroup 对象，我们在后续的文章，进行分享。
* 第 22 行：创建 [io.netty.example.echo.EchoServerHandler](https://github.com/YunaiV/netty/blob/f7016330f1483021ef1c38e0923e1c8b7cef0d10/example/src/main/java/io/netty/example/echo/EchoServerHandler.java)
  对象。
* 第 24 行：创建 ServerBootstrap 对象，用于设置服务端的启动配置。

* 第 26 行：调用#group(EventLoopGroup parentGroup, EventLoopGroup childGroup)
方法，设置使用的 EventLoopGroup 。

* 第 27 行：调用#channel(Class<? extends C> channelClass) 方法，设置要被实例化的 Channel
为 NioServerSocketChannel 类。在下文中，我们会看到该 Channel 内嵌了java.nio.channels.ServerSocketChannel 对象。

* 第 28 行：调用#option(ChannelOption<T> option, T value) 方法，设置
NioServerSocketChannel 的可选项。在 [io.netty.channel.ChannelOption](https://github.com/YunaiV/netty/blob/f7016330f1483021ef1c38e0923e1c8b7cef0d10/transport/src/main/java/io/netty/channel/ChannelOption.java)
类中，枚举了相关的可选项。

* 第 29 行：调用#handler(ChannelHandler handler) 方法，设置 NioServerSocketChannel
的处理器。在本示例中，使用了io.netty.handler.logging.LoggingHandler类，用于打印服务端的每个事件。详细解析，见后续文章。

* 第 30 至 40 行：调用#childHandler(ChannelHandler handler) 方法，设置连入服务端的 Client 的SocketChannel 的处理器。
在本实例中，使用 ChannelInitializer来初始化连入服务端的 Client 的 SocketChannel 的处理器。

* 第 44 行：**先**调用#bind(int port) 方法，绑定端口，**后**调用ChannelFuture/#sync() 方法，阻塞等待成功。这个过程，就是“**启动服务端**”。

* 第 48 行：**先**调用# closeFuture() 方法，**监听**服务器关闭，**后**调用ChannelFuture/#sync() 方法，阻塞等待成功。😈
注意，此处不是关闭服务器，而是“**监听**”关闭。

* 第 49 至 54 行：执行到此处，说明服务端已经关闭，所以调用EventLoopGroup/#shutdownGracefully() 方法，分别关闭两个 EventLoopGroup
对象。

# 3. AbstractBootstrap

我们再一起来看看 AbstractBootstrap 的代码实现。因为 ServerBootstrap 和Bootstrap 都实现这个类，
所以和 ServerBootstrap 相关度高的方法，我们会放在[4. ServerBootstrap]() 中分享，  
而和 Bootstrap相关度高的方法，我们会放在下一篇 Bootstrap 的文章分享。
下面开始分享一系列的 AbstractBootstrap的配置方法，如果比较熟悉，可以直接跳到 [「3.13 bind」]() 开始看。

## 3.1 构造方法

```java
public abstract class AbstractBootstrap<B extends AbstractBootstrap<B, C>, C extends Channel> implements Cloneable {

    /**
     * EventLoopGroup 对象
     */
    volatile EventLoopGroup group;
    /**
     * Channel 工厂，用于创建 Channel 对象。
     */
    @SuppressWarnings("deprecation")
    private volatile ChannelFactory<? extends C> channelFactory;
    /**
     * 本地地址
     */
    private volatile SocketAddress localAddress;
    /**
     * 可选项集合
     */
    private final Map<ChannelOption<?>, Object> options = new LinkedHashMap<ChannelOption<?>, Object>();
    /**
     * 属性集合
     */
    private final Map<AttributeKey<?>, Object> attrs = new LinkedHashMap<AttributeKey<?>, Object>();
    /**
     * 处理器
     */
    private volatile ChannelHandler handler;

    AbstractBootstrap() {
        // Disallow extending from a different package.
    }

    AbstractBootstrap(AbstractBootstrap<B, C> bootstrap) {
        group = bootstrap.group;
        channelFactory = bootstrap.channelFactory;
        handler = bootstrap.handler;
        localAddress = bootstrap.localAddress;
        synchronized (bootstrap.options) { // <1>
            options.putAll(bootstrap.options);
        }
        synchronized (bootstrap.attrs) { // <2>
            attrs.putAll(bootstrap.attrs);
        }
    }
    
    // ... 省略无关代码
}
```

* AbstractBootstrap 是个**抽象类**，并且实现 Cloneable 接口。另外，它声明了B 、C 两个泛型：
  * B ：继承 AbstractBootstrap 类，用于表示**自身**的类型。
  * C ：继承 Channel 类，表示表示**创建**的 Channel 类型。
* 每个属性比较简单，结合下面我们要分享的每个方法，就更易懂啦。
* 在<1> 和 <2> 两处，比较神奇的使用了synchronized 修饰符。
老艿艿也是疑惑了一下，但是这并难不倒我。#option(hannelOption<T> option, T value) 方法)，通过
synchronized 来同步，解决此问题。

## 3.2 self

/#self() 方法，返回自己。代码如下：

```java
private B self(){
    return (B) this;
}
```

* 这里就使用到了 AbstractBootstrap 声明的B 泛型。

## 3.3 group

/#group(EventLoopGroup group) 方法，设置 EventLoopGroup 到group 中。代码如下：

```java
public B group(EventLoopGroup group){
    if (group == null) {
        throw new NullPointerException("group");
    }
    if (this.group != null) { // 不允许重复设置
        throw new IllegalStateException("group set already");
    }
    this.group = group;
    return self();
}
```

* 最终调用

/#self() 方法，返回自己。实际上，AbstractBootstrap整个方法的调用，  
基本都是[“**链式调用**”](https://en.wikipedia.org/wiki/Method_chaining#Java)。

## 3.4 channel

/#channel(Class<? extends C> channelClass) 方法，设置要被**实例化**的Channel 的类。代码如下：

```java
public B channel(Class<? extends C> channelClass){
if (channelClass == null) {
    throw new NullPointerException("channelClass");
}
return channelFactory(new ReflectiveChannelFactory<C>(channelClass));
}
```

* 虽然传入的channelClass 参数，但是会使用io.netty.channel.ReflectiveChannelFactory 进行封装。
* 调用/#channelFactory(io.netty.channel.ChannelFactory<? extends C>channelFactory) 方法，设置channelFactory 属性。代码如下：

```java
public B channelFactory(io.netty.channel.ChannelFactory<? extends C> channelFactory){
    return channelFactory((ChannelFactory<C>) channelFactory);
}
@Deprecated
public B channelFactory(io.netty.bootstrap.ChannelFactory<? extends C> channelFactory){
    if (channelFactory == null) {
        throw new NullPointerException("channelFactory");
    }
    if (this.channelFactory != null) { // 不允许重复设置
        throw new IllegalStateException("channelFactory set already");
    }
    this.channelFactory = channelFactory;
    return self();
}
```

* 我们可以看到有两个/#channelFactory(...) 方法，并且第**二**个是@Deprecated 的方法。
从ChannelFactory使用的**包名**，我们就可以很容易的判断，最初 ChannelFactory 在bootstrap中，后**重构**到channel 包中。

### 3.4.1 ChannelFactory

io.netty.channel.ChannelFactory ，Channel 工厂**接口**，用于创建 Channel对象。代码如下：

```java
public interface ChannelFactory<T extends Channel> extends io.netty.bootstrap.ChannelFactory<T>{
    //*/*
    /* Creates a new channel.
    /*
    /* 创建 Channel 对象
    /*
    /*/
    @Override
    T newChannel();
}
```

* /#newChannel() 方法，用于创建 Channel 对象。

### 3.4.2 ReflectiveChannelFactory

io.netty.channel.ReflectiveChannelFactory ，实现 ChannelFactory接口，反射调用默认构造方法，创建 Channel 对象的工厂实现类。代码如下：

```java
public class ReflectiveChannelFactory<T extends Channel> implements ChannelFactory<T>{
    //*/*
    /* Channel 对应的类
    /*/
    private final Class<? extends T> clazz;
    public ReflectiveChannelFactory(Class<? extends T> clazz){
        if (clazz == null) {
            throw new NullPointerException("clazz");
        }
        this.clazz = clazz;
    }
    @Override
    public T newChannel(){
        try {
            // 反射调用默认构造方法，创建 Channel 对象
            return clazz.getConstructor().newInstance();
        } catch (Throwable t) {
            throw new ChannelException("Unable to create Channel from class " + clazz, t);
        }
    }
}
```

* 重点看clazz.getConstructor().newInstance() 代码块。

## 3.5 localAddress

/#localAddress(...) 方法，设置创建 Channel的本地地址。有四个**重载**的方法，代码如下：

```java
public B localAddress(SocketAddress localAddress){
    this.localAddress = localAddress;
    return self();
}
public B localAddress(int inetPort){
    return localAddress(new InetSocketAddress(inetPort));
}
public B localAddress(String inetHost, int inetPort){
    return localAddress(SocketUtils.socketAddress(inetHost, inetPort));
}
public B localAddress(InetAddress inetHost, int inetPort){
    return localAddress(new InetSocketAddress(inetHost, inetPort));
}
```

* 一般情况下，不会调用该方法进行配置，而是调用/#bind(...) 方法，例如 [「2. ServerBootstrap 示例」]() 。

## 3.6 option

/#option(ChannelOption<T> option, T value) 方法，设置创建 Channel的可选项。代码如下：

```java
public <T> B option(ChannelOption<T> option, T value){
    if (option == null) {
        throw new NullPointerException("option");
    }
    if (value == null) { // 空，意味着移除
        synchronized (options) {
            options.remove(option);
        }
    } else { // 非空，进行修改
        synchronized (options) {
            options.put(option, value);
        }
    }
    return self();
}
```

## 3.7 attr

/#attr(AttributeKey<T> key, T value) 方法，设置创建 Channel的属性。代码如下：

```java
public <T> B attr(AttributeKey<T> key, T value){
    if (key == null) {
        throw new NullPointerException("key");
    }
    if (value == null) { // 空，意味着移除
        synchronized (attrs) {
            attrs.remove(key);
        }
    } else { // 非空，进行修改
        synchronized (attrs) {
            attrs.put(key, value);
        }
    }
    return self();
}
```

* 怎么理解attrs 属性呢？我们可以理解成java.nio.channels.SelectionKey 的attachment 属性，并且类型为 Map 。

## 3.8 handler

/#handler(ChannelHandler handler) 方法，设置创建 Channel的处理器。代码如下：

```java
public B handler(ChannelHandler handler){
    if (handler == null) {
        throw new NullPointerException("handler");
    }
    this.handler = handler;
    return self();
}
```

## 3.9 validate

/#validate() 方法，校验配置是否正确。代码如下：

```java
public B validate(){
    if (group == null) {
        throw new IllegalStateException("group not set");
    }
    if (channelFactory == null) {
        throw new IllegalStateException("channel or channelFactory not set");
    }
    return self();
}
```

* 在/#bind(...) 方法中，绑定本地地址时，会调用该方法进行校验。

## 3.10 clone

/#clone() **抽象**方法，克隆一个 AbstractBootstrap 对象。代码如下。

```java
//*/*
/* Returns a deep clone of this bootstrap which has the identical configuration. This method is useful when making
/* multiple {@link Channel}s with similar settings. Please note that this method does not clone the
/* {@link EventLoopGroup} deeply but shallowly, making the group a shared resource.
/*/
@Override
public abstract B clone();
```

* 来自实现 Cloneable接口，在子类中实现。这是**深**拷贝，即创建一个新对象，
  但不是所有的属性是**深**拷贝。可参见[「3.1 构造方法」]() ：
    * **浅**拷贝属性：group 、channelFactory 、handler 、localAddress 。
    * **深**拷贝属性：options 、attrs 。

## 3.11 config

/#config() 方法，返回当前 AbstractBootstrap 的配置对象。代码如下：

```java
public abstract AbstractBootstrapConfig<B, C> config();
```

### 3.11.1 AbstractBootstrapConfig

io.netty.bootstrap.AbstractBootstrapConfig ，BootstrapConfig**抽象类**。代码如下：

```java
public abstract class AbstractBootstrapConfig<B extends AbstractBootstrap<B, C>, C extends Channel>{
    protected final B bootstrap;
    protected AbstractBootstrapConfig(B bootstrap){
        this.bootstrap = ObjectUtil.checkNotNull(bootstrap, "bootstrap");
    }
    public final SocketAddress localAddress(){
        return bootstrap.localAddress();
    }
    public final ChannelFactory<? extends C> channelFactory() {
        return bootstrap.channelFactory();
    }
    public final ChannelHandler handler(){
        return bootstrap.handler();
    }
    public final Map<ChannelOption<?>, Object> options() {
        return bootstrap.options();
    }
    public final Map<AttributeKey<?>, Object> attrs() {
        return bootstrap.attrs();
    }
    public final EventLoopGroup group(){
        return bootstrap.group();
    }
}
```

* bootstrap 属性，对应的启动类对象。在每个方法中，我们可以看到，都是直接调用boostrap 属性对应的方法，读取对应的配置。
AbstractBootstrapConfig的整体类图如下：
![AbstractBootstrapConfig 类图](http://static2.iocoder.cn/images/Netty/2018_04_01/03.png) AbstractBootstrapConfig类图

* 每个 Config 类，对应一个 Bootstrap 类。
* ServerBootstrapConfig 和 BootstrapConfig 的实现代码，和AbstractBootstrapConfig 基本一致，所以胖友自己去查看噢。

## 3.12 setChannelOptions

/#setChannelOptions(...) **静态**方法，设置传入的 Channel的**多个**可选项。代码如下：

```java
static void setChannelOptions(
Channel channel, Map<ChannelOption<?>, Object> options, InternalLogger logger){
    for (Map.Entry<ChannelOption<?>, Object> e: options.entrySet()) {
        setChannelOption(channel, e.getKey(), e.getValue(), logger);
    }
}
static void setChannelOptions(
Channel channel, Map.Entry<ChannelOption<?>, Object>[] options, InternalLogger logger){
    for (Map.Entry<ChannelOption<?>, Object> e: options) {
        setChannelOption(channel, e.getKey(), e.getValue(), logger);
    }
}
```

* 在两个方法的内部，**都**调用/#setChannelOption(Channel channel, ChannelOption<?> option, Object value, InternalLogger logger) **静态**方法，设置传入的 Channel的**一个**可选项。代码如下：

```java
private static void setChannelOption(
Channel channel, ChannelOption<?> option, Object value, InternalLogger logger){
    try {
        if (!channel.config().setOption((ChannelOption<Object>) option, value)) {
            logger.warn("Unknown channel option '{}' for channel '{}'", option, channel);
        }
    } catch (Throwable t) {
        logger.warn("Failed to set channel option '{}' with value '{}' for channel '{}'", option, value, channel, t);
    }
}
```

* 不同于 [「3.6 option」]() 方法，它是设置要创建的 Channel 的可选项。而/#setChannelOption(...) 方法，它是设置已经创建的 Channel 的可选项。

## 3.13 bind

/#bind(...) 方法，也可以启动 UDP 的一端，考虑到这个系列主要分享 Netty 在 NIO
相关的源码解析，所以如下所有的分享，都不考虑 UDP 的情况。

/#bind(...) 方法，绑定端口，启动服务端。代码如下：

```java
public ChannelFuture bind(){
    // 校验服务启动需要的必要参数
    validate();
    SocketAddress localAddress = this.localAddress;
    if (localAddress == null) {
        throw new IllegalStateException("localAddress not set");
    }
    // 绑定本地地址( 包括端口 )
    return doBind(localAddress);
}
public ChannelFuture bind(int inetPort){
    return bind(new InetSocketAddress(inetPort));
}
public ChannelFuture bind(String inetHost, int inetPort){
    return bind(SocketUtils.socketAddress(inetHost, inetPort));
}
public ChannelFuture bind(InetAddress inetHost, int inetPort){
    return bind(new InetSocketAddress(inetHost, inetPort));
}
public ChannelFuture bind(SocketAddress localAddress){
    // 校验服务启动需要的必要参数
    validate();
    if (localAddress == null) {
        throw new NullPointerException("localAddress");
    }
    // 绑定本地地址( 包括端口 )
    return doBind(localAddress);
}
```

* 该方法返回的是 ChannelFuture对象，也就是**异步**的绑定端口，启动服务端。如果需要**同步**，则需要调用
ChannelFuture/#sync() 方法。/#bind(...) 方法，核心流程如下图：![核心流程](http://static2.iocoder.cn/images/Netty/2018_04_01/04.png) 核心流程

* 主要有 4 个步骤，下面我们来拆解代码，看看和我们在[《精尽 Netty 源码分析 —— NIO 基础（五）之示例》](http://svip.iocoder.cn/Netty/nio-5-demo/?self)
  的 NioServer 的代码，是**怎么对应**的。

### 3.13.1 doBind

/#doBind(final SocketAddress localAddress) 方法，代码如下：

```java
public ChannelFuture bind() {
    // 校验服务启动需要的必要参数
    validate();
    SocketAddress localAddress = this.localAddress;
    if (localAddress == null) {
        throw new IllegalStateException("localAddress not set");
    }
    // 绑定本地地址( 包括端口 )
    return doBind(localAddress);
}

public ChannelFuture bind(int inetPort) {
    return bind(new InetSocketAddress(inetPort));
}

public ChannelFuture bind(String inetHost, int inetPort) {
    return bind(SocketUtils.socketAddress(inetHost, inetPort));
}

public ChannelFuture bind(InetAddress inetHost, int inetPort) {
    return bind(new InetSocketAddress(inetHost, inetPort));
}

public ChannelFuture bind(SocketAddress localAddress) {
    // 校验服务启动需要的必要参数
    validate();
    if (localAddress == null) {
        throw new NullPointerException("localAddress");
    }
    // 绑定本地地址( 包括端口 )
    return doBind(localAddress);
}

```

* 第 3 行：调用/#initAndRegister() 方法，初始化并注册一个 Channel对象。
  因为注册是**异步**的过程，所以返回一个 ChannelFuture 对象。详细解析，见[「3.14 initAndRegister」]() 。

* 第 5 至 7 行：若发生异常，直接进行返回。
* 第 9 至 37行：因为注册是**异步**的过程，有可能已完成，有可能未完成。
  所以实现代码分成了【第10 至 14 行】和【第 15 至 36 行】分别处理已完成和未完成的情况。
    * **核心**在【第 13 行】或者【第 32 行】的代码，调用/#doBind0(final ChannelFuture regFuture, final Channel channel, final
    SocketAddress localAddress, final ChannelPromise promise) 方法，绑定Channel 的端口，并注册 Channel 到 SelectionKey 中。
    * 如果**异步**注册对应的 ChanelFuture 未完成，则调用ChannelFuture/#addListener(ChannelFutureListener)
    方法，添加监听器，在**注册**完成后，进行回调执行/#doBind0(...) 方法的逻辑。详细解析，见 见 [「3.13.2 doBind0」]() 。
    * 所以总结来说，**bind 的逻辑，执行在 register 的逻辑之后**。
    * TODO 1001 Promise 2. PendingRegistrationPromise

### 3.13.2 doBind0

此小节的内容，胖友先看完 [「3.14 initAndRegister」]()
的内容在回过头来看。因为/#doBind0(...) 方法的执行，在/#initAndRegister() 方法之后。/#doBind0(...) 方法，执行 Channel 的端口绑定逻辑。代码如下：

```java
 1: private static void doBind0(
 2:         final ChannelFuture regFuture, final Channel channel,
 3:         final SocketAddress localAddress, final ChannelPromise promise) {
 4: 
 5:     // This method is invoked before channelRegistered() is triggered.  Give user handlers a chance to set up
 6:     // the pipeline in its channelRegistered() implementation.
 7:     channel.eventLoop().execute(new Runnable() {
 8:         @Override
 9:         public void run() {
11:             // 注册成功，绑定端口
12:             if (regFuture.isSuccess()) {
13:                 channel.bind(localAddress, promise).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
14:             // 注册失败，回调通知 promise 异常
15:             } else {
16:                 promise.setFailure(regFuture.cause());
17:             }
18:         }
19:     });
20: }

```

* 第 7 行：调用 EventLoop 执行 Channel
  的端口绑定逻辑。但是，实际上当前线程已经是 EventLoop
  所在的线程了，为何还要这样操作呢？答案在【第 5 至 6行】的英语注释。感叹句，Netty
  虽然代码量非常庞大且复杂，但是英文注释真的是非常齐全，包括 Github 的 issue
  对代码提交的描述，也非常健全。
* 第 14 至 17 行：注册失败，回调通知promise 异常。
* 第 11 至 13 行：注册成功，调用Channel/#bind(SocketAddress localAddress, ChannelPromise promise)
方法，执行 Channel的端口绑定逻辑。后续的方法栈调用如下图：[![Channel bind 流程](http://static2.iocoder.cn/images/Netty/2018_04_01/09.png)](http://static2.iocoder.cn/images/Netty/2018_04_01/09.png "Channel bind 流程")Channel
bind 流程

* 还是老样子，我们先省略掉 pipeline 的内部实现代码，从AbstractUnsafe/#bind(final SocketAddress localAddress, final
ChannelPromise promise) 方法，继续向下分享。AbstractUnsafe/#bind(final SocketAddress localAddress, final
ChannelPromise promise) 方法，Channel 的端口绑定逻辑。代码如下：

```java
 1: @Override
 2: public final void bind(final SocketAddress localAddress, final ChannelPromise promise) {
 3:     // 判断是否在 EventLoop 的线程中。
 4:     assertEventLoop();
 5: 
 6:     if (!promise.setUncancellable() || !ensureOpen(promise)) {
 7:         return;
 8:     }
 9: 
10:     // See: https://github.com/netty/netty/issues/576
11:     if (Boolean.TRUE.equals(config().getOption(ChannelOption.SO_BROADCAST)) &&
12:         localAddress instanceof InetSocketAddress &&
13:         !((InetSocketAddress) localAddress).getAddress().isAnyLocalAddress() &&
14:         !PlatformDependent.isWindows() && !PlatformDependent.maybeSuperUser()) {
15:         // Warn a user about the fact that a non-root user can't receive a
16:         // broadcast packet on *nix if the socket is bound on non-wildcard address.
17:         logger.warn(
18:                 "A non-root user can't receive a broadcast packet if the socket " +
19:                 "is not bound to a wildcard address; binding to a non-wildcard " +
20:                 "address (" + localAddress + ") anyway as requested.");
21:     }
22: 
23:     // 记录 Channel 是否激活
24:     boolean wasActive = isActive();
25: 
26:     // 绑定 Channel 的端口
27:     try {
28:         doBind(localAddress);
29:     } catch (Throwable t) {
30:         safeSetFailure(promise, t);
31:         closeIfClosed();
32:         return;
33:     }
34: 
35:     // 若 Channel 是新激活的，触发通知 Channel 已激活的事件。
36:     if (!wasActive && isActive()) {
37:         invokeLater(new Runnable() {
38:             @Override
39:             public void run() {
40:                 pipeline.fireChannelActive();
41:             }
42:         });
43:     }
44: 
45:     // 回调通知 promise 执行成功
46:     safeSetSuccess(promise);
47: }
```

* 第 4 行：调用

/#assertEventLoop() 方法，判断是否在 EventLoop 的线程中。即该方法，只允许在EventLoop 的线程中执行。代码如下：

```java
// AbstractUnsafe.java
private void assertEventLoop(){
    assert !registered || eventLoop.inEventLoop();
}
```

* 第 6 至 8 行：和/#register0(...) 方法的【第 5 至 8 行】的代码，是一致的。
* 第 10 至 21
  行：[https://github.com/netty/netty/issues/576](https://github.com/netty/netty/issues/576)
* 第 24 行：调用/#isActive() 方法，获得 Channel 是否激活( active )。NioServerSocketChannel
对该方法的实现代码如下：

```java
// NioServerSocketChannel.java
@Override
public boolean isActive(){
    return javaChannel().socket().isBound();
}
```

* NioServerSocketChannel 的/#isActive() 的方法实现，判断 ServerSocketChannel
是否绑定端口。此时，一般返回的是false 。
* 第 28 行：调用/#doBind(SocketAddress localAddress) 方法，绑定 Channel 的端口。代码如下：

```java
// NioServerSocketChannel.java
@Override
protected void doBind(SocketAddress localAddress) throws Exception{
    if (PlatformDependent.javaVersion() >= 7) {
        javaChannel().bind(localAddress, config.getBacklog());
    } else {
        javaChannel().socket().bind(localAddress, config.getBacklog());
    }
}
```

* 【重要】到了此处，服务端的 Java 原生 NIO ServerSocketChannel终于绑定端口。😈
* 第 36 行：再次调用/#isActive() 方法，获得 Channel 是否激活。此时，一般返回的是
true 。因此，Channel 可以认为是**新激活**的，满足【第 36 至 43行】代码的执行条件。

* 第 37 行：调用/#invokeLater(Runnable task) 方法，提交任务，让【第 40行】的代码执行，异步化。代码如下：

```java
// AbstractUnsafe.java
private void invokeLater(Runnable task){
    try {
        // This method is used by outbound operation implementations to trigger an inbound event later.
        // They do not trigger an inbound event immediately because an outbound operation might have been
        // triggered by another inbound event handler method. If fired immediately, the call stack
        // will look like this for example:
        //
        // handlerA.inboundBufferUpdated() - (1) an inbound handler method closes a connection.
        // -> handlerA.ctx.close()
        // -> channel.unsafe.close()
        // -> handlerA.channelInactive() - (2) another inbound handler method called while in (1) yet
        //
        // which means the execution of two inbound handler methods of the same handler overlap undesirably.
        eventLoop().execute(task);
    } catch (RejectedExecutionException e) {
        logger.warn("Can't invoke task later as EventLoop rejected it", e);
    }
}
```

* 从实现代码可以看出，是通过提交一个新的任务到 EventLoop 的线程中。
* 英文注释虽然有丢丢长，但是胖友耐心看完。有道在手，英文不愁啊。
* 第 40 行：调用DefaultChannelPipeline/#fireChannelActive() 方法，触发 Channel激活的事件。详细解析，见 [「3.13.3 beginRead」]() 。
* 第 46 行：调用/#safeSetSuccess(ChannelPromise) 方法，回调通知promise 执行成功。此处的通知，对应回调的是我们添加到
/#bind(...) 方法返回的 ChannelFuture 的 ChannelFutureListener的监听器。示例代码如下：

```
ChannelFuture f = b.bind(PORT).addListener(new ChannelFutureListener() { // 回调的就是我！！！
@Override
public void operationComplete(ChannelFuture future) throws Exception{
    System.out.println("测试下被触发");
}
}).sync();
```

### 3.13.3 beginRead

老艿艿：此小节的内容，胖友先看完 [「3.14 initAndRegister」]()
的内容在回过头来看。因为

/#beginRead(...) 方法的执行，在

/#doBind0(...) 方法之后。
在/#bind(final SocketAddress localAddress, final ChannelPromise promise)
方法的【第 40 行】代码，调用
Channel/#bind(SocketAddress localAddress, ChannelPromise promise)
方法，触发 Channel
激活的事件。后续的方法栈调用如下图：![触发 Channel 激活的事件](http://static2.iocoder.cn/images/Netty/2018_04_01/10.png) 触发Channel 激活的事件

```
/* 还是老样子，我们先省略掉 pipeline 的内部实现代码，从 `AbstractUnsafe/#beginRead()` 方法，继续向下分享。
```

AbstractUnsafe/#beginRead() 方法，开始读取操作。代码如下：

```java
@Override
public final void beginRead(){
    // 判断是否在 EventLoop 的线程中。
    assertEventLoop();
    // Channel 必须激活
    if (!isActive()) {
        return;
    }
    // 执行开始读取
    try {
        doBeginRead();
    } catch (final Exception e) {
    invokeLater(new Runnable() {
    @Override
    public void run(){
        pipeline.fireExceptionCaught(e);
    }
    });
    close(voidPromise());
    }
}
```

* 调用

Channel/#doBeginRead() 方法，执行开始读取。对于 NioServerSocketChannel
来说，该方法实现代码如下：

```java
// AbstractNioMessageChannel.java
@Override
protected void doBeginRead() throws Exception{
if (inputShutdown) {
return;
}
super.doBeginRead();
}
// AbstractNioChannel.java
@Override
protected void doBeginRead() throws Exception{
// Channel.read() or ChannelHandlerContext.read() was called
final SelectionKey selectionKey = this.selectionKey;
if (!selectionKey.isValid()) {
return;
}
readPending = true;
final int interestOps = selectionKey.interestOps();
if ((interestOps & readInterestOp) == 0) {
selectionKey.interestOps(interestOps | readInterestOp);
}
}
```

* 【重要】在最后几行，我们可以看到，调用

SelectionKey/#interestOps(ops) 方法，将我们创建 NioServerSocketChannel时，设置的

readInterestOp = SelectionKey.OP_ACCEPT
添加为感兴趣的事件。也就说，服务端可以开始处理客户端的连接事件。

## 3.14 initAndRegister

/#initAndRegister() 方法，初始化并注册一个 Channel 对象，并返回一个
ChannelFuture 对象。代码如下：

```java
1: final ChannelFuture initAndRegister(){
2: Channel channel = null;
3: try {
4: // 创建 Channel 对象
5: channel = channelFactory.newChannel();
6: // 初始化 Channel 配置
7: init(channel);
8: } catch (Throwable t) {
9: if (channel != null) { // 已创建 Channel 对象
10: // channel can be null if newChannel crashed (eg SocketException("too many open files"))
11: channel.unsafe().closeForcibly(); // 强制关闭 Channel
12: // as the Channel is not registered yet we need to force the usage of the GlobalEventExecutor
13: return new DefaultChannelPromise(channel, GlobalEventExecutor.INSTANCE).setFailure(t);
14: }
15: // as the Channel is not registered yet we need to force the usage of the GlobalEventExecutor
16: return new DefaultChannelPromise(new FailedChannel(), GlobalEventExecutor.INSTANCE).setFailure(t);
17: }
18:
19: // 注册 Channel 到 EventLoopGroup 中
20: ChannelFuture regFuture = config().group().register(channel);
21: if (regFuture.cause() != null) {
22: if (channel.isRegistered()) {
23: channel.close();
24: } else {
25: channel.unsafe().closeForcibly(); // 强制关闭 Channel
26: }
27: }
28:
29: return regFuture;
30: }
```

* 第 5 行：调用

ChannelFactory/#newChannel() 方法，创建 Channel 对象。在本文的示例中，会使用
ReflectiveChannelFactory 创建 NioServerSocketChannel 对象。详细解析，见
[「3.14.1 创建 Channel」]() 。

* 第 16 行：返回带异常的 DefaultChannelPromise 对象。因为创建 Channel
  对象失败，所以需要创建一个 FailedChannel 对象，设置到 DefaultChannelPromise
  中才可以返回。
* 第 7 行：调用/#init(Channel) 方法，初始化 Channel 配置。详细解析，见
[「3.14.1 创建 Channel」]() 。

* 第 9 至 14 行：返回带异常的 DefaultChannelPromise 对象。因为初始化 Channel
  对象失败，所以需要调用/#closeForcibly() 方法，强制关闭 Channel 。
* 第 20 行：首先获得 EventLoopGroup 对象，后调用
EventLoopGroup/#register(Channel) 方法，注册 Channel 到 EventLoopGroup
中。实际在方法内部，EventLoopGroup 会分配一个 EventLoop 对象，将 Channel
注册到其上。详细解析，见 [「3.14.3 注册 Channel 到 EventLoopGroup」]() 。

* 第 22 至 23 行：若发生异常，并且 Channel 已经注册成功，则调用/#close() 方法，正常关闭 Channel 。
* 第 24 至 26 行：若发生异常，并且 Channel 并未注册成功，则调用
/#closeForcibly() 方法，强制关闭 Channel 。也就是说，和【第 9 至 14
行】一致。为什么会有正常和强制关闭 Channel 两种不同的处理呢？我们来看下

/#close() 和/#closeForcibly() 方法的注释：

```java
// Channel.java/#Unsafe
//*/*
/* Close the {@link Channel} of the {@link ChannelPromise} and notify the {@link ChannelPromise} once the
/* operation was complete.
/*/
void close(ChannelPromise promise);
//*/*
/* Closes the {@link Channel} immediately without firing any events. Probably only useful
/* when registration attempt failed.
/*/
void closeForcibly();
```

* 调用的前提，在于 Channel 是否注册到 EventLoopGroup 成功。😈
  因为注册失败，也不好触发相关的事件。

### 3.14.1 创建 Channel 对象

考虑到本文的内容，我们以 NioServerSocketChannel 的创建过程作为示例。流程图如下：

![创建 NioServerSocketChannel 对象](http://static2.iocoder.cn/images/Netty/2018_04_01/05.png) 创建NioServerSocketChannel 对象

* 我们可以看到，整个流程涉及到 NioServerSocketChannel 的父类们。类图如下：
  ![Channel 类图](http://static2.iocoder.cn/images/Netty/2018_04_01/06.png)Channel类图
* 可能有部分胖友对 Netty Channel 的定义不是很理解，如下是官方的英文注释： A
  nexus to a network socket or a component which is capable of I/O
  operations such as read, write, connect, and bind

* 简单点来说，我们可以把 Netty Channel 和 Java 原生 Socket 对应，而 Netty NIO
  Channel 和 Java 原生 NIO SocketChannel 对象。

下面，我们来看看整个 NioServerSocketChannel 的创建过程的代码实现。

### 3.14.1.1 NioServerSocketChannel

```
private static final SelectorProvider DEFAULT_SELECTOR_PROVIDER = SelectorProvider.provider();
private final ServerSocketChannelConfig config;
public NioServerSocketChannel(){
this(newSocket(DEFAULT_SELECTOR_PROVIDER));
}
public NioServerSocketChannel(SelectorProvider provider){
this(newSocket(provider));
}
```

* DEFAULT_SELECTOR_PROVIDER **静态**属性，默认的 SelectorProvider 实现类。
* config 属性，Channel 对应的配置对象。每种 Channel 实现类，也会对应一个
  ChannelConfig 实现类。例如，NioServerSocketChannel 类，对应
  ServerSocketChannelConfig 配置类。 ChannelConfig 的官网英文描述： A set
  of configuration properties of a Channel.
* 在构造方法中，调用

/#newSocket(SelectorProvider provider) 方法，创建 NIO 的ServerSocketChannel 对象。代码如下：

```
private static ServerSocketChannel newSocket(SelectorProvider provider){
try {
return provider.openServerSocketChannel();
} catch (IOException e) {
throw new ChannelException("Failed to open a server socket.", e);
}
}
```

* 😈 是不是很熟悉这样的代码，效果和

ServerSocketChannel/#open() 方法创建 ServerSocketChannel 对象是一致。
* /#NioServerSocketChannel(ServerSocketChannel channel)构造方法，代码如下：

```java
public NioServerSocketChannel(ServerSocketChannel channel){
    super(null, channel, SelectionKey.OP_ACCEPT);
    config = new NioServerSocketChannelConfig(this, javaChannel().socket());
}
```

* 调用父 AbstractNioMessageChannel 的构造方法。详细解析，见
  [「3.14.1.2 AbstractNioMessageChannel」]() 。注意传入的 SelectionKey
  的值为OP_ACCEPT 。
* 初始化config 属性，创建 NioServerSocketChannelConfig 对象。

### 3.14.1.2 AbstractNioMessageChannel

```
protected AbstractNioMessageChannel(Channel parent, SelectableChannel ch, int readInterestOp){
super(parent, ch, readInterestOp);
}
```

* 直接调用父 AbstractNioChannel 的构造方法。详细解析，见[「3.14.1.3 AbstractNioChannel」]() 。

### []( "3.14.1.3 AbstractNioChannel")3.14.1.3 AbstractNioChannel

```java
private final SelectableChannel ch;
protected final int readInterestOp;
protected AbstractNioChannel(Channel parent, SelectableChannel ch, int readInterestOp){
super(parent);
this.ch = ch;
this.readInterestOp = readInterestOp;
try {
ch.configureBlocking(false);
} catch (IOException e) {
try {
ch.close();
} catch (IOException e2) {
if (logger.isWarnEnabled()) {
logger.warn("Failed to close a partially initialized socket.", e2);
}
}
throw new ChannelException("Failed to enter non-blocking mode.", e);
}
}
```

* ch 属性，**Netty NIO Channel 对象，持有的 Java 原生 NIO 的 Channel
  对象**。
* readInterestOp 属性，感兴趣的读事件的操作位值。

* 目前笔者看了 AbstractNioMessageChannel 是SelectionKey.OP_ACCEPT ， 而 AbstractNioByteChannel 是SelectionKey.OP_READ 。
* 详细的用途，我们会在 [「3.13.3 beginRead」]() 看到。
* 调用父 AbstractNioChannel 的构造方法。详细解析，见
  [「3.14.1.4 AbstractChannel」]() 。
* 调用SelectableChannel/#configureBlocking(false) 方法，设置 NIO Channel为**非阻塞**。😈 这块代码是不是非常熟悉哟。

* 若发生异常，关闭 NIO Channel ，并抛出异常。

### 3.14.1.4 AbstractChannel

```java
//*/*
/* 父 Channel 对象
/*/
private final Channel parent;
//*/*
/* Channel 编号
/*/
private final ChannelId id;
//*/*
/* Unsafe 对象
/*/
private final Unsafe unsafe;
//*/*
/* DefaultChannelPipeline 对象
/*/
private final DefaultChannelPipeline pipeline;
protected AbstractChannel(Channel parent){
this.parent = parent;
// 创建 ChannelId 对象
id = newId();
// 创建 Unsafe 对象
unsafe = newUnsafe();
// 创建 DefaultChannelPipeline 对象
pipeline = newChannelPipeline();
}
```

* parent 属性，父 Channel 对象。对于 NioServerSocketChannel 的parent 为空。
* id 属性，Channel 编号对象。在构造方法中，通过调用/#newId() 方法，进行创建。本文就先不分享，感兴趣的胖友自己看。
* unsafe 属性，Unsafe 对象。在构造方法中，通过调用/#newUnsafe() 方法，进行创建。本文就先不分享，感兴趣的胖友自己看。

* 这里的 Unsafe 并不是我们常说的 Java 自带的sun.misc.Unsafe ，而是io.netty.channel.Channel/#Unsafe 。

```java
// Channel.java/#Unsafe
//*/*
/* <em>Unsafe</em> operations that should <em>never</em> be called from user-code. These methods
/* are only provided to implement the actual transport, and must be invoked from an I/O thread except for the
/* following methods:
/* <ul>
/* <li>{@link /#localAddress()}</li>
/* <li>{@link /#remoteAddress()}</li>
/* <li>{@link /#closeForcibly()}</li>
/* <li>{@link /#register(EventLoop, ChannelPromise)}</li>
/* <li>{@link /#deregister(ChannelPromise)}</li>
/* <li>{@link /#voidPromise()}</li>
/* </ul>
/*/
```

* 这就是为什么叫 Unsafe 的原因。按照上述官网类的英文注释，Unsafe
  操作不允许被用户代码使用。这些函数是真正用于数据传输操作，必须被IO线程调用。
* 实际上，Channel 真正的具体操作，通过调用对应的 Unsafe 实现。😈
  下文，我们将会看到。
* Unsafe 不是一个具体的类，而是一个定义在 Channel 接口中的接口。不同的 Channel
  类对应不同的 Unsafe
  实现类。整体类图如下：![Unsafe 类图](http://static2.iocoder.cn/images/Netty/2018_04_01/07.png)Unsafe类图

* 对于 NioServerSocketChannel ，Unsafe 的实现类为 NioMessageUnsafe 。
* pipeline 属性，DefaultChannelPipeline 对象。在构造方法中，通过调用

/#newChannelPipeline() 方法，进行创建。本文就先不分享，感兴趣的胖友自己看。
ChannelPipeline 的英文注释：A list of ChannelHandlers which handles or
intercepts inbound events and outbound operations of a Channel 。

### 3.14.1.5 小结

看到此处，我们来对 [「3.1.4.1 创建 Channel 对象」]() 作一个小结。

对于一个 Netty NIO Channel 对象，它会包含如下几个核心组件：

* ChannelId
* Unsafe
* Pipeline
* ChannelHandler
* ChannelConfig
* **Java 原生 NIO Channel**

如果不太理解，可以撸起袖子，多调试几次。

### 3.14.2 初始化 Channel 配置

/#init(Channel channel) 方法，初始化 Channel 配置。它是个**抽象**方法，由子类
ServerBootstrap 或 Bootstrap 自己实现。代码如下：

```java
abstract void init(Channel channel) throws Exception;
```

* ServerBootstrap 对该方法的实现，我们在 [「4. ServerBootstrap」]()
  中，详细解析。

### 3.14.3 注册 Channel 到 EventLoopGroup

EventLoopGroup/#register(Channel channel) 方法，注册 Channel 到
EventLoopGroup 中。整体流程如下：

![register 流程](http://static2.iocoder.cn/images/Netty/2018_04_01/08.png) register流程

### 3.14.3.1 register

EventLoopGroup 和 EventLoop 不是本文的重点，所以省略 1 + 2 + 3
部分的代码，从第 4 步的AbstractUnsafe/#register(EventLoop eventLoop, final ChannelPromisepromise) 方法开始，代码如下：

```java
1: @Override
2: public final void register(EventLoop eventLoop, final ChannelPromise promise){
3: // 校验传入的 eventLoop 非空
4: if (eventLoop == null) {
5: throw new NullPointerException("eventLoop");
6: }
7: // 校验未注册
8: if (isRegistered()) {
9: promise.setFailure(new IllegalStateException("registered to an event loop already"));
10: return;
11: }
12: // 校验 Channel 和 eventLoop 匹配
13: if (!isCompatible(eventLoop)) {
14: promise.setFailure(new IllegalStateException("incompatible event loop type: " + eventLoop.getClass().getName()));
15: return;
16: }
17:
18: // 设置 Channel 的 eventLoop 属性
19: AbstractChannel.this.eventLoop = eventLoop;
20:
21: // 在 EventLoop 中执行注册逻辑
22: if (eventLoop.inEventLoop()) {
23: register0(promise);
24: } else {
25: try {
26: eventLoop.execute(new Runnable() {
27: @Override
28: public void run(){
31: register0(promise);
32: }
33: });
34: } catch (Throwable t) {
35: logger.warn("Force-closing a channel whose registration task was not accepted by an event loop: {}", AbstractChannel.this, t);
36: closeForcibly();
37: closeFuture.setClosed();
38: safeSetFailure(promise, t);
39: }
40: }
41: }
```

* 第 3 至 6 行：校验传入的eventLoop 参数非空。
* 第 7 至 11 行：调用/#isRegistered() 方法，校验未注册。代码如下：

```java
// AbstractChannel.java
//*/*
/* 是否注册
/*/
private volatile boolean registered;
@Override
public boolean isRegistered(){
    return registered;
}
```

* 第 12 至 16 行：校验 Channel 和eventLoop 类型是否匹配，因为它们都有多种实现类型。代码如下：

```
@Override
protected boolean isCompatible(EventLoop loop){
    return loop instanceof NioEventLoop;
}
```

* 要求eventLoop 的类型为 NioEventLoop 。
* 第 19 行：【重要】设置 Channel 的eventLoop 属性。
* 第 21 至 40 行：在evnetLoop 中，调用/#register0() 方法，执行注册的逻辑。详细解析，见 [「3.14.3.2 register0」]()。
* 第 34 至 39 行：若调用EventLoop/#execute(Runnable) 方法发生异常，则进行处理：
* 第 36 行：调用AbstractUnsafe/#closeForcibly() 方法，强制关闭 Channel 。
* 第 37 行：调用CloseFuture/#setClosed() 方法，通知closeFuture 已经关闭。详细解析，见
[《精尽 Netty 源码解析 —— Channel（七）之 close 操作》](http://svip.iocoder.cn/Netty/Channel-7-close/)。
* 第 38 行：调用

AbstractUnsafe/#safeSetFailure(ChannelPromise promise, Throwable cause)
方法，回调通知promise 发生该异常。

### 3.14.3.2 register0

/#register0(ChannelPromise promise) 方法，注册逻辑。代码如下：

```
1: private void register0(ChannelPromise promise){
2: try {
3: // check if the channel is still open as it could be closed in the mean time when the register
4: // call was outside of the eventLoop
5: if (!promise.setUncancellable() // TODO 1001 Promise
6: || !ensureOpen(promise)) { // 确保 Channel 是打开的
7: return;
8: }
9: // 记录是否为首次注册
10: boolean firstRegistration = neverRegistered;
11:
12: // 执行注册逻辑
13: doRegister();
14:
15: // 标记首次注册为 false
16: neverRegistered = false;
17: // 标记 Channel 为已注册
18: registered = true;
19:
20: // Ensure we call handlerAdded(...) before we actually notify the promise. This is needed as the
21: // user may already fire events through the pipeline in the ChannelFutureListener.
22: pipeline.invokeHandlerAddedIfNeeded();
23:
24: // 回调通知 `promise` 执行成功
25: safeSetSuccess(promise);
26:
27: // 触发通知已注册事件
28: pipeline.fireChannelRegistered();
29:
30: // TODO 芋艿
31: // Only fire a channelActive if the channel has never been registered. This prevents firing
32: // multiple channel actives if the channel is deregistered and re-registered.
33: if (isActive()) {
34: if (firstRegistration) {
35: pipeline.fireChannelActive();
36: } else if (config().isAutoRead()) {
37: // This channel was registered before and autoRead() is set. This means we need to begin read
38: // again so that we process inbound data.
39: //
40: // See https://github.com/netty/netty/issues/4805
41: beginRead();
42: }
43: }
44: } catch (Throwable t) {
45: // Close the channel directly to avoid FD leak.
46: closeForcibly();
47: closeFuture.setClosed();
48: safeSetFailure(promise, t);
49: }
50: }
```

* 第 5 行：// TODO 1001 Promise
* 第 6 行：调用/#ensureOpen(ChannelPromise) 方法，确保 Channel 是打开的。代码如下：

```
// AbstractUnsafe.java
protected final boolean ensureOpen(ChannelPromise promise){
if (isOpen()) {
return true;
}
// 若未打开，回调通知 promise 异常
safeSetFailure(promise, ENSURE_OPEN_CLOSED_CHANNEL_EXCEPTION);
return false;
}
// AbstractNioChannel.java
@Override
public boolean isOpen(){
return ch.isOpen();
}
```

* 第 10 行：记录是否**首次**注册。neverRegistered 变量声明在 AbstractUnsafe 中，代码如下：

```
//*/*
/* 是否重未注册过，用于标记首次注册
/*
/* true if the channel has never been registered, false otherwise
/*/
private boolean neverRegistered = true;
```

* 第 13 行：调用/#doRegister() 方法，执行注册逻辑。代码如下：

```
// NioUnsafe.java
1: @Override
2: protected void doRegister() throws Exception{
3: boolean selected = false;
4: for (;;) {
5: try {
6: selectionKey = javaChannel().register(eventLoop().unwrappedSelector(), 0, this);
7: return;
8: } catch (CancelledKeyException e) {
9: // TODO TODO 1003 doRegister 异常
10: if (!selected) {
11: // Force the Selector to select now as the "canceled" SelectionKey may still be
12: // cached and not removed because no Select.select(..) operation was called yet.
13: eventLoop().selectNow();
14: selected = true;
15: } else {
16: // We forced a select operation on the selector before but the SelectionKey is still cached
17: // for whatever reason. JDK bug ?
18: throw e;
19: }
20: }
21: }
22: }
```

* 第 6 行：调用

/#unwrappedSelector() 方法，返回 Java 原生 NIO Selector 对象。代码如下：

```
// NioEventLoop.java
private Selector unwrappedSelector;
Selector unwrappedSelector(){
return unwrappedSelector;
}
```

* 每个 NioEventLoop 对象上，都**独有**一个 Selector 对象。
* 第 6 行：调用/#javaChannel() 方法，获得 Java 原生 NIO 的 Channel 对象。
* 第 6 行：【重要】调用

SelectableChannel/#register(Selector sel, int ops, Object att) 方法，注册
Java 原生 NIO 的 Channel 对象到 Selector
对象上。相信胖友对这块的代码是非常熟悉的，但是为什么感兴趣的事件是为 **0**
呢？正常情况下，对于服务端来说，需要注册

SelectionKey.OP_ACCEPT 事件呢！这样做的**目的**是(摘自《Netty权威指南（第二版）》 )：
1. 注册方式是多态的，它既可以被 NIOServerSocketChannel
   用来监听客户端的连接接入，也可以注册 SocketChannel 用来监听网络读或者写操作。
2. 通过SelectionKey/#interestOps(int ops)
方法可以方便地修改监听操作位。所以，此处注册需要获取 SelectionKey 并给
AbstractNIOChannel 的成员变量selectionKey 赋值。

* 如果不理解，没关系，在下文中，我们会看到服务端对SelectionKey.OP_ACCEPT 事件的关注。😈
* 第 8 至 20 行：TODO 1003 doRegister 异常
* 第 16 行：标记首次注册为false 。
* 第 18 行：标记 Channel 为已注册。registered 变量声明在 AbstractChannel 中，代码如下：

```java
//*/*
/* 是否注册
/*/
private volatile boolean registered;
```

* 第 22 行：调用

DefaultChannelPipeline/#invokeHandlerAddedIfNeeded() 方法，触发
ChannelInitializer 执行，进行 Handler 初始化。也就是说，我们在 [「4.init」]()
写的 ServerBootstrap 对 Channel 设置的 ChannelInitializer 将被执行，进行
Channel 的 Handler 的初始化。

* 具体的 pipeline 的内部调用过程，我们在后续文章分享。
* 第 25 行：调用/#safeSetSuccess(ChannelPromise promise) 方法，回调通知
promise 执行。在 [「3.13.1 doBind」]() 小节，我们向
regFuture 注册的 ChannelFutureListener ，就会被**立即回调执行**。
老艿艿：胖友在看完这小节的内容，可以调回 [「3.13.2 doBind0」]()小节的内容继续看。
* 第 28 行：调用
DefaultChannelPipeline/#invokeHandlerAddedIfNeeded() 方法，触发通知
Channel 已注册的事件。
* 具体的 pipeline 的内部调用过程，我们在后续文章分享。
* 笔者目前调试下来，没有涉及服务端启动流程的逻辑代码。
* 第 33 至 43 行：TODO 芋艿
* 第 44 至 49 行：发生异常，和
/#register(EventLoop eventLoop, final ChannelPromise promise)
方法的处理异常的代码，是一致的。

# 4. ServerBootstrap

io.netty.bootstrap.ServerBootstrap ，实现 AbstractBootstrap 抽象类，用于
Server 的启动器实现类。

## []( "4.1 构造方法")4.1 构造方法

```java
//*/*
/* 启动类配置对象
/*/
private final ServerBootstrapConfig config = new ServerBootstrapConfig(this);
//*/*
/* 子 Channel 的可选项集合
/*/
private final Map<ChannelOption<?>, Object> childOptions = new LinkedHashMap<ChannelOption<?>, Object>();
//*/*
/* 子 Channel 的属性集合
/*/
private final Map<AttributeKey<?>, Object> childAttrs = new LinkedHashMap<AttributeKey<?>, Object>();
//*/*
/* 子 Channel 的 EventLoopGroup 对象
/*/
private volatile EventLoopGroup childGroup;
//*/*
/* 子 Channel 的处理器
/*/
private volatile ChannelHandler childHandler;
public ServerBootstrap(){ }
private ServerBootstrap(ServerBootstrap bootstrap){
super(bootstrap);
childGroup = bootstrap.childGroup;
childHandler = bootstrap.childHandler;
synchronized (bootstrap.childOptions) {
childOptions.putAll(bootstrap.childOptions);
}
synchronized (bootstrap.childAttrs) {
childAttrs.putAll(bootstrap.childAttrs);
}
}
```

* config 属性，ServerBootstrapConfig 对象，启动类配置对象。
* 在 Server 接受**一个** Client 的连接后，会创建**一个**对应的 Channel
对象。因此，我们看到 ServerBootstrap 的childOptions 、childAttrs 、childGroup 、
childHandler 属性，都是这种 Channel 的可选项集合、属性集合、EventLoopGroup
对象、处理器。下面，我们会看到 ServerBootstrap 针对这些配置项的设置方法。

## 4.2 group

/#group(..) 方法，设置 EventLoopGroup 到group 、childGroup 中。代码如下：

```java
@Override
public ServerBootstrap group(EventLoopGroup group){
return group(group, group);
}
public ServerBootstrap group(EventLoopGroup parentGroup, EventLoopGroup childGroup){
super.group(parentGroup);
if (childGroup == null) {
throw new NullPointerException("childGroup");
}
if (this.childGroup != null) {
throw new IllegalStateException("childGroup set already");
}
this.childGroup = childGroup;
return this;
}
```

* 当只传入一个 EventLoopGroup 对象时，即调用的是
/#group(EventLoopGroup group) 时，group 和childGroup 使用同一个。一般情况下，我们不使用这个方法。

## 4.3 childOption

/#childOption(ChannelOption<T> option, T value) 方法，设置子 Channel
的可选项。代码如下：

```
public <T> ServerBootstrap childOption(ChannelOption<T> childOption, T value){
if (childOption == null) {
throw new NullPointerException("childOption");
}
if (value == null) { // 空，意味着移除
synchronized (childOptions) {
childOptions.remove(childOption);
}
} else { // 非空，进行修改
synchronized (childOptions) {
childOptions.put(childOption, value);
}
}
return this;
}
```

## 4.4 childAttr

/#childAttr(AttributeKey<T> key, T value) 方法，设置子 Channel
的属性。代码如下：

```
public <T> ServerBootstrap childAttr(AttributeKey<T> childKey, T value){
if (childKey == null) {
throw new NullPointerException("childKey");
}
if (value == null) { // 空，意味着移除
childAttrs.remove(childKey);
} else { // 非空，进行修改
childAttrs.put(childKey, value);
}
return this;
}
```

## 4.5 childHandler

/#childHandler(ChannelHandler handler) 方法，设置子 Channel
的处理器。代码如下：

```
public ServerBootstrap childHandler(ChannelHandler childHandler){
if (childHandler == null) {
throw new NullPointerException("childHandler");
}
this.childHandler = childHandler;
return this;
}
```

## 4.6 validate

/#validate() 方法，校验配置是否正确。代码如下：

```
@Override
public ServerBootstrap validate(){
super.validate();
if (childHandler == null) {
throw new IllegalStateException("childHandler not set");
}
if (childGroup == null) {
logger.warn("childGroup is not set. Using parentGroup instead.");
childGroup = config.group();
}
return this;
}
```

## 4.7 clone

/#clone() 方法，克隆 ServerBootstrap 对象。代码如下：

```
@Override
public ServerBootstrap clone(){
return new ServerBootstrap(this);
}
```

* 调用参数为bootstrap 为 ServerBootstrap 构造方法，克隆一个 ServerBootstrap 对象。

## 4.8 init

/#init(Channel channel) 方法，初始化 Channel 配置。代码如下：

```
1: @Override
2: void init(Channel channel) throws Exception{
3: // 初始化 Channel 的可选项集合
4: final Map<ChannelOption<?>, Object> options = options0();
5: synchronized (options) {
6: setChannelOptions(channel, options, logger);
7: }
8:
9: // 初始化 Channel 的属性集合
10: final Map<AttributeKey<?>, Object> attrs = attrs0();
11: synchronized (attrs) {
12: for (Entry<AttributeKey<?>, Object> e: attrs.entrySet()) {
13: @SuppressWarnings("unchecked")
14: AttributeKey<Object> key = (AttributeKey<Object>) e.getKey();
15: channel.attr(key).set(e.getValue());
16: }
17: }
18:
19: ChannelPipeline p = channel.pipeline();
20:
21: // 记录当前的属性
22: final EventLoopGroup currentChildGroup = childGroup;
23: final ChannelHandler currentChildHandler = childHandler;
24: final Entry<ChannelOption<?>, Object>[] currentChildOptions;
25: final Entry<AttributeKey<?>, Object>[] currentChildAttrs;
26: synchronized (childOptions) {
27: currentChildOptions = childOptions.entrySet().toArray(newOptionArray(0));
28: }
29: synchronized (childAttrs) {
30: currentChildAttrs = childAttrs.entrySet().toArray(newAttrArray(0));
31: }
32:
33: // 添加 ChannelInitializer 对象到 pipeline 中，用于后续初始化 ChannelHandler 到 pipeline 中。
34: p.addLast(new ChannelInitializer<Channel>() {
35: @Override
36: public void initChannel(final Channel ch) throws Exception{
38: final ChannelPipeline pipeline = ch.pipeline();
39:
40: // 添加配置的 ChannelHandler 到 pipeline 中。
41: ChannelHandler handler = config.handler();
42: if (handler != null) {
43: pipeline.addLast(handler);
44: }
45:
46: // 添加 ServerBootstrapAcceptor 到 pipeline 中。
47: // 使用 EventLoop 执行的原因，参见 https://github.com/lightningMan/netty/commit/4638df20628a8987c8709f0f8e5f3679a914ce1a
48: ch.eventLoop().execute(new Runnable() {
49: @Override
50: public void run(){
52: pipeline.addLast(new ServerBootstrapAcceptor(
53: ch, currentChildGroup, currentChildHandler, currentChildOptions, currentChildAttrs));
54: }
55: });
56: }
57: });
58: }}
```

* 第 3 至 7 行：将启动器配置的可选项集合，调用/#setChannelOptions(channel, options, logger) 方法，设置到 Channel的可选项集合中。
* 第 9 至 17 行：将启动器配置的属性集合，设置到 Channel 的属性集合中。
* 第 21 至 31 行：记录启动器配置的**子 Channel**的属性，用于【第 52 至 53
  行】的代码，创建 ServerBootstrapAcceptor 对象。
* 第 34 至 57 行：创建 ChannelInitializer 对象，添加到 pipeline
  中，用于后续初始化 ChannelHandler 到 pipeline 中。

* 第 40 至 44 行：添加启动器配置的 ChannelHandler 到 pipeline 中。
* 第 46 至 55 行：创建 ServerBootstrapAcceptor 对象，添加到 pipeline
  中。为什么使用 EventLoop 执行**添加的过程**？如果启动器配置的处理器，并且
  ServerBootstrapAcceptor 不使用 EventLoop 添加，则会导致
  ServerBootstrapAcceptor 添加到配置的处理器之前。示例代码如下：

```java
ServerBootstrap b = new ServerBootstrap();
b.handler(new ChannelInitializer<Channel>() {
@Override
protected void initChannel(Channel ch){
final ChannelPipeline pipeline = ch.pipeline();
ch.eventLoop().execute(new Runnable() {
@Override
public void run(){
pipeline.addLast(new LoggingHandler(LogLevel.INFO));
}
});
}
});
```

* Netty 官方的提交，可见
  [github commit](https://github.com/lightningMan/netty/commit/4638df20628a8987c8709f0f8e5f3679a914ce1a)
  。
* ServerBootstrapAcceptor 也是一个 ChannelHandler
  实现类，用于接受客户端的连接请求。详细解析，见后续文章。
* 该 ChannelInitializer 的初始化的执行，在

AbstractChannel/#register0(ChannelPromise promise) 方法中触发执行。
* 那么为什么要使用 ChannelInitializer 进行处理器的初始化呢？而不直接添加到
  pipeline 中。例如修改为如下代码：

```
final Channel ch = channel;
final ChannelPipeline pipeline = ch.pipeline();
// 添加配置的 ChannelHandler 到 pipeline 中。
ChannelHandler handler = config.handler();
if (handler != null) {
pipeline.addLast(handler);
}
// 添加 ServerBootstrapAcceptor 到 pipeline 中。
// 使用 EventLoop 执行的原因，参见 https://github.com/lightningMan/netty/commit/4638df20628a8987c8709f0f8e5f3679a914ce1a
ch.eventLoop().execute(new Runnable() {
@Override
public void run(){
System.out.println(Thread.currentThread() + ": ServerBootstrapAcceptor");
pipeline.addLast(new ServerBootstrapAcceptor(
ch, currentChildGroup, currentChildHandler, currentChildOptions, currentChildAttrs));
}
});
```

* 因为此时 Channel 并未注册到 EventLoop 中。如果调用EventLoop/#execute(Runnable runnable) 方法，会抛出
Exception in thread "main" java.lang.IllegalStateException: channel not
registered to an event loop 异常。

# 参考

Netty
服务端启动涉及的流程非常多，所以有不理解的地方，胖友可以多多调试。在其中涉及到的
EventLoopGroup、EventLoop、Pipeline 等等组件，我们后在后续的文章，正式分享。

另外，也推荐如下和 Netty 服务端启动相关的文章，以加深理解：

* 闪电侠[《Netty 源码分析之服务端启动全解析》](https://www.jianshu.com/p/c5068caab217)
* 小明哥[《【死磕 Netty 】—— 服务端启动过程分析》](http://cmsblogs.com/?p=2470)
* 占小狼[《Netty 源码分析之服务启动》](https://www.jianshu.com/p/e577803f0fb8)
* 杨武兵[《Netty 源码分析系列 —— Bootstrap》](https://my.oschina.net/ywbrj042/blog/868798)
* 永顺[《Netty 源码分析之 一 揭开 Bootstrap 神秘的红盖头 (服务器端)》](https://segmentfault.com/a/1190000007283053)
