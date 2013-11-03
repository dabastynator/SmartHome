package de.neo.remote.controlcenter;

import java.io.IOException;
import java.net.UnknownHostException;

import de.neo.remote.controlcenter.api.IControlCenter;
import de.neo.remote.controlcenter.impl.ControlCenterImpl;
import de.neo.rmi.api.RMILogger;
import de.neo.rmi.api.Server;
import de.neo.rmi.api.RMILogger.LogPriority;
import de.neo.rmi.api.RMILogger.RMILogListener;

/**
 * Main object to start and register the control center.
 * 
 * @author sebastian
 */
public class ControlCenterMain {

	public static void main(String[] args) {

		checkArgs(args);

		String registry = getParameter("--registry", args);
		String config = getParameter("--config", args);

		System.out.println("Start Control Center");

		Server server = Server.getServer();
		RMILogger.addLogListener(new RMILogListener() {
			@Override
			public void rmiLog(LogPriority priority, String message, String id,
					long date) {
				System.out.println(priority + ": " + message + " (" + id + ")");
			}
		});

		IControlCenter center = new ControlCenterImpl(config);

		try {
			// connect to registry and start server
			server.forceConnectToRegistry(registry);
			server.startServer(IControlCenter.PORT);

			// register object at registry
			server.register(IControlCenter.ID, center);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
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

	private static boolean hasParameter(String string, String[] args) {
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if (arg.equals(string))
				return true;
		}
		return false;
	}

	private static void checkArgs(String[] args) {
		for (String str : args) {
			if (str.toLowerCase().contains("help")
					|| str.toLowerCase().contains("-h")) {
				printUsage();
				System.exit(1);
			}

		}
	}

	private static void printUsage() {
		System.out.println("Usage:  ");
		System.out.println("  --registry    : ip of the registry.");
		System.out.println("  --config      : configuration file.");
	}

}
