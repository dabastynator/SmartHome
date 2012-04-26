package de.remote.test;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import de.newsystem.rmi.transeiver.FileReceiver;
import de.newsystem.rmi.transeiver.FileSender;
import de.newsystem.rmi.transeiver.ReceiverProgress;

import junit.framework.TestCase;

public class RMITranceiverTest extends TestCase {

	/**
	 * port for transceiver
	 */
	public static final int TRANSCEIVER_PORT = RMIChatTest.RMI_TEST_PORT_CLIENT + 3;

	public static final String TEST_FILE = "received_";

	/**
	 * perform transceiver test
	 */
	@Test
	public void testTransceiver() {
		File src = new File(BrowserTest.TEST_LOCATION + MPlayerTest.TEST_FILE);
		File dest = new File(BrowserTest.TEST_LOCATION + TEST_FILE
				+ MPlayerTest.TEST_FILE);
		try {
			FileSender sender = new FileSender(src, TRANSCEIVER_PORT, 1);
			FileReceiver receiver = new FileReceiver("localhost",
					TRANSCEIVER_PORT,10000, dest);
			receiver.getProgressListener().add(new MyListener());
			sender.sendAsync();
			Thread.sleep(500);
			receiver.receiveAsync();
			Thread.sleep(500);
		} catch (IOException e) {
			Assert.assertTrue(e.getMessage(), false);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (!dest.exists())
			Assert.assertTrue("destiny file does not exist", false);
		Assert.assertTrue("file size does not equal", src.length() == dest.length());
		if (dest.exists())
			dest.delete();
	}

	public class MyListener implements ReceiverProgress {

		@Override
		public void startReceive(long size) {
			System.out.println("start size: " + size);
		}

		@Override
		public void progressReceive(long size) {
			System.out.println("progress size: " + size);

		}

		@Override
		public void endReceive(long size) {
			System.out.println("end size: " + size);

		}

	}
}
