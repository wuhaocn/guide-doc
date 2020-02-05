### [【Zookeeper】源码分析之序列化](https://www.cnblogs.com/leesf456/p/6278853.html)

### 一、前言

在完成了前面的理论学习后，现在可以从源码角度来解析Zookeeper的细节，首先笔者想从序列化入手，因为在网络通信、数据存储中都用到了序列化，下面开始分析。

### 二、序列化*

序列化主要在zookeeper.jute包中，其中涉及的主要接口如下

**· InputArchive**

**· OutputArchive**

**· Index**

**· Record**

#### InputArchive

其是所有反序列化器都需要实现的接口，其方法如下　

```java
public interface InputArchive {
    // 读取byte类型
    public byte readByte(String tag) throws IOException;
    // 读取boolean类型
    public boolean readBool(String tag) throws IOException;
    // 读取int类型
    public int readInt(String tag) throws IOException;
    // 读取long类型
    public long readLong(String tag) throws IOException;
    // 读取float类型
    public float readFloat(String tag) throws IOException;
    // 读取double类型
    public double readDouble(String tag) throws IOException;
    // 读取String类型
    public String readString(String tag) throws IOException;
    // 通过缓冲方式读取
    public byte[] readBuffer(String tag) throws IOException;
    // 开始读取记录
    public void readRecord(Record r, String tag) throws IOException;
    // 开始读取记录
    public void startRecord(String tag) throws IOException;
    // 结束读取记录
    public void endRecord(String tag) throws IOException;
    // 开始读取向量
    public Index startVector(String tag) throws IOException;
    // 结束读取向量
    public void endVector(String tag) throws IOException;
    // 开始读取Map
    public Index startMap(String tag) throws IOException;
    // 结束读取Map
    public void endMap(String tag) throws IOException;
}

```


InputArchive的类结构如下

![](https://images2015.cnblogs.com/blog/616953/201701/616953-20170112101638572-2015760788.png)

##### BinaryInputArchive　　

```java
public class BinaryInputArchive implements InputArchive {
    // DataInput接口，用于从二进制流中读取字节
    private DataInput in;
    
    // 静态方法，用于获取Archive
    static public BinaryInputArchive getArchive(InputStream strm) {
        return new BinaryInputArchive(new DataInputStream(strm));
    }
    
    // 内部类，对应BinaryInputArchive索引
    static private class BinaryIndex implements Index {
        private int nelems;
        BinaryIndex(int nelems) {
            this.nelems = nelems;
        }
        public boolean done() {
            return (nelems <= 0);
        }
        public void incr() {
            nelems--;
        }
    }
    /** Creates a new instance of BinaryInputArchive */
    // 构造函数
    public BinaryInputArchive(DataInput in) {
        this.in = in;
    }
    
    // 读取字节
    public byte readByte(String tag) throws IOException {
        return in.readByte();
    }
    
    // 读取boolean类型
    public boolean readBool(String tag) throws IOException {
        return in.readBoolean();
    }
    
    // 读取int类型
    public int readInt(String tag) throws IOException {
        return in.readInt();
    }
    
    // 读取long类型
    public long readLong(String tag) throws IOException {
        return in.readLong();
    }
    
    // 读取float类型
    public float readFloat(String tag) throws IOException {
        return in.readFloat();
    }
    
    // 读取double类型
    public double readDouble(String tag) throws IOException {
        return in.readDouble();
    }
    
    // 读取String类型
    public String readString(String tag) throws IOException {
        // 确定长度
        int len = in.readInt();
        if (len == -1) return null;
        byte b[] = new byte[len];
        // 从输入流中读取一些字节，并将它们存储在缓冲区数组b中
        in.readFully(b);
        return new String(b, "UTF8");
    }
    
    // 最大缓冲值
    static public final int maxBuffer = Integer.getInteger("jute.maxbuffer", 0xfffff);

    // 读取缓冲
    public byte[] readBuffer(String tag) throws IOException {
        // 确定长度
        int len = readInt(tag);
        if (len == -1) return null;
        // Since this is a rough sanity check, add some padding to maxBuffer to
        // make up for extra fields, etc. (otherwise e.g. clients may be able to
        // write buffers larger than we can read from disk!)
        if (len < 0 || len > maxBuffer + 1024) { // 检查长度是否合理
            throw new IOException("Unreasonable length = " + len);
        }
        byte[] arr = new byte[len];
        // 从输入流中读取一些字节，并将它们存储在缓冲区数组arr中
        in.readFully(arr);
        return arr;
    }
    
    // 读取记录
    public void readRecord(Record r, String tag) throws IOException {
        // 反序列化，动态调用
        r.deserialize(this, tag);
    }
    
    // 开始读取记录，实现为空
    public void startRecord(String tag) throws IOException {}
    
    // 结束读取记录，实现为空
    public void endRecord(String tag) throws IOException {}
    
    // 开始读取向量
    public Index startVector(String tag) throws IOException {
        // 确定长度
        int len = readInt(tag);
        if (len == -1) {
            return null;
        }
        // 返回索引
        return new BinaryIndex(len);
    }
    
    // 结束读取向量
    public void endVector(String tag) throws IOException {}
    
    // 开始读取Map
    public Index startMap(String tag) throws IOException {
        // 返回索引
        return new BinaryIndex(readInt(tag));
    }
    
    // 结束读取Map，实现为空
    public void endMap(String tag) throws IOException {}
    
}
```


##### CsvInputArchive　


```java
/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jute;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.io.UnsupportedEncodingException;

/**
 *
 */
class CsvInputArchive implements InputArchive {
    // 推回字节流
    private PushbackReader stream;
   
    // 内部类，对应CsvInputArchive索引
    private class CsvIndex implements Index {
        public boolean done() {
            char c = '\0';
            try {
                c = (char) stream.read();
                stream.unread(c);
            } catch (IOException ex) {
            }
            return (c == '}') ? true : false;
        }
        public void incr() {}
    }
    
    // 私有方法，抛出异常
    private void throwExceptionOnError(String tag) throws IOException {
        throw new IOException("Error deserializing "+tag);
    }
    
    // 私有方法，读取字段
    private String readField(String tag) throws IOException {
        try {
            StringBuilder buf = new StringBuilder();
            while (true) { 
                // 读取并转化为字符
                char c = (char) stream.read();
                switch (c) { // 判断字符
                    case ',':
                        // 读取字段完成，可直接返回
                        return buf.toString();
                    case '}':
                    case '\n':
                    case '\r':
                        // 推回缓冲区
                        stream.unread(c);
                        return buf.toString();
                    default: // 默认添加至buf中
                        buf.append(c);
                }
            }
        } catch (IOException ex) {
            throw new IOException("Error reading "+tag);
        }
    }
    
    // 获取CsvInputArchive
    static CsvInputArchive getArchive(InputStream strm)
    throws UnsupportedEncodingException {
        return new CsvInputArchive(strm);
    }
    
    /** Creates a new instance of CsvInputArchive */
    // 构造函数
    public CsvInputArchive(InputStream in)
    throws UnsupportedEncodingException {
        // 初始化stream属性
        stream = new PushbackReader(new InputStreamReader(in, "UTF-8"));
    }
    
    // 读取byte类型
    public byte readByte(String tag) throws IOException {
        return (byte) readLong(tag);
    }
    
    // 读取boolean类型
    public boolean readBool(String tag) throws IOException {
        String sval = readField(tag);
        return "T".equals(sval) ? true : false;
    }
    
    // 读取int类型
    public int readInt(String tag) throws IOException {
        return (int) readLong(tag);
    }
    
    // 读取long类型
    public long readLong(String tag) throws IOException {
        // 读取字段
        String sval = readField(tag);
        try {
            // 转化
            long lval = Long.parseLong(sval);
            return lval;
        } catch (NumberFormatException ex) {
            throw new IOException("Error deserializing "+tag);
        }
    }
    
    // 读取float类型
    public float readFloat(String tag) throws IOException {
        return (float) readDouble(tag);
    }
    
    // 读取double类型
    public double readDouble(String tag) throws IOException {
        // 读取字段
        String sval = readField(tag);
        try {
            // 转化
            double dval = Double.parseDouble(sval);
            return dval;
        } catch (NumberFormatException ex) {
            throw new IOException("Error deserializing "+tag);
        }
    }
    
    // 读取String类型
    public String readString(String tag) throws IOException {
        // 读取字段
        String sval = readField(tag);
        // 转化
        return Utils.fromCSVString(sval);
        
    }
    
    // 读取缓冲类型
    public byte[] readBuffer(String tag) throws IOException {
        // 读取字段
        String sval = readField(tag);
        // 转化
        return Utils.fromCSVBuffer(sval);
    }
    
    // 读取记录
    public void readRecord(Record r, String tag) throws IOException {
        // 反序列化
        r.deserialize(this, tag);
    }
    
    // 开始读取记录
    public void startRecord(String tag) throws IOException {
        if (tag != null && !"".equals(tag)) { 
            // 读取并转化为字符
            char c1 = (char) stream.read();
            // 读取并转化为字符
            char c2 = (char) stream.read();
            if (c1 != 's' || c2 != '{') { // 进行判断
                throw new IOException("Error deserializing "+tag);
            }
        }
    }
    
    // 结束读取记录
    public void endRecord(String tag) throws IOException {
        // 读取并转化为字符
        char c = (char) stream.read();
        if (tag == null || "".equals(tag)) {
            if (c != '\n' && c != '\r') { // 进行判断
                throw new IOException("Error deserializing record.");
            } else {
                return;
            }
        }
        
        if (c != '}') { // 进行判断
            throw new IOException("Error deserializing "+tag);
        }
        // 读取并转化为字符
        c = (char) stream.read();
        if (c != ',') { 
            // 推回缓冲区
            stream.unread(c);
        }
        
        return;
    }
    
    // 开始读取vector
    public Index startVector(String tag) throws IOException {
        char c1 = (char) stream.read();
        char c2 = (char) stream.read();
        if (c1 != 'v' || c2 != '{') {
            throw new IOException("Error deserializing "+tag);
        }
        return new CsvIndex();
    }
    
    // 结束读取vector
    public void endVector(String tag) throws IOException {
        char c = (char) stream.read();
        if (c != '}') {
            throw new IOException("Error deserializing "+tag);
        }
        c = (char) stream.read();
        if (c != ',') {
            stream.unread(c);
        }
        return;
    }
    
    // 开始读取Map
    public Index startMap(String tag) throws IOException {
        char c1 = (char) stream.read();
        char c2 = (char) stream.read();
        if (c1 != 'm' || c2 != '{') {
            throw new IOException("Error deserializing "+tag);
        }
        return new CsvIndex();
    }
    
    // 结束读取Map
    public void endMap(String tag) throws IOException {
        char c = (char) stream.read();
        if (c != '}') {
            throw new IOException("Error deserializing "+tag);
        }
        c = (char) stream.read();
        if (c != ',') {
            stream.unread(c);
        }
        return;
    }
}

```


#####  XmlInputArchive　


```java
/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jute;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
/**
 *
 */
class XmlInputArchive implements InputArchive {
    // 内部类，值（包含类型和值）
    static private class Value {
        private String type;
        private StringBuffer sb;
        
        public Value(String t) {
            type = t;
            sb = new StringBuffer();
        }
        
        // 添加chars
        public void addChars(char[] buf, int offset, int len) {
            sb.append(buf, offset, len);
        }
        
        // 返回value
        public String getValue() { return sb.toString(); }
        
        // 返回type
        public String getType() { return type; }
    }
    
    // 内部类，XML解析器
    private static class XMLParser extends DefaultHandler {
        private boolean charsValid = false;
        
        private ArrayList<Value> valList;
        
        private XMLParser(ArrayList<Value> vlist) {
            valList = vlist;
        }
        
        // 文档开始，空的实现
        public void startDocument() throws SAXException {}
        
        // 文档结束，空的实现
        public void endDocument() throws SAXException {}
        
        // 开始解析元素
        public void startElement(String ns,
                String sname,
                String qname,
                Attributes attrs) throws SAXException {
            // 
            charsValid = false;
            if ("boolean".equals(qname) ||        // boolean类型
                    "i4".equals(qname) ||        // 四个字节
                    "int".equals(qname) ||        // int类型
                    "string".equals(qname) ||    // String类型
                    "double".equals(qname) ||    // double类型
                    "ex:i1".equals(qname) ||    // 一个字节
                    "ex:i8".equals(qname) ||    // 八个字节
                    "ex:float".equals(qname)) { // 基本类型
                // 
                charsValid = true;
                // 添加至列表
                valList.add(new Value(qname));
            } else if ("struct".equals(qname) ||
                "array".equals(qname)) { // 结构体或数组类型
                // 添加至列表
                valList.add(new Value(qname));
            }
        }
        
        // 结束解析元素
        public void endElement(String ns,
                String sname,
                String qname) throws SAXException {
            charsValid = false;
            if ("struct".equals(qname) ||
                    "array".equals(qname)) { // 结构体或数组类型
                // 添加至列表
                valList.add(new Value("/"+qname));
            }
        }
        
        public void characters(char buf[], int offset, int len)
        throws SAXException {
            if (charsValid) { // 是否合法
                // 从列表获取value
                Value v = valList.get(valList.size()-1);
                // 将buf添加至value
                v.addChars(buf, offset,len);
            }
        }
        
    }
    
    // 内部类，对应XmlInputArchive
    private class XmlIndex implements Index {
        // 是否已经完成
        public boolean done() {
            // 根据索引获取value
            Value v = valList.get(vIdx);
            if ("/array".equals(v.getType())) { // 类型为/array
                // 设置开索引值为null
                valList.set(vIdx, null);
                // 增加索引值
                vIdx++;
                return true;
            } else {
                return false;
            }
        }
        // 增加索引值，空的实现
        public void incr() {}
    }
    
    // 值列表
    private ArrayList<Value> valList;
    // 值长度
    private int vLen;
    // 索引
    private int vIdx;
    
    // 下一项
    private Value next() throws IOException {
        if (vIdx < vLen) { // 当前索引值小于长度
            // 获取值
            Value v = valList.get(vIdx);
            // 设置索引值为null
            valList.set(vIdx, null);
            // 增加索引值
            vIdx++;
            return v;
        } else {
            throw new IOException("Error in deserialization.");
        }
    }
        
    // 获取XmlInputArchive
    static XmlInputArchive getArchive(InputStream strm)
    throws ParserConfigurationException, SAXException, IOException {
        return new XmlInputArchive(strm);
    }
    
    /** Creates a new instance of BinaryInputArchive */
    // 构造函数
    public XmlInputArchive(InputStream in)
    throws ParserConfigurationException, SAXException, IOException {
        // 初始化XmlInputArchive的相应字段
        valList = new ArrayList<Value>();
        DefaultHandler handler = new XMLParser(valList);
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        parser.parse(in, handler);
        vLen = valList.size();
        vIdx = 0;
    }
    
    // 读取byte类型
    public byte readByte(String tag) throws IOException {
        Value v = next();
        if (!"ex:i1".equals(v.getType())) {
            throw new IOException("Error deserializing "+tag+".");
        }
        return Byte.parseByte(v.getValue());
    }
    
    // 读取Boolean类型
    public boolean readBool(String tag) throws IOException {
        Value v = next();
        if (!"boolean".equals(v.getType())) {
            throw new IOException("Error deserializing "+tag+".");
        }
        return "1".equals(v.getValue());
    }
    
    // 读取int类型
    public int readInt(String tag) throws IOException {
        Value v = next();
        if (!"i4".equals(v.getType()) &&
                !"int".equals(v.getType())) {
            throw new IOException("Error deserializing "+tag+".");
        }
        return Integer.parseInt(v.getValue());
    }
    
    // 读取long类型
    public long readLong(String tag) throws IOException {
        Value v = next();
        if (!"ex:i8".equals(v.getType())) {
            throw new IOException("Error deserializing "+tag+".");
        }
        return Long.parseLong(v.getValue());
    }
    
    // 读取float类型
    public float readFloat(String tag) throws IOException {
        Value v = next();
        if (!"ex:float".equals(v.getType())) {
            throw new IOException("Error deserializing "+tag+".");
        }
        return Float.parseFloat(v.getValue());
    }
    
    // 读取double类型
    public double readDouble(String tag) throws IOException {
        Value v = next();
        if (!"double".equals(v.getType())) {
            throw new IOException("Error deserializing "+tag+".");
        }
        return Double.parseDouble(v.getValue());
    }
    
    // 读取String类型
    public String readString(String tag) throws IOException {
        Value v = next();
        if (!"string".equals(v.getType())) {
            throw new IOException("Error deserializing "+tag+".");
        }
        return Utils.fromXMLString(v.getValue());
    }
    
    // 读取Buffer类型
    public byte[] readBuffer(String tag) throws IOException {
        Value v = next();
        if (!"string".equals(v.getType())) {
            throw new IOException("Error deserializing "+tag+".");
        }
        return Utils.fromXMLBuffer(v.getValue());
    }
    
    // 读取Record类型
    public void readRecord(Record r, String tag) throws IOException {
        r.deserialize(this, tag);
    }
    
    // 开始读取Record
    public void startRecord(String tag) throws IOException {
        Value v = next();
        if (!"struct".equals(v.getType())) {
            throw new IOException("Error deserializing "+tag+".");
        }
    }
    
    // 结束读取Record
    public void endRecord(String tag) throws IOException {
        Value v = next();
        if (!"/struct".equals(v.getType())) {
            throw new IOException("Error deserializing "+tag+".");
        }
    }
    
    // 开始读取vector
    public Index startVector(String tag) throws IOException {
        Value v = next();
        if (!"array".equals(v.getType())) {
            throw new IOException("Error deserializing "+tag+".");
        }
        return new XmlIndex();
    }
    
    // 结束读取vector
    public void endVector(String tag) throws IOException {}
    
    // 开始读取Map
    public Index startMap(String tag) throws IOException {
        return startVector(tag);
    }
    
    // 停止读取Map
    public void endMap(String tag) throws IOException { endVector(tag); }

}

```

#### OutputArchive

其是所有序列化器都需要实现此接口，其方法如下。　　

```java
public interface OutputArchive {
    // 写Byte类型
    public void writeByte(byte b, String tag) throws IOException;
    // 写boolean类型
    public void writeBool(boolean b, String tag) throws IOException;
    // 写int类型
    public void writeInt(int i, String tag) throws IOException;
    // 写long类型
    public void writeLong(long l, String tag) throws IOException;
    // 写float类型
    public void writeFloat(float f, String tag) throws IOException;
    // 写double类型
    public void writeDouble(double d, String tag) throws IOException;
    // 写String类型
    public void writeString(String s, String tag) throws IOException;
    // 写Buffer类型
    public void writeBuffer(byte buf[], String tag)
        throws IOException;
    // 写Record类型
    public void writeRecord(Record r, String tag) throws IOException;
    // 开始写Record
    public void startRecord(Record r, String tag) throws IOException;
    // 结束写Record
    public void endRecord(Record r, String tag) throws IOException;
    // 开始写Vector
    public void startVector(List v, String tag) throws IOException;
    // 结束写Vector
    public void endVector(List v, String tag) throws IOException;
    // 开始写Map
    public void startMap(TreeMap v, String tag) throws IOException;
    // 结束写Map
    public void endMap(TreeMap v, String tag) throws IOException;

}
```

OutputArchive的类结构如下

![](https://images2015.cnblogs.com/blog/616953/201701/616953-20170112151336510-681738850.png)

##### BinaryOutputArchive　

```
/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jute;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.TreeMap;

/**
 *
 */
public class BinaryOutputArchive implements OutputArchive {
    // 字节缓冲
    private ByteBuffer bb = ByteBuffer.allocate(1024);
    // DataInput接口，用于从二进制流中读取字节
    private DataOutput out;
    
    // 静态方法，用于获取Archive
    public static BinaryOutputArchive getArchive(OutputStream strm) {
        return new BinaryOutputArchive(new DataOutputStream(strm));
    }
    
    /** Creates a new instance of BinaryOutputArchive */
    // 构造函数
    public BinaryOutputArchive(DataOutput out) {
        this.out = out;
    }
    
    // 写Byte类型
    public void writeByte(byte b, String tag) throws IOException {
        out.writeByte(b);
    }
    
    // 写boolean类型
    public void writeBool(boolean b, String tag) throws IOException {
        out.writeBoolean(b);
    }
    
    // 写int类型
    public void writeInt(int i, String tag) throws IOException {
        out.writeInt(i);
    }
    
    // 写long类型
    public void writeLong(long l, String tag) throws IOException {
        out.writeLong(l);
    }
    
    // 写float类型
    public void writeFloat(float f, String tag) throws IOException {
        out.writeFloat(f);
    }
    
    // 写double类型
    public void writeDouble(double d, String tag) throws IOException {
        out.writeDouble(d);
    }
    
    /**
     * create our own char encoder to utf8. This is faster 
     * then string.getbytes(UTF8).
     * @param s the string to encode into utf8
     * @return utf8 byte sequence.
     */
    // 将String类型转化为ByteBuffer类型
    final private ByteBuffer stringToByteBuffer(CharSequence s) {
        // 清空ByteBuffer
        bb.clear();
        // s的长度
        final int len = s.length();
        for (int i = 0; i < len; i++) { // 遍历s
            if (bb.remaining() < 3) { // ByteBuffer剩余大小小于3
                // 再进行一次分配(扩大一倍)
                ByteBuffer n = ByteBuffer.allocate(bb.capacity() << 1);
                // 切换方式
                bb.flip();
                // 写入bb
                n.put(bb);
                bb = n;
            }
            char c = s.charAt(i);
            if (c < 0x80) { // 小于128，直接写入
                bb.put((byte) c);
            } else if (c < 0x800) { // 小于2048，则进行相应处理
                bb.put((byte) (0xc0 | (c >> 6)));
                bb.put((byte) (0x80 | (c & 0x3f)));
            } else { // 大于2048，则进行相应处理
                bb.put((byte) (0xe0 | (c >> 12)));
                bb.put((byte) (0x80 | ((c >> 6) & 0x3f)));
                bb.put((byte) (0x80 | (c & 0x3f)));
            }
        }
        // 切换方式
        bb.flip();
        return bb;
    }

    // 写String类型
    public void writeString(String s, String tag) throws IOException {
        if (s == null) {
            writeInt(-1, "len");
            return;
        }
        ByteBuffer bb = stringToByteBuffer(s);
        writeInt(bb.remaining(), "len");
        out.write(bb.array(), bb.position(), bb.limit());
    }
    
    // 写Buffer类型
    public void writeBuffer(byte barr[], String tag)
    throws IOException {
        if (barr == null) {
            out.writeInt(-1);
            return;
        }
        out.writeInt(barr.length);
        out.write(barr);
    }
    
    // 写Record类型
    public void writeRecord(Record r, String tag) throws IOException {
        r.serialize(this, tag);
    }
    
    // 开始写Record
    public void startRecord(Record r, String tag) throws IOException {}
    
    // 结束写Record
    public void endRecord(Record r, String tag) throws IOException {}
    
    // 开始写Vector
    public void startVector(List v, String tag) throws IOException {
        if (v == null) {
            writeInt(-1, tag);
            return;
        }
        writeInt(v.size(), tag);
    }
    
    // 结束写Vector
    public void endVector(List v, String tag) throws IOException {}
    
    // 开始写Map
    public void startMap(TreeMap v, String tag) throws IOException {
        writeInt(v.size(), tag);
    }
    
    // 结束写Map
    public void endMap(TreeMap v, String tag) throws IOException {}
    
}
```

##### CsvOutputArchive　

![](https://images.cnblogs.com/OutliningIndicators/ContractedBlock.gif)![](https://images.cnblogs.com/OutliningIndicators/ExpandedBlockStart.gif)

```
/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jute;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.TreeMap;

/**
 *
 */
public class CsvOutputArchive implements OutputArchive {
    // PrintStream为其他输出流添加了功能，使它们能够方便地打印各种数据值表示形式
    private PrintStream stream;
    // 默认为第一次
    private boolean isFirst = true;
    
    // 获取Archive
    static CsvOutputArchive getArchive(OutputStream strm)
    throws UnsupportedEncodingException {
        return new CsvOutputArchive(strm);
    }
    
    // 私有函数，抛出异常
    private void throwExceptionOnError(String tag) throws IOException {
        if (stream.checkError()) {
            throw new IOException("Error serializing "+tag);
        }
    }
 
    // 私有函数，除第一次外，均打印","
    private void printCommaUnlessFirst() {
        if (!isFirst) {
            stream.print(",");
        }
        isFirst = false;
    }
    
    /** Creates a new instance of CsvOutputArchive */
    // 构造函数
    public CsvOutputArchive(OutputStream out)
    throws UnsupportedEncodingException {
        stream = new PrintStream(out, true, "UTF-8");
    }
    
    // 写Byte类型
    public void writeByte(byte b, String tag) throws IOException {
        writeLong((long)b, tag);
    }
    
    // 写boolean类型
    public void writeBool(boolean b, String tag) throws IOException {
        // 打印","
        printCommaUnlessFirst();
        String val = b ? "T" : "F";
        // 打印值
        stream.print(val);
        // 抛出异常
        throwExceptionOnError(tag);
    }

    // 写int类型
    public void writeInt(int i, String tag) throws IOException {
        writeLong((long)i, tag);
    }
    
    // 写long类型
    public void writeLong(long l, String tag) throws IOException {
        printCommaUnlessFirst();
        stream.print(l);
        throwExceptionOnError(tag);
    }
    
    // 写float类型
    public void writeFloat(float f, String tag) throws IOException {
        writeDouble((double)f, tag);
    }
    
    // 写double类型
    public void writeDouble(double d, String tag) throws IOException {
        printCommaUnlessFirst();
        stream.print(d);
        throwExceptionOnError(tag);
    }
    
    // 写String类型
    public void writeString(String s, String tag) throws IOException {
        printCommaUnlessFirst();
        stream.print(Utils.toCSVString(s));
        throwExceptionOnError(tag);
    }
    
    // 写Buffer类型
    public void writeBuffer(byte buf[], String tag)
    throws IOException {
        printCommaUnlessFirst();
        stream.print(Utils.toCSVBuffer(buf));
        throwExceptionOnError(tag);
    }
    
    // 写Record类型
    public void writeRecord(Record r, String tag) throws IOException {
        if (r == null) {
            return;
        }
        r.serialize(this, tag);
    }
    
    // 开始写Record
    public void startRecord(Record r, String tag) throws IOException {
        if (tag != null && !"".equals(tag)) {
            printCommaUnlessFirst();
            stream.print("s{");
            isFirst = true;
        }
    }
    
    // 结束写Record
    public void endRecord(Record r, String tag) throws IOException {
        if (tag == null || "".equals(tag)) {
            stream.print("\n");
            isFirst = true;
        } else {
            stream.print("}");
            isFirst = false;
        }
    }
    
    // 开始写Vector
    public void startVector(List v, String tag) throws IOException {
        printCommaUnlessFirst();
        stream.print("v{");
        isFirst = true;
    }
    
    // 结束写Vector
    public void endVector(List v, String tag) throws IOException {
        stream.print("}");
        isFirst = false;
    }
    
    // 开始写Map
    public void startMap(TreeMap v, String tag) throws IOException {
        printCommaUnlessFirst();
        stream.print("m{");
        isFirst = true;
    }
    
    // 结束写Map
    public void endMap(TreeMap v, String tag) throws IOException {
        stream.print("}");
        isFirst = false;
    }
}
```

##### XmlOutputArchive


```
/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jute;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Stack;
import java.util.TreeMap;

/**
 *
 */
class XmlOutputArchive implements OutputArchive {
    // PrintStream为其他输出流添加了功能，使它们能够方便地打印各种数据值表示形式
    private PrintStream stream;
    
    // 缩进个数
    private int indent = 0;
    
    // 栈结构
    private Stack<String> compoundStack;
    
    // 存放缩进
    private void putIndent() {
        StringBuilder sb = new StringBuilder("");
        for (int idx = 0; idx < indent; idx++) {
            sb.append("  ");
        }
        stream.print(sb.toString());
    }
    
    // 添加缩进
    private void addIndent() {
        indent++;
    }
    
    // 减少缩进
    private void closeIndent() {
        indent--;
    }
    
    // 打印文件头格式
    private void printBeginEnvelope(String tag) {
        if (!compoundStack.empty()) {
            String s = compoundStack.peek();
            if ("struct".equals(s)) {
                putIndent();
                stream.print("<member>\n");
                addIndent();
                putIndent();
                stream.print("<name>"+tag+"</name>\n");
                putIndent();
                stream.print("<value>");
            } else if ("vector".equals(s)) {
                stream.print("<value>");
            } else if ("map".equals(s)) {
                stream.print("<value>");
            }
        } else {
            stream.print("<value>");
        }
    }
    
    // 打印文件尾格式
    private void printEndEnvelope(String tag) {
        if (!compoundStack.empty()) {
            String s = compoundStack.peek();
            if ("struct".equals(s)) {
                stream.print("</value>\n");
                closeIndent();
                putIndent();
                stream.print("</member>\n");
            } else if ("vector".equals(s)) {
                stream.print("</value>\n");
            } else if ("map".equals(s)) {
                stream.print("</value>\n");
            }
        } else {
            stream.print("</value>\n");
        }
    }
    
    // 
    private void insideVector(String tag) {
        printBeginEnvelope(tag);
        compoundStack.push("vector");
    }
    
    private void outsideVector(String tag) throws IOException {
        String s = compoundStack.pop();
        if (!"vector".equals(s)) {
            throw new IOException("Error serializing vector.");
        }
        printEndEnvelope(tag);
    }
    
    private void insideMap(String tag) {
        printBeginEnvelope(tag);
        compoundStack.push("map");
    }
    
    private void outsideMap(String tag) throws IOException {
        String s = compoundStack.pop();
        if (!"map".equals(s)) {
            throw new IOException("Error serializing map.");
        }
        printEndEnvelope(tag);
    }
    
    private void insideRecord(String tag) {
        printBeginEnvelope(tag);
        compoundStack.push("struct");
    }
    
    private void outsideRecord(String tag) throws IOException {
        String s = compoundStack.pop();
        if (!"struct".equals(s)) {
            throw new IOException("Error serializing record.");
        }
        printEndEnvelope(tag);
    }
    
    // 获取Archive
    static XmlOutputArchive getArchive(OutputStream strm) {
        return new XmlOutputArchive(strm);
    }
    
    /** Creates a new instance of XmlOutputArchive */
    // 构造函数
    public XmlOutputArchive(OutputStream out) {
        stream = new PrintStream(out);
        compoundStack = new Stack<String>();
    }
    
    // 写Byte类型
    public void writeByte(byte b, String tag) throws IOException {
        printBeginEnvelope(tag);
        stream.print("<ex:i1>");
        stream.print(Byte.toString(b));
        stream.print("</ex:i1>");
        printEndEnvelope(tag);
    }
    
    // 写boolean类型
    public void writeBool(boolean b, String tag) throws IOException {
        printBeginEnvelope(tag);
        stream.print("<boolean>");
        stream.print(b ? "1" : "0");
        stream.print("</boolean>");
        printEndEnvelope(tag);
    }
    
    // 写int类型
    public void writeInt(int i, String tag) throws IOException {
        printBeginEnvelope(tag);
        stream.print("<i4>");
        stream.print(Integer.toString(i));
        stream.print("</i4>");
        printEndEnvelope(tag);
    }
    
    // 写long类型
    public void writeLong(long l, String tag) throws IOException {
        printBeginEnvelope(tag);
        stream.print("<ex:i8>");
        stream.print(Long.toString(l));
        stream.print("</ex:i8>");
        printEndEnvelope(tag);
    }
    
    // 写float类型
    public void writeFloat(float f, String tag) throws IOException {
        printBeginEnvelope(tag);
        stream.print("<ex:float>");
        stream.print(Float.toString(f));
        stream.print("</ex:float>");
        printEndEnvelope(tag);
    }
    
    // 写double类型
    public void writeDouble(double d, String tag) throws IOException {
        printBeginEnvelope(tag);
        stream.print("<double>");
        stream.print(Double.toString(d));
        stream.print("</double>");
        printEndEnvelope(tag);
    }
    
    // 写String类型
    public void writeString(String s, String tag) throws IOException {
        printBeginEnvelope(tag);
        stream.print("<string>");
        stream.print(Utils.toXMLString(s));
        stream.print("</string>");
        printEndEnvelope(tag);
    }
    
    // 写Buffer类型
    public void writeBuffer(byte buf[], String tag)
    throws IOException {
        printBeginEnvelope(tag);
        stream.print("<string>");
        stream.print(Utils.toXMLBuffer(buf));
        stream.print("</string>");
        printEndEnvelope(tag);
    }
    
    // 写Record类型
    public void writeRecord(Record r, String tag) throws IOException {
        r.serialize(this, tag);
    }
    
    // 开始写Record类型
    public void startRecord(Record r, String tag) throws IOException {
        insideRecord(tag);
        stream.print("<struct>\n");
        addIndent();
    }
    
    // 结束写Record类型
    public void endRecord(Record r, String tag) throws IOException {
        closeIndent();
        putIndent();
        stream.print("</struct>");
        outsideRecord(tag);
    }
    
    // 开始写Vector类型
    public void startVector(List v, String tag) throws IOException {
        insideVector(tag);
        stream.print("<array>\n");
        addIndent();
    }
    
    // 结束写Vector类型
    public void endVector(List v, String tag) throws IOException {
        closeIndent();
        putIndent();
        stream.print("</array>");
        outsideVector(tag);
    }
    
    // 开始写Map类型
    public void startMap(TreeMap v, String tag) throws IOException {
        insideMap(tag);
        stream.print("<array>\n");
        addIndent();
    }
    
    // 结束写Map类型
    public void endMap(TreeMap v, String tag) throws IOException {
        closeIndent();
        putIndent();
        stream.print("</array>");
        outsideMap(tag);
    }

}

```

#### Index

其用于迭代反序列化器的迭代器。　　

```
public interface Index {
    // 是否已经完成
    public boolean done();
    // 下一项
    public void incr();
}
```


Index的类结构如下

![](https://images2015.cnblogs.com/blog/616953/201701/616953-20170112102418775-901328202.png)

##### BinaryIndex　

```
static private class BinaryIndex implements Index {
    // 元素个数
    private int nelems;
    // 构造函数
    BinaryIndex(int nelems) {
        this.nelems = nelems;
    }
    // 是否已经完成
    public boolean done() {
        return (nelems <= 0);
    }
    // 移动一项
    public void incr() {
        nelems--;
    }
}
```


##### CsxIndex　


```
private class CsvIndex implements Index {
    // 是否已经完成
    public boolean done() {
        char c = '\0';
        try {
            // 读取字符
            c = (char) stream.read();
            // 推回缓冲区 
            stream.unread(c);
        } catch (IOException ex) {
        }
        return (c == '}') ? true : false;
    }
    // 什么都不做
    public void incr() {}
}
```


##### XmlIndex　


```
private class XmlIndex implements Index {
    // 是否已经完成
    public boolean done() {
        // 根据索引获取值
        Value v = valList.get(vIdx);
        if ("/array".equals(v.getType())) { // 判断是否值的类型是否为/array
            // 设置索引的值
            valList.set(vIdx, null);
            // 索引加1
            vIdx++;
            return true;
        } else {
            return false;
        }
    }
    // 什么都不做
    public void incr() {}
}
```


#### Record

所有用于网络传输或者本地存储的类型都实现该接口，其方法如下　　

```
public interface Record {
    // 序列化
    public void serialize(OutputArchive archive, String tag)
        throws IOException;
    // 反序列化
    public void deserialize(InputArchive archive, String tag)
        throws IOException;
}
```


所有的实现类都需要实现seriallize和deserialize方法。

### 三、示例

下面通过一个示例来理解OutputArchive和InputArchive的搭配使用。　

```
package com.leesf.zookeeper_samples;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.TreeMap;

import org.apache.jute.BinaryInputArchive;
import org.apache.jute.BinaryOutputArchive;
import org.apache.jute.Index;
import org.apache.jute.InputArchive;
import org.apache.jute.OutputArchive;
import org.apache.jute.Record;

public class ArchiveTest {
    public static void main( String[] args ) throws IOException {
        String path = "F:\\test.txt";
        // write operation
        OutputStream outputStream = new FileOutputStream(new File(path));
        BinaryOutputArchive binaryOutputArchive = BinaryOutputArchive.getArchive(outputStream);
        
        binaryOutputArchive.writeBool(true, "boolean");
        byte[] bytes = "leesf".getBytes();
        binaryOutputArchive.writeBuffer(bytes, "buffer");
        binaryOutputArchive.writeDouble(13.14, "double");
        binaryOutputArchive.writeFloat(5.20f, "float");
        binaryOutputArchive.writeInt(520, "int");
        Person person = new Person(25, "leesf");
        binaryOutputArchive.writeRecord(person, "leesf");
        TreeMap<String, Integer> map = new TreeMap<String, Integer>();
        map.put("leesf", 25);
        map.put("dyd", 25);
        Set<String> keys = map.keySet();
        binaryOutputArchive.startMap(map, "map");
        int i = 0;
        for (String key: keys) {
            String tag = i + "";
            binaryOutputArchive.writeString(key, tag);
            binaryOutputArchive.writeInt(map.get(key), tag);
            i++;
        }
        
        binaryOutputArchive.endMap(map, "map");
        
        
        // read operation
        InputStream inputStream = new FileInputStream(new File(path));
        BinaryInputArchive binaryInputArchive = BinaryInputArchive.getArchive(inputStream);
        
        System.out.println(binaryInputArchive.readBool("boolean"));
        System.out.println(new String(binaryInputArchive.readBuffer("buffer")));
        System.out.println(binaryInputArchive.readDouble("double"));
        System.out.println(binaryInputArchive.readFloat("float"));
        System.out.println(binaryInputArchive.readInt("int"));
        Person person2 = new Person();
        binaryInputArchive.readRecord(person2, "leesf");
        System.out.println(person2);       
        
        Index index = binaryInputArchive.startMap("map");
        int j = 0;
        while (!index.done()) {
            String tag = j + "";
            System.out.println("key = " + binaryInputArchive.readString(tag) 
                + ", value = " + binaryInputArchive.readInt(tag));
            index.incr();
            j++;
        }
    }
    
    static class Person implements Record {
        private int age;
        private String name;
        
        public Person() {
            
        }
        
        public Person(int age, String name) {
            this.age = age;
            this.name = name;
        }

        public void serialize(OutputArchive archive, String tag) throws IOException {
            archive.startRecord(this, tag);
            archive.writeInt(age, "age");
            archive.writeString(name, "name");
            archive.endRecord(this, tag);
        }

        public void deserialize(InputArchive archive, String tag) throws IOException {
            archive.startRecord(tag);
            age = archive.readInt("age");
            name = archive.readString("name");
            archive.endRecord(tag);            
        }    
        
        public String toString() {
            return "age = " + age + ", name = " + name;
        }
    }
}
```


运行结果：　　


```
true
leesf
13.14
5.2
age = 25, name = leesf
key = dyd, value = 25
key = leesf, value = 25
```


### 四、总结

本篇博文分析了序列化中涉及到的类，主要是org.zookeeper.jute包下的类，相对来说还是相对简单，也谢谢各位园友的观看~

### 五、参考

[源码分析之序列化](https://www.cnblogs.com/leesf456/p/6278853.html)