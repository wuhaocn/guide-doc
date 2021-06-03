# [AOP 详解](https://www.cnblogs.com/Fantastic-Code/p/11593544.html)

### 原文：https://www.cnblogs.com/hellxz/p/9629012.html

### 注意：在我们写 ssm 或者 spring 框架等等项目时，一旦这个 mapper 的相关处理不能满足现实的需求(比如要增强 CRUD 的时候，在本身对象实体不能满足需求，就要重新构建出新的组合实体来满足需求)，就需要增加一个 service 层来控制 CRUD 逻辑，这个时候就需要在 spring 的 xml 文件中添加 AOP 的切点和织入。

![](https://img2018.cnblogs.com/blog/1324938/201910/1324938-20191010112627443-838407091.png)

图片中声明切面中的 id 就是织入的 pointcut-ref 属性的值，织入的属性值就是 AOP 配置事务的 id,如下图：![](https://img2018.cnblogs.com/blog/1324938/201910/1324938-20191010113536426-957139838.png)

### 在此之前要进行如下操作：

![](https://img2018.cnblogs.com/blog/1324938/201910/1324938-20191010113733217-1942287832.png)

### Spring AOP 基本概念

1. 是一种动态编译期增强性 AOP 的实现
1. 与 IOC 进行整合，不是全面的切面框架
1. 与动态代理相辅相成
1. 有两种实现：基于 jdk 动态代理、cglib

### Spring AOP 与 AspectJ 区别

1. Spring 的 AOP 是基于动态代理的，动态增强目标对象，而 AspectJ 是静态编译时增强，需要使用自己的编译器来编译，还需要织入器
1. 使用 AspectJ 编写的 java 代码无法直接使用 javac 编译，必须使用 AspectJ 增强的 ajc 增强编译器才可以通过编译，写法不符合原生 Java 的语法；而 Spring AOP 是符合 Java 语法的，也不需要指定编译器去编译，一切都由 Spring 处理。

### JDK 动态代理与 Cglib 的区别

1. jdk 的动态代理需要实现接口 InvocationHandler
1. cglib 无需实现接口，使用字节码技术去修改 class 文件使继承
1. spring 默认使用 jdk 动态代理，如果没有实现接口会使用 cglib

### 使用步骤

1. 定义业务组件
1. 定义切点（重点）
1. 定义增强处理方法(切面方法)

### 依赖

jar 包依赖，除此以外还有 spring 依赖

1. aspectjweaver.jar
1. aspectjrt.jar
1. aspectj.jar
1. aopalliance.jar

maven 依赖

```
<dependencies> <!-- 有此依赖会远程下载其它相关依赖 --> <dependency> <groupId>org.springframework</groupId> <artifactId>spring-context</artifactId> <version>4.2.9.RELEASE</version> </dependency> <!-- aspectJ AOP 织入器 --> <dependency> <groupId>org.aspectj</groupId> <artifactId>aspectjweaver</artifactId> <version>1.8.9</version> </dependency> </dependencies>
```

### 注解方式开发

1. 扫描 Aspect 增强的类

```
<context:component-scanbase-package=""> <context:include-filtertype="annotation"expression="org.aspectj.lang.annotation.Aspect"/> </context:component-scan>
```

1. 开启@AspectJ 支持

<aop:aspectj-autoproxy/>

1. 使用@AspectJ 注解来标记一个切面类（spring 不会将切面注册为 Bean 也不会增强，但是需要扫描）
1. 使用其它注解进行开发（如下）

### 常用注解的使用

1. @Before：在切点方法前执行

- 在增强的方法上

@Before("execution(/_ 包名./_./\*(..))")

- 上述表达式可使用 pointcut 或切入表达式，效果一致,之后不再赘述
- 切点方法没有形参与返回值

示例代码

```
@Aspect public classAuthAspect{ //定义切点 @Pointcut("execution(_ com.cnblogs.hellxz.service._.\/*(..))") publicvoidpointCut(){} //前置处理 @Before("pointCut()") publicvoidauth(){ System.out.println("模拟权限检查……"); } }
```

1. @After：在切点方法后执行

- 用法同@Before
- @Around：在切点方法外环绕执行

- 在增强的方法上

@Around("execution(/_ 包名./_(..))")
或使用切点

@Around("pointcut()")

- 接收参数类型为

ProceedingJoinPoint
，必须有这个参数在切面方法的入参第一位

- 返回值为 Object
- 需要执行 ProceedingJoinPoint 对象的 proceed 方法，在这个方法前与后面做环绕处理，可以决定何时执行与完全阻止方法的执行
- 返回 proceed 方法的返回值
- @Around 相当于@Before 和@AfterReturning 功能的总和
- 可以改变方法参数，在 proceed 方法执行的时候可以传入 Object[]对象作为参数，作为目标方法的实参使用。
- 如果传入 Object[]参数与方法入参数量不同或类型不同，会抛出异常
- 通过改变 proceed()的返回值来修改目标方法的返回值

示例代码

```
@Aspect public classTxAspect{ //环绕处理 @Around("execution(_ com.cnblogs.hellxz.service._.\/*(..))") Objectauth(ProceedingJoinPoint point){ Object object = null; try { System.out.println("事务开启……"); //放行 object = point.proceed(); System.out.println("事务关闭……"); } catch (Throwable e) { e.printStackTrace(); } return object; } }
```

1. @AfterRetruning: 在方法返回之前，获取返回值并进行记录操作

- 和上边的方法不同的地方是该注解除了切点，还有一个返回值的对象名
- 不同的两个注解参数：returning 与 pointcut,其中 pointcut 参数可以为切面表达式，也可为切点
- returning 定义的参数名作为切面方法的入参名，类型可以指定。如果切面方法入参类型指定 Object 则无限制，如果为其它类型，则当且仅当目标方法返回相同类型时才会进入切面方法，否则不会
- 还有一个默认的 value 参数，如果指定了 pointcut 则会覆盖 value 的值
- 与@After 类似，但@AfterReturning 只有方法成功完成才会被织入，而@After 不管结果如何都会被织入
  虽然可以拿到返回值，但无法改变返回值

示例代码

```
@Aspect public classAfterReturningAspect{ @AfterReturning(returning="rvt", pointcut = "execution(_ com.cnblogs.hellxz.service._.\/*(..))") //声明 rvt 时指定的类型会限定目标方法的返回值类型，必须返回指定类型或者没有返回值 //rvt 类型为 Object 则是不对返回值做限制 publicvoidlog(Object rvt){ System.out.println("获取目标返回值："+ rvt); System.out.println("假装在记录日志……"); } /\/*\/* - 这个方法可以看出如果目标方法的返回值类型与切面入参的类型相同才会执行此切面方法 -@param itr _/ @AfterReturning(returning="itr", pointcut="execution(_ com.cnblogs.hellxz.service._._(..))") publicvoidtest(Integer itr){ System.out.println("故意捣乱……:"+ itr); } }
```

1. @AfterThrowing: 在异常抛出前进行处理，比如记录错误日志

- 与

@AfterReturning
类似，同样有一个切点和一个定义参数名的参数——throwing

- 同样可以通过切面方法的入参进行限制切面方法的执行，e.g. 只打印 IOException 类型的异常, 完全不限制可以使用 Throwable 类型
- pointcut 使用同

@AfterReturning

- 还有一个默认的 value 参数，如果指定了 pointcut 则会覆盖 value 的值
- 如果目标方法中的异常被 try catch 块捕获，此时异常完全被 catch 块处理，如果没有另外抛出异常，那么还是会正常运行，不会进入 AfterThrowing 切面方法

示例代码

```
@Aspect public classAfterThrowingAspect{ @Pointcut("execution(_ com.cnblogs.hellxz.test._.\/*(..))") publicvoidpointcut(){} /\/*\/* - 如果抛出异常在切面中的几个异常类型都满足，那么这几个切面方法都会执行 \/*/ @AfterThrowing(throwing="ex1", pointcut="pointcut()") //无论异常还是错误都会记录 //不捕捉错误可以使用 Exception publicvoidthrowing(Throwable ex1){ System.out.println("出现异常："+ex1); } @AfterThrowing(throwing="ex", pointcut="pointcut()") //只管 IOException 的抛出 publicvoidthrowing2(IOException ex){ System.out.println("出现 IO 异常: "+ex); } }
```

pointcut 定义的切点方法在@Before/@After/@Around 需要写在双引号中，e.g. @Before("pointCut()")

### JoinPoint 的概念与方法说明

### 概念

- 顾名思义，连接点，织入增强处理的连接点
- 程序运行时的目标方法的信息都会封装到这个连接点对象中
- 此连接点只读

### 方法说明

- Object[] getArgs()
  ：返回执行目标方法时的参数
- Signature getSignature()
  :返回被增强方法的相关信息，e.g 方法名 etc
- Object getTarget()
  :返回被织入增强处理的目标对象
- Object getThis()
  :返回 AOP 框架目标对象生成的代理对象

### 使用

- 在@Before/@After/@AfterReturning/@AfterThrowing 所修饰的切面方法的参数列表中加入 JoinPoint 对象，可以使用这个对象获得整个增强处理中的所有细节
- 此方法不适用于@Around, 其可用 ProceedingJoinPoint 作为连接点

### ProceedingJoinPoint 的概念与方法说明

### 概念

- 是 JoinPoint 的子类
- 与 JoinPoint 概念基本相同，区别在于是可修改的
- 使用@Around 时，第一个入参必须为 ProceedingJoinPoint 类型
- 在@Around 方法内时需要执行 proceed()或 proceed(Object[] args)方法使方法继续，否则会一直处于阻滞状态

### 方法说明

ProceedingJoinPoint 是 JoinPoint 的子类，包含其所有方法外，还有两个公有方法

- Object proceed()
  :执行此方法才会执行目标方法
- Object proceed(Object[] args)
  :执行此方法才会执行目标方法，而且会使用 Object 数组参数去代替实参,如果传入 Object[]参数与方法入参数量不同或类型不同，会抛出异常
  通过修改 proceed 方法的返回值来修改目标方法的返回值

### 编入的优先级

优先级最高的会最先被织入，在退出连接点的时候，具有最高的优先级的最后被织入

![](https://images2018.cnblogs.com/blog/1149398/201809/1149398-20180911092943285-1678031734.png)

当不同切面中两个增强处理切入同一连接点的时候，Spring AOP 会使用随机织入的方式
如果想要指定优先级，那么有两种方案：

- 让切面类实现

org.springframework.core.Ordered
接口，实现 getOrder 方法，返回要指定的优先级

- 切面类使用

@Order
修饰，指定一个优先级的值，值越小，优先级越高

### 访问目标方法的形参

除了使用 JoinPoint 或 ProceedingJoinPoint 来获取目标方法的相关信息外（包括形参），如果只是简单访问形参，那么还有一种方法可以实现

- 在 pointcut 的 execution 表达式之后加入

&& args(arg0,arg1)
这种方式

```
@Aspect public classAccessInputArgs{ @Before("execution(_ com.cnblogs.hellxz.test._.\/*(..)) && args(arg0, arg1)") publicvoidaccess(String arg0, String arg1){ System.out.println("接收到的参数为 arg0="+arg0+",arg1="+arg1); } }
```

注意：通过这种方式会只匹配到方法只有指定形参**数量**的方法，并且，在切面方法中指定的**类型**会限制目标方法，不符合条件的不会进行织入增强

### 定义切入点

通过定义切入点，我们可以复用切点，减少重复定义切点表达式等
切入点定义包含两个部分：

- 切入点表达式
- 包含名字和任意参数的方法签名

使用@Pointcut 注解进行标记一个无参无返回值的方法，加上切点表达式

```
@Pointcut("execution(/* com.cnblogs.hellxz.test./*./*(..))") publicvoidpointcut(){}
```

### 切入点指示符

Spring AOP 支持 10 种切点指示符：execution、within、this、target、args、@target、@args、@within、@annotation、bean 下面做下简记(没有写@Pointcut(),请注意)：

- **execution**: 用来匹配执行方法的连接点的指示符。
  用法相对复杂，格式如下:

execution(权限访问符 返回值类型 方法所属的类名包路径.方法名(形参类型) 异常类型)
e.g. execution(public String com.cnblogs.hellxz.test.Test.access(String,String))
权限修饰符和异常类型可省略，返回类型支持通配符，类名、方法名支持/\*通配，方法形参支持..通配

- **within**: 用来限定连接点属于某个确定类型的类。
  within(com.cnblogs.hellxz.test.Test)
  within(com.cnblogs.hellxz.test._) //包下类
  within(com.cnblogs.hellxz.test.._) //包下及子包下
- **this 和 target**: this 用于没有实现接口的 Cglib 代理类型，target 用于实现了接口的 JDK 代理目标类型
  举例：this(com.cnblogs.hellxz.test.Foo) //Foo 没有实现接口，使用 Cglib 代理，用 this
  实现了个接口 public class Foo implements Bar{...}
  target(com.cnblogs.hellxz.test.Test) //Foo 实现了接口的情况
- **args**: 对连接点的参数类型进行限制，要求参数类型是指定类型的实例。
  args(Long)
- **@target**: 用于匹配**类头有指定注解**的连接点
  @target(org.springframework.stereotype.Repository)
- **@args**: 用来匹配连接点的参数的，@args 指出连接点在运行时传过来的参数的类必须要有指定的注解

java @Pointcut("@args(org.springframework.web.bind.annotation.RequestBody)") public void methodsAcceptingEntities() {}

- **@within**: 指定匹配必须包括某个注解的的类里的所有连接点
  @within(org.springframework.stereotype.Repository)
- **@annotation**: 匹配那些有指定注解的连接点
  @annotation(org.springframework.stereotype.Repository)
- **bean**: 用于匹配指定 Bean 实例内的连接点，传入 bean 的 id 或 name,支持使用/\*通配符

### 切点表达式组合

使用&&、||、!、三种运算符来组合切点表达式，表示与或非的关系

execution(/_ com.cnblogs.hellxz.test./_./\*(..)) && args(arg0, arg1)

分类: [spring](https://www.cnblogs.com/Fantastic-Code/category/1524253.html)

[好文要顶]() [关注我]() [收藏该文]() [![](https://common.cnblogs.com/images/icon_weibo_24.png)]("分享至新浪微博") [![](https://common.cnblogs.com/images/wechat.png)]("分享至微信")

[FantasticSpeed](https://home.cnblogs.com/u/Fantastic-Code/)
[关注 - 15](https://home.cnblogs.com/u/Fantastic-Code/followees/)
[粉丝 - 0](https://home.cnblogs.com/u/Fantastic-Code/followers/)

[+加关注]()
0

0
[«](https://www.cnblogs.com/Fantastic-Code/p/11592380.html) 上一篇： [springboot 初学](https://www.cnblogs.com/Fantastic-Code/p/11592380.html '发布于 2019-09-26 16:04')
[»](https://www.cnblogs.com/Fantastic-Code/p/11597024.html) 下一篇： [拦截器处理](https://www.cnblogs.com/Fantastic-Code/p/11597024.html '发布于 2019-09-27 11:54')

posted @ 2019-09-26 18:16 [FantasticSpeed](https://www.cnblogs.com/Fantastic-Code/) 阅读(503) 评论(0) [编辑](https://i.cnblogs.com/EditPosts.aspx?postid=11593544) [收藏]() []()

[]()

[刷新评论]()[刷新页面]()[返回顶部]()
注册用户登录后才能发表评论，请 [登录]() 或 [注册]()， [访问](https://www.cnblogs.com/) 网站首页。

[【推荐】超 50 万行 VC++源码: 大型组态工控、电力仿真 CAD 与 GIS 源码库](http://www.ucancode.com/index.htm)
[【活动】腾讯云服务器推出云产品采购季 1 核 2G 首年仅需 99 元](https://cloud.tencent.com/act/season?fromSource=gwzcw.3422970.3422970.3422970&utm_medium=cpc&utm_id=gwzcw.3422970.3422970.3422970)
[【推荐】精品问答：前端开发必懂之 HTML 技术五十问](https://developer.aliyun.com/ask/258350?utm_content=g_1000088952)
[【推荐】精品问答：Java 技术 1000 问](https://developer.aliyun.com/ask/257905?utm_content=g_1000088947)
**相关博文：**
· [Spring AOP 详细介绍](https://www.cnblogs.com/liuruowang/p/5711563.html 'Spring AOP详细介绍')
· [spring 框架 AOP 核心详解](https://www.cnblogs.com/Snail-1174158844/p/9407535.html 'spring框架AOP核心详解')
· [spring AOP 编程--AspectJ 注解方式](https://www.cnblogs.com/caoyc/p/5627978.html 'spring AOP编程--AspectJ注解方式')
· [Spring 系列（四）：SpringAOP 详解](https://www.cnblogs.com/toby-xu/p/11361351.html 'Spring系列（四）：SpringAOP详解')
· [Spring -- AOP](https://www.cnblogs.com/androidsuperman/p/7501923.html 'Spring -- AOP')
» [更多推荐...](https://recomm.cnblogs.com/blogpost/11593544)

[精品问答：Java 技术 1000 问](https://developer.aliyun.com/ask/257905?utm_content=g_1000088947)

**最新 IT 新闻**:
· [我国首颗 5G 卫星通信试验成功：通信能力达 10Gbps]()
· [刘强东卸任京东云计算全资子公司经理一职]()
· [恒星参宿四停止变暗 近期或不会发生超新星爆炸]()
· [服务器 DRAM 合约价暴涨，但 PC 和手机快涨不动了]()
· [回应质疑：统一操作系统 UOS 的五大杀手锏]()
» [更多新闻...](https://news.cnblogs.com/ 'IT 新闻')
**历史上的今天：**
2019-09-26 [springboot 初学](https://www.cnblogs.com/Fantastic-Code/p/11592380.html)
