package de.remote.server;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

import de.newsystem.rmi.api.Server;
import de.remote.api.ControlConstants;
import de.remote.api.IStation;
import de.remote.impl.StationImpl;

public class RemoteServer {

	public static void main(String[] args) {

		String place = "/media/baestynator/";
		String registry = "192.168.1.3";

		if (args.length > 0)
			place = args[0];

		if (args.length > 1)
			registry = args[1];

		System.out.println("Browse at " + place + " (" + registry + ")");

		Server server = Server.getServer();

		IStation station = new StationImpl(place);

		try {
			forceConnectToRegistry(registry);
			server.startServer(ControlConstants.STATION_PORT);
			server.register(ControlConstants.STATION_ID, station);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static void forceConnectToRegistry(String registry)
			throws UnknownHostException, IOException {
		Server server = Server.getServer();
		boolean connected = false;
		int counter = 1;
		while (!connected) {
			try {
				server.connectToRegistry(registry);
				connected = true;
			} catch (SocketException e) {
				connected = false;
				System.out.println("socketexception " + (counter++)
						+ ". time: " + e.getMessage());
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

}
