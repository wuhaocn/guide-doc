docker commit -a "wuhaocn@126.com" -m "ubuntu-npm" e8072532ca04  ubuntu-npm:18.04
docker tag ubuntu-npm:18.04 10.10.208.193:5000/ubuntu-npm:18.04
docker push 10.10.208.193:5000/ubuntu-npm:18.04