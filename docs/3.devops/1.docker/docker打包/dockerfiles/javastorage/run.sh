docker stop javast
docker rm  javast
docker run --name javast --privileged=true -e MYSQL_ROOT_PASSWORD=root -d wuhaocn/javastorage:1.0.0



