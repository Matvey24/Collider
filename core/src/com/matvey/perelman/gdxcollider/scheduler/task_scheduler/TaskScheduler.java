package com.matvey.perelman.gdxcollider.scheduler.task_scheduler;

import com.matvey.perelman.gdxcollider.scheduler.TreeSet;
import com.matvey.perelman.gdxcollider.scheduler.pools.ObjectPool;

import java.util.function.DoubleConsumer;

public class TaskScheduler{
    private final ObjectPool.UIDPool<TaskNode> pool;
    private final TreeSet<TaskNode> queue;
    public double time;
    public boolean stop;
    public int allocations;
    public int events;
    public long delay;
    public TaskScheduler(){
        pool = new ObjectPool.UIDPool<>(() -> new TaskNode(this));
        queue = new TreeSet<>();
    }

    public void clearBuffer(){
        pool.clearBuffer();
    }

    public TaskNode addTask(DoubleConsumer task, double time){
        TaskNode node = pool.get();
        node.task = task;
        pin(node, time);
        return node;
    }

    public boolean isEmpty(){
        return queue.isEmpty();
    }

    public TaskNode createTask(){
        return pool.get();
    }

    void pin(TaskNode node, double time){
        node.time = time;
        queue.add(node);
        node.pinned = true;
        allocations++;
    }
    void unpin(TaskNode node){
        queue.remove(node);
        node.pinned = false;
        node.time = Double.POSITIVE_INFINITY;
    }

    void dispose(TaskNode node){
        node.pinned = false;
        pool.free(node);
    }

    public void runUntil(double end){
        delay = System.nanoTime();
        events = 0;
        allocations = 0;
        while(!queue.isEmpty() && queue.first().time <= end){
            TaskNode node = queue.removeFirst();
            node.task.accept(node.time);
            node.task = null;
            node.on_cancel = null;
            dispose(node);
            time = node.time;
            node.time = Double.POSITIVE_INFINITY;
            events++;
            if(stop){
                delay = System.nanoTime() - delay;
                return;
            }
        }
        time = end;
        delay = System.nanoTime() - delay;
    }
    //runs single event if stop == true, or runs infinite until stop == true, stops if there are no more events
    public void runInfinite(){
        runUntil(Long.MAX_VALUE);
    }
}
