# [数据结构之 平衡二叉树](https://www.cnblogs.com/zhujunxxxxx/p/3348798.html)

平衡二叉树，是一种二叉排序树，其中每个结点的左子树和右子树的高度差至多等于 1。它是一种高度平衡的二叉排序树。高度平衡？意思是说，要么它是一棵空树，要么它的左子树和右子树都是平衡二叉树，且左子树和右子树的深度之差的绝对值不超过 1。

将二叉树上结点的左子树深度减去右子树深度的值称为平衡因子 BF，那么平衡二叉树上的所有结点的平衡因子只可能是-1、0 和 1。只要二叉树上有一个结点的平衡因子的绝对值大于 1，则该二叉树就是不平衡的。

平衡二叉树的前提是它是一棵二叉排序树。

距离插入结点最近的，且平衡因子的绝对值大于 1 的结点为根的子树，称为最小不平衡子树。如下图所示，当插入结点 37 时，距离它最近的平衡因子的绝对值超过 1 的结点是 58。

![](http://blog.chinaunix.net/attachment/201301/20/26548237_1358647849QBbO.png)

1、平衡二叉树实现原理

平衡二叉树构建的基本思想就是在构建二叉排序树的过程中，每当插入一个结点时，先检查是否因插入而破坏了树的平衡性，若是，则找出最小不平衡子树。在保持二叉排序树特性的前提下，调整最小不平衡子树中各结点之间的链接关系，进行相应的旋转，使之成为新的平衡子树。 下面讲解一个平衡二叉树构建过程的例子。现在又 a[10] = {3, 2, 1, 4, 5, 6, 7, 10, 9, 8}需要构建二叉排序树。在没有学习平衡二叉树之前，根据二叉排序树的特性，通常会将它构建成如下左图。虽然完全符合二叉排序树的定义，但是对这样高度达到 8 的二叉树来说，查找是非常不利的。因此，更加期望构建出如下右图的样子，高度为 4 的二叉排序树，这样才可以提供高效的查找效率。

![](http://blog.chinaunix.net/attachment/201301/20/26548237_1358648262bfLw.png)

现在来看看如何将一个数组构成出如上右图的树结构。 对于数组 a 的前两位 3 和 2，很正常地构建，到了第个数“1”时，发现此时根结点“3”的平衡因子变成了 2，此时整棵树都成了最小不平衡子树，需要进行调整，如下图图 1（结点左上角数字为平衡因子 BF 值）。因为 BF 为正，因此将整个树进行右旋（顺时针），此时结点 2 成了根结点，3 成了 2 的右孩子，这样三个结点的 BF 值均为 0，非常的平衡，如下图图 2 所示。

![](http://blog.chinaunix.net/attachment/201301/20/26548237_1358648531VpPP.png)

然后再增加结点 4，平衡因子没有改变，如上图图 3。增加结点 5 时，结点 3 的 BF 值为-2，说明要旋转了。由于 BF 是负值，对这棵最小平衡子树进行左旋（逆时针旋转），如下图图 4，此时整个树又达到了平衡。

![](http://blog.chinaunix.net/attachment/201301/20/26548237_1358648821ZpPK.png)

继续增加结点 6 时，发现根结点 2 的 BF 值变成了-2，如下图图 6 所示。所以对根结点进行了左旋，注意此时本来结点 3 是结点 3 的左孩子，由于旋转后需要满足二叉排序树特性，因此它成了结点 2 的右孩子，如图 7 所示。

![](http://blog.chinaunix.net/attachment/201301/20/26548237_13586496564218.png)

增加结点 7，同样的左旋转，使得整棵树达到平衡，如下图 8 和 9 所示。

![](http://blog.chinaunix.net/attachment/201301/20/26548237_1358649801UbTZ.png)

当增加结点 10 时，结构无变化，如图 10 所示。再增加结点 9，此时结点 7 的 BF 变成了-2，理论上只需要旋转最小不平衡树 7、9、10 即可，但是，如果左旋转后，结点 9 变成了 10 的右孩子，这是不符合二叉排序树的特性的，此时不能简单的左旋。如图 11 所示。

![](http://blog.chinaunix.net/attachment/201301/20/26548237_1358650084A4zx.png)

仔细观察图 11，发现根本原因在于结点 7 的 BF 是-2，而结点 10 的 BF 是 1，也就是说，它们两个一正一负，符号并不统一，而前面的几次旋转，无论左还是右旋，最小不平衡子树的根结点与它的子结点符号都是相同的。这就是不能直接旋转的关键。 不统一，不统一就把它们先转到符号统一再说，于是先对结点 9 和结点 10 进行右旋，使得结点 10 成了 9 的右子树，结点 9 的 BF 为-1，此时就与结点 7 的 BF 值符号统一了，如图 12 所示。

![](http://blog.chinaunix.net/attachment/201301/20/26548237_13586503529IMU.png)

这样再以结点 7 为最小不平衡子树进行左旋，得到如下图 13。接着，插入 8，情况与刚才类似，结点 6 的 BF 是-2，而它的右孩子 9 的 BF 是 1，如图 14，因此首先以 9 为根结点，进行右旋，得到图 15，此时结点 6 和结点 7 的符号都是负，再以 6 为根结点左旋，最终得到最后的平衡二叉树，如图 16 所示。

![](http://blog.chinaunix.net/attachment/201301/20/26548237_1358650571sdU0.png)

通过这个例子，可以发现，当最小不平衡树根结点的平衡因子 BF 是大于 1 时，就右旋，小于-1 时就左旋，如上例中的结点 1、5、6、7 的插入等。插入结点后，最小不平衡子树的 BF 与它的子树的 BF 符号相反时，就需要对结点先进行一次旋转以使得符号相同后，再反向旋转一次才能够完成平衡操作，如上例中结点 9、8 的插入时。

下面两个图讲解了插入时所要做的旋转操作的例子。《来自：http://www.cnblogs.com/guyan/archive/2012/09/03/2668399.html》

![](http://blog.chinaunix.net/attachment/201301/20/26548237_1358690724QUaT.png)

![](http://blog.chinaunix.net/attachment/201301/20/26548237_1358690668jy1n.png)

2、平衡二叉树算法的实现

首先是需要改进二叉排序树的结点结构，增加一个 bf，用来存储平衡因子。

typedef

struct

BitNode

{
int

data;

int

bf;
struct

BitNode /*lchild, /*rchild;

}BitNode, /\*BiTree; 然后对于右旋（顺时针）操作，代码如下：

void

R_rotate(BiTree /\*t)

{
BiTree s;

s = (/\*t)->lchild;

//s 指向 t 的左子树根结点
(/\*t)->lchild = s->rchild;

//s 的右子树挂接为 t 的左子树

s->rchild = (/*t);
/*p = s;

//t 指向新的根结点

} 此函数代码的意思是说，当传入一个二叉排序树 t，将它的左孩子结点定义为 s，将 s 的右子树变成 t 的左子树，再将 t 改为 s 的右子树，最后将 s 替换为 t 的根结点。这样就完成了一次右旋操作。如下图示，图中三角形代表子树，N 代表新增的结点。

![](http://blog.chinaunix.net/attachment/201301/20/26548237_1358676013pPD7.png)

上面的例子中新增加了结点 N，就是右旋操作。

左旋代码如下所示。

void

L_rotate(BiTree /\*t)

{
BiTree s;

s = (/\*t)->rchild;

//s 指向 t 的右子树根结点
(/\*t)->rchild = s->lchild;

//s 的左子树挂接为 t 的右子树

s->lchild = (/*t);
/*p = s;

//t 指向新的根结点

} 下面看左旋转平衡（使左边平衡）的处理代码。

/#define LH +1 //_ 左高 /_/

/#define EH 0 //_ 等高 /_/
/#define RH -1 //_ 右高 /_/

//_ 对以指针 T 所指结点为根的二叉树作左平衡旋转处理 /_/
//_ 本算法结束时，指针 T 指向新的根结点 /_/

void

LeftBalance(BiTree /\*T)
{

BiTree L,Lr;
L = (/\*T)->lchild;

//_ L 指向 T 的左子树根结点 /_/

switch

(L->bf)
{

//_ 检查 T 的左子树的平衡度，并作相应平衡处理 /_/
case

LH:

//_ 新结点插入在 T 的左孩子的左子树上，要作单右旋处理 /_/

(/\*T)->bf=L->bf=EH;
R_Rotate(T);

break

;
case

RH:

//_ 新结点插入在 T 的左孩子的右子树上，要作双旋处理 /_/

Lr=L->rchild;

//_ Lr 指向 T 的左孩子的右子树根 /_/
switch

(Lr->bf)

{

//_ 修改 T 及其左孩子的平衡因子 /_/
case

LH: (/\*T)->bf=RH;

L->bf=EH;
break

;

case

EH: (/\*T)->bf=L->bf=EH;
break

;

case

RH: (/\*T)->bf=EH;
L->bf=LH;

break

;
}

Lr->bf=EH;
L_Rotate(&(/\*T)->lchild);

//_ 对 T 的左子树作左旋平衡处理 /_/

R_Rotate(T);

//_ 对 T 作右旋平衡处理 /_/
}

}

首先，定义三个常数变量，分别代码 1、0、-1。

（1）函数被调用，传入一个需调整平衡型的子树 T。由于 LeftBalance 函数被调用时，其实是已经确认当前子树是不平衡的状态，且左子树的高度大于右子树的高度。换句话说，此时 T 的根结点应该是平衡因子 BF 的值大于 1 的数。

（2）将 T 的左孩子赋值给 L。

（3）然后是分支判断。

（4）当 L 的平衡因子为 LH，即为 1 时，表明它与根结点的 BF 值符号相同，因此，将它们的 BF 值都改为 0，并进行右旋（顺时针）操作，操作方式如图所示。

（5）当 L 的平衡因子为 RH 时，即为-1 时，表明它与根结点的 BF 值符号相反，此时需要做双旋操作。针对 L 的右孩子的 BF 作判断，修改结点 T 和 L 的 BF 值。将当前的 Lr 的 BF 改为 0。

（6）对根结点的左子树进行左旋，如下图第二图所示。

（7）对根结点进行右旋，如下图第三图所示，完成平衡操作。

![](http://blog.chinaunix.net/attachment/201301/20/26548237_1358677817GNjg.png)

右平衡（使右边平衡）旋转处理的函数代码如下。

//_ 对以指针 T 所指结点为根的二叉树作右平衡旋转处理， /_/

//_ 本算法结束时，指针 T 指向新的根结点 /_/
void

RightBalance(BiTree /\*T)

{
BiTree R,Rl;

R=(/\*T)->rchild;

//_ R 指向 T 的右子树根结点 /_/
switch

(R->bf)

{

//_ 检查 T 的右子树的平衡度，并作相应平衡处理 /_/
case

RH:

//_ 新结点插入在 T 的右孩子的右子树上，要作单左旋处理 /_/

(/\*T)->bf=R->bf=EH;
L_Rotate(T);

break

;
case

LH:

//_ 新结点插入在 T 的右孩子的左子树上，要作双旋处理 /_/

Rl=R->lchild;

//_ Rl 指向 T 的右孩子的左子树根 /_/
switch

(Rl->bf)

{

//_ 修改 T 及其右孩子的平衡因子 /_/
case

RH: (/\*T)->bf=LH;

R->bf=EH;
break

;

case

EH: (/\*T)->bf=R->bf=EH;
break

;

case

LH: (/\*T)->bf=EH;
R->bf=RH;

break

;
}

Rl->bf=EH;
R_Rotate(&(/\*T)->rchild);

//_ 对 T 的右子树作右旋平衡处理 /_/

L_Rotate(T);

//_ 对 T 作左旋平衡处理 /_/
}

}

插入数据操作如下所示。

//_ 若在平衡的二叉排序树 T 中不存在和 e 有相同关键字的结点，则插入一个 /_/

//_ 数据元素为 e 的新结点，并返回 1，否则返回 0。若因插入而使二叉排序树 /_/
//_ 失去平衡，则作平衡旋转处理，布尔变量 taller 反映 T 长高与否。 /_/

Status InsertAVL(BiTree /\*T,

int

e,Status /\*taller)
{

if

(!/\*T)
{

//_ 插入新结点，树“长高”，置 taller 为 TRUE /_/
/\*T=(BiTree)

malloc

(

sizeof

(BitNode));

(/*T)->data=e; (/*T)->lchild=(/*T)->rchild=NULL; (/*T)->bf=EH;
/\*taller=TRUE;

}
else

{
if

(e==(/\*T)->data)

{
//_ 树中已存在和 e 有相同关键字的结点则不再插入 /_/

/\*taller=FALSE;

return

FALSE;
}

if

(e<(/\*T)->data)
{

//_ 应继续在 T 的左子树中进行搜索 /_/
if

(!InsertAVL(&(/\*T)->lchild,e,taller))

//_ 未插入 /_/

return

FALSE;
if

(/\*taller)

//_ 已插入到 T 的左子树中且左子树“长高” /_/

switch

((/\*T)->bf)

//_ 检查 T 的平衡度 /_/
{

case

LH:

//_ 原本左子树比右子树高，需要作左平衡处理 /_/
LeftBalance(T);

/\*taller=FALSE;

break

;
case

EH:

//_ 原本左、右子树等高，现因左子树增高而使树增高 /_/

(/*T)->bf=LH;
/*taller=TRUE;

break

;

case

RH:

//_ 原本右子树比左子树高，现左、右子树等高 /_/
(/\*T)->bf=EH;

/\*taller=FALSE;

break

;
}

}
else

{

//_ 应继续在 T 的右子树中进行搜索 /_/
if

(!InsertAVL(&(/\*T)->rchild,e,taller))

//_ 未插入 /_/

return

FALSE;
if

(/\*taller)

//_ 已插入到 T 的右子树且右子树“长高” /_/

switch

((/\*T)->bf)

//_ 检查 T 的平衡度 /_/
{

case

LH:

//_ 原本左子树比右子树高，现左、右子树等高 /_/
(/\*T)->bf=EH;

/\*taller=FALSE;

break

;
case

EH:

//_ 原本左、右子树等高，现因右子树增高而使树增高 /_/

(/*T)->bf=RH;
/*taller=TRUE;

break

;

case

RH:

//_ 原本右子树比左子树高，需要作右平衡处理 /_/
RightBalance(T);

/\*taller=FALSE;

break

;
}

}
}

return

TRUE;
}

说明：

（1）程序开始执行时，如果 T 为空时，则申请内存新增一个结点。

（2）如果表示当存在相同结点，则不需要插入。

（3）当新结点 e 小于 T 的根结点时，则在 T 的左子树查找。

（4）递归调用本函数，直到找到则返回 FALSE，否则说明插入结点成功，执行下面语句。

（5）当 taller 为 TRUE 时，说明插入结点，此时需要判断 T 的平衡因子，如果是 1，说明左子树高于右子树，需要调用 LeftBalance 函数进行左平衡旋转处理。如果为 0 或-1，则说明新插入的结点没有让整棵二叉排序树失去平衡性，只需修改相关 BF 值即可。

（6）说明结点 e 大于 T 的根结点的值，在 T 的右子树查找。与上面类似。

删除结点的代码如下所示。

3、C 语言实现

<strong>/#include <stdio.h>

/#include <stdlib.h>
/#define OK 1

/#define ERROR 0
/#define TRUE 1

/#define FALSE 0
/#define MAXSIZE 100 //_ 存储空间初始分配量 /_/

typedef

int

Status;

//_ Status 是函数的类型,其值是函数结果状态代码，如 OK 等 /_/
//_ 二叉树的二叉链表结点结构定义 /_/

typedef

struct

BitNode

//_ 结点结构 /_/
{

int

data;

//_ 结点数据 /_/
int

bf;

//_ 结点的平衡因子 /_/

struct

BitNode /*lchild, /*rchild;

//_ 左右孩子指针 /_/
} BitNode, /\*BiTree;

//_ 对以 p 为根的二叉排序树作右旋处理 /_/
//_ 处理之后 p 指向新的树根结点，即旋转处理之前的左子树的根结点 /_/

//右旋-顺时针旋转(如 LL 型就得对根结点做该旋转)
void

R_Rotate(BiTree /\*P)

{
BiTree L;

L=(/\*P)->lchild;

//_ L 指向 P 的左子树根结点 /_/
(/\*P)->lchild=L->rchild;

//_ L 的右子树挂接为 P 的左子树 /_/

L->rchild=(/*P);
/*P=L;

//_ P 指向新的根结点 /_/

}
//_ 对以 P 为根的二叉排序树作左旋处理， /_/

//_ 处理之后 P 指向新的树根结点，即旋转处理之前的右子树的根结点 0 /_/
//左旋-逆时针旋转(如 RR 型就得对根结点做该旋转)

void

L_Rotate(BiTree /\*P)
{

BiTree R;
R = (/\*P)->rchild;

//_ R 指向 P 的右子树根结点 /_/

(/\*P)->rchild = R->lchild;

//_ R 的左子树挂接为 P 的右子树 /_/
R->lchild = (/\*P);

/\*P = R;

//_ P 指向新的根结点 /_/
}

/#define LH +1 //_ 左高 /_/
/#define EH 0 //_ 等高 /_/

/#define RH -1 //_ 右高 /_/
//_ 对以指针 T 所指结点为根的二叉树作左平衡旋转处理 /_/

//_ 本算法结束时，指针 T 指向新的根结点 /_/
void

LeftBalance(BiTree /\*T)

{
BiTree L,Lr;

L = (/\*T)->lchild;

//_ L 指向 T 的左子树根结点 /_/
switch

(L->bf)

{
//_ 检查 T 的左子树的平衡度，并作相应平衡处理 /_/

case

LH:

//_ 新结点插入在 T 的左孩子的左子树上，要作单右旋处理 /_/
(/\*T)->bf=L->bf=EH;

R_Rotate(T);
break

;

case

RH:

//_ 新结点插入在 T 的左孩子的右子树上，要作双旋处理 /_/

//
Lr=L->rchild;

//_ Lr 指向 T 的左孩子的右子树根 /_/

switch

(Lr->bf)
{

//_ 修改 T 及其左孩子的平衡因子 /_/
case

LH:

(/\*T)->bf=RH;
L->bf=EH;

break

;
case

EH:

(/\*T)->bf=L->bf=EH;
break

;

case

RH:
(/\*T)->bf=EH;

L->bf=LH;
break

;

}
Lr->bf=EH;

L_Rotate(&(/\*T)->lchild);

//_ 对 T 的左子树作左旋平衡处理 /_/
R_Rotate(T);

//_ 对 T 作右旋平衡处理 /_/

}
}

//_ 对以指针 T 所指结点为根的二叉树作右平衡旋转处理， /_/
//_ 本算法结束时，指针 T 指向新的根结点 /_/

void

RightBalance(BiTree /\*T)
{

BiTree R,Rl;
R=(/\*T)->rchild;

//_ R 指向 T 的右子树根结点 /_/

switch

(R->bf)
{

//_ 检查 T 的右子树的平衡度，并作相应平衡处理 /_/
case

RH:

//_ 新结点插入在 T 的右孩子的右子树上，要作单左旋处理 /_/

(/\*T)->bf=R->bf=EH;
L_Rotate(T);

break

;
case

LH:

//_ 新结点插入在 T 的右孩子的左子树上，要作双旋处理 /_/

//最小不平衡树的根结点为负，其右孩子为正

Rl=R->lchild;

//_ Rl 指向 T 的右孩子的左子树根 /_/
switch

(Rl->bf)

{
//_ 修改 T 及其右孩子的平衡因子 /_/

case

RH:
(/\*T)->bf=LH;

R->bf=EH;
break

;

case

EH:
(/\*T)->bf=R->bf=EH;

break

;
case

LH:

(/\*T)->bf=EH;
R->bf=RH;

break

;
}

Rl->bf=EH;
R_Rotate(&(/\*T)->rchild);

//_ 对 T 的右子树作右旋平衡处理 /_/

L_Rotate(T);

//_ 对 T 作左旋平衡处理 /_/
}

}
//_ 若在平衡的二叉排序树 T 中不存在和 e 有相同关键字的结点，则插入一个 /_/

//_ 数据元素为 e 的新结点，并返回 1，否则返回 0。若因插入而使二叉排序树 /_/
//_ 失去平衡，则作平衡旋转处理，布尔变量 taller 反映 T 长高与否。 /_/

Status InsertAVL(BiTree /\*T,

int

e,Status /\*taller)
{

if

(!/\*T)
{

//_ 插入新结点，树“长高”，置 taller 为 TRUE /_/
/\*T=(BiTree)

malloc

(

sizeof

(BitNode));

(/*T)->data=e;
(/*T)->lchild=(/\*T)->rchild=NULL;

(/*T)->bf=EH;
/*taller=TRUE;

}
else

{
if

(e==(/\*T)->data)

{
//_ 树中已存在和 e 有相同关键字的结点则不再插入 /_/

/\*taller=FALSE;
return

FALSE;

}
if

(e<(/\*T)->data)

{
//_ 应继续在 T 的左子树中进行搜索 /_/

if

(!InsertAVL(&(/\*T)->lchild, e, taller))

//_ 未插入 /_/
return

FALSE;

if

(/\*taller)

//_ 已插入到 T 的左子树中且左子树“长高” /_/
switch

((/\*T)->bf)

//_ 检查 T 的平衡度 /_/

{
case

LH:

//_ 原本左子树比右子树高，需要作左平衡处理 /_/

LeftBalance(T);
/\*taller=FALSE;

break

;
case

EH:

//_ 原本左、右子树等高，现因左子树增高而使树增高 /_/

(/*T)->bf=LH;
/*taller=TRUE;

break

;
case

RH:

//_ 原本右子树比左子树高，现左、右子树等高 /_/

(/*T)->bf=EH;
/*taller=FALSE;

break

;
}

}
else

{
//_ 应继续在 T 的右子树中进行搜索 /_/

if

(!InsertAVL(&(/\*T)->rchild,e, taller))

//_ 未插入 /_/
{

return

FALSE;
}

if

(/\*taller)

//_ 已插入到 T 的右子树且右子树“长高” /_/
{

switch

((/\*T)->bf)

//_ 检查 T 的平衡度 /_/
{

case

LH:

//_ 原本左子树比右子树高，现左、右子树等高 /_/
(/\*T)->bf=EH;

/\*taller=FALSE;
break

;

case

EH:

//_ 原本左、右子树等高，现因右子树增高而使树增高 /_/
(/\*T)->bf=RH;

/\*taller=TRUE;
break

;

case

RH:

//_ 原本右子树比左子树高，需要作右平衡处理 /_/
RightBalance(T);

/\*taller=FALSE;
break

;

}
}

}
}

return

TRUE;
}

//\*
若在平衡的二叉排序树 t 中存在和 e 有相同关键字的结点，则删除之

并返回 TRUE，否则返回 FALSE。若因删除而使二叉排序树
失去平衡，则作平衡旋转处理，布尔变量 shorter 反映 t 变矮与否

/\*/
int

deleteAVL(BiTree /\*t,

int

key,

int

/\*shorter)

{
if

(/\*t == NULL)

//不存在该元素

{
return

FALSE;

//删除失败

}
else

if

(key == (/\*t)->data)

//找到元素结点

{
BitNode /\*q = NULL;

if

((/\*t)->lchild == NULL)

//左子树为空
{

q = (/*t);
(/*t) = (/\*t)->rchild;

free

(q);
/\*shorter = TRUE;

}
else

if

((/\*t)->rchild == NULL)

//右子树为空

{
q = (/\*t);

(/*t) = (/*t)->lchild;
free

(q);

/\*shorter = TRUE;
}

else

//左右子树都存在,
{

q = (/\*t)->lchild;
while

(q->rchild)

{
q = q->rchild;

}
(/\*t)->data = q->data;

deleteAVL(&(/\*t)->lchild, q->data, shorter);

//在左子树中递归删除前驱结点
}

}
else

if

(key < (/\*t)->data)

//左子树中继续查找

{
if

(!deleteAVL(&(/\*t)->lchild, key, shorter))

{
return

FALSE;

}
if

(/\*shorter)

{
switch

((/\*t)->bf)

{
case

LH:

(/*t)->bf = EH;
/*shorter = TRUE;

break

;
case

EH:

(/*t)->bf = RH;
/*shorter = FALSE;

break

;
case

RH:

RightBalance(&(/\*t));

//右平衡处理
if

((/\*t)->rchild->bf == EH)

//注意这里，画图思考一下

/\*shorter = FALSE;
else

/\*shorter = TRUE;
break

;

}
}

}
else

//右子树中继续查找

{
if

(!deleteAVL(&(/\*t)->rchild, key, shorter))

{
return

FALSE;

}
if

(shorter)

{
switch

((/\*t)->bf)

{
case

LH:

LeftBalance(&(/\*t));

//左平衡处理
if

((/\*t)->lchild->bf == EH)

//注意这里，画图思考一下

/\*shorter = FALSE;
else

/\*shorter = TRUE;
break

;

case

EH:
(/\*t)->bf = LH;

/\*shorter = FALSE;
break

;

case

RH:
(/\*t)->bf = EH;

/\*shorter = TRUE;
break

;

}
}

}
return

TRUE;

}
void

InOrderTraverse(BiTree t)

{
if

(t)

{
InOrderTraverse(t->lchild);

printf

(

"%d "

, t->data);
InOrderTraverse(t->rchild);

}
}

int

main(

void

)
{

int

i;
int

a[10]={3,2,1,4,5,6,7,10,9,8};

BiTree T=NULL;
Status taller;

for

(i=0;i<10;i++)
{

InsertAVL(&T,a[i],&taller);
}

printf

(

"中序遍历二叉平衡树:\n"

);
InOrderTraverse(T);

printf

(

"\n"

);
printf

(

"删除结点元素 5 后中序遍历:\n"

);

int

shorter;
deleteAVL(&T, 5, &shorter);

InOrderTraverse(T);
printf

(

"\n"

);

return

0;
}</strong>

参考：

[https://www.cnblogs.com/zhujunxxxxx/p/3348798.html](https://www.cnblogs.com/zhujunxxxxx/p/3348798.html)
