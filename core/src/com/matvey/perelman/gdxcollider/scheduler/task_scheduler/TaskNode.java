package com.matvey.perelman.gdxcollider.scheduler.task_scheduler;


import com.matvey.perelman.gdxcollider.scheduler.pools.UID;

import java.util.function.DoubleConsumer;

public class TaskNode implements UID, Comparable<TaskNode>{
    private final TaskScheduler scheduler;
    //used by TreeSet for determining the order of tasks, which have the same time
    public long uid;
    public Runnable on_cancel;
    double time;
    boolean pinned;
    DoubleConsumer task;

    TaskNode(TaskScheduler scheduler){
        this.scheduler = scheduler;
        time = Double.POSITIVE_INFINITY;
    }

    public double time() {
        return time;
    }

    //called from UIDPool, when creating new task
    //or called from game loader to restore previous game state to save determinism
    public void init(long uid){
        if(!pinned){
            this.uid = uid;
            return;
        }
        double tm = time;
        scheduler.unpin(this);
        this.uid = uid;
        scheduler.pin(this, tm);
    }

    public long uid() {
        return uid;
    }


    public void unpin(){
        if(!pinned)
            return;
        scheduler.unpin(this);
    }

    public void pin(double new_time){
        unpin();
        scheduler.pin(this, new_time);
    }

    public boolean pinned(){
        return pinned;
    }
    public void cancel(){
        if(pinned)
            scheduler.unpin(this);
        if(on_cancel != null) {
            on_cancel.run();
            on_cancel = null;
        }
        scheduler.dispose(this);
    }

    public void set(DoubleConsumer task){
        this.task = task;
    }

    @Override
    public int compareTo(TaskNode o) {
        int r = Double.compare(time, o.time);
        if(r == 0)
            return Long.compare(uid, o.uid);
        return r;
    }

    public TaskNode copy(){
        TaskNode n = scheduler.createTask();
        n.task = task;
        n.on_cancel = on_cancel;
        return n;
    }
}