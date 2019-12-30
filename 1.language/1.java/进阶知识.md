# 并发

## Java 并发

* [Java 并发知识合集](https://github.com/CL0610/Java-concurrency)
* [JAVA并发知识图谱](https://github.com/CL0610/Java-concurrency/blob/master/Java并发知识图谱.png)

## 多线程

* [《40个Java多线程问题总结》](http://www.importnew.com/18459.html)

## 线程安全

* [《Java并发编程——线程安全及解决机制简介》](https://www.cnblogs.com/zhanht/p/5450325.html)

## 一致性、事务

### 事务 ACID 特性
* [《数据库事务ACID特性》](https://blog.csdn.net/u012440687/article/details/52116108)

### 事务的隔离级别

* 未提交读：一个事务可以读取另一个未提交的数据，容易出现脏读的情况。
* 读提交：一个事务等另外一个事务提交之后才可以读取数据，但会出现不可重复读的情况（多次读取的数据不一致），读取过程中出现UPDATE操作，会多。（大多数数据库默认级别是RC，比如SQL Server，Oracle），读取的时候不可以修改。
* 可重复读： 同一个事务里确保每次读取的时候，获得的是同样的数据，但不保障原始数据被其他事务更新（幻读），Mysql InnoDB 就是这个级别。
* 序列化：所有事物串行处理（牺牲了效率）

* [《理解事务的4种隔离级别》](https://blog.csdn.net/qq_33290787/article/details/51924963)
* [数据库事务的四大特性及事务隔离级别](https://www.cnblogs.com/z-sm/p/7245981.html)

* [《MySQL的InnoDB的幻读问题 》](http://blog.sina.com.cn/s/blog_499740cb0100ugs7.html)
	* 幻读的例子非常清楚。
	* 通过 SELECT ... FOR UPDATE 解决。
	
* [《一篇文章带你读懂MySQL和InnoDB》](https://draveness.me/mysql-innodb)
	* 图解脏读、不可重复读、幻读问题。


### MVCC


* [《【mysql】关于innodb中MVCC的一些理解》](https://www.cnblogs.com/chenpingzhao/p/5065316.html)
	* innodb 中 MVCC 用在 Repeatable-Read 隔离级别。
	* MVCC 会产生幻读问题（更新时异常。）

* [《轻松理解MYSQL MVCC 实现机制》](https://blog.csdn.net/whoamiyang/article/details/51901888)

	* 通过隐藏版本列来实现 MVCC 控制，一列记录创建时间、一列记录删除时间，这里的时间
	* 每次只操作比当前版本小（或等于）的 行。
	


## 锁

### Java中的锁和同步类

* [《Java中的锁分类》](https://www.cnblogs.com/qifengshi/p/6831055.html)
	* 主要包括 synchronized、ReentrantLock、和 ReadWriteLock。 

* [《Java并发之AQS详解》](https://www.cnblogs.com/waterystone/p/4920797.html)

* [《Java中信号量 Semaphore》](http://cuisuqiang.iteye.com/blog/2020146)
	* 有数量控制
	* 申请用 acquire，申请不要则阻塞；释放用 release。

* [《java开发中的Mutex vs Semaphore》](https://www.cnblogs.com/davidwang456/p/6094947.html)
	* 简单的说 就是Mutex是排它的，只有一个可以获取到资源， Semaphore也具有排它性，但可以定义多个可以获取的资源的对象。	 

### 公平锁 & 非公平锁

公平锁的作用就是严格按照线程启动的顺序来执行的，不允许其他线程插队执行的；而非公平锁是允许插队的。

* [《公平锁与非公平锁》](https://blog.csdn.net/EthanWhite/article/details/55508357)
	* 默认情况下 ReentrantLock 和 synchronized 都是非公平锁。ReentrantLock 可以设置成公平锁。

### 悲观锁 

悲观锁如果使用不当（锁的条数过多），会引起服务大面积等待。推荐优先使用乐观锁+重试。

* [《【MySQL】悲观锁&乐观锁》](https://www.cnblogs.com/zhiqian-ali/p/6200874.html)
	* 乐观锁的方式：版本号+重试方式
	* 悲观锁：通过 select ... for update 进行行锁(不可读、不可写，share 锁可读不可写)。

* [《Mysql查询语句使用select.. for update导致的数据库死锁分析》](https://www.cnblogs.com/Lawson/p/5008741.html)
	* mysql的innodb存储引擎实务锁虽然是锁行，但它内部是锁索引的。
	* 锁相同数据的不同索引条件可能会引起死锁。
	
* [《Mysql并发时经典常见的死锁原因及解决方法》](https://www.cnblogs.com/zejin2008/p/5262751.html)

### 乐观锁 & CAS

* [《乐观锁的一种实现方式——CAS》](http://www.importnew.com/20472.html)
	* 和MySQL乐观锁方式相似，只不过是通过和原值进行比较。	 

### ABA 问题

由于高并发，在CAS下，更新后可能此A非彼A。通过版本号可以解决，类似于上文Mysql 中提到的的乐观锁。

* [《Java CAS 和ABA问题》](https://www.cnblogs.com/549294286/p/3766717.html)
* [《Java 中 ABA问题及避免》](https://blog.csdn.net/li954644351/article/details/50511879)
	* AtomicStampedReference 和 AtomicStampedReference。 

### CopyOnWrite容器

可以对CopyOnWrite容器进行并发的读，而不需要加锁。CopyOnWrite并发容器用于读多写少的并发场景。比如白名单，黑名单，商品类目的访问和更新场景，不适合需要数据强一致性的场景。

* [《JAVA中写时复制(Copy-On-Write)Map实现》](https://www.cnblogs.com/hapjin/p/4840107.html)
	* 实现读写分离，读取发生在原始数据上，写入发生在副本上。  
	* 不用加锁，通过最终一致实现一致性。
	
* [《聊聊并发-Java中的Copy-On-Write容器》](https://blog.csdn.net/a494303877/article/details/53404623)

### RingBuffer 
* [《线程安全的无锁RingBuffer的实现【一个读线程，一个写线程】》](http://www.cnblogs.com/l00l/p/4115001.html)

### 可重入锁 & 不可重入锁

* [《可重入锁和不可重入锁》](https://www.cnblogs.com/dj3839/p/6580765.html)
	* 通过简单代码举例说明可重入锁和不可重入锁。
	* 可重入锁指同一个线程可以再次获得之前已经获得的锁。
	* 可重入锁可以用户避免死锁。
	* Java中的可重入锁：synchronized 和 java.util.concurrent.locks.ReentrantLock

* [《ReenTrantLock可重入锁（和synchronized的区别）总结》](https://www.cnblogs.com/baizhanshi/p/7211802.html)
	* synchronized 使用方便，编译器来加锁，是非公平锁。
	* ReenTrantLock 使用灵活，锁的公平性可以定制。
	* 相同加锁场景下，推荐使用 synchronized。

### 互斥锁 & 共享锁

互斥锁：同时只能有一个线程获得锁。比如，ReentrantLock 是互斥锁，ReadWriteLock 中的写锁是互斥锁。
共享锁：可以有多个线程同时或的锁。比如，Semaphore、CountDownLatch 是共享锁，ReadWriteLock 中的读锁是共享锁。

* [《ReadWriteLock场景应用》](https://www.cnblogs.com/liang1101/p/6475555.html)

### 死锁
* [《“死锁”四个必要条件的合理解释》](https://blog.csdn.net/yunfenglw/article/details/45950305)
	* 互斥、持有、不可剥夺、环形等待。
* [Java如何查看死锁？](https://blog.csdn.net/u014039577/article/details/52351626)
	* JConsole 可以识别死锁。
	
* [java多线程系列：死锁及检测](https://blog.csdn.net/bohu83/article/details/51135061)
	* jstack 可以显示死锁。