package de.neo.smarthome.mobile.api;

import java.io.Serializable;
import java.util.ArrayList;

import de.neo.persist.annotations.Domain;
import de.neo.persist.annotations.OneToMany;
import de.neo.persist.annotations.Persist;
import de.neo.remote.web.WebField;

/**
 * The ground plot is a bean that holds information about the ground plot.
 * 
 * @author sebastian
 */
@Domain
public class GroundPlot implements Serializable {

	/**
	 * Generated uid
	 */
	private static final long serialVersionUID = -7902771836935978638L;

	/**
	 * List of walls of the ground plot
	 */
	@WebField(name = "walls", genericClass = Wall.class)
	@OneToMany(domainClass = Wall.class, name = "Wall")
	public ArrayList<Wall> mWalls = new ArrayList<>();

	/**
	 * The wall contains all information about one wall of the ground plot.
	 * 
	 * @author sebastian
	 */
	@Domain
	public static class Wall implements Serializable {

		/**
		 * Generated uid
		 */
		private static final long serialVersionUID = 4963248372236235849L;

		/**
		 * all points of the wall
		 */
		@WebField(name = "points", genericClass = Point.class)
		@OneToMany(domainClass = Point.class, name = "Point")
		public ArrayList<Point> mPoints = new ArrayList<>();

	}

	/**
	 * The Points holds information about one point: x,y and z
	 * 
	 * @author sebastian
	 */
	@Domain
	public static class Point implements Serializable {

		/**
		 * Generated uid
		 */
		private static final long serialVersionUID = 1045224009502795464L;

		@WebField(name = "x")
		@Persist(name = "x")
		public float mX;
		@WebField(name = "y")
		@Persist(name = "y")
		public float mY;
		@WebField(name = "z")
		@Persist(name = "z")
		public float mZ;

	}

}
