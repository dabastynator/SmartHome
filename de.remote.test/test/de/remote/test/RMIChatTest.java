package de.remote.test;

import java.io.IOException;
import java.net.UnknownHostException;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;

import de.newsystem.rmi.api.Oneway;
import de.newsystem.rmi.api.RMILogger;
import de.newsystem.rmi.api.RMILogger.RMILogListener;
import de.newsystem.rmi.api.Registry;
import de.newsystem.rmi.api.Server;
import de.newsystem.rmi.api.RMILogger.LogPriority;
import de.newsystem.rmi.dynamics.DynamicProxy;
import de.newsystem.rmi.protokol.RemoteException;
import de.remote.mediaserver.api.IBrowser;
import de.remote.mediaserver.api.IChatListener;
import de.remote.mediaserver.api.IChatServer;
import de.remote.mediaserver.api.IThumbnailListener;
import de.remote.mediaserver.impl.BrowserImpl;
import de.remote.mediaserver.impl.ChatServerImpl;

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
	 * remote object id for rmi test
	 */
	public static final String RMI_TEST_OBJECT_2_ID = "de.test.rmi.object2";

	/**
	 * received message over chat client
	 */
	public boolean received = false;

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
	 * perform simple test
	 */
	@Test
	public void testSimpleRMI() {
		RMILogger.addLogListener(new RMILogListener() {
			@Override
			public void rmiLog(LogPriority priority, String message, String id,
					long date) {
				System.out.println(priority + ": " + message + " (" + id + ")");
			}
		});
		startRegistry();
		startServer();
		startClient(false);
		Registry.getRegistry().close();
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Assert.assertTrue("no message received", received);
	}

	/**
	 * perform the main test
	 */
	@Test
	public void testRMI() {
		// RMILogger.addLogListener(new RMILogListener() {
		// @Override
		// public void rmiLog(LogPriority priority, String message, String id,
		// long date) {
		// System.out.println(priority + ": " + message + " (" + id + ")");
		// }
		// });
		System.out.println("     ======>>>>>      this inscance has " + received);
		startRegistry();
		startServer();
		DynamicProxy.counter = 0;
		startClient(false);
		DynamicProxy.counter = 0;
		startClient(true);
		System.out.println("     ======>>>>>      this inscance has " + received);
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("     ======>>>>>      this inscance has " + received);
		Assert.assertTrue("no message received", received);
	}

	/**
	 * start the test client
	 */
	private void startClient(boolean chatFirst) {
		Server s = new Server();
		MyChatListener l = new MyChatListener();
		IThumbnailListener l2 = new ThumbnailListener();
		try {
			s.connectToRegistry("localhost", RMI_TEST_PORT_REGISTRY);
			s.startServer(RMI_TEST_PORT_CLIENT);
			IChatServer chatServer = (IChatServer) s.find(RMI_TEST_OBJECT_ID,
					IChatServer.class);
			IBrowser browser = (IBrowser) s.find(RMI_TEST_OBJECT_2_ID,
					IBrowser.class);
			if (chatFirst) {
				chatServer.addChatListener(l);
				System.out.println("post message");
				chatServer.postMessage("me", "test message");
			}
			browser.fireThumbnails(l2, 10, 10);
			if (!chatFirst) {
				chatServer.addChatListener(l);
				System.out.println("post message");
				chatServer.postMessage("me", "test message");
			}
			s.close();
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
		IChatServer impl = new ChatServerImpl();
		IBrowser browserImpl = new BrowserImpl(BrowserTest.TEST_LOCATION);
		Server s = new Server();
		try {
			s.connectToRegistry("localhost", RMI_TEST_PORT_REGISTRY);
			s.startServer(RMI_TEST_PORT_SERVER);
			s.register(RMI_TEST_OBJECT_ID, impl);
			s.register(RMI_TEST_OBJECT_2_ID, browserImpl);
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
			received = true;
			System.out.println("    =====>    ");
			System.out.println("    =====>    receive message: " + client + " : " + message
					+ " at " + time + " " + received + "    <=====");
			System.out.println("    =====>    " + RMIChatTest.this.hashCode());
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

	public class ThumbnailListener implements IThumbnailListener {

		@Override
		@Oneway
		public void setThumbnail(String file, int width, int height,
				int[] thumbnail) throws RemoteException {
			System.out.println("get thumbnail: " + file);
		}

	}

}
