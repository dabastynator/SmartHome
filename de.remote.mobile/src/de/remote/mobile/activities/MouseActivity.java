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
import de.remote.api.IControl;
import de.remote.mobile.R;

public class MouseActivity extends BindedActivity {

	private float startX;
	private float startY;
	private DataOutputStream mouseMoveOutput;
	private Socket socket;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.remote_mouse_key);
	}

	@Override
	void binderConnected() {
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
		IControl control = binder.getControl();
		if (control != null)
			try {
				control.mousePress(IControl.LEFT_CLICK);
			} catch (RemoteException e) {
				Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
			}
	}

	public void mouseRight(View view) {
		IControl control = binder.getControl();
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
			ServerPort sp = binder.getControl().openMouseMoveStream();
			socket = new Socket(sp.getIp(), sp.getPort());
			mouseMoveOutput = new DataOutputStream(socket.getOutputStream());
		} catch (Exception e) {
			mouseMoveOutput = null;
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	void startConnecting() {
		mouseMoveOutput = null;
	}

}
