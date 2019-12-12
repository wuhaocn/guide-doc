package org.coral.leetcode.stack;

import java.util.HashMap;
import java.util.Stack;

public class L20EffectiveBrackets {
    public static void main(String[] args) {
        L20EffectiveBrackets l20EffectiveBrackets = new L20EffectiveBrackets();
        System.out.println(l20EffectiveBrackets.isValid("("));
        System.out.println(l20EffectiveBrackets.isValid("()[]{}"));
//        System.out.println(l20EffectiveBrackets.isValid("{[]}"));
    }

    public boolean isValid(String s) {
        HashMap<Character, Character> mappings = new HashMap<>();
        mappings.put(')', '(');
        mappings.put(']', '[');
        mappings.put('}', '{');
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
