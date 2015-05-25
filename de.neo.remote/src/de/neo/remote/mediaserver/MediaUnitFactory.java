package de.neo.remote.mediaserver;

import de.neo.remote.AbstractControlUnit;
import de.neo.remote.ControlUnitFactory;

public class MediaUnitFactory implements ControlUnitFactory {

	@Override
	public AbstractControlUnit createControlUnit() {
		return new MediaControlUnit();
	}

}
