#!/usr/bin/env bash
docker run -d --privileged=true -p 22122:22122 --name trakcer --net=host urcs/fastdfs_tracker tracker