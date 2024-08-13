package com.matvey.perelman.gdxcollider.raytracer.materials;


import com.badlogic.gdx.graphics.Color;

public class LightMaterial extends Material{
    public final Color color;
    public LightMaterial(){
        color = new Color(Color.WHITE);
    }
    public LightMaterial(Color c){
        color = new Color(c);
    }
}
