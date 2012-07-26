package de.remote.server;

import java.io.IOException;
import java.net.UnknownHostException;

import de.newsystem.rmi.api.Server;
import de.remote.api.ControlConstants;
import de.remote.api.IChatServer;
import de.remote.api.IMusicStation;
import de.remote.impl.ChatServerImpl;
import de.remote.impl.StationImpl;

public class RemoteServer {

	public static void main(String[] args) {

		if (args.length < 3) {
			System.err.println("argument missing!");
			System.err.println("1. argument: directory to browse");
			System.err.println("2. argument: direcotry for playlists");
			System.err.println("3. argument: ip of the registry");
			System.exit(1);
		}

		String place = args[0];
		String plsDirecotry = args[1];
		String registry = args[2];

		System.out.println("Registry Location at " + registry);
		System.out.println("Browse at " + place);
		System.out.println("Playlists at " + plsDirecotry);

		Server server = Server.getServer();

		IMusicStation station = new StationImpl(place, plsDirecotry);
		IChatServer chat = new ChatServerImpl();

		try {
			// connect to registry and start server
			server.forceConnectToRegistry(registry);
			server.startServer(ControlConstants.STATION_PORT + 2);

			// register objects at registry
			server.register(ControlConstants.STATION_ID, station);
			server.register(ControlConstants.CHAT_ID, chat);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
