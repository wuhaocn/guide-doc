#### 事务隔离级别

#### 1.什么是事务?

本文所说的 MySQL 事务都是指在 InnoDB 引擎下，MyISAM 引擎是不支持事务的。
数据库事务指的是一组数据操作，事务内的操作要么就是全部成功，要么就是全部失败，
什么都不做，其实不是没做，是可能做了一部分但是只要有一步失败，就要回滚所有操作，有点一不做二不休的意思。
假设一个网购付款的操作，用户付款后要涉及到订单状态更新、扣库存以及其他一系列动作，这就是一个事务，如果一切正常那就相安无事，
一旦中间有某个环节异常，那整个事务就要回滚，总不能更新了订单状态但是不扣库存吧，这问题就大了。

事务具有原子性（Atomicity）、一致性（Consistency）、隔离性（Isolation）、持久性（Durability）四个特性，简称 ACID，缺一不可。

#### 2.概念说明

以下几个概念是事务隔离级别要实际解决的问题，所以需要搞清楚都是什么意思。

- 脏读

脏读指的是读到了其他事务未提交的数据，未提交意味着这些数据可能会回滚，
也就是可能最终不会存到数据库中，也就是不存在的数据。读到了并一定最终存在的数据，这就是脏读。

- 不可重复读

对比可重复读，不可重复读指的是在同一事务内，不同的时刻读到的同一批数据可能是不一样的，
可能会受到其他事务的影响，比如其他事务改了这批数据并提交了。通常针对数据更新（UPDATE）操作。

- 幻读

幻读是针对数据插入（INSERT）操作来说的。
假设事务 A 对某些行的内容作了更改，但是还未提交，此时事务 B 插入了与事务 A 更改前的记录相同的记录行，
并且在事务 A 提交之前先提交了，而这时，在事务 A 中查询，会发现好像刚刚的更改对于某些数据未起作用，
但其实是事务 B 刚插入进来的，让用户感觉很魔幻，感觉出现了幻觉，这就叫幻读。

- 可重复读

可重复读指的是在一个事务内，最开始读到的数据和事务结束前的任意时刻读到的同一批数据都是一致的。
通常针对数据更新（UPDATE）操作。

#### 3.隔离级别：

事务隔离级别
SQL 标准定义了四种隔离级别，MySQL 全都支持。这四种隔离级别分别是：

读未提交（READ UNCOMMITTED）
读提交 （READ COMMITTED）
可重复读 （REPEATABLE READ）
串行化 （SERIALIZABLE）

从上往下，隔离强度逐渐增强，性能逐渐变差。采用哪种隔离级别要根据系统需求权衡决定，其中，可重复读是 MySQL 的默认级别。

事务隔离其实就是为了解决上面提到的脏读、不可重复读、幻读这几个问题，下面展示了 4 种隔离级别对这三个问题的解决程度。

| 隔离级别 | 脏读   | 不可重复读 | 幻读   | 可重复读 |
| -------- | ------ | ---------- | ------ | -------- |
| 读未提交 | 可能   | 可能       | 可能   |
| 读已提交 | 不可能 | 可能       | 可能   |
| 可重复读 | 不可能 | 不可能     | 可能   |
| 串行化   | 不可能 | 不可能     | 不可能 |

只有串行化的隔离级别解决了全部这 3 个问题，其他的 3 个隔离级别都有缺陷。

#### 4.示例

##### 4.1 事务查询&设置

- 查询事务隔离级别

```
SELECT @@tx_isolation
```

```
@@tx_isolation
REPEATABLE-READ
```

- 设置事务隔离级别

```
set global transaction isolation level read uncommitted;
```

- 查询正在运行事务

```
select * from information_schema.innodb_trx;
```

- 查询全局等待事务锁超时时间
  默认参数:innodb_lock_wait_timeout 设置锁等待的时间是 50s

```
SHOW GLOBAL VARIABLES LIKE 'innodb_lock_wait_timeout';
```

- 设置全局等待事务锁超时时间

```
SET  GLOBAL innodb_lock_wait_timeout=100;
```

- 查询当前会话等待事务锁超时时间

```
SHOW VARIABLES LIKE 'innodb_lock_wait_timeout';
```

- 事务

```
begin;
select * from user;
commit;
```

##### 4.2 初始化数据

```
CREATE TABLE `user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(30) DEFAULT NULL,
  `age` tinyint(4) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8

INSERT INTO `user` (`id`, `name`, `age`) VALUES (1, '张三', 1);
INSERT INTO `user` (`id`, `name`, `age`) VALUES (2, '李四', 2);
INSERT INTO `user` (`id`, `name`, `age`) VALUES (3, '王五', 3);
```

###### 4.3 读未提交

MySQL 事务隔离其实是依靠锁来实现的，加锁自然会带来性能的损失。
而读未提交隔离级别是不加锁的，所以它的性能是最好的，没有加锁、解锁带来的性能开销。
但有利就有弊，这基本上就相当于裸奔啊，所以它连脏读的问题都没办法解决。

任何事务对数据的修改都会第一时间暴露给其他事务，即使事务还没有提交。

下面来做个简单实验验证一下，首先设置全局隔离级别为读未提交。

```
set global transaction isolation level read uncommitted;
```

设置完成后，只对之后新起的 session 才起作用，对已经启动 session 无效。
如果用 shell 客户端那就要重新连接 MySQL，如果用 Navicat 那就要创建新的查询窗口。

启动两个事务，分别为事务 A 和事务 B，在事务 A 中使用 update 语句，修改 age 的值为 10，初始是 1 ，在执行完 update 语句之后，
在事务 B 中查询 user 表，会看到 age 的值已经是 10 了，这时候事务 A 还没有提交，而此时事务 B 有可能拿着已经修改过的 age=10 去进行其他操作了。
在事务 B 进行操作的过程中，很有可能事务 A 由于某些原因，
进行了事务回滚操作，那其实事务 B 得到的就是脏数据了，拿着脏数据去进行其他的计算，那结果肯定也是有问题的。

顺着时间轴往表示两事务中操作的执行顺序，重点看图中 age 字段的值。

```
事务A

  begin;
  select * from user;
  update user set age = 10 where id = 1
  rollback;
  
  |
  1. begin;
  |
  |
  2. select * from user;
  |
  |
  3. update user set age = 10 where id = 1
  |
  |
  4. rollback;
  |
  |
  5. select * from user;

事务B
   begin;
   select * from user;
   select * from user;
   select * from user;
   
  |
  1. begin;
  |
  |
  2.1. select * from user;  【事务A [2] 步骤之后】【1, '张三', 1】
  |
  |
  3.1. select * from user;  【事务A [3] 步骤之后】【1, '张三', 10】【脏数据】
  |
  |
  4.1 select * from user;   【事务A [4] 步骤之后】【1, '张三', 1】
```

###### 4.3 读已提交

set global transaction isolation level read committed;
READ-COMMITTED

```
事务A

  begin;
  select * from user;
  update user set age = 10 where id = 1
  rollback;
  
  |
  1. begin;
  |
  |
  2. select * from user;
  |
  |
  3. update user set age = 10 where id = 1
  |
  |
  4. rollback;
  |
  |
  5. select * from user;

事务B
   begin;
   select * from user;
   select * from user;
   select * from user;
   
  |
  1. begin;
  |
  |
  2.1. select * from user;  【事务A [2] 步骤之后】【1, '张三', 1】
  |
  |
  3.1. select * from user;  【事务A [3] 步骤之后】【1, '张三', 1】【无胀数据】
  |
  |
  4.1 select * from user;   【事务A [4] 步骤之后】【10, '张三', 1】【】
```


###### 4.4 幻读

set global transaction isolation level SERIALIZABLE;
SERIALIZABLE

```
事务A

  begin;
  select * from user;
  update user set age = 10 where id = 1
  rollback;
  
  |
  1. begin;
  |
  |
  2. select * from user;
  |
  |
  3. update user set age = 10 where id = 1
  |
  |
  4. rollback;
  |
  |
  5. select * from user;

事务B
   begin;
   select * from user;
   select * from user;
   select * from user;
   
  |
  1. begin;
  |
  |
  2.1. select * from user;  【事务A [2] 步骤之后】【1, '张三', 1】
  |
  |
  3.1. select * from user;  【事务A [3] 步骤之后】【1, '张三', 1】【无胀数据】
  |
  |
  4.1 select * from user;   【事务A [4] 步骤之后】【10, '张三', 1】【】
```


###### 4.3 可重复读

set global transaction isolation level REPEATABLE READ;
REPEATABLE-READ

```
事务A

  begin;
  select * from user;
  update user set age = 10 where id = 1
  rollback;
  
  |
  1. begin;
  |
  |
  2. select * from user;
  |
  |
  3. update user set age = 10 where id = 1
  |
  |
  4. commit;
  |
  |
  5. select * from user;

事务B
   begin;
   select * from user;
   select * from user;
   select * from user;
   
  |
  1. begin;
  |
  |
  2.1. select * from user;  【事务A [2] 步骤之后】【1, '张三', 1】
  |
  |
  3.1. select * from user;  【事务A [3] 步骤之后】【1, '张三', 1】【无胀数据】
  |
  |
  commit
  
  4.1 select * from user;   【事务A [4] 步骤之后】【10, '张三', 1】【】
```


### mysql锁
Mysql中的锁可以分为：
「享锁/读锁（Shared Locks）」
「排他锁/写锁（Exclusive Locks）」 
 [间隙锁」
 「行锁（Record Locks）」
 「表锁」。

### 备注
```
SELECT @@tx_isolation


CREATE TABLE `user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(30) DEFAULT NULL,
  `age` tinyint(4) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8

INSERT INTO `user` (`id`, `name`, `age`) VALUES (2, '李四', 2);
INSERT INTO `user` (`id`, `name`, `age`) VALUES (3, '王五', 3);

select * from information_schema.innodb_trx;


set global transaction isolation level read uncommitted;


begin;
select * from user; 
commit; 



SHOW GLOBAL VARIABLES LIKE 'innodb_lock_wait_timeout';

### 读未提交


begin;
select * from user;
update user set age = 11 where id = 1
commit;



begin;
select * from user;
commit;

### 读已提交
set global transaction isolation level read committed;
SELECT @@tx_isolation
begin;
select * from user;
update user set age = 10 where id = 1
rollback;



begin;
select * from user;
commit;

### 可重复读
set global transaction isolation level REPEATABLE READ;
SELECT @@tx_isolation
begin;
select * from user;
update user set age = 10 where id = 1
rollback;



begin;
select * from user;
commit;



```