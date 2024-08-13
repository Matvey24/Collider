package com.matvey.perelman.gdxcollider.raytracer.objects;


import com.matvey.perelman.gdxcollider.raytracer.materials.Material;

public class Sphere extends MDot{
    public float rad;
    public Sphere(Material m, float rad){
        super(m);
        this.rad = rad;
    }
    public Sphere(Material m){
        super(m);
    }
}
