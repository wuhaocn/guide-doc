#### go mod
##### gomod初尝试

    下载官方包1.11(及其以上版本将会自动支持gomod) 默认GO111MODULE=auto(auto是指如果在gopath下不启用mod)
    设置【on/offer/auto】
    export GO111MODULE=on
##### gomod命令    
    go mod help查看帮助
    go mod init<项目模块名称>初始化模块，会在项目根目录下生成 go.mod文件。
    go mod tidy根据go.mod文件来处理依赖关系。
    go mod vendor将依赖包复制到项目下的 vendor目录。建议一些使用了被墙包的话可以这么处理，方便用户快速使用命令go build -mod=vendor编译
    go list -m all显示依赖关系。go list -m -json all显示详细依赖关系。
    go mod download <path@version>下载依赖。参数<path@version>是非必写的，path是包的路径，version是包的版本。
    在gopath外新建一个项目，单独开一个cmd设置set GO111MODULE=on(习惯性的和git初始化一样)go mod init然后报错了。 正解如下：go mod init xxx（module名称可与文件名不同）

    在项目目录下执行go mod tidy下载完成后项目路径下会生成go.mod和go.sum
    
    go.mod文件必须要提交到git仓库，但go.sum文件可以不用提交到git仓库(git忽略文件.gitignore中设置一下)。
    go模块版本控制的下载文件及信息会存储到GOPATH的pkg/mod文件夹里。
    
    在国内访问golang.org/x的各个包都需要翻墙，我们可以在go.mod中使用replace替换成github上对应的库。(强烈建议翻墙，我使用的lantern专业版+proxifier)非常稳定

#### go proxy代理

    In Linux or macOS, you can execute the below commands.
    
    linux/mac
    
    export GO111MODULE=on
    export GOPROXY=https://goproxy.io
    
    windows
    # Enable the go modules feature
    $env:GO111MODULE=on
    # Set the GOPROXY environment variable
    $env:GOPROXY=https://goproxy.io
    
    Now, when you build and run your applications, go will fetch dependencies via goproxy.io. See more information in the goproxy repository.

