package de.neo.remote.transceiver;

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

	private static final int SUB_PROGRESS = 20;

	/**
	 * the directory in witch the directory will be saved
	 */
	private File directory;

	/**
	 * Size of the current download file
	 */
	private long currentFileSize;

	/**
	 * Download progress, loaded files and directories
	 */
	private long currentProgress;

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
		currentProgress = -1;
		if (state == ReceiverState.CANCELD)
			return;
		// prepare destiny
		if (!directory.exists())
			directory.mkdir();
		File newDirectory = new File(directory.getAbsolutePath()
				+ File.separator + dataInputStream.readUTF());
		newDirectory.mkdir();

		// read directory structure (file count, directory count, directory
		// name)
		int subDirectories = dataInputStream.readInt();
		int files = dataInputStream.readInt();
		if (directory.getAbsolutePath()
				.equals(this.directory.getAbsolutePath()))
			super.informStart((subDirectories + files) * SUB_PROGRESS,
					directory.getName());

		// read all directories in this directory
		for (int i = 0; i < subDirectories; i++) {
			if (directory.getAbsolutePath().equals(
					this.directory.getAbsolutePath())) {
				super.informProgress(i * SUB_PROGRESS, directory.getName());
				currentProgress = i * SUB_PROGRESS;
			}
			receiveDirectory(dataInputStream, newDirectory);
			if (state == ReceiverState.CANCELD)
				return;
		}

		// read all files in this directory
		for (int i = 0; i < files; i++) {
			this.file = new File(newDirectory.getAbsoluteFile()
					+ File.separator + dataInputStream.readUTF());
			if (directory.getAbsolutePath().equals(
					this.directory.getAbsolutePath())) {
				super.informProgress((subDirectories + i) * SUB_PROGRESS,
						this.file.getName());
				currentProgress = (subDirectories + i) * SUB_PROGRESS;
			}
			super.receiveData(dataInputStream);
			if (state == ReceiverState.CANCELD)
				return;
		}
		if (directory.getAbsolutePath()
				.equals(this.directory.getAbsolutePath()))
			super.informEnd(subDirectories + files);
	}

	@Override
	protected void informStart(long size, String file) {
		currentFileSize = size;
		progressStep = size / SUB_PROGRESS;
	}

	@Override
	protected void informEnd(long size) {
	}

	@Override
	protected void informProgress(long size, String file) {
		if (currentProgress >= 0)
			super.informProgress(currentProgress + (SUB_PROGRESS * size)
					/ currentFileSize, this.file.getName());
	}

}
