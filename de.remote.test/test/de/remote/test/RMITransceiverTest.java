package de.remote.test;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import de.newsystem.rmi.transceiver.DirectoryReceiver;
import de.newsystem.rmi.transceiver.DirectorySender;
import de.newsystem.rmi.transceiver.FileReceiver;
import de.newsystem.rmi.transceiver.FileSender;
import de.newsystem.rmi.transceiver.ReceiverProgress;

import junit.framework.TestCase;

public class RMITransceiverTest extends TestCase {

	/**
	 * port for file transceiver
	 */
	public static final int TRANSCEIVER_FILE_PORT = RMIChatTest.RMI_TEST_PORT_CLIENT + 3;

	/**
	 * port for directory transceiver
	 */
	public static final int TRANSCEIVER_DIRECTORY_PORT = RMIChatTest.RMI_TEST_PORT_CLIENT + 3;

	/**
	 * prefix for copied file
	 */
	public static final String TEST_FILE = "received_";

	/**
	 * suffix for copied directory
	 */
	public static final String TEST_COPY_DIRECTORY = "_copy";

	/**
	 * perform file transceiver test
	 */
	@Test
	public void testFileTransceiver() {
		File src = new File(BrowserTest.TEST_LOCATION + MPlayerTest.TEST_FILE);
		File dest = new File(BrowserTest.TEST_LOCATION + TEST_FILE
				+ MPlayerTest.TEST_FILE);
		try {
			FileSender sender = new FileSender(src, TRANSCEIVER_FILE_PORT, 1);
			FileReceiver receiver = new FileReceiver("localhost",
					TRANSCEIVER_FILE_PORT, 50000, dest);
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
		Assert.assertTrue("file size does not equal",
				src.length() == dest.length());
		if (dest.exists())
			dest.delete();
	}

	/**
	 * perform directory transceiver test
	 */
	@Test
	public void testDirectoryTransceiver() {
		File dest = new File(BrowserTest.TEST_LOCATION.substring(0,
				BrowserTest.TEST_LOCATION.length() - 1) + TEST_COPY_DIRECTORY);
		try {

			DirectorySender sender = new DirectorySender(new File(
					BrowserTest.TEST_LOCATION), TRANSCEIVER_DIRECTORY_PORT, 1);
			DirectoryReceiver receiver = new DirectoryReceiver("localhost",
					TRANSCEIVER_DIRECTORY_PORT, dest);
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
			Assert.assertTrue("destiny directory does not exist", false);
		Assert.assertTrue("destiny contains no file", dest.list().length > 0);
		delete(dest);
	}

	private void delete(File dest) {
		if (dest.exists()) {
			if (dest.isFile())
				dest.delete();
			else {
				for (File str : dest.listFiles())
					delete(str);
				dest.delete();
			}
		}
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

		@Override
		public void exceptionOccurred(Exception e) {
			System.out.println(e.getMessage());
		}

		@Override
		public void downloadCanceled() {
			
		}

	}
}
