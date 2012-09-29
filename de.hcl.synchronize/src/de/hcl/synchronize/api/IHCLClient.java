package de.hcl.synchronize.api;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;

import de.newsystem.rmi.protokol.RemoteAble;

/**
 * The home cloud client executes all possible file activities. It enables
 * remote file upload and download.
 * 
 * @author sebastian
 * 
 */
public interface IHCLClient extends RemoteAble {

	/**
	 * Delete given file.
	 * 
	 * @param file
	 * @return true for deleted file
	 * @throws RemoteException
	 * @throws IOException
	 */
	public boolean deleteFile(String file) throws RemoteException, IOException;

	/**
	 * Delete given directory.
	 * 
	 * @param directory
	 * @return true for deleted directory
	 * @throws RemoteException
	 * @throws IOException
	 */
	public boolean deleteDirectory(String directory) throws RemoteException,
			IOException;

	/**
	 * Publish given file. Implementation must use FileSender for publishing.
	 * The Receiver must use FileReceiver to receive the file.
	 * 
	 * @param filePath
	 * @param port
	 * @return ip for published file
	 * @throws RemoteException
	 * @throws IOException
	 */
	public String sendFile(String filePath, int port) throws RemoteException,
			IOException;

	/**
	 * Create a new Directory under given subfolder with given directory name.
	 * 
	 * @param subfolder
	 * @param directoryName
	 * @return true if and only if the directory was created
	 * @throws RemoteException
	 * @throws IOException
	 */
	public boolean createDirectory(String subfolder, String directoryName)
			throws RemoteException, IOException;

	/**
	 * The client receives a file from given ip and port by FileReceiver. The
	 * file will created at given file path and name.
	 * 
	 * @param file
	 * @param subfolder
	 * @param ip
	 * @param port
	 * @throws RemoteException
	 * @throws IOException
	 */
	public void receiveFile(String file, String subfolder, String ip, int port)
			throws RemoteException, IOException;

	/**
	 * The client reads given directory and returns all files.
	 * 
	 * @param path
	 * @return list of files
	 * @throws RemoteException
	 * @throws IOException
	 */
	public FileBean[] listFiles(String path) throws RemoteException,
			IOException;

	/**
	 * The client reads given directory and returns directories. Return just
	 * visible directories, no .*.
	 * 
	 * @param path
	 * @return list of directories
	 * @throws RemoteException
	 * @throws IOException
	 */
	public String[] listDirectories(String path) throws RemoteException,
			IOException;

	/**
	 * Get the name of the client.
	 * 
	 * @return name of the client
	 * @throws RemoteException
	 */
	public String getName() throws RemoteException;

	/**
	 * Use file bean to transfer necessary file information.
	 * 
	 * @author sebastian
	 * 
	 */
	public static class FileBean implements Serializable {

		/**
		 * Separator used in the csv format. In the toString and parse method
		 * the separator is used.
		 */
		public static final String SEPARATOR = ";";

		/**
		 * Generated serial id
		 */
		private static final long serialVersionUID = 1716677500905866018L;

		/**
		 * Allocate new file bean and set attributes
		 * 
		 * @param subfolder
		 * @param file
		 * @param lastDate
		 * @param md5
		 * @param size
		 * @param isDeleted
		 */
		public FileBean(String subfolder, String file, long lastDate,
				String md5, long size, boolean isDeleted) {
			this.subfolder = subfolder;
			this.file = file;
			this.lastDate = lastDate;
			this.md5 = md5;
			this.size = size;
			this.isDeleted = isDeleted;
		}

		public FileBean(FileBean bean) {
			this(bean.subfolder, bean.file, bean.lastDate, bean.md5, bean.size,
					bean.isDeleted);
		}

		/**
		 * name of the file
		 */
		public String file;

		/**
		 * last edition date of the file
		 */
		public long lastDate;

		/**
		 * md5 code of the file
		 */
		public String md5;

		/**
		 * file size
		 */
		public long size;

		/**
		 * the subfolder of the file
		 */
		public String subfolder;

		/**
		 * true if the file is deleted
		 */
		public boolean isDeleted;

		@Override
		public String toString() {
			return subfolder + SEPARATOR + file + SEPARATOR + size + SEPARATOR
					+ lastDate + SEPARATOR + md5 + SEPARATOR + isDeleted;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof FileBean))
				return false;
			FileBean bean = (FileBean) obj;
			boolean result = file.equals(bean.file)
					&& subfolder.equals(bean.subfolder)
					&& lastDate == bean.lastDate && isDeleted == bean.isDeleted
					&& size == bean.size;
			if (md5 == null)
				result &= bean.md5 == null;
			else
				result &= md5.equals(bean.md5);
			return result;
		}

		/**
		 * parse file bean by csv line
		 * 
		 * @param line
		 * @return bean
		 */
		public static FileBean parse(String line) {
			String[] split = line.split(";");
			String filePath = "";
			int offset = -1;
			for (int i = 0; i < split.length - 5; i++) {
				filePath += split[i];
				offset++;
			}
			String file = split[offset + 1];
			long size = Long.parseLong(split[offset + 2]);
			long lastDate = Long.parseLong(split[offset + 3]);
			String md5 = split[offset + 4];
			boolean isDeleted = split[offset + 5].equals("true");
			FileBean bean = new FileBean(filePath, file, lastDate, md5, size,
					isDeleted);
			return bean;
		}

	}

}
