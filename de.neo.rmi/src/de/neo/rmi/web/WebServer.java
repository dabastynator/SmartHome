package de.neo.rmi.web;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;

import de.neo.rmi.api.RMILogger;
import de.neo.rmi.api.RMILogger.LogPriority;
import de.neo.rmi.api.WebObject;
import de.neo.rmi.protokol.RemoteAble;

public class WebServer {

	private static WebServer mInstance;

	public static WebServer getInstance() {
		if (mInstance == null)
			mInstance = new WebServer();
		return mInstance;
	}

	int mPort = 5060;
	HttpServer mServer = null;

	private WebServer() {
		// TODO Auto-generated constructor stub
	}

	public void setPort(int port) {
		mPort = port;
	}

	/**
	 * Handle remote able object.
	 * 
	 * @param remoteAble
	 * @throws IOException
	 */
	public void handle(RemoteAble remoteAble) throws IOException {
		handle(remoteAble, null);
	}

	/**
	 * Handle the remoteable object. Every request must have token parameter.
	 * 
	 * @param remoteAble
	 * @param token
	 * @throws IOException
	 */
	public void handle(RemoteAble remoteAble, String token) throws IOException {
		if (mServer != null)
			throw new IllegalStateException("Webserver is already running.");
		WebObject webAnnotation = remoteAble.getClass().getAnnotation(WebObject.class);
		if (webAnnotation == null)
			throw new IllegalArgumentException("WebObject annotation missing for remoteable object");
		String path = "/" + webAnnotation.path();
		RMILogger.performLog(LogPriority.INFORMATION, "start handling remoteable object", mPort + path);
		mServer = HttpServer.create(new InetSocketAddress(mPort), 0);
		WebServerHandler handler = new WebServerHandler(remoteAble, path);
		handler.setSecurityToken(token);
		mServer.createContext(path, handler);
		mServer.setExecutor(null); // creates a default executor
		mServer.start();
	}

	public void shutdown() {
		RMILogger.performLog(LogPriority.INFORMATION, "shutdown webserver", null);
		mServer.stop(10);
		mServer = null;
	}

}
