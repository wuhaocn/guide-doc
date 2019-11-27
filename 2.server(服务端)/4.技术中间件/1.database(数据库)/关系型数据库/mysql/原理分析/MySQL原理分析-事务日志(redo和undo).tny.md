<div id="post_detail">
<div id="topics">
<div class="post">
<h1 class="postTitle">

<a id="cb_post_title_url" class="postTitle2" href="https://www.cnblogs.com/f-ck-need-u/p/9010872.html">详细分析MySQL事务日志(redo log和undo log)</a>

</h1>
<div class="clear"></div>
<div class="post-categoty-tags"><div class="post-categoty">
分类: 
<a href="https://www.cnblogs.com/f-ck-need-u/category/1085743.html" target="_blank">数据库系列</a></div><div class="post-tags">undefined</div></div><div class="postBody">

<div id="cnblogs_post_body" class="blogpost-body "><p><strong><span style="font-size: 18px;color: red">Shell技术交流群：921383787</span></strong><br><strong><span style="font-size: 18px;">本人博客搬家：骏马金龙<a href="https://jq.qq.com/?_wv=1027&amp;k=5fqQBsX">www.junmajinlong.com</a></span></strong></p><p>&nbsp;</p>
<p>innodb事务日志包括redo log和undo log。redo log是重做日志，提供前滚操作，undo log是回滚日志，提供回滚操作。</p>
<p class="01">undo log不是redo log的逆向过程，其实它们都算是用来恢复的日志：<br>
<span style="color: #ff0000;"><strong>1.redo log通常是物理日志，记录的是数据页的物理修改，而不是某一行或某几行修改成怎样怎样，它用来恢复提交后的物理数据页(恢复数据页，且只能恢复到最后一次提交的位置)。</strong></span><br><span style="color: #ff0000;"><strong>
2.undo用来回滚行记录到某个版本。undo log一般是逻辑日志，根据每行记录进行记录。</strong></span></p>
<p class="01"><a name="blog1"></a></p>
<h1 class="01" id="auto_id_0">1.redo log</h1>
<p><a name="blog1.1"></a></p>
<h2 class="01" id="auto_id_1">1.1 redo log和二进制日志的区别</h2>
<p class="01">二进制日志相关内容，参考：<span style="color: #0000ff;"><a href="http://www.cnblogs.com/f-ck-need-u/p/9001061.html#blog5" target="_blank"><span style="color: #0000ff;">MariaDB/MySQL的二进制日志</span></a></span>。</p>
<p class="01">redo log不是二进制日志。虽然二进制日志中也记录了innodb表的很多操作，<strong><span style="color: #ff0000;">也能实现重做的功能</span>，</strong>但是它们之间有很大区别。</p>
<ol>
<li class="01">二进制日志是在<span style="color: #ff0000;"><strong>存储引擎的上层</strong></span>产生的，不管是什么存储引擎，对数据库进行了修改都会产生二进制日志。而redo log是innodb层产生的，只记录该存储引擎中表的修改。<span style="color: #ff0000;"><strong>并且二进制日志先于</strong><strong>redo log</strong><strong>被记录</strong></span>。具体的见后文group commit小结。</li>
<li class="01">二进制日志记录操作的方法是逻辑性的语句。即便它是基于行格式的记录方式，其本质也还是逻辑的SQL设置，如该行记录的每列的值是多少。而redo log是在物理格式上的日志，它记录的是数据库中每个页的修改。</li>
<li class="01">二进制日志只在每次事务提交的时候一次性写入缓存中的日志"文件"(对于非事务表的操作，则是每次执行语句成功后就直接写入)。而redo log在数据准备修改前写入缓存中的redo log中，然后才对缓存中的数据执行修改操作；而且保证在发出事务提交指令时，先向缓存中的redo log写入日志，写入完成后才执行提交动作。</li>
<li class="01">因为二进制日志只在提交的时候一次性写入，所以二进制日志中的记录方式和提交顺序有关，且一次提交对应一次记录。而redo log中是记录的物理页的修改，redo log文件中同一个事务可能多次记录，最后一个提交的事务记录会覆盖所有未提交的事务记录。例如事务T1，可能在redo log中记录了&nbsp;<span class="cnblogs_code">T1-<span style="color: #800080;">1</span>,T1-<span style="color: #800080;">2</span>,T1-<span style="color: #800080;">3</span>，T1*</span>&nbsp;共4个操作，其中&nbsp;<span class="cnblogs_code">T1*</span>&nbsp;表示最后提交时的日志记录，所以对应的数据页最终状态是&nbsp;<span class="cnblogs_code">T1*</span>&nbsp;对应的操作结果。而且redo log是并发写入的，不同事务之间的不同版本的记录会穿插写入到redo log文件中，例如可能redo log的记录方式如下：&nbsp;<span class="cnblogs_code">T1-<span style="color: #800080;">1</span>,T1-<span style="color: #800080;">2</span>,T2-<span style="color: #800080;">1</span>,T2-<span style="color: #800080;">2</span>,T2*,T1-<span style="color: #800080;">3</span>,T1*</span>&nbsp;。</li>
<li class="01">事务日志记录的是物理页的情况，它具有幂等性，因此记录日志的方式极其简练。幂等性的意思是多次操作前后状态是一样的，例如新插入一行后又删除该行，前后状态没有变化。而二进制日志记录的是所有影响数据的操作，记录的内容较多。例如插入一行记录一次，删除该行又记录一次。</li>

























</ol>
<p class="01"><a name="blog1.2"></a></p>
<h2 class="01" id="auto_id_2">1.2 redo log的基本概念</h2>
<p class="01">redo log包括两部分：一是内存中的日志缓冲(redo log buffer)，该部分日志是易失性的；二是磁盘上的重做日志文件(redo log file)，该部分日志是持久的。</p>
<p class="01">在概念上，innodb通过<span style="color: #ff0000;"><strong><em>force log at commit</em></strong></span>机制实现事务的持久性，即在事务提交的时候，必须先将该事务的所有事务日志写入到磁盘上的redo log file和undo log file中进行持久化。</p>
<p class="01">为了确保每次日志都能写入到事务日志文件中，在每次将log buffer中的日志写入日志文件的过程中都会调用一次操作系统的fsync操作(即fsync()系统调用)。因为MariaDB/MySQL是工作在用户空间的，MariaDB/MySQL的log buffer处于用户空间的内存中。要写入到磁盘上的log file中(redo:ib_logfileN文件,undo:share tablespace或.ibd文件)，中间还要经过操作系统内核空间的os buffer，调用fsync()的作用就是将OS buffer中的日志刷到磁盘上的log file中。</p>
<p class="01">也就是说，从redo log buffer写日志到磁盘的redo log file中，过程如下：&nbsp;</p>
<p class="01"><img src="https://images2018.cnblogs.com/blog/733013/201805/733013-20180508101949424-938931340.png" alt=""></p>
<blockquote>
<p class="a">在此处需要注意一点，一般所说的log file并不是磁盘上的物理日志文件，而是操作系统缓存中的log file，官方手册上的意思也是如此(例如：With a value of 2, the contents of the <span style="color: #ff0000;"><strong>InnoDB log buffer are written to the log file</strong></span> after each transaction commit and <span style="color: #ff0000;"><strong>the log file is flushed to disk approximately once per second</strong></span>)。但说实话，这不太好理解，既然都称为file了，应该已经属于物理文件了。所以在本文后续内容中都以os buffer或者file system buffer来表示官方手册中所说的Log file，然后log file则表示磁盘上的物理日志文件，即log file on disk。</p>
<p class="a">另外，之所以要经过一层os buffer，是因为open日志文件的时候，open没有使用O_DIRECT标志位，该标志位意味着绕过操作系统层的os buffer，IO直写到底层存储设备。不使用该标志位意味着将日志进行缓冲，缓冲到了一定容量，或者显式fsync()才会将缓冲中的刷到存储设备。使用该标志位意味着每次都要发起系统调用。比如写abcde，不使用o_direct将只发起一次系统调用，使用o_object将发起5次系统调用。</p>























</blockquote>
<p class="01">MySQL支持用户自定义在commit时如何将log buffer中的日志刷log file中。这种控制通过变量&nbsp;<span class="cnblogs_code">innodb_flush_log_at_trx_commit</span>&nbsp;的值来决定。该变量有3种值：0、1、2，默认为1。但注意，这个变量只是控制commit动作是否刷新log buffer到磁盘。</p>
<ul>
<li class="a">当设置为1的时候，事务每次提交都会将log buffer中的日志写入os buffer并调用fsync()刷到log file on disk中。这种方式即使系统崩溃也不会丢失任何数据，但是因为每次提交都写入磁盘，IO的性能较差。</li>
<li class="a">当设置为0的时候，事务提交时不会将log buffer中日志写入到os buffer，而是每秒写入os buffer并调用fsync()写入到log file on disk中。也就是说设置为0时是(大约)每秒刷新写入到磁盘中的，当系统崩溃，会丢失1秒钟的数据。</li>
<li class="a">当设置为2的时候，每次提交都仅写入到os buffer，然后是每秒调用fsync()将os buffer中的日志写入到log file on disk。</li>























</ul>
<p class="01"><img src="https://images2018.cnblogs.com/blog/733013/201805/733013-20180508104623183-690986409.png" alt=""></p>
<p class="01">注意，有一个变量&nbsp;<span class="cnblogs_code">innodb_flush_log_at_timeout</span>&nbsp;的值为1秒，该变量表示的是刷日志的频率，很多人误以为是控制&nbsp;<span class="cnblogs_code">innodb_flush_log_at_trx_commit</span>&nbsp;值为0和2时的1秒频率，实际上并非如此。测试时将频率设置为5和设置为1，当&nbsp;<span class="cnblogs_code">innodb_flush_log_at_trx_commit</span>&nbsp;设置为0和2的时候性能基本都是不变的。关于这个频率是控制什么的，在后面的"<a href="file:///E:/onedrive/%E6%88%91%E7%9A%84%E5%AD%A6%E4%B9%A0/MySQL/MySQL%E7%AE%A1%E7%90%86%E7%AF%87/MySQL%E7%AE%A1%E7%90%86.docx#_刷日志到磁盘的规则">刷日志到磁盘的规则</a>"中会说。</p>
<p class="01">在主从复制结构中，要保证事务的持久性和一致性，需要对日志相关变量设置为如下：</p>
<ul>
<li><span style="color: #ff0000;"><strong>如果启用了二进制日志，则设置sync_binlog=1，即每提交一次事务同步写到磁盘中。</strong></span></li>
<li><span style="color: #ff0000;"><strong>总是设置innodb_flush_log_at_trx_commit=1，即每提交一次事务都写到磁盘中。</strong></span></li>























</ul>
<p class="01">上述两项变量的设置保证了：每次提交事务都写入二进制日志和事务日志，并在提交时将它们刷新到磁盘中。</p>
<p class="01">选择刷日志的时间会严重影响数据修改时的性能，特别是刷到磁盘的过程。下例就测试了&nbsp;<span class="cnblogs_code">innodb_flush_log_at_trx_commit</span>&nbsp;分别为0、1、2时的差距。</p>
<div>
<div class="cnblogs_code"><div class="cnblogs_code_toolbar"><span class="cnblogs_code_copy"><a href="javascript:void(0);" onclick="copyCnblogsCode(this)" title="复制代码"><img src="//common.cnblogs.com/images/copycode.gif" alt="复制代码"></a></span></div>
<pre><span style="color: #000000;">#创建测试表
</span><span style="color: #0000ff;">drop</span> <span style="color: #0000ff;">table</span> <span style="color: #0000ff;">if</span> <span style="color: #808080;">exists</span><span style="color: #000000;"> test_flush_log;
</span><span style="color: #0000ff;">create</span> <span style="color: #0000ff;">table</span> test_flush_log(id <span style="color: #0000ff;">int</span>,name <span style="color: #0000ff;">char</span>(<span style="color: #800000; font-weight: bold;">50</span>))engine<span style="color: #808080;">=</span><span style="color: #000000;">innodb;

#创建插入指定行数的记录到测试表中的存储过程
</span><span style="color: #0000ff;">drop</span> <span style="color: #0000ff;">procedure</span> <span style="color: #0000ff;">if</span> <span style="color: #808080;">exists</span> <span style="color: #0000ff;">proc</span><span style="color: #000000;">;
delimiter $$
</span><span style="color: #0000ff;">create</span> <span style="color: #0000ff;">procedure</span> <span style="color: #0000ff;">proc</span>(i <span style="color: #0000ff;">int</span><span style="color: #000000;">)
</span><span style="color: #0000ff;">begin</span>
<span style="color: #0000ff;">declare</span> s <span style="color: #0000ff;">int</span> <span style="color: #0000ff;">default</span> <span style="color: #800000; font-weight: bold;">1</span><span style="color: #000000;">;
</span><span style="color: #0000ff;">declare</span> c <span style="color: #0000ff;">char</span>(<span style="color: #800000; font-weight: bold;">50</span>) <span style="color: #0000ff;">default</span> repeat(<span style="color: #ff0000;">'</span><span style="color: #ff0000;">a</span><span style="color: #ff0000;">'</span>,<span style="color: #800000; font-weight: bold;">50</span><span style="color: #000000;">);
</span><span style="color: #0000ff;">while</span> s<span style="color: #808080;">&lt;=</span><span style="color: #000000;">i do
start </span><span style="color: #0000ff;">transaction</span><span style="color: #000000;">;
</span><span style="color: #0000ff;">insert</span> <span style="color: #0000ff;">into</span> test_flush_log <span style="color: #0000ff;">values</span>(<span style="color: #0000ff;">null</span><span style="color: #000000;">,c);
</span><span style="color: #0000ff;">commit</span><span style="color: #000000;">;
</span><span style="color: #0000ff;">set</span> s<span style="color: #808080;">=</span>s<span style="color: #808080;">+</span><span style="color: #800000; font-weight: bold;">1</span><span style="color: #000000;">;
</span><span style="color: #0000ff;">end</span> <span style="color: #0000ff;">while</span><span style="color: #000000;">;
</span><span style="color: #0000ff;">end</span><span style="color: #000000;">$$
delimiter ;</span></pre>
<div class="cnblogs_code_toolbar"><span class="cnblogs_code_copy"><a href="javascript:void(0);" onclick="copyCnblogsCode(this)" title="复制代码"><img src="//common.cnblogs.com/images/copycode.gif" alt="复制代码"></a></span></div></div>
</div>
<p class="01">当前环境下，&nbsp;<span class="cnblogs_code">innodb_flush_log_at_trx_commit</span>&nbsp;的值为1，即每次提交都刷日志到磁盘。测试此时插入10W条记录的时间。</p>
<div>
<div class="cnblogs_code">
<pre>mysql<span style="color: #808080;">&gt;</span> call <span style="color: #0000ff;">proc</span>(<span style="color: #800000; font-weight: bold;">100000</span><span style="color: #000000;">);
Query OK, </span><span style="color: #800000; font-weight: bold;">0</span> rows affected (<span style="color: #800000; font-weight: bold;">15.48</span> sec)</pre>
</div>
</div>
<p class="01">结果是15.48秒。</p>
<p class="01">再测试值为2的时候，即每次提交都刷新到os buffer，但每秒才刷入磁盘中。</p>
<div>
<div class="cnblogs_code">
<pre>mysql<span style="color: #808080;">&gt;</span> <span style="color: #0000ff;">set</span> <span style="color: #008000; font-weight: bold;">@@global</span>.innodb_flush_log_at_trx_commit<span style="color: #808080;">=</span><span style="color: #800000; font-weight: bold;">2</span><span style="color: #000000;">;    
mysql</span><span style="color: #808080;">&gt;</span> <span style="color: #0000ff;">truncate</span><span style="color: #000000;"> test_flush_log;

mysql</span><span style="color: #808080;">&gt;</span> call <span style="color: #0000ff;">proc</span>(<span style="color: #800000; font-weight: bold;">100000</span><span style="color: #000000;">);
Query OK, </span><span style="color: #800000; font-weight: bold;">0</span> rows affected (<span style="color: #800000; font-weight: bold;">3.41</span> sec)</pre>
</div>
</div>
<p class="01">结果插入时间大减，只需3.41秒。</p>
<p class="01">最后测试值为0的时候，即每秒才刷到os buffer和磁盘。</p>
<div>
<div class="cnblogs_code">
<pre>mysql<span style="color: #808080;">&gt;</span> <span style="color: #0000ff;">set</span> <span style="color: #008000; font-weight: bold;">@@global</span>.innodb_flush_log_at_trx_commit<span style="color: #808080;">=</span><span style="color: #800000; font-weight: bold;">0</span><span style="color: #000000;">;
mysql</span><span style="color: #808080;">&gt;</span> <span style="color: #0000ff;">truncate</span><span style="color: #000000;"> test_flush_log;

mysql</span><span style="color: #808080;">&gt;</span> call <span style="color: #0000ff;">proc</span>(<span style="color: #800000; font-weight: bold;">100000</span><span style="color: #000000;">);
Query OK, </span><span style="color: #800000; font-weight: bold;">0</span> rows affected (<span style="color: #800000; font-weight: bold;">2.10</span> sec)</pre>
</div>
</div>
<p class="01">结果只有2.10秒。</p>
<p class="01">最后可以发现，其实值为2和0的时候，它们的差距并不太大，但2却比0要安全的多。它们都是每秒从os buffer刷到磁盘，它们之间的时间差体现在log buffer刷到os buffer上。因为将log buffer中的日志刷新到os buffer只是内存数据的转移，并没有太大的开销，所以每次提交和每秒刷入差距并不大。可以测试插入更多的数据来比较，以下是插入100W行数据的情况。从结果可见，值为2和0的时候差距并不大，但值为1的性能却差太多。</p>
<p class="01"><img src="https://images2018.cnblogs.com/blog/733013/201805/733013-20180508105836098-1767966445.png" alt=""></p>
<p class="01">尽管设置为0和2可以大幅度提升插入性能，但是在故障的时候可能会丢失1秒钟数据，这1秒钟很可能有大量的数据，从上面的测试结果看，100W条记录也只消耗了20多秒，1秒钟大约有4W-5W条数据，尽管上述插入的数据简单，但却说明了数据丢失的大量性。<span style="color: #ff0000;"><strong>更好的插入数据的做法是将值设置为</strong><strong>1</strong><strong>，然后修改存储过程，将每次循环都提交修改为只提交一次</strong></span><strong>，</strong>这样既能保证数据的一致性，也能提升性能，修改如下：</p>
<div>
<div class="cnblogs_code"><div class="cnblogs_code_toolbar"><span class="cnblogs_code_copy"><a href="javascript:void(0);" onclick="copyCnblogsCode(this)" title="复制代码"><img src="//common.cnblogs.com/images/copycode.gif" alt="复制代码"></a></span></div>
<pre><span style="color: #0000ff;">drop</span> <span style="color: #0000ff;">procedure</span> <span style="color: #0000ff;">if</span> <span style="color: #808080;">exists</span> <span style="color: #0000ff;">proc</span><span style="color: #000000;">;
delimiter $$
</span><span style="color: #0000ff;">create</span> <span style="color: #0000ff;">procedure</span> <span style="color: #0000ff;">proc</span>(i <span style="color: #0000ff;">int</span><span style="color: #000000;">)
</span><span style="color: #0000ff;">begin</span>
<span style="color: #0000ff;">declare</span> s <span style="color: #0000ff;">int</span> <span style="color: #0000ff;">default</span> <span style="color: #800000; font-weight: bold;">1</span><span style="color: #000000;">;
</span><span style="color: #0000ff;">declare</span> c <span style="color: #0000ff;">char</span>(<span style="color: #800000; font-weight: bold;">50</span>) <span style="color: #0000ff;">default</span> repeat(<span style="color: #ff0000;">'</span><span style="color: #ff0000;">a</span><span style="color: #ff0000;">'</span>,<span style="color: #800000; font-weight: bold;">50</span><span style="color: #000000;">);
start </span><span style="color: #0000ff;">transaction</span><span style="color: #000000;">;
</span><span style="color: #0000ff;">while</span> s<span style="color: #808080;">&lt;=</span><span style="color: #000000;">i DO
</span><span style="color: #0000ff;">insert</span> <span style="color: #0000ff;">into</span> test_flush_log <span style="color: #0000ff;">values</span>(<span style="color: #0000ff;">null</span><span style="color: #000000;">,c);
</span><span style="color: #0000ff;">set</span> s<span style="color: #808080;">=</span>s<span style="color: #808080;">+</span><span style="color: #800000; font-weight: bold;">1</span><span style="color: #000000;">;
</span><span style="color: #0000ff;">end</span> <span style="color: #0000ff;">while</span><span style="color: #000000;">;
</span><span style="color: #0000ff;">commit</span><span style="color: #000000;">;
</span><span style="color: #0000ff;">end</span><span style="color: #000000;">$$
delimiter ;</span></pre>
<div class="cnblogs_code_toolbar"><span class="cnblogs_code_copy"><a href="javascript:void(0);" onclick="copyCnblogsCode(this)" title="复制代码"><img src="//common.cnblogs.com/images/copycode.gif" alt="复制代码"></a></span></div></div>
</div>
<p>测试值为1时的情况。</p>
<div>
<div class="cnblogs_code">
<pre>mysql<span style="color: #808080;">&gt;</span> <span style="color: #0000ff;">set</span> <span style="color: #008000; font-weight: bold;">@@global</span>.innodb_flush_log_at_trx_commit<span style="color: #808080;">=</span><span style="color: #800000; font-weight: bold;">1</span><span style="color: #000000;">;
mysql</span><span style="color: #808080;">&gt;</span> <span style="color: #0000ff;">truncate</span><span style="color: #000000;"> test_flush_log;

mysql</span><span style="color: #808080;">&gt;</span> call <span style="color: #0000ff;">proc</span>(<span style="color: #800000; font-weight: bold;">1000000</span><span style="color: #000000;">);
Query OK, </span><span style="color: #800000; font-weight: bold;">0</span> rows affected (<span style="color: #800000; font-weight: bold;">11.26</span> sec)</pre>
</div>
</div>
<p><a name="blog1.3"></a></p>
<h2 id="auto_id_3">1.3 日志块(log block)</h2>
<p class="01">innodb存储引擎中，redo log以块为单位进行存储的，每个块占512字节，这称为redo log block。所以不管是log buffer中还是os buffer中以及redo log file on disk中，都是这样以512字节的块存储的。</p>
<p class="01">每个redo log block由3部分组成：<span style="color: #ff0000;"><strong>日志块头、日志块尾和日志主体</strong></span>。其中日志块头占用12字节，日志块尾占用8字节，所以每个redo log block的日志主体部分只有512-12-8=492字节。</p>
<p class="a"><img src="https://images2018.cnblogs.com/blog/733013/201805/733013-20180508182701906-2079813573.png" alt=""></p>
<p class="01">因为redo log记录的是数据页的变化，当一个数据页产生的变化需要使用超过492字节()的redo log来记录，那么就会使用多个redo log block来记录该数据页的变化。</p>
<p class="01">日志块头包含4部分：</p>
<ul>
<li class="a0"> log_block_hdr_no：(4字节)该日志块在redo log buffer中的位置ID。</li>
<li class="a0"> log_block_hdr_data_len：(2字节)该log block中已记录的log大小。写满该log block时为0x200，表示512字节。</li>
<li class="a0"> log_block_first_rec_group：(2字节)该log block中第一个log的开始偏移位置。</li>
<li class="a0"> lock_block_checkpoint_no：(4字节)写入检查点信息的位置。</li>
</ul>
<p class="01">关于log block块头的第三部分&nbsp;<span class="cnblogs_code">log_block_first_rec_group</span>&nbsp;，因为有时候一个数据页产生的日志量超出了一个日志块，这是需要用多个日志块来记录该页的相关日志。例如，某一数据页产生了552字节的日志量，那么需要占用两个日志块，第一个日志块占用492字节，第二个日志块需要占用60个字节，那么对于第二个日志块来说，它的第一个log的开始位置就是73字节(60+12)。如果该部分的值和&nbsp;<span class="cnblogs_code">log_block_hdr_data_len</span>&nbsp;相等，则说明该log block中没有新开始的日志块，即表示该日志块用来延续前一个日志块。</p>
<p class="01">日志尾只有一个部分：&nbsp;<span class="cnblogs_code">log_block_trl_no</span>&nbsp;，该值和块头的&nbsp;<span class="cnblogs_code">log_block_hdr_no</span>&nbsp;相等。</p>
<p class="01">上面所说的是一个日志块的内容，在redo log buffer或者redo log file on disk中，由很多log block组成。如下图：</p>
<p><img src="https://images2018.cnblogs.com/blog/733013/201805/733013-20180508182756285-1761418702.png" alt=""></p>
<p><a name="blog1.4"></a></p>
<h2 id="auto_id_4">1.4 log group和redo log file</h2>
<p class="01">log group表示的是redo log group，一个组内由多个大小完全相同的redo log file组成。组内redo log file的数量由变量&nbsp;<span class="cnblogs_code">innodb_log_files_group</span>&nbsp;决定，默认值为2，即两个redo log file。这个组是一个逻辑的概念，并没有真正的文件来表示这是一个组，但是可以通过变量&nbsp;<span class="cnblogs_code">innodb_log_group_home_dir</span>&nbsp;来定义组的目录，redo log file都放在这个目录下，默认是在datadir下。</p>
<div>
<div class="cnblogs_code"><div class="cnblogs_code_toolbar"><span class="cnblogs_code_copy"><a href="javascript:void(0);" onclick="copyCnblogsCode(this)" title="复制代码"><img src="//common.cnblogs.com/images/copycode.gif" alt="复制代码"></a></span></div>
<pre>mysql<span style="color: #808080;">&gt;</span> show global variables <span style="color: #808080;">like</span> "innodb_log<span style="color: #808080;">%</span><span style="color: #000000;">";
</span><span style="color: #808080;">+</span><span style="color: #008080;">--</span><span style="color: #008080;">---------------------------+----------+</span>
<span style="color: #808080;">|</span> Variable_name               <span style="color: #808080;">|</span> Value    <span style="color: #808080;">|</span>
<span style="color: #808080;">+</span><span style="color: #008080;">--</span><span style="color: #008080;">---------------------------+----------+</span>
<span style="color: #808080;">|</span> innodb_log_buffer_size      <span style="color: #808080;">|</span> <span style="color: #800000; font-weight: bold;">8388608</span>  <span style="color: #808080;">|</span>
<span style="color: #808080;">|</span> innodb_log_compressed_pages <span style="color: #808080;">|</span> <span style="color: #0000ff;">ON</span>       <span style="color: #808080;">|</span>
<span style="color: #808080;">|</span> innodb_log_file_size        <span style="color: #808080;">|</span> <span style="color: #800000; font-weight: bold;">50331648</span> <span style="color: #808080;">|</span>
<span style="color: #808080;">|</span> innodb_log_files_in_group   <span style="color: #808080;">|</span> <span style="color: #800000; font-weight: bold;">2</span>        <span style="color: #808080;">|</span>
<span style="color: #808080;">|</span> innodb_log_group_home_dir   <span style="color: #808080;">|</span> .<span style="color: #808080;">/</span>       <span style="color: #808080;">|</span>
<span style="color: #808080;">+</span><span style="color: #008080;">--</span><span style="color: #008080;">---------------------------+----------+</span>

<span style="color: #ff0000;">[</span><span style="color: #ff0000;">root@xuexi data</span><span style="color: #ff0000;">]</span># ll <span style="color: #808080;">/</span>mydata<span style="color: #808080;">/</span>data<span style="color: #808080;">/</span>ib<span style="color: #808080;">*</span>
<span style="color: #808080;">-</span>rw<span style="color: #808080;">-</span>rw<span style="color: #008080;">--</span><span style="color: #008080;">-- 1 mysql mysql 79691776 Mar 30 23:12 /mydata/data/ibdata1</span>
<span style="color: #808080;">-</span>rw<span style="color: #808080;">-</span>rw<span style="color: #008080;">--</span><span style="color: #008080;">-- 1 mysql mysql 50331648 Mar 30 23:12 /mydata/data/ib_logfile0</span>
<span style="color: #808080;">-</span>rw<span style="color: #808080;">-</span>rw<span style="color: #008080;">--</span><span style="color: #008080;">-- 1 mysql mysql 50331648 Mar 30 23:12 /mydata/data/ib_logfile1</span></pre>
<div class="cnblogs_code_toolbar"><span class="cnblogs_code_copy"><a href="javascript:void(0);" onclick="copyCnblogsCode(this)" title="复制代码"><img src="//common.cnblogs.com/images/copycode.gif" alt="复制代码"></a></span></div></div>
</div>
<p class="01">可以看到在默认的数据目录下，有两个ib_logfile开头的文件，它们就是log group中的redo log file，而且它们的大小完全一致且等于变量&nbsp;<span class="cnblogs_code">innodb_log_file_size</span>&nbsp;定义的值。第一个文件ibdata1是在没有开启&nbsp;<span class="cnblogs_code">innodb_file_per_table</span>&nbsp;时的共享表空间文件，对应于开启&nbsp;<span class="cnblogs_code">innodb_file_per_table</span>&nbsp;时的.ibd文件。</p>
<p class="01">在innodb将log buffer中的redo log block刷到这些log file中时，会以追加写入的方式循环轮训写入。即先在第一个log file（即ib_logfile0）的尾部追加写，直到满了之后向第二个log file（即ib_logfile1）写。当第二个log file满了会清空一部分第一个log file继续写入。</p>
<p class="01">由于是将log buffer中的日志刷到log file，所以在log file中记录日志的方式也是log block的方式。</p>
<p class="01">在每个组的第一个redo log file中，前2KB记录4个特定的部分，从2KB之后才开始记录log block。除了第一个redo log file中会记录，log group中的其他log file不会记录这2KB，但是却会腾出这2KB的空间。如下：</p>
<p class="a"><img src="https://images2018.cnblogs.com/blog/733013/201805/733013-20180508183757511-1174307952.png" alt=""></p>
<p class="01">redo log file的大小对innodb的性能影响非常大，设置的太大，恢复的时候就会时间较长，设置的太小，就会导致在写redo log的时候循环切换redo log file。</p>
<p><a name="blog1.5"></a></p>
<h2 id="auto_id_5">1.5 redo log的格式</h2>
<p class="01">因为innodb存储引擎存储数据的单元是页(和SQL Server中一样)，所以redo log也是基于页的格式来记录的。默认情况下，innodb的页大小是16KB(由&nbsp;<span class="cnblogs_code">innodb_page_size</span>&nbsp;变量控制)，一个页内可以存放非常多的log block(每个512字节)，而log block中记录的又是数据页的变化。</p>
<p class="01">其中log block中492字节的部分是log body，该log body的格式分为4部分：</p>
<ul>
<li class="a">redo_log_type：占用1个字节，表示redo log的日志类型。</li>
<li class="a">space：表示表空间的ID，采用压缩的方式后，占用的空间可能小于4字节。</li>
<li class="a">page_no：表示页的偏移量，同样是压缩过的。</li>
<li class="a">redo_log_body表示每个重做日志的数据部分，恢复时会调用相应的函数进行解析。例如insert语句和delete语句写入redo log的内容是不一样的。</li>
</ul>
<p class="01">如下图，分别是insert和delete大致的记录方式。</p>
<p><img src="https://images2018.cnblogs.com/blog/733013/201805/733013-20180508184303598-1449455496.png" alt=""></p>
<p><a name="blog1.6"></a></p>
<h2 id="auto_id_6">1.6 日志刷盘的规则</h2>
<p class="01">log buffer中未刷到磁盘的日志称为脏日志(dirty log)。</p>
<p class="01">在上面的说过，默认情况下事务每次提交的时候都会刷事务日志到磁盘中，这是因为变量&nbsp;<span class="cnblogs_code">innodb_flush_log_at_trx_commit</span>&nbsp;的值为1。但是innodb不仅仅只会在有commit动作后才会刷日志到磁盘，这只是innodb存储引擎刷日志的规则之一。</p>
<p class="01">刷日志到磁盘有以下几种规则：</p>
<p class="a"><span style="color: #ff0000;"><strong>1.发出commit动作时。已经说明过，commit发出后是否刷日志由变量&nbsp;<span class="cnblogs_code">innodb_flush_log_at_trx_commit</span>&nbsp;控制。</strong></span></p>
<p class="a"><span style="color: #ff0000;"><strong>2.每秒刷一次。这个刷日志的频率由变量&nbsp;<span class="cnblogs_code">innodb_flush_log_at_timeout</span>&nbsp;值决定，默认是1秒。要注意，这个刷日志频率和commit动作无关。</strong></span></p>
<p class="a"><span style="color: #ff0000;"><strong>3.当log buffer中已经使用的内存超过一半时。</strong></span></p>
<p class="a"><span style="color: #ff0000;"><strong>4.当有checkpoint时，checkpoint在一定程度上代表了刷到磁盘时日志所处的LSN位置。</strong></span></p>
<p><a name="blog1.7"></a></p>
<h2 id="auto_id_7">1.7 数据页刷盘的规则及checkpoint</h2>
<p class="01">内存中(buffer pool)未刷到磁盘的数据称为脏数据(dirty data)。由于数据和日志都以页的形式存在，所以脏页表示脏数据和脏日志。</p>
<p class="01">上一节介绍了日志是何时刷到磁盘的，不仅仅是日志需要刷盘，脏数据页也一样需要刷盘。</p>
<p class="01"><strong><span style="color: #ff0000;">在innodb中，数据刷盘的规则只有一个：checkpoint。</span></strong>但是触发checkpoint的情况却有几种。<span style="color: #ff0000;"><strong>不管怎样，</strong><strong>checkpoint</strong><strong>触发后，会将buffer</strong><strong>中脏数据页和脏日志页都刷到磁盘。</strong></span></p>
<p class="01">innodb存储引擎中checkpoint分为两种：</p>
<ul>
<li class="a">sharp checkpoint：在重用redo log文件(例如切换日志文件)的时候，将所有已记录到redo log中对应的脏数据刷到磁盘。</li>
<li class="a">fuzzy checkpoint：一次只刷一小部分的日志到磁盘，而非将所有脏日志刷盘。有以下几种情况会触发该检查点：
<ul>
<li>master thread checkpoint：由master线程控制，<strong><span style="color: #ff0000;">每秒或每10秒</span></strong>刷入一定比例的脏页到磁盘。</li>
<li>flush_lru_list checkpoint：从MySQL5.6开始可通过&nbsp;<span class="cnblogs_code">innodb_page_cleaners</span>&nbsp;变量指定专门负责脏页刷盘的page cleaner线程的个数，该线程的目的是为了保证lru列表有可用的空闲页。</li>
<li>async/sync flush checkpoint：同步刷盘还是异步刷盘。例如还有非常多的脏页没刷到磁盘(非常多是多少，有比例控制)，这时候会选择同步刷到磁盘，但这很少出现；如果脏页不是很多，可以选择异步刷到磁盘，如果脏页很少，可以暂时不刷脏页到磁盘</li>
<li>dirty page too much checkpoint：脏页太多时强制触发检查点，目的是为了保证缓存有足够的空闲空间。too much的比例由变量&nbsp;<span class="cnblogs_code">innodb_max_dirty_pages_pct</span>&nbsp;控制，MySQL 5.6默认的值为75，即当脏页占缓冲池的百分之75后，就强制刷一部分脏页到磁盘。</li>
</ul>
</li>
</ul>
<p class="01">由于刷脏页需要一定的时间来完成，所以记录检查点的位置是在每次刷盘结束之后才在redo log中标记的。</p>
<blockquote>
<p class="a">MySQL停止时是否将脏数据和脏日志刷入磁盘，由变量innodb_fast_shutdown={ 0|1|2 }控制，默认值为1，即停止时只做一部分purge，忽略大多数flush操作(但至少会刷日志)，在下次启动的时候再flush剩余的内容，实现fast shutdown。</p>
</blockquote>
<p><a name="blog1.8"></a></p>
<h2 id="auto_id_8">1.8 LSN超详细分析</h2>
<p class="01">LSN称为日志的逻辑序列号(log sequence number)，在innodb存储引擎中，lsn占用8个字节。LSN的值会随着日志的写入而逐渐增大。</p>
<p class="01">根据LSN，可以获取到几个有用的信息：</p>
<p class="01">1.数据页的版本信息。</p>
<p class="01">2.写入的日志总量，通过LSN开始号码和结束号码可以计算出写入的日志量。</p>
<p class="01">3.可知道检查点的位置。</p>
<p class="01">实际上还可以获得很多隐式的信息。</p>
<p class="01">LSN不仅存在于redo log中，还存在于数据页中，在每个数据页的头部，有一个<span style="color: #ff0000;"><em>fil_page_lsn</em></span>记录了当前页最终的LSN值是多少。通过数据页中的LSN值和redo log中的LSN值比较，如果页中的LSN值小于redo log中LSN值，则表示数据丢失了一部分，这时候可以通过redo log的记录来恢复到redo log中记录的LSN值时的状态。</p>
<p class="01">redo log的lsn信息可以通过&nbsp;<span class="cnblogs_code">show engine innodb status</span>&nbsp;来查看。MySQL 5.5版本的show结果中只有3条记录，没有pages flushed up to。</p>
<div>
<div class="cnblogs_code"><div class="cnblogs_code_toolbar"><span class="cnblogs_code_copy"><a href="javascript:void(0);" onclick="copyCnblogsCode(this)" title="复制代码"><img src="//common.cnblogs.com/images/copycode.gif" alt="复制代码"></a></span></div>
<pre>mysql<span style="color: #808080;">&gt;</span><span style="color: #000000;"> show engine innodb stauts
</span><span style="color: #008080;">--</span><span style="color: #008080;">-</span>
<span style="color: #ff00ff;">LOG</span>
<span style="color: #008080;">--</span><span style="color: #008080;">-</span>
<span style="color: #ff00ff;">Log</span> sequence <span style="color: #0000ff;">number</span> <span style="color: #800000; font-weight: bold;">2225502463</span>
<span style="color: #ff00ff;">Log</span> flushed up <span style="color: #0000ff;">to</span>   <span style="color: #800000; font-weight: bold;">2225502463</span><span style="color: #000000;">
Pages flushed up </span><span style="color: #0000ff;">to</span> <span style="color: #800000; font-weight: bold;">2225502463</span><span style="color: #000000;">
Last </span><span style="color: #0000ff;">checkpoint</span> at  <span style="color: #800000; font-weight: bold;">2225502463</span>
<span style="color: #800000; font-weight: bold;">0</span> pending <span style="color: #ff00ff;">log</span> writes, <span style="color: #800000; font-weight: bold;">0</span><span style="color: #000000;"> pending chkp writes
</span><span style="color: #800000; font-weight: bold;">3201299</span> <span style="color: #ff00ff;">log</span> i<span style="color: #808080;">/</span>o<span style="color: #ff0000;">'</span><span style="color: #ff0000;">s done, 0.00 log i/o</span><span style="color: #ff0000;">'</span>s<span style="color: #808080;">/</span>second</pre>
<div class="cnblogs_code_toolbar"><span class="cnblogs_code_copy"><a href="javascript:void(0);" onclick="copyCnblogsCode(this)" title="复制代码"><img src="//common.cnblogs.com/images/copycode.gif" alt="复制代码"></a></span></div></div>
</div>
<p class="01">其中：</p>
<ul>
<li class="a"><strong>log sequence number就是当前的redo log(in buffer)中的lsn；</strong></li>
<li class="a"><strong>log flushed up to是刷到redo log file on disk中的lsn；</strong></li>
<li class="a"><strong>pages flushed up to是已经刷到磁盘数据页上的LSN；</strong></li>
<li class="a"><strong>last checkpoint at是上一次检查点所在位置的LSN。</strong></li>
</ul>
<p class="01">innodb从执行修改语句开始：</p>
<p class="01">(1).首先修改内存中的数据页，并在数据页中记录LSN，暂且称之为data_in_buffer_lsn；</p>
<p class="01">(2).并且在修改数据页的同时(几乎是同时)向redo log in buffer中写入redo log，并记录下对应的LSN，暂且称之为redo_log_in_buffer_lsn；</p>
<p class="01">(3).写完buffer中的日志后，当触发了日志刷盘的几种规则时，会向redo log file on disk刷入重做日志，并在该文件中记下对应的LSN，暂且称之为redo_log_on_disk_lsn；</p>
<p class="01">(4).数据页不可能永远只停留在内存中，在某些情况下，会触发checkpoint来将内存中的脏页(数据脏页和日志脏页)刷到磁盘，所以会在本次checkpoint脏页刷盘结束时，在redo log中记录checkpoint的LSN位置，暂且称之为checkpoint_lsn。</p>
<p class="01">(5).要记录checkpoint所在位置很快，只需简单的设置一个标志即可，但是刷数据页并不一定很快，例如这一次checkpoint要刷入的数据页非常多。也就是说要刷入所有的数据页需要一定的时间来完成，中途刷入的每个数据页都会记下当前页所在的LSN，暂且称之为data_page_on_disk_lsn。</p>
<p class="01">详细说明如下图：</p>
<p class="01"><img src="https://img2018.cnblogs.com/blog/733013/201903/733013-20190321200630187-1720258576.png" alt=""></p>
<p>上图中，从上到下的横线分别代表：时间轴、buffer中数据页中记录的LSN(data_in_buffer_lsn)、磁盘中数据页中记录的LSN(data_page_on_disk_lsn)、buffer中重做日志记录的LSN(redo_log_in_buffer_lsn)、磁盘中重做日志文件中记录的LSN(redo_log_on_disk_lsn)以及检查点记录的LSN(checkpoint_lsn)。</p>
<p class="01">假设在最初时(12:0:00)所有的日志页和数据页都完成了刷盘，也记录好了检查点的LSN，这时它们的LSN都是完全一致的。</p>
<p class="01">假设此时开启了一个事务，并立刻执行了一个update操作，执行完成后，buffer中的数据页和redo log都记录好了更新后的LSN值，假设为110。这时候如果执行&nbsp;<span class="cnblogs_code">show engine innodb status</span>&nbsp;查看各LSN的值，即图中①处的位置状态，结果会是：</p>
<div class="cnblogs_code">
<pre><span style="color: #ff00ff;">log</span> sequence <span style="color: #0000ff;">number</span>(<span style="color: #800000; font-weight: bold;">110</span>) <span style="color: #808080;">&gt;</span> <span style="color: #ff00ff;">log</span> flushed up <span style="color: #0000ff;">to</span>(<span style="color: #800000; font-weight: bold;">100</span>) <span style="color: #808080;">=</span> pages flushed up <span style="color: #0000ff;">to</span> <span style="color: #808080;">=</span> last <span style="color: #0000ff;">checkpoint</span> at</pre>
</div>
<p class="01">之后又执行了一个delete语句，LSN增长到150。等到12:00:01时，触发redo log刷盘的规则(其中有一个规则是&nbsp;<span class="cnblogs_code">innodb_flush_log_at_timeout</span>&nbsp;控制的默认日志刷盘频率为1秒)，这时redo log file on disk中的LSN会更新到和redo log in buffer的LSN一样，所以都等于150，这时&nbsp;<span class="cnblogs_code">show engine innodb status</span>&nbsp;，即图中②的位置，结果将会是：</p>
<div class="cnblogs_code">
<pre><span style="color: #ff00ff;">log</span> sequence <span style="color: #0000ff;">number</span>(<span style="color: #800000; font-weight: bold;">150</span>) <span style="color: #808080;">=</span> <span style="color: #ff00ff;">log</span> flushed up <span style="color: #0000ff;">to</span> <span style="color: #808080;">&gt;</span> pages flushed up <span style="color: #0000ff;">to</span>(<span style="color: #800000; font-weight: bold;">100</span>) <span style="color: #808080;">=</span> last <span style="color: #0000ff;">checkpoint</span> at</pre>
</div>
<p class="01">再之后，执行了一个update语句，缓存中的LSN将增长到300，即图中③的位置。</p>
<p class="01">假设随后检查点出现，即图中④的位置，正如前面所说，检查点会触发数据页和日志页刷盘，但需要一定的时间来完成，所以在数据页刷盘还未完成时，检查点的LSN还是上一次检查点的LSN，但此时磁盘上数据页和日志页的LSN已经增长了，即：</p>
<div class="cnblogs_code">
<pre><span style="color: #ff00ff;">log</span> sequence <span style="color: #0000ff;">number</span> <span style="color: #808080;">&gt;</span> <span style="color: #ff00ff;">log</span> flushed up <span style="color: #0000ff;">to</span> 和 pages flushed up <span style="color: #0000ff;">to</span> <span style="color: #808080;">&gt;</span> last <span style="color: #0000ff;">checkpoint</span> at</pre>
</div>
<p class="01">但是log flushed up to和pages flushed up to的大小无法确定，因为日志刷盘可能快于数据刷盘，也可能等于，还可能是慢于。但是checkpoint机制有保护数据刷盘速度是慢于日志刷盘的：当数据刷盘速度超过日志刷盘时，将会暂时停止数据刷盘，等待日志刷盘进度超过数据刷盘。</p>
<p class="01">等到数据页和日志页刷盘完毕，即到了位置⑤的时候，所有的LSN都等于300。</p>
<p class="01">随着时间的推移到了12:00:02，即图中位置⑥，又触发了日志刷盘的规则，但此时buffer中的日志LSN和磁盘中的日志LSN是一致的，所以不执行日志刷盘，即此时&nbsp;<span class="cnblogs_code">show engine innodb status</span>&nbsp;时各种lsn都相等。</p>
<p class="01">随后执行了一个insert语句，假设buffer中的LSN增长到了800，即图中位置⑦。此时各种LSN的大小和位置①时一样。</p>
<p class="01">随后执行了提交动作，即位置⑧。默认情况下，提交动作会触发日志刷盘，但不会触发数据刷盘，所以&nbsp;<span class="cnblogs_code">show engine innodb status</span>&nbsp;的结果是：</p>
<div class="cnblogs_code">
<pre><span style="color: #ff00ff;">log</span> sequence <span style="color: #0000ff;">number</span> <span style="color: #808080;">=</span> <span style="color: #ff00ff;">log</span> flushed up <span style="color: #0000ff;">to</span> <span style="color: #808080;">&gt;</span> pages flushed up <span style="color: #0000ff;">to</span> <span style="color: #808080;">=</span> last <span style="color: #0000ff;">checkpoint</span> at</pre>
</div>
<p class="01">最后随着时间的推移，检查点再次出现，即图中位置⑨。但是这次检查点不会触发日志刷盘，因为日志的LSN在检查点出现之前已经同步了。假设这次数据刷盘速度极快，快到一瞬间内完成而无法捕捉到状态的变化，这时&nbsp;<span class="cnblogs_code">show engine innodb status</span>&nbsp;的结果将是各种LSN相等。</p>
<p><a name="blog1.9"></a></p>
<h2 id="auto_id_9">1.9 innodb的恢复行为</h2>
<p class="01">在启动innodb的时候，不管上次是正常关闭还是异常关闭，总是会进行恢复操作。</p>
<p class="01">因为redo log记录的是数据页的物理变化，因此恢复的时候速度比逻辑日志(如二进制日志)要快很多。而且，innodb自身也做了一定程度的优化，让恢复速度变得更快。</p>
<p class="01">重启innodb时，checkpoint表示已经完整刷到磁盘上data page上的LSN，因此恢复时仅需要恢复从checkpoint开始的日志部分。例如，当数据库在上一次checkpoint的LSN为10000时宕机，且事务是已经提交过的状态。启动数据库时会检查磁盘中数据页的LSN，如果数据页的LSN小于日志中的LSN，则会从检查点开始恢复。</p>
<p class="01">还有一种情况，在宕机前正处于checkpoint的刷盘过程，且数据页的刷盘进度超过了日志页的刷盘进度。这时候一宕机，数据页中记录的LSN就会大于日志页中的LSN，在重启的恢复过程中会检查到这一情况，这时超出日志进度的部分将不会重做，因为这本身就表示已经做过的事情，无需再重做。</p>
<p class="01">另外，事务日志具有幂等性，所以多次操作得到同一结果的行为在日志中只记录一次。而二进制日志不具有幂等性，多次操作会全部记录下来，在恢复的时候会多次执行二进制日志中的记录，速度就慢得多。例如，某记录中id初始值为2，通过update将值设置为了3，后来又设置成了2，在事务日志中记录的将是无变化的页，根本无需恢复；而二进制会记录下两次update操作，恢复时也将执行这两次update操作，速度比事务日志恢复更慢。</p>
<p><a name="blog1.10"></a></p>
<h2 id="auto_id_10">1.10 和redo log有关的几个变量</h2>
<ul>
<li>innodb_flush_log_at_trx_commit={0|1|2} # 指定何时将事务日志刷到磁盘，默认为1。
<ul>
<li>0表示每秒将"log buffer"同步到"os buffer"且从"os buffer"刷到磁盘日志文件中。</li>
<li>1表示每事务提交都将"log buffer"同步到"os buffer"且从"os buffer"刷到磁盘日志文件中。</li>
<li>2表示每事务提交都将"log buffer"同步到"os buffer"但每秒才从"os buffer"刷到磁盘日志文件中。</li>
</ul>
</li>
<li>innodb_log_buffer_size：# log buffer的大小，默认8M</li>
<li>innodb_log_file_size：#事务日志的大小，默认5M</li>
<li>innodb_log_files_group =2：# 事务日志组中的事务日志文件个数，默认2个</li>
<li>innodb_log_group_home_dir =./：# 事务日志组路径，当前目录表示数据目录</li>
<li>innodb_mirrored_log_groups =1：# 指定事务日志组的镜像组个数，但镜像功能好像是强制关闭的，所以只有一个log group。在MySQL5.7中该变量已经移除。</li>
</ul>
<p><a name="blog2"></a></p>
<h1 id="auto_id_11">2.undo log</h1>
<p><a name="blog2.1"></a></p>
<h2 id="auto_id_12">2.1 基本概念</h2>
<p>undo log有两个作用：提供回滚和多个行版本控制(MVCC)。</p>
<p class="01">在数据修改的时候，不仅记录了redo，还记录了相对应的undo，如果因为某些原因导致事务失败或回滚了，可以借助该undo进行回滚。</p>
<p class="01">undo log和redo log记录物理日志不一样，它是逻辑日志。<strong><span style="color: #ff0000;">可以认为当delete一条记录时，undo log中会记录一条对应的insert记录，反之亦然，当update一条记录时，它记录一条对应相反的update记录。</span></strong></p>
<p class="01">当执行rollback时，就可以从undo log中的逻辑记录读取到相应的内容并进行回滚。有时候应用到行版本控制的时候，也是通过undo log来实现的：当读取的某一行被其他事务锁定时，它可以从undo log中分析出该行记录以前的数据是什么，从而提供该行版本信息，让用户实现非锁定一致性读取。</p>
<p class="01"><span style="color: #ff0000;"><strong>undo log</strong><strong>是采用段(segment)</strong><strong>的方式来记录的，每个undo</strong><strong>操作在记录的时候占用一个undo log segment</strong><strong>。</strong></span></p>
<p class="01">另外，<span style="color: #ff0000;"><strong>undo log</strong><strong>也会产生redo log</strong><strong>，因为undo log</strong><strong>也要实现持久性保护。</strong></span></p>
<p><a name="blog2.2"></a></p>
<h2 id="auto_id_13">2.2 undo log的存储方式</h2>
<p class="01">innodb存储引擎对undo的管理采用段的方式。<span style="color: #ff0000;"><strong>rollback segment</strong><strong>称为回滚段，每个回滚段中有1024</strong><strong>个undo log segment</strong><strong>。</strong></span></p>
<p class="01">在以前老版本，只支持1个rollback segment，这样就只能记录1024个undo log segment。后来MySQL5.5可以支持128个rollback segment，即支持128*1024个undo操作，还可以通过变量&nbsp;<span class="cnblogs_code">innodb_undo_logs</span>&nbsp;(5.6版本以前该变量是&nbsp;<span class="cnblogs_code">innodb_rollback_segments</span>&nbsp;)自定义多少个rollback segment，默认值为128。</p>
<p class="01">undo log默认存放在共享表空间中。</p>
<div>
<div class="cnblogs_code">
<pre><span style="color: #ff0000;">[</span><span style="color: #ff0000;">root@xuexi data</span><span style="color: #ff0000;">]</span># ll <span style="color: #808080;">/</span>mydata<span style="color: #808080;">/</span>data<span style="color: #808080;">/</span>ib<span style="color: #808080;">*</span>
<span style="color: #808080;">-</span>rw<span style="color: #808080;">-</span>rw<span style="color: #008080;">--</span><span style="color: #008080;">-- 1 mysql mysql 79691776 Mar 31 01:42 <em><strong><span style="color: #ff0000;">/mydata/data/ibdata1</span></strong></em></span>
<span style="color: #808080;">-</span>rw<span style="color: #808080;">-</span>rw<span style="color: #008080;">--</span><span style="color: #008080;">-- 1 mysql mysql 50331648 Mar 31 01:42 /mydata/data/ib_logfile0</span>
<span style="color: #808080;">-</span>rw<span style="color: #808080;">-</span>rw<span style="color: #008080;">--</span><span style="color: #008080;">-- 1 mysql mysql 50331648 Mar 31 01:42 /mydata/data/ib_logfile1</span></pre>
</div>
</div>
<p class="01">如果开启了&nbsp;<span class="cnblogs_code">innodb_file_per_table</span>&nbsp;，将放在每个表的.ibd文件中。</p>
<p class="01">在MySQL5.6中，undo的存放位置还可以通过变量&nbsp;<span class="cnblogs_code">innodb_undo_directory</span>&nbsp;来自定义存放目录，默认值为"."表示datadir。</p>
<p class="01">默认rollback segment全部写在一个文件中，但可以通过设置变量&nbsp;<span class="cnblogs_code">innodb_undo_tablespaces</span>&nbsp;平均分配到多少个文件中。该变量默认值为0，即全部写入一个表空间文件。该变量为静态变量，只能在数据库示例停止状态下修改，如写入配置文件或启动时带上对应参数。但是innodb存储引擎在启动过程中提示，不建议修改为非0的值，如下：</p>
<div class="cnblogs_code">
<pre><span style="color: #800000; font-weight: bold;">2017</span><span style="color: #808080;">-</span><span style="color: #800000; font-weight: bold;">03</span><span style="color: #808080;">-</span><span style="color: #800000; font-weight: bold;">31</span> <span style="color: #800000; font-weight: bold;">13</span>:<span style="color: #800000; font-weight: bold;">16</span>:<span style="color: #800000; font-weight: bold;">00</span> 7f665bfab720 InnoDB: Expected <span style="color: #0000ff;">to</span> <span style="color: #0000ff;">open</span> <span style="color: #800000; font-weight: bold;">3</span><span style="color: #000000;"> undo tablespaces but was able
</span><span style="color: #800000; font-weight: bold;">2017</span><span style="color: #808080;">-</span><span style="color: #800000; font-weight: bold;">03</span><span style="color: #808080;">-</span><span style="color: #800000; font-weight: bold;">31</span> <span style="color: #800000; font-weight: bold;">13</span>:<span style="color: #800000; font-weight: bold;">16</span>:<span style="color: #800000; font-weight: bold;">00</span> 7f665bfab720 InnoDB: <span style="color: #0000ff;">to</span> find <span style="color: #0000ff;">only</span> <span style="color: #800000; font-weight: bold;">0</span><span style="color: #000000;"> undo tablespaces.
</span><span style="color: #800000; font-weight: bold;">2017</span><span style="color: #808080;">-</span><span style="color: #800000; font-weight: bold;">03</span><span style="color: #808080;">-</span><span style="color: #800000; font-weight: bold;">31</span> <span style="color: #800000; font-weight: bold;">13</span>:<span style="color: #800000; font-weight: bold;">16</span>:<span style="color: #800000; font-weight: bold;">00</span> 7f665bfab720 InnoDB: <span style="color: #0000ff;">Set</span> the innodb_undo_tablespaces parameter <span style="color: #0000ff;">to</span><span style="color: #000000;"> the
</span><span style="color: #800000; font-weight: bold;">2017</span><span style="color: #808080;">-</span><span style="color: #800000; font-weight: bold;">03</span><span style="color: #808080;">-</span><span style="color: #800000; font-weight: bold;">31</span> <span style="color: #800000; font-weight: bold;">13</span>:<span style="color: #800000; font-weight: bold;">16</span>:<span style="color: #800000; font-weight: bold;">00</span> 7f665bfab720 InnoDB: correct value <span style="color: #808080;">and</span> retry. <em><strong><span style="color: #ff0000;">Suggested value is 0</span></strong></em></pre>
</div>
<p><a name="blog2.3"></a></p>
<h2 id="auto_id_14">2.3 和undo log相关的变量</h2>
<p>undo相关的变量在MySQL5.6中已经变得很少。如下：它们的意义在上文中已经解释了。</p>
<div>
<div class="cnblogs_code"><div class="cnblogs_code_toolbar"><span class="cnblogs_code_copy"><a href="javascript:void(0);" onclick="copyCnblogsCode(this)" title="复制代码"><img src="//common.cnblogs.com/images/copycode.gif" alt="复制代码"></a></span></div>
<pre> mysql<span style="color: #808080;">&gt;</span> show variables <span style="color: #808080;">like</span> "<span style="color: #808080;">%</span>undo<span style="color: #808080;">%</span><span style="color: #000000;">";
</span><span style="color: #808080;">+</span><span style="color: #008080;">--</span><span style="color: #008080;">-----------------------+-------+</span>
<span style="color: #808080;">|</span> Variable_name           <span style="color: #808080;">|</span> Value <span style="color: #808080;">|</span>
<span style="color: #808080;">+</span><span style="color: #008080;">--</span><span style="color: #008080;">-----------------------+-------+</span>
<span style="color: #808080;">|</span> innodb_undo_directory   <span style="color: #808080;">|</span> .     <span style="color: #808080;">|</span>
<span style="color: #808080;">|</span> innodb_undo_logs        <span style="color: #808080;">|</span> <span style="color: #800000; font-weight: bold;">128</span>   <span style="color: #808080;">|</span>
<span style="color: #808080;">|</span> innodb_undo_tablespaces <span style="color: #808080;">|</span> <span style="color: #800000; font-weight: bold;">0</span>     <span style="color: #808080;">|</span>
<span style="color: #808080;">+</span><span style="color: #008080;">--</span><span style="color: #008080;">-----------------------+-------+</span></pre>
<div class="cnblogs_code_toolbar"><span class="cnblogs_code_copy"><a href="javascript:void(0);" onclick="copyCnblogsCode(this)" title="复制代码"><img src="//common.cnblogs.com/images/copycode.gif" alt="复制代码"></a></span></div></div>
<p><a name="blog2.4"></a></p>
</div>
<h2 id="auto_id_15">2.4 delete/update操作的内部机制</h2>
<p class="01">当事务提交的时候，innodb不会立即删除undo log，因为后续还可能会用到undo log，如隔离级别为repeatable read时，事务读取的都是开启事务时的最新提交行版本，只要该事务不结束，该行版本就不能删除，即undo log不能删除。</p>
<p class="01">但是在事务提交的时候，会将该事务对应的undo log放入到删除列表中，未来通过purge来删除。并且提交事务时，还会判断undo log分配的页是否可以重用，如果可以重用，则会分配给后面来的事务，避免为每个独立的事务分配独立的undo log页而浪费存储空间和性能。</p>
<p class="01">通过undo log记录delete和update操作的结果发现：(insert操作无需分析，就是插入行而已)</p>
<ul>
<li class="a0">delete操作实际上不会直接删除，而是将delete对象打上delete flag，标记为删除，最终的删除操作是purge线程完成的。</li>
<li class="a0">update分为两种情况：update的列是否是主键列。
<ul>
<li class="a1">如果不是主键列，在undo log中直接反向记录是如何update的。即update是直接进行的。</li>
<li class="a1">如果是主键列，update分两部执行：先删除该行，再插入一行目标行。</li>
</ul>
</li>
</ul>
<p>&nbsp;<a name="blog3"></a></p>
<h1 id="auto_id_16">3.binlog和事务日志的先后顺序及group commit</h1>
<p class="01">如果事务不是只读事务，即涉及到了数据的修改，默认情况下会在commit的时候调用fsync()将日志刷到磁盘，保证事务的持久性。</p>
<p class="01">但是一次刷一个事务的日志性能较低，特别是事务集中在某一时刻时事务量非常大的时候。innodb提供了group commit功能，可以将多个事务的事务日志通过一次fsync()刷到磁盘中。</p>
<p class="01">因为事务在提交的时候不仅会记录事务日志，还会记录二进制日志，但是它们谁先记录呢？二进制日志是MySQL的上层日志，先于存储引擎的事务日志被写入。</p>
<p class="01">在MySQL5.6以前，当事务提交(即发出commit指令)后，MySQL接收到该信号进入commit prepare阶段；进入prepare阶段后，立即写内存中的二进制日志，写完内存中的二进制日志后就相当于确定了commit操作；然后开始写内存中的事务日志；最后将二进制日志和事务日志刷盘，它们如何刷盘，分别由变量&nbsp;<span class="cnblogs_code">sync_binlog</span>&nbsp;和&nbsp;<span class="cnblogs_code">innodb_flush_log_at_trx_commit</span>&nbsp;控制。</p>
<p class="01">但因为要保证二进制日志和事务日志的一致性，在提交后的prepare阶段会启用一个<span style="color: #ff0000;"><strong>prepare_commit_mutex</strong></span>锁来保证它们的顺序性和一致性。但这样会导致开启二进制日志后group commmit失效，特别是在主从复制结构中，几乎都会开启二进制日志。</p>
<p class="01">在MySQL5.6中进行了改进。提交事务时，在存储引擎层的上一层结构中会将事务按序放入一个队列，队列中的第一个事务称为leader，其他事务称为follower，leader控制着follower的行为。虽然顺序还是一样先刷二进制，再刷事务日志，但是机制完全改变了：删除了原来的prepare_commit_mutex行为，也能保证即使开启了二进制日志，group commit也是有效的。</p>
<p class="01">MySQL5.6中分为3个步骤：<span style="color: #ff0000;"><strong>flush阶段、sync阶段、commit阶段。</strong></span></p>
<p><img src="https://images2018.cnblogs.com/blog/733013/201805/733013-20180508203426454-427168291.png" alt=""></p>
<ul>
<li class="a">flush阶段：向内存中写入每个事务的二进制日志。</li>
<li class="a">sync阶段：将内存中的二进制日志刷盘。若队列中有多个事务，那么仅一次fsync操作就完成了二进制日志的刷盘操作。这在MySQL5.6中称为BLGC(binary log group commit)。</li>
<li class="a">commit阶段：leader根据顺序调用存储引擎层事务的提交，由于innodb本就支持group commit，所以解决了因为锁&nbsp;<span class="cnblogs_code">prepare_commit_mutex</span>&nbsp;而导致的group commit失效问题。</li>
</ul>
<p class="01">在flush阶段写入二进制日志到内存中，但是不是写完就进入sync阶段的，而是要等待一定的时间，多积累几个事务的binlog一起进入sync阶段，等待时间由变量&nbsp;<span class="cnblogs_code">binlog_max_flush_queue_time</span>&nbsp;决定，默认值为0表示不等待直接进入sync，设置该变量为一个大于0的值的好处是group中的事务多了，性能会好一些，但是这样会导致事务的响应时间变慢，所以建议不要修改该变量的值，除非事务量非常多并且不断的在写入和更新。</p>
<p class="01">进入到sync阶段，会将binlog从内存中刷入到磁盘，刷入的数量和单独的二进制日志刷盘一样，由变量&nbsp;<span class="cnblogs_code">sync_binlog</span>&nbsp;控制。</p>
<p class="01">当有一组事务在进行commit阶段时，其他新事务可以进行flush阶段，它们本就不会相互阻塞，所以group commit会不断生效。当然，group commit的性能和队列中的事务数量有关，如果每次队列中只有1个事务，那么group commit和单独的commit没什么区别，当队列中事务越来越多时，即提交事务越多越快时，group commit的效果越明显。</p>
<p>&nbsp;</p><p><strong><span style="font-size: 18px;color: red">Shell技术交流群：921383787</span></strong><br></p><p><strong><span style="font-size: 18px;">如果觉得文章不错，不妨给个<font color="#0000ff" size="6">打赏</font>，写作不易，各位的支持，能激发和鼓励我更大的写作热情。谢谢！</span></strong><br></p><img src="https://files.cnblogs.com/files/f-ck-need-u/wealipay.bmp"><p></p><p></p></div>
<div id="MySignature" style="display: block;"><div>作者：<a href="https://www.cnblogs.com/f-ck-need-u/" target="_blank">骏马金龙</a></div>
<div>出处：<a href="https://www.cnblogs.com/f-ck-need-u/" target="_blank">http://www.cnblogs.com/f-ck-need-u/</a></div>
<div>Linux运维交流群：<a href="https://jq.qq.com/?_wv=1027&amp;k=5zfAfeJ" target="_blank">710427601</a></div>
<p>
<span style="font-size: 14px; font-family: &quot;Microsoft YaHei&quot;; color: #0000ff">
<strong>
<a href="https://www.cnblogs.com/f-ck-need-u/p/7048359.html" target="_blank">
Linux&amp;shell系列文章：http://www.cnblogs.com/f-ck-need-u/p/7048359.html</a>
</strong>
<br>

<strong>
<a href="https://www.cnblogs.com/f-ck-need-u/p/7576137.html" target="_blank">
网站架构系列文章：http://www.cnblogs.com/f-ck-need-u/p/7576137.html</a>
</strong>
<br>

<strong>
<a href="https://www.cnblogs.com/f-ck-need-u/p/7586194.html" target="_blank">
MySQL/MariaDB系列文章：https://www.cnblogs.com/f-ck-need-u/p/7586194.html</a>
</strong>
<br>

<strong>
<a href="https://www.cnblogs.com/f-ck-need-u/p/9512185.html" target="_blank">
Perl系列：https://www.cnblogs.com/f-ck-need-u/p/9512185.html</a>
</strong>
<br>

<strong>
<a href="https://www.cnblogs.com/f-ck-need-u/p/9832538.html" target="_blank">
Go系列：https://www.cnblogs.com/f-ck-need-u/p/9832538.html</a>
</strong>
<br>

<strong>
<a href="https://www.cnblogs.com/f-ck-need-u/p/9832640.html" target="_blank">
Python系列：https://www.cnblogs.com/f-ck-need-u/p/9832640.html</a>
</strong>
<br>

<strong>
<a href="https://www.cnblogs.com/f-ck-need-u/p/10805545.html" target="_blank">
Ruby系列：https://www.cnblogs.com/f-ck-need-u/p/10805545.html</a>
</strong>
<br>

<strong>
<a href="https://www.cnblogs.com/f-ck-need-u/p/10481466.html" target="_blank">
操作系统系列：https://www.cnblogs.com/f-ck-need-u/p/10481466.html</a>
</strong>
<br>
</span>
</p></div>
<div class="clear"></div>
<div id="blog_post_info_block"><div id="BlogPostCategory">
分类: 
<a href="https://www.cnblogs.com/f-ck-need-u/category/1085743.html" target="_blank">数据库系列</a></div>


<div id="blog_post_info">
<div id="green_channel">
<a href="javascript:void(0);" id="green_channel_digg" onclick="DiggIt(9010872,cb_blogId,1);green_channel_success(this,'谢谢推荐！');">好文要顶</a>
<a id="green_channel_follow" onclick="follow('1e7fa90d-decd-e411-b908-9dcfd8948a71');" href="javascript:void(0);">关注我</a>
<a id="green_channel_favorite" onclick="AddToWz(cb_entryId);return false;" href="javascript:void(0);">收藏该文</a>
<a id="green_channel_weibo" href="javascript:void(0);" title="分享至新浪微博" onclick="ShareToTsina()"><img src="https://common.cnblogs.com/images/icon_weibo_24.png" alt=""></a>
<a id="green_channel_wechat" href="javascript:void(0);" title="分享至微信" onclick="shareOnWechat()"><img src="https://common.cnblogs.com/images/wechat.png" alt=""></a>
</div>
<div id="author_profile">
<div id="author_profile_info" class="author_profile_info">
<a href="https://home.cnblogs.com/u/f-ck-need-u/" target="_blank"><img src="https://pic.cnblogs.com/face/733013/20180330130915.png" class="author_avatar" alt=""></a>
<div id="author_profile_detail" class="author_profile_info">
<a href="https://home.cnblogs.com/u/f-ck-need-u/">骏马金龙</a><br>
<a href="https://home.cnblogs.com/u/f-ck-need-u/followees/">关注 - 23</a><br>
<a href="https://home.cnblogs.com/u/f-ck-need-u/followers/">粉丝 - 1472</a>
</div>
</div>
<div class="clear"></div>
<div id="author_profile_honor"></div>
<div id="author_profile_follow">
<a href="javascript:void(0);" onclick="follow('1e7fa90d-decd-e411-b908-9dcfd8948a71');return false;">+加关注</a>
</div>
</div>


<script type="text/javascript">
currentDiggType = 0;
</script></div>
<div class="clear"></div>
<div id="post_next_prev">

<a href="https://www.cnblogs.com/f-ck-need-u/p/9001061.html" class="p_n_p_prefix">« </a> 上一篇：    <a href="https://www.cnblogs.com/f-ck-need-u/p/9001061.html" title="发布于 2018-05-07 09:40">详细分析MySQL的日志(一)</a>
<br>
<a href="https://www.cnblogs.com/f-ck-need-u/p/9013458.html" class="p_n_p_prefix">» </a> 下一篇：    <a href="https://www.cnblogs.com/f-ck-need-u/p/9013458.html" title="发布于 2018-05-09 12:24">MariaDB/MySQL备份和恢复(一)：mysqldump工具用法详述</a>

</div>
</div>
</div>
<div class="postDesc">posted @ 
<span id="post-date">2018-05-08 20:49</span>&nbsp;<a href="https://www.cnblogs.com/f-ck-need-u/">骏马金龙</a> 阅读(<span id="post_view_count">33274</span>) 评论(<span id="post_comment_count">11</span>) <a href="https://i.cnblogs.com/EditPosts.aspx?postid=9010872" rel="nofollow"> 编辑</a> <a href="javascript:void(0)" onclick="AddToWz(9010872); return false;">收藏</a>
</div>
</div>
<script src="https://common.cnblogs.com/highlight/9.12.0/highlight.min.js"></script>
<script>markdown_highlight();</script>
<script>
var allowComments = true, cb_blogId = 222371, cb_blogApp = 'f-ck-need-u', cb_blogUserGuid = '1e7fa90d-decd-e411-b908-9dcfd8948a71';
var cb_entryId = 9010872, cb_entryCreatedDate = '2018-05-08 20:49', cb_postType = 1; 
loadViewCount(cb_entryId);
</script><a name="!comments"></a>
<div id="blog-comments-placeholder">

<div id="comment_pager_top">

</div>


<div class="feedback_area_title">评论列表</div>
<div class="feedbackNoItems"></div>

<div class="feedbackItem">
<div class="feedbackListSubtitle">
<div class="feedbackManage">
&nbsp;&nbsp;


<span class="comment_actions">




</span>


</div>
<!-- Title -->

<a href="#4136873" class="layer">#1楼</a>
<a name="4136873" id="comment_anchor_4136873"></a>


<!-- PostDate -->

<span class="comment_date">2018-12-11 17:03</span>


<!--NameLink-->


<a id="a_comment_author_4136873" href="https://www.cnblogs.com/grotian/" target="_blank">北方漂流</a>

<div class="feedbackCon">

<div id="comment_body_4136873" class="blog_comment_body">
“1.8 LSN超详细分析”一节中的图示，②和⑧节点上，不应该是 redo log 刷盘在先（是 redo_log_on_disk_lsn 而不是 data_page_on_disk_lsn）吗？
</div>
<div class="comment_vote">
<span class="comment_error" style="color: red"></span>
<a href="javascript:void(0);" class="comment_digg" onclick="return voteComment(4136873, 'Digg', this.parentElement, false);">
支持(1)
</a>
<a href="javascript:void(0);" class="comment_burry" onclick="return voteComment(4136873, 'Bury', this.parentElement, false);">
反对(0)
</a>
</div>
<span id="comment_4136873_avatar" style="display:none">
https://pic.cnblogs.com/face/u471665.jpg?id=22212241
</span>

</div>
</div>
</div>
<div class="feedbackItem">
<div class="feedbackListSubtitle">
<div class="feedbackManage">
&nbsp;&nbsp;


<span class="comment_actions">




</span>


</div>
<!-- Title -->

<a href="#4179391" class="layer">#2楼</a>
<a name="4179391" id="comment_anchor_4179391"></a>


<!-- PostDate -->

<span class="comment_date">2019-02-14 13:16</span>


<!--NameLink-->


<a id="a_comment_author_4179391" href="https://www.cnblogs.com/cbgu/" target="_blank">纪璋回</a>

<div class="feedbackCon">

<div id="comment_body_4179391" class="blog_comment_body">
<a href="#4136873" title="查看所回复的评论" onclick="commentManager.renderComments(0,50,4136873);">@</a>
北方漂流<br>wal机制，日志先行，我理解的是redo log刷盘先于数据页中数据修改，即redo_log_on_disk_lsn先于data_in_buffer_lsn，而非data_page_on_disk_lsn，我是这么理解的。
</div>
<div class="comment_vote">
<span class="comment_error" style="color: red"></span>
<a href="javascript:void(0);" class="comment_digg" onclick="return voteComment(4179391, 'Digg', this.parentElement, false);">
支持(0)
</a>
<a href="javascript:void(0);" class="comment_burry" onclick="return voteComment(4179391, 'Bury', this.parentElement, false);">
反对(0)
</a>
</div>
<span id="comment_4179391_avatar" style="display:none">
https://pic.cnblogs.com/face/1164322/20180305140845.png
</span>

</div>
</div>
</div>
<div class="feedbackItem">
<div class="feedbackListSubtitle">
<div class="feedbackManage">
&nbsp;&nbsp;


<span class="comment_actions">




</span>


</div>
<!-- Title -->

<a href="#4209723" class="layer">#3楼</a>
<a name="4209723" id="comment_anchor_4209723"></a>


<!-- PostDate -->

<span class="comment_date">2019-03-21 19:55</span>


<!--NameLink-->


<a id="a_comment_author_4209723" href="https://home.cnblogs.com/u/720129/" target="_blank">青春的节奏</a>

<div class="feedbackCon">

<div id="comment_body_4209723" class="blog_comment_body">
<a href="#4136873" title="查看所回复的评论" onclick="commentManager.renderComments(0,50,4136873);">@</a>
北方漂流<br>我也觉得是作者画图画反了
</div>
<div class="comment_vote">
<span class="comment_error" style="color: red"></span>
<a href="javascript:void(0);" class="comment_digg" onclick="return voteComment(4209723, 'Digg', this.parentElement, false);">
支持(0)
</a>
<a href="javascript:void(0);" class="comment_burry" onclick="return voteComment(4209723, 'Bury', this.parentElement, false);">
反对(0)
</a>
</div>


</div>
</div>
</div>
<div class="feedbackItem">
<div class="feedbackListSubtitle">
<div class="feedbackManage">
&nbsp;&nbsp;


<span class="comment_actions">




</span>


</div>
<!-- Title -->

<a href="#4209735" class="layer">#4楼</a>
<a name="4209735" id="comment_anchor_4209735"></a>
[<span class="louzhu">楼主</span>]

<!-- PostDate -->

<span class="comment_date">2019-03-21 20:04</span>


<!--NameLink-->


<a id="a_comment_author_4209735" href="https://www.cnblogs.com/f-ck-need-u/" target="_blank">骏马金龙</a>

<div class="feedbackCon">

<div id="comment_body_4209735" class="blog_comment_body">
<a href="#4136873" title="查看所回复的评论" onclick="commentManager.renderComments(0,50,4136873);">@</a>
北方漂流<br>@纪璋回<br>@青春的节奏<br>图中②和⑧节点处，data_page_on_disk确实是画错了，不应该画上去。但是下面的文字描述应该是没错的，希望没有影响到你们理解
</div>
<div class="comment_vote">
<span class="comment_error" style="color: red"></span>
<a href="javascript:void(0);" class="comment_digg" onclick="return voteComment(4209735, 'Digg', this.parentElement, false);">
支持(0)
</a>
<a href="javascript:void(0);" class="comment_burry" onclick="return voteComment(4209735, 'Bury', this.parentElement, false);">
反对(0)
</a>
</div>
<span id="comment_4209735_avatar" style="display:none">
https://pic.cnblogs.com/face/733013/20180330130915.png
</span>

</div>
</div>
</div>
<div class="feedbackItem">
<div class="feedbackListSubtitle">
<div class="feedbackManage">
&nbsp;&nbsp;


<span class="comment_actions">




</span>


</div>
<!-- Title -->

<a href="#4266310" class="layer">#5楼</a>
<a name="4266310" id="comment_anchor_4266310"></a>


<!-- PostDate -->

<span class="comment_date">2019-05-27 19:36</span>


<!--NameLink-->


<a id="a_comment_author_4266310" href="https://www.cnblogs.com/jerry-chin/" target="_blank">Jerry.Chin</a>

<div class="feedbackCon">

<div id="comment_body_4266310" class="blog_comment_body">
NB! 作者是 MYSQL 开发人员吗？
</div>
<div class="comment_vote">
<span class="comment_error" style="color: red"></span>
<a href="javascript:void(0);" class="comment_digg" onclick="return voteComment(4266310, 'Digg', this.parentElement, false);">
支持(0)
</a>
<a href="javascript:void(0);" class="comment_burry" onclick="return voteComment(4266310, 'Bury', this.parentElement, false);">
反对(0)
</a>
</div>
<span id="comment_4266310_avatar" style="display:none">
https://pic.cnblogs.com/face/771706/20161006131623.png
</span>

</div>
</div>
</div>
<div class="feedbackItem">
<div class="feedbackListSubtitle">
<div class="feedbackManage">
&nbsp;&nbsp;


<span class="comment_actions">




</span>


</div>
<!-- Title -->

<a href="#4312799" class="layer">#6楼</a>
<a name="4312799" id="comment_anchor_4312799"></a>


<!-- PostDate -->

<span class="comment_date">2019-07-30 20:42</span>


<!--NameLink-->


<a id="a_comment_author_4312799" href="https://www.cnblogs.com/martin-chen/" target="_blank">Web游离者</a>

<div class="feedbackCon">

<div id="comment_body_4312799" class="blog_comment_body">
@骏马金龙 楼主写的很详细，我也在看《MySQL技术内幕：InnoDB存储引擎》这本书，对于redo log这一块一直有一个疑问未能找到答案，希望能从你这边得到一些帮助。<br>当一个事务启动后，会不断持续的往redo log中写入 log record，但是这个时候事务并未提交，如果这个时候出现 redo log 刷盘，那这部分未提交事务的 log record 也会保存到 redo log file 中，如果在这个时候MySQL服务宕机，重启MySQL时，怎么判断 redo log file 中的 log record 所属的事务是否已经提交呢？
</div>
<div class="comment_vote">
<span class="comment_error" style="color: red"></span>
<a href="javascript:void(0);" class="comment_digg" onclick="return voteComment(4312799, 'Digg', this.parentElement, false);">
支持(0)
</a>
<a href="javascript:void(0);" class="comment_burry" onclick="return voteComment(4312799, 'Bury', this.parentElement, false);">
反对(0)
</a>
</div>


</div>
</div>
</div>
<div class="feedbackItem">
<div class="feedbackListSubtitle">
<div class="feedbackManage">
&nbsp;&nbsp;


<span class="comment_actions">




</span>


</div>
<!-- Title -->

<a href="#4312902" class="layer">#7楼</a>
<a name="4312902" id="comment_anchor_4312902"></a>
[<span class="louzhu">楼主</span>]

<!-- PostDate -->

<span class="comment_date">2019-07-30 23:36</span>


<!--NameLink-->


<a id="a_comment_author_4312902" href="https://www.cnblogs.com/f-ck-need-u/" target="_blank">骏马金龙</a>

<div class="feedbackCon">

<div id="comment_body_4312902" class="blog_comment_body">
<a href="#4312799" title="查看所回复的评论" onclick="commentManager.renderComments(0,50,4312799);">@</a>
Web游离者<br>这个很容易判断的。每个事务都有事务ID，对应这个事务ID，提交后都有提交标记。重启服务后检查redo log的时候，发现这个事务没有对应的标记，那么就是未完成事务
</div>
<div class="comment_vote">
<span class="comment_error" style="color: red"></span>
<a href="javascript:void(0);" class="comment_digg" onclick="return voteComment(4312902, 'Digg', this.parentElement, false);">
支持(0)
</a>
<a href="javascript:void(0);" class="comment_burry" onclick="return voteComment(4312902, 'Bury', this.parentElement, false);">
反对(0)
</a>
</div>
<span id="comment_4312902_avatar" style="display:none">
https://pic.cnblogs.com/face/733013/20180330130915.png
</span>

</div>
</div>
</div>
<div class="feedbackItem">
<div class="feedbackListSubtitle">
<div class="feedbackManage">
&nbsp;&nbsp;


<span class="comment_actions">




</span>


</div>
<!-- Title -->

<a href="#4312910" class="layer">#8楼</a>
<a name="4312910" id="comment_anchor_4312910"></a>


<!-- PostDate -->

<span class="comment_date">2019-07-31 00:09</span>


<!--NameLink-->


<a id="a_comment_author_4312910" href="https://www.cnblogs.com/martin-chen/" target="_blank">Web游离者</a>

<div class="feedbackCon">

<div id="comment_body_4312910" class="blog_comment_body">
<a href="#4312902" title="查看所回复的评论" onclick="commentManager.renderComments(0,50,4312902);">@</a>
骏马金龙<br>但是我在看其它文章的时候得到的是另外的一个说法：<a href="http://www.zhdba.com/mysqlops/2012/04/06/innodb-log1/" target="_blank">MySQL数据库InnoDB存储引擎Log漫游(1)</a>。在这篇文章里说MySQL在恢复过程中redo log是没有事务性的。<br><img src="//img2018.cnblogs.com/blog/256297/201907/256297-20190731000851617-1525756580.jpg" alt="" border="0" "="">。<br>但是我觉得这篇文章里的说法还是不能解答我的问题。
</div>
<div class="comment_vote">
<span class="comment_error" style="color: red"></span>
<a href="javascript:void(0);" class="comment_digg" onclick="return voteComment(4312910, 'Digg', this.parentElement, false);">
支持(0)
</a>
<a href="javascript:void(0);" class="comment_burry" onclick="return voteComment(4312910, 'Bury', this.parentElement, false);">
反对(0)
</a>
</div>


</div>
</div>
</div>
<div class="feedbackItem">
<div class="feedbackListSubtitle">
<div class="feedbackManage">
&nbsp;&nbsp;


<span class="comment_actions">




</span>


</div>
<!-- Title -->

<a href="#4313909" class="layer">#9楼</a>
<a name="4313909" id="comment_anchor_4313909"></a>
[<span class="louzhu">楼主</span>]

<!-- PostDate -->

<span class="comment_date">2019-07-31 21:51</span>


<!--NameLink-->


<a id="a_comment_author_4313909" href="https://www.cnblogs.com/f-ck-need-u/" target="_blank">骏马金龙</a>

<div class="feedbackCon">

<div id="comment_body_4313909" class="blog_comment_body">
<a href="#4312910" title="查看所回复的评论" onclick="commentManager.renderComments(0,50,4312910);">@</a>
Web游离者<br>每个事务肯定要记录它是否完成啊，否则它如何保证事务的原子性呢？而且就算是还没有提交的事务，都能够查询到这个事务的状态，是运行中、锁等待中、回滚中、提交中，都能够查询到，查询不到的事务一定是已经提交的事务，因为它已经能够保证数据的一致性。<br>至于你说的文章中所说的没有事务性，大概可能的意思是不像逻辑事务一样，有开启和终止事务的语句描述。但是在事务日志里，一定会记录事务是从哪里开始，到那里结束，或者某条log record属于哪个事务，并且记录事务ID。<br><br>在重启mysql服务的时候，会检查事务日志的完整性。分3种情况：<br>1.在宕机之前或mysql停止之前，事务日志和数据已经全部刷到磁盘的redo log，说明事务日志是多余的，那么启动的时候，直接跳过事务日志的检查。<br>2.如果缺少事务日志文件，也会直接跳过事务日志的检查。这可能会导致重启前后数据的不一致。<br>3.你这里所描述的情况，事务日志没有刷完就宕机，也就是说，事务日志的文件是不完整的。<br><br>对于第三种情况，在重启mysql服务时它会在这里重做一部分事务日志，从最近的一个检查点(checkpoint)处开始扫描redo log，当扫描到mysql是非正常关闭时，会从此处开始进入恢复的过程，该回滚的回滚，该前滚的前滚。而未提交的事务(其事务日志的提交点还没有刷入到磁盘redo log)，显然是要回滚的。
</div>
<div class="comment_vote">
<span class="comment_error" style="color: red"></span>
<a href="javascript:void(0);" class="comment_digg" onclick="return voteComment(4313909, 'Digg', this.parentElement, false);">
支持(0)
</a>
<a href="javascript:void(0);" class="comment_burry" onclick="return voteComment(4313909, 'Bury', this.parentElement, false);">
反对(0)
</a>
</div>
<span id="comment_4313909_avatar" style="display:none">
https://pic.cnblogs.com/face/733013/20180330130915.png
</span>

</div>
</div>
</div>
<div class="feedbackItem">
<div class="feedbackListSubtitle">
<div class="feedbackManage">
&nbsp;&nbsp;


<span class="comment_actions">




</span>


</div>
<!-- Title -->

<a href="#4329496" class="layer">#10楼</a>
<a name="4329496" id="comment_anchor_4329496"></a>


<!-- PostDate -->

<span class="comment_date">2019-08-17 15:58</span>


<!--NameLink-->


<a id="a_comment_author_4329496" href="https://home.cnblogs.com/u/1771592/" target="_blank">随风奔跑2019</a>

<div class="feedbackCon">

<div id="comment_body_4329496" class="blog_comment_body">
您好 咨询一个问题，事务提交后，只是 保障redo log落盘，而数据可能还未更新到磁盘，那事务提交后立刻查询，是不是可能查不到新提交的变化呢  ？这块是怎么处理的
</div>
<div class="comment_vote">
<span class="comment_error" style="color: red"></span>
<a href="javascript:void(0);" class="comment_digg" onclick="return voteComment(4329496, 'Digg', this.parentElement, false);">
支持(0)
</a>
<a href="javascript:void(0);" class="comment_burry" onclick="return voteComment(4329496, 'Bury', this.parentElement, false);">
反对(0)
</a>
</div>


</div>
</div>
</div>
<div class="feedbackItem">
<div class="feedbackListSubtitle">
<div class="feedbackManage">
&nbsp;&nbsp;


<span class="comment_actions">




</span>


</div>
<!-- Title -->

<a href="#4329722" class="layer">#11楼</a>
<a name="4329722" id="comment_anchor_4329722"></a>
[<span class="louzhu">楼主</span>]
<span id="comment-maxId" style="display:none">4329722</span>
<span id="comment-maxDate" style="display:none">2019/8/18 上午12:06:50</span>

<!-- PostDate -->

<span class="comment_date">2019-08-18 00:06</span>


<!--NameLink-->


<a id="a_comment_author_4329722" href="https://www.cnblogs.com/f-ck-need-u/" target="_blank">骏马金龙</a>

<div class="feedbackCon">

<div id="comment_body_4329722" class="blog_comment_body">
<a href="#4329496" title="查看所回复的评论" onclick="commentManager.renderComments(0,50,4329496);">@</a>
随风奔跑2019<br>缓存中有更新好的数据啊。<br>数据更新时：<br>在缓存中，是先写缓存数据、再写缓存redo log，这样对本事务来说能保持一致性(对其它事务来说，要看事务隔离级别)。<br>在刷到磁盘时，是先redo log再刷数据的，这样保证持久性和一致性。
</div>
<div class="comment_vote">
<span class="comment_error" style="color: red"></span>
<a href="javascript:void(0);" class="comment_digg" onclick="return voteComment(4329722, 'Digg', this.parentElement, false);">
支持(0)
</a>
<a href="javascript:void(0);" class="comment_burry" onclick="return voteComment(4329722, 'Bury', this.parentElement, false);">
反对(0)
</a>
</div>
<span id="comment_4329722_avatar" style="display:none">
https://pic.cnblogs.com/face/733013/20180330130915.png
</span>

</div>
</div>
</div>


<div id="comment_pager_bottom">

</div>
</div>
<script>
var commentManager = new blogCommentManager();
commentManager.renderComments(0);
</script>

<div id="comment_form" class="commentform">
<a name="commentform"></a>
<div id="divCommentShow"></div>
<div id="comment_nav"><span id="span_refresh_tips"></span><a href="javascript:void(0);" onclick="return RefreshCommentList();" id="lnk_RefreshComments" runat="server" clientidmode="Static">刷新评论</a><a href="#" onclick="return RefreshPage();">刷新页面</a><a href="#top">返回顶部</a></div>
<div id="comment_form_container"><div class="login_tips">
注册用户登录后才能发表评论，请 
<a rel="nofollow" href="javascript:void(0);" class="underline" onclick="return login('commentform');">登录</a>
或 
<a rel="nofollow" href="javascript:void(0);" class="underline" onclick="return register();">注册</a>，
<a href="https://www.cnblogs.com/">访问</a> 网站首页。
</div></div>
<div class="ad_text_commentbox" id="ad_text_under_commentbox"></div>
<div id="ad_t2"><a href="https://cloud.tencent.com/act/pro/overseas?fromSource=gwzcw.3090393.3090393.3090393&amp;utm_medium=cpc&amp;utm_id=gwzcw.3090393.3090393.3090393" target="_blank" onclick="ga('send', 'event', 'Link', 'click', 'T2-腾讯云')">【推荐】腾讯云海外1核2G云服务器低至2折，半价续费券限量免费领取！</a><br><a href="http://click.aliyun.com/m/1000081987/" target="_blank" onclick="ga('send', 'event', 'Link', 'click', 'T2-阿里云')">【推荐】阿里云双11返场来袭，热门产品低至一折等你来抢！</a><br><a href="http://www.ucancode.com/index.htm" target="_blank" onclick="ga('send', 'event', 'Link', 'click', 'T2-工控')">【推荐】超50万行VC++源码: 大型组态工控、电力仿真CAD与GIS源码库</a><br><a href="https://www.ctyun.cn/activity/20191111?hmsr=%E7%9C%8B%E7%9C%8B-%E5%8D%9A%E5%AE%A2%E5%9B%AD-%E5%8F%8C11-1106&amp;hmpl=&amp;hmcu=&amp;hmkw=&amp;hmci=" target="_blank" onclick="ga('send', 'event', 'Link', 'click', 'T2-天翼云')">【推荐】天翼云双十一翼降到底，云主机11.11元起，抽奖送大礼</a><br><a href="https://www.uibot.com.cn/sem/bky.html" target="_blank" onclick="ga('send', 'event', 'Link', 'click', 'T2-uibot_T2')">【推荐】流程自动化专家UiBot，体系化教程成就高薪RPA工程师</a><br></div>
<div id="opt_under_post"></div>
<script async="async" src="https://www.googletagservices.com/tag/js/gpt.js"></script>
<script>
var googletag = googletag || {};
googletag.cmd = googletag.cmd || [];
</script>
<script>
googletag.cmd.push(function () {
googletag.defineSlot("/1090369/C1", [300, 250], "div-gpt-ad-1546353474406-0").addService(googletag.pubads());
googletag.defineSlot("/1090369/C2", [468, 60], "div-gpt-ad-1539008685004-0").addService(googletag.pubads());
googletag.pubads().enableSingleRequest();
googletag.enableServices();
});
</script>
<div id="cnblogs_c1" class="c_ad_block">
<div id="div-gpt-ad-1546353474406-0" style="height:250px; width:300px;" data-google-query-id="COnWq9u7ieYCFYMaKgodGR8OBw"><div id="google_ads_iframe_/1090369/C1_0__container__" style="border: 0pt none;"><iframe id="google_ads_iframe_/1090369/C1_0" title="3rd party ad content" name="google_ads_iframe_/1090369/C1_0" width="300" height="250" scrolling="no" marginwidth="0" marginheight="0" frameborder="0" srcdoc="" style="border: 0px; vertical-align: bottom;" data-google-container-id="1" data-load-complete="true"></iframe></div></div>
</div>
<div id="under_post_news"><div class="recomm-block"><b>相关博文：</b><br>·  <a title="详细分析MySQL事务日志(redolog和undolog)" href="https://www.cnblogs.com/DataArt/p/10209573.html" target="_blank" onclick="clickRecomItmem(10209573)">详细分析MySQL事务日志(redolog和undolog)</a><br>·  <a title="Mysql事务日志(Ib_logfile)" href="https://www.cnblogs.com/qianyuliang/p/9916372.html" target="_blank" onclick="clickRecomItmem(9916372)">Mysql事务日志(Ib_logfile)</a><br>·  <a title="MySQL-事务的实现-redo" href="https://www.cnblogs.com/cuisi/p/6549757.html" target="_blank" onclick="clickRecomItmem(6549757)">MySQL-事务的实现-redo</a><br>·  <a title="mysql的innodb中事务日志ib_logfile" href="https://www.cnblogs.com/xuan52rock/p/4551830.html" target="_blank" onclick="clickRecomItmem(4551830)">mysql的innodb中事务日志ib_logfile</a><br>·  <a title="MySQL IO线程及相关参数调优" href="https://www.cnblogs.com/geaozhang/p/7214257.html" target="_blank" onclick="clickRecomItmem(7214257)">MySQL IO线程及相关参数调优</a><br>»  <a target="_blank" href="https://recomm.cnblogs.com/blogpost/9010872">更多推荐...</a><div id="cnblogs_t5"><a href="https://developer.aliyun.com/ask/258350?utm_content=g_1000088952" target="_blank" onclick="ga('send', 'event', 'Link', 'click', 'T5-阿里云开发者社区')">精品问答：前端开发必懂之 HTML 技术五十问</a></div></div></div>
<div id="cnblogs_c2" class="c_ad_block">
<div id="div-gpt-ad-1539008685004-0" style="height:60px; width:468px;" data-google-query-id="COrWq9u7ieYCFYMaKgodGR8OBw">

<div id="google_ads_iframe_/1090369/C2_0__container__" style="border: 0pt none;"><iframe id="google_ads_iframe_/1090369/C2_0" title="3rd party ad content" name="google_ads_iframe_/1090369/C2_0" width="468" height="60" scrolling="no" marginwidth="0" marginheight="0" frameborder="0" srcdoc="" style="border: 0px; vertical-align: bottom;" data-google-container-id="2" data-load-complete="true"></iframe></div></div>
</div>
<div id="under_post_kb">
<div class="itnews c_ad_block">
<b>最新 IT 新闻</b>:
<br>
·              <a href="//news.cnblogs.com/n/650100/" target="_blank">大脑如何将外部信息转化为记忆？</a>
<br>
·              <a href="//news.cnblogs.com/n/650099/" target="_blank">联发科：天玑5G芯片取名与竞品990无关，1000是内部神秘数字</a>
<br>
·              <a href="//news.cnblogs.com/n/650098/" target="_blank">工信部：携号转网正式在全国提供服务</a>
<br>
·              <a href="//news.cnblogs.com/n/650097/" target="_blank">惠普第四财季业绩优于预期 终结六季度收入下滑 盘后一度涨3%</a>
<br>
·              <a href="//news.cnblogs.com/n/650096/" target="_blank">售后服务管理应用售后宝获千万级Pre-A轮融资，盈动资本投资</a>
<br>
» <a href="https://news.cnblogs.com/" title="IT 新闻" target="_blank">更多新闻...</a>
</div></div>
<div id="HistoryToday" class="c_ad_block"></div>
<script type="text/javascript">
fixPostBody();
setTimeout(function () { incrementViewCount(cb_entryId); }, 50);
deliverAdT2();
deliverAdC1();
deliverAdC2();
loadNewsAndKb();
loadBlogSignature();
LoadPostCategoriesTags(cb_blogId, cb_entryId);        LoadPostInfoBlock(cb_blogId, cb_entryId, cb_blogApp, cb_blogUserGuid);
GetPrevNextPost(cb_entryId, cb_blogId, cb_entryCreatedDate, cb_postType);
loadOptUnderPost();
GetHistoryToday(cb_blogId, cb_blogApp, cb_entryCreatedDate);
</script>
</div>    </div>
</div>