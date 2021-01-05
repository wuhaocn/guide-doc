### 一、什么是 netlink

Netlink 套接字是用以实现用户进程与内核进程通信的一种特殊的进程间通信(IPC) ,也是网络应用程序与内核通信的最常用的接口。

在 Linux 内核中，使用 netlink 进行应用与内核通信的应用有很多，如

- 路由 daemon（NETLINK_ROUTE）
- 用户态 socket 协议（NETLINK_USERSOCK）
- 防火墙（NETLINK_FIREWALL）
- netfilter 子系统（NETLINK_NETFILTER）
- 内核事件向用户态通知（NETLINK_KOBJECT_UEVENT）
- 通用 netlink（NETLINK_GENERIC）

Netlink 是一种在内核与用户应用间进行双向数据传输的非常好的方式，用户态应用使用标准的 socket API 就可以使用 netlink 提供的强大功能，内核态需要使用专门的内核 API 来使用 netlink。

一般来说用户空间和内核空间的通信方式有三种：/proc、ioctl、Netlink。而前两种都是单向的，而 Netlink 可以实现双工通信。

Netlink 相对于系统调用，ioctl 以及 /proc 文件系统而言，具有以下优点：

- netlink 使用简单，只需要在 include/linux/netlink.h 中增加一个新类型的 netlink 协议定义即可,(如 #define NETLINK_TEST 20 然后，内核和用户态应用就可以立即通过 socket API 使用该 netlink 协议类型进行数据交换)
- netlink 是一种异步通信机制，在内核与用户态应用之间传递的消息保存在 socket 缓存队列中，发送消息只是把消息保存在接收者的 socket 的接收队列，而不需要等待接收者收到消息
  使用 netlink 的内核部分可以采用模块的方式实现，使用 netlink 的应用部分和内核部分没有编译时依赖
- netlink 支持多播，内核模块或应用可以把消息多播给一个 netlink 组，属于该 neilink 组的任何内核模块或应用都能接收到该消息，内核事件向用户态的通知机制就使用了这一特性
- 内核可以使用 netlink 首先发起会话
  Netlink 协议基于 BSD socket 和 AF_NETLINK 地址簇，使用 32 位的端口号寻址，每个 Netlink 协议通常与一个或一组内核服务/组件相关联，如 NETLINK_ROUTE 用于获取和设置路由与链路信息、NETLINK_KOBJECT_UEVENT 用于内核向用户空间的 udev 进程发送通知等。

### 二、用户态数据结构

用户态应用使用标准的 socket API 有 sendto()，recvfrom(), sendmsg(), recvmsg()。

Netlink 通信跟常用 UDP Socket 通信类似，struct sockaddr_nl 是 netlink 通信地址，跟普通 socket struct sockaddr_in 类似。

- 1. struct sockaddr_nl 结构：

```
struct sockaddr_nl {
     __kernel_sa_family_t    nl_family;  /* AF_NETLINK （跟AF_INET对应）*/
     unsigned short  nl_pad;     /* zero */
     __u32       nl_pid;     /* port ID  （通信端口号）*/
     __u32       nl_groups;  /* multicast groups mask */
};
```

- 2. struct nlmsghd 结构：

```
/* struct nlmsghd 是netlink消息头*/
struct nlmsghdr {
    __u32       nlmsg_len;  /* Length of message including header */
    __u16       nlmsg_type; /* Message content */
    __u16       nlmsg_flags;    /* Additional flags */
    __u32       nlmsg_seq;  /* Sequence number */
    __u32       nlmsg_pid;  /* Sending process port ID */
};
```

nlmsg_type：消息状态，内核在 include/uapi/linux/netlink.h 中定义了以下 4 种通用的消息类型，它们分别是：

```
#define NLMSG_NOOP      0x1 /* Nothing.     */
#define NLMSG_ERROR     0x2 /* Error        */
#define NLMSG_DONE      0x3 /* End of a dump    */
#define NLMSG_OVERRUN       0x4 /* Data lost        */
#define NLMSG_MIN_TYPE      0x10    /* < 0x10: reserved control messages */
nlmsg_flags：消息标记，它们用以表示消息的类型，如下
/* Flags values */
#define NLM_F_REQUEST       1   /* It is request message.   */
#define NLM_F_MULTI     2   /* Multipart message, terminated by NLMSG_DONE */
#define NLM_F_ACK       4   /* Reply with ack, with zero or error code */
#define NLM_F_ECHO      8   /* Echo this request        */
#define NLM_F_DUMP_INTR     16  /* Dump was inconsistent due to sequence change */

/* Modifiers to GET request */
#define NLM_F_ROOT  0x100   /* specify tree root    */
#define NLM_F_MATCH 0x200   /* return all matching  */
#define NLM_F_ATOMIC    0x400   /* atomic GET       */
#define NLM_F_DUMP  (NLM_F_ROOT|NLM_F_MATCH)

/* Modifiers to NEW request */
#define NLM_F_REPLACE   0x100   /* Override existing        */
#define NLM_F_EXCL  0x200   /* Do not touch, if it exists   */
#define NLM_F_CREATE    0x400   /* Create, if it does not exist */
#define NLM_F_APPEND    0x800   /* Add to end of list       */
```

- 3. struct msghdr 结构体

```
struct iovec {                    /* Scatter/gather array items */
     void  *iov_base;              /* Starting address */
     size_t iov_len;               /* Number of bytes to transfer */
 };
  /* iov_base: iov_base指向数据包缓冲区，即参数buff，iov_len是buff的长度。msghdr中允许一次传递多个buff，以数组的形式组织在 msg_iov中，msg_iovlen就记录数组的长度 （即有多少个buff）  */
 struct msghdr {
     void         *msg_name;       /* optional address */
     socklen_t     msg_namelen;    /* size of address */
     struct iovec *msg_iov;        /* scatter/gather array */
     size_t        msg_iovlen;     /* # elements in msg_iov */
     void         *msg_control;    /* ancillary data, see below */
     size_t        msg_controllen; /* ancillary data buffer len */
     int           msg_flags;      /* flags on received message */
 };
```

### 三、netlink 内核数据结构

- 1. netlink 消息类型：

```
#define NETLINK_ROUTE       0   /* Routing/device hook              */
#define NETLINK_UNUSED      1   /* Unused number                */
#define NETLINK_USERSOCK    2   /* Reserved for user mode socket protocols  */
#define NETLINK_FIREWALL    3   /* Unused number, formerly ip_queue     */
#define NETLINK_SOCK_DIAG   4   /* socket monitoring                */
#define NETLINK_NFLOG       5   /* netfilter/iptables ULOG */
#define NETLINK_XFRM        6   /* ipsec */
#define NETLINK_SELINUX     7   /* SELinux event notifications */
#define NETLINK_ISCSI       8   /* Open-iSCSI */
#define NETLINK_AUDIT       9   /* auditing */
#define NETLINK_FIB_LOOKUP  10
#define NETLINK_CONNECTOR   11
#define NETLINK_NETFILTER   12  /* netfilter subsystem */
#define NETLINK_IP6_FW      13
#define NETLINK_DNRTMSG     14  /* DECnet routing messages */
#define NETLINK_KOBJECT_UEVENT  15  /* Kernel messages to userspace */
#define NETLINK_GENERIC     16
/* leave room for NETLINK_DM (DM Events) */
#define NETLINK_SCSITRANSPORT   18  /* SCSI Transports */
#define NETLINK_ECRYPTFS    19
#define NETLINK_RDMA        20
#define NETLINK_CRYPTO      21  /* Crypto layer */

#define NETLINK_INET_DIAG   NETLINK_SOCK_DIAG

#define MAX_LINKS 32
```

- 2. netlink 常用宏：

```
#define NLMSG_ALIGNTO   4U
/* 宏NLMSG_ALIGN(len)用于得到不小于len且字节对齐的最小数值 */
#define NLMSG_ALIGN(len) ( ((len)+NLMSG_ALIGNTO-1) & ~(NLMSG_ALIGNTO-1) )

/* Netlink 头部长度 */
#define NLMSG_HDRLEN     ((int) NLMSG_ALIGN(sizeof(struct nlmsghdr)))

/* 计算消息数据len的真实消息长度（消息体 +　消息头）*/
#define NLMSG_LENGTH(len) ((len) + NLMSG_HDRLEN)

/* 宏NLMSG_SPACE(len)返回不小于NLMSG_LENGTH(len)且字节对齐的最小数值 */
#define NLMSG_SPACE(len) NLMSG_ALIGN(NLMSG_LENGTH(len))

/* 宏NLMSG_DATA(nlh)用于取得消息的数据部分的首地址，设置和读取消息数据部分时需要使用该宏 */
#define NLMSG_DATA(nlh)  ((void*)(((char*)nlh) + NLMSG_LENGTH(0)))

/* 宏NLMSG_NEXT(nlh,len)用于得到下一个消息的首地址, 同时len 变为剩余消息的长度 */
#define NLMSG_NEXT(nlh,len)  ((len) -= NLMSG_ALIGN((nlh)->nlmsg_len), \
                  (struct nlmsghdr*)(((char*)(nlh)) + NLMSG_ALIGN((nlh)->nlmsg_len)))

/* 判断消息是否 >len */
#define NLMSG_OK(nlh,len) ((len) >= (int)sizeof(struct nlmsghdr) && \
               (nlh)->nlmsg_len >= sizeof(struct nlmsghdr) && \
               (nlh)->nlmsg_len <= (len))

/* NLMSG_PAYLOAD(nlh,len) 用于返回payload的长度*/
#define NLMSG_PAYLOAD(nlh,len) ((nlh)->nlmsg_len - NLMSG_SPACE((len)))
```

- 3. netlink 内核常用函数

netlink_kernel_create 内核函数用于创建内核 socket 与用户态通信

```
static inline struct sock *
netlink_kernel_create(struct net *net, int unit, struct netlink_kernel_cfg *cfg)
/* net: net指向所在的网络命名空间, 一般默认传入的是&init_net(不需要定义);  定义在net_namespace.c(extern struct net init_net);
   unit：netlink协议类型
   cfg： cfg存放的是netlink内核配置参数（如下）
*/

/* optional Netlink kernel configuration parameters */
struct netlink_kernel_cfg {
    unsigned int    groups;
    unsigned int    flags;
    void        (*input)(struct sk_buff *skb); /* input 回调函数 */
    struct mutex    *cb_mutex;
    void        (*bind)(int group);
    bool        (*compare)(struct net *net, struct sock *sk);
};
```

- 4. 单播 netlink_unicast() 和 多播 netlink_broadcast()

```
/* 发送单播消息 */
extern int netlink_unicast(struct sock *ssk, struct sk_buff *skb, __u32 portid, int nonblock);
/*
 ssk: netlink socket
 skb: skb buff 指针
 portid： 通信的端口号
 nonblock：表示该函数是否为非阻塞，如果为1，该函数将在没有接收缓存可利用时立即返回，而如果为0，该函数在没有接收缓存可利用定时睡眠
*/

/* 发送多播消息 */
extern int netlink_broadcast(struct sock *ssk, struct sk_buff *skb, __u32 portid,
                 __u32 group, gfp_t allocation);
/*
   ssk: 同上（对应netlink_kernel_create 返回值）、
   skb: 内核skb buff
   portid： 端口id
   group: 是所有目标多播组对应掩码的"OR"操作的合值。
   allocation: 指定内核内存分配方式，通常GFP_ATOMIC用于中断上下文，而GFP_KERNEL用于其他场合。这个参数的存在是因为该API可能需要分配一个或多个缓冲区来对多播消息进行clone
*/
```

### 参考

https://www.jianshu.com/p/6810f42b9f8f
