package de.remote.api;

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
