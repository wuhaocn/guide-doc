# javascript

- JavaScript（简称“JS”） 是一种具有函数优先的轻量级，解释型或即时编译型的编程语言。
  虽然它是作为开发 Web 页面的脚本语言而出名的，但是它也被用到了很多非浏览器环境中，
  JavaScript 基于原型编程、多范式的动态脚本语言，并且支持面向对象、命令式和声明式（如函数式编程）风格。

- JavaScript 在 1995 年由 Netscape 公司的 Brendan Eich，在网景导航者浏览器上首次设计实现而成。
  因为 Netscape 与 Sun 合作，Netscape 管理层希望它外观看起来像 Java，因此取名为 JavaScript。但实际上它的语法风格与 Self 及 Scheme 较为接近。

- JavaScript 的标准是 ECMAScript 。截至 2012 年，所有浏览器都完整的支持 ECMAScript 5.1，旧版本的浏览器至少支持 ECMAScript 3 标准。
  2015 年 6 月 17 日，ECMA 国际组织发布了 ECMAScript 的第六版，该版本正式名称为 ECMAScript 2015，但通常被称为 ECMAScript 6 或者 ES6。

## javascript 组成部分

![javascript组成](https://gss2.bdstatic.com/-fo3dSag_xI4khGkpoWK1HF6hhy/baike/s%3D250/sign=103c88deb21c8701d2b6b5e3177f9e6e/730e0cf3d7ca7bcb3409f115bf096b63f624a89d.jpg)

- ECMAScript，描述了该语 言的语法和基本对象。
- 文档对象模型（DOM），描述处理网页内容的方法和接口。
- 浏览器对象模型（BOM），描述与浏览器进行交互的方法和接口。

## javascript 基本特点

JavaScript 是一种属于网络的脚本语言,已经被广泛用于 Web 应用开发,常用来为网页添加各式各样的动态功能,为用户提供更流畅美观的浏览效果。通常 JavaScript 脚本是通过嵌入在 HTML 中来实现自身的功能的。

1. 是一种解释性脚本语言（代码不进行[预编译]()）。
2. 主要用来向[HTML]()（[标准通用标记语言]()下的一个应用）页面添加交互行为。
3. 可以直接嵌入 HTML 页面，但写成单独的[js]()文件有利于结构和行为的[分离]()。
4. 跨平台特性，在绝大多数浏览器的支持下，可以在多种平台下运行（如[Windows]()、[Linux]()、[Mac]()、[Android]()、[iOS]()等）。
5. Javascript 脚本语言同其他语言一样，有它自身的基本数据类型，表达式和[算术运算符]()及程序的基本程序框架。Javascript 提供了四种基本的数据类型和两种特殊数据类型用来处理数据和文字。而变量提供存放信息的地方，表达式则可以完成较复杂的信息处理。

## javascript 日常用途

1. 嵌入[动态文本]()于 HTML 页面。
2. 对浏览器事件做出响应。
3. 读写[HTML 元素]()。
4. 在数据被提交到服务器之前验证数据。
5. 检测访客的浏览器信息。
6. 控制[cookies]()，包括创建和修改等。
7. 基于[Node.js]()技术进行服务器端编程。

## javascript 历史

它最初由 Netscape 的 Brendan Eich 设计。JavaScript 是甲骨文公司的注册商标。Ecma 国际以 JavaScript 为基础制定了 ECMAScript 标准。  
JavaScript 也可以用于其他场合，如服务器端编程。完整的 JavaScript 实现包含三个部分：ECMAScript，文档对象模型，浏览器对象模型。

Netscape 在最初将其脚本语言命名为 LiveScript，后来 Netscape 在与 Sun 合作之后将其改名为 JavaScript。  
JavaScript 最初受 Java 启发而开始设计的，目的之一就是“看上去像 Java”，因此语法上有类似之处，一些名称和命名规范也借自 Java。  
但 JavaScript 的主要设计原则源自 Self 和 Scheme。JavaScript 与 Java 名称上的近似，是当时 Netscape 为了营销考虑与 Sun 微系统达成协议的结果。  
为了取得技术优势，微软推出了 JScript 来迎战 JavaScript 的脚本语言。为了互用性，Ecma 国际（前身为欧洲计算机制造商协会）创建了 ECMA-262 标准（ECMAScript）。  
两者都属于 ECMAScript 的实现。尽管 JavaScript 作为给非程序人员的脚本语言，而非作为给程序人员的脚本语言来推广和宣传，但是 JavaScript 具有非常丰富的特性。

发展初期，JavaScript 的标准并未确定，同期有 Netscape 的 JavaScript，微软的 JScript 和 CEnvi 的 ScriptEase 三足鼎立。  
1997 年，在 ECMA（欧洲计算机制造商协会）的协调下，由 Netscape、Sun、微软、Borland 组成的工作组确定统一标准：ECMA-262。

## javascript 特性

JavaScript 脚本语言具有以下特点:

- (1)[脚本语言]()。JavaScript 是一种解释型的脚本语言,C、[C++]()等语言先[编译]()后执行,而 JavaScript 是在程序的运行过程中逐行进行解释。
- (2)基于对象。JavaScript 是一种基于对象的脚本语言,它不仅可以创建对象,也能使用现有的对象。
- (3)简单。JavaScript 语言中采用的是弱类型的变量类型,对使用的数据类型未做出严格的要求,是基于 Java 基本语句和控制的脚本语言,其设计简单紧凑。
- (4)动态性。JavaScript 是一种采用事件驱动的脚本语言,它不需要经过 Web 服务器就可以对用户的输入做出响应。
  在访问一个网页时,鼠标在网页中进行鼠标点击或上下移、窗口移动等操作 JavaScript 都可直接对这些事件给出相应的响应。
- (5)跨平台性。JavaScript 脚本语言不依赖于操作系统,仅需要浏览器的支持。因此一个 JavaScript 脚本在编写后可以带到任意机器上使用,
  前提上机器上的浏览器支持 JavaScript 脚本语言,JavaScript 已被大多数的浏览器所支持。
  不同于服务器端脚本语言，例如[PHP]()与[ASP]()，JavaScript 主要被作为客户端脚本语言在用户的浏览器上运行，不需要服务器的支持。
  所以在早期程序员比较青睐于 JavaScript 以减少对服务器的负担，而与此同时也带来另一个问题：安全性。
  而随着服务器的强壮，虽然程序员更喜欢运行于服务端的脚本以保证安全，但 JavaScript 仍然以其跨平台、容易上手等优势大行其道。
  同时，有些特殊功能（如[AJAX]()）必须依赖 Javascript 在客户端进行支持。随着引擎如 V8 和框架如[Node.js]()的发展，
  及其事件驱动及[异步 IO]()等特性，JavaScript 逐渐被用来编写服务器端程序。

## javascript 编程

JavaScript 是一种脚本语言，其源代码在发往客户端运行之前不需经过[编译]()，而是将文本格式的字符代码发送给浏览器由浏览器解释运行。  
直译语言的弱点是安全性较差，而且在 JavaScript 中，如果一条运行不了，那么下面的语言也无法运行。而其解决办法就是于使用 try{}catch(){}︰

Javascript 被归类为直译语言，因为主流的引擎都是每次运行时加载代码并解译。  
V8 是将所有代码解译后再开始运行，其他引擎则是逐行解译（[SpiderMonkey]()会将解译过的指令暂存，以提高性能，称为实时编译），  
但由于 V8 的核心部分多数用 Javascript 撰写（而 SpiderMonkey 是用[C++]()），因此在不同的测试上，两者性能互有优劣。  
与其相对应的是编译语言，例如[C 语言]()，以编译语言编写的程序在运行之前，必须经过编译，将代码编译为机器码，再加以运行。

## javascript 版本

JavaScript 已经被[Netscape]()公司提交给[ECMA]()制定为标准，称之为[ECMAScript]()，标准编号 ECMA-262。  
最新版为[ECMAScript 6]()。符合 ECMA-262 3rd Edition 标准的实现有：

1. Microsoft 公司的[JScript]().
2. Mozilla 的 JavaScript-C（C 语言实现），现名[SpiderMonkey]()
3. Mozilla 的[Rhino]()（Java 实现）
4. Digital Mars 公司的 DMDScript
5. Google 公司的[V8]()
6. [WebKit]()

版本 说明 实现
ECMA v1 标准化了 JavaScript1.1 的基本特性，并添加了一些新特性。没有标准化 switch 语句和正则表达式。 由 Netscape 4.5 和 IE 4 实现。
ECMA v2 ECMA v1 的维护版本，只添加了说明 由 Netscape 4.5 和 IE 4 实现。
ECMA v3 标准化了 switch 语句、异常处理和正则表达式。 由 Mozilla、Netscape 6 和 IE 5.5 实现。
