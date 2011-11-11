package alpv_ws1112.ub1.webradio.webradio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

import alpv_ws1112.ub1.webradio.communication.Client;
import alpv_ws1112.ub1.webradio.proto.ProtoBuf;
import alpv_ws1112.ub1.webradio.proto.PacketProtos.Message;
import alpv_ws1112.ub1.webradio.proto.PacketProtos.Message.Chat;

public class ClientImpl2 implements Client {

	//needed to connect
	private InetSocketAddress address;
	private int port;
	private String protocoll;
	private String username;
	
	//needed to connect to ui
	private ArrayList<String> messages = new ArrayList<String>(); 
	private AtomicBoolean music = new AtomicBoolean(true);
	private AtomicBoolean end = new AtomicBoolean(false);
	
	private UIClient ui;
	
	private InputStream is;
	private OutputStream os;
	private Socket clientsocket;
	
	
	public ClientImpl2(String proto, String serveraddr, String port, String user) {
		this.protocoll = proto;
		this.port = Integer.parseInt(port);
		this.address = new InetSocketAddress(serveraddr, this.port);
		this.username = user;
	}

	//start the gui then starts the client
	public void run() {
		try {
			connect(address);
		} catch (IOException e) {
		}

		close();
	}

	//connect and deal with the connection
	public void connect(InetSocketAddress serverAddress) throws IOException {
		System.out.println("connecting...");
		try {
			if (protocoll.equals("tcp")) {
				try{
					clientsocket = new Socket(serverAddress.getAddress(), port);
				}catch (Exception e) {
					System.out.println("Couldn't reach server. Check parameters.");
				}

				ui = new UIClient(username,this);
				ui.run();
			
				
				
				is = clientsocket.getInputStream();
				os = clientsocket.getOutputStream();

				// Open line
				SourceDataLine line = null;
				String format = null;
				String oldformat = null;
				while (true) {
					if(messages.size()!=0){
						sendChatMessages(os);
					}
					
					Message tm = Message.parseDelimitedFrom(is);
					
					format = tm.getFormat();
					
					byte[] data = tm.getData().toByteArray();
					
					//send chat messages to the ui
					Chat c = tm.getChat();
					ui.pushChat(c);
					
					//if format change open new line
					if (!format.equals(oldformat)) {
						oldformat = format;
						line = AudioSystem.getSourceDataLine(AudioFormatHelper.stringToAudioFormat(format));
						line.open();
						line.start();
					}
					
					//stream music
					if (data != null && music.get()) {
						try {
							line.write(data, 0, data.length);
						} catch (IllegalArgumentException e) {
							System.err.println("Client music write error: "
									+ e.getMessage());
						}
					}
					
					synchronized (ui.jframe) {
						end.set(!ui.jframe.isVisible());
					}
					//if end is set to true break while and shutdown
					if(end.get()){
						is.close();
						os.close();
						clientsocket.close();
						break;
					}
				}

			} else if (protocoll.equals("udp")) {

			} else if (protocoll.equals("mc")) {

			} else
				// do nothing if not correct protocol
				return;
		} catch (Exception e) {
			System.out.println("Make sure you entered the host IP and the port number correctly.");
		}
	}

	//close connections and shutdown
	public void close() {
		ui.jframe.dispose();
		System.out.println("client: shutting down");
		end.set(true);
	}
	

	//add a message to messages until they can be sent
	public void sendChatMessage(String message) throws IOException {
		synchronized (messages) {
			messages.add(message);
		}
	}

	//send all messages that have piled up till now
	private  void sendChatMessages(OutputStream os){
		synchronized(messages){
			Message tm = ProtoBuf.buildMessage(username, messages);
			try {
				tm.writeDelimitedTo(os);
				messages.clear();
			} catch (IOException e) {
				System.err.println("Error while writing Messages");
				e.printStackTrace();
			}
		}
	}

	//mute music
	public void mute() {
		music.set(!music.get());
	}

	
}
