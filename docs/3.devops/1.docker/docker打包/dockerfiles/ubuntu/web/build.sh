docker rmi --force `docker images | grep java-web | awk '{print $3}'`
docker rmi --force `docker images | grep none | awk '{print $3}'`

docker build -t wuhaocn/java-web:8 .

docker tag wuhaocn/java-web:8 wuhaocn/java-web:8

docker push wuhaocn/java-web:8


