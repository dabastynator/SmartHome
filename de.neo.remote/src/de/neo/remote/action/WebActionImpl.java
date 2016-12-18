package de.neo.remote.action;

import java.io.IOException;
import java.util.ArrayList;

import de.neo.remote.AbstractUnitHandler;
import de.neo.remote.api.ICommandAction;
import de.neo.remote.api.IControlCenter;
import de.neo.remote.api.IControlUnit;
import de.neo.remote.api.IWebAction;
import de.neo.rmi.api.WebGet;
import de.neo.rmi.api.WebRequest;
import de.neo.rmi.protokol.RemoteException;

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
