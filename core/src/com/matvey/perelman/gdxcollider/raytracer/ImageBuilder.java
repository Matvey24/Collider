package com.matvey.perelman.gdxcollider.raytracer;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Hashtable;

public class ImageBuilder {
    private ByteBuffer bytes;
    public Pixmap pixmap;
    public Texture img;
    private int width, height, line_len, full_len;
    public ImageBuilder(int width, int height){
        reset_bounds(width, height);
    }
    public void reset_bounds(int width, int height){
        this.width = width;
        this.height = height;
        line_len = width * 3;
        line_len = line_len + (-line_len & 3);
        full_len = 54 + line_len * height;
        bytes = ByteBuffer.allocate(full_len);
        bytes.order(ByteOrder.LITTLE_ENDIAN);

        bytes.put((byte)'B');
        bytes.put((byte)'M');
        bytes.putInt(full_len);//size
        bytes.putInt(0);//reserved
        bytes.putInt(54);//offset_bits

        bytes.putInt(40);//header_size
        bytes.putInt(width);//width
        bytes.putInt(height);//height
        bytes.putShort((short)1);//planes
        bytes.putShort((short)24);//bitcount
        bytes.putInt(0);//compression
        bytes.putInt(height * line_len);//size_img
        bytes.putInt(3780);//xmp
        bytes.putInt(3780);//ypm
        bytes.putInt(0);//colorUsed
        bytes.putInt(0);//colorsImportant

        pixmap = new Pixmap(width, height, Pixmap.Format.RGB888);
        img = new Texture(pixmap);
    }
    public void set(byte[] img_data){
        bytes.position(54);
        bytes.put(img_data);
        bytes.position(54);
        ByteBuffer buf = pixmap.getPixels();
        int pos = buf.position();
        buf.put(bytes);
        buf.position(pos);
        img.draw(pixmap, 0, 0);
    }
    public void save(File file){

    }
    public int getWidth() {
        return width;
    }
    public int getImageLen(){
        return line_len * height;
    }
    public int getHeight() {
        return height;
    }

    public int getLineLen() {
        return line_len;
    }
    public int fullLen(){
        return full_len;
    }
}
