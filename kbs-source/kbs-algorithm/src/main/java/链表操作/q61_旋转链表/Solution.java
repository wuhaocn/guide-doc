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
