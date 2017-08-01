package de.neo.smarthome.gpio;

import java.util.ArrayList;
import java.util.List;

import de.neo.remote.rmi.RemoteException;
import de.neo.remote.web.WebGet;
import de.neo.remote.web.WebProxyBuilder;
import de.neo.remote.web.WebRequest;
import de.neo.smarthome.AbstractUnitHandler;
import de.neo.smarthome.api.IWebSwitch;
import de.neo.smarthome.controlcenter.IControlCenter;
import de.neo.smarthome.controlcenter.IControllUnit;

public class WebSwitchImpl extends AbstractUnitHandler implements IWebSwitch {

	public WebSwitchImpl(IControlCenter center) {
		super(center);
	}

	@Override
	@WebRequest(path = "list", description = "List all switches of the controlcenter. A switch has an id, name, state and type.", genericClass = BeanSwitch.class)
	public ArrayList<BeanSwitch> getSwitches() {
		ArrayList<BeanSwitch> result = new ArrayList<>();
		for (IControllUnit unit : mCenter.getControlUnits().values()) {
			try {
				if (unit.getControllObject() instanceof InternetSwitch) {
					InternetSwitch switchObject = (InternetSwitch) unit.getControllObject();
					BeanSwitch webSwitch = new BeanSwitch();
					webSwitch.merge(unit.getWebBean());
					webSwitch.setID(unit.getID());
					webSwitch.setName(unit.getName());
					webSwitch.setState(switchObject.getState());
					webSwitch.setType(switchObject.getType());
					result.add(webSwitch);
				}
			} catch (RemoteException e) {
			}
		}
		return result;
	}

	public static void main(String[] args) {
		IWebSwitch webSwitch = new WebProxyBuilder().setEndPoint("http://192.168.2.11:5061/switch")
				.setSecurityToken("w4kzd4HQx").setInterface(IWebSwitch.class).create();
		List<BeanSwitch> switches;
		try {
			switches = webSwitch.getSwitches();
			System.out.println(switches.size());
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		if (unit.getControllObject() instanceof InternetSwitch) {
			InternetSwitch switchObject = (InternetSwitch) unit.getControllObject();
			switchObject.setState(switchState);
			BeanSwitch webSwitch = new BeanSwitch();
			webSwitch.merge(unit.getWebBean());
			webSwitch.setID(unit.getID());
			webSwitch.setName(unit.getName());
			webSwitch.setState(switchObject.getState());
			webSwitch.setType(switchObject.getType());
			return webSwitch;
		}
		return null;
	}

	@Override
	public String getWebPath() {
		return "switch";
	}

}
