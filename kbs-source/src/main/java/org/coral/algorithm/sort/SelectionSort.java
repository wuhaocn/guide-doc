package org.coral.algorithm.sort;

import java.util.Arrays;

/**
 *
 算法步骤:
 首先在未排序序列中找到最小（大）元素，存放到排序序列的起始位置。
 再从剩余未排序元素中继续寻找最小（大）元素，然后放到已排序序列的末尾。
 重复第二步，直到所有元素均排序完毕。
 */
public class SelectionSort implements Sort {
    public static void main(String[] args) {
        int[] numbers = {34, 12, 23, 56, 56, 56, 78};
        SelectionSort selectionSort = new SelectionSort();
        selectionSort.sort(numbers);
        selectionSort.print(numbers);
    }
    @Override
    public void sort(int[] numbers) {

        // 总共要经过 N-1 轮比较
        for (int i = 0; i < numbers.length - 1; i++) {
            int min = i;

            // 每轮需要比较的次数 N-i
            for (int j = i + 1; j < numbers.length; j++) {
                if (numbers[j] < numbers[min]) {
                    // 记录目前能找到的最小值元素的下标
                    min = j;
                }
            }

            // 将找到的最小值和i位置所在的值进行交换
            if (i != min) {
                int tmp = numbers[i];
                numbers[i] = numbers[min];
                numbers[min] = tmp;
            }

        }
    }
}
