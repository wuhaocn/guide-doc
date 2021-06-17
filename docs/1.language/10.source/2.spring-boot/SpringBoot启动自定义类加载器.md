## SpringBoot 启动自定义类加载器

实际项目中可能面临 Jar 加密，动态加载类等情况，
假如项目采用 SpringBoot 就需要自定义类加载器

### 1.常用自定义类加载器思路

- 设置线程类加载器
  - 此种方法比较暴力，但是当发生线程切换是无法照顾到其他线程，会造成类加载处理异常
- 传入自定义类加载器
  - 提供接口传入自定义类加载器，这种比较靠谱

### 2.SpringBoot 自定义类加载器

- 采用线程方式
  - 如上文所述，会产生资源获取不到的情况，如下示例

```
采用：
(Thread.currentThread().setContextClassLoader(ClassUtils.getExtClassLoader());)

会出现：
Caused by: java.io.FileNotFoundException: class path resource [org/springframework/session/data/redis/config/annotation/web/http/RedisHttpSessionConfiguration.class] cannot be opened because it does not exist
	at org.springframework.core.io.ClassPathResource.getInputStream(ClassPathResource.java:180) ~[spring-core-5.1.5.RELEASE.jar:5.1.5.RELEASE]
	at org.springframework.core.type.classreading.SimpleMetadataReader.<init>(SimpleMetadataReader.java:51) ~[spring-core-5.1.5.RELEASE.jar:5.1.5.RELEASE]
	at org.springframework.core.type.classreading.SimpleMetadataReaderFactory.getMetadataReader(SimpleMetadataReaderFactory.java:103) ~[spring-core-5.1.5.RELEASE.jar:5.1.5.RELEASE]
	at org.springframework.boot.type.classreading.ConcurrentReferenceCachingMetadataReaderFactory.createMetadataReader(ConcurrentReferenceCachingMetadataReaderFactory.java:88) ~[spring-boot-2.1.3.RELEASE.jar:2.1.3.RELEASE]
	at org.springframework.boot.type.classreading.ConcurrentReferenceCachingMetadataReaderFactory.getMetadataReader(ConcurrentReferenceCachingMetadataReaderFactory.java:75) ~[spring-boot-2.1.3.RELEASE.jar:2.1.3.RELEASE]
	at org.springframework.core.type.classreading.SimpleMetadataReaderFactory.getMetadataReader(SimpleMetadataReaderFactory.java:81) ~[spring-core-5.1.5.RELEASE.jar:5.1.5.RELEASE]
	at org.springframework.context.annotation.ConfigurationClassParser.asSourceClass(ConfigurationClassParser.java:685) ~[spring-context-5.1.5.RELEASE.jar:5.1.5.RELEASE]
	at org.springframework.context.annotation.ConfigurationClassParser$SourceClass.getSuperClass(ConfigurationClassParser.java:998) ~[spring-context-5.1.5.RELEASE.jar:5.1.5.RELEASE]
	at org.springframework.context.annotation.ConfigurationClassParser.doProcessConfigurationClass(ConfigurationClassParser.java:332) ~[spring-context-5.1.5.RELEASE.jar:5.1.5.RELEASE]
	at org.springframework.context.annotation.ConfigurationClassParser.processConfigurationClass(ConfigurationClassParser.java:242) ~[spring-context-5.1.5.RELEASE.jar:5.1.5.RELEASE]
	at org.springframework.context.annotation.ConfigurationClassParser.processMemberClasses(ConfigurationClassParser.java:361) ~[spring-context-5.1.5.RELEASE.jar:5.1.5.RELEASE]
	at org.springframework.context.annotation.ConfigurationClassParser.doProcessConfigurationClass(ConfigurationClassParser.java:263) ~[spring-context-5.1.5.RELEASE.jar:5.1.5.RELEASE]
	at org.springframework.context.annotation.ConfigurationClassParser.processConfigurationClass(ConfigurationClassParser.java:242) ~[spring-context-5.1.5.RELEASE.jar:5.1.5.RELEASE]
	at org.springframework.context.annotation.ConfigurationClassParser.processImports(ConfigurationClassParser.java:589) ~[spring-context-5.1.5.RELEASE.jar:5.1.5.RELEASE]
```

- 传入自定义类加载器

  - 这个使用 ResourceLoader 进行传入，下文进行介绍

### 3.自定义 ResourceLoader 类加载器

```
Properties configProperties = new Properties();
configProperties.load(config);
SpringApplication springApplication = new SpringApplication(ServerApiApplication.class);
springApplication.setDefaultProperties(configProperties);
//创建自定义ResourceLoader
DefaultResourceLoader defaultResourceLoader = new DefaultResourceLoader();
if (ClassUtils.getExtClassLoader() != null) {
    defaultResourceLoader.setClassLoader(ClassUtils.getExtClassLoader());
}
//设置自定义ResourceLoader
springApplication.setResourceLoader(defaultResourceLoader);
springApplication.run(ServerApiApplication.class);
```
