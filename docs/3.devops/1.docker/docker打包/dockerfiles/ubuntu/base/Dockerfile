# base image
FROM wuhaocn/javaapp:8

# MAINTAINER
MAINTAINER wuhaocn@126.com
VOLUME ["/tmp"]
#设置环境变量
ADD sql/*.sql /docker-entrypoint-initdb.d/
ADD run-server.sh /usr/local/bin/
RUN sed -i "`cat /usr/local/bin/docker-entrypoint.sh|wc -l`i\exec /usr/local/bin/run-server.sh" /usr/local/bin/docker-entrypoint.sh
RUN ["chmod", "+x", "/usr/local/bin/run-server.sh"]
RUN cat /usr/local/bin/docker-entrypoint.sh


