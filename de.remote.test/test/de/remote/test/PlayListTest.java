package de.remote.test;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;

import de.newsystem.rmi.protokol.RemoteException;
import de.remote.api.IPlayList;
import de.remote.api.PlayerException;
import de.remote.impl.StationImpl;

public class PlayListTest extends TestCase {

	/**
	 * perform playlist test
	 */
	@Test
	public void testPlayList() {
		try {
			IPlayList pls = new StationImpl(null, BrowserTest.TEST_LOCATION)
					.getPlayList();
			String plsName = "test_pls";
			testAddRemote(pls, plsName);
			pls.addPlayList(plsName);
			testExtend(pls, plsName);
			pls.removePlayList(plsName);
		} catch (RemoteException e) {
			Assert.assertTrue(e.getMessage(), false);
		}
	}

	private void testExtend(IPlayList pls, String plsName) {
		try {
			String plsItem = pls.getPlaylistFullpath(plsName);
			pls.extendPlayList(plsName, plsItem);
			boolean found = false;
			for (String str : pls.listContent(plsName))
				if (str.equals(plsItem)) {
					found = true;
					break;
				}
			Assert.assertTrue("playlist item could not added", found);
			pls.removeItem(plsName, plsItem);
			found = false;
			for (String str : pls.listContent(plsName))
				if (str.equals(plsItem)) {
					found = true;
					break;
				}
			Assert.assertFalse("playlist item could not removed", found);
		} catch (RemoteException e) {
			Assert.assertTrue(e.getMessage(), false);
		} catch (PlayerException e) {
			Assert.assertTrue(e.getMessage(), false);
		}		
	}

	private void testAddRemote(IPlayList pls, String name) {
		try {
			pls.addPlayList(name);
			boolean found = false;
			for (String str : pls.getPlayLists())
				if (str.equals(name)) {
					found = true;
					break;
				}
			Assert.assertTrue("playlist could not created", found);
			pls.removePlayList(name);
			found = false;
			for (String str : pls.getPlayLists())
				if (str.equals(name)) {
					found = true;
					break;
				}
			Assert.assertFalse("playlist could not deleted", found);
		} catch (RemoteException e) {
			Assert.assertTrue(e.getMessage(), false);
		}
	}

}
