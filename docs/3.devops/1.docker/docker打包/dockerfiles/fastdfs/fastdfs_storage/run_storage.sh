#!/usr/bin/env bash
docker run -d --privileged=true -p 23000:23000 -p 8888:8888 --name storage --net=host --env TRACKER_SERVER=10.21.5.4:22122 urcs/fastdfs_storage:4.08 storage