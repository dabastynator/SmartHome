package de.neo.remote.api;

import java.io.IOException;
import java.net.UnknownHostException;

import de.neo.remote.protokol.RemoteException;

public class ConnectorManager extends Thread {

	public static final int PING_TIME = 1000 * 20;

	public static final String DUMMY_ID = "de.neo.rmi.dummy";

	private boolean isConnected;
	private Server server;
	private IRegistryConnection connector;
	private String registry;

	public ConnectorManager(Server server, IRegistryConnection connector,
			String registry) {
		this.server = server;
		this.connector = connector;
		this.registry = registry;
		isConnected = false;
	}

	@Override
	public void run() {
		while (connector.isManaged()) {
			try {
				testConnection();
				Thread.sleep(PING_TIME);
			} catch (InterruptedException e) {
			}
		}
	}

	private void testConnection() {
		if (!isConnected) {
			try {
				server.forceConnectToRegistry(registry);
				connector.onRegistryConnected(server);
				isConnected = true;
			} catch (UnknownHostException e) {
				isConnected = false;
			} catch (IOException e) {
				isConnected = false;
			}
		}
		try {
			server.find(DUMMY_ID, Object.class);
		} catch (RemoteException e) {
			if (isConnected) {
				connector.onRegistryLost();
				isConnected = false;
			}
		}
	}
}
