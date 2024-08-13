package com.matvey.perelman.gdxcollider.raytracer.materials;


import com.badlogic.gdx.graphics.Color;

public class ColorMaterial extends Material{
    public final Color diffuse;
    public float reflective;
    public ColorMaterial(){
        diffuse = new Color(Color.WHITE);
        reflective = 0.1f;
    }
    public ColorMaterial(Color c){
        diffuse = new Color(c);
        reflective = 0.1f;
    }
    public ColorMaterial(Color c, float reflective){
        diffuse = new Color(c);
        this.reflective = reflective;
    }
    public ColorMaterial(float reflective){
        diffuse = new Color(Color.WHITE);
        this.reflective = reflective;
    }
    public ColorMaterial setMirror(){
        reflective = 1;
        return this;
    }
}
