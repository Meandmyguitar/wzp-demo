package com.wzp.support.list;

/**
 * 单链表
 */
public class MyList {

    public static void main(String[] args) {
        ListNode node1 = new ListNode(1);
        ListNode node2 = new ListNode(2);
        ListNode node3 = new ListNode(3);

        node1.next = node2;
        node2.next = node3;
        node3.next = null;

        traverse(node1);
        ListNode newHead = new ListNode(0);
        headInsert(node1, newHead);
        traverse(newHead);

        ListNode newTail = new ListNode(4);
        tailInsert(node3, newTail);
        traverse(newHead);

        System.out.println(find(newHead, 3));

        ListNode node5 = new ListNode(5);
        insert(node3, node5);
        traverse(newHead);

        delete(newHead, node3);
        traverse(newHead);
    }

    /**
     * 头结点的插入
     */
    public static void headInsert(ListNode head, ListNode newHead) {
        ListNode old = head;
        head = newHead;
        head.next = old;
    }

    /**
     * 尾结点的插入
     */
    public static void tailInsert(ListNode tail, ListNode newHead) {
        ListNode old = tail;
        tail = newHead;
        old.next = tail;
    }

    /**
     * 遍历
     */
    public static void traverse(ListNode head) {
        while (head != null) {
            System.out.print(head.value + " ");
            head = head.next;
        }
        System.out.println();
    }

    /**
     * 查找
     */
    public static int find(ListNode head, int value) {
        int index = -1;//返回的下标
        int count = 0;//记录循环的次数
        while (head != null) {
            if (head.value == value) {
                index = count;
                return index;
            }
            count++;
            head = head.next;
        }
        return index;
    }

    /**
     * 插入 在p节点的后面插入s节点
     */
    public static void insert(ListNode p, ListNode s) {
        ListNode next = p.next;
        p.next = s;
        s.next = next;
    }

    /**
     * 删除
     */
    public static void delete(ListNode head, ListNode delNode) {
        if (delNode != null) {
            if (delNode.next != null) {
                ListNode qNext = delNode.next;
                delNode.value = qNext.value;
                //删除掉qNext
                delNode.next = qNext.next;
                qNext = null;
            } else {
                //删除最后一个元素的情况
                while (head != null) {
                    if (head.next != null && head.next == delNode) {
                        head.next = null;
                        break;
                    }
                    head = head.next;
                }
            }
        }
    }

}
