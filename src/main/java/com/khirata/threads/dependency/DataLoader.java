package com.khirata.threads.dependency;

public class DataLoader implements Runnable {

    @Override
    public void run() {
        System.out.println("Loading data...");
        try {
            Thread.sleep(2000); // simulating data loading
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Data loading completed.");
    }
}
