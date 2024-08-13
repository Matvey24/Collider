struct Matrix{
	float a1, a2, a3, b1, b2, b3, c1, c2, c3;
};
float f_abs(float a){
    if(a < 0)
        return -a;
    return a;
}
float2 f2_fromAng(float ang){
	return (float2)(cos(ang), sin(ang));
}
float f3_scl(float3 a, float3 b){
    return a.x * b.x + a.y * b.y + a.z * b.z;
}
float f3_len2(float3 vec){
    return vec.x * vec.x + vec.y * vec.y + vec.z * vec.z;
}

float3 f3_vec(float3 a, float3 b){
    return (float3)(
        a.y * b.z - a.z * b.y,
        a.z * b.x - a.x * b.z,
        a.x * b.y - a.y * b.x);
}
float3 f3_sclv(float3 a, float3 b){
    return (float3)(
        a.x * b.x,
        a.y * b.y,
        a.z * b.z
    );
}
float3 Matrix_transform(struct Matrix mat, float3 vec){
	float x, y, z;
    x = mat.a1 * vec.x + mat.a2 * vec.y + mat.a3 * vec.z;
    y = mat.b1 * vec.x + mat.b2 * vec.y + mat.b3 * vec.z;
    z = mat.c1 * vec.x + mat.c2 * vec.y + mat.c3 * vec.z;
    return (float3)(x, y, z);
}
float3 Matrix_transformBack(struct Matrix mat, float3 vec){
    float x, y, z;
    x = mat.a1 * vec.x + mat.b1 * vec.y + mat.c1 * vec.z;
    y = mat.a2 * vec.x + mat.b2 * vec.y + mat.c2 * vec.z;
    z = mat.a3 * vec.x + mat.b3 * vec.y + mat.c3 * vec.z;
    return (float3)(x, y, z);
}
struct Matrix Matrix_mult(struct Matrix a, struct Matrix b){
	struct Matrix c;
    c.a1 = a.a1 * b.a1 + a.a2 * b.b1 + a.a3 * b.c1;
    c.a2 = a.a1 * b.a2 + a.a2 * b.b2 + a.a3 * b.c2;
    c.a3 = a.a1 * b.a3 + a.a2 * b.b3 + a.a3 * b.c3;
    c.b1 = a.b1 * b.a1 + a.b2 * b.b1 + a.b3 * b.c1;
    c.b2 = a.b1 * b.a2 + a.b2 * b.b2 + a.b3 * b.c2;
    c.b3 = a.b1 * b.a3 + a.b2 * b.b3 + a.b3 * b.c3;
    c.c1 = a.c1 * b.a1 + a.c2 * b.b1 + a.c3 * b.c1;
    c.c2 = a.c1 * b.a2 + a.c2 * b.b2 + a.c3 * b.c2;
    c.c3 = a.c1 * b.a3 + a.c2 * b.b3 + a.c3 * b.c3;
    return c;
}
struct Matrix Matrix_setRotE(float3 at, float2 ang){
	struct Matrix c;
	c.a1 = (1 - ang.x) * at.x * at.x + ang.x;
    c.a2 = (1 - ang.x) * at.x * at.y - ang.y * at.z;
    c.a3 = (1 - ang.x) * at.x * at.z + ang.y * at.y;
    c.b1 = (1 - ang.x) * at.y * at.x + ang.y * at.z;
    c.b2 = (1 - ang.x) * at.y * at.y + ang.x;
    c.b3 = (1 - ang.x) * at.y * at.z - ang.y * at.x;
    c.c1 = (1 - ang.x) * at.z * at.x - ang.y * at.y;
    c.c2 = (1 - ang.x) * at.z * at.y + ang.y * at.x;
    c.c3 = (1 - ang.x) * at.z * at.z + ang.x;
    return c;
}

struct Matrix Matrix_setRotOf(float3 ang){
	float l = sqrt(f3_len2(ang));
    float l1 = 1 / l;
    ang.x *= l1;
    ang.y *= l1;
    ang.z *= l1;
    float2 an = f2_fromAng(l);
    return Matrix_setRotE(ang, an);
}
struct RandomState{
    unsigned tausx;
    unsigned tausy;
    unsigned tausz;
    unsigned lcgw;    
};
unsigned random_lcg(unsigned *state){
    *state = *state * 1664525 + 1013904223;
    return *state;
}
unsigned random_taus_step(unsigned *state, unsigned S1, unsigned S2, unsigned S3, unsigned M){
    unsigned b = ((*state << S1) ^ *state) >> S2;
    *state = ((*state & M) << S3) ^ b;
    return *state;
}
unsigned random_taus(struct RandomState* state){
    return 
        random_taus_step(&state->tausx, 13, 19, 12, 294917294) ^
        random_taus_step(&state->tausy, 2, 25, 4, 294967288) ^
        random_taus_step(&state->tausz, 3, 11, 17, 294907280) ^
        random_lcg(&state->lcgw);
}
void init_taus(struct RandomState* state, unsigned v){
    state->lcgw = v;
    random_lcg(&state->lcgw);
    state->tausx = random_lcg(&state->lcgw);
    state->tausy = random_lcg(&state->lcgw);
    state->tausz = random_lcg(&state->lcgw);
}
float random(struct RandomState* state){
    unsigned rand = random_taus(state);
    return ((float)rand) / (4294967295);
}