package com.khirata.threads.parallel;

public class App {
    public static void main(String[] args) {
        Counter task = new Counter();

        Thread thread1 = new Thread(task, "Thread 1");
        Thread thread2 = new Thread(task, "Thread 2");
        Thread thread3 = new Thread(task, "Thread 3");
        Thread thread4 = new Thread(task, "Thread 4");
        Thread thread5 = new Thread(task, "Thread 5");
        Thread thread6 = new Thread(task, "Thread 6");
        Thread thread7 = new Thread(task, "Thread 7");
        Thread thread8 = new Thread(task, "Thread 8");

        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();
        thread5.start();
        thread6.start();
        thread7.start();
        thread8.start();
    }
}
