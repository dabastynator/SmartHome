package de.hcl.synchronize.log;

import java.util.Date;

import de.hcl.synchronize.api.IHCLClient;

/**
 * The IHCLLog interface enable listening on home cloud activities.
 * 
 * @author sebastian
 * 
 */
public interface IHCLLog {

	/**
	 * There are several type of actions on files available.
	 * 
	 * @author sebastian
	 */
	public enum HCLType {
		CREATE, UPDATE, DELETE, SEND, ERROR
	};

	/**
	 * perform message
	 * 
	 * @param message
	 */
	public void hclLog(IHCLMessage message);

	/**
	 * the message bean contains a message, time and type.
	 * 
	 * @author sebastian
	 */
	public static class IHCLMessage {

		/**
		 * allocate new home cloud message.
		 * 
		 * @param message
		 * @param type
		 * @param date
		 */
		public IHCLMessage(String message, HCLType type, Date date,
				IHCLClient author) {
			this.message = message;
			this.type = type;
			time = date;
			client = author;
		}

		/**
		 * message of action
		 */
		public String message;

		/**
		 * time of action
		 */
		public Date time;

		/**
		 * type of action
		 */
		public HCLType type;

		/**
		 * author of the message
		 */
		public IHCLClient client;

	}
}
