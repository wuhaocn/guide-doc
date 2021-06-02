### 1.分治法

#### 1.q34\_在排序数组中查找元素的第一个和最后一个位置

- 问题

---

q34\_在排序数组中查找元素的第一个和最后一个位置
https://leetcode-cn.com/problems/find-first-and-last-position-of-element-in-sorted-array/

---

给定一个按照升序排列的整数数组 nums，和一个目标值 target。
找出给定目标值在数组中的开始位置和结束位置。

如果数组中不存在目标值 target，返回 [-1, -1]。

进阶：

你可以设计并实现时间复杂度为 O(log n) 的算法解决此问题吗？

示例 1：
输入：nums = [5,7,7,8,8,10], target = 8
输出：[3,4]

示例 2：
输入：nums = [5,7,7,8,8,10], target = 6
输出：[-1,-1]

示例 3：
输入：nums = [], target = 0
输出：[-1,-1]

提示：

0 <= nums.length <= 105
-109 <= nums[i] <= 109
nums 是一个非递减数组
-109 <= target <= 109

Related Topics 数组 二分查找
👍 967 👎 0

- 解答

```
package 分治法.q34_在排序数组中查找元素的第一个和最后一个位置;

/**
 * 二分法 o(log(n))
 */
public class Solution {

    public int[] searchRange(int[] nums, int target) {
        if (nums == null || nums.length < 1) {
            return new int[]{-1, -1};
        }
        int midIndex = find(0, nums.length - 1, nums, target);
        int[] rs = new int[2];
        rs[0] = midIndex;
        rs[1] = midIndex;
        if (midIndex == -1) {
            return rs;
        }
        while (nums[rs[0]] == target && rs[0] > 0) {
            int temp = find(0, rs[0] - 1, nums, target);
            if (temp == -1) {
                break;
            } else {
                rs[0] = temp;
            }
        }

        while (nums[rs[1]] == target && rs[1] < nums.length - 1) {
            int temp = find(rs[1] + 1, nums.length - 1, nums, target);
            if (temp == -1) {
                break;
            } else {
                rs[1] = temp;
            }
        }
        return rs;
    }

    public int find(int beginIndex, int endIndex, int[] nums, int target) {
        if (beginIndex == endIndex) {
            if (nums[beginIndex] == target) {
                return beginIndex;
            } else {
                return -1;
            }
        }
        int mid = (endIndex - beginIndex) / 2 + beginIndex;
        if (nums[mid] > target) {
            return find(beginIndex, mid, nums, target);
        } else if (nums[mid] < target) {
            return find(mid + 1, endIndex, nums, target);
        } else {
            return mid;
        }
    }

    public static void main(String[] args) {
        new Solution().searchRange(new int[]{2, 2}, 2);
    }
}
```

#### 2.q33\_搜索旋转排序数组

- 问题

---

q33\_搜索旋转排序数组
https://leetcode-cn.com/problems/search-in-rotated-sorted-array/

---

整数数组 nums 按升序排列，数组中的值 互不相同 。

在传递给函数之前，nums 在预先未知的某个下标 k（0 <= k < nums.length）上进行了 旋转，
使数组变为 [nums[k], nums[k+1], ..., nums[n-1], nums[0], nums[1], ..., nums[k-1]]
（下标 从 0 开始 计数）。例如， [0,1,2,4,5,6,7] 在下标 3 处经旋转后可能变为 [4,5,6,7,0,1,2] 。

给你 旋转后 的数组 nums 和一个整数 target ，
如果 nums 中存在这个目标值 target ，则返回它的下标，否则返回 -1 。

示例 1：

输入：nums = [4,5,6,7,0,1,2], target = 0
输出：4

示例 2：

输入：nums = [4,5,6,7,0,1,2], target = 3
输出：-1

示例 3：

输入：nums = [1], target = 0
输出：-1

提示：

1 <= nums.length <= 5000
-10^4 <= nums[i] <= 10^4
nums 中的每个值都 独一无二
题目数据保证 nums 在预先未知的某个下标上进行了旋转
-10^4 <= target <= 10^4

进阶：你可以设计一个时间复杂度为 O(log n) 的解决方案吗？
Related Topics 数组 二分查找
👍 1335 👎 0

- 解答

```
package 分治法.q33_搜索旋转排序数组;

/**
 * 循环有序数组查找 二分法o(log(n))
 */
class Solution {
    public int search(int[] nums, int target) {
        return search(nums, 0, nums.length - 1, target);
    }

    private int search(int[] nums, int low, int high, int target) {
        if (low > high) {
            return -1;
        }
        int mid = (low + high) / 2;
        if (nums[mid] == target) {
            return mid;
        }
        //nums[mid] < nums[high]说明后半段有序
        if (nums[mid] < nums[high]) {
            //说明target在后半段
            if (nums[mid] < target && target <= nums[high]) {
                return search(nums, mid + 1, high, target);
            }
            return search(nums, low, mid - 1, target);
        } else {
            //后半段无序前半段有序，target在前半段
            if (nums[low] <= target && target < nums[mid]) {
                return search(nums, low, mid - 1, target);
            }
            return search(nums, mid + 1, high, target);
        }
    }
}
```

#### 3.q23\_合并 K 个排序链表

- 问题

题目

给你一个链表数组，每个链表都已经按升序排列。
请你将所有链表合并到一个升序链表中，返回合并后的链表。

示例 1：

输入：lists = [[1,4,5],[1,3,4],[2,6]]
输出：[1,1,2,3,4,4,5,6]
解释：链表数组如下：
[
1->4->5,
1->3->4,
2->6
]
将它们合并到一个有序链表中得到。
1->1->2->3->4->4->5->6

示例 2：

输入：lists = []
输出：[]

示例 3：

输入：lists = [[]]
输出：[]

提示：

k == lists.length
0 <= k <= 10^4
0 <= lists[i].length <= 500
-10^4 <= lists[i][j] <= 10^4
lists[i] 按 升序 排列
lists[i].length 的总和不超过 10^4

Related Topics 堆 链表 分治算法
👍 1226 👎 0

- 解答

```
package 分治法.q23_合并K个排序链表;

/**
 * 做k-1次mergeTwoLists  o(N*k) 可用分治法优化至o(N*log(k))) N为所有list的总节点数
 */
class Solution {
    /**
     * 合并有序数组
     *
     * @param lists 有序数组
     * @return
     */
    public ListNode mergeKLists(ListNode[] lists) {
        //1.边界条件判断
        if (lists.length == 0) {
            return null;
        }
        if (lists.length == 1) {
            return lists[0];
        }

        //2.以数组第一位元素为链表头循环遍历
        ListNode result = lists[0];
        for (int i = 1; i < lists.length; i++) {
            result = mergeTwoLists(result, lists[i]);
        }
        return result;
    }

    /**
     * 合并两个有序数组
     *
     * @param l1
     * @param l2
     * @return
     */
    public ListNode mergeTwoLists(ListNode l1, ListNode l2) {
        //1.边界条件判断
        if (l1 == null) {
            return l2;
        }
        if (l2 == null) {
            return l1;
        }
        //2.
        ListNode head = new ListNode(Integer.MIN_VALUE);
        head.next = l1;
        ListNode pre = head;
        while (l2 != null) {
            ListNode t1 = pre.next;
            ListNode t2 = l2.next;
            while (l2.val > t1.val) {
                if (t1.next == null) {
                    t1.next = l2;
                    return head.next;
                } else {
                    pre = pre.next;
                    t1 = t1.next;
                }
            }
            pre.next = l2;
            l2.next = t1;
            l2 = t2;
        }
        return head.next;
    }


    public void print(ListNode node){
        ListNode tmp = node;
        while (tmp != null){
            System.out.print(tmp.val);
            System.out.print("->");
            tmp = tmp.next;
        }
        System.out.println();
    }
    public static void main(String[] args) {
        //3个已排序数组
        ListNode listNode1 = new ListNode(1);
        ListNode listNode2 = new ListNode(2);
        ListNode listNode3 = new ListNode(3);
        listNode1.next = listNode2;
        listNode2.next = listNode3;
        ListNode listNodea1 = new ListNode(1);
        ListNode listNodea2 = new ListNode(3);
        ListNode listNodea3 = new ListNode(4);
        listNodea1.next = listNodea2;
        listNodea2.next = listNodea3;
        ListNode listNodeb1 = new ListNode(2);
        ListNode listNodeb2 = new ListNode(3);
        ListNode listNodeb3 = new ListNode(4);
        listNodeb1.next = listNodeb2;
        listNodeb2.next = listNodeb3;
        ListNode[] lists = new ListNode[]{listNode1, listNodea1, listNodeb1};
        Solution solution = new Solution();
        ListNode listNode = solution.mergeKLists(lists);
        solution.print(listNode);
    }
}
```

### 2.链表操作

#### 1.q25_k 个一组翻转链表

- 问题

* 25.K 个一组翻转链表

给你一个链表，每 k 个节点一组进行翻转，请你返回翻转后的链表。
k 是一个正整数，它的值小于或等于链表的长度。
如果节点总数不是 k 的整数倍，那么请将最后剩余的节点保持原有顺序。

进阶：

你可以设计一个只使用常数额外空间的算法来解决此问题吗？
你不能只是单纯的改变节点内部的值，而是需要实际进行节点交换。

- 示例 1：

```
输入：head = [1,2,3,4,5], k = 2
输出：[2,1,4,3,5]
```

- 示例 2：

```
输入：head = [1,2,3,4,5], k = 3
输出：[3,2,1,4,5]
```

- 示例 3：

```
输入：head = [1,2,3,4,5], k = 1
输出：[1,2,3,4,5]
```

- 示例 4：

```
输入：head = [1], k = 1
输出：[1]
```

- 提示：

```
列表中节点的数量在范围 sz 内
1 <= sz <= 5000
0 <= Node.val <= 1000
1 <= k <= sz
```

- 解答

```
package 链表操作.q25_k个一组翻转链表;

/**
 * 难点在于返回每个部分被修改的头节点，新建一个头节点的前置节点 o(n)
 */
public class Solution {

    public ListNode reverseKGroup(ListNode head, int k) {
        ListNode hair = new ListNode(0);
        hair.next = head;

        ListNode pre = hair;
        ListNode end = hair;

        while (end.next != null) {
            for (int i = 0; i < k && end != null; i++){
                end = end.next;
            }
            if (end == null){
                break;
            }
            ListNode start = pre.next;
            ListNode next = end.next;
            end.next = null;
            pre.next = reverse(start);
            start.next = next;
            pre = start;

            end = pre;
        }
        return hair.next;
    }

    private ListNode reverse(ListNode head) {
        ListNode pre = null;
        ListNode curr = head;
        while (curr != null) {
            ListNode next = curr.next;
            curr.next = pre;
            pre = curr;
            curr = next;
        }
        return pre;
    }
}
```

#### 2.q206\_反转链表

##### 1.f2

- 问题

* 解答

```
package 链表操作.q206_反转链表.f2;

/**
 * 递归法 o(n)
 */
class Solution {

    public ListNode reverseList(ListNode head) {
        if (head == null || head.next == null) {
            return head;
        }
        ListNode p = reverseList(head.next);
        head.next.next = head;
        head.next = null;
        return p;
    }
}
```

- 问题

* 206.反转链表

给你单链表的头节点 head ，请你反转链表，并返回反转后的链表。

- 示例 1：

```
输入：head = [1,2,3,4,5]
输出：[5,4,3,2,1]
```

- 示例 2：

```
输入：head = [1,2]
输出：[2,1]
```

- 示例 3：

```
输入：head = []
输出：[]
```

- 提示：

```
链表中节点的数目范围是 [0, 5000]
-5000 <= Node.val <= 5000
```

- 解答

```

```

#### 3.q19\_删除链表的倒数第 N 个节点

- 问题

* 19.删除链表的倒数第 N 个结点
  给你一个链表，删除链表的倒数第 n 个结点，
  并且返回链表的头结点。

进阶：你能尝试使用一趟扫描实现吗？

- 示例 1：

```
输入：head = [1,2,3,4,5], n = 2
输出：[1,2,3,5]
```

- 示例 2：

```
输入：head = [1], n = 1
输出：[]
```

- 示例 3：

```
输入：head = [1,2], n = 1
输出：[1]
```

- 提示：

```
链表中结点的数目为 sz
1 <= sz <= 30
0 <= Node.val <= 100
1 <= n <= sz
```

- 解答

```
package 链表操作.q19_删除链表的倒数第N个节点;

/**
 * 利用两个指针 o(n)
 * 1.双指针法
 * 2.分别记录快慢指针位置
 * 3.构造差值n
 * 4.迭代寻找
 * 5.链表结点移除
 */
public class Solution {

    public ListNode removeNthFromEnd(ListNode head, int n) {
        if (head == null){
            return head;
        }
        //1.构造中继节点
        ListNode dummy = new ListNode(0);
        dummy.next = head;
        //2.构造快慢指针
        ListNode first = dummy;
        ListNode second = dummy;

        //3.快慢指针赋值
        for (int i = 1; i <= n + 1; i++) {
            first = first.next;
        }

        //4.迭代寻找位置
        while (first != null) {
            first = first.next;
            second = second.next;
        }
        //5.链表结点移除
        second.next = second.next.next;

        return dummy.next;
    }
}
```

#### 4.q61\_旋转链表

- 问题

* 61.旋转链表

给你一个链表的头节点 head ，旋转链表，将链表每个节点向右移动 k 个位置。

- 示例 1：

```
输入：head = [1,2,3,4,5], k = 2
输出：[4,5,1,2,3]
```

- 示例 2：

```
输入：head = [0,1,2], k = 4
输出：[2,0,1]
```

- 提示：

```
链表中节点的数目在范围 [0, 500] 内
-100 <= Node.val <= 100
0 <= k <= 2 * 109
```

- 解答

```
package 链表操作.q61_旋转链表;

/**
 * 解法1 暴力解法
 * 通过遍历k次，每一次遍历都完成一次尾节点的右移，最终实现题目要求
 * 解法2 反转链表，逆序变正序
 * 通过旋转链表的方式，让原本逆向的操作变成正向的操作，然后就可以从头节点开始直接操作即可。
 * 解法3 快慢指针
 * 快慢指针的方式，这种方式也可以经常用来解决需要反向操作链表的问题。
 * 主要思想是，让快指针先走k次，然后慢指针再和快指针一起走，当快指针走完时，慢指针会刚好来到k的位置。此时慢指针的下一个节点就是新的头节点，慢指针当前的节点就是尾节点，最后把快指针当前的节点链上原来的头节点即可。
 * 解法4 求得链表长度解决
 * 比较正常的思路，先遍历一次链表，得到链表的长度，和链表的最后一个节点，有了链表的长度就可以直接遍历到（长度-k）的位置了，之后直接拼接下即可。
 * 解法5 先连接成环再找断点 o(n)
 */
public class Solution {

    public ListNode rotateRight(ListNode head, int k) {
        if (head == null) {
            return null;
        }
        if (head.next == null) {
            return head;
        }

        ListNode oldTail = head;
        int n;
        for (n = 1; oldTail.next != null; n++) {
            oldTail = oldTail.next;
        }
        oldTail.next = head;
        ListNode newTail = head;
        for (int i = 0; i < n - k % n - 1; i++) {
            newTail = newTail.next;
        }
        ListNode newHead = newTail.next;
        newTail.next = null;

        return newHead;
    }
}
```

#### 5.q2\_两数相加

- 问题

* 2.两数相加

给你两个 非空 的链表，表示两个非负的整数。
它们每位数字都是按照 逆序 的方式存储的，并且每个节点只能存储一位数字。
请你将两个数相加，并以相同形式返回一个表示和的链表。
你可以假设除了数字 0 之外，这两个数都不会以 0 开头。

- 示例 1：

```
输入：l1 = [2,4,3], l2 = [5,6,4]
输出：[7,0,8]
解释：342 + 465 = 807.
```

- 示例 2：

```
输入：l1 = [0], l2 = [0]
输出：[0]
```

- 示例 3：

```
输入：l1 = [9,9,9,9,9,9,9], l2 = [9,9,9,9]
输出：[8,9,9,9,0,0,0,1]
```

- 提示：

```
每个链表中的节点数在范围 [1, 100] 内
0 <= Node.val <= 9
题目数据保证列表表示的数字不含前导零
```

- 解答

```
package 链表操作.q2_两数相加;

/**
 * 两次遍历
 * 第一次遍历：两个链表对应每个节点分别取和，若含有空节点则空节点取0，产生一个新链表。
 * 第二次遍历：对取完和的新链表遍历，判断当前的val是否大于等于10，大于或等于则其自身-10其next加1，若next为空则新建0节点。
 */
public class Solution {
    public ListNode addTwoNumbers(ListNode l1, ListNode l2) {
        ListNode rs = new ListNode(l1.val + l2.val);

        l1 = l1.next;
        l2 = l2.next;
        ListNode temp = rs;
        //第一次遍历：两个链表对应每个节点分别取和，若含有空节点则空节点取0，产生一个新链表
        while (l1 != null || l2 != null) {
            int a = 0;
            int b = 0;
            if (l1 != null) {
                a = l1.val;
            }
            if (l2 != null) {
                b = l2.val;
            }

            int t = a + b;
            temp.next = new ListNode(t);
            temp = temp.next;
            if (l1 != null) {
                l1 = l1.next;
            }
            if (l2 != null) {
                l2 = l2.next;
            }
        }

        temp = rs;
        //第一次遍历：两个链表对应每个节点分别取和，若含有空节点则空节点取0，产生一个新链表
        while (temp != null) {
            if (temp.val >= 10) {
                temp.val = temp.val - 10;
                if (temp.next == null) {
                    temp.next = new ListNode(0);
                }
                temp.next.val = temp.next.val + 1;
            }
            temp = temp.next;
        }

        return rs;
    }
}
```

- 问题

- 1.简介

- 1.1 定义

线性表*（linear list）*是数据结构的一种，一个线性表是 n 个具有相同特性的数据元素的有限序列。数据元素是一个抽象的符号，其具体含义在不同的情况下一般不同。

在稍复杂的线性表中，一个数据元素可由多个数据项*（item）*组成，此种情况下常把数据元素称为记录，含有大量记录的线性表又称文件。
线性表中的个数 n 定义为线性表的长度，n=0 时称为空表。在非空表中每个数据元素都有一个确定的位置，如用 ai 表示数据元素，则 i 称为数据元素 ai 在线性表中的位序。

线性表的相邻元素之间存在着序偶关系。如用（a1，…，ai-1，ai，ai+1，…，an）表示一个顺序表，则表中 ai-1 领先于 ai，ai 领先于 ai+1，
称 ai-1 是 ai 的直接前驱元素，ai+1 是 ai 的直接后继元素。当 i=1,2，…，n-1 时，ai 有且仅有一个直接后继，当 i=2，3，…，n 时，ai 有且仅有一个直接前驱

- 1.2 分类

我们说“线性”和“非线性”，只在逻辑层次上讨论，而不考虑存储层次，所以双向链表和循环链表依旧是线性表。
在数据结构逻辑层次上细分，线性表可分为一般线性表和受限线性表。一般线性表也就是我们通常所说的“线性表”，可以自由的删除或添加结点。受限线性表主要包括栈和队列，受限表示对结点的操作受限制。

- 1.3 优点

线性表的逻辑结构简单，便于实现和操作。因此，线性表这种数据结构在实际应用中是广泛采用的一种数据结构。

- 2.特征

  1．集合中必存在唯一的一个“第一元素”。
  2．集合中必存在唯一的一个 “最后元素” 。
  3．除最后一个元素之外，均有唯一的后继(后件)。
  4．除第一个元素之外，均有唯一的前驱(前件)。

- 3.基本操作

  1）MakeEmpty(L) 这是一个将 L 变为空表的方法
  2）Length（L） 返回表 L 的长度，即表中元素个数

  3）Get（L，i） 这是一个函数，函数值为 L 中位置 i 处的元素（1≤i≤n）
  4）Prior（L，i） 取 i 的前驱元素

  5）Next（L，i） 取 i 的后继元素
  6）Locate（L，x） 这是一个函数，函数值为元素 x 在 L 中的位置

  7）Insert（L，i，x）在表 L 的位置 i 处插入元素 x，将原占据位置 i 的元素及后面的元素都向后推一个位置
  8）Delete（L，p） 从表 L 中删除位置 p 处的元素

  9）IsEmpty(L) 如果表 L 为空表(长度为 0)则返回 true，否则返回 false
  10）Clear（L）清除所有元素

  11）Init（L）同第一个，初始化线性表为空
  12）Traverse（L）遍历输出所有元素

  13）Find（L，x）查找并返回元素
  14）Update（L，x）修改元素

  15）Sort（L）对所有元素重新按给定的条件排序

  16. strstr(string1,string2)用于字符数组的求 string1 中出现 string2 的首地址

- 4.存储结构

线性表主要由顺序表示或链式表示。在实际应用中，常以栈、队列、字符串等特殊形式使用。

- 4.1 顺序存储

顺序表示指的是用一组地址连续的存储单元依次存储线性表的数据元素，称为线性表的顺序存储结构或顺序映像*（sequential mapping）*。它以“物理位置相邻”来表示线性表中数据元素间的逻辑关系，可随机存取表中任一元素。
线性表顺序存储结构的优缺点：

- 优点：

  - a、无须为表示表中元素之间的逻辑关系而增加额外的存储空间。
  - b、可以快速地存取表中任一位置的元素。

- 缺点：
  - a、插入和删除操作需要移动大量元素。
  - b、当线性表长度变化较大时，难以确定存储空间的容量。
  - c、造成存储空间的“碎片”。

* 4.2 链式存储

链式表示指的是用一组任意的存储单元存储线性表中的数据元素，称为线性表的链式存储结构。它的存储单元可以是连续的，也可以是不连续的。在表示数据元素之间的逻辑关系时，除了存储其本身的信息之外，还需存储一个指示其直接后继的信息*（即直接后继的存储位置）*，这两部分信息组成数据元素的存储映像，称为结点*（node）*。它包括两个域；存储数据元素信息的域称为数据域；存储直接后继存储位置的域称为指针域。指针域中存储的信息称为指针或链[1][]()。

线性表链式存储结构的优缺点：

- 优点：
  - a、插入、删除操作方便，不会导致元素的移动，因为元素增减，只需要调整指针。
- 缺点：
  - a、顺序查找，查找复杂度为 o(n)

* 5.结构特点

  1.均匀性：虽然不同数据表的数据元素可以是各种各样的，但对于同一线性表的各数据元素必定具有相同的数据类型和长度。

  2.有序性：各数据元素在线性表中的位置只取决于它们的序号，数据元素之前的相对位置是线性的，即存在唯一的“第一个“和“最后一个”的数据元素，除了第一个和最后一个外，其它元素前面均只有一个数据元素(直接前驱)和后面均只有一个数据元素（直接后继）。

* 6.线性表的推广

时间有序表、排序表、和频率有序表都可以看做是线性表的推广。如果按照结点到达结构的时间先后，作为确定结点之间关系的，这样一种线性结构称之为时间有序表。例如，在红灯前停下的一长串汽车，最先到达的为首结点，最后到达的为尾结点；在离开时最先到达的汽车将最先离开，最后到达的将最后离开。这些汽车构成理一个队列，实际上就是一个时间有序表。栈和队列都是时间有序表。频率有序表是按照结点的使用频率确定它们之间的相互关系的，而排序表是根据结点的关键字值来加以确定的。[2][]()

- 解答

```

```

### 3.其他

#### 1.丢玻璃球

- 问题

* 解答

```
package 其他.丢玻璃球;

import java.util.Scanner;

/**
 * F(N) = min{ max(1(碎了), F(N - 1) + 1(没碎)), max(2, F(N - 2) + 1), max(3, F(N - 3) + 1), …… , max(N - 1, F(1))
 */
public class Solution {
    public static void main(String[] args) {
        int N = 0;
        Scanner scanner = new Scanner(System.in);
        while (N < 1) {
            N = scanner.nextInt();
        }

        int[] dp = new int[N + 1];
        dp[0] = 0;
        dp[1] = 1;

        for (int i = 2; i <= N; ++i) {
            int min = i;
            for (int j = 1; j < i; ++j) {
                int tmp = Math.max(j, dp[i - j] + 1);
                if (tmp < min) {
                    min = tmp;
                }
            }
            dp[i] = min;
        }

        System.out.println(dp[N]);
    }
}
```

#### 2.按顺序打印线程

- 问题

* 解答

```

```

#### 3.二叉树的前中后序遍历

- 问题

* 解答

```

```

#### 4.迪杰斯特拉

- 问题

* 解答

```

```

#### 5.线程安全的本地缓存

- 问题

* 解答

```

```

#### 6.生产者消费者模型

- 问题

* 解答

```

```

### 4.数字操作

#### 1.q9\_回文数

- 问题

* 9.回文数

给你一个整数 x ，如果 x 是一个回文整数，
返回 true ；否则，返回 false 。
回文数是指正序（从左向右）和倒序（从右向左）读都是一样的整数。
例如，121 是回文，而 123 不是。

- 示例 1：

```
输入：x = 121
输出：true
```

- 示例 2：

```
输入：x = -121
输出：false
解释：从左向右读, 为 -121 。 从右向左读, 为 121- 。
     因此它不是一个回文数。
```

- 示例 3：

```
输入：x = 10
输出：false
解释：从右向左读, 为 01 。因此它不是一个回文数。
```

- 示例 4：

```
输入：x = -101
输出：false
```

- 提示：

```
-231 <= x <= 231 - 1
```

- 进阶：

```
你能不将整数转为字符串来解决这个问题吗？
```

- 解答

```
package 数字操作.q9_回文数;

/**
 * 不转换成String 反转一半的数字o(log(n))
 */
public class Solution {
    public boolean isPalindrome(int x) {
        if (x < 0) {
            return false;
        }
        if (x < 10) {
            return true;
        }
        if (x % 10 == 0) {
            return false;
        }
        int rs = 0;
        while (rs < x / 10) {
            int y = x % 10;
            x = x / 10;
            rs = rs * 10 + y;
            if (rs == x) {
                return true;
            } else if (x / 10 == rs) {
                return true;
            }
        }
        return false;
    }
}
```

#### 2.q7\_整数反转

##### 1.f2

- 问题

* 解答

```
package 数字操作.q7_整数反转.f2;

/**
 * 求余（判断是否溢出有多种方式） o(log(n))
 */
public class Solution {
    public int reverse(int x) {
        int rs = 0;
        while (true) {
            int y = x % 10;
            x = x / 10;
            if (rs * 10 / 10 != rs) {
                return 0;
            }
            rs = rs * 10 + y;
            if (x == 0) {
                break;
            }
        }
        return rs;
    }
}
```

- 问题

* 7.整数反转

给你一个 32 位的有符号整数 x ，
返回将 x 中的数字部分反转后的结果。
如果反转后整数超过 32 位的有符号整数的范围 [−231, 231 − 1] ，就返回 0。
假设环境不允许存储 64 位整数（有符号或无符号）。

- 示例 1：

```
输入：x = 123
输出：321
```

- 示例 2：

```
输入：x = -123
输出：-321
```

- 示例 3：

```
输入：x = 120
输出：21
```

- 示例 4：

```
输入：x = 0
输出：0
```

- 提示：

```
-231 <= x <= 231 - 1
```

- 解答

```

```

#### 3.q258\_各位相加

- 问题

* 258.各位相加

```
给定一个非负整数 num，反复将各个位上的数字相加，
直到结果为一位数。
```

- 示例:

```
输入: 38
输出: 2
解释: 各位相加的过程为：3 + 8 = 11, 1 + 1 = 2。
由于 2 是一位数，所以返回 2。
```

- 进阶:

```
你可以不使用循环或者递归，且在 O(1) 时间复杂度内解决这个问题吗？
```

- 解答

```
package 数字操作.q258_各位相加;

/**
 * 找规律 o(1) xyz=100*x+10*y+z=99*x+9*y+x+y+z
 */
public class Solution {

    public int addDigits(int num) {
        if (num % 9 == 0 && num != 0) {
            num = 9;
        } else {
            num %= 9;
        }
        return num;
    }
}
```

#### 4.q8\_字符串转换整数

- 问题

* 8.字符串转换整数 (atoi)
  请你来实现一个 myAtoi(string s) 函数，
  使其能将字符串转换成一个 32 位有符号整数（类似 C/C++ 中的 atoi 函数）。

函数 myAtoi(string s) 的算法如下：

读入字符串并丢弃无用的前导空格
检查下一个字符（假设还未到字符末尾）为正还是负号，读取该字符（如果有）。
确定最终结果是负数还是正数。 如果两者都不存在，则假定结果为正。
读入下一个字符，直到到达下一个非数字字符或到达输入的结尾。字符串的其余部分将被忽略。
将前面步骤读入的这些数字转换为整数（即，"123" -> 123， "0032" -> 32）。如果没有读入数字，则整数为 0 。
必要时更改符号（从步骤 2 开始）。
如果整数数超过 32 位有符号整数范围 [−231, 231 − 1] ，需要截断这个整数，使其保持在这个范围内。
具体来说，小于 −231 的整数应该被固定为 −231 ，大于 231 − 1 的整数应该被固定为 231 − 1 。
返回整数作为最终结果。
注意：

本题中的空白字符只包括空格字符 ' ' 。
除前导空格或数字后的其余字符串外，请勿忽略 任何其他字符。

- 示例 1：

```
输入：s = "42"
输出：42
解释：加粗的字符串为已经读入的字符，插入符号是当前读取的字符。
第 1 步："42"（当前没有读入字符，因为没有前导空格）
         ^
第 2 步："42"（当前没有读入字符，因为这里不存在 '-' 或者 '+'）
         ^
第 3 步："42"（读入 "42"）
           ^
解析得到整数 42 。
由于 "42" 在范围 [-231, 231 - 1] 内，最终结果为 42 。
```

- 示例 2：

```
输入：s = "   -42"
输出：-42
解释：
第 1 步："   -42"（读入前导空格，但忽视掉）
            ^
第 2 步："   -42"（读入 '-' 字符，所以结果应该是负数）
             ^
第 3 步："   -42"（读入 "42"）
               ^
解析得到整数 -42 。
由于 "-42" 在范围 [-231, 231 - 1] 内，最终结果为 -42 。
```

- 示例 3：

```
输入：s = "4193 with words"
输出：4193
解释：
第 1 步："4193 with words"（当前没有读入字符，因为没有前导空格）
         ^
第 2 步："4193 with words"（当前没有读入字符，因为这里不存在 '-' 或者 '+'）
         ^
第 3 步："4193 with words"（读入 "4193"；由于下一个字符不是一个数字，所以读入停止）
             ^
解析得到整数 4193 。
由于 "4193" 在范围 [-231, 231 - 1] 内，最终结果为 4193 。
```

- 示例 4：

```
输入：s = "words and 987"
输出：0
解释：
第 1 步："words and 987"（当前没有读入字符，因为没有前导空格）
         ^
第 2 步："words and 987"（当前没有读入字符，因为这里不存在 '-' 或者 '+'）
         ^
第 3 步："words and 987"（由于当前字符 'w' 不是一个数字，所以读入停止）
         ^
解析得到整数 0 ，因为没有读入任何数字。
由于 0 在范围 [-231, 231 - 1] 内，最终结果为 0 。
```

- 示例 5：

```
输入：s = "-91283472332"
输出：-2147483648
解释：
第 1 步："-91283472332"（当前没有读入字符，因为没有前导空格）
         ^
第 2 步："-91283472332"（读入 '-' 字符，所以结果应该是负数）
          ^
第 3 步："-91283472332"（读入 "91283472332"）
                     ^
解析得到整数 -91283472332 。
由于 -91283472332 小于范围 [-231, 231 - 1] 的下界，最终结果被截断为 -231 = -2147483648 。
```

- 提示：

```
0 <= s.length <= 200
s 由英文字母（大写和小写）、数字（0-9）、' '、'+'、'-' 和 '.' 组成
```

- 解答

```
package 数字操作.q8_字符串转换整数;

/**
 * o(n) 重点还是判断溢出
 */
public class Solution {

    public int myAtoi(String str) {
        str = str.trim();
        if (str.length() < 1) {
            return 0;
        }
        boolean negative = false;
        if (str.charAt(0) == '-') {
            negative = true;
            str = str.substring(1);
        } else if (str.charAt(0) == '+') {
            str = str.substring(1);
        }

        int rs = 0;
        for (int i = 0; i < str.length(); i++) {
            char t = str.charAt(i);
            if (Character.isDigit(t)) {
                int temp = rs * 10 - '0' + t;
                if ((temp - t + '0') / 10 != rs || temp < 0) {
                    return negative ? Integer.MIN_VALUE : Integer.MAX_VALUE;
                }
                rs = temp;
            } else {
                break;
            }
        }
        return negative ? -rs : rs;
    }

    public static void main(String[] args) {
        System.out.println(new Solution().myAtoi("2147483648"));
    }
}
```

#### 5.q172\_阶乘后的零

##### 1.f2

- 问题

* 解答

```
package 数字操作.q172_阶乘后的零.f2;

/**
 * 基于方法一，寻找5出现的规律o(log(n))
 */
public class Solution {
    public int trailingZeroes(int n) {
        int count = 0;
        while (n > 0) {
            count += n / 5;
            n = n / 5;
        }
        return count;
    }
}
```

- 问题

* 172.阶乘后的零

给定一个整数 n，返回 n! 结果尾数中零的数量。

- 示例 1:

```
输入: 3
输出: 0
解释: 3! = 6, 尾数中没有零。
```

- 示例 2:

```
输入: 5
输出: 1
解释: 5! = 120, 尾数中有 1 个零.
```

- 说明

```
你算法的时间复杂度应为 O(log n) 。
```

- 解答

```

```

#### 6.q43\_字符串相乘

- 问题

* 43.字符串相乘

```
给定两个以字符串形式表示的非负整数 num1 和 num2，
返回 num1 和 num2 的乘积，它们的乘积也表示为字符串形式。
```

- 示例 1:

```
输入: num1 = "2", num2 = "3"
输出: "6"
```

- 示例 2:

```
输入: num1 = "123", num2 = "456"
输出: "56088"
```

- 说明：

```
num1 和 num2 的长度小于110。
num1 和 num2 只包含数字 0-9。
num1 和 num2 均不以零开头，除非是数字 0 本身。
不能使用任何标准库的大数类型（比如 BigInteger）
或直接将输入转换为整数来处理。
```

- 解答

```
package 数字操作.q43_字符串相乘;

/**
 * o(n) 可基于乘数某位与被乘数某位相乘产生结果的位置的规律优化
 */
class Solution {

    public String multiply(String num1, String num2) {
        if (num1.equals("0") || num2.equals("0")) {
            return "0";
        }
        String res = "0";

        for (int i = num2.length() - 1; i >= 0; i--) {
            int carry = 0;
            StringBuilder temp = new StringBuilder();
            for (int j = 0; j < num2.length() - 1 - i; j++) {
                temp.append(0);
            }
            int n2 = num2.charAt(i) - '0';

            for (int j = num1.length() - 1; j >= 0 || carry != 0; j--) {
                int n1 = j < 0 ? 0 : num1.charAt(j) - '0';
                int product = (n1 * n2 + carry) % 10;
                temp.append(product);
                carry = (n1 * n2 + carry) / 10;
            }
            res = addStrings(res, temp.reverse().toString());
        }
        return res;
    }

    public String addStrings(String num1, String num2) {
        StringBuilder builder = new StringBuilder();
        int carry = 0;
        for (int i = num1.length() - 1, j = num2.length() - 1;
             i >= 0 || j >= 0 || carry != 0;
             i--, j--) {
            int x = i < 0 ? 0 : num1.charAt(i) - '0';
            int y = j < 0 ? 0 : num2.charAt(j) - '0';
            int sum = (x + y + carry) % 10;
            builder.append(sum);
            carry = (x + y + carry) / 10;
        }
        return builder.reverse().toString();
    }
}
```

### 5.q648\_单词替换

- 问题

* 解答

```
package q648_单词替换;

import java.util.List;

/**
 * 构建字典树（前缀树）o(n)
 */
class Solution {
    public String replaceWords(List<String> roots, String sentence) {
        TrieNode trie = new TrieNode();
        for (String root : roots) {
            TrieNode cur = trie;
            for (char letter : root.toCharArray()) {
                if (cur.children[letter - 'a'] == null) {
                    cur.children[letter - 'a'] = new TrieNode();
                }
                cur = cur.children[letter - 'a'];
            }
            cur.word = root;
        }

        StringBuilder ans = new StringBuilder();

        for (String word : sentence.split(" ")) {
            if (ans.length() > 0) {
                ans.append(" ");
            }

            TrieNode cur = trie;
            for (char letter : word.toCharArray()) {
                if (cur.children[letter - 'a'] == null || cur.word != null) {
                    break;
                }
                cur = cur.children[letter - 'a'];
            }
            ans.append(cur.word != null ? cur.word : word);
        }
        return ans.toString();
    }
}

class TrieNode {
    TrieNode[] children;
    String word;

    TrieNode() {
        children = new TrieNode[26];
    }
}
```

### 6.栈相关

#### 1.q20\_有效的括号

- 问题

* 20.有效的括号

给定一个只包括 '('，')'，'{'，'}'，'['，']' 的字符串 s ，
判断字符串是否有效。
有效字符串需满足：

左括号必须用相同类型的右括号闭合。
左括号必须以正确的顺序闭合。

- 示例 1：

```
输入：s = "()"
输出：true
```

- 示例 2：

```
输入：s = "()[]{}"
输出：true
```

- 示例 3：

```
输入：s = "(]"
输出：false
```

- 示例 4：

```
输入：s = "([)]"
输出：false
```

- 示例 5：

```
输入：s = "{[]}"
输出：true
```

- 提示：

```
1 <= s.length <= 104
s 仅由括号 '()[]{}' 组成
```

- 解答

```
package 栈相关.q20_有效的括号;

import java.util.Stack;

/**
 * 利用栈 o(n)
 */
public class Solution {
    public boolean isValid(String s) {
        Stack<Character> stack = new Stack<>();
        for (int i = 0; i < s.length(); i++) {
            char t = s.charAt(i);
            if (t == '(' || t == '[' || t == '{') {
                stack.push(t);
            } else {
                if (stack.empty()) {
                    return false;
                }
                if (t == ')') {
                    if (stack.pop() != '(') {
                        return false;
                    }
                } else if (t == ']') {
                    if (stack.pop() != '[') {
                        return false;
                    }
                } else {
                    if (stack.pop() != '{') {
                        return false;
                    }
                }
            }
        }
        return stack.empty();
    }

    public static void main(String[] args) {
        System.out.println(new Solution().isValid("()"));
    }
}
```

#### 2.q224\_基本计算器

##### 1.f2

- 问题

* 解答

```
package 栈相关.q224_基本计算器.f2;

import java.util.Stack;

/**
 * 单栈 拆分递归思想 o(n)
 */
public class Solution {

    public int evaluateExpr(Stack<Object> stack) {
        int res = 0;

        if (!stack.empty()) {
            res = (int) stack.pop();
        }

        while (!stack.empty() && !((char) stack.peek() == ')')) {
            char sign = (char) stack.pop();
            if (sign == '+') {
                res += (int) stack.pop();
            } else {
                res -= (int) stack.pop();
            }
        }
        return res;
    }

    public int calculate(String s) {
        int operand = 0;
        int n = 0;
        Stack<Object> stack = new Stack<Object>();

        for (int i = s.length() - 1; i >= 0; i--) {
            char ch = s.charAt(i);
            if (Character.isDigit(ch)) {
                operand = (int) Math.pow(10, n) * (int) (ch - '0') + operand;
                n += 1;
            } else if (ch != ' ') {
                if (n != 0) {
                    stack.push(operand);
                    n = 0;
                    operand = 0;
                }
                if (ch == '(') {
                    int res = evaluateExpr(stack);
                    stack.pop();
                    stack.push(res);
                } else {
                    stack.push(ch);
                }
            }
        }

        if (n != 0) {
            stack.push(operand);
        }
        return evaluateExpr(stack);
    }
}
```

- 问题

* 224.基本计算器

给你一个字符串表达式 s ，
请你实现一个基本计算器来计算并返回它的值。

- 示例 1：

```
输入：s = "1 + 1"
输出：2
```

- 示例 2：

```
输入：s = " 2-1 + 2 "
输出：3
```

- 示例 3：

```
输入：s = "(1+(4+5+2)-3)+(6+8)"
输出：23
```

- 提示：

```
1 <= s.length <= 3 * 105
s 由数字、'+'、'-'、'('、')'、和 ' ' 组成
s 表示一个有效的表达式
```

- 解答

```

```

#### 3.q32\_最长有效括号

- 问题

* 32. 最长有效括号

给你一个只包含 '(' 和 ')' 的字符串，
找出最长有效（格式正确且连续）括号子串的长度。

- 示例 1：

```
输入：s = "(()"
输出：2
解释：最长有效括号子串是 "()"
```

- 示例 2：

```
输入：s = ")()())"
输出：4
解释：最长有效括号子串是 "()()"
```

- 示例 3：

```
输入：s = ""
输出：0
```

- 提示：

```
0 <= s.length <= 3 * 104
s[i] 为 '(' 或 ')'
```

- 解答

```
package 栈相关.q32_最长有效括号;

import java.util.Stack;

/**
 * 利用索引栈 o(n)
 */
public class Solution {

    public int longestValidParentheses(String s) {
        if (s == null || s.length() < 2) {
            return 0;
        }

        int maxLen = 0;
        Stack<Integer> stack = new Stack<>();
        stack.push(-1);
        for (int i = 0; i < s.length(); i++) {
            char temp = s.charAt(i);
            if (temp == '(') {
                stack.push(i);
            } else {
                stack.pop();
                if (stack.empty()) {
                    stack.push(i);
                } else {
                    maxLen = Math.max(maxLen, i - stack.peek());
                }
            }
        }

        return maxLen;
    }

    public static void main(String[] args) {

        System.out.println(new Solution().longestValidParentheses(")()())"));
    }
}
```

#### 4.q232\_用栈实现队列

##### 1.含有最大值的队列

- 问题

* 解答

```

```

##### 2.f2

- 问题

* 解答

```

```

- 问题

* 232.用栈实现队列

请你仅使用两个栈实现先入先出队列。
队列应当支持一般队列支持的所有操作（push、pop、peek、empty）：

实现 MyQueue 类：

void push(int x) 将元素 x 推到队列的末尾
int pop() 从队列的开头移除并返回元素
int peek() 返回队列开头的元素
boolean empty() 如果队列为空，返回 true ；否则，返回 false

- 说明：

你只能使用标准的栈操作 —— 也就是只有 push to top, peek/pop from top, size, 和 is empty 操作是合法的。
你所使用的语言也许不支持栈。你可以使用 list 或者 deque（双端队列）来模拟一个栈，只要是标准的栈操作即可。

- 进阶：

你能否实现每个操作均摊时间复杂度为 O(1) 的队列？换句话说，执行 n 个操作的总时间复杂度为 O(n) ，即使其中一个操作可能花费较长时间。

- 示例：

```
输入：
["MyQueue", "push", "push", "peek", "pop", "empty"]
[[], [1], [2], [], [], []]
输出：
[null, null, null, 1, 1, false]

解释：
MyQueue myQueue = new MyQueue();
myQueue.push(1); // queue is: [1]
myQueue.push(2); // queue is: [1, 2] (leftmost is front of the queue)
myQueue.peek(); // return 1
myQueue.pop(); // return 1, queue is [2]
myQueue.empty(); // return false

```

- 提示：

```
1 <= x <= 9
最多调用 100 次 push、pop、peek 和 empty
假设所有操作都是有效的 （例如，一个空的队列不会调用 pop 或者 peek 操作）
```

- 解答

```

```

#### 5.q155\_最小栈

- 问题

* 155.最小栈

设计一个支持 push ，pop ，top 操作，
并能在常数时间内检索到最小元素的栈。

push(x) —— 将元素 x 推入栈中。
pop() —— 删除栈顶的元素。
top() —— 获取栈顶元素。
getMin() —— 检索栈中的最小元素。

- 示例:

```
输入：
["MinStack","push","push","push","getMin","pop","top","getMin"]
[[],[-2],[0],[-3],[],[],[],[]]

输出：
[null,null,null,null,-3,null,0,-2]

解释：
MinStack minStack = new MinStack();
minStack.push(-2);
minStack.push(0);
minStack.push(-3);
minStack.getMin();   --> 返回 -3.
minStack.pop();
minStack.top();      --> 返回 0.
minStack.getMin();   --> 返回 -2.
```

- 提示：

pop、top 和 getMin 操作总是在 非空栈 上调用。

- 解答

```

```

#### 6.q316\_去除重复字母

- 问题

* 316.去除重复字母

给你一个字符串 s ，请你去除字符串中重复的字母，
使得每个字母只出现一次。
需保证 返回结果的字典序最小（要求不能打乱其他字符的相对位置）。

注意：该题与 1081 https://leetcode-cn.com/problems/smallest-subsequence-of-distinct-characters 相同

- 示例 1：

```
输入：s = "bcabc"
输出："abc"
```

- 示例 2：

```
输入：s = "cbacdcbc"
输出："acdb"
```

- 提示：

```
1 <= s.length <= 104
s 由小写英文字母组成
```

- 解答

```
package 栈相关.q316_去除重复字母;

import java.util.Stack;

/**
 * 栈操作 o(n*log(n))
 */
public class Solution {

    public String removeDuplicateLetters(String s) {
        Stack<Character> stack = new Stack<>();
        for (int i = 0; i < s.length(); i++) {
            Character c = s.charAt(i);
            if (stack.contains(c)) {
                continue;
            }
            while (!stack.isEmpty() && stack.peek() > c && s.indexOf(stack.peek(), i) != -1) {
                stack.pop();
            }
            stack.push(c);
        }
        String rs = "";
        for (int i = 0; i < stack.size(); i++) {
            rs += stack.get(i);
        }
        return rs;
    }
}
```

### 7.字符串操作

#### 1.q14\_最长公共前缀

- 问题

* 解答

```
package 字符串操作.q14_最长公共前缀;

/**
 * 水平扫描 o(n)
 */
public class Solution {
    public String longestCommonPrefix(String[] strs) {
        if (strs.length == 0) {
            return "";
        }
        if (strs.length == 1) {
            return strs[0];
        }
        String pre = "";
        int i = 0;
        while (true) {
            if (strs[0].length() == i) {
                return pre;
            }
            char temp = strs[0].charAt(i);
            for (int k = 1; k < strs.length; k++) {
                if (strs[k].length() == i || temp != strs[k].charAt(i)) {
                    return pre;
                }
            }
            pre += temp;
            i++;
        }
    }

    public static void main(String[] args) {
        String[] s = new String[]{"c", "c"};
        System.out.println(new Solution().longestCommonPrefix(s));
    }
}
```

#### 2.q763\_划分字母区间

- 问题

* 解答

```
package 字符串操作.q763_划分字母区间;

import java.util.ArrayList;
import java.util.List;

/**
 * 先存储每个字母最后出现的位置，最后遍历一次 o(n)
 */
public class Solution {

    public List<Integer> partitionLabels(String S) {
        int[] last = new int[26];
        for (int i = 0; i < S.length(); ++i) {
            last[S.charAt(i) - 'a'] = i;
        }
        int j = 0, anchor = 0;
        List<Integer> ans = new ArrayList<>();
        for (int i = 0; i < S.length(); ++i) {
            j = Math.max(j, last[S.charAt(i) - 'a']);
            if (i == j) {
                ans.add(i - anchor + 1);
                anchor = i + 1;
            }
        }
        return ans;
    }

    public static void main(String[] args) {
        new Solution().partitionLabels("abccaddbeffe");
    }
}
```

#### 3.q6_Z 字形变换

- 问题

* 解答

```
package 字符串操作.q6_Z字形变换;

import java.util.ArrayList;
import java.util.List;

/**
 * o(n) 可用一boolean变量代替求余操作
 */
public class Solution {
    public String convert(String s, int numRows) {
        if (numRows == 1) {
            return s;
        }
        int len = s.length();
        int col = 0;
        int n = 0;
        List<StringBuffer> list = new ArrayList<>();
        for (int i = 0; i < numRows; i++) {
            StringBuffer temp = new StringBuffer();
            list.add(temp);
        }
        while (n < len) {
            int y = col % (numRows - 1);
            if (y == 0) {
                for (int i = 0; i < numRows && n < len; i++) {
                    list.get(i).append(s.charAt(n));
                    n++;
                }
            } else {
                list.get(numRows - 1 - y).append(s.charAt(n));
                n++;
            }
            col++;
        }
        String rs = "";
        for (int i = 0; i < list.size(); i++) {
            rs += list.get(i).toString();
        }
        return rs;
    }

    public static void main(String[] args) {
        System.out.println(new Solution().convert("LEETCODEISHIRING", 4));
    }
}
```

### 8.回溯法

#### 1.q10\_正则表达式匹配

- 问题

* 解答

```
package 回溯法.q10_正则表达式匹配;

/**
 * 回溯法 对于*字符，可以直接忽略模式串中这一部分，或者删除匹配串的第一个字符，前提是它能够匹配模式串当前位置字符，即 pattern[0]。如果两种操作中有任何一种使得剩下的字符串能匹配，那么初始时，匹配串和模式串就可以被匹配。
 */
public class Solution {
    public boolean isMatch(String text, String pattern) {
        if (pattern.isEmpty()){
            return text.isEmpty();
        }
        boolean firstMatch = (!text.isEmpty() &&
                (pattern.charAt(0) == text.charAt(0) || pattern.charAt(0) == '.'));

        if (pattern.length() >= 2 && pattern.charAt(1) == '*') {
            return (isMatch(text, pattern.substring(2)) ||
                    (firstMatch && isMatch(text.substring(1), pattern)));
        } else {
            return firstMatch && isMatch(text.substring(1), pattern.substring(1));
        }
    }

    public static void main(String[] args) {
        System.out.println(new Solution().isMatch("aaa", "a*a"));
    }
}
```

#### 2.q22\_括号生成

##### 1.f2

- 问题

* 解答

```
package 回溯法.q22_括号生成.f2;

import java.util.ArrayList;
import java.util.List;

/**
 * 回溯法 o((4^n)/(n^1/2))
 */
public class Solution {

    public List<String> generateParenthesis(int n) {
        List<String> ans = new ArrayList();
        backtrack(ans, "", 0, 0, n);
        return ans;
    }

    public void backtrack(List<String> ans, String cur, int open, int close, int max) {
        if (cur.length() == max * 2) {
            ans.add(cur);
            return;
        }

        if (open < max) {
            backtrack(ans, cur + "(", open + 1, close, max);
        }
        if (close < open) {
            backtrack(ans, cur + ")", open, close + 1, max);
        }
    }

    public static void main(String[] args) {
        System.out.println(new Solution().generateParenthesis(3));
    }
}
```

##### 2.f1

- 问题

* 解答

```
package 回溯法.q22_括号生成.f1;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * 暴力法 o(2^2n*n)
 */
public class Solution {

    public boolean isValid(String s) {
        Stack<Character> stack = new Stack<>();
        for (int i = 0; i < s.length(); i++) {
            char t = s.charAt(i);
            if (t == '(') {
                stack.push(t);
            } else {
                if (stack.empty() || stack.pop() != '(') {
                    return false;
                }
            }
        }
        return stack.empty();
    }

    public List<String> generateParenthesis(int n) {
        List<String> rs = new ArrayList<>();

        if (n < 1) {
            return rs;
        }
        String root = "(";
        rs.add(root);
        for (int k = 0; k < 2 * n - 1; k++) {
            List<String> tempList = new ArrayList<>();
            for (int i = 0; i < rs.size(); i++) {
                String temp = rs.get(i);
                tempList.add(temp + "(");
                tempList.add(temp + ")");
            }
            rs.clear();
            rs.addAll(tempList);
        }
        rs.removeIf(s -> !isValid(s));
        return rs;
    }

    public static void main(String[] args) {
        new Solution().generateParenthesis(3);
    }
}
```

#### 3.q40\_组合总和 2

- 问题

* 解答

```
package 回溯法.q40_组合总和2;

import java.util.*;

/**
 * 回溯法 O(n*log(n))
 */
class Solution {

    public List<List<Integer>> combinationSum2(int[] candidates, int target) {
        List<List<Integer>> res = new ArrayList<>();
        if (candidates.length == 0) {
            return res;
        }
        Arrays.sort(candidates);
        helper(candidates, target, 0, new LinkedList<>(), res);
        return res;
    }

    public void helper(int[] candidates, int target, int start, LinkedList<Integer> stack, List<List<Integer>> res) {
        if (start > candidates.length) {
            return;
        }
        if (target == 0 && !stack.isEmpty()) {
            List<Integer> item = new ArrayList<>(stack);
            res.add(item);
        }
        HashSet<Integer> set = new HashSet<>();
        for (int i = start; i < candidates.length; ++i) {
            if (!set.contains(candidates[i]) && target >= candidates[i]) {
                stack.push(candidates[i]);
                helper(candidates, target - candidates[i], i + 1, stack, res);
                stack.pop();
                set.add(candidates[i]);
            }
        }
    }

    public static void main(String[] args) {
        new Solution().combinationSum2(new int[]{10, 1, 2, 7, 6, 1, 5}, 8);
    }
}
```

#### 4.q46\_全排列

##### 1.f2

- 问题

* 解答

```
package 回溯法.q46_全排列.f2;

import java.util.ArrayList;
import java.util.List;

/**
 * 回溯法(DFS深度优先遍历) o(n*n!)
 */
public class Solution {

    public List<List<Integer>> permute(int[] nums) {
        int len = nums.length;

        List<List<Integer>> res = new ArrayList<>();

        if (len == 0) {
            return res;
        }

        boolean[] used = new boolean[len];
        List<Integer> path = new ArrayList<>();

        dfs(nums, len, 0, path, used, res);
        return res;
    }

    private void dfs(int[] nums, int len, int depth,
                     List<Integer> path, boolean[] used,
                     List<List<Integer>> res) {
        if (depth == len) {
            res.add(new ArrayList<>(path));
            return;
        }

        for (int i = 0; i < len; i++) {
            if (!used[i]) {
                path.add(nums[i]);
                used[i] = true;
                dfs(nums, len, depth + 1, path, used, res);
                // 状态重置，是从深层结点回到浅层结点的过程，代码在形式上和递归之前是对称的
                used[i] = false;
                path.remove(depth);
            }
        }
    }

    public static void main(String[] args) {
        int[] nums = {1, 2, 3};
        Solution solution = new Solution();
        List<List<Integer>> lists = solution.permute(nums);
    }
}
```

##### 2.f1

- 问题

* 解答

```
package 回溯法.q46_全排列.f1;

import java.util.ArrayList;
import java.util.List;

/**
 * 插队法 o((n-1)!+(n-2)!+···+2!+1!)
 */
public class Solution {
    public List<List<Integer>> fc(List<List<Integer>> nums, int c) {
        List<List<Integer>> result = new ArrayList<>();
        for (int i = 0; i < nums.size(); i++) {
            for (int j = 0; j <= nums.get(i).size(); j++) {
                List<Integer> temp = new ArrayList<>(nums.get(i));
                temp.add(j, c);
                result.add(temp);
            }
        }
        return result;
    }

    public List<List<Integer>> permute(int[] nums) {
        List<List<Integer>> result = new ArrayList<>();
        if (nums.length == 0) {
            return result;
        }
        List<Integer> to = new ArrayList<>();
        to.add(nums[0]);
        result.add(to);
        for (int i = 1; i < nums.length; i++) {
            result = fc(result, nums[i]);
        }
        System.out.println(result);
        return result;
    }

    public static void main(String[] args) {
        new Solution().permute(new int[]{1, 2, 3});
        //4—>3！+2！+1！
    }
}
```

### 9.二叉搜索树相关

#### 1.q701\_二叉搜索树中的插入操作

- 问题

### 701. 二叉搜索树中的插入操作

给定二叉搜索树（BST）的根节点和要插入树中的值，将值插入二叉搜索树。
返回插入后二叉搜索树的根节点。 输入数据 保证 ，新值和原始二叉搜索树中的任意节点值都不同。

注意，可能存在多种有效的插入方式，
只要树在插入后仍保持为二叉搜索树即可。 你可以返回 任意有效的结果 。

### 示例 1：

```
输入：root = [4,2,7,1,3], val = 5
输出：[4,2,7,1,3,5]
解释：另一个满足题目要求可以通过的树是：
```

### 示例 2：

```
输入：root = [40,20,60,10,30,50,70], val = 25
输出：[40,20,60,10,30,50,70,null,null,25]
```

### 示例 3：

```
输入：root = [4,2,7,1,3,null,null,null,null,null,null], val = 5
输出：[4,2,7,1,3,5]
```

### 提示：

```
给定的树上的节点数介于 0 和 10^4 之间
每个节点都有一个唯一整数值，取值范围从 0 到 10^8
-10^8 <= val <= 10^8
新值和原始二叉搜索树中的任意节点值都不同
```

- 解答

```
package 二叉搜索树相关.q701_二叉搜索树中的插入操作;

/**
 * 递归（大于插入右子树，小于插入左子树） o(n)
 */
public class Solution {

    public TreeNode insertIntoBST(TreeNode root, int val) {
        TreeNode node = new TreeNode(val);
        TreeNode temp = root;
        if (root == null) {
            return node;
        }
        if (val >= root.val) {
            if (root.right == null) {
                root.right = node;
            } else {
                insertIntoBST(root.right, val);
            }
        } else {
            if (root.left == null) {
                root.left = node;
            } else {
                insertIntoBST(root.left, val);
            }
        }
        return temp;
    }
}
```

#### 2.q450\_删除二叉搜索树中的节点

- 问题

### 450. 删除二叉搜索树中的节点

给定一个二叉搜索树的根节点 root 和一个值 key，删除二叉搜索树中的 key 对应的节点，
并保证二叉搜索树的性质不变。返回二叉搜索树（有可能被更新）的根节点的引用。

一般来说，删除节点可分为两个步骤：

首先找到需要删除的节点；
如果找到了，删除它。
说明： 要求算法时间复杂度为 O(h)，h 为树的高度。

### 示例:

```
root = [5,3,6,2,4,null,7]
key = 3

    5
   / \
  3   6
 / \   \
2   4   7

给定需要删除的节点值是 3，所以我们首先找到 3 这个节点，然后删除它。

一个正确的答案是 [5,4,6,2,null,null,7], 如下图所示。

    5
   / \
  4   6
 /     \
2       7

另一个正确答案是 [5,2,6,null,4,null,7]。

    5
   / \
  2   6
   \   \
    4   7
```

- 解答

```
package 二叉搜索树相关.q450_删除二叉搜索树中的节点;

/**
 * 用前驱节点代替待删除的节点 o(log(n))
 */
public class Solution {
    public TreeNode deleteNode(TreeNode root, int key) {
        if (root == null) {
            return null;
        }

        if (key < root.val) {
            root.left = deleteNode(root.left, key);
            return root;
        }
        if (key > root.val) {
            root.right = deleteNode(root.right, key);
            return root;
        }

        if (root.left == null) {
            return root.right;
        }

        if (root.right == null) {
            return root.left;
        }
        //求前驱节点
        TreeNode predecessor = maximum(root.left);
        TreeNode predecessorCopy = new TreeNode(predecessor.val);
        //先remove再衔接
        predecessorCopy.left = removeMax(root.left);
        predecessorCopy.right = root.right;
        root.left = null;
        root.right = null;
        return predecessorCopy;
    }

    /**
     * 两种情况，一种 node.right == null 说明前驱节点为删除节点的左节点，否则为删除节点的右侧叶节点（对应maximum(root.left)）
     *
     * @param node
     * @return
     */
    private TreeNode removeMax(TreeNode node) {
        if (node.right == null) {
            return node.left;
        }
        node.right = removeMax(node.right);
        return node;
    }

    private TreeNode maximum(TreeNode node) {
        if (node.right == null) {
            return node;
        }
        return maximum(node.right);
    }

    public static void main(String[] args) {
        TreeNode root = new TreeNode(1);
//        TreeNode n1 = new TreeNode(3);
        TreeNode n2 = new TreeNode(2);
//        TreeNode n3 = new TreeNode(2);
//        TreeNode n4 = new TreeNode(4);
//        TreeNode n5 = new TreeNode(7);
//
//        root.left = n1;
        root.right = n2;
//        n1.left = n3;
//        n1.right = n4;
//        n2.right = n5;

        new Solution().deleteNode(root, 1);
    }
}
```

#### 3.q98\_验证二叉搜索树

##### 1.f3

- 问题

* 解答

```
package 二叉搜索树相关.q98_验证二叉搜索树.f3;

import java.util.LinkedList;
import java.util.Queue;

/**
 * 层序遍历迭代法判断上下界 o(n)
 */
public class Solution {
    public boolean isValidBST(TreeNode root) {
        if (root == null) {
            return true;
        }
        Queue<TreeNode> queue = new LinkedList<>();
        queue.add(root);

        Queue<Integer> min = new LinkedList<>();
        Queue<Integer> max = new LinkedList<>();
        while (!queue.isEmpty()) {
            TreeNode temp = queue.poll();
            Integer maxt = max.poll();
            Integer mint = min.poll();

            if (mint != null && temp.val <= mint) {
                return false;
            }
            if (maxt != null && temp.val >= maxt) {
                return false;
            }

            if (temp.left != null) {
                min.add(mint);
                max.add(temp.val);
                queue.add(temp.left);
            }

            if (temp.right != null) {
                max.add(maxt);
                min.add(temp.val);
                queue.add(temp.right);
            }
        }
        return true;
    }

    public static void main(String[] args) {
        TreeNode root = new TreeNode(10);
        TreeNode n1 = new TreeNode(5);
        TreeNode n2 = new TreeNode(15);
        root.left = n1;
        root.right = n2;
        TreeNode n3 = new TreeNode(6);
        TreeNode n4 = new TreeNode(20);
        n2.left = n3;
        n2.right = n4;
        System.out.println(new Solution().isValidBST(root));
    }
}
```

##### 2.f2

- 问题

* 解答

```
package 二叉搜索树相关.q98_验证二叉搜索树.f2;

/**
 * 寻找上下界递归 o(n)
 */
public class Solution {
    public boolean valid(TreeNode root, Integer min, Integer max) {
        if (root == null) {
            return true;
        }
        int val = root.val;

        if (min != null && val <= min) {
            return false;
        }
        if (max != null && val >= max) {
            return false;
        }

        if (!valid(root.left, min, val)) {
            return false;
        }
        if (!valid(root.right, val, max)) {
            return false;
        }
        return true;
    }

    public boolean isValidBST(TreeNode root) {
        return valid(root, null, null);
    }
    public static void main(String[] args) {
        TreeNode root = new TreeNode(10);
        TreeNode n1 = new TreeNode(5);
        TreeNode n2 = new TreeNode(15);
        root.left = n1;
        root.right = n2;
        TreeNode n3 = new TreeNode(6);
        TreeNode n4 = new TreeNode(20);
        n2.left = n3;
        n2.right = n4;
        System.out.println(new Solution().isValidBST(root));
    }
}
```

##### 3.f5

- 问题

* 解答

```
package 二叉搜索树相关.q98_验证二叉搜索树.f5;

import java.util.Deque;
import java.util.LinkedList;

/**
 * 作者：LeetCode-Solution
 * 链接：https://leetcode-cn.com/problems/validate-binary-search-tree/solution/yan-zheng-er-cha-sou-suo-shu-by-leetcode-solution/
 * 来源：力扣（LeetCode）
 * 著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。
 *
 * 思路和算法
 *
 * 基于方法一中提及的性质，我们可以进一步知道二叉搜索树「中序遍历」得到的值构成的序列一定是升序的，
 * 这启示我们在中序遍历的时候实时检查当前节点的值是否大于前一个中序遍历到的节点的值即可。
 * 如果均大于说明这个序列是升序的，整棵树是二叉搜索树，否则不是，下面的代码我们使用栈来模拟中序遍历的过程。
 *
 * 可能由读者不知道中序遍历是什么，我们这里简单提及一下，中序遍历是二叉树的一种遍历方式，它先遍历左子树，再遍历根节点，最后遍历右子树。
 * 而我们二叉搜索树保证了左子树的节点的值均小于根节点的值，根节点的值均小于右子树的值，因此中序遍历以后得到的序列一定是升序序列。
 *
 *
 * 复杂度分析
 *
 * 时间复杂度 : O(n)O(n)，其中 nn 为二叉树的节点个数。二叉树的每个节点最多被访问一次，因此时间复杂度为 O(n)O(n)。
 *
 * 空间复杂度 : O(n)O(n)，其中 nn 为二叉树的节点个数。栈最多存储 nn 个节点，因此需要额外的 O(n)O(n) 的空间。
 *
 *
 */
class Solution {
    public boolean isValidBST(TreeNode root) {
        Deque<TreeNode> stack = new LinkedList<TreeNode>();
        double inorder = -Double.MAX_VALUE;

        while (!stack.isEmpty() || root != null) {
            while (root != null) {
                stack.push(root);
                root = root.left;
            }
            root = stack.pop();
              // 如果中序遍历得到的节点的值小于等于前一个 inorder，说明不是二叉搜索树
            if (root.val <= inorder) {
                return false;
            }
            inorder = root.val;
            root = root.right;
        }
        return true;
    }
    public static void main(String[] args) {
        TreeNode root = new TreeNode(10);
        TreeNode n1 = new TreeNode(5);
        TreeNode n2 = new TreeNode(15);
        root.left = n1;
        root.right = n2;
        TreeNode n3 = new TreeNode(12);
        TreeNode n4 = new TreeNode(20);
        n2.left = n3;
        n2.right = n4;
        System.out.println(new Solution().isValidBST(root));
    }
}
```

- 问题

### 98. 验证二叉搜索树

给定一个二叉树，判断其是否是一个有效的二叉搜索树。

假设一个二叉搜索树具有如下特征：

节点的左子树只包含小于当前节点的数。
节点的右子树只包含大于当前节点的数。
所有左子树和右子树自身必须也是二叉搜索树。

### 示例

```
示例 1:
输入:
    2
   / \
  1   3
输出: true


示例 2:

输入:
    5
   / \
  1   4
     / \
    3   6
输出: false
解释: 输入为: [5,1,4,null,null,3,6]。
     根节点的值为 5 ，但是其右子节点值为 4 。
```

- 解答

```

```

### 10.atool

- 问题

* 解答

```

```

### 11.q56\_合并区间

- 问题

* 解答

```
package q56_合并区间;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * 先根据start进行排序之后merge o(n*log(n))
 */
class Solution {
    public int[][] merge(int[][] intervals) {
        if(intervals.length <= 1){
            return intervals;
        }

        Arrays.sort(intervals, Comparator.comparingInt(arr -> arr[0]));

        int[] currInterval = intervals[0];
        List<int[]> resArr = new ArrayList<>();
        resArr.add(currInterval);

        for(int[] interval: intervals){
            int currEnd = currInterval[1];

            int nextBegin = interval[0];
            int nextEnd = interval[1];

            if(currEnd >= nextBegin){
                currInterval[1] = Math.max(currEnd, nextEnd);
            } else{
                currInterval = interval;
                resArr.add(currInterval);
            }
        }

        return resArr.toArray(new int[resArr.size()][]);
    }
}
```

### 12.快慢指针遍历

#### 1.q202\_快乐数

- 问题

* 202.快乐数

编写一个算法来判断一个数 n 是不是快乐数。
「快乐数」定义为：
对于一个正整数，每一次将该数替换为它每个位置上的数字的平方和。
然后重复这个过程直到这个数变为 1，
也可能是 无限循环 但始终变不到 1。
如果 可以变为 1，那么这个数就是快乐数。
如果 n 是快乐数就返回 true ；不是，则返回 false 。

- 示例 1：

```
输入：19
输出：true
解释：
12 + 92 = 82
82 + 22 = 68
62 + 82 = 100
12 + 02 + 02 = 1
```

- 示例 2：

```
输入：n = 2
输出：false
```

- 提示：

```
1 <= n <= 231 - 1
```

- 解答

```
package 快慢指针遍历.q202_快乐数;

/**
 * 快慢指针，思想同q141判断是否有环，用快慢指针找出循环终止条件 o(n)
 */
public class Solution {

    private int bitSquareSum(int n) {
        int sum = 0;
        while (n > 0) {
            int bit = n % 10;
            sum += bit * bit;
            n = n / 10;
        }
        return sum;
    }

    public boolean isHappy(int n) {
        int slow = n;
        int fast = n;
        do {
            slow = bitSquareSum(slow);
            fast = bitSquareSum(fast);
            fast = bitSquareSum(fast);
        } while (slow != fast);

        return slow == 1;
    }
}
```

#### 2.q876\_链表的中间结点

- 问题

* 876.链表的中间结点

```
给定一个头结点为 head 的非空单链表，
返回链表的中间结点。
如果有两个中间结点，则返回第二个中间结点。
```

- 示例 1：

```
输入：[1,2,3,4,5]
输出：此列表中的结点 3 (序列化形式：[3,4,5])
返回的结点值为 3 。 (测评系统对该结点序列化表述是 [3,4,5])。
注意，我们返回了一个 ListNode 类型的对象 ans，这样：
ans.val = 3, ans.next.val = 4, ans.next.next.val = 5, 以及 ans.next.next.next = NULL.
```

- 示例 2：

```
输入：[1,2,3,4,5,6]
输出：此列表中的结点 4 (序列化形式：[4,5,6])
由于该列表有两个中间结点，值分别为 3 和 4，我们返回第二个结点。
```

- 提示：

```
给定链表的结点数介于 1 和 100 之间。
```

- 解答

```
package 快慢指针遍历.q876_链表的中间结点;

/**
 * 快慢指针法 o(n)
 */
public class Solution {

    public ListNode middleNode(ListNode head) {
        ListNode slow = head, fast = head;
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
        }
        return slow;
    }
}
```

#### 3.q141\_环形链表

##### 1.f2

- 问题

* 解答

```
package 快慢指针遍历.q141_环形链表.f2;

/**
 * 快慢指针 o(n)
 */
public class Solution {

    public boolean hasCycle(ListNode head) {
        if (head == null || head.next == null) {
            return false;
        }
        ListNode slow = head;
        ListNode fast = head.next;
        while (slow != fast) {
            if (fast == null || fast.next == null) {
                return false;
            }
            slow = slow.next;
            fast = fast.next.next;
        }
        return true;
    }
}
```

- 问题

* 141.环形链表

给定一个链表，判断链表中是否有环。

如果链表中有某个节点，可以通过连续跟踪 next 指针再次到达，则链表中存在环。
为了表示给定链表中的环，我们使用整数 pos 来表示链表尾连接到链表中的位置（索引从 0 开始）。
如果 pos 是 -1，则在该链表中没有环。注意：pos 不作为参数进行传递，仅仅是为了标识链表的实际情况。

如果链表中存在环，则返回 true 。 否则，返回 false 。

- 进阶：

```
你能用 O(1)（即，常量）内存解决此问题吗？
```

- 示例 1：

```
输入：head = [3,2,0,-4], pos = 1
输出：true
解释：链表中有一个环，其尾部连接到第二个节点。
```

- 示例 2：

```
输入：head = [1,2], pos = 0
输出：true
解释：链表中有一个环，其尾部连接到第一个节点。
```

- 示例 3：

```
输入：head = [1], pos = -1
输出：false
解释：链表中没有环。
```

- 提示：

```
链表中节点的数目范围是 [0, 104]
-105 <= Node.val <= 105
pos 为 -1 或者链表中的一个 有效索引 。
```

- 解答

```

```

### 13.堆相关

#### 1.q347\_前 K 个高频元素

- 问题

* 解答

```
package 堆相关.q347_前K个高频元素;

import java.util.*;

/**
 * 利用大根堆（PriorityQueue）实现 o(n*log(k))
 */
class Solution {
    public List<Integer> topKFrequent(int[] nums, int k) {

        HashMap<Integer, Integer> count = new HashMap<>();
        for (int n : nums) {
            count.put(n, count.getOrDefault(n, 0) + 1);
        }

        PriorityQueue<Integer> heap = new PriorityQueue<>(Comparator.comparingInt(count::get));

        for (int n : count.keySet()) {
            heap.add(n);
            if (heap.size() > k) {
                heap.poll();
            }
        }

        List<Integer> topK = new LinkedList<>();
        while (!heap.isEmpty()) {
            topK.add(heap.poll());
        }
        Collections.reverse(topK);
        return topK;
    }
}
```

#### 2.q215\_数组中的第 K 个最大元素

- 问题

### 215. 数组中的第 K 个最大元素

在未排序的数组中找到第 k 个最大的元素。请注意，你需要找的是数组排序后的第 k 个最大的元素，而不是第 k 个不同的元素。

### 示例 1:

```
输入: [3,2,1,5,6,4] 和 k = 2
输出: 5
示例 2:

输入: [3,2,3,1,2,4,5,5,6] 和 k = 4
输出: 4
说明:

你可以假设 k 总是有效的，且 1 ≤ k ≤ 数组的长度。
```

- 解答

```
package 堆相关.q215_数组中的第K个最大元素;

import java.util.PriorityQueue;

/**
 * 利用大根堆实现 o(n*log(k))
 */
public class Solution {

    public int findKthLargest(int[] nums, int k) {
        PriorityQueue<Integer> heap =
                new PriorityQueue<>((n1, n2) -> n1 - n2);

        for (int n: nums) {
            heap.add(n);
            if (heap.size() > k){
                heap.poll();
            }
        }

        return heap.poll();
    }
}
```

### 14.递归

#### 1.q104\_二叉树的最大深度

- 问题

* 104.二叉树的最大深度

给定一个二叉树，找出其最大深度。

二叉树的深度为根节点到最远叶子节点的最长路径上的节点数。

说明: 叶子节点是指没有子节点的节点。

- 示例：
  给定二叉树 [3,9,20,null,null,15,7]，

```
    3
   / \
  9  20
    /  \
   15   7
```

返回它的最大深度 3 。

- 解答

```
package 递归.q104_二叉树的最大深度;

/**
 * 递归 o(n)
 */
public class Solution {

    public int maxDepth(TreeNode root) {
        if (root == null) {
            return 0;
        } else {
            int leftHeight = maxDepth(root.left);
            int rightHeight = maxDepth(root.right);
            return Math.max(leftHeight, rightHeight) + 1;
        }
    }
}
```

#### 2.q1325\_删除给定值的叶子节点

- 问题

* 1325.删除给定值的叶子节点

给你一棵以 root 为根的二叉树和一个整数 target ，请你删除所有值为 target 的 叶子节点 。

注意，一旦删除值为 target 的叶子节点，它的父节点就可能变成叶子节点；
如果新叶子节点的值恰好也是 target ，那么这个节点也应该被删除。

也就是说，你需要重复此过程直到不能继续删除。

- 示例 1：

```
输入：root = [1,2,3,2,null,2,4], target = 2
输出：[1,null,3,null,4]
解释：
上面左边的图中，绿色节点为叶子节点，且它们的值与 target 相同（同为 2 ），
它们会被删除，得到中间的图。
有一个新的节点变成了叶子节点且它的值与 target 相同，所以将再次进行删除，从而得到最右边的图。
```

- 示例 2：

```
输入：root = [1,3,3,3,2], target = 3
输出：[1,3,null,null,2]
```

- 示例 3：

```
输入：root = [1,2,null,2,null,2], target = 2
输出：[1]
解释：每一步都删除一个绿色的叶子节点（值为 2）。
```

- 示例 4：

```
输入：root = [1,1,1], target = 1
输出：[]
```

- 示例 5：

```
输入：root = [1,2,3], target = 1
输出：[1,2,3]
```

- 提示：

```
1 <= target <= 1000
每一棵树最多有 3000 个节点。
每一个节点值的范围是 [1, 1000] 。
```

https://leetcode-cn.com/problems/delete-leaves-with-a-given-value/

- 解答

```
package 递归.q1325_删除给定值的叶子节点;

/**
 * 递归 o(n)
 */
public class Solution {

    public TreeNode removeLeafNodes(TreeNode root, int target) {
        if (root == null) {
            return null;
        }

        root.left = removeLeafNodes(root.left, target);
        root.right = removeLeafNodes(root.right, target);

        if (root.val == target && root.left == null && root.right == null) {
            return null;
        }
        return root;
    }
}
```

#### 3.q101\_对称二叉树

##### 1.f3

- 问题

* 解答

```
package 递归.q101_对称二叉树.f3;

/**
 * 递归 o(n)（如果一个树的左子树与右子树镜像对称，那么这个树是对称的。根结点相同并且每个树的左子树和另一个树的右子树镜像对称的树是镜像对称的）
 */
public class Solution {
    public boolean isSymmetric(TreeNode root) {
        return isMirror(root, root);
    }

    public boolean isMirror(TreeNode t1, TreeNode t2) {
        if (t1 == null && t2 == null) {
            return true;
        }
        if (t1 == null || t2 == null) {
            return false;
        }
        return (t1.val == t2.val)
                && isMirror(t1.right, t2.left)
                && isMirror(t1.left, t2.right);
    }
}
```

##### 2.f2

- 问题

* 解答

```
package 递归.q101_对称二叉树.f2;

import java.util.LinkedList;
import java.util.Queue;

/**
 * 利用队列的层序遍历（广度优先搜索BFS）o(n)
 */
public class Solution {
    public boolean isSymmetric(TreeNode root) {
        Queue<TreeNode> q = new LinkedList<>();
        q.add(root);
        q.add(root);
        while (!q.isEmpty()) {
            TreeNode t1 = q.poll();
            TreeNode t2 = q.poll();
            if (t1 == null && t2 == null) {
                continue;
            }
            if (t1 == null || t2 == null) {
                return false;
            }
            if (t1.val != t2.val) {
                return false;
            }
            q.add(t1.left);
            q.add(t2.right);
            q.add(t1.right);
            q.add(t2.left);
        }
        return true;
    }

    public static void main(String[] args) {
        TreeNode root = new TreeNode(1);
        TreeNode t1 = new TreeNode(2);
        TreeNode t2 = new TreeNode(2);
        root.left = t1;
        root.right = t2;
        TreeNode t3 = new TreeNode(3);
        TreeNode t4 = new TreeNode(4);
        t1.left = t3;
        t1.right = t4;
        TreeNode t5 = new TreeNode(4);
        TreeNode t6 = new TreeNode(3);
        t2.left = t5;
        t2.right = t6;

        System.out.println(new Solution().isSymmetric(root));
    }
}
```

- 问题

* 101.对称二叉树

给定一个二叉树，检查它是否是镜像对称的。

例如，二叉树 [1,2,2,3,4,4,3] 是对称的。

```
    1
   / \
  2   2
 / \ / \
3  4 4  3
```

但是下面这个 [1,2,2,null,3,null,3] 则不是镜像对称的:

```
    1
   / \
  2   2
   \   \
   3    3
```

- 进阶：

你可以运用递归和迭代两种方法解决这个问题吗？

- 解答

```

```

#### 4.q226\_翻转二叉树

- 问题

* 226.翻转二叉树

翻转一棵二叉树。

- 示例：

输入：

```
     4
   /   \
  2     7
 / \   / \
1   3 6   9
```

输出：

```
     4
   /   \
  7     2
 / \   / \
9   6 3   1
```

- 备注:

这个问题是受到 Max Howell 的 原问题 启发的 ：

- 参考
  https://leetcode-cn.com/problems/invert-binary-tree/

* 解答

```
package 递归.q226_翻转二叉树;

/**
 * 递归 o(n)
 */
public class Solution {

    public TreeNode invertTree(TreeNode root) {
        if (root == null) {
            return null;
        }
        TreeNode temp = root.left;
        root.left = root.right;
        root.right = temp;
        if (root.left != null) {
            invertTree(root.left);
        }
        if (root.right != null) {
            invertTree(root.right);
        }
        return root;
    }
}
```

#### 5.q236\_二叉树的最近公共祖先

- 问题

* 236.二叉树的最近公共祖先

给定一个二叉树, 找到该树中两个指定节点的最近公共祖先。

百度百科中最近公共祖先的定义为：“对于有根树 T 的两个节点 p、q，最近公共祖先表示为一个节点 x，
满足 x 是 p、q 的祖先且 x 的深度尽可能大（一个节点也可以是它自己的祖先）。”

- 示例 1：

```
输入：root = [3,5,1,6,2,0,8,null,null,7,4], p = 5, q = 1
输出：3
解释：节点 5 和节点 1 的最近公共祖先是节点 3 。
```

- 示例 2：

```
输入：root = [3,5,1,6,2,0,8,null,null,7,4], p = 5, q = 4
输出：5
解释：节点 5 和节点 4 的最近公共祖先是节点 5 。因为根据定义最近公共祖先节点可以为节点本身。
```

- 示例 3：

```
输入：root = [1,2], p = 1, q = 2
输出：1
```

- 提示：

```
树中节点数目在范围 [2, 105] 内。
-109 <= Node.val <= 109
所有 Node.val 互不相同 。
p != q
p 和 q 均存在于给定的二叉树中。
通过次数193,321提交次数288,363
```

- 解答

```
package 递归.q236_二叉树的最近公共祖先;

/**
 * LCA二叉树的最近公共祖先（递归）o(n)
 */
class Solution {
    public TreeNode lowestCommonAncestor(TreeNode root, TreeNode p, TreeNode q) {
        if (root == p || root == q || root == null) {
            return root;
        }
        TreeNode left = lowestCommonAncestor(root.left, p, q);
        TreeNode right = lowestCommonAncestor(root.right, p, q);
        if (left != null && right == null) {
            //左子树上能找到，但是右子树上找不到，此时就应当直接返回左子树的查找结果
            return left;
        } else if (left == null) {
            //右子树上能找到，但是左子树上找不到，此时就应当直接返回右子树的查找结果
            return right;
        }
        //左右子树上均能找到，说明此时的p结点和q结点分居root结点两侧，此时就应当直接返回root结点
        return root;
    }
}
```

#### 6.q21\_合并两个有序链表

##### 1.f2

- 问题

* 解答

```
package 递归.q21_合并两个有序链表.f2;

/**
 * 递归（看成两个链表头部较小的一个与剩下元素的 merge 操作结果合并） o(n)
 */
public class Solution {
    public ListNode mergeTwoLists(ListNode l1, ListNode l2) {
        if (l1 == null) {
            return l2;
        } else if (l2 == null) {
            return l1;
        } else if (l1.val < l2.val) {
            l1.next = mergeTwoLists(l1.next, l2);
            return l1;
        } else {
            l2.next = mergeTwoLists(l1, l2.next);
            return l2;
        }
    }
}
```

- 问题

* 21.合并两个有序链表

将两个升序链表合并为一个新的 升序 链表并返回。
新链表是通过拼接给定的两个链表的所有节点组成的。

- 示例 1：

```
输入：l1 = [1,2,4], l2 = [1,3,4]
输出：[1,1,2,3,4,4]
```

- 示例 2：

```
输入：l1 = [], l2 = []
输出：[]
```

- 示例 3：

```
输入：l1 = [], l2 = [0]
输出：[0]
```

- 提示：

```
两个链表的节点数目范围是 [0, 50]
-100 <= Node.val <= 100
l1 和 l2 均按 非递减顺序 排列
```

https://leetcode-cn.com/problems/merge-two-sorted-lists/

- 解答

```

```

### 15.动态规划

#### 1.q64\_最小路径和

- 问题

---

q64\_最小路径和

---

给定一个包含非负整数的 m x n 网格 grid ，
请找出一条从左上角到右下角的路径，使得路径上的数字总和为最小。

说明：每次只能向下或者向右移动一步。

示例 1：
输入：grid = [[1,3,1],[1,5,1],[4,2,1]]
输出：7
解释：因为路径 1→3→1→1→1 的总和最小。

示例 2：
输入：grid = [[1,2,3],[4,5,6]]
输出：12

提示：

m == grid.length
n == grid[i].length
1 <= m, n <= 200
0 <= grid[i][j] <= 100

Related Topics 数组 动态规划
👍 857 👎 0

- 解答

```
package 动态规划.q64_最小路径和;

/**
 * 动态规划 dp(j)=grid(i,j)+min(dp(j),dp(j+1)) o(m*n)
 */
public class Solution {

    public int minPathSum(int[][] grid) {
        int[] dp = new int[grid[0].length];
        for (int i = grid.length - 1; i >= 0; i--) {
            for (int j = grid[0].length - 1; j >= 0; j--) {
                if (i == grid.length - 1 && j != grid[0].length - 1) {
                    dp[j] = grid[i][j] + dp[j + 1];
                } else if (j == grid[0].length - 1 && i != grid.length - 1) {
                    dp[j] = grid[i][j] + dp[j];
                } else if (j != grid[0].length - 1 && i != grid.length - 1) {
                    dp[j] = grid[i][j] + Math.min(dp[j], dp[j + 1]);

                } else {
                    dp[j] = grid[i][j];
                }
            }
        }
        return dp[0];
    }
}
```

#### 2.q403\_青蛙过河

##### 1.f2

- 问题

* 解答

```
package 动态规划.q403_青蛙过河.f2;

/**
 * 动态规划
 * 我们也可以使用动态规划的方法，令 {dp}[i][k]表示青蛙能否达到「现在所处的石子编号」为 i 且「上一次跳跃距离」为 k 的状态。
 *
 */
public class Solution {
    public boolean canCross(int[] stones) {
        int n = stones.length;
        boolean[][] dp = new boolean[n][n];
        dp[0][0] = true;
        for (int i = 1; i < n; ++i) {
            if (stones[i] - stones[i - 1] > i) {
                return false;
            }
        }
        for (int i = 1; i < n; ++i) {
            for (int j = i - 1; j >= 0; --j) {
                int k = stones[i] - stones[j];
                if (k > j + 1) {
                    break;
                }
                dp[i][k] = dp[j][k - 1] || dp[j][k] || dp[j][k + 1];
                if (i == n - 1 && dp[i][k]) {
                    return true;
                }
            }
        }
        return false;
    }
}
```

- 问题

* 403.青蛙过河

一只青蛙想要过河。 假定河流被等分为若干个单元格，
并且在每一个单元格内都有可能放有一块石子（也有可能没有）。
青蛙可以跳上石子，但是不可以跳入水中。
给你石子的位置列表 stones（用单元格序号 升序 表示），
请判定青蛙能否成功过河（即能否在最后一步跳至最后一块石子上）。
开始时， 青蛙默认已站在第一块石子上，并可以假定它第一步只能跳跃一个单位（即只能从单元格 1 跳至单元格 2 ）。
如果青蛙上一步跳跃了 k 个单位，那么它接下来的跳跃距离只能选择为 k - 1、k 或 k + 1 个单位。
另请注意，青蛙只能向前方（终点的方向）跳跃。

- 示例 1：

```
输入：stones = [0,1,3,5,6,8,12,17]
输出：true
解释：青蛙可以成功过河，按照如下方案跳跃：跳 1 个单位到第 2 块石子,
然后跳 2 个单位到第 3 块石子, 接着 跳 2 个单位到第 4 块石子,
然后跳 3 个单位到第 6 块石子, 跳 4 个单位到第 7 块石子,
最后，跳 5 个单位到第 8 个石子（即最后一块石子）。
```

- 示例 2：

```
输入：stones = [0,1,2,3,4,8,9,11]
输出：false
解释：这是因为第 5 和第 6 个石子之间的间距太大，没有可选的方案供青蛙跳跃过去。
```

- 提示：

```
2 <= stones.length <= 2000
0 <= stones[i] <= 231 - 1
stones[0] == 0
```

- 参考

作者：LeetCode-Solution
链接：https://leetcode-cn.com/problems/frog-jump/solution/qing-wa-guo-he-by-leetcode-solution-mbuo/
来源：力扣（LeetCode）
著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。

- 解答

```

```

#### 3.q53\_最大子序和

##### 1.f2

- 问题

* 解答

```
package 动态规划.q53_最大子序和.f2;

/**
 * 动态规划 dp[i]表示以nums[i]结尾的最大子序和 o(n)
 */
public class Solution {
    public int maxSubArray(int[] nums) {
        int[] dp = new int[nums.length];
        dp[0] = nums[0];

        int rs = dp[0];

        for (int i = 1; i < nums.length; i++) {
            int temp = dp[i - 1] + nums[i];
            dp[i] = Math.max(nums[i],temp);
            rs = Math.max(rs, dp[i]);
        }
        return rs;
    }

    public static void main(String[] args) {
        System.out.println(new Solution().maxSubArray(new int[]{-2,1,1,1}));
    }
}
```

- 问题

53. 最大子序和
    给定一个整数数组 nums ，
    找到一个具有最大和的连续子数组（子数组最少包含一个元素），
    返回其最大和。

示例 1：

输入：nums = [-2,1,-3,4,-1,2,1,-5,4]
输出：6
解释：连续子数组 [4,-1,2,1] 的和最大，为 6 。
示例 2：

输入：nums = [1]
输出：1
示例 3：

输入：nums = [0]
输出：0
示例 4：

输入：nums = [-1]
输出：-1
示例 5：

输入：nums = [-100000]
输出：-100000

提示：

1 <= nums.length <= 3 \* 104
-105 <= nums[i] <= 105

进阶：如果你已经实现复杂度为 O(n) 的解法，尝试使用更为精妙的 分治法 求解。

- 解答

```

```

#### 4.q62\_不同路径

- 问题

62. 不同路径
    一个机器人位于一个 m x n 网格的左上角 （起始点在下图中标记为 “Start” ）。

机器人每次只能向下或者向右移动一步。机器人试图达到网格的右下角（在下图中标记为 “Finish” ）。

问总共有多少条不同的路径？

示例 1：
输入：m = 3, n = 7
输出：28

示例 2：
输入：m = 3, n = 2
输出：3

解释：
从左上角开始，总共有 3 条路径可以到达右下角。

1. 向右 -> 向下 -> 向下
2. 向下 -> 向下 -> 向右
3. 向下 -> 向右 -> 向下

示例 3：
输入：m = 7, n = 3
输出：28

示例 4：
输入：m = 3, n = 3
输出：6

提示：

1 <= m, n <= 100
题目数据保证答案小于等于 2 \* 109

- 解答

```
package 动态规划.q62_不同路径;

/**
 * 动态规划 dp[i][j]是到达i, j的最多路径 dp[i][j] = dp[i-1][j] + dp[i][j-1] o(m*n)
 */
public class Solution {

    public int uniquePaths(int m, int n) {
        if (m < 1 || n < 1) {
            return 0;
        }
        int[][] dp = new int[m][n];
        for (int i = 0; i < n; i++) {
            dp[0][i] = 1;
        }
        for (int i = 0; i < m; i++) {
            dp[i][0] = 1;
        }
        for (int i = 1; i < m; i++) {
            for (int j = 1; j < n; j++) {
                dp[i][j] = dp[i - 1][j] + dp[i][j - 1];
            }
        }
        return dp[m - 1][n - 1];
    }
}
```

#### 5.q300\_最长上升子序列

- 问题

* 解答

```
package 动态规划.q300_最长上升子序列;

/**
 * 动态规划 dp[i]表示以i索引下标结束的最长上升子序列 o(n*log(n))
 */
public class Solution {

    public int lengthOfLIS(int[] nums) {
        if (nums == null || nums.length == 0) {
            return 0;
        }

        if (nums.length == 1) {
            return 1;
        }

        int n = nums.length;
        int[] dp = new int[n];
        int rs = 0;

        for (int i = 0; i < n; i++) {
            dp[i] = 1;
            int max = 0;
            for (int j = i - 1; j >= 0; j--) {
                if (nums[j] < nums[i] && dp[j] > max) {
                    max = dp[j];
                }
            }
            dp[i] += max;
            if (dp[i] > rs) {
                rs = dp[i];
            }
        }
        return rs;
    }
}
```

#### 6.q118\_杨辉三角

- 问题

* 解答

```
package 动态规划.q118_杨辉三角;

import java.util.ArrayList;
import java.util.List;

/**
 * 找规律，动态规划 o(n^2)
 */
public class Solution {

    public List<List<Integer>> generate(int numRows) {
        List<List<Integer>> triangle = new ArrayList<List<Integer>>();

        if (numRows == 0) {
            return triangle;
        }

        triangle.add(new ArrayList<>());
        triangle.get(0).add(1);

        for (int rowNum = 1; rowNum < numRows; rowNum++) {
            List<Integer> row = new ArrayList<>();
            List<Integer> prevRow = triangle.get(rowNum-1);
            row.add(1);

            for (int j = 1; j < rowNum; j++) {
                row.add(prevRow.get(j-1) + prevRow.get(j));
            }

            row.add(1);
            triangle.add(row);
        }
        return triangle;
    }
}
```

#### 7.q746\_使用最小花费爬楼梯

- 问题

* 解答

```
package 动态规划.q746_使用最小花费爬楼梯;

/**
 * 动态规划 o(n) f[i] = cost[i] + min(f[i+1], f[i+2])
 */
class Solution {
    public int minCostClimbingStairs(int[] cost) {
        int f1 = 0, f2 = 0;
        for (int i = cost.length - 1; i >= 0; i--) {
            int f0 = cost[i] + Math.min(f1, f2);
            f2 = f1;
            f1 = f0;
        }
        return Math.min(f1, f2);
    }

    public static void main(String[] args) {
        int[] a = new int[]{0, 2, 2, 1};
        System.out.println(new Solution().minCostClimbingStairs(a));
    }
}
```

#### 8.q1277\_统计全为 1 的正方形子矩阵

- 问题

* 解答

```
package 动态规划.q1277_统计全为1的正方形子矩阵;

/**
 * 动态规划 dp[i][j]表示 matrix[i][j] 这个点可以往左上构造的最大正方形的边长 o(n^2)
 */
public class Solution {
    public int countSquares(int[][] matrix) {
        if (matrix.length < 1) {
            return 0;
        }
        int m = matrix.length;
        int n = matrix[0].length;

        int[][] dp = new int[m][n];

        int rs = 0;
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (matrix[i][j] == 0) {
                    dp[i][j] = 0;
                } else {
                    if (i > 0 && j > 0) {
                        dp[i][j] = Math.min(Math.min(dp[i - 1][j], dp[i][j - 1]), dp[i - 1][j - 1]) + 1;
                    } else {
                        dp[i][j] = 1;
                    }
                    rs += dp[i][j];
                }
            }
        }
        return rs;
    }

    public static void main(String[] args) {
        new Solution().countSquares(new int[][]{{0, 1, 1, 1}, {1, 1, 1, 1}, {0, 1, 1, 1}});
    }
}
```

#### 9.q1143\_最长公共子序列

- 问题

* 解答

```
package 动态规划.q1143_最长公共子序列;

/**
 * 动态规划 dp[i + 1][j + 1] = Math.max(dp[i+1][j], dp[i][j+1]) o(m*n)
 *
 * 若题目为最长公共子串，则在c1,c2不相等时不做处理（赋值0），在遍历过程中记录最大值即可
 */
public class Solution {

    public int longestCommonSubsequence(String text1, String text2) {
        int m = text1.length();
        int n = text2.length();
        int[][] dp = new int[m + 1][n + 1];

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                char c1 = text1.charAt(i);
                char c2 = text2.charAt(j);
                if (c1 == c2) {
                    dp[i + 1][j + 1] = dp[i][j] + 1;
                } else {
                    dp[i + 1][j + 1] = Math.max(dp[i + 1][j], dp[i][j + 1]);
                }
            }
        }
        return dp[m][n];
    }
}
```

#### 10.q5\_最长回文子串

##### 1.f2

- 问题

* 解答

```
package 动态规划.q5_最长回文子串.f2;

/**
 * 动态规划 o(n^2)
 * 转移方程：字符串两边界值相等并且子字符串是回文字符串则该字符串是回文字符串
 * dp数组含义：字符串s从i到j的索引子字符串是否是回文字符串
 *
 *
 *
 */
public class Solution {
    public String longestPalindrome(String s) {
        int len = s.length();

        if (len < 2) {
            return s;
        }
        boolean[][] dp = new boolean[len][len];
        for (int i = 0; i < len; i++) {
            dp[i][i] = true;
        }

        int maxLen = 1;
        int start = 0;

        for (int j = 1; j < len; j++) {
            for (int i = 0; i < j; i++) {
                if (s.charAt(i) == s.charAt(j)) {
                    if (j - i < 3) {
                        dp[i][j] = true;
                    } else {
                        dp[i][j] = dp[i + 1][j - 1];
                    }
                } else {
                    dp[i][j] = false;
                }

                if (dp[i][j]) {
                    int curLen = j - i + 1;
                    if (curLen > maxLen) {
                        maxLen = curLen;
                        start = i;
                    }
                }

            }
        }
        return s.substring(start, start + maxLen);
    }
}

/**
 * 下面是「动态规划』问题的思考路径，供大家参考。
 *
 * 特别说明：
 *
 * 以下「动态规划」的解释只帮助大家了解「动态规划」问题的基本思想；
 * 「动态规划」问题可以难到非常难，在学习的时候建议不要钻到特别难的问题中去；
 * 掌握经典的动态规划问题的解法，理解状态的定义的由来、会列出状态转移方程；
 * 然后再配合适当难度的问题的练习；
 * 有时间和感兴趣的话可以做一些不太常见的类型的问题，拓宽视野；
 * 「动态规划」讲得比较好的经典书籍是《算法导论》。
 * 提示：右键「在新便签页打开图片」可查看大图。
 *
 *
 *
 * 1、思考状态（重点）
 *
 * 状态的定义，先尝试「题目问什么，就把什么设置为状态」；
 * 然后思考「状态如何转移」，如果「状态转移方程」不容易得到，尝试修改定义，目的依然是为了方便得到「状态转移方程」。
 * 「状态转移方程」是原始问题的不同规模的子问题的联系。即大问题的最优解如何由小问题的最优解得到。
 *
 * 2、思考状态转移方程（核心、难点）
 *
 * 状态转移方程是非常重要的，是动态规划的核心，也是难点；
 *
 * 常见的推导技巧是：分类讨论。即：对状态空间进行分类；
 *
 * 归纳「状态转移方程」是一个很灵活的事情，通常是具体问题具体分析；
 *
 * 除了掌握经典的动态规划问题以外，还需要多做题；
 *
 * 如果是针对面试，请自行把握难度。掌握常见问题的动态规划解法，理解动态规划解决问题，是从一个小规模问题出发，逐步得到大问题的解，并记录中间过程；
 *
 * 「动态规划」方法依然是「空间换时间」思想的体现，常见的解决问题的过程很像在「填表」。
 *
 * 3、思考初始化
 *
 * 初始化是非常重要的，一步错，步步错。初始化状态一定要设置对，才可能得到正确的结果。
 *
 * 角度 1：直接从状态的语义出发；
 *
 * 角度 2：如果状态的语义不好思考，就考虑「状态转移方程」的边界需要什么样初始化的条件；
 *
 * 角度 3：从「状态转移方程」方程的下标看是否需要多设置一行、一列表示「哨兵」（sentinel），这样可以避免一些特殊情况的讨论。
 *
 * 4、思考输出
 *
 * 有些时候是最后一个状态，有些时候可能会综合之前所有计算过的状态。
 *
 * 5、思考优化空间（也可以叫做表格复用）
 *
 * 「优化空间」会使得代码难于理解，且是的「状态」丢失原来的语义，初学的时候可以不一步到位。先把代码写正确是更重要；
 * 「优化空间」在有一种情况下是很有必要的，那就是状态空间非常庞大的时候（处理海量数据），此时空间不够用，就必须「优化空间」；
 * 非常经典的「优化空间」的典型问题是「0-1 背包」问题和「完全背包」问题。
 * （下面是这道问题「动态规划」方法的分析）
 *
 * 这道题比较烦人的是判断回文子串。因此需要一种能够快速判断原字符串的所有子串是否是回文子串的方法，于是想到了「动态规划」。
 *
 * 「动态规划」的一个关键的步骤是想清楚「状态如何转移」。事实上，「回文」天然具有「状态转移」性质。
 *
 * 一个回文去掉两头以后，剩下的部分依然是回文（这里暂不讨论边界情况）；
 * 依然从回文串的定义展开讨论：
 *
 * 如果一个字符串的头尾两个字符都不相等，那么这个字符串一定不是回文串；
 * 如果一个字符串的头尾两个字符相等，才有必要继续判断下去。
 * 如果里面的子串是回文，整体就是回文串；
 * 如果里面的子串不是回文串，整体就不是回文串。
 * 即：在头尾字符相等的情况下，里面子串的回文性质据定了整个子串的回文性质，这就是状态转移。因此可以把「状态」定义为原字符串的一个子串是否为回文子串。
 *
 * 第 1 步：定义状态
 * dp[i][j] 表示子串 s[i..j] 是否为回文子串，这里子串 s[i..j] 定义为左闭右闭区间，可以取到 s[i] 和 s[j]。
 *
 * 第 2 步：思考状态转移方程
 * 在这一步分类讨论（根据头尾字符是否相等），根据上面的分析得到：
 *
 *
 * dp[i][j] = (s[i] == s[j]) and dp[i + 1][j - 1]
 * 说明：
 *
 * 「动态规划」事实上是在填一张二维表格，由于构成子串，因此 i 和 j 的关系是 i <= j ，因此，只需要填这张表格对角线以上的部分。
 *
 * 看到 dp[i + 1][j - 1] 就得考虑边界情况。
 *
 * 边界条件是：表达式 [i + 1, j - 1] 不构成区间，即长度严格小于 2，即 j - 1 - (i + 1) + 1 < 2 ，整理得 j - i < 3。
 *
 * 这个结论很显然：j - i < 3 等价于 j - i + 1 < 4，即当子串 s[i..j] 的长度等于 2 或者等于 3 的时候，其实只需要判断一下头尾两个字符是否相等就可以直接下结论了。
 *
 * 如果子串 s[i + 1..j - 1] 只有 1 个字符，即去掉两头，剩下中间部分只有 11 个字符，显然是回文；
 * 如果子串 s[i + 1..j - 1] 为空串，那么子串 s[i, j] 一定是回文子串。
 * 因此，在 s[i] == s[j] 成立和 j - i < 3 的前提下，直接可以下结论，dp[i][j] = true，否则才执行状态转移。
 *
 * 第 3 步：考虑初始化
 * 初始化的时候，单个字符一定是回文串，因此把对角线先初始化为 true，即 dp[i][i] = true 。
 *
 * 事实上，初始化的部分都可以省去。因为只有一个字符的时候一定是回文，dp[i][i] 根本不会被其它状态值所参考。
 *
 * 第 4 步：考虑输出
 * 只要一得到 dp[i][j] = true，就记录子串的长度和起始位置，没有必要截取，这是因为截取字符串也要消耗性能，记录此时的回文子串的「起始位置」和「回文长度」即可。
 *
 * 第 5 步：考虑优化空间
 * 因为在填表的过程中，只参考了左下方的数值。事实上可以优化，但是增加了代码编写和理解的难度，丢失可读和可解释性。在这里不优化空间。
 *
 * 注意事项：总是先得到小子串的回文判定，然后大子串才能参考小子串的判断结果，即填表顺序很重要。
 *
 * 大家能够可以自己动手，画一下表格，相信会对「动态规划」作为一种「表格法」有一个更好的理解。
 *
 *
 *
 * 作者：liweiwei1419
 * 链接：https://leetcode-cn.com/problems/longest-palindromic-substring/solution/zhong-xin-kuo-san-dong-tai-gui-hua-by-liweiwei1419/
 * 来源：力扣（LeetCode）
 * 著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。
 *
 * */
```

- 问题

5. 最长回文子串
   给你一个字符串 s，找到 s 中最长的回文子串。

示例 1：

输入：s = "babad"
输出："bab"
解释："aba" 同样是符合题意的答案。

示例 2：

输入：s = "cbbd"
输出："bb"

示例 3：

输入：s = "a"
输出："a"
示例 4：

输入：s = "ac"
输出："a"

提示：

1 <= s.length <= 1000
s 仅由数字和英文字母（大写和/或小写）组成

- 解答

```

```

#### 11.q70\_爬楼梯

- 问题

### 70. 爬楼梯

假设你正在爬楼梯。需要 n 阶你才能到达楼顶。
每次你可以爬 1 或 2 个台阶。你有多少种不同的方法可以爬到楼顶呢？
注意：给定 n 是一个正整数。

### 示例 1：

```
输入： 2
输出： 2
解释： 有两种方法可以爬到楼顶。
1.  1 阶 + 1 阶
2.  2 阶
```

### 示例 2：

```
输入： 3
输出： 3
解释： 有三种方法可以爬到楼顶。
1.  1 阶 + 1 阶 + 1 阶
2.  1 阶 + 2 阶
3.  2 阶 + 1 阶
```

- 解答

```
package 动态规划.q70_爬楼梯;

/**
 * 动态规划 dp[i]表示到达第i阶的方法总数dp[i]=dp[i−1]+dp[i−2] o(n)
 */
public class Solution {

    public int climbStairs(int n) {
        if (n == 1) {
            return 1;
        }
        int[] dp = new int[n + 1];
        dp[1] = 1;
        dp[2] = 2;
        for (int i = 3; i <= n; i++) {
            dp[i] = dp[i - 1] + dp[i - 2];
        }
        return dp[n];
    }
}
```

### 16.双指针遍历

#### 1.q209\_长度最小的子数组

- 问题

* 解答

```
package 双指针遍历.q209_长度最小的子数组;

/**
 * 两个指针滑动窗口o(n)
 */
public class Solution {

    public int minSubArrayLen(int s, int[] nums) {
        int sum = 0;
        int i = 0;
        int k = 0;
        int min = Integer.MAX_VALUE;
        while (true) {
            if (k == nums.length && i == nums.length) {
                break;
            }
            if (sum == s) {
                min = Math.min(k - i, min);
                if (k < nums.length) {
                    sum += nums[k];
                    k++;
                } else {
                    sum -= nums[i];
                    i++;
                }
            } else if (sum < s) {
                if (k == nums.length) {
                    break;
                }
                sum += nums[k];
                k++;
            } else {
                min = Math.min(k - i, min);
                sum -= nums[i];
                i++;
            }
        }
        return min == Integer.MAX_VALUE ? 0 : min;
    }
}
```

#### 2.q15\_三数之和

- 问题

* 解答

```
package 双指针遍历.q15_三数之和;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 数组遍历 + 双指针遍历 o(n^2)
 */
class Solution {
    public List<List<Integer>> threeSum(int[] nums) {
        List<List<Integer>> rs = new ArrayList<>();

        if (nums.length < 3) {
            return rs;
        }

        Arrays.sort(nums);
        if (nums[0] > 0) {
            return rs;
        }

        for (int i = 0; i < nums.length - 2; i++) {
            if (i > 0 && nums[i] == nums[i - 1]) {
                continue;
            }
            int left = i + 1;
            int right = nums.length - 1;
            while (left < right) {
                int sum = nums[i] + nums[left] + nums[right];
                if (sum == 0) {
                    List<Integer> temp = new ArrayList<>();
                    temp.add(nums[i]);
                    temp.add(nums[left]);
                    temp.add(nums[right]);
                    rs.add(temp);
                    while (left < right && nums[left] == nums[left + 1]) {
                        left++;
                    }
                    while (left < right && nums[right] == nums[right - 1]) {
                        right--;
                    }
                    left++;
                    right--;
                } else if (sum > 0) {
                    right--;
                } else {
                    left++;
                }
            }
        }
        return rs;
    }
}
```

#### 3.q11\_盛最多水的容器

- 问题

* 解答

```
package 双指针遍历.q11_盛最多水的容器;

/**
 * 双指针遍历 o(n)
 */
public class Solution {
    public int maxArea(int[] height) {
        if (height.length < 2) {
            return 0;
        }
        int left = 0;
        int right = height.length - 1;
        int result = 0;
        while (right > left) {
            int c = (Math.min(height[right], height[left])) * (right - left);
            if (c >= result) {
                result = c;
            }
            if (height[left] < height[right]) {
                left++;
            } else {
                right--;
            }
        }
        return result;
    }

    public static void main(String[] args) {
        int[] a = new int[]{1, 8, 6, 2, 5, 4, 8, 3, 7};
        System.out.println(new Solution().maxArea(a));
    }
}
```

#### 4.q121\_买卖股票的最佳时机

- 问题

* 解答

```
package 双指针遍历.q121_买卖股票的最佳时机;

/**
 * 维护一个最低股价变量，同时维护当前收益o(n)
 */
class Solution {
    public int maxProfit(int[] prices) {
        int min = Integer.MAX_VALUE;
        int money = 0;
        for (int i = 0; i < prices.length; i++) {
            if (prices[i] < min) {
                min = prices[i];
            }
            if (prices[i] - min > money) {
                money = prices[i] - min;
            }
        }
        return money;
    }

    public static void main(String[] args) {
        int[] a = new int[]{7, 1, 5, 3, 6, 4};
        System.out.println(new Solution().maxProfit(a));
    }
}
```

#### 5.q26\_删除排序数组中的重复项

- 问题

* 解答

```
package 双指针遍历.q26_删除排序数组中的重复项;

/**
 * 双指针 o(n)
 */
public class Solution {
    public int removeDuplicates(int[] nums) {
        if (nums.length < 2) {
            return nums.length;
        }
        int c = 0;
        for (int i = 1; i < nums.length; i++) {
            if (nums[i] != nums[c]) {
                c++;
                nums[c] = nums[i];
            }
        }
        return c + 1;
    }

    public static void main(String[] args) {
        new Solution().removeDuplicates(new int[]{1, 1, 2});
    }
}
```

#### 6.q3\_无重复字符的最长子串

- 问题

* 解答

```
package 双指针遍历.q3_无重复字符的最长子串;

import java.util.HashMap;

/**
 * Hash+双指针滑动窗口 o(n)
 */
public class Solution {
    public int lengthOfLongestSubstring(String s) {
        int left = 0;
        int right = 0;
        int len = 0;
        HashMap<Character, Integer> map = new HashMap<>();
        while (right < s.length()) {
            Integer index = map.get(s.charAt(right));
            map.put(s.charAt(right), right);
            if (index != null && index >= left) {
                left = index + 1;
            }
            if (right - left + 1 > len) {
                len = right - left + 1;
            }
            right++;
        }
        return len;
    }
}
```

#### 7.q42\_接雨水

- 问题

* 解答

```
package 双指针遍历.q42_接雨水;

/**
 * 暴力法o(n^2) 找出每个元素（柱子）上面的水量，可提前存储最大高度数组（两个左和右），最后遍历一次优化为o(n)
 */
public class Solution {

    public int trap(int[] height) {
        int ans = 0;
        int size = height.length;
        for (int i = 1; i < size - 1; i++) {
            int maxLeft = 0, maxRight = 0;
            for (int j = i; j >= 0; j--) {
                maxLeft = Math.max(maxLeft, height[j]);
            }
            for (int j = i; j < size; j++) {
                maxRight = Math.max(maxRight, height[j]);
            }
            ans += Math.min(maxLeft, maxRight) - height[i];
        }
        return ans;
    }

    public static void main(String[] args) {
        new Solution().trap(new int[]{0, 1, 0, 2, 1, 0, 1, 3, 2, 1, 2, 1});
    }
}
```

#### 8.q16\_最接近的三数之和

- 问题

* 解答

```
package 双指针遍历.q16_最接近的三数之和;

import java.util.Arrays;

/**
 * q15类型题 数组遍历 + 双指针遍历 o(n^2)
 */

public class Solution {
    public int threeSumClosest(int[] nums, int target) {
        if (nums.length < 3) {
            return 0;
        }

        Arrays.sort(nums);
        int rs = nums[0] + nums[1] + nums[2];

        for (int i = 0; i < nums.length - 2; i++) {
            if (i > 0 && nums[i] == nums[i - 1]) {
                continue;
            }
            int left = i + 1;
            int right = nums.length - 1;
            while (left < right) {
                int sum = nums[i] + nums[left] + nums[right];
                int c = sum - target;
                if (Math.abs(c) < Math.abs(rs - target)) {
                    rs = sum;
                }
                if (c == 0) {
                    return target;
                } else if (c > 0) {
                    right--;
                } else {
                    left++;
                }
            }
        }
        return rs;
    }

    public static void main(String[] args) {
        int[] a = new int[]{-3, -2, -5, 3, -4};
        System.out.println(new Solution().threeSumClosest(a, -1));
    }
}
```

### 17.数组操作

#### 1.q945\_使数组唯一的最小增量

- 问题

* 945.使数组唯一的最小增量

给定整数数组 A，每次 move 操作将会选择任意 A[i]，并将其递增 1。
返回使 A 中的每个值都是唯一的最少操作次数。

- 示例 1:

```
输入：[1,2,2]
输出：1
解释：经过一次 move 操作，数组将变为 [1, 2, 3]。
```

- 示例 2:

```
输入：[3,2,1,2,1,7]
输出：6
解释：经过 6 次 move 操作，数组将变为 [3, 4, 1, 2, 5, 7]。
可以看出 5 次或 5 次以下的 move 操作是不能让数组的每个值唯一的。
```

- 提示：

```
0 <= A.length <= 40000
0 <= A[i] < 40000
```

- 解答

```
package 数组操作.q945_使数组唯一的最小增量;

import java.util.Arrays;

/**
 * 先排序再遍历一次 o(n*log(n))
 */
public class Solution {

    public int minIncrementForUnique(int[] A) {
        if (A == null || A.length == 0 || A.length == 1) {
            return 0;
        }

        int rs = 0;
        Arrays.sort(A);

        int t = A[0];
        for (int i = 1; i < A.length; i++) {
            if (A[i] <= t) {
                rs = rs + t - A[i] + 1;
                A[i] = t + 1;
            }
            t = A[i];
        }
        return rs;
    }
}
```

#### 2.q78\_子集

- 问题

* 78.子集

给你一个整数数组 nums ，数组中的元素 互不相同 。
返回该数组所有可能的子集（幂集）。
解集 不能 包含重复的子集。
你可以按 任意顺序 返回解集。

- 示例 1：

```
输入：nums = [1,2,3]
输出：[[],[1],[2],[1,2],[3],[1,3],[2,3],[1,2,3]]
```

- 示例 2：

```
输入：nums = [0]
输出：[[],[0]]
```

- 提示：

```
1 <= nums.length <= 10
-10 <= nums[i] <= 10
nums 中的所有元素 互不相同
```

- 解答

```
package 数组操作.q78_子集;

import java.util.ArrayList;
import java.util.List;

/**
 * 向子集中添加子集合 o(n*2^n)
 */
public class Solution {

    public List<List<Integer>> subsets(int[] nums) {
        List<List<Integer>> result = new ArrayList<>();
        result.add(new ArrayList<>());
        for (int i = 0; i < nums.length; i++) {
            int size = result.size();
            for (int j = 0; j < size; j++) {
                List<Integer> temp = new ArrayList<>(result.get(j));
                temp.add(nums[i]);
                result.add(temp);
            }
        }
        return result;
    }
}
```

#### 3.q73\_矩阵置零

- 问题

* 73.矩阵置零

给定一个 m x n 的矩阵，如果一个元素为 0 ，则将其所在行和列的所有元素都设为 0 。
请使用 原地 算法。

进阶：

一个直观的解决方案是使用 O(mn) 的额外空间，但这并不是一个好的解决方案。
一个简单的改进方案是使用 O(m + n) 的额外空间，但这仍然不是最好的解决方案。
你能想出一个仅使用常量空间的解决方案吗？

- 示例 1：

![](https://assets.leetcode.com/uploads/2020/08/17/mat1.jpg)

```
输入：matrix = [[1,1,1],[1,0,1],[1,1,1]]
输出：[[1,0,1],[0,0,0],[1,0,1]]
```

- 示例 2：

![](https://assets.leetcode.com/uploads/2020/08/17/mat2.jpg)

```
输入：matrix = [[0,1,2,0],[3,4,5,2],[1,3,1,5]]
输出：[[0,0,0,0],[0,4,5,0],[0,3,1,0]]
```

- 提示：

```
m == matrix.length
n == matrix[0].length
1 <= m, n <= 200
-231 <= matrix[i][j] <= 231 - 1
```

- 解答

```
package 数组操作.q73_矩阵置零;

/**
 * 用每行和每列的第一个元素作为标记，空间复杂度是o(1)，时间复杂度 o(m*n)
 */
public class Solution {

    public void setZeroes(int[][] matrix) {
        //第一行是否需要置零
        boolean row = false;
        //第一列是否需要置零
        boolean column = false;
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                if (matrix[i][j] == 0) {
                    if (i == 0) {
                        row = true;
                    }
                    if (j == 0) {
                        column = true;
                    }
                    //第i行第一个元素置零，表示这一行需要全部置零
                    matrix[i][0] = 0;
                    //第j列第一个元素置零，表示这一列需要全部置零
                    matrix[0][j] = 0;
                }
            }
        }
        for (int i = 1; i < matrix.length; i++) {
            if (matrix[i][0] == 0) {
                for (int j = 1; j < matrix[i].length; j++) {
                    matrix[i][j] = 0;
                }
            }
        }
        for (int j = 1; j < matrix[0].length; j++) {
            if (matrix[0][j] == 0) {
                for (int i = 1; i < matrix.length; i++) {
                    matrix[i][j] = 0;
                }
            }
        }
        if (row) {
            for (int j = 1; j < matrix[0].length; j++) {
                matrix[0][j] = 0;
            }
        }
        if (column) {
            for (int i = 1; i < matrix.length; i++) {
                matrix[i][0] = 0;
            }
        }
    }
}
```

#### 4.q54\_螺旋矩阵

- 问题

* 54.螺旋矩阵

给你一个 m 行 n 列的矩阵 matrix ，请按照 顺时针螺旋顺序 ，返回矩阵中的所有元素。

- 示例 1：

```
输入：matrix = [[1,2,3],[4,5,6],[7,8,9]]
输出：[1,2,3,6,9,8,7,4,5]
```

- 示例 2：

```
输入：matrix = [[1,2,3,4],[5,6,7,8],[9,10,11,12]]
输出：[1,2,3,4,8,12,11,10,9,5,6,7]
```

- 提示：

```
m == matrix.length
n == matrix[i].length
1 <= m, n <= 10
-100 <= matrix[i][j] <= 100
```

- 解答

```
package 数组操作.q54_螺旋矩阵;

import java.util.ArrayList;
import java.util.List;

/**
 * 方法一：模拟
 * 可以模拟螺旋矩阵的路径。初始位置是矩阵的左上角，初始方向是向右，
 * 当路径超出界限或者进入之前访问过的位置时，顺时针旋转，进入下一个方向。
 *
 * 方法二：按层模拟
 * 可以将矩阵看成若干层，首先输出最外层的元素，其次输出次外层的元素，直到输出最内层的元素。
 * 定义矩阵的第 kk 层是到最近边界距离为 kk 的所有顶点。例如，下图矩阵最外层元素都是第 11 层，
 * 次外层元素都是第 22 层，剩下的元素都是第 33 层。
 *
 *
 * [[1, 1, 1, 1, 1, 1, 1],
 *  [1, 2, 2, 2, 2, 2, 1],
 *  [1, 2, 3, 3, 3, 2, 1],
 *  [1, 2, 2, 2, 2, 2, 1],
 *  [1, 1, 1, 1, 1, 1, 1]]
 *
 * 作者：LeetCode-Solution
 * 链接：https://leetcode-cn.com/problems/spiral-matrix/solution/luo-xuan-ju-zhen-by-leetcode-solution/
 * 来源：力扣（LeetCode）
 * 著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。
 * 方向变量模拟路径 o(n)
 */
public class Solution {

    public List<Integer> spiralOrder(int[][] matrix) {
        List<Integer> rs = new ArrayList<>();
        if (matrix.length == 0 || matrix[0].length == 0) {
            return rs;
        }
        int m = matrix.length;
        int n = matrix[0].length;
        boolean[][] visited = new boolean[m][n];

        int i = 0;
        int j = 0;
        int direction = 1;
        while (true) {
            if (i < 0 || j < 0 || i == m || j == n || visited[i][j]) {
                break;
            }
            rs.add(matrix[i][j]);
            visited[i][j] = true;
            switch (direction) {
                case 1:
                    if (j + 1 == n || visited[i][j + 1]) {
                        i++;
                        direction = 2;
                    } else {
                        j++;
                    }
                    break;
                case 2:
                    if (i + 1 == m || visited[i + 1][j]) {
                        j--;
                        direction = 3;
                    } else {
                        i++;
                    }
                    break;
                case 3:
                    if (j == 0 || visited[i][j - 1]) {
                        i--;
                        direction = 4;
                    } else {
                        j--;
                    }
                    break;
                case 4:
                    if (visited[i - 1][j]) {
                        j++;
                        direction = 1;
                    } else {
                        i--;
                    }
                    break;
                default:
                    break;
            }
        }
        return rs;
    }

    public static void main(String[] args) {
        System.out.println(new Solution().spiralOrder(new int[][]{{1, 2, 3}, {4, 5, 6}, {7, 8, 9}}));
    }
}
```

#### 5.q581\_最短无序连续子数组

- 问题

* 581.最短无序连续子数组

给你一个整数数组 nums ，你需要找出一个 连续子数组 ，
如果对这个子数组进行升序排序，那么整个数组都会变为升序排序。
请你找出符合题意的 最短 子数组，并输出它的长度。

- 示例 1：

```
输入：nums = [2,6,4,8,10,9,15]
输出：5
解释：你只需要对 [6, 4, 8, 10, 9] 进行升序排序，那么整个表都会变为升序排序。
```

- 示例 2：

```
输入：nums = [1,2,3,4]
输出：0
```

- 示例 3：

```
输入：nums = [1]
输出：0
```

- 提示：

```
1 <= nums.length <= 104
-105 <= nums[i] <= 105
```

- 解答

```
package 数组操作.q581_最短无序连续子数组;

import java.util.Arrays;

/**
 * 利用排序 o(n*log(n))
 */
public class Solution {

    public int findUnsortedSubarray(int[] nums) {
        if (nums == null || nums.length < 1) {
            return 0;
        }

        int[] cloneNums = nums.clone();
        Arrays.sort(nums);

        int begin = Integer.MAX_VALUE;
        int end = 0;
        for (int i = 0; i < nums.length; i++) {
            if (nums[i] != cloneNums[i]) {
                begin = Math.min(begin, i);
                end = Math.max(end, i);
            }
        }
        return Math.max(end - begin + 1, 0);
    }

    public static void main(String[] args) {
        new Solution().findUnsortedSubarray(new int[]{2, 6, 4, 8, 10, 9, 15});
    }
}
```

#### 6.q384\_打乱数组

- 问题

* 384.打乱数组
  给你一个整数数组 nums ，
  设计算法来打乱一个没有重复元素的数组。

实现 Solution class:

```
Solution(int[] nums) 使用整数数组 nums 初始化对象
int[] reset() 重设数组到它的初始状态并返回
int[] shuffle() 返回数组随机打乱后的结果
```

- 示例：

```
输入
["Solution", "shuffle", "reset", "shuffle"]
[[[1, 2, 3]], [], [], []]
输出
[null, [3, 1, 2], [1, 2, 3], [1, 3, 2]]

解释
Solution solution = new Solution([1, 2, 3]);
solution.shuffle();    // 打乱数组 [1,2,3] 并返回结果。任何 [1,2,3]的排列返回的概率应该相同。例如，返回 [3, 1, 2]
solution.reset();      // 重设数组到它的初始状态 [1, 2, 3] 。返回 [1, 2, 3]
solution.shuffle();    // 随机返回数组 [1, 2, 3] 打乱后的结果。例如，返回 [1, 3, 2]
```

- 提示：

```
1 <= nums.length <= 200
-106 <= nums[i] <= 106
nums 中的所有元素都是 唯一的
最多可以调用 5 * 104 次 reset 和 shuffle
```

- 解答

```
package 数组操作.q384_打乱数组;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 洗牌算法 o(n)
 */
public class Solution {
    private int[] array;
    private int[] original;

    private Random rand = new Random();

    private List<Integer> getArrayCopy() {
        List<Integer> asList = new ArrayList<>();
        for (int i = 0; i < array.length; i++) {
            asList.add(array[i]);
        }
        return asList;
    }

    public Solution(int[] nums) {
        array = nums;
        original = nums.clone();
    }

    public int[] reset() {
        array = original;
        original = original.clone();
        return array;
    }

    public int[] shuffle() {
        List<Integer> aux = getArrayCopy();

        for (int i = 0; i < array.length; i++) {
            int removeIdx = rand.nextInt(aux.size());
            array[i] = aux.get(removeIdx);
            aux.remove(removeIdx);
        }

        return array;
    }
}
```

### 18.树的遍历

#### 1.q145\_二叉树的后序遍历

- 问题

* 145.二叉树的后序遍历

给定一个二叉树，返回它的 后序 遍历。

- 示例:

```
输入: [1,null,2,3]
   1
    \
     2
    /
   3

输出: [3,2,1]
```

- 进阶

递归算法很简单，你可以通过迭代算法完成吗？

- 解答

```
package 树的遍历.q145_二叉树的后序遍历;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 * 非递归法 o(n)
 */
public class Solution {
    public List<Integer> postorderTraversal(TreeNode root) {
        Stack<TreeNode> stack = new Stack<>();
        LinkedList<Integer> rs = new LinkedList<>();
        if (root == null) {
            return rs;
        }
        stack.push(root);

        while (!stack.empty()) {
            TreeNode temp = stack.pop();
            rs.addFirst(temp.val);
            if (temp.left != null) {
                stack.push(temp.left);
            }
            if (temp.right != null) {
                stack.push(temp.right);
            }
        }
        return rs;
    }
}
```

#### 2.q102\_二叉树的层次遍历

- 问题

* 102.二叉树的层序遍历

给你一个二叉树，请你返回其按 层序遍历 得到的节点值。
（即逐层地，从左到右访问所有节点）。

- 示例：

```
二叉树：[3,9,20,null,null,15,7],

    3
   / \
  9  20
    /  \
   15   7
返回其层序遍历结果：

[
  [3],
  [9,20],
  [15,7]
]
```

- 参考

作者：LeetCode-Solution
链接：https://leetcode-cn.com/problems/binary-tree-level-order-traversal/solution/er-cha-shu-de-ceng-xu-bian-li-by-leetcode-solution/
来源：力扣（LeetCode）
著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。

- 解答

```
package 树的遍历.q102_二叉树的层次遍历;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * 利用队列迭代 o(n)
 *
 */
public class Solution {
    public List<List<Integer>> levelOrder(TreeNode root) {

        List<List<Integer>> levels = new ArrayList<>();
        if (root == null) {
            return levels;
        }

        Queue<TreeNode> queue = new LinkedList<>();
        queue.add(root);
        int level = 0;
        while (!queue.isEmpty()) {

            levels.add(new ArrayList<>());

            int levelLength = queue.size();

            for (int i = 0; i < levelLength; ++i) {
                TreeNode node = queue.remove();
                levels.get(level).add(node.val);

                if (node.left != null) {
                    queue.add(node.left);
                }
                if (node.right != null) {
                    queue.add(node.right);
                }
            }
            level++;
        }
        return levels;
    }
}
```

#### 3.q110\_平衡二叉树

##### 1.f2

- 问题

* 解答

```
package 树的遍历.q110_平衡二叉树.f2;

/**
 * 从底至顶遍历 o(n)
 */
public class Solution {

    public boolean isBalanced(TreeNode root) {
        return depth(root) != -1;
    }

    private int depth(TreeNode root) {
        if (root == null) {
            return 0;
        }
        int left = depth(root.left);
        if (left == -1) {
            return -1;
        }
        int right = depth(root.right);
        if (right == -1) {
            return -1;
        }
        return Math.abs(left - right) < 2 ? Math.max(left, right) + 1 : -1;
    }
}
```

- 问题

* 110.平衡二叉树

给定一个二叉树，判断它是否是高度平衡的二叉树。
本题中，一棵高度平衡二叉树定义为：
一个二叉树每个节点 的左右两个子树的高度差的绝对值不超过 1 。

- 示例 1：

```
输入：root = [3,9,20,null,null,15,7]
输出：true
```

- 示例 2：

```
输入：root = [1,2,2,3,3,null,null,4,4]
输出：false
```

- 示例 3：

```
输入：root = []
输出：true
```

- 提示：

```
树中的节点数在范围 [0, 5000] 内
-104 <= Node.val <= 104
```

- 参考

作者：LeetCode-Solution
链接：https://leetcode-cn.com/problems/balanced-binary-tree/solution/ping-heng-er-cha-shu-by-leetcode-solution/
来源：力扣（LeetCode）
著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。

- 解答

```

```

#### 4.q144\_二叉树的前序遍历

- 问题

* 144.二叉树的前序遍历

给你二叉树的根节点 root ，返回它节点值的 前序 遍历。

- 示例 1：

```
输入：root = [1,null,2,3]
输出：[1,2,3]
```

- 示例 2：

```
输入：root = []
输出：[]
```

- 示例 3：

```
输入：root = [1]
输出：[1]
```

- 示例 4：

```
输入：root = [1,2]
输出：[1,2]
```

- 示例 5：

```
输入：root = [1,null,2]
输出：[1,2]
```

- 提示：

```
树中节点数目在范围 [0, 100] 内
-100 <= Node.val <= 100


进阶：递归算法很简单，你可以通过迭代算法完成吗？
```

- 解答

```
package 树的遍历.q144_二叉树的前序遍历;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * 非递归法 o(n)
 */
public class Solution {
    public List<Integer> preorderTraversal(TreeNode root) {
        List<Integer> rs = new ArrayList<>();
        Stack<TreeNode> stack = new Stack<>();
        while (!stack.empty() || root != null) {
            while (root != null) {
                rs.add(root.val);
                stack.push(root);
                root = root.left;
            }
            root = stack.pop();
            root = root.right;
        }
        return rs;
    }

    public static void main(String[] args) {
        TreeNode root = new TreeNode(1);
        TreeNode t1 = new TreeNode(2);
        root.right = t1;
        TreeNode t2 = new TreeNode(3);
        t1.left = t2;
        new Solution().preorderTraversal(root);
    }
}
```

#### 5.q94\_二叉树的中序遍历

##### 1.f2

- 问题

* 解答

```
package 树的遍历.q94_二叉树的中序遍历.f2;

import java.util.ArrayList;
import java.util.List;

class Solution {
    public List<Integer> inorderTraversal(TreeNode root) {
        List<Integer> res = new ArrayList<Integer>();
        inorder(root, res);
        return res;
    }

    public void inorder(TreeNode root, List<Integer> res) {
        if (root == null) {
            return;
        }
        inorder(root.left, res);
        res.add(root.val);
        inorder(root.right, res);
    }
}
```

- 问题

* 94. 二叉树的中序遍历

给定一个二叉树的根节点 root ，返回它的 中序 遍历。

- 示例 1：

```
输入：root = [1,null,2,3]
输出：[1,3,2]
```

- 示例 2：

```
输入：root = []
输出：[]
```

- 示例 3：

```
输入：root = [1]
输出：[1]
```

- 示例 4：

```
输入：root = [1,2]
输出：[2,1]
```

- 示例 5：

```
输入：root = [1,null,2]
输出：[1,2]
```

- 提示：

```
树中节点数目在范围 [0, 100] 内
-100 <= Node.val <= 100


进阶: 递归算法很简单，你可以通过迭代算法完成吗？
```

- 参考

作者：LeetCode-Solution
链接：https://leetcode-cn.com/problems/binary-tree-inorder-traversal/solution/er-cha-shu-de-zhong-xu-bian-li-by-leetcode-solutio/
来源：力扣（LeetCode）
著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。

- 解答

```

```

### 19.hash 相关

#### 1.q387\_字符串中的第一个唯一字符

- 问题

* 387. 字符串中的第一个唯一字符

给定一个字符串，找到它的第一个不重复的字符，并返回它的索引。如果不存在，则返回 -1。

- 示例：

```
s = "leetcode"
返回 0

s = "loveleetcode"
返回 2
```

- 提示：

你可以假定该字符串只包含小写字母。

- 解答

```
package hash相关.q387_字符串中的第一个唯一字符;

import java.util.HashMap;

/**
 * Hash o(n)
 */
public class Solution {

    public int firstUniqChar(String s) {
        HashMap<Character, Integer> count = new HashMap<>();
        int n = s.length();
        //统计次数
        for (int i = 0; i < n; i++) {
            char c = s.charAt(i);
            count.put(c, count.getOrDefault(c, 0) + 1);
        }
        //

        for (int i = 0; i < n; i++) {
            if (count.get(s.charAt(i)) == 1) {
                return i;
            }
        }
        return -1;
    }

    public static void main(String[] args) {
        Solution solution = new Solution();
        System.out.println(solution.firstUniqChar("abcdeab"));
    }
}
```

#### 2.q1\_两数之和

##### 1.f2

- 问题

* 解答

```
package hash相关.q1_两数之和.f2;

import java.util.HashMap;
import java.util.Map;

/**
 * 一遍hash o(n)
 */
public class Solution {
    public int[] twoSum(int[] nums, int target) {
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < nums.length; i++) {
            if (map.containsKey(target - nums[i])) {
                return new int[]{map.get(target - nums[i]), i};
            }
            map.put(nums[i], i);
        }
        return null;
    }
}
```

- 问题

* 1. 两数之和

给定一个整数数组 nums 和一个整数目标值 target，
请你在该数组中找出 和为目标值 的那 两个 整数，并返回它们的数组下标。
你可以假设每种输入只会对应一个答案。但是，数组中同一个元素不能使用两遍。
你可以按任意顺序返回答案。

- 示例 1：

```
输入：nums = [2,7,11,15], target = 9
输出：[0,1]
解释：因为 nums[0] + nums[1] == 9 ，返回 [0, 1] 。
示例 2：


输入：nums = [3,2,4], target = 6
输出：[1,2]
示例 3：

输入：nums = [3,3], target = 6
输出：[0,1]
```

- 提示：

```
2 <= nums.length <= 103
-109 <= nums[i] <= 109
-109 <= target <= 109
只会存在一个有效答案
```

- 解答

```

```
