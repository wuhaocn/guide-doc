# MySQL 日志系统：redo log、binlog、undo log 区别与作用

转载 [二十六画生的博客](https://me.csdn.net/u010002184) 发布于 2019-03-13 10:21:01 阅读数 3309 [收藏]()

更新于 2019-03-13 11:14:30
分类专栏： [Mysql](https://blog.csdn.net/u010002184/category_5878585.html)

文章标签： [MySQL](https://so.csdn.net/so/search/s.do?q=MySQL&t=blog)[日志](https://so.csdn.net/so/search/s.do?q=日志&t=blog)[binlog](https://so.csdn.net/so/search/s.do?q=binlog&t=blog)

[展开]()

日志系统主要有 redo log(重做日志)和 binlog(归档日志)。redo log 是 InnoDB 存储引擎层的日志，binlog 是 MySQL Server 层记录的日志， 两者都是记录了某些操作的日志(不是所有)自然有些重复（但两者记录的格式不同）。

![](https://www.linuxidc.com/upload/2018_11/181121105137362.png)

图来自极客时间的 mysql 实践,该图是描述的是 MySQL 的逻辑架构。

### []()[]()redo log 日志模块

redo log 是 InnoDB 存储引擎层的日志，又称重做日志文件，用于记录事务操作的变化，记录的是数据修改之后的值，不管事务是否提交都会记录下来。在实例和介质失败（media failure）时，redo log 文件就能派上用场，如数据库掉电，InnoDB 存储引擎会使用 redo log 恢复到掉电前的时刻，以此来保证数据的完整性。

在一条更新语句进行执行的时候，InnoDB 引擎会把更新记录写到 redo log 日志中，然后更新内存，此时算是语句执行完了，然后在空闲的时候或者是按照设定的更新策略将 redo log 中的内容更新到磁盘中，这里涉及到

WAL
即

Write Ahead logging
技术，他的关键点是先写日志，再写磁盘。

有了 redo log 日志，那么在数据库进行异常重启的时候，可以根据 redo log 日志进行恢复，也就达到了

crash-safe
。

redo log 日志的大小是固定的，即记录满了以后就从头循环写。

![](https://www.linuxidc.com/upload/2018_11/181121105137361.jpg)

图片来自极客时间，该图展示了一组 4 个文件的 redo log 日志，checkpoint 之前表示擦除完了的，即可以进行写的，擦除之前会更新到磁盘中，write pos 是指写的位置，当 write pos 和 checkpoint 相遇的时候表明 redo log 已经满了，这个时候数据库停止进行数据库更新语句的执行，转而进行 redo log 日志同步到磁盘中。

### []()[]()binlog 日志模块

binlog 是属于 MySQL Server 层面的，又称为归档日志，属于逻辑日志，是以二进制的形式记录的是这个语句的原始逻辑，依靠 binlog 是没有

crash-safe
能力的

### []()[]()redo log 和 binlog 区别

- redo log 是属于 innoDB 层面，binlog 属于 MySQL Server 层面的，这样在数据库用别的存储引擎时可以达到一致性的要求。
- redo log 是物理日志，记录该数据页更新的内容；binlog 是逻辑日志，记录的是这个更新语句的原始逻辑
- redo log 是循环写，日志空间大小固定；binlog 是追加写，是指一份写到一定大小的时候会更换下一个文件，不会覆盖。
- binlog 可以作为恢复数据使用，主从复制搭建，redo log 作为异常宕机或者介质故障后的数据恢复使用。

### []()[]()一条更新语句执行的顺序

update T set c=c+1 where ID=2;

- 执行器先找引擎取 ID=2 这一行。ID 是主键，引擎直接用树搜索找到这一行。如果 ID=2 这一行所在的数据页本来就在内存中，就直接返回给执行器；否则，需要先从磁盘读入内存，然后再返回。
- 执行器拿到引擎给的行数据，把这个值加上 1，比如原来是 N，现在就是 N+1，得到新的一行数据，再调用引擎接口写入这行新数据。
- 引擎将这行新数据更新到内存中，同时将这个更新操作记录到 redo log 里面，此时 redo log 处于 prepare 状态。然后告知执行器执行完成了，随时可以提交事务。
- 执行器生成这个操作的 binlog，并把 binlog 写入磁盘。
- 执行器调用引擎的提交事务接口，引擎把刚刚写入的 redo log 改成提交（commit）状态，更新完成。

这个 update 语句的执行流程图，图中浅色框表示是在 InnoDB 内部执行的，深色框表示是在执行器中执行的。

![](https://www.linuxidc.com/upload/2018_11/181121105137363.png)

[https://www.linuxidc.com/Linux/2018-11/155431.htm](https://www.linuxidc.com/Linux/2018-11/155431.htm)

---

innodb 事务日志包括 redo log 和 undo log。redo log 是重做日志，提供前滚操作，undo log 是回滚日志，提供回滚操作。

undo log 不是 redo log 的逆向过程，其实它们都算是用来恢复的日志：
**1.redo log 通常是物理日志，记录的是数据页的物理修改，而不是某一行或某几行修改成怎样怎样，它用来恢复提交后的物理数据页(恢复数据页，且只能恢复到最后一次提交的位置)。**
**2.undo 用来回滚行记录到某个版本。undo log 一般是逻辑日志，根据每行记录进行记录。**

[https://juejin.im/entry/5ba0a254e51d450e735e4a1f](https://juejin.im/entry/5ba0a254e51d450e735e4a1f)

---

# []()[]()一、重做日志（redo log）

**作用：**

确保事务的持久性。防止在发生故障的时间点，尚有脏页未写入磁盘，在重启 mysql 服务的时候，根据 redo log 进行重做，从而达到事务的持久性这一特性。

# []()[]()二、回滚日志（undo log）

**作用：**

保存了事务发生之前的数据的一个版本，可以用于回滚，同时可以提供多版本并发控制下的读（MVCC），也即非锁定读

# []()[]()三、二进制日志（binlog）：

**作用：**

用于复制，在主从复制中，从库利用主库上的 binlog 进行重播，实现主从同步。
用于数据库的基于时间点的还原。

[https://blog.csdn.net/u012834750/article/details/79533866](https://blog.csdn.net/u012834750/article/details/79533866)

---

数据库数据存放的文件称为 data file；日志文件称为 log file；数据库数据是有缓存的，如果没有缓存，每次都写或者读物理 disk，那性能就太低下了。数据库数据的缓存称为 data buffer，日志（redo）缓存称为 log buffer；既然数据库数据有缓存，就很难保证缓存数据（脏数据）与磁盘数据的一致性。比如某次数据库操作：

update driver_info set driver_status = 2 where driver_id = 10001;

更新 driver_status 字段的数据会存放在缓存中，等待存储引擎将 driver_status 刷新 data_file，并返回给业务方更新成功。如果此时数据库宕机，缓存中的数据就丢失了，业务方却以为更新成功了，数据不一致，也没有持久化存储。

上面的问题就可以通过事务的 ACID 特性来保证。

BEGIN trans；

update driver_info set driver_status = 2 where driver_id = 10001;

COMMIT;

这样执行后，更新要么成功，要么失败。业务方的返回和数据库 data file 中的数据保持一致。要保证这样的特性这就不得不说存储引擎 innodb 的 redo 和 undo 日志。

redo 日志、undo 日志：

存储引擎也会为 redo undo 日志开辟内存缓存空间，log buffer。磁盘上的日志文件称为 log file，是顺序追加的，性能非常高，注：磁盘的顺序写性能比内存的写性能差不了多少。

undo 日志用于记录事务开始前的状态，用于事务失败时的回滚操作；redo 日志记录事务执行后的状态，用来恢复未写入 data file 的已成功事务更新的数据。例如某一事务的事务序号为 T1，其对数据 X 进行修改，设 X 的原值是 5，修改后的值为 15，那么 Undo 日志为<T1, X, 5>，Redo 日志为<T1, X, 15>。

梳理下事务执行的各个阶段：

（1）写 undo 日志到 log buffer；

（2）执行事务，并写 redo 日志到 log buffer；

（3）如果 innodb_flush_log_at_trx_commit=1，则将 redo 日志写到 log file，并刷新落盘。

（4）提交事务。

可能有同学会问，为什么没有写 data file，事务就提交了？

在数据库的世界里，数据从来都不重要，日志才是最重要的，有了日志就有了一切。

因为 data buffer 中的数据会在合适的时间 由存储引擎写入到 data file，如果在写入之前，数据库宕机了，根据落盘的 redo 日志，完全可以将事务更改的数据恢复。好了，看出日志的重要性了吧。先持久化日志的策略叫做 Write Ahead Log，即预写日志。

分析几种异常情况：

- innodb_flush_log_at_trx_commit=2（[innodb_flush_log_at_trx_commit 和 sync_binlog 参数详解](http://link.zhihu.com/?target=https%3A//mp.weixin.qq.com/s%3F__biz%3DMzU4NjQwNTE5Ng%3D%3D%26mid%3D2247483681%26idx%3D1%26sn%3D03adfb89521568013f6a1efd9ca1af6a%26scene%3D21%23wechat_redirect)）时，将 redo 日志写入 logfile 后，为提升事务执行的性能，存储引擎并没有调用文件系统的 sync 操作，将日志落盘。如果此时宕机了，那么未落盘 redo 日志事务的数据是无法保证一致性的。
- undo 日志同样存在未落盘的情况，可能出现无法回滚的情况。

checkpoint：

checkpoint 是为了定期将 db buffer 的内容刷新到 data file。当遇到内存不足、db buffer 已满等情况时，需要将 db buffer 中的内容/部分内容（特别是脏数据）转储到 data file 中。在转储时，会记录 checkpoint 发生的”时刻“。在故障回复时候，只需要 redo/undo 最近的一次 checkpoint 之后的操作。

[https://blog.csdn.net/bluejoe2000/article/details/80349499](https://blog.csdn.net/bluejoe2000/article/details/80349499)

- [点赞 5]()
- [收藏]()
- [分享]()
- []()

- [文章举报]()
  [![](https://profile.csdnimg.cn/9/3/3/3_u010002184) ![](https://g.csdnimg.cn/static/user-reg-year/2x/6.png)](https://blog.csdn.net/u010002184)

[二十六画生的博客](https://blog.csdn.net/u010002184)

发布了 807 篇原创文章 · 获赞 290 · 访问量 177 万+
[他的留言板](https://bbs.csdn.net/forums/p-u010002184) [关注]()
