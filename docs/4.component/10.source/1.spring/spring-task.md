<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->

**Table of Contents** _generated with [DocToc](https://github.com/thlorenz/doctoc)_

- [开头](#%E5%BC%80%E5%A4%B4)
- [定时器](#%E5%AE%9A%E6%97%B6%E5%99%A8)
  - [用法](#%E7%94%A8%E6%B3%95)
  - [解析](#%E8%A7%A3%E6%9E%90)
    - [注册](#%E6%B3%A8%E5%86%8C)
    - [scheduler](#scheduler)
    - [scheduled-tasks](#scheduled-tasks)
  - [调度执行](#%E8%B0%83%E5%BA%A6%E6%89%A7%E8%A1%8C)
    - [scheduler 初始化](#scheduler%E5%88%9D%E5%A7%8B%E5%8C%96)
    - [任务调度](#%E4%BB%BB%E5%8A%A1%E8%B0%83%E5%BA%A6)
      - [Trigger](#trigger)
      - [Cron 解析](#cron%E8%A7%A3%E6%9E%90)
      - [调度](#%E8%B0%83%E5%BA%A6)
  - [总结](#%E6%80%BB%E7%BB%93)
- [异步执行](#%E5%BC%82%E6%AD%A5%E6%89%A7%E8%A1%8C)
  - [配置](#%E9%85%8D%E7%BD%AE)
  - [原理](#%E5%8E%9F%E7%90%86)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

# 开头

从功能上来说，spring-task 这个组件主要包括了两个/两种功能:

- 任务的定时调度/执行，对应 xml 配置的 task:scheduler 和 task:scheduled-tasks 标签。
- 方法异步执行，对应 xml 配置的 task:executor 标签。

task:annotation-driven 标签被以上两种功能共有。下面就这两种功能分别进行说明。

# 定时器

## 用法

以 XML 作为示例，基于注解的也是一样的。

```xml
<task:scheduler id="scheduler" pool-size="3" />
<bean id="task" class="task.Task"/>
<task:scheduled-tasks scheduler="scheduler">
    <task:scheduled ref="task" method="print" cron="0/5 * * * * ?"/>
</task:scheduled-tasks>
```

定义了一个定时任务，每隔 5 秒执行 Task 的 print 方法，Task:

```java
public class Task {
    public void print() {
        System.out.println("print执行");
    }
}
```

关于 cron 表达式可以参考:

[深入浅出 Spring task 定时任务](http://blog.csdn.net/u011116672/article/details/52517247)

## 解析

### 注册

此部分的解析器注册由 TaskNamespaceHandler 完成:

```java
@Override
public void init() {
    this.registerBeanDefinitionParser("annotation-driven", new AnnotationDrivenBeanDefinitionParser());
    this.registerBeanDefinitionParser("executor", new ExecutorBeanDefinitionParser());
    this.registerBeanDefinitionParser("scheduled-tasks", new ScheduledTasksBeanDefinitionParser());
    this.registerBeanDefinitionParser("scheduler", new SchedulerBeanDefinitionParser());
}
```

### scheduler

SchedulerBeanDefinitionParser 源码:

```java
@Override
protected String getBeanClassName(Element element) {
    return "org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler";
}

@Override
protected void doParse(Element element, BeanDefinitionBuilder builder) {
    String poolSize = element.getAttribute("pool-size");
    if (StringUtils.hasText(poolSize)) {
        builder.addPropertyValue("poolSize", poolSize);
    }
}
```

由于 SchedulerBeanDefinitionParser 是 AbstractSingleBeanDefinitionParser 的子类，所以 Spring 将 task:scheduler 标签解析为一个 BeanDefinition。其 beanClass 为 org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler。

### scheduled-tasks

其解析的源码较长，在此不再贴出，解析之后形成的 BeanDefinition 结构如下图:

![scheduled-tasks结构图](images/scheduled-tasks.png)

taskScheduler 属性即指向 task:scheduler 标签，如果没有配置，此属性不存在。

Spring 将每一个 task:scheduled 标签解析为一个 Task(的子类)，其类图如下:

![Task类图](images/Task.jpg)

很明显可以看出，任务的类型是由 cron, fixed-delay, fixed-rate, trigger 四个属性决定的，fixed-delay 和 fixed-rate 为 IntervalTask。

注意一点: **四种任务集合并不是互斥的**。比如说一个 task:scheduled 标签同时配置了 cron 和 trigger 属性，那么此标签会导致生成两个 beanClass 分别为 CronTask 何 TriggerTask 的 BeanDefinition 产生，并分别被放到 cronTasksList 和 triggerTasksList 中。

从图中可以看出，task:scheduled 的 method 和 ref 属性也被包装成了一个 BeanDefinition, 其 beanClass 为 org.springframework.scheduling.support.ScheduledMethodRunnable.

## 调度执行

入口便是 ContextLifecycleScheduledTaskRegistrar，类图:

![ContextLifecycleScheduledTaskRegistrar类图](images/ContextLifecycleScheduledTaskRegistrar.jpg)

ContextLifecycleScheduledTaskRegistrar 只实现了 afterSingletonsInstantiated 方法:

```java
@Override
public void afterSingletonsInstantiated() {
    scheduleTasks();
}
```

ScheduledTaskRegistrar.scheduleTasks:

```java
protected void scheduleTasks() {
     // shcheduler初始化
    if (this.taskScheduler == null) {
        this.localExecutor = Executors.newSingleThreadScheduledExecutor();
        this.taskScheduler = new ConcurrentTaskScheduler(this.localExecutor);
    }
    if (this.triggerTasks != null) {
        for (TriggerTask task : this.triggerTasks) {
            addScheduledTask(scheduleTriggerTask(task));
        }
    }
    if (this.cronTasks != null) {
        for (CronTask task : this.cronTasks) {
            addScheduledTask(scheduleCronTask(task));
        }
    }
    if (this.fixedRateTasks != null) {
        for (IntervalTask task : this.fixedRateTasks) {
            addScheduledTask(scheduleFixedRateTask(task));
        }
    }
    if (this.fixedDelayTasks != null) {
        for (IntervalTask task : this.fixedDelayTasks) {
            addScheduledTask(scheduleFixedDelayTask(task));
        }
    }
}
```

### scheduler 初始化

可见，如果没有配置 task:scheduler，那么在这里将会进行其初始化工作。

Spring 定义了 TaskScheduler 接口，独立于 jdk 之外，这样做的目的在于能够同时支持 JDK 和 quartz。对于默认来说，Spring 将真正的逻辑全部委托给 jdk 的 Executor。

TaskScheduler 类图:

![TaskScheduler类图](images/TaskScheduler.jpg)

ConcurrentTaskExecutor 来自另一个继承体系: TaskExecutor，这和 spring-task 的另一个重要功能，异步执行，这里暂且不表。

### 任务调度

以喜闻乐见的 CronTask 为例。ScheduledTaskRegistrar.scheduleCronTask:

```java
public ScheduledTask scheduleCronTask(CronTask task) {
    ScheduledTask scheduledTask = this.unresolvedTasks.remove(task);
    if (this.taskScheduler != null) {
        scheduledTask.future = this.taskScheduler.schedule(task.getRunnable(), task.getTrigger());
    }
    return (newTask ? scheduledTask : null);
}
```

#### Trigger

可见，Cron 也是通过 Trigger 实现的，在 Spring 中，Trigger 被定义为**决定一个任务的下一次执行时间**。其类图:

![Trigger.jpg](images/Trigger.jpg)

那么问题来了，字符串形式的 cron 表达式是在何时被解析为 Trigger 的呢?

#### Cron 解析

CronTask 构造器:

```java
public CronTask(Runnable runnable, String expression) {
    this(runnable, new CronTrigger(expression));
}
```

CronTrigger 构造器:

```java
public CronTrigger(String expression) {
    this.sequenceGenerator = new CronSequenceGenerator(expression);
}
```

答案便在 CronSequenceGenerator 构造器了:

```java
public CronSequenceGenerator(String expression) {
    this(expression, TimeZone.getDefault());
}

public CronSequenceGenerator(String expression, TimeZone timeZone) {
    this.expression = expression;
    this.timeZone = timeZone;
    parse(expression);
}
```

具体是如何解析的，不再深入。

ConcurrentTaskScheduler.schedule:

```java
@Override
public ScheduledFuture<?> schedule(Runnable task, Trigger trigger) {
    ErrorHandler errorHandler = (this.errorHandler != null ? this.errorHandler : 			 				TaskUtils.getDefaultErrorHandler(true));
    return new ReschedulingRunnable(task, trigger, this.scheduledExecutor, errorHandler).schedule();
}
```

#### 调度

从上面的源码可以看出，调度是通过 ReschedulingRunnable 来完成的，其类图:

![ReschedulingRunnable类图](images/ReschedulingRunnable.jpg)

schedule 方法:

```java
public ScheduledFuture<?> schedule() {
    synchronized (this.triggerContextMonitor) {
        this.scheduledExecutionTime = this.trigger.nextExecutionTime(this.triggerContext);
        if (this.scheduledExecutionTime == null) {
            return null;
        }
        long initialDelay = this.scheduledExecutionTime.getTime() - System.currentTimeMillis();
        this.currentFuture = this.executor.schedule(this, initialDelay, TimeUnit.MILLISECONDS);
        return this;
    }
}
```

可以看出，这里设置了在下一次执行窗口调用 this(ReschedulingRunnable)，从类图可以看出，ReschedulingRunnable 本身实现了 Runnable 接口，其 run 方法:

```java
@Override
public void run() {
    Date actualExecutionTime = new Date();
    super.run();
    Date completionTime = new Date();
    synchronized (this.triggerContextMonitor) {
        this.triggerContext.update(this.scheduledExecutionTime, actualExecutionTime, completionTime);
        if (!this.currentFuture.isCancelled()) {
             //下次调用
            schedule();
        }
    }
}
```

对我们自定义逻辑的调用是通过 super.run 实现的:

```java
@Override
public void run() {
    this.delegate.run();
}
```

delegate 便是前面提到过的 ScheduledMethodRunnable，其 run 方法:

```java
@Override
public void run() {
    ReflectionUtils.makeAccessible(this.method);
    this.method.invoke(this.target);
}
```

当然这只是针对 CronTask 的实现，而对于 IntervalTask 就要简单多了，ScheduledTaskRegistrar.scheduleFixedDelayTask 部分源码:

```java
public ScheduledTask scheduleFixedDelayTask(IntervalTask task) {
    if (this.taskScheduler != null) {
        if (task.getInitialDelay() > 0) {
            Date startTime = new Date(System.currentTimeMillis() + task.getInitialDelay());
            scheduledTask.future =
            this.taskScheduler.scheduleWithFixedDelay(task.getRunnable(),
                                                      startTime, task.getInterval());
        } else {
            scheduledTask.future =
                    this.taskScheduler.scheduleWithFixedDelay(task.getRunnable(), task.getInterval());
        }
    }
    return (newTask ? scheduledTask : null);
}
```

## 总结

从上面的说明可以看出，Spring 其实将核心逻辑委托给了 JDK 的 Executors.newSingleThreadScheduledExecutor()来实现，那么 JDK 是如何用一个线程来定时执行多个任务的呢?

# 异步执行

## 配置

必须以注解的方式进行配置，xml:

```xml
<task:executor id="executor" pool-size="3"/>
<task:annotation-driven executor="executor"/>
```

这样在类或方法上加上注解即可:

```java
@Async("executor")
public void print() {
    System.out.println("print执行");
}
```

## 原理

猜测:

Spring 会为带有@Async 的组件生成代理子类实现对原生组件的替换，代理子类将异步执行的方法包装为 Task(Runnable)提交到 jdk 的线程池即可。
