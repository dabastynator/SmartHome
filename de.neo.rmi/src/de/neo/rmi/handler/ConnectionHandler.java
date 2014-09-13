package de.neo.rmi.handler;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Proxy;
import java.net.Socket;
import java.net.SocketException;
import java.util.Collection;

import de.neo.rmi.api.RMILogger;
import de.neo.rmi.api.Server;
import de.neo.rmi.api.RMILogger.LogPriority;
import de.neo.rmi.dynamics.DynamicAdapter;
import de.neo.rmi.dynamics.DynamicProxy;
import de.neo.rmi.protokol.RemoteAble;
import de.neo.rmi.protokol.RemoteException;
import de.neo.rmi.protokol.Reply;
import de.neo.rmi.protokol.Request;
import de.neo.rmi.protokol.ServerPort;
import de.neo.rmi.protokol.Request.Type;

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
				"Incoming connection from client", null);
		try {
			while (true) {
				Object object = in.readObject();
				Request request = (Request) object;
				if (request.getType() == Type.CLOSE) {
					server.closeConnectionTo((ServerPort) request.getParams()[0]);
					throw new EOFException();
				}
				DynamicAdapter adapter = server.getAdapterMap().get(
						request.getObject());
				if (adapter != null) {
					Reply reply = adapter.performeRequest(request);
					if (reply.getResult() instanceof RemoteAble)
						configureReply(reply, reply.getResult());
					// if (reply.getResult() instanceof Collection)
					// configureReplyCollection(reply);
					if (request.getType() == Type.NORMAL){
						out.writeObject(reply);
						out.flush();
						out.reset();
					}
				} else {
					Reply r = new Reply();
					r.setError(new RemoteException(request.getObject(),
							"no such object found: " + request.getObject()
									+ " at " + server.getServerPort()));
					out.writeObject(r);
					out.flush();
					out.reset();
				}
			}
		} catch (IOException e) {
			if (e instanceof EOFException)
				RMILogger.performLog(LogPriority.WARNING,
						"Client connection closed by client", null);
			else if (e instanceof SocketException)
				RMILogger.performLog(LogPriority.WARNING,
						"Client connection closed by server", null);
			else
				RMILogger.performLog(
						LogPriority.ERROR,
						"Unknown request error: "
								+ e.getClass().getSimpleName() + ": "
								+ e.getMessage(), null);
		} catch (ClassNotFoundException e) {
			RMILogger.performLog(LogPriority.ERROR,
					"Request-ClassNotFoundException: " + e.getMessage(), null);
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
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
	 * @param adapter
	 * 
	 * @param reply
	 */
	private void configureReply(Reply reply, Object result) {
		if (reply.getResult() instanceof Proxy) {
			DynamicProxy dp = (DynamicProxy) Proxy.getInvocationHandler(reply
					.getResult());
			reply.addNewId(dp.getId());
			reply.setServerPort(dp.getServerPort());
			reply.setResult(null);
		} else if (server.getAdapterObjectIdMap().containsKey(result)) {
			reply.addNewId(server.getAdapterObjectIdMap().get(result));
			reply.setResult(null);
			reply.setServerPort(server.getServerPort());
		} else {
			String id = getNextId();
			DynamicAdapter dynamicAdapter = new DynamicAdapter(id, result,
					server);
			server.getAdapterMap().put(id, dynamicAdapter);
			server.getAdapterObjectIdMap().put(result, id);
			reply.addNewId(id);
			reply.setServerPort(server.getServerPort());
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
	 */
	public void close() {
		try {
			socket.close();
		} catch (IOException e) {
		}
	}

}
