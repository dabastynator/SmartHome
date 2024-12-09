package de.neo.smarthome.scenes;

import java.util.ArrayList;

import de.neo.remote.rmi.RemoteException;
import de.neo.remote.web.WebParam;
import de.neo.remote.web.WebProxyBuilder;
import de.neo.remote.web.WebRequest;
import de.neo.smarthome.AbstractUnitHandler;
import de.neo.smarthome.api.BeanWeb;
import de.neo.smarthome.api.IWebScenes;
import de.neo.smarthome.api.IWebScenes.BeanScene;
import de.neo.smarthome.controlcenter.ControlCenter;
import de.neo.smarthome.switches.HassAPI;
import de.neo.smarthome.user.User;
import de.neo.smarthome.user.UserSessionHandler;

public class WebSceneImpl extends AbstractUnitHandler implements IWebScenes{

	private static WebSceneImpl mSingleton;
	
	public static WebSceneImpl getSingleton()
	{
		return mSingleton;
	}
	
	private HassAPI mHassAPI;
	private String mAuth;
	private ArrayList<BeanScene> mScenes;

	public WebSceneImpl(ControlCenter center) {
		super(center);
		mSingleton = this;
	}

	private HassAPI getHassAPI() {
		if (mHassAPI == null) {
			mHassAPI = new WebProxyBuilder().setEndPoint(mCenter.getHassUrl()).setInterface(HassAPI.class).create();
			mAuth = "Bearer " + mCenter.getHassToken();
		}
		return mHassAPI;
	}

	@Override
	@WebRequest(path = "list", description = "List all scenes of the controlcenter. A scene has an id and name.", genericClass = BeanScene.class)
	public ArrayList<BeanScene> getScenes(
			@WebParam(name = "token") String token) throws RemoteException
	{
		UserSessionHandler.require(token);
		return mScenes;
	}

	@Override
	@WebRequest(path = "activate", description = "Activate a given scene by specified id.")
	public void activateScene(
			@WebParam(name = "token") String token, 
			@WebParam(name = "id") String id) throws RemoteException
	{
		UserSessionHandler.require(token);
		getHassAPI().setState(mAuth, id, "scene", "on");
	}
	
	public void setScenes(ArrayList<BeanScene> scenes)
	{
		mScenes = scenes;
	}

	@Override
	public String getWebPath() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
