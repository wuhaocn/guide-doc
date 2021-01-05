#!/usr/bin/env bash
docker stop doc-site
docker rm doc-site
docker run -it  --privileged=true -p 8080:8080 --name doc-site doc-site:20200617224720
#docker run -it  --privileged=true -p 8093:8093 --name doc-site 10.10.208.193:5000/doc-site:20200617224720
docker logs -f doc-site