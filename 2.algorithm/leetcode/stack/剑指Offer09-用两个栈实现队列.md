## 面试题 剑指Offer09用两个栈实现队列.md
### 问题
用两个栈实现一个队列。队列的声明如下，请实现它的两个函数 appendTail 和 deleteHead ，
分别完成在队列尾部插入整数和在队列头部删除整数的功能。
(若队列中没有元素，deleteHead 操作返回 -1 )

### 示例
```
示例 1：

输入：
["CQueue","appendTail","deleteHead","deleteHead"]
[[],[3],[],[]]
输出：[null,null,3,-1]
示例 2：

输入：
["CQueue","deleteHead","appendTail","appendTail","deleteHead","deleteHead"]
[[],[],[5],[2],[],[]]
输出：[null,-1,null,null,5,2]
提示：

1 <= values <= 10000
最多会对 appendTail、deleteHead 进行 10000 次调用
```
### 官方解说

保证stackQueue没有数据

![](https://assets.leetcode-cn.com/solution-static/jianzhi_09/jianzhi_9.gif)

### 实现
```
public class J09CQueue {
    private Stack<Integer> stack = new Stack<>();
    private Stack<Integer> stackQueue = new Stack<>();

    public J09CQueue() {

    }

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
```
来源：力扣（LeetCode）
链接：https://leetcode-cn.com/problems/yong-liang-ge-zhan-shi-xian-dui-lie-lcof
著作权归领扣网络所有。商业转载请联系官方授权，非商业转载请注明出处。