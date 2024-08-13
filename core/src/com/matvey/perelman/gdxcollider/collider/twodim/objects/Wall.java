package com.matvey.perelman.gdxcollider.collider.twodim.objects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.matvey.perelman.gdxcollider.TextureGenerator;
import com.matvey.perelman.gdxcollider.collider.twodim.Dynamic2D;

import static com.matvey.perelman.gdxcollider.WorldCreator.*;

public class Wall extends Dynamic2D {
    //pos - point of wall
    //vel - normal of wall
    private final Texture pixel;
    public int dir;
    public boolean trigger;
    public Wall(Texture pixel){
        this.pixel = pixel;
    }
    @Override
    public void render(Batch b) {
        TextureGenerator.drawLine(b, pixel, pos.x + vel.y * 1000, pos.y - vel.x * 1000, pos.x - vel.y * 1000, pos.y + vel.x * 1000);
//        if(col_with != null) {
//            float x1 = cur_pos.x, x2 = col_with.cur_pos.x;
//            float y1 = cur_pos.y, y2 = col_with.cur_pos.y;
//            b.setColor(Color.BLUE);
//            TextureGenerator.drawLine(b, pixel, x1, y1, (x2 + x1) / 2, (y2 + y1) / 2);
//            b.setColor(Color.WHITE);
//        }
    }
    public void setChunk(Dynamic2D obj){
        chunk = obj.chunk;
        switch(dir) {
            case 0:
                pos.set((chunk.x + 0.5f) * scale, (chunk.y + 1) * scale);
                trigger = chunk.y + 1 < sy;
                if(trigger)
                    chunk = chunk.near.get(7);
                break;
            case 1:
                pos.set((chunk.x + 1) * scale, (chunk.y + 0.5f) * scale);
                trigger = chunk.x + 1 < sx;
                if(trigger)
                    chunk = chunk.near.get(5);
                break;
            case 2:
                pos.set(chunk.x * scale, (chunk.y + 0.5f) * scale);
                trigger = chunk.x > 0;
                if(trigger)
                    chunk = chunk.near.get(3);
                break;
            case 3:
                pos.set((chunk.x + 0.5f) * scale, chunk.y * scale);
                trigger = chunk.y > 0;
                if(trigger)
                    chunk = chunk.near.get(1);
                break;
        }
    }
}
