package de.neo.rmi.dynamics;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import de.neo.rmi.api.Oneway;
import de.neo.rmi.api.Server;
import de.neo.rmi.handler.ServerConnection;
import de.neo.rmi.handler.ServerConnection.ConnectionSocket;
import de.neo.rmi.protokol.RemoteAble;
import de.neo.rmi.protokol.RemoteException;
import de.neo.rmi.protokol.Reply;
import de.neo.rmi.protokol.Request;
import de.neo.rmi.protokol.ServerPort;
import de.neo.rmi.protokol.Request.Type;

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
	 * server connection of this global object id
	 */
	private ServerConnection serverConnection;

	/**
	 * server on witch provides the proxy
	 */
	private Server server;

	/**
	 * the static counter counts new global objects to number them.
	 */
	public static int counter = 0;

	/**
	 * Allocates new proxy with given id, server connection and server.
	 * 
	 * @param id
	 * @param sc
	 */
	public DynamicProxy(String id, ServerConnection sc, Server server) {
		this.id = id;
		this.serverConnection = sc;
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
		checkParameter(vals, method);
		request.setParams(vals);
		request.setType(Type.NORMAL);
		if (method.getAnnotation(Oneway.class) != null)
			request.setType(Type.ONEWAY);
		return performeRequest(request);
	}

	/**
	 * perform the given request.
	 * 
	 * @param request
	 * @return result of the request
	 * @throws Throwable
	 */
	private Object performeRequest(Request request) throws Throwable {
		Reply reply = null;
		ConnectionSocket socket = null;
		Throwable resultException = null;
		for (int i = 0; i < server.getConnectionSocketCount(); i++) {
			try {
				socket = serverConnection.getFreeConnectionSocket();
			} catch (IOException e) {
				throw new RemoteException(id, e.getMessage());
			}
			try {
				try {
					socket.getOutput().writeObject(request);
					socket.getOutput().flush();
					socket.getOutput().reset();
					if (request.getType() == Type.ONEWAY)
						return null;
					reply = (Reply) socket.getInput().readObject();
					if (reply == null)
						resultException = new RemoteException(id,
								"null returned");
					else if (reply.getError() != null)
						resultException = reply.getError();
					else if (reply.getReturnType() != null
							&& reply.getNewId() != null) {
						ServerConnection sp = server.connectToServer(reply
								.getServerPort());
						return sp.createProxy(reply.getNewId(),
								reply.getReturnType(),
								!reply.isCreateNewAdapter());
					} else
						return reply.getResult();
					i = server.getConnectionSocketCount();
				} catch (IOException e) {
					socket.disconnect();
					resultException = new RemoteException(id, e.getMessage());
				}
			} catch (Exception e) {
				resultException = new RemoteException(id, e.getMessage());
			} finally {
				socket.free();
			}
		}
		throw resultException;
	}

	/**
	 * check parameters for remoteable object -> create adapter
	 * 
	 * @param paramters
	 */
	private void checkParameter(Object[] paramters, Method method) {
		if (paramters != null)
			for (int i = 0; i < paramters.length; i++) {
				if (paramters[i] instanceof Proxy) {
					DynamicProxy dp = (DynamicProxy) Proxy
							.getInvocationHandler(paramters[i]);
					Reply r = new Reply();
					r.addNewId(dp.getId());
					r.setServerPort(dp.getServerPort());
					r.setReturnType(method.getParameterTypes()[i]);
					paramters[i] = r;
				} else if (paramters[i] instanceof RemoteAble) {
					String objId = server.getAdapterObjectIdMap().get(
							paramters[i]);
					Reply r = new Reply();
					r.setCreateNewAdapter(objId == null);
					if (objId == null) {
						objId = getNextId();
						DynamicAdapter adapter = new DynamicAdapter(objId,
								paramters[i], server);
						server.getAdapterMap().put(objId, adapter);
						server.getAdapterObjectIdMap().put(paramters[i], objId);
					}
					r.addNewId(objId);
					r.setServerPort(new ServerPort(server.getServerPort()));
					r.setReturnType(method.getParameterTypes()[i]);
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

	public ServerPort getServerPort() {
		return serverConnection.getServerPort();
	}

	@Override
	public boolean equals(Object obj) {
		String me = toString();
		String other = obj.toString();
		return me.equals(other);
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
