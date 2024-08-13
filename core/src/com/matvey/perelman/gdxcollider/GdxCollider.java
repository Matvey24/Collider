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
import com.matvey.perelman.gdxcollider.collider.core.Chunk;
import com.matvey.perelman.gdxcollider.collider.twodim.Dynamic2D;
import com.matvey.perelman.gdxcollider.collider.twodim.objects.Sphere2D;
import com.matvey.perelman.gdxcollider.raytracer.GPU;

import static com.matvey.perelman.gdxcollider.WorldCreator.sy;

public class GdxCollider extends ApplicationAdapter {
	SpriteBatch world_batch, ui_batch;
	Texture sph, pixel;
	WorldCreator creator;
	public boolean RTX_ON = false;
	public Runnable task;
	public GPU gpu;
	public boolean made;
	//public Camera camera;
	public BitmapFont font;
	MediumValue updates;
	private float speed = 0.001f;
	private boolean paused, save_paused;
	public Camera cam;
	public float scale = 1;
	public Vector2 movement = new Vector2();
	public Vector2 last_used_position = new Vector2();
	public float roll;
	private int window_h = 1280, window_w = 720;
 	@Override
	public void create () {
		updates = new MediumValue();
		world_batch = new SpriteBatch();
		ui_batch = new SpriteBatch();
		int size = 40;
		sph = TextureGenerator.genSphere(size * 4, Color.WHITE);
		pixel = TextureGenerator.genPixel(Color.WHITE);
		creator = new WorldCreator(sph, pixel);
//		if(RTX_ON){
//			gpu = new GPU();
//		}
		roll = 0.001f;
		cam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		float target_time = 0f;
		do{
			update((float)(0.016));
		}while (creator.world.time < target_time);
		font = new BitmapFont();
		Gdx.input.setInputProcessor(new InputAdapter(){
			@Override
			public boolean keyDown(int keycode) {
				switch (keycode){
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
					case Input.Keys.F11:
						if(Gdx.graphics.isFullscreen()){
							Gdx.graphics.setWindowedMode(window_w, window_h);
						}else{
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
				switch (keycode){
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
				Vector3 pos = new Vector3(screenX, screenY, 0);
				cam.unproject(pos);

				if(button == Input.Buttons.RIGHT) {
					creator.inject(pos);
					return true;
				}
				if(button == Input.Buttons.LEFT){
					creator.onClick(pos);
					if(creator.preferred != null){
						last_used_position.set(creator.preferred.cur_pos);
					}
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

	private void camera_update(float dt){
		scale *= (float)Math.exp(roll);
		roll = 0;
		cam.viewportWidth = Gdx.graphics.getWidth() * scale;
		cam.viewportHeight = Gdx.graphics.getHeight() * scale;
		creator.screen_scale = scale;
		float mv_scale = dt * Gdx.graphics.getHeight() * scale;
		cam.position.add(movement.x * mv_scale, movement.y * mv_scale, 0);
		if(creator.preferred != null){
			last_used_position.sub(creator.preferred.cur_pos);
			last_used_position.scl(-1);
			cam.position.add(last_used_position.x, last_used_position.y, 0);
			last_used_position.set(creator.preferred.cur_pos);
		}

		cam.update();
		world_batch.setProjectionMatrix(cam.combined);
	}
	@Override
	public void render () {
		try {
			update(Gdx.graphics.getDeltaTime());
			ScreenUtils.clear(0, 0, 0, 1);
			world_batch.begin();
//			if (RTX_ON) {
//				batch.draw(camera.getResult(), 0, 0);
//			} else {
				creator.render(world_batch);
//			}
			creator.emitter.render(world_batch);

			world_batch.end();
			ui_batch.begin();
			font.draw(ui_batch, "avg: " + updates.avg + ", sum: " + updates.full_sum, 0, Gdx.graphics.getHeight());
			font.draw(ui_batch, String.valueOf(speed * 1000), 0, Gdx.graphics.getHeight() - 20);
			font.draw(ui_batch, String.valueOf(creator.world.time), 0, Gdx.graphics.getHeight() - 40);
			if(creator.preferred != null){
				font.draw(ui_batch, creator.preferred.vel + ", " + creator.preferred.pos, 0, Gdx.graphics.getHeight() - 60);
			}

			ui_batch.end();
		}catch (Exception e){
			e.printStackTrace();
			Gdx.app.exit();
		}
	}
	public void update(float dt){
//		if(gpu != null && !made){
//			camera = new Camera(gpu, creator.scene, 1280, 720);
//			camera.render_time = false;
//			camera.pos.set(640, 360, -1300);
//			task.run();
//			made = true;
//		}
		if(!paused) {
			creator.world.update(speed);
			creator.emitter.update(speed);
			int ev = creator.world.scheduler.events;
			updates.add(ev);
		}
		camera_update(dt);
//		eee
	}

	@Override
	public void dispose () {
		world_batch.dispose();
		sph.dispose();
		pixel.dispose();
		creator.emitter.close();
		ui_batch.dispose();
	}
}
