package com.matvey.perelman.gdxcollider;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;


// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher {
	public static void main (String[] arg) {
		new DesktopLauncher();
	}
	public static void start(GdxCollider collider){
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setTitle("MyGdxCollider");
		config.setWindowedMode(1280, 720);
		config.setForegroundFPS(60);
//		config.setFullscreenMode(Lwjgl3ApplicationConfiguration.getDisplayMode());
		new Lwjgl3Application(collider, config);
	}
	private final GdxCollider collider;
	public DesktopLauncher(){
//		setBounds(0,100, 200, 100);
//		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//		setLayout(null);
//		JToggleButton btn = new JToggleButton("RTX OFF");
//		btn.setBounds(5, 5, 170, 30);
//		add(btn);
		collider = new GdxCollider();
		collider.task = ()->{
//			setVisible(true);
		};
//		btn.addChangeListener((e)->{
//			if(btn.isSelected()){
//				btn.setText("RTX ON");
//			}else{
//				btn.setText("RTX OFF");
//			}
//			collider.RTX_ON = btn.isSelected();
//		});
		new Thread(()-> start(collider)).start();
	}
}
