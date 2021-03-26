#### jar 任务

##### 1. 打包成一个 Jar

    jar {
        from {
            //添加依懒到打包文件
            //configurations.compile.collect { it.isDirectory() ? it : zipTree(it) }
            configurations.runtime.collect{zipTree(it)}
        }
        manifest {
            attributes 'Main-Class': appMainClass
        }
    }

    执行命令gradle build或者./gradlew build，可在build/libs查看生成的jar包

##### 2. 打包成多个 Jar

    jar {
        manifest {
            attributes 'Main-Class': appMainClass
        }
    }

    task clearJar(type: Delete) {
        delete 'build/libs/lib'
    }

    task copyJar(type: Copy) {
        from configurations.runtime
        into('build/libs/lib')
    }

    task release(type: Copy, dependsOn: [build, clearJar, copyJar])

    执行命令gradle release或者./gradlew relesse，可在build/libs查看生成的jar包

    两种方式都各有缺陷，打包成一个Jar当依懒比较多情况下Jar包会很大，其它工程要要单独引用某个Jar不方便；
    打包成多个Jar没有启动脚本，不熟悉Java的新手不懂得运行。
    application插件
    apply plugin: 'application'
    mainClassName = appMainClass

    执行命令gradle build或者./gradlew build，查看build/distributions会有两个压缩文件，压缩文件包含了两个文件夹，bin为启动脚本，lib则是软件jar包和依赖。还可以执行./gradlew installDist生成未压缩文件目录build/install。
    这种方式最为简单，不需要添加复杂的脚本，打包成多个jar并生成启动脚本可一键运行

#### 全部脚本参考

    def appMainClass = 'HelloWorldKt'

    apply plugin: 'java'
    apply plugin: 'kotlin'

    apply plugin: 'maven'
    archivesBaseName = 'app'
    // 生成启动脚本打包
    //apply plugin: 'application'
    //mainClassName = appMainClass

    sourceCompatibility = 1.8

    repositories {
        mavenCentral()
    }

    dependencies {
        compile "org.jetbrains.kotlin:kotlin-stdlib-jre8:$kotlin_version"
        testCompile group: 'junit', name: 'junit', version: '4.12'
    }

    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }

    jar {
        configurations.runtime.each { println it.path }
        println "========="
        configurations.compile.each { println it.path }
        println "========="


        from {
            //添加依懒到打包文件
            //configurations.compile.collect { it.isDirectory() ? it : zipTree(it) }
            configurations.runtime.collect { zipTree(it) }
        }
        manifest {
            attributes 'Main-Class': appMainClass
        }
    }

    task clearJar(type: Delete) {
        delete 'build/libs/lib'
    }

    task copyJar(type: Copy) {
        from configurations.runtime
        into('build/libs/lib')
    }

    task release(type: Copy, dependsOn: [build, clearJar, copyJar])

参考

作者：玉兔是我啊
链接：https://www.jianshu.com/p/5bb1e87df15f
来源：简书
简书著作权归作者所有，任何形式的转载都请联系作者获得授权并注明出处。
