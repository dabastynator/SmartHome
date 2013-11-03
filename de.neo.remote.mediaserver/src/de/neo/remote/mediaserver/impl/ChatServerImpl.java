package de.neo.remote.mediaserver.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.neo.remote.mediaserver.api.IChatListener;
import de.neo.remote.mediaserver.api.IChatServer;
import de.neo.rmi.protokol.RemoteException;

public class ChatServerImpl implements IChatServer {

	private List<IChatListener> listeners;

	/**
	 * format for date
	 */
	private SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");

	public ChatServerImpl() {
		listeners = new ArrayList<IChatListener>();
	}

	@Override
	public void postMessage(String author, String msg) throws RemoteException {
		String time = formatter.format(new Date());
		for (IChatListener listener : listeners)
			listener.informMessage(author, msg, time);
	}

	@Override
	public boolean addChatListener(IChatListener listener)
			throws RemoteException {
		if (listeners.contains(listener))
			return false;
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
