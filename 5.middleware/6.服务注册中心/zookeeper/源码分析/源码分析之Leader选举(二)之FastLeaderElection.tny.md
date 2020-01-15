### 【Zookeeper】源码分析之Leader选举（二）之FastLeaderElection

### 一、前言

　　前面学习了Leader选举的总体框架，接着来学习Zookeeper中默认的选举策略，FastLeaderElection。
### 二、FastLeaderElection源码分析

#### 2.1 类的继承关系　

```java
public class FastLeaderElection implements Election {
    
}
```
说明：FastLeaderElection实现了Election接口，其需要实现接口中定义的lookForLeader方法和shutdown方法，  
其是标准的Fast Paxos算法的实现，各服务器之间基于TCP协议进行选举。

#### 2.2 类的内部类

　　FastLeaderElection有三个较为重要的内部类，分别为Notification、ToSend、Messenger。

##### 1. Notification类
说明：Notification表示收到的选举投票信息（其他服务器发来的选举投票信息），
其包含了被选举者的id、zxid、选举周期等信息，其buildMsg方法将选举信息封装至ByteBuffer中再进行发送。
```
static public class Notification {
        /*
         * Format version, introduced in 3.4.6
         */
        
        public final static int CURRENTVERSION = 0x1; 
        int version;
                
        /*
         * Proposed leader
         */
        // 被推选的leader的id
        long leader;

        /*
         * zxid of the proposed leader
         */
        // 被推选的leader的事务id
        long zxid;

        /*
         * Epoch
         */
        // 推选者的选举周期
        long electionEpoch;

        /*
         * current state of sender
         */
        // 推选者的状态
        QuorumPeer.ServerState state;

        /*
         * Address of sender
         */
        // 推选者的id
        long sid;

        /*
         * epoch of the proposed leader
         */
        // 被推选者的选举周期
        long peerEpoch;
        
        @Override
        public String toString() {
            return new String(Long.toHexString(version) + " (message format version), " 
                    + leader + " (n.leader), 0x"
                    + Long.toHexString(zxid) + " (n.zxid), 0x"
                    + Long.toHexString(electionEpoch) + " (n.round), " + state
                    + " (n.state), " + sid + " (n.sid), 0x"
                    + Long.toHexString(peerEpoch) + " (n.peerEpoch) ");
        }
    }
    
    static ByteBuffer buildMsg(int state,
            long leader,
            long zxid,
            long electionEpoch,
            long epoch) {
        byte requestBytes[] = new byte[40];
        ByteBuffer requestBuffer = ByteBuffer.wrap(requestBytes);

        /*
         * Building notification packet to send 
         */

        requestBuffer.clear();
        requestBuffer.putInt(state);
        requestBuffer.putLong(leader);
        requestBuffer.putLong(zxid);
        requestBuffer.putLong(electionEpoch);
        requestBuffer.putLong(epoch);
        requestBuffer.putInt(Notification.CURRENTVERSION);
        
        return requestBuffer;
    }
}
```
##### 2. ToSend类
说明：ToSend表示发送给其他服务器的选举投票信息，也包含了被选举者的id、zxid、选举周期等信息。
```
static public class ToSend {
        static enum mType {crequest, challenge, notification, ack}

        ToSend(mType type,
                long leader,
                long zxid,
                long electionEpoch,
                ServerState state,
                long sid,
                long peerEpoch) {

            this.leader = leader;
            this.zxid = zxid;
            this.electionEpoch = electionEpoch;
            this.state = state;
            this.sid = sid;
            this.peerEpoch = peerEpoch;
        }

        /*
         * Proposed leader in the case of notification
         */
        //被推举的leader的id
        long leader;

        /*
         * id contains the tag for acks, and zxid for notifications
         */
        // 被推举的leader的最大事务id
        long zxid;

        /*
         * Epoch
         */
        // 推举者的选举周期
        long electionEpoch;

        /*
         * Current state;
         */
        // 推举者的状态
        QuorumPeer.ServerState state;

        /*
         * Address of recipient
         */
        // 推举者的id
        long sid;
        
        /*
         * Leader epoch
         */
        // 被推举的leader的选举周期
        long peerEpoch;
    }


```
##### 3. Messenger类
Messenger包含了WorkerReceiver和WorkerSender两个内部类

* 1.WorkerReceiver

```
class WorkerReceiver implements Runnable {
            // 是否终止
            volatile boolean stop;
            // 服务器之间的连接
            QuorumCnxManager manager;

            WorkerReceiver(QuorumCnxManager manager) {
                this.stop = false;
                this.manager = manager;
            }

            public void run() {
                // 响应
                Message response;
                while (!stop) { // 不终止
                    // Sleeps on receive
                    try{
                        // 从recvQueue中取出一个选举投票消息（从其他服务器发送过来）
                        response = manager.pollRecvQueue(3000, TimeUnit.MILLISECONDS);
                        // 无投票，跳过
                        if(response == null) continue;

                        /*
                         * If it is from an observer, respond right away.
                         * Note that the following predicate assumes that
                         * if a server is not a follower, then it must be
                         * an observer. If we ever have any other type of
                         * learner in the future, we'll have to change the
                         * way we check for observers.
                         */
                        if(!self.getVotingView().containsKey(response.sid)){ // 当前的投票者集合不包含服务器
                            // 获取自己的投票
                            Vote current = self.getCurrentVote();
                            // 构造ToSend消息
                            ToSend notmsg = new ToSend(ToSend.mType.notification,
                                    current.getId(),
                                    current.getZxid(),
                                    logicalclock,
                                    self.getPeerState(),
                                    response.sid,
                                    current.getPeerEpoch());
                            // 放入sendqueue队列，等待发送
                            sendqueue.offer(notmsg);
                        } else { // 包含服务器，表示接收到该服务器的选票消息
                            // Receive new message
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Receive new notification message. My id = "
                                        + self.getId());
                            }

                            /*
                             * We check for 28 bytes for backward compatibility
                             */
                            // 检查向后兼容性
                            if (response.buffer.capacity() < 28) {
                                LOG.error("Got a short response: "
                                        + response.buffer.capacity());
                                continue;
                            }
                            // 若容量为28，则表示可向后兼容
                            boolean backCompatibility = (response.buffer.capacity() == 28);
                            // 设置buffer中的position、limit等属性
                            response.buffer.clear();

                            // Instantiate Notification and set its attributes
                            // 创建接收通知
                            Notification n = new Notification();
                            
                            // State of peer that sent this message
                            // 推选者的状态
                            QuorumPeer.ServerState ackstate = QuorumPeer.ServerState.LOOKING;
                            switch (response.buffer.getInt()) { // 读取状态
                            case 0:
                                ackstate = QuorumPeer.ServerState.LOOKING;
                                break;
                            case 1:
                                ackstate = QuorumPeer.ServerState.FOLLOWING;
                                break;
                            case 2:
                                ackstate = QuorumPeer.ServerState.LEADING;
                                break;
                            case 3:
                                ackstate = QuorumPeer.ServerState.OBSERVING;
                                break;
                            default:
                                continue;
                            }
                            
                            // 获取leader的id
                            n.leader = response.buffer.getLong();
                            // 获取zxid
                            n.zxid = response.buffer.getLong();
                            // 获取选举周期
                            n.electionEpoch = response.buffer.getLong();
                            n.state = ackstate;
                            // 设置服务器的id
                            n.sid = response.sid;
                            if(!backCompatibility){ // 不向后兼容
                                n.peerEpoch = response.buffer.getLong();
                            } else { // 向后兼容
                                if(LOG.isInfoEnabled()){
                                    LOG.info("Backward compatibility mode, server id=" + n.sid);
                                }
                                // 获取选举周期
                                n.peerEpoch = ZxidUtils.getEpochFromZxid(n.zxid);
                            }

                            /*
                             * Version added in 3.4.6
                             */
                            
                            // 确定版本号
                            n.version = (response.buffer.remaining() >= 4) ? 
                                         response.buffer.getInt() : 0x0;

                            /*
                             * Print notification info
                             */
                            if(LOG.isInfoEnabled()){
                                printNotification(n);
                            }

                            /*
                             * If this server is looking, then send proposed leader
                             */

                            if(self.getPeerState() == QuorumPeer.ServerState.LOOKING){ // 本服务器为LOOKING状态
                                // 将消息放入recvqueue中
                                recvqueue.offer(n);

                                /*
                                 * Send a notification back if the peer that sent this
                                 * message is also looking and its logical clock is
                                 * lagging behind.
                                 */
                                if((ackstate == QuorumPeer.ServerState.LOOKING) // 推选者服务器为LOOKING状态
                                        && (n.electionEpoch < logicalclock)){ // 选举周期小于逻辑时钟
                                    // 创建新的投票
                                    Vote v = getVote();
                                    // 构造新的发送消息（本服务器自己的投票）
                                    ToSend notmsg = new ToSend(ToSend.mType.notification,
                                            v.getId(),
                                            v.getZxid(),
                                            logicalclock,
                                            self.getPeerState(),
                                            response.sid,
                                            v.getPeerEpoch());
                                    // 将发送消息放置于队列，等待发送
                                    sendqueue.offer(notmsg);
                                }
                            } else { // 推选服务器状态不为LOOKING
                                /*
                                 * If this server is not looking, but the one that sent the ack
                                 * is looking, then send back what it believes to be the leader.
                                 */
                                // 获取当前投票
                                Vote current = self.getCurrentVote(); 
                                if(ackstate == QuorumPeer.ServerState.LOOKING){ // 为LOOKING状态
                                    if(LOG.isDebugEnabled()){
                                        LOG.debug("Sending new notification. My id =  " +
                                                self.getId() + " recipient=" +
                                                response.sid + " zxid=0x" +
                                                Long.toHexString(current.getZxid()) +
                                                " leader=" + current.getId());
                                    }
                                    
                                    ToSend notmsg;
                                    if(n.version > 0x0) { // 版本号大于0
                                        // 构造ToSend消息
                                        notmsg = new ToSend(
                                                ToSend.mType.notification,
                                                current.getId(),
                                                current.getZxid(),
                                                current.getElectionEpoch(),
                                                self.getPeerState(),
                                                response.sid,
                                                current.getPeerEpoch());
                                        
                                    } else { // 版本号不大于0
                                        // 构造ToSend消息
                                        Vote bcVote = self.getBCVote();
                                        notmsg = new ToSend(
                                                ToSend.mType.notification,
                                                bcVote.getId(),
                                                bcVote.getZxid(),
                                                bcVote.getElectionEpoch(),
                                                self.getPeerState(),
                                                response.sid,
                                                bcVote.getPeerEpoch());
                                    }
                                    // 将发送消息放置于队列，等待发送
                                    sendqueue.offer(notmsg);
                                }
                            }
                        }
                    } catch (InterruptedException e) {
                        System.out.println("Interrupted Exception while waiting for new message" +
                                e.toString());
                    }
                }
                LOG.info("WorkerReceiver is down");
            }
        }

WorkerReceiver
```
说明：WorkerReceiver实现了Runnable接口，是选票接收器。
其会不断地从QuorumCnxManager中获取其他服务器发来的选举消息，并将其转换成一个选票，
然后保存到recvqueue中，在选票接收过程中，如果发现该外部选票的选举轮次小于当前服务器的，
那么忽略该外部投票，同时立即发送自己的内部投票。
其是将QuorumCnxManager的Message转化为FastLeaderElection的Notification。
其中，WorkerReceiver的主要逻辑在run方法中，
其首先会从QuorumCnxManager中的recvQueue队列中取出其他服务器发来的选举消息，
消息封装在Message数据结构中。然后判断消息中的服务器id是否包含在可以投票的服务器集合中，  
若不是，则会将本服务器的内部投票发送给该服务器，其流程如下:
```
if(!self.getVotingView().containsKey(response.sid)){ // 当前的投票者集合不包含服务器
        // 获取自己的投票
        Vote current = self.getCurrentVote();
        // 构造ToSend消息
        ToSend notmsg = new ToSend(ToSend.mType.notification,
                current.getId(),
                current.getZxid(),
                logicalclock,
                self.getPeerState(),
                response.sid,
                current.getPeerEpoch());
        // 放入sendqueue队列，等待发送
        sendqueue.offer(notmsg);
}
```
若包含该服务器，则根据消息（Message）解析出投票服务器的投票信息并将其封装为Notification，  
然后判断当前服务器是否为LOOKING，若为LOOKING，  
则直接将Notification放入FastLeaderElection的recvqueue（区别于recvQueue）中。  
然后判断投票服务器是否为LOOKING状态，并且其选举周期小于当前服务器的逻辑时钟，  
则将本（当前）服务器的内部投票发送给该服务器，否则，直接忽略掉该投票。其流程如下　　
```
if(self.getPeerState() == QuorumPeer.ServerState.LOOKING){ // 本服务器为LOOKING状态
    // 将消息放入recvqueue中
    recvqueue.offer(n);

    /*
     * Send a notification back if the peer that sent this
     * message is also looking and its logical clock is
     * lagging behind.
     */
    if((ackstate == QuorumPeer.ServerState.LOOKING) // 推选者服务器为LOOKING状态
            && (n.electionEpoch < logicalclock)){ // 选举周期小于逻辑时钟
        // 创建新的投票
        Vote v = getVote();
        // 构造新的发送消息（本服务器自己的投票）
        ToSend notmsg = new ToSend(ToSend.mType.notification,
                v.getId(),
                v.getZxid(),
                logicalclock,
                self.getPeerState(),
                response.sid,
                v.getPeerEpoch());
        // 将发送消息放置于队列，等待发送
        sendqueue.offer(notmsg);
    }
}
```
若本服务器的状态不为LOOKING，则会根据投票服务器中解析的version信息来构造ToSend消息，放入sendqueue，等待发送，起流程如下　
```
else { // 本服务器状态不为LOOKING
    /*
     * If this server is not looking, but the one that sent the ack
     * is looking, then send back what it believes to be the leader.
     */
    // 获取当前投票
    Vote current = self.getCurrentVote(); 
    if(ackstate == QuorumPeer.ServerState.LOOKING){ // 为LOOKING状态
        if(LOG.isDebugEnabled()){
            LOG.debug("Sending new notification. My id =  " +
                    self.getId() + " recipient=" +
                    response.sid + " zxid=0x" +
                    Long.toHexString(current.getZxid()) +
                    " leader=" + current.getId());
        }
        
        ToSend notmsg;
        if(n.version > 0x0) { // 版本号大于0
            // 构造ToSend消息
            notmsg = new ToSend(
                    ToSend.mType.notification,
                    current.getId(),
                    current.getZxid(),
                    current.getElectionEpoch(),
                    self.getPeerState(),
                    response.sid,
                    current.getPeerEpoch());
            
        } else { // 版本号不大于0
            // 构造ToSend消息
            Vote bcVote = self.getBCVote();
            notmsg = new ToSend(
                    ToSend.mType.notification,
                    bcVote.getId(),
                    bcVote.getZxid(),
                    bcVote.getElectionEpoch(),
                    self.getPeerState(),
                    response.sid,
                    bcVote.getPeerEpoch());
        }
        // 将发送消息放置于队列，等待发送
        sendqueue.offer(notmsg);
    }
}
```
   * 2.WorkerSender
```
class WorkerSender implements Runnable {
            // 是否终止
            volatile boolean stop;
            // 服务器之间的连接
            QuorumCnxManager manager;

            // 构造器
            WorkerSender(QuorumCnxManager manager){
                // 初始化属性
                this.stop = false;
                this.manager = manager;
            }

            public void run() {
                while (!stop) { // 不终止
                    try {
                        // 从sendqueue中取出ToSend消息
                        ToSend m = sendqueue.poll(3000, TimeUnit.MILLISECONDS);
                        // 若为空，则跳过
                        if(m == null) continue;
                        // 不为空，则进行处理
                        process(m);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
                LOG.info("WorkerSender is down");
            }

            /**
             * Called by run() once there is a new message to send.
             *
             * @param m     message to send
             */
            void process(ToSend m) {
                // 构建消息
                ByteBuffer requestBuffer = buildMsg(m.state.ordinal(), 
                                                        m.leader,
                                                        m.zxid, 
                                                        m.electionEpoch, 
                                                        m.peerEpoch);
                // 发送消息
                manager.toSend(m.sid, requestBuffer);
            }
        }

WorkerSender
```
说明：WorkerSender也实现了Runnable接口，为选票发送器，  
其会不断地从sendqueue中获取待发送的选票，并将其传递到底层QuorumCnxManager中，  
其过程是将FastLeaderElection的ToSend转化为QuorumCnxManager的Message

* 3.2 类的属性　
```
protected class Messenger {
        // 选票发送器
        WorkerSender ws;
        // 选票接收器
        WorkerReceiver wr;
    }
```

说明：Messenger中维护了一个WorkerSender和WorkerReceiver，分别表示选票发送器和选票接收器
* 3.3 类的构造函数
```
Messenger(QuorumCnxManager manager) {
            // 创建WorkerSender
            this.ws = new WorkerSender(manager);
            // 新创建线程
            Thread t = new Thread(this.ws,
                    "WorkerSender[myid=" + self.getId() + "]");
            // 设置为守护线程
            t.setDaemon(true);
            // 启动
            t.start();

            // 创建WorkerReceiver
            this.wr = new WorkerReceiver(manager);
            // 创建线程
            t = new Thread(this.wr,
                    "WorkerReceiver[myid=" + self.getId() + "]");
            // 设置为守护线程
            t.setDaemon(true);
            // 启动
            t.start();
        }
```
说明：会启动WorkerSender和WorkerReceiver，并设置为守护线程。
* 2.3 类的属性
```
public class FastLeaderElection implements Election {
    // 日志
    private static final Logger LOG = LoggerFactory.getLogger(FastLeaderElection.class);

    /**
     * Determine how much time a process has to wait
     * once it believes that it has reached the end of
     * leader election.
     */
    // 完成Leader选举之后需要等待时长
    final static int finalizeWait = å200;


    /**
     * Upper bound on the amount of time between two consecutive
     * notification checks. This impacts the amount of time to get
     * the system up again after long partitions. Currently 60 seconds.
     */
    // 两个连续通知检查之间的最大时长
    final static int maxNotificationInterval = 60000;

    /**
     * Connection manager. Fast leader election uses TCP for
     * communication between peers, and QuorumCnxManager manages
     * such connections.
     */
    // 管理服务器之间的连接
    QuorumCnxManager manager;
    
    
    
    // 选票发送队列，用于保存待发送的选票
    LinkedBlockingQueue<ToSend> sendqueue;
    
    // 选票接收队列，用于保存接收到的外部投票
    LinkedBlockingQueue<Notification> recvqueue;
    
    
    
    // 投票者
    QuorumPeer self;
    Messenger messenger;
    // 逻辑时钟
    volatile long logicalclock; /* Election instance */
    // 推选的leader的id
    long proposedLeader;
    // 推选的leader的zxid
    long proposedZxid;
    // 推选的leader的选举周期
    long proposedEpoch;
    
    
    // 是否停止选举
    volatile boolean stop;
}

类的属性
```
说明：其维护了服务器之间的连接（用于发送消息）、发送消息队列、接收消息队列、推选者的一些信息（zxid、id）、是否停止选举流程标识等。

```
public FastLeaderElection(QuorumPeer self, QuorumCnxManager manager){
        // 字段赋值
        this.stop = false;
        this.manager = manager;
        // 初始化其他信息
        starter(self, manager);
    }
```

说明：构造函数中初始化了stop字段和manager字段，并且调用了starter函数，其源码如下　
```
private void starter(QuorumPeer self, QuorumCnxManager manager) {
        // 赋值，对Leader和投票者的ID进行初始化操作
        this.self = self;
        proposedLeader = -1;
        proposedZxid = -1;
        
        // 初始化发送队列
        sendqueue = new LinkedBlockingQueue<ToSend>();
        // 初始化接收队列
        recvqueue = new LinkedBlockingQueue<Notification>();
        // 创建Messenger，会启动接收器和发送器线程
        this.messenger = new Messenger(manager);
    }
```

说明：其完成在构造函数中未完成的部分，如会初始化FastLeaderElection的sendqueue和recvqueue，并且启动接收器和发送器线程。

2.5 核心函数分析

　　1. sendNotifications函数　
```
private void sendNotifications() {
        for (QuorumServer server : self.getVotingView().values()) { // 遍历投票参与者集合
            long sid = server.id;
            
            // 构造发送消息
            ToSend notmsg = new ToSend(ToSend.mType.notification,
                    proposedLeader,
                    proposedZxid,
                    logicalclock,
                    QuorumPeer.ServerState.LOOKING,
                    sid,
                    proposedEpoch);
            if(LOG.isDebugEnabled()){
                LOG.debug("Sending Notification: " + proposedLeader + " (n.leader), 0x"  +
                      Long.toHexString(proposedZxid) + " (n.zxid), 0x" + Long.toHexString(logicalclock)  +
                      " (n.round), " + sid + " (recipient), " + self.getId() +
                      " (myid), 0x" + Long.toHexString(proposedEpoch) + " (n.peerEpoch)");
            }
            // 将发送消息放置于队列
            sendqueue.offer(notmsg);
        }
    }
```
说明：其会遍历所有的参与者投票集合，然后将自己的选票信息发送至上述所有的投票者集合，其并非同步发送，而是将ToSend消息放置于sendqueue中，之后由WorkerSender进行发送。
2. totalOrderPredicate函数　
```
protected boolean totalOrderPredicate(long newId, long newZxid, long newEpoch, long curId, long curZxid, long curEpoch) {
        LOG.debug("id: " + newId + ", proposed id: " + curId + ", zxid: 0x" +
                Long.toHexString(newZxid) + ", proposed zxid: 0x" + Long.toHexString(curZxid));
        if(self.getQuorumVerifier().getWeight(newId) == 0){ // 使用计票器判断当前服务器的权重是否为0
            return false;
        }
        
        /*
         * We return true if one of the following three cases hold:
         * 1- New epoch is higher
         * 2- New epoch is the same as current epoch, but new zxid is higher
         * 3- New epoch is the same as current epoch, new zxid is the same
         *  as current zxid, but server id is higher.
         */
        // 1. 判断消息里的epoch是不是比当前的大，如果大则消息中id对应的服务器就是leader
        // 2. 如果epoch相等则判断zxid，如果消息里的zxid大，则消息中id对应的服务器就是leader
        // 3. 如果前面两个都相等那就比较服务器id，如果大，则其就是leader
        return ((newEpoch > curEpoch) ||
                ((newEpoch == curEpoch) &&
                ((newZxid > curZxid) || ((newZxid == curZxid) && (newId > curId)))));
    }
```
说明：该函数将接收的投票与自身投票进行PK，查看是否消息中包含的服务器id是否更优，其按照epoch、zxid、id的优先级进行PK。

　　3. termPredicate函数　
```
protected boolean termPredicate(
            HashMap<Long, Vote> votes,
            Vote vote) {

        HashSet<Long> set = new HashSet<Long>();

        /*
         * First make the views consistent. Sometimes peers will have
         * different zxids for a server depending on timing.
         */
        for (Map.Entry<Long,Vote> entry : votes.entrySet()) { // 遍历已经接收的投票集合
            if (vote.equals(entry.getValue())){ // 将等于当前投票的项放入set
                set.add(entry.getKey());
            }
        }

        //统计set，查看投某个id的票数是否超过一半
        return self.getQuorumVerifier().containsQuorum(set);
    }
```

说明：该函数用于判断Leader选举是否结束，即是否有一半以上的服务器选出了相同的Leader，其过程是将收到的选票与当前选票进行对比，选票相同的放入同一个集合，之后判断选票相同的集合是否超过了半数。

　　4. checkLeader函数　　

```
protected boolean checkLeader(
            HashMap<Long, Vote> votes,
            long leader,
            long electionEpoch){
        
        boolean predicate = true;

        /*
         * If everyone else thinks I'm the leader, I must be the leader.
         * The other two checks are just for the case in which I'm not the
         * leader. If I'm not the leader and I haven't received a message
         * from leader stating that it is leading, then predicate is false.
         */

        if(leader != self.getId()){ // 自己不为leader
            if(votes.get(leader) == null) predicate = false; // 还未选出leader
            else if(votes.get(leader).getState() != ServerState.LEADING) predicate = false; // 选出的leader还未给出ack信号，其他服务器还不知道leader
        } else if(logicalclock != electionEpoch) { // 逻辑时钟不等于选举周期
            predicate = false;
        } 

        return predicate;
    }
```

说明：该函数检查是否已经完成了Leader的选举，此时Leader的状态应该是LEADING状态。

　　5. lookForLeader函数　
```
public Vote lookForLeader() throws InterruptedException {
        try {
            self.jmxLeaderElectionBean = new LeaderElectionBean();
            MBeanRegistry.getInstance().register(
                    self.jmxLeaderElectionBean, self.jmxLocalPeerBean);
        } catch (Exception e) {
            LOG.warn("Failed to register with JMX", e);
            self.jmxLeaderElectionBean = null;
        }
        if (self.start_fle == 0) {
           self.start_fle = System.currentTimeMillis();
        }
        try {
            HashMap<Long, Vote> recvset = new HashMap<Long, Vote>();

            HashMap<Long, Vote> outofelection = new HashMap<Long, Vote>();

            int notTimeout = finalizeWait;

            synchronized(this){
                // 更新逻辑时钟，每进行一轮选举，都需要更新逻辑时钟
                logicalclock++;
                // 更新选票
                updateProposal(getInitId(), getInitLastLoggedZxid(), getPeerEpoch());
            }

            LOG.info("New election. My id =  " + self.getId() +
                    ", proposed zxid=0x" + Long.toHexString(proposedZxid));
            // 想其他服务器发送自己的选票
            sendNotifications();

            /*
             * Loop in which we exchange notifications until we find a leader
             */

            while ((self.getPeerState() == ServerState.LOOKING) &&
                    (!stop)){ // 本服务器状态为LOOKING并且还未选出leader
                /*
                 * Remove next notification from queue, times out after 2 times
                 * the termination time
                 */
                // 从recvqueue接收队列中取出投票
                Notification n = recvqueue.poll(notTimeout,
                        TimeUnit.MILLISECONDS);

                /*
                 * Sends more notifications if haven't received enough.
                 * Otherwise processes new notification.
                 */
                if(n == null){ // 如果没有收到足够多的选票，则发送选票
                    if(manager.haveDelivered()){ // manager已经发送了所有选票消息
                        // 向所有其他服务器发送消息
                        sendNotifications();
                    } else { // 还未发送所有消息
                        // 连接其他每个服务器
                        manager.connectAll();
                    }

                    /*
                     * Exponential backoff
                     */
                    int tmpTimeOut = notTimeout*2;
                    notTimeout = (tmpTimeOut < maxNotificationInterval?
                            tmpTimeOut : maxNotificationInterval);
                    LOG.info("Notification time out: " + notTimeout);
                }
                else if(self.getVotingView().containsKey(n.sid)) { // 投票者集合中包含接收到消息中的服务器id
                    /*
                     * Only proceed if the vote comes from a replica in the
                     * voting view.
                     */
                    switch (n.state) { // 确定接收消息中的服务器状态
                    case LOOKING: 
                        // If notification > current, replace and send messages out
                        if (n.electionEpoch > logicalclock) { // 其选举周期大于逻辑时钟
                            // 重新赋值逻辑时钟
                            logicalclock = n.electionEpoch;
                            recvset.clear();
                            if(totalOrderPredicate(n.leader, n.zxid, n.peerEpoch,
                                    getInitId(), getInitLastLoggedZxid(), getPeerEpoch())) { // 选出较优的服务器
                                // 更新选票
                                updateProposal(n.leader, n.zxid, n.peerEpoch);
                            } else { // 无法选出较优的服务器
                                // 更新选票
                                updateProposal(getInitId(),
                                        getInitLastLoggedZxid(),
                                        getPeerEpoch());
                            }
                            // 发送消息
                            sendNotifications();
                        } else if (n.electionEpoch < logicalclock) { // 选举周期小于逻辑时钟，不做处理
                            if(LOG.isDebugEnabled()){
                                LOG.debug("Notification election epoch is smaller than logicalclock. n.electionEpoch = 0x"
                                        + Long.toHexString(n.electionEpoch)
                                        + ", logicalclock=0x" + Long.toHexString(logicalclock));
                            }
                            break;
                        } else if (totalOrderPredicate(n.leader, n.zxid, n.peerEpoch,
                                proposedLeader, proposedZxid, proposedEpoch)) { // 等于，并且能选出较优的服务器
                            // 更新选票
                            updateProposal(n.leader, n.zxid, n.peerEpoch);
                            // 发送消息
                            sendNotifications();
                        }

                        if(LOG.isDebugEnabled()){
                            LOG.debug("Adding vote: from=" + n.sid +
                                    ", proposed leader=" + n.leader +
                                    ", proposed zxid=0x" + Long.toHexString(n.zxid) +
                                    ", proposed election epoch=0x" + Long.toHexString(n.electionEpoch));
                        }
                        
                        // recvset用于记录当前服务器在本轮次的Leader选举中收到的所有外部投票
                        recvset.put(n.sid, new Vote(n.leader, n.zxid, n.electionEpoch, n.peerEpoch));

                        if (termPredicate(recvset,
                                new Vote(proposedLeader, proposedZxid,
                                        logicalclock, proposedEpoch))) { // 若能选出leader

                            // Verify if there is any change in the proposed leader
                            while((n = recvqueue.poll(finalizeWait,
                                    TimeUnit.MILLISECONDS)) != null){ // 遍历已经接收的投票集合
                                if(totalOrderPredicate(n.leader, n.zxid, n.peerEpoch,
                                        proposedLeader, proposedZxid, proposedEpoch)){ // 能够选出较优的服务器
                                    recvqueue.put(n);
                                    break;
                                }
                            }

                            /*
                             * This predicate is true once we don't read any new
                             * relevant message from the reception queue
                             */
                            if (n == null) {
                                self.setPeerState((proposedLeader == self.getId()) ?
                                        ServerState.LEADING: learningState());

                                Vote endVote = new Vote(proposedLeader,
                                                        proposedZxid,
                                                        logicalclock,
                                                        proposedEpoch);
                                leaveInstance(endVote);
                                return endVote;
                            }
                        }
                        break;
                    case OBSERVING:
                        LOG.debug("Notification from observer: " + n.sid);
                        break;
                    case FOLLOWING:
                    case LEADING: // 处于LEADING状态
                        /*
                         * Consider all notifications from the same epoch
                         * together.
                         */
                        if(n.electionEpoch == logicalclock){ // 与逻辑时钟相等
                            // 将该服务器和选票信息放入recvset中
                            recvset.put(n.sid, new Vote(n.leader,
                                                          n.zxid,
                                                          n.electionEpoch,
                                                          n.peerEpoch));
                           
                            if(ooePredicate(recvset, outofelection, n)) { // 判断是否完成了leader选举
                                // 设置本服务器的状态
                                self.setPeerState((n.leader == self.getId()) ?
                                        ServerState.LEADING: learningState());
                                // 创建投票信息
                                Vote endVote = new Vote(n.leader, 
                                        n.zxid, 
                                        n.electionEpoch, 
                                        n.peerEpoch);
                                leaveInstance(endVote);
                                return endVote;
                            }
                        }

                        /*
                         * Before joining an established ensemble, verify
                         * a majority is following the same leader.
                         */
                        outofelection.put(n.sid, new Vote(n.version,
                                                            n.leader,
                                                            n.zxid,
                                                            n.electionEpoch,
                                                            n.peerEpoch,
                                                            n.state));
           
                        if(ooePredicate(outofelection, outofelection, n)) {
                            synchronized(this){
                                logicalclock = n.electionEpoch;
                                self.setPeerState((n.leader == self.getId()) ?
                                        ServerState.LEADING: learningState());
                            }
                            Vote endVote = new Vote(n.leader,
                                                    n.zxid,
                                                    n.electionEpoch,
                                                    n.peerEpoch);
                            leaveInstance(endVote);
                            return endVote;
                        }
                        break;
                    default:
                        LOG.warn("Notification state unrecognized: {} (n.state), {} (n.sid)",
                                n.state, n.sid);
                        break;
                    }
                } else {
                    LOG.warn("Ignoring notification from non-cluster member " + n.sid);
                }
            }
            return null;
        } finally {
            try {
                if(self.jmxLeaderElectionBean != null){
                    MBeanRegistry.getInstance().unregister(
                            self.jmxLeaderElectionBean);
                }
            } catch (Exception e) {
                LOG.warn("Failed to unregister with JMX", e);
            }
            self.jmxLeaderElectionBean = null;
        }
    }

lookForLeader
```
说明：该函数用于开始新一轮的Leader选举，其首先会将逻辑时钟自增，然后更新本服务器的选票信息（初始化选票），之后将选票信息放入sendqueue等待发送给其他服务器，其流程如下　

```
synchronized(this){
                // 更新逻辑时钟，每进行一轮新的leader选举，都需要更新逻辑时钟
                logicalclock++;
                // 更新选票（初始化选票）
                updateProposal(getInitId(), getInitLastLoggedZxid(), getPeerEpoch());
            }

            LOG.info("New election. My id =  " + self.getId() +
                    ", proposed zxid=0x" + Long.toHexString(proposedZxid));
            // 向其他服务器发送自己的选票（已更新的选票）
            sendNotifications();
```

　之后每台服务器会不断地从recvqueue队列中获取外部选票。如果服务器发现无法获取到任何外部投票，就立即确认自己是否和集群中其他服务器保持着有效的连接，如果没有连接，则马上建立连接，如果已经建立了连接，则再次发送自己当前的内部投票，其流程如下　　

```
// 从recvqueue接收队列中取出投票
                Notification n = recvqueue.poll(notTimeout,
                        TimeUnit.MILLISECONDS);

                /*
                 * Sends more notifications if haven't received enough.
                 * Otherwise processes new notification.
                 */
                if(n == null){ // 无法获取选票
                    if(manager.haveDelivered()){ // manager已经发送了所有选票消息（表示有连接）
                        // 向所有其他服务器发送消息
                        sendNotifications();
                    } else { // 还未发送所有消息（表示无连接）
                        // 连接其他每个服务器
                        manager.connectAll();
                    }

                    /*
                     * Exponential backoff
                     */
                    int tmpTimeOut = notTimeout*2;
                    notTimeout = (tmpTimeOut < maxNotificationInterval?
                            tmpTimeOut : maxNotificationInterval);
                    LOG.info("Notification time out: " + notTimeout);
                }
```

　在发送完初始化选票之后，接着开始处理外部投票。在处理外部投票时，会根据选举轮次来进行不同的处理。　　

　　　　· 外部投票的选举轮次大于内部投票。若服务器自身的选举轮次落后于该外部投票对应服务器的选举轮次，那么就会立即更新自己的选举轮次(logicalclock)，并且清空所有已经收到的投票，然后使用初始化的投票来进行PK以确定是否变更内部投票。最终再将内部投票发送出去。

　　　　· 外部投票的选举轮次小于内部投票。若服务器接收的外选票的选举轮次落后于自身的选举轮次，那么Zookeeper就会直接忽略该外部投票，不做任何处理。

　　　　· 外部投票的选举轮次等于内部投票。此时可以开始进行选票PK，如果消息中的选票更优，则需要更新本服务器内部选票，再发送给其他服务器。

　　之后再对选票进行归档操作，无论是否变更了投票，都会将刚刚收到的那份外部投票放入选票集合recvset中进行归档，其中recvset用于记录当前服务器在本轮次的Leader选举中收到的所有外部投票，然后开始统计投票，统计投票是为了统计集群中是否已经有过半的服务器认可了当前的内部投票，如果确定已经有过半服务器认可了该投票，然后再进行最后一次确认，判断是否又有更优的选票产生，若无，则终止投票，然后最终的选票，其流程如下

```
if (n.electionEpoch > logicalclock) { // 其选举周期大于逻辑时钟
                            // 重新赋值逻辑时钟
                            logicalclock = n.electionEpoch;
                            // 清空所有接收到的所有选票
                            recvset.clear();
                            if(totalOrderPredicate(n.leader, n.zxid, n.peerEpoch,
                                    getInitId(), getInitLastLoggedZxid(), getPeerEpoch())) { // 进行PK，选出较优的服务器
                                // 更新选票
                                updateProposal(n.leader, n.zxid, n.peerEpoch);
                            } else { // 无法选出较优的服务器
                                // 更新选票
                                updateProposal(getInitId(),
                                        getInitLastLoggedZxid(),
                                        getPeerEpoch());
                            }
                            // 发送本服务器的内部选票消息
                            sendNotifications();
                        } else if (n.electionEpoch < logicalclock) { // 选举周期小于逻辑时钟，不做处理，直接忽略
                            if(LOG.isDebugEnabled()){
                                LOG.debug("Notification election epoch is smaller than logicalclock. n.electionEpoch = 0x"
                                        + Long.toHexString(n.electionEpoch)
                                        + ", logicalclock=0x" + Long.toHexString(logicalclock));
                            }
                            break;
                        } else if (totalOrderPredicate(n.leader, n.zxid, n.peerEpoch,
                                proposedLeader, proposedZxid, proposedEpoch)) { // PK，选出较优的服务器
                            // 更新选票
                            updateProposal(n.leader, n.zxid, n.peerEpoch);
                            // 发送消息
                            sendNotifications();
                        }

                        if(LOG.isDebugEnabled()){
                            LOG.debug("Adding vote: from=" + n.sid +
                                    ", proposed leader=" + n.leader +
                                    ", proposed zxid=0x" + Long.toHexString(n.zxid) +
                                    ", proposed election epoch=0x" + Long.toHexString(n.electionEpoch));
                        }
                        
                        // recvset用于记录当前服务器在本轮次的Leader选举中收到的所有外部投票
                        recvset.put(n.sid, new Vote(n.leader, n.zxid, n.electionEpoch, n.peerEpoch));

                        if (termPredicate(recvset,
                                new Vote(proposedLeader, proposedZxid,
                                        logicalclock, proposedEpoch))) { // 若能选出leader

                            // Verify if there is any change in the proposed leader
                            while((n = recvqueue.poll(finalizeWait,
                                    TimeUnit.MILLISECONDS)) != null){ // 遍历已经接收的投票集合
                                if(totalOrderPredicate(n.leader, n.zxid, n.peerEpoch,
                                        proposedLeader, proposedZxid, proposedEpoch)){ // 选票有变更，比之前提议的Leader有更好的选票加入
                                    // 将更优的选票放在recvset中
                                    recvqueue.put(n);
                                    break;
                                }
                            }

                            /*
                             * This predicate is true once we don't read any new
                             * relevant message from the reception queue
                             */
                            if (n == null) { // 表示之前提议的Leader已经是最优的
                                // 设置服务器状态
                                self.setPeerState((proposedLeader == self.getId()) ?
                                        ServerState.LEADING: learningState());
                                // 最终的选票
                                Vote endVote = new Vote(proposedLeader,
                                                        proposedZxid,
                                                        logicalclock,
                                                        proposedEpoch);
                                // 清空recvqueue队列的选票
                                leaveInstance(endVote);
                                // 返回选票
                                return endVote;
                            }
                        }
```

　　若选票中的服务器状态为FOLLOWING或者LEADING时，其大致步骤会判断选举周期是否等于逻辑时钟，归档选票，是否已经完成了Leader选举，设置服务器状态，修改逻辑时钟等于选举周期，返回最终选票，其流程如下　

```
if(n.electionEpoch == logicalclock){ // 与逻辑时钟相等
                            // 将该服务器和选票信息放入recvset中
                            recvset.put(n.sid, new Vote(n.leader,
                                                          n.zxid,
                                                          n.electionEpoch,
                                                          n.peerEpoch));
                           
                            if(ooePredicate(recvset, outofelection, n)) { // 已经完成了leader选举
                                // 设置本服务器的状态
                                self.setPeerState((n.leader == self.getId()) ?
                                        ServerState.LEADING: learningState());
                                // 最终的选票
                                Vote endVote = new Vote(n.leader, 
                                        n.zxid, 
                                        n.electionEpoch, 
                                        n.peerEpoch);
                                // 清空recvqueue队列的选票
                                leaveInstance(endVote);
                                return endVote;
                            }
                        }

                        /*
                         * Before joining an established ensemble, verify
                         * a majority is following the same leader.
                         */
                        outofelection.put(n.sid, new Vote(n.version,
                                                            n.leader,
                                                            n.zxid,
                                                            n.electionEpoch,
                                                            n.peerEpoch,
                                                            n.state));
           
                        if(ooePredicate(outofelection, outofelection, n)) { // 已经完成了leader选举
                            synchronized(this){
                                // 设置逻辑时钟
                                logicalclock = n.electionEpoch;
                                // 设置状态
                                self.setPeerState((n.leader == self.getId()) ?
                                        ServerState.LEADING: learningState());
                            }
                            // 最终选票
                            Vote endVote = new Vote(n.leader,
                                                    n.zxid,
                                                    n.electionEpoch,
                                                    n.peerEpoch);
                            // 清空recvqueue队列的选票
                            leaveInstance(endVote);
                            // 返回选票
                            return endVote;
                        }
```

三、总结

　　本篇博文详细分析了FastLeaderElection的算法，其是ZooKeeper的核心部分，结合前面的理论学习部分（点击这里可查看），可以比较轻松的理解其具体过程。也谢谢各位园友的观看~　　