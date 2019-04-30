Vue-Resource 使用
96  圆梦人生 
 0.1 2018.04.28 10:47 字数 140 阅读 3173评论 1喜欢 4
Vue-Resource主要用于发送ajax请求，官网已经不在维护（推荐axios），下面写个请求使用案例作为参考，具体参考API：https://github.com/pagekit/vue-resource

案例效果(请求网易音乐列表展示)：

image.png
安装vue-resource
cnpm i vue-resource -S
或者
npm i vue-resource -S
在src下建立resource目录，创建index.js
// 导入vue
import Vue from 'vue'
// 导入vue-resource
import VueResource from 'vue-resource'
// 注册resource
Vue.use(VueResource)
需要配置代理，因为存在跨域
1.需要在cofong/index.js里配置proxyTable代理设置(推荐)：
//代理实现（跨域转发），本地会启动代理器帮助转发
    proxyTable: {
      '/wymusic' :{
          target: 'http://music.163.com/api/playlist/detail',
          changeOrigin:true,
          pathRewrite: {  //url重写
            '^/wymusic':''
          }
      }
    }

2.使用三方代理器，如百度（不推荐）：
https://bird.ioliu.cn/v1/?url=请求的三方地址
3.案例(src/page/resource/index.vue)：
<template>
  <div>
    <button @click="doGet">发送请求</button> <br/>
     <ul>
         <li v-for="(result, index) in list" :key="index">
            <div class="ui-flex">
                <div>
                    <img :src="result.album.blurPicUrl" width="100px" height="100px;"/>
                </div>
                <div class="ui-flex-1">
                    歌曲：{{result.name}} <br>
                    歌手：{{result.artists[0].name}} <br>
                    所属公司：{{result.album.company}}
                </div>
            </div>
         </li>
     </ul>
  </div>
</template>
<script>
// 导入vue-resource
import VueResource from '@/resource'
// 定义三方代理器（不推荐）
// const proxyUrl = 'https://bird.ioliu.cn/v1/?url='

export default {
  data(){
    return {
      list: []
    }
  },
  methods: {
    doGet(){
      // 1.使用三方代理器发送请求
       /*var url = proxyUrl  + 'http://music.163.com/api/playlist/detail?id=19723756';
       this.$http.get(url ).then((response)=>{
          this.list = response.data.result.tracks;
        }, (error)=>{
          console.log('发送失败');
          console.log(error);
        });*/
      //2. 使用本地代理转发
      var url = '/wymusic?id=19723756';
      this.$http.get(url).then((response)=>{
          this.list = response.data.result.tracks;
      },(error)=>{
         console.log('请求失败');
         console.log(error);
      });
    }
  }
}
</script>
<style scoped>
  .ui-flex {
    display: -webkit-box;
    display: box;
    margin-bottom: 5px;
  }
  .ui-flex-1 {
    -webkit-box-flex: 1;
    text-align: left;
    padding-left: 30px;
  }
</style>
