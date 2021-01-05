# [ReentrantLock 类的 hasQueuedPredecessors 方法和 head 节点的含义](https://www.cnblogs.com/kumu/p/10659835.html)

部分启发来源自文章：[Java 并发编程--Lock](https://www.cnblogs.com/zaizhoumo/p/7756310.html)

## PART 1

1、如果 h==t 成立，h 和 t 均为 null 或是同一个具体的节点，无后继节点，返回 false。
2、如果 h!=t 成立，head.next 是否为 null，如果为 null，返回 true。什么情况下 h!=t 的同时 h.next==null？？，有其他线程第一次正在入队时，可能会出现。见 AQS 的 enq 方法，compareAndSetHead(node)完成，还没执行 tail=head 语句时，此时 tail=null,head=newNode,head.next=null。
3、如果 h!=t 成立，head.next != null，则判断 head.next 是否是当前线程，如果是返回 false，否则返回 true（head 节点是获取到锁的节点，但是任意时刻 head 节点可能占用着锁，也可能释放了锁（unlock()）,未被阻塞的 head.next 节点对应的线程在任意时刻都是有必要去尝试获取锁）

```
public final boolean hasQueuedPredecessors() {
    Node t = tail;
    Node h = head;
    Node s;
    return h != t &&
        ((s = h.next) == null || s.thread != Thread.currentThread());
}
```

## PART 2 　　解释为什么要判断：s.thread != Thread.currentThread()

根据 ReentrantLock 的解锁流程，也就是下面四个方法，可以看到当线程释放锁之后还是会在队列的 head 节点，只是把 next 指针指向下一个可用节点，并唤醒它
也就是说任意时刻，head 节点可能占用着锁（除了第一次执行 enq()入队列时，head 仅仅是个 new Node()，没有实际对应任何线程，但是却“隐式”对应第一个获得锁但并未入队列的线程，和后续的 head 在含义上保持一致），也可能释放了锁（unlock()）,未被阻塞的 head.next 节点对应的线程在任意时刻都是有必要去尝试获取锁

```
 public void unlock() {
     sync.release(1);
 }
```

尝试释放锁，释放成功后把 head.next 从阻塞中唤醒

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

把 state-1
当 state=0 时，把 exclusiveOwnerThread 设置为 null，说明线程释放了锁

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

把 head.next 指向下一个 waitStatus<=0 的节点，并把该节点从阻塞中唤醒

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
