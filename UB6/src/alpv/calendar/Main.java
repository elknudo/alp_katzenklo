package alpv.calendar;


public class Main {
	private static final String	USAGE	= String.format("usage: java -jar UB%%X_%%NAMEN server PORT%n" +
														"         (to start a server)%n" +
														"or:    java -jar UB%%X_%%NAMEN client SERVERIPADDRESS SERVERPORT%n" +
														"         (to start a client)");

	/**
	 * Starts a server/client according to the given arguments. 
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			int i = 0;

			if(args[i].equals("server")) {
				String port = args[i+1];
				(new CalendarServerImpl()).init(port);
			}
			else if(args[i].equals("client")) {
				String ip = args[i+1];
				String port = args[i+2];
				(new Client()).init(ip, port);
			}
			else
				throw new IllegalArgumentException();
		}
		catch(ArrayIndexOutOfBoundsException e) {
			System.err.println(USAGE);
		}
		catch(NumberFormatException e) {
			System.err.println(USAGE);
		}
		catch(IllegalArgumentException e) {
			System.err.println(USAGE);
		}
	}
}