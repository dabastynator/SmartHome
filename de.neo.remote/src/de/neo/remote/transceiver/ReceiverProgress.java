package de.neo.remote.transceiver;

/**
 * the interface provides to listen on the progress of a receiving process.
 * 
 * @author sebastian
 */
public interface ReceiverProgress {

	/**
	 * start receive, complete size of the data and file will be given.
	 * 
	 * @param size
	 * @param file
	 */
	public void startReceive(long size, String file);

	/**
	 * new progress and current file will be set.
	 * 
	 * @param size
	 * @param file
	 */
	public void progressReceive(long size, String file);

	/**
	 * the end of a transmission is reached.
	 */
	public void endReceive(long size);

	/**
	 * inform listener about occurred exception
	 * 
	 * @param e
	 */
	public void exceptionOccurred(Exception e);

	/**
	 * 
	 */
	public void downloadCanceled();

}
