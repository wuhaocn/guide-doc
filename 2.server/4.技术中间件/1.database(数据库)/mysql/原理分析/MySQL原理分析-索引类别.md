### 项目需要将某个表的某两个字段添加唯一索引，保证这两个字段的值不能同时重复。

    Alter table 表名 add  UNIQUE index 索引名 (字段1,字段2)

### 当表中已经存在重复数据的时候，添加的时候就会报错，这时候需要将数据去重。

    先查出来重复的数据
    
    SELECT * FROM (SELECT 字段,COUNT(1) AS num FROM 表 GROUP BY 字段) temp WHERE num > 1
    
    手动删除。
    
    Alter ignore table 表名 add  UNIQUE index 索引名 (字段1,字段2)
    它会删除重复的记录（会保留一条），然后建立唯一索引，高效而且人性化(未测试)。

### mysql索引类别：

#### 1、添加PRIMARY KEY（主键索引）

ALTER TABLE `table_name` ADD PRIMARY KEY ( `column` )

#### 2、添加UNIQUE(唯一索引)

ALTER TABLE `table_name` ADD UNIQUE ( `column` )  

#### 3、添加INDEX(普通索引)

ALTER TABLE `table_name` ADD INDEX index_name ( `column` )

#### 4、添加FULLTEXT(全文索引)

mysql>ALTER TABLE `table_name` ADD FULLTEXT ( `column`)  

#### 5、添加多列索引

ALTER TABLE `table_name` ADD INDEX index_name ( `column1`, `column2`, `column3` )

### mysql索引对比：

    PRIMARY, INDEX, UNIQUE 这3种是一类
    
    PRIMARY 主键。 就是 唯一 且 不能为空。
    
    INDEX 索引，普通的
    
    UNIQUE 唯一索引。 不允许有重复。
    
    FULLTEXT 是全文索引，用于在一篇文章中，检索文本信息的。


### 原理解析：
    B+Tree的特性
    
    　　(1)由图能看出，单节点能存储更多数据，使得磁盘IO次数更少。
    
    　　(2)叶子节点形成有序链表，便于执行范围操作。
    
    　　(3)聚集索引中，叶子节点的data直接包含数据；非聚集索引中，叶子节点存储数据地址的指针。

#### 参考：
    https://blog.csdn.net/weixin_40805079/article/details/84978158 
