# JUC 源码分析-集合篇（二）：CopyOnWriteArrayList 和 CopyOnWriteArraySet

[目录](readme.md)

CopyOnWriteArrayList 是一个线程安全的 ArrayList，通过内部的 volatile 数组和显式锁 ReentrantLock 来实现线程安全。
而 CopyOnWriteArraySet 是线程安全的 Set，它是由 CopyOnWriteArrayList 实现，内部持有一个 CopyOnWriteArrayList 引用，
所有的操作都是由 CopyOnWriteArrayList 来实现的，区别就是 CopyOnWriteArraySet 是无序的，并且不允许存放重复值。由于是一个 Set，所以也不支持随机索引元素。本章我们重点介绍 CopyOnWriteArrayList。

和 ArrayList 或 Set 相比，CopyOnWriteArrayList / CopyOnWriteArraySet 拥有以下特性：

1. 适合元素比较少，并且读取操作高于更新(add/set/remove)操作的场景
2. 由于每次更新需要复制内部数组，所以更新操作开销比较大
3. 内部的迭代器 iterator 使用了“快照”技术，存储了内部数组快照， 所以它的 iterator 不支持 remove、set、add 操作，但是通过迭代器进行并发读取时效率很高。

## 源码解析

### 核心参数

```java
//锁
final transient ReentrantLock lock = new ReentrantLock();

//用于存储元素的内部数组
private transient volatile Object[] array;
```

CopyOnWriteArrayList 实现非常简单。内部使用了一个 volatile 数组(array)来存储数据，保证了多线程环境下的可见性。在更新数据时，都会新建一个数组，并将更新后的数据拷贝到新建的数组中，最后再将该数组赋值给 array。正由于这个原因，涉及到数据更新的操作效率很低。

由于 CopyOnWriteArrayList 源码比较简单，内部都是对数组的操作，所以咱们这里以

add 方法为例，其他方法就不一一分析了。

### add(int index, E element)

```
public void add(int index, E element) {
    final ReentrantLock lock = this.lock;
    lock.lock();
    try {
        Object[] elements = getArray();
        int len = elements.length;
        if (index > len || index < 0)
            throw new IndexOutOfBoundsException("Index: "+index+
                    ", Size: "+len);
        Object[] newElements;
        //计算偏移量
        int numMoved = len - index;
        if (numMoved == 0)
            //作为add(E)处理
            newElements = Arrays.copyOf(elements, len + 1);
        else {
            newElements = new Object[len + 1];
            //调用native方法根据index拷贝原数组的前半段
            System.arraycopy(elements, 0, newElements, 0, index);
            //拷贝后半段
            System.arraycopy(elements, index, newElements, index + 1,
                    numMoved);
        }
        newElements[index] = element;
        setArray(newElements);
    } finally {
        lock.unlock();
    }
}

//System中arrayCopy的实现
public static native void arraycopy(Object src,  int  srcPos, Object dest, int destPos, int length);
```

**说明：** 还是那句话，非常简单，通过
add()方法就可以看出整个 CopyOnWriteArrayList 的实现就是操作内部数组。首先通过
lock 加锁，新建一个原数组长度加 1 的新数组，将原数组（array）的数据拷贝到新数组中，如果给定索引（index）不是原数组最后一个索引，就分两部分拷贝，
然后将给定元素放到新数组中给定索引处；最后，将新数组赋值给 array。

### 小结

在整个 java.util.concurrent 框架里，这两兄弟可以说是最简单的两个类了。操作直观，没有复杂的运算逻辑。本章重点：**CopyOnWriteArrayList 是通过拷贝数组来实现内部元素操作的。**
