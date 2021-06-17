
docker stop urcs-java
docker rm urcs-java
docker run -it --name urcs-java urcs/arm64java:8.0  bash