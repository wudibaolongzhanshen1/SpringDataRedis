package cn.iocoder.boot.hmdianping.util;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class TransactionUtils {
    /**
     * 仅在当前事务成功提交后执行任务
     */
    public static void doAfterCommit(Runnable task) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    task.run();
                }
            });
        } else {
            // 如果当前没有事务，直接执行
            task.run();
        }
    }
}