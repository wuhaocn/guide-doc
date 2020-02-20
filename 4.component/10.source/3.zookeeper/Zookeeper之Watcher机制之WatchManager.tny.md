【Zookeeper】源码分析之Watcher机制（二）之WatchManager
一、前言

　　前面已经分析了Watcher机制中的第一部分，即在org.apache.zookeeper下的相关类，接着来分析org.apache.zookeeper.server下的WatchManager类。

二、WatchManager源码分析

　　2.1 类的属性　

```
public class WatchManager {
    // Logger
    private static final Logger LOG = LoggerFactory.getLogger(WatchManager.class);

    // watcher表
    private final HashMap<String, HashSet<Watcher>> watchTable =
        new HashMap<String, HashSet<Watcher>>();

    // watcher到节点路径的映射
    private final HashMap<Watcher, HashSet<String>> watch2Paths =
        new HashMap<Watcher, HashSet<String>>();
}
```
　　说明：WatcherManager类用于管理watchers和相应的触发器。watchTable表示从节点路径到watcher集合的映射，而watch2Paths则表示从watcher到所有节点路径集合的映射。

　　2.2 核心方法分析

　　1. size方法　

```
    public synchronized int size(){
        int result = 0;
        for(Set<Watcher> watches : watchTable.values()) { // 遍历watchTable所有的值集合(HashSet<Watcher>集合)
            // 每个集合大小累加
            result += watches.size();
        }
        // 返回结果
        return result;
    }
```
　　说明：可以看到size方法是同步的，因此在多线程环境下是安全的，其主要作用是获取watchTable的大小，即遍历watchTable的值集合。

　　2. addWatch方法　

```
    public synchronized void addWatch(String path, Watcher watcher) {
        // 根据路径获取对应的所有watcher
        HashSet<Watcher> list = watchTable.get(path);
        if (list == null) { // 列表为空
            // don't waste memory if there are few watches on a node
            // rehash when the 4th entry is added, doubling size thereafter
            // seems like a good compromise
            // 新生成watcher集合
            list = new HashSet<Watcher>(4);
            // 存入watcher表
            watchTable.put(path, list);
        }
        // 将watcher直接添加至watcher集合
        list.add(watcher);

        // 通过watcher获取对应的所有路径
        HashSet<String> paths = watch2Paths.get(watcher);
        if (paths == null) { // 路径为空
            // cnxns typically have many watches, so use default cap here
            // 新生成hash集合
            paths = new HashSet<String>();
            // 将watcher和对应的paths添加至映射中
            watch2Paths.put(watcher, paths);
        }
        // 将路径添加至paths集合
        paths.add(path);
    }
```
　　说明：addWatch方法同样是同步的，其大致流程如下

　　① 通过传入的path（节点路径）从watchTable获取相应的watcher集合，进入②

　　② 判断①中的watcher是否为空，若为空，则进入③，否则，进入④

　　③ 新生成watcher集合，并将路径path和此集合添加至watchTable中，进入④

　　④ 将传入的watcher添加至watcher集合，即完成了path和watcher添加至watchTable的步骤，进入⑤

　　⑤ 通过传入的watcher从watch2Paths中获取相应的path集合，进入⑥ 

　　⑥ 判断path集合是否为空，若为空，则进入⑦，否则，进入⑧

　　⑦ 新生成path集合，并将watcher和paths添加至watch2Paths中，进入⑧

　　⑧ 将传入的path（节点路径）添加至path集合，即完成了path和watcher添加至watch2Paths的步骤。

　　3. removeWatcher方法　　

```
    public synchronized void removeWatcher(Watcher watcher) {
        // 从wach2Paths中移除watcher，并返回watcher对应的path集合
        HashSet<String> paths = watch2Paths.remove(watcher);
        if (paths == null) { // 集合为空，直接返回
            return;
        }
        for (String p : paths) { // 遍历路径集合
            // 从watcher表中根据路径取出相应的watcher集合
            HashSet<Watcher> list = watchTable.get(p);
            if (list != null) { // 若集合不为空
                // 从list中移除该watcher
                list.remove(watcher);
                if (list.size() == 0) { // 移除后list为空，则从watch表中移出
                    watchTable.remove(p);
                }
            }
        }
    }
```
　　说明：removeWatcher用作从watch2Paths和watchTable中中移除该watcher，其大致步骤如下

　　① 从watch2Paths中移除传入的watcher，并且返回该watcher对应的路径集合，进入②

　　② 判断返回的路径集合是否为空，若为空，直接返回，否则，进入③

　　③ 遍历②中的路径集合，对每个路径，都从watchTable中取出与该路径对应的watcher集合，进入④

　　④ 若③中的watcher集合不为空，则从该集合中移除watcher，并判断移除元素后的集合大小是否为0，若为0，进入⑤

　　⑤ 从watchTable中移除路径。

　　4. triggerWatch方法

```
    public Set<Watcher> triggerWatch(String path, EventType type, Set<Watcher> supress) {
        // 根据事件类型、连接状态、节点路径创建WatchedEvent
        WatchedEvent e = new WatchedEvent(type,
                KeeperState.SyncConnected, path);
                
        // watcher集合
        HashSet<Watcher> watchers;
        synchronized (this) { // 同步块
            // 从watcher表中移除path，并返回其对应的watcher集合
            watchers = watchTable.remove(path);
            if (watchers == null || watchers.isEmpty()) { // watcher集合为空
                if (LOG.isTraceEnabled()) { 
                    ZooTrace.logTraceMessage(LOG,
                            ZooTrace.EVENT_DELIVERY_TRACE_MASK,
                            "No watchers for " + path);
                }
                // 返回
                return null;
            }
            for (Watcher w : watchers) { // 遍历watcher集合
                // 根据watcher从watcher表中取出路径集合
                HashSet<String> paths = watch2Paths.get(w);
                if (paths != null) { // 路径集合不为空
                    // 则移除路径
                    paths.remove(path);
                }
            }
        }
        for (Watcher w : watchers) { // 遍历watcher集合
            if (supress != null && supress.contains(w)) { // supress不为空并且包含watcher，则跳过
                continue;
            }
            // 进行处理
            w.process(e);
        }
        return watchers;
    }
```
　　说明：该方法主要用于触发watch事件，并对事件进行处理。其大致步骤如下

　　① 根据事件类型、连接状态、节点路径创建WatchedEvent，进入②

　　② 从watchTable中移除传入的path对应的键值对，并且返回path对应的watcher集合，进入③

　　③ 判断watcher集合是否为空，若为空，则之后会返回null，否则，进入④

　　④ 遍历②中的watcher集合，对每个watcher，从watch2Paths中取出path集合，进入⑤

　　⑤ 判断④中的path集合是否为空，若不为空，则从集合中移除传入的path。进入⑥

　　⑥ 再次遍历watcher集合，对每个watcher，若supress不为空并且包含了该watcher，则跳过，否则，进入⑦

　　⑦ 调用watcher的process方法进行相应处理，之后返回watcher集合。

　　5. dumpWatches方法

```
    public synchronized void dumpWatches(PrintWriter pwriter, boolean byPath) {
        if (byPath) { // 控制写入watchTable或watch2Paths
            for (Entry<String, HashSet<Watcher>> e : watchTable.entrySet()) { // 遍历每个键值对
                // 写入键
                pwriter.println(e.getKey());
                for (Watcher w : e.getValue()) { // 遍历值(HashSet<Watcher>)
                    pwriter.print("\t0x");
                    pwriter.print(Long.toHexString(((ServerCnxn)w).getSessionId()));
                    pwriter.print("\n");
                }
            }
        } else {
            for (Entry<Watcher, HashSet<String>> e : watch2Paths.entrySet()) { // 遍历每个键值对
                // 写入"0x"
                pwriter.print("0x");
                pwriter.println(Long.toHexString(((ServerCnxn)e.getKey()).getSessionId()));
                for (String path : e.getValue()) { // 遍历值(HashSet<String>)
                    // 
                    pwriter.print("\t");
                    pwriter.println(path);
                }
            }
        }
    }
```
　　说明：dumpWatches用作将watchTable或watch2Paths写入磁盘。

三、总结

　　WatchManager类用作管理watcher、其对应的路径以及触发器，其方法都是针对两个映射的操作，相对简单，也谢谢各位园友的观看~　
四、参考
https://www.cnblogs.com/leesf456/p/6288709.html