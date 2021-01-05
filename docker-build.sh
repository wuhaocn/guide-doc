#!/usr/bin/env bash
version=`date +%Y%m%d%H%M%S`;
echo "build doc-site version $version"
rm -rf ./docker/docs
rm -rf ./docker/node_modules
cp -rf ./docs  ./docker/docs
cp -rf ./*.json  ./docker/
cp -rf ./*.md  ./docker/
#docker rm `docker ps -a -q`
docker rmi --force `docker images | grep ubuntu-npm | awk '{print $3}'`
docker rmi --force `docker images | grep none | awk '{print $3}'`
docker build -t doc-site:$version ./docker
#docker tag doc-site:$version 10.10.208.193:5000/doc-site:$version
#
#docker push 10.10.208.193:5000/doc-site:$version
echo "build booth sucess 10.10.208.193:5000/doc-site:$version"