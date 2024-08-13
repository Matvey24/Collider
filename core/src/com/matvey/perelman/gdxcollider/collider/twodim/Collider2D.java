package com.matvey.perelman.gdxcollider.collider.twodim;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.matvey.perelman.gdxcollider.collider.core.Collider;
import com.matvey.perelman.gdxcollider.collider.twodim.objects.Sphere2D;
import com.matvey.perelman.gdxcollider.collider.twodim.objects.Wall;
import com.matvey.perelman.gdxcollider.particle.ParticleEmitter;

public class Collider2D implements Collider<Dynamic2D> {
    private final Vector2 tmp, tmp2, tmp3, tmp4;
    private final ParticleEmitter emitter;

    public Collider2D(ParticleEmitter emitter) {
        this.emitter = emitter;
        tmp = new Vector2();
        tmp2 = new Vector2();
        tmp3 = new Vector2();
        tmp4 = new Vector2();
    }

    public void updatePos(Dynamic2D a, double time) {
        a.update(time);
        a.pos.set(a.cur_pos);
        a.vel.mulAdd(a.acc, (float)(time - a.point_time));
        a.point_time = time;
    }
    @Override
    public void collide(Dynamic2D a, Dynamic2D b, double time) {
        Sphere2D as = (Sphere2D) a;
        Sphere2D bs = (Sphere2D) b;
        updatePos(a, time);
        updatePos(b, time);
        if(as.pos.dst2(bs.pos) > 1.1 * (as.radius + bs.radius) * (as.radius + bs.radius)){
            System.out.println("Hello world");
        }
        float mass_a = as.mass;
        float mass_b = bs.mass;
        Vector2 c = tmp, va = tmp2, vb = tmp3, dir = tmp4;
        c.setZero().mulAdd(a.vel, mass_a).mulAdd(b.vel, mass_b).scl(1 / (mass_a + mass_b));
        va.set(a.vel).sub(c);
        vb.set(b.vel).sub(c);
        dir.set(a.pos).sub(b.pos);
        dir.nor();
        va.mulAdd(dir, -2 * dir.dot(va));
        vb.mulAdd(dir, -2 * dir.dot(vb));

        a.vel.set(c).add(va);
        b.vel.set(c).add(vb);
        if (as.sup_er != bs.sup_er) {
            Color col = as.sup_er ? as.col : bs.col;
            as.col.set(col);
            bs.col.set(col);
        } else if (!as.sup_er) {
            as.col.mul(0.5f).add(bs.col.mul(0.5f));
            bs.col.set(as.col);
        }
//        tmp.set(as.pos).add(bs.pos).scl(1 / 2f);
//        emitter.addParticle(tmp.x, tmp.y, 1f);
    }

    @Override
    public void collide_static(Dynamic2D a, Dynamic2D stat, double time) {
        Wall w = (Wall) stat;
        w.setChunk(a);
        Sphere2D sp = (Sphere2D) a;
        updatePos(sp, time);
        if(w.trigger) {
            return;
        }


        sp.vel.mulAdd(w.vel, -2 * w.vel.dot(sp.vel));

//        tmp.set(sp.pos);
//        emitter.addParticle(tmp.x, tmp.y, 1f);
    }

    @Override
    public double calc_time(Dynamic2D a, Dynamic2D b) {
        float sum_rad = ((Sphere2D) a).radius + ((Sphere2D) b).radius;

        double time_delta = Math.max(a.point_time, b.point_time);
        float t01 = (float)(time_delta - a.point_time);
        float t02 = (float)(time_delta - b.point_time);
        Vector2 dp = tmp3, dv = tmp2, v1 = tmp, v2 = tmp4;
        dp.set(a.pos).mulAdd(a.vel, t01).mulAdd(a.acc, t01 * t01 / 2);
        tmp.set(b.pos).mulAdd(b.vel, t02).mulAdd(b.acc, t02 * t02 / 2);
        dp.sub(tmp);

        v1.set(a.vel).mulAdd(a.acc, t01);
        v2.set(b.vel).mulAdd(b.acc, t02);

        dv.set(v1).sub(v2);

        float ac = dv.len2();
        float bc = 2 * dp.dot(dv);
        float cc = dp.len2() - sum_rad * sum_rad;

        double t = minimum(ac, bc, cc);
        t += time_delta;
        return t;
    }

    @Override
    public double calc_time_static(Dynamic2D a, Dynamic2D stat) {
        Sphere2D sp = (Sphere2D) a;
        Wall w = (Wall) stat;
        w.setChunk(a);
        Vector2 pos = tmp;
        pos.set(sp.pos).sub(w.pos);

        float cc = pos.dot(w.vel);
        if(!w.trigger)
            cc -= sp.radius;
        float bc = sp.vel.dot(w.vel);
        float ac = sp.acc.dot(w.vel) / 2;
        return root(ac, bc, cc) + sp.point_time;
    }

    public float minimum(float a, float b, float c) {
        if (a == 0) {
            if (b * c >= 0)
                return Float.POSITIVE_INFINITY;
            return -c / b;
        }
        float D = b * b - 4 * a * c;
        if (D <= 0)
            return Float.POSITIVE_INFINITY;
        D = (float) Math.sqrt(D);
        float root = (-b - Math.signum(a) * D) / (2 * a);
        if (root <= 0)
            return Float.POSITIVE_INFINITY;
        return root;
    }

    public float root(float a, float b, float c) {
        if (a == 0) {
            if(b >= 0)
                return Float.POSITIVE_INFINITY;
            float root = -c / b;
            if(root > -0.001)
                return root;
            return Float.POSITIVE_INFINITY;
        }
        float D = b * b - 4 * a * c;
        if (D <= 0)
            return Float.POSITIVE_INFINITY;
        D = (float) Math.sqrt(D);

        float root1 = (-b + D) / (2 * a);
        float root2 = (-b - D) / (2 * a);
        float min = Math.min(root1, root2);
        float max = Math.max(root1, root2);

        if (a > 0) {
            if (min > -0.0001)
                return min;
        } else {
            if (max > -0.0001)
                return max;
        }

        return Float.POSITIVE_INFINITY;
    }
}
