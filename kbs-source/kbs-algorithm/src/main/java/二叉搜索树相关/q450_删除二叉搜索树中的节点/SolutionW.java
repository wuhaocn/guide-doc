package 二叉搜索树相关.q450_删除二叉搜索树中的节点;

/**
 * 这里有三种可能的情况：
 * <p>
 * 要删除的节点为叶子节点，可以直接删除。
 * <p>
 * <p>
 * 要删除的几点不是叶子节点且拥有右节点，则该节点可以由该节点的后继节点进行替代，该后继节点位于右子树中较低的位置。
 * 然后可以从后继节点的位置递归向下操作以删除后继节点。
 * <p>
 * <p>
 * 要删除的节点不是叶子节点，且没有右节点但是有左节点。这意味着它的后继节点在它的上面，但是我们并不想返回。
 * 我们可以使用它的前驱节点进行替代，然后再递归的向下删除前驱节点。
 * <p>
 * <p>
 * 算法：
 * <p>
 * 如果 key > root.val，说明要删除的节点在右子树，root.right = deleteNode(root.right, key)。
 * 如果 key < root.val，说明要删除的节点在左子树，root.left = deleteNode(root.left, key)。
 * 如果 key == root.val，则该节点就是我们要删除的节点，则：
 * 如果该节点是叶子节点，则直接删除它：root = null。
 * 如果该节点不是叶子节点且有右节点，则用它的后继节点的值替代 root.val = successor.val，然后删除后继节点。
 * 如果该节点不是叶子节点且只有左节点，则用它的前驱节点的值替代 root.val = predecessor.val，然后删除前驱节点。
 * 返回 root。
 */
public class SolutionW {
    /*
        One step right and then always left
   */
    public int successor(TreeNode root) {
        root = root.right;
        while (root.left != null) root = root.left;
        return root.val;
    }

    /*
    One step left and then always right
    */
    public int predecessor(TreeNode root) {
        root = root.left;
        while (root.right != null) root = root.right;
        return root.val;
    }

    public TreeNode deleteNode(TreeNode root, int key) {
        if (root == null) return null;

        // delete from the right subtree
        if (key > root.val) root.right = deleteNode(root.right, key);
            // delete from the left subtree
        else if (key < root.val) root.left = deleteNode(root.left, key);
            // delete the current node
        else {
            // the node is a leaf
            if (root.left == null && root.right == null) root = null;
                // the node is not a leaf and has a right child
            else if (root.right != null) {
                root.val = successor(root);
                root.right = deleteNode(root.right, root.val);
            }
            // the node is not a leaf, has no right child, and has a left child
            else {
                root.val = predecessor(root);
                root.left = deleteNode(root.left, root.val);
            }
        }
        return root;
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

        new SolutionW().deleteNode(root, 1);
    }
}
