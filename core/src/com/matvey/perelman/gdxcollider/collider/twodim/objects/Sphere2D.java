package com.matvey.perelman.gdxcollider.collider.twodim.objects;


import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.matvey.perelman.gdxcollider.GdxCollider;
import com.matvey.perelman.gdxcollider.collider.twodim.Dynamic2D;

import static com.matvey.perelman.gdxcollider.WorldCreator.scale;

public class Sphere2D extends Dynamic2D {
    public float radius;
    public float mass;
    public final Texture texture, pixel;
    public final Color col;
    public boolean sup_er;

    public boolean lost = false;
    boolean warn = false;
    public Sphere2D(Texture texture, Texture pixel, float mass, float radius){
        this.mass = mass;
        this.radius = radius;
        this.texture = texture;
        this.pixel = pixel;
        col = new Color(Color.WHITE);
    }

    private static boolean inside(int chunk_pos, float val){
        return val + 0.001 > chunk_pos && val - 0.001 < chunk_pos + 1;
    }

    public boolean in_bounds(){
        return inside(chunk.x, cur_pos.x / scale)
                && inside(chunk.y, cur_pos.y / scale);
    }

    @Override
    public void update(double time) {
        super.update(time);

        boolean in_bounds = in_bounds();
        if(!lost && !in_bounds){
            System.out.println("Object " + hashCode() + " in chunk " + chunk.x + ":" + chunk.y + " lost");
            System.out.println(point_time);
            lost = true;
//            GdxCollider.instance.alert(this);
        }else{
            warn = false;
        }
        if(lost && in_bounds){
            System.out.println("Object " + hashCode() + " found!");
            lost = false;
        }
    }

    @Override
    public void render(Batch batch) {
//        int speed = 10;
//        int spdup = speed + 1;
//        col.r = (col.r * speed + 1) / spdup;
//        col.g = (col.g * speed) / spdup;
//        col.b = (col.b * speed) / spdup;
        if(lost){
            batch.setColor(Color.MAGENTA);
        }else
            batch.setColor(col);
        batch.draw(texture, cur_pos.x - radius, cur_pos.y - radius, 2 * radius, 2 * radius);
        batch.setColor(Color.WHITE);
//        if(col_with != null) {
//            float x1 = cur_pos.x, x2 = col_with.cur_pos.x;
//            float y1 = cur_pos.y, y2 = col_with.cur_pos.y;
//            TextureGenerator.drawLine(batch, pixel, x1, y1, (x2 + x1) / 2, (y2 + y1) / 2);
//        }
    }
}
