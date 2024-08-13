package com.matvey.perelman.gdxcollider;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.matvey.perelman.gdxcollider.collider.core.Chunk;
import com.matvey.perelman.gdxcollider.collider.core.ColliderWorld;
import com.matvey.perelman.gdxcollider.collider.twodim.Collider2D;
import com.matvey.perelman.gdxcollider.collider.twodim.Dynamic2D;
import com.matvey.perelman.gdxcollider.collider.twodim.objects.Sphere2D;
import com.matvey.perelman.gdxcollider.collider.twodim.objects.Wall;
import com.matvey.perelman.gdxcollider.particle.ParticleEmitter;
import com.matvey.perelman.gdxcollider.raytracer.Scene;
import com.matvey.perelman.gdxcollider.raytracer.materials.ColorMaterial;
import com.matvey.perelman.gdxcollider.raytracer.materials.Material;
import com.matvey.perelman.gdxcollider.raytracer.objects.Box;
import com.matvey.perelman.gdxcollider.raytracer.objects.Sphere;

import java.util.ArrayList;

public class WorldCreator {
    public ColliderWorld<Dynamic2D> world;
    public Scene scene;
    public ArrayList<Sphere> view_spheres;
    public ArrayList<Sphere2D> spheres;
    public ParticleEmitter emitter;
    private ArrayList<ArrayList<Chunk<Dynamic2D>>> field;
    private Texture pixel;
    public static final int sx, sy, chunk_scale = 8, sphere_scale = 8;
    public static final float scale;
    public static final float size, offsetX, offsetY;
    public float screen_scale;
    static{

        sx = 16 * chunk_scale;
        sy = 9 * chunk_scale;
        float resolution_factor = 1.5f;

        scale = resolution_factor * 80f / chunk_scale;


        size = resolution_factor * 10f /  sphere_scale;
        offsetX = resolution_factor * 30f / sphere_scale;
        offsetY = resolution_factor * 100f / sphere_scale;
    }
    public Sphere2D preferred;

    private Sphere2D find_object(Vector3 coords){
        Vector2 coord = new Vector2(coords.x, coords.y);
        int cx = (int)(coords.x / scale);
        int cy = (int)(coords.y / scale);
        if(cx < 0 || cy < 0 || cx >= sx || cy >= sy)
            return null;
        Chunk<Dynamic2D> chunk = field.get(cx).get(cy);
        Sphere2D sph = null;
        for(Chunk<Dynamic2D> c : chunk.near){
            for(Dynamic2D dyn : c.objects){
                if(!(dyn instanceof Sphere2D))
                    continue;
                Sphere2D sp = (Sphere2D)dyn;
                if(coord.dst(sp.cur_pos) < sp.radius){
                    sph = sp;
                    break;
                }
            }
            if(sph != null)
                break;
        }
        return sph;
    }

    public void inject(Vector3 coords){
        Sphere2D obj = find_object(coords);
        if(obj == null)
            return;
        if(!obj.sup_er){
            obj.sup_er = true;
            obj.col.set(Color.PURPLE);
        }else if(obj.col.equals(Color.PURPLE)){
            obj.sup_er = false;
            obj.col.set(Color.WHITE);
        }
    }

    public void onClick(Vector3 coords){
        preferred = find_object(coords);
    }


    public WorldCreator(Texture sph, Texture pixel){
        createWorld(sph, pixel);
        buildScene();
        this.pixel = pixel;
    }
    public void addObject(Dynamic2D obj){
        if(obj instanceof Sphere2D)
            spheres.add((Sphere2D)obj);
        int cx = (int)(obj.pos.x / scale);
        int cy = (int)(obj.pos.y / scale);
        obj.chunk = field.get(cx).get(cy);
        world.addObject(obj);
    }
    private void createWorld(Texture sph, Texture pixel){
        spheres = new ArrayList<>();
        emitter = new ParticleEmitter();
        Collider2D collider2D = new Collider2D(emitter);
        field = new ArrayList<>();

        for(int x = 0; x < sx; ++x){
            field.add(new ArrayList<>());
            for(int y = 0; y < sy; ++y)
                field.get(x).add(new Chunk<>(collider2D));
        }

        for(int x = 0; x < sx; ++x){
            for(int y = 0; y < sy; ++y){
                Chunk<Dynamic2D> c = field.get(x).get(y);
                for(int n = 0; n < 9; ++n){
                    int dx = (n % 3) - 1;
                    int dy = (n / 3) - 1;
                    if(x + dx < sx && x + dx >= 0 && y + dy < sy && y + dy >= 0)
                        c.near.add(field.get(x + dx).get(y + dy));
                }
                c.x = x;
                c.y = y;
                Wall wu = new Wall(pixel);
                Wall wr = new Wall(pixel);
                Wall wl = new Wall(pixel);
                Wall wd = new Wall(pixel);

                wu.pos.set((x + 0.5f) * scale, (y + 1) * scale);
                wr.pos.set((x + 1) * scale, (y + 0.5f) * scale);
                wl.pos.set(x * scale, (y + 0.5f) * scale);
                wd.pos.set((x + 0.5f) * scale, y * scale);

                wu.vel.set(0, -1);
                wr.vel.set(-1, 0);
                wl.vel.set(1, 0);
                wd.vel.set(0, 1);

                wu.chunk = c;
                wr.chunk = c;
                wl.chunk = c;
                wd.chunk = c;

                if(y + 1 < sy) {
                    wu.trigger = true;
                    wu.chunk = field.get(x).get(y + 1);
                }
                if(x + 1 < sx) {
                    wr.trigger = true;
                    wr.chunk = field.get(x + 1).get(y);
                }
                if(x > 0) {
                    wl.trigger = true;
                    wl.chunk = field.get(x - 1).get(y);
                }
                if(y > 0){
                    wd.trigger = true;
                    wd.chunk = field.get(x).get(y - 1);
                }
                c.stationary.add(wu);
                c.stationary.add(wr);
                c.stationary.add(wl);
                c.stationary.add(wd);
            }
        }


        world = new ColliderWorld<>(collider2D);

        int speed = 1;
        for(int j = 1; j < 5 * sphere_scale; ++j) {
            for (int i = 0; i < 40 * sphere_scale; ++i) {
                float mass = 0.4f * j / sphere_scale;
                Sphere2D sp = new Sphere2D(sph, pixel, mass, size * (float)Math.sqrt(mass));
                sp.vel.set(0, speed);
                sp.pos.set(offsetX * i + 20, 100 + offsetY * j);
                addObject(sp);
            }
        }
        Sphere2D sp = new Sphere2D(sph, pixel, 5f, size * 2f);
        sp.vel.set(speed, 0);
        sp.pos.set(1180, 500);
        addObject(sp);
        sp.col.set(Color.BLUE);
        sp.sup_er = true;
        Sphere2D sp2 = new Sphere2D(sph, pixel, 5, size * 2f);
        sp2.vel.set(speed, 0);
        sp2.pos.set(1180, 100);
        addObject(sp2);
        sp2.sup_er = true;
        sp2.col.set(Color.RED);
        Sphere2D sp3 = new Sphere2D(sph, pixel, 5, size * 2);
        sp3.vel.set(speed, 0);
        sp3.pos.set(1180, 300);
        addObject(sp3);
        sp3.col.set(Color.GREEN);
        sp3.sup_er = true;
    }
    public void buildScene(){
        scene = new Scene();
        view_spheres = new ArrayList<>();
        for(Dynamic2D d: world.objects){
            if(d instanceof Sphere2D) {
                Sphere2D sp = (Sphere2D)d;
                Sphere vsp = new Sphere(new ColorMaterial(sp.col), sp.radius);
                view_spheres.add(vsp);
                scene.add(vsp);
            }
        }

        Material box_material = new ColorMaterial(Color.YELLOW);
        int depth = 20;

        Box w1 = new Box(box_material, 640, depth, depth);
        w1.pos.set(640, 720 + depth, 0);
        Box w2 = new Box(box_material, depth, 360 + 2 * depth, depth);
        w2.pos.set(1280 + depth, 360, 0);
        Box w3 = new Box(box_material, depth, 360 + 2 * depth, depth);
        w3.pos.set(-depth, 360, 0);
        Box w4 = new Box(box_material, 640, depth, 640);
        w4.pos.set(640, -depth, 0);

        scene.add(w1);
        scene.add(w2);
        scene.add(w3);
        scene.add(w4);
    }
    public void render(Batch batch){
        batch.setColor(Color.DARK_GRAY);
        for(int x = 0; x <= sx; ++x){
            batch.draw(pixel, x * scale, 0, 1, scale * sy);
        }
        for(int y = 0; y <= sy; ++y){
            batch.draw(pixel, 0, y * scale, scale * sx, 1);
        }
        batch.setColor(Color.WHITE);
        if(preferred != null) {
            batch.setColor(Color.CYAN);
            renderSphere(batch, preferred);
            if(preferred.col_with != null){
                batch.setColor(Color.ORANGE);
                if(preferred.col_with instanceof Sphere2D){
                    renderSphere(batch, (Sphere2D)preferred.col_with);
                }else if(preferred.col_with instanceof Wall){
                    Wall w = (Wall)preferred.col_with;
                    TextureGenerator.drawLine(
                            batch, pixel,
                            w.pos.x + w.vel.y * scale / 2,
                            w.pos.y - w.vel.x * scale / 2,
                            w.pos.x - w.vel.y * scale / 2,
                            w.pos.y + w.vel.x * scale / 2);
                }
            }

            batch.setColor(Color.WHITE);
        }
        for (Dynamic2D d : world.objects)
            d.render(batch);
    }
    private void renderSphere(Batch batch, Sphere2D sp){
        float delta = 0.1f + sp.radius * 0.25f + screen_scale * 2;
        batch.draw(sp.texture,
                sp.cur_pos.x - (sp.radius + delta),
                sp.cur_pos.y - (sp.radius + delta),
                2f * (sp.radius + delta),
                2f * (sp.radius + delta));
    }
}
