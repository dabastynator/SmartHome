package de.newsystem.dwistle.services;

import java.io.IOException;
import java.net.SocketException;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;
import de.newsystem.dwistle.BrowserActivity;
import de.newsystem.dwistle.R;
import de.newsystem.dwistle.TempBrowser;
import de.newsystem.rmi.api.Server;
import de.newsystem.rmi.protokol.RemoteException;
import de.remote.api.ControlConstants;
import de.remote.api.IBrowser;
import de.remote.api.IChatServer;
import de.remote.api.IControl;
import de.remote.api.IPlayList;
import de.remote.api.IPlayer;
import de.remote.api.IPlayerListener;
import de.remote.api.IStation;
import de.remote.api.PlayingBean;
import de.remote.api.PlayingBean.STATE;

public class IdefixService extends Service {

	public static final int NOTIFICATION_ID = 1;

	private String server;

	private PlayerBinder binder;
	private Server idefix;
	private IBrowser browser;
	private IPlayer player;
	private IControl control;
	private PlayerListener playerListener;
	private IPlayList playList;
	private IChatServer chatServer;

	private Handler handler = new Handler();
	private IStation station;

	@Override
	public void onCreate() {
		super.onCreate();
		binder = new PlayerBinder();
		playerListener = new PlayerListener();
	}

	private void connect(final Runnable r) {
		new Thread() {

			@Override
			public void run() {
				idefix = Server.getServer();
				try {
					try {
						idefix.connectToRegistry(server);
					} catch (SocketException e) {
						idefix.connectToRegistry(server);
					}
					idefix.startServer();

					station = (IStation) idefix.find(
							ControlConstants.STATION_ID, IStation.class);

					if (station == null)
						throw new RemoteException(ControlConstants.STATION_ID,
								"station not found in registry");

					browser = new TempBrowser(station.createBrowser());
					player = station.getMPlayer();
					player.addPlayerMessageListener(playerListener);
					control = station.getControl();
					playList = station.getPlayList();
					chatServer = station.getChatServer();
					if (r != null)
						handler.post(r);
				} catch (final Exception e) {
					handler.post(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(IdefixService.this, e.getMessage(),
									Toast.LENGTH_SHORT).show();
						}
					});
				}
			}
		}.start();
	}

	private void makeNotification(String title, String body) {
		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		int icon = R.drawable.browser;
		Notification notification = new Notification(icon, "Player started",
				System.currentTimeMillis());
		Intent nIntent = new Intent(this, BrowserActivity.class);
		PendingIntent pInent = PendingIntent.getActivity(this, 0, nIntent, 0);
		notification.setLatestEventInfo(getApplicationContext(), title, body,
				pInent);
		nm.notify(NOTIFICATION_ID, notification);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		disconnect();
	}

	private void disconnect() {
		if (player != null) {
			try {
				player.removePlayerMessageListener(playerListener);
			} catch (Exception e) {
			}
		}
		if (idefix != null)
			try {
				idefix.close();
			} catch (IOException e) {
			}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	/**
	 * listener for player activity
	 * 
	 * @author sebastian
	 */
	public class PlayerListener implements IPlayerListener {

		@Override
		public void playerMessage(final PlayingBean playing) {
			StringBuilder sb = new StringBuilder();
			String t = "Playing";
			if (playing.getTitle() != null && playing.getTitle().length() > 0)
				t = playing.getTitle();
			if (playing.getArtist() != null && playing.getArtist().length() > 0)
				sb.append("Artist: " + playing.getArtist() + "\n");
			if (playing.getAlbum() != null && playing.getAlbum().length() > 0)
				sb.append("Album: " + playing.getAlbum() + "\n");
			if (playing.getState() == STATE.DOWN)
				t = "player is down";
			final String msg = sb.toString();
			final String title = t;
			handler.post(new Runnable() {
				@Override
				public void run() {
					if (playing.getState() == STATE.DOWN) {
						NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
						nm.cancel(IdefixService.NOTIFICATION_ID);
					} else
						makeNotification(title, msg);
				}
			});
		}
	}

	/**
	 * binder for api for this service
	 * 
	 * @author sebastian
	 */
	public class PlayerBinder extends Binder {

		public IBrowser getBrowser() {
			return browser;
		}

		public IPlayer getPlayer() {
			return player;
		}

		public IControl getControl() {
			return control;
		}

		public void connectToServer(String ip, Runnable r) {
			if (ip.equals(server)) {
				if (r != null)
					r.run();
			} else {
				disconnect();
				server = ip;
				connect(r);
			}
		}

		public void useMPlayer() throws RemoteException {
			player = station.getMPlayer();
		}

		public void useTotemPlayer() throws RemoteException {
			player = station.getTotemPlayer();
		}

		public IPlayList getPlayList() {
			return playList;
		}

		public IChatServer getChatServer() {
			return chatServer;
		}
	}

}
