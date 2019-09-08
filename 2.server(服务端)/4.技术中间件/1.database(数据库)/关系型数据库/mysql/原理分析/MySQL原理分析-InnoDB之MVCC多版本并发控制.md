### MySQL-InnoDB-MVCC多版本并发控制


#### MVCC

(Multiversion Concurrency Control)1.先引用《高性能MySQL》中对MVCC的部分介绍

* MySQL的大多数事务型存储引擎实现的其实都不是简单的行级锁。**基于提升并发性能的考虑**, 它们一般都同时实现了多版本并发控制(MVCC)。不仅是MySQL, 包括Oracle,PostgreSQL等其他数据库系统也都实现了MVCC, 但各自的实现机制不尽相同, 因为MVCC没有一个统一的实现标准。
* 可以认为MVCC是行级锁的一个变种, 但是它在很多情况下避免了加锁操作, 因此开销更低。虽然实现机制有所不同, 但大都实现了非阻塞的读操作，写操作也只锁定必要的行。
* MVCC的实现方式有多种, 典型的有乐观(optimistic)并发控制 和 悲观(pessimistic)并发控制。
* MVCC只在READ COMMITTED和REPEATABLE READ两个隔离级别下工作。
    其他两个隔离级别够和MVCC不兼容, 
    因为READ UNCOMMITTED总是读取最新的数据行, 而不是符合当前事务版本的数据行。
    而SERIALIZABLE则会对所有读取的行都加锁。

2.可以了解到:

* MVCC是被Mysql中

事务型存储引擎InnoDB
所支持的;
* **应对高并发事务, MVCC比单纯的加行锁更有效, 开销更小**;
* MVCC只在

READ COMMITTED
和

REPEATABLE READ
两个隔离级别下工作;
* MVCC可以使用

乐观(optimistic)锁
和

悲观(pessimistic)锁
来实现;

3.另外, 《高性能Mysql》中提到, InnoDB的MVCC是通过在每行记录后面保存**两个隐藏的列**来实现的..... 这个貌似和网上很多观点不同, 具体可以参考[MySQL官方对InnoDB-MVCC的解释](https://link.juejin.im?target=https%3A%2F%2Fdev.mysql.com%2Fdoc%2Frefman%2F5.7%2Fen%2Finnodb-multi-versioning.html)
**可以看到, InnoDB存储引擎在数据库每行数据的后面添加了三个字段, 不是两个!!**

## 分析

1.InnoDB存储引擎在数据库每行数据的后面添加了三个字段

* 6字节的事务ID(

DB_TRX_ID
)字段: 标记了最新更新这条行记录的transaction id，每处理一个事务，其值自动+1
另外，删除在内部被视为一个更新，其中行中的特殊位被设置为将其标记为已删除
* 7字节的回滚指针(

DB_ROLL_PTR
)字段 : 指向当前记录项的rollback segment的

undo log
(撤销日志记录), 找之前版本的数据就是通过这个指针。
* 6字节的

DB_ROW_ID
字段: 当由innodb自动产生聚集索引时，聚集索引包括这个DB_ROW_ID的值，否则聚集索引中不包括这个值，这个用于索引当中。
结合聚簇索引的相关知识点, 我的理解是, 如果我们的表中有主键或合适的唯一索引, 也就是无法生成聚簇索引的时候, InnoDB会帮我们自动生成聚集索引, 但聚簇索引会使用DB_ROW_ID的值来作为主键; 如果我们有自己的主键或者合适的唯一索引, 那么聚簇索引中也就不会包含 DB_ROW_ID 了 。
关于聚簇索引, 《高性能MySQL》中的篇幅对我来说已经够用了, 稍后会整理一下以前的学习笔记, 然后更新上来。

2.下面来演示一下事务对某行记录的更新过程:
![](https://user-gold-cdn.xitu.io/2018/1/2/160b63c130c3c306?imageView2/0/w/1280/h/960/format/webp/ignore-error/1)
3.

read view

* 判断当前版本数据项是否可见
* 在innodb中, 每创建一个新事务, 存储引擎都会将当前系统中的活跃事务列表(trx_sys->trx_list)创建一个副本(

read view
), 副本中保存的是系统中当前不应该被本事务看到的其他事务id列表。
* 当用户在事务中要读取某行记录的时候, innodb会将该行当前的版本号与该read view进行比较, 下面介绍

比较算法
;

## 比较算法:

设该行的当前事务id为

trx_id_current
,

read view
中该行最早的事务id为

trx_id_first
, 最迟的事务id为

trx_id_last

1. 如果

trx_id_current < trx_id_first
, 那就表示
当前事务在读取该行记录的时候, 给该行数据设置的隐藏事务ID字段的值, 比

read view
中记录的 '当前系统中其他事务给该行记录设置的事务ID都要小'。
这就意味着, 当前所有和该行记录有关的事务中, 当前事务是第一个读取到该行记录的, 没有任何在当前事务前面对该行数据做过更改但还没有提交的事务, 所以当前事务可以直接拿到表中**稳定的数据**!
1. 如果

trx_id_current > trx_id_last
的话，那就表示
当前事务在读取该行记录的时候, 给该行数据设置的隐藏事务ID字段的值, 比

read view
中记录的 '当前系统中其他事务给该行记录设置的事务ID都要大'。
这就意味着, 当前所有和该行记录有关的事务中, 当前事务是最后一个读取到该行记录的, 所以需要从该行记录的

DB_ROLL_PTR
指针所指向的回滚段中取出最新的undo-log的版本号, 将它赋值给

trx_id_current
，然后继续重新开始整套比较算法, 这么迭代下去, 会在undo-log中一层层往下找下去, 最终就会取到**稳定的数据**!
1. 如果

trx_id_first < trx_id_current < trx_id_last
, 同上;

## 对比

READ COMMITED
和

REPEATABLE READ

1. 
read view
生成原则如果想深入了解的话可以自行百度或者参考[fxliutao的博客](https://link.juejin.im?target=https%3A%2F%2Fwww.jianshu.com%2Fp%2Ffd51cb8dc03b)
1. 之前已经了解到 MVCC只在

READ COMMITTED
和

REPEATABLE READ
两个隔离级别下工作;
1. 
并且根据

read view
的生成原则, 导致在这两个不同隔离级别下,

read committed
总是读最新一份快照数据, 而repeatable read 读事务开始时的行数据版本;

* 使得

READ COMMITED
级别能够保证, 只要是当前**语句执行前**已经提交的数据都是可见的/*/*。注意和

REPEATABLE READ
级别的区!!!
* 使得

REPEATABLE READ
级别能够保证, 只要是当前**事务执行前**已经提交的数据都是可见的。

## 小结

1. 
一般我们认为MVCC有下面几个特点：

* 每行数据都存在一个版本，每次数据更新时都更新该版本
* 修改时Copy出当前版本, 然后随意修改，各个事务之间无干扰
* 保存时比较版本号，如果成功(commit)，则覆盖原记录, 失败则放弃copy(rollback)
* 就是每行都有版本号，保存时根据版本号决定是否成功，**听起来含有乐观锁的味道, 因为这看起来正是，在提交的时候才能知道到底能否提交成功**
* 而InnoDB实现MVCC的方式是:

* 事务以排他锁的形式修改原始数据
* 把修改前的数据存放于undo log，通过回滚指针与主数据关联
* 修改成功（commit）啥都不做，失败则恢复undo log中的数据（rollback）
* **二者最本质的区别是**: 当修改数据时是否要

排他锁定
，如果锁定了还算不算是MVCC？

* Innodb的实现真算不上MVCC, 因为并没有实现核心的多版本共存,

undo log
中的内容只是串行化的结果, 记录了多个事务的过程, 不属于多版本共存。但理想的MVCC是难以实现的, 当事务仅修改一行记录使用理想的MVCC模式是没有问题的, 可以通过比较版本号进行回滚, 但当事务影响到多行数据时, 理想的MVCC就无能为力了。
* 比如, 如果事务A执行理想的MVCC, 修改Row1成功, 而修改Row2失败, 此时需要回滚Row1, 但因为Row1没有被锁定, 其数据可能又被事务B所修改, 如果此时回滚Row1的内容，则会破坏事务B的修改结果，导致事务B违反ACID。 这也正是所谓的

第一类更新丢失
的情况。
* 也正是因为InnoDB使用的MVCC中结合了排他锁, 不是纯的MVCC, 所以第一类更新丢失是不会出现了, 一般说更新丢失都是指第二类丢失更新。

## 本文主要参考和引用如下文章

[MySQL官方对InnoDB-MVCC的解释](https://link.juejin.im?target=https%3A%2F%2Fdev.mysql.com%2Fdoc%2Frefman%2F5.7%2Fen%2Finnodb-multi-versioning.html)
[fxliutao的博客](https://link.juejin.im?target=https%3A%2F%2Fwww.jianshu.com%2Fp%2Ffd51cb8dc03b)然后结合自己的理解重新整理了一篇新的文章;
* [MySQL]()
* [数据库]()
![](https://b-gold-cdn.xitu.io/v3/static/img/backend.58ef824.png)

相关热门文章

* [在工作中常用到的SQL

* Java3y
* 50
* 5]()
* [为什么要有复合索引？

* think123
* 7]()
* [MySQL锁机制——你想知道的都在这！

* 白山丶
* 28]()
* [我以为我对Mysql索引很了解，直到我遇到了阿里的面试官

* HollisChuang
* 448
* 37]()
* [[灵魂拷问]MySQL面试高频一百问(工程师方向)

* 呼延十
* 126
* 13]()