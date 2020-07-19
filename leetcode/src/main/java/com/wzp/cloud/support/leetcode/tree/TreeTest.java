package com.wzp.cloud.support.leetcode.tree;

public class TreeTest {


    /**
     * 二叉树深度
     * <p>
     * 给定二叉树 [3,9,20,null,null,15,7]，
     * 3
     * / \
     * 9  20
     * /  \
     * 15   7
     * 返回它的最大深度 3 。
     */
    public class BinaryTree {
        public class TreeNode {
            int val;
            TreeNode left;
            TreeNode right;

            TreeNode(int x) {
                val = x;
            }
        }

        class Solution {
            public int maxDepth(TreeNode root) {
                if (root == null) {
                    return 0;
                } else {
                    int left_height = maxDepth(root.left);
                    int right_height = maxDepth(root.right);
                    return java.lang.Math.max(left_height, right_height) + 1;
                }
            }
        }

    }
}
