# Java Threads

This is a project I created to study threads. I haven't had the opportunity to dive deep into this subject, so I created this project to learn more about it.

## Running Threads in Parallel

I started by implementing a task called `Counter` that implements a `Runnable`.

```java
public class Counter implements Runnable{

    private int counter;

    @Override
    public void run() {
        counter ++;
        System.out.println(Thread.currentThread().getName() + " is running. Counter: " + counter);
    }
}
```

Then, I created multiple threads (`Thread 1` to `Thread 8`), all sharing the same Counter instance.
```java
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
```

Each thread is competing for CPU time (managed by the OS and JVM thread scheduler).

Thread execution order is non-deterministic – the OS decides which thread gets CPU time at any moment.
```text
Thread 1 is running. Counter: 1
Thread 7 is running. Counter: 7
Thread 2 is running. Counter: 2
Thread 3 is running. Counter: 3
Thread 4 is running. Counter: 4
Thread 6 is running. Counter: 6
Thread 5 is running. Counter: 5
Thread 8 is running. Counter: 8
```
**Why is the output order different?**

The JVM does not guarantee the order in which threads will start executing. Each thread starts independently, but execution depends on the CPU scheduler.

Since all threads share the same task `Counter`, they access the same `counter` variable.


### Key Concept: Race Conditions

Then multiple threads are modifying `counter` at the same time, leading to race conditions.

**Example of Race Condition Effects**

If `Thread 1` and `Thread 2` execute at nearly the same time:

- `Thread 1` reads `counter = 0`
- `Thread 2` reads `counter = 0` (before `Thread 1` increments)
- `Thread 1` increments `counter = 1`
- `Thread 2` increments `counter = 1` (instead of 2!)

Now two threads think `counter = 1`, causing an **incorrect** count. 

Looking at the output, the `counter` value seems to be correct, but the sequence of printed statements is mixed. Since multiple threads read and modify the `counter`, it may just be luck.

Let’s assume the following possible execution flow:

| Thread   | Reads `count` | Increments `count` | Prints                                  |
|----------|--------------|--------------------|-----------------------------------------|
| Thread 1 | 0            | 1                  | "Thread 1 is running. Counter: 1"      |
| Thread 2 | 1            | 2                  | "Thread 2 is running. Counter: 2"      |
| Thread 3 | 2            | 3                  | "Thread 3 is running. Counter: 3"      |
| Thread 4 | 3            | 4                  | "Thread 4 is running. Counter: 4"      |
| Thread 5 | 4            | 5                  | "Thread 5 is running. Counter: 5"      |
| Thread 6 | 5            | 6                  | "Thread 6 is running. Counter: 6"      |
| Thread 7 | 6            | 7                  | "Thread 7 is running. Counter: 7"      |
| Thread 8 | 7            | 8                  | "Thread 8 is running. Counter: 8"      |

Here, even though different threads are running in parallel, each happens to read and update the `counter` in an ideal way, avoiding overlap.

But this is not guaranteed. The threads could have read the same `counter` value before another updated it, leading to incorrect increments.

The scheduling appears sequential, but this is not reliable. Let's try to simulate the race condition.

```java
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
```

Output
```text
Thread 8 is running. Counter: 8
Thread 1 is running. Counter: 5
Thread 2 is running. Counter: 7
Thread 4 is running. Counter: 8
Thread 3 is running. Counter: 8
Thread 6 is running. Counter: 8
Thread 7 is running. Counter: 8
Thread 5 is running. Counter: 8
```
So we can confirm that although the `counter` seemed to be correct at first, this behaviour is not guaranteed.

However, if we want to preserve the order and prevent the race condition, we can synchronize the threads, as we will see in the next section.

## Synchronizing Threads

Instead of executing the threads in parallel, we may want to execute them one by one.

To do this, we use the `synchronnized` in our task class, which prevents a thread from executing that code if another thread is already running it.

This ensures that only one thread at a time modifies the `counter`.
```java
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
```

Then the output is:
```text
Thread 1 is running. Counter: 1
Thread 8 is running. Counter: 2
Thread 7 is running. Counter: 3
Thread 6 is running. Counter: 4
Thread 5 is running. Counter: 5
Thread 4 is running. Counter: 6
Thread 3 is running. Counter: 7
Thread 2 is running. Counter: 8
```
The JVM doesn’t guarantee that `Thread 1` will execute before `Thread 2`, `Thread 3`, etc. Threads start asynchronously, but synchronized forces them to execute one at a time.

So, the first thread to acquire the lock runs first, then the next, and so on. That’s why the `counter` increments correctly (1 to 8), but the thread names appear in a seemingly random order.

## Thread Dependency

Let's imagine a scenario where `Thread A` starts only after `Thread B` finishes its execution.

The following example simulates a **data processing** task that waits for the **data loading** to complete.
```java
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
```

Simply calling the start method in the desired sequence does not guarantee that the `dataProcessor` will wait for the data loading.
```java
public class ThreadDependencyExample {
    
    public static void main(String[] args) {
        Thread dataLoaderThread = new Thread(new DataLoader());

        Thread dataProcessorThread = new Thread(() -> {
            System.out.println("Processing data after loading is complete.");
        });

        dataLoaderThread.start(); 
        dataProcessorThread.start(); 
    }
}
```

As we can see in the output, the `dataProcessor` didn't wait for `dataLoader` to finish its execution.
```text
Loading data...
Processing data after loading is complete.
Data loading completed.
```

A great way to make the `dataProcessor` wait for `dataLoader` is to use the method `join()`
```java
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
```
Then, we have:
```text
Loading data...
Data loading completed.
Processing data after loading is complete.
```

## Daemon Threads

Daemon threads are special types of threads that run in the background and are automatically terminated when all non-daemon (normal) threads finish execution.

If all normal thread finish, all daemon threads are stopped automatically, even if they haven't finished their work.

Let's create a task and set the thread as a daemon thread.

```java
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
```
Output:

```text
Daemon thread running...
Daemon thread running...
Daemon thread running...
Main thread finished. Daemon thread will stop automatically.
```

The daemon thread is forcefully stopped once the main thread finishes, so we should not use a daemon thread if it must complete its task.

## Virtual Threads

Virtual Threads, introduced in Java 19 (preview) and Java 21 (stable), are lightweight, user-mode threads that allow running millions of concurrent tasks efficiently. They are not tied to OS threads, making them much more scalable than traditional threads.

**Why Use Virtual Threads?**

- **Massive Concurrency**: Unlike platform (OS) threads, virtual threads allow millions of concurrent tasks without excessive memory usage.
- **Non-Blocking I/O**: They are perfect for handling tasks like HTTP requests, database queries, and file I/O efficiently.
- **No Thread Pool Tuning Needed**: Unlike thread pools (Executors.newFixedThreadPool(n)), virtual threads eliminate the need to manage pool sizes.

```java
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
```
In the output we can see it is a virtual thread:
```text
VirtualThread[#20]/runnable@ForkJoinPool-1-worker-1 is running...
```
### Scaling Examples

Let's compare virtual threads vs. traditional threads in handling a large number (one million) of concurrent tasks.

```java
public class VirtualThreadsScaling {

    public static void main(String[] args) {
        try (var executor = java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor()) {
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
```
Output:
```text
VirtualThread[#1000026]/runnable@ForkJoinPool-1-worker-3 is working...
VirtualThread[#1000027]/runnable@ForkJoinPool-1-worker-3 is working...
VirtualThread[#1000003]/runnable@ForkJoinPool-1-worker-4 is working...

Process finished with exit code 0
```

It worked with no issues. But if we try this with traditional threads, the system crashes:
```java
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
```

```text
[41.856s][warning][os,thread] Failed to start the native thread for java.lang.Thread "Thread-60207"
Exception in thread "main" java.lang.OutOfMemoryError: unable to create native thread: possibly out of memory or process/resource limits reached
	at java.base/java.lang.Thread.start0(Native Method)
	at java.base/java.lang.Thread.start(Thread.java:1545)
	at java.base/java.lang.System$2.start(System.java:2669)
	at java.base/java.util.concurrent.ThreadPerTaskExecutor.start(ThreadPerTaskExecutor.java:249)
	at java.base/java.util.concurrent.ThreadPerTaskExecutor.submit(ThreadPerTaskExecutor.java:287)
	at java.base/java.util.concurrent.ThreadPerTaskExecutor.submit(ThreadPerTaskExecutor.java:293)
	at com.khirata.threads.virtual.NormalThreadScaling.main(NormalThreadScaling.java:8)

Process finished with exit code 1
```
Creating 1,000,000 platform threads (newThreadPerTaskExecutor) is extremely expensive.

To avoid the system crash without using the virtual threads, we need to use a fixed pool of threads.
```java
public class NormalThreadScalingPool {

    public static void main(String[] args) {

        int availableCors = Runtime.getRuntime().availableProcessors();

        try (var executor = java.util.concurrent.Executors.newFixedThreadPool(availableCors)) {
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
```
However, the thread pool can take considerable longer time to execute than the virtual threads. So, let's compare the execution time.

First, I reduced the loop iterations from 1 million to 1,000. Then, I added a time measurement. 

```java
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

public class NormalThreadScalingPool {

    public static void main(String[] args) {

        int availableCors = Runtime.getRuntime().availableProcessors();

        Instant start = Instant.now();

        try (var executor = java.util.concurrent.Executors.newFixedThreadPool(availableCors)) {
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
        System.out.println("Thread Pool execution time: " + Duration.between(start, end).toMillis() + " ms.");
    }
}
```
Looking at the output, we can see a significant difference, and we can conclude that virtual threads are much more scalable for handling a high number of concurrent tasks.
```text
Virtual Threads execution time: 1192 ms.      # 1,192 seconds
Thread Pool execution time: 251157 ms.        # 4,18595 minutes
```