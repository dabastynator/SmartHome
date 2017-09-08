package de.neo.smarthome.controlcenter;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import de.neo.remote.rmi.RemoteException;
import de.neo.remote.rmi.RMILogger.LogPriority;
import de.neo.smarthome.RemoteLogger;
import de.neo.smarthome.api.Event;
import de.neo.smarthome.controlcenter.IControllUnit.EventException;

public class EventWorker extends Thread {

	boolean mRunning = true;

	private BlockingQueue<Event> mEventQueue = new LinkedBlockingQueue<>();

	private ControlCenter mCenter;

	protected EventWorker(ControlCenter center) {
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
					IControllUnit unit = mCenter.getControlUnit(event.getUnitID());
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