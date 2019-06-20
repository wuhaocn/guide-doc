<h1 class="title">KMP算法 理解与实现</h1>
<div class="show-content" data-note-content="">
<div class="show-content-free">
<p><code>参考：</code></p>
<p><a href="https://www.jianshu.com/p/d4cf13b32111">https://www.jianshu.com/p/d4cf13b32111</a><code></code></p>
<p><code>文中对KMP算法的匹配过程以及&ldquo;部分匹配表&rdquo;具体代表什么，都解释的十分简洁明了，看过之后也算是对KMP算法有了一个直观的了解。</code><br /><code>下面我想就算法的具体实现，尤其是&ldquo;部分匹配表&rdquo;的生成（个人认为KMP算法实现中，最不容易理解的部分），进行一些分析。</code></p>
<h3>KMP算法具体实现</h3>
<p>KMP算法的主体是，在失去匹配时，查询最后一个匹配字符所对应的&ldquo;部分匹配表&ldquo;中的值，然后向前移动，移动位数为：</p>
<p><code>移动位数 = 已匹配的字符数 - 对应的部分匹配值</code></p>
<p>比如对如下的匹配：</p>
<div class="image-package">
<div class="image-container">
<div class="image-container-fill">&nbsp;</div>
<div class="image-view" data-width="577" data-height="130"><img class="" src="https://upload-images.jianshu.io/upload_images/679154-4b19618459179ecc.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/577/format/webp" data-original-src="//upload-images.jianshu.io/upload_images/679154-4b19618459179ecc.jpg" data-original-width="577" data-original-height="130" data-original-format="image/jpeg" data-original-filesize="17615" /></div>
</div>
</div>
<p>在<strong>D</strong>处失去匹配，那么查询最后一个匹配字符<strong>B</strong>在部分匹配表中的值为<strong>2</strong>。<br />则向前移动6-2=<strong>4</strong>位。（其实就相当于从搜索词的第二位开始重新进行比较）</p>
<div class="image-package">
<div class="image-container">
<div class="image-container-fill">&nbsp;</div>
<div class="image-view" data-width="645" data-height="189"><img class="" src="https://upload-images.jianshu.io/upload_images/679154-0cb36dfe404013ab.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/645/format/webp" data-original-src="//upload-images.jianshu.io/upload_images/679154-0cb36dfe404013ab.jpg" data-original-width="645" data-original-height="189" data-original-format="image/jpeg" data-original-filesize="29561" /></div>
</div>
</div>
<p>下面是算法主体的实现<br />这里的<code>next</code>数组即&ldquo;部分匹配表&rdquo;。注意因为搜索词最后一位对应的部分匹配值是没有意义的，所以为了编程方便，</p>
<p>我们将&rdquo;部分匹配表&ldquo;整体向后移一位，并把第一位设为-1。</p>
<pre class="hljs java"><code class="java"><span class="hljs-comment">//Java</span>

<span class="hljs-comment">/**
* KMP算法.&lt;br/&gt;
* 在目标字符串中对搜索词进行搜索。&lt;br/&gt;
* 
* <span class="hljs-doctag">@param</span> t 目标字符串
* <span class="hljs-doctag">@param</span> p 搜搜词
* <span class="hljs-doctag">@return</span> 搜索词第一次匹配到的起始位置或-1
*/</span>
<span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">int</span> <span class="hljs-title">KMP</span><span class="hljs-params">(String t, String p)</span></span>{
    <span class="hljs-keyword">char</span>[] target = t.toCharArray();
    <span class="hljs-keyword">char</span>[] pattern = p.toCharArray();
    <span class="hljs-comment">// 目标字符串下标</span>
    <span class="hljs-keyword">int</span> i = <span class="hljs-number">0</span>;
    <span class="hljs-comment">// 搜索词下标</span>
    <span class="hljs-keyword">int</span> j = <span class="hljs-number">0</span>;
    <span class="hljs-comment">// 整体右移一位的部分匹配表</span>
    <span class="hljs-keyword">int</span>[] next = getNext(pattern);

    <span class="hljs-keyword">while</span> (i &lt; target.length &amp;&amp; j &lt; patter.length) {
        <span class="hljs-comment">// j == -1 表示从搜索词最开始进行匹配</span>
        <span class="hljs-keyword">if</span> (j == -<span class="hljs-number">1</span> || target[i] == pattern[j]) {
            i++;
            j++;
        <span class="hljs-comment">// 匹配失败时，查询&ldquo;部分匹配表&rdquo;，得到搜索词位置j以前的最大共同前后缀长度</span>
        <span class="hljs-comment">// 将j移动到最大共同前后缀长度的后一位，然后再继续进行匹配</span>
        } <span class="hljs-keyword">else</span> {
            j = next[j];
        }
    }

    <span class="hljs-comment">// 搜索词每一位都能匹配成功，返回匹配的的起始位置</span>
    <span class="hljs-keyword">if</span> (j == pattern.length)
        <span class="hljs-keyword">return</span> i - j;
    <span class="hljs-keyword">else</span>
        <span class="hljs-keyword">return</span> -<span class="hljs-number">1</span>;
}
</code></pre>
<p>KMP算法的搜索过程还是比较好理解的。<br />接下来最容易被绕进去的部分来了，求解&ldquo;部分匹配表&rdquo;即<code>next</code>数组。</p>
<h3>部分匹配表(next数组)的生成</h3>
<p>其实，求next数组的过程完全可以看成字符串匹配的过程，即以搜索词为主字符串，以搜索词的<strong>前缀</strong>为目标字符串，</p>
<p>一旦字符串匹配成功，那么当前的next值就是匹配成功的字符串的长度。<br />具体来说，就是从模式字符串的第一位(<strong>注意，不包括第0位</strong>)开始对自身进行匹配运算。 在任一位置，能匹配的最长长度就是当前位置的next值，如下图所示。<br />(这里next数组下标从1开始表示)</p>
<div class="image-package">
<div class="image-container">
<div class="image-container-fill">&nbsp;</div>
<div class="image-view" data-width="720" data-height="343"><img class="" src="https://upload-images.jianshu.io/upload_images/679154-55e3ce1012c52c3b.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/720/format/webp" data-original-src="//upload-images.jianshu.io/upload_images/679154-55e3ce1012c52c3b.jpg" data-original-width="720" data-original-height="343" data-original-format="image/jpeg" data-original-filesize="11408" /></div>
</div>
</div>
<div class="image-package">
<div class="image-container">
<div class="image-container-fill">&nbsp;</div>
<div class="image-view" data-width="720" data-height="349"><img class="" src="https://upload-images.jianshu.io/upload_images/679154-d17ed028ce5a4099.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/720/format/webp" data-original-src="//upload-images.jianshu.io/upload_images/679154-d17ed028ce5a4099.jpg" data-original-width="720" data-original-height="349" data-original-format="image/jpeg" data-original-filesize="11206" /></div>
</div>
</div>
<div class="image-package">
<div class="image-container">
<div class="image-container-fill">&nbsp;</div>
<div class="image-view" data-width="720" data-height="340"><img class="" src="https://upload-images.jianshu.io/upload_images/679154-b1c1d019d99536b7.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/720/format/webp" data-original-src="//upload-images.jianshu.io/upload_images/679154-b1c1d019d99536b7.jpg" data-original-width="720" data-original-height="340" data-original-format="image/jpeg" data-original-filesize="10968" /></div>
</div>
</div>
<div class="image-package">
<div class="image-container">
<div class="image-container-fill">&nbsp;</div>
<div class="image-view" data-width="720" data-height="332"><img class="" src="https://upload-images.jianshu.io/upload_images/679154-057c0e1b20deb543.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/720/format/webp" data-original-src="//upload-images.jianshu.io/upload_images/679154-057c0e1b20deb543.jpg" data-original-width="720" data-original-height="332" data-original-format="image/jpeg" data-original-filesize="10546" /></div>
</div>
</div>
<div class="image-package">
<div class="image-container">
<div class="image-container-fill">&nbsp;</div>
<div class="image-view" data-width="720" data-height="355"><img class="" src="https://upload-images.jianshu.io/upload_images/679154-537d729e32a1e62f.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/720/format/webp" data-original-src="//upload-images.jianshu.io/upload_images/679154-537d729e32a1e62f.jpg" data-original-width="720" data-original-height="355" data-original-format="image/jpeg" data-original-filesize="11191" /></div>
</div>
</div>
<p>下面是算法的具体实现</p>
<pre class="hljs java"><code class="java"><span class="hljs-comment">//Java</span>

<span class="hljs-comment">/**
* 生成部分匹配表.&lt;br/&gt;
* 生成搜索词的部分匹配表&lt;br/&gt;
* 
* <span class="hljs-doctag">@param</span> p 搜搜词
* <span class="hljs-doctag">@return</span> 部分匹配表
*/</span>
<span class="hljs-keyword">private</span> <span class="hljs-keyword">int</span>[] getNext(String pattern) {
    <span class="hljs-keyword">char</span>[] p = pattern.toCharArray();
    <span class="hljs-keyword">int</span>[] next = <span class="hljs-keyword">new</span> <span class="hljs-keyword">int</span>[p.length];
  <span class="hljs-comment">// 第一位设为-1，方便判断当前位置是否为搜索词的最开始</span>
    next[<span class="hljs-number">0</span>] = -<span class="hljs-number">1</span>;
    <span class="hljs-keyword">int</span> i = <span class="hljs-number">0</span>;
    <span class="hljs-keyword">int</span> j = -<span class="hljs-number">1</span>;

    <span class="hljs-keyword">while</span>(i &lt; p.length - <span class="hljs-number">1</span>) {
        <span class="hljs-keyword">if</span> (j == -<span class="hljs-number">1</span> || p[i] == p[j]) {
            i++;
            j++;
            next[i] = j;
        } <span class="hljs-keyword">else</span> {
            j = next[j];
        }
    }

    <span class="hljs-keyword">return</span> next;
}
</code></pre>
<p>这里<code>j = next[j]</code>的写法十分巧妙，却有点难以理解(至少我一开始想了很久...)，</p>
<p>其实就是一个不断的回溯过程。<code>next[j]</code>表示<code>p[j]</code>前面的<strong>最大</strong>共通前缀后缀的<strong>长度</strong>，<span style="color: #ff0000;">【因为匹配串与被匹配串之前已经对比过无需再对比】</span></p>
<p>那么<code>p[next[j]]</code>则表示这个共通前后缀的最后一个字符。</p>
<p>如果<code>p[next[j]] == p[j]</code>则可以肯定<code>next[j+1] == next[j] + 1</code>。</p>
<p>而当<code>p[next[j]] != p[j]</code>时，就应该考虑，既然<code>next[j]</code>长度的前缀后缀都不能匹配了，那么就应该缩短这个匹配的长度。</p>
<p>直接从头开始重新匹配，那就是最朴素的暴力匹配了，效率太低。</p>
<p>此时对于<code>next[j]</code>这个字符串本身，也是有自己的最大共通前后缀的，那么<code>next[next[j]]</code>则代表<code>p[next[j]]</code>前面的<strong>最大</strong>共通前缀后缀长度。</p>
<p>所以令<code>j = next[j]</code>然后从<code>p[j]</code>重新开始比较，如果不匹配的话再重复以上过程，最终得到结果。</p>
</div>
<div class="para-title level-2">
<h2 class="title-text">优化</h2>
</div>
<div class="para">KMP算法是可以被进一步优化的。</div>
<div class="para">我们以一个例子来说明。譬如我们给的P字符串是&ldquo;abcdaabcab&rdquo;，经过KMP算法，应当得到&ldquo;<a href="https://baike.baidu.com/item/%E7%89%B9%E5%BE%81%E5%90%91%E9%87%8F" target="_blank" rel="noopener">特征向量</a>&rdquo;如下表所示：</div>
<table class="table-view log-set-param">
<tbody>
<tr>
<td>
<div class="para">下标i</div>
</td>
<td>
<div class="para">0</div>
</td>
<td>
<div class="para">1</div>
</td>
<td>
<div class="para">2</div>
</td>
<td>
<div class="para">3</div>
</td>
<td>
<div class="para">4</div>
</td>
<td>
<div class="para">5</div>
</td>
<td>
<div class="para">6</div>
</td>
<td>
<div class="para">7</div>
</td>
<td>
<div class="para">8</div>
</td>
<td>
<div class="para">9</div>
</td>
</tr>
<tr>
<td>
<div class="para">p(i)</div>
</td>
<td>
<div class="para">a</div>
</td>
<td>
<div class="para">b</div>
</td>
<td>
<div class="para">c</div>
</td>
<td>
<div class="para">d</div>
</td>
<td>
<div class="para">a</div>
</td>
<td>
<div class="para">a</div>
</td>
<td>
<div class="para">b</div>
</td>
<td>
<div class="para">c</div>
</td>
<td>
<div class="para">a</div>
</td>
<td>
<div class="para">b</div>
</td>
</tr>
<tr>
<td>
<div class="para">next[i]</div>
</td>
<td>
<div class="para">-1</div>
</td>
<td>
<div class="para">0</div>
</td>
<td>
<div class="para">0</div>
</td>
<td>
<div class="para">0</div>
</td>
<td>
<div class="para">0</div>
</td>
<td>
<div class="para">1</div>
</td>
<td>
<div class="para">1</div>
</td>
<td>
<div class="para">2</div>
</td>
<td>
<div class="para">3</div>
</td>
<td>
<div class="para">1</div>
</td>
</tr>
</tbody>
</table>
<div class="para">但是，如果此时发现p(i) == p(k），那么应当将相应的next[i]的值更改为next[k]的值。经过优化后可以得到下面的表格：</div>
<table class="table-view log-set-param">
<tbody>
<tr>
<td>
<div class="para">下标i</div>
</td>
<td>
<div class="para">0</div>
</td>
<td>
<div class="para">1</div>
</td>
<td>
<div class="para">2</div>
</td>
<td>
<div class="para">3</div>
</td>
<td>
<div class="para">4</div>
</td>
<td>
<div class="para">5</div>
</td>
<td>
<div class="para">6</div>
</td>
<td>
<div class="para">7</div>
</td>
<td>
<div class="para">8</div>
</td>
<td>
<div class="para">9</div>
</td>
</tr>
<tr>
<td>
<div class="para">p(i)</div>
</td>
<td>
<div class="para">a</div>
</td>
<td>
<div class="para">b</div>
</td>
<td>
<div class="para">c</div>
</td>
<td>
<div class="para">d</div>
</td>
<td>
<div class="para">a</div>
</td>
<td>
<div class="para">a</div>
</td>
<td>
<div class="para">b</div>
</td>
<td>
<div class="para">c</div>
</td>
<td>
<div class="para">a</div>
</td>
<td>
<div class="para">b</div>
</td>
</tr>
<tr>
<td>
<div class="para">next[i]</div>
</td>
<td>
<div class="para">-1</div>
</td>
<td>
<div class="para">0</div>
</td>
<td>
<div class="para">0</div>
</td>
<td>
<div class="para">0</div>
</td>
<td>
<div class="para">0</div>
</td>
<td>
<div class="para">1</div>
</td>
<td>
<div class="para">1</div>
</td>
<td>
<div class="para">2</div>
</td>
<td>
<div class="para">3</div>
</td>
<td>
<div class="para">1</div>
</td>
</tr>
<tr>
<td>
<div class="para">优化的next[i]</div>
</td>
<td>
<div class="para">-1</div>
</td>
<td>
<div class="para">0</div>
</td>
<td>
<div class="para">0</div>
</td>
<td>
<div class="para">0</div>
</td>
<td>
<div class="para">-1</div>
</td>
<td>
<div class="para">1</div>
</td>
<td>
<div class="para">0</div>
</td>
<td>
<div class="para">0</div>
</td>
<td>
<div class="para">3</div>
</td>
<td>
<div class="para">0</div>
</td>
</tr>
</tbody>
</table>
<div class="para">（1）next[0]= -1 意义：任何串的第一个字符的模式值规定为-1。</div>
<div class="para">（2）next[j]= -1 意义：模式串T中下标为j的字符，如果与首字符相同，且j的前面的1&mdash;k个字符与开头的1&mdash;k个字符不等（或者相等但T[k]==T[j]）（1&le;k&lt;j），如：T=&rdquo;abCabCad&rdquo; 则 next[6]=-1，因T[3]=T[6].</div>
<div class="para">（3）next[j]=k 意义：模式串T中下标为j的字符，如果j的前面k个字符与开头的k个字符相等，且T[j] != T[k] （1&le;k&lt;j）即T[0]T[1]T[2]......T[k-1]==T[j-k]T[j-k+1]T[j-k+2]&hellip;T[j-1]且T[j] != T[k].（1&le;k&lt;j）;</div>
<div class="para">(4) next[j]=0 意义：除（1）（2）（3）的其他情况。</div>
<div class="para">补充一个next[]生成代码：</div>
<div>
<div id="highlighter_61448" class="syntaxhighlighter  cpp">
<table border="0" cellspacing="0" cellpadding="0">
<tbody>
<tr>
<td class="gutter">&nbsp;</td>
<td class="code">
<div class="container">
<div class="line number1 index0 alt2"><code class="cpp keyword bold">void</code>&nbsp;<code class="cpp plain">getNext(</code><code class="cpp keyword bold">const</code>&nbsp;<code class="cpp color1 bold">char</code><code class="cpp plain">*pattern,</code><code class="cpp color1 bold">int</code>&nbsp;<code class="cpp plain">next[]){</code></div>
<div class="line number2 index1 alt1">&nbsp;</div>
<div class="line number3 index2 alt2"><code class="cpp spaces">&nbsp;&nbsp;</code><code class="cpp plain">next[0]=-1;</code></div>
<div class="line number4 index3 alt1"><code class="cpp spaces">&nbsp;&nbsp;&nbsp;&nbsp;</code>&nbsp;</div>
<div class="line number5 index4 alt2"><code class="cpp spaces">&nbsp;&nbsp;&nbsp;&nbsp;</code><code class="cpp color1 bold">int</code>&nbsp;<code class="cpp plain">k=-1,j=0;</code></div>
<div class="line number6 index5 alt1"><code class="cpp spaces">&nbsp;&nbsp;&nbsp;&nbsp;</code>&nbsp;</div>
<div class="line number7 index6 alt2"><code class="cpp spaces">&nbsp;&nbsp;&nbsp;&nbsp;</code><code class="cpp keyword bold">while</code><code class="cpp plain">(pattern[j]!=</code><code class="cpp string">'\0'</code><code class="cpp plain">){</code></div>
<div class="line number8 index7 alt1"><code class="cpp spaces">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</code>&nbsp;</div>
<div class="line number9 index8 alt2"><code class="cpp spaces">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</code><code class="cpp keyword bold">while</code>&nbsp;<code class="cpp plain">(k!=-1&nbsp;&amp;&amp;&nbsp;pattern[k]!=pattern[j])&nbsp;k=next[k];</code></div>
<div class="line number10 index9 alt1"><code class="cpp spaces">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</code>&nbsp;</div>
<div class="line number11 index10 alt2"><code class="cpp spaces">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</code><code class="cpp plain">++j;</code></div>
<div class="line number12 index11 alt1"><code class="cpp spaces">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</code>&nbsp;</div>
<div class="line number13 index12 alt2"><code class="cpp spaces">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</code><code class="cpp plain">++k;</code></div>
<div class="line number14 index13 alt1"><code class="cpp spaces">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</code>&nbsp;</div>
<div class="line number15 index14 alt2"><code class="cpp spaces">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</code><code class="cpp keyword bold">if</code><code class="cpp plain">(pattern[k]==pattern[j])&nbsp;next[j]=next[k];</code></div>
<div class="line number16 index15 alt1"><code class="cpp spaces">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</code>&nbsp;</div>
<div class="line number17 index16 alt2"><code class="cpp spaces">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</code><code class="cpp keyword bold">else</code>&nbsp;<code class="cpp plain">next[j]=k;</code></div>
<div class="line number18 index17 alt1"><code class="cpp spaces">&nbsp;&nbsp;&nbsp;&nbsp;</code>&nbsp;</div>
<div class="line number19 index18 alt2"><code class="cpp spaces">&nbsp;&nbsp;&nbsp;&nbsp;</code><code class="cpp plain">}</code></div>
<div class="line number20 index19 alt1"><code class="cpp spaces">&nbsp;&nbsp;&nbsp;</code>&nbsp;</div>
<div class="line number21 index20 alt2"><code class="cpp plain">}</code></div>
</div>
</td>
</tr>
</tbody>
</table>
</div>
</div>
<div class="show-content-free">
<p>&nbsp;</p>
</div>
</div>