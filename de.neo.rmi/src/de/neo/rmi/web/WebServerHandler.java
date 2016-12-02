package de.neo.rmi.web;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import de.neo.rmi.api.JSONUtils;
import de.neo.rmi.api.WebGet;
import de.neo.rmi.api.WebRequest;
import de.neo.rmi.dynamics.WebProxy;
import de.neo.rmi.protokol.RemoteAble;

public class WebServerHandler implements HttpHandler {

	private RemoteAble mRemoteable;
	private Map<String, Method> mRemoteMethods;
	private String mPath;
	private String mToken;
	private String mTokenParam;
	private String mEncoding;

	public WebServerHandler(RemoteAble remoteAble, String path) {
		mRemoteable = remoteAble;
		mPath = path;
		mTokenParam = "token";
		mEncoding = "UTF-8";
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

	private Object[] queryToParams(Method method, Map<String, String> paramMap) {
		List<Object> resultParams = new ArrayList<>();
		Annotation[][] annotations = method.getParameterAnnotations();
		Class<?>[] parameterTypes = method.getParameterTypes();
		for (int i = 0; i < annotations.length; i++) {
			WebGet annotation = WebProxy.findWebGetAnnotation(annotations[i]);
			if (annotation == null)
				throw new IllegalArgumentException("Parameter of method missing WebGet annotation.");
			Class<?> paramClass = parameterTypes[i];
			String strValue = paramMap.get(annotation.name());
			if (strValue == null && annotation.required())
				throw new IllegalArgumentException("Parameter missing: " + annotation.name());
			if (strValue == null)
				strValue = annotation.defaultvalue();
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

	@SuppressWarnings("unchecked")
	private JSONObject createJSONApi() {
		JSONObject result = new JSONObject();
		result.put("path", mPath);
		if (mToken != null && mToken.length() > 0)
			result.put("security", "Requires token");
		else
			result.put("security", "Requires no token");
		JSONArray jsonMethods = new JSONArray();
		for (String methodName : mRemoteMethods.keySet()) {
			JSONObject jsonMethod = new JSONObject();
			jsonMethod.put("name", methodName);
			JSONArray parameter = new JSONArray();
			Method method = mRemoteMethods.get(methodName);
			WebRequest methodAnnotation = method.getAnnotation(WebRequest.class);
			jsonMethod.put("description", methodAnnotation.description());
			Annotation[][] annotations = method.getParameterAnnotations();
			for (int i = 0; i < annotations.length; i++) {
				WebGet annotation = WebProxy.findWebGetAnnotation(annotations[i]);
				parameter.add(annotation.name());
			}
			jsonMethod.put("parameter", parameter);
			jsonMethods.add(jsonMethod);
		}
		result.put("methods", jsonMethods);
		return result;
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		String methodPath = exchange.getRequestURI().getPath();
		methodPath = methodPath.substring(mPath.length());
		if (methodPath.startsWith("/"))
			methodPath = methodPath.substring(1);
		String resultString = "";

		try {
			Map<String, String> paramMap = queryToMap(exchange.getRequestURI().getQuery());
			if (mToken != null && mToken.length() > 0)
				if (!mToken.equals(paramMap.get(mTokenParam)))
					throw new IllegalArgumentException("Access denied");
			if ("api".equals(methodPath)) {
				resultString = createJSONApi().toString();
			} else {
				Method method = mRemoteMethods.get(methodPath);
				if (method == null)
					throw new IllegalArgumentException("Could not find method for " + methodPath);
				Object[] params = queryToParams(method, paramMap);
				Object result = method.invoke(mRemoteable, params);
				resultString = JSONUtils.objectToJson(result).toString();
			}
		} catch (Exception e) {
			resultString = JSONUtils.exceptionToJson(e).toString();
		}
		byte[] bytes = resultString.getBytes();
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
		exchange.getResponseHeaders().set("Content-Type", "application/json charset=" + mEncoding);
		exchange.sendResponseHeaders(200, bytes.length);
		OutputStream os = exchange.getResponseBody();
		ioCopyStream(is, os);
		os.close();
	}

	private void ioCopyStream(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int len = in.read(buffer);
		while (len != -1) {
			out.write(buffer, 0, len);
			len = in.read(buffer);
		}
	}

	public String getPath() {
		return mPath;
	}

	public void setSecurityToken(String token) {
		mToken = token;
	}

}
