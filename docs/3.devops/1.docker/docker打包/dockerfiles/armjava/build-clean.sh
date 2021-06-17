docker rm `docker ps -a -q`

docker rmi --force `docker images | grep java | awk '{print $3}'`
docker rmi --force `docker images | grep none | awk '{print $3}'`

docker build -t urcs/arm64java:8 .

# docker tag urcs/arm64java:8 10.10.208.193:5000/urcs/arm64java:8

# docker push 10.10.208.193:5000/urcs/arm64java:8



