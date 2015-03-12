package de.neo.remote.controlcenter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.neo.remote.RemoteLogger;
import de.neo.remote.api.GroundPlot;
import de.neo.remote.api.GroundPlot.Feature;
import de.neo.remote.api.GroundPlot.Point;
import de.neo.remote.api.GroundPlot.Wall;
import de.neo.remote.api.IControlCenter;
import de.neo.remote.api.IControlUnit;
import de.neo.rmi.api.RMILogger.LogPriority;
import de.neo.rmi.protokol.RemoteException;

/**
 * Implement the control center interface.
 * 
 * @author sebastian
 */
public class ControlCenterImpl extends Thread implements IControlCenter {

	public static String ROOT = "GroundPlot";

	/**
	 * List of all control units
	 */
	private Map<String, IControlUnit> mControlUnits = Collections
			.synchronizedMap(new HashMap<String, IControlUnit>());

	/**
	 * The ground plot of the control center area
	 */
	private GroundPlot mGround;

	public ControlCenterImpl(Node root) {
		mGround = readGround((Element) root);
		start();
	}

	private GroundPlot readGround(Element root) {
		GroundPlot ground = new GroundPlot();
		NodeList walls = root.getElementsByTagName("Wall");
		for (int i = 0; i < walls.getLength(); i++) {
			Element wallElement = (Element) walls.item(i);
			Wall wall = new GroundPlot.Wall();
			NodeList points = wallElement.getElementsByTagName("Point");
			for (int j = 0; j < points.getLength(); j++) {
				Point point = new GroundPlot.Point();
				Element pointElement = (Element) points.item(j);
				point.x = Float.parseFloat(pointElement.getAttribute("x"));
				point.y = Float.parseFloat(pointElement.getAttribute("y"));
				point.z = Float.parseFloat(pointElement.getAttribute("z"));
				wall.points.add(point);
			}
			ground.walls.add(wall);
		}
		NodeList features = root.getElementsByTagName("Feature");
		for (int i = 0; i < features.getLength(); i++) {

			// read basics
			Element featureElement = (Element) features.item(i);
			Feature feature = new Feature();
			feature.x = Float.parseFloat(featureElement.getAttribute("x"));
			feature.y = Float.parseFloat(featureElement.getAttribute("y"));
			feature.z = Float.parseFloat(featureElement.getAttribute("z"));
			feature.az = Float.parseFloat(featureElement.getAttribute("az"));
			feature.type = featureElement.getAttribute("type");
			if (featureElement.hasAttribute("extra"))
				feature.extra = featureElement.getAttribute("extra");

			ground.features.add(feature);
		}
		return ground;
	}

	@Override
	public void run() {
		while (true) {
			checkControlUnits();
			try {
				Thread.sleep(100 * 10);
			} catch (InterruptedException e) {
			}
		}
	}

	/**
	 * remove control units with exception
	 */
	private void checkControlUnits() {
		int removed = 0;
		for (String id : mControlUnits.keySet()) {
			try {
				IControlUnit unit = mControlUnits.get(id);
				unit.getID();
			} catch (RemoteException e) {
				mControlUnits.remove(id);
				removed++;
			}
		}
		if (removed > 0)
			RemoteLogger.performLog(LogPriority.WARNING, "Lost " + removed
					+ " unit(s). " + mControlUnits.size() + " unit(s) left.",
					"Controlcenter");
	}

	@Override
	public void addControlUnit(IControlUnit controlUnit) throws RemoteException {
		try {
			String id = controlUnit.getID();
			mControlUnits.put(id, controlUnit);
			RemoteLogger.performLog(LogPriority.INFORMATION,
					"Add " + mControlUnits.size() + ". control unit: "
							+ controlUnit.getName() + " (" + id + ")",
					"Controlcenter");
		} catch (RemoteException e) {

		}
	}

	@Override
	public void removeControlUnit(IControlUnit controlUnit)
			throws RemoteException {
		mControlUnits.remove(controlUnit);
	}

	@Override
	public GroundPlot getGroundPlot() throws RemoteException {
		return mGround;
	}

	@Override
	public String[] getControlUnitIDs() throws RemoteException {
		return mControlUnits.keySet().toArray(new String[mControlUnits.size()]);
	}

	@Override
	public IControlUnit getControlUnit(String id) throws RemoteException {
		return mControlUnits.get(id);
	}

}
