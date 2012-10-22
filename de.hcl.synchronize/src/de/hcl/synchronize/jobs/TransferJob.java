package de.hcl.synchronize.jobs;

import java.io.IOException;

import de.hcl.synchronize.api.IHCLClient;
import de.hcl.synchronize.api.IHCLClient.FileBean;
import de.hcl.synchronize.log.HCLLogger;
import de.hcl.synchronize.log.IHCLLogListener.HCLType;
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

	private Object author;

	public TransferJob(IHCLClient sender, IHCLClient receiver, FileBean file,
			Object author) {
		this.sender = sender;
		this.receiver = receiver;
		this.object = file;
		this.author = author;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TransferJob) {
			TransferJob job = (TransferJob) obj;
			boolean equalObject = object.subfolder.equals(job.object.subfolder)
					&& object.file.equals(job.object.file);
			boolean equalTransceiver = sender == job.sender
					&& receiver == job.receiver;
			return equalObject && equalTransceiver;
		}
		return false;
	}

	@Override
	public void execute(int port) throws IOException, RemoteException {
		HCLLogger.performLog("Execute transfer job: '" + object.file + "'.",
				HCLType.INFORMATION, author);
		if (!object.isDirectory()) {
			if (object.isDeleted()) {
				receiver.deleteFile(object.subfolder, object.file);
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