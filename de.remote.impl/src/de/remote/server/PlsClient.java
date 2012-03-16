package de.remote.server;

import de.newsystem.rmi.protokol.RemoteException;
import de.remote.api.IPlayList;
import de.remote.api.IPlayer;
import de.remote.api.PlayerException;
import de.remote.impl.StationImpl;

public class PlsClient {

	public static void main(String[] args){
		try {
			StationImpl s = new StationImpl("/home/sebastian/");
			IPlayList pls = s.getPlayList();
			String plsName = "test";
//			pls.addPlayList(plsName);
			for (String p: pls.getPlayLists()){
				System.out.println("PLS: " + p);
				for (String f: pls.listContent(p))
					System.out.println(" -> " + f);
			}
			pls.removePlayList(plsName);
			IPlayer p = s.getMPlayer();
//			p.playPlayList(plsName);
//			p.quit();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (PlayerException e) {
			e.printStackTrace();
		}
		
	}
	
}
