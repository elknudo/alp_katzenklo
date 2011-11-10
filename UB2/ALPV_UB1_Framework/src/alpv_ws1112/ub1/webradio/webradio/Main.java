package alpv_ws1112.ub1.webradio.webradio;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.sound.sampled.UnsupportedAudioFileException;

import alpv_ws1112.ub1.webradio.communication.Server;
public class Main {
	private static final String USAGE = String
			.format("usage: java -jar UB%%X_%%NAMEN [-options] server tcp|udp|mc PORT%n"
					+ "         (to start a server)%n"
					+ "or:    java -jar UB%%X_%%NAMEN [-options] client tcp|udp|mc SERVERIPADDRESS SERVERPORT USERNAME%n"
					+ "         (to start a client)");

	/**
	 * Starts a server/client according to the given arguments, using a GUI or
	 * just the command-line according to the given arguments.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			boolean useGUI = false;
			int i = -1;

			
			// Parse options. Add additional options here if you have to. Do not
			// forget to mention their usage in the help-string!
			while (args[++i].startsWith("-")) {
				if (args[i].equals("-help")) {
					System.out.println(USAGE
							+ String.format("%n%nwhere options include:"));
					System.out.println("  -help      Show this text.");
					System.out
							.println("  -gui       Show a graphical user interface.");
					System.exit(0);
				} else if (args[i].equals("-gui")) {
					useGUI = true;
				}
			}

			if (args[i].equals("server")) {
					Server server = new ServerImpl2(args[i + 1],
							Integer.valueOf(args[i + 2]));
					Thread thread = new Thread(server);
					thread.start();
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						System.err.println("main() start: " + e.getMessage());
						e.printStackTrace();
					}
					try {
						server.playSong("staticx.wav");
					} catch (MalformedURLException e) {
						System.err.println("main() start: " + e.getMessage());
						e.printStackTrace();
					} catch (UnsupportedAudioFileException e) {
						System.err.println("main() start: " + e.getMessage());
						e.printStackTrace();
					} catch (IOException e) {
						System.err.println("main() start: " + e.getMessage());
						e.printStackTrace();
					}
//					server.close();
//					thread.join();
			} else if (args[i].equals("client")) {
				new Thread(new ClientImpl2(args[i + 1], args[i + 2], args[i + 3], args[i + 4])).start();
			} else
				throw new IllegalArgumentException();
		} catch (ArrayIndexOutOfBoundsException e) {
			System.err.println(USAGE);
		} catch (NumberFormatException e) {
			System.err.println(USAGE);
		} catch (IllegalArgumentException e) {
			System.err.println(USAGE);
		}
	}
}
