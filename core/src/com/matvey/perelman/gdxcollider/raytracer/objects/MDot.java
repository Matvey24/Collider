package com.matvey.perelman.gdxcollider.raytracer.objects;


import com.matvey.perelman.gdxcollider.raytracer.materials.Material;

public abstract class MDot extends Dot{
    public final Material m;
    public MDot(Material m){
        this.m = m;
    }
}
