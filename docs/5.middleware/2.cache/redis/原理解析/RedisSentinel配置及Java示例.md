### 1.redis 配置文件：

添加如下配置

#### 1.1.redis_7021.conf

```properties
port 7021
bind 10.10.220.149
daemonize yes
protected-mode yes
timeout 0
tcp-keepalive 300
supervised no
dir "/data/redis/redis7021/"
```

#### 1.2.redis_7022.conf

```properties
port 7022
bind 10.10.220.149
daemonize yes
protected-mode yes
timeout 0
tcp-keepalive 300
supervised no
dir "/data/redis/redis7022/"
slaveof 10.10.220.149 7021
```

#### 1.3.redis_7023.conf

```properties
port 7023
bind 10.10.220.149
daemonize yes
protected-mode yes
timeout 0
tcp-keepalive 300
supervised no
dir "/data/redis/redis7023/"
slaveof 10.10.220.149 7021
```

### 2.sentinel 配置文件

#### 2.1.sentinel_17021.conf

```properties
port 17021
bind 10.10.220.149
sentinel monitor mymaster  10.10.220.149 7021 2
sentinel down-after-milliseconds mymaster 5000
sentinel failover-timeout mymaster 15000
sentinel config-epoch mymaster 1
```

#### 2.2.sentinel_17022.conf

```properties
port 17022
bind 10.10.220.149
sentinel monitor mymaster  10.10.220.149 7021 2
sentinel down-after-milliseconds mymaster 5000
sentinel failover-timeout mymaster 15000
sentinel config-epoch mymaster 1
```

#### 2.3.sentinel_17023.conf

```properties
port 17023
bind 10.10.220.149
sentinel monitor mymaster  10.10.220.149 7021 2
sentinel down-after-milliseconds mymaster 5000
sentinel failover-timeout mymaster 15000
sentinel config-epoch mymaster 1
```

### 3.java 测试

#### 3.1 测试类

```java
package org.helium.redis.test;
import org.junit.Test;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;
import sun.tools.tree.SynchronizedStatement;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class RedisSentinelTest {
   static RedisSentinelTest test = new RedisSentinelTest();
   public static void main(String[] args) throws Exception {
      test1();
   }
   public static void test1() throws IOException {
      File file = new File("/Users/wuhao/redis/redis-log");
      if (file.exists()){
         file.delete();
      }
      file.createNewFile();
      FileOutputStream fileOutputStream = new FileOutputStream(file);
      JedisPoolConfig poolConfig = new JedisPoolConfig();
      String masterName = "mymaster";
      Set<String> sentinels = new HashSet<String>();
      sentinels.add("10.10.220.149:17021");
      sentinels.add("10.10.220.149:17022");
      sentinels.add("10.10.220.149:17023");
      JedisSentinelPool jedisSentinelPool = new JedisSentinelPool(masterName, sentinels, poolConfig);

      for (int i = 0; i < 20; i++) {

         Thread run = new Thread(new Runnable() {
            @Override
            public void run() {
               while (true){
                  Jedis redisJ = null;
                  try {
                     Thread.sleep(1000);
                  } catch (InterruptedException e) {
                     e.printStackTrace();
                  }
                  try {
                     redisJ = jedisSentinelPool.getResource();
                     redisJ.set("a", "333");
                     String value = redisJ.get("a");
                     HostAndPort currentHostMaster = jedisSentinelPool.getCurrentHostMaster();
                     String host = new Date() + "OK-"+ currentHostMaster.toString() + "\n";
                     fileOutputStream.write(host.getBytes());
                  } catch (Exception e){
                     e.printStackTrace();
                     try {
                        fileOutputStream.write( ("ERROR-" + e.getMessage()).getBytes());
                        fileOutputStream.write("\n".getBytes());
                     } catch (IOException e1) {
                        e1.printStackTrace();
                     }
                  } finally {
                     if (redisJ != null){
                        redisJ.close();
                     }
                  }

               }
            }
         });
         run.start();;
      }
      try {
         Thread.sleep(1000000);
      } catch (InterruptedException e) {
         e.printStackTrace();
      }

   }

}
```

#### 3.2 结果输出

```properties
sentinel down-after-milliseconds mymaster 5000
业务恢复正常为7秒左右
Fri May 04 11:38:10 CST 2018OK-10.10.220.149:7021
Fri May 04 11:38:10 CST 2018OK-10.10.220.149:7021
ERROR-It seems like server has closed the connection.
ERROR-It seems like server has closed the connection.
ERROR-It seems like server has closed the connection.
ERROR-It seems like server has closed the connection.
ERROR-It seems like server has closed the connection.
ERROR-Could not get a resource from the pool
ERROR-Could not get a resource from the pool
Fri May 04 11:38:17 CST 2018OK-10.10.220.149:7022
Fri May 04 11:38:17 CST 2018OK-10.10.220.149:7022
Fri May 04 11:38:17 CST 2018OK-10.10.220.149:7022
Fri May 04 11:38:17 CST 2018OK-10.10.220.149:7022
Fri May 04 11:38:17 CST 2018OK-10.10.220.149:7022


sentinel down-after-milliseconds mymaster 1000
业务恢复正常为3秒左右
Fri May 04 12:05:38 CST 2018OK-10.10.220.149:7021
Fri May 04 12:05:38 CST 2018OK-10.10.220.149:7021
Fri May 04 12:05:39 CST 2018ERROR-It seems like server has closed the connection.
Fri May 04 12:05:39 CST 2018ERROR-It seems like server has closed the connection.
Fri May 04 12:05:39 CST 2018ERROR-It seems like server has closed the connection.
Fri May 04 12:05:39 CST 2018ERROR-It seems like server has closed the connection.
Fri May 04 12:05:39 CST 2018ERROR-It seems like server has closed the connection.
Fri May 04 12:05:39 CST 2018ERROR-Could not get a resource from the pool
Fri May 04 12:05:39 CST 2018ERROR-It seems like server has closed the connection.
Fri May 04 12:05:39 CST 2018ERROR-It seems like server has closed the connection.
Fri May 04 12:05:39 CST 2018ERROR-It seems like server has closed the connection.
Fri May 04 12:05:39 CST 2018ERROR-Could not get a resource from the pool
Fri May 04 12:05:39 CST 2018ERROR-Could not get a resource from the pool
Fri May 04 12:05:39 CST 2018ERROR-Could not get a resource from the pool
Fri May 04 12:05:39 CST 2018ERROR-Could not get a resource from the pool
Fri May 04 12:05:40 CST 2018ERROR-Could not get a resource from the pool
Fri May 04 12:05:41 CST 2018OK-10.10.220.149:7022
Fri May 04 12:05:41 CST 2018OK-10.10.220.149:7022
Fri May 04 12:05:41 CST 2018OK-10.10.220.149:7022
```
