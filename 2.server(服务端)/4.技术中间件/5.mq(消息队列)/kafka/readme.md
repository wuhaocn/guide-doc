### kafka介绍

kafka is a distributed,partitioned,replicated commit logservice。它提供了类似于JMS的特性，但是在设计实现上完全不同，此外它并不是JMS规范的实现。kafka对消息保存时根据Topic进行归类，发送消息者成为Producer,消息接受者成为Consumer,此外kafka集群有多个kafka实例组成，每个实例(server)成为broker。无论是kafka集群，还是producer和consumer都依赖于zookeeper来保证系统可用性集群保存一些meta信息。详细信息请参见见[官网](http://kafka.apache.org/)
### kafka适用场景

### kafka系统拓扑

### kafka关键概念
    生产者
    消费者
    


参考：
https://www.cnblogs.com/likehua/p/3999538.html