package de.newsystem.rmi.dynamics;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import de.newsystem.rmi.api.Server;
import de.newsystem.rmi.protokol.RemoteException;
import de.newsystem.rmi.protokol.Reply;
import de.newsystem.rmi.protokol.Request;
import de.newsystem.rmi.protokol.ServerPort;

/**
 * adapter
 * 
 * @author sebastian
 */
public class DynamicAdapter {

	/**
	 * current object
	 */
	private Object object;

	/**
	 * current template
	 */
	private Class template;

	/**
	 * primitive Map
	 */
	private static Map<Class, Class> primitiveMap = new HashMap<Class, Class>();
	static {
		primitiveMap.put(boolean.class, Boolean.class);
		primitiveMap.put(byte.class, Byte.class);
		primitiveMap.put(char.class, Character.class);
		primitiveMap.put(short.class, Short.class);
		primitiveMap.put(int.class, Integer.class);
		primitiveMap.put(long.class, Long.class);
		primitiveMap.put(float.class, Float.class);
		primitiveMap.put(double.class, Double.class);
	}

	/**
	 * allocates new adapter
	 * 
	 * @param object
	 */
	public DynamicAdapter(Object object) {
		this.object = object;
		template = object.getClass();
	}

	/**
	 * executes request on object
	 * 
	 * @param request
	 * @return reply
	 */
	public Reply performeRequest(Request request) {
		Reply reply = new Reply();
		try {
			Method method = findMethod(request);
			if (method == null){
				reply.setError(new RemoteException(request.getObject(),
						"no such method found: " + request.getMethod()));
				return reply;
			}
			Object result = method.invoke(object, request.getParams());
			reply.setResult(result);
			if (method.getReturnType() != void.class)
				reply.setReturnType(method.getReturnType());
		} catch (SecurityException e) {
			reply.setError(e);
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			reply.setError(e);
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			reply.setError(e);
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			reply.setError(e);
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			reply.setError(e.getTargetException());
		} catch (UnknownHostException e) {
			reply.setError(e);
			e.printStackTrace();
		} catch (IOException e) {
			reply.setError(e);
			e.printStackTrace();
		}
		return reply;
	}

	/**
	 * find the compatible method for the request. for remoteable parameter a
	 * proxy will be created.
	 * 
	 * @param request
	 * @return method
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	private Method findMethod(Request request) throws SecurityException,
			NoSuchMethodException, UnknownHostException, IOException {
		Class[] types = new Class[] {};
		if (request.getParams() != null)
			types = new Class[request.getParams().length];
		for (int i = 0; i < types.length; i++) {
			if (request.getParams()[i] instanceof Reply) {
				Reply r = ((Reply) request.getParams()[i]);
				types[i] = r.getReturnType();
				ServerPort sp = Server.getServer().connectToServer(
						r.getServerPort());
				request.getParams()[i] = Server.getServer().createProxy(
						r.getNewId(), sp, r.getReturnType());
			} else
				types[i] = request.getParams()[i].getClass();
		}
		return getCompatibleMethod(template, request.getMethod(), types);
	}

	/**
	 * find the compatible method.
	 * 
	 * @param c
	 * @param methodName
	 * @param paramTypes
	 * @return method
	 */
	public static Method getCompatibleMethod(Class c, String methodName,
			Class... paramTypes) {
		Method[] methods = c.getMethods();
		for (int i = 0; i < methods.length; i++) {
			Method m = methods[i];

			if (!m.getName().equals(methodName)) {
				continue;
			}

			Class<?>[] actualTypes = m.getParameterTypes();
			if (actualTypes.length != paramTypes.length) {
				continue;
			}

			boolean found = true;
			for (int j = 0; j < actualTypes.length; j++) {
				if (!actualTypes[j].isAssignableFrom(paramTypes[j])) {
					if (actualTypes[j].isPrimitive()) {
						found = primitiveMap.get(actualTypes[j]).equals(
								paramTypes[j]);
					} else if (paramTypes[j].isPrimitive()) {
						found = primitiveMap.get(paramTypes[j]).equals(
								actualTypes[j]);
					}
				}

				if (!found) {
					break;
				}
			}

			if (found) {
				return m;
			}
		}

		return null;
	}

}
