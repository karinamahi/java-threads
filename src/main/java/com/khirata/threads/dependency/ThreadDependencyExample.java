package com.khirata.threads.dependency;

public class ThreadDependencyExample {

    public static void main(String[] args) {
        Thread dataLoaderThread = new Thread(new DataLoader());

        Thread dataProcessorThread = new Thread(() -> {
            try {
                dataLoaderThread.join(); // waits for dataLoader to complete
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Processing data after loading is complete.");
        });

        dataLoaderThread.start();  // start data loading first
        dataProcessorThread.start(); // start data processing (but it waits)
    }
}
