<div class="col-md-12"><div class="post-container"><h1 class="post-title"><a href="https://tech.meituan.com/2019/11/07/java-dynamic-debugging-technology.html" rel="bookmark">Java 动态调试技术原理及实践</a></h1><div class="meta-box"><span class="m-post-date"><i class="fa fa-calendar-o"></i>2019年11月07日</span>
<span class="m-post-nick">作者: 胡健</span>
<span class="m-post-permalink"><i class="fa fa-link-o"></i><a href="https://tech.meituan.com/2019/11/07/java-dynamic-debugging-technology.html" target="_blank">文章链接</a></span>
<span class="m-post-count"><i class="fa fa-pencil"></i>20314字</span>
<span class="m-post-reading"><i class="fa fa-hourglass-start"></i>41分钟阅读</span></div><div class="post-content"><div class="content"><p>断点调试是我们最常使用的调试手段，它可以获取到方法执行过程中的变量信息，并可以观察到方法的执行路径。但断点调试会在断点位置停顿，使得整个应用停止响应。在线上停顿应用是致命的，动态调试技术给了我们创造新的调试模式的想象空间。本文将研究Java语言中的动态调试技术，首先概括Java动态调试所涉及的技术基础，接着介绍我们在Java动态调试领域的思考及实践，通过结合实际业务场景，设计并实现了一种具备动态性的断点调试工具Java-debug-tool，显著提高了故障排查效率。</p><p>JVMTI （JVM Tool Interface）是Java虚拟机对外提供的Native编程接口，通过JVMTI，外部进程可以获取到运行时JVM的诸多信息，比如线程、GC等。Agent是一个运行在目标JVM的特定程序，它的职责是负责从目标JVM中获取数据，然后将数据传递给外部进程。加载Agent的时机可以是目标JVM启动之时，也可以是在目标JVM运行时进行加载，而在目标JVM运行时进行Agent加载具备动态性，对于时机未知的Debug场景来说非常实用。下面将详细分析Java Agent技术的实现细节。</p><h2 id="2-1-agent的实现模式">2.1 Agent的实现模式</h2><p>JVMTI是一套Native接口，在Java SE 5之前，要实现一个Agent只能通过编写Native代码来实现。从Java SE 5开始，可以使用Java的Instrumentation接口（java.lang.instrument）来编写Agent。无论是通过Native的方式还是通过Java Instrumentation接口的方式来编写Agent，它们的工作都是借助JVMTI来进行完成，下面介绍通过Java Instrumentation接口编写Agent的方法。</p><h3 id="2-1-1-通过java-instrumentation-api">2.1.1 通过Java Instrumentation API</h3><ul><li>实现Agent启动方法</li></ul><p>Java Agent支持目标JVM启动时加载，也支持在目标JVM运行时加载，这两种不同的加载模式会使用不同的入口函数，如果需要在目标JVM启动的同时加载Agent，那么可以选择实现下面的方法：</p><pre><code class="hljs cs">[<span class="hljs-meta"><span class="hljs-meta">1</span></span>] <span class="hljs-function"><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-keyword">public</span></span></span><span class="hljs-function"> </span><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-keyword">static</span></span></span><span class="hljs-function"> </span><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-keyword">void</span></span></span><span class="hljs-function"> </span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">premain</span></span></span><span class="hljs-function">(</span><span class="hljs-params"><span class="hljs-function"><span class="hljs-params">String agentArgs, Instrumentation inst</span></span></span><span class="hljs-function">)</span></span>;
[<span class="hljs-meta"><span class="hljs-meta">2</span></span>] <span class="hljs-function"><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-keyword">public</span></span></span><span class="hljs-function"> </span><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-keyword">static</span></span></span><span class="hljs-function"> </span><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-keyword">void</span></span></span><span class="hljs-function"> </span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">premain</span></span></span><span class="hljs-function">(</span><span class="hljs-params"><span class="hljs-function"><span class="hljs-params">String agentArgs</span></span></span><span class="hljs-function">)</span></span>;
</code></pre><p>JVM将首先寻找[1]，如果没有发现[1]，再寻找[2]。如果希望在目标JVM运行时加载Agent，则需要实现下面的方法：</p><pre><code class="hljs cs">[<span class="hljs-meta"><span class="hljs-meta">1</span></span>] <span class="hljs-function"><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-keyword">public</span></span></span><span class="hljs-function"> </span><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-keyword">static</span></span></span><span class="hljs-function"> </span><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-keyword">void</span></span></span><span class="hljs-function"> </span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">agentmain</span></span></span><span class="hljs-function">(</span><span class="hljs-params"><span class="hljs-function"><span class="hljs-params">String agentArgs, Instrumentation inst</span></span></span><span class="hljs-function">)</span></span>;
[<span class="hljs-meta"><span class="hljs-meta">2</span></span>] <span class="hljs-function"><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-keyword">public</span></span></span><span class="hljs-function"> </span><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-keyword">static</span></span></span><span class="hljs-function"> </span><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-keyword">void</span></span></span><span class="hljs-function"> </span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">agentmain</span></span></span><span class="hljs-function">(</span><span class="hljs-params"><span class="hljs-function"><span class="hljs-params">String agentArgs</span></span></span><span class="hljs-function">)</span></span>;
</code></pre><p>这两组方法的第一个参数AgentArgs是随同 “– javaagent”一起传入的程序参数，如果这个字符串代表了多个参数，就需要自己解析这些参数。inst是Instrumentation类型的对象，是JVM自动传入的，我们可以拿这个参数进行类增强等操作。</p><ul><li>指定Main-Class</li></ul><p>Agent需要打包成一个jar包，在ManiFest属性中指定“Premain-Class”或者“Agent-Class”：</p><pre><code class="hljs coffeescript">Premain-Class: <span class="hljs-class"><span class="hljs-keyword"><span class="hljs-class"><span class="hljs-keyword">class</span></span></span></span>
Agent-Class: <span class="hljs-class"><span class="hljs-keyword"><span class="hljs-class"><span class="hljs-keyword">class</span></span></span></span>
</code></pre><ul><li>挂载到目标JVM</li></ul><p>将编写的Agent打成jar包后，就可以挂载到目标JVM上去了。如果选择在目标JVM启动时加载Agent，则可以使用 “-javaagent:<jarpath>[=<option>]“，具体的使用方法可以使用“Java -Help”来查看。如果想要在运行时挂载Agent到目标JVM，就需要做一些额外的开发了。</option></jarpath></p><p>com.sun.tools.attach.VirtualMachine 这个类代表一个JVM抽象，可以通过这个类找到目标JVM，并且将Agent挂载到目标JVM上。下面是使用com.sun.tools.attach.VirtualMachine进行动态挂载Agent的一般实现：</p><pre><code class="hljs php">    <span class="hljs-keyword"><span class="hljs-keyword">private</span></span> void attachAgentToTargetJVM() throws <span class="hljs-keyword"><span class="hljs-keyword">Exception</span></span> {
<span class="hljs-keyword"><span class="hljs-keyword">List</span></span>&lt;VirtualMachineDescriptor&gt; virtualMachineDescriptors = VirtualMachine.<span class="hljs-keyword"><span class="hljs-keyword">list</span></span>();
VirtualMachineDescriptor targetVM = <span class="hljs-keyword"><span class="hljs-keyword">null</span></span>;
<span class="hljs-keyword"><span class="hljs-keyword">for</span></span> (VirtualMachineDescriptor descriptor : virtualMachineDescriptors) {
<span class="hljs-keyword"><span class="hljs-keyword">if</span></span> (descriptor.id().equals(configure.getPid())) {
targetVM = descriptor;
<span class="hljs-keyword"><span class="hljs-keyword">break</span></span>;
}
}
<span class="hljs-keyword"><span class="hljs-keyword">if</span></span> (targetVM == <span class="hljs-keyword"><span class="hljs-keyword">null</span></span>) {
<span class="hljs-keyword"><span class="hljs-keyword">throw</span></span> <span class="hljs-keyword"><span class="hljs-keyword">new</span></span> IllegalArgumentException(<span class="hljs-string"><span class="hljs-string">"could not find the target jvm by process id:"</span></span> + configure.getPid());
}
VirtualMachine virtualMachine = <span class="hljs-keyword"><span class="hljs-keyword">null</span></span>;
<span class="hljs-keyword"><span class="hljs-keyword">try</span></span> {
virtualMachine = VirtualMachine.attach(targetVM);
virtualMachine.loadAgent(<span class="hljs-string"><span class="hljs-string">"{agent}"</span></span>, <span class="hljs-string"><span class="hljs-string">"{params}"</span></span>);
} <span class="hljs-keyword"><span class="hljs-keyword">catch</span></span> (<span class="hljs-keyword"><span class="hljs-keyword">Exception</span></span> e) {
<span class="hljs-keyword"><span class="hljs-keyword">if</span></span> (virtualMachine != <span class="hljs-keyword"><span class="hljs-keyword">null</span></span>) {
virtualMachine.detach();
}
}
}
</code></pre><p>首先通过指定的进程ID找到目标JVM，然后通过Attach挂载到目标JVM上，执行加载Agent操作。VirtualMachine的Attach方法就是用来将Agent挂载到目标JVM上去的，而Detach则是将Agent从目标JVM卸载。关于Agent是如何挂载到目标JVM上的具体技术细节，将在下文中进行分析。</p><h2 id="2-2-启动时加载agent">2.2 启动时加载Agent</h2><h3 id="2-2-1-参数解析">2.2.1 参数解析</h3><p>创建JVM时，JVM会进行参数解析，即解析那些用来配置JVM启动的参数，比如堆大小、GC等；本文主要关注解析的参数为-agentlib、 -agentpath、 -javaagent，这几个参数用来指定Agent，JVM会根据这几个参数加载Agent。下面来分析一下JVM是如何解析这几个参数的。</p><pre><code class="hljs cpp">  <span class="hljs-comment"><span class="hljs-comment">// -agentlib and -agentpath</span></span>
<span class="hljs-keyword"><span class="hljs-keyword">if</span></span> (match_option(option, <span class="hljs-string"><span class="hljs-string">"-agentlib:"</span></span>, &amp;tail) ||
(is_absolute_path = match_option(option, <span class="hljs-string"><span class="hljs-string">"-agentpath:"</span></span>, &amp;tail))) {
<span class="hljs-keyword"><span class="hljs-keyword">if</span></span>(tail != <span class="hljs-literal"><span class="hljs-literal">NULL</span></span>) {
<span class="hljs-keyword"><span class="hljs-keyword">const</span></span> <span class="hljs-keyword"><span class="hljs-keyword">char</span></span>* pos = <span class="hljs-built_in"><span class="hljs-built_in">strchr</span></span>(tail, <span class="hljs-string"><span class="hljs-string">'='</span></span>);
<span class="hljs-keyword"><span class="hljs-keyword">size_t</span></span> len = (pos == <span class="hljs-literal"><span class="hljs-literal">NULL</span></span>) ? <span class="hljs-built_in"><span class="hljs-built_in">strlen</span></span>(tail) : pos - tail;
<span class="hljs-keyword"><span class="hljs-keyword">char</span></span>* name = <span class="hljs-built_in"><span class="hljs-built_in">strncpy</span></span>(NEW_C_HEAP_ARRAY(<span class="hljs-keyword"><span class="hljs-keyword">char</span></span>, len + <span class="hljs-number"><span class="hljs-number">1</span></span>, mtArguments), tail, len);
name[len] = <span class="hljs-string"><span class="hljs-string">'\0'</span></span>;
<span class="hljs-keyword"><span class="hljs-keyword">char</span></span> *options = <span class="hljs-literal"><span class="hljs-literal">NULL</span></span>;
<span class="hljs-keyword"><span class="hljs-keyword">if</span></span>(pos != <span class="hljs-literal"><span class="hljs-literal">NULL</span></span>) {
options = os::strdup_check_oom(pos + <span class="hljs-number"><span class="hljs-number">1</span></span>, mtArguments);
}
<span class="hljs-meta"><span class="hljs-meta">#</span><span class="hljs-meta-keyword"><span class="hljs-meta"><span class="hljs-meta-keyword">if</span></span></span><span class="hljs-meta"> !INCLUDE_JVMTI</span></span>
<span class="hljs-keyword"><span class="hljs-keyword">if</span></span> (valid_jdwp_agent(name, is_absolute_path)) {
jio_fprintf(defaultStream::error_stream(),
<span class="hljs-string"><span class="hljs-string">"Debugging agents are not supported in this VM\n"</span></span>);
<span class="hljs-keyword"><span class="hljs-keyword">return</span></span> JNI_ERR;
}
<span class="hljs-meta"><span class="hljs-meta">#</span><span class="hljs-meta-keyword"><span class="hljs-meta"><span class="hljs-meta-keyword">endif</span></span></span><span class="hljs-meta"> </span><span class="hljs-comment"><span class="hljs-meta"><span class="hljs-comment">// !INCLUDE_JVMTI</span></span></span></span>
add_init_agent(name, options, is_absolute_path);
}
<span class="hljs-comment"><span class="hljs-comment">// -javaagent</span></span>
} <span class="hljs-keyword"><span class="hljs-keyword">else</span></span> <span class="hljs-keyword"><span class="hljs-keyword">if</span></span> (match_option(option, <span class="hljs-string"><span class="hljs-string">"-javaagent:"</span></span>, &amp;tail)) {
#<span class="hljs-keyword"><span class="hljs-keyword">if</span></span> !INCLUDE_JVMTI
jio_fprintf(defaultStream::error_stream(),
<span class="hljs-string"><span class="hljs-string">"Instrumentation agents are not supported in this VM\n"</span></span>);
<span class="hljs-keyword"><span class="hljs-keyword">return</span></span> JNI_ERR;
<span class="hljs-meta"><span class="hljs-meta">#</span><span class="hljs-meta-keyword"><span class="hljs-meta"><span class="hljs-meta-keyword">else</span></span></span></span>
<span class="hljs-keyword"><span class="hljs-keyword">if</span></span> (tail != <span class="hljs-literal"><span class="hljs-literal">NULL</span></span>) {
<span class="hljs-keyword"><span class="hljs-keyword">size_t</span></span> length = <span class="hljs-built_in"><span class="hljs-built_in">strlen</span></span>(tail) + <span class="hljs-number"><span class="hljs-number">1</span></span>;
<span class="hljs-keyword"><span class="hljs-keyword">char</span></span> *options = NEW_C_HEAP_ARRAY(<span class="hljs-keyword"><span class="hljs-keyword">char</span></span>, length, mtArguments);
jio_snprintf(options, length, <span class="hljs-string"><span class="hljs-string">"%s"</span></span>, tail);
add_init_agent(<span class="hljs-string"><span class="hljs-string">"instrument"</span></span>, options, <span class="hljs-literal"><span class="hljs-literal">false</span></span>);
<span class="hljs-comment"><span class="hljs-comment">// java agents need module java.instrument</span></span>
<span class="hljs-keyword"><span class="hljs-keyword">if</span></span> (!create_numbered_property(<span class="hljs-string"><span class="hljs-string">"jdk.module.addmods"</span></span>, <span class="hljs-string"><span class="hljs-string">"java.instrument"</span></span>, addmods_count++)) {
<span class="hljs-keyword"><span class="hljs-keyword">return</span></span> JNI_ENOMEM;
}
}
<span class="hljs-meta"><span class="hljs-meta">#</span><span class="hljs-meta-keyword"><span class="hljs-meta"><span class="hljs-meta-keyword">endif</span></span></span><span class="hljs-meta"> </span><span class="hljs-comment"><span class="hljs-meta"><span class="hljs-comment">// !INCLUDE_JVMTI</span></span></span></span>
}
</code></pre><p>上面的代码片段截取自hotspot/src/share/vm/runtime/arguments.cpp中的 Arguments::parse_each_vm_init_arg(const JavaVMInitArgs* args, bool* patch_mod_javabase, Flag::Flags origin) 函数，该函数用来解析一个具体的JVM参数。这段代码的主要功能是解析出需要加载的Agent路径，然后调用add_init_agent函数进行解析结果的存储。下面先看一下add_init_agent函数的具体实现：</p><pre><code class="hljs cpp">  <span class="hljs-comment"><span class="hljs-comment">// -agentlib and -agentpath arguments</span></span>
<span class="hljs-keyword"><span class="hljs-keyword">static</span></span> AgentLibraryList _agentList;
<span class="hljs-function"><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-keyword">static</span></span></span><span class="hljs-function"> </span><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-keyword">void</span></span></span><span class="hljs-function"> </span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">add_init_agent</span></span></span><span class="hljs-params"><span class="hljs-function"><span class="hljs-params">(</span></span><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-params"><span class="hljs-keyword">const</span></span></span></span><span class="hljs-function"><span class="hljs-params"> </span></span><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-params"><span class="hljs-keyword">char</span></span></span></span><span class="hljs-function"><span class="hljs-params">* name, </span></span><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-params"><span class="hljs-keyword">char</span></span></span></span><span class="hljs-function"><span class="hljs-params">* options, </span></span><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-params"><span class="hljs-keyword">bool</span></span></span></span><span class="hljs-function"><span class="hljs-params"> absolute_path)</span></span></span><span class="hljs-function">
</span></span>{ _agentList.add(<span class="hljs-keyword"><span class="hljs-keyword">new</span></span> AgentLibrary(name, options, absolute_path, <span class="hljs-literal"><span class="hljs-literal">NULL</span></span>)); }
</code></pre><p>AgentLibraryList是一个简单的链表结构，add_init_agent函数将解析好的、需要加载的Agent添加到这个链表中，等待后续的处理。</p><p>这里需要注意，解析-javaagent参数有一些特别之处，这个参数用来指定一个我们通过Java Instrumentation API来编写的Agent，Java Instrumentation API底层依赖的是JVMTI，对-JavaAgent的处理也说明了这一点，在调用add_init_agent函数时第一个参数是“instrument”，关于加载Agent这个问题在下一小节进行展开。到此，我们知道在启动JVM时指定的Agent已经被JVM解析完存放在了一个链表结构中。下面来分析一下JVM是如何加载这些Agent的。</p><h3 id="2-2-2-执行加载操作">2.2.2 执行加载操作</h3><p>在创建JVM进程的函数中，解析完JVM参数之后，下面的这段代码和加载Agent相关：</p><pre><code class="hljs less">  <span class="hljs-comment"><span class="hljs-comment">// Launch -agentlib/-agentpath and converted -Xrun agents</span></span>
<span class="hljs-selector-tag"><span class="hljs-selector-tag">if</span></span> (<span class="hljs-attribute"><span class="hljs-attribute">Arguments</span></span>::init_agents_at_startup()) {
<span class="hljs-selector-tag"><span class="hljs-selector-tag">create_vm_init_agents</span></span>();
}
<span class="hljs-selector-tag"><span class="hljs-selector-tag">static</span></span> <span class="hljs-selector-tag"><span class="hljs-selector-tag">bool</span></span> <span class="hljs-selector-tag"><span class="hljs-selector-tag">init_agents_at_startup</span></span>() {
<span class="hljs-selector-tag"><span class="hljs-selector-tag">return</span></span> !<span class="hljs-selector-tag"><span class="hljs-selector-tag">_agentList</span></span><span class="hljs-selector-class"><span class="hljs-selector-class">.is_empty</span></span>(); 
}
</code></pre><p>当JVM判断出上一小节中解析出来的Agent不为空的时候，就要去调用函数create_vm_init_agents来加载Agent，下面来分析一下create_vm_init_agents函数是如何加载Agent的。</p><pre><code class="hljs php">void Threads::create_vm_init_agents() {
AgentLibrary* agent;
<span class="hljs-keyword"><span class="hljs-keyword">for</span></span> (agent = Arguments::agents(); agent != <span class="hljs-keyword"><span class="hljs-keyword">NULL</span></span>; agent = agent-&gt;next()) {
OnLoadEntry_t  on_load_entry = lookup_agent_on_load(agent);
<span class="hljs-keyword"><span class="hljs-keyword">if</span></span> (on_load_entry != <span class="hljs-keyword"><span class="hljs-keyword">NULL</span></span>) {
<span class="hljs-comment"><span class="hljs-comment">// Invoke the Agent_OnLoad function</span></span>
jint err = (*on_load_entry)(&amp;main_vm, agent-&gt;options(), <span class="hljs-keyword"><span class="hljs-keyword">NULL</span></span>);
}
}
}
</code></pre><p>create_vm_init_agents这个函数通过遍历Agent链表来逐个加载Agent。通过这段代码可以看出，首先通过lookup_agent_on_load来加载Agent并且找到Agent_OnLoad函数，这个函数是Agent的入口函数。如果没找到这个函数，则认为是加载了一个不合法的Agent，则什么也不做，否则调用这个函数，这样Agent的代码就开始执行起来了。对于使用Java Instrumentation API来编写Agent的方式来说，在解析阶段观察到在add_init_agent函数里面传递进去的是一个叫做”instrument”的字符串，其实这是一个动态链接库。在Linux里面，这个库叫做libinstrument.so，在BSD系统中叫做libinstrument.dylib，该动态链接库在{JAVA_HOME}/jre/lib/目录下。</p><h3 id="2-2-3-instrument动态链接库">2.2.3 instrument动态链接库</h3><p>libinstrument用来支持使用Java Instrumentation API来编写Agent，在libinstrument中有一个非常重要的类称为：JPLISAgent（Java Programming Language Instrumentation Services Agent），它的作用是初始化所有通过Java Instrumentation API编写的Agent，并且也承担着通过JVMTI实现Java Instrumentation中暴露API的责任。</p><p>我们已经知道，在JVM启动的时候，JVM会通过-javaagent参数加载Agent。最开始加载的是libinstrument动态链接库，然后在动态链接库里面找到JVMTI的入口方法：Agent_OnLoad。下面就来分析一下在libinstrument动态链接库中，Agent_OnLoad函数是怎么实现的。</p><pre><code class="hljs cpp"><span class="hljs-function"><span class="hljs-function">JNIEXPORT jint JNICALL
</span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">DEF_Agent_OnLoad</span></span></span><span class="hljs-params"><span class="hljs-function"><span class="hljs-params">(JavaVM *vm, </span></span><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-params"><span class="hljs-keyword">char</span></span></span></span><span class="hljs-function"><span class="hljs-params"> *tail, </span></span><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-params"><span class="hljs-keyword">void</span></span></span></span><span class="hljs-function"><span class="hljs-params"> * reserved)</span></span></span><span class="hljs-function"> </span></span>{
initerror = createNewJPLISAgent(vm, &amp;agent);
<span class="hljs-keyword"><span class="hljs-keyword">if</span></span> ( initerror == JPLIS_INIT_ERROR_NONE ) {
<span class="hljs-keyword"><span class="hljs-keyword">if</span></span> (parseArgumentTail(tail, &amp;jarfile, &amp;options) != <span class="hljs-number"><span class="hljs-number">0</span></span>) {
<span class="hljs-built_in"><span class="hljs-built_in">fprintf</span></span>(<span class="hljs-built_in"><span class="hljs-built_in">stderr</span></span>, <span class="hljs-string"><span class="hljs-string">"-javaagent: memory allocation failure.\n"</span></span>);
<span class="hljs-keyword"><span class="hljs-keyword">return</span></span> JNI_ERR;
}
attributes = readAttributes(jarfile);
premainClass = getAttribute(attributes, <span class="hljs-string"><span class="hljs-string">"Premain-Class"</span></span>);
<span class="hljs-comment"><span class="hljs-comment">/* Save the jarfile name */</span></span>
agent-&gt;mJarfile = jarfile;
<span class="hljs-comment"><span class="hljs-comment">/*
* Convert JAR attributes into agent capabilities
*/</span></span>
convertCapabilityAttributes(attributes, agent);
<span class="hljs-comment"><span class="hljs-comment">/*
* Track (record) the agent class name and options data
*/</span></span>
initerror = recordCommandLineData(agent, premainClass, options);
}
<span class="hljs-keyword"><span class="hljs-keyword">return</span></span> result;
}
</code></pre><p>上述代码片段是经过精简的libinstrument中Agent_OnLoad实现的，大概的流程就是：先创建一个JPLISAgent，然后将ManiFest中设定的一些参数解析出来， 比如（Premain-Class）等。创建了JPLISAgent之后，调用initializeJPLISAgent对这个Agent进行初始化操作。跟进initializeJPLISAgent看一下是如何初始化的：</p><pre><code class="hljs rust">JPLISInitializationError initializeJPLISAgent(JPLISAgent *agent, JavaVM *vm, jvmtiEnv *jvmtienv) {
<span class="hljs-comment"><span class="hljs-comment">/* check what capabilities are available */</span></span>
checkCapabilities(agent);
<span class="hljs-comment"><span class="hljs-comment">/* check phase - if live phase then we don't need the VMInit event */</span></span>
jvmtierror = (*jvmtienv)-&gt;GetPhase(jvmtienv, &amp;phase);
<span class="hljs-comment"><span class="hljs-comment">/* now turn on the VMInit event */</span></span>
<span class="hljs-keyword"><span class="hljs-keyword">if</span></span> ( jvmtierror == JVMTI_ERROR_NONE ) {
jvmtiEventCallbacks callbacks;
memset(&amp;callbacks, <span class="hljs-number"><span class="hljs-number">0</span></span>, <span class="hljs-keyword"><span class="hljs-keyword">sizeof</span></span>(callbacks));
callbacks.VMInit = &amp;eventHandlerVMInit;
jvmtierror = (*jvmtienv)-&gt;SetEventCallbacks(jvmtienv,&amp;callbacks,<span class="hljs-keyword"><span class="hljs-keyword">sizeof</span></span>(callbacks));
}
<span class="hljs-keyword"><span class="hljs-keyword">if</span></span> ( jvmtierror == JVMTI_ERROR_NONE ) {
jvmtierror = (*jvmtienv)-&gt;SetEventNotificationMode(jvmtienv,JVMTI_ENABLE,JVMTI_EVENT_VM_INIT,NULL);
}
<span class="hljs-keyword"><span class="hljs-keyword">return</span></span> (jvmtierror == JVMTI_ERROR_NONE)? JPLIS_INIT_ERROR_NONE : JPLIS_INIT_ERROR_FAILURE;
}
</code></pre><p>这里，我们关注callbacks.VMInit = &amp;eventHandlerVMInit;这行代码，这里设置了一个VMInit事件的回调函数，表示在JVM初始化的时候会回调eventHandlerVMInit函数。下面来看一下这个函数的实现细节，猜测就是在这里调用了Premain方法：</p><pre><code class="hljs rust">void JNICALL  eventHandlerVMInit( jvmtiEnv *jvmtienv,JNIEnv *jnienv,jthread thread) {
<span class="hljs-comment"><span class="hljs-comment">// ...</span></span>
success = processJavaStart( environment-&gt;mAgent, jnienv);
<span class="hljs-comment"><span class="hljs-comment">// ...</span></span>
}
jboolean  processJavaStart(JPLISAgent *agent,JNIEnv *jnienv) {
result = createInstrumentationImpl(jnienv, agent);
<span class="hljs-comment"><span class="hljs-comment">/*
*  Load the Java agent, and call the premain.
*/</span></span>
<span class="hljs-keyword"><span class="hljs-keyword">if</span></span> ( result ) {
result = startJavaAgent(agent, jnienv, agent-&gt;mAgentClassName, agent-&gt;mOptionsString, agent-&gt;mPremainCaller);
}
<span class="hljs-keyword"><span class="hljs-keyword">return</span></span> result;
}
jboolean startJavaAgent( JPLISAgent *agent,JNIEnv *jnienv,<span class="hljs-keyword"><span class="hljs-keyword">const</span></span> <span class="hljs-built_in"><span class="hljs-built_in">char</span></span> *classname,<span class="hljs-keyword"><span class="hljs-keyword">const</span></span> <span class="hljs-built_in"><span class="hljs-built_in">char</span></span> *optionsString,jmethodID agentMainMethod) {
<span class="hljs-comment"><span class="hljs-comment">// ...  </span></span>
invokeJavaAgentMainMethod(jnienv,agent-&gt;mInstrumentationImpl,agentMainMethod, classNameObject,optionsStringObject);
<span class="hljs-comment"><span class="hljs-comment">// ...</span></span>
}
</code></pre><p>看到这里，Instrument已经实例化，invokeJavaAgentMainMethod这个方法将我们的premain方法执行起来了。接着，我们就可以根据Instrument实例来做我们想要做的事情了。</p><h2 id="2-3-运行时加载agent">2.3 运行时加载Agent</h2><p>比起JVM启动时加载Agent，运行时加载Agent就比较有诱惑力了，因为运行时加载Agent的能力给我们提供了很强的动态性，我们可以在需要的时候加载Agent来进行一些工作。因为是动态的，我们可以按照需求来加载所需要的Agent，下面来分析一下动态加载Agent的相关技术细节。</p><h3 id="2-3-1-attachlistener">2.3.1 AttachListener</h3><p>Attach机制通过Attach Listener线程来进行相关事务的处理，下面来看一下Attach Listener线程是如何初始化的。</p><pre><code class="hljs cpp"><span class="hljs-comment"><span class="hljs-comment">// Starts the Attach Listener thread</span></span>
<span class="hljs-keyword"><span class="hljs-keyword">void</span></span> AttachListener::init() {
<span class="hljs-comment"><span class="hljs-comment">// 创建线程相关部分代码被去掉了</span></span>
<span class="hljs-keyword"><span class="hljs-keyword">const</span></span> <span class="hljs-keyword"><span class="hljs-keyword">char</span></span> thread_name[] = <span class="hljs-string"><span class="hljs-string">"Attach Listener"</span></span>;
Handle <span class="hljs-built_in"><span class="hljs-built_in">string</span></span> = java_lang_String::create_from_str(thread_name, THREAD);
{ <span class="hljs-function"><span class="hljs-function">MutexLocker </span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">mu</span></span></span><span class="hljs-params"><span class="hljs-function"><span class="hljs-params">(Threads_lock)</span></span></span></span>;
JavaThread* listener_thread = <span class="hljs-keyword"><span class="hljs-keyword">new</span></span> JavaThread(&amp;attach_listener_thread_entry);
<span class="hljs-comment"><span class="hljs-comment">// ...</span></span>
}
}
</code></pre><p>我们知道，一个线程启动之后都需要指定一个入口来执行代码，Attach Listener线程的入口是attach_listener_thread_entry，下面看一下这个函数的具体实现：</p><pre><code class="hljs cpp"><span class="hljs-function"><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-keyword">static</span></span></span><span class="hljs-function"> </span><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-keyword">void</span></span></span><span class="hljs-function"> </span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">attach_listener_thread_entry</span></span></span><span class="hljs-params"><span class="hljs-function"><span class="hljs-params">(JavaThread* thread, TRAPS)</span></span></span><span class="hljs-function"> </span></span>{
AttachListener::set_initialized();
<span class="hljs-keyword"><span class="hljs-keyword">for</span></span> (;;) {
AttachOperation* op = AttachListener::dequeue();
<span class="hljs-comment"><span class="hljs-comment">// find the function to dispatch too</span></span>
AttachOperationFunctionInfo* info = <span class="hljs-literal"><span class="hljs-literal">NULL</span></span>;
<span class="hljs-keyword"><span class="hljs-keyword">for</span></span> (<span class="hljs-keyword"><span class="hljs-keyword">int</span></span> i=<span class="hljs-number"><span class="hljs-number">0</span></span>; funcs[i].name != <span class="hljs-literal"><span class="hljs-literal">NULL</span></span>; i++) {
<span class="hljs-keyword"><span class="hljs-keyword">const</span></span> <span class="hljs-keyword"><span class="hljs-keyword">char</span></span>* name = funcs[i].name;
<span class="hljs-keyword"><span class="hljs-keyword">if</span></span> (<span class="hljs-built_in"><span class="hljs-built_in">strcmp</span></span>(op-&gt;name(), name) == <span class="hljs-number"><span class="hljs-number">0</span></span>) {
info = &amp;(funcs[i]); <span class="hljs-keyword"><span class="hljs-keyword">break</span></span>;
}}
<span class="hljs-comment"><span class="hljs-comment">// dispatch to the function that implements this operation</span></span>
res = (info-&gt;func)(op, &amp;st);
<span class="hljs-comment"><span class="hljs-comment">//...</span></span>
}
}
</code></pre><p>整个函数执行逻辑，大概是这样的：</p><ul><li>拉取一个需要执行的任务：AttachListener::dequeue。</li><li>查询匹配的命令处理函数。</li><li>执行匹配到的命令执行函数。</li></ul><p>其中第二步里面存在一个命令函数表，整个表如下：</p><pre><code class="hljs php"><span class="hljs-keyword"><span class="hljs-keyword">static</span></span> AttachOperationFunctionInfo funcs[] = {
{ <span class="hljs-string"><span class="hljs-string">"agentProperties"</span></span>,  get_agent_properties },
{ <span class="hljs-string"><span class="hljs-string">"datadump"</span></span>,         data_dump },
{ <span class="hljs-string"><span class="hljs-string">"dumpheap"</span></span>,         dump_heap },
{ <span class="hljs-string"><span class="hljs-string">"load"</span></span>,             load_agent },
{ <span class="hljs-string"><span class="hljs-string">"properties"</span></span>,       get_system_properties },
{ <span class="hljs-string"><span class="hljs-string">"threaddump"</span></span>,       thread_dump },
{ <span class="hljs-string"><span class="hljs-string">"inspectheap"</span></span>,      heap_inspection },
{ <span class="hljs-string"><span class="hljs-string">"setflag"</span></span>,          set_flag },
{ <span class="hljs-string"><span class="hljs-string">"printflag"</span></span>,        print_flag },
{ <span class="hljs-string"><span class="hljs-string">"jcmd"</span></span>,             jcmd },
{ <span class="hljs-keyword"><span class="hljs-keyword">NULL</span></span>,               <span class="hljs-keyword"><span class="hljs-keyword">NULL</span></span> }
};
</code></pre><p>对于加载Agent来说，命令就是“load”。现在，我们知道了Attach Listener大概的工作模式，但是还是不太清楚任务从哪来，这个秘密就藏在AttachListener::dequeue这行代码里面，接下来我们来分析一下dequeue这个函数：</p><pre><code class="hljs cpp">LinuxAttachOperation* LinuxAttachListener::dequeue() {
<span class="hljs-keyword"><span class="hljs-keyword">for</span></span> (;;) {
<span class="hljs-comment"><span class="hljs-comment">// wait for client to connect</span></span>
<span class="hljs-class"><span class="hljs-keyword"><span class="hljs-class"><span class="hljs-keyword">struct</span></span></span><span class="hljs-class"> </span><span class="hljs-title"><span class="hljs-class"><span class="hljs-title">sockaddr</span></span></span><span class="hljs-class"> </span><span class="hljs-title"><span class="hljs-class"><span class="hljs-title">addr</span></span></span><span class="hljs-class">;</span></span>
<span class="hljs-keyword"><span class="hljs-keyword">socklen_t</span></span> len = <span class="hljs-keyword"><span class="hljs-keyword">sizeof</span></span>(addr);
RESTARTABLE(::accept(listener(), &amp;addr, &amp;len), s);
<span class="hljs-comment"><span class="hljs-comment">// get the credentials of the peer and check the effective uid/guid</span></span>
<span class="hljs-comment"><span class="hljs-comment">// - check with jeff on this.</span></span>
<span class="hljs-class"><span class="hljs-keyword"><span class="hljs-class"><span class="hljs-keyword">struct</span></span></span><span class="hljs-class"> </span><span class="hljs-title"><span class="hljs-class"><span class="hljs-title">ucred</span></span></span><span class="hljs-class"> </span><span class="hljs-title"><span class="hljs-class"><span class="hljs-title">cred_info</span></span></span><span class="hljs-class">;</span></span>
<span class="hljs-keyword"><span class="hljs-keyword">socklen_t</span></span> optlen = <span class="hljs-keyword"><span class="hljs-keyword">sizeof</span></span>(cred_info);
<span class="hljs-keyword"><span class="hljs-keyword">if</span></span> (::getsockopt(s, SOL_SOCKET, SO_PEERCRED, (<span class="hljs-keyword"><span class="hljs-keyword">void</span></span>*)&amp;cred_info, &amp;optlen) == <span class="hljs-number"><span class="hljs-number">-1</span></span>) {
::close(s);
<span class="hljs-keyword"><span class="hljs-keyword">continue</span></span>;
}
<span class="hljs-comment"><span class="hljs-comment">// peer credential look okay so we read the request</span></span>
LinuxAttachOperation* op = read_request(s);
<span class="hljs-keyword"><span class="hljs-keyword">return</span></span> op;
}
}
</code></pre><p>这是Linux上的实现，不同的操作系统实现方式不太一样。上面的代码表面，Attach Listener在某个端口监听着，通过accept来接收一个连接，然后从这个连接里面将请求读取出来，然后将请求包装成一个AttachOperation类型的对象，之后就会从表里查询对应的处理函数，然后进行处理。</p><p>Attach Listener使用一种被称为“懒加载”的策略进行初始化，也就是说，JVM启动的时候Attach Listener并不一定会启动起来。下面我们来分析一下这种“懒加载”策略的具体实现方案。</p><pre><code class="hljs rust">  <span class="hljs-comment"><span class="hljs-comment">// Start Attach Listener if +StartAttachListener or it can't be started lazily</span></span>
<span class="hljs-keyword"><span class="hljs-keyword">if</span></span> (!DisableAttachMechanism) {
AttachListener::vm_start();
<span class="hljs-keyword"><span class="hljs-keyword">if</span></span> (StartAttachListener || AttachListener::init_at_startup()) {
AttachListener::init();
}
}
<span class="hljs-comment"><span class="hljs-comment">// Attach Listener is started lazily except in the case when</span></span>
<span class="hljs-comment"><span class="hljs-comment">// +ReduseSignalUsage is used</span></span>
<span class="hljs-built_in"><span class="hljs-built_in">bool</span></span> AttachListener::init_at_startup() {
<span class="hljs-keyword"><span class="hljs-keyword">if</span></span> (ReduceSignalUsage) {
<span class="hljs-keyword"><span class="hljs-keyword">return</span></span> <span class="hljs-literal"><span class="hljs-literal">true</span></span>;
} <span class="hljs-keyword"><span class="hljs-keyword">else</span></span> {
<span class="hljs-keyword"><span class="hljs-keyword">return</span></span> <span class="hljs-literal"><span class="hljs-literal">false</span></span>;
}
}
</code></pre><p>上面的代码截取自create_vm函数，DisableAttachMechanism、StartAttachListener和ReduceSignalUsage这三个变量默认都是false，所以AttachListener::init();这行代码不会在create_vm的时候执行，而vm_start会执行。下面来看一下这个函数的实现细节：</p><pre><code class="hljs rust">void AttachListener::vm_start() {
<span class="hljs-built_in"><span class="hljs-built_in">char</span></span> <span class="hljs-function"><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-keyword">fn</span></span></span><span class="hljs-function">[</span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">UNIX_PATH_MAX</span></span></span><span class="hljs-function">];
</span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">struct</span></span></span><span class="hljs-function"> </span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">stat64</span></span></span><span class="hljs-function"> </span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">st</span></span></span><span class="hljs-function">;
</span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">int</span></span></span><span class="hljs-function"> </span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">ret</span></span></span><span class="hljs-function">;
</span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">int</span></span></span><span class="hljs-function"> </span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">n</span></span></span><span class="hljs-function"> = </span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">snprintf</span></span></span></span>(<span class="hljs-function"><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-keyword">fn</span></span></span><span class="hljs-function">, </span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">UNIX_PATH_MAX</span></span></span><span class="hljs-function">, "%</span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">s</span></span></span><span class="hljs-function">/.</span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">java_pid</span></span></span><span class="hljs-function">%</span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">d</span></span></span><span class="hljs-function">",
</span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">os</span></span></span><span class="hljs-function">::</span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">get_temp_directory</span></span></span></span>(), os::current_process_id());
assert(n &lt; (int)UNIX_PATH_MAX, <span class="hljs-string"><span class="hljs-string">"java_pid file name buffer overflow"</span></span>);
RESTARTABLE(::stat64(<span class="hljs-function"><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-keyword">fn</span></span></span><span class="hljs-function">, &amp;</span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">st</span></span></span><span class="hljs-function">), </span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">ret</span></span></span><span class="hljs-function">);
</span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">if</span></span></span><span class="hljs-function"> </span></span>(ret == <span class="hljs-number"><span class="hljs-number">0</span></span>) {
ret = ::unlink(<span class="hljs-function"><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-keyword">fn</span></span></span><span class="hljs-function">);
</span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">if</span></span></span><span class="hljs-function"> </span></span>(ret == -<span class="hljs-number"><span class="hljs-number">1</span></span>) {
log_debug(attach)(<span class="hljs-string"><span class="hljs-string">"Failed to remove stale attach pid file at %s"</span></span>, <span class="hljs-function"><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-keyword">fn</span></span></span><span class="hljs-function">);
}
}
}
</span></span></code></pre><p>这是在Linux上的实现，是将/tmp/目录下的.java_pid{pid}文件删除，后面在创建Attach Listener线程的时候会创建出来这个文件。上面说到，AttachListener::init()这行代码不会在create_vm的时候执行，这行代码的实现已经在上文中分析了，就是创建Attach Listener线程，并监听其他JVM的命令请求。现在来分析一下这行代码是什么时候被调用的，也就是“懒加载”到底是怎么加载起来的。</p><pre><code class="hljs less">  <span class="hljs-comment"><span class="hljs-comment">// Signal Dispatcher needs to be started before VMInit event is posted</span></span>
<span class="hljs-attribute"><span class="hljs-attribute">os</span></span>::signal_init();
</code></pre><p>这是create_vm中的一段代码，看起来跟信号相关，其实Attach机制就是使用信号来实现“懒加载“的。下面我们来仔细地分析一下这个过程。</p><pre><code class="hljs rust">void os::signal_init() {
<span class="hljs-keyword"><span class="hljs-keyword">if</span></span> (!ReduceSignalUsage) {
<span class="hljs-comment"><span class="hljs-comment">// Setup JavaThread for processing signals</span></span>
EXCEPTION_MARK;
Klass* k = SystemDictionary::resolve_or_fail(vmSymbols::java_lang_Thread(), <span class="hljs-literal"><span class="hljs-literal">true</span></span>, CHECK);
instanceKlassHandle klass (THREAD, k);
instanceHandle thread_oop = klass-&gt;allocate_instance_handle(CHECK);
<span class="hljs-keyword"><span class="hljs-keyword">const</span></span> <span class="hljs-built_in"><span class="hljs-built_in">char</span></span> thread_name[] = <span class="hljs-string"><span class="hljs-string">"Signal Dispatcher"</span></span>;
Handle string = java_lang_String::create_from_str(thread_name, CHECK);
<span class="hljs-comment"><span class="hljs-comment">// Initialize thread_oop to put it into the system threadGroup</span></span>
Handle thread_group (THREAD, Universe::system_thread_group());
JavaValue result(T_VOID);
JavaCalls::call_special(&amp;result, thread_oop,klass,vmSymbols::object_initializer_name(),vmSymbols::threadgroup_string_void_signature(),
thread_group,string,CHECK);
KlassHandle group(THREAD, SystemDictionary::ThreadGroup_klass());
JavaCalls::call_special(&amp;result,thread_group,group,vmSymbols::add_method_name(),vmSymbols::thread_void_signature(),thread_oop,CHECK);
os::signal_init_pd();
{ MutexLocker mu(Threads_lock);
JavaThread* signal_thread = new JavaThread(&amp;signal_thread_entry);
<span class="hljs-comment"><span class="hljs-comment">// ...</span></span>
}
<span class="hljs-comment"><span class="hljs-comment">// Handle ^BREAK</span></span>
os::signal(SIGBREAK, os::user_handler());
}
}
</code></pre><p>JVM创建了一个新的进程来实现信号处理，这个线程叫“Signal Dispatcher”，一个线程创建之后需要有一个入口，“Signal Dispatcher”的入口是signal_thread_entry：</p><p><img src="https://p0.meituan.net/travelcube/715bfe77dc43fa945919ddb81deb7c6b115183.png" alt=""></p><p>这段代码截取自signal_thread_entry函数，截取中的内容是和Attach机制信号处理相关的代码。这段代码的意思是，当接收到“SIGBREAK”信号，就执行接下来的代码，这个信号是需要Attach到JVM上的信号发出来，这个后面会再分析。我们先来看一句关键的代码：AttachListener::is_init_trigger()：</p><pre><code class="hljs rust"><span class="hljs-built_in"><span class="hljs-built_in">bool</span></span> AttachListener::is_init_trigger() {
<span class="hljs-keyword"><span class="hljs-keyword">if</span></span> (init_at_startup() || is_initialized()) {
<span class="hljs-keyword"><span class="hljs-keyword">return</span></span> <span class="hljs-literal"><span class="hljs-literal">false</span></span>;               <span class="hljs-comment"><span class="hljs-comment">// initialized at startup or already initialized</span></span>
}
<span class="hljs-built_in"><span class="hljs-built_in">char</span></span> <span class="hljs-function"><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-keyword">fn</span></span></span><span class="hljs-function">[</span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">PATH_MAX</span></span></span><span class="hljs-function">+1];
</span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">sprintf</span></span></span></span>(<span class="hljs-function"><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-keyword">fn</span></span></span><span class="hljs-function">, ".</span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">attach_pid</span></span></span><span class="hljs-function">%</span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">d</span></span></span><span class="hljs-function">", </span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">os</span></span></span><span class="hljs-function">::</span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">current_process_id</span></span></span></span>());
int ret;
<span class="hljs-class"><span class="hljs-keyword"><span class="hljs-class"><span class="hljs-keyword">struct</span></span></span><span class="hljs-class"> </span><span class="hljs-title"><span class="hljs-class"><span class="hljs-title">stat64</span></span></span></span> st;
RESTARTABLE(::stat64(<span class="hljs-function"><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-keyword">fn</span></span></span><span class="hljs-function">, &amp;</span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">st</span></span></span><span class="hljs-function">), </span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">ret</span></span></span><span class="hljs-function">);
</span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">if</span></span></span><span class="hljs-function"> </span></span>(ret == -<span class="hljs-number"><span class="hljs-number">1</span></span>) {
log_trace(attach)(<span class="hljs-string"><span class="hljs-string">"Failed to find attach file: %s, trying alternate"</span></span>, <span class="hljs-function"><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-keyword">fn</span></span></span><span class="hljs-function">);
</span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">snprintf</span></span></span></span>(<span class="hljs-function"><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-keyword">fn</span></span></span><span class="hljs-function">, </span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">sizeof</span></span></span></span>(<span class="hljs-function"><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-keyword">fn</span></span></span><span class="hljs-function">), "%</span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">s</span></span></span><span class="hljs-function">/.</span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">attach_pid</span></span></span><span class="hljs-function">%</span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">d</span></span></span><span class="hljs-function">", </span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">os</span></span></span><span class="hljs-function">::</span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">get_temp_directory</span></span></span></span>(), os::current_process_id());
RESTARTABLE(::stat64(<span class="hljs-function"><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-keyword">fn</span></span></span><span class="hljs-function">, &amp;</span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">st</span></span></span><span class="hljs-function">), </span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">ret</span></span></span><span class="hljs-function">);
}
</span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">if</span></span></span><span class="hljs-function"> </span></span>(ret == <span class="hljs-number"><span class="hljs-number">0</span></span>) {
<span class="hljs-comment"><span class="hljs-comment">// simple check to avoid starting the attach mechanism when</span></span>
<span class="hljs-comment"><span class="hljs-comment">// a bogus user creates the file</span></span>
<span class="hljs-keyword"><span class="hljs-keyword">if</span></span> (st.st_uid == geteuid()) {
init();
<span class="hljs-keyword"><span class="hljs-keyword">return</span></span> <span class="hljs-literal"><span class="hljs-literal">true</span></span>;
}
}
<span class="hljs-keyword"><span class="hljs-keyword">return</span></span> <span class="hljs-literal"><span class="hljs-literal">false</span></span>;
}
</code></pre><p>首先检查了一下是否在JVM启动时启动了Attach Listener，或者是否已经启动过。如果没有，才继续执行，在/tmp目录下创建一个叫做.attach_pid%d的文件，然后执行AttachListener的init函数，这个函数就是用来创建Attach Listener线程的函数，上面已经提到多次并进行了分析。到此，我们知道Attach机制的奥秘所在，也就是Attach Listener线程的创建依靠Signal Dispatcher线程，Signal Dispatcher是用来处理信号的线程，当Signal Dispatcher线程接收到“SIGBREAK”信号之后，就会执行初始化Attach Listener的工作。</p><h3 id="2-3-2-运行时加载agent的实现">2.3.2 运行时加载Agent的实现</h3><p>我们继续分析，到底是如何将一个Agent挂载到运行着的目标JVM上，在上文中提到了一段代码，用来进行运行时挂载Agent，可以参考上文中展示的关于“attachAgentToTargetJvm”方法的代码。这个方法里面的关键是调用VirtualMachine的attach方法进行Agent挂载的功能。下面我们就来分析一下VirtualMachine的attach方法具体是怎么实现的。</p><pre><code class="hljs java">    <span class="hljs-function"><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-keyword">public</span></span></span><span class="hljs-function"> </span><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-keyword">static</span></span></span><span class="hljs-function"> VirtualMachine </span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">attach</span></span></span><span class="hljs-params"><span class="hljs-function"><span class="hljs-params">(String var0)</span></span></span><span class="hljs-function"> </span><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-keyword">throws</span></span></span><span class="hljs-function"> AttachNotSupportedException, IOException </span></span>{
<span class="hljs-keyword"><span class="hljs-keyword">if</span></span> (var0 == <span class="hljs-keyword"><span class="hljs-keyword">null</span></span>) {
<span class="hljs-keyword"><span class="hljs-keyword">throw</span></span> <span class="hljs-keyword"><span class="hljs-keyword">new</span></span> NullPointerException(<span class="hljs-string"><span class="hljs-string">"id cannot be null"</span></span>);
} <span class="hljs-keyword"><span class="hljs-keyword">else</span></span> {
List var1 = AttachProvider.providers();
<span class="hljs-keyword"><span class="hljs-keyword">if</span></span> (var1.size() == <span class="hljs-number"><span class="hljs-number">0</span></span>) {
<span class="hljs-keyword"><span class="hljs-keyword">throw</span></span> <span class="hljs-keyword"><span class="hljs-keyword">new</span></span> AttachNotSupportedException(<span class="hljs-string"><span class="hljs-string">"no providers installed"</span></span>);
} <span class="hljs-keyword"><span class="hljs-keyword">else</span></span> {
AttachNotSupportedException var2 = <span class="hljs-keyword"><span class="hljs-keyword">null</span></span>;
Iterator var3 = var1.iterator();
<span class="hljs-keyword"><span class="hljs-keyword">while</span></span>(var3.hasNext()) {
AttachProvider var4 = (AttachProvider)var3.next();
<span class="hljs-keyword"><span class="hljs-keyword">try</span></span> {
<span class="hljs-keyword"><span class="hljs-keyword">return</span></span> var4.attachVirtualMachine(var0);
} <span class="hljs-keyword"><span class="hljs-keyword">catch</span></span> (AttachNotSupportedException var6) {
var2 = var6;
}
}
<span class="hljs-keyword"><span class="hljs-keyword">throw</span></span> var2;
}
}
}
</code></pre><p>这个方法通过attachVirtualMachine方法进行attach操作，在MacOS系统中，AttachProvider的实现类是BsdAttachProvider。我们来看一下BsdAttachProvider的attachVirtualMachine方法是如何实现的：</p><pre><code class="hljs java"><span class="hljs-function"><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-keyword">public</span></span></span><span class="hljs-function"> VirtualMachine </span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">attachVirtualMachine</span></span></span><span class="hljs-params"><span class="hljs-function"><span class="hljs-params">(String var1)</span></span></span><span class="hljs-function"> </span><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-keyword">throws</span></span></span><span class="hljs-function"> AttachNotSupportedException, IOException </span></span>{
<span class="hljs-keyword"><span class="hljs-keyword">this</span></span>.checkAttachPermission();
<span class="hljs-keyword"><span class="hljs-keyword">this</span></span>.testAttachable(var1);
<span class="hljs-keyword"><span class="hljs-keyword">return</span></span> <span class="hljs-keyword"><span class="hljs-keyword">new</span></span> BsdVirtualMachine(<span class="hljs-keyword"><span class="hljs-keyword">this</span></span>, var1);
}
BsdVirtualMachine(AttachProvider var1, String var2) <span class="hljs-keyword"><span class="hljs-keyword">throws</span></span> AttachNotSupportedException, IOException {
<span class="hljs-keyword"><span class="hljs-keyword">int</span></span> var3 = Integer.parseInt(var2);
<span class="hljs-keyword"><span class="hljs-keyword">this</span></span>.path = <span class="hljs-keyword"><span class="hljs-keyword">this</span></span>.findSocketFile(var3);
<span class="hljs-keyword"><span class="hljs-keyword">if</span></span> (<span class="hljs-keyword"><span class="hljs-keyword">this</span></span>.path == <span class="hljs-keyword"><span class="hljs-keyword">null</span></span>) {
File var4 = <span class="hljs-keyword"><span class="hljs-keyword">new</span></span> File(tmpdir, <span class="hljs-string"><span class="hljs-string">".attach_pid"</span></span> + var3);
createAttachFile(var4.getPath());
<span class="hljs-keyword"><span class="hljs-keyword">try</span></span> {
sendQuitTo(var3);
<span class="hljs-keyword"><span class="hljs-keyword">int</span></span> var5 = <span class="hljs-number"><span class="hljs-number">0</span></span>;
<span class="hljs-keyword"><span class="hljs-keyword">long</span></span> var6 = <span class="hljs-number"><span class="hljs-number">200L</span></span>;
<span class="hljs-keyword"><span class="hljs-keyword">int</span></span> var8 = (<span class="hljs-keyword"><span class="hljs-keyword">int</span></span>)(<span class="hljs-keyword"><span class="hljs-keyword">this</span></span>.attachTimeout() / var6);
<span class="hljs-keyword"><span class="hljs-keyword">do</span></span> {
<span class="hljs-keyword"><span class="hljs-keyword">try</span></span> {
Thread.sleep(var6);
} <span class="hljs-keyword"><span class="hljs-keyword">catch</span></span> (InterruptedException var21) {
;
}
<span class="hljs-keyword"><span class="hljs-keyword">this</span></span>.path = <span class="hljs-keyword"><span class="hljs-keyword">this</span></span>.findSocketFile(var3);
++var5;
} <span class="hljs-keyword"><span class="hljs-keyword">while</span></span>(var5 &lt;= var8 &amp;&amp; <span class="hljs-keyword"><span class="hljs-keyword">this</span></span>.path == <span class="hljs-keyword"><span class="hljs-keyword">null</span></span>);
} <span class="hljs-keyword"><span class="hljs-keyword">finally</span></span> {
var4.delete();
}
}
<span class="hljs-keyword"><span class="hljs-keyword">int</span></span> var24 = socket();
connect(var24, <span class="hljs-keyword"><span class="hljs-keyword">this</span></span>.path);
}
<span class="hljs-function"><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-keyword">private</span></span></span><span class="hljs-function"> String </span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">findSocketFile</span></span></span><span class="hljs-params"><span class="hljs-function"><span class="hljs-params">(</span></span><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-params"><span class="hljs-keyword">int</span></span></span></span><span class="hljs-function"><span class="hljs-params"> var1)</span></span></span><span class="hljs-function"> </span></span>{
String var2 = <span class="hljs-string"><span class="hljs-string">".java_pid"</span></span> + var1;
File var3 = <span class="hljs-keyword"><span class="hljs-keyword">new</span></span> File(tmpdir, var2);
<span class="hljs-keyword"><span class="hljs-keyword">return</span></span> var3.exists() ? var3.getPath() : <span class="hljs-keyword"><span class="hljs-keyword">null</span></span>;
}
</code></pre><p>findSocketFile方法用来查询目标JVM上是否已经启动了Attach Listener，它通过检查”tmp/“目录下是否存在java_pid{pid}来进行实现。如果已经存在了，则说明Attach机制已经准备就绪，可以接受客户端的命令了，这个时候客户端就可以通过connect连接到目标JVM进行命令的发送，比如可以发送“load”命令来加载Agent。如果java_pid{pid}文件还不存在，则需要通过sendQuitTo方法向目标JVM发送一个“SIGBREAK”信号，让它初始化Attach Listener线程并准备接受客户端连接。可以看到，发送了信号之后客户端会循环等待java_pid{pid}这个文件，之后再通过connect连接到目标JVM上。</p><h3 id="2-3-3-load命令的实现">2.3.3 load命令的实现</h3><p>下面来分析一下，“load”命令在JVM层面的实现：</p><pre><code class="hljs rust"><span class="hljs-keyword"><span class="hljs-keyword">static</span></span> jint load_agent(AttachOperation* op, outputStream* out) {
<span class="hljs-comment"><span class="hljs-comment">// get agent name and options</span></span>
<span class="hljs-keyword"><span class="hljs-keyword">const</span></span> <span class="hljs-built_in"><span class="hljs-built_in">char</span></span>* agent = op-&gt;arg(<span class="hljs-number"><span class="hljs-number">0</span></span>);
<span class="hljs-keyword"><span class="hljs-keyword">const</span></span> <span class="hljs-built_in"><span class="hljs-built_in">char</span></span>* absParam = op-&gt;arg(<span class="hljs-number"><span class="hljs-number">1</span></span>);
<span class="hljs-keyword"><span class="hljs-keyword">const</span></span> <span class="hljs-built_in"><span class="hljs-built_in">char</span></span>* options = op-&gt;arg(<span class="hljs-number"><span class="hljs-number">2</span></span>);
<span class="hljs-comment"><span class="hljs-comment">// If loading a java agent then need to ensure that the java.instrument module is loaded</span></span>
<span class="hljs-keyword"><span class="hljs-keyword">if</span></span> (strcmp(agent, <span class="hljs-string"><span class="hljs-string">"instrument"</span></span>) == <span class="hljs-number"><span class="hljs-number">0</span></span>) {
Thread* THREAD = Thread::current();
ResourceMark rm(THREAD);
HandleMark hm(THREAD);
JavaValue result(T_OBJECT);
Handle h_module_name = java_lang_String::create_from_str(<span class="hljs-string"><span class="hljs-string">"java.instrument"</span></span>, THREAD);
JavaCalls::call_static(&amp;result,SystemDictionary::module_Modules_klass(),vmSymbols::loadModule_name(),
vmSymbols::loadModule_signature(),h_module_name,THREAD);
}
<span class="hljs-keyword"><span class="hljs-keyword">return</span></span> JvmtiExport::load_agent_library(agent, absParam, options, out);
}
</code></pre><p>这个函数先确保加载了java.instrument模块，之后真正执行Agent加载的函数是 <a href="https://github.com/pandening/openjdk/blob/0301fc792ffd3c7b506ef78887af250e0e3ae09e/src/hotspot/share/prims/jvmtiExport.cpp#L2476">load_agent_library<i class="fa fa-link" aria-hidden="true"></i></a> ,这个函数的套路就是加载Agent动态链接库，如果是通过Java instrument API实现的Agent，则加载的是libinstrument动态链接库，然后通过libinstrument里面的代码实现运行agentmain方法的逻辑，这一部分内容和libinstrument实现premain方法运行的逻辑其实差不多，这里不再做分析。至此，我们对Java Agent技术已经有了一个全面而细致的了解。</p><h2 id="3-1-动态字节码修改的限制">3.1 动态字节码修改的限制</h2><p>上文中已经详细分析了Agent技术的实现，我们使用Java Instrumentation API来完成动态类修改的功能，在Instrumentation接口中，通过addTransformer方法来增加一个类转换器，类转换器由类ClassFileTransformer接口实现。ClassFileTransformer接口中唯一的方法transform用于实现类转换，当类被加载的时候，就会调用transform方法，进行类转换。在运行时，我们可以通过Instrumentation的redefineClasses方法进行类重定义，在方法上有一段注释需要特别注意：</p><pre><code class="hljs coffeescript">     * The redefinition may change method bodies, the constant pool <span class="hljs-keyword"><span class="hljs-keyword">and</span></span> attributes.
* The redefinition must <span class="hljs-keyword"><span class="hljs-keyword">not</span></span> add, remove <span class="hljs-keyword"><span class="hljs-keyword">or</span></span> rename fields <span class="hljs-keyword"><span class="hljs-keyword">or</span></span> methods, change the
* signatures <span class="hljs-keyword"><span class="hljs-keyword">of</span></span> methods, <span class="hljs-keyword"><span class="hljs-keyword">or</span></span> change inheritance.  These restrictions maybe be
* lifted <span class="hljs-keyword"><span class="hljs-keyword">in</span></span> future versions.  The <span class="hljs-class"><span class="hljs-keyword"><span class="hljs-class"><span class="hljs-keyword">class</span></span></span><span class="hljs-class"> </span><span class="hljs-title"><span class="hljs-class"><span class="hljs-title">file</span></span></span><span class="hljs-class"> </span><span class="hljs-title"><span class="hljs-class"><span class="hljs-title">bytes</span></span></span><span class="hljs-class"> </span><span class="hljs-title"><span class="hljs-class"><span class="hljs-title">are</span></span></span><span class="hljs-class"> </span><span class="hljs-title"><span class="hljs-class"><span class="hljs-title">not</span></span></span><span class="hljs-class"> </span><span class="hljs-title"><span class="hljs-class"><span class="hljs-title">checked</span></span></span><span class="hljs-class">, </span><span class="hljs-title"><span class="hljs-class"><span class="hljs-title">verified</span></span></span><span class="hljs-class"> </span><span class="hljs-title"><span class="hljs-class"><span class="hljs-title">and</span></span></span><span class="hljs-class"> </span><span class="hljs-title"><span class="hljs-class"><span class="hljs-title">installed</span></span></span></span>
* <span class="hljs-keyword"><span class="hljs-keyword">until</span></span> after the transformations have been applied, <span class="hljs-keyword"><span class="hljs-keyword">if</span></span> the resultant bytes are <span class="hljs-keyword"><span class="hljs-keyword">in</span></span>
* error <span class="hljs-keyword"><span class="hljs-keyword">this</span></span> method will <span class="hljs-keyword"><span class="hljs-keyword">throw</span></span> an exception.
</code></pre><p>这里面提到，我们不可以增加、删除或者重命名字段和方法，改变方法的签名或者类的继承关系。认识到这一点很重要，当我们通过ASM获取到增强的字节码之后，如果增强后的字节码没有遵守这些规则，那么调用redefineClasses方法来进行类的重定义就会失败。那redefineClasses方法具体是怎么实现类的重定义的呢？它对运行时的JVM会造成什么样的影响呢？下面来分析redefineClasses的实现细节。</p><h2 id="3-2-重定义类字节码的实现细节">3.2 重定义类字节码的实现细节</h2><p>上文中我们提到，libinstrument动态链接库中，JPLISAgent不仅实现了Agent入口代码执行的路由，而且还是Java代码与JVMTI之间的一道桥梁。我们在Java代码中调用Java Instrumentation API的redefineClasses，其实会调用libinstrument中的相关代码，我们来分析一下这条路径。</p><pre><code class="hljs java">    <span class="hljs-function"><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-keyword">public</span></span></span><span class="hljs-function"> </span><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-keyword">void</span></span></span><span class="hljs-function"> </span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">redefineClasses</span></span></span><span class="hljs-params"><span class="hljs-function"><span class="hljs-params">(ClassDefinition... var1)</span></span></span><span class="hljs-function"> </span><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-keyword">throws</span></span></span><span class="hljs-function"> ClassNotFoundException </span></span>{
<span class="hljs-keyword"><span class="hljs-keyword">if</span></span> (!<span class="hljs-keyword"><span class="hljs-keyword">this</span></span>.isRedefineClassesSupported()) {
<span class="hljs-keyword"><span class="hljs-keyword">throw</span></span> <span class="hljs-keyword"><span class="hljs-keyword">new</span></span> UnsupportedOperationException(<span class="hljs-string"><span class="hljs-string">"redefineClasses is not supported in this environment"</span></span>);
} <span class="hljs-keyword"><span class="hljs-keyword">else</span></span> <span class="hljs-keyword"><span class="hljs-keyword">if</span></span> (var1 == <span class="hljs-keyword"><span class="hljs-keyword">null</span></span>) {
<span class="hljs-keyword"><span class="hljs-keyword">throw</span></span> <span class="hljs-keyword"><span class="hljs-keyword">new</span></span> NullPointerException(<span class="hljs-string"><span class="hljs-string">"null passed as 'definitions' in redefineClasses"</span></span>);
} <span class="hljs-keyword"><span class="hljs-keyword">else</span></span> {
<span class="hljs-keyword"><span class="hljs-keyword">for</span></span>(<span class="hljs-keyword"><span class="hljs-keyword">int</span></span> var2 = <span class="hljs-number"><span class="hljs-number">0</span></span>; var2 &lt; var1.length; ++var2) {
<span class="hljs-keyword"><span class="hljs-keyword">if</span></span> (var1[var2] == <span class="hljs-keyword"><span class="hljs-keyword">null</span></span>) {
<span class="hljs-keyword"><span class="hljs-keyword">throw</span></span> <span class="hljs-keyword"><span class="hljs-keyword">new</span></span> NullPointerException(<span class="hljs-string"><span class="hljs-string">"element of 'definitions' is null in redefineClasses"</span></span>);
}
}
<span class="hljs-keyword"><span class="hljs-keyword">if</span></span> (var1.length != <span class="hljs-number"><span class="hljs-number">0</span></span>) {
<span class="hljs-keyword"><span class="hljs-keyword">this</span></span>.redefineClasses0(<span class="hljs-keyword"><span class="hljs-keyword">this</span></span>.mNativeAgent, var1);
}
}
}
<span class="hljs-function"><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-keyword">private</span></span></span><span class="hljs-function"> </span><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-keyword">native</span></span></span><span class="hljs-function"> </span><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-keyword">void</span></span></span><span class="hljs-function"> </span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">redefineClasses0</span></span></span><span class="hljs-params"><span class="hljs-function"><span class="hljs-params">(</span></span><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-params"><span class="hljs-keyword">long</span></span></span></span><span class="hljs-function"><span class="hljs-params"> var1, ClassDefinition[] var3)</span></span></span><span class="hljs-function"> </span><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-keyword">throws</span></span></span><span class="hljs-function"> ClassNotFoundException</span></span>;
</code></pre><p>这是InstrumentationImpl中的redefineClasses实现，该方法的具体实现依赖一个Native方法redefineClasses()，我们可以在libinstrument中找到这个Native方法的实现：</p><pre><code class="hljs cpp"><span class="hljs-function"><span class="hljs-function">JNIEXPORT </span><span class="hljs-keyword"><span class="hljs-function"><span class="hljs-keyword">void</span></span></span><span class="hljs-function"> JNICALL </span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">Java_sun_instrument_InstrumentationImpl_redefineClasses0</span></span></span><span class="hljs-function">
</span><span class="hljs-params"><span class="hljs-function"><span class="hljs-params">(JNIEnv * jnienv, jobject implThis, jlong agent, jobjectArray classDefinitions)</span></span></span><span class="hljs-function"> </span></span>{
redefineClasses(jnienv, (JPLISAgent*)(<span class="hljs-keyword"><span class="hljs-keyword">intptr_t</span></span>)agent, classDefinitions);
}
</code></pre><p>redefineClasses这个函数的实现比较复杂，代码很长。下面是一段关键的代码片段：</p><p><img src="https://p0.meituan.net/travelcube/6de93ba8ec845e85c59cce95eadbf767159682.png" alt=""></p><p>可以看到，其实是调用了JVMTI的RetransformClasses函数来完成类的重定义细节。</p><pre><code class="hljs cpp"><span class="hljs-comment"><span class="hljs-comment">// class_count - pre-checked to be greater than or equal to 0</span></span>
<span class="hljs-comment"><span class="hljs-comment">// class_definitions - pre-checked for NULL</span></span>
jvmtiError JvmtiEnv::RedefineClasses(jint class_count, <span class="hljs-keyword"><span class="hljs-keyword">const</span></span> jvmtiClassDefinition* class_definitions) {
<span class="hljs-comment"><span class="hljs-comment">//</span><span class="hljs-doctag"><span class="hljs-comment"><span class="hljs-doctag">TODO:</span></span></span><span class="hljs-comment"> add locking</span></span>
<span class="hljs-function"><span class="hljs-function">VM_RedefineClasses </span><span class="hljs-title"><span class="hljs-function"><span class="hljs-title">op</span></span></span><span class="hljs-params"><span class="hljs-function"><span class="hljs-params">(class_count, class_definitions, jvmti_class_load_kind_redefine)</span></span></span></span>;
VMThread::execute(&amp;op);
<span class="hljs-keyword"><span class="hljs-keyword">return</span></span> (op.check_error());
} <span class="hljs-comment"><span class="hljs-comment">/* end RedefineClasses */</span></span>
</code></pre><p>重定义类的请求会被JVM包装成一个VM_RedefineClasses类型的VM_Operation，VM_Operation是JVM内部的一些操作的基类，包括GC操作等。VM_Operation由VMThread来执行，新的VM_Operation操作会被添加到VMThread的运行队列中去，VMThread会不断从队列里面拉取VM_Operation并调用其doit等函数执行具体的操作。VM_RedefineClasses函数的流程较为复杂，下面是VM_RedefineClasses的大致流程：</p><ul><li>加载新的字节码，合并常量池，并且对新的字节码进行校验工作</li></ul><pre><code class="hljs less">  <span class="hljs-comment"><span class="hljs-comment">// Load the caller's new class definition(s) into _scratch_classes.</span></span>
<span class="hljs-comment"><span class="hljs-comment">// Constant pool merging work is done here as needed. Also calls</span></span>
<span class="hljs-comment"><span class="hljs-comment">// compare_and_normalize_class_versions() to verify the class</span></span>
<span class="hljs-comment"><span class="hljs-comment">// definition(s).</span></span>
<span class="hljs-selector-tag"><span class="hljs-selector-tag">jvmtiError</span></span> <span class="hljs-selector-tag"><span class="hljs-selector-tag">load_new_class_versions</span></span>(TRAPS);
</code></pre><ul><li>清除方法上的断点</li></ul><pre><code class="hljs coffeescript">  <span class="hljs-regexp"><span class="hljs-regexp">//</span></span> Remove all breakpoints <span class="hljs-keyword"><span class="hljs-keyword">in</span></span> methods <span class="hljs-keyword"><span class="hljs-keyword">of</span></span> <span class="hljs-keyword"><span class="hljs-keyword">this</span></span> <span class="hljs-class"><span class="hljs-keyword"><span class="hljs-class"><span class="hljs-keyword">class</span></span></span></span>
JvmtiBreakpoints&amp; jvmti_breakpoints = JvmtiCurrentBreakpoints::get_jvmti_breakpoints();
jvmti_breakpoints.clearall_in_class_at_safepoint(the_class());
</code></pre><ul><li>JIT逆优化</li></ul><pre><code class="hljs coffeescript">  <span class="hljs-regexp"><span class="hljs-regexp">//</span></span> Deoptimize all compiled code that depends <span class="hljs-literal"><span class="hljs-literal">on</span></span> <span class="hljs-keyword"><span class="hljs-keyword">this</span></span> <span class="hljs-class"><span class="hljs-keyword"><span class="hljs-class"><span class="hljs-keyword">class</span></span></span></span>
flush_dependent_code(the_class, THREAD);
</code></pre><ul><li>进行字节码替换工作，需要进行更新类itable/vtable等操作</li><li>进行类重定义通知</li></ul><pre><code class="hljs properties">  <span class="hljs-attr"><span class="hljs-attr">SystemDictionary</span></span>:<span class="hljs-string"><span class="hljs-string">:notice_modification();</span></span>
</code></pre><p>VM_RedefineClasses实现比较复杂的，详细实现可以参考 <a href="https://github.com/pandening/openjdk/blob/0301fc792ffd3c7b506ef78887af250e0e3ae09e/src/hotspot/share/prims/jvmtiEnv.cpp#L456">RedefineClasses的实现<i class="fa fa-link" aria-hidden="true"></i></a>。</p><p>Java-debug-tool是一个使用Java Instrument API来实现的动态调试工具，它通过在目标JVM上启动一个TcpServer来和调试客户端通信。调试客户端通过命令行来发送调试命令给TcpServer，TcpServer中有专门用来处理命令的handler，handler处理完命令之后会将结果发送回客户端，客户端通过处理将调试结果展示出来。下面将详细介绍Java-debug-tool的整体设计和实现。</p><h2 id="4-1-java-debug-tool整体架构">4.1 Java-debug-tool整体架构</h2><p>Java-debug-tool包括一个Java Agent和一个用于处理调试命令的核心API，核心API通过一个自定义的类加载器加载进来，以保证目标JVM的类不会被污染。整体上Java-debug-tool的设计是一个Client-Server的架构，命令客户端需要完整的完成一个命令之后才能继续执行下一个调试命令。Java-debug-tool支持多人同时进行调试，下面是整体架构图：</p><p><img src="https://p0.meituan.net/travelcube/810f16bba746dcf85c788037e9138a6c78210.png" alt="">
图4-1-1</p><p>下面对每一层做简单介绍：</p><ul><li>交互层：负责将程序员的输入转换成调试交互协议，并且将调试信息呈现出来。</li><li>连接管理层：负责管理客户端连接，从连接中读调试协议数据并解码，对调试结果编码并将其写到连接中去；同时将那些超时未活动的连接关闭。<br></li><li>业务逻辑层：实现调试命令处理，包括命令分发、数据收集、数据处理等过程。</li><li>基础实现层：Java-debug-tool实现的底层依赖，通过Java Instrumentation提供的API进行类查找、类重定义等能力，Java Instrumentation底层依赖JVMTI来完成具体的功能。</li></ul><p>在Agent被挂载到目标JVM上之后，Java-debug-tool会安排一个Spy在目标JVM内活动，这个Spy负责将目标JVM内部的相关调试数据转移到命令处理模块，命令处理模块会处理这些数据，然后给客户端返回调试结果。命令处理模块会增强目标类的字节码来达到数据获取的目的，多个客户端可以共享一份增强过的字节码，无需重复增强。下面从Java-debug-tool的字节码增强方案、命令设计与实现等角度详细说明。</p><h2 id="4-2-java-debug-tool的字节码增强方案">4.2 Java-debug-tool的字节码增强方案</h2><p>Java-debug-tool使用字节码增强来获取到方法运行时的信息，比如方法入参、出参等，可以在不同的字节码位置进行增强，这种行为可以称为“插桩”，每个“桩”用于获取数据并将他转储出去。Java-debug-tool具备强大的插桩能力，不同的桩负责获取不同类别的数据，下面是Java-debug-tool目前所支持的“桩”：</p><ul><li>方法进入点：用于获取方法入参信息。</li><li>Fields获取点1：在方法执行前获取到对象的字段信息。</li><li>变量存储点：获取局部变量信息。</li><li>Fields获取点2：在方法退出前获取到对象的字段信息。</li><li>方法退出点：用于获取方法返回值。</li><li>抛出异常点：用于获取方法抛出的异常信息。</li></ul><p>通过上面这些代码桩，Java-debug-tool可以收集到丰富的方法执行信息，经过处理可以返回更加可视化的调试结果。</p><h3 id="4-2-1-字节码增强">4.2.1 字节码增强</h3><p>Java-debug-tool在实现上使用了ASM工具来进行字节码增强，并且每个插桩点都可以进行配置，如果不想要什么信息，则没必要进行对应的插桩操作。这种可配置的设计是非常有必要的，因为有时候我们仅仅是想要知道方法的入参和出参，但Java-debug-tool却给我们返回了所有的调试信息，这样我们就得在众多的输出中找到我们所关注的内容。如果可以进行配置，则除了入参点和出参点外其他的桩都不插，那么就可以快速看到我们想要的调试数据，这种设计的本质是为了让调试者更加专注。下面是Java-debug-tool的字节码增强工作方式：</p><p><img src="https://p0.meituan.net/travelcube/fee09f1b0713c5b29a83d815f72d8d1b73306.png" alt="">
图4-2-1</p><p>如图4-2-1所示，当调试者发出调试命令之后，Java-debug-tool会识别命令并判断是否需要进行字节码增强，如果命令需要增强字节码，则判断当前类+当前方法是否已经被增强过。上文已经提到，字节码替换是有一定损耗的，这种具有损耗的操作发生的次数越少越好，所以字节码替换操作会被记录起来，后续命令直接使用即可，不需要重复进行字节码增强，字节码增强还涉及多个调试客户端的协同工作问题，当一个客户端增强了一个类的字节码之后，这个客户端就锁定了该字节码，其他客户端变成只读，无法对该类进行字节码增强，只有当持有锁的客户端主动释放锁或者断开连接之后，其他客户端才能继续增强该类的字节码。</p><p>字节码增强模块收到字节码增强请求之后，会判断每个增强点是否需要插桩，这个判断的根据就是上文提到的插桩配置，之后字节码增强模块会生成新的字节码，Java-debug-tool将执行字节码替换操作，之后就可以进行调试数据收集了。</p><p>经过字节码增强之后，原来的方法中会插入收集运行时数据的代码，这些代码在方法被调用的时候执行，获取到诸如方法入参、局部变量等信息，这些信息将传递给数据收集装置进行处理。数据收集的工作通过Advice完成，每个客户端同一时间只能注册一个Advice到Java-debug-tool调试模块上，多个客户端可以同时注册自己的Advice到调试模块上。Advice负责收集数据并进行判断，如果当前数据符合调试命令的要求，Java-debug-tool就会卸载这个Advice，Advice的数据就会被转移到Java-debug-tool的命令结果处理模块进行处理，并将结果发送到客户端。</p><h3 id="4-2-2-advice的工作方式">4.2.2 Advice的工作方式</h3><p>Advice是调试数据收集器，不同的调试策略会对应不同的Advice。Advice是工作在目标JVM的线程内部的，它需要轻量级和高效，意味着Advice不能做太过于复杂的事情，它的核心接口“match”用来判断本次收集到的调试数据是否满足调试需求。如果满足，那么Java-debug-tool就会将其卸载，否则会继续让他收集调试数据，这种“加载Advice” -&gt; “卸载Advice”的工作模式具备很好的灵活性。</p><p>关于Advice，需要说明的另外一点就是线程安全，因为它加载之后会运行在目标JVM的线程中，目标JVM的方法极有可能是多线程访问的，这也就是说，Advice需要有能力处理多个线程同时访问方法的能力，如果Advice处理不当，则可能会收集到杂乱无章的调试数据。下面的图片展示了Advice和Java-debug-tool调试分析模块、目标方法执行以及调试客户端等模块的关系。</p><p><img src="https://p0.meituan.net/travelcube/5f706f805ed236e065c0dd706ecbc13966920.png" alt="">
图4-2-2</p><p>Advice的首次挂载由Java-debug-tool的命令处理器完成，当一次调试数据收集完成之后，调试数据处理模块会自动卸载Advice，然后进行判断，如果调试数据符合Advice的策略，则直接将数据交由数据处理模块进行处理，否则会清空调试数据，并再次将Advice挂载到目标方法上去，等待下一次调试数据。非首次挂载由调试数据处理模块进行，它借助Advice按需取数据，如果不符合需求，则继续挂载Advice来获取数据，否则对调试数据进行处理并返回给客户端。</p><h2 id="4-3-java-debug-tool的命令设计与实现">4.3 Java-debug-tool的命令设计与实现</h2><h3 id="4-3-1-命令执行">4.3.1 命令执行</h3><p>上文已经完整的描述了Java-debug-tool的设计以及核心技术方案，本小节将详细介绍Java-debug-tool的命令设计与实现。首先需要将一个调试命令的执行流程描述清楚，下面是一张用来表示命令请求处理流程的图片：</p><p><img src="https://p0.meituan.net/travelcube/36e6a522b3859c04ad3f04733c56944e44029.png" alt="">
图4-3-1</p><p>图4-3-1简单的描述了Java-debug-tool的命令处理方式，客户端连接到服务端之后，会进行一些协议解析、协议认证、协议填充等工作，之后将进行命令分发。服务端如果发现客户端的命令不合法，则会立即返回错误信息，否则再进行命令处理。命令处理属于典型的三段式处理，前置命令处理、命令处理以及后置命令处理，同时会对命令处理过程中的异常信息进行捕获处理，三段式处理的好处是命令处理被拆成了多个阶段，多个阶段负责不同的职责。前置命令处理用来做一些命令权限控制的工作，并填充一些类似命令处理开始时间戳等信息，命令处理就是通过字节码增强，挂载Advice进行数据收集，再经过数据处理来产生命令结果的过程，后置处理则用来处理一些连接关闭、字节码解锁等事项。</p><p>Java-debug-tool允许客户端设置一个命令执行超时时间，超过这个时间则认为命令没有结果，如果客户端没有设置自己的超时时间，就使用默认的超时时间进行超时控制。Java-debug-tool通过设计了两阶段的超时检测机制来实现命令执行超时功能：首先，第一阶段超时触发，则Java-debug-tool会友好的警告命令处理模块处理时间已经超时，需要立即停止命令执行，这允许命令自己做一些现场清理工作，当然需要命令执行线程自己感知到这种超时警告；当第二阶段超时触发，则Java-debug-tool认为命令必须结束执行，会强行打断命令执行线程。超时机制的目的是为了不让命令执行太长时间，命令如果长时间没有收集到调试数据，则应该停止执行，并思考是否调试了一个错误的方法。当然，超时机制还可以定期清理那些因为未知原因断开连接的客户端持有的调试资源，比如字节码锁。</p><h3 id="4-3-4-获取方法执行视图">4.3.4 获取方法执行视图</h3><p>Java-debug-tool通过下面的信息来向调试者呈现出一次方法执行的视图：</p><ul><li>正在调试的方法信息。</li><li>方法调用堆栈。</li><li>调试耗时，包括对目标JVM造成的STW时间。</li><li>方法入参，包括入参的类型及参数值。</li><li>方法的执行路径。</li><li>代码执行耗时。</li><li>局部变量信息。</li><li>方法返回结果。</li><li>方法抛出的异常。</li><li>对象字段值快照。</li></ul><p>图4-3-2展示了Java-debug-tool获取到正在运行的方法的执行视图的信息。</p><p><img src="https://p0.meituan.net/travelcube/837d0bcebbe648561db146699ed0f412103888.png" alt="">
图4-3-2</p><h2 id="4-4-java-debug-tool与同类产品对比分析">4.4 Java-debug-tool与同类产品对比分析</h2><p>Java-debug-tool的同类产品主要是greys，其他类似的工具大部分都是基于greys进行的二次开发，所以直接选择greys来和Java-debug-tool进行对比。</p><p><img src="https://p0.meituan.net/travelcube/e8dd64a45a4283e3cb1a8d42c684c7321315030.jpg" alt=""></p><p>本文详细剖析了Java动态调试关键技术的实现细节，并介绍了我们基于Java动态调试技术结合实际故障排查场景进行的一点探索实践；动态调试技术为研发人员进行线上问题排查提供了一种新的思路，我们基于动态调试技术解决了传统断点调试存在的问题，使得可以将断点调试这种技术应用在线上，以线下调试的思维来进行线上调试，提高问题排查效率。</p><h2 id="六-参考文献">六 参考文献</h2><ul><li><a href="https://asm.ow2.io/asm4-guide.pdf">ASM 4 guide<i class="fa fa-link" aria-hidden="true"></i></a></li><li><a href="https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html">Java Virtual Machine Specification<i class="fa fa-link" aria-hidden="true"></i></a></li><li><a href="https://docs.oracle.com/javase/8/docs/platform/jvmti/jvmti.html">JVM Tool Interface<i class="fa fa-link" aria-hidden="true"></i></a></li><li><a href="https://alibaba.github.io/arthas/">alibaba arthas<i class="fa fa-link" aria-hidden="true"></i></a></li><li><a href="https://github.com/pandening/openjdk">openjdk<i class="fa fa-link" aria-hidden="true"></i></a></li></ul><h2 id="作者简介">作者简介</h2><p>胡健，美团到店餐饮研发中心研发工程师。</p><h2 id="招聘">招聘</h2><p>美团到店餐饮研发中心支撑了美团核心业务–餐饮团购业务。团队核心成员并肩作战与美团一起打赢了千团大战，实力爆表，更有来自知名互联网公司的各路大牛加盟支持。团队正在经历从优秀到卓越的蜕变，挑战多，发展空间大。期待优秀的你加入我们，共同努力，让所有人吃的更好、生活更好！
美团到店餐饮研发中心诚聘Java高级工程师、架构师、专家，欢迎有兴趣的同学投递简历到tech@meituan.com（邮件标题注明：美团到店餐饮研发中心）</p></div></div><div class="meta-box post-bottom-meta-box hidden-print"><span class="tag-links"><i class="fa fa-tags" aria-hidden="true"></i><a href="/tags/%E5%88%B0%E5%BA%97.html" rel="tag">到店</a>, <a href="/tags/java.html" rel="tag">Java</a>, <a href="/tags/%E5%8A%A8%E6%80%81%E8%B0%83%E8%AF%95.html" rel="tag">动态调试</a>, <a href="/tags/%E6%95%85%E9%9A%9C%E6%8E%92%E6%9F%A5.html" rel="tag">故障排查</a></span></div><div class="row page-navigation-container hidden-print"><div class="col-xs-12 col-sm-12 col-md-12 col-lg-12"><div class="navigation-wrapper"><div class="pager"><div class="title-box"><i class="fa fa-paperclip"></i>#看看其他</div><a href="https://tech.meituan.com/2019/10/31/trajectory-prediction-contest.html" title="CVPR 2019轨迹预测竞赛冠军方法总结" class="previous"><span aria-hidden="true">前一篇: CVPR 2019轨迹预测竞赛冠军方法总结</span></a>
<a href="https://tech.meituan.com/2019/11/07/android-static-code-canning.html" title="Android静态代码扫描效率优化与实践" class="next"><span aria-hidden="true">后一篇: Android静态代码扫描效率优化与实践</span></a></div></div></div></div><div class="row page-comments-container hidden-print"><div class="col-xs-12 col-sm-12 col-md-12 col-lg-12"><div class="comments-wrapper hidden-print"><div id="comments" class="comments-disabled"><div class="title-box"><i class="fa fa-comments-o"></i>#一起聊聊</div><div class="post-feedback-box hidden-print"><p>如发现文章有错误、对内容有疑问，都可以关注美团技术团队微信公众号（meituantech），在后台给我们留言。</p><img class="qrcode" src="https://awps-assets.meituan.net/mit-x/blog-images-bundle-common/tech-team.png" alt="美团技术团队微信二维码"><p class="follow-us">我们每周会挑选出一位热心小伙伴，送上一份精美的小礼品。快来扫码关注我们吧！</p></div></div></div></div></div></div></div>