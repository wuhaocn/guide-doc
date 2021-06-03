<div class="article-inner">

<header class="article-header">

<h1 class="article-title" itemprop="name">
精尽 MyBatis 面试题
</h1>

<a href="/MyBatis/Interview/" class="archive-article-date">
<time style="display: none;" datetime="2019-10-23T16:00:00.000Z" itemprop="datePublished"><i class="icon-calendar icon"></i>2019-10-24</time>
</a>

</header>

<div class="article-entry" itemprop="articleBody">

<p>以下面试题，基于网络整理，和自己编辑。具体参考的文章，会在文末给出所有的链接。</p>
<p>如果胖友有自己的疑问，欢迎在星球提问，我们一起整理吊吊的 MyBatis 面试题的大保健。</p>
<p>而题目的难度，艿艿尽量按照从容易到困难的顺序，逐步下去。</p>
<h2 id="MyBatis-编程步骤"><a href="#MyBatis-编程步骤" class="headerlink" title="MyBatis 编程步骤"></a>MyBatis 编程步骤</h2><ol>
<li>创建 SqlSessionFactory 对象。</li>
<li>通过 SqlSessionFactory 获取 SqlSession 对象。</li>
<li>通过 SqlSession 获得 Mapper 代理对象。</li>
<li>通过 Mapper 代理对象，执行数据库操作。</li>
<li>执行成功，则使用 SqlSession 提交事务。</li>
<li>执行失败，则使用 SqlSession 回滚事务。</li>
<li>最终，关闭会话。</li>
</ol>
<h2 id="和-的区别是什么？"><a href="#和-的区别是什么？" class="headerlink" title="#{} 和 ${} 的区别是什么？"></a><code>#{}</code> 和 <code>${}</code> 的区别是什么？</h2><p><code>${}</code> 是 Properties 文件中的变量占位符，它可以用于 XML 标签属性值和 SQL 内部，属于<strong>字符串替换</strong>。例如将 <code>${driver}</code> 会被静态替换为 <code>com.mysql.jdbc.Driver</code> ：</p>
<figure class="highlight xml"><table><tbody><tr><td class="code"><pre><span class="line"><span class="tag">&lt;<span class="name">dataSource</span> <span class="attr">type</span>=<span class="string">"UNPOOLED"</span>&gt;</span></span><br><span class="line">    <span class="tag">&lt;<span class="name">property</span> <span class="attr">name</span>=<span class="string">"driver"</span> <span class="attr">value</span>=<span class="string">"${driver}"</span>/&gt;</span></span><br><span class="line">    <span class="tag">&lt;<span class="name">property</span> <span class="attr">name</span>=<span class="string">"url"</span> <span class="attr">value</span>=<span class="string">"${url}"</span>/&gt;</span></span><br><span class="line">    <span class="tag">&lt;<span class="name">property</span> <span class="attr">name</span>=<span class="string">"username"</span> <span class="attr">value</span>=<span class="string">"${username}"</span>/&gt;</span></span><br><span class="line"><span class="tag">&lt;/<span class="name">dataSource</span>&gt;</span></span><br></pre></td></tr></tbody></table></figure>
<p><code>${}</code> 也可以对传递进来的参数<strong>原样拼接</strong>在 SQL 中。代码如下：</p>
<figure class="highlight xml"><table><tbody><tr><td class="code"><pre><span class="line"><span class="tag">&lt;<span class="name">select</span> <span class="attr">id</span>=<span class="string">"getSubject3"</span> <span class="attr">parameterType</span>=<span class="string">"Integer"</span> <span class="attr">resultType</span>=<span class="string">"Subject"</span>&gt;</span></span><br><span class="line">    SELECT * FROM subject</span><br><span class="line">    WHERE id = ${id}</span><br><span class="line"><span class="tag">&lt;/<span class="name">select</span>&gt;</span></span><br></pre></td></tr></tbody></table></figure>
<ul>
<li>实际场景下，不推荐这么做。因为，可能有 SQL 注入的风险。</li>
</ul>
<hr>
<p><code>#{}</code> 是 SQL 的参数占位符，Mybatis 会将 SQL 中的 <code>#{}</code> 替换为 <code>?</code> 号，在 SQL 执行前会使用 PreparedStatement 的参数设置方法，按序给 SQL 的 <code>?</code> 号占位符设置参数值，比如 <code>ps.setInt(0, parameterValue)</code> 。 所以，<code>#{}</code> 是<strong>预编译处理</strong>，可以有效防止 SQL 注入，提高系统安全性。</p>
<hr>
<p>另外，<code>#{}</code> 和 <code>${}</code> 的取值方式非常方便。例如：<code>#{item.name}</code> 的取值方式，为使用反射从参数对象中，获取 <code>item</code> 对象的 <code>name</code> 属性值，相当于 <code>param.getItem().getName()</code> 。</p>
<h2 id="当实体类中的属性名和表中的字段名不一样-，怎么办？"><a href="#当实体类中的属性名和表中的字段名不一样-，怎么办？" class="headerlink" title="当实体类中的属性名和表中的字段名不一样 ，怎么办？"></a>当实体类中的属性名和表中的字段名不一样 ，怎么办？</h2><p>第一种， 通过在查询的 SQL 语句中定义字段名的别名，让字段名的别名和实体类的属性名一致。代码如下：</p>
<figure class="highlight sql"><table><tbody><tr><td class="code"><pre><span class="line">&lt;select id="selectOrder" parameterType="Integer" resultType="Order"&gt; </span><br><span class="line">    <span class="keyword">SELECT</span> order_id <span class="keyword">AS</span> <span class="keyword">id</span>, order_no <span class="keyword">AS</span> orderno, order_price <span class="keyword">AS</span> price </span><br><span class="line">    <span class="keyword">FROM</span> orders </span><br><span class="line">    <span class="keyword">WHERE</span> order_id = #{<span class="keyword">id</span>}</span><br><span class="line">&lt;/<span class="keyword">select</span>&gt;</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li>这里，艿艿还有几点建议：<ul>
<li>1、数据库的关键字，统一使用大写，例如：<code>SELECT</code>、<code>AS</code>、<code>FROM</code>、<code>WHERE</code> 。</li>
<li>2、每 5 个查询字段换一行，保持整齐。</li>
<li>3、<code>,</code> 的后面，和 <code>=</code> 的前后，需要有空格，更加清晰。</li>
<li>4、<code>SELECT</code>、<code>FROM</code>、<code>WHERE</code> 等，单独一行，高端大气。</li>
</ul>
</li>
</ul>
<hr>
<p>第二种，是第一种的特殊情况。大多数场景下，数据库字段名和实体类中的属性名差，主要是前者为<strong>下划线风格</strong>，后者为<strong>驼峰风格</strong>。在这种情况下，可以直接配置如下，实现自动的下划线转驼峰的功能。</p>
<figure class="highlight xml"><table><tbody><tr><td class="code"><pre><span class="line"><span class="tag">&lt;<span class="name">setting</span> <span class="attr">name</span>=<span class="string">"logImpl"</span> <span class="attr">value</span>=<span class="string">"LOG4J"</span>/&gt;</span></span><br><span class="line">    <span class="tag">&lt;<span class="name">setting</span> <span class="attr">name</span>=<span class="string">"mapUnderscoreToCamelCase"</span> <span class="attr">value</span>=<span class="string">"true"</span> /&gt;</span></span><br><span class="line"><span class="tag">&lt;/<span class="name">settings</span>&gt;</span></span><br></pre></td></tr></tbody></table></figure>
<p>😈 也就说，约定大于配置。非常推荐！</p>
<hr>
<p>第三种，通过 <code>&lt;resultMap&gt;</code> 来映射字段名和实体类属性名的一一对应的关系。代码如下：</p>
<figure class="highlight xml"><table><tbody><tr><td class="code"><pre><span class="line"><span class="tag">&lt;<span class="name">resultMap</span> <span class="attr">type</span>=<span class="string">"me.gacl.domain.Order"</span> <span class="attr">id</span>=<span class="string">”OrderResultMap”</span>&gt;</span> </span><br><span class="line">    <span class="tag">&lt;<span class="name">!–-</span> 用 <span class="attr">id</span> 属性来映射主键字段 <span class="attr">-</span>–&gt;</span> </span><br><span class="line">    <span class="tag">&lt;<span class="name">id</span> <span class="attr">property</span>=<span class="string">"id"</span> <span class="attr">column</span>=<span class="string">"order_id"</span>&gt;</span> </span><br><span class="line">    <span class="tag">&lt;<span class="name">!–-</span> 用 <span class="attr">result</span> 属性来映射非主键字段，<span class="attr">property</span> 为实体类属性名，<span class="attr">column</span> 为数据表中的属性 <span class="attr">-</span>–&gt;</span> </span><br><span class="line">    <span class="tag">&lt;<span class="name">result</span> <span class="attr">property</span>=<span class="string">"orderNo"</span> <span class="attr">column</span> =<span class="string">"order_no"</span> /&gt;</span> </span><br><span class="line">    <span class="tag">&lt;<span class="name">result</span> <span class="attr">property</span>=<span class="string">"price"</span> <span class="attr">column</span>=<span class="string">"order_price"</span> /&gt;</span> </span><br><span class="line"><span class="tag">&lt;/<span class="name">resultMap</span>&gt;</span></span><br><span class="line"></span><br><span class="line"><span class="tag">&lt;<span class="name">select</span> <span class="attr">id</span>=<span class="string">"getOrder"</span> <span class="attr">parameterType</span>=<span class="string">"Integer"</span> <span class="attr">resultMap</span>=<span class="string">"OrderResultMap"</span>&gt;</span></span><br><span class="line">    SELECT * </span><br><span class="line">    FROM orders </span><br><span class="line">    WHERE order_id = #{id}</span><br><span class="line"><span class="tag">&lt;/<span class="name">select</span>&gt;</span></span><br></pre></td></tr></tbody></table></figure>
<ul>
<li>此处 <code>SELECT *</code> 仅仅作为示例只用，实际场景下，千万千万千万不要这么干。用多少字段，查询多少字段。</li>
<li>相比第一种，第三种的<strong>重用性</strong>会一些。</li>
</ul>
<h2 id="XML-映射文件中，除了常见的-select-insert-update-delete标-签之外，还有哪些标签？"><a href="#XML-映射文件中，除了常见的-select-insert-update-delete标-签之外，还有哪些标签？" class="headerlink" title="XML 映射文件中，除了常见的 select | insert | update | delete标 签之外，还有哪些标签？"></a>XML 映射文件中，除了常见的 select | insert | update | delete标 签之外，还有哪些标签？</h2><p>如下部分，可见 <a href="http://www.mybatis.org/mybatis-3/zh/sqlmap-xml.html" rel="external nofollow noopener noreferrer" target="_blank">《MyBatis 文档 —— Mapper XML 文件》</a> ：</p>
<ul>
<li><code>&lt;cache /&gt;</code> 标签，给定命名空间的缓存配置。<ul>
<li><code>&lt;cache-ref /&gt;</code> 标签，其他命名空间缓存配置的引用。</li>
</ul>
</li>
<li><code>&lt;resultMap /&gt;</code> 标签，是最复杂也是最强大的元素，用来描述如何从数据库结果集中来加载对象。</li>
<li><del><code>&lt;parameterMap /&gt;</code> 标签，已废弃！老式风格的参数映射。内联参数是首选,这个元素可能在将来被移除，这里不会记录。</del></li>
<li><code>&lt;sql /&gt;</code> 标签，可被其他语句引用的可重用语句块。<ul>
<li><code>&lt;include /&gt;</code> 标签，引用 <code>&lt;sql /&gt;</code> 标签的语句。</li>
</ul>
</li>
<li><code>&lt;selectKey /&gt;</code> 标签，不支持自增的主键生成策略标签。</li>
</ul>
<p>如下部分，可见 <a href="http://www.mybatis.org/mybatis-3/zh/dynamic-sql.html" rel="external nofollow noopener noreferrer" target="_blank">《MyBatis 文档 —— 动态 SQL》</a> ：</p>
<ul>
<li><code>&lt;if /&gt;</code></li>
<li><code>&lt;choose /&gt;</code>、<code>&lt;when /&gt;</code>、<code>&lt;otherwise /&gt;</code></li>
<li><code>&lt;trim /&gt;</code>、<code>&lt;where /&gt;</code>、<code>&lt;set /&gt;</code></li>
<li><code>&lt;foreach /&gt;</code></li>
<li><code>&lt;bind /&gt;</code></li>
</ul>
<h2 id="Mybatis-动态-SQL-是做什么的？都有哪些动态-SQL-？能简述一下动态-SQL-的执行原理吗？"><a href="#Mybatis-动态-SQL-是做什么的？都有哪些动态-SQL-？能简述一下动态-SQL-的执行原理吗？" class="headerlink" title="Mybatis 动态 SQL 是做什么的？都有哪些动态 SQL ？能简述一下动态 SQL 的执行原理吗？"></a>Mybatis 动态 SQL 是做什么的？都有哪些动态 SQL ？能简述一下动态 SQL 的执行原理吗？</h2><ul>
<li>Mybatis 动态 SQL ，可以让我们在 XML 映射文件内，以 XML 标签的形式编写动态 SQL ，完成逻辑判断和动态拼接 SQL 的功能。</li>
<li>Mybatis 提供了 9 种动态 SQL 标签：<code>&lt;if /&gt;</code>、<code>&lt;choose /&gt;</code>、<code>&lt;when /&gt;</code>、<code>&lt;otherwise /&gt;</code>、<code>&lt;trim /&gt;</code>、<code>&lt;where /&gt;</code>、<code>&lt;set /&gt;</code>、<code>&lt;foreach /&gt;</code>、<code>&lt;bind /&gt;</code> 。</li>
<li>其执行原理为，使用 <strong>OGNL</strong> 的表达式，从 SQL 参数对象中计算表达式的值，根据表达式的值动态拼接 SQL ，以此来完成动态 SQL 的功能。</li>
</ul>
<p>如上的内容，更加详细的话，请看 <a href="http://www.mybatis.org/mybatis-3/zh/dynamic-sql.html" rel="external nofollow noopener noreferrer" target="_blank">《MyBatis 文档 —— 动态 SQL》</a> 文档。</p>
<h2 id="最佳实践中，通常一个-XML-映射文件，都会写一个-Mapper-接口与之对应。请问，这个-Mapper-接口的工作原理是什么？Mapper-接口里的方法，参数不同时，方法能重载吗？"><a href="#最佳实践中，通常一个-XML-映射文件，都会写一个-Mapper-接口与之对应。请问，这个-Mapper-接口的工作原理是什么？Mapper-接口里的方法，参数不同时，方法能重载吗？" class="headerlink" title="最佳实践中，通常一个 XML 映射文件，都会写一个 Mapper 接口与之对应。请问，这个 Mapper 接口的工作原理是什么？Mapper 接口里的方法，参数不同时，方法能重载吗？"></a>最佳实践中，通常一个 XML 映射文件，都会写一个 Mapper 接口与之对应。请问，这个 Mapper 接口的工作原理是什么？Mapper 接口里的方法，参数不同时，方法能重载吗？</h2><p>Mapper 接口，对应的关系如下：</p>
<ul>
<li>接口的全限名，就是映射文件中的 <code>"namespace"</code> 的值。</li>
<li>接口的方法名，就是映射文件中 MappedStatement 的 <code>"id"</code> 值。</li>
<li>接口方法内的参数，就是传递给 SQL 的参数。</li>
</ul>
<p>Mapper 接口是没有实现类的，当调用接口方法时，接口全限名 + 方法名拼接字符串作为 key 值，可唯一定位一个对应的 MappedStatement 。举例：<code>com.mybatis3.mappers.StudentDao.findStudentById</code> ，可以唯一找到 <code>"namespace"</code> 为 <code>com.mybatis3.mappers.StudentDao</code> 下面 <code>"id"</code> 为 <code>findStudentById</code> 的 MappedStatement 。</p>
<p>总结来说，在 Mybatis 中，每一个 <code>&lt;select /&gt;</code>、<code>&lt;insert /&gt;</code>、<code>&lt;update /&gt;</code>、<code>&lt;delete /&gt;</code> 标签，都会被解析为一个 MappedStatement 对象。</p>
<p>另外，Mapper 接口的实现类，通过 MyBatis 使用 <strong>JDK Proxy</strong> 自动生成其代理对象 Proxy ，而代理对象 Proxy 会拦截接口方法，从而“调用”对应的 MappedStatement 方法，最终执行 SQL ，返回执行结果。整体流程如下图：<img src="http://static2.iocoder.cn/images/MyBatis/2020_03_15/02.png" alt="流程"></p>
<ul>
<li><p>其中，SqlSession 在调用 Executor 之前，会获得对应的 MappedStatement 方法。例如：<code>DefaultSqlSession#select(String statement, Object parameter, RowBounds rowBounds, ResultHandler handler)</code> 方法，代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="comment">// DefaultSqlSession.java</span></span><br><span class="line"></span><br><span class="line"><span class="meta">@Override</span></span><br><span class="line"><span class="function"><span class="keyword">public</span> <span class="keyword">void</span> <span class="title">select</span><span class="params">(String statement, Object parameter, RowBounds rowBounds, ResultHandler handler)</span> </span>{</span><br><span class="line">    <span class="keyword">try</span> {</span><br><span class="line">        <span class="comment">// 获得 MappedStatement 对象</span></span><br><span class="line">        MappedStatement ms = configuration.getMappedStatement(statement);</span><br><span class="line">        <span class="comment">// 执行查询</span></span><br><span class="line">        executor.query(ms, wrapCollection(parameter), rowBounds, handler);</span><br><span class="line">    } <span class="keyword">catch</span> (Exception e) {</span><br><span class="line">        <span class="keyword">throw</span> ExceptionFactory.wrapException(<span class="string">"Error querying database.  Cause: "</span> + e, e);</span><br><span class="line">    } <span class="keyword">finally</span> {</span><br><span class="line">        ErrorContext.instance().reset();</span><br><span class="line">    }</span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li>完整的流程，胖友可以慢慢撸下 MyBatis 的源码。</li>
</ul>
</li>
</ul>
<hr>
<p>Mapper 接口里的方法，是不能重载的，因为是<strong>全限名 + 方法名</strong>的保存和寻找策略。😈 所以有时，想个 Mapper 接口里的方法名，还是蛮闹心的，嘿嘿。</p>
<h2 id="Mapper-接口绑定有几种实现方式-分别是怎么实现的"><a href="#Mapper-接口绑定有几种实现方式-分别是怎么实现的" class="headerlink" title="Mapper 接口绑定有几种实现方式,分别是怎么实现的?"></a>Mapper 接口绑定有几种实现方式,分别是怎么实现的?</h2><p>接口绑定有三种实现方式：</p>
<p>第一种，通过 <strong>XML Mapper</strong> 里面写 SQL 来绑定。在这种情况下，要指定 XML 映射文件里面的 <code>"namespace"</code> 必须为接口的全路径名。</p>
<p>第二种，通过<strong>注解</strong>绑定，就是在接口的方法上面加上 <code>@Select</code>、<code>@Update</code>、<code>@Insert</code>、<code>@Delete</code> 注解，里面包含 SQL 语句来绑定。</p>
<p>第三种，是第二种的特例，也是通过<strong>注解</strong>绑定，在接口的方法上面加上 <code>@SelectProvider</code>、<code>@UpdateProvider</code>、<code>@InsertProvider</code>、<code>@DeleteProvider</code> 注解，通过 Java 代码，生成对应的动态 SQL 。</p>
<hr>
<p>实际场景下，最最最推荐的是<strong>第一种</strong>方式。因为，SQL 通过注解写在 Java 代码中，会非常杂乱。而写在 XML 中，更加有整体性，并且可以更加方便的使用 OGNL 表达式。</p>
<h2 id="Mybatis-的-XML-Mapper文件中，不同的-XML-映射文件，id-是否可以重复？"><a href="#Mybatis-的-XML-Mapper文件中，不同的-XML-映射文件，id-是否可以重复？" class="headerlink" title="Mybatis 的 XML Mapper文件中，不同的 XML 映射文件，id 是否可以重复？"></a>Mybatis 的 XML Mapper文件中，不同的 XML 映射文件，id 是否可以重复？</h2><p>不同的 XML Mapper 文件，如果配置了 <code>"namespace"</code> ，那么 id 可以重复；如果没有配置 <code>"namespace"</code> ，那么 id 不能重复。毕竟<code>"namespace"</code> 不是必须的，只是最佳实践而已。</p>
<p>原因就是，<code>namespace + id</code> 是作为 <code>Map&lt;String, MappedStatement&gt;</code> 的 key 使用的。如果没有 <code>"namespace"</code>，就剩下 id ，那么 id 重复会导致数据互相覆盖。如果有了 <code>"namespace"</code>，自然 id 就可以重复，<code>"namespace"</code>不同，<code>namespace + id</code> 自然也就不同。</p>
<h2 id="如何获取自动生成的-主-键值"><a href="#如何获取自动生成的-主-键值" class="headerlink" title="如何获取自动生成的(主)键值?"></a>如何获取自动生成的(主)键值?</h2><p>不同的数据库，获取自动生成的(主)键值的方式是不同的。</p>
<p>MySQL 有两种方式，但是<strong>自增主键</strong>，代码如下：</p>
<figure class="highlight sql"><table><tbody><tr><td class="code"><pre><span class="line">// 方式一，使用 useGeneratedKeys + keyProperty 属性</span><br><span class="line">&lt;insert id="insert" parameterType="Person" useGeneratedKeys="true" keyProperty="id"&gt;</span><br><span class="line">    <span class="keyword">INSERT</span> <span class="keyword">INTO</span> person(<span class="keyword">name</span>, pswd)</span><br><span class="line">    <span class="keyword">VALUE</span> (#{<span class="keyword">name</span>}, #{pswd})</span><br><span class="line">&lt;/<span class="keyword">insert</span>&gt;</span><br><span class="line">    </span><br><span class="line">// 方式二，使用 <span class="string">`&lt;selectKey /&gt;`</span> 标签</span><br><span class="line">&lt;<span class="keyword">insert</span> <span class="keyword">id</span>=<span class="string">"insert"</span> parameterType=<span class="string">"Person"</span> useGeneratedKeys=<span class="string">"true"</span> keyProperty=<span class="string">"id"</span>&gt;</span><br><span class="line">    &lt;selectKey keyProperty=<span class="string">"id"</span> resultType=<span class="string">"long"</span> <span class="keyword">order</span>=<span class="string">"AFTER"</span>&gt;</span><br><span class="line">        <span class="keyword">SELECT</span> <span class="keyword">LAST_INSERT_ID</span>()</span><br><span class="line">    &lt;/selectKey&gt;</span><br><span class="line">        </span><br><span class="line">    <span class="keyword">INSERT</span> <span class="keyword">INTO</span> person(<span class="keyword">name</span>, pswd)</span><br><span class="line">    <span class="keyword">VALUE</span> (#{<span class="keyword">name</span>}, #{pswd})</span><br><span class="line">&lt;/<span class="keyword">insert</span>&gt;</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li>其中，<strong>方式一</strong>较为常用。</li>
</ul>
<hr>
<p>Oracle 有两种方式，<strong>序列</strong>和<strong>触发器</strong>。因为艿艿自己不了解 Oracle ，所以问了银行的朋友，他们是使用<strong>序列</strong>。而基于<strong>序列</strong>，根据 <code>&lt;selectKey /&gt;</code> 执行的时机，也有两种方式，代码如下：</p>
<figure class="highlight sql"><table><tbody><tr><td class="code"><pre><span class="line">// 这个是创建表的自增序列</span><br><span class="line"><span class="keyword">CREATE</span> <span class="keyword">SEQUENCE</span> student_sequence</span><br><span class="line"><span class="keyword">INCREMENT</span> <span class="keyword">BY</span> <span class="number">1</span></span><br><span class="line"><span class="keyword">NOMAXVALUE</span></span><br><span class="line"><span class="keyword">NOCYCLE</span></span><br><span class="line"><span class="keyword">CACHE</span> <span class="number">10</span>;</span><br><span class="line"></span><br><span class="line">// 方式一，使用 `&lt;selectKey /&gt;` 标签 + BEFORE</span><br><span class="line">&lt;insert id="add" parameterType="Student"&gt;</span><br><span class="line">　　&lt;selectKey keyProperty="student_id" resultType="int" order="BEFORE"&gt;</span><br><span class="line">      <span class="keyword">select</span> student_sequence.nextval <span class="keyword">FROM</span> dual</span><br><span class="line">    &lt;/selectKey&gt;</span><br><span class="line">    </span><br><span class="line">     <span class="keyword">INSERT</span> <span class="keyword">INTO</span> student(student_id, student_name, student_age)</span><br><span class="line">     <span class="keyword">VALUES</span> (#{student_id},#{student_name},#{student_age})</span><br><span class="line">&lt;/<span class="keyword">insert</span>&gt;</span><br><span class="line"></span><br><span class="line">// 方式二，使用 <span class="string">`&lt;selectKey /&gt;`</span> 标签 + <span class="keyword">AFTER</span></span><br><span class="line">&lt;<span class="keyword">insert</span> <span class="keyword">id</span>=<span class="string">"save"</span> parameterType=<span class="string">"com.threeti.to.ZoneTO"</span> &gt;</span><br><span class="line">    &lt;selectKey resultType=<span class="string">"java.lang.Long"</span> keyProperty=<span class="string">"id"</span> <span class="keyword">order</span>=<span class="string">"AFTER"</span> &gt;</span><br><span class="line">      <span class="keyword">SELECT</span> SEQ_ZONE.CURRVAL <span class="keyword">AS</span> <span class="keyword">id</span> <span class="keyword">FROM</span> dual</span><br><span class="line">    &lt;/selectKey&gt;</span><br><span class="line">    </span><br><span class="line">    <span class="keyword">INSERT</span> <span class="keyword">INTO</span> TBL_ZONE (<span class="keyword">ID</span>, <span class="keyword">NAME</span> ) </span><br><span class="line">    <span class="keyword">VALUES</span> (SEQ_ZONE.NEXTVAL, #{<span class="keyword">name</span>,jdbcType=<span class="built_in">VARCHAR</span>})</span><br><span class="line">&lt;/<span class="keyword">insert</span>&gt;</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li>他们使用第一种方式，没有具体原因，可能就没什么讲究吧。嘿嘿。</li>
</ul>
<p>至于为什么不用<strong>触发器</strong>呢？朋友描述如下：</p>
<blockquote>
<p>朋友：触发器不行啊，我们这边原来也有触发器，一有数据更改就会有问题了呀<br>艿艿：数据更改指的是？<br>朋友：就改线上某几条数据<br>艿艿：噢噢。手动改是吧？<br>朋友：不行~</p>
</blockquote>
<hr>
<p>当然，数据库还有 SQLServer、PostgreSQL、DB2、H2 等等，具体的方式，胖友自己 Google 下噢。</p>
<p>关于如何获取自动生成的(主)键值的<strong>原理</strong>，可以看看 <a href="http://svip.iocoder.cn/MyBatis/executor-3/">《精尽 MyBatis 源码分析 —— SQL 执行（三）之 KeyGenerator》</a> 。</p>
<h2 id="Mybatis-执行批量插入，能返回数据库主键列表吗？"><a href="#Mybatis-执行批量插入，能返回数据库主键列表吗？" class="headerlink" title="Mybatis 执行批量插入，能返回数据库主键列表吗？"></a>Mybatis 执行批量插入，能返回数据库主键列表吗？</h2><p>能，JDBC 都能做，Mybatis 当然也能做。</p>
<h2 id="在-Mapper-中如何传递多个参数"><a href="#在-Mapper-中如何传递多个参数" class="headerlink" title="在 Mapper 中如何传递多个参数?"></a>在 Mapper 中如何传递多个参数?</h2><p>第一种，使用 Map 集合，装载多个参数进行传递。代码如下：</p>
<figure class="highlight"><table><tbody><tr><td class="code"><pre><span class="line"><span class="comment">// 调用方法</span></span><br><span class="line">Map&lt;String, Object&gt; map = <span class="keyword">new</span> HashMap();</span><br><span class="line">map.put(<span class="string">"start"</span>, start);</span><br><span class="line">map.put(<span class="string">"end"</span>, end);</span><br><span class="line"><span class="keyword">return</span> studentMapper.selectStudents(map);</span><br><span class="line"></span><br><span class="line"><span class="comment">// Mapper 接口</span></span><br><span class="line"><span class="function">List&lt;Student&gt; <span class="title">selectStudents</span><span class="params">(Map&lt;String, Object&gt; map)</span></span>;</span><br><span class="line"></span><br><span class="line"><span class="comment">// Mapper XML 代码</span></span><br><span class="line">&lt;select id=<span class="string">"selectStudents"</span> parameterType=<span class="string">"Map"</span> resultType=<span class="string">"Student"</span>&gt;</span><br><span class="line">    SELECT * </span><br><span class="line">    FROM students </span><br><span class="line">    LIMIT #{start}, #{end}</span><br><span class="line">&lt;/select&gt;</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li>显然，这不是一种优雅的方式。</li>
</ul>
<hr>
<p>第二种，保持传递多个参数，使用 <code>@Param</code> 注解。代码如下：</p>
<figure class="highlight"><table><tbody><tr><td class="code"><pre><span class="line"><span class="comment">// 调用方法</span></span><br><span class="line"><span class="keyword">return</span> studentMapper.selectStudents(<span class="number">0</span>, <span class="number">10</span>);</span><br><span class="line"></span><br><span class="line"><span class="comment">// Mapper 接口</span></span><br><span class="line"><span class="function">List&lt;Student&gt; <span class="title">selectStudents</span><span class="params">(@Param(<span class="string">"start"</span>)</span> Integer start, @<span class="title">Param</span><span class="params">(<span class="string">"end"</span>)</span> Integer end)</span>;</span><br><span class="line"></span><br><span class="line"><span class="comment">// Mapper XML 代码</span></span><br><span class="line">&lt;select id=<span class="string">"selectStudents"</span> resultType=<span class="string">"Student"</span>&gt;</span><br><span class="line">    SELECT * </span><br><span class="line">    FROM students </span><br><span class="line">    LIMIT #{start}, #{end}</span><br><span class="line">&lt;/select&gt;</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li>推荐使用这种方式。</li>
</ul>
<hr>
<p>第三种，保持传递多个参数，不使用 <code>@Param</code> 注解。代码如下：</p>
<figure class="highlight"><table><tbody><tr><td class="code"><pre><span class="line"><span class="comment">// 调用方法</span></span><br><span class="line"><span class="keyword">return</span> studentMapper.selectStudents(<span class="number">0</span>, <span class="number">10</span>);</span><br><span class="line"></span><br><span class="line"><span class="comment">// Mapper 接口</span></span><br><span class="line"><span class="function">List&lt;Student&gt; <span class="title">selectStudents</span><span class="params">(Integer start, Integer end)</span></span>;</span><br><span class="line"></span><br><span class="line"><span class="comment">// Mapper XML 代码</span></span><br><span class="line">&lt;select id=<span class="string">"selectStudents"</span> resultType=<span class="string">"Student"</span>&gt;</span><br><span class="line">    SELECT * </span><br><span class="line">    FROM students </span><br><span class="line">    LIMIT #{param1}, #{param2}</span><br><span class="line">&lt;/select&gt;</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li>其中，按照参数在方法方法中的位置，从 1 开始，逐个为 <code>#{param1}</code>、<code>#{param2}</code>、<code>#{param3}</code> 不断向下。</li>
</ul>
<h2 id="Mybatis-是否可以映射-Enum-枚举类？"><a href="#Mybatis-是否可以映射-Enum-枚举类？" class="headerlink" title="Mybatis 是否可以映射 Enum 枚举类？"></a>Mybatis 是否可以映射 Enum 枚举类？</h2><p>Mybatis 可以映射枚举类，对应的实现类为 EnumTypeHandler 或 EnumOrdinalTypeHandler 。</p>
<ul>
<li>EnumTypeHandler ，基于 <code>Enum.name</code> 属性( String )。<strong>默认</strong>。</li>
<li>EnumOrdinalTypeHandler ，基于 <code>Enum.ordinal</code> 属性( <code>int</code> )。可通过 <code>&lt;setting name="defaultEnumTypeHandler" value="EnumOrdinalTypeHandler" /&gt;</code> 来设置。</li>
</ul>
<p>😈 当然，实际开发场景，我们很少使用 Enum 类型，更加的方式是，代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="keyword">public</span> <span class="class"><span class="keyword">class</span> <span class="title">Dog</span> </span>{</span><br><span class="line"></span><br><span class="line">    <span class="keyword">public</span> <span class="keyword">static</span> <span class="keyword">final</span> <span class="keyword">int</span> STATUS_GOOD = <span class="number">1</span>;</span><br><span class="line">    <span class="keyword">public</span> <span class="keyword">static</span> <span class="keyword">final</span> <span class="keyword">int</span> STATUS_BETTER = <span class="number">2</span>;</span><br><span class="line">    <span class="keyword">public</span> <span class="keyword">static</span> <span class="keyword">final</span> <span class="keyword">int</span> STATUS_BEST = <span class="number">3</span>；</span><br><span class="line">    </span><br><span class="line">    <span class="keyword">private</span> <span class="keyword">int</span> status;</span><br><span class="line">    </span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<hr>
<p>并且，不单可以映射枚举类，Mybatis 可以映射任何对象到表的一列上。映射方式为自定义一个 TypeHandler 类，实现 TypeHandler 的<code>#setParameter(...)</code> 和 <code>#getResult(...)</code> 接口方法。</p>
<p>TypeHandler 有两个作用：</p>
<ul>
<li>一是，完成从 javaType 至 jdbcType 的转换。</li>
<li>二是，完成 jdbcType 至 javaType 的转换。</li>
</ul>
<p>具体体现为 <code>#setParameter(...)</code> 和 <code>#getResult(..)</code> 两个方法，分别代表设置 SQL 问号占位符参数和获取列查询结果。</p>
<p>关于 TypeHandler 的<strong>原理</strong>，可以看看 <a href="http://svip.iocoder.cn/MyBatis/type-package/">《精尽 MyBatis 源码分析 —— 类型模块》</a> 。</p>
<h2 id="Mybatis-都有哪些-Executor-执行器？它们之间的区别是什么？"><a href="#Mybatis-都有哪些-Executor-执行器？它们之间的区别是什么？" class="headerlink" title="Mybatis 都有哪些 Executor 执行器？它们之间的区别是什么？"></a>Mybatis 都有哪些 Executor 执行器？它们之间的区别是什么？</h2><p>Mybatis 有四种 Executor 执行器，分别是 SimpleExecutor、ReuseExecutor、BatchExecutor、CachingExecutor 。</p>
<ul>
<li>SimpleExecutor ：每执行一次 update 或 select 操作，就创建一个 Statement 对象，用完立刻关闭 Statement 对象。</li>
<li>ReuseExecutor ：执行 update 或 select 操作，以 SQL 作为key 查找<strong>缓存</strong>的 Statement 对象，存在就使用，不存在就创建；用完后，不关闭 Statement 对象，而是放置于缓存 <code>Map&lt;String, Statement&gt;</code> 内，供下一次使用。简言之，就是重复使用 Statement 对象。</li>
<li>BatchExecutor ：执行 update 操作（没有 select 操作，因为 JDBC 批处理不支持 select 操作），将所有 SQL 都添加到批处理中（通过 addBatch 方法），等待统一执行（使用 executeBatch 方法）。它缓存了多个 Statement 对象，每个 Statement 对象都是调用 addBatch 方法完毕后，等待一次执行 executeBatch 批处理。<strong>实际上，整个过程与 JDBC 批处理是相同</strong>。</li>
<li>CachingExecutor ：在上述的三个执行器之上，增加<strong>二级缓存</strong>的功能。</li>
</ul>
<hr>
<p>通过设置 <code>&lt;setting name="defaultExecutorType" value=""&gt;</code> 的 <code>"value"</code> 属性，可传入 SIMPLE、REUSE、BATCH 三个值，分别使用 SimpleExecutor、ReuseExecutor、BatchExecutor 执行器。</p>
<p>通过设置 <code>&lt;setting name="cacheEnabled" value=""</code> 的 <code>"value"</code> 属性为 <code>true</code> 时，创建 CachingExecutor 执行器。</p>
<hr>
<p>这块的源码解析，可见 <a href="http://svip.iocoder.cn/MyBatis/executor-1">《精尽 MyBatis 源码分析 —— SQL 执行（一）之 Executor》</a> 。</p>
<h2 id="MyBatis-如何执行批量插入"><a href="#MyBatis-如何执行批量插入" class="headerlink" title="MyBatis 如何执行批量插入?"></a>MyBatis 如何执行批量插入?</h2><p>首先，在 Mapper XML 编写一个简单的 Insert 语句。代码如下：</p>
<figure class="highlight xml"><table><tbody><tr><td class="code"><pre><span class="line"><span class="tag">&lt;<span class="name">insert</span> <span class="attr">id</span>=<span class="string">"insertUser"</span> <span class="attr">parameterType</span>=<span class="string">"String"</span>&gt;</span> </span><br><span class="line">    INSERT INTO users(name) </span><br><span class="line">    VALUES (#{value}) </span><br><span class="line"><span class="tag">&lt;/<span class="name">insert</span>&gt;</span></span><br></pre></td></tr></tbody></table></figure>
<p>然后，然后在对应的 Mapper 接口中，编写映射的方法。代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="keyword">public</span> <span class="class"><span class="keyword">interface</span> <span class="title">UserMapper</span> </span>{</span><br><span class="line">    </span><br><span class="line">    <span class="function"><span class="keyword">void</span> <span class="title">insertUser</span><span class="params">(@Param(<span class="string">"name"</span>)</span> String name)</span>;</span><br><span class="line"></span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<p>最后，调用该 Mapper 接口方法。代码如下：</p>
<figure class="highlight java"><table><tbody><tr><td class="code"><pre><span class="line"><span class="keyword">private</span> <span class="keyword">static</span> SqlSessionFactory sqlSessionFactory;</span><br><span class="line"></span><br><span class="line"><span class="meta">@Test</span></span><br><span class="line"><span class="function"><span class="keyword">public</span> <span class="keyword">void</span> <span class="title">testBatch</span><span class="params">()</span> </span>{</span><br><span class="line">    <span class="comment">// 创建要插入的用户的名字的数组</span></span><br><span class="line">    List&lt;String&gt; names = <span class="keyword">new</span> ArrayList&lt;&gt;();</span><br><span class="line">    names.add(<span class="string">"占小狼"</span>);</span><br><span class="line">    names.add(<span class="string">"朱小厮"</span>);</span><br><span class="line">    names.add(<span class="string">"徐妈"</span>);</span><br><span class="line">    names.add(<span class="string">"飞哥"</span>);</span><br><span class="line"></span><br><span class="line">    <span class="comment">// 获得执行器类型为 Batch 的 SqlSession 对象，并且 autoCommit = false ，禁止事务自动提交</span></span><br><span class="line">    <span class="keyword">try</span> (SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH, <span class="keyword">false</span>)) {</span><br><span class="line">        <span class="comment">// 获得 Mapper 对象</span></span><br><span class="line">        UserMapper mapper = session.getMapper(UserMapper.class);</span><br><span class="line">        <span class="comment">// 循环插入</span></span><br><span class="line">        <span class="keyword">for</span> (String name : names) {</span><br><span class="line">            mapper.insertUser(name);</span><br><span class="line">        }</span><br><span class="line">        <span class="comment">// 提交批量操作</span></span><br><span class="line">        session.commit();</span><br><span class="line">    }</span><br><span class="line">}</span><br></pre></td></tr></tbody></table></figure>
<p>代码比较简单，胖友仔细看看。当然，还有另一种方式，代码如下：</p>
<figure class="highlight sql"><table><tbody><tr><td class="code"><pre><span class="line"><span class="keyword">INSERT</span> <span class="keyword">INTO</span> [表名]([列名],[列名]) </span><br><span class="line"><span class="keyword">VALUES</span></span><br><span class="line">([列值],[列值])),</span><br><span class="line">([列值],[列值])),</span><br><span class="line">([列值],[列值]));</span><br></pre></td></tr></tbody></table></figure>
<ul>
<li>对于这种方式，需要保证单条 SQL 不超过语句的最大限制 <code>max_allowed_packet</code> 大小，默认为 1 M 。</li>
</ul>
<p>这两种方式的性能对比，可以看看 <a href="https://www.jianshu.com/p/cce617be9f9e" rel="external nofollow noopener noreferrer" target="_blank">《[实验]mybatis批量插入方式的比较》</a> 。</p>
<h2 id="介绍-MyBatis-的一级缓存和二级缓存的概念和实现原理？"><a href="#介绍-MyBatis-的一级缓存和二级缓存的概念和实现原理？" class="headerlink" title="介绍 MyBatis 的一级缓存和二级缓存的概念和实现原理？"></a>介绍 MyBatis 的一级缓存和二级缓存的概念和实现原理？</h2><p>内容有些长，直接参见 <a href="https://tech.meituan.com/mybatis_cache.html" rel="external nofollow noopener noreferrer" target="_blank">《聊聊 MyBatis 缓存机制》</a> 一文。</p>
<hr>
<p>这块的源码解析，可见 <a href="http://svip.iocoder.cn/MyBatis/cache-package">《精尽 MyBatis 源码分析 —— 缓存模块》</a> 。</p>
<h2 id="Mybatis-是否支持延迟加载？如果支持，它的实现原理是什么？"><a href="#Mybatis-是否支持延迟加载？如果支持，它的实现原理是什么？" class="headerlink" title="Mybatis 是否支持延迟加载？如果支持，它的实现原理是什么？"></a>Mybatis 是否支持延迟加载？如果支持，它的实现原理是什么？</h2><p>Mybatis 仅支持 association 关联对象和 collection 关联集合对象的延迟加载。其中，association 指的就是<strong>一对一</strong>，collection 指的就是<strong>一对多查询</strong>。</p>
<p>在 Mybatis 配置文件中，可以配置 <code>&lt;setting name="lazyLoadingEnabled" value="true" /&gt;</code> 来启用延迟加载的功能。默认情况下，延迟加载的功能是<strong>关闭</strong>的。</p>
<hr>
<p>它的原理是，使用 CGLIB 或 Javassist( 默认 ) 创建目标对象的代理对象。当调用代理对象的延迟加载属性的 getting 方法时，进入拦截器方法。比如调用 <code>a.getB().getName()</code> 方法，进入拦截器的 <code>invoke(...)</code> 方法，发现 <code>a.getB()</code> 需要延迟加载时，那么就会单独发送事先保存好的查询关联 B 对象的 SQL ，把 B 查询上来，然后调用<code>a.setB(b)</code> 方法，于是 <code>a</code> 对象 <code>b</code> 属性就有值了，接着完成<code>a.getB().getName()</code> 方法的调用。这就是延迟加载的基本原理。</p>
<p>当然了，不光是 Mybatis，几乎所有的包括 Hibernate 在内，支持延迟加载的原理都是一样的。</p>
<hr>
<p>这块的源码解析，可见 <a href="http://svip.iocoder.cn/MyBatis/executor-5">《 精尽 MyBatis 源码分析 —— SQL 执行（五）之延迟加载》</a> 文章。</p>
<h2 id="Mybatis-能否执行一对一、一对多的关联查询吗？都有哪些实现方式，以及它们之间的区别。"><a href="#Mybatis-能否执行一对一、一对多的关联查询吗？都有哪些实现方式，以及它们之间的区别。" class="headerlink" title="Mybatis 能否执行一对一、一对多的关联查询吗？都有哪些实现方式，以及它们之间的区别。"></a>Mybatis 能否执行一对一、一对多的关联查询吗？都有哪些实现方式，以及它们之间的区别。</h2><blockquote>
<p>艿艿：这道题有点难度。理解倒是好理解，主要那块源码的实现，艿艿看的有点懵逼。大体的意思是懂的，但是一些细节没扣完。</p>
</blockquote>
<p>能，Mybatis 不仅可以执行一对一、一对多的关联查询，还可以执行多对一，多对多的关联查询。</p>
<blockquote>
<p>艿艿：不过貌似，我自己实际开发中，还是比较喜欢自己去查询和拼接映射的数据。😈</p>
</blockquote>
<ul>
<li>多对一查询，其实就是一对一查询，只需要把 <code>selectOne(...)</code> 修改为 <code>selectList(...)</code> 即可。案例可见 <a href="https://blog.csdn.net/xzm_rainbow/article/details/15336959" rel="external nofollow noopener noreferrer" target="_blank">《MyBatis：多对一表关系详解》</a> 。</li>
<li>多对多查询，其实就是一对多查询，只需要把 <code>#selectOne(...)</code> 修改为 <code>selectList(...)</code> 即可。案例可见 <a href="https://blog.csdn.net/eson_15/article/details/51655188" rel="external nofollow noopener noreferrer" target="_blank">《【MyBatis学习10】高级映射之多对多查询》</a> 。</li>
</ul>
<hr>
<p>关联对象查询，有两种实现方式：</p>
<blockquote>
<p>艿艿：所有的技术方案，即会有好处，又会有坏处。很难出现，一个完美的银弹方案。</p>
</blockquote>
<ul>
<li>一种是单独发送一个 SQL 去查询关联对象，赋给主对象，然后返回主对象。好处是多条 SQL 分开，相对简单，坏处是发起的 SQL 可能会比较多。</li>
<li>另一种是使用嵌套查询，嵌套查询的含义为使用 <code>join</code> 查询，一部分列是 A 对象的属性值，另外一部分列是关联对象 B 的属性值。好处是只发一个 SQL 查询，就可以把主对象和其关联对象查出来，坏处是 SQL 可能比较复杂。</li>
</ul>
<p>那么问题来了，<code>join</code> 查询出来 100 条记录，如何确定主对象是 5 个，而不是 100 个呢？其去重复的原理是 <code>&lt;resultMap&gt;</code> 标签内的<code>&lt;id&gt;</code> 子标签，指定了唯一确定一条记录的 <code>id</code> 列。Mybatis 会根据<code>&lt;id&gt;</code> 列值来完成 100 条记录的去重复功能，<code>&lt;id&gt;</code> 可以有多个，代表了联合主键的语意。</p>
<p>同样主对象的关联对象，也是根据这个原理去重复的。尽管一般情况下，只有主对象会有重复记录，关联对象一般不会重复。例如：下面 <code>join</code> 查询出来6条记录，一、二列是 Teacher 对象列，第三列为 Student 对象列。Mybatis 去重复处理后，结果为 1 个老师和 6 个学生，而不是 6 个老师和 6 个学生。</p>
<table>
<thead>
<tr>
<th>t_id</th>
<th>t_name</th>
<th>s_id</th>
</tr>
</thead>
<tbody>
<tr>
<td>1</td>
<td>teacher</td>
<td>38</td>
</tr>
<tr>
<td>1</td>
<td>teacher</td>
<td>39</td>
</tr>
<tr>
<td>1</td>
<td>teacher</td>
<td>40</td>
</tr>
<tr>
<td>1</td>
<td>teacher</td>
<td>41</td>
</tr>
<tr>
<td>1</td>
<td>teacher</td>
<td>42</td>
</tr>
<tr>
<td>1</td>
<td>teacher</td>
<td>43</td>
</tr>
</tbody>
</table>
<h2 id="简述-Mybatis-的插件运行原理？以及如何编写一个插件？"><a href="#简述-Mybatis-的插件运行原理？以及如何编写一个插件？" class="headerlink" title="简述 Mybatis 的插件运行原理？以及如何编写一个插件？"></a>简述 Mybatis 的插件运行原理？以及如何编写一个插件？</h2><p>Mybatis 仅可以编写针对 ParameterHandler、ResultSetHandler、StatementHandler、Executor 这 4 种接口的插件。</p>
<p>Mybatis 使用 JDK 的动态代理，为需要拦截的接口生成代理对象以实现接口方法拦截功能，每当执行这 4 种接口对象的方法时，就会进入拦截方法，具体就是 InvocationHandler 的 <code>#invoke(...)</code>方法。当然，只会拦截那些你指定需要拦截的方法。</p>
<hr>
<p>编写一个 MyBatis 插件的步骤如下：</p>
<ol>
<li>首先，实现 Mybatis 的 Interceptor 接口，并实现 <code>#intercept(...)</code> 方法。</li>
<li>然后，在给插件编写注解，指定要拦截哪一个接口的哪些方法即可</li>
<li>最后，在配置文件中配置你编写的插件。</li>
</ol>
<p>具体的，可以参考 <a href="http://www.mybatis.org/mybatis-3/zh/configuration.html#plugins" rel="external nofollow noopener noreferrer" target="_blank">《MyBatis 官方文档 —— 插件》</a> 。</p>
<hr>
<p>插件的详细解析，可以看看 <a href="http://svip.iocoder.cn/MyBatis/plugin-1">《精尽 MyBatis 源码分析 —— 插件体系（一）之原理》</a> 。</p>
<h2 id="Mybatis-是如何进行分页的？分页插件的原理是什么？"><a href="#Mybatis-是如何进行分页的？分页插件的原理是什么？" class="headerlink" title="Mybatis 是如何进行分页的？分页插件的原理是什么？"></a>Mybatis 是如何进行分页的？分页插件的原理是什么？</h2><p>Mybatis 使用 RowBounds 对象进行分页，它是针对 ResultSet 结果集执行的<strong>内存分页</strong>，而非<strong>数据库分页</strong>。</p>
<p>所以，实际场景下，不适合直接使用 MyBatis 原有的 RowBounds 对象进行分页。而是使用如下两种方案：</p>
<ul>
<li>在 SQL 内直接书写带有数据库分页的参数来完成数据库分页功能</li>
<li>也可以使用分页插件来完成数据库分页。</li>
</ul>
<p>这两者都是基于数据库分页，差别在于前者是工程师<strong>手动</strong>编写分页条件，后者是插件<strong>自动</strong>添加分页条件。</p>
<hr>
<p>分页插件的基本原理是使用 Mybatis 提供的插件接口，实现自定义分页插件。在插件的拦截方法内，拦截待执行的 SQL ，然后重写 SQL ，根据dialect 方言，添加对应的物理分页语句和物理分页参数。</p>
<p>举例：<code>SELECT * FROM student</code> ，拦截 SQL 后重写为：<code>select * FROM student LIMI 0，10</code> 。</p>
<p>目前市面上目前使用比较广泛的 MyBatis 分页插件有：</p>
<ul>
<li><a href="https://github.com/pagehelper/Mybatis-PageHelper" rel="external nofollow noopener noreferrer" target="_blank">Mybatis-PageHelper</a></li>
<li><a href="https://github.com/baomidou/mybatis-plus" rel="external nofollow noopener noreferrer" target="_blank">MyBatis-Plus</a></li>
</ul>
<p>从现在看来，<a href="https://github.com/baomidou/mybatis-plus" rel="external nofollow noopener noreferrer" target="_blank">MyBatis-Plus</a> 逐步使用的更加广泛。</p>
<p>关于 MyBatis 分页插件的原理深入，可以看看 <a href="http://svip.iocoder.cn/MyBatis/plugin-2">《精尽 MyBatis 源码分析 —— 插件体系（二）之 PageHelper》</a> 。</p>
<h2 id="MyBatis-与-Hibernate-有哪些不同？"><a href="#MyBatis-与-Hibernate-有哪些不同？" class="headerlink" title="MyBatis 与 Hibernate 有哪些不同？"></a>MyBatis 与 Hibernate 有哪些不同？</h2><p>Mybatis 和 Hibernate 不同，它<strong>不完全是</strong>一个 ORM 框架，因为MyBatis 需要程序员自己编写 SQL 语句。不过 MyBatis 可以通过 XML 或注解方式灵活配置要运行的 SQL 语句，并将 Java 对象和 SQL 语句映射生成最终执行的 SQL ，最后将 SQL 执行的结果再映射生成 Java 对象。</p>
<p>Mybatis 学习门槛低，简单易学，程序员直接编写原生态 SQL ，可严格控制 SQL 执行性能，灵活度高。但是灵活的前提是 MyBatis 无法做到数据库无关性，如果需要实现支持多种数据库的软件则需要自定义多套 SQL 映射文件，工作量大。</p>
<p>Hibernate 对象/关系映射能力强，数据库无关性好。如果用 Hibernate 开发可以节省很多代码，提高效率。但是 Hibernate 的缺点是学习门槛高，要精通门槛更高，而且怎么设计 O/R 映射，在性能和对象模型之间如何权衡，以及怎样用好 Hibernate 需要具有很强的经验和能力才行。 </p>
<p>总之，按照用户的需求在有限的资源环境下只要能做出维护性、扩展性良好的软件架构都是好架构，所以框架只有适合才是最好。简单总结如下：</p>
<ul>
<li>Hibernate 属于全自动 ORM 映射工具，使用 Hibernate 查询关联对象或者关联集合对象时，可以根据对象关系模型直接获取。</li>
<li>Mybatis 属于半自动 ORM 映射工具，在查询关联对象或关联集合对象时，需要手动编写 SQL 来完成。</li>
</ul>
<p>另外，在 <a href="https://www.jianshu.com/p/96171e647885" rel="external nofollow noopener noreferrer" target="_blank">《浅析 Mybatis 与 Hibernate 的区别与用途》</a> 文章，也是写的非常不错的。</p>
<p>当然，实际上，MyBatis 也可以搭配自动生成代码的工具，提升开发效率，还可以使用 <a href="http://mp.baomidou.com/" rel="external nofollow noopener noreferrer" target="_blank">MyBatis-Plus</a> 框架，已经内置常用的 SQL 操作，也是非常不错的。</p>
<h2 id="JDBC-编程有哪些不足之处，MyBatis是如何解决这些问题的？"><a href="#JDBC-编程有哪些不足之处，MyBatis是如何解决这些问题的？" class="headerlink" title="JDBC 编程有哪些不足之处，MyBatis是如何解决这些问题的？"></a>JDBC 编程有哪些不足之处，MyBatis是如何解决这些问题的？</h2><p>问题一：SQL 语句写在代码中造成代码不易维护，且代码会比较混乱。</p>
<p>解决方式：将 SQL 语句配置在 Mapper XML 文件中，与 Java 代码分离。</p>
<hr>
<p>问题二：根据参数不同，拼接不同的 SQL 语句非常麻烦。例如 SQL 语句的 WHERE 条件不一定，可能多也可能少，占位符需要和参数一一对应。 </p>
<p>解决方式：MyBatis 提供 <code>&lt;where /&gt;</code>、<code>&lt;if /&gt;</code> 等等动态语句所需要的标签，并支持 OGNL 表达式，简化了动态 SQL 拼接的代码，提升了开发效率。</p>
<hr>
<p>问题三，对结果集解析麻烦，SQL 变化可能导致解析代码变化，且解析前需要遍历。</p>
<p>解决方式：Mybatis 自动将 SQL 执行结果映射成 Java 对象。</p>
<hr>
<p>问题四，数据库链接创建、释放频繁造成系统资源浪费从而影响系统性能，如果使用数据库链接池可解决此问题。</p>
<p>解决方式：在 <code>mybatis-config.xml</code> 中，配置数据链接池，使用连接池管理数据库链接。</p>
<p>😈 当然，即使不使用 MyBatis ，也可以使用数据库连接池。<br>另外，MyBatis 默认提供了数据库连接池的实现，只是说，因为其它开源的数据库连接池性能更好，所以一般很少使用 MyBatis 自带的连接池实现。</p>
<h2 id="Mybatis-比-IBatis-比较大的几个改进是什么？"><a href="#Mybatis-比-IBatis-比较大的几个改进是什么？" class="headerlink" title="Mybatis 比 IBatis 比较大的几个改进是什么？"></a>Mybatis 比 IBatis 比较大的几个改进是什么？</h2><blockquote>
<p>这是一个选择性了解的问题，因为可能现在很多面试官，都没用过 IBatis 框架。</p>
</blockquote>
<ol>
<li>有接口绑定，包括注解绑定 SQL 和 XML 绑定 SQL 。</li>
<li>动态 SQL 由原来的节点配置变成 OGNL 表达式。</li>
<li>在一对一或一对多的时候，引进了 <code>association</code> ，在一对多的时候，引入了 <code>collection</code>节点，不过都是在 <code>&lt;resultMap /&gt;</code> 里面配置。</li>
</ol>
<h2 id="Mybatis-映射文件中，如果-A-标签通过-include-引用了B标签的内容，请问，B-标签能否定义在-A-标签的后面，还是说必须定义在A标签的前面？"><a href="#Mybatis-映射文件中，如果-A-标签通过-include-引用了B标签的内容，请问，B-标签能否定义在-A-标签的后面，还是说必须定义在A标签的前面？" class="headerlink" title="Mybatis 映射文件中，如果 A 标签通过 include 引用了B标签的内容，请问，B 标签能否定义在 A 标签的后面，还是说必须定义在A标签的前面？"></a>Mybatis 映射文件中，如果 A 标签通过 include 引用了B标签的内容，请问，B 标签能否定义在 A 标签的后面，还是说必须定义在A标签的前面？</h2><blockquote>
<p>老艿艿：这道题目，已经和源码实现，有点关系了。</p>
</blockquote>
<p>虽然 Mybatis 解析 XML 映射文件是<strong>按照顺序</strong>解析的。但是，被引用的 B 标签依然可以定义在任何地方，Mybatis 都可以正确识别。<strong>也就是说，无需按照顺序，进行定义</strong>。</p>
<p>原理是，Mybatis 解析 A 标签，发现 A 标签引用了 B 标签，但是 B 标签尚未解析到，尚不存在，此时，Mybatis 会将 A 标签标记为<strong>未解析状态</strong>。然后，继续解析余下的标签，包含 B 标签，待所有标签解析完毕，Mybatis 会重新解析那些被标记为未解析的标签，此时再解析A标签时，B 标签已经存在，A 标签也就可以正常解析完成了。</p>
<p>可能有一些绕，胖友可以看看 <a href="http://svip.iocoder.cn/MyBatis/builder-package-1">《精尽 MyBatis 源码解析 —— MyBatis 初始化（一）之加载 mybatis-config》</a> 。</p>
<p>此处，我们在引申一个问题，Spring IOC 中，存在互相依赖的 Bean 对象，该如何解决呢？答案见 <a href="http://svip.iocoder.cn/Spring/IoC-get-Bean-createBean-5/">《【死磕 Spring】—— IoC 之加载 Bean：创建 Bean（五）之循环依赖处理》</a> 。</p>
<h2 id="简述-Mybatis-的-XML-映射文件和-Mybatis-内部数据结构之间的映射关系？"><a href="#简述-Mybatis-的-XML-映射文件和-Mybatis-内部数据结构之间的映射关系？" class="headerlink" title="简述 Mybatis 的 XML 映射文件和 Mybatis 内部数据结构之间的映射关系？"></a>简述 Mybatis 的 XML 映射文件和 Mybatis 内部数据结构之间的映射关系？</h2><blockquote>
<p>老艿艿：这道题目，已经和源码实现，有点关系了。</p>
</blockquote>
<p>Mybatis 将所有 XML 配置信息都封装到 All-In-One 重量级对象Configuration内部。</p>
<p>在 XML Mapper 文件中：</p>
<ul>
<li><code>&lt;parameterMap&gt;</code> 标签，会被解析为 ParameterMap 对象，其每个子元素会被解析为 ParameterMapping 对象。</li>
<li><code>&lt;resultMap&gt;</code> 标签，会被解析为 ResultMap 对象，其每个子元素会被解析为 ResultMapping 对象。</li>
<li>每一个 <code>&lt;select&gt;</code>、<code>&lt;insert&gt;</code>、<code>&lt;update&gt;</code>、<code>&lt;delete&gt;</code> 标签，均会被解析为一个 MappedStatement 对象，标签内的 SQL 会被解析为一个 BoundSql 对象。</li>
</ul>
<h2 id="666-彩蛋"><a href="#666-彩蛋" class="headerlink" title="666. 彩蛋"></a>666. 彩蛋</h2><p>参考与推荐如下文章：</p>
<ul>
<li>祖大俊 <a href="https://my.oschina.net/zudajun/blog/747682" rel="external nofollow noopener noreferrer" target="_blank">《Mybatis3.4.x技术内幕（二十三）：Mybatis面试问题集锦（大结局）》</a></li>
<li>Java3y <a href="https://segmentfault.com/a/1190000013678579" rel="external nofollow noopener noreferrer" target="_blank">《Mybatis 常见面试题》</a></li>
<li>Homiss <a href="https://github.com/Homiss/Java-interview-questions/blob/master/%E6%A1%86%E6%9E%B6/MyBatis%E9%9D%A2%E8%AF%95%E9%A2%98.md" rel="external nofollow noopener noreferrer" target="_blank">《MyBatis 面试题》</a></li>
</ul>

</div>
<div class="article-info article-info-index">

<div class="article-tag tagcloud">
<i class="icon-price-tags icon"></i>
<ul class="article-tag-list">

<li class="article-tag-list-item">
<a href="javascript:void(0)" class="js-tag article-tag-list-link color4">面试题</a>
</li>

</ul>
</div>

<div class="article-category tagcloud">
<i class="icon-book icon"></i>
<ul class="article-tag-list">

<li class="article-tag-list-item">
<a href="/categories/MyBatis//" class="article-tag-list-link color3">MyBatis</a>
</li>

</ul>
</div>

<div class="clearfix"></div>
</div>
</div>
