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
	 * The ground plot is a bean that holds information about the ground plot.
	 * The information is in the list of walls.
	 */
	public GroundPlot() {
		walls = new ArrayList<GroundPlot.Wall>();
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

		public Wall() {
			door_width = -1;
		}
		
		// properties of one wall
		public float x1, x2, y1, y2, depth, height, door_width, door_distance,door_height;

	}

}
