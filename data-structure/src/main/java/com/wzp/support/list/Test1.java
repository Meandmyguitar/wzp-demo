package com.wzp.support.list;


/**
 * 反转链表
 * 区链表中间值
 */
public class Test1 {

    public static void main(String[] args) {
        ListNode node1 = new ListNode(1);
        ListNode node2 = new ListNode(2);
        ListNode node3 = new ListNode(3);

        node1.next = node2;
        node2.next = node3;
        node3.next = null;

        System.out.println(getMid(node1).value);

        ListNode head = reverseList(node1);
        while (head != null) {
            System.out.print(head.value + " ");
            head = head.next;
        }
        System.out.println();
    }


    /**
     * 反转链表
     * O(n) O(1)
     */
    public static ListNode reverseList(ListNode head) {
        ListNode pre = null;//当前节点的上一个节点
        ListNode next = null;//当前节点的下一个节点

        while (head != null) {
            //记录下当前节点的下一个节点
            next = head.next;

            //当前节点的上一个节点赋值给当前节点的下一个节点
            head.next = pre;
            //当前节点赋值给当前节点上一个节点
            pre = head;

            //让头节点不停的向后移动
            head = next;
        }
        return pre;
    }

    /**
     * 取中间节点（偶数个取中间节点的前一个节点）
     */
    public static ListNode getMid(ListNode head) {
        if (head == null) {
            return null;
        }
        //快指针走两步，慢指针走两步，快指针到结尾的时候满指针的位置就是中间节点
        ListNode fast = head;
        ListNode slow = head;
        while (fast.next != null && fast.next.next != null) {
            slow = slow.next;
            fast = fast.next.next;
        }
        return slow;
    }

}
