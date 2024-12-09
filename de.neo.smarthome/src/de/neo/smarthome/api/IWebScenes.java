package de.neo.smarthome.api;

import java.util.ArrayList;

import de.neo.remote.rmi.RemoteAble;
import de.neo.remote.rmi.RemoteException;
import de.neo.remote.web.WebParam;
import de.neo.remote.web.WebRequest;

public interface IWebScenes extends RemoteAble {

	@WebRequest(path = "list", description = "List all scenes of the controlcenter. A scene has an id and name.", genericClass = BeanScene.class)
	public ArrayList<BeanScene> getScenes(
			@WebParam(name = "token") String token)
					throws RemoteException;

	@WebRequest(path = "activate", description = "Activate a given scene by specified id.")
	public void activateScene(
			@WebParam(name = "token") String token, 
			@WebParam(name = "id") String id)
					throws IllegalArgumentException, RemoteException;

	public static class BeanScene extends BeanWeb {

	}

}
