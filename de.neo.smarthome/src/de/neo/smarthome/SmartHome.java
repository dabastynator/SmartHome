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
import de.neo.remote.rmi.RMILogger;
import de.neo.remote.rmi.RMILogger.LogPriority;
import de.neo.remote.rmi.RMILogger.RMILogListener;
import de.neo.remote.web.WebServer;
import de.neo.smarthome.RemoteLogger.RemoteLogListener;
import de.neo.smarthome.action.ActionControlUnit;
import de.neo.smarthome.action.WebActionImpl;
import de.neo.smarthome.api.Event;
import de.neo.smarthome.api.GroundPlot;
import de.neo.smarthome.api.GroundPlot.Point;
import de.neo.smarthome.api.GroundPlot.Wall;
import de.neo.smarthome.api.Trigger;
import de.neo.smarthome.api.Trigger.Parameter;
import de.neo.smarthome.controlcenter.ControlCenter;
import de.neo.smarthome.controlcenter.CronJobTrigger;
import de.neo.smarthome.controlcenter.EventRule;
import de.neo.smarthome.controlcenter.EventRule.Information;
import de.neo.smarthome.controlcenter.IControllUnit;
import de.neo.smarthome.gpio.GPIOControlUnit;
import de.neo.smarthome.gpio.WebSwitchImpl;
import de.neo.smarthome.informations.InformationWeather;
import de.neo.smarthome.informations.WebInformation;
import de.neo.smarthome.mediaserver.MediaControlUnit;
import de.neo.smarthome.mediaserver.WebMediaServerImpl;
import de.neo.smarthome.rccolor.RCColorControlUnit;
import de.neo.smarthome.rccolor.WebLEDStripImpl;

public class SmartHome {

	public static String WEBSERVER_PATH = "controlcenter";
	public static String INFORMATION_PATH = "information";
	private static List<ControlUnitFactory> mControlUnitFactory = new ArrayList<>();

	public static final SimpleDateFormat LogFormat = new SimpleDateFormat("dd.MM-HH:mm");

	static {
		mControlUnitFactory.add(new WebMediaServerImpl.MediaFactory());
		mControlUnitFactory.add(new WebSwitchImpl.SwitchFactory());
		mControlUnitFactory.add(new WebActionImpl.ActionFactory());
		mControlUnitFactory.add(new WebLEDStripImpl.LEDStripFactory());
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

			for (Trigger trigger : controlcenter.getStartupTrigger()) {
				controlcenter.trigger(trigger);
			}
		} catch (IOException | IllegalArgumentException | DaoException e) {
			System.err.println(
					"Error parsing configuration: " + e.getMessage() + " (" + e.getClass().getSimpleName() + ")");
			System.exit(1);
		}
	}

	private static void setupXMLDao(File config) throws DaoException {
		XMLDaoFactory f = (XMLDaoFactory) XMLDaoFactory.initiate(config);
		f.registerDao(ControlCenter.class, new XMLDao<ControlCenter>(f, ControlCenter.class));
		f.registerDao(CronJobTrigger.class, new XMLDao<CronJobTrigger>(f, CronJobTrigger.class));
		f.registerDao(EventRule.class, new XMLDao<EventRule>(f, EventRule.class));
		f.registerDao(Event.class, new XMLDao<Event>(f, Event.class));
		f.registerDao(Information.class, new XMLDao<Information>(f, Information.class));
		f.registerDao(Trigger.class, new XMLDao<Trigger>(f, Trigger.class));
		f.registerDao(Parameter.class, new XMLDao<Parameter>(f, Parameter.class));

		f.registerDao(GroundPlot.class, new XMLDao<GroundPlot>(f, GroundPlot.class));
		f.registerDao(Wall.class, new XMLDao<Wall>(f, Wall.class));
		f.registerDao(Point.class, new XMLDao<Point>(f, Point.class));

		f.registerDao(MediaControlUnit.class, new XMLDao<MediaControlUnit>(f, MediaControlUnit.class));
		f.registerDao(GPIOControlUnit.class, new XMLDao<GPIOControlUnit>(f, GPIOControlUnit.class));
		f.registerDao(RCColorControlUnit.class, new XMLDao<RCColorControlUnit>(f, RCColorControlUnit.class));
		f.registerDao(ActionControlUnit.class, new XMLDao<ActionControlUnit>(f, ActionControlUnit.class));

		f.registerDao(InformationWeather.class, new XMLDao<InformationWeather>(f, InformationWeather.class));
		
		f.read();
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
			Dao<AbstractControlUnit> dao = factory.getDao(f.getUnitClass());
			for (AbstractControlUnit unit : dao.loadAll()) {
				units.add(unit);
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
		info.initialize();
		center.setInformationHandler(info);
		WebServer webServer = WebServer.getInstance();
		if (center.getPort() > 0) {
			webServer.setPort(center.getPort());
			for (ControlUnitFactory factory : mControlUnitFactory) {
				AbstractUnitHandler handler = factory.createUnitHandler(center);
				webServer.handle(handler.getWebPath(), handler, center.getToken());
			}
			webServer.handle(WEBSERVER_PATH, center, center.getToken());
			webServer.handle(INFORMATION_PATH, info, center.getToken());
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
