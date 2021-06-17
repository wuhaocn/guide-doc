docker rmi --force `docker images | grep java-app | awk '{print $3}'`

docker build -t wuhaocn/java-app:8 .

docker tag wuhaocn/java-app:8 wuhaocn/java-app:8

docker push wuhaocn/java-app:8



