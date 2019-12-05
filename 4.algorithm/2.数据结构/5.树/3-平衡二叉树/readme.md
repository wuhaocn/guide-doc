# [数据结构之 平衡二叉树](https://www.cnblogs.com/zhujunxxxxx/p/3348798.html)

平衡二叉树，是一种二叉排序树，其中每个结点的左子树和右子树的高度差至多等于1。它是一种高度平衡的二叉排序树。高度平衡？意思是说，要么它是一棵空树，要么它的左子树和右子树都是平衡二叉树，且左子树和右子树的深度之差的绝对值不超过1。

将二叉树上结点的左子树深度减去右子树深度的值称为平衡因子BF，那么平衡二叉树上的所有结点的平衡因子只可能是-1、0和1。只要二叉树上有一个结点的平衡因子的绝对值大于1，则该二叉树就是不平衡的。

平衡二叉树的前提是它是一棵二叉排序树。

距离插入结点最近的，且平衡因子的绝对值大于1的结点为根的子树，称为最小不平衡子树。如下图所示，当插入结点37时，距离它最近的平衡因子的绝对值超过1的结点是58。

![](http://blog.chinaunix.net/attachment/201301/20/26548237_1358647849QBbO.png)

1、平衡二叉树实现原理

平衡二叉树构建的基本思想就是在构建二叉排序树的过程中，每当插入一个结点时，先检查是否因插入而破坏了树的平衡性，若是，则找出最小不平衡子树。在保持二叉排序树特性的前提下，调整最小不平衡子树中各结点之间的链接关系，进行相应的旋转，使之成为新的平衡子树。 下面讲解一个平衡二叉树构建过程的例子。现在又a[10] = {3, 2, 1, 4, 5, 6, 7, 10, 9, 8}需要构建二叉排序树。在没有学习平衡二叉树之前，根据二叉排序树的特性，通常会将它构建成如下左图。虽然完全符合二叉排序树的定义，但是对这样高度达到8的二叉树来说，查找是非常不利的。因此，更加期望构建出如下右图的样子，高度为4的二叉排序树，这样才可以提供高效的查找效率。

![](http://blog.chinaunix.net/attachment/201301/20/26548237_1358648262bfLw.png)

现在来看看如何将一个数组构成出如上右图的树结构。 对于数组a的前两位3和2，很正常地构建，到了第个数“1”时，发现此时根结点“3”的平衡因子变成了2，此时整棵树都成了最小不平衡子树，需要进行调整，如下图图1（结点左上角数字为平衡因子BF值）。因为BF为正，因此将整个树进行右旋（顺时针），此时结点2成了根结点，3成了2的右孩子，这样三个结点的BF值均为0，非常的平衡，如下图图2所示。

![](http://blog.chinaunix.net/attachment/201301/20/26548237_1358648531VpPP.png)

然后再增加结点4，平衡因子没有改变，如上图图3。增加结点5时，结点3的BF值为-2，说明要旋转了。由于BF是负值，对这棵最小平衡子树进行左旋（逆时针旋转），如下图图4，此时整个树又达到了平衡。

![](http://blog.chinaunix.net/attachment/201301/20/26548237_1358648821ZpPK.png)

继续增加结点6时，发现根结点2的BF值变成了-2，如下图图6所示。所以对根结点进行了左旋，注意此时本来结点3是结点3的左孩子，由于旋转后需要满足二叉排序树特性，因此它成了结点2的右孩子，如图7所示。

![](http://blog.chinaunix.net/attachment/201301/20/26548237_13586496564218.png)

增加结点7，同样的左旋转，使得整棵树达到平衡，如下图8和9所示。

![](http://blog.chinaunix.net/attachment/201301/20/26548237_1358649801UbTZ.png)

当增加结点10时，结构无变化，如图10所示。再增加结点9，此时结点7的BF变成了-2，理论上只需要旋转最小不平衡树7、9、10即可，但是，如果左旋转后，结点9变成了10的右孩子，这是不符合二叉排序树的特性的，此时不能简单的左旋。如图11所示。

![](http://blog.chinaunix.net/attachment/201301/20/26548237_1358650084A4zx.png)

仔细观察图11，发现根本原因在于结点7的BF是-2，而结点10的BF是1，也就是说，它们两个一正一负，符号并不统一，而前面的几次旋转，无论左还是右旋，最小不平衡子树的根结点与它的子结点符号都是相同的。这就是不能直接旋转的关键。 不统一，不统一就把它们先转到符号统一再说，于是先对结点9和结点10进行右旋，使得结点10成了9的右子树，结点9的BF为-1，此时就与结点7的BF值符号统一了，如图12所示。

![](http://blog.chinaunix.net/attachment/201301/20/26548237_13586503529IMU.png)

这样再以结点7为最小不平衡子树进行左旋，得到如下图13。接着，插入8，情况与刚才类似，结点6的BF是-2，而它的右孩子9的BF是1，如图14，因此首先以9为根结点，进行右旋，得到图15，此时结点6和结点7的符号都是负，再以6为根结点左旋，最终得到最后的平衡二叉树，如图16所示。

![](http://blog.chinaunix.net/attachment/201301/20/26548237_1358650571sdU0.png)

通过这个例子，可以发现，当最小不平衡树根结点的平衡因子BF是大于1时，就右旋，小于-1时就左旋，如上例中的结点1、5、6、7的插入等。插入结点后，最小不平衡子树的BF与它的子树的BF符号相反时，就需要对结点先进行一次旋转以使得符号相同后，再反向旋转一次才能够完成平衡操作，如上例中结点9、8的插入时。

下面两个图讲解了插入时所要做的旋转操作的例子。《来自：http://www.cnblogs.com/guyan/archive/2012/09/03/2668399.html》

![](http://blog.chinaunix.net/attachment/201301/20/26548237_1358690724QUaT.png)

![](http://blog.chinaunix.net/attachment/201301/20/26548237_1358690668jy1n.png)

2、平衡二叉树算法的实现

首先是需要改进二叉排序树的结点结构，增加一个bf，用来存储平衡因子。

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

}BitNode, /*BiTree; 然后对于右旋（顺时针）操作，代码如下：

void

R_rotate(BiTree /*t)

{
BiTree s;

s = (/*t)->lchild;

//s指向t的左子树根结点
(/*t)->lchild = s->rchild;

//s的右子树挂接为t的左子树

s->rchild = (/*t);
/*p = s;

//t指向新的根结点

} 此函数代码的意思是说，当传入一个二叉排序树t，将它的左孩子结点定义为s，将s的右子树变成t的左子树，再将t改为s的右子树，最后将s替换为t的根结点。这样就完成了一次右旋操作。如下图示，图中三角形代表子树，N代表新增的结点。

![](http://blog.chinaunix.net/attachment/201301/20/26548237_1358676013pPD7.png)

上面的例子中新增加了结点N，就是右旋操作。

左旋代码如下所示。

void

L_rotate(BiTree /*t)

{
BiTree s;

s = (/*t)->rchild;

//s指向t的右子树根结点
(/*t)->rchild = s->lchild;

//s的左子树挂接为t的右子树

s->lchild = (/*t);
/*p = s;

//t指向新的根结点

} 下面看左旋转平衡（使左边平衡）的处理代码。

/#define LH +1 //* 左高 /*/

/#define EH 0 //* 等高 /*/
/#define RH -1 //* 右高 /*/

//* 对以指针T所指结点为根的二叉树作左平衡旋转处理 /*/
//* 本算法结束时，指针T指向新的根结点 /*/

void

LeftBalance(BiTree /*T)
{

BiTree L,Lr;
L = (/*T)->lchild;

//* L指向T的左子树根结点 /*/

switch

(L->bf)
{

//* 检查T的左子树的平衡度，并作相应平衡处理 /*/
case

LH:

//* 新结点插入在T的左孩子的左子树上，要作单右旋处理 /*/

(/*T)->bf=L->bf=EH;
R_Rotate(T);

break

;
case

RH:

//* 新结点插入在T的左孩子的右子树上，要作双旋处理 /*/

Lr=L->rchild;

//* Lr指向T的左孩子的右子树根 /*/
switch

(Lr->bf)

{

//* 修改T及其左孩子的平衡因子 /*/
case

LH: (/*T)->bf=RH;

L->bf=EH;
break

;

case

EH: (/*T)->bf=L->bf=EH;
break

;

case

RH: (/*T)->bf=EH;
L->bf=LH;

break

;
}

Lr->bf=EH;
L_Rotate(&(/*T)->lchild);

//* 对T的左子树作左旋平衡处理 /*/

R_Rotate(T);

//* 对T作右旋平衡处理 /*/
}

}

首先，定义三个常数变量，分别代码1、0、-1。

（1）函数被调用，传入一个需调整平衡型的子树T。由于LeftBalance函数被调用时，其实是已经确认当前子树是不平衡的状态，且左子树的高度大于右子树的高度。换句话说，此时T的根结点应该是平衡因子BF的值大于1的数。

（2）将T的左孩子赋值给L。

（3）然后是分支判断。

（4）当L的平衡因子为LH，即为1时，表明它与根结点的BF值符号相同，因此，将它们的BF值都改为0，并进行右旋（顺时针）操作，操作方式如图所示。

（5）当L的平衡因子为RH时，即为-1时，表明它与根结点的BF值符号相反，此时需要做双旋操作。针对L的右孩子的BF作判断，修改结点T和L的BF值。将当前的Lr的BF改为0。

（6）对根结点的左子树进行左旋，如下图第二图所示。

（7）对根结点进行右旋，如下图第三图所示，完成平衡操作。

![](http://blog.chinaunix.net/attachment/201301/20/26548237_1358677817GNjg.png)

右平衡（使右边平衡）旋转处理的函数代码如下。

//* 对以指针T所指结点为根的二叉树作右平衡旋转处理， /*/

//* 本算法结束时，指针T指向新的根结点 /*/
void

RightBalance(BiTree /*T)

{
BiTree R,Rl;

R=(/*T)->rchild;

//* R指向T的右子树根结点 /*/
switch

(R->bf)

{

//* 检查T的右子树的平衡度，并作相应平衡处理 /*/
case

RH:

//* 新结点插入在T的右孩子的右子树上，要作单左旋处理 /*/

(/*T)->bf=R->bf=EH;
L_Rotate(T);

break

;
case

LH:

//* 新结点插入在T的右孩子的左子树上，要作双旋处理 /*/

Rl=R->lchild;

//* Rl指向T的右孩子的左子树根 /*/
switch

(Rl->bf)

{

//* 修改T及其右孩子的平衡因子 /*/
case

RH: (/*T)->bf=LH;

R->bf=EH;
break

;

case

EH: (/*T)->bf=R->bf=EH;
break

;

case

LH: (/*T)->bf=EH;
R->bf=RH;

break

;
}

Rl->bf=EH;
R_Rotate(&(/*T)->rchild);

//* 对T的右子树作右旋平衡处理 /*/

L_Rotate(T);

//* 对T作左旋平衡处理 /*/
}

}

插入数据操作如下所示。

//* 若在平衡的二叉排序树T中不存在和e有相同关键字的结点，则插入一个 /*/

//* 数据元素为e的新结点，并返回1，否则返回0。若因插入而使二叉排序树 /*/
//* 失去平衡，则作平衡旋转处理，布尔变量taller反映T长高与否。 /*/

Status InsertAVL(BiTree /*T,

int

e,Status /*taller)
{

if

(!/*T)
{

//* 插入新结点，树“长高”，置taller为TRUE /*/
/*T=(BiTree)

malloc

(

sizeof

(BitNode));

(/*T)->data=e; (/*T)->lchild=(/*T)->rchild=NULL; (/*T)->bf=EH;
/*taller=TRUE;

}
else

{
if

(e==(/*T)->data)

{
//* 树中已存在和e有相同关键字的结点则不再插入 /*/

/*taller=FALSE;

return

FALSE;
}

if

(e<(/*T)->data)
{

//* 应继续在T的左子树中进行搜索 /*/
if

(!InsertAVL(&(/*T)->lchild,e,taller))

//* 未插入 /*/

return

FALSE;
if

(/*taller)

//* 已插入到T的左子树中且左子树“长高” /*/

switch

((/*T)->bf)

//* 检查T的平衡度 /*/
{

case

LH:

//* 原本左子树比右子树高，需要作左平衡处理 /*/
LeftBalance(T);

/*taller=FALSE;

break

;
case

EH:

//* 原本左、右子树等高，现因左子树增高而使树增高 /*/

(/*T)->bf=LH;
/*taller=TRUE;

break

;

case

RH:

//* 原本右子树比左子树高，现左、右子树等高 /*/
(/*T)->bf=EH;

/*taller=FALSE;

break

;
}

}
else

{

//* 应继续在T的右子树中进行搜索 /*/
if

(!InsertAVL(&(/*T)->rchild,e,taller))

//* 未插入 /*/

return

FALSE;
if

(/*taller)

//* 已插入到T的右子树且右子树“长高” /*/

switch

((/*T)->bf)

//* 检查T的平衡度 /*/
{

case

LH:

//* 原本左子树比右子树高，现左、右子树等高 /*/
(/*T)->bf=EH;

/*taller=FALSE;

break

;
case

EH:

//* 原本左、右子树等高，现因右子树增高而使树增高 /*/

(/*T)->bf=RH;
/*taller=TRUE;

break

;

case

RH:

//* 原本右子树比左子树高，需要作右平衡处理 /*/
RightBalance(T);

/*taller=FALSE;

break

;
}

}
}

return

TRUE;
}

说明：

（1）程序开始执行时，如果T为空时，则申请内存新增一个结点。

（2）如果表示当存在相同结点，则不需要插入。

（3）当新结点e小于T的根结点时，则在T的左子树查找。

（4）递归调用本函数，直到找到则返回FALSE，否则说明插入结点成功，执行下面语句。

（5）当taller为TRUE时，说明插入结点，此时需要判断T的平衡因子，如果是1，说明左子树高于右子树，需要调用LeftBalance函数进行左平衡旋转处理。如果为0或-1，则说明新插入的结点没有让整棵二叉排序树失去平衡性，只需修改相关BF值即可。

（6）说明结点e大于T的根结点的值，在T的右子树查找。与上面类似。

删除结点的代码如下所示。

3、C语言实现

<strong>/#include <stdio.h>

/#include <stdlib.h>
/#define OK 1

/#define ERROR 0
/#define TRUE 1

/#define FALSE 0
/#define MAXSIZE 100 //* 存储空间初始分配量 /*/

typedef

int

Status;

//* Status是函数的类型,其值是函数结果状态代码，如OK等 /*/
//* 二叉树的二叉链表结点结构定义 /*/

typedef

struct

BitNode

//* 结点结构 /*/
{

int

data;

//* 结点数据 /*/
int

bf;

//* 结点的平衡因子 /*/

struct

BitNode /*lchild, /*rchild;

//* 左右孩子指针 /*/
} BitNode, /*BiTree;

//* 对以p为根的二叉排序树作右旋处理 /*/
//* 处理之后p指向新的树根结点，即旋转处理之前的左子树的根结点 /*/

//右旋-顺时针旋转(如LL型就得对根结点做该旋转)
void

R_Rotate(BiTree /*P)

{
BiTree L;

L=(/*P)->lchild;

//* L指向P的左子树根结点 /*/
(/*P)->lchild=L->rchild;

//* L的右子树挂接为P的左子树 /*/

L->rchild=(/*P);
/*P=L;

//* P指向新的根结点 /*/

}
//* 对以P为根的二叉排序树作左旋处理， /*/

//* 处理之后P指向新的树根结点，即旋转处理之前的右子树的根结点0 /*/
//左旋-逆时针旋转(如RR型就得对根结点做该旋转)

void

L_Rotate(BiTree /*P)
{

BiTree R;
R = (/*P)->rchild;

//* R指向P的右子树根结点 /*/

(/*P)->rchild = R->lchild;

//* R的左子树挂接为P的右子树 /*/
R->lchild = (/*P);

/*P = R;

//* P指向新的根结点 /*/
}

/#define LH +1 //* 左高 /*/
/#define EH 0 //* 等高 /*/

/#define RH -1 //* 右高 /*/
//* 对以指针T所指结点为根的二叉树作左平衡旋转处理 /*/

//* 本算法结束时，指针T指向新的根结点 /*/
void

LeftBalance(BiTree /*T)

{
BiTree L,Lr;

L = (/*T)->lchild;

//* L指向T的左子树根结点 /*/
switch

(L->bf)

{
//* 检查T的左子树的平衡度，并作相应平衡处理 /*/

case

LH:

//* 新结点插入在T的左孩子的左子树上，要作单右旋处理 /*/
(/*T)->bf=L->bf=EH;

R_Rotate(T);
break

;

case

RH:

//* 新结点插入在T的左孩子的右子树上，要作双旋处理 /*/

//
Lr=L->rchild;

//* Lr指向T的左孩子的右子树根 /*/

switch

(Lr->bf)
{

//* 修改T及其左孩子的平衡因子 /*/
case

LH:

(/*T)->bf=RH;
L->bf=EH;

break

;
case

EH:

(/*T)->bf=L->bf=EH;
break

;

case

RH:
(/*T)->bf=EH;

L->bf=LH;
break

;

}
Lr->bf=EH;

L_Rotate(&(/*T)->lchild);

//* 对T的左子树作左旋平衡处理 /*/
R_Rotate(T);

//* 对T作右旋平衡处理 /*/

}
}

//* 对以指针T所指结点为根的二叉树作右平衡旋转处理， /*/
//* 本算法结束时，指针T指向新的根结点 /*/

void

RightBalance(BiTree /*T)
{

BiTree R,Rl;
R=(/*T)->rchild;

//* R指向T的右子树根结点 /*/

switch

(R->bf)
{

//* 检查T的右子树的平衡度，并作相应平衡处理 /*/
case

RH:

//* 新结点插入在T的右孩子的右子树上，要作单左旋处理 /*/

(/*T)->bf=R->bf=EH;
L_Rotate(T);

break

;
case

LH:

//* 新结点插入在T的右孩子的左子树上，要作双旋处理 /*/

//最小不平衡树的根结点为负，其右孩子为正

Rl=R->lchild;

//* Rl指向T的右孩子的左子树根 /*/
switch

(Rl->bf)

{
//* 修改T及其右孩子的平衡因子 /*/

case

RH:
(/*T)->bf=LH;

R->bf=EH;
break

;

case

EH:
(/*T)->bf=R->bf=EH;

break

;
case

LH:

(/*T)->bf=EH;
R->bf=RH;

break

;
}

Rl->bf=EH;
R_Rotate(&(/*T)->rchild);

//* 对T的右子树作右旋平衡处理 /*/

L_Rotate(T);

//* 对T作左旋平衡处理 /*/
}

}
//* 若在平衡的二叉排序树T中不存在和e有相同关键字的结点，则插入一个 /*/

//* 数据元素为e的新结点，并返回1，否则返回0。若因插入而使二叉排序树 /*/
//* 失去平衡，则作平衡旋转处理，布尔变量taller反映T长高与否。 /*/

Status InsertAVL(BiTree /*T,

int

e,Status /*taller)
{

if

(!/*T)
{

//* 插入新结点，树“长高”，置taller为TRUE /*/
/*T=(BiTree)

malloc

(

sizeof

(BitNode));

(/*T)->data=e;
(/*T)->lchild=(/*T)->rchild=NULL;

(/*T)->bf=EH;
/*taller=TRUE;

}
else

{
if

(e==(/*T)->data)

{
//* 树中已存在和e有相同关键字的结点则不再插入 /*/

/*taller=FALSE;
return

FALSE;

}
if

(e<(/*T)->data)

{
//* 应继续在T的左子树中进行搜索 /*/

if

(!InsertAVL(&(/*T)->lchild, e, taller))

//* 未插入 /*/
return

FALSE;

if

(/*taller)

//* 已插入到T的左子树中且左子树“长高” /*/
switch

((/*T)->bf)

//* 检查T的平衡度 /*/

{
case

LH:

//* 原本左子树比右子树高，需要作左平衡处理 /*/

LeftBalance(T);
/*taller=FALSE;

break

;
case

EH:

//* 原本左、右子树等高，现因左子树增高而使树增高 /*/

(/*T)->bf=LH;
/*taller=TRUE;

break

;
case

RH:

//* 原本右子树比左子树高，现左、右子树等高 /*/

(/*T)->bf=EH;
/*taller=FALSE;

break

;
}

}
else

{
//* 应继续在T的右子树中进行搜索 /*/

if

(!InsertAVL(&(/*T)->rchild,e, taller))

//* 未插入 /*/
{

return

FALSE;
}

if

(/*taller)

//* 已插入到T的右子树且右子树“长高” /*/
{

switch

((/*T)->bf)

//* 检查T的平衡度 /*/
{

case

LH:

//* 原本左子树比右子树高，现左、右子树等高 /*/
(/*T)->bf=EH;

/*taller=FALSE;
break

;

case

EH:

//* 原本左、右子树等高，现因右子树增高而使树增高 /*/
(/*T)->bf=RH;

/*taller=TRUE;
break

;

case

RH:

//* 原本右子树比左子树高，需要作右平衡处理 /*/
RightBalance(T);

/*taller=FALSE;
break

;

}
}

}
}

return

TRUE;
}

//*
若在平衡的二叉排序树t中存在和e有相同关键字的结点，则删除之

并返回TRUE，否则返回FALSE。若因删除而使二叉排序树
失去平衡，则作平衡旋转处理，布尔变量shorter反映t变矮与否

/*/
int

deleteAVL(BiTree /*t,

int

key,

int

/*shorter)

{
if

(/*t == NULL)

//不存在该元素

{
return

FALSE;

//删除失败

}
else

if

(key == (/*t)->data)

//找到元素结点

{
BitNode /*q = NULL;

if

((/*t)->lchild == NULL)

//左子树为空
{

q = (/*t);
(/*t) = (/*t)->rchild;

free

(q);
/*shorter = TRUE;

}
else

if

((/*t)->rchild == NULL)

//右子树为空

{
q = (/*t);

(/*t) = (/*t)->lchild;
free

(q);

/*shorter = TRUE;
}

else

//左右子树都存在,
{

q = (/*t)->lchild;
while

(q->rchild)

{
q = q->rchild;

}
(/*t)->data = q->data;

deleteAVL(&(/*t)->lchild, q->data, shorter);

//在左子树中递归删除前驱结点
}

}
else

if

(key < (/*t)->data)

//左子树中继续查找

{
if

(!deleteAVL(&(/*t)->lchild, key, shorter))

{
return

FALSE;

}
if

(/*shorter)

{
switch

((/*t)->bf)

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

RightBalance(&(/*t));

//右平衡处理
if

((/*t)->rchild->bf == EH)

//注意这里，画图思考一下

/*shorter = FALSE;
else

/*shorter = TRUE;
break

;

}
}

}
else

//右子树中继续查找

{
if

(!deleteAVL(&(/*t)->rchild, key, shorter))

{
return

FALSE;

}
if

(shorter)

{
switch

((/*t)->bf)

{
case

LH:

LeftBalance(&(/*t));

//左平衡处理
if

((/*t)->lchild->bf == EH)

//注意这里，画图思考一下

/*shorter = FALSE;
else

/*shorter = TRUE;
break

;

case

EH:
(/*t)->bf = LH;

/*shorter = FALSE;
break

;

case

RH:
(/*t)->bf = EH;

/*shorter = TRUE;
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

"删除结点元素5后中序遍历:\n"

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