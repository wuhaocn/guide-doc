for port in `seq 7000 7005`; do \
  docker stop redis-${port}
  docker rm redis-${port}
  docker run -d -it --memory=1G \
  -v ~/rcluster/${port}/conf/redis.conf:/usr/local/etc/redis/redis.conf \
  -v ~/rcluster/${port}/data:/data \
  --restart always --name redis-${port} --net host \
  redis:5.0 redis-server /usr/local/etc/redis/redis.conf; \
done
