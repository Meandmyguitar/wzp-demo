package com.wzp.cloud.support.juc.calculator;

import java.time.Duration;
import java.time.Instant;
import java.util.stream.LongStream;

public class Test {


    public static void main(String[] args) {
        long[] numbers = LongStream.rangeClosed(1, 10000000).toArray();

        Instant start = Instant.now();
        Calculator calculator = new ForLoopCalculator();
        long result = calculator.sumUp(numbers);
        Instant end = Instant.now();
        System.out.println("ForLoopCalculator 耗时：" + Duration.between(start, end).toMillis() + "ms");
        System.out.println("ForLoopCalculator 结果为：" + result);
        System.out.println("------------------------");

        start = Instant.now();
        calculator = new ExecutorServiceCalculator();
        result = calculator.sumUp(numbers);
        end = Instant.now();
        System.out.println("ExecutorServiceCalculator 耗时：" + Duration.between(start, end).toMillis() + "ms");
        System.out.println("ExecutorServiceCalculator 结果为：" + result);
        System.out.println("------------------------");

        start = Instant.now();
        calculator = new ForkJoinCalculator();
        result = calculator.sumUp(numbers);
        end = Instant.now();
        System.out.println("ForkJoinCalculator 耗时：" + Duration.between(start, end).toMillis() + "ms");
        System.out.println("ForkJoinCalculator 结果为：" + result);
        System.out.println("-----------------------");

        start = Instant.now();
        result = LongStream.rangeClosed(0, 10000000L).parallel().reduce(0, Long::sum);
        end = Instant.now();
        System.out.println("JDK8并行流 耗时：" + Duration.between(start, end).toMillis() + "ms");
        System.out.println("JDK8并行流 结果为：" + result);

    }


}
