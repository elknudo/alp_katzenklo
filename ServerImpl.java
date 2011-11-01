package alpv_ws1112.ub1.webradio.webradio;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.sound.sampled.*;

import alpv_ws1112.ub1.webradio.communication.Server;

/*
 * ServerImpl schickt bei PlaySong() das AudioFormat des Songs in einem ObjectOutputStream an die Clients.
 * Danach wird der Song in Häppchen als byte[] über einen einfachen OutputStream geschickt.
 * 
 * Alles ungetestet (ich mache keine Fehler ;)
 */

public class ServerImpl implements Server {
	// behaviour
	private String netprotocol;
	private int port;
	private boolean running = true;
	private final int buffLen = 1024;
	// main socket
	ServerSocket serversocket;
	// client list
	private Vector<ConnectionHandler> connections;

	public ServerImpl(String netprotocol, int port) {
		this.netprotocol = netprotocol;
		this.port = port;
	}

	public void run() { // SERVER STARTING POINT
		if (netprotocol.equals("tcp")) {
			// create tcp connection
			System.out.println("Server: Starting TCP Server on port " + port);
			try {
				serversocket = new ServerSocket(port);
				while (running) {
					// listen and accept new clients
					ConnectionHandler c = new ConnectionHandler(
							serversocket.accept());
					// (blocks until new client tries to connect)
					System.out.println("Server: client accepted");
					connections.add(c);
				}
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}

		} else if (netprotocol.equals("udp")) {
			;
		} else if (netprotocol.equals("mc")) {
			;
		} else
			// do nothing if netprotocol is not valid
			return;
	}

	public void close() {
		// don't accept new connections
		running = false;
		// close connections
		Iterator<ConnectionHandler> i = connections.iterator();
		while (i.hasNext())
			i.next().close();
	}

	/* Playsong blockiert caller solange gestreamt wird */
	public void playSong(String path) throws MalformedURLException,
			UnsupportedAudioFileException, IOException {

		// Open audio stream
		final String soundfile = "staticx.wav";
		AudioInputStream ais = AudioSystem.getAudioInputStream(new File(
				soundfile));
		AudioFormat format = ais.getFormat();
		for (int i = 0; i < connections.size(); i++)
			connections.elementAt(i).initStreaming(format);

		// Push data from audio stream into network
		byte[] data = new byte[buffLen];
		while (ais.read(data) != -1)
			for (int i = 0; i < connections.size(); i++)
				connections.elementAt(i).streamData(data);

	}

	private class ConnectionHandler {
		// connection
		private Socket socket;
		// receive
		private BufferedReader fromClientBr;
		// send
		private OutputStream toClientOutputStream;
		private ObjectOutputStream oos;

		public ConnectionHandler(Socket s) {
			try {
				// open streams for communication
				socket = s;
				fromClientBr = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));
				toClientOutputStream = socket.getOutputStream();
				oos = new ObjectOutputStream(toClientOutputStream);

			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		public void initStreaming(AudioFormat format) {
			// tell client about the audio format
			try {
				oos.writeObject(format);
				// close oos and reopen outputStream??
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}

		public void streamData(byte[] data) {
			// stream data over network
			try {
				toClientOutputStream.write(data);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void close() {
			try {
				oos.close();
				fromClientBr.close();
				toClientOutputStream.close();
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
