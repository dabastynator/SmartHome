package de.hcl.synchronize.client;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;

import net.contentobjects.jnotify.JNotify;
import net.contentobjects.jnotify.JNotifyException;
import net.contentobjects.jnotify.JNotifyListener;
import de.hcl.synchronize.api.IHCLSession;
import de.hcl.synchronize.log.HCLLogger;
import de.newsystem.rmi.protokol.RemoteException;

/**
 * The fat client listens to the directory without polling (JNotify is used for
 * this). It tells all modifications to the session.
 * 
 * @author sebastian
 */
public class FatClient extends HCLClient implements JNotifyListener {

	/**
	 * Id of directory listening
	 */
	private int watchID;

	/**
	 * Current session to inform all clients.
	 */
	private IHCLSession session;

	/**
	 * Buffers last notification name.
	 */
	private String lastName;

	/**
	 * Buffers last notification time stamp.
	 */
	private long lastInfo;

	/**
	 * Allocate new fat client. the fat client listens to the directory without
	 * polling by using JNotify.
	 * 
	 * @param basePath
	 * @param name
	 * @param session
	 * @param refreshRate 
	 * @throws IOException
	 */
	public FatClient(String basePath, String name, IHCLSession session,
			boolean readOnly, int refreshRate) throws IOException {
		super(basePath, name, readOnly, refreshRate);
		configureLIBFolder();
		this.session = session;
		startWatch();
	}

	/**
	 * Extract the os specific files to the lib folder to use JNotify
	 * 
	 * @throws IOException
	 */
	private void configureLIBFolder() throws IOException {
		String libPath = basePath + CACHE_DIRECTORY + "lib" + File.separator;
		String[] libFiles = { "CHANGELOG", "epl.html", "jnotify.dll",
				"jnotify_64bit.dll", "jnotify-0.94.jar",
				"jnotify-0.94-src.zip", "jnotify-native-linux-0.94-src.zip",
				"jnotify-native-macosx-0.94-src.zip",
				"jnotify-native-win32-0.94-src.zip", "lgpl.txt",
				"libjnotify.jnilib", "libjnotify.so", "README",
				"64-bit Linux" + File.separator + "libjnotify.so" };
		File libFolder = new File(libPath);
		if (!libFolder.exists()) {
			libFolder.mkdir();
			new File(libPath + "64-bit Linux").mkdir();
			for (String lib : libFiles)
				copyFileFromJar(this, "lib/" + lib, new File(libPath + lib));
		}
		try {
			addLibraryPath(libPath);
		} catch (Exception e) {
			HCLLogger.performLog("Fail to load library", HCLType.ERROR, this);
			e.printStackTrace();
		}
	}

	/**
	 * Adds the specified path to the java library path
	 * 
	 * @param pathToAdd
	 *            the path to add
	 * @throws Exception
	 */
	public static void addLibraryPath(String pathToAdd) throws Exception {
		final Field usrPathsField = ClassLoader.class
				.getDeclaredField("usr_paths");
		usrPathsField.setAccessible(true);

		// get array of paths
		final String[] paths = (String[]) usrPathsField.get(null);

		// check if the path to add is already present
		for (String path : paths) {
			if (path.equals(pathToAdd)) {
				return;
			}
		}

		// add the new path
		final String[] newPaths = Arrays.copyOf(paths, paths.length + 1);
		newPaths[newPaths.length - 1] = pathToAdd;
		usrPathsField.set(null, newPaths);
	}

	@Override
	public void fileRenamed(int wd, String rootPath, String oldName,
			String newName) {
		if (newName.startsWith(".hcl"))
			return;
		bubblingIdiot(newName);
		String subfolder = getSubfolderOfName(oldName);
		Subfolder subFileMap = getSubFileMap(subfolder);
		oldName = getFileOfName(oldName);
		newName = getFileOfName(newName);
		if (!oldName.startsWith(".") && !newName.startsWith("."))
			handleRename(subFileMap, oldName, newName);
		else if (oldName.startsWith(".") && !newName.startsWith("."))
			handleNewFile(subFileMap, newName);
		else if (!oldName.startsWith(".") && newName.startsWith("."))
			handleDeletion(subFileMap, oldName);
		if (new File(rootPath + newName).isDirectory()) {
			stopWatch();
			startWatch();
		}
	}

	@Override
	public void fileModified(int wd, String rootPath, String name) {
		if (bubblingIdiot(name))
			return;
		String subfolder = getSubfolderOfName(name);
		Subfolder subFileMap = getSubFileMap(subfolder);
		try {
			FileBean bean = subFileMap.getFileBean(getFileOfName(name));
			if (bean.isReceiving())
				return;
			session.modifiedFile(this, bean);
		} catch (Exception e) {
			HCLLogger.performLog(
					"Error handle modified file: " + e.getMessage(),
					HCLType.ERROR, this);
		}
	}

	@Override
	public void fileDeleted(int wd, String rootPath, String name) {
		if (bubblingIdiot(name))
			return;
		String subfolder = getSubfolderOfName(name);
		Subfolder subFileMap = getSubFileMap(subfolder);
		handleDeletion(subFileMap, getFileOfName(name));
	}

	@Override
	public void fileCreated(int wd, String rootPath, String name) {
		if (bubblingIdiot(name))
			return;
		String subfolder = getSubfolderOfName(name);
		Subfolder subFileMap = getSubFileMap(subfolder);
		handleNewFile(subFileMap, getFileOfName(name));
	}

	/**
	 * Check if name is new name.
	 * 
	 * @param name
	 * @return true if the name is send 10 ms ago
	 */
	private boolean bubblingIdiot(String name) {
		boolean relevant = !getFileOfName(name).startsWith(".")
				&& !name.endsWith("/") && !name.startsWith(".");
		if (!relevant)
			return true;
		if (!new File(basePath + name).exists())
			return true;
		boolean newValue = !name.equals(lastName)
				|| lastInfo < System.currentTimeMillis() - 50;
		lastName = name;
		lastInfo = System.currentTimeMillis();
		return !newValue;
	}

	/**
	 * Get subfolder path of name. Cut file name after last file separator.
	 * 
	 * @param name
	 * @return subfolder
	 */
	private String getSubfolderOfName(String name) {
		if (name.length() > 0)
			return name.substring(0, name.lastIndexOf(File.separator) + 1);
		return "";
	}

	/**
	 * Get file name of name. Cut file path before last separator.
	 * 
	 * @param name
	 * @return file name
	 */
	private String getFileOfName(String name) {
		if (name.contains(File.separator))
			return name.substring(name.lastIndexOf(File.separator) + 1);
		return name;
	}

	/**
	 * Handle deleted file. Tell session and subfolder about deletion.
	 * 
	 * @param subFileMap
	 * @param name
	 */
	private void handleDeletion(Subfolder subFileMap, String name) {
		FileBean bean = subFileMap.getFileBean(name);
		try {
			if (bean == null || bean.isDeleted())
				return;
			subFileMap.setDeletedFile(name);
			session.deleteFile(this, bean);
			HCLLogger.performLog("Synchronize deleted file: '" + name + "'.",
					HCLType.SEND, this);
		} catch (Exception e) {
			HCLLogger.performLog("Error handle deletion: " + e.getMessage(),
					HCLType.ERROR, this);
		}
	}

	/**
	 * Handle rename of file. Tell session and subfolder about renaming.
	 * 
	 * @param subFileMap
	 * @param oldName
	 * @param newName
	 */
	private void handleRename(Subfolder subFileMap, String oldName,
			String newName) {
		try {
			FileBean bean = subFileMap.getFileBean(oldName);
			if (bean == null) {
				if (subFileMap.getFileBean(newName) != null)
					return;
				bean = subFileMap.getFileBean(new File(basePath
						+ subFileMap.getSubfolder() + newName));
			} else {
				bean = new FileBean(bean);
				bean.file = newName;
				subFileMap.remove(oldName);
				subFileMap.push(bean);
			}
			session.renameFile(this, subFileMap.getSubfolder(), oldName,
					newName);
			HCLLogger.performLog("Rename file '" + oldName + "' to '" + newName
					+ "'.", HCLType.UPDATE, this);
		} catch (Exception e) {
			HCLLogger.performLog("Error handle rename: " + e.getMessage(),
					HCLType.ERROR, this);
		}
	}

	/**
	 * Handle new file. Tell session and subfolder about new file.
	 * 
	 * @param subFileMap
	 * @param name
	 */
	private void handleNewFile(Subfolder subFileMap, String name) {
		try {
			FileBean bean = subFileMap.getFileBean(new File(basePath
					+ subFileMap.getSubfolder() + name));
			if (bean.isReceiving() || bean.isCopying())
				return;
			session.createFile(this, bean);
		} catch (Exception e) {
			HCLLogger.performLog("Error handling new file: " + e.getMessage(),
					HCLType.ERROR, this);
			e.printStackTrace();
		}
	}

	@Override
	public void renameFile(String subfolder, String oldName, String newName)
			throws RemoteException, IOException {
		super.renameFile(subfolder, oldName, newName);
		File file = new File(basePath + subfolder + newName);
		if (file != null && file.exists() && file.isDirectory()) {
			stopWatch();
			startWatch();
		}
	}

	@Override
	public boolean createDirectory(String subfolder, String directoryName)
			throws RemoteException, IOException {
		stopWatch();
		boolean result = super.createDirectory(subfolder, directoryName);
		startWatch();
		return result;
	}

	@Override
	public boolean deleteDirectory(String directory) throws RemoteException,
			IOException {
		stopWatch();
		boolean result = super.deleteDirectory(directory);
		startWatch();
		return result;
	}

	/**
	 * Stop watching the directory. remove watching id from jnotify.
	 */
	public void stopWatch() {
		try {
			JNotify.removeWatch(watchID);
		} catch (Exception e) {
			HCLLogger.performLog("Error stopping watcher for directory.",
					HCLType.ERROR, this);
		}
	}

	/**
	 * Start watching the directory.
	 */
	public void startWatch() {
		try {
			int mask = JNotify.FILE_CREATED | JNotify.FILE_DELETED
					| JNotify.FILE_MODIFIED | JNotify.FILE_RENAMED;
			boolean watchSubtree = true;
			watchID = JNotify.addWatch(basePath, mask, watchSubtree, this);
		} catch (JNotifyException e) {
			HCLLogger.performLog("Error starting watcher for directory.",
					HCLType.ERROR, this);
		}
	}

}
