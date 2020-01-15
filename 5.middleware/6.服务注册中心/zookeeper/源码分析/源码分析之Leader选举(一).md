### 【Zookeeper】源码分析之Leader选举（一）
### 一、前言

分析完了Zookeeper中的网络机制后，接着来分析Zookeeper中一个更为核心的模块，Leader选举。

### 二、总结框架图

对于Leader选举，其总体框架图如下图所示

说明：

选举的父接口为Election，其定义了lookForLeader和shutdown两个方法，lookForLeader表示寻找Leader，shutdown则表示关闭，如关闭服务端之间的连接。

AuthFastLeaderElection，同FastLeaderElection算法基本一致，只是在消息中加入了认证信息，其在3.4.0之后的版本中已经不建议使用。

FastLeaderElection，其是标准的fast paxos算法的实现，基于TCP协议进行选举。

LeaderElection，也表示一种选举算法，其在3.4.0之后的版本中已经不建议使用。

### 三、Election源码分析

```
public interface Election {
    public Vote lookForLeader() throws InterruptedException;
    public void shutdown();
}
```

说明：可以看到Election接口定义的方法相当简单。

### 四、总结

本篇讲解了Leader选举的Election接口，其是Leader选举的父接口，关于具体子类的实现，之后会详细进行分析，谢谢各位园友的观看~