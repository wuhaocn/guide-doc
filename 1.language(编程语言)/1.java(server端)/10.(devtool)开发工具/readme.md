### 1.开发工具
#### 1.1.ide
    Eclipse:
        Eclipse是一款基于Java的开源可扩展开发平台，Eclipse不是一门编程语言，而是一个框架和一组服务。
        Eclipse为开发者提供了一个标准的插件集，包括Java开发工具（Java Development Kit，JDK）。
        虽然Eclipse 是使用Java语言开发的，但它的用途并不限于 Java 语言；还提供支持C/C++、COBOL、PHP、Android等编程语言的插件。
    
    MyEclipse:
        MyEclipse，是在eclipse 基础上加上自己的插件开发而成的功能强大的企业级集成开发环境，主要用于Java、Java EE以及移动应用的开发。
        MyEclipse的功能非常强大，支持也十分广泛，尤其是对各种开源产品的支持相当不错。
        
    IntelliJ IDEA:
        DEA 全称 IntelliJ IDEA，是java编程语言开发的集成环境。IntelliJ在业界被公认为最好的java开发工具之一，
        尤其在智能代码助手、代码自动提示、重构、J2EE支持、各类版本工具(git、svn等)、JUnit、CVS整合、代码分析、 
        创新的GUI设计等方面的功能可以说是超常的。IDEA是JetBrains公司的产品，这家公司总部位于捷克共和国的首都布拉格，
        开发人员以严谨著称的东欧程序员为主。它的旗舰版本还支持HTML，CSS，PHP，MySQL，Python等。免费版只支持Python等少数语言。
        
        免费版:基础java语言开发
            
        收费版:扩展组件较为丰富
        
#### 1.2.依赖打包工具
    Gradle:
        Gradle是一个基于Apache Ant和Apache Maven概念的项目自动化构建开源工具。
        它使用一种基于Groovy的特定领域语言(DSL)来声明项目设置，抛弃了基于XML的各种繁琐配置。
        面向Java应用为主。当前其支持的语言限于Java、Groovy、Kotlin和Scala，计划未来将支持更多的语言。
    Maven:
        Maven项目对象模型(POM)，可以通过一小段描述信息来管理项目的构建，报告和文档的项目管理工具软件。
        Maven 除了以程序构建能力为特色之外，还提供高级项目管理工具。由于 Maven 的缺省构建规则有较高的可重用性，
        所以常常用两三行 Maven 构建脚本就可以构建简单的项目。由于 Maven 的面向项目的方法，许多 Apache Jakarta 项目发文时使用 Maven，
        而且公司项目采用 Maven 的比例在持续增长。
        Maven这个单词来自于意第绪语（犹太语），意为知识的积累，最初在Jakata Turbine项目中用来简化构建过程。
        当时有一些项目（有各自Ant build文件），仅有细微的差别，而JAR文件都由CVS来维护。
        于是希望有一种标准化的方式构建项目，一个清晰的方式定义项目的组成，一个容易的方式发布项目的信息，以及一种简单的方式在多个项目中共享JARs。
#### 1.3.版本管理工具
    Git:
        Git(读音为/gɪt/。)是一个开源的分布式版本控制系统，可以有效、高速地处理从很小到非常大的项目版本管理。 
        Git 是 Linus Torvalds 为了帮助管理 Linux 内核开发而开发的一个开放源码的版本控制软件。
        Torvalds 开始着手开发 Git 是为了作为一种过渡方案来替代 BitKe [1] 
    SVN:
        SVN是Subversion的简称，是一个开放源代码的版本控制系统，相较于RCS、CVS，它采用了分支管理系统，它的设计目标就是取代CVS。
        互联网上很多版本控制服务已从CVS迁移到Subversion。说得简单一点SVN就是用于多个人共同开发同一个项目，共用资源的目的.