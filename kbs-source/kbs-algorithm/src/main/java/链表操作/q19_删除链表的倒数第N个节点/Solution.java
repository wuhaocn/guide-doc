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
