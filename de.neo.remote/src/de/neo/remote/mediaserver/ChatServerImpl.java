package de.neo.remote.mediaserver;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.neo.remote.api.IChatListener;
import de.neo.remote.api.IChatServer;
import de.neo.rmi.protokol.RemoteException;

public class ChatServerImpl implements IChatServer {

	private List<IChatListener> mListeners;

	/**
	 * format for date
	 */
	private SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");

	public ChatServerImpl() {
		mListeners = new ArrayList<IChatListener>();
	}

	@Override
	public void postMessage(String author, String msg) throws RemoteException {
		String time = formatter.format(new Date());
		for (IChatListener listener : mListeners)
			listener.informMessage(author, msg, time);
	}

	@Override
	public boolean addChatListener(IChatListener listener)
			throws RemoteException {
		if (mListeners.contains(listener))
			return false;
		boolean add = mListeners.add(listener);
		List<IChatListener> removeListener = new ArrayList<IChatListener>();
		String client = "";
		try {
			client = listener.getName();
		} catch (RemoteException e) {
			removeListener.add(listener);
		}
		for (IChatListener l : mListeners) {
			try {
				l.informNewClient(client);
			} catch (RemoteException e) {
				removeListener.add(l);
			}
		}
		mListeners.removeAll(removeListener);
		return add;
	}

	@Override
	public boolean removeChatListener(IChatListener listener)
			throws RemoteException {
		boolean remove = mListeners.remove(listener);
		List<IChatListener> removeListener = new ArrayList<IChatListener>();
		String client = "";
		try {
			client = listener.getName();
		} catch (RemoteException e) {
			removeListener.add(listener);
		}
		for (IChatListener l : mListeners) {
			try {
				l.informLeftClient(client);
			} catch (RemoteException e) {
				removeListener.add(l);
			}
		}
		mListeners.removeAll(removeListener);
		return remove;
	}

	@Override
	public String[] getAllClients() throws RemoteException {
		List<String> names = new ArrayList<String>();
		List<IChatListener> removeListener = new ArrayList<IChatListener>();
		for (IChatListener l : mListeners)
			try {
				names.add(l.getName());
			} catch (RemoteException e) {
				removeListener.add(l);
			}
		mListeners.removeAll(removeListener);
		return names.toArray(new String[names.size()]);
	}

}
