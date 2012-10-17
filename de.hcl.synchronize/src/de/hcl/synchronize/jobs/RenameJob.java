package de.hcl.synchronize.jobs;

import java.io.IOException;

import de.hcl.synchronize.api.IHCLClient;
import de.newsystem.rmi.protokol.RemoteException;

public class RenameJob implements HCLJob {

	private IHCLClient client;
	private String subfolder;
	private String oldName;
	private String newName;

	public RenameJob(IHCLClient client, String subfolder, String oldName,
			String newName) {
		this.client = client;
		this.subfolder = subfolder;
		this.oldName = oldName;
		this.newName = newName;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof RenameJob))
			return false;
		RenameJob job = (RenameJob) obj;
		return subfolder.equals(job.subfolder) && oldName.equals(job.oldName)
				&& newName.equals(job.newName) && client.equals(job.client);
	}

	@Override
	public void execute(int port) throws IOException, RemoteException {
		client.renameFile(subfolder, oldName, newName);
	}

}