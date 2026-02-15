package com.matvey.perelman.gdxcollider;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.matvey.perelman.gdxcollider.collider.twodim.Dynamic2D;
import com.matvey.perelman.gdxcollider.collider.twodim.objects.Sphere2D;
import com.matvey.perelman.gdxcollider.raytracer.GPU;
import com.matvey.perelman.gdxcollider.raytracer.materials.ColorMaterial;
import com.matvey.perelman.gdxcollider.raytracer.objects.Sphere;

public class GdxCollider extends ApplicationAdapter {
    public static GdxCollider instance;


    SpriteBatch world_batch, ui_batch;
    Texture sph, pixel;
    WorldCreator creator;
    public boolean RTX_ON = false;
    public Runnable task;
    public GPU gpu;
    public boolean made;
    //public Camera camera;
    public BitmapFont font;
    AverageValue.AverageLong updates, nanos, allocs;
    AverageValue.AverageFloat actual_speed, speed_variation;
    private float speed = 1 / 2f, last_scheduler_time = 0;
    public boolean paused, save_paused;
    public OrthographicCamera cam;
    public float scale = 1;
    public Vector2 movement = new Vector2();
    public Vector2 last_used_position = new Vector2();
    public float roll;
    private int window_h = 1280, window_w = 720;
    public com.matvey.perelman.gdxcollider.raytracer.Camera camera;

    public GdxCollider(){
        instance = this;
    }

    @Override
    public void create() {
        Gdx.graphics.setVSync(true);
        updates = new AverageValue.AverageLong();
        nanos = new AverageValue.AverageLong();
        allocs = new AverageValue.AverageLong();
        actual_speed = new AverageValue.AverageFloat();
        speed_variation = new AverageValue.AverageFloat();
        world_batch = new SpriteBatch();
        ui_batch = new SpriteBatch();
        int size = 40;
        sph = TextureGenerator.genSphere(size * 4, Color.WHITE);
        pixel = TextureGenerator.genPixel(Color.WHITE);
        creator = new WorldCreator(sph, pixel);

        if (RTX_ON) {
            gpu = new GPU();
        }
        camera = new com.matvey.perelman.gdxcollider.raytracer.Camera(gpu, creator.scene, 1920, 1080);
        camera.pos.set(1920 / 2f, 1080 / 2f, -2500);
        camera.render_time = false;
        roll = 0.001f;
        cam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(1920 / 2f, 1080 / 2f, 0);
        font = new BitmapFont();
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                creator.on_pre_cycle();
                switch (keycode) {
                    case Input.Keys.W:
                        movement.y += 1;
                        break;
                    case Input.Keys.S:
                        movement.y -= 1;
                        break;
                    case Input.Keys.A:
                        movement.x -= 1;
                        break;
                    case Input.Keys.D:
                        movement.x += 1;
                        break;
                    case Input.Keys.E:
                        speed *= 2;
                        break;
                    case Input.Keys.Q:
                        speed /= 2;
                        break;
                    case Input.Keys.ESCAPE:
                        Gdx.app.exit();
                        break;
                    case Input.Keys.SPACE:
                        paused = !paused;
                        break;
                    case Input.Keys.R:
                        creator.reversePlayback();
                        break;
                    case Input.Keys.F11:
                        if (Gdx.graphics.isFullscreen()) {
                            Gdx.graphics.setWindowedMode(window_w, window_h);
                        } else {
                            window_w = Gdx.graphics.getWidth();
                            window_h = Gdx.graphics.getHeight();
                            Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
                        }
                    default:
                        return false;
                }
                return true;
            }

            @Override
            public boolean keyUp(int keycode) {
                switch (keycode) {
                    case Input.Keys.W:
                        movement.y -= 1;
                        break;
                    case Input.Keys.S:
                        movement.y += 1;
                        break;
                    case Input.Keys.A:
                        movement.x += 1;
                        break;
                    case Input.Keys.D:
                        movement.x -= 1;
                        break;
                    default:
                        return false;
                }
                return true;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                creator.on_pre_cycle();
                Vector3 pos = new Vector3(screenX, screenY, 0);
                cam.unproject(pos);

                if (button == Input.Buttons.RIGHT) {
                    creator.inject(pos);
                    return true;
                }
                if (button == Input.Buttons.LEFT) {
                    creator.onClick(pos);
                    follow(creator.observed);
                    return true;
                }
                return false;
            }

            @Override
            public boolean scrolled(float amountX, float amountY) {
                roll = amountY / 5;
                return true;
            }
        });

//        benchmark();
    }
    public void benchmark(){
        paused = true;
        creator.update(20);

        long nanos = System.nanoTime();
        creator.executor.begin();
        creator.executor.wait_for_finish();

        nanos = System.nanoTime() - nanos;
        String sec = String.format("%.2f", ((double)nanos) / 1_000_000_000);
        System.out.println("Time taken (sec):" + sec);
        System.out.println("InGame time: " + creator.scheduler.time);
        System.out.println("Events: " + creator.scheduler.events);
        System.out.println("Queue throughput: " + creator.scheduler.allocations);
    }
    public void follow(Sphere2D sp){
        creator.observed = sp;
        if(sp != null)
            last_used_position.set(creator.observed.cur_pos);
    }
    public void alert(Sphere2D sp){
        paused = true;
        System.out.println(creator.scheduler.stop);
        creator.scheduler.stop = true;
        follow(sp);
        cam.position.set(sp.cur_pos.x, sp.cur_pos.y, 0);
    }
    @Override
    public void resize(int width, int height) {
        camera_update(0);
        ui_batch.dispose();
        ui_batch = new SpriteBatch();
    }

    @Override
    public void pause() {
        save_paused = paused;
        paused = true;
    }

    @Override
    public void resume() {
        paused = save_paused;
    }

    private void camera_update(float dt) {
        scale *= (float) Math.exp(roll);
        roll = 0;
        cam.zoom = scale;
        cam.viewportWidth = Gdx.graphics.getWidth();
        cam.viewportHeight = Gdx.graphics.getHeight();
        creator.screen_scale = scale;
        float mv_scale = dt * Gdx.graphics.getHeight() * scale;
        cam.position.add(movement.x * mv_scale, movement.y * mv_scale, 0);
        if (creator.observed != null) {
            last_used_position.sub(creator.observed.cur_pos);
            last_used_position.scl(-1);
            cam.position.add(last_used_position.x, last_used_position.y, 0);
            last_used_position.set(creator.observed.cur_pos);
        }
        if (RTX_ON) {
            for (int i = 0; i < creator.world.objects.size(); ++i) {
                creator.scene.objects.get(i).pos.set(creator.world.objects.get(i).cur_pos, 0);
                ((ColorMaterial) ((Sphere) creator.scene.objects.get(i)).m).diffuse.set(((Sphere2D) creator.world.objects.get(i)).col);
            }
        }
        cam.update();
        world_batch.setProjectionMatrix(cam.combined);
    }

    @Override
    public void render() {
        try {
            update(Gdx.graphics.getDeltaTime());
            ScreenUtils.clear(0, 0, 0, 1);
            world_batch.begin();
            if (RTX_ON) {
                camera.render();
                world_batch.draw(camera.getResult(), 0, 0);
            } else {
                creator.render(world_batch);
                creator.emitter.render(world_batch);
            }

            world_batch.end();
            ui_batch.begin();
            font.draw(ui_batch, "col: " + updates.avg + ", puts: " + allocs.avg + ", nanos: " + nanos.avg, 0, Gdx.graphics.getHeight());
            font.draw(ui_batch, "target speed: " + speed + ", actual speed: " + String.format("%.4f", actual_speed.avg), 0, Gdx.graphics.getHeight() - 20);
            font.draw(ui_batch, "speed variation: " + String.format("%.4f", Math.sqrt(speed_variation.avg)), 0, Gdx.graphics.getHeight() - 40);
            font.draw(ui_batch, "time: " + String.format("%.4f", creator.scheduler.time), 0, Gdx.graphics.getHeight() - 60);
            if (creator.observed != null) {
                Sphere2D sp = creator.observed;
                font.draw(ui_batch, "vel: " + sp.vel + ", pos: " + sp.pos, 0, Gdx.graphics.getHeight() - 80);
                font.draw(ui_batch, "point_time: " + String.format("%.4f", sp.point_time) + ", col_time: " + (float)(sp.col_time - sp.point_time), 0, Gdx.graphics.getHeight() - 100);
            }

            ui_batch.end();
            if (!paused)
                creator.on_post_cycle();
        } catch (Exception e) {
            e.printStackTrace();
            Gdx.app.exit();
        }
    }

    public void update(float dt) {
//		if(gpu != null && !made){
//			camera = new Camera(gpu, creator.scene, 1280, 720);
//			camera.render_time = false;
//			camera.pos.set(640, 360, -1300);
//			task.run();
//			made = true;
//		}
        creator.on_pre_cycle();
        if (!paused) {
            creator.update(speed * dt);
            int ev = creator.scheduler.events;
            updates.add((long) ev);
            int al = creator.scheduler.allocations;
            allocs.add((long) al);
            if (al != 0) {
                nanos.add(creator.world.scheduler.delay / al);
            }
            if (dt != 0) {
                float delta_time = (float) creator.scheduler.time - last_scheduler_time;
                last_scheduler_time = (float) creator.scheduler.time;
                float current_speed = delta_time / dt;
                actual_speed.add(current_speed);
                speed_variation.add((actual_speed.avg - current_speed) * (actual_speed.avg - current_speed));
            }
        }
        camera_update(dt);
    }

    @Override
    public void dispose() {
        world_batch.dispose();
        sph.dispose();
        pixel.dispose();
        creator.dispose();
        ui_batch.dispose();
    }
}
