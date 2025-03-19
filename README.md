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