FROM bolingcavalry/arm64jdk:8

MAINTAINER wuhaotx<wuhaotx@feinno.com>

RUN mv /etc/apt/sources.list /etc/apt/sources.list.bak && \
    echo "deb http://ports.ubuntu.com/ bionic main multiverse restricted universe" >>/etc/apt/sources.list && \
    echo "deb http://ports.ubuntu.com/ bionic-backports main multiverse restricted universe" >>/etc/apt/sources.list && \
    echo "deb http://ports.ubuntu.com/ bionic-proposed main multiverse restricted universe" >>/etc/apt/sources.list && \
    echo "deb http://ports.ubuntu.com/ bionic-security main multiverse restricted universe" >>/etc/apt/sources.list && \
    echo "deb http://ports.ubuntu.com/ bionic-updates main multiverse restricted universe" >>/etc/apt/sources.list && \
    echo "deb-src http://ports.ubuntu.com/ bionic main multiverse restricted universe" >>/etc/apt/sources.list && \
    echo "deb-src http://ports.ubuntu.com/ bionic-backports main multiverse restricted universe" >>/etc/apt/sources.list  && \
    echo "deb-src http://ports.ubuntu.com/ bionic-proposed main multiverse restricted universe" >>/etc/apt/sources.list && \
    echo "deb-src http://ports.ubuntu.com/ bionic-security main multiverse restricted universe" >>/etc/apt/sources.list  && \
    echo "deb [arch=amd64] https://mirrors.aliyun.com/docker-ce/linux/ubuntu bionic stable" >>/etc/apt/sources.list && \
    echo "deb-src http://ports.ubuntu.com/ bionic-updates main multiverse restricted universe" >>/etc/apt/sources.list


RUN cat /etc/apt/sources.list

#RUN apt-get update --allow-unauthenticated
#
#RUN apt-get -y install vim --allow-unauthenticated
#
#RUN apt-get -y install net-tools --allow-unauthenticated
#
#RUN apt-get -y install tcpdump --allow-unauthenticated
#
#RUN apt-get -y install libsctp1 --allow-unauthenticated
#RUN apt-get -y install lksctp-tools --allow-unauthenticated
