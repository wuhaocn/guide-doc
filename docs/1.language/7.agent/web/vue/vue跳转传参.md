#### vue 页面跳转

#### 1.通过 router-link 进行跳转

```js
<router-link
    :to="{
        path: 'yourPath',
        params: {
            key: 'value', // orderNum : this.searchData.orderNo
        },
        query: {
           key: 'value', // orderNum : this.searchData.orderNo
        }
    }">
    <button type="button">跳转</button>
</router-link>

备注：
     1. path -> 是要跳转的路由路径,也可以是路由文件里面配置的 name 值,两者都可以进行路由导航
     2. params -> 是要传送的参数,参数可以直接key:value形式传递
     3. query -> 是通过 url 来传递参数的同样是key:value形式传递
```

#### 2. $router 方式跳转

##### 2.1router 传参方式

    this.$router.push({name:'路由命名',params:{参数名:参数值,参数名:参数值}})
    this.$router.push({
            path: 'yourPath',
            name: '要跳转的路径的 name,在 router 文件夹下的 index.js 文件内找',
            params: {
                key: 'key',
                msgKey: this.msg
            }
            /*query: {
                key: 'key',
                msgKey: this.msg
            }*/
        })

    接受方式
    this.$route.params.参数名
    this.$route.query.参数名

##### 2.2 传递页:

```js
 <div class="app-item-img-box" v-on:click="viewApp(appInfo)">
                      <img class="app-item-img" :src="appInfo.logoUrl"/>
 </div>
 <div class="app-item-name">{{ appInfo.name }}</div>

 viewApp(appInfo) {
           console.log("view App", appInfo)
           this.$router.push({path: '/app', query: {'appName': appInfo.name, 'appUrl': appInfo.applicationUrl}})
 }
```

##### 2.3 接收页：

```js
<div class="app_view_header">
  <div class="app_view_nav">
   <div class="app_view_close">
      <b><i class="el-icon-close" v-on:click="appClose()"></i></b>
    </div>
    <div class="app_view_title">
      {{ this.$route.query.appName }}
    </div>
    <div class="app_view_other">
      <i class="el-icon-more" v-on:click="appClose()"></i>
    </div>

  </div>
```

参考
：https://www.jianshu.com/p/c699c4d197de
