for port in `seq 7000 7005`; do \
  mkdir -p ./${port}/conf \
  && PORT=${port} envsubst < ./redis_cluster.tmpl > ./${port}/conf/redis.conf \
  && mkdir -p ./${port}/data; \
done