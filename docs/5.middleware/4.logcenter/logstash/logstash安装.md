#### 1.下载

    wget https://artifacts.elastic.co/downloads/logstash/logstash-6.6.2.zip
    或者下下来copy上去

#### 2.zip 解压

    unzip logstash-6.6.2.zip
    https://www.cntofu.com/book/52/index.html

#### 3.修改配置

     input {
     # beats {
     #   port => 5044
     # }
     file {
            path => [
            "/home/dev/innerapp/cmp/nohup.out",
            "/home/dev/innerapp/dtc/nohup.out"
            ]
     }

    }
    filter {
      grok {
        match => { "message" => ["%{LOGLEVEL:logLevel}\]\[%{NUMBER:nanoTime:integer}\](?<V1>.*)\[jobId=(?<jobId>[\w\d\-_]+)\](?<V2>.*)", "%{LOGLEVEL:logLevel}\]\[%{NUMBER:nanoTime:integer}\](?<V1>.)(?<V2>.*)"] }
        add_field => { "nest_msg" => "[%{logLevel}]%{V1}%{V2}" }
        remove_field => [ "message", "V1", "V2" ]
      }

      if ![jobId] {
      drop {}
    }

    output {
      stdout {
        codec => json
      }
      elasticsearch {
        hosts => ["10.10.208.194:9200"]
        codec => json
        index => "teatalk-logstash-%{+YYYY.MM.dd}"
      }

    }

###### 参考

    github: https://github.com/elastic/logstash
    https://www.cnblogs.com/cjsblog/p/9459781.html
