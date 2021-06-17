make PREFIX=/usr/local/redis install

cp -rf /usr/local/redis-conf/redis.conf /usr/local/redis-conf/redis_${port}.conf

cp -rf /usr/local/redis-conf/sentinel.conf /usr/local/redis-conf/sentinel_${port}.conf