package de.neo.smarthome.action;

import java.io.IOException;
import java.util.ArrayList;

import de.neo.persist.Dao;
import de.neo.persist.DaoException;
import de.neo.persist.DaoFactory;
import de.neo.remote.rmi.RMILogger.LogPriority;
import de.neo.remote.rmi.RemoteException;
import de.neo.remote.web.WebGet;
import de.neo.remote.web.WebRequest;
import de.neo.smarthome.AbstractUnitHandler;
import de.neo.smarthome.RemoteLogger;
import de.neo.smarthome.SmartHome.ControlUnitFactory;
import de.neo.smarthome.api.IControlCenter;
import de.neo.smarthome.api.IControllUnit;
import de.neo.smarthome.api.IWebAction;
import de.neo.smarthome.controlcenter.ControlCenter;
import de.neo.smarthome.user.User;
import de.neo.smarthome.user.User.UserRole;
import de.neo.smarthome.user.UserSessionHandler;

public class WebActionImpl extends AbstractUnitHandler implements IWebAction {

	public WebActionImpl(IControlCenter center) {
		super(center);
	}

	private BeanAction toBean(ActionControlUnit unit) {
		BeanAction webAction = new BeanAction();
		webAction.merge(unit.getWebBean());
		webAction.setClientAction(unit.getClientAction());
		webAction.setRunning(unit.isRunning());
		webAction.setIconBase64(unit.getIconBase64());
		webAction.setCommand(unit.getCommand());
		return webAction;
	}

	@Override
	@WebRequest(path = "list", description = "List all actions with id, running-info and client-action.", genericClass = BeanAction.class)
	public ArrayList<BeanAction> getActions(@WebGet(name = "token") String token) throws RemoteException {
		User user = UserSessionHandler.require(token);
		ArrayList<BeanAction> result = new ArrayList<>();
		for (IControllUnit unit : mCenter.getAccessHandler().unitsFor(user)) {
			if (unit instanceof ActionControlUnit) {
				ActionControlUnit action = (ActionControlUnit) unit;
				result.add(toBean(action));
			}
		}
		return result;
	}

	@WebRequest(path = "start_action", description = "Start the action. Throws io exception, if error occur on executing")
	public void startAction(@WebGet(name = "token") String token, @WebGet(name = "id") String id)
			throws RemoteException, IOException {
		ActionControlUnit action = mCenter.getAccessHandler().require(token, id);
		action.startAction();
	}

	@WebRequest(path = "stop_action", description = "Stop current action.")
	public void stopAction(@WebGet(name = "token") String token, @WebGet(name = "id") String id)
			throws RemoteException {
		ActionControlUnit action = mCenter.getAccessHandler().require(token, id);
		action.stopAction();
	}

	@WebRequest(path = "create", description = "Create new action.")
	public BeanAction create(@WebGet(name = "token") String token, @WebGet(name = "id") String id,
			@WebGet(name = "name") String name, @WebGet(name = "command") String command,
			@WebGet(name = "client_action") String clientAction, @WebGet(name = "description") String description,
			@WebGet(name = "x") float x, @WebGet(name = "y") float y, @WebGet(name = "z") float z)
			throws RemoteException, IOException, DaoException {
		UserSessionHandler.require(token, UserRole.ADMIN);
		ActionControlUnit unit = new ActionControlUnit();
		if (mCenter.getControlUnit(id) != null) {
			throw new RemoteException("Unit with id " + id + " already exists");
		}
		unit.setName(name);
		unit.setDescription(description);
		unit.setPosition(x, y, z);
		unit.setId(id);
		unit.setCommand(command);
		unit.setClientAction(clientAction);
		Dao<ActionControlUnit> dao = DaoFactory.getInstance().getDao(ActionControlUnit.class);
		dao.save(unit);
		mCenter.addControlUnit(unit);
		RemoteLogger.performLog(LogPriority.INFORMATION, "Create new a action " + unit.getName(), "WebAction");
		return toBean(unit);
	}

	@WebRequest(path = "update", description = "Update existing action.")
	public BeanAction update(@WebGet(name = "token") String token, @WebGet(name = "id") String id,
			@WebGet(name = "name") String name, @WebGet(name = "command") String command,
			@WebGet(name = "client_action") String clientAction, @WebGet(name = "description") String description,
			@WebGet(name = "x") float x, @WebGet(name = "y") float y, @WebGet(name = "z") float z)
			throws RemoteException, IOException, DaoException {
		UserSessionHandler.require(token, UserRole.ADMIN);
		IControllUnit unit = mCenter.getControlUnit(id);
		if (!(unit instanceof ActionControlUnit)) {
			throw new RemoteException("Unknown action " + id);
		}
		ActionControlUnit actionUnit = (ActionControlUnit) unit;
		actionUnit.setName(name);
		actionUnit.setDescription(description);
		actionUnit.setPosition(x, y, z);
		actionUnit.setCommand(command);
		actionUnit.setClientAction(clientAction);
		Dao<ActionControlUnit> dao = DaoFactory.getInstance().getDao(ActionControlUnit.class);
		dao.update(actionUnit);
		RemoteLogger.performLog(LogPriority.INFORMATION, "Update existing action " + actionUnit.getName(), "WebAction");
		return toBean(actionUnit);
	}

	@WebRequest(path = "delete", description = "Delete action.")
	public void delete(@WebGet(name = "token") String token, @WebGet(name = "id") String id)
			throws RemoteException, IOException, DaoException {
		UserSessionHandler.require(token, UserRole.ADMIN);
		IControllUnit unit = mCenter.getControlUnit(id);
		if (!(unit instanceof ActionControlUnit)) {
			throw new RemoteException("Unknown action " + id);
		}
		ActionControlUnit actionUnit = (ActionControlUnit) unit;
		Dao<ActionControlUnit> dao = DaoFactory.getInstance().getDao(ActionControlUnit.class);
		dao.delete(actionUnit);
		mCenter.removeControlUnit(actionUnit);
		RemoteLogger.performLog(LogPriority.INFORMATION, "Remove action " + actionUnit.getName(), "WebAction");
	}

	@Override
	public String getWebPath() {
		return "action";
	}

	public static class ActionFactory implements ControlUnitFactory {

		@Override
		public Class<?> getUnitClass() {
			return ActionControlUnit.class;
		}

		@Override
		public AbstractUnitHandler createUnitHandler(ControlCenter center) {
			return new WebActionImpl(center);
		}

	}

}
