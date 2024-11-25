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
		private String mName;

		@WebField(name = "id")
		private String mID;

		public String getName() {
			return mName;
		}

		public void setName(String name) {
			mName = name;
		}

		public String getID() {
			return mID;
		}

		public void setID(String iD) {
			mID = iD;
		}

		public void merge(BeanWeb webBean) {
			mID = webBean.getID();
			mName = webBean.getName();
		}

	}
