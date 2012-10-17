package de.hcl.synchronize.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.hcl.synchronize.jobs.HCLJob;

/**
 * The transfer queue handles multiple transfer jobs with a pool of worker.
 * 
 * @author sebastian
 * 
 */
public class JobQueue {

	/**
	 * job queue
	 */
	private List<HCLJob> jobQueue;

	/**
	 * active jobs
	 */
	private List<HCLJob> activeJob;

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
	public JobQueue(int startPort, int workerCount) {
		workerQueue = new ArrayList<TransferWorker>();

		jobQueue = Collections.synchronizedList(new ArrayList<HCLJob>());
		activeJob = Collections.synchronizedList(new ArrayList<HCLJob>());
		for (int i = 0; i < workerCount; i++)
			workerQueue.add(new TransferWorker(this, startPort + i));
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
			HCLJob job = jobQueue.get(0);
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
	public void pushJob(HCLJob job) {
		if (!jobQueue.contains(job) && !activeJob.contains(job))
			jobQueue.add(job);
	}

	public void pushWorker(TransferWorker transferWorker) {
		activeJob.remove(transferWorker.getJob());
		workerQueue.add(transferWorker);
	}

}
