### Java 中使用 SCTP 通信的例子

本人理解为:

- 1.构建于 IP 协议之上,与 TCP,UDP 平级. 但提供了与 TCP 一样的可靠性传输

- 2.基于消息的发送(而不是像 TCP 按可能的非完整字节发送,有过做过断包,粘包处理经验的应该能理解). 即上层应用程序发送的消息在接收端能完整接收到,SCTP 已经自动帮我们进行了消息字节流的完整组装

- 3.接收端多 IP+端口号支持. 提供一组 IP 和相同端口给 SCTP 发送端,当接收端的某个 IP 无法接收消息时,SCTP 自动选择另外的有效 IP 发送,达到容错目的.以前这种需要应用层处理的复杂业务现由传输层提供了

- 对于 SCTP 的更多分析参见;

http://www.cnblogs.com/qlee/archive/2011/07/13/2105717.html

- 本例子使用官方提供的例子:

http://www.oracle.com/technetwork/articles/javase/index-139946.html

注意: 文中列出的所有网址,如果访问不了,请翻墙.....

#### 测试环境

- SCTP 由操作系统实现,但目前 Windows7(Window8 和 Windows10 未做测试),Mac OS X10.10.3 都没实现, 所以本次测试的运行环境为:

```
Centos6.5 64位
jdk1.8.0_40
(默认也没实现), 需要安装SCTP内核包.
```

- 安装方式:

```
http://lksctp.sourceforge.net/(或http://sourceforge.net/projects/lksctp/files/lksctp-tools/lksctp-tools-1.0.16.tar.gz/download) 提供了SCTP在linux中的实现模块

下载完毕后解压. 切换到解压后的目录

运行bootstrap(./bootstrap). 会自动生成configure

运行configure并设置参数(./configure --prefix=/usr/local), 再执行 make && make install

安装完毕后, 拷贝 /usr/local/lib/libsctp.so.1 到 /usr/java/$JAVA_HOME/lib/amd64/ 中. 如果不将此文件拷贝过去,启动时会报如下错误:

java.lang.UnsupportedOperationException: libsctp.so.1: cannot open shared object file: No such file or directory

还要临时禁用一下selinux, 输入 echo 0 > /selinux/enforce 如果不禁用,启动时会报如下错误:
java.net.SocketException: Permission denied
at sun.nio.ch.Net.localInetAddress(Native Method)
对于该错误可参见 https://bugs.openjdk.java.net/browse/JDK-7045222

对于selinux可参见 http://cnzhx.net/blog/turn-off-selinux/
```

其中要引入 rt.jar 包,java 的 sctp 类在里面.
具体代码如下:

- DaytimeServer.java

```
package com.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import com.sun.nio.sctp.MessageInfo;
import com.sun.nio.sctp.SctpChannel;
import com.sun.nio.sctp.SctpServerChannel;

public class DaytimeServer {
    static int SERVER_PORT = 3456;
    static int US_STREAM = 0;
    static int FR_STREAM = 1;
    static SimpleDateFormat USformatter = new SimpleDateFormat("h:mm:ss a EEE d MMM yy, zzzz", Locale.US);
    static SimpleDateFormat FRformatter = new SimpleDateFormat("h:mm:ss a EEE d MMM yy, zzzz", Locale.FRENCH);

    public static void main(String[] args) throws IOException {
        SctpServerChannel ssc = SctpServerChannel.open();
        InetSocketAddress serverAddr = new InetSocketAddress("192.168.52.199",SERVER_PORT);
        ssc.bind(serverAddr);

        ByteBuffer buf = ByteBuffer.allocateDirect(60);
        CharBuffer cbuf = CharBuffer.allocate(60);
        Charset charset = Charset.forName("ISO-8859-1");
        CharsetEncoder encoder = charset.newEncoder();

        while (true) {
            SctpChannel sc = ssc.accept();
            /* get the current date */
            Date today = new Date();
            cbuf.put(USformatter.format(today)).flip();
            encoder.encode(cbuf, buf, true);
            buf.flip();

            /* send the message on the US stream */
            MessageInfo messageInfo = MessageInfo.createOutgoing(null, US_STREAM);
            sc.send(buf, messageInfo);

            /* update the buffer with French format */
            cbuf.clear();
            cbuf.put(FRformatter.format(today)).flip();
            buf.clear();
            encoder.encode(cbuf, buf, true);
            buf.flip();

            /* send the message on the French stream */
            messageInfo.streamNumber(FR_STREAM);
            sc.send(buf, messageInfo);

            cbuf.clear();
            buf.clear();
            sc.close();
        }
    }
}
```

- DaytimeClient.java

```
package com.test;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import com.sun.nio.sctp.AbstractNotificationHandler;
import com.sun.nio.sctp.AssociationChangeNotification;
import com.sun.nio.sctp.AssociationChangeNotification.AssocChangeEvent;
import com.sun.nio.sctp.HandlerResult;
import com.sun.nio.sctp.MessageInfo;
import com.sun.nio.sctp.SctpChannel;
import com.sun.nio.sctp.ShutdownNotification;

public class DaytimeClient {
    static int SERVER_PORT = 3456;
    static int US_STREAM = 0;
    static int FR_STREAM = 1;

    public static void main(String[] args) throws IOException {
        InetSocketAddress serverAddr = new InetSocketAddress("192.168.52.199", SERVER_PORT);
        ByteBuffer buf = ByteBuffer.allocateDirect(60);
        Charset charset = Charset.forName("ISO-8859-1");
        CharsetDecoder decoder = charset.newDecoder();
        SctpChannel sc = SctpChannel.open(serverAddr, 0, 0);

        /* handler to keep track of association setup and termination */
        AssociationHandler assocHandler = new AssociationHandler();

         /* expect two messages and two notifications */
        MessageInfo messageInfo = null;

        do {
            messageInfo = sc.receive(buf, System.out, assocHandler);
            buf.flip();

            if (buf.remaining() > 0 && messageInfo.streamNumber() == US_STREAM) {
                System.out.println("(US) " + decoder.decode(buf).toString());
            } else if (buf.remaining() > 0 && messageInfo.streamNumber() == FR_STREAM) {
                System.out.println("(FR) " +  decoder.decode(buf).toString());
            }

            buf.clear();
        } while (messageInfo != null);

        sc.close();
    }

    static class AssociationHandler extends AbstractNotificationHandler<PrintStream>
    {
        public HandlerResult handleNotification(AssociationChangeNotification not, PrintStream stream) {
            System.out.println(">>>> "+not.event());
            if (not.event().equals(AssocChangeEvent.COMM_UP)) {
                int outbound = not.association().maxOutboundStreams();
                int inbound = not.association().maxInboundStreams();
                stream.printf("New association setup with %d outbound streams" + ", and %d inbound streams.\n", outbound, inbound);
            }

            return HandlerResult.CONTINUE;

        }

        public HandlerResult handleNotification(ShutdownNotification not,PrintStream stream) {
            stream.printf("The association has been shutdown.\n");
            return HandlerResult.RETURN;
        }
    }
}
```

代码很简单,具体细节参见 java API 的 sctp.

- 运行测试:

```
将工程以JAR file 的方式导出sctpTest.jar, 上传到centos中
以后台方式运行server端: java -cp sctpTest.jar com.test.DaytimeServer &
运行客户端: java -cp sctpTest.jar com.test.DaytimeClient
```

### 参考

转载请注明出处: https://www.jianshu.com/p/94ccb39782f6
