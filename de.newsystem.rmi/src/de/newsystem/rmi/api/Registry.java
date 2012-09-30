package de.newsystem.rmi.api;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import de.newsystem.rmi.api.RMILogger.LogPriority;
import de.newsystem.rmi.api.RMILogger.RMILogListener;
import de.newsystem.rmi.handler.RegistryHandler;
import de.newsystem.rmi.protokol.GlobalObject;

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
	private static Registry registry;

	/**
	 * all global objects
	 */
	private HashMap<String, GlobalObject> globalObjects = new HashMap<String, GlobalObject>();

	/**
	 * port for registry
	 */
	private int port = 5003;

	/**
	 * private constructor for singleton
	 */
	private Registry() {
	}

	/**
	 * @return registry
	 */
	public static Registry getRegistry() {
		if (registry == null)
			registry = new Registry();
		return registry;
	}

	/**
	 * Insert object into the registry by id
	 * 
	 * @param id
	 * @param object
	 */
	public void register(String id, GlobalObject object) {
		globalObjects.put(id, object);
		RMILogger.performLog(LogPriority.INFORMATION, "register object", id);
	}

	/**
	 * find object in the registry by id
	 * 
	 * @param id
	 * @return globalObject
	 */
	public GlobalObject find(String id) {
		return globalObjects.get(id);
	}

	/**
	 * remove object with id from the registry
	 * 
	 * @param id
	 */
	public void unRegister(String id) {
		globalObjects.remove(id);
		RMILogger.performLog(LogPriority.INFORMATION, "unregister object", id);
	}

	/**
	 * set registry port
	 * 
	 * @param port
	 */
	public void init(int port) {
		this.port = port;
	}

	/**
	 * Start the registry. it listens on the initialized port for new
	 * connections. New connections will be handled in a separate thread.
	 */
	public void run() {
		try {
			ServerSocket server = new ServerSocket(port);
			RMILogger.performLog(LogPriority.INFORMATION,
					"registry is listening on port: " + port, null);
			while (true) {
				final Socket socket = server.accept();

				new Thread() {
					@Override
					public void run() {
						RegistryHandler handler = new RegistryHandler(socket);
						handler.handle();
					}
				}.start();
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
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
}
