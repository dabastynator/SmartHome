package de.remote.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.newsystem.rmi.protokol.RemoteException;
import de.remote.api.IChatListener;
import de.remote.api.IChatServer;

public class ChatServerImpl implements IChatServer {

	private List<IChatListener> listeners;

	public ChatServerImpl() {
		listeners = new ArrayList<IChatListener>();
	}

	@Override
	public void postMessage(String author, String msg) throws RemoteException {
		Date time = new Date();
		for (IChatListener listener : listeners)
			listener.informMessage(author, msg, time);
	}

	@Override
	public boolean addChatListener(IChatListener listener)
			throws RemoteException {
		boolean add = listeners.add(listener);
		List<IChatListener> removeListener = new ArrayList<IChatListener>();
		String client = "";
		try {
			client = listener.getName();
		} catch (RemoteException e) {
			removeListener.add(listener);
		}
		for (IChatListener l : listeners) {
			try {
				l.informNewClient(client);
			} catch (RemoteException e) {
				removeListener.add(l);
			}
		}
		listeners.removeAll(removeListener);
		return add;
	}

	@Override
	public boolean removeChatListener(IChatListener listener)
			throws RemoteException {
		boolean remove = listeners.remove(listener);
		List<IChatListener> removeListener = new ArrayList<IChatListener>();
		String client = "";
		try {
			client = listener.getName();
		} catch (RemoteException e) {
			removeListener.add(listener);
		}
		for (IChatListener l : listeners) {
			try {
				l.informLeftClient(client);
			} catch (RemoteException e) {
				removeListener.add(l);
			}
		}
		listeners.removeAll(removeListener);
		return remove;
	}

	@Override
	public String[] getAllClients() throws RemoteException {
		List<String> names = new ArrayList<String>();
		List<IChatListener> removeListener = new ArrayList<IChatListener>();
		for (IChatListener l : listeners)
			try {
				names.add(l.getName());
			} catch (RemoteException e) {
				removeListener.add(l);
			}
		listeners.removeAll(removeListener);
		return names.toArray(new String[names.size()]);
	}

}
