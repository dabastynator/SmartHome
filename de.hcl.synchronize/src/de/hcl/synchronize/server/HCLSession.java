package de.hcl.synchronize.server;

import java.util.ArrayList;
import java.util.List;

import de.hcl.synchronize.api.IHCLClient;
import de.hcl.synchronize.api.IHCLClient.FileBean;
import de.hcl.synchronize.api.IHCLSession;
import de.hcl.synchronize.jobs.RenameJob;
import de.hcl.synchronize.jobs.TransferJob;
import de.hcl.synchronize.log.HCLLogger;
import de.hcl.synchronize.log.IHCLLogListener.HCLType;
import de.newsystem.rmi.protokol.RemoteException;

/**
 * The home cloud session holds a list of clients with same session ids. Clients
 * can notify the session about changes in file system.
 * 
 * @author sebastian
 */
public class HCLSession implements IHCLSession {

	/**
	 * list of all clients
	 */
	private List<IHCLClient> clients = new ArrayList<IHCLClient>();

	/**
	 * id of this session
	 */
	private String sessionID;

	/**
	 * The queue handles all jobs.
	 */
	private JobQueue queue;

	public HCLSession(String sessionID, JobQueue queue) {
		this.sessionID = sessionID;
		this.queue = queue;
	}

	@Override
	public IHCLClient getClient(int index) throws RemoteException {
		return clients.get(index);
	}

	@Override
	public int getClientSize() throws RemoteException {
		return clients.size();
	}

	@Override
	public boolean addClient(IHCLClient client) throws RemoteException {
		return clients.add(client);
	}

	@Override
	public boolean removeClient(IHCLClient client) throws RemoteException {
		return clients.remove(client);
	}

	@Override
	public String getSessionID() throws RemoteException {
		return sessionID;
	}

	@Override
	public void createFile(IHCLClient client, FileBean bean)
			throws RemoteException {
		sendFileBeanToClients(client, bean);
	}

	@Override
	public void deleteFile(IHCLClient sender, FileBean bean)
			throws RemoteException {
		sendFileBeanToClients(sender, bean);
	}

	@Override
	public void renameFile(IHCLClient sender, String subfolder, String oldName,
			String newName) throws RemoteException {
		for (IHCLClient client : clients) {
			if (client.equals(sender))
				continue;
			queue.pushJob(new RenameJob(client, subfolder, oldName, newName));
		}
	}

	@Override
	public void modifiedFile(IHCLClient client, FileBean bean)
			throws RemoteException {
		sendFileBeanToClients(client, bean);
	}

	/**
	 * Distribute file bean to all clients except for the sender.
	 * 
	 * @param sender
	 * @param bean
	 */
	private void sendFileBeanToClients(IHCLClient sender, FileBean bean) {
		for (int i = 0; i < clients.size(); i++) {
			IHCLClient client = clients.get(i);
			if (client.equals(sender))
				continue;
			try {
				FileBean clientBean = client.getFileBean(bean.subfolder,
						bean.file);
				if (clientBean == null)
					queue.pushJob(new TransferJob(sender, client, bean));
				else {
					boolean equals = Synchronizer.sameArray(clientBean.md5,
							bean.md5);
					boolean exists = clientBean.isDeleted() == bean.isDeleted();
					if (!equals || !exists)
						queue.pushJob(new TransferJob(sender, client, bean));
				}
			} catch (RemoteException e) {
				clients.remove(i);
				HCLLogger.performLog("Client does not respont on session: '"
						+ sessionID + "'", HCLType.WARNING, this);
			}
		}
	}
}
