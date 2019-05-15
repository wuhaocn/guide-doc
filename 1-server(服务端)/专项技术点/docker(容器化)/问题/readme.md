#### docker http: server gave HTTP response to HTTPS client

    默认没有配置私库走的公有仓库：配置好私库就好
    
    配置124.42.103.113:5000为私库前
    ➜  ~ docker pull 124.42.103.113:5000/auto-test-server:3.0.0-1905151438       
    Error response from daemon: Get https://124.42.103.113:5000/v2/: http: server gave HTTP response to HTTPS client
    配置124.42.103.113:5000为私库后
    ➜  ~ docker pull 124.42.103.113:5000/auto-test-server:3.0.0-1905151438
    3.0.0-1905151438: Pulling from auto-test-server
    Digest: sha256:6e4250f42a44d2c89f4b01cd5e0df59a2da5c336b1b891e2705217682ffee544
    Status: Downloaded newer image for 124.42.103.113:5000/auto-test-server:3.0.0-1905151438