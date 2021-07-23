## 1.Caffeine 简介

Caffeine 是基于 JAVA 1.8 Version 的高性能缓存库。
Caffeine 提供的内存缓存使用参考 Google guava 的 API。
Caffeine 是基于 Google Guava Cache 设计经验上改进的成果。

## 2.并发测试

![](https://upload-images.jianshu.io/upload_images/11772383-72ffd52908312e82.png?imageMogr2/auto-orient/strip|imageView2/2/w/661/format/webp)
![](https://upload-images.jianshu.io/upload_images/11772383-47e5e6b30e813c8e.png?imageMogr2/auto-orient/strip|imageView2/2/w/770/format/webp)
![](https://upload-images.jianshu.io/upload_images/11772383-6d9cf1406915a611.png?imageMogr2/auto-orient/strip|imageView2/2/w/714/format/webp)

## 3.特性

Caffeine 可以通过建造者模式灵活的组合以下特性：

- 通过异步自动加载实体到缓存中

- 基于大小的回收策略

- 基于时间的回收策略

- 自动刷新

- key 自动封装虚引用

- value 自动封装弱引用或软引用

- 实体过期或被删除的通知

- 写入外部资源

- 统计累计访问缓存

## 4.填充策略（Population）

Caffeine 提供了 3 种加载策略:手动加载，同步加载，异步加载

## 5.驱逐策略（Eviction）

- 基于大小
- 基于缓存容量
- 基于权重
- 基于时间
  · 实体被访问之后，在实体被读或被写后的一段时间后过期

## 6.基于软引用

java 种有四种引用：强引用，软引用，弱引用和虚引用，caffeine 可以将值封装成弱引用或软引用。

软引用：如果一个对象只具有软引用，则内存空间足够，垃圾回收器就不会回收它；
如果内存空间不足了，就会回收这些对象的内存。

弱引用：弱引用的对象拥有更短暂的生命周期。在垃圾回收器线程扫描它所管辖的内存区域的过程中，
一旦发现了只具有弱引用的对象，不管当前内存空间足够与否，都会回收它的内存

## 7.自动刷新

## 8.监控

## 9.移除通知

## 10.淘汰算法

作者：但时间也偷换概念
链接：https://www.jianshu.com/p/3434991ad075
来源：简书
著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。
