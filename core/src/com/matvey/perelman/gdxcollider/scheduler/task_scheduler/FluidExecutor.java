package com.matvey.perelman.gdxcollider.scheduler.task_scheduler;

import com.badlogic.gdx.Gdx;
import com.matvey.perelman.gdxcollider.AverageValue;

import java.util.Timer;
import java.util.TimerTask;

public class FluidExecutor implements AutoCloseable {
    public static final float MINIMUM_UPDATE_TIME = 0.001f;
    private final Timer timer;
    private final TaskScheduler scheduler;
    private final AverageValue.AverageFloat avg_frame_time;
    private long run_period;
    private float planning_time;
    private float target_frame_time;

    public FluidExecutor(TaskScheduler scheduler) {
        this.timer = new Timer(true);
        this.scheduler = scheduler;
        avg_frame_time = new AverageValue.AverageFloat();
        avg_frame_time.capacity = 10;
        target_fps(60);
    }

    public void update(float frame_time) {
        avg_frame_time.add(frame_time);
        float delta = avg_frame_time.avg * 0.9f - target_frame_time;

        planning_time -= delta;

        if(planning_time > target_frame_time)
            planning_time = target_frame_time;

        if (planning_time < MINIMUM_UPDATE_TIME)
            planning_time = MINIMUM_UPDATE_TIME;
        run_period = (long) (1000 * planning_time); // period in milliseconds
    }

    public void target_fps(float fps) {
        target_frame_time = 1 / fps;
        planning_time = target_frame_time;
        run_period = (long) (1000 * planning_time);
    }

    //Runs scheduler for perfect amount of milliseconds or until end
    public void run(double end) {
        scheduler.stop = false;
        Interrupter interrupter = new Interrupter();
        timer.schedule(interrupter, run_period);
        scheduler.runUntil(end);
        interrupter.cancel();
    }

    @Override
    public void close() {
        timer.cancel();
    }

    private class Interrupter extends TimerTask {
        public void run() {
            scheduler.stop = true;
        }
    }
}
