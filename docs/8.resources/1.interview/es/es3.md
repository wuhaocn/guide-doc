<div class="post">
<h1 class="postTitle"><a id="cb_post_title_url" class="postTitle2" href="https://www.cnblogs.com/daiwei1981/p/9411495.html">分布式搜索的面试题3</a></h1>
<div id="cnblogs_post_body" class="blogpost-body"><p>&nbsp;</p>
<p>1<span style="font-family: 宋体;">、面试题</span></p>
<p>&nbsp;</p>
<p>es<span style="font-family: 宋体;">在数据量很大的情况下（数十亿级别）如何提高查询效率啊？</span></p>
<p>&nbsp;</p>
<p>2<span style="font-family: 宋体;">、面试官心里分析</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">问这个问题，是肯定的，说白了，就是看你有没有实际干过</span>es<span style="font-family: 宋体;">，因为啥？</span><span style="font-family: Calibri;">es</span><span style="font-family: 宋体;">说白了其实性能并没有你想象中那么好的。很多时候数据量大了，特别是有几亿条数据的时候，可能你会懵逼的发现，跑个搜索怎么一下</span><span style="font-family: Calibri;">5</span><span style="font-family: 宋体;">秒</span><span style="font-family: Calibri;">~10</span><span style="font-family: 宋体;">秒，坑爹了。第一次搜索的时候，是</span><span style="font-family: Calibri;">5~10</span><span style="font-family: 宋体;">秒，后面反而就快了，可能就几百毫秒。</span></p>
<p>&nbsp;</p>
<p>你就很懵，每个用户第一次访问都会比较慢，比较卡么？</p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">所以你要是没玩儿过</span>es<span style="font-family: 宋体;">，或者就是自己玩玩儿</span><span style="font-family: Calibri;">demo</span><span style="font-family: 宋体;">，被问到这个问题容易懵逼，显示出你对</span><span style="font-family: Calibri;">es</span><span style="font-family: 宋体;">确实玩儿的不怎么样</span></p>
<p>&nbsp;</p>
<p>3<span style="font-family: 宋体;">、面试题剖析</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">说实话，</span>es<span style="font-family: 宋体;">性能优化是没有什么银弹的，啥意思呢？就是不要期待着随手调一个参数，就可以万能的应对所有的性能慢的场景。也许有的场景是你换个参数，或者调整一下语法，就可以搞定，但是绝对不是所有场景都可以这样。</span></p>
<p>&nbsp;</p>
<p>一块一块来分析吧</p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">在这个海量数据的场景下，如何提升</span>es<span style="font-family: 宋体;">搜索的性能，也是我们之前生产环境实践经验所得</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">（</span>1<span style="font-family: 宋体;">）性能优化的杀手锏——</span><span style="font-family: Calibri;">filesystem cache</span></p>
<p>&nbsp;</p>
<p>os cache<span style="font-family: 宋体;">，操作系统的缓存</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">你往</span>es<span style="font-family: 宋体;">里写的数据，实际上都写到磁盘文件里去了，磁盘文件里的数据操作系统会自动将里面的数据缓存到</span><span style="font-family: Calibri;">os cache</span><span style="font-family: 宋体;">里面去</span></p>
<p>&nbsp;</p>
<p>es<span style="font-family: 宋体;">的搜索引擎严重依赖于底层的</span><span style="font-family: Calibri;">filesystem cache</span><span style="font-family: 宋体;">，你如果给</span><span style="font-family: Calibri;">filesystem cache</span><span style="font-family: 宋体;">更多的内存，尽量让内存可以容纳所有的</span><span style="font-family: Calibri;">indx segment file</span><span style="font-family: 宋体;">索引数据文件，那么你搜索的时候就基本都是走内存的，性能会非常高。</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">性能差距可以有大，我们之前很多的测试和压测，如果走磁盘一般肯定上秒，搜索性能绝对是秒级别的，</span>1<span style="font-family: 宋体;">秒，</span><span style="font-family: Calibri;">5</span><span style="font-family: 宋体;">秒，</span><span style="font-family: Calibri;">10</span><span style="font-family: 宋体;">秒。但是如果是走</span><span style="font-family: Calibri;">filesystem cache</span><span style="font-family: 宋体;">，是走纯内存的，那么一般来说性能比走磁盘要高一个数量级，基本上就是毫秒级的，从几毫秒到几百毫秒不等。</span></p>
<p>&nbsp;</p>
<p>之前有个学员，一直在问我，说他的搜索性能，聚合性能，倒排索引，正排索引，磁盘文件，十几秒。。。。</p>
<p>&nbsp;</p>
<p>学员的真实案例</p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">比如说，你，</span>es<span style="font-family: 宋体;">节点有</span><span style="font-family: Calibri;">3</span><span style="font-family: 宋体;">台机器，每台机器，看起来内存很多，</span><span style="font-family: Calibri;">64G</span><span style="font-family: 宋体;">，总内存，</span><span style="font-family: Calibri;">64 * 3 = 192g</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">每台机器给</span>es jvm heap<span style="font-family: 宋体;">是</span><span style="font-family: Calibri;">32G</span><span style="font-family: 宋体;">，那么剩下来留给</span><span style="font-family: Calibri;">filesystem cache</span><span style="font-family: 宋体;">的就是每台机器才</span><span style="font-family: Calibri;">32g</span><span style="font-family: 宋体;">，总共集群里给</span><span style="font-family: Calibri;">filesystem cache</span><span style="font-family: 宋体;">的就是</span><span style="font-family: Calibri;">32 * 3 = 96g</span><span style="font-family: 宋体;">内存</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">我就问他，</span>ok<span style="font-family: 宋体;">，那么就是你往</span><span style="font-family: Calibri;">es</span><span style="font-family: 宋体;">集群里写入的数据有多少数据量？</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">如果你此时，你整个，磁盘上索引数据文件，在</span>3<span style="font-family: 宋体;">台机器上，一共占用了</span><span style="font-family: Calibri;">1T</span><span style="font-family: 宋体;">的磁盘容量，你的</span><span style="font-family: Calibri;">es</span><span style="font-family: 宋体;">数据量是</span><span style="font-family: Calibri;">1t</span><span style="font-family: 宋体;">，每台机器的数据量是</span><span style="font-family: Calibri;">300g</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">你觉得你的性能能好吗？</span>filesystem cache<span style="font-family: 宋体;">的内存才</span><span style="font-family: Calibri;">100g</span><span style="font-family: 宋体;">，十分之一的数据可以放内存，其他的都在磁盘，然后你执行搜索操作，大部分操作都是走磁盘，性能肯定差</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">当时他们的情况就是这样子，</span>es<span style="font-family: 宋体;">在测试，弄了</span><span style="font-family: Calibri;">3</span><span style="font-family: 宋体;">台机器，自己觉得还不错，</span><span style="font-family: Calibri;">64G</span><span style="font-family: 宋体;">内存的物理机。自以为可以容纳</span><span style="font-family: Calibri;">1T</span><span style="font-family: 宋体;">的数据量。</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">归根结底，你要让</span>es<span style="font-family: 宋体;">性能要好，最佳的情况下，就是你的机器的内存，至少可以容纳你的总数据量的一半</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">比如说，你一共要在</span>es<span style="font-family: 宋体;">中存储</span><span style="font-family: Calibri;">1T</span><span style="font-family: 宋体;">的数据，那么你的多台机器留个</span><span style="font-family: Calibri;">filesystem cache</span><span style="font-family: 宋体;">的内存加起来综合，至少要到</span><span style="font-family: Calibri;">512G</span><span style="font-family: 宋体;">，至少半数的情况下，搜索是走内存的，性能一般可以到几秒钟，</span><span style="font-family: Calibri;">2</span><span style="font-family: 宋体;">秒，</span><span style="font-family: Calibri;">3</span><span style="font-family: 宋体;">秒，</span><span style="font-family: Calibri;">5</span><span style="font-family: 宋体;">秒</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">如果最佳的情况下，我们自己的生产环境实践经验，所以说我们当时的策略，是仅仅在</span>es<span style="font-family: 宋体;">中就存少量的数据，就是你要用来搜索的那些索引，内存留给</span><span style="font-family: Calibri;">filesystem cache</span><span style="font-family: 宋体;">的，就</span><span style="font-family: Calibri;">100G</span><span style="font-family: 宋体;">，那么你就控制在</span><span style="font-family: Calibri;">100gb</span><span style="font-family: 宋体;">以内，相当于是，你的数据几乎全部走内存来搜索，性能非常之高，一般可以在</span><span style="font-family: Calibri;">1</span><span style="font-family: 宋体;">秒以内</span></p>
<p>&nbsp;</p>
<p>比如说你现在有一行数据</p>
<p>&nbsp;</p>
<p>id name age ....30<span style="font-family: 宋体;">个字段</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">但是你现在搜索，只需要根据</span>id name age<span style="font-family: 宋体;">三个字段来搜索</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">如果你傻乎乎的往</span>es<span style="font-family: 宋体;">里写入一行数据所有的字段，就会导致说</span><span style="font-family: Calibri;">70%</span><span style="font-family: 宋体;">的数据是不用来搜索的，结果硬是占据了</span><span style="font-family: Calibri;">es</span><span style="font-family: 宋体;">机器上的</span><span style="font-family: Calibri;">filesystem cache</span><span style="font-family: 宋体;">的空间，单挑数据的数据量越大，就会导致</span><span style="font-family: Calibri;">filesystem cahce</span><span style="font-family: 宋体;">能缓存的数据就越少</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">仅仅只是写入</span>es<span style="font-family: 宋体;">中要用来检索的少数几个字段就可以了，比如说，就写入</span><span style="font-family: Calibri;">es id name age</span><span style="font-family: 宋体;">三个字段就可以了，然后你可以把其他的字段数据存在</span><span style="font-family: Calibri;">mysql</span><span style="font-family: 宋体;">里面，我们一般是建议用</span><span style="font-family: Calibri;">es + hbase</span><span style="font-family: 宋体;">的这么一个架构。</span></p>
<p>&nbsp;</p>
<p>hbase<span style="font-family: 宋体;">的特点是适用于海量数据的在线存储，就是对</span><span style="font-family: Calibri;">hbase</span><span style="font-family: 宋体;">可以写入海量数据，不要做复杂的搜索，就是做很简单的一些根据</span><span style="font-family: Calibri;">id</span><span style="font-family: 宋体;">或者范围进行查询的这么一个操作就可以了</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">从</span>es<span style="font-family: 宋体;">中根据</span><span style="font-family: Calibri;">name</span><span style="font-family: 宋体;">和</span><span style="font-family: Calibri;">age</span><span style="font-family: 宋体;">去搜索，拿到的结果可能就</span><span style="font-family: Calibri;">20</span><span style="font-family: 宋体;">个</span><span style="font-family: Calibri;">doc id</span><span style="font-family: 宋体;">，然后根据</span><span style="font-family: Calibri;">doc id</span><span style="font-family: 宋体;">到</span><span style="font-family: Calibri;">hbase</span><span style="font-family: 宋体;">里去查询每个</span><span style="font-family: Calibri;">doc id</span><span style="font-family: 宋体;">对应的完整的数据，给查出来，再返回给前端。</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">你最好是写入</span>es<span style="font-family: 宋体;">的数据小于等于，或者是略微大于</span><span style="font-family: Calibri;">es</span><span style="font-family: 宋体;">的</span><span style="font-family: Calibri;">filesystem cache</span><span style="font-family: 宋体;">的内存容量</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">然后你从</span>es<span style="font-family: 宋体;">检索可能就花费</span><span style="font-family: Calibri;">20ms</span><span style="font-family: 宋体;">，然后再根据</span><span style="font-family: Calibri;">es</span><span style="font-family: 宋体;">返回的</span><span style="font-family: Calibri;">id</span><span style="font-family: 宋体;">去</span><span style="font-family: Calibri;">hbase</span><span style="font-family: 宋体;">里查询，查</span><span style="font-family: Calibri;">20</span><span style="font-family: 宋体;">条数据，可能也就耗费个</span><span style="font-family: Calibri;">30ms</span><span style="font-family: 宋体;">，可能你原来那么玩儿，</span><span style="font-family: Calibri;">1T</span><span style="font-family: 宋体;">数据都放</span><span style="font-family: Calibri;">es</span><span style="font-family: 宋体;">，会每次查询都是</span><span style="font-family: Calibri;">5~10</span><span style="font-family: 宋体;">秒，现在可能性能就会很高，每次查询就是</span><span style="font-family: Calibri;">50ms</span><span style="font-family: 宋体;">。</span></p>
<p>&nbsp;</p>
<p>elastcisearch<span style="font-family: 宋体;">减少数据量仅仅放要用于搜索的几个关键字段即可，尽量写入</span><span style="font-family: Calibri;">es</span><span style="font-family: 宋体;">的数据量跟</span><span style="font-family: Calibri;">es</span><span style="font-family: 宋体;">机器的</span><span style="font-family: Calibri;">filesystem cache</span><span style="font-family: 宋体;">是差不多的就可以了；其他不用来检索的数据放</span><span style="font-family: Calibri;">hbase</span><span style="font-family: 宋体;">里，或者</span><span style="font-family: Calibri;">mysql</span><span style="font-family: 宋体;">。</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">所以之前有些学员也是问，我也是跟他们说，尽量在</span>es<span style="font-family: 宋体;">里，就存储必须用来搜索的数据，比如说你现在有一份数据，有</span><span style="font-family: Calibri;">100</span><span style="font-family: 宋体;">个字段，其实用来搜索的只有</span><span style="font-family: Calibri;">10</span><span style="font-family: 宋体;">个字段，建议是将</span><span style="font-family: Calibri;">10</span><span style="font-family: 宋体;">个字段的数据，存入</span><span style="font-family: Calibri;">es</span><span style="font-family: 宋体;">，剩下</span><span style="font-family: Calibri;">90</span><span style="font-family: 宋体;">个字段的数据，可以放</span><span style="font-family: Calibri;">mysql</span><span style="font-family: 宋体;">，</span><span style="font-family: Calibri;">hadoop hbase</span><span style="font-family: 宋体;">，都可以</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">这样的话，</span>es<span style="font-family: 宋体;">数据量很少，</span><span style="font-family: Calibri;">10</span><span style="font-family: 宋体;">个字段的数据，都可以放内存，就用来搜索，搜索出来一些</span><span style="font-family: Calibri;">id</span><span style="font-family: 宋体;">，通过</span><span style="font-family: Calibri;">id</span><span style="font-family: 宋体;">去</span><span style="font-family: Calibri;">mysql</span><span style="font-family: 宋体;">，</span><span style="font-family: Calibri;">hbase</span><span style="font-family: 宋体;">里面去查询明细的数据</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">（</span>2<span style="font-family: 宋体;">）数据预热</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">假如说，哪怕是你就按照上述的方案去做了，</span>es<span style="font-family: 宋体;">集群中每个机器写入的数据量还是超过了</span><span style="font-family: Calibri;">filesystem cache</span><span style="font-family: 宋体;">一倍，比如说你写入一台机器</span><span style="font-family: Calibri;">60g</span><span style="font-family: 宋体;">数据，结果</span><span style="font-family: Calibri;">filesystem cache</span><span style="font-family: 宋体;">就</span><span style="font-family: Calibri;">30g</span><span style="font-family: 宋体;">，还是有</span><span style="font-family: Calibri;">30g</span><span style="font-family: 宋体;">数据留在了磁盘上。</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">举个例子，就比如说，微博，你可以把一些大</span>v<span style="font-family: 宋体;">，平时看的人很多的数据给提前你自己后台搞个系统，每隔一会儿，你自己的后台系统去搜索一下热数据，刷到</span><span style="font-family: Calibri;">filesystem cache</span><span style="font-family: 宋体;">里去，后面用户实际上来看这个热数据的时候，他们就是直接从内存里搜索了，很快。</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">电商，你可以将平时查看最多的一些商品，比如说</span>iphone 8<span style="font-family: 宋体;">，热数据提前后台搞个程序，每隔</span><span style="font-family: Calibri;">1</span><span style="font-family: 宋体;">分钟自己主动访问一次，刷到</span><span style="font-family: Calibri;">filesystem cache</span><span style="font-family: 宋体;">里去。</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">对于那些你觉得比较热的，经常会有人访问的数据，最好做一个专门的缓存预热子系统，就是对热数据，每隔一段时间，你就提前访问一下，让数据进入</span>filesystem cache<span style="font-family: 宋体;">里面去。这样期待下次别人访问的时候，一定性能会好一些。</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">（</span>3<span style="font-family: 宋体;">）冷热分离</span></p>
<p>&nbsp;</p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">关于</span>es<span style="font-family: 宋体;">性能优化，数据拆分，我之前说将大量不搜索的字段，拆分到别的存储中去，这个就是类似于后面我最后要讲的</span><span style="font-family: Calibri;">mysql</span><span style="font-family: 宋体;">分库分表的垂直拆分。</span></p>
<p>&nbsp;</p>
<p>es<span style="font-family: 宋体;">可以做类似于</span><span style="font-family: Calibri;">mysql</span><span style="font-family: 宋体;">的水平拆分，就是说将大量的访问很少，频率很低的数据，单独写一个索引，然后将访问很频繁的热数据单独写一个索引</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">你最好是将冷数据写入一个索引中，然后热数据写入另外一个索引中，这样可以确保热数据在被预热之后，尽量都让他们留在</span>filesystem os cache<span style="font-family: 宋体;">里，别让冷数据给冲刷掉。</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">你看，假设你有</span>6<span style="font-family: 宋体;">台机器，</span><span style="font-family: Calibri;">2</span><span style="font-family: 宋体;">个索引，一个放冷数据，一个放热数据，每个索引</span><span style="font-family: Calibri;">3</span><span style="font-family: 宋体;">个</span><span style="font-family: Calibri;">shard</span></p>
<p>&nbsp;</p>
<p>3<span style="font-family: 宋体;">台机器放热数据</span><span style="font-family: Calibri;">index</span><span style="font-family: 宋体;">；另外</span><span style="font-family: Calibri;">3</span><span style="font-family: 宋体;">台机器放冷数据</span><span style="font-family: Calibri;">index</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">然后这样的话，你大量的时候是在访问热数据</span>index<span style="font-family: 宋体;">，热数据可能就占总数据量的</span><span style="font-family: Calibri;">10%</span><span style="font-family: 宋体;">，此时数据量很少，几乎全都保留在</span><span style="font-family: Calibri;">filesystem cache</span><span style="font-family: 宋体;">里面了，就可以确保热数据的访问性能是很高的。</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">但是对于冷数据而言，是在别的</span>index<span style="font-family: 宋体;">里的，跟热数据</span><span style="font-family: Calibri;">index</span><span style="font-family: 宋体;">都不再相同的机器上，大家互相之间都没什么联系了。如果有人访问冷数据，可能大量数据是在磁盘上的，此时性能差点，就</span><span style="font-family: Calibri;">10%</span><span style="font-family: 宋体;">的人去访问冷数据；</span><span style="font-family: Calibri;">90%</span><span style="font-family: 宋体;">的人在访问热数据。</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">（</span>4<span style="font-family: 宋体;">）</span><span style="font-family: Calibri;">document</span><span style="font-family: 宋体;">模型设计</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">有不少同学问我，</span>mysql<span style="font-family: 宋体;">，有两张表</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">订单表：</span>id order_code total_price</p>
<p>&nbsp;</p>
<p>1 <span style="font-family: 宋体;">测试订单 </span><span style="font-family: Calibri;">5000</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">订单条目表：</span>id order_id goods_id purchase_count price</p>
<p>&nbsp;</p>
<p>1 1 1 2 2000</p>
<p>2 1 2 5 200</p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">我在</span>mysql<span style="font-family: 宋体;">里，都是</span><span style="font-family: Calibri;">select * from order join order_item on order.id=order_item.order_id where order.id=1</span></p>
<p>&nbsp;</p>
<p>1 <span style="font-family: 宋体;">测试订单 </span><span style="font-family: Calibri;">5000 1 1 1 2 2000</span></p>
<p>1 <span style="font-family: 宋体;">测试订单 </span><span style="font-family: Calibri;">5000 2 1 2 5 200</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">在</span>es<span style="font-family: 宋体;">里该怎么玩儿，</span><span style="font-family: Calibri;">es</span><span style="font-family: 宋体;">里面的复杂的关联查询，复杂的查询语法，尽量别用，一旦用了性能一般都不太好</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">设计</span>es<span style="font-family: 宋体;">里的数据模型</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">写入</span>es<span style="font-family: 宋体;">的时候，搞成两个索引，</span><span style="font-family: Calibri;">order</span><span style="font-family: 宋体;">索引，</span><span style="font-family: Calibri;">orderItem</span><span style="font-family: 宋体;">索引</span></p>
<p>&nbsp;</p>
<p>order<span style="font-family: 宋体;">索引，里面就包含</span><span style="font-family: Calibri;">id order_code total_price</span></p>
<p>orderItem<span style="font-family: 宋体;">索引，里面写入进去的时候，就完成</span><span style="font-family: Calibri;">join</span><span style="font-family: 宋体;">操作，</span><span style="font-family: Calibri;">id order_code total_price id order_id goods_id purchase_count price</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">写入</span>es<span style="font-family: 宋体;">的</span><span style="font-family: Calibri;">java</span><span style="font-family: 宋体;">系统里，就完成关联，将关联好的数据直接写入</span><span style="font-family: Calibri;">es</span><span style="font-family: 宋体;">中，搜索的时候，就不需要利用</span><span style="font-family: Calibri;">es</span><span style="font-family: 宋体;">的搜索语法去完成</span><span style="font-family: Calibri;">join</span><span style="font-family: 宋体;">来搜索了</span></p>
<p>&nbsp;</p>
<p>document<span style="font-family: 宋体;">模型设计是非常重要的，很多操作，不要在搜索的时候才想去执行各种复杂的乱七八糟的操作。</span><span style="font-family: Calibri;">es</span><span style="font-family: 宋体;">能支持的操作就是那么多，不要考虑用</span><span style="font-family: Calibri;">es</span><span style="font-family: 宋体;">做一些它不好操作的事情。如果真的有那种操作，尽量在</span><span style="font-family: Calibri;">document</span><span style="font-family: 宋体;">模型设计的时候，写入的时候就完成。另外对于一些太复杂的操作，比如</span><span style="font-family: Calibri;">join</span><span style="font-family: 宋体;">，</span><span style="font-family: Calibri;">nested</span><span style="font-family: 宋体;">，</span><span style="font-family: Calibri;">parent-child</span><span style="font-family: 宋体;">搜索都要尽量避免，性能都很差的。</span></p>
<p>&nbsp;</p>
<p>很多同学在问我，很多复杂的乱七八糟的一些操作，如何执行</p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">两个思路，在搜索</span>/<span style="font-family: 宋体;">查询的时候，要执行一些业务强相关的特别复杂的操作：</span></p>
<p>&nbsp;</p>
<p>1<span style="font-family: 宋体;">）在写入数据的时候，就设计好模型，加几个字段，把处理好的数据写入加的字段里面</span></p>
<p>2<span style="font-family: 宋体;">）自己用</span><span style="font-family: Calibri;">java</span><span style="font-family: 宋体;">程序封装，</span><span style="font-family: Calibri;">es</span><span style="font-family: 宋体;">能做的，用</span><span style="font-family: Calibri;">es</span><span style="font-family: 宋体;">来做，搜索出来的数据，在</span><span style="font-family: Calibri;">java</span><span style="font-family: 宋体;">程序里面去做，比如说我们，基于</span><span style="font-family: Calibri;">es</span><span style="font-family: 宋体;">，用</span><span style="font-family: Calibri;">java</span><span style="font-family: 宋体;">封装一些特别复杂的操作</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">（</span>5<span style="font-family: 宋体;">）分页性能优化</span></p>
<p>&nbsp;</p>
<p>es<span style="font-family: 宋体;">的分页是较坑的，为啥呢？举个例子吧，假如你每页是</span><span style="font-family: Calibri;">10</span><span style="font-family: 宋体;">条数据，你现在要查询第</span><span style="font-family: Calibri;">100</span><span style="font-family: 宋体;">页，实际上是会把每个</span><span style="font-family: Calibri;">shard</span><span style="font-family: 宋体;">上存储的前</span><span style="font-family: Calibri;">1000</span><span style="font-family: 宋体;">条数据都查到一个协调节点上，如果你有个</span><span style="font-family: Calibri;">5</span><span style="font-family: 宋体;">个</span><span style="font-family: Calibri;">shard</span><span style="font-family: 宋体;">，那么就有</span><span style="font-family: Calibri;">5000</span><span style="font-family: 宋体;">条数据，接着协调节点对这</span><span style="font-family: Calibri;">5000</span><span style="font-family: 宋体;">条数据进行一些合并、处理，再获取到最终第</span><span style="font-family: Calibri;">100</span><span style="font-family: 宋体;">页的</span><span style="font-family: Calibri;">10</span><span style="font-family: 宋体;">条数据。</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">分布式的，你要查第</span>100<span style="font-family: 宋体;">页的</span><span style="font-family: Calibri;">10</span><span style="font-family: 宋体;">条数据，你是不可能说从</span><span style="font-family: Calibri;">5</span><span style="font-family: 宋体;">个</span><span style="font-family: Calibri;">shard</span><span style="font-family: 宋体;">，每个</span><span style="font-family: Calibri;">shard</span><span style="font-family: 宋体;">就查</span><span style="font-family: Calibri;">2</span><span style="font-family: 宋体;">条数据？最后到协调节点合并成</span><span style="font-family: Calibri;">10</span><span style="font-family: 宋体;">条数据？你必须得从每个</span><span style="font-family: Calibri;">shard</span><span style="font-family: 宋体;">都查</span><span style="font-family: Calibri;">1000</span><span style="font-family: 宋体;">条数据过来，然后根据你的需求进行排序、筛选等等操作，最后再次分页，拿到里面第</span><span style="font-family: Calibri;">100</span><span style="font-family: 宋体;">页的数据。</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">你翻页的时候，翻的越深，每个</span>shard<span style="font-family: 宋体;">返回的数据就越多，而且协调节点处理的时间越长。非常坑爹。所以用</span><span style="font-family: Calibri;">es</span><span style="font-family: 宋体;">做分页的时候，你会发现越翻到后面，就越是慢。</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">我们之前也是遇到过这个问题，用</span>es<span style="font-family: 宋体;">作分页，前几页就几十毫秒，翻到</span><span style="font-family: Calibri;">10</span><span style="font-family: 宋体;">页之后，几十页的时候，基本上就要</span><span style="font-family: Calibri;">5~10</span><span style="font-family: 宋体;">秒才能查出来一页数据了</span></p>
<p>&nbsp;</p>
<p>1<span style="font-family: 宋体;">）不允许深度分页</span><span style="font-family: Calibri;">/</span><span style="font-family: 宋体;">默认深度分页性能很惨</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">你系统不允许他翻那么深的页，</span>pm<span style="font-family: 宋体;">，默认翻的越深，性能就越差</span></p>
<p>&nbsp;</p>
<p>2<span style="font-family: 宋体;">）类似于</span><span style="font-family: Calibri;">app</span><span style="font-family: 宋体;">里的推荐商品不断下拉出来一页一页的</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">类似于微博中，下拉刷微博，刷出来一页一页的，你可以用</span>scroll api<span style="font-family: 宋体;">，自己百度</span></p>
<p>&nbsp;</p>
<p>scroll<span style="font-family: 宋体;">会一次性给你生成所有数据的一个快照，然后每次翻页就是通过游标移动，获取下一页下一页这样子，性能会比上面说的那种分页性能也高很多很多</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">针对这个问题，你可以考虑用</span>scroll<span style="font-family: 宋体;">来进行处理，</span><span style="font-family: Calibri;">scroll</span><span style="font-family: 宋体;">的原理实际上是保留一个数据快照，然后在一定时间内，你如果不断的滑动往后翻页的时候，类似于你现在在浏览微博，不断往下刷新翻页。那么就用</span><span style="font-family: Calibri;">scroll</span><span style="font-family: 宋体;">不断通过游标获取下一页数据，这个性能是很高的，比</span><span style="font-family: Calibri;">es</span><span style="font-family: 宋体;">实际翻页要好的多的多。</span></p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">但是唯一的一点就是，这个适合于那种类似微博下拉翻页的，不能随意跳到任何一页的场景。同时这个</span>scroll<span style="font-family: 宋体;">是要保留一段时间内的数据快照的，你需要确保用户不会持续不断翻页翻几个小时。</span></p>
<p>&nbsp;</p>
<p>无论翻多少页，性能基本上都是毫秒级的</p>
<p>&nbsp;</p>
<p><span style="font-family: 宋体;">因为</span>scroll api<span style="font-family: 宋体;">是只能一页一页往后翻的，是不能说，先进入第</span><span style="font-family: Calibri;">10</span><span style="font-family: 宋体;">页，然后去</span><span style="font-family: Calibri;">120</span><span style="font-family: 宋体;">页，回到</span><span style="font-family: Calibri;">58</span><span style="font-family: 宋体;">页，不能随意乱跳页。所以现在很多产品，都是不允许你随意翻页的，</span><span style="font-family: Calibri;">app</span><span style="font-family: 宋体;">，也有一些网站，做的就是你只能往下拉，一页一页的翻.</span></p>
<p><span style="font-family: 宋体;"><img src="https://images2018.cnblogs.com/blog/918692/201808/918692-20180803090453630-383128381.png" alt=""></span></p>
<p>&nbsp;</p></div><div id="MySignature"></div>
<div class="clear"></div>
<div id="blog_post_info_block">
<div id="BlogPostCategory"></div>
<div id="EntryTag"></div>
<div id="blog_post_info"><div id="green_channel">
<a href="javascript:void(0);" id="green_channel_digg" onclick="DiggIt(9411495,cb_blogId,1);green_channel_success(this,'谢谢推荐！');">好文要顶</a>
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
<div class="diggit" onclick="votePost(9411495,'Digg')">
<span class="diggnum" id="digg_count">0</span>
</div>
<div class="buryit" onclick="votePost(9411495,'Bury')">
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
<div id="post_next_prev"><a href="https://www.cnblogs.com/daiwei1981/p/9411482.html" class="p_n_p_prefix">« </a> 上一篇：<a href="https://www.cnblogs.com/daiwei1981/p/9411482.html" title="发布于2018-08-03 09:03">分布式搜索的面试题2</a><br><a href="https://www.cnblogs.com/daiwei1981/p/9411502.html" class="p_n_p_prefix">» </a> 下一篇：<a href="https://www.cnblogs.com/daiwei1981/p/9411502.html" title="发布于2018-08-03 09:06">分布式搜索的面试题4</a><br></div>
</div>

<div class="postDesc">posted on <span id="post-date">2018-08-03 09:05</span> <a href="https://www.cnblogs.com/daiwei1981/">伪全栈的java工程师</a> 阅读(<span id="post_view_count">362</span>) 评论(<span id="post_comment_count">0</span>)  <a href="https://i.cnblogs.com/EditPosts.aspx?postid=9411495" rel="nofollow">编辑</a> <a href="#" onclick="AddToWz(9411495);return false;">收藏</a></div>
</div>
