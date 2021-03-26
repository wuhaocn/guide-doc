### 搭建 nexus

    rm -rf /home/feinno/nexus
    mkdir /home/feinno/nexus
    docker stop nexus
    docker rm nexus
    docker run -d --name nexus -p 5260:8081 -p 5261:8082 -p 5262:8083 -p 5263:8084  -p 5264:5000 -v /home/feinno/nexus:/var/nexus-data sonatype/nexus3
    docker logs -f nexus
    启动成功

    访问

    http://10.10.208.193:5260/

    查看密码:

    bash-4.2$ cd /nexus-data/

    bash-4.2$ ls
    admin.password	blobs  cache  db  elasticsearch  etc  generated-bundles  instances  javaprefs  kar  keystores  lock  log  orient  port	restore-from-backup  tmp

    bash-4.2$ cat admin.password

    51a030af-f7ab-43d5-875e-3c2775dbae2c


    登录进去修改密码~~

    注意修改密码之后，提示是否开启anonymous模式，这个要勾选，否则public需要密码访问

    admin
    urcs@2018


    helium
    helium.123
