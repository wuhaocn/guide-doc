## 1.概述

为了方便代码阅读及代码更新记录代码已上传至 github。
地址：https://github.com/coral-learning/mysql-coderead.git

## 2.目录结构

    1.client mysql命令行客户端工具
    2.dbug 调试工具
    3.Docs 一些说明文档
    4.include 基本的头文件
    5.libmysql 创建嵌入式系统的mysql客户端程序API
    6.libmysqld mysql服务器的核心级API文件(8.0没了?)
    7.mysql-test mysql的测试工具箱
    8.mysys 操作系统API的大部分封装函数和各种辅助函数
    9.regex 处理正则表达式的库
    10.scripts 一些基于shell脚本的工具
    11.sql 主要源代码
    12.sql-bench 一些性能测试工具(8.0没了)
    13.ssl一些ssl的工具和定义(8.0改为mysys_ssl)
    14.storage 插件式存储引起的代码
    15.strings 各种字符串处理函数
    16.support-files 各种辅助文件
    17.vio 网络层和套接层的代码
    18.zlib 数据压缩工具(8.0移到了utilities)

## 参考

[MySQL 源码分析 v2.0](https://blog.csdn.net/feivirus/article/details/83716680)

[MySQL 内核源码解读-SQL 解析一](https://blog.51cto.com/wangwei007/2300217?source=drh)
