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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hcl.synchronize.api.IHCLClient;
import de.hcl.synchronize.log.HCLLogger;
import de.hcl.synchronize.log.IHCLLog;
import de.newsystem.rmi.api.Server;
import de.newsystem.rmi.transceiver.FileReceiver;
import de.newsystem.rmi.transceiver.FileSender;
import de.newsystem.rmi.transceiver.ReceiverProgress;

/**
 * the HCLClient implements the client and receives and sends files.
 * 
 * @author sebastian
 * 
 */
public class HCLClient implements IHCLClient, IHCLLog, ReceiverProgress {

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
	public static final String CACHE_MD5 = CACHE_DIRECTORY + "size_md5_cache";

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
	private Map<String, Map<String, IHCLClient.FileBean>> fileMap;

	/**
	 * true if the fileMap contains new information about the file system.
	 */
	private boolean isDirty;

	/**
	 * Output stream for log data.
	 */
	private BufferedWriter logOutput;

	/**
	 * name of the client
	 */
	private String name;

	public HCLClient(String basePath, String name) throws IOException {
		this.name = name;
		this.basePath = basePath;
		if (!new File(basePath).exists())
			throw new IOException("directory does not exist");
		if (!new File(basePath).isDirectory())
			throw new IOException("base path is no directory");
		File cachDirectory = new File(basePath + CACHE_DIRECTORY);
		if (!cachDirectory.exists())
			cachDirectory.mkdir();
		File backupDirectory = new File(basePath + BACKUP_DIRECTORY);
		if (!backupDirectory.exists())
			backupDirectory.mkdir();
		fileMap = readMD5Cache(new File(basePath + CACHE_MD5));
		logOutput = new BufferedWriter(new FileWriter(new File(basePath
				+ LOG_FILE)));
		HCLLogger.addListener(this);
	}

	private Map<String, Map<String, FileBean>> readMD5Cache(File file) {
		Map<String, Map<String, FileBean>> map = new HashMap<String, Map<String, FileBean>>();
		try {
			FileReader fileReader = new FileReader(file);
			BufferedReader reader = new BufferedReader(fileReader);
			String line = reader.readLine();
			while ((line = reader.readLine()) != null) {
				FileBean bean = FileBean.parse(line);
				if (!map.containsKey(bean.subfolder))
					map.put(bean.subfolder,
							new HashMap<String, IHCLClient.FileBean>());
				map.get(bean.subfolder).put(bean.file, bean);
			}
			reader.close();
			fileReader.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			e.printStackTrace();
		}
		return map;
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
	 * deletes given directory. the directory can not be empty, all files will
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
	 * delete given file
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
		// receiver.getProgressListener().add(this);
		try {
			receiver.receiveSync();
		} catch (IOException e) {
			file.delete();
			if (fileMap.containsKey(subfolder)) {
				fileMap.get(subfolder).remove(fileName);
				isDirty = true;
			}
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
	public FileBean[] listFiles(String path) throws RemoteException,
			IOException {
		isDirty = false;
		String subfolder = "";
		if (path.contains(File.separator))
			subfolder = path.substring(0, path.lastIndexOf(File.separator) + 1);
		List<FileBean> list = new ArrayList<FileBean>();
		for (String str : new File(basePath + path).list()) {
			File file = new File(basePath + path + str);
			if (str.length() > 0 && str.charAt(0) != '.')
				list.add(getFileBean(file, subfolder));
		}
		if (fileMap.containsKey(subfolder)) {
			Map<String, FileBean> map = fileMap.get(subfolder);
			List<FileBean> deletedBean = new ArrayList<IHCLClient.FileBean>();
			for (FileBean bean : map.values()) {
				if (!list.contains(bean) && !bean.isDeleted) {
					bean.isDeleted = true;
					bean.lastDate = System.currentTimeMillis();
					FileBean newBean = new FileBean(bean);
					list.add(newBean);
					deletedBean.add(newBean);
					isDirty = true;
				} else if (bean.isDeleted)
					list.add(bean);
			}
			for (FileBean bean : deletedBean)
				map.put(bean.file, bean);
		}
		if (isDirty) {
			isDirty = false;
			saveFileMap(new File(basePath + CACHE_MD5), fileMap);
		}
		return list.toArray(new FileBean[] {});
	}

	/**
	 * save the file map to given file in csv format.
	 * 
	 * @param file
	 * @param map
	 */
	private void saveFileMap(File file, Map<String, Map<String, FileBean>> map) {
		try {
			FileWriter fileWriter = new FileWriter(file);
			BufferedWriter writer = new BufferedWriter(fileWriter);
			String newLine = System.getProperty("line.separator");
			writer.append("file_path;size;last_modified;md5" + newLine);
			long now = System.currentTimeMillis();
			for (String filePath : map.keySet()) {
				Map<String, FileBean> submap = map.get(filePath);
				for (String fileName : submap.keySet()) {
					FileBean bean = submap.get(fileName);
					if (!bean.isDeleted
							|| (now - bean.lastDate) > MAXIMUM_KNOWLEGDE)
						writer.append(bean.toString() + newLine);
				}
			}
			writer.close();
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * create file bean of given file and subfolder.
	 * 
	 * @param file
	 * @param subfolder
	 * @return file bean
	 * @throws IOException
	 */
	private FileBean getFileBean(File file, String subfolder)
			throws IOException {
		FileBean bean = null;
		if (fileMap.containsKey(subfolder))
			bean = fileMap.get(subfolder).get(file.getName());
		if (bean != null && bean.lastDate == file.lastModified()
				&& !bean.isDeleted)
			return bean;
		isDirty = true;
		bean = new FileBean(subfolder, file.getName(), file.lastModified(),
				buildMD5(file), file.length(), file.isDirectory(), false);
		if (!fileMap.containsKey(subfolder))
			fileMap.put(subfolder, new HashMap<String, FileBean>());
		fileMap.get(subfolder).put(file.getName(), bean);
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

	@Override
	public void downloadCanceled() {
		System.out.println("receive canceled");
	}

	@Override
	public void endReceive(long size) {
		System.out.println("receive done");
	}

	@Override
	public void exceptionOccurred(Exception e) {
		System.out.println("receive exception: " + e.getMessage());
	}

	@Override
	public void progressReceive(long size) {
		System.out.println("prog: " + size);
	}

	@Override
	public void startReceive(long size) {
		System.out.println("start receive : " + size);
	}

}
