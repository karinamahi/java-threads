package com.khirata.threads.daemon;

public class DaemonExample {

    public static void main(String[] args) {
        Thread daemonThread = new Thread(new DaemonTask());
        daemonThread.setDaemon(true); // mark this thread as a daemon

        daemonThread.start();

        try {
            Thread.sleep(3000); // main thread runs for 3 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Main thread finished. Daemon thread will stop automatically.");
    }
}
