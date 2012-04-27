package de.newsystem.rmi.transceiver;

/**
 * the interface provides to listen on the progress of an receive.
 * 
 * @author sebastian
 */
public interface ReceiverProgress {

	/**
	 * start receive, complete size of the data will be given.
	 * 
	 * @param size
	 */
	public void startReceive(long size);

	/**
	 * new progress will be set
	 * 
	 * @param size
	 */
	public void progressReceive(long size);

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

}
