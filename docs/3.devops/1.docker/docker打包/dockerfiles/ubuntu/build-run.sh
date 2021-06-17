docker commit faa474c052c6 wuhaocn/javaapp:18.04

docker tag wuhaocn/javaapp:18.04 wuhaocn/javaapp:18.04

docker push wuhaocn/javaapp:18.04

docker stop ubuntu-test
docker rm ubuntu-test
docker run --name ubuntu-test --privileged=true -p 13306:3306 -p 16379:3306  -d wuhaocn/javaapp:18.04



