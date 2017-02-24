package de.neo.smarthome.action;

import java.io.IOException;
import java.util.ArrayList;

import de.neo.remote.rmi.RemoteException;
import de.neo.remote.web.WebGet;
import de.neo.remote.web.WebRequest;
import de.neo.smarthome.AbstractUnitHandler;
import de.neo.smarthome.api.ICommandAction;
import de.neo.smarthome.api.IControlCenter;
import de.neo.smarthome.api.IControlUnit;
import de.neo.smarthome.api.IWebAction;
import de.neo.smarthome.api.IWebAction.BeanAction;

public class WebActionImpl extends AbstractUnitHandler implements IWebAction {

	public WebActionImpl(IControlCenter center) {
		super(center);
	}

	@Override
	@WebRequest(path = "list", description = "List all actions with id, running-info and client-action.", genericClass = BeanAction.class)
	public ArrayList<BeanAction> getActions() {
		ArrayList<BeanAction> result = new ArrayList<>();
		for (IControlUnit unit : mCenter.getControlUnits().values()) {
			try {
				if (unit.getRemoteableControlObject() instanceof ICommandAction) {
					ICommandAction action = (ICommandAction) unit.getRemoteableControlObject();
					BeanAction webAction = new BeanAction();
					webAction.merge(unit.getWebBean());
					webAction.setClientAction(action.getClientAction());
					webAction.setID(unit.getID());
					webAction.setRunning(action.isRunning());
					webAction.setIconBase64(action.getIconBase64());
					result.add(webAction);
				}
			} catch (RemoteException e) {
			}
		}
		return result;
	}

	@WebRequest(path = "start_action", description = "Start the action. Throws io exception, if error occur on executing")
	public void startAction(@WebGet(name = "id") String id) throws RemoteException, IOException {
		IControlUnit unit = mCenter.getControlUnit(id);
		if (unit != null && unit.getRemoteableControlObject() instanceof ICommandAction) {
			ICommandAction action = (ICommandAction) unit.getRemoteableControlObject();
			action.startAction();
		}
	}

	@WebRequest(path = "stop_action", description = "Stop current action.")
	public void stopAction(@WebGet(name = "id") String id) throws RemoteException {
		IControlUnit unit = mCenter.getControlUnit(id);
		if (unit != null && unit.getRemoteableControlObject() instanceof ICommandAction) {
			ICommandAction action = (ICommandAction) unit.getRemoteableControlObject();
			action.stopAction();
		}
	}

	@Override
	public String getWebPath() {
		return "action";
	}

}
