package de.neo.remote.web;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.json.simple.parser.JSONParser;

import de.neo.remote.rmi.RMILogger;
import de.neo.remote.rmi.RMILogger.LogPriority;
import de.neo.remote.rmi.RemoteException;

public class WebProxy implements InvocationHandler {

	private String mSecurityToken;
	private String mEndPoint;

	public WebProxy(String endPoint, String securityToken) {
		mEndPoint = endPoint;
		if (!mEndPoint.endsWith("/"))
			mEndPoint = mEndPoint + "/";
		mSecurityToken = securityToken;
	}

	public static WebParam findWebGetAnnotation(Annotation[] annotations) {
		for (Annotation a : annotations)
			if (a instanceof WebParam)
				return (WebParam) a;
		return null;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] parameter) throws Throwable {
		String url = createUrl(method, parameter);
		String result = doWebRequest(url, method, parameter);
		Object json = new JSONParser().parse(result);
		WebRequest request = method.getAnnotation(WebRequest.class);
		JSONUtils.checkForException(json);
		return JSONUtils.jsonToObject(method.getReturnType(), json, request, null);
	}

	public static String doWebRequest(String urlToRead, Method method, Object[] parameter) throws RemoteException {
		try {
			StringBuilder result = new StringBuilder();
			URL url;
			boolean hasPayload = false;

			url = new URL(urlToRead);

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			WebRequest request = method.getAnnotation(WebRequest.class);
			if (request.type() == WebRequest.Type.Get) {
				conn.setRequestMethod("GET");
			} else if (request.type() == WebRequest.Type.Post) {
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Content-Type", request.content());
			} else {
				throw new RemoteException("Unsupported WebRequest type: " + request.type());
			}
			if (parameter != null) {
				for (int i = 0; i < parameter.length; i++) {
					if (parameter[i] != null) {
						WebParam paramAnnotation = findWebGetAnnotation(method.getParameterAnnotations()[i]);
						if (paramAnnotation == null)
							throw new RemoteException("WebRequest for method '" + method.getName()
									+ "' not supported. Parameter " + i + " not annotated.");
						if (paramAnnotation.type() == WebParam.Type.Header) {
							String name = paramAnnotation.name();
							String value = parameter[i].toString();
							conn.setRequestProperty(name, value);
						}
						hasPayload |= paramAnnotation.type() == WebParam.Type.Payload;
					}
				}
			}
			if (request.type() == WebRequest.Type.Post && hasPayload) {
				writePayload(conn, method, parameter);
			}
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
			rd.close();
			return result.toString();
		} catch (IOException e) {
			throw new RemoteException(e.getClass().getSimpleName() + ": " + e.getMessage(), e);
		}
	}

	private static void writePayload(HttpURLConnection conn, Method method, Object[] parameter) throws IOException, RemoteException {
		conn.setDoOutput(true);
		OutputStream os = conn.getOutputStream();
		BufferedWriter writer = new BufferedWriter(
		        new OutputStreamWriter(os, "UTF-8"));
		StringBuilder sb = new StringBuilder("{");
		String prefix = "";
		if (parameter != null) {
			for (int i = 0; i < parameter.length; i++) {
				if (parameter[i] != null) {
					WebParam paramAnnotation = findWebGetAnnotation(method.getParameterAnnotations()[i]);
					if (paramAnnotation == null)
						throw new RemoteException("WebRequest for method '" + method.getName()
								+ "' not supported. Parameter " + i + " not annotated.");
					if (paramAnnotation.type() == WebParam.Type.Payload) {
						String name = paramAnnotation.name();
						String value = parameter[i].toString();
						sb.append(prefix);
						sb.append("\"" + URLEncoder.encode(name, "UTF-8") + "\": \"" + URLEncoder.encode(value, "UTF-8") +"\"");
						prefix = ",";
					}
				}
			}
		}
		sb.append("}");
		writer.write(sb.toString());
		writer.flush();
		writer.close();
		os.close();
	}

	private String createUrl(Method method, Object[] parameter) throws RemoteException {
		StringBuilder sb = new StringBuilder();
		WebRequest annotation = method.getAnnotation(WebRequest.class);
		if (annotation == null)
			throw new RemoteException(
					"WebRequest for method '" + method.getName() + "' not supported. Method not annotated.");
		String path = annotation.path();
		boolean firstParam = true;
		if (mSecurityToken != null && mSecurityToken.length() > 0) {
			sb.append("?token=");
			sb.append(mSecurityToken);
			firstParam = false;
		}
		if (parameter != null) {
			for (int i = 0; i < parameter.length; i++) {
				if (parameter[i] != null) {
					WebParam paramAnnotation = findWebGetAnnotation(method.getParameterAnnotations()[i]);
					if (paramAnnotation == null)
						throw new RemoteException("WebRequest for method '" + method.getName()
								+ "' not supported. Parameter " + i + " not annotated.");

					String name = paramAnnotation.name();

					try {
						// Encode parameter two times to avoid this signs to be
						// interpreted as query parts
						String p = URLEncoder.encode(parameter[i].toString(), "UTF-8");
						String value = URLEncoder.encode(p, "UTF-8");

						if (paramAnnotation.type() == WebParam.Type.ReplaceUrl) {
							path = path.replace("${" + paramAnnotation.name() + "}", p);
						} else if (paramAnnotation.type() == WebParam.Type.GetParameter) {
							if (firstParam)
								sb.append("?");
							else
								sb.append("&");
							sb.append(name);
							sb.append("=");
							sb.append(value);
							firstParam = false;
						}
					} catch (UnsupportedEncodingException e) {
						RMILogger.performLog(LogPriority.ERROR, e.getClass().getSimpleName() + ": " + e.getMessage(),
								"WebProxy");
					}
				}
			}
		}
		sb.insert(0, path);
		sb.insert(0, mEndPoint);
		return sb.toString();
	}

}
