## 前言

Netty 中的 Option 和 ChildOption 的区别：

- 1.Netty 中的 option 主要是设置的 ServerChannel 的一些选项，而 childOption 主要是设置的 ServerChannel 的子 Channel 的选项。

- 2.如果是在客户端，因为是 Bootstrap，只会有 option 而没有 childOption，所以设置的是客户端 Channel 的选项。

本文转载自：Netty：option 和 childOption 参数设置说明

## 一.通用参数

- CONNECT_TIMEOUT_MILLIS
  Netty 参数，连接超时毫秒数，默认值 30000 毫秒即 30 秒。

- MAX_MESSAGES_PER_READ
  Netty 参数，一次 Loop 读取的最大消息数，对于 ServerChannel 或者 NioByteChannel，默认值为 16，其他 Channel 默认值为 1。默认值这样设置，是因为：ServerChannel 需要接受足够多的连接，保证大吞吐量，NioByteChannel 可以减少不必要的系统调用 select。

- WRITE_SPIN_COUNT
  Netty 参数，一个 Loop 写操作执行的最大次数，默认值为 16。也就是说，对于大数据量的写操作至多进行 16 次，如果 16 次仍没有全部写完数据，此时会提交一个新的写任务给 EventLoop，任务将在下次调度继续执行。这样，其他的写请求才能被响应不会因为单个大数据量写请求而耽误。

- ALLOCATOR
  Netty 参数，ByteBuf 的分配器，默认值为 ByteBufAllocator.DEFAULT，4.0 版本为 UnpooledByteBufAllocator，4.1 版本为 PooledByteBufAllocator。该值也可以使用系统参数 io.netty.allocator.type 配置，使用字符串值："unpooled"，"pooled"。

- RCVBUF_ALLOCATOR
  Netty 参数，用于 Channel 分配接受 Buffer 的分配器，默认值为 AdaptiveRecvByteBufAllocator.DEFAULT，是一个自适应的接受缓冲区分配器，能根据接受到的数据自动调节大小。可选值为 FixedRecvByteBufAllocator，固定大小的接受缓冲区分配器。

- AUTO_READ
  Netty 参数，自动读取，默认值为 True。Netty 只在必要的时候才设置关心相应的 I/O 事件。对于读操作，需要调用 channel.read()设置关心的 I/O 事件为 OP_READ，这样若有数据到达才能读取以供用户处理。该值为 True 时，每次读操作完毕后会自动调用 channel.read()，从而有数据到达便能读取；否则，需要用户手动调用 channel.read()。需要注意的是：当调用 config.setAutoRead(boolean)方法时，如果状态由 false 变为 true，将会调用 channel.read()方法读取数据；由 true 变为 false，将调用 config.autoReadCleared()方法终止数据读取。

- WRITE_BUFFER_HIGH_WATER_MARK
  Netty 参数，写高水位标记，默认值 64KB。如果 Netty 的写缓冲区中的字节超过该值，Channel 的 isWritable()返回 False。

- WRITE_BUFFER_LOW_WATER_MARK
  Netty 参数，写低水位标记，默认值 32KB。当 Netty 的写缓冲区中的字节超过高水位之后若下降到低水位，则 Channel 的 isWritable()返回 True。写高低水位标记使用户可以控制写入数据速度，从而实现流量控制。推荐做法是：每次调用 channl.write(msg)方法首先调用 channel.isWritable()判断是否可写。

- MESSAGE_SIZE_ESTIMATOR
  Netty 参数，消息大小估算器，默认为 DefaultMessageSizeEstimator.DEFAULT。估算 ByteBuf、ByteBufHolder 和 FileRegion 的大小，其中 ByteBuf 和 ByteBufHolder 为实际大小，FileRegion 估算值为 0。该值估算的字节数在计算水位时使用，FileRegion 为 0 可知 FileRegion 不影响高低水位。

- SINGLE_EVENTEXECUTOR_PER_GROUP
  Netty 参数，单线程执行 ChannelPipeline 中的事件，默认值为 True。该值控制执行 ChannelPipeline 中执行 ChannelHandler 的线程。如果为 Trye，整个 pipeline 由一个线程执行，这样不需要进行线程切换以及线程同步，是 Netty4 的推荐做法；如果为 False，ChannelHandler 中的处理过程会由 Group 中的不同线程执行。

## 二.SocketChannel 参数

- SO_RCVBUF
  Socket 参数，TCP 数据接收缓冲区大小。该缓冲区即 TCP 接收滑动窗口，linux 操作系统可使用命令：cat /proc/sys/net/ipv4/tcp_rmem 查询其大小。一般情况下，该值可由用户在任意时刻设置，但当设置值超过 64KB 时，需要在连接到远端之前设置。

- SO_SNDBUF
  Socket 参数，TCP 数据发送缓冲区大小。该缓冲区即 TCP 发送滑动窗口，linux 操作系统可使用命令：cat /proc/sys/net/ipv4/tcp_smem 查询其大小。

- TCP_NODELAY
  TCP 参数，立即发送数据，默认值为 True（Netty 默认为 True 而操作系统默认为 False）。该值设置 Nagle 算法的启用，改算法将小的碎片数据连接成更大的报文来最小化所发送的报文的数量，如果需要发送一些较小的报文，则需要禁用该算法。Netty 默认禁用该算法，从而最小化报文传输延时。

- SO_KEEPALIVE
  Socket 参数，连接保活，默认值为 False。启用该功能时，TCP 会主动探测空闲连接的有效性。可以将此功能视为 TCP 的心跳机制，需要注意的是：默认的心跳间隔是 7200s 即 2 小时。Netty 默认关闭该功能。

- SO_REUSEADDR
  Socket 参数，地址复用，默认值 False。有四种情况可以使用：
  (1)当有一个有相同本地地址和端口的 socket1 处于 TIME_WAIT 状态时，而你希望启动的程序的 socket2 要占用该地址和端口，比如重启服务且保持先前端口。
  (2)有多块网卡或用 IP Alias 技术的机器在同一端口启动多个进程，但每个进程绑定的本地 IP 地址不能相同。
  (3)单个进程绑定相同的端口到多个 socket 上，但每个 socket 绑定的 ip 地址不同。
  (4)完全相同的地址和端口的重复绑定。但这只用于 UDP 的多播，不用于 TCP。

- SO_LINGER
  Socket 参数，关闭 Socket 的延迟时间，默认值为-1，表示禁用该功能。-1 表示 socket.close()方法立即返回，但 OS 底层会将发送缓冲区全部发送到对端。0 表示 socket.close()方法立即返回，OS 放弃发送缓冲区的数据直接向对端发送 RST 包，对端收到复位错误。非 0 整数值表示调用 socket.close()方法的线程被阻塞直到延迟时间到或发送缓冲区中的数据发送完毕，若超时，则对端会收到复位错误。

- IP_TOS
  IP 参数，设置 IP 头部的 Type-of-Service 字段，用于描述 IP 包的优先级和 QoS 选项。

- ALLOW_HALF_CLOSURE
  Netty 参数，一个连接的远端关闭时本地端是否关闭，默认值为 False。值为 False 时，连接自动关闭；为 True 时，触发 ChannelInboundHandler 的 userEventTriggered()方法，事件为 ChannelInputShutdownEvent。

## 三.ServerSocketChannel 参数

- SO_RCVBUF
  已说明，需要注意的是：当设置值超过 64KB 时，需要在绑定到本地端口前设置。该值设置的是由 ServerSocketChannel 使用 accept 接受的 SocketChannel 的接收缓冲区。

- SO_REUSEADDR
  Socket 参数，地址复用，默认值 False。有四种情况可以使用：
  (1)当有一个有相同本地地址和端口的 socket1 处于 TIME_WAIT 状态时，而你希望启动的程序的 socket2 要占用该地址和端口，比如重启服务且保持先前端口。
  (2)有多块网卡或用 IP Alias 技术的机器在同一端口启动多个进程，但每个进程绑定的本地 IP 地址不能相同。
  (3)单个进程绑定相同的端口到多个 socket 上，但每个 socket 绑定的 ip 地址不同。
  (4)完全相同的地址和端口的重复绑定。但这只用于 UDP 的多播，不用于 TCP。

- SO_BACKLOG
  Socket 参数，服务端接受连接的队列长度，如果队列已满，客户端连接将被拒绝。默认值，Windows 为 200，其他为 128。

## 四.DatagramChannel 参数

- SO_BROADCAST
  Socket 参数，设置广播模式。

- SO_RCVBUF
  已说明

- SO_SNDBUF
  已说明

- SO_REUSEADDR
  已说明

- IP_MULTICAST_LOOP_DISABLED
  对应 IP 参数 IP_MULTICAST_LOOP，设置本地回环接口的多播功能。由于 IP_MULTICAST_LOOP 返回 True 表示关闭，所以 Netty 加上后缀\_DISABLED 防止歧义。

- IP_MULTICAST_ADDR
  对应 IP 参数 IP_MULTICAST_IF，设置对应地址的网卡为多播模式。

- IP_MULTICAST_IF
  对应 IP 参数 IP_MULTICAST_IF2，同上但支持 IPV6。

- IP_MULTICAST_TTL
  IP 参数，多播数据报的 time-to-live 即存活跳数。

- IP_TOS
  已说明

- DATAGRAM_CHANNEL_ACTIVE_ON_REGISTRATION
  Netty 参数，DatagramChannel 注册的 EventLoop 即表示已激活。

## 五.参考

https://www.jianshu.com/p/8670f49c32d0
