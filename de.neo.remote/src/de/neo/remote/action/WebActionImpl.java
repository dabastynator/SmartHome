package de.neo.remote.action;

import java.util.ArrayList;
import java.util.List;

import de.neo.remote.AbstractUnitHandler;
import de.neo.remote.api.ICommandAction;
import de.neo.remote.api.IControlCenter;
import de.neo.remote.api.IControlUnit;
import de.neo.remote.api.IWebAction;
import de.neo.rmi.api.WebRequest;
import de.neo.rmi.protokol.RemoteException;

public class WebActionImpl extends AbstractUnitHandler implements IWebAction {

	public WebActionImpl(IControlCenter center) {
		super(center);
	}

	@Override
	@WebRequest(path = "list", description = "List all actions with id, running-info and client-action.")
	public List<BeanAction> getActions() {
		List<BeanAction> result = new ArrayList<>();
		for (IControlUnit unit : mCenter.getControlUnits().values()) {
			try {
				if (unit.getRemoteableControlObject() instanceof ICommandAction) {
					ICommandAction action = (ICommandAction) unit.getRemoteableControlObject();
					BeanAction webAction = new BeanAction();
					unit.config(webAction);
					webAction.setClientAction(action.getClientAction());
					webAction.setID(unit.getID());
					webAction.setRunning(action.isRunning());
					result.add(webAction);
				}
			} catch (RemoteException e) {
			}
		}
		return result;
	}

	@Override
	public String getWebPath() {
		return "action";
	}

}