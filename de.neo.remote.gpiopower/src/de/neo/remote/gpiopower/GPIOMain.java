package de.neo.remote.gpiopower;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.neo.remote.controlcenter.api.IControlCenter;
import de.neo.remote.gpiopower.api.IInternetSwitch;
import de.neo.rmi.api.RMILogger;
import de.neo.rmi.api.RMILogger.LogPriority;
import de.neo.rmi.api.RMILogger.RMILogListener;
import de.neo.rmi.api.Server;
import de.neo.rmi.protokol.RemoteException;

public class GPIOMain {

	public static void main(String[] args) {
		try {
			SwitchPower switchPower = new SwitchPower();
			RMILogger.addLogListener(new RMILogListener() {
				@Override
				public void rmiLog(LogPriority priority, String message,
						String id, long date) {
					System.out.println(priority.name() + ": " + message);
					System.out.flush();
				}
			});
			String registry = getParameter("--registry", args);
			Server server = Server.getServer();
			server.forceConnectToRegistry(registry);
			server.startServer(IInternetSwitch.PORT);
			IControlCenter control = (IControlCenter) server.forceFind(
					IControlCenter.ID, IControlCenter.class);
			if (control == null)
				throw new RemoteException(IControlCenter.ID,
						"not found in registry");

			String configFile = getParameter("--config", args);
			for (InternetSwitchImpl remoteSwitch : loadSwitchesFromFile(
					configFile, switchPower)) {
				GPIOControlUnit unit = new GPIOControlUnit(
						remoteSwitch.getName(), "Internet switch",
						remoteSwitch, remoteSwitch.getPosition());
				control.addControlUnit(unit);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static List<InternetSwitchImpl> loadSwitchesFromFile(
			String configFile, SwitchPower power) throws IOException,
			SAXException, ParserConfigurationException {
		List<InternetSwitchImpl> switchList = new ArrayList<InternetSwitchImpl>();
		File file = new File(configFile);
		if (!file.exists())
			throw new IOException("Config file does not exist.");
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Document doc = factory.newDocumentBuilder().parse(file);
		doc.getDocumentElement().normalize();
		NodeList switches = doc.getElementsByTagName("Switch");
		for (int i = 0; i < switches.getLength(); i++) {
			Element switchElement = (Element) switches.item(i);
			float[] position = new float[3];
			try {
				String familyCode = switchElement.getAttribute("familyCode");
				int switchNumber = Integer.parseInt(switchElement
						.getAttribute("switchNumber"));
				String name = switchElement.getAttribute("name");
				position[0] = Float.parseFloat(switchElement.getAttribute("x"));
				position[1] = Float.parseFloat(switchElement.getAttribute("y"));
				position[2] = Float.parseFloat(switchElement.getAttribute("z"));
				String type = switchElement.getAttribute("type");
				InternetSwitchImpl remoteSwitch = new InternetSwitchImpl(name,
						power, familyCode, switchNumber, type, position);
				switchList.add(remoteSwitch);
			} catch (Exception e) {
				System.err.println("Error reading switch "
						+ switchElement.toString() + ": " + e.getMessage());
				printUsage();
				System.exit(1);
			}
		}
		return switchList;
	}

	private static String getParameter(String string, String[] args) {
		for (int i = 0; i < args.length - 1; i++) {
			String arg = args[i];
			if (arg.equals(string))
				return args[i + 1];
		}
		System.err.println("Error: Parameter " + string + " missing");
		printUsage();
		System.exit(0);
		return null;
	}

	private static boolean hasParameter(String string, String[] args) {
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if (arg.equals(string))
				return true;
		}
		return false;
	}

	private static float[] getPosition(String[] args) {
		String sPos = getParameter("--position", args);
		try {
			String[] split = sPos.split(",");
			float[] pos = new float[3];
			pos[0] = Float.parseFloat(split[0]);
			pos[1] = Float.parseFloat(split[1]);
			pos[2] = Float.parseFloat(split[2]);
			return pos;
		} catch (Exception e) {
			System.err.println("Error reading position: " + e.getMessage());
			printUsage();
			System.exit(1);
		}
		return null;
	}

	private static void printUsage() {
		System.out.println("Usage:  ");
		System.out.println("  --registry    : ip of registry.");
		System.out.println("  --config      : configuration file.");
	}

}
