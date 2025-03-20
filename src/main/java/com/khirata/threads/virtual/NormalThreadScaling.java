package com.khirata.threads.virtual;

public class NormalThreadScaling {

    public static void main(String[] args) {
        try (var executor = java.util.concurrent.Executors.newThreadPerTaskExecutor(Thread.ofPlatform().factory())) {
            for (int i = 0; i < 1_000_000; i++) {
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
    }
}
