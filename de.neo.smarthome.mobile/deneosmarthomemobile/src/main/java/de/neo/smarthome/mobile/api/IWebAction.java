package de.neo.smarthome.mobile.api;

import java.io.IOException;
import java.util.ArrayList;

import de.neo.remote.rmi.RemoteAble;
import de.neo.remote.rmi.RemoteException;
import de.neo.remote.web.WebField;
import de.neo.remote.web.WebGet;
import de.neo.remote.web.WebRequest;
import de.neo.smarthome.mobile.api.IControlCenter;

public interface IWebAction extends RemoteAble {

	/**
	 * List all actions with id, running-info and client-action.
	 * 
	 * @return action list
	 */
	@WebRequest(path = "list", description = "List all actions with id, running-info and client-action.", genericClass = BeanAction.class)
	public ArrayList<BeanAction> getActions();

	/**
	 * Start the action. Throws io exception, if error occur on executing
	 * 
	 * @throws RemoteException
	 * @throws IOException
	 */
	@WebRequest(path = "start_action", description = "Start the action. Throws io exception, if error occur on executing")
	public void startAction(@WebGet(name = "id") String id) throws RemoteException, IOException;

	/**
	 * Stop current action.
	 * 
	 * @throws RemoteException
	 */
	@WebRequest(path = "stop_action", description = "Stop current action.")
	public void stopAction(@WebGet(name = "id") String id) throws RemoteException;

	public class BeanAction extends IControlCenter.BeanWeb {

		@WebField(name = "running")
		private boolean mRunning;

		@WebField(name = "client_action")
		private String mClientAction;

		@WebField(name = "icon_base64")
		private String mIconBase64;

		public boolean isRunning() {
			return mRunning;
		}

		public void setRunning(boolean running) {
			mRunning = running;
		}

		public String getClientAction() {
			return mClientAction;
		}

		public void setClientAction(String clientAction) {
			mClientAction = clientAction;
		}

		public String getIconBase64() {
			return mIconBase64;
		}

		public void setIconBase64(String iconBase64) {
			mIconBase64 = iconBase64;
		}

	}

}
