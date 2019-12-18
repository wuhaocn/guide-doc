package org.coral.leetcode;

public class L440WordK {
    public static void main(String[] args) {
        L440WordK l440WordK = new L440WordK();
        ;
        System.out.println(l440WordK.findKthNumber(10, 5));
        System.out.println(l440WordK.findKthNumber(10, 5));
    }
    public int findKthNumber(int n, int k) {

        int curr=1;
        k-=1;
        while(k>0){

            long step=calstep(n,curr,curr+1);

            if(k<step){//在这个范围内
                curr*=10;//移动一层
                k--;
            }else{//移动相邻的下一个节点
                k-=step;
                curr=curr+1;
            }

        }
        return curr;
    }
    public long calstep(int n,long n1,long n2){
        int ans=0;
        while(n1<=n){
            ans+=Math.min(n+1,n2)-n1;
            n1*=10;
            n2*=10;
        }
        return ans;
    }
}
/**
 *
 *
 作者：jiaxin-2
 链接：https://leetcode-cn.com/problems/k-th-smallest-in-lexicographical-order/solution/javaqian-zhui-shu-qing-xi-tu-jie-you-wen-ti-huan-y/
 来源：力扣（LeetCode）
 著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。
 */
