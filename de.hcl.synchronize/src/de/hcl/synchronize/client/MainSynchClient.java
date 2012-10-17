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

public class MainSynchClient {

	public static void main(String registry, String config_file) {
		try {
			Server s = Server.getServer();
			s.forceConnectToRegistry(registry);
			s.startServer(IHCLClient.CLIENT_PORT + 1);

			IHCLServer server = forceGetHCLServer(s);

			IniFile iniFile = new IniFile(new File(config_file));
			String location = null;
			for (String sessionId : iniFile.getSections()) {
				for (String clientName : iniFile.getKeySet(sessionId)) {
					location = iniFile.getPropertyString(sessionId, clientName,
							"/dev/null");
					IHCLSession session = server.getSession(sessionId);
					IHCLClient client = new FatClient(location, clientName,
							session);
					HCLLogger.performLog("Add client synchronization: '"
							+ clientName + "'", HCLType.INFORMATION, null);
					session.addClient(client);
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
