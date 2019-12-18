package org.coral.algorithm.sort;

/**
 * 插入排序
 * 通过构建有序序列，对于未排序数据，在已排序序列中从后向前扫描，找到相应的位置并插入。
 * 插入排序非常类似于整扑克牌。在开始摸牌时，左手是空的，牌面朝下放在桌上。接着， 一次从
 * 桌上摸起一张牌，并将它插入到左手一把牌中的正确位置上。 为了找到这张牌的正确位置，要将
 * 它与手中已有的牌从右到左地进行比较。无论什么时候，左手中的牌都是排好序的。
 * 如果输入数组已经是排好序的话，插入排序出现最佳情况，其运行时间是输入规模的一个线性函
 * 数。如果输入数组是逆序排列的，将出现最坏情况。平均情况与最坏情况一样，其时间代价是(n2)。
 */
public class InsertSort implements Sort {
    public static void main(String[] args) {
        InsertSort bubbleSort = new InsertSort();
        System.out.println("InsertSort");
        bubbleSort.test();

    }

    @Override
    public void sort(int[] arr) {
        for (int i = 1; i < arr.length; i++) {
            print(arr);
            //插入的数
            int insertVal = arr[i];
            //被插入的位置(准备和前一个数比较)
            int index = i - 1;
            //如果插入的数比被插入的数小
            while (index >= 0 && insertVal < arr[index]) {
                //将把 arr[index] 向后移动
                arr[index + 1] = arr[index];
                //让 index 向前移动
                index--;
            }
            //把插入的数放入合适位置
            arr[index + 1] = insertVal;
            print(arr);
        }
    }

}
