package org.coral.leetcode;

public class L668MultiplicationTable {
    public static void main(String[] args) {
        L668MultiplicationTable l668MultiplicationTable = new L668MultiplicationTable();
        System.out.println(l668MultiplicationTable.findKthNumber(3, 3, 5));
    }

    /**
     * 几乎每一个人都用 乘法表。但是你能在乘法表中快速找到第k小的数字吗？
     *
     * 给定高度m 、宽度n 的一张 m * n的乘法表，以及正整数k，你需要返回表中第k 小的数字。
     *
     */
    /**
     * 1。二分法查找处于k位置的值
     * 2。k的确定循环遍历
     * @param m
     * @param n
     * @param k
     * @return
     */
    public int findKthNumber(int m, int n, int k) {
        int lo = 1; // 结果值
        int hi = m * n + 1; //最大值 + 1
        int mid; //中间值
        int count; //排序值
        while (lo < hi) {
            mid = lo + (hi - lo) / 2;
            count = 0;
            for (int i = 1; i <= m; i++) {
                count += (mid/i > n ? n : mid/i);
            }
            //如果
            if (count >= k) hi = mid;
            else lo = mid + 1;
        }
        return lo;
    }
}
/**

 作者：caipengbo
 链接：https://leetcode-cn.com/problems/kth-smallest-number-in-multiplication-table/solution/chao-ji-jian-dan-de-javaer-fen-fa-by-caipengbo/
 来源：力扣（LeetCode）
 著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。
 */