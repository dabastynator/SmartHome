package de.neo.remote.rmi;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

import de.neo.remote.rmi.RMILogger.LogPriority;

/**
 * handle a registry connection
 * 
 * @author sebastian
 */
public class RegistryHandler {

	/**
	 * socket
	 */
	private Socket socket;

	/**
	 * outputstream
	 */
	private ObjectOutputStream out;

	/**
	 * inputstream
	 */
	private ObjectInputStream in;

	/**
	 * allocate handler
	 * 
	 * @param socket
	 */
	public RegistryHandler(Socket socket) {
		this.socket = socket;
	}

	/**
	 * handle connection
	 */
	public void handle() {
		try {
			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());
			RMILogger.performLog(LogPriority.INFORMATION,
					"registry connection created", null);
			Registry registry = Registry.getRegistry();
			while (true) {
				RegistryRequest request = (RegistryRequest) in.readObject();
				RegistryReply reply = new RegistryReply();
				switch (request.getType()) {
				case FIND:
					reply.setObject(registry.find(request.getId()));
					break;
				case REGISTER:
					registry.register(request.getId(), request.getObject());
					break;
				case UNREGISTER:
					registry.unRegister(request.getId());
					break;
				}
				out.writeObject(reply);
				out.flush();
				out.reset();
			}

		} catch (IOException e) {
			if (e instanceof EOFException)
				RMILogger.performLog(LogPriority.INFORMATION,
						"registry connection closed", null);
			else if (e instanceof SocketException)
				RMILogger.performLog(LogPriority.INFORMATION,
						"registry closed", null);
		} catch (ClassNotFoundException e) {
			RMILogger
					.performLog(
							LogPriority.ERROR,
							"RegistryRequest-ClassNotFoundException: "
									+ e.getMessage(), null);
		}
	}

	public void close() {
		try {
			socket.close();
		} catch (IOException e) {
		}
	}

}
