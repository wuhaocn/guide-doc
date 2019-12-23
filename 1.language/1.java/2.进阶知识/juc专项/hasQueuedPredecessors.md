# [ReentrantLock类的hasQueuedPredecessors方法和head节点的含义](https://www.cnblogs.com/kumu/p/10659835.html)

部分启发来源自文章：[Java并发编程--Lock](https://www.cnblogs.com/zaizhoumo/p/7756310.html)

## PART 1

1、如果h==t成立，h和t均为null或是同一个具体的节点，无后继节点，返回false。
2、如果h!=t成立，head.next是否为null，如果为null，返回true。什么情况下h!=t的同时h.next==null？？，有其他线程第一次正在入队时，可能会出现。见AQS的enq方法，compareAndSetHead(node)完成，还没执行tail=head语句时，此时tail=null,head=newNode,head.next=null。
3、如果h!=t成立，head.next != null，则判断head.next是否是当前线程，如果是返回false，否则返回true（head节点是获取到锁的节点，但是任意时刻head节点可能占用着锁，也可能释放了锁（unlock()）,未被阻塞的head.next节点对应的线程在任意时刻都是有必要去尝试获取锁）

```
public final boolean hasQueuedPredecessors() {
    Node t = tail; 
    Node h = head;
    Node s;
    return h != t &&
        ((s = h.next) == null || s.thread != Thread.currentThread());
}
```


## PART 2　　解释为什么要判断：s.thread != Thread.currentThread()

根据ReentrantLock的解锁流程，也就是下面四个方法，可以看到当线程释放锁之后还是会在队列的head节点，只是把next指针指向下一个可用节点，并唤醒它
也就是说任意时刻，head节点可能占用着锁（除了第一次执行enq()入队列时，head仅仅是个new Node()，没有实际对应任何线程，但是却“隐式”对应第一个获得锁但并未入队列的线程，和后续的head在含义上保持一致），也可能释放了锁（unlock()）,未被阻塞的head.next节点对应的线程在任意时刻都是有必要去尝试获取锁
```
 public void unlock() {
     sync.release(1);
 }
```

尝试释放锁，释放成功后把head.next从阻塞中唤醒

```
 public final boolean release(int arg) {
     if (tryRelease(arg)) {
         Node h = head;
         if (h != null && h.waitStatus != 0)
             unparkSuccessor(h);
         return true;
     }
     return false;
 }
```


把state-1
当state=0时，把exclusiveOwnerThread设置为null，说明线程释放了锁

```
  protected final boolean tryRelease(int releases) {
      int c = getState() - releases;
      if (Thread.currentThread() != getExclusiveOwnerThread())
          throw new IllegalMonitorStateException();
      boolean free = false;
      if (c == 0) {
          free = true;
          setExclusiveOwnerThread(null);
      }
     setState(c);
     return free;
 }

```


把head.next指向下一个waitStatus<=0的节点，并把该节点从阻塞中唤醒

```
private void unparkSuccessor(Node node) {
    int ws = node.waitStatus;
    if (ws < 0)
        compareAndSetWaitStatus(node, ws, 0);

    Node s = node.next;
    if (s == null || s.waitStatus > 0) {
        // 这里没看懂为什么要从tail节点倒序遍历？
        // 不是应该从head.next节点开始遍历更快嘛？
        s = null;
        for (Node t = tail; t != null && t != node; t = t.prev)
            if (t.waitStatus <= 0)
                s = t;
    }
    if (s != null)
        LockSupport.unpark(s.thread);
}

```


