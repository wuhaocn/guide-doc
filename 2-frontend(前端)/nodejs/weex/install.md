1  安装Node.js

去Node.js 官网  https://nodejs.org/      下载安装文件安装。

image

安装好后，根据如下命令检查是否安装正常。 在windows 环境下，开始-运行，输入cmd ，打开一个命令行工具  （执行命令时，不要前面的$ 符号）

$ node -v
v6.11.3
$ npm -v
3.10.10
 
如果已经安装了node.js  ，npm 版本较低的话，可以通过如下命令升级npm weex 要求，npm版本>5 (参见官网http://weex.apache.org/guide/ )
 
2  安装Weex 的weex-toolkit 工具
$ npm install -g weex-toolkit
$ weex -v //查看当前weex版本  （通过此命令检查是否安装正常,）
如果嫌弃NPM  比较慢，可以使用淘宝的 npm 镜像，
$ npm install -g cnpm --registry=https://registry.npm.taobao.org
$ cnpm install -g weex-toolkit
 
3 创建项目初始化 
$ weex create awesome-app 
一步步按照引导程序选择下来，就可以安装完毕。（会生成一个 awesome-app 的目录）
4  安装依赖，启用开发环境
 在dos 命令下，
cd awesome-app    进入 项目目录
npm install     // 安装依赖
npm start       // 启动项目       （注意，不是 npm run dev ，不是 npm run dev ，不是 npm run dev 。 重要的事情说三遍 ）
image
访问这个链接，就看到demo 了。
