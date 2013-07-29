package de.remote.mobile.activities;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import de.newsystem.rmi.protokol.RemoteException;
import de.newsystem.rmi.protokol.ServerPort;
import de.remote.gpiopower.api.IInternetSwitch.State;
import de.remote.mediaserver.api.IControl;
import de.remote.mobile.R;
import de.remote.mobile.services.RemoteService.StationStuff;

public class MouseActivity extends BindedActivity {

	private float startX;
	private float startY;
	private DataOutputStream mouseMoveOutput;
	private Socket socket;
	private String mediaServerName;
	private StationStuff mediaServer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.remote_mouse_key);
		
		if (getIntent().getExtras() != null
				&& getIntent().getExtras().containsKey(BrowserBase.EXTRA_MEDIA_NAME)) {
			mediaServerName = getIntent().getExtras().getString(BrowserBase.EXTRA_MEDIA_NAME);
		}
	}

	@Override
	void onBinderConnected() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			startX = event.getX();
			startY = event.getY();
		}
		if (event.getAction() == MotionEvent.ACTION_UP
				|| event.getAction() == MotionEvent.ACTION_MOVE) {
			int x = (int) (event.getX() - startX);
			int y = (int) (event.getY() - startY);
			startX = event.getX();
			startY = event.getY();
			if (mouseMoveOutput != null)
				try {
					mouseMoveOutput.writeInt(x * 2);
					mouseMoveOutput.writeInt(y * 2);
				} catch (IOException e) {
					Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT)
							.show();
					mouseMoveOutput = null;
				}
		}
		return true;
	}

	public void mouseLeft(View view) {
		IControl control = mediaServer.control;
		if (control != null)
			try {
				control.mousePress(IControl.LEFT_CLICK);
			} catch (RemoteException e) {
				Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
			}
	}

	public void mouseRight(View view) {
		IControl control = mediaServer.control;
		if (control != null)
			try {
				control.mousePress(IControl.RIGHT_CLICK);
			} catch (RemoteException e) {
				Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
			}
	}

	@Override
	protected void onDestroy() {
		try {
			socket.close();
		} catch (IOException e) {
		}
		super.onDestroy();
	}

	@Override
	public void onServerConnectionChanged(String serverName, int serverID) {
		try {
			mediaServer = binder.getMediaServerByName(mediaServerName);
			ServerPort sp = mediaServer.control.openMouseMoveStream();
			socket = new Socket(sp.getIp(), sp.getPort());
			mouseMoveOutput = new DataOutputStream(socket.getOutputStream());
		} catch (Exception e) {
			mouseMoveOutput = null;
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	void onStartConnecting() {
		mouseMoveOutput = null;
	}

	@Override
	public void onPowerSwitchChange(String _switch, State state) {
		// TODO Auto-generated method stub
		
	}

}
