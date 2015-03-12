package de.neo.remote;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
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

import de.neo.remote.RemoteLogger.RemoteLogListener;
import de.neo.remote.api.IControlCenter;
import de.neo.remote.api.IControlUnit;
import de.neo.remote.api.IMediaServer;
import de.neo.remote.controlcenter.ControlCenterImpl;
import de.neo.remote.gpio.GPIOControlUnit;
import de.neo.remote.gpio.InternetSwitchImpl;
import de.neo.remote.gpio.SwitchPower;
import de.neo.remote.mediaserver.MediaControlUnit;
import de.neo.remote.mediaserver.MediaServerImpl;
import de.neo.rmi.api.IRegistryConnection;
import de.neo.rmi.api.RMILogger;
import de.neo.rmi.api.RMILogger.LogPriority;
import de.neo.rmi.api.RMILogger.RMILogListener;
import de.neo.rmi.api.Server;
import de.neo.rmi.protokol.RemoteException;

public class RemoteMain {

	public static String REGISTRY_IP = "RegistryIp";
	public static String SERVER_PORT = "ServerPort";

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
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(config);
			doc.getDocumentElement().normalize();
			NodeList registryList = doc.getElementsByTagName(REGISTRY_IP);
			if (registryList == null || registryList.getLength() == 0)
				throw new SAXException("No registry ip in xml: " + REGISTRY_IP);
			String registryIp = registryList.item(0).getTextContent();

			NodeList serverPortNode = doc.getElementsByTagName(SERVER_PORT);
			int serverPort = IControlCenter.PORT;
			if (serverPortNode != null && serverPortNode.getLength() > 0)
				serverPort = Integer.parseInt(serverPortNode.item(0)
						.getTextContent());

			IControlCenter controlcenter = loadControlCenter(doc);
			List<IControlUnit> units = loadControlUnits(doc);

			Server server = Server.getServer();
			server.startServer(serverPort);

			ControlCenterRegistryConnection connector = new ControlCenterRegistryConnection(
					controlcenter, units);
			server.manageConnector(connector, registryIp);

		} catch (ParserConfigurationException | SAXException | IOException
				| IllegalArgumentException e) {
			System.err
					.println("Error parsing configuration: " + e.getMessage());
			printXMLSchema();
			System.exit(1);
		}
	}

	private static void setupLoging(final PrintStream stream) {
		RMILogger.addLogListener(new RMILogListener() {
			@Override
			public void rmiLog(LogPriority priority, String message, String id,
					long date) {
				stream.println("RMI " + priority + ": " + message + " (" + id
						+ ")");
			}
		});
		RemoteLogger.mListeners.add(new RemoteLogListener() {
			@Override
			public void remoteLog(LogPriority priority, String message,
					String object, long date) {
				stream.println("REMOTE " + priority + " by " + object + ": "
						+ message);
			}
		});
	}

	private static List<IControlUnit> loadControlUnits(Document doc)
			throws SAXException, IOException {
		List<IControlUnit> units = new ArrayList<IControlUnit>();
		NodeList switches = doc.getElementsByTagName(InternetSwitchImpl.ROOT);
		SwitchPower power = new SwitchPower();
		for (int i = 0; i < switches.getLength(); i++) {
			Node item = switches.item(i);
			if (item instanceof Element) {
				Element element = (Element) item;
				if (!element.hasAttribute("id"))
					throw new SAXException("id missing for internet switch");
				String id = element.getAttribute("id");
				InternetSwitchImpl internetSwitch = new InternetSwitchImpl(
						element, power);
				GPIOControlUnit unit = new GPIOControlUnit(id,
						internetSwitch.getName(), "Internet switch",
						internetSwitch, internetSwitch.getPosition());
				units.add(unit);
			}
		}

		NodeList mediaServer = doc.getElementsByTagName(MediaServerImpl.ROOT);
		for (int i = 0; i < mediaServer.getLength(); i++) {
			Node item = mediaServer.item(i);
			if (item instanceof Element) {
				Element element = (Element) item;
				for (String attribute : new String[] { "id", "location",
						"name", "type", "playlistLocation", "x", "y", "z" })
					if (!element.hasAttribute(attribute))
						throw new SAXException(attribute
								+ " missing for mediaserver");
				String location = element.getAttribute("location");
				String plsLocation = element.getAttribute("playlistLocation");
				String name = element.getAttribute("name");
				String type = element.getAttribute("type");
				String id = element.getAttribute("id");
				boolean thumbnailWorker = true;
				if (element.hasAttribute("thumbnailWorker")) {
					String worker = element.getAttribute("thumbnailWorker");
					thumbnailWorker = worker.equals("true")
							|| worker.equals("1");
				}
				float[] position = {
						Float.parseFloat(element.getAttribute("x")),
						Float.parseFloat(element.getAttribute("y")),
						Float.parseFloat(element.getAttribute("z")) };
				IMediaServer media = new MediaServerImpl(location, plsLocation,
						thumbnailWorker);
				MediaControlUnit unit = new MediaControlUnit(id, name, media,
						position, type);
				units.add(unit);
			}
		}
		return units;
	}

	private static IControlCenter loadControlCenter(Document doc) {
		NodeList controlCenterRoot = doc
				.getElementsByTagName(ControlCenterImpl.ROOT);
		if (controlCenterRoot != null && controlCenterRoot.getLength() > 0) {
			return new ControlCenterImpl(controlCenterRoot.item(0));
		}
		return null;
	}

	private static void checkArgs(String[] args) {
		for (String str : args) {
			if (str.equals("--help") || str.equals("-h")) {
				printUsage();
				System.exit(1);
			}
			if (str.equals("--xmlhelp")) {
				printXMLSchema();
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
		System.out.println("  --registry    : ip of the registry.");
		System.out.println("  --config      : configuration xml file.");
		System.out.println("  --xmlhelp     : print xml schema.");
	}

	private static void printXMLSchema() {
		System.out.println("<?xml version=\"1.0\"?>");
		System.out
				.println("<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">");
		System.out.println("<xs:element name=\"ControlCenter\">");
		System.out.println("<xs:complexType>");
		System.out.println("<xs:sequence>");
		System.out.println("  <xs:element name=\"" + REGISTRY_IP
				+ "\" type=\"xs:string\"/>");
		System.out.println("  <xs:element name=\"" + SERVER_PORT
				+ "\" type=\"xs:string\"/>");
		System.out.println("  <xs:element name=\"" + ControlCenterImpl.ROOT
				+ "\">");
		System.out.println("    <xs:complexType>");
		System.out.println("    <xs:sequence>");
		System.out.println("      <xs:element name=\"Wall\">");
		System.out.println("        <xs:complexType>");
		System.out.println("        <xs:sequence>");
		System.out.println("          <xs:element name=\"Point\">");
		System.out
				.println("             <xs:attribute name=\"x\" type=\"xs:double\"/>");
		System.out
				.println("             <xs:attribute name=\"y\" type=\"xs:double\"/>");
		System.out
				.println("             <xs:attribute name=\"z\" type=\"xs:double\"/>");
		System.out.println("          </xs:element>");
		System.out.println("        </xs:sequence>");
		System.out.println("        </xs:complexType>");
		System.out.println("      </xs:element>");
		System.out.println("    </xs:sequence>");
		System.out.println("    </xs:complexType>");
		System.out.println("  </xs:element>");

		System.out.println("  <xs:sequence>");
		System.out.println("    <xs:element name=\"" + InternetSwitchImpl.ROOT
				+ "\">");
		System.out
				.println("      <xs:attribute name=\"familyCode\" type=\"xs:integer\"/>");
		System.out
				.println("      <xs:attribute name=\"switchNumber\" type=\"xs:integer\"/>");
		System.out
				.println("      <xs:attribute name=\"name\" type=\"xs:string\"/>");
		System.out
				.println("      <xs:attribute name=\"type\" type=\"xs:string\"/>");
		System.out
				.println("      <xs:attribute name=\"x\" type=\"xs:double\"/>");
		System.out
				.println("      <xs:attribute name=\"y\" type=\"xs:double\"/>");
		System.out
				.println("      <xs:attribute name=\"z\" type=\"xs:double\"/>");
		System.out.println("    </xs:element>");
		System.out.println("  </xs:sequence>");

		System.out.println("  <xs:sequence>");
		System.out.println("    <xs:element name=\"" + MediaServerImpl.ROOT
				+ "\">");
		System.out
				.println("      <xs:attribute name=\"location\" type=\"xs:string\"/>");
		System.out
				.println("      <xs:attribute name=\"thumbnailWorker\" type=\"xs:string\"/>");
		System.out
				.println("      <xs:attribute name=\"playlistLocation\" type=\"xs:string\"/>");
		System.out
				.println("      <xs:attribute name=\"name\" type=\"xs:string\"/>");
		System.out
				.println("      <xs:attribute name=\"type\" type=\"xs:string\"/>");
		System.out
				.println("      <xs:attribute name=\"x\" type=\"xs:double\"/>");
		System.out
				.println("      <xs:attribute name=\"y\" type=\"xs:double\"/>");
		System.out
				.println("      <xs:attribute name=\"z\" type=\"xs:double\"/>");
		System.out.println("    </xs:element>");
		System.out.println("  </xs:sequence>");

		System.out.println("</xs:sequence>");
		System.out.println("</xs:complexType>");
		System.out.println("</xs:element>");
		System.out.println("</xs:schema>");
	}

	public static class ControlCenterRegistryConnection implements
			IRegistryConnection {

		private IControlCenter m_controlcenter;
		List<IControlUnit> m_units;

		public ControlCenterRegistryConnection(IControlCenter controlcenter,
				List<IControlUnit> units) {
			m_controlcenter = controlcenter;
			if (controlcenter != null && units != null) {
				try {
					for (IControlUnit unit : units)
						controlcenter.addControlUnit(unit);
				} catch (RemoteException e) {
				}
			} else {
				m_units = units;
			}
		}

		@Override
		public void onRegistryConnected(Server server) {
			try {
				IControlCenter controlcenter = m_controlcenter;
				if (controlcenter == null) {
					controlcenter = server.forceFind(IControlCenter.ID,
							IControlCenter.class);
				} else {
					server.register(IControlCenter.ID, m_controlcenter);
				}
				if (m_units != null)
					for (IControlUnit unit : m_units)
						controlcenter.addControlUnit(unit);
			} catch (RemoteException e) {
				RemoteLogger.performLog(LogPriority.ERROR, e.getMessage(),
						"RemoteMain");
			}
		}

		@Override
		public void onRegistryLost() {
			// TODO Auto-generated method stub

		}

		@Override
		public boolean isManaged() {
			return true;
		}

	}

}
