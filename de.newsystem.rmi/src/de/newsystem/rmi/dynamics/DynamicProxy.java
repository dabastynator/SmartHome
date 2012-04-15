package de.newsystem.rmi.dynamics;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import de.newsystem.rmi.api.Server;
import de.newsystem.rmi.api.oneway;
import de.newsystem.rmi.protokol.RemoteAble;
import de.newsystem.rmi.protokol.RemoteException;
import de.newsystem.rmi.protokol.Reply;
import de.newsystem.rmi.protokol.Request;
import de.newsystem.rmi.protokol.ServerPort;

/**
 * proxy
 * 
 * @author sebastian
 * 
 */
public class DynamicProxy implements InvocationHandler {

	/**
	 * id of the object
	 */
	private String id;

	/**
	 * outputstream
	 */
	private ObjectOutputStream out;

	/**
	 * inputstream
	 */
	private ObjectInputStream in;

	/**
	 * serverport
	 */
	private ServerPort serverPort;

	/**
	 * server on witch provides the proxy
	 */
	private Server server;

	private static int counter = 0;

	/**
	 * allocates new proxy
	 * 
	 * @param id
	 * @param serverPort
	 */
	public DynamicProxy(String id, ServerPort serverPort, Server server) {
		this.id = id;
		in = serverPort.getInput();
		out = serverPort.getOutput();
		this.serverPort = serverPort;
		this.server = server;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] vals)
			throws Throwable {
		if (method.getName().equals("equals") && vals != null
				&& vals.length == 1)
			return equals(vals[0]);
		if (method.getName().equals("hashCode") && vals == null)
			return hashCode();
		if (method.getName().equals("toString") && vals == null)
			return toString();
		Request request = new Request(id, method.getName());
		checkParameter(vals);
		request.setParams(vals);
		request.setOneway(method.getAnnotation(oneway.class) != null);
		Reply reply = null;
		try {
			try {
				out.writeObject(request);
				if (request.isOneway())
					return null;
				reply = (Reply) in.readObject();
			} catch (IOException e) {
				serverPort.close();
				serverPort = server.connectToServer(serverPort);
				in = serverPort.getInput();
				out = serverPort.getOutput();
				out.writeObject(request);
				if (request.isOneway())
					return null;
				reply = (Reply) in.readObject();
			}
		} catch (Exception e) {
			throw new RemoteException(id, e.getMessage());
		}
		if (reply.getError() == null) {
			if (reply.getReturnType() != null && reply.getNewId() != null)
				return server.createProxy(reply.getNewId(), serverPort,
						reply.getReturnType());
			else
				return reply.getResult();
		} else
			throw reply.getError();
	}

	/**
	 * check parameters for remoteable object -> create adapter
	 * 
	 * @param paramters
	 */
	private void checkParameter(Object[] paramters) {
		if (paramters != null)
			for (int i = 0; i < paramters.length; i++) {
				if (paramters[i] instanceof RemoteAble) {
					String objId = server.getAdapterObjectIdMap().get(
							paramters[i]);
					if (objId == null) {
						DynamicAdapter adapter = new DynamicAdapter(
								paramters[i], server);
						objId = getNextId();
						server.getAdapterMap().put(objId, adapter);
						server.getAdapterObjectIdMap().put(paramters[i], objId);
					}
					Reply r = new Reply();
					r.setNewId(objId);
					r.setServerPort(new ServerPort(server.getServerPort()));
					r.setReturnType(paramters[i].getClass().getInterfaces()[0]);
					paramters[i] = r;

				}
			}
	}

	private String getNextId() {
		String id = "newsystem.parameter(" + server.getServerPort() + ":"
				+ (counter++) + ")";
		return id;
	}

	public String getId() {
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		return toString().equals(obj.toString());
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public String toString() {
		return id;
	}

}
