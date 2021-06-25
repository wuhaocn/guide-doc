docker stop japp
docker rm japp
docker run --name japp --privileged=true -p 13306:3306  -p 16379:6379  -it wuhaocn/java-im:8


