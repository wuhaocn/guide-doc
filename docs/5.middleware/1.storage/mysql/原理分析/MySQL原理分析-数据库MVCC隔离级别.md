### 1. 什么是 MVCC?

多版本并发控制（Multi-Version Concurrency Control, MVCC），顾名思义，在并发访问的时候，数据存在版本的概念，可以有效地提升数据库并发能力，常见的数据库如 MySQL、MS SQL Server、IBM DB2、Hbase、MongoDB 等等都在使用。
简单讲，如果没有 MVCC，当想要读取的数据被其他事务用排它锁锁住时，只能互斥等待；而这时 MVCC 可以通过提供历史版本从而实现读取被锁的数据(的历史版本)，避免了互斥等待。

在 MySQL 中，多版本并发控制是 的 InnoDB 存储引擎实现隔离级别的一种具体方式，用于实现提交读和可重复读这两种隔离级别。而未提交读隔离级别总是读取最新的数据行，无需使用 MVCC；可串行化隔离级别需要对所有读取的行都加锁，单纯使用 MVCC 无法实现。

MVCC 一般有两种实现方式，本文所讲的 InnoDB 采用的是后者:

- 实时保留数据的一个或多个历史版本
- 在需要时通过 undo 日志构造出历史版本

### 2. 数据库的锁

### 2.1 数据库的锁加在哪里？

在学习之前，我一直想当然地认为锁是加在数据行上的，然而，数据库的锁是**加在数据行对应的索引上**的，这个概念在后续理解锁的范围时至关重要。机智如你可能会问，没有索引怎么办？答案是，不管你建或不建，总有索引在那里！下面简单过一下
InnoDB 有[两类索引](https://dev.mysql.com/doc/refman/5.7/en/innodb-index-types.html):

- 1. Clustered Index: 聚集索引(聚簇索引)，通过聚集索引可以直接定位到数据的物理存储位置，从而进行 IO 读写，它是接触到数据的必经之路。每张表都会默默地建立聚集索引，具体的建立规则[戳链接](http:)

1.

1. Secondary Index: 辅助索引(非聚集索引)，除了 Clustered Index，其他都是 Secondary Index(所以我们自己建的都叫辅助索引)。通过辅助索引，可以查到数据的主键或者数据行 id，然后再通过主键或者数据行 id 查聚集索引获取数据的物理存储位置，才能进行 IO 读写。

讲了这么多想说什么呢？综上所述！在真正接触到数据之前，任何数据库操作都会先走索引，这也就不难理解为什么锁是加在索引上的了

### 2.2 什么时候会加锁？

在数据库增删改查四种操作中，insert、delete 和 update 都是会加排它锁(即下文中的

Exclusive Lock
)的，而 select 只有显式声明才会加锁:

1.

select
: 即最常用的查询，是不加任何锁的

1.

select ... lock in share mode
: 会加共享锁(即下文中的

Shared Lock
)

1.

select ... for update
: 会加排它锁

至于后两种的使用场景，感兴趣的[戳链接](https://dev.mysql.com/doc/refman/5.7/en/innodb-locking-reads.html)

### 2.2 锁的分类

InnoDB 有很多锁，单是在官网上列出来的就有[8 种](https://dev.mysql.com/doc/refman/5.7/en/innodb-locking.html)，如果组合使用就更多了。这里只简单讲一下后续会用到的锁。其中锁的范围需要对索引有较深的了解，可以[戳链接](http://www.cnblogs.com/hustcat/archive/2009/10/28/1591648.html)自行学习

### Shared and Exclusive Locks

这两个锁类似于 Java 中的读写锁，其中

Shared Lock
相当于 Java 中的读锁，读写、写写是互斥的，读读是可以并发的；

Exclusive Lock
相当于 Java 中的写锁，读写、写写、读读都是互斥的

### Record Locks

行锁，顾名思义，是加在**索引行**(对！是索引行！不是数据行！)上的锁。比如

select /\* from user where id=1 and id=10 for update
，就会在

id=1
和

id=10
的索引行上加 Record Lock

### Gap Locks

间隙锁，它会锁住两个索引之间的区域。比如

select /\* from user where id>1 and id<10 for update
，就会在 id 为(1,10)的索引区间上加 Gap Lock

### Next-Key Locks

也叫间隙锁，它是 Record Lock + Gap Lock 形成的一个闭区间锁。比如

select /\* from user where id>=1 and id<=10 for update
，就会在 id 为[1,10]的索引闭区间上加 Next-Key Lock

至此，所有预备知识都已经讲完了(对！预备知识就是这么多！怕不怕？！)，开始本文的主题

# 3.四种隔离级别

这四种隔离级别，是在[SQL:1992 标准](https://en.wikipedia.org/wiki/SQL-92)中定义的

接下来，我们以文章开头提到的两个"不同"为主线，依次解开四种隔离级别的实现原理，同时可以慢慢品味这句话: 不同的隔离级别是在数据可靠性和并发性之间的均衡取舍，隔离级别越高，对应的并发性能越差，数据越安全可靠

## 3.1READ UNCOMMITTED

顾名思义，事务之间可以读取彼此未提交的数据。机智如你会记得，在前文有说到所有写操作都会加排它锁，那还怎么读未提交呢？该级别主要的特点是**释放锁的时机**与众不同：在执行完写操作后立即释放，而不像其他隔离级别在事务提交以后释放。因此极易出现脏读(不可重复读和幻读就更不用说了)
但该级别的并发性能也正因为锁释放得很早而变得很高，就连写写操作都很难产生锁竞争，并发性能可见一斑

## 3.2READ COMMITTED

既然读未提交有那么大的数据可靠性问题，那就往前迈一小步，读已提交。该级别下将锁的释放时机延迟到事务提交之后，从而实现了读已提交，解决了脏读
但是！好像哪里不太对？！锁的释放时机延迟了，写与写操作之间产生锁竞争就算了，那在锁释放之前，读也不能读了吗？这并发性能不能忍！这时就该 MVCC 出马了，既然不想阻塞等待最新的数据，那就无视当前持有锁的操作，读取最新的历史版本数据先用着
因此，在读已提交的级别下，**每次 select 时**都会通过 MVCC 获取当前数据的最新快照，不加任何锁，也无视任何锁(因为历史数据是构造出来的，身上不可能有锁)，完美解决读写之间的并发问题，和 READ UNCOMMITTED 的并发性能只差在写写操作上
而为了进一步提升写写操作上的并发性能，该级别下不会使用前文提到的间隙锁，无论什么查询都只会加行锁，而且在执行完 WHERE 条件筛选之后，会立即释放掉不符合条件的行锁，对于并发性能的追求可谓仁至义尽了
但是，正因为对并发性能的极致追求或者说贪婪，该级别下还是遗留了不可重复读和幻读问题：

1. MVCC 版本的生成时机: 是**每次 select 时**，这就意味着，如果我们在事务 A 中执行多次的 select，在每次 select 之间有其他事务更新了我们读取的数据并提交了，那就出现了不可重复读
1. 锁的范围: 因为没有间隙锁，这就意味着，如果我们在事务 A 中多次执行

select /\* from user where age>18 and age<30 for update
时，其他事务是可以往 age 为(18,30)这个区间插入/删除数据的，那就出现了幻读
![]()

RC 如何解决脏读问题,同样的，丢失修改问题也解决了

## 3.3 REPEATABLE READ

既然读已提交依然有较大的数据可靠性能问题，那就再往前迈一小步，可重复读，该级别在读已提交的基础上做了两点修改，从而避免了不可重复读和幻读：

1. MVCC 版本的生成时间: 一次事务中只在**第一次 select 时**生成版本，后续的查询都是在这个版本上进行，从而实现了可重复读
   ![]()

可重复读的实现

1. 锁的范围: 在行锁的基础上，加上 Gap Lock，从而形成 Next-Key Lock，在所有遍历过的(不管是否匹配条件)索引行上以及之间的区域上，都加上锁，阻塞其他事务在遍历范围内进行写操作，从而避免了幻读
   看似很完美了对吧，并发性能上、读读、读写操作依旧两不误，写写操作为了数据可靠性做了妥协也是能接受的，皆大欢喜？
   图样图森破！这个世界怕什么？猪队友啊！InnoDB 在可重复读级别下已经将数据可靠性和并发性能两方面做得尽善尽美了，但前提是用户查询时能够主动善用 Locking Reads，即前文提到的

select ... lock in share mode
和

select ... for update
。如果只是使用普通的

select
，依然防不住幻读
这是因为 MVCC 的快照只对读操作有效，对写操作无效，举例说明会更清晰一点： 事务 A 依次执行如下 3 条 sql，事务 B 在语句 1 和 2 之间，插入 10 条 age=20 的记录，事务 A 就幻读了

```
1\. select count(1) from user where age=20; -- return 0: 当前没有age=20的 2\. update user set name=test where age=20; -- Affects 10 rows: 因为事务B刚写入10条age=20的记录，而写操作是不受MVCC影响，能看到最新数据的，所以更新成功，而一旦操作成功，这些被操作的数据就会对当前事务可见 3\. select count(1) from user where age=20; -- return 10: 出现幻读
```

这种场景，需要用户主动使用 Locking Read 来防止其他事务在查询范围内进行写操作，因此，为了防患于未然，隔离级别又往前迈了一步

## 3.4 SERIALISABLE

大杀器，该级别下，会自动将所有普通

select
转化为

select ... lock in share mode
执行，即针对同一数据的所有读写都变成互斥的了，可靠性大大提高，并发性大大降低
机智如你可能会问，那可重复读级别下使用 Locking Read 不也变成读写互斥了嘛，那这两个有什么区别呢？可重复读你可以自己选择是否使用 Locking Read 呀，艺高人胆大可以使用普通的

select
读写并发的嘛

# 4.总结

一篇文章下来有太多的概念，但正是这么多的概念相辅相成打造了隔离级别，真是剪不断理还乱，最后用一张表做个小结
隔离级别 MVCC 版本生成时机 写操作释放锁的时机 锁的范围 丢失修改 脏读 不可重复度 幻读 READ UNCOMMITTED / SQL 执行完立即释放 行锁 √ √ √ √ READ COMMITTED 每次 select 时 事务结束后 行锁 √ √ REPEATABLE READ 事务第一次 select 时 事务结束后 行锁或间隙锁 特定情况下 SERIALIZABLE 事务第一次 select 时 事务结束后 行锁或间隙锁

该表有两点需要说明：

- 不同级别下，只有写操作释放锁的时机不同，而 Locking Read 的锁，不论什么级别，都是在事务结束后释放
- REPEATABLE READ 级别，可以防止大部分的幻读，但像前边举例读-写-读的情况，使用不加锁的 select 依然会幻读

### 所用到的命令

Prior to MySQL 5.7.20, use tx_isolation rather than transaction_isolation.

- 设置事务隔离级别: set (session/global) transaction isolation level [read uncommitted/read committed/repeatable read/serilisable]
- 查看事务隔离级别: select @@(session./global.)tx_isolation;
  start transaction / rollback / commit
  select ... lock in share mode / select ... for update
  select /\* from information_schema.innodb_locks;
- 查看索引使用情况: explain [sql 语句]
- 查看锁等待情况

- select /\* from information_schema.innodb_locks;
- select /\* from information_schema.innodb_lock_waits;
- select /\* from information_schema.innodb_trx;
- 查看 InnoDB 状态(包括锁): show engine innodb status;

参考：
[http://hulichao.top/posts/1324.html](http://hulichao.top/posts/1324.html)
MySQL innodb 存储引擎每一行后有

- 隐藏的 ID
- 6 字节的事务 ID（DB_TRX_ID ）
- 7 字节的回滚指针（DB_ROLL_PTR）

[https://blog.csdn.net/chen77716/article/details/6742128](https://blog.csdn.net/chen77716/article/details/6742128)
事务隔离级别与 Spring 传播事务
[https://mp.weixin.qq.com/s/xdQKOYW0HOC4mpYinXDS8A](https://mp.weixin.qq.com/s/xdQKOYW0HOC4mpYinXDS8A)
