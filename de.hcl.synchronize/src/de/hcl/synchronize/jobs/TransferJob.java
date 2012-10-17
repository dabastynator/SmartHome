package de.hcl.synchronize.jobs;

import java.io.IOException;

import de.hcl.synchronize.api.IHCLClient;
import de.hcl.synchronize.api.IHCLClient.FileBean;
import de.newsystem.rmi.protokol.RemoteException;

/**
 * the transfer job contains infromation about the job.
 * 
 * @author sebastian
 */
public class TransferJob implements HCLJob {

	public IHCLClient sender;

	public IHCLClient receiver;

	public FileBean object;

	public TransferJob(IHCLClient sender, IHCLClient receiver, FileBean file) {
		this.sender = sender;
		this.receiver = receiver;
		this.object = file;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TransferJob) {
			TransferJob job = (TransferJob) obj;
			boolean equal = object.equals(job.object) && sender == job.sender
					&& receiver == job.receiver;
			return equal;
		}
		return false;
	}

	@Override
	public void execute(int port) throws IOException, RemoteException {
		if (!object.isDirectory()) {
			if (object.isDeleted()) {
				receiver.deleteFile(object.subfolder , object.file);
			} else {
				String ip = sender.sendFile(object, port);
				receiver.receiveFile(object, ip, port);
			}
		} else {
			if (object.isDeleted()) {
				receiver.deleteDirectory(object.subfolder + object.file);
			} else
				receiver.createDirectory(object.subfolder, object.file);
		}
	}

}