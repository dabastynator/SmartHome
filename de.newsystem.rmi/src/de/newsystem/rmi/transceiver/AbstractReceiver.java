package de.newsystem.rmi.transceiver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
	 * possible stated of the receiver
	 * 
	 * @author sebastian
	 */
	public enum ReceiverState {
		READY, LOADING, FAILED, CANCELD, FINISHED
	};

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
	 * current state of the receiver
	 */
	protected ReceiverState state;

	/**
	 * input stream from the socket
	 */
	protected InputStream input;

	/**
	 * output stream from the socket
	 */
	protected OutputStream output;

	/**
	 * size of downloading data
	 */
	protected long downloadSize;

	/**
	 * current progress of downloading data
	 */
	protected long downloadProgress;

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
		this.state = ReceiverState.READY;
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
				} catch (Exception e) {
					state = ReceiverState.FAILED;
					informException(e);
				}
			};
		}.start();
	}

	/**
	 * inform all listener about occurred exception
	 * 
	 * @param e
	 */
	protected void informException(Exception e) {
		for (ReceiverProgress progress : progressListener)
			progress.exceptionOccurred(e);
	}

	/**
	 * receive data synchronous
	 * 
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public void receiveSync() throws UnknownHostException, IOException {
		Socket socket = new Socket(ip, port);
		input = socket.getInputStream();
		output = socket.getOutputStream();
		if (state == ReceiverState.READY) {
			state = ReceiverState.LOADING;
			output.write(ReceiverState.LOADING.ordinal());
			receiveData(input);
			output.write(ReceiverState.FINISHED.ordinal());
			state = ReceiverState.FINISHED;
		} else if (state == ReceiverState.CANCELD) {
			output.write(ReceiverState.CANCELD.ordinal());
		}
		socket.close();
	}

	/**
	 * receive data from stream
	 * 
	 * @param inputStream
	 * @throws IOException
	 */
	protected abstract void receiveData(InputStream inputStream)
			throws IOException;

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
		downloadSize = size;
		for (ReceiverProgress listener : progressListener)
			listener.startReceive(size);
	}

	/**
	 * inform listeners about receive progress
	 * 
	 * @param size
	 */
	protected void informProgress(long size) {
		downloadProgress = size;
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

	/**
	 * cancel current download
	 */
	public void cancel() {
		state = ReceiverState.CANCELD;
		try {
			if (output != null) {
				output.write(ReceiverState.CANCELD.ordinal());
				output.close();
			}
		} catch (IOException e) {
		}
	}

	/**
	 * @return current state of the receiver
	 */
	public ReceiverState getState() {
		return state;
	}

	/**
	 * @return full size of downloading data
	 */
	public long getFullSize() {
		return downloadSize;
	}

	/**
	 * @return current progress of downloading data
	 */
	public long getDownloadProgress() {
		return downloadProgress;
	}
}
