<div class="post">
<h1 class="postTitle"><a id="cb_post_title_url" class="postTitle2" href="https://www.cnblogs.com/daiwei1981/p/9411482.html">分布式搜索的面试题2</a></h1>
<div id="cnblogs_post_body" class="blogpost-body"><p>&nbsp;</p>
<p>1<span style="font-family: 宋体;">、面试题</span></p>
<p align="justify">&nbsp;</p>
<p align="justify">es<span style="font-family: 宋体;">写入数据的工作原理是什么啊？</span>es查询数据的工作原理是什么啊？</p>
<p align="justify">&nbsp;</p>
<p align="justify">2<span style="font-family: 宋体;">、面试官心理分析</span></p>
<p align="justify">&nbsp;</p>
<p align="justify"><span style="font-family: 宋体;">问这个，其实面试官就是要看看你了解不了解</span>es<span style="font-family: 宋体;">的一些基本原理，因为用</span><span style="font-family: Calibri;">es</span><span style="font-family: 宋体;">无非就是写入数据，搜索数据。你要是不明白你发起一个写入和搜索请求的时候，</span><span style="font-family: Calibri;">es</span><span style="font-family: 宋体;">在干什么，那你真的就是。。。。</span></p>
<p align="justify">&nbsp;</p>
<p align="justify"><span style="font-family: 宋体;">对</span>es<span style="font-family: 宋体;">基本就是个黑盒，你还能干啥？你唯一能干的就是用</span><span style="font-family: Calibri;">es</span><span style="font-family: 宋体;">的</span><span style="font-family: Calibri;">api</span><span style="font-family: 宋体;">读写数据了。。。要是出点什么问题，你啥都不知道，那还能指望你什么呢？是不是。。</span></p>
<p align="justify">&nbsp;</p>
<p align="justify">3<span style="font-family: 宋体;">、面试题剖析</span></p>
<p align="justify">&nbsp;</p>
<p align="justify"><span style="font-family: 宋体;">（</span>1<span style="font-family: 宋体;">）</span><span style="font-family: Calibri;">es</span><span style="font-family: 宋体;">写数据过程</span></p>
<p align="justify">&nbsp;</p>
<p align="justify">1<span style="font-family: 宋体;">）客户端选择一个</span><span style="font-family: Calibri;">node</span><span style="font-family: 宋体;">发送请求过去，这个</span><span style="font-family: Calibri;">node</span><span style="font-family: 宋体;">就是</span><span style="font-family: Calibri;">coordinating node</span><span style="font-family: 宋体;">（协调节点）</span></p>
<p align="justify">2<span style="font-family: 宋体;">）</span><span style="font-family: Calibri;">coordinating node</span><span style="font-family: 宋体;">，对</span><span style="font-family: Calibri;">document</span><span style="font-family: 宋体;">进行路由，将请求转发给对应的</span><span style="font-family: Calibri;">node</span><span style="font-family: 宋体;">（有</span><span style="font-family: Calibri;">primary shard</span><span style="font-family: 宋体;">）</span></p>
<p align="justify">3<span style="font-family: 宋体;">）实际的</span><span style="font-family: Calibri;">node</span><span style="font-family: 宋体;">上的</span><span style="font-family: Calibri;">primary shard</span><span style="font-family: 宋体;">处理请求，然后将数据同步到</span><span style="font-family: Calibri;">replica node</span></p>
<p align="justify">4<span style="font-family: 宋体;">）</span><span style="font-family: Calibri;">coordinating node</span><span style="font-family: 宋体;">，如果发现</span><span style="font-family: Calibri;">primary node</span><span style="font-family: 宋体;">和所有</span><span style="font-family: Calibri;">replica node</span><span style="font-family: 宋体;">都搞定之后，就返回响应结果给客户端</span></p>
<p align="justify">&nbsp;</p>
<p align="justify"><span style="font-family: 宋体;">（</span>2<span style="font-family: 宋体;">）</span><span style="font-family: Calibri;">es</span><span style="font-family: 宋体;">读数据过程</span></p>
<p align="justify">&nbsp;</p>
<p align="justify"><span style="font-family: 宋体;">查询，</span>GET<span style="font-family: 宋体;">某一条数据，写入了某个</span><span style="font-family: Calibri;">document</span><span style="font-family: 宋体;">，这个</span><span style="font-family: Calibri;">document</span><span style="font-family: 宋体;">会自动给你分配一个全局唯一的</span><span style="font-family: Calibri;">id</span><span style="font-family: 宋体;">，</span><span style="font-family: Calibri;">doc id</span><span style="font-family: 宋体;">，同时也是根据</span><span style="font-family: Calibri;">doc id</span><span style="font-family: 宋体;">进行</span><span style="font-family: Calibri;">hash</span><span style="font-family: 宋体;">路由到对应的</span><span style="font-family: Calibri;">primary shard</span><span style="font-family: 宋体;">上面去。也可以手动指定</span><span style="font-family: Calibri;">doc id</span><span style="font-family: 宋体;">，比如用订单</span><span style="font-family: Calibri;">id</span><span style="font-family: 宋体;">，用户</span><span style="font-family: Calibri;">id</span><span style="font-family: 宋体;">。</span></p>
<p align="justify">&nbsp;</p>
<p align="justify"><span style="font-family: 宋体;">你可以通过</span>doc id<span style="font-family: 宋体;">来查询，会根据</span><span style="font-family: Calibri;">doc id</span><span style="font-family: 宋体;">进行</span><span style="font-family: Calibri;">hash</span><span style="font-family: 宋体;">，判断出来当时把</span><span style="font-family: Calibri;">doc id</span><span style="font-family: 宋体;">分配到了哪个</span><span style="font-family: Calibri;">shard</span><span style="font-family: 宋体;">上面去，从那个</span><span style="font-family: Calibri;">shard</span><span style="font-family: 宋体;">去查询</span></p>
<p align="justify">&nbsp;</p>
<p align="justify">1<span style="font-family: 宋体;">）客户端发送请求到任意一个</span><span style="font-family: Calibri;">node</span><span style="font-family: 宋体;">，成为</span><span style="font-family: Calibri;">coordinate node</span></p>
<p align="justify">2<span style="font-family: 宋体;">）</span><span style="font-family: Calibri;">coordinate node</span><span style="font-family: 宋体;">对</span><span style="font-family: Calibri;">document</span><span style="font-family: 宋体;">进行路由，将请求转发到对应的</span><span style="font-family: Calibri;">node</span><span style="font-family: 宋体;">，此时会使用</span><span style="font-family: Calibri;">round-robin</span><span style="font-family: 宋体;">随机轮询算法，在</span><span style="font-family: Calibri;">primary shard</span><span style="font-family: 宋体;">以及其所有</span><span style="font-family: Calibri;">replica</span><span style="font-family: 宋体;">中随机选择一个，让读请求负载均衡</span></p>
<p align="justify">3<span style="font-family: 宋体;">）接收请求的</span><span style="font-family: Calibri;">node</span><span style="font-family: 宋体;">返回</span><span style="font-family: Calibri;">document</span><span style="font-family: 宋体;">给</span><span style="font-family: Calibri;">coordinate node</span></p>
<p align="justify">4<span style="font-family: 宋体;">）</span><span style="font-family: Calibri;">coordinate node</span><span style="font-family: 宋体;">返回</span><span style="font-family: Calibri;">document</span><span style="font-family: 宋体;">给客户端</span></p>
<p align="justify">&nbsp;</p>
<p align="justify"><span style="font-family: 宋体;">（</span>3<span style="font-family: 宋体;">）</span><span style="font-family: Calibri;">es</span><span style="font-family: 宋体;">搜索数据过程</span></p>
<p align="justify">&nbsp;</p>
<p align="justify">es<span style="font-family: 宋体;">最强大的是做全文检索，就是比如你有三条数据</span></p>
<p align="justify">&nbsp;</p>
<p align="justify">java<span style="font-family: 宋体;">真好玩儿啊</span></p>
<p align="justify">java<span style="font-family: 宋体;">好难学啊</span></p>
<p align="justify">j2ee<span style="font-family: 宋体;">特别牛</span></p>
<p align="justify">&nbsp;</p>
<p align="justify"><span style="font-family: 宋体;">你根据</span>java<span style="font-family: 宋体;">关键词来搜索，将包含</span><span style="font-family: Calibri;">java</span><span style="font-family: 宋体;">的</span><span style="font-family: Calibri;">document</span><span style="font-family: 宋体;">给搜索出来</span></p>
<p align="justify">&nbsp;</p>
<p align="justify">es<span style="font-family: 宋体;">就会给你返回：</span><span style="font-family: Calibri;">java</span><span style="font-family: 宋体;">真好玩儿啊，</span><span style="font-family: Calibri;">java</span><span style="font-family: 宋体;">好难学啊</span></p>
<p align="justify">&nbsp;</p>
<p align="justify">1<span style="font-family: 宋体;">）客户端发送请求到一个</span><span style="font-family: Calibri;">coordinate node</span></p>
<p align="justify">2<span style="font-family: 宋体;">）协调节点将搜索请求转发到所有的</span><span style="font-family: Calibri;">shard</span><span style="font-family: 宋体;">对应的</span><span style="font-family: Calibri;">primary shard</span><span style="font-family: 宋体;">或</span><span style="font-family: Calibri;">replica shard</span><span style="font-family: 宋体;">也可以</span></p>
<p align="justify">3<span style="font-family: 宋体;">）</span><span style="font-family: Calibri;">query phase</span><span style="font-family: 宋体;">：每个</span><span style="font-family: Calibri;">shard</span><span style="font-family: 宋体;">将自己的搜索结果（其实就是一些</span><span style="font-family: Calibri;">doc id</span><span style="font-family: 宋体;">），返回给协调节点，由协调节点进行数据的合并、排序、分页等操作，产出最终结果</span></p>
<p align="justify">4<span style="font-family: 宋体;">）</span><span style="font-family: Calibri;">fetch phase</span><span style="font-family: 宋体;">：接着由协调节点，根据</span><span style="font-family: Calibri;">doc id</span><span style="font-family: 宋体;">去各个节点上拉取实际的</span><span style="font-family: Calibri;">document</span><span style="font-family: 宋体;">数据，最终返回给客户端</span></p>
<p align="justify">&nbsp;</p>
<p align="justify"><span style="font-family: 宋体;">（</span>4<span style="font-family: 宋体;">）搜索的底层原理，倒排索引，画图说明传统数据库和倒排索引的区别</span></p>
<p align="justify">&nbsp;</p>
<p align="justify"><span style="font-family: 宋体;">（</span>5<span style="font-family: 宋体;">）写数据底层原理</span></p>
<p align="justify">&nbsp;</p>
<p align="justify">1<span style="font-family: 宋体;">）先写入</span><span style="font-family: Calibri;">buffer</span><span style="font-family: 宋体;">，在</span><span style="font-family: Calibri;">buffer</span><span style="font-family: 宋体;">里的时候数据是搜索不到的；同时将数据写入</span><span style="font-family: Calibri;">translog</span><span style="font-family: 宋体;">日志文件</span></p>
<p align="justify">&nbsp;</p>
<p align="justify">2<span style="font-family: 宋体;">）如果</span><span style="font-family: Calibri;">buffer</span><span style="font-family: 宋体;">快满了，或者到一定时间，就会将</span><span style="font-family: Calibri;">buffer</span><span style="font-family: 宋体;">数据</span><span style="font-family: Calibri;">refresh</span><span style="font-family: 宋体;">到一个新的</span><span style="font-family: Calibri;">segment file</span><span style="font-family: 宋体;">中，但是此时数据不是直接进入</span><span style="font-family: Calibri;">segment file</span><span style="font-family: 宋体;">的磁盘文件的，而是先进入</span><span style="font-family: Calibri;">os cache</span><span style="font-family: 宋体;">的。这个过程就是</span><span style="font-family: Calibri;">refresh</span><span style="font-family: 宋体;">。</span></p>
<p align="justify">&nbsp;</p>
<p align="justify"><span style="font-family: 宋体;">每隔</span>1<span style="font-family: 宋体;">秒钟，</span><span style="font-family: Calibri;">es</span><span style="font-family: 宋体;">将</span><span style="font-family: Calibri;">buffer</span><span style="font-family: 宋体;">中的数据写入一个新的</span><span style="font-family: Calibri;">segment file</span><span style="font-family: 宋体;">，每秒钟会产生一个新的磁盘文件，</span><span style="font-family: Calibri;">segment file</span><span style="font-family: 宋体;">，这个</span><span style="font-family: Calibri;">segment file</span><span style="font-family: 宋体;">中就存储最近</span><span style="font-family: Calibri;">1</span><span style="font-family: 宋体;">秒内</span><span style="font-family: Calibri;">buffer</span><span style="font-family: 宋体;">中写入的数据</span></p>
<p align="justify">&nbsp;</p>
<p align="justify"><span style="font-family: 宋体;">但是如果</span>buffer<span style="font-family: 宋体;">里面此时没有数据，那当然不会执行</span><span style="font-family: Calibri;">refresh</span><span style="font-family: 宋体;">操作咯，每秒创建换一个空的</span><span style="font-family: Calibri;">segment file</span><span style="font-family: 宋体;">，如果</span><span style="font-family: Calibri;">buffer</span><span style="font-family: 宋体;">里面有数据，默认</span><span style="font-family: Calibri;">1</span><span style="font-family: 宋体;">秒钟执行一次</span><span style="font-family: Calibri;">refresh</span><span style="font-family: 宋体;">操作，刷入一个新的</span><span style="font-family: Calibri;">segment file</span><span style="font-family: 宋体;">中</span></p>
<p align="justify">&nbsp;</p>
<p align="justify"><span style="font-family: 宋体;">操作系统里面，磁盘文件其实都有一个东西，叫做</span>os cache<span style="font-family: 宋体;">，操作系统缓存，就是说数据写入磁盘文件之前，会先进入</span><span style="font-family: Calibri;">os cache</span><span style="font-family: 宋体;">，先进入操作系统级别的一个内存缓存中去</span></p>
<p align="justify">&nbsp;</p>
<p align="justify"><span style="font-family: 宋体;">只要</span>buffer<span style="font-family: 宋体;">中的数据被</span><span style="font-family: Calibri;">refresh</span><span style="font-family: 宋体;">操作，刷入</span><span style="font-family: Calibri;">os cache</span><span style="font-family: 宋体;">中，就代表这个数据就可以被搜索到了</span></p>
<p align="justify">&nbsp;</p>
<p align="justify"><span style="font-family: 宋体;">为什么叫</span>es<span style="font-family: 宋体;">是准实时的？</span><span style="font-family: Calibri;">NRT</span><span style="font-family: 宋体;">，</span><span style="font-family: Calibri;">near real-time</span><span style="font-family: 宋体;">，准实时。默认是每隔</span><span style="font-family: Calibri;">1</span><span style="font-family: 宋体;">秒</span><span style="font-family: Calibri;">refresh</span><span style="font-family: 宋体;">一次的，所以</span><span style="font-family: Calibri;">es</span><span style="font-family: 宋体;">是准实时的，因为写入的数据</span><span style="font-family: Calibri;">1</span><span style="font-family: 宋体;">秒之后才能被看到。</span></p>
<p align="justify">&nbsp;</p>
<p align="justify"><span style="font-family: 宋体;">可以通过</span>es<span style="font-family: 宋体;">的</span><span style="font-family: Calibri;">restful api</span><span style="font-family: 宋体;">或者</span><span style="font-family: Calibri;">java api</span><span style="font-family: 宋体;">，手动执行一次</span><span style="font-family: Calibri;">refresh</span><span style="font-family: 宋体;">操作，就是手动将</span><span style="font-family: Calibri;">buffer</span><span style="font-family: 宋体;">中的数据刷入</span><span style="font-family: Calibri;">os cache</span><span style="font-family: 宋体;">中，让数据立马就可以被搜索到。</span></p>
<p align="justify">&nbsp;</p>
<p align="justify"><span style="font-family: 宋体;">只要数据被输入</span>os cache<span style="font-family: 宋体;">中，</span><span style="font-family: Calibri;">buffer</span><span style="font-family: 宋体;">就会被清空了，因为不需要保留</span><span style="font-family: Calibri;">buffer</span><span style="font-family: 宋体;">了，数据在</span><span style="font-family: Calibri;">translog</span><span style="font-family: 宋体;">里面已经持久化到磁盘去一份了</span></p>
<p align="justify">&nbsp;</p>
<p align="justify">3<span style="font-family: 宋体;">）只要数据进入</span><span style="font-family: Calibri;">os cache</span><span style="font-family: 宋体;">，此时就可以让这个</span><span style="font-family: Calibri;">segment file</span><span style="font-family: 宋体;">的数据对外提供搜索了</span></p>
<p align="justify">&nbsp;</p>
<p align="justify">4<span style="font-family: 宋体;">）重复</span><span style="font-family: Calibri;">1~3</span><span style="font-family: 宋体;">步骤，新的数据不断进入</span><span style="font-family: Calibri;">buffer</span><span style="font-family: 宋体;">和</span><span style="font-family: Calibri;">translog</span><span style="font-family: 宋体;">，不断将</span><span style="font-family: Calibri;">buffer</span><span style="font-family: 宋体;">数据写入一个又一个新的</span><span style="font-family: Calibri;">segment file</span><span style="font-family: 宋体;">中去，每次</span><span style="font-family: Calibri;">refresh</span><span style="font-family: 宋体;">完</span><span style="font-family: Calibri;">buffer</span><span style="font-family: 宋体;">清空，</span><span style="font-family: Calibri;">translog</span><span style="font-family: 宋体;">保留。随着这个过程推进，</span><span style="font-family: Calibri;">translog</span><span style="font-family: 宋体;">会变得越来越大。当</span><span style="font-family: Calibri;">translog</span><span style="font-family: 宋体;">达到一定长度的时候，就会触发</span><span style="font-family: Calibri;">commit</span><span style="font-family: 宋体;">操作。</span></p>
<p align="justify">&nbsp;</p>
<p align="justify">buffer<span style="font-family: 宋体;">中的数据，倒是好，每隔</span><span style="font-family: Calibri;">1</span><span style="font-family: 宋体;">秒就被刷到</span><span style="font-family: Calibri;">os cache</span><span style="font-family: 宋体;">中去，然后这个</span><span style="font-family: Calibri;">buffer</span><span style="font-family: 宋体;">就被清空了。所以说这个</span><span style="font-family: Calibri;">buffer</span><span style="font-family: 宋体;">的数据始终是可以保持住不会填满</span><span style="font-family: Calibri;">es</span><span style="font-family: 宋体;">进程的内存的。</span></p>
<p align="justify">&nbsp;</p>
<p align="justify"><span style="font-family: 宋体;">每次一条数据写入</span>buffer<span style="font-family: 宋体;">，同时会写入一条日志到</span><span style="font-family: Calibri;">translog</span><span style="font-family: 宋体;">日志文件中去，所以这个</span><span style="font-family: Calibri;">translog</span><span style="font-family: 宋体;">日志文件是不断变大的，当</span><span style="font-family: Calibri;">translog</span><span style="font-family: 宋体;">日志文件大到一定程度的时候，就会执行</span><span style="font-family: Calibri;">commit</span><span style="font-family: 宋体;">操作。</span></p>
<p align="justify">&nbsp;</p>
<p align="justify">5<span style="font-family: 宋体;">）</span><span style="font-family: Calibri;">commit</span><span style="font-family: 宋体;">操作发生第一步，就是将</span><span style="font-family: Calibri;">buffer</span><span style="font-family: 宋体;">中现有数据</span><span style="font-family: Calibri;">refresh</span><span style="font-family: 宋体;">到</span><span style="font-family: Calibri;">os cache</span><span style="font-family: 宋体;">中去，清空</span><span style="font-family: Calibri;">buffer</span></p>
<p align="justify">&nbsp;</p>
<p align="justify">6<span style="font-family: 宋体;">）将一个</span><span style="font-family: Calibri;">commit point</span><span style="font-family: 宋体;">写入磁盘文件，里面标识着这个</span><span style="font-family: Calibri;">commit point</span><span style="font-family: 宋体;">对应的所有</span><span style="font-family: Calibri;">segment file</span></p>
<p align="justify">&nbsp;</p>
<p align="justify">7<span style="font-family: 宋体;">）强行将</span><span style="font-family: Calibri;">os cache</span><span style="font-family: 宋体;">中目前所有的数据都</span><span style="font-family: Calibri;">fsync</span><span style="font-family: 宋体;">到磁盘文件中去</span></p>
<p align="justify">&nbsp;</p>
<p align="justify">translog<span style="font-family: 宋体;">日志文件的作用是什么？就是在你执行</span><span style="font-family: Calibri;">commit</span><span style="font-family: 宋体;">操作之前，数据要么是停留在</span><span style="font-family: Calibri;">buffer</span><span style="font-family: 宋体;">中，要么是停留在</span><span style="font-family: Calibri;">os cache</span><span style="font-family: 宋体;">中，无论是</span><span style="font-family: Calibri;">buffer</span><span style="font-family: 宋体;">还是</span><span style="font-family: Calibri;">os cache</span><span style="font-family: 宋体;">都是内存，一旦这台机器死了，内存中的数据就全丢了。</span></p>
<p align="justify">&nbsp;</p>
<p align="justify"><span style="font-family: 宋体;">所以需要将数据对应的操作写入一个专门的日志文件，</span>translog<span style="font-family: 宋体;">日志文件中，一旦此时机器宕机，再次重启的时候，</span><span style="font-family: Calibri;">es</span><span style="font-family: 宋体;">会自动读取</span><span style="font-family: Calibri;">translog</span><span style="font-family: 宋体;">日志文件中的数据，恢复到内存</span><span style="font-family: Calibri;">buffer</span><span style="font-family: 宋体;">和</span><span style="font-family: Calibri;">os cache</span><span style="font-family: 宋体;">中去。</span></p>
<p align="justify">&nbsp;</p>
<p align="justify">commit<span style="font-family: 宋体;">操作：</span><span style="font-family: Calibri;">1</span><span style="font-family: 宋体;">、写</span><span style="font-family: Calibri;">commit point</span><span style="font-family: 宋体;">；</span><span style="font-family: Calibri;">2</span><span style="font-family: 宋体;">、将</span><span style="font-family: Calibri;">os cache</span><span style="font-family: 宋体;">数据</span><span style="font-family: Calibri;">fsync</span><span style="font-family: 宋体;">强刷到磁盘上去；</span><span style="font-family: Calibri;">3</span><span style="font-family: 宋体;">、清空</span><span style="font-family: Calibri;">translog</span><span style="font-family: 宋体;">日志文件</span></p>
<p align="justify">&nbsp;</p>
<p align="justify">8<span style="font-family: 宋体;">）将现有的</span><span style="font-family: Calibri;">translog</span><span style="font-family: 宋体;">清空，然后再次重启启用一个</span><span style="font-family: Calibri;">translog</span><span style="font-family: 宋体;">，此时</span><span style="font-family: Calibri;">commit</span><span style="font-family: 宋体;">操作完成。默认每隔</span><span style="font-family: Calibri;">30</span><span style="font-family: 宋体;">分钟会自动执行一次</span><span style="font-family: Calibri;">commit</span><span style="font-family: 宋体;">，但是如果</span><span style="font-family: Calibri;">translog</span><span style="font-family: 宋体;">过大，也会触发</span><span style="font-family: Calibri;">commit</span><span style="font-family: 宋体;">。整个</span><span style="font-family: Calibri;">commit</span><span style="font-family: 宋体;">的过程，叫做</span><span style="font-family: Calibri;">flush</span><span style="font-family: 宋体;">操作。我们可以手动执行</span><span style="font-family: Calibri;">flush</span><span style="font-family: 宋体;">操作，就是将所有</span><span style="font-family: Calibri;">os cache</span><span style="font-family: 宋体;">数据刷到磁盘文件中去。</span></p>
<p align="justify">&nbsp;</p>
<p align="justify"><span style="font-family: 宋体;">不叫做</span>commit<span style="font-family: 宋体;">操作，</span><span style="font-family: Calibri;">flush</span><span style="font-family: 宋体;">操作。</span><span style="font-family: Calibri;">es</span><span style="font-family: 宋体;">中的</span><span style="font-family: Calibri;">flush</span><span style="font-family: 宋体;">操作，就对应着</span><span style="font-family: Calibri;">commit</span><span style="font-family: 宋体;">的全过程。我们也可以通过</span><span style="font-family: Calibri;">es api</span><span style="font-family: 宋体;">，手动执行</span><span style="font-family: Calibri;">flush</span><span style="font-family: 宋体;">操作，手动将</span><span style="font-family: Calibri;">os cache</span><span style="font-family: 宋体;">中的数据</span><span style="font-family: Calibri;">fsync</span><span style="font-family: 宋体;">强刷到磁盘上去，记录一个</span><span style="font-family: Calibri;">commit point</span><span style="font-family: 宋体;">，清空</span><span style="font-family: Calibri;">translog</span><span style="font-family: 宋体;">日志文件。</span></p>
<p align="justify">&nbsp;</p>
<p align="justify">9<span style="font-family: 宋体;">）</span><span style="font-family: Calibri;">translog</span><span style="font-family: 宋体;">其实也是先写入</span><span style="font-family: Calibri;">os cache</span><span style="font-family: 宋体;">的，默认每隔</span><span style="font-family: Calibri;">5</span><span style="font-family: 宋体;">秒刷一次到磁盘中去，所以默认情况下，可能有</span><span style="font-family: Calibri;">5</span><span style="font-family: 宋体;">秒的数据会仅仅停留在</span><span style="font-family: Calibri;">buffer</span><span style="font-family: 宋体;">或者</span><span style="font-family: Calibri;">translog</span><span style="font-family: 宋体;">文件的</span><span style="font-family: Calibri;">os cache</span><span style="font-family: 宋体;">中，如果此时机器挂了，会丢失</span><span style="font-family: Calibri;">5</span><span style="font-family: 宋体;">秒钟的数据。但是这样性能比较好，最多丢</span><span style="font-family: Calibri;">5</span><span style="font-family: 宋体;">秒的数据。也可以将</span><span style="font-family: Calibri;">translog</span><span style="font-family: 宋体;">设置成每次写操作必须是直接</span><span style="font-family: Calibri;">fsync</span><span style="font-family: 宋体;">到磁盘，但是性能会差很多。</span></p>
<p align="justify">&nbsp;</p>
<p align="justify"><span style="font-family: 宋体;">实际上你在这里，如果面试官没有问你</span>es<span style="font-family: 宋体;">丢数据的问题，你可以在这里给面试官炫一把，你说，其实</span><span style="font-family: Calibri;">es</span><span style="font-family: 宋体;">第一是准实时的，数据写入</span><span style="font-family: Calibri;">1</span><span style="font-family: 宋体;">秒后可以搜索到；可能会丢失数据的，你的数据有</span><span style="font-family: Calibri;">5</span><span style="font-family: 宋体;">秒的数据，停留在</span><span style="font-family: Calibri;">buffer</span><span style="font-family: 宋体;">、</span><span style="font-family: Calibri;">translog os cache</span><span style="font-family: 宋体;">、</span><span style="font-family: Calibri;">segment file os cache</span><span style="font-family: 宋体;">中，有</span><span style="font-family: Calibri;">5</span><span style="font-family: 宋体;">秒的数据不在磁盘上，此时如果宕机，会导致</span><span style="font-family: Calibri;">5</span><span style="font-family: 宋体;">秒的数据丢失。</span></p>
<p align="justify">&nbsp;</p>
<p align="justify"><span style="font-family: 宋体;">如果你希望一定不能丢失数据的话，你可以设置个参数，官方文档，百度一下。每次写入一条数据，都是写入</span>buffer<span style="font-family: 宋体;">，同时写入磁盘上的</span><span style="font-family: Calibri;">translog</span><span style="font-family: 宋体;">，但是这会导致写性能、写入吞吐量会下降一个数量级。本来一秒钟可以写</span><span style="font-family: Calibri;">2000</span><span style="font-family: 宋体;">条，现在你一秒钟只能写</span><span style="font-family: Calibri;">200</span><span style="font-family: 宋体;">条，都有可能。</span></p>
<p align="justify">&nbsp;</p>
<p align="justify">10<span style="font-family: 宋体;">）如果是删除操作，</span><span style="font-family: Calibri;">commit</span><span style="font-family: 宋体;">的时候会生成一个</span><span style="font-family: Calibri;">.del</span><span style="font-family: 宋体;">文件，里面将某个</span><span style="font-family: Calibri;">doc</span><span style="font-family: 宋体;">标识为</span><span style="font-family: Calibri;">deleted</span><span style="font-family: 宋体;">状态，那么搜索的时候根据</span><span style="font-family: Calibri;">.del</span><span style="font-family: 宋体;">文件就知道这个</span><span style="font-family: Calibri;">doc</span><span style="font-family: 宋体;">被删除了</span></p>
<p align="justify">&nbsp;</p>
<p align="justify">11<span style="font-family: 宋体;">）如果是更新操作，就是将原来的</span><span style="font-family: Calibri;">doc</span><span style="font-family: 宋体;">标识为</span><span style="font-family: Calibri;">deleted</span><span style="font-family: 宋体;">状态，然后新写入一条数据</span></p>
<p align="justify">&nbsp;</p>
<p align="justify">12<span style="font-family: 宋体;">）</span><span style="font-family: Calibri;">buffer</span><span style="font-family: 宋体;">每次</span><span style="font-family: Calibri;">refresh</span><span style="font-family: 宋体;">一次，就会产生一个</span><span style="font-family: Calibri;">segment file</span><span style="font-family: 宋体;">，所以默认情况下是</span><span style="font-family: Calibri;">1</span><span style="font-family: 宋体;">秒钟一个</span><span style="font-family: Calibri;">segment file</span><span style="font-family: 宋体;">，</span><span style="font-family: Calibri;">segment file</span><span style="font-family: 宋体;">会越来越多，此时会定期执行</span><span style="font-family: Calibri;">merge</span></p>
<p align="justify">&nbsp;</p>
<p align="justify">13<span style="font-family: 宋体;">）每次</span><span style="font-family: Calibri;">merge</span><span style="font-family: 宋体;">的时候，会将多个</span><span style="font-family: Calibri;">segment file</span><span style="font-family: 宋体;">合并成一个，同时这里会将标识为</span><span style="font-family: Calibri;">deleted</span><span style="font-family: 宋体;">的</span><span style="font-family: Calibri;">doc</span><span style="font-family: 宋体;">给物理删除掉，然后将新的</span><span style="font-family: Calibri;">segment file</span><span style="font-family: 宋体;">写入磁盘，这里会写一个</span><span style="font-family: Calibri;">commit point</span><span style="font-family: 宋体;">，标识所有新的</span><span style="font-family: Calibri;">segment file</span><span style="font-family: 宋体;">，然后打开</span><span style="font-family: Calibri;">segment file</span><span style="font-family: 宋体;">供搜索使用，同时删除旧的</span><span style="font-family: Calibri;">segment file</span><span style="font-family: 宋体;">。</span></p>
<p align="justify">&nbsp;</p>
<p align="justify">es<span style="font-family: 宋体;">里的写流程，有</span><span style="font-family: Calibri;">4</span><span style="font-family: 宋体;">个底层的核心概念，</span><span style="font-family: Calibri;">refresh</span><span style="font-family: 宋体;">、</span><span style="font-family: Calibri;">flush</span><span style="font-family: 宋体;">、</span><span style="font-family: Calibri;">translog</span><span style="font-family: 宋体;">、</span><span style="font-family: Calibri;">merge</span></p>
<p align="justify">&nbsp;</p>
<p align="justify"><span style="font-family: 宋体;">当</span>segment file<span style="font-family: 宋体;">多到一定程度的时候，</span><span style="font-family: Calibri;">es</span><span style="font-family: 宋体;">就会自动触发</span><span style="font-family: Calibri;">merge</span><span style="font-family: 宋体;">操作，将多个</span><span style="font-family: Calibri;">segment file</span><span style="font-family: 宋体;">给</span><span style="font-family: Calibri;">merge</span><span style="font-family: 宋体;">成一个</span><span style="font-family: Calibri;">segment file</span><span style="font-family: 宋体;">。</span></p>
<p align="justify">&nbsp;</p>
<p align="justify"><span style="font-family: 宋体;"><img src="https://images2018.cnblogs.com/blog/918692/201808/918692-20180803090300717-1495345873.png" alt=""></span></p>
<p>&nbsp;</p></div><div id="MySignature"></div>
<div class="clear"></div>
<div id="blog_post_info_block">
<div id="BlogPostCategory"></div>
<div id="EntryTag"></div>
<div id="blog_post_info"><div id="green_channel">
<a href="javascript:void(0);" id="green_channel_digg" onclick="DiggIt(9411482,cb_blogId,1);green_channel_success(this,'谢谢推荐！');">好文要顶</a>
<a id="green_channel_follow" onclick="follow('fb39f011-47ef-e511-9fc1-ac853d9f53cc');" href="javascript:void(0);">关注我</a>
<a id="green_channel_favorite" onclick="AddToWz(cb_entryId);return false;" href="javascript:void(0);">收藏该文</a>
<a id="green_channel_weibo" href="javascript:void(0);" title="分享至新浪微博" onclick="ShareToTsina()"><img src="//common.cnblogs.com/images/icon_weibo_24.png" alt=""></a>
<a id="green_channel_wechat" href="javascript:void(0);" title="分享至微信" onclick="shareOnWechat()"><img src="//common.cnblogs.com/images/wechat.png" alt=""></a>
</div>
<div id="author_profile">
<div id="author_profile_info" class="author_profile_info">
<a href="https://home.cnblogs.com/u/daiwei1981/" target="_blank"><img src="//pic.cnblogs.com/face/918692/20160622171901.png" class="author_avatar" alt=""></a>
<div id="author_profile_detail" class="author_profile_info">
<a href="https://home.cnblogs.com/u/daiwei1981/">伪全栈的java工程师</a><br>
<a href="https://home.cnblogs.com/u/daiwei1981/followees">关注 - 25</a><br>
<a href="https://home.cnblogs.com/u/daiwei1981/followers">粉丝 - 67</a>
</div>
</div>
<div class="clear"></div>
<div id="author_profile_honor"></div>
<div id="author_profile_follow">
<a href="javascript:void(0);" onclick="follow('fb39f011-47ef-e511-9fc1-ac853d9f53cc');return false;">+加关注</a>
</div>
</div>
<div id="div_digg">
<div class="diggit" onclick="votePost(9411482,'Digg')">
<span class="diggnum" id="digg_count">0</span>
</div>
<div class="buryit" onclick="votePost(9411482,'Bury')">
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
<div id="post_next_prev"><a href="https://www.cnblogs.com/daiwei1981/p/9403970.html" class="p_n_p_prefix">« </a> 上一篇：<a href="https://www.cnblogs.com/daiwei1981/p/9403970.html" title="发布于2018-08-01 21:17">分布式搜索的面试题1</a><br><a href="https://www.cnblogs.com/daiwei1981/p/9411495.html" class="p_n_p_prefix">» </a> 下一篇：<a href="https://www.cnblogs.com/daiwei1981/p/9411495.html" title="发布于2018-08-03 09:05">分布式搜索的面试题3</a><br></div>
</div>

<div class="postDesc">posted on <span id="post-date">2018-08-03 09:03</span> <a href="https://www.cnblogs.com/daiwei1981/">伪全栈的java工程师</a> 阅读(<span id="post_view_count">446</span>) 评论(<span id="post_comment_count">0</span>)  <a href="https://i.cnblogs.com/EditPosts.aspx?postid=9411482" rel="nofollow">编辑</a> <a href="#" onclick="AddToWz(9411482);return false;">收藏</a></div>
</div>
