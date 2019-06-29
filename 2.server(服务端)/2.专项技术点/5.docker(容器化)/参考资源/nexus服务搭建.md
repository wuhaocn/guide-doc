### 搭建nexus 
    docker stop nexus
    docker rm nexus
    docker run -d --name nexus -p 5000:5000 -p 8081:8081 -p 8082:8082 -p 8083:8083 -p 8084:8084 sonatype/nexus3
    docker logs -f nexus
    启动成功
    
    bash-4.2$ cd /nexus-data/
    
    bash-4.2$ ls
    admin.password	blobs  cache  db  elasticsearch  etc  generated-bundles  instances  javaprefs  kar  keystores  lock  log  orient  port	restore-from-backup  tmp
    
    bash-4.2$ cat admin.password 
    51a030af-f7ab-43d5-875e-3c2775dbae2c
    
    
    登录进去修改密码~~