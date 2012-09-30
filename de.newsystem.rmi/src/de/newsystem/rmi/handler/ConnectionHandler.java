package de.newsystem.rmi.handler;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Collection;

import de.newsystem.rmi.api.RMILogger;
import de.newsystem.rmi.api.Server;
import de.newsystem.rmi.api.RMILogger.LogPriority;
import de.newsystem.rmi.dynamics.DynamicAdapter;
import de.newsystem.rmi.protokol.RemoteAble;
import de.newsystem.rmi.protokol.RemoteException;
import de.newsystem.rmi.protokol.Reply;
import de.newsystem.rmi.protokol.Request;

/**
 * handles a client connection.
 * 
 * @author sebastian
 */
public class ConnectionHandler {

	/**
	 * static id counter
	 */
	public static int idCounter = 0;

	/**
	 * inputstream
	 */
	private ObjectOutputStream out;

	/**
	 * outputstream
	 */
	private ObjectInputStream in;

	/**
	 * ip of server
	 */
	private String ip;

	/**
	 * port of the server
	 */
	private int port;

	/**
	 * client socket
	 */
	private Socket socket;

	/**
	 * the server that holds all provided objects
	 */
	private Server server;

	/**
	 * Allocate new connection handler.
	 * 
	 * @param ip
	 * @param socket
	 * @throws IOException
	 */
	public ConnectionHandler(String ip, int port, Socket socket, Server server)
			throws IOException {
		this.ip = ip;
		this.port = port;
		out = new ObjectOutputStream(socket.getOutputStream());
		in = new ObjectInputStream(socket.getInputStream());
		this.socket = socket;
		this.server = server;
	}

	/**
	 * handle the connection
	 */
	public void handle() {
		RMILogger.performLog(LogPriority.INFORMATION,
				"incoming connection from client started", null);
		try {
			while (true) {
				Object object = in.readObject();
				Request request = (Request) object;
				DynamicAdapter adapter = server.getAdapterMap().get(
						request.getObject());
				if (adapter != null) {
					Reply reply = adapter.performeRequest(request);
					if (reply.getResult() instanceof RemoteAble)
						configureReply(reply, reply.getResult());
					// if (reply.getResult() instanceof Collection)
					// configureReplyCollection(reply);
					if (!request.isOneway())
						out.writeObject(reply);
				} else {
					Reply r = new Reply();
					r.setError(new RemoteException(request.getObject(),
							"no such object found: " + request.getObject()));
					out.writeObject(r);
				}
			}
		} catch (IOException e) {
			if (e instanceof EOFException)
				RMILogger.performLog(LogPriority.WARNING,
						"client connection closed by client", null);
			else if (e instanceof SocketException)
				RMILogger.performLog(LogPriority.WARNING,
						"client connection closed by server", null);
			else
				e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void configureReplyCollection(Reply reply) {
		Collection c = (Collection) reply.getResult();
		for (Object o : c)
			configureReply(reply, o);
	}

	/**
	 * set id for remoteable result. if there is already an adapter use the old
	 * one.
	 * 
	 * @param reply
	 */
	private void configureReply(Reply reply, Object result) {
		if (server.getAdapterObjectIdMap().containsKey(result)) {
			reply.addNewId(server.getAdapterObjectIdMap().get(result));
			reply.setResult(null);
		} else {
			DynamicAdapter dynamicAdapter = new DynamicAdapter(result, server);
			String id = getNextId();
			server.getAdapterMap().put(id, dynamicAdapter);
			server.getAdapterObjectIdMap().put(result, id);
			reply.addNewId(id);
			reply.setResult(null);
		}
	}

	/**
	 * next id for result
	 * 
	 * @return id
	 */
	public String getNextId() {
		String id = "newsystem.result(" + ip + ":" + port + ":" + (++idCounter)
				+ ")";
		return id;
	}

	/**
	 * close the connection
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {
		socket.close();
	}

}
