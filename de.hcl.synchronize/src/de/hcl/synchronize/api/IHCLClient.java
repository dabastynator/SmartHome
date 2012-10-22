package de.hcl.synchronize.api;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;

import de.newsystem.rmi.protokol.RemoteAble;
import de.newsystem.rmi.protokol.RemoteException;

/**
 * The home cloud client executes all possible file activities. It enables
 * remote file upload and download.
 * 
 * @author sebastian
 * 
 */
public interface IHCLClient extends RemoteAble {

	/**
	 * The port of a client server
	 */
	public static final int CLIENT_PORT = IHCLServer.SERVER_PORT + 1;

	/**
	 * Delete given file.
	 * 
	 * @param file
	 * @return true for deleted file
	 * @throws RemoteException
	 * @throws IOException
	 */
	public boolean deleteFile(String subfolder, String file)
			throws RemoteException, IOException;

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
	 * @param fileBean
	 * @param port
	 * @return ip for published file
	 * @throws RemoteException
	 * @throws IOException
	 */
	public String sendFile(FileBean bean, int port) throws RemoteException,
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
	 * @param fileBean
	 * @param ip
	 * @param port
	 * @throws RemoteException
	 * @throws IOException
	 */
	public void receiveFile(FileBean fileBean, String ip, int port)
			throws RemoteException, IOException;

	/**
	 * Rename given file to new file name under subfolder.
	 * 
	 * @param subfolder
	 * @param oldName
	 * @param newName
	 * @throws RemoteException
	 */
	public void renameFile(String subfolder, String oldName, String newName)
			throws RemoteException, IOException;

	/**
	 * The client reads given directory and returns all files and directories.
	 * 
	 * @param subfolder
	 * @return list of files
	 * @throws RemoteException
	 * @throws IOException
	 */
	public FileBean[] listFiles(String subfolder) throws RemoteException,
			IOException;

	/**
	 * Get the name of the client.
	 * 
	 * @return name of the client
	 * @throws RemoteException
	 */
	public String getName() throws RemoteException;

	/**
	 * Get the hash code of the given subfolder to avoid to transfer big file
	 * bean lists.
	 * 
	 * @param subfolder
	 * @return hashcode
	 * @throws RemoteException
	 */
	public String getHash(String subfolder) throws RemoteException;

	/**
	 * Get the file bean of specified file and subfolde.
	 * 
	 * @param subfolder
	 * @param file
	 * @return
	 * @throws RemoteException
	 */
	public FileBean getFileBean(String subfolder, String file)
			throws RemoteException;

	/**
	 * Get array of directories under giben subfolder.
	 * 
	 * @param subfolder
	 * @return array of directory names
	 * @throws RemoteException
	 */
	public String[] listDirectories(String subfolder) throws RemoteException,
			IOException;

	/**
	 * Check if the client is just read only client and can not write.
	 * 
	 * @return true, if client can not write.
	 * @throws RemoteException
	 */
	public boolean isReadOnly() throws RemoteException;

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
		 * flag file is 1 for file and 0 for directory
		 */
		public static final byte FILE = 1;

		/**
		 * flag exists is 1 if the file exists and 0 otherwise
		 */
		public static final byte EXISTS = 2;

		/**
		 * flag done is 1 if the file is stable and 0 if the file is receiving.
		 */
		public static final byte DONE = 4;

		/**
		 * flag copy is 1 if the file is already copying and changes its size.
		 */
		public static final byte COPY = 8;

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
				byte[] md5, long size, byte flags) {
			this.subfolder = subfolder;
			this.file = file;
			this.lastDate = lastDate;
			this.md5 = md5;
			this.size = size;
			this.creation = System.currentTimeMillis();
			this.flags = flags;
		}

		public FileBean(FileBean bean) {
			this(bean.subfolder, bean.file, bean.lastDate, bean.md5, bean.size,
					bean.flags);
		}

		/**
		 * flags for information about existent, folder directory, status
		 */
		public byte flags;

		/**
		 * creation date of the bean
		 */
		public long creation;

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
		public byte[] md5;

		/**
		 * file size
		 */
		public long size;

		/**
		 * the subfolder of the file
		 */
		public String subfolder;

		@Override
		public String toString() {
			String md5S = "";
			if (md5 != null) {
				BigInteger bi = new BigInteger(1, md5);
				md5S = bi.toString(16);
			}
			return subfolder + SEPARATOR + file + SEPARATOR + size + SEPARATOR
					+ lastDate + SEPARATOR + md5S + SEPARATOR + flags;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof FileBean))
				return false;
			FileBean bean = (FileBean) obj;
			boolean result = file.equals(bean.file)
					&& subfolder.equals(bean.subfolder)
					&& lastDate == bean.lastDate && flags == bean.flags
					&& size == bean.size;
			if (md5 == null)
				result &= bean.md5 == null;
			else
				result &= md5.equals(bean.md5);
			return result;
		}

		/**
		 * @return true if file is directory, false otherwise
		 */
		public boolean isDirectory() {
			return (flags & FILE) == 0;
		}

		/**
		 * @return true if file is deleted, false otherwise
		 */
		public boolean isDeleted() {
			return (flags & EXISTS) == 0;
		}

		/**
		 * @return true if file is receiving, false otherwise
		 */
		public boolean isReceiving() {
			return (flags & DONE) == 0;
		}

		public boolean isCopying() {
			return (flags & COPY) != 0;
		}

		/**
		 * parse file bean by csv line
		 * 
		 * @param line
		 * @return bean
		 */
		public static FileBean parse(String line) {
			try {
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
				byte[] md5 = new byte[16];
				if (split[offset + 4].length() > 0) {
					byte[] parse = new BigInteger(split[offset + 4], 16)
							.toByteArray();
					System.arraycopy(parse, Math.max(parse.length - 16, 0),
							md5, 0, Math.min(16, parse.length));
				}
				byte flags = Byte.parseByte(split[offset + 5]);
				FileBean bean = new FileBean(filePath, file, lastDate, md5,
						size, flags);
				return bean;
			} catch (RuntimeException e) {
				e.printStackTrace();
				return null;
			}
		}

	}

}
