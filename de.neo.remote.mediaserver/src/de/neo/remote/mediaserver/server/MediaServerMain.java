package de.neo.remote.mediaserver.server;

import java.io.IOException;
import java.net.UnknownHostException;

import de.neo.remote.controlcenter.api.IControlCenter;
import de.neo.remote.controlcenter.api.IControlUnit;
import de.neo.remote.mediaserver.api.IChatServer;
import de.neo.remote.mediaserver.api.IMediaServer;
import de.neo.remote.mediaserver.impl.ChatServerImpl;
import de.neo.remote.mediaserver.impl.MediaControlUnit;
import de.neo.remote.mediaserver.impl.MediaServerImpl;
import de.neo.rmi.api.RMILogger;
import de.neo.rmi.api.Server;
import de.neo.rmi.api.RMILogger.LogPriority;
import de.neo.rmi.api.RMILogger.RMILogListener;
import de.neo.rmi.protokol.RemoteException;

public class MediaServerMain {

	public static void main(String[] args) {

		checkArgs(args);

		String place = getParameter("--location", args);
		String plsDirecotry = getParameter("--temp", args);
		String registry = getParameter("--registry", args);
		String type = getParameter("--type", args);
		float[] position = getPosition(args);

		String name = getParameter("--name", args);

		System.out.println("Registry Location at " + registry);
		System.out.println("Browse at " + place);
		System.out.println("Playlists at " + plsDirecotry);

		Server server = Server.getServer();
		RMILogger.addLogListener(new RMILogListener() {
			@Override
			public void rmiLog(LogPriority priority, String message, String id,
					long date) {
				System.out.println(priority + ": " + message + " (" + id + ")");
			}
		});

		IMediaServer mediaServer = new MediaServerImpl(place, plsDirecotry);
		IControlUnit mediaUnit = new MediaControlUnit(name, mediaServer,
				position, type);
		IChatServer chat = new ChatServerImpl();

		try {
			// connect to registry and start server
			server.forceConnectToRegistry(registry);
			server.startServer(IMediaServer.STATION_PORT);

			// register objects at registry
			IControlCenter center = (IControlCenter) server.forceFind(
					IControlCenter.ID, IControlCenter.class);
			if (center == null) {
				System.err
						.println("Error: No control center found in registry. Can't add music station to any list");
				System.exit(1);
			}
			center.addControlUnit(mediaUnit);
			server.register(IChatServer.ID, chat);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}

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
		System.out.println("  --name        : specify name or music station.");
		System.out.println("  --location    : specify music browse location.");
		System.out.println("  --position    : Position of server x,y,z.");
		System.out
				.println("  --temp        : specify temprary location for playlists.");
		System.out
				.println("  --type        : specify visualization(remote, multimedia, box).");
	}

}
