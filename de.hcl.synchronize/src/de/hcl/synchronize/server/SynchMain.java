package de.hcl.synchronize.server;

import java.io.IOException;

import de.hcl.synchronize.api.IHCLClient;
import de.hcl.synchronize.api.IHCLServer;
import de.hcl.synchronize.client.HCLClient;
import de.newsystem.rmi.api.RMILogger;
import de.newsystem.rmi.api.RMILogger.RMILogListener;
import de.newsystem.rmi.api.Server;
import de.newsystem.rmi.api.RMILogger.LogPriority;
import de.newsystem.rmi.protokol.RemoteException;

public class SynchMain {

	public static void main(String[] args) {
		RMILogger.addLogListener(new RMILogListener() {

			@Override
			public void rmiLog(LogPriority priority, String message, String id,
					long date) {
				if (priority == LogPriority.INFORMATION)
					System.out.println(priority + ": " + message + " (" + id
							+ ")");
				else
					System.err.println(priority + ": " + message + " (" + id
							+ ")");
			}
		});
		try {
			if (args.length < 1) {
				System.err
						.println("1. argument must be the ip of the registry");
				System.exit(1);
			}
			IHCLServer server = new HCLServer();
			Server s = Server.getServer();
			s.forceConnectToRegistry(args[0]);
			s.startServer(IHCLServer.SERVER_PORT);
			s.register(IHCLServer.SERVER_ID, server);

			Synchronizer synchronizer = new Synchronizer(server);
			synchronizer.synchronize();

			// simulateClient();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void simulateClient() throws RemoteException, IOException {
		Server s = Server.getServer();
		IHCLServer server = (IHCLServer) s.find(IHCLServer.SERVER_ID,
				IHCLServer.class);

		IHCLClient c2 = new HCLClient("/home/sebastian/temp/cl2/", "cl2");
		IHCLClient c1 = new HCLClient("/home/sebastian/temp/cl1/", "cl1");

		server.addClient("Bastis Dokumente", c1);
		server.addClient("Bastis Dokumente", c2);

	}

}
