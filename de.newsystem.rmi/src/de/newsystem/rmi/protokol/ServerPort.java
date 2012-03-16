package de.newsystem.rmi.protokol;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * server port holds connection and streams
 * @author sebastian
 */
public class ServerPort implements Serializable{

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
	 * socket of connection 
	 */
	private Socket socket;
	
	/**
	 * inputstream of connection 
	 */
	private ObjectOutputStream out;
	
	/**
	 * outputstream of connection
	 */
	private ObjectInputStream in;

	/**
	 * counter for parameter ids
	 */
	private int counter = 0;

	/**
	 * allocate new server port
	 * @param ip
	 * @param port
	 */
	public ServerPort(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}

	/**
	 * allocate new server port with connection and streams
	 * @param socket
	 * @throws IOException
	 */
	public ServerPort(Socket socket) throws IOException {
		this.socket = socket;
		out = new ObjectOutputStream(socket.getOutputStream());
		in = new ObjectInputStream(socket.getInputStream());
	}

	/**
	 * allocate new server
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
	 * create connection to server
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public void connect() throws UnknownHostException, IOException {
		socket = new Socket(ip, port);
		out = new ObjectOutputStream(socket.getOutputStream());
		in = new ObjectInputStream(socket.getInputStream());
	}
	
	/**
	 * close connection
	 * @throws IOException
	 */
	public void disconnect() throws IOException{
		socket.close();
	}

	public ObjectInputStream getInput() {
		return in;
	}

	public ObjectOutputStream getOutput() {
		return out;
	}

	/**
	 * generate new id for parameter
	 * @return id
	 */
	public String getNextId() {
		String id = "newsystem.parameter(" + ip + ":" + port + "/" + (counter ++) + ")";
		System.out.println(id);
		return id;
	}

	public void close() throws IOException {
		if (socket != null)
			socket.close();
	}

	public Socket getSocket() {
		return socket;
	}

	@Override
	public String toString() {
		return ip + ":" + port;
	}
	
}
