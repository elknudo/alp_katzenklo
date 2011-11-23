package alpv_ws1112.ub1.webradio.webradio;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.sound.sampled.*;

import java.util.concurrent.atomic.*;

import alpv_ws1112.ub1.webradio.communication.Server;
import alpv_ws1112.ub1.webradio.proto.ProtoBuf;
import alpv_ws1112.ub1.webradio.proto.PacketProtos.Message;
import alpv_ws1112.ub1.webradio.proto.PacketProtos.Message.Chat;

/*
 * Server runs in multiple threads
 * 
 * 1) accepting clients 				ClientListener.run()
 * 2) playing music						JukeBox.run()
 * 3) accepting control commands (callers thread)
 * 
 */
public class ServerImpl2 implements Server {
	
	// shared
	private AtomicBoolean running = new AtomicBoolean(true);
	private Vector<ConnectionHandler> connections = new Vector<ServerImpl2.ConnectionHandler>(); // synchronize!
	private Vector<InetAddress> addresses = new Vector<InetAddress>(); // synchronize!
	private Vector<Integer> ports = new Vector<Integer>(); // synchronize!
	
	//ChatReceiver UDP and JukeBox shared (synchronized over users)
	ArrayList<String> messages = new ArrayList<String>();
	ArrayList<String> users = new ArrayList<String>();
	
	// client listener
	private ClientListener listener = null;
	private Thread listener_thread = null;

	// Playback stuff
	private JukeBox jb = null;
	private Thread jbThread = null;
	private ConnectionHandler ch = null;
	
	//UDP socket
	private DatagramSocket datagramsocket ;
	
	//GUI
	private UIServer ui;
	
	// c'tor
	public ServerImpl2(String netprotocol, int port) {
		listener = new ClientListener(netprotocol, port);
		listener_thread = new Thread(listener);
		listener_thread.start();
	}

	public void run() {
		
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
		private ServerSocket serversocket = null;
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
						ch = new ConnectionHandler(
								serversocket.accept());
						// (blocks until new client tries to connect)
						System.out.println("Server: client accepted");
						synchronized (connections) {
							connections.add(ch);
						}

					}
				} catch (Exception e) {
				}

			} 
			//UDP
			else if (netprotocol.equals("udp")) {
				try {
					datagramsocket  = new DatagramSocket(port);
					System.out.println("Server: Listening for " + netprotocol
							+ " Connections on port " + port);
					while (running.get()) {
						// listen and accept new clients
						DatagramPacket p = new DatagramPacket(new byte[10000], 10000);
						datagramsocket.setSoTimeout(10);
						
						//try to receive data from datagramsocket
						while(true && running.get())
						{try{
						datagramsocket.receive(p);
						break;
						}
						catch(Exception e){}
						}
						
						synchronized (addresses) {
							//get data and cut off unused bytes
							byte[] data = p.getData();
							byte[] truncated = new byte[p.getLength()];
							for(int i=0;i<truncated.length;i++)
								truncated[i]=data[i];
							
							//parse Message and add the sended chatmessages to the lists
							Message m = Message.parseFrom(truncated);
							synchronized (users) {
								if(m.hasChat())
								{
									Chat c = m.getChat();
									messages.addAll(c.getMessageList());
									users.addAll(c.getUsernameList());
									System.out.println("chat added");
								}
							}
							if(!addresses.contains(p.getAddress()) || m.getChat().getMessageList().contains("connected")){
									addresses.add(p.getAddress());
									ports.add(p.getPort());
									System.out.println("Server: added User "+p.getAddress().getHostAddress());
							}
							
						}
						

					}
				} catch (Exception e) {
					
				}
			} else if (netprotocol.equals("mc")) {
				;
			} else
				// do nothing if netprotocol is not valid
				return;
			close();
		}

		public void close() {
			if (serversocket!=null)
			synchronized (serversocket) {
				try {
					serversocket.close();
				} catch (IOException e) {
					System.err.println("Client Listener close() error: ");
					e.printStackTrace();
				}
			}
			if(datagramsocket!=null){
				datagramsocket.close();
				datagramsocket = null;
			}
		}
	
	}
	
	public class JukeBox implements Runnable {// behaviour & server state
		// buffer
		private final int buffLen = 4096;

		// shared objects
		// synchronize!
		private AudioInputStream ais;
		private AudioFormat format;
		private AtomicBoolean playing = new AtomicBoolean(false);

		private List<File> files = new ArrayList<File>();
		
		private Integer counter = 0;
		public static final int valueRange = 512;
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
		public void nextSong(){
			synchronized(files){
				while(true && files.size()>0)
					try {
					changeSong(files.get(0).getAbsolutePath());
					files.remove(0);
				} catch (Exception e) {
					files.remove(0);
				}
				
			}
			
		}
		public void addFile(File file){
			synchronized(files){
				files.add(file);
			}
		}

		public void run() {
			ui = new UIServer(jb);
			
			Thread thread = new Thread(ui);
			thread.start();
			
			try {
				thread.sleep(1000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			long tBegin = System.currentTimeMillis();
			long tEnd = 0;
			
			while (running.get()) {
				if(ui.jframe!=null)
					running.set(ui.jframe.isVisible());
				if (playing.get()) {
					// send next sample
					byte[] data = new byte[buffLen];
					// read data
					synchronized (ais) {
						try {
							if(ais.available()==0)
								{
								System.out.println("Server: song finished");
								nextSong();
								}
							
							ais.read(data);
						} catch (IOException e) {
							System.err.println("AudioInputStream.read() error in JukeBox.run()");
							e.printStackTrace();
						}
					}
					// send data
					synchronized (connections) {
						if(connections.size()!=0){
						// Push data from audio stream into network
						
						Iterator<ConnectionHandler> it;
						it = connections.iterator();
						ConnectionHandler ch;
						//check if someone wants to send messages
						while(it.hasNext()){
							ch = it.next();
							try {
								if(ch.fromClientInputStream.available()!=0)
								try {
									Message message = Message.parseDelimitedFrom(ch.fromClientInputStream);
									Chat c = message.getChat();
									//if yes add them to the lists
									users.addAll(c.getUsernameList());
									messages.addAll(c.getMessageList());
									
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						
						//send stream and all messages to everyone
						it = connections.iterator();
						Message tm;
						
						if(messages.size()!=0)
							tm = ProtoBuf.buildMessage(users,messages,AudioFormatHelper.audioFormatToString(format), data);
						else
							tm = ProtoBuf.buildMessage(AudioFormatHelper.audioFormatToString(format), data);
						
						while (it.hasNext()) {
							synchronized (format) {
								try{
									ch = it.next();
									ch.streamData(tm);
								}catch (Exception e){
									it.remove();
									System.out.println("Server: removed client with closed port");
								}
							}
						}
						//empty sent messages
						messages.clear();
						users.clear();
						
						}
					}
					counter++;
					synchronized (addresses) {
						if(addresses.size()!=0){
						// Push data from audio stream into network
						Iterator<InetAddress> it;
						it = addresses.iterator();
						InetAddress ch;
						
						
						//send stream and all messages to everyone
						it = addresses.iterator();
						Iterator<Integer> it2;
						it2 = ports.iterator();
						Message tm;
						synchronized(users){
							
							if(messages.size()!=0)
								tm = ProtoBuf.buildMessage(counter,users,messages,AudioFormatHelper.audioFormatToString(format), data,valueRange);
							else
								tm = ProtoBuf.buildMessage(counter,AudioFormatHelper.audioFormatToString(format), data,valueRange);
							messages.clear();
							users.clear();
						
						}
						
						while (it.hasNext()) {
							synchronized (format) {
								try{
									ch = it.next();
									int po = it2.next();
									
									DatagramPacket p = new DatagramPacket(tm.toByteArray(), tm.toByteArray().length,ch,po);
															
									datagramsocket.send(p);
									
									
								}catch (Exception e){
									it.remove();
									System.out.println("Server: removed client with closed port");
								}
						
							}}
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

			
			close();
		}

		public void stop() {
			synchronized(playing)
				{
				playing.set(false);
				}
		}

		public void start() {
			synchronized(playing)
			{
			playing.set(true);
			}
		}
		
		public void close(){
			System.out.println("Server: shutting down");
			running.set(false);
			playing.set(false);
			try {
				ais.close();
				listener.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private class ConnectionHandler {
		// connection
		private Socket socket;
		// receive
		private OutputStream toClientOutputStream;
		// format sent?
		private InputStream fromClientInputStream;
		
		public ConnectionHandler(Socket s) {
			try {
				// open streams for communication
				socket = s;
				toClientOutputStream = socket.getOutputStream();
				fromClientInputStream = socket.getInputStream();

			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		
		// Could be a problem: if format changes, client won't know
		public void streamData(Message tm) throws IOException {
			tm.writeDelimitedTo(toClientOutputStream);
		}
		private void close() {
			try {
				fromClientInputStream.close();
				toClientOutputStream.close();
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		
	}


}
