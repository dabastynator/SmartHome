package de.remote.mobile.activies;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.widget.ImageView;
import android.widget.Toast;
import de.newsystem.rmi.api.Server;
import de.newsystem.rmi.protokol.RemoteException;
import de.remote.mobile.R;
import de.remote.mobile.services.PlayerBinder;
import de.remote.mobile.services.RemoteService;
import de.webcam.api.IWebcam;
import de.webcam.api.IWebcamListener;

public class WebcamActivity extends Activity {

	private ImageView img_webcam;
	private Handler handler = new Handler();
	private IWebcamListener listener = new WebcamListener();

	/**
	 * binder for connection with service
	 */
	private PlayerBinder binder;

	/**
	 * connection with service
	 */
	private ServiceConnection playerConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			binder = (PlayerBinder) service;
			registerListener();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webcam);
		findComponents();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Intent intent = new Intent(this, RemoteService.class);
		startService(intent);
		bindService(intent, playerConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregister();
		unbindService(playerConnection);
	}

	private void unregister() {
		Server s = binder.getServer();
		try {
			IWebcam webcamServer = (IWebcam) s.find(IWebcam.WEBCAM_SERVER,
					IWebcam.class);
			webcamServer.removeWebcamListener(listener);
		} catch (RemoteException e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}

	private void registerListener() {
		Server s = binder.getServer();
		try {
			IWebcam webcamServer = (IWebcam) s.find(IWebcam.WEBCAM_SERVER,
					IWebcam.class);
			webcamServer.addWebcamListener(listener, 80, 60);
		} catch (RemoteException e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}

	private void findComponents() {
		img_webcam = (ImageView) findViewById(R.id.img_webcam);
	}

	public class WebcamListener implements IWebcamListener {

		private Bitmap bm = null;

		@Override
		public void onVideoFrame(int width, int height, int[] rgb)
				throws RemoteException {
			System.out.println("get frame");
			bm = Bitmap.createBitmap(rgb, width, height,
					Bitmap.Config.ARGB_8888);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			handler.post(new Runnable() {
				@Override
				public void run() {
					img_webcam.setImageBitmap(bm);
				}
			});

		}

	}

}
