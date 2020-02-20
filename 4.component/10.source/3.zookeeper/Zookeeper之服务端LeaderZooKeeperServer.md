##【Zookeeper】源码分析之服务器（三）之LeaderZooKeeperServer
### 一、前言

　　前面分析了ZooKeeperServer源码，由于QuorumZooKeeperServer的源码相对简单，于是直接分析LeaderZooKeeperServer。

### 二、LeaderZooKeeperServer源码分析

#### 2.1 类的继承关系　
```
public class LeaderZooKeeperServer extends QuorumZooKeeperServer {}
```
说明：LeaderZooKeeperServer继承QuorumZooKeeperServer抽象类，其会继承ZooKeeperServer中的很多方法。

#### 2.2 类的属性　　
```
public class LeaderZooKeeperServer extends QuorumZooKeeperServer {
    // 提交请求处理器
    CommitProcessor commitProcessor;
}
```
说明：其只有一个CommitProcessor类，表示提交请求处理器，其在处理链中的位置位于ProposalRequestProcessor之后，ToBeAppliedRequestProcessor之前。

#### 2.3 类的构造函数
```
LeaderZooKeeperServer(FileTxnSnapLog logFactory, QuorumPeer self,
        DataTreeBuilder treeBuilder, ZKDatabase zkDb) throws IOException {
    super(logFactory, self.tickTime, self.minSessionTimeout,
            self.maxSessionTimeout, treeBuilder, zkDb, self);
}
```
说明：其直接调用父类QuorumZooKeeperServer的构造函数，然后再调用ZooKeeperServer的构造函数，逐级构造。

#### 2.4 核心函数分析

##### 1. setupRequestProcessors函数　　

```
protected void setupRequestProcessors() {
    // 创建FinalRequestProcessor
    RequestProcessor finalProcessor = new FinalRequestProcessor(this);
    // 创建ToBeAppliedRequestProcessor
    RequestProcessor toBeAppliedProcessor = new Leader.ToBeAppliedRequestProcessor(
            finalProcessor, getLeader().toBeApplied);
    // 创建CommitProcessor
    commitProcessor = new CommitProcessor(toBeAppliedProcessor,
            Long.toString(getServerId()), false);
    // 启动CommitProcessor
    commitProcessor.start();
    // 创建ProposalRequestProcessor
    ProposalRequestProcessor proposalProcessor = new ProposalRequestProcessor(this,
            commitProcessor);
    // 初始化ProposalProcessor
    proposalProcessor.initialize();
    // firstProcessor为PrepRequestProcessor
    firstProcessor = new PrepRequestProcessor(this, proposalProcessor);
    // 启动PrepRequestProcessor
    ((PrepRequestProcessor)firstProcessor).start();
}
```
说明：该函数表示创建处理链，可以看到其处理链的顺序为PrepRequestProcessor -> ProposalRequestProcessor -> CommitProcessor -> Leader.ToBeAppliedRequestProcessor -> FinalRequestProcessor。

##### 2. registerJMX函数　

```
protected void registerJMX() {
    // register with JMX
    try {
        // 创建DataTreeBean
        jmxDataTreeBean = new DataTreeBean(getZKDatabase().getDataTree());
        // 进行注册
        MBeanRegistry.getInstance().register(jmxDataTreeBean, jmxServerBean);
    } catch (Exception e) {
        LOG.warn("Failed to register with JMX", e);
        jmxDataTreeBean = null;
    }
}
```
说明：该函数用于注册JMX服务，首先使用DataTree初始化DataTreeBean，然后使用DataTreeBean和ServerBean调用register函数进行注册，其源码如下　

```
public void register(ZKMBeanInfo bean, ZKMBeanInfo parent)
    throws JMException
{
    // 确保bean不为空
    assert bean != null;
    String path = null;
    if (parent != null) { // parent(ServerBean)不为空
        // 通过parent从bean2Path中获取path
        path = mapBean2Path.get(parent);
        // 确保path不为空
        assert path != null;
    }
    // 补充为完整的路径
    path = makeFullPath(path, parent);
    if(bean.isHidden())
        return;
    // 使用路径来创建名字
    ObjectName oname = makeObjectName(path, bean);
    try {
        // 注册Server
        mBeanServer.registerMBean(bean, oname);
        // 将bean和对应path放入mapBean2Path
        mapBean2Path.put(bean, path);
        // 将name和bean放入mapName2Bean
        mapName2Bean.put(bean.getName(), bean);
    } catch (JMException e) {
        LOG.warn("Failed to register MBean " + bean.getName());
        throw e;
}
      
```
说明：可以看到会通过parent来获取路径，然后创建名字，然后注册bean，之后将相应字段放入mBeanServer和mapBean2Path中，即完成注册过程。

#####　3. unregisterJMX函数　　

```
protected void unregisterJMX() {
    // unregister from JMX
    try {
        if (jmxDataTreeBean != null) {
            // 取消注册
            MBeanRegistry.getInstance().unregister(jmxDataTreeBean);
        }
    } catch (Exception e) {
        LOG.warn("Failed to unregister with JMX", e);
    }
    jmxDataTreeBean = null;
}
```
说明：该函数用于取消注册JMX服务，其会调用unregister函数，其源码如下　

```
public void unregister(ZKMBeanInfo bean) {
    if(bean==null)
        return;
    // 获取对应路径
    String path=mapBean2Path.get(bean);
    try {
        // 取消注册
        unregister(path,bean);
    } catch (JMException e) {
        LOG.warn("Error during unregister", e);
    }
    // 从mapBean2Path和mapName2Bean中移除bean
    mapBean2Path.remove(bean);
    mapName2Bean.remove(bean.getName());
}
```
说明：unregister与register的过程恰好相反，是移除bean的过程。

### 三、总结

本篇学习了LeaderZooKeeperServer的源码，其源码非常简单，主要涉及到注册和取消注册服务，其大部分逻辑可以直接使用ZooKeeperServer中的方法，也谢谢各位园友的观看~

### 参考
https://www.cnblogs.com/leesf456/p/6514897.html