<h2 id="activity-name" class="rich_media_title">Mybatis 使用的 9 种设计模式，真是太有用了～</h2>
<div id="meta_content" class="rich_media_meta_list"><span id="profileBt" class="rich_media_meta rich_media_meta_nickname"><a id="js_name"></a>Java技术栈</span>&nbsp;<em id="publish_time" class="rich_media_meta rich_media_meta_text">今天</em></div>
<div id="js_content" class="rich_media_content ">
<section data-role="outer" data-mpa-powered-by="yiban.io">
<section class="" data-tools="135编辑器" data-id="91525">
<section>
<section>
<p><img class="" src="https://mmbiz.qpic.cn/mmbiz_jpg/TNUwKhV0JpQTvaX5uYtDXzp3SK1LibTHvSyzMD4zgHZ7ADHqDeSJVM8B1ib6XtThZCD3zfiaKaGW5nPiamkHiblYG5w/640?tp=webp&amp;wxfrom=5&amp;wx_lazy=1&amp;wx_co=1" crossorigin="anonymous" data-copyright="0" data-ratio="1" data-s="300,640" data-src="https://mmbiz.qpic.cn/mmbiz_jpg/TNUwKhV0JpQTvaX5uYtDXzp3SK1LibTHvSyzMD4zgHZ7ADHqDeSJVM8B1ib6XtThZCD3zfiaKaGW5nPiamkHiblYG5w/640" data-type="jpeg" data-w="500" data-fail="0" /></p>
</section>
<section>
<p>Java技术栈</p>
<p>www.javastack.cn</p>
<p>优秀的Java技术公众号</p>
</section>
<section>
<section>
<section></section>
</section>
</section>
</section>
</section>
</section>
<p>来源：crazyant.net/2022.html</p>
<p data-mpa-powered-by="yiban.io">&nbsp;</p>
<p data-mpa-powered-by="yiban.io">虽然我们都知道有26个设计模式，但是大多停留在概念层面，真实开发中很少遇到，Mybatis源码中使用了大量的设计模式，阅读源码并观察设计模式在其中的应用，能够更深入的理解设计模式。</p>
<p>Mybatis至少遇到了以下的设计模式的使用：</p>
<ol class="list-paddingleft-2">
<li>
<p>Builder模式，例如SqlSessionFactoryBuilder、XMLConfigBuilder、XMLMapperBuilder、XMLStatementBuilder、CacheBuilder；</p>
</li>
<li>
<p>工厂模式，例如SqlSessionFactory、ObjectFactory、MapperProxyFactory；</p>
</li>
<li>
<p>单例模式，例如ErrorContext和LogFactory；</p>
</li>
<li>
<p>代理模式，Mybatis实现的核心，比如MapperProxy、ConnectionLogger，用的jdk的动态代理；还有executor.loader包使用了cglib或者javassist达到延迟加载的效果；</p>
</li>
<li>
<p>组合模式，例如SqlNode和各个子类ChooseSqlNode等；</p>
</li>
<li>
<p>模板方法模式，例如BaseExecutor和SimpleExecutor，还有BaseTypeHandler和所有的子类例如IntegerTypeHandler；</p>
</li>
<li>
<p>适配器模式，例如Log的Mybatis接口和它对jdbc、log4j等各种日志框架的适配实现；</p>
</li>
<li>
<p>装饰者模式，例如Cache包中的cache.decorators子包中等各个装饰者的实现；</p>
</li>
<li>
<p>迭代器模式，例如迭代器模式PropertyTokenizer；</p>
</li>
</ol>
<p>&nbsp;</p>
<p>接下来挨个模式进行解读，先介绍模式自身的知识，然后解读在Mybatis中怎样应用了该模式。&nbsp;</p>
<h2><a href="http://mp.weixin.qq.com/s?__biz=MzI3ODcxMzQzMw==&amp;mid=2247489094&amp;idx=1&amp;sn=e5942d1014a362217c6c7d11658de001&amp;chksm=eb539370dc241a66ccd91c7625d2c8728f8304d6dc647673b315e4f9ba5417ce4b825bb70667&amp;scene=21#wechat_redirect" target="_blank" rel="noopener" data-itemshowtype="0" data-linktype="2">1、Builder模式</a></h2>
<p>Builder模式的定义是&ldquo;将一个复杂对象的构建与它的表示分离，使得同样的构建过程可以创建不同的表示。&rdquo;，它属于创建类模式，一般来说，如果一个对象的构建比较复杂，超出了构造函数所能包含的范围，就可以使用工厂模式和Builder模式，相对于工厂模式会产出一个完整的产品，Builder应用于更加复杂的对象的构建，甚至只会构建产品的一个部分。</p>
<p><img class="" src="https://mmbiz.qpic.cn/mmbiz_jpg/TNUwKhV0JpQK9aKUcJ8lkucxeCTakZvLwsx2rdCl520ZLBY48ZO1tFfag7c4ibh2ItjFic9yPda0V4nQnlAibBl1A/640?tp=webp&amp;wxfrom=5&amp;wx_lazy=1&amp;wx_co=1" crossorigin="anonymous" data-backh="406" data-backw="508" data-before-oversubscription-url="https://mmbiz.qpic.cn/mmbiz_png/TNUwKhV0JpQK9aKUcJ8lkucxeCTakZvLypib6SzjNpYZjhQggTQeCx09kl5EZFQp9Fm4LGsVnB0VTlGDb2TRJsw/640" data-oversubscription-url="http://mmbiz.qpic.cn/mmbiz_jpg/TNUwKhV0JpQK9aKUcJ8lkucxeCTakZvLwsx2rdCl520ZLBY48ZO1tFfag7c4ibh2ItjFic9yPda0V4nQnlAibBl1A/0?wx_fmt=jpeg" data-ratio="0.7992125984251969" data-src="https://mmbiz.qpic.cn/mmbiz_jpg/TNUwKhV0JpQK9aKUcJ8lkucxeCTakZvLwsx2rdCl520ZLBY48ZO1tFfag7c4ibh2ItjFic9yPda0V4nQnlAibBl1A/640" data-type="png" data-w="508" data-fail="0" /></p>
<p>在Mybatis环境的初始化过程中，SqlSessionFactoryBuilder会调用XMLConfigBuilder读取所有的MybatisMapConfig.xml和所有的*Mapper.xml文件，构建Mybatis运行的核心对象Configuration对象，然后将该Configuration对象作为参数构建一个SqlSessionFactory对象。</p>
<p>其中XMLConfigBuilder在构建Configuration对象时，也会调用XMLMapperBuilder用于读取*Mapper文件，而XMLMapperBuilder会使用XMLStatementBuilder来读取和build所有的SQL语句。</p>
<p>在这个过程中，有一个相似的特点，就是这些Builder会读取文件或者配置，然后做大量的XpathParser解析、配置或语法的解析、反射生成对象、存入结果缓存等步骤，这么多的工作都不是一个构造函数所能包括的，因此大量采用了Builder模式来解决。</p>
<p>对于builder的具体类，方法都大都用build*开头，比如SqlSessionFactoryBuilder为例，它包含以下方法：</p>
<p>即根据不同的输入参数来构建SqlSessionFactory这个工厂对象。</p>
<h2><a href="http://mp.weixin.qq.com/s?__biz=MzI3ODcxMzQzMw==&amp;mid=2247489066&amp;idx=2&amp;sn=b3fdfe1d989053915a7c993a76e64cc6&amp;chksm=eb53931cdc241a0af905ca600e5dceef7fa848b06c5dccc40ed33aff4e5402885336c3972bf5&amp;scene=21#wechat_redirect" target="_blank" rel="noopener" data-itemshowtype="0" data-linktype="2">2、工厂模式</a></h2>
<p>在Mybatis中比如SqlSessionFactory使用的是工厂模式，该工厂没有那么复杂的逻辑，是一个简单工厂模式。</p>
<p>简单工厂模式(Simple&nbsp;Factory&nbsp;Pattern)：又称为静态工厂方法(Static&nbsp;Factory&nbsp;Method)模式，它属于类创建型模式。在简单工厂模式中，可以根据参数的不同返回不同类的实例。简单工厂模式专门定义一个类来负责创建其他类的实例，被创建的实例通常都具有共同的父类。</p>
<p><img class="" src="https://mmbiz.qpic.cn/mmbiz_png/TNUwKhV0JpQK9aKUcJ8lkucxeCTakZvLHITA7tqHBQT4iaa7dib8q1Mc9VGtLAWKAk1uGDFZRYvHz1UWkYcHD5GQ/640?tp=webp&amp;wxfrom=5&amp;wx_lazy=1&amp;wx_co=1" crossorigin="anonymous" data-backh="292" data-backw="558" data-before-oversubscription-url="http://mmbiz.qpic.cn/mmbiz_png/TNUwKhV0JpQK9aKUcJ8lkucxeCTakZvLHITA7tqHBQT4iaa7dib8q1Mc9VGtLAWKAk1uGDFZRYvHz1UWkYcHD5GQ/0?wx_fmt=png" data-ratio="0.5225464190981433" data-src="https://mmbiz.qpic.cn/mmbiz_png/TNUwKhV0JpQK9aKUcJ8lkucxeCTakZvLHITA7tqHBQT4iaa7dib8q1Mc9VGtLAWKAk1uGDFZRYvHz1UWkYcHD5GQ/640" data-type="png" data-w="754" data-fail="0" /></p>
<p>SqlSession可以认为是一个<a href="http://mp.weixin.qq.com/s?__biz=MzI3ODcxMzQzMw==&amp;mid=2247488182&amp;idx=1&amp;sn=1406f0ba8ba9ce0f6944ef9b5b938498&amp;chksm=eb539780dc241e9624e5651e6719bb9ec3d2c72742283bab199a6970ceaa40b14dbc7b434350&amp;scene=21#wechat_redirect" target="_blank" rel="noopener" data-itemshowtype="0" data-linktype="2">Mybatis</a>工作的核心的接口，通过这个接口可以执行执行SQL语句、获取Mappers、管理事务。类似于连接MySQL的Connection对象。<a href="http://mp.weixin.qq.com/s?__biz=MzI3ODcxMzQzMw==&amp;mid=2247488182&amp;idx=1&amp;sn=1406f0ba8ba9ce0f6944ef9b5b938498&amp;chksm=eb539780dc241e9624e5651e6719bb9ec3d2c72742283bab199a6970ceaa40b14dbc7b434350&amp;scene=21#wechat_redirect" target="_blank" rel="noopener" data-itemshowtype="0" data-linktype="2">从 0 开始手写一个 Mybatis 框架</a>，这个我推荐你看下。</p>
<p>&nbsp;</p>
<p><img class="" src="https://mmbiz.qpic.cn/mmbiz_png/TNUwKhV0JpQK9aKUcJ8lkucxeCTakZvLIhJtUjERSPeRM8fvaAiaUD2oftm3bVN9WB0cuZMpSlUP5Jg6icnBXkIg/640?tp=webp&amp;wxfrom=5&amp;wx_lazy=1&amp;wx_co=1" crossorigin="anonymous" data-backh="251" data-backw="558" data-before-oversubscription-url="http://mmbiz.qpic.cn/mmbiz_png/TNUwKhV0JpQK9aKUcJ8lkucxeCTakZvLIhJtUjERSPeRM8fvaAiaUD2oftm3bVN9WB0cuZMpSlUP5Jg6icnBXkIg/0?wx_fmt=png" data-ratio="0.44921875" data-src="https://mmbiz.qpic.cn/mmbiz_png/TNUwKhV0JpQK9aKUcJ8lkucxeCTakZvLIhJtUjERSPeRM8fvaAiaUD2oftm3bVN9WB0cuZMpSlUP5Jg6icnBXkIg/640" data-type="png" data-w="768" data-fail="0" /></p>
<p>&nbsp;</p>
<p>可以看到，该Factory的openSession方法重载了很多个，分别支持autoCommit、Executor、Transaction等参数的输入，来构建核心的SqlSession对象。</p>
<p>&nbsp;</p>
<p>在DefaultSqlSessionFactory的默认工厂实现里，有一个方法可以看出工厂怎么产出一个产品：</p>
<p>&nbsp;</p>
<section class="" data-mpa-preserve-tpl-color="t" data-mpa-template="t">
<p><span class="">private&nbsp;SqlSession&nbsp;openSessionFromDataSource(ExecutorType execType, TransactionIsolationLevel level,<br />&nbsp; &nbsp; &nbsp;boolean&nbsp;autoCommit)&nbsp;</span>{<br />&nbsp; &nbsp;Transaction tx =&nbsp;<span class="">null</span>;<br />&nbsp; &nbsp;<span class="">try</span>&nbsp;{<br />&nbsp; &nbsp; &nbsp;<span class="">final</span>&nbsp;Environment environment = configuration.getEnvironment();<br />&nbsp; &nbsp; &nbsp;<span class="">final</span>&nbsp;TransactionFactory transactionFactory = getTransactionFactoryFromEnvironment(environment);<br />&nbsp; &nbsp; &nbsp;tx = transactionFactory.newTransaction(environment.getDataSource(), level, autoCommit);<br />&nbsp; &nbsp; &nbsp;<span class="">final</span>&nbsp;Executor executor = configuration.newExecutor(tx, execType);<br />&nbsp; &nbsp; &nbsp;<span class="">return</span>&nbsp;<span class="">new</span>&nbsp;DefaultSqlSession(configuration, executor, autoCommit);<br />&nbsp; &nbsp;}&nbsp;<span class="">catch</span>&nbsp;(Exception e) {<br />&nbsp; &nbsp; &nbsp;closeTransaction(tx);&nbsp;<span class="">// may have fetched a connection so lets call</span><br />&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<span class="">// close()</span><br />&nbsp; &nbsp; &nbsp;<span class="">throw</span>&nbsp;ExceptionFactory.wrapException(<span class="">"Error&nbsp;opening&nbsp;session.&nbsp;&nbsp;Cause:&nbsp;"</span>&nbsp;+ e, e);<br />&nbsp; &nbsp;}&nbsp;<span class="">finally</span>&nbsp;{<br />&nbsp; &nbsp; &nbsp;ErrorContext.instance().reset();<br />&nbsp; &nbsp;}<br />&nbsp;}</p>
</section>
<p>&nbsp;</p>
<p>这是一个openSession调用的底层方法，该方法先从configuration读取对应的环境配置，然后初始化TransactionFactory获得一个Transaction对象，然后通过Transaction获取一个Executor对象，最后通过configuration、Executor、是否autoCommit三个参数构建了SqlSession。</p>
<p>&nbsp;</p>
<p>在这里其实也可以看到端倪，SqlSession的执行，其实是委托给对应的Executor来进行的。</p>
<p>而对于LogFactory，它的实现代码：</p>
<section class="" data-mpa-preserve-tpl-color="t" data-mpa-template="t">
<p><span class="">public</span>&nbsp;<span class="">final</span>&nbsp;<span class="">class&nbsp;LogFactory&nbsp;</span>{<br />&nbsp;<span class="">private</span>&nbsp;<span class="">static</span>&nbsp;Constructor<span class="">&lt;?</span>&nbsp;extends Log&gt; logConstructor;<br /><br />&nbsp;<span class="">private</span>&nbsp;LogFactory() {<br />&nbsp; &nbsp;<span class="">// disable construction</span><br />&nbsp;}<br /><br />&nbsp;<span class="">public</span>&nbsp;<span class="">static</span>&nbsp;Log getLog(<span class="">Class&lt;?&gt;&nbsp;aClass)&nbsp;</span>{<br />&nbsp; &nbsp;<span class="">return</span>&nbsp;getLog(aClass.getName());<br />&nbsp;}</p>
</section>
<p>&nbsp;</p>
<p>这里有个特别的地方，是Log变量的的类型是Constructor&lt;?&nbsp;<span class=""><strong>extends</strong></span>&nbsp;Log&gt;，也就是说该工厂生产的不只是一个产品，而是具有Log公共接口的一系列产品，比如Log4jImpl、Slf4jImpl等很多具体的Log。</p>
<p>&nbsp;</p>
<h2 class=""><a href="http://mp.weixin.qq.com/s?__biz=MzI3ODcxMzQzMw==&amp;mid=2247483742&amp;idx=1&amp;sn=9429b26871f19e4dafd1bf0c7ec0520e&amp;chksm=eb538468dc240d7e968486dbdb7fa440b365ac81a3d9920013781776b601754b1a0659b92b70&amp;scene=21#wechat_redirect" target="_blank" rel="noopener" data-itemshowtype="0" data-linktype="2">3、单例模式</a></h2>
<p>单例模式(Singleton&nbsp;Pattern)：单例模式确保某一个类只有一个实例，而且自行实例化并向整个系统提供这个实例，这个类称为单例类，它提供全局访问的方法。</p>
<p>单例模式的要点有三个：一是某个类只能有一个实例；二是它必须自行创建这个实例；三是它必须自行向整个系统提供这个实例。单例模式是一种对象创建型模式。单例模式又名单件模式或单态模式。</p>
<p><img class="" src="https://mmbiz.qpic.cn/mmbiz_jpg/TNUwKhV0JpQK9aKUcJ8lkucxeCTakZvLKQz0nIXpphgtYhDTq7pGr6jkIqZ9njMasicGibg7BNWpSb27JBXbxia0g/640?tp=webp&amp;wxfrom=5&amp;wx_lazy=1&amp;wx_co=1" crossorigin="anonymous" data-backh="273" data-backw="550" data-before-oversubscription-url="http://www.crazyant.net/wp-content/uploads/2016/12/pattern_singleton.jpg" data-oversubscription-url="http://mmbiz.qpic.cn/mmbiz_jpg/TNUwKhV0JpQK9aKUcJ8lkucxeCTakZvLyGsuMLoXw7gbrtDP98qxz0p3pSiaOaMSfiazrSjuYzktLZ0WLWCgC6HQ/0?wx_fmt=jpeg" data-ratio="0.49636363636363634" data-src="https://mmbiz.qpic.cn/mmbiz_jpg/TNUwKhV0JpQK9aKUcJ8lkucxeCTakZvLKQz0nIXpphgtYhDTq7pGr6jkIqZ9njMasicGibg7BNWpSb27JBXbxia0g/640" data-type="jpeg" data-w="550" data-fail="0" /></p>
<p>在Mybatis中有两个地方用到单例模式，ErrorContext和LogFactory，其中ErrorContext是用在每个线程范围内的单例，用于记录该线程的执行环境错误信息，而LogFactory则是提供给整个Mybatis使用的日志工厂，用于获得针对项目配置好的日志对象。<a href="http://mp.weixin.qq.com/s?__biz=MzI3ODcxMzQzMw==&amp;mid=2247483742&amp;idx=1&amp;sn=9429b26871f19e4dafd1bf0c7ec0520e&amp;chksm=eb538468dc240d7e968486dbdb7fa440b365ac81a3d9920013781776b601754b1a0659b92b70&amp;scene=21#wechat_redirect" target="_blank" rel="noopener" data-itemshowtype="0" data-linktype="2">设计模式之单例模式实践</a>，这篇文章推荐你看下。</p>
<p>ErrorContext的单例实现代码：</p>
<section class="" data-mpa-preserve-tpl-color="t" data-mpa-template="t">
<p><span class="">public</span>&nbsp;<span class="">class</span>&nbsp;<span class="">ErrorContext</span>&nbsp;{<br /><br />&nbsp;<span class="">private</span>&nbsp;<span class="">static</span>&nbsp;final ThreadLocal&lt;ErrorContext&gt; LOCAL =&nbsp;<span class="">new</span>&nbsp;ThreadLocal&lt;ErrorContext&gt;();<br /><br />&nbsp;<span class="">private&nbsp;ErrorContext()&nbsp;</span>{<br />&nbsp;}<br /><br />&nbsp;<span class="">public&nbsp;static&nbsp;ErrorContext&nbsp;instance()&nbsp;</span>{<br />&nbsp; &nbsp;ErrorContext context = LOCAL.<span class="">get</span>();<br />&nbsp; &nbsp;<span class="">if</span>&nbsp;(context ==&nbsp;<span class="">null</span>) {<br />&nbsp; &nbsp; &nbsp;context =&nbsp;<span class="">new</span>&nbsp;ErrorContext();<br />&nbsp; &nbsp; &nbsp;LOCAL.<span class="">set</span>(context);<br />&nbsp; &nbsp;}<br />&nbsp; &nbsp;<span class="">return</span>&nbsp;context;<br />&nbsp;}</p>
</section>
<p>&nbsp;</p>
<p>构造函数是private修饰，具有一个static的局部instance变量和一个获取instance变量的方法，在获取实例的方法中，先判断是否为空如果是的话就先创建，然后返回构造好的对象。</p>
<p>&nbsp;</p>
<p>只是这里有个有趣的地方是，LOCAL的静态实例变量使用了<a href="http://mp.weixin.qq.com/s?__biz=MzI3ODcxMzQzMw==&amp;mid=2247484079&amp;idx=1&amp;sn=32fe06e35026a5f0c11e4a8b8c9620d7&amp;chksm=eb538799dc240e8f71d23b6918837ade08ea2da0568aa646117bd2f4f52013a94cf4e5a66c2a&amp;scene=21#wechat_redirect" target="_blank" rel="noopener" data-itemshowtype="0" data-linktype="2">ThreadLocal</a>修饰，也就是说它属于每个线程各自的数据，而在instance()方法中，先获取本线程的该实例，如果没有就创建该线程独有的ErrorContext。</p>
<h2><a href="http://mp.weixin.qq.com/s?__biz=MzI3ODcxMzQzMw==&amp;mid=2247486759&amp;idx=2&amp;sn=6769d8ff9d163babe726b6213c6d15e4&amp;chksm=eb538811dc240107bcf2a6e65b5381b2a68175af8ff12f4e2c1b0a06f7d16850db4acb64a18e&amp;scene=21#wechat_redirect" target="_blank" rel="noopener" data-itemshowtype="0" data-linktype="2">4、代理模式</a></h2>
<p>代理模式可以认为是Mybatis的核心使用的模式，正是由于这个模式，我们只需要编写Mapper.java接口，不需要实现，由Mybatis后台帮我们完成具体SQL的执行。</p>
<p>代理模式(Proxy&nbsp;Pattern)&nbsp;：给某一个对象提供一个代&nbsp;理，并由代理对象控制对原对象的引用。代理模式的英&nbsp;文叫做Proxy或Surrogate，它是一种对象结构型模式。</p>
<p>代理模式包含如下角色：</p>
<ul class="list-paddingleft-2">
<li>
<p>Subject:&nbsp;抽象主题角色</p>
</li>
<li>
<p>Proxy:&nbsp;代理主题角色</p>
</li>
<li>
<p>RealSubject:&nbsp;真实主题角色</p>
<p>&nbsp;</p>
</li>
</ul>
<p><img class="" src="https://mmbiz.qpic.cn/mmbiz_jpg/TNUwKhV0JpQK9aKUcJ8lkucxeCTakZvLtWSJT6ib3mc3BItQOc1eH5FPxlk3s25fDicShicsnEjPjtIHaicmcE3b4g/640?tp=webp&amp;wxfrom=5&amp;wx_lazy=1&amp;wx_co=1" crossorigin="anonymous" data-backh="371" data-backw="558" data-before-oversubscription-url="http://www.crazyant.net/wp-content/uploads/2016/12/pattern_Proxy.jpg" data-ratio="0.6641337386018237" data-src="https://mmbiz.qpic.cn/mmbiz_jpg/TNUwKhV0JpQK9aKUcJ8lkucxeCTakZvLtWSJT6ib3mc3BItQOc1eH5FPxlk3s25fDicShicsnEjPjtIHaicmcE3b4g/640" data-type="jpeg" data-w="658" data-fail="0" /></p>
<p>这里有两个步骤，第一个是提前创建一个Proxy，第二个是使用的时候会自动请求Proxy，然后由Proxy来执行具体事务；</p>
<p>当我们使用Configuration的getMapper方法时，会调用mapperRegistry<span class="">.getMapper方法，而该方法又会调用</span>mapperProxyFactory<span class="">.newInstance(</span>sqlSession<span class="">)来生成一个具体的代理：</span></p>
<section class="" data-mpa-preserve-tpl-color="t" data-mpa-template="t">
<p><span class="">/**<br />*&nbsp;@author&nbsp;Lasse Voss<br />*/</span><br /><span class="">public</span>&nbsp;<span class="">class&nbsp;MapperProxyFactory&lt;T&gt;&nbsp;</span>{<br /><br />&nbsp;<span class="">private</span>&nbsp;<span class="">final</span>&nbsp;Class&lt;T&gt; mapperInterface;<br />&nbsp;<span class="">private</span>&nbsp;<span class="">final</span>&nbsp;Map&lt;Method, MapperMethod&gt; methodCache =&nbsp;<span class="">new</span>&nbsp;ConcurrentHashMap&lt;Method, MapperMethod&gt;();<br /><br />&nbsp;<span class="">public&nbsp;MapperProxyFactory(Class&lt;T&gt; mapperInterface)&nbsp;</span>{<br />&nbsp; &nbsp;<span class="">this</span>.mapperInterface = mapperInterface;<br />&nbsp;}<br /><br />&nbsp;<span class="">public&nbsp;Class&lt;T&gt;&nbsp;getMapperInterface()&nbsp;</span>{<br />&nbsp; &nbsp;<span class="">return</span>&nbsp;mapperInterface;<br />&nbsp;}<br /><br />&nbsp;<span class="">public&nbsp;Map&lt;Method, MapperMethod&gt;&nbsp;getMethodCache()&nbsp;</span>{<br />&nbsp; &nbsp;<span class="">return</span>&nbsp;methodCache;<br />&nbsp;}<br /><br />&nbsp;<span class="">@SuppressWarnings</span>(<span class="">"unchecked"</span>)<br />&nbsp;<span class="">protected&nbsp;T&nbsp;newInstance(MapperProxy&lt;T&gt; mapperProxy)&nbsp;</span>{<br />&nbsp; &nbsp;<span class="">return</span>&nbsp;(T) Proxy.newProxyInstance(mapperInterface.getClassLoader(),&nbsp;<span class="">new</span>&nbsp;Class[] { mapperInterface },<br />&nbsp; &nbsp; &nbsp; &nbsp;mapperProxy);<br />&nbsp;}<br /><br />&nbsp;<span class="">public&nbsp;T&nbsp;newInstance(SqlSession sqlSession)&nbsp;</span>{<br />&nbsp; &nbsp;<span class="">final</span>&nbsp;MapperProxy&lt;T&gt; mapperProxy =&nbsp;<span class="">new</span>&nbsp;MapperProxy&lt;T&gt;(sqlSession, mapperInterface, methodCache);<br />&nbsp; &nbsp;<span class="">return</span>&nbsp;newInstance(mapperProxy);<br />&nbsp;}<br /><br />}</p>
</section>
<p>&nbsp;</p>
<p>在这里，先通过T&nbsp;newInstance(SqlSession&nbsp;sqlSession)方法会得到一个MapperProxy对象，然后调用T&nbsp;newInstance(MapperProxy&lt;T&gt;&nbsp;mapperProxy)生成代理对象然后返回。</p>
<p>而查看MapperProxy的代码，可以看到如下内容：</p>
<section class="" data-mpa-preserve-tpl-color="t" data-mpa-template="t">
<p><span class="">public</span>&nbsp;<span class="">class&nbsp;MapperProxy&lt;T&gt;&nbsp;implements&nbsp;InvocationHandler,&nbsp;Serializable&nbsp;</span>{<br /><br />&nbsp;<span class="">@Override</span><br />&nbsp;<span class="">public&nbsp;Object&nbsp;invoke(Object proxy, Method method, Object[] args)&nbsp;throws&nbsp;Throwable&nbsp;</span>{<br />&nbsp; &nbsp;<span class="">try</span>&nbsp;{<br />&nbsp; &nbsp; &nbsp;<span class="">if</span>&nbsp;(Object.class.equals(method.getDeclaringClass())) {<br />&nbsp; &nbsp; &nbsp; &nbsp;<span class="">return</span>&nbsp;method.invoke(<span class="">this</span>, args);<br />&nbsp; &nbsp; &nbsp;}&nbsp;<span class="">else</span>&nbsp;<span class="">if</span>&nbsp;(isDefaultMethod(method)) {<br />&nbsp; &nbsp; &nbsp; &nbsp;<span class="">return</span>&nbsp;invokeDefaultMethod(proxy, method, args);<br />&nbsp; &nbsp; &nbsp;}<br />&nbsp; &nbsp;}&nbsp;<span class="">catch</span>&nbsp;(Throwable t) {<br />&nbsp; &nbsp; &nbsp;<span class="">throw</span>&nbsp;ExceptionUtil.unwrapThrowable(t);<br />&nbsp; &nbsp;}<br />&nbsp; &nbsp;<span class="">final</span>&nbsp;MapperMethod mapperMethod = cachedMapperMethod(method);<br />&nbsp; &nbsp;<span class="">return</span>&nbsp;mapperMethod.execute(sqlSession, args);<br />&nbsp;}</p>
</section>
<p>&nbsp;</p>
<p>非常典型的，该MapperProxy类实现了InvocationHandler接口，并且实现了该接口的invoke方法。</p>
<p>&nbsp;</p>
<p>通过这种方式，我们只需要编写Mapper.java接口类，当真正执行一个Mapper接口的时候，就会转发给MapperProxy.invoke方法，而该方法则会调用后续的sqlSession.cud&gt;executor.execute&gt;prepareStatement等一系列方法，完成SQL的执行和返回。</p>
<h2>5、组合模式</h2>
<p>组合模式组合多个对象形成树形结构以表示&ldquo;整体-部分&rdquo;的结构层次。</p>
<p>组合模式对单个对象(叶子对象)和组合对象(组合对象)具有一致性，它将对象组织到树结构中，可以用来描述整体与部分的关系。同时它也模糊了简单元素(叶子对象)和复杂元素(容器对象)的概念，使得客户能够像处理简单元素一样来处理复杂元素，从而使客户程序能够与复杂元素的内部结构解耦。</p>
<p>在使用组合模式中需要注意一点也是组合模式最关键的地方：叶子对象和组合对象实现相同的接口。这就是组合模式能够将叶子节点和对象节点进行一致处理的原因。</p>
<p><img class="" src="https://mmbiz.qpic.cn/mmbiz_png/TNUwKhV0JpQK9aKUcJ8lkucxeCTakZvL9WZ502vibaMRZURLg8BxWHfL3j05ARN0uYIiamnmEzZL7CXEucdyjicIw/640?tp=webp&amp;wxfrom=5&amp;wx_lazy=1&amp;wx_co=1" crossorigin="anonymous" data-backh="401" data-backw="558" data-before-oversubscription-url="http://www.crazyant.net/wp-content/uploads/2016/12/pattern_composite2.png" data-ratio="0.7194928684627575" data-src="https://mmbiz.qpic.cn/mmbiz_png/TNUwKhV0JpQK9aKUcJ8lkucxeCTakZvL9WZ502vibaMRZURLg8BxWHfL3j05ARN0uYIiamnmEzZL7CXEucdyjicIw/640" data-type="png" data-w="631" data-fail="0" /></p>
<p>Mybatis支持动态SQL的强大功能，比如下面的这个SQL：</p>
<section class="" data-mpa-preserve-tpl-color="t" data-mpa-template="t">
<p>&lt;update id=<span class="">"update"</span>&nbsp;parameterType=<span class="">"org.format.dynamicproxy.mybatis.bean.User"</span>&gt;<br />&nbsp; &nbsp;UPDATE users<br />&nbsp; &nbsp;&lt;trim prefix=<span class="">"SET"</span>&nbsp;prefixOverrides=<span class="">","</span>&gt;<br />&nbsp; &nbsp; &nbsp; &nbsp;&lt;<span class="">if</span>&nbsp;<span class="">test</span>=<span class="">"name&nbsp;!=&nbsp;null&nbsp;and&nbsp;name&nbsp;!=&nbsp;''"</span>&gt;<br />&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;name =&nbsp;<span class="">#{name}</span><br />&nbsp; &nbsp; &nbsp; &nbsp;&lt;/<span class="">if</span>&gt;<br />&nbsp; &nbsp; &nbsp; &nbsp;&lt;<span class="">if</span>&nbsp;<span class="">test</span>=<span class="">"age&nbsp;!=&nbsp;null&nbsp;and&nbsp;age&nbsp;!=&nbsp;''"</span>&gt;<br />&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;, age =&nbsp;<span class="">#{age}</span><br />&nbsp; &nbsp; &nbsp; &nbsp;&lt;/<span class="">if</span>&gt;<br />&nbsp; &nbsp; &nbsp; &nbsp;&lt;<span class="">if</span>&nbsp;<span class="">test</span>=<span class="">"birthday&nbsp;!=&nbsp;null&nbsp;and&nbsp;birthday&nbsp;!=&nbsp;''"</span>&gt;<br />&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;, birthday =&nbsp;<span class="">#{birthday}</span><br />&nbsp; &nbsp; &nbsp; &nbsp;&lt;/<span class="">if</span>&gt;<br />&nbsp; &nbsp;&lt;/trim&gt;<br />&nbsp; &nbsp;<span class="">where</span>&nbsp;id =&nbsp;<span class="">${id}</span><br />&lt;/update&gt;</p>
</section>
<p>&nbsp;</p>
<p>在这里面使用到了trim、if等动态元素，可以根据条件来生成不同情况下的SQL；</p>
<p>&nbsp;</p>
<p>在DynamicSqlSource.getBoundSql方法里，调用了rootSqlNode.apply(context)方法，apply方法是所有的动态节点都实现的接口：</p>
<section class="" data-mpa-preserve-tpl-color="t" data-mpa-template="t">
<p><span class="">public</span>&nbsp;<span class="">interface&nbsp;SqlNode&nbsp;</span>{<br />&nbsp;<span class="">boolean&nbsp;apply(DynamicContext context)</span>;<br />}</p>
</section>
<p>&nbsp;</p>
<p>对于实现该SqlSource接口的所有节点，就是整个组合模式树的各个节点：</p>
<p>&nbsp;</p>
<p><img class="" src="https://mmbiz.qpic.cn/mmbiz_png/TNUwKhV0JpQK9aKUcJ8lkucxeCTakZvLibz5giajTeBj8cOkk0WSSBglZic4tVX99HSM3M98u3g0pkmbGD6XdaWNA/640?tp=webp&amp;wxfrom=5&amp;wx_lazy=1&amp;wx_co=1" crossorigin="anonymous" data-backh="446" data-backw="558" data-before-oversubscription-url="http://www.crazyant.net/wp-content/uploads/2016/12/mybatis_sqlNode_types-768x614.png" data-ratio="0.7994791666666666" data-src="https://mmbiz.qpic.cn/mmbiz_png/TNUwKhV0JpQK9aKUcJ8lkucxeCTakZvLibz5giajTeBj8cOkk0WSSBglZic4tVX99HSM3M98u3g0pkmbGD6XdaWNA/640" data-type="png" data-w="768" data-fail="0" /></p>
<p>&nbsp;</p>
<p>组合模式的简单之处在于，所有的子节点都是同一类节点，可以递归的向下执行，比如对于TextSqlNode，因为它是最底层的叶子节点，所以直接将对应的内容append到SQL语句中：</p>
<section class="" data-mpa-preserve-tpl-color="t" data-mpa-template="t">
<p><span class="">@Override</span><br />&nbsp;<span class="">public&nbsp;boolean&nbsp;apply(DynamicContext context)&nbsp;</span>{<br />&nbsp; &nbsp;GenericTokenParser parser = createParser(<span class="">new</span>&nbsp;BindingTokenParser(context, injectionFilter));<br />&nbsp; &nbsp;context.appendSql(parser.parse(text));<br />&nbsp; &nbsp;<span class="">return</span>&nbsp;<span class="">true</span>;<br />&nbsp;}</p>
</section>
<p>&nbsp;</p>
<p>但是对于IfSqlNode，就需要先做判断，如果判断通过，仍然会调用子元素的SqlNode，即contents.apply方法，实现递归的解析。</p>
<section class="" data-mpa-preserve-tpl-color="t" data-mpa-template="t">
<p><span class="">@Override</span><br />&nbsp;<span class="">public&nbsp;boolean&nbsp;apply(DynamicContext context)&nbsp;</span>{<br />&nbsp; &nbsp;<span class="">if</span>&nbsp;(evaluator.evaluateBoolean(test, context.getBindings())) {<br />&nbsp; &nbsp; &nbsp;contents.apply(context);<br />&nbsp; &nbsp; &nbsp;<span class="">return</span>&nbsp;<span class="">true</span>;<br />&nbsp; &nbsp;}<br />&nbsp; &nbsp;<span class="">return</span>&nbsp;<span class="">false</span>;<br />&nbsp;}</p>
</section>
<p>&nbsp;</p>
<h2>6、模板方法模式</h2>
<p>模板方法模式是所有模式中最为常见的几个模式之一，是基于继承的代码复用的基本技术。关注Java技术栈微信公众号，在后台回复关键字：<strong><em>架构</em></strong>，可以获取更多栈长整理的架构和设计模式干货。</p>
<p>模板方法模式需要开发抽象类和具体子类的设计师之间的协作。一个设计师负责给出一个算法的轮廓和骨架，另一些设计师则负责给出这个算法的各个逻辑步骤。代表这些具体逻辑步骤的方法称做基本方法(primitive&nbsp;method)；而将这些基本方法汇总起来的方法叫做模板方法(template&nbsp;method)，这个设计模式的名字就是从此而来。</p>
<p>模板类定义一个操作中的算法的骨架，而将一些步骤延迟到子类中。使得子类可以不改变一个算法的结构即可重定义该算法的某些特定步骤。</p>
<p><img class="" src="https://mmbiz.qpic.cn/mmbiz_png/TNUwKhV0JpQK9aKUcJ8lkucxeCTakZvLzUo360jPsfiav69Z46XIRfKZXF9iaIDyXFficVuYHK1pMvguog6D5wOmw/640?tp=webp&amp;wxfrom=5&amp;wx_lazy=1&amp;wx_co=1" crossorigin="anonymous" data-before-oversubscription-url="http://mmbiz.qpic.cn/mmbiz_png/TNUwKhV0JpQK9aKUcJ8lkucxeCTakZvLzUo360jPsfiav69Z46XIRfKZXF9iaIDyXFficVuYHK1pMvguog6D5wOmw/0?wx_fmt=png" data-ratio="1.1818181818181819" data-src="https://mmbiz.qpic.cn/mmbiz_png/TNUwKhV0JpQK9aKUcJ8lkucxeCTakZvLzUo360jPsfiav69Z46XIRfKZXF9iaIDyXFficVuYHK1pMvguog6D5wOmw/640" data-type="png" data-w="231" data-fail="0" /></p>
<p>在Mybatis中，sqlSession的SQL执行，都是委托给Executor实现的，Executor包含以下结构：</p>
<p><img class="" src="https://mmbiz.qpic.cn/mmbiz_png/TNUwKhV0JpQK9aKUcJ8lkucxeCTakZvLvcYbkDydhRplMnBSIeCOib77dfqqzbbDefqPe2BxvptCx75a8jwibKNA/640?tp=webp&amp;wxfrom=5&amp;wx_lazy=1&amp;wx_co=1" crossorigin="anonymous" data-backh="232" data-backw="452" data-before-oversubscription-url="http://mmbiz.qpic.cn/mmbiz_png/TNUwKhV0JpQK9aKUcJ8lkucxeCTakZvLvcYbkDydhRplMnBSIeCOib77dfqqzbbDefqPe2BxvptCx75a8jwibKNA/0?wx_fmt=png" data-ratio="0.5132743362831859" data-src="https://mmbiz.qpic.cn/mmbiz_png/TNUwKhV0JpQK9aKUcJ8lkucxeCTakZvLvcYbkDydhRplMnBSIeCOib77dfqqzbbDefqPe2BxvptCx75a8jwibKNA/640" data-type="png" data-w="452" data-fail="0" /></p>
<p>其中的BaseExecutor就采用了模板方法模式，它实现了大部分的SQL执行逻辑，然后把以下几个方法交给子类定制化完成：</p>
<p><span class="">protected&nbsp;abstract&nbsp;int&nbsp;doUpdate(MappedStatement ms, Object parameter)&nbsp;throws&nbsp;SQLException</span>;<br /><br /><span class="">protected&nbsp;abstract&nbsp;List&lt;BatchResult&gt;&nbsp;doFlushStatements(boolean&nbsp;isRollback)&nbsp;throws&nbsp;SQLException</span>;<br /><br /><span class="">protected</span>&nbsp;<span class="">abstract</span>&nbsp;&lt;E&gt;&nbsp;<span class="">List&lt;E&gt;&nbsp;doQuery(MappedStatement ms, Object parameter, RowBounds rowBounds,<br />&nbsp; &nbsp; &nbsp;ResultHandler resultHandler, BoundSql boundSql)&nbsp;throws&nbsp;SQLException</span>;</p>
<p>&nbsp;</p>
<p>该模板方法类有几个子类的具体实现，使用了不同的策略：</p>
<ul class="list-paddingleft-2">
<li>
<p>简单SimpleExecutor：每执行一次update或select，就开启一个Statement对象，用完立刻关闭Statement对象。（可以是Statement或PrepareStatement对象）</p>
</li>
<li>
<p>重用ReuseExecutor：执行update或select，以sql作为key查找Statement对象，存在就使用，不存在就创建，用完后，不关闭Statement对象，而是放置于Map&lt;String,&nbsp;Statement&gt;内，供下一次使用。（可以是Statement或PrepareStatement对象）</p>
</li>
<li>
<p>批量BatchExecutor：执行update（没有select，JDBC批处理不支持select），将所有sql都添加到批处理中（addBatch()），等待统一执行（executeBatch()），它缓存了多个Statement对象，每个Statement对象都是addBatch()完毕后，等待逐一执行executeBatch()批处理的；BatchExecutor相当于维护了多个桶，每个桶里都装了很多属于自己的SQL，就像苹果蓝里装了很多苹果，番茄蓝里装了很多番茄，最后，再统一倒进仓库。（可以是Statement或PrepareStatement对象）</p>
</li>
</ul>
<p>&nbsp;</p>
<p>比如在SimpleExecutor中这样实现update方法：</p>
<section class="" data-mpa-preserve-tpl-color="t" data-mpa-template="t">
<p><span class="">@Override</span><br /><span class="">public&nbsp;int&nbsp;doUpdate(MappedStatement ms, Object parameter)&nbsp;throws&nbsp;SQLException&nbsp;</span>{<br />&nbsp; &nbsp;Statement stmt =&nbsp;<span class="">null</span>;<br />&nbsp; &nbsp;<span class="">try</span>&nbsp;{<br />&nbsp; &nbsp; &nbsp;Configuration configuration = ms.getConfiguration();<br />&nbsp; &nbsp; &nbsp;StatementHandler handler = configuration.newStatementHandler(<span class="">this</span>, ms, parameter, RowBounds.DEFAULT,&nbsp;<span class="">null</span>,<br />&nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<span class="">null</span>);<br />&nbsp; &nbsp; &nbsp;stmt = prepareStatement(handler, ms.getStatementLog());<br />&nbsp; &nbsp; &nbsp;<span class="">return</span>&nbsp;handler.update(stmt);<br />&nbsp; &nbsp;}&nbsp;<span class="">finally</span>&nbsp;{<br />&nbsp; &nbsp; &nbsp;closeStatement(stmt);<br />&nbsp; &nbsp;}<br />&nbsp;}</p>
</section>
<h2>&nbsp;</h2>
<h2>7、适配器模式</h2>
<p>适配器模式(Adapter&nbsp;Pattern)&nbsp;：将一个接口转换成客户希望的另一个接口，适配器模式使接口不兼容的那些类可以一起工作，其别名为包装器(Wrapper)。适配器模式既可以作为类结构型模式，也可以作为对象结构型模式。</p>
<p><img class="" src="https://mmbiz.qpic.cn/mmbiz_jpg/TNUwKhV0JpQK9aKUcJ8lkucxeCTakZvLDYgoF1sEr9GAFibFwfQSiav4VhSx9WWknlJpsSTqfhsmQH3ibcibVKStkQ/640?tp=webp&amp;wxfrom=5&amp;wx_lazy=1&amp;wx_co=1" sizes="(max-width: 724px) 100vw, 724px" width="724" height="313" crossorigin="anonymous" data-ratio="0.43232044198895025" data-src="https://mmbiz.qpic.cn/mmbiz_jpg/TNUwKhV0JpQK9aKUcJ8lkucxeCTakZvLDYgoF1sEr9GAFibFwfQSiav4VhSx9WWknlJpsSTqfhsmQH3ibcibVKStkQ/640" data-type="jpeg" data-w="724" data-backw="558" data-backh="241" data-before-oversubscription-url="https://mmbiz.qpic.cn/mmbiz_jpg/TNUwKhV0JpQK9aKUcJ8lkucxeCTakZvLDYgoF1sEr9GAFibFwfQSiav4VhSx9WWknlJpsSTqfhsmQH3ibcibVKStkQ/640" data-fail="0" /></p>
<p>在Mybatsi的logging包中，有一个Log接口：</p>
<section class="" data-mpa-preserve-tpl-color="t" data-mpa-template="t">
<p><span class="">/**<br />*&nbsp;@author&nbsp;Clinton Begin<br />*/</span><br /><span class="">public</span>&nbsp;<span class="">interface&nbsp;Log&nbsp;</span>{<br /><br />&nbsp;<span class="">boolean&nbsp;isDebugEnabled()</span>;<br /><br />&nbsp;<span class="">boolean&nbsp;isTraceEnabled()</span>;<br /><br />&nbsp;<span class="">void&nbsp;error(String s, Throwable e)</span>;<br /><br />&nbsp;<span class="">void&nbsp;error(String s)</span>;<br /><br />&nbsp;<span class="">void&nbsp;debug(String s)</span>;<br /><br />&nbsp;<span class="">void&nbsp;trace(String s)</span>;<br /><br />&nbsp;<span class="">void&nbsp;warn(String s)</span>;<br /><br />}</p>
</section>
<p>&nbsp;</p>
<p>该接口定义了Mybatis直接使用的日志方法，而Log接口具体由谁来实现呢？Mybatis提供了多种日志框架的实现，这些实现都匹配这个Log接口所定义的接口方法，最终实现了所有外部日志框架到Mybatis日志包的适配：</p>
<p>&nbsp;</p>
<p><img class="" src="https://mmbiz.qpic.cn/mmbiz_png/TNUwKhV0JpQK9aKUcJ8lkucxeCTakZvLahpAVH1ruzFu4PhmFYFqkTwq1lRkw88PcUJTcs9r5rEHBlTrqtfhOw/640?tp=webp&amp;wxfrom=5&amp;wx_lazy=1&amp;wx_co=1" sizes="(max-width: 700px) 100vw, 700px" width="700" height="408" crossorigin="anonymous" data-ratio="0.5833333333333334" data-src="https://mmbiz.qpic.cn/mmbiz_png/TNUwKhV0JpQK9aKUcJ8lkucxeCTakZvLahpAVH1ruzFu4PhmFYFqkTwq1lRkw88PcUJTcs9r5rEHBlTrqtfhOw/640" data-type="png" data-w="768" data-backw="558" data-backh="326" data-before-oversubscription-url="https://mmbiz.qpic.cn/mmbiz_png/TNUwKhV0JpQK9aKUcJ8lkucxeCTakZvLahpAVH1ruzFu4PhmFYFqkTwq1lRkw88PcUJTcs9r5rEHBlTrqtfhOw/640" data-fail="0" /></p>
<p>比如对于Log4jImpl的实现来说，该实现持有了org.apache.log4j.Logger的实例，然后所有的日志方法，均委托该实例来实现。</p>
<section class="" data-mpa-preserve-tpl-color="t" data-mpa-template="t">
<p><span class="">public</span>&nbsp;<span class="">class&nbsp;Log4jImpl&nbsp;implements&nbsp;Log&nbsp;</span>{<br /><br />&nbsp;<span class="">private</span>&nbsp;<span class="">static</span>&nbsp;<span class="">final</span>&nbsp;String FQCN = Log4jImpl.class.getName();<br /><br />&nbsp;<span class="">private</span>&nbsp;Logger log;<br /><br />&nbsp;<span class="">public&nbsp;Log4jImpl(String clazz)&nbsp;</span>{<br />&nbsp; &nbsp;log = Logger.getLogger(clazz);<br />&nbsp;}<br /><br />&nbsp;<span class="">@Override</span><br />&nbsp;<span class="">public&nbsp;boolean&nbsp;isDebugEnabled()&nbsp;</span>{<br />&nbsp; &nbsp;<span class="">return</span>&nbsp;log.isDebugEnabled();<br />&nbsp;}<br /><br />&nbsp;<span class="">@Override</span><br />&nbsp;<span class="">public&nbsp;boolean&nbsp;isTraceEnabled()&nbsp;</span>{<br />&nbsp; &nbsp;<span class="">return</span>&nbsp;log.isTraceEnabled();<br />&nbsp;}<br /><br />&nbsp;<span class="">@Override</span><br />&nbsp;<span class="">public&nbsp;void&nbsp;error(String s, Throwable e)&nbsp;</span>{<br />&nbsp; &nbsp;log.log(FQCN, Level.ERROR, s, e);<br />&nbsp;}<br /><br />&nbsp;<span class="">@Override</span><br />&nbsp;<span class="">public&nbsp;void&nbsp;error(String s)&nbsp;</span>{<br />&nbsp; &nbsp;log.log(FQCN, Level.ERROR, s,&nbsp;<span class="">null</span>);<br />&nbsp;}<br /><br />&nbsp;<span class="">@Override</span><br />&nbsp;<span class="">public&nbsp;void&nbsp;debug(String s)&nbsp;</span>{<br />&nbsp; &nbsp;log.log(FQCN, Level.DEBUG, s,&nbsp;<span class="">null</span>);<br />&nbsp;}<br /><br />&nbsp;<span class="">@Override</span><br />&nbsp;<span class="">public&nbsp;void&nbsp;trace(String s)&nbsp;</span>{<br />&nbsp; &nbsp;log.log(FQCN, Level.TRACE, s,&nbsp;<span class="">null</span>);<br />&nbsp;}<br /><br />&nbsp;<span class="">@Override</span><br />&nbsp;<span class="">public&nbsp;void&nbsp;warn(String s)&nbsp;</span>{<br />&nbsp; &nbsp;log.log(FQCN, Level.WARN, s,&nbsp;<span class="">null</span>);<br />&nbsp;}<br /><br />}</p>
</section>
<p>&nbsp;</p>
<p>8、装饰者模式</p>
<p>&nbsp;</p>
<p>装饰模式(Decorator&nbsp;Pattern)&nbsp;：动态地给一个对象增加一些额外的职责(Responsibility)，就增加对象功能来说，装饰模式比生成子类实现更为灵活。其别名也可以称为包装器(Wrapper)，与适配器模式的别名相同，但它们适用于不同的场合。根据翻译的不同，装饰模式也有人称之为&ldquo;油漆工模式&rdquo;，它是一种对象结构型模式。</p>
<p>&nbsp;</p>
<p><img class="" src="https://mmbiz.qpic.cn/mmbiz_jpg/TNUwKhV0JpQK9aKUcJ8lkucxeCTakZvLWdFPb6lsklBFogs9z5iaQKe7NcgNlA3or4NxCkHsVCscicdrDGCjHLBw/640?tp=webp&amp;wxfrom=5&amp;wx_lazy=1&amp;wx_co=1" sizes="(max-width: 643px) 100vw, 643px" width="643" height="546" crossorigin="anonymous" data-ratio="0.8491446345256609" data-src="https://mmbiz.qpic.cn/mmbiz_jpg/TNUwKhV0JpQK9aKUcJ8lkucxeCTakZvLWdFPb6lsklBFogs9z5iaQKe7NcgNlA3or4NxCkHsVCscicdrDGCjHLBw/640" data-type="jpeg" data-w="643" data-backw="558" data-backh="474" data-before-oversubscription-url="https://mmbiz.qpic.cn/mmbiz_jpg/TNUwKhV0JpQK9aKUcJ8lkucxeCTakZvLWdFPb6lsklBFogs9z5iaQKe7NcgNlA3or4NxCkHsVCscicdrDGCjHLBw/640" data-fail="0" /></p>
<p>在mybatis中，缓存的功能由根接口Cache（org.apache.ibatis.cache.Cache）定义。关注Java技术栈微信公众号，在后台回复关键字：<strong><em>架构</em></strong>，可以获取更多栈长整理的架构和设计模式干货。</p>
<p>整个体系采用装饰器设计模式，数据存储和缓存的基本功能由PerpetualCache（org.apache.ibatis.cache.impl.PerpetualCache）永久缓存实现，然后通过一系列的装饰器来对PerpetualCache永久缓存进行缓存策略等方便的控制。如下图：</p>
<p><img class="" src="https://mmbiz.qpic.cn/mmbiz_jpg/TNUwKhV0JpQK9aKUcJ8lkucxeCTakZvLicDvsBooo2m1jHibrRibiabZPbQoeAXBFjwgqfZ245pXMw5YicEdJKYDbyg/640?tp=webp&amp;wxfrom=5&amp;wx_lazy=1&amp;wx_co=1" sizes="(max-width: 700px) 100vw, 700px" width="700" height="349" crossorigin="anonymous" data-ratio="0.4986979166666667" data-src="https://mmbiz.qpic.cn/mmbiz_jpg/TNUwKhV0JpQK9aKUcJ8lkucxeCTakZvLicDvsBooo2m1jHibrRibiabZPbQoeAXBFjwgqfZ245pXMw5YicEdJKYDbyg/640" data-type="jpeg" data-w="768" data-backw="558" data-backh="278" data-before-oversubscription-url="https://mmbiz.qpic.cn/mmbiz_jpg/TNUwKhV0JpQK9aKUcJ8lkucxeCTakZvLicDvsBooo2m1jHibrRibiabZPbQoeAXBFjwgqfZ245pXMw5YicEdJKYDbyg/640" data-fail="0" /></p>
<p>用于装饰PerpetualCache的标准装饰器共有8个（全部在org.apache.ibatis.cache.decorators包中）：</p>
<ol class="list-paddingleft-2">
<li>
<p>FifoCache：先进先出算法，缓存回收策略</p>
</li>
<li>
<p>LoggingCache：输出缓存命中的日志信息</p>
</li>
<li>
<p>LruCache：最近最少使用算法，缓存回收策略</p>
</li>
<li>
<p>ScheduledCache：调度缓存，负责定时清空缓存</p>
</li>
<li>
<p>SerializedCache：缓存序列化和反序列化存储</p>
</li>
<li>
<p>SoftCache：基于软引用实现的缓存管理策略</p>
</li>
<li>
<p>SynchronizedCache：同步的缓存装饰器，用于防止多线程并发访问</p>
</li>
<li>
<p>WeakCache：基于弱引用实现的缓存管理策略</p>
</li>
</ol>
<p>&nbsp;</p>
<p>另外，还有一个特殊的装饰器TransactionalCache：事务性的缓存</p>
<p>正如大多数持久层框架一样，mybatis缓存同样分为一级缓存和二级缓存</p>
<ul class="list-paddingleft-2">
<li>
<p>一级缓存，又叫本地缓存，是PerpetualCache类型的永久缓存，保存在执行器中（BaseExecutor），而执行器又在SqlSession（DefaultSqlSession）中，所以一级缓存的生命周期与SqlSession是相同的。</p>
</li>
<li>
<p>二级缓存，又叫自定义缓存，实现了Cache接口的类都可以作为二级缓存，所以可配置如encache等的第三方缓存。二级缓存以namespace名称空间为其唯一标识，被保存在Configuration核心配置对象中。</p>
</li>
</ul>
<p>&nbsp;</p>
<p>二级缓存对象的默认类型为PerpetualCache，如果配置的缓存是默认类型，则mybatis会根据配置自动追加一系列装饰器。</p>
<p>Cache对象之间的引用顺序为：</p>
<p>SynchronizedCache&ndash;&gt;LoggingCache&ndash;&gt;SerializedCache&ndash;&gt;ScheduledCache&ndash;&gt;LruCache&ndash;&gt;PerpetualCache</p>
<h2>9、迭代器模式</h2>
<p>迭代器（Iterator）模式，又叫做游标（Cursor）模式。GOF给出的定义为：提供一种方法访问一个容器（container）对象中各个元素，而又不需暴露该对象的内部细节。&nbsp;</p>
<h3><img class="" src="https://mmbiz.qpic.cn/mmbiz_png/TNUwKhV0JpQK9aKUcJ8lkucxeCTakZvLNPLWD7fiah4ibwYDloNZ6PvZ4GqBYEicycBOeHGbaA5Uk009GpyCYIRvw/640?tp=webp&amp;wxfrom=5&amp;wx_lazy=1&amp;wx_co=1" width="560" height="276" crossorigin="anonymous" data-ratio="0.4928571428571429" data-src="https://mmbiz.qpic.cn/mmbiz_png/TNUwKhV0JpQK9aKUcJ8lkucxeCTakZvLNPLWD7fiah4ibwYDloNZ6PvZ4GqBYEicycBOeHGbaA5Uk009GpyCYIRvw/640" data-type="gif" data-w="560" data-backw="558" data-backh="275" data-before-oversubscription-url="https://mmbiz.qpic.cn/mmbiz_png/TNUwKhV0JpQK9aKUcJ8lkucxeCTakZvLNPLWD7fiah4ibwYDloNZ6PvZ4GqBYEicycBOeHGbaA5Uk009GpyCYIRvw/640" data-fail="0" /></h3>
<p>Java的Iterator就是迭代器模式的接口，只要实现了该接口，就相当于应用了迭代器模式：</p>
<p><img class="" src="https://mmbiz.qpic.cn/mmbiz_jpg/TNUwKhV0JpQK9aKUcJ8lkucxeCTakZvLOCe5ryLBG14S7F8X0W7iciaAxMmqjsXiauE3ibMVzAiaMddCUiaddicz50gzw/640?tp=webp&amp;wxfrom=5&amp;wx_lazy=1&amp;wx_co=1" sizes="(max-width: 646px) 100vw, 646px" width="646" height="204" crossorigin="anonymous" data-backh="176" data-backw="558" data-before-oversubscription-url="https://mmbiz.qpic.cn/mmbiz_png/TNUwKhV0JpQK9aKUcJ8lkucxeCTakZvLACQOAqzyxmkrA0mfoq4ThIsUxXLkD5PGJvSTsn4qUK5nExxLFPcIvQ/640" data-oversubscription-url="http://mmbiz.qpic.cn/mmbiz_jpg/TNUwKhV0JpQK9aKUcJ8lkucxeCTakZvLOCe5ryLBG14S7F8X0W7iciaAxMmqjsXiauE3ibMVzAiaMddCUiaddicz50gzw/0?wx_fmt=jpeg" data-ratio="0.3157894736842105" data-src="https://mmbiz.qpic.cn/mmbiz_jpg/TNUwKhV0JpQK9aKUcJ8lkucxeCTakZvLOCe5ryLBG14S7F8X0W7iciaAxMmqjsXiauE3ibMVzAiaMddCUiaddicz50gzw/640" data-type="png" data-w="646" data-fail="0" /></p>
<p>比如Mybatis的<span class="">PropertyTokenizer是</span>property包中的重量级类，该类会被reflection包中其他的类频繁的引用到。这个类实现了Iterator接口，在使用时经常被用到的是Iterator接口中的hasNext这个函数。</p>
<section class="" data-mpa-preserve-tpl-color="t" data-mpa-template="t">
<p><span class="">public</span>&nbsp;<span class="">class&nbsp;PropertyTokenizer&nbsp;implements&nbsp;Iterator&lt;PropertyTokenizer&gt;&nbsp;</span>{<br />&nbsp;<span class="">private</span>&nbsp;String name;<br />&nbsp;<span class="">private</span>&nbsp;String indexedName;<br />&nbsp;<span class="">private</span>&nbsp;String index;<br />&nbsp;<span class="">private</span>&nbsp;String children;<br /><br />&nbsp;<span class="">public&nbsp;PropertyTokenizer(String fullname)&nbsp;</span>{<br />&nbsp; &nbsp;<span class="">int</span>&nbsp;delim = fullname.indexOf(<span class="">'.'</span>);<br />&nbsp; &nbsp;<span class="">if</span>&nbsp;(delim &gt; -<span class="">1</span>) {<br />&nbsp; &nbsp; &nbsp;name = fullname.substring(<span class="">0</span>, delim);<br />&nbsp; &nbsp; &nbsp;children = fullname.substring(delim +&nbsp;<span class="">1</span>);<br />&nbsp; &nbsp;}&nbsp;<span class="">else</span>&nbsp;{<br />&nbsp; &nbsp; &nbsp;name = fullname;<br />&nbsp; &nbsp; &nbsp;children =&nbsp;<span class="">null</span>;<br />&nbsp; &nbsp;}<br />&nbsp; &nbsp;indexedName = name;<br />&nbsp; &nbsp;delim = name.indexOf(<span class="">'['</span>);<br />&nbsp; &nbsp;<span class="">if</span>&nbsp;(delim &gt; -<span class="">1</span>) {<br />&nbsp; &nbsp; &nbsp;index = name.substring(delim +&nbsp;<span class="">1</span>, name.length() -&nbsp;<span class="">1</span>);<br />&nbsp; &nbsp; &nbsp;name = name.substring(<span class="">0</span>, delim);<br />&nbsp; &nbsp;}<br />&nbsp;}<br /><br />&nbsp;<span class="">public&nbsp;String&nbsp;getName()&nbsp;</span>{<br />&nbsp; &nbsp;<span class="">return</span>&nbsp;name;<br />&nbsp;}<br /><br />&nbsp;<span class="">public&nbsp;String&nbsp;getIndex()&nbsp;</span>{<br />&nbsp; &nbsp;<span class="">return</span>&nbsp;index;<br />&nbsp;}<br /><br />&nbsp;<span class="">public&nbsp;String&nbsp;getIndexedName()&nbsp;</span>{<br />&nbsp; &nbsp;<span class="">return</span>&nbsp;indexedName;<br />&nbsp;}<br /><br />&nbsp;<span class="">public&nbsp;String&nbsp;getChildren()&nbsp;</span>{<br />&nbsp; &nbsp;<span class="">return</span>&nbsp;children;<br />&nbsp;}<br /><br />&nbsp;<span class="">@Override</span><br />&nbsp;<span class="">public&nbsp;boolean&nbsp;hasNext()&nbsp;</span>{<br />&nbsp; &nbsp;<span class="">return</span>&nbsp;children&nbsp;!=&nbsp;<span class="">null</span>;<br />&nbsp;}<br /><br />&nbsp;<span class="">@Override</span><br />&nbsp;<span class="">public&nbsp;PropertyTokenizer&nbsp;next()&nbsp;</span>{<br />&nbsp; &nbsp;<span class="">return</span>&nbsp;<span class="">new</span>&nbsp;PropertyTokenizer(children);<br />&nbsp;}<br /><br />&nbsp;<span class="">@Override</span><br />&nbsp;<span class="">public&nbsp;void&nbsp;remove()&nbsp;</span>{<br />&nbsp; &nbsp;<span class="">throw</span>&nbsp;<span class="">new</span>&nbsp;UnsupportedOperationException(<br />&nbsp; &nbsp; &nbsp; &nbsp;<span class="">"Remove is not supported, as it has no meaning in the context of properties."</span>);<br />&nbsp;}<br />}</p>
</section>
<p>&nbsp;</p>
<p>可以看到，这个类传入一个字符串到构造函数，然后提供了iterator方法对解析后的子串进行遍历，是一个很常用的方法类。</p>
<section class="" data-role="paragraph">
<p>&nbsp;</p>
<p>关注Java技术栈微信公众号，在后台回复关键字：<strong><em>架构</em></strong>，可以获取更多栈长整理的架构和设计模式干货。</p>
</section>
</div>