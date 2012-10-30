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
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.hcl.synchronize.api.IHCLClient;
import de.hcl.synchronize.api.IHCLClient.FileBean;
import de.hcl.synchronize.log.HCLLogger;
import de.hcl.synchronize.log.IHCLLogListener.HCLType;
import de.hcl.synchronize.util.IniFile;

/**
 * The subfolder class manages the knowledge of a subfolder with files,
 * directories, size, md5 and deletion.
 * 
 * @author sebastian
 */
public class Subfolder {

	/**
	 * MINIMAL_REFRESH_TIME specifies the minimal time between two directory
	 * scans. This avoids too often scans.
	 */
	public static final int MINIMAL_REFRESH_TIME_DIRECTORY = 1000 * 2;

	/**
	 * Section name in the ini file for cache.
	 */
	public static final String SECTION_DIRECTIY_HASH = "directory_hash";

	/**
	 * The cache file contains hashes of every subfolder.
	 */
	public static final String CACHE_FILE = HCLClient.CACHE_SUBFOLDER_DIRECTORY
			+ File.separator + "subolder.hash";

	/**
	 * The cache file contains hashes of every subfolder.
	 */
	private static IniFile cacheFile;

	/**
	 * create md5 code of given file.
	 * 
	 * @param file
	 * @return md5 of file
	 * @throws IOException
	 */
	public static byte[] buildMD5(File file) throws IOException {
		if (file.isDirectory())
			return null;
		try {
			MessageDigest md = MessageDigest.getInstance("md5");
			InputStream is = new FileInputStream(file);
			byte[] buffer = new byte[8192];
			int read = 0;
			while ((read = is.read(buffer)) > 0)
				md.update(buffer, 0, read);
			is.close();
			return md.digest();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Check given file if it is locked.
	 * 
	 * @param file
	 * @return ture, if file is locked and false otherwise
	 * @throws IOException
	 */
	public static boolean fileIsLocked(File file) throws IOException {
		RandomAccessFile accessFile = new RandomAccessFile(file, "rw");
		FileChannel channel = accessFile.getChannel();
		// Get an exclusive lock on the whole file
		FileLock lock = channel.lock();
		try {
			lock = channel.tryLock();
			// Ok. You get the lock
		} catch (OverlappingFileLockException e) {
			return true;
		} finally {
			accessFile.close();
			lock.release();
		}
		return false;
	}

	/**
	 * Check whether the file size is increasing in 200 ms or not.
	 * 
	 * @param file
	 * @return true, if file size was increased in 200 ms.
	 */
	public static boolean fileIsIncreasing(File file) {
		long length1 = file.length();
		try {
			Thread.sleep(125);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		boolean result = length1 != file.length();
		return result;
	}

	/**
	 * the sub file map contains all beans by their name
	 */
	private Map<String, FileBean> subFileMap = Collections
			.synchronizedMap(new HashMap<String, IHCLClient.FileBean>());

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
	 * Base path of the client
	 */
	private String basePath;

	/**
	 * The time stamp at the last directory scan.
	 */
	private long lastScan;

	/**
	 * The hash value of this directory with files, directories and their
	 * hashes.
	 */
	private String directoryHash;

	/**
	 * Minimal buffered directory cache.
	 */
	private int refreshRate;

	/**
	 * Allocate new subfolder at given path
	 * 
	 * @param subfolder
	 */
	public Subfolder(String subfolder, String basePath, int refreshRate) {
		fileMapFile = new File(basePath + HCLClient.CACHE_SUBFOLDER_DIRECTORY
				+ subfolderToCache(subfolder));
		try {
			if (cacheFile == null) {
				File file = new File(basePath + CACHE_FILE);
				if (!file.exists())
					file.createNewFile();
				cacheFile = new IniFile(file);
			}
			if (!fileMapFile.exists())
				fileMapFile.createNewFile();
		} catch (IOException e) {
		}
		this.basePath = basePath;
		this.subfolder = subfolder;
		this.refreshRate = refreshRate;
		isDirty = false;
		lastScan = 0;
		directoryHash = cacheFile.getPropertyString(SECTION_DIRECTIY_HASH,
				subfolder, null);
	}

	/**
	 * Remove given fileName from the file map.
	 * 
	 * @param fileName
	 */
	public void remove(String fileName) {
		subFileMap.remove(fileName);
		isDirty = true;
		directoryHash = null;
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
		if (subFileMap == null)
			read();
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
		directoryHash = null;
	}

	/**
	 * Change subfolder string to cache name of the subfilemap.
	 * 
	 * @param subfolder
	 * @return String
	 */
	private static String subfolderToCache(String subfolder) {
		String splash = "_";
		String cacheName = subfolder.replaceAll(File.separator, splash)
				+ ".cache";
		if (subfolder.contains(splash))
			for (String folder : subfolder.split(File.separator)) {
				cacheName += "$" + countString(folder, splash);
			}
		return cacheName;
	}

	private static int countString(String string, String sequence) {
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
	private Map<String, FileBean> read() {
		Map<String, IHCLClient.FileBean> subFileMap = new HashMap<String, IHCLClient.FileBean>();
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
		return subFileMap;
	}

	/**
	 * save the file map to cache file map in csv format.
	 * 
	 * @param file
	 * @param map
	 */
	public void write() {
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
				if (!bean.isDeleted()
						|| (now - bean.lastDate) < HCLClient.MAXIMUM_KNOWLEGDE) {
					if (!bean.isReceiving())
						writer.append(bean.toString() + newLine);
				}
			}
			writer.close();
			fileWriter.close();
		} catch (IOException e) {
			HCLLogger.performLog("Write cache data: " + e.getMessage(),
					HCLType.ERROR, this);
		}
		isDirty = false;
	}

	/**
	 * create file bean of given file and subfolder.
	 * 
	 * @param file
	 * @param subManager
	 * @return file bean
	 * @throws IOException
	 */
	public synchronized FileBean getFileBean(File file) throws IOException {
		if (file == null || !file.exists())
			throw new FileNotFoundException("File :'" + file + "' not found.");
		FileBean bean = getFileBean(file.getName());
		if (bean != null && bean.lastDate == file.lastModified()
				&& !bean.isDeleted() && !bean.isCopying())
			return bean;
		byte flags = FileBean.DONE | FileBean.EXISTS;
		byte[] md5 = new byte[16];
		long lastDate = file.lastModified();
		if (bean != null && bean.isDeleted()) {
			lastDate = bean.lastDate + 1000;
		} else if (bean != null)
			lastDate = Math.max(bean.lastDate, lastDate);
		if (file.isFile())
			flags |= FileBean.FILE;
		if ((!file.canWrite()) || fileIsIncreasing(file)) {
			flags |= FileBean.COPY;
		} else
			md5 = buildMD5(file);
		if (lastDate != file.lastModified())
			file.setLastModified(lastDate);
		lastDate = file.lastModified();
		bean = new FileBean(getSubfolder(), file.getName(), lastDate, md5,
				file.length(), flags);
		push(bean);
		return bean;
	}

	/**
	 * Get the file bean list of this subfolder.
	 * 
	 * @return list of filebeans
	 * @throws RemoteException
	 * @throws IOException
	 */
	public List<FileBean> listFiles() throws RemoteException, IOException {
		if (subFileMap == null)
			subFileMap = read();
		long startTime = System.currentTimeMillis();
		if (startTime < lastScan + refreshRate)
			return new ArrayList<IHCLClient.FileBean>(subFileMap.values());
		List<FileBean> list = new ArrayList<FileBean>();
		File directory = new File(basePath + subfolder);
		if (!directory.exists())
			return list;
		for (String str : directory.list()) {
			File file = new File(basePath + subfolder + str);
			if (str.length() > 0 && str.charAt(0) != '.')
				list.add(getFileBean(file));
			if (System.currentTimeMillis() > startTime + 1000 * 60)
				write();
		}
		List<FileBean> deletedBean = new ArrayList<IHCLClient.FileBean>();
		for (FileBean bean : getFileBeans()) {
			if (!list.contains(bean) && !bean.isDeleted()) {
				bean.flags &= ~FileBean.EXISTS;
				bean.lastDate = System.currentTimeMillis();
				FileBean newBean = new FileBean(bean);
				list.add(newBean);
				deletedBean.add(newBean);
			} else if (bean.isDeleted())
				list.add(bean);
		}
		for (FileBean bean : deletedBean)
			push(bean);
		write();
		lastScan = System.currentTimeMillis();
		return list;
	}

	/**
	 * Get the hash code of this directory. All Files, Directories and their
	 * hash.
	 * 
	 * @return hash of this directory
	 */
	public String getDirectoryHash() {
		if (System.currentTimeMillis() > lastScan + refreshRate)
			try {
				listFiles();
			} catch (Exception e) {
			}
		if (directoryHash == null) {
			directoryHash = calculateDirectoryHash();
			cacheFile.setPropertyString(SECTION_DIRECTIY_HASH, subfolder,
					directoryHash);
			try {
				cacheFile.writeFile();
			} catch (IOException e) {
			}
		}
		return directoryHash;
	}

	/**
	 * Calculate hash code of this directory. All Files, Directories and their
	 * md5 hash.
	 * 
	 * @return md5 hash
	 */
	private String calculateDirectoryHash() {
		try {
			MessageDigest md = MessageDigest.getInstance("md5");

			for (FileBean bean : sortFiles(listFiles())) {
				if (bean.isDeleted())
					continue;
				md.update(bean.file.getBytes());
				if (!bean.isDirectory())
					md.update(bean.md5);
			}
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

	private List<FileBean> sortFiles(List<FileBean> list) {
		Collections.sort(list, new Comparator<FileBean>() {
			@Override
			public int compare(FileBean o1, FileBean o2) {
				if (o1.isDirectory() && !o2.isDirectory())
					return 1;
				if (!o1.isDirectory() && o2.isDirectory())
					return -1;
				return o1.file.compareTo(o2.file);
			}
		});
		return list;
	}

	/**
	 * Set given file name to deleted. If there is such a file bean it will be
	 * returned.
	 * 
	 * @param file
	 * @return Filebean
	 */
	public FileBean setDeletedFile(String file) {
		FileBean fileBean = getFileBean(file);
		if (fileBean != null) {
			FileBean newBean = new FileBean(fileBean);
			newBean.flags &= ~FileBean.EXISTS;
			newBean.lastDate = System.currentTimeMillis();
			push(newBean);
			return newBean;
		}
		return null;
	}
	
	public boolean reduceStoreage(){
		boolean reduce = false;
		if (subFileMap != null && subFileMap.size() > 0)
			reduce = true;
		subFileMap = null;
		return reduce;
	}

}