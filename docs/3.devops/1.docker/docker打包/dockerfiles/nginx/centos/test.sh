/usr/local/openresty/nginx/sbin/nginx -c /usr/local/openresty/openresty-conf/nginx-fusion.conf
 docker run -d -p 80:80 -p 443:443 -p 8020:8020  -p 8080:8080  --name openresty-urcs 124.42.103.163:5000/urcs/openresty-tcp:1.13.6.2-centos-rpm  bash

 docker run -it  --privileged=true -p 80:80 -p 443:443 -p 8020:8020  -p 8080:8080 -p 5260:5260 -p 7260:7260   --name openresty-urcs urcs/openresty-tcp:1.13.6.2-centos-rpm
 docker run -d --privileged=true -p 80:80 -p 443:443 -p 8020:8020  -p 8080:8080 -p 5260:5260 -p 7260:7260   --name openresty-urcs urcs/openresty-tcp:1.13.6.2-centos-rpm


docker stop openresty-nginx
docker rm openresty-nginx
docker run -d  --privileged=true --net=host  --name openresty-nginx 10.10.208.193:5000/openresty-nginx-teatalk:20190826142106