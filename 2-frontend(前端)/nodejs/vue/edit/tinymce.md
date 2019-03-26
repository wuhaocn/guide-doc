在 Vue 项目中引入 tinymce 富文本编辑器


#### 富媒体编辑器对比
    UEditor：百度前端的开源项目，功能强大，基于 jQuery，但已经没有再维护，而且限定了后端代码，修改起来比较费劲
    
    bootstrap-wysiwyg：微型，易用，小而美，只是 Bootstrap + jQuery...
    
    kindEditor：功能强大，代码简洁，需要配置后台，而且好久没见更新了
    
    wangEditor：轻量、简洁、易用，但是升级到 3.x 之后，不便于定制化开发。不过作者很勤奋，广义上和我是一家人，打个call
    
    quill：本身功能不多，不过可以自行扩展，api 也很好懂，如果能看懂英文的话...
    
    summernote：没深入研究，UI挺漂亮，也是一款小而美的编辑器，可是我需要大的
    
在有这么参考的情况下，我最终还是选择了 tinymce 这个不搭梯子连官网都打不开的编辑器（简直是自讨苦吃），主要因为两点：
    
    1. GitHub 上星星很多，功能也齐全；
    
    2. 唯一一个从 word 粘贴过来还能保持绝大部分格式的编辑器；
    
    3. 不需要找后端人员扫码改接口，前后端分离；
    
    4. 说好的两点呢！

 

#### 一、资源下载

tinymce 官方为 vue 项目提供了一个组件 tinymce-vue

    npm install @tinymce/tinymce-vue -S
    
    在 vscode、webstorm 的终端运行这段代码可能会报错，最好使用操作系统自带的命令行工具
  如果有购买 tinymce 的服务，可以参考 tinymce-vue 的说明，通过 api-key 直接使用 tinymce
 
  没购买的，还是要老老实实下载 tinymce

    npm install tinymce -S
    安装之后，在 node_modules 中找到 tinymce/skins 目录，然后将 skins 目录拷贝到 static 目录下
    // 如果是使用 vue-cli 3.x 构建的 typescript 项目，就放到 public 目录下，文中所有 static 目录相关都这样处理
    
    tinymce 默认是英文界面，所以还需要下载一个中文语言包（记得搭梯子！搭梯子！搭梯子！）
    
    然后将这个语言包放到 static 目录下，为了结构清晰，我包了一层 tinymce 目录



 

#### 二、初始化

    在页面中引入以下文件
    
    import tinymce from 'tinymce/tinymce'
    import 'tinymce/themes/modern/theme'
    import Editor from '@tinymce/tinymce-vue'
    tinymce-vue 是一个组件，需要在 components 中注册，然后直接使用
    
    
    
    <Editor id="tinymce" v-model="tinymceHtml" :init="editorInit"></Editor>
     
    
    这里的 init 是 tinymce 初始化配置项，后面会讲到一些关键的 api，完整 api 可以参考官方文档
    
    编辑器需要一个 skin 才能正常工作，所以要设置一个 skin_url 指向之前复制出来的 skin 文件
    
    editorInit: {
      language_url: '/static/tinymce/zh_CN.js',
      language: 'zh_CN',
      skin_url: '/static/tinymce/skins/lightgray',
      height: 300
    }
    // vue-cli 3.x 创建的 typescript 项目，将 url 中的 static 去掉，即 skin_url: '/tinymce/skins/lightgray'
    
    同时在 mounted 中也需要初始化一次：
    
    
    
    如果在这里传入上面的 init 对象，并不能生效，但什么参数都不传也会报错，所以这里传入一个空对象
    
     
    
    有朋友反映这里有可能出现以下报错
    
     
    
    这是因为 init 参数地址错误，请核对下 init 参数中的几个路径是否正确
    
    如果参数无误，可以先删除 language_url 和 language 再试

 

 三、扩展插件

    完成了上面的初始化之后，就已经能正常运行编辑器了，但只有一些基本功能
    
    tinymce 通过添加插件 plugins 的方式来添加功能
    
    比如要添加一个上传图片的功能，就需要用到 image 插件，添加超链接需要用到 link 插件
    
    
    
    同时还需要在页面引入这些插件：
    
    
    
    添加了插件之后，默认会在工具栏 toolbar 上添加对应的功能按钮，toolbar 也可以自定义
    
    贴一下完整的组件代码：
    
    复制代码
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
    import 'tinymce/plugins/image'
    import 'tinymce/plugins/link'
    import 'tinymce/plugins/code'
    import 'tinymce/plugins/table'
    import 'tinymce/plugins/lists'
    import 'tinymce/plugins/contextmenu'
    import 'tinymce/plugins/wordcount'
    import 'tinymce/plugins/colorpicker'
    import 'tinymce/plugins/textcolor'
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
    复制代码


 

四、上传图片

    tinymce 提供了 images_upload_url 等 api 让用户配置上传图片的相关参数
    
    但为了在不麻烦后端的前提下适配自家的项目，还是得用 images_upload_handler 来自定义一个上传方法
    
    
    
    这个方法会提供三个参数：blobInfo, success, failure
    
    其中 blobinfo 是一个对象，包含上传文件的信息：
    
    
    
    success 和 failure 是函数，上传成功的时候向 success 传入一个图片地址，失败的时候向 failure 传入报错信息
    
     
    
    贴一下我自己的上传方法，使用了 axios 发送请求
    
    复制代码
    handleImgUpload (blobInfo, success, failure) {
      let formdata = new FormData()
      formdata.set('upload_file', blobInfo.blob())
      axios.post('/api/upload', formdata).then(res => {
        success(res.data.data.src)
      }).catch(res => {
        failure('error')
      })
    }
    复制代码