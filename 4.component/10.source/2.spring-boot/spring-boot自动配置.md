<div id="wrapper" class="body-wrap">
<div class="menu-l">
<div class="canvas-wrap">
<canvas data-colors="#eaeaea" data-sectionheight="100" data-contentid="js-content" id="myCanvas1" class="anm-canvas"></canvas>
</div>
<div id="js-content" class="content-ll">
<article id="post-Spring-Boot/AutoConfiguration" class="article article-type-post " itemscope="" itemprop="blogPost">
<div class="article-inner">

<header class="article-header">


<h1 class="article-title" itemprop="name">
精尽 Spring Boot 源码分析 —— 自动配置
</h1>



<a href="/Spring-Boot/AutoConfiguration/" class="archive-article-date">
<time style="display: none;" datetime="2021-01-09T16:00:00.000Z" itemprop="datePublished"><i class="icon-calendar icon"></i>2021-01-10</time>
</a>

</header>

<div class="article-entry" itemprop="articleBody">

<h1 id="1-概述"><a href="#1-概述" class="headerlink" title="1. 概述"></a>1. 概述</h1><p>本文，我们来分享 Spring Boot 自动配置的实现源码。在故事的开始，我们先来说两个事情：</p>
<ul>
<li>自动配置和自动装配的区别？</li>
<li>Spring Boot 配置的原理</li>
</ul>
<h1 id="2-自动配置-V-S-自动装配"><a href="#2-自动配置-V-S-自动装配" class="headerlink" title="2. 自动配置 V.S 自动装配"></a>2. 自动配置 V.S 自动装配</h1><p>在这篇文章的开始，艿艿是有点混淆自动配置和自动装配的概念，后来经过 Google 之后，发现两者是截然不如同的：</p>
<ul>
<li>自动配置：是 Spring Boot 提供的，实现通过 jar 包的依赖，能够自动配置应用程序。例如说：我们引入 <code>spring-boot-starter-web</code> 之后，就自动引入了 Spring MVC 相关的 jar 包，从而自动配置 Spring MVC 。</li>
<li>自动装配：是 Spring 提供的 IoC 注入方式，具体看看 <a href="http://wiki.jikexueyuan.com/project/spring/beans-autowiring.html" rel="external nofollow noopener noreferrer" target="_blank">《Spring 教程 —— Beans 自动装配》</a> 文档。</li>
</ul>
<p>所以，不要和艿艿一样愚蠢的搞错落。</p>
<h1 id="3-自动装配原理"><a href="#3-自动装配原理" class="headerlink" title="3. 自动装配原理"></a>3. 自动装配原理</h1><p>胖友可以直接看 <a href="https://www.jianshu.com/p/0114a5d728ea" rel="external nofollow noopener noreferrer" target="_blank">《详解 Spring Boot 自动配置机制》</a> 文章的 <a href="#">「二、Spring Boot 自动配置」</a> 小节，艿艿觉得写的挺清晰的。</p>
<blockquote>
<p>下面，我们即开始正式撸具体的代码实现了。</p>
</blockquote>
<h1 id="4-SpringBootApplication"><a href="#4-SpringBootApplication" class="headerlink" title="4. @SpringBootApplication"></a>4. @SpringBootApplication</h1><p><code>org.springframework.boot.autoconfigure.@SpringBootApplication</code> 注解，基本我们的 Spring Boot 应用，一定会去有这样一个注解。并且，通过使用它，不仅仅能标记这是一个 Spring Boot 应用，而且能够开启自动配置的功能。这是为什么呢？</p>
<blockquote>
<p>😈 <code>@SpringBootApplication</code> 注解，它在 <code>spring-boot-autoconfigure</code> 模块中。所以，我们使用 Spring Boot 项目时，如果不想使用自动配置功能，就不用引入它。当然，我们貌似不太会存在这样的需求，是吧~</p>
</blockquote>
<p><code>@SpringBootApplication</code> 是一个<strong>组合</strong>注解。代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="comment">// SpringBootApplication.java</span></span><br><span class="line"></span><br><span class="line"><span class="meta">@Target</span>(ElementType.TYPE)</span><br><span class="line"><span class="meta">@Retention</span>(RetentionPolicy.RUNTIME)</span><br><span class="line"><span class="meta">@Documented</span></span><br><span class="line"><span class="meta">@Inherited</span></span><br><span class="line"><span class="meta">@SpringBootConfiguration</span></span><br><span class="line"><span class="meta">@EnableAutoConfiguration</span></span><br><span class="line"><span class="meta">@ComponentScan</span>(excludeFilters = {</span><br><span class="line">		<span class="meta">@Filter</span>(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class),</span><br><span class="line">		<span class="meta">@Filter</span>(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class) })</span><br><span class="line"><span class="keyword">public</span> <span class="meta">@interface</span> SpringBootApplication {</span><br><span class="line"></span><br><span class="line">	<span class="comment">/**</span></span><br><span class="line"><span class="comment">	 * Exclude specific auto-configuration classes such that they will never be applied.</span></span><br><span class="line"><span class="comment">	 * <span class="doctag">@return</span> the classes to exclude</span></span><br><span class="line"><span class="comment">	 */</span></span><br><span class="line">	<span class="meta">@AliasFor</span>(annotation = EnableAutoConfiguration.class)</span><br><span class="line">	Class&lt;?&gt;[] exclude() <span class="keyword">default</span> {};</span><br><span class="line"></span><br><span class="line">	<span class="comment">/**</span></span><br><span class="line"><span class="comment">	 * Exclude specific auto-configuration class names such that they will never be</span></span><br><span class="line"><span class="comment">	 * applied.</span></span><br><span class="line"><span class="comment">	 * <span class="doctag">@return</span> the class names to exclude</span></span><br><span class="line"><span class="comment">	 * <span class="doctag">@since</span> 1.3.0</span></span><br><span class="line"><span class="comment">	 */</span></span><br><span class="line">	<span class="meta">@AliasFor</span>(annotation = EnableAutoConfiguration.class)</span><br><span class="line">	String[] excludeName() <span class="keyword">default</span> {};</span><br><span class="line"></span><br><span class="line">	<span class="comment">/**</span></span><br><span class="line"><span class="comment">	 * Base packages to scan for annotated components. Use {<span class="doctag">@link</span> #scanBasePackageClasses}</span></span><br><span class="line"><span class="comment">	 * for a type-safe alternative to String-based package names.</span></span><br><span class="line"><span class="comment">	 * <span class="doctag">@return</span> base packages to scan</span></span><br><span class="line"><span class="comment">	 * <span class="doctag">@since</span> 1.3.0</span></span><br><span class="line"><span class="comment">	 */</span></span><br><span class="line">	<span class="meta">@AliasFor</span>(annotation = ComponentScan.class, attribute = <span class="string">"basePackages"</span>)</span><br><span class="line">	String[] scanBasePackages() <span class="keyword">default</span> {};</span><br><span class="line"></span><br><span class="line">	<span class="comment">/**</span></span><br><span class="line"><span class="comment">	 * Type-safe alternative to {<span class="doctag">@link</span> #scanBasePackages} for specifying the packages to</span></span><br><span class="line"><span class="comment">	 * scan for annotated components. The package of each class specified will be scanned.</span></span><br><span class="line"><span class="comment">	 * &lt;p&gt;</span></span><br><span class="line"><span class="comment">	 * Consider creating a special no-op marker class or interface in each package that</span></span><br><span class="line"><span class="comment">	 * serves no purpose other than being referenced by this attribute.</span></span><br><span class="line"><span class="comment">	 * <span class="doctag">@return</span> base packages to scan</span></span><br><span class="line"><span class="comment">	 * <span class="doctag">@since</span> 1.3.0</span></span><br><span class="line"><span class="comment">	 */</span></span><br><span class="line">	<span class="meta">@AliasFor</span>(annotation = ComponentScan.class, attribute = <span class="string">"basePackageClasses"</span>)</span><br><span class="line">	Class&lt;?&gt;[] scanBasePackageClasses() <span class="keyword">default</span> {};</span><br><span class="line"></span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<p>下面，我们来逐个看 <code>@SpringBootApplication</code> 上的每个注解。</p>
<h2 id="4-1-Inherited"><a href="#4-1-Inherited" class="headerlink" title="4.1 @Inherited"></a>4.1 @Inherited</h2><blockquote>
<p>Java 自带的注解。</p>
</blockquote>
<p><code>java.lang.annotation.@Inherited</code> 注解，使用此注解声明出来的自定义注解，在使用<strong>此</strong>自定义注解时，如果注解在类上面时，子类会自动继承此注解，否则的话，子类不会继承此注解。</p>
<p>这里一定要记住，使用 <code>@Inherited</code> 声明出来的注解，只有在类上使用时才会有效，对方法，属性等其他无效。</p>
<p>不了解的胖友，可以看看 <a href="https://blog.csdn.net/snow_crazy/article/details/39381695" rel="external nofollow noopener noreferrer" target="_blank">《关于 Java 注解中元注解 Inherited 的使用详解》</a> 文章。</p>
<h2 id="4-2-SpringBootConfiguration"><a href="#4-2-SpringBootConfiguration" class="headerlink" title="4.2 @SpringBootConfiguration"></a>4.2 @SpringBootConfiguration</h2><blockquote>
<p>Spring Boot 自定义的注解</p>
</blockquote>
<p><code>org.springframework.boot.@SpringBootConfiguration</code> 注解，标记这是一个 Spring Boot 配置类。代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="comment">// SpringBootConfiguration.java</span></span><br><span class="line"></span><br><span class="line"><span class="meta">@Target</span>(ElementType.TYPE)</span><br><span class="line"><span class="meta">@Retention</span>(RetentionPolicy.RUNTIME)</span><br><span class="line"><span class="meta">@Documented</span></span><br><span class="line"><span class="meta">@Configuration</span></span><br><span class="line"><span class="keyword">public</span> <span class="meta">@interface</span> SpringBootConfiguration {</span><br><span class="line"></span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li>可以看到，它上面继承自 <code>@Configuration</code> 注解，所以两者功能也一致，可以将当前类内声明的一个或多个以 <code>@Bean</code> 注解标记的方法的实例纳入到 Srping 容器中，并且实例名就是方法名。</li>
</ul>
<h2 id="4-3-ComponentScan"><a href="#4-3-ComponentScan" class="headerlink" title="4.3 @ComponentScan"></a>4.3 @ComponentScan</h2><blockquote>
<p>Spring 自定义的注解</p>
</blockquote>
<p><code>org.springframework.context.annotation.@ComponentScan</code> 注解，扫描指定路径下的 Component（<code>@Componment</code>、<code>@Configuration</code>、<code>@Service</code> 等等）。</p>
<p>不了解的胖友，可以看看 <a href="https://shihlei.iteye.com/blog/2405675" rel="external nofollow noopener noreferrer" target="_blank">《Spring：@ComponentScan 使用》</a> 文章。</p>
<h2 id="4-4-EnableAutoConfiguration"><a href="#4-4-EnableAutoConfiguration" class="headerlink" title="4.4 @EnableAutoConfiguration"></a>4.4 @EnableAutoConfiguration</h2><blockquote>
<p>Spring Boot 自定义的注解</p>
</blockquote>
<p><code>org.springframework.boot.autoconfigure.@EnableAutoConfiguration</code> 注解，用于开启自动配置功能，是 <code>spring-boot-autoconfigure</code> 项目最核心的注解。代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="comment">// EnableAutoConfiguration.java</span></span><br><span class="line"></span><br><span class="line"><span class="meta">@Target</span>(ElementType.TYPE)</span><br><span class="line"><span class="meta">@Retention</span>(RetentionPolicy.RUNTIME)</span><br><span class="line"><span class="meta">@Documented</span></span><br><span class="line"><span class="meta">@Inherited</span></span><br><span class="line"><span class="meta">@AutoConfigurationPackage</span></span><br><span class="line"><span class="meta">@Import</span>(AutoConfigurationImportSelector.class)</span><br><span class="line"><span class="keyword">public</span> <span class="meta">@interface</span> EnableAutoConfiguration {</span><br><span class="line"></span><br><span class="line">	String ENABLED_OVERRIDE_PROPERTY = <span class="string">"spring.boot.enableautoconfiguration"</span>;</span><br><span class="line"></span><br><span class="line">	<span class="comment">/**</span></span><br><span class="line"><span class="comment">	 * Exclude specific auto-configuration classes such that they will never be applied.</span></span><br><span class="line"><span class="comment">	 * <span class="doctag">@return</span> the classes to exclude</span></span><br><span class="line"><span class="comment">	 */</span></span><br><span class="line">	Class&lt;?&gt;[] exclude() <span class="keyword">default</span> {};</span><br><span class="line"></span><br><span class="line">	<span class="comment">/**</span></span><br><span class="line"><span class="comment">	 * Exclude specific auto-configuration class names such that they will never be</span></span><br><span class="line"><span class="comment">	 * applied.</span></span><br><span class="line"><span class="comment">	 * <span class="doctag">@return</span> the class names to exclude</span></span><br><span class="line"><span class="comment">	 * <span class="doctag">@since</span> 1.3.0</span></span><br><span class="line"><span class="comment">	 */</span></span><br><span class="line">	String[] excludeName() <span class="keyword">default</span> {};</span><br><span class="line"></span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li><p><code>org.springframework.boot.autoconfigure.@AutoConfigurationPackage</code> 注解，主要功能自动配置包，它会获取主程序类所在的包路径，并将包路径（包括子包）下的所有组件注册到 Spring IOC 容器中。代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="comment">// AutoConfigurationPackage.java</span></span><br><span class="line"></span><br><span class="line"><span class="meta">@Target</span>(ElementType.TYPE)</span><br><span class="line"><span class="meta">@Retention</span>(RetentionPolicy.RUNTIME)</span><br><span class="line"><span class="meta">@Documented</span></span><br><span class="line"><span class="meta">@Inherited</span></span><br><span class="line"><span class="meta">@Import</span>(AutoConfigurationPackages.Registrar.class)</span><br><span class="line"><span class="keyword">public</span> <span class="meta">@interface</span> AutoConfigurationPackage {</span><br><span class="line"></span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li><code>org.springframework.context.annotation.@Import</code> 注解，可用于资源的导入。情况比较多，可以看看 <a href="https://blog.csdn.net/u010502101/article/details/78760032" rel="external nofollow noopener noreferrer" target="_blank">《6、@Import 注解——导入资源》</a> 文章。</li>
<li>AutoConfigurationPackages.Registrar ，有点神奇，这里先不说。胖友最后去看 <a href="#">「6. AutoConfigurationPackages」</a> 小节。</li>
</ul>
</li>
<li><p><code>@Import(AutoConfigurationImportSelector.class)</code> 注解部分，是重头戏的开始。</p>
<ul>
<li><code>org.springframework.context.annotation.@Import</code> 注解，可用于资源的导入。情况比较多，可以看看 <a href="https://blog.csdn.net/u010502101/article/details/78760032" rel="external nofollow noopener noreferrer" target="_blank">《6、@Import 注解——导入资源》</a> 文章。</li>
<li>AutoConfigurationImportSelector ，导入自动配置相关的资源。详细解析，见 <a href="#">「5. AutoConfigurationImportSelector」</a> 小节。</li>
</ul>
</li>
</ul>
<h1 id="5-AutoConfigurationImportSelector"><a href="#5-AutoConfigurationImportSelector" class="headerlink" title="5. AutoConfigurationImportSelector"></a>5. AutoConfigurationImportSelector</h1><p><code>org.springframework.boot.autoconfigure.AutoConfigurationImportSelector</code> ，实现 DeferredImportSelector、BeanClassLoaderAware、ResourceLoaderAware、BeanFactoryAware、EnvironmentAware、Ordered 接口，处理 <code>@EnableAutoConfiguration</code> 注解的资源导入。</p>
<h2 id="5-1-getCandidateConfigurations"><a href="#5-1-getCandidateConfigurations" class="headerlink" title="5.1 getCandidateConfigurations"></a>5.1 getCandidateConfigurations</h2><p><code>#getCandidateConfigurations(AnnotationMetadata metadata, AnnotationAttributes attributes)</code> 方法，获得符合条件的配置类的数组。代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="comment">// AutoConfigurationImportSelector.java</span></span><br><span class="line"></span><br><span class="line"><span class="function"><span class="keyword">protected</span> List&lt;String&gt; <span class="title">getCandidateConfigurations</span><span class="params">(AnnotationMetadata metadata, AnnotationAttributes attributes)</span> </span>{</span><br><span class="line">    <span class="comment">// &lt;1&gt; 加载指定类型 EnableAutoConfiguration 对应的，在 `META-INF/spring.factories` 里的类名的数组</span></span><br><span class="line">	List&lt;String&gt; configurations = SpringFactoriesLoader.loadFactoryNames(getSpringFactoriesLoaderFactoryClass(), getBeanClassLoader());</span><br><span class="line">	<span class="comment">// 断言，非空</span></span><br><span class="line">	Assert.notEmpty(configurations, <span class="string">"No auto configuration classes found in META-INF/spring.factories. If you "</span> + <span class="string">"are using a custom packaging, make sure that file is correct."</span>);</span><br><span class="line">	<span class="keyword">return</span> configurations;</span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li><p><code>&lt;1&gt;</code> 处，调用 <code>#getSpringFactoriesLoaderFactoryClass()</code> 方法，获得要从 <code>META-INF/spring.factories</code> 加载的指定类型为 EnableAutoConfiguration 类。代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="comment">// AutoConfigurationImportSelector.java</span></span><br><span class="line"></span><br><span class="line"><span class="keyword">protected</span> Class&lt;?&gt; getSpringFactoriesLoaderFactoryClass() {</span><br><span class="line">	<span class="keyword">return</span> EnableAutoConfiguration.class;</span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
</li>
<li><p><code>&lt;1&gt;</code> 处，调用 <code>SpringFactoriesLoader#loadFactoryNames(Class&lt;?&gt; factoryClass, ClassLoader classLoader)</code> 方法，加载指定类型 EnableAutoConfiguration 对应的，在 <code>META-INF/spring.factories</code> 里的类名的数组。看看下图，胖友相信就明白了：<img src="http://static2.iocoder.cn/images/Spring-Boot/2021-02-01/01.jpg" alt="`configurations`"></p>
</li>
</ul>
<p>一般来说，和网络上 Spring Boot 敢于这块的源码解析，我们就可以结束了。如果单纯是为了了解原理 Spring Boot 自动配置的原理，这里结束也是没问题的。因为，拿到 Configuration 配置类后，后面的就是 Spring Java Config 的事情了。不了解的胖友，可以看看 <a href="http://wiki.jikexueyuan.com/project/spring/java-based-configuration.html" rel="external nofollow noopener noreferrer" target="_blank">《Spring 教程 —— 基于 Java 的配置》</a> 文章。</p>
<p>😜 但是（“但是”同学，你赶紧坐下），具有倒腾精神的艿艿，觉得还是继续瞅瞅 <code>#getCandidateConfigurations(AnnotationMetadata metadata, AnnotationAttributes attributes)</code> 方法是怎么被调用的。所以，我们来看看调用它的方法调用链，如下图所示：<img src="http://static2.iocoder.cn/images/Spring-Boot/2021-02-01/02.jpg" alt="调用链"></p>
<ul>
<li>① 处，refresh 方法的调用，我们在 <a href="http://svip.iocoder.cn/Spring-Boot/SpringApplication">《精尽 Spring Boot 源码分析 —— SpringApplication》</a> 中，SpringApplication 启动时，会调用到该方法。</li>
<li>② 处，<code>#getCandidateConfigurations(AnnotationMetadata metadata, AnnotationAttributes attributes)</code> 方法被调用。</li>
<li><p>③ 处，那么此处，就是问题的关键。代码如下：</p>
<blockquote>
<p>艿艿：因为我还没特别完整的撸完 Spring Java Annotations 相关的源码，所以下面的部分，我们更多是看整个调用过程。😈 恰好，胖友也没看过，哈哈哈哈。 </p>
</blockquote>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="comment">// ConfigurationClassParser#DeferredImportSelectorGroupingHandler.java</span></span><br><span class="line"></span><br><span class="line"><span class="keyword">private</span> <span class="keyword">final</span> DeferredImportSelector.Group group;</span><br><span class="line"></span><br><span class="line"><span class="keyword">public</span> Iterable&lt;Group.Entry&gt; getImports() {</span><br><span class="line">	<span class="keyword">for</span> (DeferredImportSelectorHolder deferredImport : <span class="keyword">this</span>.deferredImports) {</span><br><span class="line">		<span class="keyword">this</span>.group.process(deferredImport.getConfigurationClass().getMetadata(),</span><br><span class="line">				deferredImport.getImportSelector()); <span class="comment">// &lt;1&gt;</span></span><br><span class="line">	}</span><br><span class="line">	<span class="keyword">return</span> <span class="keyword">this</span>.group.selectImports(); <span class="comment">// &lt;2&gt;</span></span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li><code>&lt;1&gt;</code> 处，调用 <code>DeferredImportSelector.Group#process(AnnotationMetadata metadata, DeferredImportSelector selector)</code> 方法，处理被 <code>@Import</code> 注解的注解。</li>
<li><code>&lt;2&gt;</code> 处，调用 <code>DeferredImportSelector.Group#this.group.selectImports()</code> 方法，选择需要导入的。例如：<img src="http://static2.iocoder.cn/images/Spring-Boot/2021-02-01/03.jpg" alt="selectImports"><ul>
<li>这里，我们可以看到需要导入的 Configuration 配置类。 </li>
</ul>
</li>
<li>具体 <code>&lt;1&gt;</code> 和 <code>&lt;2&gt;</code> 处，在 <a href="#">「5.3 AutoConfigurationGroup」</a> 详细解析。</li>
</ul>
</li>
</ul>
<h2 id="5-2-getImportGroup"><a href="#5-2-getImportGroup" class="headerlink" title="5.2 getImportGroup"></a>5.2 getImportGroup</h2><p><code>#getImportGroup()</code> 方法，获得对应的 Group 实现类。代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="comment">// AutoConfigurationImportSelector.java</span></span><br><span class="line"></span><br><span class="line"><span class="meta">@Override</span> <span class="comment">// 实现自 DeferredImportSelector 接口</span></span><br><span class="line"><span class="keyword">public</span> Class&lt;? extends Group&gt; getImportGroup() {</span><br><span class="line">	<span class="keyword">return</span> AutoConfigurationGroup.class;</span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li>关于 AutoConfigurationGroup 类，在 <a href="#">「5.3 AutoConfigurationGroup」</a> 详细解析。</li>
</ul>
<h2 id="5-3-AutoConfigurationGroup"><a href="#5-3-AutoConfigurationGroup" class="headerlink" title="5.3 AutoConfigurationGroup"></a>5.3 AutoConfigurationGroup</h2><blockquote>
<p>艿艿：注意，从这里开始后，东西会比较难。因为，涉及的东西会比较多。</p>
</blockquote>
<p>AutoConfigurationGroup ，是 AutoConfigurationImportSelector 的内部类，实现 DeferredImportSelector.Group、BeanClassLoaderAware、BeanFactoryAware、ResourceLoaderAware 接口，自动配置的 Group 实现类。</p>
<h3 id="5-3-1-属性"><a href="#5-3-1-属性" class="headerlink" title="5.3.1 属性"></a>5.3.1 属性</h3><figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="comment">// AutoConfigurationImportSelector#AutoConfigurationGroup.java</span></span><br><span class="line"></span><br><span class="line"><span class="comment">/**</span></span><br><span class="line"><span class="comment"> * AnnotationMetadata 的映射</span></span><br><span class="line"><span class="comment"> *</span></span><br><span class="line"><span class="comment"> * KEY：配置类的全类名</span></span><br><span class="line"><span class="comment"> */</span></span><br><span class="line"><span class="keyword">private</span> <span class="keyword">final</span> Map&lt;String, AnnotationMetadata&gt; entries = <span class="keyword">new</span> LinkedHashMap&lt;&gt;();</span><br><span class="line"><span class="comment">/**</span></span><br><span class="line"><span class="comment"> * AutoConfigurationEntry 的数组</span></span><br><span class="line"><span class="comment"> */</span></span><br><span class="line"><span class="keyword">private</span> <span class="keyword">final</span> List&lt;AutoConfigurationEntry&gt; autoConfigurationEntries = <span class="keyword">new</span> ArrayList&lt;&gt;();</span><br><span class="line"></span><br><span class="line"><span class="keyword">private</span> ClassLoader beanClassLoader;</span><br><span class="line"><span class="keyword">private</span> BeanFactory beanFactory;</span><br><span class="line"><span class="keyword">private</span> ResourceLoader resourceLoader;</span><br><span class="line"></span><br><span class="line"><span class="comment">/**</span></span><br><span class="line"><span class="comment"> * 自动配置的元数据</span></span><br><span class="line"><span class="comment"> */</span></span><br><span class="line"><span class="keyword">private</span> AutoConfigurationMetadata autoConfigurationMetadata;</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li><code>entries</code> 属性，AnnotationMetadata 的映射。其中，KEY 为 配置类的全类名。在后续我们将看到的 <code>AutoConfigurationGroup#process(...)</code> 方法中，被进行赋值。例如：<img src="http://static2.iocoder.cn/images/Spring-Boot/2021-02-01/04.jpg" alt="`entries`"></li>
<li><p><code>autoConfigurationEntries</code> 属性，AutoConfigurationEntry 的数组。</p>
<ul>
<li><p>其中，AutoConfigurationEntry 是 AutoConfigurationImportSelector 的内部类，自动配置的条目。代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="comment">// AutoConfigurationImportSelector#AutoConfigurationEntry.java</span></span><br><span class="line"></span><br><span class="line"><span class="keyword">protected</span> <span class="keyword">static</span> <span class="class"><span class="keyword">class</span> <span class="title">AutoConfigurationEntry</span> </span>{</span><br><span class="line"></span><br><span class="line">    <span class="comment">/**</span></span><br><span class="line"><span class="comment">     * 配置类的全类名的数组</span></span><br><span class="line"><span class="comment">     */</span></span><br><span class="line">    <span class="keyword">private</span> <span class="keyword">final</span> List&lt;String&gt; configurations;</span><br><span class="line">    <span class="comment">/**</span></span><br><span class="line"><span class="comment">     * 排除的配置类的全类名的数组 </span></span><br><span class="line"><span class="comment">     */</span></span><br><span class="line">    <span class="keyword">private</span> <span class="keyword">final</span> Set&lt;String&gt; exclusions;</span><br><span class="line">    </span><br><span class="line">    <span class="comment">// 省略构造方法和 setting/getting 方法</span></span><br><span class="line">    </span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li>属性比较简单。</li>
</ul>
</li>
<li>在后续我们将看到的 <code>AutoConfigurationGroup#process(...)</code> 方法中，被进行赋值。例如：<img src="http://static2.iocoder.cn/images/Spring-Boot/2021-02-01/05.jpg" alt="`autoConfigurationEntries`"></li>
</ul>
</li>
<li><p><code>autoConfigurationMetadata</code> 属性，自动配置的元数据（Metadata）。</p>
<ul>
<li><p>通过 <code>#getAutoConfigurationMetadata()</code> 方法，会初始化该属性。代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="comment">// AutoConfigurationImportSelector#AutoConfigurationGroup.java</span></span><br><span class="line"></span><br><span class="line"><span class="function"><span class="keyword">private</span> AutoConfigurationMetadata <span class="title">getAutoConfigurationMetadata</span><span class="params">()</span> </span>{</span><br><span class="line">    <span class="comment">// 不存在，则进行加载</span></span><br><span class="line">	<span class="keyword">if</span> (<span class="keyword">this</span>.autoConfigurationMetadata == <span class="keyword">null</span>) {</span><br><span class="line">		<span class="keyword">this</span>.autoConfigurationMetadata = AutoConfigurationMetadataLoader.loadMetadata(<span class="keyword">this</span>.beanClassLoader);</span><br><span class="line">	}</span><br><span class="line">	<span class="comment">// 存在，则直接返回</span></span><br><span class="line">	<span class="keyword">return</span> <span class="keyword">this</span>.autoConfigurationMetadata;</span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li>关于 AutoConfigurationMetadataLoader 类，我们先不去愁。避免，我们调试的太过深入。TODO 后续在补充下。</li>
<li>返回的类型是 PropertiesAutoConfigurationMetadata ，比较简单，胖友点击 <a href="https://github.com/YunaiV/spring-boot/blob/master/spring-boot-project/spring-boot-autoconfigure/src/main/java/org/springframework/boot/autoconfigure/AutoConfigurationMetadataLoader.java#L67-L119" rel="external nofollow noopener noreferrer" target="_blank">传送门</a> 瞅一眼即可。</li>
<li>如下是一个返回值的示例：<img src="http://static2.iocoder.cn/images/Spring-Boot/2021-02-01/06.jpg" alt="`autoConfigurationEntries`"><ul>
<li>可能胖友会有点懵逼，这么多，并且 KEY / VALUE 结果看不懂？不要方，我们简单来说下 <a href="https://github.com/YunaiV/spring-boot/blob/master/spring-boot-project/spring-boot-autoconfigure/src/main/java/org/springframework/boot/autoconfigure/data/couchbase/CouchbaseReactiveRepositoriesAutoConfiguration.java" rel="external nofollow noopener noreferrer" target="_blank">CouchbaseReactiveRepositoriesAutoConfiguration</a> 配置类。如果它生效，需要 classpath 下有 Bucket、ReactiveCouchbaseRepository、Flux 三个类，所以红线那个条目，对应的就是 CouchbaseReactiveRepositoriesAutoConfiguration 类上的 <code>@ConditionalOnClass({ Bucket.class, ReactiveCouchbaseRepository.class, Flux.class })</code> 注解部分。</li>
</ul>
</li>
</ul>
</li>
<li>所以，<code>autoConfigurationMetadata</code> 属性，用途就是制定配置类（Configuration）的生效条件（Condition）。</li>
</ul>
</li>
</ul>
<h3 id="5-3-2-process"><a href="#5-3-2-process" class="headerlink" title="5.3.2 process"></a>5.3.2 process</h3><p><code>#process(AnnotationMetadata annotationMetadata, DeferredImportSelector deferredImportSelector)</code> 方法，进行处理。代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="comment">// AutoConfigurationImportSelector#AutoConfigurationGroup.java</span></span><br><span class="line"></span><br><span class="line"><span class="meta">@Override</span></span><br><span class="line"><span class="function"><span class="keyword">public</span> <span class="keyword">void</span> <span class="title">process</span><span class="params">(AnnotationMetadata annotationMetadata, DeferredImportSelector deferredImportSelector)</span> </span>{</span><br><span class="line">    <span class="comment">// 断言</span></span><br><span class="line">    Assert.state(</span><br><span class="line">            deferredImportSelector <span class="keyword">instanceof</span> AutoConfigurationImportSelector,</span><br><span class="line">            () -&gt; String.format(<span class="string">"Only %s implementations are supported, got %s"</span>,</span><br><span class="line">                    AutoConfigurationImportSelector.class.getSimpleName(),</span><br><span class="line">                    deferredImportSelector.getClass().getName()));</span><br><span class="line">    <span class="comment">// &lt;1&gt; 获得 AutoConfigurationEntry 对象 </span></span><br><span class="line">    AutoConfigurationEntry autoConfigurationEntry = ((AutoConfigurationImportSelector) deferredImportSelector)</span><br><span class="line">            .getAutoConfigurationEntry(getAutoConfigurationMetadata(), annotationMetadata);</span><br><span class="line">    <span class="comment">// &lt;2&gt; 添加到 autoConfigurationEntries 中</span></span><br><span class="line">    <span class="keyword">this</span>.autoConfigurationEntries.add(autoConfigurationEntry);</span><br><span class="line">    <span class="comment">// &lt;3&gt; 添加到 entries 中</span></span><br><span class="line">    <span class="keyword">for</span> (String importClassName : autoConfigurationEntry.getConfigurations()) {</span><br><span class="line">        <span class="keyword">this</span>.entries.putIfAbsent(importClassName, annotationMetadata);</span><br><span class="line">    }</span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li><code>annotationMetadata</code> 参数，一般来说是被 <code>@SpringBootApplication</code> 注解的元数据。因为，<code>@SpringBootApplication</code> 组合了 <code>@EnableAutoConfiguration</code> 注解。</li>
<li><code>deferredImportSelector</code> 参数，<code>@EnableAutoConfiguration</code> 注解的定义的 <code>@Import</code> 的类，即 AutoConfigurationImportSelector 对象。</li>
<li><code>&lt;1&gt;</code> 处，调用 <code>AutoConfigurationImportSelector#getAutoConfigurationEntry(AutoConfigurationMetadata autoConfigurationMetadata, AnnotationMetadata annotationMetadata)</code> 方法，获得 AutoConfigurationEntry 对象。详细解析，见 <a href="#">「5.4 AutoConfigurationEntry」</a> 。因为这块比较重要，所以先跳过去瞅瞅。</li>
<li><code>&lt;2&gt;</code> 处，添加到 <code>autoConfigurationEntries</code> 中。</li>
<li><code>&lt;3&gt;</code> 处，添加到 <code>entries</code> 中。</li>
</ul>
<h3 id="5-3-3-selectImports"><a href="#5-3-3-selectImports" class="headerlink" title="5.3.3 selectImports"></a>5.3.3 selectImports</h3><p><code>#selectImports()</code> 方法，获得要引入的配置类。代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="comment">// AutoConfigurationImportSelector#AutoConfigurationGroup.java</span></span><br><span class="line"></span><br><span class="line"><span class="meta">@Override</span></span><br><span class="line"><span class="function"><span class="keyword">public</span> Iterable&lt;Entry&gt; <span class="title">selectImports</span><span class="params">()</span> </span>{</span><br><span class="line">    <span class="comment">// &lt;1&gt; 如果为空，则返回空数组</span></span><br><span class="line">    <span class="keyword">if</span> (<span class="keyword">this</span>.autoConfigurationEntries.isEmpty()) {</span><br><span class="line">        <span class="keyword">return</span> Collections.emptyList();</span><br><span class="line">    }</span><br><span class="line">    <span class="comment">// &lt;2.1&gt; 获得 allExclusions</span></span><br><span class="line">    Set&lt;String&gt; allExclusions = <span class="keyword">this</span>.autoConfigurationEntries.stream()</span><br><span class="line">            .map(AutoConfigurationEntry::getExclusions)</span><br><span class="line">            .flatMap(Collection::stream).collect(Collectors.toSet());</span><br><span class="line">    <span class="comment">// &lt;2.2&gt; 获得 processedConfigurations</span></span><br><span class="line">    Set&lt;String&gt; processedConfigurations = <span class="keyword">this</span>.autoConfigurationEntries.stream()</span><br><span class="line">            .map(AutoConfigurationEntry::getConfigurations)</span><br><span class="line">            .flatMap(Collection::stream)</span><br><span class="line">            .collect(Collectors.toCollection(LinkedHashSet::<span class="keyword">new</span>));</span><br><span class="line">    <span class="comment">// &lt;2.3&gt; 从 processedConfigurations 中，移除排除的</span></span><br><span class="line">    processedConfigurations.removeAll(allExclusions);</span><br><span class="line">    <span class="comment">// &lt;3&gt; 处理，返回结果</span></span><br><span class="line">    <span class="keyword">return</span> sortAutoConfigurations(processedConfigurations, getAutoConfigurationMetadata()) <span class="comment">// &lt;3.1&gt; 排序</span></span><br><span class="line">                .stream()</span><br><span class="line">                .map((importClassName) -&gt; <span class="keyword">new</span> Entry(<span class="keyword">this</span>.entries.get(importClassName), importClassName)) <span class="comment">// &lt;3.2&gt; 创建 Entry 对象</span></span><br><span class="line">                .collect(Collectors.toList()); <span class="comment">// &lt;3.3&gt; 转换成 List</span></span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li><code>&lt;1&gt;</code> 处，如果为空，则返回空数组。</li>
<li><code>&lt;2.1&gt;</code>、<code>&lt;2.2&gt;</code>、<code>&lt;2.3&gt;</code> 处，获得要引入的配置类集合。😈 比较奇怪的是，上面已经做过一次移除的处理，这里又做一次。不过，没多大关系，可以先无视。</li>
<li><p><code>&lt;3&gt;</code> 处，处理，返回结果。</p>
<ul>
<li><p><code>&lt;3.1&gt;</code> 处，调用 <code>#sortAutoConfigurations(Set&lt;String&gt; configurations, AutoConfigurationMetadata autoConfigurationMetadata)</code> 方法，排序。代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="comment">// AutoConfigurationImportSelector#AutoConfigurationGroup.java</span></span><br><span class="line"></span><br><span class="line"><span class="function"><span class="keyword">private</span> List&lt;String&gt; <span class="title">sortAutoConfigurations</span><span class="params">(Set&lt;String&gt; configurations, AutoConfigurationMetadata autoConfigurationMetadata)</span> </span>{</span><br><span class="line">	<span class="keyword">return</span> <span class="keyword">new</span> AutoConfigurationSorter(getMetadataReaderFactory(), autoConfigurationMetadata).getInPriorityOrder(configurations);</span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li>具体的排序逻辑，胖友自己看。实际上，还是涉及哪些，例如说 <code>@Order</code> 注解。</li>
</ul>
</li>
<li><code>&lt;3.2&gt;</code> 处，创建 Entry 对象。</li>
<li><code>&lt;3.3&gt;</code> 处，转换成 List 。结果如下图：<img src="http://static2.iocoder.cn/images/Spring-Boot/2021-02-01/09.jpg" alt="结果"></li>
</ul>
</li>
</ul>
<blockquote>
<p>艿艿：略微有点艰难的过程。不过回过头来，其实也没啥特别复杂的逻辑。是不，胖友~</p>
</blockquote>
<h2 id="5-4-getAutoConfigurationEntry"><a href="#5-4-getAutoConfigurationEntry" class="headerlink" title="5.4 getAutoConfigurationEntry"></a>5.4 getAutoConfigurationEntry</h2><blockquote>
<p>艿艿：这是一个关键方法。因为会调用到，我们会在 <a href="#">「5.1 getCandidateConfigurations」</a> 的方法。</p>
</blockquote>
<p><code>#getAutoConfigurationEntry(AutoConfigurationMetadata autoConfigurationMetadata, AnnotationMetadata annotationMetadata)</code> 方法，获得 AutoConfigurationEntry 对象。代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="comment">// AutoConfigurationImportSelector.java</span></span><br><span class="line"></span><br><span class="line"><span class="function"><span class="keyword">protected</span> AutoConfigurationEntry <span class="title">getAutoConfigurationEntry</span><span class="params">(AutoConfigurationMetadata autoConfigurationMetadata, AnnotationMetadata annotationMetadata)</span> </span>{</span><br><span class="line">    <span class="comment">// &lt;1&gt; 判断是否开启。如未开启，返回空数组。</span></span><br><span class="line">    <span class="keyword">if</span> (!isEnabled(annotationMetadata)) {</span><br><span class="line">        <span class="keyword">return</span> EMPTY_ENTRY;</span><br><span class="line">    }</span><br><span class="line">    <span class="comment">// &lt;2&gt; 获得注解的属性</span></span><br><span class="line">    AnnotationAttributes attributes = getAttributes(annotationMetadata);</span><br><span class="line">    <span class="comment">// &lt;3&gt; 获得符合条件的配置类的数组</span></span><br><span class="line">    List&lt;String&gt; configurations = getCandidateConfigurations(annotationMetadata, attributes);</span><br><span class="line">    <span class="comment">// &lt;3.1&gt; 移除重复的配置类</span></span><br><span class="line">    configurations = removeDuplicates(configurations);</span><br><span class="line">    <span class="comment">// &lt;4&gt; 获得需要排除的配置类</span></span><br><span class="line">    Set&lt;String&gt; exclusions = getExclusions(annotationMetadata, attributes);</span><br><span class="line">    <span class="comment">// &lt;4.1&gt; 校验排除的配置类是否合法</span></span><br><span class="line">    checkExcludedClasses(configurations, exclusions);</span><br><span class="line">    <span class="comment">// &lt;4.2&gt; 从 configurations 中，移除需要排除的配置类</span></span><br><span class="line">    configurations.removeAll(exclusions);</span><br><span class="line">    <span class="comment">// &lt;5&gt; 根据条件（Condition），过滤掉不符合条件的配置类</span></span><br><span class="line">    configurations = filter(configurations, autoConfigurationMetadata);</span><br><span class="line">    <span class="comment">// &lt;6&gt; 触发自动配置类引入完成的事件</span></span><br><span class="line">    fireAutoConfigurationImportEvents(configurations, exclusions);</span><br><span class="line">    <span class="comment">// &lt;7&gt; 创建 AutoConfigurationEntry 对象</span></span><br><span class="line">    <span class="keyword">return</span> <span class="keyword">new</span> AutoConfigurationEntry(configurations, exclusions);</span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<blockquote>
<p>这里每一步都是细节的方法，所以会每一个方法，都会是引导到对应的小节的方法。</p>
<p>虽然有点长，但是很不复杂。简单的来说，加载符合条件的配置类们，然后移除需要排除（exclusion）的。</p>
</blockquote>
<ul>
<li><code>&lt;1&gt;</code> 处，调用 <code>#isEnabled(AnnotationMetadata metadata)</code> 方法，判断是否开启。如未开启，返回空数组。详细解析，见 <a href="#">「5.4.1 isEnabled」</a> 。</li>
<li><code>&lt;2&gt;</code> 处，调用 <code>#getAttributes(AnnotationMetadata metadata)</code> 方法，获得注解的属性。详细解析，见 <a href="#">「5.4.2 getAttributes」</a> 。</li>
<li><p>【重要】<code>&lt;3&gt;</code> 处，调用 <code>#getCandidateConfigurations(AnnotationMetadata metadata, AnnotationAttributes attributes)</code> 方法，获得符合条件的配置类的数组。</p>
<blockquote>
<p>嘻嘻，到达此书之后，整个细节是不是就串起来了!</p>
</blockquote>
<ul>
<li><p><code>&lt;3.1&gt;</code> 处，调用 <code>#removeDuplicates(List&lt;T&gt; list)</code> 方法，移除重复的配置类。代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="comment">// AutoConfigurationImportSelector.java</span></span><br><span class="line"></span><br><span class="line"><span class="keyword">protected</span> <span class="keyword">final</span> &lt;T&gt; <span class="function">List&lt;T&gt; <span class="title">removeDuplicates</span><span class="params">(List&lt;T&gt; list)</span> </span>{</span><br><span class="line">	<span class="keyword">return</span> <span class="keyword">new</span> ArrayList&lt;&gt;(<span class="keyword">new</span> LinkedHashSet&lt;&gt;(list));</span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li>简单粗暴</li>
</ul>
</li>
</ul>
</li>
<li><p><code>&lt;4&gt;</code> 处，调用 <code>#getExclusions(AnnotationMetadata metadata, AnnotationAttributes attributes)</code> 方法，获得需要排除的配置类。详细解析，见 <a href="#">「5.4.3 getExclusions」</a> 。</p>
<ul>
<li><code>&lt;4.1&gt;</code> 处，调用 <code>#checkExcludedClasses(List&lt;String&gt; configurations, Set&lt;String&gt; exclusions)</code> 方法，校验排除的配置类是否合法。详细解析，见 <a href="#">「5.4.4 checkExcludedClasses」</a> 。</li>
<li><code>&lt;4.2&gt;</code> 处，从 <code>configurations</code> 中，移除需要排除的配置类。</li>
</ul>
</li>
<li><p><code>&lt;5&gt;</code> 处，调用 <code>#filter(List&lt;String&gt; configurations, AutoConfigurationMetadata autoConfigurationMetadata)</code> 方法，根据条件（Condition），过滤掉不符合条件的配置类。详细解析，见 <a href="http://svip.iocoder.cn/Spring-Boot/Condition/">《精尽 Spring Boot 源码分析 —— Condition》</a> 文章。</p>
</li>
<li><code>&lt;6&gt;</code> 处，调用 <code>#fireAutoConfigurationImportEvents(List&lt;String&gt; configurations, Set&lt;String&gt; exclusions)</code> 方法，触发自动配置类引入完成的事件。详细解析，见 <a href="#">「5.4.5 fireAutoConfigurationImportEvents」</a> 。</li>
<li><code>&lt;7&gt;</code> 处，创建 AutoConfigurationEntry 对象。</li>
</ul>
<blockquote>
<p>整个 <a href="#">「5.4 getAutoConfigurationEntry」</a> 看完后，胖友请跳回 <a href="#">「5.3.3 selectImports」</a> 。 </p>
</blockquote>
<h3 id="5-4-1-isEnabled"><a href="#5-4-1-isEnabled" class="headerlink" title="5.4.1 isEnabled"></a>5.4.1 isEnabled</h3><p><code>#isEnabled(AnnotationMetadata metadata)</code> 方法，判断是否开启自动配置。代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="comment">// AutoConfigurationImportSelector.java</span></span><br><span class="line"></span><br><span class="line"><span class="function"><span class="keyword">protected</span> <span class="keyword">boolean</span> <span class="title">isEnabled</span><span class="params">(AnnotationMetadata metadata)</span> </span>{</span><br><span class="line">    <span class="comment">// 判断 "spring.boot.enableautoconfiguration" 配置判断，是否开启自动配置。</span></span><br><span class="line">    <span class="comment">// 默认情况下（未配置），开启自动配置。</span></span><br><span class="line">	<span class="keyword">if</span> (getClass() == AutoConfigurationImportSelector.class) {</span><br><span class="line">		<span class="keyword">return</span> getEnvironment().getProperty(EnableAutoConfiguration.ENABLED_OVERRIDE_PROPERTY, Boolean.class, <span class="keyword">true</span>);</span><br><span class="line">	}</span><br><span class="line">	<span class="keyword">return</span> <span class="keyword">true</span>;</span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<h3 id="5-4-2-getAttributes"><a href="#5-4-2-getAttributes" class="headerlink" title="5.4.2 getAttributes"></a>5.4.2 getAttributes</h3><p><code>#getAttributes(AnnotationMetadata metadata)</code> 方法，获得注解的属性。代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="comment">// AutoConfigurationImportSelector.java</span></span><br><span class="line"></span><br><span class="line"><span class="function"><span class="keyword">protected</span> AnnotationAttributes <span class="title">getAttributes</span><span class="params">(AnnotationMetadata metadata)</span> </span>{</span><br><span class="line">	String name = getAnnotationClass().getName();</span><br><span class="line">	<span class="comment">// 获得注解的属性</span></span><br><span class="line">	AnnotationAttributes attributes = AnnotationAttributes.fromMap(metadata.getAnnotationAttributes(name, <span class="keyword">true</span>));</span><br><span class="line">	<span class="comment">// 断言</span></span><br><span class="line">	Assert.notNull(attributes,</span><br><span class="line">			() -&gt; <span class="string">"No auto-configuration attributes found. Is "</span></span><br><span class="line">					+ metadata.getClassName() + <span class="string">" annotated with "</span></span><br><span class="line">					+ ClassUtils.getShortName(name) + <span class="string">"?"</span>);</span><br><span class="line">	<span class="keyword">return</span> attributes;</span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li>注意，此处 <code>getAnnotationClass().getName()</code> 返回的是 <code>@EnableAutoConfiguration</code> 注解，所以这里返回的注解属性，只能是 <code>exclude</code> 和 <code>excludeName</code> 这两个。</li>
<li><p>举个例子，假设 Spring 应用上的注解如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="meta">@SpringBootApplication</span>(exclude = {SpringApplicationAdminJmxAutoConfiguration.class},</span><br><span class="line">    scanBasePackages = <span class="string">"cn.iocoder"</span>)</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li>返回的结果，如下图：<img src="http://static2.iocoder.cn/images/Spring-Boot/2021-02-01/07.jpg" alt="`attributes`"></li>
</ul>
</li>
</ul>
<h3 id="5-4-3-getExclusions"><a href="#5-4-3-getExclusions" class="headerlink" title="5.4.3 getExclusions"></a>5.4.3 getExclusions</h3><p><code>#getExclusions(AnnotationMetadata metadata, AnnotationAttributes attributes)</code> 方法，获得需要排除的配置类。代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="comment">// AutoConfigurationImportSelector.java</span></span><br><span class="line"></span><br><span class="line"><span class="function"><span class="keyword">protected</span> Set&lt;String&gt; <span class="title">getExclusions</span><span class="params">(AnnotationMetadata metadata, AnnotationAttributes attributes)</span> </span>{</span><br><span class="line">	Set&lt;String&gt; excluded = <span class="keyword">new</span> LinkedHashSet&lt;&gt;();</span><br><span class="line">	<span class="comment">// 注解上的 exclude 属性</span></span><br><span class="line">	excluded.addAll(asList(attributes, <span class="string">"exclude"</span>));</span><br><span class="line">	<span class="comment">// 注解上的 excludeName 属性</span></span><br><span class="line">	excluded.addAll(Arrays.asList(attributes.getStringArray(<span class="string">"excludeName"</span>)));</span><br><span class="line">	<span class="comment">// 配置文件的 spring.autoconfigure.exclude 属性</span></span><br><span class="line">	excluded.addAll(getExcludeAutoConfigurationsProperty());</span><br><span class="line">	<span class="keyword">return</span> excluded;</span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li>一共有三种方式，配置排除属性。</li>
<li><p>该方法会调用如下的方法，比较简单，胖友自己瞅瞅。</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="comment">// AutoConfigurationImportSelector.java</span></span><br><span class="line"></span><br><span class="line"><span class="function"><span class="keyword">private</span> List&lt;String&gt; <span class="title">getExcludeAutoConfigurationsProperty</span><span class="params">()</span> </span>{</span><br><span class="line">    <span class="comment">// 一般来说，会走这块的逻辑</span></span><br><span class="line">	<span class="keyword">if</span> (getEnvironment() <span class="keyword">instanceof</span> ConfigurableEnvironment) {</span><br><span class="line">		Binder binder = Binder.get(getEnvironment());</span><br><span class="line">		<span class="keyword">return</span> binder.bind(PROPERTY_NAME_AUTOCONFIGURE_EXCLUDE, String[].class).map(Arrays::asList).orElse(Collections.emptyList());</span><br><span class="line">	}</span><br><span class="line">	String[] excludes = getEnvironment().getProperty(PROPERTY_NAME_AUTOCONFIGURE_EXCLUDE, String[].class);</span><br><span class="line">	<span class="keyword">return</span> (excludes != <span class="keyword">null</span>) ? Arrays.asList(excludes) : Collections.emptyList();</span><br><span class="line">}</span><br><span class="line"></span><br><span class="line"><span class="function"><span class="keyword">protected</span> <span class="keyword">final</span> List&lt;String&gt; <span class="title">asList</span><span class="params">(AnnotationAttributes attributes, String name)</span> </span>{</span><br><span class="line">	String[] value = attributes.getStringArray(name);</span><br><span class="line">	<span class="keyword">return</span> Arrays.asList(value);</span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
</li>
</ul>
<h3 id="5-4-4-checkExcludedClasses"><a href="#5-4-4-checkExcludedClasses" class="headerlink" title="5.4.4 checkExcludedClasses"></a>5.4.4 checkExcludedClasses</h3><p><code>#checkExcludedClasses(List&lt;String&gt; configurations, Set&lt;String&gt; exclusions)</code> 方法，校验排除的配置类是否合法。代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="comment">// AutoConfigurationImportSelector.java</span></span><br><span class="line"></span><br><span class="line"><span class="function"><span class="keyword">private</span> <span class="keyword">void</span> <span class="title">checkExcludedClasses</span><span class="params">(List&lt;String&gt; configurations, Set&lt;String&gt; exclusions)</span> </span>{</span><br><span class="line">    <span class="comment">// 获得 exclusions 不在 invalidExcludes 的集合，添加到 invalidExcludes 中</span></span><br><span class="line">    List&lt;String&gt; invalidExcludes = <span class="keyword">new</span> ArrayList&lt;&gt;(exclusions.size());</span><br><span class="line">    <span class="keyword">for</span> (String exclusion : exclusions) {</span><br><span class="line">        <span class="keyword">if</span> (ClassUtils.isPresent(exclusion, getClass().getClassLoader()) <span class="comment">// classpath 存在该类</span></span><br><span class="line">                &amp;&amp; !configurations.contains(exclusion)) { <span class="comment">// configurations 不存在该类</span></span><br><span class="line">            invalidExcludes.add(exclusion);</span><br><span class="line">        }</span><br><span class="line">    }</span><br><span class="line">    <span class="comment">// 如果 invalidExcludes 非空，抛出 IllegalStateException 异常</span></span><br><span class="line">    <span class="keyword">if</span> (!invalidExcludes.isEmpty()) {</span><br><span class="line">        handleInvalidExcludes(invalidExcludes);</span><br><span class="line">    }</span><br><span class="line">}</span><br><span class="line"></span><br><span class="line"><span class="comment">/**</span></span><br><span class="line"><span class="comment"> * Handle any invalid excludes that have been specified.</span></span><br><span class="line"><span class="comment"> * <span class="doctag">@param</span> invalidExcludes the list of invalid excludes (will always have at least one</span></span><br><span class="line"><span class="comment"> * element)</span></span><br><span class="line"><span class="comment"> */</span></span><br><span class="line"><span class="function"><span class="keyword">protected</span> <span class="keyword">void</span> <span class="title">handleInvalidExcludes</span><span class="params">(List&lt;String&gt; invalidExcludes)</span> </span>{</span><br><span class="line">    StringBuilder message = <span class="keyword">new</span> StringBuilder();</span><br><span class="line">    <span class="keyword">for</span> (String exclude : invalidExcludes) {</span><br><span class="line">        message.append(<span class="string">"\t- "</span>).append(exclude).append(String.format(<span class="string">"%n"</span>));</span><br><span class="line">    }</span><br><span class="line">    <span class="keyword">throw</span> <span class="keyword">new</span> IllegalStateException(String.format(<span class="string">"The following classes could not be excluded because they are"</span></span><br><span class="line">                    + <span class="string">" not auto-configuration classes:%n%s"</span>, message));</span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li>不合法的定义，<code>exclusions</code> 存在于 classpath 中，但是不存在 <code>configurations</code> 。这样做的目的是，如果不存在的，就不要去排除啦！</li>
<li>代码比较简单，胖友自己瞅瞅即可。</li>
</ul>
<h3 id="5-4-5-fireAutoConfigurationImportEvents"><a href="#5-4-5-fireAutoConfigurationImportEvents" class="headerlink" title="5.4.5 fireAutoConfigurationImportEvents"></a>5.4.5 fireAutoConfigurationImportEvents</h3><p><code>#fireAutoConfigurationImportEvents(List&lt;String&gt; configurations, Set&lt;String&gt; exclusions)</code> 方法，触发自动配置类引入完成的事件。代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="comment">// AutoConfigurationImportSelector.java</span></span><br><span class="line"></span><br><span class="line"><span class="function"><span class="keyword">private</span> <span class="keyword">void</span> <span class="title">fireAutoConfigurationImportEvents</span><span class="params">(List&lt;String&gt; configurations, Set&lt;String&gt; exclusions)</span> </span>{</span><br><span class="line">    <span class="comment">// &lt;1&gt; 加载指定类型 AutoConfigurationImportListener 对应的，在 `META-INF/spring.factories` 里的类名的数组。</span></span><br><span class="line">	List&lt;AutoConfigurationImportListener&gt; listeners = getAutoConfigurationImportListeners();</span><br><span class="line">	<span class="keyword">if</span> (!listeners.isEmpty()) {</span><br><span class="line">	    <span class="comment">// &lt;2&gt; 创建 AutoConfigurationImportEvent 事件</span></span><br><span class="line">		AutoConfigurationImportEvent event = <span class="keyword">new</span> AutoConfigurationImportEvent(<span class="keyword">this</span>, configurations, exclusions);</span><br><span class="line">		<span class="comment">// &lt;3&gt; 遍历 AutoConfigurationImportListener 监听器们，逐个通知</span></span><br><span class="line">		<span class="keyword">for</span> (AutoConfigurationImportListener listener : listeners) {</span><br><span class="line">		    <span class="comment">// &lt;3.1&gt; 设置 AutoConfigurationImportListener 的属性</span></span><br><span class="line">			invokeAwareMethods(listener);</span><br><span class="line">			<span class="comment">// &lt;3.2&gt; 通知</span></span><br><span class="line">			listener.onAutoConfigurationImportEvent(event);</span><br><span class="line">		}</span><br><span class="line">	}</span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li><code>&lt;1&gt;</code> 处，调用 <code>#getAutoConfigurationImportListeners()</code> 方法，加载指定类型 AutoConfigurationImportListener 对应的，在 <code>META-INF/spring.factories</code> 里的类名的数组。例如：<img src="http://static2.iocoder.cn/images/Spring-Boot/2021-02-01/08.jpg" alt="`listeners`"></li>
<li><code>&lt;2&gt;</code> 处，创建 AutoConfigurationImportEvent 事件。</li>
<li><p><code>&lt;3&gt;</code> 处，遍历 AutoConfigurationImportListener 监听器们，逐个通知。</p>
<ul>
<li><p><code>&lt;3.1&gt;</code> 处，调用 <code>#invokeAwareMethods(Object instance)</code> 方法，设置 AutoConfigurationImportListener 的属性。代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="comment">// AutoConfigurationImportSelector.java</span></span><br><span class="line"></span><br><span class="line"><span class="function"><span class="keyword">private</span> <span class="keyword">void</span> <span class="title">invokeAwareMethods</span><span class="params">(Object instance)</span> </span>{</span><br><span class="line">    <span class="comment">// 各种 Aware 属性的注入</span></span><br><span class="line">	<span class="keyword">if</span> (instance <span class="keyword">instanceof</span> Aware) {</span><br><span class="line">		<span class="keyword">if</span> (instance <span class="keyword">instanceof</span> BeanClassLoaderAware) {</span><br><span class="line">			((BeanClassLoaderAware) instance).setBeanClassLoader(<span class="keyword">this</span>.beanClassLoader);</span><br><span class="line">		}</span><br><span class="line">		<span class="keyword">if</span> (instance <span class="keyword">instanceof</span> BeanFactoryAware) {</span><br><span class="line">			((BeanFactoryAware) instance).setBeanFactory(<span class="keyword">this</span>.beanFactory);</span><br><span class="line">		}</span><br><span class="line">		<span class="keyword">if</span> (instance <span class="keyword">instanceof</span> EnvironmentAware) {</span><br><span class="line">			((EnvironmentAware) instance).setEnvironment(<span class="keyword">this</span>.environment);</span><br><span class="line">		}</span><br><span class="line">		<span class="keyword">if</span> (instance <span class="keyword">instanceof</span> ResourceLoaderAware) {</span><br><span class="line">			((ResourceLoaderAware) instance).setResourceLoader(<span class="keyword">this</span>.resourceLoader);</span><br><span class="line">		}</span><br><span class="line">	}</span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li>各种 Aware 属性的注入。</li>
</ul>
</li>
<li><p><code>&lt;3.2&gt;</code> 处，调用 <code>AutoConfigurationImportListener#onAutoConfigurationImportEvent(event)</code> 方法，通知监听器。目前只有一个 ConditionEvaluationReportAutoConfigurationImportListener  监听器，没啥逻辑，有兴趣自己看哈。</p>
</li>
</ul>
</li>
</ul>
<h1 id="6-AutoConfigurationPackages"><a href="#6-AutoConfigurationPackages" class="headerlink" title="6. AutoConfigurationPackages"></a>6. AutoConfigurationPackages</h1><p><code>org.springframework.boot.autoconfigure.AutoConfigurationPackages</code> ，自动配置所在的包名。可能这么解释有点怪怪的，我们来看下官方注释：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line">Class <span class="keyword">for</span> storing auto-<span class="function">configuration packages <span class="keyword">for</span> reference <span class="title">later</span> <span class="params">(e.g. by JPA entity scanner)</span>.</span></span><br></pre></td></tr></tbody></table></figure>
<ul>
<li>简单来说，就是将使用 <code>@AutoConfigurationPackage</code> 注解的类所在的包（<code>package</code>），注册成一个 Spring IoC 容器中的 Bean 。酱紫，后续有其它模块需要使用，就可以通过获得该 Bean ，从而获得所在的包。例如说，JPA 模块，需要使用到。</li>
</ul>
<p>是不是有点神奇，艿艿也觉得。</p>
<h2 id="6-1-Registrar"><a href="#6-1-Registrar" class="headerlink" title="6.1 Registrar"></a>6.1 Registrar</h2><p>Registrar ，是 AutoConfigurationPackages 的内部类，实现 ImportBeanDefinitionRegistrar、DeterminableImports 接口，注册器，用于处理 <code>@AutoConfigurationPackage</code> 注解。代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="comment">// AutoConfigurationPackages#Registrar.java</span></span><br><span class="line"></span><br><span class="line"><span class="keyword">static</span> <span class="class"><span class="keyword">class</span> <span class="title">Registrar</span> <span class="keyword">implements</span> <span class="title">ImportBeanDefinitionRegistrar</span>, <span class="title">DeterminableImports</span> </span>{</span><br><span class="line"></span><br><span class="line">	<span class="meta">@Override</span></span><br><span class="line">	<span class="function"><span class="keyword">public</span> <span class="keyword">void</span> <span class="title">registerBeanDefinitions</span><span class="params">(AnnotationMetadata metadata, BeanDefinitionRegistry registry)</span> </span>{</span><br><span class="line">		register(registry, <span class="keyword">new</span> PackageImport(metadata).getPackageName()); <span class="comment">// &lt;X&gt;</span></span><br><span class="line">	}</span><br><span class="line"></span><br><span class="line">	<span class="meta">@Override</span></span><br><span class="line">	<span class="function"><span class="keyword">public</span> Set&lt;Object&gt; <span class="title">determineImports</span><span class="params">(AnnotationMetadata metadata)</span> </span>{</span><br><span class="line">		<span class="keyword">return</span> Collections.singleton(<span class="keyword">new</span> PackageImport(metadata));</span><br><span class="line">	}</span><br><span class="line"></span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li><p>PackageImport 是 AutoConfigurationPackages 的内部类，用于获得包名。代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="comment">// AutoConfigurationPackages#Registrar.java</span></span><br><span class="line"></span><br><span class="line"><span class="keyword">private</span> <span class="keyword">static</span> <span class="keyword">final</span> <span class="class"><span class="keyword">class</span> <span class="title">PackageImport</span> </span>{</span><br><span class="line"></span><br><span class="line">    <span class="comment">/**</span></span><br><span class="line"><span class="comment">     * 包名</span></span><br><span class="line"><span class="comment">     */</span></span><br><span class="line">    <span class="keyword">private</span> <span class="keyword">final</span> String packageName;</span><br><span class="line"></span><br><span class="line">    PackageImport(AnnotationMetadata metadata) {</span><br><span class="line">        <span class="keyword">this</span>.packageName = ClassUtils.getPackageName(metadata.getClassName());</span><br><span class="line">    }</span><br><span class="line"></span><br><span class="line">    <span class="function"><span class="keyword">public</span> String <span class="title">getPackageName</span><span class="params">()</span> </span>{</span><br><span class="line">        <span class="keyword">return</span> <span class="keyword">this</span>.packageName;</span><br><span class="line">    }</span><br><span class="line"></span><br><span class="line">    <span class="meta">@Override</span></span><br><span class="line">    <span class="function"><span class="keyword">public</span> <span class="keyword">boolean</span> <span class="title">equals</span><span class="params">(Object obj)</span> </span>{</span><br><span class="line">        <span class="keyword">if</span> (obj == <span class="keyword">null</span> || getClass() != obj.getClass()) {</span><br><span class="line">            <span class="keyword">return</span> <span class="keyword">false</span>;</span><br><span class="line">        }</span><br><span class="line">        <span class="keyword">return</span> <span class="keyword">this</span>.packageName.equals(((PackageImport) obj).packageName);</span><br><span class="line">    }</span><br><span class="line"></span><br><span class="line">    <span class="meta">@Override</span></span><br><span class="line">    <span class="function"><span class="keyword">public</span> <span class="keyword">int</span> <span class="title">hashCode</span><span class="params">()</span> </span>{</span><br><span class="line">        <span class="keyword">return</span> <span class="keyword">this</span>.packageName.hashCode();</span><br><span class="line">    }</span><br><span class="line"></span><br><span class="line">    <span class="meta">@Override</span></span><br><span class="line">    <span class="function"><span class="keyword">public</span> String <span class="title">toString</span><span class="params">()</span> </span>{</span><br><span class="line">        <span class="keyword">return</span> <span class="string">"Package Import "</span> + <span class="keyword">this</span>.packageName;</span><br><span class="line">    }</span><br><span class="line"></span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li>如下是一个示例：<img src="http://static2.iocoder.cn/images/Spring-Boot/2021-02-01/10.jpg" alt="PackageImport"></li>
</ul>
</li>
<li><code>&lt;X&gt;</code> 处，调用 <code>#register(BeanDefinitionRegistry registry, String... packageNames)</code> 方法，注册一个用于存储报名（<code>package</code>）的 Bean 到 Spring IoC 容器中。详细解析，见 <a href="#">「6.2 register」</a> 。</li>
</ul>
<h2 id="6-2-register"><a href="#6-2-register" class="headerlink" title="6.2 register"></a>6.2 register</h2><p><code>#register(BeanDefinitionRegistry registry, String... packageNames)</code> 方法，注册一个用于存储报名（<code>package</code>）的 Bean 到 Spring IoC 容器中。代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="comment">// AutoConfigurationPackages.java</span></span><br><span class="line"></span><br><span class="line"><span class="keyword">private</span> <span class="keyword">static</span> <span class="keyword">final</span> String BEAN = AutoConfigurationPackages.class.getName();</span><br><span class="line"></span><br><span class="line"><span class="function"><span class="keyword">public</span> <span class="keyword">static</span> <span class="keyword">void</span> <span class="title">register</span><span class="params">(BeanDefinitionRegistry registry, String... packageNames)</span> </span>{</span><br><span class="line">    <span class="comment">// &lt;1&gt; 如果已经存在该 BEAN ，则修改其包（package）属性</span></span><br><span class="line">	<span class="keyword">if</span> (registry.containsBeanDefinition(BEAN)) {</span><br><span class="line">		BeanDefinition beanDefinition = registry.getBeanDefinition(BEAN);</span><br><span class="line">		ConstructorArgumentValues constructorArguments = beanDefinition.getConstructorArgumentValues();</span><br><span class="line">		constructorArguments.addIndexedArgumentValue(<span class="number">0</span>, addBasePackages(constructorArguments, packageNames));</span><br><span class="line">    <span class="comment">// &lt;2&gt; 如果不存在该 BEAN ，则创建一个 Bean ，并进行注册</span></span><br><span class="line">    } <span class="keyword">else</span> { GenericBeanDefinition beanDefinition = <span class="keyword">new</span> GenericBeanDefinition();</span><br><span class="line">		beanDefinition.setBeanClass(BasePackages.class);</span><br><span class="line">		beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(<span class="number">0</span>, packageNames);</span><br><span class="line">		beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);</span><br><span class="line">		registry.registerBeanDefinition(BEAN, beanDefinition);</span><br><span class="line">	}</span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li><p>注册的 <code>BEAN</code> 的类型，为 BasePackages 类型。它是 AutoConfigurationPackages 的内部类。代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="comment">// AutoConfigurationPackages#BasePackages.java</span></span><br><span class="line"></span><br><span class="line"><span class="keyword">static</span> <span class="keyword">final</span> <span class="class"><span class="keyword">class</span> <span class="title">BasePackages</span> </span>{</span><br><span class="line"></span><br><span class="line">	<span class="keyword">private</span> <span class="keyword">final</span> List&lt;String&gt; packages;</span><br><span class="line"></span><br><span class="line">	<span class="keyword">private</span> <span class="keyword">boolean</span> loggedBasePackageInfo;</span><br><span class="line"></span><br><span class="line">	BasePackages(String... names) {</span><br><span class="line">		List&lt;String&gt; packages = <span class="keyword">new</span> ArrayList&lt;&gt;();</span><br><span class="line">		<span class="keyword">for</span> (String name : names) {</span><br><span class="line">			<span class="keyword">if</span> (StringUtils.hasText(name)) {</span><br><span class="line">				packages.add(name);</span><br><span class="line">			}</span><br><span class="line">		}</span><br><span class="line">		<span class="keyword">this</span>.packages = packages;</span><br><span class="line">	}</span><br><span class="line"></span><br><span class="line">	<span class="function"><span class="keyword">public</span> List&lt;String&gt; <span class="title">get</span><span class="params">()</span> </span>{</span><br><span class="line">		<span class="keyword">if</span> (!<span class="keyword">this</span>.loggedBasePackageInfo) {</span><br><span class="line">			<span class="keyword">if</span> (<span class="keyword">this</span>.packages.isEmpty()) {</span><br><span class="line">				<span class="keyword">if</span> (logger.isWarnEnabled()) {</span><br><span class="line">					logger.warn(<span class="string">"@EnableAutoConfiguration was declared on a class "</span></span><br><span class="line">							+ <span class="string">"in the default package. Automatic @Repository and "</span></span><br><span class="line">							+ <span class="string">"@Entity scanning is not enabled."</span>);</span><br><span class="line">				}</span><br><span class="line">			} <span class="keyword">else</span> {</span><br><span class="line">				<span class="keyword">if</span> (logger.isDebugEnabled()) {</span><br><span class="line">					String packageNames = StringUtils</span><br><span class="line">							.collectionToCommaDelimitedString(<span class="keyword">this</span>.packages);</span><br><span class="line">					logger.debug(<span class="string">"@EnableAutoConfiguration was declared on a class "</span></span><br><span class="line">							+ <span class="string">"in the package '"</span> + packageNames</span><br><span class="line">							+ <span class="string">"'. Automatic @Repository and @Entity scanning is "</span></span><br><span class="line">							+ <span class="string">"enabled."</span>);</span><br><span class="line">				}</span><br><span class="line">			}</span><br><span class="line">			<span class="keyword">this</span>.loggedBasePackageInfo = <span class="keyword">true</span>;</span><br><span class="line">		}</span><br><span class="line">		<span class="keyword">return</span> <span class="keyword">this</span>.packages;</span><br><span class="line">	}</span><br><span class="line"></span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li>就是一个有 <code>packages</code> 属性的封装类。</li>
</ul>
</li>
<li><code>&lt;1&gt;</code> 处，如果已经存在该 <code>BEAN</code> ，则修改其包（<code>package</code>）属性。而合并 <code>package</code> 的逻辑，通过 <code>#addBasePackages(ConstructorArgumentValues constructorArguments, String[] packageNames)</code> 方法，进行实现。代码如下：  <figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="comment">// AutoConfigurationPackages.java</span></span><br><span class="line"></span><br><span class="line"><span class="keyword">private</span> <span class="keyword">static</span> String[] addBasePackages(ConstructorArgumentValues constructorArguments, String[] packageNames) {</span><br><span class="line">	<span class="comment">// 获得已存在的</span></span><br><span class="line">    String[] existing = (String[]) constructorArguments.getIndexedArgumentValue(<span class="number">0</span>, String[].class).getValue();</span><br><span class="line">	<span class="comment">// 进行合并</span></span><br><span class="line">    Set&lt;String&gt; merged = <span class="keyword">new</span> LinkedHashSet&lt;&gt;();</span><br><span class="line">	merged.addAll(Arrays.asList(existing));</span><br><span class="line">	merged.addAll(Arrays.asList(packageNames));</span><br><span class="line">	<span class="keyword">return</span> StringUtils.toStringArray(merged);</span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
</li>
</ul>
<ul>
<li><code>&lt;2&gt;</code> 处，如果不存在该 <code>BEAN</code> ，则创建一个 <code>Bean</code> ，并进行注册。</li>
</ul>
<h2 id="6-3-has"><a href="#6-3-has" class="headerlink" title="6.3 has"></a>6.3 has</h2><p><code>#has(BeanFactory beanFactory)</code> 方法，判断是否存在该 <code>BEAN</code> 在传入的容器中。代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="comment">// AutoConfigurationPackages.java</span></span><br><span class="line"></span><br><span class="line"><span class="function"><span class="keyword">public</span> <span class="keyword">static</span> <span class="keyword">boolean</span> <span class="title">has</span><span class="params">(BeanFactory beanFactory)</span> </span>{</span><br><span class="line">	<span class="keyword">return</span> beanFactory.containsBean(BEAN) &amp;&amp; !get(beanFactory).isEmpty();</span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<h2 id="6-4-get"><a href="#6-4-get" class="headerlink" title="6.4 get"></a>6.4 get</h2><p><code>#get(BeanFactory beanFactory)</code> 方法，获得 <code>BEAN</code> 。代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="comment">// AutoConfigurationPackages.java</span></span><br><span class="line"></span><br><span class="line"><span class="function"><span class="keyword">public</span> <span class="keyword">static</span> List&lt;String&gt; <span class="title">get</span><span class="params">(BeanFactory beanFactory)</span> </span>{</span><br><span class="line">	<span class="keyword">try</span> {</span><br><span class="line">		<span class="keyword">return</span> beanFactory.getBean(BEAN, BasePackages.class).get();</span><br><span class="line">	} <span class="keyword">catch</span> (NoSuchBeanDefinitionException ex) {</span><br><span class="line">		<span class="keyword">throw</span> <span class="keyword">new</span> IllegalStateException(<span class="string">"Unable to retrieve @EnableAutoConfiguration base packages"</span>);</span><br><span class="line">	}</span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<h1 id="666-彩蛋"><a href="#666-彩蛋" class="headerlink" title="666. 彩蛋"></a>666. 彩蛋</h1><p>比想象中长的一篇文章。虽然中间有些地方复杂了一点，但是觉得还是蛮有趣的。</p>
<p>撸完有点不清晰的胖友，再调试两遍。还有疑惑，星球留言走一波哟。</p>
<p>参考和推荐如下文章：</p>
<ul>
<li><p>快乐崇拜 <a href="https://gitbook.cn/books/5a445f030173cb29d2041d61/index.html" rel="external nofollow noopener noreferrer" target="_blank">《Spring Boot 源码深入分析》</a></p>
<blockquote>
<p>有木发现，艿艿写的比他详细很多很多。</p>
</blockquote>
</li>
<li><p>老田 <a href="http://www.54tianzhisheng.cn/2018/04/19/SpringBootApplication-annotation/" rel="external nofollow noopener noreferrer" target="_blank">《Spring Boot 2.0 系列文章(六)：Spring Boot 2.0 中SpringBootApplication注解详解》</a></p>
</li>
<li>dm_vincent <a href="https://blog.csdn.net/dm_vincent/article/details/77619752" rel="external nofollow noopener noreferrer" target="_blank">《[Spring Boot] 4. Spring Boot实现自动配置的原理》</a></li>
</ul>




</div>
<div class="article-info article-info-index">



<div class="article-category tagcloud">
<i class="icon-book icon"></i>
<ul class="article-tag-list">

<li class="article-tag-list-item">
<a href="/categories/Spring-Boot//" class="article-tag-list-link color2">Spring Boot</a>
</li>

</ul>
</div>





<div class="clearfix"></div>
</div>
</div>
</article>

<!-- hexo 的下一篇逻辑有问题

<nav id="article-nav">

<a href="/Spring-Boot/Condition/" id="article-nav-newer" class="article-nav-link-wrap">
<i class="icon-circle-left"></i>
<div class="article-nav-title">

精尽 Spring Boot 源码分析 —— Condition

</div>
</a>


<a href="/Spring-Boot/SpringApplication/" id="article-nav-older" class="article-nav-link-wrap">
<div class="article-nav-title">精尽 Spring Boot 源码分析 —— SpringApplication</div>
<i class="icon-circle-right"></i>
</a>

</nav>

-->

<aside class="wrap-side-operation">
<div class="mod-side-operation">

<div class="jump-container" id="js-jump-container" style="display: none;">
<a href="javascript:void(0)" class="mod-side-operation__jump-to-top">
<i class="icon-font icon-back"></i>
</a>
<div id="js-jump-plan-container" class="jump-plan-container" style="top: -11px;">
<i class="icon-font icon-plane jump-plane"></i>
</div>
</div>


<div class="toc-container tooltip-left">
<i class="icon-font icon-category"></i>
<div class="tooltip tooltip-east">
<span class="tooltip-item">
</span>
<span class="tooltip-content">
<div class="toc-article">
<ol class="toc"><li class="toc-item toc-level-1"><a class="toc-link" href="#1-概述"><span class="toc-number" style="display: none;">1.</span> <span class="toc-text">1. 概述</span></a></li><li class="toc-item toc-level-1"><a class="toc-link" href="#2-自动配置-V-S-自动装配"><span class="toc-number" style="display: none;">2.</span> <span class="toc-text">2. 自动配置 V.S 自动装配</span></a></li><li class="toc-item toc-level-1"><a class="toc-link" href="#3-自动装配原理"><span class="toc-number" style="display: none;">3.</span> <span class="toc-text">3. 自动装配原理</span></a></li><li class="toc-item toc-level-1"><a class="toc-link" href="#4-SpringBootApplication"><span class="toc-number" style="display: none;">4.</span> <span class="toc-text">4. @SpringBootApplication</span></a><ol class="toc-child"><li class="toc-item toc-level-2"><a class="toc-link" href="#4-1-Inherited"><span class="toc-number" style="display: none;">4.1.</span> <span class="toc-text">4.1 @Inherited</span></a></li><li class="toc-item toc-level-2"><a class="toc-link" href="#4-2-SpringBootConfiguration"><span class="toc-number" style="display: none;">4.2.</span> <span class="toc-text">4.2 @SpringBootConfiguration</span></a></li><li class="toc-item toc-level-2"><a class="toc-link" href="#4-3-ComponentScan"><span class="toc-number" style="display: none;">4.3.</span> <span class="toc-text">4.3 @ComponentScan</span></a></li><li class="toc-item toc-level-2"><a class="toc-link" href="#4-4-EnableAutoConfiguration"><span class="toc-number" style="display: none;">4.4.</span> <span class="toc-text">4.4 @EnableAutoConfiguration</span></a></li></ol></li><li class="toc-item toc-level-1"><a class="toc-link" href="#5-AutoConfigurationImportSelector"><span class="toc-number" style="display: none;">5.</span> <span class="toc-text">5. AutoConfigurationImportSelector</span></a><ol class="toc-child"><li class="toc-item toc-level-2"><a class="toc-link" href="#5-1-getCandidateConfigurations"><span class="toc-number" style="display: none;">5.1.</span> <span class="toc-text">5.1 getCandidateConfigurations</span></a></li><li class="toc-item toc-level-2"><a class="toc-link" href="#5-2-getImportGroup"><span class="toc-number" style="display: none;">5.2.</span> <span class="toc-text">5.2 getImportGroup</span></a></li><li class="toc-item toc-level-2"><a class="toc-link" href="#5-3-AutoConfigurationGroup"><span class="toc-number" style="display: none;">5.3.</span> <span class="toc-text">5.3 AutoConfigurationGroup</span></a><ol class="toc-child"><li class="toc-item toc-level-3"><a class="toc-link" href="#5-3-1-属性"><span class="toc-number" style="display: none;">5.3.1.</span> <span class="toc-text">5.3.1 属性</span></a></li><li class="toc-item toc-level-3"><a class="toc-link" href="#5-3-2-process"><span class="toc-number" style="display: none;">5.3.2.</span> <span class="toc-text">5.3.2 process</span></a></li><li class="toc-item toc-level-3"><a class="toc-link" href="#5-3-3-selectImports"><span class="toc-number" style="display: none;">5.3.3.</span> <span class="toc-text">5.3.3 selectImports</span></a></li></ol></li><li class="toc-item toc-level-2"><a class="toc-link" href="#5-4-getAutoConfigurationEntry"><span class="toc-number" style="display: none;">5.4.</span> <span class="toc-text">5.4 getAutoConfigurationEntry</span></a><ol class="toc-child"><li class="toc-item toc-level-3"><a class="toc-link" href="#5-4-1-isEnabled"><span class="toc-number" style="display: none;">5.4.1.</span> <span class="toc-text">5.4.1 isEnabled</span></a></li><li class="toc-item toc-level-3"><a class="toc-link" href="#5-4-2-getAttributes"><span class="toc-number" style="display: none;">5.4.2.</span> <span class="toc-text">5.4.2 getAttributes</span></a></li><li class="toc-item toc-level-3"><a class="toc-link" href="#5-4-3-getExclusions"><span class="toc-number" style="display: none;">5.4.3.</span> <span class="toc-text">5.4.3 getExclusions</span></a></li><li class="toc-item toc-level-3"><a class="toc-link" href="#5-4-4-checkExcludedClasses"><span class="toc-number" style="display: none;">5.4.4.</span> <span class="toc-text">5.4.4 checkExcludedClasses</span></a></li><li class="toc-item toc-level-3"><a class="toc-link" href="#5-4-5-fireAutoConfigurationImportEvents"><span class="toc-number" style="display: none;">5.4.5.</span> <span class="toc-text">5.4.5 fireAutoConfigurationImportEvents</span></a></li></ol></li></ol></li><li class="toc-item toc-level-1"><a class="toc-link" href="#6-AutoConfigurationPackages"><span class="toc-number" style="display: none;">6.</span> <span class="toc-text">6. AutoConfigurationPackages</span></a><ol class="toc-child"><li class="toc-item toc-level-2"><a class="toc-link" href="#6-1-Registrar"><span class="toc-number" style="display: none;">6.1.</span> <span class="toc-text">6.1 Registrar</span></a></li><li class="toc-item toc-level-2"><a class="toc-link" href="#6-2-register"><span class="toc-number" style="display: none;">6.2.</span> <span class="toc-text">6.2 register</span></a></li><li class="toc-item toc-level-2"><a class="toc-link" href="#6-3-has"><span class="toc-number" style="display: none;">6.3.</span> <span class="toc-text">6.3 has</span></a></li><li class="toc-item toc-level-2"><a class="toc-link" href="#6-4-get"><span class="toc-number" style="display: none;">6.4.</span> <span class="toc-text">6.4 get</span></a></li></ol></li><li class="toc-item toc-level-1"><a class="toc-link" href="#666-彩蛋"><span class="toc-number" style="display: none;">7.</span> <span class="toc-text">666. 彩蛋</span></a></li></ol>
</div>
</span>
</div>
</div>

</div>
</aside>














</div>
</div>
</div>