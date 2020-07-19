package com.demo.msgbus;

import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@SuppressWarnings("WeakerAccess")
public final class ReactiveAmqpMessageBusOption {

    public static ReactiveAmqpMessageBusOption of(String group) {
        return new ReactiveAmqpMessageBusOption().group(group);
    }

    private String group;

    private Scheduler scheduler = Schedulers.elastic();

    private int parallelism = 4;

    private int consumeMaxRetryNum = 3;

    private int backpressureBufferSize = 10000;

    private ReactiveAmqpMessageBusOption() {
    }

    public String getGroup() {
        return group;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public int getParallelism() {
        return parallelism;
    }

    /**
     * 当事件消费失败时的最大重试次数
     */
    public int getConsumeMaxRetryNum() {
        return consumeMaxRetryNum;
    }

    public int getBackpressureBufferSize() {
        return backpressureBufferSize;
    }

    public String getNameOfQueue() {
        return "eventbus." + group;
    }

    public String getNameOfDeadLetterQueue() {
        return "eventbus.dlx." + group;
    }

    public ReactiveAmqpMessageBusOption group(String group) {
        ReactiveAmqpMessageBusOption option = copy();
        option.group = group;
        return option;
    }

    public ReactiveAmqpMessageBusOption scheduler(Scheduler scheduler) {
        ReactiveAmqpMessageBusOption option = copy();
        option.scheduler = scheduler;
        return option;
    }

    public ReactiveAmqpMessageBusOption parallelism(int parallelism) {
        ReactiveAmqpMessageBusOption option = copy();
        option.parallelism = parallelism;
        return option;
    }

    public ReactiveAmqpMessageBusOption consumeMaxRetryNum(int size) {
        ReactiveAmqpMessageBusOption option = copy();
        option.consumeMaxRetryNum = size;
        return option;
    }

    public ReactiveAmqpMessageBusOption backpressureBufferSize(int size) {
        ReactiveAmqpMessageBusOption option = copy();
        option.backpressureBufferSize = size;
        return option;
    }

    protected ReactiveAmqpMessageBusOption copy() {
        ReactiveAmqpMessageBusOption option = new ReactiveAmqpMessageBusOption();
        option.group = group;
        option.scheduler = scheduler;
        option.parallelism = parallelism;
        option.consumeMaxRetryNum = consumeMaxRetryNum;
        option.backpressureBufferSize = backpressureBufferSize;
        return option;
    }
}
