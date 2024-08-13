package com.matvey.perelman.gdxcollider.raytracer.objects;

import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector3;

public class Dot {
    public final Vector3 pos;
    public final Matrix3 rot;
    public Dot(){
        pos = new Vector3();
        rot = new Matrix3();
    }
}
