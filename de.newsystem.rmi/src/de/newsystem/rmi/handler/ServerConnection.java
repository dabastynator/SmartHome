package de.newsystem.rmi.handler;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import de.newsystem.rmi.api.RMILogger;
import de.newsystem.rmi.api.Server;
import de.newsystem.rmi.api.RMILogger.LogPriority;
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
	private List<ConnectionSocket> serverConnections = new ArrayList<ConnectionSocket>();

	/**
	 * allocate new server connection with given server and socket.
	 * 
	 * @param serverSocket
	 */
	public ServerConnection(ServerPort serverPort) {
		this.serverPort = serverPort;
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
	 * disconnect all sockets of the server connection
	 * 
	 * @throws IOException
	 */
	public void disconnect() throws IOException {
		for (ConnectionSocket cs : serverConnections) {
			cs.disconnect();
		}
		serverConnections.clear();
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
		 * @throws IOException
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

}
