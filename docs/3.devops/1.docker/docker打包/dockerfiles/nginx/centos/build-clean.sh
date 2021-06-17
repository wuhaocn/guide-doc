#!/usr/bin/env bash
version=`date +%Y%m%d%H%M%S`;
echo "build openresty-nginx-teatalk version $version"

docker rm `docker ps -a -q`

docker rmi --force `docker images | openresty-nginx-teatalk | awk '{print $3}'`
docker rmi --force `docker images | grep none | awk '{print $3}'`

docker build -t openresty-nginx-teatalk:$version ./



docker tag openresty-nginx-teatalk:$version 10.10.208.193:5000/openresty-nginx-teatalk:$version
#
docker push 10.10.208.193:5000/openresty-nginx-teatalk:$version


echo "build openresty-nginx-teatalk sucess 10.10.208.193:5000/openresty-nginx-teatalk:$version"
