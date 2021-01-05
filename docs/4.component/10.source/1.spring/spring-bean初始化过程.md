### [Spring Bean 初始化过程](https://www.cnblogs.com/fyx158497308/p/3977391.html)

#### 一、首先我们来看 ClassPathXmlApplicationContext 类体系结构

**![](https://images0.cnblogs.com/blog/665504/201409/171538180811731.jpg)**

#### 从该继承体系可以看出：

    1. BeanFactory 是一个 bean 工厂的最基本定义，里面包含了一个 bean 工厂的几个最基本的方 法，getBean(…) 、 containsBean(…) 等 ,是一个很纯粹的bean工厂，不关注资源、资源位置、事件等。 ApplicationContext 是一个容器的最基本接口定义，它继承了 BeanFactory, 拥有工厂的基本方法。同时继承了 ApplicationEventPublisher 、 MessageSource 、 ResourcePatternResolver 等接口， 使其 定义了一些额外的功能，如资源、事件等这些额外的功能。

    2. AbstractBeanFactory 和 AbstractAutowireCapableBeanFactory 是两个模 板抽象工厂类。AbstractBeanFactory 提供了 bean 工厂的抽象基类，同时提供 了 ConfigurableBeanFactory 的完整实现。AbstractAutowireCapableBeanFactory 是继承 了 AbstractBeanFactory 的抽象工厂，里面提供了 bean 创建的支持，包括 bean 的创建、依赖注入、检查等等功能，是一个 核心的 bean 工厂基类。

    3. ClassPathXmlApplicationContext之 所以拥有 bean 工厂的功能是通过持有一个真正的 bean 工厂DefaultListableBeanFactory 的实例，并通过 代理 该工厂完成。

    4. ClassPathXmlApplicationContext 的初始化过程是对本身容器的初始化同时也是对其持有的DefaultListableBeanFactory 的初始化。

#### 二、容器的初始化过程

    我们知道在spring中BeanDefinition很重要。因为Bean的创建是基于它的。容器AbstractApplicationContext中有个方法是refresh()这个里面包含了整个Bean的初始化流程实现过程，如果我们需要自己写一个ClassPathXmlApplicationContext类的话我们可以继承这个类，下面贴段这个方法的代码：

    public void refresh() throws BeansException, IllegalStateException {
            synchronized (this.startupShutdownMonitor) {
                // Prepare this context for refreshing.
                prepareRefresh();

                // Tell the subclass to refresh the internal bean factory.
                ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();

                // Prepare the bean factory for use in this context.
                prepareBeanFactory(beanFactory);

                try {
                    // Allows post-processing of the bean factory in context subclasses.
                    postProcessBeanFactory(beanFactory);

                    // Invoke factory processors registered as beans in the context.
                    invokeBeanFactoryPostProcessors(beanFactory);

                    // Register bean processors that intercept bean creation.
                    registerBeanPostProcessors(beanFactory);

                    // Initialize message source for this context.
                    initMessageSource();

                    // Initialize event multicaster for this context.
                    initApplicationEventMulticaster();

                    // Initialize other special beans in specific context subclasses.
                    onRefresh();

                    // Check for listener beans and register them.
                    registerListeners();

                    // Instantiate all remaining (non-lazy-init) singletons.
                    finishBeanFactoryInitialization(beanFactory);

                    // Last step: publish corresponding event.
                    finishRefresh();
                }

                catch (BeansException ex) {
                    // Destroy already created singletons to avoid dangling resources.
                    beanFactory.destroySingletons();

                    // Reset 'active' flag.
                    cancelRefresh(ex);

                    // Propagate exception to caller.
                    throw ex;
                }
            }
        }
    }

![](https://images0.cnblogs.com/blog/665504/201409/171550398311522.jpg)

     如上代码加上图片就是整个Bean的初始化过程。我们知道Bean有可以配置单列以及懒加载形式。在初始化的过程中，
    我们也能很好的观察到这个过程的实现。

    在AbstractBeanFactory中定义了getBean()方法，而它又调用doGetBean().

#### GetBean  的大概过程：

    1. 先试着从单例缓存对象里获取。

    2. 从父容器里取定义，有则由父容器创建。

    3. 如果是单例，则走单例对象的创建过程：在 spring 容器里单例对象和非单例对象的创建过程是一样的。都会调用父 类 AbstractAutowireCapableBeanFactory 的 createBean 方法。 不同的是单例对象只创建一次并且需要缓 存起来。 DefaultListableBeanFactory 的父类 DefaultSingletonBeanRegistry 提供了对单例对 象缓存等支持工作。所以是单例对象的话会调用 DefaultSingletonBeanRegistry 的 getSingleton 方法，它会间 接调用AbstractAutowireCapableBeanFactory 的 createBean 方法。

    如果是 Prototype 多例则直接调用父类 AbstractAutowireCapableBeanFactory 的 createBean 方法。
