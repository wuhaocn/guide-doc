### Dubbo源码-SPI(二)SPI的目的和实现
    
    2018年08月01日 18:19:00 hzqf丶 阅读数 510
    版权声明：本文为博主原创文章，未经博主允许不得转载。 https://blog.csdn.net/qq_38237683/article/details/81237367
    上篇,我们讲了spi的目的和约定,那么从这篇起,我们源码的角度来看dubbo spi到底做了什么,怎么实现的.
    


### Dubbo spi的目的:
    目的其实很假单,就是获取一个实现类对象.
    
    Dubbo spi的途径:
    ExtensionLoader.getExtension(String name)来获取一个实现类对象.
    
    Dubbo spi的实现路径:
    ExtensionLoader. getExtensionLoader(Class<T> type)
    ExtensionLoader.public T getAdaptiveExtension()
    ExtensionLoader.getExtension(String name)




    源码阅读,从第一行代码开始从main方法进入,初始化一个属性,从这开始
    
    
    ------ExtensionLoader. getExtensionLoader(Class<T> type)--------
    ExtensionLoader.getExtensionLoader(ExtensionFactory.class)
    
    --->this.type=type;//初始化 type和objectFactory属性,赋值之后保存到缓存中(map)
    
    objectFactory=(type==ExtensionFactory.class?null:ExtensionLoader.getExtensionLoader(ExtensionFactory.class).getAdaptiveExtension());
    
          --->ExtensionLoader.getExtensionLoader(ExtensionFactory.class).getAdaptiveExtension());
    
                --->this.type=type
    
                      objectFactory=null
    
    执行以上代码完成了2个属性的初始化
    
    1.每一个ExtensionLoader都包含2个值type和objectFactory.
    
                     Class<?>   type //构造器初始化时要得到的接口名
    
                     ExtensionFactory   objectFactory
    
    2.创建一个ExtensionLoader 存储在ConcurrentMap<Class<?>,ExtensionLoader<?>>EXTENSION_LOADERS中.
    
    
    
    objectFactory:
    
    objectFactory就是ExtensionFactory,他是通过ExtensionLoader.getExtensionLoader(ExtensionFactory.class)来实现的,但是他的值为null
    objectFactory的作用就是为dubbo的IOC提供所有对象
    
    
    -------------------------getAdaptiveExtension() -------------------------
    Adaptive
    
    是一个注解,只能注解在类和方法上
    
    注解在类上时:表示这个类时一个人工编码,实现了一个装饰类(装饰者模式).例如:ExtensionFactory
    注解在方法上:表示自动生成和编译一个动态的Adaptive类,例如Protocol$Adaptive
    
    
    我们从Duboo的命名空间为起始点,Protocol为例子来阅读源码,了解getAdaptiveExtension()做了什么事情
    
    
    
    
    
    
    
    --->getAdaptiveExtension() //为cachedAdaptiveInstance(缓存的adaptive实例)赋值
    
          --->createAdaptiveExtension()//获取adaptive实例
    
                 --->getAdaptiveExtensionClass()
    
                       --->getExtensionClasses()//为cachedClasses赋值.最后通过loadFile可知,如果类上上包含Adaptivre注解,那么就是                                                                 人工编码的类,直接获取之后返回
    
                                 --->loadExtensionClasses()
    
                                          --->loadFile//加载解析配置文件(spi)中的内容,并存存放在缓存变量中
    
                       -->createAdaptiveExtensionClass//如果不是人工编码的(Adaptive注解在方法上),那么就会动态创建编译这个类.创建                                                                             的类是一个代理类
    
                       --->createAdaptiveExtensionClassCode()通过Adaptive模板来生成代码(生成结果如下)
    
    
    
                       --->ExtensionLoader.getExtensionLoader(Compiler.class).getAdaptiveExtension();
    
                        --->compiler.compile(code,classLoader)//动态编译类
    
                --->injectExtension()//进入IOC的控制反转
    
    
    
    loadFile()的目的与流程:
    
    目的:加载解析配置文件(某个类的spi)中的内容,并存存放在缓存变量中,各种变量如下
    
    cachedAdaptiveClass变量//如果这类上使用了Adaptivre注解,将其缓存起来(注解在类上)
    
    cachedWrapperClasses变量//如果类上没使用Adaptivre注解,且其构造函数中包含接口(type)类型,如                                                                                            ProtocolFilterWrapper,ProtocolListenerWrapper才可以命中并缓存
    
    cachedActivates//剩下的如果类中包含Adaptivre注解,将其缓存(注解在方法上)
    
    cachedNames//其他的没有被命中的放在这个变量中
    
    
    
    至此spi在dubbo中的作用和实现就基本体现出来了,这次没这么配图,抱歉.
    
    匆忙之间写出来了,以后会加上,希望各位同学还是自动动手debug下,可以加深下印象,