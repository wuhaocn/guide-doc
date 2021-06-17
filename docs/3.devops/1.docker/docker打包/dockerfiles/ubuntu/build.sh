docker rmi --force `docker images | grep ubuntu | awk '{print $3}'`

docker build -t ubuntu-5gc:18.0401 .

docker tag ubuntu-5gc:18.0401 10.10.208.193:5000/ubuntu-5gc:18.0401

docker push 10.10.208.193:5000/ubuntu-5gc:18.0401



