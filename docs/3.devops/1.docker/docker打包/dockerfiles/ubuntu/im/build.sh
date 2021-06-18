docker rmi --force $(docker images | grep java-im | awk '{print $3}')
docker rmi --force $(docker images | grep none | awk '{print $3}')

docker build -t wuhaocn/java-im:8 .

docker tag wuhaocn/java-im:8 wuhaocn/java-im:8

docker push wuhaocn/java-im:8
