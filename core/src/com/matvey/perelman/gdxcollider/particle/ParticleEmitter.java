package com.matvey.perelman.gdxcollider.particle;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.matvey.perelman.gdxcollider.TextureGenerator;

import java.util.Comparator;
import java.util.Stack;
import java.util.TreeSet;

public class ParticleEmitter {
    private final Texture text;
    private final Stack<Particle> passive;
    private final TreeSet<Particle> active;
    private final Vector2 tmp;
    private long index = 0;
    public ParticleEmitter(){
        text = TextureGenerator.genPixel(Color.ORANGE);
        tmp = new Vector2();
        passive = new Stack<>();
        active = new TreeSet<>(Comparator.comparingLong((a)->a.index));
    }
    public void update(float dt){
        if(active.isEmpty())
            return;
        Particle first = active.first();
        while(first != null){
            if(first.update(dt)){
                Particle next = active.higher(first);
                active.remove(first);
                passive.push(first);
                first = next;
            }else{
                first = active.higher(first);
            }
        }
    }
    public void render(Batch b){
        if(active.isEmpty())
            return;
        for(Particle p: active){
            p.render(b);
        }
    }
    public void addParticle(float x, float y, float time, float vx, float vy){
        Particle p;
        if(passive.empty())
            p = new Particle();
        else
            p = passive.pop();
        p.set(text, x, y, vx, vy, time);
        p.index = index++;
        active.add(p);
    }
    public void addParticle(float x, float y, float time){
        tmp.setToRandomDirection();
        tmp.scl(MathUtils.random(100, 100));
//        tmp.setZero();
        addParticle(x, y, time, tmp.x, tmp.y);
    }
    public void close(){
        text.dispose();
    }
}
