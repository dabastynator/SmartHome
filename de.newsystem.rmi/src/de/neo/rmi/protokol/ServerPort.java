package de.neo.rmi.protokol;

import java.io.Serializable;

/**
 * server port holds connection and streams
 * 
 * @author sebastian
 */
public class ServerPort implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3507479097260925399L;

	/**
	 * ip of server
	 */
	private String ip;

	/**
	 * port of server
	 */
	private int port;

	/**
	 * counter for parameter ids
	 */
	private int counter = 0;

	/**
	 * allocate new server port
	 * 
	 * @param ip
	 * @param port
	 */
	public ServerPort(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}

	/**
	 * allocate new server
	 * 
	 * @param serverPort
	 */
	public ServerPort(ServerPort serverPort) {
		this(serverPort.getIp(), serverPort.getPort());
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public int hashCode() {
		return ip.hashCode() + port;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ServerPort))
			return false;
		ServerPort sp = (ServerPort) obj;
		return sp.getIp().equals(ip) && sp.getPort() == port;
	}

	/**
	 * generate new id for parameter
	 * 
	 * @return id
	 */
	public String getNextId() {
		String id = "newsystem.parameter(" + ip + ":" + port + "/"
				+ (counter++) + ")";
		return id;
	}

	@Override
	public String toString() {
		return ip + ":" + port;
	}

}
