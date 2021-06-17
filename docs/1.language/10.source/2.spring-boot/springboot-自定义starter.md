## springboot-自定义 starter

- 三步编写自定义 starter

### 1.自定义 AutoConfiguration

```
package com.rcloud.start.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * spring.factories 自动配置
 *
 * @author wuhao
 * @createTime 2021-05-20 13:49:00
 */
//@Configuration
public class CloudAutoConfiguration {
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	@Bean
	public CloudApplicationtProcessor cloudApplicationtProcessor(){
		LOGGER.info("CloudApplicationtProcessor Create:");
		return new CloudApplicationtProcessor();
	}
	@Bean
	public CloudBeanFactoryPostProcessor cloudBeanFactoryPostProcessor(){
		LOGGER.info("CloudBeanFactoryPostProcessor Create:");
		return new CloudBeanFactoryPostProcessor();
	}

	@Bean
	public CloudBeanPostProcessor cloudBeanPostProcessor(){
		LOGGER.info("CloudBeanPostProcessor Create:");
		return new CloudBeanPostProcessor();
	}

}
```

- 注意
  CloudAutoConfiguration 类的@Configuration 要去掉，当 springboot 包自动扫描到时候，会出现 bean 重复装载

### 2.配置文件装载

- spring.factories

```
org.springframework.boot.autoconfigure.EnableAutoConfiguration=com.rcloud.start.spring.CloudAutoConfiguration
```

### 3.打包自装载启动

- 采用 gradle/maven 打包
- 启动项目引入
- 在不指定包扫描的情况下 CloudBeanPostProcessor，CloudBeanFactoryPostProcessor，CloudApplicationtProcessor 可自动装载
