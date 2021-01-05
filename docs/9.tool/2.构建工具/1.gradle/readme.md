#### 基本使用流程：

    官方文档：https://docs.gradle.org/current/userguide/userguide.html
    建议直接查看官方文档，因为不同版本之前差异较大

    1. 下载 Gradle 到本地，并配置环境变量。

    2. 在项目中创建 build.gradle文件

    3. 编写 build.grade 脚本

    4. 执行grade 命令

#### 快捷使用方法：

    下载Android Studio，让它帮我们搞定一切。

    指定依赖：
    1. 依赖仓库中的jar

      compile 'group:name:version'
    2. 依赖本地jar文件夹

      compile fileTree(dir: 'libs', include: '*.jar')
    3. 依赖本地单个jar文件

      compile file('libs/xxx')
    3. 依赖工程

     compile project(:xx:xx)
    4. Android library依赖

#### 仓库配置：

    1. 从中央仓库下载：mavenCentral()

    2. 使用本地Maven仓库：maven {  url “file://F:/githubrepo/releases”      }

    3. 指定远程仓库：maven{url “https://xxx”}
