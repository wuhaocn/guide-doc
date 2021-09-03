## prometheus+grafana

### 安装

- 默认配置

```
docker stop prometheus
docker rm prometheus
docker run -d --name=prometheus  -p 9090:9090  prom/prometheus
docker update prometheus --restart=always

docker stop grafana
docker rm grafana
docker run -d --name=grafana  -p 3000:3000 grafana/grafana
docker update prometheus --restart=always
```

- 修改配置

```
docker stop prometheus
docker rm prometheus
docker run -d --name=prometheus  -p 9090:9090  -v /home/rcloud/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml prom/prometheus
docker update prometheus --restart=always
```

```
global:
  # 默认情况下，每15s拉取一次目标采样点数据。
  scrape_interval:     15s
  # 我们可以附加一些指定标签到采样点度量标签列表中, 用于和第三方系统进行通信, 包括：federation, remote storage, Alertmanager
  external_labels:
    # 下面就是拉取自身服务采样点数据配置
    monitor: 'codelab-monitor'
scrape_configs:
  # job名称会增加到拉取到的所有采样点上，同时还有一个instance目标服务的host：port标签也会增加到采样点上
  - job_name: 'prometheus'
    # 覆盖global的采样点，拉取时间间隔5s
    scrape_interval: 5s
    static_configs:
      - targets: ['localhost:9090']
```

### 登录配置

- 登录 http://127.0.0.1:3000
- 修改密码 默认 admin admin
- 配置 DataSource 127.0.0.1:9090
- 添加面板

app(metrics)--data--> (?) + prometheus + grafana
