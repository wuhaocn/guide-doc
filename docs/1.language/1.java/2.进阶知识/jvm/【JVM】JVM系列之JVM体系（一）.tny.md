### 【JVM】JVM 系列之 JVM 体系（一）

<div id="cnblogs_post_body" class="blogpost-body ">
<p><strong>一、前言</strong></p>
<p><strong>　　</strong><span style="color: #ff0000;">为什么要学习了解Java虚拟机</span></p>
<p>　　1.我们需要更加清楚的了解Java底层是如何运作的，有利于我们更深刻的学习好Java。</p>
<p>　　2.对我们调试错误提供很宝贵的经验。</p>
<p>　　3.这是合格的Java程序必须要了解的内容。</p>
<p>　　基于此，笔者打算出一个Java虚拟机的系列，加深自己对知识点的理解，同时也方便各位有需要的园友。</p>
<p><strong>二、Java虚拟机的定义</strong></p>
<p>　　Java虚拟机（Java Virtual Machine），简称JVM。当我们说起Java虚拟机时，可能指的是如下三种不同的东西：</p>
<p>　　　　1.抽象规范。</p>
<p>　　　　2.一个具体的实现。</p>
<p>　　　　3.一个运行中的虚拟机实例。</p>
<p>　　Java虚拟机抽象规范仅仅是一个概念，在《The Java Virtual Machine Specification》中有详细的描述。该规范的实现，可能来自多个提供商，并存在于多个平台上，它或者是全部由软件实现，或者是以硬件和软件相结合的方式来实现。当运行一个Java程序的时候，也就在运行一个Java虚拟机实例。注意，我们所说的Java平台无关性是指class文件的平台无关性，JVM是和平台相关的，不同操作系统对应不同的JVM。</p>
<p><strong>三、Java虚拟机的总体框架图</strong></p>
<p>　　下图是整个Java虚拟机的总体框架图，之后我们会经常涉及到。<img src="https://images2015.cnblogs.com/blog/616953/201602/616953-20160227093822802-1529068009.png" alt=""></p>
<p><strong>四、Java虚拟机的体系结构</strong></p>
<p>　　下图表示了Java虚拟机的结构框图，主要描述了JVM子系统和内存区。</p>
<p>　　<img src="https://images2015.cnblogs.com/blog/616953/201602/616953-20160221214401030-1375432400.png" alt=""></p>
<p><strong>五、Java虚拟机各组成部分</strong></p>
<p>　　<span style="color: #ff0000;">5.1 类装载子系统</span></p>
<p>　　类装载子系统负责查找并装载类型，Java虚拟机由两种类装载器：启动类装载器（Java虚拟机实现的一部分）和用户自定义类装载器（Java程序的一部分）。类装载子系统负责定位和导入二进制class文件，并且保证导入类的正确性，为类变量分配并初始化内存，以及帮助解析符号引用。类装载器必须严格按照如下顺序进行类的装载。</p>
<p>　　1） 装载 -- 查找并装载类型的二进制数据</p>
<p>　　2） 连接 -- 执行验证，准备，以及解析（可选），连接分为如下三个步骤</p>
<p>　　　　验证 -- 确保被导入类型的正确性</p>
<p>　　　　准备 -- 为类变量分配内存，并将其初始化为默认值</p>
<p>　　　　解析 -- 把类型中的符号引用转换为直接引用</p>
<p>　　3） 初始化 -- 把类变量初始化为正确初始值</p>
<p>　　启动类装载器 -- Java虚拟机必须有一个启动类装载器，用于装载受信任的类，如Java API的class文件。</p>
<p>　　用户自定义类装载器 -- 继承自ClassLoader类，ClassLoader的如下四个方法，是通往Java虚拟机的通道。</p>
<p>　　　　1. protected final Class defineClass(String name, byte data[], int offset, int length);</p>
<p>　　　　2. protected final Class defineClass(String name, byte data[], int offset, int length, ProtectionDomain protectionDomain);</p>
<p>　　　　3. protected final Class findSystemClass(String name);</p>
<p>　　　　4. protected final void resolveClass(Class c);</p>
<p>　　这四个方法涉及到了装载和连接两个阶段，defineClass方法data参数为二进制Java Class文件格式，表示一个新的可用类型，之后把这个类型导入到方法区中。findSystemClass的参数为全限定名，通过类装载器进行装载。resolveClass参数为Class实例，完成连接初始化操作。</p>
<p>　　<span style="color: #ff0000;">5.2 方法区</span></p>
<p>　　方法区是线程共享的内存区域，用于存储已被虚拟机加载的类信息、常量、静态变量、即时编译器编译后的代码等数据，当方法区无法满足内存分配需求时，将抛出OutOfMemoryError。</p>
<p>　　类信息包括1.类型全限定名。2.类型的直接超类的全限定名（除非这个类型是java.lang.Object，它没有超类）。3.类型是类类型还是接口类型。4.类型的访问修饰符（public、abstract、final的某个子集）。5.任何直接超接口的全限定名的有序列表。6.类型的常量池。7.字段信息。8.方法信息。9.除了常量以外的所有类（静态）变量。10.一个到类ClassLoader的引用。11.一个到Class类的引用。</p>
<p>　　着重介绍常量池 -- 虚拟机必须要为每个被装载的类型维护一个常量池。常量池就是该类型所用常量的一个有序集合，包括直接常量和对其他类型、字段和方法的符号引用。它在Java程序的动态连接中起着核心作用。</p>
<p>　　<span style="color: #ff0000;">5.3 堆</span></p>
<p>　　一个虚拟机实例只对应一个堆空间，堆是线程共享的。堆空间是存放对象实例的地方，几乎所有对象实例都在这里分配。堆也是垃圾收集器管理的主要区域。堆可以处于物理上不连续的内存空间中，只要逻辑上相连就行,当堆中没有足够的内存进行对象实例分配时，并且堆也无法扩展时，会抛出OutOfMemoryError异常。</p>
<p>　　<span style="color: #ff0000;">5.4 程序计数器</span></p>
<p>　　每个线程拥有自己的程序计数器，线程之间的程序计数器互不影响。PC寄存器的内容总是下一条将被执行指令的"地址"，这里的"地址"可以是一个本地指针，也可以是在方法字节码中相对于该方法起始指令的偏移量。如果该线程正在执行一个本地方法，则程序计数器内容为undefined，此区域在Java虚拟机规范中没有规定任何OutOfMemoryError情况的区域。</p>
<p>　　<span style="color: #ff0000;">5.5 Java栈</span></p>
<p>　　Java栈也是线程私有的，虚拟机只会对栈进行两种操作，以帧为单位的入栈和出栈。每个方法在执行时都会创建一个帧，并入栈，成为当前帧。栈帧由三部分组成：局部变量区、操作数栈、帧数据区。</p>
<p>　　局部变量区被组织为一个以字长为单位、从0开始计数的数组。字节码指令通过从0开始的索引来使用其中的数据。类型为int、float、reference和return Address的值在数组中只占据一项，而类型为byte、short和char的值存入时都会转化为int类型，也占一项，而long、double则连续占据两项。</p>
<p>　　关于局部变量区给出如下一个例子。</p>
<div class="cnblogs_code" onclick="cnblogs_code_show('142b20be-f615-40d1-ad36-9d6521a35fbc')"><img id="code_img_closed_142b20be-f615-40d1-ad36-9d6521a35fbc" class="code_img_closed" src="https://images.cnblogs.com/OutliningIndicators/ContractedBlock.gif" alt=""><img id="code_img_opened_142b20be-f615-40d1-ad36-9d6521a35fbc" class="code_img_opened" style="display: none;" onclick="cnblogs_code_hide('142b20be-f615-40d1-ad36-9d6521a35fbc',event)" src="https://images.cnblogs.com/OutliningIndicators/ExpandedBlockStart.gif" alt="">
<div id="cnblogs_code_open_142b20be-f615-40d1-ad36-9d6521a35fbc" class="cnblogs_code_hide">
<pre><span style="color: #0000ff;">public</span> <span style="color: #0000ff;">class</span><span style="color: #000000;"> Example {
</span><span style="color: #0000ff;">public</span> <span style="color: #0000ff;">static</span> <span style="color: #0000ff;">int</span> classMethod(<span style="color: #0000ff;">int</span> i, <span style="color: #0000ff;">long</span> l, <span style="color: #0000ff;">float</span> f, <span style="color: #0000ff;">double</span> d, Object o, <span style="color: #0000ff;">byte</span><span style="color: #000000;"> b) {
</span><span style="color: #0000ff;">return</span> 0<span style="color: #000000;">;
}

</span><span style="color: #0000ff;">public</span> <span style="color: #0000ff;">int</span> instanceMethod(<span style="color: #0000ff;">char</span> c, <span style="color: #0000ff;">double</span> d, <span style="color: #0000ff;">short</span> s, <span style="color: #0000ff;">boolean</span><span style="color: #000000;"> b) {
</span><span style="color: #0000ff;">return</span> 0<span style="color ### 【JVM】JVM 系列之 JVM 体系（一）

<div id="cnblogs_post_body" class="blogpost-body ">
<p><strong>一、前言</strong></p>
<p><strong>　　</strong><span style="color: #ff0000;">为什么要学习了解Java虚拟机</span></p>
<p>　　1.我们需要更加清楚的了解Java底层是如何运作的，有利于我们更深刻的学习好Java。</p>
<p>　　2.对我们调试错误提供很宝贵的经验。</p>
<p>　　3.这是合格的Java程序必须要了解的内容。</p>
<p>　　基于此，笔者打算出一个Java虚拟机的系列，加深自己对知识点的理解，同时也方便各位有需要的园友。</p>
<p><strong>二、Java虚拟机的定义</strong></p>
<p>　　Java虚拟机（Java Virtual Machine），简称JVM。当我们说起Java虚拟机时，可能指的是如下三种不同的东西：</p>
<p>　　　　1.抽象规范。</p>
<p>　　　　2.一个具体的实现。</p>
<p>　　　　3.一个运行中的虚拟机实例。</p>
<p>　　Java虚拟机抽象规范仅仅是一个概念，在《The Java Virtual Machine Specification》中有详细的描述。该规范的实现，可能来自多个提供商，并存在于多个平台上，它或者是全部由软件实现，或者是以硬件和软件相结合的方式来实现。当运行一个Java程序的时候，也就在运行一个Java虚拟机实例。注意，我们所说的Java平台无关性是指class文件的平台无关性，JVM是和平台相关的，不同操作系统对应不同的JVM。</p>
<p><strong>三、Java虚拟机的总体框架图</strong></p>
<p>　　下图是整个Java虚拟机的总体框架图，之后我们会经常涉及到。<img src="https://images2015.cnblogs.com/blog/616953/201602/616953-20160227093822802-1529068009.png" alt=""></p>
<p><strong>四、Java虚拟机的体系结构</strong></p>
<p>　　下图表示了Java虚拟机的结构框图，主要描述了JVM子系统和内存区。</p>
<p>　　<img src="https://images2015.cnblogs.com/blog/616953/201602/616953-20160221214401030-1375432400.png" alt=""></p>
<p><strong>五、Java虚拟机各组成部分</strong></p>
<p>　　<span style="color: #ff0000;">5.1 类装载子系统</span></p>
<p>　　类装载子系统负责查找并装载类型，Java虚拟机由两种类装载器：启动类装载器（Java虚拟机实现的一部分）和用户自定义类装载器（Java程序的一部分）。类装载子系统负责定位和导入二进制class文件，并且保证导入类的正确性，为类变量分配并初始化内存，以及帮助解析符号引用。类装载器必须严格按照如下顺序进行类的装载。</p>
<p>　　1） 装载 -- 查找并装载类型的二进制数据</p>
<p>　　2） 连接 -- 执行验证，准备，以及解析（可选），连接分为如下三个步骤</p>
<p>　　　　验证 -- 确保被导入类型的正确性</p>
<p>　　　　准备 -- 为类变量分配内存，并将其初始化为默认值</p>
<p>　　　　解析 -- 把类型中的符号引用转换为直接引用</p>
<p>　　3） 初始化 -- 把类变量初始化为正确初始值</p>
<p>　　启动类装载器 -- Java虚拟机必须有一个启动类装载器，用于装载受信任的类，如Java API的class文件。</p>
<p>　　用户自定义类装载器 -- 继承自ClassLoader类，ClassLoader的如下四个方法，是通往Java虚拟机的通道。</p>
<p>　　　　1. protected final Class defineClass(String name, byte data[], int offset, int length);</p>
<p>　　　　2. protected final Class defineClass(String name, byte data[], int offset, int length, ProtectionDomain protectionDomain);</p>
<p>　　　　3. protected final Class findSystemClass(String name);</p>
<p>　　　　4. protected final void resolveClass(Class c);</p>
<p>　　这四个方法涉及到了装载和连接两个阶段，defineClass方法data参数为二进制Java Class文件格式，表示一个新的可用类型，之后把这个类型导入到方法区中。findSystemClass的参数为全限定名，通过类装载器进行装载。resolveClass参数为Class实例，完成连接初始化操作。</p>
<p>　　<span style="color: #ff0000;">5.2 方法区</span></p>
<p>　　方法区是线程共享的内存区域，用于存储已被虚拟机加载的类信息、常量、静态变量、即时编译器编译后的代码等数据，当方法区无法满足内存分配需求时，将抛出OutOfMemoryError。</p>
<p>　　类信息包括1.类型全限定名。2.类型的直接超类的全限定名（除非这个类型是java.lang.Object，它没有超类）。3.类型是类类型还是接口类型。4.类型的访问修饰符（public、abstract、final的某个子集）。5.任何直接超接口的全限定名的有序列表。6.类型的常量池。7.字段信息。8.方法信息。9.除了常量以外的所有类（静态）变量。10.一个到类ClassLoader的引用。11.一个到Class类的引用。</p>
<p>　　着重介绍常量池 -- 虚拟机必须要为每个被装载的类型维护一个常量池。常量池就是该类型所用常量的一个有序集合，包括直接常量和对其他类型、字段和方法的符号引用。它在Java程序的动态连接中起着核心作用。</p>
<p>　　<span style="color: #ff0000;">5.3 堆</span></p>
<p>　　一个虚拟机实例只对应一个堆空间，堆是线程共享的。堆空间是存放对象实例的地方，几乎所有对象实例都在这里分配。堆也是垃圾收集器管理的主要区域。堆可以处于物理上不连续的内存空间中，只要逻辑上相连就行,当堆中没有足够的内存进行对象实例分配时，并且堆也无法扩展时，会抛出OutOfMemoryError异常。</p>
<p>　　<span style="color: #ff0000;">5.4 程序计数器</span></p>
<p>　　每个线程拥有自己的程序计数器，线程之间的程序计数器互不影响。PC寄存器的内容总是下一条将被执行指令的"地址"，这里的"地址"可以是一个本地指针，也可以是在方法字节码中相对于该方法起始指令的偏移量。如果该线程正在执行一个本地方法，则程序计数器内容为undefined，此区域在Java虚拟机规范中没有规定任何OutOfMemoryError情况的区域。</p>
<p>　　<span style="color: #ff0000;">5.5 Java栈</span></p>
<p>　　Java栈也是线程私有的，虚拟机只会对栈进行两种操作，以帧为单位的入栈和出栈。每个方法在执行时都会创建一个帧，并入栈，成为当前帧。栈帧由三部分组成：局部变量区、操作数栈、帧数据区。</p>
<p>　　局部变量区被组织为一个以字长为单位、从0开始计数的数组。字节码指令通过从0开始的索引来使用其中的数据。类型为int、float、reference和return Address的值在数组中只占据一项，而类型为byte、short和char的值存入时都会转化为int类型，也占一项，而long、double则连续占据两项。</p>
<p>　　关于局部变量区给出如下一个例子。</p>
<div class="cnblogs_code" onclick="cnblogs_code_show('142b20be-f615-40d1-ad36-9d6521a35fbc')"><img id="code_img_closed_142b20be-f615-40d1-ad36-9d6521a35fbc" class="code_img_closed" src="https://images.cnblogs.com/OutliningIndicators/ContractedBlock.gif" alt=""><img id="code_img_opened_142b20be-f615-40d1-ad36-9d6521a35fbc" class="code_img_opened" style="display: none;" onclick="cnblogs_code_hide('142b20be-f615-40d1-ad36-9d6521a35fbc',event)" src="https://images.cnblogs.com/OutliningIndicators/ExpandedBlockStart.gif" alt="">
<div id="cnblogs_code_open_142b20be-f615-40d1-ad36-9d6521a35fbc" class="cnblogs_code_hide">
<pre><span style="color: #0000ff;">public</span> <span style="color: #0000ff;">class</span><span style="color: #000000;"> Example {
</span><span style="color: #0000ff;">public</span> <span style="color: #0000ff;">static</span> <span style="color: #0000ff;">int</span> classMethod(<span style="color: #0000ff;">int</span> i, <span style="color: #0000ff;">long</span> l, <span style="color: #0000ff;">float</span> f, <span style="color: #0000ff;">double</span> d, Object o, <span style="color: #0000ff;">byte</span><span style="color: #000000;"> b) {
</span><span style="color: #0000ff;">return</span> 0<span style="color: #000000;">;
}

</span><span style="color: #0000ff;">public</span> <span style="color: #0000ff;">int</span> instanceMethod(<span style="color: #0000ff;">char</span> c, <span style="color: #0000ff;">double</span> d, <span style="color: #0000ff;">short</span> s, <span style="color: #0000ff;">boolean</span><span style="color: #000000;"> b) {
</span><span style="color: #0000ff;">return</span> 0<span style="color: #000000;">;
}
}</span></pre>

</div>
<span class="cnblogs_code_collapse">View Code</span></div>
<p>　　<img src="https://images2015.cnblogs.com/blog/616953/201602/616953-20160221210455436-1738098020.png" alt=""></p>
<p>　　可以看到类方法的首项中没有隐含的this指针，而对象方法则会隐含this指针。并且byte,char,short,boolean类型存入局部变量区的时候都会被转化成int类型值，当被存回堆或者方法区时，才会转化回原来的类型。</p>
<p>　　操作数栈被组织成一个以字长为单位的数组，它是通过标准的栈操作-入栈和出栈来进行访问，而不是通过索引访问。入栈和出栈也会存在类型的转化。</p>
<p>　　栈数据区存放一些用于支持常量池解析、正常方法返回以及异常派发机制的信息。即将常量池的符号引用转化为直接地址引用、恢复发起调用的方法的帧进行正常返回，发生异常时转交异常表进行处理。</p>
<p>　　<span style="color: #ff0000;">5.6 本地方法栈</span></p>
<p>　　访问本地方式时使用到的栈，为本地方法服务，本地方法区域也会抛出StackOverflowError和OutOfMemoryError异常。</p>
<p>　　<span style="color: #ff0000;">5.7 执行引擎</span></p>
<p>　　用户所编写的程序如何表现正确的行为需要执行引擎的支持，执行引擎执行字节码指令，完成程序的功能。后面会详细介绍。</p>
<p>　　<span style="color: #ff0000;">5.8 本地方法接口</span></p>
<p>　　本地方法接口称为JNI，是为可移植性准备的。</p>
<p><strong>六、总结</strong></p>
<p>　　至此，虚拟机的结构就已经大体介绍完了，现在我们只需要有一个初步的了解，后面对各个部分会有更加详细的介绍。谢谢各位园友观看~</p>
<p><a style="height: 20px; width: 40px; position: absolute; opacity: 0.85; z-index: 8675309; display: none; cursor: pointer; background-color: transparent; background-image: url('data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACgAAAAUCAYAAAD/Rn+7AAADU0lEQVR42s2WXUhTYRjHz0VEVPRFUGmtVEaFUZFhHxBhsotCU5JwBWEf1EWEEVHQx4UfFWYkFa2biPJiXbUta33OXFtuUXMzJ4bK3Nqay7m5NeZq6h/tPQ+xU20zugjOxR/+7/O8539+5znnwMtNTExwJtMb3L/fiLv3botCSmUjeCaejTOb39AiFothfHxcFIrHY8RksZjBsckJcOIRMfFsHD/SsbExUYpnI8DR0dGUGjSb0byhEJp5Uqg5CTSzc2CQleJbMEj9/ywBcGRkJEk9DQqouEVQT1sK444yWI9UonmTjGqauVLEIlHa9x8lAMbj8SSpp0rwKGMVvg8P46vbg0C7na8z8JsMcgHe7jlEa+edRhiLy8n/TUMfu6EvLElk+U0WtGwrTrdfAGQf5J8iiK4LVzDU28t8JtMSocf8E+l68myaNFXm/6rXslLK7ay5TOunuRvZWpJuvwAYjUaTpOIWoquuAZ219RTaxKYp9BbjycoN5FvL9qH9TBX5rvoGdJythvXYSTxdtRnWylO/ZdqrLsGwszzhWQ593z2KlAwCYCQSSZJ6ehZ0W7bD9VBLgN0NCqr3qR7R2rBrL3pu3Sb/7nDlz2uy6cG0OXk0GTbZXzNp8trsPAQdTj6frlWzN2DcXZGKQQAMh8NJ6rpyHe+PnkCr/CAFdZyvpfpjuvkifLF9wIt1Wwlo0OHie1RvWrKa93RjzfzliTzPKz3ltB0/Tevmwp14wGUgHAzSOoUEwFAolFaaBSuhnslPRkJexUJtZ6v5HtUeLswl33n1BgEY5fvhs9sJ3FAiT+QYyyvoAQJuD0KBAFRTJNAuz5/s3gJgMBhMJwrVFRThM5tY5zUF/A4X1f2fvQTRLCuBreoim0YmAbqNJryvPEXeeq46kaNdkQ/1HCncbJKPs9ZSv2VHGfWsZ2hfkhKAfr8/pdxWKx4wwD69PmVfNSOL+lr2w+gYqHpWDtXt1xQ8AMlWU0e1lqLd/APRHoP8AJqWrQG9gYxcPMsvSJUvAA4MDKTUJ7MZLaVy8v+qT21tcDx/OemePr0RTkNrur4A6PP5xCgBsL+/X4wiQDpuuVxOeL1eMYmYeDY6sOp0z+B0OuHxeEQhxkJMFosJiSO/UinOI/8Pc+l7KKArAT8AAAAASUVORK5CYII='); top: 1955px; left: 46px;" href="http://www.pinterest.com/pin/create/extension/">　　　　&nbsp;&nbsp; </a></p>
</div>

### 参考

https://www.cnblogs.com/leesf456/p/5204694.html: #000000;">;
}
}</span></pre>

</div>
<span class="cnblogs_code_collapse">View Code</span></div>
<p>　　<img src="https://images2015.cnblogs.com/blog/616953/201602/616953-20160221210455436-1738098020.png" alt=""></p>
<p>　　可以看到类方法的首项中没有隐含的this指针，而对象方法则会隐含this指针。并且byte,char,short,boolean类型存入局部变量区的时候都会被转化成int类型值，当被存回堆或者方法区时，才会转化回原来的类型。</p>
<p>　　操作数栈被组织成一个以字长为单位的数组，它是通过标准的栈操作-入栈和出栈来进行访问，而不是通过索引访问。入栈和出栈也会存在类型的转化。</p>
<p>　　栈数据区存放一些用于支持常量池解析、正常方法返回以及异常派发机制的信息。即将常量池的符号引用转化为直接地址引用、恢复发起调用的方法的帧进行正常返回，发生异常时转交异常表进行处理。</p>
<p>　　<span style="color: #ff0000;">5.6 本地方法栈</span></p>
<p>　　访问本地方式时使用到的栈，为本地方法服务，本地方法区域也会抛出StackOverflowError和OutOfMemoryError异常。</p>
<p>　　<span style="color: #ff0000;">5.7 执行引擎</span></p>
<p>　　用户所编写的程序如何表现正确的行为需要执行引擎的支持，执行引擎执行字节码指令，完成程序的功能。后面会详细介绍。</p>
<p>　　<span style="color: #ff0000;">5.8 本地方法接口</span></p>
<p>　　本地方法接口称为JNI，是为可移植性准备的。</p>
<p><strong>六、总结</strong></p>
<p>　　至此，虚拟机的结构就已经大体介绍完了，现在我们只需要有一个初步的了解，后面对各个部分会有更加详细的介绍。谢谢各位园友观看~</p>
<p><a style="height: 20px; width: 40px; position: absolute; opacity: 0.85; z-index: 8675309; display: none; cursor: pointer; background-color: transparent; background-image: url('data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACgAAAAUCAYAAAD/Rn+7AAADU0lEQVR42s2WXUhTYRjHz0VEVPRFUGmtVEaFUZFhHxBhsotCU5JwBWEf1EWEEVHQx4UfFWYkFa2biPJiXbUta33OXFtuUXMzJ4bK3Nqay7m5NeZq6h/tPQ+xU20zugjOxR/+7/O8539+5znnwMtNTExwJtMb3L/fiLv3botCSmUjeCaejTOb39AiFothfHxcFIrHY8RksZjBsckJcOIRMfFsHD/SsbExUYpnI8DR0dGUGjSb0byhEJp5Uqg5CTSzc2CQleJbMEj9/ywBcGRkJEk9DQqouEVQT1sK444yWI9UonmTjGqauVLEIlHa9x8lAMbj8SSpp0rwKGMVvg8P46vbg0C7na8z8JsMcgHe7jlEa+edRhiLy8n/TUMfu6EvLElk+U0WtGwrTrdfAGQf5J8iiK4LVzDU28t8JtMSocf8E+l68myaNFXm/6rXslLK7ay5TOunuRvZWpJuvwAYjUaTpOIWoquuAZ219RTaxKYp9BbjycoN5FvL9qH9TBX5rvoGdJythvXYSTxdtRnWylO/ZdqrLsGwszzhWQ593z2KlAwCYCQSSZJ6ehZ0W7bD9VBLgN0NCqr3qR7R2rBrL3pu3Sb/7nDlz2uy6cG0OXk0GTbZXzNp8trsPAQdTj6frlWzN2DcXZGKQQAMh8NJ6rpyHe+PnkCr/CAFdZyvpfpjuvkifLF9wIt1Wwlo0OHie1RvWrKa93RjzfzliTzPKz3ltB0/Tevmwp14wGUgHAzSOoUEwFAolFaaBSuhnslPRkJexUJtZ6v5HtUeLswl33n1BgEY5fvhs9sJ3FAiT+QYyyvoAQJuD0KBAFRTJNAuz5/s3gJgMBhMJwrVFRThM5tY5zUF/A4X1f2fvQTRLCuBreoim0YmAbqNJryvPEXeeq46kaNdkQ/1HCncbJKPs9ZSv2VHGfWsZ2hfkhKAfr8/pdxWKx4wwD69PmVfNSOL+lr2w+gYqHpWDtXt1xQ8AMlWU0e1lqLd/APRHoP8AJqWrQG9gYxcPMsvSJUvAA4MDKTUJ7MZLaVy8v+qT21tcDx/OemePr0RTkNrur4A6PP5xCgBsL+/X4wiQDpuuVxOeL1eMYmYeDY6sOp0z+B0OuHxeEQhxkJMFosJiSO/UinOI/8Pc+l7KKArAT8AAAAASUVORK5CYII='); top: 1955px; left: 46px;" href="http://www.pinterest.com/pin/create/extension/">　　　　&nbsp;&nbsp; </a></p>
</div>

### 参考

https://www.cnblogs.com/leesf456/p/5204694.html
