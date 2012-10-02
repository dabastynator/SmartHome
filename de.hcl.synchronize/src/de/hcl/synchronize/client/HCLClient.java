package de.hcl.synchronize.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.hcl.synchronize.api.IHCLClient;
import de.hcl.synchronize.log.HCLLogger;
import de.hcl.synchronize.log.IHCLLogListener;
import de.newsystem.rmi.api.Server;
import de.newsystem.rmi.transceiver.FileReceiver;
import de.newsystem.rmi.transceiver.FileSender;

/**
 * the HCLClient implements the client and receives and sends files.
 * 
 * @author sebastian
 * 
 */
public class HCLClient implements IHCLClient, IHCLLogListener {

	/**
	 * Set the maximum VM size in byte. (50 MB)
	 */
	public static final long MAXIMUM_VM_SIZE = 50 * 1000 * 1000l;

	/**
	 * the cache directory contains meta info to cache information about the
	 * directory.
	 */
	public static final String CACHE_DIRECTORY = ".hcl" + File.separator;

	/**
	 * the md5 cache contains all files with their md5 code. the file is
	 * structured by following:<br>
	 * subfolder;file;size;last_modified;md5
	 */
	public static final String CACHE_SUBFOLDER_DIRECTORY = CACHE_DIRECTORY
			+ "cache" + File.separator;

	/**
	 * maximum data knowledge determines the oldest file bean. The oldest bean
	 * is 200 days old.
	 */
	public static final int MAXIMUM_KNOWLEGDE = 1000 * 3600 * 24 * 200;

	/**
	 * the log file lists all actions
	 */
	public static final String LOG_FILE = CACHE_DIRECTORY + "log";

	/**
	 * the backup folder contains all overridden files
	 */
	public static final String BACKUP_DIRECTORY = CACHE_DIRECTORY + "backup"
			+ File.separator;

	/**
	 * the base directory on witch the file system should be synchronized
	 */
	private String basePath;

	/**
	 * the file map contains information about the file, to increase the speed
	 */
	private Map<String, Subfolder> fileMap;

	/**
	 * Output stream for log data.
	 */
	private BufferedWriter logOutput;

	/**
	 * name of the client
	 */
	private String name;

	/**
	 * Allocate new home cloud client. The client operates on given base path.
	 * 
	 * @param basePath
	 * @param name
	 * @throws IOException
	 */
	public HCLClient(String basePath, String name) throws IOException {
		// initialize fields
		if (!basePath.endsWith(File.separator))
			basePath += File.separator;
		this.name = name;
		this.basePath = basePath;
		this.fileMap = new HashMap<String, Subfolder>();

		// check given base path
		if (!new File(basePath).exists())
			throw new IOException("directory does not exist: " + basePath);
		if (!new File(basePath).isDirectory())
			throw new IOException("base path is no directory");

		// initialize cache structure
		File cachDirectory = new File(basePath + CACHE_DIRECTORY);
		if (!cachDirectory.exists())
			cachDirectory.mkdir();
		File backupDirectory = new File(basePath + BACKUP_DIRECTORY);
		if (!backupDirectory.exists())
			backupDirectory.mkdir();
		File md5Directory = new File(basePath + CACHE_SUBFOLDER_DIRECTORY);
		if (!md5Directory.exists())
			md5Directory.mkdir();
		logOutput = new BufferedWriter(new FileWriter(new File(basePath
				+ LOG_FILE)));
		HCLLogger.addListener(this);
	}

	/**
	 * Get the subfolder manager of given subfolder.
	 * 
	 * @param subfolder
	 * @return subFileMap
	 */
	protected Subfolder getSubFileMap(String subfolder) {
		if (fileMap.containsKey(subfolder))
			return fileMap.get(subfolder);
		Subfolder subManager = new Subfolder(subfolder);
		fileMap.put(subfolder, subManager);
		return subManager;
	}

	@Override
	public boolean deleteFile(String file) throws RemoteException, IOException {
		File f = new File(basePath + file);
		HCLLogger.performLog(file, HCLType.DELETE, this);
		return f.delete();
	}

	@Override
	public boolean deleteDirectory(String directory) throws RemoteException,
			IOException {
		File f = new File(basePath + directory);
		deleteDirectory(f);
		HCLLogger.performLog(directory, HCLType.DELETE, this);
		return f.delete();
	}

	/**
	 * Deletes given directory. the directory can not be empty, all files will
	 * be deleted.
	 * 
	 * @param file
	 * @return true if directory was deleted
	 */
	private boolean deleteDirectory(File file) {
		for (File fi : file.listFiles())
			if (fi.isDirectory())
				deleteDirectory(fi);
			else
				deleteFile(fi);
		return file.delete();
	}

	/**
	 * Delete given file
	 * 
	 * @param file
	 */
	private void deleteFile(File file) {
		file.delete();
	}

	@Override
	public String sendFile(String filePath, int port) throws RemoteException,
			IOException {
		File file = new File(basePath + filePath);
		FileSender fileSender = new FileSender(file, port);
		fileSender.sendAsync();
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
		}
		HCLLogger.performLog(filePath, HCLType.SEND, this);
		return Server.getServer().getServerPort().getIp();
	}

	@Override
	public void receiveFile(String fileName, String subfolder, String ip,
			int port) throws RemoteException, IOException {
		File file = new File(basePath + subfolder + fileName);
		if (file.exists()) {
			File destination = new File(basePath + BACKUP_DIRECTORY + subfolder
					+ fileName + "." + file.lastModified());
			file.renameTo(destination);
			file = new File(basePath + subfolder + fileName);
			HCLLogger.performLog(fileName, HCLType.UPDATE, this);
		} else
			HCLLogger.performLog(fileName, HCLType.CREATE, this);
		FileReceiver receiver = new FileReceiver(ip, port, file);
		try {
			receiver.receiveSync();
		} catch (IOException e) {
			file.delete();
			getSubFileMap(subfolder).remove(fileName);
			HCLLogger.performLog(fileName + ' ' + e.getMessage(),
					HCLType.ERROR, this);
			throw e;
		}
	}

	@Override
	public boolean createDirectory(String subfolder, String directoryName)
			throws RemoteException, IOException {
		File file = new File(basePath + subfolder + directoryName);
		HCLLogger.performLog(directoryName, HCLType.CREATE, this);
		return file.mkdir();
	}

	@Override
	public String getName() throws RemoteException {
		return name;
	}

	@Override
	public FileBean[] listFiles(String subfolder) throws RemoteException,
			IOException {
		List<FileBean> list = new ArrayList<FileBean>();
		Subfolder subManager = getSubFileMap(subfolder);
		for (String str : new File(basePath + subfolder).list()) {
			File file = new File(basePath + subfolder + str);
			if (str.length() > 0 && str.charAt(0) != '.')
				list.add(getFileBean(file, subManager));
		}
		List<FileBean> deletedBean = new ArrayList<IHCLClient.FileBean>();
		for (FileBean bean : subManager.getFileBeans()) {
			if (!list.contains(bean) && !bean.isDeleted) {
				bean.isDeleted = true;
				bean.lastDate = System.currentTimeMillis();
				FileBean newBean = new FileBean(bean);
				list.add(newBean);
				deletedBean.add(newBean);
			} else if (bean.isDeleted)
				list.add(bean);
		}
		for (FileBean bean : deletedBean)
			subManager.push(bean);
		subManager.write();
		checkVMSize();
		return list.toArray(new FileBean[] {});
	}

	/**
	 * If current mv size is bigger or equal to the maximum size, delete the
	 * first file map entry.
	 */
	private void checkVMSize() {
		long totalMemory = Runtime.getRuntime().totalMemory();
		if (totalMemory >= MAXIMUM_VM_SIZE) {
			fileMap.remove(fileMap.keySet().iterator().next());
			System.gc();
		}
	}

	/**
	 * create file bean of given file and subfolder.
	 * 
	 * @param file
	 * @param subManager
	 * @return file bean
	 * @throws IOException
	 */
	private FileBean getFileBean(File file, Subfolder subManager)
			throws IOException {
		FileBean bean = subManager.getFileBean(file.getName());
		if (bean != null && bean.lastDate == file.lastModified()
				&& !bean.isDeleted)
			return bean;
		bean = new FileBean(subManager.getSubfolder(), file.getName(),
				file.lastModified(), buildMD5(file), file.length(),
				file.isDirectory(), false);
		subManager.push(bean);
		return bean;
	}

	/**
	 * create md5 code of given file.
	 * 
	 * @param file
	 * @return md5 of file
	 */
	private String buildMD5(File file) {
		if (file.isDirectory())
			return "";
		try {
			MessageDigest md = MessageDigest.getInstance("md5");
			InputStream is = new FileInputStream(file);
			byte[] buffer = new byte[8192];
			int read = 0;
			while ((read = is.read(buffer)) > 0)
				md.update(buffer, 0, read);
			byte[] md5 = md.digest();
			BigInteger bi = new BigInteger(1, md5);
			return bi.toString(16);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}

	@Override
	public void hclLog(IHCLMessage message) {
		if (message.client != this)
			return;
		try {
			logOutput.write(message.type.toString() + ": " + message.message
					+ " (" + message.time.toString() + ")\n");
			logOutput.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * The subfolder class manages the knowledge of a subfolder with files,
	 * directories, size, md5 and deletion.
	 * 
	 * @author sebastian
	 */
	protected class Subfolder {

		/**
		 * the sub file map contains all beans by their name
		 */
		private Map<String, FileBean> subFileMap = new HashMap<String, IHCLClient.FileBean>();

		/**
		 * the file map file caches the knowledge
		 */
		private File fileMapFile;

		/**
		 * relative subfolder ot this subfolder
		 */
		private String subfolder;

		/**
		 * true, is some changes on the file map are not saved.
		 */
		private boolean isDirty;

		/**
		 * Allocate new subfolder at given path
		 * 
		 * @param subfolder
		 */
		public Subfolder(String subfolder) {
			fileMapFile = new File(basePath + CACHE_SUBFOLDER_DIRECTORY
					+ subfolderToCache(subfolder));
			if (!fileMapFile.exists())
				try {
					fileMapFile.createNewFile();
				} catch (IOException e) {
				}
			subFileMap = new HashMap<String, IHCLClient.FileBean>();
			this.subfolder = subfolder;
			isDirty = false;
			read();
		}

		/**
		 * Remove given fileName from the file map.
		 * 
		 * @param fileName
		 */
		public void remove(String fileName) {
			subFileMap.remove(fileName);
			isDirty = true;
		}

		/**
		 * Get the file names of this subfolder.
		 * 
		 * @return filenames
		 */
		public Set<String> getSubFileMap() {
			return subFileMap.keySet();
		}

		/**
		 * Get the subfolder string of this subfolder.
		 * 
		 * @return subfolder
		 */
		public String getSubfolder() {
			return subfolder;
		}

		/**
		 * Get the file bean in this subfolder for given file name.
		 * 
		 * @param file
		 * @return filebean
		 */
		public FileBean getFileBean(String file) {
			return subFileMap.get(file);
		}

		/**
		 * Get file bean collection of this subfolder.
		 * 
		 * @return filebeans
		 */
		public Collection<FileBean> getFileBeans() {
			return subFileMap.values();
		}

		/**
		 * Insert new filebean to this subfolder.
		 * 
		 * @param fileBean
		 */
		public void push(FileBean fileBean) {
			subFileMap.put(fileBean.file, fileBean);
			isDirty = true;
		}

		/**
		 * Change subfolder string to cache name of the subfilemap.
		 * 
		 * @param subfolder
		 * @return String
		 */
		private String subfolderToCache(String subfolder) {
			String splash = "_";
			String cacheName = subfolder.replaceAll(File.separator, splash)
					+ ".cache";
			if (subfolder.contains(splash))
				for (String folder : subfolder.split(File.separator)) {
					cacheName += "$" + countString(folder, splash);
				}
			return cacheName;
		}

		private int countString(String string, String sequence) {
			int ret = 0;
			while (string.contains(sequence)) {
				ret++;
				string = string.substring(string.indexOf(sequence)
						+ sequence.length());
			}
			return ret;
		}

		/**
		 * Read cache file map and parse the line to file beans.
		 * 
		 * @param file
		 * @return fileMap
		 */
		private void read() {
			try {
				FileReader fileReader = new FileReader(fileMapFile);
				BufferedReader reader = new BufferedReader(fileReader);
				String line = reader.readLine();
				while ((line = reader.readLine()) != null) {
					FileBean bean = FileBean.parse(line);
					subFileMap.put(bean.file, bean);
				}
				reader.close();
				fileReader.close();
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		/**
		 * save the file map to cache file map in csv format.
		 * 
		 * @param file
		 * @param map
		 */
		private void write() {
			if (!isDirty)
				return;
			try {
				FileWriter fileWriter = new FileWriter(fileMapFile);
				BufferedWriter writer = new BufferedWriter(fileWriter);
				String newLine = System.getProperty("line.separator");
				writer.append("file_path;size;last_modified;md5" + newLine);
				long now = System.currentTimeMillis();
				for (String fileName : subFileMap.keySet()) {
					FileBean bean = subFileMap.get(fileName);
					if (!bean.isDeleted
							|| (now - bean.lastDate) < MAXIMUM_KNOWLEGDE)
						writer.append(bean.toString() + newLine);
				}
				writer.close();
				fileWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			isDirty = false;
		}

	}

}
