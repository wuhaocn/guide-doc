docker stop ubuntu18
docker rm ubuntu18
docker stop ubuntu-5gc
docker stop ubuntu-5gc
docker run --name ubuntu-5gc --privileged=true --net=host -d 10.10.208.193:5000/ubuntu-5gc:18.0401 /bin/bash -c "tail -f /dev/null"


docker stop ubuntu-test
docker rm ubuntu-test
docker run --name ubuntu-test --privileged=true   -itd ubuntu:18.04



# docker tag docker tag ubuntu-5gc:18.04 10.10.208.193:5000/ubuntu-5gc:18.04
# docker push 10.10.208.193:5000/ubuntu-5gc:18.04

#!/bin/bash

### BEGIN INIT INFO
# Provides:     runserver
# Required-Start:  $remote_fs $syslog
# Required-Stop:   $remote_fs $syslog
# Default-Start:   2 3 4 5
# Default-Stop:   0 1 6
# Short-Description: start runserver
# Description:    start runserver
### END INIT INFO

redis-server &