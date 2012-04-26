package de.remote.test;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.newsystem.rmi.protokol.RemoteException;
import de.remote.api.IPlayerListener;
import de.remote.api.PlayerException;
import de.remote.api.PlayingBean;
import de.remote.api.PlayingBean.STATE;
import de.remote.impl.MPlayer;

/**
 * test for mplayer
 * 
 * @author sebastian
 */
public class MPlayerTest extends TestCase {

	/**
	 * file to test
	 */
	public static final String TEST_FILE = "test.mp3";

	/**
	 * state of playing file
	 */
	public STATE state;

	/**
	 * correct file is playing
	 */
	public boolean file = false;

	private MPlayer player;

	@Before
	public void createPlayer() {
		player = new MPlayer();
	}

	/**
	 * perform mplayer test
	 */
	@Test
	public void testPLayer() {
		try {
			player = new MPlayer();
			player.addPlayerMessageListener(new PlayerListener());
			player.play(BrowserTest.TEST_LOCATION + TEST_FILE);
			Thread.sleep(5000);
			Assert.assertTrue("false file is played", file);
			Assert.assertTrue("false state of the player", state == STATE.PLAY);
			player.playPause();
			Thread.sleep(1000);
			Assert.assertTrue("false state of the player", state == STATE.PAUSE);
			player.playPause();
			Thread.sleep(1000);
			Assert.assertTrue("false state of the player", state == STATE.PLAY);
			player.quit();
			Thread.sleep(1000);
			Assert.assertTrue("false state of the player", state == STATE.DOWN);
		} catch (RemoteException e) {
			Assert.assertTrue(e.getMessage(), false);
		} catch (InterruptedException e) {
			Assert.assertTrue(e.getMessage(), false);
		} catch (PlayerException e) {
			Assert.assertTrue(e.getMessage(), false);
		}
	}

	@After
	public void puitPlayer() {
		try {
			player.quit();
		} catch (PlayerException e) {
			e.printStackTrace();
		}
	}

	/**
	 * listener for the player messages
	 * 
	 * @author sebastian
	 */
	public class PlayerListener implements IPlayerListener {

		@Override
		public void playerMessage(PlayingBean playing) throws RemoteException {
			if (playing.getFile().equals(TEST_FILE))
				file = true;
			state = playing.getState();
		}

	}
}
