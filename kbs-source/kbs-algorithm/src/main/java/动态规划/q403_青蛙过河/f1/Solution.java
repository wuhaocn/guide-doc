package 动态规划.q403_青蛙过河.f1;

import java.util.Arrays;

/**
 * 方法一：记忆化搜索 + 二分查找
 * 思路及算法
 * 最直接的想法是使用深度优先搜索的方式尝试所有跳跃方案，直到我们找到一组可行解为止。
 * 但是不加优化的该算法的时间复杂度在最坏情况下是指数级的，因此考虑优化。
 * 注意到当青蛙每次能够跳跃的距离仅取决于青蛙的「上一次跳跃距离」。
 * 而青蛙此后能否到达终点，只和它「现在所处的石子编号」以及「上一次跳跃距离」有关。
 * 因此我们可以将这两个维度综合记录为一个状态。使用记忆化搜索的方式优化时间复杂度。
 * 具体地，当青蛙位于第 ii 个石子，上次跳跃距离为 \textit{lastDis}lastDis 时，
 * 它当前能够跳跃的距离范围为 [\textit{lastDis}-1,\textit{lastDis}+1][lastDis−1,lastDis+1]。
 * 我们需要分别判断这三个距离对应的三个位置是否存在石子。
 * 注意到给定的石子列表为升序，所以我们可以利用二分查找来优化查找石子的时间复杂度。
 * 每次我们找到了符合要求的位置，我们就尝试进行一次递归搜索即可。
 * 为了优化编码，我们可以认为青蛙的初始状态为：「现在所处的石子编号」为 00（石子从 00 开始编号），
 * 「上一次跳跃距离」为 00（这样可以保证青蛙的第一次跳跃距离为 11）
 * <p>
 *
 */
class Solution {
    private Boolean[][] rec;

    public boolean canCross(int[] stones) {
        int n = stones.length;
        rec = new Boolean[n][n];
        return dfs(stones, 0, 0);
    }

    private boolean dfs(int[] stones, int i, int lastDis) {
        if (i == stones.length - 1) {
            return true;
        }
        if (rec[i][lastDis] != null) {
            return rec[i][lastDis];
        }

        for (int curDis = lastDis - 1; curDis <= lastDis + 1; curDis++) {
            if (curDis > 0) {
                int j = Arrays.binarySearch(stones,
                        i + 1, stones.length, curDis + stones[i]);
                if (j >= 0 && dfs(stones, j, curDis)) {
                    return rec[i][lastDis] = true;
                }
            }
        }
        return rec[i][lastDis] = false;
    }
}

