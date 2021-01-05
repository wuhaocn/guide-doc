### 简介

    本模块为java并发包模块
    源码位置rt.jar:java.util.concurrent

### 内容

- 基础
  - [CAS 和 Unsafe](CAS和Unsafe.md)
  - [AQS](AQS.md)
- [atomic 包](1.atomic)
  - [AtomicBoolean](1.atomic/AtomicBoolean.md)
  - [AtomicInteger](1.atomic/AtomicInteger.md)
  - [AtomicIntegerArray](1.atomic/AtomicIntegerArray.md)
  - AtomicIntegerFieldUpdater
  - AtomicLong
  - AtomicLongArray
  - AtomicLongFieldUpdater
  - AtomicMarkableReference
  - AtomicReference
  - AtomicReferenceArray
  - AtomicReferenceFieldUpdater
  - AtomicStampedReference
  - DoubleAccumulator
  - DoubleAdder
  - LongAccumulator
  - LongAdder
  - Striped64
- 队列
  - [BlockingQueue](队列-BlockingQueue.md)
- locks 包
- 集合
  - [集合-CopyOnWriteArrayList 和 CopyOnWriteArraySet](集合-CopyOnWriteArrayList和CopyOnWriteArraySet.md)
  - [集合-ConcurrentSkipListMap 和 ConcurrentSkipListSet](集合-ConcurrentSkipListMap和ConcurrentSkipListSet.md)
- [线程池框架](线程池框架.md)
  - [线程池-ThreadPoolExecutor](线程池-ThreadPoolExecutor.md)
  - 分治框架
    - [Fork-Join 分治编程介绍(一)](<Fork-Join分治编程介绍(一).md>)
    - [Fork-Join 原理深入分析(二)](<Fork-Join原理深入分析(二).md>)
- 常见场景
  - [生产者消费者](示例/生产者消费者.md)
-

### 备注

    知识点大部分来源于网络，如果知识点侵害到您的权益，请反馈。
