### 跳跃表（Skip list）原理与 java 实现

Skip list 是一个用于有序元素序列快速搜索的数据结构，由美国计算机科学家 William Pugh 发明于 1989 年。
它的效率和红黑树以及 AVL 树不相上下，但实现起来比较容易。
作者 William Pugh 是这样介绍 Skip list 的：

- Skip lists are a probabilistic data structure that seem likely to supplant balanced trees as the implementation method of choice for many applications. Skip list algorithms have the same asymptotic expected time bounds as balanced trees and are simpler, faster and use less space.
- Skip list 是一个“概率型”的数据结构，可以在很多应用场景中替代平衡树。Skip list 算法与平衡树相比，有相似的渐进期望时间边界，但是它更简单，更快，使用更少的空间。
- Skip list 是一个分层结构多级链表，最下层是原始的链表，每个层级都是下一个层级的“高速跑道”。
  ![](https://img-blog.csdn.net/20160814203628269)
  跳跃的表的性质包括：
- 某个 i 层的元素，出现在 i+1 层的概率 p 是固定的，例如常取 p=1/2 或 p=1/4；
- 平均来讲，每个元素出现在 1/(1-p)个链表中；
- 最高的元素，例如 head 通常采用 Int.MIN_VALUE 作为的最小值，会出现在每一层链表中；
- 原始的链表元素如果是 n，则链表最多这里写图片描述层。例如，p=1/2 时，层数为这里写图片描述。
- 跳跃表的空间复杂度为 O(n)，插入删除的时间复杂度是这里写图片描述。例如，p=1/2 时，复杂度为这里写图片描述。

#### 1.跳跃表的构建

空的跳跃表头尾相连的双向链表。

![](https://img-blog.csdn.net/20160814205919048)
向链表中放入 key-value，假如 key 是 1。
![](https://img-blog.csdn.net/20160814210034643)
此时，不断投掷硬币，如果是反面，则不提升该节点，停止投掷硬币；否则不断提升该节点，并且垂直方向进行节点连接。
![](https://img-blog.csdn.net/20160814210138159)
如果上一步没有提升，再插入 key-value，key 等于 2，然后不断投掷硬币，发现是投了一次正面，需要提升一次。但第二次投的是反面，不再提升。
![](https://img-blog.csdn.net/20160814210246601)
再插入 key-value，key 等于 3，然后不断投掷硬币，发现第一次就投了反面，不提升。
![](https://img-blog.csdn.net/20160814210404957)
这样的规则一直持续下去。由于连续投正面的概率是 0.5，0.5\*0.5……,所以某一个节点提升很多层的概率是很低的。这也是为什么说跳跃表是一种概率型数据结构的来源。
跳跃表的查询也比较简单。例如要查找 key 是 3 的节点，则从最上层开始查找，直到找到大于或等于 3 的位置，然后返回上一个节点，再往下一层继续向右寻找。例如三层的跳跃表查询路径如下。
![](https://img-blog.csdn.net/20160814210637325)
这样，就跳过了很多节点，所以叫做“跳跃表”。

#### 2.Java 实现

这里参考了“跳跃表(Skip List)-实现(Java)”，将其更改为模版形式，并多处进行了重构。

- 节点类

```
/**
 * 跳跃表的节点,包括key-value和上下左右4个指针
 * created by 曹艳丰，2016-08-14
 * 参考：http://www.acmerblog.com/skip-list-impl-java-5773.html
 * */
public class SkipListNode <T>{
    public int key;
    public T value;
    public SkipListNode<T> up, down, left, right; // 上下左右 四个指针

    public static final int HEAD_KEY = Integer.MIN_VALUE; // 负无穷
    public static final int  TAIL_KEY = Integer.MAX_VALUE; // 正无穷
    public SkipListNode(int k,T v) {
        // TODO Auto-generated constructor stub
        key = k;
        value = v;
    }
    public int getKey() {
        return key;
    }
    public void setKey(int key) {
        this.key = key;
    }
    public T getValue() {
        return value;
    }
    public void setValue(T value) {
        this.value = value;
    }
    public boolean equals(Object o) {
        if (this==o) {
            return true;
        }
        if (o==null) {
            return false;
        }
        if (!(o instanceof SkipListNode<?>)) {
            return false;
        }
        SkipListNode<T> ent;
        try {
            ent = (SkipListNode<T>)  o; // 检测类型
        } catch (ClassCastException ex) {
            return false;
        }
        return (ent.getKey() == key) && (ent.getValue() == value);
    }
    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return "key-value:"+key+"-"+value;
    }
}
```

- 跳跃表实现。

```
import java.util.Random;

/**
 * 不固定层级的跳跃表
 * created by 曹艳丰，2016-08-14
 * 参考：http://www.acmerblog.com/skip-list-impl-java-5773.html
 * */
public class SkipList <T>{
    private SkipListNode<T> head,tail;
    private int nodes;//节点总数
    private int listLevel;//层数
    private Random random;// 用于投掷硬币
    private static final double PROBABILITY=0.5;//向上提升一个的概率
    public SkipList() {
        // TODO Auto-generated constructor stub
        random=new Random();
        clear();
    }
    /**
     *清空跳跃表
     * */
    public void clear(){
        head=new SkipListNode<T>(SkipListNode.HEAD_KEY, null);
        tail=new SkipListNode<T>(SkipListNode.TAIL_KEY, null);
        horizontalLink(head, tail);
        listLevel=0;
        nodes=0;
    }
    public boolean isEmpty(){
        return nodes==0;
    }

    public int size() {
        return nodes;
    }
    /**
     * 在最下面一层，找到要插入的位置前面的那个key
     * */
    private SkipListNode<T> findNode(int key){
        SkipListNode<T> p=head;
        while(true){
            while (p.right.key!=SkipListNode.TAIL_KEY&&p.right.key<=key) {
                p=p.right;
            }
            if (p.down!=null) {
                p=p.down;
            }else {
                break;
            }

        }
        return p;
    }
    /**
     * 查找是否存储key，存在则返回该节点，否则返回null
     * */
    public SkipListNode<T> search(int key){
        SkipListNode<T> p=findNode(key);
        if (key==p.getKey()) {
            return p;
        }else {
            return null;
        }
    }
    /**
     * 向跳跃表中添加key-value
     *
     * */
    public void put(int k,T v){
        SkipListNode<T> p=findNode(k);
        //如果key值相同，替换原来的vaule即可结束
        if (k==p.getKey()) {
            p.value=v;
            return;
        }
        SkipListNode<T> q=new SkipListNode<T>(k, v);
        backLink(p, q);
        int currentLevel=0;//当前所在的层级是0
        //抛硬币
        while (random.nextDouble()<PROBABILITY) {
            //如果超出了高度，需要重新建一个顶层
            if (currentLevel>=listLevel) {
                listLevel++;
                SkipListNode<T> p1=new SkipListNode<T>(SkipListNode.HEAD_KEY, null);
                SkipListNode<T> p2=new SkipListNode<T>(SkipListNode.TAIL_KEY, null);
                horizontalLink(p1, p2);
                vertiacallLink(p1, head);
                vertiacallLink(p2, tail);
                head=p1;
                tail=p2;
            }
            //将p移动到上一层
            while (p.up==null) {
                p=p.left;
            }
            p=p.up;

            SkipListNode<T> e=new SkipListNode<T>(k, null);//只保存key就ok
            backLink(p, e);//将e插入到p的后面
            vertiacallLink(e, q);//将e和q上下连接
            q=e;
            currentLevel++;
        }
        nodes++;//层数递增
    }
    //node1后面插入node2
    private void backLink(SkipListNode<T> node1,SkipListNode<T> node2){
        node2.left=node1;
        node2.right=node1.right;
        node1.right.left=node2;
        node1.right=node2;
    }
    /**
     * 水平双向连接
     * */
    private void horizontalLink(SkipListNode<T> node1,SkipListNode<T> node2){
        node1.right=node2;
        node2.left=node1;
    }
    /**
     * 垂直双向连接
     * */
    private void vertiacallLink(SkipListNode<T> node1,SkipListNode<T> node2){
        node1.down=node2;
        node2.up=node1;
    }
    /**
     * 打印出原始数据
     * */
    @Override
    public String toString() {
        // TODO Auto-generated method stub
        if (isEmpty()) {
            return "跳跃表为空！";
        }
        StringBuilder builder=new StringBuilder();
        SkipListNode<T> p=head;
        while (p.down!=null) {
            p=p.down;
        }

        while (p.left!=null) {
            p=p.left;
        }
        if (p.right!=null) {
            p=p.right;
        }
        while (p.right!=null) {
            builder.append(p);
            builder.append("\n");
            p=p.right;
        }

        return builder.toString();
    }

}
```

- 下面进行一下测试。

```
public class Main {

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        SkipList<String> list=new SkipList<String>();
        System.out.println(list);
        list.put(2, "yan");
        list.put(1, "co");
        list.put(3, "feng");
        list.put(1, "cao");//测试同一个key值
        list.put(4, "曹");
        list.put(6, "丰");
        list.put(5, "艳");
        System.out.println(list);
        System.out.println(list.size());
    }
}
```

#### 3.Java 中的跳跃表

Java API 中提供了支持并发操作的跳跃表 ConcurrentSkipListSet 和 ConcurrentSkipListMap。
下面摘录”Java 多线程（四）之 ConcurrentSkipListMap 深入分析“中的一些结论。
有序的情况下：

- 在非多线程的情况下，应当尽量使用 TreeMap（红黑树实现）。
- 对于并发性相对较低的并行程序可以使用 Collections.synchronizedSortedMap 将 TreeMap 进行包装，也可以提供较好的效率。
  但是对于高并发程序，应当使用 ConcurrentSkipListMap。
  无序情况下：
- 并发程度低，数据量大时，ConcurrentHashMap 存取远大于 ConcurrentSkipListMap。
- 数据量一定，并发程度高时，ConcurrentSkipListMap 比 ConcurrentHashMap 效率更高。

### 参考
