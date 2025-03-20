package com.khirata.threads.virtual;

public class VirtualThreadsExample {

    public static void main(String[] args) {

        Runnable task = () -> {
            System.out.println(Thread.currentThread() + " is running...");
        };

        Thread virtualThread = Thread.startVirtualThread(task);

        try {
            virtualThread.join(); // ensures the main thread waits for completion
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
