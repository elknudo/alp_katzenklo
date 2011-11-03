package alpv_ws1112.ub1.webradio.webradio;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import alpv_ws1112.ub1.webradio.communication.Client;

public class ClientImpl implements Client {

	private InetSocketAddress address;
	private int port;
	private String protocoll;
	private String username;
	private Socket clientsocket;
	
	public ClientImpl(String proto, String serveraddr, String port,
			String user) {
		this.protocoll = proto;
		this.port = Integer.parseInt(port);
		this.address = new InetSocketAddress(serveraddr,this.port);
		this.username = user;
	}

	@Override
	public void run() {
		try {
			connect(address);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		close();
	}

	@Override
	public void connect(InetSocketAddress serverAddress) throws IOException {
		System.out.println("connecting...");
		try{
			if(protocoll.equals("tcp")){
				clientsocket = new Socket(serverAddress.getAddress(), port);
				
				InputStream is = clientsocket.getInputStream();
				ObjectInputStream ois = new ObjectInputStream(is);
				
				Object format = (AudioFormat) ois.readObject();
				final String soundfile = "staticx.wav";
				AudioInputStream ais = AudioSystem.getAudioInputStream(new File(
						soundfile));
				
				System.out.println(format.equals(ais.getFormat()));
				
				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				System.out.println(br.ready());
				while(true){
					if(!br.ready()) break;
					System.out.println(br.readLine());
				}
			}else if (protocoll.equals("udp")){
			
			}else if (protocoll.equals("mc")){
			
			}else
				//do nothing if not correct protocoll
				return;
		}catch (Exception e){
			e.printStackTrace();
			System.out.println("Make sure you entered the host IP and the port number correctly.");
		}
	}

	@Override
	public void close() {
		System.out.println("closing connection");
		try {
			clientsocket.close();
		} catch (Exception e) {
			return;
		}
	}

	@Override
	public void sendChatMessage(String message) throws IOException {
		// TODO Auto-generated method stub
		
	}

	
	
}
