package de.remote.test;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import de.newsystem.rmi.protokol.RemoteException;
import de.remote.mediaserver.api.IBrowser;
import de.remote.mediaserver.impl.BrowserImpl;
import junit.framework.TestCase;

public class BrowserTest extends TestCase {

	/**
	 * test location for perform tests
	 */
	public static final String TEST_LOCATION = "/home/sebastian/temp/test/";
	
	@Test
	public void testBrowser() {
		String testDirectory = "test";
		new File(TEST_LOCATION + testDirectory).mkdir();
		IBrowser browser = new BrowserImpl(TEST_LOCATION);
		try {
			String fullLocation = browser.getFullLocation();
			boolean found = false;
			for (String str: browser.getDirectories())
				if (str.equals(testDirectory))
					found = true;
			browser.goTo(testDirectory);
			boolean rightDir = testDirectory.equals(browser.getLocation());
			browser.goBack();
			boolean sameDir = fullLocation.equals(browser.getFullLocation());
			new File(TEST_LOCATION + testDirectory).delete();
			Assert.assertTrue("directory is not shown", found);
			Assert.assertTrue("go back error", sameDir);
			Assert.assertTrue("go to error", rightDir);
		} catch (RemoteException e) {
			Assert.assertTrue(e.getMessage(), false);
		}
	}
}
