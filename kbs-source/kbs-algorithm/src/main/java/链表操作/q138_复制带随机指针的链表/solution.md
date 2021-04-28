首先，我们来看一下有向链表：

![image.png](https://pic.leetcode-cn.com/6a547c42c3a2d05e054223a2a512844ed2bc7f424433e0d99ae20a4a191c582d-image.png)

在上图中，对于一个节点，它的 `next` 指针指向链表中的下一个节点。 `next` 指针是通常有向链表中有的部分且将所有节点 **_链接_** 起来。图中有趣的一点，也是这题有趣的一点在于 `random` 指针，正如名字所示，它可以指向链表中的任一节点也可以为空。

- 方法 1：回溯

**想法**

回溯算法的第一想法是将链表想象成一张图。链表中每个节点都有 2 个指针（图中的边）。因为随机指针给图结构添加了随机性，所以我们可能会访问相同的节点多次，这样就形成了环。

![image.png](https://pic.leetcode-cn.com/990d6483f673537e98e6162cc9e1c6c4ca32729d0d54b82bb5e46cbdaf2246b8-image.png)

上图中，我们可以看到随机指针指向了前一个节点，因此成环。我们需要考虑这种环的实现。

此方法中，我们只需要遍历整个图并拷贝它。拷贝的意思是每当遇到一个新的未访问过的节点，你都需要创造一个新的节点。遍历按照深度优先进行。我们需要在回溯的过程中记录已经访问过的节点，否则因为随机指针的存在我们可能会产生死循环。

**算法**

1. 从 `头` 指针开始遍历整个图。

   我们将链表看做一张图。下图对应的是上面的有向链表的例子，`Head` 是图的出发节点。

![image.png](https://pic.leetcode-cn.com/bd3fb0c9f6d3fdcc3bbc4afdb47183d6aaef93552df135130ea42da77aab911d-image.png)

2. 当我们遍历到某个点时，如果我们已经有了当前节点的一个拷贝，我们不需要重复进行拷贝。
3. 如果我们还没拷贝过当前节点，我们创造一个新的节点，并把该节点放到已访问字典中，即：
   `visited_dictionary[current_node] = cloned_node_for_current_node.`
4. 我们针对两种情况进行回溯调用：一个顺着 `random` 指针调用，另一个沿着 `next` 指针调用。步骤 1 中将 `random` 和 `next` 指针分别红红色和蓝色标注。然后我们分别对两个指针进行函数递归调用：

```
cloned_node_for_current_node.next = copyRandomList(current_node.next);
cloned_node_for_current_node.random = copyRandomList(current_node.random);
```

```Java []
/*
// Definition for a Node.
class Node {
    public int val;
    public Node next;
    public Node random;

    public Node() {}

    public Node(int _val,Node _next,Node _random) {
        val = _val;
        next = _next;
        random = _random;
    }
};
*/
public class Solution {
  // HashMap which holds old nodes as keys and new nodes as its values.
  HashMap<Node, Node> visitedHash = new HashMap<Node, Node>();

  public Node copyRandomList(Node head) {

    if (head == null) {
      return null;
    }

    // If we have already processed the current node, then we simply return the cloned version of
    // it.
    if (this.visitedHash.containsKey(head)) {
      return this.visitedHash.get(head);
    }

    // Create a new node with the value same as old node. (i.e. copy the node)
    Node node = new Node(head.val, null, null);

    // Save this value in the hash map. This is needed since there might be
    // loops during traversal due to randomness of random pointers and this would help us avoid
    // them.
    this.visitedHash.put(head, node);

    // Recursively copy the remaining linked list starting once from the next pointer and then from
    // the random pointer.
    // Thus we have two independent recursive calls.
    // Finally we update the next and random pointers for the new node created.
    node.next = this.copyRandomList(head.next);
    node.random = this.copyRandomList(head.random);

    return node;
  }
}
```

```Python []
class Solution(object):
    """
    :type head: Node
    :rtype: Node
    """
    def __init__(self):
        # Dictionary which holds old nodes as keys and new nodes as its values.
        self.visitedHash = {}

    def copyRandomList(self, head):

        if head == None:
            return None

        # If we have already processed the current node, then we simply return the cloned version of it.
        if head in self.visitedHash:
            return self.visitedHash[head]

        # create a new node
        # with the value same as old node.
        node = Node(head.val, None, None)

        # Save this value in the hash map. This is needed since there might be
        # loops during traversal due to randomness of random pointers and this would help us avoid them.
        self.visitedHash[head] = node

        # Recursively copy the remaining linked list starting once from the next pointer and then from the random pointer.
        # Thus we have two independent recursive calls.
        # Finally we update the next and random pointers for the new node created.
        node.next = self.copyRandomList(head.next)
        node.random = self.copyRandomList(head.random)

        return node
```

**复杂度分析**

- 时间复杂度：_O(N)_ ，其中 _N_ 是链表中节点的数目。
- 空间复杂度：_O(N)_ 。如果我们仔细分析，我们需要维护一个回溯的栈，同时也需要记录已经被深拷贝过的节点，也就是维护一个已访问字典。渐进时间复杂度为 _O(N)_ 。

<br/>
* 方法 2： *O(N)* 空间的迭代

**想法**

迭代算法不需要将链表视为一个图。当我们在迭代链表时，我们只需要为 `random` 指针和 `next` 指针指向的未访问过节点创造新的节点并赋值即可。

**算法**

1. 从 `head` 节点开始遍历链表。下图中，我们首先创造新的 `head` 拷贝节点。拷贝的节点如下图虚线所示。实现中，我们将该新建节点的引用也放入已访问字典中。

![image.png](https://pic.leetcode-cn.com/ba345f073f3edebb79e2500ffea5fd744bf2266bb1426e5b4221f2f21ecea900-image.png)

2. `random` 指针

   - 如果当前节点 _i_ 的 `random` 指针指向一个节点 _j_ 且节点 _j_ 已经被拷贝过，我们将直接使用已访问字典中该节点的引用而不会新建节点。
   - 如果当前节点 _i_ 的 `random` 指针指向
   - 的节点 _j_ 还没有被拷贝过，我们就对 _j_ 节点创建对应的新节点，并把它放入已访问节点字典中。

   下图中， _A_ 的 `random` 指针指向的节点 _C_ 。前图中可以看出，节点 _C_ 还没有被访问过，所以我们创造一个拷贝的 _C'_ 节点与之对应，并将它添加到已访问字典中。

![image.png](https://pic.leetcode-cn.com/ac190cfe6d9de91a765c103c2a79a89f25b404fa355f1b3cb41f30e47467a676-image.png)

3. `next` 指针
   - 如果当前节点 _i_ 的 `next` 指针指向的节点 _j_ 在已访问字典中已有拷贝，我们直接使用它的拷贝节点。
   - 如果当前节点 _i_ 的`next` 指针指向的节点 _j_ 还没有被访问过，我们创建一个对应节点的拷贝，并放入已访问字典。

下图中，_A_ 节点的 `next` 指针指向节点 _B_ 。节点 _B_ 在前面的图中还没有被访问过，因此我们创造一个新的拷贝 _B'_ 节点，并放入已访问字典中。

![image.png](https://pic.leetcode-cn.com/02c55bd01ea1f85231e34ea714de8db3ccb8717f434a80f94c0efdac62b15246-image.png)

4. 我们重复步骤 2 和步骤 3 ，直到我们到达链表的结尾。

下图中， 节点 _B_ 的 `random` 指针指向的节点 _A_ 已经被访问过了，因此在步骤 2 中，我们不会创建新的拷贝，

将节点 _B'_ 的 `random` 指针指向克隆节点 _A'_ 。

同样的， 节点 _B_ 的 `next` 指针指向的节点 _C_ 已经访问过，因此在步骤 3 中，我们不会创建新的拷贝，而直接将 _B'_ 的 `next` 指针指向已经存在的拷贝节点 _C'_ 。

![image.png](https://pic.leetcode-cn.com/203559119fb45aa1bb844a5441ce18089f4005fa386bd794048c51fd25686e87-image.png)

```Java []
/*
// Definition for a Node.
class Node {
    public int val;
    public Node next;
    public Node random;

    public Node() {}

    public Node(int _val,Node _next,Node _random) {
        val = _val;
        next = _next;
        random = _random;
    }
};
*/
public class Solution {
  // Visited dictionary to hold old node reference as "key" and new node reference as the "value"
  HashMap<Node, Node> visited = new HashMap<Node, Node>();

  public Node getClonedNode(Node node) {
    // If the node exists then
    if (node != null) {
      // Check if the node is in the visited dictionary
      if (this.visited.containsKey(node)) {
        // If its in the visited dictionary then return the new node reference from the dictionary
        return this.visited.get(node);
      } else {
        // Otherwise create a new node, add to the dictionary and return it
        this.visited.put(node, new Node(node.val, null, null));
        return this.visited.get(node);
      }
    }
    return null;
  }

  public Node copyRandomList(Node head) {

    if (head == null) {
      return null;
    }

    Node oldNode = head;

    // Creating the new head node.
    Node newNode = new Node(oldNode.val);
    this.visited.put(oldNode, newNode);

    // Iterate on the linked list until all nodes are cloned.
    while (oldNode != null) {
      // Get the clones of the nodes referenced by random and next pointers.
      newNode.random = this.getClonedNode(oldNode.random);
      newNode.next = this.getClonedNode(oldNode.next);

      // Move one step ahead in the linked list.
      oldNode = oldNode.next;
      newNode = newNode.next;
    }
    return this.visited.get(head);
  }
}
```

```Python []
class Solution(object):
    def __init__(self):
        # Creating a visited dictionary to hold old node reference as "key" and new node reference as the "value"
        self.visited = {}

    def getClonedNode(self, node):
        # If node exists then
        if node:
            # Check if its in the visited dictionary
            if node in self.visited:
                # If its in the visited dictionary then return the new node reference from the dictionary
                return self.visited[node]
            else:
                # Otherwise create a new node, save the reference in the visited dictionary and return it.
                self.visited[node] = Node(node.val, None, None)
                return self.visited[node]
        return None

    def copyRandomList(self, head):
        """
        :type head: Node
        :rtype: Node
        """

        if not head:
            return head

        old_node = head
        # Creating the new head node.
        new_node = Node(old_node.val, None, None)
        self.visited[old_node] = new_node

        # Iterate on the linked list until all nodes are cloned.
        while old_node != None:

            # Get the clones of the nodes referenced by random and next pointers.
            new_node.random = self.getClonedNode(old_node.random)
            new_node.next = self.getClonedNode(old_node.next)

            # Move one step ahead in the linked list.
            old_node = old_node.next
            new_node = new_node.next

        return self.visited[head]
```

**复杂度分析**

- 时间复杂度：_O(N)_ 。因为我们需要将原链表逐一遍历。
- 空间复杂度：_O(N)_ 。 我们需要维护一个字典，保存旧的节点和新的节点的对应。因此总共需要 _N_ 个节点，需要 _O(N)_ 的空间复杂度。

<br/>
* 方法 3：*O(1)* 空间的迭代

**想法**

与上面提到的维护一个旧节点和新节点对应的字典不同，我们通过扭曲原来的链表，并将每个拷贝节点都放在原来对应节点的旁边。这种旧节点和新节点交错的方法让我们可以在不需要额外空间的情况下解决这个问题。让我们看看这个算法如何工作

**算法**

1. 遍历原来的链表并拷贝每一个节点，将拷贝节点放在原来节点的旁边，创造出一个旧节点和新节点交错的链表。

![image.png](https://pic.leetcode-cn.com/c4e075d7eb23b27074430abda66ff5a74307f85958b063ebb530873b66c117b8-image.png)
![image.png](https://pic.leetcode-cn.com/62ba6efc1d3a77ba04956a105eeaa5738ef1771d9e2fc9f4daf80a0cf1275d70-image.png)

如你所见，我们只是用了原来节点的值拷贝出新的节点。原节点 `next` 指向的都是新创造出来的节点。

`cloned_node.next = original_node.next`

`original_node.next = cloned_node`

2. 迭代这个新旧节点交错的链表，并用旧节点的 `random` 指针去更新对应新节点的 `random` 指针。比方说， `B` 的 `random` 指针指向 `A` ，意味着 `B'` 的 `random` 指针指向 `A'` 。

![image.png](https://pic.leetcode-cn.com/1789e6dd9bbe41223cab82b2e0a7615cd1a8ed16a3c992462d4e1eaec3b82fb1-image.png)

3. 现在 `random` 指针已经被赋值给正确的节点， `next` 指针也需要被正确赋值，以便将新的节点正确链接同时将旧节点重新正确链接。

![image.png](https://pic.leetcode-cn.com/a28323ef84883ec02e7d99fd13b444dede9355389c7567e43e7ee1c85262a2d3-image.png)

```Java []
/*
// Definition for a Node.
class Node {
    public int val;
    public Node next;
    public Node random;

    public Node() {}

    public Node(int _val,Node _next,Node _random) {
        val = _val;
        next = _next;
        random = _random;
    }
};
*/
public class Solution {
  public Node copyRandomList(Node head) {

    if (head == null) {
      return null;
    }

    // Creating a new weaved list of original and copied nodes.
    Node ptr = head;
    while (ptr != null) {

      // Cloned node
      Node newNode = new Node(ptr.val);

      // Inserting the cloned node just next to the original node.
      // If A->B->C is the original linked list,
      // Linked list after weaving cloned nodes would be A->A'->B->B'->C->C'
      newNode.next = ptr.next;
      ptr.next = newNode;
      ptr = newNode.next;
    }

    ptr = head;

    // Now link the random pointers of the new nodes created.
    // Iterate the newly created list and use the original nodes' random pointers,
    // to assign references to random pointers for cloned nodes.
    while (ptr != null) {
      ptr.next.random = (ptr.random != null) ? ptr.random.next : null;
      ptr = ptr.next.next;
    }

    // Unweave the linked list to get back the original linked list and the cloned list.
    // i.e. A->A'->B->B'->C->C' would be broken to A->B->C and A'->B'->C'
    Node ptr_old_list = head; // A->B->C
    Node ptr_new_list = head.next; // A'->B'->C'
    Node head_old = head.next;
    while (ptr_old_list != null) {
      ptr_old_list.next = ptr_old_list.next.next;
      ptr_new_list.next = (ptr_new_list.next != null) ? ptr_new_list.next.next : null;
      ptr_old_list = ptr_old_list.next;
      ptr_new_list = ptr_new_list.next;
    }
    return head_old;
  }
}
```

```Python []
class Solution(object):
    def copyRandomList(self, head):
        """
        :type head: Node
        :rtype: Node
        """
        if not head:
            return head

        # Creating a new weaved list of original and copied nodes.
        ptr = head
        while ptr:

            # Cloned node
            new_node = Node(ptr.val, None, None)

            # Inserting the cloned node just next to the original node.
            # If A->B->C is the original linked list,
            # Linked list after weaving cloned nodes would be A->A'->B->B'->C->C'
            new_node.next = ptr.next
            ptr.next = new_node
            ptr = new_node.next

        ptr = head

        # Now link the random pointers of the new nodes created.
        # Iterate the newly created list and use the original nodes random pointers,
        # to assign references to random pointers for cloned nodes.
        while ptr:
            ptr.next.random = ptr.random.next if ptr.random else None
            ptr = ptr.next.next

        # Unweave the linked list to get back the original linked list and the cloned list.
        # i.e. A->A'->B->B'->C->C' would be broken to A->B->C and A'->B'->C'
        ptr_old_list = head # A->B->C
        ptr_new_list = head.next # A'->B'->C'
        head_old = head.next
        while ptr_old_list:
            ptr_old_list.next = ptr_old_list.next.next
            ptr_new_list.next = ptr_new_list.next.next if ptr_new_list.next else None
            ptr_old_list = ptr_old_list.next
            ptr_new_list = ptr_new_list.next
        return head_old
```

**复杂度分析**

- 时间复杂度：_O(N)_
- 空间复杂度：_O(1)_
