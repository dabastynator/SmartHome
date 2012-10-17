package de.hcl.synchronize.server;

import java.io.IOException;

import de.hcl.synchronize.jobs.HCLJob;
import de.newsystem.rmi.protokol.RemoteException;

public class TransferWorker {

	/**
	 * the port on witch the object will be transfered
	 */
	private int port;

	/**
	 * the queue that handles all worker
	 */
	private JobQueue queue;

	/**
	 * the current job of the worker
	 */
	private HCLJob job;

	/**
	 * allocate new worker.
	 * 
	 * @param port
	 */
	public TransferWorker(JobQueue queue, int port) {
		this.port = port;
		this.queue = queue;
		new Thread() {
			public void run() {
				work();
			};
		}.start();
	}

	private synchronized void work() {
		while (true) {
			try {
				wait();
				performeJob(job);

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				queue.pushWorker(this);
			}
		}
	}

	private void performeJob(HCLJob job) throws RemoteException, IOException {
		job.execute(port);
	}

	public synchronized void setJob(HCLJob job) {
		this.job = job;
		notify();
	}

	public HCLJob getJob() {
		return job;
	}

}
