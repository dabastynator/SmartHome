package de.neo.rmi.transceiver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * the file sender sends a specified file.
 * 
 * @author sebastian
 */
public class FileSender extends AbstractSender {

	/**
	 * the file that will be transfered
	 */
	protected File file;

	/**
	 * maximal buffer size in byte.
	 */
	protected long maxSize = Long.MAX_VALUE;

	/**
	 * allocate new file sender. the sender creates a server socket on specified
	 * port. the file will be send to times clients.
	 * 
	 * @param file
	 * @param port
	 * @param times
	 * @param progress
	 * @throws IOException
	 */
	public FileSender(File file, int port, int times, long progress)
			throws IOException {
		super(port, times, progress);
		if (file.exists() == false)
			throw new IOException("the file " + file.getName()
					+ " does not exist");
		this.file = file;
	}

	/**
	 * allocate new file sender
	 * 
	 * @param file
	 * @param port
	 * @param times
	 * @throws IOException
	 */
	public FileSender(File file, int port, int times) throws IOException {
		this(file, port, times, Long.MAX_VALUE);
	}

	/**
	 * new file sender.
	 * 
	 * @param file
	 * @param port
	 * @throws IOException
	 */
	public FileSender(File file, int port) throws IOException {
		this(file, port, 1);
	}

	/**
	 * Set maximal buffer size in byte.
	 * 
	 * @param maxSize
	 */
	public void setBufferSize(long maxSize) {
		this.maxSize = maxSize;
	}

	@Override
	protected void writeData(OutputStream output) throws IOException {
		InputStream input = new FileInputStream(file);
		// send file length (long -> byte array)
		long length = file.length();
		byte[] l = new byte[4];
		for (int i = 0; i < 4; i++) {
			l[3 - i] = (byte) (length >>> (i * 8));
		}
		output.write(l);
		informStart(length);

		// send data
		int bufferSize = (int) Math
				.min(Math.min(length, maxSize), progressStep);
		long count = 0, currentSize = 0;
		byte[] data = new byte[bufferSize];
		int read = 0;
		while ((read = input.read(data, 0, bufferSize)) >= 0) {
			output.write(data, 0, read);
			currentSize += read;
			count += read;
			if (count >= progressStep) {
				informProgress(currentSize);
				count = 0;
			}
		}

		output.flush();
		input.close();
		informEnd(length);
	}

}
