docker stop japp
docker rm japp
docker run --name japp --privileged=true -p 13306:3306  -p 16379:6379  -d wuhaocn/java-app:8


