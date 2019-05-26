#### 1.开发环境代理-devServer.proxy

    - Type: `string | Object`
    
      如果你的前端应用和后端 API 服务器没有运行在同一个主机上，你需要在开发环境下将 API 请求代理到 API 服务器。这个问题可以通过 `vue.config.js` 中的 `devServer.proxy` 选项来配置。
    
      `devServer.proxy` 可以是一个指向开发环境 API 服务器的字符串：
    
      ``` js
      module.exports = {
        devServer: {
          proxy: 'http://localhost:4000'
        }
      }
      ```
    
      这会告诉开发服务器将任何未知请求 (没有匹配到静态文件的请求) 代理到`http://localhost:4000`。
    
      如果你想要更多的代理控制行为，也可以使用一个 `path: options` 成对的对象。完整的选项可以查阅 [http-proxy-middleware](https://github.com/chimurai/http-proxy-middleware#proxycontext-config) 。

##### 1.1.配置示例

``` js
module.exports = {
    devServer: {
      proxy: {
        '/api': {
          target: '<url>',
          ws: true,
          changeOrigin: true
        },
        '/foo': {
          target: '<other_url>'
        }
      }
    }
}
```

#### 2.生产环境代理-NGINX
生产环境走nginx配置

``` js
location / {
  root /home/teb/mp-booth/;
  index index.html;
}

location /coral {
  proxy_pass http://10.10.208.194:8090;
  proxy_buffer_size 200k;
  proxy_buffers 4 200k;
  proxy_http_version 1.1;
  proxy_set_header Connection "keep-alive";
}
```

#### 3.生产环境代理-java
    spring-boot control会进行相关拦截
    @RequestMapping("/api")
    @RequestMapping("/foo")
    