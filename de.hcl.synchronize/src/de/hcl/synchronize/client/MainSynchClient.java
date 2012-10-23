package de.hcl.synchronize.client;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;

import de.hcl.synchronize.api.IHCLClient;
import de.hcl.synchronize.api.IHCLServer;
import de.hcl.synchronize.api.IHCLSession;
import de.hcl.synchronize.log.HCLLogger;
import de.hcl.synchronize.log.IHCLLogListener.HCLType;
import de.hcl.synchronize.util.IniFile;
import de.newsystem.rmi.api.Server;
import de.newsystem.rmi.protokol.RemoteException;

/**
 * The main synchronization client reads the configuration file and creates the
 * clients.
 * 
 * @author sebastian
 */
public class MainSynchClient {

	/**
	 * Property name for session id
	 */
	public static final String SESSION_ID = "session";

	/**
	 * Property name for location
	 */
	public static final String LOCATION = "location";

	/**
	 * Property name for read only flag
	 */
	public static final String READ_ONLY = "readonly";

	/**
	 * Property name for register at file system flag
	 */
	public static final String REGISTER_LISTENER = "registerlistener";

	/**
	 * Property name for client name
	 */
	public static final String CLIENT_NAME = "clientname";

	/**
	 * Property name for refresh rate in seconds
	 */
	public static final String REFRESH_RATE = "prefreshrate";

	public static void main(String registry, String config_file) {
		try {
			Server s = Server.getServer();
			s.forceConnectToRegistry(registry);
			s.startServer(IHCLClient.CLIENT_PORT + 1);

			IHCLServer server = forceGetHCLServer(s);

			IniFile iniFile = new IniFile(new File(config_file));
			String location = null;
			for (String clientID : iniFile.getSections()) {
				String sessionID = iniFile.getPropertyString(clientID,
						SESSION_ID, "null");
				location = iniFile.getPropertyString(clientID, LOCATION,
						"/dev/null");
				IHCLSession session = server.getSession(sessionID);
				boolean readOnly = iniFile.getPropertyBool(clientID, READ_ONLY,
						true);
				boolean listener = iniFile.getPropertyBool(clientID,
						REGISTER_LISTENER, false);
				String clientName = iniFile.getPropertyString(clientID,
						CLIENT_NAME, "null");
				int refreshRate = iniFile.getPropertyInt(clientID,
						REFRESH_RATE, Subfolder.MINIMAL_REFRESH_TIME_DIRECTORY);
				IHCLClient client = null;
				try {
					if (listener)
						client = new FatClient(location, clientName, session,
								readOnly, refreshRate);
					else
						client = new HCLClient(location, clientName, readOnly,
								refreshRate);

					HCLLogger.performLog("Add client synchronization: '"
							+ clientName + "'", HCLType.INFORMATION, null);
					session.addClient(client);
				} catch (IOException e) {
					HCLLogger.performLog(
							"Error create client synchronization: '"
									+ clientName + "': " + e.getMessage(), HCLType.ERROR, null);
					continue;
				}
			}
			HCLLogger.addListener(new HCLNotificator("Home Cloud Client",
					location + File.separator));

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
		long waitTime = 1000;
		long maxTime = 1000 * 60 * 10;
		while (server == null) {
			server = (IHCLServer) s
					.find(IHCLServer.SERVER_ID, IHCLServer.class);
			HCLLogger.performLog("No hcl server in registry", HCLType.WARNING,
					null);
			Thread.sleep(waitTime);
			waitTime *= 2;
			if (waitTime > maxTime)
				waitTime = maxTime;
		}
		return server;
	}

}
