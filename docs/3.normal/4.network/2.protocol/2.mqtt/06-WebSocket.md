# 第六章 使用 WebSocket 作为网络层

## 目录

- [第一章 - 介绍](01-Introduction.md)
- [第二章 – MQTT 控制报文格式](02-ControlPacketFormat.md)
- [第三章 – MQTT 控制报文](03-ControlPackets.md)
- [第四章 – 操作行为](04-OperationalBehavior.md)
- [第五章 – 安全](05-Security.md)
- [第六章 – 使用 WebSocket](06-WebSocket.md)
- [第七章 – 一致性目标](07-Conformance.md)
- [附录 B - 强制性规范声明](08-AppendixB.md)

如果 MQTT 在 WebSocket [\[RFC6455\]](#RFC6455) 连接上传输，**必须**满足下面的条件：

- MQTT 控制报文**必须**使用 WebSocket 二进制数据帧发送。如果收到任何其它类型的数据帧，接收者**必须**关闭网络连接 \[MQTT-6.0.0-1\]。

- 单个 WebSocket 数据帧可以包含多个或者部分 MQTT 报文。接收者**不能**假设 MQTT 控制报文按 WebSocket 帧边界对齐 \[MQTT-6.0.0-2\]。

- 客户端**必须**将字符串 **mqtt** 包含在它提供的 WebSocket 子协议列表里 \[MQTT-6.0.0-3\]。

- 服务端选择和返回的 WebSocket 子协议名**必须**是 **mqtt** \[MQTT-6.0.0-4\] 。

- 用于连接客户端和服务器的 WebSocket URI 对 MQTT 协议没有任何影响。

## 6.1 IANA 注意事项 IANA Considerations

> 本规范请求 IANA 在 WebSocket 子协议名条目下注册 WebSocket MQTT 子协议，使用下列数据：

##### 图例 6.1 - IANA WebSocket 标识符

| 子协议标识符 | mqtt                                                         |
| ------------ | ------------------------------------------------------------ |
| 子协议通用名 | mqtt                                                         |
| 子协议定义   | http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/mqtt-v3.1.1.html |

### 项目主页

- [MQTT 协议中文版](https://github.com/mcxiaoke/mqtt)
