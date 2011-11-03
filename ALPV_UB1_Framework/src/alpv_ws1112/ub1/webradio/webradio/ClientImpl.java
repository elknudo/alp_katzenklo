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
import javax.sound.sampled.SourceDataLine;

import alpv_ws1112.ub1.webradio.communication.Client;

public class ClientImpl implements Client {

	private InetSocketAddress address;
	private int port;
	private String protocoll;
	private String username;
	private Socket clientsocket;

	public ClientImpl(String proto, String serveraddr, String port, String user) {
		this.protocoll = proto;
		this.port = Integer.parseInt(port);
		this.address = new InetSocketAddress(serveraddr, this.port);
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
		try {
			if (protocoll.equals("tcp")) {
				clientsocket = new Socket(serverAddress.getAddress(), port);

				InputStream is = clientsocket.getInputStream();

				BufferedReader br = new BufferedReader(
						new InputStreamReader(is));

				// Open line
				SourceDataLine line = null;
				AudioFormat format = null;
				byte[] music = null;
				String incoming = null;
				while (true) {
					while ((incoming = br.readLine()) == null)
						; // no stuff to read

					if ((format = AudioFormatHelper
							.stringToAudioFormat(incoming)) != null) {
						// format message received
						// System.out.println("Client: format msg received");
						line = AudioSystem.getSourceDataLine(format);
						line.open();
						line.start();
					}
					if ((music = AudioFormatHelper.stringToBytes(incoming)) != null) {
						// music message received
						// System.out.println("Client: music msg received size: "
						// + music.length);
						try {
							line.write(music, 0, music.length);
						} catch (IllegalArgumentException e) {
							System.err.println("Client music write error");
						}
					}
				}

			} else if (protocoll.equals("udp")) {

			} else if (protocoll.equals("mc")) {

			} else
				// do nothing if not correct protocoll
				return;
		} catch (Exception e) {
			e.printStackTrace();
			System.out
					.println("Make sure you entered the host IP and the port number correctly.");
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
