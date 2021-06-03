# JVM 源码分析系列

[![](https://upload.jianshu.io/users/upload_avatars/2184951/e504c85fb1dc.jpg?imageMogr2/auto-orient/strip|imageView2/1/w/96/h/96/format/webp)]()

[占小狼]()[![  ](https://upload.jianshu.io/user_badge/11f8cfa8-ec9f-4f82-be92-d6a39f61b5c1)](http://www.jianshu.com/p/d1d89ed69098)关注

32019.01.27 10:35:22 字数 363 阅读 3,753

# JVM G1 算法系列

- [G1 垃圾收集器介绍](https://www.jianshu.com/p/0f1f5adffdc1)
- [G1 垃圾收集器之 RSet](https://www.jianshu.com/p/870abddaba41)
- [G1 垃圾收集器之 SATB](https://www.jianshu.com/p/9e70097807ba)
- [G1 垃圾收集器之对象分配过程](https://www.jianshu.com/p/a0efa489b99f)

# ZGC 系列

- [ZGC，一个超乎想象的垃圾收集器](https://www.jianshu.com/p/6f89fd5842bf)
- [ZGC 什么时候进行垃圾回收](https://www.jianshu.com/p/b5fb06ffbb90)

# JVM 源码分析系列

- [深入分析 Object.finalize 方法的实现原理](https://www.jianshu.com/p/9d2788fffd5f)
- [JVM 源码分析之 Object.wait/notify 实现](https://www.jianshu.com/p/f4454164c017)
- [JVM 源码分析之 java 对象头实现](https://www.jianshu.com/p/9c19eb0ea4d8)
- [JVM 源码分析之 synchronized 实现](https://www.jianshu.com/p/c5058b6fe8e5)
- [JVM 源码分析之 Java 类的加载过程](https://www.jianshu.com/p/252e27863822)
- [JVM 源码分析之 Java 对象的创建过程](https://www.jianshu.com/p/0009aaac16ed)
- [JVM 源码分析之 JVM 启动流程](https://www.jianshu.com/p/b91258bc08ac)
- [JVM 源码分析之堆内存的初始化](https://www.jianshu.com/p/0f7bed2df952)
- [JVM 源码分析之 Java 对象的内存分配](https://www.jianshu.com/p/e56c808b6c8a)
- [JVM 源码分析之如何触发并执行 GC 线程](https://www.jianshu.com/p/1544d3011ddb)
- [JVM 源码分析之垃圾收集的执行过程](https://www.jianshu.com/p/04eff13f3707)
- [JVM 源码分析之新生代 DefNewGeneration 的实现](https://www.jianshu.com/p/2b64294fa1bd)
- [JVM 源码分析之老年代 TenuredGeneration 的垃圾回收算法实现](https://www.jianshu.com/p/29c20f0684d0)
- [JVM 源码分析之安全点 safepoint](https://www.jianshu.com/p/c79c5e02ebe6)
  [JVM 源码分析之线程局部缓存 TLAB](https://www.jianshu.com/p/cd85098cca39)
- [JVM 源码分析之不要被 GC 日志的表面现象迷惑](https://www.jianshu.com/p/1f2fd54808e2)
- [JVM 源码分析之 YGC 的来龙去脉](https://www.jianshu.com/p/9af1a63a33c3)
- [JVM 源码分析之跨代引用 CardTable](https://www.jianshu.com/p/5037459097ee)
- [JVM 源码分析之 System.gc()](https://www.jianshu.com/p/be8740726cef)
- [JVM 源码分析之 GC locker 深度分析](https://www.jianshu.com/p/6d664f026508)
- [JVM 源码分析之由 JNI 操作引起的迷惑性 GC](https://www.jianshu.com/p/94bd5864f89c)
- [从 JVM 角度看看 Java 的 clone 操作](https://www.jianshu.com/p/309f80f33190)
  25 人点赞

[java 进阶干货]()

"小礼物走一走，来简书关注我"赞赏支持还没有人赞赏，支持一下
[![  ](https://upload.jianshu.io/users/upload_avatars/2184951/e504c85fb1dc.jpg?imageMogr2/auto-orient/strip|imageView2/1/w/100/h/100/format/webp)]()

[占小狼]("占小狼")[![  ](https://upload.jianshu.io/user_badge/11f8cfa8-ec9f-4f82-be92-d6a39f61b5c1)](http://www.jianshu.com/p/d1d89ed69098)如果读完觉得有收获的话，欢迎关注我的公众号：占小狼的博客 https://dwz.cn/D8Q...

总资产 212 (约 16.81 元)共写了 22.6W 字获得 16,872 个赞共 25,121 个粉丝
关注
