float3 raytrace(float3 pos, float3 dir,
 struct RandomState* rand,
  struct Scene scene, int reref){
	float3 color = (float3)(1, 1, 1);
	for(int i = 0; i < reref; ++i){
		struct SurfacePoint surf = intersect(pos, dir, scene);
		if(!surf.reflects)
			return f3_sclv(color, surf.color);
		float scl = f3_scl(dir, surf.norm);
		if(scl > 0){
			return color;
			// scl = -scl;
			// surf.norm = -surf.norm;
		}
		float refl = calcReflect(scl, surf.reflect);
		if(random(rand) > refl){//diffuse
			float cosphi = random(rand);
            float sinphi = sqrt(1 - cosphi * cosphi);
            float psi = random(rand) * 2 * M_PI;
            float cosp;
            float sinp = sincos(random(rand) * 2 * M_PI, &cosp);
            dir = (float3)(cosphi, sinphi * sinp, sinphi * cosp);
            if(f3_scl(dir, surf.norm) < 0)
            	dir = -dir;
            color = f3_sclv(color, surf.color);
		}else{//reflect
			dir = dir - 2 * scl * surf.norm;
		}
		pos = surf.pos + DIFF * dir;
	}
	return (float3)(0, 0, 0);
}
float3 updateDiffuse(float3 pos, float3 norm,
	struct RandomState* rand,
	struct Scene scene, int reref, int disc){
    if(disc == 0)
    	disc = 1;
	int width = sqrt((float)disc);
	int height = disc / width;
	float scale = 1. / (width * height);
	float3 color = (float3)(0, 0, 0);

	for(int x = 0; x < width; ++x){
		for(int y = 0; y < height; ++y){
			float cosphi = (random(rand) + y) / height;
            float sinphi = sqrt(1 - cosphi * cosphi);
            float psi = (random(rand) + x) * 2 * M_PI / width;
            float cosp;
            float sinp = sincos(psi, &cosp);
            float3 dir = (float3)(cosphi, sinphi * sinp, sinphi * cosp);
            if(f3_scl(dir, norm) < 0)
            	dir = -dir;
            float3 pos2 = pos + DIFF * dir;
            color += scale * raytrace(pos2, dir, rand, scene, reref);
		}
	}
	return color;
}
float3 getColor(float3 pos, float3 dir,
	struct RandomState* rand,
	struct Scene scene) {
	
	float3 color = (float3)(0, 0, 0);
	float refl_scale = 1;
	
	float refl_min = 1. / 1000;
	int reref = 30;

	for(; reref >= 0; reref--){
		struct SurfacePoint surf = intersect(pos, dir, scene);
		if(!surf.reflects)
			return color + refl_scale * surf.color;

		float scl = f3_scl(dir, surf.norm);
		if(scl > 0){
			return color;
			// scl = -scl;
			// surf.norm = -surf.norm;
		}
		float refl = calcReflect(scl, surf.reflect);
		int discDiffuse = (1 - refl) * refl_scale / refl_min;
		float3 diffuse = updateDiffuse(
			surf.pos, surf.norm, rand, scene, reref, discDiffuse);
		color += (1 - refl) * refl_scale * f3_sclv(surf.color, diffuse);
		refl_scale *= refl;
		if(refl_scale < refl_min)
			return color;
		dir = dir - 2 * scl * surf.norm;
		pos = surf.pos + DIFF * dir;
	}
	return color;
}
float3 getColorNormal(float3 pos, float3 dir,
	struct RandomState* rand,
	struct Scene scene){
	struct SurfacePoint sp = intersect(pos, dir, scene);
	if(!sp.reflects)
		return (float3)(0, 0, 0);
	return (float3)(f_abs(sp.norm.x), f_abs(sp.norm.y), f_abs(sp.norm.z));
}
float3 getSimpleColor(float3 pos, float3 dir, struct RandomState* rand, struct Scene scene){
	float3 color = (float3)(0, 0, 0);
	float refl_scale = 1;
	
	int reref = 30;
	float refl_min = 0.001f;
	for(; reref >= 0; reref--){
		struct SurfacePoint surf = intersect(pos, dir, scene);
		if(!surf.reflects)
			return color + refl_scale * surf.color;

		float scl = f3_scl(dir, surf.norm);
		if(scl > 0){
			scl = -scl;
			surf.norm = -surf.norm;
		}
		float refl = calcReflect(scl, surf.reflect);
		float3 dir2 = (float3)(-2, 2, -2);
		dir2 = normalize(dir2);
		struct SurfacePoint sp = intersect(surf.pos + DIFF * dir2, dir2, scene);
		
		if(!sp.reflects){
			float power = 0.8 * f3_scl(surf.norm, dir2);
			if(power < 0)
				power = -power;
			color += power * (1 - refl) * refl_scale * surf.color;
		}

		color += 0.2f * (1 - refl) * refl_scale * surf.color;
		
		refl_scale *= refl;
		if(refl_scale < refl_min)
			return color;

		dir = dir - 2 * scl * surf.norm;
		pos = surf.pos + DIFF * dir;
	}
	return color;
}
void mem_cpy(__constant int* scene_tmp, __local int* scene_ref){
		int p = 0;
		int count = scene_tmp[p];
		scene_ref[p] = count;
		p++;
		for(; p < count; ++p)
			scene_ref[p] = scene_tmp[p];	
}
__kernel void worker_main(
	__constant struct SceneParam *param_p,
	__constant int* scene_val,
	__local int* scene_buf,
	__global char* data,
	__global char* texture) {
	int x = get_global_id(0);
	int y = get_global_id(1);

	if(get_local_id(0) == 0)
		mem_cpy(scene_val, scene_buf);

	barrier(CLK_LOCAL_MEM_FENCE);
	struct Scene scene;
	scene.sc = &scene_buf[1];
	scene.texture = ImageFromBMP(texture);

	struct SceneParam param = *param_p;
	struct ImageBMP img = Image_build(param, data);
	if(img.width <= x || img.height <= y)
		return;
	struct RandomState rand;
	init_taus(&rand, (x * param.im_width + y) * 12342 + 1237521);

	float FOV = 60;
	float table_offset = img.width / tan(FOV / 360 * M_PI) / 2;
	float3 dir = (float3)(x - img.width / 2., y - img.height / 2., table_offset);
	dir = Matrix_transform(param.rot, dir);

	dir = normalize(dir);
	float3 rgb = getSimpleColor(param.cam_pos, dir, &rand, scene);
	setPixel(img, x, y, rgb);
}
