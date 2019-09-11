### mysql查询结果乱码
#### 1.查询数据库结果乱码

    
    mysql>   select doctitle,docpuburl from wcmdocument order by docpubtime desc limit 0,3;
    +-----------+-------------------------------------------------------------+
    | doctitle  | docpuburl                                                   |
    +-----------+-------------------------------------------------------------+
    | 2016-???? | http://xxx/case_usa/case_uk_bk/1075572.shtml |
    | 2016-???? | http://xxx/case_uk_bk/1075570.shtml |
    | 444??333  | http://xxx/test/blog/1333090.shtml |
    +-----------+-------------------------------------------------------------+
    3 rows in set (0.00 sec)

#### 2.通过show variables like 'character_set_%';命令查询发现字符集不是utf-8


    mysql> show variables like 'character_set_%';
    +--------------------------+----------------------------------------+
    | Variable_name            | Value                                  |
    +--------------------------+----------------------------------------+
    | character_set_client     | latin1                                 |
    | character_set_connection | latin1                                 |
    | character_set_database   | utf8                                   |
    | character_set_filesystem | binary                                 |
    | character_set_results    | latin1                                 |
    | character_set_server     | utf8                                   |
    | character_set_system     | utf8                                   |
    | character_sets_dir       | /usr/local/mysql5.6.39/share/charsets/ |
    +--------------------------+----------------------------------------+
    8 rows in set (0.00 sec)

#### 3.修改字符集为utf-8


    mysql> set character_set_client=utf8;
    Query OK, 0 rows affected (0.00 sec)
    
    mysql> 
    mysql> set character_set_connection=utf8;
    Query OK, 0 rows affected (0.00 sec)
    
    mysql> set character_set_connection=utf8;
    Query OK, 0 rows affected (0.00 sec)
    
    mysql> set character_set_results=utf8;
    Query OK, 0 rows affected (0.00 sec)

#### 4.查询修改结果是否生效;


    mysql> show variables like 'character_set_%';
    +--------------------------+----------------------------------------+
    | Variable_name            | Value                                  |
    +--------------------------+----------------------------------------+
    | character_set_client     | utf8                                   |
    | character_set_connection | utf8                                   |
    | character_set_database   | utf8                                   |
    | character_set_filesystem | binary                                 |
    | character_set_results    | utf8                                   |
    | character_set_server     | utf8                                   |
    | character_set_system     | utf8                                   |
    | character_sets_dir       | /usr/local/mysql5.6.39/share/charsets/ |
    +--------------------------+----------------------------------------+
    8 rows in set (0.00 sec)

#### 5.再次查询,解决乱码


    mysql> select doctitle,docpuburl from wcmdocument order by docpubtime desc limit 0,3;
    +-------------------+-------------------------------------------------------------+
    | doctitle          | docpuburl                                                   |
    +-------------------+-------------------------------------------------------------+
    | 2016-xx数据 | http://xxx/case_usa/case_uk_bk/1075572.shtml |
    | 2016-xx数据 | http://xxx/case_uk_bk/1075570.shtml |
    | 2016-xx数据  | http://xxx/test/blog/1333090.shtml  |
    +-------------------+-------------------------------------------------------------+
    3 rows in set (0.00 sec)


#### 6.修改配置文件
    修改mysql数据库的编码格式
    (1)：进入mysql的安装目录，找到my-default.ini或者my.ini配置文件，你可以将my-default.ini修改成my.ini，影响不大的；
    
    (2)：我的my.ini只有一个[mysqld]标签，其他均处于注释状态，我们在my.ini里面做两件事
    
                    在[mysqld]标签下添加：character-set-server=utf8
    
                    增加一个[client]标签，并且在[client]标签下添加：default-character-set=utf8
    
    (3)：到任务列表中重启mysql服务；
    
    (4)：进入dos界面，登录数据库，输入命令：show variables like "%char%"；如果dos界面出现的下图所示结果，说明你修改mysql编码成功啦！