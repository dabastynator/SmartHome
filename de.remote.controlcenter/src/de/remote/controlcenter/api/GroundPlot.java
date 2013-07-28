package de.remote.controlcenter.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
	public List<Wall> walls;

	/**
	 * List of features of the ground plot
	 */
	public List<Feature> features;

	/**
	 * The ground plot is a bean that holds information about the ground plot.
	 * The information is in the list of walls.
	 */
	public GroundPlot() {
		walls = new ArrayList<GroundPlot.Wall>();
		features = new ArrayList<GroundPlot.Feature>();
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
		public ArrayList<Point> points;

		public Wall() {
			points = new ArrayList<Point>();
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

		/**
		 * coordinates of the point.
		 */
		public float x, y, z;

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
		public float x, y, z, ax;
		public String type, extra;

	}

}
