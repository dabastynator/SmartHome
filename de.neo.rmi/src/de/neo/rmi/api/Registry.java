package de.neo.rmi.api;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.neo.rmi.api.RMILogger.LogPriority;
import de.neo.rmi.api.RMILogger.RMILogListener;
import de.neo.rmi.handler.RegistryHandler;
import de.neo.rmi.protokol.GlobalObject;

/**
 * registry holds all global objects. To run the registry, it could be
 * initialized with a port and then start with the run method.<br>
 * <br>
 * <code>
 * Registry r = Registry.getRegistry();<br>
 * r.init(PORT);<br>
 * r.run();<br>
 * </code> <br>
 * the run method listens on the initialized port and is blocking
 * 
 * @author sebastian
 */
public class Registry {

	/**
	 * default port for registry
	 */
	public static int PORT = 5005;

	/**
	 * singleton
	 */
	private static Registry mRegistry;

	/**
	 * all global objects
	 */
	private HashMap<String, GlobalObject> mGlobalObjects = new HashMap<String, GlobalObject>();

	private List<RegistryHandler> mHandler = new ArrayList<RegistryHandler>();

	/**
	 * port for registry
	 */
	private int mPort = 5003;

	private ServerSocket mRegistrySocket;

	/**
	 * mRunning is true if registry runs and false otherwise.
	 */
	private boolean mRunning;

	/**
	 * private constructor for singleton
	 */
	private Registry() {
	}

	/**
	 * @return registry
	 */
	public static Registry getRegistry() {
		if (mRegistry == null)
			mRegistry = new Registry();
		return mRegistry;
	}

	/**
	 * Insert object into the registry by id
	 * 
	 * @param id
	 * @param object
	 */
	public void register(String id, GlobalObject object) {
		mGlobalObjects.put(id, object);
		RMILogger.performLog(LogPriority.INFORMATION, "register object", id);
	}

	/**
	 * find object in the registry by id
	 * 
	 * @param id
	 * @return globalObject
	 */
	public GlobalObject find(String id) {
		return mGlobalObjects.get(id);
	}

	/**
	 * remove object with id from the registry
	 * 
	 * @param id
	 */
	public void unRegister(String id) {
		mGlobalObjects.remove(id);
		RMILogger.performLog(LogPriority.INFORMATION, "unregister object", id);
	}

	/**
	 * set registry port
	 * 
	 * @param port
	 */
	public void init(int port) {
		this.mPort = port;
	}

	/**
	 * Start the registry. it listens on the initialized port for new
	 * connections. New connections will be handled in a separate thread.
	 */
	public void run() {
		try {
			mRegistrySocket = new ServerSocket(mPort);
			RMILogger.performLog(LogPriority.INFORMATION,
					"registry is listening on port: " + mPort, null);
			mRunning = true;
			while (mRunning) {
				final Socket socket = mRegistrySocket.accept();

				new Thread() {
					@Override
					public void run() {
						RegistryHandler h = new RegistryHandler(socket);
						mHandler.add(h);
						h.handle();
					}
				}.start();
			}
		} catch (IOException e1) {
			RMILogger.performLog(LogPriority.WARNING, "registry exception: "
					+ e1.getClass().getSimpleName() + ": " + e1.getMessage(),
					null);
		}
		RMILogger.performLog(LogPriority.WARNING, "registry closed", null);
	}

	/**
	 * static registry start
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		RMILogger.addLogListener(new RMILogListener() {
			@Override
			public void rmiLog(LogPriority priority, String message, String id,
					long date) {
				System.out.println(priority + ": " + message + " (" + id + ")");
			}
		});
		Registry registry = getRegistry();
		registry.init(PORT);
		registry.run();
	}

	public void close() {
		try {
			mRunning = false;
			for (RegistryHandler h : mHandler)
				h.close();
			mHandler.clear();
			mRegistrySocket.close();
		} catch (IOException e) {
		}
	}
}
