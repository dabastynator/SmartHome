package de.neo.smarthome;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.neo.persist.Dao;
import de.neo.persist.DaoException;
import de.neo.persist.DaoFactory;
import de.neo.persist.xml.XMLDao;
import de.neo.persist.xml.XMLDaoFactory;
import de.neo.persist.xml.XMLDaoFactory.XMLFactoryBuilder;
import de.neo.remote.rmi.RMILogger;
import de.neo.remote.rmi.RMILogger.LogPriority;
import de.neo.remote.rmi.RMILogger.RMILogListener;
import de.neo.remote.web.WebServer;
import de.neo.smarthome.RemoteLogger.RemoteLogListener;
import de.neo.smarthome.action.ActionControlUnit;
import de.neo.smarthome.action.WebActionImpl;
import de.neo.smarthome.api.IControllUnit;
import de.neo.smarthome.controlcenter.ControlCenter;
import de.neo.smarthome.informations.InformationHass;
import de.neo.smarthome.informations.InformationUnit.InformationTrigger;
import de.neo.smarthome.informations.InformationWeather;
import de.neo.smarthome.informations.WebInformation;
import de.neo.smarthome.mediaserver.MediaControlUnit;
import de.neo.smarthome.mediaserver.WebMediaServerImpl;
import de.neo.smarthome.rccolor.RCColorControlUnit;
import de.neo.smarthome.rccolor.WebLEDStripImpl;
import de.neo.smarthome.scenes.WebSceneImpl;
import de.neo.smarthome.switches.GPIOControlUnit;
import de.neo.smarthome.switches.HassHandler;
import de.neo.smarthome.switches.HassSwitchUnit;
import de.neo.smarthome.switches.WebSwitchImpl;
import de.neo.smarthome.user.UnitAccess;
import de.neo.smarthome.user.User;
import de.neo.smarthome.user.UserSessionHandler.UserSession;
import de.neo.smarthome.user.WebUser;

public class SmartHome {

	public static String WEBSERVER_PATH = "controlcenter";
	public static String INFORMATION_PATH = "information";
	public static String SCENE_PATH = "scene";
	private static List<ControlUnitFactory> mControlUnitFactory = new ArrayList<>();

	public static final SimpleDateFormat LogFormat = new SimpleDateFormat("dd.MM-HH:mm");

	static {
		mControlUnitFactory.add(new WebMediaServerImpl.MediaFactory());
		mControlUnitFactory.add(new WebSwitchImpl.GPIOFactory());
		mControlUnitFactory.add(new WebSwitchImpl.HassSwitchFactory());
		mControlUnitFactory.add(new WebActionImpl.ActionFactory());
		mControlUnitFactory.add(new WebLEDStripImpl.LEDStripFactory());
		mControlUnitFactory.add(new WebUser.UserFactory());
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
			setupXMLDao(config);
			ControlCenter controlcenter = loadControlCenter();
			List<IControllUnit> units = loadControlUnits();

			for (IControllUnit unit : units)
				controlcenter.addControlUnit(unit);

			controlcenter.onPostLoad();
			
			HassHandler.initialize(controlcenter);
		} catch (IOException | IllegalArgumentException | DaoException e) {
			System.err.println(
					"Error parsing configuration: " + e.getMessage() + " (" + e.getClass().getSimpleName() + ")");
			System.exit(1);
		}
	}

	private static void setupXMLDao(File config) throws DaoException {
		XMLFactoryBuilder builder = new XMLDaoFactory.XMLFactoryBuilder();
		builder.registerDao(new XMLDao<ControlCenter>(ControlCenter.class));
		builder.registerDao(new XMLDao<User>(User.class));
		builder.registerDao(new XMLDao<UserSession>(UserSession.class));
		builder.registerDao(new XMLDao<UnitAccess>(UnitAccess.class));

		builder.registerDao(new XMLDao<MediaControlUnit>(MediaControlUnit.class));
		builder.registerDao(new XMLDao<GPIOControlUnit>(GPIOControlUnit.class));
		builder.registerDao(new XMLDao<HassSwitchUnit>(HassSwitchUnit.class));
		builder.registerDao(new XMLDao<RCColorControlUnit>(RCColorControlUnit.class));
		builder.registerDao(new XMLDao<ActionControlUnit>(ActionControlUnit.class));

		builder.registerDao(new XMLDao<InformationWeather>(InformationWeather.class));
		builder.registerDao(new XMLDao<InformationTrigger>(InformationTrigger.class));
		builder.registerDao(new XMLDao<InformationHass>(InformationHass.class));

		DaoFactory.initiate(builder.setXmlFile(config).setFlushOnChange(true));
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

	private static List<IControllUnit> loadControlUnits() throws DaoException {
		List<IControllUnit> units = new ArrayList<IControllUnit>();
		DaoFactory factory = DaoFactory.getInstance();
		for (ControlUnitFactory f : mControlUnitFactory) {
			if (AbstractControlUnit.class.isAssignableFrom(f.getUnitClass())) {
				Dao<AbstractControlUnit> dao = factory.getDao(f.getUnitClass());
				for (AbstractControlUnit unit : dao.loadAll()) {
					units.add(unit);
				}
			}
		}
		return units;
	}

	private static ControlCenter loadControlCenter() throws IOException, DaoException {
		Dao<ControlCenter> dao = DaoFactory.getInstance().getDao(ControlCenter.class);
		if (dao.count() == 0)
			throw new DaoException("Controlcenter was not loaded!");
		ControlCenter center = dao.loadAll().get(0);
		WebInformation info = new WebInformation();
		WebSceneImpl scene = new WebSceneImpl(center);
		info.initialize();
		WebServer webServer = WebServer.getInstance();
		if (center.getPort() > 0) {
			webServer.setPort(center.getPort());
			for (ControlUnitFactory factory : mControlUnitFactory) {
				AbstractUnitHandler handler = factory.createUnitHandler(center);
				webServer.handle(handler.getWebPath(), handler);
			}
			webServer.handle(WEBSERVER_PATH, center);
			webServer.handle(INFORMATION_PATH, info);
			webServer.handle(SCENE_PATH, scene);
			webServer.start();
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

	public interface ControlUnitFactory {

		public Class<?> getUnitClass();

		public AbstractUnitHandler createUnitHandler(ControlCenter center);

	}

}
