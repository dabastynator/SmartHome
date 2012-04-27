package de.newsystem.rmi.transceiver;

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
	 * allocate new file sender. the sender creates a server socket on specified
	 * port. the file will be send to times clients.
	 * 
	 * @param file
	 * @param port
	 * @param times
	 * @throws IOException
	 */
	public FileSender(File file, int port, int times) throws IOException {
		super(port, times);
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
	 * @throws IOException
	 */
	public FileSender(File file, int port) throws IOException {
		this(file, port, -1);
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

		// send data
		byte[] data = new byte[(int) length];
		input.read(data, 0, data.length);
		output.write(data, 0, data.length);
		output.flush();
		input.close();
	}

}
