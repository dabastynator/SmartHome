package de.newsystem.rmi.transceiver;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * the sender sends data via a tcp connection. the count of clients can be
 * specified.
 * 
 * @author sebastian
 */
public abstract class AbstractSender {

	/**
	 * port on witch the tcp socket will be build.
	 */
	private int port;

	/**
	 * times the data will be send, -1 equals infinite
	 */
	private int times;

	/**
	 * allocate new sender on given port, the data will be send given times
	 * 
	 * @param port
	 * @param times
	 */
	public AbstractSender(int port, int times) {
		this.port = port;
		this.times = times;
	}

	/**
	 * allocate new sender on given port, the data will be send to infinite
	 * clients.
	 * 
	 * @param port
	 */
	public AbstractSender(int port) {
		this(port, -1);
	}

	/**
	 * start the sender asynchronous in own thread
	 */
	public void sendAsync() {
		new Thread() {
			public void run() {
				try {
					sendSync();
				} catch (IOException e) {
				}
			};
		}.start();
	}

	/**
	 * start the sender synchronous
	 * 
	 * @throws IOException
	 */
	public void sendSync() throws IOException {
		ServerSocket serverPort = new ServerSocket(port);
		for (int i = 0; i < times || times == -1; i++) {
			Socket socket = serverPort.accept();
			writeData(socket.getOutputStream());
			socket.close();
		}
		serverPort.close();
	}

	/**
	 * write data to stream
	 * 
	 * @param outputStream
	 */
	protected abstract void writeData(OutputStream outputStream);
}
