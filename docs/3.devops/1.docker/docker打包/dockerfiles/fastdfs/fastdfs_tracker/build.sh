docker rm `docker ps -a -q`

docker rmi --force `docker images | grep fastdfs_tracker | awk '{print $3}'`
docker rmi --force `docker images | grep none | awk '{print $3}'`

docker build -t urcs/fastdfs_tracker:4.08 .

docker tag urcs/fastdfs_tracker:4.08 10.10.208.193:5000/urcs/fastdfs_tracker:4.08

docker push 10.10.208.193:5000/urcs/fastdfs_tracker:4.08
