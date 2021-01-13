# 数据结构

## 队列

- [《java 队列——queue 详细分析》](https://www.cnblogs.com/lemon-flm/p/7877898.html)
  _ 非阻塞队列：ConcurrentLinkedQueue(无界线程安全)，采用 CAS 机制（compareAndSwapObject 原子操作）。
  _ 阻塞队列：ArrayBlockingQueue(有界)、LinkedBlockingQueue（无界）、DelayQueue、PriorityBlockingQueue，采用锁机制；使用 ReentrantLock 锁。

- [《LinkedList、ConcurrentLinkedQueue、LinkedBlockingQueue 对比分析》](https://www.cnblogs.com/mantu/p/5802393.html)

## 集合

- [《Java Set 集合的详解》](https://blog.csdn.net/qq_33642117/article/details/52040345)

## 链表、数组

- [《Java 集合详解--什么是 List》](https://blog.csdn.net/wz249863091/article/details/52853360)

## 字典、关联数组

- [《Java map 详解 - 用法、遍历、排序、常用 API 等》](https://baike.xsoftlab.net/view/250.html)

## 栈

- [《java 数据结构与算法之栈（Stack）设计与实现》](https://blog.csdn.net/javazejian/article/details/53362993)
- [《Java Stack 类》](http://www.runoob.com/java/java-stack-class.html)
- [《java stack 的详细实现分析》](https://blog.csdn.net/f2006116/article/details/51375225)
  _ Stack 是线程安全的。
  _ 内部使用数组保存数据，不够时翻倍。

## 树

### 二叉树

每个节点最多有两个叶子节点。

- [《二叉树》](https://blog.csdn.net/cai2016/article/details/52589952)

### 完全二叉树

- [《完全二叉树》](https://baike.baidu.com/item/%E5%AE%8C%E5%85%A8%E4%BA%8C%E5%8F%89%E6%A0%91/7773232?fr=aladdin) \* 叶节点只能出现在最下层和次下层，并且最下面一层的结点都集中在该层最左边的若干位置的二叉树。

### 平衡二叉树

左右两个子树的高度差的绝对值不超过 1，并且左右两个子树都是一棵平衡二叉树。

- [《浅谈数据结构-平衡二叉树》](http://www.cnblogs.com/polly333/p/4798944.html)
- [《浅谈算法和数据结构: 八 平衡查找树之 2-3 树》](http://www.cnblogs.com/yangecnu/p/Introduce-2-3-Search-Tree.html)

### 二叉查找树（BST）

二叉查找树（Binary Search Tree），也称有序二叉树（ordered binary tree）,排序二叉树（sorted binary tree）。

- [《浅谈算法和数据结构: 七 二叉查找树》](http://www.cnblogs.com/yangecnu/p/Introduce-Binary-Search-Tree.html)

### 红黑树

- [《最容易懂得红黑树》](https://blog.csdn.net/sun_tttt/article/details/65445754) \* 添加阶段后，左旋或者右旋从而再次达到平衡。
- [《浅谈算法和数据结构: 九 平衡查找树之红黑树》](http://www.cnblogs.com/yangecnu/p/Introduce-Red-Black-Tree.html)

### B，B+，B\*树

MySQL 是基于 B+树聚集索引组织表

- [《B-树，B+树，B\*树详解》](https://blog.csdn.net/aqzwss/article/details/53074186)
- [《B-树，B+树与 B\*树的优缺点比较》](https://blog.csdn.net/bigtree_3721/article/details/73632405) \* B+树的叶子节点链表结构相比于 B-树便于扫库，和范围检索。

### LSM 树

LSM（Log-Structured Merge-Trees）和 B+ 树相比，是牺牲了部分读的性能来换取写的性能(通过批量写入)，实现读写之间的。
Hbase、LevelDB、Tair（Long DB）、nessDB 采用 LSM 树的结构。LSM 可以快速建立索引。

- [《LSM 树 VS B+树》](https://blog.csdn.net/dbanote/article/details/8897599)
  _ B+ 树读性能好，但由于需要有序结构，当 key 比较分散时，磁盘寻道频繁，造成写性能。
  _ LSM 是将一个大树拆分成 N 棵小树，先写到内存（无寻道问题，性能高），在内存中构建一颗有序小树（有序树），随着小树越来越大，内存的小树会 flush 到磁盘上。当读时，由于不知道数据在哪棵小树上，因此必须遍历（二分查找）所有的小树，但在每颗小树内部数据是有序的。
- [《LSM 树（Log-Structured Merge Tree）存储引擎》](https://blog.csdn.net/u014774781/article/details/52105708)
  _ 极端的说，基于 LSM 树实现的 HBase 的写性能比 MySQL 高了一个数量级，读性能低了一个数量级。
  _ 优化方式：Bloom filter 替代二分查找；compact 小数位大树，提高查询性能。 \* Hbase 中，内存中达到一定阈值后，整体 flush 到磁盘上、形成一个文件（B+数），HDFS 不支持 update 操作，所以 Hbase 做整体 flush 而不是 merge update。flush 到磁盘上的小树，定期会合并成一个大树。

## BitSet

经常用于大规模数据的排重检查。

- [《Java Bitset 类》](http://www.runoob.com/java/java-bitset-class.html)
- [《Java BitSet（位集）》](https://blog.csdn.net/caiandyong/article/details/51581160)

# 常用算法

- [《常见排序算法及对应的时间复杂度和空间复杂度》](https://blog.csdn.net/gane_cheng/article/details/52652705)

## 排序、查找算法

- [《常见排序算法及对应的时间复杂度和空间复杂度》](https://blog.csdn.net/gane_cheng/article/details/52652705)

### 选择排序

- [《Java 中的经典算法之选择排序（SelectionSort）》](https://www.cnblogs.com/shen-hua/p/5424059.html) \* 每一趟从待排序的记录中选出最小的元素，顺序放在已排好序的序列最后，直到全部记录排序完毕。

### 冒泡排序

- [《冒泡排序的 2 种写法》](https://blog.csdn.net/shuaizai88/article/details/73250615)
  _ 相邻元素前后交换、把最大的排到最后。
  _ 时间复杂度 O(n²)

### 插入排序

- [《排序算法总结之插入排序》](https://www.cnblogs.com/hapjin/p/5517667.html)

### 快速排序

- [《坐在马桶上看算法：快速排序》](http://developer.51cto.com/art/201403/430986.htm) \* 一侧比另外一次都大或小。

### 归并排序

- [《图解排序算法(四)之归并排序》](http://www.cnblogs.com/chengxiao/p/6194356.html) \* 分而治之，分成小份排序，在合并(重建一个新空间进行复制)。

### 希尔排序

TODO

### 堆排序

- [《图解排序算法(三)之堆排序》](https://www.cnblogs.com/chengxiao/p/6129630.html) \* 排序过程就是构建最大堆的过程，最大堆：每个结点的值都大于或等于其左右孩子结点的值，堆顶元素是最大值。

### 计数排序

- [《计数排序和桶排序》](https://www.cnblogs.com/suvllian/p/5495780.html) \* 和桶排序过程比较像，差别在于桶的数量。

### 桶排序

- [《【啊哈！算法】最快最简单的排序——桶排序》](http://blog.51cto.com/ahalei/1362789)
- [《排序算法（三）：计数排序与桶排序》](https://blog.csdn.net/sunjinshengli/article/details/70738527)
  _ 桶排序将[0,1)区间划分为 n 个相同的大小的子区间，这些子区间被称为桶。
  _ 每个桶单独进行排序，然后再遍历每个桶。

### 基数排序

按照个位、十位、百位、...依次来排。

- [《排序算法系列：基数排序》](https://blog.csdn.net/lemon_tree12138/article/details/51695211)
- [《基数排序》](https://www.cnblogs.com/skywang12345/p/3603669.html)

### 二分查找

- [《二分查找(java 实现)》](https://www.cnblogs.com/coderising/p/5708632.html)
  _ 要求待查找的序列有序。
  _ 时间复杂度 O(logN)。

- [《java 实现二分查找-两种方式》](https://blog.csdn.net/maoyuanming0806/article/details/78176957) \* while + 递归。

### Java 中的排序工具

- [《Arrays.sort 和 Collections.sort 实现原理解析》](https://blog.csdn.net/u011410529/article/details/56668545?locationnum=6&fps=1)
  _ Collections.sort 算法调用的是合并排序。
  _ Arrays.sort() 采用了 2 种排序算法 -- 基本类型数据使用快速排序法，对象数组使用归并排序。

## 布隆过滤器

常用于大数据的排重，比如 email，url 等。
核心原理：将每条数据通过计算产生一个指纹（一个字节或多个字节，但一定比原始数据要少很多），其中每一位都是通过随机计算获得，在将指纹映射到一个大的按位存储的空间中。注意：会有一定的错误率。
优点：空间和时间效率都很高。
缺点：随着存入的元素数量增加，误算率随之增加。

- [《布隆过滤器 -- 空间效率很高的数据结构》](https://segmentfault.com/a/1190000002729689)
- [《大量数据去重：Bitmap 和布隆过滤器(Bloom Filter)》](https://blog.csdn.net/zdxiq000/article/details/57626464)
- [《基于 Redis 的布隆过滤器的实现》](https://blog.csdn.net/qq_30242609/article/details/71024458) \* 基于 Redis 的 Bitmap 数据结构。
- [《网络爬虫：URL 去重策略之布隆过滤器(BloomFilter)的使用》](https://blog.csdn.net/lemon_tree12138/article/details/47973715) \* 使用 Java 中的 BitSet 类 和 加权和 hash 算法。

## 字符串比较

### KMP 算法

KMP：Knuth-Morris-Pratt 算法（简称 KMP）
核心原理是利用一个“部分匹配表”，跳过已经匹配过的元素。

- [《字符串匹配的 KMP 算法》](http://www.ruanyifeng.com/blog/2013/05/Knuth%E2%80%93Morris%E2%80%93Pratt_algorithm.html)

## 深度优先、广度优先

- [《广度优先搜索 BFS 和深度优先搜索 DFS》](https://www.cnblogs.com/0kk470/p/7555033.html)

## 贪心算法

- [《算法：贪婪算法基础》](https://www.cnblogs.com/MrSaver/p/8641971.html)
- [《常见算法及问题场景——贪心算法》](https://blog.csdn.net/a345017062/article/details/52443781)

## 回溯算法

- [《 五大常用算法之四：回溯法》](https://blog.csdn.net/qfikh/article/details/51960331)

## 剪枝算法

- [《α-β 剪枝算法》](https://blog.csdn.net/luningcsdn/article/details/50930276)

## 动态规划

- [《详解动态规划——邹博讲动态规划》](https://www.cnblogs.com/little-YTMM/p/5372680.html)
- [《动态规划算法的个人理解》](https://blog.csdn.net/yao_zi_jie/article/details/54580283)

## 朴素贝叶斯

- [《带你搞懂朴素贝叶斯分类算法》](https://blog.csdn.net/amds123/article/details/70173402) \* P(B|A)=P(A|B)P(B)/P(A)

- [《贝叶斯推断及其互联网应用 1》](http://www.ruanyifeng.com/blog/2011/08/bayesian_inference_part_one.html)
- [《贝叶斯推断及其互联网应用 2》](http://www.ruanyifeng.com/blog/2011/08/bayesian_inference_part_two.html)

## 推荐算法

- [《推荐算法综述》](http://www.infoq.com/cn/articles/recommendation-algorithm-overview-part01)
- [《TOP 10 开源的推荐系统简介》](https://www.oschina.net/news/51297/top-10-open-source-recommendation-systems)

## 最小生成树算法

- [《算法导论--最小生成树（Kruskal 和 Prim 算法）》](https://blog.csdn.net/luoshixian099/article/details/51908175)

## 最短路径算法

- [《Dijkstra 算法详解》](https://blog.csdn.net/qq_35644234/article/details/60870719)