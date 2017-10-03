package de.neo.smarthome.mobile.api;

import java.util.ArrayList;

import de.neo.remote.rmi.RemoteAble;
import de.neo.remote.rmi.RemoteException;
import de.neo.remote.web.WebField;
import de.neo.remote.web.WebGet;
import de.neo.remote.web.WebRequest;

public interface IWebLEDStrip extends RemoteAble {

	public enum LEDMode {
		NormalMode, PartyMode
	}

	/**
	 * List all led strips
	 * 
	 * @return list of led strips
	 */
	@WebRequest(path = "list", description = "List all led strips.", genericClass = BeanLEDStrips.class)
	public ArrayList<BeanLEDStrips> getLEDStrips();

	/**
	 * Set color for specified led strip. Red, green and blue must between 0 and
	 * 255.
	 * 
	 * @param id
	 * @param red
	 * @param green
	 * @param blue
	 * @return state of led strip
	 * @throws RemoteException
	 */
	@WebRequest(path = "setcolor", description = "Set color for specified led strip. Red, green and blue must between 0 and 255.")
	public BeanLEDStrips setColor(@WebGet(name = "id") String id, @WebGet(name = "red") int red,
                                  @WebGet(name = "green") int green, @WebGet(name = "blue") int blue) throws RemoteException;

	/**
	 * Set mode for specified led strip. 'NormalMode' simply shows the color,
	 * 'PartyMode' shows the color elements with strobe effect.
	 * 
	 * @param id
	 * @param mode
	 * @return state of led strip
	 * @throws RemoteException
	 */
	@WebRequest(path = "setmode", description = "Set mode for specified led strip. 'NormalMode' simply shows the color, 'PartyMode' shows the color elements with strobe effect.")
	public BeanLEDStrips setMode(@WebGet(name = "id") String id, @WebGet(name = "mode") LEDMode mode)
			throws RemoteException;

	public static class BeanLEDStrips extends IControlCenter.BeanWeb {

		@WebField(name = "red")
		private int mRed;

		@WebField(name = "green")
		private int mGreen;

		@WebField(name = "blue")
		private int mBlue;

		public int getRed() {
			return mRed;
		}

		public void setRed(int red) {
			mRed = red;
		}

		public int getGreen() {
			return mGreen;
		}

		public void setGreen(int green) {
			mGreen = green;
		}

		public int getBlue() {
			return mBlue;
		}

		public void setBlue(int blue) {
			mBlue = blue;
		}

		public int getColor() {
			return mRed | (mGreen << 8) | (mBlue << 16);
		}
	}

}
