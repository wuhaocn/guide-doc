FROM openresty/openresty:1.13.6.2-centos-rpm

MAINTAINER wuhaotx<wuhaotx@feinno.com>

RUN yum -y install net-tools

RUN yum -y install vim

RUN rm -rf /etc/nginx/conf.d/default.conf

COPY conf/nginx.conf  /usr/local/openresty/nginx/conf/nginx.conf

ADD conf /etc/nginx/conf.d/
