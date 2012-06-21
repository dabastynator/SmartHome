package de.newsystem.rmi.api;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.newsystem.rmi.dynamics.DynamicAdapter;
import de.newsystem.rmi.dynamics.DynamicProxy;
import de.newsystem.rmi.handler.ConnectionHandler;
import de.newsystem.rmi.protokol.GlobalObject;
import de.newsystem.rmi.protokol.RegistryReply;
import de.newsystem.rmi.protokol.RegistryRequest;
import de.newsystem.rmi.protokol.RemoteException;
import de.newsystem.rmi.protokol.ServerPort;
import de.newsystem.rmi.protokol.RegistryRequest.Type;

/**
 * server api for all clients. to provide a remote object first initialize the
 * server then register the object.<br>
 * <br>
 * <code>
 * Server s = Server.getServer();<br>
 * s.connectToRegistry(REGISTRY_LOCATION, REGISTRY_URL);<br>
 * s.startServer(SERVER_PORT);<br>
 * s.register(OBJECT_ID, remoteObject);<br>
 * <code>
 * 
 * @author sebastian
 */
public class Server {

	/**
	 * default server port
	 */
	public static int PORT = 5003;

	/**
	 * singleton
	 */
	private static Server server;

	/**
	 * registry socket
	 */
	private Socket registrySocket;

	/**
	 * registry inputstream
	 */
	private ObjectInputStream registryIn;

	/**
	 * registry outputstream
	 */
	private ObjectOutputStream registryOut;

	/**
	 * server socket
	 */
	private ServerSocket serverSocket;

	/**
	 * list of all proxies
	 */
	private Map<String, Object> proxyMap = new HashMap<String, Object>();

	/**
	 * list of all adapters
	 */
	private Map<String, DynamicAdapter> adapterMap = new HashMap<String, DynamicAdapter>();

	/**
	 * map to get id of adapter object
	 */
	private Map<Object, String> adapterObjectId = new HashMap<Object, String>();

	/**
	 * list of all connections to other servers
	 */
	private HashMap<ServerPort, ServerPort> serverConnections = new HashMap<ServerPort, ServerPort>();

	/**
	 * list of all connection of the server
	 */
	private List<ConnectionHandler> handlers = new ArrayList<ConnectionHandler>();

	/**
	 * server port
	 */
	private int port = PORT;

	/**
	 * server ip
	 */
	private String ip;

	/**
	 * is connected to registry
	 */
	private boolean isConnectedRegistry = false;

	/**
	 * create connection to the registry
	 * 
	 * @param registry
	 * @param port
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public void connectToRegistry(String registry, int port)
			throws UnknownHostException, IOException {
		registrySocket = new Socket(registry, port);
		registryOut = new ObjectOutputStream(registrySocket.getOutputStream());
		registryIn = new ObjectInputStream(registrySocket.getInputStream());
		ip = registrySocket.getLocalAddress().getHostAddress();
		isConnectedRegistry = true;
	}

	/**
	 * create connection to the registry
	 * 
	 * @param registry
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public void connectToRegistry(String registry) throws UnknownHostException,
			IOException {
		connectToRegistry(registry, Registry.PORT);
	}

	/**
	 * force connection to given registry. Retry after 500 ms, if network is not
	 * available.
	 * 
	 * @param ip
	 *            of registry
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public void forceConnectToRegistry(String registry)
			throws UnknownHostException, IOException {
		boolean connected = false;
		int counter = 1;
		while (!connected) {
			try {
				connectToRegistry(registry);
				connected = true;
			} catch (SocketException e) {
				connected = false;
				System.err.println("socketexception " + (counter++)
						+ ". time: " + e.getMessage());
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	/**
	 * start the server. the port must be initialized, otherwise the default
	 * server port will be used. the server starts a new thread, so this method
	 * is not blocking.
	 * 
	 * @param port
	 */
	public void startServer(int port) {
		this.port = port;
		new ServerThread().start();
	}

	/**
	 * start the server at default port
	 */
	public void startServer() {
		startServer(PORT);
	}

	/**
	 * get singleton server
	 * 
	 * @return server
	 */
	public static Server getServer() {
		if (server == null)
			server = new Server();
		return server;
	}

	/**
	 * register a object in the registry. the registry must be initialized
	 * before.
	 * 
	 * @param id
	 * @param object
	 */
	public void register(String id, Object object) {
		// add adapter
		ServerPort serverPort = new ServerPort(ip, port);
		adapterMap.put(id, new DynamicAdapter(object, this));
		adapterObjectId.put(object, id);
		// tell registry
		GlobalObject globalObject = new GlobalObject(id, serverPort);
		RegistryRequest request = new RegistryRequest(Type.REGISTER);
		request.setObject(globalObject);
		request.setId(id);
		try {
			registryOut.writeObject(request);
			RegistryReply reply = (RegistryReply) registryIn.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * remove a object from the registry. the registry must be initialized
	 * before.
	 * 
	 * @param id
	 */
	public void unRegister(String id) {
		adapterObjectId.remove(adapterMap.get(id));
		adapterMap.remove(id);
		RegistryRequest request = new RegistryRequest(Type.UNREGISTER);
		try {
			registryOut.writeObject(request);
			RegistryReply reply = (RegistryReply) registryIn.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * search a remote object in the registry. the registry must be initialized
	 * before.
	 * 
	 * @param id
	 * @param template
	 * @return object
	 * @throws RemoteException
	 */
	public Object find(String id, Class template) throws RemoteException {
		try {
			RegistryRequest request = new RegistryRequest(Type.FIND);
			request.setId(id);
			registryOut.writeObject(request);
			RegistryReply reply = (RegistryReply) registryIn.readObject();
			if (reply.getObject() == null)
				return null;
			// connect to server
			ServerPort sp = connectToServer(reply.getObject().getServerPort());
			// create proxy
			return createProxy(id, sp, template);
		} catch (UnknownHostException e) {
			throw new RemoteException(id, e.getMessage());
		} catch (IOException e) {
			throw new RemoteException(id, e.getMessage());
		} catch (ClassNotFoundException e) {
			throw new RemoteException(id, e.getMessage());
		}
	}

	/**
	 * @param id
	 * @param sp
	 * @param template
	 * @return proxy
	 */
	public Object createProxy(String id, ServerPort sp, Class template) {
		Object p = proxyMap.get(id);
		if (p != null)
			return p;
		p = new DynamicProxy(id, sp, this);
		Object object = Proxy.newProxyInstance(p.getClass().getClassLoader(),
				new Class[] { template }, (InvocationHandler) p);
		proxyMap.put(id, object);
		return object;
	}

	public Map<String, DynamicAdapter> getAdapterMap() {
		return adapterMap;
	}

	public Map<Object, String> getAdapterObjectIdMap() {
		return adapterObjectId;
	}

	/**
	 * the server thread handles the connection handler
	 * 
	 * @author sebastian
	 */
	private class ServerThread extends Thread {

		@Override
		public void run() {
			try {
				serverSocket = new ServerSocket(port);
				System.out.println("server is listening on port: " + port);
				while (serverSocket != null) {
					final Socket socket = serverSocket.accept();
					new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								ConnectionHandler handler = new ConnectionHandler(
										ip, port, socket, Server.this);
								handlers.add(handler);
								handler.handle();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}).start();
				}
			} catch (IOException e1) {
				if (e1 instanceof SocketException)
					System.out.println("server closed " + "(" + e1.getMessage()
							+ ")");
				else
					e1.printStackTrace();
			}
		}

	}

	/**
	 * close all connections
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {
		// close connections
		for (ServerPort sp : serverConnections.keySet())
			sp.close();
		for (ConnectionHandler handler : handlers)
			handler.close();
		// close sockets
		if (registrySocket != null)
			registrySocket.close();
		if (serverSocket != null)
			serverSocket.close();

		serverSocket = null;
		registrySocket = null;
		serverConnections.clear();
		handlers.clear();
		proxyMap.clear();
		adapterMap.clear();
		adapterObjectId.clear();
	}

	/**
	 * checks connections for given connection
	 * 
	 * @param serverPort
	 * @return serverPort
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public ServerPort connectToServer(ServerPort serverPort)
			throws UnknownHostException, IOException {
		ServerPort sp = serverConnections.get(serverPort);
		if (sp != null && !sp.getSocket().isClosed())
			return sp;
		serverPort.connect();
		serverConnections.put(serverPort, serverPort);
		return serverPort;
	}

	/**
	 * returns the serverPort for this server
	 * 
	 * @return serverPort
	 */
	public ServerPort getServerPort() {
		return new ServerPort(ip, port);
	}

	public boolean isConnectedToRegistry() {
		return isConnectedRegistry;
	}

}
