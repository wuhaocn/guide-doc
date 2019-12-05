<div id="main">
<div id="post_detail">
<div class="post">
<h2>

<a id="cb_post_title_url" class="postTitle2" href="https://www.cnblogs.com/bendsha/p/cdc_bend.html">Oracle CDC简介及异步在线日志CDC部署示例</a>

</h2>
<div class="postText">
<div id="cnblogs_post_body" class="blogpost-body ">
<h2>摘要</h2>
<div>最近由于工作需要，花时间研究了一下Oracle CDC功能和LogMiner工具，希望能找到一种稳定、高效的技术来实现Oracle增量数据抽取功能。以下是个人的部分学习总结和部署实践。</div>
<h2>1. Oracle CDC 简介</h2>
<div>很多人都认为，只要是涉及到数据库数据复制和增量数据抽取，都是需要购买收费软件的。实际上，我们通过Oracle提供的CDC和LogMiner等免费工具也能实现数据库数据复制和增量数据抽取，各种数据复制软件只是使得获取增量数据更加便捷，或者是可以支持更多的扩展功能（例如：异构数据库之间的同步，ETL过程的数据清洗、装换），但实际Oracle本身是支持CDC机制，只是很少有人关注，操作起来也有些复杂，而且据传言并不稳定，常常见到论坛上爆出一些莫名其妙的问题。</div>
<div>Oracle11gR2提供给我们以下几种CDC机制：</div>
<h3>1.1&nbsp;Synchronous Change Data Capture Configuration（同步复制）</h3>
<p><img src="https://images2015.cnblogs.com/blog/1068207/201705/1068207-20170526192306372-1819930635.png" alt=""></p>
<div><img src="file:///C:/Users/BendSha/Documents/My%20Knowledge/temp/feb77305-403f-482b-80a6-bc078bad3cbf/4/index_files/eac4ce44-05e3-40f7-8e59-ed56b06804a7.png" alt="" border="0">
<div>&nbsp;原理很简单，原表、目标表必须是同一个库，采用触发器的机制（设置同步CDC后，并看不到触发器，但实际运行机理还是触发器的机制）将原表内容复制到另一个目标表。这个机制就不多说了，和自己给表建触发器没什么太大差别。</div>
</div>
<div>
<h3>1.2&nbsp;Asynchronous HotLog Configuration（异步在线日志CDC）</h3>
<p><img src="https://images2015.cnblogs.com/blog/1068207/201705/1068207-20170526192327279-376093983.png" alt=""></p>
<div><img src="file:///C:/Users/BendSha/Documents/My%20Knowledge/temp/feb77305-403f-482b-80a6-bc078bad3cbf/4/index_files/339b0a3b-0c69-442d-a213-36e4c181ca29.png" alt="" border="0">
<div>这个过程已经没有触发器了，而是使用Redo Log，但是使用在线日志，并不是归档日志。并且原表、目标表仍然必须是同一个库。这种模式是相对简单的，同时这种模式是在Oracle 10以上才产生的，9i是没有这个机制的。</div>
</div>
</div>
<div>
<h3>1.3&nbsp;Asynchronous Distributed HotLog Configuration（异步分布式CDC）</h3>
<p><img src="https://images2015.cnblogs.com/blog/1068207/201705/1068207-20170526192342669-1522054385.png" alt=""></p>
<div><img src="file:///C:/Users/BendSha/Documents/My%20Knowledge/temp/feb77305-403f-482b-80a6-bc078bad3cbf/4/index_files/01fb2fa6-9a1f-4e2a-9d68-43395956f42c.png" alt="" border="0">
<div>&nbsp;实际这个模式是对异步在线日志CDC的一种优化，也比较容易理解，就是加入了DB-LINK机制，使原表、目标表不在同一个数据库。实际是和异步在线日志CDC没有什么本质区别。</div>
</div>
</div>
<div>
<h3>1.4&nbsp;Asynchronous Autolog Online Change Data Capture Configuration（异步在线日志复制CDC）</h3>
<p><img src="https://images2015.cnblogs.com/blog/1068207/201705/1068207-20170526192358513-964979174.png" alt=""></p>
<div><img src="file:///C:/Users/BendSha/Documents/My%20Knowledge/temp/feb77305-403f-482b-80a6-bc078bad3cbf/4/index_files/9479f8d3-128a-4ba0-afc8-eb5976914400.png" alt="" border="0">
<div>异步在线日志复制CDC模式就要高级很多了，使用Standby Redo Log（热备数据库日志），实际就是使用Oracle的热备机制，将日志写入了热备数据库，目标表就可以建立在热备库上，这对主数据库性能影响就进一步降低。</div>
</div>
<div>
<h3>1.5&nbsp;Asynchronous AutoLog Archive Change Data Capture Configuration（归档日志CDC）</h3>
<p><img src="https://images2015.cnblogs.com/blog/1068207/201705/1068207-20170526192417247-198841881.png" alt=""></p>
<div><img src="file:///C:/Users/BendSha/Documents/My%20Knowledge/temp/feb77305-403f-482b-80a6-bc078bad3cbf/4/index_files/5fd8cc4d-debf-4c3c-97cf-1ed2ee6d8080.png" alt="" border="0"></div>
<div>
<div>归档日志CDC模式是最完美的模式，但是需要有机制可以获取归档日志（并行文件系统技术），然后在目标端分析归档日志进行变化数据处理，这种模式理论上来讲，几乎可以完全不影响原数据库的性能。</div>
<div>坦白来说，我对Oracle理解并不深，只是为了解决特定的几个问题多看了一点，在现实工作中遇到类似问题需要解决的，或对技术痴狂的同学可以研究一下，我贴上了4种模式具体的设置步骤，虽然是英文的，但是还是非常明确的。（我比较推荐使用第二种，因为设置比较简单，性能上也属于中规中矩，如果没有什么特别要求，可以采用异步在线日志CDC。</div>
<div>以下是我对异步在线日志CDC环境的部署测试。</div>
</div>
</div>
</div>
<div>
<h2>2. 异步在线日志CDC环境部署</h2>
</div>
<h3>2.1 环境配置准备</h3>
<div>（1）确认数据库版本</div>
<div>
<div><ol class="linenums">
<li class="L0"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;<span class="pln"> select <span class="pun">*<span class="pln"> from v$version<span class="pun">;</span></span></span></span></span></span></code></li>
<li class="L1"><code class="language-sh"></code></li>
<li class="L2"><code class="language-sh"><span class="pln">BANNER</span></code></li>
<li class="L3"><code class="language-sh"><span class="pun">--------------------------------------------------------------------------------</span></code></li>
<li class="L4"><code class="language-sh"><span class="typ">Oracle<span class="pln"> <span class="typ">Database<span class="pln"> <span class="lit">11g<span class="pln"> <span class="typ">Enterprise<span class="pln"> <span class="typ">Edition<span class="pln"> <span class="typ">Release<span class="pln"> <span class="lit">11.2<span class="pun">.<span class="lit">0.1<span class="pun">.<span class="lit">0<span class="pln"> <span class="pun">-<span class="pln"> <span class="lit">64bit<span class="pln"> <span class="typ">Production</span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></code></li>
<li class="L5"><code class="language-sh"><span class="pln">PL<span class="pun">/<span class="pln">SQL <span class="typ">Release<span class="pln"> <span class="lit">11.2<span class="pun">.<span class="lit">0.1<span class="pun">.<span class="lit">0<span class="pln"> <span class="pun">-<span class="pln"> <span class="typ">Production</span></span></span></span></span></span></span></span></span></span></span></span></span></span></code></li>
<li class="L6"><code class="language-sh"><span class="pln">CORE <span class="lit">11.2<span class="pun">.<span class="lit">0.1<span class="pun">.<span class="lit">0<span class="pln"> <span class="typ">Production</span></span></span></span></span></span></span></span></code></li>
<li class="L7"><code class="language-sh"><span class="pln">TNS <span class="kwd">for<span class="pln"> <span class="typ">Linux<span class="pun">:<span class="pln"> <span class="typ">Version<span class="pln"> <span class="lit">11.2<span class="pun">.<span class="lit">0.1<span class="pun">.<span class="lit">0<span class="pln"> <span class="pun">-<span class="pln"> <span class="typ">Production</span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></code></li>
<li class="L8"><code class="language-sh"><span class="pln">NLSRTL <span class="typ">Version<span class="pln"> <span class="lit">11.2<span class="pun">.<span class="lit">0.1<span class="pun">.<span class="lit">0<span class="pln"> <span class="pun">-<span class="pln"> <span class="typ">Production</span></span></span></span></span></span></span></span></span></span></span></span></code></li>
</ol></div>
<div>（2）配置数据库参数</div>
</div>
<div>
<div><ol class="linenums">
<li class="L0"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;<span class="pln"> alter system <span class="kwd">set<span class="pln"> streams_pool_size<span class="pun">=<span class="lit">50m<span class="pun">;</span></span></span></span></span></span></span></span></code></li>
<li class="L1"><code class="language-sh"></code></li>
<li class="L2"><code class="language-sh"><span class="typ">System<span class="pln"> altered<span class="pun">.</span></span></span></code></li>
<li class="L3"><code class="language-sh"></code></li>
<li class="L4"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;<span class="pln"> alter system <span class="kwd">set<span class="pln"> java_pool_size<span class="pun">=<span class="lit">50m<span class="pun">;</span></span></span></span></span></span></span></span></code></li>
<li class="L5"><code class="language-sh"></code></li>
<li class="L6"><code class="language-sh"><span class="typ">System<span class="pln"> altered<span class="pun">.</span></span></span></code></li>
<li class="L7"><code class="language-sh"></code></li>
<li class="L8"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;<span class="pln"> alter system <span class="kwd">set<span class="pln"> undo_retention<span class="pun">=<span class="lit">3600<span class="pun">;</span></span></span></span></span></span></span></span></code></li>
<li class="L9"><code class="language-sh"></code></li>
<li class="L0"><code class="language-sh"><span class="typ">System<span class="pln"> altered<span class="pun">.</span></span></span></code></li>
<li class="L1"><code class="language-sh"></code></li>
<li class="L2"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;<span class="pln"> show parameter streams_pool</span></span></span></code></li>
<li class="L3"><code class="language-sh"></code></li>
<li class="L4"><code class="language-sh"><span class="pln">NAME TYPE VALUE</span></code></li>
<li class="L5"><code class="language-sh"><span class="pun">------------------------------------<span class="pln"> <span class="pun">-----------<span class="pln"> <span class="pun">------------------------------</span></span></span></span></span></code></li>
<li class="L6"><code class="language-sh"><span class="pln">streams_pool_size big integer <span class="lit">52M</span></span></code></li>
<li class="L7"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;<span class="pln"> show parameter java_pool</span></span></span></code></li>
<li class="L8"><code class="language-sh"></code></li>
<li class="L9"><code class="language-sh"><span class="pln">NAME TYPE VALUE</span></code></li>
<li class="L0"><code class="language-sh"><span class="pun">------------------------------------<span class="pln"> <span class="pun">-----------<span class="pln"> <span class="pun">------------------------------</span></span></span></span></span></code></li>
<li class="L1"><code class="language-sh"><span class="pln">java_pool_size big integer <span class="lit">52M</span></span></code></li>
<li class="L2"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;<span class="pln"> show parameter undo_re</span></span></span></code></li>
<li class="L3"><code class="language-sh"></code></li>
<li class="L4"><code class="language-sh"><span class="pln">NAME TYPE VALUE</span></code></li>
<li class="L5"><code class="language-sh"><span class="pun">------------------------------------<span class="pln"> <span class="pun">-----------<span class="pln"> <span class="pun">------------------------------</span></span></span></span></span></code></li>
<li class="L6"><code class="language-sh"><span class="pln">undo_retention integer <span class="lit">3600</span></span></code></li>
</ol></div>
<div>（3）开启归档及补充日志</div>
</div>
<div>
<div><ol class="linenums">
<li class="L0"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;<span class="pln"> archive log list</span></span></span></code></li>
<li class="L1"><code class="language-sh"><span class="typ">Database<span class="pln"> log mode <span class="typ">Archive<span class="pln"> <span class="typ">Mode</span></span></span></span></span></code></li>
<li class="L2"><code class="language-sh"><span class="typ">Automatic<span class="pln"> archival <span class="typ">Enabled</span></span></span></code></li>
<li class="L3"><code class="language-sh"><span class="typ">Archive<span class="pln"> destination USE_DB_RECOVERY_FILE_DEST</span></span></code></li>
<li class="L4"><code class="language-sh"><span class="typ">Oldest<span class="pln"> online log sequence <span class="lit">487</span></span></span></code></li>
<li class="L5"><code class="language-sh"><span class="typ">Next<span class="pln"> log sequence to archive <span class="lit">489</span></span></span></code></li>
<li class="L6"><code class="language-sh"><span class="typ">Current<span class="pln"> log sequence <span class="lit">489</span></span></span></code></li>
<li class="L7"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;<span class="pln"> alter database force logging<span class="pun">;</span></span></span></span></code></li>
<li class="L8"><code class="language-sh"></code></li>
<li class="L9"><code class="language-sh"><span class="typ">Database<span class="pln"> altered<span class="pun">.</span></span></span></code></li>
<li class="L0"><code class="language-sh"></code></li>
<li class="L1"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;<span class="pln"> alter database add supplemental log data<span class="pun">;</span></span></span></span></code></li>
<li class="L2"><code class="language-sh"></code></li>
<li class="L3"><code class="language-sh"><span class="typ">Database<span class="pln"> altered<span class="pun">.</span></span></span></code></li>
<li class="L4"><code class="language-sh"></code></li>
<li class="L5"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;<span class="pln"> select LOG_MODE<span class="pun">,<span class="pln">FORCE_LOGGING<span class="pun">,<span class="pln">SUPPLEMENTAL_LOG_DATA_MIN from v$database<span class="pun">;</span></span></span></span></span></span></span></span></code></li>
<li class="L6"><code class="language-sh"></code></li>
<li class="L7"><code class="language-sh"><span class="pln">LOG_MODE FOR SUPPLEME</span></code></li>
<li class="L8"><code class="language-sh"><span class="pun">------------<span class="pln"> <span class="pun">---<span class="pln"> <span class="pun">--------</span></span></span></span></span></code></li>
<li class="L9"><code class="language-sh"><span class="pln">ARCHIVELOG YES YES</span></code></li>
</ol></div>
<div>（4）准备测试表employee_info</div>
</div>
<div>
<div><ol class="linenums">
<li class="L0"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;<span class="pln"> create table employee_info<span class="pun">(<span class="pln">n number<span class="pun">,<span class="pln">name varchar<span class="pun">(<span class="lit">20<span class="pun">),<span class="pln">address varchar<span class="pun">(<span class="lit">150<span class="pun">),<span class="pln">department varchar<span class="pun">(<span class="lit">120<span class="pun">),<span class="pln">organization varchar<span class="pun">(<span class="lit">150<span class="pun">))<span class="pln"> tablespace datafile1<span class="pun">;</span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></code></li>
<li class="L1"><code class="language-sh"></code></li>
<li class="L2"><code class="language-sh"><span class="typ">Table<span class="pln"> created<span class="pun">.</span></span></span></code></li>
<li class="L3"><code class="language-sh"></code></li>
<li class="L4"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;<span class="pln"> insert into employee_info values<span class="pun">(<span class="lit">1<span class="pun">,<span class="pln"> <span class="str">'bendsha'<span class="pun">,<span class="pln"> <span class="str">'lianhang road, shanghai, China'<span class="pun">,<span class="pln"> <span class="str">'AnyBackup'<span class="pun">,<span class="pln"> <span class="str">'EISOO'<span class="pun">);</span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></code></li>
<li class="L5"><code class="language-sh"></code></li>
<li class="L6"><code class="language-sh"><span class="lit">1<span class="pln"> row created<span class="pun">.</span></span></span></code></li>
<li class="L7"><code class="language-sh"></code></li>
<li class="L8"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;<span class="pln"> insert into employee_info values<span class="pun">(<span class="lit">2<span class="pun">,<span class="pln"> <span class="str">'bendsha'<span class="pun">,<span class="pln"> <span class="str">'lianhang road, shanghai, China'<span class="pun">,<span class="pln"> <span class="str">'AnyBackup'<span class="pun">,<span class="pln"> <span class="str">'EISOO'<span class="pun">);</span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></code></li>
<li class="L9"><code class="language-sh"></code></li>
<li class="L0"><code class="language-sh"><span class="lit">1<span class="pln"> row created<span class="pun">.</span></span></span></code></li>
</ol></div>
<div>
<h3>2.2 创建发布者和订阅者</h3>
<div>（1）创建发布者并授权</div>
</div>
</div>
<div>
<div><ol class="linenums">
<li class="L0"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;<span class="pln"> create tablespace cdc_datafile datafile <span class="str">'/u01/app/oracle/orcl/cdc_datafile.dbf'<span class="pln"> size <span class="lit">1G<span class="pun">;</span></span></span></span></span></span></span></code></li>
<li class="L1"><code class="language-sh"></code></li>
<li class="L2"><code class="language-sh"><span class="typ">Tablespace<span class="pln"> created<span class="pun">.</span></span></span></code></li>
<li class="L3"><code class="language-sh"></code></li>
<li class="L4"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;<span class="pln"> create user cdc_publisher identified by cdc_publisher default tablespace cdc_datafile temporary tablespace temp<span class="pun">;</span></span></span></span></code></li>
<li class="L5"><code class="language-sh"></code></li>
<li class="L6"><code class="language-sh"><span class="typ">User<span class="pln"> created<span class="pun">.</span></span></span></code></li>
<li class="L7"><code class="language-sh"></code></li>
<li class="L8"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;<span class="pln"> grant create session TO cdc_publisher<span class="pun">;</span></span></span></span></code></li>
<li class="L9"><code class="language-sh"></code></li>
<li class="L0"><code class="language-sh"><span class="typ">Grant<span class="pln"> succeeded<span class="pun">.</span></span></span></code></li>
<li class="L1"><code class="language-sh"></code></li>
<li class="L2"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;<span class="pln"> grant create table TO cdc_publisher<span class="pun">;</span></span></span></span></code></li>
<li class="L3"><code class="language-sh"></code></li>
<li class="L4"><code class="language-sh"><span class="typ">Grant<span class="pln"> succeeded<span class="pun">.</span></span></span></code></li>
<li class="L5"><code class="language-sh"></code></li>
<li class="L6"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;<span class="pln"> grant create sequence TO cdc_publisher<span class="pun">;</span></span></span></span></code></li>
<li class="L7"><code class="language-sh"></code></li>
<li class="L8"><code class="language-sh"><span class="typ">Grant<span class="pln"> succeeded<span class="pun">.</span></span></span></code></li>
<li class="L9"><code class="language-sh"></code></li>
<li class="L0"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;<span class="pln"> grant create procedure TO cdc_publisher<span class="pun">;</span></span></span></span></code></li>
<li class="L1"><code class="language-sh"></code></li>
<li class="L2"><code class="language-sh"><span class="typ">Grant<span class="pln"> succeeded<span class="pun">.</span></span></span></code></li>
<li class="L3"><code class="language-sh"></code></li>
<li class="L4"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;<span class="pln"> grant create any job TO cdc_publisher<span class="pun">;</span></span></span></span></code></li>
<li class="L5"><code class="language-sh"></code></li>
<li class="L6"><code class="language-sh"><span class="typ">Grant<span class="pln"> succeeded<span class="pun">.</span></span></span></code></li>
<li class="L7"><code class="language-sh"></code></li>
<li class="L8"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;<span class="pln"> grant execute_catalog_role TO cdc_publisher<span class="pun">;</span></span></span></span></code></li>
<li class="L9"><code class="language-sh"></code></li>
<li class="L0"><code class="language-sh"><span class="typ">Grant<span class="pln"> succeeded<span class="pun">.</span></span></span></code></li>
<li class="L1"><code class="language-sh"></code></li>
<li class="L2"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;<span class="pln"> grant select_catalog_role TO cdc_publisher<span class="pun">;</span></span></span></span></code></li>
<li class="L3"><code class="language-sh"></code></li>
<li class="L4"><code class="language-sh"><span class="typ">Grant<span class="pln"> succeeded<span class="pun">.</span></span></span></code></li>
<li class="L5"><code class="language-sh"></code></li>
<li class="L6"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;<span class="pln"> grant execute ON dbms_cdc_publish TO cdc_publisher<span class="pun">;</span></span></span></span></code></li>
<li class="L7"><code class="language-sh"></code></li>
<li class="L8"><code class="language-sh"><span class="typ">Grant<span class="pln"> succeeded<span class="pun">.</span></span></span></code></li>
<li class="L9"><code class="language-sh"></code></li>
<li class="L0"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;<span class="pln"> grant execute ON dbms_lock TO cdc_publisher<span class="pun">;</span></span></span></span></code></li>
<li class="L1"><code class="language-sh"></code></li>
<li class="L2"><code class="language-sh"><span class="typ">Grant<span class="pln"> succeeded<span class="pun">.</span></span></span></code></li>
<li class="L3"><code class="language-sh"></code></li>
<li class="L4"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;<span class="pln"> grant unlimited tablespace TO cdc_publisher<span class="pun">;</span></span></span></span></code></li>
<li class="L5"><code class="language-sh"></code></li>
<li class="L6"><code class="language-sh"><span class="typ">Grant<span class="pln"> succeeded<span class="pun">.</span></span></span></code></li>
<li class="L7"><code class="language-sh"></code></li>
<li class="L8"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;<span class="pln"> execute dbms_streams_auth<span class="pun">.<span class="pln">grant_admin_privilege<span class="pun">(<span class="str">'CDC_PUBLISHER'<span class="pun">);</span></span></span></span></span></span></span></span></code></li>
<li class="L9"><code class="language-sh"></code></li>
<li class="L0"><code class="language-sh"><span class="pln">PL<span class="pun">/<span class="pln">SQL procedure successfully completed<span class="pun">.</span></span></span></span></code></li>
<li class="L1"><code class="language-sh"></code></li>
<li class="L2"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;<span class="pln"> grant all on backupuser<span class="pun">.<span class="pln">employee_info to cdc_publisher<span class="pun">;</span></span></span></span></span></span></code></li>
<li class="L3"><code class="language-sh"></code></li>
<li class="L4"><code class="language-sh"><span class="typ">Grant<span class="pln"> succeeded<span class="pun">.</span></span></span></code></li>
</ol></div>
</div>
<div>（2）创建订阅者并授权</div>
<div>
<div><ol class="linenums">
<li class="L0"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;<span class="pln"> create user cdc_subscriber identified by cdc_subscriber default tablespace cdc_datafile temporary tablespace temp<span class="pun">;</span></span></span></span></code></li>
<li class="L1"><code class="language-sh"></code></li>
<li class="L2"><code class="language-sh"><span class="typ">User<span class="pln"> created<span class="pun">.</span></span></span></code></li>
<li class="L3"><code class="language-sh"></code></li>
<li class="L4"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;<span class="pln"> grant create session TO cdc_subscriber<span class="pun">;</span></span></span></span></code></li>
<li class="L5"><code class="language-sh"></code></li>
<li class="L6"><code class="language-sh"><span class="typ">Grant<span class="pln"> succeeded<span class="pun">.</span></span></span></code></li>
</ol></div>
<h3>2.3&nbsp;发布/订阅具体数据</h3>
<div>（1）发布：准备源表(Source Table)</div>
<div>
<div><ol class="linenums">
<li class="L0"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;</span></span></code></li>
<li class="L1"><code class="language-sh"><span class="pln">conn cdc_publisher<span class="pun">/<span class="pln">cdc_publisher</span></span></span></code></li>
<li class="L2"><code class="language-sh"><span class="pln">BEGIN</span></code></li>
<li class="L3"><code class="language-sh"><span class="pln">DBMS_CAPTURE_ADM<span class="pun">.<span class="pln">PREPARE_TABLE_INSTANTIATION<span class="pun">(<span class="pln">TABLE_NAME <span class="pun">=&gt;<span class="pln"> <span class="str">'backupuser.employee_info'<span class="pun">);</span></span></span></span></span></span></span></span></span></code></li>
<li class="L4"><code class="language-sh"><span class="pln">END<span class="pun">;</span></span></code></li>
<li class="L5"><code class="language-sh"><span class="typ">Connected<span class="pun">.</span></span></code></li>
<li class="L6"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;<span class="pln"> <span class="lit">2<span class="pln"> <span class="lit">3<span class="pln"> <span class="lit">4<span class="pln"> <span class="pun">/</span></span></span></span></span></span></span></span></span></span></code></li>
<li class="L7"><code class="language-sh"></code></li>
<li class="L8"><code class="language-sh"><span class="pln">PL<span class="pun">/<span class="pln">SQL procedure successfully completed<span class="pun">.</span></span></span></span></code></li>
</ol></div>
</div>
<div>
<div>（2）发布：创建变更集（Data Set）</div>
</div>
<div>
<div><ol class="linenums">
<li class="L0"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;</span></span></code></li>
<li class="L1"><code class="language-sh"><span class="pln">conn cdc_publisher<span class="pun">/<span class="pln">cdc_publisher</span></span></span></code></li>
<li class="L2"><code class="language-sh"><span class="pln">BEGIN</span></code></li>
<li class="L3"><code class="language-sh"><span class="pln">DBMS_CDC_PUBLISH<span class="pun">.<span class="pln">CREATE_CHANGE_SET<span class="pun">(</span></span></span></span></code></li>
<li class="L4"><code class="language-sh"><span class="pln">change_set_name <span class="pun">=&gt;<span class="pln"> <span class="str">'cdc_employee_info_cs'<span class="pun">,</span></span></span></span></span></code></li>
<li class="L5"><code class="language-sh"><span class="pln">description <span class="pun">=&gt;<span class="pln"> <span class="str">'Change set for backupuser.employee_info info'<span class="pun">,</span></span></span></span></span></code></li>
<li class="L6"><code class="language-sh"><span class="pln">change_source_name <span class="pun">=&gt;<span class="pln"> <span class="str">'HOTLOG_SOURCE'<span class="pun">,</span></span></span></span></span></code></li>
<li class="L7"><code class="language-sh"><span class="pln">stop_on_ddl <span class="pun">=&gt;<span class="pln"> <span class="str">'y'</span></span></span></span></code></li>
<li class="L8"><code class="language-sh"><span class="pun">);</span></code></li>
<li class="L9"><code class="language-sh"><span class="pln">END<span class="pun">;</span></span></code></li>
<li class="L0"><code class="language-sh"><span class="typ">Connected<span class="pun">.</span></span></code></li>
<li class="L1"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;<span class="pln"> <span class="lit">2<span class="pln"> <span class="lit">3<span class="pln"> <span class="lit">4<span class="pln"> <span class="lit">5<span class="pln"> <span class="lit">6<span class="pln"> <span class="lit">7<span class="pln"> <span class="lit">8<span class="pln"> <span class="lit">9<span class="pln"> <span class="pun">/</span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></code></li>
<li class="L2"><code class="language-sh"></code></li>
<li class="L3"><code class="language-sh"><span class="pln">PL<span class="pun">/<span class="pln">SQL procedure successfully completed<span class="pun">.</span></span></span></span></code></li>
</ol></div>
</div>
<div>
<div>（3）发布：创建变更表（Change Table）</div>
</div>
<div><ol class="linenums">
<li class="L0"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;</span></span></code></li>
<li class="L1"><code class="language-sh"><span class="pln">conn cdc_publisher<span class="pun">/<span class="pln">cdc_publisher</span></span></span></code></li>
<li class="L2"><code class="language-sh"><span class="pln">BEGIN</span></code></li>
<li class="L3"><code class="language-sh"><span class="pln">DBMS_CDC_PUBLISH<span class="pun">.<span class="pln">CREATE_CHANGE_TABLE<span class="pun">(</span></span></span></span></code></li>
<li class="L4"><code class="language-sh"><span class="pln">owner <span class="pun">=&gt;<span class="pln"> <span class="str">'cdc_publisher'<span class="pun">,</span></span></span></span></span></code></li>
<li class="L5"><code class="language-sh"><span class="pln">change_table_name <span class="pun">=&gt;<span class="pln"> <span class="str">'employee_info_ct'<span class="pun">,</span></span></span></span></span></code></li>
<li class="L6"><code class="language-sh"><span class="pln">change_set_name <span class="pun">=&gt;<span class="pln"> <span class="str">'cdc_employee_info_cs'<span class="pun">,</span></span></span></span></span></code></li>
<li class="L7"><code class="language-sh"><span class="pln">source_schema <span class="pun">=&gt;<span class="pln"> <span class="str">'backupuser'<span class="pun">,</span></span></span></span></span></code></li>
<li class="L8"><code class="language-sh"><span class="pln">source_table <span class="pun">=&gt;<span class="pln"> <span class="str">'employee_info'<span class="pun">,</span></span></span></span></span></code></li>
<li class="L9"><code class="language-sh"><span class="pln">column_type_list <span class="pun">=&gt;<span class="str">'n number,name varchar(20),address varchar(150)'<span class="pun">,</span></span></span></span></code></li>
<li class="L0"><code class="language-sh"><span class="pln">capture_values <span class="pun">=&gt;<span class="pln"> <span class="str">'both'<span class="pun">,</span></span></span></span></span></code></li>
<li class="L1"><code class="language-sh"><span class="pln">rs_id <span class="pun">=&gt;<span class="pln"> <span class="str">'y'<span class="pun">,</span></span></span></span></span></code></li>
<li class="L3"><code class="language-sh"><span class="pln">row_id <span class="pun">=&gt;<span class="pln"> <span class="str">'n'<span class="pun">,</span></span></span></span></span></code></li>
<li class="L4"><code class="language-sh"><span class="pln">user_id <span class="pun">=&gt;<span class="pln"> <span class="str">'n'<span class="pun">,</span></span></span></span></span></code></li>
<li class="L5"><code class="language-sh"><span class="pln">timestamp <span class="pun">=&gt;<span class="pln"> <span class="str">'n'<span class="pun">,</span></span></span></span></span></code></li>
<li class="L6"><code class="language-sh"><span class="pln">object_id <span class="pun">=&gt;<span class="pln"> <span class="str">'n'<span class="pun">,</span></span></span></span></span></code></li>
<li class="L7"><code class="language-sh"><span class="pln">source_colmap <span class="pun">=&gt;<span class="pln"> <span class="str">'n'<span class="pun">,</span></span></span></span></span></code></li>
<li class="L8"><code class="language-sh"><span class="pln">target_colmap <span class="pun">=&gt;<span class="pln"> <span class="str">'y'<span class="pun">,</span></span></span></span></span></code></li>
<li class="L9"><code class="language-sh"><span class="pln">options_string <span class="pun">=&gt;<span class="str">''<span class="pun">);</span></span></span></span></code></li>
<li class="L0"><code class="language-sh"><span class="pln">END<span class="pun">;</span></span></code></li>
<li class="L1"><code class="language-sh"><span class="pln"> <span class="lit">19<span class="pln"> <span class="pun">/</span></span></span></span></code></li>
<li class="L2"><code class="language-sh"></code></li>
<li class="L3"><code class="language-sh"><span class="pln">PL<span class="pun">/<span class="pln">SQL procedure successfully completed<span class="pun">.</span></span></span></span></code></li>
</ol></div>
<div>
<div>（4）发布：激活变更集（Data Set）</div>
</div>
<div>
<div><ol class="linenums">
<li class="L0"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;</span></span></code></li>
<li class="L1"><code class="language-sh"><span class="pln">conn cdc_publisher<span class="pun">/<span class="pln">cdc_publisher</span></span></span></code></li>
<li class="L2"><code class="language-sh"><span class="pln">BEGIN</span></code></li>
<li class="L3"><code class="language-sh"><span class="pln">DBMS_CDC_PUBLISH<span class="pun">.<span class="pln">ALTER_CHANGE_SET<span class="pun">(</span></span></span></span></code></li>
<li class="L4"><code class="language-sh"><span class="pln">change_set_name <span class="pun">=&gt;<span class="pln"> <span class="str">'cdc_employee_info_cs'<span class="pun">,</span></span></span></span></span></code></li>
<li class="L5"><code class="language-sh"><span class="pln">enable_capture <span class="pun">=&gt;<span class="pln"> <span class="str">'y'<span class="pun">);</span></span></span></span></span></code></li>
<li class="L6"><code class="language-sh"><span class="pln">END<span class="pun">;</span></span></code></li>
<li class="L7"><code class="language-sh"><span class="typ">Connected<span class="pun">.</span></span></code></li>
<li class="L8"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;<span class="pln"> <span class="lit">2<span class="pln"> <span class="lit">3<span class="pln"> <span class="lit">4<span class="pln"> <span class="lit">5<span class="pln"> <span class="lit">6<span class="pln"> <span class="pun">/</span></span></span></span></span></span></span></span></span></span></span></span></span></span></code></li>
<li class="L9"><code class="language-sh"></code></li>
<li class="L0"><code class="language-sh"><span class="pln">PL<span class="pun">/<span class="pln">SQL procedure successfully completed<span class="pun">.</span></span></span></span></code></li>
</ol></div>
</div>
<div>（5）授权给订阅者</div>
<div>
<div><ol class="linenums">
<li class="L0"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;</span></span></code></li>
<li class="L1"><code class="language-sh"><span class="pln">conn cdc_publisher<span class="pun">/<span class="pln">cdc_publisher</span></span></span></code></li>
<li class="L2"><code class="language-sh"><span class="pln">GRANT SELECT ON cdc_publisher<span class="pun">.<span class="pln">employee_info_ct TO cdc_subscriber<span class="pun">;</span></span></span></span></code></li>
<li class="L3"><code class="language-sh"><span class="pln">conn <span class="pun">/<span class="pln"> as sysdba</span></span></span></code></li>
<li class="L4"><code class="language-sh"><span class="pln">GRANT CREATE TABLE TO cdc_subscriber<span class="pun">;</span></span></code></li>
<li class="L5"><code class="language-sh"><span class="pln">GRANT CREATE SESSION TO cdc_subscriber<span class="pun">;</span></span></code></li>
<li class="L6"><code class="language-sh"><span class="pln">GRANT CREATE VIEW TO cdc_subscriber<span class="pun">;</span></span></code></li>
<li class="L7"><span class="pln">GRANT UNLIMITED TABLESPACE TO cdc_subscriber<span class="pun">;<br></span></span></li>
<li class="L2"><code class="language-sh"></code></li>
<li class="L3"><code class="language-sh"><span class="typ">Grant<span class="pln"> succeeded<span class="pun">.</span></span></span></code></li>







</ol></div>
<div>（6）订阅：创建订阅集</div>







</div>
<div>
<div><ol class="linenums">
<li class="L0"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;</span></span></code></li>
<li class="L1"><code class="language-sh"><span class="pln">conn cdc_subscriber<span class="pun">/<span class="pln">cdc_subscriber</span></span></span></code></li>
<li class="L2"><code class="language-sh"><span class="pln">BEGIN</span></code></li>
<li class="L3"><code class="language-sh"><span class="pln">DBMS_CDC_SUBSCRIBE<span class="pun">.<span class="pln">CREATE_SUBSCRIPTION<span class="pun">(</span></span></span></span></code></li>
<li class="L4"><code class="language-sh"><span class="pln">change_set_name <span class="pun">=&gt;<span class="pln"> <span class="str">'cdc_employee_info_cs'<span class="pun">,</span></span></span></span></span></code></li>
<li class="L5"><code class="language-sh"><span class="pln">description <span class="pun">=&gt;<span class="pln"> <span class="str">'Change data for employee_info'<span class="pun">,</span></span></span></span></span></code></li>
<li class="L6"><code class="language-sh"><span class="pln">subscription_name <span class="pun">=&gt;<span class="pln"> <span class="str">'employee_info_sub'<span class="pun">);</span></span></span></span></span></code></li>
<li class="L7"><code class="language-sh"><span class="pln">END<span class="pun">;</span></span></code></li>
<li class="L8"><code class="language-sh"><span class="typ">Connected<span class="pun">.</span></span></code></li>
<li class="L9"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;<span class="pln">   <span class="lit">2<span class="pln">    <span class="lit">3<span class="pln">    <span class="lit">4<span class="pln">    <span class="lit">5<span class="pln">    <span class="lit">6<span class="pln">    <span class="lit">7<span class="pln">  <span class="pun">/</span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></code></li>
<li class="L0"><code class="language-sh"></code></li>
<li class="L1"><code class="language-sh"><span class="pln">PL<span class="pun">/<span class="pln">SQL procedure successfully completed<span class="pun">.</span></span></span></span></code></li>







</ol></div>







</div>
<div>
<div>（7）订阅：开始订阅表信息</div>







</div>
<div>
<div><ol class="linenums">
<li class="L0"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;</span></span></code></li>
<li class="L1"><code class="language-sh"><span class="pln">conn cdc_subscriber<span class="pun">/<span class="pln">cdc_subscriber</span></span></span></code></li>
<li class="L2"><code class="language-sh"><span class="pln">BEGIN</span></code></li>
<li class="L3"><code class="language-sh"><span class="pln">DBMS_CDC_SUBSCRIBE<span class="pun">.<span class="pln">SUBSCRIBE<span class="pun">(</span></span></span></span></code></li>
<li class="L4"><code class="language-sh"><span class="pln">subscription_name <span class="pun">=&gt;<span class="pln"> <span class="str">'employee_info_sub'<span class="pun">,</span></span></span></span></span></code></li>
<li class="L5"><code class="language-sh"><span class="pln">source_schema <span class="pun">=&gt;<span class="pln"> <span class="str">'backupuser'<span class="pun">,</span></span></span></span></span></code></li>
<li class="L6"><code class="language-sh"><span class="pln">source_table <span class="pun">=&gt;<span class="pln"> <span class="str">'employee_info'<span class="pun">,</span></span></span></span></span></code></li>
<li class="L7"><code class="language-sh"><span class="pln">column_list <span class="pun">=&gt;<span class="pln"> <span class="str">'n,name,address'<span class="pun">,</span></span></span></span></span></code></li>
<li class="L8"><code class="language-sh"><span class="pln">subscriber_view <span class="pun">=&gt;<span class="pln"> <span class="str">'employee_info_view'<span class="pun">);</span></span></span></span></span></code></li>
<li class="L9"><code class="language-sh"><span class="pln">END<span class="pun">;</span></span></code></li>
<li class="L0"><code class="language-sh"><span class="typ">Connected<span class="pun">.</span></span></code></li>
<li class="L1"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;<span class="pln">   <span class="lit">2<span class="pln">    <span class="lit">3<span class="pln">    <span class="lit">4<span class="pln">    <span class="lit">5<span class="pln">    <span class="lit">6<span class="pln">    <span class="lit">7<span class="pln">    <span class="lit">8<span class="pln">    <span class="lit">9<span class="pln">  <span class="pun">/</span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></code></li>
<li class="L2"><code class="language-sh"></code></li>
<li class="L3"><code class="language-sh"><span class="pln">PL<span class="pun">/<span class="pln">SQL procedure successfully completed<span class="pun">.</span></span></span></span></code></li>







</ol></div>







</div>
<div>
<div>（8）订阅：激活订阅</div>







</div>
<div>
<div><ol class="linenums">
<li class="L0"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;</span></span></code></li>
<li class="L1"><code class="language-sh"><span class="pln">conn cdc_subscriber<span class="pun">/<span class="pln">cdc_subscriber</span></span></span></code></li>
<li class="L2"><code class="language-sh"><span class="pln">BEGIN</span></code></li>
<li class="L3"><code class="language-sh"><span class="pln">DBMS_CDC_SUBSCRIBE<span class="pun">.<span class="pln">ACTIVATE_SUBSCRIPTION<span class="pun">(</span></span></span></span></code></li>
<li class="L4"><code class="language-sh"><span class="pln">subscription_name <span class="pun">=&gt;<span class="pln"> <span class="str">'employee_info_sub'<span class="pun">);</span></span></span></span></span></code></li>
<li class="L5"><code class="language-sh"><span class="pln">END<span class="pun">;</span></span></code></li>
<li class="L6"><code class="language-sh"><span class="typ">Connected<span class="pun">.</span></span></code></li>
<li class="L7"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;<span class="pln">   <span class="lit">2<span class="pln">    <span class="lit">3<span class="pln">    <span class="lit">4<span class="pln">    <span class="lit">5<span class="pln">  <span class="pun">/</span></span></span></span></span></span></span></span></span></span></span></span></code></li>
<li class="L8"><code class="language-sh"></code></li>
<li class="L9"><code class="language-sh"><span class="pln">PL<span class="pun">/<span class="pln">SQL procedure successfully completed<span class="pun">.</span></span></span></span></code></li>







</ol></div>







</div>
<div>
<div>（9）订阅：扩展订阅窗口</div>







</div>
<div>
<div><ol class="linenums">
<li class="L0"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;</span></span></code></li>
<li class="L1"><code class="language-sh"><span class="pln">conn cdc_subscriber<span class="pun">/<span class="pln">cdc_subscriber</span></span></span></code></li>
<li class="L2"><code class="language-sh"><span class="pln">BEGIN</span></code></li>
<li class="L3"><code class="language-sh"><span class="pln">DBMS_CDC_SUBSCRIBE<span class="pun">.<span class="pln">EXTEND_WINDOW<span class="pun">(</span></span></span></span></code></li>
<li class="L4"><code class="language-sh"><span class="pln">subscription_name <span class="pun">=&gt;<span class="pln"> <span class="str">'employee_info_sub'<span class="pun">);</span></span></span></span></span></code></li>
<li class="L5"><code class="language-sh"><span class="pln">END<span class="pun">;</span></span></code></li>
<li class="L6"><code class="language-sh"><span class="typ">Connected<span class="pun">.</span></span></code></li>
<li class="L7"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;<span class="pln">   <span class="lit">2<span class="pln">    <span class="lit">3<span class="pln">    <span class="lit">4<span class="pln">    <span class="lit">5<span class="pln">  <span class="pun">/</span></span></span></span></span></span></span></span></span></span></span></span></code></li>
<li class="L8"><code class="language-sh"></code></li>
<li class="L9"><code class="language-sh"><span class="pln">PL<span class="pun">/<span class="pln">SQL procedure successfully completed<span class="pun">.</span></span></span></span></code></li>







</ol></div>
<div>（10）订阅：查看订阅视图内容</div>







</div>
<div>
<div><ol class="linenums">
<li class="L0"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;</span></span></code></li>
<li class="L1"><code class="language-sh"><span class="pln">conn cdc_subscriber<span class="pun">/<span class="pln">cdc_subscriber</span></span></span></code></li>
<li class="L2"><code class="language-sh"><span class="typ">Connected<span class="pun">.</span></span></code></li>
<li class="L3"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;<span class="pln"> select <span class="pun">*<span class="pln"> from employee_info_view<span class="pun">;</span></span></span></span></span></span></code></li>
<li class="L4"><code class="language-sh"></code></li>
<li class="L5"><code class="language-sh"><span class="pln">no rows selected</span></code></li>







</ol></div>







</div>
<div>
<div>
<h3>2.4 测试发布/订阅</h3>
<div>（1）源表employee_info变更</div>







</div>







</div>
<div>
<div><ol class="linenums">
<li class="L0"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;</span></span></code></li>
<li class="L1"><code class="language-sh"><span class="pln">conn backupuser<span class="pun">/<span class="pln">backupuser123</span></span></span></code></li>
<li class="L2"><code class="language-sh"><span class="pln">insert into employee_info values<span class="pun">(<span class="lit">1<span class="pun">,<span class="pln"> <span class="str">'bendsha'<span class="pun">,<span class="pln"> <span class="str">'lianhang road, shanghai, China'<span class="pun">,<span class="pln"> <span class="str">'SmartData'<span class="pun">,<span class="pln"> <span class="str">'SmartDB'<span class="pun">);</span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></code></li>
<li class="L3"><code class="language-sh"><span class="pln">insert into employee_info values<span class="pun">(<span class="lit">2<span class="pun">,<span class="pln"> <span class="str">'bendsha'<span class="pun">,<span class="pln"> <span class="str">'lianhang road, shanghai, China'<span class="pun">,<span class="pln"> <span class="str">'SmartData'<span class="pun">,<span class="pln"> <span class="str">'SmartDB'<span class="pun">);</span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></code></li>
<li class="L4"><code class="language-sh"><span class="pln">insert into employee_info values<span class="pun">(<span class="lit">3<span class="pun">,<span class="pln"> <span class="str">'bendsha'<span class="pun">,<span class="pln"> <span class="str">'lianhang road, shanghai, China'<span class="pun">,<span class="pln"> <span class="str">'SmartData'<span class="pun">,<span class="pln"> <span class="str">'SmartDB'<span class="pun">);</span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></code></li>
<li class="L5"><code class="language-sh"><span class="pln">update employee_info <span class="kwd">set<span class="pln"> name <span class="pun">=<span class="pln"> <span class="str">'zhuzi'<span class="pln"> where n <span class="pun">=<span class="pln"> <span class="lit">2<span class="pun">;</span></span></span></span></span></span></span></span></span></span></span></code></li>
<li class="L6"><code class="language-sh"><span class="pln">delete from employee_info where n <span class="pun">=<span class="pln"> <span class="lit">1<span class="pun">;</span></span></span></span></span></code></li>
<li class="L7"><code class="language-sh"><span class="typ">Connected<span class="pun">.</span></span></code></li>
<li class="L8"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;</span></span></code></li>
<li class="L9"><code class="language-sh"><span class="lit">1<span class="pln"> row created<span class="pun">.</span></span></span></code></li>
<li class="L0"><code class="language-sh"></code></li>
<li class="L1"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;</span></span></code></li>
<li class="L2"><code class="language-sh"><span class="lit">1<span class="pln"> row created<span class="pun">.</span></span></span></code></li>
<li class="L3"><code class="language-sh"></code></li>
<li class="L4"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;</span></span></code></li>
<li class="L5"><code class="language-sh"><span class="lit">1<span class="pln"> row created<span class="pun">.</span></span></span></code></li>
<li class="L6"><code class="language-sh"></code></li>
<li class="L7"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;</span></span></code></li>
<li class="L8"><code class="language-sh"><span class="lit">1<span class="pln"> row updated<span class="pun">.</span></span></span></code></li>
<li class="L9"><code class="language-sh"></code></li>
<li class="L0"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;</span></span></code></li>
<li class="L1"><code class="language-sh"><span class="lit">1<span class="pln"> row deleted<span class="pun">.</span></span></span></code></li>
<li class="L2"><code class="language-sh"></code></li>
<li class="L3"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;<span class="pln"> commit<span class="pun">;</span></span></span></span></code></li>
<li class="L4"><code class="language-sh"></code></li>
<li class="L5"><code class="language-sh"><span class="typ">Commit<span class="pln"> complete<span class="pun">.</span></span></span></code></li>







</ol></div>
<div>（2）查看数据发布情况</div>







</div>
<div>
<div><ol class="linenums">
<li class="L0"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;</span></span></code></li>
<li class="L1"><code class="language-sh"><span class="pln">conn cdc_publisher<span class="pun">/<span class="pln">cdc_publisher</span></span></span></code></li>
<li class="L2"><code class="language-sh"><span class="typ">Connected<span class="pun">.</span></span></code></li>
<li class="L3"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;<span class="pln"> select OPERATION$<span class="pun">,<span class="pln">n<span class="pun">,<span class="pln">name<span class="pun">,<span class="pln">address from employee_info_ct<span class="pun">;</span></span></span></span></span></span></span></span></span></span></code></li>
<li class="L4"><code class="language-sh"></code></li>
<li class="L5"><code class="language-sh"><span class="pln">OP	    N NAME</span></code></li>
<li class="L6"><code class="language-sh"><span class="pun">--<span class="pln"> <span class="pun">----------<span class="pln"> <span class="pun">--------------------</span></span></span></span></span></code></li>
<li class="L7"><code class="language-sh"><span class="pln">ADDRESS</span></code></li>
<li class="L8"><code class="language-sh"><span class="pun">--------------------------------------------------------------------------------</span></code></li>
<li class="L9"><code class="language-sh"><span class="pln">I	    <span class="lit">1<span class="pln"> bendsha</span></span></span></code></li>
<li class="L0"><code class="language-sh"><span class="pln">lianhang road<span class="pun">,<span class="pln"> shanghai<span class="pun">,<span class="pln"> <span class="typ">China</span></span></span></span></span></span></code></li>
<li class="L1"><code class="language-sh"></code></li>
<li class="L2"><code class="language-sh"><span class="pln">I	    <span class="lit">2<span class="pln"> bendsha</span></span></span></code></li>
<li class="L3"><code class="language-sh"><span class="pln">lianhang road<span class="pun">,<span class="pln"> shanghai<span class="pun">,<span class="pln"> <span class="typ">China</span></span></span></span></span></span></code></li>
<li class="L4"><code class="language-sh"></code></li>
<li class="L5"><code class="language-sh"><span class="pln">I	    <span class="lit">3<span class="pln"> bendsha</span></span></span></code></li>
<li class="L6"><code class="language-sh"><span class="pln">lianhang road<span class="pun">,<span class="pln"> shanghai<span class="pun">,<span class="pln"> <span class="typ">China</span></span></span></span></span></span></code></li>
<li class="L7"><code class="language-sh"></code></li>
<li class="L8"><code class="language-sh"></code></li>
<li class="L9"><code class="language-sh"><span class="pln">OP	    N NAME</span></code></li>
<li class="L0"><code class="language-sh"><span class="pun">--<span class="pln"> <span class="pun">----------<span class="pln"> <span class="pun">--------------------</span></span></span></span></span></code></li>
<li class="L1"><code class="language-sh"><span class="pln">ADDRESS</span></code></li>
<li class="L2"><code class="language-sh"><span class="pun">--------------------------------------------------------------------------------</span></code></li>
<li class="L3"><code class="language-sh"><span class="pln">UO	    <span class="lit">2<span class="pln"> bendsha</span></span></span></code></li>
<li class="L4"><code class="language-sh"><span class="pln">lianhang road<span class="pun">,<span class="pln"> shanghai<span class="pun">,<span class="pln"> <span class="typ">China</span></span></span></span></span></span></code></li>
<li class="L5"><code class="language-sh"></code></li>
<li class="L6"><code class="language-sh"><span class="pln">UN	    <span class="lit">2<span class="pln"> zhuzi</span></span></span></code></li>
<li class="L7"><code class="language-sh"><span class="pln">lianhang road<span class="pun">,<span class="pln"> shanghai<span class="pun">,<span class="pln"> <span class="typ">China</span></span></span></span></span></span></code></li>
<li class="L8"><code class="language-sh"></code></li>
<li class="L9"><code class="language-sh"><span class="pln">D	    <span class="lit">1<span class="pln"> bendsha</span></span></span></code></li>
<li class="L0"><code class="language-sh"><span class="pln">lianhang road<span class="pun">,<span class="pln"> shanghai<span class="pun">,<span class="pln"> <span class="typ">China</span></span></span></span></span></span></code></li>
<li class="L1"><code class="language-sh"></code></li>
<li class="L2"><code class="language-sh"></code></li>
<li class="L3"><code class="language-sh"><span class="lit">6<span class="pln"> rows selected<span class="pun">.</span></span></span></code></li>







</ol></div>
<div>（3）查看数据订阅情况</div>







</div>
<div>
<div><ol class="linenums">
<li class="L0"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;</span></span></code></li>
<li class="L1"><code class="language-sh"><span class="pln">conn cdc_subscriber<span class="pun">/<span class="pln">cdc_subscriber</span></span></span></code></li>
<li class="L2"><code class="language-sh"><span class="pln">BEGIN</span></code></li>
<li class="L3"><code class="language-sh"><span class="pln">DBMS_CDC_SUBSCRIBE<span class="pun">.<span class="pln">EXTEND_WINDOW<span class="pun">(</span></span></span></span></code></li>
<li class="L4"><code class="language-sh"><span class="pln">subscription_name <span class="pun">=&gt;<span class="pln"> <span class="str">'employee_info_sub'<span class="pun">);</span></span></span></span></span></code></li>
<li class="L5"><code class="language-sh"><span class="pln">END<span class="pun">;</span></span></code></li>
<li class="L6"><code class="language-sh"><span class="typ">Connected<span class="pun">.</span></span></code></li>
<li class="L7"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;<span class="pln">   <span class="lit">2<span class="pln">    <span class="lit">3<span class="pln">    <span class="lit">4<span class="pln">    <span class="lit">5<span class="pln">  <span class="pun">/</span></span></span></span></span></span></span></span></span></span></span></span></code></li>
<li class="L8"><code class="language-sh"></code></li>
<li class="L9"><code class="language-sh"><span class="pln">PL<span class="pun">/<span class="pln">SQL procedure successfully completed<span class="pun">.</span></span></span></span></code></li>
<li class="L0"><code class="language-sh"></code></li>
<li class="L1"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;<span class="pln"> select OPERATION$<span class="pun">,<span class="pln">n<span class="pun">,<span class="pln">name<span class="pun">,<span class="pln">address from employee_info_view<span class="pun">;</span></span></span></span></span></span></span></span></span></span></code></li>
<li class="L2"><code class="language-sh"></code></li>
<li class="L3"><code class="language-sh"><span class="pln">OP	    N NAME</span></code></li>
<li class="L4"><code class="language-sh"><span class="pun">--<span class="pln"> <span class="pun">----------<span class="pln"> <span class="pun">--------------------</span></span></span></span></span></code></li>
<li class="L5"><code class="language-sh"><span class="pln">ADDRESS</span></code></li>
<li class="L6"><code class="language-sh"><span class="pun">--------------------------------------------------------------------------------</span></code></li>
<li class="L7"><code class="language-sh"><span class="pln">I	    <span class="lit">1<span class="pln"> bendsha</span></span></span></code></li>
<li class="L8"><code class="language-sh"><span class="pln">lianhang road<span class="pun">,<span class="pln"> shanghai<span class="pun">,<span class="pln"> <span class="typ">China</span></span></span></span></span></span></code></li>
<li class="L9"><code class="language-sh"></code></li>
<li class="L0"><code class="language-sh"><span class="pln">I	    <span class="lit">2<span class="pln"> bendsha</span></span></span></code></li>
<li class="L1"><code class="language-sh"><span class="pln">lianhang road<span class="pun">,<span class="pln"> shanghai<span class="pun">,<span class="pln"> <span class="typ">China</span></span></span></span></span></span></code></li>
<li class="L2"><code class="language-sh"></code></li>
<li class="L3"><code class="language-sh"><span class="pln">I	    <span class="lit">3<span class="pln"> bendsha</span></span></span></code></li>
<li class="L4"><code class="language-sh"><span class="pln">lianhang road<span class="pun">,<span class="pln"> shanghai<span class="pun">,<span class="pln"> <span class="typ">China</span></span></span></span></span></span></code></li>
<li class="L5"><code class="language-sh"></code></li>
<li class="L6"><code class="language-sh"></code></li>
<li class="L7"><code class="language-sh"><span class="pln">OP	    N NAME</span></code></li>
<li class="L8"><code class="language-sh"><span class="pun">--<span class="pln"> <span class="pun">----------<span class="pln"> <span class="pun">--------------------</span></span></span></span></span></code></li>
<li class="L9"><code class="language-sh"><span class="pln">ADDRESS</span></code></li>
<li class="L0"><code class="language-sh"><span class="pun">--------------------------------------------------------------------------------</span></code></li>
<li class="L1"><code class="language-sh"><span class="pln">UO	    <span class="lit">2<span class="pln"> bendsha</span></span></span></code></li>
<li class="L2"><code class="language-sh"><span class="pln">lianhang road<span class="pun">,<span class="pln"> shanghai<span class="pun">,<span class="pln"> <span class="typ">China</span></span></span></span></span></span></code></li>
<li class="L3"><code class="language-sh"></code></li>
<li class="L4"><code class="language-sh"><span class="pln">UN	    <span class="lit">2<span class="pln"> zhuzi</span></span></span></code></li>
<li class="L5"><code class="language-sh"><span class="pln">lianhang road<span class="pun">,<span class="pln"> shanghai<span class="pun">,<span class="pln"> <span class="typ">China</span></span></span></span></span></span></code></li>
<li class="L6"><code class="language-sh"></code></li>
<li class="L7"><code class="language-sh"><span class="pln">D	    <span class="lit">1<span class="pln"> bendsha</span></span></span></code></li>
<li class="L8"><code class="language-sh"><span class="pln">lianhang road<span class="pun">,<span class="pln"> shanghai<span class="pun">,<span class="pln"> <span class="typ">China</span></span></span></span></span></span></code></li>
<li class="L9"><code class="language-sh"></code></li>
<li class="L0"><code class="language-sh"></code></li>
<li class="L1"><code class="language-sh"><span class="lit">6<span class="pln"> rows selected<span class="pun">.</span></span></span></code></li>







</ol></div>
<div>（4）清除变更数据集</div>







</div>
<div>
<div><ol class="linenums">
<li class="L0"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;</span></span></code></li>
<li class="L1"><code class="language-sh"><span class="pln">conn cdc_subscriber<span class="pun">/<span class="pln">cdc_subscriber</span></span></span></code></li>
<li class="L2"><code class="language-sh"><span class="pln">BEGIN</span></code></li>
<li class="L3"><code class="language-sh"><span class="pln">DBMS_CDC_SUBSCRIBE<span class="pun">.<span class="pln">PURGE_WINDOW<span class="pun">(</span></span></span></span></code></li>
<li class="L4"><code class="language-sh"><span class="pln">subscription_name <span class="pun">=&gt;<span class="pln"> <span class="str">'employee_info_sub'<span class="pun">);</span></span></span></span></span></code></li>
<li class="L5"><code class="language-sh"><span class="pln">END<span class="pun">;</span></span></code></li>
<li class="L6"><code class="language-sh"><span class="typ">Connected<span class="pun">.</span></span></code></li>
<li class="L7"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;<span class="pln">   <span class="lit">2<span class="pln">    <span class="lit">3<span class="pln">    <span class="lit">4<span class="pln">    <span class="lit">5<span class="pln">  <span class="pun">/</span></span></span></span></span></span></span></span></span></span></span></span></code></li>
<li class="L8"><code class="language-sh"></code></li>
<li class="L9"><code class="language-sh"><span class="pln">PL<span class="pun">/<span class="pln">SQL procedure successfully completed<span class="pun">.</span></span></span></span></code></li>
<li class="L0"><code class="language-sh"></code></li>
<li class="L1"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;<span class="pln"> select OPERATION$<span class="pun">,<span class="pln">n<span class="pun">,<span class="pln">name<span class="pun">,<span class="pln">address from employee_info_view<span class="pun">;</span></span></span></span></span></span></span></span></span></span></code></li>
<li class="L2"><code class="language-sh"></code></li>
<li class="L3"><code class="language-sh"><span class="pln">no rows selected</span></code></li>







</ol></div>
<div>（5）删除发布数据</div>







</div>
<div>
<div><ol class="linenums">
<li class="L0"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;<span class="pln"> conn cdc_publisher<span class="pun">/<span class="pln">cdc_publisher</span></span></span></span></span></code></li>
<li class="L1"><code class="language-sh"></code></li>
<li class="L2"><code class="language-sh"><span class="typ">Connected<span class="pun">.</span></span></code></li>
<li class="L3"><code class="language-sh"></code></li>
<li class="L4"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;<span class="pln"> truncate table employee_info_ct<span class="pun">;</span></span></span></span></code></li>
<li class="L5"><code class="language-sh"></code></li>
<li class="L6"><code class="language-sh"><span class="typ">Table<span class="pln"> truncated<span class="pun">.</span></span></span></code></li>
<li class="L7"><code class="language-sh"></code></li>
<li class="L8"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;<span class="pln"> select OPERATION$<span class="pun">,<span class="pln">n<span class="pun">,<span class="pln">name<span class="pun">,<span class="pln">address from employee_info_ct<span class="pun">;</span></span></span></span></span></span></span></span></span></span></code></li>
<li class="L9"><code class="language-sh"></code></li>
<li class="L0"><code class="language-sh"><span class="pln">no rows selected</span></code></li>







</ol></div>







</div>







</div>
<div>
<h2>3.&nbsp;常见问题解决方法</h2>
<h3>3.1&nbsp;<span data-wiz-span="data-wiz-span">ORA-31466: 未找到发布内容</span></h3>
<div>执行订阅表信息时，提示ORA-31466：未找到发布内容，排查发现是没有将变更表cdc_employee_info的查询权限赋予订阅者用户cdc_subscriber导致。</div>







</div>
<div>解决方法：</div>
<div>
<div><ol class="linenums">
<li class="L0"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;<span class="pln"> conn cdc_publisher<span class="pun">/<span class="pln">cdc_publisher</span></span></span></span></span></code></li>
<li class="L1"><code class="language-sh"><span class="typ">Connected<span class="pun">.</span></span></code></li>
<li class="L2"><code class="language-sh"><span class="pln">SQL<span class="pun">&gt;<span class="pln"> grant select on cdc_employee_info to cdc_subscriber<span class="pun">;</span></span></span></span></code></li>
<li class="L3"><code class="language-sh"></code></li>
<li class="L4"><code class="language-sh"><span class="typ">Grant<span class="pln"> succeeded<span class="pun">.</span></span></span></code></li>







</ol></div>
<div>
<h3>3.2 激活订阅之后，对源表进行操作，捕获不到数据</h3>
<div>我一开始遇到这个问题是监控系统用户SYS用户的employee_info表，没有出现任何异常，就是捕获不到数据，后来我替换给backupuser用户的employee_info表，按照以上步骤操作，就能正常捕获到数据了，官网也没查到相关的文档说明，很奇怪，还需要进一步研究。</div>







</div>







</div>
<div>
<h2>4. 参考文档</h2>







</div>
<div>Oracle 10.2 CDC：<a href="http://docs.oracle.com/cd/B19306_01/server.102/b14223/cdc.htm">http://docs.oracle.com/cd/B19306_01/server.102/b14223/cdc.htm</a></div>
<div>Oracle 11g CDC：<a href="http://docs.oracle.com/cd/B28359_01/server.111/b28313/cdc.htm#CHDEHIIE">http://docs.oracle.com/cd/B28359_01/server.111/b28313/cdc.htm#CHDEHIIE</a></div>
<div>DBMS_CDC_PUBLISH：<a href="http://docs.oracle.com/cd/E11882_01/appdev.112/e40758/d_cdcpub.htm#ARPLS023">http://docs.oracle.com/cd/E11882_01/appdev.112/e40758/d_cdcpub.htm#ARPLS023</a></div>
<div>DBMS_CDC_SUBSCRIBE：<a href="http://docs.oracle.com/cd/E11882_01/appdev.112/e40758/d_cdcsub.htm#ARPLS024">http://docs.oracle.com/cd/E11882_01/appdev.112/e40758/d_cdcsub.htm#ARPLS024</a></div>
</div>
<div id="MySignature"></div>
<div class="clear"></div>
<div id="blog_post_info_block"><div id="BlogPostCategory">
分类: 
<a href="https://www.cnblogs.com/bendsha/category/1009357.html" target="_blank">Database</a></div>
<div id="EntryTag">
标签: 
<a href="https://www.cnblogs.com/bendsha/tag/Oracle%20CDC/">Oracle CDC</a>,             <a href="https://www.cnblogs.com/bendsha/tag/ETL/">ETL</a>,             <a href="https://www.cnblogs.com/bendsha/tag/Database%20Replication/">Database Replication</a></div>

<div id="blog_post_info">
<div id="green_channel">
<a href="javascript:void(0);" id="green_channel_digg" onclick="DiggIt(6910152,cb_blogId,1);green_channel_success(this,'谢谢推荐！');">好文要顶</a>
<a id="green_channel_follow" onclick="follow('61a74284-b3b0-e611-845c-ac853d9f53ac');" href="javascript:void(0);">关注我</a>
<a id="green_channel_favorite" onclick="AddToWz(cb_entryId);return false;" href="javascript:void(0);">收藏该文</a>
<a id="green_channel_weibo" href="javascript:void(0);" title="分享至新浪微博" onclick="ShareToTsina()"><img src="https://common.cnblogs.com/images/icon_weibo_24.png" alt=""></a>
<a id="green_channel_wechat" href="javascript:void(0);" title="分享至微信" onclick="shareOnWechat()"><img src="https://common.cnblogs.com/images/wechat.png" alt=""></a>
</div>
<div id="author_profile">
<div id="author_profile_info" class="author_profile_info">
<div id="author_profile_detail" class="author_profile_info">
<a href="https://home.cnblogs.com/u/bendsha/">bendsha</a><br>
<a href="https://home.cnblogs.com/u/bendsha/followees/">关注 - 6</a><br>
<a href="https://home.cnblogs.com/u/bendsha/followers/">粉丝 - 1</a>
</div>
</div>
<div class="clear"></div>
<div id="author_profile_honor"></div>
<div id="author_profile_follow">
<a href="javascript:void(0);" onclick="follow('61a74284-b3b0-e611-845c-ac853d9f53ac');return false;">+加关注</a>
</div>
</div>
<div id="div_digg">
<div class="diggit" onclick="votePost(6910152,'Digg')">
<span class="diggnum" id="digg_count">2</span>
</div>
<div class="buryit" onclick="votePost(6910152,'Bury')">
<span class="burynum" id="bury_count">0</span>
</div>
<div class="clear"></div>
<div class="diggword" id="digg_tips">
</div>
</div>

<script type="text/javascript">
currentDiggType = 0;
</script></div>
<div class="clear"></div>
<div id="post_next_prev">

<a href="https://www.cnblogs.com/bendsha/p/6198908.html" class="p_n_p_prefix">« </a> 上一篇：    <a href="https://www.cnblogs.com/bendsha/p/6198908.html" title="发布于 2016-12-19 19:57">VMware Linux Guest 增加磁盘无需重启的方法</a>
<br>
<a href="https://www.cnblogs.com/bendsha/p/11145461.html" class="p_n_p_prefix">» </a> 下一篇：    <a href="https://www.cnblogs.com/bendsha/p/11145461.html" title="发布于 2019-07-07 10:49">几种常见的Docker数据容器保护方式利与弊</a>

</div>
</div></div>
<p class="postfoot">
posted on 
<span id="post-date">2017-05-26 19:27</span>&nbsp;
<a href="https://www.cnblogs.com/bendsha/">bendsha</a>&nbsp;
阅读(<span id="post_view_count">8303</span>)&nbsp;
评论(<span id="post_comment_count">0</span>)&nbsp;
<a href="https://i.cnblogs.com/EditPosts.aspx?postid=6910152" rel="nofollow">
编辑
</a>
<a href="javascript:void(0)" onclick="AddToWz(6910152);return false;">收藏</a>
</p>
</div>



<script src="https://common.cnblogs.com/highlight/9.12.0/highlight.min.js"></script>
<script>markdown_highlight();</script>
<script>
var allowComments = true, cb_blogId = 318759, cb_blogApp = 'bendsha', cb_blogUserGuid = '61a74284-b3b0-e611-845c-ac853d9f53ac';
var cb_entryId = 6910152, cb_entryCreatedDate = '2017-05-26 19:27', cb_postType = 1; 
loadViewCount(cb_entryId);
</script><a name="!comments"></a>
<div id="blog-comments-placeholder"></div>
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
<div id="ad_t2"><a href="http://www.ucancode.com/index.htm" target="_blank" onclick="ga('send', 'event', 'Link', 'click', 'T2-工控')">【推荐】超50万行VC++源码: 大型组态工控、电力仿真CAD与GIS源码库</a><br><a href="https://cloud.tencent.com/act/pro/overseas?fromSource=gwzcw.3090393.3090393.3090393&amp;utm_medium=cpc&amp;utm_id=gwzcw.3090393.3090393.3090393" target="_blank" onclick="ga('send', 'event', 'Link', 'click', 'T2-腾讯云')">【推荐】腾讯云海外1核2G云服务器低至2折，半价续费券限量免费领取！</a><br><a href="http://click.aliyun.com/m/1000081987/" target="_blank" onclick="ga('send', 'event', 'Link', 'click', 'T2-阿里云')">【推荐】阿里云双11返场来袭，热门产品低至一折等你来抢！</a><br><a href="https://www.ctyun.cn/activity/20191111?hmsr=%E7%9C%8B%E7%9C%8B-%E5%8D%9A%E5%AE%A2%E5%9B%AD-%E5%8F%8C11-1106&amp;hmpl=&amp;hmcu=&amp;hmkw=&amp;hmci=" target="_blank" onclick="ga('send', 'event', 'Link', 'click', 'T2-天翼云')">【推荐】天翼云双十一翼降到底，云主机11.11元起，抽奖送大礼</a><br><a href="https://www.uibot.com.cn/sem/bky.html" target="_blank" onclick="ga('send', 'event', 'Link', 'click', 'T2-uibot_T2')">【推荐】流程自动化专家UiBot，体系化教程成就高薪RPA工程师</a><br><a href="https://www.jdcloud.com/cn/activity/year-end?utm_source=DMT_cnblogs&amp;utm_medium=CH&amp;utm_campaign=q4vm&amp;utm_term=Virtual-Machines" target="_blank" onclick="ga('send', 'event', 'Link', 'click', 'T2-京东云')">【活动】京东云服务器_云主机低于1折，低价高性能产品备战双11</a><br><a href="https://www.qiniu.com/events/20191111?utm_campaign=2019_1111&amp;utm_content=cnblogs_1111&amp;utm_medium=banner&amp;utm_source=cnblogs&amp;utm_term=cnblogs_1111" target="_blank" onclick="ga('send', 'event', 'Link', 'click', 'T2-七牛云')">【优惠】七牛云采购嘉年华，云存储、CDN等云产品低至1折</a><br></div>
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
<div id="div-gpt-ad-1546353474406-0" style="height:250px; width:300px;" data-google-query-id="CL6tofHDmeYCFREEXAodmisCAA"><div id="google_ads_iframe_/1090369/C1_0__container__" style="border: 0pt none;"><iframe id="google_ads_iframe_/1090369/C1_0" title="3rd party ad content" name="google_ads_iframe_/1090369/C1_0" width="300" height="250" scrolling="no" marginwidth="0" marginheight="0" frameborder="0" srcdoc="" style="border: 0px; vertical-align: bottom;" data-google-container-id="1" data-load-complete="true"></iframe></div></div>
</div>
<div id="under_post_news"><div class="recomm-block"><b>相关博文：</b><br>·  <a title="SQL Server 变更数据捕获（CDC）监控表数据" href="https://www.cnblogs.com/gaizai/p/3479731.html" target="_blank" onclick="clickRecomItmem(3479731)">SQL Server 变更数据捕获（CDC）监控表数据</a><br>·  <a title="在SQL Server 2008中实现change data capture (CDC) Part One" href="https://www.cnblogs.com/esestt/archive/2007/06/10/777934.html" target="_blank" onclick="clickRecomItmem(777934)">在SQL Server 2008中实现change data capture (CDC) Part One</a><br>·  <a title="SQL Server审计功能入门：CDC（Change Data Capture）" href="https://www.cnblogs.com/Joe-T/p/4312806.html" target="_blank" onclick="clickRecomItmem(4312806)">SQL Server审计功能入门：CDC（Change Data Capture）</a><br>·  <a title="SQL Server 2008 的CDC功能" href="https://www.cnblogs.com/chenxizhang/archive/2009/04/28/1445297.html" target="_blank" onclick="clickRecomItmem(1445297)">SQL Server 2008 的CDC功能</a><br>·  <a title="SQL Server 变更数据捕获(CDC)" href="https://www.cnblogs.com/chenmh/p/4408825.html" target="_blank" onclick="clickRecomItmem(4408825)">SQL Server 变更数据捕获(CDC)</a><br>»  <a target="_blank" href="https://recomm.cnblogs.com/blogpost/6910152">更多推荐...</a><div id="cnblogs_t5"><a href="https://developer.aliyun.com/ask/258350?utm_content=g_1000088952" target="_blank" onclick="ga('send', 'event', 'Link', 'click', 'T5-阿里云开发者社区')">精品问答：前端开发必懂之 HTML 技术五十问</a></div></div></div>
<div id="cnblogs_c2" class="c_ad_block">
<div id="div-gpt-ad-1539008685004-0" style="height:60px; width:468px;" data-google-query-id="CL-tofHDmeYCFREEXAodmisCAA">

<div id="google_ads_iframe_/1090369/C2_0__container__" style="border: 0pt none;"><iframe id="google_ads_iframe_/1090369/C2_0" title="3rd party ad content" name="google_ads_iframe_/1090369/C2_0" width="468" height="60" scrolling="no" marginwidth="0" marginheight="0" frameborder="0" srcdoc="" style="border: 0px; vertical-align: bottom;" data-google-container-id="2" data-load-complete="true"></iframe></div></div>
</div>
<div id="under_post_kb">
<div class="itnews c_ad_block">
<b>最新 IT 新闻</b>:
<br>
·              <a href="//news.cnblogs.com/n/651386/" target="_blank">鹏城云脑II千P级AI算力背后，华为鲲鹏昇腾的落地大幕已拉开</a>
<br>
·              <a href="//news.cnblogs.com/n/651385/" target="_blank">长征八号运载火箭芯二级氢氧发动机高空模拟试验成功 预计明年首飞</a>
<br>
·              <a href="//news.cnblogs.com/n/651384/" target="_blank">董小姐命好</a>
<br>
·              <a href="//news.cnblogs.com/n/651383/" target="_blank">华米CEO黄汪：自主研发的黄山2号芯片2020年量产</a>
<br>
·              <a href="//news.cnblogs.com/n/651382/" target="_blank">Are you OK？搜狗输入法上线“仿雷军”音色</a>
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
</div></div>


</div>