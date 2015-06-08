package de.neo.remote.controlcenter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.neo.remote.RemoteLogger;
import de.neo.remote.api.Event;
import de.neo.remote.api.IControlUnit;
import de.neo.remote.api.IControlUnit.EventException;
import de.neo.rmi.api.RMILogger.LogPriority;
import de.neo.rmi.protokol.RemoteException;

public class EventWorker extends Thread {

	boolean mRunning = true;

	private List<Event> mEventQueue = Collections
			.synchronizedList(new ArrayList<Event>());

	private ControlCenterImpl mCenter;

	protected EventWorker(ControlCenterImpl center) {
		mCenter = center;
		start();
	}

	public synchronized void queueEvents(Event[] events) {
		for (Event event : events)
			mEventQueue.add(event);
		notify();
	}

	public synchronized void queueEvent(Event event) {
		mEventQueue.add(event);
		notify();
	}

	private synchronized void executeEvents() {
		while (mEventQueue.size() > 0) {
			Event event = mEventQueue.get(0);
			mEventQueue.remove(0);

			try {
				IControlUnit unit = mCenter.getControlUnit(event.getUnitID());
				if (unit == null)
					throw new EventException("No control unit found: "
							+ event.getUnitID());
				unit.performEvent(event);
			} catch (EventException | RemoteException e) {
				RemoteLogger.performLog(LogPriority.ERROR, e.getMessage(),
						event.getUnitID());
			}
		}
	}

	@Override
	public void run() {
		try {
			while (mRunning) {
				executeEvents();
				synchronized (this) {
					wait();
				}
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}