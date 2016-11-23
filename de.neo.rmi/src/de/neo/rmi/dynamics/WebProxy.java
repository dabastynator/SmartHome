package de.neo.rmi.dynamics;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.json.simple.parser.JSONParser;

import de.neo.rmi.api.RMILogger;
import de.neo.rmi.api.RMILogger.LogPriority;
import de.neo.rmi.api.WebGet;
import de.neo.rmi.api.WebRequest;
import de.neo.rmi.protokol.RemoteException;
import de.neo.rmi.web.JSONUtils;

public class WebProxy implements InvocationHandler {

	private String mSecurityToken;
	private String mEndPoint;

	public WebProxy(String endPoint, String securityToken) {
		mEndPoint = endPoint;
		if (!mEndPoint.endsWith("/"))
			mEndPoint = mEndPoint + "/";
		mSecurityToken = securityToken;
	}

	public static WebGet findWebGetAnnotation(Annotation[] annotations) {
		for (Annotation a : annotations)
			if (a instanceof WebGet)
				return (WebGet) a;
		return null;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] parameter) throws Throwable {
		String url = createUrl(method, parameter);
		String result = doWebGetRequest(url);
		Object json = new JSONParser().parse(result);
		WebRequest request = method.getAnnotation(WebRequest.class);
		return JSONUtils.jsonToObject(method.getReturnType(), json, request, null);
	}

	public static String doWebGetRequest(String urlToRead) throws Exception {
		StringBuilder result = new StringBuilder();
		URL url = new URL(urlToRead);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String line;
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}
		rd.close();
		return result.toString();
	}

	private String createUrl(Method method, Object[] parameter) throws RemoteException {
		StringBuilder sb = new StringBuilder(mEndPoint);
		WebRequest annotation = method.getAnnotation(WebRequest.class);
		if (annotation == null)
			throw new RemoteException("WebRequest",
					"WebRequest for method '" + method.getName() + "' not supported. Method not annotated.");
		sb.append(annotation.path());
		boolean firstParam = true;
		if (mSecurityToken != null && mSecurityToken.length() > 0) {
			sb.append("?token=");
			sb.append(mSecurityToken);
			firstParam = false;
		}
		if (parameter != null) {
			for (int i = 0; i < parameter.length; i++) {
				WebGet paramAnnoration = findWebGetAnnotation(method.getParameterAnnotations()[i]);
				if (paramAnnoration == null)
					throw new RemoteException("WebRequest", "WebRequest for method '" + method.getName()
							+ "' not supported. Parameter " + i + " not annotated.");
				if (firstParam)
					sb.append("?");
				else
					sb.append("&");
				sb.append(paramAnnoration.name());
				sb.append("=");
				if (parameter[i] != null) {
					try {
						sb.append(URLEncoder.encode(parameter[i].toString(), "UTF-8"));
					} catch (UnsupportedEncodingException e) {
						RMILogger.performLog(LogPriority.ERROR, e.getClass().getSimpleName() + ": " + e.getMessage(),
								"WebProxy");
					}
				}
			}
		}
		return sb.toString();
	}

}
