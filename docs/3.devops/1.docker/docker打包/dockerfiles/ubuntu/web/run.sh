docker stop japp
docker rm japp
docker run --name japp --privileged=true -p 3337:3306  -p 16379:6379 -p 16380:6380 -p 12181:2181  -d wuhaocn/java-web:8


