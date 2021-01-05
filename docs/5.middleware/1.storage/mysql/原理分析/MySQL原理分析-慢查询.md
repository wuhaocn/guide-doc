MySQL 源码分析-慢查询

### 1.慢查询参数

一条 sql 语句在 MySQL 中执行超过一定时间，会被记录为慢查询，慢查询相关的参数有以下几个：

- slow_query_log=1
- long_query_time=0.5
- slow_query_log_file=/mysql/data/mysql_slow.log
- log_queries_not_using_indexes=1
- log_throttle_queries_not_using_indexes=20
- min_examined_row_limit=100
- log_slow_admin_statements=1
- log_slow_slave_statements=1
  slow_query_log:
  控制是否记录慢查询；
  long_query_time:
  慢查询阈值，单位秒，执行时间超过这个值的将被记录为慢查询日志中;
  slow_query_log_file:
  慢查询日志路径；
  log_queries_not_using_indexes:
  没有使用索引的 sql 也将被记录到慢查询日志中；
  log_throttle_queries_not_using_indexes:  
   如果 log_queries_not_using_indexes 打开，
  没有使用索引的 sql 将会写入到慢查询日志中，
  该参数将限制每分钟写入的 sql 数量；
  min_examined_row_limit:
  对于查询扫描行数小于此参数的 sql，将不会记录到慢查询日志中；
  log_slow_admin_statements:
  管理语句执行时间大于阈值也将写入到慢查询日志中，
  管理语句包括 alter table, check table 等等；
  log_slow_slave_statements:
  从库应用 binlog，如果 binlog 格式是 statement，执行时间超过阈值时，将写入从库的慢查询日志，
  对于 ROW 格式 binlog，不管执行时间有没有超过阈值，都不会写入到从库的慢查询日志。

### 线索一：long_query_time 变量

根据慢查询相关参数，在源码中搜索 long_query_time，源码中肯定有执行时间大于这个值，就记为慢查询的相关代码逻辑，搜索到一段代码如下：

    //sql/sql_class.h 3279行
    void update_server_status()
      {
        ulonglong end_utime_of_query= current_utime();
        if (end_utime_of_query > utime_after_lock + variables.long_query_time)
          server_status|= SERVER_QUERY_WAS_SLOW;
      }
    这段代码里的if条件是 end_utime_of_query > utime_after_lock + variables.long_query_time， 我们稍微变换一下：
    end_utime_of_query - utime_after_lock > variables.long_query_time

    end_utime_of_query 为sql执行完取到的当前时间，单位微秒；
    utime_after_lock 为sql执行期间锁等待的时间，也就是说MySQL慢查询并不把锁等待的时间算在里面，每次执行sql前，会通过thd->set_time()将该变量重置为当前时间，单位微秒；
    variables.long_query_time 为慢查询阈值。

### 线索二：update_server_status 函数

update_server_status 函数是 THD 类的成员函数，utime_after_lock 为 THD 类的成员变量，下面是 THD 类的定义简化示意：

    //sql/sql_class.h
    class THD :public MDL_context_owner,
               public Query_arena,
               public Open_tables_state
    {
    ...
        public:
          ulonglong  start_utime, utime_after_lock;
    ...
        public:
        void update_server_status()
          {
            ulonglong end_utime_of_query= current_utime();
            if (end_utime_of_query > utime_after_lock +  variables.long_query_time)
              server_status|= SERVER_QUERY_WAS_SLOW;

          }
    ...
    };

下面来看一下 update_server_status 这个 THD 的成员函数，什么时候被调用。MySQL 是 Server/Engine 架构， 慢查询相关逻辑在 Server 层进行处理，先看一下 MySQL 执行一条 sql 的函数调用栈，如下：

    main                //main.cc
    mysqld_main         //mysqld.cc
    mysqld_socket_acceptor->connection_event_loop    //mysqld.cc
    Connection_handler_manager::process_new_connection(Channel_info* channel_info)  //connection_handler_manager.cc
    Per_thread_connection_handler::add_connection(Channel_info* channel_info)  //connection_handler_per_thread.cc
    handle_connection        //connection_handler_per_thread.cc
    do_command               //sql_parse.cc
    dispatch_command         //sql_parse.cc
    mysql_parse              //sql_parse.cc
    mysql_execute_command    //sql_parse.cc
    ...

在源码中搜索 update_server_status， 在 dispatch_command 函数中，看到如下一段代码逻辑，简化后如下：

    // sql/sql_parse.cc
    bool dispatch_command(THD *thd, const COM_DATA *com_data,
                          enum enum_server_command command)
    {
        thd->set_time();
        mysql_parse(thd, &parser_state);
        thd->update_server_status();
        thd->send_statement_status();
        log_slow_statement(thd);
    }

其中
thd->set_time() 对 utime_after_lock 进行了重置，重置后的值为当前时间，单位是微秒，后面附 thd->set_time()函数的具体实现。
mysql_parse(thd, &parser_state) 为 sql 执行的具体实现，包括调用引擎层的接口，向客户端发送查询结果数据等。
thd->update_server_status() 判断执行时间是否大于设置的慢查询阈值，如果大于慢查询阈值，设置 server_status|= SERVER_QUERY_WAS_SLOW
thd->send_statement_status() 发送 sql 语句执行状态
log_slow_statement(thd) 根据 thd->server_status 是否包含 SERVER_QUERY_WAS_SLOW 标志，决定是否写入慢查询日志

### 线索三：utime_after_lock

另外一个问题，utime_after_lock 这个值应该是不断改变的，因为它记录了锁等待的时间，代码中哪里修改了这个变量值呢？继续搜索代码，发现这么一个函数：

    // sql/sql_class.cc
    extern "C"
    void thd_storage_lock_wait(THD *thd, long long value)
    {
      thd->utime_after_lock+= value;
    }

这段代码由引擎层调用，引擎层遇到锁等待时，调用这个函数，修改 thd->utime_after_lock 变量的值

### 总结：

至此，MySQL 在 Server 层判断一个 sql 执行是否属于慢查询的逻辑变得清晰了，总结分以下几步：

- （1）sql 执行前，通过 thd->set_time()函数，记录当前时间（微秒）到 utime_after_lock
- （2）sql 执行过程中，引擎层遇到锁等待，将等待的时间（微秒）通过 thd_storage_lock_wait 函数调用，加到 utime_after_lock 变量上
- （3）sql 执行完，再次记录当前时间（微秒）到 end_utime_of_query， 通过 判断 （end_utime_of_query - utime_after_lock > variables.long_query_time）这个 if 条件，来决定是否记录到慢查询日志。

附相关函数：

    // sql/sql_class.h  THD成员函数
    inline void set_time()
      {
        start_utime= utime_after_lock= my_micro_time();
        if (user_time.tv_sec || user_time.tv_usec)
        {
          start_time= user_time;
        }
        else
          my_micro_time_to_timeval(start_utime, &start_time);
    #ifdef HAVE_PSI_THREAD_INTERFACE
        PSI_THREAD_CALL(set_thread_start_time)(start_time.tv_sec);
    #endif
      }
    // sql/sql_class.h   THD成员函数current_utime，实际调用的还是my_micro_time
    ulonglong current_utime()  { return my_micro_time(); }

    // my_getsystime.c
    ulonglong my_micro_time()
    {
    #ifdef _WIN32
      ulonglong newtime;
      my_get_system_time_as_file_time((FILETIME*)&newtime);
      newtime-= OFFSET_TO_EPOCH;
      return (newtime/10);
    #else
      ulonglong newtime;
      struct timeval t;
      /*
        The following loop is here because gettimeofday may fail on some  systems
      */
      while (gettimeofday(&t, NULL) != 0)
      {}
      newtime= (ulonglong)t.tv_sec * 1000000 + t.tv_usec;
      return newtime;
    #endif
    }

### 摘自：

https://www.jianshu.com/p/dc5e3dad5b5d
