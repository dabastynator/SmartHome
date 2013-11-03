package de.neo.remote.gpiopower;

import java.io.IOException;
import java.net.UnknownHostException;

import de.neo.remote.controlcenter.api.IControlCenter;
import de.neo.remote.gpiopower.GPIOPower.Switch;
import de.neo.remote.gpiopower.api.IInternetSwitch;
import de.neo.rmi.api.RMILogger;
import de.neo.rmi.api.Server;
import de.neo.rmi.api.RMILogger.LogPriority;
import de.neo.rmi.api.RMILogger.RMILogListener;
import de.neo.rmi.protokol.RemoteException;

public class GPIOMain {

	public static void main(String[] args) {
		try {
			GPIOPower gpioPower = GPIOPower.getGPIOPower();
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
			for (Switch s : Switch.values()) {
				String switchParameter = "-" + s.toString().toLowerCase();
				if (hasParameter(switchParameter, args)) {
					try {
						String[] switchConfig = getParameter(switchParameter,
								args).split(",");
						float[] position = new float[3];
						position[0] = Float.parseFloat(switchConfig[1]);
						position[1] = Float.parseFloat(switchConfig[2]);
						position[2] = Float.parseFloat(switchConfig[3]);
						String type = switchConfig[4];
						InternetSwitchImpl internetSwitch = new InternetSwitchImpl(
								switchConfig[0], gpioPower, s, type);
						GPIOControlUnit unit = new GPIOControlUnit(
								switchConfig[0], "Internet switch",
								internetSwitch, position);
						control.addControlUnit(unit);
					} catch (Exception e) {
						System.err.println("Error reading switch " + s + ": "
								+ e.getMessage());
						printUsage();
						System.exit(1);
					}
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
		System.out
				.println("  -[a|b|c|d]    : name,x,y,z,type of specified switch.");
	}

}
