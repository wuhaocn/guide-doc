##### 1.ES 写入注意事项

    日志写入需要加上timestrap，不然kibana无法查看最新消息
    Kibana添加索引时，可以选择时间控制字段(Index contains time-based events  )，如果Time-field name无法自动出现相关时间字段，可以通过以下方式进行映射：

    curl -XPUT http://192.122.231.126:9200/indice -d

    ’{
        "mappings":{
            "trans":{                                            // type名称
                "properties":{
                    "RequestTime":{                         // 时间字段，设置成Time-field
                        "type":"date",
                        "format":"YYYYMMDD HH:mm:ss"
                    }
                }
            }
        }
    }‘

##### 2.
