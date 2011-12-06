package alpv.calendar;

import java.io.*;
import java.net.*;
import java.util.*;

public class Client implements Runnable {
	protected InetSocketAddress serverAddress;
	protected CalendarServer serv;

	public Client(String ip, String port) {

		// serverAddress = new InetSocketAddress(ip, Integer.parseInt(port));
		// stub = RMI registry server blub

		serv = new CalendarServerA();
	}

	public void run() {
		System.out.println("Manage your calendar with add, remove, update and list");
		boolean running = true;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		while (running) {
			try {
				String input = br.readLine();

				if (input.equals("q")) {
					running = false;
				} else if (input.startsWith("add")) {
					long id = serv.addEvent(eventFromConsole());
					System.out.println("Event added with id " + id);
				} else if (input.startsWith("remove")) {
					if (serv.removeEvent(idFromConsole()))
						System.out.println("Event removed.");
					else
						System.out.println("could not remove event");

				} else if (input.startsWith("update")) {
					long id = idFromConsole();
					Event e = eventFromConsole();
					if (serv.updateEvent(id, e))
						System.out.println("Event updated.");
					else
						System.out.println("could not remove event");
				} else if ( input.startsWith("list")) {
					String name = nameFromConsole();
					List<Event> le = serv.listEvents(name);
					if(le.size() == 0)
						System.out.println("No events for " + name);
					for(Event e : le)
						printEvent(e);
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// asks user for Name, Users and Time
	private Event eventFromConsole() {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			System.out.println("Event info: ");
			System.out.print("Name: ");
			String name = br.readLine();
			System.out.print("Users (comma seperated, no whitespaces):");
			String userInput = br.readLine();
			String[] users = userInput.split(",");
			System.out.print("Time from now in secs: ");
			String time = br.readLine();
			long msTime = Long.parseLong(time) * 1000
					+ System.currentTimeMillis();
			return new Event(name, users, new Date(msTime));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	// asks user for an ID
	private long idFromConsole() {
		System.out.print("id: ");
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					System.in));
			String idLine = br.readLine();
			return Long.parseLong(idLine);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return -1;
	}

	// ask for name
	private String nameFromConsole() {
		System.out.print("Name: ");
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					System.in));
			String nameLine = br.readLine();
			return nameLine;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void printEvent(Event e) {
		System.out.println("Event: " + e.getName() + ", " + rrr(e.getUser())
				+ ", " + e.getBegin());
	}

	private static String rrr(String[] arr) {
		String s = "";
		for (int i = 0; i < arr.length; i++)
			s = s.concat(" " + arr[i]);
		return s;
	}
}
