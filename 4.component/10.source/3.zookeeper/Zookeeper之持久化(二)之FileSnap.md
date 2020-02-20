# [【Zookeeper】源码分析之持久化（二）之FileSnap](https://www.cnblogs.com/leesf456/p/6285014.html)

**一、前言**

前篇博文已经分析了FileTxnLog的源码，现在接着分析持久化中的FileSnap，其主要提供了快照相应的接口。

**二、SnapShot源码分析**

SnapShot是FileTxnLog的父类，接口类型，其方法如下　　

```
public interface SnapShot {
    
    /**
     * deserialize a data tree from the last valid snapshot and 
     * return the last zxid that was deserialized
     * @param dt the datatree to be deserialized into
     * @param sessions the sessions to be deserialized into
     * @return the last zxid that was deserialized from the snapshot
     * @throws IOException
     */
    // 反序列化
    long deserialize(DataTree dt, Map<Long, Integer> sessions) 
        throws IOException;
    
    /**
     * persist the datatree and the sessions into a persistence storage
     * @param dt the datatree to be serialized
     * @param sessions 
     * @throws IOException
     */
    // 序列化
    void serialize(DataTree dt, Map<Long, Integer> sessions, 
            File name) 
        throws IOException;
    
    /**
     * find the most recent snapshot file
     * @return the most recent snapshot file
     * @throws IOException
     */
    // 查找最新的snapshot文件
    File findMostRecentSnapshot() throws IOException;
    
    /**
     * free resources from this snapshot immediately
     * @throws IOException
     */
    // 释放资源
    void close() throws IOException;
}
```


说明：可以看到SnapShot只定义了四个方法，反序列化、序列化、查找最新的snapshot文件、释放资源。

**三、FileSnap源码分析**

FileSnap实现了SnapShot接口，主要用作存储、序列化、反序列化、访问相应snapshot文件。

3.1 类的属性　

```
public class FileSnap implements SnapShot {
    // snapshot目录文件
    File snapDir;
    // 是否已经关闭标识
    private volatile boolean close = false;
    // 版本号
    private static final int VERSION=2;
    // database id
    private static final long dbId=-1;
    // Logger
    private static final Logger LOG = LoggerFactory.getLogger(FileSnap.class);
    // snapshot文件的魔数(类似class文件的魔数)
    public final static int SNAP_MAGIC
        = ByteBuffer.wrap("ZKSN".getBytes()).getInt();
}
```


说明：FileSnap主要的属性包含了是否已经关闭标识。

3.2 类的核心函数

1. deserialize函数

函数签名如下：

public long deserialize(DataTree dt, Map<Long, Integer> sessions)，是对SnapShot的deserialize函数的实现。其源码如下　　

```java
public long deserialize(DataTree dt, Map<Long, Integer> sessions)
            throws IOException {
    // we run through 100 snapshots (not all of them)
    // if we cannot get it running within 100 snapshots
    // we should  give up
    // 查找100个合法的snapshot文件
    List<File> snapList = findNValidSnapshots(100);
    if (snapList.size() == 0) { // 无snapshot文件，直接返回
        return -1L;
    }
    // 
    File snap = null;
    // 默认为不合法
    boolean foundValid = false;
    for (int i = 0; i < snapList.size(); i++) { // 遍历snapList
        snap = snapList.get(i);
        // 输入流
        InputStream snapIS = null;
        CheckedInputStream crcIn = null;
        try {
            LOG.info("Reading snapshot " + snap);
            // 读取指定的snapshot文件
            snapIS = new BufferedInputStream(new FileInputStream(snap));
            // 验证
            crcIn = new CheckedInputStream(snapIS, new Adler32());
            InputArchive ia = BinaryInputArchive.getArchive(crcIn);
            // 反序列化
            deserialize(dt,sessions, ia);
            // 获取验证的值Checksum
            long checkSum = crcIn.getChecksum().getValue();
            // 从文件中读取val值
            long val = ia.readLong("val");
            if (val != checkSum) { // 比较验证，不相等，抛出异常
                throw new IOException("CRC corruption in snapshot :  " + snap);
            }
            // 合法
            foundValid = true;
            // 跳出循环
            break;
        } catch(IOException e) {
            LOG.warn("problem reading snap file " + snap, e);
        } finally { // 关闭流
            if (snapIS != null) 
                snapIS.close();
            if (crcIn != null) 
                crcIn.close();
        } 
    }
    if (!foundValid) { // 遍历所有文件都未验证成功
        throw new IOException("Not able to find valid snapshots in " + snapDir);
    }
    // 从文件名中解析出zxid
    dt.lastProcessedZxid = Util.getZxidFromName(snap.getName(), "snapshot");
    return dt.lastProcessedZxid;
}
```


说明：deserialize主要用作反序列化，并将反序列化结果保存至dt和sessions中。 其大致步骤如下

① 获取100个合法的snapshot文件，并且snapshot文件已经通过zxid进行降序排序，进入②

② 遍历100个snapshot文件，从zxid最大的开始，读取该文件，并创建相应的InputArchive，进入③

③ 调用deserialize(dt,sessions, ia)函数完成反序列化操作，进入④

④ 验证从文件中读取的Checksum是否与新生的Checksum相等，若不等，则抛出异常，否则，进入⑤

⑤ 跳出循环并关闭相应的输入流，并从文件名中解析出相应的zxid返回。

⑥ 在遍历100个snapshot文件后仍然无法找到通过验证的文件，则抛出异常。

在deserialize函数中，会调用findNValidSnapshots以及同名的deserialize(dt,sessions, ia)函数，findNValidSnapshots函数源码如下　　

```java
private List<File> findNValidSnapshots(int n) throws IOException {
    // 按照zxid对snapshot文件进行降序排序
    List<File> files = Util.sortDataDir(snapDir.listFiles(),"snapshot", false);
    int count = 0;
    List<File> list = new ArrayList<File>();
    for (File f : files) { // 遍历snapshot文件
        // we should catch the exceptions
        // from the valid snapshot and continue
        // until we find a valid one
        try {
            // 验证文件是否合法，在写snapshot文件时服务器宕机
            // 此时的snapshot文件非法;非snapshot文件也非法
            if (Util.isValidSnapshot(f)) {
                // 合法则添加
                list.add(f);
                // 计数器加一
                count++;
                if (count == n) { // 等于n则跳出循环
                    break;
                }
            }
        } catch (IOException e) {
            LOG.info("invalid snapshot " + f, e);
        }
    }
    return list;
}
```


说明：该函数主要是查找N个合法的snapshot文件并进行降序排序后返回，Util的isValidSnapshot函数主要是从文件名和文件的结尾符号是否是"/"来判断snapshot文件是否合法。其源码如下　


```
public static boolean isValidSnapshot(File f) throws IOException {
    // 文件为空或者非snapshot文件，则返回false
    if (f==null || Util.getZxidFromName(f.getName(), "snapshot") == -1)
        return false;

    // Check for a valid snapshot
    // 随机访问文件
    RandomAccessFile raf = new RandomAccessFile(f, "r");
    try {
        // including the header and the last / bytes
        // the snapshot should be atleast 10 bytes
        if (raf.length() < 10) { // 文件大小小于10个字节，返回false
            return false;
        }
        // 移动至倒数第五个字节
        raf.seek(raf.length() - 5);
        byte bytes[] = new byte[5];
        int readlen = 0;
        int l;
        while(readlen < 5 &&
              (l = raf.read(bytes, readlen, bytes.length - readlen)) >= 0) { // 将最后五个字节存入bytes中
            readlen += l;
        }
        if (readlen != bytes.length) {
            LOG.info("Invalid snapshot " + f
                    + " too short, len = " + readlen);
            return false;
        }
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        int len = bb.getInt();
        byte b = bb.get();
        if (len != 1 || b != '/') { // 最后字符不为"/",不合法
            LOG.info("Invalid snapshot " + f + " len = " + len
                    + " byte = " + (b & 0xff));
            return false;
        }
    } finally {
        raf.close();
    }

    return true;
}
```


deserialize(dt,sessions, ia)函数的源码如下　　


```
public void deserialize(DataTree dt, Map<Long, Integer> sessions,
        InputArchive ia) throws IOException {
    FileHeader header = new FileHeader();
    // 反序列化至header
    header.deserialize(ia, "fileheader");
    if (header.getMagic() != SNAP_MAGIC) { // 验证魔数是否相等
        throw new IOException("mismatching magic headers "
                + header.getMagic() + 
                " !=  " + FileSnap.SNAP_MAGIC);
    }
    // 反序列化至dt、sessions
    SerializeUtils.deserializeSnapshot(dt,ia,sessions);
}
```


说明：该函数主要作用反序列化，并将反序列化结果保存至header和sessions中。其中会验证header的魔数是否相等。

2. serialize函数　

函数签名如下：protected void serialize(DataTree dt,Map<Long, Integer> sessions, OutputArchive oa, FileHeader header) throws IOException

```
protected void serialize(DataTree dt,Map<Long, Integer> sessions,
            OutputArchive oa, FileHeader header) throws IOException {
    // this is really a programmatic error and not something that can
    // happen at runtime
    if(header==null) // 文件头为null
        throw new IllegalStateException(
                "Snapshot's not open for writing: uninitialized header");
    // 将header序列化
    header.serialize(oa, "fileheader");
    // 将dt、sessions序列化
    SerializeUtils.serializeSnapshot(dt,oa,sessions);
}
```


说明：该函数主要用于序列化dt、sessions和header，其中，首先会检查header是否为空，然后依次序列化header，sessions和dt。

3. serialize函数

函数签名如下：public synchronized void serialize(DataTree dt, Map<Long, Integer> sessions, File snapShot) throws IOException　　

```
public synchronized void serialize(DataTree dt, Map<Long, Integer> sessions, File snapShot)
            throws IOException {
    if (!close) { // 未关闭
        // 输出流
        OutputStream sessOS = new BufferedOutputStream(new FileOutputStream(snapShot));
        CheckedOutputStream crcOut = new CheckedOutputStream(sessOS, new Adler32());
        //CheckedOutputStream cout = new CheckedOutputStream()
        OutputArchive oa = BinaryOutputArchive.getArchive(crcOut);
        // 新生文件头
        FileHeader header = new FileHeader(SNAP_MAGIC, VERSION, dbId);
        // 序列化dt、sessions、header
        serialize(dt,sessions,oa, header);
        // 获取验证的值
        long val = crcOut.getChecksum().getValue();
        // 写入值
        oa.writeLong(val, "val");
        // 写入"/"
        oa.writeString("/", "path");
        // 强制刷新
        sessOS.flush();
        crcOut.close();
        sessOS.close();
    }
}
```


说明：该函数用于将header、sessions、dt序列化至本地snapshot文件中，并且在最后会写入"/"字符。该方法是同步的，即是线程安全的。

**四、总结**

FileSnap源码相对较简单，其主要是用于操作snapshot文件，也谢谢各位园友的观看~　　

**六、参考**
[源码分析之持久化(二)之FileSnap](https://www.cnblogs.com/leesf456/p/6285014.html)