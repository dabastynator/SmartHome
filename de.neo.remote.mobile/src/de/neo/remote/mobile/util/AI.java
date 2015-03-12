package de.neo.remote.mobile.util;

import android.content.Context;
import android.widget.Toast;
import de.neo.remote.api.IInternetSwitch;
import de.neo.remote.api.IInternetSwitch.State;
import de.neo.remote.api.PlayerException;
import de.neo.remote.mobile.services.PlayerBinder;
import de.neo.remote.mobile.util.VoiceRecognizer.IVoiceRecognition;
import de.neo.rmi.protokol.RemoteException;

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
			Toast.makeText(context,
					e.getClass().getSimpleName() + ": " + e.getMessage(),
					Toast.LENGTH_LONG).show();
		}
	}

	private void execute(String[] split) throws AIException, RemoteException,
			PlayerException {
		if (split.length < 1)
			throw new AIException("can not execute empty string");
		for (int i = 0; i < split.length; i++)
			split[i] = split[i].toLowerCase();
		String cmd = split[0];
		if (cmd.equals("pause")) {
			binder.getLatestMediaServer().player.playPause();
			Toast.makeText(context, "success pause", Toast.LENGTH_SHORT).show();
		} else if (cmd.equals("stop")) {
			binder.getLatestMediaServer().player.quit();
			Toast.makeText(context, "success stop", Toast.LENGTH_SHORT).show();
		} else if (cmd.equals("fullscreen")) {
			binder.getLatestMediaServer().player.fullScreen(true);
			Toast.makeText(context, "success fullscreen", Toast.LENGTH_SHORT)
					.show();
		} else if (cmd.equals("next")) {
			binder.getLatestMediaServer().player.next();
			Toast.makeText(context, "success next", Toast.LENGTH_SHORT).show();
		} else if (cmd.equals("previous")) {
			binder.getLatestMediaServer().player.previous();
			Toast.makeText(context, "success previous", Toast.LENGTH_SHORT)
					.show();
		} else if (cmd.startsWith("play")) {
			if (split.length < 2)
				throw new AIException("can not play empty string");
			String play = split[1];
			if (play.startsWith("playlist")) {
				if (split.length < 3)
					throw new AIException("can not play empty playlit");
				playPlayList(split[2]);
			} else
				binder.getLatestMediaServer().player.play(play);
			Toast.makeText(context, "success play", Toast.LENGTH_SHORT).show();
		} else if (cmd.startsWith("licht")) {
			if (split.length != 3)
				throw new AIException("Usage: licht 'name' [an|aus]");
			State state = State.OFF;
			if (split[2].equalsIgnoreCase("an"))
				state = State.ON;
			IInternetSwitch power = (IInternetSwitch) binder.getSwitches().get(
					split[1]).mObject;
			if (power == null)
				throw new AIException("Unknown switch: " + split[1]);
			power.setState(state);
			Toast.makeText(context, "success light", Toast.LENGTH_SHORT).show();
		} else if (cmd.startsWith("bildschirm")) {
			if (split.length < 2)
				throw new AIException("what to do with bildschirm?");
			if (split[1].equals("an"))
				binder.getLatestMediaServer().control.displayBride();
			else if (split[1].equals("aus"))
				binder.getLatestMediaServer().control.displayDark();
			else
				throw new AIException("can't make bildschirm " + split[1]);
			Toast.makeText(context, "success bildschirm", Toast.LENGTH_SHORT)
					.show();
		} else if (cmd.startsWith("tastatur")) {
			String keypress = "";
			for (int i = 1; i < split.length; i++)
				keypress = keypress + split[i] + " ";
			binder.getLatestMediaServer().control.keyPress(keypress);
			Toast.makeText(context, "success tastatur", Toast.LENGTH_SHORT)
					.show();
		} else
			throw new AIException("what is '" + cmd + "'");
	}

	private void playPlayList(String pls) throws RemoteException,
			PlayerException {
		for (String pl : binder.getLatestMediaServer().pls.getPlayLists()) {
			if (pl.contains(pls) || pls.contains(pl))
				binder.getLatestMediaServer().player.playPlayList(pl);
		}
	}

	@Override
	public void onEndOfSpeech() {
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

		/**
		 * generated
		 */
		private static final long serialVersionUID = -4124586628955545951L;

		public AIException(String string) {
			super(string);
		}

	}

}
