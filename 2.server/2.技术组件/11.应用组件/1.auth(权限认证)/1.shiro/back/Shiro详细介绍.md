## Shiro简介

SpringMVC整合Shiro，Shiro是一个强大易用的Java安全框架,提供了认证、授权、加密和会话管理等功能。

![](https://images2015.cnblogs.com/blog/1107610/201702/1107610-20170216182704129-416644991.png)

**Authentication****：**身份认证/登录，验证用户是不是拥有相应的身份；

**Authorization****：**授权，即权限验证，验证某个已认证的用户是否拥有某个权限；即判断用户是否能做事情，常见的如：验证某个用户是否拥有某个角色。或者细粒度的验证某个用户对某个资源是否具有某个权限；

**Session Manager****：**会话管理，即用户登录后就是一次会话，在没有退出之前，它的所有信息都在会话中；会话可以是普通JavaSE环境的，也可以是如Web环境的；

**Cryptography****：**加密，保护数据的安全性，如密码加密存储到数据库，而不是明文存储；

**Web Support****：**Web支持，可以非常容易的集成到Web环境；

Caching：缓存，比如用户登录后，其用户信息、拥有的角色/权限不必每次去查，这样可以提高效率；

**Concurrency****：**shiro支持多线程应用的并发验证，即如在一个线程中开启另一个线程，能把权限自动传播过去；

**Testing****：**提供测试支持；

**Run As****：**允许一个用户假装为另一个用户（如果他们允许）的身份进行访问；

**Remember Me****：**记住我，这个是非常常见的功能，即一次登录后，下次再来的话不用登录了。

**记住一点，****Shiro****不会去维护用户、维护权限；这些需要我们自己去设计****/****提供；然后通过相应的接口注入给****Shiro****即可。**

首先，我们从外部来看Shiro吧，即从应用程序角度的来观察如何使用Shiro完成工作。如下图：

![](https://images2015.cnblogs.com/blog/1107610/201702/1107610-20170216182838019-1291577076.png)

可以看到：应用代码直接交互的对象是Subject，也就是说Shiro的对外API核心就是Subject；其每个API的含义：

**Subject****：**主体，代表了当前“用户”，这个用户不一定是一个具体的人，与当前应用交互的任何东西都是Subject，如网络爬虫，机器人等；即一个抽象概念；所有Subject都绑定到SecurityManager，与Subject的所有交互都会委托给SecurityManager；可以把Subject认为是一个门面；SecurityManager才是实际的执行者；

**SecurityManager****：**安全管理器；即所有与安全有关的操作都会与SecurityManager交互；且它管理着所有Subject；可以看出它是Shiro的核心，它负责与后边介绍的其他组件进行交互，如果学习过SpringMVC，你可以把它看成DispatcherServlet前端控制器；

**Realm****：**域，Shiro从从Realm获取安全数据（如用户、角色、权限），就是说SecurityManager要验证用户身份，那么它需要从Realm获取相应的用户进行比较以确定用户身份是否合法；也需要从Realm得到用户相应的角色/权限进行验证用户是否能进行操作；可以把Realm看成DataSource，即安全数据源。

接下来我们来从Shiro内部来看下Shiro的架构，如下图所示：

![](https://images2015.cnblogs.com/blog/1107610/201702/1107610-20170216182844941-583142950.png)

**Subject****：**主体，可以看到主体可以是任何可以与应用交互的“用户”；

**SecurityManager****：**相当于SpringMVC中的DispatcherServlet或者Struts2中的FilterDispatcher；是Shiro的心脏；所有具体的交互都通过SecurityManager进行控制；它管理着所有Subject、且负责进行认证和授权、及会话、缓存的管理。

**Authenticator****：**认证器，负责主体认证的，这是一个扩展点，如果用户觉得Shiro默认的不好，可以自定义实现；其需要认证策略（Authentication Strategy），即什么情况下算用户认证通过了；

**Authrizer****：**授权器，或者访问控制器，用来决定主体是否有权限进行相应的操作；即控制着用户能访问应用中的哪些功能；

**Realm****：**可以有1个或多个Realm，可以认为是安全实体数据源，即用于获取安全实体的；可以是JDBC实现，也可以是LDAP实现，或者内存实现等等；由用户提供；注意：Shiro不知道你的用户/权限存储在哪及以何种格式存储；所以我们一般在应用中都需要实现自己的Realm；

**SessionManager****：**如果写过Servlet就应该知道Session的概念，Session呢需要有人去管理它的生命周期，这个组件就是SessionManager；而Shiro并不仅仅可以用在Web环境，也可以用在如普通的JavaSE环境、EJB等环境；所有呢，Shiro就抽象了一个自己的Session来管理主体与应用之间交互的数据；这样的话，比如我们在Web环境用，刚开始是一台Web服务器；接着又上了台EJB服务器；这时想把两台服务器的会话数据放到一个地方，这个时候就可以实现自己的分布式会话（如把数据放到Memcached服务器）；

**SessionDAO****：**DAO大家都用过，数据访问对象，用于会话的CRUD，比如我们想把Session保存到数据库，那么可以实现自己的SessionDAO，通过如JDBC写到数据库；比如想把Session放到Memcached中，可以实现自己的Memcached SessionDAO；另外SessionDAO中可以使用Cache进行缓存，以提高性能；

**CacheManager****：**缓存控制器，来管理如用户、角色、权限等的缓存的；因为这些数据基本上很少去改变，放到缓存中后可以提高访问的性能

**Cryptography****：**密码模块，Shiro提高了一些常见的加密组件用于如密码加密/解密的。

## 自定义Realm

public class ShiroRealm extends AuthorizingRealm{

}

**1****、****ShiroRealm****父类****AuthorizingRealm****将获取****Subject****相关信息分成两步**：获取身份验证信息（doGetAuthenticationInfo）及授权信息（doGetAuthorizationInfo）；

**2****、****doGetAuthenticationInfo****获取身份验证相关信息**：首先根据传入的用户名获取User信息；然后如果user为空，那么抛出没找到帐号异常UnknownAccountException；如果user找到但锁定了抛出锁定异常LockedAccountException；最后生成AuthenticationInfo信息，交给间接父类AuthenticatingRealm使用CredentialsMatcher进行判断密码是否匹配，如果不匹配将抛出密码错误异常IncorrectCredentialsException；另外如果密码重试此处太多将抛出超出重试次数异常ExcessiveAttemptsException；在组装SimpleAuthenticationInfo信息时，需要传入：身份信息（用户名）、凭据（密文密码）、盐（username+salt），CredentialsMatcher使用盐加密传入的明文密码和此处的密文密码进行匹配。

**3****、****doGetAuthorizationInfo****获取授权信息**：PrincipalCollection是一个身份集合，因为我们现在就一个Realm，所以直接调用getPrimaryPrincipal得到之前传入的用户名即可；然后根据用户名调用UserService接口获取角色及权限信息。

### AuthenticationToken

![](https://images2015.cnblogs.com/blog/1107610/201702/1107610-20170216182856347-412437771.png)

AuthenticationToken用于收集用户提交的身份（如用户名）及凭据（如密码）：
1. **public** **interface** AuthenticationToken **extends** Serializable {
1. Object getPrincipal(); //身份
1. Object getCredentials(); //凭据
1. }

扩展接口RememberMeAuthenticationToken：提供了“boolean isRememberMe()”现“记住我”的功能；

扩展接口是HostAuthenticationToken：提供了“String getHost()”方法用于获取用户“主机”的功能。

Shiro提供了一个直接拿来用的UsernamePasswordToken，用于实现用户名/密码Token组，另外其实现了RememberMeAuthenticationToken和HostAuthenticationToken，可以实现记住我及主机验证的支持。

### AuthenticationInfo

![](https://images2015.cnblogs.com/blog/1107610/201702/1107610-20170216182906425-588426570.png)

AuthenticationInfo有两个作用：

1、如果Realm是AuthenticatingRealm子类，则提供给AuthenticatingRealm内部使用的CredentialsMatcher进行凭据验证；（如果没有继承它需要在自己的Realm中自己实现验证）；

2、提供给SecurityManager来创建Subject（提供身份信息）；

MergableAuthenticationInfo用于提供在多Realm时合并AuthenticationInfo的功能，主要合并Principal、如果是其他的如credentialsSalt，会用后边的信息覆盖前边的。

比如HashedCredentialsMatcher，在验证时会判断AuthenticationInfo是否是SaltedAuthenticationInfo子类，来获取盐信息。

Account相当于我们之前的User，SimpleAccount是其一个实现；在IniRealm、PropertiesRealm这种静态创建帐号信息的场景中使用，这些Realm直接继承了SimpleAccountRealm，而SimpleAccountRealm提供了相关的API来动态维护SimpleAccount；即可以通过这些API来动态增删改查SimpleAccount；动态增删改查角色/权限信息。及如果您的帐号不是特别多，可以使用这种方式，具体请参考SimpleAccountRealm Javadoc。

其他情况一般返回SimpleAuthenticationInfo即可。

### PrincipalCollection

![](https://images2015.cnblogs.com/blog/1107610/201702/1107610-20170216182916957-1699024370.png)

因为我们可以在Shiro中同时配置多个Realm，所以呢身份信息可能就有多个；因此其提供了PrincipalCollection用于聚合这些身份信息：
1. **public** **interface** PrincipalCollection **extends** Iterable, Serializable {
1. Object getPrimaryPrincipal(); //得到主要的身份
1. <T> T oneByType(Class<T> type); //根据身份类型获取第一个
1. <T> Collection<T> byType(Class<T> type); //根据身份类型获取一组
1. List asList(); //转换为List
1. Set asSet(); //转换为Set
1. Collection fromRealm(String realmName); //根据Realm名字获取
1. Set<String> getRealmNames(); //获取所有身份验证通过的Realm名字
1. **boolean** isEmpty(); //判断是否为空

10. }

因为PrincipalCollection聚合了多个，此处最需要注意的是getPrimaryPrincipal，如果只有一个Principal那么直接返回即可，如果有多个Principal，则返回第一个（因为内部使用Map存储，所以可以认为是返回任意一个）；oneByType / byType根据凭据的类型返回相应的Principal；fromRealm根据Realm名字（每个Principal都与一个Realm关联）获取相应的Principal。

目前Shiro只提供了一个实现SimplePrincipalCollection，还记得之前的AuthenticationStrategy实现嘛，用于在多Realm时判断是否满足条件的，在大多数实现中（继承了AbstractAuthenticationStrategy）afterAttempt方法会进行AuthenticationInfo（实现了MergableAuthenticationInfo）的merge，比如SimpleAuthenticationInfo会合并多个Principal为一个PrincipalCollection。

### AuthorizationInfo

![](https://images2015.cnblogs.com/blog/1107610/201702/1107610-20170216182925207-1572991216.png)

AuthorizationInfo用于聚合授权信息的：
1. **public** **interface** AuthorizationInfo **extends** Serializable {
1. Collection<String> getRoles(); //获取角色字符串信息
1. Collection<String> getStringPermissions(); //获取权限字符串信息
1. Collection<Permission> getObjectPermissions(); //获取Permission对象信息
1. }

当我们使用AuthorizingRealm时，如果身份验证成功，在进行授权时就通过doGetAuthorizationInfo方法获取角色/权限信息用于授权验证。

Shiro提供了一个实现SimpleAuthorizationInfo，大多数时候使用这个即可。

对于Account及SimpleAccount，之前的【6.3 AuthenticationInfo】已经介绍过了，用于SimpleAccountRealm子类，实现动态角色/权限维护的。

## Subject

![](https://images2015.cnblogs.com/blog/1107610/201702/1107610-20170216182936050-1308988136.png)

Subject是Shiro的核心对象，基本所有身份验证、授权都是通过Subject完成。

**1****、身份信息获取**

**Java****代码******
1. Object getPrincipal(); //Primary Principal
1. PrincipalCollection getPrincipals(); // PrincipalCollection

**2****、身份验证**

**Java****代码******
1. **void** login(AuthenticationToken token) **throws** AuthenticationException;
1. **boolean** isAuthenticated();
1. **boolean** isRemembered();

通过login登录，如果登录失败将抛出相应的AuthenticationException，如果登录成功调用isAuthenticated就会返回true，即已经通过身份验证；如果isRemembered返回true，表示是通过记住我功能登录的而不是调用login方法登录的。isAuthenticated/isRemembered是互斥的，即如果其中一个返回true，另一个返回false。

**3****、角色授权验证**

**Java****代码******
1. **boolean** hasRole(String roleIdentifier);
1. **boolean**[] hasRoles(List<String> roleIdentifiers);
1. **boolean** hasAllRoles(Collection<String> roleIdentifiers);
1. **void** checkRole(String roleIdentifier) **throws** AuthorizationException;
1. **void** checkRoles(Collection<String> roleIdentifiers) **throws** AuthorizationException;
1. **void** checkRoles(String... roleIdentifiers) **throws** AuthorizationException;

hasRole/*进行角色验证，验证后返回true/false；而checkRole/*验证失败时抛出AuthorizationException异常。

**4****、权限授权验证**

**Java****代码******
1. **boolean** isPermitted(String permission);
1. **boolean** isPermitted(Permission permission);
1. **boolean**[] isPermitted(String... permissions);
1. **boolean**[] isPermitted(List<Permission> permissions);
1. **boolean** isPermittedAll(String... permissions);
1. **boolean** isPermittedAll(Collection<Permission> permissions);
1. **void** checkPermission(String permission) **throws** AuthorizationException;
1. **void** checkPermission(Permission permission) **throws** AuthorizationException;
1. **void** checkPermissions(String... permissions) **throws** AuthorizationException;

10. **void** checkPermissions(Collection<Permission> permissions) **throws** AuthorizationException;

isPermitted/*进行权限验证，验证后返回true/false；而checkPermission/*验证失败时抛出AuthorizationException。

**5****、会话**

**Java****代码******
1. Session getSession(); //相当于getSession(true)
1. Session getSession(**boolean** create);

类似于Web中的会话。如果登录成功就相当于建立了会话，接着可以使用getSession获取；如果create=false如果没有会话将返回null，而create=true如果没有会话会强制创建一个。

**6****、退出**

**Java****代码******
1. **void** logout();

**7****、****RunAs**

**Java****代码******
1. **void** runAs(PrincipalCollection principals) **throws** NullPointerException, IllegalStateException;
1. **boolean** isRunAs();
1. PrincipalCollection getPreviousPrincipals();
1. PrincipalCollection releaseRunAs();

RunAs即实现“允许A假设为B身份进行访问”；通过调用subject.runAs(b)进行访问；接着调用subject.getPrincipals将获取到B的身份；此时调用isRunAs将返回true；而a的身份需要通过subject. getPreviousPrincipals获取；如果不需要RunAs了调用subject. releaseRunAs即可。

**8****、多线程**

**Java****代码******
1. <V> V execute(Callable<V> callable) **throws** ExecutionException;
1. **void** execute(Runnable runnable);
1. <V> Callable<V> associateWith(Callable<V> callable);
1. Runnable associateWith(Runnable runnable);

实现线程之间的Subject传播，因为Subject是线程绑定的；因此在多线程执行中需要传播到相应的线程才能获取到相应的Subject。最简单的办法就是通过execute(runnable/callable实例)直接调用；或者通过associateWith(runnable/callable实例)得到一个包装后的实例；它们都是通过：1、把当前线程的Subject绑定过去；2、在线程执行结束后自动释放。

Subject自己不会实现相应的身份验证/授权逻辑，而是通过DelegatingSubject委托给SecurityManager实现；及可以理解为Subject是一个面门。

对于Subject的构建一般没必要我们去创建；一般通过SecurityUtils.getSubject()获取：

**Java****代码******
1. **public** **static** Subject getSubject() {
1. Subject subject = ThreadContext.getSubject();
1. **if** (subject == **null**) {
1. subject = (**new** Subject.Builder()).buildSubject();
1. ThreadContext.bind(subject);
1. }
1. **return** subject;
1. }

即首先查看当前线程是否绑定了Subject，如果没有通过Subject.Builder构建一个然后绑定到现场返回。

如果想自定义创建，可以通过：

**Java****代码******
1. **new** Subject.Builder().principals(身份).authenticated(**true**/**false**).buildSubject()

这种可以创建相应的Subject实例了，然后自己绑定到线程即可。在new Builder()时如果没有传入SecurityManager，自动调用SecurityUtils.getSecurityManager获取；也可以自己传入一个实例。

## Shiro的jstl标签

Shiro提供了JSTL标签用于在JSP/GSP页面进行权限控制，如根据登录用户显示相应的页面按钮。

**导入标签库**

**Java****代码******
1. <%@taglib prefix="shiro" uri="http://shiro.apache.org/tags" %>

标签库定义在shiro-web.jar包下的META-INF/shiro.tld中定义。

**guest****标签**

**Java****代码******
1. <shiro:guest>
1. 欢迎游客访问，<a href="${pageContext.request.contextPath}/login.jsp">登录</a>
1. </shiro:guest>

用户没有身份验证时显示相应信息，即游客访问信息。

**user****标签**

**Java****代码******
1. <shiro:user>
1. 欢迎[<shiro:principal/>]登录，<a href="${pageContext.request.contextPath}/logout">退出</a>
1. </shiro:user>

用户已经身份验证/记住我登录后显示相应的信息。

**authenticated****标签**

**Java****代码******
1. <shiro:authenticated>
1. 用户[<shiro:principal/>]已身份验证通过
1. </shiro:authenticated>

用户已经身份验证通过，即Subject.login登录成功，不是记住我登录的。

**notAuthenticated****标签**

<shiro:notAuthenticated>

未身份验证（包括记住我）

</shiro:notAuthenticated>

用户已经身份验证通过，即没有调用Subject.login进行登录，包括记住我自动登录的也属于未进行身份验证。

**principal****标签**

<shiro: principal/>

显示用户身份信息，默认调用Subject.getPrincipal()获取，即Primary Principal。

**Java****代码******
1. <shiro:principal type="java.lang.String"/>

相当于Subject.getPrincipals().oneByType(String.class)。

**Java****代码******
1. <shiro:principal type="java.lang.String"/>

相当于Subject.getPrincipals().oneByType(String.class)。

**Java****代码******
1. <shiro:principal property="username"/>

相当于((User)Subject.getPrincipals()).getUsername()。

**hasRole****标签**

**Java****代码******
1. <shiro:hasRole name="admin">
1. 用户[<shiro:principal/>]拥有角色admin<br/>
1. </shiro:hasRole>

如果当前Subject有角色将显示body体内容。

**hasAnyRoles****标签**

**Java****代码******
1. <shiro:hasAnyRoles name="admin,user">
1. 用户[<shiro:principal/>]拥有角色admin或user<br/>
1. </shiro:hasAnyRoles>

如果当前Subject有任意一个角色（或的关系）将显示body体内容。

**lacksRole****标签**

**Java****代码******
1. <shiro:lacksRole name="abc">
1. 用户[<shiro:principal/>]没有角色abc<br/>
1. </shiro:lacksRole>

如果当前Subject没有角色将显示body体内容。

**hasPermission****标签**

**Java****代码******
1. <shiro:hasPermission name="user:create">
1. 用户[<shiro:principal/>]拥有权限user:create<br/>
1. </shiro:hasPermission>

如果当前Subject有权限将显示body体内容。

**lacksPermission****标签**

**Java****代码******
1. <shiro:lacksPermission name="org:create">
1. 用户[<shiro:principal/>]没有权限org:create<br/>
1. </shiro:lacksPermission>

如果当前Subject没有权限将显示body体内容。

另外又提供了几个权限控制相关的标签：

## Shiro与web

与spring集成：在Web.xml中
1. <filter>
1. <filter-name>shiroFilter</filter-name>
1. <filter-**class**>org.springframework.web.filter.DelegatingFilterProxy</filter-**class**>
1. <init-param>
1. <param-name>targetFilterLifecycle</param-name>
1. <param-value>**true**</param-value>
1. </init-param>
1. </filter>
1. <filter-mapping>
1. 10. <filter-name>shiroFilter</filter-name>
1. 11. <url-pattern>//*</url-pattern>

12. </filter-mapping>

DelegatingFilterProxy作用是自动到spring容器查找名字为shiroFilter（filter-name）的bean并把所有Filter的操作委托给它。然后将ShiroFilter配置到spring容器即可：

## Shiro集成spring

1. <!-- 缓存管理器 使用Ehcache实现 -->
1. <bean id="cacheManager" **class**="org.apache.shiro.cache.ehcache.EhCacheManager">
1. <property name="cacheManagerConfigFile" value="classpath:ehcache.xml"/>
1. </bean>
1.
1. <!-- 凭证匹配器 -->
1. <bean id="credentialsMatcher" **class**="
1. com.github.zhangkaitao.shiro.chapter12.credentials.RetryLimitHashedCredentialsMatcher">
1. <constructor-arg ref="cacheManager"/>
1. 10. <property name="hashAlgorithmName" value="md5"/>
1. 11. <property name="hashIterations" value="2"/>
1. 12. <property name="storedCredentialsHexEncoded" value="true"/>

13. </bean>

1. 14.

15. <!-- Realm实现 -->

16. <bean id="userRealm" **class**="com.github.zhangkaitao.shiro.chapter12.realm.UserRealm">

1. 17. <property name="userService" ref="userService"/>
1. 18. <property name="credentialsMatcher" ref="credentialsMatcher"/>
1. 19. <property name="cachingEnabled" value="true"/>
1. 20. <property name="authenticationCachingEnabled" value="true"/>
1. 21. <property name="authenticationCacheName" value="authenticationCache"/>
1. 22. <property name="authorizationCachingEnabled" value="true"/>
1. 23. <property name="authorizationCacheName" value="authorizationCache"/>

24. </bean>

25. <!-- 会话ID生成器 -->

26. <bean id="sessionIdGenerator"

27. **class**="org.apache.shiro.session.mgt.eis.JavaUuidSessionIdGenerator"/>

28. <!-- 会话DAO -->

29. <bean id="sessionDAO"

30. **class**="org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO">

1. 31. <property name="activeSessionsCacheName" value="shiro-activeSessionCache"/>
1. 32. <property name="sessionIdGenerator" ref="sessionIdGenerator"/>

33. </bean>

34. <!-- 会话验证调度器 -->

35. <bean id="sessionValidationScheduler"

36. **class**="org.apache.shiro.session.mgt.quartz.QuartzSessionValidationScheduler">

1. 37. <property name="sessionValidationInterval" value="1800000"/>
1. 38. <property name="sessionManager" ref="sessionManager"/>

39. </bean>

40. <!-- 会话管理器 -->

41. <bean id="sessionManager" **class**="org.apache.shiro.session.mgt.DefaultSessionManager">

1. 42. <property name="globalSessionTimeout" value="1800000"/>
1. 43. <property name="deleteInvalidSessions" value="true"/>
1. 44. <property name="sessionValidationSchedulerEnabled" value="true"/>
1. 45. <property name="sessionValidationScheduler" ref="sessionValidationScheduler"/>
1. 46. <property name="sessionDAO" ref="sessionDAO"/>

47. </bean>

48. <!-- 安全管理器 -->

49. <bean id="securityManager" **class**="org.apache.shiro.mgt.DefaultSecurityManager">

1. 50. <property name="realms">
1. 51. <list><ref bean="userRealm"/></list>
1. 52. </property>
1. 53. <property name="sessionManager" ref="sessionManager"/>
1. 54. <property name="cacheManager" ref="cacheManager"/>

55. </bean>

56. <!-- 相当于调用SecurityUtils.setSecurityManager(securityManager) -->

57. <bean **class**="org.springframework.beans.factory.config.MethodInvokingFactoryBean">

58. <property name="staticMethod"

59. value="org.apache.shiro.SecurityUtils.setSecurityManager"/>

1. 60. <property name="arguments" ref="securityManager"/>

61. </bean>

62. <!-- Shiro生命周期处理器-->

63. <bean id="lifecycleBeanPostProcessor"

64. **class**="org.apache.shiro.spring.LifecycleBeanPostProcessor"/>

可以看出，只要把之前的ini配置翻译为此处的spring xml配置方式即可，无须多解释。LifecycleBeanPostProcessor用于在实现了Initializable接口的Shiro bean初始化时调用Initializable接口回调，在实现了Destroyable接口的Shiro bean销毁时调用 Destroyable接口回调。如UserRealm就实现了Initializable，而DefaultSecurityManager实现了Destroyable。具体可以查看它们的继承关系。

### Web应用：

Web应用和普通JavaSE应用的某些配置是类似的，此处只提供一些不一样的配置，详细配置可以参考spring-shiro-web.xml。
1. <!-- 会话Cookie模板 -->
1. <bean id="sessionIdCookie" **class**="org.apache.shiro.web.servlet.SimpleCookie">
1. <constructor-arg value="sid"/>
1. <property name="httpOnly" value="true"/>
1. <property name="maxAge" value="180000"/>
1. </bean>
1. <!-- 会话管理器 -->
1. <bean id="sessionManager"
1. **class**="org.apache.shiro.web.session.mgt.DefaultWebSessionManager">
1. 10. <property name="globalSessionTimeout" value="1800000"/>
1. 11. <property name="deleteInvalidSessions" value="true"/>
1. 12. <property name="sessionValidationSchedulerEnabled" value="true"/>
1. 13. <property name="sessionValidationScheduler" ref="sessionValidationScheduler"/>
1. 14. <property name="sessionDAO" ref="sessionDAO"/>
1. 15. <property name="sessionIdCookieEnabled" value="true"/>
1. 16. <property name="sessionIdCookie" ref="sessionIdCookie"/>

17. </bean>

18. <!-- 安全管理器 -->

19. <bean id="securityManager" **class**="org.apache.shiro.web.mgt.DefaultWebSecurityManager">

20. <property name="realm" ref="userRealm"/>

1. 21. <property name="sessionManager" ref="sessionManager"/>
1. 22. <property name="cacheManager" ref="cacheManager"/>

23. </bean>

1、sessionIdCookie是用于生产Session ID Cookie的模板；

2、会话管理器使用用于web环境的DefaultWebSessionManager；

3、安全管理器使用用于web环境的DefaultWebSecurityManager。

1. <!-- 基于Form表单的身份验证过滤器 -->
1. <bean id="formAuthenticationFilter"
1. **class**="org.apache.shiro.web.filter.authc.FormAuthenticationFilter">
1. <property name="usernameParam" value="username"/>
1. <property name="passwordParam" value="password"/>
1. <property name="loginUrl" value="/login.jsp"/>
1. </bean>
1. <!-- Shiro的Web过滤器 -->
1. <bean id="shiroFilter" **class**="org.apache.shiro.spring.web.ShiroFilterFactoryBean">
1. 10. <property name="securityManager" ref="securityManager"/>
1. 11. <property name="loginUrl" value="/login.jsp"/>
1. 12. <property name="unauthorizedUrl" value="/unauthorized.jsp"/>
1. 13. <property name="filters">
1. 14. <util:map>
1. 15. <entry key="authc" value-ref="formAuthenticationFilter"/>
1. 16. </util:map>
1. 17. </property>
1. 18. <property name="filterChainDefinitions">
1. 19. <value>
1. 20. /index.jsp = anon
1. 21. /unauthorized.jsp = anon
1. 22. /login.jsp = authc
1. 23. /logout = logout
1. 24. //*/* = user
1. 25. </value>
1. 26. </property>

27. </bean>

1、formAuthenticationFilter为基于Form表单的身份验证过滤器；此处可以再添加自己的Filter bean定义；

2、shiroFilter：此处使用ShiroFilterFactoryBean来创建ShiroFilter过滤器；filters属性用于定义自己的过滤器，即ini配置中的[filters]部分；filterChainDefinitions用于声明url和filter的关系，即ini配置中的[urls]部分。

## Shiro权限注解

### 注意:

在spring中需要开启权限注解与aop:

<!-- AOP式方法级权限检查 -->
<bean class="org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator"
depends-on="lifecycleBeanPostProcessor"/>
<!-- 启用shrio授权注解拦截方式 -->
<bean class="org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor">
<property name="securityManager" ref="securityManager"/>
</bean>

Shiro提供了相应的注解用于权限控制，如果使用这些注解就需要使用AOP的功能来进行判断，如Spring AOP；Shiro提供了Spring AOP集成用于权限注解的解析和验证。

为了测试，此处使用了Spring MVC来测试Shiro注解，当然Shiro注解不仅仅可以在web环境使用，在独立的JavaSE中也是可以用的，此处只是以web为例了。

在spring-mvc.xml配置文件添加Shiro Spring AOP权限注解的支持：
1. <aop:config proxy-target-**class**="true"></aop:config>
1. <bean **class**="
1. org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor">
1. <property name="securityManager" ref="securityManager"/>
1. </bean>

如上配置用于开启Shiro Spring AOP权限注解的支持；<aop:config proxy-target-class="true">表示代理类。

接着就可以在相应的控制器（AnnotationController）中使用如下方式进行注解：
1. @RequiresRoles("admin")
1. @RequestMapping("/hello2")
1. **public** String hello2() {
1. **return** "success";
1. }

访问hello2方法的前提是当前用户有admin角色。

当验证失败，其会抛出UnauthorizedException异常，此时可以使用Spring的ExceptionHandler（DefaultExceptionHandler）来进行拦截处理：
1. @ExceptionHandler({UnauthorizedException.**class**})
1. @ResponseStatus(HttpStatus.UNAUTHORIZED)
1. **public** ModelAndView processUnauthenticatedException(NativeWebRequest request, UnauthorizedException e) {
1. ModelAndView mv = **new** ModelAndView();
1. mv.addObject("exception", e);
1. mv.setViewName("unauthorized");
1. **return** mv;
1. }

**权限注解**

**Java****代码******
1. @RequiresAuthentication

表示当前Subject已经通过login进行了身份验证；即Subject. isAuthenticated()返回true。

**Java****代码******
1. @RequiresUser

表示当前Subject已经身份验证或者通过记住我登录的。

**Java****代码******
1. @RequiresGuest

表示当前Subject没有身份验证或通过记住我登录过，即是游客身份。

**Java****代码******
1. @RequiresRoles(value={“admin”, “user”}, logical= Logical.AND)

表示当前Subject需要角色admin和user。

**Java****代码******
1. **@RequiresPermissions** (value={“user:a”, “user:b”}, logical= Logical.OR)

表示当前Subject需要权限user:a或user:b。

## Shiro 完整项目配置

**第一步：配置****web.xml**
<!-- 配置Shiro过滤器,先让Shiro过滤系统接收到的请求 -->

<!-- 这里filter-name必须对应applicationContext.xml中定义的<bean id="shiroFilter"/> -->

<!-- 使用[//*]匹配所有请求,保证所有的可控请求都经过Shiro的过滤 -->

<!-- 通常会将此filter-mapping放置到最前面(即其他filter-mapping前面),以保证它是过滤器链中第一个起作用的 -->

<filter>

<filter-name>shiroFilter</filter-name>

<filter-class>org.springframework.web.filter.DelegatingFilterProxy</filter-class>

<init-param>

<!-- 该值缺省为false,表示生命周期由SpringApplicationContext管理,设置为true则表示由ServletContainer管理 -->

<param-name>targetFilterLifecycle</param-name>

<param-value>true</param-value>

</init-param>

</filter>

<filter-mapping>

<filter-name>shiroFilter</filter-name>

<url-pattern>//*</url-pattern>

</filter-mapping>

**第二步：配置****applicationContext.xml**

<!-- 继承自AuthorizingRealm的自定义Realm,即指定Shiro验证用户登录的类为自定义的ShiroDbRealm.java -->

<bean id="myRealm" class="com.jadyer.realm.MyRealm"/>

<!-- Shiro默认会使用Servlet容器的Session,可通过sessionMode属性来指定使用Shiro原生Session -->

<!-- 即<property name="sessionMode" value="native"/>,详细说明见官方文档 -->

<!-- 这里主要是设置自定义的单Realm应用,若有多个Realm,可使用'realms'属性代替 -->

<bean id="securityManager" class="org.apache.shiro.web.mgt.DefaultWebSecurityManager">

<property name="realm" ref="myRealm"/>

</bean>

<!-- Shiro主过滤器本身功能十分强大,其强大之处就在于它支持任何基于URL路径表达式的、自定义的过滤器的执行 -->

<!-- Web应用中,Shiro可控制的Web请求必须经过Shiro主过滤器的拦截,Shiro对基于Spring的Web应用提供了完美的支持 -->

<bean id="shiroFilter" class="org.apache.shiro.spring.web.ShiroFilterFactoryBean">

<!-- Shiro的核心安全接口,这个属性是必须的 -->

<property name="securityManager" ref="securityManager"/>

<!-- 要求登录时的链接(可根据项目的URL进行替换),非必须的属性,默认会自动寻找Web工程根目录下的"/login.jsp"页面 -->

<property name="loginUrl" value="/"/>

<!-- 登录成功后要跳转的连接(本例中此属性用不到,因为登录成功后的处理逻辑在LoginController里硬编码为main.jsp了) -->

<!-- <property name="successUrl" value="/system/main"/> -->

<!-- 用户访问未对其授权的资源时,所显示的连接 -->

<!-- 若想更明显的测试此属性可以修改它的值,如unauthor.jsp,然后用[玄玉]登录后访问/admin/listUser.jsp就看见浏览器会显示unauthor.jsp -->

<property name="unauthorizedUrl" value="/"/>

<!-- Shiro连接约束配置,即过滤链的定义 -->

<!-- 此处可配合我的这篇文章来理解各个过滤连的作用http://blog.csdn.net/jadyer/article/details/12172839 -->

<!-- 下面value值的第一个'/'代表的路径是相对于HttpServletRequest.getContextPath()的值来的 -->

<!-- anon：它对应的过滤器里面是空的,什么都没做,这里.do和.jsp后面的/*表示参数,比方说login.jsp?main这种 -->

<!-- authc：该过滤器下的页面必须验证后才能访问,它是Shiro内置的一个拦截器org.apache.shiro.web.filter.authc.FormAuthenticationFilter -->

<property name="filterChainDefinitions">

<value>

/mydemo/login=anon

/mydemo/getVerifyCodeImage=anon

/main/*/*=authc

/user/info/*/*=authc

/admin/listUser/*/*=authc,perms[admin:manage]

</value>

</property>

</bean>

<!-- 保证实现了Shiro内部lifecycle函数的bean执行 -->

<bean id="lifecycleBeanPostProcessor" class="org.apache.shiro.spring.LifecycleBeanPostProcessor"/>

<!-- 开启Shiro的注解(如@RequiresRoles,@RequiresPermissions),需借助SpringAOP扫描使用Shiro注解的类,并在必要时进行安全逻辑验证 -->

<!-- 配置以下两个bean即可实现此功能 -->

<!-- Enable Shiro Annotations for Spring-configured beans. Only run after the lifecycleBeanProcessor has run -->

<!-- 由于本例中并未使用Shiro注解,故注释掉这两个bean(个人觉得将权限通过注解的方式硬编码在程序中,查看起来不是很方便,没必要使用) -->

<!--

<bean class="org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator" depends-on="lifecycleBeanPostProcessor"/>

<bean class="org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor">

<property name="securityManager" ref="securityManager"/>

</bean>

-->

**第三步：自定义的****Realm****类**

public class MyRealm extends AuthorizingRealm {

//*/*

/* 为当前登录的Subject授予角色和权限

/* @see 经测试:本例中该方法的调用时机为需授权资源被访问时

/* @see 经测试:并且每次访问需授权资源时都会执行该方法中的逻辑,这表明本例中默认并未启用AuthorizationCache

/* @see 个人感觉若使用了Spring3.1开始提供的ConcurrentMapCache支持,则可灵活决定是否启用AuthorizationCache

/* @see 比如说这里从数据库获取权限信息时,先去访问Spring3.1提供的缓存,而不使用Shior提供的AuthorizationCache

/*/

@Override

protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals){

//获取当前登录的用户名,等价于(String)principals.fromRealm(this.getName()).iterator().next()

String currentUsername = (String)super.getAvailablePrincipal(principals);

// List<String> roleList = new ArrayList<String>();

// List<String> permissionList = new ArrayList<String>();

// //从数据库中获取当前登录用户的详细信息

// User user = userService.getByUsername(currentUsername);

// if(null != user){

// //实体类User中包含有用户角色的实体类信息

// if(null!=user.getRoles() && user.getRoles().size()>0){

// //获取当前登录用户的角色

// for(Role role : user.getRoles()){

// roleList.add(role.getName());

// //实体类Role中包含有角色权限的实体类信息

// if(null!=role.getPermissions() && role.getPermissions().size()>0){

// //获取权限

// for(Permission pmss : role.getPermissions()){

// if(!StringUtils.isEmpty(pmss.getPermission())){

// permissionList.add(pmss.getPermission());

// }

// }

// }

// }

// }

// }else{

// throw new AuthorizationException();

// }

// //为当前用户设置角色和权限

// SimpleAuthorizationInfo simpleAuthorInfo = new SimpleAuthorizationInfo();

// simpleAuthorInfo.addRoles(roleList);

// simpleAuthorInfo.addStringPermissions(permissionList);

SimpleAuthorizationInfo simpleAuthorInfo = new SimpleAuthorizationInfo();

//实际中可能会像上面注释的那样从数据库取得

if(null!=currentUsername && "mike".equals(currentUsername)){

//添加一个角色,不是配置意义上的添加,而是证明该用户拥有admin角色

simpleAuthorInfo.addRole("admin");

//添加权限

simpleAuthorInfo.addStringPermission("admin:manage");

System.out.println("已为用户[mike]赋予了[admin]角色和[admin:manage]权限");

return simpleAuthorInfo;

}

//若该方法什么都不做直接返回null的话,就会导致任何用户访问/admin/listUser.jsp时都会自动跳转到unauthorizedUrl指定的地址

//详见applicationContext.xml中的<bean id="shiroFilter">的配置

return null;

}

//*/*

/* 验证当前登录的Subject

/* @see 经测试:本例中该方法的调用时机为LoginController.login()方法中执行Subject.login()时

/*/

@Override

protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authcToken) throws AuthenticationException {

//获取基于用户名和密码的令牌

//实际上这个authcToken是从LoginController里面currentUser.login(token)传过来的

//两个token的引用都是一样的

UsernamePasswordToken token = (UsernamePasswordToken)authcToken;

System.out.println("验证当前Subject时获取到token为" + ReflectionToStringBuilder.toString(token, ToStringStyle.MULTI_LINE_STYLE));

// User user = userService.getByUsername(token.getUsername());

// if(null != user){

// AuthenticationInfo authcInfo = new SimpleAuthenticationInfo(user.getUsername(), user.getPassword(), user.getNickname());

// this.setSession("currentUser", user);

// return authcInfo;

// }else{

// return null;

// }

//此处无需比对,比对的逻辑Shiro会做,我们只需返回一个和令牌相关的正确的验证信息

//说白了就是第一个参数填登录用户名,第二个参数填合法的登录密码(可以是从数据库中取到的,本例中为了演示就硬编码了)

//这样一来,在随后的登录页面上就只有这里指定的用户和密码才能通过验证

if("mike".equals(token.getUsername())){

AuthenticationInfo authcInfo = new SimpleAuthenticationInfo("mike", "mike", this.getName());

this.setSession("currentUser", "mike");

return authcInfo;

}

//没有返回登录用户名对应的SimpleAuthenticationInfo对象时,就会在LoginController中抛出UnknownAccountException异常

return null;

}

//*/*

/* 将一些数据放到ShiroSession中,以便于其它地方使用

/* @see 比如Controller,使用时直接用HttpSession.getAttribute(key)就可以取到

/*/

private void setSession(Object key, Object value){

Subject currentUser = SecurityUtils.getSubject();

if(null != currentUser){

Session session = currentUser.getSession();

System.out.println("Session默认超时时间为[" + session.getTimeout() + "]毫秒");

if(null != session){

session.setAttribute(key, value);

}

}

}

}