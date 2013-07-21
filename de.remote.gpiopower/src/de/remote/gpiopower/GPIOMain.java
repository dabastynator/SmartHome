package de.remote.gpiopower;

import java.io.IOException;
import java.net.UnknownHostException;

import de.newsystem.rmi.api.RMILogger;
import de.newsystem.rmi.api.RMILogger.LogPriority;
import de.newsystem.rmi.api.RMILogger.RMILogListener;
import de.newsystem.rmi.api.Server;
import de.newsystem.rmi.protokol.RemoteException;
import de.remote.controlcenter.api.IControlCenter;
import de.remote.gpiopower.GPIOPower.Switch;
import de.remote.gpiopower.api.IInternetSwitch;

public class GPIOMain {

	public static void main(String[] args) {
		try {
			String registry = getParameter("--registry", args);
			Server server = Server.getServer();
			server.forceConnectToRegistry(registry);
			GPIOPower gpioPower = GPIOPower.getGPIOPower();
			RMILogger.addLogListener(new RMILogListener() {
				@Override
				public void rmiLog(LogPriority priority, String message,
						String id, long date) {
					System.out.println(priority.name() + ": " + message);
					System.out.flush();
				}
			});
			server.startServer(IInternetSwitch.PORT);
			IControlCenter control = (IControlCenter) server.find(
					IControlCenter.ID, IControlCenter.class);
			if (control == null)
				throw new RemoteException(IControlCenter.ID, "not found in registry");
			for (Switch s : Switch.values()) {
				String switchParameter = "-" + s.toString().toLowerCase();
				if (hasParameter(switchParameter, args)) {
					String switchName = getParameter(switchParameter, args);
					InternetSwitchImpl internetSwitch = new InternetSwitchImpl(
							switchName, gpioPower, s);
					GPIOControlUnit unit = new GPIOControlUnit(switchName, "Internet switch", internetSwitch);
					control.addControlUnit(unit);
				}
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
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
		System.out.println("  --registry    : ip of registry.");
		System.out.println("  -[a|b|c|d]    : name of specified switch.");
	}

}
