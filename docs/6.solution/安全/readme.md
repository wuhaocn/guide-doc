# 安全

## web 安全

### XSS

- [《xss 攻击原理与解决方法》](https://blog.csdn.net/qq_21956483/article/details/54377947)

### CSRF

- [《CSRF 原理及防范》](https://coderxing.gitbooks.io/architecture-evolution/di-san-pian-ff1a-bu-luo/641-web-an-quan-fang-fan/6412-csrf.html)

### SQL 注入

- [《SQL 注入》](https://coderxing.gitbooks.io/architecture-evolution/di-san-pian-ff1a-bu-luo/641-web-an-quan-fang-fan/6413-sql-zhu-ru.html)

### Hash Dos

- [《邪恶的 JAVA HASH DOS 攻击》](http://www.freebuf.com/articles/web/14199.html) \* 利用 JsonObject 上传大 Json，JsonObject 底层使用 HashMap；不同的数据产生相同的 hash 值，使得构建 Hash 速度变慢，耗尽 CPU。
- [《一种高级的 DoS 攻击-Hash 碰撞攻击》](http://blog.it2048.cn/article_hash-collision.html)
- [《关于 Hash Collision DoS 漏洞：解析与解决方案》](http://www.iteye.com/news/23939/)

### 脚本注入

- [《上传文件漏洞原理及防范》](https://coderxing.gitbooks.io/architecture-evolution/di-san-pian-ff1a-bu-luo/641-web-an-quan-fang-fan/6414-shang-chuan-wen-jian-guo-lv.html)

### 漏洞扫描工具

- [《DVWA》](https://coderxing.gitbooks.io/architecture-evolution/di-san-pian-ff1a-bu-luo/6421-dvwa.html)
- [W3af](https://coderxing.gitbooks.io/architecture-evolution/di-san-pian-ff1a-bu-luo/w3af.html)
- [OpenVAS 详解](https://blog.csdn.net/xygg0801/article/details/53610640)

### 验证码

- [《验证码原理分析及实现》](https://blog.csdn.net/niaonao/article/details/51112686)

- [《详解滑动验证码的实现原理》](https://my.oschina.net/jiangbianwanghai/blog/1031031) \* 滑动验证码是根据人在滑动滑块的响应时间，拖拽速度，时间，位置，轨迹，重试次数等来评估风险。

- [《淘宝滑动验证码研究》](https://www.cnblogs.com/xcj26/p/5242758.html)

## DDoS 防范

- [《学习手册：DDoS 的攻击方式及防御手段》](http://netsecurity.51cto.com/art/201601/503799.htm)
- [《免费 DDoS 攻击测试工具大合集》](http://netsecurity.51cto.com/art/201406/442756.htm)

## 用户隐私信息保护

1. 用户密码非明文保存，加动态 salt。
2. 身份证号，手机号如果要显示，用 “\*” 替代部分字符。
3. 联系方式在的显示与否由用户自己控制。
4. TODO

- [《个人隐私包括哪些》](https://zhidao.baidu.com/question/1988017976673661587.html)
- [《在互联网上，隐私的范围包括哪些？》](https://www.zhihu.com/question/20137108)

- [《用户密码保存》](https://coderxing.gitbooks.io/architecture-evolution/di-san-pian-ff1a-bu-luo/642-shu-ju-jia-mi/6425-jia-mi-chang-jing-ff1a-yong-hu-mi-ma-bao-cun.html)

## 序列化漏洞

- [《Lib 之过？Java 反序列化漏洞通用利用分析》](https://blog.chaitin.cn/2015-11-11_java_unserialize_rce/)

## 加密解密

### 对称加密

- [《常见对称加密算法》](https://coderxing.gitbooks.io/architecture-evolution/di-san-pian-ff1a-bu-luo/642-shu-ju-jia-mi/6421-chang-jian-dui-cheng-jia-mi-suan-fa.html)
  _ DES、3DES、Blowfish、AES
  _ DES 采用 56 位秘钥，Blowfish 采用 1 到 448 位变长秘钥，AES 128，192 和 256 位长度的秘钥。 \* DES 秘钥太短（只有 56 位）算法目前已经被 AES 取代，并且 AES 有硬件加速，性能很好。

### 哈希算法

- [《常用的哈希算法》](https://coderxing.gitbooks.io/architecture-evolution/di-san-pian-ff1a-bu-luo/642-shu-ju-jia-mi/6422-chang-jian-ha-xi-suan-fa-and-hmac.html)
  _ MD5 和 SHA-1 已经不再安全，已被弃用。
  _ 目前 SHA-256 是比较安全的。
- [《基于 Hash 摘要签名的公网 URL 签名验证设计方案》](https://blog.csdn.net/zhangruhong168/article/details/78033202)

### 非对称加密

- [《常见非对称加密算法》](https://coderxing.gitbooks.io/architecture-evolution/di-san-pian-ff1a-bu-luo/642-shu-ju-jia-mi/6424-chang-yong-fei-dui-cheng-jia-mi-suan-fa.html)
  _ RSA、DSA、ECDSA(螺旋曲线加密算法)
  _ 和 RSA 不同的是 DSA 仅能用于数字签名，不能进行数据加密解密，其安全性和 RSA 相当，但其性能要比 RSA 快。 \* 256 位的 ECC 秘钥的安全性等同于 3072 位的 RSA 秘钥。

      		[《区块链的加密技术》](http://baijiahao.baidu.com/s?id=1578348858092033763&wfr=spider&for=pc)

## 服务器安全

- [《Linux 强化论：15 步打造一个安全的 Linux 服务器》](http://www.freebuf.com/articles/system/121540.html)

## 数据安全

### 数据备份

TODO

## 网络隔离

### 内外网分离

TODO

### 登录跳板机

在内外环境中通过跳板机登录到线上主机。

- [《搭建简易堡垒机》](http://blog.51cto.com/zero01/2062618)

## 授权、认证

### RBAC

- [《基于组织角色的权限设计》](https://www.cnblogs.com/zq8024/p/5003050.html)
- [《权限系统与 RBAC 模型概述》](https://www.cnblogs.com/shijiaqi1066/p/3793894.html)
- [《Spring 整合 Shiro 做权限控制模块详细案例分析》](https://blog.csdn.net/he90227/article/details/38663553)

### OAuth2.0

- [《理解 OAuth 2.0》](http://www.ruanyifeng.com/blog/2014/05/oauth_2_0.html)
- [《一张图搞定 OAuth2.0》](https://www.cnblogs.com/flashsun/p/7424071.html)

### 双因素认证（2FA）

2FA - Two-factor authentication，用于加强登录验证

常用做法是 登录密码 + 手机验证码（或者令牌 Key，类似于与网银的 USB key）

- 【《双因素认证（2FA）教程》】(http://www.ruanyifeng.com/blog/2017/11/2fa-tutorial.html)

### 单点登录(SSO)

- [《单点登录原理与简单实现》](https://www.cnblogs.com/ywlaker/p/6113927.html)

- [CAS 单点登录框架](https://github.com/apereo/cas)
