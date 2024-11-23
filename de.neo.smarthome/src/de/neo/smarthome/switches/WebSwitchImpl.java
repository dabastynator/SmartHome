package de.neo.smarthome.switches;

import java.io.IOException;
import java.util.ArrayList;

import de.neo.persist.Dao;
import de.neo.persist.DaoException;
import de.neo.persist.DaoFactory;
import de.neo.remote.rmi.RMILogger.LogPriority;
import de.neo.remote.rmi.RemoteException;
import de.neo.remote.web.WebParam;
import de.neo.remote.web.WebRequest;
import de.neo.smarthome.AbstractControlUnit;
import de.neo.smarthome.AbstractUnitHandler;
import de.neo.smarthome.RemoteLogger;
import de.neo.smarthome.SmartHome.ControlUnitFactory;
import de.neo.smarthome.api.IControllUnit;
import de.neo.smarthome.api.IWebSwitch;
import de.neo.smarthome.controlcenter.ControlCenter;
import de.neo.smarthome.user.User;
import de.neo.smarthome.user.User.UserRole;
import de.neo.smarthome.user.UserSessionHandler;

public class WebSwitchImpl extends AbstractUnitHandler implements IWebSwitch {

	public WebSwitchImpl(ControlCenter center) {
		super(center);
	}

	private BeanSwitch toBean(SwitchUnit unit) {
		BeanSwitch webSwitch = new BeanSwitch();
		webSwitch.merge(unit.getWebBean());
		webSwitch.setState(unit.getState());
		return webSwitch;
	}

	@Override
	@WebRequest(path = "list", description = "List all switches of the controlcenter. A switch has an id, name, state and type.", genericClass = BeanSwitch.class)
	public ArrayList<BeanSwitch> getSwitches(@WebParam(name = "token") String token) throws RemoteException {
		User user = UserSessionHandler.require(token);
		ArrayList<BeanSwitch> result = new ArrayList<>();
		for (IControllUnit unit : mCenter.getAccessHandler().unitsFor(user)) {
			if (unit instanceof SwitchUnit) {
				SwitchUnit switchUnit = (SwitchUnit) unit;
				BeanSwitch webSwitch = new BeanSwitch();
				webSwitch.merge(unit.getWebBean());
				webSwitch.setID(unit.getID());
				webSwitch.setName(unit.getName());
				webSwitch.setState(switchUnit.getState());
				result.add(webSwitch);
			}
		}
		return result;
	}

	@Override
	@WebRequest(description = "Set the state of switch with specified id. State must be [ON|OFF].", path = "set")
	public BeanSwitch setSwitchState(@WebParam(name = "token") String token, @WebParam(name = "id") String id,
			@WebParam(name = "state") String state) throws IllegalArgumentException, RemoteException {
		SwitchUnit switchUnit = mCenter.getAccessHandler().require(token, id);
		State switchState = null;
		try {
			switchState = State.valueOf(state);
		} catch (Exception e) {
			throw new IllegalArgumentException("Could not read state value: " + state);
		}
		switchUnit.setState(switchState);
		return toBean(switchUnit);
	}

	@WebRequest(path = "create", description = "Create new switch.")
	public BeanSwitch create(@WebParam(name = "token") String token, @WebParam(name = "id") String id,
			@WebParam(name = "name") String name, @WebParam(name = "family_code") String familyCode,
			@WebParam(name = "switch_number") int switchNumber, @WebParam(name = "description") String description,
			@WebParam(name = "x") float x, @WebParam(name = "y") float y, @WebParam(name = "z") float z)
			throws RemoteException, IOException, DaoException {
		UserSessionHandler.require(token, UserRole.ADMIN);
		GPIOControlUnit unit = new GPIOControlUnit();
		if (mCenter.getControlUnit(id) != null) {
			throw new RemoteException("Unit with id " + id + " already exists");
		}
		unit.setName(name);
		unit.setId(id);
		unit.setFamilyCode(familyCode);
		unit.setSwitchNumber(switchNumber);
		Dao<GPIOControlUnit> dao = DaoFactory.getInstance().getDao(GPIOControlUnit.class);
		dao.save(unit);
		mCenter.addControlUnit(unit);
		RemoteLogger.performLog(LogPriority.INFORMATION, "Create new switch " + unit.getName(), "WebSwitch");
		return toBean(unit);
	}

	@WebRequest(path = "update", description = "Update existing switch.")
	public BeanSwitch update(@WebParam(name = "token") String token, @WebParam(name = "id") String id,
			@WebParam(name = "name") String name, @WebParam(name = "family_code") String familyCode,
			@WebParam(name = "switch_number") int switchNumber, @WebParam(name = "description") String description,
			@WebParam(name = "x") float x, @WebParam(name = "y") float y, @WebParam(name = "z") float z)
			throws RemoteException, IOException, DaoException {
		UserSessionHandler.require(token, UserRole.ADMIN);
		IControllUnit unit = mCenter.getControlUnit(id);
		if (!(unit instanceof GPIOControlUnit)) {
			throw new RemoteException("Unknown switch " + id);
		}
		GPIOControlUnit switchUnit = (GPIOControlUnit) unit;
		switchUnit.setName(name);
		switchUnit.setFamilyCode(familyCode);
		switchUnit.setSwitchNumber(switchNumber);
		Dao<GPIOControlUnit> dao = DaoFactory.getInstance().getDao(GPIOControlUnit.class);
		dao.update(switchUnit);
		RemoteLogger.performLog(LogPriority.INFORMATION, "Update existing switch " + switchUnit.getName(), "WebSwitch");
		return toBean(switchUnit);

	}

	@WebRequest(path = "delete", description = "Delete switch.")
	public void delete(@WebParam(name = "token") String token, @WebParam(name = "id") String id)
			throws RemoteException, IOException, DaoException {
		UserSessionHandler.require(token, UserRole.ADMIN);
		IControllUnit unit = mCenter.getControlUnit(id);
		if (!(unit instanceof GPIOControlUnit)) {
			throw new RemoteException("Unknown switch " + id);
		}
		GPIOControlUnit switchUnit = (GPIOControlUnit) unit;
		Dao<GPIOControlUnit> dao = DaoFactory.getInstance().getDao(GPIOControlUnit.class);
		dao.delete(switchUnit);
		mCenter.removeControlUnit(switchUnit);
		RemoteLogger.performLog(LogPriority.INFORMATION, "Remove switch " + switchUnit.getName(), "WebSwitch");
	}

	@Override
	public String getWebPath() {
		return "switch";
	}

	public static class GPIOFactory implements ControlUnitFactory {

		@Override
		public Class<?> getUnitClass() {
			return GPIOControlUnit.class;
		}

		@Override
		public AbstractUnitHandler createUnitHandler(ControlCenter center) {
			return new WebSwitchImpl(center);
		}

	}
	
	public static class HassSwitchFactory implements ControlUnitFactory {

		@Override
		public Class<?> getUnitClass() {
			return HassSwitchUnit.class;
		}

		@Override
		public AbstractUnitHandler createUnitHandler(ControlCenter center) {
			return new WebSwitchImpl(center);
		}

	}
	
	public static abstract class SwitchUnit extends AbstractControlUnit{
		
		abstract public void setState(final State state) throws RemoteException;
		
		abstract public State getState();
		
	}
}
