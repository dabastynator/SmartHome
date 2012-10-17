package de.hcl.synchronize.jobs;

import java.io.IOException;

import de.newsystem.rmi.protokol.RemoteException;

/**
 * The home cloud job executes an action to synchronize a client
 * 
 * @author sebastian
 */
public interface HCLJob {

	/**
	 * Execute the job. If any file will be transmitted, the specified port will
	 * be used.
	 */
	public void execute(int port) throws IOException, RemoteException;

}