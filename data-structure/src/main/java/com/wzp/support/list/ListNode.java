package com.wzp.support.list;

/**
 * 单链表定义
 */
public class ListNode {

    public int value;

    public ListNode next;

    public ListNode(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "ListNode{" +
                "value=" + value +
                ", next=" + next +
                '}';
    }
}
