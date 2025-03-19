package com.khirata.threads.sync;

public class SyncCounter implements Runnable{

    int counter;

    @Override
    public synchronized void run() {
        counter ++;
        try {
            Thread.sleep(1); // simulate some work
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println(Thread.currentThread().getName() + " is running. Counter: " + counter);
    }
}
