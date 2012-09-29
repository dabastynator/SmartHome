package de.hcl.synchronize.server;

import java.io.IOException;

import de.hcl.synchronize.api.IHCLClient;
import de.hcl.synchronize.api.IHCLServer;
import de.hcl.synchronize.client.HCLClient;
import de.newsystem.rmi.api.Server;
import de.newsystem.rmi.protokol.RemoteException;

public class SynchMain {

	public static void main(String[] args) {

		try {
			IHCLClient c2 = new HCLClient("/home/sebastian/temp/cl2/", "cl2");
			IHCLClient c1 = new HCLClient("/home/sebastian/temp/cl1/", "cl1");

			IHCLServer server = new HCLServer();
			server.addClient(c1);
			server.addClient(c2);

			Server s = Server.getServer();
			s.connectToRegistry("localhost");
			s.startServer(5056);
			s.register(IHCLServer.SERVER_ID, server);

			simulateClient();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void simulateClient() throws RemoteException {
		Server s = Server.getServer();
		IHCLServer hcl = (IHCLServer) s.find(IHCLServer.SERVER_ID,
				IHCLServer.class);

		Synchronizer synchronizer = new Synchronizer(hcl);
		synchronizer.synchronize();
	}

}
