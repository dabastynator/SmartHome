package de.hcl.synchronize.server;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.hcl.synchronize.api.IHCLClient;
import de.hcl.synchronize.api.IHCLClient.FileBean;
import de.hcl.synchronize.api.IHCLServer;
import de.hcl.synchronize.server.TransferQueue.TransferJob;
import de.hcl.synchronize.server.TransferQueue.TransferJob.TransferType;

public class Synchronizer {

	private IHCLServer server;
	private TransferQueue transferQueue;

	private enum Update {
		NONE, CLIENT1, CLIENT2
	};

	public Synchronizer(IHCLServer server) {
		this.server = server;
		transferQueue = new TransferQueue();
	}

	/**
	 * start synchronization in own thread.
	 */
	public void synchronize() {
		transferQueue.startTransfering();
		new Thread() {
			@Override
			public void run() {
				while (true) {
					synchronizeSynch();
					try {
						Thread.sleep(1000 * 5);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}

	private void synchronizeSynch() {
		try {
			int size = server.getClientSize();
			for (int i = 0; i < size - 1; i++) {
				for (int j = i + 1; j < size; j++) {
					IHCLClient client1 = server.getClient(i);
					IHCLClient client2 = server.getClient(j);
					if (client1 != null && client2 != null)
						synchronizeClients(client1, client2);
				}
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private void synchronizeClients(IHCLClient client1, IHCLClient client2) {
		try {
			Set<String> synchFolder = new HashSet<String>();
			synchronizeDirectories(client1, client2, "", synchFolder);

			while (!synchFolder.isEmpty()) {
				String folder = synchFolder.iterator().next();
				synchFolder.remove(folder);
				synchronizeDirectories(client1, client2, folder, synchFolder);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void synchronizeDirectories(IHCLClient client1, IHCLClient client2,
			String subfolder, Set<String> synchDirectories)
			throws RemoteException, IOException {

		synronizeFiles(client1, client2, subfolder);

		String[] files1 = client1.listDirectories(subfolder);
		String[] files2 = client2.listDirectories(subfolder);
		for (String file1 : files1) {
			Update update = Update.CLIENT2;
			for (String file2 : files2) {
				if (file1.equals(file2)) {
					update = Update.NONE;
					synchDirectories.add(subfolder + file1 + File.separator);
					break;
				}
			}
			if (update == Update.CLIENT2) {
				transferQueue.pushJob(new TransferJob(client1, client2,
						new FileBean(subfolder, file1, 0, null, 0, false),
						TransferType.DIRECTORY));
			}
			if (update == Update.CLIENT1) {
				transferQueue.pushJob(new TransferJob(client2, client1,
						new FileBean(subfolder, file1, 0, null, 0, false),
						TransferType.DIRECTORY));
			}
		}

		for (String file2 : files2) {
			Update update = Update.CLIENT1;
			for (String file1 : files1) {
				if (file1.equals(file2)) {
					update = Update.NONE;
					break;
				}
			}
			if (update == Update.CLIENT2) {
				transferQueue.pushJob(new TransferJob(client1, client2,
						new FileBean(subfolder, file2, 0, null, 0, false),
						TransferType.DIRECTORY));
			}
			if (update == Update.CLIENT1) {
				transferQueue.pushJob(new TransferJob(client2, client1,
						new FileBean(subfolder, file2, 0, null, 0, false),
						TransferType.DIRECTORY));
			}
		}
	}

	private void synronizeFiles(IHCLClient client1, IHCLClient client2,
			String subfolder) throws RemoteException, IOException {
		Map<String, FileBean> files1 = new HashMap<String, IHCLClient.FileBean>();
		FileBean[] filearray = client1.listFiles(subfolder);
		for (FileBean fb : filearray)
			files1.put(fb.file, fb);
		Map<String, FileBean> files2 = new HashMap<String, IHCLClient.FileBean>();
		filearray = client2.listFiles(subfolder);
		for (FileBean fb : filearray)
			files2.put(fb.file, fb);

		for (FileBean file1 : files1.values()) {
			Update update;
			FileBean file2 = files2.get(file1.file);
			if (file2 == null)
				update = (file1.isDeleted) ? Update.NONE : Update.CLIENT2;
			else if ((file1.md5.equals(file2.md5) && file1.isDeleted == file2.isDeleted)
					|| file1.isDeleted && file2.isDeleted)
				update = Update.NONE;
			else if (file1.lastDate > file2.lastDate)
				update = Update.CLIENT2;
			else
				update = Update.CLIENT1;

			if (update == Update.CLIENT2) {
				transferQueue.pushJob(new TransferJob(client1, client2, file1,
						TransferType.FILE));
			}
			if (update == Update.CLIENT1) {
				transferQueue.pushJob(new TransferJob(client2, client1, file2,
						TransferType.FILE));
			}
		}

		for (FileBean file2 : files2.values()) {
			Update update;
			FileBean file1 = files1.get(file2.file);
			if (file1 == null)
				update = (file2.isDeleted) ? Update.NONE : Update.CLIENT1;
			else if ((file1.md5.equals(file2.md5) && file1.isDeleted == file2.isDeleted)
					|| file1.isDeleted && file2.isDeleted)
				update = Update.NONE;
			else if (file1.lastDate > file2.lastDate)
				update = Update.CLIENT2;
			else
				update = Update.CLIENT1;

			if (update == Update.CLIENT2) {
				transferQueue.pushJob(new TransferJob(client1, client2, file1,
						TransferType.FILE));
			}
			if (update == Update.CLIENT1) {
				transferQueue.pushJob(new TransferJob(client2, client1, file2,
						TransferType.FILE));
			}
		}
	}

}
