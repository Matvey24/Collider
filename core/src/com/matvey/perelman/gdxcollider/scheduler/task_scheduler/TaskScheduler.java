package com.matvey.perelman.gdxcollider.scheduler.task_scheduler;

import com.matvey.perelman.gdxcollider.scheduler.pools.ObjectPool;

import java.util.Comparator;
import java.util.TreeSet;
import java.util.function.DoubleConsumer;

public class TaskScheduler{
    private final ObjectPool.UIDPool<TaskNode> pool;
    private final TreeSet<TaskNode> queue;
    public boolean stop;
    public int events;
    public TaskScheduler(){
        pool = new ObjectPool.UIDPool<>(() -> new TaskNode(this));
        queue = new TreeSet<>(Comparator.comparingDouble(TaskNode::time)
                .thenComparingLong(TaskNode::uid));
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

    public double runUntil(double end){
        int max_count = 5000;
        int count = 0;
        while(!queue.isEmpty() && queue.first().time <= end){
            TaskNode node = queue.pollFirst();
            node.task.accept(node.time);
            node.task = null;
            node.on_cancel = null;
            dispose(node);
            double time = node.time;
            node.time = Double.POSITIVE_INFINITY;
            count++;
            if(stop || count >= max_count) {
                this.events = count;
//                System.out.println("Overflow");
                return time;
            }
        }
        this.events = count;
        return end;
    }
    //runs single event if stop == true, or runs infinite until stop == true, stops if there are no more events
    public void runInfinite(){
        runUntil(Long.MAX_VALUE);
    }
}
