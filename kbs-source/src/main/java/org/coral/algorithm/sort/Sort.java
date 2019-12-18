package org.coral.algorithm.sort;

public interface Sort {
    void sort(int numbers[]);

    /**
     * 打印数组
     *
     * @param a
     */
    default void print(int[] a) {
        for (int i : a) {
            System.out.print(i + " ");
        }
        System.out.println("\n-----------------");
    }


    /**
     * 交换 a b 索引值
     *
     * @param numbers
     * @param a
     * @param b
     */
    default void swap(int[] numbers, int a, int b) {

        if (numbers[a] == numbers[b]) {
            return;
        }
        numbers[a] = numbers[a] ^ numbers[b];
        numbers[b] = numbers[a] ^ numbers[b];
        numbers[a] = numbers[a] ^ numbers[b];
    }
    default void test(){
        int[] numbers = new int[]{1, 10, 6, 3, 4, 4, 5};
        sort(numbers);
    }
}
