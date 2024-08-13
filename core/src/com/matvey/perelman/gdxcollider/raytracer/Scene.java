package com.matvey.perelman.gdxcollider.raytracer;

import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector3;
import com.matvey.perelman.gdxcollider.raytracer.materials.ColorMaterial;
import com.matvey.perelman.gdxcollider.raytracer.materials.LightMaterial;
import com.matvey.perelman.gdxcollider.raytracer.materials.Material;
import com.matvey.perelman.gdxcollider.raytracer.objects.Box;
import com.matvey.perelman.gdxcollider.raytracer.objects.MDot;
import com.matvey.perelman.gdxcollider.raytracer.objects.Mandelbulb;
import com.matvey.perelman.gdxcollider.raytracer.objects.Sphere;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class Scene {
    public final ArrayList<MDot> objects;
    public Scene(){
        objects = new ArrayList<>();
    }
    public Scene add(MDot obj){
        objects.add(obj);
        return this;
    }
    public ByteBuffer update(){
        ArrayList<Material> materials = new ArrayList<>();
        for(MDot dot: objects) {
            if(!materials.contains(dot.m))
                materials.add(dot.m);
        }
        int s = 8;
        for(MDot dot: objects)
            s += object_size(dot);
        int full_len = s;
        for(Material m: materials)
            full_len += material_size(m);
        ByteBuffer bb = ByteBuffer.allocate(full_len + 4);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt(full_len / 4 + 1);
        bb.putInt(objects.size());
        for(MDot dot: objects){
            bb.putInt(object_index(dot));
            putVector(bb, dot.pos);
            putMatrix(bb, dot.rot);

            int idx = materials.indexOf(dot.m);
            int off = s / 4;
            for(int i = 0; i < idx; ++i)
                off += material_size(materials.get(i)) / 4;

            bb.putInt(off);

            if(dot instanceof Sphere)
                bb.putFloat(((Sphere) dot).rad * ((Sphere) dot).rad);
            else if(dot instanceof Box){
                bb.putFloat(((Box) dot).bounds.x);
                bb.putFloat(((Box) dot).bounds.y);
                bb.putFloat(((Box) dot).bounds.z);
            }else if(dot instanceof Mandelbulb){
                bb.putFloat(((Mandelbulb) dot).param);
            }else
                throw new RuntimeException("Unknown object: " + dot);
        }
        bb.putInt(materials.size());
        for(Material m: materials){
            bb.putInt(material_index(m));
            if(m instanceof ColorMaterial) {
                bb.putFloat(((ColorMaterial) m).diffuse.r);
                bb.putFloat(((ColorMaterial) m).diffuse.g);
                bb.putFloat(((ColorMaterial) m).diffuse.b);
                bb.putFloat(((ColorMaterial) m).reflective);
            }else if(m instanceof LightMaterial){
                bb.putFloat(((LightMaterial) m).color.r);
                bb.putFloat(((LightMaterial) m).color.g);
                bb.putFloat(((LightMaterial) m).color.b);
            }else
                throw new RuntimeException("Unknown material: " + m);
        }
        return bb;
    }
    public int material_index(Material m){
        if(m instanceof ColorMaterial)
            return 0;
        if(m instanceof LightMaterial)
            return 1;
        throw new RuntimeException("Unknown material: " + m);
    }
    public int material_size(Material m){
        if(m instanceof ColorMaterial)
            return 20;
        if(m instanceof LightMaterial)
            return 16;
        throw new RuntimeException("Unknown material: " + m);
    }
    public int object_index(MDot d){
        if(d instanceof Sphere)
            return 1;
        if(d instanceof Box)
            return 2;
        if(d instanceof Mandelbulb)
            return 4;
        throw new RuntimeException("Undefined object: " + d);
    }
    public int object_size(MDot d){
        if(d instanceof Sphere)
            return 60;
        if(d instanceof Mandelbulb)
            return 60;
        if(d instanceof Box)
            return 68;
        throw new RuntimeException("Unknown object: " + d);
    }
    public static void putVector(ByteBuffer buffer, Vector3 vec){
        buffer.putFloat(vec.x);
        buffer.putFloat(vec.y);
        buffer.putFloat(vec.z);
    }
    public static void putMatrix(ByteBuffer buffer, Matrix3 mat){
        buffer.putFloat(mat.val[0]);
        buffer.putFloat(mat.val[1]);
        buffer.putFloat(mat.val[2]);
        buffer.putFloat(mat.val[3]);
        buffer.putFloat(mat.val[4]);
        buffer.putFloat(mat.val[5]);
        buffer.putFloat(mat.val[6]);
        buffer.putFloat(mat.val[7]);
        buffer.putFloat(mat.val[8]);
    }
}
