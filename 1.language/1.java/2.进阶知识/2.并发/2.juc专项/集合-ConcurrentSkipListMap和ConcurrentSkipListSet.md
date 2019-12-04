# JUC源码分析-集合篇（三）：ConcurrentSkipListMap和ConcurrentSkipListSet

[目录](readme.md)

ConcurrentSkipListMap 是一个线程安全的有序的哈希表，并发安全主要由 CAS 来实现。内部数据存储使用了跳表（Skip Lisy），时间复杂度O(log n)，空间复杂度O(n)。
ConcurrentSkipListSet 完全是由 ConcurrentSkipListMap 实现，它们俩的关系就像 TreeMap 和TreeSet 。本章我们重点分析ConcurrentSkipListMap。

## Skip List

首先来看一下本章最重要的一种数据结构—**跳表(Skip List)**，它是替代平衡树的一种据结构，和红黑树不相同的是，跳表对于树的平衡的实现是基于一种随机化的算法的，这样也就是说跳表的插入和删除的工作是比较简单的。跳表具有以下特性：

* **跳表分为多层，层级越高跳跃性越大，数据越少**
* **跳表的层级是通过“掷硬币”方式来决定增长的，也就是说在增加元素时，概率增长层数**
* **跳表的第一层包含所有元素**
* **每层的元素集合必须包含序数最小的元素**
* **查找数据时，按照从上到下，从左往右的顺序查找**
* **时间复杂度O(log n)，空间复杂度O(n)**

**链表和跳表的对比：**
![](https://upload-images.jianshu.io/upload_images/6050820-a5a233440372cba6.png?imageMogr2/auto-orient/strip|imageView2/2/w/698/format/webp)

链表查找元素

如果要找到元素 25，在链表中我们需要五步（红色箭头）才能查到；而在跳表中只需要三步（根据跳表层级分配，有可能更少），如下图（忽略向下路线）：
![](https://upload-images.jianshu.io/upload_images/6050820-08470d09b565fe3e.png?imageMogr2/auto-orient/strip|imageView2/2/w/698/format/webp)

跳表查找元素

## 内部属性

```java
//用来定义最底层(base-level)的头节点。
private static final Object BASE_HEADER = new Object();
//跳表的最高层headIndex
private transient volatile HeadIndex<K,V> head;

```

**BASE_HEADER：**初始化时放到最底层头节点的 value
**Head：**跳表最高层的 HeadIndex，在跳表层级更新时需要随之更新

## 数据结构

下面我们来看一下 ConcurrentSkipListMap 中跳表的实现：
![](https://upload-images.jianshu.io/upload_images/6050820-a4f310410ca464ff.png?imageMogr2/auto-orient/strip|imageView2/2/w/1100/format/webp)

ConcurrentSkipListMap 数据结构

ConcurrentSkipListMap 继承自AbstractMap，实现了ConcurrentNavigableMap 接口，也就是说它是一个**有序的并发安全的哈希表**。内部数据存储通过三个内部类实现：

* **Node：**存储K-V数据，持有下一个节点的引用，也就是说Node是一个单向链表。
* **Index：**跳表中的索引节点，包含了右指针(right)，向下的索引(down)和节点node。注意虽然 Node和Index都有向前的指针字段，但是它们类型不同，并且处理方式也不同，所以如果在共享的抽象类中放置这些字段不能很好地实现这一点。
* **HeadIndex：**表示跳表的表头，继承自Index，内部标识了当前层级level

### ConcurrentSkipListSet

ConcurrentSkipListSet 是一个支持并发的有序集合，内部持有一个ConcurrentSkipListMap，所有操作都是通过 ConcurrentSkipListMap 来完成。使用

Boolean.TRUE
作为map中每个元素的value，map的key作为Set的元素集合（所以不允许存储重复值）。由于是一个Set，所以它不支持随机索引元素。本章只要理解了 ConcurrentSkipListMap，ConcurrentSkipListSet 也就不在话下了。

## 源码解析

### findPredecessor (Object key, Comparator<? super K> cmp)

```java
private Node<K,V> findPredecessor(Object key, Comparator<? super K> cmp) {
    if (key == null)
        throw new NullPointerException(); // don't postpone errors
    for (;;) {
        //q:head节点; r:右节点; d:下节点
        for (Index<K,V> q = head, r = q.right, d;;) {
            //右节点不为空
            if (r != null) {
                Node<K,V> n = r.node;
                K k = n.key;
                //判断r.node是否被删除
                if (n.value == null) {
                    //如果n已经删除,尝试以CAS更新q的右节点为r.right
                    if (!q.unlink(r))
                        break;           // restart
                    r = q.right;         // reread r
                    continue;
                }
                //比较key和k
                if (cpr(cmp, key, k) > 0) {
                    //继续向右循环
                    q = r;
                    r = r.right;
                    continue;
                }
            }
            //right节点为空,则向下找
            if ((d = q.down) == null)
                //下节点为空,返回当前q节点的node
                return q.node;
            q = d;
            r = d.right;
        }
    }
}
```

**说明：** 
在 ConcurrentSkipListMap 中，所有涉及到查找节点的方法都调用了findPredecessor，
它用于查找最底层(base-level)节点链中比给定 key 小(在“给定节点”左边)的节点，也就是给定 key 节点的前继节点。
如果没找到，那么返回底层链表的头节点。在查找过程中会帮助清除已经被删除的节点。
在put、get、remove方法中都有调用findPredecessor，因为在这些方法遍历时，都是从这个节点开始向后遍历。
函数执行流程如下：
从head节点开始按照从上到下，从左到右的顺序查找，找到right节点的 key 值大于给定 key 的节点，然后返回 left 节点。
比较绕，还是画图说明吧，如下图，如果我们所传的key值为35，那么从左节点8开始向右遍历，一直到找一个比35大的节点39，然后返回39的left节点29。


![](https://upload-images.jianshu.io/upload_images/6050820-f87147c67f55d4fc.png?imageMogr2/auto-orient/strip|imageView2/2/w/698/format/webp)

findPredecessor

### put (K key, V value)

```java
public V put(K key, V value) {
    if (value == null)
        throw new NullPointerException();
    return doPut(key, value, false);
}
private V doPut(K key, V value, boolean onlyIfAbsent) {
    Node<K,V> z;             // added node
    if (key == null)
        throw new NullPointerException();
    Comparator<? super K> cmp = comparator;
    //第一个自旋，更新或插入新Node
    outer: for (;;) {
        //从最底层(base-level)给定key节点的前继节点开始向后查找
        for (Node<K,V> b = findPredecessor(key, cmp), n = b.next;;) {
            if (n != null) {
                Object v; int c;
                Node<K,V> f = n.next;
                if (n != b.next) //读不一致,跳出重试           // inconsistent read
                    break;
                if ((v = n.value) == null) {   // n is deleted
                    //帮助删除n节点，重新查找
                    n.helpDelete(b, f);
                    break;
                }
                if (b.value == null || v == n) // b is deleted
                    break;
                if ((c = cpr(cmp, key, n.key)) > 0) {//当前key大于n.key，继续向后查找
                    b = n;
                    n = f;
                    continue;
                }
                if (c == 0) {//update处理
                    if (onlyIfAbsent || n.casValue(v, value)) {//更新value，返回更新前的值
                        @SuppressWarnings("unchecked") V vv = (V)v;
                        return vv;
                    }
                    break; // restart if lost race to replace value
                }
                // else c < 0; fall through
            }
            //新建一个节点，放到b和b.next之间
            z = new Node<K,V>(key, value, n);
            //cas替换
            if (!b.casNext(n, z))
                break;         // restart if lost race to append to b
            break outer;
        }
    }
    //更新Index逻辑
    int rnd = ThreadLocalRandom.nextSecondarySeed();
    //生成随机数为正偶数才会更新层级（通过最高位和最低位不为1验证）
    if ((rnd & 0x80000001) == 0) { // test highest and lowest bits
        int level = 1, max;
        //计算跳表level
        while (((rnd >>>= 1) & 1) != 0)//判断从低2位开始向左有多少个连续的1
            ++level;
        //idx:新添加的index的level层index
        Index<K,V> idx = null;
        HeadIndex<K,V> h = head;
        //构建Index逻辑
        if (level <= (max = h.level)) {//不需要增加层级
            for (int i = 1; i <= level; ++i)
                //从下到上构建,节点持有新节点z和down节点idx的引用
                idx = new Index<K,V>(z, idx, null);
        }
        else { // try to grow by one level
            //构建新层级（旧层级+1）
            level = max + 1; // hold in array and later pick the one to use
            //构建一个level+1长度的Index数组
            @SuppressWarnings("unchecked")Index<K,V>[] idxs =
                (Index<K,V>[])new Index<?,?>[level+1];
            //从下到到上构建HeadIndex
            for (int i = 1; i <= level; ++i)
                idxs[i] = idx = new Index<K,V>(z, idx, null);
            //自旋
            for (;;) {
                h = head;
                //保存head之前的层级
                int oldLevel = h.level;
                if (level <= oldLevel) // lost race to add level
                    break;

                HeadIndex<K,V> newh = h;
                Node<K,V> oldbase = h.node;
                //构建new head
                for (int j = oldLevel+1; j <= level; ++j)
                    newh = new HeadIndex<K,V>(oldbase, newh, idxs[j], j);
                //cas替换head节点
                if (casHead(h, newh)) {
                    h = newh;
                    //idx赋值为之前层级的头节点，并将level赋值为之前的层级
                    idx = idxs[level = oldLevel];
                    break;
                }
            }
        }
        // find insertion points and splice in
        //插入Index
        splice: for (int insertionLevel = level;;) {
            //获取新跳表head的层级
            int j = h.level;
            // 查找需要清除的节点
            // q:新跳表head; r:q.right; t:新增的Index节点
            for (Index<K,V> q = h, r = q.right, t = idx;;) {
                if (q == null || t == null)
                    break splice;
                if (r != null) {
                    Node<K,V> n = r.node;
                    // compare before deletion check avoids needing recheck
                    int c = cpr(cmp, key, n.key);
                    if (n.value == null) {// 需要清除n节点
                        // 删除q的right节点r，并替换为r.right
                        if (!q.unlink(r))
                            break;
                        r = q.right;//重新对r赋值
                        continue;
                    }
                    if (c > 0) {
                        // 继续向右查找一个key小于当前遍历到的节点r
                        q = r;
                        r = r.right;
                        continue;
                    }
                }

                if (j == insertionLevel) { //新跳表head层级等于旧跳表head层级
                    if (!q.link(r, t))//把新增节点插入q和r之间
                        break; // restart
                    if (t.node.value == null) {//新增节点被删除
                        findNode(key);//清除已删除的节点
                        break splice;
                    }
                    if (--insertionLevel == 0)//到达最底层
                        break splice;
                }

                //移动到下一层level
                if (--j >= insertionLevel && j < level)
                    t = t.down;
                q = q.down;
                r = q.right;
            }
        }
    }
    return null;
}

```

说明：doput是插入或更新元素的主方法，函数执行流程分两步:
       
   - 1.自旋查找索引位置，更新或插入给定节点（Node）元素。在这一步中也会帮助清除已经删除的节点。大概流程如下：
       * 通过findPredecessor方法（后面会分析）首先找到最底层(base-level)给定key节点的前继节点b，从这个节点开始向后查找合适位置插入（或更新）
       * 如果在查找过程中遇到已删除节点会调用helpDelete方法帮助清除节点链接，然后重复a步骤
   - 2.通过“掷硬币”方式决定是否更新跳表层级。在 ConcurrentSkipListMap 中这个“掷硬币”随机算法实现如下：
       * 生成一个随机数rnd，如果rnd为正偶数（通过最高位和最低位都不为1验证）才会更新Index。
       * 然后判断这个随机数rnd从低2位开始有多少连续的1，如果这个“连续数”小于或等于旧表层级，则不需要增加跳表层级，只需更新Index；
       * 否则的话就需要增加跳表层级（旧表层级+1），并且更新Index和HeadIndex。
   

### remove (Object key)

```java
public V remove(Object key) {
    return doRemove(key, null);
}
final V doRemove(Object key, Object value) {
    if (key == null)
        throw new NullPointerException();
    Comparator<? super K> cmp = comparator;
    outer: for (;;) {
        //找到给定key节点的前继节点
        for (Node<K,V> b = findPredecessor(key, cmp), n = b.next;;) {
            Object v; int c;
            if (n == null)
                break outer;
            Node<K,V> f = n.next;
            if (n != b.next)                    // inconsistent read
                break;
            if ((v = n.value) == null) {        // n is deleted
                n.helpDelete(b, f);//帮助清除已删除节点
                break;
            }
            if (b.value == null || v == n)      // b is deleted
                break;
            if ((c = cpr(cmp, key, n.key)) < 0)
                break outer;
            if (c > 0) {
                //继续往右寻找
                b = n;
                n = f;
                continue;
            }
            if (value != null && !value.equals(v))
                break outer;
            if (!n.casValue(v, null))//找到指定节点，把节点的value置空
                break;
            //给节点添加删除标识（next节点改为一个指向自身的节点）
            //然后把前继节点的next节点CAS修改为next.next节点（彻底解除n节点的链接）
            if (!n.appendMarker(f) || !b.casNext(n, f))
                //如果cas失败，清除已删除的节点后重新循环
                findNode(key);                  // retry via findNode
            else {
                //删除n节点对应的index
                findPredecessor(key, cmp);      // clean index
                if (head.right == null)
                    //减少跳表层级
                    tryReduceLevel();
            }
            @SuppressWarnings("unchecked") V vv = (V)v;
            return vv;//返回对应value
        }
    }
    return null;
}
```

说明： doRemove是移除节点的主方法，函数逻辑如下：

- 1.首先找到需要删除节点的前节点，如果在查找过程中发现有已经删除的节点，就帮助清除节点（解除节点链接）。
- 2.在找到需要删除的节点时并不会直接移除它，而是先利用 CAS 给这个节点添加一个删除标识（next 节点改为一个指向自身的节点），
   然后再利用 CAS 解除它的链接；如果途中 CAS 执行失败，则调用findNode方法来清除已经删除的节点。
- 3.最后检查head.right如果已经被移除，就调用tryReduceLevel()方法尝试对跳表进行降级操作（跳表只有在层级大于3时才可以降级），tryReduceLevel()源码如下:


tryReduceLevel()
源码如下:

```java
private void tryReduceLevel() {
    HeadIndex<K,V> h = head;
    HeadIndex<K,V> d;
    HeadIndex<K,V> e;
    if (h.level > 3 &&
        (d = (HeadIndex<K,V>)h.down) != null &&
        (e = (HeadIndex<K,V>)d.down) != null &&
        e.right == null &&
        d.right == null &&
        h.right == null &&
        casHead(h, d) && // try to set
        h.right != null) // recheck
        casHead(d, h);   // try to backout
}

```

### get (Object key)

```java
public V get(Object key) {
    return doGet(key);
}
private V doGet(Object key) {
    if (key == null)
        throw new NullPointerException();
    Comparator<? super K> cmp = comparator;
    outer: for (;;) {
        //从最底层(base-level)给定key节点的前继节点开始向后查找
        for (Node<K,V> b = findPredecessor(key, cmp), n = b.next;;) {
            Object v; int c;
            if (n == null)
                break outer;
            Node<K,V> f = n.next;
            if (n != b.next)                // inconsistent read
                break;
            if ((v = n.value) == null) {    // n is deleted
                //节点n被删除，帮助清除已删除节点，继续循环outer
                n.helpDelete(b, f);
                break;
            }
            if (b.value == null || v == n)  // b is deleted
                break;
            if ((c = cpr(cmp, key, n.key)) == 0) {//检查key是否相等
                @SuppressWarnings("unchecked") V vv = (V)v;
                return vv;
            }
            if (c < 0)
                break outer;
            //未找到合适节点，继续向后寻找
            b = n;
            n = f;
        }
    }
    return null;
}

```

**说明：**

doGet是通过key查找节点value的主方法，源码非常之简单，流程跟doRemove
类似，不多赘述。

## 小结

本章重点：理解 ConcurrentSkipListMap 的数据结构—跳表。
Hope you like it...
1人点赞

[目录](readme.md)

参考
[JUC源码分析-集合篇（三）：ConcurrentSkipListMap和ConcurrentSkipListSet](https://www.jianshu.com/p/8a223af84fc4)