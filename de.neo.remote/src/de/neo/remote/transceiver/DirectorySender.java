package de.neo.remote.transceiver;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * the folder sender sends a specified folder.
 * 
 * @author sebastian
 */
public class DirectorySender extends FileSender {

	/**
	 * the directory that will be transfered
	 */
	private File directory;

	/**
	 * allocate new file sender. the sender creates a server socket on specified
	 * port. the file will be send to times clients.
	 * 
	 * @param directory
	 * @param port
	 * @param times
	 * @throws IOException
	 */
	public DirectorySender(File directory, int port, int times)
			throws IOException {
		super(directory, port, times);
		this.directory = directory;
	}

	/**
	 * allocate new folder sender
	 * 
	 * @param directory
	 * @param port
	 * @throws IOException
	 */
	public DirectorySender(File directory, int port) throws IOException {
		this(directory, port, 1);
	}

	@Override
	protected void writeData(OutputStream output) throws IOException {
		writeDirectory(new DataOutputStream(output), directory);
	}

	/**
	 * write given directory recursively to the data output
	 * 
	 * @param dataOutput
	 * @param directory
	 * @throws IOException
	 */
	private void writeDirectory(DataOutputStream dataOutput, File directory)
			throws IOException {
		List<File> files = new ArrayList<File>();
		List<File> folders = new ArrayList<File>();
		for (File file : directory.listFiles()) {
			if (file.isFile())
				files.add(file);
			else if (file.isDirectory())
				folders.add(file);
		}
		dataOutput.writeUTF(directory.getName());
		dataOutput.writeInt(folders.size());
		dataOutput.writeInt(files.size());
		for (File folder : folders)
			writeDirectory(dataOutput, folder);
		for (File file : files) {
			this.file = file;
			dataOutput.writeUTF(file.getName());
			super.writeData(dataOutput);
		}
	}
}
