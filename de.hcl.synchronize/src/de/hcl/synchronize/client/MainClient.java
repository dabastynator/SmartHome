package de.hcl.synchronize.client;

import java.io.IOException;
import java.net.UnknownHostException;

import de.hcl.synchronize.api.IHCLClient;
import de.hcl.synchronize.api.IHCLServer;
import de.newsystem.rmi.api.RMILogger;
import de.newsystem.rmi.api.Server;
import de.newsystem.rmi.api.RMILogger.LogPriority;
import de.newsystem.rmi.api.RMILogger.RMILogListener;
import de.newsystem.rmi.protokol.RemoteException;

public class MainClient {

	public static void main(String args[]) {
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
			if (args.length < 3) {
				System.err
						.println("1. argument must be the ip of the registry");
				System.err
						.println("2. argument must be the name or ip of the synchronization session");
				System.err
						.println("3. argument must be the location that will be synchronized");
				System.err
						.println("4. argument must be the name of the client");
				System.exit(1);
			}

			Server s = Server.getServer();
			s.forceConnectToRegistry(args[0]);
			s.startServer(IHCLClient.CLIENT_PORT + 1);

			IHCLServer server = (IHCLServer) s.find(IHCLServer.SERVER_ID,
					IHCLServer.class);

			IHCLClient client = new HCLClient(args[2], args[3]);

			server.addClient(args[1], client);

		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
