package de.hcl.synchronize.client;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hcl.synchronize.api.IHCLClient;
import de.hcl.synchronize.log.HCLLogger;
import de.hcl.synchronize.log.IHCLLogListener;
import de.newsystem.rmi.api.Server;
import de.newsystem.rmi.protokol.RemoteException;
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
	 * Set the maximum VM size in byte. (100 MB)
	 */
	public static final long MAXIMUM_VM_SIZE = 100 * 1000 * 1000l;

	/**
	 * Maximal buffer size for sending and receiving is 2 MB.
	 */
	public static final long MAXIMAL_BUFFER_SIZE = 1024 * 1024 * 2;

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
	 * the icon folder contains all icons for the gui
	 */
	public static final String ICON_DIRECTORY = CACHE_DIRECTORY + "icons"
			+ File.separator;

	/**
	 * the icon folder contains all icons for the gui
	 */
	public static final String CREATE_ICON = ICON_DIRECTORY + "create.png";

	/**
	 * the icon folder contains all icons for the gui
	 */
	public static final String DELETE_ICON = ICON_DIRECTORY + "delete.png";

	/**
	 * the icon folder contains all icons for the gui
	 */
	public static final String ERROR_ICON = ICON_DIRECTORY + "error.png";

	/**
	 * the icon folder contains all icons for the gui
	 */
	public static final String SYNCH_ICON = ICON_DIRECTORY + "synch.png";

	/**
	 * the icon folder contains all icons for the gui
	 */
	public static final String INFORM_ICON = ICON_DIRECTORY + "inform.png";

	/**
	 * the base directory on witch the file system should be synchronized
	 */
	protected String basePath;

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
	 * true if the client must not do changes on the file system.
	 */
	private boolean readOnly;

	/**
	 * Minimal buffered directory cache.
	 */
	private int refreshRate;

	/**
	 * Get input stream from resource in the current project or jar file.
	 * 
	 * @param owner
	 * @param resource
	 * @return inputstream
	 * @throws FileNotFoundException
	 */
	public static InputStream inputStreamFromResource(Object owner,
			String resource) throws FileNotFoundException {
		String path = owner.getClass().getProtectionDomain().getCodeSource()
				.getLocation().toString().substring(5);
		if (path.endsWith("bin/"))
			path = path.substring(0, path.length() - 4);
		InputStream input = null;
		if (path.toLowerCase().endsWith(".jar"))
			input = owner.getClass().getResourceAsStream("/" + resource);
		else
			input = owner.getClass().getResourceAsStream(path + resource);
		if (input == null)
			input = new FileInputStream(new File(path + resource));
		return input;
	}

	/**
	 * Reads given object from owner and save read data to given destiny file.
	 * 
	 * @param source
	 * @param destiny
	 * @throws IOException
	 */
	public static void copyFileFromJar(Object owner, String source, File destiny)
			throws IOException {
		InputStream input = inputStreamFromResource(owner, source);
		FileOutputStream output = new FileOutputStream(destiny);
		byte[] data = new byte[2048];
		int read = 0;
		while ((read = input.read(data)) > -1) {
			output.write(data, 0, read);
		}
		output.close();
		input.close();
	}

	/**
	 * Allocate new home cloud client. The client operates on given base path.
	 * 
	 * @param basePath
	 * @param name
	 * @param refreshRate
	 * @throws IOException
	 */
	public HCLClient(String basePath, String name, boolean readOnly,
			int refreshRate) throws IOException {
		// initialize fields
		if (!basePath.endsWith(File.separator))
			basePath += File.separator;
		this.name = name;
		this.basePath = basePath;
		this.fileMap = new HashMap<String, Subfolder>();
		this.readOnly = readOnly;
		this.refreshRate = refreshRate;

		// check given base path
		if (!new File(basePath).exists())
			throw new IOException("directory does not exist: " + basePath);
		if (!new File(basePath).isDirectory())
			throw new IOException("base path is no directory");

		initializeCacheStructure();

		logOutput = new BufferedWriter(new FileWriter(new File(basePath
				+ LOG_FILE)));
		HCLLogger.addListener(this);
	}

	/**
	 * Create cache structure with all necessary folder and files.
	 */
	private void initializeCacheStructure() {
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
		File iconDirectory = new File(basePath + ICON_DIRECTORY);
		if (!iconDirectory.exists()) {
			iconDirectory.mkdir();
			try {
				copyFileFromJar(this, "icons/create.png", new File(basePath
						+ CREATE_ICON));
				copyFileFromJar(this, "icons/delete.png", new File(basePath
						+ DELETE_ICON));
				copyFileFromJar(this, "icons/synch.png", new File(basePath
						+ SYNCH_ICON));
				copyFileFromJar(this, "icons/error.png", new File(basePath
						+ ERROR_ICON));
				copyFileFromJar(this, "icons/inform.png", new File(basePath
						+ INFORM_ICON));
			} catch (IOException e) {
				HCLLogger.performLog(
						"Initialize icons error: " + e.getMessage(),
						HCLType.ERROR, this);
			}
		}
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
		Subfolder subManager = new Subfolder(subfolder, basePath, refreshRate);
		fileMap.put(subfolder, subManager);
		return subManager;
	}

	@Override
	public boolean deleteFile(String subfolder, String file)
			throws RemoteException, IOException {
		checkReadOnly();
		// Tell subfolder
		Subfolder subFileMap = getSubFileMap(subfolder);
		subFileMap.setDeletedFile(file);
		// Delete
		File f = new File(basePath + subfolder + file);
		if (!f.exists())
			return false;
		HCLLogger.performLog("Delete file: '" + file + "'", HCLType.DELETE,
				this);
		return f.delete();
	}

	@Override
	public boolean deleteDirectory(String directory) throws RemoteException,
			IOException {
		checkReadOnly();
		File f = new File(basePath + directory);
		boolean deleted = deleteDirectory(f);
		fileMap.remove(directory);
		if (deleted)
			HCLLogger.performLog("Delete directory: '" + directory + "'",
					HCLType.DELETE, this);
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
		if (!file.exists())
			return false;
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
	public String sendFile(FileBean bean, int port) throws RemoteException,
			IOException {
		FileBean mapBean = getSubFileMap(bean.subfolder).getFileBean(bean.file);
		if (mapBean == null || mapBean.isReceiving() || mapBean.isCopying()) {
			String message = (mapBean != null) ? "Can not send receiving"
					: "Can not send not existing";
			if (mapBean.isCopying())
				message = "Can not send changing";
			message += " file: '" + bean.file + "'.";
			HCLLogger.performLog(message, HCLType.ERROR, this);
			throw new IOException(message);
		}
		File file = new File(basePath + bean.subfolder + bean.file);
		FileSender fileSender = new FileSender(file, port);
		fileSender.setMaximalBufferSize(MAXIMAL_BUFFER_SIZE);
		fileSender.sendAsync();
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
		}
		HCLLogger.performLog("Send file: '" + bean.file + "'", HCLType.SEND,
				this);
		return Server.getServer().getServerPort().getIp();
	}

	@Override
	public void receiveFile(FileBean fileBean, String ip, int port)
			throws RemoteException, IOException {
		checkReadOnly();
		// initialize objects
		String fileName = fileBean.file;
		String subfolder = fileBean.subfolder;
		File file = new File(basePath + subfolder + fileName);
		Subfolder subManager = getSubFileMap(subfolder);
		FileBean oldBean = subManager.getFileBean(fileName);
		// check existing beans and files
		if (oldBean != null && (oldBean.isReceiving() || oldBean.isCopying())) {
			HCLLogger.performLog("File: '" + fileName
					+ "' is already receiving.", HCLType.ERROR, this);
			throw new IOException("File '" + fileName
					+ "' is already receiving.");
		}
		if (file.exists()) {
			File destination = new File(basePath + BACKUP_DIRECTORY + subfolder
					+ fileName + "." + file.lastModified());
			file.renameTo(destination);
			file = new File(basePath + subfolder + fileName);
			HCLLogger.performLog("Update file: '" + fileName + "'",
					HCLType.UPDATE, this);
		} else
			HCLLogger.performLog("Create file: '" + fileName + "'",
					HCLType.CREATE, this);
		file.createNewFile();
		file.setLastModified(fileBean.lastDate - 1000);
		// tell subfolder about receiving file
		FileBean bean = new FileBean(subfolder, fileName, file.lastModified(),
				new byte[] {}, 0, (byte) (FileBean.EXISTS | FileBean.FILE));
		subManager.push(bean);
		FileReceiver receiver = new FileReceiver(ip, port, file);
		receiver.setBufferSize(MAXIMAL_BUFFER_SIZE);
		try {
			receiver.receiveSync();
			file.setLastModified(fileBean.lastDate - 1000);
			subManager.remove(fileName);
			subManager.getFileBean(file);
		} catch (IOException e) {
			file.delete();
			subManager.remove(fileName);
			HCLLogger.performLog(fileName + ' ' + e.getMessage(),
					HCLType.ERROR, this);
			throw e;
		}
	}

	/**
	 * Check read only state and throw exception if so.
	 * 
	 * @throws IOException
	 */
	private void checkReadOnly() throws IOException {
		if (readOnly) {
			String msg = "Can not make changes at read only client.";
			HCLLogger.performLog(msg, HCLType.ERROR, this);
			throw new IOException(msg);
		}
	}

	@Override
	public boolean createDirectory(String subfolder, String directoryName)
			throws RemoteException, IOException {
		checkReadOnly();
		File file = new File(basePath + subfolder + directoryName);
		HCLLogger.performLog("Create new directory: '" + directoryName + "'",
				HCLType.CREATE, this);
		return file.mkdir();
	}

	@Override
	public void renameFile(String subfolder, String oldName, String newName)
			throws RemoteException, IOException {
		checkReadOnly();
		File file = new File(basePath + subfolder + oldName);
		if (!file.exists())
			return;
		File newFile = new File(basePath + subfolder + newName);
		file.renameTo(newFile);
		Subfolder subFileMap = getSubFileMap(subfolder);
		FileBean oldBean = subFileMap.getFileBean(oldName);
		subFileMap.remove(oldName);
		if (oldBean != null) {
			FileBean newBean = new FileBean(oldBean);
			newBean.file = newName;
			subFileMap.push(newBean);
		}
		HCLLogger.performLog("Rename file '" + oldName + "' to '" + newName
				+ "'.", HCLType.UPDATE, this);
	}

	@Override
	public String getName() throws RemoteException {
		return name;
	}

	@Override
	public FileBean[] listFiles(String subfolder) throws RemoteException,
			IOException {
		Subfolder subManager = getSubFileMap(subfolder);
		FileBean[] listFiles = subManager.listFiles()
				.toArray(new FileBean[] {});
		checkVMSize();
		return listFiles;
	}

	/**
	 * If current mv size is bigger or equal to the maximum size, delete the
	 * first file map entry.
	 */
	private void checkVMSize() {
		long totalMemory = Runtime.getRuntime().totalMemory();
		if (totalMemory >= MAXIMUM_VM_SIZE) {
			for (Subfolder sub : fileMap.values())
				sub.reduceStoreage();
			System.gc();
			HCLLogger.performLog("Reduce heap size", HCLType.INFORMATION, this);
		}
	}

	@Override
	public String getHash(String subfolder) throws RemoteException {
		Subfolder subFileMap = getSubFileMap(subfolder);
		String hash = subFileMap.getDirectoryHash();
		return hash;
	}

	@Override
	public FileBean getFileBean(String subfolder, String file)
			throws RemoteException {
		Subfolder subFileMap = getSubFileMap(subfolder);
		FileBean bean = subFileMap.getFileBean(file);
		if (bean != null)
			return bean;
		try {
			bean = subFileMap
					.getFileBean(new File(basePath + subfolder + file));
		} catch (IOException e) {
		}
		if (bean != null)
			return bean;
		return null;
	}

	@Override
	public String[] listDirectories(String subfolder) throws IOException {
		List<String> folder = new ArrayList<String>();
		for (FileBean bean : getSubFileMap(subfolder).listFiles()) {
			if (bean.isDirectory() && !bean.isDeleted())
				folder.add(bean.file + File.separator);
		}
		return folder.toArray(new String[] {});
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

	@Override
	public boolean isReadOnly() throws RemoteException {
		return readOnly;
	}

	@Override
	public long getMinimalRefreshRate() throws RemoteException {
		return refreshRate;
	}
}
