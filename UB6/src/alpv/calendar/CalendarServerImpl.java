package alpv.calendar;

import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.util.*;
import java.util.concurrent.atomic.*;

/* Server for a) */
public class CalendarServerImpl implements CalendarServer, Runnable {
	protected Vector<Event> events; // synchronize
	protected AtomicLong runningId; // use only in sync(events) blocks
	protected AtomicLong tLastChange;
	protected final int polling_timeout = 1000; // check every second

	public CalendarServerImpl() {
		events = new Vector<Event>();
		runningId = new AtomicLong(0);
		tLastChange = new AtomicLong(0);
	}

	synchronized public boolean init(String port) {
		try {
			new Thread(this).start();
			int iPort = Integer.parseInt(port);
			String name = "Calendar";
			CalendarServerImpl serv = this;
			CalendarServer stub = (CalendarServer) UnicastRemoteObject
					.exportObject(serv, iPort);
			Registry registry = LocateRegistry.createRegistry(iPort);
			registry.rebind(name, stub);
			System.out.println("Server: CalendarServer bound");
		} catch (Exception e) {
			System.err.println("Server: Calendar exception:");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * add an event to the database of the server the field e.id has to be
	 * ignored - the server needs to keep track of id-numbers and assign a new
	 * id
	 * 
	 * @param e
	 *            event to add
	 * @return returns the id of the event
	 * @throws RemoteException
	 */
	@Override
	synchronized public long addEvent(Event e) throws RemoteException {
		long id;
		id = runningId.getAndIncrement();
		e.setId(id);
		events.add(e);
		tLastChange.set(System.currentTimeMillis());

		return id;
	}

	/**
	 * remove an event from the database
	 * 
	 * @param id
	 *            the id of the event
	 * @return true if the event was found and removed
	 * @throws RemoteException
	 */
	@Override
	synchronized public boolean removeEvent(long id) throws RemoteException {
		boolean removed = false;
		Iterator<Event> it = events.iterator();
		while (it.hasNext()) { // find event
			if (it.next().getId() == id) {
				it.remove();
				removed = true;
				tLastChange.set(System.currentTimeMillis());
				break;
			}
		}

		return removed;
	}

	/**
	 * sets every field of the event with the same id to the new values of e
	 * 
	 * @param id
	 * @param e
	 * @return true if e.id exists and the server was able to update every field
	 * @throws RemoteException
	 */
	@Override
	synchronized public boolean updateEvent(long id, Event e)
			throws RemoteException {
		boolean updated = false;
		e.setId(id);
		Iterator<Event> it = events.iterator();
		while (it.hasNext()) { // find event
			Event le = it.next();
			if (le.getId() == id) {
				le.setBegin(e.getBegin());
				le.setName(e.getName());
				le.setUser(e.getUser());
				updated = true;
				tLastChange.set(System.currentTimeMillis());
				break;
			}

		}
		return updated;
	}

	/**
	 * @param user
	 * @return a list with all the events having user as their user-field
	 * @throws RemoteException
	 */
	@Override
	synchronized public List<Event> listEvents(String user)
			throws RemoteException {
		List<Event> userEvents = new Vector<Event>();
		Iterator<Event> it = events.iterator();
		while (it.hasNext()) { // find event
			Event le = it.next();
			if (ArrayContainsString(le.getUser(), user))
				userEvents.add(le);

		}
		return userEvents;
	}

	@Override
	synchronized public Event getNextEvent(String user) throws RemoteException {
		Event eventNow = nextEventFor(user);
		if (eventNow == null) // return if there is no event for this user
			return null;
		long tCheck = System.currentTimeMillis();

		while (eventNow != null // there is an event in the future and
				&& eventNow.getBegin().getTime() > System.currentTimeMillis()) {
			// put to sleep (awoken every sec by the run()-Thread)
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// awake:
			if (tCheck < tLastChange.get()) {
				eventNow = nextEventFor(user);
				tCheck = System.currentTimeMillis();
			}
		}
		return eventNow;
	}

	/* Not implemented */
	@Override
	synchronized public void RegisterCallback(EventCallback ec, String user)
			throws RemoteException {
		// TODO Auto-generated method stub

	}

	/* Not implemented */
	@Override
	synchronized public void UnregisterCallback(EventCallback ec)
			throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	synchronized public void run() {
		while (true) {
			// every thread waiting for an event is notified every second
			notifyAll();
			try {
				wait(polling_timeout);
				// after this wait has timed out the run()-thread will try to
				// get back the monitor lock
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	// private methods: called only from inside a synchronized monitor function

	private Event nextEventFor(String user) {
		Event nextForUser = null;
		long currentTime = System.currentTimeMillis();
		for (Event e : events) {
			// is event in the future?
			if (e.getBegin().compareTo(new Date(currentTime)) >= 0) {
				// is user on the list?
				if (ArrayContainsString(e.getUser(), user)) {
					// first event in the future for user found
					if (nextForUser == null) {
						nextForUser = e;
						continue;
					}
					// is event sooner than nextForUser?
					if (e.getBegin().compareTo(nextForUser.getBegin()) < 0) {
						// save
						nextForUser = e;
					}
				}
			}
		}
		return nextForUser;
	}

	// returns true if one of the strings in the array equals string s
	private boolean ArrayContainsString(String[] arr, String s) {
		for (int i = 0; i < arr.length; i++)
			if (arr[i].equals(s))
				return true;
		return false;
	}
}
