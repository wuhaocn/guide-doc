# NIO 基础（二）之 Channel

# 1. 概述

在 Java NIO 中，基本上所有的 IO 操作都是从 Channel 开始。数据可以从 Channel 读取到 Buffer 中，也可以从 Buffer 写到 Channel 中。如下图所示：

![Buffer <=> Channel](http://static2.iocoder.cn/images/Netty/2018_02_05/01.png) Buffer <=> Channel

# 2. NIO Channel 对比 Java Stream

NIO Channel **类似** Java Stream ，但又有几点不同：

1. 对于**同一个** Channel ，我们可以从它读取数据，也可以向它写入数据。而对于**同一个** Stream ，通畅要么只能读，要么只能写，二选一( 有些文章也描述成“单向”，也是这个意思 )。
2. Channel 可以**非阻塞**的读写 IO 操作，而 Stream 只能**阻塞**的读写 IO 操作。
3. Channel **必须配合** Buffer 使用，总是先读取到一个 Buffer 中，又或者是向一个 Buffer 写入。也就是说，我们无法绕过 Buffer ，直接向 Channel 写入数据。

# 3. Channel 的实现

Channel 在 Java 中，作为一个**接口**，java.nio.channels.Channel，定义了 IO 操作的**连接与关闭**。代码如下：
```java
public interface Channel extends Closeable {

    /**
     * 判断此通道是否处于打开状态。 
     */
    public boolean isOpen();

    /**
     *关闭此通道。
     */
    public void close() throws IOException;

}
```

Channel 有非常多的实现类，最为重要的**四个** Channel 实现类如下：

* SocketChannel ：一个客户端用来**发起** TCP 的 Channel 。
* ServerSocketChannel ：一个服务端用来**监听**新进来的连接的 TCP 的 Channel 。对于每一个新进来的连接，都会创建一个对应的 SocketChannel 。
* DatagramChannel ：通过 UDP 读写数据。
* FileChannel ：从文件中，读写数据。
因为 [《Java NIO 系列教程》](http://ifeve.com/java-nio-all/) 对上述的 Channel 解释的非常不错，我就直接引用啦。

我们在使用 Netty 时，主要使用 TCP 协议，所以胖友可以只看 [「3.2 SocketChannel」]() 和 [「3.1 ServerSocketChannel」]() 。

## 3.1 ServerSocketChannel

Java NIO中的 ServerSocketChannel 是一个可以监听新进来的TCP连接的通道, 就像标准IO中的ServerSocket一样。ServerSocketChannel类在 java.nio.channels包中。

这里有个例子：
```java

ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
serverSocketChannel.socket().bind(new InetSocketAddress(9999));
while(true){
    SocketChannel socketChannel = serverSocketChannel.accept();
    //do something with socketChannel...
}

```

* 打开 ServerSocketChannel
通过调用 ServerSocketChannel.open() 方法来打开ServerSocketChannel.如：
ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();


* 关闭 ServerSocketChannel
通过调用ServerSocketChannel.close() 方法来关闭ServerSocketChannel. 如：
serverSocketChannel.close();

* 监听新进来的连接
通过 ServerSocketChannel.accept() 方法监听新进来的连接。当 accept()方法返回的时候,它返回一个包含新进来的连接的 SocketChannel。
因此, accept()方法会一直阻塞到有新连接到达。通常不会仅仅只监听一个连接,在while循环中调用 accept()方法. 如下面的例子：

```java

while(true){
    SocketChannel socketChannel = serverSocketChannel.accept();
    //do something with socketChannel...
}

```

当然,也可以在while循环中使用除了true以外的其它退出准则。

非阻塞模式
ServerSocketChannel可以设置成非阻塞模式。在非阻塞模式下，accept() 方法会立刻返回，如果还没有新进来的连接,返回的将是null。 
因此，需要检查返回的SocketChannel是否是null.如：

```java

ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
serverSocketChannel.socket().bind(new InetSocketAddress(9999));
serverSocketChannel.configureBlocking(false);
while(true){
    SocketChannel socketChannel = serverSocketChannel.accept();
    if(socketChannel != null){
        //do something with socketChannel...
    }
}
```

[《Java NIO系列教程（九） ServerSocketChannel》](http://ifeve.com/server-socket-channel/)

## 3.2 SocketChannel

Java NIO中的SocketChannel是一个连接到TCP网络套接字的通道。可以通过以下2种方式创建SocketChannel：

打开一个SocketChannel并连接到互联网上的某台服务器。
一个新连接到达ServerSocketChannel时，会创建一个SocketChannel。

* 打开 SocketChannel, 下面是SocketChannel的打开方式：
```java
SocketChannel socketChannel = SocketChannel.open();
socketChannel.connect(new InetSocketAddress("http://jenkov.com", 80));

```
* 关闭 SocketChannel
当用完SocketChannel之后调用SocketChannel.close()关闭SocketChannel：

```java

socketChannel.close();

```
* 从 SocketChannel 读取数据
要从SocketChannel中读取数据，调用一个read()的方法之一。以下是例子：
```java
ByteBuffer buf = ByteBuffer.allocate(48);
int bytesRead = socketChannel.read(buf);

```
首先，分配一个Buffer。从SocketChannel读取到的数据将会放到这个Buffer中。

然后，调用SocketChannel.read()。该方法将数据从SocketChannel 读到Buffer中。read()方法返回的int值表示读了多少字节进Buffer里。
如果返回的是-1，表示已经读到了流的末尾（连接关闭了）。

* 写入 SocketChannel
写数据到SocketChannel用的是SocketChannel.write()方法，该方法以一个Buffer作为参数。示例如下：

```java

String newData = "New String to write to file..." + System.currentTimeMillis();
ByteBuffer buf = ByteBuffer.allocate(48);
buf.clear();
buf.put(newData.getBytes());
buf.flip();
while(buf.hasRemaining()) {
    channel.write(buf);
}

```

注意SocketChannel.write()方法的调用是在一个while循环中的。Write()方法无法保证能写多少字节到SocketChannel。
所以，我们重复调用write()直到Buffer没有要写的字节为止。

非阻塞模式
可以设置 SocketChannel 为非阻塞模式（non-blocking mode）.设置之后，就可以在异步模式下调用connect(), read() 和write()了。

* connect()
 如果SocketChannel在非阻塞模式下，此时调用connect()，该方法可能在连接建立之前就返回了。为了确定连接是否建立，可以调用finishConnect()的方法。像这样：


```java

socketChannel.configureBlocking(false);
socketChannel.connect(new InetSocketAddress("http://jenkov.com", 80));
while(! socketChannel.finishConnect() ){
    //wait, or do something else...
}

```
* write()
 非阻塞模式下，write()方法在尚未写出任何内容时可能就返回了。所以需要在循环中调用write()。前面已经有例子了，这里就不赘述了。

* read()
非阻塞模式下,read()方法在尚未读取到任何数据时可能就返回了。所以需要关注它的int返回值，它会告诉你读取了多少字节。

* 非阻塞模式与选择器
非阻塞模式与选择器搭配会工作的更好，通过将一或多个SocketChannel注册到Selector，可以询问选择器哪个通道已经准备好了读取，写入等。Selector与SocketChannel的搭配使用会在后面详讲。


[《Java NIO 系列教程（八） SocketChannel》](http://ifeve.com/socket-channel/)

## 3.3 DatagramChannel

Java NIO中的DatagramChannel是一个能收发UDP包的通道。因为UDP是无连接的网络协议，所以不能像其它通道那样读取和写入。它发送和接收的是数据包。

* 打开 DatagramChannel,下面是 DatagramChannel 的打开方式：
```java
DatagramChannel channel = DatagramChannel.open();
channel.socket().bind(new InetSocketAddress(9999));
```
这个例子打开的 DatagramChannel可以在UDP端口9999上接收数据包。

* 接收数据, 通过receive()方法从DatagramChannel接收数据，如：
```java
ByteBuffer buf = ByteBuffer.allocate(48);
buf.clear();
channel.receive(buf);
```
receive()方法会将接收到的数据包内容复制到指定的Buffer. 如果Buffer容不下收到的数据，多出的数据将被丢弃。

* 发送数据

发送数据通过send()方法从DatagramChannel发送数据，如:

```java
String newData = "New String to write to file..." + System.currentTimeMillis();
ByteBuffer buf = ByteBuffer.allocate(48);
buf.clear();
buf.put(newData.getBytes());
buf.flip();
int bytesSent = channel.send(buf, new InetSocketAddress("jenkov.com", 80));
```
这个例子发送一串字符到”jenkov.com”服务器的UDP端口80。 因为服务端并没有监控这个端口，所以什么也不会发生。也不会通知你发出的数据包是否已收到，因为UDP在数据传送方面没有任何保证。

连接到特定的地址可以将DatagramChannel“连接”到网络中的特定地址的。由于UDP是无连接的，连接到特定地址并不会像TCP通道那样创建一个真正的连接。而是锁住DatagramChannel ，让其只能从特定地址收发数据。

这里有个例子:
```java
channel.connect(new InetSocketAddress("jenkov.com", 80));
```
当连接后，也可以使用read()和write()方法，就像在用传统的通道一样。只是在数据传送方面没有任何保证。这里有几个例子：

```java
int bytesRead = channel.read(buf);
int bytesWritten = channel.write(but);
```
[《Java NIO系列教程（十） Java NIO DatagramChannel》](http://ifeve.com/datagram-channel/)

## 3.4 FileChannel

Java NIO中的FileChannel是一个连接到文件的通道。可以通过文件通道读写文件。

FileChannel无法设置为非阻塞模式，它总是运行在阻塞模式下。

* 打开FileChannel
在使用FileChannel之前，必须先打开它。但是，我们无法直接打开一个FileChannel，需要通过使用一个InputStream、OutputStream或RandomAccessFile来获取一个FileChannel实例。下面是通过RandomAccessFile打开FileChannel的示例：

```java
RandomAccessFile aFile = new RandomAccessFile("data/nio-data.txt", "rw");

FileChannel inChannel = aFile.getChannel();
```

* 从FileChannel读取数据
调用多个read()方法之一从FileChannel中读取数据。如：

```java
ByteBuffer buf = ByteBuffer.allocate(48);
int bytesRead = inChannel.read(buf);
```

首先，分配一个Buffer。从FileChannel中读取的数据将被读到Buffer中。
然后，调用FileChannel.read()方法。该方法将数据从FileChannel读取到Buffer中。read()方法返回的int值表示了有多少字节被读到了Buffer中。如果返回-1，表示到了文件末尾。

* 向FileChannel写数据
使用FileChannel.write()方法向FileChannel写数据，该方法的参数是一个Buffer。如：

```java
String newData = "New String to write to file..." + System.currentTimeMillis();
ByteBuffer buf = ByteBuffer.allocate(48);
buf.clear();
buf.put(newData.getBytes());
buf.flip();
while(buf.hasRemaining()) {
    channel.write(buf);
}
```

注意FileChannel.write()是在while循环中调用的。因为无法保证write()方法一次能向FileChannel写入多少字节，因此需要重复调用write()方法，直到Buffer中已经没有尚未写入通道的字节。

* 关闭FileChannel
用完FileChannel后必须将其关闭。如：

```java
channel.close();
```
* FileChannel的position方法
有时可能需要在FileChannel的某个特定位置进行数据的读/写操作。可以通过调用position()方法获取FileChannel的当前位置。

也可以通过调用position(long pos)方法设置FileChannel的当前位置。

这里有两个例子:

```java

long pos = channel.position();
channel.position(pos +123);

```
如果将位置设置在文件结束符之后，然后试图从文件通道中读取数据，读方法将返回-1 —— 文件结束标志。
如果将位置设置在文件结束符之后，然后向通道中写数据，文件将撑大到当前位置并写入数据。
这可能导致“文件空洞”，磁盘上物理文件中写入的数据间有空隙。

* FileChannel的size方法
FileChannel实例的size()方法将返回该实例所关联文件的大小。如:

```java

long fileSize = channel.size();

```
* FileChannel的truncate方法
可以使用FileChannel.truncate()方法截取一个文件。截取文件时，文件将中指定长度后面的部分将被删除。如：

```java
channel.truncate(1024);
```
这个例子截取文件的前1024个字节。

* FileChannel的force方法

FileChannel.force()方法将通道里尚未写入磁盘的数据强制写到磁盘上。出于性能方面的考虑，
操作系统会将数据缓存在内存中，所以无法保证写入到FileChannel里的数据一定会即时写到磁盘上。
要保证这一点，需要调用force()方法。
force()方法有一个boolean类型的参数，指明是否同时将文件元数据（权限信息等）写到磁盘上。

下面的例子同时将文件数据和元数据强制写到磁盘上：

```java

channel.force(true);

```
[《Java NIO系列教程（七） FileChannel》](http://ifeve.com/file-channel/)


参考文章如下：

* [《Java NIO 系列教程（二） Channel》](http://ifeve.com/channels/)