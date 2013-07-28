package de.remote.controlcenter.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.newsystem.rmi.protokol.RemoteException;
import de.remote.controlcenter.api.GroundPlot;
import de.remote.controlcenter.api.GroundPlot.Feature;
import de.remote.controlcenter.api.GroundPlot.Point;
import de.remote.controlcenter.api.GroundPlot.Wall;
import de.remote.controlcenter.api.IControlCenter;
import de.remote.controlcenter.api.IControlUnit;

/**
 * Implement the control center interface.
 * 
 * @author sebastian
 */
public class ControlCenterImpl implements IControlCenter {

	/**
	 * List of all control units
	 */
	private List<IControlUnit> controlUnits = new ArrayList<IControlUnit>();

	/**
	 * The ground plot of the control center area
	 */
	private GroundPlot ground;

	/**
	 * allocate new control center. it checks every 5 minutes all control units
	 * and removes units with exception.
	 * 
	 * @param config
	 */
	public ControlCenterImpl(String config) {
		Thread thread = new Thread() {
			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(1000 * 60 * 5);
						checkControlUnits();
					} catch (InterruptedException e) {
					}
				}
			}
		};
		thread.start();
		File file = new File(config);
		if (!file.exists() || !file.isFile())
			throw new IllegalArgumentException(
					"The configuration file must exist and be a file: "
							+ config);
		ground = readGround(config);
	}

	private GroundPlot readGround(String config) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		GroundPlot ground = new GroundPlot();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(config);
			doc.getDocumentElement().normalize();
			NodeList walls = doc.getElementsByTagName("Wall");
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
			NodeList features = doc.getElementsByTagName("Feature");
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
		} catch (ParserConfigurationException e) {
			System.err.println("Error on reading config file: "
					+ e.getMessage());
		} catch (SAXException e) {
			System.err.println("Error on reading config file: "
					+ e.getMessage());
		} catch (IOException e) {
			System.err.println("Error on reading config file: "
					+ e.getMessage());
		}
		return ground;
	}

	/**
	 * remove control units wit exception
	 */
	private void checkControlUnits() {
		List<IControlUnit> exceptionList = new ArrayList<IControlUnit>();
		for (IControlUnit unit : controlUnits) {
			try {
				unit.getName();
			} catch (RemoteException e) {
				exceptionList.add(unit);
			}
		}
		controlUnits.removeAll(exceptionList);
	}

	@Override
	public int getControlUnitNumber() throws RemoteException {
		return controlUnits.size();
	}

	@Override
	public void addControlUnit(IControlUnit controlUnit) throws RemoteException {
		controlUnits.add(controlUnit);
		System.out.println("Add control unit: " + controlUnit.getName());
	}

	@Override
	public void removeControlUnit(IControlUnit controlUnit)
			throws RemoteException {
		controlUnits.remove(controlUnit);
	}

	@Override
	public IControlUnit getControlUnit(int number) throws RemoteException {
		return controlUnits.get(number);
	}

	@Override
	public GroundPlot getGroundPlot() throws RemoteException {
		return ground;
	}

}
