package com.matvey.perelman.gdxcollider.raytracer;

import com.badlogic.gdx.graphics.Texture;
import com.matvey.perelman.gdxcollider.raytracer.objects.Dot;
import com.nativelibs4java.opencl.*;
import org.bridj.Pointer;
import org.bridj.TypedPointer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Camera extends Dot {
    public final GPU gpu;
    public String folder = "images";

    public final Scene sc;
    private final ByteBuffer param;
    private ImageBuilder builder;
    public boolean render_time = true;


    private CLEvent event;
    private CLBuffer<Byte> param_buf, scene_buf, img_buf;
    private long time;

    public Camera(GPU gpu, Scene sc, int width, int height){
        this.gpu = gpu;
        this.sc = sc;
        param = ByteBuffer.allocate(64);
        param.order(ByteOrder.LITTLE_ENDIAN);
        resetSize(width, height);
    }
    public void resetSize(int width, int height){
        builder = new ImageBuilder(width, height);
    }
    public void startRender(){
        param.clear();
        Scene.putVector(param, pos);
        param.putInt(0);
        param.putInt(builder.getWidth());
        param.putInt(builder.getHeight());
        Scene.putMatrix(param, rot);
        param.flip();
        param_buf = gpu.context.createBuffer(CLMem.Usage.Input,
                TypedPointer.pointerToBytes(param));
        scene_buf = gpu.context.createBuffer(CLMem.Usage.Input,
                TypedPointer.pointerToBytes(sc.update()));
        img_buf = gpu.context.createBuffer(CLMem.Usage.InputOutput,
                TypedPointer.allocateBytes(builder.getImageLen()));
        gpu.kernel.setArgs(param_buf, scene_buf, LocalSize.ofByteArray(scene_buf.getByteCount()), img_buf, (long)0);
        if(render_time) {
            System.out.print("Rendered");
            time = System.currentTimeMillis();
        }
        event = gpu.kernel.enqueueNDRange(gpu.queue, new int[]{builder.getWidth(), builder.getHeight()});
    }
    public void finishRender(){
        if(event == null)
            throw new RuntimeException("render finished without starting");
        Pointer<Byte> img_pointer = img_buf.read(gpu.queue, event);
        byte[] bytes = img_pointer.getBytes();
        builder.set(bytes);
        if(render_time)
            System.out.printf(" in %.3f sec\n", (System.currentTimeMillis() - time) / 1000f);
        param_buf.release();
        scene_buf.release();
        img_buf.release();
        event = null;

        System.gc();
    }
    public void render(){
        startRender();
        finishRender();
    }
    public Texture getResult(){
        return builder.img;
    }
    public void save(String filename){
        builder.save(new File(folder, filename));
    }
}
