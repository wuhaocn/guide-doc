### strongswan介绍
```
基于开源IPsec的VPN解决方案
在Linux 2.6、3.x和4.x内核，Android，FreeBSD，OS X，iOS和Windows上运行
同时实现IKEv1和IKEv2（RFC 7296）密钥交换协议
经过全面测试的对IPv6 IPsec隧道和传输连接的支持
使用IKEv2 MOBIKE（RFC 4555）动态更新IP地址和接口
自动插入和删除基于IPsec策略的防火墙规则
通过UDP封装和端口浮动的NAT遍历（RFC 3947）
支持IKEv2消息分段（RFC 7383），避免IP分段问题
失效对等检测（DPD，RFC 3706）负责悬空隧道
静态虚拟IP和IKEv1 ModeConfig拉和推模式
IKEv1主模式身份验证之上的XAUTH服务器和客户端功能
由IKE守护程序或SQL数据库管理的虚拟IP地址池
安全的IKEv2 EAP用户身份验证（EAP-SIM，EAP-AKA，EAP-TLS，EAP-TTLS，EAP-PEAP，EAP-MSCHAPv2等）
可选通过EAP-RADIUS插件将EAP消息中继到AAA服务器
支持IKEv2多重身份验证交换（RFC 4739）
基于X.509证书或预共享密钥的身份验证
在IKEv2（RFC 7427）中将强大的签名算法与签名身份验证一起使用 
通过HTTP或LDAP检索和吊销证书吊销列表
全面支持在线证书状态协议（OCSP，RFC 2560）。
CA管理（OCSP和CRL URI，默认LDAP服务器）
基于通配符或中间CA的强大IPsec策略
将RSA私钥和证书存储在智能卡上（PKCS＃11接口）或受TPM 2.0保护
加密算法和关系数据库接口的模块化插件
支持NIST椭圆曲线DH组以及ECDSA签名和证书（Suite B，RFC 4869）
支持X25519椭圆曲线DH组（RFC 8031）和Ed25519签名和证书（RFC 8420）
插件和库的可选内置完整性和加密测试
通过StrongSwan NetworkManager小程序实现Linux桌面平滑集成
符合PB-TNC（RFC 5793），PA-TNC（RFC 5792），PT-TLS（RFC 6876），PT-EAP（RFC 7171）和SWIMA for PA-TNC（RFC 8412）的可信网络连接
适用于Android 4及更高版本的strongSwan VPN客户端
可以从Google Play下载免费的strongSwan应用程序。VPN客户端仅通过基于密码的EAP-MD5或EAP-MSCHAPv2或基于证书的用户身份验证和基于证书的VPN网关身份验证支持IKEv2。
 
带单个整体 IKEv1 / IKEv2守护程序的 strongSwan 5.x
strongSwan 5.x分支与Linux内核的本地NETKEY IPsec堆栈一起支持IKEv1和IKEv2密钥交换协议。的  戎 IKE守护进程是基于现代面向对象和多线程的概念，与代码的100％被写入C. strongSwan的IKEv2的功能已被成功地对15的IKEv2测试的第三和第四的IKEv2互操作性研讨会，2007年期间厂商和2008年。通过扩展成功的IKEv2 charon守护程序的源代码，IKEv1功能已在2012年从头开始重新实现。IKEv1互操作性已针对现有的StrongSwan 4.6 pluto守护程序和一些第三方产品进行了测试。

```