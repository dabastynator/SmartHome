package de.remote.mobile.util;

import java.util.ArrayList;
import java.util.List;

import android.content.res.Resources;
import de.newsystem.opengl.common.AbstractSceneRenderer;
import de.newsystem.opengl.common.fibures.GLFigure;
import de.newsystem.opengl.common.fibures.GLFigure.GLClickListener;
import de.newsystem.opengl.common.fibures.GLPolynom;
import de.newsystem.opengl.common.fibures.GLPolynom.GLPoint;
import de.newsystem.opengl.common.systems.GLBox;
import de.newsystem.opengl.common.systems.GLFloorlamp;
import de.newsystem.opengl.common.systems.GLGroup;
import de.newsystem.opengl.common.systems.GLLavalamp;
import de.newsystem.opengl.common.systems.GLLight;
import de.newsystem.opengl.common.systems.GLMediaServer;
import de.newsystem.opengl.common.systems.GLReadinglamp;
import de.newsystem.opengl.common.systems.GLTableround;
import de.newsystem.rmi.protokol.RemoteException;
import de.remote.controlcenter.api.GroundPlot;
import de.remote.controlcenter.api.GroundPlot.Feature;
import de.remote.controlcenter.api.GroundPlot.Point;
import de.remote.controlcenter.api.GroundPlot.Wall;
import de.remote.controlcenter.api.IControlCenter;
import de.remote.controlcenter.api.IControlUnit;
import de.remote.gpiopower.api.IInternetSwitch;
import de.remote.gpiopower.api.IInternetSwitch.State;
import de.remote.mediaserver.api.IMediaServer;
import de.remote.mobile.R;
import de.remote.mobile.activities.OverViewActivity.SelectMediaServer;
import de.remote.mobile.services.PlayerBinder;

public class ControlSceneRenderer extends AbstractSceneRenderer {

	private GLGroup room;
	private Resources resources;
	private GLGroup glObjects;
	private SelectMediaServer selecter;

	public ControlSceneRenderer(Resources resources, SelectMediaServer selecter) {
		super(resources);
		this.selecter = selecter;
		translateSceneBounds[4] = -2;
		translateSceneBounds[5] = -30;
	}

	@Override
	protected GLFigure createScene(Resources resources) {
		room = new GLGroup();
		this.resources = resources;
		return room;
	}

	public void reloadControlCenter(PlayerBinder binder) throws Exception {
		IControlCenter control = binder.getControlCenter();
		if (control == null)
			throw new Exception("No control center");
		if (glObjects != null)
			room.removeFigure(glObjects);
		glObjects = new GLGroup();
		GroundPlot ground = control.getGroundPlot();
		addGroundToScene(ground);
		for (IControlUnit unit: binder.getUnits().keySet()) {
			try {
				Object object = binder.getUnits().get(unit);
				float[] position = unit.getPosition();
				String name = binder.getUnitNames().get(unit);
				if (object instanceof IMediaServer) {
					GLMediaServer glMusic = new GLMediaServer(GLFigure.PLANE);
					glMusic.setTexture(GLBox.BOX,
							loadBitmap(resources, R.drawable.textur_holz), 1);
					glMusic.x = position[0];
					glMusic.y = position[2];
					glMusic.z = -position[1];
					MediaServerListener listener = new MediaServerListener(name);
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
			} catch (RemoteException e) {

			}
		}
		room.addFigure(glObjects);
	}

	private void addGroundToScene(GroundPlot ground) {
		float minX = Integer.MAX_VALUE;
		float maxX = Integer.MIN_VALUE;
		float minY = Integer.MAX_VALUE;
		float maxY = Integer.MIN_VALUE;
		for (Wall wall : ground.walls) {
			List<GLPoint> points = new ArrayList<GLPoint>();
			for (Point ccPoint: wall.points){
				GLPoint glPoint = new GLPoint();
				glPoint.x = ccPoint.x;
				glPoint.y = ccPoint.z;
				glPoint.z = -ccPoint.y;
				points.add(glPoint);
				minX = Math.min(minX, ccPoint.x);
				minY = Math.min(minY, ccPoint.y);
				maxX = Math.max(maxX, ccPoint.x);
				maxY = Math.max(maxY, ccPoint.y);
			}
			GLPolynom glWall = new GLPolynom(points);

			glObjects.addFigure(glWall);
		}
		translateScene[0] = -((maxX - minX) / 2 + minX);
		translateScene[1] = -((maxY - minY) / 2 + minY);
		translateScene[2] = -20;
		translateSceneBounds[1] = -maxX;
		translateSceneBounds[0] = -minX;
		translateSceneBounds[3] = -maxY;
		translateSceneBounds[2] = -minY;	
		
		for (Feature feature: ground.features){
			if (feature.type.equals("table")){
				GLTableround table = new GLTableround(GLFigure.PLANE, 2, 1.8f);
				table.x = feature.x;
				table.z = -feature.y;
				glObjects.addFigure(table);
			}
		}
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
