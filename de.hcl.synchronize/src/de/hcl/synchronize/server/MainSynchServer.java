package de.hcl.synchronize.server;

import java.io.IOException;

import de.hcl.synchronize.api.IHCLServer;
import de.neo.rmi.api.Server;

public class MainSynchServer {

	public static void main(String registry) {
		try {
			JobQueue queue = new JobQueue(5080, 5);
			queue.startTransfering();

			IHCLServer server = new HCLServer(queue);
			Server s = Server.getServer();
			s.forceConnectToRegistry(registry);
			s.startServer(IHCLServer.SERVER_PORT);
			s.register(IHCLServer.SERVER_ID, server);

			Synchronizer synchronizer = new Synchronizer(server, queue);
			synchronizer.synchronize();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
