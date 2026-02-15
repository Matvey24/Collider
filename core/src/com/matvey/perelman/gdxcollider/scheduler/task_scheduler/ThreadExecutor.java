package com.matvey.perelman.gdxcollider.scheduler.task_scheduler;

import java.util.concurrent.*;

public class ThreadExecutor {
    private final TaskScheduler scheduler;
    private final ThreadPoolExecutor executor;
    private final Locker lock;
    private boolean running;
    private RuntimeException ex;
    public Runnable on_finish;
    private double time_end;
    public ThreadExecutor(TaskScheduler scheduler){
        this.scheduler = scheduler;
        executor = new ThreadPoolExecutor(1, 1,
                        1L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1));
        lock = new Locker();
    }

    public void prepare_for_begin(double time_end){
        this.time_end = time_end;
    }

    public void begin() {
        running = true;
        scheduler.stop = false;

        executor.execute(this::run_scheduler);
    }

    private void run_scheduler(){
        try {
            scheduler.runUntil(time_end);
            lock.free();
        } catch (RuntimeException ex) {
            ThreadExecutor.this.ex = ex;
        }
    }

    public void stop(){
        scheduler.stop = true; // it can be already true, if game decided to pause itself
        wait_for_finish();
    }
    public void wait_for_finish(){
        if (!running)
            return;
        running = false;

        lock.lock();
        if(on_finish != null)
            on_finish.run();
        if(ex != null){
            RuntimeException e = ex;
            ex = null;
            throw e;
        }
    }

    public void close(){
        executor.shutdown();
    }
}
