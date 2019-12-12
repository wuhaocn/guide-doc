package org.coral.leetcode.stack;

import java.util.HashMap;
import java.util.Stack;

public class L32LongEffectiveBrackets {
    public static void main(String[] args) {
        L32LongEffectiveBrackets l20EffectiveBrackets = new L32LongEffectiveBrackets();
        System.out.println(l20EffectiveBrackets.longestValidParentheses("("));
        System.out.println(l20EffectiveBrackets.longestValidParentheses("()()()"));
    }

    /**
     *  动态规划得最大值
     * @param s
     * @return
     */
    public int longestValidParentheses(String s) {
        int maxans = 0;
        int dp[] = new int[s.length()];
        for (int i = 1; i < s.length(); i++) {
            if (s.charAt(i) == ')') {
                if (s.charAt(i - 1) == '(') {
                    dp[i] = (i >= 2 ? dp[i - 2] : 0) + 2;
                } else if (i - dp[i - 1] > 0 && s.charAt(i - dp[i - 1] - 1) == '(') {
                    dp[i] = dp[i - 1] + ((i - dp[i - 1]) >= 2 ? dp[i - dp[i - 1] - 2] : 0) + 2;
                }
                maxans = Math.max(maxans, dp[i]);
            }
        }
        return maxans;
    }


    /**
     *  暴力得最大值
     * @param s
     * @return
     */
    public int qlongestValidParentheses(String s) {
        int maxLen = 0;

        for (int i = 0; i < s.length(); i++) {
            for (int j = i + 2; j <= s.length(); j+=2){
                if (isValid(s.substring(i, j))){
                    maxLen = Math.max(maxLen, j - i);
                }
            }
        }
        return maxLen;
    }

    /**
     * 栈解法判断是否为有效括号
     *
     * @param s
     * @return
     */
    public boolean isValid(String s) {
        HashMap<Character, Character> mappings = new HashMap<>();
        mappings.put(')', '(');
        Stack<Character> stringStack = new Stack<>();
        for (int i = 0; i < s.length(); i++) {
            Character cur = s.charAt(i);
            if (mappings.containsKey(cur)){
                if (stringStack.isEmpty()){
                    return false;
                }
                if (mappings.get(cur) == stringStack.peek()){
                    stringStack.pop();
                } else {
                    return false;
                }
            } else {
                stringStack.push(cur);
            }

        }
        return stringStack.isEmpty();
    }
}
