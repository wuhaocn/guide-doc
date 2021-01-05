## MySQL 并行复制的深入浅出

参考：https://yq.aliyun.com/articles/621197

- 本文目录
  [一、并行复制的背景]()
  [二、重点]()
  [三、MySQL5.6 基于 schema 的并行复制]()
  [四、MySQL5.7 基于 group commit 的并行复制]()
  [4.1 Commit-Parent-Based 模式]()
  [4.2 Lock-Based 模式]()
  [五、MySQL8.0 基于 write-set 的并行复制]()
  [六、如何让 slave 的并行复制和 master 的事务执行的顺序一致呢]()

## 一、并行复制的背景

首先，为什么会有并行复制这个概念呢？

```
1.DBA都应该知道，MySQL的复制是基于binlog的。
2.MySQL复制包括两部分，IO线程 和 SQL线程。
3.IO线程主要是用于拉取接收Master传递过来的binlog，并将其写入到relay log
4.SQL线程主要负责解析relay log，并应用到slave中
5.不管怎么说，IO和SQL线程都是单线程的，然后master却是多线程的，所以难免会有延迟，为了解决这个问题，多线程应运而生了。
6.IO多线程？
    6.1 IO没必要多线程，因为IO线程并不是瓶颈啊
7.SQL多线程？
    7.1 没错，目前最新的5.6，5.7，8.0 都是在SQL线程上实现了多线程，来提升slave的并发度

接下来，我们就来一窥MySQL在并行复制上的努力和成果吧
```

## 二、重点

是否能够并行，关键在于多事务之间是否有锁冲突，这是关键。 下面的并行复制原理就是在看如何让避免锁冲突

## 三、MySQL5.6 基于 schema 的并行复制

slave-parallel-type=DATABASE(不同库的事务，没有锁冲突)

之前说过，并行复制的目的就是要让 slave 尽可能的多线程跑起来，当然基于库级别的多线程也是一种方式(不同库的事务，没有锁冲突)

先说说优点： 实现相对来说简单，对用户来说使用起来也简单
再说说缺点： 由于是基于库的，那么并行的粒度非常粗，现在很多公司的架构是一库一实例，针对这样的架构，5.6 的并行复制无能为力。当然还有就是主从事务的先后顺序，对于 5.6 也是个大问题

话不多说，来张图好了

![mts_1](https://yqfile.alicdn.com/ea892dbcb9c622fd86a670c28c1e6c375d094db0.png 'mts_1')

## 四、MySQL5.7 基于 group commit 的并行复制

slave-parallel-type=LOGICAL_CLOCK : Commit-Parent-Based 模式(同一组的事务[last-commit 相同]，没有锁冲突. 同一组，肯定没有冲突，否则没办法成为同一组)
slave-parallel-type=LOGICAL_CLOCK : Lock-Based 模式(即便不是同一组的事务，只要事务之间没有锁冲突[prepare 阶段]，就可以并发。 不在同一组，只要 N 个事务 prepare 阶段可以重叠，说明没有锁冲突)

group commit，之前的文章有详细描述，这里不多解释。MySQL5.7 在组提交的时候，还为每一组的事务打上了标记，现在想想就是为了方便进行 MTS 吧。

我们先看一组 binlog

```
last_committed=0 sequence_number=1
last_committed=1 sequence_number=2
last_committed=2 sequence_number=3
last_committed=3 sequence_number=4
last_committed=4 sequence_number=5
last_committed=4 sequence_number=6
last_committed=4 sequence_number=7
last_committed=6 sequence_number=8
last_committed=6 sequence_number=9
last_committed=9 sequence_number=10
```

### 4.1 Commit-Parent-Based 模式

![mts_2](https://yqfile.alicdn.com/e9e913b761d306b508547313ebe4e2587d288a71.png 'mts_2')

### 4.2 Lock-Based 模式

![mts_3](https://yqfile.alicdn.com/71121fdcab12de6999eeafaaab2d0a36ae40ed60.png 'mts_3')

## 五、MySQL8.0 基于 write-set 的并行复制

关于 write-set 的并行复制，看姜老师的这篇文章
[基于 WRITESET 的 MySQL 并行复制](https://yq.aliyun.com/go/articleRenderRedirect?url=https%3A%2F%2Fmp.weixin.qq.com%2Fs%2Foj-DzpR-hZRMMziq2_0rYg)
可以快速理解,再详细的自己去看源码即可

我这里简短的对里面的几个重要概论做些解读，这些是我当时理解的时候有偏差的地方

- 如何启用 write-set 并行复制

```
MySQL 5.7.22+ 支持基于write-set的并行复制
/#
master
loose-binlog_transaction_dependency_tracking = WRITESET
loose-transaction_write_set_extraction = XXHASH64
binlog_transaction_dependency_history_size = 25000 /#默认
/#slave
slave-parallel-type = LOGICAL_CLOCK slave-parallel-workers = 32
```

- 核心原理

```
/# master
master端在记录binlog的last_committed方式变了
基于commit-order的方式中，
last_committed表示同一组的事务拥有同一个parent_commit 基于write-set的方式中，last_committed的含义是保证冲突事务（相同记录）不能拥有同样的last_committed值 当事务每次提交时，会计算修改的每个行记录的WriteSet值，然后查找哈希表中是否已经存在有同样的WriteSet
 1. 若无，WriteSet插入到哈希表，写入二进制日志的last_committed值保持不变，意味着上一个事务跟当前事务的last_committed相等，那么在slave就可以并行执行
 2. 若有，更新哈希表对应的writeset的value为sequence number，并且写入到二进制日志的last_committed值也要更新为sequnce_number。意味着，相同记录（冲突事务）回放，last_committed值必然不同，必须等待之前的一条记录回放完成后才能执行 /# slave slave的逻辑跟以前一样没有变化，last_committed相同的事务可以并行执行
```

- 并行复制如何备份

```
1. slave的顺序如果不一致，如何备份呢？
    1.1 对于non-gtid的gap情况，xtrabackup拷贝的时候应该会通过某种方式记录某一个一致点，否则无法进行change master
    1.2 对于gitd，gtid模式本身的机制就可以解决gap的问题
```

- 要不要开启并行复制呢？

```
1. 基于order-commit的模式，本身并行复制已经很好了，如果并发量非常高，那么order-commit可以有很好的表现，如果并发量低，order-commit体现不了并行的优势。 但是大家想想，并发量低的MySQL，根本也不需要并行复制吧
2. 基于write-set的模式，这是目前并发度最高的并行复制了，基本可以解决大部分场景，如果并发量高，或者新搭建的slave需要快速追主库，这是最好的办法。
3. 单线程复制 + 安全参数双0，这种模式同样拥有不随的表现，一般压力均可应付。 以上三种情况，是目前解决延迟的最普遍的方法，目前我用的最多的是最后一种
```

- 后面的事务比前面的事务先执行，有什么影响

```
1.slave的gtid会产生gap
2.事务在某个时刻是不一致的，但是最终是一致的, 满足最终一致性
3.相同记录的修改，会按照顺序执行。不同记录的修改，可以产生并行，并无数据一致性风险 总结，基本没啥影响
```

## 六、如何让 slave 的并行复制和 master 的事务执行的顺序一致呢

5.7.19 之后，可以通过设置 slave_preserve_commit_order = 1

```
官方解释： For multithreaded slaves, enabling this variable ensures that transactions are externalized on the slave in the same order as they appear in the slave's relay log. Setting this variable has no effect on slaves for which multithreading is not enabled. All replication threads (for all replication channels if you are using multiple replication channels) must be stopped before changing this variable. --log-bin and --log-slave-updates must be enabled on the slave. In addition --slave-parallel-type must be set to LOGICAL_CLOCK. Once a multithreaded slave has been started, transactions can begin to execute in parallel. With slave_preserve_commit_order enabled, the executing thread waits until all previous transactions are committed before committing. While the slave thread is waiting for other workers to commit their transactions it reports its status as Waiting for preceding transaction to commit. 大致实现原理就是：excecution阶段可以并行执行，binlog flush的时候，按顺序进行。 引擎层提交的时候，根据binlog_order_commit也是排队顺序完成 换句话说，如果设置了这个参数，master是怎么并行的，slave就怎么办并行
```

版权声明：本文内容由互联网用户自发贡献，版权归作者所有，本社区不拥有所有权，也不承担相关法律责任。如果您发现本社区中有涉嫌抄袭的内容，欢迎发送邮件至：[yqgroup@service.aliyun.com](mailto:yqgroup@service.aliyun.com) 进行举报，并提供相关证据，一经查实，本社区将立刻删除涉嫌侵权内容。

### 网友评论

[登录](https://account.aliyun.com/login/login.htm?from_type=yqclub&oauth_callback=https%3A%2F%2Fyq.aliyun.com%2Farticles%2F621197%3Fdo%3Dlogin)后评论

0/500

评论

### 相关文章

[]()

深入理解 MySQL 主从原理专栏 发布
[重庆八怪![](https://yqfile.alicdn.com/8d41f5de338fc9698298b8a6adacc4e8.png)]() 2019-07-13 21:49:05 浏览 262

[]()

深入理解 MySQL 5.7 GTID 系列（一）
[技术小能手![](https://yqfile.alicdn.com/8d41f5de338fc9698298b8a6adacc4e8.png)]() 2018-01-08 10:07:20 浏览 2606
[]()

深入浅出 cassandra 4 数据一致性问题概述
[荣华]() 2016-03-28 09:38:21 浏览 3020

[]()

MySQL 深入 03-锁-事务-GTID
[余二五]() 2017-11-22 16:48:00 浏览 553
[]()

MySQL 深入 10-利用 Ameoba 实现读写分离
[余二五]() 2017-11-15 16:19:00 浏览 550

[]()

大而全、小而美，品 PG 和 MySQL 间的“爱恨情仇”
[云学习小组![](https://yqfile.alicdn.com/8d41f5de338fc9698298b8a6adacc4e8.png)]() 2016-09-19 18:21:57 浏览 10302
[]()

「mysql 优化专题」本专题总结终章(13)
[风月连城 1]() 2018-01-04 15:04:00 浏览 1082

[]()

MySQL 复制原理与配置
[技术小牛人]() 2017-11-12 11:58:00 浏览 565
[]()

不忘初心——做世界上最流行的云数据库
[仝一]() 2018-12-29 13:46:12 浏览 1129

[]()

MySQL 大型分布式集群
[调皮仔 3683![](https://yqfile.alicdn.com/8d41f5de338fc9698298b8a6adacc4e8.png)]() 2017-08-16 16:02:58 浏览 3020
[]()

PolarDB · 新品介绍 · 深入了解阿里云新一代产品 PolarDB
[db 匠]() 2017-09-21 09:00:02 浏览 6048

[]()

PolarDB · 新品介绍 · 深入了解阿里云新一代产品 PolarDB
[技术小能手![](https://yqfile.alicdn.com/8d41f5de338fc9698298b8a6adacc4e8.png)]() 2017-12-13 13:56:01 浏览 1379
[]()

深入浅出：对 MySQL 主从配置的一些总结
[余二五]() 2017-11-22 16:42:00 浏览 554

[]()

MySQL 深入 09-备份-恢复
[余二五]() 2017-11-23 16:43:00 浏览 686
[]()

MySQL 深入 08-日志及其参数设定
[余二五]() 2017-11-15 16:44:00 浏览 513

[]()

MySQL 深入 04-存储引擎
[余二五]() 2017-11-14 16:47:00 浏览 853
[]()

MySQL 深入 05-用户管理
[余二五]() 2017-11-15 16:47:00 浏览 514

[]()

MySQL-5.5 主从关于‘复制过滤’的深入探究
[技术小胖子]() 2017-11-09 04:35:00 浏览 566
[]()

MySQL 查询优化之 explain 的深入解析
[余二五]() 2017-11-23 17:18:00 浏览 583

[]()

深入浅出学习 Linux（基础知识二）
[初雪之路![](https://yqfile.alicdn.com/8d41f5de338fc9698298b8a6adacc4e8.png)]() 2018-12-21 21:12:30 浏览 582

下拉加载更多

- 作者介绍
  [![](https://ucc.alicdn.com/avatar/img_4132fa68859769c09bffb8a2d57534b3.jpeg)]() [兰春]() ![]("专家认证") [+ 关注]()
