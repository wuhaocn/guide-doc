# docker run ...运行出容器

# 安装东西 sctp 。。。
# faa474c052c6 为安装好镜像
docker commit faa474c052c6 urcs/java-sctp:8

docker tag urcs/java-sctp:8 10.10.208.193:5000/urcs/java-sctp:8

docker push 10.10.208.193:5000/urcs/java-sctp:8



