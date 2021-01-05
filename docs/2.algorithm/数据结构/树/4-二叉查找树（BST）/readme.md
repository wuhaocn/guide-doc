# 一、定义

一棵二叉查找树是一棵二叉树，每个节点都含有一个 Comparable 的键（以及对应的值）。

每个节点的键都大于左子树中任意节点的键而小于右子树中任意节点的键。

每个节点都有两个链接，左链接、右链接，分别指向自己的左子节点和右子节点，链接也可以指向 null。

尽管链接指向的是节点，可以将每个链接看做指向了另一棵二叉树。这个思路能帮助理解二叉查找树的递归方法。

# 二、基本实现

## 1、数据表示

[![复制代码](https://common.cnblogs.com/images/copycode.gif)]("复制代码")

```
private class Node { private Key key; private Value val; private Node left, right; private int size; public Node(Key key, Value val, int n) { this.key = key; this.val = val; this.size = n; left = right = null; } }
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)]("复制代码")

## 2、测试所有方法的用例

在实现方法后，需要一个用例来测试方法是否正常工作。

以下是用例的代码：

[![复制代码](https://common.cnblogs.com/images/copycode.gif)]("复制代码")

```
package com.qiusongde; import edu.princeton.cs.algs4.StdOut; public class BSTTest { public static void main(String[] args) { String test = "S E A R C H E X A M P L E"; String[] keys = test.split("\\s+"); int n = keys.length; //test put StdOut.println("Testing put(Key,Value)"); BST<String, Integer> st = new BST<String, Integer>(); StdOut.println(st); for (int i = 0; i < n; i++) { st.put(keys[i], i); StdOut.println("put(" + keys[i] + ", " + i +")"); StdOut.println(st); } //test search hit StdOut.println("Testing keys() and get(Key)"); StdOut.println("--------------------------------"); for (String s : st.keys()) StdOut.println(s + " " + st.get(s)); //test search miss StdOut.println("I" + " " + st.get("I")); StdOut.println(); //test delete StdOut.println("Testing delete(Key)"); StdOut.println(st); StdOut.println("delete E"); st.delete("E");//not root, has two subtree StdOut.println(st); StdOut.println("delete A"); st.delete("A");//not root, has right subtree StdOut.println(st); StdOut.println("delete P"); st.delete("P");//not root, has no subtree StdOut.println(st); StdOut.println("delete L"); st.delete("L");//not root, has left subtree StdOut.println(st); StdOut.println("delete S"); st.delete("S");//root, has two subtree StdOut.println(st); StdOut.println("delete X"); st.delete("X"); StdOut.println(st);//root, has subtree for (int i = 0; i < n; i++) { StdOut.println("delete " + keys[i]); st.delete(keys[i]); StdOut.println(st); } //insert back StdOut.println("insert back"); for (int i = 0; i < n; i++) { st.put(keys[i], i); } StdOut.println(st); StdOut.println("size = " + st.size()); StdOut.println("min = " + st.min()); StdOut.println("max = " + st.max()); StdOut.println(); // print keys in order using select StdOut.println("Testing select"); StdOut.println("--------------------------------"); for (int i = 0; i <= st.size(); i++) StdOut.println(i + " " + st.select(i)); StdOut.println(); // test rank, floor, ceiling StdOut.println("key rank floor ceil"); StdOut.println("-------------------"); for (char i = 'A' - 1; i <= 'Z'; i++) { String s = i + ""; StdOut.printf("%2s %4d %4s %4s\n", s, st.rank(s), st.floor(s), st.ceiling(s)); } StdOut.println(); // test range search and range count String[] from = { "A", "Z", "X", "0", "B", "C" }; String[] to = { "Z", "A", "X", "Z", "G", "L" }; StdOut.println("range search"); StdOut.println("-------------------"); for (int i = 0; i < from.length; i++) { StdOut.printf("%s-%s (%2d) : ", from[i], to[i], st.size(from[i], to[i])); for (String s : st.keys(from[i], to[i])) StdOut.print(s + " "); StdOut.println(); } StdOut.println(); // delete the smallest keys StdOut.println(st); StdOut.println("Test deleteMin"); for (int i = 0; i < 3; i++) { st.deleteMin(); StdOut.println(st); } // delete all the remaining keys, using deleteMax StdOut.println("Test deleteMax"); while (!st.isEmpty()) { st.deleteMax(); StdOut.println(st); } // under empty, test again StdOut.println("under empty, test again"); StdOut.println("size = " + st.size()); StdOut.println("min = " + st.min()); StdOut.println("max = " + st.max()); StdOut.println(); // print keys in order using keys() StdOut.println("Testing keys()"); StdOut.println("--------------------------------"); for (String s : st.keys()) StdOut.println(s + " " + st.get(s)); StdOut.println(); // print keys in order using select StdOut.println("Testing select"); StdOut.println("--------------------------------"); for (int i = 0; i <= st.size(); i++) StdOut.println(i + " " + st.select(i)); StdOut.println(); // test rank, floor, ceiling StdOut.println("key rank floor ceil"); StdOut.println("-------------------"); for (char i = 'A'; i <= 'Z'; i++) { String s = i + ""; StdOut.printf("%2s %4d %4s %4s\n", s, st.rank(s), st.floor(s), st.ceiling(s)); } StdOut.println(); // test range search and range count StdOut.println("range search"); StdOut.println("-------------------"); for (int i = 0; i < from.length; i++) { StdOut.printf("%s-%s (%2d) : ", from[i], to[i], st.size(from[i], to[i])); for (String s : st.keys(from[i], to[i])) StdOut.print(s + " "); StdOut.println(); } StdOut.println(); // delete the smallest keys StdOut.println(st); StdOut.println("Test deleteMin"); for (int i = 0; i < 3; i++) { st.deleteMin(); StdOut.println(st); } // delete all the remaining keys, using deleteMax StdOut.println("Test deleteMax"); while (!st.isEmpty()) { st.deleteMax(); StdOut.println(st); } StdOut.println(); StdOut.println("get(S) under empty"); StdOut.println(st.get("S")); StdOut.println(); StdOut.println("delete(S) under empty"); StdOut.println(st); st.delete("S"); StdOut.println(st); } }
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)]("复制代码")

需要实现 toString 来配合测试用例：
[![复制代码](https://common.cnblogs.com/images/copycode.gif)]("复制代码")

```
private String BSTString(Node x) { if(x == null) return ""; String s = ""; s += BSTString(x.left); s += x.key + " " + x.val + " " + x.size + "(s)\n"; s += BSTString(x.right); return s; }
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)]("复制代码")

## 3、查找实现（search）

思路：

A、如果二叉查找树为空，查找失败（search miss），返回 null；

B、如果根节点的键等于要查找的键，返回根节点的值（search hit）。

C、否则，继续在**相应的子树**中查找。如果要查找的键小于根节点的键，在左子树中查找；如果要查找的键大于根节点的键，在右子树中查找。

D、重复 ABC 步骤，直至 search miss 或者 search hit。

递归实现：
[![复制代码](https://common.cnblogs.com/images/copycode.gif)]("复制代码")

```
//*/* /* Returns the value associated with the given key. /* /* @param key the key /* @return the value associated with the given key if the key is in the symbol table /* and {@code null} if the key is not in the symbol table /* @throws IllegalArgumentException if {@code key} is {@code null} /*/ public Value get(Key key) { if(key == null) throw new IllegalArgumentException("key is null"); return get(root, key);//work when BST is empty } private Value get(Node x, Key key) { if(x == null) return null;//serach miss int cmp = key.compareTo(x.key); if(cmp < 0) return get(x.left, key); else if(cmp > 0) return get(x.right, key); else return x.val;//serach hit }
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)]("复制代码")

非递归实现：
[![复制代码](https://common.cnblogs.com/images/copycode.gif)]("复制代码")

```
//*/* /* Returns the value associated with the given key. /* /* @param key the key /* @return the value associated with the given key if the key is in the symbol table /* and {@code null} if the key is not in the symbol table /* @throws IllegalArgumentException if {@code key} is {@code null} /*/ public Value get(Key key) { if(key == null) throw new IllegalArgumentException("key is null"); Node cur = root; while(cur != null) { int cmp = key.compareTo(cur.key); if(cmp < 0) cur = cur.left; else if(cmp > 0) cur = cur.right; else return cur.val;//search hit } return null;//search miss }
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)]("复制代码")

## 4、插入实现

思路：

A、如果二叉查找树是空的，生成一个新节点，并返回该节点，相当于插入新节点后的二叉树根节点。

B、如果根节点键和要插入的键相等，更新根节点的值。

C、如果要插入的键小于根节点的键，在左子树插入，并将根节点的左链接指向插入后的左子树。

D、如果要插入的键小于根节点的键，在右子树插入，并将根节点的右链接指向插入后的右子树。

E、更新根节点的 size，并返回根节点，作为插入新节点后的二叉树根节点。

F、重复 ABCD，直至插入或者更新成功。

递归实现：
[![复制代码](https://common.cnblogs.com/images/copycode.gif)]("复制代码")

```
//*/* /* Inserts the specified key-value pair into the symbol table, overwriting the old /* value with the new value if the symbol table already contains the specified key. /* Deletes the specified key (and its associated value) from this symbol table /* if the specified value is {@code null}. /* /* @param key the key /* @param val the value /* @throws IllegalArgumentException if {@code key} is {@code null} /*/ public void put(Key key, Value val) { if(key == null) throw new IllegalArgumentException("key is null"); if(val == null) { delete(key); return; } root = put(root, key, val); } private Node put(Node x, Key key, Value val) { if(x == null) return new Node(key, val, 1); int cmp = key.compareTo(x.key); if(cmp < 0) x.left = put(x.left, key, val); else if(cmp > 0) x.right = put(x.right, key, val); else x.val = val; x.size = size(x.left) + size(x.right) + 1; return x; }
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)]("复制代码")

递归实现的，要在**插入之后更新节点的 size**。

非递归实现：
[![复制代码](https://common.cnblogs.com/images/copycode.gif)]("复制代码")

```
//*/* /* Inserts the specified key-value pair into the symbol table, overwriting the old /* value with the new value if the symbol table already contains the specified key. /* Deletes the specified key (and its associated value) from this symbol table /* if the specified value is {@code null}. /* /* @param key the key /* @param val the value /* @throws IllegalArgumentException if {@code key} is {@code null} /*/ public void put(Key key, Value val) { if(key == null) throw new IllegalArgumentException("key is null"); if(val == null) { delete(key); return; } if(root == null) { root = new Node(key, val); return; } boolean alreadyin = contains(key);//see if it needs to update the counts Node parent = null; Node cur = root; while(cur != null) { parent = cur; cur.size = alreadyin ? cur.size : cur.size + 1;//change size of cur int cmp = key.compareTo(cur.key); if(cmp < 0) cur = cur.left; else if(cmp > 0) cur = cur.right; else { cur.val = val; return; } } if(key.compareTo(parent.key) < 0) parent.left = new Node(key, val); else parent.right = new Node(key, val); }
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)]("复制代码")

非递归实现的比较麻烦，需要处理一些特殊情况：

A、**需要记录父节点**，用于更新父节点的链接。

B、还要处理一个**特殊情况**，就是**根节点**就是要插入的位置。

C、还要多调用一次 get，用于判断要插入的键值对是否已经在二叉搜索树中。如果在就不用更新 size，如果不在就需要更新 size。

D、非递归实现更新 size 跟递归实现的顺序相反，要**边搜索边更改 size**。

注：后边只要改变二叉搜索树结构的，非递归实现都需要考虑这些特殊情况，如 insert 或 delete 等。

## 5、min 实现和 max 实现

以 min 为例：

思路：

A、如果根节点的左链接是 null，返回根节点。

B、否则继续在左子树中查找。

C、重复 AB，直至找到一个根节点的左链接是 null。

递归实现：
[![复制代码](https://common.cnblogs.com/images/copycode.gif)]("复制代码")

```
//*/* /* Returns the smallest key in the symbol table. /* /* If the symbol table is empty, return {@code null}. /* /* @return the smallest key in the symbol table /*/ public Key min() { if(isEmpty()) return null; return min(root).key; } private Node min(Node x) { if(x.left == null) return x; return min(x.left); }
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)]("复制代码")

非递归实现：
[![复制代码](https://common.cnblogs.com/images/copycode.gif)]("复制代码")

```
//*/* /* Returns the smallest key in the symbol table. /* /* If the symbole table is empty, return {@code null}. /* /* @return the smallest key in the symbol table /*/ public Key min() { if(isEmpty()) return null; return min(root).key; } private Node min(Node x) {//x must not be null if(x == null) throw new IllegalArgumentException("Node x must not be null"); Node cur = x; while(cur.left != null) { cur = cur.left; } return cur; }
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)]("复制代码")

这里 private 的 min 函数，在后边**delete**需要调用到。

max 的思路和 min 思路基本差不多，只是左右相反即可。

递归实现：
[![复制代码](https://common.cnblogs.com/images/copycode.gif)]("复制代码")

```
//*/* /* Return the largest key in the symbol table. /* /* If the symbol table is empty, return {@code null} /* /* @return the largest key in the symbol table /*/ public Key max() { if(isEmpty()) return null; return max(root).key; } private Node max(Node x) { if(x.right == null) return x; return max(x.right); }
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)]("复制代码")

非递归实现：
[![复制代码](https://common.cnblogs.com/images/copycode.gif)]("复制代码")

```
//*/* /* Return the largest key in the symbol table. /* /* If the symbol table is empty, return {@code null} /* /* @return the largest key in the symbol table /*/ public Key max() { if(isEmpty()) return null; Node cur = root; while(cur.right != null) { cur = cur.right; } return cur.key; }
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)]("复制代码")

## 6、floor 和 ceiling 实现

floor 是求 the largest key in the BST less than or equal to _key_

思路：

A、如果根节点是 null，则直接返回 null。

B、如果根节点键值大于*key*，则继续 floor(key)在左子树中，所以继续在左子树中查找。

C、如果根节点键值刚好等于*key*，则根节点的键值即为 floor(key)，直接返回该键。

D、如果根节点键值小于*key*，那么根节点的 key 有可能就是 floor(key)，**只要**右子树中不存在节点的 key 小于等于输入的*key。*

E、重复 ABCD

递归实现：
[![复制代码](https://common.cnblogs.com/images/copycode.gif)]("复制代码")

```
//*/* /* Return the largest key in the symbol table less than or equal to {@code key}. /* /* If the symbol table is empty, return {@code null} /* /* @param key the key /* @return the largest key in the symbol table less than or equal to {@code key} /* /* @throws IllegalArgumentException if {@code key} is {@code null} /*/ public Key floor(Key key) { if(key == null) throw new IllegalArgumentException("key is null"); Node x = floor(root, key);//work when BST is empty if(x == null) return null; return x.key; } private Node floor(Node x, Key key) { if(x == null) return null; int cmp = key.compareTo(x.key); if(cmp == 0) return x; if(cmp < 0) return floor(x.left, key);//in left subtree Node t = floor(x.right, key);//see if right subtree has a key is the floor(key) if(t != null) return t;//yes, return t else return x;//no, return x }
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)]("复制代码")

非递归实现：
[![复制代码](https://common.cnblogs.com/images/copycode.gif)]("复制代码")

```
//*/* /* Return the largest key in the symbol table less than or equal to {@code key}. /* /* If the symbol table is empty, return {@code null} /* /* @param key the key /* @return the largest key in the symbol table less than or equal to {@code key} /* /* @throws IllegalArgumentException if {@code key} is {@code null} /*/ public Key floor(Key key) { if(key == null) throw new IllegalArgumentException("key is null"); Node cur = root; Key result = null; while(cur != null) {//it works when BST is empty int cmp = key.compareTo(cur.key); if(cmp < 0) { cur = cur.left; } else if(cmp > 0) { result = cur.key;//may be updated cur = cur.right;//see if the right subtree has a key smaller than or equal to key } else { return cur.key;//final result } } return result; }
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)]("复制代码")

这里用 result 来缓存，用于解决思路中 D 的情况。

ceiling 和 floor 思路差不多，只不过左右对调过来。

递归实现：
[![复制代码](https://common.cnblogs.com/images/copycode.gif)]("复制代码")

```
//*/* /* Return the smallest key in the symbol table larger than or equal to {@code key}. /* /* If the symbol table is empty, return {@code null}. /* /* @param key the key /* @return the smallest key in the symbol table larger than or equal to {@code key} /* /* @throws IllegalArgumentException if {@code key} is {@code null} /*/ public Key ceiling(Key key) { if(key == null) throw new IllegalArgumentException("key is null"); Node x = ceiling(root, key);//work when BST is empty if(x == null) return null; return x.key; } private Node ceiling(Node x, Key key) { if(x == null) return null; int cmp = key.compareTo(x.key); if(cmp == 0) return x; if(cmp > 0) return ceiling(x.right, key); Node t = ceiling(x.left, key); if(t != null) return t; else return x; }
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)]("复制代码")

非递归实现：
[![复制代码](https://common.cnblogs.com/images/copycode.gif)]("复制代码")

```
//*/* /* Return the smallest key in the symbol table larger than or equal to {@code key}. /* /* If the symbol table is empty, return {@code null}. /* /* @param key the key /* @return the smallest key in the symbol table larger than or equal to {@code key} /* /* @throws IllegalArgumentException if {@code key} is {@code null} /*/ public Key ceiling(Key key) { if(key == null) throw new IllegalArgumentException("key is null"); Node cur = root; Key result = null; while(cur != null) {//it works when BST is empty int cmp = key.compareTo(cur.key); if(cmp < 0) { result = cur.key;//may be updated cur = cur.left; } else if(cmp > 0) { cur = cur.right; } else { return cur.key;//final result } } return result; }
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)]("复制代码")

## 7、select 实现

**size 是专门为 select 和 rank 等函数准备的。**

思路：

A、如果根节点为 null，直接返回 null。

B、如果左子树的节点个数为 t，select(k)，如果 t=k，那么直接返回根节点的值。

C、如果 t>k，根节点的排序太大，需要在左子树中继续 select(k)。

D、如果 t<k，根节点的排序太小，需要在右子树中继续 select(k-t-1)。

E、重复 ABCD

递归实现：
[![复制代码](https://common.cnblogs.com/images/copycode.gif)]("复制代码")

```
//*/* /* Return the kth smallest key in the symbol table. /* /* When k is 0, return the smallest key. When k is <em>N</em> &minus; 1, return the largest key. /* /* @param k the order statistic /* @return the kth smallest key in the symbol table /* /* @throws IllegalArgumentException unless {@code k} is between 0 and /* <em>N</em> &minus; 1 /*/ public Key select(int k) { if(k < 0 || k >= size()) return null; Node x = select(root, k);//work when BST is empty if(x == null) return null; return x.key; } private Node select(Node x, int k) { if(x == null) return null; int t = size(x.left); if(t > k) return select(x.left, k); else if(t < k) return select(x.right, k - t - 1); else return x; }
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)]("复制代码")

非递归实现：
[![复制代码](https://common.cnblogs.com/images/copycode.gif)]("复制代码")

```
//*/* /* Return the kth smallest key in the symbol table. /* /* When k is 0, return the smallest key. When k is <em>N</em> &minus; 1, return the largest key. /* /* @param k the order statistic /* @return the kth smallest key in the symbol table /* /* @throws IllegalArgumentException unless {@code k} is between 0 and /* <em>N</em> &minus; 1 /*/ public Key select(int k) { if(k < 0 || k >= size())//include the empty situation return null; Node cur = root; while(cur != null) { int less = size(cur.left); if(less < k) { cur = cur.right; k = k - less - 1; } else if(less > k) { cur = cur.left; } else { return cur.key; } } return null; }
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)]("复制代码")

## 8、rank 实现

思路：

A、如果根节点为 null，返回 0。

B、如果根节点的键刚好等于*key*，返回左子树的节点个数（刚好只有左子树的所有键小于*key*）。

C、如果根节点的键大于*key*，则在左子树中 rank(_key_)（只有左子树才有小于*key*的键）。

D、如果根节点的键小于*key*，则除了左子树所有键都小于*key*外，右子树也可能有小于*key*的键，则返回左子树的节点个数+rank(key)在右子树的值+1。

E、重复 ABCD。

递归实现：
[![复制代码](https://common.cnblogs.com/images/copycode.gif)]("复制代码")

```
//*/* /* /* Return the number of keys in the symbol table strictly less than {@code key}. /* /* @param key the key /* @return the number of keys in the symbol table strictly less than {@code key} /* /* @throws IllegalArgumentException if key is {@code null} /*/ public int rank(Key key) { if(key == null) throw new IllegalArgumentException("key is null"); return rank(root, key);//work when BST is empty } private int rank(Node x, Key key) { if(x == null) return 0; int cmp = key.compareTo(x.key); if(cmp < 0) return rank(x.left, key); else if(cmp > 0) return size(x.left) + 1 + rank(x.right, key); else return size(x.left); }
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)]("复制代码")

非递归实现：
[![复制代码](https://common.cnblogs.com/images/copycode.gif)]("复制代码")

```
//*/* /* /* Return the number of keys in the symbol table strictly less than {@code key}. /* /* @param key the key /* @return the number of keys in the symbol table strictly less than {@code key} /* /* @throws IllegalArgumentException if key is {@code null} /*/ public int rank(Key key) { if(key == null) throw new IllegalArgumentException("key is null"); Node cur = root; int result = 0; while(cur != null) {//work when BST is empty int cmp = key.compareTo(cur.key); if(cmp < 0) { cur = cur.left; } else if(cmp > 0) { result += size(cur.left) + 1; cur = cur.right; } else { return result + size(cur.left); } } return result; }
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)]("复制代码")

9、删除最小值和最大值

**思路：**

**一直往左走，直到到达左链接为 null 的节点 x（最小值），然后将指向该节点 x 的链接替换为 x.right。**

递归实现：
[![复制代码](https://common.cnblogs.com/images/copycode.gif)]("复制代码")

```
//*/* /* Removes the smallest key and its associated value from the symbol table /* /* If the symbol table is empty, do nothing. /* /*/ public void deleteMin() { if(isEmpty()) return;//do nothing root = deleteMin(root); } private Node deleteMin(Node x) { if(x.left == null) return x.right; x.left = deleteMin(x.left); x.size = size(x.left) + size(x.right) + 1; return x; }
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)]("复制代码")

非递归实现：
[![复制代码](https://common.cnblogs.com/images/copycode.gif)]("复制代码")

```
//*/* /* Removes the smallest key and its associated value from the symbol table /* /* If the symbol table is empty, do nothing. /* /*/ public void deleteMin() { if(isEmpty()) return;//do nothing root = deleteMin(root); } private Node deleteMin(Node x) {//x must not be null if(x.left == null) { return x.right; } Node cur = x; Node parent = null; while(cur.left != null) { cur.size--; parent = cur; cur = cur.left; } parent.left = cur.right; return x; }
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)]("复制代码")

非递归：

A、**需要记录父节点**，用于更新父节点的链接。

B、还要处理一个**特殊情况**，就是**根节点**就是要删除的节点。

C、非递归实现更新 size 跟递归实现的顺序相反，要**边搜索边更改 size**。

删除最大值思路差不多，左右相反而已。

递归实现：
[![复制代码](https://common.cnblogs.com/images/copycode.gif)]("复制代码")

```
//*/* /* Removes the largest key and associated value from the symbol table. /* /* If the symbol table is empty, do nothing. /*/ public void deleteMax() { if(isEmpty()) return; //do nothing root = deleteMax(root); } private Node deleteMax(Node x) { if(x.right == null) return x.left; x.right = deleteMax(x.right); x.size = size(x.left) + size(x.right) + 1; return x; }
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)]("复制代码")

非递归实现：
[![复制代码](https://common.cnblogs.com/images/copycode.gif)]("复制代码")

```
//*/* /* Removes the largest key and associated value from the symbol table. /* /* If the symbol table is empty, do nothing. /*/ public void deleteMax() { if(isEmpty()) return;//do nothing if(root.right == null) { root = root.left; return; } Node cur = root; Node parent = null; while(cur.right != null) { cur.size--; parent = cur; cur = cur.right; } parent.right = cur.left; }
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)]("复制代码")

非递归：

A、**需要记录父节点**，用于更新父节点的链接。

B、还要处理一个**特殊情况**，就是**根节点**就是要删除的节点。

C、非递归实现更新 size 跟递归实现的顺序相反，要**边搜索边更改 size**。

## 10、删除节点

如果要删除的节点 x 只有子节点或者没有子节点，那么可以效仿删除最小值和最大值的做法。

如果有两个子节点呢？应该在右子树中找继承者来替换 x，然后再返回 x。

步骤：

A、用 t 保存即将删除的节点 x。

B、将 x 指向右子树的最小键节点，也就是继承者。

C、x.right = deleteMin(t.right)。

D、x.left = t.left。

递归实现：
[![复制代码](https://common.cnblogs.com/images/copycode.gif)]("复制代码")

```
//*/* /* Removes the specified key and its associated value from this symbol table /* If the key is not in this symbol table or this symbol table is empty, do nothing /* /* @param key the key /* @throws IllegalArgumentException if {@code key} if {@code null} /*/ public void delete(Key key) { if(key == null) throw new IllegalArgumentException("key is null"); if(isEmpty()) return; //do nothing root = delete(root, key); } private Node delete(Node x, Key key) { if(x == null) return null; int cmp = key.compareTo(x.key); if(cmp < 0) x.left = delete(x.left, key);//left subtree else if(cmp > 0) x.right = delete(x.right, key);//right subtree else { if(x.right == null) return x.left; if(x.left == null) return x.right; Node temp = x;//save the node to be deleted x = min(temp.right);//set x to point to its successor min(temp.right) x.right = deleteMin(temp.right);//set the right link of the successor to deleteMin(temp.right) x.left = temp.left;//set the left link of the successor to t.left } x.size = size(x.left) + size(x.right) + 1; return x; }
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)]("复制代码")

非递归实现：
[![复制代码](https://common.cnblogs.com/images/copycode.gif)]("复制代码")

```
//*/* /* Removes the specified key and its associated value from this symbol table /* If the key is not in this symbol table or this symbol talbe is empty, do nothing /* /* @param key the key /* @throws IllegalArgumentException if {@code key} if {@code null} /*/ public void delete(Key key) { if(key == null) throw new IllegalArgumentException("key is null"); if(!contains(key) || isEmpty()) { return;//do nothing } if(key.compareTo(root.key) == 0) { deleteRoot(); return; } Node cur = root; Node parent = null; while(cur != null) { int cmp = key.compareTo(cur.key); if(cmp < 0) { cur.size--; parent = cur;//record parent cur = cur.left; } else if(cmp > 0){ cur.size--; parent = cur;//record parent cur = cur.right; } else { int parentcmp = key.compareTo(parent.key); if(cur.left == null) {//special case if(parentcmp < 0) { parent.left = cur.right; } else { parent.right = cur.right; } return; } if(cur.right == null) {//special case if(parentcmp < 0) { parent.left = cur.left; } else { parent.right = cur.left; } return; } Node temp = cur; cur = min(temp.right);//temp.right will not be null cur.right = deleteMin(temp.right); cur.left = temp.left; cur.size = size(cur.left) + size(cur.right) + 1; if(parentcmp < 0) { parent.left = cur; } else { parent.right = cur; } return; } } }
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)]("复制代码")

这个非递归的实现太麻烦了，有很多特殊情况需要处理。

A、**需要记录父节点**，用于更新父节点的链接。

B、还要处理一个**特殊情况**，就是**根节点**就是要删除的节点。

C、还要多调用一次 get，用于判断要删除的节点是否在二叉搜索树中。

D、非递归实现更新 size 跟递归实现的顺序相反，要**边搜索边更改 size**。

## 11、其他操作

[![复制代码](https://common.cnblogs.com/images/copycode.gif)]("复制代码")

```
//*/* /* Returns all keys in the symbol table as an {@code Iterable}. /* To iterate over all of the keys in the symbol table named {@code st}, /* use the foreach notation: {@code for (Key key : st.keys())}. /* /* @return all keys in the symbol table /*/ public Iterable<Key> keys() { return keys(min(), max()); } //*/* /* Returns all keys in the symbol table in the given range, /* as an {@code Iterable}. /* /* @param lo minimum endpoint /* @param hi maximum endpoint /* @return all keys in the symbol table between {@code lo} /* (inclusive) and {@code hi} (inclusive) /* /* @throws IllegalArgumentException if either {@code lo} or {@code hi} /* is {@code null} /*/ public Iterable<Key> keys(Key lo, Key hi) { LinkedList<Key> queue = new LinkedList<>(); keys(root, queue, lo, hi); return queue; } private void keys(Node x, LinkedList<Key> queue, Key lo, Key hi) { if(x == null) return; int cmplo = lo.compareTo(x.key); int cmphi = hi.compareTo(x.key); if(cmplo < 0) keys(x.left, queue, lo, hi); if(cmplo <= 0 && cmphi >= 0) queue.add(x.key); if(cmphi > 0) keys(x.right, queue, lo, hi); } //*/* /* Returns the number of key-value pairs in this symbol table. /* @return the number of key-value pairs in this symbol table /*/ public int size() { return size(root); } //*/* /* Returns the number of keys in the symbol table in the given range. /* /* @param lo minimum endpoint /* @param hi maximum endpoint /* @return the number of keys in the symbol table between {@code lo} /* (inclusive) and {@code hi} (inclusive) /* /* @throws IllegalArgumentException if either {@code lo} or {@code hi} /* is {@code null} /*/ public int size(Key lo, Key hi) { if(lo == null) throw new IllegalArgumentException("lo is null"); if(hi == null) throw new IllegalArgumentException("hi is null"); if(hi.compareTo(lo) < 0) return 0; else if(contains(hi)) return rank(hi) - rank(lo) + 1; else return rank(hi) - rank(lo); } private int size(Node x) { if(x == null) return 0; else return x.size; } //*/* /* Returns true if this symbol table is empty. /* /* @return {@code true} if this symbol table is empty; {@code false} otherwise /*/ public boolean isEmpty() { return size() == 0; } //*/* /* Does this symbol table contain the given key? /* /* @param key the key /* @return {@code true} if this symbol table contains {@code key} and /* {@code false} otherwise /* /* @throws IllegalArgumentException if {@code key} is {@code null} /*/ public boolean contains(Key key) { if(key == null) throw new IllegalArgumentException("key is null"); return get(key) != null; }
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)]("复制代码")

# 三、性能分析

假设键的顺序是随机的，也就是插入的顺序是随机的。

这个的分析其实和快排是差不多的。

**结论 1：**在 N 个随机键构造的二叉查找树中，**查找命中**平均需要的比较次数为~2lnN ~1.39log2N。

证明：

定义**内部路径长度**为所有节点的**深度**的和。

令 IN 为 N 个随机排序的不同键**构造**而成的二叉查找树的内部路径长度，其中 I0=I1=0。

则 IN = （**N-1**） + （I0+IN-1）/N + （I1+CN-2）/N + …… + （IN-1+I0）/N。

根节点使两个子树所有节点的深度都加 1，也就是**N-1**。

这个的分析跟快排的分析差不多，IN~2NlnN。

平均比较次数为：1+IN/N~2lnN。

**结论 2：**在 N 个随机键构造的二叉查找树中，**插入和查找失败**平均需要的比较次数为~2lnN ~1.39log2N

证明：

定义**外部路径长度**为根节点到所有 null 节点的所有路径的节点总和。

令 EN 为 N 个随机排序的不同键**构造**而成的二叉查找树的外部路径长度，其中 E0=0，E1=2。

则 EN = （N+1） + （E0+EN-1）/N + （E1+EN-2）/N + …… + （EN-1+E0）/N。

其中**N+1**为经过根节点的总路径的总和为**N+1**。这里还需要证明有 N 个节点的二叉树，其 null 链接为 N+1。所以总路径数为 N+1。

N 个节点，总共有 2N 个链接，其中除了根节点外，每个节点都有一个链接指向该节点，故总共有 N-1 个链接指向节点，剩下 2N-(N-1)=N+1 个链接指向 null。

EN 的分析和 IN 差不多。

**结论 3：**EN=IN+2N

用归纳法，即可证明。略。

**结论 4：插入和查找失败平均比查找成功多一次比较。**

证明：

插入和查找失败平均比较次数为 EN/N，查找成功平均比较次数为 IN/N+1。

EN/N-(IN/N+1)=(EN-IN)/N - 1= 2N/N - 1 = 1。

**结论 5：**在一棵二叉查找树中，所有操作在最坏情况下所需的时间都和树的**高度**成正比。

树的高度将会比平均内部路径长度要大。对于足够大的 N，高度趋近于**2.99logN**。

但是构造树的键**不是随机**的话，最坏情况将会变得不可接受。比如用例按照顺序或者逆序插入符号表。

这种情况还是有可能出现的，因为用例控制着插入和查找等操作的顺序。

**平衡二叉查找树（红黑树）**可以解决这个问题。
