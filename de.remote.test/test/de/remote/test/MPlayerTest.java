package de.remote.test;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.neo.remote.mediaserver.api.IPlayerListener;
import de.neo.remote.mediaserver.api.PlayerException;
import de.neo.remote.mediaserver.api.PlayingBean;
import de.neo.remote.mediaserver.api.PlayingBean.STATE;
import de.neo.remote.mediaserver.impl.MPlayer;
import de.neo.rmi.protokol.RemoteException;

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
		player = new MPlayer(BrowserTest.TEST_LOCATION);
	}

	/**
	 * perform mplayer test
	 */
	@Test
	public void testPLayer() {
		try {
			player = new MPlayer(BrowserTest.TEST_LOCATION);
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
