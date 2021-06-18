# base image
FROM wuhaocn/java-app:8

# MAINTAINER
MAINTAINER wuhaocn@126.com
VOLUME ["/tmp"]


#设置环境变量
ADD sql/*.sql /docker-entrypoint-initdb.d/
ADD sh/*.sh /usr/local/bin/

## 此处可添加java app
RUN ["chmod", "+x", "/usr/local/bin/run-java.sh"]
RUN echo "/usr/local/bin/run-java.sh" >> /usr/local/bin/run-server.sh

