package de.remote.desktop;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * main class to start frame and connect to server.
 * 
 * @author sebastian
 */
public class RemoteControl {

	public static void main(String[] args) {
		ControlFrame frame = new ControlFrame();
		String registry = "192.168.1.3";
		int port = 5007;
		if (args.length > 0)
			registry = args[0];
		if (args.length > 1)
			port = Integer.parseInt(args[1]);
		String name = "Mustermann";
		try {
			name = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException localUnknownHostException) {
		}
		frame.connectToServer(registry, port, name);
	}
}