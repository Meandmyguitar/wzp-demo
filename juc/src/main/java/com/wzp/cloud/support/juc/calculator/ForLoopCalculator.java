package com.wzp.cloud.support.juc.calculator;

import java.time.Duration;
import java.time.Instant;
import java.util.stream.LongStream;

public class ForLoopCalculator implements Calculator {

    @Override
    public long sumUp(long[] numbers) {
        long total = 0;
        for (long i : numbers) {
            total += i;
        }
        return total;
    }

}