package com.matvey.perelman.gdxcollider;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class TextureGenerator {
    public static Texture genSphere(int rad, Color c){
        Pixmap pixmap = new Pixmap(rad * 2, rad * 2, Pixmap.Format.RGBA8888);
        pixmap.setColor(c);
        pixmap.fillCircle(rad, rad, rad - 1);
        Texture tex = new Texture(pixmap);
        pixmap.dispose();
        return tex;
    }
    public static Texture genPixel(Color c){
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGB888);
        pixmap.setColor(c);
        pixmap.fill();
        Texture tex = new Texture(pixmap);
        pixmap.dispose();
        return tex;
    }
    public static void drawLine(Batch batch, Texture pixel, float x1, float y1, float x2, float y2){
        batch.draw(pixel, x1, y1,
                0.5f, 0.5f,
                Vector2.dst(x1, y1, x2, y2), 1,
                1, 1,
                MathUtils.radiansToDegrees * MathUtils.atan2(y2 - y1, x2 - x1),
                0, 0, 1, 1,
                false, false);
    }
}
