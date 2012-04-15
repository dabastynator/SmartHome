package de.remote.test;

import java.io.IOException;
import java.net.UnknownHostException;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;

import de.newsystem.rmi.api.Registry;
import de.newsystem.rmi.api.Server;
import de.newsystem.rmi.protokol.RemoteException;
import de.remote.api.IChatListener;
import de.remote.api.IChatServer;
import de.remote.api.IStation;
import de.remote.impl.StationImpl;

public class RMIChatTest extends TestCase {

	/**
	 * port for registry
	 */
	public static final int RMI_TEST_PORT_REGISTRY = 5024;

	/**
	 * port for server
	 */
	public static final int RMI_TEST_PORT_SERVER = RMI_TEST_PORT_REGISTRY + 1;

	/**
	 * port for client
	 */
	public static final int RMI_TEST_PORT_CLIENT = RMI_TEST_PORT_REGISTRY + 2;

	/**
	 * remote object id for rmi test
	 */
	public static final String RMI_TEST_OBJECT_ID = "de.test.rmi.object";

	/**
	 * start registry in separate thread
	 */
	private void startRegistry() {
		new Thread() {
			public void run() {
				Registry registry = Registry.getRegistry();
				registry.init(RMI_TEST_PORT_REGISTRY);
				registry.run();
			}
		}.start();
	}

	/**
	 * perform the main test
	 */
	@Test
	public void testRMI() {
		startRegistry();
		startServer();
		startClient();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * start the test client
	 */
	private void startClient() {
		Server s = new Server();
		MyChatListener l = new MyChatListener();
		try {
			s.connectToRegistry("localhost", RMI_TEST_PORT_REGISTRY);
			s.startServer(RMI_TEST_PORT_CLIENT);
			IStation station = (IStation) s.find(RMI_TEST_OBJECT_ID,
					IStation.class);
			IChatServer chatServer = station.getChatServer();
			chatServer.addChatListener(l);
			System.out.println("post message");
			chatServer.postMessage("me", "test message");
		} catch (RemoteException e) {
			Assert.assertTrue(e.getMessage(), false);
		} catch (UnknownHostException e) {
			Assert.assertTrue(e.getMessage(), false);
		} catch (IOException e) {
			Assert.assertTrue(e.getMessage(), false);
		}
	}

	/**
	 * start the server
	 */
	private void startServer() {
		StationImpl impl = new StationImpl(null, null);
		Server s = new Server();
		try {
			s.connectToRegistry("localhost", RMI_TEST_PORT_REGISTRY);
			s.startServer(RMI_TEST_PORT_SERVER);
			s.register(RMI_TEST_OBJECT_ID, impl);
		} catch (UnknownHostException e) {
			Assert.assertTrue(e.getMessage(), false);
		} catch (IOException e) {
			Assert.assertTrue(e.getMessage(), false);
		}
	}

	public class MyChatListener implements IChatListener {
		@Override
		public void informMessage(String client, String message, String time)
				throws RemoteException {
			System.out.println("receive message: " + client + " : " + message
					+ " at " + time);
		}

		@Override
		public void informNewClient(String client) throws RemoteException {
		}

		@Override
		public void informLeftClient(String client) throws RemoteException {
		}

		@Override
		public String getName() throws RemoteException {
			return "testclient";
		}

	}

}
