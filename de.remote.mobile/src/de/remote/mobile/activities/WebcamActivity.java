package de.remote.mobile.activities;

import java.nio.IntBuffer;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.Toast;
import de.newsystem.rmi.api.Server;
import de.newsystem.rmi.protokol.RemoteException;
import de.remote.mobile.R;
import de.webcam.api.IWebcam;
import de.webcam.api.IWebcamListener;

public class WebcamActivity extends BindedActivity {

	private ImageView img_webcam;
	private Handler handler = new Handler();
	private IWebcamListener listener = new WebcamListener();


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webcam);
		findComponents();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (binder != null && binder.isConnected())
			registerListener();
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregister();
	}

	private void unregister() {
		Server s = binder.getServer();
		try {
			IWebcam webcamServer = (IWebcam) s.find(IWebcam.WEBCAM_SERVER,
					IWebcam.class);
			if (webcamServer == null)
				throw new RemoteException("",
						"webcam server not found in registry");
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
			int width = 320;
			int height = 240;
			double quality = 0.7f;
			int w = (int) (width * quality);
			int h = (int) (height * quality);
			webcamServer.addWebcamListener(listener, w - w % 2, h - h % 2,
					IWebcam.RGB_565);
			webcamServer.startCapture();
		} catch (Exception e) {
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
			if (bm == null)
				bm = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
			else if (bm.getWidth() != width || bm.getHeight() != height)
				bm = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
			IntBuffer buf = IntBuffer.wrap(rgb); // data is my array
			bm.copyPixelsFromBuffer(buf);

			handler.post(new Runnable() {
				@Override
				public void run() {
					img_webcam.setImageBitmap(bm);
				}
			});
		}

	}

	@Override
	void onBinderConnected() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onServerConnectionChanged(String serverName, int serverID) {
		if (binder.isConnected())
			registerListener();
	}

	@Override
	void onStartConnecting() {
		// TODO Auto-generated method stub
		
	}

}
