package com.wzp.cloud.support.juc.conditionThread;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CountDownLatchDemo {

    public static void main(String[] args) throws InterruptedException {
//        general();
//        countDownLatchTest();
        SummaryCountDownLatchTest();
    }

    private static void SummaryCountDownLatchTest() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(6);
        ConcurrentHashMap<String, String> concurrentHashMap = new ConcurrentHashMap<>();
        for (int i = 1; i <= 6; i++) {
            int finalI = i;
            new Thread(() -> {
                concurrentHashMap.put("大国" + finalI, getChar(finalI));
                countDownLatch.countDown();
            }).start();
        }
        countDownLatch.await();
        System.out.println(concurrentHashMap);
    }

    private static String getChar(int i) {
        return i + "";
    }


    public static void general() {
        for (int i = 1; i <= 6; i++) {
            new Thread(() -> {
                System.out.println(Thread.currentThread().getName() + "\t上完自习，离开教室");
            }, "Thread-->" + i).start();
        }
        while (Thread.activeCount() > 2) {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println(Thread.currentThread().getName() + "\t=====班长最后关门走人");
    }

    public static void countDownLatchTest() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(6);
        for (int i = 1; i <= 6; i++) {
            new Thread(() -> {
                System.out.println(Thread.currentThread().getName() + "\t被灭");
                countDownLatch.countDown();
            }, CountryEnum.forEach_CountryEnum(i).getRetMessage()).start();
        }
        countDownLatch.await();
        System.out.println(Thread.currentThread().getName() + "\t=====秦统一");
    }
}
