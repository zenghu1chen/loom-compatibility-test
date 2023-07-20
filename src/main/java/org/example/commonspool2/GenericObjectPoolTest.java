package org.example.commonspool2;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Run with --enable-preview -Djdk.virtualThreadScheduler.parallelism=4
 * This test case is likely to hang between 1-20 minutes.
 * The default value of jdk.virtualThreadScheduler.parallelism is the number of CPU cores. In order to reproduce deadlock situations,
 * this value needs to be set to be consistent with or smaller than MAX_TOTAL.
 * @author zenghu1chen
 * @date 2023/7/19 14:32
 */
public class GenericObjectPoolTest {
    public static final int MAX_TOTAL = 8;
    public static final int MAX_IDLE = 8;

    // Create a static instance of GenericObjectPool
    public static final GenericObjectPool<Object> pool = initPool();

    // Atomic counters to keep track of task start and completion counts
    static AtomicInteger end = new AtomicInteger(0);
    static AtomicInteger start = new AtomicInteger(0);

    // ExecutorService to manage thread pool
    static ExecutorService executorService = getExecutor();

    private static ExecutorService getExecutor() {
        // Return a new VirtualThreadPerTaskExecutor
        // return new ThreadPoolExecutor(0, 10, 1L, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    public static void main(String[] args) throws Exception {
        // Create a new thread to continuously print task start and completion counts
        Thread thread = new Thread(() -> {
            for (int i = 0; i < Integer.MAX_VALUE; i++) {
                System.out.println("started task count：" + start.get());
                System.out.println("completed task count：" + end.get());
                try {
                    Thread.sleep(10000L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        thread.start();

        // Run the test 100 times with a delay of 1 seconds between each run
        for (int i = 0; i < 100; i++) {
            test();
            Thread.sleep(1000L);
        }
        System.out.println("end success");
    }

    public static void test() throws InterruptedException {
        int loopCount = 2000;
        CountDownLatch countDownLatch = new CountDownLatch(loopCount);
        end = new AtomicInteger(0);
        start = new AtomicInteger(0);
        for (int j = 0; j < loopCount; j++) {
            // Submit tasks to the executor service
            executorService.submit(() -> run(countDownLatch));
        }

        System.out.println("all task start ");
        countDownLatch.await();
        if (countDownLatch.getCount() == 0) {
            System.out.println("all task end ");
        } else {
            throw new RuntimeException("failed");
        }
    }

    private static void run(CountDownLatch countDownLatch) {
        try {
            start.incrementAndGet();
            // Borrow an object from the pool
            Object item = pool.borrowObject();
            if (item != null) {
                // Return the object back to the pool and invalidate it
                pool.returnObject(item);
                pool.invalidateObject(item);
            } else {
                System.out.println("item is null");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            // Count down the latch and increment the completion count
            countDownLatch.countDown();
            end.incrementAndGet();
        }
    }

    private static GenericObjectPool<Object> initPool() {
        // Create a new GenericObjectPoolConfig
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMaxTotal(MAX_TOTAL);
        config.setMaxIdle(MAX_IDLE);
        config.setMaxWaitMillis(60000L);
        // Create a new GenericObjectPool with a custom PooledObjectFactory and the config
        GenericObjectPool<Object> pool = new GenericObjectPool<>(new SimplePooledObjectFactory(), config);
        try {
            // Add objects to the pool
            for (int i = 0; i < MAX_TOTAL; i++) {
                pool.addObject();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return pool;
    }
}