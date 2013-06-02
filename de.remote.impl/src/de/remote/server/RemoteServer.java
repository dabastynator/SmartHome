package de.remote.server;

import java.io.IOException;
import java.net.UnknownHostException;

import de.newsystem.rmi.api.RMILogger;
import de.newsystem.rmi.api.RMILogger.RMILogListener;
import de.newsystem.rmi.api.Server;
import de.newsystem.rmi.api.RMILogger.LogPriority;
import de.newsystem.rmi.protokol.RemoteException;
import de.remote.api.ControlConstants;
import de.remote.api.IChatServer;
import de.remote.api.IMusicStation;
import de.remote.api.IStationHandler;
import de.remote.impl.ChatServerImpl;
import de.remote.impl.StationHandler;
import de.remote.impl.StationImpl;

public class RemoteServer {

	public static void main(String[] args) {

		checkArgs(args);

		String place = getParameter("--location", args);
		String plsDirecotry = getParameter("--temp", args);
		String registry = getParameter("--registry", args);
		;
		String name = getParameter("--name", args);
		;
		boolean stationList = hasParameter("--stationlist", args);

		System.out.println("Registry Location at " + registry);
		System.out.println("Browse at " + place);
		System.out.println("Playlists at " + plsDirecotry);
		if (stationList)
			System.out.println("Register station handler");

		Server server = Server.getServer();
		RMILogger.addLogListener(new RMILogListener() {
			@Override
			public void rmiLog(LogPriority priority, String message, String id,
					long date) {
				System.out.println(priority + ": " + message + " (" + id + ")");
			}
		});

		IMusicStation station = new StationImpl(place, plsDirecotry, name);
		IChatServer chat = new ChatServerImpl();
		IStationHandler handler = null;
		if (stationList)
			handler = new StationHandler();

		try {
			// connect to registry and start server
			server.forceConnectToRegistry(registry);
			server.startServer(ControlConstants.STATION_PORT + 3);

			// register objects at registry
			if (stationList)
			  server.register(IStationHandler.STATION_ID, handler);
			else{
				handler = (IStationHandler) server.find(IStationHandler.STATION_ID, IStationHandler.class);
				if (handler == null){
					System.err.println("Error: No music station hander found in registry. Can't add music station to any list");
					System.exit(1);
				}
			}
			handler.addMusicStation(station);
			server.register(ControlConstants.CHAT_ID, chat);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
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
		System.out.println("  --registry    : ip of the registry.");
		System.out
				.println("  --stationlist : register station list. no parameter.");
		System.out.println("  --name        : specify name or music station.");
		System.out.println("  --location    : specify music browse location.");
		System.out
				.println("  --temp        : specify temprary location for playlists.");
	}

}
