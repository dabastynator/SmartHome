package de.neo.smarthome.api;

import java.util.ArrayList;

import de.neo.persist.DaoException;
import de.neo.remote.rmi.RemoteAble;
import de.neo.remote.rmi.RemoteException;
import de.neo.remote.web.WebField;
import de.neo.remote.web.WebGet;
import de.neo.remote.web.WebRequest;
import de.neo.smarthome.api.IControlCenter.BeanWeb;

public interface IWebLEDStrip extends RemoteAble {

	public enum LEDMode {
		NormalMode, PartyMode
	}

	/**
	 * List all led strips
	 * 
	 * @param user token
	 * @return led list
	 * @throws RemoteException
	 */
	@WebRequest(path = "list", description = "List all led strips.", genericClass = BeanLEDStrips.class)
	public ArrayList<BeanLEDStrips> getLEDStrips(@WebGet(name = "token") String token) throws RemoteException;

	/**
	 * Set color for specified led strip. Red, green and blue must between 0 and
	 * 255.
	 * 
	 * @param user  token
	 * @param id
	 * @param red
	 * @param green
	 * @param blue
	 * @return state of led strip
	 * @throws RemoteException
	 */
	@WebRequest(path = "setcolor", description = "Set color for specified led strip. Red, green and blue must between 0 and 255.")
	public BeanLEDStrips setColor(@WebGet(name = "token") String token, @WebGet(name = "id") String id,
			@WebGet(name = "red") int red, @WebGet(name = "green") int green, @WebGet(name = "blue") int blue)
			throws RemoteException;

	/**
	 * Set mode for specified led strip. 'NormalMode' simply shows the color,
	 * 'PartyMode' shows the color elements with strobe effect.
	 * 
	 * @param user token
	 * @param id
	 * @param mode
	 * @return state of led strip
	 * @throws RemoteException
	 */
	@WebRequest(path = "setmode", description = "Set mode for specified led strip. 'NormalMode' simply shows the color, 'PartyMode' shows the color elements with strobe effect.")
	public BeanLEDStrips setMode(@WebGet(name = "token") String token, @WebGet(name = "id") String id,
			@WebGet(name = "mode") LEDMode mode) throws RemoteException;

	/**
	 * Create new LED strip
	 * 
	 * @param token
	 * @param id
	 * @param name
	 * @param type
	 * @param x
	 * @param y
	 * @param y
	 * @return new led strip
	 * @throws RemoteException
	 * @throws DaoException
	 */
	@WebRequest(path = "create", description = "Create new LED strip.")
	public BeanLEDStrips createNew(@WebGet(name = "token") String token, @WebGet(name = "id") String id,
			@WebGet(name = "name") String name, @WebGet(name = "description") String description,
			@WebGet(name = "x") float x, @WebGet(name = "y") float y, @WebGet(name = "z") float z)
			throws RemoteException, DaoException;

	/**
	 * Update existing LED strip
	 * 
	 * @param token
	 * @param id
	 * @param name
	 * @param description
	 * @param x
	 * @param y
	 * @param z
	 * @return updated led strip
	 * @throws RemoteException
	 * @throws DaoException
	 */
	@WebRequest(path = "update", description = "Update existing LED strip.")
	public BeanLEDStrips updateExisting(@WebGet(name = "token") String token, @WebGet(name = "id") String id,
			@WebGet(name = "name") String name, @WebGet(name = "description") String description,
			@WebGet(name = "x") float x, @WebGet(name = "y") float y, @WebGet(name = "z") float z)
			throws RemoteException, DaoException;

	/**
	 * Delete LED strip
	 * 
	 * @param token
	 * @param id
	 * @throws RemoteException
	 * @throws DaoException
	 */
	@WebRequest(path = "delete", description = "Delete LED strip.")
	public void delete(@WebGet(name = "token") String token, @WebGet(name = "id") String id)
			throws RemoteException, DaoException;

	public static class BeanLEDStrips extends BeanWeb {

		@WebField(name = "red")
		private int mRed;

		@WebField(name = "green")
		private int mGreen;

		@WebField(name = "blue")
		private int mBlue;

		@WebField(name = "mode")
		private LEDMode mMode;

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

		public LEDMode getMode() {
			return mMode;
		}

		public void setMode(LEDMode mode) {
			mMode = mode;
		}

		public int getColor() {
			return mRed | (mGreen << 8) | (mBlue << 16);
		}
	}

}
