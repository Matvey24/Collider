package com.matvey.perelman.gdxcollider.collider.core;

import com.matvey.perelman.gdxcollider.scheduler.task_scheduler.TaskScheduler;

import java.util.ArrayList;

public class ColliderWorld<T extends Dynamic<T>> {
    public final TaskScheduler scheduler;
    public final Collider<T> collider;
    public final ArrayList<T> objects;
    public double time;

    public ColliderWorld(Collider<T> collider){
        this.collider = collider;
        objects = new ArrayList<>();
        scheduler = new TaskScheduler();
    }
    public void addObject(T dim){
        dim.col_task = scheduler.createTask();
        dim.col_task.set(time -> collide(dim, time));
        dim.chunk.objects.add(dim);
        notifyObjectAdded(dim);
        objects.add(dim);
    }
    public void removeAll(){
        for(T dim: objects){
            dim.chunk.objects.remove(dim);
            dim.col_task.cancel();
            dim.col_task = null;
            dim.col_with = null;
        }
        objects.clear();
    }

    public void notifyObjectAdded(T d){//changed velocity, position or size
        d.col_with = null;
        d.col_task.unpin();

        double time_min = d.chunk.bounds(d);
        T oth = d.col_with;
        T bound = oth;

        for(Chunk<T> c: d.chunk.near){
            double time = c.collisions(d);
            if(time < time_min){
                time_min = time;
                oth = d.col_with;
            }
        }

        if(oth == null) {
            d.col_with = null;
            return;
        }

        if(oth == bound){
            d.col_with = bound;
            d.col_task.pin(time_min);
            return;
        }

        T third = oth.col_with;
        oth.col_with = d;
        oth.col_task.pin(time_min);
        d.col_with = oth;
        d.col_task.pin(time_min);

        if(third != null && third.col_with != null)
            notifyObjectAdded(third);
    }
    public void collide(T a, double time){
        T b = a.col_with;
        a.col_task = a.col_task.copy();
//        if(time >= 0.6234){
//            System.out.println("Hello world");
//        }
        if(b.col_with == null){ //static object or trigger
            collider.collide_static(a, b, time);
            a.chunk.objects.remove(a);
            a.chunk = b.chunk;
            a.chunk.objects.add(a);
            notifyObjectAdded(a);
            return;
        }

        collider.collide(a, b, time);

        notifyObjectAdded(a);
        notifyObjectAdded(b);
    }
    public void update(float dt){
        time += dt;
        time = scheduler.runUntil(time);

        for(T d: objects){
            d.update((float)time);
        }
    }
}
