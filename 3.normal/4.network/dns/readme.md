### 1.DNS概念
dns为域名解析服务器，协议传输一般为UDP，在报文较大时可采用TCP传输



### 2.DNS解析软件

#### 2.1 dnsmasq
轻量级DNS配置软件，支持DNS及DHCP配置
详细可参考[dnsmasq安装](dnsmasq.md)


### 3.测试

dig命令使用

* dig 命令主要用来从 DNS 域名服务器查询主机地址信息。

* dig @8.8.8.8 www.126.com


```
dig www.126.com

; <<>> DiG 9.10.6 <<>> www.126.com
;; global options: +cmd
;; Got answer:
;; ->>HEADER<<- opcode: QUERY, status: NOERROR, id: 19710
;; flags: qr rd ra; QUERY: 1, ANSWER: 1, AUTHORITY: 13, ADDITIONAL: 27

;; OPT PSEUDOSECTION:
; EDNS: version: 0, flags:; udp: 4096
;; QUESTION SECTION:
;www.126.com.			IN	A

;; ANSWER SECTION:
www.126.com.		167	IN	A	220.181.12.218
...

```


```
dig @8.8.8.8  www.126.com

; <<>> DiG 9.10.6 <<>> @8.8.8.8 www.126.com
; (1 server found)
;; global options: +cmd
;; Got answer:
;; ->>HEADER<<- opcode: QUERY, status: NOERROR, id: 58122
;; flags: qr rd ra; QUERY: 1, ANSWER: 1, AUTHORITY: 0, ADDITIONAL: 1

;; OPT PSEUDOSECTION:
; EDNS: version: 0, flags:; udp: 4096
;; QUESTION SECTION:
;www.126.com.			IN	A

;; ANSWER SECTION:
www.126.com.		108	IN	A	220.181.12.218

;; Query time: 44 msec
;; SERVER: 8.8.8.8#53(8.8.8.8)
;; WHEN: Wed Sep 16 10:30:20 CST 2020
;; MSG SIZE  rcvd: 56
```





#### 3.报文附录

* 请求报文
```
Domain Name System (query)
    Transaction ID: 0xaae9
    Flags: 0x0100 Standard query
        0... .... .... .... = Response: Message is a query
        .000 0... .... .... = Opcode: Standard query (0)
        .... ..0. .... .... = Truncated: Message is not truncated
        .... ...1 .... .... = Recursion desired: Do query recursively
        .... .... .0.. .... = Z: reserved (0)
        .... .... ...0 .... = Non-authenticated data: Unacceptable
    Questions: 1
    Answer RRs: 0
    Authority RRs: 0
    Additional RRs: 0
    Queries
        eclick.baidu.com: type A, class IN
            Name: eclick.baidu.com
            [Name Length: 16]
            [Label Count: 3]
            Type: A (Host Address) (1)
            Class: IN (0x0001)
    [Response In: 32]

```
* 应答报文
```
Domain Name System (response)
    Transaction ID: 0x2be8
    Flags: 0x8180 Standard query response, No error
        1... .... .... .... = Response: Message is a response
        .000 0... .... .... = Opcode: Standard query (0)
        .... .0.. .... .... = Authoritative: Server is not an authority for domain
        .... ..0. .... .... = Truncated: Message is not truncated
        .... ...1 .... .... = Recursion desired: Do query recursively
        .... .... 1... .... = Recursion available: Server can do recursive queries
        .... .... .0.. .... = Z: reserved (0)
        .... .... ..0. .... = Answer authenticated: Answer/authority portion was not authenticated by the server
        .... .... ...0 .... = Non-authenticated data: Unacceptable
        .... .... .... 0000 = Reply code: No error (0)
    Questions: 1
    Answer RRs: 1
    Authority RRs: 1
    Additional RRs: 0
    Queries
        eclick.baidu.com: type AAAA, class IN
            Name: eclick.baidu.com
            [Name Length: 16]
            [Label Count: 3]
            Type: AAAA (IPv6 Address) (28)
            Class: IN (0x0001)
    Answers
        eclick.baidu.com: type CNAME, class IN, cname eclick.e.shifen.com
            Name: eclick.baidu.com
            Type: CNAME (Canonical NAME for an alias) (5)
            Class: IN (0x0001)
            Time to live: 5210 (1 hour, 26 minutes, 50 seconds)
            Data length: 18
            CNAME: eclick.e.shifen.com
    Authoritative nameservers
        e.shifen.com: type SOA, class IN, mname ns1.e.shifen.com
            Name: e.shifen.com
            Type: SOA (Start Of a zone of Authority) (6)
            Class: IN (0x0001)
            Time to live: 77 (1 minute, 17 seconds)
            Data length: 45
            Primary name server: ns1.e.shifen.com
            Responsible authority's mailbox: baidu_dns_master.baidu.com
            Serial Number: 2009160003
            Refresh Interval: 5 (5 seconds)
            Retry Interval: 5 (5 seconds)
            Expire limit: 2592000 (30 days)
            Minimum TTL: 3600 (1 hour)
    [Request In: 24]
    [Time: 0.032070000 seconds]

```
