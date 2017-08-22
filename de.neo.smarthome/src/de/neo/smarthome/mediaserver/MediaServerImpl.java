package de.neo.smarthome.mediaserver;

import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import de.neo.remote.rmi.RMILogger.LogPriority;
import de.neo.remote.rmi.RemoteException;
import de.neo.remote.transceiver.DirectorySender;
import de.neo.remote.transceiver.FileSender;
import de.neo.remote.transceiver.SenderProgress;
import de.neo.smarthome.RemoteLogger;
import de.neo.smarthome.api.IWebMediaServer.BeanDownload;
import de.neo.smarthome.api.IWebMediaServer.BeanDownload.DownloadType;

public class MediaServerImpl {

	public static final String ROOT = "MediaServer";

	public static final int DOWNLOAD_PORT = 5033;

	private TotemPlayer mTotem;
	private MPlayer mMplayer;
	private PlayList mPlaylist;
	private String mBrowserLocation;
	private OMXPlayer mOmxplayer;

	public IPlayer getTotemPlayer() throws RemoteException {
		return mTotem;
	}

	public IPlayer getMPlayer() throws RemoteException {
		return mMplayer;
	}

	public PlayList getPlayList() throws RemoteException {
		return mPlaylist;
	}

	public IPlayer getOMXPlayer() throws RemoteException {
		return mOmxplayer;
	}

	public void initialize(Element element) throws SAXException, IOException {
		for (String attribute : new String[] { "location", "playlistLocation" })
			if (!element.hasAttribute(attribute))
				throw new SAXException(attribute + " missing for mediaserver");
		String browseLocation = element.getAttribute("location");
		String playlistLocation = element.getAttribute("playlistLocation");

		if (!new File(browseLocation).exists())
			throw new IOException("Browser location does not exist: " + browseLocation);
		if (!new File(playlistLocation).exists())
			throw new IOException("Playlist location does not exist: " + playlistLocation);
		mTotem = new TotemPlayer();
		mMplayer = new MPlayer(playlistLocation);
		mBrowserLocation = browseLocation;
		if (!mBrowserLocation.endsWith(File.separator))
			mBrowserLocation += File.separator;
		mPlaylist = new PlayList(playlistLocation);
		mOmxplayer = new OMXPlayer();
	}

	public String[] listDirectories(String path) throws RemoteException {
		if (path.startsWith("..") || path.contains(File.separator + ".."))
			throw new IllegalArgumentException("Path must not contain '..'");
		String location = mBrowserLocation + path;
		if (!location.endsWith(File.separator))
			location += File.separator;
		List<String> list = new ArrayList<String>();
		for (String str : new File(location).list())
			if (new File(location + str).isDirectory())
				if (str.length() > 0 && str.charAt(0) != '.')
					list.add(str);
		return list.toArray(new String[] {});
	}

	public String[] listFiles(String path) throws RemoteException {
		if (path.startsWith("..") || path.contains(File.separator + ".."))
			throw new IllegalArgumentException("Path must not contain '..'");
		String location = mBrowserLocation + path;
		if (!location.endsWith(File.separator))
			location += File.separator;
		List<String> list = new ArrayList<String>();
		for (String str : new File(location).list())
			if (new File(location + str).isFile())
				if (str.length() > 0 && str.charAt(0) != '.')
					list.add(str);
		return list.toArray(new String[] {});
	}

	public String getBrowserPath() {
		return mBrowserLocation;
	}

	public BeanDownload publishForDownload(String file) throws RemoteException, IOException {
		// Get next free port
		int port = DOWNLOAD_PORT;
		while (!portIsAvailable(port) && port < DOWNLOAD_PORT + 10) {
			port++;
		}
		if (!portIsAvailable(port))
			throw new IOException("There is no open port available. Too many downloads.");
		// Check file type to create right publisher
		File download = new File(mBrowserLocation + file);
		BeanDownload result = new BeanDownload();
		result.setIP(getLocalHostLANAddress().getHostAddress());
		result.setPort(port);
		if (download.isFile()) {
			result.setType(DownloadType.File);
			FileSender sender = new FileSender(download, port, 1, 1024 * 512);
			sender.getProgressListener().add(new FileSendListener(file));
			sender.sendAsync();
		} else if (download.isDirectory()) {
			result.setType(DownloadType.Directory);
			DirectorySender sender = new DirectorySender(download, port, 1);
			sender.getProgressListener().add(new FileSendListener(file));
			sender.sendAsync();
		} else {
			throw new IOException("File does not exist: " + file);
		}
		return result;
	}

	private static InetAddress getLocalHostLANAddress() throws UnknownHostException {
		try {
			InetAddress candidateAddress = null;
			// Iterate all NICs (network interface cards)...
			for (Enumeration ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements();) {
				NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
				// Iterate all IP addresses assigned to each card...
				for (Enumeration inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements();) {
					InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
					if (!inetAddr.isLoopbackAddress()) {

						if (inetAddr.isSiteLocalAddress()) {
							// Found non-loopback site-local address. Return it
							// immediately...
							return inetAddr;
						} else if (candidateAddress == null) {
							// Found non-loopback address, but not necessarily
							// site-local.
							// Store it as a candidate to be returned if
							// site-local address is not subsequently found...
							candidateAddress = inetAddr;
							// Note that we don't repeatedly assign non-loopback
							// non-site-local addresses as candidates,
							// only the first. For subsequent iterations,
							// candidate will be non-null.
						}
					}
				}
			}
			if (candidateAddress != null) {
				// We did not find a site-local address, but we found some other
				// non-loopback address.
				// Server might have a non-site-local address assigned to its
				// NIC (or it might be running
				// IPv6 which deprecates the "site-local" concept).
				// Return this non-loopback candidate address...
				return candidateAddress;
			}
			// At this point, we did not find a non-loopback address.
			// Fall back to returning whatever InetAddress.getLocalHost()
			// returns...
			InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
			if (jdkSuppliedAddress == null) {
				throw new UnknownHostException("The JDK InetAddress.getLocalHost() method unexpectedly returned null.");
			}
			return jdkSuppliedAddress;
		} catch (Exception e) {
			UnknownHostException unknownHostException = new UnknownHostException(
					"Failed to determine LAN address: " + e);
			unknownHostException.initCause(e);
			throw unknownHostException;
		}
	}

	/**
	 * Checks to see if a specific port is available.
	 *
	 * @param port
	 *            the port to check for availability
	 */
	public static boolean portIsAvailable(int port) {
		ServerSocket ss = null;
		DatagramSocket ds = null;
		try {
			ss = new ServerSocket(port);
			ss.setReuseAddress(true);
			ds = new DatagramSocket(port);
			ds.setReuseAddress(true);
			return true;
		} catch (IOException e) {
		} finally {
			if (ds != null) {
				ds.close();
			}

			if (ss != null) {
				try {
					ss.close();
				} catch (IOException e) {
					/* should not be thrown */
				}
			}
		}

		return false;
	}

	public class FileSendListener implements SenderProgress {

		private String mFile;

		public FileSendListener(String file) {
			mFile = file;
		}

		@Override
		public void startSending(long size) {
		}

		@Override
		public void progressSending(long size) {
		}

		@Override
		public void endSending(long size) {
			RemoteLogger.performLog(LogPriority.INFORMATION, "Successfully send file :" + mFile, "Mediaserver");
		}

		@Override
		public void exceptionOccurred(Exception e) {
			RemoteLogger.performLog(LogPriority.ERROR, "Error occured sending file '" + mFile + "': "
					+ e.getClass().getSimpleName() + ": " + e.getMessage(), "Mediaserver");
		}

		@Override
		public void sendingCanceled() {
		}

	}

}
