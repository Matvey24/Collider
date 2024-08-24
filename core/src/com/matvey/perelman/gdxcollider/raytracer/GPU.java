package com.matvey.perelman.gdxcollider.raytracer;

import com.badlogic.gdx.Gdx;
import com.nativelibs4java.opencl.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

public class GPU implements AutoCloseable {
    public final CLQueue queue;
    public final CLContext context;
    public final CLKernel kernel;
    private final CLProgram prg;

    public GPU() {
        CLPlatform platform = JavaCL.listPlatforms()[1];
        context = platform.createContext(new HashMap<>(), platform.getBestDevice());
        String[] paths = {"math.c", "ImageBMP.c", "intersections.c", "figures.c", "main.c"};
        queue = context.createDefaultQueue();
        String[] srcs = new String[paths.length];
        for (int i = 0; i < paths.length; ++i) {
            srcs[i] = new String(Gdx.files.internal("cl_code/" + paths[i]).readBytes());
        }
        prg = context.createProgram(srcs);
        prg.addBuildOption("-cl-mad-enable");
        long time = System.currentTimeMillis();
        System.out.print("Building program");
        prg.build();
        System.out.printf(": %.1f second.\n", (System.currentTimeMillis() - time) / 1000f);
        kernel = prg.createKernel("worker_main");
    }

    public void close() {
        queue.flush();
        queue.finish();
        kernel.release();
        prg.release();
        queue.release();
        context.release();
    }
}
