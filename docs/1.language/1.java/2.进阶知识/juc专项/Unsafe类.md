## 死磕 java 魔法类之 Unsafe 解析

原创 丹卿 [彤哥读源码]()

**彤哥读源码** ![]()

微信号

功能介绍 彤哥读源码，与彤哥一起畅游源码的海洋。
_2019-05-06_

🖕 欢迎关注我的公众号“彤哥读源码”，查看更多源码系列文章, 与彤哥一起畅游源码的海洋。

（手机横屏看源码更方便）

## 问题

（1）Unsafe 是什么？

（2）Unsafe 只有 CAS 的功能吗？

（3）Unsafe 为什么是不安全的？

（4）怎么使用 Unsafe？

## 简介

本章是 java 并发包专题的第一章，但是第一篇写的却不是 java 并发包中类，而是 java 中的魔法类 sun.misc.Unsafe。

Unsafe 为我们提供了访问底层的机制，这种机制仅供 java 核心类库使用，而不应该被普通用户使用。

但是，为了更好地了解 java 的生态体系，我们应该去学习它，去了解它，不求深入到底层的 C/C++代码，但求能了解它的基本功能。

## 获取 Unsafe 的实例

查看 Unsafe 的源码我们会发现它提供了一个 getUnsafe()的静态方法。

```
1.
@CallerSensitive
1.
publicstaticUnsafegetUnsafe(){
1.
Classvar0=Reflection.getCallerClass();
1.
if(!VM.isSystemDomainLoader(var0.getClassLoader())){
1.
thrownewSecurityException("Unsafe");
1.
}else{
1.
returntheUnsafe;
1.
}
1.
}
```

但是，如果直接调用这个方法会抛出一个 SecurityException 异常，这是因为 Unsafe 仅供 java 内部类使用，外部类不应该使用它。

那么，我们就没有方法了吗？

当然不是，我们有反射啊！查看源码，我们发现它有一个属性叫 theUnsafe，我们直接通过反射拿到它即可。

```
1.
publicclassUnsafeTest{
1.
publicstaticvoidmain(String[]args)throwsNoSuchFieldException,IllegalAccessException{
1.
Fieldf=Unsafe.class.getDeclaredField("theUnsafe");
1.
f.setAccessible(true);
1.
Unsafeunsafe=(Unsafe)f.get(null);
1.
}
1.
}
```

## 使用 Unsafe 实例化一个类

假如我们有一个简单的类如下：

```
1.
classUser{
1.
intage;
1.
1.
publicUser(){
1.
this.age=10;
1.
}
1.
}
```

如果我们通过构造方法实例化这个类，age 属性将会返回 10。

```
1.
Useruser1=newUser();
1.
// 打印10
1.
System.out.println(user1.age);
```

如果我们调用 Unsafe 来实例化呢？

```
1.
Useruser2=(User)unsafe.allocateInstance(User.class);
1.
// 打印0
1.
System.out.println(user2.age);
```

age 将返回 0，因为

Unsafe.allocateInstance()
只会给对象分配内存，并不会调用构造方法，所以这里只会返回 int 类型的默认值 0。

## 修改私有字段的值

使用 Unsafe 的 putXXX()方法，我们可以修改任意私有字段的值。

```
1.
publicclassUnsafeTest{
1.
publicstaticvoidmain(String[]args)throwsException{
1.
Fieldf=Unsafe.class.getDeclaredField("theUnsafe");
1.
f.setAccessible(true);
1.
Unsafeunsafe=(Unsafe)f.get(null);
1.
1.
Useruser=newUser();
1.
Fieldage=user.getClass().getDeclaredField("age");
1.
unsafe.putInt(user,unsafe.objectFieldOffset(age),20);
1.
1.
// 打印20
1.
System.out.println(user.getAge());
1.
}
1.
}
1.
1.
classUser{
1.
privateintage;
1.
1.
publicUser(){
1.
this.age=10;
1.
}
1.
1.
publicintgetAge(){
1.
returnage;
1.
}
1.
}
```

一旦我们通过反射调用得到字段 age，我们就可以使用 Unsafe 将其值更改为任何其他 int 值。（当然，这里也可以通过反射直接修改）

## 抛出 checked 异常

我们知道如果代码抛出了 checked 异常，要不就使用 try...catch 捕获它，要不就在方法签名上定义这个异常，但是，通过 Unsafe 我们可以抛出一个 checked 异常，同时却不用捕获或在方法签名上定义它。

```
1.
// 使用正常方式抛出IOException需要定义在方法签名上往外抛
1.
publicstaticvoidreadFile()throwsIOException{
1.
thrownewIOException();
1.
}
1.
// 使用Unsafe抛出异常不需要定义在方法签名上往外抛
1.
publicstaticvoidreadFileUnsafe(){
1.
unsafe.throwException(newIOException());
1.
}
```

## 使用堆外内存

如果进程在运行过程中 JVM 上的内存不足了，会导致频繁的进行 GC。理想情况下，我们可以考虑使用堆外内存，这是一块不受 JVM 管理的内存。

使用 Unsafe 的 allocateMemory()我们可以直接在堆外分配内存，这可能非常有用，但我们要记住，这个内存不受 JVM 管理，因此我们要调用 freeMemory()方法手动释放它。

假设我们要在堆外创建一个巨大的 int 数组，我们可以使用 allocateMemory()方法来实现：

```
1.
classOffHeapArray{
1.
// 一个int等于4个字节
1.
privatestaticfinalintINT=4;
1.
privatelongsize;
1.
privatelongaddress;
1.
1.
privatestaticUnsafeunsafe;
1.
static{
1.
try{
1.
Fieldf=Unsafe.class.getDeclaredField("theUnsafe");
1.
f.setAccessible(true);
1.
unsafe=(Unsafe)f.get(null);
1.
}catch(NoSuchFieldExceptione){
1.
e.printStackTrace();
1.
}catch(IllegalAccessExceptione){
1.
e.printStackTrace();
1.
}
1.
}
1.
1.
// 构造方法，分配内存
1.
publicOffHeapArray(longsize){
1.
this.size=size;
1.
// 参数字节数
1.
address=unsafe.allocateMemory(size/*INT);
1.
}
1.
1.
// 获取指定索引处的元素
1.
publicintget(longi){
1.
returnunsafe.getInt(address+i/*INT);
1.
}
1.
// 设置指定索引处的元素
1.
publicvoidset(longi,intvalue){
1.
unsafe.putInt(address+i/*INT,value);
1.
}
1.
// 元素个数
1.
publiclongsize(){
1.
returnsize;
1.
}
1.
// 释放堆外内存
1.
publicvoidfreeMemory(){
1.
unsafe.freeMemory(address);
1.
}
1.
}
```

在构造方法中调用 allocateMemory()分配内存，在使用完成后调用 freeMemory()释放内存。

使用方式如下：

```
1.
OffHeapArrayoffHeapArray=newOffHeapArray(4);
1.
offHeapArray.set(0,1);
1.
offHeapArray.set(1,2);
1.
offHeapArray.set(2,3);
1.
offHeapArray.set(3,4);
1.
offHeapArray.set(2,5);// 在索引2的位置重复放入元素
1.
1.
intsum=0;
1.
for(inti=0;i<offHeapArray.size();i++){
1.
sum+=offHeapArray.get(i);
1.
}
1.
// 打印12
1.
System.out.println(sum);
1.
1.
offHeapArray.freeMemory();
```

最后，一定要记得调用 freeMemory()将内存释放回操作系统。

## CompareAndSwap 操作

JUC 下面大量使用了 CAS 操作，它们的底层是调用的 Unsafe 的 CompareAndSwapXXX()方法。这种方式广泛运用于无锁算法，与 java 中标准的悲观锁机制相比，它可以利用 CAS 处理器指令提供极大的加速。

比如，我们可以基于 Unsafe 的 compareAndSwapInt()方法构建线程安全的计数器。

```
1.
classCounter{
1.
privatevolatileintcount=0;
1.
1.
privatestaticlongoffset;
1.
privatestaticUnsafeunsafe;
1.
static{
1.
try{
1.
Fieldf=Unsafe.class.getDeclaredField("theUnsafe");
1.
f.setAccessible(true);
1.
unsafe=(Unsafe)f.get(null);
1.
offset=unsafe.objectFieldOffset(Counter.class.getDeclaredField("count"));
1.
}catch(NoSuchFieldExceptione){
1.
e.printStackTrace();
1.
}catch(IllegalAccessExceptione){
1.
e.printStackTrace();
1.
}
1.
}
1.
1.
publicvoidincrement(){
1.
intbefore=count;
1.
// 失败了就重试直到成功为止
1.
while(!unsafe.compareAndSwapInt(this,offset,before,before+1)){
1.
before=count;
1.
}
1.
}
1.
1.
publicintgetCount(){
1.
returncount;
1.
}
1.
}
```

我们定义了一个 volatile 的字段 count，以便对它的修改所有线程都可见，并在类加载的时候获取 count 在类中的偏移地址。

在 increment()方法中，我们通过调用 Unsafe 的 compareAndSwapInt()方法来尝试更新之前获取到的 count 的值，如果它没有被其它线程更新过，则更新成功，否则不断重试直到成功为止。

我们可以通过使用多个线程来测试我们的代码：

```
1.
Countercounter=newCounter();
1.
ExecutorServicethreadPool=Executors.newFixedThreadPool(100);
1.
1.
// 起100个线程，每个线程自增10000次
1.
IntStream.range(0,100)
1.
.forEach(i->threadPool.submit(()->IntStream.range(0,10000)
1.
.forEach(j->counter.increment())));
1.
1.
threadPool.shutdown();
1.
1.
Thread.sleep(2000);
1.
1.
// 打印1000000
1.
System.out.println(counter.getCount());
```

## park/unpark

JVM 在上下文切换的时候使用了 Unsafe 中的两个非常牛逼的方法 park()和 unpark()。

当一个线程正在等待某个操作时，JVM 调用 Unsafe 的 park()方法来阻塞此线程。

当阻塞中的线程需要再次运行时，JVM 调用 Unsafe 的 unpark()方法来唤醒此线程。

我们之前在分析 java 中的集合时看到了大量的 LockSupport.park()/unpark()，它们底层都是调用的 Unsafe 的这两个方法。

## 总结

使用 Unsafe 几乎可以操作一切：

（1）实例化一个类；

（2）修改私有字段的值；

（3）抛出 checked 异常；

（4）使用堆外内存；

（5）CAS 操作；

（6）阻塞/唤醒线程；

## 彩蛋

论实例化一个类的方式？

（1）通过构造方法实例化一个类；

（2）通过 Class 实例化一个类；

（3）通过反射实例化一个类；

（4）通过克隆实例化一个类；

（5）通过反序列化实例化一个类；

（6）通过 Unsafe 实例化一个类；

```
1.
publicclassInstantialTest{
1.
1.
privatestaticUnsafeunsafe;
1.
static{
1.
try{
1.
Fieldf=Unsafe.class.getDeclaredField("theUnsafe");
1.
f.setAccessible(true);
1.
unsafe=(Unsafe)f.get(null);
1.
}catch(NoSuchFieldExceptione){
1.
e.printStackTrace();
1.
}catch(IllegalAccessExceptione){
1.
e.printStackTrace();
1.
}
1.
}
1.
1.
publicstaticvoidmain(String[]args)throwsException{
1.
// 1. 构造方法
1.
Useruser1=newUser();
1.
// 2. Class，里面实际也是反射
1.
Useruser2=User.class.newInstance();
1.
// 3. 反射
1.
Useruser3=User.class.getConstructor().newInstance();
1.
// 4. 克隆
1.
Useruser4=(User)user1.clone();
1.
// 5. 反序列化
1.
Useruser5=unserialize(user1);
1.
// 6. Unsafe
1.
Useruser6=(User)unsafe.allocateInstance(User.class);
1.
1.
System.out.println(user1.age);
1.
System.out.println(user2.age);
1.
System.out.println(user3.age);
1.
System.out.println(user4.age);
1.
System.out.println(user5.age);
1.
System.out.println(user6.age);
1.
}
1.
1.
privatestaticUserunserialize(Useruser1)throwsException{
1.
ObjectOutputStreamoos=newObjectOutputStream(newFileOutputStream("D://object.txt"));
1.
oos.writeObject(user1);
1.
oos.close();
1.
1.
ObjectInputStreamois=newObjectInputStream(newFileInputStream("D://object.txt"));
1.
// 反序列化
1.
Useruser5=(User)ois.readObject();
1.
ois.close();
1.
returnuser5;
1.
}
1.
1.
staticclassUserimplementsCloneable,Serializable{
1.
privateintage;
1.
1.
publicUser(){
1.
this.age=10;
1.
}
1.
1.
@Override
1.
protectedObjectclone()throwsCloneNotSupportedException{
1.
returnsuper.clone();
1.
}
1.
}
1.
}
```

![]()

丹卿

能吃 🍗 不？

![赞赏二维码]() **微信扫一扫赞赏作者** [赞赏]()
[]() 人赞赏
上一页 [1]()/3 下一页

长按二维码向我转账

能吃 🍗 不？
![]()

受苹果公司新规定影响，微信 iOS 版的赞赏功能被关闭，可通过二维码转账支持公众号。

[阅读原文]()

阅读
在看

已同步到看一看[写下你的想法]()

前往“发现”-“看一看”浏览“朋友在看”

![]()
前往看一看
**看一看入口已关闭**

在“设置”-“通用”-“发现页管理”打开“看一看”入口
[我知道了]()

已发送
取消

### 发送到看一看

发送

死磕 java 魔法类之 Unsafe 解析
最多 200 字，当前共字

发送中
