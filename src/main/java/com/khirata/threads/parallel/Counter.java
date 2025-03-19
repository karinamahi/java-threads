package com.khirata.threads.parallel;

public class Counter implements Runnable{

    private int counter;

    @Override
    public void run() {
        counter ++;
        try {
            Thread.sleep(1); // simulate some work
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println(Thread.currentThread().getName() + " is running. Counter: " + counter);
    }
}
