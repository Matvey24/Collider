package com.matvey.perelman.gdxcollider.scheduler.task_scheduler;

public class Locker {
    private int count;

    // pause the thread until free() is called
    // if free() was called earlier, lock() does not pause the thread
    public synchronized void lock() {
        count--;
        if (count >= 0)
            return;
        try {
            wait();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void free() {
        count++;
        if (count <= 0)
            notify();
    }
}
