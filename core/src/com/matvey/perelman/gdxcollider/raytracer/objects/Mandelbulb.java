package com.matvey.perelman.gdxcollider.raytracer.objects;


import com.matvey.perelman.gdxcollider.raytracer.materials.Material;

public class Mandelbulb extends MDot{
    public float param;
    public Mandelbulb(Material m, float param){
        super(m);
        this.param = param;
    }
}
