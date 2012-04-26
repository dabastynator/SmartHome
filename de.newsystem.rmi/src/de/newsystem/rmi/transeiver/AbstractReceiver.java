package de.newsystem.rmi.transeiver;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * the receiver receives date via a tcp connection.
 * 
 * @author sebastian
 */
public abstract class AbstractReceiver {

	/**
	 * port of the sender
	 */
	protected int port;

	/**
	 * ip of the sender
	 */
	protected String ip;

	/**
	 * list of progress listener
	 */
	protected List<ReceiverProgress> progressListener = new ArrayList<ReceiverProgress>();

	/**
	 * byte block to inform progress listener
	 */
	protected long progressStep;

	/**
	 * allocate new receiver. the receiver receives from specified ip and port.
	 * inform listener about progress specified in progressStep.
	 * 
	 * @param ip
	 * @param port
	 * @param progressStep
	 */
	public AbstractReceiver(String ip, int port, long progressStep) {
		this.ip = ip;
		this.port = port;
		this.progressStep = progressStep;
	}

	/**
	 * allocate new receiver. the receiver receives from specified ip and port.
	 * 
	 * @param ip
	 * @param port
	 */
	public AbstractReceiver(String ip, int port) {
		this(ip, port, Long.MAX_VALUE);
	}

	/**
	 * receive data asynchronous in own thread
	 */
	public void receiveAsync() {
		new Thread() {
			public void run() {
				try {
					receiveSync();
				} catch (UnknownHostException e) {
				} catch (IOException e) {
				}
			};
		}.start();
	}

	/**
	 * receive data synchronous
	 * 
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public void receiveSync() throws UnknownHostException, IOException {
		Socket socket = new Socket(ip, port);
		receiveData(socket.getInputStream());
	}

	/**
	 * receive data from stream
	 * 
	 * @param inputStream
	 */
	protected abstract void receiveData(InputStream inputStream);

	/**
	 * @return list of progress listener
	 */
	public List<ReceiverProgress> getProgressListener() {
		return progressListener;
	}

	/**
	 * inform listeners about receive start
	 * 
	 * @param size
	 */
	protected void informStart(long size) {
		for (ReceiverProgress listener : progressListener)
			listener.startReceive(size);
	}

	/**
	 * inform listeners about receive progress
	 * 
	 * @param size
	 */
	protected void informProgress(long size) {
		for (ReceiverProgress listener : progressListener)
			listener.progressReceive(size);
	}

	/**
	 * inform listeners about receive end
	 * 
	 * @param size
	 */
	protected void informEnd(long size) {
		for (ReceiverProgress listener : progressListener)
			listener.endReceive(size);
	}

}
