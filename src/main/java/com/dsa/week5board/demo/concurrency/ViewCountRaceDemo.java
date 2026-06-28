package com.dsa.week5board.demo.concurrency;

import java.util.ArrayList;
import java.util.List;

public class ViewCountRaceDemo {

    private static final int THREAD_COUNT = 10;
    private static final int INCREASE_PER_THREAD = 100_000;

    private int unsafeViews = 0;

    public static void main(String[] args) throws InterruptedException {
        ViewCountRaceDemo demo = new ViewCountRaceDemo();
        demo.run();
    }

    private void run() throws InterruptedException {
        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < THREAD_COUNT; i++) {
            Thread thread = new Thread(() -> {
                for (int j = 0; j < INCREASE_PER_THREAD; j++) {
                    unsafeViews++;
                }
            });
            threads.add(thread);
        }

        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }

        int expected = THREAD_COUNT * INCREASE_PER_THREAD;
        System.out.println("expected views = " + expected);
        System.out.println("actual views   = " + unsafeViews);
        System.out.println("lost updates   = " + (expected - unsafeViews));
    }
}
