package com.matvey.perelman.gdxcollider.collider.core;

import java.util.ArrayList;

public class Chunk<T extends Dynamic<T>> {
    public final Collider<T> collider;
    public final ArrayList<T> objects;
    public final ArrayList<T> stationary;    // stationary.chunk() returns target chunk
    public final ArrayList<Chunk<T>> near; // near chunks to update just collisions, including this chunk
    public int x, y;
    public Chunk(Collider<T> collider){    // assuming overriding constructor
        this.collider = collider;
        objects = new ArrayList<>();
        stationary = new ArrayList<>();
        near = new ArrayList<>();
    }
    public double collisions(T d){
        double time_min = Double.POSITIVE_INFINITY;
        T oth = null;

        for(T obj: objects){
            if(obj == d)
                continue;
            double time = collider.calc_time(d, obj);
            if(time < time_min && (time < obj.col_task.time())){
                time_min = time;
                oth = obj;
            }
        }
        d.col_with = oth;
        return time_min;
    }
    public double bounds(T d){
        double time_min = Double.POSITIVE_INFINITY;
        T oth = null;

        for(T obj: stationary){
            double time = collider.calc_time_static(d, obj);
            if(time < time_min){
                time_min = time;
                oth = obj;
            }
        }
        d.col_with = oth;
        return time_min;
    }
}
