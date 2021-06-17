docker rm `docker ps -a -q`

docker rmi --force `docker images | grep logstash | awk '{print $3}'`
docker rmi --force `docker images | grep none | awk '{print $3}'`

docker build -t urcs/logstash:6.0.0 .

docker tag urcs/logstash:6.0.0 10.10.208.193:5000/urcs/logstash:6.0.0

docker push 10.10.208.193:5000/urcs/logstash:6.0.0



