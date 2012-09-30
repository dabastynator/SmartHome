package de.newsystem.rmi.handler;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import de.newsystem.rmi.api.RMILogger;
import de.newsystem.rmi.api.Registry;
import de.newsystem.rmi.api.RMILogger.LogPriority;
import de.newsystem.rmi.protokol.RegistryReply;
import de.newsystem.rmi.protokol.RegistryRequest;

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
			}

		} catch (IOException e) {
			if (e instanceof EOFException)
				RMILogger.performLog(LogPriority.INFORMATION,
						"registry connection closed", null);
			else
				e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

}
