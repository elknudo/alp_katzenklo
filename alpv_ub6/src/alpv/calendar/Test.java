package alpv.calendar;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		b();
	}
	
	private static void b() {
		/* client SERVERIPADDRESS SERVERPORT */
		String[] argz = { "client", "f00.0.0.1", "666" };
		Main.main(argz);
		
	}
	private static void a() {
		CalendarServer serv = new CalendarServerA();

		String name = "mahlzeit";
		String[] user = { "Tom", "Maria" };
		Date date = new Date(System.currentTimeMillis() + 30000);
		Event e1 = new Event(name, user, date);
		name = "party";
		String[] user2 = { "Schorsch", "Gunther", "Tom" };
		
		date = new Date(System.currentTimeMillis() + 60000);
		Event e2 = new Event(name, user2, date);

		try {
			e1.setId(serv.addEvent(e1));
			e2.setId(serv.addEvent(e2));

			List<Event> le1 = serv.listEvents("Tom");
			for (Event e : le1)
				System.out.println("Event: " + e.getName() + ", "
						+ rrr(e.getUser()) + ", " + e.getBegin());

			System.out.println();
			List<Event> le2 = serv.listEvents("Gunther");
			for (Event e : le2)
				System.out.println("Event: " + e.getName() + ", "
						+ rrr(e.getUser()) + ", " + e.getBegin());

			e2.setName("Kiez");
			serv.updateEvent(e2.getId(), e2);

			System.out.println();
			le2 = serv.listEvents("Gunther");
			for (Event e : le2)
				System.out.println("Event: " + e.getName() + ", "
						+ rrr(e.getUser()) + ", " + e.getBegin());

		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	private static String rrr(String[] arr) {
		String s = "";
		for(int i=0; i < arr.length; i++)
			s = s.concat(" " + arr[i]);
		return s;
	}

}
