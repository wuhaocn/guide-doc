### 1.安装
    需安装tcp扩展插件
### 2.后端服务停止服务
    nginx-tcp代理握手失败，建立后立马关闭
    10.10.220.91:1111(nginx)->10.10.220.92:1111(服务)
    服务停止后,telnet 立马返回失败
    Trying 10.10.208.193...
    Connected to 10.10.208.193.
    Escape character is '^]'.
    Connection closed by foreign host.
    
    启动后中间停止：
    链接自动被掐断