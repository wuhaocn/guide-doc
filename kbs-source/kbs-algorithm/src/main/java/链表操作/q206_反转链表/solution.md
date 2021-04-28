- 方法一：迭代

假设链表为 ![1\rightarrow2\rightarrow3\rightarrow\varnothing ](./p__1_rightarrow_2_rightarrow_3_rightarrow_varnothing_.png) ，我们想要把它改成 ![\varnothing\leftarrow1\leftarrow2\leftarrow3 ](./p__varnothing_leftarrow_1_leftarrow_2_leftarrow_3_.png) 。

在遍历链表时，将当前节点的 ![\textit{next} ](./p__textit{next}_.png) 指针改为指向前一个节点。由于节点没有引用其前一个节点，因此必须事先存储其前一个节点。在更改引用之前，还需要存储后一个节点。最后返回新的头引用。

```Java [sol1-Java]
class Solution {
    public ListNode reverseList(ListNode head) {
        ListNode prev = null;
        ListNode curr = head;
        while (curr != null) {
            ListNode next = curr.next;
            curr.next = prev;
            prev = curr;
            curr = next;
        }
        return prev;
    }
}
```

```JavaScript [sol1-JavaScript]
var reverseList = function(head) {
    let prev = null;
    let curr = head;
    while (curr) {
        const next = curr.next;
        curr.next = prev;
        prev = curr;
        curr = next;
    }
    return prev;
};
```

```go [sol1-Golang]
func reverseList(head *ListNode) *ListNode {
    var prev *ListNode
    curr := head
    for curr != nil {
        next := curr.Next
        curr.Next = prev
        prev = curr
        curr = next
    }
    return prev
}
```

```C++ [sol1-C++]
class Solution {
public:
    ListNode* reverseList(ListNode* head) {
        ListNode* prev = nullptr;
        ListNode* curr = head;
        while (curr) {
            ListNode* next = curr->next;
            curr->next = prev;
            prev = curr;
            curr = next;
        }
        return prev;
    }
};
```

```C [sol1-C]
struct ListNode* reverseList(struct ListNode* head) {
    struct ListNode* prev = NULL;
    struct ListNode* curr = head;
    while (curr) {
        struct ListNode* next = curr->next;
        curr->next = prev;
        prev = curr;
        curr = next;
    }
    return prev;
}
```

**复杂度分析**

- 时间复杂度：_O(n)_，其中 _n_ 是链表的长度。需要遍历链表一次。

- 空间复杂度：_O(1)_。

* 方法二：递归

递归版本稍微复杂一些，其关键在于反向工作。假设链表的其余部分已经被反转，现在应该如何反转它前面的部分？

假设链表为：
![n_1\rightarrow\ldots\rightarrown_{k-1}\rightarrown_k\rightarrown_{k+1}\rightarrow\ldots\rightarrown_m\rightarrow\varnothing ](./p__n_1rightarrow_ldots_rightarrow_n_{k-1}_rightarrow_n_k_rightarrow_n_{k+1}_rightarrow_ldots_rightarrow_n_m_rightarrow_varnothing_.png)

若从节点 _n\_{k+1}_ 到 _n_m_ 已经被反转，而我们正处于 _n_k_。

![n_1\rightarrow\ldots\rightarrown_{k-1}\rightarrown_k\rightarrown_{k+1}\leftarrow\ldots\leftarrown_m ](./p__n_1rightarrow_ldots_rightarrow_n_{k-1}_rightarrow_n_k_rightarrow_n_{k+1}_leftarrow_ldots_leftarrow_n_m_.png)

我们希望 _n\_{k+1}_ 的下一个节点指向 _n_k_。

所以，![n_k.\textit{next}.\textit{next}=n_k ](./p__n_k.textit{next}.textit{next}_=_n_k_.png) 。

需要注意的是 _n_1_ 的下一个节点必须指向 ![\varnothing ](./p__varnothing_.png) 。如果忽略了这一点，链表中可能会产生环。

```Java [sol2-Java]
class Solution {
    public ListNode reverseList(ListNode head) {
        if (head == null || head.next == null) {
            return head;
        }
        ListNode newHead = reverseList(head.next);
        head.next.next = head;
        head.next = null;
        return newHead;
    }
}
```

```JavaScript [sol2-JavaScript]
var reverseList = function(head) {
    if (head == null || head.next == null) {
        return head;
    }
    const newHead = reverseList(head.next);
    head.next.next = head;
    head.next = null;
    return newHead;
};
```

```go [sol2-Golang]
func reverseList(head *ListNode) *ListNode {
    if head == nil || head.Next == nil {
        return head
    }
    newHead := reverseList(head.Next)
    head.Next.Next = head
    head.Next = nil
    return newHead
}
```

```C++ [sol2-C++]
class Solution {
public:
    ListNode* reverseList(ListNode* head) {
        if (!head || !head->next) {
            return head;
        }
        ListNode* newHead = reverseList(head->next);
        head->next->next = head;
        head->next = nullptr;
        return newHead;
    }
};
```

```C [sol2-C]
struct ListNode* reverseList(struct ListNode* head) {
    if (head == NULL || head->next == NULL) {
        return head;
    }
    struct ListNode* newHead = reverseList(head->next);
    head->next->next = head;
    head->next = NULL;
    return newHead;
}
```

**复杂度分析**

- 时间复杂度：_O(n)_，其中 _n_ 是链表的长度。需要对链表的每个节点进行反转操作。

- 空间复杂度：_O(n)_，其中 _n_ 是链表的长度。空间复杂度主要取决于递归调用的栈空间，最多为 _n_ 层。
