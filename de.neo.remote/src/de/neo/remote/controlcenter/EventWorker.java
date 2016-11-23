package de.neo.remote.controlcenter;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import de.neo.remote.RemoteLogger;
import de.neo.remote.api.Event;
import de.neo.remote.api.IControlUnit;
import de.neo.remote.api.IControlUnit.EventException;
import de.neo.rmi.api.RMILogger.LogPriority;
import de.neo.rmi.protokol.RemoteException;

public class EventWorker extends Thread {

	boolean mRunning = true;

	private BlockingQueue<Event> mEventQueue = new LinkedBlockingQueue<>();

	private ControlCenterImpl mCenter;

	protected EventWorker(ControlCenterImpl center) {
		mCenter = center;
		start();
	}

	public synchronized void queueEvents(Event[] events) {
		for (Event event : events)
			mEventQueue.add(event);
	}

	public synchronized void queueEvent(Event event) {
		mEventQueue.add(event);
	}

	@Override
	public void run() {
		while (mRunning) {
			try {
				Event event = mEventQueue.take();
				try {
					IControlUnit unit = mCenter.getControlUnit(event.getUnitID());
					if (unit == null)
						throw new EventException("No control unit found: " + event.getUnitID());
					unit.performEvent(event);
				} catch (EventException | RemoteException e) {
					RemoteLogger.performLog(LogPriority.ERROR, e.getMessage(), event.getUnitID());
				}
			} catch (InterruptedException e) {
				RemoteLogger.performLog(LogPriority.ERROR, e.getMessage(), "EventWorker");
			}
		}
	}
}