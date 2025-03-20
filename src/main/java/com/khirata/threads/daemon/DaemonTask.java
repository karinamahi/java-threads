package com.khirata.threads.daemon;

public class DaemonTask implements Runnable {

    @Override
    public void run() {
        while (true) {
            System.out.println("Daemon thread running...");
            try {
                Thread.sleep(1000); // simulating background work
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
