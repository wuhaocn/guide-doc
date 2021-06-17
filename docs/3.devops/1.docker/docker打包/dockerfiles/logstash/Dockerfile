FROM docker.elastic.co/logstash/logstash:6.0.0


RUN apt-get update

RUN apt-get -y install vim

RUN apt-get -y install net-tools

RUN apt-get -y install tcpdump

#自定义输入、输出流
RUN rm -f /usr/share/logstash/pipeline/logstash.conf
ADD config/pipeline/ /usr/share/logstash/pipeline/


MAINTAINER wuhaotx<wuhaotx@feinno.com>


