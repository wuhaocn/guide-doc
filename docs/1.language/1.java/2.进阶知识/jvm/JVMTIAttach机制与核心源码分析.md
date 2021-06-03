# ☆JVMTI Attach 机制与核心源码分析

[![](https://upload.jianshu.io/users/upload_avatars/2062729/105612dd-1e85-48e5-b3fa-a13cf07fddb6.jpg?imageMogr2/auto-orient/strip|imageView2/1/w/96/h/96/format/webp)]()

[猿码道]()关注

0.782018.05.29 15:31:42 字数 2,378 阅读 1,295

# 0 前言

前面文章，我们已讲述了[《基于 JVMTI 的 Agent 实现》](https://www.jianshu.com/p/5cea483f1b36)和[《基于 Java Instrument 的 Agent 实现》](https://www.jianshu.com/p/b72f66da679f)两种 Agent 的实现方式，其中每种方式都会分为：**启动时 Agent、运行时 Agent**。

对于 **启动时 Agent 的触发机制**，在上一节[《JVMTI Agent 工作原理及核心源码分析》](https://www.jianshu.com/p/7b2072513819)中，已经在源码级进行了分析，具体如下：
![]()

加载 Agent 链接库，触发调用 Agent_OnLoad 方法

但是对于 **运行时 Agent 的触发机制**，却没有进行详细说明，本节的主要目标就是在源码级分析下 JVMTI Attach 工作机制。

# 1 Attach 是什么

**Attach 机制是 JVM 提供一种 JVM 进程间通信的能力，能让一个进程传命令给另外一个进程，并让它执行内部的一些操作**。
比如：为了让另外一个 JVM 进程把线程 dump 出来，那么首先跑了一个 jstack 的进程，然后传了个 pid 的参数，告诉它要哪个进程进行线程 dump，既然是两个进程，那肯定涉及到进程间通信，以及传输协议的定义，比如：要执行什么操作，传了什么参数等。

有时当我们感觉线程一直卡在某个地方，想知道卡在哪里，首先想到的是进行 **线程 dump，而常用的命令是 jstack**，我们就可以看到如下线程栈：

```
2014-06-18 12:56:14 Full thread dump Java HotSpot(TM) 64-Bit Server VM (24.51-b03 mixed mode): "Attach Listener" daemon prio=5 tid=0x00007fb0c6800800 nid=0x440b waiting on condition [0x0000000000000000] java.lang.Thread.State: RUNNABLE "Service Thread" daemon prio=5 tid=0x00007fb0c584d800 nid=0x5303 runnable [0x0000000000000000] java.lang.Thread.State: RUNNABLE "C2 CompilerThread1" daemon prio=5 tid=0x00007fb0c482e000 nid=0x5103 waiting on condition [0x0000000000000000] java.lang.Thread.State: RUNNABLE "C2 CompilerThread0" daemon prio=5 tid=0x00007fb0c482c800 nid=0x4f03 waiting on condition [0x0000000000000000] java.lang.Thread.State: RUNNABLE "Signal Dispatcher" daemon prio=5 tid=0x00007fb0c4815800 nid=0x4d03 runnable [0x0000000000000000] java.lang.Thread.State: RUNNABLE "Finalizer" daemon prio=5 tid=0x00007fb0c4813800 nid=0x3903 in Object.wait() [0x00000001187d2000] java.lang.Thread.State: WAITING (on object monitor) at java.lang.Object.wait(Native Method) - waiting on <0x00000007aaa85568>(a java.lang.ref.ReferenceQueue$Lock) at java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:135) - locked <0x00000007aaa85568>(a java.lang.ref.ReferenceQueue$Lock) at java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:151) at java.lang.ref.Finalizer$FinalizerThread.run(Finalizer.java:189) "Reference Handler" daemon prio=5 tid=0x00007fb0c4800000 nid=0x3703 in Object.wait() [0x00000001186cf000] java.lang.Thread.State: WAITING (on object monitor) at java.lang.Object.wait(Native Method) - waiting on <0x00000007aaa850f0>(a java.lang.ref.Reference$Lock) at java.lang.Object.wait(Object.java:503) at java.lang.ref.Reference$ReferenceHandler.run(Reference.java:133) - locked <0x00000007aaa850f0>(a java.lang.ref.Reference$Lock) "main" prio=5 tid=0x00007fb0c5800800 nid=0x1903 waiting on condition [0x0000000107962000] java.lang.Thread.State: TIMED_WAITING (sleeping) at java.lang.Thread.sleep(Native Method) at Test.main(Test.java:5) "VM Thread" prio=5 tid=0x00007fb0c583d800 nid=0x3503 runnable "GC task thread/#0 (ParallelGC)" prio=5 tid=0x00007fb0c401e000 nid=0x2503 runnable "GC task thread/#1 (ParallelGC)" prio=5 tid=0x00007fb0c401e800 nid=0x2703 runnable "GC task thread/#2 (ParallelGC)" prio=5 tid=0x00007fb0c401f800 nid=0x2903 runnable "GC task thread/#3 (ParallelGC)" prio=5 tid=0x00007fb0c4020000 nid=0x2b03 runnable "GC task thread/#4 (ParallelGC)" prio=5 tid=0x00007fb0c4020800 nid=0x2d03 runnable "GC task thread/#5 (ParallelGC)" prio=5 tid=0x00007fb0c4021000 nid=0x2f03 runnable "GC task thread/#6 (ParallelGC)" prio=5 tid=0x00007fb0c4022000 nid=0x3103 runnable "GC task thread/#7 (ParallelGC)" prio=5 tid=0x00007fb0c4022800 nid=0x3303 runnable "VM Periodic Task Thread" prio=5 tid=0x00007fb0c5845000 nid=0x5503 waiting on condition
```

在上面的 Thread Dump 日志中，出现了两个线程：**“Attach Listener” 和 “Signal Dispatcher”**，这两个线程便是 Attach 机制的关键。

那么 JVM 是如何启动这两个线程呢？**JVM 有很多线程主要在 thread.cpp 里的 create_vm 方法体里实现**：

```
JvmtiExport::enter_live_phase(); // 1. Signal Dispatcher 需要在发布VMInit事件之前启动 os::signal_init(); // 2. Start Attach Listener 如果配置 +StartAttachListener; 否则会延迟启动 if (!DisableAttachMechanism) { if (StartAttachListener || AttachListener::init_at_startup()) { AttachListener::init(); } }
```

**其中 JVM 相关参数：DisableAttachMechanism，StartAttachListener ，ReduceSignalUsage 均默认是 false**：

```
product(bool, DisableAttachMechanism, false, "Disable mechanism that allows tools to Attach to this VM”); product(bool, StartAttachListener, false, "Always start Attach Listener at VM startup"); product(bool, ReduceSignalUsage, false, "Reduce the use of OS signals in Java and/or the VM”);
```

如上面 create_vm 源码所示，在启动的时候有可能不会创建 AttachListener 线程，那么 **在上面 Thread Stack 日志中看到的 AttachListener 线程是怎么创建的呢，这个就要关注另外一个线程“Signal Dispatcher”了**，顾名思义是处理信号的，这个线程是在 JVM 启动的时候肯定会创建的。

## 1.1 Signal Dispatcher 线程

在 os.cpp 中的

signal_init()
函数中，启动了 signal dispatcher 线程，**对 signal dispather 线程主要是用于处理信号，等待信号并且分发处理**，可以详细看

signal_thread_entry
的方法：

```
// 该方法用于Signal Dispatcher线程处理接受到的信号 static void signal_thread_entry(JavaThread/* thread, TRAPS) { os::set_priority(thread, NearMaxPriority); while (true) { int sig; { // FIXME : Currently we have not decieded what should be the status // for this java thread blocked here. Once we decide about // that we should fix this. 等待信号 sig = os::signal_wait(); } if (sig == os::sigexitnum_pd()) { // Terminate the signal thread return; } switch (sig) { case SIGBREAK: { // Check if the signal is a trigger to start the Attach Listener - in that // case don't print stack traces. if (!DisableAttachMechanism && AttachListener::is_init_trigger()) { continue; } // Print stack traces // Any SIGBREAK operations added here should make sure to flush // the output stream (e.g. tty->flush()) after output. See 4803766. // Each module also prints an extra carriage return after its output. VM_PrintThreads op; VMThread::execute(&op); VM_PrintJNI jni_op; VMThread::execute(&jni_op); VM_FindDeadlocks op1(tty); VMThread::execute(&op1); Universe::print_heap_at_SIGBREAK(); if (PrintClassHistogram) { VM_GC_HeapInspection op1(gclog_or_tty, true //* force full GC before heap inspection /*/, true //* need_prologue /*/); VMThread::execute(&op1); } if (JvmtiExport::should_post_data_dump()) { JvmtiExport::post_data_dump(); } break; } default: { // Dispatch the signal to java HandleMark hm(THREAD); klassOop k = SystemDictionary::resolve_or_null(vmSymbolHandles::sun_misc_Signal(), THREAD); KlassHandle klass (THREAD, k); if (klass.not_null()) { JavaValue result(T_VOID); JavaCallArguments args; args.push_int(sig); JavaCalls::call_static( &result, klass, vmSymbolHandles::dispatch_name(), vmSymbolHandles::int_void_signature(), &args, THREAD ); } if (HAS_PENDING_EXCEPTION) { // tty is initialized early so we don't expect it to be null, but // if it is we can't risk doing an initialization that might // trigger additional out-of-memory conditions if (tty != NULL) { char klass_name[256]; char tmp_sig_name[16]; const char/* sig_name = "UNKNOWN"; instanceKlass::cast(PENDING_EXCEPTION->klass())-> name()->as_klass_external_name(klass_name, 256); if (os::exception_name(sig, tmp_sig_name, 16) != NULL) sig_name = tmp_sig_name; warning("Exception %s occurred dispatching signal %s to handler" "- the VM may need to be forcibly terminated", klass_name, sig_name ); } CLEAR_PENDING_EXCEPTION; } } } } }
```

可以看到通过

os::signal_wait();
等待信号，而在 Linux 里是通过

sem_wait()
来实现，**当接受到信号是 SIGBREAK(在 JVM 里做了/#define，其实就是 SIGQUIT)的时候，就会触发 AttachListener::is_init_trigger()的执行初始化 attach listener 线程**。

1. **第一次收到信号，会开始初始化**，当初始化成功，将会直接返回，而且 **不返回任何线程 stack 的信息（通过 socket file 的操作返回），并且第二次将不在需要初始化**。如果初始化不成功，将直接在控制台的 outputstream 中打印线程栈信息；
1. **第二次收到信号**，如果已经初始化过，将直接在控制台中打印线程的栈信息。如果没有初始化，继续初始化，走和第一次相同的流程；

比如：我们经常会 **使用 kill -3 pid 的操作打印出线程栈信息**，可以看到具体的实现是在 Signal Dispatcher 线程中完成的，**因为 kill -3 pid 并不会创建.attach_pid/#pid 文件**，所以一直初始化不成功，从而线程的栈信息被打印到控制台中。

## 1.2 Attach Listener 线程

**Attach Listener 线程是负责接收到外部的命令，而对该命令进行执行的并且把结果返回给发送者**。在 JVM 启动的时候，如果没有指定

+StartAttachListener
，该 Attach Listener 线程是不会启动的。

在接受到

quit
信号之后，会调用

AttachListener::is_init_trigger()
方法，

AttachListener::is_init_trigger()
内会调用

AttachListener::init()
启动了 Attach Listener 线程，在不同的操作系统下初始化实现是不同的，在 linux 中是在 attachListener_Linux.cpp 文件中实现的。

**
AttachListener::is_init_trigger()
代码如下**：

```
bool AttachListener::is_init_trigger() { if (init_at_startup() || is_initialized()) { return false; // initialized at startup or already initialized } char fn[PATH_MAX+1]; sprintf(fn, ".Attach_pid%d", os::current_process_id()); int ret; struct stat64 st; RESTARTABLE(::stat64(fn, &st), ret); if (ret == -1) { snprintf(fn, sizeof(fn), "%s/.Attach_pid%d", os::get_temp_directory(), os::current_process_id()); RESTARTABLE(::stat64(fn, &st), ret); } if (ret == 0) { // simple check to avoid starting the Attach mechanism when // a bogus user creates the file if (st.st_uid == geteuid()) { // 创建AttachListener线程 init(); return true; } } return false; }
```

一开始会 **判断当前进程目录下是否有个.Attach_pid 文件**，如果没有就会在/tmp 下创建一个

/tmp/.Attach_pid
，**当那个文件的 uid 和自己的 uid 是一致的情况下（为了安全）再调用 init 方法**。

```
// Starts the Attach Listener thread void AttachListener::init() { EXCEPTION_MARK; klassOop k = SystemDictionary::resolve_or_fail(vmSymbols::java_lang_Thread(), true, CHECK); instanceKlassHandle klass (THREAD, k); instanceHandle thread_oop = klass->allocate_instance_handle(CHECK); const char thread_name[] = "Attach Listener"; Handle string = java_lang_String::create_from_str(thread_name, CHECK); // Initialize thread_oop to put it into the system threadGroup Handle thread_group (THREAD, Universe::system_thread_group()); JavaValue result(T_VOID); JavaCalls::call_special(&result, thread_oop, klass, vmSymbols::object_initializer_name(), vmSymbols::threadgroup_string_void_signature(), thread_group, string, CHECK); KlassHandle group(THREAD, SystemDictionary::ThreadGroup_klass()); JavaCalls::call_special(&result, thread_group, group, vmSymbols::add_method_name(), vmSymbols::thread_void_signature(), thread_oop, // ARG 1 CHECK); { MutexLocker mu(Threads_lock); JavaThread\/* listener_thread = new JavaThread(&Attach_listener_thread_entry); // Check that thread and osthread were created if (listener_thread == NULL || listener_thread->osthread() == NULL) { vm_exit_during_initialization("java.lang.OutOfMemoryError", "unable to create new native thread"); } java_lang_Thread::set_thread(thread_oop(), listener_thread); java_lang_Thread::set_daemon(thread_oop()); listener_thread->set_threadObj(thread_oop()); Threads::add(listener_thread); Thread::start(listener_thread); } }
```

此时水落石出了，看到创建了一个线程，并且取名为 Attach Listener。**再看看 Linux 系统下其子类 LinuxAttachListener 的 init 方法**：

```
int LinuxAttachListener::init() { char path[UNIX_PATH_MAX]; // socket file char initial_path[UNIX_PATH_MAX]; // socket file during setup int listener; // listener socket (file descriptor) // register function to cleanup ::atexit(listener_cleanup); int n = snprintf(path, UNIX_PATH_MAX, "%s/.java_pid%d", os::get_temp_directory(), os::current_process_id()); if (n < (int)UNIX_PATH_MAX) { n = snprintf(initial_path, UNIX_PATH_MAX, "%s.tmp", path); } if (n >= (int)UNIX_PATH_MAX) { return -1; } // create the listener socket listener = ::socket(PF_UNIX, SOCK_STREAM, 0); if (listener == -1) { return -1; } // bind socket struct sockaddr_un addr; addr.sun_family = AF_UNIX; strcpy(addr.sun_path, initial_path); ::unlink(initial_path); int res = ::bind(listener, (struct sockaddr\/*)&addr, sizeof(addr)); if (res == -1) { RESTARTABLE(::close(listener), res); return -1; } // put in listen mode, set permissions, and rename into place res = ::listen(listener, 5); if (res == 0) { RESTARTABLE(::chmod(initial_path, S_IREAD|S_IWRITE), res); if (res == 0) { res = ::rename(initial_path, path); } } if (res == -1) { RESTARTABLE(::close(listener), res); ::unlink(initial_path); return -1; } set_path(path); set_listener(listener); return 0; }
```

**看到其创建了一个监听套接字，并创建了一个文件/tmp/.java_pid，这个文件就是客户端之前一直在轮询等待的文件**，随着这个文件的生成，意味着 Attach 的创建过程圆满结束了。

Attach Listener 线程接收到请求时，**具体的请求处理在 attach_listener_thread_entry 方法体中实现**：

```
static void attach_listener_thread_entry(JavaThread/* thread, TRAPS) { os::set_priority(thread, NearMaxPriority); if (AttachListener::pd_init() != 0) { return; } AttachListener::set_initialized(); for (;;) { AttachOperation/* op = AttachListener::dequeue(); if (op == NULL) { return; // dequeue failed or shutdown } ResourceMark rm; bufferedStream st; jint res = JNI_OK; // handle special detachall operation if (strcmp(op->name(), AttachOperation::detachall_operation_name()) == 0) { AttachListener::detachall(); } else { // find the function to dispatch too AttachOperationFunctionInfo/* info = NULL; for (int i=0; funcs[i].name != NULL; i++) { const char/* name = funcs[i].name; assert(strlen(name) <= AttachOperation::name_length_max, "operation <= name_length_max"); if (strcmp(op->name(), name) == 0) { info = &(funcs[i]); break; } } // check for platform dependent attach operation if (info == NULL) { info = AttachListener::pd_find_operation(op->name()); } if (info != NULL) { // dispatch to the function that implements this operation res = (info->func)(op, &st); } else { st.print("Operation %s not recognized!", op->name()); res = JNI_ERR; } } // operation complete - send result and output to client op->complete(res, &st); } }
```

从代码来看就是 **从队列里不断取 AttachOperation，然后找到请求命令对应的方法进行执行**，比如一开始说的 jstack 命令，找到 { “threaddump”, thread_dump }的映射关系，然后执行 thread_dump 方法。

AttachOperation 有很多种类，比如：内存 dump，线程 dump，类信息统计(比如加载的类及大小以及实例个数等)，动态加载 agent，动态设置 vm flag(但是并不是所有的 flag 都可以设置的，因为有些 flag 是在 jvm 启动过程中使用的，是一次性的)，打印 vm flag，获取系统属性等，这些对应的源码（

AttachListener.cpp
）如下：

```
static AttachOperationFunctionInfo funcs[] = { // 第二个参数是命令对应的处理函数 { "agentProperties", get_agent_properties }, { "datadump", data_dump }, { "dumpheap", dump_heap }, { "load", JvmtiExport::load_agent_library }, { "properties", get_system_properties }, { "threaddump", thread_dump }, { "inspectheap", heap_inspection }, { "setflag", set_flag }, { "printflag", print_flag }, { "jcmd", jcmd }, { NULL, NULL } };
```

再来看看其要调用的

AttachListener::dequeue();
：

```
AttachOperation/* AttachListener::dequeue() { JavaThread/* thread = JavaThread::current(); ThreadBlockInVM tbivm(thread); thread->set_suspend_equivalent(); // cleared by handle_special_suspend_equivalent_condition() or // java_suspend_self() via check_and_wait_while_suspended() AttachOperation\/* op = LinuxAttachListener::dequeue(); // were we externally suspended while we were waiting? thread->check_and_wait_while_suspended(); return op; }
```

最终会调用的是

LinuxAttachListener::dequeue()
：

```
LinuxAttachOperation/* LinuxAttachListener::dequeue() { for (;;) { int s; // wait for client to connect struct sockaddr addr; socklen_t len = sizeof(addr); // 如果没有请求的话，会一直accept在那里 RESTARTABLE(::accept(listener(), &addr, &len), s); if (s == -1) { return NULL; // log a warning? } // get the credentials of the peer and check the effective uid/guid // - check with jeff on this. struct ucred cred_info; socklen_t optlen = sizeof(cred_info); if (::getsockopt(s, SOL_SOCKET, SO_PEERCRED, (void/*)&cred_info, &optlen) == -1) { int res; RESTARTABLE(::close(s), res); continue; } uid_t euid = geteuid(); gid_t egid = getegid(); if (cred_info.uid != euid || cred_info.gid != egid) { int res; RESTARTABLE(::close(s), res); continue; } // peer credential look okay so we read the request LinuxAttachOperation/* op = read_request(s); if (op == NULL) { int res; RESTARTABLE(::close(s), res); continue; } else { return op; } } }
```

如上代码中可以看到，**如果没有请求的话，会一直 accept 在那里，当来了请求，然后就会创建一个套接字，并读取数据，构建出 LinuxAttachOperation 返回，找到请求对应的操作，调用操作得到结果并把结果写到这个 socket 的文件**，如果你把 socket 的文件删除，jstack/jmap 会出现错误信息 unable to open socket file:........

## 1.3 jstack/jmap 命令流程图

以 jstack 的实现来说明触发 Attach 这一机制进行的过程，**jstack 命令的实现其实是一个叫做 JStack.java 的类**，jstack 命令首先会 attach 到目标 JVM 进程，产生 VirtualMachine 类；Linux 系统下，其实现类为 LinuxVirtualMachine，调用其 remoteDataDump 方法，打印堆栈信息；查看

JStack.java
代码后会走到下面的方法里：

```
private static void runThreadDump(String pid, String args[]) throws Exception { VirtualMachine vm = null; try { // jstack命令首先会attach到目标JVM进程 vm = VirtualMachine.Attach(pid); } catch (Exception x) { String msg = x.getMessage(); if (msg != null) { System.err.println(pid + ": " + msg); } else { x.printStackTrace(); } if ((x instanceof AttachNotSupportedException) && (loadSAClass() != null)) { System.err.println("The -F option can be used when the target " + "process is not responding"); } System.exit(1); } // Cast to HotSpotVirtualMachine as this is implementation specific // method. // 输出堆栈信息 InputStream in = ((HotSpotVirtualMachine)vm).remoteDataDump((Object[])args); // read to EOF and just print output byte b[] = new byte[256]; int n; do { n = in.read(b); if (n > 0) { String s = new String(b, 0, n, "UTF-8"); System.out.print(s); } } while (n > 0); in.close(); vm.detach(); }
```

那么 VirtualMachine 是如何连接到目标 JVM 进程的呢？请注意

VirtualMachine.Attach(pid);
这行代码，触发 Attach pid 的关键，如果是在 Linux 下具体的实现逻辑在

sun.tools.attach.LinuxVirtualMachine
的构造函数：

```
LinuxVirtualMachine(AttachProvider provider, String vmid) throws AttachNotSupportedException, IOException { super(provider, vmid); // This provider only understands pids int pid; try { pid = Integer.parseInt(vmid); } catch (NumberFormatException x) { throw new AttachNotSupportedException("Invalid process identifier"); } // Find the socket file. If not found then we attempt to start the // Attach mechanism in the target VM by sending it a QUIT signal. // Then we attempt to find the socket file again. path = findSocketFile(pid); if (path == null) { File f = createAttachFile(pid); try { // On LinuxThreads each thread is a process and we don't have the // pid of the VMThread which has SIGQUIT unblocked. To workaround // this we get the pid of the "manager thread" that is created // by the first call to pthread_create. This is parent of all // threads (except the initial thread). if (isLinuxThreads) { int mpid; try { mpid = getLinuxThreadsManager(pid); } catch (IOException x) { throw new AttachNotSupportedException(x.getMessage()); } assert(mpid >= 1); sendQuitToChildrenOf(mpid); } else { sendQuitTo(pid); } // give the target VM time to start the Attach mechanism int i = 0; long delay = 200; int retries = (int)(AttachTimeout() / delay); do { try { Thread.sleep(delay); } catch (InterruptedException x) { } path = findSocketFile(pid); i++; } while (i <= retries && path == null); if (path == null) { throw new AttachNotSupportedException( "Unable to open socket file: target process not responding " + "or HotSpot VM not loaded"); } } finally { f.delete(); } } // Check that the file owner/permission to avoid Attaching to // bogus process checkPermissions(path); // Check that we can connect to the process // - this ensures we throw the permission denied error now rather than // later when we attempt to enqueue a command. int s = socket(); try { connect(s, path); } finally { close(s); } }
```

1. 查找/tmp 目录下是否存在

".java_pid"+pid
文件；

1. 如果文件不存在，则首先创建

"/proc/" + pid + "/cwd/" + ".attach_pid" + pid
文件；

1. 通过

kill
命令发送

SIGQUIT
信号给目标 JVM 进程，由于 JVM 里除了信号线程，其他线程都设置了对此信号的屏蔽，因此收不到该信号，于是该信号就传给了

“Signal Dispatcher”
；

1. 目标 JVM 进程接收到信号之后，会在

/tmp
目录下创建

".java_pid"+pid
文件；

1. 当发现

/tmp
目录下存在

".java_pid"+pid
文件，

LinuxVirtualMachine
会通过 connect 系统调用连接到该文件描述符，后续通过该

fd
进行双方的通讯；

JVM 接受 SIGQUIT 信号的相关逻辑处理，\*\*则是在前面

signal_thread_entry
方法中进行实现\*\*。

![]()

jstack/jmap 命令流程图

前面

JStack.java
源码中，输出堆栈信息是通过调用

remoteDataDump
方法实现的，**该方法就是通过往前面提到的 fd 中写入 threaddump 指令，读取返回结果，从而得到目标 JVM 的堆栈信息**。

# 2 Java 代码实现动态 attach Agent

Java 动态 attach Agent 与上面所讲到的

JStack.java
实现基本类似，在 attach 的 java 代码中，使用 sun 自用的 tool.jar 中的 VirtualMachine 的 attach 的方式：

```
VirtualMachine vm = VirtualMachine.attach(processid); vm.loadAgent(agentpath, args)
```

在

HotSpotVirtualMachine.java
中，

loadAgent
方法源码如下：

```
public void loadAgent(String agent, String options) throws AgentLoadException, AgentInitializationException, IOException { String args = agent; if (options != null) { args = args + "=" + options; } try { loadAgentLibrary("instrument", args); } ..... } private void loadAgentLibrary(String agentLibrary, boolean isAbsolute, String options) throws AgentLoadException, AgentInitializationException, IOException { InputStream in = execute("load", agentLibrary, isAbsolute ? "true" : "false", options); try { int result = readInt(in); if (result != 0) { throw new AgentInitializationException("Agent_OnAttach failed", result); } } finally { in.close(); } }
```

在

LinuxVirtualMachine.java
中的

execute
方法：

```
InputStream execute(String cmd, Object ... args) throws AgentLoadException, IOException { assert args.length <= 3; // includes null // did we detach? String p; synchronized (this) { if (this.path == null) { throw new IOException("Detached from target VM"); } p = this.path; } // create UNIX socket int s = socket(); // connect to target VM try { connect(s, p); } catch (IOException x) { close(s); throw x; } IOException ioe = null; // connected - write request // <ver> <cmd> <args...> try { writeString(s, PROTOCOL_VERSION); writeString(s, cmd); for (int i=0; i<3; i++) { if (i < args.length && args[i] != null) { writeString(s, (String)args[i]); } else { writeString(s, ""); } } } catch (IOException x) { ioe = x; } // Create an input stream to read reply SocketInputStream sis = new SocketInputStream(s); // Read the command completion status int completionStatus; try { completionStatus = readInt(sis); } catch (IOException x) { sis.close(); if (ioe != null) { throw ioe; } else { throw x; } } .... }
```

也就是向 socket 的中写入了，格式为：

```
<ver> <cmd> <args...>
```

具体内容为：

```
1 load instrument agentPath=path.jar
```

既然 Load Agent 往 socket 里发了 load 指令，匹配到 JVM 的操作：

```
static AttachOperationFunctionInfo funcs[] = { { "agentProperties", get_agent_properties }, { "datadump", data_dump }, /#ifndef SERVICES_KERNEL { "dumpheap", dump_heap }, /#endif// SERVICES_KERNEL { "load", JvmtiExport::load_agent_library }, { "properties", get_system_properties }, { "threaddump", thread_dump }, { "inspectheap", heap_inspection }, { "setflag", set_flag }, { "printflag", print_flag }, { NULL, NULL } };
```

"load", JvmtiExport::load_agent_library
，具体源码如下：

```
jint JvmtiExport::load_agent_library(AttachOperation/* op, outputStream/* st) { char ebuf[1024]; char buffer[JVM_MAXPATHLEN]; void/* library; jint result = JNI_ERR; const char/* agent = op->arg(0); const char/* absParam = op->arg(1); const char/* options = op->arg(2); bool is_absolute_path = (absParam != NULL) && (strcmp(absParam,"true")==0); if (is_absolute_path) { library = os::dll_load(agent, ebuf, sizeof ebuf); } else { // Try to load the agent from the standard dll directory os::dll_build_name(buffer, sizeof(buffer), Arguments::get_dll_dir(), agent); library = os::dll_load(buffer, ebuf, sizeof ebuf); if (library == NULL) { // not found - try local path char ns[1] = {0}; os::dll_build_name(buffer, sizeof(buffer), ns, agent); library = os::dll_load(buffer, ebuf, sizeof ebuf); } } if (library != NULL) { // Lookup the Agent_OnAttach function OnAttachEntry_t on_attach_entry = NULL; const char /*on_attach_symbols[] = AGENT_ONATTACH_SYMBOLS; for (uint symbol_index = 0; symbol_index < ARRAY_SIZE(on_attach_symbols); symbol_index++) { on_attach_entry = CAST_TO_FN_PTR(OnAttachEntry_t, os::dll_lookup(library, on_attach_symbols[symbol_index])); if (on_attach_entry != NULL) break; } if (on_attach_entry == NULL) { // Agent_OnAttach missing - unload library os::dll_unload(library); } else { // Invoke the Agent_OnAttach function JavaThread/* THREAD = JavaThread::current(); { extern struct JavaVM_ main_vm; JvmtiThreadEventMark jem(THREAD); JvmtiJavaThreadEventTransition jet(THREAD); result = (/*on_attach_entry)(&main_vm, (char/*)options, NULL); } if (HAS_PENDING_EXCEPTION) { CLEAR_PENDING_EXCEPTION; } if (result == JNI_OK) { Arguments::add_loaded_agent(agent, (char/*)options, is_absolute_path, library); } // Agent_OnAttach executed so completion status is JNI_OK st->print_cr("%d", result); result = JNI_OK; } } return result; } /#define AGENT_ONATTACH_SYMBOLS {"Agent_OnAttach"}
```

# 3 执行 Instrument 的 Agent on attach

加载 instrument 的动态库，并且调用方法 instrument 动态库中的

Agent_OnAttach
方法：

```
JNIEXPORT jint JNICALL Agent_OnAttach(JavaVM/* vm, char /*args, void /* reserved) { ..... initerror = createNewJPLISAgent(vm, &agent); if ( initerror == JPLIS_INIT_ERROR_NONE ) { ...... if (parseArgumentTail(args, &jarfile, &options) != 0) { return JNI_ENOMEM; } attributes = readAttributes( jarfile ); if (attributes == NULL) { fprintf(stderr, "Error opening zip file or JAR manifest missing: %s\n", jarfile); free(jarfile); if (options != NULL) free(options); return AGENT_ERROR_BADJAR; } agentClass = getAttribute(attributes, "Agent-Class"); if (agentClass == NULL) { fprintf(stderr, "Failed to find Agent-Class manifest attribute from %s\n", jarfile); free(jarfile); if (options != NULL) free(options); freeAttributes(attributes); return AGENT_ERROR_BADJAR; } if (appendClassPath(agent, jarfile)) { fprintf(stderr, "Unable to add %s to system class path " "- not supported by system class loader or configuration error!\n", jarfile); free(jarfile); if (options != NULL) free(options); freeAttributes(attributes); return AGENT_ERROR_NOTONCP; } oldLen = strlen(agentClass); newLen = modifiedUtf8LengthOfUtf8(agentClass, oldLen); if (newLen == oldLen) { agentClass = strdup(agentClass); } else { char/* str = (char/*)malloc( newLen+1 ); if (str != NULL) { convertUtf8ToModifiedUtf8(agentClass, oldLen, str, newLen); } agentClass = str; } if (agentClass == NULL) { free(jarfile); if (options != NULL) free(options); freeAttributes(attributes); return JNI_ENOMEM; } bootClassPath = getAttribute(attributes, "Boot-Class-Path"); if (bootClassPath != NULL) { appendBootClassPath(agent, jarfile, bootClassPath); } convertCapabilityAtrributes(attributes, agent); success = createInstrumentationImpl(jni_env, agent); jplis_assert(success); //* /* Turn on the ClassFileLoadHook. /*/ if (success) { success = setLivePhaseEventHandlers(agent); jplis_assert(success); } if (success) { success = startJavaAgent(agent, jni_env, agentClass, options, agent->mAgentmainCaller); } if (!success) { fprintf(stderr, "Agent failed to start!\n"); result = AGENT_ERROR_STARTFAIL; } if (options != NULL) free(options); free(agentClass); freeAttributes(attributes); } return result; }
```

上面代码里一开始的

createNewJPLISAgent
和

on_load
是一样的注册了一些钩子函数，具体详情可参考：[《JVMTI Agent 工作原理及核心源码分析》](https://www.jianshu.com/p/7b2072513819)。

在上面的

Agent_OnAttach
代码中我们也看到了，读取加载的 jar 中 MANIFEST Agent-Class 的配置：

```
agentClass = getAttribute(attributes, "Agent-Class");
```

创建生成 sun.instrument.InstrumentationImpl 对象：

```
success = createInstrumentationImpl(jni_env, agent);
```

通过 InstrumentationImpl 对象中的

loadClassAndCallAgentmain
方法去初始化在 Agent-Class 中的类，并调用 class 里的

agentmain
的方法：

```
success = startJavaAgent(agent, jni_env, agentClass, options, agent->mAgentmainCaller);
```

也就是说定义的

on_attach
的 class 里需要有

agentmain
的方法实现：

```
public class MyTransformer { public static void agentmain(String agentArgs, Instrumentation inst) throws ClassNotFoundException, UnmodifiableClassException, NotFoundException, CannotCompileException, IOException{ .... } }
```

50 人点赞

[01\_深入浅出 Java]()
"赞赏我的文章给您带来收获"赞赏支持还没有人赞赏，支持一下

[![  ](https://upload.jianshu.io/users/upload_avatars/2062729/105612dd-1e85-48e5-b3fa-a13cf07fddb6.jpg?imageMogr2/auto-orient/strip|imageView2/1/w/100/h/100/format/webp)]()

[猿码道]("猿码道")以知识为海，以人生为舟，自书海泛舟，而陶然以乐。

总资产 164 (约 13.03 元)共写了 67.1W 字获得 12,336 个赞共 6,628 个粉丝
关注

# ☆JVMTI Attach 机制与核心源码分析

[![](https://upload.jianshu.io/users/upload_avatars/2062729/105612dd-1e85-48e5-b3fa-a13cf07fddb6.jpg?imageMogr2/auto-orient/strip|imageView2/1/w/96/h/96/format/webp)]()

[猿码道]()关注

0.782018.05.29 15:31:42 字数 2,378 阅读 1,295

# 0 前言

前面文章，我们已讲述了[《基于 JVMTI 的 Agent 实现》](https://www.jianshu.com/p/5cea483f1b36)和[《基于 Java Instrument 的 Agent 实现》](https://www.jianshu.com/p/b72f66da679f)两种 Agent 的实现方式，其中每种方式都会分为：**启动时 Agent、运行时 Agent**。

对于 **启动时 Agent 的触发机制**，在上一节[《JVMTI Agent 工作原理及核心源码分析》](https://www.jianshu.com/p/7b2072513819)中，已经在源码级进行了分析，具体如下：
![]()

加载 Agent 链接库，触发调用 Agent_OnLoad 方法

但是对于 **运行时 Agent 的触发机制**，却没有进行详细说明，本节的主要目标就是在源码级分析下 JVMTI Attach 工作机制。

# 1 Attach 是什么

**Attach 机制是 JVM 提供一种 JVM 进程间通信的能力，能让一个进程传命令给另外一个进程，并让它执行内部的一些操作**。
比如：为了让另外一个 JVM 进程把线程 dump 出来，那么首先跑了一个 jstack 的进程，然后传了个 pid 的参数，告诉它要哪个进程进行线程 dump，既然是两个进程，那肯定涉及到进程间通信，以及传输协议的定义，比如：要执行什么操作，传了什么参数等。

有时当我们感觉线程一直卡在某个地方，想知道卡在哪里，首先想到的是进行 **线程 dump，而常用的命令是 jstack**，我们就可以看到如下线程栈：

```
2014-06-18 12:56:14 Full thread dump Java HotSpot(TM) 64-Bit Server VM (24.51-b03 mixed mode): "Attach Listener" daemon prio=5 tid=0x00007fb0c6800800 nid=0x440b waiting on condition [0x0000000000000000] java.lang.Thread.State: RUNNABLE "Service Thread" daemon prio=5 tid=0x00007fb0c584d800 nid=0x5303 runnable [0x0000000000000000] java.lang.Thread.State: RUNNABLE "C2 CompilerThread1" daemon prio=5 tid=0x00007fb0c482e000 nid=0x5103 waiting on condition [0x0000000000000000] java.lang.Thread.State: RUNNABLE "C2 CompilerThread0" daemon prio=5 tid=0x00007fb0c482c800 nid=0x4f03 waiting on condition [0x0000000000000000] java.lang.Thread.State: RUNNABLE "Signal Dispatcher" daemon prio=5 tid=0x00007fb0c4815800 nid=0x4d03 runnable [0x0000000000000000] java.lang.Thread.State: RUNNABLE "Finalizer" daemon prio=5 tid=0x00007fb0c4813800 nid=0x3903 in Object.wait() [0x00000001187d2000] java.lang.Thread.State: WAITING (on object monitor) at java.lang.Object.wait(Native Method) - waiting on <0x00000007aaa85568>(a java.lang.ref.ReferenceQueue$Lock) at java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:135) - locked <0x00000007aaa85568>(a java.lang.ref.ReferenceQueue$Lock) at java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:151) at java.lang.ref.Finalizer$FinalizerThread.run(Finalizer.java:189) "Reference Handler" daemon prio=5 tid=0x00007fb0c4800000 nid=0x3703 in Object.wait() [0x00000001186cf000] java.lang.Thread.State: WAITING (on object monitor) at java.lang.Object.wait(Native Method) - waiting on <0x00000007aaa850f0>(a java.lang.ref.Reference$Lock) at java.lang.Object.wait(Object.java:503) at java.lang.ref.Reference$ReferenceHandler.run(Reference.java:133) - locked <0x00000007aaa850f0>(a java.lang.ref.Reference$Lock) "main" prio=5 tid=0x00007fb0c5800800 nid=0x1903 waiting on condition [0x0000000107962000] java.lang.Thread.State: TIMED_WAITING (sleeping) at java.lang.Thread.sleep(Native Method) at Test.main(Test.java:5) "VM Thread" prio=5 tid=0x00007fb0c583d800 nid=0x3503 runnable "GC task thread/#0 (ParallelGC)" prio=5 tid=0x00007fb0c401e000 nid=0x2503 runnable "GC task thread/#1 (ParallelGC)" prio=5 tid=0x00007fb0c401e800 nid=0x2703 runnable "GC task thread/#2 (ParallelGC)" prio=5 tid=0x00007fb0c401f800 nid=0x2903 runnable "GC task thread/#3 (ParallelGC)" prio=5 tid=0x00007fb0c4020000 nid=0x2b03 runnable "GC task thread/#4 (ParallelGC)" prio=5 tid=0x00007fb0c4020800 nid=0x2d03 runnable "GC task thread/#5 (ParallelGC)" prio=5 tid=0x00007fb0c4021000 nid=0x2f03 runnable "GC task thread/#6 (ParallelGC)" prio=5 tid=0x00007fb0c4022000 nid=0x3103 runnable "GC task thread/#7 (ParallelGC)" prio=5 tid=0x00007fb0c4022800 nid=0x3303 runnable "VM Periodic Task Thread" prio=5 tid=0x00007fb0c5845000 nid=0x5503 waiting on condition
```

在上面的 Thread Dump 日志中，出现了两个线程：**“Attach Listener” 和 “Signal Dispatcher”**，这两个线程便是 Attach 机制的关键。

那么 JVM 是如何启动这两个线程呢？**JVM 有很多线程主要在 thread.cpp 里的 create_vm 方法体里实现**：

```
JvmtiExport::enter_live_phase(); // 1. Signal Dispatcher 需要在发布VMInit事件之前启动 os::signal_init(); // 2. Start Attach Listener 如果配置 +StartAttachListener; 否则会延迟启动 if (!DisableAttachMechanism) { if (StartAttachListener || AttachListener::init_at_startup()) { AttachListener::init(); } }
```

**其中 JVM 相关参数：DisableAttachMechanism，StartAttachListener ，ReduceSignalUsage 均默认是 false**：

```
product(bool, DisableAttachMechanism, false, "Disable mechanism that allows tools to Attach to this VM”); product(bool, StartAttachListener, false, "Always start Attach Listener at VM startup"); product(bool, ReduceSignalUsage, false, "Reduce the use of OS signals in Java and/or the VM”);
```

如上面 create_vm 源码所示，在启动的时候有可能不会创建 AttachListener 线程，那么 **在上面 Thread Stack 日志中看到的 AttachListener 线程是怎么创建的呢，这个就要关注另外一个线程“Signal Dispatcher”了**，顾名思义是处理信号的，这个线程是在 JVM 启动的时候肯定会创建的。

## 1.1 Signal Dispatcher 线程

在 os.cpp 中的

signal_init()
函数中，启动了 signal dispatcher 线程，**对 signal dispather 线程主要是用于处理信号，等待信号并且分发处理**，可以详细看

signal_thread_entry
的方法：

```
// 该方法用于Signal Dispatcher线程处理接受到的信号 static void signal_thread_entry(JavaThread/* thread, TRAPS) { os::set_priority(thread, NearMaxPriority); while (true) { int sig; { // FIXME : Currently we have not decieded what should be the status // for this java thread blocked here. Once we decide about // that we should fix this. 等待信号 sig = os::signal_wait(); } if (sig == os::sigexitnum_pd()) { // Terminate the signal thread return; } switch (sig) { case SIGBREAK: { // Check if the signal is a trigger to start the Attach Listener - in that // case don't print stack traces. if (!DisableAttachMechanism && AttachListener::is_init_trigger()) { continue; } // Print stack traces // Any SIGBREAK operations added here should make sure to flush // the output stream (e.g. tty->flush()) after output. See 4803766. // Each module also prints an extra carriage return after its output. VM_PrintThreads op; VMThread::execute(&op); VM_PrintJNI jni_op; VMThread::execute(&jni_op); VM_FindDeadlocks op1(tty); VMThread::execute(&op1); Universe::print_heap_at_SIGBREAK(); if (PrintClassHistogram) { VM_GC_HeapInspection op1(gclog_or_tty, true //* force full GC before heap inspection /*/, true //* need_prologue /*/); VMThread::execute(&op1); } if (JvmtiExport::should_post_data_dump()) { JvmtiExport::post_data_dump(); } break; } default: { // Dispatch the signal to java HandleMark hm(THREAD); klassOop k = SystemDictionary::resolve_or_null(vmSymbolHandles::sun_misc_Signal(), THREAD); KlassHandle klass (THREAD, k); if (klass.not_null()) { JavaValue result(T_VOID); JavaCallArguments args; args.push_int(sig); JavaCalls::call_static( &result, klass, vmSymbolHandles::dispatch_name(), vmSymbolHandles::int_void_signature(), &args, THREAD ); } if (HAS_PENDING_EXCEPTION) { // tty is initialized early so we don't expect it to be null, but // if it is we can't risk doing an initialization that might // trigger additional out-of-memory conditions if (tty != NULL) { char klass_name[256]; char tmp_sig_name[16]; const char/* sig_name = "UNKNOWN"; instanceKlass::cast(PENDING_EXCEPTION->klass())-> name()->as_klass_external_name(klass_name, 256); if (os::exception_name(sig, tmp_sig_name, 16) != NULL) sig_name = tmp_sig_name; warning("Exception %s occurred dispatching signal %s to handler" "- the VM may need to be forcibly terminated", klass_name, sig_name ); } CLEAR_PENDING_EXCEPTION; } } } } }
```

可以看到通过

os::signal_wait();
等待信号，而在 Linux 里是通过

sem_wait()
来实现，**当接受到信号是 SIGBREAK(在 JVM 里做了/#define，其实就是 SIGQUIT)的时候，就会触发 AttachListener::is_init_trigger()的执行初始化 attach listener 线程**。

1. **第一次收到信号，会开始初始化**，当初始化成功，将会直接返回，而且 **不返回任何线程 stack 的信息（通过 socket file 的操作返回），并且第二次将不在需要初始化**。如果初始化不成功，将直接在控制台的 outputstream 中打印线程栈信息；
1. **第二次收到信号**，如果已经初始化过，将直接在控制台中打印线程的栈信息。如果没有初始化，继续初始化，走和第一次相同的流程；

比如：我们经常会 **使用 kill -3 pid 的操作打印出线程栈信息**，可以看到具体的实现是在 Signal Dispatcher 线程中完成的，**因为 kill -3 pid 并不会创建.attach_pid/#pid 文件**，所以一直初始化不成功，从而线程的栈信息被打印到控制台中。

## 1.2 Attach Listener 线程

**Attach Listener 线程是负责接收到外部的命令，而对该命令进行执行的并且把结果返回给发送者**。在 JVM 启动的时候，如果没有指定

+StartAttachListener
，该 Attach Listener 线程是不会启动的。

在接受到

quit
信号之后，会调用

AttachListener::is_init_trigger()
方法，

AttachListener::is_init_trigger()
内会调用

AttachListener::init()
启动了 Attach Listener 线程，在不同的操作系统下初始化实现是不同的，在 linux 中是在 attachListener_Linux.cpp 文件中实现的。

**
AttachListener::is_init_trigger()
代码如下**：

```
bool AttachListener::is_init_trigger() { if (init_at_startup() || is_initialized()) { return false; // initialized at startup or already initialized } char fn[PATH_MAX+1]; sprintf(fn, ".Attach_pid%d", os::current_process_id()); int ret; struct stat64 st; RESTARTABLE(::stat64(fn, &st), ret); if (ret == -1) { snprintf(fn, sizeof(fn), "%s/.Attach_pid%d", os::get_temp_directory(), os::current_process_id()); RESTARTABLE(::stat64(fn, &st), ret); } if (ret == 0) { // simple check to avoid starting the Attach mechanism when // a bogus user creates the file if (st.st_uid == geteuid()) { // 创建AttachListener线程 init(); return true; } } return false; }
```

一开始会 **判断当前进程目录下是否有个.Attach_pid 文件**，如果没有就会在/tmp 下创建一个

/tmp/.Attach_pid
，**当那个文件的 uid 和自己的 uid 是一致的情况下（为了安全）再调用 init 方法**。

```
// Starts the Attach Listener thread void AttachListener::init() { EXCEPTION_MARK; klassOop k = SystemDictionary::resolve_or_fail(vmSymbols::java_lang_Thread(), true, CHECK); instanceKlassHandle klass (THREAD, k); instanceHandle thread_oop = klass->allocate_instance_handle(CHECK); const char thread_name[] = "Attach Listener"; Handle string = java_lang_String::create_from_str(thread_name, CHECK); // Initialize thread_oop to put it into the system threadGroup Handle thread_group (THREAD, Universe::system_thread_group()); JavaValue result(T_VOID); JavaCalls::call_special(&result, thread_oop, klass, vmSymbols::object_initializer_name(), vmSymbols::threadgroup_string_void_signature(), thread_group, string, CHECK); KlassHandle group(THREAD, SystemDictionary::ThreadGroup_klass()); JavaCalls::call_special(&result, thread_group, group, vmSymbols::add_method_name(), vmSymbols::thread_void_signature(), thread_oop, // ARG 1 CHECK); { MutexLocker mu(Threads_lock); JavaThread/* listener_thread = new JavaThread(&Attach_listener_thread_entry); // Check that thread and osthread were created if (listener_thread == NULL || listener_thread->osthread() == NULL) { vm_exit_during_initialization("java.lang.OutOfMemoryError", "unable to create new native thread"); } java_lang_Thread::set_thread(thread_oop(), listener_thread); java_lang_Thread::set_daemon(thread_oop()); listener_thread->set_threadObj(thread_oop()); Threads::add(listener_thread); Thread::start(listener_thread); } }
```

此时水落石出了，看到创建了一个线程，并且取名为 Attach Listener。**再看看 Linux 系统下其子类 LinuxAttachListener 的 init 方法**：

```
int LinuxAttachListener::init() { char path[UNIX_PATH_MAX]; // socket file char initial_path[UNIX_PATH_MAX]; // socket file during setup int listener; // listener socket (file descriptor) // register function to cleanup ::atexit(listener_cleanup); int n = snprintf(path, UNIX_PATH_MAX, "%s/.java_pid%d", os::get_temp_directory(), os::current_process_id()); if (n < (int)UNIX_PATH_MAX) { n = snprintf(initial_path, UNIX_PATH_MAX, "%s.tmp", path); } if (n >= (int)UNIX_PATH_MAX) { return -1; } // create the listener socket listener = ::socket(PF_UNIX, SOCK_STREAM, 0); if (listener == -1) { return -1; } // bind socket struct sockaddr_un addr; addr.sun_family = AF_UNIX; strcpy(addr.sun_path, initial_path); ::unlink(initial_path); int res = ::bind(listener, (struct sockaddr/*)&addr, sizeof(addr)); if (res == -1) { RESTARTABLE(::close(listener), res); return -1; } // put in listen mode, set permissions, and rename into place res = ::listen(listener, 5); if (res == 0) { RESTARTABLE(::chmod(initial_path, S_IREAD|S_IWRITE), res); if (res == 0) { res = ::rename(initial_path, path); } } if (res == -1) { RESTARTABLE(::close(listener), res); ::unlink(initial_path); return -1; } set_path(path); set_listener(listener); return 0; }
```

**看到其创建了一个监听套接字，并创建了一个文件/tmp/.java_pid，这个文件就是客户端之前一直在轮询等待的文件**，随着这个文件的生成，意味着 Attach 的创建过程圆满结束了。

Attach Listener 线程接收到请求时，**具体的请求处理在 attach_listener_thread_entry 方法体中实现**：

```
static void attach_listener_thread_entry(JavaThread/* thread, TRAPS) { os::set_priority(thread, NearMaxPriority); if (AttachListener::pd_init() != 0) { return; } AttachListener::set_initialized(); for (;;) { AttachOperation/* op = AttachListener::dequeue(); if (op == NULL) { return; // dequeue failed or shutdown } ResourceMark rm; bufferedStream st; jint res = JNI_OK; // handle special detachall operation if (strcmp(op->name(), AttachOperation::detachall_operation_name()) == 0) { AttachListener::detachall(); } else { // find the function to dispatch too AttachOperationFunctionInfo/* info = NULL; for (int i=0; funcs[i].name != NULL; i++) { const char/* name = funcs[i].name; assert(strlen(name) <= AttachOperation::name_length_max, "operation <= name_length_max"); if (strcmp(op->name(), name) == 0) { info = &(funcs[i]); break; } } // check for platform dependent attach operation if (info == NULL) { info = AttachListener::pd_find_operation(op->name()); } if (info != NULL) { // dispatch to the function that implements this operation res = (info->func)(op, &st); } else { st.print("Operation %s not recognized!", op->name()); res = JNI_ERR; } } // operation complete - send result and output to client op->complete(res, &st); } }
```

从代码来看就是 **从队列里不断取 AttachOperation，然后找到请求命令对应的方法进行执行**，比如一开始说的 jstack 命令，找到 { “threaddump”, thread_dump }的映射关系，然后执行 thread_dump 方法。

AttachOperation 有很多种类，比如：内存 dump，线程 dump，类信息统计(比如加载的类及大小以及实例个数等)，动态加载 agent，动态设置 vm flag(但是并不是所有的 flag 都可以设置的，因为有些 flag 是在 jvm 启动过程中使用的，是一次性的)，打印 vm flag，获取系统属性等，这些对应的源码（

AttachListener.cpp
）如下：

```
static AttachOperationFunctionInfo funcs[] = { // 第二个参数是命令对应的处理函数 { "agentProperties", get_agent_properties }, { "datadump", data_dump }, { "dumpheap", dump_heap }, { "load", JvmtiExport::load_agent_library }, { "properties", get_system_properties }, { "threaddump", thread_dump }, { "inspectheap", heap_inspection }, { "setflag", set_flag }, { "printflag", print_flag }, { "jcmd", jcmd }, { NULL, NULL } };
```

再来看看其要调用的

AttachListener::dequeue();
：

```
AttachOperation/* AttachListener::dequeue() { JavaThread/* thread = JavaThread::current(); ThreadBlockInVM tbivm(thread); thread->set_suspend_equivalent(); // cleared by handle_special_suspend_equivalent_condition() or // java_suspend_self() via check_and_wait_while_suspended() AttachOperation/* op = LinuxAttachListener::dequeue(); // were we externally suspended while we were waiting? thread->check_and_wait_while_suspended(); return op; }
```

最终会调用的是

LinuxAttachListener::dequeue()
：

```
LinuxAttachOperation/* LinuxAttachListener::dequeue() { for (;;) { int s; // wait for client to connect struct sockaddr addr; socklen_t len = sizeof(addr); // 如果没有请求的话，会一直accept在那里 RESTARTABLE(::accept(listener(), &addr, &len), s); if (s == -1) { return NULL; // log a warning? } // get the credentials of the peer and check the effective uid/guid // - check with jeff on this. struct ucred cred_info; socklen_t optlen = sizeof(cred_info); if (::getsockopt(s, SOL_SOCKET, SO_PEERCRED, (void/*)&cred_info, &optlen) == -1) { int res; RESTARTABLE(::close(s), res); continue; } uid_t euid = geteuid(); gid_t egid = getegid(); if (cred_info.uid != euid || cred_info.gid != egid) { int res; RESTARTABLE(::close(s), res); continue; } // peer credential look okay so we read the request LinuxAttachOperation/* op = read_request(s); if (op == NULL) { int res; RESTARTABLE(::close(s), res); continue; } else { return op; } } }
```

如上代码中可以看到，**如果没有请求的话，会一直 accept 在那里，当来了请求，然后就会创建一个套接字，并读取数据，构建出 LinuxAttachOperation 返回，找到请求对应的操作，调用操作得到结果并把结果写到这个 socket 的文件**，如果你把 socket 的文件删除，jstack/jmap 会出现错误信息 unable to open socket file:........

## 1.3 jstack/jmap 命令流程图

以 jstack 的实现来说明触发 Attach 这一机制进行的过程，**jstack 命令的实现其实是一个叫做 JStack.java 的类**，jstack 命令首先会 attach 到目标 JVM 进程，产生 VirtualMachine 类；Linux 系统下，其实现类为 LinuxVirtualMachine，调用其 remoteDataDump 方法，打印堆栈信息；查看

JStack.java
代码后会走到下面的方法里：

```
private static void runThreadDump(String pid, String args[]) throws Exception { VirtualMachine vm = null; try { // jstack命令首先会attach到目标JVM进程 vm = VirtualMachine.Attach(pid); } catch (Exception x) { String msg = x.getMessage(); if (msg != null) { System.err.println(pid + ": " + msg); } else { x.printStackTrace(); } if ((x instanceof AttachNotSupportedException) && (loadSAClass() != null)) { System.err.println("The -F option can be used when the target " + "process is not responding"); } System.exit(1); } // Cast to HotSpotVirtualMachine as this is implementation specific // method. // 输出堆栈信息 InputStream in = ((HotSpotVirtualMachine)vm).remoteDataDump((Object[])args); // read to EOF and just print output byte b[] = new byte[256]; int n; do { n = in.read(b); if (n > 0) { String s = new String(b, 0, n, "UTF-8"); System.out.print(s); } } while (n > 0); in.close(); vm.detach(); }
```

那么 VirtualMachine 是如何连接到目标 JVM 进程的呢？请注意

VirtualMachine.Attach(pid);
这行代码，触发 Attach pid 的关键，如果是在 Linux 下具体的实现逻辑在

sun.tools.attach.LinuxVirtualMachine
的构造函数：

```
LinuxVirtualMachine(AttachProvider provider, String vmid) throws AttachNotSupportedException, IOException { super(provider, vmid); // This provider only understands pids int pid; try { pid = Integer.parseInt(vmid); } catch (NumberFormatException x) { throw new AttachNotSupportedException("Invalid process identifier"); } // Find the socket file. If not found then we attempt to start the // Attach mechanism in the target VM by sending it a QUIT signal. // Then we attempt to find the socket file again. path = findSocketFile(pid); if (path == null) { File f = createAttachFile(pid); try { // On LinuxThreads each thread is a process and we don't have the // pid of the VMThread which has SIGQUIT unblocked. To workaround // this we get the pid of the "manager thread" that is created // by the first call to pthread_create. This is parent of all // threads (except the initial thread). if (isLinuxThreads) { int mpid; try { mpid = getLinuxThreadsManager(pid); } catch (IOException x) { throw new AttachNotSupportedException(x.getMessage()); } assert(mpid >= 1); sendQuitToChildrenOf(mpid); } else { sendQuitTo(pid); } // give the target VM time to start the Attach mechanism int i = 0; long delay = 200; int retries = (int)(AttachTimeout() / delay); do { try { Thread.sleep(delay); } catch (InterruptedException x) { } path = findSocketFile(pid); i++; } while (i <= retries && path == null); if (path == null) { throw new AttachNotSupportedException( "Unable to open socket file: target process not responding " + "or HotSpot VM not loaded"); } } finally { f.delete(); } } // Check that the file owner/permission to avoid Attaching to // bogus process checkPermissions(path); // Check that we can connect to the process // - this ensures we throw the permission denied error now rather than // later when we attempt to enqueue a command. int s = socket(); try { connect(s, path); } finally { close(s); } }
```

1. 查找/tmp 目录下是否存在

".java_pid"+pid
文件；

1. 如果文件不存在，则首先创建

"/proc/" + pid + "/cwd/" + ".attach_pid" + pid
文件；

1. 通过

kill
命令发送

SIGQUIT
信号给目标 JVM 进程，由于 JVM 里除了信号线程，其他线程都设置了对此信号的屏蔽，因此收不到该信号，于是该信号就传给了

“Signal Dispatcher”
；

1. 目标 JVM 进程接收到信号之后，会在

/tmp
目录下创建

".java_pid"+pid
文件；

1. 当发现

/tmp
目录下存在

".java_pid"+pid
文件，

LinuxVirtualMachine
会通过 connect 系统调用连接到该文件描述符，后续通过该

fd
进行双方的通讯；

JVM 接受 SIGQUIT 信号的相关逻辑处理，\*\*则是在前面

signal_thread_entry
方法中进行实现\*\*。

![]()

jstack/jmap 命令流程图

前面

JStack.java
源码中，输出堆栈信息是通过调用

remoteDataDump
方法实现的，**该方法就是通过往前面提到的 fd 中写入 threaddump 指令，读取返回结果，从而得到目标 JVM 的堆栈信息**。

# 2 Java 代码实现动态 attach Agent

Java 动态 attach Agent 与上面所讲到的

JStack.java
实现基本类似，在 attach 的 java 代码中，使用 sun 自用的 tool.jar 中的 VirtualMachine 的 attach 的方式：

```
VirtualMachine vm = VirtualMachine.attach(processid); vm.loadAgent(agentpath, args)
```

在

HotSpotVirtualMachine.java
中，

loadAgent
方法源码如下：

```
public void loadAgent(String agent, String options) throws AgentLoadException, AgentInitializationException, IOException { String args = agent; if (options != null) { args = args + "=" + options; } try { loadAgentLibrary("instrument", args); } ..... } private void loadAgentLibrary(String agentLibrary, boolean isAbsolute, String options) throws AgentLoadException, AgentInitializationException, IOException { InputStream in = execute("load", agentLibrary, isAbsolute ? "true" : "false", options); try { int result = readInt(in); if (result != 0) { throw new AgentInitializationException("Agent_OnAttach failed", result); } } finally { in.close(); } }
```

在

LinuxVirtualMachine.java
中的

execute
方法：

```
InputStream execute(String cmd, Object ... args) throws AgentLoadException, IOException { assert args.length <= 3; // includes null // did we detach? String p; synchronized (this) { if (this.path == null) { throw new IOException("Detached from target VM"); } p = this.path; } // create UNIX socket int s = socket(); // connect to target VM try { connect(s, p); } catch (IOException x) { close(s); throw x; } IOException ioe = null; // connected - write request // <ver> <cmd> <args...> try { writeString(s, PROTOCOL_VERSION); writeString(s, cmd); for (int i=0; i<3; i++) { if (i < args.length && args[i] != null) { writeString(s, (String)args[i]); } else { writeString(s, ""); } } } catch (IOException x) { ioe = x; } // Create an input stream to read reply SocketInputStream sis = new SocketInputStream(s); // Read the command completion status int completionStatus; try { completionStatus = readInt(sis); } catch (IOException x) { sis.close(); if (ioe != null) { throw ioe; } else { throw x; } } .... }
```

也就是向 socket 的中写入了，格式为：

```
<ver> <cmd> <args...>
```

具体内容为：

```
1 load instrument agentPath=path.jar
```

既然 Load Agent 往 socket 里发了 load 指令，匹配到 JVM 的操作：

```
static AttachOperationFunctionInfo funcs[] = { { "agentProperties", get_agent_properties }, { "datadump", data_dump }, /#ifndef SERVICES_KERNEL { "dumpheap", dump_heap }, /#endif// SERVICES_KERNEL { "load", JvmtiExport::load_agent_library }, { "properties", get_system_properties }, { "threaddump", thread_dump }, { "inspectheap", heap_inspection }, { "setflag", set_flag }, { "printflag", print_flag }, { NULL, NULL } };
```

"load", JvmtiExport::load_agent_library
，具体源码如下：

```
jint JvmtiExport::load_agent_library(AttachOperation/* op, outputStream/* st) { char ebuf[1024]; char buffer[JVM_MAXPATHLEN]; void/* library; jint result = JNI_ERR; const char/* agent = op->arg(0); const char/* absParam = op->arg(1); const char/* options = op->arg(2); bool is_absolute_path = (absParam != NULL) && (strcmp(absParam,"true")==0); if (is_absolute_path) { library = os::dll_load(agent, ebuf, sizeof ebuf); } else { // Try to load the agent from the standard dll directory os::dll_build_name(buffer, sizeof(buffer), Arguments::get_dll_dir(), agent); library = os::dll_load(buffer, ebuf, sizeof ebuf); if (library == NULL) { // not found - try local path char ns[1] = {0}; os::dll_build_name(buffer, sizeof(buffer), ns, agent); library = os::dll_load(buffer, ebuf, sizeof ebuf); } } if (library != NULL) { // Lookup the Agent_OnAttach function OnAttachEntry_t on_attach_entry = NULL; const char /*on_attach_symbols[] = AGENT_ONATTACH_SYMBOLS; for (uint symbol_index = 0; symbol_index < ARRAY_SIZE(on_attach_symbols); symbol_index++) { on_attach_entry = CAST_TO_FN_PTR(OnAttachEntry_t, os::dll_lookup(library, on_attach_symbols[symbol_index])); if (on_attach_entry != NULL) break; } if (on_attach_entry == NULL) { // Agent_OnAttach missing - unload library os::dll_unload(library); } else { // Invoke the Agent_OnAttach function JavaThread/* THREAD = JavaThread::current(); { extern struct JavaVM_ main_vm; JvmtiThreadEventMark jem(THREAD); JvmtiJavaThreadEventTransition jet(THREAD); result = (/*on_attach_entry)(&main_vm, (char/*)options, NULL); } if (HAS_PENDING_EXCEPTION) { CLEAR_PENDING_EXCEPTION; } if (result == JNI_OK) { Arguments::add_loaded_agent(agent, (char/*)options, is_absolute_path, library); } // Agent_OnAttach executed so completion status is JNI_OK st->print_cr("%d", result); result = JNI_OK; } } return result; } /#define AGENT_ONATTACH_SYMBOLS {"Agent_OnAttach"}
```

# 3 执行 Instrument 的 Agent on attach

加载 instrument 的动态库，并且调用方法 instrument 动态库中的

Agent_OnAttach
方法：

```
JNIEXPORT jint JNICALL Agent_OnAttach(JavaVM/* vm, char /*args, void /* reserved) { ..... initerror = createNewJPLISAgent(vm, &agent); if ( initerror == JPLIS_INIT_ERROR_NONE ) { ...... if (parseArgumentTail(args, &jarfile, &options) != 0) { return JNI_ENOMEM; } attributes = readAttributes( jarfile ); if (attributes == NULL) { fprintf(stderr, "Error opening zip file or JAR manifest missing: %s\n", jarfile); free(jarfile); if (options != NULL) free(options); return AGENT_ERROR_BADJAR; } agentClass = getAttribute(attributes, "Agent-Class"); if (agentClass == NULL) { fprintf(stderr, "Failed to find Agent-Class manifest attribute from %s\n", jarfile); free(jarfile); if (options != NULL) free(options); freeAttributes(attributes); return AGENT_ERROR_BADJAR; } if (appendClassPath(agent, jarfile)) { fprintf(stderr, "Unable to add %s to system class path " "- not supported by system class loader or configuration error!\n", jarfile); free(jarfile); if (options != NULL) free(options); freeAttributes(attributes); return AGENT_ERROR_NOTONCP; } oldLen = strlen(agentClass); newLen = modifiedUtf8LengthOfUtf8(agentClass, oldLen); if (newLen == oldLen) { agentClass = strdup(agentClass); } else { char/* str = (char/*)malloc( newLen+1 ); if (str != NULL) { convertUtf8ToModifiedUtf8(agentClass, oldLen, str, newLen); } agentClass = str; } if (agentClass == NULL) { free(jarfile); if (options != NULL) free(options); freeAttributes(attributes); return JNI_ENOMEM; } bootClassPath = getAttribute(attributes, "Boot-Class-Path"); if (bootClassPath != NULL) { appendBootClassPath(agent, jarfile, bootClassPath); } convertCapabilityAtrributes(attributes, agent); success = createInstrumentationImpl(jni_env, agent); jplis_assert(success); //* /* Turn on the ClassFileLoadHook. /*/ if (success) { success = setLivePhaseEventHandlers(agent); jplis_assert(success); } if (success) { success = startJavaAgent(agent, jni_env, agentClass, options, agent->mAgentmainCaller); } if (!success) { fprintf(stderr, "Agent failed to start!\n"); result = AGENT_ERROR_STARTFAIL; } if (options != NULL) free(options); free(agentClass); freeAttributes(attributes); } return result; }
```

上面代码里一开始的

createNewJPLISAgent
和

on_load
是一样的注册了一些钩子函数，具体详情可参考：[《JVMTI Agent 工作原理及核心源码分析》](https://www.jianshu.com/p/7b2072513819)。

在上面的

Agent_OnAttach
代码中我们也看到了，读取加载的 jar 中 MANIFEST Agent-Class 的配置：

```
agentClass = getAttribute(attributes, "Agent-Class");
```

创建生成 sun.instrument.InstrumentationImpl 对象：

```
success = createInstrumentationImpl(jni_env, agent);
```

通过 InstrumentationImpl 对象中的

loadClassAndCallAgentmain
方法去初始化在 Agent-Class 中的类，并调用 class 里的

agentmain
的方法：

```
success = startJavaAgent(agent, jni_env, agentClass, options, agent->mAgentmainCaller);
```

也就是说定义的

on_attach
的 class 里需要有

agentmain
的方法实现：

```
public class MyTransformer { public static void agentmain(String agentArgs, Instrumentation inst) throws ClassNotFoundException, UnmodifiableClassException, NotFoundException, CannotCompileException, IOException{ .... } }
```

50 人点赞

[01\_深入浅出 Java]()
"赞赏我的文章给您带来收获"赞赏支持还没有人赞赏，支持一下

[![  ](https://upload.jianshu.io/users/upload_avatars/2062729/105612dd-1e85-48e5-b3fa-a13cf07fddb6.jpg?imageMogr2/auto-orient/strip|imageView2/1/w/100/h/100/format/webp)]()

[猿码道]("猿码道")以知识为海，以人生为舟，自书海泛舟，而陶然以乐。

总资产 164 (约 13.03 元)共写了 67.1W 字获得 12,336 个赞共 6,628 个粉丝
关注
