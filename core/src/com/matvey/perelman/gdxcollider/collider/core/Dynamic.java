package com.matvey.perelman.gdxcollider.collider.core;

import com.matvey.perelman.gdxcollider.scheduler.task_scheduler.TaskNode;

public abstract class Dynamic<T extends Dynamic<T>> {
    public TaskNode col_task;
    public T col_with;
    public Chunk<T> chunk;

    public abstract void update(double time);
}
