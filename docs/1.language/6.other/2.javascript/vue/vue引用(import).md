### 1。引入第三方插件

    import echarts from 'echarts'

### 2.引入工具类

    第一种是引入单个方法

    import {axiosfetch} from './util';

    下面是写法，需要export导出
    export function axiosfetch(options) {

    }

    第二种  导入成组的方法

    import * as tools from './libs/tools'

    其中tools.js中有多个export方法,把tools里所有export的方法导入

    vue中怎么用呢？
    Vue.prototype.$tools = tools
    直接用 this.$tools.method调用就可以了

    说到这 export 和 export default 又有什么区别呢？
    下面看下区别
     先是 export
    import {axiosfetch} from './util';  //需要加花括号  可以一次导入多个也可以一次导入一个，但都要加括号
    如果是两个方法
    import {axiosfetch,post} from './util';
    再是 export default
    import axiosfetch from './util';  //不需要加花括号  只能一个一个导入

### 3.导入 css 文件

    import 'iview/dist/styles/iview.css';

    如果是在.vue文件中那么在外面套个style
    <style>
      @import './test.css';

    </style>

### 4.导入组件

    import name1 from './name1'
    import name2 from './name2'
    components:{
       name1,
       name2,
    },

### 5.导入 js

    比如你想给Arrary封一个属性，首先需要新建一个prototype.js的文件
    文件里
    Array.prototype.max = function(){
        return Math.max.apply({},this);
    }

    然后引入
    import './libs/prototype'
    在main.js中引用那么在所有的组件都可以用

    [].max();

### 6.引入 public 资源文件

    如果这个js文夹,放在vue-cli3中搭建的项目中的，public文件夹下，通过

    //.js可以省略不行
    import '/public/xxx.js'
    其实你在浏览器中看的时候，发现会报错误 ： 找不到 xxx模块；

    这时候你可以考虑吧xxx.js 放到src下的assets文件下

    然后你通过

    //@代表src文件夹，vue-cli中默认的
    import '@/assets/xxx.js'

参考：
https://www.jianshu.com/p/784e51ec68ce
