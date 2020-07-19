package com.wzp.util.spring;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 关于Spring事务处理的一些工具方法
 */
public class TxUtils {

    /**
     * 在事务提交后进行一些操作
     *
     * @throws IllegalStateException 当前线程不在事务处理范围内
     */
    public static void afterCommit(Runnable runnable) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException("当前线程不在事务处理范围内");
        }

        // 事务提交后发送通知
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                runnable.run();
            }
        });
    }
}
