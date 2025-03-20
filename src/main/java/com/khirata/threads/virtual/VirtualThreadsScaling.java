package com.khirata.threads.virtual;

import java.time.Duration;
import java.time.Instant;

public class VirtualThreadsScaling {

    public static void main(String[] args) {
        Instant start = Instant.now();
        try (var executor = java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 1_000; i++) {
                executor.submit(() -> {
                    System.out.println(Thread.currentThread() + " is working...");
                    try {
                        Thread.sleep(1000); // simulate some work
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            }
        } // automatically shuts down executor

        Instant end = Instant.now();
        System.out.println("Virtual Threads execution time: " + Duration.between(start, end).toMillis() + " ms.");
    }
}
