### 1. IPv4 VS IPv6 对比分析

IPv6 较之 IPv4 优势，IPV6 具有以下几个优势：

#### 1.1 更大的用户接入【IPv6 具有更大的地址空间】

    IPv4中规定IP地址长度为32，即有2^32-1（符号^表示升幂，下同）个地址；而IPv6中IP地址的长度为128，即有2^128-1个地址。

#### 1.2 更快的传输速度【IPv6 使用更小的路由表、优化协议】

    为满足IPv6快、更快、非常快，这是现在互联网长期的追求，而 IPv6 是固定报头，不像 IPv4 那样携带一堆冗长的数据，
    简短的报头提升了网络数据转发的效率。由于 IPv6 的路由表更小，聚合能力更强，保证了数据转发的路径更短，极大的提高了转发效率。
    IPv6的地址分配一开始就遵循聚类（Aggregation）的原则，这使得路由器能在路由表中用一条记录（Entry）表示一片子网，
    大大减小了路由器中路由表的长度，提高了路由器转发数据包的速度。

#### 1.3 更安全的传输方式【IPv6 协议本身支持安全校验】

    安全虽然越来越多的网站正在开启 SSL，但是依旧有大量的网站没有采用 HTTPS 。在 IPv4 协议中并没有强制使用 IPSec加密数据，
    导致网站明文传输泄漏数据，而 IPv6 则是直接集成了 IPSec，在网络层认证与加密数据，为用户提供端到端的数据安全，保证数据不被劫持。
    在使用IPv6网络中用户可以对网络层的数据进行加密并对IP报文进行校验，极大的增强了网络的安全性。

#### 1.4 增强组播【IPv4 到 IPv6 的过渡技术】

       IPv6增加了增强的组播（Multicast）支持以及对流的控制（Flow Control），这使得网络上的多媒体应用有了长足发展的机会，
       为服务质量（QoS，Quality of Service）控制提供了良好的网络平台。

#### 1.5 对移动端更加友好

    手机等移动设备可以说已经成为许多人不可缺少的一部分了， IPv6 协议可以增强移动终端的移动特性、安全特性、路由特性，
    同时降低网络部署的难度和投资。

#### 1.6 即插即用

    同样与 IPv4 相比， IPv6 增加了自动配置以及重配置技术，对于 IP 地址等信息实现自动增删更新配置，提升 IPv6 的易管理性。

### 2 IPv4 到 IPv6 的过渡技术

    IPv6 在 1992 年被提出，到现在已经二十多年，IPv6 技术的发展已经很成熟，那么 IPv4 能否一下全部切换到 IPv6 呢，答案肯定是否定的。
    主要因为 IPv6 不是 IPv4 的改进，IPv6 是一个全新的协议，在链路层是不同的网络协议，不能直接进行通信。
    而且目前几乎都是在使用 IPv4，所以这种转换可能会持续很久。
    目前，IETF 已经成立了专门的工作组，研究 IPv4 到 IPv6 的转换问题，并且已提出了很多方案，我们先介绍其中几种。
    双栈技术、隧道技术、协议转换技术

#### 2.1 双栈技术

    IPv4 和 IPv6 有功能相近的网络层协议，都是基于相同的硬件平台，同一个主机同时运行 IPv4 和 IPv6 两套协议栈，
    具有 IPv4/IPv6 双协议栈的结点称为双栈节点，这些结点既可以收发 IPv4 报文，也可以收发 IPv6 报文。
    它们可以使用 IPv4 与 IPv4 结点互通，也可以直接使用 IPv6 与 IPv6 结点互通。
    双栈节点同时包含 IPv4 和 IPv6 的网络层，但传输层协议（如 TCP 和 UDP）的使用仍然是单一的。

    双栈技术具体有哪些优缺点呢？

优点：

    处理效率高、无信息丢失
    互通性好、网络规划简单
    充分发挥 IPv6 协议的所有优点，更小的路由表、更高的安全性等。
    资源占用多，运维复杂。

缺点：

    无法实现 IPv4 和 IPv6 互通
    对网络设备要求较高，内部网络改造牵扯比较大，周期性相比较较长。

#### 2.2 隧道技术

    隧道技术指将另外一个协议数据包的报头直接封装在原数据包报头前，从而可以实现在不同协议的网络上直接进行传输，
    这种机制用来在 IPv4 网络之上连接 IPv6 的站点，站点可以是一台主机，也可以是多个主机。
    隧道技术将 IPv6 的分组封装到 IPv4 的分组中，或者把 IPv4 的分组封装到 IPv6 的分组中，
    封装后的 IPv4 分组将通过 IPv4 的路由体系传输或者 IPv6 的分组进行传输。

隧道技术的优缺点则为：

优点：

无信息丢失
网络运维相比较简单
容易实现，只要在隧道的入口和出口进行修改
缺点：

隧道需要进行封装解封装，转发效率低。
无法实现 IPv4 和 IPv6 互通
无法解决 IPv4 短缺问题

#### 2.3 协议转换技术

    NAT-PT 技术附带协议转换器的网络地址转换器。是一种纯 IPv6 节点和 IPv4 节点间的互通方式，
    所有包括地址、协议在内的转换工作都由网络设备来完成。NAT-PT 包括静态和动态两种，两者都提供一对一的 IPv6 地址和 IPv4 地址的映射，
    只不过动态 NAT-PT 需要一个 IPv4 的地址池进行动态的地址转换。NAT-PT 技术有个最大的优点就是不需要进行 IPv4、IPv6 节点的升级改造，
    而缺点也是十分明显的。缺点是 IPv4 节点访问 IPv6 节点的实现方法比较复杂，网络设备进行协议转换、
    地址转换的处理开销较大一般在其他互通方式无法使用的情况下使用。

总结
无论哪种技术，我们都要从以下几个方面考虑，周期性、成本、技术难度、部署的便捷性。按照目前分析的过渡技术，双栈技术以及隧道技术是相比较易用性更高，也更容易实现。作为国内领先的数据云服务厂商，又拍云在 2016 年便开始投入 IPv6 的建设，为客户提供智能化的 IPv6 服务，无需进行配置修改，即可实现 IPv4 到 IPv6 的网关转换，客户和终端用户能够早一步享受 IPv6 更稳定、快速的网络质量。

参考：
https://www.jianshu.com/p/0e9c581e64ff
https://baike.baidu.com/item/IPv6/172297?fr=aladdin#reference-[6]-5228-wrap