### 【Zookeeper】源码分析之 Leader 选举（一）

### 一、前言

分析完了 Zookeeper 中的网络机制后，接着来分析 Zookeeper 中一个更为核心的模块，Leader 选举。

### 二、总结框架图

对于 Leader 选举，其总体框架图如下图所示

说明：

选举的父接口为 Election，其定义了 lookForLeader 和 shutdown 两个方法，lookForLeader 表示寻找 Leader，shutdown 则表示关闭，如关闭服务端之间的连接。

AuthFastLeaderElection，同 FastLeaderElection 算法基本一致，只是在消息中加入了认证信息，其在 3.4.0 之后的版本中已经不建议使用。

FastLeaderElection，其是标准的 fast paxos 算法的实现，基于 TCP 协议进行选举。

LeaderElection，也表示一种选举算法，其在 3.4.0 之后的版本中已经不建议使用。

### 三、Election 源码分析

```
public interface Election {
    public Vote lookForLeader() throws InterruptedException;
    public void shutdown();
}
```

说明：可以看到 Election 接口定义的方法相当简单。

### 四、总结

本篇讲解了 Leader 选举的 Election 接口，其是 Leader 选举的父接口，关于具体子类的实现，之后会详细进行分析，谢谢各位园友的观看~
