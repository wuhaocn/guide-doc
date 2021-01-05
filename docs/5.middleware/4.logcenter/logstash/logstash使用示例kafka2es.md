### 下载

    wget https://artifacts.elastic.co/downloads/logstash/logstash-6.0.0.tar.gz

### 解压

    zip -xzf wget https://artifacts.elastic.co/downloads/logstash/logstash-6.0.0.tar.gz

### 配置

    进入logstash-6.0.0文件下bin目录

    新建 kafka2es.conf

    input {
      kafka {
        type => "kafka"
        bootstrap_servers => "10.10.220.120:9092"
        topics => "extas-gz-userapp"
        auto_offset_reset => "earliest"
      }
    }

    #filter must in order
    filter {
        if ([message] !~ "^{") {
          drop {}
        }else {
          json {
           source => "message"
           #target => "jsoncontent"
          }
          mutate {
            remove_field => "message"
            remove_field => "@version"
          }
        }
    }

    output {
        if [type] == "kafka" {
          elasticsearch {
            hosts => ["10.10.220.120:9200"]
            index => "extas-gz-userapp"
            manage_template => true
          }
        }
    }

保存

### 启动

./logstash -f kafka2es.conf
