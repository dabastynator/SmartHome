package de.hcl.synchronize.server;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.hcl.synchronize.api.IHCLClient;
import de.hcl.synchronize.api.IHCLClient.FileBean;
import de.hcl.synchronize.api.IHCLServer;
import de.hcl.synchronize.api.IHCLSession;
import de.hcl.synchronize.jobs.TransferJob;
import de.hcl.synchronize.log.HCLLogger;
import de.hcl.synchronize.log.IHCLLogListener.HCLType;
import de.newsystem.rmi.protokol.RemoteException;

public class Synchronizer {

	private IHCLServer server;
	private JobQueue jobQueue;

	private enum Update {
		NONE, CLIENT1, CLIENT2
	};

	public Synchronizer(IHCLServer server, JobQueue queue) {
		this.server = server;
		jobQueue = queue;
	}

	/**
	 * start synchronization in own thread.
	 */
	public void synchronize() {
		new Thread() {
			@Override
			public void run() {
				HCLLogger.performLog("Synchronization started",
						HCLType.INFORMATION, Synchronizer.this);
				while (true) {
					synchronizeSynch();
					try {
						Thread.sleep(1000 * 2);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}

	private void synchronizeSynch() {
		try {
			String[] sessionIDs = server.getSessionIDs();
			for (String session : sessionIDs)
				synchronizeSession(server.getSession(session));
		} catch (RemoteException e) {
		}
	}

	private void synchronizeSession(IHCLSession session) throws RemoteException {
		int size = session.getClientSize();
		String sessionID = session.getSessionID();
		for (int i = 0; i < size - 1; i++) {
			for (int j = i + 1; j < size; j++) {
				IHCLClient client1 = session.getClient(i);
				IHCLClient client2 = session.getClient(j);
				try {
					client1.getName();
				} catch (RemoteException e) {
					HCLLogger.performLog(
							"Client does not respond at session: '" + sessionID
									+ "'", HCLType.WARNING, this);
					session.removeClient(client1);
					client1 = null;
				}
				try {
					client2.getName();
				} catch (RemoteException e) {
					HCLLogger.performLog(
							"Client does not respond at session: '" + sessionID
									+ "'", HCLType.WARNING, this);
					session.removeClient(client2);
					client2 = null;
				}
				if (client1 != null && client2 != null)
					synchronizeClients(client1, client2);
				else
					return;
			}
		}
	}

	private void synchronizeClients(IHCLClient client1, IHCLClient client2) {
		try {
			Set<String> synchFolder = new HashSet<String>();
			if (!client1.getHash("").equals(client2.getHash("")))
				synronizeFiles(client1, client2, "", synchFolder);
			else {
				for (String subfolder : client1.listDirectories(""))
					synchFolder.add(subfolder);
			}
			while (!synchFolder.isEmpty()) {
				String folder = synchFolder.iterator().next();
				synchFolder.remove(folder);

				if (!client1.getHash(folder).equals(client2.getHash(folder)))
					synronizeFiles(client1, client2, folder, synchFolder);
				else {
					for (String subfolder : client1.listDirectories(folder))
						synchFolder.add(folder + subfolder);
				}
			}
		} catch (Exception e) {
			HCLLogger.performLog(
					e.getClass().getSimpleName() + " " + e.getMessage(),
					HCLType.ERROR, this);
			e.printStackTrace();
		}
	}

	/**
	 * synchronize two clients at given subfolder.
	 * 
	 * @param client1
	 * @param client2
	 * @param subfolder
	 * @param synchFolder
	 * @throws RemoteException
	 * @throws IOException
	 */
	private void synronizeFiles(IHCLClient client1, IHCLClient client2,
			String subfolder, Set<String> synchFolder) throws RemoteException,
			IOException {
		// list files
		Map<String, FileBean> files1 = new HashMap<String, IHCLClient.FileBean>();
		FileBean[] filearray = client1.listFiles(subfolder);
		for (FileBean fb : filearray)
			files1.put(fb.file, fb);
		Map<String, FileBean> files2 = new HashMap<String, IHCLClient.FileBean>();
		filearray = client2.listFiles(subfolder);
		for (FileBean fb : filearray)
			files2.put(fb.file, fb);

		// synchronize each other
		synchronizeFromTo(client1, client2, files1, files2);
		synchronizeFromTo(client2, client1, files2, files1);

		// get synchronized subsubfolder
		for (FileBean file1 : files1.values()) {
			if (file1.isDirectory()) {
				FileBean fileBean = files2.get(file1.file);
				if (fileBean != null && !file1.isDeleted()
						&& !fileBean.isDeleted())
					synchFolder.add(subfolder + file1.file + File.separator);
			}
		}
	}

	private void synchronizeFromTo(IHCLClient client1, IHCLClient client2,
			Map<String, FileBean> files1, Map<String, FileBean> files2) {
		for (FileBean file1 : files1.values()) {
			if (file1.isReceiving())
				continue;
			Update update;
			FileBean file2 = files2.get(file1.file);
			if (file2 == null)
				update = (file1.isDeleted()) ? Update.NONE : Update.CLIENT2;
			else if ((sameArray(file1.md5, file2.md5) && file1.isDeleted() == file2
					.isDeleted()) || file1.isDeleted() && file2.isDeleted())
				update = Update.NONE;
			else if (file1.lastDate > file2.lastDate)
				update = Update.CLIENT2;
			else
				update = Update.CLIENT1;
			if (file2 != null && file2.isReceiving())
				update = Update.NONE;

			if (update == Update.CLIENT2) {
				jobQueue.pushJob(new TransferJob(client1, client2, file1));
			}
			if (update == Update.CLIENT1) {
				jobQueue.pushJob(new TransferJob(client2, client1, file2));
			}
		}
	}

	/**
	 * Check arrays if they have the same content.
	 * 
	 * @param a1
	 * @param a2
	 * @return true if arrays have same content
	 */
	public static boolean sameArray(byte[] a1, byte[] a2) {
		if (a1 == null && a2 == null)
			return true;
		if (a1 == null || a2 == null)
			return false;
		if (a1.length != a2.length)
			return false;
		for (int i = 0; i < a1.length; i++)
			if (a1[i] != a2[i])
				return false;
		return true;
	}
}
