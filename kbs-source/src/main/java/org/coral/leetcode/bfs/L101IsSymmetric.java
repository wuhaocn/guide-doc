package org.coral.leetcode.bfs;

import javax.swing.tree.TreeNode;

/***
 *     作者：LeetCode
 *     链接：https://leetcode-cn.com/problems/symmetric-tree/solution/dui-cheng-er-cha-shu-by-leetcode/
 *     来源：力扣（LeetCode）
 *     著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。
 */
public class L101IsSymmetric {

//    public boolean isSymmetric(TreeNode root) {
//        return isMirror(root, root);
//    }
//
//    public boolean isMirror(TreeNode t1, TreeNode t2) {
//        if (t1 == null && t2 == null) return true;
//        if (t1 == null || t2 == null) return false;
//        return (t1.val == t2.val)
//                && isMirror(t1.right, t2.left)
//                && isMirror(t1.left, t2.right);
//    }
//
//
//    public boolean isSymmetric(TreeNode root) {
//        Queue<TreeNode> q = new LinkedList<>();
//        q.add(root);
//        q.add(root);
//        while (!q.isEmpty()) {
//            TreeNode t1 = q.poll();
//            TreeNode t2 = q.poll();
//            if (t1 == null && t2 == null) continue;
//            if (t1 == null || t2 == null) return false;
//            if (t1.val != t2.val) return false;
//            q.add(t1.left);
//            q.add(t2.right);
//            q.add(t1.right);
//            q.add(t2.left);
//        }
//        return true;
//    }



}
