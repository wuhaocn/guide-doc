## 3.8 SUBSCRIBE - 订阅主题

客户端向服务端发送 SUBSCRIBE 报文用于创建一个或多个订阅。每个订阅注册客户端关心的一个或多个主题。为了将应用消息转发给与那些订阅匹配的主题，服务端发送 PUBLISH 报文给客户端。SUBSCRIBE 报文也（为每个订阅）指定了最大的 QoS 等级，服务端根据这个发送应用消息给客户端。

### 3.8.1 固定报头

##### 图例 3.20 – SUBSCRIBE 报文固定报头

| **Bit** | **7**                 | **6**  | **5** | **4** | **3** | **2** | **1** | **0** |
| ------- | --------------------- | ------ | ----- | ----- | ----- | ----- | ----- | ----- |
| byte 1  | MQTT 控制报文类型 (8) | 保留位 |
|         | 1                     | 0      | 0     | 0     | 0     | 0     | 1     | 0     |
| byte 2  | 剩余长度              |

<table style="text-align:center">
   <tr>
     <td align="center"><strong>Bit</strong></td>
     <td align="center"><strong>7</strong></td>
     <td align="center"><strong>6</strong></td>
     <td align="center"><strong>5</strong></td>
     <td align="center"><strong>4</strong></td>
     <td align="center"><strong>3</strong></td>
     <td align="center"><strong>2</strong></td>
     <td align="center"><strong>1</strong></td>
     <td align="center"><strong>0</strong></td>
   </tr>
   <tr>
     <td>byte 1</td>
     <td colspan="4" align="center">MQTT控制报文类型 (8)</td>
     <td colspan="4" align="center">保留位</td>
   </tr>
   <tr>
       <td></td>
       <td align="center">1</td>
       <td align="center">0</td>
       <td align="center">0</td>
       <td align="center">0</td>
       <td align="center">0</td>
       <td align="center">0</td>
       <td align="center">0</td>
       <td align="center">0</td>
     </tr>
   <tr>
     <td>byte 2</td>
     <td colspan="8" align="center">剩余长度</td>
   </tr>
 </table>

SUBSCRIBE 控制报固定报头的第 3,2,1,0 位是保留位，**必须**分别设置为 0,0,1,0。服务端**必须**将其它的任何值都当做是不合法的并关闭网络连接 \[MQTT-3.8.1-1\]。

**剩余长度字段**

等于可变报头的长度（2 字节）加上有效载荷的长度。

### 3.8.2 可变报头

可变报头包含报文标识符。2.3.1 提供了有关报文标识符的更多信息。

#### 可变报头非规范示例

[图例 3.21 – 报文标识符等于 10 的可变报头，非规范示例](#_图例_3.21_–) 展示了报文标识符设置为 10 时的可变报头。

##### 图例 3.21 – 报文标识符等于 10 的可变报头，非规范示例

|            | **描述**            | **7** | **6** | **5** | **4** | **3** | **2** | **1** | **0** |
| ---------- | ------------------- | ----- | ----- | ----- | ----- | ----- | ----- | ----- | ----- |
| 报文标识符 |
| byte 1     | 报文标识符 MSB (0)  | 0     | 0     | 0     | 0     | 0     | 0     | 0     | 0     |
| byte 2     | 报文标识符 LSB (10) | 0     | 0     | 0     | 0     | 1     | 0     | 1     | 0     |

### 3.8.3 有效载荷

SUBSCRIBE 报文的有效载荷包含了一个主题过滤器列表，它们表示客户端想要订阅的主题。SUBSCRIBE 报文有效载荷中的主题过滤器列表**必须**是 1.5.3 节定义的 UTF-8 字符串 \[MQTT-3.8.3-1\]。服务端**应该**支持包含通配符（4.7.1 节定义的）的主题过滤器。如果服务端选择不支持包含通配符的主题过滤器，**必须**拒绝任何包含通配符过滤器的订阅请求 \[MQTT-3.8.3-2\]。每一个过滤器后面跟着一个字节，这个字节被叫做 服务质量要求（Requested QoS）。它给出了服务端向客户端发送应用消息所允许的最大 QoS 等级。

SUBSCRIBE 报文的有效载荷**必须**包含至少一对主题过滤器 和 QoS 等级字段组合。没有有效载荷的 SUBSCRIBE 报文是违反协议的 \[MQTT-3.8.3-3\]。有关错误处理的信息请查看 4.8 节。

请求的最大服务质量等级字段编码为一个字节，它后面跟着 UTF-8 编码的主题名，那些主题过滤器 /和 QoS 等级组合是连续地打包。

##### 图例 3.22 – SUBSCRIBE 报文有效载荷格式

| **描述**                      | **7**                      | **6**        | **5** | **4** | **3** | **2** | **1** | **0** |
| ----------------------------- | -------------------------- | ------------ | ----- | ----- | ----- | ----- | ----- | ----- |
| 主题过滤器                    |
| byte 1                        | 长度 MSB                   |
| byte 2                        | 长度 LSB                   |
| bytes 3..N                    | 主题过滤器（Topic Filter） |
| 服务质量要求（Requested QoS） |
|                               | 保留位                     | 服务质量等级 |
| byte N+1                      | 0                          | 0            | 0     | 0     | 0     | 0     | X     | X     |

<table style="text-align:center">
   <tr>
     <td align="center"><strong>描述</strong></td>
     <td align="center"><strong>7</strong></td>
     <td align="center"><strong>6</strong></td>
     <td align="center"><strong>5</strong></td>
     <td align="center"><strong>4</strong></td>
     <td align="center"><strong>3</strong></td>
     <td align="center"><strong>2</strong></td>
     <td align="center"><strong>1</strong></td>
     <td align="center"><strong>0</strong></td>
   </tr>
   <tr>
     <td colspan="9">主题过滤器</td>
   </tr>
    <tr>
     <td>byte 1</td>
     <td colspan="8" align="center">长度 MSB</td>
   </tr>
    <tr>
     <td>byte 2</td>
     <td colspan="8" align="center">长度 LSB</td>
   </tr>
   <tr>
     <td>byte 3..N</td>
     <td colspan="8" align="center">主题过滤器（Topic Filter）</td>
   </tr>
   <tr>
     <td colspan="9">服务质量要求（Requested QoS）</td>
   </tr>
    <tr>
     <td></td>
     <td colspan="6" align="center">保留位</td>
     <td colspan="2" align="center">服务质量等级</td>
   </tr>
   <tr>
       <td> byte N+1</td>
       <td align="center">0</td>
       <td align="center">0</td>
       <td align="center">0</td>
       <td align="center">0</td>
       <td align="center">0</td>
       <td align="center">0</td>
       <td align="center">X</td>
       <td align="center">X</td>
     </tr>
 </table>

当前版本的协议没有用到服务质量要求（Requested QoS）字节的高六位。如果有效载荷中的任何位是非零值，或者 QoS 不等于 0,1 或 2，服务端**必须**认为 SUBSCRIBE 报文是不合法的并关闭网络连接 \[MQTT-3-8.3-4\]。

#### 有效载荷非规范示例

> [图例 3.23 – 有效载荷字节格式非规范示例](#_Figure_3.23_-) 展示了 [表格 3.5 – 有效载荷非规范示例](#_Table_3.4_-) 中简略描述的 SUBSCRIBE 报文的有效载荷。

##### 表格 3.5 – 有效载荷非规范示例

| 主题名       | “a/b” |
| ------------ | ----- |
| 服务质量要求 | 0x01  |
| 主题名       | “c/d” |
| 服务质量要求 | 0x02  |

##### 图例 3.23 – 有效载荷字节格式非规范示例

|                               | **描述**         | **7** | **6** | **5** | **4** | **3** | **2** | **1** | **0** |
| ----------------------------- | ---------------- | ----- | ----- | ----- | ----- | ----- | ----- | ----- | ----- |
| 主题过滤器（Topic Filter）    |
| byte 1                        | Length MSB (0)   | 0     | 0     | 0     | 0     | 0     | 0     | 0     | 0     |
| byte 2                        | Length LSB (3)   | 0     | 0     | 0     | 0     | 0     | 0     | 1     | 1     |
| byte 3                        | ‘a’ (0x61)       | 0     | 1     | 1     | 0     | 0     | 0     | 0     | 1     |
| byte 4                        | ‘/’ (0x2F)       | 0     | 0     | 1     | 0     | 1     | 1     | 1     | 1     |
| byte 5                        | ‘b’ (0x62)       | 0     | 1     | 1     | 0     | 0     | 0     | 1     | 0     |
| 服务质量要求（Requested QoS） |
| byte 6                        | Requested QoS(1) | 0     | 0     | 0     | 0     | 0     | 0     | 0     | 1     |
| 主题过滤器（Topic Filter）    |
| byte 7                        | Length MSB (0)   | 0     | 0     | 0     | 0     | 0     | 0     | 0     | 0     |
| byte 8                        | Length LSB (3)   | 0     | 0     | 0     | 0     | 0     | 0     | 1     | 1     |
| byte 9                        | ‘c’ (0x63)       | 0     | 1     | 1     | 0     | 0     | 0     | 1     | 1     |
| byte 10                       | ‘/’ (0x2F)       | 0     | 0     | 1     | 0     | 1     | 1     | 1     | 1     |
| byte 11                       | ‘d’ (0x64)       | 0     | 1     | 1     | 0     | 0     | 1     | 0     | 0     |
| 服务质量要求（Requested QoS） |
| byte 12                       | Requested QoS(2) | 0     | 0     | 0     | 0     | 0     | 0     | 1     | 0     |

### 3.8.4 响应

服务端收到客户端发送的一个 SUBSCRIBE 报文时，**必须**使用 SUBACK 报文响应 \[MQTT-3.8.4-1\]。SUBACK 报文**必须**和等待确认的 SUBSCRIBE 报文有相同的报文标识符 \[MQTT-3.8.4-2\]。

允许服务端在发送 SUBACK 报文之前就开始发送与订阅匹配的 PUBLISH 报文。

如果服务端收到一个 SUBSCRIBE 报文，报文的主题过滤器与一个现存订阅的主题过滤器相同，那么**必须**使用新的订阅彻底替换现存的订阅。新订阅的主题过滤器和之前订阅的相同，但是它的最大 QoS 值可以不同。与这个主题过滤器匹配的任何现存的保留消息**必须**被重发，但是发布流程**不能**中断 \[[MQTT-3](https://tools.oasis-open.org/issues/browse/MQTT-3).8.4-3\]。

如果主题过滤器不同于任何现存订阅的过滤器，服务端会创建一个新的订阅并发送所有匹配的保留消息。

如果服务端收到包含多个主题过滤器的 SUBSCRIBE 报文，它**必须**如同收到了一系列的多个 SUBSCRIBE 报文一样处理那个，除了需要将它们的响应合并到一个单独的 SUBACK 报文发送 \[MQTT-3.8.4-4\]。

服务端发送给客户端的 SUBACK 报文对每一对主题过滤器 和 QoS 等级都**必须**包含一个返回码。这个返回码**必须**表示那个订阅被授予的最大 QoS 等级，或者表示这个订阅失败 \[[MQTT-3](https://tools.oasis-open.org/issues/browse/MQTT-3).8.4-5\]。服务端可以授予比订阅者要求的低一些的 QoS 等级。为响应订阅而发出的消息的有效载荷的 QoS**必须**是原始发布消息的 QoS 和服务端授予的 QoS 两者中的最小值。如果原始消息的 QoS 是 1 而被授予的最大 QoS 是 0，允许服务端重复发送一个消息的副本给订阅者 \[[MQTT-3](https://tools.oasis-open.org/issues/browse/MQTT-3).8.4-6\]。

> **非规范示例**
> 对某个特定的主题过滤器，如果正在订阅的客户端被授予的最大 QoS 等级是 1，那么匹配这个过滤器的 QoS 等级 0 的应用消息会按 QoS 等级 0 分发给这个客户端。这意味着客户端最多收到这个消息的一个副本。从另一方面说，发布给同一主题的 QoS 等级 2 的消息会被服务端降级到 QoS 等级 1 再分发给客户端，因此客户端可能会收到重复的消息副本。
>
> 如果正在订阅的客户端被授予的最大 QoS 等级是 0，那么原来按 QoS 等级 2 发布给客户端的应用消息在繁忙时可能会丢失，但是服务端不应该发送重复的消息副本。发布给同一主题的 QoS 等级 1 的消息在传输给客户端时可能会丢失或重复。
>
> **非规范评注**
>
> 使用 QoS 等级 2 订阅一个主题过滤器等于是说：_我想要按照它们发布时的 QoS 等级接受匹配这个过滤器的消息_ 。这意味着，确定消息分发时可能的最大 QoS 等级是发布者的责任，而订阅者可以要求服务端降低 QoS 到更适合它的等级。

### 第三章目录 MQTT 控制报文

- [3.0 Contents – MQTT 控制报文](03-ControlPackets.md)
- [3.1 CONNECT – 连接服务端](0301-CONNECT.md)
- [3.2 CONNACK – 确认连接请求](0302-CONNACK.md)
- [3.3 PUBLISH – 发布消息](0303-PUBLISH.md)
- [3.4 PUBACK –发布确认](0304-PUBACK.md)
- [3.5 PUBREC – 发布收到（QoS 2，第一步）](0305-PUBREC.md)
- [3.6 PUBREL – 发布释放（QoS 2，第二步）](0306-PUBREL.md)
- [3.7 PUBCOMP – 发布完成（QoS 2，第三步）](0307-PUBCOMP.md)
- [3.8 SUBSCRIBE - 订阅主题](0308-SUBSCRIBE.md)
- [3.9 SUBACK – 订阅确认](0309-SUBACK.md)
- [3.10 UNSUBSCRIBE –取消订阅](0310-UNSUBSCRIBE.md)
- [3.11 UNSUBACK – 取消订阅确认](0311-UNSUBACK.md)
- [3.12 PINGREQ – 心跳请求](0312-PINGREQ.md)
- [3.13 PINGRESP – 心跳响应](0313-PINGRESP.md)
- [3.14 DISCONNECT –断开连接](0314-DISCONNECT.md)

### 项目主页

- [MQTT 协议中文版](https://github.com/mcxiaoke/mqtt)
