package de.neo.rmi.transceiver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import de.neo.rmi.transceiver.AbstractReceiver.ReceiverState;

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
	 * list of progress listener
	 */
	protected List<SenderProgress> progressListener = new ArrayList<SenderProgress>();

	/**
	 * size of sending data (in bytes)
	 */
	private long sendingSize;

	/**
	 * current send progress (size of send data in bytes)
	 */
	private long sendingProgress;

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
			} catch (IOException e) {
				informException(e);
			}
		}
		serverPort.close();
	}

	/**
	 * get the list of progress listener
	 * 
	 * @return progress listener list
	 */
	public List<SenderProgress> getProgressListener() {
		return progressListener;
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
					if (msg == ReceiverState.CANCELD.ordinal()
							|| msg == ReceiverState.FINISHED.ordinal()) {
						socket.getOutputStream().close();
						socket.getInputStream().close();
						socket.close();
					}
				}
			} catch (IOException e) {
			}
		}

	}

	/**
	 * inform listeners about receive start
	 * 
	 * @param size
	 */
	protected void informStart(long size) {
		sendingSize = size;
		for (SenderProgress listener : progressListener)
			listener.startSending(size);
	}

	/**
	 * inform listeners about receive progress
	 * 
	 * @param size
	 */
	protected void informProgress(long size) {
		sendingProgress = size;
		for (SenderProgress listener : progressListener)
			listener.progressSending(size);
	}

	/**
	 * inform listeners about receive end
	 * 
	 * @param size
	 */
	protected void informEnd(long size) {
		for (SenderProgress listener : progressListener)
			listener.endSending(size);
	}

	/**
	 * inform all listener about occurred exception
	 * 
	 * @param e
	 */
	protected void informException(Exception e) {
		for (SenderProgress progress : progressListener)
			progress.exceptionOccurred(e);
	}

	/**
	 * get full size of sending data
	 * 
	 * @return size of sending data
	 */
	public long getFullSize() {
		return sendingSize;
	}

	/**
	 * get size of data already send
	 * 
	 * @return size of send data
	 */
	/**
	 * @return
	 */
	public long getCurrentProgress() {
		return sendingProgress;
	}
}
