# base image
FROM centos

# MAINTAINER
MAINTAINER wuhaocn@126.com
VOLUME ["/tmp"]
#设置环境变量
ADD redis-3.2.8/ /usr/local/redis-install/
ADD entrypoint.sh /usr/local/redis-install/
ADD conf/ /usr/local/redis-conf/
ENTRYPOINT [ "/usr/local/redis-install/entrypoint.sh" ]
