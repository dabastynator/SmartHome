package de.neo.smarthome.api;

import java.io.IOException;
import java.util.ArrayList;

import de.neo.persist.DaoException;
import de.neo.remote.rmi.RemoteAble;
import de.neo.remote.rmi.RemoteException;
import de.neo.remote.web.WebField;
import de.neo.remote.web.WebParam;
import de.neo.remote.web.WebRequest;

public interface IWebAction extends RemoteAble {

	/**
	 * List all actions with id, running-info and client-action.
	 * 
	 * @return action list
	 * @throws RemoteException
	 */
	@WebRequest(path = "list", description = "List all actions with id, running-info and client-action.", genericClass = BeanAction.class)
	public ArrayList<BeanAction> getActions(@WebParam(name = "token") String token) throws RemoteException;

	/**
	 * Start the action. Throws io exception, if error occur on executing
	 * 
	 * @throws RemoteException
	 * @throws IOException
	 */
	@WebRequest(path = "start_action", description = "Start the action. Throws io exception, if error occur on executing")
	public void startAction(@WebParam(name = "token") String token, @WebParam(name = "id") String id)
			throws RemoteException, IOException;

	/**
	 * Stop current action.
	 * 
	 * @throws RemoteException
	 */
	@WebRequest(path = "stop_action", description = "Stop current action.")
	public void stopAction(@WebParam(name = "token") String token, @WebParam(name = "id") String id) throws RemoteException;

	/**
	 * Create new action
	 * 
	 * @param token
	 * @param id
	 * @param name
	 * @param command
	 * @param clientAction
	 * @param description
	 * @param x
	 * @param y
	 * @param z
	 * @return new action
	 * @throws RemoteException
	 * @throws IOException
	 * @throws DaoException
	 */
	@WebRequest(path = "create", description = "Create new action.")
	public BeanAction create(@WebParam(name = "token") String token, @WebParam(name = "id") String id,
			@WebParam(name = "name") String name, @WebParam(name = "command") String command,
			@WebParam(name = "client_action") String clientAction, @WebParam(name = "description") String description,
			@WebParam(name = "x") float x, @WebParam(name = "y") float y, @WebParam(name = "z") float z)
			throws RemoteException, IOException, DaoException;

	/**
	 * Update existing action
	 * 
	 * @param token
	 * @param id
	 * @param name
	 * @param command
	 * @param clientAction
	 * @param description
	 * @param x
	 * @param y
	 * @param z
	 * @return updated action
	 * @throws RemoteException
	 * @throws IOException
	 * @throws DaoException
	 */
	@WebRequest(path = "update", description = "Update existing action.")
	public BeanAction update(@WebParam(name = "token") String token, @WebParam(name = "id") String id,
			@WebParam(name = "name") String name, @WebParam(name = "command") String command,
			@WebParam(name = "client_action") String clientAction, @WebParam(name = "description") String description,
			@WebParam(name = "x") float x, @WebParam(name = "y") float y, @WebParam(name = "z") float z)
			throws RemoteException, IOException, DaoException;

	/**
	 * Delete action
	 * 
	 * @param token
	 * @param id
	 * @throws RemoteException
	 * @throws IOException
	 * @throws DaoException
	 */
	@WebRequest(path = "delete", description = "Delete action.")
	public void delete(@WebParam(name = "token") String token, @WebParam(name = "id") String id)
			throws RemoteException, IOException, DaoException;

	public class BeanAction extends BeanWeb {

		@WebField(name = "running")
		private boolean mRunning;

		@WebField(name = "client_action")
		private String mClientAction;

		@WebField(name = "icon_base64")
		private String mIconBase64;

		@WebField(name = "command")
		private String mCommand;

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

		public String getCommand() {
			return mCommand;
		}

		public void setCommand(String command) {
			mCommand = command;
		}

	}

}
