package com.wzp.util.highconcurrency;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class HighConcurrencyTest {

    public static final ExecutorService executor = Executors.newFixedThreadPool(4);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        futureMethod();
        completableFutureMethod();
        guavaListenableFutureMethod();
    }

    private static void futureMethod() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {

        }
        Future<String> submit1 = executor.submit(HighConcurrencyTest::test1);
        Future<String> submit2 = executor.submit(HighConcurrencyTest::test1);
        Future<String> submit3 = executor.submit(HighConcurrencyTest::test1);
        Future<String> submit4 = executor.submit(HighConcurrencyTest::test1);
        List<Future<String>> futures = Arrays.asList(submit1, submit2, submit3, submit4);
        List<String> collect = futures.stream().map(future -> {
            try {
                return future.get();
            } catch (Exception e) {
                return null;
            }
        }).collect(Collectors.toList());
        System.out.println(collect);
        System.out.println("Future方式耗时：" + (System.currentTimeMillis() - start) + "ms");
        System.out.println();
    }

    private static void completableFutureMethod() {
        long start = System.currentTimeMillis();
        ArrayList<CompletableFuture<String>> futures = new ArrayList<>(4);
        futures.add(CompletableFuture.supplyAsync(HighConcurrencyTest::test1, executor));
        futures.add(CompletableFuture.supplyAsync(HighConcurrencyTest::test1, executor));
        futures.add(CompletableFuture.supplyAsync(HighConcurrencyTest::test1, executor));
        futures.add(CompletableFuture.supplyAsync(HighConcurrencyTest::test1, executor));

        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[4]));

        CompletableFuture<List<String>> listCompletableFuture = allOf.thenApplyAsync(value ->
                futures.stream().map(CompletableFuture::join).collect(Collectors.toList()), executor);

        List<String> join = listCompletableFuture.join();
        System.out.println(join);
        System.out.println("CompletableFuture方式耗时：" + (System.currentTimeMillis() - start) + "ms");
        System.out.println();
    }

    private static void guavaListenableFutureMethod() throws ExecutionException, InterruptedException {
        long start = System.currentTimeMillis();
        ListeningExecutorService listeningExecutorService = MoreExecutors.listeningDecorator(executor);

        ListenableFuture<String> submit1 = listeningExecutorService.submit(HighConcurrencyTest::test1);
        ListenableFuture<String> submit2 = listeningExecutorService.submit(HighConcurrencyTest::test1);
        ListenableFuture<String> submit3 = listeningExecutorService.submit(HighConcurrencyTest::test1);
        ListenableFuture<String> submit4 = listeningExecutorService.submit(HighConcurrencyTest::test1);

        ListenableFuture<List<String>> listListenableFuture = Futures.allAsList(submit1, submit2, submit3, submit4);
        List<String> join = listListenableFuture.get();
        System.out.println(join);
        System.out.println("GuavaListenableFuture方式耗时：" + (System.currentTimeMillis() - start) + "ms");
        System.out.println();
    }

    private static String test1() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "test1";
    }
}
