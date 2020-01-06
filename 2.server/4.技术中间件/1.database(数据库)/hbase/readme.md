### 详解HBase架构原理

#### 一、什么是HBase

HBase是一个高可靠、高性能、面向列、可伸缩的分布式存储系统，利用HBase技术可在廉价的PC Server上搭建大规模结构化存储集群。

HBase是Google BigTable的开源实现，与Google BigTable利用GFS作为其文件存储系统类似，HBase利用Hadoop HDFS作为其文件存储系统；

Google运行MapReduce来处理BigTable中的海量数据，HBase同样利用Hadoop MapReduce来处理HBase中的海量数据；

Google BigTable利用Chubby作为协同服务，HBase利用Zookeeper作为协同服务。

#### 二、HBase设计模型

HBase中的每一张表就是所谓的BigTable。BigTable会存储一系列的行记录，行记录有三个基本类型的定义：

1.RowKey

是行在BigTable中的唯一标识。

2.TimeStamp：

是每一次数据操作对应关联的时间戳，可以看作SVN的版本。

3.Column：

定义为<family>:<label>，通过这两部分可以指定唯一的数据的存储列，family的定义和修改需要对HBase进行类似于DB的DDL操作，

而label，不需要定义直接可以使用，这也为动态定制列提供了一种手段。family另一个作用体现在物理存储优化读写操作上，同family

的数据物理上保存的会比较接近，因此在业务设计的过程中可以利用这个特性。

1. 逻辑存储模型

HBase以表的形式存储数据，表由行和列组成。列划分为若干个列簇，如下图所示：

![](http://www.uml.org.cn/bigdata/images/201810181.png)

下面是对表中元素的详细解析：

RowKey

与NoSQL数据库一样，rowkey是用来检索记录的主键。访问HBase Table中的行，只有三种方式：

1.通过单个rowkey访问

2.通过rowkey的range

3.全表扫描

rowkey行键可以任意字符串（最大长度64KB，实际应用中长度一般为10-100bytes），在HBase内部RowKey保存为字节数组。

存储时，数据按照RowKey的字典序（byte order）排序存储，设计key时，要充分了解这个特性，将经常一起读取的行存放在一起。

需要注意的是：行的一次读写是原子操作（不论一次读写多少列）

列簇

HBase表中的每个列，都归属于某个列簇，列簇是表的schema的一部分（而列不是），必须在使用表之前定义。列名都以列簇作为前缀。例如：

courses:history, courses:math 都属于 courses 这个列簇。

访问控制，磁盘和内存的使用统计都是在列簇层面进行的。

实际应用中，列簇上的控制权限能帮助我们管理不同类型的应用：我们允许一些应用可以添加新的基本数据、

一些应用可以读取基本数据并创建继承的列簇、一些应用则只允许浏览数据（设置可能因为隐私的原因不能浏览所有数据）。

时间戳

HBase中通过row和columns确定的为一个存储单元称为cell。每个cell都保存着同一份数据的多个版本。版本通过时间戳来索引。

时间戳的类型是64位整型。时间戳可以由HBase在写入时自动赋值，此时时间戳是精确到毫秒的当前系统时间。时间戳也可以由客户显示赋值。

如果应用程序要避免数据版本冲突，就必须自己生成具有唯一性的时间戳。每个cell中在不同版本的数据按照时间倒序排序，即最新的数据排在最前面。

为了避免数据存在过多的版本造成的管理负担，HBase提供了两种数据版本回收方式。一是保存数据的最后n个版本，二是保存最近一段时间内的版本

（比如最近七天）。用户可以针对每个列簇进行设置。

Cell

由{row key, column(=+), version} 唯一确定的单元。cell中的数据是没有类型的，全部是字节码形式存储。

2. 物理存储模型

Table在行的方向上分割为多个HRegion，每个HRegion分散在不同的RegionServer中。

![](http://www.uml.org.cn/bigdata/images/201810182.jpg)

每个HRegion由多个Store构成，每个Store由一个MemStore和0或多个StoreFile组成，每个Store保存一个Columns Family

![](http://www.uml.org.cn/bigdata/images/201810183.png)

StoreFile以HFile格式存储在HDFS中。

#### 三、HBase存储架构

从HBase的架构图上可以看出，HBase中的存储包括HMaster、HRegionSever、HRegion、HLog、Store、MemStore、StoreFile、HFile等，以下是HBase存储架构图：

![](http://www.uml.org.cn/bigdata/images/201810184.png)

HBase中的每张表都通过键按照一定的范围被分割成多个子表（HRegion），默认一个HRegion超过256M就要被分割成两个，这个过程由HRegionServer管理，

而HRegion的分配由HMaster管理。

HMaster的作用：

1.为HRegionServer分配HRegion

2.负责HRegionServer的负载均衡

3.发现失效的HRegionServer并重新分配

4.HDFS上的垃圾文件回收

5.处理Schema更新请求

HRegionServer的作用：

1.维护HMaster分配给它的HRegion，处理对这些HRegion的IO请求

2.负责切分正在运行过程中变得过大的HRegion

可以看到，Client访问HBase上的数据并不需要HMaster参与，寻址访问ZooKeeper和HRegionServer，数据读写访问HRegionServer，

HMaster仅仅维护Table和Region的元数据信息，Table的元数据信息保存在ZooKeeper上，负载很低。HRegionServer存取一个子表时，

会创建一个HRegion对象，然后对表的每个列簇创建一个Store对象，每个Store都会有一个MemStore和0或多个StoreFile与之对应，

每个StoreFile都会对应一个HFile，HFile就是实际的存储文件。因此，一个HRegion有多少列簇就有多少个Store。

一个HRegionServer会有多个HRegion和一个HLog。

HRegion

Table在行的方向上分割为多个HRegion，HRegion是HBase中分布式存储和负载均衡的最小单元，即不同的HRegion可以分别在不同的HRegionServer上，

但同一个HRegion是不会拆分到多个HRegionServer上的。HRegion按大小分割，每个表一般只有一个HRegion，随着数据不断插入表，HRegion不断增大，

当HRegion的某个列簇达到一个阀值（默认256M）时就会分成两个新的HRegion。

1、<表名，StartRowKey, 创建时间>

2、由目录表(-ROOT-和.META.)记录该Region的EndRowKey

HRegion定位：HRegion被分配给哪个HRegionServer是完全动态的，所以需要机制来定位HRegion具体在哪个HRegionServer，HBase使用三层结构来定位HRegion：

1、通过zk里的文件/hbase/rs得到-ROOT-表的位置。-ROOT-表只有一个region。

2、通过-ROOT-表查找.META.表的第一个表中相应的HRegion位置。其实-ROOT-表是.META.表的第一个region；

.META.表中的每一个Region在-ROOT-表中都是一行记录。

3、通过.META.表找到所要的用户表HRegion的位置。用户表的每个HRegion在.META.表中都是一行记录。

-ROOT-表永远不会被分隔为多个HRegion，保证了最多需要三次跳转，就能定位到任意的region。Client会将查询的位置信息保存缓存起来，缓存不会主动失效，

因此如果Client上的缓存全部失效，则需要进行6次网络来回，才能定位到正确的HRegion，其中三次用来发现缓存失效，另外三次用来获取位置信息。

Store

每一个HRegion由一个或多个Store组成，至少是一个Store，HBase会把一起访问的数据放在一个Store里面，即为每个ColumnFamily建一个Store，

如果有几个ColumnFamily，也就有几个Store。一个Store由一个MemStore和0或者多个StoreFile组成。 HBase以Store的大小来判断是否需要切分HRegion。

MemStore

MemStore 是放在内存里的，保存修改的数据即keyValues。当MemStore的大小达到一个阀值（默认64MB）时，MemStore会被Flush到文件，

即生成一个快照。目前HBase会有一个线程来负责MemStore的Flush操作。

StoreFile

MemStore内存中的数据写到文件后就是StoreFile，StoreFile底层是以HFile的格式保存。

HFile

HBase中KeyValue数据的存储格式，是Hadoop的二进制格式文件。 首先HFile文件是不定长的，长度固定的只有其中的两块：Trailer和FileInfo。

Trailer中有指针指向其他数据块的起始点，FileInfo记录了文件的一些meta信息。Data Block是HBase IO的基本单元，为了提高效率，

HRegionServer中有基于LRU的Block Cache机制。每个Data块的大小可以在创建一个Table的时候通过参数指定（默认块大小64KB），

大号的Block有利于顺序Scan，小号的Block利于随机查询。每个Data块除了开头的Magic以外就是一个个KeyValue对拼接而成，

Magic内容就是一些随机数字，目的是防止数据损坏，结构如下。

![](http://www.uml.org.cn/bigdata/images/201810185.png)

HFile结构图如下：

![](http://www.uml.org.cn/bigdata/images/201810186.png)

Data Block段用来保存表中的数据，这部分可以被压缩。 Meta Block段（可选的）用来保存用户自定义的kv段，可以被压缩。 FileInfo段用来保存HFile的元信息，不能被压缩，用户也可以在这一部分添加自己的元信息。 Data Block Index段（可选的）用来保存Meta Blcok的索引。 Trailer这一段是定长的。保存了每一段的偏移量，读取一个HFile时，会首先读取Trailer，Trailer保存了每个段的起始位置(段的Magic Number用来做安全check)，然后，DataBlock Index会被读取到内存中，这样，当检索某个key时，不需要扫描整个HFile，而只需从内存中找到key所在的block，通过一次磁盘io将整个 block读取到内存中，再找到需要的key。DataBlock Index采用LRU机制淘汰。 HFile的Data Block，Meta Block通常采用压缩方式存储，压缩之后可以大大减少网络IO和磁盘IO，随之而来的开销当然是需要花费cpu进行压缩和解压缩。（备注： DataBlock Index的缺陷。 a) 占用过多内存　b) 启动加载时间缓慢）

HLog

HLog(WAL log)：WAL意为write ahead log，用来做灾难恢复使用，HLog记录数据的所有变更，一旦region server 宕机，就可以从log中进行恢复。

LogFlusher

定期的将缓存中信息写入到日志文件中

LogRoller　

对日志文件进行管理维护 ![](http://www.uml.org.cn/images/2star.png) 

参考：
http://www.uml.org.cn/bigdata/201810181.asp