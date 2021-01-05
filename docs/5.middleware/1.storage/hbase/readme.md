### 详解 HBase 架构原理

#### 一、什么是 HBase

HBase 是一个高可靠、高性能、面向列、可伸缩的分布式存储系统，利用 HBase 技术可在廉价的 PC Server 上搭建大规模结构化存储集群。

HBase 是 Google BigTable 的开源实现，与 Google BigTable 利用 GFS 作为其文件存储系统类似，HBase 利用 Hadoop HDFS 作为其文件存储系统；

Google 运行 MapReduce 来处理 BigTable 中的海量数据，HBase 同样利用 Hadoop MapReduce 来处理 HBase 中的海量数据；

Google BigTable 利用 Chubby 作为协同服务，HBase 利用 Zookeeper 作为协同服务。

#### 二、HBase 设计模型

HBase 中的每一张表就是所谓的 BigTable。BigTable 会存储一系列的行记录，行记录有三个基本类型的定义：

1.RowKey

是行在 BigTable 中的唯一标识。

2.TimeStamp：

是每一次数据操作对应关联的时间戳，可以看作 SVN 的版本。

3.Column：

定义为<family>:<label>，通过这两部分可以指定唯一的数据的存储列，family 的定义和修改需要对 HBase 进行类似于 DB 的 DDL 操作，

而 label，不需要定义直接可以使用，这也为动态定制列提供了一种手段。family 另一个作用体现在物理存储优化读写操作上，同 family

的数据物理上保存的会比较接近，因此在业务设计的过程中可以利用这个特性。

1. 逻辑存储模型

HBase 以表的形式存储数据，表由行和列组成。列划分为若干个列簇，如下图所示：

![](http://www.uml.org.cn/bigdata/images/201810181.png)

下面是对表中元素的详细解析：

RowKey

与 NoSQL 数据库一样，rowkey 是用来检索记录的主键。访问 HBase Table 中的行，只有三种方式：

1.通过单个 rowkey 访问

2.通过 rowkey 的 range

3.全表扫描

rowkey 行键可以任意字符串（最大长度 64KB，实际应用中长度一般为 10-100bytes），在 HBase 内部 RowKey 保存为字节数组。

存储时，数据按照 RowKey 的字典序（byte order）排序存储，设计 key 时，要充分了解这个特性，将经常一起读取的行存放在一起。

需要注意的是：行的一次读写是原子操作（不论一次读写多少列）

列簇

HBase 表中的每个列，都归属于某个列簇，列簇是表的 schema 的一部分（而列不是），必须在使用表之前定义。列名都以列簇作为前缀。例如：

courses:history, courses:math 都属于 courses 这个列簇。

访问控制，磁盘和内存的使用统计都是在列簇层面进行的。

实际应用中，列簇上的控制权限能帮助我们管理不同类型的应用：我们允许一些应用可以添加新的基本数据、

一些应用可以读取基本数据并创建继承的列簇、一些应用则只允许浏览数据（设置可能因为隐私的原因不能浏览所有数据）。

时间戳

HBase 中通过 row 和 columns 确定的为一个存储单元称为 cell。每个 cell 都保存着同一份数据的多个版本。版本通过时间戳来索引。

时间戳的类型是 64 位整型。时间戳可以由 HBase 在写入时自动赋值，此时时间戳是精确到毫秒的当前系统时间。时间戳也可以由客户显示赋值。

如果应用程序要避免数据版本冲突，就必须自己生成具有唯一性的时间戳。每个 cell 中在不同版本的数据按照时间倒序排序，即最新的数据排在最前面。

为了避免数据存在过多的版本造成的管理负担，HBase 提供了两种数据版本回收方式。一是保存数据的最后 n 个版本，二是保存最近一段时间内的版本

（比如最近七天）。用户可以针对每个列簇进行设置。

Cell

由{row key, column(=+), version} 唯一确定的单元。cell 中的数据是没有类型的，全部是字节码形式存储。

2. 物理存储模型

Table 在行的方向上分割为多个 HRegion，每个 HRegion 分散在不同的 RegionServer 中。

![](http://www.uml.org.cn/bigdata/images/201810182.jpg)

每个 HRegion 由多个 Store 构成，每个 Store 由一个 MemStore 和 0 或多个 StoreFile 组成，每个 Store 保存一个 Columns Family

![](http://www.uml.org.cn/bigdata/images/201810183.png)

StoreFile 以 HFile 格式存储在 HDFS 中。

#### 三、HBase 存储架构

从 HBase 的架构图上可以看出，HBase 中的存储包括 HMaster、HRegionSever、HRegion、HLog、Store、MemStore、StoreFile、HFile 等，以下是 HBase 存储架构图：

![](http://www.uml.org.cn/bigdata/images/201810184.png)

HBase 中的每张表都通过键按照一定的范围被分割成多个子表（HRegion），默认一个 HRegion 超过 256M 就要被分割成两个，这个过程由 HRegionServer 管理，

而 HRegion 的分配由 HMaster 管理。

HMaster 的作用：

1.为 HRegionServer 分配 HRegion

2.负责 HRegionServer 的负载均衡

3.发现失效的 HRegionServer 并重新分配

4.HDFS 上的垃圾文件回收

5.处理 Schema 更新请求

HRegionServer 的作用：

1.维护 HMaster 分配给它的 HRegion，处理对这些 HRegion 的 IO 请求

2.负责切分正在运行过程中变得过大的 HRegion

可以看到，Client 访问 HBase 上的数据并不需要 HMaster 参与，寻址访问 ZooKeeper 和 HRegionServer，数据读写访问 HRegionServer，

HMaster 仅仅维护 Table 和 Region 的元数据信息，Table 的元数据信息保存在 ZooKeeper 上，负载很低。HRegionServer 存取一个子表时，

会创建一个 HRegion 对象，然后对表的每个列簇创建一个 Store 对象，每个 Store 都会有一个 MemStore 和 0 或多个 StoreFile 与之对应，

每个 StoreFile 都会对应一个 HFile，HFile 就是实际的存储文件。因此，一个 HRegion 有多少列簇就有多少个 Store。

一个 HRegionServer 会有多个 HRegion 和一个 HLog。

HRegion

Table 在行的方向上分割为多个 HRegion，HRegion 是 HBase 中分布式存储和负载均衡的最小单元，即不同的 HRegion 可以分别在不同的 HRegionServer 上，

但同一个 HRegion 是不会拆分到多个 HRegionServer 上的。HRegion 按大小分割，每个表一般只有一个 HRegion，随着数据不断插入表，HRegion 不断增大，

当 HRegion 的某个列簇达到一个阀值（默认 256M）时就会分成两个新的 HRegion。

1、<表名，StartRowKey, 创建时间>

2、由目录表(-ROOT-和.META.)记录该 Region 的 EndRowKey

HRegion 定位：HRegion 被分配给哪个 HRegionServer 是完全动态的，所以需要机制来定位 HRegion 具体在哪个 HRegionServer，HBase 使用三层结构来定位 HRegion：

1、通过 zk 里的文件/hbase/rs 得到-ROOT-表的位置。-ROOT-表只有一个 region。

2、通过-ROOT-表查找.META.表的第一个表中相应的 HRegion 位置。其实-ROOT-表是.META.表的第一个 region；

.META.表中的每一个 Region 在-ROOT-表中都是一行记录。

3、通过.META.表找到所要的用户表 HRegion 的位置。用户表的每个 HRegion 在.META.表中都是一行记录。

-ROOT-表永远不会被分隔为多个 HRegion，保证了最多需要三次跳转，就能定位到任意的 region。Client 会将查询的位置信息保存缓存起来，缓存不会主动失效，

因此如果 Client 上的缓存全部失效，则需要进行 6 次网络来回，才能定位到正确的 HRegion，其中三次用来发现缓存失效，另外三次用来获取位置信息。

Store

每一个 HRegion 由一个或多个 Store 组成，至少是一个 Store，HBase 会把一起访问的数据放在一个 Store 里面，即为每个 ColumnFamily 建一个 Store，

如果有几个 ColumnFamily，也就有几个 Store。一个 Store 由一个 MemStore 和 0 或者多个 StoreFile 组成。 HBase 以 Store 的大小来判断是否需要切分 HRegion。

MemStore

MemStore 是放在内存里的，保存修改的数据即 keyValues。当 MemStore 的大小达到一个阀值（默认 64MB）时，MemStore 会被 Flush 到文件，

即生成一个快照。目前 HBase 会有一个线程来负责 MemStore 的 Flush 操作。

StoreFile

MemStore 内存中的数据写到文件后就是 StoreFile，StoreFile 底层是以 HFile 的格式保存。

HFile

HBase 中 KeyValue 数据的存储格式，是 Hadoop 的二进制格式文件。 首先 HFile 文件是不定长的，长度固定的只有其中的两块：Trailer 和 FileInfo。

Trailer 中有指针指向其他数据块的起始点，FileInfo 记录了文件的一些 meta 信息。Data Block 是 HBase IO 的基本单元，为了提高效率，

HRegionServer 中有基于 LRU 的 Block Cache 机制。每个 Data 块的大小可以在创建一个 Table 的时候通过参数指定（默认块大小 64KB），

大号的 Block 有利于顺序 Scan，小号的 Block 利于随机查询。每个 Data 块除了开头的 Magic 以外就是一个个 KeyValue 对拼接而成，

Magic 内容就是一些随机数字，目的是防止数据损坏，结构如下。

![](http://www.uml.org.cn/bigdata/images/201810185.png)

HFile 结构图如下：

![](http://www.uml.org.cn/bigdata/images/201810186.png)

Data Block 段用来保存表中的数据，这部分可以被压缩。 Meta Block 段（可选的）用来保存用户自定义的 kv 段，可以被压缩。 FileInfo 段用来保存 HFile 的元信息，不能被压缩，用户也可以在这一部分添加自己的元信息。 Data Block Index 段（可选的）用来保存 Meta Blcok 的索引。 Trailer 这一段是定长的。保存了每一段的偏移量，读取一个 HFile 时，会首先读取 Trailer，Trailer 保存了每个段的起始位置(段的 Magic Number 用来做安全 check)，然后，DataBlock Index 会被读取到内存中，这样，当检索某个 key 时，不需要扫描整个 HFile，而只需从内存中找到 key 所在的 block，通过一次磁盘 io 将整个 block 读取到内存中，再找到需要的 key。DataBlock Index 采用 LRU 机制淘汰。 HFile 的 Data Block，Meta Block 通常采用压缩方式存储，压缩之后可以大大减少网络 IO 和磁盘 IO，随之而来的开销当然是需要花费 cpu 进行压缩和解压缩。（备注： DataBlock Index 的缺陷。 a) 占用过多内存　 b) 启动加载时间缓慢）

HLog

HLog(WAL log)：WAL 意为 write ahead log，用来做灾难恢复使用，HLog 记录数据的所有变更，一旦 region server 宕机，就可以从 log 中进行恢复。

LogFlusher

定期的将缓存中信息写入到日志文件中

LogRoller

对日志文件进行管理维护 ![](http://www.uml.org.cn/images/2star.png)

参考：
http://www.uml.org.cn/bigdata/201810181.asp
