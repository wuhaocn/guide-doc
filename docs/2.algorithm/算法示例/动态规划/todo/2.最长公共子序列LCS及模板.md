# [动态规划——最长公共子序列 LCS 及模板](https://www.cnblogs.com/wkfvawl/p/9362287.html)

**摘自 [https://www.cnblogs.com/hapjin/p/5572483.html](https://www.cnblogs.com/hapjin/p/5572483.html)**

**这位大佬写的对理解 DP 也很有帮助，我就直接摘抄过来了，代码部分来自我做过的题**

**一，问题描述**

给定两个字符串，求解这两个字符串的最长公共子序列（Longest Common Sequence）。比如字符串 1：BDCABA；字符串 2：ABCBDAB

则这两个字符串的最长公共子序列长度为 4，最长公共子序列是：BCBA

**二，算法求解**

这是一个动态规划的题目。对于可用动态规划求解的问题，一般有两个特征：① 最优子结构；② 重叠子问题

**① 最优子结构**

设 X=(x1,x2,.....xn) 和 Y={y1,y2,.....ym} 是两个序列，将 X 和 Y 的最长公共子序列记为 LCS(X,Y)

找出 LCS(X,Y)就是一个**最优化问题**。因为，我们需要找到 X 和 Y 中**最长的**那个公共子序列。而要找 X 和 Y 的 LCS，首先考虑 X 的最后一个元素和 Y 的最后一个元素。

**1）如果 xn=ym**，即 X 的最后一个元素与 Y 的最后一个元素相同，这说明该元素一定位于公共子序列中。因此，现在只需要找：LCS(Xn-1，Ym-1)

LCS(Xn-1，Ym-1)就是原问题的**一个**子问题。为什么叫子问题？因为它的规模比原问题小。（小一个元素也是小嘛....）

为什么是最优的子问题？因为我们要找的是 Xn-1 和 Ym-1 的最长公共子序列啊。。。最长的！！！换句话说，就是最优的那个。（这里的最优就是最长的意思）

**2）如果 xn != ym**，这下要麻烦一点，因为它产生了**两个**子问题：LCS(Xn-1，Ym) 和 LCS(Xn，Ym-1)

因为序列 X 和 序列 Y 的最后一个元素不相等嘛，那说明最后一个元素不可能是最长**公共**子序列中的元素嘛。（都不相等了，怎么公共嘛）。

LCS(Xn-1，Ym)表示：最长公共序列可以在(x1,x2,....x(n-1)) 和 (y1,y2,...yn)中找。

LCS(Xn，Ym-1)表示：最长公共序列可以在(x1,x2,....xn) 和 (y1,y2,...y(n-1))中找。

求解上面两个子问题，得到的公共子序列谁最长，那谁就是 LCS（X,Y）。用数学表示就是：

LCS=max{LCS(Xn-1，Ym)，LCS(Xn，Ym-1)}

由于条件 1) 和 2) 考虑到了所有可能的情况。因此，**我们成功地把原问题 转化 成了 三个规模更小的子问题。**

**② 重叠子问题**

重叠子问题是啥？就是说原问题 转化 成子问题后， 子问题中有相同的问题。咦？我怎么没有发现上面的三个子问题中有相同的啊？？？？

OK，来看看，原问题是：LCS(X,Y)。子问题有 ❶LCS(Xn-1，Ym-1) ❷LCS(Xn-1，Ym) ❸LCS(Xn，Ym-1)

初一看，这三个子问题是不重叠的。可本质上它们是重叠的，因为它们只重叠了一大部分。举例：

第二个子问题：LCS(Xn-1，Ym) 就包含了：问题 ❶LCS(Xn-1，Ym-1)，为什么？

因为，当 Xn-1 和 Ym 的最后一个元素不相同时，我们又需要将 LCS(Xn-1，Ym)进行分解：分解成：LCS(Xn-1，Ym-1) 和 LCS(Xn-2，Ym)

也就是说：在子问题的**继续**分解中，有些问题是重叠的。

由于像 LCS 这样的问题，它具有重叠子问题的性质，因此：用递归来求解就太不划算了。因为采用递归，它重复地求解了子问题啊。而且注意哦，所有子问题加起来的个数 可是指数级的哦。。。。

[这篇文章](http://blog.csdn.net/trochiluses/article/details/37966729)中就演示了一个递归求解重叠子问题的示例。

那么问题来了，你说用递归求解，有指数级个子问题，故时间复杂度是指数级。这指数级个子问题，难道用了动态规划，就变成多项式时间了？？

呵呵哒。。。。

关键是采用动态规划时，并不需要去一 一 计算那些重叠了的子问题。或者说：用了动态规划之后，有些子问题 是通过 “查表“ 直接得到的，而不是重新又计算一遍得到的。废话少说：举个例子吧！比如求 Fib 数列。关于 Fib 数列，[可参考：](http://www.cnblogs.com/hapjin/p/5571352.html)

![](https://images2015.cnblogs.com/blog/715283/201606/715283-20160611203653590-28450133.png)

求 fib(5)，分解成了两个子问题：fib(4) 和 fib(3)，求解 fib(4) 和 fib(3)时，又分解了一系列的小问题....

从图中可以看出：根的左右子树：fib(4) 和 fib(3)下，是有很多重叠的！！！比如，对于 fib(2)，它就一共出现了三次。**如果用递归来求解，fib(2)就会被计算三次，而用 DP(Dynamic Programming)动态规划，则 fib(2)只会计算一次，其他两次则是通过”查表“直接求得。而且，更关键的是：查找求得该问题的解之后，就不需要再继续去分解该问题了。而对于递归，是不断地将问题分解，直到分解为 基准问题(fib(1) 或者 fib(0))
**

说了这么多，还是要写下最长公共子序列的递归式才完整。借用网友的一张图吧：）

![](https://pic002.cnblogs.com/images/2012/214741/2012111100085930.png)

**c[i,j]表示：(x1,x2....xi) 和 (y1,y2...yj) 的最长公共子序列的长度。**（是长度哦，就是一个整数嘛）。公式的具体解释可参考《算法导论》动态规划章节

这张 DP 表很是重要，从中我们可以窥见最长公共子序列的来源，同时可以根据这张表打印出最长公共子序列的构成路径

![](https://images2018.cnblogs.com/blog/1358881/201807/1358881-20180724195351829-1792192564.png)

**最长公共子序列模板：**
[![复制代码](https://common.cnblogs.com/images/copycode.gif)]("复制代码")

```
1 /#include<cstdio> 2 /#include<cstring> 3 /#include<algorithm> 4 using namespace std; 5 const int N = 1000; 6 char a[N],b[N]; 7 int dp[N][N]; 8 int main() 9 { 10 int lena,lenb,i,j; 11 while(scanf("%s%s",a,b)!=EOF) 12 { 13 memset(dp,0,sizeof(dp)); 14 lena=strlen(a); 15 lenb=strlen(b); 16 for(i=1;i<=lena;i++) 17 { 18 for(j=1;j<=lenb;j++) 19 { 20 if(a[i-1]==b[j-1]) 21 { 22 dp[i][j]=dp[i-1][j-1]+1; 23 } 24 else 25 { 26 dp[i][j]=max(dp[i-1][j],dp[i][j-1]); 27 } 28 } 29 } 30 printf("%d\n",dp[lena][lenb]); 31 } 32 return 0; 33 }
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)]("复制代码")

**最长公共子序列打印路径的模板**

**递归法：**
[![复制代码](https://common.cnblogs.com/images/copycode.gif)]("复制代码")

```
1 /#include<cstdio> 2 /#include<cstring> 3 /#include<algorithm> 4 using namespace std; 5 const int N = 1010; 6 char a[N],b[N]; 7 int dp[N][N]; 8 int flag[N][N]; 9 void Print(int i,int j) 10 { 11 if(i==0||j==0)///递归终止条件 12 { 13 return ; 14 } 15 if(!flag[i][j]) 16 { 17 Print(i-1,j-1); 18 printf("%c",a[i-1]); 19 } 20 else if(flag[i][j]==1) 21 { 22 Print(i-1,j); 23 } 24 else if(flag[i][j]=-1) 25 { 26 Print(i,j-1); 27 } 28 } 29 int main() 30 { 31 int lena,lenb,i,j; 32 while(scanf("%s%s",a,b)!=EOF) 33 { 34 memset(dp,0,sizeof(dp)); 35 memset(flag,0,sizeof(flag)); 36 lena=strlen(a); 37 lenb=strlen(b); 38 for(i=1;i<=lena;i++) 39 { 40 for(j=1;j<=lenb;j++) 41 { 42 if(a[i-1]==b[j-1]) 43 { 44 dp[i][j]=dp[i-1][j-1]+1; 45 flag[i][j]=0;///来自于左上方 46 } 47 else 48 { 49 if(dp[i-1][j]>dp[i][j-1]) 50 { 51 dp[i][j]=dp[i-1][j]; 52 flag[i][j]=1;///来自于左方 53 } 54 else 55 { 56 dp[i][j]=dp[i][j-1]; 57 flag[i][j]=-1;///来自于上方 58 } 59 } 60 } 61 } 62 Print(lena,lenb); 63 } 64 return 0; 65 }
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)]("复制代码")

**非递归，在这里因为是逆序的回溯，所以我使用了栈来存储路径**
[![复制代码](https://common.cnblogs.com/images/copycode.gif)]("复制代码")

```
1 /#include<stdio.h> 2 /#include<string.h> 3 /#include<stack> 4 /#include<algorithm> 5 using namespace std; 6 /#define N 1010 7 int dp[N][N]; 8 char c; 9 int main() 10 { 11 char a[N]; 12 char b[N]; 13 scanf("%s%s",a,b); 14 int la=strlen(a); 15 int lb=strlen(b); 16 memset(dp,0,sizeof(dp)); 17 for(int i=1; i<=la; i++) 18 { 19 for(int j=1; j<=lb; j++) 20 { 21 if(a[i-1]==b[j-1]) 22 dp[i][j]=dp[i-1][j-1]+1; 23 else 24 dp[i][j]=max(dp[i-1][j],dp[i][j-1]); 25 } 26 } 27 int i=la,j=lb; 28 stack<char>s; 29 while(dp[i][j]) 30 { 31 if(dp[i][j]==dp[i-1][j])///来自于左方向 32 { 33 i--; 34 } 35 else if(dp[i][j]==dp[i][j-1])///来自于上方向 36 { 37 j--; 38 } 39 else if(dp[i][j]>dp[i-1][j-1])///来自于左上方向 40 { 41 i--; 42 j--; 43 s.push(a[i]); 44 } 45 } 46 while(!s.empty()) 47 { 48 c=s.top(); 49 printf("%c",c); 50 s.pop(); 51 } 52 return 0; 53 }
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)]("复制代码")
