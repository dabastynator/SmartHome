package de.remote.gpiopower;

import java.io.IOException;
import java.net.UnknownHostException;

import de.newsystem.rmi.api.RMILogger;
import de.newsystem.rmi.api.Registry;
import de.newsystem.rmi.api.Server;
import de.newsystem.rmi.api.RMILogger.LogPriority;
import de.newsystem.rmi.api.RMILogger.RMILogListener;
import de.remote.gpiopower.api.IGPIOPower;

public class GPIOMain {

	public static void main(String[] args) {
		try {
			String root = null;
			Server server = connectToRegistry(args);
			GPIOPower gpioPower = GPIOPower.getGPIOPower();
			RMILogger.addLogListener(new RMILogListener() {
				@Override
				public void rmiLog(LogPriority priority, String message,
						String id, long date) {
					System.out.println(priority.name() + ": " + message);
					System.out.flush();
				}
			});
			server.startServer(IGPIOPower.PORT);
			server.register(IGPIOPower.ID, gpioPower);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static Server connectToRegistry(String[] args)
			throws UnknownHostException, IOException {
		String registry = null;
		int port = Registry.PORT;
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-r"))
				registry = args[i + 1];
			if (args[i].equals("-p"))
				port = Integer.valueOf(args[i + 1]);
		}
		if (registry == null) {
			System.err.println("Argument missing: -r for registry ip");
			System.exit(1);
		}
		Server server = Server.getServer();
		server.forceConnectToRegistry(registry);
		return server;
	}
	
}
