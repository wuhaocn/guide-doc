docker commit -a "wuhaocn@126.com" -m "urcs/arm64java" f0fc8e960c7b  urcs/arm64java:8.0
docker tag urcs/arm64java:8.0 10.10.208.193:5000/urcs/arm64java:8.0
docker push 10.10.208.193:5000/urcs/arm64java:8.0