package alpv_ws1112.ub1.webradio.webradio;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.sound.sampled.*;

import java.util.concurrent.atomic.*;

import alpv_ws1112.ub1.webradio.communication.Server;

/*
 * Server runs in multiple threads
 * 
 * 1) accepting clients 				ClientListener.run()
 * 2) playing music						JukeBox.run()
 * 3) accepting control commands (callers thread)
 * 
 */
public class ServerImpl implements Server {

	// shared
	private AtomicBoolean running = new AtomicBoolean(true);
	private Vector<ConnectionHandler> connections = new Vector<ServerImpl.ConnectionHandler>(); // synchronize!

	// client listener
	private ClientListener listener = null;
	private Thread listener_thread = null;

	// Playback stuff
	private JukeBox jb = null;
	private Thread jbThread = null;

	// c'tor
	public ServerImpl(String netprotocol, int port) {
		listener = new ClientListener(netprotocol, port);
		listener_thread = new Thread(listener);
		listener_thread.start();
	}

	public void run() {
		// running in "child"-threads
	}

	/* Playsong blockiert caller solange gestreamt wird */
	public void playSong(String path) throws MalformedURLException,
			UnsupportedAudioFileException, IOException {
		if (jb == null) {
			jb = new JukeBox();
			jbThread = new Thread(jb);
			jbThread.start();
		}
		jb.changeSong(path);
	}

	public void close() {
		// don't accept new connections
		running.set(false);
		// jukeBox and Listenr will end run() because running=false
		try {
			listener_thread.join();
			jbThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// close connections
		synchronized (connections) {
			Iterator<ConnectionHandler> i = connections.iterator();
			while (i.hasNext())
				i.next().close();
			connections.clear();
		}
	}

	private class ClientListener implements Runnable {

		// main socket
		private ServerSocket serversocket;
		private String netprotocol;
		private int port;

		public ClientListener(String protocol, int port) {
			this.netprotocol = protocol;
			this.port = port;
		}

		public void run() {
			if (netprotocol.equals("tcp")) {
				// create tcp connection
				try {
					serversocket = new ServerSocket(port);
					System.out.println("Server: Listening for " + netprotocol
							+ " Connections on port " + port);
					while (running.get()) {
						// listen and accept new clients
						ConnectionHandler c = new ConnectionHandler(
								serversocket.accept());
						// (blocks until new client tries to connect)
						System.out.println("Server: client accepted");
						synchronized (connections) {
							connections.add(c);
						}

					}
				} catch (Exception e) {
					System.err.println("Client Listener run() error: "
							+ e.getMessage());
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
			synchronized (serversocket) {
				try {
					serversocket.close();
				} catch (IOException e) {
					System.err.println("Client Listener close() error: ");
					e.printStackTrace();
				}
			}
		}
	}

	private class JukeBox implements Runnable {// behaviour & server state
		// buffer
		private final int buffLen = 4096;

		// shared objects
		// synchronize!
		private AudioInputStream ais;
		private AudioFormat format;
		private AtomicBoolean playing = new AtomicBoolean(false);

		/*
		 * Stop playback while changeSong() run continues playback after
		 * changeSong() has finished
		 */
		public void changeSong(String filename)
				throws UnsupportedAudioFileException, IOException {
			// stop playback
			playing.set(false);
			// Open audio stream
			if (ais != null)
				synchronized (ais) {
					ais = AudioSystem.getAudioInputStream(new File(filename));
					// get Format
					synchronized (format) {
						format = ais.getFormat();
					}
				}
			else { // first call to changeSong(): cannot synchronize on null
				ais = AudioSystem.getAudioInputStream(new File(filename));
				format = ais.getFormat();
			}

			// start playback
			System.out.println("Server: Starting to stream song " + filename);
			playing.set(true);
		}

		public void run() {
			long tBegin = System.currentTimeMillis();
			long tEnd = 0;
			while (running.get()) {
				if (playing.get()) {
					// send next sample
					byte[] data = new byte[buffLen];
					// read data
					synchronized (ais) {
						try {
							ais.read(data);
						} catch (IOException e) {
							System.err
									.println("AudioInputStream.read() error in JukeBox.run()");
							e.printStackTrace();
						}
					}
					// send data
					synchronized (connections) {
						// Push data from audio stream into network

						Iterator<ConnectionHandler> it;
						it = connections.iterator();
						while (it.hasNext()) {
							synchronized (format) {
								it.next().streamData(format, data);
							}
						}
					}
					try {
						/*
						 * Schedule playback
						 * 
						 * example: samplerate = 44100.0 Hz sample duration d =
						 * 1 / 44100 (seconds) = 0,000022s d in milliseconds = d
						 * * 1000 = 0,000022s * 1000 = 0,022 ms
						 * 
						 * example cont.: one frame lasts 0,022 ms a buffer
						 * holds 1024 bytes a frame has 4 bytes frames per
						 * buffer = 1024/4 = 256 frames
						 * 
						 * one buffer with 256 frames, each lasting 0.000022s or
						 * 0.022ms lasts 256*0.022ms = 5,8ms
						 */
						float sampleRate;
						int frameSize;
						synchronized (format) {
							sampleRate = format.getSampleRate();
							frameSize = format.getFrameSize();
						}

						double frame_duration_millisecs = 1000 / sampleRate;
						int how_many_frames_per_buffer = (int) (buffLen / (double) frameSize);
						long buffer_duration = (long) (frame_duration_millisecs * how_many_frames_per_buffer);

						tEnd = System.currentTimeMillis();
						long tElapsed = tEnd - tBegin;
						long time_to_sleep = buffer_duration - tElapsed;
						if (time_to_sleep > 0)
							Thread.sleep(time_to_sleep);
						tBegin = System.currentTimeMillis();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else { // playing == false
					// don't send anything
				}
			}
		}
	}

	private class ConnectionHandler {
		// connection
		private Socket socket;
		// receive
		private BufferedReader fromClientBr;
		// send
		private OutputStream toClientOutputStream;
		// format sent?
		private AudioFormat old_format = null;

		private PrintWriter toClientPrintWriter;

		public ConnectionHandler(Socket s) {
			try {
				// open streams for communication
				socket = s;
				fromClientBr = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));
				toClientOutputStream = socket.getOutputStream();

				toClientPrintWriter = new PrintWriter(toClientOutputStream);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		// Could be a problem: if format changes, client won't know
		public void streamData(AudioFormat format, byte[] data) {
			if (!format.equals(old_format)) {
				old_format = format;
				// tell client about the audio format
				String formatString = AudioFormatHelper
						.audioFormatToString(format);
				toClientPrintWriter.flush();
				toClientPrintWriter.println(formatString);

			}

			// send music
			String musicString = AudioFormatHelper.bytesToString(data);
			toClientPrintWriter.flush();
			toClientPrintWriter.println(musicString);
		}

		private void close() {
			try {
				fromClientBr.close();
				toClientOutputStream.close();
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}