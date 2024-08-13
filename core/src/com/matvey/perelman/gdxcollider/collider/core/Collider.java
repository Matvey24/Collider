package com.matvey.perelman.gdxcollider.collider.core;

public interface Collider<T extends Dynamic<T>> {
    double calc_time(T a, T b);
    double calc_time_static(T a, T stat);

    void collide(T a, T b, double time);
    void collide_static(T a, T stat, double time);
}
