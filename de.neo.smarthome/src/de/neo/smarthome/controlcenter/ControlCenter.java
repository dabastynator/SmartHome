package de.neo.smarthome.controlcenter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.neo.persist.annotations.Domain;
import de.neo.persist.annotations.OnLoad;
import de.neo.persist.annotations.Persist;
import de.neo.remote.rmi.RMILogger.LogPriority;
import de.neo.remote.rmi.RemoteException;
import de.neo.smarthome.RemoteLogger;
import de.neo.smarthome.api.IControllUnit;
import de.neo.smarthome.user.UnitAccessHandler;

/**
 * Implement the control center interface.
 * 
 * @author sebastian
 */
@Domain
public class ControlCenter {

	@Persist(name = "port")
	private int mPort;
	
	@Persist(name = "hassToken")
	private String mHassToken;
	
	@Persist(name = "hassUrl")
	private String mHassUrl;

	/**
	 * List of all control units
	 */
	private Map<String, IControllUnit> mControlUnits = Collections
			.synchronizedMap(new HashMap<String, IControllUnit>());
	
	private List<IControllUnit> mControlUnitList = Collections
			.synchronizedList(new ArrayList<IControllUnit>());

	private UnitAccessHandler mAccessHandler = new UnitAccessHandler(this);

	@OnLoad
	public void onLoad() {
		
	}

	public void addControlUnit(IControllUnit controlUnit) {
		try {
			controlUnit.setControlCenter(this);
			String id = controlUnit.getID();
			mControlUnits.put(id, controlUnit);
			mControlUnitList.add(controlUnit);
			RemoteLogger.performLog(LogPriority.INFORMATION,
					"Add " + mControlUnits.size() + ". control unit: " + controlUnit.getName() + " (" + id + ")",
					"Controlcenter");
		} catch (RemoteException e) {
			RemoteLogger.performLog(LogPriority.ERROR, "Could not add control unit: " + e.getMessage(), "");
		}
	}

	public void removeControlUnit(IControllUnit controlUnit) {
		try {
			mControlUnits.remove(controlUnit.getID());
			mControlUnitList.remove(controlUnit);
		} catch (RemoteException e) {
			RemoteLogger.performLog(LogPriority.ERROR, "Could not remove control unit: " + e.getMessage(), "");

		}
	}

	public IControllUnit getControlUnit(String id) {
		return mControlUnits.get(id);
	}

	public Map<String, IControllUnit> getControlUnits() {
		return mControlUnits;
	}

	public UnitAccessHandler getAccessHandler() {
		return mAccessHandler;
	}

	public int getPort() {
		return mPort;
	}
	
	public String getHassToken() {
		return mHassToken;
	}
	
	public String getHassUrl() {
		return mHassUrl;
	}

	public void onPostLoad() {
		mAccessHandler.initialize();
	}

	public ArrayList<IControllUnit> getControlUnitList() {
		return new ArrayList<>(mControlUnitList);
	}

}
