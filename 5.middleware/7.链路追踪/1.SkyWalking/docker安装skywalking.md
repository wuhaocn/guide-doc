## Docker安装Skywalking APM分布式追踪系统
 

### 1.Skywalking简单介绍

Skywalking是一个应用性能管理(APM)系统，具有服务器性能监测，应用程序间调用关系及性能监测等功能，
Skywalking分为服务端、管理界面、以及嵌入到程序中的探针部分，由程序中的探针采集各类调用数据发送给服务端保存，
在管理界面上可以查看各类性能数据。本文介绍服务端及管理界面的安装。

 

### 2.环境介绍

本文使用虚拟机unbutu18+docker。本unbutu18系统IP地址为：10.10.208.198 大家在使用时记得将此地址换成自己的实际地址。

docker的安装可参考：https://www.cnblogs.com/sunyuliang/p/11422674.html

 

### 3.Skywalking安装

#### 3.1 安装服务端：

这里介绍服务端的两种存储等式，
一种是默认的H2存储，即数据存储在内存中，
一种是使用elasticsearch存储，大家可以任选1.1或1.2其中一种安装方式

##### 3.1.1 默认H2存储
```
sudo docker run --name skywalking -d -p 1234:1234 -p 11800:11800 -p 12800:12800 --restart always apache/skywalking-oap-server 
```
##### 3.1.2 elasticsearch存储

* 1.安装ElasticSearch，因为在安装latest版本时失败了，找不到版本信息(Unable to find image 'elasticsearch:latest' locally)，所以这里指定以ElasticSearch 6.72版为例。 
```
sudo docker run -d --name elasticsearch -p 9200:9200 -p 9300:9300 --restart always -e "discovery.type=single-node" elasticsearch:6.7.2
```
* 2.安装 ElasticSearch管理界面elasticsearch-hq
```
sudo docker run -d --name elastic-hq -p 5000:5000 --restart always elastichq/elasticsearch-hq 
```
* 3.输入以下命令，并等待下载。　　　        
```
sudo docker run --name skywalking -d -p 1234:1234 -p 11800:11800 -p 12800:12800 --restart always --link elasticsearch:elasticsearch -e SW_STORAGE=elasticsearch -e SW_STORAGE_ES_CLUSTER_NODES=elasticsearch:9200 apache/skywalking-oap-server 
```
          

#### 3.2 安装管理界面：

输入以下命令，并等待下载安装。
```
sudo docker run --name skywalking-ui -d -p 8080:8080 --link skywalking:skywalking -e SW_OAP_ADDRESS=10.10.208.198:12800 --restart always apache/skywalking-ui 
```

出现以下界面后就安装完成了。

            
### 3.3访问管理界验证安装结果

在浏览器里面输入[http://10.10.208.198:8080)](http://10.10.208.198:8080),出现了如下界面，到此Skywalking的安装就大功告成了。

 