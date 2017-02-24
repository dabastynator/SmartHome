package de.neo.remote.mobile.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Base64;
import de.neo.android.opengl.AbstractSceneRenderer;
import de.neo.android.opengl.figures.GLFigure;
import de.neo.android.opengl.figures.GLFigure.GLClickListener;
import de.neo.android.opengl.figures.GLPolynom;
import de.neo.android.opengl.figures.GLPolynom.GLPoint;
import de.neo.android.opengl.figures.GLSTL;
import de.neo.android.opengl.figures.GLSquare;
import de.neo.android.opengl.figures.GLTorus;
import de.neo.android.opengl.systems.GLBox;
import de.neo.android.opengl.systems.GLCube;
import de.neo.android.opengl.systems.GLFlatScreen;
import de.neo.android.opengl.systems.GLFloorlamp;
import de.neo.android.opengl.systems.GLGroup;
import de.neo.android.opengl.systems.GLLaptop;
import de.neo.android.opengl.systems.GLLavalamp;
import de.neo.android.opengl.systems.GLMediaServer;
import de.neo.android.opengl.systems.GLReadinglamp;
import de.neo.android.opengl.systems.IGLSwitch;
import de.neo.android.opengl.touchhandler.TranslateSceneHandler;
import de.neo.remote.rmi.RemoteException;
import de.neo.smarthome.api.GroundPlot;
import de.neo.smarthome.api.GroundPlot.Point;
import de.neo.smarthome.api.GroundPlot.Wall;
import de.neo.smarthome.api.IControlCenter.BeanWeb;
import de.neo.smarthome.api.IWebAction.BeanAction;
import de.neo.smarthome.api.IWebLEDStrip.BeanLEDStrips;
import de.neo.smarthome.api.IWebMediaServer.BeanMediaServer;
import de.neo.smarthome.api.IWebSwitch;
import de.neo.smarthome.api.IWebSwitch.BeanSwitch;
import de.neo.smarthome.api.IWebSwitch.State;
import de.neo.smarthome.api.PlayingBean;
import de.neo.smarthome.api.PlayingBean.STATE;
import de.remote.mobile.R;

public class ControlSceneRenderer extends AbstractSceneRenderer {

	public static final String AUDO = "audio";
	public static final String VIDEO = "video";
	public static final String LAMP_LAVA = "lavalamp";
	public static final String LAMP_READ = "readinglamp";
	public static final String SWITCH_COFFEE = "coffee";
	public static final String LAMP_FLOOR = "floorlamp";
	public static final String TABLE = "table";

	public enum UnitType {
		MediaServer, ActionComman
	}

	private GLGroup mRoom;
	private BeanClickHandler mControlUnitListener;

	private Map<String, GLFigure> mGLMediaServers;
	private Map<String, IGLSwitch> mGLSwitches;
	private Map<BeanWeb, GLFigure> mGLBeanFigureMap;
	private TranslateSceneHandler mHandler;
	private IWebSwitch mWebSwitch;

	public ControlSceneRenderer(Context context, BeanClickHandler selecter) {
		super(context);
		this.mControlUnitListener = selecter;
		setGradient(new float[] { 0.3f, 0.3f, 1, 1 }, new float[] { 1, 1, 1, 1 });
		mHandler = new TranslateSceneHandler();
		mHandler.sceneRotation.rotateByAngleAxis(Math.PI / 4, 1, 0, 0);
		mHandler.zoomBounds[0] = -4;
		mHandler.zoomBounds[1] = -15;
		setTouchSceneHandler(mHandler);
		setLighting(true);
		mGLMediaServers = new HashMap<String, GLFigure>();
		mGLSwitches = new HashMap<String, IGLSwitch>();
		mGLBeanFigureMap = new HashMap<BeanWeb, GLFigure>();
	}

	public void setSwitchApi(IWebSwitch webSwitch) {
		mWebSwitch = webSwitch;
	}

	@Override
	protected GLFigure createScene() {
		mRoom = new GLGroup();
		return mRoom;
	}

	public void addMediaServer(BeanMediaServer ms) {
		GLFigure glServer = createGLMediaServer(ms.getDescription());
		glServer.mPosition[0] = ms.getX();
		glServer.mPosition[1] = ms.getZ() + 5;
		glServer.mPosition[2] = -ms.getY();
		glServer.setOnClickListener(new GLUnitClickListener(ms));
		glServer.setOnLongClickListener(new GLUnitLongClickListener(ms));
		addFigure(glServer);
		mGLMediaServers.put(ms.getID(), glServer);
		putUnitFigure(ms, glServer);
	}

	public void addSwitch(BeanSwitch bs) {
		GLFigure light = loadGLSwitchByType(bs.getType());
		((IGLSwitch) light).setSwitch(bs.getState() == State.ON);
		light.mPosition[0] = bs.getX();
		light.mPosition[1] = bs.getZ() + 5;
		light.mPosition[2] = -bs.getY();
		InternetSwitchListener listener = new InternetSwitchListener((IGLSwitch) light, bs);
		light.setOnClickListener(listener);
		addFigure(light);
		mGLSwitches.put(bs.getID(), (IGLSwitch) light);
		putUnitFigure(bs, light);
	}

	public void addLEDStrip(BeanLEDStrips led) {
		GLTorus color = new GLTorus(0.5f, 0.1f, GLFigure.STYLE_PLANE);
		color.mColor[0] = 0.1f;
		color.mColor[1] = color.mColor[2] = 0.8f;
		color.mSize[0] = color.mSize[1] = color.mSize[2] = 1f;
		color.mPosition[0] = led.getX();
		color.mPosition[1] = led.getZ() + 5;
		color.mPosition[2] = -led.getY();
		addFigure(color);
		color.setOnClickListener(new GLUnitClickListener(led));
		color.setOnLongClickListener(new GLUnitLongClickListener(led));
		putUnitFigure(led, color);
	}

	public void addAction(BeanAction action) {
		GLCube cube = new GLCube(GLFigure.STYLE_PLANE);
		if (action.getIconBase64() != null && action.getIconBase64().length() > 0) {
			byte[] data = Base64.decode(action.getIconBase64(), Base64.DEFAULT);
			Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);
			cube.setTexture(bm);
			cube.setColor(1, 1, 1);
		} else
			cube.setColor(0.2f, 0.2f, 0.2f);
		cube.mSize[0] = cube.mSize[1] = cube.mSize[2] = 0.5f;
		cube.mPosition[0] = action.getX();
		cube.mPosition[1] = action.getZ() + 5;
		cube.mPosition[2] = -action.getY();
		addFigure(cube);
		cube.setOnClickListener(new GLUnitClickListener(action));
		cube.setOnLongClickListener(new GLUnitLongClickListener(action));
		putUnitFigure(action, cube);
	}

	private GLFigure createGLMediaServer(String description) {
		if ("multimedia".equals(description)) {
			GLMediaServer glMusic = new GLMediaServer(GLFigure.STYLE_PLANE, true);
			glMusic.setTexture(GLBox.BOX, loadBitmap(R.drawable.textur_holz), 1);
			glMusic.setTexture(GLFlatScreen.BOTTOM, loadBitmap(R.drawable.textur_metal), 1);
			return glMusic;
		}
		if ("remote".equals(description)) {
			GLCube cube = new GLCube(GLFigure.STYLE_PLANE);
			cube.setTexture(loadBitmap(R.drawable.ic_launcher));
			cube.mSize[0] = cube.mSize[1] = cube.mSize[2] = 0.5f;
			cube.setColor(1, 1, 1);
			return cube;
		}
		if ("laptop".equals(description)) {
			GLLaptop laptop = new GLLaptop(GLFigure.STYLE_PLANE, (float) (3 * Math.PI / 4));
			laptop.setTexture(GLLaptop.SURFACE_DISPLAY, loadBitmap(R.drawable.textur_mamor));
			laptop.setTexture(GLLaptop.SURFACE_KEYBOARD, loadBitmap(R.drawable.textur_keyboard));
			return laptop;
		}
		GLBox box = new GLBox(GLFigure.STYLE_PLANE);
		box.setTexture(GLBox.BOX, loadBitmap(R.drawable.textur_holz), 1);
		return box;
	}

	public void addGroundToScene(GroundPlot ground) {
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

			addFigure(glWall);
		}
		mHandler.translateScene[0] = -((maxX - minX) / 2 + minX);
		mHandler.translateScene[1] = ((maxY - minY) / 2 + minY);
		mHandler.zoom = -15;
		mHandler.translateSceneBounds[0] = -minX;
		mHandler.translateSceneBounds[1] = -maxX;
		mHandler.translateSceneBounds[2] = maxY;// -minY;
		mHandler.translateSceneBounds[3] = minY;// -maxY;

		GLSquare laminat = new GLSquare(GLFigure.STYLE_PLANE);
		laminat.mPosition[0] = (maxX - minX) / 2 + minX;
		laminat.mPosition[2] = -(maxY - minY) / 2 - minY;
		laminat.mPosition[1] = -0.03f;
		laminat.mSize[0] = (maxX - minX);
		laminat.mSize[1] = (maxY - minY);
		laminat.mRotation.rotateByAngleAxis(Math.PI / 2, 1, 0, 0);
		laminat.mColor[0] = laminat.mColor[1] = laminat.mColor[2] = 1;
		laminat.setTexture(loadBitmap(R.drawable.textur_wood), laminat.mSize[0], laminat.mSize[1]);
		addFigure(laminat);
	}

	private GLFigure loadGLSwitchByType(String type) {
		if (type.equalsIgnoreCase(LAMP_FLOOR)) {
			GLFloorlamp lamp = new GLFloorlamp(GLFigure.STYLE_PLANE);
			lamp.setTexture(GLFloorlamp.BOTTOM | GLFloorlamp.PILLAR, loadBitmap(R.drawable.textur_mamor));
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
			GLFlatScreen video = new GLFlatScreen(GLFigure.STYLE_PLANE, 1.2f, 0.67f, 2f);
			video.setSwitchTexture(GLFlatScreen.SCREEN, loadBitmap(R.drawable.textur_mamor), true);
			video.setTexture(GLFlatScreen.BOTTOM, loadBitmap(R.drawable.textur_metal));
			return video;
		}
		if (type.equalsIgnoreCase(AUDO)) {
			GLMediaServer audio = new GLMediaServer(GLFigure.STYLE_PLANE, false);
			audio.setTexture(GLBox.BOX, loadBitmap(R.drawable.textur_holz), 1);
			return audio;
		}
		if (type.equalsIgnoreCase(SWITCH_COFFEE)) {
			GLSTLCup coffeeCup;
			try {
				coffeeCup = new GLSTLCup(mContext.getResources().openRawResource(R.raw.cup));
				return coffeeCup;
			} catch (NotFoundException | IOException e) {
			}

		}
		GLLavalamp lamp = new GLLavalamp(20, GLFigure.STYLE_PLANE);
		return lamp;
	}

	@Override
	public synchronized void onDrawFrame(GL10 gl) {
		for (BeanWeb bean : mGLBeanFigureMap.keySet()) {
			GLFigure figure = mGLBeanFigureMap.get(bean);
			if (figure != null) {
				figure.mPosition[1] = (figure.mPosition[1] + bean.getZ()) / 2;
			}
		}
		super.onDrawFrame(gl);
	}

	private synchronized void addFigure(GLFigure figure) {
		mRoom.addFigure(figure);
	}

	private synchronized void putUnitFigure(BeanWeb unit, GLFigure figure) {
		mGLBeanFigureMap.put(unit, figure);
	}

	public synchronized void clearControlCenter() {
		mGLBeanFigureMap.clear();
		mGLMediaServers.clear();
		mGLSwitches.clear();
		mRoom.clear();
	}

	class GLUnitClickListener implements GLClickListener {

		private BeanWeb mBean;

		public GLUnitClickListener(BeanWeb bean) {
			mBean = bean;
		}

		@Override
		public void onGLClick() {
			mControlUnitListener.selectBean(mBean);
		}

	}

	class GLUnitLongClickListener implements GLClickListener {

		private BeanWeb mBean;

		public GLUnitLongClickListener(BeanWeb bean) {
			mBean = bean;
		}

		@Override
		public void onGLClick() {
			mControlUnitListener.selectLongClickBean(mBean);
		}
	}

	class InternetSwitchListener implements GLClickListener {

		private IGLSwitch ligthtObject;
		private BeanSwitch mSwitch;

		public InternetSwitchListener(IGLSwitch ligthtObject, BeanSwitch bs) {
			this.ligthtObject = ligthtObject;
			mSwitch = bs;
		}

		@Override
		public void onGLClick() {
			final boolean lightOn = !ligthtObject.isSwitchOn();
			new Thread() {
				public void run() {
					try {
						IWebSwitch.State state = IWebSwitch.State.OFF;
						if (lightOn)
							state = IWebSwitch.State.ON;
						mWebSwitch.setSwitchState(mSwitch.getID(), state.toString());
					} catch (RemoteException e) {
					}
				};
			}.start();
			ligthtObject.setSwitch(lightOn);
		}

	}

	public void setPlayingBean(String mediaserver, PlayingBean bean) {
		GLFigure glFigure = mGLMediaServers.get(mediaserver);
		if (glFigure instanceof GLMediaServer) {
			Bitmap bitmap = createBitmapByText(getBeanString(bean));
			((GLMediaServer) glFigure).setTexture(GLFlatScreen.SCREEN, bitmap, 1);
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
		Bitmap mirroredBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
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
		IGLSwitch glSwitch = mGLSwitches.get(_switch);
		if (glSwitch != null) {
			glSwitch.setSwitch(state == State.ON);
		}
	}

	public synchronized Set<BeanWeb> getUnits() {
		return mGLBeanFigureMap.keySet();
	}

	public void setConnectionState(boolean available) {
		if (available)
			setGradient(new float[] { 0.3f, 0.3f, 1, 1 }, new float[] { 1, 1, 1, 1 });
		else
			setGradient(new float[] { 1, 0.3f, 0.3f, 1 }, new float[] { 1, 1, 1, 1 });

	}

	class GLSTLCup extends GLSTL implements IGLSwitch {

		boolean mState;

		public GLSTLCup(InputStream stlStream) throws IOException {
			super(stlStream);
			mSize[0] = mSize[1] = mSize[2] = 0.7f;
			mRotation.rotateByAngleAxis(-Math.PI / 2, 1, 0, 0);
			setSwitch(false);
		}

		@Override
		public void setSwitch(boolean on) {
			mState = on;
			if (mState) {
				mColor[0] = 1f;
				mColor[1] = mColor[2] = 0.4f;
			} else {
				mColor[0] = 0.5f;
				mColor[1] = mColor[2] = 0.2f;
			}
		}

		@Override
		public boolean isSwitchOn() {
			return mState;
		}

	}

}
