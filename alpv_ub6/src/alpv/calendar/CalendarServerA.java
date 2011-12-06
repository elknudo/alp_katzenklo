package alpv.calendar;

import java.rmi.RemoteException;
import java.util.*;

/* Server for a) */
public class CalendarServerA implements CalendarServer {
	protected Vector<Event> events; // synchronize
	protected long runningId = 0; // use only in sync(events) blocks

	public CalendarServerA() {
		events = new Vector<Event>();
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
	public long addEvent(Event e) throws RemoteException {
		long id;
		synchronized (events) {
			// parameter e is a local copy (isn't it?)
			id = runningId++;
			e.setId(id);
			events.add(e);
		}
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
	public boolean removeEvent(long id) throws RemoteException {
		boolean removed = false;
		synchronized (events) {
			Iterator<Event> it = events.iterator();
			while (it.hasNext()) { // find event
				if (it.next().getId() == id) {
					it.remove();
					removed = true;
					break;
				}
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
	public boolean updateEvent(long id, Event e) throws RemoteException {
		boolean updated = false;
		e.setId(id);
		synchronized (events) {
			Iterator<Event> it = events.iterator();
			while (it.hasNext()) { // find event
				Event le = it.next();
				if (le.getId() == id) {
					le.setBegin(e.getBegin());
					le.setName(e.getName());
					le.setUser(e.getUser());
					updated = true;
					break;
				}
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
	public List<Event> listEvents(String user) throws RemoteException {
		List<Event> userEvents = new Vector<Event>();
		synchronized (events) {
			Iterator<Event> it = events.iterator();
			while (it.hasNext()) { // find event
				Event le = it.next();
				if (ArrayContainsString(le.getUser(), user))
					userEvents.add(le);

			}
		}
		return userEvents;
	}

	/* Not implemented */
	@Override
	public Event getNextEvent(String user) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void RegisterCallback(EventCallback ec, String user)
			throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void UnregisterCallback(EventCallback ec) throws RemoteException {
		// TODO Auto-generated method stub

	}

	// returns true if one of the strings in the array equals string s
	private boolean ArrayContainsString(String[] arr, String s) {
		for (int i = 0; i < arr.length; i++)
			if (arr[i].equals(s))
				return true;
		return false;
	}
}
