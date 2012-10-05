package de.hcl.synchronize.server;

import java.io.IOException;

import de.hcl.synchronize.server.TransferQueue.TransferJob;
import de.newsystem.rmi.protokol.RemoteException;

public class TransferWorker {

	/**
	 * the port on witch the object will be transfered
	 */
	private int port;

	/**
	 * the queue that handles all worker
	 */
	private TransferQueue queue;

	/**
	 * the current job of the worker
	 */
	private TransferJob job;

	/**
	 * allocate new worker.
	 * 
	 * @param port
	 */
	public TransferWorker(TransferQueue queue, int port) {
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
				if (!job.object.isDirectory) {
					if (job.object.isDeleted) {
						job.receiver.deleteFile(job.object.subfolder
								+ job.object.file);
					} else {
						String ip = job.sender.sendFile(job.object.subfolder
								+ job.object.file, port);
						job.receiver.receiveFile(job.object.file,
								job.object.subfolder, ip, port);
					}
				} else {
					if (job.object.isDeleted) {
						job.receiver.deleteDirectory(job.object.subfolder
								+ job.object.file);
					} else
						job.receiver.createDirectory(job.object.subfolder,
								job.object.file);
				}

			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (IOException e) {
				System.err.println(e.getMessage());
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				queue.pushWorker(this);
			}
		}
	}

	public synchronized void setJob(TransferJob job) {
		this.job = job;
		notify();
	}

	public TransferJob getJob() {
		return job;
	}

}
