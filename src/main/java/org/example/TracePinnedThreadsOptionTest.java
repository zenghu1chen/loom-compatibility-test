package org.example;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Using the VM parameter -Djdk.tracePinnedThreads has a probability of causing the application to hang, while it can run normally without using it.
 * @author zenghu1chen
 * @date 2023/8/3 10:58
 */
public class TracePinnedThreadsOptionTest {

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < 100; i++) {
            test();
        }
        System.out.println("end");
    }

    private static void test() throws InterruptedException {
        int loopCount = 1000;
        CountDownLatch countDownLatch = new CountDownLatch(loopCount);
        for (int i = 0; i < loopCount; i++) {
            Thread thread = new Thread(() -> {
                runInSynchronized(countDownLatch);
            });
            thread.setDaemon(false);
            thread.start();
        }
        countDownLatch.await();
        if (countDownLatch.getCount() == 0) {
            System.out.println("success");
        } else {
            throw new RuntimeException("fail");
        }
    }

    private static void runInSynchronized(CountDownLatch countDownLatch) {
        Thread.ofVirtual().start(() -> {
            synchronized (UUID.randomUUID()) {
                try {
                    System.out.print("11");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    countDownLatch.countDown();
                }
            }
        });
    }

    private static void runInReentrantLock(CountDownLatch countDownLatch) {
        Thread.ofVirtual().start(() -> {
            ReentrantLock reentrantLock = new ReentrantLock();
            reentrantLock.lock();
            try {
                System.out.print("11");
            } finally {
                countDownLatch.countDown();
                reentrantLock.unlock();
            }
        });
    }

}
