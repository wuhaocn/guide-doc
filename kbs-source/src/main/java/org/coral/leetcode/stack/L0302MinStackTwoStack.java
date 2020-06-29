package org.coral.leetcode.stack;

import java.util.Stack;

public class L0302MinStackTwoStack {

    private Stack<Integer> stack;
    private Stack<Integer> minStack;

    public L0302MinStackTwoStack() {
        stack = new Stack<>();
        minStack = new Stack<>();
    }

    public void push(int x) {
        //原生栈
        stack.push(x);
        //最小栈
        if (minStack.size() <= 0) {
            minStack.push(x);
        } else {
            //x比当前最小值小
            if (minStack.peek() > x) {
                minStack.push(x);
            } else {
                minStack.push(minStack.peek());
            }
        }

    }

    public void pop() {
        minStack.pop();
        stack.pop();
    }

    public int top() {
        return stack.peek();
    }

    public int getMin() {
        return minStack.peek();
    }

    public static void main(String[] args) {
        L0302MinStackTwoStack minStack = new L0302MinStackTwoStack();
        minStack.push(-2);
        minStack.push(0);
        minStack.push(-3);
        System.out.println(minStack.getMin());   //--> 返回 -3.
        minStack.pop();
        System.out.println(minStack.top());      //--> 返回 0.
        System.out.println(minStack.getMin());   //--> 返回 -2.
    }

}
