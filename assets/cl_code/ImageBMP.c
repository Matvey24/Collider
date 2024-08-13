struct ImageBMP{
	__global char* buf;
	unsigned width, height;
	unsigned llen;
};
struct SceneParam{
	float3 cam_pos;
	unsigned im_width;
	unsigned im_height;
	struct Matrix rot;
};

struct ImageBMP Image_build(struct SceneParam param, __global char* buf){
	struct ImageBMP img;
	img.width = param.im_width;
	img.height = param.im_height;
	int w = img.width * 3;
	img.llen = w + (-w & 3);
	img.buf = buf;
	return img;
}
struct ImageBMP ImageFromBMP(__global char* buf){
	struct ImageBMP img;
	img.buf = &buf[54];
	__global unsigned char* buff = (__global unsigned char*)buf;
	img.width = (buff[18]) + (buff[19] << 8) + (buff[20] << 16) + (buff[21] << 24);
	img.height = (buff[22]) + (buff[23] << 8) + (buff[24] << 16) + (buff[25] << 24);
	int w = img.width * 3;
	img.llen = w + (-w & 3);
	return img;
}
float limt(float v, float min, float max){
	if(v > max)
		return max;
	if(v < min)
		return min;
	return v;
}
void setPixel(struct ImageBMP img, int x, int y, float3 col) {
	col *= 255;
	col.x = limt(col.x, 0, 255);
	col.y = limt(col.y, 0, 255);
	col.z = limt(col.z, 0, 255);
	long index = (img.height - y - 1) * img.llen + 3 * x;
	img.buf[index] = (unsigned char)col.x;
	img.buf[index + 1] = (unsigned char)col.y;
	img.buf[index + 2] = (unsigned char)col.z;
}
float3 getThePixel(struct ImageBMP img, int x, int y){
	__global unsigned char* c = (__global unsigned char*)&img.buf[y * img.llen + 4 * x];
	return (float3)(c[2], c[1], c[0]);
}
float3 getPixel(struct ImageBMP img, float x, float y){
	x *= 0.4;
	y *= 0.5;
	x += 0.55;
	y += 0.5;
	x *= img.width;
	y *= img.height;
	int xs = (int)x;
	int ys = (int)y;
	if(xs < 0 || ys < 0 || xs >= img.width || ys >= img.height)
		return (float3)(0, 0, 0);
	float dx = x - xs;
	float dy = y - ys;
	if(dx == 0 || dy == 0)
		return getThePixel(img, xs, ys) / 255;
	if(xs == img.width - 1 || ys == img.height - 1)
		return (float3)(0, 0, 0);
 	return ((getThePixel(img, xs, ys) * (1 - dx) 
 		+ getThePixel(img, xs + 1, ys) * dx) * (1 - dy)
 		 + (getThePixel(img, xs, ys + 1) * (1 - dx)
 		  + getThePixel(img, xs + 1, ys + 1) * dx) * dy) / 255;
}
