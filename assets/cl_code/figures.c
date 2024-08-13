#define OBJECT_SPHERE 1
#define OBJECT_RECT 2
#define OBJECT_RECT_NOROT 3
#define OBJECT_FRACTAL 4

#define MATERIAL_FILL 0
#define MATERIAL_LIGHT 1
#define MATERIAL_TEXTURE 2
#define DOT_FULL 14

struct SPoint{
	__local int* dot;
	float d;
    float info;
};
struct SurfacePoint{
	char reflects;
	float3 color;
	float3 pos;
	float3 norm;
	float reflect;
};
struct Scene{
	__local int* sc;
	struct ImageBMP texture;
};
float3 getFloat3(__local int* dat){
	return (float3)(
		*(__local float*)&dat[0], 
		*(__local float*)&dat[1], 
		*(__local float*)&dat[2]);
	//return (*(__local double3*)dat);
}
struct Matrix DotGetRot(__local int* dat){
	struct Matrix m;
	m.a1 = *(__local float*)&dat[4];
	m.a2 = *(__local float*)&dat[5];
	m.a3 = *(__local float*)&dat[6];
	m.b1 = *(__local float*)&dat[7];
	m.b2 = *(__local float*)&dat[8];
	m.b3 = *(__local float*)&dat[9];
	m.c1 = *(__local float*)&dat[10];
	m.c2 = *(__local float*)&dat[11];
	m.c3 = *(__local float*)&dat[12];
	return m;
}
struct SPoint sphInter(__local int* sphere, float3 pos, float3 dir){
	struct SPoint p;
	pos = getFloat3(&sphere[1]) - pos;
	p.d = nasphInter(*(__local float*)&sphere[DOT_FULL], pos, dir);
	p.dot = sphere;
	return p;
}
struct SPoint rectInter(__local int* rect, float3 pos, float3 dir){
	struct Matrix m = DotGetRot(rect);
	pos = getFloat3(&rect[1]) - pos;
	pos = Matrix_transform(m, pos);
	dir = Matrix_transform(m, dir);
	struct SPoint p;
	p.d = naRectInter(getFloat3(&rect[DOT_FULL]), pos, dir);
	p.dot = rect;
	return p;
}
struct SPoint rectNorotInter(__local int* rect, float3 pos, float3 dir){
	pos = getFloat3(&rect[1]) - pos;
	struct SPoint p;
	p.d = naRectInter(getFloat3(&rect[DOT_FULL]), pos, dir);
	p.dot = rect;
	return p;
}
struct SPoint fractInter(__local int* mandelbulb, float3 pos, float3 dir){
	pos = getFloat3(&mandelbulb[1]) - pos;
	float dist = nasphInter(4, pos, dir);
	float size = *(__local float*)&mandelbulb[DOT_FULL];
	if(dist != dist){
		struct SPoint p;
		p.d = NAN;
		return p;
	}
	pos = -pos;
	struct Matrix m = DotGetRot(mandelbulb);
	pos = Matrix_transform(m, pos);
	dir = Matrix_transform(m, dir);

	//dist /= size;
	//pos /= size;

	struct SPoint p;
	float2 vec = naFractalInter(dist, pos, dir, size);
	p.d = vec.x;//*size
	p.dot = mandelbulb;
	p.info = vec.y;
	return p;
}

struct SurfacePoint intersect(float3 pos, float3 dir, struct Scene scene){
	int p = 0;
	int count = scene.sc[p];
	p++;
	struct SPoint near;
	struct SPoint cur;
	near.d = INFINITY;

	for(int i = 0; i < count; ++i){
		switch(scene.sc[p]){
		case OBJECT_SPHERE:
			cur = sphInter(&scene.sc[p], pos, dir);
			p += DOT_FULL + 1;
			break;
		case OBJECT_RECT:
			cur = rectInter(&scene.sc[p], pos, dir);
			p += DOT_FULL + 3;
			break;
		case OBJECT_RECT_NOROT:
			cur = rectNorotInter(&scene.sc[p], pos, dir);
			p += DOT_FULL + 3;
			break;
		case OBJECT_FRACTAL:
			cur = fractInter(&scene.sc[p], pos, dir);
			p += DOT_FULL + 1;
			break;
		default:
			cur.d = NAN;
			break;
		}
		if(cur.d < near.d)
			near = cur;
	}

	struct SurfacePoint surf;
	if(near.d == INFINITY){
		surf.reflects = false;
		surf.color = (float3)(0, 0, 0);
		return surf;
	}
	
	int mater_d = near.dot[DOT_FULL - 1];
	struct Matrix m;

	switch(scene.sc[mater_d]){
	case MATERIAL_FILL:
		surf.color = getFloat3(&scene.sc[mater_d + 1]);
		surf.reflects = true;
		surf.reflect = *(__local float*)&scene.sc[mater_d + 4];
		break;
	case MATERIAL_LIGHT:
		surf.color = getFloat3(&scene.sc[mater_d + 1]);
		surf.reflects = false;
		return surf;
	case MATERIAL_TEXTURE:{
		float3 cur_pos = pos + (near.d * dir);
		cur_pos = cur_pos - getFloat3(&near.dot[1]);
		m = DotGetRot(near.dot);
		cur_pos = Matrix_transform(m, cur_pos);
		surf.color = getPixel(scene.texture, (cur_pos.x - cur_pos.z) / 2, cur_pos.y);
		surf.reflects = true;
		surf.reflect = *(__local float*)&scene.sc[mater_d + 4];
		break;
	}
	default:
		surf.reflects = false;
		surf.color = (float3)(0, 0, 0);
		return surf;
	}

	float3 tmp;
	surf.pos = pos + (near.d * dir);
	surf.norm = surf.pos - getFloat3(&near.dot[1]);
	float diff = 0.0001f;

	switch(*near.dot){
	case OBJECT_SPHERE:
		surf.norm = normalize(surf.norm);
		break;
	case OBJECT_RECT:
		m = DotGetRot(near.dot);
		surf.norm = Matrix_transform(m, surf.norm);
		tmp = getFloat3(&near.dot[DOT_FULL]);
		{
			float x = surf.norm.x / tmp.x, y = surf.norm.y / tmp.y, z = surf.norm.z / tmp.z;
			x *= x;
			y *= y;
			z *= z;
			char xz = x > z, xy = x > y, yz = y > z;
			surf.norm = (float3)(
				xz && xy,
				!xy && yz,
				!xz && !yz);
		}
		surf.norm = Matrix_transformBack(m, surf.norm);
		break;
	case OBJECT_RECT_NOROT:
		tmp = getFloat3(&near.dot[DOT_FULL]);
		{
			float x = surf.norm.x / tmp.x, y = surf.norm.y / tmp.y, z = surf.norm.z / tmp.z;
			x *= x;
			y *= y;
			z *= z;
			char xz = x > z, xy = x > y, yz = y > z;
			surf.norm = (float3)(
				xz && xy,
				!xy && yz,
				!xz && !yz);
		}
		break;
	case OBJECT_FRACTAL:
		m = DotGetRot(near.dot);
		surf.norm = Matrix_transform(m, surf.norm);
		float size = *(__local float*)&near.dot[DOT_FULL];
		// surf.norm /= size;
		{
			float xp = naMandelBulbDEHalf(surf.norm + (float3)(diff, 0, 0), size);
			float xn = naMandelBulbDEHalf(surf.norm - (float3)(diff, 0, 0), size);
			float yp = naMandelBulbDEHalf(surf.norm + (float3)(0, diff, 0), size);
			float yn = naMandelBulbDEHalf(surf.norm - (float3)(0, diff, 0), size);
			float zp = naMandelBulbDEHalf(surf.norm + (float3)(0, 0, diff), size);
			float zn = naMandelBulbDEHalf(surf.norm - (float3)(0, 0, diff), size);
			surf.norm = (float3)(xp - xn, yp - yn, zp - zn) / (2 * diff);
		}
		surf.norm = Matrix_transformBack(m, surf.norm);
		surf.norm = normalize(surf.norm);
		// surf.color *= near.info / 100;
		break;
	default:
		surf.reflects = false;
		surf.color = (float3)(0, 0, 0);
		break;
	}
	return surf;
}
