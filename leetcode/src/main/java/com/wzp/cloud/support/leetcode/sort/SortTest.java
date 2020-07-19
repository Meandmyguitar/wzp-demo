package com.wzp.cloud.support.leetcode.sort;

public class SortTest {

    //冒泡排序
    public void bubbleSort(int[] a) {
        int len = a.length;
        for (int i = 0; i < len; i++) {
            for (int j = 0; j < len - i - 1; j++) {//注意第二重循环的条件
                if (a[j] > a[j + 1]) {
                    int temp = a[j];
                    a[j] = a[j + 1];
                    a[j + 1] = temp;
                }
            }
        }
    }

    //快速排序
    public void insertSort(int[] a) {
        int len = a.length;//单独把数组长度拿出来，提高效率
        int insertNum;//要插入的数
        for (int i = 1; i < len; i++) {//因为第一次不用，所以从1开始
            insertNum = a[i];
            int j = i - 1;//序列元素个数
            while (j >= 0 && a[j] > insertNum) {//从后往前循环，将大于insertNum的数向后移动
                a[j + 1] = a[j];//元素向后移动
                j--;
            }
            a[j + 1] = insertNum;//找到位置，插入当前元素
        }
    }
}
