package de.hcl.synchronize.server;

import java.io.IOException;

import de.hcl.synchronize.api.IHCLServer;
import de.hcl.synchronize.client.MainSynchClient;
import de.newsystem.rmi.api.Server;

public class MainSynchServer {

	public static void main(String[] args) {
		MainSynchClient.configureHCLLogOutput();
		MainSynchClient.configureRMILogOutput();
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

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
