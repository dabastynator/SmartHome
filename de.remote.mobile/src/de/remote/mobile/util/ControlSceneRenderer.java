package de.remote.mobile.util;

import android.content.res.Resources;
import de.newsystem.opengl.common.AbstractSceneRenderer;
import de.newsystem.opengl.common.fibures.GLCube;
import de.newsystem.opengl.common.fibures.GLFigure;
import de.newsystem.opengl.common.fibures.GLFigure.GLClickListener;
import de.newsystem.opengl.common.systems.GLBox;
import de.newsystem.opengl.common.systems.GLFloorlamp;
import de.newsystem.opengl.common.systems.GLGroup;
import de.newsystem.opengl.common.systems.GLLavalamp;
import de.newsystem.opengl.common.systems.GLLight;
import de.newsystem.opengl.common.systems.GLMusicStation;
import de.newsystem.opengl.common.systems.GLReadinglamp;
import de.newsystem.rmi.protokol.RemoteException;
import de.remote.controlcenter.api.GroundPlot;
import de.remote.controlcenter.api.GroundPlot.Wall;
import de.remote.controlcenter.api.IControlCenter;
import de.remote.controlcenter.api.IControlUnit;
import de.remote.gpiopower.api.IInternetSwitch;
import de.remote.gpiopower.api.IInternetSwitch.State;
import de.remote.mediaserver.api.IMediaServer;
import de.remote.mobile.R;
import de.remote.mobile.activities.OverViewActivity.SelectMediaServer;

public class ControlSceneRenderer extends AbstractSceneRenderer {

	private GLGroup room;
	private Resources resources;
	private GLGroup glObjects;
	private SelectMediaServer selecter;

	public ControlSceneRenderer(Resources resources, SelectMediaServer selecter) {
		super(resources);
		this.selecter = selecter;
	}

	@Override
	protected GLFigure createScene(Resources resources) {
		room = new GLGroup();
		this.resources = resources;
		return room;
	}

	public void reloadControlCenter(IControlCenter center)
			throws RemoteException {
		if (glObjects != null)
			room.removeFigure(glObjects);
		glObjects = new GLGroup();
		GroundPlot ground = center.getGroundPlot();
		float minX = Integer.MAX_VALUE;
		float maxX = Integer.MIN_VALUE;
		float minY = Integer.MAX_VALUE;
		float maxY = Integer.MIN_VALUE;
		for (Wall wall : ground.walls) {
			GLCube glWall = new GLCube(GLFigure.GRID);
			glWall.SizeX = Math.abs(wall.x2 - wall.x1) + wall.depth;
			glWall.SizeZ = Math.abs(wall.y2 - wall.y1) + wall.depth;
			glWall.SizeY = wall.height;

			glWall.x = (wall.x1 + wall.x2) / 2;
			glWall.z = -(wall.y1 + wall.y2) / 2;
			glWall.y = glWall.SizeY / 2;

			glObjects.addFigure(glWall);

			minX = Math.min(Math.min(minX, wall.x1), wall.x2);
			minY = Math.min(Math.min(minY, wall.y1), wall.y2);
			maxX = Math.max(Math.max(maxX, wall.x1), wall.x2);
			maxY = Math.max(Math.max(maxY, wall.y1), wall.y2);
		}
		mooveX = -((maxX - minX) / 2 + minX);
		mooveY = -((maxY - minY) / 2 + minY);
		maxmooveX = -maxX;
		minmooveX = -minX;
		maxmooveY = -maxY;
		minmooveY = -minY;
		zoom = -20;
		int unitCount = center.getControlUnitNumber();
		for (int i = 0; i < unitCount; i++) {
			IControlUnit unit = center.getControlUnit(i);
			Object object = unit.getRemoteableControlObject();
			float[] position = unit.getPosition();
			if (object instanceof IMediaServer) {
				IMediaServer media = (IMediaServer) object;
				GLMusicStation glMusic = new GLMusicStation(GLFigure.PLANE);
				glMusic.setTexture(GLBox.BOX,
						loadBitmap(resources, R.drawable.textur_holz), 1);
				glMusic.x = position[0];
				glMusic.y = position[2];
				glMusic.z = -position[1];
				MediaServerListener listener = new MediaServerListener(
						unit.getName());
				glMusic.setOnClickListener(listener);
				glObjects.addFigure(glMusic);
			}
			if (object instanceof IInternetSwitch) {
				IInternetSwitch internet = (IInternetSwitch) object;
				String type = internet.getType();
				GLLight light = loadGLLightByType(type);
				light.setLight(internet.getState() == State.ON);
				light.x = position[0];
				light.y = position[2];
				light.z = -position[1];
				InternetSwitchListener listener = new InternetSwitchListener(
						light, internet);
				light.setOnClickListener(listener);
				glObjects.addFigure(light);
			}
		}
		room.addFigure(glObjects);
	}

	private GLLight loadGLLightByType(String type) {
		if (type.equalsIgnoreCase("floorlamp")) {
			GLFloorlamp lamp = new GLFloorlamp(GLFigure.PLANE);
			lamp.setTexture(GLFloorlamp.BOTTOM | GLFloorlamp.PILLAR,
					loadBitmap(resources, R.drawable.textur_mamor));
			return lamp;
		}
		if (type.equalsIgnoreCase("readinglamp")) {
			GLReadinglamp lamp = new GLReadinglamp(GLFigure.PLANE);
			return lamp;
		}
		if (type.equalsIgnoreCase("lavalamp")) {
			GLLavalamp lamp = new GLLavalamp(20, GLFigure.PLANE);
			return lamp;
		}
		GLLavalamp lamp = new GLLavalamp(20, GLFigure.PLANE);
		return lamp;
	}

	@Override
	protected float getNearestZoom() {
		// TODO Auto-generated method stub
		return -5;
	}

	@Override
	protected float getFarZoom() {
		// TODO Auto-generated method stub
		return -30;
	}

	class MediaServerListener implements GLClickListener {

		private String mediaServerName;

		public MediaServerListener(String mediaServerName) {
			this.mediaServerName = mediaServerName;
		}

		@Override
		public void onGLClick() {
			selecter.selectMediaServer(mediaServerName);
		}

	}

	class InternetSwitchListener implements GLClickListener {

		private GLLight ligthtObject;
		private IInternetSwitch internet;

		public InternetSwitchListener(GLLight ligthtObject,
				IInternetSwitch internet) {
			this.ligthtObject = ligthtObject;
			this.internet = internet;
		}

		@Override
		public void onGLClick() {
			final boolean lightOn = !ligthtObject.isLightOn();
			new Thread() {
				public void run() {
					try {
						de.remote.gpiopower.api.IInternetSwitch.State state = de.remote.gpiopower.api.IInternetSwitch.State.OFF;
						if (lightOn)
							state = de.remote.gpiopower.api.IInternetSwitch.State.ON;
						internet.setState(state);
					} catch (RemoteException e) {
					}
				};
			}.start();
			ligthtObject.setLight(lightOn);
		}

	}

}
