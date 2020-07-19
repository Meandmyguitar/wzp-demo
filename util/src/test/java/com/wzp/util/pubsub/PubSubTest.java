package com.wzp.util.pubsub;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicLong;

class PubSubTest {

    @Test
    void test() throws Exception {
        ParallelPubSub pub = new ParallelPubSub(5);

        AtomicLong sum = new AtomicLong();
        pub.subscribe("a", (t, m) -> sum.incrementAndGet());

        AtomicLong sum2 = new AtomicLong();
        pub.subscribeSync("a", (t, m) -> sum2.incrementAndGet());

        int loop = 10000;
        for (int i = 0; i < loop; i++) {
            pub.publishAsync("a", i);
            pub.publishSync("a", i);
        }

        pub.close();
        Assertions.assertEquals(loop, sum.get());
        Assertions.assertEquals(loop, sum2.get());
        Assertions.assertEquals(loop, pub.getStatPublishCount());
        Assertions.assertEquals(loop, pub.getStatConsumeCount());
    }
}