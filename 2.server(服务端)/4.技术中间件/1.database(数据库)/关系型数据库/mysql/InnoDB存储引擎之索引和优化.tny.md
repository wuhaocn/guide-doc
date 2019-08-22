<div class="content"><h1>InnoDB 存储引擎之索引和优化</h1>
<div class="meta">
    <a href="http://ju.outofmemory.cn/feed/123/" title="ImportNew"><i class="ico man"></i>ImportNew</a>
    <time><i class="ico date"></i>2018-10-22</time>
    <span class="pv"><b>87</b> 阅读</span>
</div>
<div class="tags">
<a class="tag" href="http://ju.outofmemory.cn/tag/innodb/">innodb</a>

<a class="tag" href="http://ju.outofmemory.cn/tag/mysql/">mysql</a>

</div>

<script type="text/javascript" async="" src="http://pos.baidu.com/auto_dup?psi=b95fd2cab7529eb864b4640908c0e85f&amp;di=0&amp;dri=0&amp;dis=0&amp;dai=0&amp;ps=0&amp;enu=encoding&amp;dcb=___baidu_union_callback_&amp;dtm=AUTO_JSONP&amp;dvi=0.0&amp;dci=-1&amp;dpt=none&amp;tsr=0&amp;tpr=1564112389937&amp;ti=InnoDB%20%E5%AD%98%E5%82%A8%E5%BC%95%E6%93%8E%E4%B9%8B%E7%B4%A2%E5%BC%95%E5%92%8C%E4%BC%98%E5%8C%96%20-%20%E4%B8%BA%E7%A8%8B%E5%BA%8F%E5%91%98%E6%9C%8D%E5%8A%A1&amp;ari=2&amp;dbv=2&amp;drs=1&amp;pcs=1265x666&amp;pss=1265x3327&amp;cfv=0&amp;cpl=3&amp;chi=2&amp;cce=true&amp;cec=UTF-8&amp;tlm=1564112389&amp;rw=666&amp;ltu=http%3A%2F%2Fju.outofmemory.cn%2Fentry%2F374349&amp;ltr=http%3A%2F%2Fju.outofmemory.cn%2Ffeed%2F123%2F&amp;ecd=1&amp;uc=1920x983&amp;pis=-1x-1&amp;sr=1920x1080&amp;tcn=1564112390&amp;dc=4"></script><script src="//hm.baidu.com/hm.js?2051de3619785a7bff6213250ea9fbbd"></script><script async="" src="//www.google-analytics.com/analytics.js"></script><script type="text/javascript" async="" src="http://pos.baidu.com/fcam?psi=b95fd2cab7529eb864b4640908c0e85f&amp;di=u2191912&amp;dri=0&amp;dis=0&amp;dai=1&amp;ps=3317x10&amp;enu=encoding&amp;dcb=___adblockplus&amp;dtm=SSP_JSONP&amp;dvi=0.0&amp;dci=-1&amp;dpt=none&amp;tsr=0&amp;tpr=1564112389937&amp;ti=InnoDB%20%E5%AD%98%E5%82%A8%E5%BC%95%E6%93%8E%E4%B9%8B%E7%B4%A2%E5%BC%95%E5%92%8C%E4%BC%98%E5%8C%96%20-%20%E4%B8%BA%E7%A8%8B%E5%BA%8F%E5%91%98%E6%9C%8D%E5%8A%A1&amp;ari=2&amp;dbv=2&amp;drs=1&amp;pcs=1265x666&amp;pss=1265x3327&amp;cfv=0&amp;cpl=3&amp;chi=2&amp;cce=true&amp;cec=UTF-8&amp;tlm=1564112389&amp;rw=666&amp;ltu=http%3A%2F%2Fju.outofmemory.cn%2Fentry%2F374349&amp;ltr=http%3A%2F%2Fju.outofmemory.cn%2Ffeed%2F123%2F&amp;ecd=1&amp;uc=1920x983&amp;pis=-1x-1&amp;sr=1920x1080&amp;tcn=1564112390&amp;exps=110011"></script><script>
    function imgError(img){
        if (typeof img.hasReplaceSrc != 'undefined'){
            var refer = $('div.author a:last').attr('href');
            img.src = '/imgr?src=' + encodeURIComponent(img.src) + '&r=' + encodeURIComponent(refer);
        } else {
            var urlPattern = /(http|ftp|https):\/\/[\w-]+(\.[\w-]+)+([\w.,@?^=%&amp;:\/~+#-]*[\w@?^=%&amp;\/~+#-])?/;
            for (var i=0;i<img.attributes.length;i++){
                var attrName = img.attributes[i].nodeName;
                var attrVal = img.attributes[i].nodeValue;
                if(attrName.toLowerCase() != 'src' && urlPattern.test(attrVal)){
                    img.src = attrVal;
                    img.hasReplaceSrc=true;
                    break;
                }
            }
        }
        return true;
    }
</script>
<div>
 原文出处：
 <a rel="nofollow external" href="http://taozj.net/201809/innodb-index.html" ref="nofollow" target="_blank">
  Nicol
 </a>
</div>
<p>
 数据库优化可以说是后台开发中永恒的话题，数据库的性能通常是整个服务吞吐量的瓶颈之所在。
</p>
<h3>
 1. 索引概述
</h3>
<p>
 InnoDB中的表都是按照主键顺序组织存放的，这种组织方式称之为索引组织表，对比于MyISAM的表组织方式。在InnoDB中每张表都必须有一个主键，如果在创建表的时候没有显式定义主键，则InnoDB首先会判断表中是否有非空的唯一索引，如果有则将该列作为主键；否则InnoDB会自动创建一个6字节大小的指针作为主键。除主键之外，InnoDB还可以有辅助索引，而辅助索引页中仅仅存放键值和指向数据页的偏移量，而不像主键数据页存储的是一个完整的行记录。
</p>
<p>
 InnoDB存储引擎中，所有的数据都被逻辑地存放在一个表空间中，表空间又被分为段(Segment)、区(Extent)、页(Page)组成，其中段由存储引擎自动管理，区的大小固定为1M，然后默认情况下页的大小为16KB，也就是一个区总共有64个连续的页组成。不过在MySQL5.6开始，页的大小可以设置为4K、8K了，设置成4K除了可以提高磁盘的利用率之外，对于现代SSD硬盘将更加合适，不过这中更新比较的麻烦，需要将输入导出后再重新导入，一般的备份恢复工具都是原样复制数据，没有办法支持变更页大小。
</p>
<p>
 默认的B+树索引其查找次数(效率)取决于B+树的高度，生产环境下一般树高为3~4层，即查询一条记录需要经过3~4个索引页，而且B+树索引并不能找到一个给定键值的具体行，其只能根据键和索引找到数据行所在的页，然后数据库把对应的页读取到内存，再在内存中执行查找，并最后得到需要查询的数据。InnoDB还会监控对表上各索引页的查询操作，如果观察到通过建立hash索引可以带来速度提升，则会根据访问频率和访问模式自动为部分热点页建立hash索引，这个过程称之为自适应哈希索引，而且该过程是人为无法干预、存储引擎自动实现的。
</p>
<p>
 使用索引的一大禁忌是不要在引用索引列的时候使用函数，比如max(id)、id+3&gt;5等，或者隐式的数据类型转换操作，这样会导致索引失效导致全扫描。
</p>
<h3>
 2. 在线修改数据表
</h3>
<p>
 在MySQL 5.5之前修改表结构、或者创建新索引的时候，需要经过：先锁定原始表，创建一张新的临时表(临时使用tmpdir路径，确保有足够空间可用)，然后把原表中的数据导入到新的临时表中，接着删除原表，最后再把临时表重新命名为原来的表名。所以修改表结构需要注意，将对同一个表的ALTER TABLE多个操作合并到一条语句中，减少上述重复的步骤。同时，针对修改列名、修改数值类型的表示长度INT(3)-&gt;INT(10)、修改数据表注释、向ENUM增加新的类型、修改数据表名这些操作不需要将数据表中的所有记录都复制到临时表。
</p>
<p>
 新版MySQL支持Fast Index Creation，具体说来就是对于新辅助索引的创建，InnoDB会对要创建索引的表上一个S锁，使该表以只读的可用性提供服务，由于不需要重新创建表、拷贝数据，因而辅助索引的创建速度也快很多；删除索引的时候InnoDB只需更新内部试图标记辅助索引的空间为可用，同时删除MySQL数据库内部试图上对应表的索引定义即可。
</p>
<p>
 MySQL 5.6的版本支持Online DDL，允许在辅助索引创建的同时，还允许对表同时执行诸如INSERT、UPDATE、DELETE等DML操作而不会被阻塞，其原理是在执行索引创建或者删除操作的时候，将INSERT、UPDATE、DELETE这类的操作日志先记录到一个叫做“在线修改日志”的内存空间中，当索引完成后再重新应用这些更新到表上，以此达到数据的一致性。不过“在线修改日志“只存留在内存中，默认大小是128MB，如果修改表结构时候DML操作太多，会导致该空间不够用而撤销修改。
</p>
<h3>
 3. 创建索引
</h3>
<p>
 创建索引的时候讲求一个Cardinality指标，该值表示索引中唯一值的估计数目，理想情况下该值除以表行数应该尽可能接近1，否则表示该列选择性太低而应该考虑删除该索引。 对Cardinality的统计是使用采样方式进行估算的，当表的修改数目超过总记录的1/16、或者修改总次数超过20亿次，则会随机选择8个数据页重新统计该值，不过通过ANALYZE TABLE命令可以强制让数据库重新收集相关的统计信息。
</p>
<p>
 实践中OLTP和OLAP对索引的要求是有差异的，在OLTP应用中查询操作通常只从数据库返回很小部分数据集，此时根据查询条件选择高区分度的列来创建索引是很有意义的；对于OLAP应用通常都需要返回大批量的数据，很多情况下建立索引意义不是很大，因为大量数据返回的话往往全表顺序扫描效率更高，不过OLAP中对时间创建索引是很常见的操作。
</p>
<h3>
 4. 覆盖索引
</h3>
<p>
 表示直接从辅助索引中就可以得到需要的查询记录，而不需要再从聚簇索引中查询行记录。使用覆盖索引的好处是辅助索引不包含整行记录，所以索引大小会远远小于聚簇索引，单个索引页就可以存储更多的索引项，那么访问索引本身的操作就可以减少顺序IO操作了。有些情况，比如在MySQL中SELECT COUNT(*) FROM t;优化器是可以选择使用辅助索引来优化查询速度的，因为可以访问更少的索引页就可以统计到查询结果了。
</p>
<p>
 如果SELECT列不能使用覆盖索引完成，那么除了在辅助索引上查到指定记录后，还需要进行一次书签访问才能查找到整行中其他列的数据，并且此时的查找将是成本很高的随机离散读操作(相对于传统机械磁盘)。
</p>
<p>
 所以如果优化器觉得需要返回的数据量很少，则优化器还是可能会选择使用辅助索引外加访问聚簇索引的方式来返回记录的；但是当访问数据量占整个表记录中挺大一部分的时候(比如20%)，则优化器可能会选择全表扫描的方式来查找数据，因为全表顺序读的代价可能比大量随机读的效率要高。大部分时候优化器都能做的不错，不过当用户有对索引的使用有足够信心的时候还是可以影响优化器执行计划的生成的，比如：可以使用USE INDEX的方式来提示优化器使用某个索引，不过实际上优化器还是会根据自己的判断确定是否需要使用该索引；而通过FORCE INDEX则会强制选择使用该索引；使用IGNORE INDEX会使优化器不能使用指定的索引，这通常可以诱导触发执行全表扫描。
</p>
<h3>
 5. Multi-Range Read(MRR)优化
</h3>
<p>
 为了防止非覆盖索引取数据的时候造成的大量随机I/O，MyISAM和InnoDB会将查询到的辅助索引存放在一个缓存中，然后将他们通过主键进行排序，并按排序后的主键进行顺序书签查找。通过这种方式可以将低效随机访问转化为高效顺序数据访问，而且同一数据块确保只需要被访问一次，同时也减少缓冲池中页被替换的次数，所以可以带来查询性能的极大提升。
</p>
<p>
 MySQL5.6开始支持该项优化，使用的时候需要SET optimizer_switch=’mrr=on|off’的方式打开。MRR特性可以用于range、ref、eq_ref类型的查询操作，当查询使用到该特性的时候就可以在Extra看到Using MRR提示了，当在有表连接的情况下，如果连接键是被驱动表的主键的时候，也会先基于驱动表的连接键进行排序，按照这个顺序就可以MRR按照被驱动表的主键访问数据了。
</p>
<p>
 从上面的介绍看到MRR是一个思路简单但是却很重要的优化，但是在某些情况下使用也可能会有负面效应。当表的数据量很小，大部分数据也都被缓存的时候，使用MRR不会带来随机访问的收益，反而会因为额外的排序操作增加资源消耗；当限制只需要返回LIMIT n的时候，这种优化会读取排序很多不需要的索引，性能反而会降低；排序使用的内存空间大小由mrr_buffer_size设定的，如果该内存较小但是待排序的索引数量大的时候，就需要使用磁盘辅助进行多块排序归并，这也会降低性能。
</p>
<h3>
 6. Index Condition Pushdown(ICP)优化
</h3>
<p>
 老旧数据库版本只有索引可用的限制条件才会被传输到存储引擎层，在新版本开启ICP优化的时候，针对选用索引涉及到的数据列条件就都会被传输到存储引擎层，所以在支持ICP特性后，存储引擎在处理索引的同时就可以判断是否可以通过下推的选择条件对部分记录直接进行过滤操作了。所以在老版本的数据库，都是存储引擎对索引可以直接使用的条件进行操作，然后再将这些数据传递给MySQL引擎，这样就会涉及到大量数据条目的读取、传递和筛选工作，这时候在Extra中肯定会看到Using where的提示，因为MySQL引擎对存储引擎传递来的数据进行了筛选加工；现在将索引涉及到的筛选条件下推放到了存储引擎层，就大大减少了上面的操作任务。
</p>
<p>
 该功能可以使用SET optimizer_switch=’index_condition_pushdown=on|off’的方式打开或者关闭。ICP优化可以用于range、ref、req_ref、ref_or_null类型的查询，当查询使用到该特性的时候可以在Extra看到Using index condition。
</p>
<h3>
 7. 索引合并
</h3>
<p>
 当查询WHERE中罗列有多个条件，他们都可以使用不同的索引进行优化查询的时候，如果优化器发现某一个索引返回的记录相比其他索引显著的要少，那么执行计划就会选用这个索引；而如果优化器发现多个索引都不高效的时候，优化器会将这些查询条件分离，用各自的索引分别独立执行检索，最后再将多个结果集合进行合并后返回。当然，这种情况优化器也可能使用全表扫面的方式处理。
</p>
<p>
 本文完！
</p>
<h1>
 参考
</h1>
<div>
 <ul>
  <li>
   <a class="external" href="https://book.douban.com/subject/24708143/" rel="noopener nofollow" target="_blank">
    MySQL技术内幕 – InnoDB存储引擎
   </a>
  </li>
 </ul>
</div>

<div class="like">
    <a href="javascript:void(0)" class="assertLogin" rel="nofollow" target="_self">点赞</a>
</div>
<div class="tags">
<a class="tag" href="http://ju.outofmemory.cn/tag/innodb/">innodb</a>

<a class="tag" href="http://ju.outofmemory.cn/tag/mysql/">mysql</a>

</div>

<div class="author">
    <span class="name">作者：<a href="http://ju.outofmemory.cn/feed/123/" title="ImportNew">ImportNew</a></span>
    <div class="authorAvatar">
        <a href="http://ju.outofmemory.cn/feed/123/">
            <img width="128" align="center" valign="absmiddle" src="http://ju.outofmemory.cn/flogos/3/123.png" alt="ImportNew">
        </a>
    </div>
    <div class="small">
    </div>
    <div class="small">原文地址：<a rel="nofollow external" target="_blank" href="http://www.importnew.com/30259.html">InnoDB 存储引擎之索引和优化</a>, 感谢原作者分享。</div>
</div>

<div class="pn">
    <span class="next"><mark>→</mark><a href="/entry/374556">使用Thread Pool不当引发的死锁</a></span>
    <span class="pre"><mark>←</mark><a href="/entry/374348">ssh 服务突然连接不了案例总结</a></span>
</div>
<div class="tgBD" style="width: 597px; text-align: center; overflow: hidden; border: 1px solid transparent; border-radius: 2px;">
<script type="text/javascript">

var cpro_id = "u2191912"
</script>
<script src="http://cpro.baidustatic.com/cpro/ui/cm.js" type="text/javascript"></script><div id="BAIDU_SSP__wrapper_u2191912_0"><iframe id="iframeu2191912_0" name="iframeu2191912_0" src="http://pos.baidu.com/fcam?conwid=760&amp;conhei=90&amp;rdid=2191912&amp;dc=3&amp;exps=110011&amp;psi=b95fd2cab7529eb864b4640908c0e85f&amp;di=u2191912&amp;dri=0&amp;dis=0&amp;dai=1&amp;ps=3317x10&amp;enu=encoding&amp;dcb=___adblockplus&amp;dtm=HTML_POST&amp;dvi=0.0&amp;dci=-1&amp;dpt=none&amp;tsr=0&amp;tpr=1564112389937&amp;ti=InnoDB%20%E5%AD%98%E5%82%A8%E5%BC%95%E6%93%8E%E4%B9%8B%E7%B4%A2%E5%BC%95%E5%92%8C%E4%BC%98%E5%8C%96%20-%20%E4%B8%BA%E7%A8%8B%E5%BA%8F%E5%91%98%E6%9C%8D%E5%8A%A1&amp;ari=2&amp;dbv=2&amp;drs=1&amp;pcs=1265x666&amp;pss=1265x3327&amp;cfv=0&amp;cpl=3&amp;chi=2&amp;cce=true&amp;cec=UTF-8&amp;tlm=1564112389&amp;rw=666&amp;ltu=http%3A%2F%2Fju.outofmemory.cn%2Fentry%2F374349&amp;ltr=http%3A%2F%2Fju.outofmemory.cn%2Ffeed%2F123%2F&amp;ecd=1&amp;uc=1920x983&amp;pis=-1x-1&amp;sr=1920x1080&amp;tcn=1564112390&amp;qn=de818ea5d8695a45&amp;tt=1564112389916.29.205.211" width="760" height="90" align="center,center" vspace="0" hspace="0" marginwidth="0" marginheight="0" scrolling="no" frameborder="0" style="border:0;vertical-align:bottom;margin:0;width:760px;height:90px" allowtransparency="true"></iframe></div>
</div>

<div class="comments">
<a name="comments"></a>
</div>


<div class="newComment"><a name="newComment"></a>
<h3>发表评论</h3>
<form action="/entry/comment/add" method="POST">
    <input type="hidden" name="targetId" value="374349">
    <input type="hidden" name="title" value="回复:InnoDB 存储引擎之索引和优化">
    <input type="hidden" name="replyId">
    <textarea name="content" cols="100" rows="6" class="mdInput" style="width:98%"></textarea>
    <p>
        <button type="button" id="btnComment">发表评论</button><span id="commentTip"></span>
    </p>
</form>
</div>




</div>