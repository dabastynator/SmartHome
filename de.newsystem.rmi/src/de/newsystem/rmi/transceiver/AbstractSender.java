package de.newsystem.rmi.transceiver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import de.newsystem.rmi.transceiver.AbstractReceiver.ReceiverState;

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
		this(port, 1);
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
			try {
				Socket socket = serverPort.accept();
				new UploadObserver(socket).start();
				writeData(socket.getOutputStream());
				socket.close();
			} catch (IOException e) {

			}
		}
		serverPort.close();
	}

	/**
	 * write data to stream
	 * 
	 * @param outputStream
	 */
	protected abstract void writeData(OutputStream outputStream)
			throws IOException;

	public class UploadObserver extends Thread {

		private Socket socket;

		public UploadObserver(Socket socket) {
			this.socket = socket;
		}

		public void run() {
			try {
				InputStream input = socket.getInputStream();
				int msg;
				while ((msg = input.read()) != -1) {
					if (msg == ReceiverState.CANCELD.ordinal()) {
						socket.getOutputStream().close();
						socket.getInputStream().close();
						socket.close();
					}
				}
			} catch (IOException e) {
			}
		}

	}
}
