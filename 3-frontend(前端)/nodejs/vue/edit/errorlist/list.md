Can't resolve 'sass-loader' in /xxxx

Vue2+element UI（不会推荐去Element UI官网看看）项目中需要用到<style lang="scss">，发现每次都会报错，Can't resolve 'sass-loader' in 

    在Chrome浏览器中的错误：

    就是不能解析sass-loader 这种东西。 
    所以第一步需要在项目中引入：
        
      1.
      npm install sass-loader -D
    
      
      2.
      npm install node-sass -D
        或者
      cnpm install node-sass -D 


如果你还不行那可能就需要去webpack.base.conf.js中的module：的rules：[ ] 下最后添加试试（反正我是没有用）：

     {
        test: /\.vue$/,
        loader: 'vue-loader',
        options: {
            loaders: {
                scss: 'style-loader!css-loader!sass-loader',
                sass: 'style-loader!css-loader!sass-loader?indentedSyntax',
            },
        },
    },


下面是官方Sass Loader的引入方法Sass Loade官方仓库：
    
    npm install sass-loader node-sass webpack --save-dev
    
    npm install style-loader css-loader --save-dev
    
    // webpack.config.js
    module.exports = {
        ...
        module: {
            rules: [{
                test: /\.scss$/,
                use: [{
                    loader: "style-loader" // creates style nodes from JS strings
                }, {
                    loader: "css-loader" // translates CSS into CommonJS
                }, {
                    loader: "sass-loader" // compiles Sass to CSS
                }]
            }]
        }
    };
--------------------- 
作者：CodeChenL 
来源：CSDN 
原文：https://blog.csdn.net/qq_24058693/article/details/80056557 
版权声明：本文为博主原创文章，转载请附上博文链接！