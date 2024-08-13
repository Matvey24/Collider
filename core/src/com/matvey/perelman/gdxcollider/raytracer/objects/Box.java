package com.matvey.perelman.gdxcollider.raytracer.objects;

import com.badlogic.gdx.math.Vector3;
import com.matvey.perelman.gdxcollider.raytracer.materials.Material;

public class Box extends MDot{
    public final Vector3 bounds;
    public Box(Material m){
        super(m);
        bounds = new Vector3();
    }
    public Box(Material m, float hw, float hh, float hd){
        super(m);
        bounds = new Vector3(hw, hh, hd);
    }
}
