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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.newsystem.rmi.protokol.RemoteException;
import de.remote.controlcenter.api.GroundPlot;
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
				try {
					Thread.sleep(1000 * 60 * 5);
				} catch (InterruptedException e) {
				}
				checkControlUnits();
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

				// read basics
				Element wallElement = (Element) walls.item(i);
				Wall wall = new GroundPlot.Wall();
				wall.x1 = Float.parseFloat(wallElement.getAttribute("x1"));
				wall.x2 = Float.parseFloat(wallElement.getAttribute("x2"));
				wall.y1 = Float.parseFloat(wallElement.getAttribute("y1"));
				wall.y2 = Float.parseFloat(wallElement.getAttribute("y2"));
				wall.depth = Float
						.parseFloat(wallElement.getAttribute("depth"));
				wall.height = Float.parseFloat(wallElement
						.getAttribute("height"));

				// read door
				NodeList doors = wallElement.getElementsByTagName("Door");
				if (doors.getLength() > 0) {
					Element doorElement = (Element) doors.item(0);
					wall.door_distance = Float.parseFloat(doorElement
							.getAttribute("distance"));
					wall.door_width = Float.parseFloat(doorElement
							.getAttribute("width"));
					wall.door_height = Float.parseFloat(doorElement
							.getAttribute("height"));
				}

				ground.walls.add(wall);
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
