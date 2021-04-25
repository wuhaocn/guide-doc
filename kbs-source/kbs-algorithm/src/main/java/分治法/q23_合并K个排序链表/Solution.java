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
