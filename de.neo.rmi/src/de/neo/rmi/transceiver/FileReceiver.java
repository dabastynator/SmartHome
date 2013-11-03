package de.neo.rmi.transceiver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * the file receiver receives a file via a tcp connection and creates the file
 * on the file system.
 * 
 * @author sebastian
 */
public class FileReceiver extends AbstractReceiver {

	/**
	 * the file that will be created
	 */
	protected File file;

	/**
	 * maximum size of the array to buffer read bytes
	 */
	private long maxSize = Long.MAX_VALUE;

	/**
	 * allocate new file receiver with given specifications. the file must not
	 * exist and will be created by received data.
	 * 
	 * @param ip
	 * @param port
	 * @param file
	 */
	public FileReceiver(String ip, int port, File file) {
		this(ip, port, Long.MAX_VALUE, file);
	}

	/**
	 * allocate new file receiver with given specifications. the file must not
	 * exist and will be created by received data.
	 * 
	 * @param ip
	 * @param port
	 * @param progressStep
	 * @param file
	 */
	public FileReceiver(String ip, int port, long progressStep, File file) {
		super(ip, port, progressStep);
		this.file = file;
	}

	@Override
	protected void receiveData(InputStream input) throws IOException {
		OutputStream output = new FileOutputStream(file);

		// read file size from stream (byte array -> long)
		long size = 0;
		byte[] b = new byte[4];
		input.read(b);
		for (int j = 0; j < 4; j++) {
			size <<= 8;
			size ^= (long) b[j] & 0xFF;
		}
		informStart(size);

		// receive file data from stream
		byte[] data = new byte[(int) Math.min(Math.min(size, progressStep),
				maxSize)];
		int i;
		long currentSize = 0, count = 0;
		while ((i = input.read(data, 0,
				(int) Math.min(data.length, size - currentSize))) != -1) {
			output.write(data, 0, i);
			currentSize += i;
			count += i;
			if (count >= progressStep) {
				count = 0;
				informProgress(currentSize);
			}
			if (currentSize >= size)
				break;
			if (state == ReceiverState.CANCELD) {
				output.flush();
				output.close();
				return;
			}
		}

		output.close();
		informEnd(size);
	}

	/**
	 * set the maximum buffer size in byte for reading from the stream and
	 * writing to the file
	 * 
	 * @param maxSize
	 */
	public void setBufferSize(long maxSize) {
		this.maxSize = maxSize;
	}

}
