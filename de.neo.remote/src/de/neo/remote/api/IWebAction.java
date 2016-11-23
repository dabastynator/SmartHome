package de.neo.remote.api;

import java.util.ArrayList;

import de.neo.remote.api.IControlCenter.BeanWeb;
import de.neo.rmi.api.WebField;
import de.neo.rmi.api.WebRequest;
import de.neo.rmi.protokol.RemoteAble;

public interface IWebAction extends RemoteAble {

	/**
	 * List all actions with id, running-info and client-action.
	 * 
	 * @return action list
	 */
	@WebRequest(path = "list", description = "List all actions with id, running-info and client-action.")
	public ArrayList<BeanAction> getActions();

	public class BeanAction extends BeanWeb {

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
