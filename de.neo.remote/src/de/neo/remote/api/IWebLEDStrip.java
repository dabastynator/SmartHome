package de.neo.remote.api;

import java.util.ArrayList;

import de.neo.remote.api.IControlCenter.BeanWeb;
import de.neo.rmi.api.WebField;
import de.neo.rmi.api.WebGet;
import de.neo.rmi.api.WebRequest;
import de.neo.rmi.protokol.RemoteAble;

public interface IWebLEDStrip extends RemoteAble {

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
	 */
	@WebRequest(path = "setcolor", description = "Set color for specified led strip. Red, green and blue must between 0 and 255.")
	public BeanLEDStrips setColor(@WebGet(name = "id") String id, @WebGet(name = "red") int red,
			@WebGet(name = "green") int green, @WebGet(name = "blue") int blue);

	public static class BeanLEDStrips extends BeanWeb {

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
	}

}
