package de.newsystem.rmi.transeiver;

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
	private File file;

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
	protected void receiveData(InputStream input) {
		try {
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
			byte[] data = new byte[(int) Math.min(size, progressStep)];
			int i;
			long currentSize = 0;
			while ((i = input.read(data, 0, data.length)) != -1) {
				output.write(data, 0, i);
				currentSize += i;
				informProgress(currentSize);
			}

			output.close();
			informEnd(size);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
