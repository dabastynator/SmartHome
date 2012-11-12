package de.remote.mobile.util;

import de.newsystem.rmi.protokol.RemoteException;
import de.remote.api.PlayerException;
import de.remote.mobile.services.PlayerBinder;
import de.remote.mobile.util.VoiceRecognizer.IVoiceRecognition;
import android.content.Context;
import android.widget.Toast;

/**
 * The artificial intelligence records, recognizes and executes speech.
 * 
 * @author sebastian
 */
public class AI implements IVoiceRecognition {

	/**
	 * Android context for
	 */
	private Context context;

	/**
	 * The recognizer recognizes the microphone
	 */
	private VoiceRecognizer recognizer;

	/**
	 * The binder has all necessary objects
	 */
	private PlayerBinder binder;

	public AI(Context context) {
		this.context = context;
		recognizer = new VoiceRecognizer(context, this);
	}

	public void setPlayerBinder(PlayerBinder binder) {
		this.binder = binder;
	}

	public void record() {
		recognizer.startRecognizing();
	}

	@Override
	public void onRecognized(String result) {
		try {
			execute(result.split(" "));
		} catch (Exception e) {
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	private void execute(String[] split) throws AIException, RemoteException,
			PlayerException {
		if (split.length < 1)
			throw new AIException("can not execute empty string");
		String cmd = split[0];
		if (cmd.equals("pause"))
			binder.getPlayer().playPause();
		else if (cmd.equals("stop"))
			binder.getPlayer().quit();
		else if (cmd.equals("fullscreen"))
			binder.getPlayer().fullScreen();
		else if (cmd.equals("next"))
			binder.getPlayer().next();
		else if (cmd.equals("previous"))
			binder.getPlayer().previous();
		else if (cmd.startsWith("play")) {
			if (split.length < 2)
				throw new AIException("can not play empty string");
			String play = split[1];
			if (play.startsWith("playlist")) {
				if (split.length < 3)
					throw new AIException("can not play empty playlit");
				playPlayList(split[2]);
			} else
				binder.getPlayer().play(play);
		} else
			throw new AIException("what is '" + cmd + "'");
	}

	private void playPlayList(String pls) throws RemoteException,
			PlayerException {
		for (String pl : binder.getPlayList().getPlayLists()) {
			if (pl.contains(pls) || pls.contains(pl))
				binder.getPlayer().playPlayList(pl);
		}
	}

	@Override
	public void onEndOfSpeech() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onError() {
		Toast.makeText(context, "error occured", Toast.LENGTH_LONG).show();
	}

	@Override
	public void onBufferReceived(byte[] buffer) {
		// TODO Auto-generated method stub

	}

	/**
	 * AIException for errors on executing commands
	 * 
	 * @author sebastian
	 */
	public class AIException extends Exception {

		public AIException(String string) {
			super(string);
		}

	}

}
