### 简介
    本模块为java并发包模块
    源码位置rt.jar:java.util.concurrent
### 内容
- atomic包
    - AtomicBoolean 
        - 一般情况下，使用 AtomicBoolean 高效并发处理 “只初始化一次” 的功能要求  
    - AtomicInteger
        - 一般情况下，在应用程序中以原子的方式更新int值。
    - AtomicIntegerArray
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
    详细参考[atomic包](1.atomic)
- locks包

- 
### 备注
    知识点大部分来源于网络，如果知识点侵害到您的权益，请反馈。