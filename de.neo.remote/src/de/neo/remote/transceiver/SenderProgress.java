package de.neo.remote.transceiver;

/**
 * the interface provides to listen on the progress of an sending process.
 * 
 * @author sebastian
 */
public interface SenderProgress {

	/**
	 * start sending, complete size of the data will be given.
	 * 
	 * @param size
	 */
	public void startSending(long size);

	/**
	 * new progress will be set
	 * 
	 * @param size
	 */
	public void progressSending(long size);

	/**
	 * the end of a transmission is reached.
	 */
	public void endSending(long size);

	/**
	 * inform listener about occurred exception
	 * 
	 * @param e
	 */
	public void exceptionOccurred(Exception e);
	
	/**
	 * 
	 */
	public void sendingCanceled();
	
}
