# [SHA1算法分析及实现](https://www.cnblogs.com/foxclever/p/8282366.html)

SHA算法，即安全散列算法（Secure Hash Algorithm）是一种与MD5同源的数据加密算法，该算法经过加密专家多年来的发展和改进已日益完善，现在已成为公认的最安全的散列算法之一，并被广泛使用。

**1****、概述**

SHA算法能计算出一个数位信息所对应到的，长度固定的字串，又称信息摘要。而且如果输入信息有任何的不同，输出的对应摘要不同的机率非常高。因此SHA算法也是FIPS所认证的五种安全杂凑算法之一。原因有两点：一是由信息摘要反推原输入信息，从计算理论上来说是极为困难的；二是，想要找到两组不同的输入信息发生信息摘要碰撞的几率，从计算理论上来说是非常小的。任何对输入信息的变动，都有很高的几率导致的信息摘要大相径庭。

SHA实际上是一系列算法的统称，分别包括：SHA-1、SHA-224、SHA-256、SHA-384以及SHA-512。后面4中统称为SHA-2，事实上SHA-224是SHA-256的缩减版，SHA-384是SHA-512的缩减版。各中SHA算法的数据比较如下表，其中的长度单位均为位：
**类别**
 
**SHA-1**
 
**SHA-224**
 
**SHA-256**
 
**SHA-384**
 
**SHA-512** 消息摘要长度
 
160
 
224
 
256
 
384
 
512 消息长度
 
小于264位
 
小于264位
 
小于264位
 
小于2128位
 
小于2128位 分组长度
 
512
 
512
 
512
 
1024
 
1024 计算字长度
 
32
 
32
 
32
 
64
 
64 计算步骤数
 
80
 
64
 
64
 
80
 
80

SHA-1在许多安全协定中广为使用，包括TLS和SSL、PGP、SSH、S/MIME和IPsec，曾被视为是MD5的后继者。SHA1主要适用于数字签名标准（Digital Signature Standard DSS）里面定义的数字签名算法（Digital Signature Algorithm DSA）。对于长度小于264位的消息，SHA1会产生一个160位的消息摘要。

**2****、基本原理**

前面我们简单的介绍了SHA算法族，接下来我们以SHA-1为例来分析其基本原理。SHA-1是一种数据加密算法，该算法的思想是接收一段明文，然后以一种不可逆的方式将它转换成一段密文，也可以简单的理解为输入一串二进制码，并把它们转化为长度较短、位数固定的输出序列即散列值，也称为信息摘要或信息认证代码的过程。

SHA-1算法输入报文的最大长度不超过264位，产生的输出是一个160位的报文摘要。输入是按512 位的分组进行处理的。SHA-1是不可逆的、防冲突，并具有良好的雪崩效应。

一般来说SHA-1算法包括有如下的处理过程：

**（****1****）、对输入信息进行处理**

既然SHA-1算法是对给定的信息进行处理得到相应的摘要，那么首先需要按算法的要求对信息进行处理。那么如何处理呢？对输入的信息按512位进行分组并进行填充。如何填充信息报文呢？其实即使填充报文后使其按512进行分组后，最后正好余448位。那填充什么内容呢？就是先在报文后面加一个1，再加很多个0，直到长度满足对512取模结果为448。到这里可能有人会奇怪，为什么非得是448呢？这是因为在最后会附加上一个64位的报文长度信息，而448+64正好是512。

**（****2****）、填充长度信息**

前面已经说过了，最后会补充信息报文使其按512位分组后余448位，剩下的64位就是用来填写报文的长度信息的。至次可能大家也明白了前面说过的报文长度不能超过264位了。填充长度值时要注意必须是低位字节优先。

**（****3****）信息分组处理**

经过添加位数处理的明文，其长度正好为512位的整数倍，然后按512位的长度进行分组，可以得到一定数量的明文分组，我们用Y0，Y1，……YN-1表示这些明文分组。对于每一个明文分组，都要重复反复的处理，这些与MD5都是相同的。

而对于每个512位的明文分组，SHA1将其再分成16份更小的明文分组，称为子明文分组，每个子明文分组为32位，我们且使用M[t]（t= 0, 1,……15）来表示这16个子明文分组。然后需要将这16个子明文分组扩充到80个子明文分组，我们将其记为W[t]（t= 0, 1,……79），扩充的具体方法是：当0≤t≤15时，Wt = Mt；当16≤t≤79时，Wt = ( Wt-3 ⊕ Wt-8⊕ Wt-14⊕ Wt-16) <<< 1，从而得到80个子明文分组。

**（****4****）初始化缓存**

所谓初始化缓存就是为链接变量赋初值。前面我们实现MD5算法时，说过由于摘要是128位，以32位为计算单位，所以需要4个链接变量。同样SHA-1采用160位的信息摘要，也以32位为计算长度，就需要5个链接变量。我们记为A、B、C、D、E。其初始赋值分别为：A = 0x67452301、B = 0xEFCDAB89、Ｃ = 0x98BADCFE、Ｄ = 0x10325476、Ｅ = 0xC3D2E1F0。

如果我们对比前面说过的MD5算法就会发现，前４个链接变量的初始值是一样的，因为它们本来就是同源的。

**（****5****）计算信息摘要**

经过前面的准备，接下来就是计算信息摘要了。SHA1有4轮运算，每一轮包括20个步骤，一共80步，最终产生160位的信息摘要，这160位的摘要存放在5个32位的链接变量中。

在SHA1的4论运算中，虽然进行的就具体操作函数不同，但逻辑过程却是一致的。首先，定义5个变量，假设为H0、H1、H2、H3、H4，对其分别进行如下操作：

（A）、将A左移5为与 函数的结果求和，再与对应的子明文分组、E以及计算常数求和后的结果赋予H0。

（B）、将A的值赋予H1。

（C）、将B左移30位，并赋予H2。

（D）、将C的值赋予H3。

（E）、将D的值赋予H4。

（F）、最后将H0、H1、H2、H3、H4的值分别赋予A、B、C、D

这一过程表示如下：

![](https://images2017.cnblogs.com/blog/564295/201801/564295-20180114092148551-789577304.png)

而在4轮80步的计算中使用到的函数和固定常熟如下表所示：
**计算轮次**
 
**计算的步数**
 
**计算函数**
 
**计算常数** 第一轮
 
0≤t≤19步
 
ft(B,C,D)=(B&C)|(~B&D)
 
*K*t=0x5A827999 第二轮
 
20≤t≤39步
 
ft(B,C,D)=B⊕C⊕D
 
*K*t=0x6ED9EBA1 第三轮
 
40≤t≤59步
 
ft(B,C,D)=(B&C)|(B&D)|(C&D)
 
*K*t=0x8F188CDC 第四轮
 
60≤t≤79步
 
ft(B,C,D)=B⊕C⊕D
 
*K*t=0xCA62C1D6

经过4论80步计算后得到的结果，再与各链接变量的初始值求和，就得到了我们最终的信息摘要。而对于有多个铭文分组的，则将前面所得到的结果作为初始值进行下一明文分组的计算，最终计算全部的明文分组就得到了最终的结果。

**3****、软件实现**

经过前面的分析过程，接下来要具体实现SHA1算法其实已经很简单了！下面来一步步实现它，首先实现初始化函数：
[![复制代码](http://common.cnblogs.com/images/copycode.gif)]( "复制代码")
```
1 //* SHA1Reset函数用于初始化SHA1的内容值 /*/ 2 //* 参数：context，SHA的内容值，存储计算结果既初始值，输入输出 /*/ 3 //* 返回值：SHA错误代码 /*/ 4 ErrorCode SHA1Reset(SHA1Context /*context) 5 { 6 if (!context) 7 { 8 return shaNull; 9 } 10 11 context->Length_Low = 0; 12 context->Length_High = 0; 13 context->Message_Block_Index = 0; 14 15 context->Intermediate_Hash[0] = 0x67452301; 16 context->Intermediate_Hash[1] = 0xEFCDAB89; 17 context->Intermediate_Hash[2] = 0x98BADCFE; 18 context->Intermediate_Hash[3] = 0x10325476; 19 context->Intermediate_Hash[4] = 0xC3D2E1F0; 20 21 context->Computed = 0; 22 context->Corrupted = 0; 23 24 return shaSuccess; 25 }
```
[![复制代码](http://common.cnblogs.com/images/copycode.gif)]( "复制代码")

接下来实现明文信息的读取及处理函数，该函数读取信息分组，并输入前次计算的结果，对除最后一组信息外的全部分组进行信息摘要计算。

[![复制代码](http://common.cnblogs.com/images/copycode.gif)]( "复制代码")
```
1 //* SHA1Input函数，将分组的信息读入并进行摘要计算 /*/ 2 //* 参数： /*/ 3 //* context，SHA的内容值，存储计算结果既初始值，输入输出 /*/ 4 //* message_array，待处理的信息分组的字节数组，输入参数 /*/ 5 //* length，message_array数组中信息的长度 /*/ 6 //* 返回值：SHA错误代码 /*/ 7 ErrorCode SHA1Input(SHA1Context /*context,const uint8_t /*message_array,unsigned length) 8 { 9 if (!length) 10 { 11 return shaSuccess; 12 } 13 14 if (!context || !message_array) 15 { 16 return shaNull; 17 } 18 19 if (context->Computed) 20 { 21 context->Corrupted = shaStateError; 22 23 return shaStateError; 24 } 25 26 if (context->Corrupted) 27 { 28 return (ErrorCode)context->Corrupted; 29 } 30 while(length-- && !context->Corrupted) 31 { 32 context->Message_Block[context->Message_Block_Index++] = 33 34 (/*message_array & 0xFF); 35 36 context->Length_Low += 8; 37 if (context->Length_Low == 0) 38 { 39 context->Length_High++; 40 if (context->Length_High == 0) 41 { 42 //* 消息长度超过限值 /*/ 43 context->Corrupted = 1; 44 } 45 } 46 47 if (context->Message_Block_Index == 64) 48 { 49 SHA1ProcessMessageBlock(context); 50 } 51 52 message_array++; 53 } 54 55 return shaSuccess; 56 }
```
[![复制代码](http://common.cnblogs.com/images/copycode.gif)]( "复制代码")

然后实现结果输出函数，该函数对信息的最后部分进行处理并返回160位的信息摘要到Message_Digest数组，该数组作为参数有调用者输入。第一个元素存第一个字节，依次20个字节。

[![复制代码](http://common.cnblogs.com/images/copycode.gif)]( "复制代码")
```
1 //* SHA1Result函数，对信息的最后部分进行处理并输出最终计算结果 /*/ 2 //* 参数： /*/ 3 //* context，SHA的内容值，存储计算结果既初始值，输入输出 /*/ 4 //* Message_Digest，信息摘要的返回值，输出参数 /*/ 5 //* 返回值：SHA错误代码 /*/ 6 ErrorCode SHA1Result( SHA1Context /*context,uint8_t Message_Digest[SHA1HashSize]) 7 { 8 int i; 9 10 if (!context || !Message_Digest) 11 { 12 return shaNull; 13 } 14 15 if (context->Corrupted) 16 { 17 return (ErrorCode)context->Corrupted; 18 } 19 20 if (!context->Computed) 21 { 22 SHA1PadMessage(context); 23 for(i=0; i<64; ++i) 24 { 25 //* 处理完毕，清除消息分组 /*/ 26 context->Message_Block[i] = 0; 27 } 28 context->Length_Low = 0; //* 清除长度数据 /*/ 29 context->Length_High = 0; 30 context->Computed = 1; 31 } 32 33 for(i = 0; i < SHA1HashSize; ++i) 34 { 35 Message_Digest[i] = context->Intermediate_Hash[i>>2]>>8/*(3-(i&0x03)); 36 } 37 38 return shaSuccess; 39 }
```
[![复制代码](http://common.cnblogs.com/images/copycode.gif)]( "复制代码")

还需要实现消息分组的处理函数。该函数处理存储于Message_Block数组中的，待处理的512位的明文分组，将其处理为80个子明文分组，并进行4轮80步运算，返回相应的摘要值。

[![复制代码](http://common.cnblogs.com/images/copycode.gif)]( "复制代码")
```
1 //* SHA1ProcessMessageBlock函数，处理消息分组 /*/ 2 //* 描述： /*/ 3 //* 参数： /*/ 4 //* context，SHA的内容值，存储计算结果既初始值，输入输出 /*/ 5 //* 返回值：无 /*/ 6 static void SHA1ProcessMessageBlock(SHA1Context /*context) 7 { 8 //* SHA-1计算中用到的常数定义 /*/ 9 const uint32_t K[]={0x5A827999,0x6ED9EBA1,0x8F1BBCDC,0xCA62C1D6}; 10 11 int t; //* 循环变量 /*/ 12 uint32_t temp; //* 临时变量，存计算值 /*/ 13 uint32_t W[80]; //* 子明文分组数组 /*/ 14 uint32_t A, B, C, D, E; //* 初始值缓存变量 /*/ 15 16 //* 初始化子明文分组W的前16个字 /*/ 17 for(t = 0; t < 16; t++) 18 { 19 W[t] = context->Message_Block[t /* 4] << 24; 20 W[t] |= context->Message_Block[t /* 4 + 1] << 16; 21 W[t] |= context->Message_Block[t /* 4 + 2] << 8; 22 W[t] |= context->Message_Block[t /* 4 + 3]; 23 } 24 25 for(t = 16; t < 80; t++) 26 { 27 W[t] = SHA1CircularShift(1,W[t-3] ^ W[t-8] ^ W[t-14] ^ W[t-16]); 28 } 29 30 A = context->Intermediate_Hash[0]; 31 B = context->Intermediate_Hash[1]; 32 C = context->Intermediate_Hash[2]; 33 D = context->Intermediate_Hash[3]; 34 E = context->Intermediate_Hash[4]; 35 36 //*第1轮20步计算/*/ 37 for(t = 0; t < 20; t++) 38 { 39 temp = SHA1CircularShift(5,A)+((B & C) | ((~B) & D)) + E + W[t] + K[0]; 40 E = D; 41 D = C; 42 C = SHA1CircularShift(30,B); 43 B = A; 44 A = temp; 45 } 46 47 //*第2轮20步计算/*/ 48 for(t = 20; t < 40; t++) 49 { 50 temp = SHA1CircularShift(5,A) + (B ^ C ^ D) + E + W[t] + K[1]; 51 E = D; 52 D = C; 53 C = SHA1CircularShift(30,B); 54 B = A; 55 A = temp; 56 } 57 58 //*第3轮20步计算/*/ 59 for(t = 40; t < 60; t++) 60 { 61 temp = SHA1CircularShift(5,A)+((B & C) | (B & D) | (C & D)) + E + W[t] + K[2]; 62 E = D; 63 D = C; 64 C = SHA1CircularShift(30,B); 65 B = A; 66 A = temp; 67 } 68 69 //*第4轮20步计算/*/ 70 for(t = 60; t < 80; t++) 71 { 72 temp = SHA1CircularShift(5,A) + (B ^ C ^ D) + E + W[t] + K[3]; 73 E = D; 74 D = C; 75 C = SHA1CircularShift(30,B); 76 B = A; 77 A = temp; 78 } 79 80 context->Intermediate_Hash[0] += A; 81 context->Intermediate_Hash[1] += B; 82 context->Intermediate_Hash[2] += C; 83 context->Intermediate_Hash[3] += D; 84 context->Intermediate_Hash[4] += E; 85 86 context->Message_Block_Index = 0; 87 }
```
[![复制代码](http://common.cnblogs.com/images/copycode.gif)]( "复制代码")

还需要实现一个对消息进行补位和追加消息长度并进行处理的函数。根据标准，消息必须被填充到一个剩至512位。第一个填充位必须是'1'。最后64位表示原始消息的长度。中间的所有位都应该是0。该函数将根据这些规则填充消息，并相应地填充Message_Block数组。

[![复制代码](http://common.cnblogs.com/images/copycode.gif)]( "复制代码")
```
1 //* SHA1PadMessage函数，补全消息，并添加长度，计算最终结果 /*/ 2 //* 参数： /*/ 3 //* context，SHA的内容值，存储计算结果既初始值，输入输出 /*/ 4 //* 返回值：无 /*/ 5 static void SHA1PadMessage(SHA1Context /*context) 6 { 7 //* 检查当前的消息分组，如果小于等于55个字节，则直接添加补位和长度信息。否则，如果大于55个字节，我们填充块到512位，并处理它，然后继续填充到第二个块中直道448位，最后填写长度信息。/*/ 8 if (context->Message_Block_Index > 55) 9 { 10 context->Message_Block[context->Message_Block_Index++] = 0x80; 11 while(context->Message_Block_Index < 64) 12 { 13 context->Message_Block[context->Message_Block_Index++] = 0; 14 } 15 16 SHA1ProcessMessageBlock(context); 17 18 while(context->Message_Block_Index < 56) 19 { 20 context->Message_Block[context->Message_Block_Index++] = 0; 21 } 22 } 23 else 24 { 25 context->Message_Block[context->Message_Block_Index++] = 0x80; 26 while(context->Message_Block_Index < 56) 27 { 28 context->Message_Block[context->Message_Block_Index++] = 0; 29 } 30 } 31 32 //* 将明文长度填入到最后8个字节中 /*/ 33 context->Message_Block[56] = context->Length_High >> 24; 34 context->Message_Block[57] = context->Length_High >> 16; 35 context->Message_Block[58] = context->Length_High >> 8; 36 context->Message_Block[59] = context->Length_High; 37 context->Message_Block[60] = context->Length_Low >> 24; 38 context->Message_Block[61] = context->Length_Low >> 16; 39 context->Message_Block[62] = context->Length_Low >> 8; 40 context->Message_Block[63] = context->Length_Low; 41 42 SHA1ProcessMessageBlock(context); 43 }
```
[![复制代码](http://common.cnblogs.com/images/copycode.gif)]( "复制代码")

至此SHA1散列算法就全部实现完了，需要说明一下的是相应的结构体定义和错误代码的定义如下：

[![复制代码](http://common.cnblogs.com/images/copycode.gif)]( "复制代码")
```
1 //* 定义SHA-1内容保存结构体 /*/ 2 typedef struct SHA1Context 3 { 4 uint32_t Intermediate_Hash[SHA1HashSize/4]; //* 信息摘要 /*/ 5 6 uint32_t Length_Low; //* 按位计算的信息长度低字 /*/ 7 uint32_t Length_High; //* 按位计算的信息长度高字 /*/ 8 9 int_least16_t Message_Block_Index; //* 信息分组数组的索引 /*/ 10 uint8_t Message_Block[64]; //* 512位信息分组 /*/ 11 12 int Computed; //* 摘要计算标识 /*/ 13 int Corrupted; //* 信息摘要损坏标识 /*/ 14 } SHA1Context; 15 16 typedef enum 17 { 18 shaSuccess = 0, //* 处理成功 /*/ 19 shaNull, //* 指针参数为NUll /*/ 20 shaInputTooLong, //* 输入消息长度超范围 /*/ 21 shaStateError //* 在处理完毕后，未经初始化直接调用输入处理 /*/ 22 }ErrorCode;
```
[![复制代码](http://common.cnblogs.com/images/copycode.gif)]( "复制代码")

**4****、总结**

我们已经实现了SHA1这一散列算法，接下来我们验证一下它的效果如何。首先我们输入信息“abcdef”，计算结果，并使用通用工具验算。

![](https://images2017.cnblogs.com/blog/564295/201801/564295-20180114100216972-964090966.png)![](https://images2017.cnblogs.com/blog/564295/201801/564295-20180114100225254-1310627574.png)

以上2图我们可以看到结果是一致的，接下来我们输入信息：“a1b23c4d5e6f7g8h9i0j”，计算结果如下：

![](https://images2017.cnblogs.com/blog/564295/201801/564295-20180114100234566-475793509.png)![](https://images2017.cnblogs.com/blog/564295/201801/564295-20180114100241847-1250484560.png)

对比上述2图的结果也是一致的。接下来我们分别测试长度448位、长度超过448位、长度超过512位的明文信息，所得的结果也是正确的，说明我们的实现没有问题。

前面我说了SHA-1与MD5是同源的散列算法，那他们究竟有何区别于联系呢？接下来我们简单的比较一下这两种算法：

（1）、因为二者均由MD4导出，SHA-1和MD5彼此很相似。相应的，他们的强度和其他特性也是相似，但还有以下几点不同：

（2）、对强行供给的安全性：最显著和最重要的区别是SHA-1摘要比MD5摘要长32 位。使用强行技术，产生任何一个报文使其摘要等于给定报摘要的难度对MD5是2^128数量级的操作，而对SHA-1则是2^160数量级的操作。这样，SHA-1对强行攻击有更大的强度。

（3）、对密码分析的安全性：由于MD5的设计，易受密码分析的攻击，SHA-1显得不易受这样的攻击。

（4）、速度：在相同的硬件上，SHA-1的运行速度比MD5慢。

欢迎关注：

![](https://images2017.cnblogs.com/blog/564295/201801/564295-20180114162427535-2098052277.jpg)

如果阅读这篇文章让您略有所得，还请点击下方的【**好文要顶**】按钮。

当然，如果您想及时了解我的博客更新，不妨点击下方的【**关注我**】按钮。

如果您希望更方便且及时的阅读相关文章，也可以扫描上方二维码关注我的微信公众号【**木南创智**】

参考：

[http://www.cnblogs.com/foxclever/p/8282366.html](http://www.cnblogs.com/foxclever/p/8282366.html)