#### ssl重协商攻击

##### SSL重协商：以不断的SSL密钥重协商来耗尽HTTPS服务器性能的一种攻击。
##### 如何测试SSL重协商是否禁用：
    使用openssl命令连接ssl端口，输入R后如果连接端口说明已禁用
    如下是已禁用的
    
    openssl s_client -connect 10.67.164.199:31943
        …
        Verify return code: 18 (self signed certificate)
    ---
    R
    RENEGOTIATING
    139994761959080:error:1409E0E5:SSL routines:SSL3_WRITE_BYTES:ssl handshake failure:s3_pkt.c:615:
    
##### 如下是未禁用的

    openssl s_client -connect 10.10.12.134:8001
        …
        Verify return code: 18 (self signed certificate)
    ---
    R
    RENEGOTIATING
    depth=0 C = CH, ST = ShenZhen, L = ShenZhen, O = Techstar, OU = Developer, CN = 10.66.49.232
    verify error:num=18:self signed certificate
    verify return:1
    depth=0 C = CH, ST = ShenZhen, L = ShenZhen, O = Techstar, OU = Developer, CN = 10.66.49.232
    verify return:1
    ^C

##### netty禁用重协商
    netty4:
        netty自身开关以4.1.9存在问题，4.1.31已经关闭
        private static final boolean JDK_REJECT_CLIENT_INITIATED_RENEGOTIATION =
            AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
                @Override
                public Boolean run() {
                    return SystemPropertyUtil.getBoolean("jdk.tls.rejectClientInitiatedRenegotiation", false);
                }
            });
        main方法启动时加上
        System.setProperty("jdk.tls.rejectClientInitiatedRenegotiation", "true");
    netty3:
         netty不支持重协商
    

参考：https://blog.csdn.net/hdyrz/article/details/76070411 
