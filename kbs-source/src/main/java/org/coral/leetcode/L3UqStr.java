package org.coral.leetcode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class L3UqStr {
    public static void main(String[] args) {
        L3UqStr l3UqStr = new L3UqStr();
        System.out.println(l3UqStr.maxUnqStr("1"));
        System.out.println(l3UqStr.lengthOfLongestSubstringHash("12345617abcdes"));
    }
    public int maxUnqStr(String str){
        int maxUq = 0;
        int i = 0;
        HashMap<Character, Integer> hashMap = new HashMap<>();
        for (int j = 0; j < str.length(); j++) {
            char character = str.charAt(j);
            if (hashMap.containsKey(character)){
                i = hashMap.get(character) > i ? hashMap.get(character): i;

            }
            hashMap.put(character, j + 1);
            maxUq = Math.max(maxUq, j - i + 1);
        }
        return maxUq;
    }


    /**
     * 有回溯
     *
     * @param s
     * @return
     */
    public int lengthOfLongestSubstring(String s) {
        int n = s.length();
        Set<Character> set = new HashSet<>();
        int ans = 0, i = 0, j = 0;
        while (i < n && j < n) {
            // try to extend the range [i, j]
            if (!set.contains(s.charAt(j))){
                set.add(s.charAt(j++));
                ans = Math.max(ans, j - i);
            }
            else {
                set.remove(s.charAt(i++));
            }
        }
        return ans;
    }

    /**
     * 减少回溯
     *
     * @param s
     * @return
     */
    public int lengthOfLongestSubstringHash(String s) {
        int n = s.length(), ans = 0;
        Map<Character, Integer> map = new HashMap<>(); // current index of character
        // try to extend the range [i, j]
        for (int j = 0, i = 0; j < n; j++) {
            if (map.containsKey(s.charAt(j))) {
                i = Math.max(map.get(s.charAt(j)), i);
            }
            ans = Math.max(ans, j - i + 1);
            map.put(s.charAt(j), j + 1);
        }
        return ans;
    }

    /**
     * 减少判断索引
     * @param s
     * @return
     */
    public int lengthOfLongestSubstringChar(String s) {
        int n = s.length(), ans = 0;
        int[] index = new int[128]; // current index of character
        // try to extend the range [i, j]
        for (int j = 0, i = 0; j < n; j++) {
            i = Math.max(index[s.charAt(j)], i);
            ans = Math.max(ans, j - i + 1);
            index[s.charAt(j)] = j + 1;
        }
        return ans;
    }





}
/**
 *
 作者：LeetCode
 链接：https://leetcode-cn.com/problems/longest-substring-without-repeating-characters/solution/wu-zhong-fu-zi-fu-de-zui-chang-zi-chuan-by-leetcod/
 来源：力扣（LeetCode）
 著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。
 */
