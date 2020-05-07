package de.neo.smarthome.gpio;

import java.io.IOException;
import java.util.ArrayList;

import de.neo.persist.Dao;
import de.neo.persist.DaoException;
import de.neo.persist.DaoFactory;
import de.neo.remote.rmi.RemoteException;
import de.neo.remote.rmi.RMILogger.LogPriority;
import de.neo.remote.web.WebGet;
import de.neo.remote.web.WebRequest;
import de.neo.smarthome.AbstractUnitHandler;
import de.neo.smarthome.RemoteLogger;
import de.neo.smarthome.SmartHome.ControlUnitFactory;
import de.neo.smarthome.api.IControlCenter;
import de.neo.smarthome.api.IControllUnit;
import de.neo.smarthome.api.IWebSwitch;
import de.neo.smarthome.controlcenter.ControlCenter;
import de.neo.smarthome.user.User;
import de.neo.smarthome.user.UserSessionHandler;
import de.neo.smarthome.user.User.UserRole;

public class WebSwitchImpl extends AbstractUnitHandler implements IWebSwitch {

	public WebSwitchImpl(IControlCenter center) {
		super(center);
	}

	private BeanSwitch toBean(GPIOControlUnit unit) {
		BeanSwitch webSwitch = new BeanSwitch();
		webSwitch.merge(unit.getWebBean());
		webSwitch.setState(unit.getState());
		webSwitch.setType(unit.getDescription());
		return webSwitch;
	}

	@Override
	@WebRequest(path = "list", description = "List all switches of the controlcenter. A switch has an id, name, state and type.", genericClass = BeanSwitch.class)
	public ArrayList<BeanSwitch> getSwitches(@WebGet(name = "token") String token) throws RemoteException {
		User user = UserSessionHandler.require(token);
		ArrayList<BeanSwitch> result = new ArrayList<>();
		for (IControllUnit unit : mCenter.getAccessHandler().unitsFor(user)) {
			if (unit instanceof GPIOControlUnit) {
				GPIOControlUnit switchUnit = (GPIOControlUnit) unit;
				BeanSwitch webSwitch = new BeanSwitch();
				webSwitch.merge(unit.getWebBean());
				webSwitch.setID(unit.getID());
				webSwitch.setName(unit.getName());
				webSwitch.setState(switchUnit.getState());
				webSwitch.setType(switchUnit.getDescription());
				result.add(webSwitch);
			}
		}
		return result;
	}

	@Override
	@WebRequest(description = "Set the state of switch with specified id. State must be [ON|OFF].", path = "set")
	public BeanSwitch setSwitchState(@WebGet(name = "token") String token, @WebGet(name = "id") String id,
			@WebGet(name = "state") String state) throws IllegalArgumentException, RemoteException {
		GPIOControlUnit switchUnit = mCenter.getAccessHandler().require(token, id);
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
	public BeanSwitch create(@WebGet(name = "token") String token, @WebGet(name = "id") String id,
			@WebGet(name = "name") String name, @WebGet(name = "family_code") String familyCode,
			@WebGet(name = "switch_number") int switchNumber, @WebGet(name = "description") String description,
			@WebGet(name = "x") float x, @WebGet(name = "y") float y, @WebGet(name = "z") float z)
			throws RemoteException, IOException, DaoException {
		UserSessionHandler.require(token, UserRole.ADMIN);
		GPIOControlUnit unit = new GPIOControlUnit();
		if (mCenter.getControlUnit(id) != null) {
			throw new RemoteException("Unit with id " + id + " already exists");
		}
		unit.setName(name);
		unit.setDescription(description);
		unit.setPosition(x, y, z);
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
	public BeanSwitch update(@WebGet(name = "token") String token, @WebGet(name = "id") String id,
			@WebGet(name = "name") String name, @WebGet(name = "family_code") String familyCode,
			@WebGet(name = "switch_number") int switchNumber, @WebGet(name = "description") String description,
			@WebGet(name = "x") float x, @WebGet(name = "y") float y, @WebGet(name = "z") float z)
			throws RemoteException, IOException, DaoException {
		UserSessionHandler.require(token, UserRole.ADMIN);
		IControllUnit unit = mCenter.getControlUnit(id);
		if (!(unit instanceof GPIOControlUnit)) {
			throw new RemoteException("Unknown switch " + id);
		}
		GPIOControlUnit switchUnit = (GPIOControlUnit) unit;
		switchUnit.setName(name);
		switchUnit.setDescription(description);
		switchUnit.setPosition(x, y, z);
		switchUnit.setFamilyCode(familyCode);
		switchUnit.setSwitchNumber(switchNumber);
		Dao<GPIOControlUnit> dao = DaoFactory.getInstance().getDao(GPIOControlUnit.class);
		dao.update(switchUnit);
		RemoteLogger.performLog(LogPriority.INFORMATION, "Update existing switch " + switchUnit.getName(), "WebSwitch");
		return toBean(switchUnit);

	}

	@WebRequest(path = "delete", description = "Delete switch.")
	public void delete(@WebGet(name = "token") String token, @WebGet(name = "id") String id)
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

	public static class SwitchFactory implements ControlUnitFactory {

		@Override
		public Class<?> getUnitClass() {
			return GPIOControlUnit.class;
		}

		@Override
		public AbstractUnitHandler createUnitHandler(ControlCenter center) {
			return new WebSwitchImpl(center);
		}

	}
}
