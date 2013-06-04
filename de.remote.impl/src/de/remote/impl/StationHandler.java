package de.remote.impl;

import java.util.ArrayList;
import java.util.List;

import de.newsystem.rmi.protokol.RemoteException;
import de.remote.api.IMusicStation;
import de.remote.api.IStationHandler;

public class StationHandler implements IStationHandler {

	private List<IMusicStation> musicStations = new ArrayList<IMusicStation>();

	public StationHandler() {
		new Thread() {
			@Override
			public void run() {
				while (true) {
					checkStations();
					try {
						Thread.sleep(1000 * 60 * 10);
					} catch (InterruptedException e) {
					}
				}
			}
		}.start();
	}

	/**
	 * check whether stations are available.
	 */
	protected void checkStations() {
		List<IMusicStation> exceptionList = new ArrayList<IMusicStation>();
		for (IMusicStation station: musicStations){
			try {
				station.getName();
			} catch (RemoteException e) {
				exceptionList.add(station);
			}
		}
		musicStations.removeAll(exceptionList);
	}

	@Override
	public int getStationSize() throws RemoteException {
		return musicStations.size();
	}

	@Override
	public IMusicStation getStation(int station) throws RemoteException {
		if (station >= getStationSize())
			throw new RemoteException(IStationHandler.STATION_ID,
					"wrong index for musicstation: " + station);
		return musicStations.get(station);
	}

	@Override
	public void addMusicStation(IMusicStation station) throws RemoteException {
		musicStations.add(station);
		System.out.println("get station: " + station.getName());
	}

}
