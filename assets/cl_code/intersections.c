#define DIFF (float)0.1
float getDist(float t1, float t2){
	if (t1 < 0 && t2 < 0)
        return NAN;
    if (t1 > t2) {
        float t = t1;
        t1 = t2;
        t2 = t;
    }
    if (t1 < 0)
        return t2;
    return t1;
}
float calcReflect(float sclDirNorm, float ref){
    ref = 1 - ref;
    float cosfi = f_abs(sclDirNorm);
    float sinfi = sqrt(1 - cosfi * cosfi);
    float sinpsi = ref * sinfi;
    float cospsi = sqrt(1 - sinpsi * sinpsi);
    float rpe = (cosfi - ref*cospsi)/(cosfi + ref*cospsi);
    float rpa = (ref*cosfi - cospsi)/(ref*cosfi +cospsi);
    return sqrt(rpe * rpe + rpa * rpa) / 2;
}
float nasphInter(float rad2, float3 pos, float3 dir){
	float k1 = f3_len2(dir);
	float k2 = f3_scl(pos, dir);
    float k3 = f3_len2(pos) - rad2;
    float disk = k2 * k2 - k1 * k3;
    if (disk < 0)
        return NAN;
    if (disk == 0) {
        float t = k2 / k1;
        if (t < 0)
            return NAN;
        return t;
    }
    else {
        disk = sqrt(disk);
        return getDist(
            (k2 + disk) / k1,
            (k2 - disk) / k1);
    }
}
float naRectInter(float3 bd, float3 p, float3 d){
    if ((d.x == 0 && f_abs(p.x) > bd.x)
        || (d.y == 0 && f_abs(p.y) > bd.y)
        || (d.z == 0 && f_abs(p.z) > bd.z))
        return NAN;
    float s1, s2, s3, e1, e2, e3;
    float t1 = (p.x + bd.x) / d.x;
    float t2 = (p.x - bd.x) / d.x;
    s1 = min(t1, t2);
    e1 = max(t1, t2);
    t1 = (p.y + bd.y) / d.y;
    t2 = (p.y - bd.y) / d.y;
    s2 = min(t1, t2);
    e2 = max(t1, t2);
    t1 = (p.z + bd.z) / d.z;
    t2 = (p.z - bd.z) / d.z;
    s3 = min(t1, t2);
    e3 = max(t1, t2);
    t1 = max(max(s1, s2), s3);
    t2 = min(min(e1, e2), e3);
    if (t1 > t2 || t2 < 0)
        return NAN;
    return t1;
}
float naMandelBulbDEHalf(float3 posf, float s){
    float3 rad = posf;
    float dr = 1;
    float r = 0;
    for (int i = 0; i < 20; ++i) {
        r = fast_length(rad);
        if (r > 2)
            break;
        float p = r;
        // float p = r * r;
        // p = (p * p) * (p * r);
        dr = p * 2 * dr + 1;
        p *= r;

        float2 angs = (float2)(acos(rad.z / r), atan2(rad.y, rad.x));
        angs *= 2;
        // angs.x += s;
        // angs.y *= 2;
        float2 coss;
        float2 sins = sincos(angs, &coss);

        rad = (float3)(sins.x * coss.y, sins.x * sins.y, coss.x);
        rad *= p;

        rad += posf;
    }
    return 0.5 * log(r) * r / dr;
}
float2 naFractalInter(float dist, float3 pos, float3 dir, float s){
    float diff = DIFF * 10;
    float d = DIFF * 50;
    float max_d = dist;
    if (f3_len2(pos) >= 4) {
        d = dist;
        max_d = 4;
    }
    pos += d * dir;
    float total_dist = 0;
    int steps;
    for(steps = 0; steps < 500; ++steps){
        float3 p = pos + total_dist * dir;
        dist = naMandelBulbDEHalf(p, s);
        if (dist < diff)
            return (float2)(total_dist + d, steps);
        total_dist += dist;
        if (total_dist > max_d)
            return (float2)(NAN, 0);
    }
    return (float2)(total_dist + d, steps);
}
