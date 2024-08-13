package com.matvey.perelman.gdxcollider.particle;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.matvey.perelman.gdxcollider.TextureGenerator;

public class Particle {
    public long index;
    private float ttl;
    private Texture pixel;
    private final Vector2 pos;
    private final Vector2 vel;

    public Particle(){
        pos = new Vector2();
        vel = new Vector2();
    }
    public void set(Texture col, float x, float y, float vx, float vy, float ttl){
        this.pixel = col;
        this.pos.set(x, y);
        this.vel.set(vx, vy);
        this.ttl = ttl;
    }
    public boolean update(float dt){
        ttl -= dt;
        if(ttl <= 0) {
            ttl = 0;
            return true;
        }
        pos.mulAdd(vel, dt);
        return false;
    }
    public float getTime(){
        return ttl;
    }
    public void render(Batch b){
        TextureGenerator.drawLine(b, pixel, pos.x, pos.y, pos.x + vel.x / 100, pos.y + vel.y / 100);
    }
}
