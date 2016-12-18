package de.neo.remote.gpio;

import java.util.ArrayList;
import java.util.List;

import de.neo.remote.AbstractUnitHandler;
import de.neo.remote.api.IControlCenter;
import de.neo.remote.api.IControlUnit;
import de.neo.remote.api.IInternetSwitch;
import de.neo.remote.api.IWebSwitch;
import de.neo.rmi.api.WebGet;
import de.neo.rmi.api.WebProxyBuilder;
import de.neo.rmi.api.WebRequest;
import de.neo.rmi.protokol.RemoteException;

public class WebSwitchImpl extends AbstractUnitHandler implements IWebSwitch {

	public WebSwitchImpl(IControlCenter center) {
		super(center);
	}

	@Override
	@WebRequest(path = "list", description = "List all switches of the controlcenter. A switch has an id, name, state and type.", genericClass = BeanSwitch.class)
	public ArrayList<BeanSwitch> getSwitches() {
		ArrayList<BeanSwitch> result = new ArrayList<>();
		for (IControlUnit unit : mCenter.getControlUnits().values()) {
			try {
				if (unit.getRemoteableControlObject() instanceof IInternetSwitch) {
					IInternetSwitch switchObject = (IInternetSwitch) unit.getRemoteableControlObject();
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
		IWebSwitch webSwitch = new WebProxyBuilder().setEndPoint("http://localhost:5061/switch")
				.setSecurityToken("w4kzd4HQ").setInterface(IWebSwitch.class).create();
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
		IControlUnit unit = mCenter.getControlUnits().get(id);
		State switchState = null;
		try {
			switchState = State.valueOf(state);
		} catch (Exception e) {
			throw new IllegalArgumentException("Could not read state value: " + state);
		}
		if (unit.getRemoteableControlObject() instanceof IInternetSwitch) {
			IInternetSwitch switchObject = (IInternetSwitch) unit.getRemoteableControlObject();
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
