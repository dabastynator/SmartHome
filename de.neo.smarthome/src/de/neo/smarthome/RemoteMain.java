package de.neo.smarthome;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.neo.remote.rmi.RMILogger;
import de.neo.remote.rmi.RMILogger.LogPriority;
import de.neo.remote.rmi.RMILogger.RMILogListener;
import de.neo.remote.web.WebServer;
import de.neo.smarthome.RemoteLogger.RemoteLogListener;
import de.neo.smarthome.action.ActionUnitFactory;
import de.neo.smarthome.api.IControlCenter;
import de.neo.smarthome.api.IControllUnit;
import de.neo.smarthome.api.Trigger;
import de.neo.smarthome.controlcenter.ControlCenterImpl;
import de.neo.smarthome.gpio.GPIOUnitFactory;
import de.neo.smarthome.mediaserver.MediaUnitFactory;
import de.neo.smarthome.rccolor.RCColorUnitFactory;

public class RemoteMain {

	public static String WEBSERVER = "WebServer";
	public static String WEBSERVER_PORT = "port";
	public static String WEBSERVER_TOKEN = "token";
	public static String WEBSERVER_PATH = "controlcenter";

	public static final SimpleDateFormat LogFormat = new SimpleDateFormat("dd.MM-HH:mm");

	public static Map<String, ControlUnitFactory> mControlUnitFactory;

	static {
		mControlUnitFactory = new HashMap<String, ControlUnitFactory>();
		mControlUnitFactory.put("InternetSwitch", new GPIOUnitFactory());
		mControlUnitFactory.put("CommandAction", new ActionUnitFactory());
		mControlUnitFactory.put("MediaServer", new MediaUnitFactory());
		mControlUnitFactory.put("ColorSetter", new RCColorUnitFactory());
	}

	public static void main(String args[]) {
		checkArgs(args);
		File config = new File(getParameter("--config", args));
		if (!config.exists() || !config.isFile()) {
			System.err.println("Configuration file does not exist.");
			printUsage();
			System.exit(1);
		}
		setupLoging(System.out);
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(config);
			doc.getDocumentElement().normalize();

			ControlCenterImpl controlcenter = loadControlCenter(doc);
			List<IControllUnit> units = loadControlUnits(doc, controlcenter);

			for (IControllUnit unit : units)
				controlcenter.addControlUnit(unit);

			for (Trigger trigger : readStartupTrigger(doc.getElementsByTagName("StartTrigger"))) {
				controlcenter.trigger(trigger);
			}
		} catch (ParserConfigurationException | SAXException | IOException | IllegalArgumentException e) {
			System.err.println("Error parsing configuration: " + e.getMessage());
			System.exit(1);
		}
	}

	private static void setupLoging(final PrintStream stream) {
		RMILogger.addLogListener(new RMILogListener() {
			@Override
			public void rmiLog(LogPriority priority, String message, String id, long date) {
				stream.println(LogFormat.format(new Date()) + " RMI " + priority + " by " + message + " (" + id + ")");
			}
		});
		RemoteLogger.mListeners.add(new RemoteLogListener() {
			@Override
			public void remoteLog(LogPriority priority, String message, String object, long date) {
				stream.println(LogFormat.format(new Date()) + " REMOTE " + priority + " by " + object + ": " + message);
			}
		});
	}

	private static List<IControllUnit> loadControlUnits(Document doc, IControlCenter center)
			throws SAXException, IOException {
		List<IControllUnit> units = new ArrayList<IControllUnit>();
		for (String unitName : mControlUnitFactory.keySet()) {
			NodeList nodeList = doc.getElementsByTagName(unitName);
			ControlUnitFactory factory = mControlUnitFactory.get(unitName);
			for (int i = 0; i < nodeList.getLength(); i++) {
				Element element = (Element) nodeList.item(i);
				AbstractControlUnit unit = factory.createControlUnit(center);
				unit.initialize(element);
				units.add(unit);
			}
		}
		return units;
	}

	private static ControlCenterImpl loadControlCenter(Document doc) throws SAXException, IOException {
		NodeList controlCenterRoot = doc.getElementsByTagName(ControlCenterImpl.ROOT);
		NodeList webServerRoot = doc.getElementsByTagName(WEBSERVER);
		ControlCenterImpl center = new ControlCenterImpl();
		if (controlCenterRoot != null && controlCenterRoot.getLength() > 0)
			center.initializeGroundPlot(controlCenterRoot.item(0));
		center.initializeRules(doc.getElementsByTagName("EventRule"));
		center.initializeTrigger(doc.getElementsByTagName("TimeTrigger"));
		WebServer webServer = WebServer.getInstance();
		if (webServerRoot != null && webServerRoot.getLength() > 0) {
			Element webRoot = (Element) webServerRoot.item(0);
			if (webRoot.hasAttribute(WEBSERVER_PORT) && webRoot.hasAttribute(WEBSERVER_TOKEN)) {
				webServer.setPort(Integer.valueOf(webRoot.getAttribute(WEBSERVER_PORT)));
				for (ControlUnitFactory factory : mControlUnitFactory.values()) {
					AbstractUnitHandler handler = factory.createUnitHandler(center);
					webServer.handle(handler.getWebPath(), handler, webRoot.getAttribute(WEBSERVER_TOKEN));
				}
				webServer.handle(WEBSERVER_PATH, center, webRoot.getAttribute(WEBSERVER_TOKEN));
				webServer.start();
			}
		}
		return center;
	}

	private static void checkArgs(String[] args) {
		for (String str : args) {
			if (str.equals("--help") || str.equals("-h")) {
				printUsage();
				System.exit(1);
			}
		}
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

	private static void printUsage() {
		System.out.println("Usage:  ");
		System.out.println("  --help     -h : print usage.");
		System.out.println("  --config      : configuration xml file.");
	}

	public static List<Trigger> readStartupTrigger(NodeList nodeList) throws SAXException {
		List<Trigger> triggers = new ArrayList<Trigger>();
		for (int i = 0; i < nodeList.getLength(); i++)
			if (nodeList.item(i) instanceof Element) {
				Element element = (Element) nodeList.item(i);
				for (int j = 0; j < element.getChildNodes().getLength(); j++)
					if (element.getChildNodes().item(j) instanceof Element) {
						Element trigger = (Element) element.getChildNodes().item(j);
						Trigger t = new Trigger();
						t.initialize(trigger);
						triggers.add(t);
					}
			}
		return triggers;
	}

}
