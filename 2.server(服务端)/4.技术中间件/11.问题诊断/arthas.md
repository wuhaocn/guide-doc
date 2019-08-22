### 1.Arthas 简介
    Arthas 是Alibaba开源的Java诊断工具，深受开发者喜爱。
    
    当你遇到以下类似问题而束手无策时，Arthas可以帮助你解决：
    
    这个类从哪个 jar 包加载的？为什么会报各种类相关的 Exception？
    我改的代码为什么没有执行到？难道是我没 commit？分支搞错了？
    遇到问题无法在线上 debug，难道只能通过加日志再重新发布吗？
    线上遇到某个用户的数据处理有问题，但线上同样无法 debug，线下无法重现！
    是否有一个全局视角来查看系统的运行状况？
    有什么办法可以监控到JVM的实时运行状态？
    Arthas支持JDK 6+，支持Linux/Mac/Winodws，采用命令行交互模式，同时提供丰富的 Tab 自动补全功能，进一步方便进行问题的定位和诊断。
    
### 2.官网参考：
    
    https://alibaba.github.io/arthas/
        
### 3.Arthas 命令

### 3.1.dashboard 
    当前系统的实时数据面板，按 ctrl+c 退出。
    当运行在Ali-tomcat时，会显示当前tomcat的实时信息，如HTTP请求的qps, rt, 错误数, 线程池信息等等。
    
    数据说明
    ID: Java级别的线程ID，注意这个ID不能跟jstack中的nativeID一一对应
    NAME: 线程名
    GROUP: 线程组名
    PRIORITY: 线程优先级, 1~10之间的数字，越大表示优先级越高
    STATE: 线程的状态
    CPU%: 线程消耗的cpu占比，采样100ms，将所有线程在这100ms内的cpu使用量求和，再算出每个线程的cpu使用占比。
    TIME: 线程运行总时间，数据格式为分：秒
    INTERRUPTED: 线程当前的中断位状态
    DAEMON: 是否是daemon线程

### 3.2.thread
    thead
    查看当前线程信息，查看线程的堆栈
    参数说明
    参数名称	参数说明
    id	线程id
    [n:]	指定最忙的前N个线程并打印堆栈
    [b]	找出当前阻塞其他线程的线程
    [i <value>]	指定cpu占比统计的采样间隔，单位为毫秒
 