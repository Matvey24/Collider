package com.matvey.perelman.gdxcollider.collider.twodim;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.matvey.perelman.gdxcollider.collider.core.Dynamic;

public abstract class Dynamic2D extends Dynamic<Dynamic2D> {
    public final Vector2 pos;
    public final Vector2 vel;
    public final Vector2 acc;
    public double point_time;
    public final Vector2 cur_pos;

    public Dynamic2D(){
        cur_pos = new Vector2();
        pos = new Vector2();
        vel = new Vector2();
        acc = new Vector2(600, 0);
    }
    public void update(double time){
        float dt = (float)(time - point_time);
        cur_pos.set(pos);
        cur_pos.mulAdd(vel, dt).mulAdd(acc, dt * dt / 2);
    }

    public abstract void render(Batch batch);
}
