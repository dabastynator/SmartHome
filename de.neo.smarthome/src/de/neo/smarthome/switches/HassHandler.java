package de.neo.smarthome.switches;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.neo.remote.rmi.RMILogger.LogPriority;
import de.neo.remote.rmi.RemoteException;
import de.neo.remote.web.WebProxyBuilder;
import de.neo.smarthome.RemoteLogger;
import de.neo.smarthome.api.IControllUnit;
import de.neo.smarthome.controlcenter.ControlCenter;
import de.neo.smarthome.switches.HassAPI.HassEntity;

public class HassHandler implements Runnable {

	public static int RefreshIntervalMS = 2000;
	
	public static void initialize(ControlCenter center){
		HassHandler handler = new HassHandler(center);
		handler.startThread();
	}
	
	private ControlCenter mCenter;
	private HassAPI mHassAPI;
	private String mAuth;

	public HassHandler(ControlCenter center) {
		mCenter = center;
		mHassAPI = new WebProxyBuilder().setEndPoint(mCenter.getHassUrl()).setInterface(HassAPI.class).create();
		mAuth = "Bearer " + mCenter.getHassToken();
	}
	
	@Override
	public void run() {
		while(true) {
			try {
				Thread.sleep(RefreshIntervalMS);
				try {
					Map<String, HassSwitchUnit> hassSwitches = new HashMap<String, HassSwitchUnit>();
					for (IControllUnit unit: mCenter.getControlUnits().values()) {
						if(unit instanceof HassSwitchUnit) {
							HassSwitchUnit hassSwitch =  (HassSwitchUnit)unit;
							hassSwitches.put(hassSwitch.getEntityId(), hassSwitch);
						}
					}
					
					ArrayList<HassEntity> entities = mHassAPI.getEntities(mAuth);
					for (HassEntity e: entities) {
						HassSwitchUnit hassSwitch = hassSwitches.get(e.id);
						if(hassSwitch != null) {
							hassSwitch.setStateIntern(e.state);
						}
					}
				} catch (RemoteException e) {
					RemoteLogger.performLog(LogPriority.ERROR, "Home assistent refresh failed: " + e.getMessage(), "HassHandler");
				}
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void startThread() {
		new Thread(this).start();
	}
	
}
