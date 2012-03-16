package de.remote.server;

import java.io.IOException;
import java.net.UnknownHostException;

import de.newsystem.rmi.api.Server;
import de.newsystem.rmi.protokol.RemoteException;
import de.remote.api.ControlConstants;
import de.remote.api.IPlayer;
import de.remote.api.IPlayerListener;
import de.remote.api.IStation;
import de.remote.api.PlayerException;
import de.remote.api.PlayingBean;

public class Client {

	public static void main(String[] args){
		
		try {
			try {
				Server server = Server.getServer();
				server.connectToRegistry("192.168.1.3");
				server.startServer(5001);
				
				IStation station = (IStation) server.find(ControlConstants.STATION_ID, IStation.class);
				
				demo(station.getMPlayer());
			} catch (PlayerException e) {
				System.out.println("playerexception: " + e.getMessage());
			} catch (RemoteException e) {
				System.out.println("id: " + e.getId() + ", message: " + e.getMessage());
				e.printStackTrace();
			}
			Server.getServer().close();
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void demo(IPlayer player) throws RemoteException, PlayerException {
		PlayerMessage pm = new PlayerMessage();
		
		player.addPlayerMessageListener(pm);
		
		player.play("/media/baestynator/Musik/Alben/Electro/Dance/matrix");
		
		player.next();
		
		player.playPause();
		player.playPause();
		
		player.removePlayerMessageListener(pm);
		
		player.quit();
	}

	public static class PlayerMessage implements IPlayerListener{

		@Override
		public void playerMessage(PlayingBean bean) {
			System.out.println("------------------------------------------");
			System.out.println("Arist: " + bean.getArtist());
			System.out.println("Album: " + bean.getAlbum());
			System.out.println("Title: " + bean.getTitle());
			System.out.println("Radio: " + bean.getRadio());
			System.out.println("File:  " + bean.getFile());
		}
		
	}
	
}
