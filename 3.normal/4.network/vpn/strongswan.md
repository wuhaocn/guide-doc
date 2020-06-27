## 官网

https://www.strongswan.org/


## 安装配置
https://www.920430.com/archives/2743378950.html

## docker  

docker stop strongswan 
docker rm strongswan
docker run --name strongswan -d -p 500:500/udp -p 4500:4500/udp -p 1701:1701/udp --privileged philplckthun/strongswan
docker logs -f strongswan

