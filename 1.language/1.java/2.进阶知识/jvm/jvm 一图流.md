<div data-v-15c3fc70="" data-id="5f1e31825188252e906fa9da" itemprop="articleBody" class="article-content"><h1 class="heading" data-id="heading-0">JVM一图流</h1>
<h1 class="heading" data-id="heading-1">总纲</h1>
<p></p><figure><img alt="Java Virtual Machine architecture diagram" class="lazyload inited loaded" data-src="https://javatutorial.net/wp-content/uploads/2017/10/jvm-architecture-992x1024.png" data-width="800" data-height="600" src="https://javatutorial.net/wp-content/uploads/2017/10/jvm-architecture-992x1024.png"><figcaption></figcaption></figure><p></p>
<h2 class="heading" data-id="heading-2">类手绘版:</h2>
<p></p><figure><img alt="img" class="lazyload inited loaded" data-src="https://user-gold-cdn.xitu.io/2020/7/27/1738df1209e1359f?imageView2/0/w/1280/h/960/format/webp/ignore-error/1" data-width="1280" data-height="576" src="https://user-gold-cdn.xitu.io/2020/7/27/1738df1209e1359f?imageView2/0/w/1280/h/960/format/webp/ignore-error/1"><figcaption></figcaption></figure><p></p>
<h2 class="heading" data-id="heading-3">全流程</h2>
<p></p><figure><img alt="img" class="lazyload inited loaded" data-src="https://user-gold-cdn.xitu.io/2020/7/27/1738df07f5695d2f?imageView2/0/w/1280/h/960/format/webp/ignore-error/1" data-width="557" data-height="617" src="https://user-gold-cdn.xitu.io/2020/7/27/1738df07f5695d2f?imageView2/0/w/1280/h/960/format/webp/ignore-error/1"><figcaption></figcaption></figure><p></p>
<h1 class="heading" data-id="heading-4">classLoader</h1>
<h2 class="heading" data-id="heading-5">class文件结构</h2>
<p><a target="_blank" href="https://zhuanlan.zhihu.com/p/149876413" rel="nofollow noopener noreferrer">zhuanlan.zhihu.com/p/149876413</a></p>
<p><a target="_blank" href="https://my.oschina.net/u/2246410/blog/1800670" rel="nofollow noopener noreferrer">my.oschina.net/u/2246410/b…</a></p>
<p></p><figure><img alt="image-20200726112348450"><figcaption></figcaption></figure><p></p>
<p></p><figure><img alt="preview" class="lazyload inited loaded" data-src="https://user-gold-cdn.xitu.io/2020/7/27/1738df07f5e16b0e?imageView2/0/w/1280/h/960/format/webp/ignore-error/1" data-width="849" data-height="993" src="https://user-gold-cdn.xitu.io/2020/7/27/1738df07f5e16b0e?imageView2/0/w/1280/h/960/format/webp/ignore-error/1"><figcaption></figcaption></figure><p></p>
<h2 class="heading" data-id="heading-6">双亲委托加载机制</h2>
<p></p><figure><img alt="img" class="lazyload inited loaded" data-src="https://user-gold-cdn.xitu.io/2020/7/27/1738df0826bfb358?imageView2/0/w/1280/h/960/format/webp/ignore-error/1" data-width="746" data-height="724" src="https://user-gold-cdn.xitu.io/2020/7/27/1738df0826bfb358?imageView2/0/w/1280/h/960/format/webp/ignore-error/1"><figcaption></figcaption></figure><p></p>
<p></p><figure><img alt="img" class="lazyload inited loaded" data-src="https://user-gold-cdn.xitu.io/2020/7/27/1738df082e3d59c0?imageView2/0/w/1280/h/960/format/webp/ignore-error/1" data-width="708" data-height="1135" src="https://user-gold-cdn.xitu.io/2020/7/27/1738df082e3d59c0?imageView2/0/w/1280/h/960/format/webp/ignore-error/1"><figcaption></figcaption></figure><p></p>
<h2 class="heading" data-id="heading-7">对象在内存中的结构</h2>
<h3 class="heading" data-id="heading-8">简略:</h3>
<p></p><figure><img alt="img" class="lazyload inited loaded" data-src="https://user-gold-cdn.xitu.io/2020/7/27/1738df08598f8c80?imageView2/0/w/1280/h/960/format/webp/ignore-error/1" data-width="407" data-height="255" src="https://user-gold-cdn.xitu.io/2020/7/27/1738df08598f8c80?imageView2/0/w/1280/h/960/format/webp/ignore-error/1"><figcaption></figcaption></figure><p></p>
<h3 class="heading" data-id="heading-9">详细</h3>
<p></p><figure><img alt="image" class="lazyload inited loaded" data-src="https://user-gold-cdn.xitu.io/2020/7/27/1738df0827191c3e?imageView2/0/w/1280/h/960/format/webp/ignore-error/1" data-width="895" data-height="559" src="https://user-gold-cdn.xitu.io/2020/7/27/1738df0827191c3e?imageView2/0/w/1280/h/960/format/webp/ignore-error/1"><figcaption></figcaption></figure><p></p>
<p></p><figure><img alt="img" class="lazyload inited loaded" data-src="https://user-gold-cdn.xitu.io/2020/7/27/1738df085626d887?imageView2/0/w/1280/h/960/format/webp/ignore-error/1" data-width="720" data-height="595" src="https://user-gold-cdn.xitu.io/2020/7/27/1738df085626d887?imageView2/0/w/1280/h/960/format/webp/ignore-error/1"><figcaption></figcaption></figure><p></p>
<h1 class="heading" data-id="heading-10">Runtime Data Area</h1>
<p>主要参考:</p>
<p><a target="_blank" href="https://blog.csdn.net/u010349169/article/details/40043991" rel="nofollow noopener noreferrer">blog.csdn.net/u010349169/…</a></p>
<h2 class="heading" data-id="heading-11">整体结构</h2>
<p></p><figure><img alt="img" class="lazyload inited loaded" data-src="https://user-gold-cdn.xitu.io/2020/7/27/1738df0865d55e8d?imageView2/0/w/1280/h/960/format/webp/ignore-error/1" data-width="1080" data-height="475" src="https://user-gold-cdn.xitu.io/2020/7/27/1738df0865d55e8d?imageView2/0/w/1280/h/960/format/webp/ignore-error/1"><figcaption></figcaption></figure><p></p>
<p></p><figure><img alt="img" class="lazyload inited loaded" data-src="https://user-gold-cdn.xitu.io/2020/7/27/1738df0867fe294f?imageView2/0/w/1280/h/960/format/webp/ignore-error/1" data-width="848" data-height="1219" src="https://user-gold-cdn.xitu.io/2020/7/27/1738df0867fe294f?imageView2/0/w/1280/h/960/format/webp/ignore-error/1"><figcaption></figcaption></figure><p></p>
<h2 class="heading" data-id="heading-12">栈</h2>
<p></p><figure><img alt="img" class="lazyload inited loaded" data-src="https://user-gold-cdn.xitu.io/2020/7/27/1738df088298792c?imageView2/0/w/1280/h/960/format/webp/ignore-error/1" data-width="850" data-height="744" src="https://user-gold-cdn.xitu.io/2020/7/27/1738df088298792c?imageView2/0/w/1280/h/960/format/webp/ignore-error/1"><figcaption></figcaption></figure><p></p>
<h2 class="heading" data-id="heading-13">栈帧</h2>
<p></p><figure><img alt="img" class="lazyload inited loaded" data-src="https://user-gold-cdn.xitu.io/2020/7/27/1738df088f3074ea?imageView2/0/w/1280/h/960/format/webp/ignore-error/1" data-width="793" data-height="847" src="https://user-gold-cdn.xitu.io/2020/7/27/1738df088f3074ea?imageView2/0/w/1280/h/960/format/webp/ignore-error/1"><figcaption></figcaption></figure><p></p>
<h2 class="heading" data-id="heading-14">方法区</h2>
<p></p><figure><img alt="img" class="lazyload inited loaded" data-src="https://user-gold-cdn.xitu.io/2020/7/27/1738df088f2a9b19?imageView2/0/w/1280/h/960/format/webp/ignore-error/1" data-width="816" data-height="1280" src="https://user-gold-cdn.xitu.io/2020/7/27/1738df088f2a9b19?imageView2/0/w/1280/h/960/format/webp/ignore-error/1"><figcaption></figcaption></figure><p></p>
<h2 class="heading" data-id="heading-15">堆</h2>
<p></p><figure><img alt="img" class="lazyload inited loaded" data-src="https://user-gold-cdn.xitu.io/2020/7/27/1738df1240a1be4f?imageView2/0/w/1280/h/960/format/webp/ignore-error/1" data-width="1280" data-height="722" src="https://user-gold-cdn.xitu.io/2020/7/27/1738df1240a1be4f?imageView2/0/w/1280/h/960/format/webp/ignore-error/1"><figcaption></figcaption></figure><p></p>
<h1 class="heading" data-id="heading-16">Execution Engine</h1>
<h2 class="heading" data-id="heading-17">JIT</h2>
<h2 class="heading" data-id="heading-18">GC</h2>
<h3 class="heading" data-id="heading-19">引用相关</h3>
<p></p><figure><img alt="WeChatWorkScreenshot_5d441425-a469-411d-b8f2-767e9eb67385" class="lazyload inited loaded" data-src="https://user-gold-cdn.xitu.io/2020/7/27/1738df08c849e911?imageView2/0/w/1280/h/960/format/webp/ignore-error/1" data-width="1280" data-height="934" src="https://user-gold-cdn.xitu.io/2020/7/27/1738df08c849e911?imageView2/0/w/1280/h/960/format/webp/ignore-error/1"><figcaption></figcaption></figure><p></p>
<p></p><figure><img alt="Image" class="lazyload inited loaded" data-src="https://user-gold-cdn.xitu.io/2020/7/27/1738df08e2663f6d?imageView2/0/w/1280/h/960/format/webp/ignore-error/1" data-width="1280" data-height="628" src="https://user-gold-cdn.xitu.io/2020/7/27/1738df08e2663f6d?imageView2/0/w/1280/h/960/format/webp/ignore-error/1"><figcaption></figcaption></figure><p></p>
<h3 class="heading" data-id="heading-20">算法</h3>
<p><a target="_blank" href="https://baijiahao.baidu.com/s?id=1632054498996744393&amp;wfr=spider&amp;for=pc" rel="nofollow noopener noreferrer">baijiahao.baidu.com/s?id=163205…</a>
<a target="_blank" href="https://zhuanlan.zhihu.com/p/106707595" rel="nofollow noopener noreferrer">zhuanlan.zhihu.com/p/106707595</a></p>
<p></p><figure><img alt="image-20200726111507098" class="lazyload inited loaded" data-src="https://user-gold-cdn.xitu.io/2020/7/27/1738df08eb8a4eb0?imageView2/0/w/1280/h/960/format/webp/ignore-error/1" data-width="1280" data-height="1048" src="https://user-gold-cdn.xitu.io/2020/7/27/1738df08eb8a4eb0?imageView2/0/w/1280/h/960/format/webp/ignore-error/1"><figcaption></figcaption></figure><p></p>
<h4 class="heading" data-id="heading-21">分代回收算法:</h4>
<p>分代回收算法https://zhuanlan.zhihu.com/p/84338255</p>
<p></p><figure><img alt="image-20200726111648848" class="lazyload inited loaded" data-src="https://user-gold-cdn.xitu.io/2020/7/27/1738df08f7c26668?imageView2/0/w/1280/h/960/format/webp/ignore-error/1" data-width="1280" data-height="912" src="https://user-gold-cdn.xitu.io/2020/7/27/1738df08f7c26668?imageView2/0/w/1280/h/960/format/webp/ignore-error/1"><figcaption></figcaption></figure><p></p>
<h1 class="heading" data-id="heading-22">JNI</h1>
<p></p><figure><img alt="Figure 7-1." class="lazyload inited loaded" data-src="https://user-gold-cdn.xitu.io/2020/7/27/1738df1231ab4b5b?imageView2/0/w/1280/h/960/format/webp/ignore-error/1" data-width="402" data-height="340" src="https://user-gold-cdn.xitu.io/2020/7/27/1738df1231ab4b5b?imageView2/0/w/1280/h/960/format/webp/ignore-error/1"><figcaption></figcaption></figure><p></p>
<p></p><figure><img alt="Image for post" class="lazyload inited loaded" data-src="https://miro.medium.com/max/3143/1*3jnCbOPGbR1U5IN5G1i7-Q.png" data-width="800" data-height="600" src="https://miro.medium.com/max/3143/1*3jnCbOPGbR1U5IN5G1i7-Q.png"><figcaption></figcaption></figure><p></p>
<h1 class="heading" data-id="heading-23">相关博客</h1>
<h2 class="heading" data-id="heading-24">jvm图解-经典</h2>
<p><a target="_blank" href="https://blog.csdn.net/u010349169/category_2630089.html" rel="nofollow noopener noreferrer">blog.csdn.net/u010349169/…</a></p>
<p><a target="_blank" href="https://blog.csdn.net/csdnliuxin123524/article/details/81303711" rel="nofollow noopener noreferrer">blog.csdn.net/csdnliuxin1…</a></p>
</div>