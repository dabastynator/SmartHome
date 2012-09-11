package de.remote.api;

import java.io.IOException;

import de.newsystem.rmi.protokol.RemoteAble;
import de.newsystem.rmi.protokol.RemoteException;

/**
 * the browser browses throw a file system
 * 
 * @author sebastian
 */
public interface IBrowser extends RemoteAble {

	/**
	 * @return isRoot
	 * @throws RemoteException
	 */
	boolean goBack() throws RemoteException;

	/**
	 * goes into a directory
	 * 
	 * @param directory
	 * @throws RemoteException
	 */
	void goTo(String directory) throws RemoteException;

	/**
	 * returns a list of all directories in this directory
	 * 
	 * @return directories
	 * @throws RemoteException
	 */
	String[] getDirectories() throws RemoteException;

	/**
	 * returns a list of all files in this directory
	 * 
	 * @return files
	 * @throws RemoteException
	 */
	String[] getFiles() throws RemoteException;

	/**
	 * returns the current folder name
	 * 
	 * @return location
	 * @throws RemoteException
	 */
	String getLocation() throws RemoteException;

	/**
	 * return the current full location
	 * 
	 * @return location
	 * @throws RemoteException
	 */
	String getFullLocation() throws RemoteException;

	/**
	 * publish given file, so a client can connect to the given port and
	 * download the file. the implementation must return the ip of the server on
	 * witch the file can be download.
	 * 
	 * @param file
	 * @param port
	 * @return ip of the file server
	 * @throws RemoteException
	 * @throws IOException
	 */
	String publishFile(String file, int port) throws RemoteException,
			IOException;

	/**
	 * start download from published file. The implementation must connect to
	 * given ip and port to load the file.
	 * 
	 * @param file
	 * @param ip
	 *            of the server
	 * @param port
	 * @throws RemoteException
	 */
	void updloadFile(String file, String serverIp, int port)
			throws RemoteException;

	/**
	 * publish given directory, so a client can connect to the given port and
	 * download the directory. the implementation must return the ip of the
	 * server on witch the directory can be download.
	 * 
	 * @param directory
	 * @param port
	 * @return ip of the file server
	 * @throws RemoteException
	 * @throws IOException
	 */
	String publishDirectory(String directory, int port) throws RemoteException,
			IOException;

	/**
	 * Deletes the file or directory denoted by this abstract pathname. If this
	 * pathname denotes a directory, then the directory must be empty in order
	 * to be deleted.
	 * 
	 * @param file
	 * @return true if and only if the file or directory is successfully
	 *         deleted; false otherwise
	 * @throws RemoteException
	 */
	boolean delete(String file) throws RemoteException;

}
