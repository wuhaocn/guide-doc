# NIO 基础（五）之示例

# 1. 概述

在前面的四篇文章，我们已经对 NIO 的概念已经有了一定的了解。当然，胖友也可能和我一样，已经被一堆概念烦死了。

那么本文，我们撸起袖子，就是干代码，不瞎比比了。

当然，下面更多的是提供一个 NIO 示例。真正生产级的 NIO 代码，建议胖友重新写，或者直接使用 Netty 。

代码仓库在 [example/yunai/nio](https://github.com/YunaiV/netty/tree/f7016330f1483021ef1c38e0923e1c8b7cef0d10/example/src/main/java/io/netty/example/yunai/nio) 目录下。一共 3 个类：

* NioServer ：NIO 服务端。
* NioClient ：NIO 客户端。
* CodecUtil ：消息编解码工具类。

# 2. 服务端

```java
package io.netty.example.yunai.nio;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NioServer {

    private ServerSocketChannel serverSocketChannel;
    private Selector selector;

    public NioServer() throws IOException {
        // 打开 Server Socket Channel
        serverSocketChannel = ServerSocketChannel.open();
        // 配置为非阻塞
        serverSocketChannel.configureBlocking(false);
        // 绑定 Server port
        serverSocketChannel.socket().bind(new InetSocketAddress(8080));
        // 创建 Selector
        selector = Selector.open();
        // 注册 Server Socket Channel 到 Selector
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("Server 启动完成");

        handleKeys();
    }

    @SuppressWarnings("Duplicates")
    private void handleKeys() throws IOException {
        while (true) {
            // 通过 Selector 选择 Channel
            int selectNums = selector.select(30 * 1000L);
            if (selectNums == 0) {
                continue;
            }
            System.out.println("选择 Channel 数量：" + selectNums);

            // 遍历可选择的 Channel 的 SelectionKey 集合
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove(); // 移除下面要处理的 SelectionKey
                if (!key.isValid()) { // 忽略无效的 SelectionKey
                    continue;
                }

                handleKey(key);
            }
        }
    }

    private void handleKey(SelectionKey key) throws IOException {
        // 接受连接就绪
        if (key.isAcceptable()) {
            handleAcceptableKey(key);
        }
        // 读就绪
        if (key.isReadable()) {
            handleReadableKey(key);
        }
        // 写就绪
        if (key.isWritable()) {
            handleWritableKey(key);
        }
    }

    private void handleAcceptableKey(SelectionKey key) throws IOException {
        // 接受 Client Socket Channel
        SocketChannel clientSocketChannel = ((ServerSocketChannel) key.channel()).accept();
        // 配置为非阻塞
        clientSocketChannel.configureBlocking(false);
        // log
        System.out.println("接受新的 Channel");
        // 注册 Client Socket Channel 到 Selector
        clientSocketChannel.register(selector, SelectionKey.OP_READ, new ArrayList<String>());
    }

    private void handleReadableKey(SelectionKey key) throws IOException {
        // Client Socket Channel
        SocketChannel clientSocketChannel = (SocketChannel) key.channel();
        // 读取数据
        ByteBuffer readBuffer = CodecUtil.read(clientSocketChannel);
        // 处理连接已经断开的情况
        if (readBuffer == null) {
            System.out.println("断开 Channel");
            clientSocketChannel.register(selector, 0);
            return;
        }
        // 打印数据
        if (readBuffer.position() > 0) { // 写入模式下，
            String content = CodecUtil.newString(readBuffer);
            System.out.println("读取数据：" + content);

            // 添加到响应队列
            List<String> responseQueue = (ArrayList<String>) key.attachment();
            responseQueue.add("响应：" + content);
            // 注册 Client Socket Channel 到 Selector
            clientSocketChannel.register(selector, SelectionKey.OP_WRITE, key.attachment());
        }
    }

    @SuppressWarnings("Duplicates")
    private void handleWritableKey(SelectionKey key) throws ClosedChannelException {
        // Client Socket Channel
        SocketChannel clientSocketChannel = (SocketChannel) key.channel();

        // 遍历响应队列
        List<String> responseQueue = (ArrayList<String>) key.attachment();
        for (String content : responseQueue) {
            // 打印数据
            System.out.println("写入数据：" + content);
            // 返回
            CodecUtil.write(clientSocketChannel, content);
        }
        responseQueue.clear();

        // 注册 Client Socket Channel 到 Selector
        clientSocketChannel.register(selector, SelectionKey.OP_READ, responseQueue);
    }

    public static void main(String[] args) throws IOException {
        NioServer server = new NioServer();
    }

}
```

整块代码我们可以分成 3 部分：

* 构造方法：初始化 NIO 服务端。
* /#handleKeys()方法：基于 Selector 处理 IO 操作。
* /#main(String[] args)方法：创建 NIO 服务端。
下面，我们逐小节来分享。

## 2.1 构造方法

对应【第 3 至 20 行】的代码。
* serverSocketChannel属性，服务端的 ServerSocketChannel ，在【第 7 至 12 行】的代码进行初始化，
重点是此处启动了服务端，并监听指定端口( 此处为 8080 )。
* selector属性，选择器，在【第 14 至 16 行】的代码进行初始化，重点是此处将serverSocketChannel到
selector中，并对SelectionKey.OP_ACCEPT事件感兴趣。这样子，在客户端连接服务端时，我们就可以处理该 IO 事件。
* 第 19 行：调用/#handleKeys()方法，基于 Selector 处理 IO 事件。

## 2.2 handleKeys

对应【第 22 至 43 行】的代码。
* 第 23 行：死循环。本文的示例，不考虑服务端关闭的逻辑。
* 第 24 至 29 行：调用

Selector/#select(long timeout)方法，每 30 秒阻塞等待有就绪的 IO 事件。
此处的 30 秒为笔者随意写的，实际也可以改成其他超时时间，
或者Selector/#select()方法。当不存在就绪的 IO 事件，直接continue，继续下一次阻塞等待。
* 第 32 行：调用Selector/#selectedKeys()方法，获得有就绪的 IO 事件( 也可以称为“选择的” ) Channel 对应的 SelectionKey 集合。
* 第 33 行 至 35 行：遍历iterator，进行逐个 SelectionKey 处理。重点注意下，处理完需要进行移除，
具体原因，在 [《精尽 Netty 源码分析 —— NIO 基础（四）之 Selector》「10. 简单 Selector 示例」](http://svip.iocoder.cn/Netty/nio-4-selector/#10-%E7%AE%80%E5%8D%95-Selector-%E7%A4%BA%E4%BE%8B) 有详细解析。
* 第 36 至 38 行：在遍历的过程中，可能该 SelectionKey 已经**失效**，直接continue，不进行处理。
* 第 40 行：调用/#handleKey()方法，逐个 SelectionKey 处理。

### 2.2.1 handleKey

对应【第 45 至 58 行】的代码。

* 通过调用 SelectionKey 的/#isAcceptable()、/#isReadable()、/#isWritable()
方法，**分别**判断 Channel 是**接受连接**就绪，还是**读**就绪，或是**写**就绪，并调用相应的
/#handleXXXX(SelectionKey key)
方法，处理对应的 IO 事件。
* 因为 SelectionKey 可以**同时**对**一个** Channel 的**多个**事件感兴趣，所以此处的代码都是
if判断，而不是if else判断。虽然，考虑到让示例更简单，本文的并未编写同时对一个 Channel 的多个事件感兴趣，后续我们会在 Netty 的源码解析中看到。
* SelectionKey.OP_CONNECT使用在**客户端**中，所以此处不需要做相应的判断和处理。

### 2.2.2 handleAcceptableKey

对应【第 60 至 69 行】的代码。

* 第 62 行：调用ServerSocketChannel/#accept()方法，获得连接的客户端的 SocketChannel 。
* 第 64 行：配置客户端的 SocketChannel 为非阻塞，否则无法使用 Selector 。
* 第 66 行：打印日志，方便调试。实际场景下，使用 Logger 而不要使用

System.out进行输出。
* 第 68 行：注册客户端的 SocketChannel 到selector中，并对SelectionKey.OP_READ
事件感兴趣。这样子，在客户端发送消息( 数据 )到服务端时，我们就可以处理该 IO 事件。

* 为什么不对

SelectionKey.OP_WRITE事件感兴趣呢？因为这个时候，服务端一般不会主动向客户端发送消息，所以不需要对
SelectionKey.OP_WRITE事件感兴趣。
* 细心的胖友会发现，
Channel/#register(Selector selector, int ops, Object attachment)
方法的第 3 个参数，我们注册了 SelectionKey 的
attachment属性为new ArrayList<String>()，这又是为什么呢？结合下面的
/#handleReadableKey(Selection key)方法，我们一起解析。

### 2.2.3 handleReadableKey

对应【第 71 至 93 行】的代码。

* 第 73 行：调用SelectionKey/#channel()方法，获得该 SelectionKey 对应的 SocketChannel ，即客户端的 SocketChannel 。
* 第 75 行：调用CodecUtil/#read(SocketChannel channel)
方法，读取数据。具体代码如下：
```java
// CodecUtil.java
public static ByteBuffer read(SocketChannel channel){
// 注意，不考虑拆包的处理
    ByteBuffer buffer = ByteBuffer.allocate(1024);
    try {
        int count = channel.read(buffer);
        if (count == -1) {
        return null;
    }
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
    return buffer;
}
```

* 考虑到示例的简单性，数据的读取，就不考虑拆包的处理。不理解的胖友，可以自己 Google 下。
* 调用SocketChannel/#read(ByteBuffer)方法，读取 Channel 的缓冲区的数据到 ByteBuffer 中。
  若返回的结果(count) 为 -1 ，意味着客户端连接已经断开，我们直接返回null。为什么是返回null呢？下面继续见分晓。
* 第 76 至 81 行：若读取数据返回的结果为null时，意味着客户端的连接已经断开，因此取消注册selector
  对该客户端的 SocketChannel 的感兴趣的 IO 事件。通过调用注册方法，并且第 2 个参数
    ops为 0 ，可以达到取消注册的效果。😈 感兴趣的胖友，可以将这行代码进行注释，测试下效果就很容易明白了。
* 第 83 行：通过调用
  ByteBuffer/#position()大于 0 ，来判断**实际**读取到数据。

* 第 84 至 85 行：调用CodecUtil/#newString(ByteBuffer)方法，格式化为字符串，并进行打印。代码如下：
```java
// CodecUtil.java
public static String newString(ByteBuffer buffer){
    buffer.flip();
    byte[] bytes = new byte[buffer.remaining()];
    System.arraycopy(buffer.array(), buffer.position(), bytes, 0, buffer.remaining());
    try {
        return new String(bytes, "UTF-8");
    } catch (UnsupportedEncodingException e) {
        throw new RuntimeException(e);
    }
}
```

* 注意，需要调用

ByteBuffer/#flip()
方法，将 ByteBuffer 从**写**模式切换到**读**模式。
* 第 86 行：一般在此处，我们可以进行一些业务逻辑的处理，并返回处理的相应结果。例如，我们熟悉的 Request / Response 的处理。
当然，考虑到性能，我们甚至可以将逻辑的处理，丢到逻辑线程池。

* 😈 如果不理解，木有关系，在 [《精尽 Dubbo 源码分析 —— NIO 服务器（二）之 Transport 层》「8. Dispacher」](http://svip.iocoder.cn/Dubbo/remoting-api-transport/) 中，有详细解析。
* 🙂 考虑到示例的简洁性，所以在【第 88 至 89 行】的代码中，我们直接返回（"响应："+ 请求内容）给客户端。
* 第 88 行：通过调用

SelectionKey/#attachment()
方法，获得我们**附加**在 SelectionKey 的响应队列(responseQueue)。可能有胖友会问啦，为什么不调用

SocketChannel/#write(ByteBuf)方法，直接写数据给客户端呢？虽然大多数情况下，SocketChannel 都是**可写**的，但是如果写入比较频繁，超过 SocketChannel 的缓存区大小，就会导致数据“**丢失**”，并未写给客户端。

* 所以，此处笔者在示例中，处理的方式为添加响应数据到responseQueue中，
并在【第 91 行】的代码中，注册客户端的 SocketChannel 到selector中，并对SelectionKey.OP_WRITE事件感兴趣。
这样子，在 SocketChannel **写就绪**时，在/#handleWritableKey(SelectionKey key)方法中，统一处理写数据给客户端。
* 当然，还是因为是示例，所以这样的实现方式不是最优。
在 Netty 中，具体的实现方式是，先尝试调用SocketChannel/#write(ByteBuf)方法，写数据给客户端。
若写入失败( 方法返回结果为 0 )时，再进行类似笔者的上述实现方式。牛逼！Netty ！
* 如果不太理解分享的原因，可以再阅读如下两篇文章：

* [《深夜对话：NIO 中 SelectionKey.OP_WRITE 你了解多少》](https://mp.weixin.qq.com/s/V4tEH1j64FHFmB8bReNI7g)
* [《Java.nio 中 socketChannle.write() 返回 0 的简易解决方案》](https://blog.csdn.net/a34140974/article/details/48464845)
* 第 91 行：有一点需要注意，

Channel/#register(Selector selector, int ops, Object attachment)
方法的第 3 个参数，需要继续传入响应队列(responseQueue)，因为每次注册生成**新**的 SelectionKey 。若不传入，下面的

/#handleWritableKey(SelectionKey key)
方法，会获得不到响应队列(responseQueue)。

### 2.2.4 handleWritableKey

对应【第 96 至 112 行】的代码。

* 第 98 行：调用SelectionKey/#channel()方法，获得该 SelectionKey 对应的 SocketChannel ，即客户端的 SocketChannel 。
* 第 101 行：通过调用SelectionKey/#attachment()方法，获得我们**附加**在 SelectionKey 的响应队列(responseQueue)。

* 第 102 行：遍历响应队列。
* 第 106 行：调用CodeUtil/#write(SocketChannel, content)方法，写入响应数据给客户端。代码如下：
```java
// CodecUtil.java
public static void write(SocketChannel channel, String content){
    // 写入 Buffer
    ByteBuffer buffer = ByteBuffer.allocate(1024);
    try {
        buffer.put(content.getBytes("UTF-8"));
    } catch (UnsupportedEncodingException e) {
        throw new RuntimeException(e);
    }
    // 写入 Channel
    buffer.flip();
    try {
    // 注意，不考虑写入超过 Channel 缓存区上限。
        channel.write(buffer);
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
}
```

* 代码比较简单，**还是要注意**，需要调用ByteBuffer/#flip()方法，将 ByteBuffer 从**写**模式切换到**读**模式。
* 第 111 行：**注意**，再结束写入后，需要**重新**注册客户端的 SocketChannel 到selector中，并对

SelectionKey.OP_READ事件感兴趣。为什么呢？其实还是我们在上文中提到的，大多数情况下，SocketChannel **都是写就绪的**，如果不取消掉注册掉对

SelectionKey.OP_READ事件感兴趣，就会导致反复触发无用的写事件处理。😈 感兴趣的胖友，可以将这行代码进行注释，测试下效果就很容易明白了。

## 2.3 main

对应【第 114 至 116 行】

* 比较简单，就是创建一个 NioServer 对象。我们可以直接通过telnet 127.0.0.1 8080的方式，连接服务端，进行读写数据的测试。

# 3. 客户端

客户端的实现代码，绝大数和服务端相同，所以我们分析的相对会简略一些。不然，自己都嫌弃自己太啰嗦了。
```java
package io.netty.example.yunai.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class NioClient {

    private SocketChannel clientSocketChannel;
    private Selector selector;
    private final List<String> responseQueue = new ArrayList<String>();

    private CountDownLatch connected = new CountDownLatch(1);

    public NioClient() throws IOException, InterruptedException {
        // 打开 Client Socket Channel
        clientSocketChannel = SocketChannel.open();
        // 配置为非阻塞
        clientSocketChannel.configureBlocking(false);
        // 创建 Selector
        selector = Selector.open();
        // 注册 Server Socket Channel 到 Selector
        clientSocketChannel.register(selector, SelectionKey.OP_CONNECT);
        // 连接服务器
        clientSocketChannel.connect(new InetSocketAddress(8080));

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    handleKeys();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        if (connected.getCount() != 0) {
            connected.await();
        }
        System.out.println("Client 启动完成");
    }

    @SuppressWarnings("Duplicates")
    private void handleKeys() throws IOException {
        while (true) {
            // 通过 Selector 选择 Channel
            int selectNums = selector.select(30 * 1000L);
            if (selectNums == 0) {
                continue;
            }

            // 遍历可选择的 Channel 的 SelectionKey 集合
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove(); // 移除下面要处理的 SelectionKey
                if (!key.isValid()) { // 忽略无效的 SelectionKey
                    continue;
                }

                handleKey(key);
            }
        }
    }

    private synchronized void handleKey(SelectionKey key) throws IOException {
        // 接受连接就绪
        if (key.isConnectable()) {
            handleConnectableKey(key);
        }
        // 读就绪
        if (key.isReadable()) {
            handleReadableKey(key);
        }
        // 写就绪
        if (key.isWritable()) {
            handleWritableKey(key);
        }
    }

    private void handleConnectableKey(SelectionKey key) throws IOException {
        // 完成连接
        if (!clientSocketChannel.isConnectionPending()) {
            return;
        }
        clientSocketChannel.finishConnect();
        // log
        System.out.println("接受新的 Channel");
        // 注册 Client Socket Channel 到 Selector
        clientSocketChannel.register(selector, SelectionKey.OP_READ, responseQueue);
        // 标记为已连接
        connected.countDown();
    }

    @SuppressWarnings("Duplicates")
    private void handleReadableKey(SelectionKey key) throws ClosedChannelException {
        // Client Socket Channel
        SocketChannel clientSocketChannel = (SocketChannel) key.channel();
        // 读取数据
        ByteBuffer readBuffer = CodecUtil.read(clientSocketChannel);
        // 打印数据
        if (readBuffer.position() > 0) { // 写入模式下，
            String content = CodecUtil.newString(readBuffer);
            System.out.println("读取数据：" + content);
        }
    }

    @SuppressWarnings("Duplicates")
    private void handleWritableKey(SelectionKey key) throws ClosedChannelException {
        // Client Socket Channel
        SocketChannel clientSocketChannel = (SocketChannel) key.channel();

        // 遍历响应队列
        List<String> responseQueue = (ArrayList<String>) key.attachment();
        for (String content : responseQueue) {
            // 打印数据
            System.out.println("写入数据：" + content);
            // 返回
            CodecUtil.write(clientSocketChannel, content);
        }
        responseQueue.clear();

        // 注册 Client Socket Channel 到 Selector
        clientSocketChannel.register(selector, SelectionKey.OP_READ, responseQueue);
    }

    public synchronized void send(String content) throws ClosedChannelException {
        // 添加到响应队列
        responseQueue.add(content);
        // 打印数据
        System.out.println("写入数据：" + content);
        // 注册 Client Socket Channel 到 Selector
        clientSocketChannel.register(selector, SelectionKey.OP_WRITE, responseQueue);
        selector.wakeup();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        NioClient client = new NioClient();
        for (int i = 0; i < 30; i++) {
            client.send("nihao: " + i);
            Thread.sleep(1000L);
        }
    }

}
```

整块代码我们可以分成 3 部分：

* 构造方法：初始化 NIO 客户端。
* /#handleKeys()方法：基于 Selector 处理 IO 操作。
* /#main(String[] args)方法：创建 NIO 客户端，并向服务器发送请求数据。

下面，我们逐小节来分享。

## 3.1 构造方法

对应【第 3 至 36 行】的代码。

* clientSocketChannel属性，客户端的 SocketChannel ，在【第 9 至 13 行】和【第 19 行】的代码进行初始化，重点是此处连接了指定服务端。
* selector属性，选择器，在【第 14 至 17 行】的代码进行初始化，重点是此处将clientSocketChannel到selector中，并对SelectionKey.OP_CONNECT事件感兴趣。
  这样子，在客户端连接服务端**成功**时，我们就可以处理该 IO 事件。
* responseQueue属性，直接声明为 NioClient 的成员变量，是为了方便/#send(String content)方法的实现。
* 第 21 至 30 行：
调用/#handleKeys()方法，基于 Selector 处理 IO 事件。比较特殊的是，我们是启动了一个**线程**进行处理。因为在后续的
/#main()方法中，我们需要调用发送请求数据的方法，不能直接在**主线程**，轮询处理 IO 事件。
😈 机智的胖友，可能已经发现，NioServer 严格来说，也是应该这样处理。
* 第 32 至 34 行：通过 CountDownLatch 来实现阻塞等待客户端成功连接上服务端。
具体的CountDownLatch/#countDown()方法，在
/#handleConnectableKey(SelectionKey key)
方法中调用。当然，除了可以使用 CountDownLatch 来实现阻塞等待，还可以通过如下方式:

* Object 的 wait 和 notify 的方式。
* Lock 的 await 和 notify 的方式。
* Queue 的阻塞等待方式。
* 😈 开心就好，皮一下很开心。

## 3.2 handleKeys

对应【第 38 至 59 行】的代码。
**完全**和 NioServer 中的该方法一模一样，省略。

### 3.2.1 handleKey

对应【第 61 至 74 行】的代码。

**大体**逻辑和 NioServer 中的该方法一模一样，差别将对

SelectionKey.OP_WRITE事件的处理改成对

SelectionKey.OP_CONNECT事件的处理。

### 3.3.2 handleConnectableKey

对应【第 76 至 88 行】的代码。

* 第 77 至 81 行：判断客户端的 SocketChannel 上是否**正在进行连接**的操作，若是，则完成连接。
* 第 83 行：打印日志。
* 第 85 行：注册客户端的 SocketChannel 到selector中，并对
SelectionKey.OP_READ事件感兴趣。这样子，在客户端接收到到服务端的消息( 数据 )时，我们就可以处理该 IO 事件。
* 第 87 行：调用CountDownLatch/#countDown()方法，结束 NioClient 构造方法中的【第 32 至 34 行】的阻塞等待连接完成。

### 3.3.3 handleReadableKey

对应【第 91 至 101 行】的代码。**大体**逻辑和 NioServer 中的该方法一模一样，**去掉响应请求的相关逻辑**。
😈 如果不去掉，就是客户端和服务端互发消息的“死循环”了。

### 3.3.4 handleWritableKey

对应【第 103 至 120 行】的代码。

**完全**和 NioServer 中的该方法一模一样。

## 3.3 send

对应【第 122 至 130 行】的代码。客户端发送请求消息给服务端。

* 第 124 行：添加到响应队列(responseQueue) 中。
* 第 126 行：打印日志。
* 第 128 行：注册客户端的 SocketChannel 到selector中，并对SelectionKey.OP_WRITE事件感兴趣。
  具体的原因，和 NioServer 的/#handleReadableKey(SelectionKey key)方法的【第 88 行】一样。
* 第 129 行：调用Selector/#wakeup()方法，唤醒/#handleKeys()方法中，Selector/#select(long timeout)方法的阻塞等待。
* 因为，在Selector#select(long timeout)方法的实现中，是以调用**当时**，对 SocketChannel 的感兴趣的事件 。
* 所以，在【第 128 行】的代码中，即使修改了对 SocketChannel 的感兴趣的事件，
  也不会结束Selector#select(long timeout)方法的阻塞等待。因此，需要进行唤醒操作。


## 3.4 main

对应【第 132 至 137 行】的代码。

* 第 133 行：创建一个 NioClient 对象。
* 第 134 至 137 行：每秒发送一次请求。考虑到代码没有处理拆包的逻辑，所以增加了间隔 1 秒的 sleep 。

# 推荐阅读文章如下：

* [《【NIO系列】—— Reactor 模式》](https://mp.weixin.qq.com/s/GpeaNowZKo1plaES9oxZ7g)
* [《lanux/java-demo/nio/example》](https://github.com/lanux/java-demo/tree/5b29c4b0d0056578a6eaa847e0d1efc9e42e48a4/src/main/java/com/lanux/io/nio)