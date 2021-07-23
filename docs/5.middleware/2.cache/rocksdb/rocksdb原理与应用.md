# RocksDB 原理及应用

## RocksDB 简介

LSM 类存储引擎、数据库之一。所谓 LSM，一般的名字叫 Log Structured-Merge Tree（日志结构合并树），来源于分布式数据库领域，
也是 BigTable 的论文中所使用的文件组织方式。它的特点在于写入的时候是 append only 的形式，就像名字所显示的那样，跟日志一样只在文件后面追加。
LSM 树结构的问题: 写入速度快，读取速度慢，写放大和读放大都较高。

Rocksdb 本身支持单个 kv 的读写和批量 kv 值的读写。由于 LSM 的出身，它专注于利用 LSM 树的特性，适应有序、层次化的磁盘读写。
在 LSM 树之上构建了 Rocksdb，而在 RocksDB 之上同样有一些更面向应用层的数据库，包括分布式数据库、查询引擎、大数据存储引擎、图数据库如 Janusgraph 等。

## 一、RocksDB 的读写层次和数据结构

rocksdb 的数据写入结构如下：
在内存有 memtable，
磁盘有 WAL 文件目录和 SST 文件目录。

![](https://img2020.cnblogs.com/blog/891812/202006/891812-20200626182947653-1798359026.png)

- memtable 和 SST 文件

内存中的数据和 SST 文件组成了 RocksDB 数据的全集。
rocksdb 中的数据结构有三种，分别是 skiplist、hash-skiplist、hash-linklist；跟 leveldb 不同。跳表的好处在于插入的时候可以保证数据的有序，
支持二分查找、范围查询。当然，删除的时候不是立即删除，因为会影响到数据的写放大，一般是在 compact 阶段进行真正的删除。

hash-skiplist 的索引既有 hash 索引，又有 skiplit 的二分索引，针对于有明确 key 或教完整 key 前缀的查询，如果要进行全表扫描，会非常耗费内存及低性能，
因为要产生临时的有序表； hash-linklist 也是一样的道理。

对于 hash-skiplist, 读取时候的缓存分片如下：

![](https://img2020.cnblogs.com/blog/891812/202006/891812-20200626182957814-1815441846.png)

- SST 文件结构类型

SSTable 是一种数据结构，当 memtable 达到一定上限之后，会 flush 到 disk 形成 immutable Sorted String Table (SSTable)；
Rocksdb 写入数据，即 put 的时候，在磁盘中保存 level 层次树，在单个 level 中超出大小的时候进行合并；合并的时候是支持多线程并发的；

![](https://img2020.cnblogs.com/blog/891812/202006/891812-20200626183005269-238658884.png)

- 紧凑 compact 操作

compact，紧凑，可译作压缩。

为了提高读性能，一般会做 compact 操作。compact 操作将数据根据 key 进行合并，删除了无效数据，缩短了读取路径，优化了范围查询。

把内存里面不可变的 Memtable 给 dump 到到硬盘上的 SSTable 层中，叫做 minor compaction。

SStable 里的合并称为 major compaction。由于 major compaction 耗费 CPU 和磁盘性能，HBase 中需要在高峰时段禁用 major compaction。

写放大：一份数据被顺序写几次（还是那一份数据，只是需要一直往下流）。第一次写到 L0，之后由于 compaction 可能在 L1~LN 各写一次

读放大：一个 query 会在多个 sstable，而这些 sstable 分布在 L0~LN

空间放大：一份原始数据被 copy/overwritten（一份用于服务 query，一份用于 snapshot 之后用于 compaction）

下面是一些典型的 Compaction 技巧，可以看到，没有写放大，读放大，空间放大三者都很完美的方案:

 Leveled Compaction

全部层级都按照标准的从上到下进行层级合并
读写放大都比较严重，但是空间放大较低
在这篇论文（Towards Accurate and Fast Evaluation of Multi-Stage Log-Structured Designs）中有详细的阐述
 Tiered Compaction

即 RocksDB 中的 Universal Compaction
空间放大和读放大严重，但是写放大最小
在这篇论文（Dostoevsky: Better Space-Time Trade-Offs for LSM-Tree Based Key-Value Stores via Adaptive Removal of Superfluous Merging）
有详细的阐述
 Tiered+Leveled Compaction

即 RocksDB 中的 Level Compaction
是一个混合的 Compaction 策略，空间放大比 Tiered Compaction 更低，写放大也比 Leveled 低
 Leveled-N Compaction

比 Leveled Compaction 写放大更低，读放大更高
一次合并 N - 1 层到第 N 层

## 二、分布式 Rocksdb 的改造

Read-Only 模式下可以读取数据，但是数据并不一定是最新的；Read Secondary Instance 模式下可以读到最新的数据，实现方式是 redo WAL（重放日志）；

可用性
多节点下可用性比较重要；一般是通过 WAL 实现重放，但是 WAL 仍然会遇到磁盘损坏的问题。常用的，拍脑门能想出来的解决方式是增加数据一致性的模块；

数据一致性
分布式数据库的一致性协议需要共识协议作为基础，rocksdb 之上加入一层 raft 协议可以实现分布式一致性，实现该协议也是为了尽可能地提高可用性，
尽管在系统陷入到节点数超过一半挂掉的情况是无法保证完好的一致性的。

但是这样子会加深写放大的问题。对此可以使用写放大较低的 compact 策略缓解；对于系统层面，如果不想写放大太多，也可以减少 raft 写及数据备份，
直接将读节点的比例增加。(都不是完美的方案)

事务处理
在分布式数据库中自然而然会遇到事务的处理，2pc 协议是分布式事务的一个解决方案；但是存在性能不够好，单点故障的问题，宕机导致数据不一致的问题；
实际使用中，常使用 XA 协议，采用 2PC 两段式提交作为基础，是 X/Open DTP 组织（X/Open DTP group）定义的两阶段提交协议。

高层应用对 Rocksdb 的封装

### 1.MVCC

对于 DBMS 来说，常用到 MVCC 进行并发情况下的快速读写和隔离，MVCC 会需要扫描同一个 key 下多个 timestamp 下的值，
但是由于 LSM 的特性，SCAN 操作的时候每个 key 可能出现在 SSTable 的任意层次，所以读放大明显。
一般采用 bloom filter 降低 IO 读写次数，但是在长 key 造成很大的 key 空间的情况下，这种方法也捉襟见肘。CockroachDB 采用了 prefix 前缀 bloom filter 来缓解这个问题。

### 2.Backwards iteration

后向遍历在其他 key-value db 上一般效率比 forward iteration 慢，但是 Rocksdb 有对其进行优化。

### 3.Graph Database

图数据库 Dgraph 利用 Rocksdb 作为(Predicate, Subject) --> PostingList 的 Key-Val 存储，即图中的一个有向边。
其次，利用 Rocksdb 的单 key 随机读写性能，建立 PostingList 的索引。

## 三、Rocksdb 可以改进的问题

读写放大严重；

应对突发流量的时候削峰能力不足；

压缩率有限；

索引效率较低；

scan 效率慢；

## References

https://www.cnblogs.com/wangzming/p/12969599.html

https://cloud.tencent.com/developer/article/1441835深入理解LSM-Tree

https://zhuanlan.zhihu.com/p/49966056 看图了解 RocksDB

https://www.jianshu.com/p/8de55d5df05e LSM Compaction Strategy

https://docs.scylladb.com/kb/compaction/ Compaction

https://wanghenshui.github.io/2019/03/12/rocksdb-2pc-xa-transcation

http://alexstocks.github.io/html/rocksdb.html RocksDB 笔记

https://zhuanlan.zhihu.com/p/148941340 Rocksdb Secondary Instance 启发与实践

https://www.cockroachlabs.com/blog/cockroachdb-on-rocksd/
