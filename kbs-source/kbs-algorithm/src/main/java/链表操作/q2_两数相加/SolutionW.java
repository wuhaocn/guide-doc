package 链表操作.q2_两数相加;

public class SolutionW {


    public ListNode addTwoNumbers(ListNode l1, ListNode l2) {
        //目标链表
        ListNode lz = new ListNode(0);
        ListNode lheader = lz;

        //遍历指针
        ListNode ll1 = l1;
        ListNode ll2 = l2;

        //存储相加溢出值
        int over = 0;
        while (ll1 != null || ll2 != null || over != 0) {
            //基础相加
            ListNode tmp = new ListNode(0);
            if (ll1 != null) {
                tmp.val += ll1.val;
                ll1 = ll1.next;
            }
            if (ll2 != null) {
                tmp.val += ll2.val;
                ll2 = ll2.next;
            }

            //溢出值处理
            tmp.val += over;
            if (tmp.val >= 10) {
                over = 1;
                tmp.val = tmp.val - 10;
            } else {
                over = 0;
            }
            //头节点后移
            lz.next = tmp;
            lz = tmp;
        }
        lheader = lheader.next;
        return lheader;
    }

    public void print(ListNode listNode) {
        System.out.println();
        while (listNode != null) {
            System.out.print(listNode.val);
            listNode = listNode.next;
        }
        System.out.println();
    }

    public static void main(String[] args) {
        SolutionW solutionW = new SolutionW();
        ListNode listNode1 = new ListNode(1);
        ListNode listNode2 = new ListNode(6);
        ListNode listNode3 = new ListNode(3);
        listNode1.next = listNode2;
        listNode2.next = listNode3;

        ListNode listNode11 = new ListNode(4);
        ListNode listNode12 = new ListNode(5);
        ListNode listNode13 = new ListNode(9);
        ListNode listNode14 = new ListNode(9);
        listNode11.next = listNode12;
        listNode12.next = listNode13;
        listNode13.next = listNode14;
        solutionW.print(listNode1);
        solutionW.print(listNode11);
        Solution solution = new Solution();
        solutionW.print(solution.addTwoNumbers(listNode1, listNode11));
        solutionW.print(solutionW.addTwoNumbers(listNode1, listNode11));
    }
}
