package de.neo.smarthome.gpio;

import java.util.ArrayList;

import de.neo.remote.rmi.RemoteException;
import de.neo.remote.web.WebGet;
import de.neo.remote.web.WebRequest;
import de.neo.smarthome.AbstractUnitHandler;
import de.neo.smarthome.SmartHome.ControlUnitFactory;
import de.neo.smarthome.api.IControlCenter;
import de.neo.smarthome.api.IControllUnit;
import de.neo.smarthome.api.IWebSwitch;
import de.neo.smarthome.controlcenter.ControlCenter;
import de.neo.smarthome.user.User;
import de.neo.smarthome.user.UserSessionHandler;

public class WebSwitchImpl extends AbstractUnitHandler implements IWebSwitch {

	public WebSwitchImpl(IControlCenter center) {
		super(center);
	}

	@Override
	@WebRequest(path = "list", description = "List all switches of the controlcenter. A switch has an id, name, state and type.", genericClass = BeanSwitch.class)
	public ArrayList<BeanSwitch> getSwitches(@WebGet(name = "token") String token) throws RemoteException {
		User user = UserSessionHandler.getSingleton().require(token);
		ArrayList<BeanSwitch> result = new ArrayList<>();
		for (IControllUnit unit : mCenter.getAccessHandler().getUnitsFor(user)) {
			try {
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
			} catch (RemoteException e) {
			}
		}
		return result;
	}

	@Override
	@WebRequest(description = "Set the state of switch with specified id. State must be [ON|OFF].", path = "set")
	public BeanSwitch setSwitchState(@WebGet(name = "id") String id, @WebGet(name = "state") String state)
			throws IllegalArgumentException, RemoteException {
		IControllUnit unit = mCenter.getControlUnits().get(id);
		State switchState = null;
		try {
			switchState = State.valueOf(state);
		} catch (Exception e) {
			throw new IllegalArgumentException("Could not read state value: " + state);
		}
		if (unit instanceof GPIOControlUnit) {
			GPIOControlUnit switchUnit = (GPIOControlUnit) unit;
			switchUnit.setState(switchState);
			BeanSwitch webSwitch = new BeanSwitch();
			webSwitch.merge(unit.getWebBean());
			webSwitch.setID(unit.getID());
			webSwitch.setName(unit.getName());
			webSwitch.setState(switchUnit.getState());
			webSwitch.setType(switchUnit.getDescription());
			return webSwitch;
		}
		return null;
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
