package de.hcl.synchronize.server;

import java.io.File;

import de.hcl.synchronize.api.IHCLClient;
import de.hcl.synchronize.client.MainSynchClient;
import de.hcl.synchronize.gui.SynchronizerGui;
import de.hcl.synchronize.log.HCLLogger;
import de.hcl.synchronize.log.IHCLLogListener;
import de.neo.rmi.api.RMILogger;
import de.neo.rmi.api.RMILogger.LogPriority;
import de.neo.rmi.api.RMILogger.RMILogListener;
import de.neo.rmi.protokol.RemoteException;

/**
 * The main synchronizer is the start class in the jar file and gets parameter.
 * 
 * @author sebastian
 */
public class MainSynchronizer {

	public static void main(String args[]) {
		configureRMILogOutput();
		configureHCLLogOutput();
		if (args.length == 0 || (args.length == 1 && args[0].contains("-h"))) {
			printParameter();
		}
		if (args.length == 0 || (args.length == 1 && args[0].contains("-g"))) {
			new SynchronizerGui().setVisible(true);
		} else {
			String registry = null;
			String config = null;
			boolean client = true;
			for (int i = 0; i < args.length; i++) {
				if (args[i].startsWith("-s") || args[i].startsWith("--server")) {
					client = false;
					continue;
				}
				if (args[i].startsWith("-r")
						|| args[i].startsWith("--registry")) {
					if (i + 1 >= args.length) {
						System.err.println("missing registry ip after -r");
						printParameter();
						System.exit(1);
					}
					registry = args[i + 1];
					i++;
					continue;
				}
				if (args[i].startsWith("-c") || args[i].startsWith("--config")) {
					if (i + 1 >= args.length) {
						System.err.println("missing config file after -c");
						printParameter();
						System.exit(1);
					}
					config = args[i + 1];
					i++;
					continue;
				}
				System.err.println("unknown parameter: " + args[i]);
				printParameter();
				System.exit(1);
			}
			if (registry == null) {
				System.err.println("registry missing.");
				printParameter();
				System.exit(1);
			}
			if (client) {
				if (config == null || (!new File(config).exists())) {
					System.err
							.println("configuration file for client missing or does not exist.");
					printParameter();
					System.exit(1);
				}
				MainSynchClient.main(registry, config);
			} else {
				MainSynchServer.main(registry);
			}
		}
	}

	private static void printParameter() {
		System.out.println("Parameter: ");
		System.out.println("  -r, --registry\tip of the registry to connect");
		System.out.println("  -s, --server\t\tstart as server");
		System.out
				.println("  -c, --config\t\tlocation of configure file in ini format");
		System.out.println("  -g, --gui\t\tstart configure gui");
	}

	/**
	 * print rmi log on str and err output
	 */
	public static void configureRMILogOutput() {
		RMILogger.addLogListener(new RMILogListener() {

			@Override
			public void rmiLog(LogPriority priority, String message, String id,
					long date) {
				if (priority == LogPriority.INFORMATION)
					System.out.println(priority + ": " + message + " (" + id
							+ ")");
				else
					System.err.println(priority + ": " + message + " (" + id
							+ ")");
			}
		});
	}

	/**
	 * print rmi log on str and err output
	 */
	public static void configureHCLLogOutput() {
		HCLLogger.addListener(new IHCLLogListener() {

			@Override
			public void hclLog(IHCLMessage message) {
				String author = message.client.getClass().getSimpleName();
				if (message.client instanceof IHCLClient)
					try {
						author = ((IHCLClient) message.client).getName();
					} catch (RemoteException e) {
					}
				if (message.client instanceof String)
					author = (String) message.client;
				if (message.type == HCLType.ERROR)
					System.err.println(message.type + ": " + message.message
							+ " (" + author + ")");
				else
					System.out.println(message.type + ": " + message.message
							+ " (" + author + ")");
			}
		});
	}

}
