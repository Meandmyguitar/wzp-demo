package com.wzp.cloud.support.juc.conditionThread;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

public class CyclicBarrierDemo {

    public static void main(String[] args) {
//        cyclicBarrierTest();
        SummaryCyclicBarrierTest();
    }

    public static void cyclicBarrierTest() {
        CyclicBarrier cyclicBarrier = new CyclicBarrier(7, () -> {
            System.out.println("====召唤神龙=====");
        });
        for (int i = 1; i <= 7; i++) {
            final int tempInt = i;
            new Thread(() -> {
                System.out.println(Thread.currentThread().getName() + "\t收集到第" + tempInt + "颗龙珠");
                try {
                    cyclicBarrier.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }
            }, "" + i).start();
        }
    }


    private static void SummaryCyclicBarrierTest() {
        ConcurrentHashMap<String, String> concurrentHashMap = new ConcurrentHashMap<>();
        CyclicBarrier cyclicBarrier = new CyclicBarrier(7, () -> {
            System.out.println("====召唤神龙=====" + concurrentHashMap);
        });
        for (int i = 1; i <= 7; i++) {
            int finalI = i;
            new Thread(() -> {
                concurrentHashMap.put("神龙" + finalI, getChar(finalI));
                try {
                    cyclicBarrier.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private static String getChar(int i) {
        return i + "";
    }


}
