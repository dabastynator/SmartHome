package de.newsystem.rmi.handler;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.newsystem.rmi.api.RMILogger;
import de.newsystem.rmi.api.RMILogger.LogPriority;
import de.newsystem.rmi.api.Server;
import de.newsystem.rmi.dynamics.DynamicProxy;
import de.newsystem.rmi.protokol.Request;
import de.newsystem.rmi.protokol.Request.Type;
import de.newsystem.rmi.protokol.ServerPort;

/**
 * the server connection handles the sockets with input and output to a server.
 * it creates dynamically new sockets, if they are needed.
 * 
 * @author sebastian
 */
public class ServerConnection {

	/**
	 * the server port contains information about the server to be connected.
	 */
	private ServerPort serverPort;

	/**
	 * list of server connections
	 */
	private List<ConnectionSocket> serverConnections = Collections.synchronizedList(new ArrayList<ConnectionSocket>());

	/**
	 * list of all proxies that belong to this server port
	 */
	private Map<String, Object> proxyMap = new HashMap<String, Object>();

	private Server server;

	/**
	 * allocate new server connection with given server and socket.
	 * 
	 * @param serverSocket
	 */
	public ServerConnection(ServerPort serverPort, Server server) {
		this.serverPort = serverPort;
		this.server = server;
	}

	/**
	 * get a free socket for a proxy. ensure to call the free - method of the
	 * connection socket so the socket can be used by other proxies.
	 * 
	 * @return free socket
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public synchronized ConnectionSocket getFreeConnectionSocket()
			throws UnknownHostException, IOException {
		while (true) {
			// return free socket if there is anyone
			for (int i = 0; i < serverConnections.size(); i++) {
				ConnectionSocket socket = serverConnections.get(i);
				if (socket.socket.isClosed()) {
					serverConnections.remove(i);
					RMILogger
							.performLog(
									LogPriority.WARNING,
									"Connection closed from: "
											+ serverPort.getIp() + ":"
											+ serverPort.getPort() + "("
											+ serverConnections.size()
											+ " left)", null);
					continue;
				}
				if (!socket.isInUse()) {
					socket.setInUse(true);
					return socket;
				}
			}
			// create new socket if maximum socket count is lower than current
			// sockets
			int maximumSockets = Server.getServer().getConnectionSocketCount();
			if (serverConnections.size() < maximumSockets) {
				Socket socket = new Socket(serverPort.getIp(),
						serverPort.getPort());
				ObjectOutputStream output = new ObjectOutputStream(
						socket.getOutputStream());
				ObjectInputStream input = new ObjectInputStream(
						socket.getInputStream());
				ConnectionSocket newSocket = new ConnectionSocket(socket,
						input, output);
				newSocket.setInUse(true);
				serverConnections.add(newSocket);
				RMILogger
						.performLog(LogPriority.INFORMATION, "create "
								+ serverConnections.size()
								+ ". connection to: " + serverPort.getIp()
								+ ":" + serverPort.getPort(), null);
				return newSocket;
			}
			try {
				// wait for free socket
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Create new Proxy with given id, server connection and class template.
	 * 
	 * @param id
	 * @param sc
	 * @param template
	 * @return proxy
	 */
	public Object createProxy(String id, Class template) {
		Object p = proxyMap.get(id);
		if (p != null)
			return p;
		p = new DynamicProxy(id, this, server);
		Object object = Proxy.newProxyInstance(p.getClass().getClassLoader(),
				new Class[] { template }, (InvocationHandler) p);
		proxyMap.put(id, object);
		return object;
	}

	/**
	 * disconnect all sockets of the server connection
	 * 
	 */
	public void disconnect() {
		Request closeRequest = new Request("", "");
		closeRequest.setType(Type.CLOSE);
		closeRequest.setParams(new Object[]{server.getServerPort()});
		for (ConnectionSocket cs : serverConnections) {
			try {
				cs.output.writeObject(closeRequest);
				System.out.println("success send close packet to " + serverPort.getIp());
			} catch (IOException e) {
				System.out.println("fail send close packet to " + serverPort.getIp());
			}
			cs.disconnect();
		}
		proxyMap.clear();
		serverConnections.clear();

	}
	
	@Override
	public String toString() {
		int size = serverConnections.size();
		String ip = serverPort.getIp();
		return size + " connections to " + ip;
	}

	/**
	 * the socket connection contains input and output streams for a connection
	 * with a server.
	 * 
	 * @author sebastian
	 */
	public class ConnectionSocket {

		/**
		 * the socket
		 */
		private Socket socket;

		/**
		 * data input stream of the socket
		 */
		private ObjectInputStream input;

		/**
		 * data output stream of the socket
		 */
		private ObjectOutputStream output;

		/**
		 * true if the socket is in use by a proxy
		 */
		private boolean inUse;

		/**
		 * allocate new connection socket with streams
		 * 
		 * @param socket
		 * @param input
		 * @param output
		 */
		public ConnectionSocket(Socket socket, ObjectInputStream input,
				ObjectOutputStream output) {
			this.socket = socket;
			this.input = input;
			this.output = output;
			inUse = false;
		}

		/**
		 * close streams of the socket
		 * 
		 */
		public void disconnect() {
			try {
				socket.close();
			} catch (IOException e) {
			}
		}

		/**
		 * @return input stream of the connection
		 */
		public ObjectInputStream getInput() {
			return input;
		}

		/**
		 * @return output stream of the connection
		 */
		public ObjectOutputStream getOutput() {
			return output;
		}

		/**
		 * sets the usage of the socket
		 */
		public void setInUse(boolean inUse) {
			this.inUse = inUse;
		}

		/**
		 * @return true if the socket is not in use
		 */
		public boolean isInUse() {
			return inUse;
		}

		/**
		 * set the socket not to be used and informs the server connection. this
		 * method is necessary to be called to use this socket by other proxies.
		 */
		public void free() {
			setInUse(false);
			synchronized (ServerConnection.this) {
				ServerConnection.this.notify();
			}
		}

	}

	public ServerPort getServerPort() {
		return serverPort;
	}

}
