package de.neo.smarthome.mediaserver;

import de.neo.smarthome.AbstractControlUnit;
import de.neo.smarthome.AbstractUnitHandler;
import de.neo.smarthome.ControlUnitFactory;
import de.neo.smarthome.controlcenter.IControlCenter;

public class MediaUnitFactory implements ControlUnitFactory {

	@Override
	public AbstractControlUnit createControlUnit(IControlCenter center) {
		return new MediaControlUnit(center);
	}

	@Override
	public AbstractUnitHandler createUnitHandler(IControlCenter center) {
		return new WebMediaServerImpl(center);
	}

}
