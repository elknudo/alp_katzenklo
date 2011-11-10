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

	private InetSocketAddress address;
	private int port;
	private String protocoll;
	private String username;
	private Socket clientsocket;
	private ArrayList<String> messages = new ArrayList<String>(); 
	private AtomicBoolean music = new AtomicBoolean(true);
	private AtomicBoolean end = new AtomicBoolean(false);
	private UIClient ui;
	
	
	public ClientImpl2(String proto, String serveraddr, String port, String user) {
		this.protocoll = proto;
		this.port = Integer.parseInt(port);
		this.address = new InetSocketAddress(serveraddr, this.port);
		this.username = user;
	}

	@Override
	public void run() {
		ui = new UIClient(username,this);
		ui.run();
	
		try {
			connect(address);
		} catch (IOException e) {
		}

		close();
	}

	@Override
	public void connect(InetSocketAddress serverAddress) throws IOException {
		System.out.println("connecting...");
		try {
			if (protocoll.equals("tcp")) {
				try{
					clientsocket = new Socket(serverAddress.getAddress(), port);
				}catch (Exception e) {
					return;
				}

				InputStream is = clientsocket.getInputStream();
				OutputStream os = clientsocket.getOutputStream();

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
					
					Chat c = tm.getChat();
					ui.pushChat(c);
					
					if (!format.equals(oldformat)) {
						// format message received
						oldformat = format;
//						System.out.println("Client: format msg received");
						line = AudioSystem.getSourceDataLine(AudioFormatHelper.stringToAudioFormat(format));
						line.open();
						line.start();
					}
					if (data != null && music.get()) {
						// music message received
						try {
							line.write(data, 0, data.length);
						} catch (IllegalArgumentException e) {
							System.err.println("Client music write error: "
									+ e.getMessage());
						}
					}
					if(end.get()){
						is.close();
						os.close();
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

	public void close() {
		System.out.println("closing connection");
		try {
			clientsocket.close();
			
		} catch (Exception e) {
			return;
		}
	}

	public void sendChatMessage(String message) throws IOException {
		synchronized (messages) {
			messages.add(message);
		}
	}

	public  void sendChatMessages(OutputStream os){
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

	public void mute() {
		music.set(!music.get());
	}
	
}
