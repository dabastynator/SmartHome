package de.hcl.synchronize.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.hcl.synchronize.api.IHCLClient;
import de.hcl.synchronize.api.IHCLClient.FileBean;

/**
 * The transfer queue handles multiple transfer jobs with a pool of worker.
 * 
 * @author sebastian
 * 
 */
public class TransferQueue {

	/**
	 * starting file port
	 */
	public static final int FILE_PORT = 5080;

	/**
	 * count of worker
	 */
	public static final int WORKER_COUNT = 1;

	/**
	 * job queue
	 */
	private List<TransferJob> jobQueue;

	/**
	 * active jobs
	 */
	private List<TransferJob> activeJob;

	/**
	 * queue of worker
	 */
	private List<TransferWorker> workerQueue;

	/**
	 * true if the queue is active
	 */
	private boolean isActive;

	/**
	 * Allocate new queue. The worker will be created.
	 */
	public TransferQueue() {
		workerQueue = new ArrayList<TransferWorker>();

		jobQueue = Collections
				.synchronizedList(new ArrayList<TransferQueue.TransferJob>());
		activeJob = Collections
				.synchronizedList(new ArrayList<TransferQueue.TransferJob>());
		for (int i = 0; i < WORKER_COUNT; i++)
			workerQueue.add(new TransferWorker(this, FILE_PORT + i));
	}

	/**
	 * start the queue.
	 */
	public void startTransfering() {
		isActive = true;
		new Thread() {
			@Override
			public void run() {
				while (isActive) {
					boolean transfering = handleQueue();
					try {
						if (!transfering)
							Thread.sleep(1000);
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}

	/**
	 * handle the queue.
	 * 
	 * @return true if there was a job and free worker.
	 */
	private boolean handleQueue() {
		if (jobQueue.size() > 0 && workerQueue.size() > 0) {
			TransferJob job = jobQueue.get(0);
			TransferWorker worker = workerQueue.remove(0);
			activeJob.add(job);
			jobQueue.remove(0);
			worker.setJob(job);
			return true;
		}
		return false;
	}

	/**
	 * stop transferring queue
	 */
	public void stopTransfering() {
		isActive = false;
	}

	/**
	 * push job to be handled
	 * 
	 * @param job
	 */
	public void pushJob(TransferJob job) {
		if (!jobQueue.contains(job) && !activeJob.contains(job))
			jobQueue.add(job);
	}

	/**
	 * the transfer job contains infromation about the job.
	 * 
	 * @author sebastian
	 */
	public static class TransferJob {

		public enum TransferType {
			FILE, DIRECTORY
		};

		public IHCLClient sender;

		public IHCLClient receiver;

		public FileBean object;

		public TransferType type;

		public TransferJob(IHCLClient sender, IHCLClient receiver,
				FileBean file, TransferType type) {
			this.sender = sender;
			this.receiver = receiver;
			this.object = file;
			this.type = type;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof TransferJob) {
				TransferJob job = (TransferJob) obj;
				boolean equal = object.equals(job.object)
						&& sender == job.sender && receiver == job.receiver;
				return equal;
			}
			return false;
		}

	}

	public void pushWorker(TransferWorker transferWorker) {
		activeJob.remove(transferWorker.getJob());
		workerQueue.add(transferWorker);
	}

}
