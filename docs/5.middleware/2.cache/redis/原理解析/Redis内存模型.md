# [深入学习 Redis（1）：Redis 内存模型](https://www.cnblogs.com/kismetv/p/8654978.html)

## 前言

Redis 是目前最火爆的内存数据库之一，通过在内存中读写数据，大大提高了读写速度，可以说 Redis 是实现网站高并发不可或缺的一部分。

我们使用 Redis 时，会接触 Redis 的 5 种对象类型（字符串、哈希、列表、集合、有序集合），丰富的类型是 Redis 相对于 Memcached 等的一大优势。在了解 Redis 的 5 种对象类型的用法和特点的基础上，进一步了解 Redis 的内存模型，对 Redis 的使用有很大帮助，例如：

1、估算 Redis 内存使用量。目前为止，内存的使用成本仍然相对较高，使用内存不能无所顾忌；根据需求合理的评估 Redis 的内存使用量，选择合适的机器配置，可以在满足需求的情况下节约成本。

2、优化内存占用。了解 Redis 内存模型可以选择更合适的数据类型和编码，更好的利用 Redis 内存。

3、分析解决问题。当 Redis 出现阻塞、内存占用等问题时，尽快发现导致问题的原因，便于分析解决问题。

这篇文章主要介绍 Redis 的内存模型（以 3.0 为例），包括 Redis 占用内存的情况及如何查询、不同的对象类型在内存中的编码方式、内存分配器(jemalloc)、简单动态字符串(SDS)、RedisObject 等；然后在此基础上介绍几个 Redis 内存模型的应用。

在后面的文章中，会陆续介绍关于 Redis 高可用的内容，包括主从复制、哨兵、集群等等，欢迎关注。

## 系列文章

[深入学习 Redis（1）：Redis 内存模型](https://www.cnblogs.com/kismetv/p/8654978.html)

[深入学习 Redis（2）：持久化](https://www.cnblogs.com/kismetv/p/9137897.html)

[深入学习 Redis（3）：主从复制](https://www.cnblogs.com/kismetv/p/9236731.html)

[深入学习 Redis（4）：哨兵](https://www.cnblogs.com/kismetv/p/9609938.html)

[深入学习 Redis（5）：集群](https://www.cnblogs.com/kismetv/p/9853040.html)

## 目录

[一、Redis 内存统计]()

[二、Redis 内存划分]()

[1、数据（或者称为对象）]()

[2、进程本身运行需要的内存]()

[3、缓冲内存]()

[4、内存碎片]()

[三、Redis 数据存储的细节]()

[1、概述]()

[2、jemalloc]()

[3、redisObject]()

[4、SDS]()

[四、Redis 的对象类型与内部编码]()

[1、字符串]()

[2、列表]()

[3、哈希]()

[4、集合]()

[5、有序集合]()

[五、应用举例]()

[1、估算 Redis 内存使用量]()

[2、优化内存占用]()

[3、关注内存碎片率]()

[六、参考文献]()

# []()一、Redis 内存统计

工欲善其事必先利其器，在说明 Redis 内存之前首先说明如何统计 Redis 使用内存的情况。

在客户端通过 redis-cli 连接服务器后（后面如无特殊说明，客户端一律使用 redis-cli），通过 info 命令可以查看内存使用情况：
[?]()1info memory

![](https://images2018.cnblogs.com/blog/1174710/201803/1174710-20180327000947752-2103814952.png)

其中，info 命令可以显示 redis 服务器的许多信息，包括服务器基本信息、CPU、内存、持久化、客户端连接信息等等；memory 是参数，表示只显示内存相关的信息。

返回结果中比较重要的几个说明如下：

（1）**used_memory\*\***：\*\*Redis 分配器分配的内存总量（单位是字节），包括使用的虚拟内存（即 swap）；Redis 分配器后面会介绍。used_memory_human 只是显示更友好。

（2）**used_memory_rss\*\***：\*\*Redis 进程占据操作系统的内存（单位是字节），与 top 及 ps 命令看到的值是一致的；除了分配器分配的内存之外，used_memory_rss 还包括进程运行本身需要的内存、内存碎片等，但是不包括虚拟内存。

因此，used_memory 和 used_memory_rss，前者是从 Redis 角度得到的量，后者是从操作系统角度得到的量。二者之所以有所不同，一方面是因为内存碎片和 Redis 进程运行需要占用内存，使得前者可能比后者小，另一方面虚拟内存的存在，使得前者可能比后者大。

由于在实际应用中，Redis 的数据量会比较大，此时进程运行占用的内存与 Redis 数据量和内存碎片相比，都会小得多；因此 used_memory_rss 和 used_memory 的比例，便成了衡量 Redis 内存碎片率的参数；这个参数就是 mem_fragmentation_ratio。

（3）**mem_fragmentation_ratio\*\***：\*\*内存碎片比率，该值是 used_memory_rss / used_memory 的比值。

mem_fragmentation_ratio 一般大于 1，且该值越大，内存碎片比例越大。mem_fragmentation_ratio<1，说明 Redis 使用了虚拟内存，由于虚拟内存的媒介是磁盘，比内存速度要慢很多，当这种情况出现时，应该及时排查，如果内存不足应该及时处理，如增加 Redis 节点、增加 Redis 服务器的内存、优化应用等。

一般来说，mem_fragmentation_ratio 在 1.03 左右是比较健康的状态（对于 jemalloc 来说）；上面截图中的 mem_fragmentation_ratio 值很大，是因为还没有向 Redis 中存入数据，Redis 进程本身运行的内存使得 used_memory_rss 比 used_memory 大得多。

（4）**mem_allocator\*\***：\*\*Redis 使用的内存分配器，在编译时指定；可以是 libc 、jemalloc 或者 tcmalloc，默认是 jemalloc；截图中使用的便是默认的 jemalloc。

# []()二、Redis 内存划分

Redis 作为内存数据库，在内存中存储的内容主要是数据（键值对）；通过前面的叙述可以知道，除了数据以外，Redis 的其他部分也会占用内存。

Redis 的内存占用主要可以划分为以下几个部分：

## []()1、数据

作为数据库，数据是最主要的部分；这部分占用的内存会统计在 used_memory 中。

Redis 使用键值对存储数据，其中的值（对象）包括 5 种类型，即字符串、哈希、列表、集合、有序集合。这 5 种类型是 Redis 对外提供的，实际上，在 Redis 内部，每种类型可能有 2 种或更多的内部编码实现；此外，Redis 在存储对象时，并不是直接将数据扔进内存，而是会对对象进行各种包装：如 redisObject、SDS 等；这篇文章后面将重点介绍 Redis 中数据存储的细节。

## []()2、进程本身运行需要的内存

Redis 主进程本身运行肯定需要占用内存，如代码、常量池等等；这部分内存大约几兆，在大多数生产环境中与 Redis 数据占用的内存相比可以忽略。这部分内存不是由 jemalloc 分配，因此不会统计在 used_memory 中。

补充说明：除了主进程外，Redis 创建的子进程运行也会占用内存，如 Redis 执行 AOF、RDB 重写时创建的子进程。当然，这部分内存不属于 Redis 进程，也不会统计在 used_memory 和 used_memory_rss 中。

## []()3、缓冲内存

缓冲内存包括客户端缓冲区、复制积压缓冲区、AOF 缓冲区等；其中，客户端缓冲存储客户端连接的输入输出缓冲；复制积压缓冲用于部分复制功能；AOF 缓冲区用于在进行 AOF 重写时，保存最近的写入命令。在了解相应功能之前，不需要知道这些缓冲的细节；这部分内存由 jemalloc 分配，因此会统计在 used_memory 中。

## []()4、内存碎片

内存碎片是 Redis 在分配、回收物理内存过程中产生的。例如，如果对数据的更改频繁，而且数据之间的大小相差很大，可能导致 redis 释放的空间在物理内存中并没有释放，但 redis 又无法有效利用，这就形成了内存碎片。内存碎片不会统计在 used_memory 中。

内存碎片的产生与对数据进行的操作、数据的特点等都有关；此外，与使用的内存分配器也有关系：如果内存分配器设计合理，可以尽可能的减少内存碎片的产生。后面将要说到的 jemalloc 便在控制内存碎片方面做的很好。

如果 Redis 服务器中的内存碎片已经很大，可以通过安全重启的方式减小内存碎片：因为重启之后，Redis 重新从备份文件中读取数据，在内存中进行重排，为每个数据重新选择合适的内存单元，减小内存碎片。

# []()三、Redis 数据存储的细节

## []()1、概述

关于 Redis 数据存储的细节，涉及到内存分配器（如 jemalloc）、简单动态字符串（SDS）、5 种对象类型及内部编码、redisObject。在讲述具体内容之前，先说明一下这几个概念之间的关系。

下图是执行 set hello world 时，所涉及到的数据模型。

![](https://images2018.cnblogs.com/blog/1174710/201803/1174710-20180327001055927-1896197804.png)

图片来源：https://searchdatabase.techtarget.com.cn/7-20218/

（1）dictEntry：Redis 是 Key-Value 数据库，因此对每个键值对都会有一个 dictEntry，里面存储了指向 Key 和 Value 的指针；next 指向下一个 dictEntry，与本 Key-Value 无关。

（2）Key：图中右上角可见，Key（”hello”）并不是直接以字符串存储，而是存储在 SDS 结构中。

（3）redisObject：Value(“world”)既不是直接以字符串存储，也不是像 Key 一样直接存储在 SDS 中，而是存储在 redisObject 中。实际上，不论 Value 是 5 种类型的哪一种，都是通过 redisObject 来存储的；而 redisObject 中的 type 字段指明了 Value 对象的类型，ptr 字段则指向对象所在的地址。不过可以看出，字符串对象虽然经过了 redisObject 的包装，但仍然需要通过 SDS 存储。

实际上，redisObject 除了 type 和 ptr 字段以外，还有其他字段图中没有给出，如用于指定对象内部编码的字段；后面会详细介绍。

（4）jemalloc：无论是 DictEntry 对象，还是 redisObject、SDS 对象，都需要内存分配器（如 jemalloc）分配内存进行存储。以 DictEntry 对象为例，有 3 个指针组成，在 64 位机器下占 24 个字节，jemalloc 会为它分配 32 字节大小的内存单元。

下面来分别介绍 jemalloc、redisObject、SDS、对象类型及内部编码。

## []()2、jemalloc

Redis 在编译时便会指定内存分配器；内存分配器可以是 libc 、jemalloc 或者 tcmalloc，默认是 jemalloc。

jemalloc 作为 Redis 的默认内存分配器，在减小内存碎片方面做的相对比较好。jemalloc 在 64 位系统中，将内存空间划分为小、大、巨大三个范围；每个范围内又划分了许多小的内存块单位；当 Redis 存储数据时，会选择大小最合适的内存块进行存储。

jemalloc 划分的内存单元如下图所示：

![](https://images2018.cnblogs.com/blog/1174710/201803/1174710-20180327001126509-2023165562.png)

图片来源：http://blog.csdn.net/zhengpeitao/article/details/76573053

例如，如果需要存储大小为 130 字节的对象，jemalloc 会将其放入 160 字节的内存单元中。

## []()3、redisObject

前面说到，Redis 对象有 5 种类型；无论是哪种类型，Redis 都不会直接存储，而是通过 redisObject 对象进行存储。

redisObject 对象非常重要，Redis 对象的类型、内部编码、内存回收、共享对象等功能，都需要 redisObject 支持，下面将通过 redisObject 的结构来说明它是如何起作用的。

redisObject 的定义如下（不同版本的 Redis 可能稍稍有所不同）：

```java
typedef struct redisObject {
　　unsigned type:4;
　　unsigned encoding:4;
　　unsigned lru:REDIS_LRU_BITS; /* lru time (relative to server.lruclock) */
　　int refcount;
　　void *ptr;
} robj;
```

redisObject 的每个字段的含义和作用如下：

### （1）type

type 字段表示对象的类型，占 4 个比特；目前包括 REDIS_STRING(字符串)、REDIS_LIST (列表)、REDIS_HASH(哈希)、REDIS_SET(集合)、REDIS_ZSET(有序集合)。

当我们执行 type 命令时，便是通过读取 RedisObject 的 type 字段获得对象的类型；如下图所示：

![](https://images2018.cnblogs.com/blog/1174710/201803/1174710-20180327001214189-1733420705.png)

### （2）encoding

encoding 表示对象的内部编码，占 4 个比特。

对于 Redis 支持的每种类型，都有至少两种内部编码，例如对于字符串，有 int、embstr、raw 三种编码。通过 encoding 属性，Redis 可以根据不同的使用场景来为对象设置不同的编码，大大提高了 Redis 的灵活性和效率。以列表对象为例，有压缩列表和双端链表两种编码方式；如果列表中的元素较少，Redis 倾向于使用压缩列表进行存储，因为压缩列表占用内存更少，而且比双端链表可以更快载入；当列表对象元素较多时，压缩列表就会转化为更适合存储大量元素的双端链表。

通过 object encoding 命令，可以查看对象采用的编码方式，如下图所示：

![](https://images2018.cnblogs.com/blog/1174710/201803/1174710-20180327001228807-998910409.png)

5 种对象类型对应的编码方式以及使用条件，将在后面介绍。

### （3）lru

lru 记录的是对象最后一次被命令程序访问的时间，占据的比特数不同的版本有所不同（如 4.0 版本占 24 比特，2.6 版本占 22 比特）。

通过对比 lru 时间与当前时间，可以计算某个对象的空转时间；object idletime 命令可以显示该空转时间（单位是秒）。object idletime 命令的一个特殊之处在于它不改变对象的 lru 值。

![](https://images2018.cnblogs.com/blog/1174710/201803/1174710-20180327001239788-1325383307.png)

lru 值除了通过 object idletime 命令打印之外，还与 Redis 的内存回收有关系：如果 Redis 打开了 maxmemory 选项，且内存回收算法选择的是 volatile-lru 或 allkeys—lru，那么当 Redis 内存占用超过 maxmemory 指定的值时，Redis 会优先选择空转时间最长的对象进行释放。

### （4）refcount

**refcount\*\***与共享对象\*\*

refcount 记录的是该对象被引用的次数，类型为整型。refcount 的作用，主要在于对象的引用计数和内存回收。当创建新对象时，refcount 初始化为 1；当有新程序使用该对象时，refcount 加 1；当对象不再被一个新程序使用时，refcount 减 1；当 refcount 变为 0 时，对象占用的内存会被释放。

Redis 中被多次使用的对象(refcount>1)，称为共享对象。Redis 为了节省内存，当有一些对象重复出现时，新的程序不会创建新的对象，而是仍然使用原来的对象。这个被重复使用的对象，就是共享对象。目前共享对象仅支持整数值的字符串对象。

**共享对象的具体实现**

Redis 的共享对象目前只支持整数值的字符串对象。之所以如此，实际上是对内存和 CPU（时间）的平衡：共享对象虽然会降低内存消耗，但是判断两个对象是否相等却需要消耗额外的时间。对于整数值，判断操作复杂度为 O(1)；对于普通字符串，判断复杂度为 O(n)；而对于哈希、列表、集合和有序集合，判断的复杂度为 O(n^2)。

虽然共享对象只能是整数值的字符串对象，但是 5 种类型都可能使用共享对象（如哈希、列表等的元素可以使用）。

就目前的实现来说，Redis 服务器在初始化时，会创建 10000 个字符串对象，值分别是 0~9999 的整数值；当 Redis 需要使用值为 0~9999 的字符串对象时，可以直接使用这些共享对象。10000 这个数字可以通过调整参数 REDIS_SHARED_INTEGERS（4.0 中是 OBJ_SHARED_INTEGERS）的值进行改变。

共享对象的引用次数可以通过 object refcount 命令查看，如下图所示。命令执行的结果页佐证了只有 0~9999 之间的整数会作为共享对象。

![](https://images2018.cnblogs.com/blog/1174710/201803/1174710-20180327001256958-1309209644.png)

### （5）ptr

ptr 指针指向具体的数据，如前面的例子中，set hello world，ptr 指向包含字符串 world 的 SDS。

### （6）总结

综上所述，redisObject 的结构与对象类型、编码、内存回收、共享对象都有关系；一个 redisObject 对象的大小为 16 字节：

4bit+4bit+24bit+4Byte+8Byte=16Byte。

## []()4、SDS

Redis 没有直接使用 C 字符串(即以空字符’\0’结尾的字符数组)作为默认的字符串表示，而是使用了 SDS。SDS 是简单动态字符串(Simple Dynamic String)的缩写。

### （1）SDS 结构

sds 的结构如下：

```java
struct sdshdr {
    int len;
    int free;
    char buf[];
};
```

其中，buf 表示字节数组，用来存储字符串；len 表示 buf 已使用的长度，free 表示 buf 未使用的长度。下面是两个例子。

![](https://images2018.cnblogs.com/blog/1174710/201803/1174710-20180327001321434-1043595793.png)

![](https://images2018.cnblogs.com/blog/1174710/201803/1174710-20180327001325561-890602831.png)

图片来源：《Redis 设计与实现》

通过 SDS 的结构可以看出，buf 数组的长度=free+len+1（其中 1 表示字符串结尾的空字符）；所以，一个 SDS 结构占据的空间为：free 所占长度+len 所占长度+ buf 数组的长度=4+4+free+len+1=free+len+9。

### （2）SDS 与 C 字符串的比较

SDS 在 C 字符串的基础上加入了 free 和 len 字段，带来了很多好处：

- 获取字符串长度：SDS 是 O(1)，C 字符串是 O(n)
- 缓冲区溢出：使用 C 字符串的 API 时，如果字符串长度增加（如 strcat 操作）而忘记重新分配内存，很容易造成缓冲区的溢出；而 SDS 由于记录了长度，相应的 API 在可能造成缓冲区溢出时会自动重新分配内存，杜绝了缓冲区溢出。
- 修改字符串时内存的重分配：对于 C 字符串，如果要修改字符串，必须要重新分配内存（先释放再申请），因为如果没有重新分配，字符串长度增大时会造成内存缓冲区溢出，字符串长度减小时会造成内存泄露。而对于 SDS，由于可以记录 len 和 free，因此解除了字符串长度和空间数组长度之间的关联，可以在此基础上进行优化：空间预分配策略（即分配内存时比实际需要的多）使得字符串长度增大时重新分配内存的概率大大减小；惰性空间释放策略使得字符串长度减小时重新分配内存的概率大大减小。
- 存取二进制数据：SDS 可以，C 字符串不可以。因为 C 字符串以空字符作为字符串结束的标识，而对于一些二进制文件（如图片等），内容可能包括空字符串，因此 C 字符串无法正确存取；而 SDS 以字符串长度 len 来作为字符串结束标识，因此没有这个问题。

此外，由于 SDS 中的 buf 仍然使用了 C 字符串（即以’\0’结尾），因此 SDS 可以使用 C 字符串库中的部分函数；但是需要注意的是，只有当 SDS 用来存储文本数据时才可以这样使用，在存储二进制数据时则不行（’\0’不一定是结尾）。

### （3）SDS 与 C 字符串的应用

Redis 在存储对象时，一律使用 SDS 代替 C 字符串。例如 set hello world 命令，hello 和 world 都是以 SDS 的形式存储的。而 sadd myset member1 member2 member3 命令，不论是键（”myset”），还是集合中的元素（”member1”、 ”member2”和”member3”），都是以 SDS 的形式存储。除了存储对象，SDS 还用于存储各种缓冲区。

只有在字符串不会改变的情况下，如打印日志时，才会使用 C 字符串。

# []()四、Redis 的对象类型与内部编码

前面已经说过，Redis 支持 5 种对象类型，而每种结构都有至少两种编码；这样做的好处在于：一方面接口与实现分离，当需要增加或改变内部编码时，用户使用不受影响，另一方面可以根据不同的应用场景切换内部编码，提高效率。

Redis 各种对象类型支持的内部编码如下图所示(图中版本是 Redis3.0，Redis 后面版本中又增加了内部编码，略过不提；本章所介绍的内部编码都是基于 3.0 的)：

![](https://images2018.cnblogs.com/blog/1174710/201803/1174710-20180327001358239-1304238510.png)

图片来源：《Redis 设计与实现》

关于 Redis 内部编码的转换，都符合以下规律：**编码转换在\*\***Redis\***\*写入数据时完成，且转换过程不可逆，只能从小内存编码向大内存编码转换。**

## []()1、字符串

### （1）概况

字符串是最基础的类型，因为所有的键都是字符串类型，且字符串之外的其他几种复杂类型的元素也是字符串。

字符串长度不能超过 512MB。

### （2）内部编码

字符串类型的内部编码有 3 种，它们的应用场景如下：

- int：8 个字节的长整型。字符串值是整型时，这个值使用 long 整型表示。
- embstr：<=39 字节的字符串。embstr 与 raw 都使用 redisObject 和 sds 保存数据，区别在于，embstr 的使用只分配一次内存空间（因此 redisObject 和 sds 是连续的），而 raw 需要分配两次内存空间（分别为 redisObject 和 sds 分配空间）。因此与 raw 相比，embstr 的好处在于创建时少分配一次空间，删除时少释放一次空间，以及对象的所有数据连在一起，寻找方便。而 embstr 的坏处也很明显，如果字符串的长度增加需要重新分配内存时，整个 redisObject 和 sds 都需要重新分配空间，因此 redis 中的 embstr 实现为只读。
- raw：大于 39 个字节的字符串

示例如下图所示：

![](https://images2018.cnblogs.com/blog/1174710/201803/1174710-20180327001417703-15851809.png)

embstr 和 raw 进行区分的长度，是 39；是因为 redisObject 的长度是 16 字节，sds 的长度是 9+字符串长度；因此当字符串长度是 39 时，embstr 的长度正好是 16+9+39=64，jemalloc 正好可以分配 64 字节的内存单元。

### （3）编码转换

当 int 数据不再是整数，或大小超过了 long 的范围时，自动转化为 raw。

而对于 embstr，由于其实现是只读的，因此在对 embstr 对象进行修改时，都会先转化为 raw 再进行修改，因此，只要是修改 embstr 对象，修改后的对象一定是 raw 的，无论是否达到了 39 个字节。示例如下图所示：

![](https://images2018.cnblogs.com/blog/1174710/201803/1174710-20180327001426651-1225081171.png)

## []()2、列表

### （1）概况

列表（list）用来存储多个有序的字符串，每个字符串称为元素；一个列表可以存储 2^32-1 个元素。Redis 中的列表支持两端插入和弹出，并可以获得指定位置（或范围）的元素，可以充当数组、队列、栈等。

### （2）内部编码

列表的内部编码可以是压缩列表（ziplist）或双端链表（linkedlist）。

双端链表：由一个 list 结构和多个 listNode 结构组成；典型结构如下图所示：

![](https://images2018.cnblogs.com/blog/1174710/201803/1174710-20180327001435577-242733744.png)

图片来源：《Redis 设计与实现》

通过图中可以看出，双端链表同时保存了表头指针和表尾指针，并且每个节点都有指向前和指向后的指针；链表中保存了列表的长度；dup、free 和 match 为节点值设置类型特定函数，所以链表可以用于保存各种不同类型的值。而链表中每个节点指向的是 type 为字符串的 redisObject。

压缩列表：压缩列表是 Redis 为了节约内存而开发的，是由一系列特殊编码的**连续内存块**(而不是像双端链表一样每个节点是指针)组成的顺序型数据结构；具体结构相对比较复杂，略。与双端链表相比，压缩列表可以节省内存空间，但是进行修改或增删操作时，复杂度较高；因此当节点数量较少时，可以使用压缩列表；但是节点数量多时，还是使用双端链表划算。

压缩列表不仅用于实现列表，也用于实现哈希、有序列表；使用非常广泛。

### （3）编码转换

只有同时满足下面两个条件时，才会使用压缩列表：列表中元素数量小于 512 个；列表中所有字符串对象都不足 64 字节。如果有一个条件不满足，则使用双端列表；且编码只可能由压缩列表转化为双端链表，反方向则不可能。

下图展示了列表编码转换的特点：

![](https://images2018.cnblogs.com/blog/1174710/201803/1174710-20180327001457636-673470263.png)

其中，单个字符串不能超过 64 字节，是为了便于统一分配每个节点的长度；这里的 64 字节是指字符串的长度，不包括 SDS 结构，因为压缩列表使用连续、定长内存块存储字符串，不需要 SDS 结构指明长度。后面提到压缩列表，也会强调长度不超过 64 字节，原理与这里类似。

## []()3、哈希

### （1）概况

哈希（作为一种数据结构），不仅是 redis 对外提供的 5 种对象类型的一种（与字符串、列表、集合、有序结合并列），也是 Redis 作为 Key-Value 数据库所使用的数据结构。为了说明的方便，在本文后面当使用“内层的哈希”时，代表的是 redis 对外提供的 5 种对象类型的一种；使用“外层的哈希”代指 Redis 作为 Key-Value 数据库所使用的数据结构。

### （2）内部编码

内层的哈希使用的内部编码可以是压缩列表（ziplist）和哈希表（hashtable）两种；Redis 的外层的哈希则只使用了 hashtable。

压缩列表前面已介绍。与哈希表相比，压缩列表用于元素个数少、元素长度小的场景；其优势在于集中存储，节省空间；同时，虽然对于元素的操作复杂度也由 O(1)变为了 O(n)，但由于哈希中元素数量较少，因此操作的时间并没有明显劣势。

hashtable：一个 hashtable 由 1 个 dict 结构、2 个 dictht 结构、1 个 dictEntry 指针数组（称为 bucket）和多个 dictEntry 结构组成。

正常情况下（即 hashtable 没有进行 rehash 时）各部分关系如下图所示：

![](https://images2018.cnblogs.com/blog/1174710/201803/1174710-20180327001627028-325473621.png)

图片改编自：《Redis 设计与实现》

下面从底层向上依次介绍各个部分：

**dictEntry**

dictEntry 结构用于保存键值对，结构定义如下：

```java
typedef struct dictEntry{
    void *key;
    union{
        void *val;
        uint64_tu64;
        int64_ts64;
    }v;
    struct dictEntry *next;
}dictEntry;
```

其中，各个属性的功能如下：

- key：键值对中的键；
- val：键值对中的值，使用 union(即共用体)实现，存储的内容既可能是一个指向值的指针，也可能是 64 位整型，或无符号 64 位整型；
- next：指向下一个 dictEntry，用于解决哈希冲突问题

在 64 位系统中，一个 dictEntry 对象占 24 字节（key/val/next 各占 8 字节）。

**bucket**

bucket 是一个数组，数组的每个元素都是指向 dictEntry 结构的指针。redis 中 bucket 数组的大小计算规则如下：大于 dictEntry 的、最小的 2^n；例如，如果有 1000 个 dictEntry，那么 bucket 大小为 1024；如果有 1500 个 dictEntry，则 bucket 大小为 2048。

**dictht**

dictht 结构如下：

```java
typedef struct dictht{
    dictEntry **table;
    unsigned long size;
    unsigned long sizemask;
    unsigned long used;
}dictht;
```

其中，各个属性的功能说明如下：

- table 属性是一个指针，指向 bucket；
- size 属性记录了哈希表的大小，即 bucket 的大小；
- used 记录了已使用的 dictEntry 的数量；
- sizemask 属性的值总是为 size-1，这个属性和哈希值一起决定一个键在 table 中存储的位置。

**dict**

一般来说，通过使用 dictht 和 dictEntry 结构，便可以实现普通哈希表的功能；但是 Redis 的实现中，在 dictht 结构的上层，还有一个 dict 结构。下面说明 dict 结构的定义及作用。

dict 结构如下：

```java
typedef struct dict{
    dictType *type;
    void *privdata;
    dictht ht[2];
    int trehashidx;
} dict;
```

其中，type 属性和 privdata 属性是为了适应不同类型的键值对，用于创建多态字典。

ht 属性和 trehashidx 属性则用于 rehash，即当哈希表需要扩展或收缩时使用。ht 是一个包含两个项的数组，每项都指向一个 dictht 结构，这也是 Redis 的哈希会有 1 个 dict、2 个 dictht 结构的原因。通常情况下，所有的数据都是存在放 dict 的 ht[0]中，ht[1]只在 rehash 的时候使用。dict 进行 rehash 操作的时候，将 ht[0]中的所有数据 rehash 到 ht[1]中。然后将 ht[1]赋值给 ht[0]，并清空 ht[1]。

因此，Redis 中的哈希之所以在 dictht 和 dictEntry 结构之外还有一个 dict 结构，一方面是为了适应不同类型的键值对，另一方面是为了 rehash。

### （3）编码转换

如前所述，Redis 中内层的哈希既可能使用哈希表，也可能使用压缩列表。

只有同时满足下面两个条件时，才会使用压缩列表：哈希中元素数量小于 512 个；哈希中所有键值对的键和值字符串长度都小于 64 字节。如果有一个条件不满足，则使用哈希表；且编码只可能由压缩列表转化为哈希表，反方向则不可能。

下图展示了 Redis 内层的哈希编码转换的特点：

![](https://images2018.cnblogs.com/blog/1174710/201803/1174710-20180327001855681-1566128865.png)

## []()4、集合

### （1）概况

集合（set）与列表类似，都是用来保存多个字符串，但集合与列表有两点不同：集合中的元素是无序的，因此不能通过索引来操作元素；集合中的元素不能有重复。

一个集合中最多可以存储 2^32-1 个元素；除了支持常规的增删改查，Redis 还支持多个集合取交集、并集、差集。

### （2）内部编码

集合的内部编码可以是整数集合（intset）或哈希表（hashtable）。

哈希表前面已经讲过，这里略过不提；需要注意的是，集合在使用哈希表时，值全部被置为 null。

整数集合的结构定义如下：

```java
typedef struct intset{
    uint32_t encoding;
    uint32_t length;
    int8_t contents[];
} intset;
```

其中，encoding 代表 contents 中存储内容的类型，虽然 contents（存储集合中的元素）是 int8_t 类型，但实际上其存储的值是 int16_t、int32_t 或 int64_t，具体的类型便是由 encoding 决定的；length 表示元素个数。

整数集合适用于集合所有元素都是整数且集合元素数量较小的时候，与哈希表相比，整数集合的优势在于集中存储，节省空间；同时，虽然对于元素的操作复杂度也由 O(1)变为了 O(n)，但由于集合数量较少，因此操作的时间并没有明显劣势。

### （3）编码转换

只有同时满足下面两个条件时，集合才会使用整数集合：集合中元素数量小于 512 个；集合中所有元素都是整数值。如果有一个条件不满足，则使用哈希表；且编码只可能由整数集合转化为哈希表，反方向则不可能。

下图展示了集合编码转换的特点：

![](https://images2018.cnblogs.com/blog/1174710/201803/1174710-20180327001926146-2105183556.png)

## []()5、有序集合

### （1）概况

有序集合与集合一样，元素都不能重复；但与集合不同的是，有序集合中的元素是有顺序的。与列表使用索引下标作为排序依据不同，有序集合为每个元素设置一个分数（score）作为排序依据。

### （2）内部编码

有序集合的内部编码可以是压缩列表（ziplist）或跳跃表（skiplist）。ziplist 在列表和哈希中都有使用，前面已经讲过，这里略过不提。

跳跃表是一种有序数据结构，通过在每个节点中维持多个指向其他节点的指针，从而达到快速访问节点的目的。除了跳跃表，实现有序数据结构的另一种典型实现是平衡树；大多数情况下，跳跃表的效率可以和平衡树媲美，且跳跃表实现比平衡树简单很多，因此 redis 中选用跳跃表代替平衡树。跳跃表支持平均 O(logN)、最坏 O(N)的复杂点进行节点查找，并支持顺序操作。Redis 的跳跃表实现由 zskiplist 和 zskiplistNode 两个结构组成：前者用于保存跳跃表信息（如头结点、尾节点、长度等），后者用于表示跳跃表节点。具体结构相对比较复杂，略。

### （3）编码转换

只有同时满足下面两个条件时，才会使用压缩列表：有序集合中元素数量小于 128 个；有序集合中所有成员长度都不足 64 字节。如果有一个条件不满足，则使用跳跃表；且编码只可能由压缩列表转化为跳跃表，反方向则不可能。

下图展示了有序集合编码转换的特点：

![](https://images2018.cnblogs.com/blog/1174710/201803/1174710-20180327001936290-955216194.png)

# []()五、应用举例

了解 Redis 的内存模型之后，下面通过几个例子说明其应用。

## []()1、估算 Redis 内存使用量

要估算 redis 中的数据占据的内存大小，需要对 redis 的内存模型有比较全面的了解，包括前面介绍的 hashtable、sds、redisobject、各种对象类型的编码方式等。

下面以最简单的字符串类型来进行说明。

假设有 90000 个键值对，每个 key 的长度是 7 个字节，每个 value 的长度也是 7 个字节（且 key 和 value 都不是整数）；下面来估算这 90000 个键值对所占用的空间。在估算占据空间之前，首先可以判定字符串类型使用的编码方式：embstr。

90000 个键值对占据的内存空间主要可以分为两部分：一部分是 90000 个 dictEntry 占据的空间；一部分是键值对所需要的 bucket 空间。

每个 dictEntry 占据的空间包括：

1. 一个 dictEntry，24 字节，jemalloc 会分配 32 字节的内存块

2. 一个 key，7 字节，所以 SDS(key)需要 7+9=16 个字节，jemalloc 会分配 16 字节的内存块

3. 一个 redisObject，16 字节，jemalloc 会分配 16 字节的内存块

4. 一个 value，7 字节，所以 SDS(value)需要 7+9=16 个字节，jemalloc 会分配 16 字节的内存块

5. 综上，一个 dictEntry 需要 32+16+16+16=80 个字节。

bucket 空间：bucket 数组的大小为大于 90000 的最小的 2^n，是 131072；每个 bucket 元素为 8 字节（因为 64 位系统中指针大小为 8 字节）。

因此，可以估算出这 90000 个键值对占据的内存大小为：90000/*80 + 131072/*8 = 8248576。

下面写个程序在 redis 中验证一下：

```java
public class RedisTest {

　　public static Jedis jedis = new Jedis("localhost", 6379);

　　public static void main(String[] args) throws Exception{
　　　　Long m1 = Long.valueOf(getMemory());
　　　　insertData();
　　　　Long m2 = Long.valueOf(getMemory());
　　　　System.out.println(m2 - m1);
　　}

　　public static void insertData(){
　　　　for(int i = 10000; i < 100000; i++){
　　　　　　jedis.set("aa" + i, "aa" + i); //key和value长度都是7字节，且不是整数
　　　　}
　　}

　　public static String getMemory(){
　　　　String memoryAllLine = jedis.info("memory");
　　　　String usedMemoryLine = memoryAllLine.split("\r\n")[1];
　　　　String memory = usedMemoryLine.substring(usedMemoryLine.indexOf(':') + 1);
　　　　return memory;
　　}
}
```

运行结果：8247552

理论值与结果值误差在万分之 1.2，对于计算需要多少内存来说，这个精度已经足够了。之所以会存在误差，是因为在我们插入 90000 条数据之前 redis 已分配了一定的 bucket 空间，而这些 bucket 空间尚未使用。

作为对比将 key 和 value 的长度由 7 字节增加到 8 字节，则对应的 SDS 变为 17 个字节，jemalloc 会分配 32 个字节，因此每个 dictEntry 占用的字节数也由 80 字节变为 112 字节。此时估算这 90000 个键值对占据内存大小为：90000/*112 + 131072/*8 = 11128576。

在 redis 中验证代码如下（只修改插入数据的代码）：

```java
public static void insertData(){
　　for(int i = 10000; i < 100000; i++){
　　　　jedis.set("aaa" + i, "aaa" + i); //key和value长度都是8字节，且不是整数
　　}
}
```

运行结果：11128576；估算准确。

对于字符串类型之外的其他类型，对内存占用的估算方法是类似的，需要结合具体类型的编码方式来确定。

## []()2、优化内存占用

了解 redis 的内存模型，对优化 redis 内存占用有很大帮助。下面介绍几种优化场景。

（1）利用 jemalloc 特性进行优化

上一小节所讲述的 90000 个键值便是一个例子。由于 jemalloc 分配内存时数值是不连续的，因此 key/value 字符串变化一个字节，可能会引起占用内存很大的变动；在设计时可以利用这一点。

例如，如果 key 的长度如果是 8 个字节，则 SDS 为 17 字节，jemalloc 分配 32 字节；此时将 key 长度缩减为 7 个字节，则 SDS 为 16 字节，jemalloc 分配 16 字节；则每个 key 所占用的空间都可以缩小一半。

（2）使用整型/长整型

如果是整型/长整型，Redis 会使用 int 类型（8 字节）存储来代替字符串，可以节省更多空间。因此在可以使用长整型/整型代替字符串的场景下，尽量使用长整型/整型。

（3）共享对象

利用共享对象，可以减少对象的创建（同时减少了 redisObject 的创建），节省内存空间。目前 redis 中的共享对象只包括 10000 个整数（0-9999）；可以通过调整 REDIS_SHARED_INTEGERS 参数提高共享对象的个数；例如将 REDIS_SHARED_INTEGERS 调整到 20000，则 0-19999 之间的对象都可以共享。

考虑这样一种场景：论坛网站在 redis 中存储了每个帖子的浏览数，而这些浏览数绝大多数分布在 0-20000 之间，这时候通过适当增大 REDIS_SHARED_INTEGERS 参数，便可以利用共享对象节省内存空间。

（4）避免过度设计

然而需要注意的是，不论是哪种优化场景，都要考虑内存空间与设计复杂度的权衡；而设计复杂度会影响到代码的复杂度、可维护性。

如果数据量较小，那么为了节省内存而使得代码的开发、维护变得更加困难并不划算；还是以前面讲到的 90000 个键值对为例，实际上节省的内存空间只有几 MB。但是如果数据量有几千万甚至上亿，考虑内存的优化就比较必要了。

## []()3、关注内存碎片率

内存碎片率是一个重要的参数，对 redis 内存的优化有重要意义。

如果内存碎片率过高（jemalloc 在 1.03 左右比较正常），说明内存碎片多，内存浪费严重；这时便可以考虑重启 redis 服务，在内存中对数据进行重排，减少内存碎片。

如果内存碎片率小于 1，说明 redis 内存不足，部分数据使用了虚拟内存（即 swap）；由于虚拟内存的存取速度比物理内存差很多（2-3 个数量级），此时 redis 的访问速度可能会变得很慢。因此必须设法增大物理内存（可以增加服务器节点数量，或提高单机内存），或减少 redis 中的数据。

要减少 redis 中的数据，除了选用合适的数据类型、利用共享对象等，还有一点是要设置合理的数据回收策略（maxmemory-policy），当内存达到一定量后，根据不同的优先级对内存进行回收。

# []()六、参考文献

《Redis 开发与运维》

《Redis 设计与实现》

https://redis.io/documentation

http://redisdoc.com/server/info.html

https://www.cnblogs.com/lhcpig/p/4769397.html

https://searchdatabase.techtarget.com.cn/7-20218/

http://www.cnblogs.com/mushroom/p/4738170.html

http://www.imooc.com/article/3645

http://blog.csdn.net/zhengpeitao/article/details/76573053

**创作不易，如果文章对你有帮助，就点个赞、评个论呗~**

**创作不易，如果文章对你有帮助，就点个赞、评个论呗~**

**创作不易，如果文章对你有帮助，就点个赞、评个论呗~**

分类: [Redis](https://www.cnblogs.com/kismetv/category/1186633.html)

标签: [redis](https://www.cnblogs.com/kismetv/tag/redis/), [内存模型](https://www.cnblogs.com/kismetv/tag/%E5%86%85%E5%AD%98%E6%A8%A1%E5%9E%8B/), [对象类型](https://www.cnblogs.com/kismetv/tag/%E5%AF%B9%E8%B1%A1%E7%B1%BB%E5%9E%8B/), [内存估算](https://www.cnblogs.com/kismetv/tag/%E5%86%85%E5%AD%98%E4%BC%B0%E7%AE%97/), [内存优化](https://www.cnblogs.com/kismetv/tag/%E5%86%85%E5%AD%98%E4%BC%98%E5%8C%96/), [jemalloc](https://www.cnblogs.com/kismetv/tag/jemalloc/), [SDS](https://www.cnblogs.com/kismetv/tag/SDS/), [RedisObject](https://www.cnblogs.com/kismetv/tag/RedisObject/), [内存碎片](https://www.cnblogs.com/kismetv/tag/%E5%86%85%E5%AD%98%E7%A2%8E%E7%89%87/)
[好文要顶]() [关注我]() [收藏该文]() [![](https://common.cnblogs.com/images/icon_weibo_24.png)]("分享至新浪微博") [![](https://common.cnblogs.com/images/wechat.png)]("分享至微信")

[![](https://pic.cnblogs.com/face/1174710/20180329153616.png)](https://home.cnblogs.com/u/kismetv/)

[编程迷思](https://home.cnblogs.com/u/kismetv/)
[关注 - 2](https://home.cnblogs.com/u/kismetv/followees/)
[粉丝 - 679](https://home.cnblogs.com/u/kismetv/followers/)

[+加关注]()
200

0

[«](https://www.cnblogs.com/kismetv/p/7806063.html) 上一篇： [详解 tomcat 的连接数与线程池](https://www.cnblogs.com/kismetv/p/7806063.html '发布于 2017-11-09 08:51')
[»](https://www.cnblogs.com/kismetv/p/8757260.html) 下一篇： [Spring 中获取 request 的几种方法，及其线程安全性分析](https://www.cnblogs.com/kismetv/p/8757260.html '发布于 2018-04-10 08:26')

posted @ 2018-03-27 08:53 [编程迷思](https://www.cnblogs.com/kismetv/) 阅读(41388) 评论(77) [编辑](https://i.cnblogs.com/EditPosts.aspx?postid=8654978) [收藏]()
