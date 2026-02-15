package com.matvey.perelman.gdxcollider.collider.core;

import com.matvey.perelman.gdxcollider.scheduler.task_scheduler.TaskNode;

public abstract class Dynamic<T extends Dynamic<T>> {
    public TaskNode col_task;
    public T col_with;
    public double col_time;
    public Chunk<T> chunk;

    public void pin(){
        if(col_with.col_task != null && col_with.col_task.pinned()) {
            return;
        }
        col_task.pin(col_time);
    }
    public void unpin(){
        col_with = null;
        col_task.unpin();
        col_time = col_task.time();
    }
    public abstract void update(double time);
}
