package de.neo.remote.mobile.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import de.neo.opengl.common.AbstractSceneRenderer;
import de.neo.opengl.common.figures.GLFigure;
import de.neo.opengl.common.figures.GLFigure.GLClickListener;
import de.neo.opengl.common.figures.GLPolynom;
import de.neo.opengl.common.figures.GLPolynom.GLPoint;
import de.neo.opengl.common.figures.GLSquare;
import de.neo.opengl.common.systems.GLBox;
import de.neo.opengl.common.systems.GLCube;
import de.neo.opengl.common.systems.GLFlatScreen;
import de.neo.opengl.common.systems.GLFloorlamp;
import de.neo.opengl.common.systems.GLGroup;
import de.neo.opengl.common.systems.GLLaptop;
import de.neo.opengl.common.systems.GLLavalamp;
import de.neo.opengl.common.systems.GLMediaServer;
import de.neo.opengl.common.systems.GLReadinglamp;
import de.neo.opengl.common.systems.GLSwitch;
import de.neo.opengl.common.systems.GLTableround;
import de.neo.opengl.common.touchhandler.TranslateSceneHandler;
import de.neo.remote.api.GroundPlot;
import de.neo.remote.api.GroundPlot.Feature;
import de.neo.remote.api.GroundPlot.Point;
import de.neo.remote.api.GroundPlot.Wall;
import de.neo.remote.api.IControlCenter;
import de.neo.remote.api.IInternetSwitch;
import de.neo.remote.api.IInternetSwitch.State;
import de.neo.remote.api.IMediaServer;
import de.neo.remote.api.PlayingBean;
import de.neo.remote.api.PlayingBean.STATE;
import de.neo.remote.mobile.activities.ControlSceneActivity.SelectMediaServer;
import de.neo.remote.mobile.services.PlayerBinder;
import de.neo.remote.mobile.services.RemoteService.BufferdUnit;
import de.neo.rmi.protokol.RemoteException;
import de.remote.mobile.R;

public class ControlSceneRenderer extends AbstractSceneRenderer {

	public static final String AUDO = "audio";
	public static final String VIDEO = "video";
	public static final String LAMP_LAVA = "lavalamp";
	public static final String LAMP_READ = "readinglamp";
	public static final String LAMP_FLOOR = "floorlamp";
	public static final String TABLE = "table";

	private GLGroup room;
	private GLGroup glObjects;
	private SelectMediaServer selecter;

	private Map<String, GLFigure> glMediaServers;
	private Map<String, GLSwitch> glSwitches;
	private TranslateSceneHandler handler;

	public ControlSceneRenderer(Context context, SelectMediaServer selecter) {
		super(context);
		this.selecter = selecter;
		setGradient(new float[] { 0.3f, 0.3f, 1, 1 },
				new float[] { 1, 1, 1, 1 });
		handler = new TranslateSceneHandler();
		handler.sceneRotation.rotateByAngleAxis(Math.PI / 4, 1, 0, 0);
		handler.zoomBounds[0] = -4;
		handler.zoomBounds[1] = -15;
		setTouchSceneHandler(handler);
		setLighting(true);
		glMediaServers = new HashMap<String, GLFigure>();
		glSwitches = new HashMap<String, GLSwitch>();
	}

	@Override
	protected GLFigure createScene() {
		room = new GLGroup();
		return room;
	}

	public void reloadControlCenter(PlayerBinder binder) throws RemoteException {
		IControlCenter control = binder.getControlCenter();
		if (glObjects != null)
			room.removeFigure(glObjects);
		glMediaServers.clear();
		glSwitches.clear();
		if (control == null)
			return;
		glObjects = new GLGroup();
		GroundPlot ground = control.getGroundPlot();
		addGroundToScene(ground);
		for (String id : binder.getUnits().keySet()) {
			try {
				BufferdUnit unit = binder.getUnits().get(id);
				if (unit.mObject instanceof IMediaServer) {
					GLFigure glServer = createGLMediaServer(unit.mDescription);
					glServer.position[0] = unit.mPosition[0];
					glServer.position[1] = unit.mPosition[2];
					glServer.position[2] = -unit.mPosition[1];
					MediaServerListener listener = new MediaServerListener(unit.mID);
					glServer.setOnClickListener(listener);
					glObjects.addFigure(glServer);
					glMediaServers.put(unit.mID, glServer);
				}
				if (unit.mObject instanceof IInternetSwitch) {
					IInternetSwitch internet = (IInternetSwitch) unit.mObject;
					String type = internet.getType();
					GLSwitch light = loadGLSwitchByType(type);
					light.setSwitch(internet.getState() == State.ON);
					light.position[0] = unit.mPosition[0];
					light.position[1] = unit.mPosition[2];
					light.position[2] = -unit.mPosition[1];
					InternetSwitchListener listener = new InternetSwitchListener(
							light, internet);
					light.setOnClickListener(listener);
					glObjects.addFigure(light);
					glSwitches.put(unit.mID, light);
				}
			} catch (RemoteException e) {

			}
		}
		room.addFigure(glObjects);
	}

	private GLFigure createGLMediaServer(String description) {
		if ("multimedia".equals(description)) {
			GLMediaServer glMusic = new GLMediaServer(GLFigure.STYLE_PLANE,
					true);
			glMusic.setTexture(GLBox.BOX, loadBitmap(R.drawable.textur_holz), 1);
			glMusic.setTexture(GLFlatScreen.BOTTOM,
					loadBitmap(R.drawable.textur_metal), 1);
			return glMusic;
		}
		if ("remote".equals(description)) {
			GLCube cube = new GLCube(GLFigure.STYLE_PLANE);
			cube.setTexture(loadBitmap(R.drawable.ic_launcher));
			cube.size[0] = cube.size[1] = cube.size[2] = 0.3f;
			cube.setColor(1, 1, 1);
			return cube;
		}
		if ("laptop".equals(description)) {
			GLLaptop laptop = new GLLaptop(GLFigure.STYLE_PLANE,
					(float) (3 * Math.PI / 4));
			laptop.setTexture(GLLaptop.SURFACE_DISPLAY,
					loadBitmap(R.drawable.textur_image_sunset));
			laptop.setTexture(GLLaptop.SURFACE_KEYBOARD,
					loadBitmap(R.drawable.textur_keyboard));
			return laptop;
		}
		GLBox box = new GLBox(GLFigure.STYLE_PLANE);
		box.setTexture(GLBox.BOX, loadBitmap(R.drawable.textur_holz), 1);
		return box;
	}

	private void addGroundToScene(GroundPlot ground) {
		float minX = Integer.MAX_VALUE;
		float maxX = Integer.MIN_VALUE;
		float minY = Integer.MAX_VALUE;
		float maxY = Integer.MIN_VALUE;
		for (Wall wall : ground.walls) {
			List<GLPoint> points = new ArrayList<GLPoint>();
			for (Point ccPoint : wall.points) {
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
			GLPolynom glWall = new GLPolynom(points, 2);

			glObjects.addFigure(glWall);
		}
		handler.translateScene[0] = -((maxX - minX) / 2 + minX);
		handler.translateScene[1] = ((maxY - minY) / 2 + minY);
		handler.zoom = -15;
		handler.translateSceneBounds[0] = -minX;
		handler.translateSceneBounds[1] = -maxX;
		handler.translateSceneBounds[2] = maxY;// -minY;
		handler.translateSceneBounds[3] = minY;// -maxY;

		GLSquare laminat = new GLSquare(GLFigure.STYLE_PLANE);
		laminat.position[0] = (maxX - minX) / 2 + minX;
		laminat.position[2] = -(maxY - minY) / 2 - minY;
		laminat.position[1] = -0.03f;
		laminat.size[0] = (maxX - minX);
		laminat.size[1] = (maxY - minY);
		laminat.rotation.rotateByAngleAxis(Math.PI / 2, 1, 0, 0);
		laminat.color[0] = laminat.color[1] = laminat.color[2] = 1;
		laminat.setTexture(loadBitmap(R.drawable.textur_wood), laminat.size[0],
				laminat.size[1]);
		glObjects.addFigure(laminat);

		for (Feature feature : ground.features) {
			GLFigure figure = null;
			if (feature.type.equals("table")) {
				figure = new GLTableround(GLFigure.STYLE_PLANE, 1.4f, 1.5f);
				figure.setTexture(loadBitmap(R.drawable.textur_wood));
			}
			if (feature.type.equals("picture")) {
				figure = new GLSquare(GLFigure.STYLE_PLANE);
				figure.size[0] = 2.2f;
				figure.color[0] = figure.color[1] = figure.color[2] = 1;
				if ("leaves".equals(feature.extra))
					figure.setTexture(loadBitmap(R.drawable.textur_image_leaves));
				if ("africa".equals(feature.extra))
					figure.setTexture(loadBitmap(R.drawable.textur_image_africa));
				if ("sunset".equals(feature.extra))
					figure.setTexture(loadBitmap(R.drawable.textur_image_sunset));
			}
			if (figure != null) {
				figure.position[0] = feature.x;
				figure.position[2] = -feature.y;
				figure.position[1] = feature.z;
				figure.rotation.rotateByAngleAxis(180 * feature.az / Math.PI,
						0, 1, 0);
				figure.color[3] = 0.5f;
				glObjects.addFigure(figure);
			}
		}
	}

	private GLSwitch loadGLSwitchByType(String type) {
		if (type.equalsIgnoreCase(LAMP_FLOOR)) {
			GLFloorlamp lamp = new GLFloorlamp(GLFigure.STYLE_PLANE);
			lamp.setTexture(GLFloorlamp.BOTTOM | GLFloorlamp.PILLAR,
					loadBitmap(R.drawable.textur_mamor));
			return lamp;
		}
		if (type.equalsIgnoreCase(LAMP_READ)) {
			GLReadinglamp lamp = new GLReadinglamp(GLFigure.STYLE_PLANE);
			return lamp;
		}
		if (type.equalsIgnoreCase(LAMP_LAVA)) {
			GLLavalamp lamp = new GLLavalamp(20, GLFigure.STYLE_PLANE);
			return lamp;
		}
		if (type.equalsIgnoreCase(VIDEO)) {
			GLFlatScreen video = new GLFlatScreen(GLFigure.STYLE_PLANE, 1.2f,
					0.67f, 2f);
			video.setSwitchTexture(GLFlatScreen.SCREEN,
					loadBitmap(R.drawable.textur_image_sunset), true);
			video.setTexture(GLFlatScreen.BOTTOM,
					loadBitmap(R.drawable.textur_metal));
			return video;
		}
		if (type.equalsIgnoreCase(AUDO)) {
			GLMediaServer audio = new GLMediaServer(GLFigure.STYLE_PLANE, false);
			audio.setTexture(GLBox.BOX, loadBitmap(R.drawable.textur_holz), 1);
			return audio;
		}
		GLLavalamp lamp = new GLLavalamp(20, GLFigure.STYLE_PLANE);
		return lamp;
	}

	class MediaServerListener implements GLClickListener {

		private String mID;

		public MediaServerListener(String id) {
			mID = id;
		}

		@Override
		public void onGLClick() {
			selecter.selectMediaServer(mID);
		}

	}

	class InternetSwitchListener implements GLClickListener {

		private GLSwitch ligthtObject;
		private IInternetSwitch internet;

		public InternetSwitchListener(GLSwitch ligthtObject,
				IInternetSwitch internet) {
			this.ligthtObject = ligthtObject;
			this.internet = internet;
		}

		@Override
		public void onGLClick() {
			final boolean lightOn = !ligthtObject.isSwitchOn();
			new Thread() {
				public void run() {
					try {
						de.neo.remote.api.IInternetSwitch.State state = de.neo.remote.api.IInternetSwitch.State.OFF;
						if (lightOn)
							state = de.neo.remote.api.IInternetSwitch.State.ON;
						internet.setState(state);
					} catch (RemoteException e) {
					}
				};
			}.start();
			ligthtObject.setSwitch(lightOn);
		}

	}

	public void setPlayingBean(String mediaserver, PlayingBean bean) {
		GLFigure glFigure = glMediaServers.get(mediaserver);
		if (glFigure instanceof GLMediaServer) {
			Bitmap bitmap = createBitmapByText(getBeanString(bean));
			((GLMediaServer) glFigure).setTexture(GLFlatScreen.SCREEN, bitmap,
					1);
		}
	}

	private Bitmap createBitmapByText(String text) {
		// Create an empty, mutable bitmap
		Bitmap bitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_4444);
		// get a canvas to paint over the bitmap
		Canvas canvas = new Canvas(bitmap);
		bitmap.eraseColor(0);

		// set bitmap background
		canvas.drawRGB(0, 0, 0);

		// Draw the text
		Paint textPaint = new Paint();
		textPaint.setTextSize(32);
		textPaint.setAntiAlias(true);
		textPaint.setARGB(0xff, 0xFF, 0xFF, 0x00);

		// draw the text centered
		int i = 0;
		while (text.length() > 0) {
			i++;
			String line = text;
			if (text.contains("\n")) {
				line = text.substring(0, text.indexOf('\n'));
				text = text.substring(text.indexOf('\n') + 1);
			} else
				text = "";
			canvas.drawText(line, 10, 20 + i * 30, textPaint);
		}
		Matrix matrix = new Matrix();
		matrix.preScale(1.0f, -1.0f);
		Bitmap mirroredBitmap = Bitmap.createBitmap(bitmap, 0, 0,
				bitmap.getWidth(), bitmap.getHeight(), matrix, false);
		return mirroredBitmap;
	}

	private String getBeanString(PlayingBean bean) {
		String str = "";
		if (bean == null)
			return str;
		if (bean.getTitle() != null)
			str = str + "-- Title --\n" + bean.getTitle() + "\n";
		if (bean.getArtist() != null)
			str = str + "-- Artist --\n" + bean.getArtist() + "\n";
		if (bean.getAlbum() != null)
			str = str + "Album: " + bean.getAlbum() + "\n";
		if (str.length() == 0 && bean.getFile() != null)
			str = str + bean.getFile() + "\n";
		if (bean.getState() == STATE.PAUSE)
			str = str + "Pause";
		return str;
	}

	public void powerSwitchChanged(String _switch, State state) {
		GLSwitch glSwitch = glSwitches.get(_switch);
		if (glSwitch != null) {
			glSwitch.setSwitch(state == State.ON);
		}
	}

}
