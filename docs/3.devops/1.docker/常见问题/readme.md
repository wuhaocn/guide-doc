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

#### Either boot into a newer kernel or disable selinux in docker (--selinux-enabled=false)

    描述：
    7月 03 14:46:34 localhost.localdomain systemd[1]: Starting Docker Application Container Engine...
    7月 03 14:46:34 localhost.localdomain dockerd-current[10463]: time="2019-07-03T14:46:34.807935459+08:00" level=warning msg="could not change group /var/run/docker.sock to docker: group docker not found"
    7月 03 14:46:34 localhost.localdomain dockerd-current[10463]: time="2019-07-03T14:46:34.809394707+08:00" level=info msg="libcontainerd: new containerd process, pid: 10471"
    7月 03 14:46:36 localhost.localdomain dockerd-current[10463]: Error starting daemon: SELinux is not supported with the overlay2 graph driver on this kernel. Either boot into a newer kernel or disable selinux in docker (--selinux-enabled=false)
    7月 03 14:46:36 localhost.localdomain systemd[1]: docker.service: main process exited, code=exited, status=1/FAILURE
    7月 03 14:46:36 localhost.localdomain systemd[1]: Failed to start Docker Application Container Engine.

    解决办法：
    [root@registry lib]# cat /etc/sysconfig/docker
    # /etc/sysconfig/docker

    # Modify these options if you want to change the way the docker daemon runs
    #OPTIONS='--selinux-enabled --log-driver=journald --signature-verification=false'

    OPTIONS='--selinux-enabled=false --log-driver=journald --signature-verification=false --registry-mirror=https://fzhifedh.mirror.aliyuncs.com --insecure-registry=registry.sese.com'    #修改这里的"--selinux-enabled"，改成"--selinux-enabled=false"
    if [ -z "${DOCKER_CERT_PATH}" ]; then
        DOCKER_CERT_PATH=/etc/docker
    fi

    ......   #配置文件后面的内容省略
    [root@registry lib]#

#### Error response from daemon: oci runtime error: container_linux.go:247: starting container process

    https://blog.csdn.net/liqun_super/article/details/88304094
