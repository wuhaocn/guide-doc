docker rm `docker ps -a -q`

docker rmi --force `docker images | grep kafka | awk '{print $3}'`
docker rmi --force `docker images | grep none | awk '{print $3}'`

docker build -t urcs/kafka:2.11-1.1.1 .

docker tag urcs/kafka:2.11-1.1.1 10.10.208.193:5000/urcs/kafka:2.11-1.1.1

docker push 10.10.208.193:5000/urcs/kafka:2.11-1.1.1



