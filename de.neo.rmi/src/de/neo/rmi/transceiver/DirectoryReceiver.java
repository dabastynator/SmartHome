package de.neo.rmi.transceiver;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * the directory receiver receives a whole directory by the directory sender
 * over the tcp connection.
 * 
 * @author sebastian
 */
public class DirectoryReceiver extends FileReceiver {

	/**
	 * the directory in witch the directory will be saved
	 */
	private File directory;

	/**
	 * allocate new directory receiver
	 * 
	 * @param ip
	 * @param port
	 * @param directory
	 */
	public DirectoryReceiver(String ip, int port, File directory) {
		super(ip, port, null);
		this.directory = directory;
	}

	@Override
	protected void receiveData(InputStream input) throws IOException {
		receiveDirectory(new DataInputStream(input), directory);
	}

	/**
	 * read a directory from the input stream and save to the given directory
	 * 
	 * @param dataInputStream
	 * @param directory
	 * @throws IOException
	 */
	private void receiveDirectory(DataInputStream dataInputStream,
			File directory) throws IOException {
		if (state == ReceiverState.CANCELD)
			return;
		// prepare destiny
		if (!directory.exists())
			directory.mkdir();
		File newDirectory = new File(directory.getAbsolutePath()
				+ File.separator + dataInputStream.readUTF());
		newDirectory.mkdir();
		
		// read directory structure (file count, directory count, directory name)
		int subDirectories = dataInputStream.readInt();
		int files = dataInputStream.readInt();
		if (directory.getAbsolutePath()
				.equals(this.directory.getAbsolutePath()))
			super.informStart(subDirectories + files);
		
		// read all files in this directory
		for (int i = 0; i < subDirectories; i++) {
			receiveDirectory(dataInputStream, newDirectory);
			if (directory.getAbsolutePath().equals(
					this.directory.getAbsolutePath()))
				super.informProgress(i);
			if (state == ReceiverState.CANCELD)
				return;
		}
		
		// read all directories in this directory
		for (int i = 0; i < files; i++) {
			this.file = new File(newDirectory.getAbsoluteFile()
					+ File.separator + dataInputStream.readUTF());
			super.receiveData(dataInputStream);
			if (directory.getAbsolutePath().equals(
					this.directory.getAbsolutePath()))
				super.informProgress(subDirectories + i);
			if (state == ReceiverState.CANCELD)
				return;
		}
		if (directory.getAbsolutePath()
				.equals(this.directory.getAbsolutePath()))
			super.informEnd(subDirectories + files);
	}

	@Override
	protected void informStart(long size) {
	}

	@Override
	protected void informEnd(long size) {
	}

	@Override
	protected void informProgress(long size) {
	}

}
