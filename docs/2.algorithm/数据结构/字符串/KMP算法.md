# KMP 算法 理解与实现

参考：

[https://www.jianshu.com/p/d4cf13b32111](https://www.jianshu.com/p/d4cf13b32111)

文中对 KMP 算法的匹配过程以及“部分匹配表”具体代表什么，都解释的十分简洁明了，看过之后也算是对 KMP 算法有了一个直观的了解。

下面我想就算法的具体实现，尤其是“部分匹配表”的生成（个人认为 KMP 算法实现中，最不容易理解的部分），进行一些分析。

### KMP 算法具体实现

KMP 算法的主体是，在失去匹配时，查询最后一个匹配字符所对应的“部分匹配表“中的值，然后向前移动，移动位数为：

移动位数 = 已匹配的字符数 - 对应的部分匹配值

比如对如下的匹配：
![](https://upload-images.jianshu.io/upload_images/679154-4b19618459179ecc.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/577/format/webp)

在**D**处失去匹配，那么查询最后一个匹配字符**B**在部分匹配表中的值为**2**。
则向前移动 6-2=**4**位。（其实就相当于从搜索词的第二位开始重新进行比较）

![](https://upload-images.jianshu.io/upload_images/679154-0cb36dfe404013ab.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/645/format/webp)

下面是算法主体的实现
这里的

next
数组即“部分匹配表”。注意因为搜索词最后一位对应的部分匹配值是没有意义的，所以为了编程方便，

我们将”部分匹配表“整体向后移一位，并把第一位设为-1。

```
//Java //*/* /* KMP算法.<br/> /* 在目标字符串中对搜索词进行搜索。<br/> /* /* @param t 目标字符串 /* @param p 搜搜词 /* @return 搜索词第一次匹配到的起始位置或-1 /*/ public int KMP(String t, String p){ char[] target = t.toCharArray(); char[] pattern = p.toCharArray(); // 目标字符串下标 int i = 0; // 搜索词下标 int j = 0; // 整体右移一位的部分匹配表 int[] next = getNext(pattern); while (i < target.length && j < patter.length) { // j == -1 表示从搜索词最开始进行匹配 if (j == -1 || target[i] == pattern[j]) { i++; j++; // 匹配失败时，查询“部分匹配表”，得到搜索词位置j以前的最大共同前后缀长度 // 将j移动到最大共同前后缀长度的后一位，然后再继续进行匹配 } else { j = next[j]; } } // 搜索词每一位都能匹配成功，返回匹配的的起始位置 if (j == pattern.length) return i - j; else return -1; }
```

KMP 算法的搜索过程还是比较好理解的。
接下来最容易被绕进去的部分来了，求解“部分匹配表”即

next
数组。

### 部分匹配表(next 数组)的生成

其实，求 next 数组的过程完全可以看成字符串匹配的过程，即以搜索词为主字符串，以搜索词的**前缀**为目标字符串，

一旦字符串匹配成功，那么当前的 next 值就是匹配成功的字符串的长度。
具体来说，就是从模式字符串的第一位(**注意，不包括第 0 位**)开始对自身进行匹配运算。 在任一位置，能匹配的最长长度就是当前位置的 next 值，如下图所示。
(这里 next 数组下标从 1 开始表示)
![](https://upload-images.jianshu.io/upload_images/679154-55e3ce1012c52c3b.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/720/format/webp)

![](https://upload-images.jianshu.io/upload_images/679154-d17ed028ce5a4099.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/720/format/webp)
![](https://upload-images.jianshu.io/upload_images/679154-b1c1d019d99536b7.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/720/format/webp)

![](https://upload-images.jianshu.io/upload_images/679154-057c0e1b20deb543.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/720/format/webp)
![](https://upload-images.jianshu.io/upload_images/679154-537d729e32a1e62f.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/720/format/webp)

下面是算法的具体实现

```
//Java //*/* /* 生成部分匹配表.<br/> /* 生成搜索词的部分匹配表<br/> /* /* @param p 搜搜词 /* @return 部分匹配表 /*/ private int[] getNext(String pattern) { char[] p = pattern.toCharArray(); int[] next = new int[p.length]; // 第一位设为-1，方便判断当前位置是否为搜索词的最开始 next[0] = -1; int i = 0; int j = -1; while(i < p.length - 1) { if (j == -1 || p[i] == p[j]) { i++; j++; next[i] = j; } else { j = next[j]; } } return next; }
```

这里

j = next[j]
的写法十分巧妙，却有点难以理解(至少我一开始想了很久...)，

其实就是一个不断的回溯过程。

next[j]
表示

p[j]
前面的**最大**共通前缀后缀的**长度**，【因为匹配串与被匹配串之前已经对比过无需再对比】

那么

p[next[j]]
则表示这个共通前后缀的最后一个字符。

如果

p[next[j]] == p[j]
则可以肯定

next[j+1] == next[j] + 1
。

而当

p[next[j]] != p[j]
时，就应该考虑，既然

next[j]
长度的前缀后缀都不能匹配了，那么就应该缩短这个匹配的长度。

直接从头开始重新匹配，那就是最朴素的暴力匹配了，效率太低。

此时对于

next[j]
这个字符串本身，也是有自己的最大共通前后缀的，那么

next[next[j]]
则代表

p[next[j]]
前面的**最大**共通前缀后缀长度。

所以令

j = next[j]
然后从

p[j]
重新开始比较，如果不匹配的话再重复以上过程，最终得到结果。

## 优化

KMP 算法是可以被进一步优化的。

我们以一个例子来说明。譬如我们给的 P 字符串是“abcdaabcab”，经过 KMP 算法，应当得到“[特征向量](https://baike.baidu.com/item/%E7%89%B9%E5%BE%81%E5%90%91%E9%87%8F)”如下表所示：
下标 i 0 1 2 3 4 5 6 7 8 9 p(i) a b c d a a b c a b next[i] -1 0 0 0 0 1 1 2 3 1

但是，如果此时发现 p(i) == p(k），那么应当将相应的 next[i]的值更改为 next[k]的值。经过优化后可以得到下面的表格：
下标 i 0 1 2 3 4 5 6 7 8 9 p(i) a b c d a a b c a b next[i] -1 0 0 0 0 1 1 2 3 1 优化的 next[i] -1 0 0 0 -1 1 0 0 3 0

（1）next[0]= -1 意义：任何串的第一个字符的模式值规定为-1。
（2）next[j]= -1 意义：模式串 T 中下标为 j 的字符，如果与首字符相同，且 j 的前面的 1—k 个字符与开头的 1—k 个字符不等（或者相等但 T[k]==T[j]）（1≤k<j），如：T=”abCabCad” 则 next[6]=-1，因 T[3]=T[6].

（3）next[j]=k 意义：模式串 T 中下标为 j 的字符，如果 j 的前面 k 个字符与开头的 k 个字符相等，且 T[j] != T[k] （1≤k<j）即 T[0]T[1]T[2]......T[k-1]==T[j-k]T[j-k+1]T[j-k+2]…T[j-1]且 T[j] != T[k].（1≤k<j）;
(4) next[j]=0 意义：除（1）（2）（3）的其他情况。

补充一个 next[]生成代码：
void

getNext(

const

char

/\*pattern,

int

next[]){

next[0]=-1;
int

k=-1,j=0;

while

(pattern[j]!=

'\0'

){

while

(k!=-1 && pattern[k]!=pattern[j]) k=next[k];

++j;

++k;

if

(pattern[k]==pattern[j]) next[j]=next[k];

else

next[j]=k;

}

}
