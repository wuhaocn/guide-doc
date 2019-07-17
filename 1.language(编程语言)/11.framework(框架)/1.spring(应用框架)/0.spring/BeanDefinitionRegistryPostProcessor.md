#### BeanDefinitionRegistryPostProcessor实现动态添加到spring容器

#### 1.定义一个bean

```
public class Person {

    private  String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                '}';
    }
}
```
#### 2.然后实现BeanDefinitionRegistryPostProcessor 

```
@Component
public class MyBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        for (int i = 0; i < 10; i++) {
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(Person.class);
            builder.addPropertyValue("name", "sfz_" + i);
            registry.registerBeanDefinition("person" + i, builder.getBeanDefinition());
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }
}
```
#### 3.这样就可以动态注入

```
public class App4 {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext("com.fz.song.four");
        context.getBeansOfType(Person.class).values().forEach(System.out::println);

        context.close();
    }
}
```
#### 4.第二种方式

```
public class App4 {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext("com.fz.song.four");
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(Person.class);
        builder.addPropertyValue("name","songfazhun");
        context.registerBeanDefinition("Person_app",builder.getBeanDefinition());

        context.getBeansOfType(Person.class).values().forEach(System.out::println);


        context.close();
    }
}
```
 

 

#### 5.顺便提一提BeanFactoryPostProcessor

```
@Component
public class MyBeanFactoryPostProcessor implements BeanFactoryPostProcessor {
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        int beanDefinitionCount = beanFactory.getBeanDefinitionCount();
        System.out.println("beanDefinitionCount = " + beanDefinitionCount);
    }
}
```
这个是在spring容器初始化的时候调用的


参考：
https://www.cnblogs.com/songfahzun/p/9236656.html