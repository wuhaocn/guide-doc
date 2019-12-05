# 数据库
## 基础理论
### 数据库设计的三大范式
* [《数据库的三大范式以及五大约束》](https://www.cnblogs.com/waj6511988/p/7027127.html)
	* 第一范式：数据表中的每一列（每个字段）必须是不可拆分的最小单元，也就是确保每一列的原子性；
	* 第二范式（2NF）：满足1NF后，要求表中的所有列，都必须依赖于主键，而不能有任何一列与主键没有关系，也就是说一个表只描述一件事情；
	* 第三范式：必须先满足第二范式（2NF），要求：表中的每一列只与主键直接相关而不是间接相关，（表中的每一列只能依赖于主键）；

## MySQL

### 原理
* [《MySQL的InnoDB索引原理详解》](http://www.admin10000.com/document/5372.html)

* [《MySQL存储引擎－－MyISAM与InnoDB区别》](https://blog.csdn.net/xifeijian/article/details/20316775)
	* 两种类型最主要的差别就是Innodb 支持事务处理与外键和行级锁

* [《myisam和innodb索引实现的不同》](https://www.2cto.com/database/201211/172380.html)

### InnoDB

* [《一篇文章带你读懂Mysql和InnoDB》](https://my.oschina.net/kailuncen/blog/1504217)

### 优化

* [《MySQL36条军规》](http://vdisk.weibo.com/s/muWOT)

* [《MYSQL性能优化的最佳20+条经验》](https://www.cnblogs.com/zhouyusheng/p/8038224.html)
* [《SQL优化之道》](https://blog.csdn.net/when_less_is_more/article/details/70187459)
* [《mysql数据库死锁的产生原因及解决办法》](https://www.cnblogs.com/sivkun/p/7518540.html)
* [《导致索引失效的可能情况》](https://blog.csdn.net/monkey_d_feilong/article/details/52291556)
* [《 MYSQL分页limit速度太慢优化方法》](https://blog.csdn.net/zy_281870667/article/details/51604540)
	* 原则上就是缩小扫描范围。


### 索引

#### 聚集索引, 非聚集索引

* [《MySQL 聚集索引/非聚集索引简述》](https://blog.csdn.net/no_endless/article/details/77073549)
* [《MyISAM和InnoDB的索引实现》](https://www.cnblogs.com/zlcxbb/p/5757245.html)

MyISAM 是非聚集，InnoDB 是聚集

#### 复合索引

* [《复合索引的优点和注意事项》](https://www.cnblogs.com/summer0space/p/7247778.html)
	* 文中有一处错误：
	> 对于复合索引,在查询使用时,最好将条件顺序按找索引的顺序,这样效率最高; select * from table1 where col1=A AND col2=B AND col3=D 如果使用 where col2=B AND col1=A 或者 where col2=B 将不会使用索引
	* 原文中提到索引是按照“col1，col2，col3”的顺序创建的，而mysql在按照最左前缀的索引匹配原则，且会自动优化 where 条件的顺序，当条件中只有 col2=B AND col1=A 时，会自动转化为 col1=A AND col2=B，所以依然会使用索引。
	
* [《MySQL查询where条件的顺序对查询效率的影响》](https://www.cnblogs.com/acode/p/7489258.html)
	
#### 自适应哈希索引(AHI)

* [《InnoDB存储引擎——自适应哈希索引》](https://blog.csdn.net/Linux_ever/article/details/62043708)


### explain
* [《MySQL 性能优化神器 Explain 使用分析》](https://segmentfault.com/a/1190000008131735)

## NoSQL

### MongoDB

* [MongoDB 教程](http://www.runoob.com/mongodb/mongodb-tutorial.html)
* [《Mongodb相对于关系型数据库的优缺点》](http://mxdxm.iteye.com/blog/2093603)
	* 优点：弱一致性（最终一致），更能保证用户的访问速度；内置GridFS，支持大容量的存储；Schema-less 数据库，不用预先定义结构；内置Sharding；相比于其他NoSQL，第三方支持丰富；性能优越；
	* 缺点：mongodb不支持事务操作；mongodb占用空间过大；MongoDB没有如MySQL那样成熟的维护工具，这对于开发和IT运营都是个值得注意的地方；

### Hbase

* [《简明 HBase 入门教程（开篇）》](http://www.thebigdata.cn/HBase/35831.html)
* [《深入学习HBase架构原理》](https://www.cnblogs.com/qiaoyihang/p/6246424.html)
* [《传统的行存储和（HBase）列存储的区别》](https://blog.csdn.net/youzhouliu/article/details/67632882)


* [《Hbase与传统数据库的区别》](https://blog.csdn.net/lifuxiangcaohui/article/details/39891099)
	* 空数据不存储，节省空间，且适用于并发。

* [《HBase Rowkey设计》](https://blog.csdn.net/u014091123/article/details/73163088)
	* rowkey 按照字典顺序排列，便于批量扫描。
	* 通过散列可以避免热点。