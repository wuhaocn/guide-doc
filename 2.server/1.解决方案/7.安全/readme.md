
# 安全

## web 安全

### XSS
* [《xss攻击原理与解决方法》](https://blog.csdn.net/qq_21956483/article/details/54377947)
### CSRF
* [《CSRF原理及防范》](https://coderxing.gitbooks.io/architecture-evolution/di-san-pian-ff1a-bu-luo/641-web-an-quan-fang-fan/6412-csrf.html)

### SQL 注入

* [《SQL注入》](https://coderxing.gitbooks.io/architecture-evolution/di-san-pian-ff1a-bu-luo/641-web-an-quan-fang-fan/6413-sql-zhu-ru.html)

### Hash Dos


* [《邪恶的JAVA HASH DOS攻击》](http://www.freebuf.com/articles/web/14199.html)
	* 利用JsonObject 上传大Json，JsonObject 底层使用HashMap；不同的数据产生相同的hash值，使得构建Hash速度变慢，耗尽CPU。
* [《一种高级的DoS攻击-Hash碰撞攻击》](http://blog.it2048.cn/article_hash-collision.html )
* [《关于Hash Collision DoS漏洞：解析与解决方案》](http://www.iteye.com/news/23939/)

### 脚本注入

* [《上传文件漏洞原理及防范》](https://coderxing.gitbooks.io/architecture-evolution/di-san-pian-ff1a-bu-luo/641-web-an-quan-fang-fan/6414-shang-chuan-wen-jian-guo-lv.html)

### 漏洞扫描工具
* [《DVWA》](https://coderxing.gitbooks.io/architecture-evolution/di-san-pian-ff1a-bu-luo/6421-dvwa.html)
* [W3af](https://coderxing.gitbooks.io/architecture-evolution/di-san-pian-ff1a-bu-luo/w3af.html)
* [OpenVAS详解](https://blog.csdn.net/xygg0801/article/details/53610640)

### 验证码

* [《验证码原理分析及实现》](https://blog.csdn.net/niaonao/article/details/51112686)

* [《详解滑动验证码的实现原理》](https://my.oschina.net/jiangbianwanghai/blog/1031031)
	* 滑动验证码是根据人在滑动滑块的响应时间，拖拽速度，时间，位置，轨迹，重试次数等来评估风险。

* [《淘宝滑动验证码研究》](https://www.cnblogs.com/xcj26/p/5242758.html)

## DDoS 防范
* [《学习手册：DDoS的攻击方式及防御手段》](http://netsecurity.51cto.com/art/201601/503799.htm)
* [《免费DDoS攻击测试工具大合集》](http://netsecurity.51cto.com/art/201406/442756.htm)

## 用户隐私信息保护

1. 用户密码非明文保存，加动态salt。
2. 身份证号，手机号如果要显示，用 “\*” 替代部分字符。
3. 联系方式在的显示与否由用户自己控制。
4. TODO

* [《个人隐私包括哪些》](https://zhidao.baidu.com/question/1988017976673661587.html)
* [《在互联网上，隐私的范围包括哪些？》](https://www.zhihu.com/question/20137108)

* [《用户密码保存》](https://coderxing.gitbooks.io/architecture-evolution/di-san-pian-ff1a-bu-luo/642-shu-ju-jia-mi/6425-jia-mi-chang-jing-ff1a-yong-hu-mi-ma-bao-cun.html)

## 序列化漏洞
* [《Lib之过？Java反序列化漏洞通用利用分析》](https://blog.chaitin.cn/2015-11-11_java_unserialize_rce/)

## 加密解密

### 对称加密

* [《常见对称加密算法》](https://coderxing.gitbooks.io/architecture-evolution/di-san-pian-ff1a-bu-luo/642-shu-ju-jia-mi/6421-chang-jian-dui-cheng-jia-mi-suan-fa.html)
	* DES、3DES、Blowfish、AES
	* DES 采用 56位秘钥，Blowfish 采用1到448位变长秘钥，AES 128，192和256位长度的秘钥。
	* DES 秘钥太短（只有56位）算法目前已经被 AES 取代，并且 AES 有硬件加速，性能很好。
	
### 哈希算法
* [《常用的哈希算法》](https://coderxing.gitbooks.io/architecture-evolution/di-san-pian-ff1a-bu-luo/642-shu-ju-jia-mi/6422-chang-jian-ha-xi-suan-fa-and-hmac.html)
	* MD5 和 SHA-1 已经不再安全，已被弃用。
	* 目前 SHA-256 是比较安全的。
	
* [《基于Hash摘要签名的公网URL签名验证设计方案》](https://blog.csdn.net/zhangruhong168/article/details/78033202)

### 非对称加密
* [《常见非对称加密算法》](https://coderxing.gitbooks.io/architecture-evolution/di-san-pian-ff1a-bu-luo/642-shu-ju-jia-mi/6424-chang-yong-fei-dui-cheng-jia-mi-suan-fa.html)
	* RSA、DSA、ECDSA(螺旋曲线加密算法)
	* 和 RSA 不同的是 DSA 仅能用于数字签名，不能进行数据加密解密，其安全性和RSA相当，但其性能要比RSA快。
	* 256位的ECC秘钥的安全性等同于3072位的RSA秘钥。

		[《区块链的加密技术》](http://baijiahao.baidu.com/s?id=1578348858092033763&wfr=spider&for=pc)	


## 服务器安全
* [《Linux强化论：15步打造一个安全的Linux服务器》](http://www.freebuf.com/articles/system/121540.html)

## 数据安全

### 数据备份

TODO

## 网络隔离

### 内外网分离

TODO

### 登录跳板机
在内外环境中通过跳板机登录到线上主机。
* [《搭建简易堡垒机》](http://blog.51cto.com/zero01/2062618)

## 授权、认证
### RBAC 
* [《基于组织角色的权限设计》](https://www.cnblogs.com/zq8024/p/5003050.html)
* [《权限系统与RBAC模型概述》](https://www.cnblogs.com/shijiaqi1066/p/3793894.html)
* [《Spring整合Shiro做权限控制模块详细案例分析》](https://blog.csdn.net/he90227/article/details/38663553)

### OAuth2.0
* [《理解OAuth 2.0》](http://www.ruanyifeng.com/blog/2014/05/oauth_2_0.html)
* [《一张图搞定OAuth2.0》](https://www.cnblogs.com/flashsun/p/7424071.html)

### 双因素认证（2FA）

2FA - Two-factor authentication，用于加强登录验证

常用做法是 登录密码 + 手机验证码（或者令牌Key，类似于与网银的 USB key）

* 【《双因素认证（2FA）教程》】(http://www.ruanyifeng.com/blog/2017/11/2fa-tutorial.html)

### 单点登录(SSO)

* [《单点登录原理与简单实现》](https://www.cnblogs.com/ywlaker/p/6113927.html)

* [CAS单点登录框架](https://github.com/apereo/cas)