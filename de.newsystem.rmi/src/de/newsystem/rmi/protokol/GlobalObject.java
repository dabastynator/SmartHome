package de.newsystem.rmi.protokol;

import java.io.Serializable;


/**
 * global object for comunication with the registry
 * @author sebastian
 */
public class GlobalObject implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7887670852637957404L;
	
	
	/**
	 * object id
	 */
	private String id;
	
	/**
	 * server port 
	 */
	private ServerPort serverPort;
	
	public GlobalObject(String id, ServerPort serverPort) {
		this.id = id;
		this.serverPort = serverPort;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public ServerPort getServerPort() {
		return serverPort;
	}
	public void setServerPort(ServerPort serverPort) {
		this.serverPort = serverPort;
	}
	
		
}
