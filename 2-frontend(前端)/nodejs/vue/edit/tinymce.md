程序员大本营
技术文章内容聚合第一站

首页 / 联系我们

 
Vue富文本编辑器 tinymce
1. 介绍tinymce
1.1 查看官网https://www.tiny.cloud/
1.2 查看样子


 

2 入门使用
2.1 资源下载
第一步：npm install @tinymce/tinymce-vue -S
第二步：npm install tinymce -S
2.2安装之后，在 node_modules 中找到 tinymce/skins 目录，然后将 skins 目录拷贝到 static 目录下
2.3 tinymce 默认是英文界面，所以还需要下载一个中文语言包
下载地址：https://www.tiny.cloud/get-tiny/language-packages/

如图所示：



下载后解压提取：zh_CN.js 并拷贝到static/tinymce/目录下面



最后目录结构为



3. 初始化页面
导包

import tinymce from 'tinymce/tinymce'
import 'tinymce/themes/modern/theme'
import Editor from '@tinymce/tinymce-vue'
注意 这里的Editor是tinymce-vue 的一个组件 需要在自己的页面注册后使用

export default {
  name: 'Tinymce',
  components: { Editor },
3.2 添加页面代码
<Editor id="tinymce" v-model="tinymceHtml" :init="editorInit"></Editor>
3.3 编辑器需要一个 skin 才能正常工作，所以要设置一个 skin_url 指向之前复制出来的 skin 文件
editorInit: {
  language_url: '/static/tinymce/zh_CN.js',
  language: 'zh_CN',
  skin_url: '/static/tinymce/skins/lightgray',
  height: 300
}
此段代码放在data(){}函数中，直接返回即可

data() {
    return { editorInit: { language_url: '/static/tinymce/zh_CN.js', language: 'zh_CN', skin_url: '/static/tinymce/skins/lightgray', height: 300 }}
  }
3.4 在mounted也要初始化一次
mounted() {
    tinymce.init({})
  },
4. 贴出完整的代码
<template>
  <div class='tinymce'>
    <h1>tinymce</h1>
    <editor id='tinymce' v-model='tinymceHtml' :init='init'></editor>
    <div v-html='tinymceHtml'></div>
  </div>
</template>
 
<script>
import tinymce from 'tinymce/tinymce'
import 'tinymce/themes/modern/theme'
import Editor from '@tinymce/tinymce-vue'
export default {
  name: 'tinymce',
  data () {
    return {
      tinymceHtml: '请输入内容',
      init: {
        language_url: '/static/tinymce/zh_CN.js',
        language: 'zh_CN',
        skin_url: '/static/tinymce/skins/lightgray',
        height: 300,
        plugins: 'link lists image code table colorpicker textcolor wordcount contextmenu',
        toolbar:
          'bold italic underline strikethrough | fontsizeselect | forecolor backcolor | alignleft aligncenter alignright alignjustify | bullist numlist | outdent indent blockquote | undo redo | link unlink image code | removeformat',
        branding: false
      }
    }
  },
  mounted () {
    tinymce.init({})
  },
  components: {Editor}
}
</script>
 

相关文章
Vue富文本编辑器 tinymce
Django 之 富文本编辑器-tinymce
vue-cli3 使用 Tinymce富文本编辑器
Tinymce富文本编辑器 在 vue 项目中的封装与使用 解决上传图片与文件
django验证码插件captcha以及富文本编辑器tinymce部署
富文本编辑器tinymce支持从word复制粘贴保留格式和图片的插件powerpaste
vue中TinyMCE编辑器的使用
vue中引入富文本编辑器ueditor
富文本编辑器--Ueditor
NicEdit 富文本编辑器
Copyright © 2018-2019 - All Rights Reserved - www.pianshen.com 
import tinymce from 'tinymce/tinymce'
import 'tinymce/themes/modern/theme'
import Editor from '@tinymce/tinymce-vue'


export default {
  name: 'Tinymce',
  components: { Editor },


<Editor id="tinymce" v-model="tinymceHtml" :init="editorInit"></Editor>

