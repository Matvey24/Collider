package com.matvey.perelman.gdxcollider.scheduler.task_scheduler;

import java.util.Timer;
import java.util.TimerTask;

public class FluidExecutor implements AutoCloseable{
    private final Timer timer;
    private final TaskScheduler scheduler;
    private final Interrupter interrupter;

    public long run_period = 10;

    public FluidExecutor(TaskScheduler scheduler){
        this.timer = new Timer(true);
        this.scheduler = scheduler;
        this.interrupter = new Interrupter();
    }

    //Runs scheduler for given amount of milliseconds or until its ends
    public double run(double end){
        scheduler.stop = false;
        timer.schedule(interrupter, run_period);
        double current_time = scheduler.runUntil(end);
        interrupter.cancel();
        return current_time;
    }

    @Override
    public void close() {
        timer.cancel();
    }

    public class Interrupter extends TimerTask{
        @Override
        public void run() {
            scheduler.stop = true;
        }
    }
}
