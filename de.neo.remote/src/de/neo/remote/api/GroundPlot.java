package de.neo.remote.api;

import java.io.Serializable;
import java.util.ArrayList;

import de.neo.rmi.api.WebField;

/**
 * The ground plot is a bean that holds information about the ground plot.
 * 
 * @author sebastian
 */
public class GroundPlot implements Serializable {

	/**
	 * Generated uid
	 */
	private static final long serialVersionUID = -7902771836935978638L;

	/**
	 * List of walls of the ground plot
	 */
	@WebField(name = "walls", genericClass = Wall.class)
	public ArrayList<Wall> mWalls;

	/**
	 * List of features of the ground plot
	 */
	@WebField(name = "features", genericClass = Feature.class)
	public ArrayList<Feature> mFeatures;

	/**
	 * The ground plot is a bean that holds information about the ground plot.
	 * The information is in the list of walls.
	 */
	public GroundPlot() {
		mWalls = new ArrayList<GroundPlot.Wall>();
		mFeatures = new ArrayList<GroundPlot.Feature>();
	}

	/**
	 * The wall contains all information about one wall of the ground plot.
	 * 
	 * @author sebastian
	 */
	public static class Wall implements Serializable {

		/**
		 * Generated uid
		 */
		private static final long serialVersionUID = 4963248372236235849L;

		/**
		 * all points of the wall
		 */
		@WebField(name = "points", genericClass = Point.class)
		public ArrayList<Point> mPoints;

		public Wall() {
			mPoints = new ArrayList<Point>();
		}

	}

	/**
	 * The Points holds information about one point: x,y and z
	 * 
	 * @author sebastian
	 */
	public static class Point implements Serializable {

		/**
		 * Generated uid
		 */
		private static final long serialVersionUID = 1045224009502795464L;

		@WebField(name = "x")
		public float x;
		@WebField(name = "y")
		public float y;
		@WebField(name = "z")
		public float z;

	}

	/**
	 * The feature defines one feature in the ground plot.
	 * 
	 * @author sebastian
	 */
	public static class Feature implements Serializable {

		/**
		 * Generated uid
		 */
		private static final long serialVersionUID = 1529625875832161486L;

		// properties of the feature
		public float x, y, z, az;
		public String type, extra;

	}

}
