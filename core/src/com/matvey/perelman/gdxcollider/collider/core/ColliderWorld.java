package com.matvey.perelman.gdxcollider.collider.core;

import com.matvey.perelman.gdxcollider.GdxCollider;
import com.matvey.perelman.gdxcollider.collider.twodim.objects.Sphere2D;
import com.matvey.perelman.gdxcollider.scheduler.task_scheduler.TaskScheduler;

import java.util.ArrayList;

public class ColliderWorld<T extends Dynamic<T>> {
    public final TaskScheduler scheduler;
    public final Collider<T> collider;
    public final ArrayList<T> objects;

    public ColliderWorld(Collider<T> collider, TaskScheduler scheduler){
        this.collider = collider;
        objects = new ArrayList<>();
        this.scheduler = scheduler;
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
        d.unpin();

        d.chunk.bounds(d);
        T bound = d.col_with;
        for(Chunk<T> c: d.chunk.near){
            if(c == null)
                continue;
            c.collisions(d);
        }

        if(d.col_with == null)
            return;

//        if(d.col_time < scheduler.time - 1E-5){
//            Sphere2D sp = (Sphere2D)d;
//            GdxCollider.instance.alert(sp);
//        }
        if(d.col_with == bound){
            d.pin();
            return;
        }

        T third = d.col_with.col_with;
        d.col_with.col_with = d;
        d.col_with.col_time = d.col_time;
        d.col_with.pin();
        d.pin();

        if(third != null && third != d && third.col_with != null)
            notifyObjectAdded(third);
    }
    public void collide(T a, double time){
        T b = a.col_with;
        a.col_task = a.col_task.copy();
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
    public void update(float time){
        for(T d: objects){
            d.update(time);
        }
    }
}
