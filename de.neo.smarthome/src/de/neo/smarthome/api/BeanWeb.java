package de.neo.smarthome.api;

import java.io.Serializable;

import de.neo.remote.web.WebField;

/**
 * The control center handles all control units and information about the
 * controlled object.
 * 
 * @author sebastian
 * 
 */
public class BeanWeb implements Serializable {

		private static final long serialVersionUID = -4066506544238955935L;

		@WebField(name = "name")
		public String mName;

		@WebField(name = "id")
		public String mID;

		public void merge(BeanWeb webBean) {
			mID = webBean.mID;
			mName = webBean.mID;
		}

	}
