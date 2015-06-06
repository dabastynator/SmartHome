package de.neo.remote.mediaserver;

import de.neo.remote.AbstractControlUnit;
import de.neo.remote.ControlUnitFactory;
import de.neo.remote.api.IControlCenter;

public class MediaUnitFactory implements ControlUnitFactory {

	@Override
	public AbstractControlUnit createControlUnit(IControlCenter center) {
		return new MediaControlUnit(center);
	}

}
