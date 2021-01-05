# 1. 造成内存泄漏的原因？

threadLocal 是为了解决**对象不能被多线程共享访问**的问题，通过 threadLocal.set 方法将对象实例保存在每个线程自己所拥有的 threadLocalMap 中，这样每个线程使用自己的对象实例，彼此不会影响达到隔离的作用，从而就解决了对象在被共享访问带来线程安全问题。如果将同步机制和 threadLocal 做一个横向比较的话，同步机制就是通过控制线程访问共享对象的顺序，而 threadLocal 就是为每一个线程分配一个该对象，各用各的互不影响。打个比方说，现在有 100 个同学需要填写一张表格但是只有一支笔，同步就相当于 A 使用完这支笔后给 B，B 使用后给 C 用......老师就控制着这支笔的使用顺序，使得同学之间不会产生冲突。而 threadLocal 就相当于，老师直接准备了 100 支笔，这样每个同学都使用自己的，同学之间就不会产生冲突。很显然这就是两种不同的思路，同步机制以“时间换空间”，由于每个线程在同一时刻共享对象只能被一个线程访问造成整体上响应时间增加，但是对象只占有一份内存，牺牲了时间效率换来了空间效率即“时间换空间”。而 threadLocal，为每个线程都分配了一份对象，自然而然内存使用率增加，每个线程各用各的，整体上时间效率要增加很多，牺牲了空间效率换来时间效率即“空间换时间”。

关于 threadLocal,threadLocalMap 更多的细节可以看[这篇文章](https://juejin.im/post/5aeeb22e6fb9a07aa213404a)，给出了很详细的各个方面的知识（很多也是面试高频考点）。threadLocal,threadLocalMap,entry 之间的关系如下图所示：

![threadLocal引用示意图](http://upload-images.jianshu.io/upload_images/2615789-9107eeb7ad610325.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/620)

上图中，实线代表强引用，虚线代表的是弱引用，如果 threadLocal 外部强引用被置为 null(threadLocalInstance=null)的话，threadLocal 实例就没有一条引用链路可达，很显然在 gc(垃圾回收)的时候势必会被回收，因此 entry 就存在 key 为 null 的情况，无法通过一个 Key 为 null 去访问到该 entry 的 value。同时，就存在了这样一条引用链：threadRef->currentThread->threadLocalMap->entry->valueRef->valueMemory,导致在垃圾回收的时候进行可达性分析的时候,value 可达从而不会被回收掉，但是该 value 永远不能被访问到，这样就存在了**内存泄漏**。当然，如果线程执行结束后，threadLocal，threadRef 会断掉，因此 threadLocal,threadLocalMap，entry 都会被回收掉。可是，在实际使用中我们都是会用线程池去维护我们的线程，比如在 Executors.newFixedThreadPool()时创建线程的时候，为了复用线程是不会结束的，所以 threadLocal 内存泄漏就值得我们关注。

# 2. 已经做出了哪些改进？

实际上，为了解决 threadLocal 潜在的内存泄漏的问题，Josh Bloch and Doug Lea 大师已经做了一些改进。在 threadLocal 的 set 和 get 方法中都有相应的处理。下文为了叙述，针对 key 为 null 的 entry，源码注释为 stale entry，直译为不新鲜的 entry，这里我就称之为“脏 entry”。比如在 ThreadLocalMap 的 set 方法中：

    private void set(ThreadLocal<?> key, Object value) {

        // We don't use a fast path as with get() because it is at
        // least as common to use set() to create new entries as
        // it is to replace existing ones, in which case, a fast
        // path would fail more often than not.

        Entry[] tab = table;
        int len = tab.length;
        int i = key.threadLocalHashCode & (len-1);

        for (Entry e = tab[i];
                 e != null;
                 e = tab[i = nextIndex(i, len)]) {
            ThreadLocal<?> k = e.get();

            if (k == key) {
                e.value = value;
                return;
            }

            if (k == null) {
                replaceStaleEntry(key, value, i);
                return;
            }
         }

        tab[i] = new Entry(key, value);
        int sz = ++size;
        if (!cleanSomeSlots(i, sz) && sz >= threshold)
            rehash();
    }

在该方法中针对脏 entry 做了这样的处理：

1. 如果当前 table[i]！=null 的话说明 hash 冲突就需要向后环形查找，若在查找过程中遇到脏 entry 就通过 replaceStaleEntry 进行处理；
2. 如果当前 table[i]==null 的话说明新的 entry 可以直接插入，但是插入后会调用 cleanSomeSlots 方法检测并清除脏 entry

## 2.1 cleanSomeSlots

该方法的源码为：

    /* @param i a position known NOT to hold a stale entry. The
     * scan starts at the element after i.
     *
     * @param n scan control: {@code log2(n)} cells are scanned,
     * unless a stale entry is found, in which case
     * {@code log2(table.length)-1} additional cells are scanned.
     * When called from insertions, this parameter is the number
     * of elements, but when from replaceStaleEntry, it is the
     * table length. (Note: all this could be changed to be either
     * more or less aggressive by weighting n instead of just
     * using straight log n. But this version is simple, fast, and
     * seems to work well.)
     *
     * @return true if any stale entries have been removed.
     */
    private boolean cleanSomeSlots(int i, int n) {
        boolean removed = false;
        Entry[] tab = table;
        int len = tab.length;
        do {
            i = nextIndex(i, len);
            Entry e = tab[i];
            if (e != null && e.get() == null) {
                n = len;
                removed = true;
                i = expungeStaleEntry(i);
            }
        } while ( (n >>>= 1) != 0);
        return removed;
    }

**入参：**

1. i 表示：插入 entry 的位置 i，很显然在上述情况 2（table[i]==null）中，entry 刚插入后该位置 i 很显然不是脏 entry;

2. 参数 n

   2.1. n 的用途

   主要用于**扫描控制（scan control），从 while 中是通过 n 来进行条件判断的说明 n 就是用来控制扫描趟数（循环次数）的**。在扫描过程中，如果没有遇到脏 entry 就整个扫描过程持续 log2(n)次，log2(n)的得来是因为`n >>>= 1`，每次 n 右移一位相当于 n 除以 2。如果在扫描过程中遇到脏 entry 的话就会令 n 为当前 hash 表的长度（`n=len`），再扫描 log2(n)趟，注意此时 n 增加无非就是多增加了循环次数从而通过 nextIndex 往后搜索的范围扩大，示意图如下

![cleanSomeSlots示意图.png](http://upload-images.jianshu.io/upload_images/2615789-176285739b74da18.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

按照 n 的初始值，搜索范围为黑线，当遇到了脏 entry，此时 n 变成了哈希数组的长度（n 取值增大），搜索范围 log2(n)增大，红线表示。如果在整个搜索过程没遇到脏 entry 的话，搜索结束，采用这种方式的主要是用于时间效率上的平衡。

2.2. n 的取值
如果是在 set 方法插入新的 entry 后调用（上述情况 2），n 位当前已经插入的 entry 个数 size；如果是在 replaceSateleEntry 方法中调用 n 为哈希表的长度 len。

## 2.2 expungeStaleEntry

如果对输入参数能够理解的话，那么 cleanSomeSlots 方法搜索基本上清除了，但是全部搞定还需要掌握 expungeStaleEntry 方法，当在搜索过程中遇到了脏 entry 的话就会调用该方法去清理掉脏 entry。源码为：

    /**
     * Expunge a stale entry by rehashing any possibly colliding entries
     * lying between staleSlot and the next null slot.  This also expunges
     * any other stale entries encountered before the trailing null.  See
     * Knuth, Section 6.4
     *
     * @param staleSlot index of slot known to have null key
     * @return the index of the next null slot after staleSlot
     * (all between staleSlot and this slot will have been checked
     * for expunging).
     */
    private int expungeStaleEntry(int staleSlot) {
        Entry[] tab = table;
        int len = tab.length;

    	//清除当前脏entry
        // expunge entry at staleSlot
        tab[staleSlot].value = null;
        tab[staleSlot] = null;
        size--;

        // Rehash until we encounter null
        Entry e;
        int i;
    	//2.往后环形继续查找,直到遇到table[i]==null时结束
        for (i = nextIndex(staleSlot, len);
             (e = tab[i]) != null;
             i = nextIndex(i, len)) {
            ThreadLocal<?> k = e.get();
    		//3. 如果在向后搜索过程中再次遇到脏entry，同样将其清理掉
            if (k == null) {
                e.value = null;
                tab[i] = null;
                size--;
            } else {
    			//处理rehash的情况
                int h = k.threadLocalHashCode & (len - 1);
                if (h != i) {
                    tab[i] = null;

                    // Unlike Knuth 6.4 Algorithm R, we must scan until
                    // null because multiple entries could have been stale.
                    while (tab[h] != null)
                        h = nextIndex(h, len);
                    tab[h] = e;
                }
            }
        }
        return i;
    }

该方法逻辑请看注释（第 1,2,3 步），主要做了这么几件事情：

1. 清理当前脏 entry，即将其 value 引用置为 null，并且将 table[staleSlot]也置为 null。value 置为 null 后该 value 域变为不可达，在下一次 gc 的时候就会被回收掉，同时 table[staleSlot]为 null 后以便于存放新的 entry;
2. 从当前 staleSlot 位置向后环形（nextIndex）继续搜索，直到遇到哈希桶（tab[i]）为 null 的时候退出；
3. 若在搜索过程再次遇到脏 entry，继续将其清除。

也就是说该方法，**清理掉当前脏 entry 后，并没有闲下来继续向后搜索，若再次遇到脏 entry 继续将其清理，直到哈希桶（table[i]）为 null 时退出**。因此方法执行完的结果为 **从当前脏 entry（staleSlot）位到返回的 i 位，这中间所有的 entry 不是脏 entry**。为什么是遇到 null 退出呢？原因是存在脏 entry 的前提条件是 **当前哈希桶（table[i]）不为 null**,只是该 entry 的 key 域为 null。如果遇到哈希桶为 null,很显然它连成为脏 entry 的前提条件都不具备。

现在对 cleanSomeSlot 方法做一下总结，其方法执行示意图如下：

![cleanSomeSlots示意图.png](http://upload-images.jianshu.io/upload_images/2615789-176285739b74da18.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

如图所示，cleanSomeSlot 方法主要有这样几点：

1. 从当前位置 i 处（位于 i 处的 entry 一定不是脏 entry）为起点在初始小范围（log2(n)，n 为哈希表已插入 entry 的个数 size）开始向后搜索脏 entry，若在整个搜索过程没有脏 entry，方法结束退出
2. 如果在搜索过程中遇到脏 entryt 通过 expungeStaleEntry 方法清理掉当前脏 entry，并且该方法会返回下一个哈希桶(table[i])为 null 的索引位置为 i。这时重新令搜索起点为索引位置 i，n 为哈希表的长度 len，再次扩大搜索范围为 log2(n')继续搜索。

下面，以一个例子更清晰的来说一下，假设当前 table 数组的情况如下图。

![cleanSomeSlots执行情景图.png](http://upload-images.jianshu.io/upload_images/2615789-217512cee7e45fc7.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

1. 如图当前 n 等于 hash 表的 size 即 n=10，i=1,在第一趟搜索过程中通过 nextIndex,i 指向了索引为 2 的位置，此时 table[2]为 null，说明第一趟未发现脏 entry,则第一趟结束进行第二趟的搜索。

2. 第二趟所搜先通过 nextIndex 方法，索引由 2 的位置变成了 i=3,当前 table[3]!=null 但是该 entry 的 key 为 null，说明找到了一个脏 entry，**先将 n 置为哈希表的长度 len,然后继续调用 expungeStaleEntry 方法**，该方法会将当前索引为 3 的脏 entry 给清除掉（令 value 为 null，并且 table[3]也为 null）,但是**该方法可不想偷懒，它会继续往后环形搜索**，往后会发现索引为 4,5 的位置的 entry 同样为脏 entry，索引为 6 的位置的 entry 不是脏 entry 保持不变，直至 i=7 的时候此处 table[7]位 null，该方法就以 i=7 返回。至此，第二趟搜索结束；
3. 由于在第二趟搜索中发现脏 entry，n 增大为数组的长度 len，因此扩大搜索范围（增大循环次数）继续向后环形搜索；
4. 直到在整个搜索范围里都未发现脏 entry，cleanSomeSlot 方法执行结束退出。

## 2.3 replaceStaleEntry

先来看 replaceStaleEntry 方法，该方法源码为：

    /*
     * @param  key the key
     * @param  value the value to be associated with key
     * @param  staleSlot index of the first stale entry encountered while
     *         searching for key.
     */
    private void replaceStaleEntry(ThreadLocal<?> key, Object value,
                                   int staleSlot) {
        Entry[] tab = table;
        int len = tab.length;
        Entry e;

        // Back up to check for prior stale entry in current run.
        // We clean out whole runs at a time to avoid continual
        // incremental rehashing due to garbage collector freeing
        // up refs in bunches (i.e., whenever the collector runs).

    	//向前找到第一个脏entry
        int slotToExpunge = staleSlot;
        for (int i = prevIndex(staleSlot, len);
             (e = tab[i]) != null;
             i = prevIndex(i, len))
            if (e.get() == null)
    1.          slotToExpunge = i;

        // Find either the key or trailing null slot of run, whichever
        // occurs first
        for (int i = nextIndex(staleSlot, len);
             (e = tab[i]) != null;
             i = nextIndex(i, len)) {
            ThreadLocal<?> k = e.get();

            // If we find key, then we need to swap it
            // with the stale entry to maintain hash table order.
            // The newly stale slot, or any other stale slot
            // encountered above it, can then be sent to expungeStaleEntry
            // to remove or rehash all of the other entries in run.
            if (k == key) {

    			//如果在向后环形查找过程中发现key相同的entry就覆盖并且和脏entry进行交换
    2.            e.value = value;
    3.            tab[i] = tab[staleSlot];
    4.            tab[staleSlot] = e;

                // Start expunge at preceding stale entry if it exists
    			//如果在查找过程中还未发现脏entry，那么就以当前位置作为cleanSomeSlots
    			//的起点
                if (slotToExpunge == staleSlot)
    5.                slotToExpunge = i;
    			//搜索脏entry并进行清理
    6.            cleanSomeSlots(expungeStaleEntry(slotToExpunge), len);
                return;
            }

            // If we didn't find stale entry on backward scan, the
            // first stale entry seen while scanning for key is the
            // first still present in the run.
    		//如果向前未搜索到脏entry，则在查找过程遇到脏entry的话，后面就以此时这个位置
    		//作为起点执行cleanSomeSlots
            if (k == null && slotToExpunge == staleSlot)
    7.            slotToExpunge = i;
        }

        // If key not found, put new entry in stale slot
    	//如果在查找过程中没有找到可以覆盖的entry，则将新的entry插入在脏entry
    8.    tab[staleSlot].value = null;
    9.    tab[staleSlot] = new Entry(key, value);

        // If there are any other stale entries in run, expunge them
    10.    if (slotToExpunge != staleSlot)
    		//执行cleanSomeSlots
    11.        cleanSomeSlots(expungeStaleEntry(slotToExpunge), len);
    }

该方法的逻辑请看注释，下面我结合各种情况详细说一下该方法的执行过程。首先先看这一部分的代码：

    int slotToExpunge = staleSlot;
        for (int i = prevIndex(staleSlot, len);
             (e = tab[i]) != null;
             i = prevIndex(i, len))
            if (e.get() == null)
                slotToExpunge = i;

这部分代码通过 PreIndex 方法实现往前环形搜索脏 entry 的功能，初始时 slotToExpunge 和 staleSlot 相同，若在搜索过程中发现了脏 entry，则更新 slotToExpunge 为当前索引 i。另外，说明 replaceStaleEntry 并不仅仅局限于处理当前已知的脏 entry，它认为在出**现脏 entry 的相邻位置也有很大概率出现脏 entry，所以为了一次处理到位，就需要向前环形搜索，找到前面的脏 entry**。那么根据在向前搜索中是否还有脏 entry 以及在 for 循环后向环形查找中是否找到可覆盖的 entry，我们分这四种情况来充分理解这个方法：

- 1.前向有脏 entry - 1.1 后向环形查找找到可覆盖的 entry
  该情形如下图所示。

![向前环形搜索到脏entry，向后环形查找到可覆盖的entry的情况.png](http://upload-images.jianshu.io/upload_images/2615789-ebc60645134a0342.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
如图，slotToExpunge 初始状态和 staleSlot 相同，当前向环形搜索遇到脏 entry 时，在第 1 行代码中 slotToExpunge 会更新为当前脏 entry 的索引 i，直到遇到哈希桶（table[i]）为 null 的时候，前向搜索过程结束。在接下来的 for 循环中进行后向环形查找，若查找到了可覆盖的 entry，第 2,3,4 行代码先覆盖当前位置的 entry，然后再与 staleSlot 位置上的脏 entry 进行交换。交换之后脏 entry 就更换到了 i 处，最后使用 cleanSomeSlots 方法从 slotToExpunge 为起点开始进行清理脏 entry 的过程

    - 1.2后向环形查找未找到可覆盖的entry
    	该情形如下图所示。
    	![前向环形搜索到脏entry,向后环形未搜索可覆盖entry.png](http://upload-images.jianshu.io/upload_images/2615789-423c8c8dfb2e9557.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
    	如图，slotToExpunge初始状态和staleSlot相同，当前向环形搜索遇到脏entry时，在第1行代码中slotToExpunge会更新为当前脏entry的索引i，直到遇到哈希桶（table[i]）为null的时候，前向搜索过程结束。在接下来的for循环中进行后向环形查找，若没有查找到了可覆盖的entry，哈希桶（table[i]）为null的时候，后向环形查找过程结束。那么接下来在8,9行代码中，将插入的新entry直接放在staleSlot处即可，最后使用cleanSomeSlots方法从slotToExpunge为起点开始进行清理脏entry的过程

- 2.前向没有脏 entry

      	- 2.1后向环形查找找到可覆盖的entry
      		该情形如下图所示。
      		![前向未搜索到脏entry，后向环形搜索到可覆盖的entry.png.png](http://upload-images.jianshu.io/upload_images/2615789-018d077773a019dc.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
      		如图，slotToExpunge初始状态和staleSlot相同，当前向环形搜索直到遇到哈希桶（table[i]）为null的时候，前向搜索过程结束，若在整个过程未遇到脏entry，slotToExpunge初始状态依旧和staleSlot相同。在接下来的for循环中进行后向环形查找，若遇到了脏entry，在第7行代码中更新slotToExpunge为位置i。若查找到了可覆盖的entry，第2,3,4行代码先覆盖当前位置的entry，然后再与staleSlot位置上的脏entry进行交换，交换之后脏entry就更换到了i处。如果在整个查找过程中都还没有遇到脏entry的话，会通过第5行代码，将slotToExpunge更新当前i处，最后使用cleanSomeSlots方法从slotToExpunge为起点开始进行清理脏entry的过程。

      	 - 2.2后向环形查找未找到可覆盖的entry
      		该情形如下图所示。

![前向环形未搜索到脏entry,后向环形查找未查找到可覆盖的entry.png](http://upload-images.jianshu.io/upload_images/2615789-eee96f3eca481ae0.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
如图，slotToExpunge 初始状态和 staleSlot 相同，当前向环形搜索直到遇到哈希桶（table[i]）为 null 的时候，前向搜索过程结束，若在整个过程未遇到脏 entry，slotToExpunge 初始状态依旧和 staleSlot 相同。在接下来的 for 循环中进行后向环形查找，若遇到了脏 entry，在第 7 行代码中更新 slotToExpunge 为位置 i。若没有查找到了可覆盖的 entry，哈希桶（table[i]）为 null 的时候，后向环形查找过程结束。那么接下来在 8,9 行代码中，将插入的新 entry 直接放在 staleSlot 处即可。另外，如果发现 slotToExpunge 被重置，则第 10 行代码 if 判断为 true,就使用 cleanSomeSlots 方法从 slotToExpunge 为起点开始进行清理脏 entry 的过程。

下面用一个实例来有个直观的感受，示例代码就不给出了，代码 debug 时 table 状态如下图所示：

![1.2情况示意图.png](http://upload-images.jianshu.io/upload_images/2615789-f26327e4bc42436a.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

如图所示，当前的 staleSolt 为 i=4，首先先进行前向搜索脏 entry，当 i=3 的时候遇到脏 entry，slotToExpung 更新为 3，当 i=2 的时候 tabel[2]为 null，因此前向搜索脏 entry 的过程结束。然后进行后向环形查找，知道 i=7 的时候遇到 table[7]为 null，结束后向查找过程，并且在该过程并没有找到可以覆盖的 entry。最后只能在 staleSlot（4）处插入新 entry，然后从 slotToExpunge（3）为起点进行 cleanSomeSlots 进行脏 entry 的清理。是不是上面的 1.2 的情况。

这些核心方法，通过源码又给出示例图，应该最终都能掌握了，也还挺有意思的。若觉得不错，对我的辛劳付出能给出鼓励欢迎点赞，给小弟鼓励，在此谢过 :)。

**当我们调用 threadLocal 的 get 方法**时，当 table[i]不是和所要找的 key 相同的话，会继续通过 threadLocalMap 的
getEntryAfterMiss 方法向后环形去找，该方法为：

    private Entry getEntryAfterMiss(ThreadLocal<?> key, int i, Entry e) {
        Entry[] tab = table;
        int len = tab.length;

        while (e != null) {
            ThreadLocal<?> k = e.get();
            if (k == key)
                return e;
            if (k == null)
                expungeStaleEntry(i);
            else
                i = nextIndex(i, len);
            e = tab[i];
        }
        return null;
    }

当 key==null 的时候，即遇到脏 entry 也会调用 expungeStleEntry 对脏 entry 进行清理。

**当我们调用 threadLocal.remove 方法时候**，实际上会调用 threadLocalMap 的 remove 方法，该方法的源码为：

    private void remove(ThreadLocal<?> key) {
        Entry[] tab = table;
        int len = tab.length;
        int i = key.threadLocalHashCode & (len-1);
        for (Entry e = tab[i];
             e != null;
             e = tab[i = nextIndex(i, len)]) {
            if (e.get() == key) {
                e.clear();
                expungeStaleEntry(i);
                return;
            }
        }
    }

同样的可以看出，当遇到了 key 为 null 的脏 entry 的时候，也会调用 expungeStaleEntry 清理掉脏 entry。

从以上 set,getEntry,remove 方法看出，**在 threadLocal 的生命周期里，针对 threadLocal 存在的内存泄漏的问题，都会通过 expungeStaleEntry，cleanSomeSlots,replaceStaleEntry 这三个方法清理掉 key 为 null 的脏 entry**。

## 2.4 为什么使用弱引用？

从文章开头通过 threadLocal,threadLocalMap,entry 的引用关系看起来 threadLocal 存在内存泄漏的问题似乎是因为 threadLocal 是被弱引用修饰的。那为什么要使用弱引用呢？

> 如果使用强引用

假设 threadLocal 使用的是强引用，在业务代码中执行`threadLocalInstance==null`操作，以清理掉 threadLocal 实例的目的，但是因为 threadLocalMap 的 Entry 强引用 threadLocal，因此在 gc 的时候进行可达性分析，threadLocal 依然可达，对 threadLocal 并不会进行垃圾回收，这样就无法真正达到业务逻辑的目的，出现逻辑错误

> 如果使用弱引用

假设 Entry 弱引用 threadLocal，尽管会出现内存泄漏的问题，但是在 threadLocal 的生命周期里（set,getEntry,remove）里，都会针对 key 为 null 的脏 entry 进行处理。

从以上的分析可以看出，使用弱引用的话在 threadLocal 生命周期里会尽可能的保证不出现内存泄漏的问题，达到安全的状态。

## 2.5 Thread.exit()

当线程退出时会执行 exit 方法：

    private void exit() {
        if (group != null) {
            group.threadTerminated(this);
            group = null;
        }
        /* Aggressively null out all reference fields: see bug 4006245 */
        target = null;
        /* Speed the release of some of these resources */
        threadLocals = null;
        inheritableThreadLocals = null;
        inheritedAccessControlContext = null;
        blocker = null;
        uncaughtExceptionHandler = null;
    }

从源码可以看出当线程结束时，会令 threadLocals=null，也就意味着 GC 的时候就可以将 threadLocalMap 进行垃圾回收，换句话说 threadLocalMap 生命周期实际上 thread 的生命周期相同。

# 3. threadLocal 最佳实践

通过这篇文章对 threadLocal 的内存泄漏做了很详细的分析，我们可以完全理解 threadLocal 内存泄漏的前因后果，那么实践中我们应该怎么做？

1. 每次使用完 ThreadLocal，都调用它的 remove()方法，清除数据。
2. 在使用线程池的情况下，没有及时清理 ThreadLocal，不仅是内存泄漏的问题，更严重的是可能导致业务逻辑出现问题。所以，使用 ThreadLocal 就跟加锁完要解锁一样，用完就清理。

> 参考资料

《java 高并发程序设计》
[http://blog.xiaohansong.com/2016/08/06/ThreadLocal-memory-leak/](http://blog.xiaohansong.com/2016/08/06/ThreadLocal-memory-leak/)
