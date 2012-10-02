package de.hcl.synchronize.client;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;

import de.hcl.synchronize.api.IHCLClient;
import de.hcl.synchronize.api.IHCLServer;
import de.hcl.synchronize.log.HCLLogger;
import de.hcl.synchronize.log.IHCLLogListener;
import de.hcl.synchronize.log.IHCLLogListener.HCLType;
import de.hcl.synchronize.util.IniFile;
import de.newsystem.rmi.api.RMILogger;
import de.newsystem.rmi.api.RMILogger.LogPriority;
import de.newsystem.rmi.api.RMILogger.RMILogListener;
import de.newsystem.rmi.api.Server;
import de.newsystem.rmi.protokol.RemoteException;

public class MainSynchClient {

	public static void main(String args[]) {
		configureRMILogOutput();
		configureHCLLogOutput();

		try {
			checkParameter(args);

			Server s = Server.getServer();
			s.forceConnectToRegistry(args[0]);
			s.startServer(IHCLClient.CLIENT_PORT + 1);

			IHCLServer server = forceGetHCLServer(s);

			IniFile iniFile = new IniFile(new File(args[1]));

			for (String sessionId : iniFile.getSections()) {
				for (String clientName : iniFile.getKeySet(sessionId)) {
					String location = iniFile.getPropertyString(sessionId,
							clientName, "/dev/null");
					IHCLClient client = new HCLClient(location, clientName);
					HCLLogger.performLog("add client synch: " + clientName
							+ " at " + location, HCLType.CREATE, null);
					server.addClient(sessionId, client);
				}
			}

		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * force get hcl server from registry. while the registry does not contain
	 * the server, retry every second.
	 * 
	 * @param registryIp
	 * @return IHCLServer
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws RemoteException
	 * @throws InterruptedException
	 */
	private static IHCLServer forceGetHCLServer(Server s)
			throws UnknownHostException, IOException, RemoteException,
			InterruptedException {

		IHCLServer server = (IHCLServer) s.find(IHCLServer.SERVER_ID,
				IHCLServer.class);

		while (server == null) {
			server = (IHCLServer) s
					.find(IHCLServer.SERVER_ID, IHCLServer.class);
			HCLLogger.performLog("no hcl server in registry", HCLType.ERROR,
					null);
			Thread.sleep(1000);
		}
		return server;
	}

	/**
	 * check parameter. if size is not correct, print information on err output
	 * stream and exit vm.
	 * 
	 * @param args
	 */
	private static void checkParameter(String[] args) {
		if (args.length < 2 || !(new File(args[1]).exists())) {
			System.err.println("1. argument must be the ip of the registry");
			System.err
					.println("2. argument must be the full path to the configuration file. "
							+ "The file must exits. "
							+ "The file must be in the ini format. "
							+ "The section determines the synchronization session id ."
							+ "Key and value are the clients in the session");
			System.exit(1);
		}
	}

	/**
	 * print rmi log on str and err output
	 */
	public static void configureRMILogOutput() {
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
	}

	/**
	 * print rmi log on str and err output
	 */
	public static void configureHCLLogOutput() {
		HCLLogger.addListener(new IHCLLogListener() {

			@Override
			public void hclLog(IHCLMessage message) {
				String author = message.client.getClass().getSimpleName();
				if (message.client instanceof IHCLClient)
					try {
						author = ((IHCLClient) message.client).getName();
					} catch (java.rmi.RemoteException e) {
					}
				if (message.client instanceof String)
					author = (String) message.client;
				if (message.type == HCLType.ERROR)
					System.err.println(message.type + ": " + message.message
							+ " (" + author + ")");
				else
					System.out.println(message.type + ": " + message.message
							+ " (" + author + ")");
			}
		});
	}
}
