package com.matvey.perelman.gdxcollider.collider.twodim.objects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.matvey.perelman.gdxcollider.TextureGenerator;
import com.matvey.perelman.gdxcollider.collider.twodim.Dynamic2D;

import static com.matvey.perelman.gdxcollider.WorldCreator.*;

public class Wall extends Dynamic2D {
    public static final float SQRT2 = (float)Math.sqrt(2) * 0.5f;
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
        float first_x = (chunk.x + (1 - vel.x + vel.y) * 0.5f) * scale - vel.y * 0.5f,
                first_y = (chunk.y + (1 - vel.y - vel.x) * 0.5f) * scale + vel.x * 0.5f,
                second_x = (chunk.x + (1 - vel.x - vel.y) * 0.5f) * scale + vel.y * 0.5f,
                second_y = (chunk.y + (1 - vel.y + vel.x) * 0.5f) * scale - vel.x * 0.5f;

        TextureGenerator.drawLine(b, pixel, first_x, first_y, second_x, second_y);
        TextureGenerator.draw(b, pixel, first_x, first_y, SQRT2, SQRT2, 45);
        TextureGenerator.draw(b, pixel, second_x, second_y, SQRT2, SQRT2, 45);
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
