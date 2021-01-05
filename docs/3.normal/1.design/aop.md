<div id="mainContent">
<div class="forFlow">
<div id="post_detail">
<!--done-->
<div id="topics">
<div class="post">
<h1 class="postTitle">

<a id="cb_post_title_url" class="postTitle2" href="https://www.cnblogs.com/Fantastic-Code/p/11593544.html">AOP 详解</a>

</h1>
<div class="clear"></div>
<div class="postBody">

<div id="cnblogs_post_body" class="blogpost-body ">
<h3>原文：https://www.cnblogs.com/hellxz/p/9629012.html</h3>
<h3>注意：在我们写ssm或者spring框架等等项目时，一旦这个mapper的相关处理不能满足现实的需求(比如要增强CRUD的时候，在本身对象实体不能满足需求，就要重新构建出新的组合实体来满足需求)，就需要增加一个service层来控制CRUD逻辑，这个时候就需要在spring的xml文件中添加AOP的切点和织入。</h3>
<p><img src="https://img2018.cnblogs.com/blog/1324938/201910/1324938-20191010112627443-838407091.png" alt=""></p>
<p><span style="font-size: 16px;">图片中声明切面中的id就是织入的pointcut-ref属性的值，织入的属性值就是AOP配置事务的id,如下图：</span><img src="https://img2018.cnblogs.com/blog/1324938/201910/1324938-20191010113536426-957139838.png" alt=""></p>
<p>&nbsp;</p>
<h3>&nbsp;在此之前要进行如下操作：</h3>
<p><img src="https://img2018.cnblogs.com/blog/1324938/201910/1324938-20191010113733217-1942287832.png" alt=""></p>
<h3 id="spring-aop基本概念">Spring AOP基本概念</h3>
<ol>
<li>是一种动态编译期增强性AOP的实现</li>
<li>与IOC进行整合，不是全面的切面框架</li>
<li>与动态代理相辅相成</li>
<li>有两种实现：基于jdk动态代理、cglib</li>
</ol>
<h3 id="spring-aop与aspectj区别">Spring AOP与AspectJ区别</h3>
<ol>
<li>Spring的AOP是基于动态代理的，动态增强目标对象，而AspectJ是静态编译时增强，需要使用自己的编译器来编译，还需要织入器</li>
<li>使用AspectJ编写的java代码无法直接使用javac编译，必须使用AspectJ增强的ajc增强编译器才可以通过编译，写法不符合原生Java的语法；而Spring AOP是符合Java语法的，也不需要指定编译器去编译，一切都由Spring 处理。</li>
</ol>
<h3 id="jdk动态代理与cglib的区别">JDK动态代理与Cglib的区别</h3>
<ol>
<li>jdk的动态代理需要实现接口 InvocationHandler</li>
<li>cglib无需实现接口，使用字节码技术去修改class文件使继承</li>
<li>spring默认使用jdk动态代理，如果没有实现接口会使用cglib</li>
</ol>
<h3 id="使用步骤">使用步骤</h3>
<ol>
<li>定义业务组件</li>
<li>定义切点（重点）</li>
<li>定义增强处理方法(切面方法)</li>
</ol>
<h3 id="依赖">依赖</h3>
<p>jar包依赖，除此以外还有spring依赖</p>
<ol>
<li>aspectjweaver.jar</li>
<li>aspectjrt.jar</li>
<li>aspectj.jar</li>
<li>aopalliance.jar</li>
</ol>
<p>maven依赖</p>
<pre class="xml"><code class="hljs">    <span class="hljs-tag"><span class="hljs-tag">&lt;</span><span class="hljs-name"><span class="hljs-tag"><span class="hljs-name">dependencies</span>&gt;</span>
<span class="hljs-comment"><span class="hljs-comment">&lt;!-- 有此依赖会远程下载其它相关依赖 --&gt;</span>
<span class="hljs-tag"><span class="hljs-tag">&lt;</span><span class="hljs-name"><span class="hljs-tag"><span class="hljs-name">dependency</span>&gt;</span>
<span class="hljs-tag"><span class="hljs-tag">&lt;</span><span class="hljs-name"><span class="hljs-tag"><span class="hljs-name">groupId</span>&gt;</span>org.springframework<span class="hljs-tag"><span class="hljs-tag">&lt;/</span><span class="hljs-name"><span class="hljs-tag"><span class="hljs-name">groupId</span>&gt;</span>
<span class="hljs-tag"><span class="hljs-tag">&lt;</span><span class="hljs-name"><span class="hljs-tag"><span class="hljs-name">artifactId</span>&gt;</span>spring-context<span class="hljs-tag"><span class="hljs-tag">&lt;/</span><span class="hljs-name"><span class="hljs-tag"><span class="hljs-name">artifactId</span>&gt;</span>
<span class="hljs-tag"><span class="hljs-tag">&lt;</span><span class="hljs-name"><span class="hljs-tag"><span class="hljs-name">version</span>&gt;</span>4.2.9.RELEASE<span class="hljs-tag"><span class="hljs-tag">&lt;/</span><span class="hljs-name"><span class="hljs-tag"><span class="hljs-name">version</span>&gt;</span>
<span class="hljs-tag"><span class="hljs-tag">&lt;/</span><span class="hljs-name"><span class="hljs-tag"><span class="hljs-name">dependency</span>&gt;</span>
<span class="hljs-comment"><span class="hljs-comment">&lt;!-- aspectJ AOP 织入器 --&gt;</span>
<span class="hljs-tag"><span class="hljs-tag">&lt;</span><span class="hljs-name"><span class="hljs-tag"><span class="hljs-name">dependency</span>&gt;</span>
<span class="hljs-tag"><span class="hljs-tag">&lt;</span><span class="hljs-name"><span class="hljs-tag"><span class="hljs-name">groupId</span>&gt;</span>org.aspectj<span class="hljs-tag"><span class="hljs-tag">&lt;/</span><span class="hljs-name"><span class="hljs-tag"><span class="hljs-name">groupId</span>&gt;</span>
<span class="hljs-tag"><span class="hljs-tag">&lt;</span><span class="hljs-name"><span class="hljs-tag"><span class="hljs-name">artifactId</span>&gt;</span>aspectjweaver<span class="hljs-tag"><span class="hljs-tag">&lt;/</span><span class="hljs-name"><span class="hljs-tag"><span class="hljs-name">artifactId</span>&gt;</span>
<span class="hljs-tag"><span class="hljs-tag">&lt;</span><span class="hljs-name"><span class="hljs-tag"><span class="hljs-name">version</span>&gt;</span>1.8.9<span class="hljs-tag"><span class="hljs-tag">&lt;/</span><span class="hljs-name"><span class="hljs-tag"><span class="hljs-name">version</span>&gt;</span>
<span class="hljs-tag"><span class="hljs-tag">&lt;/</span><span class="hljs-name"><span class="hljs-tag"><span class="hljs-name">dependency</span>&gt;</span>

<span class="hljs-tag"><span class="hljs-tag">&lt;/</span><span class="hljs-name"><span class="hljs-tag"><span class="hljs-name">dependencies</span>&gt;</span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></code></pre>

<h3 id="注解方式开发">注解方式开发</h3>
<ol>
<li>扫描Aspect增强的类</li>
</ol>
<pre class="xml"><code class="hljs"><span class="hljs-tag"><span class="hljs-tag">&lt;</span><span class="hljs-name"><span class="hljs-tag"><span class="hljs-name">context:component-scan</span> </span><span class="hljs-attr"><span class="hljs-tag"><span class="hljs-attr">base-package</span>=</span><span class="hljs-string"><span class="hljs-tag"><span class="hljs-string">""</span>&gt;</span>
<span class="hljs-tag"><span class="hljs-tag">&lt;</span><span class="hljs-name"><span class="hljs-tag"><span class="hljs-name">context:include-filter</span> </span><span class="hljs-attr"><span class="hljs-tag"><span class="hljs-attr">type</span>=</span><span class="hljs-string"><span class="hljs-tag"><span class="hljs-string">"annotation"</span>
</span><span class="hljs-attr"><span class="hljs-tag"><span class="hljs-attr">expression</span>=</span><span class="hljs-string"><span class="hljs-tag"><span class="hljs-string">"org.aspectj.lang.annotation.Aspect"</span>/&gt;</span>
<span class="hljs-tag"><span class="hljs-tag">&lt;/</span><span class="hljs-name"><span class="hljs-tag"><span class="hljs-name">context:component-scan</span>&gt;</span></span></span></span></span></span></span></span></span></span></span></span></span></code></pre>
<ol>
<li>开启@AspectJ支持&nbsp;<code>&lt;aop:aspectj-autoproxy/&gt;</code></li>
<li>使用@AspectJ注解来标记一个切面类（spring不会将切面注册为Bean也不会增强，但是需要扫描）</li>
<li>使用其它注解进行开发（如下）</li>
</ol>
<h3 id="常用注解的使用">常用注解的使用</h3>
<ol>
<li>@Before：在切点方法前执行
<ul>
<li>在增强的方法上<code>@Before("execution(* 包名.*.*(..))")</code></li>
<li>上述表达式可使用pointcut或切入表达式，效果一致,之后不再赘述</li>
<li>切点方法没有形参与返回值</li>
</ul>
</li>
</ol>
<p>示例代码</p>
<pre class="java"><code class="hljs"><span class="hljs-meta"><span class="hljs-meta">@Aspect</span>
<span class="hljs-keyword"><span class="hljs-keyword">public</span> <span class="hljs-class"><span class="hljs-keyword"><span class="hljs-class"><span class="hljs-keyword">class</span> </span><span class="hljs-title"><span class="hljs-class"><span class="hljs-title">AuthAspect</span> </span>{

<span class="hljs-comment"><span class="hljs-comment">//定义切点</span>
<span class="hljs-meta"><span class="hljs-meta">@Pointcut</span>(<span class="hljs-string"><span class="hljs-string">"execution(_ com.cnblogs.hellxz.service._.\*(..))"</span>)
<span class="hljs-function"><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-keyword">public</span> </span><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-keyword">void</span> </span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">pointCut</span></span><span class="hljs-params"><span class="hljs-function"><span class="hljs-params">()</span> </span>{}

<span class="hljs-comment"><span class="hljs-comment">//前置处理</span>
<span class="hljs-meta"><span class="hljs-meta">@Before</span>(<span class="hljs-string"><span class="hljs-string">"pointCut()"</span>)
<span class="hljs-function"><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-keyword">public</span> </span><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-keyword">void</span> </span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">auth</span></span><span class="hljs-params"><span class="hljs-function"><span class="hljs-params">()</span> </span>{
System.out.println(<span class="hljs-string"><span class="hljs-string">"模拟权限检查……"</span>);
}
}
</span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></code></pre>

<ol>
<li>@After：在切点方法后执行
<ul>
<li>用法同@Before</li>
</ul>
</li>
<li>@Around：在切点方法外环绕执行
<ul>
<li>在增强的方法上<code>@Around("execution(* 包名.*(..))")</code>或使用切点<code>@Around("pointcut()")</code></li>
<li>接收参数类型为<code>ProceedingJoinPoint</code>，必须有这个参数在切面方法的入参第一位</li>
<li>返回值为Object</li>
<li>需要执行ProceedingJoinPoint对象的proceed方法，在这个方法前与后面做环绕处理，可以决定何时执行与完全阻止方法的执行</li>
<li>返回proceed方法的返回值</li>
<li>@Around相当于@Before和@AfterReturning功能的总和</li>
<li>可以改变方法参数，在proceed方法执行的时候可以传入Object[]对象作为参数，作为目标方法的实参使用。</li>
<li>如果传入Object[]参数与方法入参数量不同或类型不同，会抛出异常</li>
<li>通过改变proceed()的返回值来修改目标方法的返回值</li>
</ul>
</li>
</ol>
<p>示例代码</p>
<pre class="java"><code class="hljs"><span class="hljs-meta"><span class="hljs-meta">@Aspect</span>
<span class="hljs-keyword"><span class="hljs-keyword">public</span> <span class="hljs-class"><span class="hljs-keyword"><span class="hljs-class"><span class="hljs-keyword">class</span> </span><span class="hljs-title"><span class="hljs-class"><span class="hljs-title">TxAspect</span> </span>{

<span class="hljs-comment"><span class="hljs-comment">//环绕处理</span>
<span class="hljs-meta"><span class="hljs-meta">@Around</span>(<span class="hljs-string"><span class="hljs-string">"execution(_ com.cnblogs.hellxz.service._.\*(..))"</span>)
<span class="hljs-function"><span class="hljs-function">Object </span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">auth</span></span><span class="hljs-params"><span class="hljs-function"><span class="hljs-params">(ProceedingJoinPoint point)</span> </span>{

Object object = <span class="hljs-keyword"><span class="hljs-keyword">null</span>;
<span class="hljs-keyword"><span class="hljs-keyword">try</span> {
System.out.println(<span class="hljs-string"><span class="hljs-string">"事务开启……"</span>);
<span class="hljs-comment"><span class="hljs-comment">//放行</span>
object = point.proceed();
System.out.println(<span class="hljs-string"><span class="hljs-string">"事务关闭……"</span>);
} <span class="hljs-keyword"><span class="hljs-keyword">catch</span> (Throwable e) {
e.printStackTrace();
}

<span class="hljs-keyword"><span class="hljs-keyword">return</span> object;
}
}</span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></code></pre>

<ol>
<li>@AfterRetruning: 在方法返回之前，获取返回值并进行记录操作
<ul>
<li>和上边的方法不同的地方是该注解除了切点，还有一个返回值的对象名</li>
<li>不同的两个注解参数：returning与pointcut,其中pointcut参数可以为切面表达式，也可为切点</li>
<li>returning定义的参数名作为切面方法的入参名，类型可以指定。如果切面方法入参类型指定Object则无限制，如果为其它类型，则当且仅当目标方法返回相同类型时才会进入切面方法，否则不会</li>
<li>还有一个默认的value参数，如果指定了pointcut则会覆盖value的值</li>
<li>与@After类似，但@AfterReturning只有方法成功完成才会被织入，而@After不管结果如何都会被织入</li>
</ul>
</li>
</ol>
<blockquote>
<p>虽然可以拿到返回值，但无法改变返回值</p>
</blockquote>
<p>示例代码</p>
<pre class="java"><code class="hljs"><span class="hljs-meta"><span class="hljs-meta">@Aspect</span>
<span class="hljs-keyword"><span class="hljs-keyword">public</span> <span class="hljs-class"><span class="hljs-keyword"><span class="hljs-class"><span class="hljs-keyword">class</span> </span><span class="hljs-title"><span class="hljs-class"><span class="hljs-title">AfterReturningAspect</span> </span>{

<span class="hljs-meta"><span class="hljs-meta">@AfterReturning</span>(returning=<span class="hljs-string"><span class="hljs-string">"rvt"</span>,
pointcut = <span class="hljs-string"><span class="hljs-string">"execution(_ com.cnblogs.hellxz.service._.\*(..))"</span>)
<span class="hljs-comment"><span class="hljs-comment">//声明 rvt 时指定的类型会限定目标方法的返回值类型，必须返回指定类型或者没有返回值</span>
<span class="hljs-comment"><span class="hljs-comment">//rvt 类型为 Object 则是不对返回值做限制</span>
<span class="hljs-function"><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-keyword">public</span> </span><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-keyword">void</span> </span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">log</span></span><span class="hljs-params"><span class="hljs-function"><span class="hljs-params">(Object rvt)</span> </span>{
System.out.println(<span class="hljs-string"><span class="hljs-string">"获取目标返回值："</span>+ rvt);
System.out.println(<span class="hljs-string"><span class="hljs-string">"假装在记录日志……"</span>);
}

<span class="hljs-comment"><span class="hljs-comment">/\*\*

- 这个方法可以看出如果目标方法的返回值类型与切面入参的类型相同才会执行此切面方法
- </span><span class="hljs-doctag"><span class="hljs-comment"><span class="hljs-doctag">@param</span> itr
  _/</span>
  <span class="hljs-meta"><span class="hljs-meta">@AfterReturning</span>(returning=<span class="hljs-string"><span class="hljs-string">"itr"</span>,
  pointcut=<span class="hljs-string"><span class="hljs-string">"execution(_ com.cnblogs.hellxz.service._._(..))"</span>)
  <span class="hljs-function"><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-keyword">public</span> </span><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-keyword">void</span> </span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">test</span></span><span class="hljs-params"><span class="hljs-function"><span class="hljs-params">(Integer itr)</span> </span>{
  System.out.println(<span class="hljs-string"><span class="hljs-string">"故意捣乱……:"</span>+ itr);
  }
  }</span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></code></pre>
  <ol>
  <li>@AfterThrowing: 在异常抛出前进行处理，比如记录错误日志
  <ul>
  <li>与<code>@AfterReturning</code>类似，同样有一个切点和一个定义参数名的参数——throwing</li>
  <li>同样可以通过切面方法的入参进行限制切面方法的执行，e.g. 只打印IOException类型的异常, 完全不限制可以使用Throwable类型</li>
  <li>pointcut使用同<code>@AfterReturning</code></li>
  <li>还有一个默认的value参数，如果指定了pointcut则会覆盖value的值</li>
  <li>如果目标方法中的异常被try catch块捕获，此时异常完全被catch块处理，如果没有另外抛出异常，那么还是会正常运行，不会进入AfterThrowing切面方法</li>
  </ul>
  </li>
  </ol>
  <p>示例代码</p>
  <pre class="java"><code class="hljs"><span class="hljs-meta"><span class="hljs-meta">@Aspect</span>
  <span class="hljs-keyword"><span class="hljs-keyword">public</span> <span class="hljs-class"><span class="hljs-keyword"><span class="hljs-class"><span class="hljs-keyword">class</span> </span><span class="hljs-title"><span class="hljs-class"><span class="hljs-title">AfterThrowingAspect</span> </span>{

<span class="hljs-meta"><span class="hljs-meta">@Pointcut</span>(<span class="hljs-string"><span class="hljs-string">"execution(_ com.cnblogs.hellxz.test._.\*(..))"</span>)
<span class="hljs-function"><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-keyword">public</span> </span><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-keyword">void</span> </span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">pointcut</span></span><span class="hljs-params"><span class="hljs-function"><span class="hljs-params">()</span> </span>{}

<span class="hljs-comment"><span class="hljs-comment">/\*\*

- 如果抛出异常在切面中的几个异常类型都满足，那么这几个切面方法都会执行
  \*/</span>
  <span class="hljs-meta"><span class="hljs-meta">@AfterThrowing</span>(throwing=<span class="hljs-string"><span class="hljs-string">"ex1"</span>,
  pointcut=<span class="hljs-string"><span class="hljs-string">"pointcut()"</span>)
  <span class="hljs-comment"><span class="hljs-comment">//无论异常还是错误都会记录</span>
  <span class="hljs-comment"><span class="hljs-comment">//不捕捉错误可以使用 Exception</span>
  <span class="hljs-function"><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-keyword">public</span> </span><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-keyword">void</span> </span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">throwing</span></span><span class="hljs-params"><span class="hljs-function"><span class="hljs-params">(Throwable ex1)</span> </span>{
  System.out.println(<span class="hljs-string"><span class="hljs-string">"出现异常："</span>+ex1);
  }

<span class="hljs-meta"><span class="hljs-meta">@AfterThrowing</span>(throwing=<span class="hljs-string"><span class="hljs-string">"ex"</span>,
pointcut=<span class="hljs-string"><span class="hljs-string">"pointcut()"</span>)
<span class="hljs-comment"><span class="hljs-comment">//只管 IOException 的抛出</span>
<span class="hljs-function"><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-keyword">public</span> </span><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-keyword">void</span> </span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">throwing2</span></span><span class="hljs-params"><span class="hljs-function"><span class="hljs-params">(IOException ex)</span> </span>{
System.out.println(<span class="hljs-string"><span class="hljs-string">"出现 IO 异常: "</span>+ex);
}
}</span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></span></code></pre>

<blockquote>
<p>pointcut定义的切点方法在@Before/@After/@Around需要写在双引号中，e.g. @Before("pointCut()")</p>
</blockquote>
<h3 id="joinpoint的概念与方法说明">JoinPoint的概念与方法说明</h3>
<h4 id="概念">概念</h4>
<ul>
<li>顾名思义，连接点，织入增强处理的连接点</li>
<li>程序运行时的目标方法的信息都会封装到这个连接点对象中</li>
<li>此连接点只读</li>
</ul>
<h4 id="方法说明">方法说明</h4>
<ul>
<li><code>Object[] getArgs()</code>：返回执行目标方法时的参数</li>
<li><code>Signature getSignature()</code>:返回被增强方法的相关信息，e.g 方法名 etc</li>
<li><code>Object getTarget()</code>:返回被织入增强处理的目标对象</li>
<li><code>Object getThis()</code>:返回AOP框架目标对象生成的代理对象</li>
</ul>
<h4 id="使用">使用</h4>
<ul>
<li>在@Before/@After/@AfterReturning/@AfterThrowing所修饰的切面方法的参数列表中加入JoinPoint对象，可以使用这个对象获得整个增强处理中的所有细节</li>
<li>此方法不适用于@Around, 其可用ProceedingJoinPoint作为连接点</li>
</ul>
<h3 id="proceedingjoinpoint的概念与方法说明">ProceedingJoinPoint的概念与方法说明</h3>
<h4 id="概念-1">概念</h4>
<ul>
<li>是JoinPoint的子类</li>
<li>与JoinPoint概念基本相同，区别在于是可修改的</li>
<li>使用@Around时，第一个入参必须为ProceedingJoinPoint类型</li>
<li>在@Around方法内时需要执行proceed()或proceed(Object[] args)方法使方法继续，否则会一直处于阻滞状态</li>
</ul>
<h4 id="方法说明-1">方法说明</h4>
<p>ProceedingJoinPoint是JoinPoint的子类，包含其所有方法外，还有两个公有方法</p>
<ul>
<li><code>Object proceed()</code>:执行此方法才会执行目标方法</li>
<li>
<p><code>Object proceed(Object[] args)</code>:执行此方法才会执行目标方法，而且会使用Object数组参数去代替实参,如果传入Object[]参数与方法入参数量不同或类型不同，会抛出异常</p>
<blockquote>
<p>通过修改proceed方法的返回值来修改目标方法的返回值</p>
</blockquote>
</li>
</ul>
<h3 id="编入的优先级">编入的优先级</h3>
<p>优先级最高的会最先被织入，在退出连接点的时候，具有最高的优先级的最后被织入</p>
<p><img src="https://images2018.cnblogs.com/blog/1149398/201809/1149398-20180911092943285-1678031734.png" alt=""></p>
<p>当不同切面中两个增强处理切入同一连接点的时候，Spring AOP 会使用随机织入的方式<br>如果想要指定优先级，那么有两种方案：</p>
<ul>
<li>让切面类实现&nbsp;<code>org.springframework.core.Ordered</code>接口，实现getOrder方法，返回要指定的优先级</li>
<li>切面类使用<code>@Order</code>修饰，指定一个优先级的值，值越小，优先级越高</li>

</ul>
<h3 id="访问目标方法的形参">访问目标方法的形参</h3>
<p>除了使用JoinPoint或ProceedingJoinPoint来获取目标方法的相关信息外（包括形参），如果只是简单访问形参，那么还有一种方法可以实现</p>
<ul>
<li>在pointcut的execution表达式之后加入<code>&amp;&amp; args(arg0,arg1)</code>这种方式</li>

</ul>
<pre class="java"><code class="hljs"><span class="hljs-meta"><span class="hljs-meta">@Aspect</span>
<span class="hljs-keyword"><span class="hljs-keyword">public</span> <span class="hljs-class"><span class="hljs-keyword"><span class="hljs-class"><span class="hljs-keyword">class</span> </span><span class="hljs-title"><span class="hljs-class"><span class="hljs-title">AccessInputArgs</span> </span>{

<span class="hljs-meta"><span class="hljs-meta">@Before</span>(<span class="hljs-string"><span class="hljs-string">"execution(_ com.cnblogs.hellxz.test._.\*(..)) &amp;&amp; args(arg0, arg1)"</span>)
<span class="hljs-function"><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-keyword">public</span> </span><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-keyword">void</span> </span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">access</span></span><span class="hljs-params"><span class="hljs-function"><span class="hljs-params">(String arg0, String arg1)</span></span>{
System.out.println(<span class="hljs-string"><span class="hljs-string">"接收到的参数为 arg0="</span>+arg0+<span class="hljs-string"><span class="hljs-string">",arg1="</span>+arg1);
}
}</span></span></span></span></span></span></span></span></span></span></span></span></span></span></code></pre>

<blockquote>
<p>注意：通过这种方式会只匹配到方法只有指定形参<strong>数量</strong>的方法，并且，在切面方法中指定的<strong>类型</strong>会限制目标方法，不符合条件的不会进行织入增强</p>
</blockquote>
<h3 id="定义切入点">定义切入点</h3>
<p>通过定义切入点，我们可以复用切点，减少重复定义切点表达式等<br>切入点定义包含两个部分：</p>
<ul>
<li>切入点表达式</li>
<li>包含名字和任意参数的方法签名</li>

</ul>
<p>使用@Pointcut注解进行标记一个无参无返回值的方法，加上切点表达式</p>
<pre class="java"><code class="hljs">    <span class="hljs-meta"><span class="hljs-meta">@Pointcut</span>(<span class="hljs-string"><span class="hljs-string">"execution(* com.cnblogs.hellxz.test.*.*(..))"</span>)
<span class="hljs-function"><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-keyword">public</span> </span><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-keyword">void</span> </span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">pointcut</span></span><span class="hljs-params"><span class="hljs-function"><span class="hljs-params">()</span></span>{}</span></span></span></span></span></span></span></code></pre>
<h3 id="切入点指示符">切入点指示符</h3>
<p>Spring AOP 支持10种切点指示符：execution、within、this、target、args、@target、@args、@within、@annotation、bean下面做下简记(没有写@Pointcut(),请注意)：</p>
<ul>
<ul>
<li>
<p><strong>execution</strong>: 用来匹配执行方法的连接点的指示符。<br>用法相对复杂，格式如下:<code>execution(权限访问符 返回值类型 方法所属的类名包路径.方法名(形参类型) 异常类型)</code><br>e.g. execution(public String com.cnblogs.hellxz.test.Test.access(String,String))<br>权限修饰符和异常类型可省略，返回类型支持通配符，类名、方法名支持*通配，方法形参支持..通配</p>

</li>
<li>
<p><strong>within</strong>: 用来限定连接点属于某个确定类型的类。<br>within(com.cnblogs.hellxz.test.Test)<br>within(com.cnblogs.hellxz.test.<em>) //包下类<br>within(com.cnblogs.hellxz.test..</em>) //包下及子包下</p>

</li>
<li><strong>this和target</strong>: this用于没有实现接口的Cglib代理类型，target用于实现了接口的JDK代理目标类型<br>举例：this(com.cnblogs.hellxz.test.Foo) //Foo没有实现接口，使用Cglib代理，用this<br>实现了个接口public class Foo implements Bar{...}<br>target(com.cnblogs.hellxz.test.Test) //Foo实现了接口的情况</li>
<li><strong>args</strong>: 对连接点的参数类型进行限制，要求参数类型是指定类型的实例。<br>args(Long)</li>
<li><strong>@target</strong>: 用于匹配<strong>类头有指定注解</strong>的连接点<br>@target(org.springframework.stereotype.Repository)</li>
<li><strong>@args</strong>: 用来匹配连接点的参数的，@args指出连接点在运行时传过来的参数的类必须要有指定的注解<br><code>java @Pointcut("@args(org.springframework.web.bind.annotation.RequestBody)") public void methodsAcceptingEntities() {}</code></li>
<li><strong>@within</strong>: 指定匹配必须包括某个注解的的类里的所有连接点<br>@within(org.springframework.stereotype.Repository)</li>
<li><strong>@annotation</strong>: 匹配那些有指定注解的连接点<br>@annotation(org.springframework.stereotype.Repository)</li>
<li>
<p><strong>bean</strong>: 用于匹配指定Bean实例内的连接点，传入bean的id或name,支持使用*通配符</p>
<h3 id="切点表达式组合">切点表达式组合</h3>
<p>使用&amp;&amp;、||、!、三种运算符来组合切点表达式，表示与或非的关系<br><code>execution(* com.cnblogs.hellxz.test.*.*(..)) &amp;&amp; args(arg0, arg1)</code></p>

</li>

</ul>

</ul>
</div>
<div id="MySignature"></div>
<div class="clear"></div>
<div id="blog_post_info_block"><div id="BlogPostCategory">
分类: 
<a href="https://www.cnblogs.com/Fantastic-Code/category/1524253.html" target="_blank">spring</a></div>

<div id="blog_post_info">
<div id="green_channel">
<a href="javascript:void(0);" id="green_channel_digg" onclick="DiggIt(11593544,cb_blogId,1);green_channel_success(this,'谢谢推荐！');">好文要顶</a>
<a id="green_channel_follow" onclick="follow('ba2cd06b-0400-409c-3b0d-08d54dba4453');" href="javascript:void(0);">关注我</a>
<a id="green_channel_favorite" onclick="AddToWz(cb_entryId);return false;" href="javascript:void(0);">收藏该文</a>
<a id="green_channel_weibo" href="javascript:void(0);" title="分享至新浪微博" onclick="ShareToTsina()"><img src="https://common.cnblogs.com/images/icon_weibo_24.png" alt=""></a>
<a id="green_channel_wechat" href="javascript:void(0);" title="分享至微信" onclick="shareOnWechat()"><img src="https://common.cnblogs.com/images/wechat.png" alt=""></a>
</div>
<div id="author_profile">
<div id="author_profile_info" class="author_profile_info">
<div id="author_profile_detail" class="author_profile_info">
<a href="https://home.cnblogs.com/u/Fantastic-Code/">FantasticSpeed</a><br>
<a href="https://home.cnblogs.com/u/Fantastic-Code/followees/">关注 - 15</a><br>
<a href="https://home.cnblogs.com/u/Fantastic-Code/followers/">粉丝 - 0</a>
</div>
</div>
<div class="clear"></div>
<div id="author_profile_honor"></div>
<div id="author_profile_follow">
<a href="javascript:void(0);" onclick="follow('ba2cd06b-0400-409c-3b0d-08d54dba4453');return false;">+加关注</a>
</div>
</div>
<div id="div_digg">
<div class="diggit" onclick="votePost(11593544,'Digg')">
<span class="diggnum" id="digg_count">0</span>
</div>
<div class="buryit" onclick="votePost(11593544,'Bury')">
<span class="burynum" id="bury_count">0</span>
</div>
<div class="clear"></div>
<div class="diggword" id="digg_tips">
</div>
</div>

<script type="text/javascript">
currentDiggType = 0;
</script></div>
<div class="clear"></div>
<div id="post_next_prev">

<a href="https://www.cnblogs.com/Fantastic-Code/p/11592380.html" class="p_n_p_prefix">« </a> 上一篇： <a href="https://www.cnblogs.com/Fantastic-Code/p/11592380.html" title="发布于 2019-09-26 16:04">springboot 初学</a>
<br>
<a href="https://www.cnblogs.com/Fantastic-Code/p/11597024.html" class="p_n_p_prefix">» </a> 下一篇： <a href="https://www.cnblogs.com/Fantastic-Code/p/11597024.html" title="发布于 2019-09-27 11:54">拦截器处理</a>

</div>
</div>
</div>
<div class="postDesc">posted @ 
<span id="post-date">2019-09-26 18:16</span>&nbsp;
<a href="https://www.cnblogs.com/Fantastic-Code/">FantasticSpeed</a>&nbsp;
阅读(<span id="post_view_count">503</span>)&nbsp;
评论(<span id="post_comment_count">0</span>)&nbsp;
<a href="https://i.cnblogs.com/EditPosts.aspx?postid=11593544" rel="nofollow">编辑</a>&nbsp;
<a href="javascript:void(0)" onclick="AddToWz(11593544);return false;">收藏</a></div>
</div>

</div><!--end: topics 文章、评论容器-->
</div>
<script src="https://common.cnblogs.com/highlight/9.12.0/highlight.min.js"></script>
<script>markdown_highlight();</script>
<script>
var allowComments = true, cb_blogId = 409216, cb_blogApp = 'Fantastic-Code', cb_blogUserGuid = 'ba2cd06b-0400-409c-3b0d-08d54dba4453';
var cb_entryId = 11593544, cb_entryCreatedDate = '2019-09-26 18:16', cb_postType = 1; 
loadViewCount(cb_entryId);
</script><a name="!comments"></a>
<div id="blog-comments-placeholder"></div>
<script>
var commentManager = new blogCommentManager();
commentManager.renderComments(0);
</script>

<div id="comment_form" class="commentform">
<a name="commentform"></a>
<div id="divCommentShow"></div>
<div id="comment_nav"><span id="span_refresh_tips"></span><a href="javascript:void(0);" onclick="return RefreshCommentList();" id="lnk_RefreshComments" runat="server" clientidmode="Static">刷新评论</a><a href="#" onclick="return RefreshPage();">刷新页面</a><a href="#top">返回顶部</a></div>
<div id="comment_form_container"><div class="login_tips">
注册用户登录后才能发表评论，请 
<a rel="nofollow" href="javascript:void(0);" class="underline" onclick="return login('commentform');">登录</a>
或 
<a rel="nofollow" href="javascript:void(0);" class="underline" onclick="return register();">注册</a>，
<a href="https://www.cnblogs.com/">访问</a> 网站首页。
</div></div>
<div class="ad_text_commentbox" id="ad_text_under_commentbox"></div>
<div id="ad_t2"><a href="http://www.ucancode.com/index.htm" target="_blank" onclick="ga('send', 'event', 'Link', 'click', 'T2-ucancode')">【推荐】超50万行VC++源码: 大型组态工控、电力仿真CAD与GIS源码库</a><br><a href="https://cloud.tencent.com/act/season?fromSource=gwzcw.3422970.3422970.3422970&amp;utm_medium=cpc&amp;utm_id=gwzcw.3422970.3422970.3422970" target="_blank" onclick="ga('send', 'event', 'Link', 'click', 'T2-腾讯云')">【活动】腾讯云服务器推出云产品采购季 1核2G首年仅需99元</a><br><a href="https://developer.aliyun.com/ask/258350?utm_content=g_1000088952" target="_blank" onclick="ga('send', 'event', 'Link', 'click', 'T2-阿里云开发者社区')">【推荐】精品问答：前端开发必懂之 HTML 技术五十问</a><br><a href="https://developer.aliyun.com/ask/257905?utm_content=g_1000088947" target="_blank" onclick="ga('send', 'event', 'Link', 'click', 'T2-阿里云开发者社区')">【推荐】精品问答：Java 技术 1000 问</a><br></div>
<div id="opt_under_post"></div>
<script async="async" src="https://www.googletagservices.com/tag/js/gpt.js"></script>
<script>
var googletag = googletag || {};
googletag.cmd = googletag.cmd || [];
</script>
<script>
googletag.cmd.push(function () {
googletag.defineSlot("/1090369/C1", [300, 250], "div-gpt-ad-1546353474406-0").addService(googletag.pubads());
googletag.defineSlot("/1090369/C2", [468, 60], "div-gpt-ad-1539008685004-0").addService(googletag.pubads());
googletag.pubads().enableSingleRequest();
googletag.enableServices();
});
</script>
<div id="cnblogs_c1" class="c_ad_block">
<div id="div-gpt-ad-1546353474406-0" style="height:250px; width:300px;" data-google-query-id="CN6wvZeP3ecCFRFuvQodKKwPcg"><div id="google_ads_iframe_/1090369/C1_0__container__" style="border: 0pt none;"><iframe id="google_ads_iframe_/1090369/C1_0" title="3rd party ad content" name="google_ads_iframe_/1090369/C1_0" width="300" height="250" scrolling="no" marginwidth="0" marginheight="0" frameborder="0" srcdoc="" style="border: 0px; vertical-align: bottom;" data-google-container-id="1" data-load-complete="true"></iframe></div></div>
</div>
<div id="under_post_news"><div class="recomm-block"><b>相关博文：</b><br>·  <a title="Spring AOP详细介绍" href="https://www.cnblogs.com/liuruowang/p/5711563.html" target="_blank" onclick="clickRecomItmem(5711563)">Spring AOP详细介绍</a><br>·  <a title="spring框架AOP核心详解" href="https://www.cnblogs.com/Snail-1174158844/p/9407535.html" target="_blank" onclick="clickRecomItmem(9407535)">spring框架AOP核心详解</a><br>·  <a title="spring AOP编程--AspectJ注解方式" href="https://www.cnblogs.com/caoyc/p/5627978.html" target="_blank" onclick="clickRecomItmem(5627978)">spring AOP编程--AspectJ注解方式</a><br>·  <a title="Spring系列（四）：SpringAOP详解" href="https://www.cnblogs.com/toby-xu/p/11361351.html" target="_blank" onclick="clickRecomItmem(11361351)">Spring系列（四）：SpringAOP详解</a><br>·  <a title="Spring -- AOP" href="https://www.cnblogs.com/androidsuperman/p/7501923.html" target="_blank" onclick="clickRecomItmem(7501923)">Spring -- AOP</a><br>»  <a target="_blank" href="https://recomm.cnblogs.com/blogpost/11593544">更多推荐...</a><div id="cnblogs_t5"><a href="https://developer.aliyun.com/ask/257905?utm_content=g_1000088947" target="_blank" onclick="ga('send', 'event', 'Link', 'click', 'T5-阿里云开发者社区')">精品问答：Java 技术 1000 问</a></div></div></div>
<div id="cnblogs_c2" class="c_ad_block">
<div id="div-gpt-ad-1539008685004-0" style="height:60px; width:468px;" data-google-query-id="CN-wvZeP3ecCFRFuvQodKKwPcg">
<script>
if (new Date() >= new Date(2018, 9, 13)) {
googletag.cmd.push(function () { googletag.display("div-gpt-ad-1539008685004-0"); });
}
</script>
<div id="google_ads_iframe_/1090369/C2_0__container__" style="border: 0pt none; width: 468px; height: 60px;"></div></div>
</div>
<div id="under_post_kb">
<div class="itnews c_ad_block">
<b>最新 IT 新闻</b>:
<br>
·              <a href="//news.cnblogs.com/n/655975/" target="_blank">我国首颗5G卫星通信试验成功：通信能力达10Gbps</a>
<br>
·              <a href="//news.cnblogs.com/n/655974/" target="_blank">刘强东卸任京东云计算全资子公司经理一职</a>
<br>
·              <a href="//news.cnblogs.com/n/655973/" target="_blank">恒星参宿四停止变暗 近期或不会发生超新星爆炸</a>
<br>
·              <a href="//news.cnblogs.com/n/655972/" target="_blank">服务器DRAM合约价暴涨，但PC和手机快涨不动了</a>
<br>
·              <a href="//news.cnblogs.com/n/655971/" target="_blank">回应质疑：统一操作系统UOS的五大杀手锏</a>
<br>
» <a href="https://news.cnblogs.com/" title="IT 新闻" target="_blank">更多新闻...</a>
</div></div>
<div id="HistoryToday" class="c_ad_block">
<b>历史上的今天：</b>
<br>

2019-09-26 <a href="https://www.cnblogs.com/Fantastic-Code/p/11592380.html">springboot 初学</a>
<br>

</div>
<script type="text/javascript">
fixPostBody();
setTimeout(function() { incrementViewCount(cb_entryId); }, 50);        deliverAdT2();
deliverAdC1();
deliverAdC2();
loadNewsAndKb();
loadBlogSignature();
LoadPostCategoriesTags(cb_blogId, cb_entryId);        LoadPostInfoBlock(cb_blogId, cb_entryId, cb_blogApp, cb_blogUserGuid);
GetPrevNextPost(cb_entryId, cb_blogId, cb_entryCreatedDate, cb_postType);
loadOptUnderPost();
GetHistoryToday(cb_blogId, cb_blogApp, cb_entryCreatedDate);
</script>
</div>
</div><!--end: forFlow -->
</div>
