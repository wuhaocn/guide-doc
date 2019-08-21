<h2>Shiro简介</h2>
<p>SpringMVC整合Shiro，Shiro是一个强大易用的Java安全框架,提供了认证、授权、加密和会话管理等功能。</p>
<p>&nbsp;<img src="https://images2015.cnblogs.com/blog/1107610/201702/1107610-20170216182704129-416644991.png" alt="" /></p>
<p>&nbsp;</p>
<p align="left"><strong>Authentication</strong><strong>：</strong>身份认证/登录，验证用户是不是拥有相应的身份；</p>
<p align="left"><strong>Authorization</strong><strong>：</strong>授权，即权限验证，验证某个已认证的用户是否拥有某个权限；即判断用户是否能做事情，常见的如：验证某个用户是否拥有某个角色。或者细粒度的验证某个用户对某个资源是否具有某个权限；</p>
<p align="left"><strong>Session Manager</strong><strong>：</strong>会话管理，即用户登录后就是一次会话，在没有退出之前，它的所有信息都在会话中；会话可以是普通JavaSE环境的，也可以是如Web环境的；</p>
<p align="left"><strong>Cryptography</strong><strong>：</strong>加密，保护数据的安全性，如密码加密存储到数据库，而不是明文存储；</p>
<p align="left"><strong>Web Support</strong><strong>：</strong>Web支持，可以非常容易的集成到Web环境；</p>
<p align="left">Caching：缓存，比如用户登录后，其用户信息、拥有的角色/权限不必每次去查，这样可以提高效率；</p>
<p align="left"><strong>Concurrency</strong><strong>：</strong>shiro支持多线程应用的并发验证，即如在一个线程中开启另一个线程，能把权限自动传播过去；</p>
<p align="left"><strong>Testing</strong><strong>：</strong>提供测试支持；</p>
<p align="left"><strong>Run As</strong><strong>：</strong>允许一个用户假装为另一个用户（如果他们允许）的身份进行访问；</p>
<p align="left"><strong>Remember Me</strong><strong>：</strong>记住我，这个是非常常见的功能，即一次登录后，下次再来的话不用登录了。</p>
<p><strong>记住一点，</strong><strong>Shiro</strong><strong>不会去维护用户、维护权限；这些需要我们自己去设计</strong><strong>/</strong><strong>提供；然后通过相应的接口注入给</strong><strong>Shiro</strong><strong>即可。</strong></p>
<p>&nbsp;</p>
<p>首先，我们从外部来看Shiro吧，即从应用程序角度的来观察如何使用Shiro完成工作。如下图：</p>
<p><img src="https://images2015.cnblogs.com/blog/1107610/201702/1107610-20170216182838019-1291577076.png" alt="" /></p>
<p>&nbsp;</p>
<p align="left">可以看到：应用代码直接交互的对象是Subject，也就是说Shiro的对外API核心就是Subject；其每个API的含义：</p>
<p align="left"><strong>Subject</strong><strong>：</strong>主体，代表了当前&ldquo;用户&rdquo;，这个用户不一定是一个具体的人，与当前应用交互的任何东西都是Subject，如网络爬虫，机器人等；即一个抽象概念；所有Subject都绑定到SecurityManager，与Subject的所有交互都会委托给SecurityManager；可以把Subject认为是一个门面；SecurityManager才是实际的执行者；</p>
<p align="left"><strong>SecurityManager</strong><strong>：</strong>安全管理器；即所有与安全有关的操作都会与SecurityManager交互；且它管理着所有Subject；可以看出它是Shiro的核心，它负责与后边介绍的其他组件进行交互，如果学习过SpringMVC，你可以把它看成DispatcherServlet前端控制器；</p>
<p align="left"><strong>Realm</strong><strong>：</strong>域，Shiro从从Realm获取安全数据（如用户、角色、权限），就是说SecurityManager要验证用户身份，那么它需要从Realm获取相应的用户进行比较以确定用户身份是否合法；也需要从Realm得到用户相应的角色/权限进行验证用户是否能进行操作；可以把Realm看成DataSource，即安全数据源。</p>
<p align="left">&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 接下来我们来从Shiro内部来看下Shiro的架构，如下图所示：</p>
<p>&nbsp;<img src="https://images2015.cnblogs.com/blog/1107610/201702/1107610-20170216182844941-583142950.png" alt="" /></p>
<p>&nbsp;</p>
<p align="left"><strong>Subject</strong><strong>：</strong>主体，可以看到主体可以是任何可以与应用交互的&ldquo;用户&rdquo;；</p>
<p align="left"><strong>SecurityManager</strong><strong>：</strong>相当于SpringMVC中的DispatcherServlet或者Struts2中的FilterDispatcher；是Shiro的心脏；所有具体的交互都通过SecurityManager进行控制；它管理着所有Subject、且负责进行认证和授权、及会话、缓存的管理。</p>
<p align="left"><strong>Authenticator</strong><strong>：</strong>认证器，负责主体认证的，这是一个扩展点，如果用户觉得Shiro默认的不好，可以自定义实现；其需要认证策略（Authentication Strategy），即什么情况下算用户认证通过了；</p>
<p align="left"><strong>Authrizer</strong><strong>：</strong>授权器，或者访问控制器，用来决定主体是否有权限进行相应的操作；即控制着用户能访问应用中的哪些功能；</p>
<p align="left"><strong>Realm</strong><strong>：</strong>可以有1个或多个Realm，可以认为是安全实体数据源，即用于获取安全实体的；可以是JDBC实现，也可以是LDAP实现，或者内存实现等等；由用户提供；注意：Shiro不知道你的用户/权限存储在哪及以何种格式存储；所以我们一般在应用中都需要实现自己的Realm；</p>
<p align="left"><strong>SessionManager</strong><strong>：</strong>如果写过Servlet就应该知道Session的概念，Session呢需要有人去管理它的生命周期，这个组件就是SessionManager；而Shiro并不仅仅可以用在Web环境，也可以用在如普通的JavaSE环境、EJB等环境；所有呢，Shiro就抽象了一个自己的Session来管理主体与应用之间交互的数据；这样的话，比如我们在Web环境用，刚开始是一台Web服务器；接着又上了台EJB服务器；这时想把两台服务器的会话数据放到一个地方，这个时候就可以实现自己的分布式会话（如把数据放到Memcached服务器）；</p>
<p align="left"><strong>SessionDAO</strong><strong>：</strong>DAO大家都用过，数据访问对象，用于会话的CRUD，比如我们想把Session保存到数据库，那么可以实现自己的SessionDAO，通过如JDBC写到数据库；比如想把Session放到Memcached中，可以实现自己的Memcached SessionDAO；另外SessionDAO中可以使用Cache进行缓存，以提高性能；</p>
<p align="left"><strong>CacheManager</strong><strong>：</strong>缓存控制器，来管理如用户、角色、权限等的缓存的；因为这些数据基本上很少去改变，放到缓存中后可以提高访问的性能</p>
<p align="left"><strong>Cryptography</strong><strong>：</strong>密码模块，Shiro提高了一些常见的加密组件用于如密码加密/解密的。</p>
<h2 align="center">自定义Realm</h2>
<p>public class ShiroRealm extends AuthorizingRealm{</p>
<p>&nbsp;</p>
<p>}</p>
<p align="left"><strong>1</strong><strong>、</strong><strong>ShiroRealm</strong><strong>父类</strong><strong>AuthorizingRealm</strong><strong>将获取</strong><strong>Subject</strong><strong>相关信息分成两步</strong>：获取身份验证信息（doGetAuthenticationInfo）及授权信息（doGetAuthorizationInfo）；</p>
<p align="left"><strong>2</strong><strong>、</strong><strong>doGetAuthenticationInfo</strong><strong>获取身份验证相关信息</strong>：首先根据传入的用户名获取User信息；然后如果user为空，那么抛出没找到帐号异常UnknownAccountException；如果user找到但锁定了抛出锁定异常LockedAccountException；最后生成AuthenticationInfo信息，交给间接父类AuthenticatingRealm使用CredentialsMatcher进行判断密码是否匹配，如果不匹配将抛出密码错误异常IncorrectCredentialsException；另外如果密码重试此处太多将抛出超出重试次数异常ExcessiveAttemptsException；在组装SimpleAuthenticationInfo信息时，需要传入：身份信息（用户名）、凭据（密文密码）、盐（username+salt），CredentialsMatcher使用盐加密传入的明文密码和此处的密文密码进行匹配。</p>
<p align="left"><strong>3</strong><strong>、</strong><strong>doGetAuthorizationInfo</strong><strong>获取授权信息</strong>：PrincipalCollection是一个身份集合，因为我们现在就一个Realm，所以直接调用getPrimaryPrincipal得到之前传入的用户名即可；然后根据用户名调用UserService接口获取角色及权限信息。</p>
<h3>AuthenticationToken</h3>
<p>&nbsp;<img src="https://images2015.cnblogs.com/blog/1107610/201702/1107610-20170216182856347-412437771.png" alt="" /></p>
<p>&nbsp;</p>
<p>AuthenticationToken用于收集用户提交的身份（如用户名）及凭据（如密码）：</p>
<div>
<ol>
<li><strong>public</strong>&nbsp;<strong>interface</strong>&nbsp;AuthenticationToken&nbsp;<strong>extends</strong>&nbsp;Serializable&nbsp;{&nbsp;&nbsp;</li>
<li>&nbsp;&nbsp;&nbsp;&nbsp;Object&nbsp;getPrincipal();&nbsp;//身份&nbsp;&nbsp;</li>
<li>&nbsp;&nbsp;&nbsp;&nbsp;Object&nbsp;getCredentials();&nbsp;//凭据&nbsp;&nbsp;</li>
<li>}&nbsp;&nbsp;</li>
</ol>
</div>
<p align="left">扩展接口RememberMeAuthenticationToken：提供了&ldquo;boolean isRememberMe()&rdquo;现&ldquo;记住我&rdquo;的功能；</p>
<p align="left">扩展接口是HostAuthenticationToken：提供了&ldquo;String getHost()&rdquo;方法用于获取用户&ldquo;主机&rdquo;的功能。</p>
<p align="left">&nbsp;</p>
<p align="left">Shiro提供了一个直接拿来用的UsernamePasswordToken，用于实现用户名/密码Token组，另外其实现了RememberMeAuthenticationToken和HostAuthenticationToken，可以实现记住我及主机验证的支持。</p>
<h3>AuthenticationInfo</h3>
<p>&nbsp;<img src="https://images2015.cnblogs.com/blog/1107610/201702/1107610-20170216182906425-588426570.png" alt="" /></p>
<p>&nbsp;</p>
<p align="left">AuthenticationInfo有两个作用：</p>
<p align="left">1、如果Realm是AuthenticatingRealm子类，则提供给AuthenticatingRealm内部使用的CredentialsMatcher进行凭据验证；（如果没有继承它需要在自己的Realm中自己实现验证）；</p>
<p align="left">2、提供给SecurityManager来创建Subject（提供身份信息）；</p>
<p align="left">&nbsp;</p>
<p align="left">MergableAuthenticationInfo用于提供在多Realm时合并AuthenticationInfo的功能，主要合并Principal、如果是其他的如credentialsSalt，会用后边的信息覆盖前边的。</p>
<p align="left">&nbsp;</p>
<p align="left">比如HashedCredentialsMatcher，在验证时会判断AuthenticationInfo是否是SaltedAuthenticationInfo子类，来获取盐信息。</p>
<p align="left">&nbsp;</p>
<p align="left">Account相当于我们之前的User，SimpleAccount是其一个实现；在IniRealm、PropertiesRealm这种静态创建帐号信息的场景中使用，这些Realm直接继承了SimpleAccountRealm，而SimpleAccountRealm提供了相关的API来动态维护SimpleAccount；即可以通过这些API来动态增删改查SimpleAccount；动态增删改查角色/权限信息。及如果您的帐号不是特别多，可以使用这种方式，具体请参考SimpleAccountRealm Javadoc。</p>
<p align="left">&nbsp;</p>
<p align="left">其他情况一般返回SimpleAuthenticationInfo即可。</p>
<h3>PrincipalCollection</h3>
<p>&nbsp;<img src="https://images2015.cnblogs.com/blog/1107610/201702/1107610-20170216182916957-1699024370.png" alt="" /></p>
<p>&nbsp;</p>
<p>因为我们可以在Shiro中同时配置多个Realm，所以呢身份信息可能就有多个；因此其提供了PrincipalCollection用于聚合这些身份信息：</p>
<div>
<ol>
<li><strong>public</strong>&nbsp;<strong>interface</strong>&nbsp;PrincipalCollection&nbsp;<strong>extends</strong>&nbsp;Iterable,&nbsp;Serializable&nbsp;{&nbsp;&nbsp;</li>
<li>&nbsp;&nbsp;&nbsp;&nbsp;Object&nbsp;getPrimaryPrincipal();&nbsp;//得到主要的身份&nbsp;&nbsp;</li>
<li>&nbsp;&nbsp;&nbsp;&nbsp;&lt;T&gt;&nbsp;T&nbsp;oneByType(Class&lt;T&gt;&nbsp;type);&nbsp;//根据身份类型获取第一个&nbsp;&nbsp;</li>
<li>&nbsp;&nbsp;&nbsp;&nbsp;&lt;T&gt;&nbsp;Collection&lt;T&gt;&nbsp;byType(Class&lt;T&gt;&nbsp;type);&nbsp;//根据身份类型获取一组&nbsp;&nbsp;</li>
<li>&nbsp;&nbsp;&nbsp;&nbsp;List&nbsp;asList();&nbsp;//转换为List&nbsp;&nbsp;</li>
<li>&nbsp;&nbsp;&nbsp;&nbsp;Set&nbsp;asSet();&nbsp;//转换为Set&nbsp;&nbsp;</li>
<li>&nbsp;&nbsp;&nbsp;&nbsp;Collection&nbsp;fromRealm(String&nbsp;realmName);&nbsp;//根据Realm名字获取&nbsp;&nbsp;</li>
<li>&nbsp;&nbsp;&nbsp;&nbsp;Set&lt;String&gt;&nbsp;getRealmNames();&nbsp;//获取所有身份验证通过的Realm名字&nbsp;&nbsp;</li>
<li>&nbsp;&nbsp;&nbsp;&nbsp;<strong>boolean</strong>&nbsp;isEmpty();&nbsp;//判断是否为空&nbsp;&nbsp;</li>
</ol>
<p align="left">10. }&nbsp;&nbsp;</p>
</div>
<p>因为PrincipalCollection聚合了多个，此处最需要注意的是getPrimaryPrincipal，如果只有一个Principal那么直接返回即可，如果有多个Principal，则返回第一个（因为内部使用Map存储，所以可以认为是返回任意一个）；oneByType / byType根据凭据的类型返回相应的Principal；fromRealm根据Realm名字（每个Principal都与一个Realm关联）获取相应的Principal。</p>
<p>目前Shiro只提供了一个实现SimplePrincipalCollection，还记得之前的AuthenticationStrategy实现嘛，用于在多Realm时判断是否满足条件的，在大多数实现中（继承了AbstractAuthenticationStrategy）afterAttempt方法会进行AuthenticationInfo（实现了MergableAuthenticationInfo）的merge，比如SimpleAuthenticationInfo会合并多个Principal为一个PrincipalCollection。</p>
<h3>AuthorizationInfo</h3>
<p>&nbsp;<img src="https://images2015.cnblogs.com/blog/1107610/201702/1107610-20170216182925207-1572991216.png" alt="" /></p>
<p>&nbsp;</p>
<p>AuthorizationInfo用于聚合授权信息的：</p>
<div>
<ol>
<li><strong>public</strong>&nbsp;<strong>interface</strong>&nbsp;AuthorizationInfo&nbsp;<strong>extends</strong>&nbsp;Serializable&nbsp;{&nbsp;&nbsp;</li>
<li>&nbsp;&nbsp;&nbsp;&nbsp;Collection&lt;String&gt;&nbsp;getRoles();&nbsp;//获取角色字符串信息&nbsp;&nbsp;</li>
<li>&nbsp;&nbsp;&nbsp;&nbsp;Collection&lt;String&gt;&nbsp;getStringPermissions();&nbsp;//获取权限字符串信息&nbsp;&nbsp;</li>
<li>&nbsp;&nbsp;&nbsp;&nbsp;Collection&lt;Permission&gt;&nbsp;getObjectPermissions();&nbsp;//获取Permission对象信息&nbsp;&nbsp;</li>
<li>}&nbsp;&nbsp;&nbsp;</li>
</ol>
</div>
<p>&nbsp;</p>
<p align="left">当我们使用AuthorizingRealm时，如果身份验证成功，在进行授权时就通过doGetAuthorizationInfo方法获取角色/权限信息用于授权验证。</p>
<p align="left">&nbsp;</p>
<p align="left">Shiro提供了一个实现SimpleAuthorizationInfo，大多数时候使用这个即可。</p>
<p align="left">&nbsp;</p>
<p align="left">对于Account及SimpleAccount，之前的【6.3 AuthenticationInfo】已经介绍过了，用于SimpleAccountRealm子类，实现动态角色/权限维护的。</p>
<h2 align="center">Subject</h2>
<p>&nbsp;<img src="https://images2015.cnblogs.com/blog/1107610/201702/1107610-20170216182936050-1308988136.png" alt="" /></p>
<p>&nbsp;</p>
<p>Subject是Shiro的核心对象，基本所有身份验证、授权都是通过Subject完成。</p>
<p align="left"><strong>1</strong><strong>、身份信息获取</strong></p>
<p align="left"><strong>Java</strong><strong>代码</strong><strong>&nbsp;&nbsp;</strong></p>
<div>
<ol>
<li>Object&nbsp;getPrincipal();&nbsp;//Primary&nbsp;Principal&nbsp;&nbsp;</li>
<li>PrincipalCollection&nbsp;getPrincipals();&nbsp;//&nbsp;PrincipalCollection&nbsp;&nbsp;&nbsp;</li>
</ol>
</div>
<p align="left">&nbsp;</p>
<p align="left"><strong>2</strong><strong>、身份验证</strong></p>
<p align="left"><strong>Java</strong><strong>代码</strong><strong>&nbsp;&nbsp;</strong></p>
<div>
<ol>
<li><strong>void</strong>&nbsp;login(AuthenticationToken&nbsp;token)&nbsp;<strong>throws</strong>&nbsp;AuthenticationException;&nbsp;&nbsp;</li>
<li><strong>boolean</strong>&nbsp;isAuthenticated();&nbsp;&nbsp;</li>
<li><strong>boolean</strong>&nbsp;isRemembered();&nbsp;&nbsp;</li>
</ol>
</div>
<p align="left">通过login登录，如果登录失败将抛出相应的AuthenticationException，如果登录成功调用isAuthenticated就会返回true，即已经通过身份验证；如果isRemembered返回true，表示是通过记住我功能登录的而不是调用login方法登录的。isAuthenticated/isRemembered是互斥的，即如果其中一个返回true，另一个返回false。</p>
<p align="left">&nbsp;&nbsp;</p>
<p align="left"><strong>3</strong><strong>、角色授权验证</strong>&nbsp;</p>
<p align="left"><strong>Java</strong><strong>代码</strong><strong>&nbsp;&nbsp;</strong></p>
<div>
<ol>
<li><strong>boolean</strong>&nbsp;hasRole(String&nbsp;roleIdentifier);&nbsp;&nbsp;</li>
<li><strong>boolean</strong>[]&nbsp;hasRoles(List&lt;String&gt;&nbsp;roleIdentifiers);&nbsp;&nbsp;</li>
<li><strong>boolean</strong>&nbsp;hasAllRoles(Collection&lt;String&gt;&nbsp;roleIdentifiers);&nbsp;&nbsp;</li>
<li><strong>void</strong>&nbsp;checkRole(String&nbsp;roleIdentifier)&nbsp;<strong>throws</strong>&nbsp;AuthorizationException;&nbsp;&nbsp;</li>
<li><strong>void</strong>&nbsp;checkRoles(Collection&lt;String&gt;&nbsp;roleIdentifiers)&nbsp;<strong>throws</strong>&nbsp;AuthorizationException;&nbsp;&nbsp;</li>
<li><strong>void</strong>&nbsp;checkRoles(String...&nbsp;roleIdentifiers)&nbsp;<strong>throws</strong>&nbsp;AuthorizationException;&nbsp;&nbsp;&nbsp;</li>
</ol>
</div>
<p align="left">hasRole*进行角色验证，验证后返回true/false；而checkRole*验证失败时抛出AuthorizationException异常。&nbsp;</p>
<p align="left">&nbsp;</p>
<p align="left"><strong>4</strong><strong>、权限授权验证</strong></p>
<p align="left"><strong>Java</strong><strong>代码</strong><strong>&nbsp;&nbsp;</strong></p>
<div>
<ol>
<li><strong>boolean</strong>&nbsp;isPermitted(String&nbsp;permission);&nbsp;&nbsp;</li>
<li><strong>boolean</strong>&nbsp;isPermitted(Permission&nbsp;permission);&nbsp;&nbsp;</li>
<li><strong>boolean</strong>[]&nbsp;isPermitted(String...&nbsp;permissions);&nbsp;&nbsp;</li>
<li><strong>boolean</strong>[]&nbsp;isPermitted(List&lt;Permission&gt;&nbsp;permissions);&nbsp;&nbsp;</li>
<li><strong>boolean</strong>&nbsp;isPermittedAll(String...&nbsp;permissions);&nbsp;&nbsp;</li>
<li><strong>boolean</strong>&nbsp;isPermittedAll(Collection&lt;Permission&gt;&nbsp;permissions);&nbsp;&nbsp;</li>
<li><strong>void</strong>&nbsp;checkPermission(String&nbsp;permission)&nbsp;<strong>throws</strong>&nbsp;AuthorizationException;&nbsp;&nbsp;</li>
<li><strong>void</strong>&nbsp;checkPermission(Permission&nbsp;permission)&nbsp;<strong>throws</strong>&nbsp;AuthorizationException;&nbsp;&nbsp;</li>
<li><strong>void</strong>&nbsp;checkPermissions(String...&nbsp;permissions)&nbsp;<strong>throws</strong>&nbsp;AuthorizationException;&nbsp;&nbsp;</li>
</ol>
<p align="left">10.&nbsp;<strong>void</strong>&nbsp;checkPermissions(Collection&lt;Permission&gt;&nbsp;permissions)&nbsp;<strong>throws</strong>&nbsp;AuthorizationException;&nbsp;&nbsp;</p>
</div>
<p align="left">isPermitted*进行权限验证，验证后返回true/false；而checkPermission*验证失败时抛出AuthorizationException。</p>
<p align="left">&nbsp;</p>
<p align="left"><strong>5</strong><strong>、会话</strong></p>
<p align="left"><strong>Java</strong><strong>代码</strong><strong>&nbsp;&nbsp;</strong></p>
<div>
<ol>
<li>Session&nbsp;getSession();&nbsp;//相当于getSession(true)&nbsp;&nbsp;</li>
<li>Session&nbsp;getSession(<strong>boolean</strong>&nbsp;create);&nbsp;&nbsp;&nbsp;&nbsp;</li>
</ol>
</div>
<p align="left">类似于Web中的会话。如果登录成功就相当于建立了会话，接着可以使用getSession获取；如果create=false如果没有会话将返回null，而create=true如果没有会话会强制创建一个。</p>
<p align="left">&nbsp;</p>
<p align="left"><strong>6</strong><strong>、退出</strong>&nbsp;</p>
<p align="left"><strong>Java</strong><strong>代码</strong><strong>&nbsp;&nbsp;</strong></p>
<div>
<ol>
<li><strong>void</strong>&nbsp;logout();&nbsp;&nbsp;</li>
</ol>
</div>
<p align="left">&nbsp;</p>
<p align="left"><strong>7</strong><strong>、</strong><strong>RunAs</strong>&nbsp;&nbsp;</p>
<p align="left"><strong>Java</strong><strong>代码</strong><strong>&nbsp;&nbsp;</strong></p>
<div>
<ol>
<li><strong>void</strong>&nbsp;runAs(PrincipalCollection&nbsp;principals)&nbsp;<strong>throws</strong>&nbsp;NullPointerException,&nbsp;IllegalStateException;&nbsp;&nbsp;</li>
<li><strong>boolean</strong>&nbsp;isRunAs();&nbsp;&nbsp;</li>
<li>PrincipalCollection&nbsp;getPreviousPrincipals();&nbsp;&nbsp;</li>
<li>PrincipalCollection&nbsp;releaseRunAs();&nbsp;&nbsp;&nbsp;</li>
</ol>
</div>
<p align="left">RunAs即实现&ldquo;允许A假设为B身份进行访问&rdquo;；通过调用subject.runAs(b)进行访问；接着调用subject.getPrincipals将获取到B的身份；此时调用isRunAs将返回true；而a的身份需要通过subject. getPreviousPrincipals获取；如果不需要RunAs了调用subject. releaseRunAs即可。</p>
<p align="left">&nbsp;</p>
<p align="left"><strong>8</strong><strong>、多线程</strong></p>
<p align="left"><strong>Java</strong><strong>代码</strong><strong>&nbsp;&nbsp;</strong></p>
<div>
<ol>
<li>&lt;V&gt;&nbsp;V&nbsp;execute(Callable&lt;V&gt;&nbsp;callable)&nbsp;<strong>throws</strong>&nbsp;ExecutionException;&nbsp;&nbsp;</li>
<li><strong>void</strong>&nbsp;execute(Runnable&nbsp;runnable);&nbsp;&nbsp;</li>
<li>&lt;V&gt;&nbsp;Callable&lt;V&gt;&nbsp;associateWith(Callable&lt;V&gt;&nbsp;callable);&nbsp;&nbsp;</li>
<li>Runnable&nbsp;associateWith(Runnable&nbsp;runnable);&nbsp;&nbsp;&nbsp;</li>
</ol>
</div>
<p align="left">实现线程之间的Subject传播，因为Subject是线程绑定的；因此在多线程执行中需要传播到相应的线程才能获取到相应的Subject。最简单的办法就是通过execute(runnable/callable实例)直接调用；或者通过associateWith(runnable/callable实例)得到一个包装后的实例；它们都是通过：1、把当前线程的Subject绑定过去；2、在线程执行结束后自动释放。</p>
<p align="left">&nbsp;</p>
<p align="left">Subject自己不会实现相应的身份验证/授权逻辑，而是通过DelegatingSubject委托给SecurityManager实现；及可以理解为Subject是一个面门。</p>
<p align="left">&nbsp;</p>
<p align="left">对于Subject的构建一般没必要我们去创建；一般通过SecurityUtils.getSubject()获取：</p>
<p align="left"><strong>Java</strong><strong>代码</strong><strong>&nbsp;&nbsp;</strong></p>
<div>
<ol>
<li><strong>public</strong>&nbsp;<strong>static</strong>&nbsp;Subject&nbsp;getSubject()&nbsp;{&nbsp;&nbsp;</li>
<li>&nbsp;&nbsp;&nbsp;&nbsp;Subject&nbsp;subject&nbsp;=&nbsp;ThreadContext.getSubject();&nbsp;&nbsp;</li>
<li>&nbsp;&nbsp;&nbsp;&nbsp;<strong>if</strong>&nbsp;(subject&nbsp;==&nbsp;<strong>null</strong>)&nbsp;{&nbsp;&nbsp;</li>
<li>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;subject&nbsp;=&nbsp;(<strong>new</strong>&nbsp;Subject.Builder()).buildSubject();&nbsp;&nbsp;</li>
<li>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;ThreadContext.bind(subject);&nbsp;&nbsp;</li>
<li>&nbsp;&nbsp;&nbsp;&nbsp;}&nbsp;&nbsp;</li>
<li>&nbsp;&nbsp;&nbsp;&nbsp;<strong>return</strong>&nbsp;subject;&nbsp;&nbsp;</li>
<li>}&nbsp;&nbsp;&nbsp;</li>
</ol>
</div>
<p align="left">即首先查看当前线程是否绑定了Subject，如果没有通过Subject.Builder构建一个然后绑定到现场返回。</p>
<p align="left">&nbsp;</p>
<p align="left">如果想自定义创建，可以通过：</p>
<p align="left"><strong>Java</strong><strong>代码</strong><strong>&nbsp;&nbsp;</strong></p>
<div>
<ol>
<li><strong>new</strong>&nbsp;Subject.Builder().principals(身份).authenticated(<strong>true</strong>/<strong>false</strong>).buildSubject()&nbsp;&nbsp;</li>
</ol>
</div>
<p align="left">这种可以创建相应的Subject实例了，然后自己绑定到线程即可。在new Builder()时如果没有传入SecurityManager，自动调用SecurityUtils.getSecurityManager获取；也可以自己传入一个实例。</p>
<h2>Shiro的jstl标签</h2>
<p>Shiro提供了JSTL标签用于在JSP/GSP页面进行权限控制，如根据登录用户显示相应的页面按钮。</p>
<p align="left">&nbsp;</p>
<p align="left"><strong>导入标签库</strong></p>
<p align="left"><strong>Java</strong><strong>代码</strong><strong>&nbsp;&nbsp;</strong></p>
<div>
<ol>
<li>&lt;%@taglib&nbsp;prefix="shiro"&nbsp;uri="http://shiro.apache.org/tags"&nbsp;%&gt;&nbsp;&nbsp;</li>
</ol>
</div>
<p align="left">标签库定义在shiro-web.jar包下的META-INF/shiro.tld中定义。</p>
<p align="left">&nbsp;</p>
<p align="left"><strong>guest</strong><strong>标签</strong>&nbsp;</p>
<p align="left"><strong>Java</strong><strong>代码</strong><strong>&nbsp;&nbsp;</strong></p>
<div>
<ol>
<li>&lt;shiro:guest&gt;&nbsp;&nbsp;</li>
<li>欢迎游客访问，&lt;a&nbsp;href="${pageContext.request.contextPath}/login.jsp"&gt;登录&lt;/a&gt;&nbsp;&nbsp;</li>
<li>&lt;/shiro:guest&gt;&nbsp;&nbsp;&nbsp;</li>
</ol>
</div>
<p align="left">用户没有身份验证时显示相应信息，即游客访问信息。</p>
<p align="left">&nbsp;</p>
<p align="left"><strong>user</strong><strong>标签</strong>&nbsp;</p>
<p align="left"><strong>Java</strong><strong>代码</strong><strong>&nbsp;&nbsp;</strong></p>
<div>
<ol>
<li>&lt;shiro:user&gt;&nbsp;&nbsp;</li>
<li>欢迎[&lt;shiro:principal/&gt;]登录，&lt;a&nbsp;href="${pageContext.request.contextPath}/logout"&gt;退出&lt;/a&gt;&nbsp;&nbsp;</li>
<li>&lt;/shiro:user&gt;&nbsp;&nbsp;&nbsp;</li>
</ol>
</div>
<p align="left">用户已经身份验证/记住我登录后显示相应的信息。</p>
<p align="left">&nbsp;&nbsp;</p>
<p align="left"><strong>authenticated</strong><strong>标签</strong>&nbsp;</p>
<p align="left"><strong>Java</strong><strong>代码</strong><strong>&nbsp;&nbsp;</strong></p>
<div>
<ol>
<li>&lt;shiro:authenticated&gt;&nbsp;&nbsp;</li>
<li>&nbsp;&nbsp;&nbsp;&nbsp;用户[&lt;shiro:principal/&gt;]已身份验证通过&nbsp;&nbsp;</li>
<li>&lt;/shiro:authenticated&gt;&nbsp;&nbsp;&nbsp;</li>
</ol>
</div>
<p align="left">用户已经身份验证通过，即Subject.login登录成功，不是记住我登录的。&nbsp; &nbsp;&nbsp;</p>
<p align="left">&nbsp;</p>
<p align="left"><strong>notAuthenticated</strong><strong>标签</strong></p>
<p align="left">&lt;shiro:notAuthenticated&gt;</p>
<p align="left">&nbsp;&nbsp;&nbsp; 未身份验证（包括记住我）</p>
<p align="left">&lt;/shiro:notAuthenticated&gt;&nbsp;</p>
<p align="left">用户已经身份验证通过，即没有调用Subject.login进行登录，包括记住我自动登录的也属于未进行身份验证。&nbsp;</p>
<p align="left">&nbsp;</p>
<p align="left"><strong>principal</strong><strong>标签</strong>&nbsp;</p>
<p align="left">&lt;shiro: principal/&gt;</p>
<p align="left">显示用户身份信息，默认调用Subject.getPrincipal()获取，即Primary Principal。</p>
<p align="left">&nbsp;</p>
<p align="left"><strong>Java</strong><strong>代码</strong><strong>&nbsp;</strong></p>
<div>
<ol>
<li>&lt;shiro:principal&nbsp;type="java.lang.String"/&gt;&nbsp;&nbsp;</li>
</ol>
</div>
<p align="left">相当于Subject.getPrincipals().oneByType(String.class)。&nbsp;</p>
<p align="left">&nbsp;</p>
<p align="left"><strong>Java</strong><strong>代码</strong><strong>&nbsp;</strong></p>
<div>
<ol>
<li>&lt;shiro:principal&nbsp;type="java.lang.String"/&gt;&nbsp;&nbsp;</li>
</ol>
</div>
<p align="left">相当于Subject.getPrincipals().oneByType(String.class)。</p>
<p align="left">&nbsp;</p>
<p align="left"><strong>Java</strong><strong>代码</strong><strong>&nbsp;</strong></p>
<div>
<ol>
<li>&lt;shiro:principal&nbsp;property="username"/&gt;&nbsp;&nbsp;</li>
</ol>
</div>
<p align="left">相当于((User)Subject.getPrincipals()).getUsername()。&nbsp;&nbsp;&nbsp;</p>
<p align="left">&nbsp;</p>
<p align="left"><strong>hasRole</strong><strong>标签</strong>&nbsp;</p>
<p align="left"><strong>Java</strong><strong>代码</strong><strong>&nbsp;</strong></p>
<div>
<ol>
<li>&lt;shiro:hasRole&nbsp;name="admin"&gt;&nbsp;&nbsp;</li>
<li>&nbsp;&nbsp;&nbsp;&nbsp;用户[&lt;shiro:principal/&gt;]拥有角色admin&lt;br/&gt;&nbsp;&nbsp;</li>
<li>&lt;/shiro:hasRole&gt;&nbsp;&nbsp;&nbsp;</li>
</ol>
</div>
<p align="left">如果当前Subject有角色将显示body体内容。</p>
<p align="left">&nbsp;</p>
<p align="left"><strong>hasAnyRoles</strong><strong>标签</strong>&nbsp;</p>
<p align="left"><strong>Java</strong><strong>代码</strong><strong>&nbsp;</strong></p>
<div>
<ol>
<li>&lt;shiro:hasAnyRoles&nbsp;name="admin,user"&gt;&nbsp;&nbsp;</li>
<li>&nbsp;&nbsp;&nbsp;&nbsp;用户[&lt;shiro:principal/&gt;]拥有角色admin或user&lt;br/&gt;&nbsp;&nbsp;</li>
<li>&lt;/shiro:hasAnyRoles&gt;&nbsp;&nbsp;&nbsp;</li>
</ol>
</div>
<p align="left">如果当前Subject有任意一个角色（或的关系）将显示body体内容。&nbsp;</p>
<p align="left">&nbsp;</p>
<p align="left"><strong>lacksRole</strong><strong>标签</strong>&nbsp;</p>
<p align="left"><strong>Java</strong><strong>代码</strong><strong>&nbsp;</strong></p>
<div>
<ol>
<li>&lt;shiro:lacksRole&nbsp;name="abc"&gt;&nbsp;&nbsp;</li>
<li>&nbsp;&nbsp;&nbsp;&nbsp;用户[&lt;shiro:principal/&gt;]没有角色abc&lt;br/&gt;&nbsp;&nbsp;</li>
<li>&lt;/shiro:lacksRole&gt;&nbsp;&nbsp;&nbsp;</li>
</ol>
</div>
<p align="left">如果当前Subject没有角色将显示body体内容。&nbsp;</p>
<p align="left">&nbsp;&nbsp;</p>
<p align="left"><strong>hasPermission</strong><strong>标签</strong></p>
<p align="left"><strong>Java</strong><strong>代码</strong><strong>&nbsp;</strong></p>
<div>
<ol>
<li>&lt;shiro:hasPermission&nbsp;name="user:create"&gt;&nbsp;&nbsp;</li>
<li>&nbsp;&nbsp;&nbsp;&nbsp;用户[&lt;shiro:principal/&gt;]拥有权限user:create&lt;br/&gt;&nbsp;&nbsp;</li>
<li>&lt;/shiro:hasPermission&gt;&nbsp;&nbsp;&nbsp;</li>
</ol>
</div>
<p align="left">如果当前Subject有权限将显示body体内容。&nbsp;</p>
<p align="left">&nbsp;&nbsp;</p>
<p align="left"><strong>lacksPermission</strong><strong>标签</strong></p>
<p align="left"><strong>Java</strong><strong>代码</strong><strong>&nbsp;</strong></p>
<div>
<ol>
<li>&lt;shiro:lacksPermission&nbsp;name="org:create"&gt;&nbsp;&nbsp;</li>
<li>&nbsp;&nbsp;&nbsp;&nbsp;用户[&lt;shiro:principal/&gt;]没有权限org:create&lt;br/&gt;&nbsp;&nbsp;</li>
<li>&lt;/shiro:lacksPermission&gt;&nbsp;&nbsp;&nbsp;</li>
</ol>
</div>
<p align="left">如果当前Subject没有权限将显示body体内容。</p>
<p align="left">&nbsp;</p>
<p align="left">另外又提供了几个权限控制相关的标签：</p>
<h2 align="center">Shiro与web</h2>
<p>与spring集成：在Web.xml中</p>
<div>
<ol>
<li>&lt;filter&gt;&nbsp;&nbsp;</li>
<li>&nbsp;&nbsp;&nbsp;&nbsp;&lt;filter-name&gt;shiroFilter&lt;/filter-name&gt;&nbsp;&nbsp;</li>
<li>&nbsp;&nbsp;&nbsp;&nbsp;&lt;filter-<strong>class</strong>&gt;org.springframework.web.filter.DelegatingFilterProxy&lt;/filter-<strong>class</strong>&gt;&nbsp;&nbsp;</li>
<li>&nbsp;&nbsp;&nbsp;&nbsp;&lt;init-param&gt;&nbsp;&nbsp;</li>
<li>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;param-name&gt;targetFilterLifecycle&lt;/param-name&gt;&nbsp;&nbsp;</li>
<li>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;param-value&gt;<strong>true</strong>&lt;/param-value&gt;&nbsp;&nbsp;</li>
<li>&nbsp;&nbsp;&nbsp;&nbsp;&lt;/init-param&gt;&nbsp;&nbsp;</li>
<li>&lt;/filter&gt;&nbsp;&nbsp;</li>
<li>&lt;filter-mapping&gt;&nbsp;&nbsp;</li>
<li>10. &nbsp;&nbsp;&nbsp;&nbsp;&lt;filter-name&gt;shiroFilter&lt;/filter-name&gt;&nbsp;&nbsp;</li>
<li>11. &nbsp;&nbsp;&nbsp;&nbsp;&lt;url-pattern&gt;/*&lt;/url-pattern&gt;&nbsp;&nbsp;</li>
</ol>
<p align="left">12. &lt;/filter-mapping&gt;&nbsp;&nbsp;&nbsp;</p>
</div>
<p>DelegatingFilterProxy作用是自动到spring容器查找名字为shiroFilter（filter-name）的bean并把所有Filter的操作委托给它。然后将ShiroFilter配置到spring容器即可：</p>
<h2 align="center">Shiro集成spring</h2>
<div>
<ol>
<li>&lt;!--&nbsp;缓存管理器&nbsp;使用Ehcache实现&nbsp;--&gt;&nbsp;&nbsp;</li>
<li>&lt;bean&nbsp;id="cacheManager"&nbsp;<strong>class</strong>="org.apache.shiro.cache.ehcache.EhCacheManager"&gt;&nbsp;&nbsp;</li>
<li>&nbsp;&nbsp;&nbsp;&nbsp;&lt;property&nbsp;name="cacheManagerConfigFile"&nbsp;value="classpath:ehcache.xml"/&gt;&nbsp;&nbsp;</li>
<li>&lt;/bean&gt;&nbsp;&nbsp;</li>
<li>&nbsp;&nbsp;</li>
<li>&lt;!--&nbsp;凭证匹配器&nbsp;--&gt;&nbsp;&nbsp;</li>
<li>&lt;bean&nbsp;id="credentialsMatcher"&nbsp;<strong>class</strong>="&nbsp;&nbsp;</li>
<li>com.github.zhangkaitao.shiro.chapter12.credentials.RetryLimitHashedCredentialsMatcher"&gt;&nbsp;&nbsp;</li>
<li>&nbsp;&nbsp;&nbsp;&nbsp;&lt;constructor-arg&nbsp;ref="cacheManager"/&gt;&nbsp;&nbsp;</li>
<li>10. &nbsp;&nbsp;&nbsp;&nbsp;&lt;property&nbsp;name="hashAlgorithmName"&nbsp;value="md5"/&gt;&nbsp;&nbsp;</li>
<li>11. &nbsp;&nbsp;&nbsp;&nbsp;&lt;property&nbsp;name="hashIterations"&nbsp;value="2"/&gt;&nbsp;&nbsp;</li>
<li>12. &nbsp;&nbsp;&nbsp;&nbsp;&lt;property&nbsp;name="storedCredentialsHexEncoded"&nbsp;value="true"/&gt;&nbsp;&nbsp;</li>
</ol>
<p align="left">13. &lt;/bean&gt;&nbsp;&nbsp;</p>
<ol>
<li>14. &nbsp;&nbsp;</li>
</ol>
<p align="left">15. &lt;!--&nbsp;Realm实现&nbsp;--&gt;&nbsp;&nbsp;</p>
<p align="left">16. &lt;bean&nbsp;id="userRealm"&nbsp;<strong>class</strong>="com.github.zhangkaitao.shiro.chapter12.realm.UserRealm"&gt;&nbsp;&nbsp;</p>
<ol>
<li>17. &nbsp;&nbsp;&nbsp;&nbsp;&lt;property&nbsp;name="userService"&nbsp;ref="userService"/&gt;&nbsp;&nbsp;</li>
<li>18. &nbsp;&nbsp;&nbsp;&nbsp;&lt;property&nbsp;name="credentialsMatcher"&nbsp;ref="credentialsMatcher"/&gt;&nbsp;&nbsp;</li>
<li>19. &nbsp;&nbsp;&nbsp;&nbsp;&lt;property&nbsp;name="cachingEnabled"&nbsp;value="true"/&gt;&nbsp;&nbsp;</li>
<li>20. &nbsp;&nbsp;&nbsp;&nbsp;&lt;property&nbsp;name="authenticationCachingEnabled"&nbsp;value="true"/&gt;&nbsp;&nbsp;</li>
<li>21. &nbsp;&nbsp;&nbsp;&nbsp;&lt;property&nbsp;name="authenticationCacheName"&nbsp;value="authenticationCache"/&gt;&nbsp;&nbsp;</li>
<li>22. &nbsp;&nbsp;&nbsp;&nbsp;&lt;property&nbsp;name="authorizationCachingEnabled"&nbsp;value="true"/&gt;&nbsp;&nbsp;</li>
<li>23. &nbsp;&nbsp;&nbsp;&nbsp;&lt;property&nbsp;name="authorizationCacheName"&nbsp;value="authorizationCache"/&gt;&nbsp;&nbsp;</li>
</ol>
<p align="left">24. &lt;/bean&gt;&nbsp;&nbsp;</p>
<p align="left">25. &lt;!--&nbsp;会话ID生成器&nbsp;--&gt;&nbsp;&nbsp;</p>
<p align="left">26. &lt;bean&nbsp;id="sessionIdGenerator"&nbsp;&nbsp;&nbsp;</p>
<p align="left">27.&nbsp;<strong>class</strong>="org.apache.shiro.session.mgt.eis.JavaUuidSessionIdGenerator"/&gt;&nbsp;&nbsp;</p>
<p align="left">28. &lt;!--&nbsp;会话DAO&nbsp;--&gt;&nbsp;&nbsp;</p>
<p align="left">29. &lt;bean&nbsp;id="sessionDAO"&nbsp;&nbsp;&nbsp;</p>
<p align="left">30.&nbsp;<strong>class</strong>="org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO"&gt;&nbsp;&nbsp;</p>
<ol>
<li>31. &nbsp;&nbsp;&nbsp;&nbsp;&lt;property&nbsp;name="activeSessionsCacheName"&nbsp;value="shiro-activeSessionCache"/&gt;&nbsp;&nbsp;</li>
<li>32. &nbsp;&nbsp;&nbsp;&nbsp;&lt;property&nbsp;name="sessionIdGenerator"&nbsp;ref="sessionIdGenerator"/&gt;&nbsp;&nbsp;</li>
</ol>
<p align="left">33. &lt;/bean&gt;&nbsp;&nbsp;</p>
<p align="left">34. &lt;!--&nbsp;会话验证调度器&nbsp;--&gt;&nbsp;&nbsp;</p>
<p align="left">35. &lt;bean&nbsp;id="sessionValidationScheduler"&nbsp;&nbsp;&nbsp;</p>
<p align="left">36.&nbsp;<strong>class</strong>="org.apache.shiro.session.mgt.quartz.QuartzSessionValidationScheduler"&gt;&nbsp;&nbsp;</p>
<ol>
<li>37. &nbsp;&nbsp;&nbsp;&nbsp;&lt;property&nbsp;name="sessionValidationInterval"&nbsp;value="1800000"/&gt;&nbsp;&nbsp;</li>
<li>38. &nbsp;&nbsp;&nbsp;&nbsp;&lt;property&nbsp;name="sessionManager"&nbsp;ref="sessionManager"/&gt;&nbsp;&nbsp;</li>
</ol>
<p align="left">39. &lt;/bean&gt;&nbsp;&nbsp;</p>
<p align="left">40. &lt;!--&nbsp;会话管理器&nbsp;--&gt;&nbsp;&nbsp;</p>
<p align="left">41. &lt;bean&nbsp;id="sessionManager"&nbsp;<strong>class</strong>="org.apache.shiro.session.mgt.DefaultSessionManager"&gt;&nbsp;&nbsp;</p>
<ol>
<li>42. &nbsp;&nbsp;&nbsp;&nbsp;&lt;property&nbsp;name="globalSessionTimeout"&nbsp;value="1800000"/&gt;&nbsp;&nbsp;</li>
<li>43. &nbsp;&nbsp;&nbsp;&nbsp;&lt;property&nbsp;name="deleteInvalidSessions"&nbsp;value="true"/&gt;&nbsp;&nbsp;</li>
<li>44. &nbsp;&nbsp;&nbsp;&nbsp;&lt;property&nbsp;name="sessionValidationSchedulerEnabled"&nbsp;value="true"/&gt;&nbsp;&nbsp;</li>
<li>45. &nbsp;&nbsp;&nbsp;&lt;property&nbsp;name="sessionValidationScheduler"&nbsp;ref="sessionValidationScheduler"/&gt;&nbsp;&nbsp;</li>
<li>46. &nbsp;&nbsp;&nbsp;&nbsp;&lt;property&nbsp;name="sessionDAO"&nbsp;ref="sessionDAO"/&gt;&nbsp;&nbsp;</li>
</ol>
<p align="left">47. &lt;/bean&gt;&nbsp;&nbsp;</p>
<p align="left">48. &lt;!--&nbsp;安全管理器&nbsp;--&gt;&nbsp;&nbsp;</p>
<p align="left">49. &lt;bean&nbsp;id="securityManager"&nbsp;<strong>class</strong>="org.apache.shiro.mgt.DefaultSecurityManager"&gt;&nbsp;&nbsp;</p>
<ol>
<li>50. &nbsp;&nbsp;&nbsp;&nbsp;&lt;property&nbsp;name="realms"&gt;&nbsp;&nbsp;</li>
<li>51. &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;list&gt;&lt;ref&nbsp;bean="userRealm"/&gt;&lt;/list&gt;&nbsp;&nbsp;</li>
<li>52. &nbsp;&nbsp;&nbsp;&nbsp;&lt;/property&gt;&nbsp;&nbsp;</li>
<li>53. &nbsp;&nbsp;&nbsp;&nbsp;&lt;property&nbsp;name="sessionManager"&nbsp;ref="sessionManager"/&gt;&nbsp;&nbsp;</li>
<li>54. &nbsp;&nbsp;&nbsp;&nbsp;&lt;property&nbsp;name="cacheManager"&nbsp;ref="cacheManager"/&gt;&nbsp;&nbsp;</li>
</ol>
<p align="left">55. &lt;/bean&gt;&nbsp;&nbsp;</p>
<p align="left">56. &lt;!--&nbsp;相当于调用SecurityUtils.setSecurityManager(securityManager)&nbsp;--&gt;&nbsp;&nbsp;</p>
<p align="left">57. &lt;bean&nbsp;<strong>class</strong>="org.springframework.beans.factory.config.MethodInvokingFactoryBean"&gt;&nbsp;&nbsp;</p>
<p align="left">58. &lt;property&nbsp;name="staticMethod"&nbsp;&nbsp;&nbsp;</p>
<p align="left">59. value="org.apache.shiro.SecurityUtils.setSecurityManager"/&gt;&nbsp;&nbsp;</p>
<ol>
<li>60. &nbsp;&nbsp;&nbsp;&nbsp;&lt;property&nbsp;name="arguments"&nbsp;ref="securityManager"/&gt;&nbsp;&nbsp;</li>
</ol>
<p align="left">61. &lt;/bean&gt;&nbsp;&nbsp;</p>
<p align="left">62. &lt;!--&nbsp;Shiro生命周期处理器--&gt;&nbsp;&nbsp;</p>
<p align="left">63. &lt;bean&nbsp;id="lifecycleBeanPostProcessor"&nbsp;&nbsp;&nbsp;</p>
<p align="left">64.&nbsp;<strong>class</strong>="org.apache.shiro.spring.LifecycleBeanPostProcessor"/&gt;&nbsp;&nbsp;</p>
</div>
<p>可以看出，只要把之前的ini配置翻译为此处的spring xml配置方式即可，无须多解释。LifecycleBeanPostProcessor用于在实现了Initializable接口的Shiro bean初始化时调用Initializable接口回调，在实现了Destroyable接口的Shiro bean销毁时调用&nbsp;Destroyable接口回调。如UserRealm就实现了Initializable，而DefaultSecurityManager实现了Destroyable。具体可以查看它们的继承关系。</p>
<h3>Web应用：</h3>
<p>Web应用和普通JavaSE应用的某些配置是类似的，此处只提供一些不一样的配置，详细配置可以参考spring-shiro-web.xml。&nbsp;</p>
<div>
<ol>
<li>&lt;!--&nbsp;会话Cookie模板&nbsp;--&gt;&nbsp;&nbsp;</li>
<li>&lt;bean&nbsp;id="sessionIdCookie"&nbsp;<strong>class</strong>="org.apache.shiro.web.servlet.SimpleCookie"&gt;&nbsp;&nbsp;</li>
<li>&nbsp;&nbsp;&nbsp;&nbsp;&lt;constructor-arg&nbsp;value="sid"/&gt;&nbsp;&nbsp;</li>
<li>&nbsp;&nbsp;&nbsp;&nbsp;&lt;property&nbsp;name="httpOnly"&nbsp;value="true"/&gt;&nbsp;&nbsp;</li>
<li>&nbsp;&nbsp;&nbsp;&nbsp;&lt;property&nbsp;name="maxAge"&nbsp;value="180000"/&gt;&nbsp;&nbsp;</li>
<li>&lt;/bean&gt;&nbsp;&nbsp;</li>
<li>&lt;!--&nbsp;会话管理器&nbsp;--&gt;&nbsp;&nbsp;</li>
<li>&lt;bean&nbsp;id="sessionManager"&nbsp;&nbsp;&nbsp;</li>
<li><strong>class</strong>="org.apache.shiro.web.session.mgt.DefaultWebSessionManager"&gt;&nbsp;&nbsp;</li>
<li>10. &nbsp;&nbsp;&nbsp;&nbsp;&lt;property&nbsp;name="globalSessionTimeout"&nbsp;value="1800000"/&gt;&nbsp;&nbsp;</li>
<li>11. &nbsp;&nbsp;&nbsp;&nbsp;&lt;property&nbsp;name="deleteInvalidSessions"&nbsp;value="true"/&gt;&nbsp;&nbsp;</li>
<li>12. &nbsp;&nbsp;&nbsp;&nbsp;&lt;property&nbsp;name="sessionValidationSchedulerEnabled"&nbsp;value="true"/&gt;&nbsp;&nbsp;</li>
<li>13. &nbsp;&nbsp;&nbsp;&nbsp;&lt;property&nbsp;name="sessionValidationScheduler"&nbsp;ref="sessionValidationScheduler"/&gt;&nbsp;&nbsp;</li>
<li>14. &nbsp;&nbsp;&nbsp;&nbsp;&lt;property&nbsp;name="sessionDAO"&nbsp;ref="sessionDAO"/&gt;&nbsp;&nbsp;</li>
<li>15. &nbsp;&nbsp;&nbsp;&nbsp;&lt;property&nbsp;name="sessionIdCookieEnabled"&nbsp;value="true"/&gt;&nbsp;&nbsp;</li>
<li>16. &nbsp;&nbsp;&nbsp;&nbsp;&lt;property&nbsp;name="sessionIdCookie"&nbsp;ref="sessionIdCookie"/&gt;&nbsp;&nbsp;</li>
</ol>
<p align="left">17. &lt;/bean&gt;&nbsp;&nbsp;</p>
<p align="left">18. &lt;!--&nbsp;安全管理器&nbsp;--&gt;&nbsp;&nbsp;</p>
<p align="left">19. &lt;bean&nbsp;id="securityManager"&nbsp;<strong>class</strong>="org.apache.shiro.web.mgt.DefaultWebSecurityManager"&gt;&nbsp;&nbsp;</p>
<p align="left">20. &lt;property&nbsp;name="realm"&nbsp;ref="userRealm"/&gt;&nbsp;&nbsp;</p>
<ol>
<li>21. &nbsp;&nbsp;&nbsp;&nbsp;&lt;property&nbsp;name="sessionManager"&nbsp;ref="sessionManager"/&gt;&nbsp;&nbsp;</li>
<li>22. &nbsp;&nbsp;&nbsp;&nbsp;&lt;property&nbsp;name="cacheManager"&nbsp;ref="cacheManager"/&gt;&nbsp;&nbsp;</li>
</ol>
<p align="left">23. &lt;/bean&gt;&nbsp;&nbsp;&nbsp;</p>
</div>
<p><br />1、sessionIdCookie是用于生产Session ID Cookie的模板；</p>
<p>2、会话管理器使用用于web环境的DefaultWebSessionManager；</p>
<p>3、安全管理器使用用于web环境的DefaultWebSecurityManager。</p>
<p>&nbsp;</p>
<div>
<ol>
<li>&lt;!--&nbsp;基于Form表单的身份验证过滤器&nbsp;--&gt;&nbsp;&nbsp;</li>
<li>&lt;bean&nbsp;id="formAuthenticationFilter"&nbsp;&nbsp;&nbsp;</li>
<li><strong>class</strong>="org.apache.shiro.web.filter.authc.FormAuthenticationFilter"&gt;&nbsp;&nbsp;</li>
<li>&nbsp;&nbsp;&nbsp;&nbsp;&lt;property&nbsp;name="usernameParam"&nbsp;value="username"/&gt;&nbsp;&nbsp;</li>
<li>&nbsp;&nbsp;&nbsp;&nbsp;&lt;property&nbsp;name="passwordParam"&nbsp;value="password"/&gt;&nbsp;&nbsp;</li>
<li>&nbsp;&nbsp;&nbsp;&nbsp;&lt;property&nbsp;name="loginUrl"&nbsp;value="/login.jsp"/&gt;&nbsp;&nbsp;</li>
<li>&lt;/bean&gt;&nbsp;&nbsp;</li>
<li>&lt;!--&nbsp;Shiro的Web过滤器&nbsp;--&gt;&nbsp;&nbsp;</li>
<li>&lt;bean&nbsp;id="shiroFilter"&nbsp;<strong>class</strong>="org.apache.shiro.spring.web.ShiroFilterFactoryBean"&gt;&nbsp;&nbsp;</li>
<li>10. &nbsp;&nbsp;&nbsp;&nbsp;&lt;property&nbsp;name="securityManager"&nbsp;ref="securityManager"/&gt;&nbsp;&nbsp;</li>
<li>11. &nbsp;&nbsp;&nbsp;&nbsp;&lt;property&nbsp;name="loginUrl"&nbsp;value="/login.jsp"/&gt;&nbsp;&nbsp;</li>
<li>12. &nbsp;&nbsp;&nbsp;&nbsp;&lt;property&nbsp;name="unauthorizedUrl"&nbsp;value="/unauthorized.jsp"/&gt;&nbsp;&nbsp;</li>
<li>13. &nbsp;&nbsp;&nbsp;&nbsp;&lt;property&nbsp;name="filters"&gt;&nbsp;&nbsp;</li>
<li>14. &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;util:map&gt;&nbsp;&nbsp;</li>
<li>15. &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;entry&nbsp;key="authc"&nbsp;value-ref="formAuthenticationFilter"/&gt;&nbsp;&nbsp;</li>
<li>16. &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;/util:map&gt;&nbsp;&nbsp;</li>
<li>17. &nbsp;&nbsp;&nbsp;&nbsp;&lt;/property&gt;&nbsp;&nbsp;</li>
<li>18. &nbsp;&nbsp;&nbsp;&nbsp;&lt;property&nbsp;name="filterChainDefinitions"&gt;&nbsp;&nbsp;</li>
<li>19. &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;value&gt;&nbsp;&nbsp;</li>
<li>20. &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;/index.jsp&nbsp;=&nbsp;anon&nbsp;&nbsp;</li>
<li>21. &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;/unauthorized.jsp&nbsp;=&nbsp;anon&nbsp;&nbsp;</li>
<li>22. &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;/login.jsp&nbsp;=&nbsp;authc&nbsp;&nbsp;</li>
<li>23. &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;/logout&nbsp;=&nbsp;logout&nbsp;&nbsp;</li>
<li>24. &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;/**&nbsp;=&nbsp;user&nbsp;&nbsp;</li>
<li>25. &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;/value&gt;&nbsp;&nbsp;</li>
<li>26. &nbsp;&nbsp;&nbsp;&nbsp;&lt;/property&gt;&nbsp;&nbsp;</li>
</ol>
<p align="left">27. &lt;/bean&gt;&nbsp;&nbsp;&nbsp;</p>
</div>
<p>&nbsp;</p>
<p align="left">1、formAuthenticationFilter为基于Form表单的身份验证过滤器；此处可以再添加自己的Filter bean定义；</p>
<p align="left">2、shiroFilter：此处使用ShiroFilterFactoryBean来创建ShiroFilter过滤器；filters属性用于定义自己的过滤器，即ini配置中的[filters]部分；filterChainDefinitions用于声明url和filter的关系，即ini配置中的[urls]部分。</p>
<h2>Shiro权限注解</h2>
<h3>注意:</h3>
<p>在spring中需要开启权限注解与aop:</p>
<p align="left">&lt;!-- AOP式方法级权限检查&nbsp; --&gt;<br />&lt;bean class="org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator"<br />&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; depends-on="lifecycleBeanPostProcessor"/&gt;<br /><br />&lt;!-- 启用shrio授权注解拦截方式 --&gt;<br />&lt;bean class="org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor"&gt;<br />&nbsp;&nbsp;&nbsp; &lt;property name="securityManager" ref="securityManager"/&gt;<br />&lt;/bean&gt;</p>
<p>&nbsp;</p>
<p>&nbsp;</p>
<p align="left">Shiro提供了相应的注解用于权限控制，如果使用这些注解就需要使用AOP的功能来进行判断，如Spring AOP；Shiro提供了Spring AOP集成用于权限注解的解析和验证。</p>
<p align="left">为了测试，此处使用了Spring MVC来测试Shiro注解，当然Shiro注解不仅仅可以在web环境使用，在独立的JavaSE中也是可以用的，此处只是以web为例了。</p>
<p align="left">&nbsp;</p>
<p align="left">在spring-mvc.xml配置文件添加Shiro Spring AOP权限注解的支持：</p>
<div>
<ol>
<li>&lt;aop:config&nbsp;proxy-target-<strong>class</strong>="true"&gt;&lt;/aop:config&gt;&nbsp;&nbsp;</li>
<li>&lt;bean&nbsp;<strong>class</strong>="&nbsp;&nbsp;</li>
<li>org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor"&gt;&nbsp;&nbsp;</li>
<li>&nbsp;&nbsp;&nbsp;&nbsp;&lt;property&nbsp;name="securityManager"&nbsp;ref="securityManager"/&gt;&nbsp;&nbsp;</li>
<li>&lt;/bean&gt;&nbsp;&nbsp;&nbsp;</li>
</ol>
</div>
<p align="left">如上配置用于开启Shiro Spring AOP权限注解的支持；&lt;aop:config proxy-target-class="true"&gt;表示代理类。</p>
<p align="left">&nbsp;</p>
<p align="left">接着就可以在相应的控制器（AnnotationController）中使用如下方式进行注解：&nbsp;</p>
<div>
<ol>
<li>@RequiresRoles("admin")&nbsp;&nbsp;</li>
<li>@RequestMapping("/hello2")&nbsp;&nbsp;</li>
<li><strong>public</strong>&nbsp;String&nbsp;hello2()&nbsp;{&nbsp;&nbsp;</li>
<li>&nbsp;&nbsp;&nbsp;&nbsp;<strong>return</strong>&nbsp;"success";&nbsp;&nbsp;</li>
<li>}&nbsp;&nbsp;</li>
</ol>
</div>
<p>访问hello2方法的前提是当前用户有admin角色。</p>
<p>&nbsp;</p>
<p>当验证失败，其会抛出UnauthorizedException异常，此时可以使用Spring的ExceptionHandler（DefaultExceptionHandler）来进行拦截处理：</p>
<div>
<ol>
<li>@ExceptionHandler({UnauthorizedException.<strong>class</strong>})&nbsp;&nbsp;</li>
<li>@ResponseStatus(HttpStatus.UNAUTHORIZED)&nbsp;&nbsp;</li>
<li><strong>public</strong>&nbsp;ModelAndView&nbsp;processUnauthenticatedException(NativeWebRequest&nbsp;request,&nbsp;UnauthorizedException&nbsp;e)&nbsp;{&nbsp;&nbsp;</li>
<li>&nbsp;&nbsp;&nbsp;&nbsp;ModelAndView&nbsp;mv&nbsp;=&nbsp;<strong>new</strong>&nbsp;ModelAndView();&nbsp;&nbsp;</li>
<li>&nbsp;&nbsp;&nbsp;&nbsp;mv.addObject("exception",&nbsp;e);&nbsp;&nbsp;</li>
<li>&nbsp;&nbsp;&nbsp;&nbsp;mv.setViewName("unauthorized");&nbsp;&nbsp;</li>
<li>&nbsp;&nbsp;&nbsp;&nbsp;<strong>return</strong>&nbsp;mv;&nbsp;&nbsp;</li>
<li>}&nbsp;&nbsp;&nbsp;</li>
</ol>
</div>
<p align="left"><strong>权限注解</strong>&nbsp; &nbsp; &nbsp;&nbsp;</p>
<p align="left"><strong>Java</strong><strong>代码</strong><strong>&nbsp;&nbsp;</strong></p>
<div>
<ol>
<li>@RequiresAuthentication&nbsp;&nbsp;</li>
</ol>
</div>
<p align="left">表示当前Subject已经通过login进行了身份验证；即Subject. isAuthenticated()返回true。&nbsp;</p>
<p align="left">&nbsp;</p>
<p align="left"><strong>Java</strong><strong>代码</strong><strong>&nbsp;&nbsp;</strong></p>
<div>
<ol>
<li>@RequiresUser&nbsp;&nbsp;</li>
</ol>
</div>
<p align="left">表示当前Subject已经身份验证或者通过记住我登录的。</p>
<p align="left">&nbsp;</p>
<p align="left"><strong>Java</strong><strong>代码</strong><strong>&nbsp;&nbsp;</strong></p>
<div>
<ol>
<li>@RequiresGuest&nbsp;&nbsp;</li>
</ol>
</div>
<p align="left">表示当前Subject没有身份验证或通过记住我登录过，即是游客身份。&nbsp;&nbsp;</p>
<p align="left">&nbsp;</p>
<p align="left"><strong>Java</strong><strong>代码</strong><strong>&nbsp;&nbsp;</strong></p>
<div>
<ol>
<li>@RequiresRoles(value={&ldquo;admin&rdquo;,&nbsp;&ldquo;user&rdquo;},&nbsp;logical=&nbsp;Logical.AND)&nbsp;&nbsp;</li>
</ol>
</div>
<p align="left">表示当前Subject需要角色admin和user。</p>
<p align="left">&nbsp;</p>
<p align="left"><strong>Java</strong><strong>代码</strong><strong>&nbsp;&nbsp;</strong></p>
<div>
<ol>
<li><strong>@RequiresPermissions</strong>&nbsp;(value={&ldquo;user:a&rdquo;,&nbsp;&ldquo;user:b&rdquo;},&nbsp;logical=&nbsp;Logical.OR)&nbsp;&nbsp;</li>
</ol>
</div>
<p align="left">表示当前Subject需要权限user:a或user:b。 &nbsp;</p>
<h2 align="center">Shiro 完整项目配置</h2>
<p><strong>第一步：配置</strong><strong>web.xml</strong></p>
<div>
<p align="left">&lt;!--&nbsp;配置Shiro过滤器,先让Shiro过滤系统接收到的请求&nbsp;--&gt;&nbsp;&nbsp;</p>
<p align="left">&lt;!--&nbsp;这里filter-name必须对应applicationContext.xml中定义的&lt;bean&nbsp;id="shiroFilter"/&gt;&nbsp;--&gt;&nbsp;&nbsp;</p>
<p align="left">&lt;!--&nbsp;使用[/*]匹配所有请求,保证所有的可控请求都经过Shiro的过滤&nbsp;--&gt;&nbsp;&nbsp;</p>
<p align="left">&lt;!--&nbsp;通常会将此filter-mapping放置到最前面(即其他filter-mapping前面),以保证它是过滤器链中第一个起作用的&nbsp;--&gt;&nbsp;&nbsp;</p>
<p align="left">&lt;filter&gt;&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&lt;filter-name&gt;shiroFilter&lt;/filter-name&gt;&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&lt;filter-class&gt;org.springframework.web.filter.DelegatingFilterProxy&lt;/filter-class&gt;&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&lt;init-param&gt;&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&lt;!--&nbsp;该值缺省为false,表示生命周期由SpringApplicationContext管理,设置为true则表示由ServletContainer管理&nbsp;--&gt;&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&lt;param-name&gt;targetFilterLifecycle&lt;/param-name&gt;&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&lt;param-value&gt;true&lt;/param-value&gt;&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&lt;/init-param&gt;&nbsp;&nbsp;</p>
<p align="left">&lt;/filter&gt;&nbsp;&nbsp;</p>
<p align="left">&lt;filter-mapping&gt;&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;filter-name&gt;shiroFilter&lt;/filter-name&gt;&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;url-pattern&gt;/*&lt;/url-pattern&gt;&nbsp;&nbsp;</p>
<p>&lt;/filter-mapping&gt;</p>
</div>
<p><strong>第二步：配置</strong><strong>applicationContext.xml</strong></p>
<p align="left">&lt;!--&nbsp;继承自AuthorizingRealm的自定义Realm,即指定Shiro验证用户登录的类为自定义的ShiroDbRealm.java&nbsp;--&gt;&nbsp;&nbsp;</p>
<p align="left">&lt;bean&nbsp;id="myRealm"&nbsp;class="com.jadyer.realm.MyRealm"/&gt;&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;</p>
<p align="left">&lt;!--&nbsp;Shiro默认会使用Servlet容器的Session,可通过sessionMode属性来指定使用Shiro原生Session&nbsp;--&gt;&nbsp;&nbsp;</p>
<p align="left">&lt;!--&nbsp;即&lt;property&nbsp;name="sessionMode"&nbsp;value="native"/&gt;,详细说明见官方文档&nbsp;--&gt;&nbsp;&nbsp;</p>
<p align="left">&lt;!--&nbsp;这里主要是设置自定义的单Realm应用,若有多个Realm,可使用'realms'属性代替&nbsp;--&gt;&nbsp;&nbsp;</p>
<p align="left">&lt;bean&nbsp;id="securityManager"&nbsp;class="org.apache.shiro.web.mgt.DefaultWebSecurityManager"&gt;&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&lt;property&nbsp;name="realm"&nbsp;ref="myRealm"/&gt;&nbsp;&nbsp;</p>
<p align="left">&lt;/bean&gt;&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;</p>
<p align="left">&lt;!--&nbsp;Shiro主过滤器本身功能十分强大,其强大之处就在于它支持任何基于URL路径表达式的、自定义的过滤器的执行&nbsp;--&gt;&nbsp;&nbsp;</p>
<p align="left">&lt;!--&nbsp;Web应用中,Shiro可控制的Web请求必须经过Shiro主过滤器的拦截,Shiro对基于Spring的Web应用提供了完美的支持&nbsp;--&gt;&nbsp;&nbsp;</p>
<p align="left">&lt;bean&nbsp;id="shiroFilter"&nbsp;class="org.apache.shiro.spring.web.ShiroFilterFactoryBean"&gt;&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&lt;!--&nbsp;Shiro的核心安全接口,这个属性是必须的&nbsp;--&gt;&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&lt;property&nbsp;name="securityManager"&nbsp;ref="securityManager"/&gt;&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&lt;!--&nbsp;要求登录时的链接(可根据项目的URL进行替换),非必须的属性,默认会自动寻找Web工程根目录下的"/login.jsp"页面&nbsp;--&gt;&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&lt;property&nbsp;name="loginUrl"&nbsp;value="/"/&gt;&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&lt;!--&nbsp;登录成功后要跳转的连接(本例中此属性用不到,因为登录成功后的处理逻辑在LoginController里硬编码为main.jsp了)&nbsp;--&gt;&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&lt;!--&nbsp;&lt;property&nbsp;name="successUrl"&nbsp;value="/system/main"/&gt;&nbsp;--&gt;&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&lt;!--&nbsp;用户访问未对其授权的资源时,所显示的连接&nbsp;--&gt;&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&lt;!--&nbsp;若想更明显的测试此属性可以修改它的值,如unauthor.jsp,然后用[玄玉]登录后访问/admin/listUser.jsp就看见浏览器会显示unauthor.jsp&nbsp;--&gt;&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&lt;property&nbsp;name="unauthorizedUrl"&nbsp;value="/"/&gt;&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&lt;!--&nbsp;Shiro连接约束配置,即过滤链的定义&nbsp;--&gt;&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&lt;!--&nbsp;此处可配合我的这篇文章来理解各个过滤连的作用http://blog.csdn.net/jadyer/article/details/12172839&nbsp;--&gt;&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&lt;!--&nbsp;下面value值的第一个'/'代表的路径是相对于HttpServletRequest.getContextPath()的值来的&nbsp;--&gt;&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&lt;!--&nbsp;anon：它对应的过滤器里面是空的,什么都没做,这里.do和.jsp后面的*表示参数,比方说login.jsp?main这种&nbsp;--&gt;&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&lt;!--&nbsp;authc：该过滤器下的页面必须验证后才能访问,它是Shiro内置的一个拦截器org.apache.shiro.web.filter.authc.FormAuthenticationFilter&nbsp;--&gt;&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&lt;property&nbsp;name="filterChainDefinitions"&gt;&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;value&gt;&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;/mydemo/login=anon&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;/mydemo/getVerifyCodeImage=anon&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;/main**=authc&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;/user/info**=authc&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;/admin/listUser**=authc,perms[admin:manage]&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;/value&gt;&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&lt;/property&gt;</p>
<p align="left">&lt;/bean&gt;&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;</p>
<p align="left">&lt;!--&nbsp;保证实现了Shiro内部lifecycle函数的bean执行&nbsp;--&gt;&nbsp;&nbsp;</p>
<p align="left">&lt;bean&nbsp;id="lifecycleBeanPostProcessor"&nbsp;class="org.apache.shiro.spring.LifecycleBeanPostProcessor"/&gt;&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;</p>
<p align="left">&lt;!--&nbsp;开启Shiro的注解(如@RequiresRoles,@RequiresPermissions),需借助SpringAOP扫描使用Shiro注解的类,并在必要时进行安全逻辑验证&nbsp;--&gt;&nbsp;&nbsp;</p>
<p align="left">&lt;!--&nbsp;配置以下两个bean即可实现此功能&nbsp;--&gt;&nbsp;&nbsp;</p>
<p align="left">&lt;!--&nbsp;Enable&nbsp;Shiro&nbsp;Annotations&nbsp;for&nbsp;Spring-configured&nbsp;beans.&nbsp;Only&nbsp;run&nbsp;after&nbsp;the&nbsp;lifecycleBeanProcessor&nbsp;has&nbsp;run&nbsp;--&gt;&nbsp;&nbsp;</p>
<p align="left">&lt;!--&nbsp;由于本例中并未使用Shiro注解,故注释掉这两个bean(个人觉得将权限通过注解的方式硬编码在程序中,查看起来不是很方便,没必要使用)&nbsp;--&gt;&nbsp;&nbsp;</p>
<p align="left">&lt;!--&nbsp;&nbsp;&nbsp;</p>
<p align="left">&lt;bean&nbsp;class="org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator"&nbsp;depends-on="lifecycleBeanPostProcessor"/&gt;&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&lt;bean&nbsp;class="org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor"&gt;&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&lt;property&nbsp;name="securityManager"&nbsp;ref="securityManager"/&gt;&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&lt;/bean&gt;&nbsp;&nbsp;</p>
<p>--&gt;</p>
<p>&nbsp;</p>
<p><strong>第三步：自定义的</strong><strong>Realm</strong><strong>类</strong></p>
<p align="left">public&nbsp;class&nbsp;MyRealm&nbsp;extends&nbsp;AuthorizingRealm&nbsp;{&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;/**&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;*&nbsp;为当前登录的Subject授予角色和权限&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;*&nbsp;@see&nbsp;&nbsp;经测试:本例中该方法的调用时机为需授权资源被访问时&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;*&nbsp;@see&nbsp;&nbsp;经测试:并且每次访问需授权资源时都会执行该方法中的逻辑,这表明本例中默认并未启用AuthorizationCache&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;*&nbsp;@see&nbsp;&nbsp;个人感觉若使用了Spring3.1开始提供的ConcurrentMapCache支持,则可灵活决定是否启用AuthorizationCache&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;*&nbsp;@see&nbsp;&nbsp;比如说这里从数据库获取权限信息时,先去访问Spring3.1提供的缓存,而不使用Shior提供的AuthorizationCache&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;*/&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;@Override&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;protected&nbsp;AuthorizationInfo&nbsp;doGetAuthorizationInfo(PrincipalCollection&nbsp;principals){&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;//获取当前登录的用户名,等价于(String)principals.fromRealm(this.getName()).iterator().next()&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;String&nbsp;currentUsername&nbsp;=&nbsp;(String)super.getAvailablePrincipal(principals);&nbsp;&nbsp;</p>
<p align="left">//&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;List&lt;String&gt;&nbsp;roleList&nbsp;=&nbsp;new&nbsp;ArrayList&lt;String&gt;();&nbsp;&nbsp;</p>
<p align="left">//&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;List&lt;String&gt;&nbsp;permissionList&nbsp;=&nbsp;new&nbsp;ArrayList&lt;String&gt;();&nbsp;&nbsp;</p>
<p align="left">//&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;//从数据库中获取当前登录用户的详细信息&nbsp;&nbsp;</p>
<p align="left">//&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;User&nbsp;user&nbsp;=&nbsp;userService.getByUsername(currentUsername);&nbsp;&nbsp;</p>
<p align="left">//&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;if(null&nbsp;!=&nbsp;user){&nbsp;&nbsp;</p>
<p align="left">//&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;//实体类User中包含有用户角色的实体类信息&nbsp;&nbsp;</p>
<p align="left">//&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;if(null!=user.getRoles()&nbsp;&amp;&amp;&nbsp;user.getRoles().size()&gt;0){&nbsp;&nbsp;</p>
<p align="left">//&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;//获取当前登录用户的角色&nbsp;&nbsp;</p>
<p align="left">//&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;for(Role&nbsp;role&nbsp;:&nbsp;user.getRoles()){&nbsp;&nbsp;</p>
<p align="left">//&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;roleList.add(role.getName());&nbsp;&nbsp;</p>
<p align="left">//&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;//实体类Role中包含有角色权限的实体类信息&nbsp;&nbsp;</p>
<p align="left">//&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;if(null!=role.getPermissions()&nbsp;&amp;&amp;&nbsp;role.getPermissions().size()&gt;0){&nbsp;&nbsp;</p>
<p align="left">//&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;//获取权限&nbsp;&nbsp;</p>
<p align="left">//&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;for(Permission&nbsp;pmss&nbsp;:&nbsp;role.getPermissions()){&nbsp;&nbsp;</p>
<p align="left">//&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;if(!StringUtils.isEmpty(pmss.getPermission())){&nbsp;&nbsp;</p>
<p align="left">//&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;permissionList.add(pmss.getPermission());&nbsp;&nbsp;</p>
<p align="left">//&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}&nbsp;&nbsp;</p>
<p align="left">//&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}&nbsp;&nbsp;</p>
<p align="left">//&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}&nbsp;&nbsp;</p>
<p align="left">//&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}&nbsp;&nbsp;</p>
<p align="left">//&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}&nbsp;&nbsp;</p>
<p align="left">//&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}else{&nbsp;&nbsp;</p>
<p align="left">//&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;throw&nbsp;new&nbsp;AuthorizationException();&nbsp;&nbsp;</p>
<p align="left">//&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}&nbsp;&nbsp;</p>
<p align="left">//&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;//为当前用户设置角色和权限&nbsp;&nbsp;</p>
<p align="left">//&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;SimpleAuthorizationInfo&nbsp;simpleAuthorInfo&nbsp;=&nbsp;new&nbsp;SimpleAuthorizationInfo();&nbsp;&nbsp;</p>
<p align="left">//&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;simpleAuthorInfo.addRoles(roleList);&nbsp;&nbsp;</p>
<p align="left">//&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;simpleAuthorInfo.addStringPermissions(permissionList);&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;SimpleAuthorizationInfo&nbsp;simpleAuthorInfo&nbsp;=&nbsp;new&nbsp;SimpleAuthorizationInfo();&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;//实际中可能会像上面注释的那样从数据库取得&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;if(null!=currentUsername&nbsp;&amp;&amp;&nbsp;"mike".equals(currentUsername)){&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;//添加一个角色,不是配置意义上的添加,而是证明该用户拥有admin角色&nbsp;&nbsp;&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;simpleAuthorInfo.addRole("admin");&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;//添加权限&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;simpleAuthorInfo.addStringPermission("admin:manage");&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;System.out.println("已为用户[mike]赋予了[admin]角色和[admin:manage]权限");&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;return&nbsp;simpleAuthorInfo;&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;//若该方法什么都不做直接返回null的话,就会导致任何用户访问/admin/listUser.jsp时都会自动跳转到unauthorizedUrl指定的地址&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;//详见applicationContext.xml中的&lt;bean&nbsp;id="shiroFilter"&gt;的配置&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;return&nbsp;null;&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;}&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;/**&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;*&nbsp;验证当前登录的Subject&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;*&nbsp;@see&nbsp;&nbsp;经测试:本例中该方法的调用时机为LoginController.login()方法中执行Subject.login()时&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;*/&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;@Override&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;protected&nbsp;AuthenticationInfo&nbsp;doGetAuthenticationInfo(AuthenticationToken&nbsp;authcToken)&nbsp;throws&nbsp;AuthenticationException&nbsp;{&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;//获取基于用户名和密码的令牌&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;//实际上这个authcToken是从LoginController里面currentUser.login(token)传过来的&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;//两个token的引用都是一样的</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;UsernamePasswordToken&nbsp;token&nbsp;=&nbsp;(UsernamePasswordToken)authcToken;&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;System.out.println("验证当前Subject时获取到token为"&nbsp;+&nbsp;ReflectionToStringBuilder.toString(token,&nbsp;ToStringStyle.MULTI_LINE_STYLE));&nbsp;&nbsp;</p>
<p align="left">//&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;User&nbsp;user&nbsp;=&nbsp;userService.getByUsername(token.getUsername());&nbsp;&nbsp;</p>
<p align="left">//&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;if(null&nbsp;!=&nbsp;user){&nbsp;&nbsp;</p>
<p align="left">//&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;AuthenticationInfo&nbsp;authcInfo&nbsp;=&nbsp;new&nbsp;SimpleAuthenticationInfo(user.getUsername(),&nbsp;user.getPassword(),&nbsp;user.getNickname());&nbsp;&nbsp;</p>
<p align="left">//&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;this.setSession("currentUser",&nbsp;user);&nbsp;&nbsp;</p>
<p align="left">//&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;return&nbsp;authcInfo;&nbsp;&nbsp;</p>
<p align="left">//&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}else{&nbsp;&nbsp;</p>
<p align="left">//&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;return&nbsp;null;&nbsp;&nbsp;</p>
<p align="left">//&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;//此处无需比对,比对的逻辑Shiro会做,我们只需返回一个和令牌相关的正确的验证信息&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;//说白了就是第一个参数填登录用户名,第二个参数填合法的登录密码(可以是从数据库中取到的,本例中为了演示就硬编码了)&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;//这样一来,在随后的登录页面上就只有这里指定的用户和密码才能通过验证&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;if("mike".equals(token.getUsername())){&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;AuthenticationInfo&nbsp;authcInfo&nbsp;=&nbsp;new&nbsp;SimpleAuthenticationInfo("mike",&nbsp;"mike",&nbsp;this.getName());&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;this.setSession("currentUser",&nbsp;"mike");&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;return&nbsp;authcInfo;&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;//没有返回登录用户名对应的SimpleAuthenticationInfo对象时,就会在LoginController中抛出UnknownAccountException异常&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;return&nbsp;null;&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;}&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;/**&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;*&nbsp;将一些数据放到ShiroSession中,以便于其它地方使用&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;*&nbsp;@see&nbsp;&nbsp;比如Controller,使用时直接用HttpSession.getAttribute(key)就可以取到&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;*/&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;private&nbsp;void&nbsp;setSession(Object&nbsp;key,&nbsp;Object&nbsp;value){&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Subject&nbsp;currentUser&nbsp;=&nbsp;SecurityUtils.getSubject();&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;if(null&nbsp;!=&nbsp;currentUser){&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Session&nbsp;session&nbsp;=&nbsp;currentUser.getSession();&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;System.out.println("Session默认超时时间为["&nbsp;+&nbsp;session.getTimeout()&nbsp;+&nbsp;"]毫秒");&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;if(null&nbsp;!=&nbsp;session){&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;session.setAttribute(key,&nbsp;value);&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}&nbsp;&nbsp;</p>
<p align="left">&nbsp;&nbsp;&nbsp;&nbsp;}&nbsp;&nbsp;</p>
<p>}</p>