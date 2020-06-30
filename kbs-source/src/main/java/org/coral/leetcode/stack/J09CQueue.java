package org.coral.leetcode.stack;

import java.util.Stack;

/**
 * push 1 2 3 4
 ｜ 4 ｜   ｜ 1 ｜
 ｜ 3 ｜   ｜ 2 ｜
 ｜ 2 ｜   ｜ 3 ｜
 ｜ 1 ｜   ｜ 4 ｜
 *
 */
public class J09CQueue {


    public J09CQueue() {

    }
    private Stack<Integer> stack = new Stack<>();
    private Stack<Integer> stackQueue = new Stack<>();

    public void appendTail(int value) {
        stack.push(value);
    }
    
    public int deleteHead() {
        // 如果第二个栈为空,优先处理队列栈，队列栈符合先进先出
        if (stackQueue.isEmpty()) {
            while (!stack.isEmpty()) {
                stackQueue.push(stack.pop());
            }
        }
        if (stackQueue.isEmpty()) {
            return -1;
        } else {
            return stackQueue.pop();
        }
    }


    public static void main(String[] args) {
        J09CQueue j09CQueue = new J09CQueue();
        j09CQueue.appendTail(1);
        j09CQueue.appendTail(2);
        System.out.println(j09CQueue.deleteHead());
        j09CQueue.appendTail(3);
        j09CQueue.appendTail(4);
        System.out.println(j09CQueue.deleteHead());
        System.out.println(j09CQueue.deleteHead());
        System.out.println(j09CQueue.deleteHead());
    }
}

/**
 * Your CQueue object will be instantiated and called as such:
 * CQueue obj = new CQueue();
 * obj.appendTail(value);
 * int param_2 = obj.deleteHead();
 */