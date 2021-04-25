package 二叉搜索树相关.q98_验证二叉搜索树.f6;

/**
 * 作者：LeetCode-Solution
 * 链接：https://leetcode-cn.com/problems/validate-binary-search-tree/solution/yan-zheng-er-cha-sou-suo-shu-by-leetcode-solution/
 * 来源：力扣（LeetCode）
 * 著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。
 *
 * 思路和算法
 *
 要解决这道题首先我们要了解二叉搜索树有什么性质可以给我们利用，由题目给出的信息我们可以知道：如果该二叉树的左子树不为空，
 则左子树上所有节点的值均小于它的根节点的值； 若它的右子树不空，则右子树上所有节点的值均大于它的根节点的值；它的左右子树也为二叉搜索树。

 这启示我们设计一个递归函数 helper(root, lower, upper) 来递归判断，函数表示考虑以 root 为根的子树，
 判断子树中所有节点的值是否都在 (l,r)(l,r) 的范围内（注意是开区间）。如果 root 节点的值 val 不在 (l,r)(l,r) 的范围内说明不满足条件直接返回，
 否则我们要继续递归调用检查它的左右子树是否满足，如果都满足才说明这是一棵二叉搜索树。
 那么根据二叉搜索树的性质，在递归调用左子树时，我们需要把上界 upper 改为 root.val，即调用 helper(root.left, lower, root.val)，
 因为左子树里所有节点的值均小于它的根节点的值。同理递归调用右子树时，我们需要把下界 lower 改为 root.val，即调用 helper(root.right, root.val, upper)。

 函数递归调用的入口为 helper(root, -inf, +inf)， inf 表示一个无穷大的值。
 *
 *
 复杂度分析

 时间复杂度 : O(n)O(n)，其中 nn 为二叉树的节点个数。在递归调用的时候二叉树的每个节点最多被访问一次，因此时间复杂度为 O(n)O(n)。

 空间复杂度 : O(n)O(n)，其中 nn 为二叉树的节点个数。递归函数在递归过程中需要为每一层递归函数分配栈空间，
 所以这里需要额外的空间且该空间取决于递归的深度，即二叉树的高度。最坏情况下二叉树为一条链，树的高度为 nn ，
 递归最深达到 nn 层，故最坏情况下空间复杂度为 O(n)O(n) 。

 *
 */
class Solution {
    /**
     * 是否为二叉搜索树
     * @param root
     * @return
     */
    public boolean isValidBST(TreeNode root){
        return isValidBST(root, Long.MIN_VALUE, Long.MAX_VALUE);
    }

    /***
     * 节点的至如果小于最小，大于等于最大就不行
     * @param root
     * @param min
     * @param max
     * @return
     */
    public boolean isValidBST(TreeNode root, long min, long max){
        //1.已经是根节点则满足
        if (root == null){
            return true;
        }
        //2.判断当前节点
        if (root.val <= min || root.val >= max){
            return false;
        }
        //3.判断左右子树[左节点应该小于当前值，大于最小值   || 右节点应该大于当前值，小于最大值]
        return isValidBST(root.left, min, root.val) && isValidBST(root.right, root.val, max);
    }
    public static void main(String[] args) {
        TreeNode root = new TreeNode(11);
        TreeNode n1 = new TreeNode(5);
        TreeNode n2 = new TreeNode(15);
        root.left = n1;
        root.right = n2;
        TreeNode n3 = new TreeNode(14);
        TreeNode n4 = new TreeNode(16);
        n2.left = n3;
        n2.right = n4;
        System.out.println(new Solutionw().isValidBST(root));
    }
}

