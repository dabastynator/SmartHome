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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.hcl.synchronize.api.IHCLClient;
import de.hcl.synchronize.api.IHCLClient.FileBean;

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
	public static final int MINIMAL_REFRESH_TIME = 1000 * 5;

	/**
	 * create md5 code of given file.
	 * 
	 * @param file
	 * @return md5 of file
	 */
	public static String buildMD5(File file) {
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
	 * Base path of the client
	 */
	private String basePath;

	/**
	 * The time stamp at the last directory scan.
	 */
	private long lastScan;

	/**
	 * Allocate new subfolder at given path
	 * 
	 * @param subfolder
	 */
	public Subfolder(String subfolder, String basePath) {
		fileMapFile = new File(basePath + HCLClient.CACHE_SUBFOLDER_DIRECTORY
				+ subfolderToCache(subfolder));
		if (!fileMapFile.exists())
			try {
				fileMapFile.createNewFile();
			} catch (IOException e) {
			}
		subFileMap = new HashMap<String, IHCLClient.FileBean>();
		this.basePath = basePath;
		this.subfolder = subfolder;
		isDirty = false;
		lastScan = 0;
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
				if (!bean.isDeleted
						|| (now - bean.lastDate) < HCLClient.MAXIMUM_KNOWLEGDE)
					writer.append(bean.toString() + newLine);
			}
			writer.close();
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
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
	public FileBean getFileBean(File file) throws IOException {
		FileBean bean = getFileBean(file.getName());
		if (bean != null && bean.lastDate == file.lastModified()
				&& !bean.isDeleted)
			return bean;
		bean = new FileBean(getSubfolder(), file.getName(),
				file.lastModified(), buildMD5(file), file.length(),
				file.isDirectory(), false);
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
		long startTime = System.currentTimeMillis();
		if (startTime < lastScan + MINIMAL_REFRESH_TIME)
			return new ArrayList<IHCLClient.FileBean>(subFileMap.values());
		List<FileBean> list = new ArrayList<FileBean>();
		File directory = new File(basePath + subfolder);
		for (String str : directory.list()) {
			File file = new File(basePath + subfolder + str);
			if (str.length() > 0 && str.charAt(0) != '.')
				list.add(getFileBean(file));
			if (System.currentTimeMillis() > startTime + 1000 * 60)
				write();
		}
		List<FileBean> deletedBean = new ArrayList<IHCLClient.FileBean>();
		for (FileBean bean : getFileBeans()) {
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
			push(bean);
		write();
		lastScan = System.currentTimeMillis();
		return list;
	}

	/**
	 * Get hash code of this directory. All Files, Directories and their md5
	 * hash.
	 * 
	 * @return md5 hash
	 */
	public String getDirectoryHash() {
		try {
			MessageDigest md = MessageDigest.getInstance("md5");

			for (FileBean bean : sortFiles(listFiles())) {
				if (bean.isDeleted)
					continue;
				md.update(bean.file.getBytes());
				if (!bean.isDirectory)
					md.update(bean.md5.getBytes());
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
				if (o1.isDirectory && !o2.isDirectory)
					return 1;
				if (!o1.isDirectory && o2.isDirectory)
					return -1;
				return o1.file.compareTo(o2.file);
			}
		});
		return list;
	}

}