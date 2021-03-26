Creating a markdown app with Electron and React

简介

Electron 是一个使用 js 来开发跨平台桌面端软件的一个框架。
比较著名的产品包括：

VS code
skype
atom
钉钉
SwitchHosts!
重点

electron 的原理是把 chromium 的 v8 引擎和 node 的运行时结合到了一起。
架构还是前后端分离
前端按照 web 开发的方式正常开发
通信原理：每一个前端窗口是一个进程，后端单独是一个进程。electron 提供前后端进程间通信的接口。
开发流程

前提条件

有一个可以跑通的 web 项目，比如用 create-react-app 创建的项目。

安装 electron

npm install electron --save-dev
在 package.json 的 script 中添加
"electron-start": "electron ."
在 package.json 的顶层添加
"main": "public/main.js"
此时的 package.json 看起来是这样的

{
"name": "mook",
"version": "0.1.0",
"main": "public/main.js",
"private": true,
"dependencies": {
"react": "^15.6.1",
"react-dom": "^15.6.1",
"react-scripts": "1.0.13"
},
"scripts": {
"start": "react-scripts start",
"build": "react-scripts build",
"test": "react-scripts test --env=jsdom",
"eject": "react-scripts eject",
"electron-start": "electron ."
},
"devDependencies": {
"electron": "^1.7.6"
}
}
main.js 文件是后端主进程的执行入口，内容如下：

const electron = require('electron');
const app = electron.app;
const BrowserWindow = electron.BrowserWindow;

let mainWindow;

function createWindow() {
mainWindow = new BrowserWindow({width: 900, height: 680});
mainWindow.loadURL('http://localhost:3000');

app.setAboutPanelOptions({
applicationName: "Mook",
applicationVersion: "0.0.1",
})

mainWindow.on('closed', () => mainWindow = null);
}

app.on('ready', createWindow);

app.on('window-all-closed', () => {
if (process.platform !== 'darwin') {
app.quit();
}
});

app.on('activate', () => {
if (mainWindow === null) {
createWindow();
}
});
其中 mainWindow.loadURL('http://localhost:3000');这行为加载的前端页面的地址。
此时，先执行 npm run start 启动前端页面，再执行 npm run electron-start 启动 electron。

简化开发启动

每次启动都需要执行两个命令，比较麻烦，因此，可以通过安装几个包来解决这个问题。
npm install wait-on concurrently --save-dev
然后再 package.json 文件的 script 中添加
"electron-dev": "concurrently \"npm start\" \"wait-on http://localhost:8080 && electron .\"",
这样，就可以通过执行 npm run electron-dev 一个命令启动了。

创建打包脚本

首先需要安装打包工具
npm install electron-builder --save-dev
添加打包配置
然后再 package.json 文件的 script 中添加
"preelectron-pack": "npm run build"
以及
"electron-pack": "build -c.extraMetadata.main=build/main.js"
preelectron-pack 命令会在执行 npm run electron-pack 之前自动执行，也就是会先打包前端的代码，生成一个 html 文件。
需要注意的是，build/main.js 就是刚刚 package.json 文件中的 public/main.js。只不过在打包前端代码的时候一并打包入了 build 目录中。如果你用的不是 create-react-app，那么需要注意把这个文件一起打包进去。
在 package.json 中，需要增加一个 build 块和 homepage 属性，build 用来告诉 electron-pack 如何打包，而 homepage 则指明了 js 和 css 文件的位置。配置如下：
"homepage": "./",
"build": {
"appId": "com.mook",
"files": [
"build/**/*",
"node_modules/**/*"
],
"directories": {
"buildResources": "assets"
}
}
修改 main.js
之前的 main.js 是加载了http://localhost:3000的方式进行的。打包后不能继续使用这种方法。需要安装一个包
npm install electron-is-dev --save
这个包用来判断当前执行的环境。
之后修改 public/main.js
const isDev = require('electron-is-dev');
const path = require('path');
将原来的

mainWindow.loadURL('http://localhost:3000');
改为

mainWindow.loadURL(isDev ? 'http://localhost:3000' : `file://${path.join(__dirname, '../build/index.html')}`);
打包
执行 npm run electron-pack 就会生成 dmg 文件。
至此整个项目的架子基本就已经搭完了。

前后端通信

electron 提供了 ipcMain 和 ipcRenderer 两个类进行通信。
前端发送请求

const {ipcRenderer} = window.require('electron')
ipcRenderer.send(channel, data)
其中 channel 可以理解为调用的方法名，需要后端对该方法进行监听。
后端接受请求

const { ipcMain } = electron
ipcMain.on(channel, (event, arg) => {

# do something here

})
反过来
后端发送请求

mainWindow.webContents.send(channel, data)
其中 mainWindow 为项目启动时创建的窗口，如果创建了多个窗口，可以向指定的窗口发送请求。
前端监听请求

const {ipcRenderer} = window.require('electron')
ipcRenderer.on(channel, (event, arg) => {

# do something here

})
小结

以上是开发工程中遇到的比较费时间的一些事情的整理。electron 用来开发一些小工具还是一个不错的选择。

参考：
https://www.jianshu.com/p/834fd8b0b0e1
