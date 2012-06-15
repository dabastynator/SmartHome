package de.webcam.server;

import de.newsystem.rmi.api.Server;
import de.webcam.api.IWebcam;
import de.webcam.impl.Webcam;


public class WebcamServer {
	
	public static void main(String[] args) throws Exception{
		
		if (args.length == 0)
			throw new Exception("first argument must be ip of the registry");
		
		Server s = Server.getServer();
		
		s.forceConnectToRegistry(args[0]);
		s.startServer(IWebcam.PORT);
		Webcam webcam = new Webcam();
		s.register(IWebcam.WEBCAM_SERVER, webcam);
	}
	
}
