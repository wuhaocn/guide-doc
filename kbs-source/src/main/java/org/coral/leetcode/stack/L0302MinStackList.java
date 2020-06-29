package org.coral.leetcode.stack;

import java.util.Stack;

/**
 * header[min, value]
 * node[min,value]->header[min.value]
 */
public class L0302MinStackList {

    private Node header;

    public L0302MinStackList() {
        header = null;
    }

    public void push(int x) {

        //为空
        if (header == null){
            header = new Node();
            header.min = x;
            header.value = x;
        } else {
            Node node = new Node();
            if (getMin() > x){
                node.min = x;
            } else {
                node.min = header.min;
            }
            node.value = x;
            header.next = node;
            header = node;
        }

    }

    public void pop() {
        if (header != null){
            header = header.next;
        }
    }

    public int top() {
        if (header == null){return 0;}
        return header.value;
    }

    public int getMin() {
        if (header == null){return 0;}
        return header.min;
    }
    class Node{
        private Node next;
        private int min;
        private int value;
    }
    public static void main(String[] args) {
        L0302MinStackList minStack = new L0302MinStackList();
        minStack.push(-2);
        minStack.push(0);
        minStack.push(-3);
        System.out.println(minStack.getMin());   //--> 返回 -3.
        minStack.pop();
        System.out.println(minStack.top());      //--> 返回 0.
        System.out.println(minStack.getMin());   //--> 返回 -2.
    }

}

