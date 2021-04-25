## 国产达梦数据库与MySQL的区别
### 背景
由于项目上的需要，把项目实现国产化，把底层的MySQL数据库替换为国产的达梦数据库，花了一周的时间研究了国产的数据库-达梦数据库，
它和MySQL有一定的区别，SQL的写法也有一些区别。

### 介绍
以下介绍来自于达梦数据库官网，相关的文档在[官网](https://www.dameng.com/)中也是可以下载的。
达梦数据库管理系统是达梦公司推出的具有完全自主知识产权的高性能数据库管理系统，简称DM，它具有如下特点：

* 1、通用性
达梦数据库管理系统兼容多种硬件体系，可运行于X86、X64、SPARC、POWER等硬件体系之上。DM各种平台上的数据存储结构和消息通信结构完全一致，
使得DM各种组件在不同的硬件平台上具有一致的使用特性。
达梦数据库管理系统产品实现了平台无关性，支持Windows系列、各版本Linux（2.4及2.4以上内核）、Unix、Kylin、AIX、Solaris等各种主流操作系统。
达梦数据库的服务器、接口程序和管理工具均可在32位/64 位版本操作系统上使用。

* 2、高性能
支持列存储、数据压缩、物化视图等面向联机事务分析场景的优化选项；
通过表级行存储、列存储选项技术，在同一产品中提供对联机事务处理和联机分析处理业务场景的支持；

* 3、高可用
可配置数据守护系统(主备)，自动快速故障恢复，具有强大的容灾处理能力。

* 4、跨平台
跨平台，支持主流软硬件体系（支持windows、Linux、中标麒麟、银河麒麟等操作系统),支持主流标准接口。

* 5、高可扩展
支持拓展软件包和多种工具，实现海量数据分析处理、数据共享集群(DSC)和无共享数据库集群(MPP)等扩展功能

### 与MySQL的区别
#### 概览
* comment 注释
* 不支持 date_sub 函数，使用 dateadd(datepart,n,date) 代替
* 不支持 date_format 函数，它有三种代替方法
* 不支持 substring_index 函数
* 不支持 group_concat 函数
* 不支持 from_unixtime 函数
* 不支持 case-when-then-else
* current_timestamp 的返回值带有时区
* convert(type, value) 函数
* 不支持 on duplicate key update，
* 不支持 ignore，即 insert ignore  into
* 不支持 replace into，
* 不支持 if。
* 不支持 "",只支持''
* 不支持 auto_increment， 使用 identity 代替
* 不支持 longtext 类型，
#### 详细介绍


* 1. 创建表的时候，不支持在列的后面直接加 comment 注释，使用 COMMENT ON  IS 代替，如：

   COMMENT ON TABLE xxx IS xxx
   COMMENT ON COLUMN xxx IS xxx

* 2. 不支持 date_sub 函数，使用 dateadd(datepart,n,date) 代替，

   其中，datepart可以为：year(yy,yyyy)，quarter(qq,q)，month(mm,m)，dayofyear(dy,y)，day(dd,d)，week(wk,ww)，weekday(dw)，hour(hh)，         minute(mi,n)， second(ss,s)， millisecond(ms)
   例子：
   select dateadd(month, -6, now());
   select dateadd(month, 2, now());

* 3. 不支持 date_format 函数，它有三种代替方法：

   a:  使用 datepart 代替：语法：datepart(datepart, date)，返回代表日期的指定部分的整数，

        datepart可以为：year(yy,yyyy)，quarter(qq,q)，month(mm,m)，dayofyear(dy,y)，day(dd,d)，week(wk,ww)，weekday(dw)，hour(hh)，                   minute(mi,n)，second(ss,s)， millisecond(ms)
         例子：
         select datepart(year, '2018-12-13 08:45:00'); --2018
         select datepart(month, '2018-12-13 08:45:00'); --12

   b: 使用 date_part 代替，功能和 datepart 一样，写法不同，参数顺序颠倒，且都要加引号，

       例子：
       select date_part('2018-12-13 08:45:00', 'year');--2018
       select date_part('2018-12-13 08:45:00', 'mm'); -- 12

   c:  使用 extract 代替，语法：extract(dtfield from date)，从日期类型date中抽取dtfield对应的值
   dtfield 可以是 year，month，day，hour，minute，second
   例子：
   select extract(year from  '2018-12-13 08:45:00'); --2018
   select extract(month from  '2018-12-13 08:45:00'); --12

* 4.  不支持 substring_index 函数， 使用 substr / substring 代替，

    语法：
    substr(char[,m[,n]])
    substring(char[from m[ for n]])

* 5. 不支持 group_concat 函数，使用 wm_concat 代替，

   例子：
   select wm_concat(id) as idstr from persion ORDER BY id ;

* 6. 不支持 from_unixtime 函数，使用 round 代替

   语法：round(date[,format])

* 7. 不支持 case-when-then-else ，

   例如：
   select case  when id = 2 then "aaa" when id = 3 then "bbb" else "ccc" end as test
   from (select id from person) tt;

* 8. current_timestamp 的返回值带有时区，

   例子：
   select current_timestamp();
   2018-12-17 14:34:18.433839 +08:00

* 9. convert(type, value) 函数，

   与 mysql 的 convert 一样，但是参数是反过来的，mysql 是 convert(value, type)

* 10.  不支持 on duplicate key update，

     使用 merge into 代替

* 11. 不支持 ignore，即 insert ignore  into

* 12. 不支持 replace into，

    使用 merge into 代替

* 13. 不支持 if。

* 14.  不支持 "",只支持''

* 15. 不支持 auto_increment， 使用 identity 代替

    如： identity(1, 1)，从 1 开始，每次增 1

* 16. 不支持 longtext 类型，

    可用 CLOB 代替。