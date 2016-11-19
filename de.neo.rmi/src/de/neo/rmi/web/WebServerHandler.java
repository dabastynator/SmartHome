package de.neo.rmi.web;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import de.neo.rmi.api.WebGet;
import de.neo.rmi.api.WebRequest;
import de.neo.rmi.protokol.RemoteAble;

public class WebServerHandler implements HttpHandler {

	private RemoteAble mRemoteable;
	private Map<String, Method> mRemoteMethods;

	public WebServerHandler(RemoteAble remoteAble) {
		mRemoteable = remoteAble;
		mRemoteMethods = new HashMap<>();
		for (Method method : mRemoteable.getClass().getMethods()) {
			WebRequest annotation = method.getAnnotation(WebRequest.class);
			if (annotation != null)
				mRemoteMethods.put(annotation.path(), method);
		}
	}

	public Map<String, String> queryToMap(String query) {
		Map<String, String> result = new HashMap<String, String>();
		if (query == null)
			return result;
		for (String param : query.split("&")) {
			String pair[] = param.split("=");
			if (pair.length > 1) {
				result.put(pair[0], pair[1]);
			} else {
				result.put(pair[0], "");
			}
		}
		return result;
	}

	private Object[] queryToParams(Method method, String query) {
		Map<String, String> requestParams = queryToMap(query);
		List<Object> resultParams = new ArrayList<>();
		Annotation[][] annotations = method.getParameterAnnotations();
		Class<?>[] parameterTypes = method.getParameterTypes();
		for (int i = 0; i < annotations.length; i++) {
			WebGet annotation = findWebGetAnnotation(annotations[i]);
			if (annotation == null)
				throw new IllegalArgumentException("Parameter of method missing WebGet annotation.");
			Class<?> paramClass = parameterTypes[i];
			String strValue = requestParams.get(annotation.name());
			if (strValue == null)
				throw new IllegalArgumentException("Parameter missing: " + annotation.name());
			if (paramClass.equals(int.class) || paramClass.equals(Integer.class))
				resultParams.add(Integer.valueOf(strValue));
			else if (paramClass.equals(float.class) || paramClass.equals(Float.class))
				resultParams.add(Float.valueOf(strValue));
			else if (paramClass.equals(double.class) || paramClass.equals(Double.class))
				resultParams.add(Double.valueOf(strValue));
			else if (paramClass.equals(String.class))
				resultParams.add(strValue);
			else
				throw new IllegalArgumentException(
						"Parameter-type of method not supported: " + parameterTypes[i].getSimpleName());
		}
		return resultParams.toArray();
	}

	private WebGet findWebGetAnnotation(Annotation[] annotations) {
		for (Annotation a : annotations)
			if (a instanceof WebGet)
				return (WebGet) a;
		return null;
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		String methodPath = exchange.getRequestURI().getPath();
		if (methodPath.lastIndexOf('/') >= 0)
			methodPath = methodPath.substring(methodPath.lastIndexOf('/') + 1);
		Method method = mRemoteMethods.get(methodPath);
		String resultString = "";
		try {
			if (method == null)
				throw new IllegalArgumentException("Could not find method for " + methodPath);
			Object[] params = queryToParams(method, exchange.getRequestURI().getQuery());
			Object result = method.invoke(mRemoteable, params);
			resultString = JSON.createByObject(result).toString();
		} catch (Exception e) {
			resultString = JSON.createByException(e).toString();
		}
		exchange.sendResponseHeaders(200, resultString.length());
		OutputStream os = exchange.getResponseBody();
		os.write(resultString.getBytes());
		os.close();
	}

}
