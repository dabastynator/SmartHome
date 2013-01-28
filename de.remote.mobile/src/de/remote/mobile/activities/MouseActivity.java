package de.remote.mobile.activities;

import de.newsystem.rmi.protokol.RemoteException;
import de.remote.api.IControl;
import de.remote.mobile.R;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

public class MouseActivity extends BindedActivity {

	private float startX;
	private float startY;

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
			IControl control = binder.getControl();
			if (control != null)
				try {
					control.mouseMove(x, y);
				} catch (RemoteException e) {
					Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT)
							.show();
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
	void remoteConnected() {
		// TODO Auto-generated method stub

	}

	@Override
	void disableScreen() {
		// TODO Auto-generated method stub

	}

}
