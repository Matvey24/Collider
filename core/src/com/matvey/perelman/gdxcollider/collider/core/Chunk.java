package com.matvey.perelman.gdxcollider.collider.core;

import java.util.ArrayList;

public class Chunk<T extends Dynamic<T>> {
    public final Collider<T> collider;
    public final ArrayList<T> objects;
    public final ArrayList<T> stationary;
    public final ArrayList<Chunk<T>> near; // near chunks to update just collisions, including this chunk
    public int x, y;
    public Chunk(Collider<T> collider){    // assuming overriding constructor
        this.collider = collider;
        objects = new ArrayList<>();
        stationary = new ArrayList<>();
        near = new ArrayList<>();
    }
    public void collisions(T d){
        for(T obj: objects){
            if(obj == d)
                continue;
            double time = collider.calc_time(d, obj);
            if(time < d.col_time && (time < obj.col_time)){
                d.col_time = time;
                d.col_with = obj;
            }
        }
    }
    public void bounds(T d){
        for(T obj: stationary){
            double time = collider.calc_time_static(d, obj);
            if(time < d.col_time){
                d.col_time = time;
                d.col_with = obj;
            }
        }
    }
}
